package base.io

import base.vectors.Matrix
import kotlinx.serialization.json.Json

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MatrixSerializerTest {


    @Test
    fun serialize() {
        val matrix = Matrix(3, 2) { it.toByte() }

        val format = Json {
            prettyPrint = true
            serializersModule = module
        }
        val jsonMatrix = format.encodeToString(MatrixSerializer, matrix)
println(jsonMatrix)
        val matrix1 = format.decodeFromString(MatrixSerializer, jsonMatrix)
        val equals = matrix.equals(matrix1)
        assertTrue(equals)
        assertEquals(matrix, matrix1)
    }
}
