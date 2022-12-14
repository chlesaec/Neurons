import base.Trainer
import base.data.TrainingData
import base.data.TrainingItem
import base.neurons.Brain
import base.neurons.Layer
import base.vectors.Matrix
import base.vectors.RealVector
import base.vectors.VectorImpl
import kotlin.random.Random

val random = Random(23411)

fun generateData(
    nbe: Int,
    intputSize: Int,
    fct: (RealVector) -> Int
): TrainingData {

    val items: List<TrainingItem> = (1..nbe).map {
        val input = RealVector(intputSize) {
            random.nextDouble(0.0, 10.0)
        }
        TrainingItem(input, fct(input))
    }
    return TrainingData(items)
}

fun addData4(t1 : TrainingData,
             fct: (RealVector) -> Int) : TrainingData {
    var x : Double = 0.0
    val items: List<TrainingItem> = (1..1000).map {
        x = 0.0
        val input = RealVector(4) {
            if (it < 3) {
                val res = random.nextDouble(0.0, 10.0)
                x = x + res*res
                res
            }
            else {
                var theorical : Double = 200 - x
                if (theorical < 0) {
                    0.0
                }
                else {
                    Math.sqrt(theorical) + random.nextDouble(-0.001, 0.001)
                }
            }
        }
        TrainingItem(input, fct(input))
    }
    return TrainingData(items + t1.items)
}

fun data1(): TrainingData {
    return generateData(1000, 4) { 0 }
}

fun dataVerif1(): TrainingData {
    return generateData(10, 4) { 0 }
}

fun data2(): TrainingData {
    return generateData(1000, 4) { it[0].toInt() / 5 }
}

fun dataVerif2(): TrainingData {
    return generateData(30, 4) { it[0].toInt() / 5 }
}

fun data3(): TrainingData {
    return generateData(1000, 4) {
        val value: Int
        if (it[0] < 0.5 && it[1] > 0.5) {
            value = 0
        } else if (it[0] >= 0.5 && it[1] <= 0.5) {
            value = 0
        } else {
            value = 1
        }
        value
    }
}

fun dataVerif3(): TrainingData {
    return generateData(30, 4) {
        val value: Int
        if (it[0] < 0.5 && it[1] > 0.5) {
            value = 0
        } else if (it[0] >= 0.5 && it[1] <= 0.5) {
            value = 0
        } else {
            value = 1
        }
        value
    }
}

fun data4(nbe: Int): TrainingData {
    return generateData(nbe, 4) {
        if (it.times(it) < 200.0) {
            0
        } else {
            1
        }
    }
}

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    val m1 = Matrix(5, 4) {
        (-23 + it).toByte()
    }
    val v1 = VectorImpl(5) { it.toByte() }

    val m2 = Matrix(7, 5) {
        (-23 + it).toByte()
    }
    val v2 = VectorImpl(7) { it.toByte() }

    val m3 = Matrix(3, 7) {
        (-23 + it).toByte()
    }
    val v3 = VectorImpl(3) { it.toByte() }

    val m4 = Matrix(2, 3) {
        (-23 + it).toByte()
    }
    val v4 = VectorImpl(2) { it.toByte() }


    val brain = Brain(
        layers = listOf<Layer>(
            Layer(m1, v1),
            Layer(m2, v2),
            Layer(m3, v3),
            Layer(m4, v4)
        )
    )
    val datas: TrainingData = addData4(data4(3000))
     {
        if (it.times(it) < 200.0) {
            0
        } else {
            1
        }
    }
    val trainer = Trainer(brain, datas)

    val bestBrain = trainer.train()
    val verif: TrainingData = data4(30)
    val score = verif.tryScore(bestBrain)
    println("Score : ${score}")

    println(bestBrain)
}
