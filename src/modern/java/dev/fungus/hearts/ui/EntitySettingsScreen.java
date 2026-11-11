package dev.fungus.hearts.ui;

import dev.fungus.hearts.HeartConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public final class EntitySettingsScreen extends EntitySelectionScreen {
    public EntitySettingsScreen(Minecraft minecraft, Screen parent, HeartConfig draft,
                                SettingsAutosave autosave) {
        this(minecraft, parent, draft, autosave, EntitySelection.registered());
    }

    EntitySettingsScreen(Minecraft minecraft, Screen parent, HeartConfig draft,
                         SettingsAutosave autosave, EntitySelection selection) {
        super(minecraft, parent, draft, autosave, selection);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 16, 0xFFFFFFFF);
        if (noMatches()) graphics.centeredText(font, text("no_entities"), width / 2, 80, 0xFFAAAAAA);
        graphics.centeredText(font, pageLabel(), pageLabelX(), height - 64, 0xFFFFFFFF);
        if (saveFailed()) graphics.centeredText(font, text("save_failed"), width / 2, height - 14, 0xFFFF7777);
    }
}
