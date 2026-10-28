package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Mímico Hidromimético de Rhodeia (Oceánida de Liyue).
 * Ser hecho de agua pura que adopta la forma de fauna silvestre agresiva.
 * Al ser derrotado explota en un estallido de agua pura.
 */
public class WaterMimicEntity extends Monster {

    public WaterMimicEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 65.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.random.nextFloat() < 0.4F) {
                this.level().addParticle(ParticleTypes.FALLING_WATER,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getY() + this.random.nextDouble() * 1.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        0, -0.05D, 0);
                this.level().addParticle(ParticleTypes.SPLASH,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getY() + 0.2D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        0, 0.1D, 0);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FREEZING)) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            amount *= 1.8F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 1.8F, 1.0F);
            sl.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.5D, this.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.15D);
            sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.5D, this.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
        }
    }
}
