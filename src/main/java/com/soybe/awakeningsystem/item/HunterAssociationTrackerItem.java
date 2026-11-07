package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.network.SyncHunterAssociationWaypointPayload;
import com.soybe.awakeningsystem.world.SeriesStructureWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;

/**
 * Brújula / Rastreador oficial de la Asociación de Cazadores.
 * Permite a los cazadores ubicar con facilidad la Sede de Evaluación de Rango en el Overworld,
 * proyectando un haz de luz vertical en el mapa y en el mundo, indicando distancia y coordenadas exactas.
 */
public class HunterAssociationTrackerItem extends Item {

    public HunterAssociationTrackerItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(held, true);
        }

        if (level.dimension() != Level.OVERWORLD) {
            player.displayClientMessage(Component.translatable("item.awakening_system.hunter_association_tracker.not_overworld"), true);
            return InteractionResultHolder.fail(held);
        }

        if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            SeriesStructureWorldGen.SeriesStructureData data = SeriesStructureWorldGen.SeriesStructureData.get(serverLevel.getServer());
            BlockPos targetPos = null;

            // 1. Detectar si ya existe un Evaluador presente en el mundo
            for (net.minecraft.world.entity.Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof com.soybe.awakeningsystem.entity.SeriesNpcEntity npc && npc.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.HUNTER_EVALUATOR) {
                    targetPos = npc.blockPosition();
                    break;
                }
            }

            // 2. Si no hay evaluador cargado, buscar la estructura en los datos del mundo
            if (targetPos == null) {
                for (Map.Entry<String, BlockPos> entry : data.getAll().entrySet()) {
                    if (entry.getKey().startsWith("hunter_association#overworld") || entry.getKey().startsWith("hunter_association_")) {
                        targetPos = entry.getValue();
                        break;
                    }
                }
            }

            if (targetPos == null) {
                // Determine a safe, accessible spot relative to world spawn
                BlockPos spawnPos = serverLevel.getSharedSpawnPos();
                int targetX = spawnPos.getX() + 260;
                int targetZ = spawnPos.getZ() + 240;
                int y = SeriesStructureWorldGen.findStableSolidGround(serverLevel, targetX, targetZ, 12);
                if (y <= 0) {
                    y = serverLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, targetX, targetZ);
                    if (y <= 0) y = 68;
                }
                targetPos = new BlockPos(targetX, y, targetZ);
                data.recordStructure("hunter_association", targetPos, Level.OVERWORLD);
            }

            int dx = targetPos.getX() - player.getBlockX();
            int dz = targetPos.getZ() - player.getBlockZ();
            int dist = (int) Math.sqrt(dx * dx + dz * dz);
            String dirStr = getCardinalDirection(dx, dz);

            // Audio effects
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.4F);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);

            // Guidance particle beam
            double len = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
            double normX = dx / len;
            double normZ = dz / len;
            for (int i = 1; i <= 8; i++) {
                double px = player.getX() + normX * i * 0.9;
                double py = player.getY() + 1.2;
                double pz = player.getZ() + normZ * i * 0.9;
                serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0, 0, 0, 0.01);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py - 0.2, pz, 1, 0, 0.02, 0, 0.01);
            }

            // Notification
            if (dist <= 15) {
                player.displayClientMessage(Component.translatable("item.awakening_system.hunter_association_tracker.arrived"), true);
                player.sendSystemMessage(Component.translatable("item.awakening_system.hunter_association_tracker.arrived_chat",
                        targetPos.getX(), targetPos.getY(), targetPos.getZ()));
            } else {
                player.displayClientMessage(Component.translatable("item.awakening_system.hunter_association_tracker.actionbar",
                        dist, dirStr, targetPos.getX(), targetPos.getZ()), true);
                player.sendSystemMessage(Component.translatable("item.awakening_system.hunter_association_tracker.chat",
                        dist, dirStr, targetPos.getX(), targetPos.getY(), targetPos.getZ()));
            }

            // Sync Waypoint to client
            PacketDistributor.sendToPlayer(serverPlayer, new SyncHunterAssociationWaypointPayload(targetPos.getX(), targetPos.getY(), targetPos.getZ()));

            player.getCooldowns().addCooldown(this, 20);
            return InteractionResultHolder.sidedSuccess(held, false);
        }

        return InteractionResultHolder.sidedSuccess(held, false);
    }

    private static String getCardinalDirection(int dx, int dz) {
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (angle < 0) angle += 360;

        if (angle >= 337.5 || angle < 22.5) return "Sur";
        if (angle >= 22.5 && angle < 67.5) return "Suroeste";
        if (angle >= 67.5 && angle < 112.5) return "Oeste";
        if (angle >= 112.5 && angle < 157.5) return "Noroeste";
        if (angle >= 157.5 && angle < 202.5) return "Norte";
        if (angle >= 202.5 && angle < 247.5) return "Noreste";
        if (angle >= 247.5 && angle < 292.5) return "Este";
        return "Sureste";
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.hunter_association_tracker.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.hunter_association_tracker.desc2"));
        tooltip.add(Component.translatable("item.awakening_system.hunter_association_tracker.desc3"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
