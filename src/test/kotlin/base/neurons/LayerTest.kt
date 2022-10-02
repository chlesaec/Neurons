package base.neurons

import base.vectors.Matrix
import base.vectors.RealVector
import base.vectors.Vector
import base.vectors.VectorImpl
import kotlin.test.Test

internal class LayerTest {

    @Test
    fun test() {
        val layer = Layer(
            inputAxons = Matrix(3, 3) { 0 },
            thresholds = VectorImpl(3) {
                if (it == 0) 0 else 100
            }
        )

        val inputData = RealVector(3) {
            (it + 5.0)*0.4
        }

        val result: Vector = layer * inputData
        for (i : Int in 0..result.size - 1) {
            println("r[${i}]= ${result[i]}")
        }
    }
}
