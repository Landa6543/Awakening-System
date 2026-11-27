package com.soybe.awakeningsystem.entity.bestiary;

import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Sonrisa (roadmap 14.12): a round boulder of cracked, mossy stone with a face carved into it, two sunken eyes and a
 * wide grin. It lies still in the tunnels like any rock, its mouth shut. When a hunter comes near (or hits it) it
 * wakes: the grin falls open, the eyes light up, and it rolls at them fast, crushing what it runs into, knocking it
 * far and leaving it stunned. Arrows barely scratch it. After a while without prey it settles again.
 */
public class SonrisaEntity extends Monster {
    private static final EntityDataAccessor<Boolean> AWAKE = SynchedEntityData.defineId(SonrisaEntity.class, EntityDataSerializers.BOOLEAN);
    private int sleepTimer, crushCooldown;
    private float mouth, mouthO;

    public SonrisaEntity(EntityType<? extends SonrisaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(AWAKE, false);
    }

    public boolean isAwake() {
        return this.entityData.get(AWAKE);
    }

    public float mouth(float partialTick) {
        return mouthO + (mouth - mouthO) * partialTick;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.6D, true) {
            @Override
            public boolean canUse() { return isAwake() && super.canUse(); }
            @Override
            public boolean canContinueToUse() { return isAwake() && super.canContinueToUse(); }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity t = this.getTarget();
        boolean prey = t != null && t.isAlive();
        if (this.getTags().contains("shadow_soldier")) {
            if (!isAwake()) {
                this.entityData.set(AWAKE, true);
            }
            sleepTimer = 0;
        } else if (!isAwake()) {
            // just a rock: no moving, no turning
            this.getNavigation().stop();
            this.setYRot(this.yRotO);
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            if (prey && this.distanceToSqr(t) < 10 * 10) wake();
            return;
        }
        if (crushCooldown > 0) crushCooldown--;
        if (prey && this.distanceToSqr(t) < 32 * 32) sleepTimer = 0;
        else if (++sleepTimer > 200) {
            this.entityData.set(AWAKE, false);
            sleepTimer = 0;
        }
        // rolling into its prey crushes it, knocks it far and stuns it
        Vec3 v = this.getDeltaMovement();
        if (prey && crushCooldown == 0 && v.horizontalDistanceSqr() > 0.01 && this.getBoundingBox().inflate(0.4).intersects(t.getBoundingBox())) {
            crushCooldown = 30;
            t.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2F);
            Vec3 push = t.position().subtract(this.position()).multiply(1, 0, 1).normalize().scale(1.6);
            t.push(push.x, 0.55, push.z);
            t.hurtMarked = true;
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 3), this);
            t.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0), this);
            this.level().playSound(null, this, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8F, 0.6F);
        }
    }

    public void wake() {
        this.entityData.set(AWAKE, true);
        sleepTimer = 0;
        this.level().playSound(null, this, SoundEvents.GRINDSTONE_USE, SoundSource.HOSTILE, 1.4F, 0.5F);
        if (this.level() instanceof ServerLevel sl)
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), this.getX(), this.getY() + 1, this.getZ(), 30, 0.8, 0.8, 0.8, 0.1);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Projectile) amount *= 0.25F;   // arrows barely scratch the stone
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && !isAwake() && source.getEntity() instanceof LivingEntity) wake();
        return hurt;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            mouthO = mouth;
            mouth += ((isAwake() ? 1.0F : 0.0F) - mouth) * 0.12F;
            if (isAwake() && this.getDeltaMovement().horizontalDistanceSqr() > 0.01 && this.random.nextInt(2) == 0)
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        this.getX() + (this.random.nextDouble() - 0.5), this.getY() + 0.1, this.getZ() + (this.random.nextDouble() - 0.5), 0, 0.1, 0);
        }
    }

    @Override
    public boolean isPushable() {
        return isAwake();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.STONE_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STONE_BREAK;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.GRINDSTONE_USE, 0.25F, 0.4F + this.random.nextFloat() * 0.2F);
    }
}
