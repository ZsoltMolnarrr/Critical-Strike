package net.critical_strike.client.particle;

import net.critical_strike.client.util.Color;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface TemplateParticleEffect extends ParticleOptions {
    class Appearance {
        public @Nullable Color color;
        public float scale = 1;
        public Entity entityFollowed;
        public boolean grounded = false;
        public float max_age = 1;
    }

    void setAppearance(Appearance appearance);
    Appearance getAppearance();

    default Appearance createOrDefaultAppearance() {
        var appearance = getAppearance() == null ? new Appearance() : getAppearance();
        setAppearance(appearance);
        return appearance;
    }

    TemplateParticleEffect copy();
}
