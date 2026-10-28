package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Ojo Vigilante del Abismo (Abyssal Beholder / Estilo Ojo de Cthulhu).
 * Ojo colosal volador con vuelo 3D acrobático en el cielo, embestidas en picada,
 * pulsos de distorsión y Rayo Anulador del Abismo.
 */
public class AbyssalBeholderEntity extends Monster {

    private static final EntityDataAccessor<Boolean> CHANNELING_BEAM =
            SynchedEntityData.defineId(AbyssalBeholderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHARGING =
            SynchedEntityData.defineId(AbyssalBeholderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TARGET_ENTITY_ID =
            SynchedEntityData.defineId(AbyssalBeholderEntity.class, EntityDataSerializers.INT);

    private int beamCooldown = 50;
    private int chargeCooldown = 80;
    private int pulseCooldown = 60;
    private LivingEntity beamTarget;

    public AbyssalBeholderEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new CthulhuEyeFlightMoveControl(this);
        this.setNoGravity(true);
        this.xpReward = 85;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 130.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FLYING_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHANNELING_BEAM, false);
        builder.define(CHARGING, false);
        builder.define(TARGET_ENTITY_ID, -1);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new GravityPulseGoal(this));
        this.goalSelector.addGoal(2, new CthulhuSwoopChargeGoal(this));
        this.goalSelector.addGoal(3, new AbyssalNullRayGoal(this));
        this.goalSelector.addGoal(4, new CthulhuAerialOrbitGoal(this));
        this.goalSelector.addGoal(6, new CthulhuSkyWanderGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isChannelingBeam() {
        return this.entityData.get(CHANNELING_BEAM);
    }

    public void setChannelingBeam(boolean channeling) {
        this.entityData.set(CHANNELING_BEAM, channeling);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(CHARGING, charging);
    }

    @Nullable
    public LivingEntity getBeamTarget() {
        int id = this.entityData.get(TARGET_ENTITY_ID);
        if (id <= 0) return null;
        Entity entity = this.level().getEntity(id);
        return (entity instanceof LivingEntity living) ? living : null;
    }

    public void setBeamTarget(@Nullable LivingEntity target) {
        this.beamTarget = target;
        this.entityData.set(TARGET_ENTITY_ID, target != null ? target.getId() : -1);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        // Sensor de techo: Si hay un bloque solido encima, desciende para no atorarse en techos
        if (!this.level().isClientSide() && this.tickCount % 4 == 0) {
            BlockPos above = this.blockPosition().above(2);
            if (!this.level().isEmptyBlock(above)) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.15D, this.getDeltaMovement().z);
            }
        }

        if (this.level().isClientSide()) {
            if (this.random.nextFloat() < 0.45F || this.isCharging()) {
                Vec3 back = this.getLookAngle().reverse();
                double px = this.getX() + back.x * 1.5D + (this.random.nextDouble() - 0.5D) * 0.8D;
                double py = this.getY() + 1.1D + back.y * 1.5D + (this.random.nextDouble() - 0.5D) * 0.8D;
                double pz = this.getZ() + back.z * 1.5D + (this.random.nextDouble() - 0.5D) * 0.8D;
                this.level().addParticle(ParticleTypes.CRIMSON_SPORE, px, py, pz,
                        back.x * 0.1D, back.y * 0.1D, back.z * 0.1D);
                if (this.isCharging()) {
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0, 0);
                }
            }

            if (this.isChannelingBeam()) {
                Vec3 look = this.getLookAngle();
                double fx = this.getX() + look.x * 1.4D;
                double fy = this.getY() + 1.1D + look.y * 1.4D;
                double fz = this.getZ() + look.z * 1.4D;
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ParticleTypes.REVERSE_PORTAL, fx, fy, fz,
                            -look.x * 0.4D + (this.random.nextDouble() - 0.5D) * 0.2D,
                            -look.y * 0.4D + (this.random.nextDouble() - 0.5D) * 0.2D,
                            -look.z * 0.4D + (this.random.nextDouble() - 0.5D) * 0.2D);
                }
            }
        } else {
            if (this.beamCooldown > 0) this.beamCooldown--;
            if (this.chargeCooldown > 0) this.chargeCooldown--;
            if (this.pulseCooldown > 0) this.pulseCooldown--;
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel sl) {
            Entity killer = source.getEntity();
            if (killer instanceof Player p) {
                com.soybe.awakeningsystem.SystemData kData = p.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                if (kData != null && ((kData.hasHunterClass() && "shadow_monarch".equals(kData.hunterClass())) || kData.skills().containsKey("unique_absolute_extraction"))) {
                    var soul = new ShadowSoulEntity(sl, this.getX(), this.getY(), this.getZ(), this, p.getUUID());
                    sl.addFreshEntity(soul);
                }
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ELDER_GUARDIAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ELDER_GUARDIAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ELDER_GUARDIAN_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.2F;
    }

    static class CthulhuEyeFlightMoveControl extends MoveControl {
        private final AbyssalBeholderEntity beholder;
        private int courseChangeCooldown;

        public CthulhuEyeFlightMoveControl(AbyssalBeholderEntity beholder) {
            super(beholder);
            this.beholder = beholder;
        }

        @Override
        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                if (this.courseChangeCooldown-- <= 0) {
                    this.courseChangeCooldown += this.beholder.getRandom().nextInt(4) + 2;
                    Vec3 dir = new Vec3(this.wantedX - this.beholder.getX(),
                            this.wantedY - this.beholder.getY(),
                            this.wantedZ - this.beholder.getZ());
                    double dist = dir.length();
                    if (dist < 0.8D) {
                        this.beholder.setDeltaMovement(this.beholder.getDeltaMovement().scale(0.8D));
                        return;
                    }
                    dir = dir.normalize();
                    double speed = this.speedModifier * this.beholder.getAttributeValue(Attributes.FLYING_SPEED);
                    if (this.beholder.isCharging()) {
                        speed *= 1.8D;
                    }

                    Vec3 curMove = this.beholder.getDeltaMovement();
                    this.beholder.setDeltaMovement(curMove.add(dir.scale(speed * 0.35D)).scale(0.92D));

                    float targetYaw = (float)(Mth.atan2(dir.z, dir.x) * (180F / (float)Math.PI)) - 90.0F;
                    this.beholder.setYRot(this.rotlerp(this.beholder.getYRot(), targetYaw, 18.0F));
                    this.beholder.yBodyRot = this.beholder.getYRot();
                }
            }
        }
    }

    static class CthulhuSwoopChargeGoal extends Goal {
        private final AbyssalBeholderEntity beholder;
        private LivingEntity target;
        private int chargeTicks = 0;
        private Vec3 chargeDirection = Vec3.ZERO;

        public CthulhuSwoopChargeGoal(AbyssalBeholderEntity beholder) {
            this.beholder = beholder;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.beholder.chargeCooldown > 0 || this.beholder.isChannelingBeam()) return false;
            this.target = this.beholder.getTarget();
            if (this.target == null || !this.target.isAlive()) return false;
            double distSq = this.beholder.distanceToSqr(this.target);
            return distSq >= 36.0D && distSq <= 900.0D && this.beholder.hasLineOfSight(this.target);
        }

        @Override
        public void start() {
            this.chargeTicks = 0;
            this.beholder.setCharging(true);
            this.beholder.playSound(ModSounds.ABYSSAL_BEHOLDER_ROAR.get(), 1.6F, 1.0F);

            Vec3 start = this.beholder.position();
            Vec3 dest = this.target.position().add(0, this.target.getBbHeight() * 0.5D, 0);
            this.chargeDirection = dest.subtract(start).normalize();
        }

        @Override
        public void stop() {
            this.beholder.setCharging(false);
            this.beholder.chargeCooldown = 110 + this.beholder.getRandom().nextInt(40);
            this.chargeTicks = 0;
        }

        @Override
        public void tick() {
            if (this.target == null || !this.target.isAlive()) {
                this.stop();
                return;
            }

            this.chargeTicks++;

            if (this.chargeTicks <= 12) {
                Vec3 back = this.chargeDirection.reverse().scale(0.35D);
                this.beholder.setDeltaMovement(back.x, back.y + 0.1D, back.z);
                this.beholder.getLookControl().setLookAt(this.target, 40.0F, 40.0F);
            } else if (this.chargeTicks <= 36) {
                double swoopSpeed = 1.35D;
                this.beholder.setDeltaMovement(
                        this.chargeDirection.x * swoopSpeed,
                        this.chargeDirection.y * swoopSpeed,
                        this.chargeDirection.z * swoopSpeed
                );

                AABB hitBox = this.beholder.getBoundingBox().inflate(1.2D);
                for (LivingEntity victim : this.beholder.level().getEntitiesOfClass(LivingEntity.class, hitBox)) {
                    if (victim != this.beholder && victim.isAlive() && !victim.isAlliedTo(this.beholder)) {
                        victim.hurt(this.beholder.damageSources().mobAttack(this.beholder), 16.0F);
                        victim.setDeltaMovement(this.chargeDirection.x * 1.6D, 0.6D, this.chargeDirection.z * 1.6D);
                        victim.hasImpulse = true;
                        this.beholder.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.4F, 0.7F);
                        this.beholder.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.8F, 1.6F);
                    }
                }
            } else {
                this.beholder.setDeltaMovement(this.beholder.getDeltaMovement().scale(0.85D));
                if (this.chargeTicks >= 45) {
                    this.stop();
                }
            }
        }
    }

    static class CthulhuAerialOrbitGoal extends Goal {
        private final AbyssalBeholderEntity beholder;

        public CthulhuAerialOrbitGoal(AbyssalBeholderEntity beholder) {
            this.beholder = beholder;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.beholder.getTarget();
            return target != null && target.isAlive() && !this.beholder.isCharging() && !this.beholder.isChannelingBeam();
        }

        @Override
        public void tick() {
            LivingEntity target = this.beholder.getTarget();
            if (target == null) return;

            this.beholder.getLookControl().setLookAt(target, 25.0F, 25.0F);

            double targetSkyY = target.getY() + 1.8D + Math.sin(this.beholder.tickCount * 0.08D) * 0.7D;

            double angle = (this.beholder.tickCount * 0.045D);
            double orbitRadius = 4.5D;
            double ox = Math.cos(angle) * orbitRadius;
            double oz = Math.sin(angle) * orbitRadius;

            this.beholder.getMoveControl().setWantedPosition(
                    target.getX() + ox,
                    targetSkyY,
                    target.getZ() + oz,
                    1.1D
            );
        }
    }

    static class CthulhuSkyWanderGoal extends Goal {
        private final AbyssalBeholderEntity beholder;

        public CthulhuSkyWanderGoal(AbyssalBeholderEntity beholder) {
            this.beholder = beholder;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl mc = this.beholder.getMoveControl();
            if (!mc.hasWanted()) return true;
            double dx = mc.getWantedX() - this.beholder.getX();
            double dy = mc.getWantedY() - this.beholder.getY();
            double dz = mc.getWantedZ() - this.beholder.getZ();
            double d = dx * dx + dy * dy + dz * dz;
            return d < 2.0D || d > 4900.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource rand = this.beholder.getRandom();
            double tx = this.beholder.getX() + (rand.nextDouble() * 2.0D - 1.0D) * 16.0D;
            double tz = this.beholder.getZ() + (rand.nextDouble() * 2.0D - 1.0D) * 16.0D;

            BlockPos ground = BlockPos.containing(tx, this.beholder.getY(), tz);
            while (ground.getY() > this.beholder.level().getMinBuildHeight() && this.beholder.level().isEmptyBlock(ground)) {
                ground = ground.below();
            }
            double skyY = ground.getY() + 2.2D + rand.nextDouble() * 1.5D;

            this.beholder.getMoveControl().setWantedPosition(tx, skyY, tz, 0.95D);
        }
    }

    static class GravityPulseGoal extends Goal {
        private final AbyssalBeholderEntity beholder;

        public GravityPulseGoal(AbyssalBeholderEntity beholder) {
            this.beholder = beholder;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.beholder.pulseCooldown > 0 || this.beholder.isCharging()) return false;
            LivingEntity target = this.beholder.getTarget();
            if (target == null || !target.isAlive()) return false;
            return this.beholder.distanceToSqr(target) <= 25.0D;
        }

        @Override
        public void start() {
            this.beholder.pulseCooldown = 80 + this.beholder.getRandom().nextInt(30);
            LivingEntity target = this.beholder.getTarget();
            if (target == null) return;

            this.beholder.playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.2F, 1.3F);

            AABB box = this.beholder.getBoundingBox().inflate(5.0D);
            for (LivingEntity nearby : this.beholder.level().getEntitiesOfClass(LivingEntity.class, box)) {
                if (nearby != this.beholder && nearby.isAlive() && !nearby.isAlliedTo(this.beholder)) {
                    nearby.hurt(this.beholder.damageSources().mobAttack(this.beholder), 10.0F);
                    Vec3 away = nearby.position().subtract(this.beholder.position()).normalize();
                    nearby.setDeltaMovement(away.x * 1.5D, 0.5D, away.z * 1.5D);
                    nearby.hasImpulse = true;
                }
            }

            if (this.beholder.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SQUID_INK, this.beholder.getX(), this.beholder.getY() + 1.0D, this.beholder.getZ(),
                        40, 1.5D, 1.0D, 1.5D, 0.2D);
                sl.sendParticles(ParticleTypes.WITCH, this.beholder.getX(), this.beholder.getY() + 1.0D, this.beholder.getZ(),
                        25, 1.2D, 0.8D, 1.2D, 0.1D);
            }
        }
    }

    static class AbyssalNullRayGoal extends Goal {
        private final AbyssalBeholderEntity beholder;
        private int chargeTicks;

        public AbyssalNullRayGoal(AbyssalBeholderEntity beholder) {
            this.beholder = beholder;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.beholder.beamCooldown > 0 || this.beholder.isCharging()) return false;
            LivingEntity target = this.beholder.getTarget();
            if (target == null || !target.isAlive()) return false;
            double distSq = this.beholder.distanceToSqr(target);
            return distSq >= 25.0D && distSq <= 750.0D && this.beholder.hasLineOfSight(target);
        }

        @Override
        public void start() {
            this.chargeTicks = 0;
            this.beholder.setChannelingBeam(true);
            this.beholder.setBeamTarget(this.beholder.getTarget());
        }

        @Override
        public void stop() {
            this.beholder.setChannelingBeam(false);
            this.beholder.setBeamTarget(null);
            this.chargeTicks = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.beholder.getTarget();
            if (target == null || !target.isAlive()) {
                this.stop();
                return;
            }

            this.beholder.getLookControl().setLookAt(target, 35.0F, 35.0F);
            this.chargeTicks++;

            if (this.chargeTicks == 1) {
                this.beholder.playSound(ModSounds.ABYSSAL_BEHOLDER_BEAM.get(), 1.2F, 1.0F);
            }

            if (this.chargeTicks >= 26) {
                this.fireBeam(target);
                this.beholder.beamCooldown = 70 + this.beholder.getRandom().nextInt(30);
                this.stop();
            }
        }

        private void fireBeam(LivingEntity target) {
            if (target == null || !target.isAlive()) return;

            target.hurt(this.beholder.damageSources().mobAttack(this.beholder), 14.0F);
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 90, 0, false, true));
            target.addTag("abyssal_silenced");

            if (this.beholder.level() instanceof ServerLevel sl) {
                Vec3 start = new Vec3(this.beholder.getX(), this.beholder.getY() + 1.1D, this.beholder.getZ());
                Vec3 end = new Vec3(target.getX(), target.getY() + target.getEyeHeight() * 0.6D, target.getZ());
                Vec3 ray = end.subtract(start);
                double steps = Math.max(12, (int)(ray.length() * 4));

                for (int i = 0; i < steps; i++) {
                    double progress = i / steps;
                    Vec3 pt = start.add(ray.scale(progress));
                    sl.sendParticles(ParticleTypes.WITCH, pt.x, pt.y, pt.z, 2, 0.05D, 0.05D, 0.05D, 0.01D);
                    if (i % 3 == 0) {
                        sl.sendParticles(ParticleTypes.PORTAL, pt.x, pt.y, pt.z, 1, 0.03D, 0.03D, 0.03D, 0.02D);
                    }
                }
                sl.sendParticles(ParticleTypes.SONIC_BOOM, end.x, end.y, end.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
