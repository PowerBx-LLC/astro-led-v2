# ASTRO LED Library - Consumer ProGuard rules

-keep class com.powerbx.astro.led.AstroLed { *; }
-keep class com.powerbx.astro.led.Color { *; }
-keep class com.powerbx.astro.led.Effect { *; }
-keep class com.powerbx.astro.led.Power { *; }
-keep class com.powerbx.astro.led.LedState { *; }
-keep class com.powerbx.astro.led.Result { *; }
-keep class com.powerbx.astro.led.Result$* { *; }
-keep class com.powerbx.astro.led.LegacyLedController { *; }
-keep class com.powerbx.astro.led.LegacyLedController$* { *; }

-keepclassmembers enum com.powerbx.astro.led.* {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
