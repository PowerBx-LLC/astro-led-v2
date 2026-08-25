# ASTRO LED v2 Hardware Protocol

Low-level protocol documentation for direct sysfs access or advanced integration.

## sysfs Interface

### Path

```
/sys/devices/platform/led_con_h/zigbee_reset
```

### Write Format

All commands are written as hex bytes (no "0x" prefix in the echo):

```bash
echo w 0xHH > /sys/devices/platform/led_con_h/zigbee_reset
```

Example:
```bash
echo w 0x01 > /sys/devices/platform/led_con_h/zigbee_reset  # RED color
echo w 0x03 > /sys/devices/platform/led_con_h/zigbee_reset  # ON
```

### Write Order (Critical)

Always write in this sequence with 200ms delays between writes:

1. **Color** (if changing)
2. **Effect** (if changing)
3. **Power** (only if state is changing: OFF→ON or ON→OFF)
4. **Brightness** (if changing)

**Important:** Do NOT write power after changing color/effect if the LED is already in the desired power state. The driver will re-commit the previous color and override the new one.

## Command Codes

### Power States

| Command | Hex Code | Description |
|---------|----------|-------------|
| OFF | 0x02 | Turn off |
| ON | 0x03 | Turn on |

### Colors (16 total)

| Color | Hex Code | Color | Hex Code |
|-------|----------|-------|----------|
| RED | 0x01 | GREEN | 0x02 |
| BLUE | 0x03 | WHITE | 0x04 |
| RED_ORANGE | 0x05 | MINT | 0x06 |
| PURPLE | 0x07 | ORANGE | 0x08 |
| TURQUOISE | 0x09 | PURPLE_PINK | 0x0A |
| ORANGE_YELLOW | 0x0B | LIGHT_BLUE | 0x0C |
| PINK | 0x0D | YELLOW | 0x0E |
| TEAL | 0x0F | MAGENTA | 0x10 |

### Effects

| Effect | Hex Code | Description |
|--------|----------|-------------|
| ON (none) | 0x03 | Solid (no effect) |
| FLASH | 0x04 | Rapid on/off |
| STROBE | 0x05 | Synchronized flashing |
| FADE | 0x06 | Smooth transition |
| SMOOTH | 0x07 | Continuous smoothing |

### Brightness

| Command | Hex Code | Description |
|---------|----------|-------------|
| DOWN | 0x00 | Decrease brightness |
| UP | 0x01 | Increase brightness |

## Examples

### Set RED, then turn ON

```bash
echo w 0x01 > /sys/devices/platform/led_con_h/zigbee_reset  # RED
sleep 0.2
echo w 0x03 > /sys/devices/platform/led_con_h/zigbee_reset  # ON
```

### Change to BLUE (LED already ON)

```bash
echo w 0x03 > /sys/devices/platform/led_con_h/zigbee_reset  # BLUE
sleep 0.2
# Do NOT write 0x03 (ON) — it would revert to RED
```

### Flash effect

```bash
echo w 0x01 > /sys/devices/platform/led_con_h/zigbee_reset  # RED
sleep 0.2
echo w 0x04 > /sys/devices/platform/led_con_h/zigbee_reset  # FLASH
sleep 0.2
echo w 0x03 > /sys/devices/platform/led_con_h/zigbee_reset  # ON
```

## Device Profile (RK3288)

- **Chipset:** RK3288
- **Kernel Driver:** led_con_h
- **Interface:** sysfs character device
- **Write Method:** echo command
- **Delay Requirement:** 200ms between writes

## Notes

- Color codes are standard across the LED driver
- Effect codes may vary depending on driver version
- The service layer (astro-led-service) abstracts this protocol and adds 200ms delays automatically
- Direct sysfs access requires root privileges
