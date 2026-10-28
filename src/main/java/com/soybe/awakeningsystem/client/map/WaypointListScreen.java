package com.soybe.awakeningsystem.client.map;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.client.gui.GuiConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pantalla de administración de Waypoints estilo JourneyMap (Manage Waypoints).
 * Incluye ordenación por distancia, filtros por dimensión, botones por fila
 * [Teleport] [Find] [On/Off] [Remove] [Edit] [Chat], y barra inferior [Options] [New] [Dimension] [Close].
 */
public class WaypointListScreen extends Screen {
    private final Screen parentScreen;
    private int scrollOffset = 0;
    private boolean sortDistanceAsc = true;
    private String selectedDimensionFilter = "ALL"; // "ALL", "CURRENT", "OVERWORLD", "NETHER", "END"

    public WaypointListScreen(Screen parentScreen) {
        super(Component.translatable("awakening_system.waypoint_list.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Neutralizado: El fondo se dibuja en render() antes de los componentes para evitar doble tintado
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
    public void onClose() {
        if (parentScreen != null) {
            Minecraft.getInstance().setScreen(parentScreen);
        } else {
            super.onClose();
        }
    }

    private List<Waypoint> getFilteredAndSortedWaypoints() {
        Minecraft mc = Minecraft.getInstance();
        String currentDim = mc.level != null ? mc.level.dimension().location().toString() : "minecraft:overworld";
        List<Waypoint> all = WaypointManager.getAllWaypoints();
        List<Waypoint> filtered = new ArrayList<>();

        for (Waypoint wp : all) {
            String wpDim = wp.getDimension() != null ? wp.getDimension() : "minecraft:overworld";
            boolean match = switch (selectedDimensionFilter) {
                case "CURRENT" -> WaypointBeaconRenderer.isDimensionMatching(wpDim, currentDim);
                case "OVERWORLD" -> wpDim.equalsIgnoreCase("all") || wpDim.contains("overworld");
                case "NETHER" -> wpDim.equalsIgnoreCase("all") || wpDim.contains("the_nether") || wpDim.contains("nether");
                case "END" -> wpDim.equalsIgnoreCase("all") || wpDim.contains("the_end") || wpDim.contains("end");
                default -> true; // "ALL"
            };
            if (match) {
                filtered.add(wp);
            }
        }

        if (mc.player != null) {
            double px = mc.player.getX();
            double py = mc.player.getY();
            double pz = mc.player.getZ();
            if (sortDistanceAsc) {
                filtered.sort(Comparator.comparingDouble(w -> w.distanceToSq(px, py, pz)));
            } else {
                filtered.sort((w1, w2) -> Double.compare(w2.distanceToSq(px, py, pz), w1.distanceToSq(px, py, pz)));
            }
        }
        return filtered;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0x85050C14, 0xA0050C14);
        Minecraft mc = Minecraft.getInstance();

        int panelW = Math.min(540, this.width - 20);
        int panelH = Math.min(270, this.height - 30);
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2 - 6;

        // Fondo y marco del Panel Táctico (JourneyMap Holographic Style)
        g.fill(left - 1, top - 1, left + panelW + 1, top + panelH + 1, 0xFF4EE2EC);
        g.fill(left, top, left + panelW, top + panelH, 0xF508111A);
        g.fill(left, top, left + panelW, top + 24, 0xFF10202E);

        // Título Superior
        g.drawCenteredString(font, Component.translatable("awakening_system.waypoint_list.title"), left + panelW / 2, top + 7, 0xFF4EE2EC);

        // Subcabecera de Columnas: Distance ▲, Name, All X: On/Off
        int headerY = top + 26;
        g.fill(left, headerY, left + panelW, headerY + 16, 0xEE0B1522);

        // 1. Botón Distancia (Ordenación asc/desc)
        String distHeader = (sortDistanceAsc ? "▲ " : "▼ ") + Component.translatable("awakening_system.waypoint_list.col.distance").getString();
        boolean distHov = mouseX >= left + 26 && mouseX < left + 90 && mouseY >= headerY && mouseY < headerY + 16;
        g.drawString(font, distHeader, left + 28, headerY + 4, distHov ? 0xFF00E5FF : 0xFF70E8FF, true);

        // 2. Columna Nombre
        g.drawString(font, Component.translatable("awakening_system.waypoint_list.col.name"), left + 96, headerY + 4, 0xFFFFFFFF, true);

        // 3. Toggle Masivo: All X: On / Off
        List<Waypoint> list = getFilteredAndSortedWaypoints();
        long enabledCount = list.stream().filter(Waypoint::isEnabled).count();
        boolean allEnabled = !list.isEmpty() && enabledCount == list.size();
        String toggleAllText = String.format("%s %d: %s",
                Component.translatable("awakening_system.waypoint_list.all").getString(),
                list.size(),
                allEnabled ? Component.translatable("awakening_system.gui_config.toggle.on").getString() : Component.translatable("awakening_system.gui_config.toggle.off").getString()
        );
        int allBtnW = font.width(toggleAllText) + 12;
        int allBtnX = left + panelW - allBtnW - 8;
        boolean allHov = mouseX >= allBtnX && mouseX < allBtnX + allBtnW && mouseY >= headerY + 1 && mouseY < headerY + 15;
        g.fill(allBtnX, headerY + 1, allBtnX + allBtnW, headerY + 15, allHov ? 0xDD2A4D66 : 0x88152533);
        g.renderOutline(allBtnX, headerY + 1, allBtnW, 14, allHov ? 0xFF00E5FF : 0xFF2A4255);
        g.drawCenteredString(font, toggleAllText, allBtnX + allBtnW / 2, headerY + 4, allEnabled ? 0xFF50FA7B : 0xFFFFAAAA);

        // Separador fino
        g.fill(left, headerY + 16, left + panelW, headerY + 17, 0xFF2A3D4D);

        // Lista de Waypoints
        int itemH = 20;
        int listY = headerY + 18;
        int bottomBarY = top + panelH - 24;
        int visibleCount = (bottomBarY - listY) / itemH;

        if (list.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("awakening_system.waypoint_list.empty"), left + panelW / 2, top + 90, 0xFFAAAAAA);
            g.drawCenteredString(font, Component.translatable("awakening_system.waypoint_list.hint"), left + panelW / 2, top + 105, 0xFF667788);
        } else {
            double px = mc.player != null ? mc.player.getX() : 0;
            double py = mc.player != null ? mc.player.getY() : 64;
            double pz = mc.player != null ? mc.player.getZ() : 0;

            for (int i = scrollOffset; i < list.size() && i < scrollOffset + visibleCount; i++) {
                Waypoint wp = list.get(i);
                int y = listY + (i - scrollOffset) * itemH;
                boolean rowHov = mouseX >= left + 4 && mouseX < left + panelW - 4 && mouseY >= y && mouseY < y + itemH - 1;

                g.fill(left + 4, y, left + panelW - 4, y + itemH - 1, rowHov ? 0xDD142533 : 0xAA0D1824);
                g.renderOutline(left + 4, y, panelW - 8, itemH - 1, rowHov ? 0x8800E5FF : 0x442A4255);

                // 1. Diamante de color (JourneyMap diamond marker)
                int col = wp.getColor() | 0xFF000000;
                int iconX = left + 10;
                int iconY = y + 4;
                g.drawString(font, "◆", iconX, iconY, col, true);

                // 2. Distancia en metros (Cian brillante con drop shadow para máximo contraste)
                double dist = wp.distanceTo(px, py, pz);
                String distStr = (int) Math.round(dist) + "m";
                g.drawString(font, distStr, left + 26, y + 5, 0xFF8BE9FD, true);

                // 3. Nombre del Waypoint (en su color vibrante optimizado con drop shadow)
                int nameMaxW = panelW - 320;
                String truncatedName = font.plainSubstrByWidth(wp.getName(), nameMaxW);
                int brightCol = ensureHighContrast(col);
                g.drawString(font, truncatedName, left + 96, y + 5, brightCol, true);

                // 4. Botones de Acción por fila: [Teleport] [Find] [On/Off] [Remove] [Edit] [Chat]
                int bx = left + panelW - 228;
                int btnH = 14;
                int by = y + 2;

                // [Teleport]
                renderSmallButton(g, bx, by, 42, btnH, Component.translatable("awakening_system.waypoint_list.btn.teleport"), mouseX, mouseY, 0xFF50FA7B);
                bx += 44;

                // [Find]
                renderSmallButton(g, bx, by, 32, btnH, Component.translatable("awakening_system.waypoint_list.btn.find"), mouseX, mouseY, 0xFF70E8FF);
                bx += 34;

                // [On / Off]
                Component onOffComp = wp.isEnabled() ? Component.translatable("awakening_system.gui_config.toggle.on") : Component.translatable("awakening_system.gui_config.toggle.off");
                int onOffCol = wp.isEnabled() ? 0xFF50FA7B : 0xFFFFAAAA;
                renderSmallButton(g, bx, by, 28, btnH, onOffComp, mouseX, mouseY, onOffCol);
                bx += 30;

                // [Remove]
                renderSmallButton(g, bx, by, 38, btnH, Component.translatable("awakening_system.waypoint_list.btn.remove"), mouseX, mouseY, 0xFFFF6666);
                bx += 40;

                // [Edit]
                renderSmallButton(g, bx, by, 32, btnH, Component.translatable("awakening_system.waypoint_list.btn.edit"), mouseX, mouseY, 0xFFFFE066);
                bx += 34;

                // [Chat]
                renderSmallButton(g, bx, by, 32, btnH, Component.translatable("awakening_system.waypoint_list.btn.chat"), mouseX, mouseY, 0xFFBD93F9);
            }
        }

        // ==========================================
        // BARRA INFERIOR ESTILO JOURNEYMAP
        // [Options...] [New...] [Dimension: ...] [Close]
        // ==========================================
        g.fill(left, bottomBarY, left + panelW, top + panelH, 0xFF10202E);
        g.fill(left, bottomBarY - 1, left + panelW, bottomBarY, 0xFF2A3D4D);

        int barBtnH = 18;
        int barBtnY = bottomBarY + 3;
        int barBtnW = (panelW - 25) / 4;

        // [Options...]
        int b1X = left + 5;
        renderBottomBarButton(g, b1X, barBtnY, barBtnW, barBtnH, Component.translatable("awakening_system.waypoint_list.btn.options"), mouseX, mouseY, 0xFFCCCCCC);

        // [New...]
        int b2X = b1X + barBtnW + 5;
        renderBottomBarButton(g, b2X, barBtnY, barBtnW, barBtnH, Component.translatable("awakening_system.waypoint_list.btn.create"), mouseX, mouseY, 0xFF50FA7B);

        // [Dimension: ...]
        int b3X = b2X + barBtnW + 5;
        Component dimLabel = Component.translatable("awakening_system.waypoint_list.dim", selectedDimensionFilter);
        renderBottomBarButton(g, b3X, barBtnY, barBtnW, barBtnH, dimLabel, mouseX, mouseY, 0xFF70E8FF);

        // [Close]
        int b4X = b3X + barBtnW + 5;
        renderBottomBarButton(g, b4X, barBtnY, barBtnW, barBtnH, Component.translatable("awakening_system.waypoint_list.btn.close"), mouseX, mouseY, 0xFFFF6666);

        g.flush();
    }

    private void renderSmallButton(GuiGraphics g, int x, int y, int w, int h, Component text, int mouseX, int mouseY, int textCol) {
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        g.fill(x, y, x + w, y + h, hov ? 0xDD2A4D66 : 0x88152533);
        g.renderOutline(x, y, w, h, hov ? 0xFF00E5FF : 0xFF2A4255);
        g.drawCenteredString(font, text, x + w / 2, y + 3, hov ? 0xFFFFFFFF : textCol);
    }

    private void renderBottomBarButton(GuiGraphics g, int x, int y, int w, int h, Component text, int mouseX, int mouseY, int textCol) {
        boolean hov = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        g.fill(x, y, x + w, y + h, hov ? 0xDD2A4D66 : 0xAA102230);
        g.renderOutline(x, y, w, h, hov ? 0xFF4EE2EC : 0xFF2A4255);
        g.drawCenteredString(font, text, x + w / 2, y + 5, hov ? 0xFFFFFFFF : textCol);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Minecraft mc = Minecraft.getInstance();
            int panelW = Math.min(540, this.width - 20);
            int panelH = Math.min(270, this.height - 30);
            int left = (this.width - panelW) / 2;
            int top = (this.height - panelH) / 2 - 6;

            int headerY = top + 26;
            // 1. Clic en Ordenar por Distancia
            if (mouseX >= left + 26 && mouseX < left + 90 && mouseY >= headerY && mouseY < headerY + 16) {
                sortDistanceAsc = !sortDistanceAsc;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                return true;
            }

            // 2. Clic en Toggle Masivo (All X: On/Off)
            List<Waypoint> list = getFilteredAndSortedWaypoints();
            long enabledCount = list.stream().filter(Waypoint::isEnabled).count();
            boolean allEnabled = !list.isEmpty() && enabledCount == list.size();
            String toggleAllText = String.format("%s %d: %s",
                    Component.translatable("awakening_system.waypoint_list.all").getString(),
                    list.size(),
                    allEnabled ? Component.translatable("awakening_system.gui_config.toggle.on").getString() : Component.translatable("awakening_system.gui_config.toggle.off").getString()
            );
            int allBtnW = font.width(toggleAllText) + 12;
            int allBtnX = left + panelW - allBtnW - 8;
            if (mouseX >= allBtnX && mouseX < allBtnX + allBtnW && mouseY >= headerY + 1 && mouseY < headerY + 15) {
                boolean newTargetState = !allEnabled;
                for (Waypoint wp : list) {
                    wp.setEnabled(newTargetState);
                }
                WaypointManager.save();
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, newTargetState ? 1.3F : 0.8F);
                return true;
            }

            // 3. Clic en botones por fila
            int itemH = 20;
            int listY = headerY + 18;
            int bottomBarY = top + panelH - 24;
            int visibleCount = (bottomBarY - listY) / itemH;

            for (int i = scrollOffset; i < list.size() && i < scrollOffset + visibleCount; i++) {
                Waypoint wp = list.get(i);
                int y = listY + (i - scrollOffset) * itemH;
                int bx = left + panelW - 228;
                int btnH = 14;
                int by = y + 2;

                // [Teleport]
                if (mouseX >= bx && mouseX < bx + 42 && mouseY >= by && mouseY < by + btnH) {
                    if (mc.player != null && mc.level != null) {
                        int safeY = findSafeTeleportY(mc.level, wp.getX(), wp.getY(), wp.getZ());
                        mc.player.connection.sendCommand("tp @s " + wp.getX() + " " + safeY + " " + wp.getZ());
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.3F);
                        onClose();
                    }
                    return true;
                }
                bx += 44;

                // [Find]
                if (mouseX >= bx && mouseX < bx + 32 && mouseY >= by && mouseY < by + btnH) {
                    HunterWorldMapScreen mapScreen = new HunterWorldMapScreen();
                    mapScreen.centerOnCoordinates(wp.getX(), wp.getZ());
                    mc.setScreen(mapScreen);
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    return true;
                }
                bx += 34;

                // [On/Off]
                if (mouseX >= bx && mouseX < bx + 28 && mouseY >= by && mouseY < by + btnH) {
                    wp.setEnabled(!wp.isEnabled());
                    WaypointManager.save();
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, wp.isEnabled() ? 1.2F : 0.8F);
                    return true;
                }
                bx += 30;

                // [Remove]
                if (mouseX >= bx && mouseX < bx + 38 && mouseY >= by && mouseY < by + btnH) {
                    WaypointManager.removeWaypoint(wp.getId());
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.9F, 0.8F);
                    return true;
                }
                bx += 40;

                // [Edit]
                if (mouseX >= bx && mouseX < bx + 32 && mouseY >= by && mouseY < by + btnH) {
                    mc.setScreen(new WaypointEditScreen(wp, false, this));
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                    return true;
                }
                bx += 34;

                // [Chat]
                if (mouseX >= bx && mouseX < bx + 32 && mouseY >= by && mouseY < by + btnH) {
                    if (mc.player != null) {
                        String chatShare = String.format("📍 [%s] X: %d, Y: %d, Z: %d (%s)", wp.getName(), wp.getX(), wp.getY(), wp.getZ(), wp.getDimension());
                        mc.player.connection.sendChat(chatShare);
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                    }
                    return true;
                }
            }

            // 4. Clic en Barra Inferior [Options] [New] [Dimension] [Close]
            int barBtnH = 18;
            int barBtnY = bottomBarY + 3;
            int barBtnW = (panelW - 25) / 4;

            // [Options]
            int b1X = left + 5;
            if (mouseX >= b1X && mouseX < b1X + barBtnW && mouseY >= barBtnY && mouseY < barBtnY + barBtnH) {
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                mc.setScreen(new GuiConfigScreen(this));
                return true;
            }

            // [New]
            int b2X = b1X + barBtnW + 5;
            if (mouseX >= b2X && mouseX < b2X + barBtnW && mouseY >= barBtnY && mouseY < barBtnY + barBtnH) {
                if (mc.player != null) {
                    Waypoint newWp = new Waypoint("Waypoint", mc.player.level().dimension().location().toString(),
                            mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(), 0x00E5FF);
                    mc.setScreen(new WaypointEditScreen(newWp, true, this));
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                }
                return true;
            }

            // [Dimension]
            int b3X = b2X + barBtnW + 5;
            if (mouseX >= b3X && mouseX < b3X + barBtnW && mouseY >= barBtnY && mouseY < barBtnY + barBtnH) {
                selectedDimensionFilter = switch (selectedDimensionFilter) {
                    case "ALL" -> "CURRENT";
                    case "CURRENT" -> "OVERWORLD";
                    case "OVERWORLD" -> "NETHER";
                    case "NETHER" -> "END";
                    default -> "ALL";
                };
                scrollOffset = 0;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                return true;
            }

            // [Close]
            int b4X = b3X + barBtnW + 5;
            if (mouseX >= b4X && mouseX < b4X + barBtnW && mouseY >= barBtnY && mouseY < barBtnY + barBtnH) {
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.9F, 0.9F);
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static int findSafeTeleportY(ClientLevel level, int x, int fallbackY, int z) {
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<Waypoint> list = getFilteredAndSortedWaypoints();
        int panelH = Math.min(270, this.height - 30);
        int bottomBarY = panelH - 24;
        int listY = 44;
        int visibleCount = (bottomBarY - listY) / 20;
        int maxOffset = Math.max(0, list.size() - visibleCount);

        if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
            return true;
        } else if (scrollY < 0 && scrollOffset < maxOffset) {
            scrollOffset++;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * Ajusta dinámicamente el brillo y saturación de cualquier color personalizado
     * para asegurar máxima legibilidad y contraste contra fondos oscuros de interfaz.
     */
    public static int ensureHighContrast(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
        float s = Math.min(hsb[1], 0.75F);
        float br = Math.max(hsb[2], 0.95F);
        return java.awt.Color.HSBtoRGB(hsb[0], s, br) | 0xFF000000;
    }
}
