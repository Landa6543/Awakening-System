package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.entity.ClandestineCashierBlockEntity;
import com.soybe.awakeningsystem.casino.network.OpenClandestineCashierOwnerPayload;
import com.soybe.awakeningsystem.casino.network.OpenClandestineCashierPayload;
import com.soybe.awakeningsystem.item.ClandestineVaultKeyItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class ClandestineCashierBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<ClandestineCashierBlock> CODEC = simpleCodec(ClandestineCashierBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ClandestineCashierBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClandestineCashierBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ClandestineCashierBlockEntity cashierBe) {
                cashierBe.setOwner(player);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ClandestineCashierBlockEntity cashierBe)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean isPublic = com.soybe.awakeningsystem.world.SeriesStructureBuilder.isInsideBlackMarketDen(level, pos)
                || com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.isInsideCasino(level, pos);

        if (!isPublic && cashierBe.getOwnerUUID() == null && player != null) {
            cashierBe.setOwner(player);
        }

        boolean isKeyInHand = stack.is(SystemItems.CLANDESTINE_VAULT_KEY.get());
        boolean isOwner = cashierBe.isOwner(player);
        boolean isOwnerOrAdmin = player.isCreative() || player.hasPermissions(2) || isOwner;

        // Si sostiene la llave maestra directamente o se agacha para gestionar
        if (isKeyInHand || (player.isShiftKeyDown() && (isOwnerOrAdmin || isPublic))) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                if (isPublic && !sp.isCreative() && !sp.hasPermissions(2)) {
                    sp.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.9F, 0.8F);
                    sp.sendSystemMessage(Component.translatable("awakening_system.block.clandestine_cashier.sealed"), true);
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }

                if (!isOwnerOrAdmin) {
                    sp.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.9F, 0.8F);
                    sp.sendSystemMessage(Component.translatable("awakening_system.block.clandestine_cashier.not_owner", cashierBe.getOwnerName()), true);
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }

                if (sp.isCreative() || sp.hasPermissions(2) || isKeyInHand || ClandestineVaultKeyItem.hasKey(sp)) {
                    sp.playNotifySound(SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.9F, 1.3F);
                    PacketDistributor.sendToPlayer(sp, new OpenClandestineCashierOwnerPayload(
                            pos,
                            cashierBe.getOwnerName(),
                            cashierBe.getStockChips(),
                            cashierBe.getCollectedMora(),
                            cashierBe.getTotalChipsSold(),
                            ClandestineCashierBlockEntity.countBlackChips(sp),
                            SystemEvents.data(sp).mora()
                    ));
                } else {
                    sp.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.9F, 0.8F);
                    sp.sendSystemMessage(Component.translatable("awakening_system.block.clandestine_cashier.need_key"), true);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Interacción normal (cliente/comprador)
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long mora = SystemEvents.data(sp).mora();
            PacketDistributor.sendToPlayer(sp, new OpenClandestineCashierPayload(pos, mora, cashierBe.getStockChips(), isOwner));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ClandestineCashierBlockEntity cashierBe)) {
            return InteractionResult.PASS;
        }

        boolean isPublic = com.soybe.awakeningsystem.world.SeriesStructureBuilder.isInsideBlackMarketDen(level, pos)
                || com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.isInsideCasino(level, pos);

        if (!isPublic && cashierBe.getOwnerUUID() == null && player != null) {
            cashierBe.setOwner(player);
        }

        boolean isOwner = cashierBe.isOwner(player);
        boolean isOwnerOrAdmin = player.isCreative() || player.hasPermissions(2) || isOwner;

        if (player.isShiftKeyDown() && (isOwnerOrAdmin || isPublic)) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                if (isPublic && !sp.isCreative() && !sp.hasPermissions(2)) {
                    sp.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.9F, 0.8F);
                    sp.sendSystemMessage(Component.translatable("awakening_system.block.clandestine_cashier.sealed"), true);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                if (!isOwnerOrAdmin) {
                    sp.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.9F, 0.8F);
                    sp.sendSystemMessage(Component.translatable("awakening_system.block.clandestine_cashier.not_owner", cashierBe.getOwnerName()), true);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                if (sp.isCreative() || sp.hasPermissions(2) || ClandestineVaultKeyItem.hasKey(sp)) {
                    sp.playNotifySound(SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.9F, 1.3F);
                    PacketDistributor.sendToPlayer(sp, new OpenClandestineCashierOwnerPayload(
                            pos,
                            cashierBe.getOwnerName(),
                            cashierBe.getStockChips(),
                            cashierBe.getCollectedMora(),
                            cashierBe.getTotalChipsSold(),
                            ClandestineCashierBlockEntity.countBlackChips(sp),
                            SystemEvents.data(sp).mora()
                    ));
                } else {
                    sp.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.9F, 0.8F);
                    sp.sendSystemMessage(Component.translatable("awakening_system.block.clandestine_cashier.need_key"), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // Interacción normal (cliente)
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long mora = SystemEvents.data(sp).mora();
            PacketDistributor.sendToPlayer(sp, new OpenClandestineCashierPayload(pos, mora, cashierBe.getStockChips(), isOwner));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        if (player.level() != null) {
            boolean isProtected = com.soybe.awakeningsystem.world.SeriesStructureBuilder.isInsideBlackMarketDen(player.level(), pos)
                    || com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.isInsideCasino(player.level(), pos);
            if (isProtected && !player.getAbilities().instabuild && !player.hasPermissions(2)) {
                return 0.0F;
            }
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        boolean isProtected = com.soybe.awakeningsystem.world.SeriesStructureBuilder.isInsideBlackMarketDen(level, pos)
                || com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.isInsideCasino(level, pos);
        if (!level.isClientSide() && isProtected && !player.getAbilities().instabuild && !player.hasPermissions(2)) {
            player.displayClientMessage(Component.translatable("awakening_system.block.clandestine_cashier.house_protection"), true);
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
        boolean isProtected = com.soybe.awakeningsystem.world.SeriesStructureBuilder.isInsideBlackMarketDen(level, pos)
                || com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.isInsideCasino(level, pos);
        if (isProtected && !player.getAbilities().instabuild && !player.hasPermissions(2)) {
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
