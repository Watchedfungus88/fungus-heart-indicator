package dev.fungus.hearts.ui;

import dev.fungus.hearts.HeartConfig;

public final class SliderValues {
    public static final int DISTANCE_STEPS = HeartConfig.MAX_DISTANCE - HeartConfig.MIN_DISTANCE + 1;
    public static final int HEART_LIMIT_STEPS = HeartConfig.MAX_HEART_LIMIT;
    public static final int SIZE_STEPS = 100;

    private SliderValues() {}

    public static int distance(int step) {
        step = Math.clamp(step, 0, DISTANCE_STEPS);
        return step == DISTANCE_STEPS ? HeartConfig.UNLIMITED_DISTANCE : HeartConfig.MIN_DISTANCE + step;
    }

    public static int distanceStep(int distance) {
        return distance == HeartConfig.UNLIMITED_DISTANCE ? DISTANCE_STEPS
                : Math.clamp(distance, HeartConfig.MIN_DISTANCE, HeartConfig.MAX_DISTANCE) - HeartConfig.MIN_DISTANCE;
    }

    public static int heartLimit(int step) {
        step = Math.clamp(step, 0, HEART_LIMIT_STEPS);
        return step == HEART_LIMIT_STEPS ? HeartConfig.UNLIMITED_HEARTS : HeartConfig.MIN_HEART_LIMIT + step;
    }

    public static int heartLimitStep(int limit) {
        return limit == HeartConfig.UNLIMITED_HEARTS ? HEART_LIMIT_STEPS
                : Math.clamp(limit, HeartConfig.MIN_HEART_LIMIT, HeartConfig.MAX_HEART_LIMIT)
                - HeartConfig.MIN_HEART_LIMIT;
    }

    public static float size(int step) {
        return (50 + Math.clamp(step, 0, SIZE_STEPS)) / 100f;
    }

    public static int sizeStep(float scale) {
        return Float.isFinite(scale) ? Math.clamp(Math.round(scale * 100) - 50, 0, SIZE_STEPS) : 50;
    }
}
