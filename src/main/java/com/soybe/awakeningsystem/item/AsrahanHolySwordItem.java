package com.soybe.awakeningsystem.item;

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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AsrahanHolySwordItem extends SwordItem implements AwakeningWeaponItem {
    public AsrahanHolySwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 12, -2.9F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("asrahan_holy_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.asrahan.cooldown", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("asrahan_holy_cd", gameTime + 280L); // 14s cooldown
            sp.getCooldowns().addCooldown(this, 280);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SPLASH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 80, 2.0D, 1.0D, 2.0D, 0.2D);
                sl.sendParticles(ParticleTypes.NAUTILUS, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 1.5D, 1.0D, 1.5D, 0.1D);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 1.8D, 1.0D, 1.8D, 0.15D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.PLAYERS, 1.4F, 1.2F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.1F);

                AABB box = sp.getBoundingBox().inflate(6.0D, 2.5D, 6.0D);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(sp), 20.0F);
                        nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                    }
                }
            }

            sp.clearFire();
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, true));
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.1D);
            sl.sendParticles(ParticleTypes.GLOW, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.2D, 0.3D, 0.2D, 0.05D);
            if (attacker instanceof Player p) {
                target.hurt(target.damageSources().playerAttack(p), 5.0F);
            }
        }
        attacker.clearFire();
        attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.asrahan_holy_sword.lore"));
        tooltip.add(Component.translatable("item.awakening_system.asrahan_holy_sword.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.asrahan_holy_sword.passive"));
        tooltip.add(Component.translatable("item.awakening_system.asrahan_holy_sword.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
