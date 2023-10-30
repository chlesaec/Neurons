package base.io

import base.neurons.*
import base.vectors.Matrix
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BrainSerializerTest {

    @Test
    fun serialize() {
        val layers = buildConvolutionLayers(Dimension(4, 5))

        val layer1 = buildSimpleLayer(7, 12)

        val layer2 = buildSimpleLayer(12, 4)


        val brain = Brain(
            layers = listOf(layers, layer1, layer2)
        )

        val format = Json {
            prettyPrint = false
            serializersModule = module
        }
        val brainJson = format.encodeToString(BrainSerializer, brain)
        val brain2 = format.decodeFromString(BrainSerializer, brainJson)

        assertEquals(brain, brain2)
    }

    private fun buildSimpleLayer(inputSize: Int, outputSize: Int) : Layer {
        val random = Random(System.currentTimeMillis())
        val matrix = Matrix(inputSize, outputSize) {
            random.nextInt().toByte()
        }

        return SimpleLayer(matrix)
    }

    private fun buildConvolutionLayers(imageSize: Dimension) : ConvolutionalLayers {
        val random = Random(System.currentTimeMillis())
        val layers = mutableListOf<ConvolutionalLayer>()

        repeat(3) {
            val matrix = Matrix(5, 5) {
                random.nextInt().toByte()
            }

            val convolutionalLayer = ConvolutionalLayer(matrix, imageSize)
            layers.add(convolutionalLayer)
        }
        return ConvolutionalLayers(layers)
    }
}
