package com.soybe.awakeningsystem.entity.hypostasis;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AnemoRevivalOrbEntity extends Monster {

    public AnemoRevivalOrbEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0, 0, 0);

        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.CLOUD,
                    this.getX() + (random.nextDouble() - 0.5D) * 0.8D,
                    this.getY() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 0.8D,
                    0, 0.02D, 0);
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    this.getX() + (random.nextDouble() - 0.5D) * 0.6D,
                    this.getY() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 0.6D,
                    0, 0.05D, 0);
        } else {
            for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(0.4D))) {
                destroyOrb();
                break;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide) {
            destroyOrb();
        }
        return true;
    }

    private void destroyOrb() {
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.6F);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.3F, 1.2F);
            sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.5D, this.getZ(), 25, 0.4D, 0.4D, 0.4D, 0.1D);
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 0.5D, this.getZ(), 2, 0.1D, 0.1D, 0.1D, 0.0D);
        }
        this.discard();
    }
}
