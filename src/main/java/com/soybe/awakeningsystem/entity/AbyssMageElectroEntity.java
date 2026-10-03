package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Mago del Abismo Electro de Genshin Impact.
 * Dispara descargas eléctricas arcanas e invoca un anillo giratorio de orbes de relámpago.
 */
public class AbyssMageElectroEntity extends AbstractAbyssMageEntity {

    public AbyssMageElectroEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public MageElement getMageElement() {
        return MageElement.ELECTRO;
    }

    @Override
    public SimpleParticleType getElementParticle() {
        return ParticleTypes.ELECTRIC_SPARK;
    }

    @Override
    public void performElementalAttack(LivingEntity target) {
        if (this.level().isClientSide) return;

        if (this.random.nextFloat() < 0.60F) {
            // Descarga de rayo arcana
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 0.7F, 1.8F);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 35, 0.4D, 0.6D, 0.4D, 0.15D);
                target.hurt(this.damageSources().mobAttack(this), 7.0F);
            }
        } else {
            // Anillo de orbes electrificados alrededor del objetivo
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE, 1.4F, 1.4F);

                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2.0 / 8.0) * i;
                    double ox = Math.cos(angle) * 2.5D;
                    double oz = Math.sin(angle) * 2.5D;
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX() + ox, target.getY() + 0.8D, target.getZ() + oz, 10, 0.2D, 0.2D, 0.2D, 0.05D);
                    sl.sendParticles(ParticleTypes.FIREWORK, target.getX() + ox, target.getY() + 0.8D, target.getZ() + oz, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                }

                target.hurt(this.damageSources().mobAttack(this), 9.0F);
                Vec3 push = target.position().subtract(this.position()).normalize().scale(0.6D);
                target.setDeltaMovement(push.x, 0.25D, push.z);
            }
        }
    }
}
