package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.CameraShakePayload;
import com.soybe.awakeningsystem.SpawnMagicCirclePayload;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class PrimogemSwordItem extends SwordItem implements AwakeningWeaponItem {

    public PrimogemSwordItem() {
        super(CustomToolTiers.PRIMOGEM, new Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .attributes(SwordItem.createAttributes(CustomToolTiers.PRIMOGEM, 6.5F, -2.2F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("primogem_sword_skill_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatableWithFallback("item.awakening_system.primogem_sword.cd", "§d✦ Destiny's Wish on cooldown: §f%ss", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("primogem_sword_skill_cd", gameTime + 280L);
            sp.getCooldowns().addCooldown(this, 280);

            if (level instanceof ServerLevel sl) {
                Vec3 look = sp.getLookAngle();

                PacketDistributor.sendToPlayersNear(
                        sl, null, sp.getX(), sp.getY(), sp.getZ(), 64.0D,
                        new SpawnMagicCirclePayload(
                                sp.getId(), sp.getX(), sp.getY() + 0.05D, sp.getZ(),
                                0.0F, 1.0F, 0.0F,
                                4.5F, 45, 0x9370DB, 0, false
                        )
                );

                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.6F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.3F, 1.4F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.7F);

                CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 28.0D, 0.55F, 8);

                float dmg = (float) (24.0D * WeaponLevelManager.getDamageMultiplier(stack));
                SlashWaveEntity starSlash = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_COSMIC_AMETHYST, dmg, 4.2F, 1.5F, 0.0F, 26, 8);
                starSlash.setDeltaMovement(look.scale(2.2D));
                sl.addFreshEntity(starSlash);

                sp.heal(5.0F);
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 1, false, true));
                sp.addEffect(new MobEffectInstance(MobEffects.LUCK, 200, 1, false, true));

                WeaponLevelManager.addExp(stack, 30, sp);
            }

            sp.displayClientMessage(Component.translatableWithFallback("item.awakening_system.primogem_sword.msg", "§5§l✦ [PRIMOGEM SWORD] §dAstral Wish of Destiny unleashed! (+Health restored)"), true);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result && !attacker.level().isClientSide() && attacker.level() instanceof ServerLevel serverLevel) {
            boolean isCrit = attacker.fallDistance > 0.0F && !attacker.onGround() && !attacker.onClimbable()
                    && !attacker.isInWater() && !attacker.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
                    && !attacker.isPassenger();

            if (isCrit) {
                float extraDamage = 5.0F;
                target.hurt(attacker.damageSources().magic(), extraDamage);
                float healAmount = Math.max(1.0F, extraDamage * 0.4F);
                attacker.heal(healAmount);

                serverLevel.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.4, 0.4, 0.1);
                serverLevel.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 0.8, target.getZ(), 15, 0.3, 0.3, 0.3, 0.2);
                attacker.level().playSound(null, target.blockPosition(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 1.2F, 1.6F);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.primogem_sword.badge").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.primogem_sword.desc"));
        tooltip.add(Component.translatable("item.awakening_system.primogem_sword.active"));
        tooltip.add(Component.translatable("item.awakening_system.primogem_sword.passive"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
