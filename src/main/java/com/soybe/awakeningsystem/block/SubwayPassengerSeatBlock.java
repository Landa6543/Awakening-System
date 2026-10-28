package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.entity.MeditationSeatEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Asiento de pasajeros ergonómico de acero inoxidable y cojín acolchado
 * característico de los vagones del Metro de Seúl (ORV Coche 3707).
 * Se conecta automáticamente con asientos adyacentes formando bancos continuos.
 * Permite sentarse haciendo clic derecho para descansar y cultivar Qi (+4/s).
 */
public class SubwayPassengerSeatBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SubwayPassengerSeatBlock> CODEC = simpleCodec(SubwayPassengerSeatBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<SeatType> TYPE = EnumProperty.create("type", SeatType.class);

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 14.0D),
            Block.box(0.0D, 8.0D, 12.0D, 16.0D, 16.0D, 16.0D)
    );
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(0.0D, 0.0D, 2.0D, 16.0D, 8.0D, 16.0D),
            Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 4.0D)
    );
    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(2.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
            Block.box(0.0D, 8.0D, 0.0D, 4.0D, 16.0D, 16.0D)
    );
    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 14.0D, 8.0D, 16.0D),
            Block.box(12.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D)
    );

    public enum SeatType implements StringRepresentable {
        SINGLE("single"),
        LEFT("left"),
        MIDDLE("middle"),
        RIGHT("right");

        private final String name;
        SeatType(String name) { this.name = name; }
        @Override
        public String getSerializedName() { return this.name; }
    }

    public SubwayPassengerSeatBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, SeatType.SINGLE));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return updateSeatShape(level, pos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        if (direction.getAxis().isHorizontal() && direction.getAxis() != facing.getAxis()) {
            return updateSeatShape(level, pos, facing);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    private BlockState updateSeatShape(BlockGetter level, BlockPos pos, Direction facing) {
        Direction leftDir = facing.getCounterClockWise();
        Direction rightDir = facing.getClockWise();

        boolean hasLeft = isMatchingSeat(level, pos.relative(leftDir), facing);
        boolean hasRight = isMatchingSeat(level, pos.relative(rightDir), facing);

        SeatType type;
        if (hasLeft && hasRight) {
            type = SeatType.MIDDLE;
        } else if (hasLeft) {
            type = SeatType.RIGHT;
        } else if (hasRight) {
            type = SeatType.LEFT;
        } else {
            type = SeatType.SINGLE;
        }

        return this.defaultBlockState().setValue(FACING, facing).setValue(TYPE, type);
    }

    private boolean isMatchingSeat(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof SubwayPassengerSeatBlock && state.getValue(FACING) == facing;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                Direction newFacing = state.getValue(FACING).getClockWise();
                BlockState newState = updateSeatShape(level, pos, newFacing);
                level.setBlock(pos, newState, 3);
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 0.7F, 1.2F);
                return InteractionResult.CONSUME;
            }

            AABB checkArea = new AABB(pos).inflate(0.2D);
            List<com.soybe.awakeningsystem.entity.TavernSeatEntity> seats = level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.TavernSeatEntity.class, checkArea);
            if (!seats.isEmpty()) {
                com.soybe.awakeningsystem.entity.TavernSeatEntity existing = seats.get(0);
                if (existing.hasPassenger(player)) {
                    return InteractionResult.PASS;
                } else if (!existing.getPassengers().isEmpty()) {
                    player.displayClientMessage(Component.translatable("block.awakening_system.subway_seat.occupied"), true);
                    return InteractionResult.CONSUME;
                }
            }

            com.soybe.awakeningsystem.entity.TavernSeatEntity seat = ModEntities.TAVERN_SEAT.get().create(level);
            if (seat != null) {
                Direction facing = state.getValue(FACING);
                seat.setPos(pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);
                seat.setYRot(facing.toYRot());
                level.addFreshEntity(seat);
                player.startRiding(seat, true);
                player.setYRot(facing.toYRot());
                player.setXRot(0.0F);
                player.yRotO = facing.toYRot();
                player.xRotO = 0.0F;
                level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, 1.1F);
                player.displayClientMessage(Component.translatable("block.awakening_system.subway_seat.seated"), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }
}
