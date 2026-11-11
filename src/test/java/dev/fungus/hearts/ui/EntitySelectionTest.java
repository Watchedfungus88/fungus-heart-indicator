package dev.fungus.hearts.ui;

import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntitySelectionTest {
    private EntitySelection selection() {
        return new EntitySelection(List.of(
                new EntitySelection.Choice("minecraft:player", Component.literal("Player")),
                new EntitySelection.Choice("minecraft:cow", Component.literal("Cow")),
                new EntitySelection.Choice("example:frost_wolf", Component.literal("Snow Hound"))));
    }

    @Test void searchMatchesNamesNamespacesAndIdsIgnoringCaseAndOuterWhitespace() {
        EntitySelection selection = selection();
        for (String term : new String[] {"FROST", " Hound ", "example:"}) {
            selection.search(term);
            assertEquals(1, selection.matches());
            assertEquals("example:frost_wolf", selection.rows().getFirst().id());
        }
        selection.search("minecraft:");
        assertEquals(2, selection.matches());
        selection.search("");
        assertEquals(3, selection.matches());
    }

    @Test void pagesClampAndSearchResetsToTheFirstPageIncludingNoResults() {
        EntitySelection selection = selection();
        selection.pageSize(1);
        selection.move(99);
        assertEquals(2, selection.page());
        assertEquals("example:frost_wolf", selection.rows().getFirst().id());
        selection.search("cow");
        assertEquals(0, selection.page());
        assertEquals(1, selection.pages());
        selection.search("not an entity");
        assertEquals(0, selection.matches());
        assertEquals(1, selection.pages());
        assertTrue(selection.rows().isEmpty());
        selection.move(-99);
        selection.move(99);
        assertEquals(0, selection.page());
    }

    @Test void paginationDoesNotDropEntriesAndResizeClampsTheCurrentPage() {
        EntitySelection selection = new EntitySelection(IntStream.range(0, 101).mapToObj(i ->
                new EntitySelection.Choice("example:mob_" + i, Component.literal("Mob " + i))).toList());
        selection.pageSize(7);
        int rows = 0;
        for (int i = 0; i < selection.pages(); i++) {
            rows += selection.rows().size();
            selection.move(1);
        }
        assertEquals(101, rows);
        assertEquals(14, selection.page());
        selection.pageSize(1000);
        assertEquals(0, selection.page());
        assertEquals(101, selection.rows().size());
    }
}
