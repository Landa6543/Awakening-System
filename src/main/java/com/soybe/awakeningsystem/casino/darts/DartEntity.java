package com.soybe.awakeningsystem.casino.darts;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class DartEntity extends ThrowableItemProjectile {

    private static final int[] DART_SECTORS = {
            20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5
    };

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_IS_STUCK =
            net.minecraft.network.syncher.SynchedEntityData.defineId(DartEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Direction> DATA_STUCK_FACING =
            net.minecraft.network.syncher.SynchedEntityData.defineId(DartEntity.class, net.minecraft.network.syncher.EntityDataSerializers.DIRECTION);

    public DartEntity(EntityType<? extends DartEntity> type, Level level) {
        super(type, level);
    }

    public DartEntity(Level level, LivingEntity shooter) {
        super(ModEntities.DART.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_STUCK, false);
        builder.define(DATA_STUCK_FACING, Direction.NORTH);
    }

    public boolean isStuck() {
        return this.entityData.get(DATA_IS_STUCK);
    }

    public Direction getStuckFacing() {
        return this.entityData.get(DATA_STUCK_FACING);
    }

    @Override
    protected Item getDefaultItem() {
        return SystemItems.DART.get();
    }

    private int stuckTicks = 0;

    @Override
    public void tick() {
        if (this.isStuck()) {
            this.stuckTicks++;
            if (this.stuckTicks > 600) {
                if (!this.level().isClientSide()) {
                    this.discard();
                }
            }
            return;
        }
        super.tick();
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        Level level = this.level();
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof DartboardBlock) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
            this.entityData.set(DATA_IS_STUCK, true);
            this.entityData.set(DATA_STUCK_FACING, hitResult.getDirection());
            Vec3 normal = Vec3.atLowerCornerOf(hitResult.getDirection().getNormal());
            this.setPos(hitResult.getLocation().add(normal.scale(0.04)));
            handleDartboardImpact(hitResult, pos, state);
        } else {
            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 0.6F, 1.8F);
            if (!level.isClientSide()) {
                this.spawnAtLocation(new ItemStack(SystemItems.DART.get()));
                this.discard();
            }
        }
    }

    @Override
    public net.minecraft.world.InteractionResult interact(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (this.isStuck()) {
            if (!this.level().isClientSide()) {
                player.addItem(new ItemStack(SystemItems.DART.get()));
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.4F);
                this.discard();
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return super.interact(player, hand);
    }

    @Override
    public void playerTouch(net.minecraft.world.entity.player.Player player) {
        if (this.isStuck() && this.stuckTicks > 10) {
            if (!this.level().isClientSide()) {
                if (player.addItem(new ItemStack(SystemItems.DART.get()))) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.4F);
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (hitResult.getEntity() instanceof LivingEntity target) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 4.0F);
        }
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    private void handleDartboardImpact(BlockHitResult hitResult, BlockPos pos, BlockState state) {
        Level level = this.level();
        Vec3 hitPos = hitResult.getLocation();

        level.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.2F, 1.8F);
        level.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 0.9F, 1.5F);

        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, hitPos.x, hitPos.y, hitPos.z, 8, 0.05, 0.05, 0.05, 0.1);
        }

        if (level.isClientSide()) return;

        Direction facing = state.getValue(DartboardBlock.FACING);
        double lx = hitPos.x - pos.getX();
        double ly = hitPos.y - pos.getY();
        double lz = hitPos.z - pos.getZ();

        double u, v;
        v = ly;

        switch (facing) {
            case NORTH -> u = 1.0 - lx;
            case SOUTH -> u = lx;
            case WEST -> u = lz;
            case EAST -> u = 1.0 - lz;
            default -> u = 0.5;
        }

        double du = u - 0.5;
        double dv = v - 0.5;
        double radius = Math.sqrt(du * du + dv * dv);

        double angleRad = Math.atan2(dv, du);
        double angleDeg = Math.toDegrees(angleRad);
        if (angleDeg < 0) angleDeg += 360.0;

        int score = 0;
        Component descComp;
        int coinsReward = 0;

        if (radius <= 0.035) {
            score = 50;
            descComp = Component.translatable("awakening_system.casino.darts.hit.bullseye", 50);
            coinsReward = 5000;
        } else if (radius <= 0.075) {
            score = 25;
            descComp = Component.translatable("awakening_system.casino.darts.hit.outer_bull", 25);
            coinsReward = 2500;
        } else if (radius <= 0.44) {
            double shiftedAngle = (angleDeg - 90.0 + 9.0);
            while (shiftedAngle < 0) shiftedAngle += 360.0;
            while (shiftedAngle >= 360.0) shiftedAngle -= 360.0;
            int sectorIdx = (int) (shiftedAngle / 18.0);
            int baseNum = DART_SECTORS[sectorIdx % 20];

            if (radius >= 0.22 && radius <= 0.27) {
                score = baseNum * 3;
                descComp = Component.translatable("awakening_system.casino.darts.hit.triple", baseNum, score);
                coinsReward = score * 120;
            } else if (radius >= 0.40 && radius <= 0.44) {
                score = baseNum * 2;
                descComp = Component.translatable("awakening_system.casino.darts.hit.double", baseNum, score);
                coinsReward = score * 80;
            } else {
                score = baseNum;
                descComp = Component.translatable("awakening_system.casino.darts.hit.single", baseNum, score);
                coinsReward = score * 40;
            }
        } else {
            descComp = Component.translatable("awakening_system.casino.darts.hit.miss");
            coinsReward = 0;
        }

        if (this.getOwner() instanceof ServerPlayer player) {
            net.minecraft.network.chat.MutableComponent finalDesc = descComp.copy();

            SystemData data = SystemEvents.data(player);
            if (coinsReward > 0) {
                com.soybe.awakeningsystem.casino.CasinoChipHelper.giveChips(player, coinsReward, false);
                data.addCasinoWinnings(coinsReward);
                AwakeningSystemMod.sync(player);
                finalDesc.append(Component.literal(" §6+" + String.format(java.util.Locale.US, "%,d", coinsReward) + " 🪙"));
            }

            DartboardServerHandler.onDartHit(pos, player, score, finalDesc);

            if (score == 50) {
                player.sendSystemMessage(Component.translatable("awakening_system.casino.darts.announcer_bullseye"));
                player.playNotifySound(com.soybe.awakeningsystem.ModSounds.DART_ANNOUNCER_BULLSEYE.get(), SoundSource.PLAYERS, 1.3F, 1.0F);
                if (player.level() instanceof ServerLevel sl) {
                    com.soybe.awakeningsystem.casino.CasinoTrophyManager.tryClaimTrophy(sl, player, 5, "Master Double Bullseye Shot (50 pts)");
                }
            } else if (score == 60) {
                player.sendSystemMessage(Component.translatable("awakening_system.casino.darts.announcer_triple", 20, 60));
                player.playNotifySound(com.soybe.awakeningsystem.ModSounds.DART_ANNOUNCER_TRIPLE.get(), SoundSource.PLAYERS, 1.3F, 1.0F);
                if (player.level() instanceof ServerLevel sl) {
                    com.soybe.awakeningsystem.casino.CasinoTrophyManager.tryClaimTrophy(sl, player, 5, "Master Triple 20 Max Shot (60 pts)");
                }
            } else if (score >= 45) {
                player.sendSystemMessage(Component.translatable("awakening_system.casino.darts.announcer_excellent", score));
                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.3F);
            }
        }
    }
}
