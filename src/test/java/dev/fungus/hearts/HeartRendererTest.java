package dev.fungus.hearts;

import net.minecraft.SharedConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HeartRendererTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static final class State extends EntityRenderState implements HeartState {
        private HeartFrame frame;
        @Override public HeartFrame fungus$getHearts() { return frame; }
        @Override public void fungus$setHearts(HeartFrame frame) { this.frame = frame; }
    }

    private static final class Fixture implements AutoCloseable {
        final HeartConfig config = new HeartConfig();
        final Minecraft client = mock(Minecraft.class);
        final NameTagVisibility names = mock(NameTagVisibility.class);
        final State state = new State();
        final MockedStatic<HeartPlatform> platform;

        Fixture() throws Exception {
            Options options = mock(Options.class);
            when(options.getCameraType()).thenReturn(CameraType.THIRD_PERSON_BACK);
            var optionsField = Minecraft.class.getField("options");
            optionsField.setAccessible(true);
            optionsField.set(client, options);
            client.player = mock(LocalPlayer.class);
            platform = mockStatic(HeartPlatform.class);
            platform.when(() -> HeartPlatform.nameRange(any())).thenReturn(64.0);
            platform.when(() -> HeartPlatform.sneakingColor(client)).thenReturn(0x80FFFFFF);
            when(names.fungus$canShowName(any(), anyDouble())).thenReturn(true);
            state.distanceToCameraSq = 16 * 16;
        }

        LivingEntity mob(String type) {
            LivingEntity mob = mock(LivingEntity.class, withSettings().extraInterfaces(EntityHistory.class));
            doReturn(HeartEntityTypesTest.type(type)).when(mob).getType();
            when(mob.isAlive()).thenReturn(true);
            when(mob.getMaxHealth()).thenReturn(20f);
            when(mob.getHealth()).thenReturn(13f);
            when(((EntityHistory) mob).fungus$history()).thenReturn(new HealthHistory());
            return mob;
        }

        AbstractClientPlayer player() {
            AbstractClientPlayer player = mock(AbstractClientPlayer.class,
                    withSettings().extraInterfaces(EntityHistory.class));
            doReturn(HeartEntityTypesTest.type("player")).when(player).getType();
            when(player.isAlive()).thenReturn(true);
            when(player.getMaxHealth()).thenReturn(20f);
            when(player.getHealth()).thenReturn(13f);
            when(((EntityHistory) player).fungus$history()).thenReturn(new HealthHistory());
            when(player.level()).thenReturn(mock(Level.class, RETURNS_DEEP_STUBS));
            return player;
        }

        void extract(Entity entity) {
            HeartRenderer.extract(entity, state, 0.5f, names, config, client, () -> HeartFrameTest.PALETTE);
        }

        @Override public void close() { platform.close(); }
    }

    @Test void defaultConfigurationSkipsMobsAndClearsReusedRenderState() throws Exception {
        try (Fixture f = new Fixture()) {
            f.state.frame = HeartFrameTest.frame(20, 20, 0, 20, 0, false, HeartStyle.NORMAL, false, false, false);
            LivingEntity cow = f.mob("cow");
            f.extract(cow);
            assertNull(f.state.frame);
            verify(cow, never()).getHealth();
            Entity arrow = mock(Entity.class);
            f.extract(arrow);
            assertNull(f.state.frame);
        }
    }

    @Test void individuallyEnabledUnnamedMobsUseTheirHealthAndOwnHistory() throws Exception {
        try (Fixture f = new Fixture()) {
            LivingEntity cow = f.mob("cow"), zombie = f.mob("zombie");
            f.config.setEntityEnabled("minecraft:cow", true);
            f.extract(cow);
            assertNotNull(f.state.frame);
            assertEquals(10, f.state.frame.normalCount());
            assertEquals(HeartStyle.NORMAL.slot(false, true, false), f.state.frame.layer(6, 2));
            assertNull(f.state.nameTag);
            f.extract(zombie);
            assertNull(f.state.frame);
            f.config.setEntityEnabled("minecraft:zombie", true);
            f.extract(zombie);
            assertNotNull(f.state.frame);
            f.config.setEntityEnabled("minecraft:cow", false);
            f.extract(cow);
            assertNull(f.state.frame);
        }
    }

    @Test void sneakingPlayersUseTheSliderBeyond32BlocksAndKeepRendererVisibilityPolicy() throws Exception {
        try (Fixture f = new Fixture()) {
            AbstractClientPlayer player = f.player();
            when(player.isDiscrete()).thenReturn(true);
            for (int cap : new int[] {64, 128, 0}) {
                f.config.distance = cap;
                double range = cap == 0 ? 100_000 : cap;
                f.state.distanceToCameraSq = range * range;
                f.extract(player);
                assertNotNull(f.state.frame, "distance " + range);
                assertEquals(0x80FFFFFF, f.state.frame.color());
            }
            verify(f.names, times(3)).fungus$canShowName(eq(player), doubleThat(distance -> distance < 32 * 32));
            f.config.distance = 64;
            f.state.distanceToCameraSq = 64 * 64 + 1;
            f.extract(player);
            assertNull(f.state.frame);
            f.config.distance = 0;
            when(f.names.fungus$canShowName(any(), anyDouble())).thenReturn(false);
            f.extract(player);
            assertNull(f.state.frame);
        }
    }

    @Test void nameTagSuppressionAtCloseRangeAndStandingRangePolicyArePreserved() throws Exception {
        try (Fixture f = new Fixture()) {
            AbstractClientPlayer player = f.player();
            f.extract(player);
            assertNull(f.state.frame);
            f.state.nameTag = net.minecraft.network.chat.Component.literal("Player");
            f.extract(player);
            assertNotNull(f.state.frame);
            assertEquals(-1, f.state.frame.color());
            f.state.nameTag = null;
            f.config.distance = 128;
            f.state.distanceToCameraSq = 100 * 100;
            f.extract(player);
            assertNotNull(f.state.frame);
            verify(f.names).fungus$canShowName(player, 100 * 100);
        }
    }

    @Test void ownPlayerStillRequiresBothPlayerSelectionAndThirdPersonToggle() throws Exception {
        try (Fixture f = new Fixture()) {
            LocalPlayer player = mock(LocalPlayer.class, withSettings().extraInterfaces(EntityHistory.class));
            doReturn(HeartEntityTypesTest.type("player")).when(player).getType();
            when(player.isAlive()).thenReturn(true);
            when(player.getMaxHealth()).thenReturn(20f);
            when(player.getHealth()).thenReturn(20f);
            when(player.isDiscrete()).thenReturn(true);
            when(((EntityHistory) player).fungus$history()).thenReturn(new HealthHistory());
            when(player.level()).thenReturn(mock(Level.class, RETURNS_DEEP_STUBS));
            f.client.player = player;
            f.config.showSelf = true;
            f.extract(player);
            assertNotNull(f.state.frame);
            f.config.setEntityEnabled("minecraft:player", false);
            f.extract(player);
            assertNull(f.state.frame);
            f.config.setEntityEnabled("minecraft:player", true);
            f.config.showSelf = false;
            f.extract(player);
            assertNull(f.state.frame);
            f.config.showSelf = true;
            when(f.client.options.getCameraType()).thenReturn(CameraType.FIRST_PERSON);
            f.extract(player);
            assertNull(f.state.frame);
        }
    }

    @Test void mobTeamRulesRespectAllVisibilityModesAndInvisibility() throws Exception {
        try (Fixture f = new Fixture()) {
            LivingEntity mob = f.mob("cow");
            f.config.setEntityEnabled("minecraft:cow", true);
            PlayerTeam team = mock(PlayerTeam.class);
            PlayerTeam viewer = mock(PlayerTeam.class);
            when(mob.getTeam()).thenReturn(team);
            for (Team.Visibility visibility : Team.Visibility.values()) {
                when(team.getNameTagVisibility()).thenReturn(visibility);
                for (boolean allied : new boolean[] {false, true}) {
                    when(f.client.player.getTeam()).thenReturn(viewer);
                    when(team.isAlliedTo(viewer)).thenReturn(allied);
                    boolean expected = switch (visibility) {
                        case ALWAYS -> true;
                        case NEVER -> false;
                        case HIDE_FOR_OTHER_TEAMS -> allied;
                        case HIDE_FOR_OWN_TEAM -> !allied;
                    };
                    f.extract(mob);
                    assertEquals(expected, f.state.frame != null, visibility + " allied=" + allied);
                }
                when(f.client.player.getTeam()).thenReturn(null);
                f.extract(mob);
                assertEquals(visibility != Team.Visibility.NEVER, f.state.frame != null);
            }
            when(team.getNameTagVisibility()).thenReturn(Team.Visibility.ALWAYS);
            when(mob.isInvisible()).thenReturn(true);
            f.extract(mob);
            assertNull(f.state.frame);
            when(mob.isInvisible()).thenReturn(false);
            when(mob.isInvisibleTo(f.client.player)).thenReturn(true);
            f.extract(mob);
            assertNull(f.state.frame);
        }
    }

    @Test void mobDeathHudHideAndSpectatorStillSuppressSelectedEntities() throws Exception {
        try (Fixture f = new Fixture()) {
            LivingEntity mob = f.mob("cow");
            f.config.setEntityEnabled("minecraft:cow", true);
            f.extract(mob);
            assertNotNull(f.state.frame);
            when(mob.isAlive()).thenReturn(false);
            f.extract(mob);
            assertNull(f.state.frame);
            when(mob.isAlive()).thenReturn(true);
            when(mob.isSpectator()).thenReturn(true);
            f.extract(mob);
            assertNull(f.state.frame);
            when(mob.isSpectator()).thenReturn(false);
            f.platform.when(() -> HeartPlatform.hidden(f.client)).thenReturn(true);
            f.extract(mob);
            assertNull(f.state.frame);
        }
    }

    @Test void opaqueAndSneakingFramesSubmitOnlyOneNormalDepthTestedPass() throws Exception {
        try (Fixture f = new Fixture();
             var types = mockStatic(net.minecraft.client.renderer.rendertype.RenderTypes.class)) {
            var type = mock(net.minecraft.client.renderer.rendertype.RenderType.class);
            types.when(() -> net.minecraft.client.renderer.rendertype.RenderTypes.text(HeartSprites.ATLAS))
                    .thenReturn(type);
            var poses = new com.mojang.blaze3d.vertex.PoseStack();
            poses.translate(1, 2, 3);
            var before = new org.joml.Matrix4f(poses.last().pose());
            var collector = mock(net.minecraft.client.renderer.SubmitNodeCollector.class);
            for (int color : new int[] {-1, 0x80FFFFFF}) {
                f.state.frame = new HeartFrame(13, 20, 4, 20, 3, true, HeartStyle.NORMAL,
                        false, false, true, 1, HeartFrameTest.PALETTE, color);
                HeartRenderer.submit(f.state, poses, collector, new org.joml.Quaternionf());
                verify(collector).submitCustomGeometry(poses, type, f.state.frame);
                assertTrue(before.equals(poses.last().pose(), 0.00001f));
            }
            types.verify(() -> net.minecraft.client.renderer.rendertype.RenderTypes.text(HeartSprites.ATLAS), times(2));
            types.verifyNoMoreInteractions();
            verifyNoMoreInteractions(collector);
        }
    }
}
