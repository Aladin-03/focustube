package dev.aladins.focustube.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Units spent per Pacific day. The blocking methods are called from OkHttp's background threads
 * (never the main thread).
 */
@Dao
interface QuotaDao {
    @Query("SELECT COALESCE((SELECT units_used FROM quota_log WHERE pacific_date = :date), 0)")
    fun observeUnits(date: String): Flow<Long>

    @Query("SELECT COALESCE((SELECT units_used FROM quota_log WHERE pacific_date = :date), 0)")
    fun unitsBlocking(date: String): Long

    @Query("INSERT OR IGNORE INTO quota_log (pacific_date, units_used) VALUES (:date, 0)")
    fun ensureRowBlocking(date: String)

    @Query("UPDATE quota_log SET units_used = units_used + :units WHERE pacific_date = :date")
    fun incrementBlocking(date: String, units: Long)

    @Query("UPDATE quota_log SET units_used = MAX(units_used, :units) WHERE pacific_date = :date")
    fun raiseToAtLeastBlocking(date: String, units: Long)

    @Query("UPDATE quota_log SET units_used = :units WHERE pacific_date = :date")
    fun setBlocking(date: String, units: Long)

    /** Adds [units] to [date] (works on SQLite versions without UPSERT, for API 26). */
    @Transaction
    fun addBlocking(date: String, units: Long) {
        ensureRowBlocking(date)
        incrementBlocking(date, units)
    }
}
