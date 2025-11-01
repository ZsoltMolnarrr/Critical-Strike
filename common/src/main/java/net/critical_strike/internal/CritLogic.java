package net.critical_strike.internal;

import net.minecraft.entity.damage.DamageSource;

public class CritLogic {
    public record Result(DamageSource source, float amount) {}
    public static Result modifyDamage(CriticalStriker critter, DamageSource source, float amount) {
        var isCritical = critter.rng_shouldDealCriticalHit();
        if (isCritical) {
            var bonusMultiplier = critter.rng_criticalDamageMultiplier();

            // TODO: modify DamageSource - set critical flag

            return new Result(source, amount * (float) bonusMultiplier);
        }
        return null;
    }
}
