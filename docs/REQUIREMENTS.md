# FocusTube requirements

Signed off by Aladin on 26 Nov 2026, with decisions added on 27 Nov 2026. This is the source of
truth for behaviour. If code and this file disagree, ask.

## Who it's for

Aladin, Aladin's sister, friends and family. Installed from an APK, never published. Phones and
Aladin's tablet. No Google sign-in. Nothing syncs back to YouTube.

## v1 scope

### Personas
- First launch asks you to create a persona: a name plus an optional goal
  (e.g. "Learn fingerstyle guitar. I'm a beginner.").
- Unlimited personas. Switch by tapping the persona name in the top bar.
- No hard delete. **Archive** a persona instead; it keeps its channels, Watch later and history.
  An Archived list (from the Personas screen and Settings) lets you restore it.
- Edit name and goal. Edit screen also lists the persona's hidden channels, which can be unhidden.
- A persona **with a goal** gets AI picks and opens on the Picks tab.
- A persona **without a goal** (e.g. "General") has no AI picks, still has both searches and
  My channels, and opens on the My channels tab. Its Picks tab invites you to add a goal.

### Navigation
- Four bottom tabs per persona: **Picks, Channels, Search, Library**.
- Top bar: persona switcher (left), Settings gear (right).
- On wide screens (tablet), the bottom tabs become a navigation rail on the left side and
  content stays at a comfortable phone-like width.

### AI picks (personas with a goal)
- 12 picks per batch. Each shows a one-line reason and a level: beginner, intermediate or advanced.
- About 3 of 12 slots go to small creators, tagged **"New voice"**. A small creator is a
  channel with **500 to 100,000 subscribers**.
- Pipeline: region first -> gather widely -> filter on numbers -> AI ranks against the goal.
  Details in `ARCHITECTURE.md`.
- Feedback: thumbs up, thumbs down, "Never show this channel" (per persona).
  Thumbs down removes the video from the list immediately and teaches the persona.
- Picks are saved and stay until the user taps Refresh.
- Watched videos drop out of picks and AI search for that persona.

### My channels
- Add a channel by searching for it. When adding, choose which personas get it
  (current persona pre-ticked). A channel can belong to several personas.
- Starts empty. No import from YouTube or Google Takeout.
- "New uploads" feed, newest first, grouped by day. New uploads are checked automatically
  once a day, plus a "Check now" button.
- Channel page: uploads with Newest and Popular sorts, which personas include it, edit or remove.

### Search
- **AI search:** results ranked against the persona's goal, each with reason and level.
  May include videos from the persona's own channels, tagged "Your channel".
- **Classic search:** plain YouTube results. Works without an OpenAI key and in every persona.
  Has a Videos / Shorts filter.
- Shorts appear mixed in results with a "Short" label.
- Same search (same persona, query, mode and regions) reuses saved results until Refresh.

### Regions
- Region filter on AI picks, AI search and Classic search.
- Defaults (set in Settings): United States, United Kingdom, Germany, France, Spain, Italy,
  Netherlands. Users can add other countries.
- Each persona remembers its own region choice; new personas start from the defaults.

### Player
- Embedded YouTube player. Title, channel, views, date, description (collapsed).
- Actions: thumbs up, thumbs down, Watch later, open channel.
- **No** recommendations, autoplay, up-next or comments. When a video ends, show FocusTube's
  own end state ("That's it. Nothing plays next.") instead of YouTube's suggestions.
- Shorts play one at a time in the same player (tall video). No swiping to the next.
- Rotating to landscape goes full screen.

### Library
- **Watch later** and **History**, both per persona, stored on the phone.
- A video counts as **watched** when played past about half its length, or to the end for a Short.
- History grouped by day; items can be removed; Watch later items removed by swipe.

### Caching and quota
- Fetch in bulk and cache picks, search results and channel lists on the phone. Screens show
  the saved copy with "updated X ago" until Refresh.
- The YouTube key is shared by all users and has a daily limit that cannot be bought.
  It resets at midnight Pacific time.
- When the limit is reached, show a clear banner: "YouTube's daily limit is reached. Your saved
  picks, channels and watch later still work. New results come back after midnight Pacific."
  Refresh and new searches are disabled. Never fail silently.
- Cached YouTube data older than 30 days is refetched or dropped (YouTube API policy).

### Settings
- OpenAI API key: enter, test ("Key works"), stored encrypted on the phone only.
- Default regions.
- YouTube daily limit: how much is used today and when it resets.
- Archived personas.
- "Backup and restore" row shown as "Coming later".

### Keys
- YouTube key: Aladin's own key, built into the APK (restricted in Google Cloud to this app's
  package name and signing certificate). Bring-your-own YouTube key is a future feature.
- OpenAI key: every user brings their own. Without one, Picks and AI search show an
  "Add your OpenAI key" screen; Classic search, channels and Library still work.

## Not in v1 (leave room in the code and UI)
- "From your channels" row on persona home: best older videos from your channels for the goal.
- "Best for your goal" sort on the channel page.
- Summarize button in the player (transcript + cheap OpenAI model; fallback to description).
- Backup export / import to a file.
- Bring-your-own YouTube key. Siblings / parent PIN. Landing page.

## Never
- No Google sign-in, no comments, no history sync with YouTube, no Shorts swipe feed,
  no autoplay, no notification badges or counters.
