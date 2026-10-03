package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Mandrágora Cantarina (Screaming Mandrake).
 * Planta / Raíz mágica del Reino Isekai y bosques encantados.
 * - En estado enterrado (camuflada), parece un brote de flor en el suelo.
 * - Al ser pisada, cosechada o golpeada, salta de la tierra y emite un grito estridente ensordecedor
 *   que causa Náusea y Lentitud a quienes la rodean.
 * - Tras el grito, huye despavorida agitando sus bracitos de raíz a toda prisa.
 * - Al ser derrotada, suelta su Raíz de Mandrágora Mágica para alquimia.
 */
public class ScreamingMandrakeEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> BURIED =
            SynchedEntityData.defineId(ScreamingMandrakeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SCREAMING =
            SynchedEntityData.defineId(ScreamingMandrakeEntity.class, EntityDataSerializers.BOOLEAN);

    private int screamTicks = 0;

    public ScreamingMandrakeEntity(EntityType<? extends ScreamingMandrakeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BURIED, true);
        builder.define(SCREAMING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 12.0F, 1.3D, 1.55D) {
            @Override
            public boolean canUse() {
                return !ScreamingMandrakeEntity.this.isBuried() && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 10.0F, 1.3D, 1.5D) {
            @Override
            public boolean canUse() {
                return !ScreamingMandrakeEntity.this.isBuried() && super.canUse();
            }
        });
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !ScreamingMandrakeEntity.this.isBuried() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public boolean isBuried() {
        return this.entityData.get(BURIED);
    }

    public void setBuried(boolean buried) {
        this.entityData.set(BURIED, buried);
    }

    public boolean isScreaming() {
        return this.entityData.get(SCREAMING);
    }

    public void setScreaming(boolean screaming) {
        this.entityData.set(SCREAMING, screaming);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isBuried()) {
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y < 0 ? this.getDeltaMovement().y : 0.0D, 0.0D);

            // Si un jugador se acerca mucho (< 1.2 bloques), se desentierra
            if (!this.level().isClientSide()) {
                Player nearest = this.level().getNearestPlayer(this, 1.3D);
                if (nearest != null && nearest.isAlive()) {
                    this.unearth();
                }
            }
        } else if (this.isScreaming()) {
            this.screamTicks++;
            if (this.level().isClientSide()) {
                // Partículas sónicas de histeria
                if (this.screamTicks % 3 == 0) {
                    this.level().addParticle(ParticleTypes.NOTE,
                            this.getX() + (this.random.nextDouble() - 0.5D) * 0.5D,
                            this.getY() + 0.6D,
                            this.getZ() + (this.random.nextDouble() - 0.5D) * 0.5D,
                            this.random.nextDouble(), 0.0D, 0.0D);
                }
            }

            if (this.screamTicks >= 50) {
                this.setScreaming(false);
                this.screamTicks = 0;
            }
        }
    }

    public void unearth() {
        if (!this.isBuried()) return;

        this.setBuried(false);
        this.setScreaming(true);
        this.screamTicks = 0;

        // Salto de sorpresa fuera de la tierra
        this.setDeltaMovement(
                (this.random.nextDouble() - 0.5D) * 0.25D,
                0.42D,
                (this.random.nextDouble() - 0.5D) * 0.25D
        );
        this.hasImpulse = true;

        this.level().playSound(null, this.blockPosition(), ModSounds.MANDRAKE_SCREAM.get(),
                SoundSource.HOSTILE, 1.5F, 1.0F);

        // Efecto del grito sónico: Náusea y Lentitud a entidades cercanas
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.4D, this.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);

            AABB box = this.getBoundingBox().inflate(6.5D);
            List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class, box);
            for (LivingEntity target : targets) {
                if (target != this && target.isAlive()) {
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 130, 0, false, true));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1, false, true));
                }
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isBuried()) {
            this.unearth();
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isBuried()) {
            this.unearth();
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        int count = 1 + this.random.nextInt(2);
        this.spawnAtLocation(new net.minecraft.world.item.ItemStack(SystemItems.MANDRAKE_ROOT.get(), count));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isScreaming() ? ModSounds.MANDRAKE_SCREAM.get() : ModSounds.MANDRAKE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.MANDRAKE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MANDRAKE_DEATH.get();
    }
}
