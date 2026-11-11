package dev.fungus.hearts;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Team;

public final class HeartEntityVisibility {
    private HeartEntityVisibility() {}

    public static boolean allowed(LivingEntity entity, Minecraft client) {
        if (client.player == null || entity.isInvisibleTo(client.player)) return false;
        Team team = entity.getTeam();
        Team viewerTeam = client.player.getTeam();
        if (team != null) {
            return switch (team.getNameTagVisibility()) {
                case ALWAYS -> true;
                case NEVER -> false;
                case HIDE_FOR_OTHER_TEAMS -> viewerTeam == null || team.isAlliedTo(viewerTeam);
                case HIDE_FOR_OWN_TEAM -> viewerTeam == null || !team.isAlliedTo(viewerTeam);
            };
        }
        return !HeartPlatform.hidden(client) && entity != client.getCameraEntity() && !entity.isVehicle();
    }
}
