package dev.fungus.hearts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeartLayoutTest {
    @Test void normalHalfHeartsMatchVanillaRounding() {
        assertEquals(10, HeartLayout.count(20, 20));
        assertEquals(2, HeartLayout.fillPoints(20, 2, 9));
        assertEquals(1, HeartLayout.fillPoints(19, 2, 9));
        assertEquals(0, HeartLayout.fillPoints(18, 2, 9));
        assertEquals(1, HeartLayout.fillPoints(1, 2, 0));
        assertEquals(2, HeartLayout.fillPoints(2, 2, 0));
        assertEquals(0, HeartLayout.fillPoints(0, 2, 0));
    }

    @Test void hugeHealthAndAbsorptionStayBounded() {
        for (float max : new float[] {1, 19, 20, 21, 40, 41, 100, 10000, Float.MAX_VALUE}) {
            assertTrue(HeartLayout.count(max, 20) <= 20);
            assertTrue(HeartLayout.count(max, 10) <= 10);
            assertTrue(HeartLayout.units(max, 20) >= 2);
        }
        assertEquals(20, HeartLayout.count(1_000_000, 20));
        assertEquals(10, HeartLayout.count(1_000_000, 10));
        assertEquals(0, HeartLayout.count(Float.NaN, 20));
        assertEquals(0, HeartLayout.count(-10, 20));
    }

    @Test void eachRowIsCenteredIndependently() {
        assertEquals(-40.5f, HeartLayout.x(0, 12));
        assertEquals(31.5f, HeartLayout.x(9, 12));
        assertEquals(-8.5f, HeartLayout.x(10, 12));
        assertEquals(-0.5f, HeartLayout.x(11, 12));
    }
}
