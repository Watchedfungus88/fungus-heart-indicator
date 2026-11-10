package dev.fungus.hearts.ui;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class SettingSlider extends AbstractSliderButton {
    private final int steps;
    private final IntFunction<Component> label;
    private final IntConsumer changed;
    private int selected;

    public SettingSlider(int x, int y, int width, int steps, int initial,
                         IntFunction<Component> label, IntConsumer changed) {
        super(x, y, width, 20, CommonComponents.EMPTY, Math.clamp(initial, 0, steps) / (double) steps);
        this.steps = steps;
        this.selected = Math.clamp(initial, 0, steps);
        this.label = label;
        this.changed = changed;
        updateMessage();
    }

    @Override
    protected void updateMessage() { setMessage(label.apply(selected)); }

    @Override
    protected void applyValue() {
        int next = (int) Math.round(value * steps);
        if (next != selected) {
            selected = next;
            changed.accept(next);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (canChangeValue && (event.isLeft() || event.isRight())) {
            setValue((selected + (event.isLeft() ? -1 : 1)) / (double) steps);
            return true;
        }
        return super.keyPressed(event);
    }
}
