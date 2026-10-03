package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FlyingSwordEntity extends Entity {

    public FlyingSwordEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return first instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity controller = this.getControllingPassenger();
        if (controller instanceof Player player) {
            this.setYRot(player.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(player.getXRot() * 0.6F);
            this.xRotO = this.getXRot();

            float forward = player.zza;
            float strafe = player.xxa;

            Vec3 look = player.getLookAngle();
            if (forward > 0.0F) {
                double speed = 0.85D;
                this.setDeltaMovement(look.scale(speed));
            } else if (forward < 0.0F) {
                double speed = 0.35D;
                this.setDeltaMovement(look.scale(-speed));
            } else {
                Vec3 current = this.getDeltaMovement();
                double hoverY = 0.015D * Math.sin(this.tickCount * 0.15D);
                this.setDeltaMovement(current.x * 0.85D, hoverY, current.z * 0.85D);
            }

            if (strafe != 0.0F) {
                Vec3 side = look.cross(new Vec3(0, 1, 0)).normalize().scale(strafe * 0.35D);
                this.setDeltaMovement(this.getDeltaMovement().add(side));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());

            if (this.tickCount % 2 == 0 && this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.15D, this.getZ(), 2, 0.15D, 0.05D, 0.15D, 0.01D);
                sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.15D, this.getZ(), 1, 0.1D, 0.05D, 0.1D, 0.01D);
            }
        } else {
            Vec3 cur = this.getDeltaMovement();
            this.setDeltaMovement(cur.x * 0.7D, 0.01D * Math.sin(this.tickCount * 0.1D), cur.z * 0.7D);
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (!this.level().isClientSide && this.tickCount > 36000) {
                this.discard();
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide) {
                ItemStack stack = new ItemStack(SystemItems.FLYING_SWORD.get());
                if (!player.addItem(stack)) {
                    this.spawnAtLocation(stack);
                }
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                this.discard();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.isVehicle()) {
            if (!this.level().isClientSide) {
                player.startRiding(this, true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        if (!this.level().isClientSide && !this.isRemoved()) {
            if (source.getEntity() instanceof Player) {
                ItemStack stack = new ItemStack(SystemItems.FLYING_SWORD.get());
                this.spawnAtLocation(stack);
                this.discard();
                return true;
            }
        }
        return false;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0D, 0.78D, -0.05D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        // Silenciado: la espada voladora no genera sonidos de paso ni choques de madera al rozar bloques
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) {
    }
}
