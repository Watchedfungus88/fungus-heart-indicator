package dev.fungus.hearts.ui;

import dev.fungus.hearts.HeartConfig;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HeartSettingsScreenTest {
    @Test void actualScreenHasThreeTogglesTwoSlidersAndAnEntitiesButton() {
        Minecraft minecraft = mock(Minecraft.class);
        HeartSettingsScreen screen = new HeartSettingsScreen(minecraft, null, new HeartConfig(), config -> true);
        screen.width = 320;
        screen.height = 240;
        screen.init();
        assertEquals(6, screen.children().size());
        assertEquals(4, screen.children().stream().filter(Button.class::isInstance).count());
        assertEquals(2, screen.children().stream().filter(SettingSlider.class::isInstance).count());
        assertEquals(0, screen.children().stream().filter(Button.class::isInstance)
                .map(Button.class::cast).filter(button -> {
                    String label = button.getMessage().getString();
                    return label.equals("Done") || label.equals("Cancel") || label.equals("Reset");
                }).count());
    }

    @Test void actualEscapeSavesTheLatestSliderValueAndReturnsToParentWithoutABackup() {
        Minecraft minecraft = mock(Minecraft.class);
        Screen parent = mock(Screen.class);
        HeartConfig draft = new HeartConfig();
        AtomicInteger writes = new AtomicInteger();
        HeartSettingsScreen screen = new HeartSettingsScreen(minecraft, parent, draft, config -> {
            assertEquals(0, config.distance);
            writes.incrementAndGet();
            return true;
        });
        screen.width = 320;
        screen.height = 240;
        screen.init();
        SettingSlider distance = (SettingSlider) screen.children().get(3);
        distance.onClick(new MouseButtonEvent(distance.getX() + 256, distance.getY() + 5,
                new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0)), false);
        assertEquals(0, draft.distance);
        assertTrue(screen.keyPressed(new KeyEvent(InputConstants.KEY_ESCAPE, 0, 0)));
        screen.removed();
        assertEquals(1, writes.get());
        verify(minecraft).setScreenAndShow(parent);
    }

    @Test void togglesAlsoSaveWithoutAnyDoneButton() {
        Minecraft minecraft = mock(Minecraft.class);
        HeartConfig draft = new HeartConfig();
        AtomicInteger writes = new AtomicInteger();
        HeartSettingsScreen screen = new HeartSettingsScreen(minecraft, null, draft, config -> {
            assertFalse(config.enabled);
            writes.incrementAndGet();
            return true;
        });
        screen.width = 320;
        screen.height = 240;
        screen.init();
        ((Button) screen.children().getFirst()).onPress(new KeyEvent(InputConstants.KEY_RETURN, 0, 0));
        screen.onClose();
        assertEquals(1, writes.get());
        verify(minecraft).setScreenAndShow(null);
    }

    @Test void saveFailureKeepsTheScreenOpenAndEscapeRetries() {
        Minecraft minecraft = mock(Minecraft.class);
        Screen parent = mock(Screen.class);
        AtomicInteger attempts = new AtomicInteger();
        HeartSettingsScreen screen = new HeartSettingsScreen(minecraft, parent, new HeartConfig(),
                config -> attempts.incrementAndGet() > 1);
        screen.width = 320;
        screen.height = 240;
        screen.init();
        ((Button) screen.children().getFirst()).onPress(new KeyEvent(InputConstants.KEY_RETURN, 0, 0));
        screen.keyPressed(new KeyEvent(InputConstants.KEY_ESCAPE, 0, 0));
        verify(minecraft, never()).setScreenAndShow(parent);
        screen.keyPressed(new KeyEvent(InputConstants.KEY_ESCAPE, 0, 0));
        verify(minecraft).setScreenAndShow(parent);
        assertEquals(2, attempts.get());
    }
}
