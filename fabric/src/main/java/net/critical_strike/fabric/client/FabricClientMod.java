package net.critical_strike.fabric.client;

import net.critical_strike.client.CriticalStrikeClient;
import net.critical_strike.client.particle.CriticalStrikeParticle;
import net.critical_strike.fx.CriticalStrikeParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public final class FabricClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CriticalStrikeClient.init();
        registerParticleAppearances();
    }

    public void registerParticleAppearances() {
        ParticleFactoryRegistry.getInstance().register(
                CriticalStrikeParticles.SPARKLE.particleType(),
                (provider) -> new CriticalStrikeParticle.MagicVariant(provider, CriticalStrikeParticles.SPARKLE.behaviour())
        );
        ParticleFactoryRegistry.getInstance().register(
                CriticalStrikeParticles.SKULL.particleType(),
                (provider) -> new CriticalStrikeParticle.MagicVariant(provider, CriticalStrikeParticles.SKULL.behaviour())
        );
    }
}
