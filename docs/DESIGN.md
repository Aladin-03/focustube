# FocusTube design

Approved by Aladin on 27 Nov 2026. Aladin plans some changes to the home screen later
(Milestone 8); build what is here until then.

Direction: calm and quiet. Pine green on soft paper, like a well-kept notebook. No red, no badges,
no counters. The one accent marks what the user chose to focus on.

## Colour tokens

Put these in `core/designsystem/theme/Color.kt` and map them onto a Material 3 `ColorScheme`
plus a small `FocusTubeColors` extension for tokens Material doesn't have. Feature code uses
tokens only, never hex values.

| Token | Light | Dark | Used for |
| --- | --- | --- | --- |
| paper (background) | `#F4F5F1` | `#0E1412` | Screen background |
| surface | `#FFFFFF` | `#161E1B` | Bottom bar, cards, inputs, sheets |
| surface2 | `#EBEEE8` | `#1E2824` | Reason box, segmented control track, icon buttons |
| line (outline) | `#DCE0D9` | `#2A3531` | Borders, dividers |
| ink (onBackground) | `#16201C` | `#E5ECE8` | Main text |
| muted (onSurfaceVariant) | `#5B6761` | `#9BA9A2` | Meta text, inactive icons |
| pine (primary) | `#1E6B58` | `#6CCBAE` | Accent, buttons, active tab, level meter |
| onPine (onPrimary) | `#FFFFFF` | `#08221A` | Text on primary buttons |
| pineSoft (primaryContainer) | `#DCEDE5` | `#1B3A31` | Active tab pill, persona avatar, "Your channel" tag |
| amberSoft | `#F8E9D2` | `#3A2B14` | "New voice" tag, daily-limit banner background |
| amberInk | `#6B3D06` | `#F2C88C` | Text on amberSoft (tag text can use `#8A4E08` in light) |
| meterOff | `#C9D6CF` | `#34433D` | Unfilled bars of the level meter |
| videoScrim | `rgba(14,20,18,0.8)` | same | Duration badge on thumbnails |

Theme follows the system light/dark setting.

## Typography

Bundle the fonts as resources (all are on Google Fonts under the SIL Open Font License).

| Style | Font | Size / weight | Used for |
| --- | --- | --- | --- |
| screenTitle | Schibsted Grotesk | 24 sp / 600, letter-spacing -0.02em | Persona name in top bar, screen titles |
| displayTitle | Schibsted Grotesk | 32 sp / 600 | Welcome heading |
| section | Schibsted Grotesk | 20 sp / 600 | "Today's picks", day headers (15 sp) |
| videoTitle | Inter | 16 sp / 600 (14 sp in rows) | Video titles |
| body | Inter | 14 sp / 400, line height 1.45 | AI reasons, descriptions |
| meta | Inter | 13 sp / 400, muted | Channel, views, date |
| label | Inter | 12 sp / 500-600 | Tab labels, chips |
| mono | JetBrains Mono | 11-12 sp / 500, letter-spacing 0.04em, uppercase for status lines | Durations, "12 PICKS · SAVED 3 H AGO", "CHECKED TODAY 08:10" |

## Shape and spacing

- Thumbnails: 16 dp corners (large), 12 dp (rows). Cards and settings groups: 18 dp. Sheets: 18 dp top corners.
- Chips, buttons, tabs pills, search field: fully rounded.
- Touch targets at least 44-48 dp. Spacing on a 4 dp grid; screen side padding 16-20 dp.
- Primary button height 56 dp (onboarding) / 48 dp elsewhere.

## Shared components (`core/designsystem/components`)

Build each once, use everywhere.

| Component | Description |
| --- | --- |
| `FtTopBar` | Persona avatar (letter in pineSoft circle) + name + chevron as a button; right slot for an action (gear, add). |
| `FtBottomBar` / `FtNavRail` | Four tabs: Picks (sparkle), Channels (screen), Search (magnifier), Library (bookmark). Active = icon inside a pineSoft pill + ink bold label. Rail on wide screens. |
| `PrimaryButton`, `SecondaryButton` | Filled pine / outlined line. Fully rounded. |
| `RegionChip` | Globe icon + "US, UK, DE +4". Opens the region sheet. |
| `SegmentedTabs` | Two options in a surface2 track; selected option on surface with a light shadow. Used for New uploads / Channels, Watch later / History, AI / Classic. |
| `LevelMeter` | Three small rising bars (4×7, 4×10, 4×13 dp) + level word. Filled bars in pine, the rest meterOff. Beginner = 1, Intermediate = 2, Advanced = 3. |
| `Tag` | Small uppercase label, 6 dp corners: "New voice" (amber), "Short" (surface2 / muted), "Your channel" (pineSoft / pine), "Coming later". |
| `Thumbnail` | Rounded image with duration badge (mono, videoScrim) and optional progress bar (pine) along the bottom. |
| `PickCard` | Large thumbnail, title (+ New voice tag), meta, reason box (surface2) with LevelMeter, action row: thumbs up, thumbs down, "Later", more (⋯). |
| `VideoRow` | 136×77 dp thumbnail left, title / meta / tags right. Used in channels, search, library. |
| `ReasonBox` | surface2 box with optional LevelMeter + one-line reason. |
| `StatusLine` | Mono uppercase muted line ("SAVED 3 H AGO") + optional Refresh icon button. |
| `LimitBanner` | amberSoft box, clock icon, title + explanation. |
| `EmptyState` | Illustration block, title, text, primary button, optional text link. |
| `SettingsGroup` / `SettingsRow` | Rounded surface group with mono section label above; rows with value + chevron. |
| `OptionsSheet` | Bottom sheet for pick options and add-to-personas checklist. |

Icons: simple 2 dp stroke line icons (Material Symbols Rounded outlined is fine). No emoji in UI.

## Screens

The approved wireframes number the screens; keep these names in code.

| # | Screen | Key content |
| --- | --- | --- |
| 01 | Welcome / create first persona | Logo mark, "Who is this feed for?", name field, optional goal field, info box ("With a goal, FocusTube picks videos for you and says why..."), Create persona button. |
| 02 | Personas | Persona cards (current one outlined), + New persona, Archived (n) link. |
| 03 | Create / edit persona | Name, goal, hidden channels, Archive persona (with "keeps everything" note). |
| 04 | Archived personas | List with Restore buttons. |
| 05 | Picks | Top bar, goal line, "Today's picks" + status line, region chip, refresh button, PickCards, bottom bar. |
| 06 | Picks, no goal | EmptyState "No picks without a goal" + Add a goal / Search instead. |
| 07 | Pick options sheet | Save to watch later, Not for my goal, Never show [channel], Add [channel] to My channels. |
| 08 | My channels | Top bar with + (add channel), SegmentedTabs New uploads / Channels (n), "CHECKED TODAY 08:10" + Check now, day groups of VideoRows. |
| 09 | Add channel | Search field, channel rows with Add / Added, sheet "Add [channel] to..." with persona checkboxes. |
| 10 | Channel page | Avatar, name, subscribers, video count, "In: Guitar, General · Edit", sort chips Newest / Popular, uploads. Leave space for a third chip "Best for your goal" (later). |
| 11 | AI search | Rounded search field with persona avatar, AI / Classic toggle, region chip, status line, VideoRows with LevelMeter, tags and reason box. |
| 12 | Classic search | Same layout, plain results, Videos / Shorts filter. |
| 13 | Region filter sheet | Country checkboxes, + Add a country, Reset to defaults, Apply. |
| 14 | Player | Back, "Watching as [persona]" chip, 16:9 player, title, channel row + Channel button, actions (Helpful, thumbs down, Watch later toggle), description card, end mark "That's it. Nothing plays next." Leave room for a Summarize action (later). |
| 15 | Player, Short | Same screen with a tall video. |
| 16 | Library | SegmentedTabs Watch later (n) / History, total time line, VideoRows with progress bars and notes ("Stopped at 6:50"). |
| 17 | Settings | AI group (OpenAI key + Test + status), Videos group (default regions, YouTube daily limit with progress bar and reset time), Personas group (Archived, Backup "Coming later"), version footer. |
| 18 | Daily limit reached | LimitBanner on top of the saved content; refresh greyed out. |
| 19 | No OpenAI key | EmptyState "Add your OpenAI key" + Open Settings / Use Classic search. |

Sample content used in the design: personas Guitar ("Learn fingerstyle guitar. I'm a beginner."),
Cooking, General; channels Fingerstyle Fred, Ana Plays, Strum School, Lena Strings.
Use these for previews and fake data, never for real defaults.

## Copy style

Plain, short, friendly. Name things as the user sees them. Errors say what happened and what to
do. No exclamation marks, no emoji.
