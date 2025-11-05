package net.critical_strike.fabric.datagen;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.fx.CriticalStrikeParticles;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

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
        protected LangGenerator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, "en_us", registryLookup);
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
            for (var entry: CriticalStrikeAttributes.all) {
                translationBuilder.add(entry.translationKey, entry.translations.name());
            }
            for (var entry: Enchantments.entries) {
                var key = Util.createTranslationKey("enchantment", entry.id());
                translationBuilder.add(key, entry.name());
                translationBuilder.add(key + ".description", entry.description());
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


    public static class Enchantments {
        public record Entry(Identifier id, String name, String description) { }
        public static ArrayList<Entry> entries = new ArrayList<>();
        public static Entry add(Entry entry) {
            entries.add(entry);
            return entry;
        }

        public static Entry CRITICAL_CHANCE = add(new Entry(
            Identifier.of(CriticalStrikeMod.ID, "chance"),
            "Critical Hit", "Increase chance to deal critical hits."
        ));

        public static Entry CRITICAL_DAMAGE = add(new Entry(
            Identifier.of(CriticalStrikeMod.ID, "damage"),
            "Critical Impact", "Increase damage dealt by critical hits."
        ));

        private static Identifier mutexTagId = Identifier.of(CriticalStrikeMod.ID, "critical_enchantments");
    }

    public static class EnchantmentTagGen extends FabricTagProvider<Enchantment> {
        public EnchantmentTagGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.ENCHANTMENT, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            var tagKey = TagKey.of(RegistryKeys.ENCHANTMENT, Enchantments.mutexTagId);
            getOrCreateTagBuilder(tagKey)
                    .addOptional(Enchantments.CRITICAL_CHANCE.id)
                    .addOptional(Enchantments.CRITICAL_DAMAGE.id);

            var nonTreasureTagKey = EnchantmentTags.NON_TREASURE;
            getOrCreateTagBuilder(nonTreasureTagKey)
                    .addOptional(Enchantments.CRITICAL_CHANCE.id)
                    .addOptional(Enchantments.CRITICAL_DAMAGE.id);
        }

        @Override
        public String getName() {
            return "Critical Strike Enchantment Tags";
        }
    }

    public static final Identifier CRITICAL_WEAPON_ENCHANTABLE = Identifier.of(CriticalStrikeMod.ID, "enchantable/weapon");
    public static class ItemTagGen extends FabricTagProvider<Item> {
        public ItemTagGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.ITEM, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            var tagKey = TagKey.of(RegistryKeys.ITEM, CRITICAL_WEAPON_ENCHANTABLE);
            getOrCreateTagBuilder(tagKey)
                    .addOptionalTag(ItemTags.WEAPON_ENCHANTABLE.id())
                    .addOptionalTag(ItemTags.BOW_ENCHANTABLE.id())
                    .addOptionalTag(ItemTags.CROSSBOW_ENCHANTABLE.id());
        }

        @Override
        public String getName() {
            return "Critical Strike Item Tags";
        }
    }

    public static class EnchantmentGenerator extends FabricDynamicRegistryProvider {
        public EnchantmentGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
            RegistryEntryLookup<Item> itemLookup = registries.createRegistryLookup().getOrThrow(RegistryKeys.ITEM);
            RegistryEntryLookup<Enchantment> enchantmentLookup = registries.createRegistryLookup().getOrThrow(RegistryKeys.ENCHANTMENT);

            // Critical Strike enchantment - increases critical hit chance
            var criticalStrikeId = RegistryKey.of(RegistryKeys.ENCHANTMENT, Enchantments.CRITICAL_CHANCE.id());
            Enchantment.Builder criticalStrike = Enchantment.builder(
                    Enchantment.definition(
                            itemLookup.getOrThrow(TagKey.of(RegistryKeys.ITEM, CRITICAL_WEAPON_ENCHANTABLE)),
                            5, // weight (rarity) - 5 is uncommon
                            5, // max level
                            Enchantment.leveledCost(3, 12), // min cost
                            Enchantment.leveledCost(12, 11), // max cost
                            1, // anvil cost
                            AttributeModifierSlot.MAINHAND)
            )
            .addEffect(
                    EnchantmentEffectComponentTypes.ATTRIBUTES,
                    new AttributeEnchantmentEffect(
                            Identifier.of(Enchantments.CRITICAL_CHANCE.id().getNamespace(),
                                    "enchantment_" + Enchantments.CRITICAL_CHANCE.id.getPath()),
                            CriticalStrikeAttributes.CHANCE.entry,
                            EnchantmentLevelBasedValue.linear(0.04F),
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
            //.exclusiveSet(enchantmentLookup.getOrThrow(TagKey.of(RegistryKeys.ENCHANTMENT, Enchantments.mutexTagId)))
            ;
            entries.add(criticalStrikeId, criticalStrike.build(criticalStrikeId.getValue()));


            var criticalDamageId = RegistryKey.of(RegistryKeys.ENCHANTMENT, Enchantments.CRITICAL_DAMAGE.id());
            Enchantment.Builder criticalDamage = Enchantment.builder(
                        Enchantment.definition(
                                itemLookup.getOrThrow(TagKey.of(RegistryKeys.ITEM, CRITICAL_WEAPON_ENCHANTABLE)),
                                5, // weight (rarity) - 5 is uncommon
                                5, // max level
                                Enchantment.leveledCost(3, 12), // min cost
                                Enchantment.leveledCost(12, 11), // max cost
                                1, // anvil cost
                                AttributeModifierSlot.MAINHAND)
                )
                .addEffect(
                        EnchantmentEffectComponentTypes.ATTRIBUTES,
                        new AttributeEnchantmentEffect(
                                Identifier.of(Enchantments.CRITICAL_DAMAGE.id().getNamespace(),
                                        "enchantment_" + Enchantments.CRITICAL_DAMAGE.id.getPath()),
                                CriticalStrikeAttributes.DAMAGE.entry,
                                EnchantmentLevelBasedValue.linear(0.1F),
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                )
                //.exclusiveSet(enchantmentLookup.getOrThrow(TagKey.of(RegistryKeys.ENCHANTMENT, Enchantments.mutexTagId)))
                ;
            entries.add(criticalDamageId, criticalDamage.build(criticalDamageId.getValue()));
        }

        @Override
        public String getName() {
            return "Critical Strike Enchantments";
        }
    }
}
