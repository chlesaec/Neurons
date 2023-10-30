package base.neurons

import base.vectors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.Assertions
import kotlin.coroutines.CoroutineContext
import kotlin.math.cos
import kotlin.test.Test

internal class LayerTest {

    @Test
    fun channels() {
        runBlocking {
            val channel = Channel<Double>()
            launch {
                for (x in 1..5) {
                    println("\tinside ${x}")
                    channel.send(longMethod())
                }
                channel.close() // we're done sending
            }
// here we print received values using `for` loop (until the channel is closed)
            println("After launch")
            for (y in channel) println(y)
            println("Done!")
        }
    }

    @Test
    fun teco() {
        runBlocking(Dispatchers.Unconfined) {

            val job1 = launch {
                println("Start 1")
                longMethod()
                println("End 1")
            }
            val job2 = launch {
                println("Start 2")
                longMethod()
                println("End 2")
            }
            job1.join()
            job2.join()
        }
    }

    fun longMethod(): Double {
        /*suspendCancellableCoroutine<Unit> {
            cont: CancellableContinuation<Unit> ->
            cont.context.delay.
        }*/
      //  delay(22)
        var result = 0.0
        for (i in 1..10000) {
           // launch(CoroutineContext.) {
                result += cos(i * 3.0)
           // }
        }
        return result
    }

    @Test
    fun test() {
        val layer = SimpleLayer(
            inputAxons = Matrix(3, 3) { 0 },
            thresholds = VectorByteImpl(3) {
                if (it == 0) 0 else 100
            }
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
        val thresholds = VectorByteImpl(1) {
            it.toByte()
        }
        val layer = ConvolutionalLayer(axons, thresholds, Dimension(20, 20))
        val inputVector = VectorByteImpl(400) {
            (it / 3).toByte()

        }
        val result = layer.times(inputVector)
        Assertions.assertEquals(17*17, result.size)

    }
}
