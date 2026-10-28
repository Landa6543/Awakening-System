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

/**
 * Goblin Guerrero del Reino Isekai:
 * Robusto, armado con maza y escudo improvisado que bloquea impactos.
 */
public class GoblinWarriorEntity extends Monster {

    public GoblinWarriorEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD) && this.random.nextFloat() < 0.30F) {
            this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.2F);
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0D, this.getZ(),
                        10, 0.3D, 0.3D, 0.3D, 0.05D);
            }
            amount *= 0.3F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            this.playSound(com.soybe.awakeningsystem.ModSounds.GOBLIN_WARRIOR_GRUNT.get(), 1.0F, 1.0F);
            this.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 0.9F);
            living.knockback(0.4D, Math.sin(Math.toRadians(this.getYRot())), -Math.cos(Math.toRadians(this.getYRot())));
        }
        return hurt;
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
            this.spawnAtLocation(new ItemStack(SystemItems.GOBLIN_EAR.get(), 1 + this.random.nextInt(3)));
        }
        this.spawnAtLocation(new ItemStack(Items.IRON_INGOT, 1));
        this.spawnAtLocation(new ItemStack(Items.COOKED_BEEF, 1 + this.random.nextInt(2)));
        this.spawnAtLocation(SystemItems.coin(40 + this.random.nextInt(30)));
    }
}
