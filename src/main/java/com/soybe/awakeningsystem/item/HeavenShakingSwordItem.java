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
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HeavenShakingSwordItem extends SwordItem implements AwakeningWeaponItem {
    public HeavenShakingSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 10, -2.4F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("heaven_shaking_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.heaven_shaking.cooldown", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("heaven_shaking_cd", gameTime + 280L); // 14s cooldown
            sp.getCooldowns().addCooldown(this, 280);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 8, 1.2D, 0.4D, 1.2D, 0.0D);
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 60, 2.0D, 0.6D, 2.0D, 0.08D);
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 1.5D, 0.5D, 1.5D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_BREAK_BLOCK, SoundSource.PLAYERS, 1.3F, 0.8F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.5F, 0.9F);

                AABB box = sp.getBoundingBox().inflate(5.5D, 2.5D, 5.5D);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(sp), 22.0F);
                        nearby.setDeltaMovement(0.0D, 0.7D, 0.0D);
                        nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    }
                }
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1, false, true));
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            
            // Regressor's Will bonus when wounded
            if (attacker.getHealth() < attacker.getMaxHealth() * 0.5F && attacker instanceof Player p) {
                target.hurt(target.damageSources().playerAttack(p), 6.0F);
                attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, false, false));
                sl.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.8F, 1.6F);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.heaven_shaking_sword.lore"));
        tooltip.add(Component.translatable("item.awakening_system.heaven_shaking_sword.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.heaven_shaking_sword.passive"));
        tooltip.add(Component.translatable("item.awakening_system.heaven_shaking_sword.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
