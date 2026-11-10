package dev.fungus.hearts;

public final class HeartVisibility {
    private HeartVisibility() {}

    public static boolean visible(boolean enabled, boolean hudHidden, boolean invisible, boolean spectator,
                                  boolean alive, boolean sneaking, double distanceSquared, int distance,
                                  boolean self, boolean showSelf, boolean firstPerson, boolean nameVisible) {
        if (!enabled || hudHidden || invisible || spectator || !alive || sneaking
                || !Double.isFinite(distanceSquared)
                || (distance != HeartConfig.UNLIMITED_DISTANCE && distanceSquared > (double) distance * distance)) return false;
        return self ? showSelf && !firstPerson : nameVisible;
    }

    public static boolean beyondNameRange(double distanceSquared, double nameRange) {
        return Double.isFinite(nameRange) && nameRange > 0 && distanceSquared >= nameRange * nameRange;
    }
}
