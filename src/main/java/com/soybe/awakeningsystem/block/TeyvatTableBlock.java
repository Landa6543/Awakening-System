package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Mesa de té y banquete tradicional de Teyvat (Bambú y Otogi).
 * Al hacer clic derecho, sirve té artesanal que restaura hambre y otorga regeneración pacífica.
 */
public class TeyvatTableBlock extends Block {
    public static final MapCodec<TeyvatTableBlock> CODEC = simpleCodec(TeyvatTableBlock::new);

    private static final VoxelShape TOP = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape LEG = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 12.0D, 11.0D);
    private static final VoxelShape SHAPE = Shapes.or(TOP, LEG);

    public TeyvatTableBlock(BlockBehaviour.Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            long now = level.getGameTime();
            long lastDrink = sp.getPersistentData().getLong("last_tea_sip_time");
            if (now - lastDrink < 200L) {
                sp.displayClientMessage(Component.translatable("block.awakening_system.teyvat_table.cooldown"), true);
                return ItemInteractionResult.SUCCESS;
            }
            sp.getPersistentData().putLong("last_tea_sip_time", now);

            sp.getFoodData().eat(2, 0.6F);
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, false, false, true));

            sl.playSound(null, pos, SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.1F);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D, 8, 0.3D, 0.2D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 5, 0.2D, 0.1D, 0.2D, 0.02D);

            sp.displayClientMessage(Component.translatable("block.awakening_system.teyvat_table.tea_enjoyed"), true);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.SUCCESS;
    }
}
