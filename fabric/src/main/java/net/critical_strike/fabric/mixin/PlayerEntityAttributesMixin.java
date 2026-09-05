package net.critical_strike.fabric.mixin;

import net.critical_strike.api.CriticalStrikeAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fabric only: Forge attaches the attributes through EntityAttributeModificationEvent instead (see ForgeMod). */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAttributesMixin {
    @Inject(
            method = "createPlayerAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
            require = 1, allow = 1, at = @At("RETURN")
    )
    private static void addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        for (var entry : CriticalStrikeAttributes.all) {
            info.getReturnValue().add(entry.attribute);
        }
    }
}
