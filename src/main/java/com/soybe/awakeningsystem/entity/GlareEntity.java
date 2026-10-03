package com.soybe.awakeningsystem.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GlareEntity extends PathfinderMob implements FlyingAnimal {

    private int grumbleCooldown = 60;

    public GlareEntity(EntityType<? extends GlareEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 14, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.FLYING_SPEED, 0.28D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.15D, Ingredient.of(Items.GLOW_BERRIES, Items.SWEET_BERRIES), false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide()) {
            // Partículas de esporas de musgo
            if (this.random.nextInt(4) == 0) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
                double py = this.getY() + 0.2D + this.random.nextDouble() * 0.6D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;
                this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, px, py, pz, 0, 0, 0);
            }
        } else {
            // Detectar oscuridad que permite el spawn de monstruos
            if (--grumbleCooldown <= 0) {
                grumbleCooldown = 80 + this.random.nextInt(60);
                BlockPos pos = this.blockPosition();
                int light = this.level().getBrightness(LightLayer.BLOCK, pos);
                if (light <= 2) {
                    // Mas oscuro: aviso sonoro y partículas de disgusto
                    this.level().playSound(null, pos, SoundEvents.MOSS_FALL, SoundSource.NEUTRAL, 1.2F, 0.7F);
                    ((ServerLevel) this.level()).sendParticles(ParticleTypes.ANGRY_VILLAGER,
                            this.getX(), this.getY() + 0.8D, this.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
                }
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.GLOW_BERRIES)) {
            if (!this.level().isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(4.0F);
                this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 1.2F);
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.HEART,
                        this.getX(), this.getY() + 0.6D, this.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.0D);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.MOSS_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.MOSS_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.MOSS_BREAK;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        this.spawnAtLocation(new ItemStack(Blocks.MOSS_BLOCK, 1 + this.random.nextInt(2)));
        if (this.random.nextFloat() < 0.40F) {
            this.spawnAtLocation(new ItemStack(Items.GLOW_BERRIES, 1 + this.random.nextInt(2)));
        }
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // No recibe daño de caída
    }
}
