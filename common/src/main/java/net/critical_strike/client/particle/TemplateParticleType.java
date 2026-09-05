package net.critical_strike.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import net.minecraft.client.particle.Particle;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

/**
 * A particle type that is also its own (parameter-less on the wire) particle effect, carrying a client-side
 * {@link Appearance} that is never serialized. 1.20.1 shape: {@code Codec} + {@code ParticleEffect.Factory}
 * + {@code write(PacketByteBuf)} / {@code asString()} (the 1.21 MapCodec/PacketCodec pair does not exist here).
 */
public class TemplateParticleType extends ParticleType<TemplateParticleType> implements ParticleEffect, TemplateParticleEffect {
    private static final ParticleEffect.Factory<TemplateParticleType> PARAMETER_FACTORY = new ParticleEffect.Factory<>() {
        @Override
        public TemplateParticleType read(ParticleType<TemplateParticleType> particleType, StringReader stringReader) {
            return (TemplateParticleType) particleType;
        }

        @Override
        public TemplateParticleType read(ParticleType<TemplateParticleType> particleType, PacketByteBuf packetByteBuf) {
            return (TemplateParticleType) particleType;
        }
    };

    private final Codec<TemplateParticleType> codec = Codec.unit(this::getType);

    /** The registered instance; copies made for per-spawn appearance point back to it. */
    private TemplateParticleType type;
    public TemplateParticleType() {
        this(true);
    }

    public TemplateParticleType(boolean alwaysShow) {
        super(alwaysShow, PARAMETER_FACTORY);
        this.type = this;
    }

    @Override
    public TemplateParticleType getType() {
        return this.type;
    }

    @Override
    public Codec<TemplateParticleType> getCodec() {
        return this.codec;
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public String asString() {
        return Registries.PARTICLE_TYPE.getId(this.type).toString();
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
        var copy = new TemplateParticleType(this.shouldAlwaysSpawn());
        copy.type = this.type;
        return copy;
    }

    public static void apply(TemplateParticleType templateParticleType, Particle particle) {
        var appearance = templateParticleType.getAppearance();
        if (appearance != null) {
            var color = appearance.color;
            if (color != null) {
                particle.setColor(color.red(), color.green(), color.blue());
            }
        }
    }
}
