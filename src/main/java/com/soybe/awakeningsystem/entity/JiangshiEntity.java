package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Jiangshi (Zombi Saltador Taoísta / 僵尸) del Reino Murim:
 * Muerto viviente milenario con túnica imperial y talismán en la frente.
 * Se desplaza mediante saltos rítmicos y posee una piel dura como bronce.
 */
public class JiangshiEntity extends Monster {

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_SEALED =
            net.minecraft.network.syncher.SynchedEntityData.defineId(JiangshiEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    private int hopCooldown = 0;
    private boolean talismanBurned = false;
    private int frenzyTicks = 0;
    private int sealedTicks = 0;

    public JiangshiEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 70;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SEALED, false);
    }

    public boolean isSealed() {
        return this.entityData.get(DATA_SEALED);
    }

    public void setSealed(boolean sealed) {
        this.entityData.set(DATA_SEALED, sealed);
    }

    public void applyTalismanSeal(int ticks) {
        this.sealedTicks = Math.max(this.sealedTicks, ticks);
        this.setSealed(true);
        this.setTarget(null);
        this.getNavigation().stop();
        this.clearFire();
        this.talismanBurned = false;
        this.frenzyTicks = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isSealed()) {
            this.setDeltaMovement(0, Math.min(0, this.getDeltaMovement().y), 0);
            if (!this.level().isClientSide) {
                if (sealedTicks > 0) {
                    sealedTicks--;
                    if (this.getTarget() != null) this.setTarget(null);
                    if (this.getNavigation().isInProgress()) this.getNavigation().stop();

                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl && this.random.nextFloat() < 0.35F) {
                        sl.sendParticles(ParticleTypes.WAX_ON, this.getX(), this.getY() + 1.8D, this.getZ(),
                                3, 0.2D, 0.2D, 0.2D, 0.02D);
                        sl.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 1.7D, this.getZ(),
                                2, 0.15D, 0.15D, 0.15D, 0.05D);
                    }
                } else {
                    this.setSealed(false);
                    this.playSound(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 0.9F, 1.5F);
                }
            }
            return;
        }

        if (!this.level().isClientSide) {
            if (frenzyTicks > 0) frenzyTicks--;

            // Mecánica de saltos rítmicos característicos
            if (this.onGround() && !this.isInWater()) {
                LivingEntity target = this.getTarget();
                boolean moving = target != null || this.getNavigation().isInProgress();

                if (moving) {
                    hopCooldown++;
                    int threshold = (frenzyTicks > 0) ? 8 : 16;
                    if (hopCooldown >= threshold) {
                        hopCooldown = 0;
                        float yaw = this.getYRot();
                        double forwardSpeed = (frenzyTicks > 0) ? 0.45D : 0.32D;
                        double dx = -Math.sin(Math.toRadians(yaw)) * forwardSpeed;
                        double dz = Math.cos(Math.toRadians(yaw)) * forwardSpeed;
                        this.setDeltaMovement(dx, 0.38D, dz);
                        this.playSound(SoundEvents.ZOMBIE_STEP, 0.8F, 0.5F);

                        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                            sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(),
                                    5, 0.2D, 0.1D, 0.2D, 0.02D);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Si recibe fuego, el talismán se quema y entra en frenesí
        if (!talismanBurned && (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) || this.isOnFire())) {
            talismanBurned = true;
            frenzyTicks = 200; // 10 segundos de frenesí
            this.playSound(SoundEvents.FIRECHARGE_USE, 1.2F, 1.4F);
            this.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 1.2F, 0.7F);

            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.8D, this.getZ(),
                        20, 0.3D, 0.3D, 0.3D, 0.05D);
            }
        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            // Qi Cadavérico (Yin Chill): Lentitud y fatiga
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0));
            this.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.2F, 0.7F);

            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL, living.getX(), living.getY() + 1.0D, living.getZ(),
                        12, 0.3D, 0.3D, 0.3D, 0.02D);
            }
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.STRAY_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.STRAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRAY_DEATH;
    }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (SystemItems.TAOIST_TALISMAN != null && SystemItems.TAOIST_TALISMAN.isBound() && held.is(SystemItems.TAOIST_TALISMAN.get())) {
            if (!this.level().isClientSide) {
                applyTalismanSeal(200); // 10 seconds freeze
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                this.playSound(ModSounds.TALISMAN_SEAL.value(), 1.4F, 1.0F);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.WAX_ON, this.getX(), this.getY() + 1.8D, this.getZ(),
                            25, 0.3D, 0.4D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 1.5D, this.getZ(),
                            15, 0.3D, 0.3D, 0.3D, 0.02D);
                }
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("TalismanBurned", this.talismanBurned);
        compound.putInt("FrenzyTicks", this.frenzyTicks);
        compound.putInt("SealedTicks", this.sealedTicks);
        compound.putBoolean("Sealed", this.isSealed());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.talismanBurned = compound.getBoolean("TalismanBurned");
        this.frenzyTicks = compound.getInt("FrenzyTicks");
        this.sealedTicks = compound.getInt("SealedTicks");
        this.setSealed(compound.getBoolean("Sealed"));
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        if (SystemItems.TAOIST_TALISMAN != null && SystemItems.TAOIST_TALISMAN.isBound() && !talismanBurned) {
            this.spawnAtLocation(new ItemStack(SystemItems.TAOIST_TALISMAN.get(), 1));
        }
        this.spawnAtLocation(new ItemStack(Items.PACKED_ICE, 1 + this.random.nextInt(2)));
        this.spawnAtLocation(new ItemStack(Items.IRON_INGOT, 1 + this.random.nextInt(2)));
        this.spawnAtLocation(SystemItems.coin(120 + this.random.nextInt(60)));
    }
}
