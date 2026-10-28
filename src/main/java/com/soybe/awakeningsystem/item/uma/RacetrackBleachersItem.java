package com.soybe.awakeningsystem.item.uma;

import com.soybe.awakeningsystem.block.RacetrackBleachersBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Item de Gradas de Hipódromo.
 * Permite la colocación estándar sobre cualquier bloque, o conversión directa de losas bajas (SlabType.BOTTOM)
 * sustituyendo la losa por la grada a ras de piso sin desfasar la altura ni dejar huecos de aire.
 */
public class RacetrackBleachersItem extends BlockItem {

    public RacetrackBleachersItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (clickedState.getBlock() instanceof SlabBlock && clickedState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
            Player player = context.getPlayer();
            Direction facing = context.getHorizontalDirection().getOpposite();

            BlockState newState = this.getBlock().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
            if (this.getBlock() instanceof RacetrackBleachersBlock bleachers) {
                newState = bleachers.updateBleacherShape(newState, level, clickedPos);
            }

            if (!level.isClientSide()) {
                level.setBlock(clickedPos, newState, 3);
                level.playSound(null, clickedPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);

                if (player != null && !player.isCreative()) {
                    context.getItemInHand().shrink(1);
                    ItemStack droppedSlab = new ItemStack(clickedState.getBlock());
                    if (!player.getInventory().add(droppedSlab)) {
                        player.drop(droppedSlab, false);
                    }
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }
}
