package dev.aladins.focustube.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.aladins.focustube.data.repository.DefaultPersonaRepository
import dev.aladins.focustube.domain.repository.PersonaRepository

/** Binds repository interfaces (domain) to their implementations (data). */
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun bindPersonaRepository(impl: DefaultPersonaRepository): PersonaRepository
}
