package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Jefe de Campo de Teyvat: Regisvid Electro (Electro Regisvine).
 * Titánica planta elemental de relámpago arraigada en las líneas ley electrificadas de Inazuma.
 * Posee un escudo de Corola Electro vulnerable a Pyro y Cryo, ataques de rayo fulminante,
 * rayos láser eléctricos rotativos y azotes de raíz cargada.
 */
public class ElectroRegisvineEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_STUNNED =
            SynchedEntityData.defineId(ElectroRegisvineEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SLAM_TICKS =
            SynchedEntityData.defineId(ElectroRegisvineEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SPIN_TICKS =
            SynchedEntityData.defineId(ElectroRegisvineEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("boss.awakening_system.electro_regisvine.boss_bar"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private float corollaShield = 60.0F;
    private int stunTimer = 0;
    private int attackCooldown = 40;

    public ElectroRegisvineEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable("boss.awakening_system.electro_regisvine.name"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        this.xpReward = 800;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 3200.0D)
                .add(Attributes.ATTACK_DAMAGE, 28.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STUNNED, false);
        builder.define(DATA_SLAM_TICKS, 0);
        builder.define(DATA_SPIN_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    public boolean isStunned() {
        return this.entityData.get(DATA_STUNNED);
    }

    public int getSlamTicks() {
        return this.entityData.get(DATA_SLAM_TICKS);
    }

    public int getSpinTicks() {
        return this.entityData.get(DATA_SPIN_TICKS);
    }

    public boolean isSlamming() {
        return getSlamTicks() > 0;
    }

    public float getSlamProgress(float partialTicks) {
        return Math.max(0.0F, Math.min(1.0F, 1.0F - ((float) getSlamTicks() - partialTicks) / 25.0F));
    }

    public boolean isSpinning() {
        return getSpinTicks() > 0;
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(0, this.getDeltaMovement().y < 0 ? this.getDeltaMovement().y : 0, 0);

        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            if (isStunned()) {
                stunTimer--;
                if (stunTimer <= 0) {
                    this.entityData.set(DATA_STUNNED, false);
                    this.corollaShield = 60.0F;
                    if (this.level() instanceof ServerLevel sl) {
                        sl.playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.0F, 1.2F);
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 3.0D, this.getZ(), 60, 2.0D, 1.5D, 2.0D, 0.15D);
                    }
                }
                return;
            }

            int slam = getSlamTicks();
            if (slam > 0) {
                this.entityData.set(DATA_SLAM_TICKS, slam - 1);
            }
            int spin = getSpinTicks();
            if (spin > 0) {
                this.entityData.set(DATA_SPIN_TICKS, spin - 1);
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && this.distanceToSqr(target) <= 1024.0D) {
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);

                if (attackCooldown > 0) {
                    attackCooldown--;
                } else {
                    chooseAttack(target);
                }
            }
        } else {
            if (this.random.nextFloat() < 0.35F) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 3.0D,
                        this.getY() + 1.0D + this.random.nextDouble() * 4.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 3.0D,
                        0.0D, 0.05D, 0.0D);
            }
        }
    }

    private void chooseAttack(LivingEntity target) {
        double distSq = this.distanceToSqr(target);
        int choice = this.random.nextInt(3);

        if (distSq <= 64.0D && choice == 0) {
            executeHeadSlam(target);
        } else if (distSq <= 100.0D && choice == 1) {
            executeSpinAttack();
        } else {
            executeLightningBarrage(target);
        }
    }

    private void executeHeadSlam(LivingEntity target) {
        this.entityData.set(DATA_SLAM_TICKS, 25);
        this.attackCooldown = 50;

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 1.2F);
            sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.5D, target.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 0.5D, target.getZ(), 40, 1.5D, 0.5D, 1.5D, 0.1D);

            target.hurt(this.damageSources().mobAttack(this), 28.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));

            List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D),
                    e -> e != this && e != target && e.isAlive());
            for (LivingEntity e : nearby) {
                e.hurt(this.damageSources().mobAttack(this), 18.0F);
                e.knockback(1.0D, e.getX() - target.getX(), e.getZ() - target.getZ());
            }
        }
    }

    private void executeSpinAttack() {
        this.entityData.set(DATA_SPIN_TICKS, 35);
        this.attackCooldown = 65;

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 2.5F, 0.8F);

            for (int i = 0; i < 32; i++) {
                double angle = (i / 32.0D) * Math.PI * 2.0D;
                double px = this.getX() + Math.cos(angle) * 5.0D;
                double pz = this.getZ() + Math.sin(angle) * 5.0D;
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, this.getY() + 0.5D, pz, 3, 0.2D, 0.2D, 0.2D, 0.05D);
            }

            List<LivingEntity> inRange = sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0D, 2.0D, 6.0D),
                    e -> e != this && e.isAlive());
            for (LivingEntity e : inRange) {
                e.hurt(this.damageSources().mobAttack(this), 22.0F);
                e.knockback(1.4D, e.getX() - this.getX(), e.getZ() - this.getZ());
            }
        }
    }

    private void executeLightningBarrage(LivingEntity target) {
        this.attackCooldown = 45;

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.8F, 1.4F);

            for (int i = 0; i < 4; i++) {
                double ox = target.getX() + (this.random.nextDouble() - 0.5D) * 4.0D;
                double oz = target.getZ() + (this.random.nextDouble() - 0.5D) * 4.0D;
                BlockPos strikePos = BlockPos.containing(ox, target.getY(), oz);

                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, ox, target.getY() + 0.5D, oz, 20, 0.5D, 1.0D, 0.5D, 0.1D);

                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(strikePos));
                    bolt.setVisualOnly(true);
                    sl.addFreshEntity(bolt);
                }

                if (target.blockPosition().closerThan(strikePos, 3.0D)) {
                    target.hurt(this.damageSources().lightningBolt(), 16.0F);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !isStunned()) {
            boolean isElementalVulnerable = source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                    || source.is(net.minecraft.tags.DamageTypeTags.IS_FREEZING);

            float shieldDamage = isElementalVulnerable ? amount * 2.2F : amount * 0.4F;
            this.corollaShield -= shieldDamage;

            if (this.corollaShield <= 0.0F) {
                this.corollaShield = 0.0F;
                this.entityData.set(DATA_STUNNED, true);
                this.stunTimer = 160;

                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 2.5F, 0.8F);
                    sl.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0F, 0.9F);
                    sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 2.5D, this.getZ(), 2, 0.5D, 0.5D, 0.5D, 0.0D);

                    for (ServerPlayer sp : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
                        sp.displayClientMessage(Component.translatable("boss.awakening_system.electro_regisvine.corolla_broken"), true);
                    }
                }
            }
        }

        if (!isStunned()) {
            amount *= 0.55F;
        }

        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.0F, 0.7F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.0D, this.getZ(), 4, 1.5D, 1.5D, 1.5D, 0.0D);

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (ServerPlayer sp : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
                var data = SystemEvents.data(sp);
                if (isTeyvat) {
                    data.addMora(60000);
                    data.addPrimogems(35);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                    sp.displayClientMessage(Component.translatable("boss.awakening_system.electro_regisvine.defeated_teyvat"), false);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 24));
                    }
                    sp.displayClientMessage(Component.translatable("boss.awakening_system.electro_regisvine.defeated_vanilla"), false);
                }
            }

            dropRegisvineLoot(sl, isTeyvat);
        }
    }

    private void dropRegisvineLoot(ServerLevel sl, boolean isTeyvat) {
        if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 25 + this.random.nextInt(15)));
        }
        this.spawnAtLocation(new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2));

        if (SystemItems.VAJRADA_AMETHYST_GEMSTONE.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.VAJRADA_AMETHYST_GEMSTONE.get(), 1));
        }
        if (SystemItems.LIGHTNING_PRISM.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.LIGHTNING_PRISM.get(), 2 + this.random.nextInt(3)));
        }
        if (SystemItems.VISION_ELECTRO.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.VISION_ELECTRO.get(), 1));
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
}
