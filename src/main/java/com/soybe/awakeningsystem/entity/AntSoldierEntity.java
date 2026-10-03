package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Soldado Hormiga Gigante (Ant Soldier / Beru Style)
 * Criatura quimérica mutada de Rango Élite inspirada en la Isla Jeju (Solo Leveling).
 * Posee mandíbulas trituradoras, brazos de guadaña, alas de impulso y alta velocidad.
 */
public class AntSoldierEntity extends Monster {
    private static final EntityDataAccessor<Boolean> POUNCING =
            SynchedEntityData.defineId(AntSoldierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SCREECHING =
            SynchedEntityData.defineId(AntSoldierEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState pounceAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState screechAnimationState = new AnimationState();

    private int pounceCooldown = 80;
    private int screechCooldown = 0;
    private boolean hasEnraged = false;
    private int attackAnimationTicks = 0;
    private int pounceAnimationTicks = 0;
    private int screechTicks = 0;

    public AntSoldierEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 85;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(POUNCING, false);
        builder.define(SCREECHING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 130.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.ARMOR, 9.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AntWingedPounceGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.28D, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity enemy) {
                if (this.canPerformAttack(enemy)) {
                    this.resetAttackCooldown();
                    AntSoldierEntity.this.triggerAttackAnimation();
                    this.mob.doHurtTarget(enemy);
                }
            }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.IronGolem.class, true));
    }

    public boolean isPouncing() {
        return this.entityData.get(POUNCING);
    }

    public void setPouncing(boolean pouncing) {
        this.entityData.set(POUNCING, pouncing);
    }

    public boolean isScreeching() {
        return this.entityData.get(SCREECHING);
    }

    public void setScreeching(boolean screeching) {
        this.entityData.set(SCREECHING, screeching);
    }

    public void triggerAttackAnimation() {
        this.level().broadcastEntityEvent(this, (byte) 4);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.attackAnimationTicks = 12;
            this.attackAnimationState.start(this.tickCount);
        } else if (id == 5) {
            this.screechTicks = 25;
            this.screechAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            if (this.isPouncing()) {
                if (!this.pounceAnimationState.isStarted()) {
                    this.pounceAnimationState.start(this.tickCount);
                }
            } else {
                this.pounceAnimationState.stop();
            }

            if (this.isScreeching()) {
                if (!this.screechAnimationState.isStarted()) {
                    this.screechAnimationState.start(this.tickCount);
                }
            }

            if (this.attackAnimationTicks > 0) {
                this.attackAnimationTicks--;
                if (this.attackAnimationTicks <= 0) {
                    this.attackAnimationState.stop();
                }
            }

            if (this.screechTicks > 0) {
                this.screechTicks--;
                if (this.screechTicks <= 0) {
                    this.screechAnimationState.stop();
                }
            }
        } else {
            if (this.pounceCooldown > 0) {
                this.pounceCooldown--;
            }
            if (this.screechCooldown > 0) {
                this.screechCooldown--;
            }

            // Chillido al caer por debajo del 50% de HP
            if (!this.hasEnraged && this.getHealth() < this.getMaxHealth() * 0.5F && this.screechCooldown <= 0) {
                this.performSwarmScreech();
            }

            // Partículas sutiles de quitina/sombras al correr
            if (this.isPouncing() && this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.8D, this.getZ(),
                        3, 0.2D, 0.2D, 0.2D, 0.02D);
            }
        }
    }

    private void performSwarmScreech() {
        this.hasEnraged = true;
        this.screechCooldown = 400;
        this.setScreeching(true);
        this.level().broadcastEntityEvent(this, (byte) 5);

        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, true));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 0, false, true));

        this.playSound(com.soybe.awakeningsystem.ModSounds.ANT_SOLDIER_SCREECH.get(), 1.6F, 1.0F);

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.2D, this.getZ(),
                    1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.0D, this.getZ(),
                    20, 0.5D, 0.6D, 0.5D, 0.05D);

            // Alerta a monstruos cercanos
            var nearby = sl.getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(18.0D));
            for (Monster m : nearby) {
                if (m != this && m.getTarget() == null && this.getTarget() != null) {
                    m.setTarget(this.getTarget());
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            // Desgarro / Veneno de quitina
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));

            this.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.1F, 1.2F);
            this.playSound(com.soybe.awakeningsystem.ModSounds.ANT_SOLDIER_STEP.get(), 1.0F, 1.5F);

            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIT, living.getX(), living.getY() + 1.0D, living.getZ(),
                        12, 0.3D, 0.4D, 0.3D, 0.1D);
                sl.sendParticles(ParticleTypes.SQUID_INK, living.getX(), living.getY() + 1.0D, living.getZ(),
                        8, 0.2D, 0.3D, 0.2D, 0.02D);
            }
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Resistencia parcial a daño indirecto por quitina dura
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            amount *= 0.75F;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return com.soybe.awakeningsystem.ModSounds.ANT_SOLDIER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return com.soybe.awakeningsystem.ModSounds.ANT_SOLDIER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return com.soybe.awakeningsystem.ModSounds.ANT_SOLDIER_DEATH.get();
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(com.soybe.awakeningsystem.ModSounds.ANT_SOLDIER_STEP.get(), 0.7F, 1.0F);
    }

    /**
     * Salto alado en picada hacia la presa
     */
    static class AntWingedPounceGoal extends Goal {
        private final AntSoldierEntity ant;
        private LivingEntity target;
        private int leapTicks = 0;

        public AntWingedPounceGoal(AntSoldierEntity ant) {
            this.ant = ant;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (this.ant.pounceCooldown > 0) return false;
            this.target = this.ant.getTarget();
            if (this.target == null || !this.target.isAlive()) return false;
            double distSq = this.ant.distanceToSqr(this.target);
            return distSq >= 16.0D && distSq <= 196.0D && this.ant.onGround() && this.ant.hasLineOfSight(this.target);
        }

        @Override
        public void start() {
            this.ant.setPouncing(true);
            this.ant.pounceCooldown = 100 + this.ant.getRandom().nextInt(40);
            this.leapTicks = 0;

            Vec3 diff = this.target.position().subtract(this.ant.position());
            Vec3 horiz = new Vec3(diff.x, 0, diff.z).normalize();
            double distance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

            double forwardSpeed = Math.min(1.45D, 0.45D + distance * 0.08D);
            double upwardImpulse = 0.42D + Math.min(0.28D, diff.y * 0.1D);

            this.ant.setDeltaMovement(horiz.x * forwardSpeed, upwardImpulse, horiz.z * forwardSpeed);
            this.ant.hasImpulse = true;

            this.ant.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.2F, 1.7F);
            this.ant.playSound(SoundEvents.PHANTOM_SWOOP, 1.0F, 1.3F);

            if (this.ant.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CLOUD, this.ant.getX(), this.ant.getY() + 0.2D, this.ant.getZ(),
                        10, 0.3D, 0.1D, 0.3D, 0.05D);
            }
        }

        @Override
        public void tick() {
            this.leapTicks++;
            if (this.target != null && this.ant.distanceToSqr(this.target) < 4.0D && this.leapTicks < 25) {
                this.ant.doHurtTarget(this.target);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !this.ant.onGround() && this.leapTicks < 40;
        }

        @Override
        public void stop() {
            this.ant.setPouncing(false);
        }
    }
}
