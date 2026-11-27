package dev.aladins.focustube.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.ZoneId
import kotlin.time.Clock

/** Provides the system clock and time zone; tests inject fixed ones instead. */
@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    fun provideClock(): Clock = Clock.System

    /** The phone's current time zone (not cached: it can change while the app runs). */
    @Provides
    fun provideZone(): ZoneId = ZoneId.systemDefault()
}
