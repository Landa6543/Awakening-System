package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Bloque de Suelo de Andén de Metro de Seúl (ORV).
 * Baldosas de granito y terrazo pulido utilizadas en las estaciones subterráneas.
 */
public class SubwayPlatformBlock extends Block {
    public static final MapCodec<SubwayPlatformBlock> CODEC = simpleCodec(SubwayPlatformBlock::new);

    public SubwayPlatformBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
