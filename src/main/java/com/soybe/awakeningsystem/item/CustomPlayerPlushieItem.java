package com.soybe.awakeningsystem.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class CustomPlayerPlushieItem extends BlockItem implements Equipable {
    public CustomPlayerPlushieItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return this.swapWithEquipmentSlot(this, level, player, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        if (profile != null && profile.name().isPresent()) {
            return Component.translatable("block.awakening_system.custom_player_plushie.named", profile.name().get());
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        if (profile != null && profile.name().isPresent()) {
            tooltip.add(Component.translatable("item.awakening_system.custom_plushie.owner", profile.name().get()));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.custom_plushie.hint"));
        }
        tooltip.add(Component.translatable("item.awakening_system.plushie.squeeze"));
        tooltip.add(Component.translatable("item.awakening_system.plushie.wear"));
    }
}
