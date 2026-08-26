package net.critical_strike.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class TemplateParticleType extends ParticleType<TemplateParticleType> implements ParticleOptions, TemplateParticleEffect {
    private final MapCodec<TemplateParticleType> codec = MapCodec.unit(this::getType);
    private final StreamCodec<RegistryFriendlyByteBuf, TemplateParticleType> packetCodec = StreamCodec.unit(this);

    private TemplateParticleType type;
    public TemplateParticleType() {
        this(true);
    }

    public TemplateParticleType(boolean alwaysShow) {
        super(alwaysShow);
        this.type = this;
    }

    public TemplateParticleType getType() {
        return this.type;
    }

    @Override
    public MapCodec<TemplateParticleType> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, TemplateParticleType> streamCodec() {
        return packetCodec;
    }


    private TemplateParticleEffect.Appearance appearance = new TemplateParticleEffect.Appearance();

    @Override
    public void setAppearance(Appearance appearance) {
        this.appearance = appearance;
    }
    @Override
    public Appearance getAppearance() {
        return appearance;
    }
    @Override
    public TemplateParticleEffect copy() {
        var copy = new TemplateParticleType(this.getOverrideLimiter());
        copy.type = this.type;
        return copy;
    }

    public static void apply(TemplateParticleType templateParticleType, SingleQuadParticle particle) {
        var appearance = templateParticleType.getAppearance();
        if (appearance != null) {
            var color = appearance.color;
            if (color != null) {
                particle.setColor(color.red(), color.green(), color.blue());
            }
        }
    }
}
