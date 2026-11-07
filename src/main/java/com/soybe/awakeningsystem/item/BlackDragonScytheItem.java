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

public class BlackDragonScytheItem extends SwordItem implements AwakeningWeaponItem {
    public BlackDragonScytheItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 13, -3.1F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("black_dragon_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.scythe.cooldown", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("black_dragon_cd", gameTime + 300L);
            sp.getCooldowns().addCooldown(this, 300);

            if (sp.onGround()) {
                sp.setDeltaMovement(sp.getLookAngle().x * 1.2D, 0.7D, sp.getLookAngle().z * 1.2D);
            } else {
                sp.setDeltaMovement(sp.getLookAngle().x * 0.8D, -1.2D, sp.getLookAngle().z * 0.8D);
            }
            sp.hurtMarked = true;

            if (level instanceof ServerLevel sl) {
                Vec3 look = sp.getLookAngle();

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), com.soybe.awakeningsystem.ModSounds.DOMAIN_OF_SANCTION_SLASH.get(), SoundSource.PLAYERS, 1.9F, 0.85F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.3F, 1.4F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 0.9F);

                sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CLAW.get(), sp.getX() + look.x * 1.5D, sp.getY() + 1.2D + look.y * 1.5D, sp.getZ() + look.z * 1.5D, 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.SOUL, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 35, 1.5D, 0.8D, 1.5D, 0.1D);
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 1.4D, 0.6D, 1.4D, 0.12D);

                com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 34.0D, 0.75F, 12);

                float dmg = (float) (32.0D * WeaponLevelManager.getDamageMultiplier(stack));

                SlashWaveEntity scytheWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, SlashWaveEntity.STYLE_SCYTHE_REAP, dmg, 5.6F, 2.2F, -25.0F, 28, 14);
                scytheWave.setDeltaMovement(look.scale(2.25D));
                sl.addFreshEntity(scytheWave);

                sp.swing(hand, true);
                WeaponLevelManager.addExp(stack, 35, sp);
            }

            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, true));
            sp.displayClientMessage(Component.translatable("item.awakening_system.black_dragon_scythe.unleashed"), true);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        if (level.isClientSide()) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                if (player.onGround()) {
                    player.setDeltaMovement(player.getLookAngle().x * 1.2D, 0.7D, player.getLookAngle().z * 1.2D);
                } else {
                    player.setDeltaMovement(player.getLookAngle().x * 0.8D, -1.2D, player.getLookAngle().z * 0.8D);
                }
                player.getCooldowns().addCooldown(this, 300);
                return InteractionResultHolder.sidedSuccess(stack, true);
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.STRIKE_CLAW.get(), target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), com.soybe.awakeningsystem.ModSounds.DOMAIN_OF_SANCTION_SLASH.get(), SoundSource.PLAYERS, 1.4F, 1.1F);
            com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, target.getX(), target.getY(), target.getZ(), 18.0D, 0.3F, 4);

            sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.08D);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.2D, 0.3D, 0.2D, 0.02D);
            target.igniteForSeconds(5);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
        }
        attacker.heal(2.0F);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.black_dragon_scythe.lore"));
        tooltip.add(Component.translatable("item.awakening_system.black_dragon_scythe.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.black_dragon_scythe.passive"));
        tooltip.add(Component.translatable("item.awakening_system.black_dragon_scythe.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
