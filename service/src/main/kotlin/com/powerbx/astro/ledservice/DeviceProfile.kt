package com.powerbx.astro.ledservice

/**
 * Device profile for RK3288-based ASTRO LED controller.
 * Maps command codes and color codes to sysfs operations.
 */
object DeviceProfile {
    const val DEVICE_NAME = "RK3288_ASTRO"
    const val SYSFS_PATH = "/sys/class/leds/rk3288-astro/brightness"

    // Command codes (0x00-0x0F)
    enum class CommandCode(val value: Int) {
        CMD_POWER_OFF(0x00),
        CMD_POWER_ON(0x01),
        CMD_EFFECT_STATIC(0x02),
        CMD_EFFECT_PULSE(0x03),
        CMD_EFFECT_STROBE(0x04),
        CMD_EFFECT_FADE(0x05),
        CMD_EFFECT_RAINBOW(0x06),
        CMD_BRIGHTNESS_10(0x07),
        CMD_BRIGHTNESS_25(0x08),
        CMD_BRIGHTNESS_50(0x09),
        CMD_BRIGHTNESS_75(0x0A),
        CMD_BRIGHTNESS_100(0x0B),
        CMD_SPEED_SLOW(0x0C),
        CMD_SPEED_NORMAL(0x0D),
        CMD_SPEED_FAST(0x0E),
        CMD_RESERVED(0x0F);

        companion object {
            fun fromValue(value: Int): CommandCode? =
                values().find { it.value == value }
        }
    }

    // Color codes (0x00-0x0F)
    enum class ColorCode(val value: Int, val name: String, val rgbHex: String) {
        COLOR_RED(0x00, "RED", "FF0000"),
        COLOR_GREEN(0x01, "GREEN", "00FF00"),
        COLOR_BLUE(0x02, "BLUE", "0000FF"),
        COLOR_YELLOW(0x03, "YELLOW", "FFFF00"),
        COLOR_CYAN(0x04, "CYAN", "00FFFF"),
        COLOR_MAGENTA(0x05, "MAGENTA", "FF00FF"),
        COLOR_WHITE(0x06, "WHITE", "FFFFFF"),
        COLOR_BLACK(0x07, "BLACK", "000000"),
        COLOR_ORANGE(0x08, "ORANGE", "FF8000"),
        COLOR_PURPLE(0x09, "PURPLE", "8000FF"),
        COLOR_PINK(0x0A, "PINK", "FF00FF"),
        COLOR_LIME(0x0B, "LIME", "00FF80"),
        COLOR_TEAL(0x0C, "TEAL", "00FFFF"),
        COLOR_NAVY(0x0D, "NAVY", "000080"),
        COLOR_SILVER(0x0E, "SILVER", "C0C0C0"),
        COLOR_GOLD(0x0F, "GOLD", "FFD700");

        companion object {
            fun fromValue(value: Int): ColorCode? =
                values().find { it.value == value }

            fun fromName(name: String): ColorCode? =
                values().find { it.name.equals(name, ignoreCase = true) }
        }
    }
}
