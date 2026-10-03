package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Clase base para los Magos del Abismo (Abyss Mages) de Teyvat.
 * Cuenta con escudos elementales reactivos, teletransporte evasivo, aturdimiento
 * y ritual de regeneración de barrera.
 */
public abstract class AbstractAbyssMageEntity extends Monster {

    public enum MageElement {
        PYRO, HYDRO, CRYO, ELECTRO
    }

    private static final EntityDataAccessor<Boolean> SHIELDED =
            SynchedEntityData.defineId(AbstractAbyssMageEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STUNNED =
            SynchedEntityData.defineId(AbstractAbyssMageEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHANTING =
            SynchedEntityData.defineId(AbstractAbyssMageEntity.class, EntityDataSerializers.BOOLEAN);

    protected float shieldHealth = 50.0F;
    protected final float maxShieldHealth = 50.0F;
    protected int stunTicks = 0;
    protected int chantTicks = 0;
    protected int teleportCooldown = 0;

    protected AbstractAbyssMageEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 20;
    }

    public abstract MageElement getMageElement();

    public abstract SimpleParticleType getElementParticle();

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHIELDED, true);
        builder.define(STUNNED, false);
        builder.define(CHANTING, false);
    }

    public boolean isShielded() {
        return this.entityData.get(SHIELDED);
    }

    public void setShielded(boolean shielded) {
        this.entityData.set(SHIELDED, shielded);
    }

    public boolean isStunned() {
        return this.entityData.get(STUNNED);
    }

    public void setStunned(boolean stunned) {
        this.entityData.set(STUNNED, stunned);
    }

    public boolean isChanting() {
        return this.entityData.get(CHANTING);
    }

    public void setChanting(boolean chanting) {
        this.entityData.set(CHANTING, chanting);
    }

    public static AttributeSupplier.Builder createAbyssMageAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AbyssMageSpellGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.teleportCooldown > 0) this.teleportCooldown--;

        if (this.level().isClientSide) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
            double py = this.getY() + this.random.nextDouble() * 1.5D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;

            if (isShielded()) {
                this.level().addParticle(getElementParticle(), px, py, pz, 0, 0.02, 0);
            }
            if (isChanting()) {
                this.level().addParticle(ParticleTypes.ENCHANT, px, py + 0.5D, pz, (random.nextDouble() - 0.5) * 0.5, 0.2, (random.nextDouble() - 0.5) * 0.5);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        // 1. Mecánica de Aturdimiento tras ruptura de escudo
        if (isStunned()) {
            this.stunTicks--;
            this.getNavigation().stop();
            if (this.stunTicks <= 0) {
                setStunned(false);
                setChanting(true);
                this.chantTicks = 100; // 5 segundos para canalizar la recuperación
                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.6F, 1.4F);
                }
            }
            return;
        }

        // 2. Mecánica de Ritual de Regeneración de Escudo
        if (isChanting()) {
            this.chantTicks--;
            this.getNavigation().stop();
            if (this.chantTicks % 20 == 0 && this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, SoundSource.HOSTILE, 1.4F, 1.3F);
            }
            if (this.chantTicks <= 0) {
                // Regenerar escudo con onda expansiva
                setChanting(false);
                setShielded(true);
                this.shieldHealth = this.maxShieldHealth;

                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 1.2F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 1.4F);
                    sl.sendParticles(getElementParticle(), this.getX(), this.getY() + 1.0D, this.getZ(), 50, 1.5D, 1.0D, 1.5D, 0.15D);

                    List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0D));
                    for (LivingEntity victim : nearby) {
                        if (victim != this) {
                            victim.hurt(this.damageSources().mobAttack(this), 10.0F);
                            Vec3 push = victim.position().subtract(this.position()).normalize().scale(1.2D);
                            victim.setDeltaMovement(push.x, 0.4D, push.z);
                        }
                    }
                }
            }
            return;
        }

        // 3. Flotación suave sobre el terreno mientras tiene escudo
        if (isShielded()) {
            int groundY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.getBlockX(), this.getBlockZ());
            double targetY = groundY + 1.2D;
            if (this.getY() < targetY - 0.2D) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.04D, 0.0D));
            } else if (this.getY() > targetY + 0.3D) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
            }
        }

        // 4. Teletransporte Evasivo si el jugador se acerca demasiado
        LivingEntity target = this.getTarget();
        if (target != null && isShielded() && this.teleportCooldown <= 0 && this.distanceToSqr(target) < 16.0D) {
            teleportEvade(target);
        }
    }

    private void teleportEvade(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        double angle = this.random.nextDouble() * Math.PI * 2.0D;
        double dist = 8.0D + this.random.nextDouble() * 4.0D;
        double newX = target.getX() + Math.cos(angle) * dist;
        double newZ = target.getZ() + Math.sin(angle) * dist;
        int newY = sl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(newX), Mth.floor(newZ));

        sl.sendParticles(getElementParticle(), this.getX(), this.getY() + 1.0D, this.getZ(), 25, 0.5D, 0.8D, 0.5D, 0.05D);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5F, 1.2F);

        this.teleportTo(newX, newY + 0.5D, newZ);

        sl.sendParticles(getElementParticle(), newX, newY + 1.0D, newZ, 25, 0.5D, 0.8D, 0.5D, 0.05D);
        sl.playSound(null, newX, newY, newZ,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5F, 1.2F);

        this.teleportCooldown = 140; // 7 segundos de enfriamiento
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Daño aumentado mientras está pataleando indefenso en el suelo
        if (isStunned()) {
            return super.hurt(source, amount * 2.0F);
        }

        // Si está cantando para recuperar el escudo, 22 de daño cancela el ritual
        if (isChanting()) {
            if (amount >= 20.0F) {
                setChanting(false);
                setStunned(true);
                this.stunTicks = 100;
                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ITEM_BREAK, SoundSource.HOSTILE, 1.8F, 0.6F);
                }
            }
            return super.hurt(source, amount);
        }

        // Daño al escudo elemental
        if (isShielded()) {
            float shieldDamage = calculateElementalShieldDamage(source, amount);
            this.shieldHealth -= shieldDamage;

            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8F, 1.6F);
                sl.sendParticles(getElementParticle(), this.getX(), this.getY() + 1.0D, this.getZ(), 10, 0.4D, 0.4D, 0.4D, 0.05D);
            }

            if (this.shieldHealth <= 0.0F) {
                breakShield();
            }

            // El escudo mitiga el daño directo a la vida en un 80%
            return super.hurt(source, amount * 0.20F);
        }

        return super.hurt(source, amount);
    }

    protected float calculateElementalShieldDamage(DamageSource source, float amount) {
        boolean isFire = source.is(DamageTypeTags.IS_FIRE) || (source.getEntity() != null && source.getEntity().isOnFire());
        boolean isExplosion = source.is(DamageTypeTags.IS_EXPLOSION);
        boolean isLightning = source.is(DamageTypes.LIGHTNING_BOLT);

        switch (getMageElement()) {
            case PYRO -> {
                if (isFire) return 0.0F; // Inmune a fuego
                if (source.is(DamageTypes.DROWN)) return amount * 4.0F;
                return isExplosion ? amount * 1.5F : amount * 0.4F;
            }
            case HYDRO -> {
                if (source.is(DamageTypes.DROWN)) return 0.0F;
                if (isLightning) return amount * 3.5F;
                if (isFire) return amount * 1.8F;
                return amount * 0.4F;
            }
            case CRYO -> {
                if (source.is(DamageTypes.FREEZE)) return 0.0F;
                if (isFire) return amount * 3.5F; // El fuego pulveriza el escudo Cryo
                return amount * 0.4F;
            }
            case ELECTRO -> {
                if (isLightning) return 0.0F;
                if (isFire) return amount * 2.5F; // Reacción de Sobrecarga
                return amount * 0.4F;
            }
            default -> {
                return amount * 0.5F;
            }
        }
    }

    private void breakShield() {
        setShielded(false);
        setStunned(true);
        this.stunTicks = 160; // 8 segundos aturdido

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0F, 0.7F);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 2.0F, 0.8F);
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0D, this.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
            sl.sendParticles(getElementParticle(), this.getX(), this.getY() + 1.0D, this.getZ(), 40, 0.8D, 0.8D, 0.8D, 0.1D);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        // Drops canónicos de Ramas y Brotes de Líneas Ley de Genshin Impact
        if (SystemItems.DEAD_LEY_LINE_BRANCH.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.DEAD_LEY_LINE_BRANCH.get(), 1 + this.random.nextInt(3)));
        }
        if (SystemItems.DEAD_LEY_LINE_LEAVES.isBound() && this.random.nextFloat() < 0.70F) {
            this.spawnAtLocation(new ItemStack(SystemItems.DEAD_LEY_LINE_LEAVES.get(), 1 + this.random.nextInt(2)));
        }
        if (SystemItems.LEY_LINE_SPROUT.isBound() && this.random.nextFloat() < 0.35F) {
            this.spawnAtLocation(new ItemStack(SystemItems.LEY_LINE_SPROUT.get(), 1));
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITCH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITCH_DEATH;
    }

    public abstract void performElementalAttack(LivingEntity target);

    // === IA: LANZAMIENTO DE HECHIZOS ELEMENTALES ===
    static class AbyssMageSpellGoal extends Goal {
        private final AbstractAbyssMageEntity mage;
        private int attackTimer = 0;

        public AbyssMageSpellGoal(AbstractAbyssMageEntity mage) {
            this.mage = mage;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mage.getTarget();
            return target != null && target.isAlive() && !this.mage.isStunned() && !this.mage.isChanting();
        }

        @Override
        public void start() {
            this.attackTimer = 20;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mage.getTarget();
            if (target == null) return;

            this.mage.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.attackTimer--;

            double distSqr = this.mage.distanceToSqr(target);
            if (distSqr > 49.0D) {
                this.mage.getNavigation().moveTo(target, 1.0D);
            } else if (distSqr < 16.0D) {
                // Retroceder un poco
                Vec3 away = this.mage.position().subtract(target.position()).normalize().scale(1.5D);
                this.mage.getNavigation().moveTo(this.mage.getX() + away.x, this.mage.getY(), this.mage.getZ() + away.z, 1.1D);
            } else {
                this.mage.getNavigation().stop();
            }

            if (this.attackTimer <= 0) {
                this.mage.performElementalAttack(target);
                this.attackTimer = 50 + this.mage.getRandom().nextInt(30);
            }
        }
    }
}
