package com.soybe.awakeningsystem.routine.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.routine.NpcRoutine;
import com.soybe.awakeningsystem.routine.NpcRoutine.Stop;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The client side of a guild NPC's routine: the routine being edited, the editor window (classic or full-dive),
 * and the "pick places" mode, where each right click on a block adds a stop and a crouching right click goes
 * back to the editor.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class RoutineClient {
    private RoutineClient() {}

    /** A routine being edited. */
    public static final class Draft {
        public final int npcId;
        public final String npcName;
        public boolean on;
        public final List<Stop> stops;

        Draft(int npcId, String npcName, boolean on, List<Stop> stops) {
            this.npcId = npcId;
            this.npcName = npcName;
            this.on = on;
            this.stops = new ArrayList<>(stops);
        }

        /** The time for a new stop: two hours after the last one, or 8:00 for the first. */
        public int nextTime() {
            return stops.isEmpty() ? 2000 : Math.floorMod(stops.get(stops.size() - 1).time() + 2000, 24000);
        }

        public boolean add(BlockPos pos) {
            if (stops.size() >= NpcRoutine.MAX_STOPS) return false;
            stops.add(new Stop(pos.immutable(), nextTime()));
            return true;
        }

        public void shift(int index, int ticks) {
            Stop s = stops.get(index);
            stops.set(index, new Stop(s.pos(), Math.floorMod(s.time() + ticks, 24000)));
        }

        public void save() {
            List<Stop> sorted = new ArrayList<>(stops);
            sorted.sort(Comparator.comparingInt(Stop::time));
            PacketDistributor.sendToServer(new NpcRoutine.SavePayload(npcId, on, sorted));
        }
    }

    private static Draft picking;

    public static void open(NpcRoutine.OpenPayload p) {
        show(new Draft(p.npcId(), p.npcName(), p.on(), p.stops()));
    }

    static void show(Draft draft) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(com.soybe.awakeningsystem.client.sao.UiStyle.fullDive()
                ? new com.soybe.awakeningsystem.client.sao.SaoRoutineScreen(draft) : new RoutineScreen(draft));
    }

    /** Leaves the editor to pick places in the world. */
    public static void startPicking(Draft draft) {
        picking = draft;
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(null);
        if (mc.player != null) mc.player.displayClientMessage(Component.translatable("awakening_system.routine.pick_start"), false);
    }

    public static boolean isPicking() {
        return picking != null;
    }

    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (picking == null || !event.isUseItem()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        if (mc.player.isShiftKeyDown()) {
            Draft d = picking;
            picking = null;
            show(d);
            return;
        }
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult bh && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = bh.getBlockPos().above();
            if (picking.add(pos)) {
                Stop s = picking.stops.get(picking.stops.size() - 1);
                mc.player.displayClientMessage(Component.translatable("awakening_system.routine.pick_added",
                        picking.stops.size(), NpcRoutine.clock(s.time()), pos.getX(), pos.getY(), pos.getZ()), false);
                mc.player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6F, 1.4F);
            } else {
                mc.player.displayClientMessage(Component.translatable("awakening_system.routine.full", NpcRoutine.MAX_STOPS), true);
            }
        }
    }
}
