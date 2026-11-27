package dev.aladins.focustube.domain.repository

import dev.aladins.focustube.domain.model.QuotaStatus
import kotlinx.coroutines.flow.Flow

/** Today's YouTube units used from this phone. Emits again when the Pacific day changes. */
interface QuotaRepository {
    fun observeToday(): Flow<QuotaStatus>
}
