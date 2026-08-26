package net.critical_strike.client.particle;

import net.critical_strike.fx.CriticalStrikeParticles;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class CriticalStrikeParticle extends BillboardParticle  {
    private static final Random RANDOM = Random.create();
    private final SpriteProvider spriteProvider;
    private final CriticalStrikeParticles.Motion motion;
    private boolean animated = false;
    public boolean glows = true;
    public boolean translucent = true;
    @Nullable Entity followEntity;
    private float growPerTickDelta = 0F;
    private float fadePerTickDelta = 0F;
    private float overlayScale = 0.8F;

    CriticalStrikeParticle(ClientWorld world, SpriteProvider spriteProvider, CriticalStrikeParticles.Motion motion, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, 0.5 - RANDOM.nextDouble(), velocityY, 0.5 - RANDOM.nextDouble(),
                spriteProvider.getSprite(world.getRandom()));
        this.spriteProvider = spriteProvider;
        this.motion = motion;

        switch (motion) {
            case FLOAT, DECELERATE -> {
                this.velocityMultiplier = 0.96F;
                this.velocityX = this.velocityX * 0.01F + velocityX;
                this.velocityY = this.velocityY * 0.01F + velocityY;
                this.velocityZ = this.velocityZ * 0.01F + velocityZ;
                this.x = this.x + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
                this.y = this.y + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
                this.z = this.z + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
                if (motion == CriticalStrikeParticles.Motion.DECELERATE) {
                    this.velocityMultiplier *= 0.8F;
                }
                this.maxAge = (int)(8.0 / (Math.random() * 0.8 + 0.2));
            }
            case ASCEND -> {
                this.velocityMultiplier = 0.96F;
                this.gravityStrength = -0.1F;
                this.ascending = true;
                this.velocityY *= 0.2;
                if (velocityX == 0.0 && velocityZ == 0.0) {
                    this.velocityX *= 0.10000000149011612;
                    this.velocityZ *= 0.10000000149011612;
                }
                this.maxAge = (int)(8.0 / (Math.random() * 0.8 + 0.2));
            }
            case BURST -> {
                this.velocityMultiplier = 0.7f;
                this.gravityStrength = 0.5f;
                this.velocityX *= (double)0.1f;
                this.velocityY *= (double)0.1f;
                this.velocityZ *= (double)0.1f;
                this.velocityX += velocityX * 0.4;
                this.velocityY += velocityY * 0.4;
                this.velocityZ += velocityZ * 0.4;
                this.maxAge = Math.max((int)(6.0 / (Math.random() * 0.8 + 0.6)), 1);
            }
        }

        this.updateSprite(spriteProvider);
        this.collidesWithWorld = false;
    }

    /// Render type = atlas + pipeline since 1.21.9. `LIT` had no dedicated sheet any more; it maps to translucent.
    @Override
    protected RenderType getRenderType() {
        if (glows || translucent) {
            return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
        } else {
            return RenderType.PARTICLE_ATLAS_OPAQUE;
        }
    }

    @Override
    public int getBrightness(float tint) {
        if (glows) {
            return 255;
        } else {
            return super.getBrightness(tint);
        }
    }

    public void move(double dx, double dy, double dz) {
        if (followEntity != null && !followEntity.isRemoved()) {
            dx += followEntity.getX() - followEntity.lastX;
            dy += followEntity.getY() - followEntity.lastY;
            dz += followEntity.getZ() - followEntity.lastZ;
        }
        super.move(dx, dy, dz);
    }

    @Override
    public void tick() {
        super.tick();
        if (animated) {
            this.updateSprite(this.spriteProvider);
        }
    }


    private float lastRendered = 0F;

    @Override
    public void render(BillboardParticleSubmittable submittable, Camera camera, float tickProgress) {
        var currentAge = this.age + tickProgress;
        var elapsed = currentAge - lastRendered;
        this.scale += growPerTickDelta * elapsed;
        this.alpha -= fadePerTickDelta * elapsed;
        if (this.alpha < 0F) {
            this.alpha = 0F;
        }

        super.render(submittable, camera, tickProgress);

        var red = this.red;
        var green = this.green;
        var blue = this.blue;
        var scale = this.scale;

        this.red = 1F;
        this.green = 1F;
        this.blue = 1F;
        this.scale = this.scale * overlayScale;

        geometryForOverlay = true;
        super.render(submittable, camera, tickProgress);
        geometryForOverlay = false;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.scale = scale;

        lastRendered = currentAge;
    }


    private boolean geometryForOverlay = false;
    @Override
    protected void render(BillboardParticleSubmittable submittable, Camera camera, Quaternionf rotation, float tickProgress) {
        Vec3d vec3d = camera.getCameraPos();

        // Bringing z position slightly closer for overlay pass to prevent z-fighting
        Vec3d overlayOffset = new Vec3d(0.0, 0.0, 0.0);
        if (geometryForOverlay) {
            var cameraLook = Vec3d.fromPolar(0, camera.getYaw());
            overlayOffset = cameraLook.normalize().multiply(0.01);
        }

        float g = (float)(MathHelper.lerp((double)tickProgress, this.lastX, this.x) - vec3d.getX() - overlayOffset.getX());
        float h = (float)(MathHelper.lerp((double)tickProgress, this.lastY, this.y) - vec3d.getY());
        float i = (float)(MathHelper.lerp((double)tickProgress, this.lastZ, this.z) - vec3d.getZ() - overlayOffset.getZ());
        this.renderVertex(submittable, rotation, g, h, i, tickProgress);
    }

    // MARK: Factories

    public static class MagicVariant implements ParticleFactory<TemplateParticleType> {
        private final SpriteProvider spriteProvider;
        private final CriticalStrikeParticles.Behaviour particleBehaviour;

        public MagicVariant(SpriteProvider spriteProvider, CriticalStrikeParticles.Behaviour particleBehaviour) {
            this.spriteProvider = spriteProvider;
            this.particleBehaviour = particleBehaviour;
        }

        public Particle createParticle(TemplateParticleType particleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, Random random) {
            var particle = new CriticalStrikeParticle(clientWorld, this.spriteProvider, particleBehaviour.motion(), d, e, f, g, h, i);
            particle.glows = true;
            particle.translucent = particleBehaviour.fadePerTickDelta() > 0F;
            particle.red = 1F;
            particle.green = 1F;
            particle.blue = 1F;
            particle.alpha = 0.75F;

            particle.animated = particleBehaviour.animated();
            particle.scale *= particleBehaviour.scale();
            particle.maxAge = particleBehaviour.maxAge();
            particle.growPerTickDelta = particleBehaviour.growPerTickDelta();
            particle.fadePerTickDelta = particleBehaviour.fadePerTickDelta();
            particle.overlayScale = particleBehaviour.overlayScale();

            TemplateParticleType.apply(particleType, particle);
            var appearance = particleType.getAppearance();
            if (appearance != null) {
                var color = appearance.color;
                if (color != null) {
                    particle.alpha *= appearance.color.alpha();
                }
                particle.scale *= appearance.scale;
                particle.followEntity = appearance.entityFollowed;
            }

            float j = clientWorld.random.nextFloat() * 0.4F + 0.6F;
            particle.setColor(particle.red * j, particle.green * j, particle.blue * j);

            return particle;
        }
    }
}
