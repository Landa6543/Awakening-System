package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
import com.soybe.awakeningsystem.block.DragonNestBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.entity.DragonNestBlockEntity;
import com.soybe.awakeningsystem.entity.AncientDragonEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generación de Nidos de Dragón Ancestral 3D en el bioma Nido de Dragones Isekai.
 * Coloca un lecho de piedra/ceniza/magma, el nido con un huevo de variante aleatoria
 * y genera un Dragón Ancestral adulto que custodia ferozmente el nido.
 */
public class DragonNestFeature extends Feature<NoneFeatureConfiguration> {

    public DragonNestFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource rand = context.random();
        int chunkCenterX = (context.origin().getX() & ~15) + 8;
        int chunkCenterZ = (context.origin().getZ() & ~15) + 8;

        int searchY = Math.min(context.origin().getY() + 10, level.getMaxBuildHeight() - 10);
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(chunkCenterX, searchY, chunkCenterZ);
        while (probe.getY() > level.getMinBuildHeight() + 10 && (level.isEmptyBlock(probe) || level.getBlockState(probe).canBeReplaced() || level.getBlockState(probe).is(net.minecraft.tags.BlockTags.LEAVES))) {
            probe.move(net.minecraft.core.Direction.DOWN);
        }

        BlockPos origin = probe.above().immutable();
        BlockPos ground = origin.below();
        BlockState groundState = level.getBlockState(ground);

        if (groundState.isAir() || groundState.liquid() || groundState.is(net.minecraft.tags.BlockTags.LEAVES)) {
            return false;
        }

        if (!level.isEmptyBlock(origin) && !level.getBlockState(origin).canBeReplaced()) {
            return false;
        }

        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= 7.2D) {
                    BlockPos pFloor = origin.offset(dx, -1, dz);

                    if (dist <= 2.2D) {
                        if ((dx == 0 && dz == 0) || rand.nextInt(3) == 0) {
                            level.setBlock(pFloor, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                        } else if (rand.nextInt(4) == 0) {
                            level.setBlock(pFloor, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                        } else {
                            level.setBlock(pFloor, Blocks.POLISHED_BASALT.defaultBlockState(), 2);
                        }
                    } else if (dist <= 5.2D) {
                        float rollTreasure = rand.nextFloat();
                        if (rollTreasure < 0.08F) {
                            level.setBlock(pFloor, Blocks.RAW_GOLD_BLOCK.defaultBlockState(), 2);
                        } else if (rollTreasure < 0.16F) {
                            level.setBlock(pFloor, Blocks.GOLD_ORE.defaultBlockState(), 2);
                        } else if (rollTreasure < 0.22F) {
                            level.setBlock(pFloor, Blocks.BONE_BLOCK.defaultBlockState(), 2);
                        } else if (rollTreasure < 0.50F) {
                            level.setBlock(pFloor, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                        } else if (rollTreasure < 0.75F) {
                            level.setBlock(pFloor, Blocks.POLISHED_BASALT.defaultBlockState(), 2);
                        } else {
                            level.setBlock(pFloor, Blocks.BLACKSTONE.defaultBlockState(), 2);
                        }
                    } else {
                        BlockPos pRim = origin.offset(dx, 0, dz);
                        if (rand.nextFloat() < 0.65F && level.isEmptyBlock(pRim)) {
                            if (rand.nextFloat() < 0.20F) {
                                level.setBlock(pRim, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                            } else if (rand.nextFloat() < 0.40F) {
                                level.setBlock(pRim, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                            } else {
                                level.setBlock(pRim, Blocks.BLACKSTONE.defaultBlockState(), 2);
                            }
                        }
                        level.setBlock(pFloor, Blocks.BLACKSTONE.defaultBlockState(), 2);
                    }

                    for (int dy = (dist > 5.2D ? 1 : 0); dy <= 7; dy++) {
                        BlockPos airPos = origin.offset(dx, dy, dz);
                        if (!level.isEmptyBlock(airPos) && (level.getBlockState(airPos).canBeReplaced() || level.getBlockState(airPos).is(net.minecraft.tags.BlockTags.LEAVES))) {
                            level.setBlock(airPos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        int eggCount = rand.nextFloat() < 0.65F ? 2 : 1;
        int variant1 = rollRandomVariant(rand);
        int variant2 = rollRandomVariant(rand);

        BlockState nestState = SystemBlocks.DRAGON_NEST.get().defaultBlockState().setValue(DragonNestBlock.EGGS, eggCount);
        level.setBlock(origin, nestState, 3);

        BlockEntity be = level.getBlockEntity(origin);
        if (be instanceof DragonNestBlockEntity nestBe) {
            nestBe.setEggs(eggCount, variant1, variant2);
        }

        try {
            net.minecraft.world.phys.AABB checkArea = new net.minecraft.world.phys.AABB(origin).inflate(64.0D);
            if (rand.nextFloat() < 0.70F && level.getEntitiesOfClass(AncientDragonEntity.class, checkArea).isEmpty()) {
                AncientDragonEntity guardian = ModEntities.ANCIENT_DRAGON.get().create(level.getLevel());
                if (guardian != null) {
                    guardian.moveTo(origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D, rand.nextFloat() * 360.0F, 0.0F);
                    guardian.setVariant(variant1);
                    guardian.setDragonAge(AncientDragonEntity.ADULT_AGE_TICKS);
                    level.addFreshEntity(guardian);
                }
            }
        } catch (Exception ignored) {
        }

        return true;
    }

    private int rollRandomVariant(RandomSource rand) {
        float roll = rand.nextFloat();
        if (roll < 0.28F) {
            return AncientDragonEntity.VARIANT_FIRE;
        } else if (roll < 0.48F) {
            return AncientDragonEntity.VARIANT_SHADOW;
        } else if (roll < 0.66F) {
            return AncientDragonEntity.VARIANT_LIGHTNING;
        } else if (roll < 0.80F) {
            return AncientDragonEntity.VARIANT_ICE;
        } else if (roll < 0.90F) {
            return AncientDragonEntity.VARIANT_CELESTIAL;
        } else {
            return AncientDragonEntity.VARIANT_FOREST;
        }
    }
}
