package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class VigorPotionItem extends Item {
    public VigorPotionItem(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.restoreEnergy(100);
            AwakeningSystemMod.sync(sp);

            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 0));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.FLAME, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.05D);
            sl.sendParticles(ParticleTypes.CRIT, sp.getX(), sp.getY() + 0.8D, sp.getZ(), 25, 0.4D, 0.5D, 0.4D, 0.1D);

            sp.playNotifySound(SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.1F);
            sp.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.5F);

            sp.displayClientMessage(Component.translatable("item.awakening_system.vigor_potion.use_message"), true);
            AwakeningSystemMod.notify(sp, "awakening_system.notification.shop.title",
                    "awakening_system.notification.energy_restored", 0xFF50FA7B);

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
        tooltip.add(Component.translatable("item.awakening_system.vigor_potion.title"));
        tooltip.add(Component.translatable("item.awakening_system.vigor_potion.desc"));
        tooltip.add(Component.translatable("item.awakening_system.vigor_potion.effect"));
    }
}
