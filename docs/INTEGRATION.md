# ASTRO LED v2 Integration Guide

Partner-facing guide for integrating LED control into your app or service.

## Overview

ASTRO LED v2 is a service-based LED control system for ASTRO perimeter devices (RK3288; RK3566 planned). You can integrate with **zero libraries** (direct HTTP or Intent), use the **optional typed library** for Kotlin/Java, or **integrate via Cordova** for cross-platform apps.

No sysfs access, no root in your app, no complex setup. Service handles all hardware communication.

## Quick Start (Choose Your Path)

### Path 1: Zero-Library (Simplest)

#### Kotlin/Java (Intent Broadcast)

**CRITICAL:** Intent senders MUST call `intent.setPackage("com.powerbx.astro.ledservice")` — implicit broadcasts are blocked on Android 8+.

```kotlin
val intent = Intent("com.powerbx.astro.LED").apply {
    putExtra("color", "RED")
    putExtra("power", "on")
    putExtra("effect", "flash")
    setPackage("com.powerbx.astro.ledservice")  // REQUIRED on Android 8+
}
context.sendBroadcast(intent)
```

Order of execution: color → effect → power → brightness (color must come before power).

#### HTTP POST (Any Language)

```bash
curl -X POST http://127.0.0.1:8188/led \
  -H "Content-Type: application/json" \
  -d '{"power":"on","color":"RED","effect":"flash"}'
```

#### ADB (Testing)

First broadcast after sideload (before reboot) needs `--include-stopped-packages`:
```bash
adb shell am broadcast -a com.powerbx.astro.LED \
  --include-stopped-packages \
  --es power on \
  --es color RED \
  --es effect flash
```

After reboot, package is started and flag not needed.

---

### Path 2: Typed Library (Recommended for Android)

#### Installation

```gradle
dependencies {
    implementation 'com.powerbx.astro:led:2.0.0'
}
```

#### Kotlin Example (5 lines)

```kotlin
if (AstroLed.isAvailable(context)) {
    AstroLed.setColor(context, Color.RED)
    AstroLed.setEffect(context, Effect.FLASH)
    AstroLed.on(context)
}
```

The library automatically calls `intent.setPackage()` for you.

#### Full API

```kotlin
AstroLed.isAvailable(context)                    // Boolean
AstroLed.on(context)                             // Result<Unit>
AstroLed.off(context)                            // Result<Unit>
AstroLed.setColor(context, Color.RED)            // Result<Unit>
AstroLed.setEffect(context, Effect.FLASH)        // Result<Unit>
AstroLed.brightnessUp(context)                   // Result<Unit>
AstroLed.brightnessDown(context)                 // Result<Unit>
AstroLed.getState(context)                       // Result<LedState>
```

See [lib/README.md](../lib/README.md) for complete documentation.

---

### Path 3: Cordova (Cross-Platform)

#### Installation

```bash
cordova plugin add https://github.com/PowerBx-LLC/astro-led-v2.git#subdir=cordova-plugin
```

#### JavaScript Example

```javascript
window.LedController.setColor('red');
window.LedController.sendCommand('flash');
```

The plugin automatically calls `intent.setPackage()` for you.

See [cordova-plugin/README.md](../cordova-plugin/README.md) for complete documentation.

---

## Command Reference

### Power States

| Command | Effect |
|---------|--------|
| `on` | Turn LEDs on |
| `off` | Turn LEDs off |

### Colors (16 available)

| Name | Name | Name | Name |
|------|------|------|------|
| RED | GREEN | BLUE | WHITE |
| RED_ORANGE | MINT | PURPLE | ORANGE |
| TURQUOISE | PURPLE_PINK | ORANGE_YELLOW | LIGHT_BLUE |
| PINK | YELLOW | TEAL | MAGENTA |

Case-insensitive; accepts both `LIGHT_BLUE` and `lightBlue`.

### Effects

| Command | Effect |
|---------|--------|
| `flash` | Rapid on/off |
| `strobe` | Synchronized flashing |
| `fade` | Smooth transition |
| `smooth` | Continuous smoothing |
| `none` | Disable effect |

### Brightness

| Command | Effect |
|---------|--------|
| `up` | Increase brightness |
| `down` | Decrease brightness |

---

## Execution Order (Critical)

When multiple commands are sent in one transaction:

1. **Color** (FIRST — driver ignores color after power ON)
2. **Effect**
3. **Power**
4. **Brightness**

Example Intent:
```kotlin
val intent = Intent("com.powerbx.astro.LED").apply {
    putExtra("color", "RED")      // 1st
    putExtra("effect", "flash")   // 2nd
    putExtra("power", "on")       // 3rd (after color)
    setPackage("com.powerbx.astro.ledservice")
}
context.sendBroadcast(intent)
```

---

## Error Codes

| Code | Meaning | Recoverable |
|------|---------|-------------|
| `ERR_ROOT_DENIED` | Service lost root | No |
| `ERR_NODE_MISSING` | sysfs path not found | No |
| `ERR_WRITE_FAILED` | Write to sysfs failed | Yes |
| `ERR_UNSUPPORTED_DEVICE` | Not an ASTRO device | No |
| `ERR_BAD_ARG` | Invalid color/effect | No |

---

## Device Detection

### Query Service

```bash
curl http://127.0.0.1:8188/health
# {"ok":true,"profile":"RK3288_ASTRO"}
```

### ADB Property

```bash
adb shell getprop ro.product.model
# Should contain "ASTRO" or similar
```

### Kotlin/Java

```kotlin
if (AstroLed.isAvailable(context)) {
    // Service is installed and responding
}
```

---

## Intent Broadcast Rules (Android 8+)

**Implicit broadcasts are blocked.** Always use:

```kotlin
intent.setPackage("com.powerbx.astro.ledservice")
```

This is required for all three Intent broadcast actions:
- `com.powerbx.astro.LED` (command)
- `com.powerbx.astro.LED_QUERY` (state query)
- Response: `com.powerbx.astro.LED_STATE` (received as broadcast from service)

The typed library and Cordova plugin handle this automatically.

---

## Deployment

Service installation:

```bash
adb install astro-led-service.apk
```

After sideload (before reboot):
```bash
adb shell am broadcast -a com.powerbx.astro.LED \
  --include-stopped-packages \
  --es power on --es color RED
```

After reboot, service is started and flag not needed.

For factory image integration, see [INSTALL.md](INSTALL.md).

---

## Support

- **Integration issues:** Check paths 1–3 above and error codes
- **Service issues:** See [service/README.md](../service/README.md)
- **Library API:** See [lib/README.md](../lib/README.md)
- **Cordova:** See [cordova-plugin/README.md](../cordova-plugin/README.md)
- **Protocol details:** See [PROTOCOL.md](PROTOCOL.md)
