package net.critical_strike.internal;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.api.CriticalDamageSource;
import net.critical_strike.fx.CriticalStrikeSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CritLogic {
    public static boolean isWeapon(ItemStack itemStack) {
        var attributes = itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
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

    private static boolean isHand(EquipmentSlotGroup slot) {
        return slot == EquipmentSlotGroup.MAINHAND
                || slot == EquipmentSlotGroup.OFFHAND
                || slot == EquipmentSlotGroup.HAND;
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
        var world = target.level();
        if (world instanceof ServerLevel serverWorld) {
            serverWorld.getChunkSource().sendToTrackingPlayersAndSelf(target, new ClientboundAnimatePacket(target, CriticalStrikeMod.CRIT_PACKET_CODE));
            var pitch = 0.9F + (world.getRandom().nextFloat() * 0.2F);
            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                    CriticalStrikeSounds.CRITICAL_HIT.soundEvent(), SoundSource.PLAYERS, volume, pitch);
        }
        // world.playSoundFromEntity(entity, CriticalStrikeSounds.CRITICAL_HIT.soundEvent(), SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}
