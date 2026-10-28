package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Mago del Abismo Cryo de Genshin Impact.
 * Dispara carámbanos gélidos y convoca una tormenta de estalactitas heladas que ralentiza a los objetivos.
 */
public class AbyssMageCryoEntity extends AbstractAbyssMageEntity {

    public AbyssMageCryoEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public MageElement getMageElement() {
        return MageElement.CRYO;
    }

    @Override
    public SimpleParticleType getElementParticle() {
        return ParticleTypes.SNOWFLAKE;
    }

    @Override
    public void performElementalAttack(LivingEntity target) {
        if (this.level().isClientSide) return;

        if (this.random.nextFloat() < 0.60F) {
            Snowball snowball = new Snowball(this.level(), this);
            snowball.setPos(this.getX(), this.getY() + 1.2D, this.getZ());
            Vec3 dir = target.getEyePosition().subtract(this.getEyePosition());
            snowball.shoot(dir.x, dir.y + 0.1D, dir.z, 1.4F, 2.0F);
            this.level().addFreshEntity(snowball);

            target.hurt(this.damageSources().mobAttack(this), 6.0F);
            target.setTicksFrozen(140);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.HOSTILE, 1.6F, 0.7F);
        } else {
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.8F, 1.5F);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_HURT_FREEZE, SoundSource.HOSTILE, 1.5F, 0.8F);

                sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 2.5D, target.getZ(), 50, 1.0D, 1.0D, 1.0D, 0.1D);
                sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, target.getX(), target.getY() + 3.0D, target.getZ(), 30, 0.8D, 1.2D, 0.8D, 0.05D);

                target.hurt(this.damageSources().mobAttack(this), 9.0F);
                target.setTicksFrozen(200);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            }
        }
    }
}
