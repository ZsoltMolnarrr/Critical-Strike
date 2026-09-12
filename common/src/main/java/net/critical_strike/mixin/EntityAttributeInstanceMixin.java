package net.critical_strike.mixin;

import net.critical_strike.api.AttributeIdentifiers;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityAttributeInstance.class)
public abstract class EntityAttributeInstanceMixin {
    @Shadow public abstract boolean hasModifier(Identifier id);

    @Inject(
            method = "addPersistentModifier", require = 1, allow = 1,
            at = @At("HEAD"), cancellable = true
    )
    private void criticalStrike_skipDuplicateInnate(EntityAttributeModifier modifier, CallbackInfo ci) {
        // The innate bonus is seeded in the PlayerEntity constructor. On End-portal return vanilla
        // builds a fresh ServerPlayerEntity (constructor seeds it again) and then copies the old
        // player's persistent modifiers via copyFrom, which would throw
        // "Modifier is already applied on this attribute!". Re-adding the innate bonus when it is
        // already present is a no-op.
        if (AttributeIdentifiers.INNATE_BONUS.equals(modifier.id()) && hasModifier(modifier.id())) {
            ci.cancel();
        }
    }
}
