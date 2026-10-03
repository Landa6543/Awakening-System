package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Jefe de Campo de Teyvat: Regisvid Criogénica (Cryo Regisvine).
 * Gigantesca megaflora elemental congelada nacida de las líneas ley.
 * Cuenta con un escudo de Corola Cryo rompible con Pyro, letales cabezazos congelantes,
 * ráfagas de proyectiles de escarcha y barridos en torbellino de 360 grados.
 */
public class CryoRegisvineEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_STUNNED =
            SynchedEntityData.defineId(CryoRegisvineEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SLAM_TICKS =
            SynchedEntityData.defineId(CryoRegisvineEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SPIN_TICKS =
            SynchedEntityData.defineId(CryoRegisvineEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.literal("§b§lRegisvid Criogénica §7- §3Flor Criopurpúrea Titánica"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private float corollaShield = 50.0F;
    private int stunTimer = 0;
    private int slamCooldown = 100;
    private int spinCooldown = 180;
    private int barrageCooldown = 140;

    public CryoRegisvineEntity(EntityType<? extends CryoRegisvineEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 60;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 3200.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 28.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.001D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
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
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 40.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public boolean isStunned() {
        return this.entityData.get(DATA_STUNNED);
    }

    public void setStunned(boolean stunned) {
        this.entityData.set(DATA_STUNNED, stunned);
    }

    public boolean isSlamming() {
        return this.entityData.get(DATA_SLAM_TICKS) > 0;
    }

    public float getSlamProgress(float partialTick) {
        int ticks = this.entityData.get(DATA_SLAM_TICKS);
        if (ticks <= 0) return 0.0F;
        float elapsed = 30.0F - (ticks - partialTick);
        return Math.max(0.0F, Math.min(1.0F, elapsed / 30.0F));
    }

    public boolean isSpinning() {
        return this.entityData.get(DATA_SPIN_TICKS) > 0;
    }

    @Override
    public void tick() {
        super.tick();

        // Anclado al terreno (no se desplaza por empujes)
        Vec3 curMotion = this.getDeltaMovement();
        this.setDeltaMovement(0.0D, curMotion.y < 0 ? curMotion.y : 0.0D, 0.0D);

        if (!this.level().isClientSide) {
            // Actualización de la barra de jefe
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            this.bossEvent.setVisible(true);

            // Reducir cooldowns
            if (this.slamCooldown > 0) this.slamCooldown--;
            if (this.spinCooldown > 0) this.spinCooldown--;
            if (this.barrageCooldown > 0) this.barrageCooldown--;

            // Manejo de estado aturdido
            if (isStunned()) {
                this.stunTimer--;
                if (this.stunTimer <= 0) {
                    recoverFromStun();
                }
                return; // Si está aturdida, no realiza ataques
            }

            // Manejo de animación de cabezazo sísmico
            int slamTicks = this.entityData.get(DATA_SLAM_TICKS);
            if (slamTicks > 0) {
                this.entityData.set(DATA_SLAM_TICKS, slamTicks - 1);
                if (slamTicks == 14) {
                    performSlamImpact();
                }
            }

            // Manejo de animación de giro en 360 grados
            int spinTicks = this.entityData.get(DATA_SPIN_TICKS);
            if (spinTicks > 0) {
                this.entityData.set(DATA_SPIN_TICKS, spinTicks - 1);
                if (spinTicks % 6 == 0) {
                    performSpinHit();
                }
            }

            // IA de combate y selección de ataques
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && !isSlamming() && !isSpinning()) {
                double distSqr = this.distanceToSqr(target);

                if (distSqr <= 36.0D && this.spinCooldown <= 0) {
                    // Objetivo a corta distancia: Látigo giratorio 360 grados
                    startSpinAttack();
                } else if (distSqr <= 100.0D && this.slamCooldown <= 0) {
                    // Distancia media: Cabezazo masivo al suelo
                    startSlamAttack();
                } else if (distSqr >= 36.0D && distSqr <= 1024.0D && this.barrageCooldown <= 0) {
                    // Distancia larga: Ráfaga de proyectiles de escarcha teledirigidos
                    launchHoarfrostBarrage(target);
                }
            }
        }
    }

    private void startSlamAttack() {
        this.entityData.set(DATA_SLAM_TICKS, 30);
        this.slamCooldown = 130 + this.random.nextInt(40);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.8F, 0.6F);
    }

    private void performSlamImpact() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        Vec3 look = this.getLookAngle();
        double impactX = this.getX() + look.x * 3.5D;
        double impactY = this.getY();
        double impactZ = this.getZ() + look.z * 3.5D;

        sl.playSound(null, impactX, impactY, impactZ,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.7F);
        sl.playSound(null, impactX, impactY, impactZ,
                SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.2F, 0.8F);

        sl.sendParticles(ParticleTypes.EXPLOSION, impactX, impactY + 0.5D, impactZ, 2, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, impactX, impactY + 0.5D, impactZ, 60, 2.5, 0.5, 2.5, 0.15);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                impactX, impactY + 0.5D, impactZ, 45, 2.0, 0.5, 2.0, 0.2);

        List<LivingEntity> victims = sl.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(impactX - 4.5D, impactY - 1.0D, impactZ - 4.5D,
                        impactX + 4.5D, impactY + 3.0D, impactZ + 4.5D));

        for (LivingEntity v : victims) {
            if (v != this) {
                v.hurt(this.damageSources().mobAttack(this), 24.0F);
                v.setDeltaMovement(0.0D, 0.75D, 0.0D);
                v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3));
                v.setTicksFrozen(v.getTicksFrozen() + 160);
            }
        }
    }

    private void startSpinAttack() {
        this.entityData.set(DATA_SPIN_TICKS, 36);
        this.spinCooldown = 180 + this.random.nextInt(60);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 2.0F, 0.5F);
    }

    private void performSpinHit() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.2D, this.getZ(), 4, 1.2, 0.2, 1.2, 0.0);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.0D, this.getZ(), 20, 3.0, 0.5, 3.0, 0.1);

        List<LivingEntity> victims = sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.5D, 1.5D, 3.5D));
        for (LivingEntity v : victims) {
            if (v != this) {
                v.hurt(this.damageSources().mobAttack(this), 14.0F);
                Vec3 push = v.position().subtract(this.position()).normalize().scale(0.8D);
                v.setDeltaMovement(push.x, 0.35D, push.z);
                v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            }
        }
    }

    private void launchHoarfrostBarrage(LivingEntity target) {
        this.barrageCooldown = 160 + this.random.nextInt(50);
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.getX(), this.getY() + 3.0D, this.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.5F, 1.3F);

        int count = 4 + this.random.nextInt(3);
        for (int i = 0; i < count; i++) {
            HoarfrostShardEntity shard = new HoarfrostShardEntity(this.level(), this, target);
            double spawnX = this.getX() + (this.random.nextDouble() - 0.5D) * 2.0D;
            double spawnY = this.getY() + 3.2D + this.random.nextDouble() * 0.8D;
            double spawnZ = this.getZ() + (this.random.nextDouble() - 0.5D) * 2.0D;
            shard.setPos(spawnX, spawnY, spawnZ);

            double dx = target.getX() - spawnX;
            double dy = target.getEyeY() - spawnY + 1.5D;
            double dz = target.getZ() - spawnZ;
            shard.shoot(dx, dy + 2.0D, dz, 0.9F + this.random.nextFloat() * 0.4F, 10.0F);
            sl.addFreshEntity(shard);
        }
    }

    private void recoverFromStun() {
        setStunned(false);
        this.corollaShield = 50.0F;

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.8F, 1.4F);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_PLACE, SoundSource.HOSTILE, 2.0F, 0.7F);

            // Onda de choque Cryo expansiva al reincorporarse
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0D, this.getZ(), 2, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.0D, this.getZ(), 80, 4.0, 1.0, 4.0, 0.2);

            List<LivingEntity> victims = sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.5D));
            for (LivingEntity v : victims) {
                if (v != this) {
                    v.hurt(this.damageSources().mobAttack(this), 12.0F);
                    Vec3 push = v.position().subtract(this.position()).normalize().scale(1.2D);
                    v.setDeltaMovement(push.x, 0.5D, push.z);
                    v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !isStunned()) {
            boolean isFire = source.is(DamageTypeTags.IS_FIRE) || (source.getEntity() != null && source.getEntity().isOnFire());
            float shieldDmg = isFire ? amount * 3.0F : amount;
            this.corollaShield -= shieldDmg;

            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 2.5D, this.getZ(),
                        12, 0.8, 0.8, 0.8, 0.1);
            }

            if (this.corollaShield <= 0.0F) {
                // ¡Corola destruida! Entra en estado noqueado
                setStunned(true);
                this.entityData.set(DATA_SLAM_TICKS, 0);
                this.entityData.set(DATA_SPIN_TICKS, 0);
                this.stunTimer = 160; // 8 segundos de vulnerabilidad extrema

                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.5F, 0.6F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ITEM_BREAK, SoundSource.HOSTILE, 2.0F, 0.5F);
                    sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, this.getX(), this.getY() + 1.5D, this.getZ(),
                            50, 1.5, 1.5, 1.5, 0.2);

                    for (Player p : sl.getPlayers(pl -> pl.distanceToSqr(this) < 900.0D)) {
                        p.displayClientMessage(Component.literal("§b§l✦ ¡COROLA CRYO DESTRUIDA! §fLa Regisvid Criogénica yace desplomada."), true);
                    }
                }
            }
        }

        // Si está aturdida recibe daño crítico incrementado (1.8x)
        if (isStunned()) {
            return super.hurt(source, amount * 1.8F);
        }

        return super.hurt(source, amount);
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
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GRASS_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_EXPLODE.value();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        // Drops de Núcleo de Escarcha y Jade Shivada con física nativa
        if (SystemItems.HOARFROST_CORE.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.HOARFROST_CORE.get(), 1 + this.random.nextInt(3)));
        }
        if (SystemItems.SHIVADA_JADE_GEMSTONE.isBound() && this.random.nextFloat() < 0.25F) {
            this.spawnAtLocation(new ItemStack(SystemItems.SHIVADA_JADE_GEMSTONE.get(), 1));
        } else if (SystemItems.SHIVADA_JADE_CHUNK.isBound() && this.random.nextFloat() < 0.65F) {
            this.spawnAtLocation(new ItemStack(SystemItems.SHIVADA_JADE_CHUNK.get(), 1 + this.random.nextInt(2)));
        } else if (SystemItems.SHIVADA_JADE_FRAGMENT.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.SHIVADA_JADE_FRAGMENT.get(), 2 + this.random.nextInt(2)));
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.8F);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.5F, 0.5F);

            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.0D, this.getZ(), 2, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 2.0D, this.getZ(), 100, 3.0, 1.5, 3.0, 0.25);

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            // Recompensas a los jugadores cercanos
            for (Player p : sl.getPlayers(pl -> pl.distanceToSqr(this) < 1200.0D)) {
                if (p instanceof ServerPlayer sp) {
                    var data = SystemEvents.data(sp);
                    if (isTeyvat) {
                        int moraGain = 8000 + sl.random.nextInt(8000);
                        int primoGain = 20 + sl.random.nextInt(15);
                        data.addMora(moraGain);
                        data.addPrimogems(primoGain);
                        com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                        sp.displayClientMessage(Component.literal("§b§l✦ ¡REGISVID CRIOGÉNICA PURGADA! §e+" + moraGain + " Mora, §b+" + primoGain + " Protogemas§e."), false);
                    } else {
                        int coinGain = 20 + sl.random.nextInt(15);
                        if (SystemItems.AWAKENING_COIN.isBound()) {
                            sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), coinGain));
                        }
                        sp.displayClientMessage(Component.literal("§b§l✦ ¡REGISVID CRIOGÉNICA PURGADA! §6+" + coinGain + " Monedas de Despertar§e."), false);
                    }
                }
            }
        }
    }
}
