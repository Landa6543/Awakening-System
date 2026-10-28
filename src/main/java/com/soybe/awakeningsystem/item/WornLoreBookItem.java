package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;

import java.util.List;

public class WornLoreBookItem extends Item {
    public WornLoreBookItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createVolume(int volume) {
        ItemStack stack = new ItemStack(SystemItems.WORN_LORE_BOOK.get());
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putInt("Volume", volume);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        switch (volume) {
            case 2 -> {
                stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.awakening_system.worn_lore_book.vol2.name"));
                stack.set(DataComponents.LORE, new ItemLore(List.of(
                        Component.translatable("item.awakening_system.worn_lore_book.vol2.lore1"),
                        Component.translatable("item.awakening_system.worn_lore_book.vol2.lore2")
                )));
            }
            case 3 -> {
                stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.awakening_system.worn_lore_book.vol3.name"));
                stack.set(DataComponents.LORE, new ItemLore(List.of(
                        Component.translatable("item.awakening_system.worn_lore_book.vol3.lore1"),
                        Component.translatable("item.awakening_system.worn_lore_book.vol3.lore2")
                )));
            }
            default -> {
                stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.awakening_system.worn_lore_book.vol1.name"));
                stack.set(DataComponents.LORE, new ItemLore(List.of(
                        Component.translatable("item.awakening_system.worn_lore_book.vol1.lore1"),
                        Component.translatable("item.awakening_system.worn_lore_book.vol1.lore2")
                )));
            }
        }
        return stack;
    }

    public static int getVolume(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.contains("Volume")) {
            return cd.copyTag().getInt("Volume");
        }
        return 1;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_NAME)) {
            int vol = getVolume(stack);
            return switch (vol) {
                case 2 -> Component.translatable("item.awakening_system.worn_lore_book.vol2.name");
                case 3 -> Component.translatable("item.awakening_system.worn_lore_book.vol3.name");
                default -> Component.translatable("item.awakening_system.worn_lore_book.vol1.name");
            };
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        if (!stack.has(DataComponents.LORE)) {
            int vol = getVolume(stack);
            switch (vol) {
                case 2 -> {
                    tooltipComponents.add(Component.translatable("item.awakening_system.worn_lore_book.vol2.lore1"));
                    tooltipComponents.add(Component.translatable("item.awakening_system.worn_lore_book.vol2.lore2"));
                }
                case 3 -> {
                    tooltipComponents.add(Component.translatable("item.awakening_system.worn_lore_book.vol3.lore1"));
                    tooltipComponents.add(Component.translatable("item.awakening_system.worn_lore_book.vol3.lore2"));
                }
                default -> {
                    tooltipComponents.add(Component.translatable("item.awakening_system.worn_lore_book.vol1.lore1"));
                    tooltipComponents.add(Component.translatable("item.awakening_system.worn_lore_book.vol1.lore2"));
                }
            }
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int vol = getVolume(stack);

        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            tag.putInt("Volume", 1);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }

        if (level.isClientSide()) {
            player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
            AwakeningSystemClient.openWornBookScreen(vol);
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
