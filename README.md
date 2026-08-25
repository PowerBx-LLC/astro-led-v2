# ASTRO LED v2

Service-based LED control system for ASTRO perimeter devices (RK3288; RK3566 planned).

## Status

✅ **Verified on RK3288: on/off, 16 colors, effects via Intent**

## Overview

ASTRO LED v2 replaces the legacy binary-only `ledControl.aar` with a modular, open-source architecture:

- **astro-led-service** — System service that owns root and exposes LED control via Intent broadcasts and HTTP
- **astro-led-lib** — Optional typed Android library for zero-dependency integration
- **cordova-plugin-astro-led** — Updated Cordova plugin for cross-platform apps

Partners can integrate with **zero libraries** (direct HTTP/Intent) or use the optional typed library.

## Quick Start

### Zero-Library (HTTP)
```bash
curl -X POST http://127.0.0.1:8188/led \
  -d '{"power":"on","color":"RED","effect":"flash"}'
```

### Zero-Library (Broadcast)
```bash
adb shell am broadcast -a com.powerbx.astro.LED \
  --include-stopped-packages \
  --es power on --es color RED --es effect flash
```

### Typed Library (Kotlin)
```gradle
implementation 'com.powerbx.astro:led:2.0.0'
```

```kotlin
AstroLed.on(context)
AstroLed.setColor(context, Color.RED)
AstroLed.setEffect(context, Effect.FLASH)
```

### Cordova
```bash
cordova plugin add https://github.com/PowerBx-LLC/astro-led-v2.git#subdir=cordova-plugin
```

```javascript
window.LedController.sendCommand('on');
window.LedController.setColor('red');
```

## Modules

| Module | Purpose | Link |
|--------|---------|------|
| **service** | System service, sysfs writer, Intent/HTTP APIs | [README](service/README.md) |
| **lib** | Public Kotlin/Java API, HTTP client | [README](lib/README.md) |
| **cordova-plugin** | Cordova plugin wrapper | [README](cordova-plugin/README.md) |
| **docs** | Integration guides, protocols, deployment | [docs/](docs/) |

## Documentation

- **[docs/INTEGRATION.md](docs/INTEGRATION.md)** — Partner integration guide (all 3 paths)
- **[docs/PROTOCOL.md](docs/PROTOCOL.md)** — Sysfs protocol and command reference
- **[docs/INSTALL.md](docs/INSTALL.md)** — Deployment and verification

## Architecture

```
App (any language)
    ↓
Intent broadcast (with setPackage) OR HTTP POST to 127.0.0.1:8188/led
    ↓
astro-led-service (system app, root)
    ↓
SysfsWriter: echo w 0x{hex} > /sys/devices/platform/led_con_h/zigbee_reset
             (with 200ms delay between writes)
    ↓
RK3288 Kernel LED Driver
    ↓
GPIO/LED Hardware
```

**Execution order (critical):** color → effect → power → brightness

## Build

```bash
# Build all modules
./gradlew build

# Build individual modules
./gradlew :service:build
./gradlew :lib:build

# Publish lib to JitPack
git tag v2.0.0 && git push --tags
```

## Install

```bash
# Install service
adb install -r service/build/outputs/apk/release/service-release.apk

# First broadcast (before reboot)
adb shell am broadcast -a com.powerbx.astro.LED \
  --include-stopped-packages \
  --es power on

# After reboot, service auto-starts (no flag needed)
adb shell am broadcast -a com.powerbx.astro.LED --es power on

# Verify
adb forward tcp:8188 tcp:8188
curl http://127.0.0.1:8188/health
# {"ok":true,"profile":"RK3288_ASTRO"}
```

## Hardware Requirements

- ASTRO device with RK3288 chipset
- Android 8.1+ (API 27+)
- LED hardware (perimeter device) with sysfs driver loaded

## License

[To be specified]

## Related

- **Legacy library** (deprecated): [PowerBx-LLC/ASTRO_LED](https://github.com/PowerBx-LLC/ASTRO_LED) — v1.0, binary-only `.aar`
- **Legacy Cordova plugin** (deprecated): [PowerBx-LLC/Cordova-LED-Plugin](https://github.com/PowerBx-LLC/Cordova-LED-Plugin) — v1.x
