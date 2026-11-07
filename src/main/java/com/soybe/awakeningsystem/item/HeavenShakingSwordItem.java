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
import com.soybe.awakeningsystem.entity.SlashWaveEntity;

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

            sp.getPersistentData().putLong("heaven_shaking_cd", gameTime + 280L);
            sp.getCooldowns().addCooldown(this, 280);

            if (level instanceof ServerLevel sl) {
                Vec3 look = sp.getLookAngle();

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.9F, 0.9F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.2F, 1.3F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_BREAK_BLOCK, SoundSource.PLAYERS, 1.3F, 0.9F);

                sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_SLASH.get(), sp.getX() + look.x * 1.5D, sp.getY() + 1.2D + look.y * 1.5D, sp.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 40, 1.5D, 1.0D, 1.5D, 0.15D);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 36.0D, 0.85F, 14);

                float dmg = (float) (28.0D * WeaponLevelManager.getDamageMultiplier(stack));

                SlashWaveEntity heavenWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_COSMIC_AMETHYST, SlashWaveEntity.STYLE_INK_WUXIA, dmg * 1.1F, 5.2F, 2.0F, 0.0F, 28, 12);
                heavenWave.setDeltaMovement(look.scale(2.25D));
                sl.addFreshEntity(heavenWave);

                SlashWaveEntity verticalCleave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_COSMIC_AMETHYST, SlashWaveEntity.STYLE_HEAVY_CLEAVE, dmg * 0.9F, 3.8F, 2.6F, 90.0F, 24, 8);
                verticalCleave.setDeltaMovement(look.scale(2.1D));
                sl.addFreshEntity(verticalCleave);

                sp.swing(hand, true);
                WeaponLevelManager.addExp(stack, 35, sp);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1, false, true));
            sp.displayClientMessage(Component.translatable("item.awakening_system.heaven_shaking_sword.unleashed"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CLEAVE.get(), target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.4F, 0.85F);
            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, target.getX(), target.getY(), target.getZ(), 18.0D, 0.3F, 4);

            sl.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            
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
