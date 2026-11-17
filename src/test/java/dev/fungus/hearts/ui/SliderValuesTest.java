package dev.fungus.hearts.ui;

import dev.fungus.hearts.HeartConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SliderValuesTest {
    @Test void everyFiniteDistanceAndUnlimitedMapBackToTheirExactStep() {
        for (int step = 0; step <= SliderValues.DISTANCE_STEPS; step++) {
            int distance = SliderValues.distance(step);
            assertEquals(step, SliderValues.distanceStep(distance));
            if (step < SliderValues.DISTANCE_STEPS) assertEquals(8 + step, distance);
        }
        assertEquals(128, SliderValues.distance(SliderValues.DISTANCE_STEPS - 1));
        assertEquals(HeartConfig.UNLIMITED_DISTANCE, SliderValues.distance(SliderValues.DISTANCE_STEPS));
        assertEquals(24, SliderValues.distanceStep(32));
    }

    @Test void everySizePercentageMapsBackWithoutRoundingDrift() {
        for (int step = 0; step <= SliderValues.SIZE_STEPS; step++) {
            float size = SliderValues.size(step);
            assertEquals(step, SliderValues.sizeStep(size));
            assertEquals(50 + step, Math.round(size * 100));
        }
        assertEquals(0.5f, SliderValues.size(0));
        assertEquals(1.5f, SliderValues.size(100));
        assertEquals(50, SliderValues.sizeStep(1));
    }

    @Test void everyHeartLimitAndUnlimitedMapBackToTheirExactStep() {
        for (int step = 0; step <= SliderValues.HEART_LIMIT_STEPS; step++) {
            int limit = SliderValues.heartLimit(step);
            assertEquals(step, SliderValues.heartLimitStep(limit));
            if (step < SliderValues.HEART_LIMIT_STEPS) assertEquals(step + 1, limit);
        }
        assertEquals(HeartConfig.MIN_HEART_LIMIT, SliderValues.heartLimit(0));
        assertEquals(HeartConfig.MAX_HEART_LIMIT, SliderValues.heartLimit(SliderValues.HEART_LIMIT_STEPS - 1));
        assertEquals(HeartConfig.UNLIMITED_HEARTS, SliderValues.heartLimit(SliderValues.HEART_LIMIT_STEPS));
        assertEquals(1, SliderValues.heartLimit(-5));
        assertEquals(HeartConfig.UNLIMITED_HEARTS, SliderValues.heartLimit(Integer.MAX_VALUE));
        assertEquals(0, SliderValues.heartLimitStep(-5));
        assertEquals(99, SliderValues.heartLimitStep(Integer.MAX_VALUE));
    }

    @Test void invalidValuesClampWithoutTurningNegativeDistancesIntoUnlimited() {
        assertEquals(0, SliderValues.distanceStep(-5));
        assertEquals(SliderValues.DISTANCE_STEPS - 1, SliderValues.distanceStep(Integer.MAX_VALUE));
        assertEquals(0.5f, SliderValues.size(-100));
        assertEquals(1.5f, SliderValues.size(1000));
        assertEquals(50, SliderValues.sizeStep(Float.NaN));
    }
}
