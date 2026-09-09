package net.critical_strike.forge;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.api.CriticalStrikeEnchantments;
import net.critical_strike.forge.client.ForgeClientMod;
import net.critical_strike.fx.CriticalStrikeParticles;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Forge 47 entrypoint. Every registration (attributes, status effects, potions, enchantments, sounds,
 * particles) happens here, through the helper RegisterEvent hands out; the Fabric side keeps using
 * `CriticalStrikeMod.register*` (<clinit>-TAIL mixins for the first three, the mod initializer for the rest).
 * Player default attributes are attached through EntityAttributeModificationEvent (Fabric:
 * `createPlayerAttributes` RETURN inject).
 */
@Mod(CriticalStrikeMod.ID)
public final class ForgeMod {
    // FMLJavaModLoadingContext.get() is flagged for removal by late 47.x builds, but the
    // constructor-injected replacement doesn't exist on early 47.x; get() works on all of [47,).
    @SuppressWarnings("removal")
    public ForgeMod() {
        // Config refresh + config-driven tuning; independent of registration.
        CriticalStrikeMod.init();

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Explicit event classes: Forge 47's plain addListener(Consumer) infers the event type from the
        // lambda via TypeTools, which is fragile; the 4-arg overload takes it directly.
        modBus.addListener(EventPriority.NORMAL, false, RegisterEvent.class, ForgeMod::register);
        modBus.addListener(EventPriority.NORMAL, false, EntityAttributeModificationEvent.class, ForgeMod::attachAttributes);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClientMod.register(modBus);
        }
    }

    /**
     * Registration is duplicated here rather than delegated to `common`'s register* methods, because a plain
     * `Registry.register` is not usable on this loader: Forge only clears the vanilla registry's own lock from
     * 47.4.0 onwards, so on 47.0-47.3 and NeoForge 1.20.1 it throws "Can not register to a locked registry"
     * even inside the correct RegisterEvent window. Our mods.toml declares loaderVersion "[47,)", so those are
     * supported configurations. The helper this event hands out is the API every build of [47,) sanctions, so
     * Forge iterates the same content `common` exposes and registers it itself.
     * <p>
     * `event.register` is a no-op unless its key matches the event's registry, so every block is declared
     * unconditionally; Forge posts one event per registry and each block runs in exactly its own window.
     * The keys are Forge's own constants, so a silently mismatched key (event.register has no else and no
     * throw) is not possible.
     */
    private static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ATTRIBUTES, helper -> {
            for (var entry : CriticalStrikeAttributes.all) {
                helper.register(entry.id, entry.attribute);
            }
        });

        event.register(ForgeRegistries.Keys.MOB_EFFECTS, helper -> {
            for (var entry : CriticalStrikeAttributes.all) {
                var effect = entry.statusEffect();
                if (effect == null) continue;
                helper.register(entry.id, effect);
            }
        });

        // The potions are built by `common` (creation, not registration) and only registered here.
        event.register(ForgeRegistries.Keys.POTIONS, helper ->
                CriticalStrikeMod.potionsToRegister().forEach(helper::register));

        event.register(ForgeRegistries.Keys.ENCHANTMENTS, helper -> {
            for (var entry : CriticalStrikeEnchantments.entries) {
                helper.register(entry.id, entry.enchantment);
            }
        });

        event.register(ForgeRegistries.Keys.SOUND_EVENTS, helper -> {
            for (var entry : CriticalStrikeSounds.entries) {
                helper.register(entry.id(), entry.soundEvent());
            }
        });

        event.register(ForgeRegistries.Keys.PARTICLE_TYPES, helper -> {
            for (var entry : CriticalStrikeParticles.ENTRIES) {
                helper.register(entry.id(), entry.particleType());
            }
            for (var entry : CriticalStrikeParticles.TEMPLATE_ENTRIES) {
                helper.register(entry.id(), entry.particleType());
            }
        });
    }

    private static void attachAttributes(EntityAttributeModificationEvent event) {
        for (var entry : CriticalStrikeAttributes.all) {
            if (!event.has(EntityType.PLAYER, entry.attribute)) {
                event.add(EntityType.PLAYER, entry.attribute);
            }
        }
    }
}
