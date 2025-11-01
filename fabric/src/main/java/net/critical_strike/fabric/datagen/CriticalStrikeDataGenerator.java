package net.critical_strike.fabric.datagen;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class CriticalStrikeDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ParticlesGen::new);
        pack.addProvider(SoundGen::new);
    }

    public static class ParticlesGen extends SimpleParticleGenerator {
        public ParticlesGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generateSimpleParticles(Builder builder) {
            // Example particle generation
            // builder.add(
            //     Identifier.of(CriticalStrikeMod.ID, "crit_spark"),
            //     new ParticleData(List.of(CriticalStrikeMod.ID + ":crit_spark"))
            // );
        }
    }

    public static class SoundGen extends SimpleSoundGeneratorV2 {
        public SoundGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generateSounds(Builder builder) {
            builder.entries.add(new Entry(
                CriticalStrikeMod.ID,
                CriticalStrikeSounds.entries.stream()
                    .map(entry -> SoundEntry.withVariants(entry.id().getPath(), entry.variants()))
                    .toList()
            ));
        }
    }
}
