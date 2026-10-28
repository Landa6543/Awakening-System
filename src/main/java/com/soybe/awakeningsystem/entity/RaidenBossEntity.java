package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.artifact.GenshinArtifactItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Jefe Legendario de Teyvat: Shogun Raiden / Baal (Magatsu Mitake Narukami no Mikoto).
 * Arconte Electro de Inazuma con 2 Fases Canónicas:
 * - Fase 1: Shogun Raiden con katana Musou Isshin, teletransportación eléctrica y rayos celestiales.
 * - Fase 2: Forma Marioneta de Calamidad colosal con manos mecánicas, escudo impenetrable y tajos dimensionales.
 */
public class RaidenBossEntity extends Monster {

    public static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(RaidenBossEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> DATA_ATTACKING =
            SynchedEntityData.defineId(RaidenBossEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("boss.awakening_system.raiden.boss_bar"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private int attackCooldown = 40;
    private int ultimateCooldown = 220;
    private int transitionTicks = 0;
    private boolean isTransitioning = false;
    private long lastBgmStartTick = 0;

    public RaidenBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 450;
        this.setCustomName(Component.translatable("boss.awakening_system.raiden.name"));
        this.setCustomNameVisible(true);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SystemItems.ENGULFING_LIGHTNING.get()));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4800.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 28.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, 1);
        builder.define(DATA_ATTACKING, false);
    }

    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    public void setPhase(int phase) {
        this.entityData.set(DATA_PHASE, phase);
        if (phase == 2) {
            bossEvent.setName(Component.translatable("boss.awakening_system.raiden.phase2"));
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.42D);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(24.0D);
            this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.setNoGravity(true);
        } else {
            bossEvent.setName(Component.translatable("boss.awakening_system.raiden.boss_bar"));
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SystemItems.ENGULFING_LIGHTNING.get()));
            this.setNoGravity(false);
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(DATA_ATTACKING, attacking);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isTransitioning) return false;

        boolean hurt = super.hurt(source, amount);
        if (hurt && getPhase() == 1 && this.getHealth() <= this.getMaxHealth() * 0.50F) {
            triggerPhaseTransition();
        }
        return hurt;
    }

    private void triggerPhaseTransition() {
        this.isTransitioning = true;
        this.transitionTicks = 60;
        this.setInvulnerable(true);

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.0F, 0.9F);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.5D, this.getZ(), 80, 2.0D, 2.0D, 2.0D, 0.3D);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (getPhase() == 2) {
                if (this.random.nextInt(2) == 0) {
                    this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                            this.getRandomX(1.4D), this.getY() - 0.2D + this.random.nextDouble() * 0.4D, this.getRandomZ(1.4D),
                            (this.random.nextDouble() - 0.5D) * 0.1D, -0.05D, (this.random.nextDouble() - 0.5D) * 0.1D);
                }
                if (this.random.nextInt(4) == 0) {
                    this.level().addParticle(ParticleTypes.PORTAL,
                            this.getRandomX(0.9D), this.getY() + 0.1D, this.getRandomZ(0.9D),
                            0, -0.1D, 0);
                }
            }
            return;
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        long now = this.level().getGameTime();
        if (now - lastBgmStartTick > 1800L && this.level() instanceof ServerLevel sl) {
            lastBgmStartTick = now;
            for (ServerPlayer sp : sl.players()) {
                if (sp.distanceToSqr(this) < 48.0D * 48.0D) {
                    sp.playNotifySound(ModSounds.RAIDEN_BATTLE_BGM.get(), SoundSource.RECORDS, 0.85F, 1.0F);
                }
            }
        }

        if (getPhase() == 2) {
            this.setNoGravity(true);
            this.fallDistance = 0.0F;

            if (!this.getMainHandItem().isEmpty()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }

            this.getNavigation().stop();

            BlockPos currentPos = this.blockPosition();
            int floorY = currentPos.getY();
            for (int dy = 0; dy <= 12; dy++) {
                BlockPos p = currentPos.below(dy);
                if (!this.level().getBlockState(p).isAir()) {
                    floorY = p.getY() + 1;
                    break;
                }
            }

            double desiredHoverY = floorY + 2.2D;
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                desiredHoverY = Math.max(desiredHoverY, target.getY() + 2.0D);
            }

            double diffY = desiredHoverY - this.getY();
            double vy = Mth.clamp(diffY * 0.18D, -0.32D, 0.32D);

            if (target != null && target.isAlive()) {
                Vec3 toTarget = target.position().subtract(this.position());
                double dist2D = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
                if (dist2D > 2.5D) {
                    Vec3 horiz = new Vec3(toTarget.x, 0, toTarget.z).normalize().scale(0.38D);
                    this.setDeltaMovement(horiz.x, vy, horiz.z);
                } else {
                    this.setDeltaMovement(this.getDeltaMovement().x * 0.7D, vy, this.getDeltaMovement().z * 0.7D);
                }
                this.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, target.getEyePosition());

                if (dist2D <= 3.2D && this.tickCount % 20 == 0) {
                    this.doHurtTarget(target);
                    target.hurt(this.damageSources().mobAttack(this), 24.0F);
                    target.setDeltaMovement(target.getDeltaMovement().add(toTarget.normalize().scale(-0.8D).add(0, 0.35D, 0)));
                }
            } else {
                this.setDeltaMovement(this.getDeltaMovement().x * 0.8D, vy, this.getDeltaMovement().z * 0.8D);
            }
        } else {
            this.setNoGravity(false);
        }

        if (isTransitioning) {
            this.setDeltaMovement(0, 0.05D, 0);
            this.transitionTicks--;

            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.2D, this.getZ(), 20, 1.0D, 1.5D, 1.0D, 0.1D);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.0D, this.getZ(), 10, 0.8D, 1.0D, 0.8D, 0.2D);
            }

            if (this.transitionTicks <= 0) {
                this.isTransitioning = false;
                this.setInvulnerable(false);
                this.setPhase(2);
                this.setHealth(this.getMaxHealth() * 0.50F);

                if (this.level() instanceof ServerLevel sl) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                    if (bolt != null) {
                        bolt.moveTo(this.position());
                        bolt.setVisualOnly(true);
                        sl.addFreshEntity(bolt);
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
                attackCooldown = 50 + this.random.nextInt(30);
                performCombatSkill(target);
            }

            if (ultimateCooldown > 0) {
                ultimateCooldown--;
            } else {
                ultimateCooldown = 200 + this.random.nextInt(60);
                performUltimateSlash(target);
            }
        }
    }

    private void performCombatSkill(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        setAttacking(true);

        if (getPhase() == 1) {
            Vec3 targetLook = target.getLookAngle();
            Vec3 behindPos = target.position().subtract(targetLook.scale(2.0D));

            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.5D, 1.0D, 0.5D, 0.2D);
            this.teleportTo(behindPos.x, target.getY(), behindPos.z);
            sl.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5F, 1.2F);

            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
            if (bolt != null) {
                bolt.moveTo(target.position());
                bolt.setVisualOnly(false);
                sl.addFreshEntity(bolt);
            }
        } else {
            Vec3 targetPos = target.position();
            Vec3 aerialPos = targetPos.add((this.random.nextDouble() - 0.5D) * 4.0D, 2.5D, (this.random.nextDouble() - 0.5D) * 4.0D);
            this.teleportTo(aerialPos.x, aerialPos.y, aerialPos.z);
            this.setDeltaMovement(0, 0.08D, 0);

            sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.0D, this.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.0D, this.getZ(), 60, 1.8D, 1.8D, 1.8D, 0.25D);

            for (int i = 0; i < 3; i++) {
                double ox = (this.random.nextDouble() - 0.5D) * 6.0D;
                double oz = (this.random.nextDouble() - 0.5D) * 6.0D;
                Vec3 p = target.position().add(ox, 0, oz);

                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                if (bolt != null) {
                    bolt.moveTo(p);
                    bolt.setVisualOnly(false);
                    sl.addFreshEntity(bolt);
                }
            }
            sl.playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.8F, 1.0F);
        }

        this.level().getServer().tell(new net.minecraft.server.TickTask(
                this.level().getServer().getTickCount() + 15,
                () -> setAttacking(false)
        ));
    }

    private void performUltimateSlash(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE, 2.0F, 0.8F);
        sl.playSound(null, this.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.5F, 1.4F);

        AABB area = this.getBoundingBox().inflate(10.0D);
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, area)) {
            if (e != this && !(e instanceof RaidenBossEntity)) {
                e.hurt(this.damageSources().mobAttack(this), 28.0F);
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                Vec3 knockback = e.position().subtract(this.position()).normalize().scale(1.8D);
                e.setDeltaMovement(knockback.x, 0.45D, knockback.z);
            }
        }

        for (int i = 0; i < 360; i += 15) {
            double rad = Math.toRadians(i);
            double px = this.getX() + Math.cos(rad) * 6.0D;
            double pz = this.getZ() + Math.sin(rad) * 6.0D;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, px, this.getY() + 1.0D, pz, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, this.getY() + 1.2D, pz, 5, 0.2D, 0.5D, 0.2D, 0.1D);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            if (SystemItems.MUDRA_OF_THE_MALEFIC_GENERAL.isBound()) {
                dropItem(new ItemStack(SystemItems.MUDRA_OF_THE_MALEFIC_GENERAL.get(), 2 + this.random.nextInt(2)));
            }
            if (SystemItems.INTERTWINED_FATE.isBound()) {
                dropItem(new ItemStack(SystemItems.INTERTWINED_FATE.get(), 1 + this.random.nextInt(2)));
            }
            boolean isTeyvat = this.level().dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                dropItem(new ItemStack(SystemItems.PRIMOGEM.get(), 60 + this.random.nextInt(30)));
            }
            if (isTeyvat && SystemItems.MORA.isBound()) {
                dropItem(new ItemStack(SystemItems.MORA.get(), 64));
            } else if (!isTeyvat && SystemItems.AWAKENING_COIN.isBound()) {
                dropItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 30));
            }

            try {
                var items = net.minecraft.core.registries.BuiltInRegistries.ITEM;
                java.util.List<GenshinArtifactItem> arts = new java.util.ArrayList<>();
                for (var it : items) {
                    if (it instanceof GenshinArtifactItem art) {
                        arts.add(art);
                    }
                }
                if (!arts.isEmpty()) {
                    dropItem(new ItemStack(arts.get(this.random.nextInt(arts.size()))));
                }
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error dropping artifact for Raiden Boss: {}", e.getMessage(), e);
            }

            if (SystemItems.TEYVAT_SHRINE_KEY.isBound()) {
                dropItem(new ItemStack(SystemItems.TEYVAT_SHRINE_KEY.get(), 1));
            }

            if (this.level() instanceof ServerLevel sl) {
                for (ServerPlayer sp : sl.players()) {
                    if (sp.distanceToSqr(this) < 64.0D * 64.0D) {
                        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(ModSounds.RAIDEN_BATTLE_BGM.get().getLocation(), SoundSource.RECORDS));
                    }
                }
            }
        }
    }

    private void dropItem(ItemStack stack) {
        ItemEntity it = new ItemEntity(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), stack);
        it.setDefaultPickUpDelay();
        this.level().addFreshEntity(it);
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
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(ModSounds.RAIDEN_BATTLE_BGM.get().getLocation(), SoundSource.RECORDS));
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel sl) {
            for (ServerPlayer sp : sl.players()) {
                if (sp.distanceToSqr(this) < 64.0D * 64.0D) {
                    sp.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(ModSounds.RAIDEN_BATTLE_BGM.get().getLocation(), SoundSource.RECORDS));
                }
            }
        }
    }
}
