package net.critical_strike;

import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.critical_strike.internal.Config;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.tiny_config.ConfigManager;

public final class CriticalStrikeMod {
    public static final String ID = "critical_strike";

    public static ConfigManager<Config> config = new ConfigManager<>("server_config", new Config())
            .builder()
            .setDirectory(ID)
            .sanitize(true)
            .build();

    public static final int CRIT_PACKET_CODE = 43;

    public static void init() {
        config.refresh();
        CriticalStrikeAttributes.CHANCE.setInnateBonus(config.value.attribute_crit_chance_innate_bonus);
        CriticalStrikeAttributes.CHANCE.setEffectBonus(config.value.effect_crit_chance_per_level);
        CriticalStrikeAttributes.DAMAGE.setInnateBonus(config.value.attribute_crit_damage_innate_bonus);
        CriticalStrikeAttributes.DAMAGE.setEffectBonus(config.value.effect_crit_damage_per_level);
    }

    public static void registerSounds() {
        CriticalStrikeSounds.register();
    }

    public static void registerEffects() {
        for (var entry: CriticalStrikeAttributes.all) {
            entry.registerEffect();
        }
    }

    public static void registerPotions() {
        for (var entry: CriticalStrikeAttributes.all) {
            if (entry.effectEntry == null) continue;
            var potion = new Potion(new StatusEffectInstance(entry.effectEntry, 3600,
                    0, false, true));
            Registry.register(Registries.POTION, entry.potionId(), potion);
        }
    }
}
