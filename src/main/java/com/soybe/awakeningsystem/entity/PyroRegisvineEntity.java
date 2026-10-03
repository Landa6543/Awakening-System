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
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Jefe de Campo de Teyvat: Regisvid Pirotécnica (Pyro Regisvine).
 * Titánica planta elemental llameante arraigada en las líneas ley ardientes.
 * Posee un escudo de Corola Pyro vulnerable a Hydro/Agua, letales cabezazos ígneos,
 * ráfagas de proyectiles de fuego y torbellinos abrasadores.
 */
public class PyroRegisvineEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_STUNNED =
            SynchedEntityData.defineId(PyroRegisvineEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SLAM_TICKS =
            SynchedEntityData.defineId(PyroRegisvineEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SPIN_TICKS =
            SynchedEntityData.defineId(PyroRegisvineEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.literal("§c§lRegisvid Pirotécnica §7- §4Flor Llameante Titánica"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private float corollaShield = 55.0F;
    private int stunTimer = 0;
    private int slamCooldown = 90;
    private int spinCooldown = 160;
    private int barrageCooldown = 120;

    public PyroRegisvineEntity(EntityType<? extends PyroRegisvineEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 65;
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

        // Anclado al terreno
        Vec3 curMotion = this.getDeltaMovement();
        this.setDeltaMovement(0.0D, curMotion.y < 0 ? curMotion.y : 0.0D, 0.0D);

        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            this.bossEvent.setVisible(true);

            if (this.slamCooldown > 0) this.slamCooldown--;
            if (this.spinCooldown > 0) this.spinCooldown--;
            if (this.barrageCooldown > 0) this.barrageCooldown--;

            // Partículas ambientales de cenizas ardientes y fuego
            if (this.random.nextFloat() < 0.35F && this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 3.5D,
                        this.getY() + 2.0D + this.random.nextDouble() * 3.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 3.5D,
                        2, 0.1, 0.2, 0.1, 0.02);
            }

            if (isStunned()) {
                this.stunTimer--;
                if (this.stunTimer <= 0) {
                    recoverFromStun();
                }
                return;
            }

            int slamTicks = this.entityData.get(DATA_SLAM_TICKS);
            if (slamTicks > 0) {
                this.entityData.set(DATA_SLAM_TICKS, slamTicks - 1);
                if (slamTicks == 14) {
                    performSlamImpact();
                }
            }

            int spinTicks = this.entityData.get(DATA_SPIN_TICKS);
            if (spinTicks > 0) {
                this.entityData.set(DATA_SPIN_TICKS, spinTicks - 1);
                performSpinTick();
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && !isSlamming() && !isSpinning()) {
                double distSq = this.distanceToSqr(target);

                if (distSq < 144.0D && this.spinCooldown <= 0 && this.random.nextFloat() < 0.4F) {
                    startSpinAttack();
                } else if (distSq < 225.0D && this.slamCooldown <= 0 && this.random.nextFloat() < 0.5F) {
                    startSlamAttack(target);
                } else if (this.barrageCooldown <= 0) {
                    startFireballBarrage(target);
                }
            }
        }
    }

    private void startSlamAttack(LivingEntity target) {
        this.entityData.set(DATA_SLAM_TICKS, 30);
        this.slamCooldown = 110 + this.random.nextInt(40);

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, 0.7F);
            sl.sendParticles(ParticleTypes.LAVA,
                    this.getX(), this.getY() + 4.5D, this.getZ(),
                    30, 1.2, 0.8, 1.2, 0.15);
        }
    }

    private void performSlamImpact() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        double fwd = 3.2D;
        float yawRad = (float) Math.toRadians(this.getYRot());
        double hitX = this.getX() - Math.sin(yawRad) * fwd;
        double hitZ = this.getZ() + Math.cos(yawRad) * fwd;
        double hitY = this.getY();

        sl.playSound(null, hitX, hitY, hitZ,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.5F, 0.85F);
        sl.playSound(null, hitX, hitY, hitZ,
                SoundEvents.LAVA_EXTINGUISH, SoundSource.HOSTILE, 1.8F, 1.2F);

        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, hitX, hitY + 0.5D, hitZ, 1, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.FLAME, hitX, hitY + 0.5D, hitZ, 80, 2.8, 0.6, 2.8, 0.25);
        sl.sendParticles(ParticleTypes.LAVA, hitX, hitY + 0.5D, hitZ, 25, 2.0, 0.5, 2.0, 0.15);

        List<LivingEntity> victims = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(6.5D, 3.0D, 6.5D),
                e -> e != this && e.isAlive()
        );

        for (LivingEntity v : victims) {
            double dSq = v.distanceToSqr(hitX, hitY, hitZ);
            if (dSq < 36.0D) {
                float dmg = 24.0F * (float) (1.0D - Math.sqrt(dSq) / 6.0D);
                v.hurt(this.damageSources().mobAttack(this), Math.max(10.0F, dmg));
                v.setRemainingFireTicks(120);

                Vec3 push = v.position().subtract(hitX, hitY, hitZ).normalize().scale(1.2D);
                v.setDeltaMovement(v.getDeltaMovement().add(push.x, 0.45D, push.z));
                v.hurtMarked = true;
            }
        }
    }

    private void startSpinAttack() {
        this.entityData.set(DATA_SPIN_TICKS, 35);
        this.spinCooldown = 180 + this.random.nextInt(50);

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 2.5F, 0.6F);
        }
    }

    private void performSpinTick() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        double radius = 5.5D;
        int particles = 16;
        for (int i = 0; i < particles; i++) {
            double angle = (2.0 * Math.PI / particles) * i + (this.tickCount * 0.4D);
            double px = this.getX() + Math.cos(angle) * radius;
            double pz = this.getZ() + Math.sin(angle) * radius;
            sl.sendParticles(ParticleTypes.FLAME, px, this.getY() + 0.6D, pz, 1, 0, 0.05, 0, 0.05);
        }

        if (this.tickCount % 5 == 0) {
            List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(5.5D, 1.5D, 5.5D),
                    e -> e != this && e.isAlive()
            );
            for (LivingEntity e : nearby) {
                e.hurt(this.damageSources().mobAttack(this), 14.0F);
                e.setRemainingFireTicks(100);
            }
        }
    }

    private void startFireballBarrage(LivingEntity target) {
        this.barrageCooldown = 130 + this.random.nextInt(35);
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, 1.0F);

        // Disparo de 3 proyectiles de fuego ígneo
        for (int i = -1; i <= 1; i++) {
            Vec3 dir = target.position().subtract(this.getX(), this.getY() + 4.0D, this.getZ()).normalize();
            double spread = i * 0.15D;
            SmallFireball fb = new SmallFireball(this.level(), this, new Vec3(dir.x + spread, dir.y, dir.z + spread));
            fb.setPos(this.getX(), this.getY() + 4.2D, this.getZ());
            this.level().addFreshEntity(fb);
        }
    }

    private void recoverFromStun() {
        setStunned(false);
        this.corollaShield = 55.0F;

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.8F);
            sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 3.0D, this.getZ(),
                    40, 1.0, 1.5, 1.0, 0.2);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !isStunned()) {
            // Escudo de corola vulnerable a daño de agua/Hydro
            boolean isWater = source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)
                    || (source.getEntity() instanceof Player p && (p.isInWaterRainOrBubble() || p.getMainHandItem().is(net.minecraft.world.item.Items.WATER_BUCKET)));
            float shieldDmg = isWater ? amount * 3.5F : amount;
            this.corollaShield -= shieldDmg;

            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 2.5D, this.getZ(),
                        12, 0.8, 0.8, 0.8, 0.1);
            }

            if (this.corollaShield <= 0.0F) {
                setStunned(true);
                this.entityData.set(DATA_SLAM_TICKS, 0);
                this.entityData.set(DATA_SPIN_TICKS, 0);
                this.stunTimer = 160;

                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.5F, 0.6F);
                    sl.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.5D, this.getZ(),
                            50, 1.5, 1.5, 1.5, 0.2);

                    for (Player p : sl.getPlayers(pl -> pl.distanceToSqr(this) < 900.0D)) {
                        p.displayClientMessage(Component.literal("§c§l✦ ¡COROLA PYRO DESTRUIDA! §fLa Regisvid Pirotécnica yace noqueada."), true);
                    }
                }
            }
        }

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

        if (SystemItems.AGNIDUS_AGATE_GEMSTONE.isBound() && this.random.nextFloat() < 0.25F) {
            this.spawnAtLocation(new ItemStack(SystemItems.AGNIDUS_AGATE_GEMSTONE.get(), 1));
        } else if (SystemItems.AGNIDUS_AGATE_CHUNK.isBound() && this.random.nextFloat() < 0.65F) {
            this.spawnAtLocation(new ItemStack(SystemItems.AGNIDUS_AGATE_CHUNK.get(), 1 + this.random.nextInt(2)));
        } else if (SystemItems.AGNIDUS_AGATE_FRAGMENT.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.AGNIDUS_AGATE_FRAGMENT.get(), 2 + this.random.nextInt(2)));
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.8F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.0D, this.getZ(), 2, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 2.0D, this.getZ(), 100, 3.0, 1.5, 3.0, 0.25);

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (Player p : sl.getPlayers(pl -> pl.distanceToSqr(this) < 1200.0D)) {
                if (p instanceof ServerPlayer sp) {
                    var data = SystemEvents.data(sp);
                    if (isTeyvat) {
                        int moraGain = 8000 + sl.random.nextInt(8000);
                        int primoGain = 20 + sl.random.nextInt(15);
                        data.addMora(moraGain);
                        data.addPrimogems(primoGain);
                        com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                        sp.displayClientMessage(Component.literal("§c§l✦ ¡REGISVID PIROTÉCNICA DERROTADA! §e+" + moraGain + " Mora, §b+" + primoGain + " Protogemas§e."), false);
                    } else {
                        int coinGain = 20 + sl.random.nextInt(15);
                        if (SystemItems.AWAKENING_COIN.isBound()) {
                            sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), coinGain));
                        }
                        sp.displayClientMessage(Component.literal("§c§l✦ ¡REGISVID PIROTÉCNICA DERROTADA! §6+" + coinGain + " Monedas de Despertar§e."), false);
                    }
                }
            }
        }
    }
}
