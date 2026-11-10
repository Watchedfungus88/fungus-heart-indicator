package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.PoseStack;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeartFrameTest {
    static final HeartSprites.Palette PALETTE = palette();

    private static HeartSprites.Palette palette() {
        float[] uv = new float[48 * 4];
        for (int i = 0; i < 48; i++) {
            uv[i * 4] = i / 64f;
            uv[i * 4 + 1] = 0.25f;
            uv[i * 4 + 2] = (i + 0.5f) / 64f;
            uv[i * 4 + 3] = 0.5f;
        }
        return new HeartSprites.Palette(new Object(), uv);
    }

    static HeartFrame frame(float health, float max, float absorption, float previous,
                            long tick, boolean blink, HeartStyle style, boolean regeneration,
                            boolean hardcore, boolean animations) {
        return new HeartFrame(health, max, absorption, previous, tick, blink, style,
                regeneration, hardcore, animations, 1, PALETTE);
    }

    @Test void damageUsesActualBlinkingSpritesUnderTheCurrentHalfHeart() {
        HeartFrame frame = frame(13, 20, 0, 20, 100, true, HeartStyle.NORMAL, false, false, true);
        assertEquals("hud/heart/container_blinking", HeartSprites.id(frame.layer(6, 0)).getPath());
        assertEquals("hud/heart/full_blinking", HeartSprites.id(frame.layer(6, 1)).getPath());
        assertEquals("hud/heart/half", HeartSprites.id(frame.layer(6, 2)).getPath());
        assertEquals(-1, frame.layer(7, 2));
        assertTrue(frame.flashing());
    }

    @Test void disablingAnimationsDisablesBlinkingJitterAndRegenerationBounce() {
        HeartFrame frame = frame(2, 20, 0, 20, 0, true, HeartStyle.NORMAL, true, false, false);
        assertFalse(frame.flashing());
        for (int i = 0; i < frame.count(); i++) {
            assertEquals(0, frame.offset(i));
            assertEquals(-1, frame.layer(i, 1));
        }
    }

    @Test void regenerationMovesOnlyTheSelectedRegularHeartAndHasTheVanillaRestPeriod() {
        HeartFrame frame = frame(20, 20, 4, 20, 3, false, HeartStyle.NORMAL, true, false, true);
        for (int i = 0; i < frame.count(); i++) assertEquals(i == 3 ? -2 : 0, frame.offset(i));
        frame = frame(20, 20, 4, 20, 12, false, HeartStyle.NORMAL, true, false, true);
        for (int i = 0; i < frame.count(); i++) assertEquals(0, frame.offset(i));
    }

    @Test void absorptionPreventsLowHealthJitterLikeTheVanillaHud() {
        HeartFrame protectedPlayer = frame(2, 20, 4, 2, 100, false, HeartStyle.NORMAL, false, false, true);
        for (int i = 0; i < protectedPlayer.count(); i++) assertEquals(0, protectedPlayer.offset(i));
        HeartFrame low = frame(2, 20, 0, 2, 100, false, HeartStyle.NORMAL, false, false, true);
        boolean moved = false;
        for (int i = 0; i < low.count(); i++) {
            assertTrue(low.offset(i) == 0 || low.offset(i) == 1);
            moved |= low.offset(i) == 1;
        }
        assertTrue(moved);
    }

    @Test void hardcoreAndWitheredAbsorptionUseTheVanillaFamilies() {
        HeartFrame withered = frame(19, 20, 3, 20, 100, false, HeartStyle.WITHERED, false, true, true);
        assertEquals("hud/heart/container_hardcore", HeartSprites.id(withered.layer(9, 0)).getPath());
        assertEquals("hud/heart/withered_hardcore_half", HeartSprites.id(withered.layer(9, 2)).getPath());
        assertEquals("hud/heart/withered_hardcore_full", HeartSprites.id(withered.layer(10, 2)).getPath());
        assertEquals("hud/heart/withered_hardcore_half", HeartSprites.id(withered.layer(11, 2)).getPath());
        HeartFrame poisoned = frame(19, 20, 3, 20, 100, false, HeartStyle.POISONED, false, true, true);
        assertEquals("hud/heart/absorbing_hardcore_half", HeartSprites.id(poisoned.layer(11, 2)).getPath());
    }

    @Test void headlessDrawCallbackCoversAllStatesAndTheTrueWorstCase() {
        for (HeartStyle style : new HeartStyle[] {HeartStyle.NORMAL, HeartStyle.POISONED,
                HeartStyle.WITHERED, HeartStyle.FROZEN}) {
            for (boolean hardcore : new boolean[] {false, true}) {
                for (boolean blink : new boolean[] {false, true}) {
                    for (float health : new float[] {0.1f, 1, 13, 19, 20, 40, 41, 1024, 1_000_000}) {
                        float maximum = Math.max(20, health);
                        HeartFrame frame = frame(health, maximum, 1_000_000, maximum, 3,
                                blink, style, true, hardcore, true);
                        CountingVertices output = new CountingVertices();
                        frame.render(new PoseStack().last(), output);
                        int expected = 0;
                        for (int i = 0; i < frame.count(); i++) {
                            for (int layer = 0; layer < 3; layer++) if (frame.layer(i, layer) >= 0) expected += 4;
                        }
                        assertEquals(expected, output.vertices);
                        assertTrue(output.vertices <= 320);
                        assertTrue(output.validUvs && output.finitePositions);
                        assertEquals(output.vertices, output.colors);
                        assertEquals(output.vertices, output.lights);
                        assertEquals(0, output.normals + output.overlays);
                    }
                }
            }
        }
        HeartFrame worst = frame(1_000_000, 1_000_000, 1_000_000, 1_000_000,
                3, true, HeartStyle.FROZEN, true, true, true);
        CountingVertices output = new CountingVertices();
        worst.render(new PoseStack().last(), output);
        assertEquals(320, output.vertices);
    }
}
