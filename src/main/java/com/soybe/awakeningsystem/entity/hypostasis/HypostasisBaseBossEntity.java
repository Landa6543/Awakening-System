package com.soybe.awakeningsystem.entity.hypostasis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public abstract class HypostasisBaseBossEntity extends Monster {

    public static final int STATE_PROTECTED = 0;
    public static final int STATE_ATTACKING = 1;
    public static final int STATE_VULNERABLE = 2;
    public static final int STATE_REVIVAL = 3;
    public static final int STATE_DEFEATED = 4;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(HypostasisBaseBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ID =
            SynchedEntityData.defineId(HypostasisBaseBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ATTACK_PROGRESS =
            SynchedEntityData.defineId(HypostasisBaseBossEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ANIM_TICK =
            SynchedEntityData.defineId(HypostasisBaseBossEntity.class, EntityDataSerializers.FLOAT);

    protected final ServerBossEvent bossEvent;
    protected final Random rng = new Random();

    private int stateTimer = 60;
    private int attackDuration = 0;
    private int attackElapsed = 0;
    private boolean revivalAttempted = false;

    public HypostasisBaseBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.xpReward = 800;

        this.bossEvent = (ServerBossEvent) new ServerBossEvent(
                getBossTitle(),
                getBossBarColor(),
                BossEvent.BossBarOverlay.NOTCHED_10
        ).setDarkenScreen(false);
    }

    public static AttributeSupplier.Builder createHypostasisAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 3800.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 26.0D)
                .add(Attributes.ARMOR, 24.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, STATE_PROTECTED);
        builder.define(DATA_ATTACK_ID, HypostasisAttackType.NONE.getId());
        builder.define(DATA_ATTACK_PROGRESS, 0.0F);
        builder.define(DATA_ANIM_TICK, 0.0F);
    }

    public int getBossState() {
        return this.entityData.get(DATA_STATE);
    }

    public void setBossState(int state) {
        this.entityData.set(DATA_STATE, state);
    }

    public HypostasisAttackType getAttackType() {
        return HypostasisAttackType.byId(this.entityData.get(DATA_ATTACK_ID));
    }

    public void setAttackType(HypostasisAttackType type) {
        this.entityData.set(DATA_ATTACK_ID, type.getId());
    }

    public float getAttackProgress() {
        return this.entityData.get(DATA_ATTACK_PROGRESS);
    }

    public void setAttackProgress(float progress) {
        this.entityData.set(DATA_ATTACK_PROGRESS, progress);
    }

    public float getAnimTick() {
        return this.entityData.get(DATA_ANIM_TICK);
    }

    @Override
    public void tick() {
        super.tick();

        float anim = this.entityData.get(DATA_ANIM_TICK) + 1.0F;
        this.entityData.set(DATA_ANIM_TICK, anim);

        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            handleServerBossAi();
        } else {
            handleClientParticles();
        }
    }

    protected void handleServerBossAi() {
        ServerLevel sl = (ServerLevel) this.level();
        Player target = sl.getNearestPlayer(this, 40.0D);

        if (!revivalAttempted && this.getHealth() <= this.getMaxHealth() * 0.06F && getBossState() != STATE_REVIVAL && getBossState() != STATE_DEFEATED) {
            triggerRevivalPhase(sl);
            return;
        }

        int state = getBossState();
        switch (state) {
            case STATE_PROTECTED -> {
                hoverTowards(target, 2.5D, 0.06D);

                stateTimer--;
                if (stateTimer <= 0) {
                    selectAndStartAttack(target);
                }
            }
            case STATE_ATTACKING -> {
                HypostasisAttackType currentAttack = getAttackType();
                attackElapsed++;
                float progress = attackDuration > 0 ? (float) attackElapsed / (float) attackDuration : 1.0F;
                setAttackProgress(Math.min(progress, 1.0F));

                if (target != null) {
                    executeAttackStep(sl, target, currentAttack, progress);
                }

                if (attackElapsed >= attackDuration) {
                    startVulnerableState();
                }
            }
            case STATE_VULNERABLE -> {
                descendToGround();

                stateTimer--;
                if (stateTimer <= 0) {
                    endVulnerableState(sl);
                }
            }
            case STATE_REVIVAL -> {
                hoverAtHeight(3.5D);

                stateTimer--;
                boolean cleared = checkRevivalObjectsCleared(sl);
                if (cleared) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 1.8F, 1.0F);
                    setBossState(STATE_DEFEATED);
                    this.hurt(this.damageSources().genericKill(), 9999.0F);
                } else if (stateTimer <= 0) {
                    cleanupRevivalObjects(sl);
                    this.heal(this.getMaxHealth() * 0.30F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5F, 1.2F);
                    setBossState(STATE_PROTECTED);
                    stateTimer = 60;
                }
            }
        }
    }

    protected void selectAndStartAttack(Player target) {
        int pick = rng.nextInt(4);
        HypostasisAttackType atk = switch (pick) {
            case 0 -> HypostasisAttackType.CLAP;
            case 1 -> HypostasisAttackType.MISSILES;
            case 2 -> HypostasisAttackType.SPECIAL_ACTION;
            default -> HypostasisAttackType.BURST_ULTIMATE;
        };

        setAttackType(atk);
        setBossState(STATE_ATTACKING);
        this.attackDuration = atk.getDurationTicks();
        this.attackElapsed = 0;
        setAttackProgress(0.0F);

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.4F, 0.8F);
        }
    }

    protected void startVulnerableState() {
        setBossState(STATE_VULNERABLE);
        setAttackType(HypostasisAttackType.NONE);
        setAttackProgress(0.0F);
        this.stateTimer = 130;

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 1.8F, 0.9F);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 1.2F, 1.3F);
        }
    }

    protected void endVulnerableState(ServerLevel sl) {
        setBossState(STATE_PROTECTED);
        this.stateTimer = 70;

        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.8F, 1.2F);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.4F, 1.4F);
    }

    protected void triggerRevivalPhase(ServerLevel sl) {
        this.revivalAttempted = true;
        setBossState(STATE_REVIVAL);
        setAttackType(HypostasisAttackType.NONE);
        this.stateTimer = 340;

        sl.playSound(null, this.getX(), this.getY(), this.getZ(), getRevivalSound(), SoundSource.HOSTILE, 1.8F, 1.0F);

        spawnRevivalObjects(sl);
    }

    protected SoundEvent getRevivalSound() {
        return SoundEvents.TOTEM_USE;
    }

    protected void hoverTowards(Player target, double heightAboveGround, double speed) {
        double destY = this.getY();
        BlockPos below = this.blockPosition();
        while (below.getY() > this.level().getMinBuildHeight() && this.level().getBlockState(below).isAir()) {
            below = below.below();
        }
        destY = below.getY() + 1 + heightAboveGround;

        double vx = 0;
        double vz = 0;
        if (target != null && this.distanceToSqr(target) > 16.0D) {
            Vec3 dir = target.position().subtract(this.position()).normalize();
            vx = dir.x * speed;
            vz = dir.z * speed;
        }

        double vy = (destY - this.getY()) * 0.1D;
        this.setDeltaMovement(vx, vy, vz);
    }

    protected void descendToGround() {
        BlockPos below = this.blockPosition();
        while (below.getY() > this.level().getMinBuildHeight() && this.level().getBlockState(below).isAir()) {
            below = below.below();
        }
        double targetGroundY = below.getY() + 1.2D;
        double vy = (targetGroundY - this.getY()) * 0.15D;
        this.setDeltaMovement(0, vy, 0);
    }

    protected void hoverAtHeight(double heightAboveGround) {
        BlockPos below = this.blockPosition();
        while (below.getY() > this.level().getMinBuildHeight() && this.level().getBlockState(below).isAir()) {
            below = below.below();
        }
        double targetY = below.getY() + 1 + heightAboveGround;
        double vy = (targetY - this.getY()) * 0.12D;
        this.setDeltaMovement(0, vy, 0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        int state = getBossState();

        if (state == STATE_REVIVAL || state == STATE_DEFEATED) {
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 1.5F);
            }
            return false;
        }

        if (state == STATE_PROTECTED || state == STATE_ATTACKING) {
            float reduced = amount * 0.10F;
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_BREAK, SoundSource.HOSTILE, 1.0F, 1.6F);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 1.0D, this.getZ(), 8, 0.4D, 0.4D, 0.4D, 0.1D);
            }
            return super.hurt(source, reduced);
        }

        if (state == STATE_VULNERABLE) {
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.4F, 1.1F);
                sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.8D, this.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.2D);
            }
            return super.hurt(source, amount * 1.25F);
        }

        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel sl) {
            cleanupRevivalObjects(sl);
            dropBossLoot(sl);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("BossState", getBossState());
        compound.putInt("AttackId", getAttackType().getId());
        compound.putInt("StateTimer", this.stateTimer);
        compound.putBoolean("RevivalAttempted", this.revivalAttempted);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setBossState(compound.getInt("BossState"));
        setAttackType(HypostasisAttackType.byId(compound.getInt("AttackId")));
        this.stateTimer = compound.getInt("StateTimer");
        this.revivalAttempted = compound.getBoolean("RevivalAttempted");
    }

    protected abstract Component getBossTitle();
    protected abstract BossEvent.BossBarColor getBossBarColor();
    public abstract ParticleOptions getElementalParticle();
    public abstract int getElementalColorHex();

    protected abstract void executeAttackStep(ServerLevel sl, Player target, HypostasisAttackType attack, float progress);
    protected abstract void spawnRevivalObjects(ServerLevel sl);
    protected abstract boolean checkRevivalObjectsCleared(ServerLevel sl);
    protected abstract void cleanupRevivalObjects(ServerLevel sl);
    protected abstract void dropBossLoot(ServerLevel sl);
    protected abstract void handleClientParticles();
}
