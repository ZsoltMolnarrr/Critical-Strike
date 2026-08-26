package net.critical_strike.fx;

import net.critical_strike.CriticalStrikeMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import java.util.ArrayList;
import java.util.List;

public class CriticalStrikeSounds {
    public static final class Entry {
        private final Identifier id;
        private final SoundEvent soundEvent;
        private Holder<SoundEvent> entry;
        private int variants = 1;

        public Entry(Identifier id, SoundEvent soundEvent) {
            this.id = id;
            this.soundEvent = soundEvent;
        }

        public Entry(String name) {
            this(Identifier.fromNamespaceAndPath(CriticalStrikeMod.ID, name));
        }

        public Entry(Identifier id) {
            this(id, SoundEvent.createVariableRangeEvent(id));
        }

        public Entry travelDistance(float distance) {
            return new Entry(id, SoundEvent.createFixedRangeEvent(id, distance));
        }

        public Entry variants(int variants) {
            this.variants = variants;
            return this;
        }

        public Identifier id() {
            return id;
        }

        public SoundEvent soundEvent() {
            return soundEvent;
        }

        public Holder<SoundEvent> entry() {
            return entry;
        }

        public int variants() {
            return variants;
        }

        public void register() {
            if (entry == null) {
                entry = Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id(), soundEvent());
            }
        }
    }

    public static final List<Entry> entries = new ArrayList<>();

    public static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    // MARK: Critical Strike sounds

    public static final Entry CRITICAL_HIT = add(new Entry("critical_hit").variants(2));

    public static void register() {
        for (var entry: entries) {
            entry.entry = Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, entry.id(), entry.soundEvent());
        }
    }
}
