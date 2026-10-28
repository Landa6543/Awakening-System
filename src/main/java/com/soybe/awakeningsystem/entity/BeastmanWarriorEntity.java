package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Guerrero Hombre Bestia (Beastman Warrior) del Reino Isekai:
 * Feroz luchador cuerpo a cuerpo con rugido de batalla y salto demoledor.
 */
public class BeastmanWarriorEntity extends Monster {

    private int roarCooldown = 60;
    private int leapCooldown = 40;

    public BeastmanWarriorEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 65;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 130.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            if (roarCooldown > 0) roarCooldown--;
            if (leapCooldown > 0) leapCooldown--;

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distSq = this.distanceToSqr(target);

                if (roarCooldown <= 0 && distSq < 225.0D) {
                    performWarRoar();
                    roarCooldown = 220;
                }

                if (leapCooldown <= 0 && distSq >= 25.0D && distSq <= 196.0D && this.onGround()) {
                    Vec3 dir = target.position().subtract(this.position()).normalize();
                    this.setDeltaMovement(dir.x * 1.2D, 0.45D, dir.z * 1.2D);
                    this.playSound(SoundEvents.RAVAGER_ATTACK, 1.2F, 1.3F);
                    leapCooldown = 120;
                }
            }
        }
    }

    private void performWarRoar() {
        this.playSound(com.soybe.awakeningsystem.ModSounds.BEASTMAN_ATTACK.get(), 1.5F, 0.9F);
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.1F);
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 0));
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 0));

        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1.8D, this.getZ(),
                    15, 0.6D, 0.4D, 0.6D, 0.0D);
            sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 1.5D, this.getZ(),
                    20, 0.5D, 0.3D, 0.5D, 0.05D);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            this.playSound(com.soybe.awakeningsystem.ModSounds.BEASTMAN_ATTACK.get(), 0.9F, 1.2F);
            this.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.2F, 0.8F);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return com.soybe.awakeningsystem.ModSounds.BEASTMAN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return com.soybe.awakeningsystem.ModSounds.BEASTMAN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return com.soybe.awakeningsystem.ModSounds.BEASTMAN_DEATH.get();
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        if (SystemItems.BEASTMAN_FUR != null && SystemItems.BEASTMAN_FUR.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.BEASTMAN_FUR.get(), 1 + this.random.nextInt(3)));
        }
        if (SystemItems.BEASTMAN_FANG != null && SystemItems.BEASTMAN_FANG.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.BEASTMAN_FANG.get(), 1 + this.random.nextInt(2)));
        }
        this.spawnAtLocation(new ItemStack(Items.BEEF, 2 + this.random.nextInt(3)));
        this.spawnAtLocation(SystemItems.coin(100 + this.random.nextInt(60)));

        if (this.random.nextFloat() < 0.25F) {
            this.spawnAtLocation(new ItemStack(Items.IRON_AXE, 1));
        }
    }
}
