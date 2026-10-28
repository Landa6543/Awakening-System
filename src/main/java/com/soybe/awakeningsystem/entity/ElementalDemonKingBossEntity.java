package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ElementalDemonKingBossEntity extends Monster {

    private final ServerBossEvent bossBar = new ServerBossEvent(
            Component.translatable("awakening_system.boss.demon_king.name"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    private int windGaleCooldown = 80;
    private int stormLightningCooldown = 120;
    private int magmaGeyserCooldown = 160;
    private int frostShieldCooldown = 200;

    public ElementalDemonKingBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 25000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 32.0D)
                .add(Attributes.ARMOR, 24.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isOnFire()) {
            this.clearFire();
        }
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.level().isClientSide()) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 1.5D;
            double py = this.getY() + this.random.nextDouble() * 2.8D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 1.5D;

            float hpPct = this.getHealth() / this.getMaxHealth();
            if (hpPct > 0.5F) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.05D, 0);
                this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, 0, 0.02D, 0);
            } else if (hpPct > 0.2F) {
                this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0.05D, 0);
                this.level().addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, 0, 0.02D, 0);
            } else {
                this.level().addParticle(ParticleTypes.DRAGON_BREATH, px, py, pz, 0, 0.05D, 0);
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.05D, 0);
            }
            return;
        }

        if (this.getTarget() != null && this.getTarget().isAlive()) {
            double distSq = this.distanceToSqr(this.getTarget());
            float hpPct = this.getHealth() / this.getMaxHealth();

            if (this.windGaleCooldown > 0) {
                this.windGaleCooldown--;
            } else if (distSq <= 200.0D) {
                this.windGaleCooldown = 100;
                this.level().playSound(null, this.blockPosition(), SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 1.5F, 0.6F);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.5D, this.getZ(), 20, 2.0D, 0.5D, 2.0D, 0.1D);
                    sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 1.0D, this.getZ(), 60, 4.0D, 1.0D, 4.0D, 0.15D);
                }
                AABB aabb = this.getBoundingBox().inflate(12.0D, 4.0D, 12.0D);
                for (Player p : this.level().getEntitiesOfClass(Player.class, aabb, p -> !p.isSpectator())) {
                    p.hurt(this.damageSources().mobAttack(this), 12.0F);
                    double dx = p.getX() - this.getX();
                    double dz = p.getZ() - this.getZ();
                    p.push(dx * 0.4D, 0.6D, dz * 0.4D);
                }
            }

            if (this.stormLightningCooldown > 0) {
                this.stormLightningCooldown--;
            } else if (distSq <= 250.0D) {
                this.stormLightningCooldown = 130;
                this.level().playSound(null, this.getTarget().blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.0F, 1.0F);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getTarget().getX(), this.getTarget().getY() + 1.0D, this.getTarget().getZ(), 40, 1.0D, 1.5D, 1.0D, 0.2D);
                }
                this.getTarget().hurt(this.damageSources().magic(), 18.0F);
            }

            if (hpPct <= 0.55F) {
                if (this.magmaGeyserCooldown > 0) {
                    this.magmaGeyserCooldown--;
                } else {
                    this.magmaGeyserCooldown = 110;
                    this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 1.2F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5D, this.getZ(), 10, 2.5D, 0.5D, 2.5D, 0.1D);
                        sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0D, this.getZ(), 80, 4.0D, 1.5D, 4.0D, 0.1D);
                    }
                    AABB blastBox = this.getBoundingBox().inflate(10.0D, 3.0D, 10.0D);
                    for (Player p : this.level().getEntitiesOfClass(Player.class, blastBox, p -> !p.isSpectator())) {
                        p.hurt(this.damageSources().magic(), 16.0F);
                        p.setRemainingFireTicks(100);
                    }
                }
            }

            if (hpPct <= 0.55F) {
                if (this.frostShieldCooldown > 0) {
                    this.frostShieldCooldown--;
                } else {
                    this.frostShieldCooldown = 150;
                    this.level().playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.8F, 0.8F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.5D, this.getZ(), 60, 2.0D, 1.5D, 2.0D, 0.1D);
                    }
                    AABB slowBox = this.getBoundingBox().inflate(8.0D, 3.0D, 8.0D);
                    for (Player p : this.level().getEntitiesOfClass(Player.class, slowBox, p -> !p.isSpectator())) {
                        p.setTicksFrozen(140);
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        float hpPct = this.getHealth() / this.getMaxHealth();
        if (hpPct <= 0.5F && hpPct > 0.2F) {
            amount *= 0.8F;
        } else if (hpPct <= 0.2F) {
            amount *= 0.65F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.5D, this.getZ(), 200, 2.0D, 2.0D, 2.0D, 0.3D);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);
        }

        if (SystemItems.ELEMENTAL_DEMON_CROWN.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.ELEMENTAL_DEMON_CROWN.get()));
        }
        if (SystemItems.CHAOS_MANA_CORE.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_MANA_CORE.get(), 2));
        }
        if (SystemItems.ELEMENTAL_CASTLE_KEY.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.ELEMENTAL_CASTLE_KEY.get()));
        }
        this.spawnAtLocation(new ItemStack(Items.EMERALD, 64));
        this.spawnAtLocation(new ItemStack(Items.DIAMOND, 32));
        this.spawnAtLocation(new ItemStack(Items.NETHERITE_INGOT, 8));

        this.level().playSound(null, this.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.HOSTILE, 2.0F, 1.0F);

        List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(80.0D));
        for (ServerPlayer sp : players) {
            AwakeningSystemMod.notify(sp,
                    "awakening_system.castles.demon_king_defeated_title",
                    "awakening_system.castles.demon_king_defeated_desc",
                    0xFFFFD700);
            sp.displayClientMessage(Component.translatable("awakening_system.castles.demon_king_defeated_chat"), false);
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
