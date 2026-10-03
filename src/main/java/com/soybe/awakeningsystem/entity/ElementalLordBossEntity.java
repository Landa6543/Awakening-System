package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.ElementalAltarBlock;
import com.soybe.awakeningsystem.block.ElementalType;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ElementalLordBossEntity extends Monster {

    private static final EntityDataAccessor<Integer> ELEMENT_TYPE =
            SynchedEntityData.defineId(ElementalLordBossEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossBar = new ServerBossEvent(
            Component.literal("§6§lSeñor Elemental"),
            BossEvent.BossBarColor.WHITE,
            BossEvent.BossBarOverlay.NOTCHED_6
    );

    private int attackCooldown = 60;

    public ElementalLordBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5000;
        updateBossBarDetails();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 2500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 24.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ELEMENT_TYPE, 0); // 0: WIND, 1: FROST, 2: FIRE, 3: STORM
    }

    public ElementalType getElementalType() {
        int id = this.entityData.get(ELEMENT_TYPE);
        ElementalType[] values = ElementalType.values();
        return values[Math.floorMod(id, values.length)];
    }

    public void setElementalType(ElementalType type) {
        this.entityData.set(ELEMENT_TYPE, type.ordinal());
        updateBossBarDetails();
    }

    private void updateBossBarDetails() {
        ElementalType type = getElementalType();
        switch (type) {
            case WIND -> {
                this.bossBar.setName(Component.translatable("awakening_system.boss.elemental_lord.wind"));
                this.bossBar.setColor(BossEvent.BossBarColor.WHITE);
            }
            case FROST -> {
                this.bossBar.setName(Component.translatable("awakening_system.boss.elemental_lord.frost"));
                this.bossBar.setColor(BossEvent.BossBarColor.BLUE);
            }
            case FIRE -> {
                this.bossBar.setName(Component.translatable("awakening_system.boss.elemental_lord.fire"));
                this.bossBar.setColor(BossEvent.BossBarColor.RED);
            }
            case STORM -> {
                this.bossBar.setName(Component.translatable("awakening_system.boss.elemental_lord.storm"));
                this.bossBar.setColor(BossEvent.BossBarColor.YELLOW);
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean fireImmune() {
        return getElementalType() == ElementalType.FIRE || super.fireImmune();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (getElementalType() == ElementalType.FIRE && source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (getElementalType() == ElementalType.FIRE && this.isOnFire()) {
            this.clearFire();
        }
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());

        ElementalType type = getElementalType();

        if (this.level().isClientSide()) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 1.2D;
            double py = this.getY() + this.random.nextDouble() * 2.2D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 1.2D;
            this.level().addParticle(type.getParticle(), px, py, pz, 0, 0.04D, 0);
            return;
        }

        // Ataques especiales temáticos del mini-jefe
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        } else if (this.getTarget() != null && this.getTarget().isAlive()) {
            this.attackCooldown = 80;
            executeElementalAbility(type);
        }
    }

    private void executeElementalAbility(ElementalType type) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        switch (type) {
            case WIND -> {
                // Ráfaga cortante y levitación
                sl.playSound(null, this.blockPosition(), SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 1.5F, 0.8F);
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.2D, this.getZ(), 25, 2.0D, 0.5D, 2.0D, 0.1D);
                AABB box = this.getBoundingBox().inflate(10.0D, 3.0D, 10.0D);
                for (Player p : this.level().getEntitiesOfClass(Player.class, box, p -> !p.isSpectator())) {
                    p.hurt(this.damageSources().mobAttack(this), 12.0F);
                    double dx = p.getX() - this.getX();
                    double dz = p.getZ() - this.getZ();
                    p.push(dx * 0.35D, 0.5D, dz * 0.35D);
                    p.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));
                }
            }
            case FROST -> {
                // Congelación glacial y lentitud
                sl.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.8F, 0.7F);
                sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.2D, this.getZ(), 50, 3.0D, 1.0D, 3.0D, 0.1D);
                AABB box = this.getBoundingBox().inflate(8.0D, 3.0D, 8.0D);
                for (Player p : this.level().getEntitiesOfClass(Player.class, box, p -> !p.isSpectator())) {
                    p.hurt(this.damageSources().magic(), 10.0F);
                    p.setTicksFrozen(140);
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                }
            }
            case FIRE -> {
                // Explosión volcánica y quemadura
                sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.4F, 1.3F);
                sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.8D, this.getZ(), 60, 3.0D, 1.0D, 3.0D, 0.15D);
                sl.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.0D, this.getZ(), 20, 1.5D, 0.5D, 1.5D, 0.1D);
                AABB box = this.getBoundingBox().inflate(9.0D, 3.0D, 9.0D);
                for (Player p : this.level().getEntitiesOfClass(Player.class, box, p -> !p.isSpectator())) {
                    p.hurt(this.damageSources().magic(), 14.0F);
                    p.setRemainingFireTicks(120);
                }
            }
            case STORM -> {
                // Saeta de relámpago
                sl.playSound(null, this.getTarget().blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.0F, 1.1F);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getTarget().getX(), this.getTarget().getY() + 1.0D, this.getTarget().getZ(), 40, 1.0D, 1.5D, 1.0D, 0.2D);
                this.getTarget().hurt(this.damageSources().magic(), 15.0F);
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        ElementalType type = getElementalType();

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.2D, this.getZ(), 80, 1.0D, 1.0D, 1.0D, 0.2D);
            sl.sendParticles(type.getParticle(), this.getX(), this.getY() + 1.2D, this.getZ(), 60, 1.5D, 1.0D, 1.5D, 0.1D);

            // Intentar activar automáticamente el altar en su torre cercana
            for (int dx = -25; dx <= 25; dx++) {
                for (int dz = -25; dz <= 25; dz++) {
                    for (int dy = -6; dy <= 6; dy++) {
                        BlockPos ap = this.blockPosition().offset(dx, dy, dz);
                        BlockState st = sl.getBlockState(ap);
                        if (st.is(SystemBlocks.ELEMENTAL_ALTAR.get()) && st.getValue(ElementalAltarBlock.ELEMENT) == type && !st.getValue(ElementalAltarBlock.ACTIVE)) {
                            sl.setBlock(ap, st.setValue(ElementalAltarBlock.ACTIVE, true), 3);
                            sl.playSound(null, ap, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.2F);

                            // Comprobar si los 4 sellos se han desactivado en el complejo
                            List<BlockPos> altars = ElementalAltarBlock.findAltarsInComplex(sl, ap);
                            int activeCount = 0;
                            for (BlockPos p : altars) {
                                BlockState ast = sl.getBlockState(p);
                                if (ast.is(SystemBlocks.ELEMENTAL_ALTAR.get()) && ast.getValue(ElementalAltarBlock.ACTIVE)) {
                                    activeCount++;
                                }
                            }
                            if (activeCount >= 4) {
                                ElementalAltarBlock.disperseCentralBarrier(sl, ap);
                                for (ServerPlayer sp : sl.getEntitiesOfClass(ServerPlayer.class, new AABB(ap).inflate(120.0D))) {
                                    sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
                                    sp.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.2F, 0.8F);
                                    sp.displayClientMessage(Component.translatable("awakening_system.castles.barrier_dispelled"), false);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        // Recompensas
        this.spawnAtLocation(new ItemStack(Items.DIAMOND, 4));
        this.spawnAtLocation(new ItemStack(Items.EMERALD, 16));
        if (SystemItems.AWAKENING_COIN.isBound()) {
            this.spawnAtLocation(SystemItems.coin(32));
        }

        List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(40.0D));
        for (ServerPlayer sp : players) {
            sp.displayClientMessage(Component.translatable("awakening_system.castles.elemental_lord_defeated", this.bossBar.getName().getString()), false);
            sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.2F);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ElementalType", this.entityData.get(ELEMENT_TYPE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ElementalType")) {
            setElementalType(ElementalType.values()[Math.floorMod(compound.getInt("ElementalType"), ElementalType.values().length)]);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }
}
