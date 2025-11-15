package net.critical_strike.internal;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalDamageSource;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.jetbrains.annotations.Nullable;

public class CritLogic {
    public static boolean isWeapon(ItemStack itemStack) {
        var attributes = itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributes == null || attributes.modifiers() == null || attributes.modifiers().isEmpty()) {
            return false;
        }
        for (var modifier: attributes.modifiers()) {
            if (isHand(modifier.slot())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHand(AttributeModifierSlot slot) {
        return slot == AttributeModifierSlot.MAINHAND
                || slot == AttributeModifierSlot.OFFHAND
                || slot == AttributeModifierSlot.HAND;
    }

    public record Result(DamageSource source, float amount) {}
    @Nullable public static Result modifyDamage(CriticalStriker critter, DamageSource source, float amount) {
        var isCritical = critter.rng_shouldDealCriticalHit();
        if (isCritical) {
            var bonusMultiplier = (float) critter.rng_criticalDamageMultiplier();
            ((CriticalDamageSource)source).rng_setCriticalDamageMultiplier(bonusMultiplier);
            return new Result(source, amount * bonusMultiplier);
        }
        return null;
    }

    public static void playFxAt(Entity target, float volume) {
        var world = target.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().sendToNearbyPlayers(target, new EntityAnimationS2CPacket(target, CriticalStrikeMod.CRIT_PACKET_CODE));
            var pitch = 0.9F + (world.getRandom().nextFloat() * 0.2F);
            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                    CriticalStrikeSounds.CRITICAL_HIT.soundEvent(), SoundCategory.PLAYERS, volume, pitch);
        }
        // world.playSoundFromEntity(entity, CriticalStrikeSounds.CRITICAL_HIT.soundEvent(), SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}
