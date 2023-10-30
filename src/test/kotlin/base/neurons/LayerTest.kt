package base.neurons

import base.vectors.Matrix
import base.vectors.RealVector
import base.vectors.Vector
import base.vectors.VectorByteImpl
import org.junit.jupiter.api.Assertions
import kotlin.test.Test

internal class LayerTest {

    @Test
    fun test() {
        val layer = SimpleLayer(
            inputAxons = Matrix(3, 3) { 0 }
        )

        val inputData = RealVector(3) {
            (it + 5.0)*0.4
        }

        val result: Vector<Byte> = layer.timesDouble(inputData)
        for (i : Int in 0..result.size - 1) {
            println("r[${i}]= ${result[i]}")
        }
    }

    @Test
    fun convolution() {
        val axons = Matrix(3, 3) {
            val line = it / 3
            val col = it % 3
            if (line != col) {
                0
            }
            else if (line == 2) {
                -1
            }
            else {
                1
            }
        }

        val layer = ConvolutionalLayer(axons, Dimension(20, 20))
        val inputVector = VectorByteImpl(400) {
            (it / 3).toByte()
        }
        val result = layer.times(inputVector)
        Assertions.assertEquals(81, result.size)
    }
}
