package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Borde de Andén de Metro con Línea Amarilla de Seguridad y Franja Táctil (ORV).
 * Se orienta mirando hacia las vías férreas para señalizar el límite seguro de espera de pasajeros.
 */
public class SubwayPlatformEdgeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SubwayPlatformEdgeBlock> CODEC = simpleCodec(SubwayPlatformEdgeBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public SubwayPlatformEdgeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Al colocar el borde mirando hacia la vía, se orienta en la dirección que mira el jugador
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }
}
