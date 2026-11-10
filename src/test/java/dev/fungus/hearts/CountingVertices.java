package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.VertexConsumer;

final class CountingVertices implements VertexConsumer {
    int vertices, colors, normals, lights, overlays;
    boolean validUvs = true, finitePositions = true;
    @Override public VertexConsumer addVertex(float x, float y, float z) {
        vertices++;
        finitePositions &= Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z);
        return this;
    }
    @Override public VertexConsumer setColor(int r, int g, int b, int a) { colors++; return this; }
    @Override public VertexConsumer setColor(int color) { colors++; return this; }
    @Override public VertexConsumer setUv(float u, float v) {
        validUvs &= u >= 0 && u <= 1 && v >= 0 && v <= 1;
        return this;
    }
    @Override public VertexConsumer setUv1(int u, int v) { overlays++; return this; }
    @Override public VertexConsumer setUv2(int u, int v) { lights++; return this; }
    public VertexConsumer setUv3(float u, float v) { return this; }
    @Override public VertexConsumer setNormal(float x, float y, float z) { normals++; return this; }
    @Override public VertexConsumer setLineWidth(float width) { return this; }
}
