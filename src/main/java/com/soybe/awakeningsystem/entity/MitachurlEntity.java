package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Mitachurl de Genshin Impact:
 * Cacique Hilichurl de gran envergadura física con máscara ósea astada y cuernos masivos curvados.
 * Variantes de combate:
 * - Gran Hacha Ardiente: Ataque contundente de fuego y salto con onda de choque.
 * - Muro de Escudos: Escudo torre colosal que absorbe impactos frontales y embiste al jugador.
 */
public class MitachurlEntity extends Monster {

    public enum MitachurlType {
        BLAZING_AXE(0, "blazing_axe", "§4§lMitachurl con Gran Hacha Ardiente"),
        SHIELDWALL(1, "shieldwall", "§6§lMitachurl con Escudo Colosal");

        private final int id;
        private final String name;
        private final String displayName;

        MitachurlType(int id, String name, String displayName) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }

        public static MitachurlType byId(int id) {
            for (MitachurlType t : values()) {
                if (t.id == id) return t;
            }
            return BLAZING_AXE;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_TYPE =
            SynchedEntityData.defineId(MitachurlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(MitachurlEntity.class, EntityDataSerializers.BOOLEAN);

    private int specialAttackCooldown = 60;
    private int chargeTicks = 0;

    public MitachurlEntity(EntityType<? extends MitachurlEntity> type, Level level) {
        super(type, level);
        this.xpReward = 35;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.70D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, MitachurlType.BLAZING_AXE.getId());
        builder.define(DATA_CHARGING, false);
    }

    public MitachurlType getMitachurlType() {
        return MitachurlType.byId(this.entityData.get(DATA_TYPE));
    }

    public void setMitachurlType(MitachurlType type) {
        this.entityData.set(DATA_TYPE, type.getId());
        this.setCustomName(Component.literal(type.getDisplayName()));
        this.setCustomNameVisible(true);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(DATA_CHARGING, charging);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LeyLineMonolithEntity.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.IronGolem.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Variante Escudo Colosal: bloquea el 85% de impactos frontales
        if (getMitachurlType() == MitachurlType.SHIELDWALL && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD)) {
            Entity attacker = source.getEntity();
            if (attacker != null) {
                double dx = attacker.getX() - this.getX();
                double dz = attacker.getZ() - this.getZ();
                float angle = (float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90F;
                float diff = Mth.wrapDegrees(angle - this.getYRot());

                if (Math.abs(diff) < 110.0F) {
                    this.playSound(SoundEvents.SHIELD_BLOCK, 1.2F, 0.9F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.2D, this.getZ(),
                                12, 0.4D, 0.4D, 0.4D, 0.08D);
                    }
                    return super.hurt(source, amount * 0.15F);
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.specialAttackCooldown > 0) {
            this.specialAttackCooldown--;
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.level().isClientSide) {
            double distSqr = this.distanceToSqr(target);

            // Ataque especial cada ~5-7 segundos
            if (this.specialAttackCooldown <= 0) {
                if (getMitachurlType() == MitachurlType.BLAZING_AXE && distSqr < 25.0D) {
                    performAxeGroundSlam(target);
                } else if (getMitachurlType() == MitachurlType.SHIELDWALL && distSqr > 16.0D && distSqr < 144.0D) {
                    startShieldCharge(target);
                }
            }

            // Lógica de embestida con escudo activa
            if (this.chargeTicks > 0) {
                this.chargeTicks--;
                Vec3 forward = Vec3.directionFromRotation(0, this.getYRot()).scale(0.55D);
                this.setDeltaMovement(forward.x, this.getDeltaMovement().y, forward.z);

                // Colisión con el objetivo o entidades cercanas
                AABB box = this.getBoundingBox().inflate(0.5D);
                List<LivingEntity> hit = this.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != this);
                for (LivingEntity e : hit) {
                    e.hurt(this.damageSources().mobAttack(this), 12.0F);
                    e.knockback(1.8D, -forward.x, -forward.z);
                    this.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.2F, 0.8F);
                    this.chargeTicks = 0;
                    setCharging(false);
                    break;
                }
                if (this.chargeTicks == 0) {
                    setCharging(false);
                }
            }
        }
    }

    private void performAxeGroundSlam(LivingEntity target) {
        this.specialAttackCooldown = 120;
        this.setDeltaMovement(this.getDeltaMovement().add(0, 0.45D, 0));
        this.playSound(ModSounds.MITACHURL_AXE_SLAM.get(), 1.2F, 1.0F);

        // Al tocar tierra genera onda de choque ígnea
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.5D, target.getZ(), 2, 0.5D, 0.5D, 0.5D, 0.0D);
            sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 0.2D, target.getZ(), 40, 1.5D, 0.2D, 1.5D, 0.1D);
            target.igniteForSeconds(5);
            target.hurt(this.damageSources().mobAttack(this), 16.0F);
        }
    }

    private void startShieldCharge(LivingEntity target) {
        this.specialAttackCooldown = 140;
        this.chargeTicks = 30; // 1.5s de carga
        setCharging(true);
        this.lookAt(target, 30.0F, 30.0F);
        this.playSound(ModSounds.MITACHURL_SHIELD_CHARGE.get(), 1.2F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MITACHURL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.MITACHURL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MITACHURL_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Type", getMitachurlType().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Type")) {
            setMitachurlType(MitachurlType.byId(tag.getInt("Type")));
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        boolean axe = this.random.nextBoolean();
        setMitachurlType(axe ? MitachurlType.BLAZING_AXE : MitachurlType.SHIELDWALL);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }
}
