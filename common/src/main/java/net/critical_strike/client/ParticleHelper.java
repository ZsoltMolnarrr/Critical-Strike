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

            var width = target.getWidth();
            var originX = target.getX() + width * 0.5F;
            var originY = target.getY() + target.getHeight() * 0.5F;
            var originZ = target.getZ() + width * 0.5F;

            var spark = resolveParticleType(CriticalStrikeParticles.SPARKLE.id(), Color.from(0xff66ff), null);
            for (int i = 0; i < 15; i++) {

                var velocity = new Vec3d(1F,0,0).rotateY(clientWorld.random.nextFloat() * 360F)
                        .rotateX(clientWorld.random.nextFloat() * 360F)
                        .multiply(0.4F + clientWorld.random.nextFloat() * 0.2F);

                clientWorld.addParticle(spark, true,
                        originX, originY, originZ,
                        velocity.x, velocity.y, velocity.z);
            }

            var skull = resolveParticleType(CriticalStrikeParticles.SKULL.id(), Color.from(0xff66ff), target);
            for (int i = 0; i < 1; i++) {
                var offset = new Vec3d(width,0,0).rotateY(clientWorld.random.nextFloat() * 360F);
                var velocity = new Vec3d(0, 0.1F,0);
                clientWorld.addParticle(skull, true,
                        originX + offset.x, originY + offset.y, originZ + offset.z,
                        velocity.x, velocity.y, velocity.z);

                offset = offset.negate();
                clientWorld.addParticle(skull, true,
                        originX + offset.x, originY + offset.y, originZ + offset.z,
                        velocity.x, velocity.y, velocity.z);
            }

            var circle = resolveParticleType(CriticalStrikeParticles.CIRCLE.id(), Color.from(0xff66ff), target);
            clientWorld.addParticle(circle, true,
                    originX, originY, originZ,
                    1, 1, 0);
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
//            if (batch.color_rgba >= 0) {
//                appearance.color = Color.fromRGBA(batch.color_rgba);
//            }
//            if (batch.follow_entity) {
//                appearance.entityFollowed = sourceEntity;
//            }
//            if (batch.scale != 1) {
//                appearance.scale = batch.scale;
//            }
//            if (batch.origin == ParticleBatch.Origin.GROUND) {
//                appearance.grounded = true;
//            }
//            appearance.max_age = batch.max_age;
            particle = copy;
        }
        return particle;
    }

}
