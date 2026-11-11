package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VersionCompatibilityTest {
    @Test void mixinTargetsAndCallbackDescriptorsMatchThisMinecraftJar() throws Exception {
        for (String name : List.of("LivingEntityMixin", "EntityStateMixin", "EntityRendererMixin")) {
            ClassNode mixin = read("dev/fungus/hearts/mixin/" + name);
            AnnotationNode annotation = find(mixin.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/Mixin;");
            assertNotNull(annotation, name);
            List<?> targets = (List<?>) value(annotation, "value");
            assertEquals(1, targets.size());
            ClassNode target = read(((Type) targets.getFirst()).getInternalName());
            for (MethodNode method : mixin.methods) {
                if (find(method.visibleAnnotations, "Lorg/spongepowered/asm/mixin/Shadow;") != null
                        || find(method.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/Shadow;") != null) {
                    assertTrue(target.methods.stream().anyMatch(m -> m.name.equals(method.name)
                            && m.desc.equals(method.desc)), name + ": missing shadow " + method.name);
                }
                AnnotationNode inject = find(method.visibleAnnotations, "Lorg/spongepowered/asm/mixin/injection/Inject;");
                if (inject == null) inject = find(method.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/injection/Inject;");
                if (inject == null) continue;
                for (Object selector : (List<?>) value(inject, "method")) {
                    String selected = (String) selector;
                    List<MethodNode> matches = target.methods.stream()
                            .filter(m -> selected.equals(m.name) || selected.equals(m.name + m.desc)).toList();
                    assertEquals(1, matches.size(), name + ": ambiguous or missing " + selected);
                    Type[] actual = Type.getArgumentTypes(matches.getFirst().desc);
                    Type[] callback = Type.getArgumentTypes(method.desc);
                    assertEquals(actual.length + 1, callback.length);
                    for (int i = 0; i < actual.length; i++) assertEquals(actual[i], callback[i]);
                    String callbackClass = Type.getReturnType(matches.getFirst().desc).equals(Type.VOID_TYPE)
                            ? "CallbackInfo" : "CallbackInfoReturnable";
                    assertEquals("org.spongepowered.asm.mixin.injection.callback." + callbackClass,
                            callback[actual.length].getClassName());
                }
            }
        }
    }

    @Test void platformRotationMatchesTheCameraQuaternionAndPreservesTranslation() {
        PoseStack poses = new PoseStack();
        poses.translate(1, 2, 3);
        Quaternionf camera = new Quaternionf().rotationYXZ(0.7f, -0.3f, 0.2f);
        HeartPlatform.rotate(poses, camera);
        assertTrue(new Matrix4f().translation(1, 2, 3).rotate(camera)
                .equals(poses.last().pose(), 0.00001f));
    }

    @Test void scoreboardDetectionUsesThePlayerStateOnEveryVersion() throws Exception {
        AvatarRenderState state = mock(AvatarRenderState.class);
        assertFalse(HeartPlatform.hasScore(state));
        state.getClass().getField("scoreText").set(state, Component.literal("12"));
        assertTrue(HeartPlatform.hasScore(state));
    }

    private static ClassNode read(String name) throws IOException {
        try (var stream = VersionCompatibilityTest.class.getResourceAsStream("/" + name + ".class")) {
            assertNotNull(stream, name);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, ClassReader.SKIP_CODE);
            return node;
        }
    }

    private static AnnotationNode find(List<AnnotationNode> annotations, String descriptor) {
        return annotations == null ? null : annotations.stream()
                .filter(a -> a.desc.equals(descriptor)).findFirst().orElse(null);
    }

    private static Object value(AnnotationNode annotation, String key) {
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (annotation.values.get(i).equals(key)) return annotation.values.get(i + 1);
        }
        throw new AssertionError("Missing annotation key: " + key);
    }
}
