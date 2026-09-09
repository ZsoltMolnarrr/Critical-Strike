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
import net.minecraft.util.Identifier;
import net.tiny_config.ConfigManager;

import java.util.LinkedHashMap;
import java.util.Map;

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
    // All of these are idempotent, and all of them are the *Fabric* path: attributes/effects/potions come
    // from the <clinit>-TAIL mixins on EntityAttributes/StatusEffects/Potions, the rest from the mod
    // initializer. Forge does NOT call them — a plain `Registry.register` throws "Can not register to a
    // locked registry" on Forge 47.0-47.3 and NeoForge 1.20.1 even inside the RegisterEvent window (only
    // 47.4.0+ clears the vanilla registry's own lock), so ForgeMod registers the same content itself through
    // the helper RegisterEvent hands out. The content each pass iterates is public, so nothing is shared
    // beyond it.

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

    private static Map<Identifier, Potion> potionsToRegister = null;

    /**
     * Builds every potion the mod adds, keyed by the id it registers under. Creation only — nothing is
     * registered here, so a loader that registers potions itself iterates this map instead of duplicating the
     * construction. Built once; repeated calls return the same map.
     */
    public static Map<Identifier, Potion> potionsToRegister() {
        if (potionsToRegister != null) { return potionsToRegister; }
        var potions = new LinkedHashMap<Identifier, Potion>();
        for (var entry: CriticalStrikeAttributes.all) {
            var effect = entry.statusEffect();
            if (effect == null) continue;
            potions.put(entry.potionId(), new Potion(new StatusEffectInstance(effect, 3600, 0, false, true)));
        }
        potionsToRegister = potions;
        return potionsToRegister;
    }

    public static void registerPotions() {
        for (var entry: potionsToRegister().entrySet()) {
            if (Registries.POTION.containsId(entry.getKey())) continue;
            Registry.register(Registries.POTION, entry.getKey(), entry.getValue());
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
