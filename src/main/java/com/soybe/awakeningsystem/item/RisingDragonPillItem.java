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

public class RisingDragonPillItem extends Item {
    public RisingDragonPillItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            data.addCultivationQi(5000);

            int subLevel = data.cultivationSubLevel();
            int realm = data.dantianRealm();
            if (data.cultivationQi() >= data.maxCultivationQi() && subLevel < 9) {
                data.setCultivationSubLevel(subLevel + 1);
                data.setCultivationQi(0);
                sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.3F);
                sp.displayClientMessage(Component.translatable("awakening_system.cultivation.sublevel_success", data.cultivationSubLevel(), SystemEvents.getDantianRealmComponent(realm)), true);
            } else {
                sp.displayClientMessage(Component.translatable("item.awakening_system.rising_dragon_pill.used", 5000), true);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400, 0));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.6D, 0.6D, 0.6D, 0.15D);
            sl.sendParticles(ParticleTypes.FLAME, sp.getX(), sp.getY() + 0.8D, sp.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);

            sp.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.2F, 1.4F);
            sp.playNotifySound(SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.2F);

            AwakeningSystemMod.notify(sp, "awakening_system.item.rising_dragon_pill.title", "awakening_system.item.rising_dragon_pill.desc", 0xFFFFD700);

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
        tooltip.add(Component.translatable("item.awakening_system.rising_dragon_pill.desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
