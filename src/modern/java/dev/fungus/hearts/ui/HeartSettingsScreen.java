package dev.fungus.hearts.ui;

import dev.fungus.hearts.HeartConfig;
import dev.fungus.hearts.HeartFrame;
import dev.fungus.hearts.HeartIndicator;
import dev.fungus.hearts.HeartSprites;
import dev.fungus.hearts.HeartStyle;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class HeartSettingsScreen extends Screen {
    private final Screen parent;
    private final HeartConfig draft;
    private final SettingsAutosave autosave;
    private int top;

    public HeartSettingsScreen(Minecraft minecraft, Screen parent) {
        this(minecraft, parent, HeartIndicator.config().copy(), HeartIndicator::save);
    }

    HeartSettingsScreen(Minecraft minecraft, Screen parent, HeartConfig draft, Predicate<HeartConfig> save) {
        super(minecraft, minecraft.font, text("title"));
        this.parent = parent;
        this.draft = draft;
        this.autosave = new SettingsAutosave(draft, save);
    }

    private static Component text(String key, Object... args) {
        return Component.translatable("fungus_hearts." + key, args);
    }

    @Override
    protected void init() {
        top = Math.max(64, (height - 140) / 2);
        int left = width / 2 - 130;
        toggle(left, top, "enabled", draft.enabled, button -> {
            draft.enabled = !draft.enabled;
            button.setMessage(label("enabled", draft.enabled));
            changed();
        });
        toggle(left, top + 24, "animations", draft.animations, button -> {
            draft.animations = !draft.animations;
            button.setMessage(label("animations", draft.animations));
            changed();
        });
        toggle(left, top + 48, "self", draft.showSelf, button -> {
            draft.showSelf = !draft.showSelf;
            button.setMessage(label("self", draft.showSelf));
            changed();
        }).setTooltip(Tooltip.create(text("self_tip")));
        SettingSlider distance = addRenderableWidget(new SettingSlider(left, top + 72, 260,
                SliderValues.DISTANCE_STEPS, SliderValues.distanceStep(draft.distance),
                step -> SliderValues.distance(step) == HeartConfig.UNLIMITED_DISTANCE
                        ? text("distance_unlimited") : text("distance", SliderValues.distance(step)),
                step -> { draft.distance = SliderValues.distance(step); changed(); }));
        distance.setTooltip(Tooltip.create(text("distance_tip")));
        addRenderableWidget(new SettingSlider(left, top + 96, 260,
                SliderValues.SIZE_STEPS, SliderValues.sizeStep(draft.scale),
                step -> text("size", Math.round(SliderValues.size(step) * 100)),
                step -> { draft.scale = SliderValues.size(step); changed(); }));
        addRenderableWidget(Button.builder(text("entities"), button -> {
            if (autosave.flush()) minecraft.setScreenAndShow(
                    new EntitySettingsScreen(minecraft, this, draft, autosave));
        }).bounds(left, top + 120, 260, 20).build());
    }

    private static long now() { return System.nanoTime() / 1_000_000; }
    private void changed() { autosave.changed(now()); }

    @Override public void tick() { autosave.tick(now()); }
    private Component label(String key, boolean enabled) {
        return text(key).copy().append(": ").append(enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private Button toggle(int x, int y, String key, boolean value, Button.OnPress onPress) {
        return addRenderableWidget(Button.builder(label(key, value), onPress).bounds(x, y, 260, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, top - 56, 0xFFFFFFFF);
        long time = System.nanoTime() / 50_000_000L % 160;
        boolean damaged = time < 80;
        HeartFrame preview = new HeartFrame(damaged ? 13 : 17, 20, 4, damaged ? 20 : 13,
                time, damaged && time % 80 < 20 && (20 - time % 80) / 3 % 2 == 1,
                HeartStyle.NORMAL, !damaged, false, draft.animations, draft.scale,
                HeartSprites.get());
        float scale = draft.scale;
        graphics.pose().pushMatrix();
        graphics.pose().translate(width / 2.0f, top - 22);
        graphics.pose().scale(scale, scale);
        int count = preview.normalCount() + preview.absorptionCount();
        for (int i = count - 1; i >= 0; i--) {
            int x = Math.round(dev.fungus.hearts.HeartLayout.x(i, count));
            int y = -i / 10 * 10 + Math.round(preview.offset(i));
            for (int layer = 0; layer < 3; layer++) {
                int sprite = preview.layer(i, layer);
                if (sprite >= 0) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HeartSprites.id(sprite), x, y, 9, 9);
            }
        }
        graphics.pose().popMatrix();
        graphics.centeredText(font, text(autosave.failed() ? "save_failed" : "auto_saved"),
                width / 2, top + 152, autosave.failed() ? 0xFFFF7777 : 0xFFAAAAAA);
    }

    @Override public void onClose() {
        if (autosave.flush()) minecraft.setScreenAndShow(parent);
    }

    @Override public void removed() {
        autosave.flush();
        super.removed();
    }
}
