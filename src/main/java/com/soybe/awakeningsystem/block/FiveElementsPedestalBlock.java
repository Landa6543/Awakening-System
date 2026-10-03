package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.world.WuXingPuzzleManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FiveElementsPedestalBlock extends Block {
    public static final MapCodec<FiveElementsPedestalBlock> CODEC = simpleCodec(FiveElementsPedestalBlock::new);
    public static final EnumProperty<WuXingElement> ELEMENT = EnumProperty.create("element", WuXingElement.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape BASE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D);
    private static final VoxelShape PILLAR = Block.box(4.0D, 3.0D, 4.0D, 12.0D, 12.0D, 12.0D);
    private static final VoxelShape TOP = Block.box(2.0D, 12.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE = Shapes.or(BASE, PILLAR, TOP);

    public FiveElementsPedestalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ELEMENT, WuXingElement.WOOD)
                .setValue(ACTIVE, false));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ELEMENT, ACTIVE);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            WuXingPuzzleManager.interact(sp, pos, state.getValue(ELEMENT));
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            WuXingPuzzleManager.interact(sp, pos, state.getValue(ELEMENT));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        WuXingElement element = state.getValue(ELEMENT);
        if (random.nextInt(4) == 0) {
            double px = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double py = pos.getY() + 1.05D;
            double pz = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            level.addParticle(element.getParticle(), px, py, pz, 0.0D, 0.02D, 0.0D);
        }
    }
}
