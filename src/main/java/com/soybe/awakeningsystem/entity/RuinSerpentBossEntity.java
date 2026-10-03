package com.soybe.awakeningsystem.entity;

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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Jefe de Campo de Teyvat: La Serpiente de las Ruinas (Ruin Serpent).
 * Antigua máquina excavadora de Khaenri'ah que habita en las profundidades de La Sima.
 * Capaz de excavar la roca subterránea, desatar torbellinos de fango lodoso y embestir con su broca perforadora.
 */
public class RuinSerpentBossEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_BURROWED =
            SynchedEntityData.defineId(RuinSerpentBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_STUNNED =
            SynchedEntityData.defineId(RuinSerpentBossEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.literal("§8§lSerpiente de las Ruinas §7- §6Excavador de La Sima"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private int attackCooldown = 40;
    private int stunTimer = 0;
    private int burrowTimer = 0;

    public RuinSerpentBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("§8§lSerpiente de las Ruinas"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        this.xpReward = 800;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4200.0D)
                .add(Attributes.ATTACK_DAMAGE, 32.0D)
                .add(Attributes.ARMOR, 22.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BURROWED, false);
        builder.define(DATA_STUNNED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isBurrowed() {
        return this.entityData.get(DATA_BURROWED);
    }

    public boolean isStunned() {
        return this.entityData.get(DATA_STUNNED);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            if (isStunned()) {
                stunTimer--;
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                if (stunTimer <= 0) {
                    this.entityData.set(DATA_STUNNED, false);
                }
                return;
            }

            if (isBurrowed()) {
                burrowTimer--;
                LivingEntity target = this.getTarget();
                if (target != null && target.isAlive()) {
                    Vec3 tPos = target.position();
                    this.setPos(tPos.x, tPos.y - 3.0D, tPos.z);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, tPos.x, tPos.y + 0.2D, tPos.z, 8, 0.8D, 0.2D, 0.8D, 0.05D);
                    }
                }

                if (burrowTimer <= 0) {
                    // Erupción hacia la superficie
                    this.entityData.set(DATA_BURROWED, false);
                    if (target != null && target.isAlive()) {
                        this.setPos(target.getX(), target.getY(), target.getZ());
                        if (this.level() instanceof ServerLevel sl) {
                            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.5F, 0.7F);
                            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0D, this.getZ(), 4, 1.0D, 0.5D, 1.0D, 0.0D);

                            target.hurt(this.damageSources().mobAttack(this), 28.0F);
                            target.knockback(1.8D, 0, 1.0D);
                            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                        }
                    }
                }
                return;
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                if (attackCooldown > 0) {
                    attackCooldown--;
                } else {
                    executeSerpentAttack(target);
                }
            }
        }
    }

    private void executeSerpentAttack(LivingEntity target) {
        this.attackCooldown = 60;
        if (!(this.level() instanceof ServerLevel sl)) return;

        int choice = this.random.nextInt(2);
        if (choice == 0) {
            // Excavación subterránea
            this.entityData.set(DATA_BURROWED, true);
            this.burrowTimer = 45; // 2.2 segundos bajo tierra
            sl.playSound(null, this.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 2.0F, 0.8F);
            sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 0.2D, this.getZ(), 20, 1.0D, 0.2D, 1.0D, 0.05D);
        } else {
            // Barrido de cola y fango lodoso
            sl.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 2.0F, 0.7F);
            AABB sweepArea = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
            for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, sweepArea, e -> e != this && e.isAlive())) {
                nearby.hurt(this.damageSources().mobAttack(this), 22.0F);
                nearby.knockback(1.2D, nearby.getX() - this.getX(), nearby.getZ() - this.getZ());
                nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isBurrowed()) {
            return false; // Invulnerable bajo tierra
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.0F, 0.7F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (ServerPlayer sp : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
                var data = SystemEvents.data(sp);
                if (isTeyvat) {
                    data.addMora(60000);
                    data.addPrimogems(35);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                    sp.displayClientMessage(Component.literal("§8§l✦ ¡SERPIENTE DE LAS RUINAS DERROTADA! §eHas ganado §6+60,000 Mora §ey §b+35 Protogemas§e."), false);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 24));
                    }
                    sp.displayClientMessage(Component.literal("§8§l✦ ¡SERPIENTE DE LAS RUINAS DERROTADA! §6+24 Monedas de Despertar§e."), false);
                }
            }

            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 25 + random.nextInt(15)));
            }
            this.spawnAtLocation(new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2));
            if (SystemItems.PRITHIVA_TOPAZ_GEMSTONE.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRITHIVA_TOPAZ_GEMSTONE.get(), 1));
            }
            if (SystemItems.CHAOS_CORE.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CORE.get(), 2 + random.nextInt(2)));
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
}
