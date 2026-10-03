package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.DungeonManager;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AwakeningCoinEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> DATA_COIN_FLIP =
            SynchedEntityData.defineId(AwakeningCoinEntity.class, EntityDataSerializers.BOOLEAN);

    private int bounces = 0;
    private int lifeTicks = 0;

    public AwakeningCoinEntity(EntityType<? extends AwakeningCoinEntity> entityType, Level level) {
        super(entityType, level);
    }

    public AwakeningCoinEntity(Level level, LivingEntity shooter, boolean isCoinFlip) {
        super(ModEntities.AWAKENING_COIN_PROJECTILE.get(), shooter, level);
        this.entityData.set(DATA_COIN_FLIP, isCoinFlip);
    }

    public AwakeningCoinEntity(Level level, double x, double y, double z) {
        super(ModEntities.AWAKENING_COIN_PROJECTILE.get(), x, y, z, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COIN_FLIP, false);
    }

    public boolean isCoinFlip() {
        return this.entityData.get(DATA_COIN_FLIP);
    }

    public void setCoinFlip(boolean coinFlip) {
        this.entityData.set(DATA_COIN_FLIP, coinFlip);
    }

    @Override
    protected Item getDefaultItem() {
        return SystemItems.AWAKENING_COIN.get();
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        // Partículas de estela giratoria dorada mientras vuela
        if (level().isClientSide) {
            Vec3 motion = getDeltaMovement();
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getX() - motion.x * 0.2,
                    getY() - motion.y * 0.2 + 0.1,
                    getZ() - motion.z * 0.2,
                    0, 0.02, 0);
            if (isCoinFlip() && random.nextFloat() < 0.4F) {
                level().addParticle(ParticleTypes.WAX_ON, getX(), getY() + 0.1, getZ(), 0, 0.05, 0);
            }
        }

        // Lógica de Cara o Cruz al caer o alcanzar el ápice en el aire
        if (!level().isClientSide && isCoinFlip()) {
            if (lifeTicks >= 22 || (lifeTicks > 10 && getDeltaMovement().y <= 0 && getOwner() != null && distanceToSqr(getOwner()) < 6.0)) {
                resolveCoinFlip();
                discard();
                return;
            }
        }

        if (!level().isClientSide && lifeTicks > 200) {
            spawnDropItem();
            discard();
        }
    }

    private void resolveCoinFlip() {
        if (!(getOwner() instanceof ServerPlayer player)) {
            spawnDropItem();
            return;
        }

        boolean heads = random.nextBoolean();
        if (heads) {
            // Cara: Suerte del Sistema (Sol Dorado)
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 600, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1, false, false, true));
            player.displayClientMessage(Component.translatable("awakening_system.coin_entity.heads"), true);
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.4F);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
            }
        } else {
            // Cruz: Escudo del Sistema (Luna Sombría)
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false, true));
            player.displayClientMessage(Component.translatable("awakening_system.coin_entity.tails"), true);
            player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.2F);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.5, 0.5, 0.5, 0.05);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;

        if (result.getEntity() instanceof LivingEntity target && target != getOwner()) {
            float damage = 6.0F;
            target.hurt(damageSources().thrown(this, getOwner()), damage);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false, false));

            level().playSound(null, getX(), getY(), getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.2F, 1.8F);
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 20, 0.3, 0.3, 0.3, 0.15);
            }

            if (!target.isAlive()) {
                // Drop de moneda extra por impacto crítico
                ItemEntity bonus = new ItemEntity(level(), target.getX(), target.getY() + 0.5, target.getZ(),
                        new ItemStack(SystemItems.AWAKENING_COIN.get(), 1 + random.nextInt(2)));
                level().addFreshEntity(bonus);
            }
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide) return;

        // Si impacta contra un Altar de Ruleta en la mazmorra
        if (level().getBlockState(result.getBlockPos()).is(Blocks.RESPAWN_ANCHOR)) {
            level().playSound(null, result.getBlockPos(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.2F, 1.5F);
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.PORTAL, result.getBlockPos().getX() + 0.5, result.getBlockPos().getY() + 1.0, result.getBlockPos().getZ() + 0.5, 40, 0.4, 0.8, 0.4, 0.2);
            }
            if (getOwner() instanceof ServerPlayer sp && DungeonManager.isInDungeon(sp.getUUID())) {
                DungeonManager.onRightClickChest(sp, result.getBlockPos());
            }
            discard();
            return;
        }

        // Física de rebote de la ficha contra el suelo o muros
        bounces++;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 0.9F, 1.8F + random.nextFloat() * 0.4F);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.8F, 1.6F);

        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.WAX_ON, getX(), getY(), getZ(), 6, 0.1, 0.1, 0.1, 0.05);
        }

        if (bounces >= 3 || getDeltaMovement().lengthSqr() < 0.05) {
            spawnDropItem();
            discard();
            return;
        }

        // Invertir velocidad según la cara golpeada con disipación de energía cinética
        Vec3 delta = getDeltaMovement();
        Direction.Axis axis = result.getDirection().getAxis();
        double factor = 0.55;
        double nx = (axis == Direction.Axis.X ? -delta.x : delta.x) * factor;
        double ny = (axis == Direction.Axis.Y ? -delta.y : delta.y) * factor;
        double nz = (axis == Direction.Axis.Z ? -delta.z : delta.z) * factor;
        setDeltaMovement(nx, ny, nz);
    }

    private void spawnDropItem() {
        if (!level().isClientSide && isAlive()) {
            ItemEntity drop = new ItemEntity(level(), getX(), getY(), getZ(), new ItemStack(SystemItems.AWAKENING_COIN.get()));
            drop.setDefaultPickUpDelay();
            level().addFreshEntity(drop);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Bounces", bounces);
        tag.putInt("LifeTicks", lifeTicks);
        tag.putBoolean("CoinFlip", isCoinFlip());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        bounces = tag.getInt("Bounces");
        lifeTicks = tag.getInt("LifeTicks");
        setCoinFlip(tag.getBoolean("CoinFlip"));
    }
}
