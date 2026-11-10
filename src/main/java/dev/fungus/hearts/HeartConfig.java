package dev.fungus.hearts;

public final class HeartConfig {
    public static final int MIN_DISTANCE = 8;
    public static final int MAX_DISTANCE = 128;
    public static final int UNLIMITED_DISTANCE = 0;
    public boolean enabled = true;
    public boolean animations = true;
    public boolean showSelf = false;
    public int distance = 32;
    public float scale = 1.0f;

    public void normalize() {
        if (distance != UNLIMITED_DISTANCE) distance = Math.clamp(distance, MIN_DISTANCE, MAX_DISTANCE);
        scale = Float.isFinite(scale) ? Math.clamp(scale, 0.5f, 1.5f) : 1.0f;
    }

    public HeartConfig copy() {
        HeartConfig result = new HeartConfig();
        result.enabled = enabled;
        result.animations = animations;
        result.showSelf = showSelf;
        result.distance = distance;
        result.scale = scale;
        result.normalize();
        return result;
    }
}
