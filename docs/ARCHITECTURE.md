# FocusTube architecture

## Principles

1. **One place for everything.** Every kind of code has exactly one home (table below). Before
   writing anything, check whether it already exists there.
2. **Layers only call downward:** UI -> ViewModel -> Repository -> Data source.
   A screen never touches Retrofit, Room or DataStore.
3. **Cache first.** Repositories return what is saved on the phone. They call YouTube or OpenAI
   only when nothing is saved or the user taps Refresh.
4. **Features don't import each other.** Shared things move down into `core`, `domain` or `data`.
   Features talk only through navigation routes.
5. **Pure logic is testable.** Rules like "watched", the picks filter, the new-voice quota and the
   Pacific-day quota reset live in plain Kotlin classes in `domain`, with unit tests.

## Folder structure

Single Gradle app module for v1, with strict packages. Base package `dev.aladins.focustube`.

This is the target structure. A package is created when the first file that belongs in it is
written; there are no empty or placeholder packages.

```
app/src/main/java/dev/aladins/focustube/
├── FocusTubeApp.kt              # @HiltAndroidApp Application; WorkManager setup
├── MainActivity.kt              # Single activity, sets FocusTubeTheme + AppNavHost
│
├── navigation/                  # How screens connect
│   ├── AppScaffold.kt           # App shell: top bar, bottom bar / rail, centred content, NavHost
│   ├── AppNavHost.kt            # NavHost with every route; navigateToTab()
│   ├── Route.kt                 # Type-safe routes: sealed interface Route { Picks, Settings... }
│   └── TopLevelDestination.kt   # The 4 tabs (icon, label, route)
│
├── core/                        # Shared, feature-agnostic building blocks
│   ├── designsystem/
│   │   ├── theme/               # Color.kt, Type.kt, Shape.kt, FocusTubeSpacing.kt, Theme.kt,
│   │   │                        #   FocusTubeColors.kt, FocusTubeTypography.kt
│   │   ├── components/          # FtTopBar, FtBottomBar, PickCard, VideoRow, LevelMeter, Tag...
│   │   └── icons/               # FocusTubeIcons (one object listing every icon)
│   ├── common/
│   │   ├── result/              # AppResult / AppError (QuotaExceeded, NoOpenAiKey, Network...)
│   │   ├── time/                # Clock interface, PacificDay helper, "3 h ago" formatter
│   │   ├── format/              # Duration, view count ("84K"), subscriber formatters
│   │   └── dispatchers/         # Injected coroutine dispatchers
│   └── ui/                      # UiState helpers, collectAsStateWithLifecycle wrappers, previews
│
├── domain/                      # Business rules: plain Kotlin, no Android imports
│   ├── model/                   # Persona, Channel, Video, Pick, PickBatch, Level, Region...
│   ├── repository/              # Repository INTERFACES (PersonaRepository, VideoRepository...)
│   └── usecase/                 # One job each: RefreshPicksUseCase, MarkWatchedUseCase,
│                                #   FilterCandidatesUseCase, ApplyNewVoiceQuotaUseCase...
│
├── data/                        # Where data comes from
│   ├── repository/              # Repository IMPLEMENTATIONS (cache-first logic lives here)
│   ├── local/
│   │   ├── database/            # FocusTubeDatabase, Converters, migrations
│   │   ├── dao/                 # One DAO per table group
│   │   └── entity/              # Room entities
│   ├── remote/
│   │   ├── youtube/             # YouTubeApi (Retrofit), dto/, YouTubeDataSource
│   │   ├── openai/              # OpenAiApi (Retrofit), dto/, OpenAiDataSource, prompts/
│   │   └── quota/               # QuotaTracker + OkHttp interceptor counting YouTube units
│   ├── settings/                # SettingsStore (encrypted DataStore): OpenAI key, default regions
│   └── mapper/                  # Entity <-> domain, DTO <-> domain. Each mapping written once.
│
├── di/                          # Hilt modules only, each added with the first thing it provides
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   ├── RepositoryModule.kt      # Binds interfaces to implementations
│   ├── SettingsModule.kt
│   └── DispatchersModule.kt
│
├── work/                        # Background jobs
│   └── DailyUploadsWorker.kt    # Checks followed channels once a day
│
└── feature/                     # One package per area of the app
    ├── onboarding/              # 01 Welcome
    ├── personas/                # 02 Personas, 03 Edit, 04 Archived
    ├── picks/                   # 05, 06, 07
    ├── channels/                # 08 My channels, 09 Add channel, 10 Channel page
    ├── search/                  # 11 AI search, 12 Classic search, 13 Region sheet
    ├── player/                  # 14, 15
    ├── library/                 # 16
    └── settings/                # 17
```

Route naming: navigation destinations are `Route.X` objects in `navigation/Route.kt`
(`Route.Picks`, `Route.Player(videoId)`). The `XRoute` composables below are the per-screen entry
points. The two never share a name.

Layout tokens: `FocusTubeSpacing.screenHorizontal` (16 dp) is every screen's side padding, and
`FocusTubeSpacing.contentMaxWidth` (600 dp) caps content width on tablets, where it is centred
next to the navigation rail. The rail replaces the bottom bar from medium width (600 dp) up.

Inside each feature package:

```
feature/picks/
├── PicksScreen.kt          # Stateless composable: takes UiState + callbacks
├── PicksRoute.kt           # Gets the ViewModel, collects state, calls PicksScreen
├── PicksViewModel.kt       # Holds state, calls use cases / repositories (added once the screen has state)
├── PicksUiState.kt         # Data class (or sealed interface) for everything the screen shows
└── components/             # Composables used ONLY by this feature.
                            # Used by a second feature? Move it to core/designsystem/components.
```

Tests mirror the same packages under `app/src/test/` (unit) and `app/src/androidTest/` (UI, Room).

### Where does it go?

| I'm writing... | It goes in |
| --- | --- |
| A colour, font, size, shape, spacing | `core/designsystem/theme` |
| A composable used by 2+ features | `core/designsystem/components` |
| A composable used by one feature | `feature/<name>/components` |
| A formatter ("84K views", "11:04", "3 h ago") | `core/common/format` or `core/common/time` |
| A business rule (watched, filter, new voice) | `domain/usecase` |
| A data class the app reasons about | `domain/model` |
| Saving or loading from the phone | `data/local` (entity + dao) behind a repository |
| A YouTube or OpenAI call | `data/remote/<service>` behind a repository |
| Cache-or-fetch decisions | `data/repository` |
| Converting between layers | `data/mapper` |
| Wiring with Hilt | `di` |
| A background job | `work` |

### Coming from Flutter

| Flutter habit | FocusTube equivalent |
| --- | --- |
| `widgets/` or `components/` | `core/designsystem/components` |
| Provider / Riverpod notifier | ViewModel exposing `StateFlow` |
| Service (API client) | `data/remote/.../DataSource` |
| Repository | `domain/repository` (interface) + `data/repository` (implementation) |
| get_it / provider DI | Hilt modules in `di/` |
| Models | `domain/model` (app), `entity` (database), `dto` (network) |

## Libraries

Versions in `gradle/libs.versions.toml` (version catalog). Use current stable versions.

| Need | Library |
| --- | --- |
| UI | Jetpack Compose (BOM), Material 3, Material 3 adaptive (window size class for bottom bar / rail), Navigation Compose with type-safe routes |
| Lifecycle | lifecycle-viewmodel-compose, lifecycle-runtime-compose |
| DI | Hilt (+ hilt-navigation-compose, hilt-work) |
| Database | Room with KSP |
| Network | Retrofit, OkHttp (+ logging in debug only, with keys redacted), kotlinx.serialization |
| Images | Coil 3 |
| Player | `com.pierfrancescosoffritti.androidyoutubeplayer:core` (wraps the official IFrame player) |
| Background | WorkManager |
| Settings | DataStore, encrypted with Android Keystore (Tink) |
| Formatting | Spotless + ktlint |
| Tests | JUnit, kotlinx-coroutines-test, Turbine, MockWebServer, Room testing, Compose UI test |

Min SDK 26. Target SDK 37, compile SDK 37.2 (Compose 1.13 needs 37.1 or later).
Add each library to the version catalog in the milestone that first uses it.

## Data model (Room)

| Table | Holds | Key fields |
| --- | --- | --- |
| persona | Each persona | id, name, goal?, regions, archivedAt?, createdAt |
| channel | Every channel the app has seen | youtubeId, title, avatarUrl, subscriberCount, uploadsPlaylistId, lastCheckedAt, fetchedAt |
| persona_channel | Persona follows channel | personaId, channelId, addedAt |
| video | Every video shown anywhere | youtubeId, title, channelId, durationSec, publishedAt, viewCount, isShort, description, thumbnailUrl, fetchedAt |
| pick_batch | A set of 12 picks | id, personaId, createdAt, regions |
| pick | A video in a batch | batchId, videoId, rank, reason, level, isNewVoice, removed |
| search_cache | A saved search | id, personaId, query, mode (AI/CLASSIC), regions, shortsFilter, createdAt |
| search_result | A video in a saved search | cacheId, videoId, rank, reason?, level? |
| feedback | Thumbs | personaId, videoId, channelId, type (UP/DOWN), at |
| hidden_channel | Never show this channel | personaId, channelId, at |
| watch_later | Library tab | personaId, videoId, addedAt |
| history | Library tab | personaId, videoId, watchedAt, progressSec, isWatched |
| quota_log | YouTube units spent | pacificDate, unitsUsed |

Persona-owned rows always carry `personaId`. Archive sets `archivedAt`; nothing is deleted.
Room schema export is on (`room.schemaLocation`) so migrations are tracked from the start.

## YouTube Data API and quota

Daily limit: 10,000 units, resets at midnight Pacific. Costs (approximate, from YouTube's docs):
`search.list` = 100, `videos.list` / `channels.list` / `playlistItems.list` = 1 per call (up to 50 ids).

- Every YouTube request goes through `QuotaTracker`, which adds the call's cost to today's
  `quota_log` row (Pacific date).
- A 403 `quotaExceeded` response, or the tracked total reaching the limit, returns
  `AppError.QuotaExceeded`. Screens then show `LimitBanner`, disable Refresh and new searches,
  and keep showing saved data.
- Batch video detail calls (50 ids per `videos.list`).
- New uploads: use each channel's uploads playlist (`playlistItems.list`, 1 unit), not search.
- Add channel uses `search.list` with `type=channel` (100 units); cache results.
- Shorts detection: duration of 60 seconds or less (refine later if needed).
- Data older than 30 days is refetched or dropped.

## OpenAI

- Called from `OpenAiDataSource` only, with the key from `SettingsStore`. No key ->
  `AppError.NoOpenAiKey` -> screen 19.
- Default model: a small, cheap model (e.g. `gpt-4.1-mini`). Keep the model name in one constant
  so it can change.
- Ask for JSON output and parse it with kotlinx.serialization. Invalid JSON -> one retry, then a
  clear error.
- Prompts live in `data/remote/openai/prompts/` as functions that build the request text.

## AI picks pipeline (RefreshPicksUseCase)

1. **Write queries:** OpenAI turns the goal + feedback summary into 4 search phrases. Save them;
   rewrite only when the goal changes.
2. **Gather:** one `search.list` per phrase with the persona's regions (rotate regionCode across
   the persona's regions), ~25 results each. Add recent uploads from the persona's own channels
   from the cache (free).
3. **Filter on numbers (code, no AI):** remove duplicates, watched, thumbs-down videos, hidden
   channels. Fetch details (`videos.list`, `channels.list`). Mark small creators
   (500-100,000 subscribers). Keep the best 60 by views per day.
4. **Rank:** OpenAI gets goal, feedback summary (last 20 up, last 20 down) and the 60 candidates
   (title, channel, duration, subscribers, first 200 characters of description). Returns 12 with
   a one-line reason and level.
5. **New voice quota:** if fewer than 3 of the 12 are small creators, swap in the best-ranked
   small ones. Mark them `isNewVoice`.
6. **Save** as a new `pick_batch`. Shown until the next Refresh.

AI search uses steps 2-4 with the user's query (1-2 searches), includes matching cached videos
from the persona's own channels (tag "Your channel"), and returns the top 15.

## Player

- Use android-youtube-player with controls on, `rel=0`, no autoplay of other videos.
- On end of video, stop and show FocusTube's own end state over the player so YouTube's end-screen
  suggestions are never shown.
- Report progress to `MarkWatchedUseCase` (watched past ~50%, or the end for a Short).

## Secrets and build config

- `local.properties`: `YOUTUBE_API_KEY=...` -> `buildConfigField` in `app/build.gradle.kts`.
  If it is missing, debug builds print a clear warning and use an empty key (the app then shows
  a "YouTube key missing" error); release builds fail with a clear message.
- CI (GitHub Actions): no YouTube key. The repo is public, so CI builds with the empty-key
  fallback, and the uploaded debug APK carries no key. CI also runs gitleaks over the full history.
- `.githooks/pre-commit` blocks commits whose staged diff looks like a key or adds a secret file,
  and runs gitleaks when installed. Enable per clone: `git config core.hooksPath .githooks`.
- `.gitignore` must include `local.properties`, `*.jks`, `*.keystore`, `keystore.properties`,
  `/screenshots/`, build outputs.
- The OpenAI key is never in the build.
