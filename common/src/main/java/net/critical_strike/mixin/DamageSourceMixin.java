package net.critical_strike.mixin;

import net.critical_strike.api.CriticalDamageSource;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements CriticalDamageSource {
    @Unique private boolean criticalStrike_isCritical = false;

    @Override
    public boolean rng_isCritical() {
        return criticalStrike_isCritical;
    }

    @Override
    public void rng_setCritical(boolean critical) {
        criticalStrike_isCritical = critical;
    }
}
