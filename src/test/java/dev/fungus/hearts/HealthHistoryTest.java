package dev.fungus.hearts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HealthHistoryTest {
    @Test void initialHealthNeverFlashes() {
        HealthHistory history = new HealthHistory();
        history.update(13, 20, 100);
        assertEquals(13, history.previous());
        assertFalse(history.flashing(100));
    }

    @Test void overlappingHitsKeepTheOriginalDisplayedBaseline() {
        HealthHistory history = new HealthHistory();
        history.update(20, 20, 0);
        history.update(16, 20, 1);
        history.update(12, 20, 2);
        assertEquals(20, history.previous());
        history.update(18, 20, 3);
        assertEquals(20, history.previous());
    }

    @Test void damageAndHealingUseVanillasTwentyAndTenTickBlinkWindows() {
        HealthHistory history = new HealthHistory();
        history.update(20, 20, 0);
        history.update(10, 20, 1);
        for (int age = 0; age <= 21; age++) {
            history.update(10, 20, 1 + age);
            int remaining = 20 - age;
            assertEquals(remaining > 0 && remaining / 3 % 2 == 1, history.flashing(1 + age));
        }
        history.update(15, 20, 23);
        for (int age = 0; age <= 11; age++) {
            history.update(15, 20, 23 + age);
            int remaining = 10 - age;
            assertEquals(remaining > 0 && remaining / 3 % 2 == 1, history.flashing(23 + age));
        }
    }

    @Test void stableHealthExpiresTheOldBaselineAndFractionalChangesDoNotRestartBlinking() {
        HealthHistory history = new HealthHistory();
        history.update(20, 20, 0);
        history.update(10.1f, 20, 1);
        history.update(10.5f, 20, 5);
        assertEquals(20, history.previous());
        assertEquals((20 - 4) / 3 % 2 == 1, history.flashing(5));
        history.update(10.5f, 20, 20);
        history.update(10.5f, 20, 22);
        assertEquals(11, history.previous());
        history.update(9, 20, 23);
        assertEquals(11, history.previous());
    }

    @Test void unseenPlayersMaxHealthChangesAndTimeRewindsSnapWithoutOldFlashes() {
        HealthHistory history = new HealthHistory();
        history.update(20, 20, 100);
        history.update(5, 20, 130);
        assertEquals(5, history.previous());
        assertFalse(history.flashing(130));
        history.update(40, 40, 131);
        assertEquals(40, history.previous());
        history.update(1, 40, 0);
        assertEquals(1, history.previous());
        assertFalse(history.flashing(0));
    }

    @Test void multipleExtractionsWithinATickDoNotRestartTheAnimation() {
        HealthHistory history = new HealthHistory();
        history.update(20, 20, 0);
        history.update(10, 20, 1);
        for (int frame = 0; frame < 100; frame++) history.update(10, 20, 1 + frame / 100.0);
        assertTrue(history.flashing(4));
        assertFalse(history.flashing(21));
    }
}
