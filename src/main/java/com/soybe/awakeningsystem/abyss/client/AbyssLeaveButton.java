package com.soybe.awakeningsystem.abyss.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.abyss.AbyssLeave;
import com.soybe.awakeningsystem.abyss.SpiralAbyssManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** In the Spiral Abyss the pause menu gets a "Leave the Abyss" button at its top. */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class AbyssLeaveButton {
    private AbyssLeaveButton() {}

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen screen)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(SpiralAbyssManager.SPIRAL_ABYSS_DIMENSION)) return;
        event.addListener(Button.builder(Component.translatable("gui.awakening_system.spiral_abyss.leave_abyss"), b -> {
            PacketDistributor.sendToServer(new AbyssLeave.LeavePayload());
            mc.setScreen(null);
        }).bounds(screen.width / 2 - 102, 8, 204, 20).build());
    }
}
