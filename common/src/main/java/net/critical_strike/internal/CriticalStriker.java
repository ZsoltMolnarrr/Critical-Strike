package net.critical_strike.internal;

public interface CriticalStriker {
    boolean rng_shouldDealCriticalHit();
    double rng_criticalChance();
    double rng_criticalDamageMultiplier();
}
