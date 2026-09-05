package net.critical_strike.forge.client;

import net.critical_strike.client.CriticalStrikeClient;
import net.critical_strike.client.particle.CriticalStrikeParticle;
import net.critical_strike.fx.CriticalStrikeParticles;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only wiring; only touched from ForgeMod behind a Dist.CLIENT check. */
public final class ForgeClientMod {
    public static void register(IEventBus modBus) {
        modBus.addListener(EventPriority.NORMAL, false, FMLClientSetupEvent.class, ForgeClientMod::onClientSetup);
        modBus.addListener(EventPriority.NORMAL, false, RegisterParticleProvidersEvent.class, ForgeClientMod::onRegisterParticleProviders);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        CriticalStrikeClient.init();
    }

    private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                CriticalStrikeParticles.SPARKLE.particleType(),
                (provider) -> new CriticalStrikeParticle.MagicVariant(provider, CriticalStrikeParticles.SPARKLE.behaviour())
        );
        event.registerSpriteSet(
                CriticalStrikeParticles.SKULL.particleType(),
                (provider) -> new CriticalStrikeParticle.MagicVariant(provider, CriticalStrikeParticles.SKULL.behaviour())
        );
        event.registerSpriteSet(
                CriticalStrikeParticles.CIRCLE.particleType(),
                (provider) -> new CriticalStrikeParticle.MagicVariant(provider, CriticalStrikeParticles.CIRCLE.behaviour())
        );
    }
}
