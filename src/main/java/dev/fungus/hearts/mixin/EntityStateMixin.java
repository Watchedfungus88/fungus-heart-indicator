package dev.fungus.hearts.mixin;

import dev.fungus.hearts.HeartFrame;
import dev.fungus.hearts.HeartState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityStateMixin implements HeartState {
    @Unique private HeartFrame fungus$hearts;

    @Override public HeartFrame fungus$getHearts() { return fungus$hearts; }
    @Override public void fungus$setHearts(HeartFrame frame) { fungus$hearts = frame; }
}
