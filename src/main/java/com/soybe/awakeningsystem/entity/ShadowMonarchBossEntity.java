package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.phys.Vec3;

public class ShadowMonarchBossEntity extends Monster {
    public final AnimationState slashAnimationState = new AnimationState();
    public final AnimationState castAnimationState = new AnimationState();
    public final AnimationState hoverAnimationState = new AnimationState();
    public final AnimationState pullAnimationState = new AnimationState();
    public int slashAnimationTimeout = 0;
    public int castAnimationTimeout = 0;
    public int pullAnimationTimeout = 0;

    private int teleportCooldown = 80;
    private int summonCooldown = 240;
    private int pullCooldown = 180;
    private int spearRainCooldown = 130;
    private int domainCooldown = 320;
    private int domainTicks = 0;
    private boolean enraged = false;

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_VARIANT =
            net.minecraft.network.syncher.SynchedEntityData.defineId(ShadowMonarchBossEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    public ShadowMonarchBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 150;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Variant")) {
            this.setVariant(compound.getInt("Variant"));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 5000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 32.0D)
                .add(Attributes.ARMOR, 24.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
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
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.level().broadcastEntityEvent(this, (byte) 7);
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            }

            var aabb = target.getBoundingBox().inflate(3.0D);
            var nearby = this.level().getEntitiesOfClass(LivingEntity.class, aabb,
                    e -> e != this && !e.getTags().contains("shadow_soldier") && !e.getType().equals(this.getType()));
            for (var victim : nearby) {
                victim.hurt(this.damageSources().mobAttack(this), 10.0F);
                double dx = victim.getX() - this.getX();
                double dz = victim.getZ() - this.getZ();
                double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                victim.setDeltaMovement(dx / dist * 1.2D, 0.4D, dz / dist * 1.2D);
            }

            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(),
                        3, 0.5D, 0.2D, 0.5D, 0.05D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(),
                        30, 0.6D, 0.6D, 0.6D, 0.1D);
            }
            this.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.2F, 0.7F);
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (slashAnimationTimeout > 0) slashAnimationTimeout--;
            if (castAnimationTimeout > 0) castAnimationTimeout--;
            if (pullAnimationTimeout > 0) pullAnimationTimeout--;
            this.hoverAnimationState.startIfStopped(this.tickCount);

            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.DRAGON_BREATH,
                        this.getRandomX(0.7D), this.getY() + this.random.nextDouble() * 2.2D, this.getRandomZ(0.7D),
                        0, 0.02D, 0);
            }
            if (enraged && this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getRandomX(0.8D), this.getY() + 0.5D, this.getRandomZ(0.8D),
                        0, 0.04D, 0);
            }

            if (domainTicks > 0) {
                domainTicks--;
                for (int deg = 0; deg < 360; deg += 36) {
                    double rad = Math.toRadians(deg + (this.tickCount * 3));
                    double px = this.getX() + Math.cos(rad) * 8.0D;
                    double pz = this.getZ() + Math.sin(rad) * 8.0D;
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, this.getY() + 0.15D, pz, 0, 0.04D, 0);
                    this.level().addParticle(ParticleTypes.SCULK_SOUL, px, this.getY() + 0.2D, pz, 0, 0.02D, 0);
                }
            }
        } else {
            if (domainTicks > 0) {
                domainTicks--;
                if (domainTicks % 10 == 0) {
                    this.heal(3.0F);
                }
                if (domainTicks % 20 == 0 && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    var domainAabb = this.getBoundingBox().inflate(8.5D, 3.0D, 8.5D);
                    var victims = sl.getEntitiesOfClass(LivingEntity.class, domainAabb,
                            e -> e != this && !e.getTags().contains("shadow_soldier") && !e.getType().equals(this.getType()));
                    for (var v : victims) {
                        v.hurt(this.damageSources().magic(), 7.0F);
                        v.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
                        v.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
                        v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                    }
                }
            }

            if (!enraged && this.getHealth() <= this.getMaxHealth() * 0.5F) {
                enraged = true;
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 99999, 1));
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, 1));
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5F, 0.8F);
                broadcastBossMessage("awakening_system.boss.shadow_monarch.phase2_taunt");
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0D, this.getZ(),
                            1, 0, 0, 0, 0);
                }
            }

            if (pullCooldown > 0) pullCooldown--;
            if (spearRainCooldown > 0) spearRainCooldown--;
            if (domainCooldown > 0) domainCooldown--;

            LivingEntity target = this.getTarget();
            if (target != null) {
                double distSq = this.distanceToSqr(target);

                if (domainCooldown <= 0 && distSq < 144.0D) {
                    domainCooldown = enraged ? 240 : 360;
                    domainTicks = 160;
                    this.level().broadcastEntityEvent(this, (byte) 8);
                    this.playSound(SoundEvents.WITHER_SPAWN, 1.2F, 1.2F);
                    broadcastBossMessage("awakening_system.boss.shadow_monarch.domain_taunt");
                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        for (int i = 0; i < 40; i++) {
                            double angle = (Math.PI * 2 * i) / 40.0;
                            double px = this.getX() + Math.cos(angle) * 8.0D;
                            double pz = this.getZ() + Math.sin(angle) * 8.0D;
                            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, this.getY() + 0.2D, pz, 3, 0.1D, 0.2D, 0.1D, 0.02D);
                            sl.sendParticles(ParticleTypes.DRAGON_BREATH, px, this.getY() + 0.2D, pz, 2, 0.05D, 0.05D, 0.05D, 0.01D);
                        }
                    }
                }

                if (pullCooldown <= 0 && distSq >= 16.0D && distSq <= 289.0D) {
                    pullCooldown = enraged ? 140 : 200;
                    this.level().broadcastEntityEvent(this, (byte) 10);
                    this.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 1.4F, 1.1F);
                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        Vec3 pullDir = this.position().subtract(target.position()).normalize();
                        target.setDeltaMovement(pullDir.x * 1.5D, 0.35D, pullDir.z * 1.5D);
                        target.hurtMarked = true;
                        sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.2D, this.getZ(), 1, 0, 0, 0, 0);
                        sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.2D);
                        target.hurt(this.damageSources().magic(), 14.0F);
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                    }
                }

                if (spearRainCooldown <= 0 && distSq <= 324.0D) {
                    spearRainCooldown = enraged ? 100 : 150;
                    this.level().broadcastEntityEvent(this, (byte) 8);
                    this.playSound(SoundEvents.TRIDENT_THUNDER.value(), 1.3F, 0.8F);
                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        double tx = target.getX();
                        double ty = target.getY();
                        double tz = target.getZ();
                        for (int s = 0; s < 3; s++) {
                            double ox = tx + (this.random.nextDouble() - 0.5D) * 3.0D;
                            double oz = tz + (this.random.nextDouble() - 0.5D) * 3.0D;
                            for (double y = ty + 7.0D; y >= ty; y -= 1.0D) {
                                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ox, y, oz, 4, 0.1D, 0.1D, 0.1D, 0.02D);
                            }
                            sl.sendParticles(ParticleTypes.EXPLOSION, ox, ty + 0.2D, oz, 2, 0.2D, 0.1D, 0.2D, 0.02D);
                            sl.sendParticles(ParticleTypes.SCULK_SOUL, ox, ty + 0.5D, oz, 8, 0.3D, 0.5D, 0.3D, 0.05D);
                        }
                        target.hurt(this.damageSources().magic(), 12.0F);
                        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                    }
                }

                if (teleportCooldown > 0) teleportCooldown--;
                if (teleportCooldown <= 0 && distSq > 64.0D) {
                    teleportCooldown = 140;
                    Vec3 look = target.getLookAngle();
                    double bx = target.getX() - look.x * 2.5D;
                    double by = target.getY();
                    double bz = target.getZ() - look.z * 2.5D;
                    Vec3 safePos = com.soybe.awakeningsystem.TeleportHelper.findSafeTeleportPos(this.level(), this, bx, by, bz, target.position());
                    this.teleportTo(safePos.x, safePos.y, safePos.z);
                    this.level().playSound(null, safePos.x, safePos.y, safePos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2F, 0.9F);
                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.PORTAL, safePos.x, safePos.y + 1.0D, safePos.z, 40, 0.5D, 0.8D, 0.5D, 0.2D);
                    }
                }

                if (summonCooldown > 0) summonCooldown--;
                if (summonCooldown <= 0 && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    summonCooldown = enraged ? 200 : 320;
                    this.level().broadcastEntityEvent(this, (byte) 8);
                    this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.2F, 0.8F);
                    for (int i = 0; i < (enraged ? 3 : 2); i++) {
                        double ox = this.getX() + (this.random.nextDouble() - 0.5D) * 4.0D;
                        double oz = this.getZ() + (this.random.nextDouble() - 0.5D) * 4.0D;
                        ShadowInfantryEntity minion = ModEntities.SHADOW_INFANTRY.get().create(sl);
                        if (minion != null) {
                            minion.setPos(ox, this.getY(), oz);
                            minion.addTag("awakening_dungeon_mob");
                            minion.setTarget(target);
                            sl.addFreshEntity(minion);
                            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ox, this.getY() + 0.8D, oz,
                                    25, 0.3D, 0.5D, 0.3D, 0.05D);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7) {
            this.slashAnimationState.start(this.tickCount);
            this.slashAnimationTimeout = 20;
        } else if (id == 8) {
            this.castAnimationState.start(this.tickCount);
            this.castAnimationTimeout = 30;
        } else if (id == 10) {
            this.pullAnimationState.start(this.tickCount);
            this.pullAnimationTimeout = 30;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.getVariant() == 1) {
            broadcastBossMessage("awakening_system.boss.ashborn.defeat_quote");
        } else {
            broadcastBossMessage("awakening_system.boss.antares.defeat_quote");
        }
        if (damageSource.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            com.soybe.awakeningsystem.ConstellationAscension.checkMonarchBossDefeat(player, this.getVariant());
        }
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.2D, this.getZ(), 60, 0.8D, 1.0D, 0.8D, 0.1D);
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY() + 1.2D, this.getZ(), 40, 0.8D, 0.8D, 0.8D, 0.05D);
        }
    }

    private void broadcastBossMessage(String translationKey) {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            net.minecraft.network.chat.Component comp = net.minecraft.network.chat.Component.translatable(translationKey);
            for (net.minecraft.server.level.ServerPlayer p : sl.players()) {
                if (p.distanceToSqr(this) < 64.0D * 64.0D) {
                    p.displayClientMessage(comp, true);
                }
            }
        }
    }
}
