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
├── MainActivity.kt              # Single activity: splash until AppViewModel knows the start, then AppScaffold
│
├── navigation/                  # How screens connect
│   ├── AppScaffold.kt           # App shell: top bar, bottom bar / rail, centred content, NavHost
│   ├── AppNavHost.kt            # NavHost with every route; navigateToTab(), resetToTab()
│   ├── AppViewModel.kt          # Loading / NeedsWelcome / Ready(current persona)
│   ├── Route.kt                 # Type-safe routes: sealed interface Route { Picks, Settings... }
│   └── TopLevelDestination.kt   # The 4 tabs (icon, label, route); Persona.startTab()
│
├── core/                        # Shared, feature-agnostic building blocks
│   ├── designsystem/
│   │   ├── theme/               # Color.kt, Type.kt, Shape.kt, FocusTubeSpacing.kt, Theme.kt,
│   │   │                        #   FocusTubeColors.kt, FocusTubeTypography.kt
│   │   ├── components/          # FtTopBar, FtBottomBar, PickCard, VideoRow, LevelMeter, Tag...
│   │   └── icons/               # FocusTubeIcons (one object listing every icon)
│   ├── common/
│   │   ├── result/              # AppResult / AppError (QuotaExceeded, NoOpenAiKey, Network...)
│   │   ├── time/                # PacificDay helper, "3 h ago" formatter (the clock is kotlin.time.Clock)
│   │   ├── format/              # Duration, view count ("84K"), subscriber formatters
│   │   └── dispatchers/         # Injected coroutine dispatchers
│   └── ui/                      # UI shared by 2+ features that isn't a design-system component:
│                                #   PersonaFormFields (Welcome + Create/edit), UiState helpers
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
│   │   ├── database/            # FocusTubeDatabase, migrations/ (written Migration objects)
│   │   ├── dao/                 # One DAO per table group
│   │   └── entity/              # Room entities
│   ├── remote/
│   │   ├── youtube/             # YouTubeApi (Retrofit), dto/, YouTubeDataSource, YouTubeKeyInterceptor
│   │   ├── openai/              # OpenAiApi (Retrofit), dto/, OpenAiDataSource, prompts/
│   │   └── quota/               # QuotaTracker + QuotaInterceptor (counts YouTube units)
│   ├── settings/                # AppPreferences (plain DataStore: current persona, default
│   │                            #   regions); SettingsStore (encrypted DataStore): OpenAI key
│   └── mapper/                  # Entity <-> domain, DTO <-> domain. Each mapping written once.
│
├── di/                          # Hilt modules only, each added with the first thing it provides
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   ├── RepositoryModule.kt      # Binds interfaces to implementations
│   ├── SettingsModule.kt        # DataStore(s)
│   ├── TimeModule.kt            # kotlin.time.Clock (Clock.System; tests use a fixed clock)
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
Unit-test fakes live in `app/src/test/.../testing/`. Fakes needed by both unit and on-device
tests (e.g. `FakeYouTubeApi`) live once in `app/src/sharedTest/java/.../testing/`. Instrumented
tests run with `HiltTestRunner`; `androidTest/.../di/` swaps in an in-memory database, a fresh
DataStore and the fake YouTube API, so UI tests never spend real units. Composables that need Hilt
ViewModels are tested inside `HiltTestActivity` (debug build only).

Debug-only code lives in `app/src/debug/`: the key-redacting HTTP logger, `HiltTestActivity`, and
`DebugQuotaReceiver`, which simulates the daily limit from adb (only callers with the DUMP
permission, i.e. the adb shell):
`adb shell am broadcast -n dev.aladins.focustube/.debug.DebugQuotaReceiver --es state exhausted`
(or `--es state reset [--el units N]`).

### Navigation and the current persona

- The NavHost starts on `Route.Welcome` when there is no active persona, otherwise on the current
  persona's start tab (`Persona.startTab()`: Picks with a goal, Channels without).
- The current persona's start tab is the root of the back stack. Back from another tab returns
  there, then leaves the app.
- Screens never navigate on a persona change. They only change data (create, switch, archive);
  `AppScaffold` sees the current persona's id change and clears the back stack to the new
  persona's start tab.
- Full-screen routes (Welcome, Personas, CreatePersona, EditPersona(personaId),
  ArchivedPersonas, Settings) hide the tabs and draw their own top bar.

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
| persona | Each persona (since v1) | id, name, goal?, regions ("US,GB,DE"), created_at, archived_at? |
| video | Every video shown anywhere (since v2) | youtube_id, title, channel_id, channel_title, duration_sec, published_at, view_count?, is_short, description, thumbnail_url?, fetched_at |
| search_cache | A saved search (since v2) | id, persona_id, query, query_normalized, mode (CLASSIC/AI), regions (sorted), region_order, pages_loaded, page_tokens (JSON per region), created_at, last_used_at. Unique: persona + query_normalized + mode + regions |
| search_result | A video in a saved search (since v2) | cache_id (deleted with its search), video_id, rank, reason?, level? |
| quota_log | YouTube units this phone spent (since v2) | pacific_date, units_used |
| channel | Every channel the app has seen | youtubeId, title, avatarUrl, subscriberCount, uploadsPlaylistId, lastCheckedAt, fetchedAt |
| persona_channel | Persona follows channel | personaId, channelId, addedAt |
| pick_batch | A set of 12 picks | id, personaId, createdAt, regions |
| pick | A video in a batch | batchId, videoId, rank, reason, level, isNewVoice, removed |
| feedback | Thumbs | personaId, videoId, channelId, type (UP/DOWN), at |
| hidden_channel | Never show this channel | personaId, channelId, at |
| watch_later | Library tab | personaId, videoId, addedAt |
| history | Library tab | personaId, videoId, watchedAt, progressSec, isWatched |

Tables are created in the milestone that first uses them (v1 has only `persona`); the table above
is the plan. Times are stored as epoch milliseconds; mappers convert to `kotlin.time.Instant`.
Region codes are ISO 3166-1 alpha-2 (the UK is `GB`).

Persona-owned rows always carry `personaId`. Archive sets `archived_at`; nothing is deleted.
The current persona is not a table: it is `current_persona_id` in `AppPreferences`.

### Schema changes and migrations

An app update must never wipe a persona, Watch later or History.

- Never call `fallbackToDestructiveMigration`. A missing migration crashes on open instead,
  which tests catch before release.
- Every schema change bumps `version` in `FocusTubeDatabase`. Room exports each version's
  schema to `app/schemas/<database class>/<version>.json` (Room Gradle plugin). These files are
  committed and never edited; CI fails if a build changes or adds a schema file that isn't
  committed.
- Adding tables, or columns with a default: `AutoMigration(from = N - 1, to = N)`. Renames and
  deletions: an `AutoMigrationSpec` (`@RenameColumn`, `@DeleteColumn`...). Anything that reshapes
  data: a written `Migration` in `data/local/database/migrations/`.
- `androidTest/.../MigrationTest` creates each old version from its schema file, inserts rows,
  opens the database with the current code and checks the rows survived. Add a case per version.
- Updates keep data only when signed with the same key, so the release keystore must be kept
  forever (Milestone 8).

## YouTube Data API and quota

Daily limit: 10,000 units, resets at midnight Pacific. Costs (approximate, from YouTube's docs):
`search.list` = 100, `videos.list` / `channels.list` / `playlistItems.list` = 1 per call (up to 50 ids).

- Every YouTube request goes through `QuotaInterceptor`: it refuses a request that would pass
  today's limit (never sent), otherwise `QuotaTracker` adds the call's cost to today's `quota_log`
  row (Pacific date, `America/Los_Angeles`) once a response arrives. The count is this phone's only;
  the limit is shared by everyone using the key, so YouTube's 403 is the final word.
- `YouTubeKeyInterceptor` adds the key only to `www.googleapis.com` requests. Thumbnails use a
  separate OkHttp client with no key. Errors never include URLs; the debug HTTP log redacts `key`.
- Classic search: `search.list` (`part=id`, `type=video`, `videoEmbeddable=true`,
  `safeSearch=moderate`, `maxResults=50`, one `regionCode`) then one `videos.list` for details:
  101 units per page of 50. Page 1 uses the persona's first region; "More results" takes the other
  regions in turn (their page 1), then their next pages. The Videos / Shorts filter works on saved
  results (0 units). A saved search is reused until Refresh or until it is 30 days old.
- A 403 `quotaExceeded` response, or the tracked total reaching the limit, returns
  `AppError.QuotaExceeded`. Screens then show `LimitBanner`, disable Refresh and new searches,
  and keep showing saved data.
- Batch video detail calls (50 ids per `videos.list`).
- New uploads: use each channel's uploads playlist (`playlistItems.list`, 1 unit), not search.
- Add channel uses `search.list` with `type=channel` (100 units); cache results.
- Shorts detection: 3 minutes or less **and** vertical. `videos.list` is called with
  `maxHeight`, so YouTube reports the embed size; taller than wide = vertical. Live or upcoming
  videos (duration 0) are never Shorts.
- Data older than 30 days is refetched or dropped: on app start, saved searches older than 30 days
  and video rows no saved search uses are deleted (`SearchRepository.removeStale`).

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

- Use android-youtube-player (YouTube's official IFrame player in a WebView) with controls on,
  `rel=0`, `iv_load_policy=3`, nothing queued after the video. The tapped video starts at once.
- YouTube's own fullscreen button is off: rotating to landscape, or FocusTube's full-screen
  button, shows only the player with system bars hidden. `MainActivity` handles rotation itself
  (`configChanges`), so the WebView isn't recreated and playback continues.
- If the screen is recreated anyway (theme change), playback resumes where it was; an ended video
  is only loaded, never played again under the end state.
- Known limitation: while paused, YouTube's embed can show a "More videos" shelf. It can't be
  turned off in the embedded player.
- On end of video, stop and show FocusTube's own end state over the player so YouTube's end-screen
  suggestions are never shown.
- Report progress to `MarkWatchedUseCase` (watched past ~50%, or the end for a Short). Until
  Milestone 4, progress is kept in memory only.

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
