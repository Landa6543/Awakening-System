package com.soybe.awakeningsystem.combat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Handles server-side validation and multi-target damage application
 * for the Awakening Combat Engine (ACE).
 */
public class AceServerCombatHandler {

    public static void handleAttackPacket(ServerPlayer player, AceAttackPacket packet) {
        if (player == null || !player.isAlive() || player.isSpectator()) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        double weaponRange = AceTargetFinder.getWeaponRange(player, weapon);
        double maxAllowedRange = weaponRange + 1.8; // Generous buffer for network ping / latency
        double maxAllowedRangeSq = maxAllowedRange * maxAllowedRange;

        List<Integer> targetIds = packet.targetIds();
        if (targetIds == null || targetIds.isEmpty()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 lookVec = player.getViewVector(1.0F);

        boolean isUnarmed = weapon.isEmpty();

        // 1. Process Primary Target (Full player attack pipeline or Martial Arts)
        int primaryId = targetIds.get(0);
        Entity primaryEntity = level.getEntity(primaryId);

        if (primaryEntity instanceof LivingEntity primaryLiving && primaryLiving.isAlive()) {
            if (player.distanceToSqr(primaryLiving) <= maxAllowedRangeSq) {
                if (isUnarmed) {
                    float unarmedDmg = AceMartialArts.calculateUnarmedDamage(player, packet.comboStep());
                    primaryLiving.hurt(player.damageSources().playerAttack(player), unarmedDmg);
                    AceMartialArts.applyUnarmedImpactEffects(player, primaryLiving, packet.comboStep());
                } else {
                    // Execute standard attack (triggers enchantments, stats, and system damage hooks)
                    player.attack(primaryLiving);

                    if (packet.comboStep() == 2) {
                        // Finisher impact stagger
                        Vec3 pushDir = primaryLiving.position().subtract(player.position()).normalize();
                        primaryLiving.knockback(0.40, -pushDir.x, -pushDir.z);
                    }
                }
            }
        }

        // 2. Process Secondary Targets (Sweeping Arc Damage)
        float sweepDamage;
        if (isUnarmed) {
            float unarmedBase = AceMartialArts.calculateUnarmedDamage(player, packet.comboStep());
            sweepDamage = (float) Math.max(1.0, unarmedBase * 0.55);
        } else {
            double baseAttackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            sweepDamage = (float) Math.max(1.0, baseAttackDamage * 0.70);
        }

        for (int i = 1; i < targetIds.size(); i++) {
            int secondaryId = targetIds.get(i);
            Entity secEntity = level.getEntity(secondaryId);

            if (secEntity instanceof LivingEntity secLiving && secLiving.isAlive() && secLiving != primaryEntity) {
                if (player.distanceToSqr(secLiving) <= maxAllowedRangeSq) {
                    secLiving.hurt(player.damageSources().playerAttack(player), sweepDamage);

                    // Gentle directional sweep knockback
                    Vec3 pushDir = secLiving.position().subtract(player.position()).normalize();
                    secLiving.knockback(isUnarmed ? 0.25 : 0.35, -pushDir.x, -pushDir.z);

                    level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                            secLiving.getX(), secLiving.getY() + secLiving.getBbHeight() * 0.5, secLiving.getZ(),
                            5, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }

        // Store combo step for third-person model animations
        player.getPersistentData().putInt("ace_combo_step", packet.comboStep());

        // 3. Broadcast Directional Slash Trail VFX to other players in multiplayer
        AceSlashVfx.broadcastServerSlashTrail(player, packet.comboStep(), weapon);

        // Finisher audio feedback for weapons (unarmed plays its own martial SFX)
        if (!isUnarmed && packet.comboStep() == 2) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.1F);
        }
    }
}
