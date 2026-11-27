package dev.aladins.focustube.data.mapper

import dev.aladins.focustube.domain.model.Region
import dev.aladins.focustube.testing.persona
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonaMapperTest {
    @Test
    fun roundTripKeepsEverything() {
        val original = persona(7, "Guitar", goal = "Learn", archivedMinute = 42)
        assertEquals(original, original.toEntity().toDomain())
    }

    @Test
    fun regionsKeepOrder() {
        val regions = listOf("NL", "US", "GB").map(::Region)
        assertEquals("NL,US,GB", regions.toStoredRegions())
        assertEquals(regions, "NL,US,GB".toRegions())
    }

    @Test
    fun emptyRegionsStringGivesEmptyList() {
        assertEquals(emptyList<Region>(), "".toRegions())
    }
}
