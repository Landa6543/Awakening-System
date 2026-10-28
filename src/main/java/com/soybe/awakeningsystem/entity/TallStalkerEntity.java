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
 * Acechador Sombrío del Limbo (Tall Three-Eyed Stalker):
 * Figura humanoide alta de tres ojos y garras finas.
 * Se aproxima ceremoniosamente hasta 3.5 bloques del jugador,
 * mirándolo de cerca y emitiendo un zumbido distorsionado de vacío y latidos apagados.
 */
public class TallStalkerEntity extends Monster implements GeoEntity, StalkPlayerGoal.StalkSoundEmitter, LimboStalkerCreature {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.tall_stalker.idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.tall_stalker.stalk");

    public TallStalkerEntity(EntityType<? extends TallStalkerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new com.soybe.awakeningsystem.entity.ai.AvoidLimboDoorGoal(this, 1.25D));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // Acecha de cerca: se aproxima hasta 3.5 bloques del jugador
        this.goalSelector.addGoal(2, new StalkPlayerGoal(this, 1.0D, 11.0F, 48.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void emitStalkSound() {
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    com.soybe.awakeningsystem.ModSounds.LIMBO_ENTITY_FOLLOWING.get(), SoundSource.HOSTILE, 3.5F, 0.8F);

            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.8D, this.getZ(),
                        12, 0.4D, 0.6D, 0.4D, 0.05D);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return com.soybe.awakeningsystem.ModSounds.LIMBO_ENTITY_AMBIENT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return com.soybe.awakeningsystem.ModSounds.LIMBO_OPHANIM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return com.soybe.awakeningsystem.ModSounds.LIMBO_OPHANIM_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "tall_stalker_controller", 4, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(WALK_ANIM);
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
