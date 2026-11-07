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

public class RadiantLightSwordItem extends SwordItem implements AwakeningWeaponItem {
    public RadiantLightSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 7, -2.0F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("radiant_light_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("item.awakening_system.radiant_light_sword.cooldown", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("radiant_light_cd", gameTime + 200L);
            sp.getCooldowns().addCooldown(this, 200);

            Vec3 look = sp.getLookAngle();
            sp.setDeltaMovement(look.x * 1.8D, 0.22D, look.z * 1.8D);
            sp.hurtMarked = true;

            if (level instanceof ServerLevel sl) {
                sp.swing(hand, true);
                sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CELESTIAL.get(), sp.getX() + look.x * 1.5D, sp.getY() + 1.2D + look.y * 1.5D, sp.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX() + look.x * 1.5D, sp.getY() + 1.0D, sp.getZ() + look.z * 1.5D, 35, 0.8D, 0.8D, 0.8D, 0.1D);
                sl.sendParticles(ParticleTypes.GLOW, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.05D);

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.8F, 1.5F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.5F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.3F, 1.4F);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 28.0D, 0.55F, 8);

                float dmg = (float) (25.0D * WeaponLevelManager.getDamageMultiplier(stack));

                SlashWaveEntity solarWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_SOLAR_LIGHT, dmg, 4.2F, 1.4F, 0.0F, 24, 8);
                solarWave.setSlashStyle(SlashWaveEntity.STYLE_CELESTIAL);
                solarWave.setDeltaMovement(look.scale(2.2D));
                sl.addFreshEntity(solarWave);

                SlashWaveEntity solarBeam = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_SOLAR_LIGHT, dmg * 1.2F, 1.4F, 3.6F, 0.0F, 20, 10);
                solarBeam.setSlashStyle(SlashWaveEntity.STYLE_THRUST);
                solarBeam.setDeltaMovement(look.scale(2.7D));
                solarBeam.setArmorPenetration(0.40F);
                sl.addFreshEntity(solarBeam);

                sp.removeEffect(MobEffects.POISON);
                sp.removeEffect(MobEffects.WITHER);
                sp.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                sp.removeEffect(MobEffects.WEAKNESS);
                sp.removeEffect(MobEffects.BLINDNESS);
                sp.removeEffect(MobEffects.DARKNESS);
                sp.removeEffect(MobEffects.CONFUSION);

                sp.heal(6.0F);
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1, false, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, true));

                WeaponLevelManager.addExp(stack, 35, sp);
                sp.displayClientMessage(Component.translatable("item.awakening_system.radiant_light_sword.unleashed"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        if (level.isClientSide()) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(look.x * 1.8D, 0.22D, look.z * 1.8D);
                player.getCooldowns().addCooldown(this, 200);
                return InteractionResultHolder.sidedSuccess(stack, true);
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CELESTIAL.get(), target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), SoundSource.PLAYERS, 1.3F, 1.5F);
            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, target.getX(), target.getY(), target.getZ(), 16.0D, 0.25F, 4);

            sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.GLOW, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.01D);
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
            if (target.isInvertedHealAndHarm() && attacker instanceof Player p) {
                target.hurt(target.damageSources().playerAttack(p), 8.0F);
                target.igniteForSeconds(3);
            }
        }
        attacker.heal(1.0F);
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.radiant_light_sword.lore"));
        tooltip.add(Component.translatable("item.awakening_system.radiant_light_sword.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.radiant_light_sword.passive"));
        tooltip.add(Component.translatable("item.awakening_system.radiant_light_sword.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
