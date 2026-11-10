package dev.fungus.hearts.ui;

import dev.fungus.hearts.ConfigStore;
import dev.fungus.hearts.HeartConfig;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class SettingsAutosaveTest {
    @TempDir Path directory;

    @Test void openingOrClosingWithoutChangesDoesNotWrite() {
        AtomicInteger writes = new AtomicInteger();
        SettingsAutosave save = new SettingsAutosave(new HeartConfig(), config -> {
            writes.incrementAndGet(); return true;
        });
        save.tick(10000);
        assertTrue(save.flush());
        assertEquals(0, writes.get());
    }

    @Test void rapidDraggingCoalescesAndSavesOnlyTheLatestValue() {
        HeartConfig draft = new HeartConfig();
        AtomicInteger writes = new AtomicInteger();
        SettingsAutosave save = new SettingsAutosave(draft, config -> {
            assertEquals(128, config.distance);
            writes.incrementAndGet();
            return true;
        });
        for (int i = 0; i < 100; i++) {
            draft.distance = 8 + i;
            save.changed(i * 5);
            save.tick(i * 5);
        }
        draft.distance = 128;
        save.changed(500);
        save.tick(799);
        assertEquals(0, writes.get());
        save.tick(800);
        save.tick(1000);
        assertTrue(save.flush());
        assertEquals(1, writes.get());
    }

    @Test void exitingMidDragFlushesWithoutWaitingAndDoesNotSaveTwice() throws Exception {
        HeartConfig draft = new HeartConfig();
        ConfigStore store = new ConfigStore(directory.resolve("settings.json"));
        AtomicInteger writes = new AtomicInteger();
        SettingsAutosave save = new SettingsAutosave(draft, config -> {
            try { store.save(config); writes.incrementAndGet(); return true; }
            catch (Exception exception) { throw new AssertionError(exception); }
        });
        draft.distance = 0;
        draft.scale = 1.5f;
        save.changed(0);
        assertTrue(save.flush());
        assertTrue(save.flush());
        assertEquals(1, writes.get());
        assertEquals(0, store.load().distance);
        assertEquals(1.5f, store.load().scale);
    }

    @Test void failureDoesNotLoopAndCanBeRetriedOnExitOrAfterAnotherEdit() {
        AtomicInteger attempts = new AtomicInteger();
        SettingsAutosave save = new SettingsAutosave(new HeartConfig(), config -> attempts.incrementAndGet() > 1);
        save.changed(0);
        save.tick(300);
        assertTrue(save.failed());
        for (int i = 0; i < 100; i++) save.tick(1000 + i * 1000);
        assertEquals(1, attempts.get());
        assertTrue(save.flush());
        assertFalse(save.failed());
        assertEquals(2, attempts.get());
        save.changed(200000);
        save.tick(200300);
        assertEquals(3, attempts.get());
    }
}
