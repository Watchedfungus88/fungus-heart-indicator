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

    @Test void existingSettingsAndNullSelectionEnableOnlyPlayers() throws Exception {
        ConfigStore store = new ConfigStore(directory.resolve("config.json"));
        for (String body : new String[] {"{}", "{\"showSelf\":true,\"distance\":0}", "{\"entities\":null}"}) {
            Files.writeString(directory.resolve("config.json"), body);
            assertEquals(Set.of("minecraft:player"), store.load().entities);
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
