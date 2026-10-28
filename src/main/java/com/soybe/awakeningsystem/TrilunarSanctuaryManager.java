package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gestor del Antiguo Santuario de las Tres Diosas de la Luna (Aria, Sonnet y Canon).
 * Construye el santuario sagrado con plataforma circular de mármol y cuarzo,
 * los tres pilares de las diosas y el Altar Trilunar central de Columbina.
 */
public final class TrilunarSanctuaryManager {

    private TrilunarSanctuaryManager() {}

    public static void buildSanctuary(ServerLevel level, BlockPos center) {
        int radius = 8;
        int baseY = center.getY();

        for (int x = -radius - 1; x <= radius + 1; x++) {
            for (int z = -radius - 1; z <= radius + 1; z++) {
                for (int y = 1; y <= 6; y++) {
                    level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= radius) {
                    BlockPos p = center.offset(x, 0, z);
                    if (dist > radius - 1.2) {
                        level.setBlock(p, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 3);
                    } else if (dist <= 2.2) {
                        level.setBlock(p, Blocks.CALCITE.defaultBlockState(), 3);
                    } else if ((x + z) % 3 == 0) {
                        level.setBlock(p, Blocks.SEA_LANTERN.defaultBlockState(), 3);
                    } else {
                        level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 3);
                    }
                    level.setBlock(p.below(), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
                }
            }
        }

        buildPillar(level, center.offset(0, 0, -5), Blocks.SMOOTH_QUARTZ, Blocks.SEA_LANTERN, "Aria");
        buildPillar(level, center.offset(4, 0, 3), Blocks.CALCITE, Blocks.GLOWSTONE, "Sonnet");
        buildPillar(level, center.offset(-4, 0, 3), Blocks.DARK_PRISMARINE, Blocks.AMETHYST_BLOCK, "Canon");

        BlockPos altarPos = center.above();
        level.setBlock(center, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 3);
        level.setBlock(altarPos, SystemBlocks.TRILUNAR_ALTAR.get().defaultBlockState(), 3);

        level.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5D, center.getY() + 2.0D, center.getZ() + 0.5D, 60, 2.0D, 1.0D, 2.0D, 0.08D);
        level.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5F, 1.2F);
    }

    private static void buildPillar(ServerLevel level, BlockPos base, net.minecraft.world.level.block.Block stone, net.minecraft.world.level.block.Block light, String goddess) {
        for (int y = 1; y <= 3; y++) {
            level.setBlock(base.above(y), stone.defaultBlockState(), 3);
        }
        level.setBlock(base.above(4), light.defaultBlockState(), 3);
        level.setBlock(base.above(5), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 3);
    }
}
