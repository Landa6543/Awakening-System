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
                sp.displayClientMessage(Component.literal("§c⏳ Espada de Luz en recarga (" + sec + "s)..."), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("radiant_light_cd", gameTime + 200L); // 10s cooldown
            sp.getCooldowns().addCooldown(this, 200);

            Vec3 look = sp.getLookAngle();
            sp.setDeltaMovement(look.x * 1.8D, 0.22D, look.z * 1.8D);
            sp.hurtMarked = true;

            if (level instanceof ServerLevel sl) {
                // Haz de luz radiante deslumbrante en la trayectoria
                Vec3 eyePos = sp.getEyePosition();
                for (double d = 0; d <= 10.0D; d += 0.5D) {
                    Vec3 beam = eyePos.add(look.scale(d));
                    sl.sendParticles(ParticleTypes.END_ROD, beam.x, beam.y, beam.z, 3, 0.12D, 0.12D, 0.12D, 0.02D);
                    sl.sendParticles(ParticleTypes.GLOW, beam.x, beam.y, beam.z, 2, 0.1D, 0.1D, 0.1D, 0.01D);
                    if (d % 2.0 == 0) {
                        sl.sendParticles(ParticleTypes.FLASH, beam.x, beam.y, beam.z, 1, 0, 0, 0, 0);
                    }
                }

                // Paisaje sonoro sacro y cortante
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.5F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.2F, 1.3F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.3F, 1.4F);

                AABB box = sp.getBoundingBox().inflate(6.0D, 2.5D, 6.0D);
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (nearby != sp && (nearby instanceof Enemy || (nearby.isAlive() && !nearby.getTags().contains("shadow_soldier")))) {
                        float dmg = 22.0F;
                        // +75% daño sagrado letal contra no-muertos
                        if (nearby.isInvertedHealAndHarm()) {
                            dmg = 38.5F;
                            nearby.igniteForSeconds(5);
                            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, nearby.getX(), nearby.getY() + 1.0D, nearby.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.05D);
                        }
                        nearby.hurt(nearby.damageSources().playerAttack(sp), dmg);
                        nearby.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
                        nearby.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                        nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                    }
                }

                // Purificación absoluta de estados negativos
                sp.removeEffect(MobEffects.POISON);
                sp.removeEffect(MobEffects.WITHER);
                sp.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                sp.removeEffect(MobEffects.WEAKNESS);
                sp.removeEffect(MobEffects.BLINDNESS);
                sp.removeEffect(MobEffects.DARKNESS);
                sp.removeEffect(MobEffects.CONFUSION);

                // Curación y bendición radiante
                sp.heal(4.0F);
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1, false, true));
                sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, true));
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
            sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.GLOW, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.01D);
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
            if (target.isInvertedHealAndHarm() && attacker instanceof Player p) {
                target.hurt(target.damageSources().playerAttack(p), 8.0F); // Daño sagrado extra a no-muertos
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
