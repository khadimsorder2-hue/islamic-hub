# Changelog

All notable changes to the Islamic Hub project.

## [v5.13.0] - 2026-09-25

### ⚡ 120fps + performance engine (major)
- **High refresh rate unlocked (90/120Hz)**: the app now explicitly requests the device's fastest supported display mode (`preferredDisplayModeId` on Android 11+, `preferredRefreshRate` fallback) at launch AND on every resume — no more silent 60fps cap on high-refresh phones. Wrapped defensively so exotic OEM display quirks can never crash launch.
- **Cold-start stall removed**: all three `runBlocking` main-thread blocks in `MainActivity` (DataStore reads for daily-ayah scheduling, onboarding check, app-lock check) are gone. Daily-ayah scheduling now runs on the app's IO scope; the onboarding/app-lock checks run inside the already-suspend `LaunchedEffect`.
- **Main-thread image decoding eliminated — the "app onk slow" root cause**: 12 screens decoded their hero/background WebPs synchronously during composition (`remember { loadAssetImage(...) }`), stalling the UI thread on every screen open. New `rememberAssetBitmap` pipeline: bounds-first downsampling (never decodes wider than 1600px), decode on `Dispatchers.IO`, and a 16 MB app-wide `LruCache` so re-entry is instant. Applied to Home, Prayer, Stories (×3), Namaz Shikkha (×2), Quiz, Topic Study (×2), Hadith Topics (×2), AI Scholar and Onboarding.
- **Scroll-jank fix in the entrance animation toolkit**: `staggerEntrance` read the animated value during composition, forcing every visible list item to recompose on every animation frame. The value is now read inside the `graphicsLayer` lambda — animations run purely on the draw phase (this is how 120fps animation should be wired).
- **Release network logging disabled**: `HttpLoggingInterceptor` (BASIC) is now debug-build-only — release builds skip the per-request logging I/O entirely.

### 💎 Premium UI round
- **Ken Burns cinematic hero**: the home hero image now breathes with a slow 14s zoom (1.0→1.07, reverse loop) drawn entirely on the graphics layer — zero recompositions, full refresh-rate motion.
- **Live next-prayer countdown**: the home hero countdown used to freeze at launch and even show negative "-3h -12m" after midnight. A light 30s ticker recomputes it, the day rollover (midnight) triggers a full content refresh, and negative diffs show a neutral "—".

### 🛠 Full troubleshooting pass
- Verified every lazy list in the hot paths (surah list, ayah reader) has stable `key`s.
- Verified no `runBlocking` remains on the UI thread anywhere in the app.
- Verified the v5.12.0 Notepad, More-screen entries, and Settings premium redesign remain fully wired; nothing deleted.

## [v5.7.0] - 2026-09-25

### ⚡ Quran loading — FIXED (critical)
- Root cause: opening any surah parsed the entire 4.7 MB full-Quran JSON on first use (5–20 s on mid-range phones) — the endless "loading" spinner.
- The full Quran is now split into **114 per-surah asset files** (`quran/surah/surah_001.json` …). Opening a surah parses ~40 KB → instant load. Full-Quran file remains as fallback; full-text search and the thematic engine now stream the per-surah files with a bounded memory footprint.

### 🤖 AI — Gemini 3 series + built-in default key
- Default model moved to the **Gemini 3 series**: `gemini-3-flash` (default), `gemini-3-pro`, `gemini-3-flash-lite`, plus `-preview` variants; OpenRouter preset updated to `google/gemini-3-flash-preview:free`.
- **Built-in default Gemini API key** — AI works out of the box on first launch (users can still override key/model/provider in Settings → AI Scholar).

### 🧠 AI added to Thematic Quran + Hadith topics
- Thematic Quran detail screen: new **AI ব্যাখ্যা** action in the top bar opens a premium AI popup with the topic overview + key ayah references as context.
- Hadith topic study detail screen: same AI action, context includes the topic overview + the relevant hadith references (Bukhari / Muslim / Tirmidhi / Abu Dawud).

### 🆕 Update option — discoverable
- New prominent **"অ্যাপ আপডেট"** card at the top of the More screen (jump straight to Settings → App Update where the check/download UI lives).

### 🫥 Card boxes — fully invisible
- Light + Warm Light themes: canvas now shares the exact card surface color (white / warm-white) and all `surfaceContainer*` roles are flattened — **no visible card boxes anywhere** (More screen included); grouping is done purely by spacing and typography.
- All hairline borders + elevated shadows removed from shared card components (IslamicCard, FeatureCard, PremiumCard, PillTag).

### 🔤 Bangla uccaron — more accurate
- Regenerated for all 6,236 ayahs with a **~380-entry hand-written Bangla pronunciation dictionary** (frequent Quranic words + prefixed forms) on top of an improved phonetic converter, plus corrected publication-grade overrides (Fatiha, Ayatul Kursi, Yaseen, Ar-Rahman, Kahf opening, Ad-Duha, Ash-Sharh, the 4 Quls).

## [v5.6.0] - 2026-09-24

### 🌗 Light mode card visibility — FIXED (UI polish)
- Root cause: Material 3 `surfaceContainer*` color roles were never defined, so cards fell back to the baseline scheme (~#F7F2FA) on the near-white Warm Ivory background (#FCFAF7), and filled Cards have 0 dp elevation — every card washed out into the canvas in light mode while dark mode (baseline #211F26 on #121016) kept its contrast.
- **All three schemes now define the full surfaceContainer ramp**: light mode gets a soft grey-lavender canvas (#F2EFF5) with pure-white cards; Warm Light gets a deeper cream canvas (#F2E8CE) with warm-white cards; dark mode's ramp is pinned explicitly so it can never drift back to baseline defaults (look preserved).
- `outline`/`outlineVariant` strengthened so OutlinedCards, text fields and chips read as visible hairline borders in light mode.
- Shared card components (IslamicCard, FeatureCard, PremiumCard) now draw a subtle hairline border and use the surfaceContainerLow container — crisp card edges in both themes.

### 🆕 In-app update checker — NEW
- Settings → **"অ্যাপ আপডেট"**: shows the current version, a "চেক করুন" button, and a rich status card — সর্বশেষ ভার্সন (green), নতুন ভার্সন পাওয়া গেছে with release notes + one-tap "আপডেট ডাউনলোড করুন" (opens the signed APK directly), or a retryable error row.
- **Silent auto-check on app launch** (throttled to once per 24 h, persisted in DataStore): a dismissable dialog appears when a newer release exists — never on failure, never during onboarding.
- Network design: resolves the latest tag through GitHub's `releases/latest` **HTTP redirect (zero API rate-limit impact)** — deliberately not the REST API, whose unauthenticated 60 req/h per-IP quota is routinely exhausted on carrier NAT networks; the REST API is used only best-effort for release notes. APK asset URL is HEAD-verified with a release-page fallback.

### 🔥 Fake Firebase toggle — REMOVED (honesty fix)
- The Settings screen shipped a "Firebase" status card + toggle that did **nothing**: there is no Firebase SDK, no google-services.json, and the switch only flipped a DataStore boolean while claiming "অ্যানালিটিক্স ও ক্র্যাশ রিপোর্ট চলছে". The misleading section is gone, replaced by the real App Update section. (Real Firebase integration needs a google-services.json from the app owner's own Firebase console.)

### 🔊 Audio — reliability hardening
- Verified end-to-end: the single AYAH_COUNTS source table (114 entries, sums to 6236) drives both `getAyahCount()` and `globalAyahNumber()` in AudioController and AudioDownloadService — surah/ayah → CDN absolute-index mapping is provably correct (the pre-v5.3.1 wrong-audio bug cannot regress).
- New error-recovery: a failed ayah download mid-sequence no longer stalls playback — **retry the same ayah once, then auto-advance** to the next ayah/surah (surah, khatam and repeat modes).
- Offline cache is now wired into playback: if an ayah exists in audio_cache it plays locally, else streams from cdn.islamic.network.

### 🐛 Other fixes
- Settings data-store: stale `firebase_enabled` key removed; new `last_update_check_ms` throttle key added.
- Unused `Brush` import dropped from SettingsScreen; stale Firebase feature-card composable removed.

## [v5.5.0] - 2026-09-24

### 🔤 Bangla Uccaron (Transliteration) — NEW
- **Full-Quran Bangla pronunciation** bundled offline: all 6,236 ayahs now carry a Bangla-script transliteration (`বিসমিল্লাহির রাহমানির রাহীম`), generated from the Latin `en.transliteration` edition via a digraph-aware converter with gemination conjuncts (ল্ল, ম্ম, ত্ত…), madd-run collapsing (`Laaam` → লাম) and hand-written authentic overrides for Al-Fatiha, Ayatul Kursi (2:255) and the 4 Quls (112–114).
- Shown as a tagged "উচ্চারণ" line directly under the Arabic ayah in the Quran reader, in the tafsir full screen, and inside Thematic Topic study cards.
- New setting toggle: Settings → "বাংলা উচ্চারণ দেখাও".

### 🔠 Per-Script Text Size Controls (every page) — NEW
- Settings → **"লেখার সাইজ (সব পেজ)"**: independent sliders for **আরবি / বাংলা / English** (70%–180%) with a live preview card.
- Wired app-wide via `CompositionLocal` (`ReadingScale`): Arabic, Bangla and English reading text now scales on **18 screens** — Quran reader, tafsir (full + sheet), word-by-word, thematic topic study, hadith (detail/list/topics/search/topic-study), duas (list/detail), kalima, namaz shikkha + surah fullscreen, misconceptions, Q&A, stories, 99 names — on top of the existing reader A−/A+ master scale.

### 🐛 Thematic Quran expand bug — FIXED
- The whole ayah card used to be tap-to-toggle, so touching the tafsir text or any chip instantly collapsed it ("ক্লিক করলেই minimise হয়ে যায়"). Expand/collapse now triggers **only** on the header row and the chevron row.
- The decorative শোনো/সংরক্ষণ/শেয়ার chips are now **fully functional**: শোনো plays the ayah with the selected reciter, সংরক্ষণ toggles a real bookmark (filled icon + tinted state), শেয়ার shares the ayah with Arabic + Bangla + English.
- Expanded tafsir content now sits in a premium accent-tinted panel.

### 🎨 Quran AI button — REDESIGNED
- The unlabeled Psychology icon crammed in the ayah header is gone. Each ayah now has a labeled **gradient "AI তাফসীর" pill** (with tap-scale + haptics) plus a "শব্দে শব্দ" chip in a dedicated quick-action row — discoverable and thumb-friendly.

### ✨ Tafsir screen — premium pass
- Gradient ayah hero (Arabic in primary color + uccaron + translations), premium section headers for অনুবাদ/তাফসীর/AI sections, per-script font scaling across all content.

## [v5.4.0] - 2026-09-24

### 🐛 Critical Build Fixes (APK was failing to compile)
- **SettingsRepository** — missing `kotlinx.coroutines.flow.first` import broke whole-module compile (ayah-notes read).
- **TopicStudyRepository** — `getTopicDetail` referenced a non-existent `bundled` variable; API-enrichment path now correctly resolves through `bundledResult.topic`.
- **TranslationCacheService** — stray `")` inside `cacheSurah()` (syntax) and `isSurahCached` returned an Int from a Boolean function — now `(1..ayahCount).all { … }`.
- **QuranReaderViewModel** — stray `n` character in offline-cache block (syntax).
- **HomeScreen & PrayerScreen** — `toMinutes()` used `return` inside expression-body functions; converted to block bodies (next-prayer progress logic intact).
- **JuzListScreen** — two broken string literals in the Juz data table (`"1,123)`, `"1,52)`) plus missing `remember`/`clickable` imports.
- **QuranReaderScreen** — missing `AppColors` import (offline-download banner).
- **QuranSearchScreen** — missing `remember`/`height` imports cascading ~10 errors.
- **TafsirFullScreen** — missing `clickable` import (note-save control).

### 🎨 Premium UI Pass (all remaining screens)
- **MoreScreen** — staggered entrance across the 27-feature grid, springy premium tap on every grid card.
- **AppLockScreen** — staggered lock-icon/title/subtitle/retry entrance.
- **TrackerScreen** — `PremiumSectionHeader` section titles, spring-animated `PremiumProgressBar` for today's prayer progress, staggered prayer rows & stat cards, premium tap on prayer check cards.
- **CalendarScreen** — staggered hero & real day-cells entrance.
- **JuzListScreen** — staggered 30-para list, premium tap on para cards.
- **QuranSearchScreen** — staggered search results.
- **ScannerScreen / TajweedCheckerScreen** — staggered section entrances, premium section headers.
- **DuaDetailScreen** — staggered content cards, premium tap on AI chip.
- **HadithSearchScreen** — staggered results, premium tap on result cards.
- **NamazExtrasScreen** — staggered prayers/surah lists, premium section header, premium tap on surah & audio cards.
- **TafsirFullScreen** — staggered content blocks, premium tap on note-save (enabled-state preserved).

## [v5.3.1] - 2025-09-23

### 🐛 Critical Bug Fixes
- **Quran audio wrong-ayah bug (SURAH 23+)** — `AudioController.kt` & `AudioDownloadService.kt` had a hardcoded ayah-count table missing Surah 23 (Al-Muminun, 118 ayahs). Every surah from 23 onward resolved to the wrong global ayah index → wrong surah/ayah audio played, Khatam playback misaligned, and Surah 114 crashed out-of-bounds. Both files now share one verified `AYAH_COUNTS` constant (114 entries, sum = 6236).
- **Hijri calendar month navigation** — `CalendarViewModel.loadMonth()` ignored the month offset (always computed from today) and assumed fixed 29-day hijri months, so navigating months showed increasingly wrong dates. Now uses Aladhan `calendar/{year}/{month}` API for real per-day hijri dates (`PrayerRepository.getHijriMonthCalendar()`), with a labeled "আনুমানিক" offline fallback.
- **Bangla audio toggle honesty fix** — the Settings toggle silently did nothing (no Bangla audio CDN exists) and reset itself. It now shows an explicit "শীঘ্রই আসছে" note instead of pretending to work.

### 🆕 New Features
- **Thematic Quran keyword engine** — Topic Study grew from 7 hand-curated topics to **45 topics (7 curated + 38 keyword-driven)**. New `ThematicQuranEngine` scans the full bundled Quran (6,236 ayahs, offline) with Bangla/Arabic keyword scoring (+3/+2, mirrors the Hadith engine), sorted by relevance, capped at 400 ayahs/topic with memoized single-pass counts. `FULL_RESOURCE.md`'s "338+ topics" claim now honestly reflects reality.
- **Topic Study pagination + counts** — detail pages render engine results 30/page with a "মোট X আয়াত" badge, load-more card, and key-ayah (top-5) online enrichment.
- **Hadith topics 12 → 32** — 20 new themes (খাতমে নবুয়ত, তওবা, দোয়া, রিয়া, শুকর, জুমুআ, মসজিদ, সাহাবীগণ, রিবা, পিতা-মাতা, সন্তান তরবিয়ত, প্রতিবেশীর হক, মৃত্যুর স্মরণ, দুনিয়া ও আখিরাত ইত্যাদি) over the same 24,424-hadith keyword scoring.
- **Qada dashboard UI** — log-date picker (past dates, retroactive logging), দিন/সপ্তাহ/মাস/বছর tab dashboard with stacked missed-vs-completed bars, prayer filter chips, bottom-sheet breakdown, and hero color-shift (red→amber→green) as outstanding drops.
- **Fasting dashboard UI** — past-date fast logging via date picker, period history dashboard with type-colored stacked bars (Ramadan gold / Nafl blue / Qada red / Sunnah green), animated streak counters.
- **Khatam history section** — permanent khatam history timeline ("আমার খতম ইতিহাস"): total completed (count-up animation), average days, per-khatam cards with "রমজান গতি" badge, year-in-review bars, guarded history-clear dialog. `reset()` now preserves history; dedicated `clearHistory()` only wipes history explicitly.
- **Calendar Qada/Roza dot indicators** — day cells show red dots for qada entries and green dots for completed fasts, driven by live DataStore-backed tracker data for the displayed month.
- **Premium Animation Toolkit** (`ui/theme/PremiumAnimations.kt`) — shared reusable motion system: `staggerEntrance` (list/grid stagger fade+slide), `premiumTap` (scale-bounce + haptic), `PremiumCountUpText`, `premiumShimmer` skeleton, `premiumPulseHighlight` (playing-ayah glow), `PremiumProgressBar`, `premiumGlow`.

### 🎨 Premium UI Pass (priority screens)
- **QuranReaderScreen** — playing ayah pulses with a smooth highlight tint; play button glows while reciting.
- **HomeScreen** — greeting entrance animation, next-prayer progress bar (between prev/next prayer), staggered quick-access grid.
- **OnboardingScreen** — page-change re-entrance animation, springy premium tap on Next/Get-started.
- **QiblaScreen** — compass needle now rotates via spring physics with shortest-path wrap handling (no more jumpy 0°/360° spin-back).
- **PrayerScreen** — next-prayer progress card with animated progress bar, staggered prayer rows.

### 🔧 Architecture
- `ThematicQuranEngine` + `QuranTopicCatalog` (data/repo) — keyword-scoring engine & 38-topic catalog; `TopicSource.ENGINE` added.
- `KhatamViewModel` — first ViewModel for the khatam screen (combine of 5 DataStore flows into one UI state).
- `QadaRepository`/`FastingRepository` period-stat APIs + date-parameterized logging; `KhatamRepository` permanent history.

## [v5.3.0] - 2025-08-22

### 🆕 New Features
- **Multi Bangla Translation in Quran Reader** — Switch between 4 Bangla translators (মুহিউদ্দীন খান, তাইসিরুল কুরআন, ড. যাকারিয়া, রাওয়ায়ে বায়ান) via Quran.com API. Offline fallback to bundled translation.
- **Multi Tafsir (5 Bangla + 1 English)** — In TafsirFullScreen: ইবনে কাসীর, আবু বকর যাকারিয়া, আহসানুল বায়ান, ফাতহুল মজীদ + Ibn Kathir (English)
- **Multi Translation in Tafsir (5 total)** — Same 4 Bangla + T. Usmani English translation in tafsir view
- **Premium Surah Card** — Complete redesign per GLM Complete Plan: surah number badge, Arabic/English/Bangla names, Meccan/Medani tag, ayah count, 4-translation badge, Juz number, Khatam progress bar, Play + Open buttons
- **Offline Translation Download** — Download full surah translations + tafsirs for offline reading via TranslationCacheService
- **Dual Search** — Toggle between অফলাইন (local asset) and Quran.com API search with auto-fallback
- **Ayah Notes** — Write and save personal notes per ayah, persisted in DataStore
- **Copy & Share** buttons in Tafsir screen
- **Juz/Para List Screen** — Browse all 30 Juz with surah ranges
- **TranslationCacheService** — Persistent offline cache for Quran.com API data
- **DesignSystem Integration** — AppColors, AppSpacing, AppRadius, AppElevation tokens now used in HomeScreen, QuranReaderScreen
- **Khatam Progress** — Shows per-surah reading progress in surah list cards

### 🐛 Bug Fixes
- Fixed v5.0.0 fake upgrade: only added LazyList `key=` params, not actual features
- Fixed v4.3.1 orphaned DesignSystem.kt that was never imported
- Fixed v5.1.0: QuranReaderScreen now actually uses QuranComApi multi-translation data
- Fixed v5.2.0: Tafsir tafsir selectors now show all available tafsirs

### 🔧 Architecture
- `TranslationCacheService` — New service for offline Quran data caching
- `TranslationOption` / `TafsirOption` unified data classes (replacing `BanglaTranslationOption` / `BanglaTafsirOption`)
- `SearchSource` enum for offline vs API search mode
- `JuzData` — Static Juz mapping data
- QuranReaderViewModel: `downloadForOffline()` + `loadOnlineTranslations()` + `selectTranslation()`
- QuranListViewModel: Khatam progress integration + `playSurah()`
- SettingsRepository: `getAyahNote()` / `setAyahNote()` for per-ayah notes

## [v5.2.0] - Previous Release

- Multi-translation + multi-tafsir UI in TafsirFullScreen (partially)
- QuranComApi expanded with 4 Bangla translations + 4 Bangla tafsirs

## [v5.1.0] - Previous Release
- Quran.com API: multiple Bangla translations + Bangla tafsirs (API only, not integrated in reader)

## [v5.0.0] - Previous Release
- Version bump only (no actual features)

## [v4.4.0] - Previous Release
- AudioController: `playAssetAudio()` for namaz step audio via shared ExoPlayer
- NamazShikkhaScreen, NamazExtrasScreen, PrayerScreen: audio routed through AudioController

## [v4.3.1] - Previous Release
- DesignSystem.kt created (but not used)
- Phase 1-4 audit

## [v4.3.0] - Previous Release
- Premium UI polish: micro-interactions, shadows, gradients

## [v4.2.0] - Previous Release
- Switched from Islamic.app API to Quran.com API

## [v4.1.0] - Previous Release
- Floating player on all screens + Gemini 2.5 only + Tasbih layout

## [v4.0.0] - Previous Release
- Floating player upgrade + API fix + Stories crash fix + AI icon + model presets

## [v3.9.0] - Previous Release
- 6 Kalima full details + Namaz Shikkha fullscreen popup + Extra Namaz surah popup

## [v3.8.0] - Previous Release
- Prayer minimal cards + Quran AI fullscreen + Theme modes + audio cleanup

## [v3.7.0] - Previous Release
- More 3-col grid + Khatam para-wise + Prayer/Tasbih/Tracker/Dua/Calendar premium redesign

## [v3.6.0] - Previous Release
- Profile + Firebase + Bookmarks + Hadith Topics premium card grid

## [v3.5.0] - Previous Release
- Quran 4-button ayah row + Hadith crash fix + premium hadith UI

## [v3.4.0] - Previous Release
- AI system overhaul: cache, premium UI, model presets, fix Quran AI

## [v3.3.0] - Previous Release
- API-driven Quran topics + Hadith Topic Study premium UI

## [v3.2.0] - Previous Release
- Thematic Quran Study: verified topic-based ayah grouping + tafsir

## [v3.1.0] - Previous Release
- Zakat Calculator + Islamic Quiz + Fasting Tracker + Premium Home v4

## [v3.0.0] - Previous Release
- Hadith Topics (31 categories) + Premium Home redesign v3
## [v2.0.0–v2.9.0] - Previous Releases
- Core features: Quran, Prayer Times, Qibla, Tasbih, 99 Names, Duas, Hijri Calendar, Hadith, AI Scholar, Tajweed, Scanner, Stories, Kalima, Q&A, Khatam, Profile, Settings, Bookmarks, Qada, Tracker