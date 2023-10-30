package base

import base.data.TrainingData
import base.data.TrainingItem
import base.neurons.Brain
import base.vectors.Vector
import kotlinx.coroutines.*
import java.util.*

data class EvaluatedBrain(
    val brain: Brain,
    val score: Double,
    val rate: Double
)

class Brains(val max : Int) {
    val brains : SortedMap<Double, EvaluatedBrain> = TreeMap()

    fun addBrain(brain: EvaluatedBrain) {
            if (this.brains.size < this.max) {
                if (!this.brains.containsKey(brain.score)) {
                    this.brains[brain.score] = brain
                }
            } else {
                val min: Double = this.brains.firstKey()
                if (brain.score > min) {
                    this.brains.remove(min)
                    this.brains[brain.score] = brain
                }
            }
    }

    fun size() : Int = this.brains.size

    fun best() : EvaluatedBrain? {
        if (this.brains.isEmpty()) {
            return null
        }
        return this.brains[this.brains.lastKey()]
    }

    fun mix(others : Brains) : Brains {
        //mutexMix.withLock {
            val best = Brains(this.max)
            this.brains.values.forEach { b: EvaluatedBrain ->
                best.addBrain(b)
            }
            others.brains.values.forEach { b: EvaluatedBrain ->
                best.addBrain(b)
            }
            return best
       // }
    }
}


class Trainer<T>(val initBrain : Brain,
                 val data: () -> TrainingData<T>)
    where T:Number, T:Comparable<T>
{
    fun train() : Brain {
        val brains = Brains(10)
        var trainingData =  data()
        val firstNumber = trainingData.extractOne()
        runBlocking {
            val predictor = this@Trainer.massBytePredictor(this@Trainer.initBrain)
            val score = trainingData.scoreAndRateMass(predictor)
            brains.addBrain(EvaluatedBrain(this@Trainer.initBrain, score.first, score.second))
        }

        var current = brains
        for (index in 0 until 10) {
            val startTime = System.currentTimeMillis()
            println("Epoch ${index}")

            //runBlocking {
                println("Rate=${current.best()?.rate}, Score ${current.best()?.score}")
                current = this.trainBrains(current, trainingData)
           // }
            val duration = System.currentTimeMillis() - startTime
            if (current.best()?.rate ?: 0.0 > 0.9999) {
                break
            }
            trainingData =  data()
            println("---- $duration ----")
        }

        return current.best()!!.brain
    }

    fun test(brain: Brain, data: TrainingData<Byte>) : Double {
        val total = data.items
            .map {
                item: TrainingItem<Byte> ->
                item.scorePrevisionOK(brain::digest).second
            }
            .reduce(Double::plus)
        return total / data.items.size.toDouble()
    }

    private fun getPredictor(number: Number?,
                             brain: Brain): (Vector<T>) -> Vector<Byte> {
        if (number == null) {
            throw IllegalStateException("No data")
        }
        else if (number is Byte) {
            return { v:Vector<T> -> brain.digest(v as Vector<Byte>) }
        }
        else if (number is Double) {
            return  { v:Vector<T> -> brain.digestDouble(v as Vector<Double>) }
        }
        else {
            throw IllegalStateException("Can't analyse vector of ${number.javaClass.name}")
        }
    }

    private fun massBytePredictor(brain: Brain): (List<Vector<T>>) -> List<Vector<Byte>> {
        return { v:List<Vector<T>> -> brain.massDigest(v as List<Vector<Byte>>) }
    }

    private fun trainBrains(brains: Brains, trainingData: TrainingData<T>)  : Brains {

        var newBestBrains = Brains(10).mix(brains)

        repeat(4) {
            val changeRate: Double =  3.0 / (it.toDouble() + 6.0 + (newBestBrains.best()?.rate ?: 0.0))
            println("on tour ${it}, change rate : $changeRate; best rate: ${newBestBrains.best()?.rate ?: 0.0}")

            runBlocking {
                val jobs = mutableListOf<Deferred<Brains>>()
                for (brain: EvaluatedBrain in newBestBrains.brains.values) {
                    val job: Deferred<Brains> = GlobalScope.async {
                        this@Trainer.mutate(brain, changeRate, trainingData)
                    }
                    jobs.add(job)
                }
                /*while (jobs.size < newBestBrains.brains.size) {
                    delay(10)
                }*/
                val startTime = System.currentTimeMillis()
              //  println("trainBrains $it, wait jobs (${jobs.size} == ${newBestBrains.brains.size}")
                val brainsList: List<Brains> = jobs.map { it.await() }
             //   val durationMutates = System.currentTimeMillis() - startTime
               // println("trainBrains $it, duration mutations ${durationMutates} ms")
                newBestBrains = this@Trainer.reduce(brainsList)
                //val durationReduce = System.currentTimeMillis() - startTime - durationMutates
              //  println("trainBrains $it, duration reduce ${durationReduce} ms")
                /*jobs.forEach {
                    val brains: Brains = it.await()
                    newBestBrains = newBestBrains.mix(brains)
                }*/
               // println("--- trainBrains $it ---")
            }
        }
        return newBestBrains
    }

    private suspend fun reduce(brains: List<Brains>) : Brains {
        var current = brains
        while (current.size >= 2) {
            val result = mutableListOf<Brains>()
            repeat(current.size / 2) {
                val mixedBrains = mutableListOf<Deferred<Brains>>()
                val mixed: Deferred<Brains> = GlobalScope.async {
                    current[it*2].mix(current[it*2 + 1])
                }
                mixedBrains.add(mixed)
                mixedBrains.forEach {
                    val brains: Brains = it.await()
                    result.add(brains)
                }
                if (current.size and 1 == 1) {
                    result.add(current.last())
                }
            }
            current = result
        }
        return current[0]
    }

    private fun mutate(startBrain : EvaluatedBrain,
                       rate: Double,
                       trainingData: TrainingData<T>) : Brains {

        val bestBrains = Brains(10)
        val jobs = mutableListOf<Job>()
        runBlocking {
            bestBrains.addBrain(startBrain)
            val number = trainingData.extractOne()

            repeat(15) {
                val job = GlobalScope.launch {
                    val brain = startBrain.brain.randomUpdates(rate)

                    val predictor = this@Trainer.massBytePredictor(brain)
                    val score: Pair<Double, Double> = trainingData.scoreAndRateMass(predictor)

                    bestBrains.addBrain(EvaluatedBrain(brain, score.first, score.second))
                }
                jobs.add(job)
            }
            /*while (jobs.size < 20) {
                delay(10)
            }*/
            jobs.forEach { it.join() }
            //jobs.clear()
            /*println("end mutate, best score ${bestBrains.brains.lastKey()}," +
                    " (worst: ${bestBrains.brains.firstKey()};" +
                    " start=${startBrain.score})")
            println("\t best rate ${bestBrains.best()?.rate}, " +
                    "(worst: ${bestBrains.brains[bestBrains.brains.firstKey()]?.rate}}; " +
                    "start=${startBrain.rate})")*/
        }
        return bestBrains
    }

}
