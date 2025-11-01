package net.critical_strike.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.internal.CritLogic;
import net.critical_strike.internal.CriticalStriker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements CriticalStriker {

    @Shadow public abstract void addCritParticles(Entity target);

    @Shadow public abstract void addEnchantedHitParticles(Entity target);

    @Inject(
            method = "createPlayerAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
            require = 1, allow = 1, at = @At("RETURN")
    )
    private static void addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        for (var entry : CriticalStrikeAttributes.all) {
            info.getReturnValue().add(entry.entry);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructed(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        for (var entry : CriticalStrikeAttributes.all) {
            if (entry.innateModifier != null) {
                ((PlayerEntity)(Object)this)
                        .getAttributes()
                        .getCustomInstance(entry.entry)
                        .addPersistentModifier(entry.innateModifier);
            }
        }
    }

    private int critical_chance_time = 0;
    private boolean critical_strike_active = false;
    public boolean rng_shouldDealCriticalHit() {
        var player = (PlayerEntity)(Object)this;

        // Disabled batching
//        if (critical_chance_time != player.age) {
//            critical_chance_time = player.age;
//            var value = player.getAttributeValue(CriticalStrikeAttributes.CHANCE.entry);
//            var chance = CriticalStrikeAttributes.CHANCE.asChance(value);
//            critical_strike_active = player.getRandom().nextFloat() < chance;
//        }
//        return critical_strike_active;

        var value = player.getAttributeValue(CriticalStrikeAttributes.CHANCE.entry);
        var chance = CriticalStrikeAttributes.CHANCE.asChance(value);
        return critical_strike_active = player.getRandom().nextFloat() < chance;
    }

    public double rng_criticalChance() {
        var player = (PlayerEntity)(Object)this;
        var value = player.getAttributeValue(CriticalStrikeAttributes.CHANCE.entry);
        return CriticalStrikeAttributes.CHANCE.asChance(value);
    }

    public double rng_criticalDamageMultiplier() {
        var player = (PlayerEntity)(Object)this;
        var value = player.getAttributeValue(CriticalStrikeAttributes.DAMAGE.entry);
        return CriticalStrikeAttributes.DAMAGE.asMultiplier(value);
    }


    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isOnGround()Z"
            )
    )
    private boolean disableVanillaCrit(PlayerEntity instance, Operation<Boolean> original) {
        original.call(instance);
        return true;
    }

    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean applyCriticalStrikeDamage(Entity instance, DamageSource source, float amount, Operation<Boolean> original) {
        var critter = (CriticalStriker)(Object)this;
        var crit = CritLogic.modifyDamage(critter, source, amount);
        if (crit != null) {
            var result = original.call(instance, crit.source(), crit.amount());
            this.addEnchantedHitParticles(instance);
            return result;
        } else {
            return original.call(instance, source, amount);
        }
    }
}
