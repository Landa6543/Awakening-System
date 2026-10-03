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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Vía férrea ancha decorativa para ambientar túneles, estaciones y estructuras del metro de ORV.
 * Bloque puramente constructivo y decorativo:
 * - Clic derecho: Alterna la sección (Riel Izquierdo -> Durmientes -> Riel Derecho).
 * - Shift + Clic derecho: Alterna la orientación (Norte-Sur / Este-Oeste).
 * - No destruye bloques adyacentes ni genera reacciones en cadena.
 */
public class SubwayWideRailTrackBlock extends Block {
    public static final MapCodec<SubwayWideRailTrackBlock> CODEC = simpleCodec(SubwayWideRailTrackBlock::new);
    public static final EnumProperty<WideTrackShape> SHAPE = EnumProperty.create("shape", WideTrackShape.class);
    public static final EnumProperty<TrackSection> SECTION = EnumProperty.create("section", TrackSection.class);

    private static final VoxelShape SHAPE_BOX = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);

    public enum WideTrackShape implements StringRepresentable {
        NORTH_SOUTH("north_south"),
        EAST_WEST("east_west"),
        SOUTH_EAST("south_east"),
        SOUTH_WEST("south_west"),
        NORTH_WEST("north_west"),
        NORTH_EAST("north_east");

        private final String name;
        WideTrackShape(String name) { this.name = name; }
        @Override
        public String getSerializedName() { return this.name; }

        public boolean isCurve() {
            return this != NORTH_SOUTH && this != EAST_WEST;
        }

        public WideTrackShape nextStraight() {
            return (this == NORTH_SOUTH) ? EAST_WEST : NORTH_SOUTH;
        }
    }

    public enum TrackSection implements StringRepresentable {
        LEFT_RAIL("left_rail", 0, "Riel Izquierdo"),
        TIE_1("tie_1", 1, "Durmiente Interior Izq"),
        TIE_CENTER("tie_center", 2, "Durmiente Central"),
        TIE_2("tie_2", 3, "Durmiente Interior Der"),
        RIGHT_RAIL("right_rail", 4, "Riel Derecho");

        private final String name;
        private final int index;
        private final String label;

        TrackSection(String name, int index, String label) {
            this.name = name;
            this.index = index;
            this.label = label;
        }

        public int getIndex() { return index; }
        public String getLabel() { return label; }
        @Override
        public String getSerializedName() { return this.name; }

        public static TrackSection fromIndex(int idx) {
            return switch (idx) {
                case 0 -> LEFT_RAIL;
                case 1 -> TIE_1;
                case 2 -> TIE_CENTER;
                case 3 -> TIE_2;
                case 4 -> RIGHT_RAIL;
                default -> TIE_CENTER;
            };
        }

        public TrackSection next() {
            TrackSection[] vals = values();
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public SubwayWideRailTrackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, WideTrackShape.NORTH_SOUTH)
                .setValue(SECTION, TrackSection.TIE_CENTER));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, SECTION);
    }

    public static Direction getCrossDirection(WideTrackShape shape) {
        return (shape == WideTrackShape.EAST_WEST) ? Direction.NORTH : Direction.EAST;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        WideTrackShape shape = (facing.getAxis() == Direction.Axis.X) ? WideTrackShape.EAST_WEST : WideTrackShape.NORTH_SOUTH;
        return this.defaultBlockState().setValue(SHAPE, shape).setValue(SECTION, TrackSection.TIE_CENTER);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                // Shift + Clic: Alternar orientación (Norte-Sur <-> Este-Oeste)
                WideTrackShape currentShape = state.getValue(SHAPE);
                WideTrackShape nextShape = currentShape.nextStraight();
                level.setBlock(pos, state.setValue(SHAPE, nextShape), 3);
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5F, 1.4F);
                player.displayClientMessage(Component.literal("§e[Vía Decorativa ORV] §7Orientación: §f" + (nextShape == WideTrackShape.NORTH_SOUTH ? "Norte - Sur" : "Este - Oeste")), true);
            } else {
                // Clic normal: Alternar sección del modelo (Riel Izquierdo -> Durmientes -> Riel Derecho)
                TrackSection nextSection = state.getValue(SECTION).next();
                level.setBlock(pos, state.setValue(SECTION, nextSection), 3);
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 0.8F, 1.2F);
                player.displayClientMessage(Component.literal("§b[Vía Decorativa ORV] §7Sección: §f" + nextSection.getLabel()), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BOX;
    }
}
