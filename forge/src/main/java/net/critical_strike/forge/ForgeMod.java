package net.critical_strike.forge;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalStrikeAttributes;
import net.critical_strike.forge.client.ForgeClientMod;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Forge 47 entrypoint. Vanilla registries are locked outside the RegisterEvent window on Forge, so every
 * registration (attributes, status effects, potions, enchantments, sounds, particles) is driven from
 * RegisterEvent here through the idempotent `CriticalStrikeMod.register*` functions; the Fabric side uses
 * <clinit>-TAIL mixins for the first three. Player default attributes are attached through
 * EntityAttributeModificationEvent (Fabric: `createPlayerAttributes` RETURN inject).
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

    private static void register(RegisterEvent event) {
        event.register(RegistryKeys.ATTRIBUTE, helper -> CriticalStrikeMod.registerAttributes());
        event.register(RegistryKeys.STATUS_EFFECT, helper -> CriticalStrikeMod.registerEffects());
        event.register(RegistryKeys.POTION, helper -> CriticalStrikeMod.registerPotions());
        event.register(RegistryKeys.ENCHANTMENT, helper -> CriticalStrikeMod.registerEnchantments());
        event.register(RegistryKeys.SOUND_EVENT, helper -> CriticalStrikeMod.registerSounds());
        event.register(RegistryKeys.PARTICLE_TYPE, helper -> CriticalStrikeMod.registerParticles());
    }

    private static void attachAttributes(EntityAttributeModificationEvent event) {
        for (var entry : CriticalStrikeAttributes.all) {
            if (!event.has(EntityType.PLAYER, entry.attribute)) {
                event.add(EntityType.PLAYER, entry.attribute);
            }
        }
    }
}
