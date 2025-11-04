package net.critical_strike.mixin;

import net.critical_strike.api.CriticalDamageSource;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements CriticalDamageSource {
    @Unique private float criticalStrike_damageMultiplier = 0F;

    @Override
    public boolean rng_isCritical() {
        return criticalStrike_damageMultiplier != 0F;
    }

    @Override
    public void rng_setCriticalDamageMultiplier(float multiplier) {
        criticalStrike_damageMultiplier = multiplier;
    }
    
    @Override
    public float rng_getCriticalDamageMultiplier() {
        return criticalStrike_damageMultiplier;
    }
}
