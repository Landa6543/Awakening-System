package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.IsekaiWorldManager;
import com.soybe.awakeningsystem.MurimWorldManager;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import com.soybe.awakeningsystem.YggdrasilTreeGenerator;
import com.soybe.awakeningsystem.block.entity.AscensionPortalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AscensionPortalBlock extends Block implements EntityBlock {
    public static final MapCodec<AscensionPortalBlock> CODEC = simpleCodec(AscensionPortalBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public enum PortalType implements StringRepresentable {
        BASE("base"),
        RETURN("return"),
        ASCEND("ascend");

        private final String name;

        PortalType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<PortalType> PORTAL_TYPE = EnumProperty.create("portal_type", PortalType.class);

    // VoxelShape volumétrico adaptado a las proporciones del portal gate (2.4x2.4 bloques)
    protected static final VoxelShape X_AXIS_AABB = Block.box(-12.0D, 0.0D, 4.0D, 28.0D, 38.0D, 12.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(4.0D, 0.0D, -12.0D, 12.0D, 38.0D, 28.0D);

    public AscensionPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(AXIS, Direction.Axis.X)
                .setValue(PORTAL_TYPE, PortalType.BASE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AXIS, PORTAL_TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        Direction.Axis portalAxis = (facing.getAxis() == Direction.Axis.Z) ? Direction.Axis.X : Direction.Axis.Z;
        return this.defaultBlockState().setValue(FACING, facing).setValue(AXIS, portalAxis).setValue(PORTAL_TYPE, PortalType.BASE);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = state.hasProperty(AXIS) ? state.getValue(AXIS) : Direction.Axis.X;
        return axis == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    @Override
    public boolean canBeReplaced(BlockState state, net.minecraft.world.level.material.Fluid fluid) {
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        Direction currentFacing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        Direction newFacing = rot.rotate(currentFacing);
        Direction.Axis newAxis = (newFacing.getAxis() == Direction.Axis.Z) ? Direction.Axis.X : Direction.Axis.Z;
        return state.setValue(FACING, newFacing).setValue(AXIS, newAxis);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Direction currentFacing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        return rotate(state, mirror.getRotation(currentFacing));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            handlePortalActivation(serverPlayer, pos, level);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            handlePortalActivation(serverPlayer, pos, level);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static void handlePortalActivation(ServerPlayer player, BlockPos pos, Level level) {
        if (player.isShiftKeyDown()) {
            player.setRespawnPosition(level.dimension(), pos, player.getYRot(), true, true);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§d§l[Portal de la Torre] §aPunto de reaparición establecido."), true);
            player.playNotifySound(net.minecraft.sounds.SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
        player.playNotifySound(com.soybe.awakeningsystem.ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.9F, 1.0F);

        if (level.dimension().equals(TeyvatWorldManager.TEYVAT_WORLD)) {
            com.soybe.awakeningsystem.abyss.SpiralAbyssManager.openAbyssScreen(player);
            return;
        }

        if (level.dimension().equals(com.soybe.awakeningsystem.TowerManager.RANKER_TOWER)) {
            com.soybe.awakeningsystem.TowerManager.exitTowerToSpawn(player.getServer(), player.getUUID());
            return;
        }

        if (com.soybe.awakeningsystem.tower.GreatTowerWorldManager.isGreatTowerDimension(level)) {
            player.setPortalCooldown();
            BlockState blockState = level.getBlockState(pos);
            PortalType pType = blockState.hasProperty(PORTAL_TYPE) ? blockState.getValue(PORTAL_TYPE) : PortalType.RETURN;
            int currentFloor = com.soybe.awakeningsystem.tower.GreatTowerWorldManager.getFloorForPos(level, pos);
            if (pType == PortalType.ASCEND) {
                if (currentFloor >= 10) {
                    player.displayClientMessage(Component.literal("§6§l[Gran Torre] §7Has alcanzado la cima de los pisos disponibles en esta versión (Piso 10). ¡Nuevos pisos en próximas actualizaciones!"), true);
                    player.playNotifySound(SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.0F);
                } else {
                    com.soybe.awakeningsystem.tower.GreatTowerWorldManager.enterFloor(player, currentFloor + 1);
                }
            } else {
                if (currentFloor <= 1) {
                    com.soybe.awakeningsystem.tower.GreatTowerWorldManager.exitTower(player);
                } else {
                    com.soybe.awakeningsystem.tower.GreatTowerWorldManager.enterFloor(player, currentFloor - 1);
                }
            }
            return;
        }

        // Gran Torre Física (Fase 14): Verificación de Selección de la Torre y Despertar de Potencial
        boolean hasPotential = player.getPersistentData().contains("tower_potential_rank")
                || player.getPersistentData().getBoolean("tower_selected");
        if (!hasPotential) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5§l[Gran Torre] §r§7El vórtice dimensional te rechaza... Solo aquellos §deleggidos por la Torre§7 cuyo §dPotencial Innato§7 haya sido despertado en el altar pueden cruzar este umbral."), true);
            player.playNotifySound(SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 0.75F);
        } else {
            String rank = player.getPersistentData().getString("tower_potential_rank");
            if (rank.isEmpty()) rank = "EX";
            com.soybe.awakeningsystem.tower.GreatTowerWorldManager.enterFloor(player, 1);
            player.playNotifySound(SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AscensionPortalBlockEntity(pos, state);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
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
                player.setPortalCooldown();
                com.soybe.awakeningsystem.abyss.SpiralAbyssManager.openAbyssScreen(player);
            } else if (level.dimension().equals(com.soybe.awakeningsystem.TowerManager.RANKER_TOWER)) {
                player.setPortalCooldown();
                com.soybe.awakeningsystem.TowerManager.exitTowerToSpawn(player.getServer(), player.getUUID());
            } else if (com.soybe.awakeningsystem.tower.GreatTowerWorldManager.isGreatTowerDimension(level)) {
                player.setPortalCooldown();
                PortalType pType = state.hasProperty(PORTAL_TYPE) ? state.getValue(PORTAL_TYPE) : PortalType.RETURN;
                int currentFloor = com.soybe.awakeningsystem.tower.GreatTowerWorldManager.getFloorForPos(level, pos);
                if (pType == PortalType.ASCEND) {
                    if (currentFloor >= 20) {
                        player.displayClientMessage(Component.literal("§6§l[Gran Torre] §7Has alcanzado la cima de los pisos disponibles en esta versión (Piso 20). ¡Nuevos pisos en próximas actualizaciones!"), true);
                        player.playNotifySound(SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.0F);
                    } else {
                        com.soybe.awakeningsystem.tower.GreatTowerWorldManager.enterFloor(player, currentFloor + 1);
                    }
                } else {
                    if (currentFloor <= 1) {
                        com.soybe.awakeningsystem.tower.GreatTowerWorldManager.exitTower(player);
                    } else {
                        com.soybe.awakeningsystem.tower.GreatTowerWorldManager.enterFloor(player, currentFloor - 1);
                    }
                }
            } else if (level.dimension().equals(Level.OVERWORLD)) {
                player.setPortalCooldown();
                boolean hasPotential = player.getPersistentData().contains("tower_potential_rank")
                        || player.getPersistentData().getBoolean("tower_selected");
                if (!hasPotential) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5§l[Gran Torre] §r§7El vórtice dimensional te rechaza... Solo aquellos §deleggidos por la Torre§7 cuyo §dPotencial Innato§7 haya sido despertado en el altar pueden cruzar este umbral."), true);
                    player.playNotifySound(SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 0.75F);
                } else {
                    String rank = player.getPersistentData().getString("tower_potential_rank");
                    if (rank.isEmpty()) rank = "EX";
                    com.soybe.awakeningsystem.tower.GreatTowerWorldManager.enterFloor(player, 1);
                    player.playNotifySound(SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8F, 1.2F);
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

            int side = random.nextInt(2) * 2 - 1;

            if (axis == Direction.Axis.X) {
                pz = (double) pos.getZ() + 0.5D + 0.25D * (double) side;
                vz = random.nextFloat() * 2.0F * (float) side;
            } else {
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
