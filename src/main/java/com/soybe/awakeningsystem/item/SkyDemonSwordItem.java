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
                sp.displayClientMessage(Component.literal("§c⏳ Espada del Demonio Celestial en recarga (" + sec + "s)..."), true);
                return InteractionResultHolder.fail(stack);
            }

            sp.getPersistentData().putLong("sky_demon_cd", gameTime + 240L); // 12s cooldown
            sp.getCooldowns().addCooldown(this, 240);

            Vec3 look = sp.getLookAngle();
            Vec3 pos = sp.getEyePosition();

            if (level instanceof ServerLevel sl) {
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5F, 1.2F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.5F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 1.4F);

                // Onda de choque masiva de 14 bloques: Qi Carmesí y Oscuro de Demonio Celestial
                for (double d = 1.0D; d <= 14.0D; d += 0.5D) {
                    Vec3 beamPos = pos.add(look.scale(d));
                    if (d % 2.0 == 0) {
                        sl.sendParticles(ParticleTypes.SONIC_BOOM, beamPos.x, beamPos.y, beamPos.z, 1, 0, 0, 0, 0);
                    }
                    sl.sendParticles(ParticleTypes.SQUID_INK, beamPos.x, beamPos.y, beamPos.z, 4, 0.15D, 0.15D, 0.15D, 0.02D);
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, beamPos.x, beamPos.y, beamPos.z, 6, 0.2D, 0.2D, 0.2D, 0.05D);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, beamPos.x, beamPos.y, beamPos.z, 5, 0.15D, 0.15D, 0.15D, 0.05D);
                    sl.sendParticles(ParticleTypes.SCULK_SOUL, beamPos.x, beamPos.y, beamPos.z, 2, 0.1D, 0.1D, 0.1D, 0.01D);
                }

                AABB hitBox = sp.getBoundingBox().expandTowards(look.scale(14.0D)).inflate(2.5D, 2.0D, 2.5D);
                int hitCount = 0;
                for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, hitBox)) {
                    if (nearby != sp && (nearby instanceof Enemy || (nearby.isAlive() && !nearby.getTags().contains("shadow_soldier")))) {
                        Vec3 toEntity = nearby.getEyePosition().subtract(pos).normalize();
                        if (look.dot(toEntity) > 0.35D || nearby.distanceToSqr(sp) <= 9.0D) {
                            // Levantamiento aéreo violento (Knockup)
                            Vec3 currVel = nearby.getDeltaMovement();
                            nearby.setDeltaMovement(currVel.x, 0.62D, currVel.z);
                            nearby.hurtMarked = true;

                            // Cálculo balístico de Nano: Perforación de blindaje del 40%
                            float armorBonus = Math.min(12.0F, nearby.getArmorValue() * 0.4F);
                            float totalDmg = 26.0F + armorBonus;
                            nearby.hurt(nearby.damageSources().playerAttack(sp), totalDmg);

                            nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                            nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                            hitCount++;
                        }
                    }
                }

                sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1, false, true));
                sp.displayClientMessage(Component.translatable("item.awakening_system.sky_demon_sword.nano_calc", hitCount), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 25, 0.3D, 0.4D, 0.3D, 0.08D);
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.05D);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0D, target.getZ(), 10, 0.2D, 0.3D, 0.2D, 0.02D);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            if (attacker instanceof Player p) {
                target.hurt(target.damageSources().playerAttack(p), 4.0F); // Daño penetrante extra
            }
        }
        attacker.heal(2.0F); // Protocolo de bioreparación de nanites
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
