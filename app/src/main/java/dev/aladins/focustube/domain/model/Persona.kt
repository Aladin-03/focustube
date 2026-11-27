package dev.aladins.focustube.domain.model

import kotlin.time.Instant

/**
 * A persona: one focused feed (for example "Guitar" or "General") with its own picks, channels,
 * searches, Watch later and History.
 */
data class Persona(
    val id: Long,
    val name: String,
    /** What the persona wants to learn or watch. Null means no goal: no AI picks. */
    val goal: String?,
    /** Countries used for picks and search, in the user's order. */
    val regions: List<Region>,
    val createdAt: Instant,
    /** When it was archived; null while active. Archiving never deletes anything. */
    val archivedAt: Instant? = null
) {
    /** A persona with a goal gets AI picks and opens on the Picks tab. */
    val hasGoal: Boolean get() = !goal.isNullOrBlank()

    val isArchived: Boolean get() = archivedAt != null
}
