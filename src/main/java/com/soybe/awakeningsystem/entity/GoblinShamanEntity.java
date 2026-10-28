package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Goblin Chamán del Reino Isekai:
 * Líder místico que lanza fuego de maná a distancia y canaliza cánticos de frenesí que fortalecen a su manada.
 */
public class GoblinShamanEntity extends Monster implements RangedAttackMob {

    private int chantCooldown = 120;
    private int shootCooldown = 30;

    public GoblinShamanEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 35;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            if (chantCooldown > 0) chantCooldown--;
            if (shootCooldown > 0) shootCooldown--;

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distSq = this.distanceToSqr(target);

                if (chantCooldown <= 0) {
                    performTribalChant();
                    chantCooldown = 180;
                }

                if (shootCooldown <= 0 && distSq >= 9.0D && distSq <= 576.0D && this.hasLineOfSight(target)) {
                    performRangedAttack(target, 1.0F);
                    shootCooldown = 40 + this.random.nextInt(20);
                }

                if (distSq < 16.0D) {
                    this.getNavigation().moveTo(this.getX() - (target.getX() - this.getX()), this.getY(), this.getZ() - (target.getZ() - this.getZ()), 1.1D);
                } else if (distSq > 100.0D) {
                    this.getNavigation().moveTo(target, 1.0D);
                }
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        Vec3 look = target.position().subtract(this.position()).normalize();
        SmallFireball fireball = new SmallFireball(this.level(), this, look);
        fireball.setPos(this.getX() + look.x * 0.8D, this.getY() + 1.2D, this.getZ() + look.z * 0.8D);
        this.level().addFreshEntity(fireball);
        this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.5F);

        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2D, this.getZ(),
                    8, 0.2D, 0.2D, 0.2D, 0.05D);
        }
    }

    private void performTribalChant() {
        this.playSound(com.soybe.awakeningsystem.ModSounds.GOBLIN_CHANT.get(), 1.5F, 1.0F);

        AABB box = this.getBoundingBox().inflate(12.0D);
        List<LivingEntity> allies = this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e instanceof GoblinScoutEntity || e instanceof GoblinWarriorEntity || e == this);

        for (LivingEntity ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 0));
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 0));
        }

        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 1.2D, this.getZ(),
                    35, 1.2D, 0.8D, 1.2D, 0.1D);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.0D, this.getZ(),
                    15, 0.5D, 0.5D, 0.5D, 0.1D);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return com.soybe.awakeningsystem.ModSounds.GOBLIN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return com.soybe.awakeningsystem.ModSounds.GOBLIN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return com.soybe.awakeningsystem.ModSounds.GOBLIN_DEATH.get();
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        if (SystemItems.GOBLIN_EAR != null && SystemItems.GOBLIN_EAR.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.GOBLIN_EAR.get(), 1 + this.random.nextInt(2)));
        }
        this.spawnAtLocation(new ItemStack(Items.BLAZE_POWDER, 1 + this.random.nextInt(2)));
        this.spawnAtLocation(new ItemStack(Items.EMERALD, 1));
        this.spawnAtLocation(SystemItems.coin(80 + this.random.nextInt(40)));
    }
}
