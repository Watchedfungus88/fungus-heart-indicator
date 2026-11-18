package dev.fungus.hearts.mixin;

import dev.fungus.hearts.HeartIndicator;
import java.util.Arrays;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow @Final @Mutable private KeyMapping[] keyMappings;

    // register before options.txt is read so the player's binding survives a restart
    @Inject(method = "load", at = @At("HEAD"))
    private void fungus$addSettingsKey(CallbackInfo callback) {
        if (Arrays.stream(keyMappings).noneMatch(key -> key == HeartIndicator.OPEN_SETTINGS)) {
            keyMappings = Arrays.copyOf(keyMappings, keyMappings.length + 1);
            keyMappings[keyMappings.length - 1] = HeartIndicator.OPEN_SETTINGS;
        }
    }
}
