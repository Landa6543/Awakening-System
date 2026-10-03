package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemData;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class DeathKingClockItem extends Item {
    public DeathKingClockItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("death_king_active_cooldown");

            if (gameTime < cdUntil) {
                long remainingSec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.death_king_clock.cooldown", remainingSec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("death_king_active_cooldown", gameTime + 2400L);
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2, false, true, true));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 2, false, true, true));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1, false, true, true));
            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0, false, true, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.1D);
                sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.2D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.0F, 1.2F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.5F);
            }

            AwakeningSystemMod.notify(sp,
                    "awakening_system.item.death_king_clock.title",
                    "awakening_system.item.death_king_clock.desc",
                    0xFF55FF55);

            if (data != null && !data.skills().containsKey("death_king_rewind")) {
                data.skills().put("death_king_rewind", 1);
                data.gainSkillProgress("death_king_rewind", 1);
                AwakeningSystemMod.notify(sp,
                        "awakening_system.item.death_king_clock.skill_title",
                        "awakening_system.item.death_king_clock.skill_desc",
                        0xFFFFD866);
                AwakeningSystemMod.sync(sp);
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.death_king_clock.lore"));
        tooltip.add(Component.translatable("item.awakening_system.death_king_clock.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.death_king_clock.passive"));
        tooltip.add(Component.translatable("item.awakening_system.death_king_clock.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
