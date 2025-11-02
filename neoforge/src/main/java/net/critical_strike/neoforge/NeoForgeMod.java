package net.critical_strike.neoforge;

import net.critical_strike.fx.CriticalStrikeParticles;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.minecraft.registry.RegistryKeys;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import net.critical_strike.CriticalStrikeMod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(CriticalStrikeMod.ID)
public final class NeoForgeMod {
    public NeoForgeMod(IEventBus modBus) {
        // Run our common setup.
        CriticalStrikeMod.init();
        modBus.addListener(RegisterEvent.class, NeoForgeMod::register);
    }

    public static void register(RegisterEvent event) {
        event.register(RegistryKeys.PARTICLE_TYPE, reg -> {
            CriticalStrikeParticles.register();
        });
        event.register(RegistryKeys.SOUND_EVENT, reg -> {
            CriticalStrikeSounds.register();
        });
    }
}
