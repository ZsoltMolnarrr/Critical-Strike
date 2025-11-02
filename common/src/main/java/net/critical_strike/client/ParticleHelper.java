package net.critical_strike.client;

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
            var particle = resolveParticleType(Identifier.of("lava"), target);
            for (int i = 0; i < 10; i++) {

                var originX = target.getX() + target.getWidth() * 0.5F;
                var originY = target.getY() + target.getHeight() * 0.5F;
                var originZ = target.getZ() + target.getWidth() * 0.5F;

                var velocity = new Vec3d(1,0,0).rotateY(clientWorld.random.nextFloat() * 360F)
                        .rotateX(clientWorld.random.nextFloat() * 360F)
                        .multiply(0.2 + clientWorld.random.nextFloat() * 0.2);

                clientWorld.addParticle(particle, true,
                        originX, originY, originZ,
                        velocity.x, velocity.y, velocity.z);
            }
        }


    }

    private static ParticleEffect resolveParticleType(Identifier particleId, @Nullable Entity sourceEntity) {
        var particle = (ParticleEffect) Registries.PARTICLE_TYPE.get(particleId);

//        if (particle instanceof TemplateParticleEffect templateParticleEffect) {
//            var copy = templateParticleEffect.copy();
//            var appearance = copy.createOrDefaultAppearance();
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
//            particle = copy;
//        }
        return particle;
    }

}
