package base

import base.vectors.Matrix
import base.vectors.RealVector
import base.vectors.VectorImpl
import org.junit.jupiter.api.Assertions


import org.junit.jupiter.api.Test

internal class MatrixTest {

    @Test
    fun times() {
        val matrix = Matrix(2, 3 ) {
            // 1, 2, 3
            // 4, 5, 6
            index : Int -> (index + 1).toByte()
        }

        val vector = RealVector(3) {
            index : Int -> (index + 1) * 1.5  // 1.5, 3, 3.5
        }

        val result = matrix * vector
        Assertions.assertNotNull(result)
        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(1.5 + 3.0*2.0 + 4.5*3.0, result.get(0), 0.0000001);
        Assertions.assertEquals(1.5*4.0 + 3.0*5.0 + 4.5*6.0, result.get(1), 0.0000001);
    }

    @Test
    fun testTimes() {
        val matrix = Matrix(2, 3 ) {
            // 1, 2, 3
            // 4, 5, 6
                index : Int -> (index + 1).toByte()
        }
        val vector = VectorImpl(3) {
                index : Int -> (index + 1).toByte()   // 1.5, 3, 3.5
        }
        val result = matrix * vector
        Assertions.assertNotNull(result)
        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(1 + 2*2 + 3*3, result.get(0));
        Assertions.assertEquals(4 + 2*5 + 3*6, result.get(1));

        val vectorMax = VectorImpl(3) { Byte.MAX_VALUE }
        val resultMax = matrix * vectorMax
        Assertions.assertEquals(2, resultMax.size)
        Assertions.assertEquals(resultMax.get(0), Byte.MAX_VALUE)
        Assertions.assertEquals(resultMax.get(1), Byte.MAX_VALUE)

        val vectorMin = VectorImpl(3) { Byte.MIN_VALUE }
        val resultMin = matrix * vectorMin
        Assertions.assertEquals(2, resultMin.size)
        Assertions.assertEquals(resultMin.get(0), Byte.MIN_VALUE)
        Assertions.assertEquals(resultMin.get(1), Byte.MIN_VALUE)
    }
}
