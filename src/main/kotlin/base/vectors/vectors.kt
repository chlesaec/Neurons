package base.vectors

import java.util.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sqrt

interface Vector<T> : Iterable<T> where T:Number, T:Comparable<T> {
    val size : Int

    operator fun get(i : Int) : T

    operator fun times(v : Vector<T>) : T

    fun timesDouble(v : Vector<Double>) : Double

    fun forEachScalar(f: (T) -> T) : Vector<T>

    fun max() : T {
        return this.select { e1 : T, e2: T -> if (e1 > e2) e1 else e2 }
    }

    fun min() : T {
        return this.select { e1 : T, e2: T -> if (e1 < e2) e1 else e2 }
    }

    private fun select(select : (T, T) -> T) : T {
        var element = this.get(0)
        for (index : Int in 1 until this.size) {
            element = select(this.get(index), element)
        }
        return element
    }

    fun normalize() : RealVector {
        val length = this.length()
        Double.MIN_VALUE
        if (length >= -Double.MIN_VALUE && length <= Double.MIN_VALUE) {
            return RealVector(this.size) {
                0.0
            }
        }
        val l = Math.sqrt(length)
        return RealVector(this.size) {
                index : Int ->  this[index].toDouble() / l
        }
    }

    fun length() : Double {
        return (0..<this.size)
            .map { this[it] }
            .map{ it.toDouble()*it.toDouble()}
            .reduce(Double::plus)
    }

}

interface VectorByte: Vector<Byte> {

    override operator fun times(v : Vector<Byte>) : Byte {
        if (v.size != this.size) {
            throw IllegalArgumentException("This ${size} not equals to input ${v.size}")
        }
        var result : Int = 0
        repeat (this.size) {
            result += this.get(it) * v.get(it)
        }
        result /= v.size
        result = (sign(result.toDouble())*sqrt(abs(result.toDouble()))).toInt()

        return if (result >= Byte.MAX_VALUE) {
            Byte.MAX_VALUE
        }
        else if (result <= Byte.MIN_VALUE) {
            Byte.MIN_VALUE
        }
        else {
            result.toByte();
        }
    }

    override fun timesDouble(v: Vector<Double>): Double {
        var result : Double = 0.0
        for (index : Int in 0 until this.size) {
            result += this[index] * v[index]
        }
        return result
    }

    override fun iterator(): Iterator<Byte> {
        return object : Iterator<Byte> {

            var index = 0

            override fun hasNext(): Boolean {
                return index < this@VectorByte.size
            }

            override fun next(): Byte {
                if (!this.hasNext()) {
                    throw NoSuchElementException("End of vector reached")
                }
                val result = this@VectorByte[this.index]
                this.index++
                return result
            }
        }
    }
}

open class VectorByteImpl(val values: ByteArray) : VectorByte {

    constructor(size : Int, init : (Int) -> Byte)
            : this(ByteArray(size, init))

    override operator fun get(i: Int): Byte {
        return values[i]
    }

    override val size: Int
        get() = this.values.size

    override fun toString(): String {
        return "VectorDirect(${values.contentToString()})"
    }

    override fun iterator(): Iterator<Byte> {
        return this.values.iterator()
    }

    override fun forEachScalar(f: (Byte) -> Byte): Vector<Byte> {
        val newValues = ByteArray(this.values.size) {
            f(this.values[it])
        }
        return VectorByteImpl(newValues)
    }
}

class RealVector(val values: DoubleArray) : Vector<Double> {

    constructor(size : Int, init : (Int) -> Double)
        : this(DoubleArray(size, init))

    override val size: Int
        get() = this.values.size

    override operator fun get(i: Int): Double {
        return values[i]
    }

    override operator fun times(v : Vector<Double>) : Double {
        var result : Double = 0.0
        repeat (this.size) {
            result += values[it] * v.get(it)
        }
        return result
    }

    override fun timesDouble(v: Vector<Double>): Double {
        return this*v
    }

    override fun iterator(): Iterator<Double> {
        return this.values.iterator()
    }

    override fun toString(): String {
        return "RealVector(size=$size, values=${values.contentToString()})"
    }

    override fun forEachScalar(f: (Double) -> Double): Vector<Double> {
        val newValues = DoubleArray(this.values.size) {
            f(this.values[it])
        }
        return RealVector(newValues)
    }
}

data class Matrix(val line : Int, val col : Int, val values: ByteArray) {

    constructor(line : Int, col : Int, init : (Int) -> Byte)
        : this(line, col, ByteArray(line*col, init))

    fun timesDouble(v : Vector<Double>) : Vector<Double> {
        val result : RealVector = if (v.size == this.line) {
            RealVector(this.col) {
                    i : Int -> colVector(i).timesDouble(v)
            }
        }
        else if (v.size == this.col) {
            RealVector(this.line) {
                    i : Int -> lineVector(i).timesDouble(v)
            }
        }
        else {
            throw IllegalArgumentException("Vector not compatible")
        }
        return result
    }

    fun value(l : Int, c: Int) : Byte {
        return this.values[l*col + c]
    }

    fun value(pos : Int) : Byte {
        return this.values[pos]
    }

    operator fun times(v : Vector<Byte>) : Vector<Byte> {
        val result : Vector<Byte> = if (v.size == this.line) {
            val array = ByteArray(this.col)
                repeat(array.size) {
                     array[it] = v * colVector(it)
                }
            VectorByteImpl(array)
        }
        else if (v.size == this.col) {
            val array = ByteArray(this.line)
                repeat(array.size) {
                    array[it] = v *lineVector(it)
                }
            VectorByteImpl(array)
        }
        else {
            throw IllegalArgumentException("Vector not compatible : size:${v.size} for matrix (${this.line}, ${this.col})")
        }
        return result
    }

    fun timesMassMeans(v : List<Vector<Byte>>) : List<Vector<Byte>> {

        val result: List<Vector<Byte>> = if (v[0].size == this.line) {
            val arrays = Array(v.size) { ByteArray(this.col) }

            repeat(this.col) {
                indexCol: Int ->
                val columnVector = colVector(indexCol)
                repeat(v.size) {
                    arrays[it][indexCol] = v[it] * columnVector
                }
            }
            val map:List<Vector<Byte>>  = arrays.map { VectorByteImpl(it) }
            map
        }
        else if (v[0].size == this.col) {
            val arrays = Array(v.size) { ByteArray(this.line) }
            repeat(this.line) {
                indexLine: Int ->
                val lineVector = lineVector(indexLine)
                repeat(v.size) {
                    arrays[it][indexLine] = v[it] * lineVector
                }
            }
            arrays.map { VectorByteImpl(it) }
        }
        else {
            throw IllegalArgumentException("Vector not compatible : size:${v.size} for matrix (${this.line}, ${this.col})")
        }
        return result
    }

    fun timesMatrix(m: Matrix) : Byte {
        assert(this.line == m.line)
        assert(this.col == m.col)

        var result: Int = 0
        for (index in 0 until this.values.size) {
            result += m.values[index] * this.values[index]
        }
        return if (result > Byte.MAX_VALUE) {
            Byte.MAX_VALUE
        }
        else if (result < Byte.MIN_VALUE) {
            Byte.MIN_VALUE
        }
        else {
            result.toByte()
        }
    }


    fun asVector() : VectorByte {
        return VectorByteImpl(this.values)
    }


    override fun toString(): String {
        val result = StringBuilder()
        for (i in 0..line-1) {
            (0..col-1).forEach {
                result.append("${this.value(i, it)}  ")
            }
            result.append(System.lineSeparator())
        }
        return result.toString()
    }

    private class ColumnVector(val colNumber : Int,
        val matrix: Matrix) : VectorByte {
        override val size: Int  = this.matrix.line

        override fun get(i: Int): Byte {
            val index = colNumber + (i * this.matrix.col)
            return this.matrix.values[index]
        }

        override fun forEachScalar(f: (Byte) -> Byte): Vector<Byte> {
            TODO("Not yet implemented")
        }
    }

    private fun colVector(colNumber : Int) : Vector<Byte> {
        return ColumnVector(colNumber, this)
    }

    private class LineVector(val lineNumber : Int,
                             val matrix: Matrix) : VectorByte {
        override val size : Int = this.matrix.col

        val start = lineNumber* this.matrix.col

        override fun get(i : Int) : Byte {
            return this.matrix.values[start + i]
        }

        override fun forEachScalar(f: (Byte) -> Byte): Vector<Byte> {
            TODO("Not yet implemented")
        }
    }

    private fun lineVector(lineNumber : Int) : Vector<Byte> {
        return LineVector(lineNumber, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Matrix

        if (line != other.line) return false
        if (col != other.col) return false
        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = line
        result = 31 * result + col
        result = 31 * result + Arrays.copyOf(values, min(3, values.size)).contentHashCode()
        return result
    }
}
