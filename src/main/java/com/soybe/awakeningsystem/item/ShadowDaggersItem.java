package com.soybe.awakeningsystem.item;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;

import java.util.List;

public class ShadowDaggersItem extends SwordItem implements AwakeningWeaponItem {
    public ShadowDaggersItem(Properties properties) {
        super(Tiers.NETHERITE, properties
                .rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 8, -1.4F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("shadow_daggers_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.literal("§c⏳ Dagas de Sangre y Sombras en recarga (" + sec + "s)..."), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("shadow_daggers_cd", gameTime + 240L); // 12s cooldown
            sp.getCooldowns().addCooldown(this, 240);

            if (level instanceof ServerLevel sl) {
                Vec3 eyePos = sp.getEyePosition();
                Vec3 lookVec = sp.getLookAngle();
                Vec3 reachVec = eyePos.add(lookVec.scale(16.0D));

                LivingEntity target = null;
                double closestDist = Double.MAX_VALUE;
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, sp.getBoundingBox().expandTowards(lookVec.scale(16.0D)).inflate(2.0D))) {
                    if (entity == sp || !entity.isAlive() || entity.getTags().contains("shadow_soldier") || entity.getUUID().equals(sp.getUUID())) continue;
                    AABB aabb = entity.getBoundingBox().inflate(0.5D);
                    var hit = aabb.clip(eyePos, reachVec);
                    if (hit.isPresent()) {
                        double dist = eyePos.distanceToSqr(hit.get());
                        if (dist < closestDist) {
                            closestDist = dist;
                            target = entity;
                        }
                    }
                }

                if (target != null) {
                    // --- PASO DE SOMBRAS: APARICIÓN TRAS EL OBJETIVO ---
                    sl.sendParticles(ParticleTypes.SQUID_INK, sp.getX(), sp.getY() + 0.8D, sp.getZ(), 35, 0.4D, 0.6D, 0.4D, 0.05D);
                    sl.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 0.8D, sp.getZ(), 20, 0.3D, 0.5D, 0.3D, 0.1D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);

                    // Posición detrás del enemigo (1.5 bloques a su espalda)
                    Vec3 targetLook = target.getLookAngle().normalize();
                    double destX = target.getX() - targetLook.x * 1.5D;
                    double destZ = target.getZ() - targetLook.z * 1.5D;
                    double destY = target.getY();

                    sp.teleportTo(sl, destX, destY, destZ, target.getYRot(), target.getXRot());

                    // Estallido de sombra al reaparecer
                    sl.sendParticles(ParticleTypes.SQUID_INK, destX, destY + 0.8D, destZ, 45, 0.5D, 0.6D, 0.5D, 0.05D);
                    sl.playSound(null, destX, destY, destZ, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.6F, 1.4F);
                    sl.playSound(null, destX, destY, destZ, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.65F, 1.8F);

                    com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, destX, destY, destZ, 28.0D, 0.70F, 10);

                    // Puñalada por la espalda y Cruz de Sombras
                    float backstabDmg = (float) (28.0D * WeaponLevelManager.getDamageMultiplier(stack));
                    if (target.getHealth() < target.getMaxHealth() * 0.5F) {
                        backstabDmg += 14.0F; // Golpe de ejecución letal
                    }

                    // Tajo Cruzado 'X' de Vacío a la Espalda
                    SlashWaveEntity crossSlice = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, backstabDmg, 3.2F, 3.2F, 0.0F, 16, 4);
                    crossSlice.setCross(true);
                    crossSlice.setDeltaMovement(targetLook.scale(1.8D));
                    sl.addFreshEntity(crossSlice);

                    target.hurt(target.damageSources().playerAttack(sp), backstabDmg);
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));

                    sp.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, true));
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 2, false, true));

                    WeaponLevelManager.addExp(stack, 35, sp);

                    String targetName = target.getCustomName() != null ? target.getCustomName().getString() : target.getType().getDescription().getString();
                    sp.displayClientMessage(Component.literal("§8§l✦ [DEGÜELLO DE SOMBRAS] §5¡Degüello en Cruz ejecutado tras §f" + targetName + " §5(§c" + (int)backstabDmg + " daño§5)!"), true);
                } else {
                    // --- PARPADEO SOMBRÍO (SHADOW DASH) SI NO HAY OBJETIVO ---
                    Vec3 dashDest = eyePos.add(lookVec.scale(8.0D));
                    BlockPos targetPos = BlockPos.containing(dashDest);
                    if (level.getBlockState(targetPos).isAir() || level.getBlockState(targetPos).canBeReplaced()) {
                        sp.teleportTo(sl, dashDest.x, dashDest.y - 0.5D, dashDest.z, sp.getYRot(), sp.getXRot());
                    } else {
                        sp.setDeltaMovement(lookVec.x * 1.6D, 0.25D, lookVec.z * 1.6D);
                        sp.hurtMarked = true;
                    }

                    // Disparar onda cortante de sombra frontal en el parpadeo
                    SlashWaveEntity dashWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_DARK_VOID, 20.0F, 3.4F, 1.2F, 0.0F, 22, 6);
                    dashWave.setDeltaMovement(lookVec.scale(2.2D));
                    sl.addFreshEntity(dashWave);

                    com.soybe.awakeningsystem.CameraShakePayload.sendToNearby(sl, sp.getX(), sp.getY(), sp.getZ(), 20.0D, 0.4F, 6);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 2, false, true));
                    sp.displayClientMessage(Component.literal("§8§l✦ [PASO DE SOMBRAS] §7¡Parpadeo sombrío ejecutado con cuchilla de vacío!"), true);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 1.0D, target.getZ(), 18, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.3D, 0.2D, 0.02D);
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.1D);
            
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));

            // Backstab / execute bonus
            if (target.getHealth() < target.getMaxHealth() * 0.5F && attacker instanceof Player p) {
                target.hurt(target.damageSources().playerAttack(p), 8.0F);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.3F, 1.6F);
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY() + 0.8D, target.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.03D);
            }
        }
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.shadow_daggers.lore"));
        tooltip.add(Component.translatable("item.awakening_system.shadow_daggers.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.shadow_daggers.passive"));
        tooltip.add(Component.translatable("item.awakening_system.shadow_daggers.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
