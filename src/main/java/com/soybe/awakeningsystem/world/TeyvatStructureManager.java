package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.NpcProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.io.InputStream;
import java.util.Optional;

public final class TeyvatStructureManager {
    private TeyvatStructureManager() {}

    private static final java.util.Map<String, StructureTemplate> TEMPLATE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile boolean spawnTreeGenerated = false;

    public static void clearCache() {
        TEMPLATE_CACHE.clear();
    }

    /**
     * Carga y coloca una estructura NBT en las coordenadas dadas con el desfase especificado.
     */
    public static boolean placeNbtStructure(ServerLevelAccessor level, BlockPos center, String name, int offsetX, int offsetY, int offsetZ) {
        try {
            ServerLevel serverLevel = level.getLevel();
            StructureTemplate template = TEMPLATE_CACHE.get(name);

            if (template == null) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, name);
                StructureTemplateManager stm = serverLevel.getStructureManager();
                Optional<StructureTemplate> opt = stm.get(id);

                if (opt.isPresent()) {
                    template = opt.get();
                } else {
                    String[] paths = {
                            "/data/awakening_system/structure/" + name + ".nbt",
                            "/data/awakening_system/structures/" + name + ".nbt"
                    };
                    for (String p : paths) {
                        try (InputStream in = TeyvatStructureManager.class.getResourceAsStream(p)) {
                            if (in != null) {
                                CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                                remapOldBlockNamespaces(tag);
                                template = new StructureTemplate();
                                template.load(serverLevel.holderLookup(Registries.BLOCK), tag);
                                break;
                            }
                        } catch (Exception e) {
                            AwakeningSystemMod.LOGGER.debug("Could not read template {} from {}: {}", name, p, e.getMessage());
                        }
                    }
                }
                if (template != null) {
                    TEMPLATE_CACHE.put(name, template);
                }
            }

            if (template != null) {
                BlockPos origin = center.offset(offsetX, offsetY, offsetZ);

                if (name.contains("hypos") || name.contains("bunker") || name.contains("regisvine") || name.contains("arena")) {
                    clearArenaCylinder(level, center, 12, 14);
                }

                StructurePlaceSettings settings = new StructurePlaceSettings()
                        .setIgnoreEntities(false)
                        .setKnownShape(true)
                        .setMirror(Mirror.NONE)
                        .setRotation(Rotation.NONE);

                template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);

                if (name.contains("shrine_of_depths")) {
                    net.minecraft.core.Vec3i size = template.getSize();
                    for (int dx = 0; dx < size.getX(); dx++) {
                        for (int dy = 0; dy < size.getY(); dy++) {
                            for (int dz = 0; dz < size.getZ(); dz++) {
                                BlockPos check = origin.offset(dx, dy, dz);
                                BlockState st = level.getBlockState(check);
                                if (st.is(Blocks.BARRIER)) {
                                    level.setBlock(check, com.soybe.awakeningsystem.block.SystemBlocks.ELEMENTAL_BARRIER.get().defaultBlockState(), 2);
                                }
                                if (st.is(Blocks.CHEST) || st.is(Blocks.TRAPPED_CHEST) || st.is(com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_TREASURE_CHEST.get())) {
                                    level.setBlock(check, com.soybe.awakeningsystem.block.SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState().setValue(com.soybe.awakeningsystem.block.LockedTreasureChestBlock.LOCKED, true), 3);
                                    TeyvatWorldGenManager.populateShrineChestLoot(level, check, level.getRandom());
                                }
                            }
                        }
                    }
                }
                                if (name.equalsIgnoreCase("temple")) {
                    net.minecraft.core.Vec3i size = template.getSize();
                    BlockPos thronePos = origin.offset(size.getX() / 2, 2, size.getZ() - 25);
                    if (serverLevel.getEntitiesOfClass(com.soybe.awakeningsystem.entity.StatueOfGodEntity.class, new net.minecraft.world.phys.AABB(thronePos).inflate(30.0D)).isEmpty()) {
                        com.soybe.awakeningsystem.entity.StatueOfGodEntity statue = com.soybe.awakeningsystem.entity.ModEntities.STATUE_OF_GOD.get().create(serverLevel);
                        if (statue != null) {
                            statue.moveTo(thronePos.getX() + 0.5D, thronePos.getY(), thronePos.getZ() + 0.5D, 180.0F, 0.0F);
                            statue.setYRot(180.0F);
                            statue.setPersistenceRequired();
                            serverLevel.addFreshEntity(statue);
                        }
                    }
                } else if (name.equalsIgnoreCase("igris_boss_room")) {
                    net.minecraft.core.Vec3i size = template.getSize();
                    BlockPos bossPos = origin.offset(size.getX() / 2, 2, size.getZ() / 2);
                    if (serverLevel.getEntitiesOfClass(com.soybe.awakeningsystem.entity.IgrisBossEntity.class, new net.minecraft.world.phys.AABB(bossPos).inflate(25.0D)).isEmpty()) {
                        com.soybe.awakeningsystem.entity.IgrisBossEntity igris = com.soybe.awakeningsystem.entity.ModEntities.IGRIS_BOSS.get().create(serverLevel);
                        if (igris != null) {
                            igris.moveTo(bossPos.getX() + 0.5D, bossPos.getY(), bossPos.getZ() + 0.5D, 180.0F, 0.0F);
                            igris.setPersistenceRequired();
                            serverLevel.addFreshEntity(igris);
                        }
                    }
                } else if (name.contains("dungeon_ant") && (name.contains("crystals") || name.endsWith("3"))) {
                    net.minecraft.core.Vec3i size = template.getSize();
                    BlockPos bossPos = origin.offset(size.getX() / 2, 2, size.getZ() / 2);
                    if (serverLevel.getEntitiesOfClass(com.soybe.awakeningsystem.entity.BeruBossEntity.class, new net.minecraft.world.phys.AABB(bossPos).inflate(20.0D)).isEmpty()) {
                        com.soybe.awakeningsystem.entity.BeruBossEntity beru = com.soybe.awakeningsystem.entity.ModEntities.BERU_BOSS.get().create(serverLevel);
                        if (beru != null) {
                            beru.moveTo(bossPos.getX() + 0.5D, bossPos.getY(), bossPos.getZ() + 0.5D, 0.0F, 0.0F);
                            beru.setPersistenceRequired();
                            serverLevel.addFreshEntity(beru);
                        }
                    }
                }

                if (serverLevel.getServer() != null) {
                    SeriesStructureWorldGen.SeriesStructureData.get(serverLevel.getServer()).recordStructure(name, center, serverLevel.dimension());
                }
                return true;
            } else {
                AwakeningSystemMod.LOGGER.error("No se pudo cargar la estructura NBT '{}'", name);
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al colocar estructura NBT '{}': ", name, e);
        }
        return false;
    }

    /**
     * Asegura la colocación del Gran Árbol de Viento (Windrise) en las proximidades del spawn de Teyvat.
     */
    public static synchronized void ensureWindriseTreeAtSpawn(ServerLevel level) {
        if (spawnTreeGenerated) return;

        BlockPos treePos = new BlockPos(90, 68, -140);
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 90, -140);
        if (surfaceY > 55 && surfaceY < 130) {
            treePos = new BlockPos(90, surfaceY, -140);
        }

        int centerChunkX = treePos.getX() >> 4;
        int centerChunkZ = treePos.getZ() >> 4;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                level.getChunk(centerChunkX + dx, centerChunkZ + dz);
            }
        }

        BlockPos checkPos = treePos.above(10);
        if (level.getBlockState(checkPos).is(Blocks.OAK_LOG) || level.getBlockState(checkPos).is(Blocks.DARK_OAK_LOG) || level.getBlockState(checkPos).is(Blocks.OAK_WOOD)) {
            spawnTreeGenerated = true;
            purgeParasiteTreesInsideWindrise(level, treePos);
            return;
        }

        int radius = 14;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq <= radius * radius) {
                    BlockPos p = treePos.offset(dx, -1, dz);
                    for (int dy = 0; dy <= 24; dy++) {
                        BlockPos down = p.below(dy);
                        BlockState s = level.getBlockState(down);
                        if (s.isSolid() && !s.is(BlockTags.LEAVES) && !s.is(BlockTags.LOGS) && s.getFluidState().isEmpty() && !s.is(BlockTags.REPLACEABLE)) {
                            break;
                        }
                        level.setBlock(down, dy < 2 ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState(), 2);
                    }
                }
            }
        }

        boolean success = placeNbtStructure(level, treePos, "windrise_giant_tree", -71, -2, -69);
        if (success) {
            spawnTreeGenerated = true;
            purgeParasiteTreesInsideWindrise(level, treePos);
            SeriesStructureWorldGen.SeriesStructureData data = SeriesStructureWorldGen.SeriesStructureData.get(level.getServer());
            data.recordStructure("teyvat_windrise_tree", treePos);

            TeyvatWorldGenManager.generateStatueOfTheSeven(level, treePos.offset(0, 0, 16));

            SeriesStructureBuilder.spawnNpcIfAbsent(level, treePos.offset(0, 1, 6), ModEntities.SERIES_NPC.get(), NpcProfile.GANYU);
            SeriesStructureBuilder.spawnNpcIfAbsent(level, treePos.offset(4, 1, 4), ModEntities.SERIES_NPC.get(), NpcProfile.MARCH_7TH);
        }
    }

    /**
     * Determina si un bloque es parte de vegetación, árboles naturales o árboles custom antiguos
     * (Eldoria, Mana Wood, árboles gigantes de fantasía, faroles, cadenas, etc.).
     */
    public static boolean isVegetationOrTreeBlock(BlockState state) {
        if (state.isAir()) return false;
        return state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.FLOWERS)
                || state.getBlock() instanceof LeavesBlock
                || state.is(Blocks.VINE)
                || state.is(Blocks.DEAD_BUSH)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.MANGROVE_ROOTS)
                || state.is(Blocks.SHROOMLIGHT)
                || state.is(Blocks.CHAIN)
                || state.is(Blocks.LANTERN)
                || state.is(SystemBlocks.ELDORIA_LOG.get())
                || state.is(SystemBlocks.ELDORIA_LEAVES.get())
                || state.is(SystemBlocks.ELDORIA_SAPLING.get())
                || state.is(SystemBlocks.MANA_WOOD_LOG.get())
                || state.is(SystemBlocks.MANA_WOOD_LEAVES.get())
                || state.is(SystemBlocks.MANA_WOOD_SAPLING.get())
                || state.is(SystemBlocks.CUIHUA_LOG.get())
                || state.is(SystemBlocks.CUIHUA_LEAVES.get())
                || state.is(SystemBlocks.CUIHUA_SAPLING.get());
    }

    /**
     * Limpia completamente un cilindro de arena (radio alrededor de center, hacia arriba y hacia abajo)
     * eliminando cualquier árbol, tronco, hoja, farol, cadena o vegetación previa.
     */
    public static int clearArenaCylinder(LevelAccessor level, BlockPos center, int radius, int heightUp) {
        int r = Math.min(radius, 12);
        int h = Math.min(heightUp, 14);
        int cleared = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int rSq = (r + 1) * (r + 1);
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                if (dx * dx + dz * dz <= rSq) {
                    for (int dy = -1; dy <= h; dy++) {
                        mpos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                        BlockState state = level.getBlockState(mpos);
                        if (isVegetationOrTreeBlock(state)) {
                            level.setBlock(mpos, Blocks.AIR.defaultBlockState(), 2);
                            cleared++;
                        }
                    }
                }
            }
        }
        return cleared;
    }

    /**
     * Limpia completamente cualquier vegetación (troncos, hojas, enredaderas, árboles)
     * en el área antes de colocar una estructura en Teyvat.
     */
    public static void clearAllVegetationInArea(LevelAccessor level, BlockPos origin, net.minecraft.core.Vec3i size) {
        int minX = origin.getX();
        int maxX = minX + size.getX() - 1;
        int minZ = origin.getZ();
        int maxZ = minZ + size.getZ() - 1;
        int startY = origin.getY();
        int endY = startY + size.getY() - 1;

        int cleared = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int topY = Math.min(endY, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z));
                for (int y = startY; y <= topY; y++) {
                    mpos.set(x, y, z);
                    BlockState state = level.getBlockState(mpos);
                    if (isVegetationOrTreeBlock(state)) {
                        level.setBlock(mpos, Blocks.AIR.defaultBlockState(), 2);
                        cleared++;
                    }
                }
            }
        }
        if (cleared > 0) {
            AwakeningSystemMod.LOGGER.info("Structure pre-clear: Eliminados {} bloques de vegetación en {}", cleared, origin);
        }
    }

    /**
     * Purga retroactivamente cualquier árbol parásito (abedules, robles vanilla con tronco de corte,
     * cuihua, hojas no persistentes) que se hayan generado dentro de la estructura de Windrise.
     * Preserva al 100% todos los bloques legítimos del Gran Roble (oak_wood, stripped_oak_wood,
     * dark_oak_wood, stripped_spruce_wood, hojas persistentes, etc.).
     */
    public static int purgeParasiteTreesInsideWindrise(ServerLevel level, BlockPos treePos) {
        BlockPos origin = treePos.offset(-71, -2, -69);
        int sizeX = 143;
        int sizeY = 136;
        int sizeZ = 138;

        int minX = origin.getX();
        int maxX = minX + sizeX - 1;
        int minY = origin.getY();
        int maxY = minY + sizeY - 1;
        int minZ = origin.getZ();
        int maxZ = minZ + sizeZ - 1;

        int purged = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int topY = Math.min(maxY, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z));
                for (int y = minY; y <= topY; y++) {
                    mpos.set(x, y, z);
                    BlockState st = level.getBlockState(mpos);
                    if (st.isAir()) continue;

                    boolean isParasite = false;

                    if (st.is(Blocks.BIRCH_LOG) || st.is(Blocks.BIRCH_WOOD) || st.is(Blocks.BIRCH_LEAVES)
                            || st.is(Blocks.STRIPPED_BIRCH_LOG) || st.is(Blocks.STRIPPED_BIRCH_WOOD)) {
                        isParasite = true;
                    }
                    else if (st.is(SystemBlocks.CUIHUA_LOG.get()) || st.is(SystemBlocks.CUIHUA_LEAVES.get())
                            || st.is(SystemBlocks.CUIHUA_SAPLING.get())) {
                        isParasite = true;
                    }
                    else if (st.is(Blocks.OAK_LOG)) {
                        isParasite = true;
                    }
                    else if (st.getBlock() instanceof LeavesBlock && st.hasProperty(LeavesBlock.PERSISTENT) && !st.getValue(LeavesBlock.PERSISTENT)) {
                        isParasite = true;
                    }
                    else if (st.is(BlockTags.SAPLINGS)) {
                        isParasite = true;
                    }

                    if (isParasite) {
                        level.setBlock(mpos, Blocks.AIR.defaultBlockState(), 2);
                        purged++;
                    }
                }
            }
        }

        if (purged > 0) {
            AwakeningSystemMod.LOGGER.info("Windrise parasite purge: {} bloques parásitos eliminados con éxito en {}", purged, treePos);
        }
        return purged;
    }

    private static void remapOldBlockNamespaces(CompoundTag tag) {
        if (tag.contains("palette", 9)) {
            net.minecraft.nbt.ListTag palette = tag.getList("palette", 10);
            for (int i = 0; i < palette.size(); i++) {
                CompoundTag state = palette.getCompound(i);
                String bName = state.getString("Name");
                if ("solo_leveling:magic_crystal".equals(bName)) {
                    state.putString("Name", "awakening_system:mana_crystal_cluster_c");
                }
            }
        }
        if (tag.contains("palettes", 9)) {
            net.minecraft.nbt.ListTag palettes = tag.getList("palettes", 9);
            for (int i = 0; i < palettes.size(); i++) {
                net.minecraft.nbt.ListTag pal = palettes.getList(i);
                for (int j = 0; j < pal.size(); j++) {
                    CompoundTag state = pal.getCompound(j);
                    String bName = state.getString("Name");
                    if ("solo_leveling:magic_crystal".equals(bName)) {
                        state.putString("Name", "awakening_system:mana_crystal_cluster_c");
                    }
                }
            }
        }
    }
}
