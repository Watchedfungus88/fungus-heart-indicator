package dev.fungus.hearts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import com.mojang.blaze3d.platform.InputConstants;
import dev.fungus.hearts.ui.HeartSettingsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HeartIndicator implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fungus_heart_indicator");
    private static final ConfigStore STORE = new ConfigStore(
            FabricLoader.getInstance().getConfigDir().resolve("fungus-heart-indicator.json"));
    // opens the settings without requiring an optional menu mod
    public static final KeyMapping OPEN_SETTINGS = new KeyMapping(
            "key.fungus_heart_indicator.settings", InputConstants.KEY_H,
            KeyMapping.Category.register(Identifier.withDefaultNamespace("fungus_heart_indicator")));
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

    public static void handleSettingsKey() {
        if (!OPEN_SETTINGS.consumeClick()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && !HeartPlatform.hasScreen(client)) {
            client.setScreenAndShow(new HeartSettingsScreen(client, null));
        }
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
