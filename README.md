# Lịch Âm - Vietnamese Lunar Calendar

A lightweight offline Android app for Vietnamese lunar calendar.

## Features

- Monthly calendar with solar and lunar dates
- Highlight today, full moon (15th), and first lunar day (1st)
- Can Chi (干支) day/month/year calculation
- Good/bad day assessment with scoring
- Hoàng Đạo (good) and Hắc Đạo (bad) hours
- Old Vietnamese quotes and proverbs
- No internet, no ads, no tracking

## Build

### Local build

Open the project in Android Studio and run, or build from command line:

```bash
# Generate Gradle wrapper (one-time setup)
gradle wrapper --gradle-version 8.5

# Build debug APK
./gradlew clean assembleDebug

# Build release APK
./gradlew clean assembleRelease -PversionCode=1 -PversionName=1.1
```

### GitHub Actions CI

The project includes two CI workflows:

- `Android CI` — builds debug APK on push, or signed release APK manually
- `Generate Android Keystore` — generates signing keystore (run once only)

## How to create signing key

Follow these steps exactly once to enable signed release builds.

### Step 1: Generate keystore

1. Go to your GitHub repository → **Actions** tab.
2. Select **Generate Android Keystore** workflow.
3. Click **Run workflow** → **Run workflow**.
4. Wait for the workflow to finish.
5. Download the `release-keystore` artifact.
6. Extract `release.keystore` from the downloaded zip.

### Step 2: Convert keystore to Base64

On Linux/macOS:
```bash
base64 -w0 /path/to/release.keystore > keystore_base64.txt
```

On Windows (PowerShell):
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) > keystore_base64.txt
```

### Step 3: Add GitHub Secrets

Go to **Settings** → **Secrets and variables** → **Actions** and add:

| Secret | Value |
|--------|-------|
| `ANDROID_KEYSTORE_BASE64` | Content of `keystore_base64.txt` |
| `ANDROID_KEYSTORE_PASSWORD` | `android` |
| `ANDROID_KEY_ALIAS` | `lunarcalendar` |
| `ANDROID_KEY_PASSWORD` | `android` |

### Step 4: Build signed release APK

1. Go to **Actions** → **Android CI** → **Run workflow**.
2. Select branch `main`, build type `release`.
3. Click **Run workflow**.
4. Download the signed APK from artifacts.

> **IMPORTANT:**
> - Run the keystore generation workflow **ONLY ONCE**.
> - Keep the `release.keystore` file in a safe place (backup it).
> - If the keystore is lost or a different keystore is used, existing users **cannot update** the app.
> - The `com.vietnamese.lunarcalendar` applicationId must never change after the first signed release.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material3)
- **minSdk:** 23
- **targetSdk:** 34
- **Architecture:** Minimal, no-viewmodel, simple state-based navigation
- **Storage:** JSON files in assets

## License

MIT
