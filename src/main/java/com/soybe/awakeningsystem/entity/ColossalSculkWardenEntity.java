package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.DimensionGenSavedData;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Titán Colosal de Sculk, the boss of floor 10 of the Great Tower.
 *
 * <p>Has its own GeckoLib model (a hunched giant of deepslate and sculk with a glowing heart, bone horns and four back\n * tentacles) and its own animations. Every attack plays its own
 * animation and is announced before it lands, so the player can read it and dodge:
 * <ul>
 *   <li><b>Zarpazo</b> (close): a heavy swipe in front of it.</li>
 *   <li><b>Doble aplastamiento</b> (close): both fists hit the ground twice just ahead of it.</li>
 *   <li><b>Onda sísmica</b> (close/mid): a ring of shock travels outward along the floor; jump over it.</li>
 *   <li><b>Latigazo de tentáculos</b> (mid): three sweeps around its front that drag targets in.</li>
 *   <li><b>Púas de sculk</b> (mid/far): spikes burst from the floor in a line towards the target.</li>
 *   <li><b>Almas en erupción</b> (mid/far): glowing marks under the players explode a moment later.</li>
 *   <li><b>Estallido sónico</b> (far): it charges, aims, and fires a beam at where the player stood.</li>
 * </ul>
 * Below half health it roars, calls two Sculk Golems, and attacks faster with stronger versions.
 */
public class ColossalSculkWardenEntity extends Monster implements GeoEntity {

    private static final String PREFIX = "animation.sculk_titan.";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(PREFIX + "idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop(PREFIX + "walk");

    /** One attack: its animation, how long it lasts, and the range it is chosen at. */
    private enum Attack {
        ROAR("roar", 56, 0, 99),
        SWIPE("attack", 30, 0, 6.5),
        DOUBLE_SLAM("double_slam", 36, 0, 7),
        GROUND_SMASH("ground_smash", 36, 0, 12),
        TENTACLES("tentacle_attack", 40, 0, 9),
        SPIKES("spike_barrage", 40, 5, 26),
        SOULS("soul_attack", 64, 6, 30),
        SONIC_BOOM("sonic_boom", 64, 12, 34);

        final String anim;
        final int length;
        final double minRange, maxRange;

        Attack(String anim, int length, double minRange, double maxRange) {
            this.anim = anim;
            this.length = length;
            this.minRange = minRange;
            this.maxRange = maxRange;
        }
    }

    private static final EntityDimensions SIZE = EntityDimensions.scalable(3.4F, 6.4F).withEyeHeight(5.2F);
    private static final BlockState SCULK = Blocks.SCULK.defaultBlockState();
    private static final DustParticleOptions CYAN = new DustParticleOptions(new Vector3f(0.10F, 0.90F, 1.00F), 1.6F);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatableWithFallback("entity.awakening_system.colossal_sculk_warden", "Titán Colosal de Sculk")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_20).setDarkenScreen(true);

    private Attack current;
    private Attack last;
    private int attackTick;
    private int cooldown = 40;
    private boolean roared;
    private boolean enraged;
    private int darknessTimer;

    /** What the current attack aimed at, fixed when it was announced so the player can move out of it. */
    private Vec3 aim = Vec3.ZERO;
    private Vec3 aimDir = Vec3.ZERO;
    private final List<Vec3> marks = new ArrayList<>();
    private final Set<UUID> alreadyHit = new HashSet<>();

    public ColossalSculkWardenEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setPersistenceRequired();
        this.refreshDimensions();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1200.0D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.STEP_HEIGHT, 1.6D)
                .add(Attributes.SAFE_FALL_DISTANCE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return SIZE;
    }

    // ------------------------------------------------------------------ boss bar and persistence

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void checkDespawn() {
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Roared", roared);
        tag.putBoolean("Enraged", enraged);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        roared = tag.getBoolean("Roared");
        enraged = tag.getBoolean("Enraged");
        if (enraged) setEnragedLook();
    }

    // ------------------------------------------------------------------ sounds

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WARDEN_STEP, 2.0F, 0.6F);
    }

    @Override
    public float getVoicePitch() {
        return 0.65F + this.random.nextFloat() * 0.1F;
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    // ------------------------------------------------------------------ brain

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) this.level();
        bossEvent.setProgress(getHealth() / getMaxHealth());

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || distanceToSqr(target) > 56 * 56
                || (target instanceof Player p && (p.isCreative() || p.isSpectator()))) {
            Player nearest = level.getNearestPlayer(getX(), getY(), getZ(), 48.0D,
                    e -> e instanceof Player p && !p.isCreative() && !p.isSpectator());
            setTarget(nearest);
            target = nearest;
        }

        if (++darknessTimer >= 100) {
            darknessTimer = 0;
            for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(30.0D))) {
                if (!p.isCreative() && !p.isSpectator()) {
                    p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 140, 0, false, false, true));
                }
            }
        }

        if (!enraged && getHealth() <= getMaxHealth() * 0.5F && current == null) {
            enraged = true;
            setEnragedLook();
            start(Attack.ROAR, target);
            return;
        }

        if (current != null) {
            getNavigation().stop();
            setDeltaMovement(0, getDeltaMovement().y, 0);
            if (target != null && attackTick < current.length * 0.4) faceTowards(target);
            runAttack(level, target);
            if (++attackTick >= current.length) {
                last = current;
                current = null;
                cooldown = enraged ? 12 : 26;
            }
            return;
        }

        if (target == null) return;
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        double dist = distanceTo(target);

        if (!roared && dist < 30) {
            roared = true;
            start(Attack.ROAR, target);
            return;
        }

        if (cooldown > 0) cooldown--;
        if (cooldown <= 0) {
            Attack next = pick(dist);
            if (next != null) {
                start(next, target);
                return;
            }
        }
        if (dist > 4.5 && tickCount % 10 == 0) getNavigation().moveTo(target, 1.0D);
    }

    private Attack pick(double dist) {
        List<Attack> options = new ArrayList<>();
        for (Attack a : Attack.values()) {
            if (a == Attack.ROAR || a == last) continue;
            if (dist >= a.minRange && dist <= a.maxRange) options.add(a);
        }
        if (options.isEmpty()) return null;
        return options.get(random.nextInt(options.size()));
    }

    private void start(Attack attack, LivingEntity target) {
        current = attack;
        attackTick = 0;
        marks.clear();
        alreadyHit.clear();
        aim = target != null ? target.position() : position().add(getLookAngle().scale(8));
        aimDir = horizontal(aim.subtract(position()));
        if (target != null) faceTowards(target);
        triggerAnim("attack", attack.name());
    }

    private void faceTowards(LivingEntity target) {
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        setYRot(yaw);
        yBodyRot = yaw;
        yHeadRot = yaw;
        getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    private void setEnragedLook() {
        bossEvent.setColor(BossEvent.BossBarColor.RED);
        bossEvent.setName(Component.translatableWithFallback("entity.awakening_system.colossal_sculk_warden.enraged",
                "Titán Colosal de Sculk (Enfurecido)").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
    }

    // ------------------------------------------------------------------ attacks

    private void runAttack(ServerLevel level, LivingEntity target) {
        int t = attackTick;
        switch (current) {
            case ROAR -> roar(level, t);
            case SWIPE -> {
                if (t == 4) playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 2.0F, 0.5F);
                if (t == 13) frontalHit(level, 6.0, 0.55, 32.0F, 1.8, 0.5);
            }
            case DOUBLE_SLAM -> {
                if (t < 15) telegraphCircle(level, front(3.6), 4.0, t);
                if (t == 15 || t == 22) slam(level, front(3.6), 4.0, enraged ? 26.0F : 22.0F);
            }
            case GROUND_SMASH -> groundSmash(level, t);
            case TENTACLES -> {
                if (t == 10 || t == 19 || t == 29) tentacleSweep(level, t);
            }
            case SPIKES -> spikes(level, target, t);
            case SOULS -> souls(level, t);
            case SONIC_BOOM -> sonicBoom(level, target, t);
        }
    }

    private void roar(ServerLevel level, int t) {
        if (t == 14) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 4.0F, 0.55F);
        }
        if (t >= 14 && t <= 40 && t % 4 == 0) {
            Vec3 mouth = position().add(getLookAngle().scale(2.0)).add(0, 4.4, 0);
            level.sendParticles(ParticleTypes.SONIC_BOOM, mouth.x, mouth.y, mouth.z, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 3.0, getZ(), 12, 2.0, 1.5, 2.0, 0.08);
        }
        if (t == 16) {
            for (LivingEntity e : others(level, getBoundingBox().inflate(12.0, 4.0, 12.0))) {
                Vec3 push = horizontal(e.position().subtract(position())).scale(1.6);
                e.push(push.x, 0.4, push.z);
                e.hurtMarked = true;
                e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0));
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            }
            if (enraged) summonGolems(level);
        }
    }

    private void summonGolems(ServerLevel level) {
        for (int side = -1; side <= 1; side += 2) {
            SculkGolemEntity golem = new SculkGolemEntity(ModEntities.SCULK_GOLEM.get(), level);
            Vec3 at = position().add(horizontal(getLookAngle()).yRot((float) Math.toRadians(90 * side)).scale(5.0));
            golem.moveTo(at.x, getY(), at.z, getYRot(), 0);
            golem.getEntityData().set(SculkGolemEntity.IS_STATUE, false);
            if (getTarget() != null) golem.setTarget(getTarget());
            level.addFreshEntity(golem);
            level.sendParticles(ParticleTypes.SCULK_SOUL, at.x, getY() + 1, at.z, 30, 0.6, 1.0, 0.6, 0.05);
        }
    }

    /** A blow that lands on everything inside a cone in front of it. */
    private void frontalHit(ServerLevel level, double reach, double minDot, float damage, double push, double lift) {
        Vec3 look = horizontal(getLookAngle());
        Vec3 tip = position().add(look.scale(reach * 0.6)).add(0, 1.5, 0);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, tip.x, tip.y, tip.z, 3, 1.2, 0.4, 1.2, 0);
        for (LivingEntity e : others(level, getBoundingBox().inflate(reach, 3.0, reach))) {
            Vec3 to = e.position().subtract(position());
            if (horizontal(to).dot(look) < minDot || horizontalDist(to) > reach + 1.7) continue;
            if (e.hurt(damageSources().mobAttack(this), damage)) {
                Vec3 kb = horizontal(to).scale(push);
                e.push(kb.x, lift, kb.z);
                e.hurtMarked = true;
            }
        }
    }

    private void slam(ServerLevel level, Vec3 at, double radius, float damage) {
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.6F, 0.6F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.HOSTILE, 2.5F, 0.6F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, SCULK), at.x, at.y + 0.2, at.z, 80, radius * 0.5, 0.3, radius * 0.5, 0.3);
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5, at.z, 3, radius * 0.4, 0.2, radius * 0.4, 0);
        for (LivingEntity e : others(level, new AABB(at, at).inflate(radius, 3.0, radius))) {
            if (horizontalDist(e.position().subtract(at)) > radius) continue;
            if (e.hurt(damageSources().mobAttack(this), damage)) {
                Vec3 kb = horizontal(e.position().subtract(at)).scale(0.8);
                e.push(kb.x, 0.85, kb.z);
                e.hurtMarked = true;
            }
        }
    }

    /** A ring of shock that rolls outward along the floor. Anyone on the ground when it passes is hit. */
    private void groundSmash(ServerLevel level, int t) {
        if (t < 15) {
            if (t % 3 == 0) level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, getX(), getY() + 0.3, getZ(), 20, 2.5, 0.1, 2.5, 0.02);
            return;
        }
        if (t == 15) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.5F);
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.5F, 0.5F);
        }
        double radius = 2.0 + (t - 15) * 0.85;
        double maxRadius = enraged ? 20.0 : 16.0;
        if (radius > maxRadius) return;
        int points = (int) (radius * 6);
        for (int i = 0; i < points; i++) {
            double a = i * Math.PI * 2 / points;
            double x = getX() + Math.cos(a) * radius, z = getZ() + Math.sin(a) * radius;
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, SCULK), x, getY() + 0.2, z, 2, 0.1, 0.1, 0.1, 0.15);
            if (i % 3 == 0) level.sendParticles(CYAN, x, getY() + 0.4, z, 1, 0, 0.1, 0, 0);
        }
        for (LivingEntity e : others(level, getBoundingBox().inflate(radius + 1.5, 2.0, radius + 1.5))) {
            double d = horizontalDist(e.position().subtract(position()));
            if (Math.abs(d - radius) > 1.1 || !e.onGround() || !alreadyHit.add(e.getUUID())) continue;
            if (e.hurt(damageSources().mobAttack(this), enraged ? 24.0F : 20.0F)) {
                Vec3 kb = horizontal(e.position().subtract(position())).scale(0.9);
                e.push(kb.x, 0.7, kb.z);
                e.hurtMarked = true;
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            }
        }
    }

    /** One sweep of the back tentacles: hits everything around its front and drags it closer. */
    private void tentacleSweep(ServerLevel level, int t) {
        playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 2.0F, 0.8F + t * 0.01F);
        Vec3 look = horizontal(getLookAngle());
        for (int i = -6; i <= 6; i++) {
            Vec3 dir = look.yRot((float) Math.toRadians(i * 15));
            Vec3 p = position().add(dir.scale(6.5)).add(0, 1.6, 0);
            level.sendParticles(ParticleTypes.SCULK_SOUL, p.x, p.y, p.z, 2, 0.2, 0.2, 0.2, 0.01);
            level.sendParticles(CYAN, p.x, p.y, p.z, 2, 0.3, 0.3, 0.3, 0);
        }
        for (LivingEntity e : others(level, getBoundingBox().inflate(9.0, 3.0, 9.0))) {
            Vec3 to = e.position().subtract(position());
            if (horizontalDist(to) > 9.0 || horizontal(to).dot(look) < -0.2) continue;
            if (e.hurt(damageSources().mobAttack(this), enraged ? 16.0F : 13.0F)) {
                Vec3 pull = horizontal(to).scale(-0.9);
                e.push(pull.x, 0.25, pull.z);
                e.hurtMarked = true;
            }
        }
    }

    /** Sculk spikes burst out of the floor one after another, in a line towards where the target stood. */
    private void spikes(ServerLevel level, LivingEntity target, int t) {
        if (t == 8 && target != null) aimDir = horizontal(target.position().subtract(position()));
        if (t < 10 || t > 36 || t % 2 != 0) return;
        int step = (t - 10) / 2;
        double along = 2.5 + step * 1.6;
        float[] fan = enraged ? new float[]{-22, 0, 22} : new float[]{0};
        for (float angle : fan) {
            Vec3 dir = aimDir.yRot((float) Math.toRadians(angle));
            Vec3 at = position().add(dir.scale(along));
            double y = groundY(level, at);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, SCULK), at.x, y + 0.5, at.z, 25, 0.4, 0.9, 0.4, 0.2);
            level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, at.x, y + 1.2, at.z, 8, 0.3, 1.0, 0.3, 0.02);
            level.sendParticles(CYAN, at.x, y + 1.0, at.z, 6, 0.3, 1.2, 0.3, 0);
            if (step % 2 == 0) level.playSound(null, at.x, y, at.z, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.HOSTILE, 1.6F, 0.5F);
            Vec3 floor = new Vec3(at.x, y, at.z);
            for (LivingEntity e : others(level, new AABB(floor, floor).inflate(1.4, 2.5, 1.4))) {
                if (!alreadyHit.add(e.getUUID())) continue;
                if (e.hurt(damageSources().mobAttack(this), enraged ? 18.0F : 15.0F)) {
                    e.push(0, 0.9, 0);
                    e.hurtMarked = true;
                }
            }
        }
    }

    /** Glowing marks appear under the players; a moment later they burst. Step off the mark. */
    private void souls(ServerLevel level, int t) {
        if (t == 10) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 3.0F, 0.6F);
            int perPlayer = enraged ? 3 : 2;
            for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(32.0),
                    p -> !p.isCreative() && !p.isSpectator())) {
                marks.add(new Vec3(p.getX(), groundY(level, p.position()), p.getZ()));
                for (int i = 1; i < perPlayer; i++) {
                    Vec3 off = new Vec3(random.nextDouble() * 8 - 4, 0, random.nextDouble() * 8 - 4);
                    Vec3 at = p.position().add(off);
                    marks.add(new Vec3(at.x, groundY(level, at), at.z));
                }
            }
        }
        if (t >= 10 && t < 36 && t % 2 == 0) {
            for (Vec3 m : marks) {
                for (int i = 0; i < 12; i++) {
                    double a = i * Math.PI / 6 + t * 0.2;
                    level.sendParticles(CYAN, m.x + Math.cos(a) * 2.0, m.y + 0.15, m.z + Math.sin(a) * 2.0, 1, 0, 0, 0, 0);
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL, m.x, m.y + 0.3, m.z, 1, 0.4, 0.1, 0.4, 0.01);
            }
        }
        if (t == 36) {
            for (Vec3 m : marks) {
                level.playSound(null, m.x, m.y, m.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.2F, 1.4F);
                level.sendParticles(ParticleTypes.SCULK_SOUL, m.x, m.y + 1.0, m.z, 30, 0.6, 1.4, 0.6, 0.08);
                level.sendParticles(ParticleTypes.SONIC_BOOM, m.x, m.y + 1.0, m.z, 1, 0, 0, 0, 0);
                for (LivingEntity e : others(level, new AABB(m, m).inflate(2.0, 3.0, 2.0))) {
                    if (horizontalDist(e.position().subtract(m)) > 2.0) continue;
                    if (e.hurt(damageSources().magic(), enraged ? 22.0F : 18.0F)) {
                        e.push(0, 1.0, 0);
                        e.hurtMarked = true;
                    }
                }
            }
        }
    }

    /** Charges, takes aim at where the target is, then fires a beam along that line a moment later. */
    private void sonicBoom(ServerLevel level, LivingEntity target, int t) {
        Vec3 chest = position().add(0, 4.0, 0).add(horizontal(getLookAngle()).scale(1.5));
        if (t == 2) level.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 3.0F, 0.7F);
        if (t < 18) {
            for (int i = 0; i < 6; i++) {
                Vec3 from = chest.add(random.nextGaussian() * 2.5, random.nextGaussian() * 2.0, random.nextGaussian() * 2.5);
                Vec3 v = chest.subtract(from).scale(0.15);
                level.sendParticles(ParticleTypes.SCULK_SOUL, from.x, from.y, from.z, 0, v.x, v.y, v.z, 1.0);
            }
        }
        if (t == 16 && target != null) {
            aim = target.position().add(0, target.getBbHeight() * 0.5, 0);
        }
        if (t >= 16 && t < 22) {
            Vec3 dir = aim.subtract(chest).normalize();
            for (double d = 2; d < 34; d += 1.5) {
                Vec3 p = chest.add(dir.scale(d));
                level.sendParticles(CYAN, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
        }
        if (t == 22) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 3.5F, 0.6F);
            Vec3 dir = aim.subtract(chest).normalize();
            Vec3 end = chest.add(dir.scale(36));
            for (double d = 1.5; d < 36; d += 1.4) {
                Vec3 p = chest.add(dir.scale(d));
                level.sendParticles(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
            for (LivingEntity e : others(level, new AABB(chest, end).inflate(1.5))) {
                if (e.getBoundingBox().inflate(0.9).clip(chest, end).isEmpty()) continue;
                if (e.hurt(damageSources().sonicBoom(this), enraged ? 34.0F : 28.0F)) {
                    Vec3 kb = dir.scale(1.6);
                    e.push(kb.x, 0.45, kb.z);
                    e.hurtMarked = true;
                }
            }
        }
    }

    private void telegraphCircle(ServerLevel level, Vec3 at, double radius, int t) {
        if (t % 3 != 0) return;
        for (int i = 0; i < 20; i++) {
            double a = i * Math.PI / 10;
            level.sendParticles(CYAN, at.x + Math.cos(a) * radius, at.y + 0.15, at.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
        }
    }

    // ------------------------------------------------------------------ helpers

    private Vec3 front(double distance) {
        Vec3 p = position().add(horizontal(getLookAngle()).scale(distance));
        return new Vec3(p.x, getY(), p.z);
    }

    private List<LivingEntity> others(ServerLevel level, AABB box) {
        return level.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e.isAlive()
                && !(e instanceof SculkGolemEntity) && !(e instanceof Player p && (p.isCreative() || p.isSpectator())));
    }

    private static Vec3 horizontal(Vec3 v) {
        Vec3 h = new Vec3(v.x, 0, v.z);
        return h.lengthSqr() < 1.0E-6 ? new Vec3(0, 0, 1) : h.normalize();
    }

    private static double horizontalDist(Vec3 v) {
        return Math.sqrt(v.x * v.x + v.z * v.z);
    }

    /** The floor under a point, looked for a few blocks above and below the boss's own height. */
    private double groundY(ServerLevel level, Vec3 at) {
        int x = Mth.floor(at.x), z = Mth.floor(at.z);
        int top = Mth.floor(Math.max(at.y, getY())) + 4;
        for (int y = top; y > top - 14; y--) {
            net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) return y + 1;
        }
        return getY();
    }

    // ------------------------------------------------------------------ death and loot

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        bossEvent.removeAllPlayers();
        bossEvent.setVisible(false);
        if (level() instanceof ServerLevel sl) {
            DimensionGenSavedData.get(sl.getServer()).setTowerFloor10BossDefeated(true);
            sl.playSound(null, getX(), getY(), getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 2.0F, 0.9F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 3.0D, getZ(), 4, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 2.0D, getZ(), 100, 2.0D, 2.0D, 2.0D, 0.15D);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        bossEvent.removeAllPlayers();
        bossEvent.setVisible(false);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        spawnAtLocation(new ItemStack(SystemItems.MANA_CRYSTAL_SHARD_S.get(), 15 + random.nextInt(11)));
        spawnAtLocation(new ItemStack(SystemItems.MANA_CRYSTAL_CLUSTER_S_ITEM.get(), 2 + random.nextInt(3)));
        spawnAtLocation(new ItemStack(SystemItems.MANA_CRYSTAL_SHARD_A.get(), 25 + random.nextInt(16)));
        spawnAtLocation(new ItemStack(SystemItems.MANA_CRYSTAL_CLUSTER_A_ITEM.get(), 4 + random.nextInt(4)));
        spawnAtLocation(new ItemStack(Items.ECHO_SHARD, 8));
        spawnAtLocation(new ItemStack(Blocks.SCULK_CATALYST, 3));
        spawnAtLocation(new ItemStack(Blocks.SCULK_SHRIEKER, 2));
        spawnAtLocation(new ItemStack(Items.NETHERITE_SCRAP, 4));
        spawnAtLocation(new ItemStack(Items.NETHER_STAR, 1));
    }

    // ------------------------------------------------------------------ animations

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 6,
                state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
        AnimationController<ColossalSculkWardenEntity> attack = new AnimationController<>(this, "attack", 3, state -> PlayState.STOP);
        for (Attack a : Attack.values()) {
            attack.triggerableAnim(a.name(), RawAnimation.begin().thenPlay(PREFIX + a.anim));
        }
        controllers.add(attack);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
