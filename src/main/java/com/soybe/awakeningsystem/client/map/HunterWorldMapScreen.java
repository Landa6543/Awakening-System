package com.soybe.awakeningsystem.client.map;

import com.mojang.blaze3d.platform.NativeImage;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.client.gui.GuiConfig;
import com.soybe.awakeningsystem.client.gui.GuiConfigScreen;
import com.soybe.awakeningsystem.compat.XaeroGuildMapIntegration;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pantalla completa (100% de la ventana) de Mapa Táctico de Cazador (tecla dedicada M).
 * Ofrece desplazamiento continuo y fluido píxel a píxel, zoom suave centrado en el cursor,
 * barras de control flotantes translúcidas, alternancia instantánea del minimapa del HUD,
 * modo cuevas inteligente y creación de Waypoints con clic derecho (estilo Xaero's / JourneyMap).
 */
public class HunterWorldMapScreen extends Screen {
    // Zoom: píxeles por bloque
    private static final double[] ZOOM_PIXELS_PER_BLOCK = {0.75, 1.0, 1.5, 2.0, 2.75, 3.5};
    private int zoomIndex = 2; // 1.5 píxeles por bloque por defecto (24 px por chunk)

    // Tamaño de la textura del terreno: 2048x2048 píxeles = 128x128 chunks (2048x2048 bloques)
    // Cubre completamente monitores Ultra-Wide (21:9 / 32:9) y 4K con cero barras negras
    private static final int TEX_SIZE = 2048;
    private static final int TEX_CHUNKS = 128;
    private static final int HALF_TEX_CHUNKS = 64;

    // Caché en memoria para chunks estáticos (0 lag al arrastrar)
    private static final Map<String, int[]> CHUNK_SURFACE_CACHE = new HashMap<>();
    private static final Map<String, Long> CHUNK_CACHE_TIME = new HashMap<>();

    private final boolean[][] chunkRendered = new boolean[TEX_CHUNKS][TEX_CHUNKS];

    private DynamicTexture mapTexture;
    private ResourceLocation mapTextureLoc;
    private int texCenterChunkX = Integer.MIN_VALUE;
    private int texCenterChunkZ = Integer.MIN_VALUE;
    private String lastRenderedDim = "";
    private String lastRenderedTimeSlice = "";
    private long lastTextureUpdateMs = 0L;
    private boolean forceCaveMode = false;

    // Coordenadas continuas de la cámara en bloques del mundo (sub-píxel)
    private double cameraWorldX;
    private double cameraWorldZ;

    private boolean isDragging = false;

    private int hoveredBlockX = 0;
    private int hoveredBlockZ = 0;
    private boolean isBlockHovered = false;

    // Opciones de capas estilo JourneyMap
    private boolean showChunkGrid = false;
    private boolean showEntities = true;
    private boolean showWaypoints = true;
    private boolean showGuildClaims = true;
    private boolean showItems = true;
    private boolean showGates = true;
    private LivingEntity hoveredEntity = null;
    private net.minecraft.world.entity.item.ItemEntity hoveredItemDrop = null;
    private com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry hoveredGate = null;

    // Estado del Menú Contextual (Clic Derecho estilo JourneyMap)
    private boolean contextMenuOpen = false;
    private int contextMenuX = 0;
    private int contextMenuY = 0;
    private int contextBlockX = 0;
    private int contextBlockY = 0;
    private int contextBlockZ = 0;
    private int contextChunkX = 0;
    private int contextChunkZ = 0;
    private String contextBiome = "";
    private Component contextGuildClaim = null;

    private static class PillButtonDef {
        int x, y, w, h;
        Runnable onClick;
    }
    private final List<PillButtonDef> pillButtons = new java.util.ArrayList<>();

    public HunterWorldMapScreen() {
        super(Component.translatable("awakening_system.map.title"));
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            this.cameraWorldX = mc.player.getX();
            this.cameraWorldZ = mc.player.getZ();
        } else {
            this.cameraWorldX = 0.0D;
            this.cameraWorldZ = 0.0D;
        }
    }

    public void centerOnCoordinates(double x, double z) {
        this.cameraWorldX = x;
        this.cameraWorldZ = z;
    }

    @Override
    protected void init() {
        super.init();
        CHUNK_CACHE_TIME.clear();
        for (int i = 0; i < TEX_CHUNKS; i++) {
            java.util.Arrays.fill(this.chunkRendered[i], false);
        }
    }

    public static void updateCachedChunk(String dim, int cx, int cz, int[] pixels) {
        if (pixels == null || pixels.length < 256) return;
        String key = dim + "#" + cx + "#" + cz;
        CHUNK_SURFACE_CACHE.put(key, pixels);
        CHUNK_CACHE_TIME.put(key, System.currentTimeMillis());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // No-op: do not call vanilla renderBackground to avoid blur
    }

    @Override
    public void renderBlurredBackground(float partialTick) {
        // No-op: disable vanilla 1.21 post-processing blur shader
    }

    @Override
    public void renderTransparentBackground(GuiGraphics g) {
        // No-op
    }

    @Override
    public void removed() {
        super.removed();
        releaseMapTexture();
        WorldMapChunkCache.flushAll();
    }

    private void releaseMapTexture() {
        if (this.mapTexture != null) {
            this.mapTexture.close();
            this.mapTexture = null;
        }
        this.mapTextureLoc = null;
        this.texCenterChunkX = Integer.MIN_VALUE;
        this.texCenterChunkZ = Integer.MIN_VALUE;
        for (int i = 0; i < TEX_CHUNKS; i++) {
            java.util.Arrays.fill(this.chunkRendered[i], false);
        }
    }

    private double getPixelsPerBlock() {
        return ZOOM_PIXELS_PER_BLOCK[zoomIndex];
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            this.renderBackground(g, mouseX, mouseY, partialTick);
            return;
        }

        double minPpb = Math.max(0.75D, Math.max((double) this.width / (double) TEX_SIZE, (double) this.height / (double) TEX_SIZE));
        double ppb = Math.max(minPpb, getPixelsPerBlock());
        double scX = this.width / 2.0D;
        double scY = this.height / 2.0D;
        String currentDim = mc.level.dimension().location().toString();

        // 1. Fondo Oscuro del Lienzo a Pantalla Completa
        g.fill(0, 0, this.width, this.height, 0xFF080D14);

        int minVisChunkX = (int) Math.floor((cameraWorldX - scX / ppb) / 16.0D) - 1;
        int maxVisChunkX = (int) Math.floor((cameraWorldX + scX / ppb) / 16.0D) + 1;
        int minVisChunkZ = (int) Math.floor((cameraWorldZ - scY / ppb) / 16.0D) - 1;
        int maxVisChunkZ = (int) Math.floor((cameraWorldZ + scY / ppb) / 16.0D) + 1;

        // 2. Actualizar Textura Táctica Dinámica si es necesario (sólo muestrea chunks visibles para 0 lag)
        updateMapTextureIfNeeded(mc.level, currentDim, minVisChunkX, maxVisChunkX, minVisChunkZ, maxVisChunkZ);

        // 3. Renderizar Textura Continua a Pantalla Completa con filtrado GL_NEAREST (cero blur)
        if (this.mapTextureLoc != null) {
            double texOriginWorldX = (texCenterChunkX - HALF_TEX_CHUNKS) * 16.0D;
            double texOriginWorldZ = (texCenterChunkZ - HALF_TEX_CHUNKS) * 16.0D;

            int drawX = (int) Math.round(scX + (texOriginWorldX - cameraWorldX) * ppb);
            int drawY = (int) Math.round(scY + (texOriginWorldZ - cameraWorldZ) * ppb);
            int drawW = (int) Math.round(TEX_SIZE * ppb);
            int drawH = (int) Math.round(TEX_SIZE * ppb);

            com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, this.mapTextureLoc);
            com.mojang.blaze3d.platform.GlStateManager._texParameter(3553, 10240, 9728);
            com.mojang.blaze3d.platform.GlStateManager._texParameter(3553, 10241, 9728);
            g.blit(this.mapTextureLoc, drawX, drawY, drawW, drawH, 0.0F, 0.0F, TEX_SIZE, TEX_SIZE, TEX_SIZE, TEX_SIZE);
            g.flush();
        }

        // 3.5. Cuadrícula de Chunks estilo JourneyMap (Líneas finas cian / azul translúcido)
        if (showChunkGrid) {
            for (int cx = minVisChunkX; cx <= maxVisChunkX + 1; cx++) {
                int lineX = (int) Math.round(scX + (cx * 16.0D - cameraWorldX) * ppb);
                g.fill(lineX, 0, lineX + 1, this.height, 0x2E4EE2EC);
            }
            for (int cz = minVisChunkZ; cz <= maxVisChunkZ + 1; cz++) {
                int lineY = (int) Math.round(scY + (cz * 16.0D - cameraWorldZ) * ppb);
                g.fill(0, lineY, this.width, lineY + 1, 0x2E4EE2EC);
            }
        }

        // 3.6. Chunks Reclamados por Gremios (Territorio Táctico)
        if (showGuildClaims) {
            renderGuildClaims(g, mc.level, scX, scY, ppb, minVisChunkX, maxVisChunkX, minVisChunkZ, maxVisChunkZ);
        }

        // 4. Calcular Bloque y Chunk bajo el Cursor
        hoveredBlockX = (int) Math.floor(cameraWorldX + (mouseX - scX) / ppb);
        hoveredBlockZ = (int) Math.floor(cameraWorldZ + (mouseY - scY) / ppb);
        isBlockHovered = mouseX >= 0 && mouseX < this.width && mouseY >= 42 && mouseY < this.height - 28;

        // Cuadrícula sutil del chunk bajo el cursor
        if (isBlockHovered) {
            int hoveredChunkX = hoveredBlockX >> 4;
            int hoveredChunkZ = hoveredBlockZ >> 4;
            int chunkScreenX = (int) Math.round(scX + (hoveredChunkX * 16.0D - cameraWorldX) * ppb);
            int chunkScreenY = (int) Math.round(scY + (hoveredChunkZ * 16.0D - cameraWorldZ) * ppb);
            int chunkScreenSize = (int) Math.round(16.0D * ppb);
            g.renderOutline(chunkScreenX, chunkScreenY, chunkScreenSize, chunkScreenSize, 0x664EE2EC);
        }

        // 5. Marcadores Waypoints en Pantalla Completa
        if (showWaypoints) {
            renderWaypoints(g, currentDim, scX, scY, ppb);
        }

        // 5.5 Ítems Caídos y Artefactos en Pantalla Completa
        this.hoveredItemDrop = null;
        if (showItems) {
            renderItemDropsOnMap(g, mc, scX, scY, ppb, mouseX, mouseY);
        }

        // 5.8 Gates y Grietas Activas en Pantalla Completa
        this.hoveredGate = null;
        if (showGates) {
            renderActiveRiftsOnMap(g, mc, scX, scY, ppb, mouseX, mouseY);
        }

        // 6. Entidades y Radar en Pantalla Completa
        this.hoveredEntity = null;
        if (showEntities) {
            renderEntitiesOnMap(g, mc, scX, scY, ppb, mouseX, mouseY);
        }

        // 7. Marcador del Jugador en Pantalla Completa
        renderPlayerOnMap(g, mc.player, scX, scY, ppb, mouseX, mouseY);

        // 8. Barra Superior Flotante Translúcida y Rosa de los Vientos
        renderTopBar(g, mouseX, mouseY);

        // 9. Barra Inferior Flotante de Coordenadas e Información
        renderBottomBar(g, mc);

        // 9.5. Tooltip Flotante de Gate, Entidad o Ítem Drop (Estilo JourneyMap)
        if (this.hoveredGate != null && !contextMenuOpen) {
            renderGateHoverTooltip(g, mouseX, mouseY);
        } else if (this.hoveredEntity != null && !contextMenuOpen) {
            renderEntityHoverTooltip(g, mouseX, mouseY);
        } else if (this.hoveredItemDrop != null && !contextMenuOpen) {
            renderItemDropHoverTooltip(g, mouseX, mouseY);
        }

        // 10. Menú Contextual Flotante de Clic Derecho (Estilo JourneyMap)
        if (contextMenuOpen) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 500);
            renderContextMenu(g, mouseX, mouseY);
            g.pose().popPose();
            g.flush();
        }

        g.flush();
    }

    private void renderGuildClaims(GuiGraphics g, ClientLevel level, double scX, double scY, double ppb, int minVisChunkX, int maxVisChunkX, int minVisChunkZ, int maxVisChunkZ) {
        int[] colorStore = new int[5];
        for (int cz = minVisChunkZ; cz <= maxVisChunkZ; cz++) {
            for (int cx = minVisChunkX; cx <= maxVisChunkX; cx++) {
                if (!XaeroGuildMapIntegration.isChunkClaimed(level.dimension(), cx, cz)) continue;

                int[] colors = XaeroGuildMapIntegration.computeChunkColors(colorStore, level.dimension(), cx, cz);
                if (colors == null) continue;

                int x0 = (int) Math.round(scX + (cx * 16.0D - cameraWorldX) * ppb);
                int y0 = (int) Math.round(scY + (cz * 16.0D - cameraWorldZ) * ppb);
                int x1 = (int) Math.round(scX + ((cx + 1) * 16.0D - cameraWorldX) * ppb);
                int y1 = (int) Math.round(scY + ((cz + 1) * 16.0D - cameraWorldZ) * ppb);

                // Relleno translúcido
                g.fill(x0, y0, x1, y1, colors[0]);

                // Bordes con conectividad inteligente
                int bThick = Math.max(1, (int) Math.round(ppb * 0.75D));
                if (colors[1] != colors[0]) g.fill(x0, y0, x1, y0 + bThick, colors[1]); // North
                if (colors[2] != colors[0]) g.fill(x1 - bThick, y0, x1, y1, colors[2]); // East
                if (colors[3] != colors[0]) g.fill(x0, y1 - bThick, x1, y1, colors[3]); // South
                if (colors[4] != colors[0]) g.fill(x0, y0, x0 + bThick, y1, colors[4]); // West
            }
        }
    }

    public static int applyLightingToPixel(int abgr, float dayNightFactor) {
        if (dayNightFactor >= 0.99F) {
            return abgr;
        }
        int a = (abgr >> 24) & 0xFF;
        int b = (abgr >> 16) & 0xFF;
        int g = (abgr >> 8) & 0xFF;
        int r = abgr & 0xFF;

        r = Mth.clamp((int) (r * dayNightFactor), 0, 255);
        g = Mth.clamp((int) (g * dayNightFactor), 0, 255);
        b = Mth.clamp((int) (b * dayNightFactor), 0, 255);

        // Luz de luna fría de noche
        float nightRatio = (0.90F - dayNightFactor) / 0.55F;
        if (nightRatio > 0.0F) {
            r = Mth.clamp((int) (r * (1.0F - nightRatio * 0.12F)), 0, 255);
            g = Mth.clamp((int) (g * (1.0F - nightRatio * 0.08F)), 0, 255);
            b = Mth.clamp((int) (b * (1.0F + nightRatio * 0.18F)), 0, 255);
        }

        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private String getChunkCacheKey(ClientLevel level, String dim, int cx, int cz) {
        if (forceCaveMode) {
            int scanY = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getBlockY() : 64;
            if (Minecraft.getInstance().player != null && level.canSeeSky(Minecraft.getInstance().player.blockPosition())) {
                scanY = Math.min(scanY - 16, 32);
            }
            int caveSlice = scanY >> 3; // Rebanadas de 8 bloques verticales estilo JourneyMap
            return dim + "#" + cx + "#" + cz + "#c" + caveSlice;
        } else {
            return dim + "#" + cx + "#" + cz;
        }
    }

    private String getCurrentTimeSliceTag(ClientLevel level) {
        if (forceCaveMode) {
            int scanY = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getBlockY() : 64;
            if (Minecraft.getInstance().player != null && level.canSeeSky(Minecraft.getInstance().player.blockPosition())) {
                scanY = Math.min(scanY - 16, 32);
            }
            return "c" + (scanY >> 3);
        } else {
            String lightMode = GuiConfig.minimapLightingMode != null ? GuiConfig.minimapLightingMode.toUpperCase(java.util.Locale.ROOT) : "AUTO";
            if ("DAY".equals(lightMode)) {
                return "lm_DAY";
            } else if ("NIGHT".equals(lightMode)) {
                return "lm_NIGHT";
            } else {
                float skyAngle = level.getTimeOfDay(1.0F);
                float sunFactor = Mth.clamp(Mth.cos(skyAngle * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
                float dayNightFactor = Mth.lerp(sunFactor, 0.35F, 1.0F);
                int timeBucket = (int) (dayNightFactor * 7.99F);
                return "lm_AUTO_t" + timeBucket;
            }
        }
    }

    private void updateMapTextureIfNeeded(ClientLevel level, String currentDim, int minVisChunkX, int maxVisChunkX, int minVisChunkZ, int maxVisChunkZ) {
        long now = System.currentTimeMillis();
        int curCamChunkX = (int) Math.floor(cameraWorldX / 16.0D);
        int curCamChunkZ = (int) Math.floor(cameraWorldZ / 16.0D);

        boolean dimChanged = !currentDim.equals(this.lastRenderedDim);
        boolean centerFar = Math.abs(curCamChunkX - this.texCenterChunkX) >= 48 || Math.abs(curCamChunkZ - this.texCenterChunkZ) >= 48;
        boolean needsReset = this.mapTexture == null || dimChanged || centerFar;

        String currentSliceTag = getCurrentTimeSliceTag(level);
        boolean sliceChanged = !currentSliceTag.equals(this.lastRenderedTimeSlice);
        if (sliceChanged) {
            this.lastRenderedTimeSlice = currentSliceTag;
            for (int i = 0; i < TEX_CHUNKS; i++) {
                java.util.Arrays.fill(this.chunkRendered[i], false);
            }
        }

        if (needsReset) {
            this.texCenterChunkX = curCamChunkX;
            this.texCenterChunkZ = curCamChunkZ;
            this.lastRenderedDim = currentDim;
            this.lastTextureUpdateMs = now;
            for (int i = 0; i < TEX_CHUNKS; i++) {
                java.util.Arrays.fill(this.chunkRendered[i], false);
            }
            if (this.mapTexture != null && this.mapTexture.getPixels() != null) {
                this.mapTexture.getPixels().fillRect(0, 0, TEX_SIZE, TEX_SIZE, 0xFF080D14);
            }
        }

        if (this.mapTexture == null) {
            NativeImage img = new NativeImage(TEX_SIZE, TEX_SIZE, false);
            img.fillRect(0, 0, TEX_SIZE, TEX_SIZE, 0xFF080D14);
            this.mapTexture = new DynamicTexture(img);
            this.mapTexture.setFilter(false, false);
            this.mapTextureLoc = Minecraft.getInstance().getTextureManager().register("hunter_worldmap_fullscreen_dyn", this.mapTexture);
        }

        NativeImage img = this.mapTexture.getPixels();
        if (img == null) return;

        int minChunkX = this.texCenterChunkX - HALF_TEX_CHUNKS;
        int minChunkZ = this.texCenterChunkZ - HALF_TEX_CHUNKS;

        int startGx = Math.max(0, minVisChunkX - minChunkX);
        int endGx = Math.min(TEX_CHUNKS - 1, maxVisChunkX - minChunkX);
        int startGz = Math.max(0, minVisChunkZ - minChunkZ);
        int endGz = Math.min(TEX_CHUNKS - 1, maxVisChunkZ - minChunkZ);

        boolean anyUpdated = false;
        List<int[]> pendingUncached = new java.util.ArrayList<>();

        float dayNightFactor = 1.0F;
        if (!forceCaveMode) {
            String lightMode = GuiConfig.minimapLightingMode != null ? GuiConfig.minimapLightingMode.toUpperCase(java.util.Locale.ROOT) : "AUTO";
            if ("NIGHT".equals(lightMode)) {
                dayNightFactor = 0.35F;
            } else if ("AUTO".equals(lightMode)) {
                float skyAngle = level.getTimeOfDay(1.0F);
                float sunFactor = Mth.clamp(Mth.cos(skyAngle * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
                dayNightFactor = Mth.lerp(sunFactor, 0.35F, 1.0F);
            }
        }

        // 1. Chunks en caché: copiar a textura instantáneamente a coste 0 CPU
        for (int gz = startGz; gz <= endGz; gz++) {
            int cz = minChunkZ + gz;
            int basePz = gz * 16;

            for (int gx = startGx; gx <= endGx; gx++) {
                if (this.chunkRendered[gz][gx] && !needsReset) {
                    continue;
                }

                int cx = minChunkX + gx;
                String key = getChunkCacheKey(level, currentDim, cx, cz);
                int[] cached = CHUNK_SURFACE_CACHE.get(key);
                if (cached == null) {
                    cached = WorldMapChunkCache.getChunkPixels(currentDim + (forceCaveMode ? "#cave" : ""), cx, cz);
                    if (cached != null) {
                        CHUNK_SURFACE_CACHE.put(key, cached);
                    }
                }

                boolean isLoaded = level.getChunkSource().hasChunk(cx, cz);
                long lastSampleTime = CHUNK_CACHE_TIME.getOrDefault(key, 0L);
                boolean needsLiveRefresh = isLoaded && (now - lastSampleTime > 2000L);

                if (cached != null && !needsLiveRefresh) {
                    int basePx = gx * 16;
                    for (int lz = 0; lz < 16; lz++) {
                        int rowOffset = lz * 16;
                        for (int lx = 0; lx < 16; lx++) {
                            int rawPix = cached[rowOffset + lx];
                            int finalPix = forceCaveMode ? rawPix : applyLightingToPixel(rawPix, dayNightFactor);
                            img.setPixelRGBA(basePx + lx, basePz + lz, finalPix);
                        }
                    }
                    this.chunkRendered[gz][gx] = true;
                    anyUpdated = true;
                } else {
                    // Si ya tenemos datos previos en caché, dibujarlos de inmediato para evitar huecos negros mientras se actualiza en vivo
                    if (cached != null && !this.chunkRendered[gz][gx]) {
                        int basePx = gx * 16;
                        for (int lz = 0; lz < 16; lz++) {
                            int rowOffset = lz * 16;
                            for (int lx = 0; lx < 16; lx++) {
                                int rawPix = cached[rowOffset + lx];
                                int finalPix = forceCaveMode ? rawPix : applyLightingToPixel(rawPix, dayNightFactor);
                                img.setPixelRGBA(basePx + lx, basePz + lz, finalPix);
                            }
                        }
                        anyUpdated = true;
                    }
                    pendingUncached.add(new int[]{gx, gz, cx, cz});
                }
            }
        }

        // 2. Chunks nuevos o con cambios en vivo: muestrear hasta 24 por frame ordenados por cercanía a la cámara (0 lag)
        if (!pendingUncached.isEmpty()) {
            pendingUncached.sort((a, b) -> {
                int d1 = (a[2] - curCamChunkX) * (a[2] - curCamChunkX) + (a[3] - curCamChunkZ) * (a[3] - curCamChunkZ);
                int d2 = (b[2] - curCamChunkX) * (b[2] - curCamChunkX) + (b[3] - curCamChunkZ) * (b[3] - curCamChunkZ);
                return Integer.compare(d1, d2);
            });

            int limit = Math.min(24, pendingUncached.size());
            for (int i = 0; i < limit; i++) {
                int[] p = pendingUncached.get(i);
                int gx = p[0];
                int gz = p[1];
                int cx = p[2];
                int cz = p[3];
                int basePx = gx * 16;
                int basePz = gz * 16;

                int[] chunkPixels = getOrSampleChunk(level, currentDim, cx, cz, now, true);
                if (chunkPixels != null) {
                    for (int lz = 0; lz < 16; lz++) {
                        int rowOffset = lz * 16;
                        for (int lx = 0; lx < 16; lx++) {
                            int rawPix = chunkPixels[rowOffset + lx];
                            int finalPix = forceCaveMode ? rawPix : applyLightingToPixel(rawPix, dayNightFactor);
                            img.setPixelRGBA(basePx + lx, basePz + lz, finalPix);
                        }
                    }
                    this.chunkRendered[gz][gx] = true;
                    anyUpdated = true;
                } else if (!this.chunkRendered[gz][gx]) {
                    // Chunks fuera de mundo o no explorados: Cuadrícula táctica estilizada y elegante
                    for (int lz = 0; lz < 16; lz++) {
                        for (int lx = 0; lx < 16; lx++) {
                            int col;
                            if (lx == 0 || lz == 0) {
                                col = 0xFF141F2B;
                            } else if (lx == 8 && lz == 8) {
                                col = 0xFF1C2B3C;
                            } else {
                                col = ((lx + lz) & 4) == 0 ? 0xFF0A0F16 : 0xFF080D13;
                            }
                            img.setPixelRGBA(basePx + lx, basePz + lz, col);
                        }
                    }
                    this.chunkRendered[gz][gx] = true;
                    anyUpdated = true;
                }
            }
        }

        if (anyUpdated) {
            this.mapTexture.upload();
            this.mapTexture.setFilter(false, false);
        }
    }

    private int[] getOrSampleChunk(ClientLevel level, String dim, int cx, int cz, long now, boolean isVisible) {
        String key = getChunkCacheKey(level, dim, cx, cz);
        int[] cached = CHUNK_SURFACE_CACHE.get(key);
        if (cached == null) {
            cached = WorldMapChunkCache.getChunkPixels(dim + (forceCaveMode ? "#cave" : ""), cx, cz);
            if (cached != null) {
                CHUNK_SURFACE_CACHE.put(key, cached);
            }
        }
        long lastTime = CHUNK_CACHE_TIME.getOrDefault(key, 0L);

        // Si ya está en la caché en memoria, conservarlo si aún está vigente y no ha expirado
        if (cached != null) {
            if (!level.getChunkSource().hasChunk(cx, cz)) {
                return cached;
            }
            if ((now - lastTime) < 2000L) {
                return cached;
            }
        }

        if (!isVisible) {
            return cached;
        }

        LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
        if (chunk == null || chunk.isEmpty()) {
            return cached;
        }

        int[] pixels = cached != null ? cached : new int[256];
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        int minBuildY = level.getMinBuildHeight();
        int startX = cx << 4;
        int startZ = cz << 4;

        if (forceCaveMode) {
            int scanY = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getBlockY() : 64;
            if (Minecraft.getInstance().player != null && level.canSeeSky(Minecraft.getInstance().player.blockPosition())) {
                scanY = Math.min(scanY - 16, 32);
            }
            for (int lz = 0; lz < 16; lz++) {
                for (int lx = 0; lx < 16; lx++) {
                    int wx = startX + lx;
                    int wz = startZ + lz;
                    pixels[lz * 16 + lx] = sampleCavePixel(level, wx, scanY, wz, minBuildY, mPos);
                }
            }
        } else {
            float dayNightFactor = 1.0F;
            float sunFactor = 1.0F;

            int[][] heights = new int[18][18];
            for (int lz = -1; lz < 16; lz++) {
                for (int lx = -1; lx < 16; lx++) {
                    heights[lz + 1][lx + 1] = getColSurfaceY(level, chunk, startX + lx, startZ + lz, lx, lz, minBuildY, mPos);
                }
            }

            for (int lz = 0; lz < 16; lz++) {
                for (int lx = 0; lx < 16; lx++) {
                    int wx = startX + lx;
                    int wz = startZ + lz;
                    int surfaceY = heights[lz + 1][lx + 1];
                    mPos.set(wx, surfaceY, wz);
                    BlockState state = level.getBlockState(mPos);

                    int rgb;
                    boolean isWater = !state.getFluidState().isEmpty() && state.getFluidState().is(FluidTags.WATER);
                    if (isWater) {
                        int biomeWater = BiomeColors.getAverageWaterColor(level, mPos);

                        // Batimetría natural estilo JourneyMap (SurfaceRenderer):
                        // Muestreo del lecho marino/fluvial para lograr transparencia realista
                        int seabedY = surfaceY - 1;
                        while (seabedY > minBuildY && (surfaceY - seabedY) < 14) {
                            mPos.set(wx, seabedY, wz);
                            if (!level.getFluidState(mPos).is(FluidTags.WATER)) break;
                            seabedY--;
                        }
                        mPos.set(wx, seabedY, wz);
                        BlockState seabedState = level.getBlockState(mPos);
                        int seabedRgb = getBlockRgb(level, seabedState, mPos);

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
                        rgb = getBlockRgb(level, state, mPos);
                    }

                    // Sombreado de pendientes Noroeste estilo JourneyMap (BaseRenderer + RGB.bevelSlope)
                    float slope = 1.0F;
                    if (!isWater) {
                        int yNW = heights[lz][lx];
                        int yN = heights[lz][lx + 1];
                        int yW = heights[lz + 1][lx];
                        float diffN = surfaceY - yN;
                        float diffW = surfaceY - yW;
                        float diffNW = surfaceY - yNW;
                        float avgDiff = (diffN + diffW + diffNW) / 3.0F;

                        if (avgDiff > 0.0F) {
                            // Relieve orientado al sol del Noroeste (resalte suave máx +18%)
                            slope = 1.0F + Math.min(0.18F, avgDiff * 0.05F);
                        } else if (avgDiff < 0.0F) {
                            // Ladera opuesta en sombra ambiental suave (máx -22%)
                            slope = 1.0F + Math.max(-0.22F, avgDiff * 0.06F);
                        }
                    }

                    // Aplicar bisel de pendiente auténtico JourneyMap (RGB.bevelSlope)
                    float shadowFactor = (slope < 1.0F) ? 0.88F : 1.0F;
                    float rShaded = ((rgb >> 16) & 0xFF) * shadowFactor * slope;
                    float gShaded = ((rgb >> 8) & 0xFF) * shadowFactor * slope;
                    float bShaded = (rgb & 0xFF) * slope; // Canal azul más alto en sombra

                    int r = Mth.clamp((int) rShaded, 0, 255);
                    int g = Mth.clamp((int) gShaded, 0, 255);
                    int b = Mth.clamp((int) bShaded, 0, 255);

                    pixels[lz * 16 + lx] = (0xFF << 24) | (b << 16) | (g << 8) | r;
                }
            }
        }

        CHUNK_SURFACE_CACHE.put(key, pixels);
        CHUNK_CACHE_TIME.put(key, now);
        WorldMapChunkCache.putChunkPixels(dim + (forceCaveMode ? "#cave" : ""), cx, cz, pixels);
        return pixels;
    }

    private int sampleCavePixel(ClientLevel level, int wx, int py, int wz, int minBuildY, BlockPos.MutableBlockPos mPos) {
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
                int baseCol = getBlockRgb(level, wallState, mPos);
                float wallShade = 0.85F;
                if (northAir || westAir) wallShade += 0.30F; // Resalte Noroeste nítido (JourneyMap style)
                if (southAir || eastAir) wallShade -= 0.25F; // Sombra Sureste

                int r = Mth.clamp((int) (((baseCol >> 16) & 0xFF) * wallShade), 0, 255);
                int g = Mth.clamp((int) (((baseCol >> 8) & 0xFF) * wallShade), 0, 255);
                int b = Mth.clamp((int) ((baseCol & 0xFF) * wallShade), 0, 255);
                return (0xFF << 24) | (b << 16) | (g << 8) | r;
            } else {
                return 0xFF0D1219; // Manto rocoso profundo no expuesto
            }
        }

        // 3. Encontramos espacio abierto: escanear hacia abajo desde airY para hallar el suelo
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
            int col = getBlockRgb(level, floorState, mPos);
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

            if (blockLight > 0) {
                float warmRatio = blockLight / 15.0F;
                r = Mth.clamp((int) (r * (1.0F + warmRatio * 0.35F)), 0, 255);
                g = Mth.clamp((int) (g * (1.0F + warmRatio * 0.18F)), 0, 255);
            }
        }

        return (0xFF << 24) | (b << 16) | (g << 8) | r;
    }

    private int getColSurfaceY(ClientLevel level, LevelChunk centerChunk, int wx, int wz, int lx, int lz, int minBuildY, BlockPos.MutableBlockPos mPos) {
        int y;
        if (lx >= 0 && lx < 16 && lz >= 0 && lz < 16) {
            y = centerChunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz);
        } else {
            LevelChunk neighbor = level.getChunkSource().getChunk(wx >> 4, wz >> 4, ChunkStatus.FULL, false);
            y = neighbor != null && !neighbor.isEmpty() ? neighbor.getHeight(Heightmap.Types.WORLD_SURFACE, wx & 15, wz & 15) : level.getSeaLevel();
        }

        int steps = 0;
        while (y > minBuildY && steps < 30) {
            mPos.set(wx, y, wz);
            BlockState st = level.getBlockState(mPos);
            if (!st.isAir()) {
                if (!st.getFluidState().isEmpty() || st.getMapColor(level, mPos) != MapColor.NONE) return y;
            }
            y--;
            steps++;
        }
        return Math.max(minBuildY, y);
    }

    private int getBlockRgb(ClientLevel level, BlockState state, BlockPos pos) {
        return HunterMinimapRenderer.getBlockBaseRgb(level, state, pos);
    }

    private void renderWaypoints(GuiGraphics g, String currentDim, double scX, double scY, double ppb) {
        List<Waypoint> waypoints = WaypointManager.getWaypointsForDimension(currentDim);
        Minecraft mc = Minecraft.getInstance();
        double px = mc.player != null ? mc.player.getX() : cameraWorldX;
        double pz = mc.player != null ? mc.player.getZ() : cameraWorldZ;

        for (Waypoint wp : waypoints) {
            if (!wp.isEnabled()) continue;

            int wx = (int) Math.round(scX + (wp.getX() - cameraWorldX) * ppb);
            int wz = (int) Math.round(scY + (wp.getZ() - cameraWorldZ) * ppb);

            if (wx >= -20 && wx <= this.width + 20 && wz >= -20 && wz <= this.height + 20) {
                int col = wp.getColor() | 0xFF000000;
                if (wp.isDeathPoint()) {
                    g.fill(wx - 6, wz - 6, wx + 7, wz + 7, 0x88FF0033);
                    g.fill(wx - 5, wz - 5, wx + 6, wz + 6, 0xEE330008);
                    g.fill(wx - 4, wz - 4, wx + 5, wz + 5, 0xFFFF2244);
                    g.drawCenteredString(font, "☠", wx, wz - 4, 0xFFFFFFFF);
                } else {
                    g.fill(wx - 4, wz - 4, wx + 5, wz + 5, 0xEE08111A);
                    g.fill(wx - 3, wz - 3, wx + 4, wz + 4, col);
                    g.fill(wx - 1, wz - 1, wx + 2, wz + 2, 0xFFFFFFFF);
                }

                // Badge holográfico con Nombre y Distancia estilo JourneyMap
                double dist = Math.sqrt((wp.getX() - px) * (wp.getX() - px) + (wp.getZ() - pz) * (wp.getZ() - pz));
                String label = wp.getName() + " (" + (int) Math.round(dist) + "m)";
                int textW = font.width(label);
                int badgeW = textW + 8;
                int badgeH = 12;
                int badgeX = wx - badgeW / 2;
                int badgeY = wz - 16;

                g.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, wp.isDeathPoint() ? 0xDD330008 : 0xDD08111A);
                g.renderOutline(badgeX, badgeY, badgeW, badgeH, col);
                g.drawString(font, label, badgeX + 4, badgeY + 2, wp.isDeathPoint() ? 0xFFFF6677 : 0xFFFFFFFF, false);
            }
        }
    }

    private void renderItemDropsOnMap(GuiGraphics g, Minecraft mc, double scX, double scY, double ppb, int mouseX, int mouseY) {
        if (mc.player == null || mc.level == null) return;

        double minX = cameraWorldX - (scX / ppb);
        double maxX = cameraWorldX + (scX / ppb);
        double minZ = cameraWorldZ - (scY / ppb);
        double maxZ = cameraWorldZ + (scY / ppb);

        AABB box = new AABB(minX - 16, -64, minZ - 16, maxX + 16, 320, maxZ + 16);
        List<net.minecraft.world.entity.item.ItemEntity> items = mc.level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box, net.minecraft.world.entity.item.ItemEntity::isAlive);
        if (items.isEmpty()) return;

        float gameTime = mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(true);
        float pulse = (float) (Math.sin(gameTime * 0.35F) * 0.5F + 0.5F);

        int count = 0;
        for (net.minecraft.world.entity.item.ItemEntity item : items) {
            if (count >= 120) break;
            net.minecraft.world.item.ItemStack stack = item.getItem();
            if (stack.isEmpty()) continue;

            int ix = (int) Math.round(scX + (item.getX() - cameraWorldX) * ppb);
            int iy = (int) Math.round(scY + (item.getZ() - cameraWorldZ) * ppb);

            if (ix < -10 || ix > this.width + 10 || iy < -10 || iy > this.height + 10) continue;

            int itemColor = HunterMinimapRenderer.getItemTacticalColor(stack);
            boolean isHighTier = HunterMinimapRenderer.isHighTierDrop(stack);

            if (isHighTier) {
                int glowAlpha = (int) (140 + 115 * pulse);
                int glowCol = (glowAlpha << 24) | (itemColor & 0x00FFFFFF);
                g.fill(ix - 3, iy, ix + 4, iy + 1, glowCol);
                g.fill(ix, iy - 3, ix + 1, iy + 4, glowCol);
            }

            g.fill(ix - 1, iy - 2, ix + 2, iy + 3, 0xEE08111A);
            g.fill(ix - 2, iy - 1, ix + 3, iy + 2, 0xEE08111A);

            g.fill(ix - 1, iy, ix + 2, iy + 1, itemColor);
            g.fill(ix, iy - 1, ix + 1, iy + 2, itemColor);
            g.fill(ix, iy, ix + 1, iy + 1, 0xFFFFFFFF);

            if (Math.abs(mouseX - ix) <= 6 && Math.abs(mouseY - iy) <= 6) {
                this.hoveredItemDrop = item;
            }

            count++;
        }
    }

    private void renderItemDropHoverTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (hoveredItemDrop == null || !hoveredItemDrop.isAlive()) return;
        Minecraft mc = Minecraft.getInstance();
        net.minecraft.world.item.ItemStack stack = hoveredItemDrop.getItem();
        if (stack.isEmpty()) return;

        double px = mc.player != null ? mc.player.getX() : cameraWorldX;
        double py = mc.player != null ? mc.player.getY() : 64.0D;
        double pz = mc.player != null ? mc.player.getZ() : cameraWorldZ;
        double dist = Math.sqrt(hoveredItemDrop.distanceToSqr(px, py, pz));

        int itemColor = HunterMinimapRenderer.getItemTacticalColor(stack);
        boolean isHighTier = HunterMinimapRenderer.isHighTierDrop(stack);

        String category;
        if (isHighTier) {
            category = "§6[★ ARTIFACT]";
        } else if ((itemColor & 0x00FFFFFF) == 0x00E5FF) {
            category = "§b[✦ MANA/ESSENCE]";
        } else if ((itemColor & 0x00FFFFFF) == 0xA855F7) {
            category = "§d[☠ SHADOW/MONARCH]";
        } else {
            category = "§7[ITEM]";
        }

        String itemName = stack.getHoverName().getString() + " x" + stack.getCount();
        String posStr = String.format("📍 %dm [X: %d, Y: %d, Z: %d]", (int) Math.round(dist), hoveredItemDrop.getBlockX(), hoveredItemDrop.getBlockY(), hoveredItemDrop.getBlockZ());

        int pad = 6;
        int lineH = 10;
        int maxTextW = Math.max(font.width(category + " " + itemName), font.width(posStr));
        int boxW = maxTextW + pad * 2;
        int boxH = pad * 2 + lineH * 2 + 2;

        int boxX = mouseX + 12;
        int boxY = mouseY - boxH - 4;
        if (boxX + boxW > this.width - 6) boxX = mouseX - boxW - 8;
        if (boxY < 6) boxY = mouseY + 14;

        g.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xF008111A);
        g.renderOutline(boxX, boxY, boxW, boxH, itemColor);

        int textY = boxY + pad;
        g.drawString(font, category + " §f" + itemName, boxX + pad, textY, 0xFFFFFFFF, false);
        textY += lineH + 2;
        g.drawString(font, "§7" + posStr, boxX + pad, textY, 0xFFA0AEC0, false);
    }

    private void renderActiveRiftsOnMap(GuiGraphics g, Minecraft mc, double scX, double scY, double ppb, int mouseX, int mouseY) {
        if (mc.level == null) return;
        if (AwakeningSystemClient.ACTIVE_RIFTS.isEmpty()) return;

        float gameTime = mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(true);
        float pulse = (float) (Math.sin(gameTime * 0.25F) * 0.5F + 0.5F);

        for (com.soybe.awakeningsystem.SyncRiftsPayload.RiftEntry rift : AwakeningSystemClient.ACTIVE_RIFTS) {
            if (rift.pos() == null) continue;

            double screenX = scX + (rift.pos().getX() + 0.5D - cameraWorldX) * ppb;
            double screenY = scY + (rift.pos().getZ() + 0.5D - cameraWorldZ) * ppb;

            if (screenX < -20 || screenX > this.width + 20 || screenY < 20 || screenY > this.height + 20) {
                continue;
            }

            int ix = (int) Math.round(screenX);
            int iy = (int) Math.round(screenY);

            int themeColor = AwakeningSystemClient.getGateColor(rift);
            boolean isHovered = mouseX >= ix - 10 && mouseX <= ix + 10 && mouseY >= iy - 10 && mouseY <= iy + 10;
            if (isHovered) {
                this.hoveredGate = rift;
            }

            // Halo pulsante exterior
            int alpha = (int) (140 + 115 * pulse);
            int glowCol = (alpha << 24) | (themeColor & 0x00FFFFFF);
            g.fill(ix - 8, iy - 2, ix + 9, iy + 3, glowCol);
            g.fill(ix - 2, iy - 8, ix + 3, iy + 9, glowCol);

            // Marco de Obsidiana con contorno de dimensión y aura cromática de rango
            int boxSize = 14;
            int halfBox = boxSize / 2;
            g.fill(ix - halfBox, iy - halfBox, ix + halfBox + 1, iy + halfBox + 1, 0xF5080E17);
            g.renderOutline(ix - halfBox, iy - halfBox, boxSize + 1, boxSize + 1, isHovered ? 0xFFFFFFFF : themeColor);

            // Icono de Portal Dimensional (vórtice de energía con color de rango y horizonte de sucesos)
            g.fill(ix - 4, iy - 2, ix + 5, iy + 3, themeColor);
            g.fill(ix - 2, iy - 4, ix + 3, iy + 5, themeColor);
            g.fill(ix - 1, iy - 1, ix + 2, iy + 2, 0xFF05070B);

            // Indicador de elevación vertical
            if (mc.player != null) {
                int dy = rift.pos().getY() - mc.player.getBlockY();
                if (dy >= 4) {
                    g.drawCenteredString(font, "§a▲", ix, iy - halfBox - 8, 0xFF50FA7B);
                } else if (dy <= -4) {
                    g.drawCenteredString(font, "§c▼", ix, iy + halfBox + 1, 0xFFFF4444);
                }
            }
        }
    }

    private void renderGateHoverTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (hoveredGate == null) return;
        Minecraft mc = Minecraft.getInstance();

        // 1. Título y Rango
        String titleStr;
        int themeColor = AwakeningSystemClient.getGateColor(hoveredGate);

        if (hoveredGate.isRedGate()) {
            titleStr = "§c§l☠ " + Component.translatable("awakening_system.map.tooltip.gate_red").getString();
        } else if (hoveredGate.type() == 1) {
            titleStr = "§a§l🚪 " + Component.translatable("awakening_system.map.tooltip.gate_exit").getString();
        } else if (hoveredGate.type() == 98 || "MURIM".equalsIgnoreCase(hoveredGate.rank())) {
            titleStr = "§6§l☯ " + Component.translatable("awakening_system.map.tooltip.gate_dim").getString() + ": MURIM";
        } else if (hoveredGate.type() == 99 || "ISEKAI".equalsIgnoreCase(hoveredGate.rank())) {
            titleStr = "§b§l🌌 " + Component.translatable("awakening_system.map.tooltip.gate_dim").getString() + ": ISEKAI";
        } else if (hoveredGate.type() == 97) {
            titleStr = "§9§l✨ " + Component.translatable("awakening_system.map.tooltip.gate_dim").getString() + ": ABYSS";
        } else if (hoveredGate.type() == 96) {
            titleStr = "§c§l🔒 " + Component.translatable("awakening_system.map.tooltip.gate_dim").getString() + ": ABYSS (SEALED)";
        } else {
            titleStr = "§b§l🌀 GATE RANGO " + hoveredGate.rank();
        }

        // 2. Subtítulo y Estado de Dungeon Break
        String subTitle;
        if (hoveredGate.type() == 2) {
            subTitle = "§e✦ " + Component.translatable("awakening_system.map.tooltip.gate_perm").getString();
        } else if (hoveredGate.type() == 1) {
            subTitle = "§a✔ Salida Segura a Superficie";
        } else {
            int remSecs = AwakeningSystemClient.getRiftRemainingSeconds(hoveredGate);
            if (remSecs > 0) {
                String timeStr = AwakeningSystemClient.formatRiftRemainingTime(remSecs);
                subTitle = (remSecs <= 60 ? "§c⏱ ¡Dungeon Break Inminente! (" + timeStr + ")" : String.format(Component.translatable("awakening_system.map.tooltip.dungeon_break_in").getString(), timeStr));
            } else if (remSecs == 0) {
                subTitle = "§4⚠ " + Component.translatable("awakening_system.map.tooltip.dungeon_break_ongoing").getString();
            } else {
                subTitle = "§7Puerta Dimensional de Mazmorra";
            }
        }

        // 3. Ubicación Táctica y Coordenadas
        double dist = mc.player != null ? Math.sqrt(mc.player.distanceToSqr(hoveredGate.pos().getX() + 0.5D, hoveredGate.pos().getY(), hoveredGate.pos().getZ() + 0.5D)) : 0.0D;
        int dy = mc.player != null ? (hoveredGate.pos().getY() - mc.player.getBlockY()) : 0;
        String elevStr;
        if (dy >= 3) {
            elevStr = "▲ +" + dy + "m";
        } else if (dy <= -3) {
            elevStr = "▼ " + dy + "m";
        } else {
            elevStr = "=" + Component.translatable("awakening_system.map.tooltip.same_level").getString();
        }
        String posStr = String.format("📍 %dm (%s)  [X: %d, Y: %d, Z: %d]",
                (int) Math.round(dist),
                elevStr,
                hoveredGate.pos().getX(),
                hoveredGate.pos().getY(),
                hoveredGate.pos().getZ());

        // 4. Advertencia de Dificultad
        String warningStr;
        if (hoveredGate.isRedGate()) {
            warningStr = "§4⚠ Calabozo sellado de alta letalidad. Jefe Monstruoso.";
        } else if (hoveredGate.rank().contains("S") || hoveredGate.rank().contains("GOD") || hoveredGate.rank().contains("MONARCH")) {
            warningStr = "§6⚠ Peligro Extremo: Se recomienda grupo de cazadores S.";
        } else if (hoveredGate.rank().contains("A")) {
            warningStr = "§e⚠ Dificultad Alta: Precaución y equipo preparado.";
        } else if (hoveredGate.type() == 1) {
            warningStr = "§a✔ Retorno inmediato a la dimensión de origen.";
        } else {
            warningStr = "§bℹ Mazmorra estándar para obtención de núcleos.";
        }

        // 5. Dimensiones de la Tarjeta Táctica
        int titleW = font.width(titleStr) + 16;
        int subW = font.width(subTitle) + 16;
        int posW = font.width(posStr) + 16;
        int warnW = font.width(warningStr) + 16;

        int cardW = Math.max(215, Math.max(Math.max(titleW, subW), Math.max(posW, warnW)) + 14);
        int cardH = 60;

        int tx = mouseX + 14;
        int ty = mouseY - 14;
        if (tx + cardW > this.width) tx = mouseX - cardW - 8;
        if (ty + cardH > this.height) ty = this.height - cardH - 6;
        if (ty < 6) ty = 6;
        if (tx < 6) tx = 6;

        // Fondo Holográfico Translúcido
        g.fill(tx, ty, tx + cardW, ty + cardH, 0xF2070D14);
        g.renderOutline(tx, ty, cardW, cardH, 0xFF1C2733);
        g.renderOutline(tx + 1, ty + 1, cardW - 2, cardH - 2, (themeColor & 0x00FFFFFF) | 0x88000000);

        // Remaches / Brackets de Esquina en Oro
        int gold = 0xFFFFD700;
        g.fill(tx, ty, tx + 4, ty + 1, gold);
        g.fill(tx, ty, tx + 1, ty + 4, gold);
        g.fill(tx + cardW - 4, ty, tx + cardW, ty + 1, gold);
        g.fill(tx + cardW - 1, ty, tx + cardW, ty + 4, gold);
        g.fill(tx, ty + cardH - 1, tx + 4, ty + cardH, gold);
        g.fill(tx, ty + cardH - 4, tx + 1, ty + cardH, gold);
        g.fill(tx + cardW - 4, ty + cardH - 1, tx + cardW, ty + cardH, gold);
        g.fill(tx + cardW - 1, ty + cardH - 4, tx + cardW, ty + cardH, gold);

        // Textos con drop shadow
        g.drawString(font, titleStr, tx + 8, ty + 6, 0xFFFFFFFF, true);
        g.drawString(font, subTitle, tx + 8, ty + 18, 0xFFFFE066, true);
        g.drawString(font, posStr, tx + 8, ty + 31, 0xFF70E8FF, true);
        g.drawString(font, warningStr, tx + 8, ty + 44, 0xFFAAAAAA, true);
    }

    private void renderEntitiesOnMap(GuiGraphics g, Minecraft mc, double scX, double scY, double ppb, int mouseX, int mouseY) {
        if (mc.player == null || mc.level == null) return;
        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        // Calcular el rango visible en el mundo según el zoom y la posición de la cámara
        double halfVisW = (this.width / Math.max(0.1D, ppb)) * 0.5D + 16.0D;
        double halfVisH = (this.height / Math.max(0.1D, ppb)) * 0.5D + 16.0D;
        double minX = cameraWorldX - halfVisW;
        double maxX = cameraWorldX + halfVisW;
        double minZ = cameraWorldZ - halfVisH;
        double maxZ = cameraWorldZ + halfVisH;

        double minY = forceCaveMode ? (py - 12.0D) : (py - 16.0D);
        double maxY = forceCaveMode ? (py + 12.0D) : (py + 24.0D);

        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        List<Entity> rawEntities = mc.level.getEntities((Entity) null, box, ent -> ent != mc.player && ent.isAlive() && ent instanceof LivingEntity);
        if (rawEntities.isEmpty()) return;

        List<LivingEntity> filteredEntities = new java.util.ArrayList<>();
        List<LivingEntity> acceptedAnimals = new java.util.ArrayList<>();
        int caveScanY = forceCaveMode ? (int) Math.round(py) : 64;

        for (Entity raw : rawEntities) {
            LivingEntity living = (LivingEntity) raw;
            int lx = living.getBlockX();
            int lz = living.getBlockZ();
            double ly = living.getY();

            if (forceCaveMode) {
                // Modo cueva: culling vertical estricto a ±8.5 bloques del corte de cueva
                if (Math.abs(ly - caveScanY) > 8.5D) continue;
            } else {
                // Modo superficie: descartar cualquier criatura que esté en cuevas/minas bajo tierra
                int surfaceY = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, lx, lz);
                if (ly < surfaceY - 5.0D) {
                    continue; // Criatura en cueva subterránea
                }
                // Si la elevación vertical respecto al jugador es excesiva
                if (Math.abs(ly - py) > 16.0D) {
                    continue;
                }
            }

            int priority = getEntityPriority(living);
            if (priority <= 1) { // Criaturas pasivas / ganado
                // Culling por proximidad: máx 2 animales por radio de 7 bloques
                int nearbyCount = 0;
                for (LivingEntity otherAnimal : acceptedAnimals) {
                    double distSq = (otherAnimal.getX() - living.getX()) * (otherAnimal.getX() - living.getX())
                                  + (otherAnimal.getZ() - living.getZ()) * (otherAnimal.getZ() - living.getZ());
                    if (distSq < 49.0D) {
                        nearbyCount++;
                        if (nearbyCount >= 2) break;
                    }
                }
                if (nearbyCount >= 2) {
                    continue; // Evitar que apiñamientos de vacas/ovejas saturen el mapa
                }
                if (acceptedAnimals.size() >= 24) {
                    continue; // Límite total de animales en pantalla
                }
                acceptedAnimals.add(living);
            }

            filteredEntities.add(living);
        }

        // Ordenar: pasivos primero, hostiles y jefes al final para que queden dibujados encima
        filteredEntities.sort((e1, e2) -> Integer.compare(getEntityPriority(e1), getEntityPriority(e2)));

        for (LivingEntity living : filteredEntities) {
            int ex = (int) Math.round(scX + (living.getX() - cameraWorldX) * ppb);
            int ez = (int) Math.round(scY + (living.getZ() - cameraWorldZ) * ppb);

            if (ex < -20 || ex > this.width + 20 || ez < -20 || ez > this.height + 20) continue;

            EntityIconRenderer.renderEntityIcon(g, living, ex, ez, 10, mc.player.getY());

            // Detectar hover del cursor sobre la entidad para mostrar tooltip informativo
            if (Math.abs(mouseX - ex) <= 8 && Math.abs(mouseY - ez) <= 8) {
                if (this.hoveredEntity == null || getEntityPriority(living) > getEntityPriority(this.hoveredEntity)) {
                    this.hoveredEntity = living;
                }
            }
        }
    }

    private void renderEntityHoverTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (hoveredEntity == null || !hoveredEntity.isAlive()) return;
        Minecraft mc = Minecraft.getInstance();
        double px = mc.player != null ? mc.player.getX() : cameraWorldX;
        double py = mc.player != null ? mc.player.getY() : 64.0D;
        double pz = mc.player != null ? mc.player.getZ() : cameraWorldZ;

        double dist = Math.sqrt(hoveredEntity.distanceToSqr(px, py, pz));
        int dy = (int) Math.round(hoveredEntity.getY() - py);

        // 1. Identificación táctica y facción
        boolean isBoss = com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(hoveredEntity);
        boolean isPlayer = hoveredEntity instanceof Player;
        boolean isHostile = hoveredEntity instanceof net.minecraft.world.entity.monster.Enemy;
        boolean isNeutral = hoveredEntity instanceof net.minecraft.world.entity.NeutralMob;

        String nameStr = hoveredEntity.getDisplayName().getString();

        String factionBadge;
        int themeColor;
        if (isBoss) {
            factionBadge = Component.translatable("awakening_system.map.tooltip.boss").getString();
            themeColor = 0xFFFF2244;
        } else if (isPlayer) {
            factionBadge = Component.translatable("awakening_system.map.tooltip.player").getString();
            themeColor = 0xFF4EE2EC;
        } else if (isHostile) {
            factionBadge = Component.translatable("awakening_system.map.tooltip.hostile").getString();
            themeColor = 0xFFFF4444;
        } else if (isNeutral) {
            factionBadge = Component.translatable("awakening_system.map.tooltip.neutral").getString();
            themeColor = 0xFFFFB86C;
        } else {
            factionBadge = Component.translatable("awakening_system.map.tooltip.passive").getString();
            themeColor = 0xFF50FA7B;
        }

        // 2. Subtítulo: Nivel, Rango y Clase / Título
        String subTitle;
        if (isPlayer && hoveredEntity == mc.player) {
            com.soybe.awakeningsystem.SystemData data = mc.player.getData(com.soybe.awakeningsystem.AwakeningSystemMod.PLAYER_SYSTEM);
            int lvl = data != null ? data.level() : mc.player.experienceLevel;
            String rank = data != null ? data.rank() : com.soybe.awakeningsystem.SystemData.rankForLevel(lvl);
            String rawTitle = (data != null && !data.equippedTitle().isEmpty()) ? data.equippedTitle() : "";
            String titleDisplay = "";
            if (!rawTitle.isEmpty() && !rawTitle.equalsIgnoreCase("none")) {
                // Títulos de rango básico (rank_f, rank_e...) no se duplican porque el rango ya está presente al lado
                if (!rawTitle.startsWith("rank_")) {
                    com.soybe.awakeningsystem.TitleDefinitions.Definition tDef = com.soybe.awakeningsystem.TitleDefinitions.get(rawTitle);
                    if (tDef != null && tDef.translationKey() != null) {
                        titleDisplay = Component.translatable(tDef.translationKey()).getString();
                    } else {
                        titleDisplay = Component.translatable("awakening_system.title." + rawTitle).getString();
                    }
                }
            }
            if (!titleDisplay.isEmpty()) {
                subTitle = String.format("⭐ %s • %s • %s",
                        Component.translatable("awakening_system.map.tooltip.level", lvl).getString(),
                        Component.translatable("awakening_system.map.tooltip.rank", rank).getString(),
                        titleDisplay);
            } else {
                subTitle = String.format("⭐ %s • %s",
                        Component.translatable("awakening_system.map.tooltip.level", lvl).getString(),
                        Component.translatable("awakening_system.map.tooltip.rank", rank).getString());
            }
        } else if (isPlayer) {
            Player otherP = (Player) hoveredEntity;
            int lvl = otherP.experienceLevel;
            String rank = com.soybe.awakeningsystem.SystemData.rankForLevel(lvl);
            subTitle = String.format("👤 %s • %s",
                    Component.translatable("awakening_system.map.tooltip.level", lvl).getString(),
                    Component.translatable("awakening_system.map.tooltip.rank", rank).getString());
        } else if (isBoss) {
            int bossLvl = hoveredEntity.getPersistentData().contains("world_boss_scaled_level")
                    ? hoveredEntity.getPersistentData().getInt("world_boss_scaled_level")
                    : Math.max(50, (int) (hoveredEntity.getMaxHealth() / 4));
            subTitle = String.format("☠ %s • %s",
                    Component.translatable("awakening_system.map.tooltip.level", bossLvl).getString(),
                    Component.translatable("awakening_system.map.tooltip.rank", "S+").getString());
        } else if (isHostile) {
            int mobLvl = Math.max(1, (int) (hoveredEntity.getMaxHealth() / 3) + hoveredEntity.getArmorValue());
            String mobRank = mobLvl >= 40 ? "A" : (mobLvl >= 25 ? "B" : (mobLvl >= 12 ? "C" : (mobLvl >= 5 ? "D" : "E")));
            subTitle = String.format("⚔ %s • %s",
                    Component.translatable("awakening_system.map.tooltip.level", mobLvl).getString(),
                    Component.translatable("awakening_system.map.tooltip.rank", mobRank).getString());
        } else {
            subTitle = "☘ " + Component.translatable("awakening_system.map.tooltip.wildlife").getString();
        }

        // 3. Salud (HP y Porcentaje)
        float hp = hoveredEntity.getHealth();
        float maxHp = hoveredEntity.getMaxHealth();
        float hpPct = Math.max(0.0F, Math.min(1.0F, hp / Math.max(1.0F, maxHp)));
        int hpPercent = Math.round(hpPct * 100);
        String hpStr = String.format("♥ %.1f / %.1f (%d%%)", hp, maxHp, hpPercent);

        // 4. Ubicación Táctica y Coordenadas
        String elevStr;
        if (dy >= 3) {
            elevStr = "▲ +" + dy + "m";
        } else if (dy <= -3) {
            elevStr = "▼ " + dy + "m";
        } else {
            elevStr = "=" + Component.translatable("awakening_system.map.tooltip.same_level").getString();
        }
        String posStr = String.format("📍 %dm (%s)  [X: %d, Y: %d, Z: %d]",
                (int) Math.round(dist),
                elevStr,
                hoveredEntity.getBlockX(),
                hoveredEntity.getBlockY(),
                hoveredEntity.getBlockZ());

        // Territorio de Gremio
        String claimInfo = null;
        if (showGuildClaims && mc.level != null) {
            Component guildTip = com.soybe.awakeningsystem.compat.XaeroGuildMapIntegration.getHighlightTooltip(mc.level.dimension(), hoveredEntity.getBlockX() >> 4, hoveredEntity.getBlockZ() >> 4);
            if (guildTip != null) {
                claimInfo = guildTip.getString();
            }
        }

        // Armadura / Defensa
        int armor = hoveredEntity.getArmorValue();

        // 5. Dimensiones de la Ficha Táctica (con espacio para Avatar/Rostro de la Entidad)
        int titleW = font.width(nameStr) + font.width(factionBadge) + 42;
        int subW = font.width(subTitle) + 42;
        int hpW = font.width(hpStr) + 20;
        int posW = font.width(posStr) + 20;
        int claimW = claimInfo != null ? font.width("🏛 " + claimInfo) + 20 : 0;

        int cardW = Math.max(205, Math.max(Math.max(titleW, subW), Math.max(Math.max(hpW, posW), claimW)) + 14);
        int cardH = (claimInfo != null || armor > 0) ? 69 : 59;

        int tx = mouseX + 14;
        int ty = mouseY - 14;
        if (tx + cardW > this.width) tx = mouseX - cardW - 8;
        if (ty + cardH > this.height) ty = this.height - cardH - 6;
        if (ty < 6) ty = 6;
        if (tx < 6) tx = 6;

        // 6. Fondo Holográfico Translúcido de Obsidiana
        g.fill(tx, ty, tx + cardW, ty + cardH, 0xF2070D14);
        g.renderOutline(tx, ty, cardW, cardH, 0xFF1C2733);
        g.renderOutline(tx + 1, ty + 1, cardW - 2, cardH - 2, (themeColor & 0x00FFFFFF) | 0x88000000);

        // Remaches / Brackets de Esquina en Oro
        int gold = 0xFFFFD700;
        g.fill(tx, ty, tx + 4, ty + 1, gold);
        g.fill(tx, ty, tx + 1, ty + 4, gold);
        g.fill(tx + cardW - 4, ty, tx + cardW, ty + 1, gold);
        g.fill(tx + cardW - 1, ty, tx + cardW, ty + 4, gold);
        g.fill(tx, ty + cardH - 1, tx + 4, ty + cardH, gold);
        g.fill(tx, ty + cardH - 4, tx + 1, ty + cardH, gold);
        g.fill(tx + cardW - 4, ty + cardH - 1, tx + cardW, ty + cardH, gold);
        g.fill(tx + cardW - 1, ty + cardH - 4, tx + cardW, ty + cardH, gold);

        // 7. Avatar / Rostro frontal de la entidad
        int faceSize = 18;
        int faceX = tx + 7;
        int faceY = ty + 6;
        g.fill(faceX - 1, faceY - 1, faceX + faceSize + 1, faceY + faceSize + 1, 0xFF0D1520);
        g.renderOutline(faceX - 1, faceY - 1, faceSize + 2, faceSize + 2, (themeColor & 0x00FFFFFF) | 0xDD000000);
        EntityIconRenderer.renderEntityFaceOnly(g, hoveredEntity, faceX, faceY, faceSize);

        // 8. Encabezado: Nombre y Badge de Facción (desplazado a la derecha del Avatar)
        int textX = tx + 30;
        g.drawString(font, nameStr, textX, ty + 6, 0xFFFFFFFF, true);
        int badgeX = tx + cardW - font.width(factionBadge) - 8;
        g.drawString(font, factionBadge, badgeX, ty + 6, themeColor, true);

        // Subtítulo (Nivel / Rango / Clase) con drop shadow
        g.drawString(font, subTitle, textX, ty + 17, 0xFFFFE066, true);

        // Línea divisoria fina
        g.fill(tx + 7, ty + 28, tx + cardW - 7, ty + 29, 0x444EE2EC);

        // 9. Barra de Salud con Trough Metálico
        int barX = tx + 7;
        int barY = ty + 32;
        int barW = cardW - 14;
        int barH = 7;

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF15181F);
        g.renderOutline(barX - 1, barY - 1, barW + 2, barH + 2, 0xFF2A3440);

        int fillW = (int) Math.round(barW * hpPct);
        if (fillW > 0) {
            int hpColor;
            if (isBoss) {
                hpColor = 0xFFFF2244;
            } else if (hpPct > 0.5F) {
                hpColor = isHostile ? 0xFFFF4444 : 0xFF2ECC71;
            } else if (hpPct > 0.2F) {
                hpColor = 0xFFFFB86C;
            } else {
                boolean flash = (System.currentTimeMillis() / 250L) % 2 == 0;
                hpColor = flash ? 0xFFFF1111 : 0xFF880000;
            }
            g.fill(barX, barY, barX + fillW, barY + barH, hpColor);
            g.fill(barX, barY, barX + fillW, barY + 2, (hpColor & 0x00FFFFFF) | 0xAAFFFFFF);
        }

        // Texto centrado en la barra
        g.drawCenteredString(font, hpStr, tx + cardW / 2, barY - 1, 0xFFFFFFFF);

        // 10. Pie: Ubicación Táctica y Coordenadas
        int footerY = ty + 43;
        g.drawString(font, posStr, tx + 7, footerY, 0xFF8BE9FD, true);

        // 11. Extra: Armadura o Territorio de Gremio
        if (claimInfo != null) {
            g.drawString(font, "§d🏛 " + claimInfo, tx + 7, footerY + 11, 0xFFFF79C6, true);
        } else if (armor > 0) {
            g.drawString(font, String.format("§b🛡 %s", Component.translatable("awakening_system.map.tooltip.armor", armor).getString()), tx + 7, footerY + 11, 0xFF5DF5F8, true);
        }
    }

    private static int getEntityPriority(LivingEntity e) {
        if (com.soybe.awakeningsystem.config.BossCompatibilityManager.isBoss(e)) return 5;
        if (e instanceof Player) return 4;
        if (e instanceof net.minecraft.world.entity.monster.Enemy) return 3;
        if (e instanceof net.minecraft.world.entity.NeutralMob) return 2;
        return 1;
    }

    private void renderPlayerOnMap(GuiGraphics g, Player player, double scX, double scY, double ppb, int mouseX, int mouseY) {
        if (player == null) return;
        int px = (int) Math.round(scX + (player.getX() - cameraWorldX) * ppb);
        int pz = (int) Math.round(scY + (player.getZ() - cameraWorldZ) * ppb);
        EntityIconRenderer.drawPlayerMarker(g, player, px, pz, player.getYRot(), 12);

        if (Math.abs(mouseX - px) <= 8 && Math.abs(mouseY - pz) <= 8) {
            if (this.hoveredEntity == null) {
                this.hoveredEntity = player;
            }
        }
    }

    private void renderTopBar(GuiGraphics g, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();

        // 1. Banner Informativo Superior Central (Coordenadas y Bioma estilo JourneyMap)
        int infoX = isBlockHovered ? hoveredBlockX : (mc.player != null ? mc.player.getBlockX() : 0);
        int infoZ = isBlockHovered ? hoveredBlockZ : (mc.player != null ? mc.player.getBlockZ() : 0);
        int infoY = (mc.level != null && mc.player != null) ? getSurfaceYAt(mc.level, infoX, infoZ) : 64;
        String curBiome = (mc.level != null) ? formatBiome(mc.level.getBiome(new BlockPos(infoX, infoY, infoZ))) : Component.translatable("awakening_system.minimap.wilderness").getString();

        String topCoordText = String.format("X: %d  Y: %d  Z: %d", infoX, infoY, infoZ);
        int bannerW = Math.min(Math.max(font.width(topCoordText), font.width(curBiome)) + 24, this.width - 48);
        int bannerH = 22;
        int bannerX = (this.width - bannerW) / 2;
        int bannerY = 2;

        g.fill(bannerX, bannerY, bannerX + bannerW, bannerY + bannerH, 0xD008111A);
        g.renderOutline(bannerX, bannerY, bannerW, bannerH, 0x554EE2EC);
        g.drawCenteredString(font, topCoordText, this.width / 2, bannerY + 2, 0xFFFFFFFF);
        g.drawCenteredString(font, "§e" + curBiome, this.width / 2, bannerY + 12, 0xFFFFE066);

        // 2. Barra de Botones Tipo Pastilla Flotante (JourneyMap Pill Toolbar)
        pillButtons.clear();
        int pillY = 26;
        int pillH = 20;

        Component caveText = Component.translatable("awakening_system.map.pill.cave");
        Component lightText = Component.translatable("awakening_system.map.pill.light");
        Component gridText = Component.translatable("awakening_system.map.pill.grid");
        Component radarText = Component.translatable("awakening_system.map.pill.radar");
        Component itemsText = Component.translatable("awakening_system.map.pill.items");
        Component gatesText = Component.translatable("awakening_system.map.pill.gates");
        boolean isCirc = "CIRCULAR".equalsIgnoreCase(GuiConfig.minimapShape);
        Component shapeText = Component.translatable(isCirc ? "awakening_system.map.pill.shape_circular" : "awakening_system.map.pill.shape_square");
        Component waypointsText = Component.translatable("awakening_system.map.pill.waypoints");
        Component guildsText = Component.translatable("awakening_system.map.pill.guilds");
        Component centerText = Component.translatable("awakening_system.map.pill.center");
        Component settingsText = Component.translatable("awakening_system.map.pill.settings");

        String curLight = GuiConfig.minimapLightingMode != null ? GuiConfig.minimapLightingMode.toUpperCase(java.util.Locale.ROOT) : "AUTO";

        Component[] labels = {
                Component.literal(forceCaveMode ? "§b🕳 " : "§7🕳 ").append(caveText),
                Component.literal("§6☀ ").append(lightText).append(": " + curLight),
                Component.literal(showChunkGrid ? "§a▦ " : "§7▦ ").append(gridText),
                Component.literal(showEntities ? "§c👤 " : "§7👤 ").append(radarText),
                Component.literal(showItems ? "§d💎 " : "§7💎 ").append(itemsText),
                Component.literal(showGates ? "§d🌀 " : "§7🌀 ").append(gatesText),
                Component.literal(isCirc ? "§b🔘 " : "§6🔲 ").append(shapeText),
                Component.literal("§e📍 ").append(waypointsText),
                Component.literal(showGuildClaims ? "§6🏰 " : "§7🏰 ").append(guildsText),
                Component.literal("§f⌖ ").append(centerText),
                Component.literal("§7⚙ ").append(settingsText)
        };
        boolean[] actives = {forceCaveMode, !"AUTO".equals(curLight), showChunkGrid, showEntities, showItems, showGates, !isCirc, false, showGuildClaims, false, false};
        Runnable[] actions = {
                () -> {
                    forceCaveMode = !forceCaveMode;
                    this.lastRenderedTimeSlice = "";
                    for (int r = 0; r < TEX_CHUNKS; r++) {
                        java.util.Arrays.fill(this.chunkRendered[r], false);
                    }
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, forceCaveMode ? 1.2F : 0.8F);
                },
                () -> {
                    String cur = GuiConfig.minimapLightingMode != null ? GuiConfig.minimapLightingMode.toUpperCase(java.util.Locale.ROOT) : "AUTO";
                    String next;
                    if ("AUTO".equals(cur)) next = "DAY";
                    else if ("DAY".equals(cur)) next = "NIGHT";
                    else next = "AUTO";
                    GuiConfig.minimapLightingMode = next;
                    GuiConfig.save();
                    this.lastRenderedTimeSlice = "";
                    for (int r = 0; r < TEX_CHUNKS; r++) {
                        java.util.Arrays.fill(this.chunkRendered[r], false);
                    }
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                },
                () -> {
                    showChunkGrid = !showChunkGrid;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, showChunkGrid ? 1.2F : 0.8F);
                },
                () -> {
                    showEntities = !showEntities;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, showEntities ? 1.2F : 0.8F);
                },
                () -> {
                    showItems = !showItems;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, showItems ? 1.2F : 0.8F);
                },
                () -> {
                    showGates = !showGates;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, showGates ? 1.2F : 0.8F);
                },
                () -> {
                    boolean c = "CIRCULAR".equalsIgnoreCase(GuiConfig.minimapShape);
                    GuiConfig.minimapShape = c ? "SQUARE" : "CIRCULAR";
                    GuiConfig.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                },
                () -> {
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    mc.setScreen(new WaypointListScreen(this));
                },
                () -> {
                    showGuildClaims = !showGuildClaims;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, showGuildClaims ? 1.2F : 0.8F);
                },
                () -> {
                    if (mc.player != null) {
                        cameraWorldX = mc.player.getX();
                        cameraWorldZ = mc.player.getZ();
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.3F);
                    }
                },
                () -> {
                    mc.setScreen(new GuiConfigScreen(this));
                }
        };

        int totalButtonsW = 0;
        int[] btnWidths = new int[labels.length];
        for (int i = 0; i < labels.length; i++) {
            btnWidths[i] = font.width(labels[i]) + 10;
            totalButtonsW += btnWidths[i];
        }
        int totalPillW = totalButtonsW + (labels.length - 1) * 3 + 6;
        int pillX = (this.width - totalPillW) / 2;

        // Escala adaptativa en pantallas estrechas / resoluciones reducidas
        // Deja margen para la barra de zoom izquierda y el botón cerrar / brújula derecha
        int availW = this.width - 68;
        float barScale = Math.min(1.0F, (float) availW / (float) totalPillW);
        if (barScale < 0.55F) barScale = 0.55F;

        g.pose().pushPose();
        float pillCenterX = this.width / 2.0F;
        float pillCenterY = pillY + pillH / 2.0F;
        if (barScale < 1.0F) {
            g.pose().translate(pillCenterX, pillCenterY, 0);
            g.pose().scale(barScale, barScale, 1.0F);
            g.pose().translate(-pillCenterX, -pillCenterY, 0);
        }

        g.fill(pillX, pillY, pillX + totalPillW, pillY + pillH, 0xEE08111A);
        g.renderOutline(pillX, pillY, totalPillW, pillH, 0x884EE2EC);

        int localMouseX = barScale < 1.0F ? (int) ((mouseX - pillCenterX) / barScale + pillCenterX) : mouseX;
        int localMouseY = barScale < 1.0F ? (int) ((mouseY - pillCenterY) / barScale + pillCenterY) : mouseY;

        int curBx = pillX + 3;
        for (int i = 0; i < labels.length; i++) {
            int bw = btnWidths[i];
            drawPillButton(g, curBx, pillY + 2, bw, 16, labels[i], actives[i], localMouseX, localMouseY);
            PillButtonDef def = new PillButtonDef();
            if (barScale < 1.0F) {
                def.x = (int) (pillCenterX + (curBx - pillCenterX) * barScale);
                def.y = (int) (pillCenterY + ((pillY + 2) - pillCenterY) * barScale);
                def.w = Math.max(1, (int) (bw * barScale));
                def.h = Math.max(1, (int) (16 * barScale));
            } else {
                def.x = curBx;
                def.y = pillY + 2;
                def.w = bw;
                def.h = 16;
            }
            def.onClick = actions[i];
            pillButtons.add(def);
            curBx += bw + 3;
        }

        g.pose().popPose();

        // 3. Rosa de los Vientos / Brújula Superior Derecha (Xaero's & JourneyMap Style)
        // Posicionada en cy = 72 para quedar completamente libre debajo de la barra superior y del botón cerrar
        renderCompassRose(g, this.width - 24, 72);

        // 4. Botón Cerrar [✕] en la esquina superior derecha
        int closeX = this.width - 24;
        int closeY = 6;
        boolean closeHov = mouseX >= closeX && mouseX < closeX + 18 && mouseY >= closeY && mouseY < closeY + 16;
        g.fill(closeX, closeY, closeX + 18, closeY + 16, closeHov ? 0xDD882222 : 0x88441111);
        g.renderOutline(closeX, closeY, 18, 16, closeHov ? 0xFFFF4444 : 0xFF882222);
        g.drawCenteredString(font, "✕", closeX + 9, closeY + 4, 0xFFFFFFFF);

        // 5. Barra Vertical de Zoom y Centrado en el lateral izquierdo
        // Posicionada en y = 52 para no colisionar nunca con la barra de herramientas superior
        renderVerticalZoomBar(g, 10, 52, mouseX, mouseY);
    }

    private void renderVerticalZoomBar(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        int w = 20;
        int h = 60;
        g.fill(x, y, x + w, y + h, 0xDD08111A);
        g.renderOutline(x, y, w, h, 0x884EE2EC);

        // Botón [+] Zoom In
        boolean inHov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 20;
        g.fill(x + 1, y + 1, x + w - 1, y + 19, inHov ? 0xDD2A4D66 : 0x00000000);
        g.drawCenteredString(font, "+", x + w / 2, y + 5, inHov ? 0xFF00E5FF : 0xFFFFFFFF);

        // Separador 1
        g.fill(x + 2, y + 20, x + w - 2, y + 21, 0x444EE2EC);

        // Botón [⌖] Centrar en Jugador
        boolean centerHov = mouseX >= x && mouseX < x + w && mouseY >= y + 20 && mouseY < y + 40;
        g.fill(x + 1, y + 21, x + w - 1, y + 39, centerHov ? 0xDD2A4D66 : 0x00000000);
        g.drawCenteredString(font, "⌖", x + w / 2, y + 25, centerHov ? 0xFF00E5FF : 0xFFFFFFFF);

        // Separador 2
        g.fill(x + 2, y + 40, x + w - 2, y + 41, 0x444EE2EC);

        // Botón [—] Zoom Out
        boolean outHov = mouseX >= x && mouseX < x + w && mouseY >= y + 40 && mouseY < y + 60;
        g.fill(x + 1, y + 41, x + w - 1, y + 59, outHov ? 0xDD2A4D66 : 0x00000000);
        g.drawCenteredString(font, "—", x + w / 2, y + 45, outHov ? 0xFF00E5FF : 0xFFFFFFFF);
    }

    private void renderCompassRose(GuiGraphics g, int cx, int cy) {
        // Círculo base de la brújula
        g.fill(cx - 15, cy - 15, cx + 16, cy + 16, 0xAA08111A);
        g.renderOutline(cx - 15, cy - 15, 31, 31, 0x664EE2EC);

        // Líneas en cruz cardinales
        g.fill(cx, cy - 12, cx + 1, cy + 13, 0x44FFFFFF);
        g.fill(cx - 12, cy, cx + 13, cy + 1, 0x44FFFFFF);

        // Puntos cardinales N, S, E, W
        g.drawCenteredString(font, "§c§lN", cx, cy - 13, 0xFFFF3333);
        g.drawCenteredString(font, "§7S", cx, cy + 6, 0xFFBBBBBB);
        g.drawCenteredString(font, "§7E", cx + 9, cy - 4, 0xFFBBBBBB);
        g.drawCenteredString(font, "§7W", cx - 9, cy - 4, 0xFFBBBBBB);
    }

    private void drawPillButton(GuiGraphics g, int x, int y, int w, int h, Component text, boolean active, int mouseX, int mouseY) {
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = active ? (hov ? 0xDD1E5C6B : 0xAA133D47) : (hov ? 0xDD253447 : 0x66111B24);
        int border = active ? 0xFF00E5FF : (hov ? 0xFF88AACC : 0x444EE2EC);

        g.fill(x, y, x + w, y + h, bg);
        g.renderOutline(x, y, w, h, border);
        g.drawCenteredString(font, text, x + w / 2, y + 4, 0xFFFFFFFF);
    }

    private void renderBottomBar(GuiGraphics g, Minecraft mc) {
        // Indicador de Escala de Zoom en el centro inferior (estilo 2.5x en JourneyMap)
        String scaleText = String.format(java.util.Locale.ROOT, "%.1fx", getPixelsPerBlock());
        int scaleW = font.width(scaleText) + 16;
        int scaleX = (this.width - scaleW) / 2;
        int scaleY = this.height - 20;
        g.fill(scaleX, scaleY, scaleX + scaleW, scaleY + 16, 0xDD08111A);
        g.renderOutline(scaleX, scaleY, scaleW, 16, 0x884EE2EC);
        g.drawCenteredString(font, scaleText, this.width / 2, scaleY + 4, 0xFF00E5FF);

        // Barra Inferior Estilizada
        int barX = 10;
        int barY = this.height - 22;
        int barW = this.width - 20;

        if (isBlockHovered && mc.level != null) {
            int hoveredChunkX = hoveredBlockX >> 4;
            int hoveredChunkZ = hoveredBlockZ >> 4;
            Component guildTip = XaeroGuildMapIntegration.getHighlightTooltip(mc.level.dimension(), hoveredChunkX, hoveredChunkZ);

            if (guildTip != null) {
                Component hoverWithGuild = Component.literal("§7")
                        .append(Component.translatable("awakening_system.map.cursor_coords", hoveredBlockX, hoveredBlockZ))
                        .append(Component.literal(" §8| "))
                        .append(guildTip)
                        .append(Component.literal(" §8| §e"))
                        .append(Component.translatable("awakening_system.map.options_hint"));
                g.drawString(font, hoverWithGuild, barX + 4, barY + 4, 0xFFFFFFFF, true);
            } else {
                Component hoverInfo = Component.literal("§7")
                        .append(Component.translatable("awakening_system.map.cursor_coords", hoveredBlockX, hoveredBlockZ))
                        .append(Component.literal(" §8| §b"))
                        .append(Component.translatable("awakening_system.map.context_menu_hint"));
                g.drawString(font, hoverInfo, barX + 4, barY + 4, 0xFFFFFFFF, true);
            }
        } else if (mc.player != null) {
            Component playerInfo = Component.translatable("awakening_system.map.player_info", mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ());
            g.drawString(font, playerInfo, barX + 4, barY + 4, 0xFFFFFFFF, true);
        }

        // Información de dimensión en la esquina derecha
        String dimName = mc.level != null ? mc.level.dimension().location().getPath() : "overworld";
        String dimStr = "\u00A77Dim: \u00A7f" + capitalize(dimName);
        g.drawString(font, dimStr, barX + barW - font.width(dimStr) - 4, barY + 4, 0xFF70E8FF, true);
    }

    private void renderContextMenu(GuiGraphics g, int mouseX, int mouseY) {
        int menuW = 194;
        int menuH = 196;

        // Asegurar que el menú contextual no se salga de la pantalla
        int menuX = Math.min(contextMenuX, this.width - menuW - 4);
        int menuY = Math.min(contextMenuY, this.height - menuH - 4);

        // Fondo oscuro y marco elegante (JourneyMap Style)
        g.fill(menuX, menuY, menuX + menuW, menuY + menuH, 0xFA0C131D);
        g.renderOutline(menuX, menuY, menuW, menuH, 0xFF4EE2EC);

        // Encabezado
        g.drawCenteredString(font, Component.translatable("awakening_system.map.context.title"), menuX + menuW / 2, menuY + 6, 0xFFFFFFFF);
        int relX = contextBlockX & 15;
        int relZ = contextBlockZ & 15;
        String chunkCoordStr = String.format("§7C: (%d,%d):(%d,%d)", contextChunkX, contextChunkZ, relX, relZ);
        g.drawCenteredString(font, chunkCoordStr, menuX + menuW / 2, menuY + 18, 0xFFAAAAAA);

        String blockCoordStr = String.format("§eX: %d, Y: %d, Z: %d", contextBlockX, contextBlockY, contextBlockZ);
        g.drawCenteredString(font, blockCoordStr, menuX + menuW / 2, menuY + 28, 0xFFFFE066);

        String biomeStr = "§b" + contextBiome;
        g.drawCenteredString(font, biomeStr, menuX + menuW / 2, menuY + 38, 0xFF70E8FF);

        // Separador horizontal
        int sepY = menuY + 49;
        g.fill(menuX + 4, sepY, menuX + menuW - 4, sepY + 1, 0xFF354455);

        // 7 Opciones interactivas estilo JourneyMap
        boolean isCirc = "CIRCULAR".equalsIgnoreCase(GuiConfig.minimapShape);
        Component[] options = {
                Component.literal("§a+ §f").append(Component.translatable("awakening_system.map.context.create_waypoint")),
                Component.literal("§a- §f").append(Component.translatable("awakening_system.map.context.temp_waypoint")),
                Component.literal("§e⚡ §f").append(Component.translatable("awakening_system.map.context.teleport")),
                Component.literal("§b💬 §f").append(Component.translatable("awakening_system.map.context.share_chat")),
                Component.literal(isCirc ? "§b🔘 §f" : "§6🔲 §f").append(Component.translatable("awakening_system.map.context.toggle_shape")).append(": ").append(Component.translatable(isCirc ? "awakening_system.map.pill.shape_circular" : "awakening_system.map.pill.shape_square")),
                Component.literal("§aCTRL+N §f").append(Component.translatable("awakening_system.map.context.open_waypoints")),
                Component.literal("§7⚙ §f").append(Component.translatable("awakening_system.map.context.open_settings"))
        };

        int optStartY = sepY + 4;
        int optH = 20;

        for (int i = 0; i < options.length; i++) {
            int oy = optStartY + i * optH;
            boolean hov = mouseX >= menuX + 4 && mouseX < menuX + menuW - 4 && mouseY >= oy && mouseY < oy + optH;

            if (hov) {
                g.fill(menuX + 4, oy, menuX + menuW - 4, oy + optH - 1, 0xDD2A4D66);
                g.renderOutline(menuX + 4, oy, menuW - 8, optH - 1, 0xFF00E5FF);
            }

            g.drawString(font, options[i], menuX + 10, oy + 5, 0xFFFFFFFF);
        }
    }

    private void handleContextMenuOption(int index) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        String currentDim = mc.level != null ? mc.level.dimension().location().toString() : "minecraft:overworld";

        switch (index) {
            case 0 -> { // + Create Waypoint
                Waypoint newWp = new Waypoint(contextBiome + " (" + contextBlockX + ", " + contextBlockZ + ")", currentDim, contextBlockX, contextBlockY, contextBlockZ, 0x00E5FF);
                mc.setScreen(new WaypointEditScreen(newWp, true, this));
                this.contextMenuOpen = false;
            }
            case 1 -> { // Set Temporary Waypoint
                Waypoint tempWp = new Waypoint("Punto (" + contextBlockX + ", " + contextBlockZ + ")", currentDim, contextBlockX, contextBlockY, contextBlockZ, 0xFFA500);
                WaypointManager.addWaypoint(tempWp);
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                this.contextMenuOpen = false;
            }
            case 2 -> { // Teleport Here
                int safeY = findSafeTeleportY(mc.level, contextBlockX, contextBlockY, contextBlockZ);
                mc.player.connection.sendCommand("tp @s " + contextBlockX + " " + safeY + " " + contextBlockZ);
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.3F);
                this.contextMenuOpen = false;
            }
            case 3 -> { // Share Location In Chat
                String shareMsg = String.format("📍 [%s] X: %d, Y: %d, Z: %d", contextBiome, contextBlockX, contextBlockY, contextBlockZ);
                mc.player.connection.sendChat(shareMsg);
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                this.contextMenuOpen = false;
            }
            case 4 -> { // Toggle Minimap Shape
                boolean c = "CIRCULAR".equalsIgnoreCase(GuiConfig.minimapShape);
                GuiConfig.minimapShape = c ? "SQUARE" : "CIRCULAR";
                GuiConfig.save();
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                this.contextMenuOpen = false;
            }
            case 5 -> { // CTRL + N Open Waypoint Menu
                mc.setScreen(new WaypointListScreen(this));
                this.contextMenuOpen = false;
            }
            case 6 -> { // Open Settings
                mc.setScreen(new GuiConfigScreen(this));
                this.contextMenuOpen = false;
            }
        }
    }

    private static int findSafeTeleportY(ClientLevel level, int x, int fallbackY, int z) {
        if (level == null) return fallbackY;
        LevelChunk chunk = level.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false);
        int topY = (chunk != null && !chunk.isEmpty()) ? chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15) : fallbackY;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int searchStart = Math.min(level.getMaxBuildHeight() - 2, Math.max(fallbackY + 5, topY + 2));
        int searchEnd = Math.max(level.getMinBuildHeight() + 1, fallbackY - 15);

        for (int y = searchStart; y >= searchEnd; y--) {
            pos.set(x, y, z);
            BlockState foot = level.getBlockState(pos);
            BlockState head = level.getBlockState(pos.above());
            BlockState ground = level.getBlockState(pos.below());

            if ((foot.isAir() || !foot.blocksMotion()) && (head.isAir() || !head.blocksMotion()) && ground.isSolidRender(level, pos.below())) {
                return y;
            }
        }
        return topY + 1;
    }

    private String formatBiome(net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeHolder) {
        if (biomeHolder == null) return Component.translatable("awakening_system.minimap.wilderness").getString();
        return biomeHolder.unwrapKey().map(k ->
            Component.translatable(net.minecraft.Util.makeDescriptionId("biome", k.location())).getString()
        ).orElseGet(() -> Component.translatable("awakening_system.minimap.wilderness").getString());
    }

    private String formatBiomeName(String raw) {
        if (raw == null || raw.isEmpty()) return "Wilderness";
        String clean = raw.replace('_', ' ');
        return capitalize(clean);
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

    private int getSurfaceYAt(ClientLevel level, int wx, int wz) {
        LevelChunk chunk = level.getChunkSource().getChunk(wx >> 4, wz >> 4, ChunkStatus.FULL, false);
        if (chunk != null && !chunk.isEmpty()) {
            return chunk.getHeight(Heightmap.Types.WORLD_SURFACE, wx & 15, wz & 15);
        }
        return level.getSeaLevel();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double scX = this.width / 2.0D;
        double scY = this.height / 2.0D;
        double oldPpb = getPixelsPerBlock();

        double mouseWorldX = cameraWorldX + (mouseX - scX) / oldPpb;
        double mouseWorldZ = cameraWorldZ + (mouseY - scY) / oldPpb;

        if (scrollY > 0 && zoomIndex < ZOOM_PIXELS_PER_BLOCK.length - 1) {
            zoomIndex++;
            double newPpb = getPixelsPerBlock();
            cameraWorldX = mouseWorldX - (mouseX - scX) / newPpb;
            cameraWorldZ = mouseWorldZ - (mouseY - scY) / newPpb;
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.7F, 1.3F);
            return true;
        } else if (scrollY < 0 && zoomIndex > 0) {
            zoomIndex--;
            double newPpb = getPixelsPerBlock();
            cameraWorldX = mouseWorldX - (mouseX - scX) / newPpb;
            cameraWorldZ = mouseWorldZ - (mouseY - scY) / newPpb;
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.7F, 0.9F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && !contextMenuOpen) {
            double ppb = getPixelsPerBlock();
            this.cameraWorldX -= dragX / ppb;
            this.cameraWorldZ -= dragY / ppb;
            this.isDragging = true;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Minecraft mc = Minecraft.getInstance();

        // 1. Interacción con Menú Contextual (si está abierto)
        if (contextMenuOpen) {
            int menuW = 194;
            int menuH = 196;
            int menuX = Math.min(contextMenuX, this.width - menuW - 4);
            int menuY = Math.min(contextMenuY, this.height - menuH - 4);
            int sepY = menuY + 49;
            int optStartY = sepY + 4;
            int optH = 20;

            if (mouseX >= menuX && mouseX < menuX + menuW && mouseY >= menuY && mouseY < menuY + menuH) {
                // Clic dentro del menú contextual (7 opciones)
                if (button == 0 && mouseY >= optStartY && mouseY < optStartY + 7 * optH) {
                    int optIdx = (int) ((mouseY - optStartY) / optH);
                    if (optIdx >= 0 && optIdx < 7) {
                        handleContextMenuOption(optIdx);
                        return true;
                    }
                }
                return true; // Consumir clic dentro del menú
            } else {
                // Clic fuera del menú: cerrar menú
                contextMenuOpen = false;
                if (button == 0) return true;
                // Si fue clic derecho, continuar abajo para reabrir en la nueva posición
            }
        }

        // 2. Clic en Barra Superior Tipo Pastilla Flotante (Responsive)
        if (button == 0) {
            for (PillButtonDef pBtn : pillButtons) {
                if (mouseX >= pBtn.x && mouseX < pBtn.x + pBtn.w && mouseY >= pBtn.y && mouseY < pBtn.y + pBtn.h) {
                    pBtn.onClick.run();
                    return true;
                }
            }
        }

        // 3. Clic en Barra Vertical de Zoom y Centrado en la izquierda
        int zoomBarX = 10;
        int zoomBarY = 52;
        if (button == 0 && mouseX >= zoomBarX && mouseX < zoomBarX + 20 && mouseY >= zoomBarY && mouseY < zoomBarY + 60) {
            if (mouseY < zoomBarY + 20) { // [+]
                if (zoomIndex < ZOOM_PIXELS_PER_BLOCK.length - 1) {
                    zoomIndex++;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.3F);
                }
            } else if (mouseY < zoomBarY + 40) { // [⌖]
                if (mc.player != null) {
                    cameraWorldX = mc.player.getX();
                    cameraWorldZ = mc.player.getZ();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.3F);
                }
            } else { // [—]
                if (zoomIndex > 0) {
                    zoomIndex--;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 0.9F);
                }
            }
            return true;
        }

        // 4. Clic en Botón Cerrar [✕]
        int closeX = this.width - 24;
        int closeY = 6;
        if (button == 0 && mouseX >= closeX && mouseX < closeX + 18 && mouseY >= closeY && mouseY < closeY + 16) {
            this.onClose();
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.9F, 0.9F);
            return true;
        }

        // 5. Clic Derecho en el mapa: ABRIR MENÚ CONTEXTUAL ESTILO JOURNEYMAP (Imagen 4)
        if (button == 1) {
            double scX = this.width / 2.0D;
            double scY = this.height / 2.0D;
            double ppb = getPixelsPerBlock();

            contextBlockX = (int) Math.floor(cameraWorldX + (mouseX - scX) / ppb);
            contextBlockZ = (int) Math.floor(cameraWorldZ + (mouseY - scY) / ppb);
            contextChunkX = contextBlockX >> 4;
            contextChunkZ = contextBlockZ >> 4;

            if (mc.level != null) {
                contextBlockY = getSurfaceYAt(mc.level, contextBlockX, contextBlockZ);
                BlockPos targetPos = new BlockPos(contextBlockX, contextBlockY, contextBlockZ);
                contextBiome = formatBiome(mc.level.getBiome(targetPos));
            } else {
                contextBlockY = 64;
                contextBiome = Component.translatable("awakening_system.minimap.wilderness").getString();
            }

            contextMenuX = (int) mouseX;
            contextMenuY = (int) mouseY;
            contextMenuOpen = true;

            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
            return true;
        }

        if (button == 0) {
            this.isDragging = false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Cerrar menú contextual con tecla Escape si está abierto
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && contextMenuOpen) {
            contextMenuOpen = false;
            return true;
        }

        // CTRL + N: Abrir Gestor de Waypoints (JourneyMap shortcut)
        if (keyCode == GLFW.GLFW_KEY_N && hasControlDown()) {
            Minecraft.getInstance().setScreen(new WaypointListScreen(this));
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
