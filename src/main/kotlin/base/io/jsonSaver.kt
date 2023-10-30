package base.io

import base.neurons.*
import base.vectors.Matrix
import base.vectors.VectorByteImpl
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.reflect.KType
import kotlin.reflect.typeOf

val module = SerializersModule {
    polymorphicDefaultSerializer(Layer::class) {
        instance: Layer ->
        @Suppress("UNCHECKED_CAST")
        when (instance) {
            is SimpleLayer -> SimpleLayerSerializer as SerializationStrategy<Layer>
            is ConvolutionalLayer -> ConvolutionLayerSerializer as SerializationStrategy<Layer>
            is ConvolutionalLayers -> ConvolutionLayersSerializer as SerializationStrategy<Layer>
            else -> null
        }
    }

    polymorphicDefaultDeserializer(Layer::class) {
        name: String? ->
        when (name) {
            "ConvolutionLayers" -> ConvolutionLayersSerializer
            "ConvolutionLayer" -> ConvolutionLayerSerializer
            "SimpleLayer" -> SimpleLayerSerializer
            else -> null
        }
    }
}

object Dimensionserializer: KSerializer<Dimension> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Dimension") {
        element<Int>("line")
        element<Int>("col")
    }

    override fun deserialize(decoder: Decoder): Dimension {
        return decoder.decodeStructure(descriptor) {
            var line = -1
            var col = -1

            var index = decodeElementIndex(MatrixSerializer.descriptor)
            while (index != CompositeDecoder.DECODE_DONE) {
                when (index) {
                    0 -> line = decodeIntElement(MatrixSerializer.descriptor, 0)
                    1 -> col = decodeIntElement(MatrixSerializer.descriptor, 1)
                    else -> error("Unexpected index: $index")
                }
                index = decodeElementIndex(MatrixSerializer.descriptor)
            }
            Dimension(line, col)
        }
    }

    override fun serialize(encoder: Encoder, dimension: Dimension) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, dimension.line)
            encodeIntElement(descriptor, 1, dimension.col)
        }
    }
}

object MatrixSerializer : KSerializer<Matrix> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Matrix") {
        element<Int>("line")
        element<Int>("col")
        element<ByteArray>("content")
    }

    override fun serialize(encoder: Encoder, matrix: Matrix) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, matrix.line)
            encodeIntElement(descriptor, 1, matrix.col)
            encodeSerializableElement(descriptor, 2, ByteArraySerializer(), matrix.values)
        }
    }

    override fun deserialize(decoder: Decoder): Matrix {
        return decoder.decodeStructure(descriptor) {
            var line = -1
            var col = -1
            var values = ByteArray(0)
            var index = decodeElementIndex(descriptor)
            while (index != CompositeDecoder.DECODE_DONE) {
                when (index) {
                    0 -> line = decodeIntElement(descriptor, 0)
                    1 -> col = decodeIntElement(descriptor, 1)
                    2 -> values = decodeSerializableElement(descriptor, 2, ByteArraySerializer())
                    else -> error("Unexpected index: $index")
                }
                index = decodeElementIndex(descriptor)
            }
            Matrix(line, col, values)
        }
    }
}

object SimpleLayerSerializer : KSerializer<SimpleLayer> {
    override val descriptor = buildClassSerialDescriptor("SimpleLayer") {
        element("axons", MatrixSerializer.descriptor)
    }

    override fun deserialize(decoder: Decoder): SimpleLayer {
        return decoder.decodeStructure(descriptor) {
            var matrix : Matrix? = null
            var index = decodeElementIndex(descriptor)
            while (index != CompositeDecoder.DECODE_DONE) {
                when (index) {
                    0 -> matrix = decodeSerializableElement(descriptor, 0, MatrixSerializer)
                    else -> error("Unexpected index: $index")
                }
                index = decodeElementIndex(descriptor)
            }
            val resultMatrix = matrix
            if (resultMatrix == null) {
                error("No matrix")
            }
            SimpleLayer(resultMatrix)
        }
    }

    override fun serialize(encoder: Encoder, layer: SimpleLayer) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, MatrixSerializer, layer.inputAxons)
        }
    }
}

object ConvolutionLayerSerializer : KSerializer<ConvolutionalLayer> {
    override val descriptor = buildClassSerialDescriptor("ConvolutionLayer") {
        element("axons", MatrixSerializer.descriptor)
        element("dimensions", Dimensionserializer.descriptor)
    }

    override fun deserialize(decoder: Decoder): ConvolutionalLayer {
        return decoder.decodeStructure(descriptor) {
            var matrix : Matrix? = null
            var dim : Dimension? = null
            var index = decodeElementIndex(descriptor)
            while (index != CompositeDecoder.DECODE_DONE) {
                when (index) {
                    0 -> matrix = decodeSerializableElement(descriptor, 0, MatrixSerializer)
                    1 -> dim = decodeSerializableElement(descriptor, 1, Dimensionserializer)
                    else -> error("Unexpected index: $index")
                }
                index = decodeElementIndex(descriptor)
            }
            val resultMatrix = matrix
            if (resultMatrix == null) {
                error("No matrix")
            }
            ConvolutionalLayer(resultMatrix, dim!!)
        }
    }

    override fun serialize(encoder: Encoder, layer: ConvolutionalLayer) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, MatrixSerializer, layer.inputAxons)
            encodeSerializableElement(descriptor, 1, Dimensionserializer, layer.inputDimension)
        }
    }
}


object ConvolutionLayersSerializer : KSerializer<ConvolutionalLayers> {
    override val descriptor = buildClassSerialDescriptor("ConvolutionLayers") {
        element("layers", ListSerializer(ConvolutionLayerSerializer).descriptor)
    }

    override fun deserialize(decoder: Decoder): ConvolutionalLayers {
        return decoder.decodeStructure(descriptor) {

            val layers = mutableListOf<ConvolutionalLayer>()
            var index = decodeElementIndex(descriptor)
            while (index != CompositeDecoder.DECODE_DONE) {
                when (index) {
                    0 -> {
                        val layer: List<ConvolutionalLayer> = this.decodeSerializableElement(descriptor, 0, ListSerializer(ConvolutionLayerSerializer))
                        layers.addAll(layer)
                    }
                    else -> error("Unexpected index: $index")
                }
                index = decodeElementIndex(descriptor)
            }

            ConvolutionalLayers(layers)
        }
    }

    override fun serialize(encoder: Encoder, layers: ConvolutionalLayers) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, ListSerializer(ConvolutionLayerSerializer), layers.layers)
        }
    }
}

object BrainSerializer : KSerializer<Brain> {

    val layersSerializer = module.serializer(typeOf<Layer>()) as KSerializer<Layer>

    override val descriptor = buildClassSerialDescriptor("ConvolutionLayers") {
        element("layers", ListSerializer(layersSerializer).descriptor)
    }

   override fun deserialize(decoder: Decoder): Brain {
        return decoder.decodeStructure(descriptor) {

            val layers = mutableListOf<Layer>()
            var index = decodeElementIndex(descriptor)
            while (index != CompositeDecoder.DECODE_DONE) {
                when (index) {
                    0 -> {
                        val layer: List<Layer> = this.decodeSerializableElement(descriptor, 0, ListSerializer(layersSerializer))
                        layers.addAll(layer)
                    }
                    else -> error("Unexpected index: $index")
                }
                index = decodeElementIndex(descriptor)
            }

            Brain(layers)
        }
    }

    override fun serialize(encoder: Encoder, brain: Brain) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, ListSerializer(layersSerializer), brain.layers)
        }
    }
}

