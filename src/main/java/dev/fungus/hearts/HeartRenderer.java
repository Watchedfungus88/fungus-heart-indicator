package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Quaternionf;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;

public final class HeartRenderer {
    private static final RenderType HEARTS = RenderTypes.text(HeartSprites.ATLAS);
    private HeartRenderer() {}

    public static void extract(Entity entity, EntityRenderState state, float partialTick, NameTagVisibility names) {
        if (!(state instanceof AvatarRenderState) || !(entity instanceof AbstractClientPlayer player)) return;
        HeartState target = (HeartState) state;
        target.fungus$setHearts(null);
        HeartConfig config = HeartIndicator.config();
        if (!config.enabled) return;
        Minecraft client = Minecraft.getInstance();
        boolean self = player == client.player;
        if (!HeartVisibility.visible(config.enabled, HeartPlatform.hidden(client), player.isInvisible(),
                player.isSpectator(), player.isAlive(), player.isDiscrete(), state.distanceToCameraSq,
                config.distance, self, config.showSelf, client.options.getCameraType().isFirstPerson(),
                true)) return;
        if (!self && state.nameTag == null) {

            double nameRange = HeartPlatform.nameRange(player);
            if (!HeartVisibility.beyondNameRange(state.distanceToCameraSq, nameRange)
                    || !names.fungus$canShowName(player, state.distanceToCameraSq)) return;
        }
        float max = Math.max(1, HeartLayout.finiteHealth(player.getMaxHealth()));
        float health = Math.min(max, HeartLayout.finiteHealth(player.getHealth()));
        double time = (double) player.tickCount + partialTick;
        HealthHistory history = ((PlayerHistory) player).fungus$history();
        history.update(health, max, time);
        HeartStyle style = HeartStyle.forEffects(player.hasEffect(MobEffects.POISON),
                player.hasEffect(MobEffects.WITHER), player.isFullyFrozen());
        target.fungus$setHearts(new HeartFrame(health, max,
                HeartLayout.finiteHealth(player.getAbsorptionAmount()), history.previous(),
                player.tickCount, history.flashing(time), style, player.hasEffect(MobEffects.REGENERATION),
                player.level().getLevelData().isHardcore(), config.animations, config.scale,
                HeartSprites.get()));
    }

    public static void submit(EntityRenderState state, PoseStack poses,
                              SubmitNodeCollector collector, Quaternionf orientation) {
        if (!(state instanceof HeartState hearts)) return;
        HeartFrame frame = hearts.fungus$getHearts();
        if (frame == null) return;
        poses.pushPose();
        if (state.nameTagAttachment != null) {
            poses.translate(state.nameTagAttachment.x, state.nameTagAttachment.y + 0.5,
                    state.nameTagAttachment.z);
        } else {
            poses.translate(0, state.boundingBoxHeight + 0.5, 0);
        }
        if (HeartPlatform.hasScore(state)) poses.translate(0, 9 * 1.15f * 0.025f, 0);
        HeartPlatform.rotate(poses, orientation);
        poses.scale(0.025f * frame.scale(), -0.025f * frame.scale(), 0.025f * frame.scale());
        collector.submitCustomGeometry(poses, HEARTS, frame);
        poses.popPose();
    }
}
