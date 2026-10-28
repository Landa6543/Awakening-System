package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.entity.LimboDoorEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class LimboDoorItem extends Item {

    public LimboDoorItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = clickedPos.relative(face);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            LimboDoorEntity door = new LimboDoorEntity(ModEntities.LIMBO_DOOR.get(), serverLevel);
            Player player = context.getPlayer();
            Direction dir = player != null ? player.getDirection().getOpposite() : Direction.NORTH;
            float yaw = dir.toYRot();

            door.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
            door.setYRot(yaw);
            door.yRotO = yaw;
            door.setYHeadRot(yaw);
            door.setYBodyRot(yaw);
            serverLevel.addFreshEntity(door);

            serverLevel.playSound(null, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D,
                    SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.2F);

            ItemStack stack = context.getItemInHand();
            if (player != null && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
