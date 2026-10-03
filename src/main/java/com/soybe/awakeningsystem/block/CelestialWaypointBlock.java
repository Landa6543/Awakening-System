package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CelestialWaypointBlock extends Block {
    public static final MapCodec<CelestialWaypointBlock> CODEC = simpleCodec(CelestialWaypointBlock::new);

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 26.0D, 14.0D);

    public CelestialWaypointBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(net.minecraft.world.level.block.state.BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(net.minecraft.world.level.block.state.BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            handleWaypointTeleport(level, pos, sp);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, net.minecraft.world.level.block.state.BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            handleWaypointTeleport(level, pos, sp);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    private void handleWaypointTeleport(Level level, BlockPos pos, ServerPlayer player) {
        com.soybe.awakeningsystem.SystemData data = com.soybe.awakeningsystem.SystemEvents.data(player);
        if (data != null && !data.isWelkinEnhanced() && data.welkinQuestStep() == 3) {
            com.soybe.awakeningsystem.GachaManager.completeColumbinaAscension(player);
        }

        ServerLevel currentLevel = player.serverLevel();
        if (currentLevel.dimension().equals(com.soybe.awakeningsystem.subspace.SubspaceManager.SUBSPACE_REALM)) {
            com.soybe.awakeningsystem.subspace.SubspaceManager.exitSubspace(player);
            return;
        }
        boolean inTeyvat = currentLevel.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD);

        if (inTeyvat) {
            // Regresar al Overworld
            ServerLevel overworld = player.getServer().overworld();
            BlockPos targetPos = player.getRespawnPosition();
            if (targetPos == null) {
                targetPos = overworld.getSharedSpawnPos();
            }

            currentLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 1.0D, 0.5D, 0.1D);
            player.teleportTo(overworld, targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D, 0.0F, 0.0F);
            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
            player.displayClientMessage(Component.translatable("awakening_system.waypoint.teleport_overworld"), true);
        } else {
            // Teletransportar a Teyvat (Requiere bendición de Yggdrasil/Isekai y Cultivo Marcial Murim)
            boolean hasIsekai = data.systemUpdated() || data.isekaiUnlocked() || data.conqueredRankerTower() || data.conqueredYggdrasilLabyrinth() || player.getAbilities().instabuild;
            boolean hasMurim = data.system30Updated() || data.murimUnlocked() || data.dantianRealm() >= 1 || data.conqueredNineTribulationsPagoda() || player.getAbilities().instabuild;

            if (!hasIsekai || !hasMurim) {
                if (!hasIsekai && !hasMurim) {
                    player.displayClientMessage(Component.translatable("awakening_system.waypoint.requirements_not_met"), true);
                } else if (!hasIsekai) {
                    player.displayClientMessage(Component.translatable("awakening_system.waypoint.need_isekai"), true);
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.waypoint.need_murim"), true);
                }
                level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.0F, 0.8F);
                currentLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 30, 0.3D, 0.5D, 0.3D, 0.05D);
                return;
            }

            currentLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, 40, 0.5D, 0.8D, 0.5D, 0.05D);
            currentLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 35, 0.4D, 0.6D, 0.4D, 0.1D);
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.2F);
            TeyvatWorldManager.teleportToTeyvat(player);
        }
    }

    @Override
    public void animateTick(net.minecraft.world.level.block.state.BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(3) == 0) {
            double px = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.5D;
            double py = pos.getY() + 0.8D + rand.nextDouble() * 0.8D;
            double pz = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.5D;
            level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0D, 0.02D, 0.0D);
        }
        if (rand.nextInt(5) == 0) {
            double px = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.6D;
            double py = pos.getY() + 0.5D + rand.nextDouble() * 1.0D;
            double pz = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.6D;
            level.addParticle(ParticleTypes.PORTAL, px, py, pz, (rand.nextDouble() - 0.5D) * 0.2D, 0.1D, (rand.nextDouble() - 0.5D) * 0.2D);
        }
    }
}
