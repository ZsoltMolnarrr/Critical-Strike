package net.critical_strike.client;

import net.critical_strike.client.particle.TemplateParticleEffect;
import net.critical_strike.client.util.Color;
import net.critical_strike.fx.CriticalStrikeParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ParticleHelper {
    public static void spawnCritParticles(Entity target) {
        if (target.level() instanceof ClientLevel clientWorld) {
            var config = CriticalStrikeClient.config.value;

            var width = target.getBbWidth();
            var originX = target.getX();
            var originY = target.getY() + target.getBbHeight() * 0.5F;
            var originZ = target.getZ();

            var color = Color.from(config.particle_alt_color);

            var spark = resolveParticleType(CriticalStrikeParticles.SPARKLE.id(), color, null);
            for (int i = 0; i < config.particle_spark_count; i++) {
                var speed = config.particle_spark_speed;
                var velocity = new Vec3(1F,0,0).yRot(clientWorld.random.nextFloat() * 360F)
                        .xRot(clientWorld.random.nextFloat() * 360F)
                        .scale(speed + clientWorld.random.nextFloat() * (speed * 0.5F));
                clientWorld.addParticle(spark, true, false,
                        originX, originY, originZ,
                        velocity.x, velocity.y, velocity.z);
            }

            if (!target.isAlive()) {
                return;
            }

            var skull = resolveParticleType(CriticalStrikeParticles.SKULL.id(), color, target);
            for (int i = 0; i < config.particle_skull_count; i+=2 ) {
                var offset = new Vec3(width,0,0).yRot(clientWorld.random.nextFloat() * 360F);
                var velocity = new Vec3(0, 0.1F,0);
                clientWorld.addParticle(skull, true, false,
                        originX + offset.x, originY + offset.y, originZ + offset.z,
                        velocity.x, velocity.y, velocity.z);

                if (i + 1 >= config.particle_skull_count) break;
                offset = offset.reverse();
                clientWorld.addParticle(skull, true, false,
                        originX + offset.x, originY + offset.y, originZ + offset.z,
                        velocity.x, velocity.y, velocity.z);
            }

            var circle = resolveParticleType(CriticalStrikeParticles.CIRCLE.id(), color, target);
            clientWorld.addParticle(circle, true, false,
                    originX, originY, originZ,
                    0, 0, 0);
        }
    }

    private static ParticleOptions resolveParticleType(Identifier particleId, Color color, @Nullable Entity sourceEntity) {
        var particle = (ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.getValue(particleId);

        if (particle instanceof TemplateParticleEffect templateParticleEffect) {
            var copy = templateParticleEffect.copy();
            var appearance = copy.createOrDefaultAppearance();
            appearance.color = color;
            if (sourceEntity != null) {
                appearance.entityFollowed = sourceEntity;
            }
            particle = copy;
        }
        return particle;
    }

}
