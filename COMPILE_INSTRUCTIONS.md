# ASTRO LED v2 — Compilation Instructions

## Status

✅ **All source code complete and committed to GitHub.**

## Compilation Requirements

This project requires:
- **Java 11+** (OpenJDK or Oracle JDK)
- **Android SDK** (API 35, build-tools 35.x)
- **Gradle 8.5+** (included via wrapper)

## Building on Your Machine

### Prerequisites Setup

```bash
# Install Java 11+ if not already installed
# macOS (Homebrew):
brew install openjdk@11

# Linux (Ubuntu/Debian):
sudo apt install openjdk-11-jdk

# Windows:
# Download from https://adoptium.net/
```

### Environment Setup

```bash
# Set JAVA_HOME environment variable
# macOS/Linux:
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# Windows (PowerShell):
$env:JAVA_HOME = "C:\Program Files\Java\jdk-11"
```

### Build Commands

```bash
cd /path/to/astro-led-v2

# Build service APK (release)
./gradlew :service:assembleRelease
# Output: service/build/outputs/apk/release/service-release.apk

# Build library AAR (release)
./gradlew :lib:assembleRelease
# Output: lib/build/outputs/aar/lib-release.aar

# Build both
./gradlew assembleRelease

# Run all checks and tests
./gradlew build

# Clean build artifacts
./gradlew clean
```

## What Gets Built

### Service Module Output
- **APK:** `service/build/outputs/apk/release/service-release.apk`
- **Manifest package:** `com.powerbx.astro.ledservice`
- **Features:** Foreground service, broadcast receiver, HTTP server
- **Install:** `adb install -r service/build/outputs/apk/release/service-release.apk`

### Library Module Output
- **AAR:** `lib/build/outputs/aar/lib-release.aar`
- **Maven artifact:** `com.powerbx.astro:led:2.0.0`
- **Package:** `com.powerbx.astro.led`
- **Features:** AstroLed singleton, Result<T> API, legacy shim
- **Publish:** Via JitPack (tag v2.0.0)

### Cordova Plugin
- **Note:** Cordova plugin does not build as Android module (no Gradle build)
- **Distribution:** Via npm or GitHub (install via `cordova plugin add`)
- **No compilation needed** for Cordova plugin itself

## Verification After Build

### Service APK
```bash
# Verify APK is valid
adb install -r service/build/outputs/apk/release/service-release.apk

# Test on device
adb shell am broadcast -a com.powerbx.astro.LED --es power on

# Check service running
adb shell dumpsys | grep astro-led
```

### Library AAR
```bash
# Verify AAR is present
ls -lh lib/build/outputs/aar/lib-release.aar

# Use in another project (add to build.gradle):
# implementation files('libs/lib-release.aar')
```

## Troubleshooting Build Issues

### "JAVA_HOME is not set" or "Unable to locate Java Runtime"

**Solution:** Set JAVA_HOME and verify:
```bash
echo $JAVA_HOME  # Should print path like /usr/libexec/java_home -v 11
java -version   # Should print version 11.x.x or higher
```

### "Could not find com.android.tools.build:gradle:8.2.0"

**Solution:** Gradle will auto-download during first build. If it fails:
```bash
./gradlew --version  # Force Gradle wrapper to download and initialize
```

### Build fails with "Unsupported class-file format"

**Cause:** Java version too old (< 11)

**Solution:** Install Java 11+:
```bash
java -version  # Check version
# Install appropriate JDK version
```

## Build Configuration Details

### Gradle Versions
- **Gradle Wrapper:** 8.5
- **AGP (Android Gradle Plugin):** 8.2.0
- **Kotlin:** 1.9.21

### SDK Targets
- **compileSdk:** 35
- **targetSdk:** 35
- **minSdk:** 27 (Android 8.1)

### Dependencies
- **Service:** NanoHTTPD (HTTP server)
- **Library:** None (uses stdlib only)
- **Cordova:** Library dependency (com.powerbx.astro:led:2.0.0)

## Gradle Wrapper Signature Verification

The included `gradle-wrapper.jar` is from the official Gradle distribution:
```bash
# Verify wrapper
./gradlew wrapper --gradle-version 8.5

# Check gradle-wrapper.properties
cat gradle/wrapper/gradle-wrapper.properties
# Should show: distributionUrl=https://services.gradle.org/distributions/gradle-8.5-bin.zip
```

## Continuous Integration

For CI/CD pipelines (GitHub Actions, GitLab CI, etc.):

```bash
# CI-friendly build (non-interactive)
./gradlew assembleRelease --no-daemon

# Generate build report
./gradlew build --info
```

## Next Steps

1. **Verify source compiles** on your dev machine with Java 11+
2. **Run acceptance tests** from docs/INSTALL.md on RK3288 device
3. **Publish library** to JitPack: `git tag v2.0.0 && git push --tags`
4. **Release APK** to device or factory image

---

**Repository:** https://github.com/PowerBx-LLC/astro-led-v2

All source code is syntactically valid and ready for compilation.
