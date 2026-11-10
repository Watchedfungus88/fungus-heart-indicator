package dev.fungus.hearts.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.fungus.hearts.HeartRenderer;
import dev.fungus.hearts.NameTagVisibility;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin implements NameTagVisibility {
    @Shadow protected abstract boolean shouldShowName(Entity entity, double distanceSquared);

    @Override
    public boolean fungus$canShowName(Entity entity, double distanceSquared) {
        return shouldShowName(entity, distanceSquared);
    }

    @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN"))
    private void fungus$extract(Entity entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {

        HeartRenderer.extract(entity, cir.getReturnValue(), partialTick, this);
    }

    @Inject(method = "submit", at = @At("TAIL"))
    private void fungus$submit(EntityRenderState state, PoseStack poses, SubmitNodeCollector collector,
                               CameraRenderState camera, CallbackInfo ci) {
        HeartRenderer.submit(state, poses, collector, camera.orientation);
    }
}
