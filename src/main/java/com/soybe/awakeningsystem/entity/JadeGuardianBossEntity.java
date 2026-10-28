package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class JadeGuardianBossEntity extends Monster {
    private static final EntityDataAccessor<Byte> DATA_ATTACK_STATE =
            SynchedEntityData.defineId(JadeGuardianBossEntity.class, EntityDataSerializers.BYTE);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("awakening_system.boss.jade_guardian.name"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private int jadePalmCooldown = 80;
    private int earthTremorCooldown = 140;
    private int qiBarrierCooldown = 220;
    private int attackAnimationTicks = 0;

    public JadeGuardianBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 600;
        this.setCustomName(Component.translatable("awakening_system.boss.jade_guardian.custom_name"));
        this.setCustomNameVisible(true);
        this.addTag("jade_grotto_boss");
        this.addTag("no_shadow_extraction");
        this.addTag("no_block_destroy");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK_STATE, (byte) 0);
    }

    public byte getAttackState() {
        return this.entityData.get(DATA_ATTACK_STATE);
    }

    public void setAttackState(byte state, int durationTicks) {
        this.entityData.set(DATA_ATTACK_STATE, state);
        this.attackAnimationTicks = durationTicks;
    }

    public int getAttackAnimationTicks() {
        return this.attackAnimationTicks;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 3800.0D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D)
                .add(Attributes.ARMOR, 24.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.attackAnimationTicks > 0) {
            this.attackAnimationTicks--;
            if (this.attackAnimationTicks <= 0) {
                this.entityData.set(DATA_ATTACK_STATE, (byte) 0);
            }
        }

        if (this.level().isClientSide()) {
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.5D,
                        this.getY() + this.random.nextDouble() * 2.5D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.5D,
                        0, 0.03D, 0);
            }
            return;
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            double distSq = this.distanceToSqr(target);

            if (this.jadePalmCooldown > 0) {
                this.jadePalmCooldown--;
            } else if (distSq <= 16.0D) {
                this.jadePalmCooldown = 80;
                this.setAttackState((byte) 1, 20);
                this.level().playSound(null, this.blockPosition(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.HOSTILE, 1.4F, 0.85F);
                target.hurt(this.damageSources().mobAttack(this), 24.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));

                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.2D);
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.2D, target.getZ(), 30, 0.6D, 0.6D, 0.6D, 0.1D);
                }
            }

            if (this.earthTremorCooldown > 0) {
                this.earthTremorCooldown--;
            } else if (distSq <= 120.0D) {
                this.earthTremorCooldown = 140;
                this.setAttackState((byte) 2, 28);
                this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2F, 1.3F);
                this.level().playSound(null, this.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.HOSTILE, 1.5F, 0.7F);

                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.2D, this.getZ(), 10, 2.0D, 0.2D, 2.0D, 0.1D);
                    sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 0.3D, this.getZ(), 60, 3.5D, 0.2D, 3.5D, 0.08D);
                }

                AABB shockwaveBox = this.getBoundingBox().inflate(8.0D, 3.0D, 8.0D);
                List<Player> hitPlayers = this.level().getEntitiesOfClass(Player.class, shockwaveBox, p -> !p.isSpectator());
                for (Player p : hitPlayers) {
                    p.hurt(this.damageSources().mobAttack(this), 14.0F);
                    p.push(0.0D, 0.65D, 0.0D);
                    p.hurtMarked = true;
                }
            }

            if (this.qiBarrierCooldown > 0) {
                this.qiBarrierCooldown--;
            } else if (this.getHealth() < this.getMaxHealth() * 0.6F) {
                this.qiBarrierCooldown = 220;
                this.setAttackState((byte) 3, 30);
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.4F, 1.2F);

                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.5D, this.getZ(), 80, 1.2D, 1.2D, 1.2D, 0.15D);
                    sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.5D, this.getZ(), 3, 0, 0, 0, 0);
                }
            }
        }
    }

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
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide()) {
            ServerLevel sl = (ServerLevel) this.level();

            if (SystemItems.JADE_ANCESTRAL_KEY.isBound()) {
                ItemStack keyStack = new ItemStack(SystemItems.JADE_ANCESTRAL_KEY.get());
                ItemEntity drop = new ItemEntity(sl, this.getX(), this.getY() + 0.5D, this.getZ(), keyStack);
                drop.setDefaultPickUpDelay();
                sl.addFreshEntity(drop);
            }

            if (damageSource.getEntity() instanceof ServerPlayer killer) {
                var data = SystemEvents.data(killer);
                data.addCultivationQi(800);
                data.addCoins(250000);
                data.addExperience(25000);
                AwakeningSystemMod.sync(killer);

                AwakeningSystemMod.notify(killer,
                        "awakening_system.grotto.boss_defeated_title",
                        "awakening_system.grotto.boss_defeated_desc",
                        0xFFFFD700);
            }

            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.5D, this.getZ(), 200, 2.0D, 2.0D, 2.0D, 0.3D);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(), 2, 0, 0, 0, 0);

            this.bossEvent.removeAllPlayers();
        }
    }
}
