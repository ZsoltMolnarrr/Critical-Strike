package net.critical_strike;

import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.api.CriticalStrikeEnchantments;
import net.critical_strike.fx.CriticalStrikeParticles;
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

    /**
     * Config refresh + config-driven tuning. Registration-independent, so it may run before or after
     * the register* functions (Fabric: after the clinit mixins; Forge: mod constructor, before RegisterEvent).
     */
    public static void init() {
        config.refresh();
        CriticalStrikeAttributes.CHANCE.setInnateBonus(config.value.attribute_crit_chance_innate_bonus);
        CriticalStrikeAttributes.CHANCE.setEffectBonus(config.value.effect_crit_chance_per_level);
        CriticalStrikeAttributes.DAMAGE.setInnateBonus(config.value.attribute_crit_damage_innate_bonus);
        CriticalStrikeAttributes.DAMAGE.setEffectBonus(config.value.effect_crit_damage_per_level);
    }

    // MARK: Registration
    // All of these are idempotent. Fabric drives attributes/effects/potions from <clinit>-TAIL mixins on
    // EntityAttributes/StatusEffects/Potions and the rest from the mod initializer; Forge drives every one
    // of them from RegisterEvent (vanilla registries are locked outside that window on Forge 47).

    public static void registerAttributes() {
        for (var entry: CriticalStrikeAttributes.all) {
            entry.register();
        }
    }

    public static void registerEffects() {
        for (var entry: CriticalStrikeAttributes.all) {
            entry.registerEffect();
        }
    }

    public static void registerPotions() {
        for (var entry: CriticalStrikeAttributes.all) {
            var effect = entry.statusEffect();
            if (effect == null) continue;
            var potionId = entry.potionId();
            if (Registries.POTION.containsId(potionId)) continue;
            var potion = new Potion(new StatusEffectInstance(effect, 3600, 0, false, true));
            Registry.register(Registries.POTION, potionId, potion);
        }
    }

    public static void registerEnchantments() {
        CriticalStrikeEnchantments.register();
    }

    public static void registerSounds() {
        CriticalStrikeSounds.register();
    }

    public static void registerParticles() {
        CriticalStrikeParticles.register();
    }
}
