package dev.fungus.hearts.ui;

import dev.fungus.hearts.HeartEntityTypes;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;

public final class EntitySelection {
    public record Choice(String id, Component name) {}

    private final List<Choice> choices;
    private List<Choice> filtered;
    private int page;
    private int pageSize = 1;

    public EntitySelection(List<Choice> choices) {
        this.choices = List.copyOf(choices);
        filtered = this.choices;
    }

    public static EntitySelection registered() {
        return new EntitySelection(HeartEntityTypes.choices().stream()
                .map(type -> new Choice(HeartEntityTypes.id(type), type.getDescription())).toList());
    }

    public void search(String query) {
        String term = query.strip().toLowerCase(Locale.ROOT);
        filtered = choices.stream().filter(choice -> choice.id().toLowerCase(Locale.ROOT).contains(term)
                || choice.name().getString().toLowerCase(Locale.ROOT).contains(term)).toList();
        page = 0;
    }

    public void pageSize(int size) {
        pageSize = Math.max(1, size);
        page = Math.min(page, pages() - 1);
    }

    public void move(int delta) { page = Math.clamp(page + delta, 0, pages() - 1); }
    public int page() { return page; }
    public int pages() { return Math.max(1, (filtered.size() + pageSize - 1) / pageSize); }
    public int matches() { return filtered.size(); }

    public List<Choice> rows() {
        int first = page * pageSize;
        return filtered.subList(first, Math.min(filtered.size(), first + pageSize));
    }
}
