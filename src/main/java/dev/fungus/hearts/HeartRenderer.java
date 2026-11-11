package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

public final class HeartRenderer {
    private HeartRenderer() {}

    public static void extract(Entity entity, EntityRenderState state, float partialTick, NameTagVisibility names) {
        extract(entity, state, partialTick, names, HeartIndicator.config(), Minecraft.getInstance(), HeartSprites::get);
    }

    static void extract(Entity entity, EntityRenderState state, float partialTick, NameTagVisibility names,
                        HeartConfig config, Minecraft client, Supplier<HeartSprites.Palette> sprites) {
        HeartState target = (HeartState) state;
        target.fungus$setHearts(null);
        if (!config.enabled || !(entity instanceof LivingEntity living)
                || !config.entityEnabled(HeartEntityTypes.id(entity.getType()))) return;
        boolean self = entity == client.player;
        if (!HeartVisibility.visible(config.enabled, HeartPlatform.hidden(client), living.isInvisible(),
                living.isSpectator(), living.isAlive(), living.isDiscrete(), state.distanceToCameraSq,
                config.distance, self, config.showSelf, client.options.getCameraType().isFirstPerson(),
                true)) return;
        if (!self) {
            if (entity instanceof AbstractClientPlayer player) {
                if (state.nameTag == null && (!HeartVisibility.needsNameCheck(state.distanceToCameraSq,
                        HeartPlatform.nameRange(player), living.isDiscrete())
                        || !names.fungus$canShowName(entity, HeartVisibility.namePolicyDistance(
                                state.distanceToCameraSq, living.isDiscrete())))) return;
            } else if (!HeartEntityVisibility.allowed(living, client)) return;
        }
        float max = Math.max(1, HeartLayout.finiteHealth(living.getMaxHealth()));
        float health = Math.min(max, HeartLayout.finiteHealth(living.getHealth()));
        double time = (double) living.tickCount + partialTick;
        HealthHistory history = ((EntityHistory) living).fungus$history();
        history.update(health, max, time);
        HeartStyle style = HeartStyle.forEffects(living.hasEffect(MobEffects.POISON),
                living.hasEffect(MobEffects.WITHER), living.isFullyFrozen());
        target.fungus$setHearts(new HeartFrame(health, max,
                HeartLayout.finiteHealth(living.getAbsorptionAmount()), history.previous(),
                living.tickCount, history.flashing(time), style, living.hasEffect(MobEffects.REGENERATION),
                living instanceof AbstractClientPlayer && living.level().getLevelData().isHardcore(),
                config.animations, config.scale, sprites.get(),
                living.isDiscrete() ? HeartPlatform.sneakingColor(client) : -1));
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
        collector.submitCustomGeometry(poses, RenderTypes.text(HeartSprites.ATLAS), frame);
        poses.popPose();
    }
}
