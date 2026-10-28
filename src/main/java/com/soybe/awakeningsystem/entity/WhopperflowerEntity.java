package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Megaflora (Whopperflower) de Genshin Impact.
 * Monstruo mimetizador que se oculta bajo tierra asemejando plantas silvestres.
 * Dispone de variantes Pyro, Cryo y Electro, con ráfagas de proyectiles elementales y carga giratoria.
 */
public class WhopperflowerEntity extends Monster {

    public enum Type {
        PYRO("pyro", "§cPyro", 0xff4422),
        CRYO("cryo", "§bCryo", 0x80ffff),
        ELECTRO("electro", "§dElectro", 0xb366ff);

        private final String id;
        private final String displayName;
        private final int color;

        Type(String id, String displayName, int color) {
            this.id = id;
            this.displayName = displayName;
            this.color = color;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public int getColor() { return color; }

        public static Type byOrdinal(int ordinal) {
            Type[] values = values();
            if (ordinal < 0 || ordinal >= values.length) return PYRO;
            return values[ordinal];
        }
    }

    private static final EntityDataAccessor<Integer> FLOWER_TYPE =
            SynchedEntityData.defineId(WhopperflowerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BURROWED =
            SynchedEntityData.defineId(WhopperflowerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHARGING =
            SynchedEntityData.defineId(WhopperflowerEntity.class, EntityDataSerializers.BOOLEAN);

    private int attackCooldown = 30;
    private int chargeTicks = 0;

    public WhopperflowerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 80;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLOWER_TYPE, 0);
        builder.define(BURROWED, false);
        builder.define(CHARGING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 110.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    public Type getFlowerType() {
        return Type.byOrdinal(this.entityData.get(FLOWER_TYPE));
    }

    public void setFlowerType(Type type) {
        this.entityData.set(FLOWER_TYPE, type.ordinal());
        updateName();
    }

    public boolean isBurrowed() {
        return this.entityData.get(BURROWED);
    }

    public void setBurrowed(boolean burrowed) {
        this.entityData.set(BURROWED, burrowed);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(CHARGING, charging);
    }

    private void updateName() {
        Type t = getFlowerType();
        this.setCustomName(Component.literal(t.getDisplayName() + " §lMegaflora"));
        this.setCustomNameVisible(true);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnGroupData) {
        setFlowerType(Type.byOrdinal(this.random.nextInt(Type.values().length)));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WhopperflowerAttackGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            Type t = getFlowerType();
            if (this.random.nextFloat() < 0.3F) {
                switch (t) {
                    case PYRO -> this.level().addParticle(ParticleTypes.FLAME, getRandomX(0.4D), getY() + 1.2D, getRandomZ(0.4D), 0, 0.03, 0);
                    case CRYO -> this.level().addParticle(ParticleTypes.SNOWFLAKE, getRandomX(0.4D), getY() + 1.2D, getRandomZ(0.4D), 0, -0.02, 0);
                    case ELECTRO -> this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, getRandomX(0.4D), getY() + 1.2D, getRandomZ(0.4D), 0, 0, 0);
                }
            }
        }
    }

    public void shootElementalBurst(LivingEntity target) {
        if (this.level() instanceof ServerLevel sLevel) {
            Vec3 dir = target.getEyePosition().subtract(this.getEyePosition()).normalize();
            Type t = getFlowerType();

            for (int i = -1; i <= 1; i++) {
                Vec3 spreadDir = dir.add((this.random.nextDouble() - 0.5D) * 0.15D, (this.random.nextDouble() - 0.5D) * 0.1D, (this.random.nextDouble() - 0.5D) * 0.15D).normalize();
                SmallFireball ball = new SmallFireball(sLevel, this, spreadDir.scale(0.75D));
                ball.setPos(getX() + dir.x * 0.6D, getY() + 1.2D, getZ() + dir.z * 0.6D);
                sLevel.addFreshEntity(ball);
            }

            sLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.2F, 1.3F);
        }
    }

    public void performSpinMeleeAttack() {
        if (this.level() instanceof ServerLevel sLevel) {
            AABB area = this.getBoundingBox().inflate(2.5D);
            for (LivingEntity entity : sLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                if (entity == this) continue;
                entity.hurt(sLevel.damageSources().mobAttack(this), 16.0F);

                Type t = getFlowerType();
                if (t == Type.PYRO) entity.setRemainingFireTicks(60);
                else if (t == Type.CRYO) entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                else if (t == Type.ELECTRO) entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            }
            sLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.2F, 0.9F);
        }
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sLevel) {
            Player nearestPlayer = sLevel.getNearestPlayer(this, 24.0D);
            if (nearestPlayer instanceof ServerPlayer sp) {
                SystemEvents.data(sp).restoreMana(25);
            }
            if (sLevel.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD)) {
                this.spawnAtLocation(new ItemStack(SystemItems.CRYSTAL_CORE.get(), 1));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("FlowerTypeOrdinal", this.entityData.get(FLOWER_TYPE));
        compound.putBoolean("Burrowed", this.entityData.get(BURROWED));
        compound.putBoolean("Charging", this.entityData.get(CHARGING));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("FlowerTypeOrdinal")) {
            setFlowerType(Type.byOrdinal(compound.getInt("FlowerTypeOrdinal")));
        }
        if (compound.contains("Burrowed")) {
            setBurrowed(compound.getBoolean("Burrowed"));
        }
        if (compound.contains("Charging")) {
            setCharging(compound.getBoolean("Charging"));
        }
    }

    static class WhopperflowerAttackGoal extends Goal {
        private final WhopperflowerEntity flower;
        private int timer = 0;

        public WhopperflowerAttackGoal(WhopperflowerEntity flower) {
            this.flower = flower;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.flower.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.flower.getTarget();
            if (target == null) return;

            this.flower.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = this.flower.distanceToSqr(target);

            if (distSq > 16.0D) {
                this.flower.getNavigation().moveTo(target, 1.15D);
            } else {
                this.flower.getNavigation().stop();
            }

            if (++timer >= 40) {
                timer = 0;
                if (distSq <= 12.0D) {
                    this.flower.performSpinMeleeAttack();
                } else {
                    this.flower.shootElementalBurst(target);
                }
            }
        }
    }
}
