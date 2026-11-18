package dev.fungus.hearts;

public final class HealthHistory {
    private int health = -1;
    private int displayed;
    private float maximum;
    private double lastSeen;
    private double displayedAt;
    private double blinkUntil = -100;

    public void update(float next, float max, double time) {
        update(next, max, time, true);
    }

    public void update(float next, float max, double time, boolean invulnerable) {
        int rounded = (int) Math.ceil(HeartLayout.finiteHealth(next));
        if (health < 0 || max != maximum || time < lastSeen || time - lastSeen > 20) {
            health = displayed = rounded;
            displayedAt = time;
            blinkUntil = time - 100;
        } else {
            if (invulnerable && rounded < health) {
                displayedAt = time;
                blinkUntil = time + 20;
            } else if (invulnerable && rounded > health) {
                displayedAt = time;
                blinkUntil = time + 10;
            }
            if (time - displayedAt > 20) {
                displayed = rounded;
                displayedAt = time;
            }
            health = rounded;
        }
        maximum = max;
        lastSeen = time;
    }

    public int previous() { return displayed; }

    public boolean flashing(double time) {
        int remaining = (int) Math.ceil(blinkUntil - time);
        return remaining > 0 && remaining / 3 % 2 == 1;
    }
}
