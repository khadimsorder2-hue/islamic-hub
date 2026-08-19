# Changelog

All notable changes to **Islamic Hub** are documented here.
This file is the source of truth for release notes — the GitHub Actions
workflow reads the section matching the tag and uses it as the release body.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## [v5.4.0] — 2026-08-20

### Fixed
- **Release notes were stuck on v1.2.0** for every release from v4.3 onward.
  The GitHub Actions workflow had a hardcoded `## What's new (v1.2.0)` block
  that was reused for every tag, so users could not see what actually changed
  between versions. The workflow now reads the matching section from this
  `CHANGELOG.md` and uses it as the release body.
- **APK rebuild verification** — v5.4.0 triggers a clean rebuild from the
  v5.3.0 source tree (which already contains the multi-Bangla translation UI
  in `QuranReaderScreen` and the `DesignSystem` token integration in
  `HomeScreen`). No source code changes were needed beyond the version bump
  and changelog/workflow fix.

### Changed
- `app/build.gradle.kts`: `versionCode` 40 → 41, `versionName` "5.3.0" → "5.4.0".
- `.github/workflows/build-apk.yml`: release `body` is now generated from
  `CHANGELOG.md` (extracts the section between `## [v5.4.0]` and the next
  `## [` heading).

---

## [v5.3.0] — 2026-08-19

### Added
- **Multi-Bangla translation in Quran reader** — `QuranReaderScreen` now
  fetches all 4 available Bangla translations from the Quran.com API per
  surah and shows a `FilterChip` selector:
  - মুহিউদ্দীন খান (Sheikh Mujibur Rahman)
  - তাইসিরুল কুরআন (Tawheed Publication)
  - ড. আবু বকর মুহাম্মদ যাকারিয়া
  - রাওয়ায়ে বয়ান
  - অফলাইন (fallback to bundled translation)
- **Loading indicator** while online translations are being fetched.
- **Share uses the selected translation text** — previously share always
  used the bundled translation regardless of which chip was active.

### Fixed
- **`DesignSystem.kt` was orphaned since v4.3.1** — the file was added in
  v4.3.1 but no screen imported it. v5.3.0 finally wires it up:
  - `HomeScreen` uses `AppColors`, `AppSpacing`, `AppRadius`, `AppElevation`,
    `AppFontSizes` tokens for hero card, chip grid, section spacing.
  - `QuranReaderScreen` uses `AppSpacing`, `AppRadius`, `AppIconSizes`.

### Added (QuranReaderViewModel)
- `loadOnlineTranslations()` — fetches all Bangla translations for the
  current surah in one coroutine call, stores them in
  `onlineTranslationsMap: Map<Int, List<String>>`.
- `selectTranslation(index)` — switches the active translation chip.
- `getBanglaTextForAyah(ayahNumber, fallback)` — returns the text from the
  selected translator, falling back to the bundled translation if online
  data is not loaded or the ayah is missing.

---

## [v5.2.0] — 2026-08-19

### Added (Tafsir multi-translation + multi-tafsir UI)
- **`TafsirViewModel.loadOnlineVerseData()`** — new method that calls
  `container.quranComApi.getVerseByKey("$surah:$ayah")` and fetches:
  - All 4 Bangla translations (163, 161, 213, 162)
  - All Bangla tafsirs available (164, 166)
  - Word-level transliteration for pronunciation
- New state fields in `TafsirUiState`:
  - `onlineBanglaTranslations: List<BanglaTranslationOption>`
  - `onlineBanglaTafsirs: List<BanglaTafsirOption>`
  - `transliteration: String?`
  - `selectedTranslationIndex: Int`
  - `selectedTafsirIndex: Int`
- `selectTranslation(index)` and `selectTafsir(index)` methods.

### Fixed (TafsirFullScreen — was not using v5.1 API data)
- Previous v5.1.0 commit added `getAllBanglaTranslations()` and
  `getAllBanglaTafsirs()` helpers to `VerseApi`, but `TafsirFullScreen`
  and `TafsirViewModel` never called them. Users saw no difference from
  v4.3 because the new data was never displayed. v5.2.0 fixes this.
- New UI sections in `TafsirFullScreen`:
  1. **Transliteration card** (🔊 উচ্চারণ) — verse pronunciation from
     Quran.com word-level data.
  2. **Online Bangla Translations** (📖 বাংলা অনুবাদ) — horizontal
     scrollable `FilterChip` selector, 4 translations, tap to switch.
     HTML tags stripped from text for clean display.
  3. **Online Bangla Tafsirs** (📚 বাংলা তাফসীর) — horizontal
     scrollable `FilterChip` selector, 2+ tafsirs, tap to switch.
     Premium `tertiaryContainer` card with icon.
  4. **Offline Tafsir** (renamed to 'অফলাইন') — existing bundled tafsir
     still works, now visually separated from online tafsirs.
  5. **AI Tafsir** — unchanged, village khotib style AI explanation.

---

## [v5.1.0] — 2026-08-19

### Added (Quran.com API expansion — data layer only, UI not yet wired)
- `QuranComApi.getVerseByKey()` now requests:
  - 3 Bangla translations: 163 (Mujibur Rahman), 161 (Taisirul Quran), 213 (Zakaria)
  - 1 English translation: 84 (T. Usmani)
  - 2 Bangla tafsirs: 164 (Ibn Kathir), 166 (Zakaria)
  - Word-level transliteration for pronunciation
- New data model `TafsirApi`: `id`, `text`, `language_name`, `resource_name`.
- `VerseApi` now has a `tafsirs` field (`List<TafsirApi>?`).
- New `VerseApi` helper methods:
  - `getBanglaTranslationMujib()`, `getBanglaTranslationTaisirul()`,
    `getBanglaTranslationZakaria()`, `getBanglaTranslationRawai()`,
    `getAllBanglaTranslations()` — returns `List<Pair<name, text>>`.
  - `getTafsirIbnKathirBn()`, `getTafsirZakariaBn()`,
    `getTafsirAhsanulBn()`, `getTafsirFathulMajidBn()`,
    `getAllBanglaTafsirs()`.
- `QuranComApi.Companion` now exposes constants for all 4 Bangla
  translation IDs, 1 English translation ID, 4 Bangla tafsir IDs,
  1 English tafsir ID, plus `DEFAULT_TRANSLATIONS` and `DEFAULT_TAFSIRS`.

### Note
These helpers were not yet called by any UI screen in v5.1.0 — that
wiring was done in v5.2.0 (Tafsir) and v5.3.0 (Quran reader).

---

## [v5.0.0] — 2026-08-18

### Fixed
- `HomeScreen`, `NamazShikkhaScreen`, `QuizScreen` — added missing
  `key = ...` parameters to `LazyColumn` / `LazyRow` items to prevent
  recomposition issues.

### Note
The commit message "All 18 phases complete — production-grade polish"
was aspirational; the actual diff was only 3 LazyList key parameters.
Real production polish landed in v5.2.0 and v5.3.0.

---

## [v4.4.0] — 2026-08-18

### Added
- **`AudioController`** — new central audio playback controller that all
  screens now use instead of each screen managing its own `ExoPlayer`.
- `FloatingPlayer` now appears on all screens (was previously only on
  Quran and Tafsir screens).
- `NamazShikkhaScreen`, `NamazExtrasScreen`, `PrayerScreen` — refactored
  to use `AudioController` and the shared floating player.

---

## [v4.3.1] — 2026-08-18

### Added
- **`DesignSystem.kt`** — new file with semantic design tokens:
  `AppColors`, `AppSpacing`, `AppRadius`, `AppElevation`,
  `AppFontSizes`, `AppIconSizes`, `AppTouchTargets`, `AppDurations`.
- Phase 1-4 design system audit.

### Fixed
- Security fix (specifics not documented in commit).

### Note
`DesignSystem.kt` was not actually imported by any screen until v5.3.0.

---

## [v4.3.0] — 2026-08-18

### Added
- Premium UI polish — micro-interactions, shadows, gradients across
  Home, Quran, Hadith, Prayer, Tasbih, Tracker, Dua, Calendar screens.

---

## Earlier versions

See `git log --oneline` for v1.0.0 through v4.2.0. Each version's commit
message describes the headline change. The release notes on GitHub for
these older versions incorrectly showed "What's new (v1.2.0)" for every
tag due to the hardcoded workflow body — this is fixed starting v5.4.0.
