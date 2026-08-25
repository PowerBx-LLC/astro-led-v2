# ASTRO LED v2 Hardware Protocol

Low-level protocol documentation for direct sysfs access or advanced integration.

## sysfs Interface

### Path

```
/sys/devices/platform/led_con_h/zigbee_reset
```

### Write Format

All commands are written as hex bytes (with 0x prefix):

```bash
echo w 0xHH > /sys/devices/platform/led_con_h/zigbee_reset
```

### Write Order (Critical)

Always write in this sequence with 200ms delays between writes:

1. **Color** (if changing)
2. **Effect** (if changing)
3. **Power** (only if state is changing: OFF→ON or ON→OFF)
4. **Brightness** (if changing)

**Important:** Do NOT write power after changing color/effect if the LED is already in the desired power state. The driver will re-commit the previous color and override the new one.

## Command Codes

All codes from `DeviceProfile.kt` (RK3288).

### Brightness & Power States

| Command | Hex Code |
|---------|----------|
| BRIGHTNESS_DOWN | 0x00 |
| BRIGHTNESS_UP | 0x01 |
| OFF | 0x02 |
| ON | 0x03 |

### Colors (16 total)

| Color | Hex Code | Color | Hex Code |
|-------|----------|-------|----------|
| RED | 0x04 | GREEN | 0x05 |
| BLUE | 0x06 | WHITE | 0x07 |
| RED_ORANGE | 0x08 | MINT | 0x09 |
| PURPLE | 0x0A | ORANGE | 0x0C |
| TURQUOISE | 0x0D | PURPLE_PINK | 0x0E |
| ORANGE_YELLOW | 0x10 | LIGHT_BLUE | 0x11 |
| PINK | 0x12 | YELLOW | 0x14 |
| TEAL | 0x15 | MAGENTA | 0x16 |

### Effects

| Effect | Hex Code |
|--------|----------|
| FLASH | 0x0B |
| STROBE | 0x0F |
| FADE | 0x13 |
| SMOOTH | 0x17 |



## Examples

### Set RED, then turn ON

```bash
echo w 0x04 > /sys/devices/platform/led_con_h/zigbee_reset  # RED
sleep 0.2
echo w 0x03 > /sys/devices/platform/led_con_h/zigbee_reset  # ON
```

### Change to BLUE (LED already ON)

```bash
echo w 0x06 > /sys/devices/platform/led_con_h/zigbee_reset  # BLUE
sleep 0.2
# Do NOT write 0x03 (ON) — it would revert to RED
```

### Flash effect

```bash
echo w 0x04 > /sys/devices/platform/led_con_h/zigbee_reset  # RED
sleep 0.2
echo w 0x0B > /sys/devices/platform/led_con_h/zigbee_reset  # FLASH
sleep 0.2
echo w 0x03 > /sys/devices/platform/led_con_h/zigbee_reset  # ON
```

### Brightness up

```bash
echo w 0x01 > /sys/devices/platform/led_con_h/zigbee_reset  # UP
```

## Device Profile (RK3288)

- **Chipset:** RK3288
- **Kernel Driver:** led_con_h
- **Interface:** sysfs character device
- **Write Method:** echo command with 0x prefix
- **Delay Requirement:** 200ms between writes

## Notes

- All color and effect codes are sourced from `service/src/main/kotlin/com/powerbx/astro/ledservice/DeviceProfile.kt`
- The service layer (astro-led-service) abstracts this protocol and adds 200ms delays automatically
- Direct sysfs access requires root privileges
