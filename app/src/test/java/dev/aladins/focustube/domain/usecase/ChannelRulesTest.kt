package dev.aladins.focustube.domain.usecase

import dev.aladins.focustube.core.common.result.AppResult
import dev.aladins.focustube.domain.model.ChannelQuery
import dev.aladins.focustube.testing.ChannelTestRig
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Reading the Add channel field, and which channels a check covers. */
class ChannelRulesTest {
    @Test
    fun linksAndHandlesAreLookups_wordsAreSearches() {
        assertEquals(ChannelQuery.Handle("fingerstylefred"), ChannelQuery.parse(" @fingerstylefred "))
        assertEquals(ChannelQuery.Handle("fred"), ChannelQuery.parse("https://www.youtube.com/@fred/videos"))
        assertEquals(ChannelQuery.Handle("fred"), ChannelQuery.parse("youtube.com/@fred"))
        assertEquals(
            ChannelQuery.Id("UC1234567890123456789012"),
            ChannelQuery.parse("https://m.youtube.com/channel/UC1234567890123456789012")
        )
        assertEquals(ChannelQuery.Username("fredguitar"), ChannelQuery.parse("https://youtube.com/user/fredguitar"))
        assertEquals(ChannelQuery.Search("fredsguitar"), ChannelQuery.parse("https://youtube.com/c/fredsguitar"))
        assertEquals(ChannelQuery.Search("fingerstyle fred"), ChannelQuery.parse("fingerstyle fred"))
        assertNull(ChannelQuery.parse("   "))
    }

    private var now = Instant.parse("2026-11-27T12:00:00Z")
    private val clock = object : Clock {
        override fun now() = this@ChannelRulesTest.now
    }

    @Test
    fun dailyCheckSkipsRecentlyCheckedChannelsAndArchivedPersonas_checkNowDoesNot() = runTest {
        val archived = mutableSetOf<Long>()
        val rig = ChannelTestRig(clock = clock, activePersonas = { setOf(1L, 2L) - archived })
        val check = CheckUploadsUseCase(rig.repository, clock)
        rig.repository.find(ChannelQuery.Handle("fred"))
        rig.repository.find(ChannelQuery.Handle("ana"))
        rig.repository.setFollowers("UC_fred", setOf(1L))
        rig.repository.setFollowers("UC_ana", setOf(2L))
        val afterFollow = rig.api.playlistCalls

        now += 2.hours
        check(personaId = null, force = false)
        assertEquals(afterFollow, rig.api.playlistCalls) // both checked 2 h ago when followed

        now += 20.hours
        archived += 2L
        check(personaId = null, force = false)
        assertEquals(afterFollow + 1, rig.api.playlistCalls) // only Fred: Ana's only persona is archived

        assertEquals(AppResult.Success(0), check(personaId = 1L, force = true))
        assertEquals(afterFollow + 2, rig.api.playlistCalls) // Check now ignores the 20 h gap
    }
}
