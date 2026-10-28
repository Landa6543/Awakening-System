package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemAttributeEffects;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class OliveElixirItem extends Item {
    public OliveElixirItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            if (!data.resetAttributes()) {
                sp.displayClientMessage(Component.translatable("item.awakening_system.olive_elixir.no_attributes"), true);
                sp.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8F, 1.0F);
                return InteractionResultHolder.fail(stack);
            }

            SystemAttributeEffects.apply(sp);
            AwakeningSystemMod.sync(sp);

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 0.5D, 0.8D, 0.5D, 0.3D);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 0.6D, sp.getZ(), 30, 0.4D, 0.5D, 0.4D, 0.05D);

            sp.playNotifySound(SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

            sp.displayClientMessage(Component.translatable("item.awakening_system.olive_elixir.use_message"), true);
            AwakeningSystemMod.notify(sp, "awakening_system.notification.shop.title",
                    "awakening_system.notification.reset_success", 0xFF50FA7B);

            if (!player.isCreative()) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    return InteractionResultHolder.sidedSuccess(new ItemStack(Items.GLASS_BOTTLE), level.isClientSide());
                } else {
                    if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                        player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                    }
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.olive_elixir.title"));
        tooltip.add(Component.translatable("item.awakening_system.olive_elixir.desc"));
        tooltip.add(Component.translatable("item.awakening_system.olive_elixir.effect"));
    }
}
