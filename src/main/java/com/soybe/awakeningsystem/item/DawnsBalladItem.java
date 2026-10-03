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

public class DawnsBalladItem extends SwordItem implements AwakeningWeaponItem {
    public DawnsBalladItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 8, -2.2F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("dawns_ballad_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.dawns_ballad.cooldown", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("dawns_ballad_cd", gameTime + 240L); // 12s cooldown
            sp.getCooldowns().addCooldown(this, 240);

            Vec3 origin = sp.position();
            Vec3 look = sp.getLookAngle();
            double targetX = origin.x + look.x * 7.0D;
            double targetY = origin.y + look.y * 2.0D;
            double targetZ = origin.z + look.z * 7.0D;

            sp.teleportTo(targetX, targetY, targetZ);
            sp.setDeltaMovement(look.x * 0.5D, 0.1D, look.z * 0.5D);
            sp.hurtMarked = true;

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, origin.x, origin.y + 1.0D, origin.z, 40, 0.8D, 1.0D, 0.8D, 0.05D);
                sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 60, 1.0D, 1.0D, 1.0D, 0.1D);
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.8D, 0.8D, 0.8D, 0.05D);
                
                sl.playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2F, 1.4F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.5F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 1.2F);

                AABB box = sp.getBoundingBox().inflate(4.5D, 2.0D, 4.5D);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(sp), 18.0F);
                        nearby.igniteForSeconds(4);
                        Vec3 knock = nearby.position().subtract(sp.position()).normalize().scale(0.8D);
                        nearby.setDeltaMovement(knock.x, 0.4D, knock.z);
                    }
                }
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 1, false, true));
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.05D);
            sl.sendParticles(ParticleTypes.GLOW, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.05D);
            target.igniteForSeconds(3);
            if (attacker instanceof Player p) {
                target.hurt(target.damageSources().playerAttack(p), 4.0F);
            }
        }
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0, false, false));
        attacker.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.dawns_ballad.lore"));
        tooltip.add(Component.translatable("item.awakening_system.dawns_ballad.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.dawns_ballad.passive"));
        tooltip.add(Component.translatable("item.awakening_system.dawns_ballad.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
