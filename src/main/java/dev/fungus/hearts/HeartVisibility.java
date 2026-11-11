package dev.fungus.hearts;

public final class HeartVisibility {
    private HeartVisibility() {}

    public static boolean visible(boolean enabled, boolean hudHidden, boolean invisible, boolean spectator,
                                  boolean alive, boolean sneaking, double distanceSquared, int distance,
                                  boolean self, boolean showSelf, boolean firstPerson, boolean nameVisible) {
        if (!enabled || hudHidden || invisible || spectator || !alive
                || !Double.isFinite(distanceSquared)
                || (distance != HeartConfig.UNLIMITED_DISTANCE && distanceSquared > (double) distance * distance)) return false;
        return self ? showSelf && !firstPerson : nameVisible;
    }

    public static boolean needsNameCheck(double distanceSquared, double nameRange, boolean sneaking) {
        return beyondNameRange(distanceSquared, nameRange) || (sneaking && distanceSquared >= 32 * 32);
    }

    public static double namePolicyDistance(double distanceSquared, boolean sneaking) {
        return sneaking ? Math.min(distanceSquared, Math.nextDown(32.0 * 32)) : distanceSquared;
    }

    public static boolean beyondNameRange(double distanceSquared, double nameRange) {
        return Double.isFinite(nameRange) && nameRange > 0 && distanceSquared >= nameRange * nameRange;
    }
}
