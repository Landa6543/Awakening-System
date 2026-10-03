package com.soybe.awakeningsystem.item.pouch;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PouchItem extends Item {
    private final PouchTier tier;

    public PouchItem(PouchTier tier) {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(tier.getRarity()));
        this.tier = tier;
    }

    public PouchTier getTier() {
        return tier;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.awakening_system.pouch.capacity", tier.getSlotCount())
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.pouch.lore")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.8F, 1.1F);

            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable(tier.getTranslationKey());
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    return new PouchMenu(containerId, playerInventory, hand, tier);
                }
            }, buf -> {
                buf.writeEnum(hand);
                buf.writeEnum(tier);
            });
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }
}
