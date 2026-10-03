package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ruedas de metro y bogie ferroviario en 3D para vagones de metro (ORV).
 * Bloque decorativo puro para la carrocería de trenes y ambientación:
 * - Clic derecho: Alterna el tipo visual (Juego Doble -> Rueda Izquierda -> Eje -> Rueda Derecha).
 * - Shift + Clic derecho: Rota la orientación horizontal en 90 grados.
 * - Sin destrucción de bloques adyacentes ni generación de bloques fantasma.
 */
public class SubwayTrainWheelBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SubwayTrainWheelBlock> CODEC = simpleCodec(SubwayTrainWheelBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<WheelType> TYPE = EnumProperty.create("type", WheelType.class);

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public enum WheelType implements StringRepresentable {
        DUAL("dual", "Juego Doble (Estándar)"),
        LEFT("left", "Rueda Izquierda"),
        RIGHT("right", "Rueda Derecha"),
        AXLE("axle", "Eje Central");

        private final String name;
        private final String label;

        WheelType(String name, String label) {
            this.name = name;
            this.label = label;
        }

        public String getLabel() { return label; }
        @Override
        public String getSerializedName() { return this.name; }

        public WheelType next() {
            return switch (this) {
                case DUAL -> LEFT;
                case LEFT -> AXLE;
                case AXLE -> RIGHT;
                case RIGHT -> DUAL;
            };
        }
    }

    public SubwayTrainWheelBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, WheelType.DUAL));
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
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction playerHorizontal = context.getHorizontalDirection();

        // Si se coloca sobre una vía decorativa ancha, heredar orientación y tipo de rueda inteligentemente
        BlockState below = level.getBlockState(clickedPos.below());
        if (below.getBlock() instanceof SubwayWideRailTrackBlock) {
            SubwayWideRailTrackBlock.WideTrackShape shape = below.getValue(SubwayWideRailTrackBlock.SHAPE);
            Direction railFacing = (shape == SubwayWideRailTrackBlock.WideTrackShape.EAST_WEST) ? Direction.EAST : Direction.NORTH;
            SubwayWideRailTrackBlock.TrackSection section = below.getValue(SubwayWideRailTrackBlock.SECTION);
            WheelType type = switch (section) {
                case LEFT_RAIL -> WheelType.LEFT;
                case RIGHT_RAIL -> WheelType.RIGHT;
                case TIE_CENTER -> WheelType.AXLE;
                default -> WheelType.DUAL;
            };
            return this.defaultBlockState().setValue(FACING, railFacing).setValue(TYPE, type);
        }

        return this.defaultBlockState().setValue(FACING, playerHorizontal).setValue(TYPE, WheelType.DUAL);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                Direction nextFacing = state.getValue(FACING).getClockWise();
                level.setBlock(pos, state.setValue(FACING, nextFacing), 3);
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5F, 1.4F);
                player.displayClientMessage(Component.literal("§e[Ruedas ORV] §7Orientación: §f" + nextFacing.getName().toUpperCase()), true);
            } else {
                WheelType next = state.getValue(TYPE).next();
                level.setBlock(pos, state.setValue(TYPE, next), 3);
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 0.8F, 1.0F);
                player.displayClientMessage(Component.literal("§b[Ruedas ORV] §7Tipo: §f" + next.getLabel()), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
