package dev.fungus.hearts.mixin;

import dev.fungus.hearts.HealthHistory;
import dev.fungus.hearts.EntityHistory;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements EntityHistory {
    @Unique private HealthHistory fungus$history;

    @Override
    public HealthHistory fungus$history() {
        if (fungus$history == null) fungus$history = new HealthHistory();
        return fungus$history;
    }
}
