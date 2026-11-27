# FocusTube v1 milestones

Built in this order. Each milestone: plan -> approval -> build on a branch -> test on the
connected phone -> report -> Aladin tests -> merge to `main`. See `CLAUDE.md`.

Status: update this table as milestones finish.

| # | Milestone | Status |
| --- | --- | --- |
| 1 | Foundation | Done |
| 2 | Personas | Not started |
| 3 | Classic search and player | Not started |
| 4 | Library | Not started |
| 5 | My channels | Not started |
| 6 | AI picks | Not started |
| 7 | AI search | Not started |
| 8 | Polish and release | Not started |

## 1. Foundation
**Scope:** Gradle project (version catalog, Kotlin, Compose, Hilt, KSP, Spotless/ktlint), app id
`dev.aladins.focustube`, the package structure from `ARCHITECTURE.md` (packages are created when
first used), design-system theme
(colours light/dark, bundled fonts, type, shapes, spacing), the core shared components that the
empty screens need (`FtTopBar`, `FtBottomBar`/rail, `EmptyState`, buttons), navigation with the
four tabs and placeholder screens, the Settings route, `.gitignore` with all secrets excluded,
`local.properties` key wiring into `BuildConfig`, and a GitHub Actions workflow that builds the
debug APK and runs unit tests on every push.
**Acceptance:**
- App installs and opens on the phone; four tabs switch; gear opens Settings placeholder.
- Light and dark themes both match `DESIGN.md` tokens (screenshots of each tab in both).
- No key or secret in the repo (`git log -p` shows none). Missing key gives a clear warning.
- `lintDebug`, `testDebugUnitTest` pass; a theme/token unit test or screenshot check exists.

## 2. Personas
**Scope:** Room database (all tables from `ARCHITECTURE.md`, schema export on), persona
repository, screens 01-04, persona switcher in the top bar, start tab rule (goal -> Picks,
no goal -> My channels), current persona remembered across restarts.
**Acceptance:** first launch shows Welcome; create 3 personas; switch; edit; archive one; restore
it; force-stop and reopen: everything kept. DAO and repository tests pass.

## 3. Classic search and player
**Scope:** YouTube client, `QuotaTracker`, `AppError`, Classic search (screen 12) with region
sheet (13) and Videos/Shorts filter, saved searches, player (14, 15) with FocusTube end state,
LimitBanner (18), Settings: default regions + YouTube daily limit display.
**Acceptance:** search, filter, open and watch a video and a Short; nothing plays next; same search
twice spends no units; simulated quota error shows the banner and keeps saved results.

## 4. Library
**Scope:** Watch later, History (screen 16), watched rule (`MarkWatchedUseCase`), Watch later
actions from player and lists.
**Acceptance:** save videos; watch past half -> appears in History as watched; swipe removes from
Watch later; per-persona separation verified.

## 5. My channels
**Scope:** Add channel (09) with persona checklist, My channels feed and list (08), channel page
(10), `DailyUploadsWorker`, Check now.
**Acceptance:** follow channels in two personas; feed groups by day; daily worker runs (trigger it
manually with adb for the test); channel page sorts work.

## 6. AI picks
**Scope:** Settings OpenAI key (encrypted, Test), `RefreshPicksUseCase` pipeline, screens 05-07,
feedback (thumbs, never show channel), New voice quota (500-100,000 subscribers), no-key screen
(19), goal-less behaviour.
**Acceptance:** with a key and a goal, Refresh gives 12 picks with reasons and levels and about 3
New voice; thumbs down removes immediately; hidden channel never returns; without a key, screen 19.
Pipeline filter and new-voice rule have unit tests with fake data.

## 7. AI search
**Scope:** AI search (11) with reasons, levels, "Your channel" tags, saved results, AI/Classic toggle.
**Acceptance:** AI search returns ranked results; repeat search spends no units; own-channel
matches tagged.

## 8. Polish and release
**Scope:** Aladin's home-screen changes (to be discussed), empty and error states everywhere,
tablet layout (navigation rail on wide screens, content at comfortable width), accessibility pass
(content descriptions, touch targets, contrast), performance check, signed release APK
(keystore kept out of git), GitHub release.
**Acceptance:** works on Aladin's phone and tablet; release APK installs on a second phone.
