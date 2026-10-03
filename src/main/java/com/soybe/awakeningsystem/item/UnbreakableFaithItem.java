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

public class UnbreakableFaithItem extends SwordItem implements AwakeningWeaponItem {
    public UnbreakableFaithItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 7, -2.2F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("unbreakable_faith_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.literal("§c⏳ Fe Inquebrantable en recarga (" + sec + "s)..."), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("unbreakable_faith_cd", gameTime + 260L); // 13s cooldown
            sp.getCooldowns().addCooldown(this, 260);

            Vec3 look = sp.getLookAngle();
            sp.setDeltaMovement(look.x * 1.6D, 0.25D, look.z * 1.6D);
            sp.hurtMarked = true;

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 1.2D, 0.8D, 1.2D, 0.1D);
                sl.sendParticles(ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 60, 1.5D, 1.0D, 1.5D, 0.2D);
                sl.sendParticles(ParticleTypes.FIREWORK, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.8D, 0.5D, 0.8D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.4F, 1.3F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 1.4F);

                AABB box = sp.getBoundingBox().inflate(5.0D, 2.0D, 5.0D);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(sp), 18.0F);
                        nearby.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                    }
                }
            }

            // Fourth Wall Barrier: massive resistance for 3s
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 3, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, true));
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        if (level.isClientSide()) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(look.x * 1.6D, 0.25D, look.z * 1.6D);
                player.getCooldowns().addCooldown(this, 260);
                return InteractionResultHolder.sidedSuccess(stack, true);
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FIREWORK, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.1D);
            if (attacker instanceof Player p) {
                target.hurt(target.damageSources().playerAttack(p), 4.0F);
            }
        }
        attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, false));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.unbreakable_faith.lore"));
        tooltip.add(Component.translatable("item.awakening_system.unbreakable_faith.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.unbreakable_faith.passive"));
        tooltip.add(Component.translatable("item.awakening_system.unbreakable_faith.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
