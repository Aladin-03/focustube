package dev.aladins.focustube.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.aladins.focustube.data.local.dao.HistoryDao
import dev.aladins.focustube.data.local.dao.PersonaDao
import dev.aladins.focustube.data.local.dao.QuotaDao
import dev.aladins.focustube.data.local.dao.SearchDao
import dev.aladins.focustube.data.local.dao.VideoDao
import dev.aladins.focustube.data.local.dao.WatchLaterDao
import dev.aladins.focustube.data.local.database.FocusTubeDatabase
import javax.inject.Singleton

/** Replaces the real database with a fresh in-memory one for every Hilt test. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FocusTubeDatabase =
        Room.inMemoryDatabaseBuilder(context, FocusTubeDatabase::class.java).build()

    @Provides
    fun providePersonaDao(database: FocusTubeDatabase): PersonaDao = database.personaDao()

    @Provides
    fun provideVideoDao(database: FocusTubeDatabase): VideoDao = database.videoDao()

    @Provides
    fun provideSearchDao(database: FocusTubeDatabase): SearchDao = database.searchDao()

    @Provides
    fun provideQuotaDao(database: FocusTubeDatabase): QuotaDao = database.quotaDao()

    @Provides
    fun provideWatchLaterDao(database: FocusTubeDatabase): WatchLaterDao = database.watchLaterDao()

    @Provides
    fun provideHistoryDao(database: FocusTubeDatabase): HistoryDao = database.historyDao()
}
