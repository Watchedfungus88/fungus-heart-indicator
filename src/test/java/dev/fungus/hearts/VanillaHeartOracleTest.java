package dev.fungus.hearts;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VanillaHeartOracleTest {
    private final Map<Identifier, BufferedImage> images = new HashMap<>();
    private final Object[] vanilla;
    private final Method getSprite;

    VanillaHeartOracleTest() throws Exception {
        Class<?> type = Class.forName(System.getProperty("hearts.vanillaHeartType"));
        vanilla = type.getEnumConstants();
        getSprite = type.getDeclaredMethod("getSprite", boolean.class, boolean.class, boolean.class);
        getSprite.setAccessible(true);
    }

    @Test void composedPixelsMatchVanillaHudLayersForEveryStyleAndFlag() throws Exception {
        int cases = 0;
        for (int type : new int[] {1, 2, 3, 5}) {
            for (boolean hardcore : new boolean[] {false, true}) {
                for (boolean blink : new boolean[] {false, true}) {
                    for (float health : new float[] {0.1f, 1, 2, 3, 13, 19, 20}) {
                        for (int absorption : new int[] {0, 1, 3, 4, 20}) {
                            HeartFrame actual = HeartFrameTest.frame(health, 20, absorption, 20, 3,
                                    blink, HeartStyle.values()[type], false, hardcore, true);
                            int current = (int) Math.ceil(health);
                            for (int index = 0; index < 10 + (absorption + 1) / 2; index++) {
                                BufferedImage expected = new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
                                draw(expected, vanillaSprite(0, hardcore, false, blink));
                                int halves = index * 2;

                                if (index >= 10) {
                                    int absorptionHalves = halves - 20;
                                    if (absorptionHalves < absorption) {
                                        draw(expected, vanillaSprite(type == 3 ? 3 : 4, hardcore,
                                                absorptionHalves + 1 == absorption, false));
                                    }
                                }
                                if (blink && halves < 20) draw(expected,
                                        vanillaSprite(type, hardcore, halves + 1 == 20, true));
                                if (halves < current) draw(expected,
                                        vanillaSprite(type, hardcore, halves + 1 == current, false));
                                BufferedImage result = compose(actual, index);
                                assertArrayEquals(expected.getRGB(0, 0, 9, 9, null, 0, 9),
                                        result.getRGB(0, 0, 9, 9, null, 0, 9),
                                        "type=" + type + ", hardcore=" + hardcore + ", blink=" + blink
                                                + ", health=" + health + ", absorption=" + absorption + ", heart=" + index);
                            }
                            cases++;
                        }
                    }
                }
            }
        }
        assertEquals(560, cases);
    }

    private Identifier vanillaSprite(int type, boolean hardcore, boolean half, boolean blink) throws Exception {
        return (Identifier) getSprite.invoke(vanilla[type], hardcore, half, blink);
    }

    private BufferedImage compose(HeartFrame frame, int index) throws Exception {
        BufferedImage image = new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
        for (int layer = 0; layer < 3; layer++) {
            int slot = frame.layer(index, layer);
            if (slot >= 0) draw(image, HeartSprites.id(slot));
        }
        return image;
    }

    private void draw(BufferedImage target, Identifier id) throws Exception {
        BufferedImage source = images.get(id);
        if (source == null) {
            String path = "/assets/minecraft/textures/gui/sprites/" + id.getPath() + ".png";
            try (var stream = getClass().getResourceAsStream(path)) {
                assertNotNull(stream, "Missing actual vanilla asset: " + path);
                source = ImageIO.read(stream);
            }
            assertEquals(9, source.getWidth());
            assertEquals(9, source.getHeight());
            images.put(id, source);
        }
        var graphics = target.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
    }
}
