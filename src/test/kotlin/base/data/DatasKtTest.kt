package base.data

import base.vectors.Vector
import base.vectors.VectorByte
import base.vectors.VectorByteImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class DatasKtTest {

    @Test
    fun calculateScoreTest() {
        val bestVector = VectorByteImpl(4) {
            if (it == 0) 10 else 0
        }
        val niceVector = VectorByteImpl(4) {
            if (it == 0) 70 else 10
        }
        val wrongVector = VectorByteImpl(4) {
            if (it == 0) 0 else 10
        }

        val s1: Double = calculateScore(0, bestVector)
        println("s1 : ${s1}")
        val s2: Double = calculateScore(0, niceVector)
        println("s2 : ${s2}")
        val s3: Double = calculateScore(0, wrongVector)
        println("s3 : ${s3}")
        Assertions.assertTrue(s1 > s2)
        Assertions.assertTrue(s2 > s3)
    }

    @Test
    fun negativeValue() {
        val bestVector = VectorByteImpl(4) {
            if (it == 1) -1 else -5
        }
        val niceVector = VectorByteImpl(4) {
            if (it == 0) -4
            else if (it == 1) 2
            else -7
        }
        val wrongVector = VectorByteImpl(4) {
            if (it == 0) 0 else -10
        }
        val s1: Double = calculateScore(1, bestVector)
        println("s1 : ${s1}")
        val s2: Double = calculateScore(1, niceVector)
        println("s2 : ${s2}")
        val s3: Double = calculateScore(1, wrongVector)
        println("s3 : ${s3}")
        Assertions.assertTrue(s1 > s2)
        Assertions.assertTrue(s2 > s3)
    }

    @Test
    fun badScore() {
        val badVector = VectorByteImpl(10) {
            if (it == 4) {
                -104
            }
            else if (it == 9) {
                -106
            }
            else 0
        }
        val score: Double = calculateScore(3, badVector)

        println("score = $score")
    }

    @Test
    fun bytePrevision() {
        val result = VectorByteImpl(4) {
            if (it == 1) 1 else 0
        }
        val item = TrainingItem(VectorByteImpl(4) {
            it.toByte()
        }, 1)

        val predictor: (Vector<Byte>) -> Vector<Byte> = { _ -> result }
        //Assertions.assertTrue(item.scorePrevisionOK(predictor).second)
    }

}
