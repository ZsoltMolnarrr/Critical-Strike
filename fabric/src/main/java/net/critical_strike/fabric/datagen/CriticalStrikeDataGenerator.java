package net.critical_strike.fabric.datagen;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.fx.CriticalStrikeParticles;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CriticalStrikeDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(LangGenerator::new);
        pack.addProvider(ParticlesGen::new);
        pack.addProvider(SoundGen::new);
    }

    public static class LangGenerator extends FabricLanguageProvider {
        protected LangGenerator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, "en_us", registryLookup);
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
            for (var entry: CriticalStrikeAttributes.all) {
                translationBuilder.add(entry.translationKey, entry.translations.name());
            }
        }
    }

    public static class ParticlesGen extends SimpleParticleGenerator {
        public ParticlesGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generateSimpleParticles(Builder builder) {
            for (var entry : CriticalStrikeParticles.ENTRIES) {
                if (entry.texture().frames() > 1) {
                    for (int i = 0; i < entry.texture().frames(); i++) {
                        // var index = entry.texture().reverseOrder() ? (entry.texture().frames() - 1 - i) : i;
                        builder.add(
                            entry.id(),
                            new ParticleData(List.of(entry.texture().id().toString() + "_" + i))
                        );
                    }
                } else {
                    builder.add(
                        entry.id(),
                        new ParticleData(List.of(entry.texture().id().toString()))
                    );
                }
            }
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
