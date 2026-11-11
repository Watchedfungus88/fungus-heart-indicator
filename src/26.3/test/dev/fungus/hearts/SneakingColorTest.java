package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SneakingColorTest {
    private void field(Object target, String name, Object value) throws Exception {
        var field = target.getClass().getField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test void opacityMatchesActualMinecraftSneakingNameTagsForEveryBackgroundSetting() throws Exception {
        Minecraft client = mock(Minecraft.class);
        GameRenderer renderer = mock(GameRenderer.class);
        GameRenderState state = mock(GameRenderState.class);
        OptionsRenderState options = new OptionsRenderState();
        field(client, "font", mock(Font.class));
        field(client, "gameRenderer", renderer);
        field(state, "optionsRenderState", options);
        when(renderer.gameRenderState()).thenReturn(state);
        SubmitNodeCollection nativeNames = mock(SubmitNodeCollection.class, CALLS_REAL_METHODS);
        SimpleFeatureRenderPhase phase = mock(SimpleFeatureRenderPhase.class);
        field(nativeNames, "nameTags", phase);
        CameraRenderState camera = new CameraRenderState();
        camera.orientation = new Quaternionf();
        try (var minecraft = mockStatic(Minecraft.class)) {
            minecraft.when(Minecraft::getInstance).thenReturn(client);
            for (boolean chatOnly : new boolean[] {false, true}) {
                options.backgroundForChatOnly = chatOnly;
                for (int opacity = 0; opacity <= 100; opacity++) {
                    options.textBackgroundOpacity = opacity / 100f;
                    clearInvocations(phase);
                    nativeNames.submitNameTag(new PoseStack(), Vec3.ZERO, 0,
                            Component.literal("Player"), false, 0xF000F0, camera);
                    ArgumentCaptor<TextFeatureRenderer.Submit> submitted =
                            ArgumentCaptor.forClass(TextFeatureRenderer.Submit.class);
                    verify(phase).submit(submitted.capture());
                    var text = (TextFeatureRenderer.Content.Text) submitted.getValue().content();
                    assertEquals(text.color(), HeartPlatform.sneakingColor(client),
                            "chatOnly=" + chatOnly + ", opacity=" + opacity);
                    assertEquals(Font.DisplayMode.NORMAL, submitted.getValue().displayMode());
                }
            }
        }
    }
}
