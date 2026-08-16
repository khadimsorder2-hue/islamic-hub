# Islamic Hub — Native Android (Kotlin + Jetpack Compose)

Bangla-first reference implementation of the Islamic Hub app — built natively for Android
with Jetpack Compose + Material 3, with no WebView / Capacitor / RN wrapper.

## v1.1.0 Features
- ✅ **Full Quran (114 surahs, 6236 ayahs)** with Arabic (Uthmani) + Bengali (Muhiuddin Khan) + English (Sahih International) text — bundled as JSON asset (4.5 MB)
- ✅ **Audio tilawat** via Media3 ExoPlayer — 12 reciters, play full surah or per-ayah
- ✅ **Prayer notifications** — AlarmManager exact alarms, boot/timezone/time-change receivers, deterministic IDs, idempotent scheduling
- ✅ **Custom release keystore** — APK is signed with a stable key
- ✅ **Premium UI** — Islamic Violet (#6D45C7) + Warm Ivory (#FCFAF7) + Muted Gold (#C9A34E)
- ✅ **Quran full-text search** — across all 6236 ayahs

## Tech stack
- **Language:** Kotlin 1.9.24
- **UI:** Jetpack Compose (BOM 2024.06.00) + Material 3
- **Min SDK:** 24 (Android 7.0+) / Target SDK 34
- **Architecture:** MVVM + StateFlow (UDF) — manual DI via AppContainer
- **Persistence:** DataStore Preferences
- **Audio:** Media3 ExoPlayer 1.3.1
- **Networking:** Retrofit + OkHttp + Gson (Aladhan API for prayer times, AlQuran.cloud CDN for audio)
- **Location:** FusedLocationProviderClient
- **Sensors:** Rotation vector sensor for Qibla compass
- **Notifications:** AlarmManager + BroadcastReceiver + boot receiver

## Premium design system
Per MD plan §18 (Warm Ivory + Islamic Violet + Muted Gold):
- `primary`: #6D45C7 (Islamic Violet)
- `primaryContainer`: #F1EBFA
- `secondary`: #C9A34E (Muted Gold)
- `background`: #FCFAF7 (Warm Ivory)
- `surface`: #FFFFFF
- Gradient hero cards (vertical gradient on prayer hero, linear gradient on icon tiles)
- Rounded corners (18-28dp radius)
- Premium typography hierarchy (display, headline, title, body, label)

## Features in detail
1. **Home** — Hijri date, next-prayer countdown (gradient hero), ayah/hadith of the day, quick-action cards
2. **Al-Quran** — All 114 surahs with full Arabic + Bengali + English text, search-across-ayahs, audio tilawat (play full surah or per-ayah)
3. **Prayer Times** — Aladhan API + FusedLocation, prayer notification alarms (boot/timezone/time-change aware)
4. **Qibla** — Live compass pointing to Kaaba using rotation-vector sensor
5. **Tasbih** — Tap counter with 5 dhikr presets, round + total tracking
6. **99 Names of Allah** — Full Asma ul Husna with Arabic, transliteration, English + Bengali
7. **Duas** — 8 daily duas with Arabic, transliteration, English + Bengali translation
8. **Hijri Calendar** — Month view with today highlighted

## Concurrency architecture (per MD plan §9, §10, §11-15)
- Immutable UI state (data classes only)
- Single-writer repositories (QuranRepository, PrayerRepository, TasbihRepository, AudioController)
- Idempotent alarm scheduling (cancel-then-set with deterministic IDs)
- Atomic schedule lock (AtomicLong CAS) to prevent concurrent reschedules
- Single ExoPlayer instance per app (never per-screen)
- Single NotificationChannel created once in AppContainer

## Build locally
```bash
./gradlew assembleDebug      # debug APK
./gradlew assembleRelease    # release APK (signed with committed keystore)
```

## CI build
GitHub Actions workflow at `.github/workflows/build-apk.yml`:
- Builds release APK on every push to `main`
- Builds + creates GitHub Release on every `v*` tag
- APK is signed with the committed `keystore/islamichub-release.keystore`
- For CI override, set env vars: `KEYSTORE_BASE64`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

## Replace keystore for production
1. Generate your own keystore (see `keystore/README.md`)
2. Replace `keystore/islamichub-release.keystore` with your file
3. Update `keystore/keystore.properties` with your credentials
4. Or set GitHub Actions secrets (preferred for private forks)

## Data sources & attributions
- **Quran text:** AlQuran.cloud (Tanzil Uthmani Arabic, Sahih International English, Muhiuddin Khan Bengali) — see `app/src/main/assets/quran/NOTICE.txt`
- **Prayer times:** Aladhan API (https://aladhan.com)
- **Audio tilawat:** AlQuran.cloud CDN (https://cdn.islamic.network/quran/audio/)

## License
Reference source code is MIT. Bundled Quran text, Asma ul Husna, and dua content are
public-domain Islamic references. Audio streams are served from AlQuran.cloud CDN under
their respective usage terms.
