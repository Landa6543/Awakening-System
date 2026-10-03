package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class SystemNotificationOverlay {
    public static boolean HIDE_VANILLA_HEARTS = true;

    private SystemNotificationOverlay() {}

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        boolean isChat = minecraft.screen instanceof ChatScreen;
        if (minecraft.screen == null || isChat) {
            SystemHudOverlay.render(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight());
            AwakeningSystemClient.renderNotification(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight());
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayerPre(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre event) {
        boolean shouldHide = com.soybe.awakeningsystem.client.gui.GuiConfig.shouldHideVanillaHearts();
        if (shouldHide && (VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName())
                || VanillaGuiLayers.ARMOR_LEVEL.equals(event.getName()))) {
            var mc = Minecraft.getInstance();
            if (mc.player != null && !mc.player.isCreative() && !mc.player.isSpectator()) {
                event.setCanceled(true);
            }
        }
    }
}
