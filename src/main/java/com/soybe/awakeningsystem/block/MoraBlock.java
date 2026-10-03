package com.soybe.awakeningsystem.block;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

/**
 * Bloque de Mora (Mora Block) de PrimogemCraft:
 * Bloque decorativo de oro macizo acuñado por Rex Lapis (Morax).
 * Se puede minar para obtener sacos de Mora o colocar como tesoro en campamentos y ruinas.
 */
public class MoraBlock extends Block {

    public MoraBlock(BlockBehaviour.Properties properties) {
        super(properties.strength(3.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide() && level instanceof ServerLevel sl) {
            // Soltar entre 4 y 9 Bolsas de Mora
            int count = 4 + level.random.nextInt(6);
            if (SystemItems.MORA_POUCH.isBound()) {
                popResource(sl, pos, new ItemStack(SystemItems.MORA_POUCH.get(), count));
            }
            sl.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 25, 0.4D, 0.4D, 0.4D, 0.1D);
            sl.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 1.4F);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
