package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class JadeFeijianEntity extends ThrowableProjectile {

    private static final EntityDataAccessor<Boolean> RETURNING =
            SynchedEntityData.defineId(JadeFeijianEntity.class, EntityDataSerializers.BOOLEAN);

    private int lifeTicks = 0;
    private ItemStack swordStack = ItemStack.EMPTY;

    public JadeFeijianEntity(EntityType<? extends JadeFeijianEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public JadeFeijianEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.JADE_FEIJIAN.get(), shooter, level);
        this.swordStack = stack.copy();
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RETURNING, false);
    }

    public boolean isReturning() {
        return this.entityData.get(RETURNING);
    }

    public void setReturning(boolean returning) {
        this.entityData.set(RETURNING, returning);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (this.level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                double ox = (this.random.nextDouble() - 0.5D) * 0.25D;
                double oy = (this.random.nextDouble() - 0.5D) * 0.25D;
                double oz = (this.random.nextDouble() - 0.5D) * 0.25D;
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getX() + ox, this.getY() + oy, this.getZ() + oz, 0, 0.02D, 0);
                this.level().addParticle(ParticleTypes.ENCHANT, this.getX() + ox, this.getY() + oy, this.getZ() + oz, 0, 0.05D, 0);
            }
            if (isReturning()) {
                this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
            return;
        }

        Entity owner = this.getOwner();

        if (owner == null || !owner.isAlive() || owner.level() != this.level() || lifeTicks > 400) {
            dropItemAndDiscard();
            return;
        }

        if (!isReturning()) {
            if (lifeTicks < 25) {
                AABB searchBox = this.getBoundingBox().inflate(8.0D);
                List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, searchBox, e ->
                        e.isAlive() && e != owner && (e instanceof Enemy || !e.getTags().contains("shadow_soldier")));
                LivingEntity closest = null;
                double closestDist = Double.MAX_VALUE;
                for (LivingEntity e : enemies) {
                    double d = this.distanceToSqr(e);
                    if (d < closestDist) {
                        closestDist = d;
                        closest = e;
                    }
                }
                if (closest != null) {
                    Vec3 toEnemy = closest.getBoundingBox().getCenter().subtract(this.position()).normalize();
                    Vec3 currentMov = this.getDeltaMovement();
                    Vec3 newMov = currentMov.scale(0.85D).add(toEnemy.scale(0.35D)).normalize().scale(1.4D);
                    this.setDeltaMovement(newMov);
                }
            } else {
                setReturning(true);
            }
        } else {
            Vec3 toOwner = owner.getEyePosition().subtract(0, 0.25D, 0).subtract(this.position());
            double distSq = toOwner.lengthSqr();

            if (distSq < 2.5D) {
                catchSword((Player) owner);
                return;
            }

            double speed = Math.min(1.8D, 0.8D + (distSq * 0.02D));
            this.setDeltaMovement(toOwner.normalize().scale(speed));
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (isReturning()) return;
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (target != owner && target instanceof LivingEntity living) {
            if (!this.level().isClientSide) {
                DamageSource ds = owner instanceof Player p
                        ? this.damageSources().playerAttack(p)
                        : this.damageSources().magic();
                living.hurt(ds, 16.0F);
                this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 1.4F);
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.5F, 1.8F);

                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.1D);
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.05D);
                }
                setReturning(true);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!isReturning()) {
            this.playSound(SoundEvents.AMETHYST_BLOCK_HIT, 1.2F, 1.5F);
            setReturning(true);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) result);
        } else if (result.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) result);
        }
    }

    private void catchSword(Player player) {
        ItemStack returnStack = this.swordStack.isEmpty()
                ? new ItemStack(SystemItems.JADE_FEIJIAN.get())
                : this.swordStack.copy();

        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, returnStack);
        } else if (!player.getInventory().add(returnStack)) {
            player.drop(returnStack, false);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.4F);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.8F);

        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 15, 0.3D, 0.5D, 0.3D, 0.05D);
        }

        this.discard();
    }

    private void dropItemAndDiscard() {
        ItemStack dropStack = this.swordStack.isEmpty()
                ? new ItemStack(SystemItems.JADE_FEIJIAN.get())
                : this.swordStack;
        ItemEntity entity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), dropStack);
        this.level().addFreshEntity(entity);
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LifeTicks", lifeTicks);
        tag.putBoolean("Returning", isReturning());
        if (!swordStack.isEmpty()) {
            tag.put("SwordStack", swordStack.save(this.registryAccess()));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lifeTicks = tag.getInt("LifeTicks");
        if (tag.contains("Returning")) {
            setReturning(tag.getBoolean("Returning"));
        }
        if (tag.contains("SwordStack")) {
            swordStack = ItemStack.parse(this.registryAccess(), tag.getCompound("SwordStack")).orElse(ItemStack.EMPTY);
        }
    }
}
