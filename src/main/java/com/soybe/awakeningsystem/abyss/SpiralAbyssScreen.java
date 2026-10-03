package com.soybe.awakeningsystem.abyss;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Pantalla Monumental de la Espiral del Abismo (Spiral Abyss) con estética y proporciones auténticas de Genshin Impact:
 * - Cielo Cósmico Dinámico del Arrecife de Musk con nebulosas ondulantes, constelaciones y polvo estelar.
 * - Barra Superior con estilo de cristal ahumado celestial, divisas reales (Mora, Protogemas, Destinos) y estrellas globales.
 * - Pestañas de navegación con biseles dorados e indicador astral ("Corredor del Abismo 1-8" y "Espiral Lunar 9-12").
 * - VITRINA MONUMENTAL DE PUERTAS DE DOMINIO (Domain Doors):
 *   - Proporciones imponentes ocupando el 55% de la altura vertical de la pantalla.
 *   - Frontón ceremonial de 3 niveles con moldura denticulada de piedra labrada y biseles 3D.
 *   - Medallón cenital en forma de broche dorado con número romano esculpido (I..XII).
 *   - Columnas gemelas estriadas con capiteles decorados, plintos y canales rúnicos luminiscentes.
 *   - Vano abovedado en arco de medio punto romano.
 *   - Vórtice Cósmico Activo: singularidad estelar con órbitas elípticas, polvo cósmico y destello central animado.
 *   - Puerta Sellada Ancestral: hojas dobles de obsidiana con runas, cadenas diagonales de hierro y candado carmesí.
 *   - Pedestal inferior de piedra con placa del piso, gemas de las 3 cámaras (★ ★ ★) y nivel recomendado.
 *   - Elevación 3D, halo celestial y esquineros dorados al seleccionar o pasar el cursor.
 * - PANEL INFERIOR TÁCTICO INTEGRADO:
 *   - Tarjeta de Anomalía de las Líneas Ley con emblema elemental, bonificaciones detalladas, consejo táctico y medidor de nivel.
 *   - Selector de Cámaras (Sala 1, 2 y 3) con objetivos de tiempo para 1, 2 y 3 estrellas y lista de enemigos.
 *   - Escaparate de Recompensas del Piso con iconos reales de ítems y botón de reclamo brillante.
 *   - Botón monumental "⚔ DESAFIAR PISO X ⚔" con estilo Genshin Impact.
 */
public final class SpiralAbyssScreen extends Screen {

    private int unlockedFloor;
    private final int[] starsPerFloor = new int[SpiralAbyssManager.TOTAL_FLOORS];
    private final boolean[] claimedRewards = new boolean[SpiralAbyssManager.TOTAL_FLOORS];
    private int totalStars;
    private int playerLevel;
    private int primogems;
    private int coins;
    private int intertwinedFates;

    // Estado de navegación
    private int selectedTab = 0; // 0: Corredor del Abismo (1-8), 1: Espiral Lunar (9-12)
    private int corridorPage = 0; // 0: Pisos 1-4, 1: Pisos 5-8
    private int selectedFloor = 1;
    private int selectedChamber = 1; // 1, 2 o 3

    private static final String[] ROMAN_NUMERALS = {
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"
    };

    public SpiralAbyssScreen(OpenSpiralAbyssPayload payload) {
        super(Component.literal("Espiral del Abismo"));
        updateData(payload);

        this.selectedFloor = Math.max(1, Math.min(this.unlockedFloor, SpiralAbyssManager.TOTAL_FLOORS));
        if (this.selectedFloor >= 9) {
            this.selectedTab = 1;
        } else {
            this.selectedTab = 0;
            this.corridorPage = (this.selectedFloor > 4) ? 1 : 0;
        }
    }

    public void updateData(OpenSpiralAbyssPayload payload) {
        this.unlockedFloor = Math.max(1, payload.currentUnlockedFloor());
        System.arraycopy(payload.starsPerFloor(), 0, this.starsPerFloor, 0, Math.min(SpiralAbyssManager.TOTAL_FLOORS, payload.starsPerFloor().length));
        System.arraycopy(payload.claimedRewards(), 0, this.claimedRewards, 0, Math.min(SpiralAbyssManager.TOTAL_FLOORS, payload.claimedRewards().length));
        this.totalStars = payload.totalStars();
        this.playerLevel = payload.playerLevel();
        this.primogems = payload.primogems();
        this.coins = payload.coins();
        this.intertwinedFates = payload.intertwinedFates();
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(ModSounds.ABYSS_ENTER.value(), 0.75F, 1.0F);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Evitar el fondo gris borroso por defecto de Minecraft
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int w = this.width;
        int h = this.height;

        // 1. Cielo Cósmico Dinámico con nebulosas y constelaciones
        renderCosmicAbyssSky(graphics, w, h);

        // 2. Barra Superior (Header Genshin: Salir, Estrellas Totales, Título, Divisas)
        renderTopBar(graphics, mouseX, mouseY, w);

        // 3. Pestañas Principales (Corredor del Abismo / Espiral Lunar)
        int tabY = 36;
        int tabH = 22;
        renderTabs(graphics, mouseX, mouseY, tabY, tabH, w);

        // 4. Medidas dinámicas con proporciones ideales (Las puertas dominan el centro)
        int bottomPad = 8;
        int detailsH = Math.min(132, Math.max(108, (int) (h * 0.30)));
        int detailsY = h - detailsH - bottomPad;

        int doorsY = tabY + tabH + 8;
        int doorH = detailsY - doorsY - 8;

        // Renderizado de la Vitrina de Puertas de Dominio
        renderDoorsShowcase(graphics, mouseX, mouseY, doorsY, doorH, w);

        // 5. Panel Inferior de Información y Desafío
        renderFloorDetailPanel(graphics, mouseX, mouseY, 16, detailsY, w - 32, detailsH);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    // ==========================================
    // 1. FONDO CÓSMICO DINÁMICO (MUSK REEF SKY)
    // ==========================================
    private void renderCosmicAbyssSky(GuiGraphics graphics, int w, int h) {
        long time = System.currentTimeMillis();

        // Degradado abisal de medianoche cósmica
        graphics.fillGradient(0, 0, w, h, 0xFF02050E, 0xFF0B1124);

        // Resplandor celestial astral difuso en el horizonte superior
        int rayCenterX = w / 2;
        for (int r = 0; r < 5; r++) {
            int rayOffset = (r - 2) * (w / 4);
            int x1 = rayCenterX + rayOffset - 50;
            int x2 = rayCenterX + rayOffset + 50;
            graphics.fillGradient(x1, 0, x2, h, 0x087DD3FC, 0x00000000);
        }

        // Nebulosas ondulantes en el fondo
        float wave = (time % 40000L) / 40000.0F * w;
        for (int i = -1; i <= 1; i++) {
            int cloudX = (int) (wave + i * w);
            graphics.fillGradient(cloudX, h - 160, cloudX + w, h, 0x121E3A8A, 0x00000000);
            graphics.fillGradient(w - cloudX, h - 120, w - cloudX + w, h, 0x10581C87, 0x00000000);
        }

        // Constelaciones conectadas por líneas sutiles de polvo estelar
        int[][] constStars = {
                {w / 10, 50}, {w / 7, 30}, {w / 5, 65}, {w / 4, 38},
                {w * 3 / 4, 40}, {w * 4 / 5, 70}, {w * 9 / 10, 35}
        };
        for (int i = 0; i < constStars.length - 1; i++) {
            int x1 = constStars[i][0];
            int y1 = constStars[i][1];
            int x2 = constStars[i + 1][0];
            int y2 = constStars[i + 1][1];
            if (Math.abs(x1 - x2) < w / 3) {
                drawThinLine(graphics, x1, y1, x2, y2, 0x187DD3FC);
            }
        }

        // Estrellas parpadeantes
        for (int i = 0; i < constStars.length; i++) {
            int sx = constStars[i][0];
            int sy = constStars[i][1];
            float twinkle = (float) Math.sin((time / 300.0) + i) * 0.5F + 0.5F;
            int alpha = (int) (100 + twinkle * 155);
            graphics.fill(sx - 1, sy, sx + 2, sy + 1, (alpha << 24) | 0x00BAE6FD);
            graphics.fill(sx, sy - 1, sx + 1, sy + 2, (alpha << 24) | 0x00BAE6FD);
        }
    }

    private void drawThinLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps <= 0) return;
        float dx = (x2 - x1) / (float) steps;
        float dy = (y2 - y1) / (float) steps;
        float cx = x1;
        float cy = y1;
        for (int s = 0; s <= steps; s += 2) {
            graphics.fill((int) cx, (int) cy, (int) cx + 1, (int) cy + 1, color);
            cx += dx * 2;
            cy += dy * 2;
        }
    }

    // ==========================================
    // 2. BARRA SUPERIOR (HEADER GENSHIN)
    // ==========================================
    private void renderTopBar(GuiGraphics graphics, int mouseX, int mouseY, int w) {
        int barH = 34;
        graphics.fill(0, 0, w, barH, 0xEE080C18);
        graphics.fill(0, barH - 1, w, barH, 0x66C3A462); // Divisor dorado sutil inferior

        // 1. Botón Salir
        int exitX = 12;
        int exitY = 6;
        int exitW = 56;
        int exitH = 22;
        boolean hoverExit = mouseX >= exitX && mouseX <= exitX + exitW && mouseY >= exitY && mouseY <= exitY + exitH;
        graphics.fill(exitX, exitY, exitX + exitW, exitY + exitH, hoverExit ? 0xDD2D3748 : 0x99171923);
        graphics.renderOutline(exitX, exitY, exitW, exitH, hoverExit ? 0xFFFFD700 : 0xFF4A5568);
        graphics.drawCenteredString(this.font, "✕ Salir", exitX + exitW / 2, exitY + 7, hoverExit ? 0xFFFFE082 : 0xFFCBD5E1);

        // 2. Cápsula de Estrellas Totales (⭐ X / 108)
        int starBadgeX = exitX + exitW + 8;
        int starBadgeW = 94;
        graphics.fill(starBadgeX, exitY, starBadgeX + starBadgeW, exitY + exitH, 0xEE1E1B4B);
        graphics.renderOutline(starBadgeX, exitY, starBadgeW, exitH, 0xFF818CF8);
        graphics.drawCenteredString(this.font, "⭐ " + this.totalStars + " / 108", starBadgeX + starBadgeW / 2, exitY + 7, 0xFFFFD700);

        // 3. Título Central Sagrado
        String title = "✦ ESPIRAL DEL ABISMO ✦";
        int titleW = this.font.width(title);
        graphics.drawString(this.font, title, (w - titleW) / 2, 6, 0xFFFFDF88, true);

        String sub = "Arrecife de Musk · Dominio del Abismo Sagrado";
        int subW = this.font.width(sub);
        graphics.drawString(this.font, sub, (w - subW) / 2, 18, 0xFF94A3B8, false);

        // 4. Divisas en la esquina derecha
        int curX = w - 12;

        // Destinos Entrelazados
        int pillW = 60;
        curX -= pillW;
        ItemStack fateStack = SystemItems.INTERTWINED_FATE.isBound() ? new ItemStack(SystemItems.INTERTWINED_FATE.get()) : ItemStack.EMPTY;
        renderCurrencyPill(graphics, curX, exitY, pillW, exitH, fateStack, "✦ " + this.intertwinedFates, 0xFF80D8FF);

        // Protogemas
        curX -= (pillW + 6);
        ItemStack primoStack = SystemItems.PRIMOGEM.isBound() ? new ItemStack(SystemItems.PRIMOGEM.get()) : ItemStack.EMPTY;
        renderCurrencyPill(graphics, curX, exitY, pillW, exitH, primoStack, "" + this.primogems, 0xFFFFE082);

        // Monedas / Mora
        int moraPillW = 76;
        curX -= (moraPillW + 6);
        ItemStack moraStack = SystemItems.MORA.isBound() ? new ItemStack(SystemItems.MORA.get()) : ItemStack.EMPTY;
        renderCurrencyPill(graphics, curX, exitY, moraPillW, exitH, moraStack, formatNumber(this.coins), 0xFFFFD700);

        // Nivel del Jugador
        int lvlPillW = 56;
        curX -= (lvlPillW + 6);
        graphics.fill(curX, exitY, curX + lvlPillW, exitY + exitH, 0xEE0F172A);
        graphics.renderOutline(curX, exitY, lvlPillW, exitH, 0xFF38BDF8);
        graphics.drawCenteredString(this.font, "Nv. " + this.playerLevel, curX + lvlPillW / 2, exitY + 7, 0xFF38BDF8);
    }

    private void renderCurrencyPill(GuiGraphics graphics, int x, int y, int w, int h, ItemStack iconStack, String text, int color) {
        graphics.fill(x, y, x + w, y + h, 0xEE0B0F1A);
        graphics.renderOutline(x, y, w, h, 0xFF334155);
        if (!iconStack.isEmpty()) {
            graphics.renderItem(iconStack, x + 2, y + 3);
            graphics.drawString(this.font, text, x + 20, y + 7, color, false);
        } else {
            graphics.drawString(this.font, text, x + 6, y + 7, color, false);
        }
    }

    // ==========================================
    // 3. PESTAÑAS PRINCIPALES (TABS)
    // ==========================================
    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY, int tabY, int tabH, int w) {
        int tabW = 175;
        int startX = (w - (tabW * 2 + 8)) / 2;

        // Pestaña 1: Corredor del Abismo (1-8)
        boolean hoverTab0 = mouseX >= startX && mouseX <= startX + tabW && mouseY >= tabY && mouseY <= tabY + tabH;
        int bg0 = (selectedTab == 0) ? 0xEE1E3A8A : (hoverTab0 ? 0xAA1E293B : 0x770F172A);
        int border0 = (selectedTab == 0) ? 0xFFFFD700 : 0x55475569;
        graphics.fill(startX, tabY, startX + tabW, tabY + tabH, bg0);
        graphics.renderOutline(startX, tabY, tabW, tabH, border0);
        graphics.drawCenteredString(this.font, "◆ Corredor del Abismo (1-8)", startX + tabW / 2, tabY + 7, (selectedTab == 0) ? 0xFFFFFFFF : 0xFF94A3B8);

        // Indicador de triángulo activo debajo de la pestaña
        if (selectedTab == 0) {
            graphics.fill(startX + tabW / 2 - 4, tabY + tabH, startX + tabW / 2 + 4, tabY + tabH + 2, 0xFFFFD700);
        }

        // Pestaña 2: Espiral Lunar (9-12)
        int tab1X = startX + tabW + 8;
        boolean hoverTab1 = mouseX >= tab1X && mouseX <= tab1X + tabW && mouseY >= tabY && mouseY <= tabY + tabH;
        int bg1 = (selectedTab == 1) ? 0xEE4C1D95 : (hoverTab1 ? 0xAA1E293B : 0x770F172A);
        int border1 = (selectedTab == 1) ? 0xFFC084FC : 0x55475569;
        graphics.fill(tab1X, tabY, tab1X + tabW, tabY + tabH, bg1);
        graphics.renderOutline(tab1X, tabY, tabW, tabH, border1);
        graphics.drawCenteredString(this.font, "◇ Espiral Lunar (9-12)", tab1X + tabW / 2, tabY + 7, (selectedTab == 1) ? 0xFFFFFFFF : 0xFF94A3B8);

        if (selectedTab == 1) {
            graphics.fill(tab1X + tabW / 2 - 4, tabY + tabH, tab1X + tabW / 2 + 4, tabY + tabH + 2, 0xFFC084FC);
        }
    }

    // ==========================================
    // 4. VITRINA DE PUERTAS DE DOMINIO (SHOWCASE)
    // ==========================================
    private void renderDoorsShowcase(GuiGraphics graphics, int mouseX, int mouseY, int doorsY, int doorH, int w) {
        int minFloor, maxFloor;
        if (selectedTab == 0) {
            minFloor = (corridorPage == 0) ? 1 : 5;
            maxFloor = (corridorPage == 0) ? 4 : 8;
        } else {
            minFloor = 9;
            maxFloor = 12;
        }
        int count = 4; // 4 puertas por vitrina

        int maxDoorW = (w - 140) / count;
        int doorW = Math.max(82, Math.min((int) (doorH * 0.54), maxDoorW));
        int gap = Math.min(24, Math.max(10, (w - 90 - count * doorW) / (count + 1)));
        int totalDoorsW = count * doorW + (count - 1) * gap;
        int startX = (w - totalDoorsW) / 2;

        // Flechas de Paginación (solo para el Corredor del Abismo)
        if (selectedTab == 0) {
            int arrowW = 24;
            int arrowH = 46;
            int leftArrowX = startX - arrowW - 12;
            int arrowY = doorsY + (doorH - arrowH) / 2;
            boolean canGoLeft = (corridorPage > 0);
            boolean hoverLeft = canGoLeft && mouseX >= leftArrowX && mouseX <= leftArrowX + arrowW && mouseY >= arrowY && mouseY <= arrowY + arrowH;

            graphics.fill(leftArrowX, arrowY, leftArrowX + arrowW, arrowY + arrowH, hoverLeft ? 0xDD334155 : 0x771E293B);
            graphics.renderOutline(leftArrowX, arrowY, arrowW, arrowH, canGoLeft ? (hoverLeft ? 0xFFFFD700 : 0xFF64748B) : 0x33475569);
            graphics.drawCenteredString(this.font, "◀", leftArrowX + arrowW / 2, arrowY + 19, canGoLeft ? (hoverLeft ? 0xFFFFE082 : 0xFFFFFFFF) : 0xFF475569);

            int rightArrowX = startX + totalDoorsW + 12;
            boolean canGoRight = (corridorPage < 1);
            boolean hoverRight = canGoRight && mouseX >= rightArrowX && mouseX <= rightArrowX + arrowW && mouseY >= arrowY && mouseY <= arrowY + arrowH;

            graphics.fill(rightArrowX, arrowY, rightArrowX + arrowW, arrowY + arrowH, hoverRight ? 0xDD334155 : 0x771E293B);
            graphics.renderOutline(rightArrowX, arrowY, arrowW, arrowH, canGoRight ? (hoverRight ? 0xFFFFD700 : 0xFF64748B) : 0x33475569);
            graphics.drawCenteredString(this.font, "▶", rightArrowX + arrowW / 2, arrowY + 19, canGoRight ? (hoverRight ? 0xFFFFE082 : 0xFFFFFFFF) : 0xFF475569);
        }

        // Renderizado individual de las 4 Puertas Sagradas
        for (int i = 0; i < count; i++) {
            int floor = minFloor + i;
            int dx = startX + i * (doorW + gap);
            boolean isSelected = (floor == selectedFloor);
            boolean isHover = mouseX >= dx && mouseX <= dx + doorW && mouseY >= doorsY && mouseY <= doorsY + doorH;
            boolean isLocked = (floor > unlockedFloor);

            renderSingleDomainDoor(graphics, dx, doorsY, doorW, doorH, floor, isSelected, isHover, isLocked);
        }
    }

    /**
     * Renderiza una auténtica PUERTA DE DOMINIO CELESTIAL inspirada fielmente en Genshin Impact.
     */
    private void renderSingleDomainDoor(GuiGraphics graphics, int x, int y, int w, int h, int floor, boolean isSelected, boolean isHover, boolean isLocked) {
        long time = System.currentTimeMillis();
        int floorIdx = floor - 1;
        int stars = starsPerFloor[floorIdx];
        int recLevel = getRecommendedLevel(floor);

        // Elevación suave tridimensional si está seleccionada
        int drawY = isSelected ? (y - 5) : (isHover ? (y - 2) : y);
        int curH = isSelected ? (h + 5) : (isHover ? (h + 2) : h);

        // 1. Resplandor Celestial Aureolar exterior al estar seleccionada
        if (isSelected) {
            graphics.fillGradient(x - 6, drawY - 6, x + w + 6, drawY + curH + 6, 0x44FFE082, 0x00FFE082);
            graphics.fill(x - 3, drawY - 3, x + w + 3, drawY + curH + 3, 0x332563EB);

            // Esquineros dorados de filigrana Genshin
            int cg = 6;
            // Superior Izquierdo
            graphics.fill(x - 4, drawY - 4, x - 4 + cg, drawY - 2, 0xFFFFD700);
            graphics.fill(x - 4, drawY - 4, x - 2, drawY - 4 + cg, 0xFFFFD700);
            // Superior Derecho
            graphics.fill(x + w + 4 - cg, drawY - 4, x + w + 4, drawY - 2, 0xFFFFD700);
            graphics.fill(x + w + 2, drawY - 4, x + w + 4, drawY - 4 + cg, 0xFFFFD700);
            // Inferior Izquierdo
            graphics.fill(x - 4, drawY + curH + 2, x - 4 + cg, drawY + curH + 4, 0xFFFFD700);
            graphics.fill(x - 4, drawY + curH + 4 - cg, x - 2, drawY + curH + 4, 0xFFFFD700);
            // Inferior Derecho
            graphics.fill(x + w + 4 - cg, drawY + curH + 2, x + w + 4, drawY + curH + 4, 0xFFFFD700);
            graphics.fill(x + w + 2, drawY + curH + 4 - cg, x + w + 4, drawY + curH + 4, 0xFFFFD700);
        }

        // 2. FRONTÓN Y ARCO SUPERIOR DE PIEDRA LABRADA
        int archH = Math.max(34, (int) (curH * 0.22));
        int stoneBg = isSelected ? 0xFF2D3748 : 0xFF1A202C;
        int stoneBorder = isSelected ? 0xFFFFD700 : (isHover ? 0xFF93C5FD : 0xFF4A5568);

        // Nivel 1 del frontón: cornisa de base ensanchada
        graphics.fill(x - 2, drawY + archH - 6, x + w + 2, drawY + archH, 0xFF334155);
        graphics.renderOutline(x - 2, drawY + archH - 6, w + 4, 6, stoneBorder);

        // Nivel 2 del frontón: cuerpo principal del frontón
        graphics.fill(x, drawY + 8, x + w, drawY + archH - 6, stoneBg);
        graphics.renderOutline(x, drawY + 8, w, archH - 14, stoneBorder);

        // Dientes de piedra / Moldura denticulada decorativa
        for (int d = x + 4; d < x + w - 4; d += 6) {
            graphics.fill(d, drawY + archH - 10, d + 3, drawY + archH - 7, isSelected ? 0xFF64748B : 0xFF334155);
        }

        // Nivel 3 del frontón: cresta piramidal superior
        graphics.fill(x + 10, drawY, x + w - 10, drawY + 8, 0xFF242E3D);
        graphics.renderOutline(x + 10, drawY, w - 20, 8, stoneBorder);

        // Bisel de luz superior
        graphics.fill(x + 11, drawY + 1, x + w - 11, drawY + 2, 0xFF94A3B8);

        // Medallón Cenital Celestial con Número Romano
        int medW = 30;
        int medH = 18;
        int medX = x + (w - medW) / 2;
        int medY = drawY + 5;
        graphics.fill(medX, medY, medX + medW, medY + medH, isSelected ? 0xFF92400E : 0xFF1E1B4B);
        graphics.renderOutline(medX, medY, medW, medH, isSelected ? 0xFFFFD700 : 0xFF818CF8);

        // Alas ornamentales a los lados del medallón
        graphics.fill(medX - 4, medY + 6, medX, medY + 12, isSelected ? 0xFFFFD700 : 0xFF64748B);
        graphics.fill(medX + medW, medY + 6, medX + medW + 4, medY + 12, isSelected ? 0xFFFFD700 : 0xFF64748B);

        String roman = (floor >= 1 && floor <= 12) ? ROMAN_NUMERALS[floor - 1] : String.valueOf(floor);
        graphics.drawCenteredString(this.font, roman, medX + medW / 2, medY + 5, isSelected ? 0xFFFFDF88 : 0xFFF1F5F9);

        // 3. COLUMNAS GEMELAS ESTRIADAS (TWIN FLUTED COLUMNS)
        int pillarW = Math.max(10, (int) (w * 0.13));
        int bodyY = drawY + archH;
        int bodyH = curH - archH - 36;

        renderFlutedPillar(graphics, x, bodyY, pillarW, bodyH, stoneBorder, isSelected, floor, time);
        renderFlutedPillar(graphics, x + w - pillarW, bodyY, pillarW, bodyH, stoneBorder, isSelected, floor, time);

        // 4. VANO INTERIOR DEL PORTAL (APERTURE & VORTEX)
        int coreX = x + pillarW;
        int coreW = w - pillarW * 2;
        int coreY = bodyY;
        int coreH = bodyH;

        if (!isLocked) {
            // --- VÓRTICE CÓSMICO ACTIVO (UNLOCKED PORTAL) ---
            int topCol = (floor >= 9) ? 0xFF0A021C : 0xFF02071A;
            int botCol = (floor >= 9) ? 0xFF4A044E : 0xFF1D4ED8;
            graphics.fillGradient(coreX, coreY, coreX + coreW, coreY + coreH, topCol, botCol);

            // Bóveda en arco de medio punto (esquinas superiores redondeadas)
            graphics.fill(coreX, coreY, coreX + 4, coreY + 4, 0xFF1E293B);
            graphics.fill(coreX + coreW - 4, coreY, coreX + coreW, coreY + 4, 0xFF1E293B);
            graphics.fill(coreX, coreY, coreX + 2, coreY + 6, 0xFF1E293B);
            graphics.fill(coreX + coreW - 2, coreY, coreX + coreW, coreY + 6, 0xFF1E293B);

            // Órbitas elípticas celestiales en perspectiva 3D
            int centerX = coreX + coreW / 2;
            int centerY = coreY + coreH / 2;

            double pulse = Math.sin(time / 400.0 + floor);
            int ringAlpha = (int) (50 + pulse * 25);
            int ringCol = (ringAlpha << 24) | ((floor >= 9) ? 0x00C084FC : 0x007DD3FC);

            // Anillo elíptico exterior e interior
            drawDottedEllipse(graphics, centerX, centerY, (int) (coreW * 0.40), (int) (coreH * 0.28), ringCol);
            drawDottedEllipse(graphics, centerX, centerY, (int) (coreW * 0.22), (int) (coreH * 0.16), ringCol);

            // Triqueta Sagrada de Dominio en el vórtice
            int triAlpha = (int) (70 + Math.sin(time / 500.0 + floor) * 35);
            int triColor = (triAlpha << 24) | ((floor >= 9) ? 0x00E879F9 : 0x00FDE047);
            drawTriquetraSymbol(graphics, centerX, centerY, (int) (coreW * 0.44), triColor);

            // Estrellas en rotación orbital
            for (int s = 0; s < 5; s++) {
                double angle = (time / 700.0) + (s * (Math.PI * 2 / 5)) + floor;
                double radX = (coreW * 0.32);
                double radY = (coreH * 0.22);
                int px = centerX + (int) (Math.cos(angle) * radX);
                int py = centerY + (int) (Math.sin(angle) * radY);
                int sCol = (s % 2 == 0) ? 0xFFFFFFFF : ((floor >= 9) ? 0xFFF0ABFC : 0xFFBAE6FD);
                graphics.fill(px, py, px + 2, py + 2, sCol);
            }

            // Núcleo Estelar / Singularity Flash
            graphics.fill(centerX - 8, centerY, centerX + 9, centerY + 1, 0x88FFFFFF); // Rayo horizontal
            graphics.fill(centerX, centerY - 6, centerX + 1, centerY + 7, 0x88FFFFFF); // Rayo vertical
            graphics.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, 0xFFFFFFFF); // Núcleo
        } else {
            // --- PUERTA SELLADA ANCESTRAL (LOCKED PORTAL) ---
            graphics.fill(coreX, coreY, coreX + coreW, coreY + coreH, 0xFF080D18);

            // Esquinas arqueadas de piedra
            graphics.fill(coreX, coreY, coreX + 4, coreY + 4, 0xFF1E293B);
            graphics.fill(coreX + coreW - 4, coreY, coreX + coreW, coreY + 4, 0xFF1E293B);

            // Junta central entre las dos hojas de piedra labrada
            int midX = coreX + coreW / 2;
            graphics.fill(midX - 1, coreY, midX, coreY + coreH, 0xFF020408);
            graphics.fill(midX, coreY, midX + 1, coreY + coreH, 0xFF1E293B);

            // Círculos y Triqueta grabados en la piedra sellada
            int centerY = coreY + coreH / 2;
            drawTriquetraSymbol(graphics, midX, centerY, (int) (coreW * 0.46), 0x3364748B);
            drawDottedEllipse(graphics, midX, centerY, (int) (coreW * 0.38), (int) (coreH * 0.28), 0x2264748B);

            // Cadenas de hierro cruzadas diagonales
            drawChainDiagonal(graphics, coreX, coreY, coreX + coreW, coreY + coreH);
            drawChainDiagonal(graphics, coreX + coreW, coreY, coreX, coreY + coreH);

            // Candado Rúnico Carmesí Monumental en la intersección
            int lockW = 20;
            int lockH = 16;
            int lockX = midX - lockW / 2;
            int lockY = centerY - lockH / 2 - 2;

            // Arco del candado
            graphics.fill(lockX + 5, lockY - 5, lockX + lockW - 5, lockY, 0xFF64748B);
            graphics.fill(lockX + 7, lockY - 3, lockX + lockW - 7, lockY, 0xFF080D18);

            // Cuerpo del candado
            graphics.fill(lockX, lockY, lockX + lockW, lockY + lockH, 0xFF7F1D1D);
            graphics.renderOutline(lockX, lockY, lockW, lockH, 0xFFDC2626);

            // Gema / Ojo arcano carmesí palpitante
            int eyeAlpha = (int) (180 + Math.sin(time / 250.0 + floor) * 75);
            graphics.fill(lockX + lockW / 2 - 2, lockY + lockH / 2 - 2, lockX + lockW / 2 + 2, lockY + lockH / 2 + 2, (eyeAlpha << 24) | 0x00F87171);

            graphics.drawCenteredString(this.font, "§cBLOQUEADO", midX, lockY + lockH + 3, 0xFFEF4444);
            graphics.drawCenteredString(this.font, "§7Piso " + (floor - 1) + " req.", midX, lockY + lockH + 13, 0xFF64748B);
        }

        // 5. PEDESTAL / ESTRADO DE PIEDRA INFERIOR
        int baseH = 36;
        int baseY = drawY + curH - baseH;

        // Cornisa superior del pedestal
        graphics.fill(x - 1, baseY, x + w + 1, baseY + 4, 0xFF334155);
        graphics.renderOutline(x - 1, baseY, w + 2, 4, stoneBorder);

        // Bloque del pedestal
        graphics.fill(x, baseY + 4, x + w, baseY + baseH, 0xFF0F172A);
        graphics.renderOutline(x, baseY + 4, w, baseH - 4, stoneBorder);

        // Placa del Piso
        String floorTitle = "✦ PISO " + floor + " ✦";
        graphics.drawCenteredString(this.font, floorTitle, x + w / 2, baseY + 6, isLocked ? 0xFF64748B : (isSelected ? 0xFFFFDF88 : 0xFFE2E8F0));

        if (!isLocked) {
            // Gemas de las 3 Cámaras (★ ★ ★)
            // Sala 1: stars >= 1; Sala 2: stars >= 4; Sala 3: stars >= 7
            int gemY = baseY + 16;
            int gemSpacing = 16;
            int g1X = x + w / 2 - gemSpacing;
            int g2X = x + w / 2;
            int g3X = x + w / 2 + gemSpacing;

            renderChamberStarGem(graphics, g1X, gemY, stars >= 1);
            renderChamberStarGem(graphics, g2X, gemY, stars >= 4);
            renderChamberStarGem(graphics, g3X, gemY, stars >= 7);

            // Contador de Estrellas Total del Piso (⭐ X / 9)
            String starStr = "⭐ " + stars + " / 9";
            int starCol = (stars >= 9) ? 0xFFFFD700 : ((stars > 0) ? 0xFF38BDF8 : 0xFF94A3B8);
            graphics.drawCenteredString(this.font, starStr, x + w / 2, baseY + 26, starCol);
        } else {
            graphics.drawCenteredString(this.font, "Nv. Sugerido: " + recLevel, x + w / 2, baseY + 18, 0xFF475569);
        }

        // 6. Indicador Luminoso de Recompensa Lista
        if (!isLocked && stars >= 3 && !claimedRewards[floorIdx]) {
            int tagW = 42;
            int tagH = 11;
            int tagX = x + (w - tagW) / 2;
            int tagY = drawY - 6;
            graphics.fill(tagX, tagY, tagX + tagW, tagY + tagH, 0xFFB45309);
            graphics.renderOutline(tagX, tagY, tagW, tagH, 0xFFFFD700);
            graphics.drawCenteredString(this.font, "★ ¡PREMIO!", tagX + tagW / 2, tagY + 2, 0xFFFFFFFF);
        }
    }

    /**
     * Renderiza una columna cilíndrica con acanaladuras estriadas y canal rúnico luminiscente.
     */
    private void renderFlutedPillar(GuiGraphics graphics, int px, int py, int pw, int ph, int outlineCol, boolean isSelected, int floor, long time) {
        // Capitel superior
        graphics.fill(px - 1, py, px + pw + 1, py + 5, 0xFF475569);
        graphics.renderOutline(px - 1, py, pw + 2, 5, outlineCol);

        // Fuste con estriado 3D
        graphics.fill(px, py + 5, px + pw, py + ph - 5, 0xFF1E293B);
        // Canaladura 1 (resplandor izquierdo)
        graphics.fill(px + 1, py + 5, px + 2, py + ph - 5, 0xFF64748B);
        // Canaladura 2 (sombra interna)
        graphics.fill(px + 3, py + 5, px + 4, py + ph - 5, 0xFF0F172A);
        // Canaladura 3 (sombra derecha)
        graphics.fill(px + pw - 2, py + 5, px + pw - 1, py + ph - 5, 0xFF0F172A);

        // Canal rúnico central palpitante
        int runeCol = (floor >= 9) ? 0x88C084FC : 0x8838BDF8;
        for (int ry = py + 8; ry < py + ph - 10; ry += 8) {
            graphics.fill(px + pw / 2, ry, px + pw / 2 + 1, ry + 3, runeCol);
        }

        // Basa inferior
        graphics.fill(px - 1, py + ph - 5, px + pw + 1, py + ph, 0xFF475569);
        graphics.renderOutline(px - 1, py + ph - 5, pw + 2, 5, outlineCol);
    }

    /**
     * Renderiza una gema de estrella con relieve de oro si está conseguida, o un hueco oscuro si no.
     */
    private void renderChamberStarGem(GuiGraphics graphics, int cx, int cy, boolean lit) {
        if (lit) {
            // Corona exterior de oro
            graphics.fill(cx - 3, cy, cx + 4, cy + 1, 0xFFFFD700);
            graphics.fill(cx, cy - 3, cx + 1, cy + 4, 0xFFFFD700);
            graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFFFFF); // Núcleo radiante blanco
        } else {
            // Hueco de gema vacío de bronce oscuro
            graphics.fill(cx - 2, cy - 2, cx + 3, cy + 3, 0xFF1E293B);
            graphics.renderOutline(cx - 2, cy - 2, 5, 5, 0xFF475569);
        }
    }

    private void drawTriquetraSymbol(GuiGraphics graphics, int cx, int cy, int size, int color) {
        int r = Math.max(6, size / 2);
        int topY = cy - (int) (r * 0.45);
        int blX = cx - (int) (r * 0.42);
        int brX = cx + (int) (r * 0.42);
        int botY = cy + (int) (r * 0.30);

        // 3 lóbulos entrelazados de la Triqueta de Teyvat
        drawDottedEllipse(graphics, cx, topY, (int) (r * 0.55), (int) (r * 0.55), color);
        drawDottedEllipse(graphics, blX, botY, (int) (r * 0.55), (int) (r * 0.55), color);
        drawDottedEllipse(graphics, brX, botY, (int) (r * 0.55), (int) (r * 0.55), color);
        // Círculo concéntrico celestial
        drawDottedEllipse(graphics, cx, cy, (int) (r * 0.85), (int) (r * 0.85), color);
    }

    private void drawDottedEllipse(GuiGraphics graphics, int cx, int cy, int rx, int ry, int color) {
        int steps = 18;
        for (int i = 0; i < steps; i++) {
            double angle = (i * Math.PI * 2) / steps;
            int px = cx + (int) (Math.cos(angle) * rx);
            int py = cy + (int) (Math.sin(angle) * ry);
            graphics.fill(px, py, px + 1, py + 1, color);
        }
    }

    private void drawChainDiagonal(GuiGraphics graphics, int x1, int y1, int x2, int y2) {
        int steps = 16;
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            int px = (int) (x1 + (x2 - x1) * t);
            int py = (int) (y1 + (y2 - y1) * t);
            graphics.fill(px - 1, py - 1, px + 2, py + 2, 0xFF475569);
            graphics.fill(px, py, px + 1, py + 1, 0xFF94A3B8); // Brillo metálico del eslabón
        }
    }

    // ==========================================
    // 5. PANEL INFERIOR: DETALLES DE COMBATE
    // ==========================================
    private void renderFloorDetailPanel(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xEE0B0F1A);
        graphics.renderOutline(x, y, w, h, 0xFF2D3748);

        int floorIdx = selectedFloor - 1;
        int stars = starsPerFloor[floorIdx];
        boolean isLocked = (selectedFloor > unlockedFloor);
        int recLevel = getRecommendedLevel(selectedFloor);

        int pad = 8;
        int leftColW = (int) ((w - pad * 3) * 0.48);
        int rightColW = w - pad * 3 - leftColW;
        int rightColX = x + pad + leftColW + pad;

        // --- COLUMNA IZQUIERDA: ANOMALÍA DE LAS LÍNEAS LEY ---
        int leftY = y + pad;
        int leftH = h - pad * 2;
        graphics.fill(x + pad, leftY, x + pad + leftColW, leftY + leftH, 0xEE111827);
        graphics.renderOutline(x + pad, leftY, leftColW, leftH, 0xFF374151);

        // Emblema y Título Elemental
        String badge = getElementalBadge(selectedFloor);
        int badgeCol = getElementalColor(selectedFloor);
        graphics.drawString(this.font, badge, x + pad + 8, leftY + 6, badgeCol, true);

        // Líneas descriptivas de la anomalía
        String disorderDesc = getLeyLineDisorderText(selectedFloor);
        List<net.minecraft.util.FormattedCharSequence> lines = this.font.split(Component.literal(disorderDesc), leftColW - 16);
        int lineY = leftY + 18;
        for (var line : lines) {
            graphics.drawString(this.font, line, x + pad + 8, lineY, 0xFFE5E7EB, false);
            lineY += 9;
        }

        // Consejo Táctico
        String tip = "💡 Táctica: " + getTacticalTip(selectedFloor);
        List<net.minecraft.util.FormattedCharSequence> tipLines = this.font.split(Component.literal(tip), leftColW - 16);
        int tipY = Math.max(lineY + 2, leftY + leftH - 26);
        for (var tLine : tipLines) {
            if (tipY < leftY + leftH - 12) {
                graphics.drawString(this.font, tLine, x + pad + 8, tipY, 0xFFFDE68A, false);
                tipY += 9;
            }
        }

        // Medidor de Nivel: Nivel Sugerido vs Nivel del Jugador
        int gaugeY = leftY + leftH - 11;
        String lvlText = "Nivel: Nv. " + recLevel + (this.playerLevel >= recLevel ? " §a(Cumplido)" : " §c(Bajo Nivel)");
        graphics.drawString(this.font, lvlText, x + pad + 8, gaugeY, (this.playerLevel >= recLevel) ? 0xFF4ADE80 : 0xFFF87171, false);

        // Barra de nivel
        int barX = x + pad + leftColW - 74;
        int barW = 66;
        int barH = 5;
        graphics.fill(barX, gaugeY + 2, barX + barW, gaugeY + 2 + barH, 0xFF1F2937);
        int fillW = Math.min(barW, Math.max(4, (barW * this.playerLevel) / recLevel));
        graphics.fill(barX, gaugeY + 2, barX + fillW, gaugeY + 2 + barH, (this.playerLevel >= recLevel) ? 0xFF22C55E : 0xFFEF4444);
        graphics.renderOutline(barX, gaugeY + 2, barW, barH, 0xFF4B5563);

        // --- COLUMNA DERECHA: SALAS DE COMBATE Y ACCIÓN ---
        int rightY = y + pad;

        // Pestañas de Salas (Sala 1, 2, 3)
        int chamberTabW = (rightColW - 8) / 3;
        int chamberTabH = 18;
        for (int c = 1; c <= 3; c++) {
            int cx = rightColX + (c - 1) * (chamberTabW + 4);
            boolean isCSelected = (c == selectedChamber);
            boolean isCHover = mouseX >= cx && mouseX <= cx + chamberTabW && mouseY >= rightY && mouseY <= rightY + chamberTabH;

            graphics.fill(cx, rightY, cx + chamberTabW, rightY + chamberTabH, isCSelected ? 0xEE1E3A8A : (isCHover ? 0x99374151 : 0x771F2937));
            graphics.renderOutline(cx, rightY, chamberTabW, chamberTabH, isCSelected ? 0xFF38BDF8 : 0x334B5563);
            graphics.drawCenteredString(this.font, "Sala " + c, cx + chamberTabW / 2, rightY + 5, isCSelected ? 0xFFFFFFFF : 0xFF9CA3AF);
        }

        int chamberBoxY = rightY + chamberTabH + 3;
        int actionRowH = 22;
        int chamberBoxH = leftH - chamberTabH - actionRowH - 7;
        graphics.fill(rightColX, chamberBoxY, rightColX + rightColW, chamberBoxY + chamberBoxH, 0xEE111827);
        graphics.renderOutline(rightColX, chamberBoxY, rightColW, chamberBoxH, 0xFF374151);

        // Monstruos y Objetivos de la Cámara
        if (selectedFloor >= 5) {
            graphics.drawString(this.font, "§6✦ 1ª Mitad: §f" + getChamberHalfMonsters(selectedFloor, selectedChamber, 1), rightColX + 6, chamberBoxY + 3, 0xFFE5E7EB, false);
            graphics.drawString(this.font, "§b✦ 2ª Mitad: §f" + getChamberHalfMonsters(selectedFloor, selectedChamber, 2), rightColX + 6, chamberBoxY + 12, 0xFFE5E7EB, false);
            graphics.drawString(this.font, "§eMetas de Tiempo: §f⭐⭐⭐ ≥180s  §7|  §f⭐⭐ ≥120s  §7|  §f⭐ Superar Sala", rightColX + 6, chamberBoxY + 21, 0xFFD1D5DB, false);
            graphics.drawString(this.font, "§7Desafío en 2 etapas consecutivas (1ª y 2ª Mitad).", rightColX + 6, chamberBoxY + 30, 0xFF9CA3AF, false);
        } else {
            graphics.drawString(this.font, "§bMonstruos: §f" + getChamberMonsters(selectedFloor, selectedChamber), rightColX + 6, chamberBoxY + 4, 0xFFE5E7EB, false);
            graphics.drawString(this.font, "§eMetas de Tiempo: §f⭐⭐⭐ ≥180s  §7|  §f⭐⭐ ≥120s  §7|  §f⭐ Superar Sala", rightColX + 6, chamberBoxY + 14, 0xFFD1D5DB, false);
            graphics.drawString(this.font, "§7Límite de tiempo: 5 minutos (300s). Guarda tu definitiva para los élites.", rightColX + 6, chamberBoxY + 24, 0xFF9CA3AF, false);
        }

        // Barra Inferior de Acción (Recompensas y Desafiar Piso)
        int actionRowY = y + h - pad - actionRowH;
        int rewardBoxW = (int) (rightColW * 0.58);
        int challengeBtnW = rightColW - rewardBoxW - 6;

        // Caja / Botón de Reclamar Recompensas
        boolean alreadyClaimed = claimedRewards[floorIdx];
        boolean canClaim = (stars >= 3 && !alreadyClaimed);
        boolean hoverClaim = mouseX >= rightColX && mouseX <= rightColX + rewardBoxW && mouseY >= actionRowY && mouseY <= actionRowY + actionRowH;

        int claimBg = alreadyClaimed ? 0xEE14532D : (canClaim ? (hoverClaim ? 0xFFD97706 : 0xFFB45309) : 0xEE1F2937);
        int claimBorder = alreadyClaimed ? 0xFF22C55E : (canClaim ? 0xFFFFD700 : 0xFF374151);
        graphics.fill(rightColX, actionRowY, rightColX + rewardBoxW, actionRowY + actionRowH, claimBg);
        graphics.renderOutline(rightColX, actionRowY, rewardBoxW, actionRowH, claimBorder);

        String claimText = alreadyClaimed ? "✓ Premios Reclamados" : (canClaim ? "★ RECLAMAR 300 ✦ ★" : "Premios: 300✦ · 20k Mora (≥3⭐)");
        int claimCol = alreadyClaimed ? 0xFF86EFAC : (canClaim ? 0xFFFFFFFF : 0xFF9CA3AF);
        graphics.drawCenteredString(this.font, claimText, rightColX + rewardBoxW / 2, actionRowY + 7, claimCol);

        // Botón Monumental: DESAFIAR PISO
        int chX = rightColX + rewardBoxW + 6;
        boolean canChallenge = !isLocked;
        boolean hoverChallenge = canChallenge && mouseX >= chX && mouseX <= chX + challengeBtnW && mouseY >= actionRowY && mouseY <= actionRowY + actionRowH;

        int actBg = isLocked ? 0xEE1F2937 : (hoverChallenge ? 0xFF2563EB : 0xFF1D4ED8);
        int actBorder = isLocked ? 0xFF4B5563 : (hoverChallenge ? 0xFFFFD700 : 0xFF60A5FA);
        graphics.fill(chX, actionRowY, chX + challengeBtnW, actionRowY + actionRowH, actBg);
        graphics.renderOutline(chX, actionRowY, challengeBtnW, actionRowH, actBorder);
        if (canChallenge) {
            graphics.renderOutline(chX + 1, actionRowY + 1, challengeBtnW - 2, actionRowH - 2, 0x44FFFFFF);
        }

        String actText = isLocked ? "🔒 Bloqueado" : ("⚔ DESAFIAR " + selectedFloor);
        int actCol = isLocked ? 0xFF9CA3AF : 0xFFFFFFFF;
        graphics.drawCenteredString(this.font, actText, chX + challengeBtnW / 2, actionRowY + 7, actCol);
    }

    // ==========================================
    // 6. GESTIÓN DE CLICS DEL RATÓN
    // ==========================================
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int w = this.width;
        int h = this.height;

        // Clic en Salir
        if (mouseX >= 12 && mouseX <= 68 && mouseY >= 6 && mouseY <= 28) {
            playClickSound();
            this.onClose();
            return true;
        }

        // Clic en Pestañas (Corredor vs Espiral Lunar)
        int tabY = 36;
        int tabH = 22;
        int tabW = 175;
        int startX = (w - (tabW * 2 + 8)) / 2;

        if (mouseX >= startX && mouseX <= startX + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
            if (this.selectedTab != 0) {
                this.selectedTab = 0;
                this.selectedFloor = (corridorPage == 0) ? 1 : 5;
                playClickSound();
            }
            return true;
        }
        int tab1X = startX + tabW + 8;
        if (mouseX >= tab1X && mouseX <= tab1X + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
            if (this.selectedTab != 1) {
                this.selectedTab = 1;
                this.selectedFloor = 9;
                playClickSound();
            }
            return true;
        }

        // Medidas de las puertas idénticas a render
        int bottomPad = 8;
        int detailsH = Math.min(132, Math.max(108, (int) (h * 0.30)));
        int detailsY = h - detailsH - bottomPad;
        int doorsY = tabY + tabH + 8;
        int doorH = detailsY - doorsY - 8;

        int count = 4;
        int maxDoorW = (w - 140) / count;
        int doorW = Math.max(82, Math.min((int) (doorH * 0.54), maxDoorW));
        int gap = Math.min(24, Math.max(10, (w - 90 - count * doorW) / (count + 1)));
        int totalDoorsW = count * doorW + (count - 1) * gap;
        int doorsStartX = (w - totalDoorsW) / 2;

        // Clic en Flechas de Paginación de Puertas (◀ y ▶)
        if (selectedTab == 0) {
            int arrowW = 24;
            int arrowH = 46;
            int leftArrowX = doorsStartX - arrowW - 12;
            int arrowY = doorsY + (doorH - arrowH) / 2;

            if (mouseX >= leftArrowX && mouseX <= leftArrowX + arrowW && mouseY >= arrowY && mouseY <= arrowY + arrowH) {
                if (corridorPage > 0) {
                    corridorPage--;
                    selectedFloor = (corridorPage == 0) ? 1 : 5;
                    playClickSound();
                }
                return true;
            }

            int rightArrowX = doorsStartX + totalDoorsW + 12;
            if (mouseX >= rightArrowX && mouseX <= rightArrowX + arrowW && mouseY >= arrowY && mouseY <= arrowY + arrowH) {
                if (corridorPage < 1) {
                    corridorPage++;
                    selectedFloor = 5;
                    playClickSound();
                }
                return true;
            }
        }

        // Clic en las 4 PUERTAS DEL ABISMO
        int minFloor = (selectedTab == 0) ? ((corridorPage == 0) ? 1 : 5) : 9;
        for (int i = 0; i < 4; i++) {
            int floor = minFloor + i;
            int dx = doorsStartX + i * (doorW + gap);
            if (mouseX >= dx && mouseX <= dx + doorW && mouseY >= doorsY - 6 && mouseY <= doorsY + doorH + 6) {
                if (this.selectedFloor != floor) {
                    this.selectedFloor = floor;
                    playClickSound();
                }
                return true;
            }
        }

        // Clic en Pestañas de Salas (1, 2, 3)
        int pad = 8;
        int leftColW = (int) ((w - 32 - pad * 3) * 0.48);
        int rightColW = (w - 32) - pad * 3 - leftColW;
        int rightColX = 16 + pad + leftColW + pad;
        int rightY = detailsY + pad;

        int chamberTabW = (rightColW - 8) / 3;
        int chamberTabH = 18;
        for (int c = 1; c <= 3; c++) {
            int cx = rightColX + (c - 1) * (chamberTabW + 4);
            if (mouseX >= cx && mouseX <= cx + chamberTabW && mouseY >= rightY && mouseY <= rightY + chamberTabH) {
                if (this.selectedChamber != c) {
                    this.selectedChamber = c;
                    playClickSound();
                }
                return true;
            }
        }

        // Clic en Reclamar Recompensas
        int actionRowH = 22;
        int actionRowY = detailsY + detailsH - pad - actionRowH;
        int rewardBoxW = (int) (rightColW * 0.58);
        if (mouseX >= rightColX && mouseX <= rightColX + rewardBoxW && mouseY >= actionRowY && mouseY <= actionRowY + actionRowH) {
            int floorIdx = selectedFloor - 1;
            int stars = starsPerFloor[floorIdx];
            boolean alreadyClaimed = claimedRewards[floorIdx];
            if (stars >= 3 && !alreadyClaimed) {
                PacketDistributor.sendToServer(new ClaimAbyssRewardPayload(this.selectedFloor));
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                }
            } else {
                playClickSound();
            }
            return true;
        }

        // Clic en Desafiar Piso
        int challengeBtnW = rightColW - rewardBoxW - 6;
        int chX = rightColX + rewardBoxW + 6;
        if (mouseX >= chX && mouseX <= chX + challengeBtnW && mouseY >= actionRowY && mouseY <= actionRowY + actionRowH) {
            if (this.selectedFloor <= this.unlockedFloor) {
                PacketDistributor.sendToServer(new StartAbyssFloorPayload(this.selectedFloor));
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(ModSounds.ABYSS_ENTER.value(), 0.9F, 1.0F);
                }
                this.onClose();
            } else {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(SoundEvents.CHEST_LOCKED, 0.8F, 1.0F);
                }
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playClickSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(ModSounds.SYSTEM_CLICK.value(), 0.5F, 1.2F);
        }
    }

    // ==========================================
    // HELPERS: NIVELES, ANOMALÍAS Y EMBLEMAS
    // ==========================================
    private static int getRecommendedLevel(int floor) {
        return switch (floor) {
            case 1 -> 25;
            case 2 -> 40;
            case 3 -> 45;
            case 4 -> 50;
            case 5 -> 55;
            case 6 -> 60;
            case 7 -> 70;
            case 8 -> 70;
            case 9 -> 72;
            case 10 -> 80;
            case 11 -> 88;
            case 12 -> 95;
            default -> 25;
        };
    }

    private static String getElementalBadge(int floor) {
        return switch (floor) {
            case 1 -> "✦ ANOMALÍA ANEMO · Torbellino Sagrado";
            case 2 -> "🔥 ANOMALÍA PYRO · Furia Ígnea";
            case 3 -> "⚡ ANOMALÍA ELECTRO · Tormenta de Relámpagos";
            case 4 -> "❄ ANOMALÍA CRYO · Escarcha Polar";
            case 5 -> "💧 ANOMALÍA HYDRO · Marea Abisal";
            case 6 -> "🔶 ANOMALÍA GEO · Firmeza Inquebrantable";
            case 7 -> "💥 ANOMALÍA CRÍTICA · Ruptura Devastadora";
            case 8 -> "🩸 ANOMALÍA PRIMORDIAL · Ira de Berserker";
            case 9 -> "🌙 ESPIRAL LUNAR · Marea de Plasma Astral";
            case 10 -> "🌙 ESPIRAL LUNAR · Furia Estelar";
            case 11 -> "🌙 ESPIRAL LUNAR · Torrente Abisal";
            case 12 -> "🌙 ESPIRAL LUNAR · Cima del Vacío Celestial";
            default -> "✦ ANOMALÍA ELEMENTAL";
        };
    }

    private static int getElementalColor(int floor) {
        return switch (floor) {
            case 1 -> 0xFF22D3EE; // Anemo Cyan
            case 2 -> 0xFFFB923C; // Pyro Orange
            case 3 -> 0xFFC084FC; // Electro Purple
            case 4 -> 0xFF67E8F9; // Cryo Light Cyan
            case 5 -> 0xFF60A5FA; // Hydro Blue
            case 6 -> 0xFFFBBF24; // Geo Amber
            case 7 -> 0xFFFACC15; // Critical Yellow
            case 8 -> 0xFFF87171; // Berserker Red
            default -> 0xFFE879F9; // Lunar Violet
        };
    }

    private static String getTacticalTip(int floor) {
        return switch (floor) {
            case 1 -> "Aprovecha el torbellino para agrupar y empujar hordas.";
            case 2 -> "Usa armas imbuidas de fuego para desatar quemaduras voraces.";
            case 3 -> "La alta velocidad de ataque permite combos ininterrumpidos.";
            case 4 -> "La absorción de armadura reduce el daño letal de los élites.";
            case 5 -> "El flujo continuo restaura tu salud sin necesidad de comida.";
            case 6 -> "Los escudos absorben el impacto de ataques devastadores.";
            case 7 -> "Maximiza tu probabilidad crítica para causar ondas expansivas.";
            case 8 -> "Tu daño se duplica cuando tu salud está por debajo del 70%.";
            case 9, 10 -> "Derrota primero a los subordinados para dañar en cadena a los líderes.";
            case 11, 12 -> "Golpea puntos débiles y usa ataques cargados contra jefes.";
            default -> "Adapta tus armas y elementos a las debilidades del enemigo.";
        };
    }

    private static String getLeyLineDisorderText(int floor) {
        return switch (floor) {
            case 1 -> "El daño de Torbellino y reacciones Anemo aumenta un §a+50%§f. La velocidad de movimiento y altura de salto se duplican.";
            case 2 -> "El daño Pyro aumenta un §a+60%§f. Otorga inmunidad al fuego y ráfagas ígneas en golpes cuerpo a cuerpo.";
            case 3 -> "El daño Electro aumenta un §a+60%§f. La velocidad de ataque se multiplica, permitiendo combos letales continuos.";
            case 4 -> "El daño Cryo aumenta un §a+60%§f. La resistencia y dureza de armadura absorben un §a+30%§f de daño general.";
            case 5 -> "El daño Hydro aumenta un §a+60%§f. El torrente vital restaura continuamente un 3% de vida máxima cada 4 segundos.";
            case 6 -> "La potencia de escudos aumenta un §a+100%§f. Otorga absorción continua de daño e impacto sísmico demoledor.";
            case 7 -> "El Daño Crítico aumenta un §a+120%§f. Los golpes críticos asestan ondas expansivas que aturden a los monstruos.";
            case 8 -> "Cuando la vida es inferior al 70%, el ataque y velocidad se duplican, liberando furia imparable de combate.";
            case 9 -> "Los enemigos eliminados liberan ráfagas de plasma que dañan a monstruos adyacentes. Daño general +40%.";
            case 10 -> "Los golpes continuos acumulan cargas estelares, desatando una supernova elemental tras 10 impactos.";
            case 11 -> "El daño de habilidades y destrezas aumenta un §a+75%§f. El enfriamiento de ataques especiales disminuye.";
            case 12 -> "Desafío supremo de la cima. Los enemigos poseen defensas colosales, pero tus golpes críticos ignoran el 50% de su armadura.";
            default -> "Perturbación mágica en la arena del Abismo.";
        };
    }

    private static String getChamberMonsters(int floor, int chamber) {
        if (floor <= 3) {
            return switch (chamber) {
                case 1 -> "Hilichurls Berserkers · Slimes Menores";
                case 2 -> "Hilichurls con Escudo · Slimes Gigantes";
                case 3 -> "Hilichurls Campeones · Slimes Colosales";
                default -> "Monstruos de Teyvat";
            };
        } else if (floor <= 6) {
            return switch (chamber) {
                case 1 -> "Mitachurls con Hacha · Magos Menores";
                case 2 -> "Magos Pyro con Escudos · Mitachurls";
                case 3 -> "Guardián de las Ruinas · Magos del Abismo";
                default -> "Monstruos del Abismo";
            };
        } else {
            return switch (chamber) {
                case 1 -> "Escuadrón de Heraldos · Magos Cryo";
                case 2 -> "Nigromantes del Abismo · Mitachurls Élite";
                case 3 -> "Heraldos Supremos del Abismo · Guardián Élite";
                default -> "Élites de la Espiral Lunar";
            };
        }
    }

    private static String getChamberHalfMonsters(int floor, int chamber, int half) {
        if (floor <= 6) {
            if (half == 1) {
                return switch (chamber) {
                    case 1 -> "Slimes Pyro Grandes · Hilichurls Berserkers";
                    case 2 -> "Hilichurls con Escudo · Slimes Electro";
                    case 3 -> "Mitachurls Hacha Ardiente · Hilichurls Tiradores";
                    default -> "Vanguardia de Teyvat";
                };
            } else {
                return switch (chamber) {
                    case 1 -> "Monolito de las Líneas Ley · Mitachurls y Magos Cryo";
                    case 2 -> "Magos del Abismo Pyro · Mitachurl Escudero";
                    case 3 -> "Guardián de las Ruinas Ancestral · Magos del Abismo";
                    default -> "Élites de Refuerzo";
                };
            }
        } else {
            if (half == 1) {
                return switch (chamber) {
                    case 1 -> "Mitachurls Pesados · Slimes Elementales";
                    case 2 -> "Magos del Abismo Pyro & Cryo · Hilichurls Élite";
                    case 3 -> "Escuadrón de Magos del Abismo · Mitachurls Campeones";
                    default -> "Vanguardia de la Espiral Lunar";
                };
            } else {
                return switch (chamber) {
                    case 1 -> "Magos Cryo/Electro · Mitachurls Hacha";
                    case 2 -> "Heraldo del Abismo: Torrente · Magos Élite";
                    case 3 -> "Heraldo Supremo · Guardián de las Ruinas Titánico";
                    default -> "Jefes de la Cima Astral";
                };
            }
        }
    }

    private static String formatNumber(int val) {
        if (val >= 1_000_000) {
            return String.format(java.util.Locale.US, "%.1fM", val / 1_000_000.0);
        } else if (val >= 10_000) {
            return String.format(java.util.Locale.US, "%.1fk", val / 1000.0);
        }
        return String.valueOf(val);
    }
}
