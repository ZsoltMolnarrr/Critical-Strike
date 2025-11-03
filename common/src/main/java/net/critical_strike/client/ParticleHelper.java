package net.critical_strike.client;

import net.critical_strike.client.particle.TemplateParticleEffect;
import net.critical_strike.client.util.Color;
import net.critical_strike.fx.CriticalStrikeParticles;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ParticleHelper {
    public static void spawnCritParticles(Entity target) {
        if (target.getWorld() instanceof ClientWorld clientWorld) {
            var config = CriticalStrikeClient.config.value;

            var width = target.getWidth();
            var originX = target.getX();
            var originY = target.getY() + target.getHeight() * 0.5F;
            var originZ = target.getZ();

            var color = Color.from(config.particle_alt_color);

            var spark = resolveParticleType(CriticalStrikeParticles.SPARKLE.id(), color, null);
            for (int i = 0; i < config.particle_spark_count; i++) {
                var speed = config.particle_spark_speed;
                var velocity = new Vec3d(1F,0,0).rotateY(clientWorld.random.nextFloat() * 360F)
                        .rotateX(clientWorld.random.nextFloat() * 360F)
                        .multiply(speed + clientWorld.random.nextFloat() * (speed * 0.5F));
                clientWorld.addParticle(spark, true,
                        originX, originY, originZ,
                        velocity.x, velocity.y, velocity.z);
            }

            var skull = resolveParticleType(CriticalStrikeParticles.SKULL.id(), color, target);
            for (int i = 0; i < config.particle_skull_count; i+=2 ) {
                var offset = new Vec3d(width,0,0).rotateY(clientWorld.random.nextFloat() * 360F);
                var velocity = new Vec3d(0, 0.1F,0);
                clientWorld.addParticle(skull, true,
                        originX + offset.x, originY + offset.y, originZ + offset.z,
                        velocity.x, velocity.y, velocity.z);

                if (i + 1 >= config.particle_skull_count) break;
                offset = offset.negate();
                clientWorld.addParticle(skull, true,
                        originX + offset.x, originY + offset.y, originZ + offset.z,
                        velocity.x, velocity.y, velocity.z);
            }

            var circle = resolveParticleType(CriticalStrikeParticles.CIRCLE.id(), color, target);
            clientWorld.addParticle(circle, true,
                    originX, originY, originZ,
                    0, 0, 0);
        }
    }

    private static ParticleEffect resolveParticleType(Identifier particleId, Color color, @Nullable Entity sourceEntity) {
        var particle = (ParticleEffect) Registries.PARTICLE_TYPE.get(particleId);

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
