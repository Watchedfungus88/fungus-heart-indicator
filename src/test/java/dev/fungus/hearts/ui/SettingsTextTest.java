package dev.fungus.hearts.ui;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import static org.junit.jupiter.api.Assertions.*;

class SettingsTextTest {
    @Test void removedHelperMessagesAreAbsentFromTheUiTranslations() throws Exception {
        try (var stream = getClass().getResourceAsStream("/assets/fungus_heart_indicator/lang/en_us.json")) {
            assertNotNull(stream);
            var translations = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertFalse(translations.has("fungus_hearts.auto_saved"));
            assertFalse(translations.has("fungus_hearts.entity_hint"));
            assertTrue(translations.has("fungus_hearts.save_failed"));
        }
    }

    @Test void neitherSettingsScreenCanRenderTheRemovedHelperMessages() throws Exception {
        for (Class<?> type : List.of(HeartSettingsScreen.class, EntitySelectionScreen.class, EntitySettingsScreen.class)) {
            try (var stream = type.getResourceAsStream("/" + type.getName().replace('.', '/') + ".class")) {
                assertNotNull(stream);
                ClassNode node = new ClassNode();
                new ClassReader(stream).accept(node, 0);
                for (var method : node.methods) {
                    for (var instruction : method.instructions) {
                        if (instruction instanceof LdcInsnNode literal && literal.cst instanceof String text) {
                            assertNotEquals("auto_saved", text, type.getSimpleName());
                            assertNotEquals("entity_hint", text, type.getSimpleName());
                            assertNotEquals("fungus_hearts.auto_saved", text, type.getSimpleName());
                            assertNotEquals("fungus_hearts.entity_hint", text, type.getSimpleName());
                        }
                    }
                }
            }
        }
    }
}
