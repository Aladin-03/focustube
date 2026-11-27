package dev.aladins.focustube.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.aladins.focustube.data.local.dao.PersonaDao
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
}
