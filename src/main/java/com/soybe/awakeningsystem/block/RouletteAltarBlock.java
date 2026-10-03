package com.soybe.awakeningsystem.block;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import com.soybe.awakeningsystem.block.entity.RouletteAltarBlockEntity;
import com.soybe.awakeningsystem.item.RouletteTokenItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RouletteAltarBlock extends BaseEntityBlock {
    public static final com.mojang.serialization.MapCodec<RouletteAltarBlock> CODEC = simpleCodec(RouletteAltarBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public RouletteAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE =
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 16.0D);

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RouletteAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ROULETTE_ALTAR.get(), RouletteAltarBlockEntity::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RouletteAltarBlockEntity rouletteBE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (rouletteBE.isSpinning()) {
            if (player instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.translatable("awakening_system.action.roulette_spinning"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        if (stack.getItem() instanceof RouletteTokenItem) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                if (rouletteBE.startSpin(sp)) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.VAULT_INSERT_ITEM, SoundSource.BLOCKS, 1.0F, 1.2F);
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 1.0F);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Si el jugador hace clic con cualquier otro ítem que no sea la Tirada de Ruleta del Sistema
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            level.playSound(null, pos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.9F, 1.2F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 0.65F);
            sp.displayClientMessage(Component.translatable("awakening_system.altar.need_token_mandatory"), true);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RouletteAltarBlockEntity rouletteBE)) {
            return InteractionResult.PASS;
        }

        if (rouletteBE.isSpinning()) {
            if (player instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.translatable("awakening_system.action.roulette_spinning"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // Clic derecho con mano vacía rechazado
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            level.playSound(null, pos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.9F, 1.2F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 0.65F);
            sp.displayClientMessage(Component.translatable("awakening_system.altar.need_token_mandatory"), true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            double px = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
            double py = pos.getY() + 1.05D;
            double pz = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;

            level.addParticle(ParticleTypes.ENCHANT, px, py, pz, (random.nextDouble() - 0.5D) * 0.2D, 0.1D, (random.nextDouble() - 0.5D) * 0.2D);
            if (random.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0D, 0.015D, 0.0D);
            }
        }
    }
}

