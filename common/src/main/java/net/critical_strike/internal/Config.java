package net.critical_strike.internal;

public class Config {
    public boolean disable_vanilla_jump_criticals = true;
    public boolean enable_melee_criticals = true;
    public boolean enable_ranged_criticals = true;
    public boolean enable_critical_strike_batching = false;
    public float sound_melee_crit_volume = 0.6F;
    public float sound_ranged_crit_volume = 0.8F;
    public float attribute_crit_chance_innate_bonus = 0.05F;
    public float attribute_crit_damage_innate_bonus = 0.5F;
    public float effect_crit_chance_per_level = 0.05F;
    public float effect_crit_damage_per_level = 0.1F;
}
