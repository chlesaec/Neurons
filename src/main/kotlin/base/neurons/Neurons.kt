package base.neurons

import base.vectors.Matrix
import base.vectors.Vector
import base.vectors.VectorByte
import base.vectors.VectorByteImpl
import kotlin.math.min
import kotlin.random.Random

interface Layer {
    fun timesDouble(v : Vector<Double>) : Vector<Byte>

    operator fun times(v : Vector<Byte>) : Vector<Byte>

    fun massTimes(v: List<Vector<Byte>>) : List<Vector<Byte>>

    fun randomUpdates(rate : Double) : Layer
}

open class SimpleLayer(val inputAxons : Matrix) : Layer {

    private val random = Random(System.nanoTime())

    override fun timesDouble(v : Vector<Double>) : Vector<Byte> {
        val neuronsOutput : Vector<Double> = (this.inputAxons.timesDouble(v)).normalize()

        return VectorByteImpl(neuronsOutput.size) {
            val byteValue =(neuronsOutput[it] * Byte.MAX_VALUE.toDouble()).toInt().toByte()
            this.buildResult(byteValue)//, this.thresholds[it])
        }
    }

    override operator fun times(v : Vector<Byte>) : Vector<Byte> {
        val neuronsInput : Vector<Byte> = this.inputAxons * v

        val result = VectorByteImpl(neuronsInput.size) {
            index : Int -> buildResult(neuronsInput.get(index))
        }
        return result
    }

    override fun massTimes(v: List<Vector<Byte>>): List<Vector<Byte>> {
        return this.inputAxons.timesMassMeans(v)
            .map {
                it.forEachScalar(this::buildResult)
            }
    }

    inline protected fun buildResult(input : Byte) : Byte {
        return if (input > 0) {
            input
        }
        else {
            0
        }
    }

    override fun randomUpdates(rate : Double) : SimpleLayer {
        val threshold = ((1.0 - min(1.0, rate)) * 10_000L).toLong()
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

        return SimpleLayer(m)
    }

    override fun toString(): String {
        return "Layer(inputAxons=" + System.lineSeparator() + "${inputAxons.toString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleLayer

        return inputAxons == other.inputAxons
    }

    override fun hashCode(): Int {
        return inputAxons.hashCode()
    }

}

data class Dimension(val line: Int, val col: Int) {
    fun size(): Int = this.line * this.col

    fun resultSize(subElement: Dimension): Int {
        return (this.col - subElement.col)*(this.line - subElement.line)
    }
}

class SubVectorByte(val totalVector: Vector<Byte>,
                    val totalDimension: Dimension,
                    val subDimension: Dimension): VectorByte {
    override val size: Int
        get() = subDimension.size()

    var startIndex = 0
        set(value) {
            field = value
            startCol = startIndex % (totalDimension.col + 1 - subDimension.col)
            startLine = startIndex / (totalDimension.col + 1 - subDimension.col)
        }

    private var startCol = startIndex % (totalDimension.col + 1 - subDimension.col)

    private var startLine = startIndex / (totalDimension.col + 1 - subDimension.col)


    override fun get(i: Int): Byte {
        val realIndex = realIndex(i)
        return totalVector.get(realIndex)
    }

    private fun realIndex(index: Int) : Int {
        val realCol = startCol + (index % subDimension.col)
        val realLine = startLine + (index / subDimension.col)
        return realLine*totalDimension.col + realCol
    }

    override fun toString(): String {
        val result = StringBuilder()
        for (i in 0 until subDimension.line) {
            (0..subDimension.col-1).forEach {
                result.append("${this.get(i*subDimension.col + it)}  ")
            }
            result.append(System.lineSeparator())
        }
        return result.toString()
    }

    override fun forEachScalar(f: (Byte) -> Byte): Vector<Byte> {
        TODO("Not yet implemented")
    }
}

class ConvolutionalLayer(inputAxons : Matrix, // axons
                         val inputDimension: Dimension) : SimpleLayer(inputAxons) {

    private val resultDim = Dimension(inputDimension.line + 1 - inputAxons.line, inputDimension.col + 1 - inputAxons.col)

    private val resultSize = resultDim.size()

    private val subDim = Dimension(inputAxons.line, inputAxons.col)

    val axons: VectorByte = inputAxons.asVector()

    override operator fun times(v : Vector<Byte>) : Vector<Byte> {

        val resultByte = ByteArray(resultSize)
        val sub = SubVectorByte(v, inputDimension, subDim)

        for (startIndex: Int in 0 until resultSize) {
            sub.startIndex = startIndex
            val res: Byte = axons * sub
            val result = this.buildResult(res)//, this.thresholds.get(0))
            resultByte[startIndex] = result
        }

        val (dimension, vector) = this.pool(2, resultByte, resultDim)

        return vector
    }

    private fun pool(poolSize:Int, input : ByteArray, inputDimension: Dimension)
        : Pair<Dimension, Vector<Byte>> {
        val dim = Dimension(inputDimension.line / poolSize, inputDimension.col / poolSize)
        val resultSize: Int = dim.size()
        val resultByte = ByteArray(resultSize)

        for (line in 0..<dim.line) {
            for (col in 0..<dim.col) {
                var max = Byte.MIN_VALUE
                for (deltaCol in 0..<poolSize) {
                    for (deltaLine in 0..<poolSize) {
                        val v =
                            input[col * poolSize + deltaCol + (line) * poolSize * inputDimension.col + deltaLine * inputDimension.col]
                        if (v > max) {
                            max = v
                        }
                    }
                }
                resultByte[line*dim.col + col] = max
            }
        }
        return Pair(dim, VectorByteImpl(resultByte))
    }

    override fun massTimes(v: List<Vector<Byte>>): List<Vector<Byte>> {
        // TODO, rework
        return v.map(this::times)
    }

    override fun randomUpdates(rate: Double): ConvolutionalLayer {
        val simpleLayer = super.randomUpdates(rate)
        return ConvolutionalLayer(simpleLayer.inputAxons,
            inputDimension)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ConvolutionalLayer

        if (inputDimension != other.inputDimension) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + axons.hashCode()
        return result
    }


}

class ConvolutionalLayers(val layers : List<ConvolutionalLayer>) : Layer {
    override fun timesDouble(v: Vector<Double>): Vector<Byte> {
        val subResult: List<Vector<Byte>> = layers.map { it.timesDouble(v) }

        val res = ByteArray(subResult.size * subResult[0].size)

        var index = 0
        subResult.forEach {
            it.iterator().forEach {
                res.set(index, it)
                index++
            }
        }

        return VectorByteImpl(res)
    }

    override fun times(v: Vector<Byte>): Vector<Byte> {
        val subResult: List<Vector<Byte>> = layers.map { it.times(v) }

        val res = ByteArray(subResult.size * subResult[0].size)

        var index = 0
        subResult.forEach {
            it.iterator().forEach {
                res.set(index, it)
                index++
            }
        }
        return VectorByteImpl(res)
    }

    override fun randomUpdates(rate: Double): Layer {
        return ConvolutionalLayers(layers.map { it.randomUpdates(rate) })
    }

    override fun massTimes(v: List<Vector<Byte>>): List<Vector<Byte>> {
        //Array()
        val result = mutableListOf<ByteArray>()
        layers.map { it.massTimes(v) }
            .mapIndexed {
                indexLayer: Int, subResult: List<Vector<Byte>> ->

               // println("Layers:${layers.size}; sub result:${subResult.size}; first: ${subResult[0].size}")
                //val res = ByteArray(layers.size * subResult[0].size)
                val resultSize = layers.size * subResult[0].size
                subResult.forEachIndexed {
                    indexResult: Int, r: Vector<Byte> ->
                    var index = 0
                    r.iterator().forEach {
                        while (result.size <= indexResult) {
                            result.add(ByteArray(resultSize))
                        }
                        result[indexResult].set(indexLayer + index, it)
                        index++
                    }
                }

            }
        return result.map { VectorByteImpl(it) }
    }

    private fun max(vector: List<Vector<Byte>>, index: Int) : Byte {
        return vector.maxOf { it.get(index) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConvolutionalLayers

        return layers == other.layers
    }

    override fun hashCode(): Int {
        return layers.firstOrNull()?.hashCode() ?: 0
    }


}


class Brain(val layers : List<Layer>) {
    fun digestDouble(input : Vector<Double>) : Vector<Byte> {
        val firstResult : Vector<Byte> = layers[0].timesDouble(input)

        return layers.subList(1, layers.size)
            .fold(firstResult) {
                    inputVector : Vector<Byte>, layer : Layer -> layer * inputVector
            }
    }

    fun digest(input : Vector<Byte>) : Vector<Byte> {
        return layers
            .fold(input) {
                    inputVector : Vector<Byte>, layer : Layer -> layer * inputVector
            }
    }

    fun massDigest(input : List<Vector<Byte>>) : List<Vector<Byte>> {
        return layers
            .fold(input) {
                    inputVector : List<Vector<Byte>>, layer : Layer ->
                layer.massTimes(inputVector)
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Brain

        return layers == other.layers
    }

    override fun hashCode(): Int {
        return layers.firstOrNull()?.hashCode() ?: 0
    }

}
