package com.soybe.awakeningsystem.entity.mutant;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The Mutant Warden (the user's own model), an elite of the Great Tower's upper floors. A towering blind
 * horror with tentacles arching from its back: it roars the first time it finds someone (darkening their
 * sight), and fights with one of its attacks at a time, each played through to the end with its blow landing
 * on the matching beat of the animation:
 * <ul>
 * <li>close: a heavy blow, a slam, a double slam, a ground smash that throws everyone up, a tentacle lash;</li>
 * <li>middle: a barrage of spikes bursting from the ground in a line;</li>
 * <li>far: the warden's sonic boom straight through armour, and a burst of souls.</li>
 * </ul>
 */
public class MutantWardenEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.mutant_warden.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.mutant_warden.walk");

    /** An attack: its animation, how long it lasts and when its blows land (ticks). */
    private enum Move {
        ROAR("roar", 56, 22),
        ATTACK("attack", 30, 14),
        SLAM("slam", 30, 15),
        DOUBLE_SLAM("double_slam", 36, 12, 26),
        GROUND_SMASH("ground_smash", 36, 19),
        TENTACLE("tentacle_attack", 40, 20),
        SPIKES("spike_barrage", 40, 20),
        SONIC_BOOM("sonic_boom", 64, 40),
        SOULS("soul_attack", 64, 34);

        final String anim;
        final int length;
        final int[] hits;

        Move(String anim, int length, int... hits) {
            this.anim = anim;
            this.length = length;
            this.hits = hits;
        }
    }

    private Move move;
    private int moveTick, cooldown = 30;
    private boolean roared;

    public MutantWardenEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 400;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 650.0D)
                .add(Attributes.ATTACK_DAMAGE, 22.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.STEP_HEIGHT, 1.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ── fighting: one move at a time ──

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = getTarget();
        if (move != null) {
            moveTick++;
            getNavigation().stop();
            if (target != null) getLookControl().setLookAt(target, 30.0F, 30.0F);
            for (int h : move.hits) if (moveTick == h) land(move, target);
            if (moveTick >= move.length) {
                move = null;
                cooldown = 18 + random.nextInt(22);
            }
            return;
        }
        if (target == null || !target.isAlive()) return;
        if (!roared) {
            roared = true;
            start(Move.ROAR);
            return;
        }
        double d = distanceTo(target);
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (cooldown > 0) cooldown--;
        if (cooldown <= 0) {
            Move m = pick(d);
            if (m != null) {
                start(m);
                return;
            }
        }
        if (d > 3.2D) getNavigation().moveTo(target, 1.0D);
        else getNavigation().stop();
    }

    private Move pick(double d) {
        int r = random.nextInt(100);
        if (d <= 4.0D) return r < 30 ? Move.ATTACK : r < 50 ? Move.SLAM : r < 68 ? Move.DOUBLE_SLAM : r < 84 ? Move.GROUND_SMASH : Move.TENTACLE;
        if (d <= 8.0D) return r < 45 ? Move.TENTACLE : r < 80 ? Move.SPIKES : Move.GROUND_SMASH;
        if (d <= 22.0D && hasLineOfSight(getTarget())) return r < 55 ? Move.SONIC_BOOM : r < 85 ? Move.SOULS : Move.SPIKES;
        return null;
    }

    private void start(Move m) {
        move = m;
        moveTick = 0;
        triggerAnim("attacks", m.name().toLowerCase());
        if (m == Move.ROAR) playSound(SoundEvents.WARDEN_ROAR, 3.0F, 0.75F);
        if (m == Move.SONIC_BOOM) playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 0.9F);
    }

    /** The blow of a move lands. */
    private void land(Move m, LivingEntity target) {
        if (!(level() instanceof ServerLevel sl)) return;
        float dmg = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        Vec3 look = getLookAngle().multiply(1, 0, 1).normalize();
        switch (m) {
            case ROAR -> {
                for (Player p : sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(24.0D)))
                    p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0));
                sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 3.0D, getZ(), 40, 1.5D, 1.0D, 1.5D, 0.05D);
            }
            case ATTACK, TENTACLE -> {
                double reach = m == Move.TENTACLE ? 7.0D : 4.2D;
                hitCone(sl, look, reach, m == Move.TENTACLE ? dmg * 0.85F : dmg, 0.6D);
                playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 2.0F, m == Move.TENTACLE ? 1.2F : 0.9F);
            }
            case SLAM, DOUBLE_SLAM -> {
                Vec3 at = position().add(look.scale(2.5D));
                smash(sl, at, 3.5D, dmg * (m == Move.DOUBLE_SLAM ? 0.7F : 1.0F), 0.4D);
            }
            case GROUND_SMASH -> smash(sl, position(), 7.0D, dmg * 0.9F, 1.0D);
            case SPIKES -> {
                // spikes burst out of the ground in a line toward the target
                for (int i = 2; i <= 10; i++) {
                    Vec3 p = position().add(look.scale(i));
                    sl.sendParticles(ParticleTypes.SCULK_CHARGE_POP, p.x, p.y + 0.3D, p.z, 6, 0.3D, 0.5D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.CRIT, p.x, p.y + 0.8D, p.z, 4, 0.2D, 0.6D, 0.2D, 0.1D);
                    for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, new AABB(p, p).inflate(1.1D, 2.0D, 1.1D), e -> e != this && e.isAlive())) {
                        e.hurt(damageSources().mobAttack(this), dmg * 0.75F);
                        e.push(0.0D, 0.6D, 0.0D);
                    }
                }
                playSound(SoundEvents.POINTED_DRIPSTONE_LAND, 2.5F, 0.6F);
            }
            case SONIC_BOOM -> {
                if (target == null) return;
                Vec3 from = position().add(0.0D, 2.4D, 0.0D), to = target.getEyePosition();
                Vec3 step = to.subtract(from).normalize();
                for (int i = 1; i < (int) Math.floor(to.subtract(from).length()) + 6; i++) {
                    Vec3 p = from.add(step.scale(i));
                    sl.sendParticles(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
                playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
                if (hasLineOfSight(target) && distanceTo(target) < 24) {
                    target.hurt(damageSources().sonicBoom(this), dmg * 0.9F);
                    target.push(step.x * 1.6D, 0.4D, step.z * 1.6D);
                }
            }
            case SOULS -> {
                // a burst of souls: everyone near is struck, weakened and slowed
                sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 2.5D, getZ(), 120, 6.0D, 1.5D, 6.0D, 0.08D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.0D, getZ(), 60, 5.0D, 0.5D, 5.0D, 0.02D);
                playSound(SoundEvents.SOUL_ESCAPE.value(), 3.0F, 0.6F);
                for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12.0D, 4.0D, 12.0D), e -> e != this && e.isAlive())) {
                    e.hurt(damageSources().magic(), dmg * 0.6F);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                }
            }
        }
    }

    private void hitCone(ServerLevel sl, Vec3 look, double reach, float dmg, double up) {
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(reach, 2.0D, reach), e -> e != this && e.isAlive())) {
            Vec3 to = e.position().subtract(position()).multiply(1, 0, 1);
            if (to.length() > reach + e.getBbWidth() || (to.lengthSqr() > 1 && to.normalize().dot(look) < 0.35D)) continue;
            e.hurt(damageSources().mobAttack(this), dmg);
            Vec3 k = to.lengthSqr() < 1.0E-4 ? look : to.normalize();
            e.push(k.x * 1.1D, up, k.z * 1.1D);
        }
    }

    private void smash(ServerLevel sl, Vec3 at, double radius, float dmg, double up) {
        sl.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.2D, at.z, 3, radius * 0.3D, 0.1D, radius * 0.3D, 0.0D);
        sl.sendParticles(ParticleTypes.SCULK_CHARGE_POP, at.x, at.y + 0.2D, at.z, 40, radius * 0.5D, 0.2D, radius * 0.5D, 0.05D);
        playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 2.5F, 0.6F);
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 0.6F);
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius, 2.5D, radius), e -> e != this && e.isAlive())) {
            if (e.position().distanceTo(at) > radius + 0.5D) continue;
            e.hurt(damageSources().mobAttack(this), dmg);
            Vec3 k = e.position().subtract(at).multiply(1, 0, 1);
            k = k.lengthSqr() < 1.0E-4 ? Vec3.ZERO : k.normalize();
            e.push(k.x * 0.8D, up, k.z * 0.8D);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        return false;                                                       // it only hurts through its moves
    }

    // ── sounds ──
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
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        playSound(SoundEvents.WARDEN_STEP, 1.2F, 0.7F);
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    // ── animations ──
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 6, state ->
                state.setAndContinue(state.isMoving() ? WALK : IDLE)));
        AnimationController<MutantWardenEntity> attacks = new AnimationController<>(this, "attacks", 3, state -> PlayState.STOP);
        for (Move m : Move.values())
            attacks.triggerableAnim(m.name().toLowerCase(), RawAnimation.begin().thenPlay("animation.mutant_warden." + m.anim));
        controllers.add(attacks);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
