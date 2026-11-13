package com.soybe.awakeningsystem.mortis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A jurel (horse mackerel) on two legs. They live on top of a San Mortis can and, every few minutes and
 * without any warning, all of them gather in a ring on the roof and dance round it: hopping, spinning and
 * kicking their legs, then wander off again as if nothing happened.
 */
public class WalkingJurelEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DANCING = SynchedEntityData.defineId(WalkingJurelEntity.class, EntityDataSerializers.BOOLEAN);

    /** One ritual every 4 minutes, lasting 20 seconds; the phase depends on the can, so all its jurels agree. */
    private static final int CYCLE = 4800, RITUAL = 400;

    private BlockPos home;

    public WalkingJurelEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DANCING, false);
    }

    public boolean isDancing() {
        return this.entityData.get(DANCING);
    }

    public void setHome(BlockPos home) {
        this.home = home;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.LeapAtTargetGoal(this, 0.45F));
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.25D, false));
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    private boolean ritualTime() {
        if (home == null) return false;
        long phase = Math.floorMod(home.asLong() * 31L, CYCLE);
        return Math.floorMod(this.level().getGameTime() + phase, CYCLE) < RITUAL;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        boolean dance = ritualTime() && this.getLastHurtByMob() == null;
        if (dance != isDancing()) this.entityData.set(DANCING, dance);
        if (!dance) return;
        this.setTarget(null);                                   // nothing interrupts the ritual

        // a place on the ring, turning round the centre of the can's roof
        long t = this.level().getGameTime();
        double slot = (this.getUUID().getLeastSignificantBits() & 0xFFFF) / 65536.0 * Math.PI * 2;
        double ang = slot + t * 0.035;
        Vec3 spot = new Vec3(home.getX() + 0.5 + Math.cos(ang) * 3.5, home.getY(), home.getZ() + 0.5 + Math.sin(ang) * 3.5);
        this.getNavigation().stop();
        Vec3 to = spot.subtract(this.position());
        Vec3 move = new Vec3(to.x, 0, to.z);
        double len = move.length();
        if (len > 0.05) move = move.scale(Math.min(0.22, len) / len);
        this.setDeltaMovement(move.x, this.getDeltaMovement().y, move.z);
        if (this.onGround() && (t + (this.getId() * 7L)) % 14 == 0) this.setDeltaMovement(this.getDeltaMovement().x, 0.42, this.getDeltaMovement().z);
        float spin = (float) ((t * 18 + this.getId() * 40) % 360);
        this.setYRot(spin);
        this.yBodyRot = spin;
        this.yHeadRot = spin;
        if (t % 10 == 0 && this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + 1.4, this.getZ(), 1, 0.2, 0.1, 0.2, Mth.nextDouble(this.random, 0, 1));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (home != null) tag.put("Home", NbtUtils.writeBlockPos(home));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        home = NbtUtils.readBlockPos(tag, "Home").orElse(null);
    }

    /** The attack: a flying kick, with a slap of the tail on landing. */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            this.playSound(SoundEvents.COD_FLOP, 1.2F, 0.8F);
            net.minecraft.world.phys.Vec3 push = target.position().subtract(this.position()).normalize().scale(0.6);
            target.push(push.x, 0.25, push.z);
        }
        return hit;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.COD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COD_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 1.3F + this.random.nextFloat() * 0.3F;
    }
}
