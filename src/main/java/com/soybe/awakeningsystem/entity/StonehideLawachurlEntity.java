package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Stonehide Lawachurl (Cacique Mitachurl de Roca Geo).
 * Jefe de campo colosal hilichurl imbuido con el elemento Geo,
 * dotado de un escudo pétreo endurecido y saltos de impacto sísmico devastadores.
 */
public class StonehideLawachurlEntity extends Monster {

    private static final EntityDataAccessor<Boolean> LEAPING =
            SynchedEntityData.defineId(StonehideLawachurlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_GEO_SHIELD =
            SynchedEntityData.defineId(StonehideLawachurlEntity.class, EntityDataSerializers.BOOLEAN);

    private float geoShieldHealth = 120.0F;
    private int leapCooldown = 180;

    public StonehideLawachurlEntity(EntityType<? extends StonehideLawachurlEntity> type, Level level) {
        super(type, level);
        this.xpReward = 55;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 320.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LEAPING, false);
        builder.define(HAS_GEO_SHIELD, true);
    }

    public boolean isLeaping() {
        return this.entityData.get(LEAPING);
    }

    public void setLeaping(boolean leaping) {
        this.entityData.set(LEAPING, leaping);
    }

    public boolean hasGeoShield() {
        return this.entityData.get(HAS_GEO_SHIELD);
    }

    public void setHasGeoShield(boolean has) {
        this.entityData.set(HAS_GEO_SHIELD, has);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LawachurlSlamGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.leapCooldown > 0) this.leapCooldown--;

        if (this.level().isClientSide && hasGeoShield() && this.random.nextFloat() < 0.25F) {
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.RAW_GOLD_BLOCK.defaultBlockState()),
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.5D,
                    this.getY() + this.random.nextDouble() * 2.5D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.5D,
                    0, 0.05, 0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (hasGeoShield()) {
            this.geoShieldHealth -= amount;
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.0F, 1.6F);
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DRIPSTONE_BLOCK.defaultBlockState()),
                        this.getX(), this.getY() + 1.5D, this.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
            }

            if (this.geoShieldHealth <= 0.0F) {
                setHasGeoShield(false);
                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.8F, 0.8F);
                    sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.5D, this.getZ(), 1, 0, 0, 0, 0);
                }
            }

            // El escudo absorbe el 60% del daño
            return super.hurt(source, amount * 0.40F);
        }

        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        // Drops de cuernos y materiales de Lawachurl con física nativa
        if (SystemItems.HEAVY_HORN.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.HEAVY_HORN.get(), 2 + this.random.nextInt(3)));
        }
        if (SystemItems.BLACK_BRONZE_HORN.isBound() && this.random.nextFloat() < 0.65F) {
            this.spawnAtLocation(new ItemStack(SystemItems.BLACK_BRONZE_HORN.get(), 1 + this.random.nextInt(2)));
        }
        if (SystemItems.BLACK_CRYSTAL_HORN.isBound() && this.random.nextFloat() < 0.30F) {
            this.spawnAtLocation(new ItemStack(SystemItems.BLACK_CRYSTAL_HORN.get(), 1));
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.4F, 0.9F);

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (Player p : sl.getPlayers(pl -> pl.distanceToSqr(this) < 900.0D)) {
                if (p instanceof ServerPlayer sp) {
                    var data = SystemEvents.data(sp);
                    if (isTeyvat) {
                        int moraGain = 5000 + sl.random.nextInt(5000);
                        int primoGain = 12 + sl.random.nextInt(10);
                        data.addMora(moraGain);
                        data.addPrimogems(primoGain);
                        com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                        sp.displayClientMessage(Component.literal("§6§l✦ ¡STONEHIDE LAWACHURL DERROTADO! §e+" + moraGain + " Mora, §b+" + primoGain + " Protogemas§e."), false);
                    } else {
                        if (SystemItems.AWAKENING_COIN.isBound()) {
                            sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 6));
                        }
                    }
                }
            }
        }
    }

    // === IA: IMPACTO SÍSMICO Y SALTO EN PICADO ===
    static class LawachurlSlamGoal extends Goal {
        private final StonehideLawachurlEntity lawachurl;
        private int leapTicks = 0;

        public LawachurlSlamGoal(StonehideLawachurlEntity lawachurl) {
            this.lawachurl = lawachurl;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.lawachurl.getTarget();
            return target != null && target.isAlive() && this.lawachurl.leapCooldown <= 0
                    && this.lawachurl.distanceToSqr(target) > 25.0D && this.lawachurl.distanceToSqr(target) < 225.0D
                    && this.lawachurl.onGround();
        }

        @Override
        public void start() {
            this.lawachurl.setLeaping(true);
            this.leapTicks = 40;
            this.lawachurl.leapCooldown = 220;

            LivingEntity target = this.lawachurl.getTarget();
            if (target != null) {
                Vec3 dir = target.position().subtract(this.lawachurl.position()).normalize();
                this.lawachurl.setDeltaMovement(dir.x * 1.1D, 0.95D, dir.z * 1.1D);
                this.lawachurl.hasImpulse = true;
            }

            this.lawachurl.level().playSound(null, this.lawachurl.getX(), this.lawachurl.getY(), this.lawachurl.getZ(),
                    SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.8F, 0.7F);
        }

        @Override
        public void tick() {
            this.leapTicks--;

            // Al tocar el suelo tras haber estado en el aire
            if (this.leapTicks < 30 && this.lawachurl.onGround() && !this.lawachurl.level().isClientSide) {
                ServerLevel sl = (ServerLevel) this.lawachurl.level();
                sl.playSound(null, this.lawachurl.getX(), this.lawachurl.getY(), this.lawachurl.getZ(),
                        SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.6F);
                sl.sendParticles(ParticleTypes.EXPLOSION, this.lawachurl.getX(), this.lawachurl.getY(), this.lawachurl.getZ(), 1, 0, 0, 0, 0);
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        this.lawachurl.getX(), this.lawachurl.getY() + 0.5D, this.lawachurl.getZ(), 40, 2.0, 0.4, 2.0, 0.2);

                List<LivingEntity> victims = sl.getEntitiesOfClass(LivingEntity.class, this.lawachurl.getBoundingBox().inflate(4.0D));
                for (LivingEntity v : victims) {
                    if (v != this.lawachurl) {
                        v.hurt(this.lawachurl.damageSources().mobAttack(this.lawachurl), 22.0F);
                        v.setDeltaMovement(0.0D, 0.6D, 0.0D);
                    }
                }
                this.leapTicks = 0;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.leapTicks > 0;
        }

        @Override
        public void stop() {
            this.lawachurl.setLeaping(false);
        }
    }
}
