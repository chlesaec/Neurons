package base.mnist

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions

import kotlin.test.Test

import java.io.File
import java.io.InputStream

class ReaderTest {

    @Test
    fun readFile() {
        val resourceImages = Thread.currentThread().contextClassLoader.getResource("./train-images-idx3-ubyte")
        val resourceLabels = Thread.currentThread().contextClassLoader.getResource("./train-labels-idx1-ubyte")
        val reader = Reader()
        val mnists: Mnists = reader.readFile(resourceImages.path, resourceLabels.path)

        Assertions.assertNotNull(mnists)
        Assertions.assertEquals(60000, mnists.size)
        Assertions.assertEquals(28, mnists.imageSize.col)
        Assertions.assertEquals(28, mnists.imageSize.line)

        val firstImage = mnists.images[0]
        Assertions.assertNotNull(firstImage)
        Assertions.assertEquals(5, mnists.labels[0])

        firstImage.print()
    }

}

