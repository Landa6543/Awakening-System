package com.soybe.awakeningsystem.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * Ángel Ofanim del Limbo (Biblical Ophanim / Throne Eye Angel of Limbo):
 * Entidad celestial misteriosa compuesta por tres anillos concéntricos giroscópicos
 * (blancos, negros y dorados) que rotan en direcciones perpendiculares alrededor de un Ojo central.
 * Levita pacíficamente en el aire; si se siente amenazado o detecta intrusos, acelera sus aros
 * a hipervelocidad y manifiesta un Orbe Divino blanco y dorado para aniquilar a su objetivo.
 */
public class LimboOphanimEntity extends Monster implements GeoEntity {

    public static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(LimboOphanimEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.angel_ofanim.idle");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenLoop("animation.angel_ofanim.attack");

    private int attackAnimationTicks = 0;

    public LimboOphanimEntity(EntityType<? extends LimboOphanimEntity> type, Level level) {
        super(type, level);
        this.moveControl = new OphanimMoveControl(this);
        this.setNoGravity(true);
        this.xpReward = 85;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FLYING_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new OphanimShootOrbGoal(this));
        this.goalSelector.addGoal(5, new OphanimRandomFlyGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attackAnimationTicks > 0) {
            this.attackAnimationTicks--;
            if (this.attackAnimationTicks == 0 && this.isAttacking()) {
                this.setAttacking(false);
            }
        }

        // Mantener altura de vuelo constante y estable a 3.0 - 4.5 bloques sobre el suelo
        if (!this.level().isClientSide()) {
            BlockPos pos = this.blockPosition();
            int groundY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
            if (groundY <= this.level().getMinBuildHeight()) {
                groundY = 1;
            }
            double targetY = groundY + 3.5D; // Hover estable a 3.5 bloques de altura

            double diff = targetY - this.getY();
            if (Math.abs(diff) > 0.3D) {
                double vy = Math.max(-0.20D, Math.min(0.20D, diff * 0.12D));
                this.setDeltaMovement(this.getDeltaMovement().x * 0.90D, vy, this.getDeltaMovement().z * 0.90D);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().x * 0.92D, this.getDeltaMovement().y * 0.6D, this.getDeltaMovement().z * 0.92D);
            }
        }

        // Partículas místicas ambientales constantes alrededor del ojo y los aros
        if (this.level().isClientSide()) {
            if (this.random.nextFloat() < 0.35F) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 1.8D;
                double py = this.getY() + 1.2D + (this.random.nextDouble() - 0.5D) * 1.5D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 1.8D;
                this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.015D, 0);
            }
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    public void triggerAttackAnimation() {
        this.setAttacking(true);
        this.attackAnimationTicks = 40; // 2.0 segundos de animación
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // Bucle continuo sin pausas gestionado por OphanimAmbientSoundInstance en el cliente
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return com.soybe.awakeningsystem.ModSounds.LIMBO_OPHANIM_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return com.soybe.awakeningsystem.ModSounds.LIMBO_OPHANIM_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "ophanim_controller", 4, state -> {
            if (this.isAttacking()) {
                return state.setAndContinue(ATTACK_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    com.soybe.awakeningsystem.ModSounds.LIMBO_OPHANIM_DEATH.get(),
                    SoundSource.HOSTILE, 2.5F, 1.0F);
            java.util.List<LimboDoorEntity> doors = this.level().getEntitiesOfClass(
                    LimboDoorEntity.class,
                    this.getBoundingBox().inflate(64.0D)
            );
            net.minecraft.server.level.ServerPlayer killer = damageSource.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null;
            for (LimboDoorEntity door : doors) {
                door.notifyGuardianKilled(killer);
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    // ==================== MOVIMIENTO Y CONTROL DE VUELO ====================

    static class OphanimMoveControl extends MoveControl {
        private final LimboOphanimEntity ophanim;

        public OphanimMoveControl(LimboOphanimEntity ophanim) {
            super(ophanim);
            this.ophanim = ophanim;
        }

        @Override
        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                Vec3 vec3 = new Vec3(this.wantedX - this.ophanim.getX(), this.wantedY - this.ophanim.getY(), this.wantedZ - this.ophanim.getZ());
                double d0 = vec3.length();
                if (d0 < 0.3D) {
                    this.operation = Operation.WAIT;
                    this.ophanim.setDeltaMovement(this.ophanim.getDeltaMovement().scale(0.5D));
                    return;
                }
                vec3 = vec3.normalize();
                double speed = this.speedModifier * 0.12D;
                this.ophanim.setDeltaMovement(this.ophanim.getDeltaMovement().add(vec3.scale(speed)).scale(0.85D));
            }
        }
    }

    static class OphanimRandomFlyGoal extends Goal {
        private final LimboOphanimEntity ophanim;

        public OphanimRandomFlyGoal(LimboOphanimEntity ophanim) {
            this.ophanim = ophanim;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl movecontrol = this.ophanim.getMoveControl();
            if (!movecontrol.hasWanted()) return true;
            double d0 = movecontrol.getWantedX() - this.ophanim.getX();
            double d1 = movecontrol.getWantedY() - this.ophanim.getY();
            double d2 = movecontrol.getWantedZ() - this.ophanim.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            return d3 < 1.0D || d3 > 3600.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            double tx = this.ophanim.getX() + (this.ophanim.getRandom().nextDouble() - 0.5D) * 16.0D;
            double tz = this.ophanim.getZ() + (this.ophanim.getRandom().nextDouble() - 0.5D) * 16.0D;
            int groundY = this.ophanim.level().getHeight(Heightmap.Types.MOTION_BLOCKING, (int) tx, (int) tz);
            if (groundY <= this.ophanim.level().getMinBuildHeight()) {
                groundY = 1;
            }
            double ty = groundY + 3.0D + (this.ophanim.getRandom().nextDouble() * 1.5D);
            this.ophanim.getMoveControl().setWantedPosition(tx, ty, tz, 0.4D);
        }
    }

    // ==================== ATAQUE: MANIFESTACIÓN DEL ORBE SAGRADO ====================

    static class OphanimShootOrbGoal extends Goal {
        private final LimboOphanimEntity ophanim;
        private int attackTimer = 0;

        public OphanimShootOrbGoal(LimboOphanimEntity ophanim) {
            this.ophanim = ophanim;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.ophanim.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.attackTimer = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.ophanim.getTarget();
            if (target == null) return;

            this.ophanim.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = this.ophanim.distanceToSqr(target);

            // Mantener distancia aérea de 10 a 18 bloques y 2.5 - 3.0 bloques de altura
            if (distSq > 256.0D) {
                this.ophanim.getMoveControl().setWantedPosition(target.getX(), target.getY() + 2.8D, target.getZ(), 0.5D);
            } else if (distSq < 49.0D) {
                Vec3 flee = this.ophanim.position().subtract(target.position()).normalize().scale(8.0D);
                this.ophanim.getMoveControl().setWantedPosition(this.ophanim.getX() + flee.x, target.getY() + 2.8D, this.ophanim.getZ() + flee.z, 0.6D);
            }

            this.attackTimer++;

            // Iniciar animación de carga a los 40 ticks
            if (this.attackTimer == 40) {
                this.ophanim.triggerAttackAnimation();
                this.ophanim.level().playSound(null, this.ophanim.getX(), this.ophanim.getY(), this.ophanim.getZ(),
                        com.soybe.awakeningsystem.ModSounds.LIMBO_OPHANIM_CHARGE.get(), SoundSource.HOSTILE, 2.5F, 1.4F);
            }

            // A los 60 ticks (1.0s después de iniciar carga): Manifestar y lanzar el Orbe Divino
            if (this.attackTimer >= 62) {
                shootDivineOrb(target);
                this.attackTimer = 0;
            }
        }

        private void shootDivineOrb(LivingEntity target) {
            Level level = this.ophanim.level();
            if (level.isClientSide()) return;

            Vec3 eyePos = this.ophanim.position().add(0, 1.3D, 0);
            Vec3 dir = target.position().add(0, target.getBbHeight() * 0.5D, 0).subtract(eyePos).normalize();

            LimboDivineOrbProjectileEntity orb = new LimboDivineOrbProjectileEntity(level, this.ophanim);
            orb.setPos(eyePos.x + dir.x * 1.5D, eyePos.y + dir.y * 1.5D, eyePos.z + dir.z * 1.5D);
            orb.shoot(dir.x, dir.y, dir.z, 1.25F, 0.5F);

            level.addFreshEntity(orb);

            level.playSound(null, this.ophanim.getX(), this.ophanim.getY(), this.ophanim.getZ(),
                    com.soybe.awakeningsystem.ModSounds.LIMBO_OPHANIM_SHOOT.get(), SoundSource.HOSTILE, 2.5F, 1.8F);
        }
    }
}
