package dev.aladins.focustube.data.local.database

import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton

/** Runs a block in one database transaction (an interface so repositories test without Room). */
interface TransactionRunner {
    suspend operator fun <T> invoke(block: suspend () -> T): T
}

/** [TransactionRunner] on the Room database. */
@Singleton
class RoomTransactionRunner @Inject constructor(private val database: FocusTubeDatabase) : TransactionRunner {
    override suspend fun <T> invoke(block: suspend () -> T): T = database.withTransaction { block() }
}
