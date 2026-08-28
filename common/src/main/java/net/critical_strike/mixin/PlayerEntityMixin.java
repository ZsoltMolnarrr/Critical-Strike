package net.critical_strike.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.internal.CritLogic;
import net.critical_strike.internal.CriticalStriker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements CriticalStriker {

    @Inject(
            method = "createAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;",
            require = 1, allow = 1, at = @At("RETURN")
    )
    private static void addAttributes(final CallbackInfoReturnable<AttributeSupplier.Builder> info) {
        for (var entry : CriticalStrikeAttributes.all) {
            info.getReturnValue().add(entry.attributeEntry);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructed(Level world, GameProfile gameProfile, CallbackInfo ci) {
        for (var entry : CriticalStrikeAttributes.all) {
            if (entry.innateModifier != null) {
                ((Player)(Object)this)
                        .getAttributes()
                        .getInstance(entry.attributeEntry)
                        .addPermanentModifier(entry.innateModifier);
            }
        }
    }

    private int critical_chance_time = 0;
    private boolean critical_strike_active = false;
    public boolean rng_shouldDealCriticalHit() {
        var player = (Player)(Object)this;

        if (CriticalStrikeMod.config.value.enable_critical_strike_batching) {
            if (critical_chance_time != player.tickCount) {
                var chance = rng_criticalChance();
                critical_strike_active = player.getRandom().nextFloat() < chance;
                critical_chance_time = player.tickCount;
            }
            return critical_strike_active;
        } else {
            var chance = rng_criticalChance();
            return player.getRandom().nextFloat() < chance;
        }
    }

    public double rng_criticalChance() {
        var player = (Player)(Object)this;
        var value = player.getAttributeValue(CriticalStrikeAttributes.CHANCE.attributeEntry);
        return CriticalStrikeAttributes.CHANCE.asChance(value);
    }

    public double rng_criticalDamageMultiplier() {
        var player = (Player)(Object)this;
        var value = player.getAttributeValue(CriticalStrikeAttributes.DAMAGE.attributeEntry);
        return CriticalStrikeAttributes.DAMAGE.asMultiplier(value);
    }


    /// Since 1.21.2 `attack` no longer inlines the fall-distance/`isOnGround` chain — it asks
    /// `isCriticalHit(target)`. Forcing that to `false` disables the vanilla jump crit without
    /// touching the sweep-attack check, which does its own `isOnGround()` call.
    @WrapOperation(
            method = "attack",
            require = 1, allow = 1,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;canCriticalAttack(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean disableVanillaCrit(Player instance, Entity target, Operation<Boolean> original) {
        if (CriticalStrikeMod.config.value.disable_vanilla_jump_criticals) {
            return false;
        }
        return original.call(instance, target);
    }

    @WrapOperation(
            method = "attack",
            require = 1, allow = 1,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    private boolean applyCriticalStrikeDamage(Entity instance, DamageSource source, float amount, Operation<Boolean> original) {
        var attacker = (Player)(Object)this;
        return criticalStrike_wrapMeleeHit(
                instance, source, amount, original,
                CriticalStrikeMod.config.value.enable_melee_criticals,
                attacker.getMainHandItem());
    }

    /// 26.1 added a second melee path next to `attack`: `stabAttack`, driven by the `PiercingWeapon`
    /// and `KineticWeapon` item components. It carries its own `hurtOrSimulate` call, so without a
    /// wrap of its own a stab would never crit and would never carry the crit `DamageSource` stamp
    /// that SpellEngine reads back as `criticalImpact`.
    ///
    /// Deliberately a second injector rather than `method = {"attack", "stabAttack"}`: Mixin counts
    /// `require` across *all* target methods, so an aggregated `require = 2` would still pass if one
    /// of the two silently stopped matching. One injector per method keeps `require = 1, allow = 1`
    /// exact for each.
    ///
    /// The `canCriticalAttack` wrap above stays on `attack` only — `stabAttack` never calls it.
    /// The weapon gate resolves the stack from the slot the stab is performed with, which is not
    /// necessarily the main hand.
    @WrapOperation(
            method = "stabAttack",
            require = 1, allow = 1,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    private boolean applyCriticalStabDamage(Entity instance, DamageSource source, float amount, Operation<Boolean> original,
                                            EquipmentSlot slot, Entity target, float baseDamage,
                                            boolean dealsDamage, boolean dealsKnockback, boolean dismounts) {
        var attacker = (Player)(Object)this;
        return criticalStrike_wrapMeleeHit(
                instance, source, amount, original,
                CriticalStrikeMod.config.value.enable_stab_criticals,
                attacker.getItemBySlot(slot));
    }

    @Unique
    private boolean criticalStrike_wrapMeleeHit(Entity instance, DamageSource source, float amount,
                                                Operation<Boolean> original, boolean enabled, ItemStack weapon) {
        var config = CriticalStrikeMod.config.value;
        if (!enabled) {
            return original.call(instance, source, amount);
        }
        if (config.require_weapon_for_critical_strikes && !CritLogic.isWeapon(weapon)) {
            return original.call(instance, source, amount);
        }

        var critter = (CriticalStriker)(Object)this;
        var crit = CritLogic.modifyDamage(critter, source, amount);
        if (crit != null) {
            var result = original.call(instance, crit.source(), crit.amount() * config.balance_melee_damage_multiplier);
            if (result) {
                CritLogic.playFxAt(instance, config.sound_melee_crit_volume);
            }
            return result;
        } else {
            return original.call(instance, source, amount);
        }
    }
}
