package dev.aladins.focustube.data.local.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import dev.aladins.focustube.data.local.dao.PersonaDao
import dev.aladins.focustube.data.local.dao.QuotaDao
import dev.aladins.focustube.data.local.dao.SearchDao
import dev.aladins.focustube.data.local.dao.VideoDao
import dev.aladins.focustube.data.local.entity.PersonaEntity
import dev.aladins.focustube.data.local.entity.QuotaLogEntity
import dev.aladins.focustube.data.local.entity.SearchCacheEntity
import dev.aladins.focustube.data.local.entity.SearchResultEntity
import dev.aladins.focustube.data.local.entity.VideoEntity

/**
 * The app's Room database.
 *
 * Schema changes: bump [version] and add an AutoMigration (or a written Migration for data
 * reshaping) from the previous version. Never use destructive fallback: an update must never
 * wipe personas, Watch later or History. Each version's schema JSON is committed in app/schemas.
 */
@Database(
    entities = [
        PersonaEntity::class,
        VideoEntity::class,
        SearchCacheEntity::class,
        SearchResultEntity::class,
        QuotaLogEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        // v2 (Milestone 3): video, search_cache, search_result, quota_log.
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class FocusTubeDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao

    abstract fun videoDao(): VideoDao

    abstract fun searchDao(): SearchDao

    abstract fun quotaDao(): QuotaDao

    companion object {
        const val NAME = "focustube.db"
    }
}
