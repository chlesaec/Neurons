package base.vectors

import java.lang.StringBuilder

interface Vector {
    val size : Int

    operator fun get(i : Int) : Byte

    operator fun times(v : Vector) : Byte {
        if (v.size != this.size) {
            throw IllegalArgumentException("")
        }
        var result : Int = 0
        for (index : Int in 0 until this.size) {
            result += this.get(index) * v.get(index)
        }
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

    fun select(select : (Byte, Byte) -> Byte) : Byte {
        var element = this.get(0)
        for (index : Int in 1 until this.size) {
            element = select(this.get(index), element)
        }
        return element
    }

    fun max() : Byte {
        return this.select { byte1 : Byte, byte2: Byte ->  if (byte1 > byte2) byte1 else byte2 }
    }

    fun min() : Byte {
        return this.select { byte1 : Byte, byte2: Byte ->  if (byte1 < byte2) byte1 else byte2 }
    }

    fun normalize() : Vector {
        val l = Math.sqrt(length().toDouble())

        return VectorImpl(this.size) {
            index : Int ->
             (((this.get(index)).toDouble()/l)*Byte.MAX_VALUE.toDouble()).toInt().toByte()
        }
    }

    fun length() : Int {
        return (0..this.size-1)
            .map { this.get(it) }
            .map{ it.toInt()*it.toInt()}
            .reduce(Int::plus)
    }


}

class VectorImpl(override val size : Int, private val init : (Int) -> Byte) : Vector {
    private val values = ByteArray(this.size, this.init)

    override operator fun get(i: Int): Byte {
        return values[i]
    }

    override fun toString(): String {
        return super.toString()
    }
}

class RealVector(val size : Int, private val init : (Int) -> Double) {
    private val values = DoubleArray(this.size, this.init)

    operator fun get(i: Int): Double {
        return values[i]
    }

    operator fun times(v : Vector) : Double {
        var result : Double = 0.0
        for (index : Int in 0 until this.size) {
            result += values[index] * v.get(index)
        }
        return result
    }

    operator fun times(v : RealVector) : Double {
        var result : Double = 0.0
        for (index : Int in 0 until this.size) {
            result += values[index] * v.get(index)
        }
        return result
    }

    fun toVector(convert : (Int, Double) -> Byte) : Vector {
        return VectorImpl(this.size) {
            index : Int -> convert(index, this.values[index])
        }
    }

}

class Matrix(val line : Int, val col : Int, private val init : (Int) -> Byte) {
    private val values = ByteArray(this.line*this.col, this.init)

    operator fun times(v : RealVector) : RealVector {
        val result : RealVector = if (v.size == this.line) {
            RealVector(this.col) {
                    i : Int -> v * colVector(i)
            }
        }
        else if (v.size == this.col) {
            RealVector(this.line) {
                    i : Int -> v * lineVector(i)
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

    operator fun times(v : Vector) : Vector {
        val result : Vector = if (v.size == this.line) {
            VectorImpl(this.col) {
                    i : Int -> v * colVector(i)
            }
        }
        else if (v.size == this.col) {
            VectorImpl(this.line) {
                i : Int -> v * lineVector(i)
            }
        }
        else {
            throw IllegalArgumentException("Vector not compatible")
        }
        return result
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

    private fun colVector(colNumber : Int) : Vector {
        return object : Vector {
            override val size: Int  = this@Matrix.line

            override fun get(i: Int): Byte {
                val index = colNumber + (i * this@Matrix.line)
                return this@Matrix.values[index]
            }
        }
    }

    private fun lineVector(lineNumber : Int) : Vector {
        val start = lineNumber*col;
        val v = object : Vector {
            override val size : Int = this@Matrix.col
            override fun get(i : Int) : Byte {
                return this@Matrix.values[start + i]
            }
        }
        return v
    }
}
