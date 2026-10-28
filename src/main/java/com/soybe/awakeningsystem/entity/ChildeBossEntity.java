package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.artifact.GenshinArtifactItem;
import com.soybe.awakeningsystem.block.SystemBlocks;
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
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Jefe Legendario de Teyvat: Tartaglia / Childe (Undécimo de los Heraldos de Fatui).
 * Presenta 2 Fases Canónicas de Combate en la Casa Dorada de Liyue:
 * - Fase 1: Hidromancia (Visión Hydro con arco, combo de cortes rápidos de agua y marca de Riptide).
 * - Fase 2: Legado del Mal (Engaño Electro desatado, teletransportación relámpago y Lanzas del Juicio de Trueno).
 */
public class ChildeBossEntity extends Monster {

    public static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(ChildeBossEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM =
            SynchedEntityData.defineId(ChildeBossEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("boss.awakening_system.childe.boss_bar"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private int attackCooldown = 40;
    private int ultimateCooldown = 200;
    private int transitionTicks = 0;
    private boolean isTransitioning = false;
    private long lastBgmStartTick = 0;

    public ChildeBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 350;
        this.setCustomName(Component.translatable("boss.awakening_system.childe.name"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 26.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, 1);
        builder.define(DATA_ATTACK_ANIM, 0);
    }

    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    public void setPhase(int phase) {
        this.entityData.set(DATA_PHASE, phase);
        if (phase == 2) {
            bossEvent.setName(Component.translatable("boss.awakening_system.childe.phase2"));
            bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.38D);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(22.0D);
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
        } else {
            bossEvent.setName(Component.translatable("boss.awakening_system.childe.boss_bar"));
            bossEvent.setColor(BossEvent.BossBarColor.BLUE);
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.35D, false));
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
        if (hurt && getPhase() == 1 && this.getHealth() <= this.getMaxHealth() * 0.45F) {
            triggerPhaseTransition();
        }
        return hurt;
    }

    private void triggerPhaseTransition() {
        this.isTransitioning = true;
        this.transitionTicks = 50;
        this.setInvulnerable(true);

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.2D, this.getZ(), 80, 2.0D, 1.5D, 2.0D, 0.15D);
            sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.5D, this.getZ(), 4, 0.5D, 0.5D, 0.5D, 0.0D);
            sl.playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.8F, 1.2F);
            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 1.0F);

            List<Player> nearby = sl.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0D));
            for (Player p : nearby) {
                Vec3 push = p.position().subtract(this.position()).normalize().scale(1.8D).add(0, 0.4D, 0);
                p.setDeltaMovement(push);
                p.hurtMarked = true;
                p.displayClientMessage(Component.translatable("boss.awakening_system.childe.phase2"), false);
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.level().isClientSide) {
            if (getPhase() == 1) {
                if (this.random.nextInt(3) == 0) {
                    this.level().addParticle(ParticleTypes.FALLING_WATER, this.getRandomX(0.6D), this.getY() + 0.8D + this.random.nextDouble() * 0.8D, this.getRandomZ(0.6D), 0, 0.05D, 0);
                }
            } else {
                if (this.random.nextInt(2) == 0) {
                    this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getRandomX(0.7D), this.getY() + 0.5D + this.random.nextDouble() * 1.2D, this.getRandomZ(0.7D), (this.random.nextDouble() - 0.5D) * 0.1D, 0.05D, (this.random.nextDouble() - 0.5D) * 0.1D);
                }
            }
            return;
        }

        long now = this.level().getGameTime();
        if (now - lastBgmStartTick > 1800L && this.level() instanceof ServerLevel sl) {
            lastBgmStartTick = now;
            for (ServerPlayer sp : sl.players()) {
                if (sp.distanceToSqr(this) < 48.0D * 48.0D) {
                    sp.playNotifySound(ModSounds.CHILDE_BATTLE_BGM.get(), SoundSource.RECORDS, 0.85F, 1.0F);
                }
            }
        }

        if (isTransitioning) {
            transitionTicks--;
            this.setDeltaMovement(0, 0.05D, 0);
            if (transitionTicks <= 0) {
                isTransitioning = false;
                this.setInvulnerable(false);
                setPhase(2);
                this.setHealth(this.getMaxHealth() * 0.85F);
                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE, 2.0F, 1.4F);
                }
            }
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        double distSq = this.distanceToSqr(target);

        if (getPhase() == 1) {
            attackCooldown--;
            if (attackCooldown <= 0 && distSq > 16.0D && distSq < 400.0D) {
                attackCooldown = 80;
                fireHydroArrows(target);
            }
        } else {
            attackCooldown--;
            ultimateCooldown--;

            if (attackCooldown <= 0 && distSq > 20.0D) {
                attackCooldown = 70;
                thunderBlink(target);
            }

            if (ultimateCooldown <= 0) {
                ultimateCooldown = 180;
                castJudgementOfThunder(target);
            }
        }
    }

    private void fireHydroArrows(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        this.playSound(SoundEvents.ARROW_SHOOT, 1.2F, 1.3F);

        for (int i = 0; i < 3; i++) {
            Arrow arrow = new Arrow(this.level(), this, new ItemStack(Items.ARROW), null);
            double dx = target.getX() - this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
            double dy = target.getY(0.5D) - this.getY(0.6D);
            double dz = target.getZ() - this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;
            arrow.shoot(dx, dy, dz, 1.8F, 3.0F);
            arrow.setBaseDamage(6.0D);
            arrow.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
            this.level().addFreshEntity(arrow);
        }
        sl.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.2D, this.getZ(), 25, 0.4D, 0.4D, 0.4D, 0.1D);
    }

    private void thunderBlink(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        Vec3 look = target.getLookAngle().normalize();
        Vec3 behind = target.position().subtract(look.scale(2.0D));

        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.4D, 0.8D, 0.4D, 0.15D);
        sl.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2F, 1.5F);

        this.teleportTo(behind.x, target.getY(), behind.z);

        sl.sendParticles(ParticleTypes.FLASH, behind.x, target.getY() + 1.0D, behind.z, 2, 0.2D, 0.4D, 0.2D, 0.0D);
        sl.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.5F, 1.4F);

        target.hurt(this.damageSources().mobAttack(this), 18.0F);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
    }

    private void castJudgementOfThunder(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        this.playSound(SoundEvents.TRIDENT_THUNDER.value(), 1.8F, 1.1F);
        if (target instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.translatable("boss.awakening_system.childe.phase3"), true);
        }

        int[][] offsets = {{-3, 0}, {3, 0}, {0, -3}, {0, 3}};
        for (int[] off : offsets) {
            Vec3 strikePos = target.position().add(off[0], 0, off[1]);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, strikePos.x, strikePos.y + 0.5D, strikePos.z, 40, 0.5D, 2.0D, 0.5D, 0.2D);
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, strikePos.x, strikePos.y + 0.2D, strikePos.z, 2, 0.4D, 0.1D, 0.4D, 0.05D);

            sl.playSound(null, strikePos.x, strikePos.y, strikePos.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.2F, 1.4F);

            List<LivingEntity> hit = sl.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D), e -> e != this);
            for (LivingEntity e : hit) {
                e.hurt(this.damageSources().mobAttack(this), 16.0F);
                e.setRemainingFireTicks(60);
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity) {
        boolean success = super.doHurtTarget(entity);
        if (success && entity instanceof LivingEntity target) {
            if (getPhase() == 1) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.3D, 0.5D, 0.3D, 0.1D);
                    sl.playSound(null, target.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.HOSTILE, 1.2F, 1.2F);
                }
            } else {
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.1D);
                    sl.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 0.8F, 1.8F);
                }
            }
        }
        return success;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.PLAYER_DEATH, SoundSource.HOSTILE, 1.5F, 0.9F);
            for (Player p : sl.players()) {
                if (p.distanceToSqr(this) < 2500.0D) {
                    p.displayClientMessage(Component.translatable("boss.awakening_system.childe.defeat"), false);
                }
            }

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            if (isTeyvat) {
                spawnItem(sl, new ItemStack(SystemItems.PRIMOGEM.get(), 100));
            }

            if (isTeyvat) {
                if (SystemItems.MORA_BUNDLE.isBound()) {
                    spawnItem(sl, new ItemStack(SystemItems.MORA_BUNDLE.get(), 2));
                } else {
                    spawnItem(sl, new ItemStack(SystemItems.MORA.get(), 64));
                }
            } else {
                if (SystemItems.AWAKENING_COIN.isBound()) {
                    spawnItem(sl, new ItemStack(SystemItems.AWAKENING_COIN.get(), 24));
                }
            }

            if (SystemItems.INTERTWINED_FATE.isBound()) {
                spawnItem(sl, new ItemStack(SystemItems.INTERTWINED_FATE.get(), 1 + this.random.nextInt(2)));
            }

            var tartagliaPlushie = SystemBlocks.PLUSHIE_BLOCKS.get("tartagliaplushie");
            if (tartagliaPlushie != null && tartagliaPlushie.isBound()) {
                spawnItem(sl, new ItemStack(tartagliaPlushie.get(), 1));
            }

            ItemStack artifact = createRandomGenshinArtifact();
            if (!artifact.isEmpty()) {
                spawnItem(sl, artifact);
            }

            if (SystemItems.TEYVAT_SHRINE_KEY.isBound()) {
                spawnItem(sl, new ItemStack(SystemItems.TEYVAT_SHRINE_KEY.get(), 1));
            }

            for (ServerPlayer sp : sl.players()) {
                if (sp.distanceToSqr(this) < 64.0D * 64.0D) {
                    sp.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(ModSounds.CHILDE_BATTLE_BGM.get().getLocation(), SoundSource.RECORDS));
                }
            }
        }
    }

    private void spawnItem(ServerLevel level, ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity item = new ItemEntity(level, this.getX(), this.getY() + 0.5D, this.getZ(), stack);
            item.setDeltaMovement(this.random.nextGaussian() * 0.05D, 0.25D, this.random.nextGaussian() * 0.05D);
            level.addFreshEntity(item);
        }
    }

    private ItemStack createRandomGenshinArtifact() {
        try {
            var items = net.minecraft.core.registries.BuiltInRegistries.ITEM;
            List<GenshinArtifactItem> arts = new ArrayList<>();
            for (var it : items) {
                if (it instanceof GenshinArtifactItem art) {
                    arts.add(art);
                }
            }
            if (!arts.isEmpty()) {
                GenshinArtifactItem chosen = arts.get(this.random.nextInt(arts.size()));
                return new ItemStack(chosen);
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.debug("Error choosing artifact drop for Childe: {}", e.getMessage(), e);
        }
        return ItemStack.EMPTY;
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
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(ModSounds.CHILDE_BATTLE_BGM.get().getLocation(), SoundSource.RECORDS));
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel sl) {
            for (ServerPlayer sp : sl.players()) {
                if (sp.distanceToSqr(this) < 64.0D * 64.0D) {
                    sp.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(ModSounds.CHILDE_BATTLE_BGM.get().getLocation(), SoundSource.RECORDS));
                }
            }
        }
    }
}
