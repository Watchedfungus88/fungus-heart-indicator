package dev.fungus.hearts.ui;

import dev.fungus.hearts.HeartConfig;
import java.util.function.Predicate;

public final class SettingsAutosave {
    private static final long DELAY_MILLIS = 300;
    private final HeartConfig draft;
    private final Predicate<HeartConfig> save;
    private boolean dirty;
    private boolean failed;
    private long deadline;

    public SettingsAutosave(HeartConfig draft, Predicate<HeartConfig> save) {
        this.draft = draft;
        this.save = save;
    }

    public void changed(long now) {
        dirty = true;
        failed = false;
        deadline = now + DELAY_MILLIS;
    }

    public void tick(long now) {
        if (dirty && !failed && now >= deadline) flush();
    }

    public boolean flush() {
        if (!dirty) return true;
        if (!save.test(draft)) {
            failed = true;
            return false;
        }
        dirty = failed = false;
        return true;
    }

    public boolean failed() { return failed; }
}
