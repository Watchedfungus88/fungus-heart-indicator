package dev.fungus.hearts;

import java.util.HashSet;
import java.util.Set;

public final class HeartConfig {
    public static final int MIN_DISTANCE = 8;
    public static final int MAX_DISTANCE = 128;
    public static final int UNLIMITED_DISTANCE = 0;
    public static final int MIN_HEART_LIMIT = 1;
    public static final int MAX_HEART_LIMIT = 100;
    public static final int UNLIMITED_HEARTS = 0;
    public boolean enabled = true;
    public boolean animations = true;
    public boolean showSelf = false;
    public int distance = 32;
    public int normalHeartLimit = UNLIMITED_HEARTS;
    public int absorptionHeartLimit = UNLIMITED_HEARTS;
    public float scale = 1.0f;
    public Set<String> entities = new HashSet<>(Set.of("minecraft:player"));

    public boolean entityEnabled(String id) { return entities.contains(id); }

    public void setEntityEnabled(String id, boolean enabled) {
        if (enabled) entities.add(id);
        else entities.remove(id);
    }

    public void normalize() {
        if (distance != UNLIMITED_DISTANCE) distance = Math.clamp(distance, MIN_DISTANCE, MAX_DISTANCE);
        if (normalHeartLimit != UNLIMITED_HEARTS) {
            normalHeartLimit = Math.clamp(normalHeartLimit, MIN_HEART_LIMIT, MAX_HEART_LIMIT);
        }
        if (absorptionHeartLimit != UNLIMITED_HEARTS) {
            absorptionHeartLimit = Math.clamp(absorptionHeartLimit, MIN_HEART_LIMIT, MAX_HEART_LIMIT);
        }
        scale = Float.isFinite(scale) ? Math.clamp(scale, 0.5f, 1.5f) : 1.0f;
        if (entities == null) entities = new HashSet<>(Set.of("minecraft:player"));
        else {
            entities = new HashSet<>(entities);
            entities.removeIf(id -> id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+"));
        }
    }

    public HeartConfig copy() {
        HeartConfig result = new HeartConfig();
        result.enabled = enabled;
        result.animations = animations;
        result.showSelf = showSelf;
        result.distance = distance;
        result.normalHeartLimit = normalHeartLimit;
        result.absorptionHeartLimit = absorptionHeartLimit;
        result.scale = scale;
        result.entities = entities == null ? null : new HashSet<>(entities);
        result.normalize();
        return result;
    }
}
