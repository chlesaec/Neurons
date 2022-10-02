package base.data

import base.vectors.VectorImpl
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DatasKtTest {

    @Test
    fun calculateScore() {
        val bestVector = VectorImpl(4) {
            if (it == 0) 1 else 0
        }
        val niceVector = VectorImpl(4) {
            if (it == 0) 70 else 10
        }
        val wrongVector = VectorImpl(4) {
            if (it == 0) 0 else 10
        }

        val s1: Double = base.data.calculateScore(0, bestVector)
        val s2: Double = base.data.calculateScore(0, niceVector)
        val s3: Double = base.data.calculateScore(0, wrongVector)

        println("s1 : ${s1}")
        println("s2 : ${s2}")
        println("s3 : ${s3}")
    }
}
