package net.critical_strike.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.internal.CritLogic;
import net.critical_strike.internal.CriticalStriker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public class PersistentProjectileEntityMixin {
    @WrapOperation(
            method = "onHitEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    private boolean wrapDamageEntity(Entity instance, DamageSource source, float amount, Operation<Boolean> original) {
        var config = CriticalStrikeMod.config.value;
        if (!config.enable_ranged_criticals) {
            return original.call(instance, source, amount);
        }

        var projectile = (AbstractArrow)(Object)this;
        if (projectile.getOwner() instanceof CriticalStriker critter) {
            var crit = CritLogic.modifyDamage(critter, source, amount);
            if (crit != null) {
                var result = original.call(instance, crit.source(), crit.amount() * config.balance_ranged_damage_multiplier);
                if (result) {
                    CritLogic.playFxAt(instance, config.sound_ranged_crit_volume);
                }
                return result;
            }
        }
        return original.call(instance, source, amount);
    }
}
