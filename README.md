# FocusTube

FocusTube is a personal Android app for watching YouTube with focus: no recommendations feed, no
autoplay and no comments. Each persona (for example "Guitar", "Cooking" or "General") has its own
AI picks for a goal, followed channels, searches, Watch later and History. It is shared as an APK
and not published on the Play Store. Built with Kotlin and Jetpack Compose.

## Setup

Requirements: Android Studio (or JDK 17+ and the Android SDK with platform 37), and a YouTube Data
API v3 key.

1. Clone the repository.
2. Add your YouTube key to `local.properties` in the project root (this file is git-ignored and
   must never be committed):

   ```
   YOUTUBE_API_KEY=your-key-here
   ```

   Without a key, debug builds still work: Gradle prints a warning and YouTube features show
   "YouTube key missing". Release builds fail until the key is set.
3. Turn on the secret check that runs before every commit:

   ```
   git config core.hooksPath .githooks
   ```

4. Build and install on a connected phone:

   ```
   ./gradlew assembleDebug       # build the debug APK
   ./gradlew installDebug        # install it on the connected phone
   ./gradlew testDebugUnitTest   # unit tests
   ./gradlew lintDebug           # Android lint
   ./gradlew spotlessApply       # format the code
   ```

The OpenAI key is never part of the build: each user enters their own in the app's Settings.

## Docs

- [Requirements](docs/REQUIREMENTS.md)
- [Design](docs/DESIGN.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Milestones](docs/MILESTONES.md)

Fonts: Schibsted Grotesk, Inter and JetBrains Mono, bundled under the SIL Open Font License
(see `app/src/main/assets/licenses`).
