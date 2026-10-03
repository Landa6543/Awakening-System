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

public class GiganticGreatswordItem extends SwordItem implements AwakeningWeaponItem {
    public GiganticGreatswordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 14, -3.15F))); // 18 Damage, 0.85 Attack Speed
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("greatsword_cross_impact_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.greatsword.cooldown", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("greatsword_cross_impact_cd", gameTime + 280L); // 14s cooldown
            sp.getCooldowns().addCooldown(this, 280);

            // Seismic leap & slam: si está en el suelo, salta hacia adelante/arriba; si está en el aire, picado hacia el suelo
            if (sp.onGround()) {
                sp.setDeltaMovement(sp.getLookAngle().x * 0.8D, 0.65D, sp.getLookAngle().z * 0.8D);
            } else {
                sp.setDeltaMovement(sp.getLookAngle().x * 0.5D, -1.2D, sp.getLookAngle().z * 0.5D);
            }
            sp.hurtMarked = true;

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.EXPLOSION, sp.getX(), sp.getY(), sp.getZ(), 4, 1.5D, 0.5D, 1.5D, 0.1D);
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, sp.getX(), sp.getY(), sp.getZ(), 50, 2.5D, 0.5D, 2.5D, 0.1D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 0.8F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.3F, 0.6F);

                AABB box = sp.getBoundingBox().inflate(6.0D, 3.0D, 6.0D);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(sp), 26.0F);
                        nearby.setDeltaMovement(0.0D, 0.85D, 0.0D); // Launch into air
                        nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    }
                }
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, false, true));
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        if (level.isClientSide()) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                if (player.onGround()) {
                    player.setDeltaMovement(player.getLookAngle().x * 0.8D, 0.65D, player.getLookAngle().z * 0.8D);
                } else {
                    player.setDeltaMovement(player.getLookAngle().x * 0.5D, -1.2D, player.getLookAngle().z * 0.5D);
                }
                player.getCooldowns().addCooldown(this, 280);
                return InteractionResultHolder.sidedSuccess(stack, true);
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.5D, target.getZ(), 1, 0.2D, 0.2D, 0.2D, 0.0D);
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 25, 0.6D, 0.6D, 0.6D, 0.2D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 1.2F, 0.7F);

            // Broad cleave shockwave
            AABB cleave = target.getBoundingBox().inflate(3.5D, 1.5D, 3.5D);
            for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, cleave)) {
                if (nearby != attacker && nearby != target && (nearby instanceof Enemy)) {
                    if (attacker instanceof Player p) {
                        nearby.hurt(nearby.damageSources().playerAttack(p), 8.0F);
                    }
                    nearby.knockback(0.7D, attacker.getX() - nearby.getX(), attacker.getZ() - nearby.getZ());
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.gigantic_greatsword.lore"));
        tooltip.add(Component.translatable("item.awakening_system.gigantic_greatsword.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.gigantic_greatsword.passive"));
        tooltip.add(Component.translatable("item.awakening_system.gigantic_greatsword.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
