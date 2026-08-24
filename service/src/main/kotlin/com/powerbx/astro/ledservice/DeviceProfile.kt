package com.powerbx.astro.ledservice

/**
 * Device profile for RK3288 ASTRO LED controller.
 * Defines sysfs path, command codes, and color codes per Section 1 spec.
 */
object DeviceProfile {
    const val DEVICE_NAME = "RK3288_ASTRO"
    const val SYSFS_PATH = "/sys/devices/platform/led_con_h/zigbee_reset"
    const val REQUIRES_ROOT = true

    /**
     * Command codes (sysfs write format: "echo w 0x{hex} > {SYSFS_PATH}")
     */
    object Commands {
        const val BRIGHTNESS_DOWN = 0x00
        const val BRIGHTNESS_UP = 0x01
        const val OFF = 0x02
        const val ON = 0x03
        const val FLASH = 0x0b
        const val STROBE = 0x0f
        const val FADE = 0x13
        const val SMOOTH = 0x17
    }

    /**
     * Color codes (sysfs write format: "echo w 0x{hex} > {SYSFS_PATH}")
     */
    object Colors {
        const val RED = 0x04
        const val GREEN = 0x05
        const val BLUE = 0x06
        const val WHITE = 0x07
        const val RED_ORANGE = 0x08
        const val MINT = 0x09
        const val PURPLE = 0x0a
        const val ORANGE = 0x0c
        const val TURQUOISE = 0x0d
        const val PURPLE_PINK = 0x0e
        const val ORANGE_YELLOW = 0x10
        const val LIGHT_BLUE = 0x11
        const val PINK = 0x12
        const val YELLOW = 0x14
        const val TEAL = 0x15
        const val MAGENTA = 0x16
    }

    /**
     * RK3566_ASTRO profile (pending factory response)
     * TODO: Add sysfs path, command codes, and color codes when available
     */
    object RK3566Stub {
        const val STATUS = "PENDING_FACTORY_RESPONSE"
    }
}
