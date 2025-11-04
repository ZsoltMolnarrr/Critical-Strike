package net.critical_strike.neoforge.client;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.client.CriticalStrikeClient;
import net.critical_strike.client.particle.CriticalStrikeParticle;
import net.critical_strike.fx.CriticalStrikeParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = CriticalStrikeMod.ID, value = Dist.CLIENT)
public final class NeoForgeClientMod {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CriticalStrikeClient.init();
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
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
