package dev.aladins.focustube.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.aladins.focustube.data.local.dao.HistoryDao
import dev.aladins.focustube.data.local.dao.PersonaDao
import dev.aladins.focustube.data.local.dao.QuotaDao
import dev.aladins.focustube.data.local.dao.SearchDao
import dev.aladins.focustube.data.local.dao.VideoDao
import dev.aladins.focustube.data.local.dao.WatchLaterDao
import dev.aladins.focustube.data.local.database.FocusTubeDatabase
import javax.inject.Singleton

/** Provides the Room database and its DAOs. No destructive migration fallback, ever. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FocusTubeDatabase =
        Room.databaseBuilder(context, FocusTubeDatabase::class.java, FocusTubeDatabase.NAME).build()

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
