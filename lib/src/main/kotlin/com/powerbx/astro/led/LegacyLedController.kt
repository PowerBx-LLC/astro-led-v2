package com.powerbx.astro.led

/**
 * @deprecated Use [AstroLed] instead. This class provides backward compatibility
 * with the legacy LED control API.
 */
@Deprecated(
    "Use AstroLed object instead",
    replaceWith = ReplaceWith("AstroLed")
)
object LegacyLedController {
    
    // Legacy color enum
    enum class COLORS {
        RED, GREEN, BLUE, WHITE, RED_ORANGE, MINT, PURPLE, ORANGE,
        TURQUOISE, PURPLE_PINK, ORANGE_YELLOW, LIGHT_BLUE, PINK, YELLOW, TEAL, MAGENTA
    }

    // Legacy command enum
    enum class COMMANDS {
        ON, OFF, FLASH, STROBE, FADE, SMOOTH
    }

    /**
     * @deprecated Use [AstroLed.on] instead
     */
    @Deprecated("Use AstroLed.on()", replaceWith = ReplaceWith("AstroLed.on()"))
    fun on(): Result<Unit> = AstroLed.on()

    /**
     * @deprecated Use [AstroLed.off] instead
     */
    @Deprecated("Use AstroLed.off()", replaceWith = ReplaceWith("AstroLed.off()"))
    fun off(): Result<Unit> = AstroLed.off()

    /**
     * @deprecated Use [AstroLed.setColor] instead
     */
    @Deprecated("Use AstroLed.setColor(Color)", replaceWith = ReplaceWith("AstroLed.setColor(mapColor(color))"))
    fun setLEDColor(color: COLORS): Result<Unit> {
        val newColor = mapLegacyColor(color)
        return AstroLed.setColor(newColor)
    }

    /**
     * @deprecated Use [AstroLed.setEffect] with [Effect.FLASH] instead
     */
    @Deprecated("Use AstroLed.setEffect(Effect.FLASH)", replaceWith = ReplaceWith("AstroLed.setEffect(Effect.FLASH)"))
    fun setFlash(): Result<Unit> = AstroLed.setEffect(Effect.FLASH)

    /**
     * @deprecated Use [AstroLed.setEffect] with [Effect.STROBE] instead
     */
    @Deprecated("Use AstroLed.setEffect(Effect.STROBE)", replaceWith = ReplaceWith("AstroLed.setEffect(Effect.STROBE)"))
    fun setStrobe(): Result<Unit> = AstroLed.setEffect(Effect.STROBE)

    /**
     * @deprecated Use [AstroLed.setEffect] with [Effect.FADE] instead
     */
    @Deprecated("Use AstroLed.setEffect(Effect.FADE)", replaceWith = ReplaceWith("AstroLed.setEffect(Effect.FADE)"))
    fun setFade(): Result<Unit> = AstroLed.setEffect(Effect.FADE)

    /**
     * @deprecated Use [AstroLed.setEffect] with [Effect.SMOOTH] instead
     */
    @Deprecated("Use AstroLed.setEffect(Effect.SMOOTH)", replaceWith = ReplaceWith("AstroLed.setEffect(Effect.SMOOTH)"))
    fun setSmooth(): Result<Unit> = AstroLed.setEffect(Effect.SMOOTH)

    /**
     * @deprecated Use [AstroLed.getState] instead
     */
    @Deprecated("Use AstroLed.getState()", replaceWith = ReplaceWith("AstroLed.getState()"))
    fun getLEDColor(): COLORS? {
        return AstroLed.getState()
            .getOrNull()
            ?.color
            ?.let { mapNewColorToLegacy(it) }
    }

    /**
     * @deprecated Use [AstroLed.getState] instead
     */
    @Deprecated("Use AstroLed.getState()", replaceWith = ReplaceWith("AstroLed.getState()"))
    fun getLEDState(): COMMANDS? {
        val state = AstroLed.getState().getOrNull() ?: return null
        return when {
            state.power == Power.OFF -> COMMANDS.OFF
            state.power == Power.ON && state.effect == Effect.FLASH -> COMMANDS.FLASH
            state.power == Power.ON && state.effect == Effect.STROBE -> COMMANDS.STROBE
            state.power == Power.ON && state.effect == Effect.FADE -> COMMANDS.FADE
            state.power == Power.ON && state.effect == Effect.SMOOTH -> COMMANDS.SMOOTH
            else -> COMMANDS.ON
        }
    }

    private fun mapLegacyColor(color: COLORS): Color = when (color) {
        COLORS.RED -> Color.RED
        COLORS.GREEN -> Color.GREEN
        COLORS.BLUE -> Color.BLUE
        COLORS.WHITE -> Color.WHITE
        COLORS.RED_ORANGE -> Color.RED_ORANGE
        COLORS.MINT -> Color.MINT
        COLORS.PURPLE -> Color.PURPLE
        COLORS.ORANGE -> Color.ORANGE
        COLORS.TURQUOISE -> Color.TURQUOISE
        COLORS.PURPLE_PINK -> Color.PURPLE_PINK
        COLORS.ORANGE_YELLOW -> Color.ORANGE_YELLOW
        COLORS.LIGHT_BLUE -> Color.LIGHT_BLUE
        COLORS.PINK -> Color.PINK
        COLORS.YELLOW -> Color.YELLOW
        COLORS.TEAL -> Color.TEAL
        COLORS.MAGENTA -> Color.MAGENTA
    }

    private fun mapNewColorToLegacy(color: Color): COLORS = when (color) {
        Color.RED -> COLORS.RED
        Color.GREEN -> COLORS.GREEN
        Color.BLUE -> COLORS.BLUE
        Color.WHITE -> COLORS.WHITE
        Color.RED_ORANGE -> COLORS.RED_ORANGE
        Color.MINT -> COLORS.MINT
        Color.PURPLE -> COLORS.PURPLE
        Color.ORANGE -> COLORS.ORANGE
        Color.TURQUOISE -> COLORS.TURQUOISE
        Color.PURPLE_PINK -> COLORS.PURPLE_PINK
        Color.ORANGE_YELLOW -> COLORS.ORANGE_YELLOW
        Color.LIGHT_BLUE -> COLORS.LIGHT_BLUE
        Color.PINK -> COLORS.PINK
        Color.YELLOW -> COLORS.YELLOW
        Color.TEAL -> COLORS.TEAL
        Color.MAGENTA -> COLORS.MAGENTA
    }
}
