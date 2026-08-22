# Changelog

All notable changes to the Islamic Hub project.

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