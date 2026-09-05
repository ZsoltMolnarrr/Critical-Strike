package net.critical_strike.fabric;

import net.fabricmc.api.ModInitializer;

import net.critical_strike.CriticalStrikeMod;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // Attributes, status effects and potions are registered earlier, from the <clinit>-TAIL mixins in
        // critical_strike.fabric.mixins.json (so they resolve from the very first attribute container build).
        CriticalStrikeMod.registerSounds();
        CriticalStrikeMod.registerParticles();
        CriticalStrikeMod.registerEnchantments();
        CriticalStrikeMod.init();
    }
}
