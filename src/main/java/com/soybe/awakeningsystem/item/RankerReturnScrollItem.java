package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.TowerManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class RankerReturnScrollItem extends Item {
    public RankerReturnScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            if (TowerManager.isInTower(sp.getUUID())) {
                var session = TowerManager.getSession(sp.getUUID());
                int savedFloor = session != null ? (session.elevatorSpawned ? Math.min(TowerManager.MAX_FLOORS, session.floor + 1) : session.floor) : 1;
                SystemData data = SystemEvents.data(sp);
                data.setSavedTowerFloor(Math.max(data.savedTowerFloor(), savedFloor));
                AwakeningSystemMod.sync(sp);

                TowerManager.exitTowerToSpawn(sp.getServer(), sp.getUUID());

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                sp.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2F, 1.0F);
                AwakeningSystemMod.notify(sp,
                        "awakening_system.tower.return_scroll_title",
                        "awakening_system.tower.return_scroll_desc",
                        0xFFFFD700, data.savedTowerFloor());
                return InteractionResultHolder.consume(stack);
            } else {
                sp.displayClientMessage(Component.translatable("awakening_system.tower.return_scroll_only_in_tower"), true);
                sp.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.item.ranker_return_scroll.tooltip_lore"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("awakening_system.item.ranker_return_scroll.tooltip_desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
