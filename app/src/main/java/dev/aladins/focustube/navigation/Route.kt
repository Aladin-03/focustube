package dev.aladins.focustube.navigation

import kotlinx.serialization.Serializable

/**
 * Every navigation destination in the app, as type-safe routes.
 *
 * Named `Route.X` so they never clash with the `XRoute` composables inside each feature.
 * Later milestones add Onboarding, Personas, EditPersona(personaId), ArchivedPersonas,
 * Player(videoId), Channel(channelId) and AddChannel here.
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
}
