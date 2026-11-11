package dev.fungus.hearts.ui;

import dev.fungus.hearts.HeartConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public abstract class EntitySelectionScreen extends Screen {
    private final Screen parent;
    private final HeartConfig draft;
    private final SettingsAutosave autosave;
    private final EntitySelection selection;
    private final List<Button> rows = new ArrayList<>();
    private EditBox search;
    private Button previous, next;
    private String query = "";
    private int listTop, listBottom, left, listWidth;

    protected EntitySelectionScreen(Minecraft minecraft, Screen parent, HeartConfig draft,
                                    SettingsAutosave autosave, EntitySelection selection) {
        super(minecraft, minecraft.font, text("entities"));
        this.parent = parent;
        this.draft = draft;
        this.autosave = autosave;
        this.selection = selection;
    }

    protected static Component text(String key, Object... args) {
        return Component.translatable("fungus_hearts." + key, args);
    }

    @Override
    protected void init() {
        rows.clear();
        listWidth = Math.min(360, width - 40);
        left = (width - listWidth) / 2;
        listTop = 66;
        int rowCount = Math.clamp((height - 140) / 24, 1, 12);
        listBottom = listTop + rowCount * 24;
        selection.pageSize(rowCount);
        search = addRenderableWidget(new EditBox(font, left, 38, listWidth, 20, text("entity_search")));
        search.setMaxLength(128);
        search.setHint(text("entity_search"));
        search.setValue(query);
        search.setResponder(value -> {
            query = value;
            selection.search(value);
            refreshRows();
        });
        for (int i = 0; i < rowCount; i++) {
            int row = i;
            rows.add(addRenderableWidget(Button.builder(CommonComponents.EMPTY, button -> toggle(row))
                    .bounds(left, listTop + i * 24, listWidth, 20).build()));
        }
        int footer = height - 70;
        previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
                .bounds(width / 2 - 75, footer, 30, 20).build());
        previous.setTooltip(Tooltip.create(text("previous_page")));
        next = addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
                .bounds(width / 2 + 45, footer, 30, 20).build());
        next.setTooltip(Tooltip.create(text("next_page")));
        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> onClose())
                .bounds(width / 2 - 100, height - 40, 200, 20).build());
        refreshRows();
    }

    private void toggle(int row) {
        List<EntitySelection.Choice> choices = selection.rows();
        if (row >= choices.size()) return;
        String id = choices.get(row).id();
        draft.setEntityEnabled(id, !draft.entityEnabled(id));
        autosave.changed(now());
        refreshRows();
    }

    private void refreshRows() {
        List<EntitySelection.Choice> choices = selection.rows();
        for (int i = 0; i < rows.size(); i++) {
            Button button = rows.get(i);
            button.visible = button.active = i < choices.size();
            if (button.visible) {
                EntitySelection.Choice choice = choices.get(i);
                button.setMessage(choice.name().copy().append(": ").append(draft.entityEnabled(choice.id())
                        ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                button.setTooltip(Tooltip.create(Component.literal(choice.id())));
            }
        }
        if (previous != null) previous.active = selection.page() > 0;
        if (next != null) next.active = selection.page() + 1 < selection.pages();
    }

    private void changePage(int delta) {
        selection.move(delta);
        refreshRows();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= left && x < left + listWidth && y >= listTop && y < listBottom && vertical != 0) {
            changePage(vertical > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    @Override public void tick() { autosave.tick(now()); }

    @Override public void onClose() {
        if (autosave.flush()) minecraft.setScreenAndShow(parent);
    }

    @Override public void removed() {
        autosave.flush();
        super.removed();
    }

    protected Component pageLabel() { return Component.literal((selection.page() + 1) + " / " + selection.pages()); }
    protected int pageLabelX() { return width / 2; }
    protected boolean noMatches() { return selection.matches() == 0; }
    protected boolean saveFailed() { return autosave.failed(); }
    private static long now() { return System.nanoTime() / 1_000_000; }
}
