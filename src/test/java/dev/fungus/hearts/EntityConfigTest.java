package dev.fungus.hearts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class EntityConfigTest {
    @TempDir Path directory;

    @Test void heartLimitsDefaultToUnlimitedAndNormalizeFiniteValues() {
        HeartConfig config = new HeartConfig();
        assertEquals(HeartConfig.UNLIMITED_HEARTS, config.normalHeartLimit);
        assertEquals(HeartConfig.UNLIMITED_HEARTS, config.absorptionHeartLimit);
        config.normalHeartLimit = -4;
        config.absorptionHeartLimit = 1000;
        config.normalize();
        assertEquals(HeartConfig.MIN_HEART_LIMIT, config.normalHeartLimit);
        assertEquals(HeartConfig.MAX_HEART_LIMIT, config.absorptionHeartLimit);
    }

    @Test void heartLimitsAreCopiedAndPersisted() throws Exception {
        ConfigStore store = new ConfigStore(directory.resolve("config.json"));
        HeartConfig config = new HeartConfig();
        config.normalHeartLimit = 40;
        config.absorptionHeartLimit = 12;
        HeartConfig copy = config.copy();
        config.normalHeartLimit = HeartConfig.UNLIMITED_HEARTS;
        assertEquals(40, copy.normalHeartLimit);
        store.save(copy);
        HeartConfig loaded = store.load();
        assertEquals(40, loaded.normalHeartLimit);
        assertEquals(12, loaded.absorptionHeartLimit);
        loaded.normalHeartLimit = HeartConfig.UNLIMITED_HEARTS;
        loaded.absorptionHeartLimit = HeartConfig.UNLIMITED_HEARTS;
        store.save(loaded);
        loaded = store.load();
        assertEquals(HeartConfig.UNLIMITED_HEARTS, loaded.normalHeartLimit);
        assertEquals(HeartConfig.UNLIMITED_HEARTS, loaded.absorptionHeartLimit);
    }

    @Test void existingSettingsAndNullSelectionEnableOnlyPlayers() throws Exception {
        ConfigStore store = new ConfigStore(directory.resolve("config.json"));
        for (String body : new String[] {"{}", "{\"showSelf\":true,\"distance\":0}", "{\"entities\":null}"}) {
            Files.writeString(directory.resolve("config.json"), body);
            HeartConfig config = store.load();
            assertEquals(Set.of("minecraft:player"), config.entities);
            assertEquals(HeartConfig.UNLIMITED_HEARTS, config.normalHeartLimit);
            assertEquals(HeartConfig.UNLIMITED_HEARTS, config.absorptionHeartLimit);
        }
    }

    @Test void individualEntityChoicesAndAllOffSurviveSaving() throws Exception {
        ConfigStore store = new ConfigStore(directory.resolve("config.json"));
        HeartConfig config = new HeartConfig();
        config.setEntityEnabled("minecraft:cow", true);
        config.setEntityEnabled("example:creature", true);
        config.setEntityEnabled("minecraft:player", false);
        store.save(config);
        assertEquals(Set.of("minecraft:cow", "example:creature"), store.load().entities);
        config.entities.clear();
        store.save(config);
        assertTrue(store.load().entities.isEmpty());
        try (var files = Files.list(directory)) { assertEquals(1, files.count()); }
    }

    @Test void draftAndSavedCopiesOwnTheirSelections() {
        HeartConfig live = new HeartConfig();
        HeartConfig draft = live.copy();
        draft.setEntityEnabled("minecraft:cow", true);
        draft.setEntityEnabled("minecraft:player", false);
        assertEquals(Set.of("minecraft:player"), live.entities);
        HeartConfig saved = draft.copy();
        draft.entities.clear();
        assertEquals(Set.of("minecraft:cow"), saved.entities);
    }

    @Test void invalidIdsAreRemovedWithoutDroppingUnknownModdedIds() {
        HeartConfig config = new HeartConfig();
        config.entities = new HashSet<>();
        config.entities.addAll(Set.of("minecraft:cow", "example:creature", "bad id", "UPPER:case", "pig"));
        config.entities.add(null);
        config.normalize();
        assertEquals(Set.of("minecraft:cow", "example:creature"), config.entities);
    }
}
