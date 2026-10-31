package com.soybe.awakeningsystem.block;

import com.soybe.awakeningsystem.item.ManaCrystalShardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BuddingManaCrystalBlock extends Block {
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingManaCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof ManaCrystalShardItem shardItem) {
            Direction face = hitResult.getDirection();
            BlockPos targetPos = pos.relative(face);
            BlockState targetState = level.getBlockState(targetPos);

            if (targetState.isAir() || targetState.is(Blocks.WATER)) {
                if (!level.isClientSide()) {
                    Block clusterBlock = getClusterForRank(shardItem.getRank());
                    BlockState clusterState = clusterBlock.defaultBlockState().setValue(TeyvatCrystalBlock.FACING, face);
                    level.setBlockAndUpdate(targetPos, clusterState);

                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }

                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.GLOW, targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 20, 0.3D, 0.3D, 0.3D, 0.05D);
                        sl.sendParticles(ParticleTypes.PORTAL, targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 15, 0.2D, 0.2D, 0.2D, 0.1D);
                    }
                    level.playSound(null, targetPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.2F);
                    level.playSound(null, targetPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.7F, 0.8F);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) != 0) return;

        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.isAir() || targetState.is(Blocks.WATER)) {
            ManaCrystalShardItem.CrystalRank rank = pickSpontaneousRank(level, random);
            Block clusterBlock = getClusterForRank(rank);
            BlockState clusterState = clusterBlock.defaultBlockState().setValue(TeyvatCrystalBlock.FACING, direction);
            level.setBlockAndUpdate(targetPos, clusterState);

            level.sendParticles(ParticleTypes.GLOW, targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 12, 0.25D, 0.25D, 0.25D, 0.03D);
        }
    }

    public static Block getClusterForRank(ManaCrystalShardItem.CrystalRank rank) {
        return switch (rank) {
            case E -> SystemBlocks.MANA_CRYSTAL_CLUSTER_E.get();
            case D -> SystemBlocks.MANA_CRYSTAL_CLUSTER_D.get();
            case C -> SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get();
            case B -> SystemBlocks.MANA_CRYSTAL_CLUSTER_B.get();
            case A -> SystemBlocks.MANA_CRYSTAL_CLUSTER_A.get();
            case S -> SystemBlocks.MANA_CRYSTAL_CLUSTER_S.get();
            case EX -> SystemBlocks.MANA_CRYSTAL_CLUSTER_EX.get();
        };
    }

    private static ManaCrystalShardItem.CrystalRank pickSpontaneousRank(ServerLevel level, RandomSource random) {
        int roll = random.nextInt(100);
        var dim = level.dimension();
        if (dim.equals(Level.END) || dim.equals(com.soybe.awakeningsystem.subspace.SubspaceManager.SUBSPACE_REALM)) {
            if (roll < 20) return ManaCrystalShardItem.CrystalRank.C;
            if (roll < 50) return ManaCrystalShardItem.CrystalRank.B;
            if (roll < 80) return ManaCrystalShardItem.CrystalRank.A;
            if (roll < 95) return ManaCrystalShardItem.CrystalRank.S;
            return ManaCrystalShardItem.CrystalRank.EX;
        } else if (dim.equals(Level.NETHER) || dim.equals(com.soybe.awakeningsystem.MurimWorldManager.MURIM_WORLD)) {
            if (roll < 25) return ManaCrystalShardItem.CrystalRank.D;
            if (roll < 60) return ManaCrystalShardItem.CrystalRank.C;
            if (roll < 85) return ManaCrystalShardItem.CrystalRank.B;
            if (roll < 97) return ManaCrystalShardItem.CrystalRank.A;
            return ManaCrystalShardItem.CrystalRank.S;
        } else {
            // Overworld y cavernas estándar
            if (roll < 45) return ManaCrystalShardItem.CrystalRank.E;
            if (roll < 75) return ManaCrystalShardItem.CrystalRank.D;
            if (roll < 92) return ManaCrystalShardItem.CrystalRank.C;
            if (roll < 98) return ManaCrystalShardItem.CrystalRank.B;
            return ManaCrystalShardItem.CrystalRank.A;
        }
    }
}
