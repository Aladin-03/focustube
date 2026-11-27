package dev.aladins.focustube.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Row in `quota_log`: YouTube units this phone spent on one Pacific day ("2026-11-27"). */
@Entity(tableName = "quota_log")
data class QuotaLogEntity(
    @PrimaryKey @ColumnInfo(name = "pacific_date") val pacificDate: String,
    @ColumnInfo(name = "units_used") val unitsUsed: Long
)
