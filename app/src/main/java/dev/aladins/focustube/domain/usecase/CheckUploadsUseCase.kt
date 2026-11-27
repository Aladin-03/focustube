package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.repository.ChannelRepository
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Checks followed channels for new uploads. "Check now" checks the current persona's channels
 * ([force]); the daily job checks channels of active personas not checked in the last 20 hours.
 */
class CheckUploadsUseCase @Inject constructor(private val channels: ChannelRepository, private val clock: Clock) {
    suspend operator fun invoke(personaId: Long?, force: Boolean): AppResult<Int> {
        val ids = channels.followedIds(personaId)
        val due = if (force) ids else channels.checkedBefore(ids, clock.now() - MIN_GAP)
        if (due.isEmpty()) return AppResult.Success(0)
        return channels.checkUploads(due)
    }

    companion object {
        /** The daily job skips channels checked more recently than this. */
        val MIN_GAP = 20.hours
    }
}
