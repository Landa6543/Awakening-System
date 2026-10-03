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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class RealmBreakthroughPillItem extends Item {
    public RealmBreakthroughPillItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            int realm = data.dantianRealm();
            int subLevel = data.cultivationSubLevel();
            ServerLevel sl = sp.serverLevel();

            if (realm >= 9) {
                data.addCultivationQi(5000);
                sp.heal(20.0F);
                sp.displayClientMessage(Component.translatable("awakening_system.action.dantian_realm_max"), true);
            } else if (subLevel < 9) {
                data.setCultivationSubLevel(9);
                data.setCultivationQi(data.maxCultivationQi());
                sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getEyeY(), sp.getZ(), 3, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 60, 0.8D, 1.0D, 0.8D, 0.15D);
                sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.realm_breakthrough_pill.pinnacle", SystemEvents.getDantianRealmComponent(realm)), true);
                AwakeningSystemMod.notify(sp, "awakening_system.item.breakthrough_pill.peak_title", "awakening_system.item.breakthrough_pill.peak_desc", 0xFFD055FF, SystemEvents.getDantianRealmComponent(realm).getString());
            } else {
                data.setDantianRealm(realm + 1);
                data.setCultivationSubLevel(1);
                data.setCultivationQi(0);
                sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getEyeY(), sp.getZ(), 5, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 90, 1.2D, 1.2D, 1.2D, 0.25D);
                sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 1.0D, 1.0D, 1.0D, 0.1D);
                sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
                sp.displayClientMessage(Component.translatable("awakening_system.action.dantian_realm_success", SystemEvents.getDantianRealmComponent(realm + 1)), true);
                AwakeningSystemMod.notify(sp, "awakening_system.item.breakthrough_pill.success_title", "awakening_system.item.breakthrough_pill.success_desc", 0xFFFFD700, SystemEvents.getDantianRealmComponent(realm + 1).getString());
            }

            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 3));

            AwakeningSystemMod.sync(sp);
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.realm_breakthrough_pill.desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
