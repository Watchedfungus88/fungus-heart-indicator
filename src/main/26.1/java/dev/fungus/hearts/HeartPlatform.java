package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;

public final class HeartPlatform {
    private static final SpriteId[] SPRITES = new SpriteId[48];

    static {
        for (int i = 0; i < SPRITES.length; i++) SPRITES[i] = new SpriteId(HeartSprites.ATLAS, HeartSprites.id(i));
    }

    private HeartPlatform() {}

    public static void rotate(PoseStack poses, Quaternionf orientation) { poses.mulPose(orientation); }

    public static int sneakingColor(Minecraft client) { return 0x80FFFFFF; }

    public static boolean hidden(Minecraft client) { return client.options.hideGui; }
    public static double nameRange(AbstractClientPlayer player) {
        return 64;
    }
    public static boolean hasScore(EntityRenderState state) { return state.scoreText != null; }
    public static TextureAtlasSprite sprite(Minecraft client, int slot) {
        return client.getAtlasManager().get(SPRITES[slot]);
    }
}
