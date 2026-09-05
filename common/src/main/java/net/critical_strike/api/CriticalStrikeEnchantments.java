package net.critical_strike.api;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.internal.CriticalStrikeEnchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * The two Java enchantments of the mod (1.20.1 has no data-driven enchantments).
 * <p>
 * On 1.21 these were attribute-modifier enchantments (`add_multiplied_base` on the crit attributes,
 * mainhand slot). Here the level bonus is applied at query time instead: {@link #bonus(Entry, LivingEntity)}
 * is added on top of the attribute-derived value inside the crit logic, so no equip/unequip bookkeeping
 * and no persisted modifiers are needed.
 */
public class CriticalStrikeEnchantments {
    /** Items the crit enchantments accept, in addition to vanilla swords/axes/bows/crossbows. */
    public static final TagKey<Item> WEAPON_ENCHANTABLE = TagKey.of(RegistryKeys.ITEM,
            new Identifier(CriticalStrikeMod.ID, "enchantable/weapon"));

    public static final class Entry {
        public final Identifier id;
        public final CriticalStrikeAttributes.Entry attribute;
        /** Bonus added per enchantment level, in attribute-multiplier units (0.04 = +4% chance, +0.1x damage). */
        public final float bonusPerLevel;
        public final String name;
        public final String description;
        public final CriticalStrikeEnchantment enchantment;

        Entry(String path, CriticalStrikeAttributes.Entry attribute, float bonusPerLevel, String name, String description) {
            this.id = new Identifier(CriticalStrikeMod.ID, path);
            this.attribute = attribute;
            this.bonusPerLevel = bonusPerLevel;
            this.name = name;
            this.description = description;
            this.enchantment = new CriticalStrikeEnchantment(this);
        }

        public Identifier id() { return id; }
        public String name() { return name; }
        public String description() { return description; }

        /** Level of this enchantment on the entity's main hand item. */
        public int level(LivingEntity entity) {
            return EnchantmentHelper.getLevel(enchantment, entity.getMainHandStack());
        }
    }

    public static final List<Entry> entries = new ArrayList<>();
    private static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    public static final Entry CHANCE = add(new Entry("chance", CriticalStrikeAttributes.CHANCE, 0.04F,
            "Critical Hit", "Increase chance to deal critical hits."));
    public static final Entry DAMAGE = add(new Entry("damage", CriticalStrikeAttributes.DAMAGE, 0.1F,
            "Critical Impact", "Increase damage dealt by critical hits."));

    /** Query-time equivalent of the 1.21 attribute effect: {@code level * bonusPerLevel} from the main hand. */
    public static double bonus(Entry entry, LivingEntity entity) {
        var level = entry.level(entity);
        return level > 0 ? level * (double) entry.bonusPerLevel : 0;
    }

    /** Mirrors the 1.21 `#critical_strike:enchantable/weapon` tag (weapons + bows + crossbows). */
    public static boolean isWeaponEnchantable(ItemStack stack) {
        if (stack.isEmpty()) { return false; }
        var item = stack.getItem();
        return stack.isIn(WEAPON_ENCHANTABLE)
                || stack.isIn(ItemTags.SWORDS)
                || stack.isIn(ItemTags.AXES)
                || item instanceof SwordItem
                || item instanceof AxeItem
                || item instanceof BowItem
                || item instanceof CrossbowItem;
    }

    /** Idempotent: safe to call from both the Fabric mod initializer and the Forge RegisterEvent. */
    public static void register() {
        for (var entry : entries) {
            if (Registries.ENCHANTMENT.containsId(entry.id)) { continue; }
            Registry.register(Registries.ENCHANTMENT, entry.id, entry.enchantment);
        }
    }
}
