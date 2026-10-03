package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Dragón Bebé de Cristal Astral (Criatura mística y voladora del Reino Isekai).
 * - Domesticable con Amatista o Diamantes.
 * - Puede volar y flotar suavemente siguiendo a su maestro.
 * - Ataca a distancia disparando orbes de aliento astral cristalino.
 * - Confiere Resonancia Astral pasiva a su maestro: regeneración de Maná (+10 MP) y bendición celestial.
 */
public class AstralDragonEntity extends TamableAnimal implements FlyingAnimal, RangedAttackMob {

    private int scaleDropTimer = 12000 + this.random.nextInt(6000); // 10-15 min

    public AstralDragonEntity(EntityType<? extends AstralDragonEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 12, true);
        this.setTame(false, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FLYING_SPEED, 0.42D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.2D, 24, 40, 16.0F));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.25D, 6.0F, 2.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.AMETHYST_SHARD) || stack.is(Items.DIAMOND)
                || stack.is(Items.GLOWSTONE_DUST) || stack.is(Items.EMERALD);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack itemstack = player.getItemInHand(hand);

        // 1. Proceso de Domesticación (con alimento místico, en creativo o con mano vacía)
        if (!this.isTame()) {
            if (isFood(itemstack)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                this.tame(player);
                this.setOrderedToSit(true);
                this.setInSittingPose(true);
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7); // Corazones
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2F, 1.6F);
                this.playSound(SoundEvents.STRIDER_HAPPY, 1.0F, 1.6F);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else if (itemstack.isEmpty() || player.getAbilities().instabuild) {
                // Domesticación directa con mano vacía
                this.tame(player);
                this.setOrderedToSit(true);
                this.setInSittingPose(true);
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2F, 1.6F);
                this.playSound(SoundEvents.STRIDER_HAPPY, 1.0F, 1.6F);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return InteractionResult.PASS;
        }

        // 2. Interacción cuando ya está domesticado
        if (this.isTame() && this.isOwnedBy(player)) {
            // Curar si está herido y se le ofrece comida
            if (isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                this.heal(8.0F);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.playSound(SoundEvents.AMETHYST_CLUSTER_HIT, 1.0F, 1.6F);
                this.playSound(SoundEvents.STRIDER_HAPPY, 0.9F, 1.7F);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // Alternar estado de Sentado / Levantarse
            boolean willSit = !this.isOrderedToSit();
            this.setOrderedToSit(willSit);
            this.setInSittingPose(willSit);
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);

            if (willSit) {
                if (!this.onGround()) {
                    this.setDeltaMovement(this.getDeltaMovement().x * 0.5D, -0.4D, this.getDeltaMovement().z * 0.5D); // Desciende suavemente
                }
                this.playSound(SoundEvents.STRIDER_AMBIENT, 0.9F, 1.6F);
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.9F, 1.8F);
            } else {
                this.playSound(SoundEvents.STRIDER_HAPPY, 0.9F, 1.7F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Control de gravedad: si está sentado desciende y se sienta en tierra, si no, vuela suavemente
        if (this.isOrderedToSit() || this.isInSittingPose()) {
            this.setNoGravity(false);
            if (!this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().x * 0.7D, -0.3D, this.getDeltaMovement().z * 0.7D);
            }
        } else {
            this.setNoGravity(!this.onGround());
        }

        // 1. Efectos del servidor (Resonancia Astral con su maestro)
        if (!this.level().isClientSide) {
            if (this.isTame() && this.getOwner() instanceof Player owner) {
                if (this.distanceToSqr(owner) < 144.0D) { // Radio de 12 bloques
                    // Resonancia de Maná: Regeneración pasiva continua
                    if (this.tickCount % 40 == 0) {
                        SystemData data = SystemEvents.data(owner);
                        if (data != null && data.energy() < data.maxEnergy()) {
                            data.restoreEnergy(8);
                            if (this.level() instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.END_ROD,
                                        this.getX(), this.getY() + 0.4D, this.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.01D);
                            }
                        }
                    }
                }
            }

            // Mudanza periódica de escamas astrales
            if (--this.scaleDropTimer <= 0) {
                this.scaleDropTimer = 12000 + this.random.nextInt(6000);
                this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0F, 1.8F);
                if (SystemItems.ASTRAL_CRYSTAL_SCALE != null && SystemItems.ASTRAL_CRYSTAL_SCALE.isBound()) {
                    this.spawnAtLocation(SystemItems.ASTRAL_CRYSTAL_SCALE.get());
                } else {
                    this.spawnAtLocation(Items.AMETHYST_SHARD);
                }
            }
        }

        // 2. Destellos astrales clientes (Sutiles partículas de polvo estelar y reposo)
        if (this.level().isClientSide) {
            if (this.isOrderedToSit()) {
                if (this.random.nextInt(16) == 0) {
                    double ox = (this.random.nextDouble() - 0.5D) * 0.45D;
                    double oy = 0.15D + this.random.nextDouble() * 0.25D;
                    double oz = (this.random.nextDouble() - 0.5D) * 0.45D;
                    this.level().addParticle(ParticleTypes.END_ROD,
                            this.getX() + ox, this.getY() + oy, this.getZ() + oz, 0, 0.012D, 0);
                }
            } else if (this.random.nextInt(18) == 0) {
                double ox = (this.random.nextDouble() - 0.5D) * 0.4D;
                double oy = 0.2D + this.random.nextDouble() * 0.4D;
                double oz = (this.random.nextDouble() - 0.5D) * 0.4D;
                this.level().addParticle(ParticleTypes.GLOW,
                        this.getX() + ox, this.getY() + oy, this.getZ() + oz, 0, 0.01D, 0);
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        AstralBreathProjectileEntity projectile = new AstralBreathProjectileEntity(this.level(), this);
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - 0.2D - projectile.getY();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        projectile.shoot(dx, dy + dist * 0.04D, dz, 1.35F, 1.5F);

        this.playSound(SoundEvents.AMETHYST_CLUSTER_HIT, 1.2F, 1.8F);
        this.playSound(SoundEvents.BREEZE_SHOOT, 0.7F, 1.6F);
        this.level().addFreshEntity(projectile);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.ASTRAL_DRAGON.get().create(level);
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.1F + 1.55F; // Tono dulce y agudo de dragón cría
    }

    @Override
    protected float getSoundVolume() {
        return 0.55F; // Volumen moderado de mascota
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.STRIDER_AMBIENT; // Ronroneo / gorjeo suave de reptil místico
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.FOX_HURT; // Chillido agudo de cachorro / cría
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRIDER_DEATH; // Suspiro triste suave
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.TURTLE_SHAMBLE_BABY, 0.25F, 1.4F);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Los dragones astrales planean con sus alas y son totalmente inmunes a daño por caída
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Sin acumulación de daño de caída al descender
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }
}
