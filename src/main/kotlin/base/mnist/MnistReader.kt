package base.mnist

import base.neurons.Dimension
import java.io.File
import java.io.InputStream


class Reader {

    fun readFile(dataFileName: String,
                 labelFileName: String): Mnists {
        val labels: ByteArray = this.readLabels(labelFileName)

        return File(dataFileName).inputStream().use {
            input: InputStream ->

            val buffer = ByteArray(12)
            input.read(buffer, 0, 4) // magic number
            input.read(buffer, 0, 12)
            val nbeImage = this.nextInt(buffer, 0)
            val height = this.nextInt(buffer, 4)
            val width = this.nextInt(buffer, 8)

            val imageSize = Dimension(height, width)

            val mnist = this.readImages(input, imageSize, nbeImage)
            Mnists(nbeImage, labels, imageSize, mnist)
        }

    }

    private fun readImages(input: InputStream, size: Dimension, nbe: Int) : List<MnistImage> {
        return List(nbe) { readImage(input, size) }
    }

    private fun readImage(input: InputStream, size: Dimension) : MnistImage {
        val image = ByteArray(size.size())
        input.read(image, 0, size.size())
        return MnistImage(image.toUByteArray(), size)
    }

    fun readLabels(fileName: String) : ByteArray {
        return File(fileName).inputStream().use {
            input: InputStream ->

            val buffer = ByteArray(8)
            input.read(buffer, 0, 8)
            val nbeLabel = this.nextInt(buffer, 4)

            val bufferLabels = ByteArray(nbeLabel)
            input.read(bufferLabels)
            bufferLabels
        }
    }

    private fun nextInt(buffer: ByteArray, start: Int) : Int {
        var nbe : Int = 0
        nbe = nbe.plus(toUnsignedInt(buffer[start + 3]))
        nbe = nbe.plus(toUnsignedInt(buffer[start + 2]) shl Byte.SIZE_BITS)
        nbe = nbe.plus(toUnsignedInt(buffer[start + 1]) shl (Byte.SIZE_BITS*2))
        nbe = nbe.plus(toUnsignedInt(buffer[start + 0]) shl (Byte.SIZE_BITS*3))
        return nbe
    }

    private fun toUnsignedInt(n: Byte): Int {
        return n.toInt() and 0xff
    }


}
