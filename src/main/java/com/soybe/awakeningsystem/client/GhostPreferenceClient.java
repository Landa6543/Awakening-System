package com.soybe.awakeningsystem.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.tombstone.GhostPreference;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The player's own "become a ghost on death" setting: kept in config/awakening_system-ghost.txt and told to the
 * server on joining a world and whenever it is switched in the interface settings.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class GhostPreferenceClient {
    private GhostPreferenceClient() {}

    private static Boolean ghost;

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("awakening_system-ghost.txt");
    }

    public static boolean ghostOnDeath() {
        if (ghost == null) {
            ghost = true;
            try {
                if (Files.exists(file())) ghost = !"off".equals(Files.readString(file()).trim());
            } catch (Exception ignored) {}
        }
        return ghost;
    }

    public static void toggle() {
        ghost = !ghostOnDeath();
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), ghost ? "on" : "off");
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.warn("Could not save the ghost setting", e);
        }
        send();
    }

    private static void send() {
        var c = Minecraft.getInstance().getConnection();
        if (c != null && c.hasChannel(GhostPreference.Payload.TYPE)) PacketDistributor.sendToServer(new GhostPreference.Payload(ghostOnDeath()));
    }

    @SubscribeEvent
    public static void joined(ClientPlayerNetworkEvent.LoggingIn event) {
        send();
    }
}
