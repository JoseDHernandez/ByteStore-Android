package com.example.bytestore.core

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extensiones de utilidad para operaciones de base de datos usando Room
 */

/**
 * Ejecuta múltiples operaciones en una transacción de manera segura
 */
suspend fun RoomDatabase.executeInTransaction(vararg operations: suspend () -> Unit) {
    withContext(Dispatchers.IO) {
        withTransaction {
            operations.forEach { operation ->
                operation()
            }
        }
    }
}

/**
 * Ejecuta una operación en un contexto IO
 */
suspend fun <T> RoomDatabase.withIOContext(block: suspend () -> T): T =
    withContext(Dispatchers.IO) {
        block()
    }

/**
 * Ejecuta una operación en una transacción de manera segura
 */
suspend fun <T> RoomDatabase.safeTransaction(
    block: suspend () -> T
): Result<T> = withContext(Dispatchers.IO) {
    try {
        withTransaction {
            Result.success(block())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Versión síncrona de safeTransaction para operaciones que no requieren corrutinas
 */
fun <T> RoomDatabase.safeTransactionSync(
    block: () -> T
): Result<T> = try {
    runInTransaction(block)
    Result.success(runInTransaction(block))
} catch (e: Exception) {
    Result.failure(e)
}

/**
 * Ejecutar una consulta con manejo de errores
 * @param defaultValue valor por defecto en caso de error
 */
suspend fun <T> RoomDatabase.safeQuery(
    defaultValue: T,
    block: suspend () -> T
): T = safeTransaction(block).getOrDefault(defaultValue)