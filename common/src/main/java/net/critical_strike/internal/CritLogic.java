package net.critical_strike.internal;

import net.critical_strike.fx.CriticalStrikeSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundCategory;

public class CritLogic {
    public record Result(DamageSource source, float amount) {}
    public static Result modifyDamage(CriticalStriker critter, DamageSource source, float amount) {
        var isCritical = critter.rng_shouldDealCriticalHit();
        if (isCritical) {
            var bonusMultiplier = critter.rng_criticalDamageMultiplier();

            // TODO: modify DamageSource - set critical flag

            return new Result(source, amount * (float) bonusMultiplier);
        }
        return null;
    }

    public static void playFxAt(Entity entity, float volume) {
        var world = entity.getWorld();
        // world.playSoundFromEntity(entity, CriticalStrikeSounds.CRITICAL_HIT.soundEvent(), SoundCategory.PLAYERS, 1.0f, 1.0f);

        var pitch = 0.9F + (world.getRandom().nextFloat() * 0.2F);
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                CriticalStrikeSounds.CRITICAL_HIT.soundEvent(), SoundCategory.PLAYERS, volume, pitch);
    }
}
