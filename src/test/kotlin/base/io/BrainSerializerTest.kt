package base.io

import base.neurons.*
import base.vectors.Matrix
import base.vectors.VectorByteImpl
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BrainSerializerTest {

    @Test
    fun serialize() {
        val layers = base.buildConvolutionLayers(Dimension(4, 5), 5)

        val layer1 = base.buildSimpleLayer(7, 12)

        val layer2 = base.buildSimpleLayer(12, 4)


        val brain = Brain(
            layers = listOf(layers, layer1, layer2)
        )

        val format = Json {
            prettyPrint = false
            serializersModule = module
        }
        val brainJson = format.encodeToString(BrainSerializer, brain)
        println(brainJson)
        val brain2 = format.decodeFromString(BrainSerializer, brainJson)

        val equals = brain.equals(brain2)
        assertTrue(equals)
        assertEquals(brain, brain2)
    }

    private fun buildSimpleLayer(inputSize: Int, outputSize: Int) : Layer {
        val random = Random(System.currentTimeMillis())
        val matrix = Matrix(inputSize, outputSize) {
            random.nextInt().toByte()
        }
        val thresholds = VectorByteImpl(outputSize) { random.nextInt().toByte() }
        return SimpleLayer(matrix, thresholds)
    }

    private fun buildConvolutionLayers(imageSize: Dimension) : ConvolutionalLayers {
        val random = Random(System.currentTimeMillis())
        val layers = mutableListOf<ConvolutionalLayer>()

        repeat(3) {
            val matrix = Matrix(5, 5) {
                random.nextInt().toByte()
            }
            val thresholds = VectorByteImpl(5) { random.nextInt().toByte() }
            val convolutionalLayer = ConvolutionalLayer(matrix, thresholds, imageSize)
            layers.add(convolutionalLayer)
        }
        return ConvolutionalLayers(layers)
    }
}
