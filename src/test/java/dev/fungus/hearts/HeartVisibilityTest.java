package dev.fungus.hearts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeartVisibilityTest {
    private boolean visible(boolean enabled, boolean hidden, boolean invisible, boolean spectator,
                            boolean alive, boolean sneaking, double distance,
                            boolean self, boolean showSelf, boolean firstPerson, boolean nameVisible) {
        return HeartVisibility.visible(enabled, hidden, invisible, spectator, alive, sneaking,
                distance, 32, self, showSelf, firstPerson, nameVisible);
    }

    @Test void everyHideRuleIndependentlySuppressesTheIndicator() {
        assertTrue(visible(true, false, false, false, true, false, 1, false, false, true, true));
        assertFalse(visible(false, false, false, false, true, false, 1, false, false, true, true));
        assertFalse(visible(true, true, false, false, true, false, 1, false, false, true, true));
        assertFalse(visible(true, false, true, false, true, false, 1, false, false, true, true));
        assertFalse(visible(true, false, false, true, true, false, 1, false, false, true, true));
        assertFalse(visible(true, false, false, false, false, false, 1, false, false, true, true));
        assertFalse(visible(true, false, false, false, true, true, 1, false, false, true, true));
        assertFalse(visible(true, false, false, false, true, false, 1, false, false, true, false));
    }

    @Test void rangeBoundaryAndInvalidCameraDistancesAreHandled() {
        assertTrue(visible(true, false, false, false, true, false, 1024, false, false, true, true));
        assertFalse(visible(true, false, false, false, true, false, 1024.01, false, false, true, true));
        for (double distance : new double[] {Double.NaN, Double.POSITIVE_INFINITY}) {
            assertFalse(visible(true, false, false, false, true, false, distance, false, false, true, true));
        }
    }

    @Test void selfRequiresTheToggleAndThirdPersonButNotAnOwnNameTag() {
        for (boolean show : new boolean[] {false, true}) {
            for (boolean firstPerson : new boolean[] {false, true}) {
                assertEquals(show && !firstPerson, visible(true, false, false, false, true,
                        false, 1, true, show, firstPerson, false));
            }
        }
    }

    @Test void unlimitedRemovesOnlyTheModRangeCapAndKeepsTheOtherVisibilityRules() {
        assertTrue(HeartVisibility.visible(true, false, false, false, true, false,
                1_000_000_000, 0, false, false, true, true));
        assertFalse(HeartVisibility.visible(true, true, false, false, true, false,
                1_000_000_000, 0, false, false, true, true));
        assertFalse(HeartVisibility.visible(true, false, true, false, true, false,
                1_000_000_000, 0, false, false, true, true));
        assertFalse(HeartVisibility.visible(true, false, false, false, true, false,
                1_000_000_000, 0, false, false, true, false));
        assertFalse(HeartVisibility.visible(true, false, false, false, true, false,
                Double.NaN, 0, false, false, true, true));
        assertTrue(HeartVisibility.visible(true, false, false, false, true, false,
                128 * 128, 128, false, false, true, true));
        assertFalse(HeartVisibility.visible(true, false, false, false, true, false,
                128 * 128 + 1, 128, false, false, true, true));
    }

    @Test void missingNameTagsAreRecheckedOnlyBeyondTheActualVanillaRange() {
        assertFalse(HeartVisibility.beyondNameRange(64 * 64 - 1, 64));
        assertTrue(HeartVisibility.beyondNameRange(64 * 64, 64));
        assertTrue(HeartVisibility.beyondNameRange(128 * 128, 64));
        assertFalse(HeartVisibility.beyondNameRange(16 * 16 - 1, 16));
        assertTrue(HeartVisibility.beyondNameRange(16 * 16, 16));
        assertFalse(HeartVisibility.beyondNameRange(10000, 0));
        assertFalse(HeartVisibility.beyondNameRange(10000, Double.NaN));
    }
}
