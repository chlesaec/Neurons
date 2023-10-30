package base.data

import base.EvaluatedBrain
import base.vectors.RealVector
import base.vectors.Vector
import kotlin.math.exp
import kotlin.math.sqrt

fun softMax(values : RealVector): Pair<Double, DoubleArray> {
    val exponential = DoubleArray(values.size) {
        exp(values[it])
    }
    val sum: Double = exponential.sum()
    return Pair(sum, exponential)
}

fun calculateScore(referencedIndex : Int, values : Vector<Byte>) : Double {
    var min = Byte.MAX_VALUE
    values.iterator().forEach {
        if (it < min) {
            min = it
        }
    }

    var suml = values.length()
    if (suml == 0.0) {
        suml = 1.0
    }


    var score = 0.0
    values.iterator().withIndex().forEach {
        if (it.index == referencedIndex) {
            score += (it.value.toDouble() - min.toDouble())/suml
        }
        else {
            score += -(it.value.toDouble() - min.toDouble())/suml
        }
    }
/*println("score : $score")

    val unitPredictions: RealVector = values.normalize()
    val perfect = RealVector(values.size) {
        if (it == referencedIndex)
            1.0
        else
            -1.0
    }
    return perfect * unitPredictions*/
    return score
}

data class TrainingItem<T>(val input : Vector<T>, val indexResult: Int)
    where T:Number, T:Comparable<T> {

    fun scorePrevisionOK(predictor: (Vector<T>) -> Vector<Byte>) : Pair<Double, Double> {
        val predictions: Vector<Byte> = predictor(this.input)
        return score(predictions)
    }

    fun score(predictions: Vector<Byte>): Pair<Double, Double> {
        //val score = calculateScore(indexResult, predictions)

        var maxIndex = 0
        repeat(predictions.size) {
            if (predictions[it] > predictions[maxIndex]) {
                maxIndex = it
            }
        }
        var nbeMax = 0
        var nbeOK = 0.0
        repeat(predictions.size) {
            if (predictions[it] == predictions[maxIndex]) {
                nbeMax++
                if (maxIndex == it) {
                    nbeOK = 1.0
                }
            }
        }
//println("nbe max ${nbeMax}; prediction : ${predictions[maxIndex]}, prev:${maxIndex}, vs:${indexResult}")
        val rate = nbeOK / nbeMax.toDouble()

        return Pair(rate,  rate)
    }

    fun extractOne(): Number? {
        if (this.input.size == 0) {
            return null
        }
        return input[0]
    }
}

class TrainingData<T>(val items : List<TrainingItem<T>>)
    where T:Number, T:Comparable<T>{

    fun scoreAndRate(predictor: (Vector<T>) -> Vector<Byte>): Pair<Double, Double> {
        val result = this.items
            .map { item -> item.scorePrevisionOK(predictor) }
            .fold(Pair(0.0, 0.0)) {
                    buffer: Pair<Double, Double>,
                    scoreAndPrev: Pair<Double, Double> ->
                Pair(buffer.first + scoreAndPrev.first,
                    buffer.second + scoreAndPrev.second)
            }

        return Pair(result.first / this.items.size, result.second / this.items.size)
    }

    fun scoreAndRateMass(predictor: (List<Vector<T>>) -> List<Vector<Byte>>): Pair<Double, Double> {
        val predictions: List<Vector<Byte>> = predictor(this.items.map {
            it.input
        })
        val (score, rate) = predictions.mapIndexed { index: Int, pred: Vector<Byte> ->
            this.items[index].score(pred)
        }
            .fold(Pair(0.0, 0.0)) { buffer: Pair<Double, Double>,
                                  scoreAndPrev: Pair<Double, Double> ->
                Pair(
                    buffer.first + scoreAndPrev.first,
                    buffer.second + scoreAndPrev.second
                )
            }

        return Pair(score / this.items.size, rate / this.items.size)
    }


    fun extractOne(): Number? {
        if (items.isEmpty()) {
            return null
        }
        val firstData = items[0]
        return firstData.extractOne()
    }

}

