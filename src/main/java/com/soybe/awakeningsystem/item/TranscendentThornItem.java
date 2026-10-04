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
import com.soybe.awakeningsystem.CameraShakePayload;
import com.soybe.awakeningsystem.SpawnMagicCirclePayload;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class TranscendentThornItem extends SwordItem implements AwakeningWeaponItem {
    public TranscendentThornItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 10, -2.6F))); // 14 Damage, 1.4 Attack Speed
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("thorn_ignition_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.thorn.cooldown", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("thorn_ignition_cd", gameTime + 500L); // 25s cooldown
            sp.getCooldowns().addCooldown(this, 500);

            if (level instanceof ServerLevel sl) {
                Vec3 look = sp.getLookAngle();

                // 1. Sello de Ignición de Shinsoo (Carmesí Rubí Trascendente)
                PacketDistributor.sendToPlayersNear(
                        sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                        new SpawnMagicCirclePayload(
                                sp.getId(), sp.getX(), sp.getY() + 0.05D, sp.getZ(),
                                0.0F, 1.0F, 0.0F,
                                5.0F, 50, 0xDC143C, 0, false
                        )
                );

                sl.sendParticles(ParticleTypes.SONIC_BOOM, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 1, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.FLAME, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 80, 2.0D, 1.5D, 2.0D, 0.2D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.2F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.3F, 1.4F);

                CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 36.0D, 0.80F, 12);

                // 2. Tajo Creciente de Ignición de Shinsoo Trascendente
                float dmg = (float) (32.0D * WeaponLevelManager.getDamageMultiplier(stack));
                SlashWaveEntity thornSlash = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRIMSON_BLOOD, dmg, 4.8F, 1.6F, 0.0F, 28, 10);
                thornSlash.setDeltaMovement(look.scale(2.3D));
                sl.addFreshEntity(thornSlash);

                AABB blast = sp.getBoundingBox().inflate(9.0D, 4.0D, 9.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, blast)) {
                    if (nearby != sp && (nearby instanceof Enemy || nearby != player)) {
                        nearby.hurt(nearby.damageSources().playerAttack(sp), dmg * 0.75F);
                        nearby.knockback(1.5D, sp.getX() - nearby.getX(), sp.getZ() - nearby.getZ());
                        nearby.setRemainingFireTicks(100);
                    }
                }

                WeaponLevelManager.addExp(stack, 35, sp);
            }

            // Buffs
            sp.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0, false, false));
            sp.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, true));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1, false, true));

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.05D);
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.2F);
            
            // Extra Shinsoo piercing pulse
            target.hurt(target.damageSources().magic(), 5.0F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.transcendent_thorn.lore"));
        tooltip.add(Component.translatable("item.awakening_system.transcendent_thorn.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.transcendent_thorn.passive"));
        tooltip.add(Component.translatable("item.awakening_system.transcendent_thorn.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
