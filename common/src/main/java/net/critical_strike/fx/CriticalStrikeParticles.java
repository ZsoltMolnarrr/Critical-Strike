package net.critical_strike.fx;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.client.particle.TemplateParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class CriticalStrikeParticles {
    public enum Motion { FLOAT, ASCEND, DECELERATE, BURST }
    public record Behaviour(Motion motion, boolean animated, int maxAge, float scale, float overlayScale,
                            float growPerTickDelta, float fadePerTickDelta) {
        public Behaviour(Motion motion, boolean animated, int maxAge, float scale) {
            this(motion, animated, maxAge, scale, 0.8F, 0F, 0F);
        }
        public Behaviour grow(float growPerTickDelta) {
            return new Behaviour(this.motion, this.animated, this.maxAge, this.scale, this.overlayScale,
                    growPerTickDelta, this.fadePerTickDelta);
        }
        public Behaviour fade(float fadePerTickDelta) {
            return new Behaviour(this.motion, this.animated, this.maxAge, this.scale, this.overlayScale,
                    this.growPerTickDelta, fadePerTickDelta);
        }
        public Behaviour overlayScale(float overlayScale) {
            return new Behaviour(this.motion, this.animated, this.maxAge, this.scale, overlayScale,
                    this.growPerTickDelta, this.fadePerTickDelta);
        }
    }

    private static class Helper extends SimpleParticleType {
        protected Helper(boolean alwaysShow) {
            super(alwaysShow);
        }
    }
    private static SimpleParticleType createSimple() {
        return new Helper(false);
    }

    public record Texture(Identifier id, int frames) {
        public static Texture vanilla(String name) {
            return new Texture(Identifier.ofVanilla(name), 1);
        }
        public static Texture vanilla(String name, int frames) {
            return new Texture(Identifier.ofVanilla(name), frames);
        }
        public static Texture of(String name) {
            return new Texture(Identifier.of(CriticalStrikeMod.ID, name), 1);
        }
    }

    public record Entry(Identifier id, Texture texture, SimpleParticleType particleType) {
        public Entry(String name, Texture texture) {
            this(Identifier.of(CriticalStrikeMod.ID, name), texture);
        }
        public Entry(Identifier id, Texture texture) {
            this(id, texture, createSimple());
        }
    }
    public static final ArrayList<Entry> ENTRIES = new ArrayList<>();
    private static Entry add(Entry simpleEntry) {
        ENTRIES.add(simpleEntry);
        return simpleEntry;
    }

    // Template particle entries
    public record TemplateEntry(Identifier id, Texture texture, TemplateParticleType particleType, Behaviour behaviour) {
        public TemplateEntry(String name, Texture texture, Behaviour behaviour) {
            this(Identifier.of(CriticalStrikeMod.ID, name), texture, new TemplateParticleType(), behaviour);
        }
    }

    public static final ArrayList<TemplateEntry> TEMPLATE_ENTRIES = new ArrayList<>();
    private static TemplateEntry addTemplate(TemplateEntry entry) {
        TEMPLATE_ENTRIES.add(entry);
        return entry;
    }

    // Template particles with variants - SPARKLE: SPARK shape, FLOAT motion, no animation, 8 ticks lifetime
    public static final TemplateEntry SPARKLE = addTemplate(new TemplateEntry(
        "sparkle",
            Texture.of("sparkle"),
            new Behaviour(Motion.DECELERATE, false, 10, 0.5F)
    ));
    public static final TemplateEntry SKULL = addTemplate(new TemplateEntry(
        "skull",
        Texture.of("skull"),
        new Behaviour(Motion.ASCEND, false, 20, 0.85F)
    ));
    public static final TemplateEntry CIRCLE = addTemplate(new TemplateEntry(
        "circle",
        Texture.of("circle"),
        new Behaviour(Motion.FLOAT, true, 16, 2F)
                .grow(0.08F)
                .fade(0.05F)
                .overlayScale(0.95F)
    ));

    public static void register() {
        for (var entry : ENTRIES) {
            Registry.register(Registries.PARTICLE_TYPE, entry.id, entry.particleType);
        }
        for (var entry : TEMPLATE_ENTRIES) {
            Registry.register(Registries.PARTICLE_TYPE, entry.id, entry.particleType);
        }
    }
}
