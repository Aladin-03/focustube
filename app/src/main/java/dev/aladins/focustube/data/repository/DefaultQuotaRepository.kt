package dev.aladins.focustube.data.repository

import dev.aladins.focustube.data.remote.quota.QuotaTracker
import dev.aladins.focustube.domain.model.QuotaStatus
import dev.aladins.focustube.domain.repository.QuotaRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/** [QuotaRepository] backed by [QuotaTracker]. */
@Singleton
class DefaultQuotaRepository @Inject constructor(private val tracker: QuotaTracker) : QuotaRepository {
    override fun observeToday(): Flow<QuotaStatus> = tracker.observeToday()
}
