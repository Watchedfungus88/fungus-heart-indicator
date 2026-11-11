package dev.fungus.hearts;

import java.util.HashSet;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeartEntityTypesTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test void actualRegistryListsPlayersAndHealthBearingEntitiesButNotVehiclesOrProjectiles() {
        var choices = HeartEntityTypes.choices();
        var ids = choices.stream().map(HeartEntityTypes::id).toList();
        assertEquals("minecraft:player", ids.getFirst());
        for (String id : new String[] {"cow", "pig", "zombie", "slime", "wither", "ender_dragon", "armor_stand"}) {
            assertTrue(ids.contains("minecraft:" + id), id);
        }
        for (String id : new String[] {"arrow", "item", "oak_boat", "minecart", "painting", "text_display"}) {
            assertFalse(ids.contains("minecraft:" + id), id);
        }
        assertEquals(ids.size(), new HashSet<>(ids).size());
        assertTrue(ids.size() > 60);
    }

    @Test void everySupportedTypeIsOffByDefaultExceptPlayers() {
        HeartConfig config = new HeartConfig();
        for (EntityType<?> type : HeartEntityTypes.choices()) {
            String id = HeartEntityTypes.id(type);
            assertEquals(id.equals("minecraft:player"), config.entityEnabled(id), id);
            assertSame(HeartEntityTypes.id(type), HeartEntityTypes.id(type));
        }
    }

    static EntityType<?> type(String id) {
        return BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:" + id));
    }
}
