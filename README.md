# Islamic Hub — Native Android (Kotlin + Jetpack Compose)

Bangla-first reference implementation of the Islamic Hub app — built natively for Android
with Jetpack Compose + Material 3, with no WebView / Capacitor / RN wrapper.

## Tech stack
- **Language:** Kotlin 1.9.24
- **UI:** Jetpack Compose (BOM 2024.06.00) + Material 3
- **Min SDK:** 24 (Android 7.0+) / Target SDK 34
- **Architecture:** MVVM + StateFlow (UDF) — manual DI via AppContainer
- **Persistence:** DataStore Preferences (tasbih counts, settings)
- **Networking:** Retrofit + OkHttp + Gson (Aladhan API for prayer times)
- **Location:** FusedLocationProviderClient (Google Play Services)
- **Sensors:** Rotation vector sensor for Qibla compass

## Features
1. **Home** — Hijri date, next-prayer countdown, ayah/hadith of the day, quick-action cards
2. **Al-Quran** — All 114 surah names + 10 bundled full-text surahs (Al-Fatihah, Al-Asr,
   Al-Kawthar, An-Nasr, Al-Ikhlas, Al-Falaq, An-Nas, Al-Kafirun, Al-Masad, Al-Ma'un)
   with Arabic + Bengali + English
3. **Prayer Times** — Daily Fajr/Sunrise/Dhuhr/Asr/Maghrib/Isha via Aladhan API
   (auto-falls back to Makkah coordinates if location is unavailable)
4. **Qibla** — Live compass pointing to Kaaba using rotation-vector sensor
5. **Tasbih** — Tap counter with 5 dhikr presets, round + total tracking
6. **99 Names of Allah** — Full Asma ul Husna with Arabic, transliteration, English + Bengali
7. **Duas** — 8 daily duas with Arabic, transliteration, English + Bengali translation
8. **Hijri Calendar** — Month view with today highlighted

## Build locally
```bash
./gradlew assembleDebug      # debug APK
./gradlew assembleRelease    # release APK (signed with debug key)
```

## CI build
GitHub Actions workflow at `.github/workflows/build-apk.yml` builds the release APK on every
push to `main` and every tag `v*`. Tagging a release (`git tag v1.0.0 && git push --tags`)
creates a GitHub Release with the APK attached.

## License
Reference source code is MIT. Bundled Quran text, Asma ul Husna, and dua content are
public-domain Islamic references.
