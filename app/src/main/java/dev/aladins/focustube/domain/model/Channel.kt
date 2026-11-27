package dev.aladins.focustube.domain.model

import kotlin.time.Instant

/** A YouTube channel as the app shows it. */
data class Channel(
    val id: String,
    val title: String,
    /** "@fingerstylefred", if the channel has one. */
    val handle: String?,
    val avatarUrl: String?,
    /** Null when the channel hides it. */
    val subscriberCount: Long?,
    val videoCount: Long?,
    /** When uploads were last checked; null if never. */
    val lastCheckedAt: Instant?,
    /** Newest saved upload ("Latest upload 2 h ago"); null if none saved yet. */
    val latestUploadAt: Instant? = null,
    /** "More uploads" can load another page. */
    val hasMoreUploads: Boolean = false
)

/** What the Add channel field holds: a link or @handle (1 unit to look up) or words to search (100). */
sealed interface ChannelQuery {
    data class Handle(val handle: String) : ChannelQuery

    data class Id(val channelId: String) : ChannelQuery

    /** Old /user/ links. */
    data class Username(val username: String) : ChannelQuery

    data class Search(val text: String) : ChannelQuery

    companion object {
        private val handleRegex = Regex("""^@([A-Za-z0-9._-]{3,30})$""")
        private val idRegex = Regex("""^(UC[A-Za-z0-9_-]{22})$""")
        private val urlRegex =
            Regex(
                """^(?:https?://)?(?:www\.|m\.)?youtube\.com/(@[A-Za-z0-9._-]{3,30}|channel/UC[A-Za-z0-9_-]{22}|user/[A-Za-z0-9]+)(?:[/?#].*)?$"""
            )

        /** Reads the field: "@fred", "youtube.com/@fred", ".../channel/UC…", ".../user/fred" or plain words. */
        fun parse(text: String): ChannelQuery? {
            val t = text.trim()
            if (t.isEmpty()) return null
            handleRegex.matchEntire(t)?.let { return Handle(it.groupValues[1]) }
            idRegex.matchEntire(t)?.let { return Id(it.groupValues[1]) }
            urlRegex.matchEntire(t)?.let { m ->
                val path = m.groupValues[1]
                return when {
                    path.startsWith("@") -> Handle(path.drop(1))
                    path.startsWith("channel/") -> Id(path.removePrefix("channel/"))
                    else -> Username(path.removePrefix("user/"))
                }
            }
            // Other links (e.g. /c/ custom URLs) can't be looked up by the API: search their last part.
            if (t.contains("youtube.com/")) return Search(t.substringAfterLast('/').substringBefore('?'))
            return Search(t)
        }
    }
}
