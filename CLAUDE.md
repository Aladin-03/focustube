# FocusTube

A personal Android app: a focused YouTube. No recommendations feed, no autoplay, no comments.
Each **persona** (e.g. "Guitar", "Cooking", "General") has its own AI picks, followed channels,
searches, Watch later and History. Never published to the Play Store; shared as an APK with
friends and family.

Owner: Aladin. Stack: Kotlin, Jetpack Compose, Room, YouTube Data API v3, OpenAI API.
App id: `dev.aladins.focustube`.

## Read these first

| File | What it holds |
| --- | --- |
| `docs/REQUIREMENTS.md` | What the app does. The source of truth for behaviour. |
| `docs/DESIGN.md` | Colours, type, components, every screen. |
| `docs/ARCHITECTURE.md` | Folder structure, layers, libraries, data model, API and AI rules. |
| `docs/MILESTONES.md` | The 8 milestones, their scope and acceptance checks. |

If something you need is not in these docs, ask. Do not invent product behaviour.

## How we work (always)

Aladin controls scope and quality. Every milestone, and every larger task, goes like this:

1. **Plan first.** Read the docs and the current code. Write a short plan: files you will create
   or change, the approach, and open questions. **Stop and wait for approval.** Write no code
   before the plan is approved.
2. **Build on a branch** named `m<N>-<short-name>` (e.g. `m1-foundation`). Small commits with
   clear messages.
3. **Test it yourself before reporting.** Aladin's Android phone is connected to this Mac by USB.
   - `./gradlew lintDebug testDebugUnitTest` must pass.
   - `./gradlew installDebug`, then launch the app on the phone with `adb`.
   - Run `./gradlew connectedDebugAndroidTest` when the milestone has UI tests.
   - Walk the milestone's acceptance checks on the phone. Take screenshots with
     `adb exec-out screencap -p > screenshots/<name>.png` (light and dark theme) and check them.
   - Watch `adb logcat` for crashes and errors while testing. Fix anything you find.
4. **Report.** What was built, test results, screenshots, anything that differs from the plan,
   and anything Aladin should try by hand.
5. **Wait.** Aladin tests on the phone. After Aladin's OK, merge the branch into `main` and push.
   No pull requests: this is a one-person project.

Never skip or disable a test to make it pass. If a check cannot pass, say so in the report.

## Secrets: never in git

- The YouTube API key lives only in `local.properties` as `YOUTUBE_API_KEY=...` and reaches the
  code through `BuildConfig`. `local.properties` is git-ignored.
- The OpenAI key is never in code, config or git. Each user types it in Settings; it is stored
  encrypted on the phone.
- Signing keystores (`*.jks`, `*.keystore`, `keystore.properties`) are git-ignored and never committed.
- Before every commit, check the staged diff for anything that looks like a key
  (`AIza`, `sk-`, `password`, `storePassword`). If a key ever reaches a commit, stop and tell
  Aladin; do not push. The key must be rotated, and history cleaned.
- Never print a real key in logs, reports or screenshots.

## Code rules

- Follow the folder structure in `docs/ARCHITECTURE.md` exactly. New files go where that doc says.
  If something doesn't fit, ask before inventing a new top-level package.
- **DRY.** Search the codebase before writing a helper, composable, formatter or mapper. If it
  exists, reuse it. If two places need the same thing, move it to the shared place (`core/...`)
  rather than copying it.
- UI uses only design tokens and shared components from `core/designsystem`. No hard-coded
  colours, font sizes or spacing in feature screens.
- User-facing text goes in `res/values/strings.xml`.
- Screens never call the network or database. Screen -> ViewModel -> repository -> data source.
- Repositories answer from the Room cache first and only call YouTube or OpenAI when the cache is
  empty or the user taps Refresh. Every YouTube call goes through the quota counter.
- One ViewModel per screen, state exposed as `StateFlow<UiState>`. Events go in as functions.
- Prefer small files with one clear job. Name things by what the user sees (persona, pick,
  watch later), matching the docs.
- KDoc on every public class and on any function whose purpose is not obvious from its name.
- Format with ktlint (via Spotless) before committing.

## Commands

```
./gradlew assembleDebug            # build
./gradlew installDebug             # install on the connected phone
./gradlew testDebugUnitTest        # unit tests
./gradlew connectedDebugAndroidTest  # UI tests on the phone
./gradlew lintDebug                # Android lint
./gradlew spotlessApply            # format
adb devices                        # check the phone is connected
```
