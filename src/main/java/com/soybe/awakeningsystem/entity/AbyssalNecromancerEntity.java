package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
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
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AbyssalNecromancerEntity extends Monster {
    public final AnimationState castAnimationState = new AnimationState();
    public final AnimationState staffSwingAnimationState = new AnimationState();
    public final AnimationState summonAnimationState = new AnimationState();
    public int castAnimationTimeout = 0;
    public int staffSwingAnimationTimeout = 0;
    public int summonAnimationTimeout = 0;

    private int summonCooldown = 180;
    private int soulOrbCooldown = 70;
    private int curseCooldown = 140;
    private int activeSummonTicks = 0;
    private LivingEntity pendingSummonTarget = null;

    public AbyssalNecromancerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity enemy) {
                if (this.canPerformAttack(enemy)) {
                    this.resetAttackCooldown();
                    this.mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(enemy);
                }
            }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            this.level().broadcastEntityEvent(this, (byte) 11);
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
            this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.1F, 0.7F);
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (castAnimationTimeout > 0) castAnimationTimeout--;
            if (staffSwingAnimationTimeout > 0) staffSwingAnimationTimeout--;
            if (summonAnimationTimeout > 0) summonAnimationTimeout--;

            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + 0.4D, this.getY() + 1.8D, this.getZ() + 0.4D,
                        0, 0.02D, 0);
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() - 0.3D, this.getY() + 0.3D, this.getZ() - 0.3D,
                        0, 0.01D, 0);
            }
        } else {
            if (summonCooldown > 0) summonCooldown--;
            if (soulOrbCooldown > 0) soulOrbCooldown--;
            if (curseCooldown > 0) curseCooldown--;

            if (activeSummonTicks > 0) {
                activeSummonTicks--;
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                if (pendingSummonTarget != null && pendingSummonTarget.isAlive()) {
                    this.getLookControl().setLookAt(pendingSummonTarget, 30.0F, 30.0F);
                }

                if (activeSummonTicks == 12 && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    Vec3 look = this.getLookAngle();
                    double groundX = this.getX() + look.x * 1.5D;
                    double groundY = this.getY();
                    double groundZ = this.getZ() + look.z * 1.5D;

                    this.playSound(SoundEvents.MACE_SMASH_GROUND, 1.5F, 0.7F);
                    this.playSound(SoundEvents.WITHER_SPAWN, 1.0F, 1.2F);

                    sl.sendParticles(ParticleTypes.EXPLOSION, groundX, groundY + 0.1D, groundZ, 2, 0.4D, 0.1D, 0.4D, 0.0D);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, groundX, groundY + 0.2D, groundZ, 50, 1.2D, 0.4D, 1.2D, 0.08D);
                    sl.sendParticles(ParticleTypes.SCULK_SOUL, groundX, groundY + 0.4D, groundZ, 30, 0.8D, 0.5D, 0.8D, 0.05D);
                    sl.sendParticles(ParticleTypes.LARGE_SMOKE, groundX, groundY + 0.2D, groundZ, 35, 1.0D, 0.3D, 1.0D, 0.04D);

                    int count = 2 + (this.random.nextBoolean() ? 1 : 0);
                    for (int i = 0; i < count; i++) {
                        double angle = (Math.PI * 2 * i) / count;
                        double ox = groundX + Math.cos(angle) * (1.5D + this.random.nextDouble());
                        double oz = groundZ + Math.sin(angle) * (1.5D + this.random.nextDouble());
                        Skeleton minion = new Skeleton(EntityType.SKELETON, sl);
                        minion.setPos(ox, groundY, oz);
                        minion.setPersistenceRequired();
                        minion.addTag("awakening_dungeon_mob");
                        if (pendingSummonTarget != null && pendingSummonTarget.isAlive()) {
                            minion.setTarget(pendingSummonTarget);
                        }
                        minion.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 0));
                        minion.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0));
                        sl.addFreshEntity(minion);
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ox, groundY + 0.5D, oz, 20, 0.3D, 0.4D, 0.3D, 0.04D);
                    }
                }
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distSq = this.distanceToSqr(target);

                if (soulOrbCooldown <= 0 && distSq < 400.0D && activeSummonTicks <= 0) {
                    soulOrbCooldown = 80;
                    this.level().broadcastEntityEvent(this, (byte) 8);
                    this.playSound(SoundEvents.WITHER_SHOOT, 1.2F, 1.2F);
                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        Vec3 dir = target.position().subtract(this.position()).normalize();
                        for (int i = 0; i < 3; i++) {
                            double spreadX = (this.random.nextDouble() - 0.5D) * 0.4D;
                            double spreadZ = (this.random.nextDouble() - 0.5D) * 0.4D;
                            double stepX = this.getX() + dir.x * (1.5D + i * 2.0D) + spreadX;
                            double stepY = this.getY() + 1.5D + dir.y * (1.5D + i * 2.0D);
                            double stepZ = this.getZ() + dir.z * (1.5D + i * 2.0D) + spreadZ;

                            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, stepX, stepY, stepZ, 6, 0.2D, 0.2D, 0.2D, 0.04D);
                            sl.sendParticles(ParticleTypes.SCULK_SOUL, stepX, stepY, stepZ, 4, 0.1D, 0.1D, 0.1D, 0.02D);
                        }
                        target.hurt(this.damageSources().indirectMagic(this, this), 12.0F);
                        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    }
                }

                if (curseCooldown <= 0 && distSq < 100.0D && activeSummonTicks <= 0) {
                    curseCooldown = 160;
                    this.level().broadcastEntityEvent(this, (byte) 8);
                    this.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.2F, 0.6F);
                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0D, target.getZ(),
                                30, 0.5D, 0.5D, 0.5D, 0.08D);
                        target.hurt(this.damageSources().magic(), 10.0F);
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                    }
                }

                if (summonCooldown <= 0 && activeSummonTicks <= 0 && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    summonCooldown = 280;
                    activeSummonTicks = 35;
                    pendingSummonTarget = target;
                    this.level().broadcastEntityEvent(this, (byte) 12);
                    this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.4F, 0.7F);
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 8) {
            this.castAnimationState.start(this.tickCount);
            this.castAnimationTimeout = 25;
        } else if (id == 11) {
            this.staffSwingAnimationState.start(this.tickCount);
            this.staffSwingAnimationTimeout = 20;
        } else if (id == 12) {
            this.summonAnimationState.start(this.tickCount);
            this.summonAnimationTimeout = 35;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }
}
