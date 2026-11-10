package dev.fungus.hearts;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.function.IntFunction;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HeartSpritesTest {
    @Test void everySpriteExactlyMatchesTheActualVanillaHeartTypeEnum() throws Exception {
        Class<?> vanilla = Class.forName(System.getProperty("hearts.vanillaHeartType"));
        Object[] types = vanilla.getEnumConstants();
        Method sprite = vanilla.getDeclaredMethod("getSprite", boolean.class, boolean.class, boolean.class);
        sprite.setAccessible(true);
        for (HeartStyle style : HeartStyle.values()) {
            for (int flags = 0; flags < 8; flags++) {
                boolean hardcore = (flags & 4) != 0, half = (flags & 2) != 0, blink = (flags & 1) != 0;
                Identifier expected = (Identifier) sprite.invoke(types[style.ordinal()], hardcore, half, blink);
                assertEquals(expected, HeartSprites.id(style.slot(hardcore, half, blink)));
                assertEquals("minecraft", expected.getNamespace());
            }
        }
    }

    @Test void effectPrecedenceMatchesPoisonThenWitherThenFrozen() {
        for (int flags = 0; flags < 8; flags++) {
            boolean poison = (flags & 1) != 0, wither = (flags & 2) != 0, frozen = (flags & 4) != 0;
            assertEquals(poison ? HeartStyle.POISONED : wither ? HeartStyle.WITHERED
                    : frozen ? HeartStyle.FROZEN : HeartStyle.NORMAL, HeartStyle.forEffects(poison, wither, frozen));
        }
    }

    @Test void stableAtlasUsesOneLookupAndReloadRebuildsCoordinatesEvenAtTheSameAtlasSize() {
        AtomicInteger lookups = new AtomicInteger();
        Map<Integer, TextureAtlasSprite> generation = new HashMap<>();
        IntFunction<TextureAtlasSprite> getter = id -> {
            lookups.incrementAndGet();
            return generation.computeIfAbsent(id, unused -> sprite(0.1f, 0.2f));
        };
        HeartSprites.Cache cache = new HeartSprites.Cache();
        HeartSprites.Palette old = cache.get(getter);
        assertEquals(48, lookups.get());
        assertSame(old, cache.get(getter));
        assertEquals(49, lookups.get());
        generation.clear();
        TextureAtlasSprite newAnchor = sprite(0.6f, 0.8f);
        generation.put(0, newAnchor);
        HeartSprites.Palette replacement = cache.get(getter);
        assertNotSame(old, replacement);
        assertEquals(97, lookups.get());
        assertEquals(0.1f, old.u0(0));
        assertEquals(0.6f, replacement.u0(0));
        assertSame(replacement, cache.get(getter));
        assertEquals(98, lookups.get());
    }

    private TextureAtlasSprite sprite(float u0, float u1) {
        TextureAtlasSprite sprite = mock(TextureAtlasSprite.class);
        when(sprite.getU0()).thenReturn(u0);
        when(sprite.getU1()).thenReturn(u1);
        when(sprite.getV0()).thenReturn(0.25f);
        when(sprite.getV1()).thenReturn(0.5f);
        return sprite;
    }
}
