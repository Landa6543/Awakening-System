package com.soybe.awakeningsystem.combat;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Handles barehand and unarmed martial arts combat scaling for the
 * Awakening Combat Engine (ACE), translating hunter stats (Strength, Agility,
 * Dantian Realm, and Meridians) into formidable hand-to-hand combat power.
 */
public class AceMartialArts {

    /**
     * Calculates barehanded attack damage based on Hunter statistics and cultivation.
     */
    public static float calculateUnarmedDamage(ServerPlayer player, int comboStep) {
        SystemData data = SystemEvents.data(player);
        int str = data.strength();
        int agi = data.agility();
        int realm = data.dantianRealm();
        int meridians = data.getOpenedMeridiansCount();

        // Base fist strike damage scaled by hunter stats
        double base = 2.5 + (str * 0.35) + (agi * 0.15) + (realm * 2.0) + (meridians * 0.45);

        // Combo Step Multiplier:
        // Step 0: Quick Jab (1.0x)
        // Step 1: Heavy Cross (1.25x)
        // Step 2: Martial Palm / Demolishing Uppercut (1.65x)
        double comboMult = switch (comboStep) {
            case 1 -> 1.25;
            case 2 -> 1.65;
            default -> 1.0;
        };

        double totalDamage = base * comboMult;

        // Cultivation Qi Infusion: If the hunter has Qi, infuse the strike with pure spirit energy
        if (data.cultivationQi() >= 2) {
            data.consumeCultivationQi(2);
            totalDamage += 4.5 + (realm * 1.5);

            if (player.serverLevel() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        player.getX(), player.getY() + 1.2, player.getZ(),
                        8, 0.3, 0.3, 0.3, 0.1);
            }
        }

        return (float) Math.max(1.0, totalDamage);
    }

    /**
     * Applies visceral physical impact, aerial launch, and particles for unarmed hits.
     */
    public static void applyUnarmedImpactEffects(ServerPlayer player, LivingEntity target, int comboStep) {
        ServerLevel level = player.serverLevel();
        Vec3 pushDir = target.position().subtract(player.position()).normalize();

        switch (comboStep) {
            case 0 -> { // 1. Left Jab: quick disruption
                target.knockback(0.20, -pushDir.x, -pushDir.z);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1.0F, 1.3F);
                level.sendParticles(ParticleTypes.CRIT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                        4, 0.2, 0.2, 0.2, 0.05);
            }
            case 1 -> { // 2. Right Cross: forceful direct strike
                target.knockback(0.45, -pushDir.x, -pushDir.z);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.1F, 1.1F);
                level.sendParticles(ParticleTypes.CRIT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                        8, 0.25, 0.25, 0.25, 0.1);
            }
            case 2 -> { // 3. Finisher Uppercut / Palm Strike: devastating air launch
                target.knockback(0.65, -pushDir.x, -pushDir.z);
                // Vertical launch velocity
                Vec3 vel = target.getDeltaMovement();
                target.setDeltaMovement(vel.x, Math.max(vel.y, 0.42), vel.z);
                target.hurtMarked = true;

                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.2F, 0.9F);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.35F, 1.8F);

                level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        target.getX(), target.getY() + 0.9, target.getZ(),
                        1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        target.getX(), target.getY() + 0.8, target.getZ(),
                        16, 0.35, 0.4, 0.35, 0.15);
            }
        }
    }
}
