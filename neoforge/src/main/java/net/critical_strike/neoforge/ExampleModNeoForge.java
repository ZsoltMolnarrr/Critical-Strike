package net.critical_strike.neoforge;

import net.neoforged.fml.common.Mod;

import net.critical_strike.ExampleMod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        ExampleMod.init();
    }
}
