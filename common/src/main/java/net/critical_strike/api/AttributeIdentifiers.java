package net.critical_strike.api;

import net.critical_strike.CriticalStrikeMod;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Identifiers of the attribute modifiers this mod applies.
 * <p>
 * 1.20.1 keys attribute modifiers by {@link UUID} + display name instead of {@link Identifier};
 * the UUIDs are derived deterministically from the modern identifiers via {@link #uuid(Identifier)},
 * so they are stable across worlds and match what a 1.21 world would call the same modifier.
 */
public class AttributeIdentifiers {
    public static Identifier INNATE_BONUS = new Identifier(CriticalStrikeMod.ID, "innate_bonus");
    public static Identifier EFFECT_BONUS = new Identifier(CriticalStrikeMod.ID, "effect_bonus");

    public static final UUID INNATE_BONUS_UUID = uuid(INNATE_BONUS);
    public static final UUID EFFECT_BONUS_UUID = uuid(EFFECT_BONUS);

    /** Deterministic modifier UUID for a modern-style modifier identifier. */
    public static UUID uuid(Identifier id) {
        return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** Modifier display name for a modern-style modifier identifier. */
    public static String name(Identifier id) {
        return id.toString();
    }
}
