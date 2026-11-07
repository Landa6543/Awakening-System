package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.CameraShakePayload;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
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

/**
 * Kasaka's Venom Fang (Colmillo Envenenado de Kasaka).
 * Daga icónica obtenida tras derrotar a Kasaka de Colmillos Venenosos Azules en Solo Leveling.
 * Equipada con veneno de neurotoxina, parálisis motora y tajo punzante de alta velocidad.
 */
public class KasakasVenomFangItem extends SwordItem implements AwakeningWeaponItem {

    public KasakasVenomFangItem(Properties properties) {
        super(Tiers.DIAMOND, properties
                .rarity(Rarity.RARE)
                .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 4, -1.2F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            long gameTime = level.getGameTime();
            long cdUntil = sp.getPersistentData().getLong("kasakas_venom_cd");
            if (gameTime < cdUntil) {
                long sec = (cdUntil - gameTime) / 20;
                sp.displayClientMessage(Component.translatable("item.awakening_system.kasakas_venom_fang.cooldown", sec), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("kasakas_venom_cd", gameTime + 200L);
            sp.getCooldowns().addCooldown(this, 200);

            if (level instanceof ServerLevel sl) {
                Vec3 eyePos = sp.getEyePosition();
                Vec3 lookVec = sp.getLookAngle();
                Vec3 reachVec = eyePos.add(lookVec.scale(12.0D));

                LivingEntity target = null;
                double closestDist = Double.MAX_VALUE;
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, sp.getBoundingBox().expandTowards(lookVec.scale(12.0D)).inflate(2.0D))) {
                    if (entity == sp || !entity.isAlive() || entity.getTags().contains("shadow_soldier") || entity.getUUID().equals(sp.getUUID())) continue;
                    AABB aabb = entity.getBoundingBox().inflate(0.6D);
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
                    sl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, sp.getX(), sp.getY() + 0.8D, sp.getZ(), 25, 0.4D, 0.5D, 0.4D, 0.05D);
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 0.8D, sp.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.1D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.PLAYERS, 1.2F, 1.4F);

                    Vec3 targetLook = target.getLookAngle().normalize();
                    double destX = target.getX() - targetLook.x * 1.2D;
                    double destZ = target.getZ() - targetLook.z * 1.2D;
                    double destY = target.getY();

                    sp.teleportTo(sl, destX, destY, destZ, sp.getYRot(), sp.getXRot());

                    sl.sendParticles(ParticleTypes.COMPOSTER, destX, destY + 0.9D, destZ, 30, 0.4D, 0.5D, 0.4D, 0.08D);
                    sl.sendParticles(ParticleTypes.DRAGON_BREATH, destX, destY + 0.9D, destZ, 20, 0.3D, 0.4D, 0.3D, 0.03D);
                    sl.playSound(null, destX, destY, destZ, SoundEvents.SPIDER_HURT, SoundSource.PLAYERS, 1.5F, 1.2F);
                    sl.playSound(null, destX, destY, destZ, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.4F, 1.3F);

                    CameraShakePayload.sendToNearby(sl, destX, destY, destZ, 20.0D, 0.5F, 8);

                    float strikeDmg = (float) (22.0D * WeaponLevelManager.getDamageMultiplier(stack));
                    SlashWaveEntity aquaThrust = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, strikeDmg, 2.8F, 1.2F, 0.0F, 16, 3);
                    aquaThrust.setSlashStyle(SlashWaveEntity.STYLE_THRUST);
                    aquaThrust.setDeltaMovement(targetLook.scale(1.6D));
                    sl.addFreshEntity(aquaThrust);

                    target.hurt(target.damageSources().playerAttack(sp), strikeDmg);
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));

                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 1, false, true));
                    sp.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, false, false));

                    WeaponLevelManager.addExp(stack, 25, sp);

                    String targetName = target.getCustomName() != null ? target.getCustomName().getString() : target.getType().getDescription().getString();
                    sp.displayClientMessage(Component.translatable("item.awakening_system.kasakas_venom_fang.strike", targetName, (int)strikeDmg), true);
                } else {
                    Vec3 dashDest = eyePos.add(lookVec.scale(8.0D));
                    BlockPos targetPos = BlockPos.containing(dashDest);
                    if (level.getBlockState(targetPos).isAir() || level.getBlockState(targetPos).canBeReplaced()) {
                        sp.teleportTo(sl, dashDest.x, dashDest.y - 0.5D, dashDest.z, sp.getYRot(), sp.getXRot());
                    } else {
                        sp.setDeltaMovement(lookVec.x * 1.5D, 0.2D, lookVec.z * 1.5D);
                        sp.hurtMarked = true;
                    }

                    SlashWaveEntity aquaWave = new SlashWaveEntity(sl, sp, SlashWaveEntity.TYPE_CRYSTAL_AQUA, 16.0F, 2.5F, 1.0F, 0.0F, 20, 4);
                    aquaWave.setSlashStyle(SlashWaveEntity.STYLE_THRUST);
                    aquaWave.setDeltaMovement(lookVec.scale(2.0D));
                    sl.addFreshEntity(aquaWave);

                    sl.sendParticles(ParticleTypes.COMPOSTER, sp.getX(), sp.getY() + 0.8D, sp.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.05D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.PLAYERS, 1.0F, 1.5F);
                    sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, true));
                    sp.displayClientMessage(Component.translatable("item.awakening_system.kasakas_venom_fang.rush"), true);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            // Passive 1: Kasaka's Venom (35% chance)
            if (attacker.getRandom().nextFloat() < 0.35F) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                sl.sendParticles(ParticleTypes.COMPOSTER, target.getX(), target.getY() + 0.8D, target.getZ(), 12, 0.25D, 0.3D, 0.25D, 0.04D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SPIDER_HURT, SoundSource.PLAYERS, 0.8F, 1.6F);
            }

            // Passive 2: Nerve Paralysis on targets already poisoned or low health
            if (target.hasEffect(MobEffects.POISON) || target.getHealth() < target.getMaxHealth() * 0.4F) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 3));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 0.9D, target.getZ(), 8, 0.2D, 0.25D, 0.2D, 0.02D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.7F, 1.4F);
            }

            // Passive 3: Dagger Backstab mechanics
            Vec3 view = target.getViewVector(1.0F);
            Vec3 toAttacker = attacker.position().subtract(target.position()).normalize();
            if (view.dot(toAttacker) < -0.25D) {
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.25D, 0.25D, 0.25D, 0.15D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.1F, 1.4F);
                if (attacker instanceof Player player) {
                    float bonus = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.45F;
                    target.hurt(attacker.damageSources().playerAttack(player), bonus);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.kasakas_venom_fang.rank").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.kasakas_venom_fang.lore").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.kasakas_venom_fang.origin").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.kasakas_venom_fang.passive").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.kasakas_venom_fang.active").withStyle(ChatFormatting.GREEN));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
