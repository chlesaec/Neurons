package base

import base.data.TrainingData
import base.neurons.Brain
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class Brains(val max : Int) {
    val brains : SortedMap<Double, Brain> = TreeMap()

    fun addBrain(score: Double, brain: Brain) {
        if (this.brains.size < this.max) {
            this.brains[score] = brain
        }
        else {
            val min : Double = this.brains.firstKey()
            if (score > min) {
                this.brains.remove(min)
                this.brains[score] = brain
            }
        }
    }

    fun brains() : Collection<Brain> = this.brains.values

    fun best() : Brain {
        return this.brains[this.brains.lastKey()]!!
    }

    fun mix(others : Brains) : Brains {
        val best = Brains(this.max)
        this.brains.forEach{ score: Double, b : Brain ->
            best.addBrain(score, b)
        }
        others.brains.forEach{ score: Double, b : Brain ->
            best.addBrain(score, b)
        }
        return best
    }
}

class Trainer(val initBrain : Brain,
              val data: TrainingData) {


    fun train() : Brain {
        val brains = Brains(10)
        val score = data.tryScore(this.initBrain)
        brains.addBrain(score, this.initBrain)

        var current = brains
        (1..20).forEach {
            println("Current ${it}")
            val score = data.tryScore(current.best())
            println("Score ${score}")
            current = this.trainBrains(current)
            println("-------")
        }

        return current.best()
    }

    private fun trainBrains(brains: Brains)  : Brains {
        var b = brains
        (1..30).forEach {
            val rate: Double = 0.8 / it.toDouble()
            b = b.brains().map {
                this.tryLoop(it, rate)
            }.reduce(Brains::mix)
        }
        return b
    }

    private fun tryLoop(startBrain : Brain, rate: Double) : Brains {
        val bestBrains = Brains(10)
        val startScore : Double = data.tryScore(startBrain)
        bestBrains.addBrain(startScore, startBrain)

        (1..20).forEach {
            val b = startBrain.randomUpdates(rate);
            val score : Double = data.tryScore(b)
            bestBrains.addBrain(score, b)
        }
        return bestBrains
    }

}
