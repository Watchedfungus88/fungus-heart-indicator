package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

final class TestBuffers {
    private TestBuffers() {}
    static BufferBuilder create(ByteBufferBuilder storage) {
        return new BufferBuilder(storage, VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);
    }
}
