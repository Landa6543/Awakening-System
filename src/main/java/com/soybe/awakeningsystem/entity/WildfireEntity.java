package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class WildfireEntity extends Monster {

    private static final EntityDataAccessor<Integer> ACTIVE_SHIELDS =
            SynchedEntityData.defineId(WildfireEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_PERFORMING_SHOCKWAVE =
            SynchedEntityData.defineId(WildfireEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SHOCKWAVE_TICKS =
            SynchedEntityData.defineId(WildfireEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MINI_BOSS =
            SynchedEntityData.defineId(WildfireEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(Component.translatable("entity.awakening_system.wildfire.boss_name"),
                    BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    private float shieldDamageAccumulator = 0.0F;
    private int shieldRegenCooldown = 0;
    private boolean summonedBlazes = false;

    public WildfireEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.xpReward = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 280.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVE_SHIELDS, 4);
        builder.define(IS_PERFORMING_SHOCKWAVE, false);
        builder.define(SHOCKWAVE_TICKS, 0);
        builder.define(IS_MINI_BOSS, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ShockwaveAttackGoal(this));
        this.goalSelector.addGoal(2, new FireballBarrageGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public int getActiveShields() {
        return this.entityData.get(ACTIVE_SHIELDS);
    }

    public void setActiveShields(int count) {
        this.entityData.set(ACTIVE_SHIELDS, Math.max(0, Math.min(4, count)));
    }

    public boolean isPerformingShockwave() {
        return this.entityData.get(IS_PERFORMING_SHOCKWAVE);
    }

    public boolean isMiniBoss() {
        return this.entityData.get(IS_MINI_BOSS);
    }

    public void setMiniBoss(boolean mini) {
        this.entityData.set(IS_MINI_BOSS, mini);
        if (mini) {
            this.bossEvent.setName(Component.translatable("entity.awakening_system.wildfire.miniboss_name"));
            this.bossEvent.setColor(BossEvent.BossBarColor.YELLOW);
            this.bossEvent.setOverlay(BossEvent.BossBarOverlay.PROGRESS);
            this.setCustomName(Component.translatable("entity.awakening_system.wildfire.miniboss_name"));
            this.setCustomNameVisible(true);
        } else {
            this.bossEvent.setName(Component.translatable("entity.awakening_system.wildfire.boss_name"));
            this.bossEvent.setColor(BossEvent.BossBarColor.RED);
            this.bossEvent.setOverlay(BossEvent.BossBarOverlay.NOTCHED_10);
            this.setCustomName(Component.translatable("entity.awakening_system.wildfire.boss_name"));
        }
    }

    public float getShockwaveProgress() {
        return this.entityData.get(SHOCKWAVE_TICKS) / 20.0F;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            return false;
        }

        int shields = getActiveShields();
        if (shields > 0 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            float reduced = amount * 0.30F;
            shieldDamageAccumulator += amount;

            if (shieldDamageAccumulator >= 35.0F) {
                shieldDamageAccumulator = 0.0F;
                int remaining = shields - 1;
                setActiveShields(remaining);

                if (!this.level().isClientSide()) {
                    this.level().playSound(null, this.blockPosition(), ModSounds.WILDFIRE_SHIELD_BREAK.get(), SoundSource.HOSTILE, 1.6F, 1.0F);
                    ((ServerLevel) this.level()).sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.2D, this.getZ(), 25, 0.6D, 0.6D, 0.6D, 0.1D);
                }

                if (remaining == 0) {
                    shieldRegenCooldown = 300;
                }
            }
            return super.hurt(source, reduced);
        }

        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.onGround() && this.getDeltaMovement().y < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }

        if (this.level().isClientSide()) {
            if (this.random.nextInt(3) == 0) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 1.4D;
                double py = this.getY() + 0.5D + this.random.nextDouble() * 1.5D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 1.4D;
                this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0.0D, 0.03D, 0.0D);
            }
        } else {
            if (getActiveShields() < 4) {
                if (shieldRegenCooldown > 0) {
                    shieldRegenCooldown--;
                    if (shieldRegenCooldown <= 0) {
                        setActiveShields(4);
                        this.level().playSound(null, this.blockPosition(), ModSounds.WILDFIRE_SHOCKWAVE.get(), SoundSource.HOSTILE, 1.0F, 1.4F);
                        ((ServerLevel) this.level()).sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0D, this.getZ(), 40, 1.0D, 1.0D, 1.0D, 0.05D);
                    }
                }
            }

            if (!summonedBlazes && this.getHealth() < this.getMaxHealth() * 0.5F) {
                summonedBlazes = true;
                this.level().playSound(null, this.blockPosition(), ModSounds.WILDFIRE_SUMMON_BLAZE.get(), SoundSource.HOSTILE, 1.8F, 1.0F);
                ServerLevel sl = (ServerLevel) this.level();
                for (int i = 0; i < 2; i++) {
                    Blaze blaze = EntityType.BLAZE.create(sl);
                    if (blaze != null) {
                        double ox = (i == 0 ? 2.5D : -2.5D);
                        blaze.moveTo(this.getX() + ox, this.getY(), this.getZ(), this.getYRot(), 0.0F);
                        sl.addFreshEntity(blaze);
                        sl.sendParticles(ParticleTypes.LARGE_SMOKE, blaze.getX(), blaze.getY() + 1.0D, blaze.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.05D);
                    }
                }
            }

            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
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
    protected SoundEvent getAmbientSound() {
        return ModSounds.WILDFIRE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.WILDFIRE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WILDFIRE_DEATH.get();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        if (SystemItems.WILDFIRE_CROWN != null && SystemItems.WILDFIRE_CROWN.isBound()) {
            if (this.random.nextFloat() < 0.65F) {
                this.spawnAtLocation(new ItemStack(SystemItems.WILDFIRE_CROWN.get()));
            } else if (SystemItems.WILDFIRE_CROWN_FRAGMENT != null && SystemItems.WILDFIRE_CROWN_FRAGMENT.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.WILDFIRE_CROWN_FRAGMENT.get(), 2 + this.random.nextInt(3)));
            }
        }

        this.spawnAtLocation(new ItemStack(Items.BLAZE_ROD, 3 + this.random.nextInt(4)));
        this.spawnAtLocation(new ItemStack(Items.MAGMA_CREAM, 2 + this.random.nextInt(3)));
        if (this.random.nextFloat() < 0.50F) {
            this.spawnAtLocation(new ItemStack(Items.NETHERITE_SCRAP, 1 + this.random.nextInt(2)));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ActiveShields", getActiveShields());
        tag.putInt("ShieldRegenCooldown", shieldRegenCooldown);
        tag.putBoolean("SummonedBlazes", summonedBlazes);
        tag.putBoolean("IsMiniBoss", isMiniBoss());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ActiveShields")) {
            setActiveShields(tag.getInt("ActiveShields"));
        }
        if (tag.contains("IsMiniBoss")) {
            setMiniBoss(tag.getBoolean("IsMiniBoss"));
        }
        this.shieldRegenCooldown = tag.getInt("ShieldRegenCooldown");
        this.summonedBlazes = tag.getBoolean("SummonedBlazes");
    }

    static class ShockwaveAttackGoal extends Goal {
        private final WildfireEntity wildfire;
        private int channelTicks = 0;
        private int cooldown = 80;

        public ShockwaveAttackGoal(WildfireEntity wildfire) {
            this.wildfire = wildfire;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = wildfire.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            return wildfire.distanceToSqr(target) <= 36.0D;
        }

        @Override
        public void start() {
            channelTicks = 20;
            wildfire.entityData.set(IS_PERFORMING_SHOCKWAVE, true);
            wildfire.entityData.set(SHOCKWAVE_TICKS, 0);
        }

        @Override
        public void tick() {
            channelTicks--;
            wildfire.entityData.set(SHOCKWAVE_TICKS, 20 - channelTicks);

            if (wildfire.level() instanceof ServerLevel sl) {
                double px = wildfire.getX();
                double py = wildfire.getY() + 0.2D;
                double pz = wildfire.getZ();
                sl.sendParticles(ParticleTypes.SMOKE, px, py, pz, 10, 1.2D, 0.2D, 1.2D, 0.05D);
            }

            if (channelTicks <= 0) {
                if (wildfire.level() instanceof ServerLevel sl) {
                    double px = wildfire.getX();
                    double py = wildfire.getY() + 0.5D;
                    double pz = wildfire.getZ();

                    sl.playSound(null, wildfire.blockPosition(), ModSounds.WILDFIRE_SHOCKWAVE.get(), SoundSource.HOSTILE, 2.0F, 0.9F);
                    sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, px, py, pz, 1, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.FLAME, px, py, pz, 120, 3.5D, 0.5D, 3.5D, 0.25D);
                    sl.sendParticles(ParticleTypes.LAVA, px, py, pz, 30, 2.5D, 0.5D, 2.5D, 0.1D);

                    AABB blastBox = wildfire.getBoundingBox().inflate(6.0D, 3.0D, 6.0D);
                    List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, blastBox, e -> e != wildfire && e.isAlive());
                    DamageSource dmg = wildfire.damageSources().mobAttack(wildfire);

                    for (LivingEntity victim : nearby) {
                        victim.hurt(dmg, 18.0F);
                        victim.setRemainingFireTicks(160);

                        Vec3 knockback = victim.position().subtract(wildfire.position()).normalize().scale(1.8D);
                        victim.setDeltaMovement(knockback.x, 0.6D, knockback.z);
                        victim.hurtMarked = true;
                    }
                }
                cooldown = 140;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return channelTicks > 0;
        }

        @Override
        public void stop() {
            wildfire.entityData.set(IS_PERFORMING_SHOCKWAVE, false);
            wildfire.entityData.set(SHOCKWAVE_TICKS, 0);
        }
    }

    static class FireballBarrageGoal extends Goal {
        private final WildfireEntity wildfire;
        private int attackStep = 0;
        private int attackTime = 0;

        public FireballBarrageGoal(WildfireEntity wildfire) {
            this.wildfire = wildfire;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = wildfire.getTarget();
            return target != null && target.isAlive() && wildfire.canAttack(target);
        }

        @Override
        public void start() {
            this.attackStep = 0;
            this.attackTime = 20;
        }

        @Override
        public void tick() {
            this.attackTime--;
            LivingEntity target = wildfire.getTarget();
            if (target == null) return;

            wildfire.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = wildfire.distanceToSqr(target);

            if (distSq < 16.0D) {
                Vec3 away = wildfire.position().subtract(target.position()).normalize().scale(0.15D);
                wildfire.setDeltaMovement(wildfire.getDeltaMovement().add(away.x, 0.05D, away.z));
            }

            if (this.attackTime <= 0) {
                this.attackStep++;
                if (this.attackStep == 1) {
                    this.attackTime = 60;
                } else if (this.attackStep <= 4) {
                    this.attackTime = 6;
                } else {
                    this.attackTime = 80;
                    this.attackStep = 0;
                }

                if (this.attackStep > 1) {
                    double dx = target.getX() - wildfire.getX();
                    double dy = target.getY(0.5D) - (wildfire.getY() + 1.2D);
                    double dz = target.getZ() - wildfire.getZ();

                    if (!wildfire.isSilent()) {
                        wildfire.level().playSound(null, wildfire.blockPosition(), ModSounds.WILDFIRE_SHOOT.get(), SoundSource.HOSTILE, 1.2F, 1.0F);
                    }

                    SmallFireball fireball = new SmallFireball(wildfire.level(), wildfire, new Vec3(dx, dy, dz).normalize());
                    fireball.setPos(fireball.getX(), wildfire.getY() + 1.4D, fireball.getZ());
                    wildfire.level().addFreshEntity(fireball);
                }
            }
        }
    }
}
