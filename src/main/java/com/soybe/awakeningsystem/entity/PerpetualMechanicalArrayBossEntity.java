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

import java.util.ArrayList;
import java.util.List;

/**
 * Jefe Mundial de Inazuma: Matriz Mecánica Perpetua (Perpetual Mechanical Array).
 * Misterioso artefacto bélico de Khaenri'ah capaz de adoptar múltiples configuraciones geométricas.
 * Al descender de cierta salud, entra en modo de defensa y despliega los cuatro Centinelas de las Ruinas.
 */
public class PerpetualMechanicalArrayBossEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_DEFENSE_MODE =
            SynchedEntityData.defineId(PerpetualMechanicalArrayBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_PARALYZED =
            SynchedEntityData.defineId(PerpetualMechanicalArrayBossEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.literal("§6§lMatriz Mecánica Perpetua §7- §eIngenio de Ruinas Estelar"),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private final List<RuinSentinelEntity> activeSentinels = new ArrayList<>();
    private boolean splitTriggered = false;
    private int paralyzeTimer = 0;
    private int attackCooldown = 40;

    public PerpetualMechanicalArrayBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("§6§lMatriz Mecánica Perpetua"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        this.xpReward = 850;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4500.0D)
                .add(Attributes.ATTACK_DAMAGE, 34.0D)
                .add(Attributes.ARMOR, 24.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DEFENSE_MODE, false);
        builder.define(DATA_PARALYZED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isDefenseMode() {
        return this.entityData.get(DATA_DEFENSE_MODE);
    }

    public boolean isParalyzed() {
        return this.entityData.get(DATA_PARALYZED);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            if (isParalyzed()) {
                paralyzeTimer--;
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                if (paralyzeTimer <= 0) {
                    this.entityData.set(DATA_PARALYZED, false);
                }
                return;
            }

            if (isDefenseMode()) {
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                this.activeSentinels.removeIf(s -> !s.isAlive() || s.isRemoved());
                if (this.activeSentinels.isEmpty()) {
                    // Todos los centinelas derrotados: la matriz colapsa aturdida
                    this.entityData.set(DATA_DEFENSE_MODE, false);
                    this.entityData.set(DATA_PARALYZED, true);
                    this.paralyzeTimer = 240; // 12 segundos aturdida

                    if (this.level() instanceof ServerLevel sl) {
                        sl.playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 2.0F, 0.6F);
                        sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.5D, this.getZ(), 5, 0.5D, 0.5D, 0.5D, 0.0D);

                        for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
                            player.displayClientMessage(Component.literal("§6§l✦ [COLAPSO DE MATRIZ] §f¡La Matriz Mecánica ha entrado en estado de colapso y es vulnerable!"), true);
                        }
                    }
                }
                return;
            }

            // Desencadenar división al 50% de HP
            if (!splitTriggered && (this.getHealth() / this.getMaxHealth()) <= 0.55F) {
                triggerSplitDefense();
                return;
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                if (attackCooldown > 0) {
                    attackCooldown--;
                } else {
                    executeArrayAttack(target);
                }
            }
        } else {
            // Partículas de engranajes y núcleo de energía dorada
            if (this.random.nextFloat() < 0.3F) {
                this.level().addParticle(ParticleTypes.CRIT,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.5D,
                        this.getY() + 1.0D + this.random.nextDouble() * 1.5D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.5D,
                        0, 0.05D, 0);
            }
        }
    }

    private void triggerSplitDefense() {
        this.splitTriggered = true;
        this.entityData.set(DATA_DEFENSE_MODE, true);

        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.blockPosition(), SoundEvents.IRON_GOLEM_DEATH, SoundSource.HOSTILE, 2.5F, 0.8F);

        for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
            player.displayClientMessage(Component.literal("§6§l✦ [MODO DEFENSIVO] §f¡La Matriz se ha fragmentado! ¡Destruye a sus Centinelas Ruina!"), true);
        }

        // Invocar los 4 Centinelas (Cruiser, Defender, Destroyer, Scout)
        RuinSentinelEntity.SentinelType[] types = RuinSentinelEntity.SentinelType.values();
        for (int i = 0; i < 4; i++) {
            double angle = (i * Math.PI / 2.0D);
            double dist = 6.0D;
            double sx = this.getX() + Math.cos(angle) * dist;
            double sz = this.getZ() + Math.sin(angle) * dist;
            double sy = this.getY();

            RuinSentinelEntity sentinel = new RuinSentinelEntity(ModEntities.RUIN_SENTINEL.get(), sl);
            sentinel.setSentinelType(types[i % types.length]);
            sentinel.setPos(sx, sy, sz);
            sl.addFreshEntity(sentinel);
            this.activeSentinels.add(sentinel);

            sl.sendParticles(ParticleTypes.EXPLOSION, sx, sy + 1.0D, sz, 2, 0.3D, 0.3D, 0.3D, 0.0D);
        }
    }

    private void executeArrayAttack(LivingEntity target) {
        this.attackCooldown = 50;
        if (!(this.level() instanceof ServerLevel sl)) return;

        int choice = this.random.nextInt(2);
        if (choice == 0) {
            // Cañón láser ocular frontal
            sl.playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 1.4F);
            Vec3 dir = target.position().subtract(this.position()).normalize();
            for (double d = 1.0D; d <= 18.0D; d += 0.8D) {
                Vec3 p = this.position().add(dir.scale(d)).add(0, 1.2D, 0);
                sl.sendParticles(ParticleTypes.FIREWORK, p.x, p.y, p.z, 2, 0.05D, 0.05D, 0.05D, 0.01D);
            }
            if (this.distanceToSqr(target) <= 324.0D) {
                target.hurt(this.damageSources().mobAttack(this), 22.0F);
                target.knockback(0.8D, -dir.x, -dir.z);
            }
        } else {
            // Embestida en forma de espada vertical
            sl.playSound(null, target.blockPosition(), SoundEvents.ANVIL_FALL, SoundSource.HOSTILE, 2.0F, 1.0F);
            sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.5D, target.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);
            target.hurt(this.damageSources().mobAttack(this), 28.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isDefenseMode()) {
            return false; // Totalmente invulnerable durante la fase de centinelas
        }
        if (isParalyzed()) {
            amount *= 2.0F; // Vulnerabilidad crítica al estar colapsada
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
                    data.addMora(65000);
                    data.addPrimogems(35);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                    sp.displayClientMessage(Component.literal("§6§l✦ ¡MATRIZ MECÁNICA PERPETUA DERROTADA! §eHas ganado §6+65,000 Mora §ey §b+35 Protogemas§e."), false);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 24));
                    }
                    sp.displayClientMessage(Component.literal("§6§l✦ ¡MATRIZ MECÁNICA PERPETUA DERROTADA! §6+24 Monedas de Despertar§e."), false);
                }
            }

            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 30));
            }
            this.spawnAtLocation(new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2));
            if (SystemItems.PRITHIVA_TOPAZ_GEMSTONE.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRITHIVA_TOPAZ_GEMSTONE.get(), 1));
            }
            if (SystemItems.CHAOS_CORE.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CORE.get(), 2 + random.nextInt(2)));
            }

            for (RuinSentinelEntity s : activeSentinels) {
                if (s.isAlive()) s.discard();
            }
            activeSentinels.clear();
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
