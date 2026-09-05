package net.critical_strike.mixin;

import net.critical_strike.api.CriticalStrikeEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * The 1.20.1 enchanting table only consults {@code enchantment.target.isAcceptableItem(item)} (swords for
 * {@code WEAPON}), never {@code Enchantment.isAcceptableItem(stack)}. Re-run the vanilla candidate loop for
 * the crit enchantments with the stack-based check so axes, bows and crossbows get offers too.
 * Idempotent: on Forge the patched table already goes through {@code canApplyAtEnchantingTable}, in which
 * case the entries are found present and nothing is added.
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "getPossibleEntries", at = @At("RETURN"))
    private static void critical_strike$addWeaponEntries(int power, ItemStack stack, boolean treasureAllowed,
                                                         CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        var list = cir.getReturnValue();
        for (var entry : CriticalStrikeEnchantments.entries) {
            var enchantment = entry.enchantment;
            boolean present = false;
            for (var existing : list) {
                if (existing.enchantment == enchantment) { present = true; break; }
            }
            if (present) continue;
            if (!enchantment.isAvailableForRandomSelection() || !enchantment.isAcceptableItem(stack)) continue;
            for (int level = enchantment.getMaxLevel(); level > enchantment.getMinLevel() - 1; level--) {
                if (power >= enchantment.getMinPower(level) && power <= enchantment.getMaxPower(level)) {
                    list.add(new EnchantmentLevelEntry(enchantment, level));
                    break;
                }
            }
        }
    }
}
