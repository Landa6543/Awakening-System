package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Bestia Acorazada de Jade Espiritual (Jade Armored Beast / 灵甲兽) del Reino Murim:
 * Bestia mística de cuatro patas cubierta por un caparazón de Jade Espiritual impenetrable.
 * Embiste como una avalancha de jade rodante contra sus enemigos.
 */
public class JadeArmoredBeastEntity extends Monster {

    private int chargeCooldown = 40;
    private boolean isCharging = false;
    private int chargeDuration = 0;

    public JadeArmoredBeastEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            if (chargeCooldown > 0) chargeCooldown--;

            if (isCharging) {
                chargeDuration--;
                if (chargeDuration <= 0) {
                    isCharging = false;
                } else {
                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.5D, this.getZ(),
                                4, 0.4D, 0.2D, 0.4D, 0.05D);
                    }
                }
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && !isCharging && chargeCooldown <= 0) {
                double distSq = this.distanceToSqr(target);
                if (distSq >= 36.0D && distSq <= 256.0D && this.onGround() && this.hasLineOfSight(target)) {
                    isCharging = true;
                    chargeDuration = 25;
                    chargeCooldown = 140;

                    Vec3 dir = target.position().subtract(this.position()).normalize();
                    this.setDeltaMovement(dir.x * 1.35D, 0.2D, dir.z * 1.35D);
                    this.playSound(com.soybe.awakeningsystem.ModSounds.JADE_BEAST_CHARGE.get(), 1.5F, 1.0F);
                    this.playSound(com.soybe.awakeningsystem.ModSounds.JADE_BEAST_ROAR.get(), 1.6F, 1.0F);
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            if (isCharging) {
                living.knockback(1.2D, Math.sin(Math.toRadians(this.getYRot())), -Math.cos(Math.toRadians(this.getYRot())));
                living.hurt(this.damageSources().mobAttack(this), 6.0F);
                this.playSound(SoundEvents.ANVIL_LAND, 0.8F, 1.4F);
            } else {
                this.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 1.0F, 0.8F);
            }
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt) {
            this.playSound(com.soybe.awakeningsystem.ModSounds.JADE_BEAST_HURT.get(), 1.3F, 1.0F);
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.6D, this.getZ(),
                        8, 0.3D, 0.3D, 0.3D, 0.05D);
            }
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return com.soybe.awakeningsystem.ModSounds.JADE_BEAST_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return com.soybe.awakeningsystem.ModSounds.JADE_BEAST_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return com.soybe.awakeningsystem.ModSounds.JADE_BEAST_DEATH.get();
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        if (SystemItems.RAW_SPIRIT_JADE != null && SystemItems.RAW_SPIRIT_JADE.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.RAW_SPIRIT_JADE.get(), 1 + this.random.nextInt(3)));
        }
        if (SystemItems.JADE_CARAPACE_SCALE != null && SystemItems.JADE_CARAPACE_SCALE.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.JADE_CARAPACE_SCALE.get(), 1 + this.random.nextInt(2)));
        }
        this.spawnAtLocation(new ItemStack(Items.EMERALD, 1 + this.random.nextInt(2)));
        this.spawnAtLocation(SystemItems.coin(140 + this.random.nextInt(70)));
    }
}
