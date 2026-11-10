package dev.fungus.hearts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HeartIndicator implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fungus_heart_indicator");
    private static final ConfigStore STORE = new ConfigStore(
            FabricLoader.getInstance().getConfigDir().resolve("fungus-heart-indicator.json"));
    private static HeartConfig config = new HeartConfig();

    @Override
    public void onInitializeClient() {
        try {
            config = STORE.load();
        } catch (Exception exception) {
            LOGGER.warn("Could not load heart indicator settings; using defaults. The file was not changed.", exception);
        }
    }

    public static HeartConfig config() {
        return config;
    }

    public static boolean save(HeartConfig next) {
        try {
            STORE.save(next);
            config = next.copy();
            return true;
        } catch (Exception exception) {
            LOGGER.warn("Could not save heart indicator settings", exception);
            return false;
        }
    }
}
