package dev.fungus.hearts;

public final class HeartLayout {
    public static final int PER_ROW = 10;

    private HeartLayout() {}

    public static float finiteHealth(float value) {
        return Float.isFinite(value) ? Math.clamp(value, 0, 1_000_000) : 0;
    }

    public static float units(float maximum, int limit) {
        if (limit == HeartConfig.UNLIMITED_HEARTS) return 2;
        return Math.max(2, (float) Math.ceil(finiteHealth(maximum) / (limit * 2)) * 2);
    }

    public static int count(float maximum, int limit) {
        if (limit == HeartConfig.UNLIMITED_HEARTS) return (int) Math.ceil(finiteHealth(maximum) / 2);
        return Math.min(limit, (int) Math.ceil(finiteHealth(maximum) / units(maximum, limit)));
    }

    public static int fillPoints(int roundedHealth, int units, int index) {
        int remaining = roundedHealth - index * units;
        return remaining <= 0 ? 0 : remaining <= units / 2 ? 1 : 2;
    }

    public static int rowHeight(int count) {
        int rows = Math.max(1, (count + PER_ROW - 1) / PER_ROW);
        return Math.max(10 - (rows - 2), 3);
    }

    public static float x(int index, int count) {
        int columns = Math.min(PER_ROW, count - index / PER_ROW * PER_ROW);
        return index % PER_ROW * 8 - (columns * 8 + 1) / 2.0f;
    }
}
