package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeartVertexBufferTest {
    @Test void translucentColorReachesEveryVertexAcrossAllLayersAtTheMaximumFiniteLimits() {
        for (int color : new int[] {0x80FFFFFF, 0x7FFFFFFF, 0xDFFFFFFF, -1}) {
            HeartFrame frame = new HeartFrame(1_000_000, 1_000_000, 1_000_000, 1_000_000,
                    3, true, HeartStyle.FROZEN, true, true, true, 1, HeartFrameTest.PALETTE, 100, 100, color);
            try (ByteBufferBuilder storage = new ByteBufferBuilder(16384)) {
                BufferBuilder builder = TestBuffers.create(storage);
                frame.render(new PoseStack().last(), builder);
                try (var mesh = builder.buildOrThrow()) {
                    assertEquals(2000, mesh.drawState().vertexCount());
                    int stride = DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR.getVertexSize();
                    for (int vertex = 0; vertex < 2000; vertex++) {
                        int offset = vertex * stride + stride - 4;
                        assertEquals(255, Byte.toUnsignedInt(mesh.vertexBuffer().get(offset)));
                        assertEquals(255, Byte.toUnsignedInt(mesh.vertexBuffer().get(offset + 1)));
                        assertEquals(255, Byte.toUnsignedInt(mesh.vertexBuffer().get(offset + 2)));
                        assertEquals(color >>> 24, Byte.toUnsignedInt(mesh.vertexBuffer().get(offset + 3)));
                    }
                }
            }
        }
    }

    @Test void actualMinecraftBufferBuilderDrawsUnlimitedHeartsPastBothFiniteSliderLimits() {
        HeartFrame frame = HeartFrameTest.frame(1001, 1001, 301, 1001,
                3, true, HeartStyle.FROZEN, true, true, true);
        try (ByteBufferBuilder storage = new ByteBufferBuilder(16384)) {
            BufferBuilder builder = TestBuffers.create(storage);
            frame.render(new PoseStack().last(), builder);
            try (var mesh = builder.buildOrThrow()) {
                assertEquals(7220, mesh.drawState().vertexCount());
                assertEquals(7220 * DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR.getVertexSize(),
                        mesh.vertexBuffer().remaining());
            }
        }
    }
}
