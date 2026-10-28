package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.entity.ai.StalkPlayerGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Monstruo Cabeza de Araña del Limbo (Spider Head Monster):
 * Cabeza aberrante sostenida por patas arácnidas.
 * Acecha al jugador muy de cerca (3.0 bloques) reptando por el suelo
 * y emitiendo chasquidos de patas y siseos oscuros.
 */
public class SpiderHeadEntity extends Monster implements GeoEntity, StalkPlayerGoal.StalkSoundEmitter, LimboStalkerCreature {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.spider_head.idle");
    private static final RawAnimation CRAWL_ANIM = RawAnimation.begin().thenLoop("animation.spider_head.crawl");

    public SpiderHeadEntity(EntityType<? extends SpiderHeadEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new com.soybe.awakeningsystem.entity.ai.AvoidLimboDoorGoal(this, 1.25D));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // Acecha de cerca: se aproxima hasta 3.0 bloques del jugador
        this.goalSelector.addGoal(2, new StalkPlayerGoal(this, 1.0D, 11.0F, 48.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void emitStalkSound() {
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    com.soybe.awakeningsystem.ModSounds.SPIDER_HEAD_FOLLOWING.get(), SoundSource.HOSTILE, 3.5F, 1.0F);

            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.MYCELIUM, this.getX(), this.getY() + 0.3D, this.getZ(),
                        8, 0.4D, 0.2D, 0.4D, 0.01D);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return com.soybe.awakeningsystem.ModSounds.SPIDER_HEAD_AMBIENT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "spider_head_controller", 4, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(CRAWL_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public net.minecraft.network.chat.Component getCustomName() {
        return null;
    }

    @Override
    public void setCustomName(net.minecraft.network.chat.Component name) {
        // Bloqueado: las criaturas acechantes del Limbo nunca reciben nombres RPG ni niveles
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

}
