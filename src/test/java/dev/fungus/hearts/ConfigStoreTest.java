package dev.fungus.hearts;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ConfigStoreTest {
    @TempDir Path directory;

    @Test void missingAndPartialSettingsKeepDefaults() throws Exception {
        Path path = directory.resolve("settings.json");
        ConfigStore store = new ConfigStore(path);
        assertTrue(store.load().enabled);
        assertFalse(Files.exists(path));
        Files.writeString(path, "{\"animations\":false}");
        HeartConfig config = store.load();
        assertFalse(config.animations);
        assertTrue(config.enabled);
        assertEquals(32, config.distance);
        assertEquals(1, config.scale);
    }

    @Test void writesRoundTripWithoutTemporaryFiles() throws Exception {
        Path path = directory.resolve("settings.json");
        ConfigStore store = new ConfigStore(path);
        HeartConfig config = new HeartConfig();
        config.enabled = false;
        config.showSelf = true;
        config.distance = 64;
        config.scale = 1.25f;
        store.save(config);
        HeartConfig loaded = store.load();
        assertFalse(loaded.enabled);
        assertTrue(loaded.showSelf);
        assertEquals(64, loaded.distance);
        assertEquals(1.25f, loaded.scale);
        try (var files = Files.list(directory)) { assertEquals(1, files.count()); }
    }

    @Test void invalidSettingsAreNotOverwritten() throws Exception {
        Path path = directory.resolve("settings.json");
        Files.writeString(path, "{broken");
        assertThrows(Exception.class, () -> new ConfigStore(path).load());
        assertEquals("{broken", Files.readString(path));
        Files.writeString(path, "null");
        assertThrows(Exception.class, () -> new ConfigStore(path).load());
    }

    @Test void outOfRangeValuesAreNormalized() throws Exception {
        Path path = directory.resolve("settings.json");
        Files.writeString(path, "{\"distance\":9999,\"scale\":-5}");
        HeartConfig config = new ConfigStore(path).load();
        assertEquals(128, config.distance);
        assertEquals(0.5f, config.scale);
        config.scale = Float.NaN;
        config.distance = -100;
        config.normalize();
        assertEquals(1, config.scale);
        assertEquals(8, config.distance);
    }

    @Test void copyingDraftDoesNotChangeLiveSettings() {
        HeartConfig live = new HeartConfig();
        HeartConfig draft = live.copy();
        draft.enabled = false;
        assertTrue(live.enabled);
    }

    @Test void unlimitedAndSliderEndpointsSurviveRoundTrips() throws Exception {
        ConfigStore store = new ConfigStore(directory.resolve("settings.json"));
        for (int distance : new int[] {0, 8, 32, 64, 128}) {
            for (float size : new float[] {0.5f, 1, 1.5f}) {
                HeartConfig config = new HeartConfig();
                config.distance = distance;
                config.scale = size;
                store.save(config);
                HeartConfig loaded = store.load();
                assertEquals(distance, loaded.distance);
                assertEquals(size, loaded.scale);
            }
        }
    }
}
