package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

public final class HeartFrame implements SubmitNodeCollector.CustomGeometryRenderer {
    private final int health, previous, absorption, normalCount, absorptionCount;
    private final int normalUnits, absorptionUnits, normalBase, absorptionBase, container;
    private final int regenerationIndex, jitterSeed;
    private final boolean animations, flashing, jitter;
    private final int[] jitterOffsets;
    private final float scale;
    private final int color;
    private final HeartSprites.Palette sprites;

    public HeartFrame(float health, float maximum, float absorption, float previous, long tick,
                      boolean flashing, HeartStyle style, boolean regeneration, boolean hardcore,
                      boolean animations, float scale, HeartSprites.Palette sprites) {
        this(health, maximum, absorption, previous, tick, flashing, style, regeneration, hardcore,
                animations, scale, sprites, HeartConfig.UNLIMITED_HEARTS, HeartConfig.UNLIMITED_HEARTS, -1);
    }

    public HeartFrame(float health, float maximum, float absorption, float previous, long tick,
                      boolean flashing, HeartStyle style, boolean regeneration, boolean hardcore,
                      boolean animations, float scale, HeartSprites.Palette sprites, int color) {
        this(health, maximum, absorption, previous, tick, flashing, style, regeneration, hardcore,
                animations, scale, sprites, HeartConfig.UNLIMITED_HEARTS, HeartConfig.UNLIMITED_HEARTS, color);
    }

    public HeartFrame(float health, float maximum, float absorption, float previous, long tick,
                      boolean flashing, HeartStyle style, boolean regeneration, boolean hardcore,
                      boolean animations, float scale, HeartSprites.Palette sprites,
                      int normalLimit, int absorptionLimit, int color) {
        this.color = color;
        maximum = Math.max(1, HeartLayout.finiteHealth(maximum));
        this.health = (int) Math.ceil(Math.min(maximum, HeartLayout.finiteHealth(health)));
        this.previous = (int) Math.ceil(Math.min(maximum, HeartLayout.finiteHealth(previous)));
        this.absorption = (int) Math.ceil(HeartLayout.finiteHealth(absorption));
        normalUnits = (int) HeartLayout.units(maximum, normalLimit);
        absorptionUnits = (int) HeartLayout.units(this.absorption, absorptionLimit);
        normalCount = HeartLayout.count(maximum, normalLimit);
        absorptionCount = HeartLayout.count(this.absorption, absorptionLimit);
        normalBase = style.slot(hardcore, false, false);

        absorptionBase = (style == HeartStyle.WITHERED ? style : HeartStyle.ABSORBING)
                .slot(hardcore, false, false);
        this.flashing = animations && flashing;
        container = HeartStyle.CONTAINER.slot(hardcore, false, this.flashing);
        regenerationIndex = animations && regeneration ? (int) (tick % (long) Math.ceil(maximum + 5)) : -1;
        jitterSeed = (int) tick * 312871;
        this.animations = animations;
        jitter = animations && health + absorption <= 4;
        if (jitter) {
            jitterOffsets = new int[normalCount + absorptionCount];
            RandomSource random = RandomSource.create(jitterSeed);
            for (int index = jitterOffsets.length - 1; index >= 0; index--) {
                jitterOffsets[index] = random.nextInt(2);
            }
        } else {
            jitterOffsets = null;
        }
        this.scale = scale;
        this.sprites = sprites;
    }

    public int normalCount() { return normalCount; }
    public int absorptionCount() { return absorptionCount; }
    public int count() { return normalCount + absorptionCount; }
    public int rowHeight() { return HeartLayout.rowHeight(count()); }
    public float scale() { return scale; }
    public int color() { return color; }
    public boolean flashing() { return flashing; }

    public int layer(int index, int layer) {
        if (layer == 0) return container;
        if (layer == 1) {
            if (!flashing || index >= normalCount) return -1;
            int fill = HeartLayout.fillPoints(previous, normalUnits, index);
            return fill == 0 ? -1 : normalBase + (fill == 1 ? 2 : 0) + 1;
        }
        int fill = index < normalCount ? HeartLayout.fillPoints(health, normalUnits, index)
                : HeartLayout.fillPoints(absorption, absorptionUnits, index - normalCount);
        int base = index < normalCount ? normalBase : absorptionBase;
        return fill == 0 ? -1 : base + (fill == 1 ? 2 : 0);
    }

    public float offset(int index) {
        if (!animations) return 0;
        float offset = jitter ? jitterOffsets[index] : 0;
        return index < normalCount && index == regenerationIndex ? offset - 2 : offset;
    }

    @Override
    public void render(PoseStack.Pose pose, VertexConsumer vertices) {
        int count = count();
        // keep the common player-sized layout on the original fast path
        if (count <= 20) {
            renderSmall(pose, vertices, count, HeartLayout.rowHeight(count));
            return;
        }
        renderRows(pose.pose(), vertices, count, HeartLayout.rowHeight(count));
    }

    private void renderSmall(PoseStack.Pose pose, VertexConsumer vertices, int count, int rowHeight) {
        for (int index = count - 1; index >= 0; index--) {
            float x = HeartLayout.x(index, count);
            float y = -12 - index / HeartLayout.PER_ROW * rowHeight + offset(index);
            for (int layer = 0; layer < 3; layer++) {
                int slot = layer(index, layer);
                if (slot >= 0) quad(pose, vertices, x, y, slot);
            }
        }
    }

    private void renderRows(Matrix4f matrix, VertexConsumer vertices, int count, int rowHeight) {
        int rows = (count + HeartLayout.PER_ROW - 1) / HeartLayout.PER_ROW;
        float containerU0 = sprites.u0(container), containerV0 = sprites.v0(container);
        float containerU1 = sprites.u1(container), containerV1 = sprites.v1(container);
        for (int row = rows - 1; row >= 0; row--) {
            int first = row * HeartLayout.PER_ROW;
            int columns = Math.min(HeartLayout.PER_ROW, count - first);
            float left = -(columns * 8 + 1) / 2.0f;
            float yBase = -12 - row * rowHeight;
            for (int column = columns - 1; column >= 0; column--) {
                int index = first + column;
                float x = left + column * 8;
                float y = yBase + offset(index);
                quad(matrix, vertices, x, y, containerU0, containerV0, containerU1, containerV1);
                if (flashing && index < normalCount) {
                    int fill = HeartLayout.fillPoints(previous, normalUnits, index);
                    if (fill != 0) {
                        quad(matrix, vertices, x, y,
                                normalBase + (fill == 1 ? 2 : 0) + 1);
                    }
                }
                int fill = index < normalCount ? HeartLayout.fillPoints(health, normalUnits, index)
                        : HeartLayout.fillPoints(absorption, absorptionUnits, index - normalCount);
                if (fill != 0) {
                    int base = index < normalCount ? normalBase : absorptionBase;
                    quad(matrix, vertices, x, y, base + (fill == 1 ? 2 : 0));
                }
            }
        }
    }

    private void quad(PoseStack.Pose pose, VertexConsumer vertices, float x, float y, int slot) {
        float u0 = sprites.u0(slot), v0 = sprites.v0(slot);
        float u1 = sprites.u1(slot), v1 = sprites.v1(slot);
        vertex(pose, vertices, x, y, u0, v0);
        vertex(pose, vertices, x, y + 9, u0, v1);
        vertex(pose, vertices, x + 9, y + 9, u1, v1);
        vertex(pose, vertices, x + 9, y, u1, v0);
    }

    private void quad(Matrix4f matrix, VertexConsumer vertices, float x, float y, int slot) {
        float u0 = sprites.u0(slot), v0 = sprites.v0(slot);
        float u1 = sprites.u1(slot), v1 = sprites.v1(slot);
        quad(matrix, vertices, x, y, u0, v0, u1, v1);
    }

    private void quad(Matrix4f matrix, VertexConsumer vertices, float x, float y,
                      float u0, float v0, float u1, float v1) {
        vertex(matrix, vertices, x, y, u0, v0);
        vertex(matrix, vertices, x, y + 9, u0, v1);
        vertex(matrix, vertices, x + 9, y + 9, u1, v1);
        vertex(matrix, vertices, x + 9, y, u1, v0);
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer vertices,
                        float x, float y, float u, float v) {
        vertices.addVertex(pose, x, y, 0).setColor(color).setUv(u, v).setLight(0xF000F0);
    }

    private void vertex(Matrix4f matrix, VertexConsumer vertices,
                        float x, float y, float u, float v) {
        float px = matrix.m00() * x + matrix.m10() * y + matrix.m30();
        float py = matrix.m01() * x + matrix.m11() * y + matrix.m31();
        float pz = matrix.m02() * x + matrix.m12() * y + matrix.m32();
        vertices.addVertex(px, py, pz)
                .setColor(color).setUv(u, v).setLight(0xF000F0);
    }
}
