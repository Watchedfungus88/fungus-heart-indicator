package dev.fungus.hearts;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

public final class HeartEntityTypes {
    private static final Map<EntityType<?>, String> IDS = new IdentityHashMap<>();

    private HeartEntityTypes() {}

    public static String id(EntityType<?> type) {
        return IDS.computeIfAbsent(type, key -> EntityType.getKey(key).toString());
    }

    public static boolean supports(EntityType<?> type) {
        return id(type).equals("minecraft:player") || DefaultAttributes.hasSupplier(type)
                || LivingEntity.class.isAssignableFrom(type.getBaseClass());
    }

    public static List<EntityType<?>> choices() {
        return BuiltInRegistries.ENTITY_TYPE.stream().filter(HeartEntityTypes::supports)
                .sorted(Comparator.<EntityType<?>, Boolean>comparing(type -> !id(type).equals("minecraft:player"))
                        .thenComparing(type -> type.getDescription().getString(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(HeartEntityTypes::id)).toList();
    }
}
