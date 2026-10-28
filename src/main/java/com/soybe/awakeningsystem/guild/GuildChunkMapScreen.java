package com.soybe.awakeningsystem.guild;

import com.mojang.blaze3d.platform.NativeImage;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.network.PacketDistributor;
import dev.diorama.MapSession;
import dev.diorama.Projection;
import dev.diorama.bake.TileKey;
import dev.diorama.render.MapViewport;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildChunkMapScreen extends Screen {
    private static final int[] ZOOM_GRID_SIZES = {7, 9, 11, 15};
    private static final int[] ZOOM_CELL_SIZES = {30, 24, 20, 15};
    private int zoomIndex = 1; // Por defecto: 9x9 chunks (24px por chunk = 216x216 px)

    // Caché estática en memoria de píxeles 16x16 por chunk para 0 lag al abrir/mover el mapa
    private static final Map<String, int[]> CHUNK_TERRAIN_CACHE = new HashMap<>();
    private static final Map<String, Long> CHUNK_CACHE_TIMESTAMP = new HashMap<>();

    private final MapViewport dioramaViewport = new MapViewport();
    private DynamicTexture mapTexture;
    private ResourceLocation mapTextureLocation;
    private int currentTexSize = -1;
    private int lastRenderedCenterX = Integer.MIN_VALUE;
    private int lastRenderedCenterZ = Integer.MIN_VALUE;
    private int lastRenderedZoom = -1;
    private String lastRenderedDim = "";
    private long lastTextureUpdateMs = 0L;

    private Guild guild;
    private CompoundTag allClaimsTag;
    private final Screen parentScreen;

    private int centerChunkX;
    private int centerChunkZ;

    private double mouseDownX = -1;
    private double mouseDownY = -1;
    private boolean isDragging = false;
    private double accumulatedDragX = 0;
    private double accumulatedDragZ = 0;
    private long openTimeMs = System.currentTimeMillis();

    public GuildChunkMapScreen(Guild guild, CompoundTag allClaimsTag, Screen parentScreen) {
        super(Component.translatable("gui.awakening_system.guild.map_title"));
        this.guild = guild;
        this.allClaimsTag = allClaimsTag != null ? allClaimsTag : new CompoundTag();
        this.parentScreen = parentScreen;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            this.centerChunkX = mc.player.chunkPosition().x;
            this.centerChunkZ = mc.player.chunkPosition().z;
        } else {
            this.centerChunkX = 0;
            this.centerChunkZ = 0;
        }
    }

    /**
     * Permite al cliente actualizar los reclamos y datos del gremio en tiempo real
     * sin cerrar esta pantalla ni expulsar al jugador al menú anterior.
     */
    public void updateData(Guild updatedGuild, CompoundTag updatedClaimsTag) {
        if (updatedGuild != null) {
            this.guild = updatedGuild;
        }
        if (updatedClaimsTag != null) {
            this.allClaimsTag = updatedClaimsTag;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.openTimeMs = System.currentTimeMillis();
        this.mouseDownX = -1;
        this.mouseDownY = -1;
        this.isDragging = false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        releaseMapTexture();
        this.dioramaViewport.close();
    }

    private void releaseMapTexture() {
        if (this.mapTexture != null) {
            this.mapTexture.close();
            this.mapTexture = null;
        }
        this.mapTextureLocation = null;
        this.currentTexSize = -1;
    }

    private int getGridSize() {
        return ZOOM_GRID_SIZES[zoomIndex];
    }

    private int getCellSize() {
        return ZOOM_CELL_SIZES[zoomIndex];
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 290.0F;
        float scaleY = (float) this.height / 330.0F;
        float scale = Math.min(scaleX, scaleY);
        return Math.min(1.0F, Math.max(0.35F, scale));
    }

    public double transformMouseX(double mouseX) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseX;
        double cx = this.width / 2.0;
        return (mouseX - cx) / scale + cx;
    }

    public double transformMouseY(double mouseY) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseY;
        double cy = this.height / 2.0;
        return (mouseY - cy) / scale + cy;
    }

    private void ensureTerrainTextureUpdated(ClientLevel level, String currentDim) {
        int gridSize = getGridSize();
        int texSize = gridSize * 16; // 1 píxel exacto por cada bloque del mundo (16x16 por chunk)
        long now = System.currentTimeMillis();

        boolean needsRecreate = (this.mapTexture == null || this.currentTexSize != texSize);
        if (needsRecreate) {
            releaseMapTexture();
            this.mapTexture = new DynamicTexture(texSize, texSize, false);
            this.mapTextureLocation = Minecraft.getInstance().getTextureManager()
                    .register("guild_terrain_map_" + texSize, this.mapTexture);
            this.currentTexSize = texSize;
        }

        boolean viewChanged = needsRecreate
                || this.lastRenderedCenterX != this.centerChunkX
                || this.lastRenderedCenterZ != this.centerChunkZ
                || this.lastRenderedZoom != this.zoomIndex
                || !this.lastRenderedDim.equals(currentDim);

        // Refrescar suavemente cada 2.5s o de inmediato al mover/hacer zoom
        if (!viewChanged && (now - this.lastTextureUpdateMs) < 2500L) {
            return;
        }

        this.lastRenderedCenterX = this.centerChunkX;
        this.lastRenderedCenterZ = this.centerChunkZ;
        this.lastRenderedZoom = this.zoomIndex;
        this.lastRenderedDim = currentDim;
        this.lastTextureUpdateMs = now;

        NativeImage img = this.mapTexture.getPixels();
        if (img == null) return;

        int halfGrid = gridSize / 2;
        for (int gz = 0; gz < gridSize; gz++) {
            for (int gx = 0; gx < gridSize; gx++) {
                int cx = centerChunkX + (gx - halfGrid);
                int cz = centerChunkZ + (gz - halfGrid);
                int[] chunkPixels = getOrSampleChunkTerrain(level, currentDim, cx, cz, now);

                int basePx = gx * 16;
                int basePz = gz * 16;

                if (chunkPixels != null) {
                    for (int lz = 0; lz < 16; lz++) {
                        int rowOffset = lz * 16;
                        for (int lx = 0; lx < 16; lx++) {
                            img.setPixelRGBA(basePx + lx, basePz + lz, chunkPixels[rowOffset + lx]);
                        }
                    }
                } else {
                    // Chunk no explorado / fuera del radio cargado: patrón cartográfico oscuro elegante
                    for (int lz = 0; lz < 16; lz++) {
                        for (int lx = 0; lx < 16; lx++) {
                            boolean checker = ((lx + lz) & 3) == 0;
                            int abgr = checker ? 0xFF1E1712 : 0xFF18120E;
                            img.setPixelRGBA(basePx + lx, basePz + lz, abgr);
                        }
                    }
                }
            }
        }
        this.mapTexture.upload();
    }

    private int[] getOrSampleChunkTerrain(ClientLevel level, String dim, int cx, int cz, long now) {
        if (level == null) return null;
        String key = dim + "#" + cx + "#" + cz;
        int[] cached = CHUNK_TERRAIN_CACHE.get(key);
        long lastTime = CHUNK_CACHE_TIMESTAMP.getOrDefault(key, 0L);

        // Si ya está en caché y se actualizó hace menos de 8 segundos, reusar instantáneamente (0 lag)
        if (cached != null && (now - lastTime) < 8000L) {
            return cached;
        }

        LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
        if (chunk == null || chunk.isEmpty()) {
            return cached; // Si el chunk se descargó al alejarse, conservar su terreno en memoria
        }

        int[] pixels = cached != null ? cached : new int[256];
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos floorPos = new BlockPos.MutableBlockPos();

        int minBuildY = level.getMinBuildHeight();
        int startWorldX = cx << 4;
        int startWorldZ = cz << 4;

        // Primero calculamos las alturas superficiales de 18x18 (incluyendo borde Norte y Oeste para sombreado topográfico 3D)
        int[][] heights = new int[17][17];
        for (int lz = -1; lz < 16; lz++) {
            for (int lx = -1; lx < 16; lx++) {
                int wx = startWorldX + lx;
                int wz = startWorldZ + lz;
                heights[lz + 1][lx + 1] = getColumnSurfaceY(level, chunk, wx, wz, lx, lz, minBuildY, mPos);
            }
        }

        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                int wx = startWorldX + lx;
                int wz = startWorldZ + lz;
                int surfaceY = heights[lz + 1][lx + 1];
                mPos.set(wx, surfaceY, wz);
                BlockState state = level.getBlockState(mPos);

                int rgb;
                float shade = 1.0F;

                if (!state.getFluidState().isEmpty() && state.getFluidState().is(FluidTags.WATER)) {
                    // Agua estilo JourneyMap / Xaero's: color de bioma + profundidad + transparencia costera
                    int biomeWater = BiomeColors.getAverageWaterColor(level, mPos);
                    int wr = (biomeWater >> 16) & 0xFF;
                    int wg = (biomeWater >> 8) & 0xFF;
                    int wb = biomeWater & 0xFF;
                    // Mezclar con azul marino limpio para que ningún bioma se vea apagado
                    wr = (wr * 65 + 32 * 35) / 100;
                    wg = (wg * 65 + 105 * 35) / 100;
                    wb = (wb * 65 + 210 * 35) / 100;

                    int waterDepth = 1;
                    int scanY = surfaceY - 1;
                    while (scanY > minBuildY && waterDepth < 18) {
                        floorPos.set(wx, scanY, wz);
                        BlockState below = level.getBlockState(floorPos);
                        if (!below.getFluidState().isEmpty() && below.getFluidState().is(FluidTags.WATER)) {
                            waterDepth++;
                            scanY--;
                        } else {
                            break;
                        }
                    }

                    if (waterDepth <= 3) {
                        // Orilla poco profunda: mezclar sutilmente con el color de la arena/fondo marino
                        floorPos.set(wx, Math.max(minBuildY, surfaceY - waterDepth), wz);
                        BlockState floorState = level.getBlockState(floorPos);
                        int floorRgb = getBlockBaseColor(level, floorState, floorPos);
                        float floorWeight = (4 - waterDepth) * 0.14F;
                        wr = (int) (wr * (1.0F - floorWeight) + ((floorRgb >> 16) & 0xFF) * floorWeight);
                        wg = (int) (wg * (1.0F - floorWeight) + ((floorRgb >> 8) & 0xFF) * floorWeight);
                        wb = (int) (wb * (1.0F - floorWeight) + (floorRgb & 0xFF) * floorWeight);
                        shade = 1.04F;
                    } else {
                        // Agua profunda: oscurecer gradualmente según profundidad
                        shade = Math.max(0.62F, 1.0F - (waterDepth - 3) * 0.026F);
                    }

                    // Ligero brillo de olas
                    if (((wx + wz) & 3) == 0) {
                        shade += 0.03F;
                    }
                    rgb = (Mth.clamp(wr, 0, 255) << 16) | (Mth.clamp(wg, 0, 255) << 8) | Mth.clamp(wb, 0, 255);
                } else if (!state.getFluidState().isEmpty() && state.getFluidState().is(FluidTags.LAVA)) {
                    rgb = (((wx * 3 + wz * 5) & 3) == 0) ? 0xFF8C1A : 0xFF6210;
                    shade = 1.12F;
                } else {
                    rgb = getBlockBaseColor(level, state, mPos);

                    // Sombreado topográfico 3D (sol desde el Noroeste, igual que Xaero's Minimap y JourneyMap)
                    int yNorth = heights[lz][lx + 1];
                    int yWest = heights[lz + 1][lx];
                    int slope = (surfaceY - yNorth) + (surfaceY - yWest);
                    slope = Mth.clamp(slope, -6, 6);
                    shade = 1.0F + slope * 0.068F;

                    // Micro-relieve en copas de árboles y vegetación
                    if (state.getBlock() instanceof LeavesBlock) {
                        int canopyNoise = ((wx * 19 + wz * 37) & 3) - 1;
                        shade += canopyNoise * 0.04F;
                    }
                }

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Iluminación cálida de antorchas/faroles en la superficie
                mPos.set(wx, surfaceY + 1, wz);
                int blockLight = level.getBrightness(LightLayer.BLOCK, mPos);
                if (blockLight > 3) {
                    float lightBoost = (blockLight - 3) * 0.015F;
                    r = Mth.clamp((int) (r * (shade + lightBoost * 1.3F)), 0, 255);
                    g = Mth.clamp((int) (g * (shade + lightBoost * 1.1F)), 0, 255);
                    b = Mth.clamp((int) (b * shade), 0, 255);
                } else {
                    r = Mth.clamp((int) (r * shade), 0, 255);
                    g = Mth.clamp((int) (g * shade), 0, 255);
                    b = Mth.clamp((int) (b * shade), 0, 255);
                }

                // NativeImage usa formato ABGR en memoria (0xAABBGGRR)
                pixels[lz * 16 + lx] = (0xFF << 24) | (b << 16) | (g << 8) | r;
            }
        }

        CHUNK_TERRAIN_CACHE.put(key, pixels);
        CHUNK_CACHE_TIMESTAMP.put(key, now);
        return pixels;
    }

    private int getColumnSurfaceY(ClientLevel level, LevelChunk centerChunk, int wx, int wz, int lx, int lz, int minBuildY, BlockPos.MutableBlockPos mPos) {
        int y;
        if (lx >= 0 && lx < 16 && lz >= 0 && lz < 16) {
            y = centerChunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz);
        } else {
            LevelChunk neighbor = level.getChunkSource().getChunk(wx >> 4, wz >> 4, ChunkStatus.FULL, false);
            if (neighbor != null && !neighbor.isEmpty()) {
                y = neighbor.getHeight(Heightmap.Types.WORLD_SURFACE, wx & 15, wz & 15);
            } else {
                y = level.getSeaLevel();
            }
        }

        // Bajar si el bloque superior es aire o invisible (MapColor.NONE) para encontrar el suelo/tejado real
        int steps = 0;
        while (y > minBuildY && steps < 40) {
            mPos.set(wx, y, wz);
            BlockState st = level.getBlockState(mPos);
            if (!st.isAir()) {
                if (!st.getFluidState().isEmpty()) {
                    return y;
                }
                MapColor mc = st.getMapColor(level, mPos);
                if (mc != MapColor.NONE) {
                    return y;
                }
            }
            y--;
            steps++;
        }
        return Math.max(minBuildY, y);
    }

    private int getBlockBaseColor(ClientLevel level, BlockState state, BlockPos pos) {
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.SUGAR_CANE)) {
            int grassCol = BiomeColors.getAverageGrassColor(level, pos);
            return brightenColor(grassCol, 1.06F);
        }
        if (state.getBlock() instanceof LeavesBlock || state.is(Blocks.VINE)) {
            int blockTint = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
            if (blockTint != -1 && blockTint != 0xFFFFFF) {
                return brightenColor(blockTint, 1.04F);
            }
            MapColor mc = state.getMapColor(level, pos);
            if (mc != MapColor.NONE && mc != MapColor.PLANT) {
                return mc.col;
            }
            return brightenColor(BiomeColors.getAverageFoliageColor(level, pos), 1.02F);
        }
        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
            return 0xF2F7FA;
        }
        int tint = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
        MapColor mapColor = state.getMapColor(level, pos);
        if (mapColor != MapColor.NONE) {
            if (tint != -1 && tint != 0xFFFFFF && mapColor == MapColor.PLANT) {
                return tint;
            }
            return mapColor.col;
        }
        return 0x565C64;
    }

    private int brightenColor(int rgb, float factor) {
        int r = Mth.clamp((int) (((rgb >> 16) & 0xFF) * factor), 0, 255);
        int g = Mth.clamp((int) (((rgb >> 8) & 0xFF) * factor), 0, 255);
        int b = Mth.clamp((int) ((rgb & 0xFF) * factor), 0, 255);
        return (r << 16) | (g << 8) | b;
    }

    private String getChunkOwnerId(String currentDim, int cx, int cz) {
        String key = currentDim + "#" + cx + "#" + cz;
        if (guild != null && guild.getClaimedChunks().contains(key)) {
            return "MY_GUILD";
        }
        if (allClaimsTag != null && allClaimsTag.contains(key)) {
            String other = allClaimsTag.getString(key);
            if (!other.isEmpty()) return other;
        }
        return "";
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cxF = this.width / 2.0F;
            float cyF = this.height / 2.0F;
            g.pose().translate(cxF, cyF, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cxF, -cyF, 0.0F);
        }

        int gridSize = getGridSize();
        int cellSize = getCellSize();
        int gridPixelSize = gridSize * cellSize;
        int mapLeft = (this.width - gridPixelSize) / 2;
        int mapTop = (this.height - gridPixelSize) / 2 - 8;

        // Marco exterior estilizado
        g.fill(mapLeft - 14, mapTop - 30, mapLeft + gridPixelSize + 14, mapTop + gridPixelSize + 40, 0xF20A1118);
        g.renderOutline(mapLeft - 14, mapTop - 30, gridPixelSize + 28, gridPixelSize + 70, 0xFFDFB76C);
        g.renderOutline(mapLeft - 1, mapTop + 5, gridPixelSize + 2, gridPixelSize + 2, 0xFF4EE2EC);

        String guildTag = guild != null ? guild.getTag() : "---";
        String guildName = guild != null ? guild.getName() : Component.translatable("gui.awakening_system.guild.no_guild").getString();
        g.drawString(this.font, Component.translatable("gui.awakening_system.guild.map.header", guildTag, guildName), mapLeft - 6, mapTop - 24, 0xFFFFD700);

        int claimedCount = guild != null ? guild.getClaimedChunks().size() : 0;
        int maxClaims = guild != null ? guild.getMaxClaims() : 0;
        int guildLevel = guild != null ? guild.getLevel() : 1;
        boolean canClaimMore = claimedCount < maxClaims;

        String claimStatus = canClaimMore
                ? Component.translatable("gui.awakening_system.guild.map.status_claimable", claimedCount, maxClaims, guildLevel).getString()
                : Component.translatable("gui.awakening_system.guild.map.status_limit", claimedCount, maxClaims, guildLevel).getString();
        g.drawString(this.font, claimStatus, mapLeft - 6, mapTop - 11, 0xFFFFFFFF);

        Minecraft mc = Minecraft.getInstance();
        String currentDim = mc.level != null ? mc.level.dimension().location().toString() : "minecraft:overworld";
        int playerChunkX = mc.player != null ? mc.player.chunkPosition().x : 0;
        int playerChunkZ = mc.player != null ? mc.player.chunkPosition().z : 0;

        // 1. Renderizar el Terreno 3D Voxel con el motor de Diorama (Relieve sombreado y biomas de alta fidelidad)
        int canvasTop = mapTop + 6;
        boolean dioramaRendered = false;
        MapSession session = MapSession.get();
        if (session != null && session.map() != null) {
            double centerU = (this.centerChunkX * 16.0) + 8.0;
            double centerV = Projection.v(0.0, (this.centerChunkZ * 16.0) + 8.0);
            double zoom = (double) cellSize / 16.0;
            MapViewport.View view = new MapViewport.View(
                    mapLeft, canvasTop, gridPixelSize, gridPixelSize,
                    centerU, centerV, zoom, TileKey.SURFACE
            );
            this.dioramaViewport.draw(g, session.map(), view);
            dioramaRendered = true;
        }

        if (!dioramaRendered) {
            if (mc.level != null) {
                ensureTerrainTextureUpdated(mc.level, currentDim);
                if (this.mapTextureLocation != null) {
                    int texSize = gridSize * 16;
                    g.blit(this.mapTextureLocation, mapLeft, canvasTop, gridPixelSize, gridPixelSize, 0.0F, 0.0F, texSize, texSize, texSize, texSize);
                }
            } else {
                g.fill(mapLeft, canvasTop, mapLeft + gridPixelSize, canvasTop + gridPixelSize, 0xFF101820);
            }
        }

        int halfGrid = gridSize / 2;
        int hoveredChunkX = Integer.MIN_VALUE;
        int hoveredChunkZ = Integer.MIN_VALUE;
        int hoveredBlockX = 0;
        int hoveredBlockZ = 0;
        String hoveredClaimInfo = null;

        // 2. Superponer Cuadrícula de Chunks y Bordes Conectados de Territorios Reclamados
        for (int gz = 0; gz < gridSize; gz++) {
            for (int gx = 0; gx < gridSize; gx++) {
                int cx = centerChunkX + (gx - halfGrid);
                int cz = centerChunkZ + (gz - halfGrid);
                int cellX = mapLeft + gx * cellSize;
                int cellY = canvasTop + gz * cellSize;

                String owner = getChunkOwnerId(currentDim, cx, cz);
                boolean isMyGuild = "MY_GUILD".equals(owner);
                boolean isOtherGuild = !owner.isEmpty() && !isMyGuild;

                boolean isHqHere = guild != null && guild.hasHq() && currentDim.equals(guild.getHqDimension())
                        && ((int) Math.floor(guild.getHqX() / 16.0)) == cx
                        && ((int) Math.floor(guild.getHqZ() / 16.0)) == cz;

                boolean isHovered = mX >= cellX && mX < cellX + cellSize && mY >= cellY && mY < cellY + cellSize;
                if (isHovered) {
                    hoveredChunkX = cx;
                    hoveredChunkZ = cz;
                    int localBx = Mth.clamp((int) (((mX - cellX) / (float) cellSize) * 16.0F), 0, 15);
                    int localBz = Mth.clamp((int) (((mY - cellY) / (float) cellSize) * 16.0F), 0, 15);
                    hoveredBlockX = (cx << 4) + localBx;
                    hoveredBlockZ = (cz << 4) + localBz;

                    if (isMyGuild) {
                        hoveredClaimInfo = Component.translatable("gui.awakening_system.guild.map.claimed_by_us", guild.getTag()).getString();
                    } else if (isOtherGuild) {
                        hoveredClaimInfo = Component.translatable("gui.awakening_system.guild.map.claimed_by_other", owner).getString();
                    } else if (canClaimMore) {
                        hoveredClaimInfo = Component.translatable("gui.awakening_system.guild.map.claimable_hint").getString();
                    } else {
                        hoveredClaimInfo = Component.translatable("gui.awakening_system.guild.map.limit_reached_hint").getString();
                    }
                }

                // Tinte translúcido sobre el terreno para ver los edificios, ríos y montañas debajo
                if (isMyGuild) {
                    int fill = isHqHere ? 0x48FFD700 : 0x3A00E5FF;
                    if (isHovered) fill = isHqHere ? 0x66FFD700 : 0x5500E5FF;
                    g.fill(cellX, cellY, cellX + cellSize, cellY + cellSize, fill);
                } else if (isOtherGuild) {
                    int fill = isHovered ? 0x60FF4444 : 0x42FF3333;
                    g.fill(cellX, cellY, cellX + cellSize, cellY + cellSize, fill);
                } else {
                    // Cuadrícula sutil para chunks salvajes
                    g.fill(cellX + cellSize - 1, cellY, cellX + cellSize, cellY + cellSize, 0x20FFFFFF);
                    g.fill(cellX, cellY + cellSize - 1, cellX + cellSize, cellY + cellSize, 0x20FFFFFF);
                    if (isHovered) {
                        g.fill(cellX, cellY, cellX + cellSize, cellY + cellSize, canClaimMore ? 0x3850FA7B : 0x30FFFFFF);
                        g.renderOutline(cellX, cellY, cellSize, cellSize, canClaimMore ? 0xFF50FA7B : 0xAAFFFFFF);
                    }
                }

                // Bordes conectados inteligentes (solo dibuja borde grueso en el perímetro exterior del territorio)
                if (!owner.isEmpty()) {
                    int borderCol = isHqHere ? 0xFFFFD700 : (isMyGuild ? 0xFF00E5FF : 0xFFFF5555);
                    int innerGridCol = isMyGuild ? 0x2600E5FF : 0x26FF5555;

                    boolean sameNorth = owner.equals(getChunkOwnerId(currentDim, cx, cz - 1));
                    boolean sameSouth = owner.equals(getChunkOwnerId(currentDim, cx, cz + 1));
                    boolean sameWest = owner.equals(getChunkOwnerId(currentDim, cx - 1, cz));
                    boolean sameEast = owner.equals(getChunkOwnerId(currentDim, cx + 1, cz));

                    // Norte
                    g.fill(cellX, cellY, cellX + cellSize, cellY + (sameNorth ? 1 : 2), sameNorth ? innerGridCol : borderCol);
                    // Sur
                    g.fill(cellX, cellY + cellSize - (sameSouth ? 1 : 2), cellX + cellSize, cellY + cellSize, sameSouth ? innerGridCol : borderCol);
                    // Oeste
                    g.fill(cellX, cellY, cellX + (sameWest ? 1 : 2), cellY + cellSize, sameWest ? innerGridCol : borderCol);
                    // Este
                    g.fill(cellX + cellSize - (sameEast ? 1 : 2), cellY, cellX + cellSize, cellY + cellSize, sameEast ? innerGridCol : borderCol);
                }

                if (isHqHere) {
                    g.drawCenteredString(this.font, "🏛️", cellX + cellSize / 2, cellY + cellSize / 2 - 4, 0xFFFFD700);
                }
            }
        }

        // 3. Renderizar Marcador del Jugador en su Posición Exacta del Mundo + Rostro de su Skin
        if (mc.player != null) {
            int relChunkX = playerChunkX - (centerChunkX - halfGrid);
            int relChunkZ = playerChunkZ - (centerChunkZ - halfGrid);
            if (relChunkX >= 0 && relChunkX < gridSize && relChunkZ >= 0 && relChunkZ < gridSize) {
                double inChunkX = (mc.player.getX() - (playerChunkX * 16.0)) / 16.0;
                double inChunkZ = (mc.player.getZ() - (playerChunkZ * 16.0)) / 16.0;
                int px = mapLeft + (int) Math.round((relChunkX + Mth.clamp(inChunkX, 0.0, 1.0)) * cellSize);
                int pz = canvasTop + (int) Math.round((relChunkZ + Mth.clamp(inChunkZ, 0.0, 1.0)) * cellSize);

                // Halo indicador de dirección de mirada
                double yawRad = Math.toRadians(mc.player.getYRot() + 180.0F);
                int dirX = px + (int) Math.round(Math.sin(yawRad) * 6.5);
                int dirZ = pz - (int) Math.round(Math.cos(yawRad) * 6.5);
                g.fill(dirX - 2, dirZ - 2, dirX + 2, dirZ + 2, 0xFFFFD700);

                // Rostro 8x8 del jugador con borde blanco/dorado tipo minimapa
                g.fill(px - 5, pz - 5, px + 5, pz + 5, 0xFFFFFFFF);
                PlayerFaceRenderer.draw(g, mc.player.getSkin(), px - 4, pz - 4, 8);
            }
        }

        // 4. Indicador de Brújula Cardinal (N) en la esquina superior derecha del mapa
        g.fill(mapLeft + gridPixelSize - 15, canvasTop + 3, mapLeft + gridPixelSize - 3, canvasTop + 15, 0xCC0A1118);
        g.renderOutline(mapLeft + gridPixelSize - 15, canvasTop + 3, 12, 12, 0xFFDFB76C);
        g.drawCenteredString(this.font, "§c§lN", mapLeft + gridPixelSize - 9, canvasTop + 5, 0xFFFF5555);

        // Botones de Zoom (+/-) en la esquina superior derecha del encabezado
        int zoomOutX = mapLeft + gridPixelSize - 36;
        int zoomInX = mapLeft + gridPixelSize - 17;
        int zoomY = mapTop - 13;
        renderSmallBtn(g, zoomOutX, zoomY, 16, 14, "§f-", mX, mY);
        renderSmallBtn(g, zoomInX, zoomY, 16, 14, "§f+", mX, mY);

        g.drawCenteredString(this.font, Component.translatable("gui.awakening_system.guild.map_hint"), mapLeft + gridPixelSize / 2, canvasTop + gridPixelSize + 3, 0xFFAAAAAA);
        int navY = canvasTop + gridPixelSize + 14;
        int btnH = 16;

        int myLocBtnX = mapLeft;
        int myLocBtnW = 86;
        boolean myLocHov = mX >= myLocBtnX && mX < myLocBtnX + myLocBtnW && mY >= navY && mY < navY + btnH;
        g.fill(myLocBtnX, navY, myLocBtnX + myLocBtnW, navY + btnH, myLocHov ? 0xDD225577 : 0x88113344);
        g.renderOutline(myLocBtnX, navY, myLocBtnW, btnH, myLocHov ? 0xFF00E5FF : 0xFF2A4255);
        g.drawCenteredString(this.font, Component.translatable("gui.awakening_system.guild.map_center"), myLocBtnX + myLocBtnW / 2, navY + 4, 0xFF70E8FF);

        int arrowStartX = myLocBtnX + myLocBtnW + 6;
        renderNavArrow(g, arrowStartX, navY, "◀", mX, mY);
        renderNavArrow(g, arrowStartX + 18, navY, "▲", mX, mY);
        renderNavArrow(g, arrowStartX + 36, navY, "▼", mX, mY);
        renderNavArrow(g, arrowStartX + 54, navY, "▶", mX, mY);

        int backBtnX = mapLeft + gridPixelSize - 64;
        int backBtnW = 64;
        boolean backHov = mX >= backBtnX && mX < backBtnX + backBtnW && mY >= navY && mY < navY + btnH;
        g.fill(backBtnX, navY, backBtnX + backBtnW, navY + btnH, backHov ? 0xDD8B2B2B : 0x885A1A1A);
        g.renderOutline(backBtnX, navY, backBtnW, btnH, backHov ? 0xFFFF5555 : 0xFF8B2B2B);
        g.drawCenteredString(this.font, Component.translatable("gui.awakening_system.guild.map.back"), backBtnX + backBtnW / 2, navY + 4, 0xFFFF8888);

        if (hoveredChunkX != Integer.MIN_VALUE) {
            Component tip = Component.literal("§eChunk [" + hoveredChunkX + ", " + hoveredChunkZ + "] §7| §b"
                    + Component.translatable("gui.awakening_system.guild.map.coords", hoveredBlockX, hoveredBlockZ).getString()
                    + "\n" + hoveredClaimInfo);
            g.renderTooltip(this.font, tip, mX, mY);
        }

        g.pose().popPose();
    }

    private void renderSmallBtn(GuiGraphics g, int x, int y, int w, int h, String label, int mouseX, int mouseY) {
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        g.fill(x, y, x + w, y + h, hov ? 0xDD2A4D66 : 0xAA152533);
        g.renderOutline(x, y, w, h, hov ? 0xFF00E5FF : 0xFF2A4255);
        g.drawCenteredString(this.font, label, x + w / 2, y + 3, hov ? 0xFFFFFFFF : 0xFF70E8FF);
    }

    private void renderNavArrow(GuiGraphics g, int x, int y, String label, int mouseX, int mouseY) {
        boolean hov = mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
        g.fill(x, y, x + 16, y + 16, hov ? 0xDD2A4D66 : 0x88152533);
        g.renderOutline(x, y, 16, 16, hov ? 0xFF00E5FF : 0xFF2A4255);
        g.drawCenteredString(this.font, label, x + 8, y + 4, hov ? 0xFFFFFFFF : 0xFF70E8FF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0 && zoomIndex > 0) {
            zoomIndex--;
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.7F, 1.3F);
            return true;
        } else if (scrollY < 0 && zoomIndex < ZOOM_GRID_SIZES.length - 1) {
            zoomIndex++;
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.7F, 0.9F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        float s = getGuiScale();
        dragX /= s;
        dragY /= s;

        int cellPitch = getCellSize();
        accumulatedDragX += dragX;
        accumulatedDragZ += dragY;

        boolean moved = false;
        while (accumulatedDragX >= cellPitch) {
            centerChunkX--;
            accumulatedDragX -= cellPitch;
            moved = true;
        }
        while (accumulatedDragX <= -cellPitch) {
            centerChunkX++;
            accumulatedDragX += cellPitch;
            moved = true;
        }
        while (accumulatedDragZ >= cellPitch) {
            centerChunkZ--;
            accumulatedDragZ -= cellPitch;
            moved = true;
        }
        while (accumulatedDragZ <= -cellPitch) {
            centerChunkZ++;
            accumulatedDragZ += cellPitch;
            moved = true;
        }

        if (moved) {
            this.isDragging = true;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (System.currentTimeMillis() - this.openTimeMs < 200L) {
            return false;
        }
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);

        if (button == 0 || button == 1) {
            this.mouseDownX = mouseX;
            this.mouseDownY = mouseY;
            this.isDragging = false;
        }

        if (button == 0) {
            int gridSize = getGridSize();
            int cellSize = getCellSize();
            int gridPixelSize = gridSize * cellSize;
            int mapLeft = (this.width - gridPixelSize) / 2;
            int mapTop = (this.height - gridPixelSize) / 2 - 8;
            int canvasTop = mapTop + 6;
            int navY = canvasTop + gridPixelSize + 14;
            int btnH = 16;

            // Botones de Zoom (+/-)
            int zoomOutX = mapLeft + gridPixelSize - 36;
            int zoomInX = mapLeft + gridPixelSize - 17;
            int zoomY = mapTop - 13;
            if (mouseY >= zoomY && mouseY < zoomY + 14) {
                if (mouseX >= zoomOutX && mouseX < zoomOutX + 16) {
                    if (zoomIndex < ZOOM_GRID_SIZES.length - 1) zoomIndex++;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 0.9F);
                    return true;
                } else if (mouseX >= zoomInX && mouseX < zoomInX + 16) {
                    if (zoomIndex > 0) zoomIndex--;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.3F);
                    return true;
                }
            }

            int myLocBtnX = mapLeft;
            int myLocBtnW = 86;
            if (mouseX >= myLocBtnX && mouseX < myLocBtnX + myLocBtnW && mouseY >= navY && mouseY < navY + btnH) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    this.centerChunkX = mc.player.chunkPosition().x;
                    this.centerChunkZ = mc.player.chunkPosition().z;
                    this.accumulatedDragX = 0;
                    this.accumulatedDragZ = 0;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                }
                return true;
            }

            int arrowStartX = myLocBtnX + myLocBtnW + 6;
            if (mouseY >= navY && mouseY < navY + 16) {
                if (mouseX >= arrowStartX && mouseX < arrowStartX + 16) {
                    centerChunkX--;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.0F);
                    return true;
                } else if (mouseX >= arrowStartX + 18 && mouseX < arrowStartX + 34) {
                    centerChunkZ--;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.0F);
                    return true;
                } else if (mouseX >= arrowStartX + 36 && mouseX < arrowStartX + 52) {
                    centerChunkZ++;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.0F);
                    return true;
                } else if (mouseX >= arrowStartX + 54 && mouseX < arrowStartX + 70) {
                    centerChunkX++;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.0F);
                    return true;
                }
            }

            int backBtnX = mapLeft + gridPixelSize - 64;
            int backBtnW = 64;
            if (mouseX >= backBtnX && mouseX < backBtnX + backBtnW && mouseY >= navY && mouseY < navY + btnH) {
                if (this.parentScreen != null) {
                    Minecraft.getInstance().setScreen(this.parentScreen);
                } else {
                    onClose();
                }
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (System.currentTimeMillis() - this.openTimeMs < 200L) {
            this.mouseDownX = -1;
            this.mouseDownY = -1;
            this.isDragging = false;
            return false;
        }
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        boolean wasClick = (this.mouseDownX >= 0 && this.mouseDownY >= 0 && !this.isDragging);
        if (wasClick) {
            double distSq = Math.pow(mouseX - this.mouseDownX, 2) + Math.pow(mouseY - this.mouseDownY, 2);
            if (distSq > 36.0) {
                wasClick = false;
            }
        }
        this.mouseDownX = -1;
        this.mouseDownY = -1;
        this.isDragging = false;

        if (button == 0 && wasClick) {
            int gridSize = getGridSize();
            int cellSize = getCellSize();
            int gridPixelSize = gridSize * cellSize;
            int mapLeft = (this.width - gridPixelSize) / 2;
            int mapTop = (this.height - gridPixelSize) / 2 - 8;
            int canvasTop = mapTop + 6;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && guild != null) {
                UUID myUuid = mc.player.getUUID();
                GuildRole role = guild.getMemberRole(myUuid);
                boolean canClaim = (role != null && role.canClaimTerritory());

                String currentDim = mc.level != null ? mc.level.dimension().location().toString() : "minecraft:overworld";
                int halfGrid = gridSize / 2;

                for (int gz = 0; gz < gridSize; gz++) {
                    for (int gx = 0; gx < gridSize; gx++) {
                        int cx = centerChunkX + (gx - halfGrid);
                        int cz = centerChunkZ + (gz - halfGrid);
                        int cellX = mapLeft + gx * cellSize;
                        int cellY = canvasTop + gz * cellSize;

                        if (mouseX >= cellX && mouseX < cellX + cellSize && mouseY >= cellY && mouseY < cellY + cellSize) {
                            String key = currentDim + "#" + cx + "#" + cz;
                            boolean isMyGuild = guild.getClaimedChunks().contains(key);
                            boolean isOtherGuild = (!isMyGuild && allClaimsTag.contains(key));

                            if (isOtherGuild) {
                                AwakeningSystemClient.playUiSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                return true;
                            }

                            if (!canClaim) {
                                mc.player.sendSystemMessage(Component.translatable("gui.awakening_system.guild.no_permission_territory"));
                                AwakeningSystemClient.playUiSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                return true;
                            }

                            if (isMyGuild) {
                                guild.unclaimChunk(currentDim, cx, cz);
                                PacketDistributor.sendToServer(new GuildActionPayload("UNCLAIM_CHUNK", cx + ":" + cz, 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_DESTROY, 0.7F, 1.4F);
                            } else {
                                if (guild.getClaimedChunks().size() >= guild.getMaxClaims()) {
                                    mc.player.sendSystemMessage(Component.translatable("gui.awakening_system.guild.claim_limit_reached", guild.getClaimedChunks().size(), guild.getMaxClaims()));
                                    AwakeningSystemClient.playUiSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                    return true;
                                }
                                guild.claimChunk(currentDim, cx, cz);
                                PacketDistributor.sendToServer(new GuildActionPayload("CLAIM_CHUNK", cx + ":" + cz, 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.6F);
                            }
                            return true;
                        }
                    }
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }
}
