package com.soybe.awakeningsystem.block;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Vasija de Tesoro de Teyvat (Teyvat Jar) de PrimogemCraft:
 * Vasija tribal o antigua de arcilla cocida que se rompe instantáneamente
 * al recibir un golpe, impacto de proyectil o ser pisada fuertemente, soltando botín aleatorio.
 */
public class TeyvatJarBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);

    public TeyvatJarBlock(BlockBehaviour.Properties properties) {
        super(properties.instabreak().sound(SoundType.DECORATED_POT).noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide()) {
            level.destroyBlock(hit.getBlockPos(), true, projectile);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide() && level instanceof ServerLevel sl) {
            RandomSource rand = sl.getRandom();

            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5D, pos.getY() + 0.4D, pos.getZ() + 0.5D, 30, 0.3D, 0.3D, 0.3D, 0.15D);
            sl.playSound(null, pos, SoundEvents.DECORATED_POT_SHATTER, SoundSource.BLOCKS, 1.2F, 1.0F);

            net.minecraft.world.entity.player.Player nearbyPlayer = sl.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 9.0, false);
            int multiplier = 1;
            ItemStack lottoStack = ItemStack.EMPTY;

            if (nearbyPlayer != null) {
                for (int i = 0; i < nearbyPlayer.getInventory().getContainerSize(); i++) {
                    ItemStack st = nearbyPlayer.getInventory().getItem(i);
                    if (!st.isEmpty()) {
                        if (SystemItems.OMNISCIENCE_POUCH.isBound() && st.is(SystemItems.OMNISCIENCE_POUCH.get())) {
                            multiplier = 2;
                        }
                        if (lottoStack.isEmpty() && SystemItems.GALACTIC_BIG_LOTTO.isBound() && st.is(SystemItems.GALACTIC_BIG_LOTTO.get())) {
                            lottoStack = st;
                        }
                    }
                }
            }

            if (multiplier > 1 && nearbyPlayer != null) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 15, 0.4, 0.4, 0.4, 0.1);
                sl.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.8F);
            }

            if (!lottoStack.isEmpty() && nearbyPlayer != null) {
                if (rand.nextFloat() < 0.75F) {
                    popResource(sl, pos, new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), 6 + rand.nextInt(10)));
                    popResource(sl, pos, new ItemStack(SystemItems.PRIMOGEM.get(), 2 + rand.nextInt(3)));
                    if (rand.nextFloat() < 0.15F && SystemItems.INTERTWINED_FATE.isBound()) {
                        popResource(sl, pos, new ItemStack(SystemItems.INTERTWINED_FATE.get(), 1));
                    }
                    sl.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 30, 0.5, 0.5, 0.5, 0.15);
                    sl.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.5F);
                    nearbyPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable("block.awakening_system.teyvat_jar.lotto_win"));
                } else {
                    lottoStack.shrink(1);
                    nearbyPlayer.setHealth(1.0F);
                    sl.sendParticles(ParticleTypes.CRIT, nearbyPlayer.getX(), nearbyPlayer.getY() + 1.0, nearbyPlayer.getZ(), 40, 0.5, 0.5, 0.5, 0.2);
                    sl.playSound(null, nearbyPlayer.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.4F, 0.5F);
                    sl.playSound(null, nearbyPlayer.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.2F, 0.8F);
                    nearbyPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable("block.awakening_system.teyvat_jar.lotto_broken"));
                }
            }

            if (rand.nextFloat() < 0.60F && SystemItems.MORA_POUCH.isBound()) {
                popResource(sl, pos, new ItemStack(SystemItems.MORA_POUCH.get(), (1 + rand.nextInt(2)) * multiplier));
            }

            if (rand.nextFloat() < 0.40F && SystemItems.SLIME_CONDENSATE.isBound()) {
                popResource(sl, pos, new ItemStack(SystemItems.SLIME_CONDENSATE.get(), (1 + rand.nextInt(3)) * multiplier));
            }

            if (rand.nextFloat() < 0.35F && SystemItems.DAMAGED_MASK.isBound()) {
                popResource(sl, pos, new ItemStack(SystemItems.DAMAGED_MASK.get(), (1 + rand.nextInt(2)) * multiplier));
            }

            if (rand.nextFloat() < 0.30F && SystemItems.COSMIC_FRAGMENT.isBound()) {
                popResource(sl, pos, new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), (3 + rand.nextInt(8)) * multiplier));
            }

            if (rand.nextFloat() < 0.15F && SystemItems.PRIMOGEM.isBound()) {
                popResource(sl, pos, new ItemStack(SystemItems.PRIMOGEM.get(), (1 + rand.nextInt(3)) * multiplier));
            }

            if (rand.nextFloat() < 0.08F && SystemItems.CRYSTAL_CORE.isBound()) {
                popResource(sl, pos, new ItemStack(SystemItems.CRYSTAL_CORE.get(), 1 * multiplier));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
