package net.critical_strike;

import net.critical_strike.fx.CriticalStrikeSounds;

public final class CriticalStrikeMod {
    public static final String ID = "critical_strike";

    public static void init() {
        // Write common init code here.
    }

    public static void registerSounds() {
        CriticalStrikeSounds.register();
    }
}
