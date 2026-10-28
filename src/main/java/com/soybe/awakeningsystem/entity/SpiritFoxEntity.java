package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.SystemBlocks;
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
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Zorro Espiritual de Nueve Colas (Gumiho / Bestia Celestial de Murim).
 * Criatura mística y leal compañera de cultivo.
 * - Puede ser domesticado con Qingseng Milenario o bayas celestiales.
 * - Canaliza un Aura de Concentración de Qi para su maestro meditador (+25% absorción).
 * - Rastrea de forma innata plantas de Qingseng Milenario en un radio de 24 bloques.
 * - En combate ataca con mordiscos espirituales imbuidos en Qi.
 */
public class SpiritFoxEntity extends TamableAnimal {

    private int sniffCooldown = 0;
    private BlockPos trackedGinsengPos = null;

    public SpiritFoxEntity(EntityType<? extends SpiritFoxEntity> type, Level level) {
        super(type, level);
        this.setTame(false, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.15D, 8.0F, 2.5F));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(SystemItems.THOUSAND_YEAR_GINSENG.get()) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            boolean canInteract = this.isOwnedBy(player) || this.isTame() || (isFood(itemstack) && !this.isTame());
            return canInteract ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        if (!this.isTame() && isFood(itemstack)) {
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }

            boolean instantSuccess = itemstack.is(SystemItems.THOUSAND_YEAR_GINSENG.get());
            if (instantSuccess || this.random.nextInt(3) == 0) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.4F);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.playSound(SoundEvents.FOX_SNIFF, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.isTame() && this.isOwnedBy(player)) {
            if (isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                this.heal(8.0F);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.playSound(SoundEvents.FOX_EAT, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }

            if (itemstack.isEmpty() || player.isShiftKeyDown()) {
                boolean willSit = !this.isOrderedToSit();
                this.setOrderedToSit(willSit);
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                this.playSound(willSit ? SoundEvents.FOX_SLEEP : SoundEvents.FOX_AMBIENT, 0.8F, 1.2F);
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.isTame() && this.getOwner() instanceof Player owner) {
            if (this.distanceToSqr(owner) <= 64.0D) {
                if (this.tickCount % 20 == 0) {
                    SystemData data = SystemEvents.data(owner);
                    if (data != null && data.dantianRealm() > 0 && data.cultivationQi() < data.maxCultivationQi()) {
                        int bonusQi = Math.max(1, (int) (data.cultivationSubLevel() * 1.5));
                        data.addCultivationQi(bonusQi);
                        if (this.level() instanceof ServerLevel sl) {
                            sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.SPIRIT_FOXFIRE.get(),
                                    this.getX(), this.getY() + 0.5D, this.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.01D);
                        }
                    }
                }
            }
        }

        if (!this.level().isClientSide && --this.sniffCooldown <= 0) {
            this.sniffCooldown = 120 + this.random.nextInt(80);
            findNearestWildGinseng();
            if (this.trackedGinsengPos != null) {
                this.playSound(SoundEvents.FOX_SNIFF, 0.8F, 1.3F);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(com.soybe.awakeningsystem.particle.ModParticles.SPIRIT_FOXFIRE.get(),
                            this.getX(), this.getY() + 0.6D, this.getZ(), 3, 0.15D, 0.15D, 0.15D, 0.015D);
                }
            }
        }

        if (this.level().isClientSide && this.random.nextInt(25) == 0) {
            double angle = this.random.nextFloat() * Math.PI * 2.0;
            double dist = 0.25D + this.random.nextDouble() * 0.35D;
            double px = this.getX() + Math.cos(angle) * dist;
            double py = this.getY() + 0.6D + this.random.nextDouble() * 0.35D;
            double pz = this.getZ() + Math.sin(angle) * dist;
            this.level().addParticle(com.soybe.awakeningsystem.particle.ModParticles.SPIRIT_FOXFIRE.get(),
                    px, py, pz, 0, 0.008D, 0);
        }
    }

    private void findNearestWildGinseng() {
        BlockPos origin = this.blockPosition();
        int searchRadius = 24;
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;

        for (int dx = -searchRadius; dx <= searchRadius; dx += 4) {
            for (int dz = -searchRadius; dz <= searchRadius; dz += 4) {
                for (int dy = -6; dy <= 6; dy += 2) {
                    BlockPos p = origin.offset(dx, dy, dz);
                    if (this.level().getBlockState(p).is(SystemBlocks.WILD_GINSENG.get())) {
                        double d = origin.distSqr(p);
                        if (d < bestDistSq) {
                            bestDistSq = d;
                            best = p;
                        }
                    }
                }
            }
        }
        this.trackedGinsengPos = best;
    }

    @Nullable
    public BlockPos getTrackedGinsengPos() {
        return this.trackedGinsengPos;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.SPIRIT_FOX.get().create(level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isSleeping() ? SoundEvents.FOX_SLEEP : (this.random.nextBoolean() ? SoundEvents.FOX_AMBIENT : SoundEvents.FOX_SNIFF);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState block) {
        this.playSound(SoundEvents.FOX_SPIT, 0.15F, 1.0F);
    }
}
