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
 * Mago del Abismo Hydro de Genshin Impact.
 * Dispara burbujas aprisionadoras que inmovilizan a los rivales y olas de agua a presión.
 */
public class AbyssMageHydroEntity extends AbstractAbyssMageEntity {

    public AbyssMageHydroEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public MageElement getMageElement() {
        return MageElement.HYDRO;
    }

    @Override
    public SimpleParticleType getElementParticle() {
        return ParticleTypes.SPLASH;
    }

    @Override
    public void performElementalAttack(LivingEntity target) {
        if (this.level().isClientSide) return;

        if (this.random.nextFloat() < 0.60F) {
            AbyssBubbleEntity bubble = new AbyssBubbleEntity(this.level(), this, target);
            bubble.setPos(this.getX(), this.getY() + 1.2D, this.getZ());
            Vec3 dir = target.getEyePosition().subtract(this.getEyePosition()).normalize().scale(0.35D);
            bubble.setDeltaMovement(dir);
            this.level().addFreshEntity(bubble);

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.HOSTILE, 1.8F, 1.2F);
        } else {
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_SPLASH, SoundSource.HOSTILE, 1.8F, 0.9F);
                sl.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.2D);
                target.hurt(this.damageSources().mobAttack(this), 8.0F);

                Vec3 push = target.position().subtract(this.position()).normalize().scale(0.8D);
                target.setDeltaMovement(push.x, 0.35D, push.z);
            }
        }
    }
}
