package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.FiveElementsPedestalBlock;
import com.soybe.awakeningsystem.block.WuXingElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class WuXingPuzzleManager {
    private WuXingPuzzleManager() {}

    public static void interact(ServerPlayer player, BlockPos pedestalPos, WuXingElement element) {
        ServerLevel level = player.serverLevel();
        BlockPos grottoCenter = findGrottoCenter(level, pedestalPos);

        BlockPos gateTestPos = new BlockPos(grottoCenter.getX(), grottoCenter.getY() + 2, grottoCenter.getZ() + 13);
        if (level.getBlockState(gateTestPos).isAir()) {
            player.displayClientMessage(Component.translatable("awakening_system.wuxing.already_open"), true);
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.3F);
            return;
        }

        BlockState currentState = level.getBlockState(pedestalPos);
        if (currentState.hasProperty(FiveElementsPedestalBlock.ACTIVE) && currentState.getValue(FiveElementsPedestalBlock.ACTIVE)) {
            player.displayClientMessage(Component.translatable("awakening_system.wuxing.already_active", element.getDisplayName()), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.7F, 1.2F);
            return;
        }

        List<BlockPos> roomPedestals = findPedestalsInRoom(level, grottoCenter);
        int activeCount = 0;
        for (BlockPos p : roomPedestals) {
            BlockState st = level.getBlockState(p);
            if (st.hasProperty(FiveElementsPedestalBlock.ACTIVE) && st.getValue(FiveElementsPedestalBlock.ACTIVE)) {
                activeCount++;
            }
        }

        if (element.getIndex() == activeCount) {
            int newActive = activeCount + 1;
            level.setBlock(pedestalPos, currentState.setValue(FiveElementsPedestalBlock.ACTIVE, true), 3);

            level.sendParticles(element.getParticle(),
                    pedestalPos.getX() + 0.5D, pedestalPos.getY() + 1.2D, pedestalPos.getZ() + 0.5D,
                    35, 0.3D, 0.3D, 0.3D, 0.05D);
            level.sendParticles(ParticleTypes.END_ROD,
                    pedestalPos.getX() + 0.5D, pedestalPos.getY() + 1.0D, pedestalPos.getZ() + 0.5D,
                    20, 0.2D, 0.2D, 0.2D, 0.04D);

            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 0.8F + newActive * 0.2F);
            player.playNotifySound(SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 0.9F, 1.0F + newActive * 0.15F);

            player.displayClientMessage(Component.translatable("awakening_system.wuxing.element_activated", element.getDisplayName(), newActive), true);

            if (newActive >= 5) {
                openSanctumGate(level, grottoCenter);

                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        grottoCenter.getX() + 0.5D, grottoCenter.getY() + 3.0D, grottoCenter.getZ() + 13.0D,
                        150, 2.5D, 2.0D, 2.5D, 0.2D);
                level.sendParticles(ParticleTypes.FLASH,
                        grottoCenter.getX() + 0.5D, grottoCenter.getY() + 3.0D, grottoCenter.getZ() + 13.0D,
                        5, 0, 0, 0, 0);

                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.4F, 1.0F);
                player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.2F, 1.2F);

                AwakeningSystemMod.notify(player,
                        "awakening_system.grotto.puzzle_solved_title",
                        "awakening_system.grotto.puzzle_solved_desc",
                        0xFF00FF7F);
                player.displayClientMessage(Component.translatable("awakening_system.wuxing.gate_open"), false);
            }
        } else {
            for (BlockPos p : roomPedestals) {
                BlockState st = level.getBlockState(p);
                if (st.hasProperty(FiveElementsPedestalBlock.ACTIVE) && st.getValue(FiveElementsPedestalBlock.ACTIVE)) {
                    level.setBlock(p, st.setValue(FiveElementsPedestalBlock.ACTIVE, false), 3);
                    level.sendParticles(ParticleTypes.SMOKE, p.getX() + 0.5D, p.getY() + 1.1D, p.getZ() + 0.5D, 20, 0.25D, 0.25D, 0.25D, 0.05D);
                }
            }

            player.hurt(level.damageSources().magic(), 6.0F);
            player.push(0.0D, 0.4D, -0.6D);

            level.sendParticles(ParticleTypes.EXPLOSION,
                    pedestalPos.getX() + 0.5D, pedestalPos.getY() + 1.0D, pedestalPos.getZ() + 0.5D,
                    15, 0.3D, 0.3D, 0.3D, 0.08D);
            level.sendParticles(ParticleTypes.SMOKE,
                    pedestalPos.getX() + 0.5D, pedestalPos.getY() + 1.0D, pedestalPos.getZ() + 0.5D,
                    30, 0.4D, 0.4D, 0.4D, 0.05D);

            player.playNotifySound(SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.9F, 1.2F);
            player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 1.2F, 0.6F);

            String expectedName = switch (activeCount) {
                case 0 -> "Madera (Mù - 木)";
                case 1 -> "Fuego (Huǒ - 火)";
                case 2 -> "Tierra (Tǔ - 土)";
                case 3 -> "Metal (Jīn - 金)";
                case 4 -> "Agua (Shuǐ - 水)";
                default -> "Madera (Mù - 木)";
            };

            player.displayClientMessage(Component.translatable("awakening_system.wuxing.dissonance", expectedName), true);
        }
    }

    public static List<BlockPos> findPedestalsInRoom(ServerLevel level, BlockPos center) {
        List<BlockPos> pedestals = new ArrayList<>();
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                for (int dy = -1; dy <= 3; dy++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() instanceof FiveElementsPedestalBlock) {
                        pedestals.add(p);
                    }
                }
            }
        }
        return pedestals;
    }

    public static BlockPos findGrottoCenter(ServerLevel level, BlockPos pos) {
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos candidate = pos.offset(dx, dy, dz);
                    if (level.getBlockState(candidate).is(Blocks.SEA_LANTERN)) {
                        return candidate;
                    }
                }
            }
        }

        if (level.getServer() != null) {
            var sData = SeriesStructureWorldGen.SeriesStructureData.get(level.getServer());
            for (var entry : sData.getAll().entrySet()) {
                if (entry.getKey().contains("immortal_grotto") || entry.getKey().contains("ancestral")) {
                    if (pos.closerThan(entry.getValue(), 60.0D)) {
                        return entry.getValue();
                    }
                }
            }
        }

        BlockPos minPos = pos;
        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() instanceof FiveElementsPedestalBlock) {
                        if (p.compareTo(minPos) < 0) {
                            minPos = p;
                        }
                    }
                }
            }
        }
        return minPos;
    }

    public static void openSanctumGate(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 1; dy <= 4; dy++) {
                for (int dz = 12; dz <= 14; dz++) {
                    BlockPos p = new BlockPos(cx + dx, cy + dy, cz + dz);
                    BlockState state = level.getBlockState(p);
                    if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        level.playSound(null, new BlockPos(cx, cy + 2, cz + 13), SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.5F, 0.7F);
        level.playSound(null, new BlockPos(cx, cy + 2, cz + 13), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.2F);
    }
}
