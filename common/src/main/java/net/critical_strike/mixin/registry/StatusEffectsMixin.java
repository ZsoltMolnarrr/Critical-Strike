package net.critical_strike.mixin.registry;

import net.critical_strike.CriticalStrikeMod;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffects.class)
public class StatusEffectsMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_CriticalStrike(CallbackInfo ci) {
        CriticalStrikeMod.registerEffects();
    }
}
