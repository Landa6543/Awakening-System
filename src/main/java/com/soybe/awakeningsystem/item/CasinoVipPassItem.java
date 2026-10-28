package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class CasinoVipPassItem extends Item {

    public CasinoVipPassItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    public static boolean isBound(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null && cd.copyTag().contains("bound_player_uuid") && !cd.copyTag().getString("bound_player_uuid").isEmpty();
    }

    public static String getBoundUuid(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return "";
        return cd.copyTag().getString("bound_player_uuid");
    }

    public static String getBoundName(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return "";
        return cd.copyTag().getString("bound_player_name");
    }

    public static void bindToPlayer(ItemStack stack, ServerPlayer player) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = cd != null ? cd.copyTag() : new CompoundTag();
        tag.putString("bound_player_uuid", player.getUUID().toString());
        tag.putString("bound_player_name", player.getGameProfile().getName());
        tag.putString("bound_date", java.time.LocalDate.now().toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("§6✦ Pase VIP Oficial §7(" + player.getGameProfile().getName() + ")"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            if (!isBound(stack)) {
                // Registrar por primera vez al usuario
                bindToPlayer(stack, sp);
                sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.casino_vip_pass.registered", sp.getGameProfile().getName()), false);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }

            String boundUuid = getBoundUuid(stack);
            String boundName = getBoundName(stack);

            if (boundUuid.equals(sp.getUUID().toString())) {
                sp.displayClientMessage(Component.translatable("item.awakening_system.casino_vip_pass.valid", boundName), true);
                sp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            } else {
                sp.displayClientMessage(Component.translatable("item.awakening_system.casino_vip_pass.invalid", boundName), true);
                sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.desc2"));

        if (isBound(stack)) {
            String name = getBoundName(stack);
            tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.owner_bound", (name.isEmpty() ? "Acreditado" : name)).withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.status_bound").withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.owner_unbound").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.bind_hint").withStyle(ChatFormatting.AQUA));
        }

        tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.footer"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
