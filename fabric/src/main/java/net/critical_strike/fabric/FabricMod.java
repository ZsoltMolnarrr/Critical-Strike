package net.critical_strike.fabric;

import net.critical_strike.fx.CriticalStrikeParticles;
import net.fabricmc.api.ModInitializer;

import net.critical_strike.CriticalStrikeMod;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        CriticalStrikeMod.registerSounds();
        CriticalStrikeMod.init();
        CriticalStrikeParticles.register();
    }
}
