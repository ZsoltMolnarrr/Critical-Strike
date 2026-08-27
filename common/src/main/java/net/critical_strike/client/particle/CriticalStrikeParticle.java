package net.critical_strike.client.particle;

import net.critical_strike.fx.CriticalStrikeParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class CriticalStrikeParticle extends SingleQuadParticle  {
    private static final RandomSource RANDOM = RandomSource.create();
    private final SpriteSet spriteProvider;
    private final CriticalStrikeParticles.Motion motion;
    private boolean animated = false;
    public boolean glows = true;
    public boolean translucent = true;
    @Nullable Entity followEntity;
    private float growPerTickDelta = 0F;
    private float fadePerTickDelta = 0F;
    private float overlayScale = 0.8F;

    CriticalStrikeParticle(ClientLevel world, SpriteSet spriteProvider, CriticalStrikeParticles.Motion motion, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, 0.5 - RANDOM.nextDouble(), velocityY, 0.5 - RANDOM.nextDouble(),
                spriteProvider.get(world.getRandom()));
        this.spriteProvider = spriteProvider;
        this.motion = motion;

        switch (motion) {
            case FLOAT, DECELERATE -> {
                this.friction = 0.96F;
                this.xd = this.xd * 0.01F + velocityX;
                this.yd = this.yd * 0.01F + velocityY;
                this.zd = this.zd * 0.01F + velocityZ;
                this.x = this.x + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
                this.y = this.y + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
                this.z = this.z + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
                if (motion == CriticalStrikeParticles.Motion.DECELERATE) {
                    this.friction *= 0.8F;
                }
                this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
            }
            case ASCEND -> {
                this.friction = 0.96F;
                this.gravity = -0.1F;
                this.speedUpWhenYMotionIsBlocked = true;
                this.yd *= 0.2;
                if (velocityX == 0.0 && velocityZ == 0.0) {
                    this.xd *= 0.10000000149011612;
                    this.zd *= 0.10000000149011612;
                }
                this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
            }
            case BURST -> {
                this.friction = 0.7f;
                this.gravity = 0.5f;
                this.xd *= (double)0.1f;
                this.yd *= (double)0.1f;
                this.zd *= (double)0.1f;
                this.xd += velocityX * 0.4;
                this.yd += velocityY * 0.4;
                this.zd += velocityZ * 0.4;
                this.lifetime = Math.max((int)(6.0 / (Math.random() * 0.8 + 0.6)), 1);
            }
        }

        this.setSpriteFromAge(spriteProvider);
        this.hasPhysics = false;
    }

    /// Render type = atlas + pipeline since 1.21.9. `LIT` had no dedicated sheet any more; it maps to translucent.
    @Override
    protected Layer getLayer() {
        if (glows || translucent) {
            return Layer.TRANSLUCENT;
        } else {
            return Layer.OPAQUE;
        }
    }

    @Override
    protected int getLightCoords(float tint) {
        if (glows) {
            return 255;
        } else {
            return super.getLightCoords(tint);
        }
    }

    public void move(double dx, double dy, double dz) {
        if (followEntity != null && !followEntity.isRemoved()) {
            dx += followEntity.getX() - followEntity.xo;
            dy += followEntity.getY() - followEntity.yo;
            dz += followEntity.getZ() - followEntity.zo;
        }
        super.move(dx, dy, dz);
    }

    @Override
    public void tick() {
        super.tick();
        if (animated) {
            this.setSpriteFromAge(this.spriteProvider);
        }
    }


    private float lastRendered = 0F;

    @Override
    public void extract(QuadParticleRenderState submittable, Camera camera, float tickProgress) {
        var currentAge = this.age + tickProgress;
        var elapsed = currentAge - lastRendered;
        this.quadSize += growPerTickDelta * elapsed;
        this.alpha -= fadePerTickDelta * elapsed;
        if (this.alpha < 0F) {
            this.alpha = 0F;
        }

        super.extract(submittable, camera, tickProgress);

        var red = this.rCol;
        var green = this.gCol;
        var blue = this.bCol;
        var scale = this.quadSize;

        this.rCol = 1F;
        this.gCol = 1F;
        this.bCol = 1F;
        this.quadSize = this.quadSize * overlayScale;

        geometryForOverlay = true;
        super.extract(submittable, camera, tickProgress);
        geometryForOverlay = false;
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        this.quadSize = scale;

        lastRendered = currentAge;
    }


    private boolean geometryForOverlay = false;
    @Override
    protected void extractRotatedQuad(QuadParticleRenderState submittable, Camera camera, Quaternionf rotation, float tickProgress) {
        Vec3 vec3d = camera.position();

        // Bringing z position slightly closer for overlay pass to prevent z-fighting
        Vec3 overlayOffset = new Vec3(0.0, 0.0, 0.0);
        if (geometryForOverlay) {
            var cameraLook = Vec3.directionFromRotation(0, camera.yRot());
            overlayOffset = cameraLook.normalize().scale(0.01);
        }

        float g = (float)(Mth.lerp((double)tickProgress, this.xo, this.x) - vec3d.x() - overlayOffset.x());
        float h = (float)(Mth.lerp((double)tickProgress, this.yo, this.y) - vec3d.y());
        float i = (float)(Mth.lerp((double)tickProgress, this.zo, this.z) - vec3d.z() - overlayOffset.z());
        this.extractRotatedQuad(submittable, rotation, g, h, i, tickProgress);
    }

    // MARK: Factories

    public static class MagicVariant implements ParticleProvider<TemplateParticleType> {
        private final SpriteSet spriteProvider;
        private final CriticalStrikeParticles.Behaviour particleBehaviour;

        public MagicVariant(SpriteSet spriteProvider, CriticalStrikeParticles.Behaviour particleBehaviour) {
            this.spriteProvider = spriteProvider;
            this.particleBehaviour = particleBehaviour;
        }

        public Particle createParticle(TemplateParticleType particleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, RandomSource random) {
            var particle = new CriticalStrikeParticle(clientWorld, this.spriteProvider, particleBehaviour.motion(), d, e, f, g, h, i);
            particle.glows = true;
            particle.translucent = particleBehaviour.fadePerTickDelta() > 0F;
            particle.rCol = 1F;
            particle.gCol = 1F;
            particle.bCol = 1F;
            particle.alpha = 0.75F;

            particle.animated = particleBehaviour.animated();
            particle.quadSize *= particleBehaviour.scale();
            particle.lifetime = particleBehaviour.maxAge();
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
                particle.quadSize *= appearance.scale;
                particle.followEntity = appearance.entityFollowed;
            }

            float j = clientWorld.getRandom().nextFloat() * 0.4F + 0.6F;
            particle.setColor(particle.rCol * j, particle.gCol * j, particle.bCol * j);

            return particle;
        }
    }
}
