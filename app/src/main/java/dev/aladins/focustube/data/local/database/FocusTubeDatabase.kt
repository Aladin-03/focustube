package dev.aladins.focustube.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.aladins.focustube.data.local.dao.PersonaDao
import dev.aladins.focustube.data.local.entity.PersonaEntity

/**
 * The app's Room database.
 *
 * Schema changes: bump [version] and add an AutoMigration (or a written Migration for data
 * reshaping) from the previous version. Never use destructive fallback: an update must never
 * wipe personas, Watch later or History. Each version's schema JSON is committed in app/schemas.
 */
@Database(
    entities = [PersonaEntity::class],
    version = 1,
    exportSchema = true
)
abstract class FocusTubeDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao

    companion object {
        const val NAME = "focustube.db"
    }
}
