package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Golem de Sculk (Sculk Golem).
 * Criatura ancestral de oscuridad profunda que habita las cavernas del Piso 10.
 * Permanece en reposo petrificado como una estatua pétrea durmiente hasta que detecta vibraciones,
 * pasos no sigilosos o daño, momento en el que despierta emitiendo rugidos y atacando.
 */
public class SculkGolemEntity extends Monster {

    public static final EntityDataAccessor<Boolean> IS_STATUE =
            SynchedEntityData.defineId(SculkGolemEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(SculkGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private int attackAnimationTick = 0;
    private int vibrationCheckCooldown = 0;

    public SculkGolemEntity(EntityType<? extends SculkGolemEntity> type, Level level) {
        super(type, level);
        this.xpReward = 45;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.90D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                return !SculkGolemEntity.this.isStatue() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !SculkGolemEntity.this.isStatue() && super.canContinueToUse();
            }
        });

        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D) {
            @Override
            public boolean canUse() {
                return !SculkGolemEntity.this.isStatue() && super.canUse();
            }
        });

        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F) {
            @Override
            public boolean canUse() {
                return !SculkGolemEntity.this.isStatue() && super.canUse();
            }
        });

        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !SculkGolemEntity.this.isStatue() && super.canUse();
            }
        });

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return !SculkGolemEntity.this.isStatue() && super.canUse();
            }
        });
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_STATUE, true);
        builder.define(IS_ATTACKING, false);
    }

    public boolean isStatue() {
        return this.entityData.get(IS_STATUE);
    }

    public void setStatue(boolean statue) {
        this.entityData.set(IS_STATUE, statue);
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("is_statue", isStatue());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("is_statue")) {
            setStatue(compound.getBoolean("is_statue"));
        }
    }

    public int getAttackAnimationTick() {
        return this.attackAnimationTick;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;
            if (this.attackAnimationTick == 0) {
                setAttacking(false);
            }
        }

        if (isStatue()) {
            // Inmóvil mientras es estatua
            this.setDeltaMovement(0, Math.min(0, this.getDeltaMovement().y), 0);

            if (!this.level().isClientSide) {
                vibrationCheckCooldown++;
                if (vibrationCheckCooldown >= 5) {
                    vibrationCheckCooldown = 0;
                    checkVibrations();
                }
            } else {
                // Partículas tenues de sculk en estado de reposo
                if (this.random.nextFloat() < 0.08F) {
                    this.level().addParticle(ParticleTypes.SCULK_SOUL,
                            this.getX() + (this.random.nextDouble() - 0.5D) * 1.2D,
                            this.getY() + 1.2D + this.random.nextDouble() * 0.8D,
                            this.getZ() + (this.random.nextDouble() - 0.5D) * 1.2D,
                            0.0D, 0.02D, 0.0D);
                }
            }
        }
    }

    private void checkVibrations() {
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(12.0D));
        for (Player player : nearbyPlayers) {
            if (player.isSpectator() || player.isCreative()) continue;

            double dist = player.distanceTo(this);
            // Si corre o salta cerca (hasta 12 bloques)
            boolean isRunningOrJumping = player.isSprinting() || !player.onGround();
            // Si camina sin estar agachado (hasta 6 bloques)
            boolean isWalkingLoudly = dist <= 6.5D && !player.isCrouching() && player.getDeltaMovement().horizontalDistanceSqr() > 0.005D;

            if (isRunningOrJumping || isWalkingLoudly || dist <= 3.0D) {
                awaken(player);
                break;
            }
        }
    }

    public void awaken(@Nullable LivingEntity trigger) {
        if (!isStatue()) return;

        setStatue(false);
        this.playSound(SoundEvents.WARDEN_ROAR, 1.8F, 0.85F);
        this.playSound(SoundEvents.WARDEN_EMERGE, 1.5F, 0.9F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.5D, this.getZ(), 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 1.2D, this.getZ(), 25, 0.8D, 0.8D, 0.8D, 0.08D);

            // Efecto de oscuridad a los jugadores cercanos
            List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(14.0D));
            for (Player p : players) {
                p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false, true));
            }
        }

        if (trigger != null) {
            this.setTarget(trigger);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isStatue()) {
            // Despierta al recibir cualquier daño
            if (source.getEntity() instanceof LivingEntity attacker) {
                awaken(attacker);
            } else {
                awaken(null);
            }
            // En modo estatua tiene armadura de piedra: 65% de reducción
            amount *= 0.35F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.attackAnimationTick = 15;
        setAttacking(true);
        this.level().broadcastEntityEvent(this, (byte) 4);

        boolean success = super.doHurtTarget(target);
        if (success && target instanceof LivingEntity living) {
            this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 1.3F, 0.85F);
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.1F, 0.75F);

            // Empuje sónico
            Vec3 knockbackDir = living.position().subtract(this.position()).normalize().scale(1.1D);
            living.push(knockbackDir.x, 0.40D, knockbackDir.z);
            living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            }
        }
        return success;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        // Suelta Cristales de Maná 3D
        int shardCount = 2 + this.random.nextInt(4);
        this.spawnAtLocation(new ItemStack(SystemItems.MANA_CRYSTAL_SHARD_C.get(), shardCount));

        // Botín temático de Sculk
        this.spawnAtLocation(new ItemStack(Items.ECHO_SHARD, 1));
        this.spawnAtLocation(new ItemStack(Blocks.SCULK_CATALYST, 1));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isStatue() ? null : SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return isStatue() ? SoundEvents.DEEPSLATE_HIT : SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.WARDEN_STEP;
    }
}
