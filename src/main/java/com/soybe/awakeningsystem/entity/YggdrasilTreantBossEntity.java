package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.world.SeriesStructureBuilder;
import net.minecraft.core.BlockPos;
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

public class YggdrasilTreantBossEntity extends Monster {
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("awakening_system.boss.yggdrasil_treant.name"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private int rootEntangleCooldown = 100;
    private int seismicShockwaveCooldown = 140;
    private int sapHealingCooldown = 220;

    public YggdrasilTreantBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
        this.setCustomName(Component.translatable("awakening_system.boss.yggdrasil_treant.custom_name"));
        this.setCustomNameVisible(true);
        this.addTag("yggdrasil_boss");
        this.addTag("no_shadow_extraction");
        this.addTag("no_block_destroy");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4000.0D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D)
                .add(Attributes.ARMOR, 24.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 14.0F));
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

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (this.random.nextFloat() < 0.35F) {
                this.level().addParticle(ParticleTypes.GLOW,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.8D,
                        this.getY() + this.random.nextDouble() * 2.8D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.8D,
                        0, 0.02D, 0);
            }
            if (this.random.nextFloat() < 0.25F) {
                this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 2.2D,
                        this.getY() + this.random.nextDouble() * 3.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 2.2D,
                        0, 0, 0);
            }
            return;
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            if (--rootEntangleCooldown <= 0) {
                triggerRootEntangle(target);
                rootEntangleCooldown = 120 + this.random.nextInt(40);
            }

            if (--seismicShockwaveCooldown <= 0) {
                if (this.distanceToSqr(target) <= 10.0D * 10.0D) {
                    triggerSeismicShockwave();
                    seismicShockwaveCooldown = 140 + this.random.nextInt(60);
                }
            }

            if (this.getHealth() < this.getMaxHealth() * 0.55F) {
                if (--sapHealingCooldown <= 0) {
                    triggerSapHealing();
                    sapHealingCooldown = 260 + this.random.nextInt(60);
                }
            }
        }
    }

    private void triggerRootEntangle(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        BlockPos targetPos = target.blockPosition();
        sl.playSound(null, targetPos, SoundEvents.ROOTED_DIRT_PLACE, SoundSource.HOSTILE, 1.6F, 0.8F);
        sl.playSound(null, targetPos, SoundEvents.GRASS_PLACE, SoundSource.HOSTILE, 1.4F, 0.6F);

        for (int i = 0; i < 35; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * 1.4D;
            double oz = (this.random.nextDouble() - 0.5D) * 1.4D;
            sl.sendParticles(ParticleTypes.COMPOSTER, target.getX() + ox, target.getY() + 0.1D, target.getZ() + oz, 1, 0, 0.2D, 0, 0.05D);
            sl.sendParticles(ParticleTypes.GLOW, target.getX() + ox, target.getY() + 0.3D, target.getZ() + oz, 1, 0, 0.1D, 0, 0.02D);
        }

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4, false, true));
        target.hurt(this.damageSources().mobAttack(this), 8.0F);

        if (target instanceof ServerPlayer player) {
            AwakeningSystemMod.notify(player,
                    "awakening_system.treant.root_entangle_title",
                    "awakening_system.treant.root_entangle_desc",
                    0xFF22AA44);
        }
    }

    private void triggerSeismicShockwave() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 1.8F, 0.6F);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2F, 0.8F);

        for (int deg = 0; deg < 360; deg += 12) {
            double rad = Math.toRadians(deg);
            for (double dist = 1.5D; dist <= 7.0D; dist += 1.5D) {
                double px = this.getX() + Math.cos(rad) * dist;
                double pz = this.getZ() + Math.sin(rad) * dist;
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, this.getY() + 0.2D, pz, 1, 0, 0.1D, 0, 0.02D);
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, this.getY() + 0.1D, pz, 1, 0, 0.05D, 0, 0.01D);
            }
        }

        AABB aabb = this.getBoundingBox().inflate(8.0D, 3.0D, 8.0D);
        List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, aabb,
                e -> e != this && e.isAlive() && !e.getTags().contains("yggdrasil_boss"));

        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().mobAttack(this), 14.0F);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, true));
            Vec3 knock = e.position().subtract(this.position()).normalize().scale(1.4D);
            e.setDeltaMovement(knock.x, 0.45D, knock.z);
            e.hasImpulse = true;
        }
    }

    private void triggerSapHealing() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.CHORUS_FLOWER_GROW, SoundSource.HOSTILE, 1.6F, 0.9F);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.HOSTILE, 1.4F, 0.8F);

        this.heal(45.0F);

        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 2.0D, this.getZ(), 40, 1.0D, 1.2D, 1.0D, 0.1D);
        sl.sendParticles(ParticleTypes.GLOW_SQUID_INK, this.getX(), this.getY() + 1.8D, this.getZ(), 30, 0.8D, 1.0D, 0.8D, 0.05D);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity) {
        boolean hurt = super.doHurtTarget(entity);
        if (hurt && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
        }
        return hurt;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.bossEvent.removeAllPlayers();
        this.bossEvent.setVisible(false);

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(), 3, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.8D, this.getZ(), 90, 1.8D, 1.8D, 1.8D, 0.2D);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.6F, 0.9F);

            if (SystemItems.YGGDRASIL_HEART_SEED.isBound()) {
                sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY() + 0.5D, this.getZ(), new ItemStack(SystemItems.YGGDRASIL_HEART_SEED.get())));
            }
            if (SystemItems.YGGDRASIL_TREANT_KEY.isBound()) {
                sl.addFreshEntity(new ItemEntity(sl, this.getX() + 0.3D, this.getY() + 0.5D, this.getZ() + 0.3D, new ItemStack(SystemItems.YGGDRASIL_TREANT_KEY.get())));
            }

            if (damageSource.getEntity() instanceof ServerPlayer player) {
                var data = SystemEvents.data(player);
                data.setConqueredYggdrasilLabyrinth(true);
                data.addCoins(9000);
                data.setEquippedTitle("yggdrasil_guardian");
                AwakeningSystemMod.sync(player);
                AwakeningSystemMod.notify(player,
                        "awakening_system.yggdrasil.conquered_title",
                        "awakening_system.yggdrasil.conquered_desc",
                        0xFF22BB55);
            }
        }
    }
}
