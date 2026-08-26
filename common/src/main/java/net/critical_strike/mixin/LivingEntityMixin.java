package net.critical_strike.mixin;

import net.critical_strike.api.CriticalStrikeAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {
    LivingEntityMixin(final EntityType<?> type, final Level world) {
        super(type, world);
    }

//    @Inject(
//            method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
//            require = 1, allow = 1, at = @At("RETURN")
//    )
//    private static void addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
//        for (var entry : CriticalStrikeAttributes.all) {
//            info.getReturnValue().add(entry.entry);
//        }
//    }
}