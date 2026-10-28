package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.TavernSeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Taburete artesanal de Teyvat (Otogi y Yumemiru).
 * Permite a los jugadores sentarse cómodamente haciendo clic derecho.
 */
public class TeyvatStoolBlock extends Block {
    public static final MapCodec<TeyvatStoolBlock> CODEC = simpleCodec(TeyvatStoolBlock::new);

    private static final VoxelShape SEAT_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 9.0D, 13.0D);

    public TeyvatStoolBlock(BlockBehaviour.Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SEAT_SHAPE;
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
                sp.displayClientMessage(Component.translatable("block.awakening_system.teyvat_stool.occupied"), true);
                return ItemInteractionResult.FAIL;
            }

            TavernSeatEntity seat = ModEntities.TAVERN_SEAT.get().create(sl);
            if (seat != null) {
                seat.setPos(pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);
                sl.addFreshEntity(seat);
                sp.startRiding(seat, true);
                sl.playSound(null, pos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.6F, 1.2F);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }
}
