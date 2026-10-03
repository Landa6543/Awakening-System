package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class IgrisBossEntity extends Monster {
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("awakening_system.boss.igris.name"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private int thrustCooldown = 60;
    private int whirlwindCooldown = 120;
    private int guardCooldown = 180;
    private int guardTicks = 0;
    private boolean isGuarding = false;

    public IgrisBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 250;
        this.setCustomName(Component.translatable("awakening_system.boss.igris.name"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 3500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 24.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (SystemItems.GIGANTIC_GREATSWORD != null && SystemItems.GIGANTIC_GREATSWORD.isBound()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SystemItems.GIGANTIC_GREATSWORD.get()));
        }
        return data;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> target != null && !this.isShadowSoldier()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                target -> this.isShadowSoldier() && target != null && !target.getTags().contains("shadow_soldier")));
    }

    public boolean isShadowSoldier() {
        return this.getTags().contains("shadow_soldier");
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!isShadowSoldier()) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (!isShadowSoldier()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isShadowSoldier()) {
            // Visual particles for Shadow Commander
            if (this.level().isClientSide) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
                double py = this.getY() + this.random.nextDouble() * 1.8D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.02D, 0);
                this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.01D, 0);
            }
        }

        if (this.level() instanceof ServerLevel sl) {
            LivingEntity target = this.getTarget();

            // Guard stance counter
            if (guardTicks > 0) {
                guardTicks--;
                if (guardTicks == 0) isGuarding = false;
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 1.2D, this.getZ(), 5, 0.4D, 0.4D, 0.4D, 0.05D);
            }

            if (target != null && target.isAlive()) {
                double distSq = this.distanceToSqr(target);
                boolean enraged = this.getHealth() < this.getMaxHealth() * 0.5F;

                // 1. Supersonic Thrust (Dash attack)
                if (thrustCooldown > 0) thrustCooldown--;
                if (thrustCooldown <= 0 && distSq >= 16.0D && distSq <= 144.0D) {
                    thrustCooldown = enraged ? 50 : 80;
                    Vec3 dir = target.position().subtract(this.position()).normalize();
                    this.setDeltaMovement(dir.x * 1.6D, 0.25D, dir.z * 1.6D);
                    this.hasImpulse = true;

                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.5F, 0.6F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.6F, 1.8F);
                    sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.0D, this.getZ(), 1, 0, 0, 0, 0);

                    target.hurt(this.damageSources().mobAttack(this), enraged ? 20.0F : 16.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                }

                // 2. Whirlwind 360° Slash
                if (whirlwindCooldown > 0) whirlwindCooldown--;
                if (whirlwindCooldown <= 0 && distSq <= 25.0D) {
                    whirlwindCooldown = enraged ? 80 : 120;
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.8F, 0.5F);

                    List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.5D),
                            e -> e != this && (!isShadowSoldier() || (!e.getTags().contains("shadow_soldier") && !(e instanceof Player))));
                    for (LivingEntity victim : nearby) {
                        victim.hurt(this.damageSources().mobAttack(this), enraged ? 18.0F : 14.0F);
                        Vec3 push = victim.position().subtract(this.position()).normalize().scale(1.2D);
                        victim.setDeltaMovement(push.x, 0.4D, push.z);
                    }
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.0D, this.getZ(), 8, 1.0D, 0.2D, 1.0D, 0.05D);
                    sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 1.2D, 0.5D, 1.2D, 0.1D);
                }

                // 3. Defensive Guard
                if (guardCooldown > 0) guardCooldown--;
                if (guardCooldown <= 0 && this.getHealth() < this.getMaxHealth() * 0.7F && this.random.nextFloat() < 0.3F) {
                    guardCooldown = 200;
                    guardTicks = 60; // 3 seconds
                    isGuarding = true;
                    this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 2));
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.5F, 0.8F);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isGuarding && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount *= 0.2F;
            this.playSound(SoundEvents.SHIELD_BLOCK, 1.2F, 1.0F);
            if (source.getEntity() instanceof LivingEntity attacker && attacker != this) {
                attacker.hurt(this.damageSources().thorns(this), 6.0F);
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (this.level() instanceof ServerLevel sl && !isShadowSoldier()) {
            // Drops
            if (SystemItems.RUNE_STONE_RULERS_AUTHORITY != null && SystemItems.RUNE_STONE_RULERS_AUTHORITY.isBound()) {
                sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY() + 0.5D, this.getZ(),
                        new ItemStack(SystemItems.RUNE_STONE_RULERS_AUTHORITY.get())));
            }
            if (SystemItems.AWAKENING_COIN != null && SystemItems.AWAKENING_COIN.isBound()) {
                sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY() + 0.5D, this.getZ(),
                        SystemItems.coin(45)));
            }

            // Spawn Shadow Extraction Soul (Exclusivo para Monarca de las Sombras)
            Player killer = (damageSource.getEntity() instanceof Player p) ? p : null;
            if (killer != null) {
                com.soybe.awakeningsystem.SystemData kData = killer.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                if (kData != null && ((kData.hasHunterClass() && "shadow_monarch".equals(kData.hunterClass())) || kData.skills().containsKey("unique_absolute_extraction"))) {
                    ShadowSoulEntity soul = new ShadowSoulEntity(sl, this.getX(), this.getY(), this.getZ(), this, killer.getUUID());
                    soul.setMobName(Component.translatable("awakening_system.mob.igris_crimson_knight").getString());
                    sl.addFreshEntity(soul);
                }
            }

            if (killer instanceof ServerPlayer sp) {
                AwakeningSystemMod.notify(sp, "awakening_system.notification.igris_defeated.title", "awakening_system.notification.igris_defeated.desc", 0xFFFF5555);
            }
        }
    }
}
