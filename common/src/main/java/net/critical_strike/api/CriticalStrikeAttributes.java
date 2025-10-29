package net.critical_strike.api;

import net.critical_strike.CriticalStrikeMod;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
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

    public static class Entry {
        public final Identifier id;
        public final String translationKey;
        public final EntityAttribute attribute;
        public final double baseValue;
        @Nullable public RegistryEntry<EntityAttribute> entry;
        @Nullable public EntityAttributeModifier innateModifier;

        public Entry(String name, double minValue, double baseValue, boolean tracked) {
            this.id = Identifier.of(NAMESPACE, name);
            this.translationKey = "attribute.name." + NAMESPACE + "." + name;
            this.attribute = new ClampedEntityAttribute(translationKey, baseValue, minValue, 2048)
                    .setTracked(tracked);
            this.baseValue = baseValue;
        }

        public double asMultiplier(double attributeValue) {
            return attributeValue / baseValue;
        }

        public float asChance(double attributeValue) {
            return (float) ((attributeValue - baseValue) / baseValue);
        }

        public void register() {
            entry = Registry.registerReference(Registries.ATTRIBUTE, id, attribute);
        }

        public Entry innateModifier(EntityAttributeModifier.Operation operation, float value) {
            innateModifier = new EntityAttributeModifier(AttributeIdentifiers.INNATE_BONUS, value, operation);
            return this;
        }
    }

    public static final Entry CHANCE = entry("chance", 100, 100, false)
            .innateModifier(EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.05F);
    public static final Entry DAMAGE = entry("damage", 100, 100, false)
            .innateModifier(EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.5F);
}
