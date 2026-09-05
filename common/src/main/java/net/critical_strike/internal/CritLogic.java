package net.critical_strike.internal;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalDamageSource;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.jetbrains.annotations.Nullable;

public class CritLogic {
    /**
     * "Is a weapon" = carries any attribute modifier for a hand slot (1.21: ATTRIBUTE_MODIFIERS component
     * with a MAINHAND/OFFHAND/HAND slot; 1.20.1: the per-slot modifier multimap, item defaults or NBT).
     */
    public static boolean isWeapon(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        return !itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty()
                || !itemStack.getAttributeModifiers(EquipmentSlot.OFFHAND).isEmpty();
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
    }
}
