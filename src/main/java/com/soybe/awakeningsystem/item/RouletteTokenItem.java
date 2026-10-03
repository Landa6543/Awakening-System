package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.block.RouletteAltarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class RouletteTokenItem extends Item {
    public RouletteTokenItem(Properties properties) {
        super(properties.stacksTo(64).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();

        if (level.getBlockState(pos).getBlock() instanceof RouletteAltarBlock) {
            return InteractionResult.PASS;
        }

        if (player != null) {
            player.displayClientMessage(Component.translatable("item.awakening_system.roulette_token.must_use_on_altar"), true);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("item.awakening_system.roulette_token.must_use_on_altar"), true);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.roulette_token.desc"));
        tooltip.add(Component.translatable("item.awakening_system.roulette_token.use_altar"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}

