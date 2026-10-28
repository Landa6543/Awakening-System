package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
 * Cultivador Hereje del Culto Demoníaco (Demonic Cult Rogue Swordsman) del Reino Murim:
 * Espadachín marcial con técnicas de Qinggong y tajos de Qi carmesí a distancia.
 */
public class DemonicCultivatorEntity extends Monster {

    private int slashCooldown = 40;
    private int dashCooldown = 50;

    public DemonicCultivatorEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 85;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            if (slashCooldown > 0) slashCooldown--;
            if (dashCooldown > 0) dashCooldown--;

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distSq = this.distanceToSqr(target);

                if (slashCooldown <= 0 && distSq >= 16.0D && distSq <= 400.0D && this.hasLineOfSight(target)) {
                    performSwordQiSlash(target);
                    slashCooldown = 70 + this.random.nextInt(30);
                }
            }
        }
    }

    private void performSwordQiSlash(LivingEntity target) {
        this.playSound(com.soybe.awakeningsystem.ModSounds.CULTIVATOR_SLASH.get(), 1.4F, 1.0F);
        Vec3 dir = target.position().add(0, target.getEyeHeight() * 0.5D, 0).subtract(this.position().add(0, 1.2D, 0)).normalize();

        DemonicSwordQiEntity qi = new DemonicSwordQiEntity(this.level(), this);
        qi.setPos(this.getX() + dir.x * 0.8D, this.getY() + 1.2D, this.getZ() + dir.z * 0.8D);
        qi.shoot(dir.x, dir.y, dir.z, 1.4F, 0.5F);
        this.level().addFreshEntity(qi);

        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 1.2D, this.getZ(),
                    12, 0.4D, 0.4D, 0.4D, 0.05D);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean flag = super.doHurtTarget(target);
        if (flag) {
            this.swing(InteractionHand.MAIN_HAND);
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 1, 0, 0, 0, 0);
            }
        }
        return flag;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);

        if (hurt && !this.level().isClientSide && dashCooldown <= 0) {
            dashCooldown = 90;
            LivingEntity target = this.getTarget();
            float dashAngle = this.getYRot() + (this.random.nextBoolean() ? 85.0F : -85.0F);
            double dx = -Math.sin(Math.toRadians(dashAngle)) * 0.9D;
            double dz = Math.cos(Math.toRadians(dashAngle)) * 0.9D;
            this.setDeltaMovement(dx, 0.25D, dz);
            this.playSound(com.soybe.awakeningsystem.ModSounds.CULTIVATOR_DASH.get(), 1.4F, 1.1F);

            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.8D, this.getZ(),
                        15, 0.3D, 0.4D, 0.3D, 0.02D);
            }
        }

        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return com.soybe.awakeningsystem.ModSounds.CULTIVATOR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return com.soybe.awakeningsystem.ModSounds.CULTIVATOR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return com.soybe.awakeningsystem.ModSounds.CULTIVATOR_DEATH.get();
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        if (SystemItems.SKY_DEMON_SWORD != null && SystemItems.SKY_DEMON_SWORD.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.SKY_DEMON_SWORD.get(), 1));
        } else {
            this.spawnAtLocation(new ItemStack(Items.IRON_SWORD, 1));
        }
        this.spawnAtLocation(new ItemStack(Items.BLAZE_POWDER, 1 + this.random.nextInt(2)));
        this.spawnAtLocation(new ItemStack(Items.BOOK, 1));
        this.spawnAtLocation(SystemItems.coin(150 + this.random.nextInt(75)));
    }
}
