package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.casino.network.OpenSlotMachinePayload;
import com.soybe.awakeningsystem.block.entity.SlotMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class SlotMachineBlock extends BaseEntityBlock {
    public enum SlotMachinePart implements net.minecraft.util.StringRepresentable {
        FRONT("front"),
        BACK("back");

        private final String name;
        SlotMachinePart(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    public static final MapCodec<SlotMachineBlock> CODEC = simpleCodec(SlotMachineBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<SlotMachinePart> PART =
            net.minecraft.world.level.block.state.properties.EnumProperty.create("part", SlotMachinePart.class);

    private static final VoxelShape BASE_SHAPE = Block.box(0, 0, 0, 16, 43, 16);

    private final boolean illegal;

    public SlotMachineBlock(BlockBehaviour.Properties properties) {
        this(false, properties);
    }

    public SlotMachineBlock(boolean illegal, BlockBehaviour.Properties properties) {
        super(properties);
        this.illegal = illegal;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, SlotMachinePart.FRONT));
    }

    public boolean isIllegal() {
        return illegal;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == SlotMachinePart.FRONT ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE_SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(PART) == SlotMachinePart.BACK) {
            BlockPos frontPos = pos.relative(state.getValue(FACING));
            BlockState frontState = level.getBlockState(frontPos);
            if (frontState.is(this) && frontState.getValue(PART) == SlotMachinePart.FRONT) {
                return useItemOn(stack, frontState, level, frontPos, player, hand, hitResult);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SlotMachineBlockEntity slotEntity)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean isKeyInHand = stack.is(com.soybe.awakeningsystem.SystemItems.CLANDESTINE_VAULT_KEY.get());
        boolean isShiftOpen = player.isShiftKeyDown();
        boolean isPublic = com.soybe.awakeningsystem.world.SeriesStructureBuilder.isInsideBlackMarketDen(level, pos)
                || com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.isInsideCasino(level, pos);

        if (this.illegal && !isPublic && slotEntity.getOwnerUUID() == null && player != null) {
            slotEntity.setOwner(player);
        }

        if (this.illegal && (isKeyInHand || isShiftOpen)) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                if (isPublic && !player.isCreative() && !player.hasPermissions(2)) {
                    sp.playNotifySound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 0.8F);
                    player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.sealed"), true);
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }

                boolean isOwnerOrAdmin = player.isCreative() || player.hasPermissions(2) || slotEntity.isOwner(player);
                if (isOwnerOrAdmin) {
                    boolean hasKey = player.isCreative() || player.hasPermissions(2) || isKeyInHand || com.soybe.awakeningsystem.item.ClandestineVaultKeyItem.hasKey(player);
                    if (hasKey) {
                        sp.playNotifySound(net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_OPEN, net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 1.3F);
                        PacketDistributor.sendToPlayer(sp, new com.soybe.awakeningsystem.casino.network.OpenClandestineOwnerPayload(
                                pos, slotEntity.getOwnerName(), slotEntity.getReserveChips(),
                                slotEntity.getCollectedVaultChips(), slotEntity.getTotalCustomerSpins(),
                                SlotMachineBlockEntity.countBlackChips(player)
                        ));
                    } else {
                        sp.playNotifySound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 0.8F);
                        player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.need_key"), true);
                    }
                } else {
                    sp.playNotifySound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 0.8F);
                    player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.not_owner", slotEntity.getOwnerName()), true);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        if (stack.getItem() instanceof com.soybe.awakeningsystem.item.CasinoChipItem chipItem) {
            if (this.illegal && chipItem.getTier() != com.soybe.awakeningsystem.item.CasinoChipItem.ChipTier.BLACK) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.only_black_chips"), true);
                    if (player instanceof ServerPlayer sp) {
                        sp.playNotifySound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.0F);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!this.illegal && chipItem.getTier() == com.soybe.awakeningsystem.item.CasinoChipItem.ChipTier.BLACK) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.legal_rejects_black"), true);
                    if (player instanceof ServerPlayer sp) {
                        sp.playNotifySound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.0F);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }

            if (slotEntity.isSpinning()) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.already_spinning"), true);
                    if (player instanceof ServerPlayer sp) {
                        sp.playNotifySound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.2F);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                slotEntity.startSpin(sp, chipItem.getTier());

                if (level instanceof net.minecraft.server.level.ServerLevel sl) {
                    if (this.illegal) {
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D, 22, 0.25D, 0.2D, 0.25D, 0.08D);
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 14, 0.2D, 0.2D, 0.2D, 0.05D);
                    } else {
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.WAX_ON, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 18, 0.25D, 0.25D, 0.25D, 0.1D);
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 10, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                }
            } else if (level.isClientSide()) {
                slotEntity.triggerSpin();
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide()) {
            if (this.illegal) {
                player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.insert_black_chip"), true);
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.insert_legal_chip"), true);
            }
            if (player instanceof ServerPlayer sp) {
                sp.playNotifySound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, net.minecraft.sounds.SoundSource.BLOCKS, 0.7F, 1.0F);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(PART) == SlotMachinePart.BACK) {
            BlockPos frontPos = pos.relative(state.getValue(FACING));
            BlockState frontState = level.getBlockState(frontPos);
            if (frontState.is(this) && frontState.getValue(PART) == SlotMachinePart.FRONT) {
                return useWithoutItem(frontState, level, frontPos, player, hitResult);
            }
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SlotMachineBlockEntity slotEntity)) {
            return InteractionResult.PASS;
        }

        if (this.illegal && slotEntity.getOwnerUUID() == null && player != null) {
            slotEntity.setOwner(player);
        }

        if (!level.isClientSide() && this.illegal && player.isShiftKeyDown() && player instanceof ServerPlayer sp) {
            if (player.isCreative() || player.hasPermissions(2) || slotEntity.isOwner(player)) {
                if (player.isCreative() || player.hasPermissions(2) || com.soybe.awakeningsystem.item.ClandestineVaultKeyItem.hasKey(player)) {
                    sp.playNotifySound(net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_OPEN, net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 1.3F);
                    PacketDistributor.sendToPlayer(sp, new com.soybe.awakeningsystem.casino.network.OpenClandestineOwnerPayload(
                            pos, slotEntity.getOwnerName(), slotEntity.getReserveChips(),
                            slotEntity.getCollectedVaultChips(), slotEntity.getTotalCustomerSpins(),
                            SlotMachineBlockEntity.countBlackChips(player)
                    ));
                    return InteractionResult.sidedSuccess(level.isClientSide());
                } else {
                    sp.playNotifySound(net.minecraft.sounds.SoundEvents.DISPENSER_FAIL, net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 0.8F);
                    player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.need_key"), true);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.not_owner", slotEntity.getOwnerName()), true);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        if (slotEntity.isSpinning()) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.already_spinning"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {


            com.soybe.awakeningsystem.SystemData data = com.soybe.awakeningsystem.SystemEvents.data(sp);
            long moraBalance = data.mora();
            int chipsAvail = this.illegal ? SlotMachineBlockEntity.countBlackChips(player) : SlotMachineBlockEntity.countLegalChips(player);
            PacketDistributor.sendToPlayer(sp, new OpenSlotMachinePayload(
                    pos, moraBalance, 1, 1, this.illegal, chipsAvail, slotEntity.getReserveChips(), slotEntity.isOwner(player)
            ));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        Direction backDir = facing.getOpposite();
        BlockPos frontPos = context.getClickedPos();
        BlockPos backPos = frontPos.relative(backDir);
        Level level = context.getLevel();
        if (!level.getBlockState(backPos).canBeReplaced(context)) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(PART, SlotMachinePart.FRONT);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            if (state.getValue(PART) == SlotMachinePart.FRONT) {
                Direction facing = state.getValue(FACING);
                BlockPos backPos = pos.relative(facing.getOpposite());
                level.setBlock(backPos, state.setValue(PART, SlotMachinePart.BACK), 3);
            }
            if (this.illegal && placer instanceof Player player) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof SlotMachineBlockEntity slotEntity) {
                    slotEntity.setOwner(player);
                    boolean hasKey = com.soybe.awakeningsystem.item.ClandestineVaultKeyItem.hasKey(player);
                    if (!hasKey) {
                        ItemStack keyStack = new ItemStack(com.soybe.awakeningsystem.SystemItems.CLANDESTINE_VAULT_KEY.get());
                        if (!player.getInventory().add(keyStack)) {
                            player.drop(keyStack, false);
                        }
                        player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.received_key"), false);
                        if (player instanceof ServerPlayer sp) {
                            sp.playNotifySound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                    }
                    player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.installed"), false);
                }
            }
        }
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
            player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.house_protection"), true);
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
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            boolean isProtected = com.soybe.awakeningsystem.world.SeriesStructureBuilder.isInsideBlackMarketDen(level, pos)
                    || com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.isInsideCasino(level, pos);
            if (!player.isCreative() && !player.hasPermissions(2) && isProtected) {
                player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.house_protection"), true);
                return state;
            }

            SlotMachinePart part = state.getValue(PART);
            Direction facing = state.getValue(FACING);
            BlockPos frontPos = (part == SlotMachinePart.FRONT) ? pos : pos.relative(facing);
            BlockEntity be = level.getBlockEntity(frontPos);

            if (this.illegal && be instanceof SlotMachineBlockEntity slotEntity) {
                if (!player.isCreative() && !player.hasPermissions(2) && !slotEntity.isOwner(player)) {
                    player.displayClientMessage(Component.translatable("awakening_system.block.slot_machine.cannot_dismantle", slotEntity.getOwnerName()), true);
                    return state;
                }
            }

            if (part == SlotMachinePart.FRONT) {
                if (be instanceof SlotMachineBlockEntity slotEntity && level instanceof net.minecraft.server.level.ServerLevel sl) {
                    slotEntity.onBlockBroken(sl, pos);
                }
                BlockPos backPos = pos.relative(facing.getOpposite());
                BlockState backState = level.getBlockState(backPos);
                if (backState.is(this) && backState.getValue(PART) == SlotMachinePart.BACK) {
                    level.destroyBlock(backPos, false);
                }
            } else {
                if (frontState(level, frontPos, this)) {
                    if (be instanceof SlotMachineBlockEntity slotEntity && level instanceof net.minecraft.server.level.ServerLevel sl) {
                        slotEntity.onBlockBroken(sl, frontPos);
                    }
                    level.destroyBlock(frontPos, !player.isCreative());
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private static boolean frontState(Level level, BlockPos frontPos, Block block) {
        BlockState frontState = level.getBlockState(frontPos);
        return frontState.is(block) && frontState.getValue(PART) == SlotMachinePart.FRONT;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == SlotMachinePart.FRONT) {
            return new SlotMachineBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (state.getValue(PART) == SlotMachinePart.FRONT) {
            return createTickerHelper(blockEntityType, ModBlockEntities.SLOT_MACHINE.get(), SlotMachineBlockEntity::tick);
        }
        return null;
    }
}
