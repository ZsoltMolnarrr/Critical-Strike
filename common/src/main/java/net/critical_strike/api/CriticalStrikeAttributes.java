package net.critical_strike.api;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.internal.CustomStatusEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CriticalStrikeAttributes {
    public static final String NAMESPACE = CriticalStrikeMod.ID;
    public static final ArrayList<Entry> all = new ArrayList<>();
    private static Entry entry(String name, double baseValue, boolean tracked) {
        return entry(name, 0, baseValue, tracked);
    }
    private static Entry entry(String name, double minValue, double baseValue, boolean tracked) {
        var entry = new Entry(name, minValue, baseValue, tracked);
        all.add(entry);
        return entry;
    }

    public record Translations(String name, String effectName, String effectDescription) { }
    public static class Entry {
        public final Identifier id;
        public final String translationKey;
        public final Attribute attribute;
        public final double baseValue;
        @Nullable public Holder<Attribute> attributeEntry;
        @Nullable public AttributeModifier innateModifier;
        @Nullable public Translations translations;

        public Entry(String name, double minValue, double baseValue, boolean tracked) {
            this.id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
            this.translationKey = "attribute.name." + NAMESPACE + "." + name;
            this.attribute = new RangedAttribute(translationKey, baseValue, minValue, 2048)
                    .setSyncable(tracked);
            this.baseValue = baseValue;
        }

        public double asMultiplier(double attributeValue) {
            return attributeValue / baseValue;
        }

        public float asChance(double attributeValue) {
            return (float) ((attributeValue - baseValue) / baseValue);
        }

        public void register() {
            if (attributeEntry != null) { return; }
            attributeEntry = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, id, attribute);
        }

        public Entry innateModifier(AttributeModifier.Operation operation, float value) {
            innateModifier = new AttributeModifier(AttributeIdentifiers.INNATE_BONUS, value, operation);
            return this;
        }

        public void setInnateBonus(float bonus) {
            if (this.innateModifier != null) {
                this.innateModifier = new AttributeModifier(innateModifier.id(), bonus, innateModifier.operation());
            }
        }

        public Entry translations(String name, String effectName, String effectDescription) {
            this.translations = new Translations(name, effectName, effectDescription);
            return this;
        }

        @Nullable private MobEffect statusEffect = null;
        @Nullable public Holder<MobEffect> effectEntry = null;
        public Entry effect(int color) {
            this.statusEffect = new CustomStatusEffect(MobEffectCategory.BENEFICIAL, color);
            return this;
        }
        public void setEffectBonus(float bonus) {
            if (this.statusEffect != null && this.attributeEntry != null) {
                this.statusEffect.addAttributeModifier(this.attributeEntry,
                        AttributeIdentifiers.EFFECT_BONUS,
                        bonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
            }
        }
        public void registerEffect() {
            if (this.statusEffect != null) {
                this.effectEntry = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id, this.statusEffect);
            }
        }
        public Identifier potionId() {
            return Identifier.fromNamespaceAndPath(id.getNamespace(), id.getNamespace() + "_" + id.getPath());
        }
    }

    public static final Entry CHANCE = entry("chance", 100, 100, false)
            .translations("Critical Hit Chance", "Critical Hit", "Increases the chance to deal critical hits.")
            .innateModifier(AttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.05F)
            .effect(0xF400FF);
    public static final Entry DAMAGE = entry("damage", 100, 100, false)
            .translations("Critical Hit Damage", "Critical Impact", "Increases the damage dealt by critical hits.")
            .innateModifier(AttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.5F)
            .effect(0x800000);
}
