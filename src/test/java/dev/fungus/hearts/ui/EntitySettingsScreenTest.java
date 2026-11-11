package dev.fungus.hearts.ui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.fungus.hearts.HeartConfig;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntitySettingsScreenTest {
    private org.mockito.MockedStatic<Minecraft> currentClient;

    @org.junit.jupiter.api.AfterEach void closeClient() {
        if (currentClient != null) currentClient.close();
    }

    private Minecraft client() throws Exception {
        Minecraft client = mock(Minecraft.class);
        currentClient = mockStatic(Minecraft.class);
        currentClient.when(Minecraft::getInstance).thenReturn(client);
        when(client.getLastInputType()).thenReturn(net.minecraft.client.InputType.KEYBOARD_TAB);
        Font font = mock(Font.class);
        when(font.plainSubstrByWidth(anyString(), anyInt())).thenAnswer(call -> call.getArgument(0));
        when(font.plainSubstrByWidth(anyString(), anyInt(), anyBoolean())).thenAnswer(call -> call.getArgument(0));
        var fontField = Minecraft.class.getField("font");
        fontField.setAccessible(true);
        fontField.set(client, font);
        return client;
    }

    private EntitySelection choices(int extra) {
        var choices = new java.util.ArrayList<>(List.of(
                new EntitySelection.Choice("minecraft:player", net.minecraft.network.chat.Component.literal("Player")),
                new EntitySelection.Choice("minecraft:cow", net.minecraft.network.chat.Component.literal("Cow"))));
        IntStream.range(0, extra).forEach(i -> choices.add(new EntitySelection.Choice(
                "example:mob_" + i, net.minecraft.network.chat.Component.literal("Mob " + i))));
        return new EntitySelection(choices);
    }

    private EntitySettingsScreen screen(Minecraft client, HeartConfig draft, SettingsAutosave autosave,
                                        EntitySelection choices) {
        EntitySettingsScreen screen = new EntitySettingsScreen(client, null, draft, autosave, choices);
        screen.width = 320;
        screen.height = 240;
        screen.resize(320, 240);
        return screen;
    }

    private Button row(EntitySettingsScreen screen, int index) {
        return (Button) screen.children().get(index + 1);
    }

    private void press(Button button) { button.onPress(new KeyEvent(InputConstants.KEY_RETURN, 0, 0)); }

    private Button button(EntitySettingsScreen screen, net.minecraft.network.chat.Component label) {
        return screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getMessage().getString().equals(label.getString())).findFirst().orElseThrow();
    }

    @Test void paginationIsCenteredWithBackBelowAndNoOverlappingRowsAfterResize() throws Exception {
        Minecraft client = client();
        HeartConfig draft = new HeartConfig();
        EntitySettingsScreen screen = screen(client, draft, new SettingsAutosave(draft, config -> true), choices(100));
        for (int width : new int[] {320, 321, 854}) {
            for (int height : new int[] {240, 241, 255, 256, 259, 260, 300, 360, 480}) {
                screen.resize(width, height);
                Button previous = button(screen, net.minecraft.network.chat.Component.literal("<"));
                Button next = button(screen, net.minecraft.network.chat.Component.literal(">"));
                Button back = button(screen, net.minecraft.network.chat.CommonComponents.GUI_BACK);
                int center = width / 2;
                assertEquals(center, screen.pageLabelX());
                assertEquals(2 * center, previous.getX() + next.getX() + next.getWidth());
                assertEquals(previous.getY(), next.getY());
                assertEquals(center, back.getX() + back.getWidth() / 2);
                assertTrue(back.getY() >= next.getY() + next.getHeight() + 8);
                assertTrue(back.getY() + back.getHeight() <= height - 20);
                for (var child : screen.children()) {
                    if (child instanceof Button row && row != previous && row != next && row != back) {
                        assertTrue(row.getY() + row.getHeight() <= previous.getY() - 8,
                                width + "x" + height);
                    }
                }
            }
        }
    }

    @Test void realSearchFiltersRowsAndTogglesIndividualTypesThenEscapeSaves() throws Exception {
        Minecraft client = client();
        HeartConfig draft = new HeartConfig();
        AtomicInteger writes = new AtomicInteger();
        SettingsAutosave autosave = new SettingsAutosave(draft, config -> {
            assertTrue(config.entityEnabled("minecraft:cow"));
            assertTrue(config.entityEnabled("minecraft:player"));
            writes.incrementAndGet();
            return true;
        });
        EntitySettingsScreen screen = screen(client, draft, autosave, choices(20));
        ((EditBox) screen.children().getFirst()).setValue("COW");
        assertTrue(row(screen, 0).getMessage().getString().startsWith("Cow:"));
        assertFalse(row(screen, 1).visible);
        press(row(screen, 0));
        assertTrue(draft.entityEnabled("minecraft:cow"));
        assertTrue(screen.keyPressed(new KeyEvent(InputConstants.KEY_ESCAPE, 0, 0)));
        screen.removed();
        assertEquals(1, writes.get());
        verify(client).setScreenAndShow(null);
    }

    @Test void nativeMouseWheelAndPageButtonsReachEntitiesBeyondTheFirstPage() throws Exception {
        Minecraft client = client();
        HeartConfig draft = new HeartConfig();
        SettingsAutosave autosave = new SettingsAutosave(draft, config -> true);
        EntitySettingsScreen screen = screen(client, draft, autosave, choices(20));
        assertTrue(screen.mouseScrolled(160, 80, 0, -1));
        assertEquals("Mob 2: ", row(screen, 0).getMessage().getString().substring(0, 7));
        press(row(screen, 0));
        assertTrue(draft.entityEnabled("example:mob_2"));
        assertTrue(screen.mouseScrolled(160, 80, 0, 1));
        assertTrue(row(screen, 0).getMessage().getString().startsWith("Player:"));
        Button next = button(screen, net.minecraft.network.chat.Component.literal(">"));
        press(next);
        assertTrue(row(screen, 0).getMessage().getString().startsWith("Mob 2:"));
        ((EditBox) screen.children().getFirst()).setValue("does not exist");
        assertTrue(screen.noMatches());
        assertFalse(row(screen, 0).visible);
        assertFalse(next.active);
        Object oldSearch = screen.children().getFirst();
        screen.resize(400, 300);
        assertNotSame(oldSearch, screen.children().getFirst());
        assertEquals("does not exist", ((EditBox) screen.children().getFirst()).getValue());
        assertEquals(10, screen.children().size());
        assertTrue(screen.noMatches());
        ((EditBox) screen.children().getFirst()).setValue("cow");
        press(row(screen, 0));
        assertTrue(draft.entityEnabled("minecraft:cow"));
    }

    @Test void saveFailureKeepsEntityScreenOpenAndRetryDoesNotLoseSelection() throws Exception {
        Minecraft client = client();
        HeartConfig draft = new HeartConfig();
        AtomicInteger attempts = new AtomicInteger();
        SettingsAutosave autosave = new SettingsAutosave(draft, config -> attempts.incrementAndGet() > 1);
        EntitySettingsScreen screen = screen(client, draft, autosave, choices(0));
        assertFalse(screen.saveFailed());
        press(row(screen, 0));
        assertFalse(draft.entityEnabled("minecraft:player"));
        Button back = button(screen, net.minecraft.network.chat.CommonComponents.GUI_BACK);
        press(back);
        verify(client, never()).setScreenAndShow(any());
        assertTrue(autosave.failed());
        assertTrue(screen.saveFailed());
        press(back);
        assertFalse(screen.saveFailed());
        assertFalse(draft.entityEnabled("minecraft:player"));
        verify(client).setScreenAndShow(null);
        assertEquals(2, attempts.get());
    }

    @Test void entitiesButtonSharesTheDraftSoReturningDoesNotOverwriteEntityChoices() throws Exception {
        Minecraft client = client();
        HeartConfig draft = new HeartConfig();
        AtomicInteger writes = new AtomicInteger();
        HeartSettingsScreen parent = new HeartSettingsScreen(client, null, draft, config -> {
            writes.incrementAndGet();
            return true;
        });
        parent.width = 320;
        parent.height = 240;
        parent.resize(320, 240);
        try (var registered = mockStatic(EntitySelection.class)) {
            registered.when(EntitySelection::registered).thenReturn(choices(0));
            press((Button) parent.children().getFirst());
            press((Button) parent.children().getLast());
        }
        assertEquals(1, writes.get());
        ArgumentCaptor<net.minecraft.client.gui.screens.Screen> opened =
                ArgumentCaptor.forClass(net.minecraft.client.gui.screens.Screen.class);
        verify(client).setScreenAndShow(opened.capture());
        EntitySettingsScreen child = (EntitySettingsScreen) opened.getValue();
        child.width = 320;
        child.height = 240;
        child.resize(320, 240);
        press(row(child, 1));
        child.onClose();
        verify(client).setScreenAndShow(parent);
        assertEquals(2, writes.get());
        parent.resize(320, 240);
        press((Button) parent.children().get(1));
        parent.onClose();
        assertFalse(draft.enabled);
        assertFalse(draft.animations);
        assertTrue(draft.entityEnabled("minecraft:cow"));
        assertEquals(3, writes.get());
    }
}
