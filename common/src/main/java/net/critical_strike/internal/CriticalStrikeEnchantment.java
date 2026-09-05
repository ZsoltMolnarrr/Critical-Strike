package net.critical_strike.internal;

import net.critical_strike.api.CriticalStrikeEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Java counterpart of the 1.21 data-driven crit enchantments.
 * <p>
 * Tuning mirrors the former enchantment JSON: weight 5 (UNCOMMON), 5 levels, min cost 3 + 12/level,
 * max cost 12 + 11/level, mainhand slot, mutually exclusive (`#critical_strike:critical_enchantments`),
 * non-treasure. {@link EnchantmentTarget#WEAPON} only covers swords in 1.20.1, so item acceptance is
 * overridden to mirror the 1.21 `#critical_strike:enchantable/weapon` tag (weapons + bows + crossbows);
 * the enchanting table is widened accordingly by {@code EnchantmentHelperMixin} (Fabric) and by
 * {@link #canApplyAtEnchantingTable(ItemStack)} (Forge's patched table hook).
 */
public class CriticalStrikeEnchantment extends Enchantment {
    private static final int MAX_LEVEL = 5;
    private static final int MIN_COST_BASE = 3;
    private static final int MIN_COST_PER_LEVEL = 12;
    private static final int MAX_COST_BASE = 12;
    private static final int MAX_COST_PER_LEVEL = 11;

    public final CriticalStrikeEnchantments.Entry entry;

    public CriticalStrikeEnchantment(CriticalStrikeEnchantments.Entry entry) {
        super(Rarity.UNCOMMON, EnchantmentTarget.WEAPON, new EquipmentSlot[]{ EquipmentSlot.MAINHAND });
        this.entry = entry;
    }

    @Override
    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    @Override
    public int getMinPower(int level) {
        return MIN_COST_BASE + (level - 1) * MIN_COST_PER_LEVEL;
    }

    @Override
    public int getMaxPower(int level) {
        return MAX_COST_BASE + (level - 1) * MAX_COST_PER_LEVEL;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return CriticalStrikeEnchantments.isWeaponEnchantable(stack);
    }

    /**
     * Forge 47 hook (no {@code @Override}: the method only exists on Forge's patched {@code Enchantment}).
     * Forge's {@code EnchantmentHelper.getPossibleEntries} and anvil consult this instead of the target.
     */
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return isAcceptableItem(stack);
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return !(other instanceof CriticalStrikeEnchantment) && super.canAccept(other);
    }

    @Override
    public boolean isTreasure() {
        return false;
    }
}
