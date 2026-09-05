package net.critical_strike.fabric.datagen;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.api.CriticalStrikeEnchantments;
import net.critical_strike.fx.CriticalStrikeParticles;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 1.20.1 datagen: lang, particles, sounds and the `enchantable/weapon` item tag.
 * The 1.21 enchantment + enchantment-tag providers are gone: enchantments are Java classes here
 * ({@link CriticalStrikeEnchantments}), exclusivity and treasure-ness are `Enchantment` overrides.
 */
public class CriticalStrikeDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(LangGenerator::new);
        pack.addProvider(ParticlesGen::new);
        pack.addProvider(SoundGen::new);
        pack.addProvider(ItemTagGen::new);
    }

    public static class LangGenerator extends FabricLanguageProvider {
        protected LangGenerator(FabricDataOutput dataOutput) {
            super(dataOutput, "en_us");
        }

        @Override
        public void generateTranslations(TranslationBuilder translationBuilder) {
            for (var entry: CriticalStrikeAttributes.all) {
                translationBuilder.add(entry.translationKey, entry.translations.name());

                var effectKey = Util.createTranslationKey("effect", entry.id);
                translationBuilder.add(effectKey, entry.translations.effectName());
                translationBuilder.add(effectKey + ".description", entry.translations.effectDescription());

                var potionPath = entry.potionId().getPath();
                var tippedArrowKey = "item.minecraft.tipped_arrow." + "effect." + potionPath;
                translationBuilder.add(tippedArrowKey, "Arrow of " + entry.translations.effectName());
                var potionKey = "item.minecraft.potion." + "effect." + potionPath;
                translationBuilder.add(potionKey, "Potion of " + entry.translations.effectName());
                var splashPotionKey = "item.minecraft.splash_potion." + "effect." + potionPath;
                translationBuilder.add(splashPotionKey, "Splash Potion of " + entry.translations.effectName());
                var lingeringPotionKey = "item.minecraft.lingering_potion." + "effect." + potionPath;
                translationBuilder.add(lingeringPotionKey, "Lingering Potion of " + entry.translations.effectName());
            }
            for (var entry: CriticalStrikeEnchantments.entries) {
                var key = Util.createTranslationKey("enchantment", entry.id());
                translationBuilder.add(key, entry.name());
                translationBuilder.add(key + ".description", entry.description());
                // Enchantment Descriptions (1.20.1) reads the `.desc` suffix
                translationBuilder.add(key + ".desc", entry.description());
            }
        }
    }

    public static class ParticlesGen extends SimpleParticleGenerator {
        public ParticlesGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generateSimpleParticles(Builder builder) {
            // Generate simple particles
            for (var entry : CriticalStrikeParticles.ENTRIES) {
                if (entry.texture().frames() > 1) {
                    for (int i = 0; i < entry.texture().frames(); i++) {
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

            // Generate template particles
            for (var entry : CriticalStrikeParticles.TEMPLATE_ENTRIES) {
                if (entry.texture().frames() > 1) {
                    for (int i = 0; i < entry.texture().frames(); i++) {
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

    /**
     * `critical_strike:enchantable/weapon` (1.20.1 path `tags/items/`). The 1.21 `#minecraft:enchantable/*`
     * tags do not exist here; swords/axes come from vanilla tags, bows/crossbows are listed directly.
     * The Java {@link CriticalStrikeEnchantments#isWeaponEnchantable} check covers the same set even
     * without the tag, so the tag is mainly an extension point for datapacks.
     */
    public static class ItemTagGen extends FabricTagProvider<Item> {
        public ItemTagGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.ITEM, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            getOrCreateTagBuilder(CriticalStrikeEnchantments.WEAPON_ENCHANTABLE)
                    .addOptionalTag(ItemTags.SWORDS.id())
                    .addOptionalTag(ItemTags.AXES.id())
                    .addOptional(new net.minecraft.util.Identifier("minecraft", "bow"))
                    .addOptional(new net.minecraft.util.Identifier("minecraft", "crossbow"));
        }

        @Override
        public String getName() {
            return "Critical Strike Item Tags";
        }
    }
}
