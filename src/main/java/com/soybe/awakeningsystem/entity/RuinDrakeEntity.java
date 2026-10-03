package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Dragón de las Ruinas (Ruin Drake: Skywatch / Earthwatch).
 * Autómata militar aéreo con alas mecánicas de cuchillas, absorción de energía
 * y bombardeo de misiles biomecánicos guiados.
 */
public class RuinDrakeEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(RuinDrakeEntity.class, EntityDataSerializers.BOOLEAN);

    private int attackCooldown = 35;

    public RuinDrakeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setCustomName(Component.literal("§6§lDragón de las Ruinas: Vigía Celeste"));
        this.setCustomNameVisible(true);
        this.setNoGravity(true);
        this.xpReward = 320;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 280.0D)
                .add(Attributes.ATTACK_DAMAGE, 22.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.FLYING_SPEED, 0.40D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLYING, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DrakeFlightGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isFlyingDrake() {
        return this.entityData.get(DATA_FLYING);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.getTarget();
        if (!this.level().isClientSide && target != null && target.isAlive()) {
            if (attackCooldown > 0) {
                attackCooldown--;
            } else {
                executeDrakeBarrage(target);
            }
        }
    }

    private void executeDrakeBarrage(LivingEntity target) {
        this.attackCooldown = 55;
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.blockPosition(), SoundEvents.DISPENSER_LAUNCH, SoundSource.HOSTILE, 2.0F, 0.8F);

        // Disparo de 4 misiles mecánicos en abanico
        Vec3 dir = target.position().subtract(this.position()).normalize();
        for (int i = 0; i < 4; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * 1.5D;
            double oz = (this.random.nextDouble() - 0.5D) * 1.5D;
            Vec3 missilePos = this.position().add(ox, 1.5D, oz);

            for (double d = 1.0D; d <= 15.0D; d += 1.0D) {
                Vec3 p = missilePos.add(dir.scale(d));
                sl.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                sl.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 1, 0.02D, 0.02D, 0.02D, 0.01D);
            }
        }

        if (this.distanceToSqr(target) <= 225.0D) {
            target.hurt(this.damageSources().mobAttack(this), 20.0F);
            target.knockback(1.0D, -dir.x, -dir.z);
        }
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.9F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0D, this.getZ(), 2, 0.3D, 0.3D, 0.3D, 0.0D);

            if (SystemItems.CHAOS_CORE.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CORE.get(), 1));
            }
            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            if (isTeyvat && SystemItems.PRIMOGEM.get() != null) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 8 + random.nextInt(6)));
            } else if (!isTeyvat && SystemItems.AWAKENING_COIN.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.AWAKENING_COIN.get(), 4));
            }
        }
    }

    static class DrakeFlightGoal extends Goal {
        private final RuinDrakeEntity drake;

        public DrakeFlightGoal(RuinDrakeEntity drake) {
            this.drake = drake;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = drake.getTarget();
            if (target != null) {
                double targetY = target.getY() + 5.0D;
                double dx = target.getX() - drake.getX();
                double dz = target.getZ() - drake.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);

                double desiredDist = 8.0D;
                double speed = 0.28D;

                if (dist > desiredDist + 1.5D) {
                    drake.setDeltaMovement(drake.getDeltaMovement().add(dx / dist * speed, (targetY - drake.getY()) * 0.04D, dz / dist * speed));
                } else if (dist < desiredDist - 1.5D) {
                    drake.setDeltaMovement(drake.getDeltaMovement().add(-dx / dist * speed, (targetY - drake.getY()) * 0.04D, -dz / dist * speed));
                }
            } else {
                drake.setDeltaMovement(drake.getDeltaMovement().scale(0.92D));
            }
        }
    }
}
