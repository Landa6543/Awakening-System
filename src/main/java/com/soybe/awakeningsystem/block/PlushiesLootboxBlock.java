package com.soybe.awakeningsystem.block;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PlushiesLootboxBlock extends Block {

    public PlushiesLootboxBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            level.destroyBlock(pos, false);
            ItemStack randomPlushie = SystemItems.getRandomPlushieItem(sl.random);
            popResource(sl, pos, randomPlushie);

            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 35, 0.4D, 0.4D, 0.4D, 0.15D);
            sl.sendParticles(ParticleTypes.HEART, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 6, 0.3D, 0.3D, 0.3D, 0.05D);
            sl.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.0F, 1.2F);
            sl.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.2F, 1.8F);

            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6🎁 ¡Has abierto una Caja de Peluches de Genshin y obtenido: §e" + randomPlushie.getHoverName().getString() + "§6!"), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel sl && !player.isCreative()) {
            ItemStack randomPlushie = SystemItems.getRandomPlushieItem(sl.random);
            popResource(sl, pos, randomPlushie);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 25, 0.3D, 0.3D, 0.3D, 0.1D);
            sl.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 0.9F, 1.1F);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
