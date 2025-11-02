package net.critical_strike.fx;

import net.critical_strike.CriticalStrikeMod;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class CriticalStrikeParticles {
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

    public static final Entry SPARKLE = add(new Entry("sparkle", Texture.of("sparkle")));
    public static final Entry SKULL = add(new Entry("skull", Texture.of("skull")));
    public static final Entry CIRCLE = add(new Entry("circle", Texture.of("circle")));

    public static void register() {
        for (var entry : ENTRIES) {
            Registry.register(Registries.PARTICLE_TYPE, entry.id, entry.particleType);
        }
    }
}
