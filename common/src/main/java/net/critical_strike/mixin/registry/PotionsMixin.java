package net.critical_strike.mixin.registry;

import net.critical_strike.CriticalStrikeMod;
import net.minecraft.potion.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Potions.class)
public class PotionsMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_CriticalStrike(CallbackInfo ci) {
        CriticalStrikeMod.registerPotions();
    }
}
