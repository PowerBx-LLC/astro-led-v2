# ASTRO LED Service Installation Guide

Deployment instructions for astro-led-service on ASTRO devices.

## Quick Start (ADB Sideload)

**Prerequisite:** ASTRO device connected via USB with ADB enabled.

```bash
# 1. Build or obtain service APK
./gradlew :service:assembleRelease

# 2. Install on device
adb install -r service/build/outputs/apk/release/service-release.apk

# 3. First broadcast MUST use --include-stopped-packages (before reboot)
adb shell am broadcast -a com.powerbx.astro.LED \
  --include-stopped-packages \
  --es power on --es color RED

# 4. After reboot, service is started and flag not needed
adb shell am broadcast -a com.powerbx.astro.LED \
  --es power on --es color RED

# 5. Verify with HTTP
adb forward tcp:8188 tcp:8188
curl http://127.0.0.1:8188/health
# {"ok":true,"profile":"RK3288_ASTRO"}
```

Service starts automatically on device reboot. No additional configuration needed.

---

## Critical Note: First Broadcast After Sideload

On Android 8+, implicit broadcasts are blocked. The first broadcast **before reboot** must include `--include-stopped-packages`:

```bash
# REQUIRED before first reboot:
adb shell am broadcast -a com.powerbx.astro.LED \
  --include-stopped-packages \
  --es power on --es color RED

# After reboot, service is started automatically:
adb shell am broadcast -a com.powerbx.astro.LED \
  --es power on --es color RED  # --include-stopped-packages not needed
```

Code senders (Intent and library) must call `intent.setPackage("com.powerbx.astro.ledservice")`.

---

## Installation Methods

### Method 1: ADB Sideload (Recommended for Development)

Easiest method for testing and development.

```bash
adb install -r service/build/outputs/apk/release/service-release.apk
```

**Pros:**
- Fast, no factory integration required
- Works on any ASTRO device with ADB access
- Service starts on boot automatically

**Cons:**
- Requires manual install per device
- Not suitable for factory/OTA deployment

**Boot Autostart:** Enabled via `RECEIVE_BOOT_COMPLETED` receiver.

### Method 2: Factory Image Integration (Optional)

For factory/OTA deployment, add APK to system partition.

#### Step 1: Obtain Signed Service APK

```bash
./gradlew :service:buildRelease
# Output: service/build/outputs/apk/release/service-release.apk

# Sign with your key (or factory key)
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore my-release-key.jks \
  service/build/outputs/apk/release/service-release.apk \
  key_alias
```

#### Step 2: Add to System Partition

**Method A: priv-app (Recommended)**

```bash
mkdir -p system/priv-app/astro-led-service
cp astro-led-service-release.apk system/priv-app/astro-led-service/astro-led-service.apk
chmod 644 system/priv-app/astro-led-service/astro-led-service.apk
```

**Method B: app (Regular system app)**

```bash
mkdir -p system/app/astro-led-service
cp astro-led-service.apk system/app/astro-led-service/astro-led-service.apk
chmod 644 system/app/astro-led-service/astro-led-service.apk
```

#### Step 3: SELinux Policy (If Enabled)

If SELinux is enforcing, add policy for service.

#### Step 4: Flash New Factory Image

Standard factory flash procedure for your device.

---

## Verification Checklist

After installation, verify the service is working:

**Checklist:**
- [ ] APK installed: `adb shell pm list packages | grep astro-led`
- [ ] Service running: `adb shell dumpsys | grep astro-led`
- [ ] HTTP responding: `adb forward tcp:8188 tcp:8188` then `curl http://127.0.0.1:8188/health`
- [ ] Profile correct: Response should include `"profile":"RK3288_ASTRO"`
- [ ] LEDs respond: `adb shell am broadcast -a com.powerbx.astro.LED --es power on` (after reboot, no --include-stopped-packages flag)
- [ ] State persists: Reboot device, verify LEDs maintain state
- [ ] Logs clean: `adb logcat | grep astro-led` (no errors)

---

## Troubleshooting

### APK Installation Fails

**Error:** `INSTALL_FAILED_INVALID_APK`

- APK is corrupted. Re-download or rebuild.
- Android version incompatible. Check minSdk (27) and targetSdk (35).

```bash
./gradlew :service:build
adb install -r service/build/outputs/apk/release/service-release.apk
```

### Service Not Running After Install

**Check installation:**
```bash
adb shell pm list packages | grep astro-led
# Should show: package:com.powerbx.astro.ledservice
```

**Check service status:**
```bash
adb shell dumpsys | grep astro-led
# Should show foreground service running
```

**Start manually:**
```bash
adb shell am start-service com.powerbx.astro.ledservice/.LedService
```

### First Broadcast Fails

**Issue:** First broadcast before reboot shows no response.

**Solution:** Use `--include-stopped-packages` flag:
```bash
adb shell am broadcast -a com.powerbx.astro.LED \
  --include-stopped-packages \
  --es power on
```

After reboot, service is started and flag not needed.

### HTTP Port Unreachable

**Error:** `curl: (7) Failed to connect to 127.0.0.1:8188`

1. Service not running: Start manually (see above)
2. Port in use: Check if another app binds 8188
3. Wrong device: Verify you're testing on ASTRO

```bash
adb forward tcp:8188 tcp:8188
curl http://127.0.0.1:8188/health
```

### LEDs Not Responding

**Error:** `curl response: {"ok":false,"error":"ERR_ROOT_DENIED"}`

Service lost root. Check:
1. Device filesystem: `adb shell ls -la /sys/devices/platform/led_con_h/`
2. Permissions: Service needs root for sysfs writes
3. Reinstall: `adb uninstall com.powerbx.astro.ledservice` then `adb install ...`

### Boot Autostart Not Working

Service should start automatically on reboot.

**Check boot receiver:**
```bash
adb shell dumpsys | grep BootCompletedReceiver
```

**Verify manifest:**
- `RECEIVE_BOOT_COMPLETED` permission present
- Receiver configured and exported

**Force test:**
```bash
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
adb shell dumpsys | grep astro-led
# Should be running now
```

### Wrong Device Error

**Error:** `{"ok":false,"error":"ERR_UNSUPPORTED_DEVICE"}`

Device is not RK3288. Check:
```bash
adb shell getprop ro.product.model
# Should show ASTRO or similar

adb shell uname -r
# Should show RK3288-based kernel
```

**For RK3566:** Profile stub exists but not implemented. Wait for factory response.

---

## Uninstallation

```bash
adb uninstall com.powerbx.astro.ledservice
```

**For system apps (priv-app):**

Service persists with system ROM. Must be removed during factory image rebuild:
1. Delete from system/priv-app/astro-led-service in factory image
2. Rebuild and flash ROM

---

## Performance & Resource Usage

**APK Size:** ~500 KB (service + NanoHTTPD library)

**Memory Usage:** ~20–30 MB (foreground service)

**CPU Usage:** <1% (idle), <5% (during command)

**Boot Time Impact:** <500ms (quick service startup)

**Network:** Loopback only (127.0.0.1:8188); no external traffic

---

## Security Notes

**Permissions:**
- `RECEIVE_BOOT_COMPLETED` — Autostart on boot
- `INTERNET` — Loopback HTTP server
- `FOREGROUND_SERVICE` — Persistent foreground service
- `FOREGROUND_SERVICE_SPECIAL_USE` — (API 34+) Special use service type

**Open Interface:**
- Intent receiver: setPackage("com.powerbx.astro.ledservice") required
- HTTP server: Loopback only (127.0.0.1)
- LEDs: Not a security surface; acceptable for single-purpose device

**Root Access:**
- Service runs with app-level permissions
- Kernel driver provides root escalation for sysfs writes
- Uses: `su 0 sh -c "echo w 0x{hex} > /sys/devices/platform/led_con_h/zigbee_reset"`

---

## Support

For installation issues:
- Check device model: `adb shell getprop ro.product.model`
- Verify ADB: `adb devices`
- Review logs: `adb logcat -s astro-led`
- See [INTEGRATION.md](INTEGRATION.md) for post-install verification
