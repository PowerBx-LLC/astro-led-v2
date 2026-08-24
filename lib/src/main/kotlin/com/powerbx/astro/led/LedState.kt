package com.powerbx.astro.led

data class LedState(
    val power: Power,
    val color: Color? = null,
    val effect: Effect? = null,
    val lastError: String? = null
)
