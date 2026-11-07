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

public class SkyDemonSwordItem extends SwordItem implements AwakeningWeaponItem {
    public SkyDemonSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 9, -2.3F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("sky_demon_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("item.awakening_system.sky_demon_sword.cooldown", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("sky_demon_cd", gameTime + 240L);
            sp.getCooldowns().addCooldown(this, 240);

            Vec3 look = sp.getLookAngle();
            Vec3 pos = sp.getEyePosition();

            if (level instanceof ServerLevel sl) {
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.DOMAIN_OF_SANCTION_SLASH.get(), SoundSource.PLAYERS, 1.9F, 0.9F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 1.2F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.4F, 1.3F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.8F, 0.7F);

                sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CLAW.get(), sp.getX() + look.x * 1.5D, sp.getY() + 1.2D + look.y * 1.5D, sp.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 40, 1.5D, 0.8D, 1.5D, 0.1D);
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 50, 1.5D, 1.0D, 1.5D, 0.15D);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 36.0D, 0.80F, 13);

                float dmg = (float) (28.0D * WeaponLevelManager.getDamageMultiplier(stack));

                SlashWaveEntity demonCrescent = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRIMSON_BLOOD, SlashWaveEntity.STYLE_INK_WUXIA, dmg * 1.25F, 5.0F, 1.9F, 0.0F, 28, 12);
                demonCrescent.setDeltaMovement(look.scale(2.35D));
                demonCrescent.setArmorPenetration(0.40F);
                sl.addFreshEntity(demonCrescent);

                SlashWaveEntity verticalCutter = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, SlashWaveEntity.STYLE_SHADOW_CLAW, dmg * 0.9F, 3.6F, 2.4F, 90.0F, 24, 8);
                verticalCutter.setDeltaMovement(look.scale(2.2D));
                sl.addFreshEntity(verticalCutter);

                sp.swing(hand, true);
                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1, false, true));
                sp.displayClientMessage(Component.translatable("item.awakening_system.sky_demon_sword.unleashed"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CLAW.get(), target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), com.soybe.awakeningsystem.ModSounds.DOMAIN_OF_SANCTION_SLASH.get(), SoundSource.PLAYERS, 1.4F, 1.15F);
            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, target.getX(), target.getY(), target.getZ(), 18.0D, 0.28F, 4);

            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 25, 0.3D, 0.4D, 0.3D, 0.08D);
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.2D, 0.3D, 0.2D, 0.02D);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            if (attacker instanceof Player p) {
                target.hurt(target.damageSources().playerAttack(p), 4.0F);
            }
        }
        attacker.heal(2.0F);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.sky_demon_sword.lore"));
        tooltip.add(Component.translatable("item.awakening_system.sky_demon_sword.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.sky_demon_sword.passive"));
        tooltip.add(Component.translatable("item.awakening_system.sky_demon_sword.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
