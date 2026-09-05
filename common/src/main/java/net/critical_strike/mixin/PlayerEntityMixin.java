package net.critical_strike.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.api.CriticalStrikeEnchantments;
import net.critical_strike.internal.CritLogic;
import net.critical_strike.internal.CriticalStriker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Crit rolls + vanilla jump-crit suppression for players.
 * <p>
 * The default-attribute injection (`createPlayerAttributes` RETURN) lives in the Fabric-only mixin set;
 * Forge attaches the attributes through EntityAttributeModificationEvent instead.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements CriticalStriker {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructed(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        var player = (PlayerEntity)(Object)this;
        for (var entry : CriticalStrikeAttributes.all) {
            if (entry.innateModifier == null) continue;
            var instance = player.getAttributes().getCustomInstance(entry.attribute);
            if (instance == null) continue; // attribute not attached to this container
            if (instance.getModifier(entry.innateModifier.getId()) != null) continue;
            instance.addPersistentModifier(entry.innateModifier);
        }
    }

    @Unique private int critical_chance_time = 0;
    @Unique private boolean critical_strike_active = false;
    public boolean rng_shouldDealCriticalHit() {
        var player = (PlayerEntity)(Object)this;

        if (CriticalStrikeMod.config.value.enable_critical_strike_batching) {
            if (critical_chance_time != player.age) {
                var chance = rng_criticalChance();
                critical_strike_active = player.getRandom().nextFloat() < chance;
                critical_chance_time = player.age;
            }
            return critical_strike_active;
        } else {
            var chance = rng_criticalChance();
            return player.getRandom().nextFloat() < chance;
        }
    }

    public double rng_criticalChance() {
        var player = (PlayerEntity)(Object)this;
        var value = player.getAttributeValue(CriticalStrikeAttributes.CHANCE.attribute);
        return CriticalStrikeAttributes.CHANCE.asChance(value)
                + CriticalStrikeEnchantments.bonus(CriticalStrikeEnchantments.CHANCE, player);
    }

    public double rng_criticalDamageMultiplier() {
        var player = (PlayerEntity)(Object)this;
        var value = player.getAttributeValue(CriticalStrikeAttributes.DAMAGE.attribute);
        return CriticalStrikeAttributes.DAMAGE.asMultiplier(value)
                + CriticalStrikeEnchantments.bonus(CriticalStrikeEnchantments.DAMAGE, player);
    }


    // Vanilla jump-crit condition is `... && this.fallDistance > 0.0F && !this.isOnGround() && ...`;
    // forcing isOnGround() true disables it (same wrap as the 1.21 line, which also covers the later
    // sweep-attack isOnGround() check in the same method).
    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isOnGround()Z"
            )
    )
    private boolean disableVanillaCrit(PlayerEntity instance, Operation<Boolean> original) {
        var result = original.call(instance);
        return CriticalStrikeMod.config.value.disable_vanilla_jump_criticals || result;
    }

    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean applyCriticalStrikeDamage(Entity instance, DamageSource source, float amount, Operation<Boolean> original) {
        var config = CriticalStrikeMod.config.value;
        if (!config.enable_melee_criticals) {
            return original.call(instance, source, amount);
        }
        var attacker = (PlayerEntity)(Object)this;
        if (config.require_weapon_for_critical_strikes && !CritLogic.isWeapon(attacker.getMainHandStack())) {
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
