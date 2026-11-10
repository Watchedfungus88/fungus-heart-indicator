package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeartVertexBufferTest {
    @Test void actualMinecraftBufferBuilderAcceptsTheWorldTextVertexFormatAtTheMaximum() {
        HeartFrame frame = HeartFrameTest.frame(1_000_000, 1_000_000, 1_000_000, 1_000_000,
                3, true, HeartStyle.FROZEN, true, true, true);
        try (ByteBufferBuilder storage = new ByteBufferBuilder(16384)) {
            BufferBuilder builder = TestBuffers.create(storage);
            frame.render(new PoseStack().last(), builder);
            try (var mesh = builder.buildOrThrow()) {
                assertEquals(320, mesh.drawState().vertexCount());
                assertEquals(320 * DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR.getVertexSize(),
                        mesh.vertexBuffer().remaining());
            }
        }
    }
}
