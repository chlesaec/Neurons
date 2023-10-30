package base.neurons

import base.vectors.VectorByteImpl
import org.junit.jupiter.api.Assertions
import kotlin.test.Test


internal class SubVectorTestOld {

    @Test
    fun get() {
        val v1 = VectorByteImpl(64) { it.toByte() } // 8 x 8
        // 0  1  2  3  4  5  6  7
        // 8  9 10 11 12 13 14 15
        //16 17 18 19 20 21 22 23
        //24 25 26 27 28
        val sub = SubVectorByte(totalVector = v1,
            totalDimension = Dimension(8,8),
            subDimension = Dimension(3, 3))
        sub.startIndex = 0
        Assertions.assertEquals(0, sub.get(0))
        Assertions.assertEquals(1, sub.get(1))
        Assertions.assertEquals(2, sub.get(2))
        Assertions.assertEquals(8, sub.get(3))
        Assertions.assertEquals(9, sub.get(4))
        Assertions.assertEquals(10, sub.get(5))
        Assertions.assertEquals(16, sub.get(6))
        Assertions.assertEquals(17, sub.get(7))
        Assertions.assertEquals(18, sub.get(8))

        sub.startIndex = 1
        Assertions.assertEquals(1, sub.get(0))
        Assertions.assertEquals(2, sub.get(1))
        Assertions.assertEquals(3, sub.get(2))
        Assertions.assertEquals(9, sub.get(3))
        Assertions.assertEquals(10, sub.get(4))
        Assertions.assertEquals(11, sub.get(5))
        Assertions.assertEquals(17, sub.get(6))
        Assertions.assertEquals(18, sub.get(7))
        Assertions.assertEquals(19, sub.get(8))

        sub.startIndex = 5
        Assertions.assertEquals(5, sub.get(0))
        Assertions.assertEquals(6, sub.get(1))
        Assertions.assertEquals(7, sub.get(2))
        Assertions.assertEquals(13, sub.get(3))
        Assertions.assertEquals(14, sub.get(4))
        Assertions.assertEquals(15, sub.get(5))
        Assertions.assertEquals(21, sub.get(6))
        Assertions.assertEquals(22, sub.get(7))
        Assertions.assertEquals(23, sub.get(8))


        sub.startIndex = 6
        Assertions.assertEquals(8, sub.get(0))
        Assertions.assertEquals(9, sub.get(1))
        Assertions.assertEquals(10, sub.get(2))
        Assertions.assertEquals(16, sub.get(3))
        Assertions.assertEquals(17, sub.get(4))
        Assertions.assertEquals(18, sub.get(5))
        Assertions.assertEquals(24, sub.get(6))
        Assertions.assertEquals(25, sub.get(7))
        Assertions.assertEquals(26, sub.get(8))

        sub.startIndex = 23

        Assertions.assertEquals(sub.get(1), (sub.get(0) + 1).toByte())
    }
}
