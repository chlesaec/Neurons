package base.mnist

import base.data.TrainingData
import base.data.TrainingItem
import base.neurons.Dimension
import base.vectors.Vector
import base.vectors.VectorByte

data class Mnists(val size: Int,
                  val labels: ByteArray,
                  val imageSize: Dimension,
                  val images: List<MnistImage>) {

    fun toItem(index: Int) : TrainingItem<Byte> {
        return TrainingItem(images[index], labels[index].toInt())
    }

    fun toTrainingData(start: Int, size: Int) : TrainingData<Byte> {
        val items: List<TrainingItem<Byte>> = List(size) {
            toItem(it + start)
        }
        //val items: List<ByteTrainingItem> = images.subList(start, start + size).mapIndexed { index, mnistImage -> toItem(index) }

        return TrainingData(items)
    }

}



class MnistImage(val data: UByteArray,
                 val imageSize: Dimension): VectorByte {

    override val size: Int
        get() = imageSize.size()

    override fun get(i: Int): Byte = this.data[i].toByte()

    fun print() {
        println(this.toString())
    }

    fun get(line:Int, col:Int): UByte {
        val index = line*imageSize.col + col
        assert(index < imageSize.size())
        return this.data[index]
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        for (line : Int in 0..< imageSize.line) {
            this.lineToBuffer(line, buffer)
        }
        return buffer.toString()
    }

    private fun lineToBuffer(line: Int, buffer: StringBuffer) {
        for (column : Int in line*imageSize.col..<(line + 1)*imageSize.col) {
            buffer.append("-%02X".format(this.data[column].toByte()))
        }
        buffer.append(System.lineSeparator())
    }

    override fun forEachScalar(f: (Byte) -> Byte): Vector<Byte> {
        TODO("Not yet implemented")
    }
}
