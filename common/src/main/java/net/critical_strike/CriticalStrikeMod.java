package net.critical_strike;

import net.critical_strike.fx.CriticalStrikeSounds;
import net.critical_strike.internal.Config;
import net.tiny_config.ConfigManager;

public final class CriticalStrikeMod {
    public static final String ID = "critical_strike";

    public static ConfigManager<Config> config = new ConfigManager<>(ID, new Config())
            .builder()
            .setDirectory(ID)
            .sanitize(true)
            .build();

    public static final int CRIT_PACKET_CODE = 43;

    public static void init() {
        config.refresh();
    }

    public static void registerSounds() {
        CriticalStrikeSounds.register();
    }
}
