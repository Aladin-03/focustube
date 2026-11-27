package dev.aladins.focustube.data.repository

import dev.aladins.focustube.core.common.result.AppError
import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.data.local.dao.ChannelDao
import dev.aladins.focustube.data.local.dao.VideoDao
import dev.aladins.focustube.data.local.database.TransactionRunner
import dev.aladins.focustube.data.local.entity.ChannelEntity
import dev.aladins.focustube.data.local.entity.ChannelSearchEntity
import dev.aladins.focustube.data.local.entity.ChannelUploadEntity
import dev.aladins.focustube.data.local.entity.PersonaChannelEntity
import dev.aladins.focustube.data.mapper.toDomain
import dev.aladins.focustube.data.mapper.toEntity
import dev.aladins.focustube.data.remote.youtube.YouTubeDataSource
import dev.aladins.focustube.data.remote.youtube.dto.ChannelDto
import dev.aladins.focustube.data.remote.youtube.dto.PlaylistItem
import dev.aladins.focustube.domain.model.Channel
import dev.aladins.focustube.domain.model.ChannelQuery
import dev.aladins.focustube.domain.model.SearchKey
import dev.aladins.focustube.domain.model.Video
import dev.aladins.focustube.domain.repository.ChannelRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [ChannelRepository] on Room and the YouTube API. Uploads come from each channel's uploads
 * playlist (1 unit per page), never from search. A check reads only page 1 and keeps the uploads it
 * doesn't know yet; details for new videos are fetched in batches of 50 across channels.
 */
@Singleton
class DefaultChannelRepository @Inject constructor(
    private val dao: ChannelDao,
    private val videoDao: VideoDao,
    private val youTube: YouTubeDataSource,
    private val transaction: TransactionRunner,
    private val clock: Clock
) : ChannelRepository {
    override suspend fun find(query: ChannelQuery): AppResult<List<Channel>> = when (query) {
        is ChannelQuery.Handle -> lookup(youTube.channelBy(handle = query.handle))
        is ChannelQuery.Username -> lookup(youTube.channelBy(username = query.username))
        is ChannelQuery.Id -> lookup(youTube.channels(listOf(query.channelId)))
        is ChannelQuery.Search -> search(query.text)
    }

    private suspend fun lookup(result: AppResult<List<ChannelDto>>): AppResult<List<Channel>> = when (result) {
        is AppResult.Failure -> result
        is AppResult.Success -> AppResult.Success(saveDetails(result.value).map { it.toDomain() })
    }

    /** Name search: saved for 30 days, so the same words again cost nothing. */
    private suspend fun search(text: String): AppResult<List<Channel>> {
        val key = SearchKey.normalizeQuery(text)
        val now = clock.now()
        val saved = dao.getSearch(key)?.takeIf { now - Instant.fromEpochMilliseconds(it.createdAt) < MAX_AGE }
        val ids =
            if (saved != null) {
                saved.channelIds.split(",").filter { it.isNotBlank() }
            } else {
                when (val found = youTube.searchChannels(text.trim())) {
                    is AppResult.Failure -> return found
                    is AppResult.Success -> found.value
                }
            }
        val known = dao.getAll(ids).associateBy { it.youtubeId }
        val missing = ids.filter { it !in known }
        val fetched =
            if (missing.isEmpty()) {
                emptyList()
            } else {
                when (val details = youTube.channels(missing)) {
                    is AppResult.Failure -> return details
                    is AppResult.Success -> saveDetails(details.value)
                }
            }
        val all = known + fetched.associateBy { it.youtubeId }
        val ordered = ids.mapNotNull { all[it] }
        if (saved ==
            null
        ) {
            dao.saveSearch(
                ChannelSearchEntity(
                    key,
                    ordered.joinToString(",") {
                        it.youtubeId
                    },
                    now.toEpochMilliseconds()
                )
            )
        }
        return AppResult.Success(ordered.map { it.toDomain() })
    }

    private suspend fun saveDetails(dtos: List<ChannelDto>): List<ChannelEntity> {
        val rows = dtos.mapNotNull { it.toEntity(clock.now()) }
        dao.upsertDetails(rows)
        return rows
    }

    override fun observe(channelId: String): Flow<Channel?> = dao.observe(channelId).map { it?.toDomain() }

    override fun observeFollowed(personaId: Long): Flow<List<Channel>> = dao.observeFollowed(personaId).map { rows ->
        rows.map { it.toDomain() }
    }

    override fun observeFollowers(channelId: String): Flow<Set<Long>> = dao.observeFollowers(channelId).map {
        it.toSet()
    }

    override fun observeAllFollowers(): Flow<Map<String, Set<Long>>> = dao.observeAllFollows().map { rows ->
        rows.groupBy({ it.channelId }, { it.personaId }).mapValues { it.value.toSet() }
    }

    override fun observeFeed(personaId: Long, since: Instant): Flow<List<Video>> =
        dao.observeFeed(personaId, since.toEpochMilliseconds()).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeUploads(channelId: String): Flow<List<Video>> = dao.observeUploads(channelId).map { rows ->
        rows.map { it.toDomain() }
    }

    override fun observeLastChecked(personaId: Long): Flow<Instant?> = dao.observeLastChecked(personaId).map {
        it?.let(Instant::fromEpochMilliseconds)
    }

    override suspend fun setFollowers(channelId: String, personaIds: Set<Long>): AppResult<Unit> {
        val before = dao.followers(channelId).toSet()
        val now = clock.now().toEpochMilliseconds()
        transaction {
            (personaIds - before).forEach { dao.follow(PersonaChannelEntity(it, channelId, now)) }
            (before - personaIds).forEach { dao.unfollow(it, channelId) }
        }
        // First follow: fetch the first page of uploads so the feed isn't empty.
        return if (personaIds.isNotEmpty() &&
            dao.uploadIds(channelId).isEmpty()
        ) {
            firstUploads(channelId)
        } else {
            AppResult.Success(Unit)
        }
    }

    override suspend fun unfollow(personaId: Long, channelId: String) = dao.unfollow(personaId, channelId)

    override suspend fun restoreFollow(personaId: Long, channelId: String, addedAt: Instant) =
        dao.upsertFollow(PersonaChannelEntity(personaId, channelId, addedAt.toEpochMilliseconds()))

    override suspend fun followedSince(personaId: Long, channelId: String): Instant? =
        dao.getFollow(personaId, channelId)?.addedAt?.let(Instant::fromEpochMilliseconds)

    override suspend fun ensureChannel(channelId: String): AppResult<Unit> {
        if (dao.get(channelId) == null) {
            when (val details = youTube.channels(listOf(channelId))) {
                is AppResult.Failure -> return details

                is AppResult.Success -> if (saveDetails(
                        details.value
                    ).isEmpty()
                ) {
                    return AppResult.Failure(AppError.NotFound)
                }
            }
        }
        return if (dao.uploadIds(channelId).isEmpty()) firstUploads(channelId) else AppResult.Success(Unit)
    }

    private suspend fun firstUploads(channelId: String): AppResult<Unit> {
        val channel = dao.get(channelId) ?: return AppResult.Failure(AppError.NotFound)
        return when (val page = youTube.uploads(channel.uploadsPlaylistId, null)) {
            is AppResult.Failure -> page

            is AppResult.Success -> {
                val saved = saveUploads(mapOf(channelId to page.value.items))
                if (saved is AppResult.Failure) return saved
                dao.setNextToken(channelId, page.value.nextPageToken)
                dao.setLastChecked(listOf(channelId), clock.now().toEpochMilliseconds())
                AppResult.Success(Unit)
            }
        }
    }

    override suspend fun loadMoreUploads(channelId: String): AppResult<Unit> {
        val channel = dao.get(channelId) ?: return AppResult.Failure(AppError.NotFound)
        val token = channel.uploadsNextToken ?: return AppResult.Success(Unit)
        return when (val page = youTube.uploads(channel.uploadsPlaylistId, token)) {
            is AppResult.Failure -> page

            is AppResult.Success -> {
                val saved = saveUploads(mapOf(channelId to page.value.items))
                if (saved is AppResult.Failure) return saved
                dao.setNextToken(channelId, page.value.nextPageToken)
                AppResult.Success(Unit)
            }
        }
    }

    override suspend fun followedIds(personaId: Long?): List<String> = if (personaId ==
        null
    ) {
        dao.followedIdsOfActivePersonas()
    } else {
        dao.followedIds(personaId)
    }

    override suspend fun checkedBefore(channelIds: List<String>, cutoff: Instant): List<String> =
        dao.getAll(channelIds).filter {
            (it.lastCheckedAt ?: 0) < cutoff.toEpochMilliseconds()
        }.map { it.youtubeId }

    override suspend fun checkUploads(channelIds: List<String>): AppResult<Int> {
        val pages = mutableMapOf<String, List<PlaylistItem>>()
        for (channel in dao.getAll(channelIds)) {
            when (val page = youTube.uploads(channel.uploadsPlaylistId, null)) {
                is AppResult.Failure -> return page

                is AppResult.Success -> {
                    pages[channel.youtubeId] = page.value.items
                    if (channel.uploadsNextToken == null && dao.uploadIds(channel.youtubeId).isEmpty()) {
                        dao.setNextToken(channel.youtubeId, page.value.nextPageToken)
                    }
                }
            }
        }
        return when (val saved = saveUploads(pages)) {
            is AppResult.Failure -> saved

            is AppResult.Success -> {
                dao.setLastChecked(pages.keys.toList(), clock.now().toEpochMilliseconds())
                saved
            }
        }
    }

    /**
     * Saves uploads the channels don't have yet, fetching details of videos not saved at all in
     * batches of 50 (across channels). Returns how many uploads were new.
     */
    private suspend fun saveUploads(pages: Map<String, List<PlaylistItem>>): AppResult<Int> {
        val fresh = mutableListOf<ChannelUploadEntity>()
        for ((channelId, items) in pages) {
            val known = dao.uploadIds(channelId).toSet()
            items.map { it.contentDetails }.filter { it.videoId !in known }.forEach { item ->
                val published = item.videoPublishedAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
                fresh +=
                    ChannelUploadEntity(
                        channelId,
                        item.videoId,
                        published?.toEpochMilliseconds() ?: clock.now().toEpochMilliseconds()
                    )
            }
        }
        if (fresh.isEmpty()) return AppResult.Success(0)
        val needDetails = fresh.map { it.videoId }.distinct().filter { videoDao.get(it) == null }
        if (needDetails.isNotEmpty()) {
            when (val details = youTube.videos(needDetails)) {
                is AppResult.Failure -> return details
                is AppResult.Success -> videoDao.upsertAll(details.value.map { it.toEntity(clock.now()) })
            }
        }
        // Keep only uploads whose details exist (private or removed videos have none).
        val withDetails = fresh.filter { videoDao.get(it.videoId) != null }
        dao.insertUploads(withDetails)
        return AppResult.Success(withDetails.size)
    }

    override suspend fun removeStale() {
        val cutoff = (clock.now() - MAX_AGE).toEpochMilliseconds()
        transaction {
            dao.deleteUnfollowedOlderThan(cutoff)
            dao.deleteSearchesOlderThan(cutoff)
        }
    }

    private companion object {
        val MAX_AGE = 30.days
    }
}
