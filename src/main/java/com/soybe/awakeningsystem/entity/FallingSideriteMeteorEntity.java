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
 * Entidad proyectil del meteorito de Siderita que cae del firmamento en trayectoria diagonal
 * con estela de partículas estelares e impacta en el suelo creando un cráter y soltando fragmentos.
 */
public class FallingSideriteMeteorEntity extends Entity {

    private static final EntityDataAccessor<BlockPos> TARGET_POS = SynchedEntityData.defineId(
            FallingSideriteMeteorEntity.class, EntityDataSerializers.BLOCK_POS);

    private int lifeTicks = 0;

    public FallingSideriteMeteorEntity(EntityType<? extends FallingSideriteMeteorEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
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

        BlockPos target = this.getTargetPos();
        Vec3 currentPos = this.position();

        // Calcular dirección hacia el objetivo
        if (target != null && target != BlockPos.ZERO) {
            Vec3 diff = target.getCenter().subtract(currentPos);
            double dist = diff.length();
            if (dist > 0.1D) {
                Vec3 dir = diff.normalize();
                double speed = Math.min(1.10D, dist);
                this.setDeltaMovement(dir.scale(speed));
            }
        }

        // Movimiento suave sin colisiones prematuras
        Vec3 nextPos = currentPos.add(this.getDeltaMovement());
        this.setPos(nextPos.x, nextPos.y, nextPos.z);

        // Estela cósmica y efectos visuales de bólido celeste en cada tick
        if (this.level().isClientSide()) {
            for (int i = 0; i < 14; i++) {
                double rx = (this.random.nextDouble() - 0.5D) * 1.0D;
                double ry = (this.random.nextDouble() - 0.5D) * 1.0D;
                double rz = (this.random.nextDouble() - 0.5D) * 1.0D;
                this.level().addParticle(ParticleTypes.FIREWORK, this.getX() + rx, this.getY() + ry, this.getZ() + rz, 0.0, 0.02, 0.0);
                this.level().addParticle(ParticleTypes.END_ROD, this.getX() + rx, this.getY() + ry, this.getZ() + rz, 0.0, 0.01, 0.0);
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX() + rx, this.getY() + ry, this.getZ() + rz, 0.0, 0.01, 0.0);
            }
            this.level().addParticle(ParticleTypes.FLASH, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.GLOW, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.level() instanceof ServerLevel sl) {
            // Emisión de partículas desde el servidor para visibilidad global
            sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 16, 0.7, 0.7, 0.7, 0.03);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 12, 0.6, 0.6, 0.6, 0.02);
            sl.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), 14, 0.6, 0.6, 0.6, 0.04);
            sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY(), this.getZ(), 4, 0.4, 0.4, 0.4, 0.01);

            if (this.lifeTicks % 5 == 0) {
                sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.WEATHER, 4.0F, 0.6F);
            }

            // Comprobar llegada al suelo / objetivo
            boolean arrived = false;
            if (target != null && target != BlockPos.ZERO) {
                if (this.getY() <= target.getY() + 1.2 || this.position().distanceToSqr(target.getCenter()) < 9.0) {
                    arrived = true;
                }
            }

            if (arrived || this.lifeTicks > 180) {
                this.detonateImpact();
            }
        }
    }

    private void detonateImpact() {
        if (this.level() instanceof ServerLevel sl) {
            BlockPos impactPos = this.blockPosition();
            BlockPos target = this.getTargetPos();
            if (target != null && target != BlockPos.ZERO) {
                impactPos = target;
            }
            createImpact(sl, impactPos);
        }

        this.discard();
    }

    /** Efectos, cráter y aviso del impacto. El aviso no da la ubicación: el jugador debe buscarlo. */
    public static void createImpact(ServerLevel sl, BlockPos impactPos) {
        {
            // 1. Sonidos resonantes de cataclismo estelar
            sl.playSound(null, impactPos.getX(), impactPos.getY(), impactPos.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 10.0F, 0.65F);
            sl.playSound(null, impactPos.getX(), impactPos.getY(), impactPos.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 5.0F, 0.80F);
            sl.playSound(null, impactPos.getX(), impactPos.getY(), impactPos.getZ(), SoundEvents.END_PORTAL_SPAWN, SoundSource.WEATHER, 4.0F, 1.4F);

            // 2. Ondas y ráfagas masivas de partículas cósmicas
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, impactPos.getX() + 0.5, impactPos.getY() + 1.0, impactPos.getZ() + 0.5, 4, 2.0, 2.0, 2.0, 0.0);
            sl.sendParticles(ParticleTypes.FLASH, impactPos.getX() + 0.5, impactPos.getY() + 1.0, impactPos.getZ() + 0.5, 3, 0.0, 0.0, 0.0, 0.0);
            sl.sendParticles(ParticleTypes.FIREWORK, impactPos.getX() + 0.5, impactPos.getY() + 1.0, impactPos.getZ() + 0.5, 150, 3.5, 2.5, 3.5, 0.40);
            sl.sendParticles(ParticleTypes.END_ROD, impactPos.getX() + 0.5, impactPos.getY() + 1.0, impactPos.getZ() + 0.5, 100, 3.0, 2.0, 3.0, 0.25);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, impactPos.getX() + 0.5, impactPos.getY() + 1.0, impactPos.getZ() + 0.5, 80, 2.5, 2.0, 2.5, 0.20);

            // 3. Columna de luz y humo cósmico de 45 bloques de altura para ubicar el cráter desde lejos
            for (int y = 0; y <= 45; y += 2) {
                sl.sendParticles(ParticleTypes.END_ROD, impactPos.getX() + 0.5, impactPos.getY() + y, impactPos.getZ() + 0.5, 4, 0.3, 0.5, 0.3, 0.01);
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, impactPos.getX() + 0.5, impactPos.getY() + y, impactPos.getZ() + 0.5, 3, 0.3, 0.5, 0.3, 0.03);
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
                            if (dist <= 1.4D) {
                                // Núcleo del impacto: Meteorito de Siderita puro encajado en el fondo
                                sl.setBlockAndUpdate(floorPos, SystemBlocks.SIDERITE_BLOCK.get().defaultBlockState());
                                BlockPos under = floorPos.below();
                                if (canTransform(sl, sl.getBlockState(under))) {
                                    sl.setBlockAndUpdate(under, SystemBlocks.SIDERITE_ORE.get().defaultBlockState());
                                }
                            } else if (dist <= 3.2D) {
                                // Capa interior fundida: Magma ardiente, obsidiana llorosa, basalto y vetas de siderita
                                float roll = sl.random.nextFloat();
                                if (roll < 0.20F) {
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
                                if (roll < 0.10F) {
                                    sl.setBlockAndUpdate(floorPos, SystemBlocks.SIDERITE_ORE.get().defaultBlockState());
                                } else if (roll < 0.40F) {
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

            // 5. Anuncio del impacto, sin revelar dónde fue
            Component announcement = Component.translatable("awakening_system.announcement.meteor_impact");
            for (ServerPlayer player : sl.players()) {
                player.sendSystemMessage(announcement);
            }
        }
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
