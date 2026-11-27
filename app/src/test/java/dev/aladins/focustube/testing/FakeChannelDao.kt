package dev.aladins.focustube.testing

import dev.aladins.focustube.data.local.dao.ChannelDao
import dev.aladins.focustube.data.local.entity.ChannelEntity
import dev.aladins.focustube.data.local.entity.ChannelRow
import dev.aladins.focustube.data.local.entity.ChannelSearchEntity
import dev.aladins.focustube.data.local.entity.ChannelUploadEntity
import dev.aladins.focustube.data.local.entity.PersonaChannelEntity
import dev.aladins.focustube.data.local.entity.VideoEntity
import dev.aladins.focustube.data.remote.youtube.YouTubeDataSource
import dev.aladins.focustube.data.repository.DefaultChannelRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

/** In-memory ChannelDao with the same rules as its SQL. [activePersonas] lists non-archived ids. */
class FakeChannelDao(
    private val videos: FakeVideoDao,
    private val activePersonas: () -> Set<Long> = { setOf(1L, 2L) }
) : ChannelDao {
    val channels = MutableStateFlow<Map<String, ChannelEntity>>(emptyMap())
    val follows = MutableStateFlow<List<PersonaChannelEntity>>(emptyList())
    val uploads = MutableStateFlow<List<ChannelUploadEntity>>(emptyList())
    private val searches = mutableMapOf<String, ChannelSearchEntity>()

    private fun latest(id: String) = uploads.value.filter { it.channelId == id }.maxOfOrNull { it.publishedAt }

    override fun observe(id: String): Flow<ChannelRow?> = combine(channels, uploads) { c, _ ->
        c[id]?.let { ChannelRow(it, latest(id)) }
    }

    override suspend fun get(id: String) = channels.value[id]

    override suspend fun getAll(ids: List<String>) = ids.mapNotNull { channels.value[it] }

    override fun observeFollowed(personaId: Long): Flow<List<ChannelRow>> = combine(channels, follows, uploads) {
            c,
            f,
            _
        ->
        f.filter {
            it.personaId == personaId
        }.mapNotNull { c[it.channelId] }.sortedBy { it.title.lowercase() }.map { ChannelRow(it, latest(it.youtubeId)) }
    }

    override fun observeFollowers(channelId: String): Flow<List<Long>> = follows.map { f ->
        f.filter {
            it.channelId ==
                channelId
        }.map { it.personaId }
    }

    override fun observeAllFollows(): Flow<List<PersonaChannelEntity>> = follows

    override suspend fun getFollow(personaId: Long, channelId: String) =
        follows.value.firstOrNull { it.personaId == personaId && it.channelId == channelId }

    override suspend fun followers(channelId: String) = follows.value.filter {
        it.channelId == channelId
    }.map { it.personaId }

    override fun observeFeed(personaId: Long, since: Long): Flow<List<VideoEntity>> =
        combine(follows, uploads, videos.rows) { f, u, v ->
            val mine = f.filter { it.personaId == personaId }.map { it.channelId }.toSet()
            u.filter {
                it.channelId in mine && it.publishedAt >= since
            }.sortedByDescending { it.publishedAt }.mapNotNull { v[it.videoId] }
        }

    override fun observeUploads(channelId: String): Flow<List<VideoEntity>> = combine(uploads, videos.rows) { u, v ->
        u.filter { it.channelId == channelId }.sortedByDescending { it.publishedAt }.mapNotNull { v[it.videoId] }
    }

    override fun observeLastChecked(personaId: Long): Flow<Long?> = combine(channels, follows) { c, f ->
        f.filter { it.personaId == personaId }.mapNotNull { c[it.channelId]?.lastCheckedAt }.maxOrNull()
    }

    override suspend fun followedIds(personaId: Long) = follows.value.filter {
        it.personaId == personaId
    }.map { it.channelId }

    override suspend fun followedIdsOfActivePersonas() = follows.value.filter {
        it.personaId in activePersonas()
    }.map { it.channelId }.distinct()

    override suspend fun uploadIds(channelId: String) = uploads.value.filter {
        it.channelId == channelId
    }.map { it.videoId }

    override suspend fun insertIgnore(channel: ChannelEntity) = channels.update {
        if (channel.youtubeId in
            it
        ) {
            it
        } else {
            it + (channel.youtubeId to channel)
        }
    }

    override suspend fun updateDetails(
        id: String,
        title: String,
        handle: String?,
        avatarUrl: String?,
        subscriberCount: Long?,
        videoCount: Long?,
        uploads: String,
        fetchedAt: Long
    ) = channels.update { m ->
        m[id]?.let {
            m +
                (
                    id to
                        it.copy(
                            title = title,
                            handle = handle,
                            avatarUrl = avatarUrl,
                            subscriberCount = subscriberCount,
                            videoCount = videoCount,
                            uploadsPlaylistId = uploads,
                            fetchedAt = fetchedAt
                        )
                    )
        }
            ?: m
    }

    override suspend fun setNextToken(id: String, token: String?) =
        channels.update { m -> m[id]?.let { m + (id to it.copy(uploadsNextToken = token)) } ?: m }

    override suspend fun setLastChecked(ids: List<String>, at: Long) =
        channels.update { m -> m.mapValues { (k, v) -> if (k in ids) v.copy(lastCheckedAt = at) else v } }

    override suspend fun insertUploads(uploads: List<ChannelUploadEntity>) = this.uploads.update { list ->
        list + uploads.filter { u -> list.none { it.channelId == u.channelId && it.videoId == u.videoId } }
    }

    override suspend fun follow(follow: PersonaChannelEntity) = follows.update { list ->
        if (list.any {
                it.personaId ==
                    follow.personaId &&
                    it.channelId == follow.channelId
            }
        ) {
            list
        } else {
            list + follow
        }
    }

    override suspend fun upsertFollow(follow: PersonaChannelEntity) = follows.update { list ->
        list.filterNot {
            it.personaId ==
                follow.personaId &&
                it.channelId == follow.channelId
        } +
            follow
    }

    override suspend fun unfollow(personaId: Long, channelId: String) =
        follows.update { list -> list.filterNot { it.personaId == personaId && it.channelId == channelId } }

    override suspend fun getSearch(query: String) = searches[query]

    override suspend fun saveSearch(search: ChannelSearchEntity) {
        searches[search.queryNormalized] = search
    }

    override suspend fun deleteUnfollowedOlderThan(cutoff: Long): Int {
        val followed = follows.value.map { it.channelId }.toSet()
        val drop = channels.value.values.filter {
            it.fetchedAt < cutoff && it.youtubeId !in followed
        }.map { it.youtubeId }.toSet()
        channels.update { it - drop }
        uploads.update { list -> list.filterNot { it.channelId in drop } }
        return drop.size
    }

    override suspend fun deleteSearchesOlderThan(cutoff: Long): Int {
        val old = searches.values.filter { it.createdAt < cutoff }.map { it.queryNormalized }
        old.forEach { searches.remove(it) }
        return old.size
    }
}

/** The real channel repository on in-memory DAOs and [api]. */
class ChannelTestRig(
    val api: FakeYouTubeApi = FakeYouTubeApi(),
    clock: Clock = Clock.System,
    activePersonas: () -> Set<Long> = { setOf(1L, 2L) }
) {
    val videoDao = FakeVideoDao()
    val dao = FakeChannelDao(videoDao, activePersonas)
    val repository =
        DefaultChannelRepository(
            dao = dao,
            videoDao = videoDao,
            youTube = YouTubeDataSource(api, Json { ignoreUnknownKeys = true }),
            transaction = DirectTransactionRunner,
            clock = clock
        )
}

/** A ChannelRepository for tests that don't care about channels. */
fun fakeChannelRepository() = ChannelTestRig().repository
