package com.soybe.awakeningsystem.entity.hypostasis;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class ElectroRevivalPrismEntity extends Monster {

    public ElectroRevivalPrismEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0, 0, 0);

        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + (random.nextDouble() - 0.5D) * 0.8D,
                    this.getY() + 0.6D + (random.nextDouble() - 0.5D) * 0.8D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 0.8D,
                    0, 0.05D, 0);
            this.level().addParticle(ParticleTypes.PORTAL,
                    this.getX() + (random.nextDouble() - 0.5D) * 0.6D,
                    this.getY() + 0.6D + (random.nextDouble() - 0.5D) * 0.6D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 0.6D,
                    0, 0.02D, 0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.4F, 1.5F);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.6D, this.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.1D);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 1.2F);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.8F, 1.8F);
            sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 0.6D, this.getZ(), 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.6D, this.getZ(), 35, 0.5D, 0.5D, 0.5D, 0.15D);
        }
    }
}
