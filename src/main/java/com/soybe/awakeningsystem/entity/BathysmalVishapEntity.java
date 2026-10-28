package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Dragarto de las Profundidades Primigenio (Primordial Bathysmal Vishap).
 * Voraz reptil ancestral adaptado a las sombras de Enkanomiya y La Sima.
 * Dispara orbes de agua condensada que drenan la energía del objetivo.
 */
public class BathysmalVishapEntity extends Monster {

    private int spitCooldown = 45;

    public BathysmalVishapEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable("entity.awakening_system.bathysmal_vishap.name"));
        this.setCustomNameVisible(true);
        this.xpReward = 240;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 260.0D)
                .add(Attributes.ATTACK_DAMAGE, 22.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 30.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 22.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.getTarget();
        if (!this.level().isClientSide && target != null && target.isAlive()) {
            if (spitCooldown > 0) {
                spitCooldown--;
            } else if (this.distanceToSqr(target) > 16.0D && this.distanceToSqr(target) <= 256.0D) {
                executeHydroSpit(target);
            }
        }
    }

    private void executeHydroSpit(LivingEntity target) {
        this.spitCooldown = 65;
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.HOSTILE, 1.8F, 1.2F);

        Vec3 dir = target.position().subtract(this.position()).normalize();
        for (double d = 1.0D; d <= 14.0D; d += 0.8D) {
            Vec3 p = this.position().add(dir.scale(d)).add(0, 1.2D, 0);
            sl.sendParticles(ParticleTypes.SPLASH, p.x, p.y, p.z, 3, 0.05D, 0.05D, 0.05D, 0.05D);
            sl.sendParticles(ParticleTypes.FALLING_WATER, p.x, p.y, p.z, 2, 0.02D, 0.02D, 0.02D, 0.02D);
        }

        if (this.distanceToSqr(target) <= 196.0D) {
            target.hurt(this.damageSources().indirectMagic(this, this), 18.0F);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        }
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 1.8F, 0.8F);
            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (ServerPlayer sp : sl.getPlayers(p -> p.distanceToSqr(this) < 900.0D)) {
                var data = SystemEvents.data(sp);
                if (isTeyvat) {
                    data.addMora(9000);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 5));
                    }
                }
            }
            if (isTeyvat && SystemItems.PRIMOGEM.get() != null) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 4 + random.nextInt(4)));
            }
        }
    }
}
