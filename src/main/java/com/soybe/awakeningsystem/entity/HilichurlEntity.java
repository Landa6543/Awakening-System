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
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Hilichurl de Genshin Impact:
 * Monstruo tribal canónico con máscara de hueso blanca con runas rojas, melena de pelo salvaje
 * y 3 roles de combate: Berserker/Luchador, Guardia con escudo de madera y Tirador con ballesta.
 */
public class HilichurlEntity extends Monster implements RangedAttackMob {

    public enum HilichurlRole {
        BERSERKER(0, "berserker", "§6Hilichurl Berserker"),
        GUARD(1, "guard", "§eWooden Shield Hilichurl"),
        SHOOTER(2, "shooter", "§bHilichurl Shooter"),
        FIGHTER(3, "fighter", "§eHilichurl Fighter");

        private final int id;
        private final String name;
        private final String displayName;

        HilichurlRole(int id, String name, String displayName) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }

        public static HilichurlRole byId(int id) {
            for (HilichurlRole r : values()) {
                if (r.id == id) return r;
            }
            return BERSERKER;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ROLE =
            SynchedEntityData.defineId(HilichurlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_BLOCKING =
            SynchedEntityData.defineId(HilichurlEntity.class, EntityDataSerializers.BOOLEAN);

    private int blockCooldown = 0;

    public HilichurlEntity(EntityType<? extends HilichurlEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ROLE, HilichurlRole.BERSERKER.getId());
        builder.define(DATA_BLOCKING, false);
    }

    public HilichurlRole getRole() {
        return HilichurlRole.byId(this.entityData.get(DATA_ROLE));
    }

    public void setRole(HilichurlRole role) {
        this.entityData.set(DATA_ROLE, role.getId());
        this.setCustomName(Component.literal(role.getDisplayName()));
        this.setCustomNameVisible(true);
        this.updateRoleAttributes(role);
    }

    public boolean isBlocking() {
        return this.entityData.get(DATA_BLOCKING);
    }

    public void setBlocking(boolean blocking) {
        this.entityData.set(DATA_BLOCKING, blocking);
    }

    private void updateRoleAttributes(HilichurlRole role) {
        if (role == HilichurlRole.BERSERKER) {
            var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(0.32D);
            var dmg = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null) dmg.setBaseValue(9.0D);
        } else if (role == HilichurlRole.GUARD) {
            var armor = this.getAttribute(Attributes.ARMOR);
            if (armor != null) armor.setBaseValue(8.0D);
            var kb = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kb != null) kb.setBaseValue(0.5D);
        } else if (role == HilichurlRole.SHOOTER) {
            var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(0.26D);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));

        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 30, 15.0F) {
            @Override
            public boolean canUse() {
                return HilichurlEntity.this.getRole() == HilichurlRole.SHOOTER && super.canUse();
            }
        });

        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.15D, false) {
            @Override
            public boolean canUse() {
                return HilichurlEntity.this.getRole() != HilichurlRole.SHOOTER && super.canUse();
            }
        });

        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LeyLineMonolithEntity.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.IronGolem.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (getRole() == HilichurlRole.GUARD && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD)) {
            Entity attacker = source.getEntity();
            if (attacker != null) {
                double dx = attacker.getX() - this.getX();
                double dz = attacker.getZ() - this.getZ();
                float angle = (float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90F;
                float diff = Mth.wrapDegrees(angle - this.getYRot());

                if (Math.abs(diff) < 100.0F) {
                    setBlocking(true);
                    this.blockCooldown = 15;
                    this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.2F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0D, this.getZ(),
                                8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                    if (attacker instanceof LivingEntity living) {
                        living.knockback(0.4D, -Math.sin(Math.toRadians(this.getYRot())), Math.cos(Math.toRadians(this.getYRot())));
                    }
                    return super.hurt(source, amount * 0.2F);
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.blockCooldown > 0) {
            this.blockCooldown--;
            if (this.blockCooldown == 0) {
                setBlocking(false);
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        Arrow arrow = new Arrow(this.level(), this, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW), null);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + dist * 0.15D, dz, 1.6F, (float) (14 - this.level().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.2F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && this.random.nextFloat() < 0.45F) {
            this.playSound(ModSounds.HILICHURL_ATTACK.get(), 1.0F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.15F);
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HILICHURL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.HILICHURL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HILICHURL_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Role", getRole().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Role")) {
            setRole(HilichurlRole.byId(tag.getInt("Role")));
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        int r = this.random.nextInt(3);
        setRole(HilichurlRole.byId(r));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }
}
