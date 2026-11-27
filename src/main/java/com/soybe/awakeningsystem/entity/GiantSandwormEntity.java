package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.penalty.PenaltyZoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sandworm boss entity faithful to Enter Sandworm original mechanics.
 * Features undulating subterranean swimming, charge lunges, continuous corkscrew
 * body rotation, and train-physics trailing segments.
 */
public class GiantSandwormEntity extends Monster {

    /**
     * How a giant sandworm hunts:
     * PROWL - swims under the sand in wide curves, each worm in its own sector around the player, keeping
     *         away from the other worms; a ripple of sand on the surface shows where it is.
     * STALK - one worm at a time drives straight at the player under the sand.
     * BREACH - right under the player it bursts out upwards, biting, and arcs over.
     * DIVE / RETREAT - it plunges back in, swims off and prowls a while before the next attack.
     */
    public enum Stage {
        PROWL,
        STALK,
        BREACH,
        RETREAT
    }

    /** When any worm in a dimension last went for the player: attacks are spaced out, never two at once. */
    private static final java.util.Map<net.minecraft.resources.ResourceKey<Level>, Long> LAST_ATTACK = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MIN_GAP_BETWEEN_ATTACKS = 100;   // 5 s between one worm's attack and the next's

    private static final EntityDataAccessor<Integer> BODY_PART_COUNT =
            SynchedEntityData.defineId(GiantSandwormEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.awakening_system.giant_sandworm.boss_title"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    @Nullable
    public GiantSandwormHeadPart head;
    public final List<GiantSandwormBodyPart> bodyParts = new ArrayList<>();

    private Stage stage = Stage.PROWL;
    private int waveTicks = 0;
    private int stageTicks = 0;
    private int attackCooldown = 120;
    private Vec3 breachVelocity = Vec3.ZERO;
    private boolean wasInSolid = false;
    private Player currentTarget;
    private float currentHeadingYaw = 0.0F;
    /** This worm's own angle around the player while prowling, so the worms spread out instead of bunching up. */
    private float wanderAngle = 0.0F;

    public GiantSandwormEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.65D)
                .add(Attributes.FOLLOW_RANGE, 200.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BODY_PART_COUNT, 32);
    }

    public void scaleToPlayerLevel(int playerLevel) {
        int safeLvl = Math.max(1, playerLevel);
        double maxHp = 120.0D + (safeLvl * 6.0D);
        double attackDmg = Math.min(6.0D + (safeLvl * 0.35D), 30.0D);
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHp);
            this.setHealth((float) maxHp);
        }
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attackDmg);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);

        if (!level.isClientSide()) {
            int parts = 28 + this.random.nextInt(8); // 28 to 35 segments
            this.entityData.set(BODY_PART_COUNT, parts);
            this.cleanupParts();
            this.rebuildParts();
        }
        return data;
    }

    public void rebuildParts() {
        if (level().isClientSide) return;
        // whatever is left of the old body goes first (some parts may have unloaded with their land)
        if (this.head != null && !this.head.isRemoved()) this.head.discard();
        for (GiantSandwormBodyPart old : this.bodyParts) if (!old.isRemoved()) old.discard();
        this.bodyParts.clear();

        this.head = new GiantSandwormHeadPart(ModEntities.GIANT_SANDWORM_HEAD.get(), level());
        this.head.initOwner(this);
        this.head.setPos(this.getX(), this.getY(), this.getZ());
        this.head.setYRot(this.getYRot());
        this.head.setXRot(this.getXRot());
        this.head.yRotO = this.getYRot();
        this.head.xRotO = this.getXRot();
        level().addFreshEntity(this.head);

        int parts = this.entityData.get(BODY_PART_COUNT);
        if (parts < 20 || parts > 45) parts = 32;

        float yawRad = (float) Math.toRadians(this.getYRot());
        double backX = Math.sin(yawRad);
        double backZ = -Math.cos(yawRad);

        this.bodyParts.clear();
        for (int i = 0; i < parts; i++) {
            GiantSandwormBodyPart bodyPart = new GiantSandwormBodyPart(ModEntities.GIANT_SANDWORM_BODY.get(), level());
            bodyPart.initOwner(this);
            double px = this.getX() + backX * (i + 1) * 2.0D;
            double py = this.getY();
            double pz = this.getZ() + backZ * (i + 1) * 2.0D;
            bodyPart.setPos(px, py, pz);
            bodyPart.setYRot(this.getYRot());
            bodyPart.setXRot(this.getXRot());
            bodyPart.yRotO = this.getYRot();
            bodyPart.xRotO = this.getXRot();

            // Taper the tail (last 15 segments taper down gracefully to 1)
            if (parts - i < 15) {
                bodyPart.setRenderScale(Math.max(1, parts - i));
            } else {
                bodyPart.setRenderScale(15);
            }

            level().addFreshEntity(bodyPart);
            this.bodyParts.add(bodyPart);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // Crucial: never despawn far away in penalty zone
    }

    @Override
    public void checkDespawn() {
        // Prevent random vanilla monster despawning
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 65536.0D; // 256 block render distance
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();

        if (this.head != null) {
            this.head.yRotO = this.head.getYRot();
            this.head.xRotO = this.head.getXRot();
        }

        if (!level().isClientSide && this.isDeadOrDying()) {
            // dead: no more hunting, no parts brought back, and its health bar gone for good
            // (it used to be shown again, empty, to everyone near while the death played out)
            this.bossEvent.removeAllPlayers();
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        if (!level().isClientSide) {
            // (parts are never saved: after an unload they are gone and are built again here)
            if (this.head == null || this.head.isRemoved() || this.bodyParts.isEmpty() || this.bodyParts.get(0).isRemoved()) {
                this.rebuildParts();
            }

            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            // Boss bar only visible when player is within 64 blocks of this worm
            for (Player player : level().players()) {
                if (player instanceof ServerPlayer sp) {
                    if (sp.distanceToSqr(this) <= 4096.0D) {
                        if (!this.bossEvent.getPlayers().contains(sp)) {
                            this.bossEvent.addPlayer(sp);
                        }
                    } else {
                        this.bossEvent.removePlayer(sp);
                    }
                }
            }

            this.currentTarget = findTargetPlayer();

            // Movement logic faithful to original mod
            tickMovementAI();

            // Collision audio and sand eruption particles
            checkBlockCollisions();

            // Synchronize parts train positions and rotations
            tickHeadPart();
            tickBodyParts();
        }
    }

    private Player findTargetPlayer() {
        if (this.currentTarget != null && this.currentTarget.isAlive() && !this.currentTarget.isSpectator()) {
            if (this.distanceToSqr(this.currentTarget) < 40000.0D) {
                return this.currentTarget;
            }
        }

        Player nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (Player p : level().players()) {
            if (p.isAlive() && !p.isSpectator()) {
                // If in creative, engage if within 40 blocks so user can test the boss attacks
                if (p.isCreative() && p.distanceToSqr(this) > 1600.0D) {
                    continue;
                }
                double dSq = p.distanceToSqr(this);
                if (dSq < 40000.0D && dSq < nearestDistSq) { // 200 blocks
                    nearest = p;
                    nearestDistSq = dSq;
                }
            }
        }
        return nearest;
    }

    private double getGroundHeight() {
        int blockX = (int) this.getX();
        int blockZ = (int) this.getZ();
        if (level() instanceof ServerLevel sl && !sl.hasChunk(blockX >> 4, blockZ >> 4)) {
            return this.getY();
        }
        int h = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);
        if (h <= level().getMinBuildHeight() || h < 40) {
            return 70.0D;
        }
        return (double) h;
    }

    private void tickMovementAI() {
        if (this.currentHeadingYaw == 0.0F && this.getYRot() != 0.0F) this.currentHeadingYaw = this.getYRot();
        if (this.wanderAngle == 0.0F) this.wanderAngle = this.random.nextFloat() * 360.0F;
        double groundY = getGroundHeight();
        double cruiseY = groundY - 4.0D;
        stageTicks++;
        waveTicks++;

        Player target = currentTarget != null && currentTarget.isAlive() ? currentTarget : null;
        // too far from the player (it would sit in unloaded ground): resurface underground at the edge of the area
        if (target != null && stage != Stage.BREACH && horizontalDistance(target) > 110.0D) {
            relocateNear(target);
            return;
        }

        switch (stage) {
            case PROWL -> {
                if (attackCooldown > 0) attackCooldown--;
                if (target != null) {
                    // its own spot on a slowly turning ring around the player, 30-55 blocks out
                    wanderAngle += 0.25F;
                    double ring = 42.0D + Math.sin(waveTicks * 0.015D + getId()) * 13.0D;
                    double a = Math.toRadians(wanderAngle);
                    double gx = target.getX() + Math.cos(a) * ring, gz = target.getZ() + Math.sin(a) * ring;
                    Vec3 away = awayFromOtherWorms();
                    steerToward(gx + away.x * 25.0D, gz + away.z * 25.0D, 2.0F);
                    long last = LAST_ATTACK.getOrDefault(level().dimension(), 0L);
                    if (attackCooldown <= 0 && level().getGameTime() - last > MIN_GAP_BETWEEN_ATTACKS) {
                        LAST_ATTACK.put(level().dimension(), level().getGameTime());
                        setStage(Stage.STALK);
                        level().playSound(null, blockPosition(), ModSounds.SANDWORM_ROAR.get(), SoundSource.HOSTILE, 4.0F, 0.8F);
                    }
                } else {
                    // nobody around: long lazy curves through the dunes
                    if (waveTicks % 140 == 0) wanderAngle += (this.random.nextFloat() - 0.5F) * 70.0F;
                    Vec3 away = awayFromOtherWorms();
                    double a = Math.toRadians(wanderAngle);
                    steerToward(getX() + Math.cos(a) * 30.0D + away.x * 25.0D, getZ() + Math.sin(a) * 30.0D + away.z * 25.0D, 1.2F);
                }
                swim(0.6D, cruiseY + Math.sin(waveTicks * 0.06D) * 1.2D);
                surfaceRipple(groundY, 6);
            }
            case STALK -> {
                if (target == null || stageTicks > 220) {
                    setStage(Stage.RETREAT);
                    break;
                }
                double dist = horizontalDistance(target);
                steerToward(target.getX(), target.getZ(), 4.5F);
                // closer under the sand as it comes, and faster
                swim(dist > 30.0D ? 1.0D : 0.85D, groundY - (dist > 20.0D ? 4.0D : 2.5D));
                surfaceRipple(groundY, 2);
                if (stageTicks % 20 == 0) level().playSound(null, blockPosition(), SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 3.0F, 0.5F);
                if (dist < 5.0D) {
                    // right under them: burst out of the sand
                    Vec3 flat = new Vec3(target.getX() - getX(), 0, target.getZ() - getZ());
                    flat = flat.lengthSqr() < 1.0E-4 ? Vec3.ZERO : flat.normalize();
                    breachVelocity = new Vec3(flat.x * 0.45D, 1.7D, flat.z * 0.45D);
                    setStage(Stage.BREACH);
                    level().playSound(null, blockPosition(), ModSounds.SANDWORM_ROAR.get(), SoundSource.HOSTILE, 5.0F, 1.0F);
                }
            }
            case BREACH -> {
                // a leap straight up out of the sand that falls back under its own weight
                breachVelocity = breachVelocity.add(0, -0.085D, 0);
                setDeltaMovement(breachVelocity);
                float yaw = (float) (Mth.atan2(breachVelocity.x, breachVelocity.z) * 180.0F / (float) Math.PI) + 180.0F;
                if (breachVelocity.horizontalDistanceSqr() > 1.0E-4) currentHeadingYaw = yaw;
                checkAttackPlayer();
                if (breachVelocity.y < 0 && getY() < groundY - 3.0D) setStage(Stage.RETREAT);
                if (stageTicks > 80) setStage(Stage.RETREAT);
            }
            case RETREAT -> {
                // dive and swim away before prowling again
                if (target != null && stageTicks < 5) wanderAngle = (float) Math.toDegrees(Math.atan2(getZ() - target.getZ(), getX() - target.getX()));
                double a = Math.toRadians(wanderAngle);
                steerToward(getX() + Math.cos(a) * 30.0D, getZ() + Math.sin(a) * 30.0D, 3.0F);
                swim(0.9D, cruiseY - 1.5D);
                if (stageTicks > 70) {
                    attackCooldown = 160 + this.random.nextInt(180);   // 8-17 s before it may attack again
                    setStage(Stage.PROWL);
                }
            }
        }

        this.setPos(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);
        if (stage != Stage.BREACH) checkAttackPlayer();
    }

    private void setStage(Stage next) {
        stage = next;
        stageTicks = 0;
    }

    private double horizontalDistance(Player p) {
        double dx = p.getX() - getX(), dz = p.getZ() - getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Turns the heading towards a point, at most {@code maxTurn} degrees this tick (a big worm turns wide). */
    private void steerToward(double x, double z, float maxTurn) {
        float desired = (float) (Mth.atan2(x - getX(), z - getZ()) * 180.0F / (float) Math.PI) + 180.0F;
        currentHeadingYaw += Mth.clamp(Mth.wrapDegrees(desired - currentHeadingYaw), -maxTurn, maxTurn);
    }

    /** Moves along the heading at {@code speed}, easing towards depth {@code y}. */
    private void swim(double speed, double y) {
        double rad = Math.toRadians(currentHeadingYaw);
        double dy = Mth.clamp((y - getY()) * 0.15D, -0.6D, 0.6D);
        setDeltaMovement(-Math.sin(rad) * speed, dy, -Math.cos(rad) * speed);
    }

    /** A push away from other worms within 35 blocks, so they spread out over the desert. */
    private Vec3 awayFromOtherWorms() {
        Vec3 push = Vec3.ZERO;
        for (GiantSandwormEntity other : level().getEntitiesOfClass(GiantSandwormEntity.class, getBoundingBox().inflate(35.0D), w -> w != this)) {
            Vec3 d = new Vec3(getX() - other.getX(), 0, getZ() - other.getZ());
            double len = d.length();
            if (len < 1.0E-3) d = new Vec3(this.random.nextDouble() - 0.5D, 0, this.random.nextDouble() - 0.5D);
            push = push.add(d.normalize().scale((35.0D - Math.min(35.0D, len)) / 35.0D));
        }
        return push.lengthSqr() > 1.0D ? push.normalize() : push;
    }

    /** Sand churning on the surface above it, so the player can see it coming. */
    private void surfaceRipple(double groundY, int every) {
        if (stageTicks % every != 0 || !(level() instanceof ServerLevel sl) || getY() < groundY - 6.0D) return;
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                getX(), groundY + 0.1D, getZ(), 12, 1.6D, 0.1D, 1.6D, 0.15D);
    }

    /** Moves the worm, underground, to somewhere around the player 55-70 blocks out, away from the other worms. */
    private void relocateNear(Player target) {
        double best = -1, bx = 0, bz = 0;
        for (int i = 0; i < 8; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0D, r = 55.0D + this.random.nextDouble() * 15.0D;
            double x = target.getX() + Math.cos(a) * r, z = target.getZ() + Math.sin(a) * r;
            double nearest = Double.MAX_VALUE;
            for (GiantSandwormEntity w : level().getEntitiesOfClass(GiantSandwormEntity.class, target.getBoundingBox().inflate(150.0D), w -> w != this))
                nearest = Math.min(nearest, w.distanceToSqr(x, w.getY(), z));
            if (nearest > best) { best = nearest; bx = x; bz = z; }
        }
        this.setPos(bx, getY(), bz);
        this.setPos(bx, getGroundHeight() - 5.0D, bz);
        this.currentHeadingYaw = (float) (Mth.atan2(target.getX() - bx, target.getZ() - bz) * 180.0F / (float) Math.PI) + 180.0F;
        this.wanderAngle = (float) Math.toDegrees(Math.atan2(bz - target.getZ(), bx - target.getX()));
        setStage(Stage.PROWL);
        this.rebuildParts();
    }

    private void tickHeadPart() {
        if (head == null) return;
        head.setPos(this.getX(), this.getY(), this.getZ());
        Vec3 v = getDeltaMovement();
        if (v.lengthSqr() > 1.0E-6) {
            double velX = v.x;
            double velZ = v.z;
            double velY = v.y;

            double horizontalSpeed = Math.sqrt(velX * velX + velZ * velZ);
            float newYaw = (float) (Mth.atan2(velX, velZ) * 180.0F / (float) Math.PI) + 180.0F;
            float newPitch = (float) (-(Mth.atan2(velY, horizontalSpeed) * 180.0F / (float) Math.PI));

            head.setYRot(newYaw);
            head.setXRot(newPitch);
            this.setYRot(newYaw);
            this.setXRot(newPitch);
        }
    }

    private void tickBodyParts() {
        Vec3 prevPos = head != null ? head.position() : position();

        for (int i = 0; i < bodyParts.size(); i++) {
            GiantSandwormBodyPart part = bodyParts.get(i);
            if (part == null) continue;

            part.yRotO = part.getYRot();
            part.xRotO = part.getXRot();

            Vec3 partPos = part.position();
            Vec3 delta = prevPos.subtract(partPos);

            double desiredDistance = 2.0D; // authentic segment spacing
            double distance = delta.length();

            if (distance > 1.0E-4) {
                Vec3 dir = delta.scale(1.0D / distance);
                Vec3 targetPos = prevPos.subtract(dir.scale(desiredDistance));
                part.setDeltaMovement(targetPos.subtract(partPos));
                part.setPos(targetPos.x, targetPos.y, targetPos.z);

                double velX = dir.x;
                double velY = dir.y;
                double velZ = dir.z;

                double horizontalSpeed = Math.sqrt(velX * velX + velZ * velZ);
                float newYaw = (float) (Mth.atan2(velX, velZ) * 180.0F / (float) Math.PI) + 180.0F;
                float newPitch = (float) (-(Mth.atan2(velY, horizontalSpeed) * 180.0F / (float) Math.PI));

                part.setYRot(newYaw);
                part.setXRot(newPitch);
            }

            prevPos = part.position();
        }
    }

    private void checkAttackPlayer() {
        AABB hitBox = this.getBoundingBox().inflate(3.5D);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (Player p : level().getEntitiesOfClass(Player.class, hitBox)) {
            if (p.isAlive() && !p.isSpectator() && !p.isCreative()) {
                p.hurt(level().damageSources().mobAttack(this), damage);
                p.knockback(1.4D, this.getDeltaMovement().x, this.getDeltaMovement().z);
            }
        }
    }

    private void checkBlockCollisions() {
        BlockPos currentPos = blockPosition();
        BlockState state = level().getBlockState(currentPos);
        boolean isSolid = state.isSolid();

        if (isSolid != this.wasInSolid) {
            this.wasInSolid = isSolid;
            if (level() instanceof ServerLevel serverLevel) {
                BlockState sandState = Blocks.SAND.defaultBlockState();
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, sandState),
                        this.getX(), this.getY() + 1.0D, this.getZ(),
                        75, 4.0D, 3.0D, 4.0D, 0.35D);

                serverLevel.playSound(null, currentPos, ModSounds.SANDWORM_BOOM.get(), SoundSource.HOSTILE, 3.0F, 0.85F + this.random.nextFloat() * 0.3F);
                serverLevel.playSound(null, currentPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 0.6F);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getEntity() instanceof Player player) {
            this.currentTarget = player;
        }
        return hurt;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.0D, this.getZ(), 4, 2.0D, 2.0D, 2.0D, 0.1D);
            serverLevel.playSound(null, blockPosition(), ModSounds.SANDWORM_BOOM.get(), SoundSource.HOSTILE, 4.0F, 0.6F);
            // the whole body bursts into sand along its length, so the death is seen, not just a vanishing
            BlockParticleOption sand = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState());
            for (int i = 0; i < this.bodyParts.size(); i += 2) {
                GiantSandwormBodyPart p = this.bodyParts.get(i);
                if (p != null) serverLevel.sendParticles(sand, p.getX(), p.getY() + 1.0D, p.getZ(), 30, 1.5D, 1.5D, 1.5D, 0.2D);
                if (p != null && i % 8 == 0) serverLevel.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY() + 1.0D, p.getZ(), 1, 0, 0, 0, 0);
            }

            if (damageSource.getEntity() instanceof ServerPlayer sp) {
                PenaltyZoneManager.onSandwormKilled(serverLevel, sp);
            } else if (this.currentTarget instanceof ServerPlayer sp) {
                PenaltyZoneManager.onSandwormKilled(serverLevel, sp);
            }
        }

        this.cleanupParts();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        // its parts go with it only when it is really gone (killed, discarded). When its land is unloading (the
        // last player left the desert), removing other creatures in the middle of that crashed the server; the
        // parts are never saved, so they simply go with the land
        if (reason.shouldDestroy()) this.cleanupParts();
        else this.bossEvent.removeAllPlayers();
    }

    /** Also when it leaves the world without being killed (its land unloaded, the server stopping): no bar left behind. */
    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        this.bossEvent.removeAllPlayers();
    }

    private void cleanupParts() {
        if (this.head != null) {
            this.head.discard();
        }
        for (GiantSandwormBodyPart part : this.bodyParts) {
            part.discard();
        }
        this.bossEvent.removeAllPlayers();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.isAlive()) this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("wave_ticks", this.waveTicks);
        tag.putString("stage", this.stage.name());
        tag.putFloat("heading_yaw", this.currentHeadingYaw);
        tag.putFloat("wander_angle", this.wanderAngle);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.waveTicks = tag.getInt("wave_ticks");
        if (tag.contains("stage")) {
            try {
                this.stage = Stage.valueOf(tag.getString("stage"));
            } catch (Exception ignored) {}
        }
        if (tag.contains("heading_yaw")) this.currentHeadingYaw = tag.getFloat("heading_yaw");
        if (tag.contains("wander_angle")) this.wanderAngle = tag.getFloat("wander_angle");
    }
}
