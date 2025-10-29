package net.critical_strike.neoforge;

import net.neoforged.fml.common.Mod;

import net.critical_strike.CriticalStrikeMod;

@Mod(CriticalStrikeMod.ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        CriticalStrikeMod.init();
    }
}
