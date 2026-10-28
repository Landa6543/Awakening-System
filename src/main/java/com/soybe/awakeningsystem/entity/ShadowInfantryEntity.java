package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.Vec3;

public class ShadowInfantryEntity extends Monster {
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(ShadowInfantryEntity.class, EntityDataSerializers.INT);

    public static final int VARIANT_CYAN = 0; // Sombra invocada aliada del jugador (Celeste)
    public static final int VARIANT_RED = 1;  // Caballero hostil / minion de mazmorra (Rojo)

    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState castAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;
    public int castAnimationTimeout = 0;
    private int dashCooldown = 50;
    private int rangedCooldown = 70;
    private int decoyCooldown = 160;
    private Vec3 pendingDecoyPos = null;
    private int pendingDecoyTimer = -1;

    public ShadowInfantryEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 25;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_RED);
    }

    public int getVariant() {
        if (this.getTags().contains("shadow_soldier")) {
            return VARIANT_CYAN;
        }
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    public boolean isFriendlyShadow() {
        return this.getTags().contains("shadow_soldier") || this.getVariant() == VARIANT_CYAN;
    }

    public boolean isRedVariant() {
        return !isFriendlyShadow() && this.getVariant() == VARIANT_RED;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Variant")) {
            this.setVariant(compound.getInt("Variant"));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 85.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 11.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity enemy) {
                if (this.canPerformAttack(enemy)) {
                    this.resetAttackCooldown();
                    this.mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(enemy);
                }
            }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                if (mob.getLastHurtByMob() instanceof Player p && (mob.getTags().contains("shadow_soldier") || mob.getTags().contains("shadow_master_" + p.getUUID()))) {
                    return false;
                }
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (target) -> {
            if (this.getTags().contains("shadow_soldier")) {
                return false;
            }
            return target instanceof Player;
        }));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof Player p) {
            if (this.getTags().contains("shadow_soldier") || this.getTags().contains("shadow_master_" + p.getUUID())) {
                return false;
            }
        }
        return super.canAttack(target);
    }

    @Override
    public void setTarget(LivingEntity target) {
        if (target instanceof Player p) {
            if (this.getTags().contains("shadow_soldier") || this.getTags().contains("shadow_master_" + p.getUUID())) {
                return;
            }
        }
        super.setTarget(target);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.level().broadcastEntityEvent(this, (byte) 4);
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                var flameParticle = this.isRedVariant() ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;
                sl.sendParticles(flameParticle, target.getX(), target.getY() + 1.0D, target.getZ(),
                        16, 0.25D, 0.35D, 0.25D, 0.05D);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 0.8D, target.getZ(),
                        2, 0.2D, 0.2D, 0.2D, 0.0D);
            }
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.2F);
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && decoyCooldown <= 0 && this.getHealth() < this.getMaxHealth() * 0.45F) {
            decoyCooldown = 220;
            LivingEntity attacker = source.getEntity() instanceof LivingEntity le ? le : this.getTarget();
            if (attacker != null) {
                pendingDecoyPos = this.position();
                pendingDecoyTimer = 20;

                Vec3 look = attacker.getLookAngle();
                double bx = attacker.getX() - look.x * 2.5D;
                double by = attacker.getY();
                double bz = attacker.getZ() - look.z * 2.5D;
                Vec3 safePos = com.soybe.awakeningsystem.TeleportHelper.findSafeTeleportPos(this.level(), this, bx, by, bz, attacker.position());
                this.teleportTo(safePos.x, safePos.y, safePos.z);
                this.level().playSound(null, safePos.x, safePos.y, safePos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.3F);

                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    var flameParticle = this.isRedVariant() ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;
                    sl.sendParticles(flameParticle, pendingDecoyPos.x, pendingDecoyPos.y + 0.8D, pendingDecoyPos.z,
                            25, 0.3D, 0.5D, 0.3D, 0.08D);
                    sl.sendParticles(ParticleTypes.SMOKE, pendingDecoyPos.x, pendingDecoyPos.y + 0.5D, pendingDecoyPos.z,
                            20, 0.3D, 0.4D, 0.3D, 0.02D);
                }
            }
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            setupAnimationStates();
            if (this.random.nextInt(3) == 0) {
                var flameParticle = this.isRedVariant() ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;
                this.level().addParticle(flameParticle,
                        this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D),
                        0, 0.02D, 0);
            }
        } else {
            if (this.getTags().contains("shadow_soldier") && !this.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING)) {
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 72000, 0, false, false, false));
            }
            if (pendingDecoyTimer > 0) {
                pendingDecoyTimer--;
                if (pendingDecoyTimer == 0 && pendingDecoyPos != null && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    var flameParticle = this.isRedVariant() ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;
                    sl.sendParticles(ParticleTypes.EXPLOSION, pendingDecoyPos.x, pendingDecoyPos.y + 0.5D, pendingDecoyPos.z, 3, 0.4D, 0.4D, 0.4D, 0.05D);
                    sl.sendParticles(flameParticle, pendingDecoyPos.x, pendingDecoyPos.y + 0.8D, pendingDecoyPos.z, 35, 0.6D, 0.6D, 0.6D, 0.12D);
                    this.level().playSound(null, pendingDecoyPos.x, pendingDecoyPos.y, pendingDecoyPos.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0F, 1.2F);

                    var aabb = new net.minecraft.world.phys.AABB(pendingDecoyPos.x - 3.0D, pendingDecoyPos.y - 1.0D, pendingDecoyPos.z - 3.0D,
                            pendingDecoyPos.x + 3.0D, pendingDecoyPos.y + 3.0D, pendingDecoyPos.z + 3.0D);
                    var victims = sl.getEntitiesOfClass(LivingEntity.class, aabb,
                            e -> e != this && !e.getTags().contains("shadow_soldier") && !e.getType().equals(this.getType()));
                    double currentAtk = this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float decoyDmg = (float) Math.max(2.5D, currentAtk * 0.9D);
                    for (var v : victims) {
                        v.hurt(this.damageSources().magic(), decoyDmg);
                        v.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, 60, 0));
                    }
                    pendingDecoyPos = null;
                }
            }

            if (decoyCooldown > 0) decoyCooldown--;
            if (dashCooldown > 0) dashCooldown--;
            if (rangedCooldown > 0) rangedCooldown--;

            LivingEntity target = this.getTarget();
            if (target != null) {
                double distSq = this.distanceToSqr(target);

                if (rangedCooldown <= 0 && distSq >= 25.0D && distSq <= 324.0D) {
                    rangedCooldown = 100;
                    this.level().broadcastEntityEvent(this, (byte) 9);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.2F, 0.6F);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.2F, 1.2F);

                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        var flameParticle = this.isRedVariant() ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;
                        var trailParticle = this.isRedVariant() ? ParticleTypes.SMOKE : ParticleTypes.SCULK_SOUL;
                        Vec3 dir = target.position().subtract(this.position()).normalize();
                        for (double s = 1.0D; s <= Math.sqrt(distSq); s += 1.2D) {
                            double px = this.getX() + dir.x * s;
                            double py = this.getY() + 1.0D + dir.y * s;
                            double pz = this.getZ() + dir.z * s;
                            sl.sendParticles(flameParticle, px, py, pz, 4, 0.15D, 0.15D, 0.15D, 0.02D);
                            sl.sendParticles(trailParticle, px, py, pz, 1, 0.1D, 0.1D, 0.1D, 0.01D);
                        }
                        double rangedAtk = this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        float waveDmg = (float) Math.max(2.0D, rangedAtk * 0.8D);
                        target.hurt(this.damageSources().magic(), waveDmg);
                        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, 50, 0));
                    }
                }

                if (dashCooldown <= 0 && distSq > 9.0D && distSq < 64.0D) {
                    Vec3 dir = target.position().subtract(this.position()).normalize();
                    this.setDeltaMovement(dir.x * 1.4D, 0.28D, dir.z * 1.4D);
                    this.hurtMarked = true;
                    this.dashCooldown = 90;
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.0F, 1.4F);
                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        var flameParticle = this.isRedVariant() ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;
                        sl.sendParticles(flameParticle, this.getX(), this.getY() + 0.8D, this.getZ(),
                                20, 0.3D, 0.5D, 0.3D, 0.1D);
                    }
                }
            }
        }
    }

    private void setupAnimationStates() {
        if (this.attackAnimationTimeout > 0) this.attackAnimationTimeout--;
        if (this.castAnimationTimeout > 0) this.castAnimationTimeout--;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.attackAnimationState.start(this.tickCount);
            this.attackAnimationTimeout = 24;
        } else if (id == 9) {
            this.castAnimationState.start(this.tickCount);
            this.castAnimationTimeout = 20;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }
}
