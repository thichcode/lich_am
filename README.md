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
./gradlew clean assembleRelease
```

### GitHub Actions CI

The project includes a CI workflow at `.github/workflows/android-build.yml`.

#### Automatic build (every push to main)

1. Push code to the `main` branch.
2. Go to your repository's **Actions** tab.
3. Select the **Android CI** workflow.
4. Download the APK from workflow artifacts.

#### Manual release build

1. On GitHub, go to **Actions** → **Android CI** → **Run workflow**.
2. Select the branch and choose **release** as build type.
3. Download the release APK from artifacts.

### Release signing (optional)

Add these secrets in your GitHub repository: **Settings** → **Secrets and variables** → **Actions**.

| Secret | Description |
|--------|-------------|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded keystore file |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias |
| `ANDROID_KEY_PASSWORD` | Key password |

To encode your keystore:

```bash
base64 -w0 /path/to/keystore.jks > keystore_base64.txt
# Copy the content of keystore_base64.txt into ANDROID_KEYSTORE_BASE64 secret
```

If signing secrets are not provided, the release APK will be built unsigned.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material3)
- **minSdk:** 23
- **targetSdk:** 34
- **Architecture:** Minimal, no-viewmodel, simple state-based navigation
- **Storage:** JSON files in assets

## License

MIT
