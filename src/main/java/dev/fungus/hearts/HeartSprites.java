package dev.fungus.hearts;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.function.IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class HeartSprites {
    public static final Identifier ATLAS = Identifier.withDefaultNamespace("textures/atlas/gui.png");
    private static final Identifier[] IDS = new Identifier[48];
    private static final Cache CACHE = new Cache();
    private static final IntFunction<TextureAtlasSprite> LOOKUP = slot -> HeartPlatform.sprite(Minecraft.getInstance(), slot);

    static {
        for (HeartStyle style : HeartStyle.values()) {
            for (int flags = 0; flags < 8; flags++) {
                IDS[style.ordinal() * 8 + flags] = Identifier.withDefaultNamespace(
                        style.path((flags & 4) != 0, (flags & 2) != 0, (flags & 1) != 0));
            }
        }
    }

    private HeartSprites() {}

    public static Identifier id(int slot) { return IDS[slot]; }
    public static Palette get() { return CACHE.get(LOOKUP); }

    public static final class Cache {
        private Palette palette;

        public Palette get(IntFunction<TextureAtlasSprite> getter) {
            TextureAtlasSprite anchor = getter.apply(0);

            if (palette == null || palette.anchor != anchor) {
                float[] coordinates = new float[IDS.length * 4];
                for (int i = 0; i < IDS.length; i++) {
                    TextureAtlasSprite sprite = i == 0 ? anchor : getter.apply(i);
                    coordinates[i * 4] = sprite.getU0();
                    coordinates[i * 4 + 1] = sprite.getV0();
                    coordinates[i * 4 + 2] = sprite.getU1();
                    coordinates[i * 4 + 3] = sprite.getV1();
                }
                palette = new Palette(anchor, coordinates);
            }
            return palette;
        }
    }

    public static final class Palette {
        private final Object anchor;
        private final float[] coordinates;

        Palette(Object anchor, float[] coordinates) {
            this.anchor = anchor;
            this.coordinates = coordinates;
        }

        public float u0(int slot) { return coordinates[slot * 4]; }
        public float v0(int slot) { return coordinates[slot * 4 + 1]; }
        public float u1(int slot) { return coordinates[slot * 4 + 2]; }
        public float v1(int slot) { return coordinates[slot * 4 + 3]; }
    }
}
