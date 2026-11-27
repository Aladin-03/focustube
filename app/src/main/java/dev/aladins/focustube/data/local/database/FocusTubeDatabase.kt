package dev.aladins.focustube.data.local.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import dev.aladins.focustube.data.local.dao.ChannelDao
import dev.aladins.focustube.data.local.dao.HistoryDao
import dev.aladins.focustube.data.local.dao.PersonaDao
import dev.aladins.focustube.data.local.dao.QuotaDao
import dev.aladins.focustube.data.local.dao.SearchDao
import dev.aladins.focustube.data.local.dao.VideoDao
import dev.aladins.focustube.data.local.dao.WatchLaterDao
import dev.aladins.focustube.data.local.entity.ChannelEntity
import dev.aladins.focustube.data.local.entity.ChannelSearchEntity
import dev.aladins.focustube.data.local.entity.ChannelUploadEntity
import dev.aladins.focustube.data.local.entity.HistoryEntity
import dev.aladins.focustube.data.local.entity.PersonaChannelEntity
import dev.aladins.focustube.data.local.entity.PersonaEntity
import dev.aladins.focustube.data.local.entity.QuotaLogEntity
import dev.aladins.focustube.data.local.entity.SearchCacheEntity
import dev.aladins.focustube.data.local.entity.SearchResultEntity
import dev.aladins.focustube.data.local.entity.VideoEntity
import dev.aladins.focustube.data.local.entity.WatchLaterEntity

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
        QuotaLogEntity::class,
        WatchLaterEntity::class,
        HistoryEntity::class,
        ChannelEntity::class,
        PersonaChannelEntity::class,
        ChannelUploadEntity::class,
        ChannelSearchEntity::class
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        // v2 (Milestone 3): video, search_cache, search_result, quota_log.
        AutoMigration(from = 1, to = 2),
        // v3 (Milestone 4): watch_later, history.
        AutoMigration(from = 2, to = 3),
        // v4 (Milestone 5): channel, persona_channel, channel_upload, channel_search.
        AutoMigration(from = 3, to = 4)
    ]
)
abstract class FocusTubeDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao

    abstract fun videoDao(): VideoDao

    abstract fun searchDao(): SearchDao

    abstract fun quotaDao(): QuotaDao

    abstract fun watchLaterDao(): WatchLaterDao

    abstract fun historyDao(): HistoryDao

    abstract fun channelDao(): ChannelDao

    companion object {
        const val NAME = "focustube.db"
    }
}
