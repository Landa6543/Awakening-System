package com.soybe.awakeningsystem.client.gui;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

/**
 * The System window's open/close animation, shared by the mod's other windows: they unfold from their
 * middle line when opened, and fold away when closed with Escape. The System window does it itself.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class ScreenUnfoldAnimation {
    private ScreenUnfoldAnimation() {}

    private static final long ANIM_MS = 180L;
    private static final Set<String> SCREENS = Set.of(
            "ConstellationChannelScreen", "FablesScreen", "CultivationScreen", "GuildScreen", "GuildChunkMapScreen",
            "ClassSkillTreeScreen", "SkillDetailScreen", "SystemGuidebookScreen", "SystemBestiaryScreen", "BountyBoardScreen",
            "WorldArchiveScreen", "CompanionScreen", "CompanionAscensionScreen", "SponsorSelectionScreen", "ClassSelectionScreen",
            "TrilunarGuideScreen", "MurimAlchemyScreen", "DungeonKeyScreen", "TowerMissionBoardScreen", "HunterWorldMapScreen",
            "WaypointListScreen", "GuildReceptionScreen", "StatueOfTheSevenScreen", "VisionSelectionScreen", "SpiralAbyssScreen");

    private static Screen current;
    private static long start;
    private static boolean closing;
    private static boolean pushed;

    private static boolean animated(Screen s) {
        return s != null && SCREENS.contains(s.getClass().getSimpleName())
                && s.getClass().getName().startsWith("com.soybe.awakeningsystem");
    }

    private static float reveal() {
        float raw = Math.min(1.0F, (Util.getMillis() - start) / (float) ANIM_MS);
        float eased = raw * raw * (3.0F - 2.0F * raw);
        return Math.max(0.02F, closing ? 1.0F - eased : eased);
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event) {
        Screen s = event.getScreen();
        // only a newly opened window unfolds; a resize re-inits the same one
        if (animated(s) && s != current) {
            current = s;
            start = Util.getMillis();
            closing = false;
        }
    }

    @SubscribeEvent
    public static void onRenderPre(ScreenEvent.Render.Pre event) {
        pushed = false;
        if (event.getScreen() != current) return;
        float r = reveal();
        if (r >= 1.0F && !closing) return;
        GuiGraphics g = event.getGuiGraphics();
        float cx = event.getScreen().width / 2.0F, cy = event.getScreen().height / 2.0F;
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0.0F);
        g.pose().scale(1.0F, r, 1.0F);
        g.pose().translate(-cx, -cy, 0.0F);
        pushed = true;
    }

    @SubscribeEvent
    public static void onRenderPost(ScreenEvent.Render.Post event) {
        if (!pushed) return;
        pushed = false;
        GuiGraphics g = event.getGuiGraphics();
        g.pose().popPose();
        float r = reveal();
        Screen s = event.getScreen();
        float cy = s.height / 2.0F, half = s.height * 0.32F * r + 1.0F;
        int x0 = (int) (s.width * 0.18F), x1 = (int) (s.width * 0.82F);
        g.fill(x0, (int) (cy - half), x1, (int) (cy - half) + 1, 0xFF70E8FF);
        g.fill(x0, (int) (cy + half) - 1, x1, (int) (cy + half), 0xFF70E8FF);
    }

    @SubscribeEvent
    public static void onKey(ScreenEvent.KeyPressed.Pre event) {
        Screen s = event.getScreen();
        if (s != current || event.getKeyCode() != GLFW.GLFW_KEY_ESCAPE || !s.shouldCloseOnEsc()) return;
        if (!closing) {
            closing = true;
            start = Util.getMillis();
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (current != null && mc.screen != current) {
            current = null;
            closing = false;
            return;
        }
        if (current != null && closing && Util.getMillis() - start >= ANIM_MS) {
            Screen s = current;
            current = null;
            closing = false;
            s.onClose();
        }
    }
}
