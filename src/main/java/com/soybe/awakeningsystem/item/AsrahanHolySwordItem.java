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

            sp.getPersistentData().putLong("asrahan_holy_cd", gameTime + 280L);
            sp.getCooldowns().addCooldown(this, 280);

            if (level instanceof ServerLevel sl) {
                Vec3 look = sp.getLookAngle();

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.8F, 1.4F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.PLAYERS, 1.6F, 1.2F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.3F, 1.6F);

                sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CELESTIAL.get(), sp.getX() + look.x * 1.5D, sp.getY() + 1.2D + look.y * 1.5D, sp.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 45, 1.5D, 0.8D, 1.5D, 0.1D);
                sl.sendParticles(ParticleTypes.GLOW, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 35, 1.2D, 0.8D, 1.2D, 0.05D);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 30.0D, 0.60F, 9);

                float dmg = (float) (28.0D * WeaponLevelManager.getDamageMultiplier(stack));

                SlashWaveEntity tidalWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, SlashWaveEntity.STYLE_CELESTIAL, dmg, 4.8F, 1.7F, 0.0F, 26, 10);
                tidalWave.setDeltaMovement(look.scale(2.25D));
                sl.addFreshEntity(tidalWave);

                sp.swing(hand, true);
                WeaponLevelManager.addExp(stack, 35, sp);
            }

            sp.clearFire();
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, true));

            sp.displayClientMessage(Component.translatable("item.awakening_system.asrahan.unleashed"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CELESTIAL.get(), target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.3F, 1.4F);
            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, target.getX(), target.getY(), target.getZ(), 16.0D, 0.25F, 4);

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
