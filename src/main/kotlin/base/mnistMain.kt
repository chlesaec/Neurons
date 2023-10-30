package base

import base.data.TrainingData
import base.io.BrainSerializer
import base.io.module
import base.mnist.Mnists
import base.mnist.Reader
import base.neurons.*
import base.vectors.Matrix
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import kotlin.random.Random

fun main(args: Array<String>) {
    println("Mnist main")

    val resourceImages = Thread.currentThread().contextClassLoader.getResource("./train-images-idx3-ubyte")
    val resourceLabels = Thread.currentThread().contextClassLoader.getResource("./train-labels-idx1-ubyte")
    val reader = Reader()
    val mnists: Mnists = reader.readFile(resourceImages.path, resourceLabels.path)

    val format = Json {
        prettyPrint = false
        serializersModule = module
    }
    val brainFile: File = loadLastBrain()
    val brain = if (brainFile.exists()) {
        println("File exists")
        Files.newBufferedReader(brainFile.toPath()).use {
            val inputJson = it.readText()
            format.decodeFromString(BrainSerializer, inputJson)
        }
    }
    else {
        randomBrain(mnists.imageSize)
    }

    val mnist = getMnist()
    val data = BatchData(mnist, 300)

    val trainer = Trainer<Byte>(brain, data::getData)

    val bestBrain = trainer.train()

    val score = runBlocking { data.getData().scoreAndRate{ v -> bestBrain.digest(v) } }
    println("Score : ${score.first}")

    saveNewBrain(bestBrain, format)

    val dataForTest = data.getData()
    val testRate = trainer.test(bestBrain, dataForTest)
    println("test : ${testRate}")
}

fun randomBrain(imageSize: Dimension): Brain {
    val layers = buildConvolutionLayers(imageSize, 10)

    val layer0 = buildSimpleLayer(12 * 12 * 10, 400)

    val layer1 = buildSimpleLayer(400, 100)

    val layer3 = buildSimpleLayer(100, 10)

    return Brain(
        layers = listOf(layers, layer0, layer1, /*layer2, */layer3)
    )
}

fun loadLastBrain(): File {
    val brainsFolder = Thread.currentThread().contextClassLoader.getResource("./")
    var brainFile = File(brainsFolder.path, "brain.json")
    var index = 1
    var nextFile = File(brainsFolder.path, "brain${index}.json")
    while (nextFile.exists()) {
        index++
        brainFile = nextFile
        nextFile = File(brainsFolder.path, "brain${index}.json")
    }
    return brainFile
}

fun saveNewBrain(brain: Brain, format: StringFormat) {

    val outBrain = Thread.currentThread().contextClassLoader.getResource("./")
    var index = 1
    var outFile = File(outBrain.path, "brain${index}.json")
    while (outFile.exists()) {
        index++
        outFile = File(outBrain.path, "brain${index}.json")
    }

    Files.newBufferedWriter(outFile.toPath()).use {
        val stringBrain = format.encodeToString(BrainSerializer, brain)
        it.append(stringBrain)
    }
}

class BatchData(val mnist: Mnists, val batchSize: Int) {
    var index = 0

    fun getData(): TrainingData<Byte> {
        if ((this.index + 1) * this.batchSize >= mnist.size) {
            this.index = 0
        }
        val data = this.mnist.toTrainingData(index * this.batchSize, this.batchSize)
        this.index++
        return data
    }
}

fun buildSimpleLayer(inputSize: Int, outputSize: Int) : Layer {
    val random = Random(System.currentTimeMillis())
    val matrix = Matrix(inputSize, outputSize) {
        random.nextInt().toByte()
    }
    return SimpleLayer(matrix)
}

fun buildConvolutionLayers(imageSize: Dimension, nbe: Int) : ConvolutionalLayers {
    val random = Random(System.currentTimeMillis())
    val layers = mutableListOf<ConvolutionalLayer>()

    repeat(nbe) {
        val matrix = Matrix(5, 5) {
            random.nextInt().toByte()
        }
        val convolutionalLayer = ConvolutionalLayer(matrix, imageSize)
        layers.add(convolutionalLayer)
    }
    return ConvolutionalLayers(layers)
}

fun getMnist() : Mnists {
    val resourceImages = Thread.currentThread().contextClassLoader.getResource("./train-images-idx3-ubyte")
    val resourceLabels = Thread.currentThread().contextClassLoader.getResource("./train-labels-idx1-ubyte")
    val reader = Reader()
    return reader.readFile(resourceImages.path, resourceLabels.path)
}


