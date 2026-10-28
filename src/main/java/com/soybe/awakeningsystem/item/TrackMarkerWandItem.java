package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.block.entity.CustomRacetrackBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Varita Trazadora de Hipódromos de Tracen (Track Marker Wand).
 * Permite a los jugadores marcar waypoints secuenciales en cualquier terreno
 * y vincularlos a una Terminal de Hipódromo Custom para organizar derbys.
 */
public class TrackMarkerWandItem extends Item {

    public TrackMarkerWandItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player != null && player.isShiftKeyDown()) {
            if (player.getXRot() < -60.0F) {
                int prev = getWaypoints(stack).size();
                clearWaypoints(stack);
                if (player instanceof ServerPlayer sp) {
                    sp.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.9F, 0.6F);
                    sp.displayClientMessage(Component.translatable("awakening_system.track_marker.reset", prev), true);
                }
                return InteractionResult.SUCCESS;
            }

            List<BlockPos> points = getWaypoints(stack);
            if (!points.isEmpty()) {
                BlockPos removed = points.remove(points.size() - 1);
                setWaypoints(stack, points);
                if (player instanceof ServerPlayer sp) {
                    sp.playNotifySound(SoundEvents.DISPENSER_DISPENSE, SoundSource.PLAYERS, 1.0F, 1.6F);
                    sp.displayClientMessage(Component.translatable("awakening_system.track_marker.undone", (points.size() + 1), points.size()), true);
                }
            } else {
                if (player instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.translatable("awakening_system.track_marker.no_waypoints"), true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(clickedPos);
        if (be instanceof CustomRacetrackBlockEntity terminal) {
            List<BlockPos> points = getWaypoints(stack);
            if (points.isEmpty()) {
                if (player != null) {
                    player.displayClientMessage(Component.translatable("awakening_system.track_marker.wand_empty"), true);
                }
                return InteractionResult.FAIL;
            }

            terminal.setWaypoints(points);
            double totalLen = calculateTotalDistance(points);
            if (player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
                java.io.File jsonFile = com.soybe.awakeningsystem.uma.RacetrackJsonHelper.exportTrack(terminal, sl);
                sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                sp.displayClientMessage(Component.translatable("awakening_system.track_marker.circuit_saved",
                        points.size(), String.format(java.util.Locale.US, "%.0f", totalLen)), false);
                if (jsonFile != null) {
                    sp.displayClientMessage(Component.translatable("awakening_system.track_marker.json_saved", jsonFile.getName()), false);
                }
            }
            return InteractionResult.SUCCESS;
        }

        BlockPos waypointPos = clickedPos.relative(context.getClickedFace());
        List<BlockPos> existing = getWaypoints(stack);
        if (!existing.isEmpty()) {
            BlockPos last = existing.get(existing.size() - 1);
            double distSq = last.distSqr(waypointPos);
            if (distSq < 2.5) {
                if (player instanceof ServerPlayer sp) {
                    sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                    sp.displayClientMessage(Component.translatable("awakening_system.track_marker.too_close"), true);
                }
                return InteractionResult.FAIL;
            }
        }

        addWaypoint(stack, waypointPos);
        existing = getWaypoints(stack);
        int total = existing.size();
        double distFromLast = (total > 1) ? Math.sqrt(existing.get(total - 2).distSqr(waypointPos)) : 0.0;
        double totalLen = calculateTotalDistance(existing);

        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    waypointPos.getX() + 0.5, waypointPos.getY() + 0.5, waypointPos.getZ() + 0.5,
                    14, 0.35, 0.35, 0.35, 0.05);
        }

        if (player instanceof ServerPlayer sp) {
            float pitch = 0.6F + ((total - 1) % 12) * 0.1F;
            sp.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, pitch);
            if (total == 1) {
                sp.displayClientMessage(Component.translatable("awakening_system.track_marker.start_registered"), true);
            } else {
                sp.displayClientMessage(Component.translatable("awakening_system.track_marker.waypoint_added",
                        total, String.format(java.util.Locale.US, "%.1f", distFromLast), String.format(java.util.Locale.US, "%.0f", totalLen)), true);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player.isShiftKeyDown()) {
            if (player.getXRot() < -60.0F) {
                int prev = getWaypoints(stack).size();
                clearWaypoints(stack);
                if (player instanceof ServerPlayer sp) {
                    sp.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.6F);
                    sp.displayClientMessage(Component.translatable("awakening_system.track_marker.cleared", prev), true);
                }
                return InteractionResultHolder.consume(stack);
            }

            List<BlockPos> points = getWaypoints(stack);
            if (!points.isEmpty()) {
                points.remove(points.size() - 1);
                setWaypoints(stack, points);
                if (player instanceof ServerPlayer sp) {
                    sp.playNotifySound(SoundEvents.DISPENSER_DISPENSE, SoundSource.PLAYERS, 1.0F, 1.6F);
                    sp.displayClientMessage(Component.translatable("awakening_system.track_marker.undone_simple", points.size()), true);
                }
                return InteractionResultHolder.consume(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    public static double calculateTotalDistance(List<BlockPos> points) {
        if (points == null || points.size() < 2) return 0.0;
        double dist = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            dist += Math.sqrt(points.get(i).distSqr(points.get(i + 1)));
        }
        return dist;
    }

    public static void setWaypoints(ItemStack stack, List<BlockPos> points) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();
        ListTag listTag = new ListTag();
        for (BlockPos p : points) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", p.getX());
            posTag.putInt("y", p.getY());
            posTag.putInt("z", p.getZ());
            listTag.add(posTag);
        }
        tag.put("Waypoints", listTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static List<BlockPos> getWaypoints(ItemStack stack) {
        List<BlockPos> list = new ArrayList<>();
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("Waypoints", net.minecraft.nbt.Tag.TAG_LIST)) {
                ListTag listTag = tag.getList("Waypoints", net.minecraft.nbt.Tag.TAG_COMPOUND);
                for (int i = 0; i < listTag.size(); i++) {
                    CompoundTag posTag = listTag.getCompound(i);
                    list.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
                }
            }
        }
        return list;
    }

    public static void addWaypoint(ItemStack stack, BlockPos pos) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();
        ListTag listTag = tag.getList("Waypoints", net.minecraft.nbt.Tag.TAG_COMPOUND);
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        listTag.add(posTag);
        tag.put("Waypoints", listTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void clearWaypoints(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            tag.remove("Waypoints");
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<BlockPos> pts = getWaypoints(stack);
        int count = pts.size();
        double dist = calculateTotalDistance(pts);
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.stats", count, String.format("%.0f", dist)).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.add_point").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.undo").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.clear").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.link").withStyle(ChatFormatting.AQUA));
    }
}
