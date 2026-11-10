package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * El meteorito de Siderita mientras cae: un bloque de siderita de verdad, girando y envuelto en el fuego
 * normal de Minecraft, con una estela de llamas, lava y humo.
 * <p>
 * Lo mueve {@link com.soybe.awakeningsystem.world.SideriteMeteorManager} tick a tick por una recta desde lo
 * alto del cielo hasta el punto de impacto (así cae bien aunque su chunk no esté "activo"); esta entidad solo
 * se ve y deja la estela. El impacto y el cráter están en {@link #createImpact}.
 */
public class FallingSideriteMeteorEntity extends Entity {

    private static final EntityDataAccessor<BlockPos> TARGET_POS = SynchedEntityData.defineId(
            FallingSideriteMeteorEntity.class, EntityDataSerializers.BLOCK_POS);
    /** Tamaño a efectos de dibujo: el fuego del juego envuelve toda la caja de la entidad. */
    private static final net.minecraft.world.entity.EntityDimensions SIZE = net.minecraft.world.entity.EntityDimensions.fixed(1.6F, 1.6F);

    private int lifeTicks = 0;

    public FallingSideriteMeteorEntity(EntityType<? extends FallingSideriteMeteorEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.refreshDimensions();
    }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
        return SIZE;
    }

    /** Siempre ardiendo: el juego le dibuja sus llamas normales alrededor. */
    @Override
    public boolean displayFireAnimation() {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 512.0D * 512.0D;
    }

    public int getLifeTicks() {
        return lifeTicks;
    }

    public FallingSideriteMeteorEntity(Level level, Vec3 startPos, BlockPos targetPos) {
        this(ModEntities.FALLING_SIDERITE_METEOR.get(), level);
        this.setPos(startPos.x, startPos.y, startPos.z);
        this.setTargetPos(targetPos);

        Vec3 dir = targetPos.getCenter().subtract(startPos).normalize();
        this.setDeltaMovement(dir.scale(1.10D));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TARGET_POS, BlockPos.ZERO);
    }

    public BlockPos getTargetPos() {
        return this.entityData.get(TARGET_POS);
    }

    public void setTargetPos(BlockPos pos) {
        this.entityData.set(TARGET_POS, pos != null ? pos : BlockPos.ZERO);
    }

    @Override
    public void tick() {
        super.tick();
        this.lifeTicks++;
        if (!this.level().isClientSide()) {
            // sin nadie que lo mueva (por ejemplo tras reiniciar el servidor) no tiene sentido que siga ahí
            if (this.lifeTicks > 400) this.discard();
            return;
        }
        // Estela con partículas del juego: fuego y lava pegados al bloque, humo que queda flotando detrás.
        // "Siempre visibles": las partículas normales desaparecen a 32 bloques y el meteorito cae más lejos.
        double x = this.getX(), y = this.getY() + 0.8D, z = this.getZ();
        Vec3 back = this.getDeltaMovement().lengthSqr() > 1.0E-4 ? this.getDeltaMovement().normalize().scale(-0.4D) : Vec3.ZERO;
        for (int i = 0; i < 6; i++) {
            double rx = (this.random.nextDouble() - 0.5D) * 1.4D, ry = (this.random.nextDouble() - 0.5D) * 1.4D, rz = (this.random.nextDouble() - 0.5D) * 1.4D;
            this.level().addAlwaysVisibleParticle(ParticleTypes.FLAME, true, x + rx, y + ry, z + rz, back.x * 0.3, back.y * 0.3, back.z * 0.3);
        }
        for (int i = 0; i < 2; i++) {
            this.level().addAlwaysVisibleParticle(ParticleTypes.LAVA, true, x, y, z, 0, 0, 0);
        }
        for (int i = 0; i < 3; i++) {
            double rx = (this.random.nextDouble() - 0.5D) * 1.0D, rz = (this.random.nextDouble() - 0.5D) * 1.0D;
            this.level().addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, x + rx, y, z + rz, back.x * 0.15, 0.02, back.z * 0.15);
        }
        if (this.lifeTicks % 2 == 0) {
            // la columna de humo que deja en el cielo y que tarda en deshacerse
            this.level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, x, y, z, 0, 0.01, 0);
        }
    }

    /** Efectos, cráter y aviso del impacto. El aviso no da la ubicación: el jugador debe buscarlo. */
    public static void createImpact(ServerLevel sl, BlockPos impactPos) {
        {
            // 1. El golpe: explosión y trueno del propio juego
            sl.playSound(null, impactPos.getX(), impactPos.getY(), impactPos.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 10.0F, 0.65F);
            sl.playSound(null, impactPos.getX(), impactPos.getY(), impactPos.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 5.0F, 0.80F);

            // 2. La explosión, en el sitio real del impacto y con partículas del juego. Se envían "forzadas"
            // (las normales solo se ven a 32 bloques) a quien esté a menos de 512 bloques
            double cx = impactPos.getX() + 0.5D;
            double cy = impactPos.getY() + 1.0D;
            double cz = impactPos.getZ() + 0.5D;
            BlockState ground = sl.getBlockState(impactPos);
            for (ServerPlayer viewer : sl.players()) {
                if (viewer.distanceToSqr(cx, cy, cz) > 512.0D * 512.0D) continue;
                sl.sendParticles(viewer, ParticleTypes.EXPLOSION_EMITTER, true, cx, cy + 1.0D, cz, 5, 2.5D, 1.5D, 2.5D, 0.0D);
                sl.sendParticles(viewer, ParticleTypes.EXPLOSION, true, cx, cy + 1.0D, cz, 30, 4.0D, 2.0D, 4.0D, 0.0D);
                sl.sendParticles(viewer, ParticleTypes.LAVA, true, cx, cy, cz, 70, 3.0D, 1.0D, 3.0D, 0.0D);
                sl.sendParticles(viewer, ParticleTypes.LARGE_SMOKE, true, cx, cy + 1.0D, cz, 80, 3.5D, 2.0D, 3.5D, 0.05D);
                if (!ground.isAir()) {
                    // tierra y piedra que salen volando
                    sl.sendParticles(viewer, new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, ground), true,
                            cx, cy + 0.5D, cz, 150, 3.5D, 1.0D, 3.5D, 0.3D);
                }
                for (int i = 0; i < 32; i++) {
                    double angle = Math.PI * 2.0D * i / 32.0D;
                    // Con cantidad 0 los tres valores son la velocidad: un anillo de fuego que se abre a ras de suelo
                    sl.sendParticles(viewer, ParticleTypes.FLAME, true, cx, cy + 0.3D, cz, 0,
                            Math.cos(angle) * 0.8D, 0.2D, Math.sin(angle) * 0.8D, 1.0D);
                }
                // la columna de humo que sube del cráter
                for (int y = 0; y <= 30; y += 3) {
                    sl.sendParticles(viewer, ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true, cx, cy + y, cz, 3, 0.6D, 0.8D, 0.6D, 0.02D);
                }
            }

            // 4. Excavación Cóncava y Formación Real del Cráter de Impacto de Siderita (Hole / Bowl)
            final double craterRadius = 6.5D;
            final int maxDepth = 4;
            final int rCeil = (int) Math.ceil(craterRadius);

            for (int dx = -rCeil - 2; dx <= rCeil + 2; dx++) {
                for (int dz = -rCeil - 2; dz <= rCeil + 2; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > craterRadius + 1.8D) continue;

                    if (dist <= craterRadius) {
                        // Excavación cóncava parabólica: hueco profundo en el centro, suavizado hacia los bordes
                        double factor = 1.0D - (dist / craterRadius);
                        int cavityDepth = (int) Math.round(factor * factor * maxDepth);
                        int floorY = impactPos.getY() - cavityDepth;

                        // Ahuecar el interior del cráter hacia arriba (despejando árboles, tierra, agua y rocas)
                        for (int y = floorY + 1; y <= impactPos.getY() + 8; y++) {
                            BlockPos airPos = new BlockPos(impactPos.getX() + dx, y, impactPos.getZ() + dz);
                            BlockState stateToClear = sl.getBlockState(airPos);
                            if (canExcavate(sl, stateToClear)) {
                                sl.setBlockAndUpdate(airPos, Blocks.AIR.defaultBlockState());
                            }
                        }

                        // Formación del suelo y paredes del cráter con mezcla geológica fundida
                        BlockPos floorPos = new BlockPos(impactPos.getX() + dx, floorY, impactPos.getZ() + dz);
                        BlockState currentFloorState = sl.getBlockState(floorPos);
                        if (canTransform(sl, currentFloorState)) {
                            if (dx == 0 && dz == 0) {
                                // El meteorito en sí: una sola roca de mineral de siderita en el fondo del cráter
                                // (antes había 9 bloques de siderita y mineral debajo: regalaba demasiada)
                                sl.setBlockAndUpdate(floorPos, SystemBlocks.SIDERITE_ORE.get().defaultBlockState());
                            } else if (dist <= 1.5D) {
                                // la roca fundida que lo rodea
                                sl.setBlockAndUpdate(floorPos, sl.random.nextBoolean() ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState());
                            } else if (dist <= 3.2D) {
                                // Capa interior fundida: magma, obsidiana llorosa, basalto y, muy rara vez, una veta de siderita
                                float roll = sl.random.nextFloat();
                                if (roll < 0.03F) {
                                    sl.setBlockAndUpdate(floorPos, SystemBlocks.SIDERITE_ORE.get().defaultBlockState());
                                } else if (roll < 0.50F) {
                                    sl.setBlockAndUpdate(floorPos, Blocks.MAGMA_BLOCK.defaultBlockState());
                                } else if (roll < 0.70F) {
                                    sl.setBlockAndUpdate(floorPos, Blocks.CRYING_OBSIDIAN.defaultBlockState());
                                } else if (roll < 0.88F) {
                                    sl.setBlockAndUpdate(floorPos, Blocks.SMOOTH_BASALT.defaultBlockState());
                                } else {
                                    sl.setBlockAndUpdate(floorPos, Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                                }
                            } else {
                                // Paredes y pendiente exterior: Basalto, piedra negra, tierra calcinada y piedra agrietada
                                float roll = sl.random.nextFloat();
                                if (roll < 0.40F) {
                                    sl.setBlockAndUpdate(floorPos, Blocks.BASALT.defaultBlockState());
                                } else if (roll < 0.65F) {
                                    sl.setBlockAndUpdate(floorPos, Blocks.BLACKSTONE.defaultBlockState());
                                } else if (roll < 0.85F) {
                                    sl.setBlockAndUpdate(floorPos, Blocks.COBBLESTONE.defaultBlockState());
                                } else {
                                    sl.setBlockAndUpdate(floorPos, Blocks.COARSE_DIRT.defaultBlockState());
                                }
                            }

                            // Fuego residual o ascuas cósmicas en puntos dispersos de las paredes
                            if (sl.random.nextFloat() < 0.12F && sl.getBlockState(floorPos.above()).isAir()) {
                                sl.setBlockAndUpdate(floorPos.above(), Blocks.FIRE.defaultBlockState());
                            }
                        }
                    } else if (dist <= craterRadius + 1.8D) {
                        // Borde exterior elevado (Ejecta Lip): escombros levantados por la explosión
                        BlockPos rimPos = new BlockPos(impactPos.getX() + dx, impactPos.getY(), impactPos.getZ() + dz);
                        if (sl.getBlockState(rimPos).isAir() && !sl.getBlockState(rimPos.below()).isAir()) {
                            if (sl.random.nextFloat() < 0.40F) {
                                BlockState rimBlock = sl.random.nextBoolean() ? Blocks.BASALT.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState();
                                sl.setBlockAndUpdate(rimPos, rimBlock);
                            }
                        }
                    }
                }
            }

            // 4b. Restos del meteorito en el fondo del cráter: esquirlas para la brújula y fragmentos
            // de siderita pura, que solo se consiguen aquí. No desaparecen hasta que alguien los recoja.
            BlockPos lootPos = impactPos.below(maxDepth - 1);
            dropMeteorRemains(sl, lootPos, new ItemStack(SystemItems.METEOR_SHARD.get(), 3 + sl.random.nextInt(3)));
            dropMeteorRemains(sl, lootPos, new ItemStack(SystemItems.SIDERITE_FRAGMENT.get(), 1 + sl.random.nextInt(2)));

            // 5. Anuncio del impacto, sin revelar dónde fue
            Component announcement = Component.translatable("awakening_system.announcement.meteor_impact");
            for (ServerPlayer player : sl.players()) {
                player.sendSystemMessage(announcement);
            }
        }
    }

    private static void dropMeteorRemains(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D, stack, 0.0D, 0.0D, 0.0D);
        item.setUnlimitedLifetime();
        level.addFreshEntity(item);
    }

    private static boolean canExcavate(ServerLevel level, BlockState state) {
        if (state.isAir()) return false;
        if (state.getDestroySpeed(level, BlockPos.ZERO) < 0) return false; // Bedrock inmune
        if (state.is(Blocks.CHEST) || state.is(Blocks.BARREL) || state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL)) return false;
        return true;
    }

    private static boolean canTransform(ServerLevel level, BlockState state) {
        if (state.getDestroySpeed(level, BlockPos.ZERO) < 0) return false;
        if (state.is(Blocks.CHEST) || state.is(Blocks.BARREL) || state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL)) return false;
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.lifeTicks = compound.getInt("lifeTicks");
        if (compound.contains("targetPos")) {
            this.setTargetPos(NbtUtils.readBlockPos(compound, "targetPos").orElse(BlockPos.ZERO));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("lifeTicks", this.lifeTicks);
        BlockPos target = this.getTargetPos();
        if (target != null && target != BlockPos.ZERO) {
            compound.put("targetPos", NbtUtils.writeBlockPos(target));
        }
    }
}
