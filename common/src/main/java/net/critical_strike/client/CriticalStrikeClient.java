package net.critical_strike.client;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.internal.ClientConfig;
import net.tiny_config.ConfigManager;

public class CriticalStrikeClient {
    public static final ConfigManager<ClientConfig> config = new ConfigManager<>("client_config", new ClientConfig())
            .builder()
            .setDirectory(CriticalStrikeMod.ID)
            .sanitize(true)
            .build();

    public static void init() {
        config.refresh();
    }
}
