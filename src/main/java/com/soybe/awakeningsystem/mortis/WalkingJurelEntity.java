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
 * A jurel (horse mackerel) on two legs. They live round the foot of a San Mortis can and, every few minutes and
 * without any warning, all of them gather in a ring on the ground and dance round the can: hopping, spinning
 * and kicking their legs, then wander off again as if nothing happened. They drop a jurel when they die.
 */
public class WalkingJurelEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DANCING = SynchedEntityData.defineId(WalkingJurelEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     * One ritual every 4 minutes, lasting as long as its music is heard (the clip is 60 s and fades out over its
     * last 3, so the dance stops as the music does); the phase depends on the can, so all its jurels agree.
     */
    private static final int CYCLE = 4800, RITUAL = 1150;
    /** When each can's music last started (by the can's position), so the jurels of one can play it once. */
    private static final java.util.Map<Long, Long> MUSIC_STARTED = new java.util.concurrent.ConcurrentHashMap<>();

    private BlockPos home;
    /** When this can's dances began to count (game time): the first comes two minutes after the can is built. */
    private long cycleStart = Long.MIN_VALUE;
    /**
     * For a jurel of the sea (born in the water): the spot of sea it lives in. When a nearby can's ritual begins it
     * climbs out, walks to the can, dances with the others, and goes back here when the music ends.
     */
    private BlockPos seaHome;
    /** The can a sea jurel goes to dance at (the nearest within reach), looked up now and then. */
    private BlockPos danceCan;
    private static final int SEA_REACH = 96;

    public WalkingJurelEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        // at home in water as on land
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, 0.0F);
        this.moveControl = new net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl(this, 85, 10, 0.06F, 0.1F, true);
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        return new net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation(this, level);
    }

    // a fish never drowns: it is in the entity type tag minecraft:can_breathe_under_water

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    /** In water it swims like a fish (no sinking, no bobbing at the surface); on land it walks. */
    @Override
    public void travel(Vec3 input) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed() * 0.6F, input);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        } else {
            super.travel(input);
        }
    }

    /** Born in the water (spawned in the sea), it is a jurel of the sea: that spot is its home. */
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                                  net.minecraft.world.entity.MobSpawnType reason, net.minecraft.world.entity.SpawnGroupData data) {
        if (home == null && this.isInWater()) this.seaHome = this.blockPosition();
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    /** Where the sea jurels spawn: in ocean water, at least a block deep. */
    public static boolean canSpawnInSea(EntityType<WalkingJurelEntity> type, net.minecraft.world.level.ServerLevelAccessor level,
                                        net.minecraft.world.entity.MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER) && level.getFluidState(pos.above()).is(net.minecraft.tags.FluidTags.WATER);
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
        if (home != null) this.restrictTo(home, SanMortisCan.R + 12);   // they wander round their can, not off into the world
    }

    /** The can's dance clock: all the jurels of one can are given the same start, so they dance together. */
    public void setCycleStart(long start) {
        this.cycleStart = start;
    }

    @Override
    protected void registerGoals() {
        // no FloatGoal: a fish stays under the water instead of bobbing at the top
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.RandomSwimmingGoal(this, 1.0D, 30));
        this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.LeapAtTargetGoal(this, 0.45F));
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.25D, false));
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** When /sanmortis dance started a dance for every can (game time), or -1. */
    public static long forcedStart = -1;

    private static boolean forced(long now) {
        return forcedStart >= 0 && now >= forcedStart && now - forcedStart < RITUAL;
    }

    private boolean ritualTime() {
        BlockPos can = home != null ? home : danceCan;
        if (can == null) return false;
        long now = this.level().getGameTime();
        if (forced(now)) return true;
        // a sea jurel keeps the clock of the can it visits (the same rule as for jurels from before the clock)
        long start = home != null ? cycleStart : Long.MIN_VALUE;
        if (start == Long.MIN_VALUE) start = now - Math.floorMod(can.asLong() * 31L, CYCLE);
        if (home != null) cycleStart = start;
        return now >= start && Math.floorMod(now - start, CYCLE) < RITUAL;
    }

    /**
     * A sea jurel's day: it swims round its spot of sea; when the ritual of a can within reach begins it walks out
     * to it (and dances once it is near, in the usual ring); when the music ends it goes back to the water.
     * Returns true while it is on its way and must not dance yet.
     */
    private boolean seaJurelStep(boolean ritual) {
        if (seaHome == null || home != null || !(this.level() instanceof ServerLevel sl)) return false;
        if (this.tickCount % 100 == 7) danceCan = SanMortisCan.Cans.nearest(sl, this.blockPosition(), SEA_REACH);
        if (ritual && danceCan != null) {
            this.clearRestriction();
            if (this.position().distanceToSqr(Vec3.atCenterOf(danceCan)) > (SanMortisCan.R + 10) * (SanMortisCan.R + 10)) {
                if (this.tickCount % 20 == 0) this.getNavigation().moveTo(danceCan.getX() + 0.5, danceCan.getY(), danceCan.getZ() + 0.5, 1.2D);
                return true;
            }
            return false;
        }
        // back to the sea: home is its spot of water
        this.restrictTo(seaHome, 24);
        if (!this.isInWater() && this.tickCount % 20 == 0) this.getNavigation().moveTo(seaHome.getX() + 0.5, seaHome.getY(), seaHome.getZ() + 0.5, 1.2D);
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        boolean ritualNow = ritualTime() && this.getLastHurtByMob() == null;
        if (seaJurelStep(ritualNow)) {
            if (isDancing()) this.entityData.set(DANCING, false);
            return;
        }
        BlockPos can = home != null ? home : danceCan;
        // its can is unbreakable, also one built before cans were kept track of
        if (home != null && this.tickCount % 100 == 1 && this.level() instanceof net.minecraft.server.level.ServerLevel sl)
            SanMortisCan.Cans.adopt(sl, home);
        boolean dance = ritualNow && can != null;
        if (dance != isDancing()) this.entityData.set(DANCING, dance);
        if (!dance) return;
        this.setTarget(null);                                   // nothing interrupts the ritual

        // the music, once per ritual, from the middle of the can's roof
        long now = this.level().getGameTime();
        long clock = home != null ? cycleStart : now - Math.floorMod(can.asLong() * 31L, CYCLE);
        long start = forced(now) ? forcedStart : now - Math.floorMod(now - clock, CYCLE);
        Long played = MUSIC_STARTED.get(can.asLong());
        if (played == null || played != start) {
            MUSIC_STARTED.put(can.asLong(), start);
            if (now - start < 40) {   // only at the very start, not when a jurel loads in halfway through
                this.level().playSound(null, can.getX() + 0.5, can.getY() + 1.0, can.getZ() + 0.5,
                        com.soybe.awakeningsystem.ModSounds.JUREL_DANCE.value(), net.minecraft.sounds.SoundSource.RECORDS, 1.0F, 1.0F);
            }
        }

        // a place on the ring on the ground, turning round the can (home is the middle of its foot)
        long t = this.level().getGameTime();
        double slot = (this.getUUID().getLeastSignificantBits() & 0xFFFF) / 65536.0 * Math.PI * 2;
        double ang = slot + t * 0.02;
        double ringR = SanMortisCan.R + 4.0;
        Vec3 spot = new Vec3(can.getX() + 0.5 + Math.cos(ang) * ringR, this.getY(), can.getZ() + 0.5 + Math.sin(ang) * ringR);
        // one that wandered off (or still stands on the roof) joins the ring at once
        if (this.position().distanceToSqr(spot) > 100) {
            BlockPos ground = this.level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(spot));
            this.teleportTo(spot.x, ground.getY(), spot.z);
        }
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
        if (seaHome != null) tag.put("SeaHome", NbtUtils.writeBlockPos(seaHome));
        if (cycleStart != Long.MIN_VALUE) tag.putLong("CycleStart", cycleStart);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setHome(NbtUtils.readBlockPos(tag, "Home").orElse(null));
        seaHome = NbtUtils.readBlockPos(tag, "SeaHome").orElse(null);
        if (tag.contains("CycleStart")) cycleStart = tag.getLong("CycleStart");
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
        // the sea ones come and go like any fish (not mid-dance); the ones living at a can stay
        return seaHome != null && home == null && !isDancing();
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
