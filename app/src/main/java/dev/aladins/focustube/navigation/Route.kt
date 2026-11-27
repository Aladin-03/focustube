package dev.aladins.focustube.navigation

import kotlinx.serialization.Serializable

/**
 * Every navigation destination in the app, as type-safe routes.
 *
 * Named `Route.X` so they never clash with the `XRoute` composables inside each feature.
 * Later milestones add Channel(channelId) and AddChannel here.
 */
sealed interface Route {
    /** Tab: AI picks for the current persona. */
    @Serializable
    data object Picks : Route

    /** Tab: My channels. */
    @Serializable
    data object Channels : Route

    /** Tab: AI and Classic search. */
    @Serializable
    data object Search : Route

    /** Tab: Watch later and History. */
    @Serializable
    data object Library : Route

    /** Full-screen Settings, opened from the gear in the top bar. */
    @Serializable
    data object Settings : Route

    /** 01: first launch, create the first persona. */
    @Serializable
    data object Welcome : Route

    /** 02: list of active personas, opened from the persona name in the top bar. */
    @Serializable
    data object Personas : Route

    /** 03 in create mode. */
    @Serializable
    data object CreatePersona : Route

    /** 03 in edit mode. */
    @Serializable
    data class EditPersona(val personaId: Long) : Route {
        companion object {
            /** SavedStateHandle key of [personaId] (the property name). */
            const val PERSONA_ID_KEY = "personaId"
        }
    }

    /** 14 / 15: the player for one video (a Short gets the tall layout). */
    @Serializable
    data class Player(val videoId: String) : Route {
        companion object {
            /** SavedStateHandle key of [videoId] (the property name). */
            const val VIDEO_ID_KEY = "videoId"
        }
    }

    /** 04: archived personas with Restore. */
    @Serializable
    data object ArchivedPersonas : Route
}
