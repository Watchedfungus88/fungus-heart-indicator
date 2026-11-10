package dev.fungus.hearts.mixin;

import dev.fungus.hearts.HealthHistory;
import dev.fungus.hearts.PlayerHistory;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractClientPlayer.class)
public abstract class ClientPlayerMixin implements PlayerHistory {
    @Unique private HealthHistory fungus$history;

    @Override
    public HealthHistory fungus$history() {
        if (fungus$history == null) fungus$history = new HealthHistory();
        return fungus$history;
    }
}
