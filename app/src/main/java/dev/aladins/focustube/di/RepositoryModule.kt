package dev.aladins.focustube.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.aladins.focustube.data.local.database.RoomTransactionRunner
import dev.aladins.focustube.data.local.database.TransactionRunner
import dev.aladins.focustube.data.repository.DefaultHistoryRepository
import dev.aladins.focustube.data.repository.DefaultPersonaRepository
import dev.aladins.focustube.data.repository.DefaultQuotaRepository
import dev.aladins.focustube.data.repository.DefaultSearchRepository
import dev.aladins.focustube.data.repository.DefaultSettingsRepository
import dev.aladins.focustube.data.repository.DefaultVideoRepository
import dev.aladins.focustube.data.repository.DefaultWatchLaterRepository
import dev.aladins.focustube.domain.repository.HistoryRepository
import dev.aladins.focustube.domain.repository.PersonaRepository
import dev.aladins.focustube.domain.repository.QuotaRepository
import dev.aladins.focustube.domain.repository.SearchRepository
import dev.aladins.focustube.domain.repository.SettingsRepository
import dev.aladins.focustube.domain.repository.VideoRepository
import dev.aladins.focustube.domain.repository.WatchLaterRepository

/** Binds repository interfaces (domain) to their implementations (data). */
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun bindPersonaRepository(impl: DefaultPersonaRepository): PersonaRepository

    @Binds
    fun bindTransactionRunner(impl: RoomTransactionRunner): TransactionRunner

    @Binds
    fun bindSearchRepository(impl: DefaultSearchRepository): SearchRepository

    @Binds
    fun bindVideoRepository(impl: DefaultVideoRepository): VideoRepository

    @Binds
    fun bindQuotaRepository(impl: DefaultQuotaRepository): QuotaRepository

    @Binds
    fun bindSettingsRepository(impl: DefaultSettingsRepository): SettingsRepository

    @Binds
    fun bindWatchLaterRepository(impl: DefaultWatchLaterRepository): WatchLaterRepository

    @Binds
    fun bindHistoryRepository(impl: DefaultHistoryRepository): HistoryRepository
}
