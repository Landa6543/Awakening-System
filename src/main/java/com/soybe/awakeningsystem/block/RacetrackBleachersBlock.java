package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.TavernSeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.List;

/**
 * Gradas de Hipódromo (Racetrack Bleachers / Stadium Seats).
 * Bloque orientado con asiento y respaldo que permite a los espectadores sentarse
 * cómodamente para ver las carreras de Umas.
 * Cuenta con conexión continua en filas de asientos (Single, Left, Right, Middle)
 * y soporte para colocarse sobre losas bajas ocupando la mitad superior libre.
 */
public class RacetrackBleachersBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<RacetrackBleachersBlock> CODEC = simpleCodec(RacetrackBleachersBlock::new);

    public enum BleacherShape implements StringRepresentable {
        SINGLE("single"),
        LEFT("left"),
        RIGHT("right"),
        MIDDLE("middle");

        private final String name;

        BleacherShape(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<BleacherShape> SHAPE = EnumProperty.create("shape", BleacherShape.class);

    private static final VoxelShape BOX_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D);

    public RacetrackBleachersBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SHAPE, BleacherShape.SINGLE));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHAPE);
    }

    public BlockState updateBleacherShape(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);

        Direction leftDir = facing.getCounterClockWise();
        Direction rightDir = facing.getClockWise();

        BlockState leftState = level.getBlockState(pos.relative(leftDir));
        BlockState rightState = level.getBlockState(pos.relative(rightDir));

        boolean hasLeft = leftState.getBlock() instanceof RacetrackBleachersBlock
                && leftState.getValue(FACING) == facing;

        boolean hasRight = rightState.getBlock() instanceof RacetrackBleachersBlock
                && rightState.getValue(FACING) == facing;

        BleacherShape shape;
        if (hasLeft && hasRight) {
            shape = BleacherShape.MIDDLE;
        } else if (hasRight) {
            shape = BleacherShape.LEFT;
        } else if (hasLeft) {
            shape = BleacherShape.RIGHT;
        } else {
            shape = BleacherShape.SINGLE;
        }

        return state.setValue(SHAPE, shape);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();

        BlockState state = this.defaultBlockState()
                .setValue(FACING, facing);

        return updateBleacherShape(state, context.getLevel(), pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return updateBleacherShape(state, level, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BOX_SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            AABB searchBox = new AABB(pos);
            List<TavernSeatEntity> existing = sl.getEntitiesOfClass(TavernSeatEntity.class, searchBox);
            if (!existing.isEmpty()) {
                sp.displayClientMessage(Component.translatable("block.awakening_system.racetrack_bleachers.occupied"), true);
                return ItemInteractionResult.FAIL;
            }

            TavernSeatEntity seat = ModEntities.TAVERN_SEAT.get().create(sl);
            if (seat != null) {
                seat.setPos(pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);
                sl.addFreshEntity(seat);
                sp.startRiding(seat, true);
                sl.playSound(null, pos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.7F, 1.1F);
                sp.displayClientMessage(Component.translatable("block.awakening_system.racetrack_bleachers.watching"), true);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }
}
