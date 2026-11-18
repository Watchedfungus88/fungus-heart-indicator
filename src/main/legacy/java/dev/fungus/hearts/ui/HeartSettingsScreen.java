package dev.fungus.hearts.ui;

import dev.fungus.hearts.HeartConfig;
import dev.fungus.hearts.HeartFrame;
import dev.fungus.hearts.HeartIndicator;
import dev.fungus.hearts.HeartSprites;
import dev.fungus.hearts.HeartStyle;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
        top = Math.max(52, (height - 180) / 2);
        int left = width / 2 - 130;
        toggle(left, top, "enabled", draft.enabled, button -> {
            draft.enabled = !draft.enabled;
            button.setMessage(label("enabled", draft.enabled));
            changed();
        });
        Button animations = toggle(left, top + 22, "animations", draft.animations, button -> {
            draft.animations = !draft.animations;
            button.setMessage(label("animations", draft.animations));
            changed();
        });
        animations.setTooltip(Tooltip.create(text("animations_tip")));
        toggle(left, top + 44, "self", draft.showSelf, button -> {
            draft.showSelf = !draft.showSelf;
            button.setMessage(label("self", draft.showSelf));
            changed();
        }).setTooltip(Tooltip.create(text("self_tip")));
        SettingSlider distance = addRenderableWidget(new SettingSlider(left, top + 66, 260,
                SliderValues.DISTANCE_STEPS, SliderValues.distanceStep(draft.distance),
                step -> SliderValues.distance(step) == HeartConfig.UNLIMITED_DISTANCE
                        ? text("distance_unlimited") : text("distance", SliderValues.distance(step)),
                step -> { draft.distance = SliderValues.distance(step); changed(); }));
        distance.setTooltip(Tooltip.create(text("distance_tip")));
        SettingSlider normalHearts = addRenderableWidget(new SettingSlider(left, top + 88, 260,
                SliderValues.HEART_LIMIT_STEPS, SliderValues.heartLimitStep(draft.normalHeartLimit),
                step -> SliderValues.heartLimit(step) == HeartConfig.UNLIMITED_HEARTS
                        ? text("normal_hearts_unlimited") : text("normal_hearts", SliderValues.heartLimit(step)),
                step -> { draft.normalHeartLimit = SliderValues.heartLimit(step); changed(); }));
        normalHearts.setTooltip(Tooltip.create(text("heart_limit_tip")));
        SettingSlider absorptionHearts = addRenderableWidget(new SettingSlider(left, top + 110, 260,
                SliderValues.HEART_LIMIT_STEPS, SliderValues.heartLimitStep(draft.absorptionHeartLimit),
                step -> SliderValues.heartLimit(step) == HeartConfig.UNLIMITED_HEARTS
                        ? text("absorption_hearts_unlimited") : text("absorption_hearts", SliderValues.heartLimit(step)),
                step -> { draft.absorptionHeartLimit = SliderValues.heartLimit(step); changed(); }));
        absorptionHearts.setTooltip(Tooltip.create(text("heart_limit_tip")));
        SettingSlider size = addRenderableWidget(new SettingSlider(left, top + 132, 260,
                SliderValues.SIZE_STEPS, SliderValues.sizeStep(draft.scale),
                step -> text("size", Math.round(SliderValues.size(step) * 100)),
                step -> { draft.scale = SliderValues.size(step); changed(); }));
        size.setTooltip(Tooltip.create(text("size_tip")));
        addRenderableWidget(Button.builder(text("entities"), button -> {
            if (autosave.flush()) minecraft.setScreenAndShow(
                    new EntitySettingsScreen(minecraft, this, draft, autosave));
        }).bounds(left, top + 154, 260, 20).build());
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, top - 44, 0xFFFFFFFF);
        long time = System.nanoTime() / 50_000_000L % 160;
        boolean damaged = time < 80;
        HeartFrame preview = new HeartFrame(damaged ? 13 : 17, 20, 4, damaged ? 20 : 13,
                time, damaged && time % 80 < 20 && (20 - time % 80) / 3 % 2 == 1,
                HeartStyle.NORMAL, !damaged, false, draft.animations, draft.scale,
                HeartSprites.get(), draft.normalHeartLimit, draft.absorptionHeartLimit, -1);
        float scale = draft.scale;
        graphics.pose().pushMatrix();
        graphics.pose().translate(width / 2.0f, top - 18);
        graphics.pose().scale(scale, scale);
        int count = preview.normalCount() + preview.absorptionCount();
        for (int i = count - 1; i >= 0; i--) {
            int x = Math.round(dev.fungus.hearts.HeartLayout.x(i, count));
            int y = -i / dev.fungus.hearts.HeartLayout.PER_ROW * preview.rowHeight()
                    + Math.round(preview.offset(i));
            for (int layer = 0; layer < 3; layer++) {
                int sprite = preview.layer(i, layer);
                if (sprite >= 0) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HeartSprites.id(sprite), x, y, 9, 9);
            }
        }
        graphics.pose().popMatrix();
        if (autosave.failed()) graphics.drawCenteredString(font, text("save_failed"), width / 2, top + 178, 0xFFFF7777);
    }

    @Override public void onClose() {
        if (autosave.flush()) minecraft.setScreenAndShow(parent);
    }

    @Override public void removed() {
        autosave.flush();
        super.removed();
    }
}
