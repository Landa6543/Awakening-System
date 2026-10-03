package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.world.SeriesStructureBuilder;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BloodPatriarchBossEntity extends Monster {
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("awakening_system.boss.blood_patriarch.name"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private int crimsonWaveCooldown = 80;
    private int bloodMistCooldown = 120;

    public BloodPatriarchBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 450;
        this.setCustomName(Component.translatable("awakening_system.boss.blood_patriarch.custom_name"));
        this.setCustomNameVisible(true);
        this.addTag("blood_sect");
        this.addTag("no_shadow_extraction");
        this.addTag("no_block_destroy");
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 3600.0D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D)
                .add(Attributes.ARMOR, 22.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
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

    private boolean hasActiveTotems() {
        if (!(this.level() instanceof ServerLevel sl)) return false;
        AABB box = this.getBoundingBox().inflate(45.0D, 20.0D, 45.0D);
        return sl.getEntitiesOfClass(BloodQiTotemEntity.class, box, BloodQiTotemEntity::isAlive).size() > 0;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isDeadOrDying() || this.isRemoved()) return false;

        // Si los estandartes siguen activos, el Patriarca recibe 75% menos de daño
        if (hasActiveTotems()) {
            amount *= 0.25F;
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 1.2D, this.getZ(), 12, 0.3D, 0.4D, 0.3D, 0.05D);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 1.0D, this.getZ(), 8, 0.4D, 0.4D, 0.4D, 0.1D);
            }
            this.playSound(SoundEvents.SHIELD_BLOCK, 1.2F, 0.8F);
        }

        boolean damaged = super.hurt(source, amount);
        if (damaged) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
        return damaged;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            // Drenaje de Esencia Vital: cura un 30% del daño infligido
            float healAmount = 6.0F;
            this.heal(healAmount);

            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.8D, this.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.0D);
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, living.getX(), living.getY() + 1.0D, living.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 1.2F, 0.7F);
            }
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (!this.level().isClientSide && this.isAlive() && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            double distSq = this.distanceToSqr(target);

            // 1. Corte en Abanico Carmesí (Área frontal)
            this.crimsonWaveCooldown--;
            if (this.crimsonWaveCooldown <= 0 && distSq <= 100.0D) {
                this.crimsonWaveCooldown = 90 + this.random.nextInt(40);
                triggerCrimsonWave();
            }

            // 2. Niebla Demoníaca y Teletransporte (Fase de Furia cuando tiene menos del 50% de HP)
            if (this.getHealth() < this.getMaxHealth() * 0.5F) {
                this.bloodMistCooldown--;
                if (this.bloodMistCooldown <= 0 && distSq >= 16.0D) {
                    this.bloodMistCooldown = 120 + this.random.nextInt(60);
                    triggerBloodMistBlink(target);
                }
            }
        }
    }

    private void triggerCrimsonWave() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.6F, 0.7F);
        this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.4F, 0.6F);

        AABB waveBox = this.getBoundingBox().inflate(6.0D, 2.0D, 6.0D);
        List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, waveBox,
                e -> e != this && !(e instanceof BloodSectDiscipleEntity) && !(e instanceof BloodQiTotemEntity) && e.isAlive());

        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().mobAttack(this), 22.0F);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
            Vec3 knock = e.position().subtract(this.position()).normalize().scale(1.2D);
            e.setDeltaMovement(knock.x, 0.4D, knock.z);
            e.hasImpulse = true;
        }

        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.2D, this.getZ(), 8, 1.2D, 0.4D, 1.2D, 0.0D);
        sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 1.0D, this.getZ(), 40, 1.5D, 0.6D, 1.5D, 0.1D);
    }

    private void triggerBloodMistBlink(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        // Desvanecerse en niebla
        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.4D, 0.8D, 0.4D, 0.05D);
        sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 1.0D, this.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.05D);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2F, 0.6F);

        // Aparecer a espaldas del jugador
        Vec3 back = target.getViewVector(1.0F).scale(-2.2D);
        double tx = target.getX() + back.x;
        double ty = target.getY();
        double tz = target.getZ() + back.z;

        this.teleportTo(tx, ty, tz);
        this.lookAt(target, 180.0F, 180.0F);

        sl.sendParticles(ParticleTypes.FLASH, tx, ty + 1.0D, tz, 2, 0, 0, 0, 0);
        sl.playSound(null, tx, ty, tz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.4F, 1.2F);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.bossEvent.removeAllPlayers();
        this.bossEvent.setVisible(false);

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.2D, this.getZ(), 3, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.5D, this.getZ(), 80, 1.5D, 1.5D, 1.5D, 0.2D);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.6F, 0.9F);

            // Recompensa legendaria: Manual del Arte Secreto de la Secta de Sangre
            ItemStack relic = SeriesStructureBuilder.makeRelic(Items.WRITTEN_BOOK, 1,
                    "§4§l✦ Manual del Arte Secreto de la Secta de Sangre ✦",
                    "Secta de Sangre - Murim",
                    "Técnica prohibida del Culto Demoníaco. Otorga maestría marcial oscura y desata el Qi Carmesí.");
            sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY() + 0.5D, this.getZ(), relic));

            // Si el asesino fue un jugador, otorgar título y monedas
            if (damageSource.getEntity() instanceof ServerPlayer player) {
                var data = SystemEvents.data(player);
                data.setConqueredBloodSect(true);
                data.addCoins(8000);
                data.setEquippedTitle("blood_sect_conqueror");
                AwakeningSystemMod.sync(player);
                AwakeningSystemMod.notify(player,
                        "awakening_system.blood_sect.conquered_title",
                        "awakening_system.blood_sect.conquered_desc",
                        0xFFCC1122);
            }
        }
    }
}
