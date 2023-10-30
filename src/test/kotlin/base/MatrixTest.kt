package base

import base.vectors.Matrix
import base.vectors.RealVector
import base.vectors.VectorByteImpl
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import kotlin.random.Random


import kotlin.test.Test

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

        val result = matrix.timesDouble(vector)
        Assertions.assertNotNull(result)
        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(1.5 + 3.0*2.0 + 4.5*3.0, result.get(0), 0.0000001);
        Assertions.assertEquals(1.5*4.0 + 3.0*5.0 + 4.5*6.0, result.get(1), 0.0000001);

        val vector2 = RealVector(2) {
                index : Int -> (index + 1) * 1.5  // 1.5, 3
        }
        val result2 = matrix.timesDouble(vector2)
        Assertions.assertEquals(3, result2.size)
    }

    @Test
    fun timesSimple() {
        val matrix = Matrix(2, 3 ) {
            // 1, 2, 3
            // 4, 5, 6
                index : Int -> (index + 1).toByte()
        }

        val vector = VectorByteImpl(3) {
                index : Int -> (index + 1).toByte()
        }

        val result = matrix * vector
        Assertions.assertNotNull(result)

        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(1 + 2*2 + 3*3, result.get(0));
        Assertions.assertEquals(4 + 5*2 + 6*3, result.get(1));

        val vector2 = VectorByteImpl(2) {
                index : Int -> (index + 1).toByte()
        }
        val result2 = matrix * vector2
        Assertions.assertEquals(3, result2.size)
        Assertions.assertEquals(1 + 2*4, result2.get(0));
        Assertions.assertEquals(2 + 5*2, result2.get(1));
        Assertions.assertEquals(3 + 6*2, result2.get(2));
    }

    @Test
    fun testTimes() {
        val matrix = Matrix(2, 3 ) {
            // 1, 2, 3
            // 4, 5, 6
                index : Int -> (index + 1).toByte()
        }
        val vector = VectorByteImpl(3) {
                index : Int -> (index + 1).toByte()   // 1.5, 3, 3.5
        }
        val result = matrix * vector
        Assertions.assertNotNull(result)
        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(1 + 2*2 + 3*3, result.get(0));
        Assertions.assertEquals(4 + 2*5 + 3*6, result.get(1));

        val vectorMax = VectorByteImpl(3) { Byte.MAX_VALUE }
        val resultMax = matrix * vectorMax
        Assertions.assertEquals(2, resultMax.size)
        Assertions.assertEquals(Byte.MAX_VALUE, resultMax.get(0))
        Assertions.assertEquals(Byte.MAX_VALUE, resultMax.get(1))

        val vectorMin = VectorByteImpl(3) { Byte.MIN_VALUE }
        val resultMin = matrix * vectorMin
        Assertions.assertEquals(2, resultMin.size)
        Assertions.assertEquals(resultMin.get(0), Byte.MIN_VALUE)
        Assertions.assertEquals(resultMin.get(1), Byte.MIN_VALUE)
    }

    @Test
    fun testMass() {
        val matrix = Matrix(2, 3 ) {
            // 1, 2, 3
            // 4, 5, 6
            index : Int -> (index + 1).toByte()
        }
        val vector = VectorByteImpl(3) {
                index : Int -> (index + 1).toByte()   // 1, 2, 3
        }
        val vector2 = VectorByteImpl(3) {
                index : Int -> (index + 2).toByte()   // 2, 3, 4
        }
        val result = matrix.timesMassMeans(listOf(vector, vector2))
        Assertions.assertNotNull(result)
        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(1 + 2*2 + 3*3, result[0].get(0));
        Assertions.assertEquals(4 + 2*5 + 3*6, result[0].get(1));

        Assertions.assertEquals(1*2 + 2*3 + 3*4, result[1].get(0));
        Assertions.assertEquals(4*2 + 5*3 + 6*4, result[1].get(1));

        val matrix2= Matrix(2, 3 ) {
            //  1, -2,  3
            // -4,  5, -6
            index : Int ->
                var result = (index + 1).toByte()
                if ((index and 1) > 0)
                    (-result).toByte()
                else result
        }
        val result1 = matrix2.timesMassMeans(listOf(vector, vector2))
        Assertions.assertNotNull(result1)

    }

    @Test
    fun bigMatrixValues() {
        val matrix = Matrix(2, 3 ) {
            //  1, -2,  3
            // -4,  5, -6
                index : Int ->
            var result = (index*40 + 1).toByte()
            if ((index and 1) > 0)
                (-result).toByte()
            else result
        }
        val vector = VectorByteImpl(3) {
                index : Int -> (index*20 + 1).toByte()   // 1, 2, 3
        }
        val vector2 = VectorByteImpl(3) {
                index : Int -> (index*20 + 2).toByte()   // 2, 3, 4
        }
        val result = matrix.timesMassMeans(listOf(vector, vector2))
        Assertions.assertNotNull(result)
    }


    @Test
    fun runBlock() {
        runBlocking {

            val random = Random(23)
            val jobs = mutableListOf<Job>()
            repeat(20) {
                val job = GlobalScope.launch {
                    var sum = 0
                    repeat(random.nextInt(20)) {
                        sum += it
                    }

                    println("Sum for $it is $sum; thread ${Thread.currentThread().id}")
                }
                jobs.add(job)
            }
            while (jobs.size < 20) {
                delay(4)
            }
            jobs.forEach { it.join() }
        }

        println("end")
    }
}
