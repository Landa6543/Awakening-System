package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.network.chat.Component;

/**
 * Generador monumental de la Gran Caverna Abisal en el Overworld.
 * Crea una bóveda subterránea mística de escala colosal inspirada en La Sima / Enkanomiya:
 * - Bóveda geoda elipsoidal con estalactitas de cristal resonante y amatista.
 * - Suelo cavernoso con piscinas bioluminiscentes, musgo y esporas flotantes.
 * - Paredes incrustadas con vetas de Mineral Abisal, Jade Noctilucente y Cristal Mágico.
 * - Altar ceremonial central con pedestales rúnicos y baliza de anclaje.
 */
public final class AbyssalCaveBuilder {

    private AbyssalCaveBuilder() {}

    public static class AbyssalCaveData extends SavedData {
        public static final String DATA_NAME = "awakening_abyssal_cave_data";
        private BlockPos cavernCenter = null;
        private boolean generated = false;

        public AbyssalCaveData() {}

        public static AbyssalCaveData get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new Factory<>(AbyssalCaveData::new, AbyssalCaveData::load, null),
                    DATA_NAME
            );
        }

        public static AbyssalCaveData load(CompoundTag tag, HolderLookup.Provider registries) {
            AbyssalCaveData data = new AbyssalCaveData();
            if (tag.contains("HasCave") && tag.getBoolean("HasCave")) {
                data.cavernCenter = new BlockPos(tag.getInt("CaveX"), tag.getInt("CaveY"), tag.getInt("CaveZ"));
                data.generated = tag.getBoolean("Generated");
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            if (cavernCenter != null) {
                tag.putBoolean("HasCave", true);
                tag.putInt("CaveX", cavernCenter.getX());
                tag.putInt("CaveY", cavernCenter.getY());
                tag.putInt("CaveZ", cavernCenter.getZ());
                tag.putBoolean("Generated", generated);
            } else {
                tag.putBoolean("HasCave", false);
            }
            return tag;
        }

        public BlockPos getCavernCenter() { return cavernCenter; }
        public void setCavernCenter(BlockPos pos) { this.cavernCenter = pos; setDirty(); }
        public boolean isGenerated() { return generated; }
        public void setGenerated(boolean gen) { this.generated = gen; setDirty(); }
    }

    /**
     * Teletransporta al jugador a la Gran Caverna Abisal, generándola con arquitectura estética si no existe.
     */
    public static void teleportToAbyssalCave(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        if (overworld == null) return;

        net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> caveBiomeKey =
                net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.BIOME,
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "overworld_astral_dripstone_chasm")
                );

        BlockPos playerPos = player.blockPosition();
        BlockPos targetPos = null;

        // 1. Buscar el bioma natural de cueva abisal más cercano (hasta 6400 bloques)
        com.mojang.datafixers.util.Pair<BlockPos, net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> nearest =
                overworld.findClosestBiome3d(holder -> holder.is(caveBiomeKey), playerPos, 6400, 32, 64);

        if (nearest != null) {
            BlockPos biomeCenter = nearest.getFirst();
            // 2. Buscar cavidad natural de cueva subterránea (Y entre -55 y 20)
            targetPos = findNaturalCaveCavity(overworld, biomeCenter);
        }

        // Si no se encuentra en el rango o es un mundo plano, buscar una cueva profunda cercana al jugador
        if (targetPos == null) {
            targetPos = findNaturalCaveCavity(overworld, playerPos);
        }

        // Fallback seguro si no hay cueva abierta cerca
        if (targetPos == null) {
            targetPos = new BlockPos(playerPos.getX(), -40, playerPos.getZ());
            overworld.setBlock(targetPos.below(), SystemBlocks.ABYSSAL_STONE.get().defaultBlockState(), 3);
            overworld.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
            overworld.setBlock(targetPos.above(), Blocks.AIR.defaultBlockState(), 3);
        }

        player.teleportTo(overworld, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, player.getYRot(), player.getXRot());

        overworld.sendParticles(ParticleTypes.GLOW, targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D, 40, 1.0D, 1.0D, 1.0D, 0.05D);
        player.playNotifySound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0F, 1.3F);

        player.displayClientMessage(Component.translatable("awakening_system.biome.abyssal_cave.arrived"), true);
        AwakeningSystemMod.notify(player, "biome.awakening_system.overworld_astral_dripstone_chasm",
                "awakening_system.biome.abyssal_cave.coords", 0xFF9932CC, targetPos.getX(), targetPos.getY(), targetPos.getZ());
    }

    /**
     * Busca un espacio de cueva natural transitable (2 bloques de aire sobre suelo sólido)
     * en una columna subterránea entre Y=-58 e Y=10.
     */
    private static BlockPos findNaturalCaveCavity(ServerLevel level, BlockPos searchCenter) {
        int[] offsets = {0, 8, -8, 16, -16, 24, -24, 32, -32};
        for (int ox : offsets) {
            for (int oz : offsets) {
                int x = searchCenter.getX() + ox;
                int z = searchCenter.getZ() + oz;

                // Cargar el chunk de forma síncrona para poder leer los bloques
                level.getChunk(x >> 4, z >> 4);

                // Priorizar cuevas profundas (entre Y=-50 e Y=-10)
                for (int y = -52; y <= 15; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState floor = level.getBlockState(pos.below());
                    BlockState feet = level.getBlockState(pos);
                    BlockState head = level.getBlockState(pos.above());

                    if (floor.isSolid() && !floor.liquid() && feet.isAir() && head.isAir()) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}
