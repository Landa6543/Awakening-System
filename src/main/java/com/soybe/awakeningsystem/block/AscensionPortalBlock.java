package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.IsekaiWorldManager;
import com.soybe.awakeningsystem.MurimWorldManager;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import com.soybe.awakeningsystem.YggdrasilTreeGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AscensionPortalBlock extends Block {
    public static final MapCodec<AscensionPortalBlock> CODEC = simpleCodec(AscensionPortalBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    public AscensionPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis axis = context.getHorizontalDirection().getAxis();
        Direction.Axis portalAxis = (axis == Direction.Axis.Z) ? Direction.Axis.X : Direction.Axis.Z;
        return this.defaultBlockState().setValue(AXIS, portalAxis);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return switch (rot) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(AXIS)) {
                case X -> state.setValue(AXIS, Direction.Axis.Z);
                case Z -> state.setValue(AXIS, Direction.Axis.X);
                default -> state;
            };
            default -> state;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            // Evitar spam de teletransporte si el jugador ya está cambiando de dimensión
            if (player.isOnPortalCooldown()) return;

            if (level.dimension().equals(IsekaiWorldManager.ISEKAI_WORLD)) {
                player.setPortalCooldown();
                MurimWorldManager.teleportToMurim(player);
            } else if (level.dimension().equals(MurimWorldManager.MURIM_WORLD)) {
                if (player.getServer() != null) {
                    ServerLevel isekai = player.getServer().getLevel(IsekaiWorldManager.ISEKAI_WORLD);
                    if (isekai != null) {
                        player.setPortalCooldown();
                        BlockPos target = YggdrasilTreeGenerator.ASCENSION_GATE_POS.offset(0, 0, 2);
                        player.teleportTo(isekai, target.getX() + 0.5D, (double) target.getY(), target.getZ() + 0.5D, 180.0F, 0.0F);
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 200, 0, false, false));
                        player.playNotifySound(com.soybe.awakeningsystem.ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
                    }
                }
            } else if (level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
                if (player.getServer() != null) {
                    ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
                    if (overworld != null) {
                        player.setPortalCooldown();
                        BlockPos targetPos = player.getRespawnPosition();
                        if (targetPos == null) targetPos = overworld.getSharedSpawnPos();
                        player.teleportTo(overworld, targetPos.getX() + 0.5D, (double) targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D, player.getYRot(), 0.0F);
                        player.playNotifySound(com.soybe.awakeningsystem.ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
                        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("awakening_system.tower.returned_to_overworld"), true);
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(120) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    com.soybe.awakeningsystem.ModSounds.GATE_AMBIENT.value(), SoundSource.BLOCKS, 0.4F, random.nextFloat() * 0.15F + 0.75F, false);
        }

        Direction.Axis axis = state.getValue(AXIS);
        for (int i = 0; i < 2; i++) {
            double px = (double) pos.getX() + random.nextDouble();
            double py = (double) pos.getY() + random.nextDouble();
            double pz = (double) pos.getZ() + random.nextDouble();

            double vx = (random.nextFloat() - 0.5D) * 0.5D;
            double vy = (random.nextFloat() - 0.5D) * 0.5D;
            double vz = (random.nextFloat() - 0.5D) * 0.5D;

            int side = random.nextInt(2) * 2 - 1; // -1 or 1

            if (axis == Direction.Axis.X) {
                // Portal plane extends along X, normal/faces are in Z
                pz = (double) pos.getZ() + 0.5D + 0.25D * (double) side;
                vz = random.nextFloat() * 2.0F * (float) side;
            } else {
                // Portal plane extends along Z, normal/faces are in X
                px = (double) pos.getX() + 0.5D + 0.25D * (double) side;
                vx = random.nextFloat() * 2.0F * (float) side;
            }

            level.addParticle(ParticleTypes.PORTAL, px, py, pz, vx, vy, vz);
            if (random.nextFloat() < 0.4F) {
                level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0D, 0.02D, 0.0D);
            }
            if (random.nextFloat() < 0.25F) {
                level.addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, 0.0D, 0.015D, 0.0D);
            }
        }
    }
}
