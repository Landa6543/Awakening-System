package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Hunter Association's loot bag, handed out by the daily commission. Its colour shows the tier
 * (one per rank band); right-click and the loot of that tier drops straight into the inventory.
 */
public class LootBagItem extends Item {
    /** Bag colour per tier 1..6: green, blue, purple, gold, crimson, white-cyan. */
    public static final int[] COLORS = {0x5BC25B, 0x4A8CFF, 0xA45BFF, 0xFFB52E, 0xE0383E, 0x9FF6FF};
    private static final ChatFormatting[] NAME_COLORS = {
            ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.GOLD, ChatFormatting.RED, ChatFormatting.AQUA};

    public LootBagItem(Properties properties) {
        super(properties);
    }

    public static ItemStack of(int tier) {
        ItemStack stack = new ItemStack(SystemItems.GUILD_LOOT_BAG.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt("tier", Math.max(1, Math.min(6, tier)));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static int tier(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 1;
        int t = data.copyTag().getInt("tier");
        return t < 1 ? 1 : Math.min(6, t);
    }

    @Override
    public Component getName(ItemStack stack) {
        int t = tier(stack);
        return Component.translatable("item.awakening_system.guild_loot_bag.tier" + t).withStyle(NAME_COLORS[t - 1]);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            int t = tier(stack);
            stack.consume(1, player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 1.0F);
            SystemEvents.openLootBag(sp, t);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.guild_loot_bag.desc").withStyle(ChatFormatting.GRAY));
    }
}
