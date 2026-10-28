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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class ConstellationNectarItem extends Item {

    public ConstellationNectarItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(16));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.setMurimPhysique("");
            AwakeningSystemMod.sync(sp);

            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 45, 0.5D, 0.8D, 0.5D, 0.08D);
            sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 0.8D, sp.getZ(), 60, 0.6D, 0.6D, 0.6D, 0.15D);

            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.1F, 1.4F);
            sp.playNotifySound(SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 1.0F);

            sp.displayClientMessage(Component.translatable("item.awakening_system.constellation_nectar.purified"), false);
            AwakeningSystemMod.notify(sp, "item.awakening_system.constellation_nectar",
                    "item.awakening_system.constellation_nectar.notify_desc", 0xFFD055FF);

            if (!sp.isCreative()) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.title"));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.desc"));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.effect1"));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.effect2"));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.effect3"));
    }
}
