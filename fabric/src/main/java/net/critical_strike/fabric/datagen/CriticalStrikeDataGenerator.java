package net.critical_strike.fabric.datagen;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.fx.CriticalStrikeParticles;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CriticalStrikeDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(LangGenerator::new);
        pack.addProvider(ParticlesGen::new);
        pack.addProvider(SoundGen::new);
        pack.addProvider(ItemTagGen::new);
        pack.addProvider(EnchantmentTagGen::new);
        pack.addProvider(EnchantmentGenerator::new);
    }

    public static class LangGenerator extends FabricLanguageProvider {
        protected LangGenerator(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(dataOutput, "en_us", registryLookup);
        }

        @Override
        public void generateTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
            for (var entry: CriticalStrikeAttributes.all) {
                translationBuilder.add(entry.translationKey, entry.translations.name());

                var effectKey = Util.makeDescriptionId("effect", entry.id);
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
            for (var entry: Enchantments.entries) {
                var key = Util.makeDescriptionId("enchantment", entry.id());
                translationBuilder.add(key, entry.name());
                translationBuilder.add(key + ".description", entry.description());
            }
        }
    }

    public static class ParticlesGen extends SimpleParticleGenerator {
        public ParticlesGen(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
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
        public SoundGen(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
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


    public static class Enchantments {
        public record Entry(Identifier id, String name, String description) { }
        public static ArrayList<Entry> entries = new ArrayList<>();
        public static Entry add(Entry entry) {
            entries.add(entry);
            return entry;
        }

        public static Entry CRITICAL_CHANCE = add(new Entry(
            Identifier.fromNamespaceAndPath(CriticalStrikeMod.ID, "chance"),
            "Critical Hit", "Increase chance to deal critical hits."
        ));

        public static Entry CRITICAL_DAMAGE = add(new Entry(
            Identifier.fromNamespaceAndPath(CriticalStrikeMod.ID, "damage"),
            "Critical Impact", "Increase damage dealt by critical hits."
        ));

        private static Identifier mutexTagId = Identifier.fromNamespaceAndPath(CriticalStrikeMod.ID, "critical_enchantments");
    }

    public static class EnchantmentTagGen extends FabricTagsProvider<Enchantment> {
        public EnchantmentTagGen(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, Registries.ENCHANTMENT, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider wrapperLookup) {
            var tagKey = TagKey.create(Registries.ENCHANTMENT, Enchantments.mutexTagId);
            builder(tagKey)
                    .addOptional(ResourceKey.create(Registries.ENCHANTMENT, Enchantments.CRITICAL_CHANCE.id))
                    .addOptional(ResourceKey.create(Registries.ENCHANTMENT, Enchantments.CRITICAL_DAMAGE.id));

            var nonTreasureTagKey = EnchantmentTags.NON_TREASURE;
            builder(nonTreasureTagKey)
                    .addOptional(ResourceKey.create(Registries.ENCHANTMENT, Enchantments.CRITICAL_CHANCE.id))
                    .addOptional(ResourceKey.create(Registries.ENCHANTMENT, Enchantments.CRITICAL_DAMAGE.id));
        }

        @Override
        public String getName() {
            return "Critical Strike Enchantment Tags";
        }
    }

    public static final Identifier CRITICAL_WEAPON_ENCHANTABLE = Identifier.fromNamespaceAndPath(CriticalStrikeMod.ID, "enchantable/weapon");
    public static class ItemTagGen extends FabricTagsProvider<Item> {
        public ItemTagGen(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, Registries.ITEM, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider wrapperLookup) {
            var tagKey = TagKey.create(Registries.ITEM, CRITICAL_WEAPON_ENCHANTABLE);
            builder(tagKey)
                    .addOptionalTag(ItemTags.WEAPON_ENCHANTABLE)
                    .addOptionalTag(ItemTags.BOW_ENCHANTABLE)
                    .addOptionalTag(ItemTags.CROSSBOW_ENCHANTABLE);
        }

        @Override
        public String getName() {
            return "Critical Strike Item Tags";
        }
    }

    public static class EnchantmentGenerator extends FabricDynamicRegistryProvider {
        public EnchantmentGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(HolderLookup.Provider registries, Entries entries) {
            HolderGetter<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);
            HolderGetter<Enchantment> enchantmentLookup = registries.lookupOrThrow(Registries.ENCHANTMENT);

            // Critical Strike enchantment - increases critical hit chance
            var criticalStrikeId = ResourceKey.create(Registries.ENCHANTMENT, Enchantments.CRITICAL_CHANCE.id());
            Enchantment.Builder criticalStrike = Enchantment.enchantment(
                    Enchantment.definition(
                            itemLookup.getOrThrow(TagKey.create(Registries.ITEM, CRITICAL_WEAPON_ENCHANTABLE)),
                            5, // weight (rarity) - 5 is uncommon
                            5, // max level
                            Enchantment.dynamicCost(3, 12), // min cost
                            Enchantment.dynamicCost(12, 11), // max cost
                            1, // anvil cost
                            EquipmentSlotGroup.MAINHAND)
            )
            .withEffect(
                    EnchantmentEffectComponents.ATTRIBUTES,
                    new EnchantmentAttributeEffect(
                            Identifier.fromNamespaceAndPath(Enchantments.CRITICAL_CHANCE.id().getNamespace(),
                                    "enchantment_" + Enchantments.CRITICAL_CHANCE.id.getPath()),
                            CriticalStrikeAttributes.CHANCE.attributeEntry,
                            LevelBasedValue.perLevel(0.04F),
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
            .exclusiveWith(enchantmentLookup.getOrThrow(TagKey.create(Registries.ENCHANTMENT, Enchantments.mutexTagId)))
            ;
            entries.add(criticalStrikeId, criticalStrike.build(criticalStrikeId.identifier()));


            var criticalDamageId = ResourceKey.create(Registries.ENCHANTMENT, Enchantments.CRITICAL_DAMAGE.id());
            Enchantment.Builder criticalDamage = Enchantment.enchantment(
                        Enchantment.definition(
                                itemLookup.getOrThrow(TagKey.create(Registries.ITEM, CRITICAL_WEAPON_ENCHANTABLE)),
                                5, // weight (rarity) - 5 is uncommon
                                5, // max level
                                Enchantment.dynamicCost(3, 12), // min cost
                                Enchantment.dynamicCost(12, 11), // max cost
                                1, // anvil cost
                                EquipmentSlotGroup.MAINHAND)
                )
                .withEffect(
                        EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                Identifier.fromNamespaceAndPath(Enchantments.CRITICAL_DAMAGE.id().getNamespace(),
                                        "enchantment_" + Enchantments.CRITICAL_DAMAGE.id.getPath()),
                                CriticalStrikeAttributes.DAMAGE.attributeEntry,
                                LevelBasedValue.perLevel(0.1F),
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                )
                .exclusiveWith(enchantmentLookup.getOrThrow(TagKey.create(Registries.ENCHANTMENT, Enchantments.mutexTagId)))
                ;
            entries.add(criticalDamageId, criticalDamage.build(criticalDamageId.identifier()));
        }

        @Override
        public String getName() {
            return "Critical Strike Enchantments";
        }
    }
}
