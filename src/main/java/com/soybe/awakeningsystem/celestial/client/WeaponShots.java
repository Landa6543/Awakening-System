package com.soybe.awakeningsystem.celestial.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.celestial.CelestialContent;
import com.soybe.awakeningsystem.celestial.CelestialWeapon;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A development photographer, only active when the game is started with -Dawakening.weaponShots: once in a
 * world it puts each celestial weapon in the player's hand and takes pictures of it held (Epic Fight's battle
 * stance, the plain stance and first person) into screenshots/, then closes the game. Used to check how the
 * weapons are held without guessing.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class WeaponShots {
    private WeaponShots() {}

    private static final boolean ON = System.getProperty("awakening.weaponShots") != null;
    private static final String ONLY = System.getProperty("awakening.weaponShots", "");
    private static int t = -1;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ON) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        t++;
        var server = mc.getSingleplayerServer();
        if (t == 40) {
            String n = mc.player.getGameProfile().getName();
            for (String c : new String[]{"time set noon", "weather clear", "gamerule doDaylightCycle false", "gamemode creative " + n,
                    "fill -3 199 -3 3 199 3 minecraft:smooth_stone", "fill -3 200 -3 3 205 3 minecraft:air", "tp " + n + " 0.5 200 0.5 0 0"})
                server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), c));
            mc.options.hideGui = true;
        }
        if (t < 60) return;
        var list = new java.util.ArrayList<String>();
        list.add("minecraft:diamond_sword");                 // the reference: how a vanilla sword sits
        for (CelestialWeapon w : CelestialWeapon.values()) if (ONLY.isEmpty() || ONLY.equals("1") || ONLY.contains(w.id)) list.add(w.id);
        int i = (t - 60) / 34, k = (t - 60) % 34;
        if (i >= list.size()) {
            if (k == 5) mc.stop();
            return;
        }
        String id = list.get(i);
        var item = id.contains(":") ? net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(id))
                : CelestialContent.ITEMS.get(CelestialWeapon.valueOf(id.toUpperCase(java.util.Locale.ROOT)));
        String name = id.contains(":") ? "0_" + id.substring(id.indexOf(':') + 1) : id;
        // turn the body sideways to the camera so the blade is seen flat, not edge-on
        float side = Float.parseFloat(System.getProperty("awakening.shotsYaw", "90"));
        mc.player.yBodyRot = mc.player.yBodyRotO = mc.player.getYRot() + side;
        EpicFight.turn(mc, mc.player.getYRot() + side);
        if (k == 0) {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            ItemStack stack = new ItemStack(item);
            if (sp != null) server.execute(() -> sp.setItemInHand(InteractionHand.MAIN_HAND, stack));
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            mc.options.hideGui = true;
            EpicFight.battle(mc, true);
        }
        if (k == 12) shot(mc, name + "_1_battle");
        if (k == 13) EpicFight.battle(mc, false);
        if (k == 22) shot(mc, name + "_2_plain");
        if (k == 23) { mc.options.setCameraType(CameraType.FIRST_PERSON); mc.options.hideGui = false; }
        if (k == 32) shot(mc, name + "_3_first");
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "ws_" + name + ".png", mc.getMainRenderTarget(), msg -> {});
    }

    /** Epic Fight's stance switch, only touched when Epic Fight is there. */
    private static final class EpicFight {
        static void battle(Minecraft mc, boolean on) {
            if (!net.neoforged.fml.ModList.get().isLoaded("epicfight")) return;
            Impl.set(mc, on);
        }

        static void turn(Minecraft mc, float yaw) {
            if (!net.neoforged.fml.ModList.get().isLoaded("epicfight")) return;
            Impl.turn(mc, yaw);
        }

        private static final class Impl {
            static void set(Minecraft mc, boolean on) {
                var patch = yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(mc.player,
                        yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch.class);
                if (patch == null) return;
                if (on) patch.toEpicFightMode(true);
                else patch.toVanillaMode(true);
            }

            static void turn(Minecraft mc, float yaw) {
                var patch = yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(mc.player,
                        yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch.class);
                if (patch != null) patch.setModelYRot(yaw, false);
            }
        }
    }
}
