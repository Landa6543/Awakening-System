package com.soybe.awakeningsystem.effect;

import com.soybe.awakeningsystem.particle.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Efecto de Poción: Eco Abisal (Percepción del Vacío).
 * Cada segundo emite una onda sónica de ecolocalización que revela la silueta
 * resplandeciente de todas las criaturas a través de las paredes en la oscuridad subterránea.
 */
public class AbyssalEchoEffect extends MobEffect {

    public AbyssalEchoEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
            double radius = 24.0D + (amplifier * 8.0D);
            AABB area = entity.getBoundingBox().inflate(radius);

            List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                    target -> target != entity && target.isAlive() && !target.isSpectator());

            boolean detectedHostiles = false;

            for (LivingEntity target : nearby) {
                // Aplica Glowing por 2 segundos (se renueva continuamente mientras dure el efecto)
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
                
                // Si es un enemigo hostil, emite destellos de maná sónico sobre su cabeza
                if (target instanceof Enemy) {
                    detectedHostiles = true;
                    if (serverLevel.random.nextFloat() < 0.6F) {
                        serverLevel.sendParticles(ModParticles.MANA_AURA.get(),
                                target.getX(), target.getEyeY() + 0.35D, target.getZ(),
                                2, 0.15D, 0.15D, 0.15D, 0.02D);
                    }
                }
            }

            // Pulso acústico radial de maná sónico expandiéndose hacia afuera
            int points = 12;
            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI * i) / points;
                double vx = Math.cos(angle) * 0.18D;
                double vz = Math.sin(angle) * 0.18D;
                serverLevel.sendParticles(ModParticles.MANA_AURA.get(),
                        entity.getX(), entity.getY() + 0.25D, entity.getZ(),
                        1, vx, 0.01D, vz, 0.1D);
            }

            // Sonido de eco sónico armónico
            if (detectedHostiles || (entity.isCrouching() && entity instanceof Player)) {
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.45F, 1.8F);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Se ejecuta una vez cada 20 ticks (exactamente cada 1 segundo)
        return duration % 20 == 0;
    }
}
