package base.neurons

import base.vectors.Matrix
import base.vectors.RealVector
import base.vectors.Vector
import base.vectors.VectorImpl
import kotlin.math.abs
import kotlin.random.Random


class Layer(val inputAxons : Matrix,
            val thresholds : Vector) {

    private val random = Random(System.nanoTime())

    operator fun times(v : RealVector) : Vector {
        val neuronsInput : RealVector = this.inputAxons * v
        var min : Double = neuronsInput[0]
        var max : Double = neuronsInput[0]
        for (index in 1 until v.size) {
            if (neuronsInput[index] < min) {
                min = neuronsInput[index]
            }
            if (neuronsInput[index] > max) {
                max = neuronsInput[index]
            }
        }

        return neuronsInput.toVector {
            index : Int, value : Double ->
            val v: Double = (value / (max - min)) * Byte.MAX_VALUE
            val byteValue = v.toInt().toByte();
            this.buildResult(byteValue, this.thresholds[index])
        }
    }

    operator fun times(v : Vector) : Vector {
        val neuronsInput : Vector = this.inputAxons * v
        return VectorImpl(neuronsInput.size) {
                index : Int -> buildResult(neuronsInput.get(index), this.thresholds.get(index))
        }
    }


    private fun buildResult(input : Byte, threshold : Byte) : Byte {
        val distance : Int = input - threshold;
        if (distance > -30 && distance < 30) {
            return (Byte.MAX_VALUE - abs(distance).toByte()).toByte();
        }
        if (distance < -200 || distance > 200) {
            return (Byte.MIN_VALUE + (abs(distance)/10).toByte()).toByte()
        }
        return 0;
    }

    fun randomUpdates(rate : Double) : Layer {
        val threshold = ((1 - rate) * 10_000L).toLong()
        val m = Matrix(this.inputAxons.line, this.inputAxons.col)
        {
            p: Int ->
            val change = this.random.nextLong(10_000L)
            if (change > threshold) {
                this.random.nextInt().toByte()
            }
            else {
                this.inputAxons.value(p)
            }
        }

        val t = VectorImpl(this.thresholds.size) {
            p : Int ->
            val change = this.random.nextLong(10_000L)
            if (change > threshold) {
                this.random.nextInt().toByte()
            }
            else {
                this.thresholds[p]
            }
        }

        return Layer(m, t )
    }

    override fun toString(): String {
        return "Layer(inputAxons=" + System.lineSeparator() + "${inputAxons.toString()}, " + System.lineSeparator() +
                "thresholds=${thresholds.toString()})"
    }


}

class Brain(val layers : List<Layer>) {
    fun digest(input : RealVector) : Vector {
        val firstResult : Vector = layers[0] * input

        return layers.subList(1, layers.size)
            .fold(firstResult) {
                inputVector : Vector, layer : Layer -> layer * inputVector
            }
    }

    fun randomUpdates(rate : Double) : Brain {
        val updateLayers = this.layers.map { it.randomUpdates(rate) }
        return Brain(updateLayers)
    }

    override fun toString(): String {
        return this.layers
            .map(Layer::toString)
            .reduce {
                s1: String, s2 : String -> s1 + System.lineSeparator() + s2
            }
    }
}
