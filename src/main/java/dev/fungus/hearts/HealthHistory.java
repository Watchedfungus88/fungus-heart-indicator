package dev.fungus.hearts;

public final class HealthHistory {
    private int health = -1;
    private int previous;
    private float maximum;
    private double lastSeen;
    private double changedAt = -100;
    private double blinkUntil = -100;

    public void update(float next, float max, double time) {
        int rounded = (int) Math.ceil(HeartLayout.finiteHealth(next));
        if (health < 0 || max != maximum || time < lastSeen || time - lastSeen > 20) {
            health = previous = rounded;
            changedAt = blinkUntil = time - 100;
        } else {
            if (time - changedAt > 20) previous = health;
            if (rounded != health) {
                blinkUntil = time + (rounded < health ? 20 : 10);
                health = rounded;
                changedAt = time;
            }
        }
        maximum = max;
        lastSeen = time;
    }

    public int previous() { return previous; }

    public boolean flashing(double time) {
        int remaining = (int) Math.ceil(blinkUntil - time);
        return remaining > 0 && remaining / 3 % 2 == 1;
    }
}
