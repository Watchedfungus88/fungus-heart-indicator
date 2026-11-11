package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;

public final class HeartFrame implements SubmitNodeCollector.CustomGeometryRenderer {
    private final int health, previous, absorption, normalCount, absorptionCount;
    private final int normalUnits, absorptionUnits, normalBase, absorptionBase, container;
    private final int regenerationIndex, jitterSeed;
    private final boolean animations, flashing;
    private final float scale;
    private final int color;
    private final HeartSprites.Palette sprites;

    public HeartFrame(float health, float maximum, float absorption, float previous, long tick,
                      boolean flashing, HeartStyle style, boolean regeneration, boolean hardcore,
                      boolean animations, float scale, HeartSprites.Palette sprites) {
        this(health, maximum, absorption, previous, tick, flashing, style, regeneration, hardcore,
                animations, scale, sprites, -1);
    }

    public HeartFrame(float health, float maximum, float absorption, float previous, long tick,
                      boolean flashing, HeartStyle style, boolean regeneration, boolean hardcore,
                      boolean animations, float scale, HeartSprites.Palette sprites, int color) {
        this.color = color;
        maximum = Math.max(1, HeartLayout.finiteHealth(maximum));
        this.health = (int) Math.ceil(Math.min(maximum, HeartLayout.finiteHealth(health)));
        this.previous = (int) Math.ceil(Math.min(maximum, HeartLayout.finiteHealth(previous)));
        this.absorption = (int) Math.ceil(HeartLayout.finiteHealth(absorption));
        normalUnits = (int) HeartLayout.units(maximum, HeartLayout.MAX_NORMAL);
        absorptionUnits = (int) HeartLayout.units(this.absorption, HeartLayout.MAX_ABSORPTION);
        normalCount = HeartLayout.count(maximum, HeartLayout.MAX_NORMAL);
        absorptionCount = HeartLayout.count(this.absorption, HeartLayout.MAX_ABSORPTION);
        normalBase = style.slot(hardcore, false, false);

        absorptionBase = (style == HeartStyle.WITHERED ? style : HeartStyle.ABSORBING)
                .slot(hardcore, false, false);
        this.flashing = animations && flashing;
        container = HeartStyle.CONTAINER.slot(hardcore, false, this.flashing);
        regenerationIndex = animations && regeneration ? (int) (tick % (long) Math.ceil(maximum + 5)) : -1;
        jitterSeed = (int) tick * 312871;
        this.animations = animations;
        this.scale = scale;
        this.sprites = sprites;
    }

    public int normalCount() { return normalCount; }
    public int absorptionCount() { return absorptionCount; }
    public int count() { return normalCount + absorptionCount; }
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
        int seed = jitterSeed + index * 7349;
        float offset = health + absorption <= 4 ? (seed ^ seed >>> 7) & 1 : 0;
        return index < normalCount && index == regenerationIndex ? offset - 2 : offset;
    }

    @Override
    public void render(PoseStack.Pose pose, VertexConsumer vertices) {
        int count = count();
        for (int index = count - 1; index >= 0; index--) {
            float x = HeartLayout.x(index, count);
            float y = -12 - index / HeartLayout.PER_ROW * 10 + offset(index);
            for (int layer = 0; layer < 3; layer++) {
                int slot = layer(index, layer);
                if (slot >= 0) quad(pose, vertices, x, y, slot);
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

    private void vertex(PoseStack.Pose pose, VertexConsumer vertices,
                        float x, float y, float u, float v) {
        vertices.addVertex(pose, x, y, 0).setColor(color).setUv(u, v).setLight(0xF000F0);
    }
}
