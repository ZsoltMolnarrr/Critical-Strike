package net.critical_strike.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.critical_strike.internal.CritLogic;
import net.critical_strike.internal.CriticalStriker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {


    @WrapOperation(
            method = "onEntityHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean wrapDamageEntity(Entity instance, DamageSource source, float amount, Operation<Boolean> original) {
        var projectile = (PersistentProjectileEntity)(Object)this;

        if (projectile.getOwner() instanceof CriticalStriker critter) {
            var crit = CritLogic.modifyDamage(critter, source, amount);
            if (crit != null) {
                return original.call(instance, crit.source(), crit.amount());
            }
        }
        return original.call(instance, source, amount);
    }
}
