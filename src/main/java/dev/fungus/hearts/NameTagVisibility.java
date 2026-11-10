package dev.fungus.hearts;

import net.minecraft.world.entity.Entity;

public interface NameTagVisibility {
    boolean fungus$canShowName(Entity entity, double distanceSquared);
}
