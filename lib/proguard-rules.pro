# ASTRO LED Library ProGuard rules

# Keep all public API
-keep class com.powerbx.astro.led.AstroLed { *; }
-keep class com.powerbx.astro.led.Color { *; }
-keep class com.powerbx.astro.led.Effect { *; }
-keep class com.powerbx.astro.led.Power { *; }
-keep class com.powerbx.astro.led.LedState { *; }
-keep class com.powerbx.astro.led.Result { *; }
-keep class com.powerbx.astro.led.Result$* { *; }
-keep class com.powerbx.astro.led.LegacyLedController { *; }
-keep class com.powerbx.astro.led.LegacyLedController$* { *; }

# Keep enum names and values
-keepclassmembers enum com.powerbx.astro.led.* {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep data class constructors and toString
-keep class com.powerbx.astro.led.LedState {
    <init>(...);
    public ** component1();
    public ** component2();
    public ** component3();
    public ** component4();
    public ** copy(...);
    public java.lang.String toString();
    public int hashCode();
    public boolean equals(java.lang.Object);
}

# Don't warn about missing Android classes (library module)
-dontwarn android.**
