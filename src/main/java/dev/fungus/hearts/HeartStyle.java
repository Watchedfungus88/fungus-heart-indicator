package dev.fungus.hearts;

public enum HeartStyle {
    CONTAINER("container"), NORMAL(""), POISONED("poisoned_"),
    WITHERED("withered_"), ABSORBING("absorbing_"), FROZEN("frozen_");

    private final String prefix;

    HeartStyle(String prefix) { this.prefix = prefix; }

    public static HeartStyle forEffects(boolean poisoned, boolean withered, boolean frozen) {
        return poisoned ? POISONED : withered ? WITHERED : frozen ? FROZEN : NORMAL;
    }

    public int slot(boolean hardcore, boolean half, boolean blinking) {
        return ordinal() * 8 + (hardcore ? 4 : 0) + (half ? 2 : 0) + (blinking ? 1 : 0);
    }

    public String path(boolean hardcore, boolean half, boolean blinking) {
        String name = this == CONTAINER ? "container" + (hardcore ? "_hardcore" : "")
                : prefix + (hardcore ? "hardcore_" : "") + (half ? "half" : "full");
        return "hud/heart/" + name + (blinking ? "_blinking" : "");
    }
}
