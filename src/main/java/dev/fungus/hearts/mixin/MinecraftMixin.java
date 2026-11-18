package dev.fungus.hearts.mixin;

import dev.fungus.hearts.HeartIndicator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftAccess {
    @Accessor("screen")
    public abstract Screen fungus$getScreen();

    @Inject(method = "tick", at = @At("TAIL"))
    private void fungus$settingsKey(CallbackInfo callback) {
        // consume the key only while the game world is active
        HeartIndicator.handleSettingsKey();
    }
}
