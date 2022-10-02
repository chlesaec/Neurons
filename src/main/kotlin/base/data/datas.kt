package base.data

import base.neurons.Brain
import base.vectors.RealVector
import base.vectors.Vector
import kotlin.math.pow

fun calculateScore(referencedIndex : Int, values : Vector) : Double {
    val normalize = values.normalize()
    var distance : Double = 0.0
    for (i : Int in 0..normalize.size - 1) {
        val rawValue = normalize[i].toDouble() / Byte.MAX_VALUE.toDouble();
        if (i == referencedIndex) {
            distance += Math.pow(1.0 - rawValue, 2.0)
        }
        else {
            distance += rawValue.pow(2.0)
        }
    }

    if (distance < 0.000001) {
        return 1000000.0
    }
    return 1.0 / distance
}

data class TrainingItem(val input : RealVector, val indexResult: Int) {
    fun tryScore(brain: Brain) : Double {
        val predictions: Vector = brain.digest(this.input)
        return calculateScore(indexResult, predictions)
    }
}

class TrainingData(val items : List<TrainingItem>) {

    fun tryScore(brain: Brain) : Double {
        return this.items
            .map { it.tryScore(brain) }
            .reduce(Double::plus) / items.size
    }
}
