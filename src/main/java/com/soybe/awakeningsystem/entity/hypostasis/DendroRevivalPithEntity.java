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

/**
 * Núcleo Restaurador Tri-Lakshana de Dendro Hypostasis ("Zayin").
 * Aparece durante la fase de crisis de Zayin y debe ser purificado.
 */
public class DendroRevivalPithEntity extends Monster {

    public DendroRevivalPithEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 35.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0, 0, 0);

        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    this.getX() + (random.nextDouble() - 0.5D) * 0.8D,
                    this.getY() + 0.6D + (random.nextDouble() - 0.5D) * 0.8D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 0.8D,
                    0, 0.05D, 0);
            this.level().addParticle(ParticleTypes.COMPOSTER,
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
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.4F, 1.2F);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.6D, this.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.1D);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.6F, 1.8F);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 0.6D, this.getZ(), 20, 0.3D, 0.3D, 0.3D, 0.15D);
        }
    }
}
