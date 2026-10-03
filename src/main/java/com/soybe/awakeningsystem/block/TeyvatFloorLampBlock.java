package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Lámpara de suelo del Pabellón Liuli (Liuli Pavilion Sandbearer Floor Lamp).
 * Se puede encender y apagar haciendo clic derecho, emitiendo luz de nivel 15 al estar activa.
 */
public class TeyvatFloorLampBlock extends Block {
    public static final MapCodec<TeyvatFloorLampBlock> CODEC = simpleCodec(TeyvatFloorLampBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape BASE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 3.0D, 12.0D);
    private static final VoxelShape PILLAR = Block.box(6.0D, 3.0D, 6.0D, 10.0D, 24.0D, 10.0D);
    private static final VoxelShape SHADE = Block.box(3.0D, 20.0D, 3.0D, 13.0D, 32.0D, 13.0D);
    private static final VoxelShape SHAPE = Shapes.or(BASE, PILLAR, SHADE);

    public TeyvatFloorLampBlock(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIT) ? 15 : 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, true));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            boolean current = state.getValue(LIT);
            level.setBlock(pos, state.setValue(LIT, !current), Block.UPDATE_ALL);
            if (!current) {
                level.playSound(null, pos, SoundEvents.COPPER_BULB_TURN_ON, SoundSource.BLOCKS, 1.0F, 1.2F);
            } else {
                level.playSound(null, pos, SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS, 0.8F, 0.9F);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }
}
