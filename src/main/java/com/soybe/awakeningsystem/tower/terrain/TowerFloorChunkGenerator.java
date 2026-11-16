package com.soybe.awakeningsystem.tower.terrain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The terrain of a Great Tower floor, made by the mod instead of Minecraft's noise: each floor has its own
 * land (see {@link TowerFloorTerrain}), always dry, with its own trees and features, built while the
 * chunk generates so light and heightmaps are right from the start.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class TowerFloorChunkGenerator extends ChunkGenerator {
    public static final MapCodec<TowerFloorChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
            Codec.INT.fieldOf("floor").forGetter(g -> g.floor)
    ).apply(i, TowerFloorChunkGenerator::new));

    private final int floor;

    public TowerFloorChunkGenerator(BiomeSource biomeSource, int floor) {
        super(biomeSource);
        this.floor = floor;
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.CHUNK_GENERATOR, helper ->
                helper.register(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "tower_floor"), CODEC));
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        TowerFloorTerrain.fill(floor, chunk);
        Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
        return CompletableFuture.completedFuture(chunk);
    }

    /** The biome's features (none on the tower's floors), then the floors that use Minecraft's own trees. */
    @Override
    public void applyBiomeDecoration(net.minecraft.world.level.WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        super.applyBiomeDecoration(level, chunk, structureManager);
        if (floor == 9) TowerFloorTerrainDeep.cherryTrees(level, this, chunk);
        TowerFloorTerrainAscent.vanillaTrees(floor, level, this, chunk);
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {}

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {}

    @Override
    public int getGenDepth() {
        return TowerFloorTerrain.isCave(floor) ? 128 : 384;
    }

    @Override
    public int getSeaLevel() {
        return -63;
    }

    @Override
    public int getMinY() {
        return TowerFloorTerrain.isCave(floor) ? 0 : -64;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return TowerFloorTerrain.height(floor, x, z) + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        int top = TowerFloorTerrain.height(floor, x, z);
        BlockState[] states = new BlockState[height.getHeight()];
        for (int i = 0; i < states.length; i++) {
            int y = height.getMinBuildHeight() + i;
            states[i] = y <= top ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(height.getMinBuildHeight(), states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Great Tower floor " + floor + " terrain");
    }
}
