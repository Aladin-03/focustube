package dev.aladins.focustube.data.mapper

import dev.aladins.focustube.data.local.entity.PersonaEntity
import dev.aladins.focustube.domain.model.Persona
import dev.aladins.focustube.domain.model.Region
import kotlin.time.Instant

private const val REGION_SEPARATOR = ","

/** Converts a stored persona row to the domain model. */
fun PersonaEntity.toDomain(): Persona = Persona(
    id = id,
    name = name,
    goal = goal,
    regions = regions.toRegions(),
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    archivedAt = archivedAt?.let(Instant::fromEpochMilliseconds)
)

/** Converts a domain persona to a row (for inserts and tests). */
fun Persona.toEntity(): PersonaEntity = PersonaEntity(
    id = id,
    name = name,
    goal = goal,
    regions = regions.toStoredRegions(),
    createdAt = createdAt.toEpochMilliseconds(),
    archivedAt = archivedAt?.toEpochMilliseconds()
)

/** "US,GB,DE" -> regions, keeping order. */
fun String.toRegions(): List<Region> = split(REGION_SEPARATOR).filter { it.isNotBlank() }.map { Region(it.trim()) }

/** Regions -> "US,GB,DE". */
fun List<Region>.toStoredRegions(): String = joinToString(REGION_SEPARATOR) { it.code }
