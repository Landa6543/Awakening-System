package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Mago del Abismo Pyro de Genshin Impact.
 * Dispara bolas de fuego arcanas y canaliza un ritual de pilares llameantes.
 */
public class AbyssMagePyroEntity extends AbstractAbyssMageEntity {

    public AbyssMagePyroEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public MageElement getMageElement() {
        return MageElement.PYRO;
    }

    @Override
    public SimpleParticleType getElementParticle() {
        return ParticleTypes.FLAME;
    }

    @Override
    public void performElementalAttack(LivingEntity target) {
        if (this.level().isClientSide) return;

        if (this.random.nextFloat() < 0.65F) {
            Vec3 dir = target.getEyePosition().subtract(this.getEyePosition()).normalize();
            SmallFireball fireball = new SmallFireball(this.level(), this, dir);
            fireball.setPos(this.getX(), this.getY() + 1.2D, this.getZ());
            this.level().addFreshEntity(fireball);

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.5F, 1.2F);
        } else {
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.8F, 0.9F);
                for (int i = 0; i < 3; i++) {
                    double angle = (Math.PI * 2.0 / 3.0) * i + (random.nextDouble() * 0.5);
                    double ox = Math.cos(angle) * 2.0D;
                    double oz = Math.sin(angle) * 2.0D;
                    sl.sendParticles(ParticleTypes.FLAME, target.getX() + ox, target.getY(), target.getZ() + oz, 20, 0.3D, 1.2D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.LAVA, target.getX() + ox, target.getY() + 0.5D, target.getZ() + oz, 5, 0.2D, 0.5D, 0.2D, 0.0D);
                }
                target.hurt(this.damageSources().mobAttack(this), 7.0F);
                target.igniteForSeconds(4);
            }
        }
    }
}
