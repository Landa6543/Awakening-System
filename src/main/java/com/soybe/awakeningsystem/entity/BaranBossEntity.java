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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class BaranBossEntity extends Monster {
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("awakening_system.boss.baran.name"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_12
    ).setDarkenScreen(false);

    private int lightningCooldown = 100;
    private int dashCooldown = 80;
    private int stormCooldown = 160;

    public BaranBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 350;
        this.setCustomName(Component.translatable("awakening_system.boss.baran.custom_name"));
        this.setCustomNameVisible(true);
        if (SystemItems.SHADOW_DAGGERS != null && SystemItems.SHADOW_DAGGERS.isBound()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SystemItems.SHADOW_DAGGERS.get()));
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(SystemItems.SHADOW_DAGGERS.get()));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (SystemItems.SHADOW_DAGGERS != null && SystemItems.SHADOW_DAGGERS.isBound()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SystemItems.SHADOW_DAGGERS.get()));
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(SystemItems.SHADOW_DAGGERS.get()));
        }
        return data;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 3800.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 26.0D)
                .add(Attributes.ARMOR, 22.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.FOLLOW_RANGE, 45.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 14.0F));
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

        if (this.level().isClientSide) {
            // Constant electric spark aura around Baran
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 1.0D;
            double py = this.getY() + this.random.nextDouble() * 2.0D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 1.0D;
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.05D, 0);

            if (isShadowSoldier()) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.02D, 0);
            }
        }

        if (this.level() instanceof ServerLevel sl) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distSq = this.distanceToSqr(target);
                boolean enraged = this.getHealth() < this.getMaxHealth() * 0.5F;

                // 1. White Lightning Barrage
                if (lightningCooldown > 0) lightningCooldown--;
                if (lightningCooldown <= 0 && distSq <= 400.0D) {
                    lightningCooldown = enraged ? 70 : 110;
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.5F, 1.2F);

                    int count = enraged ? 4 : 2;
                    for (int i = 0; i < count; i++) {
                        double ox = target.getX() + (this.random.nextDouble() - 0.5D) * 4.0D;
                        double oz = target.getZ() + (this.random.nextDouble() - 0.5D) * 4.0D;
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                        if (bolt != null) {
                            bolt.moveTo(ox, target.getY(), oz);
                            bolt.setVisualOnly(false);
                            sl.addFreshEntity(bolt);
                        }
                    }
                    target.hurt(this.damageSources().lightningBolt(), enraged ? 16.0F : 12.0F);
                }

                // 2. Thunder Dash (Instant lightning dash toward target)
                if (dashCooldown > 0) dashCooldown--;
                if (dashCooldown <= 0 && distSq >= 25.0D && distSq <= 196.0D) {
                    dashCooldown = enraged ? 60 : 90;
                    Vec3 dir = target.position().subtract(this.position()).normalize();
                    this.setDeltaMovement(dir.x * 1.8D, 0.3D, dir.z * 1.8D);
                    this.hasImpulse = true;

                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.4F, 1.6F);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.0D, this.getZ(), 40, 0.6D, 0.8D, 0.6D, 0.2D);

                    target.hurt(this.damageSources().mobAttack(this), enraged ? 22.0F : 18.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                }

                // 3. Storm Shockwave (360 electric burst)
                if (stormCooldown > 0) stormCooldown--;
                if (stormCooldown <= 0 && distSq <= 36.0D) {
                    stormCooldown = enraged ? 100 : 150;
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2F, 1.3F);

                    List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D),
                            e -> e != this && (!isShadowSoldier() || (!e.getTags().contains("shadow_soldier") && !(e instanceof Player))));
                    for (LivingEntity victim : nearby) {
                        victim.hurt(this.damageSources().magic(), enraged ? 18.0F : 14.0F);
                        Vec3 push = victim.position().subtract(this.position()).normalize().scale(1.5D);
                        victim.setDeltaMovement(push.x, 0.5D, push.z);
                    }
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.0D, this.getZ(), 60, 1.5D, 0.8D, 1.5D, 0.2D);
                    sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.0D, this.getZ(), 1, 0, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (this.level() instanceof ServerLevel sl && !isShadowSoldier()) {
            // Drops
            if (SystemItems.DEMON_MONARCH_RING != null && SystemItems.DEMON_MONARCH_RING.isBound()) {
                sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY() + 0.5D, this.getZ(),
                        new ItemStack(SystemItems.DEMON_MONARCH_RING.get())));
            }
            if (SystemItems.RUNE_STONE_BLOODTHIRST != null && SystemItems.RUNE_STONE_BLOODTHIRST.isBound()) {
                sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY() + 0.5D, this.getZ(),
                        new ItemStack(SystemItems.RUNE_STONE_BLOODTHIRST.get())));
            }
            if (SystemItems.AWAKENING_COIN != null && SystemItems.AWAKENING_COIN.isBound()) {
                sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY() + 0.5D, this.getZ(),
                        SystemItems.coin(65)));
            }

            // Spawn Shadow Extraction Soul (Exclusivo para Monarca de las Sombras)
            Player killer = (damageSource.getEntity() instanceof Player p) ? p : null;
            if (killer != null) {
                com.soybe.awakeningsystem.SystemData kData = killer.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                if (kData != null && ((kData.hasHunterClass() && "shadow_monarch".equals(kData.hunterClass())) || kData.skills().containsKey("unique_absolute_extraction"))) {
                    ShadowSoulEntity soul = new ShadowSoulEntity(sl, this.getX(), this.getY(), this.getZ(), this, killer.getUUID());
                    soul.setMobName(Component.translatable("entity.awakening_system.baran_boss").getString());
                    sl.addFreshEntity(soul);
                }
            }

            if (killer instanceof ServerPlayer sp) {
                AwakeningSystemMod.notify(sp, "awakening_system.notification.baran_defeated.title", "awakening_system.notification.baran_defeated.desc", 0xFF5599FF);
            }
        }
    }
}
