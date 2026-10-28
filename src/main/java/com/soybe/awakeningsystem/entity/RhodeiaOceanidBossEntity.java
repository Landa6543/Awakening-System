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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Jefe Mundial de Liyue: Rhodeia, la Oceánida de las Aguas Claras (Rhodeia of Loch).
 * Soberana hidromántica que combate invocando mímicos acuáticos,
 * desatando torbellinos de agua y bombas de sumersión.
 */
public class RhodeiaOceanidBossEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_SHIELDED =
            SynchedEntityData.defineId(RhodeiaOceanidBossEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("boss.awakening_system.rhodeia.boss_bar"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private final List<WaterMimicEntity> activeMimics = new ArrayList<>();
    private int summonCooldown = 60;
    private int attackCooldown = 40;

    public RhodeiaOceanidBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setCustomName(Component.translatable("boss.awakening_system.rhodeia.name"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        this.setNoGravity(true);
        this.xpReward = 850;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4000.0D)
                .add(Attributes.ATTACK_DAMAGE, 28.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.FLYING_SPEED, 0.45D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SHIELDED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new OceanidHoverGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isShielded() {
        return this.entityData.get(DATA_SHIELDED);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            this.activeMimics.removeIf(m -> !m.isAlive() || m.isRemoved());
            boolean hasMimics = !this.activeMimics.isEmpty();
            this.entityData.set(DATA_SHIELDED, hasMimics);

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                if (activeMimics.isEmpty()) {
                    if (summonCooldown > 0) {
                        summonCooldown--;
                    } else {
                        summonMimicWave();
                        summonCooldown = 180;
                    }
                }

                if (attackCooldown > 0) {
                    attackCooldown--;
                } else {
                    executeHydroAttack(target);
                }
            }
        } else {
            if (this.random.nextFloat() < 0.35F) {
                this.level().addParticle(ParticleTypes.FALLING_WATER,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.5D,
                        this.getY() + this.random.nextDouble() * 2.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.5D,
                        0, -0.05D, 0);
            }
        }
    }

    private void summonMimicWave() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 2.0F, 0.8F);

        for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
            player.displayClientMessage(Component.translatable("boss.awakening_system.rhodeia.quote"), true);
        }

        for (int i = 0; i < 3; i++) {
            double angle = (i * 2.0D * Math.PI / 3.0D) + (random.nextDouble() * 0.4D);
            double dist = 4.0D + random.nextDouble() * 3.0D;
            double mx = this.getX() + Math.cos(angle) * dist;
            double mz = this.getZ() + Math.sin(angle) * dist;
            double my = this.getY() - 1.0D;

            WaterMimicEntity mimic = new WaterMimicEntity(ModEntities.WATER_MIMIC.get(), sl);
            mimic.setPos(mx, my, mz);
            sl.addFreshEntity(mimic);
            this.activeMimics.add(mimic);

            sl.sendParticles(ParticleTypes.SPLASH, mx, my + 0.5D, mz, 25, 0.5D, 0.5D, 0.5D, 0.1D);
        }
    }

    private void executeHydroAttack(LivingEntity target) {
        this.attackCooldown = 50;
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.HOSTILE, 2.0F, 1.2F);

        Vec3 targetPos = target.position();
        sl.sendParticles(ParticleTypes.SPLASH, targetPos.x, targetPos.y + 0.2D, targetPos.z, 50, 1.5D, 0.5D, 1.5D, 0.15D);

        AABB hitArea = new AABB(targetPos.x - 3.0D, targetPos.y - 1.0D, targetPos.z - 3.0D,
                targetPos.x + 3.0D, targetPos.y + 3.0D, targetPos.z + 3.0D);

        for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, hitArea, e -> e != this && !activeMimics.contains(e) && e.isAlive())) {
            nearby.hurt(this.damageSources().indirectMagic(this, this), 20.0F);
            nearby.knockback(1.2D, nearby.getX() - this.getX(), nearby.getZ() - this.getZ());
            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isShielded()) {
            amount *= 0.15F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 3.0F, 0.6F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0D, this.getZ(), 2, 0.5D, 0.5D, 0.5D, 0.0D);

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (ServerPlayer sp : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
                var data = SystemEvents.data(sp);
                if (isTeyvat) {
                    data.addMora(60000);
                    data.addPrimogems(35);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                    sp.displayClientMessage(Component.translatable("boss.awakening_system.rhodeia.defeated_teyvat"), false);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 24));
                    }
                    sp.displayClientMessage(Component.translatable("boss.awakening_system.rhodeia.defeated_vanilla"), false);
                }
            }

            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 30));
            }
            this.spawnAtLocation(new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2));
            if (SystemItems.VARUNADA_LAZURITE_GEMSTONE.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.VARUNADA_LAZURITE_GEMSTONE.get(), 1));
            }
            if (SystemItems.VARUNADA_LAZURITE_CHUNK.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.VARUNADA_LAZURITE_CHUNK.get(), 2 + random.nextInt(2)));
            }
            if (SystemItems.VISION_HYDRO.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.VISION_HYDRO.get(), 1));
            }

            for (WaterMimicEntity m : activeMimics) {
                if (m.isAlive()) m.discard();
            }
            activeMimics.clear();
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

    static class OceanidHoverGoal extends Goal {
        private final RhodeiaOceanidBossEntity oceanid;

        public OceanidHoverGoal(RhodeiaOceanidBossEntity oceanid) {
            this.oceanid = oceanid;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = oceanid.getTarget();
            if (target != null) {
                double targetY = target.getY() + 4.5D;
                double dx = target.getX() - oceanid.getX();
                double dz = target.getZ() - oceanid.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);

                double desiredDist = 7.0D;
                double speed = 0.25D;

                if (dist > desiredDist + 1.0D) {
                    oceanid.setDeltaMovement(oceanid.getDeltaMovement().add(dx / dist * speed, (targetY - oceanid.getY()) * 0.05D, dz / dist * speed));
                } else if (dist < desiredDist - 1.0D) {
                    oceanid.setDeltaMovement(oceanid.getDeltaMovement().add(-dx / dist * speed, (targetY - oceanid.getY()) * 0.05D, -dz / dist * speed));
                }
            } else {
                oceanid.setDeltaMovement(oceanid.getDeltaMovement().scale(0.9D));
            }
        }
    }
}
