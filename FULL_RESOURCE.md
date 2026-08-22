# Islamic Hub — Full Resource Guide

## Project Overview
**App Name:** Islamic Hub
**Package:** `com.islamichub.app`
**Min SDK:** 24 (Android 7.0)
**Target SDK:** 34
**Language:** Kotlin + Jetpack Compose + Material 3
**Architecture:** MVVM (Manual DI via AppContainer)
**Data:** Local JSON assets + Quran.com API v4 + Aladhan API
**Audio:** Media3 ExoPlayer
**Storage:** DataStore Preferences + File cache

---

## Features

### 📖 Quran (Al-Quran)
- **114 Surahs** — Full Arabic text with verified Uthmani script
- **4 Bangla Translations** (Quran.com API):
  1. মুহিউদ্দীন খান (Sheikh Mujibur Rahman, Darussalaam)
  2. তাইসিরুল কুরআন (Tawheed Publication)
  3. ড. আবু বকর মুহাম্মদ যাকারিয়া
  4. রাওয়ায়ে বায়ান (Bayaan Foundation)
- **1 English Translation**: T. Usmani
- **5 Bangla Tafsirs** (Quran.com API):
  1. তাফসীর ইবনে কাসীর
  2. তাফসীর আবু বকর যাকারিয়া
  3. তাফসীর আহসানুল বায়ান
  4. তাফসীর ফাতহুল মজীদ
- **1 English Tafsir**: Ibn Kathir (Abridged)
- **Transliteration** per ayah (Quran.com word-level data)
- **Word-by-Word** study with Arabic + transliteration + translation
- **Multiple Reciters** (15+ reciters with Arabic audio)
- **Bangla Audio** toggle (read translation after Arabic)
- **Font size** adjustment (70% to 200%)
- **Show/Hide** Arabic, Bangla, English independently
- **Bookmarks** per ayah
- **Notes** per ayah (persistent)
- **Copy & Share** ayah with all translations
- **Search** — Offline (local) + Quran.com API with Arabic/Bangla/English
- **Topic Study** — 338+ thematic ayah groupings
- **Juz/Para List** — 30 Juz with surah ranges
- **Khatam Tracker** — Full Quran reading progress
- **Offline Download** — Cache translations+tafsirs per surah
- **Surah Info** — Revelation type (Makki/Medani), ayah count, Juz number, meaning

### 🕌 Prayer Times
- 5 daily prayer times via Aladhan API
- Next prayer countdown
- Hijri date
- Qibla compass
- Jamat time tracking
- Prayer notification scheduling
- Auto-pause timer options (5/15/30/60 min)

### 📿 Tasbih
- Digital counter with 33/99/100/1000 targets
- Multiple dhikr options
- Round counter
- Auto-reset on target completion

### 🕌 99 Names of Allah
- All 99 Asma ul Husna with Arabic, transliteration, English & Bangla meanings

### 🤲 Duas
- 28 daily duas with Arabic, transliteration, translations

### 📚 Hadith
- 24,424+ hadiths
- 31 topic categories
- Full-text search
- Topic Study with 12+ themes
- Hadith detail with source chains


### 🧠 AI Features
- AI Scholar (Gemini/GPT) for Islamic Q&A
- AI Tafsir (village khotib style explanation)
- AI response caching for offline use
- Multiple AI providers & models
- Tajweed Checker
- Image Scanner (Islamic content analysis)

### 🔧 Tools
- Zakat Calculator (2.5% with Nisab)
- Islamic Quiz (6 categories)
- Fasting/Roza Tracker (streak counter)
- 6 Kalima (with detailed meanings)
- Misconceptions (192+)
- Q&A (887)
- Hijri Calendar

### 👤 User
- Profile with name
- App Lock (biometric)
- Backup & Restore
- Settings: theme, language, AI config, reciter, background mode

---

## Architecture

### DI Container
`AppContainer` — Manual DI, single instance in `IslamicHubApp`

No Hilt — keeps build simple and fast.

### Data Flow
```
UI (Compose) ← ViewModel (StateFlow) ← Repository ← DataSource (API/Local)
```
### Key Components

| Component | File | Purpose |
|----------|------|--------|
| AppContainer | `data/AppContainer.kt` | All repositories & services |
| QuranComApi | `data/remote/QuranComApi.kt` | Quran.com API v4 Retrofit |
| AladhanApi | `data/remote/AladhanApi.kt` | Prayer times API |
| AudioController | `data/repo/AudioController.kt` | Media3 ExoPlayer wrapper |
| TranslationCache | `data/repo/TranslationCacheService.kt` | Offline translation cache |
| DesignSystem | `ui/theme/DesignSystem.kt` | Centralized design tokens |
| FloatingAudioPlayer | `ui/components/FloatingAudioPlayer.kt` | Global audio player bar |

### Design System Tokens

| Token | Usage |
|-------|-------|
| `AppColors.brandPrimary` | Islamic Violet `#6D45C7` |
| `AppColors.brandSecondary` | Muted Gold `#C9A34E` |
| `AppColors.fajrAccent` through `ishaAccent` | Prayer time colors |
| `AppSpacing.xs` (4dp) through `xxxl` (32dp) | Spacing scale |
| `AppRadius.xs` (8dp) through `xxl` (28dp) | Corner radius scale |
| `AppElevation.low` (1dp) through `max` (8dp) | Card elevation |
| `AppTouchTargets.minimum` (48dp) | Material Design minimum |
| `AppDurations.fast` (150ms) | Animation speed |

---

## API Endpoints Used

### Quran.com API v4 (No auth required)
| Endpoint | Purpose |
|----------|---------|
| `GET /verses/by_key/{verse_key}` | Single verse with translations+tafsirs+words |
| `GET /verses/by_chapter/{chapter}` | All verses in a surah |
| `GET /verses/by_juz/{juz}` | All verses in a juz |
| `GET /chapters` | All 114 surah metadata |
| `GET /search?keyword=` | Full-text verse search |

### Translation IDs
| ID | Translator |
|----|-----------|
| 163 | Sheikh Mujibur Rahman (Bangla) |
| 161 | Taisirul Quran (Bangla) |
| 162 | Rawai Al-bayan (Bangla) |
| 213 | Dr. Abu Bakr Zakaria (Bangla) |
| 84 | T. Usmani (English) |

### Tafsir IDs
| ID | Tafsir |
|----|--------|
| 164 | Ibn Kathir (Bangla) |
| 165 | Ahsanul Bayaan (Bangla) |
| 166 | Abu Bakr Zakaria (Bangla) |
| 381 | Fathul Majid (Bangla) |
| 169 | Ibn Kathir Abridged (English) |

### Aladhan API
| Endpoint | Purpose |
|----------|---------|
| `GET /v1/timingsByCity` | Prayer times by city |
| `GET /v1/hijriCalendar` | Hijri date |

---

## File Structure

```
app/src/main/java/com/islamichub/app/
├── IslamicHubApp.kt
├── MainActivity.kt
├── data/
│   ├── AppContainer.kt          # DI container
│   ├── model/Models.kt       # Domain models
│   ├── remote/
│   │   ├── QuranComApi.kt     # Quran.com API
│   │   ├── AladhanApi.kt       # Prayer times API
│   │   └── IslamicAppApi.kt  # (legacy)
│   ├── local/                  # JSON asset loaders
│   └── repo/
│       ├── AudioController.kt      # Media3 player
│       ├── AudioDownloadService.kt # Audio offline
│       ├── TranslationCacheService.kt # Translation offline
│       ├── QuranRepository.kt    # Quran data
│       ├── TafsirRepository.kt   # Tafsir offline
│       ├── SettingsRepository.kt  # App settings
│       ├── BookmarkRepository.kt  # Bookmarks
│       ├── KhatamRepository.kt    # Khatam tracker
│       ├── ... (20+ repositories)
├── ui/
│   ├── theme/
│   │   ├── Color.kt, Theme.kt, Type.kt
│   │   └── DesignSystem.kt       # Design tokens
│   ├── components/              # Shared UI components
│   ├── navigation/
│   │   ├── Screen.kt              # Route definitions
│   │   └── IslamicHubNavGraph.kt
│   └── screens/
       ├── quran/    # Quran screens (6 files)
       ├── tafsir/   # Tafsir screens (3 files)
       ├── home/     # Home screen
       ├── hadith/   # Hadith screens (5 files)
       ├── prayer/   # Prayer screens
       ├── ... (25+ feature screens)
       └── settings/ # Settings screen
└── res/
    ├── values/   # English strings
    └── values-bn/ # Bangla strings
```

---

## Build & Run

```bash
# Clone
https://github.com/khadimsorder2-hue/islamic-hub

# Build
./gradlew assembleDebug

# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Requirements
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34
- Kotlin 1.9+
- Jetpack Compose BOM 2024.06.00

---

## License

- Quran.com content: Personal, non-commercial use only. See https://quran.com/terms-and-conditions
- Audio: Belongs to respective reciters/organizations
- App code: Open source

---

## Attribution

### Quran Translations
1. Sheikh Mujibur Rahman — Darussalaam
2. Taisirul Quran — Tawheed Publication
3. Dr. Abu Bakr Muhammad Zakaria
4. Rawai Al-bayan — Bayaan Foundation
5. T. Usmani — English

### Tafsirs
1. Tafseer Ibn Kathir (Bangla)
2. Tafsir Ahsanul Bayaan
3. Tafsir Abu Bakr Zakaria
4. Tafsir Fathul Majid
5. Ibn Kathir (Abridged, English)

### Audio Reciters
- Mishary Rashid Alafasy, Abdul Basit, Mahmoud Khalil Al-Husary, and 12 more
