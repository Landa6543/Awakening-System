package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

import java.util.List;

/**
 * The Skeleton Lord, boss of the Forgotten Catacombs (model from the user's collection, converted to
 * GeckoLib with its own animations).
 * <ul>
 *   <li>It waits on its throne sealed in crystal, as a statue that cannot be hurt.</li>
 *   <li>When someone comes close (or strikes it) the crystal bursts, it laughs, and the fight starts.</li>
 *   <li>Close up: a knockback spell that blasts everything around it away. Further off: a soul bolt that
 *       withers and slows. Below three quarters of its health it raises skeletons (wither skeletons when
 *       badly hurt).</li>
 * </ul>
 */
public class SkeletonLordEntity extends Monster implements GeoEntity {
    public static final byte STATUE = 0, WAKING = 1, AWAKE = 2;
    private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(SkeletonLordEntity.class, EntityDataSerializers.BYTE);

    private static final RawAnimation STATUE_ANIM = RawAnimation.begin().thenLoop("statue");
    private static final RawAnimation LAUGH_ANIM = RawAnimation.begin().thenPlayAndHold("NYEHEHEHE");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation CAST_ANIM = RawAnimation.begin().thenPlay("spell_knockback");

    private static final int WAKE_TICKS = 40, CAST_TICKS = 19, CAST_HIT_AT = 9;
    private static final double WAKE_RANGE = 10.0, BLAST_RANGE = 5.5;
    private static final String MINION_TAG = "skeleton_lord_minion";

    private enum Spell { BLAST, BOLT, SUMMON }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(Component.translatable("entity.awakening_system.skeleton_lord"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(true);

    private int wakeTicks, castTicks, blastCd = 40, boltCd = 80, summonCd = 200;
    private Spell casting;

    public SkeletonLordEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 300;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, STATUE);
    }

    public byte state() {
        return this.entityData.get(DATA_STATE);
    }

    public boolean isAwake() {
        return state() == AWAKE;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return isAwake() && casting == null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return isAwake() && casting == null && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F) {
            @Override
            public boolean canUse() {
                return isAwake() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractSkeleton.class) {
            @Override
            public boolean canUse() {
                return isAwake() && super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return isAwake() && super.canUse();
            }
        });
    }

    // ---------------------------------------------------------------- statue and awakening
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!isAwake()) {
            // the crystal holds: striking it only wakes what is inside
            if (!level().isClientSide && state() == STATUE && source.getEntity() instanceof Player) startWaking();
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return isAwake() && super.isPushable();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    private void startWaking() {
        this.entityData.set(DATA_STATE, WAKING);
        this.wakeTicks = WAKE_TICKS;
        if (level() instanceof ServerLevel sl) {
            sl.playSound(null, blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 2.0F, 0.6F);
            sl.playSound(null, blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0F, 0.5F);
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.5, getZ(), 60, 0.9, 1.6, 0.9, 0.12);
            sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.AMETHYST_BLOCK.defaultBlockState()),
                    getX(), getY() + 2.5, getZ(), 120, 0.9, 1.6, 0.9, 0.2);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) level();
        switch (state()) {
            case STATUE -> {
                getNavigation().stop();
                if (tickCount % 10 == 0) {
                    Player near = level.getNearestPlayer(this, WAKE_RANGE);
                    if (near != null && !near.isCreative() && !near.isSpectator() && hasLineOfSight(near)) startWaking();
                }
            }
            case WAKING -> {
                getNavigation().stop();
                if (wakeTicks == WAKE_TICKS - 8) {
                    // the laugh
                    level.playSound(null, blockPosition(), SoundEvents.SKELETON_AMBIENT, SoundSource.HOSTILE, 2.5F, 0.5F);
                    level.playSound(null, blockPosition(), SoundEvents.EVOKER_CELEBRATE, SoundSource.HOSTILE, 1.5F, 0.6F);
                }
                if (--wakeTicks <= 0) {
                    this.entityData.set(DATA_STATE, AWAKE);
                    level.playSound(null, blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.3F);
                }
            }
            default -> fight(level);
        }
        bossEvent.setProgress(getHealth() / getMaxHealth());
        bossEvent.setVisible(isAwake());
    }

    // ---------------------------------------------------------------- the fight
    private void fight(ServerLevel level) {
        if (blastCd > 0) blastCd--;
        if (boltCd > 0) boltCd--;
        if (summonCd > 0) summonCd--;
        if (casting != null) {
            getNavigation().stop();
            LivingEntity t = getTarget();
            if (t != null) getLookControl().setLookAt(t, 30F, 30F);
            castTicks++;
            if (castTicks == CAST_HIT_AT) release(level, casting);
            if (castTicks >= CAST_TICKS) casting = null;
            return;
        }
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        double d = distanceTo(target);
        if (summonCd <= 0 && getHealth() < getMaxHealth() * 0.75F && minions(level).size() < 6) {
            cast(Spell.SUMMON);
            summonCd = 300;
        } else if (blastCd <= 0 && d < BLAST_RANGE) {
            cast(Spell.BLAST);
            blastCd = 100;   // 5 s: in between it fights hand to hand
        } else if (boltCd <= 0 && d > 6 && d < 22 && hasLineOfSight(target)) {
            cast(Spell.BOLT);
            boltCd = 110;
        }
    }

    private void cast(Spell spell) {
        casting = spell;
        castTicks = 0;
        triggerAnim("attack", "cast");
        playSound(spell == Spell.SUMMON ? SoundEvents.EVOKER_PREPARE_SUMMON : SoundEvents.EVOKER_PREPARE_ATTACK, 1.6F, 0.7F);
    }

    private void release(ServerLevel level, Spell spell) {
        Vec3 c = position();
        float power = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (spell) {
            case BLAST -> {
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(BLAST_RANGE, 2, BLAST_RANGE),
                        e -> e != this && e.isAlive() && !e.getTags().contains(MINION_TAG) && !(e instanceof SkeletonLordEntity))) {
                    if (e.distanceTo(this) > BLAST_RANGE + 1) continue;
                    e.hurt(damageSources().indirectMagic(this, this), power * 1.3F);
                    Vec3 away = e.position().subtract(c).multiply(1, 0, 1);
                    away = away.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : away.normalize();
                    e.push(away.x * 1.8, 0.6, away.z * 1.8);
                    e.hurtMarked = true;
                }
                for (int i = 0; i < 36; i++) {
                    double a = i * Math.PI * 2 / 36;
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x + Math.cos(a) * 1.5, c.y + 0.3, c.z + Math.sin(a) * 1.5, 0, Math.cos(a), 0.05, Math.sin(a), 0.6);
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL, c.x, c.y + 1, c.z, 30, 2.5, 0.5, 2.5, 0.05);
                level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.5, c.z, 2, 0.5, 0.2, 0.5, 0);
                level.playSound(null, blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 2.0F, 0.6F);
                level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0F, 1.4F);
            }
            case BOLT -> {
                LivingEntity t = getTarget();
                if (t == null) return;
                Vec3 from = c.add(0, 3.2, 0), to = t.position().add(0, t.getBbHeight() / 2, 0);
                Vec3 step = to.subtract(from);
                int n = (int) Math.max(4, step.length() * 2);
                for (int i = 0; i <= n; i++) {
                    Vec3 p = from.add(step.scale(i / (double) n));
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.01);
                    if (i % 3 == 0) level.sendParticles(ParticleTypes.SCULK_SOUL, p.x, p.y, p.z, 1, 0, 0, 0, 0.01);
                }
                t.hurt(damageSources().indirectMagic(this, this), power * 0.9F);
                t.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1), this);
                t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
                level.playSound(null, t.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.2F, 0.8F);
            }
            case SUMMON -> {
                boolean desperate = getHealth() < getMaxHealth() * 0.35F;
                for (int i = 0; i < 3; i++) {
                    double a = random.nextDouble() * Math.PI * 2;
                    double x = c.x + Math.cos(a) * 3, z = c.z + Math.sin(a) * 3;
                    AbstractSkeleton s = desperate ? EntityType.WITHER_SKELETON.create(level) : EntityType.SKELETON.create(level);
                    if (s == null) continue;
                    s.moveTo(x, c.y, z, random.nextFloat() * 360F, 0);
                    s.finalizeSpawn(level, level.getCurrentDifficultyAt(s.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                    if (!desperate) s.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
                    for (EquipmentSlot slot : EquipmentSlot.values()) s.setDropChance(slot, 0F);
                    s.addTag(MINION_TAG);
                    s.setPersistenceRequired();
                    if (getTarget() != null) s.setTarget(getTarget());
                    level.addFreshEntity(s);
                    level.sendParticles(ParticleTypes.SOUL, x, c.y + 0.5, z, 15, 0.3, 0.6, 0.3, 0.03);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, x, c.y + 0.2, z, 10, 0.3, 0.2, 0.3, 0.02);
                }
                level.playSound(null, blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 2.0F, 0.5F);
            }
        }
    }

    private List<AbstractSkeleton> minions(ServerLevel level) {
        return level.getEntitiesOfClass(AbstractSkeleton.class, new AABB(blockPosition()).inflate(32), s -> s.isAlive() && s.getTags().contains(MINION_TAG));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) {
            // its skeletons crumble with it
            for (AbstractSkeleton s : minions(level)) s.kill();
            level.sendParticles(ParticleTypes.SOUL, getX(), getY() + 2, getZ(), 80, 1, 1.8, 1, 0.08);
        }
    }

    // ---------------------------------------------------------------- boss bar, saving, sounds
    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("LordState", state() == WAKING ? AWAKE : state());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_STATE, tag.getByte("LordState"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isAwake() ? SoundEvents.WITHER_SKELETON_AMBIENT : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    // ---------------------------------------------------------------- animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 6, state -> switch (state()) {
            case STATUE -> state.setAndContinue(STATUE_ANIM);
            case WAKING -> state.setAndContinue(LAUGH_ANIM);
            default -> state.isMoving() ? state.setAndContinue(getDeltaMovement().horizontalDistanceSqr() > 0.01 ? RUN_ANIM : WALK_ANIM)
                    : state.setAndContinue(IDLE_ANIM);
        }));
        controllers.add(new AnimationController<>(this, "attack", 2, state -> PlayState.STOP).triggerableAnim("cast", CAST_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
