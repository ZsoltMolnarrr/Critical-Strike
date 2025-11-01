package net.critical_strike.neoforge;

import net.neoforged.fml.common.Mod;

import net.critical_strike.CriticalStrikeMod;

@Mod(CriticalStrikeMod.ID)
public final class NeoForgeMod {
    public NeoForgeMod() {
        // Run our common setup.
        CriticalStrikeMod.init();
    }
}
