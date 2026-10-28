package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.item.CasinoVipPassItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.BlockHitResult;

public class CasinoVipGateBlock extends FenceGateBlock {
    public static final MapCodec<CasinoVipGateBlock> CODEC = simpleCodec(CasinoVipGateBlock::new);

    public CasinoVipGateBlock(BlockBehaviour.Properties properties) {
        super(WoodType.OAK, properties);
    }

    @Override
    public MapCodec<FenceGateBlock> codec() {
        return (MapCodec<FenceGateBlock>) (MapCodec<?>) CODEC;
    }

    public static boolean hasPlayerVipAccess(Player player) {
        if (player.getAbilities().instabuild || player.hasPermissions(2)) {
            return true;
        }
        if (player instanceof ServerPlayer sp) {
            if (SystemEvents.data(sp).isCasinoVip()) {
                return true;
            }
        }
        if (!SystemItems.CASINO_VIP_PASS.isBound()) return false;
        var passItem = SystemItems.CASINO_VIP_PASS.get();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(passItem)) {
                if (CasinoVipPassItem.isBound(stack)) {
                    String boundUuid = CasinoVipPassItem.getBoundUuid(stack);
                    if (boundUuid.equals(player.getUUID().toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static final net.minecraft.world.phys.shapes.VoxelShape INVISIBLE_WALL = net.minecraft.world.level.block.Block.box(0.0D, 0.0D, 0.0D, 16.0D, 64.0D, 16.0D);

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        if (context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext ecc && ecc.getEntity() instanceof Player player) {
            if (!hasPlayerVipAccess(player)) {
                return INVISIBLE_WALL;
            }
        }
        if (state.getValue(OPEN)) {
            return net.minecraft.world.phys.shapes.Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    private boolean checkVipAccess(Level level, BlockPos pos, ServerPlayer sp) {
        if (hasPlayerVipAccess(sp)) {
            return true;
        }

        // Verificación de detalles para mensaje específico
        if (!SystemItems.CASINO_VIP_PASS.isBound()) return false;
        var passItem = SystemItems.CASINO_VIP_PASS.get();

        boolean hasUnboundPass = false;
        String stolenPassOwner = null;

        for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
            ItemStack stack = sp.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(passItem)) {
                if (CasinoVipPassItem.isBound(stack)) {
                    String boundUuid = CasinoVipPassItem.getBoundUuid(stack);
                    if (boundUuid.equals(sp.getUUID().toString())) {
                        return true; // Pase legítimo y válido
                    } else {
                        stolenPassOwner = CasinoVipPassItem.getBoundName(stack);
                    }
                } else {
                    hasUnboundPass = true;
                }
            }
        }

        // Si no tiene acceso válido, emitir respuesta según el caso
        level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 0.8F);
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 12, 0.25D, 0.25D, 0.25D, 0.05D);
        }

        if (stolenPassOwner != null) {
            sp.displayClientMessage(Component.translatable("awakening_system.block.vip_gate.stolen_pass", stolenPassOwner), true);
        } else if (hasUnboundPass) {
            sp.displayClientMessage(Component.translatable("awakening_system.block.vip_gate.unbound_pass"), true);
        } else {
            sp.displayClientMessage(Component.translatable("awakening_system.block.vip_gate.restricted"), true);
        }
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            boolean isOpen = state.getValue(OPEN);
            // Si ya está abierta, cerrarla no requiere permisos ni envía textos molestos
            if (isOpen) {
                BlockState toggled = state.cycle(OPEN);
                level.setBlock(pos, toggled, 10);
                return InteractionResult.SUCCESS;
            }

            // Si está cerrada e intenta abrir, verificar pase
            if (!checkVipAccess(level, pos, sp)) {
                return InteractionResult.FAIL;
            }

            BlockState toggled = state.cycle(OPEN);
            level.setBlock(pos, toggled, 10);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            boolean isOpen = state.getValue(OPEN);
            // Si ya está abierta, cerrarla es libre
            if (isOpen) {
                BlockState toggled = state.cycle(OPEN);
                level.setBlock(pos, toggled, 10);
                return ItemInteractionResult.SUCCESS;
            }

            // Si está cerrada e intenta abrir, verificar pase
            if (!checkVipAccess(level, pos, sp)) {
                return ItemInteractionResult.FAIL;
            }

            BlockState toggled = state.cycle(OPEN);
            level.setBlock(pos, toggled, 10);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}
