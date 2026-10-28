package com.soybe.awakeningsystem.client.map;

import com.mojang.blaze3d.platform.NativeImage;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.client.gui.GuiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Motor cartográfico y radar táctico en tiempo real del mod Awakening System.
 * Soporta renderizado topográfico 3D de superficie y modo cuevas automático transversal (Cave Mode).
 * Incluye radar de entidades con rostros reales, flechas de elevación (▲ / ▼),
 * aureolas de rango para bosses y waypoints con color HEX/RGB totalmente libre.
 */
public final class HunterMinimapRenderer {
    public static final int MAP_TEX_SIZE = 96; // 96x96 píxeles (default no invasivo)
    public static final int RADAR_RADIUS_BLOCKS = 48;

    private static DynamicTexture dynamicTexture;
    private static ResourceLocation textureLocation;
    private static int lastPlayerBlockX = Integer.MIN_VALUE;
    private static int lastPlayerBlockY = Integer.MIN_VALUE;
    private static int lastPlayerBlockZ = Integer.MIN_VALUE;
    private static String lastDimension = "";
    private static long lastSampleTimeMs = 0L;
    private static boolean lastWasCaveMode = false;
    private static int activeThreatLevel = 0; // 0 = seguro, 1 = Hostil de Élite, 2 = Jefe/Rango S, 3 = Apex Boss/Calamidad
    private static String activeThreatName = "";
    private static double activeThreatDist = 999.0D;
    private static long lastThreatAlertSoundMs = 0L;

    // Cachés de alto rendimiento para alcanzar 160-180+ FPS estables con Sodium
    private static boolean cachedCaveResult = false;
    private static int lastCaveCheckX = Integer.MIN_VALUE;
    private static int lastCaveCheckY = Integer.MIN_VALUE;
    private static int lastCaveCheckZ = Integer.MIN_VALUE;
    private static long lastCaveCheckMs = 0L;

    private static final java.util.Map<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>, Integer> BIOME_GRASS_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>, Integer> BIOME_WATER_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>, Integer> BIOME_FOLIAGE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private static final int[][] CHUNK_HEIGHTS = new int[18][18];
    private static final java.util.Set<Long> OCCUPIED_CELLS = new java.util.HashSet<>(64);
    private static final int[][] CIRCLE_DX_CACHE = new int[256][];

    private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> lastBiomeKey = null;
    private static String cachedBiomeName = "";

    private static final java.util.Map<Long, int[]> MINIMAP_CHUNK_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Long, Long> MINIMAP_CHUNK_TIME = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int[] EMPTY_CHUNK_PIXELS = new int[256];
    static {
        java.util.Arrays.fill(EMPTY_CHUNK_PIXELS, 0xFF080D13);
    }

    private static final java.util.List<LivingEntity> cachedRadarEntities = new java.util.ArrayList<>();
    private static long lastRadarScanTimeMs = 0L;

    private static final java.util.List<ItemEntity> cachedRadarItems = new java.util.ArrayList<>();
    private static long lastItemRadarScanTimeMs = 0L;

    private HunterMinimapRenderer() {}

    public static void release() {
        if (dynamicTexture != null) {
            dynamicTexture.close();
            dynamicTexture = null;
        }
        textureLocation = null;
        MINIMAP_CHUNK_CACHE.clear();
        MINIMAP_CHUNK_TIME.clear();
        BIOME_GRASS_CACHE.clear();
        BIOME_WATER_CACHE.clear();
        BIOME_FOLIAGE_CACHE.clear();
        OCCUPIED_CELLS.clear();
        cachedRadarEntities.clear();
        cachedRadarItems.clear();
        lastBiomeKey = null;
        cachedBiomeName = "";
        EntityIconRenderer.clearTextureCache();
        WorldMapChunkCache.flushAll();
    }

    public static int getQuickGrassColor(ClientLevel level, BlockPos pos) {
        var biomeHolder = level.getBiome(pos);
        Integer cached = BIOME_GRASS_CACHE.get(biomeHolder);
        if (cached != null) return cached;
        int col = BiomeColors.getAverageGrassColor(level, pos);
        BIOME_GRASS_CACHE.put(biomeHolder, col);
        return col;
    }

    public static int getQuickWaterColor(ClientLevel level, BlockPos pos) {
        var biomeHolder = level.getBiome(pos);
        Integer cached = BIOME_WATER_CACHE.get(biomeHolder);
        if (cached != null) return cached;
        int col = BiomeColors.getAverageWaterColor(level, pos);
        BIOME_WATER_CACHE.put(biomeHolder, col);
        return col;
    }

    public static int getQuickFoliageColor(ClientLevel level, BlockPos pos) {
        var biomeHolder = level.getBiome(pos);
        Integer cached = BIOME_FOLIAGE_CACHE.get(biomeHolder);
        if (cached != null) return cached;
        int col = BiomeColors.getAverageFoliageColor(level, pos);
        BIOME_FOLIAGE_CACHE.put(biomeHolder, col);
        return col;
    }

    public static boolean isCaveActive() {
        return lastWasCaveMode;
    }

    public static boolean isExternalMapModLoaded() {
        try {
            var ml = net.neoforged.fml.ModList.get();
            if (ml != null) {
                return ml.isLoaded("journeymap") || ml.isLoaded("xaerominimap") || ml.isLoaded("xaeroworldmap");
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static String getDetectedExternalMapName() {
        try {
            var ml = net.neoforged.fml.ModList.get();
            if (ml != null) {
                if (ml.isLoaded("journeymap")) return "JourneyMap";
                if (ml.isLoaded("xaerominimap")) return "Xaero's Minimap";
                if (ml.isLoaded("xaeroworldmap")) return "Xaero's WorldMap";
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static boolean isDioramaMinimapActive() {
        try {
            return dev.diorama.Settings.get().minimap;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isMinimapActive() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return false;
        return isDioramaMinimapActive();
    }

    public static float getEffectiveScale(int screenH) {
        float baseScale = Math.max(0.5F, Math.min(2.5F, GuiConfig.minimapScale));
        // Adaptación dinámica inteligente a la resolución y tamaño de la pantalla
        float responsiveRatio = Math.max(0.75F, Math.min(2.0F, (float) screenH / 360.0F));
        return baseScale * responsiveRatio;
    }

    public static int getMinimapBottom(int screenW, int screenH) {
        if (!isMinimapActive()) return 0;
        float scale = getEffectiveScale(screenH);
        int mapPixelSize = (int) (MAP_TEX_SIZE * scale);
        boolean isCompact = mapPixelSize < 80;
        int ribbonOffset = isCompact ? 27 : 32;
        int mapY = GuiConfig.minimapY >= 0 ? Mth.clamp(GuiConfig.minimapY, 16, Math.max(16, screenH - mapPixelSize - ribbonOffset)) : 16;
        return mapY + mapPixelSize + ribbonOffset;
    }

    /**
     * Renderiza el minimapa en el HUD según las coordenadas y opciones de GuiConfig.
     */
    public static void render(GuiGraphics g, int screenW, int screenH) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        // Si el usuario tiene una pantalla abierta distinta al chat o el editor de HUD, no dibujar
        if (mc.screen != null && !(mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen)) {
            return;
        }

        // 1. Actualizar siempre la detección de amenazas tácticas (Bosses, Monarcas, Rango S, Calamidades)
        updateThreatDetection(mc);

        // 2. Renderizar alerta de peligro inminente en Top-Center (no colisiona con el minimapa ni la tarjeta de estado)
        if (GuiConfig.minimapDangerAlert && activeThreatLevel > 0) {
            renderDangerAlertBannerCentered(g, mc, screenW);
        }

        // 3. El minimapa 2D antiguo ha sido completamente eliminado del mod.
        // Diorama es ahora el minimapa oficial e integrado de Awakening System.
        // Si el usuario desactiva el minimapa de Diorama en sus ajustes, no se renderiza ningún minimapa.
    }

    private static boolean evaluateCaveMode(ClientLevel level, Player player) {
        String mode = GuiConfig.minimapCaveMode != null ? GuiConfig.minimapCaveMode.toUpperCase(java.util.Locale.ROOT) : "AUTO";
        if ("ALWAYS".equals(mode)) return true;
        if ("NEVER".equals(mode)) return false;

        if (level.dimensionType().hasCeiling()) return true;

        long now = System.currentTimeMillis();
        int px = player.getBlockX();
        int py = player.getBlockY();
        int pz = player.getBlockZ();

        // Evitar ejecutar decenas de getBlockState() cada fotograma si el jugador no cambió de bloque o pasaron menos de 400ms
        if (px == lastCaveCheckX && py == lastCaveCheckY && pz == lastCaveCheckZ && (now - lastCaveCheckMs) < 400L) {
            return cachedCaveResult;
        }

        lastCaveCheckX = px;
        lastCaveCheckY = py;
        lastCaveCheckZ = pz;
        lastCaveCheckMs = now;

        BlockPos pos = player.blockPosition();
        if (level.canSeeSky(pos)) {
            cachedCaveResult = false;
            return false;
        }

        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        if (skyLight > 2) {
            cachedCaveResult = false;
            return false;
        }

        // Comprobar la altura de la superficie en este punto del chunk
        net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, net.minecraft.world.level.chunk.status.ChunkStatus.FULL, false);
        if (chunk != null && !chunk.isEmpty()) {
            int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() & 15, pos.getZ() & 15);
            if (surfaceY > pos.getY() + 4) {
                cachedCaveResult = true;
                return true; // La superficie está claramente por encima del jugador y no hay cielo visible
            }
        }

        int solidAbove = 0;
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        for (int dy = 2; dy <= 60; dy++) {
            mPos.set(pos.getX(), pos.getY() + dy, pos.getZ());
            if (mPos.getY() >= level.getMaxBuildHeight()) break;
            if (level.getBlockState(mPos).isSolidRender(level, mPos)) {
                solidAbove++;
                if (solidAbove >= 2) {
                    cachedCaveResult = true;
                    return true;
                }
            }
        }
        cachedCaveResult = false;
        return false;
    }

    private static void updateMapTextureIfNeeded(ClientLevel level, Player player, boolean isCave, boolean isCircular) {
        long now = System.currentTimeMillis();
        int px = player.getBlockX();
        int py = player.getBlockY();
        int pz = player.getBlockZ();
        String currentDim = level.dimension().location().toString();

        boolean moved = Math.abs(px - lastPlayerBlockX) >= 1 || Math.abs(pz - lastPlayerBlockZ) >= 1 || Math.abs(py - lastPlayerBlockY) >= 2;
        boolean dimChanged = !currentDim.equals(lastDimension);
        boolean caveChanged = isCave != lastWasCaveMode;
        boolean timeExpired = (now - lastSampleTimeMs) > 1500L;

        // Limitar la tasa de re-baking y upload a OpenGL a un máximo de 10 Hz (100ms) mientras el jugador se mueve,
        // evitando stalls en el hilo de render de Sodium a 160-180+ FPS.
        if (moved && (now - lastSampleTimeMs) < 100L && !dimChanged && !caveChanged) {
            return;
        }

        if (!moved && !dimChanged && !caveChanged && !timeExpired && dynamicTexture != null) {
            return;
        }

        if (dimChanged || caveChanged) {
            MINIMAP_CHUNK_CACHE.clear();
            MINIMAP_CHUNK_TIME.clear();
            BIOME_GRASS_CACHE.clear();
            BIOME_WATER_CACHE.clear();
            BIOME_FOLIAGE_CACHE.clear();
        }

        lastPlayerBlockX = px;
        lastPlayerBlockY = py;
        lastPlayerBlockZ = pz;
        lastDimension = currentDim;
        lastSampleTimeMs = now;
        lastWasCaveMode = isCave;

        if (dynamicTexture == null) {
            NativeImage img = new NativeImage(MAP_TEX_SIZE, MAP_TEX_SIZE, false);
            dynamicTexture = new DynamicTexture(img);
            dynamicTexture.setFilter(false, false);
            textureLocation = Minecraft.getInstance().getTextureManager().register("hunter_minimap_tex", dynamicTexture);
        }

        NativeImage img = dynamicTexture.getPixels();
        if (img == null) return;

        int halfTex = MAP_TEX_SIZE / 2;
        int maxRadiusSq = (halfTex - 1) * (halfTex - 1);
        float zoom = Math.max(0.5F, Math.min(3.0F, GuiConfig.minimapZoom));

        int minBuildY = level.getMinBuildHeight();
        int maxBuildY = level.getMaxBuildHeight();

        int caveY = py;
        if (isCave && level.canSeeSky(player.blockPosition())) {
            caveY = Math.min(py - 16, 32);
        }

        float dayNightMultiplier = 1.0F;
        String lightMode = GuiConfig.minimapLightingMode != null ? GuiConfig.minimapLightingMode.toUpperCase(java.util.Locale.ROOT) : "DAY";
        if ("NIGHT".equals(lightMode)) {
            dayNightMultiplier = 0.85F; // Visibilidad nocturna táctica clara, sin oscurecer en marrón
        } else if ("AUTO".equals(lightMode)) {
            float skyAngle = level.getTimeOfDay(1.0F);
            float sunFactor = Mth.clamp(Mth.cos(skyAngle * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
            dayNightMultiplier = Mth.lerp(sunFactor, 0.90F, 1.0F); // Máximo 10% de sombreado sutil, manteniendo colores vivos
        }

        int lastCx = Integer.MIN_VALUE;
        int lastCz = Integer.MIN_VALUE;
        int[] curChunk = null;

        for (int lz = 0; lz < MAP_TEX_SIZE; lz++) {
            int dz = lz - halfTex;
            for (int lx = 0; lx < MAP_TEX_SIZE; lx++) {
                int dx = lx - halfTex;
                int distSq = dx * dx + dz * dz;

                // En modo circular, los píxeles fuera del círculo son transparentes
                if (isCircular && distSq > maxRadiusSq) {
                    img.setPixelRGBA(lx, lz, 0x00000000);
                    continue;
                }

                // Coordenadas en bloques del mundo (ajustadas por zoom)
                int wx = px + (int) Math.round(dx / zoom);
                int wz = pz + (int) Math.round(dz / zoom);

                int cx = wx >> 4;
                int cz = wz >> 4;

                if (cx != lastCx || cz != lastCz || curChunk == null) {
                    curChunk = getOrSampleMinimapChunk(level, cx, cz, isCave, caveY, minBuildY, maxBuildY);
                    lastCx = cx;
                    lastCz = cz;
                }

                int abgr = (curChunk != null) ? curChunk[((wz & 15) << 4) | (wx & 15)] : 0xFF080D13;

                // Aplicar iluminación diurna si no es cueva
                if (!isCave && dayNightMultiplier < 0.98F) {
                    abgr = applyDayNightToPixel(abgr, dayNightMultiplier);
                }

                // Viñeta circular sutil en el borde para transición suave
                if (isCircular && distSq > (maxRadiusSq - 160)) {
                    float edgeFade = 1.0F - (float) (distSq - (maxRadiusSq - 160)) / 160.0F;
                    edgeFade = Mth.clamp(edgeFade, 0.2F, 1.0F);
                    int a = (int) (255 * edgeFade);
                    abgr = (abgr & 0x00FFFFFF) | (a << 24);
                }

                img.setPixelRGBA(lx, lz, abgr);
            }
        }

        dynamicTexture.upload();
    }

    /**
     * Muestreo transversal inteligente para cavernas y minas con algoritmo vertical tipo JourneyMap.
     * Muestra las paredes rocosas en 3D, túneles transitables continuos, lava y agua iluminada sin vacíos negros.
     */
    private static int sampleCavePixel(ClientLevel level, int wx, int py, int wz, int minBuildY, BlockPos.MutableBlockPos mPos) {
        int maxBuildY = level.getMaxBuildHeight();
        int sliceTop = Math.min(maxBuildY - 1, py + 12);
        int sliceBottom = Math.max(minBuildY, py - 18);

        // 1. Buscar espacio abierto (aire o fluido) en la rodaja vertical más cercana a py
        int airY = -999;
        for (int y = py; y <= sliceTop; y++) {
            mPos.set(wx, y, wz);
            BlockState st = level.getBlockState(mPos);
            if (!st.isSolidRender(level, mPos)) {
                airY = y;
                break;
            }
        }
        if (airY == -999) {
            for (int y = py - 1; y >= sliceBottom; y--) {
                mPos.set(wx, y, wz);
                BlockState st = level.getBlockState(mPos);
                if (!st.isSolidRender(level, mPos)) {
                    airY = y;
                    break;
                }
            }
        }

        // 2. Si toda la columna es roca sólida en esta rodaja: comprobar si es muro de caverna
        if (airY == -999) {
            boolean westAir = !level.getBlockState(mPos.set(wx - 1, py, wz)).isSolidRender(level, mPos);
            boolean northAir = !level.getBlockState(mPos.set(wx, py, wz - 1)).isSolidRender(level, mPos);
            boolean eastAir = !level.getBlockState(mPos.set(wx + 1, py, wz)).isSolidRender(level, mPos);
            boolean southAir = !level.getBlockState(mPos.set(wx, py, wz + 1)).isSolidRender(level, mPos);
            boolean isWall = westAir || northAir || eastAir || southAir;

            if (isWall) {
                mPos.set(wx, py, wz);
                BlockState wallState = level.getBlockState(mPos);
                int baseCol = getBlockBaseRgb(level, wallState, mPos);
                float wallShade = 0.85F;
                if (northAir || westAir) wallShade += 0.30F; // Resalte Noroeste nítido (JourneyMap style)
                if (southAir || eastAir) wallShade -= 0.25F; // Sombra Sureste

                int r = Mth.clamp((int) (((baseCol >> 16) & 0xFF) * wallShade), 0, 255);
                int g = Mth.clamp((int) (((baseCol >> 8) & 0xFF) * wallShade), 0, 255);
                int b = Mth.clamp((int) ((baseCol & 0xFF) * wallShade), 0, 255);
                return (0xFF << 24) | (b << 16) | (g << 8) | r;
            } else {
                // Roca no expuesta / manto sólido (azul noche / gris pizarra táctico)
                return 0xFF0D1219;
            }
        }

        // 3. Encontramos espacio abierto: escanear hacia abajo desde airY para hallar el suelo de la cueva
        int floorY = airY;
        BlockState floorState = level.getBlockState(mPos.set(wx, floorY, wz));
        while (floorY > minBuildY) {
            mPos.set(wx, floorY, wz);
            BlockState st = level.getBlockState(mPos);
            if (st.isSolidRender(level, mPos) || !st.getFluidState().isEmpty()) {
                floorState = st;
                break;
            }
            floorY--;
        }

        int r, g, b;
        if (!floorState.getFluidState().isEmpty()) {
            if (floorState.getFluidState().is(FluidTags.LAVA)) {
                r = 255; g = 115; b = 15; // Lava brillante
            } else {
                r = 30; g = 110; b = 220; // Agua subterránea vibrante
            }
        } else {
            int col = getBlockBaseRgb(level, floorState, mPos);
            int drop = py - floorY;
            float depthFactor = Mth.clamp(1.0F - drop * 0.035F, 0.40F, 1.25F);

            mPos.set(wx, floorY + 1, wz);
            int blockLight = level.getBrightness(LightLayer.BLOCK, mPos);
            if (blockLight == 0) {
                depthFactor *= 0.65F; // Claridad ambiental de caverna (nunca negro ciego)
            } else {
                float lightRatio = blockLight / 15.0F;
                depthFactor = depthFactor * 0.70F + lightRatio * 0.75F;
            }

            r = Mth.clamp((int) (((col >> 16) & 0xFF) * depthFactor), 0, 255);
            g = Mth.clamp((int) (((col >> 8) & 0xFF) * depthFactor), 0, 255);
            b = Mth.clamp((int) ((col & 0xFF) * depthFactor), 0, 255);

            // Calidez de antorchas y luz artificial
            if (blockLight > 0) {
                float warmRatio = blockLight / 15.0F;
                r = Mth.clamp((int) (r * (1.0F + warmRatio * 0.35F)), 0, 255);
                g = Mth.clamp((int) (g * (1.0F + warmRatio * 0.18F)), 0, 255);
            }
        }

        return (0xFF << 24) | (b << 16) | (g << 8) | r;
    }

    private static int applyDayNightToPixel(int abgr, float factor) {
        int a = (abgr >> 24) & 0xFF;
        int b = (abgr >> 16) & 0xFF;
        int g = (abgr >> 8) & 0xFF;
        int r = abgr & 0xFF;

        r = (int) (r * factor);
        g = (int) (g * factor);
        b = (int) (b * factor);

        if (factor < 0.90F) {
            float nightRatio = (0.90F - factor) / 0.55F;
            r = Mth.clamp((int) (r * (1.0F - nightRatio * 0.12F)), 0, 255);
            g = Mth.clamp((int) (g * (1.0F - nightRatio * 0.08F)), 0, 255);
            b = Mth.clamp((int) (b * (1.0F + nightRatio * 0.15F)), 0, 255);
        }

        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private static int getColSurfaceY(ClientLevel level, LevelChunk currentChunk, int wx, int wz, int lx, int lz, int minBuildY, BlockPos.MutableBlockPos p) {
        LevelChunk chunk = (lx >= 0 && lx < 16 && lz >= 0 && lz < 16) ? currentChunk : level.getChunkSource().getChunk(wx >> 4, wz >> 4, ChunkStatus.FULL, false);
        if (chunk != null && !chunk.isEmpty()) {
            int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, wx & 15, wz & 15);
            int steps = 0;
            while (y > minBuildY && steps < 30) {
                p.set(wx, y, wz);
                BlockState st = level.getBlockState(p);
                if (!st.isAir()) {
                    if (!st.getFluidState().isEmpty() || st.getMapColor(level, p) != MapColor.NONE) {
                        return y;
                    }
                }
                y--;
                steps++;
            }
            return Math.max(minBuildY, y);
        }
        return level.getSeaLevel();
    }

    private static int[] getOrSampleMinimapChunk(ClientLevel level, int cx, int cz, boolean isCave, int playerY, int minBuildY, int maxBuildY) {
        long key = (((long) (isCave ? 1 : 0)) << 62) | (((long) (cx & 0x1FFFFFFF)) << 31) | (cz & 0x1FFFFFFF);
        int[] cached = MINIMAP_CHUNK_CACHE.get(key);
        long now = System.currentTimeMillis();
        if (cached != null) {
            long lastTime = MINIMAP_CHUNK_TIME.getOrDefault(key, 0L);
            if ((now - lastTime) < 20000L) {
                return cached;
            }
        }

        LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
        if (chunk == null || chunk.isEmpty()) {
            return cached != null ? cached : EMPTY_CHUNK_PIXELS;
        }

        int[] pixels = cached != null ? cached : new int[256];
        int startX = cx << 4;
        int startZ = cz << 4;
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        if (isCave) {
            for (int lz = 0; lz < 16; lz++) {
                for (int lx = 0; lx < 16; lx++) {
                    int wx = startX + lx;
                    int wz = startZ + lz;
                    pixels[(lz << 4) | lx] = sampleCavePixel(level, wx, playerY, wz, minBuildY, mPos);
                }
            }
        } else {
            for (int lz = -1; lz < 16; lz++) {
                for (int lx = -1; lx < 16; lx++) {
                    CHUNK_HEIGHTS[lz + 1][lx + 1] = getColSurfaceY(level, chunk, startX + lx, startZ + lz, lx, lz, minBuildY, mPos);
                }
            }

            for (int lz = 0; lz < 16; lz++) {
                for (int lx = 0; lx < 16; lx++) {
                    int wx = startX + lx;
                    int wz = startZ + lz;
                    int surfaceY = CHUNK_HEIGHTS[lz + 1][lx + 1];
                    mPos.set(wx, surfaceY, wz);
                    BlockState state = level.getBlockState(mPos);

                    if (state.isAir() || (level.dimension().location().getPath().contains("subspace") && state.is(Blocks.BEDROCK))) {
                        pixels[(lz << 4) | lx] = 0xFF080D13;
                        continue;
                    }

                    int rgb;
                    boolean isWater = !state.getFluidState().isEmpty() && state.getFluidState().is(FluidTags.WATER);
                    if (isWater) {
                        int biomeWater = getQuickWaterColor(level, mPos);
                        int seabedY = surfaceY - 1;
                        while (seabedY > minBuildY && (surfaceY - seabedY) < 14) {
                            mPos.set(wx, seabedY, wz);
                            if (!level.getFluidState(mPos).is(FluidTags.WATER)) break;
                            seabedY--;
                        }
                        mPos.set(wx, seabedY, wz);
                        BlockState seabedState = level.getBlockState(mPos);
                        int seabedRgb = getBlockBaseRgb(level, seabedState, mPos);

                        int depth = Math.max(1, surfaceY - seabedY);
                        float alpha = Math.min(0.92F, 0.36F + (depth - 1) * 0.075F);
                        float depthShade = Math.max(0.72F, 1.0F - (depth - 1) * 0.022F);

                        float wr = ((biomeWater >> 16) & 0xFF) / 255.0F;
                        float wg = ((biomeWater >> 8) & 0xFF) / 255.0F;
                        float wb = (biomeWater & 0xFF) / 255.0F;
                        float sr = ((seabedRgb >> 16) & 0xFF) / 255.0F;
                        float sg = ((seabedRgb >> 8) & 0xFF) / 255.0F;
                        float sb = (seabedRgb & 0xFF) / 255.0F;

                        int r = Mth.clamp((int) ((wr * alpha + sr * (1.0F - alpha)) * depthShade * 255.0F), 0, 255);
                        int g = Mth.clamp((int) ((wg * alpha + sg * (1.0F - alpha)) * depthShade * 255.0F), 0, 255);
                        int b = Mth.clamp((int) ((wb * alpha + sb * (1.0F - alpha)) * depthShade * 255.0F), 0, 255);
                        rgb = (r << 16) | (g << 8) | b;
                    } else {
                        rgb = getBlockBaseRgb(level, state, mPos);
                    }

                    float slope = 1.0F;
                    if (!isWater) {
                        int yNW = CHUNK_HEIGHTS[lz][lx];
                        int yN = CHUNK_HEIGHTS[lz][lx + 1];
                        int yW = CHUNK_HEIGHTS[lz + 1][lx];
                        float diffN = surfaceY - yN;
                        float diffW = surfaceY - yW;
                        float diffNW = surfaceY - yNW;
                        float avgDiff = (diffN + diffW + diffNW) / 3.0F;

                        if (avgDiff > 0.0F) {
                            slope = 1.0F + Math.min(0.18F, avgDiff * 0.05F);
                        } else if (avgDiff < 0.0F) {
                            slope = 1.0F + Math.max(-0.22F, avgDiff * 0.06F);
                        }
                    }

                    float shadowFactor = (slope < 1.0F) ? 0.88F : 1.0F;
                    float rShaded = ((rgb >> 16) & 0xFF) * shadowFactor * slope;
                    float gShaded = ((rgb >> 8) & 0xFF) * shadowFactor * slope;
                    float bShaded = (rgb & 0xFF) * slope;

                    int r = Mth.clamp((int) rShaded, 0, 255);
                    int g = Mth.clamp((int) gShaded, 0, 255);
                    int b = Mth.clamp((int) bShaded, 0, 255);

                    pixels[(lz << 4) | lx] = (0xFF << 24) | (b << 16) | (g << 8) | r;
                }
            }
        }

        MINIMAP_CHUNK_CACHE.put(key, pixels);
        MINIMAP_CHUNK_TIME.put(key, now);
        String dimStr = level.dimension().location().toString();
        WorldMapChunkCache.putChunkPixels(dimStr + (isCave ? "#cave" : ""), cx, cz, pixels);
        HunterWorldMapScreen.updateCachedChunk(dimStr + (isCave ? "#cave" : ""), cx, cz, pixels);
        if (MINIMAP_CHUNK_CACHE.size() > 512) {
            MINIMAP_CHUNK_CACHE.clear();
            MINIMAP_CHUNK_TIME.clear();
            MINIMAP_CHUNK_CACHE.put(key, pixels);
            MINIMAP_CHUNK_TIME.put(key, now);
        }
        return pixels;
    }

    private static int getSurfaceY(ClientLevel level, int wx, int wz, int minBuildY) {
        LevelChunk chunk = level.getChunkSource().getChunk(wx >> 4, wz >> 4, ChunkStatus.FULL, false);
        if (chunk != null && !chunk.isEmpty()) {
            int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, wx & 15, wz & 15);
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            int steps = 0;
            while (y > minBuildY && steps < 30) {
                p.set(wx, y, wz);
                BlockState st = level.getBlockState(p);
                if (!st.isAir()) {
                    if (!st.getFluidState().isEmpty() || st.getMapColor(level, p) != MapColor.NONE) {
                        return y;
                    }
                }
                y--;
                steps++;
            }
            return Math.max(minBuildY, y);
        }
        return level.getSeaLevel();
    }

    public static int boostColor(int rgb, float satMult, float valMult) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float h = 0.0f;
        if (delta > 0.0001f) {
            if (max == rf) {
                h = ((gf - bf) / delta) % 6.0f;
            } else if (max == gf) {
                h = ((bf - rf) / delta) + 2.0f;
            } else {
                h = ((rf - gf) / delta) + 4.0f;
            }
            h *= 60.0f;
            if (h < 0.0f) h += 360.0f;
        }

        float s = (max <= 0.0001f) ? 0.0f : (delta / max);
        float v = max;

        s = Math.min(1.0f, s * satMult);
        v = Math.min(1.0f, v * valMult);

        float c = v * s;
        float x = c * (1.0f - Math.abs((h / 60.0f) % 2.0f - 1.0f));
        float m = v - c;

        float r1 = 0, g1 = 0, b1 = 0;
        if (h < 60) { r1 = c; g1 = x; b1 = 0; }
        else if (h < 120) { r1 = x; g1 = c; b1 = 0; }
        else if (h < 180) { r1 = 0; g1 = c; b1 = x; }
        else if (h < 240) { r1 = 0; g1 = x; b1 = c; }
        else if (h < 300) { r1 = x; g1 = 0; b1 = c; }
        else { r1 = c; g1 = 0; b1 = x; }

        int rout = Mth.clamp((int) ((r1 + m) * 255.0f), 0, 255);
        int gout = Mth.clamp((int) ((g1 + m) * 255.0f), 0, 255);
        int bout = Mth.clamp((int) ((b1 + m) * 255.0f), 0, 255);

        return (rout << 16) | (gout << 8) | bout;
    }

    public static int getBlockBaseRgb(ClientLevel level, BlockState state, BlockPos pos) {
        // 1. Árboles y Hojas Especiales (Cerezo / Sakura, Azalea, Manglar, Nether, etc.)
        if (state.is(Blocks.CHERRY_LEAVES)) {
            return 0xFFB7D5; // Sakura Rosa vibrante auténtico
        }
        if (state.is(Blocks.PINK_PETALS)) {
            return 0xFFB5D0;
        }
        if (state.is(Blocks.AZALEA_LEAVES)) {
            return 0x6EA038;
        }
        if (state.is(Blocks.FLOWERING_AZALEA_LEAVES)) {
            return 0x9E7092;
        }
        if (state.is(Blocks.BIRCH_LEAVES)) {
            return 0x80A755;
        }
        if (state.is(Blocks.SPRUCE_LEAVES)) {
            return 0x619961;
        }
        if (state.is(Blocks.MANGROVE_LEAVES)) {
            return 0x83A43C;
        }
        if (state.is(Blocks.NETHER_WART_BLOCK)) {
            return 0x720000;
        }
        if (state.is(Blocks.WARPED_WART_BLOCK)) {
            return 0x147970;
        }
        if (state.is(Blocks.SHROOMLIGHT)) {
            return 0xF69B56;
        }

        // 2. Flores y Plantas llamativas
        if (state.is(Blocks.DANDELION)) return 0xFED766;
        if (state.is(Blocks.POPPY) || state.is(Blocks.RED_TULIP)) return 0xD62828;
        if (state.is(Blocks.BLUE_ORCHID) || state.is(Blocks.CORNFLOWER)) return 0x2E86DE;
        if (state.is(Blocks.ALLIUM)) return 0x9B59B6;
        if (state.is(Blocks.AZURE_BLUET) || state.is(Blocks.WHITE_TULIP) || state.is(Blocks.OXEYE_DAISY) || state.is(Blocks.LILY_OF_THE_VALLEY)) return 0xECF0F1;
        if (state.is(Blocks.ORANGE_TULIP)) return 0xE67E22;
        if (state.is(Blocks.PINK_TULIP) || state.is(Blocks.PEONY) || state.is(Blocks.LILAC)) return 0xF368E0;
        if (state.is(Blocks.SUNFLOWER)) return 0xF8C01D;
        if (state.is(Blocks.ROSE_BUSH)) return 0xBF1922;
        if (state.is(Blocks.WITHER_ROSE)) return 0x2C3E50;

        // 3. Troncos de madera / Stems
        if (state.is(Blocks.OAK_LOG) || state.is(Blocks.STRIPPED_OAK_LOG) || state.is(Blocks.OAK_WOOD)) return 0x725530;
        if (state.is(Blocks.SPRUCE_LOG) || state.is(Blocks.STRIPPED_SPRUCE_LOG) || state.is(Blocks.SPRUCE_WOOD)) return 0x412E18;
        if (state.is(Blocks.BIRCH_LOG) || state.is(Blocks.STRIPPED_BIRCH_LOG) || state.is(Blocks.BIRCH_WOOD)) return 0xD5D0C5;
        if (state.is(Blocks.JUNGLE_LOG) || state.is(Blocks.STRIPPED_JUNGLE_LOG) || state.is(Blocks.JUNGLE_WOOD)) return 0x544318;
        if (state.is(Blocks.ACACIA_LOG) || state.is(Blocks.STRIPPED_ACACIA_LOG) || state.is(Blocks.ACACIA_WOOD)) return 0x645C52;
        if (state.is(Blocks.DARK_OAK_LOG) || state.is(Blocks.STRIPPED_DARK_OAK_LOG) || state.is(Blocks.DARK_OAK_WOOD)) return 0x2A1D0F;
        if (state.is(Blocks.MANGROVE_LOG) || state.is(Blocks.STRIPPED_MANGROVE_LOG) || state.is(Blocks.MANGROVE_WOOD)) return 0x4D211A;
        if (state.is(Blocks.CHERRY_LOG) || state.is(Blocks.STRIPPED_CHERRY_LOG) || state.is(Blocks.CHERRY_WOOD)) return 0x331E24;
        if (state.is(Blocks.CRIMSON_STEM) || state.is(Blocks.STRIPPED_CRIMSON_STEM)) return 0x642436;
        if (state.is(Blocks.WARPED_STEM) || state.is(Blocks.STRIPPED_WARPED_STEM)) return 0x2A5D5A;

        // 4. Pasto y Césped - Tono vanilla natural, fresco y vibrante (Xaero's / JourneyMap style)
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)) {
            return getQuickGrassColor(level, pos);
        }

        // 5. Soporte universal de BlockColors para hojas y bloques de biomas custom
        try {
            int blockTint = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
            if (blockTint != -1 && blockTint != 0) {
                return blockTint;
            }
        } catch (Throwable ignored) {}

        if (state.getBlock() instanceof LeavesBlock || state.is(Blocks.VINE)) {
            MapColor mcFoliage = state.getMapColor(level, pos);
            return (mcFoliage != MapColor.NONE && mcFoliage != MapColor.PLANT && mcFoliage.col != 0)
                    ? mcFoliage.col
                    : getQuickFoliageColor(level, pos);
        }

        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) {
            return 0xEDF4F9;
        }
        if (state.is(Blocks.WATER)) {
            return getQuickWaterColor(level, pos);
        }
        if (state.is(Blocks.LAVA)) {
            return 0xD85A14;
        }

        // Bloques de superficie y cavernas naturales estilo JourneyMap
        if (state.is(Blocks.STONE)) return 0x787A7D;
        if (state.is(Blocks.COBBLESTONE)) return 0x6E7073;
        if (state.is(Blocks.MOSSY_COBBLESTONE)) return 0x586E52;
        if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.POLISHED_DEEPSLATE)) return 0x44464D;
        if (state.is(Blocks.TUFF) || state.is(Blocks.POLISHED_TUFF)) return 0x565853;
        if (state.is(Blocks.ANDESITE) || state.is(Blocks.POLISHED_ANDESITE)) return 0x7E8284;
        if (state.is(Blocks.DIORITE) || state.is(Blocks.POLISHED_DIORITE)) return 0xC4C7C9;
        if (state.is(Blocks.GRANITE) || state.is(Blocks.POLISHED_GRANITE)) return 0x8A5F52;
        if (state.is(Blocks.DRIPSTONE_BLOCK) || state.is(Blocks.POINTED_DRIPSTONE)) return 0x795E4E;
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.MUD)) return 0x77553A;
        if (state.is(Blocks.DIRT_PATH)) return 0x805E35;
        if (state.is(Blocks.SAND) || state.is(Blocks.SANDSTONE)) return 0xD2C48D; // Arena de playa cálida natural JourneyMap
        if (state.is(Blocks.RED_SAND) || state.is(Blocks.RED_SANDSTONE)) return 0xB05634;
        if (state.is(Blocks.GRAVEL)) return 0x787572;
        if (state.is(Blocks.CLAY)) return 0x8E949F;
        if (state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MOSS_CARPET)) return 0x527622;
        if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)) return 0x1A1426;
        // Minerales
        if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) return 0x2A2E32;
        if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)) return 0xD4AB8E;
        if (state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)) return 0xD87858;
        if (state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)) return 0xF5C83C;
        if (state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)) return 0xE52525;
        if (state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE)) return 0x2052C8;
        if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) return 0x52E0E5;
        if (state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE)) return 0x17D662;

        if (state.isAir() || (level.dimension().location().getPath().contains("subspace") && state.is(Blocks.BEDROCK))) {
            return 0x080D13;
        }

        MapColor mapColor = state.getMapColor(level, pos);
        if (mapColor != MapColor.NONE) {
            int mc = mapColor.col;
            int r = (int) (((mc >> 16) & 0xFF) * 0.85F);
            int g = (int) (((mc >> 8) & 0xFF) * 0.85F);
            int b = (int) ((mc & 0xFF) * 0.85F);
            return (r << 16) | (g << 8) | b;
        }
        return 0x5C636E;
    }

    // ==========================================
    // DETECCIÓN TÁCTICA DE AMENAZAS Y RADAR
    // ==========================================

    public static void updateThreatDetection(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            activeThreatLevel = 0;
            return;
        }

        long now = System.currentTimeMillis();
        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        int perception = 10;
        try {
            var data = mc.player.getData(com.soybe.awakeningsystem.AwakeningSystemMod.PLAYER_SYSTEM);
            if (data != null) {
                perception = Math.max(1, data.perception());
            }
        } catch (Throwable ignored) {}

        double radarBlockRange = RADAR_RADIUS_BLOCKS + (GuiConfig.minimapPerceptionRadar ? Math.min(48.0D, Math.max(0, perception - 10) * 0.45D) : 0.0D);
        double verticalRange = 32.0D + (GuiConfig.minimapPerceptionRadar ? Math.min(32.0D, Math.max(0, perception - 10) * 0.35D) : 0.0D);

        AABB box = new AABB(px - radarBlockRange, py - verticalRange, pz - radarBlockRange, px + radarBlockRange, py + verticalRange, pz + radarBlockRange);
        boolean canDetectStealth = GuiConfig.minimapPerceptionRadar && perception >= 25;

        if (now - lastRadarScanTimeMs > 200L || cachedRadarEntities.isEmpty()) {
            lastRadarScanTimeMs = now;
            cachedRadarEntities.clear();
            List<Entity> entities = mc.level.getEntities((Entity) null, box, ent -> {
                if (ent == mc.player || !ent.isAlive() || !(ent instanceof LivingEntity living)) return false;
                if (living.isInvisible() && !canDetectStealth) return false;
                return true;
            });
            for (Entity ent : entities) {
                if (ent instanceof LivingEntity l) cachedRadarEntities.add(l);
            }
            if (!cachedRadarEntities.isEmpty()) {
                cachedRadarEntities.sort((a, b) -> Integer.compare(getMinimapEntityPriority(b), getMinimapEntityPriority(a)));
            }
        }

        if (cachedRadarEntities.isEmpty()) {
            activeThreatLevel = 0;
            return;
        }

        int detectedThreatLevel = 0;
        String detectedThreatName = "";
        double minThreatDist = 999.0D;
        boolean isMinimapCave = lastWasCaveMode || (py < mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, mc.player.getBlockX(), mc.player.getBlockZ()) - 4 && !mc.level.canSeeSky(mc.player.blockPosition()));

        for (LivingEntity living : cachedRadarEntities) {
            if (!living.isAlive() || living.isRemoved()) continue;
            double ly = living.getY();

            if (isMinimapCave) {
                if (Math.abs(ly - py) > 8.5D) continue;
            } else {
                if (Math.abs(ly - py) > 14.0D) continue;
                int surfaceY = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, living.getBlockX(), living.getBlockZ());
                if (ly < surfaceY - 5.0D) continue;
            }

            double dx = living.getX() - px;
            double dz = living.getZ() - pz;
            double distBlocks = Math.sqrt(dx * dx + dz * dz);

            boolean isBoss = com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(living)
                    || living instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                    || living instanceof net.minecraft.world.entity.monster.warden.Warden
                    || living instanceof com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity;

            float maxHp = living.getMaxHealth();
            boolean isApex = isBoss || (maxHp >= 150.0F);
            boolean isTargetingMe = (living instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == mc.player);
            boolean isHostileElite = (living instanceof net.minecraft.world.entity.monster.Enemy) && (maxHp >= 60.0F || isTargetingMe);

            if (isApex) {
                int level = (maxHp >= 300.0F || living instanceof net.minecraft.world.entity.monster.warden.Warden || living instanceof net.minecraft.world.entity.boss.wither.WitherBoss) ? 3 : 2;
                if (level > detectedThreatLevel) {
                    detectedThreatLevel = level;
                    detectedThreatName = living.getDisplayName().getString();
                    minThreatDist = distBlocks;
                }
            } else if (isHostileElite && detectedThreatLevel < 2) {
                detectedThreatLevel = 1;
                detectedThreatName = living.getDisplayName().getString();
                minThreatDist = distBlocks;
            }
        }

        activeThreatLevel = detectedThreatLevel;
        activeThreatName = detectedThreatName;
        activeThreatDist = minThreatDist;

        // Sonido de alerta táctica del Sistema al detectar peligro inminente (cooldown 14 segundos)
        if (GuiConfig.minimapDangerAlert && activeThreatLevel >= 2) {
            if (now - lastThreatAlertSoundMs > 14000L) {
                lastThreatAlertSoundMs = now;
                try {
                    mc.player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.75F, 0.55F);
                } catch (Throwable ignored) {}
            }
        }
    }

    private static void renderEntityRadar(GuiGraphics g, Minecraft mc, int centerX, int centerY, int maxRadius, boolean isCircular) {
        if (mc.level == null || mc.player == null) return;

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        // 1. Integración de Percepción del Jugador (Sentido de Cazador)
        int perception = 10;
        try {
            var data = mc.player.getData(com.soybe.awakeningsystem.AwakeningSystemMod.PLAYER_SYSTEM);
            if (data != null) {
                perception = Math.max(1, data.perception());
            }
        } catch (Throwable ignored) {}

        float zoom = Math.max(0.5F, Math.min(3.0F, GuiConfig.minimapZoom));
        float perceptionBonus = GuiConfig.minimapPerceptionRadar ? Math.min(48.0F, Math.max(0, perception - 10) * 0.45F) : 0.0F;
        float totalRadarBlocks = RADAR_RADIUS_BLOCKS + perceptionBonus;
        float radarBlockRange = totalRadarBlocks / zoom;

        double verticalRange = 32.0D + (GuiConfig.minimapPerceptionRadar ? Math.min(32.0D, Math.max(0, perception - 10) * 0.35D) : 0.0D);

        AABB box = new AABB(px - radarBlockRange, py - verticalRange, pz - radarBlockRange, px + radarBlockRange, py + verticalRange, pz + radarBlockRange);
        boolean canDetectStealth = GuiConfig.minimapPerceptionRadar && perception >= 25;

        long now = System.currentTimeMillis();
        if (now - lastRadarScanTimeMs > 200L || cachedRadarEntities.isEmpty()) {
            lastRadarScanTimeMs = now;
            cachedRadarEntities.clear();
            List<Entity> entities = mc.level.getEntities((Entity) null, box, ent -> {
                if (ent == mc.player || !ent.isAlive() || !(ent instanceof LivingEntity living)) return false;
                if (living.isInvisible() && !canDetectStealth) return false;
                return true;
            });
            for (Entity ent : entities) {
                if (ent instanceof LivingEntity l) cachedRadarEntities.add(l);
            }
            if (!cachedRadarEntities.isEmpty()) {
                cachedRadarEntities.sort((a, b) -> Integer.compare(getMinimapEntityPriority(b), getMinimapEntityPriority(a)));
            }
        }

        if (cachedRadarEntities.isEmpty()) {
            activeThreatLevel = 0;
            return;
        }

        OCCUPIED_CELLS.clear();
        int renderedCount = 0;
        int maxRadiusSq = maxRadius * maxRadius;

        int detectedThreatLevel = 0;
        String detectedThreatName = "";
        double minThreatDist = 999.0D;
        boolean isMinimapCave = lastWasCaveMode || (py < mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, mc.player.getBlockX(), mc.player.getBlockZ()) - 4 && !mc.level.canSeeSky(mc.player.blockPosition()));
        int renderedAnimalsCount = 0;

        for (LivingEntity living : cachedRadarEntities) {
            if (!living.isAlive() || living.isRemoved()) continue;
            double ly = living.getY();

            // Culling vertical táctico: si estamos en superficie, descartar cuevas subterráneas
            if (isMinimapCave) {
                if (Math.abs(ly - py) > 8.5D) continue;
            } else {
                if (Math.abs(ly - py) > 14.0D) continue; // Descarte rápido antes de consultar heightmap
                int surfaceY = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, living.getBlockX(), living.getBlockZ());
                if (ly < surfaceY - 5.0D) continue; // Criatura en cueva subterránea
            }

            int prio = getMinimapEntityPriority(living);
            if (prio <= 1) {
                if (renderedAnimalsCount >= 10) continue; // Máximo 10 animales pasivos en minimapa
                renderedAnimalsCount++;
            }

            double dx = living.getX() - px;
            double dz = living.getZ() - pz;
            double distBlocks = Math.sqrt(dx * dx + dz * dz);

            // Criaturas del Limbo: acechantes invisibles para el radar y sin alertas de amenaza
            if (living instanceof com.soybe.awakeningsystem.entity.LimboStalkerCreature) {
                continue;
            }

            // Detección de Peligro / Amenaza Inminente (Bosses, Monarcas, Rango S, Calamidades)
            boolean isBoss = com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(living)
                    || living instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                    || living instanceof net.minecraft.world.entity.monster.warden.Warden
                    || living instanceof com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity;

            float maxHp = living.getMaxHealth();
            boolean isApex = isBoss || (maxHp >= 150.0F);
            boolean isTargetingMe = (living instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == mc.player);
            boolean isHostileElite = (living instanceof net.minecraft.world.entity.monster.Enemy) && (maxHp >= 60.0F || isTargetingMe);

            if (isApex) {
                int level = (maxHp >= 300.0F || living instanceof net.minecraft.world.entity.monster.warden.Warden || living instanceof net.minecraft.world.entity.boss.wither.WitherBoss) ? 3 : 2;
                if (level > detectedThreatLevel) {
                    detectedThreatLevel = level;
                    detectedThreatName = living.getDisplayName().getString();
                    minThreatDist = distBlocks;
                }
            } else if (isHostileElite && detectedThreatLevel < 2) {
                detectedThreatLevel = 1;
                detectedThreatName = living.getDisplayName().getString();
                minThreatDist = distBlocks;
            }

            int ex = centerX + (int) Math.round(dx * zoom);
            int ey = centerY + (int) Math.round(dz * zoom);

            int dxC = ex - centerX;
            int dyC = ey - centerY;
            if (isCircular && (dxC * dxC + dyC * dyC) > maxRadiusSq) continue;
            if (!isCircular && (Math.abs(dxC) > maxRadius || Math.abs(dyC) > maxRadius)) continue;

            if (renderedCount >= 24) continue;

            long cellKey = (((long) (ex / 8)) << 32) | ((ey / 8) & 0xFFFFFFFFL);
            if (prio < 3 && OCCUPIED_CELLS.contains(cellKey)) {
                continue; // Evitar que enjambres de murciélagos o pollos tapen el minimapa
            }
            OCCUPIED_CELLS.add(cellKey);

            // Si es una entidad invisible detectada por Percepción, dibujar aura sensorial etérea cian
            if (living.isInvisible() && canDetectStealth) {
                g.fill(ex - 5, ey - 5, ex + 6, ey + 6, 0x4400E5FF);
                g.renderOutline(ex - 5, ey - 5, 11, 11, 0xAA00E5FF);
            }

            EntityIconRenderer.renderEntityIcon(g, living, ex, ey, 8, py);
            renderedCount++;
        }

        activeThreatLevel = detectedThreatLevel;
        activeThreatName = detectedThreatName;
        activeThreatDist = minThreatDist;

        // Sonido de alerta táctica del Sistema al detectar peligro inminente (cooldown 14 segundos)
        if (GuiConfig.minimapDangerAlert && activeThreatLevel >= 2) {
            if (now - lastThreatAlertSoundMs > 14000L) {
                lastThreatAlertSoundMs = now;
                try {
                    mc.player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.75F, 0.55F);
                } catch (Throwable ignored) {}
            }
        }
    }

    private static int getMinimapEntityPriority(LivingEntity e) {
        if (com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(e)) return 5;
        if (e instanceof Player) return 4;
        if (e instanceof net.minecraft.world.entity.monster.Enemy) return 3;
        if (e instanceof net.minecraft.world.entity.NeutralMob) return 2;
        return 1;
    }

    // ==========================================
    // RADAR DE ÍTEMS CAÍDOS Y ARTEFACTOS
    // ==========================================

    private static void renderItemRadar(GuiGraphics g, Minecraft mc, int centerX, int centerY, int maxRadius, boolean isCircular) {
        if (mc.level == null || mc.player == null) return;

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        float zoom = Math.max(0.5F, Math.min(3.0F, GuiConfig.minimapZoom));
        float radarBlockRange = RADAR_RADIUS_BLOCKS / zoom;

        long now = System.currentTimeMillis();
        if (now - lastItemRadarScanTimeMs > 250L || cachedRadarItems.isEmpty()) {
            lastItemRadarScanTimeMs = now;
            cachedRadarItems.clear();
            AABB box = new AABB(px - radarBlockRange, py - 32.0D, pz - radarBlockRange, px + radarBlockRange, py + 32.0D, pz + radarBlockRange);
            List<ItemEntity> items = mc.level.getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive);
            cachedRadarItems.addAll(items);
        }
        if (cachedRadarItems.isEmpty()) return;

        float gameTime = mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(true);
        float pulse = (float) (Math.sin(gameTime * 0.35F) * 0.5F + 0.5F);

        int count = 0;
        for (ItemEntity item : cachedRadarItems) {
            if (!item.isAlive() || item.isRemoved()) continue;
            if (count >= 40) break;
            ItemStack stack = item.getItem();
            if (stack.isEmpty()) continue;

            double dx = item.getX() - px;
            double dz = item.getZ() - pz;
            int ix = centerX + (int) Math.round(dx * zoom);
            int iy = centerY + (int) Math.round(dz * zoom);

            int dxC = ix - centerX;
            int dyC = iy - centerY;
            int maxItemRadius = maxRadius - 2;
            if (isCircular && (dxC * dxC + dyC * dyC) > maxItemRadius * maxItemRadius) continue;
            if (!isCircular && (Math.abs(dxC) > maxItemRadius || Math.abs(dyC) > maxItemRadius)) continue;

            int itemColor = getItemTacticalColor(stack);
            boolean isArtifactOrKey = isHighTierDrop(stack);

            // Aura pulsante para artefactos legendarios, llaves de mazmorra y armas divinas
            if (isArtifactOrKey) {
                int glowAlpha = (int) (140 + 115 * pulse);
                int glowCol = (glowAlpha << 24) | (itemColor & 0x00FFFFFF);
                g.fill(ix - 3, iy, ix + 4, iy + 1, glowCol);
                g.fill(ix, iy - 3, ix + 1, iy + 4, glowCol);
            }

            // Borde oscuro táctico para contraste perfecto
            g.fill(ix - 1, iy - 2, ix + 2, iy + 3, 0xEE08111A);
            g.fill(ix - 2, iy - 1, ix + 3, iy + 2, 0xEE08111A);

            // Rombo táctico con color de rareza
            g.fill(ix - 1, iy, ix + 2, iy + 1, itemColor);
            g.fill(ix, iy - 1, ix + 1, iy + 2, itemColor);
            g.fill(ix, iy, ix + 1, iy + 1, 0xFFFFFFFF);

            // Micro-indicador de elevación vertical si hay desnivel pronunciado
            double dy = item.getY() - py;
            if (dy >= 4.0D) {
                g.fill(ix, iy - 3, ix + 1, iy - 2, 0xFF50FA7B);
            } else if (dy <= -4.0D) {
                g.fill(ix, iy + 2, ix + 1, iy + 3, 0xFFFF4444);
            }

            count++;
        }
    }

    public static int getItemTacticalColor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0xFFFFFFFF;
        var item = stack.getItem();

        // 1. Artefactos, Armas de Rango S, Llaves de Mazmorra, Rareza Épica
        if (item instanceof com.soybe.awakeningsystem.item.ManhwaArtifactItem
                || item instanceof com.soybe.awakeningsystem.item.AwakeningWeaponItem
                || item instanceof com.soybe.awakeningsystem.item.GenshinWeaponItem
                || item instanceof com.soybe.awakeningsystem.item.TeyvatShrineKeyItem
                || item instanceof com.soybe.awakeningsystem.item.ClandestineVaultKeyItem
                || stack.getRarity() == net.minecraft.world.item.Rarity.EPIC) {
            return 0xFFFFD700; // Dorado Radiante
        }

        // 2. Núcleos de Sombra, Monarcas, Reliquias del Vacío
        String name = item.getDescriptionId().toLowerCase(java.util.Locale.ROOT);
        if (item instanceof com.soybe.awakeningsystem.item.ShadowDaggersItem
                || item instanceof com.soybe.awakeningsystem.item.DeathKingClockItem
                || name.contains("shadow") || name.contains("monarch") || name.contains("void")) {
            return 0xFFA855F7; // Violeta Monarca
        }

        // 3. Cristales de Maná, Píldoras de Cultivo, Elixires, Gemas Esenciales, Destinos/Tiradas
        if (item instanceof com.soybe.awakeningsystem.item.PrimogemItem
                || item instanceof com.soybe.awakeningsystem.item.StellarJadeItem
                || item instanceof com.soybe.awakeningsystem.item.IntertwinedFateItem
                || item instanceof com.soybe.awakeningsystem.item.AcquaintFateItem
                || name.contains("mana") || name.contains("pill") || name.contains("elixir") || name.contains("core")) {
            return 0xFF00E5FF; // Cian Mágico
        }

        // 4. Objetos Raros, Diamantes, Netherite, Armas u objetos encantados
        if (stack.isEnchanted() || stack.getRarity() == net.minecraft.world.item.Rarity.RARE
                || item == net.minecraft.world.item.Items.DIAMOND || item == net.minecraft.world.item.Items.NETHERITE_INGOT
                || item == net.minecraft.world.item.Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
            return 0xFF38BDF8; // Aqua Celeste
        }

        // 5. Ítems comunes (Tierra, Madera, Flechas, etc.)
        return 0xFFE2E8F0; // Blanco Plateado Táctico
    }

    public static boolean isHighTierDrop(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var item = stack.getItem();
        return item instanceof com.soybe.awakeningsystem.item.ManhwaArtifactItem
                || item instanceof com.soybe.awakeningsystem.item.AwakeningWeaponItem
                || item instanceof com.soybe.awakeningsystem.item.GenshinWeaponItem
                || item instanceof com.soybe.awakeningsystem.item.TeyvatShrineKeyItem
                || item instanceof com.soybe.awakeningsystem.item.ClandestineVaultKeyItem
                || stack.getRarity() == net.minecraft.world.item.Rarity.EPIC;
    }

    // ==========================================
    // WAYPOINTS EN EL MINIMAPA
    // ==========================================

    private static void renderWaypointsOnMinimap(GuiGraphics g, Player player, int centerX, int centerY, int maxRadius, boolean isCircular) {
        String currentDim = player.level().dimension().location().toString();
        List<Waypoint> waypoints = WaypointManager.getWaypointsForDimension(currentDim);
        if (waypoints.isEmpty()) return;

        double px = player.getX();
        double pz = player.getZ();
        float zoom = Math.max(0.5F, Math.min(3.0F, GuiConfig.minimapZoom));

        for (Waypoint wp : waypoints) {
            if (!wp.isEnabled()) continue;

            double dx = (wp.getX() + 0.5) - px;
            double dz = (wp.getZ() + 0.5) - pz;
            int wx = centerX + (int) Math.round(dx * zoom);
            int wy = centerY + (int) Math.round(dz * zoom);

            int dxC = wx - centerX;
            int dyC = wy - centerY;
            int maxWpRadius = maxRadius - 3;

            // Si está fuera del rango del minimapa, no mostrarlo (ya se ve directamente en persona en el mundo 3D)
            if (isCircular && (dxC * dxC + dyC * dyC) > maxWpRadius * maxWpRadius) {
                continue;
            }
            if (!isCircular && (Math.abs(dxC) > maxWpRadius || Math.abs(dyC) > maxWpRadius)) {
                continue;
            }

            int color = wp.getColor() | 0xFF000000;

            // Dibujar ÚNICAMENTE el puntito de Waypoint dentro del radar (sin nombres ni distancias que tapen el minimapa)
            if (wp.isDeathPoint()) {
                g.fill(wx - 4, wy - 4, wx + 5, wy + 5, 0xEE330008);
                g.fill(wx - 3, wy - 3, wx + 4, wy + 4, 0xFFFF2244);
                g.drawCenteredString(Minecraft.getInstance().font, "☠", wx, wy - 4, 0xFFFFFFFF);
            } else {
                g.fill(wx - 3, wy - 3, wx + 4, wy + 4, 0xEE08111A);
                g.fill(wx - 2, wy - 2, wx + 3, wy + 3, color);
                g.fill(wx - 1, wy - 1, wx + 2, wy + 2, 0xFFFFFFFF);
            }
        }
    }

    private static void renderRiftsOnMinimap(GuiGraphics g, Minecraft mc, int centerX, int centerY, int maxRadius, boolean isCircular) {
        if (mc.player == null || mc.level == null) return;
        if (AwakeningSystemClient.ACTIVE_RIFTS.isEmpty()) return;

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();
        float zoom = Math.max(0.5F, Math.min(3.0F, GuiConfig.minimapZoom));

        float gameTime = mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(true);
        float pulse = (float) (Math.sin(gameTime * 0.25F) * 0.5F + 0.5F);

        for (com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry rift : AwakeningSystemClient.ACTIVE_RIFTS) {
            if (rift.pos() == null) continue;

            double dx = (rift.pos().getX() + 0.5D) - px;
            double dz = (rift.pos().getZ() + 0.5D) - pz;
            double distSq = dx * dx + dz * dz;

            int rx = centerX + (int) Math.round(dx * zoom);
            int ry = centerY + (int) Math.round(dz * zoom);
            double distFromCenter = Math.sqrt(Math.pow(rx - centerX, 2) + Math.pow(ry - centerY, 2));

            int themeColor = AwakeningSystemClient.getGateColor(rift);

            if (distFromCenter <= maxRadius - 3) {
                // Dentro del radar del minimapa: Renderizar vortex dimensional completo
                int alpha = (int) (160 + 95 * pulse);
                int glowCol = (alpha << 24) | (themeColor & 0x00FFFFFF);

                // Halo pulsante
                g.fill(rx - 4, ry - 1, rx + 5, ry + 2, glowCol);
                g.fill(rx - 1, ry - 4, rx + 2, ry + 5, glowCol);

                // Núcleo de obsidiana
                g.fill(rx - 3, ry - 3, rx + 4, ry + 4, 0xEE090D14);
                g.renderOutline(rx - 3, ry - 3, 7, 7, themeColor);

                // Núcleo del vórtice
                g.fill(rx - 1, ry - 1, rx + 2, ry + 2, themeColor);

                // Indicador de elevación
                double dy = rift.pos().getY() - py;
                if (dy >= 5.0D) {
                    g.fill(rx, ry - 5, rx + 1, ry - 4, 0xFF50FA7B);
                } else if (dy <= -5.0D) {
                    g.fill(rx, ry + 4, rx + 1, ry + 5, 0xFFFF4444);
                }
            } else if (distSq <= 180.0D * 180.0D) {
                // Fuera del radar pero a menos de 180m: Clampear al borde exterior como objetivo táctico
                double angle = Math.atan2(dz, dx);
                int clampR = maxRadius - 4;
                int edgeX = centerX + (int) Math.round(Math.cos(angle) * clampR);
                int edgeY = centerY + (int) Math.round(Math.sin(angle) * clampR);

                int alpha = (int) (130 + 125 * pulse);
                int pipColor = (alpha << 24) | (themeColor & 0x00FFFFFF);

                g.fill(edgeX - 2, edgeY - 2, edgeX + 3, edgeY + 3, 0xEE090D14);
                g.fill(edgeX - 1, edgeY - 1, edgeX + 2, edgeY + 2, pipColor);
            }
        }
    }

    private static void renderPlayerMarker(GuiGraphics g, Player player, int cx, int cy) {
        EntityIconRenderer.drawPlayerMarker(g, player, cx, cy, player.getYRot(), 10);
    }

    private static void renderCompassPoints(GuiGraphics g, int cx, int cy, int radius, boolean isCircular, int screenW, int screenH) {
        Minecraft mc = Minecraft.getInstance();

        // 4 remaches cardinales montados directamente sobre el marco metálico (estilo Xaero's)
        drawCompassRivet(g, mc, "§c§lN", cx, cy - radius);       // Norte (Rojo distintivo)
        drawCompassRivet(g, mc, "§f§lS", cx, cy + radius);       // Sur (Blanco brillante)
        drawCompassRivet(g, mc, "§f§lE", cx + radius, cy);       // Este (Blanco brillante)
        drawCompassRivet(g, mc, "§f§lW", cx - radius, cy);       // Oeste (Blanco brillante)
    }

    private static void drawCompassRivet(GuiGraphics g, Minecraft mc, String text, int px, int py) {
        int r = 5;
        // Remache metálico oscuro circular/cuadrado integrado en el bisel
        g.fill(px - r, py - r, px + r + 1, py + r + 1, 0xEE0B121A);
        g.renderOutline(px - r, py - r, r * 2 + 1, r * 2 + 1, 0xFF3D4B5C);
        g.fill(px - r + 1, py - r + 1, px + r, py + r, 0xEE141C26);

        // Letra cardinal centrada nítida
        g.drawCenteredString(mc.font, text, px, py - 4, 0xFFFFFFFF);
    }

    private static void renderInfoRibbon(GuiGraphics g, Minecraft mc, int mapX, int mapY, int mapSize, boolean isCave, int screenW) {
        Player player = mc.player;
        if (player == null) return;

        // Bioma / Indicador de Cueva / Nombre de Territorio
        String modeTag = isCave ? Component.translatable("awakening_system.minimap.cave_tag").getString() + " " : "";
        String biomeName;
        if (mc.level != null) {
            var bKey = mc.level.getBiome(player.blockPosition()).unwrapKey().orElse(null);
            if (bKey != null && bKey.equals(lastBiomeKey) && !cachedBiomeName.isEmpty()) {
                biomeName = cachedBiomeName;
            } else {
                lastBiomeKey = bKey;
                cachedBiomeName = bKey != null
                        ? Component.translatable(net.minecraft.Util.makeDescriptionId("biome", bKey.location())).getString()
                        : Component.translatable("awakening_system.minimap.wilderness").getString();
                biomeName = cachedBiomeName;
            }
        } else {
            biomeName = Component.translatable("awakening_system.minimap.wilderness").getString();
        }

        int perception = 10;
        try {
            var data = player.getData(com.soybe.awakeningsystem.AwakeningSystemMod.PLAYER_SYSTEM);
            if (data != null) perception = data.perception();
        } catch (Throwable ignored) {}
        String perTag = (GuiConfig.minimapPerceptionRadar && perception > 10) ? " §8| §b👁" + perception : "";
        String subText = modeTag + "§e" + biomeName + perTag;

        // Coordenadas compactas estilo Xaero's (X, Y, Z)
        String coords = "§f" + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ();

        int textW = Math.max(mc.font.width(coords), mc.font.width(subText));
        int ribbonW = Math.max(mapSize, textW + 16);
        int ribbonX = mapX + (mapSize - ribbonW) / 2;
        // Ajuste estricto dentro de la pantalla para evitar desbordamiento hacia la derecha
        if (screenW > 0) {
            ribbonX = Mth.clamp(ribbonX, 6, screenW - ribbonW - 6);
        }
        boolean isCompact = mapSize < 80;
        int ribbonY = mapY + mapSize + (isCompact ? 7 : 9);
        int ribbonH = isCompact ? 19 : 22;

        g.fill(ribbonX, ribbonY, ribbonX + ribbonW, ribbonY + ribbonH, 0xEE08111A);
        g.renderOutline(ribbonX, ribbonY, ribbonW, ribbonH, 0xAA4EE2EC);

        // Línea 1: Coordenadas
        g.drawCenteredString(mc.font, coords, ribbonX + ribbonW / 2, ribbonY + 2, 0xFFFFFFFF);
        // Línea 2: Nombre del Bioma / Lugar
        g.drawCenteredString(mc.font, subText, ribbonX + ribbonW / 2, ribbonY + (isCompact ? 10 : 12), 0xFFFFE066);
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    // ==========================================
    // MARCOS Y GEOMETRÍA ESTÉTICA METÁLICA (XAERO'S STYLE)
    // ==========================================

    private static int[] getCircleDx(int radius) {
        if (radius < 0 || radius >= 256) return null;
        int[] cached = CIRCLE_DX_CACHE[radius];
        if (cached != null) return cached;
        int size = radius * 2 + 1;
        cached = new int[size];
        int rSq = radius * radius;
        for (int dy = -radius; dy <= radius; dy++) {
            cached[dy + radius] = (int) Math.round(Math.sqrt(Math.max(0, rSq - dy * dy)));
        }
        CIRCLE_DX_CACHE[radius] = cached;
        return cached;
    }

    private static void drawCircularFrame(GuiGraphics g, int cx, int cy, int radius) {
        int[] outer = getCircleDx(radius);
        int[] mid = getCircleDx(radius - 1);
        int[] inner = getCircleDx(radius - 3);
        if (outer == null || mid == null || inner == null) return;

        for (int dy = -radius; dy <= radius; dy++) {
            int outerDx = outer[dy + radius];
            int midDx = (dy >= -(radius - 1) && dy <= (radius - 1)) ? mid[dy + radius - 1] : 0;
            int innerDx = (dy >= -(radius - 3) && dy <= (radius - 3)) ? inner[dy + radius - 3] : 0;

            if (outerDx > midDx) {
                g.fill(cx - outerDx, cy + dy, cx - midDx, cy + dy + 1, 0xFF1C222B);
                g.fill(cx + midDx, cy + dy, cx + outerDx, cy + dy + 1, 0xFF1C222B);
            }
            if (midDx > innerDx) {
                // Brillo metálico acero
                int steelCol = (dy < 0) ? 0xFF8895A5 : 0xFF4E5866;
                g.fill(cx - midDx, cy + dy, cx - innerDx, cy + dy + 1, steelCol);
                g.fill(cx + innerDx, cy + dy, cx + midDx, cy + dy + 1, steelCol);
            }
        }
    }

    private static void drawSquareFrame(GuiGraphics g, int x, int y, int size) {
        // Marco de bisel metálico de 2 capas
        g.renderOutline(x - 1, y - 1, size + 2, size + 2, 0xFF1C222B);
        g.renderOutline(x, y, size, size, 0xFF8895A5);

        // Esquinas reforzadas estilo interfaz táctica
        int gold = 0xFFFFD700;
        int cornerLen = 8;
        // Sup-Izq
        g.fill(x - 2, y - 2, x + cornerLen, y + 1, gold);
        g.fill(x - 2, y - 2, x + 1, y + cornerLen, gold);
        // Sup-Der
        g.fill(x + size - cornerLen, y - 2, x + size + 2, y + 1, gold);
        g.fill(x + size - 1, y - 2, x + size + 2, y + cornerLen, gold);
        // Inf-Izq
        g.fill(x - 2, y + size - 1, x + cornerLen, y + size + 2, gold);
        g.fill(x - 2, y + size - cornerLen, x + 1, y + size + 2, gold);
        // Inf-Der
        g.fill(x + size - cornerLen, y + size - 1, x + size + 2, y + size + 2, gold);
        g.fill(x + size - 1, y + size - cornerLen, x + size + 2, y + size + 2, gold);
    }

    private static void renderDangerAlertFrame(GuiGraphics g, int cx, int cy, int radius, int mapX, int mapY, int mapSize, boolean isCircular, Minecraft mc) {
        float gameTime = mc.level != null ? mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(true) : 0.0F;
        float pulse = (float) (Math.sin(gameTime * 0.28F) * 0.5F + 0.5F); // Pulso rítmico ~1.5 Hz

        int alertColor;
        if (activeThreatLevel >= 3) {
            // Apex Boss / Calamidad (Warden, Wither, Jefes Mayores): Pulso Dorado Celestial a Carmesí
            int r = (int) (255 * (0.8F + 0.2F * pulse));
            int gr = (int) (180 * (1.0F - pulse * 0.7F));
            int b = (int) (50 * (1.0F - pulse));
            int alpha = (int) (160 + 95 * pulse);
            alertColor = (alpha << 24) | (r << 16) | (gr << 8) | b;
        } else if (activeThreatLevel == 2) {
            // Jefe / Rango S: Pulso Carmesí Intenso Sed de Sangre
            int alpha = (int) (140 + 115 * pulse);
            alertColor = (alpha << 24) | 0xFF0033;
        } else {
            // Amenaza Hostil de Élite / Acechando: Ámbar / Naranja
            int alpha = (int) (120 + 80 * pulse);
            alertColor = (alpha << 24) | 0xFF8800;
        }

        if (isCircular) {
            // Anillo exterior brillante pulsante alrededor del marco circular
            int extraR = (int) Math.round(pulse * 3.0F);
            drawHollowCircle(g, cx, cy, radius + 1, alertColor);
            drawHollowCircle(g, cx, cy, radius + 2 + extraR, (alertColor & 0x00FFFFFF) | ((int) ((alertColor >>> 24) * 0.45F) << 24));
        } else {
            // Aura brillante pulsante en el marco cuadrado
            int extraW = (int) Math.round(pulse * 3.0F);
            g.renderOutline(mapX - 2, mapY - 2, mapSize + 4, mapSize + 4, alertColor);
            g.renderOutline(mapX - 2 - extraW, mapY - 2 - extraW, mapSize + 4 + extraW * 2, mapSize + 4 + extraW * 2, (alertColor & 0x00FFFFFF) | ((int) ((alertColor >>> 24) * 0.45F) << 24));
        }
    }

    public static boolean isDangerAlertActive() {
        return GuiConfig.minimapDangerAlert && activeThreatLevel > 0;
    }

    public static int getDangerAlertBottom(Minecraft mc) {
        if (!isDangerAlertActive()) return 0;
        int bossBottom = com.soybe.awakeningsystem.SystemHudOverlay.getBossBarOffset(mc);
        int bannerY = bossBottom > 0 ? bossBottom + 5 : 8;
        return bannerY + 13;
    }

    private static void renderDangerAlertBanner(GuiGraphics g, Minecraft mc, int mapX, int mapY, int mapSize, int screenW) {
        renderDangerAlertBannerCentered(g, mc, screenW);
    }

    public static void renderDangerAlertBannerCentered(GuiGraphics g, Minecraft mc, int screenW) {
        if (activeThreatLevel <= 0 || !GuiConfig.minimapDangerAlert || !GuiConfig.showBossAlert) return;

        float gameTime = mc.level != null ? mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(true) : 0.0F;
        float pulse = (float) (Math.sin(gameTime * 0.32F) * 0.5F + 0.5F);

        String distStr = activeThreatDist < 990 ? " (" + (int) Math.round(activeThreatDist) + "m)" : "";
        String text;
        int bannerCol;
        int bgCol;

        String threatName = activeThreatName;
        // Adaptar prefijo de nivel según idioma ([Nv. X] -> [Ур. X] en ruso, [Lv. X] en inglés/otros)
        String lang = mc.getLanguageManager().getSelected();
        if (lang != null) {
            String l = lang.toLowerCase(java.util.Locale.ROOT);
            if (l.startsWith("ru_")) {
                threatName = threatName.replace("[Nv.", "[Ур.");
            } else if (!l.startsWith("es_")) {
                threatName = threatName.replace("[Nv.", "[Lv.");
            }
        }

        if (activeThreatLevel >= 3) {
            String tag = Component.translatable("awakening_system.minimap.danger_apex").getString();
            text = "§6§l▲ " + tag + ": §e" + threatName + distStr;
            bannerCol = ((int) (180 + 75 * pulse) << 24) | 0xFFD700;
            bgCol = 0xEE2A000A;
        } else if (activeThreatLevel == 2) {
            String tag = Component.translatable("awakening_system.minimap.danger_boss").getString();
            text = "§c§l▲ " + tag + ": §f" + threatName + distStr;
            bannerCol = ((int) (180 + 75 * pulse) << 24) | 0xFF2244;
            bgCol = 0xEE330008;
        } else {
            String tag = Component.translatable("awakening_system.minimap.danger_threat").getString();
            text = "§6§l▲ " + tag + ": §f" + threatName + distStr;
            bannerCol = ((int) (160 + 80 * pulse) << 24) | 0xFFA500;
            bgCol = 0xEE1E1205;
        }

        float bannerScale = 0.82F;
        int textW = mc.font.width(text);
        int badgeW = textW + 10;
        int badgeH = 13;

        int scaledW = (int) Math.round(badgeW * bannerScale);
        int bannerX;
        int bannerY;
        int screenH = mc.getWindow().getGuiScaledHeight();

        if (GuiConfig.bossAlertX >= 0 && GuiConfig.bossAlertY >= 0) {
            bannerX = GuiConfig.bossAlertX;
            bannerY = GuiConfig.bossAlertY;
        } else if (isMinimapActive()) {
            float scale = getEffectiveScale(screenH);
            int mapPixelSize = (int) (MAP_TEX_SIZE * scale);
            int mapX = (GuiConfig.minimapX >= 0)
                    ? Mth.clamp(GuiConfig.minimapX, 16, Math.max(16, screenW - mapPixelSize - 16))
                    : Math.max(16, screenW - mapPixelSize - 18);
            int mapY = (GuiConfig.minimapY >= 0)
                    ? Mth.clamp(GuiConfig.minimapY, 16, Math.max(16, screenH - mapPixelSize - 32))
                    : 16;

            // Posicionar a la izquierda del minimapa/radar de portales
            if (mapX - scaledW - 6 >= 8) {
                bannerX = mapX - scaledW - 6;
                bannerY = mapY + 2;
            } else {
                bannerX = Math.max(8, mapX);
                bannerY = mapY + mapPixelSize + 6;
            }
        } else {
            int bossBottom = com.soybe.awakeningsystem.SystemHudOverlay.getBossBarOffset(mc);
            bannerX = 16;
            bannerY = bossBottom > 0 ? bossBottom + 5 : 12;
        }

        g.pose().pushPose();
        g.pose().translate(bannerX, bannerY, 300);
        g.pose().scale(bannerScale, bannerScale, 1.0F);

        g.fill(0, 0, badgeW, badgeH, bgCol);
        g.renderOutline(0, 0, badgeW, badgeH, bannerCol);
        g.drawString(mc.font, text, 5, 2, 0xFFFFFFFF, true);

        g.pose().popPose();
    }

    private static void drawHollowCircle(GuiGraphics g, int cx, int cy, int radius, int color) {
        if (radius <= 0) return;
        int[] outer = getCircleDx(radius);
        int[] inner = getCircleDx(radius - 1);
        if (outer == null || inner == null) return;

        for (int dy = -radius; dy <= radius; dy++) {
            int outerDx = outer[dy + radius];
            int innerDx = (dy >= -(radius - 1) && dy <= (radius - 1)) ? inner[dy + radius - 1] : 0;
            if (outerDx > innerDx) {
                g.fill(cx - outerDx, cy + dy, cx - innerDx, cy + dy + 1, color);
                g.fill(cx + innerDx, cy + dy, cx + outerDx, cy + dy + 1, color);
            }
        }
    }
}
