package net.critical_strike.api;

public interface CriticalDamageSource {
    boolean rng_isCritical();
    void rng_setCritical(boolean critical);
}
