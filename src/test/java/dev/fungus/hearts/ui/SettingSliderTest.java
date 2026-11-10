package dev.fungus.hearts.ui;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingSliderTest {
    private SettingSlider slider(int steps, int initial, List<Integer> changes) {
        return new SettingSlider(0, 0, 260, steps, initial,
                step -> Component.literal(Integer.toString(step)), changes::add);
    }

    private MouseButtonEvent mouse(double x) {
        return new MouseButtonEvent(x, 5, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
    }

    @Test void nativeMouseInputReachesBothEndsAndDoesNotWriteOnConstruction() {
        List<Integer> changes = new ArrayList<>();
        SettingSlider slider = slider(SliderValues.DISTANCE_STEPS, 24, changes);
        assertTrue(changes.isEmpty());
        slider.onClick(mouse(4), false);
        assertEquals(List.of(0), changes);
        slider.onClick(mouse(256), false);
        assertEquals(SliderValues.DISTANCE_STEPS, changes.getLast());
        assertEquals(Integer.toString(SliderValues.DISTANCE_STEPS), slider.getMessage().getString());
        slider.onClick(mouse(1000), false);
        assertEquals(2, changes.size());
    }

    @Test void nativeMouseDragUpdatesTheSizeContinuouslyInOnePercentSteps() {
        List<Integer> changes = new ArrayList<>();
        SettingSlider slider = slider(100, 0, changes);
        slider.onClick(mouse(4), false);
        for (int percent = 1; percent <= 100; percent++) {
            slider.mouseDragged(mouse(4 + percent * 252.0 / 100), 2.52, 0);
        }
        assertEquals(100, changes.size());
        for (int i = 0; i < 100; i++) assertEquals(i + 1, changes.get(i));
    }

    @Test void keyboardArrowsMoveExactlyOneLogicalStepAndClampAtUnlimited() {
        List<Integer> changes = new ArrayList<>();
        SettingSlider slider = slider(SliderValues.DISTANCE_STEPS, SliderValues.DISTANCE_STEPS - 1, changes);
        slider.keyPressed(new KeyEvent(InputConstants.KEY_SPACE, 0, 0));
        slider.keyPressed(new KeyEvent(InputConstants.KEY_RIGHT, 0, 0));
        assertEquals(List.of(SliderValues.DISTANCE_STEPS), changes);
        slider.keyPressed(new KeyEvent(InputConstants.KEY_RIGHT, 0, 0));
        assertEquals(1, changes.size());
        slider.keyPressed(new KeyEvent(InputConstants.KEY_LEFT, 0, 0));
        assertEquals(SliderValues.DISTANCE_STEPS - 1, changes.getLast());
    }
}
