package dev.aladins.focustube.data.mapper

import dev.aladins.focustube.data.remote.youtube.dto.VideoPlayer
import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.domain.model.SearchKey
import dev.aladins.focustube.domain.model.SearchMode
import dev.aladins.focustube.domain.model.ShortsFilter
import dev.aladins.focustube.domain.model.isShort
import dev.aladins.focustube.testing.FakeYouTubeApi
import dev.aladins.focustube.testing.video
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoMapperTest {
    @Test
    fun isoDurations() {
        assertEquals(664L, parseIsoDurationSeconds("PT11M4S"))
        assertEquals(45L, parseIsoDurationSeconds("PT45S"))
        assertEquals(93_600L, parseIsoDurationSeconds("P1DT2H"))
        assertEquals(0L, parseIsoDurationSeconds("P0D"))
        assertEquals(0L, parseIsoDurationSeconds(null))
        assertEquals(0L, parseIsoDurationSeconds("garbage"))
    }

    @Test
    fun shortIsThreeMinutesOrLessAndVertical() {
        assertTrue(isShort(45, 563, 1000))
        assertTrue(isShort(180, 563, 1000))
        assertFalse(isShort(181, 563, 1000)) // too long
        assertFalse(isShort(45, 1778, 1000)) // wide
        assertFalse(isShort(45, null, null)) // unknown shape
        assertFalse(isShort(0, 563, 1000)) // live / upcoming
    }

    @Test
    fun dtoToEntityToDomain() {
        val fetchedAt = Instant.parse("2026-11-27T12:00:00Z")
        val short = FakeYouTubeApi.videoDto("a3", isShort = true).toEntity(fetchedAt).toDomain()
        assertTrue(short.isShort)
        assertEquals(45L, short.durationSeconds)
        assertEquals(84_000L, short.viewCount)
        assertEquals("Fingerstyle Fred", short.channelTitle)
        assertEquals(Instant.parse("2026-11-01T10:00:00Z"), short.publishedAt)

        val wide = FakeYouTubeApi.videoDto("a1").copy(player = VideoPlayer(1778, 1000)).toEntity(fetchedAt)
        assertFalse(wide.isShort)
        assertEquals(fetchedAt.toEpochMilliseconds(), wide.fetchedAt)
    }

    @Test
    fun searchKeyNormalisesQueryAndRegionOrder() {
        val a = SearchKey(1, "  Fingerstyle   BASICS ", SearchMode.Classic, listOf("US", "GB").map(::Region))
        val b = SearchKey(1, "fingerstyle basics", SearchMode.Classic, listOf("GB", "US").map(::Region))
        assertEquals(a.normalizedQuery, b.normalizedQuery)
        assertEquals(a.regionsKey, b.regionsKey)
    }

    @Test
    fun shortsFilter() {
        val list = listOf(video("v1"), video("s1", isShort = true))
        assertEquals(2, ShortsFilter.All.apply(list).size)
        assertEquals(listOf("v1"), ShortsFilter.Videos.apply(list).map { it.id })
        assertEquals(listOf("s1"), ShortsFilter.Shorts.apply(list).map { it.id })
    }
}
