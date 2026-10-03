package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Interfaz sagrada de la Guía de la Diosa Trilunar.
 * Reemplaza el spam de mensajes en chat con un libro místico visual
 * con pestañas de Lore, Estado dinámico de Autoridades y Localizador de Altares.
 */
public class TrilunarGuideScreen extends Screen {

    private static final int BOOK_WIDTH = 320;
    private static final int BOOK_HEIGHT = 224;

    private int activePage = 0; // 0: Crónica, 1: Autoridades, 2: Santuario
    private static final int TOTAL_PAGES = 3;

    public TrilunarGuideScreen() {
        super(Component.literal("Crónica Trilunar: El Despertar de Columbina"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) {
            if (activePage > 0) {
                activePage--;
                playTurnSound();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) {
            if (activePage < TOTAL_PAGES - 1) {
                activePage++;
                playTurnSound();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_1) {
            activePage = 0;
            playTurnSound();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_2) {
            activePage = 1;
            playTurnSound();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_3) {
            activePage = 2;
            playTurnSound();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void playTurnSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.9F, 1.1F);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x55060D15);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int left = (this.width - BOOK_WIDTH) / 2;
        int top = (this.height - BOOK_HEIGHT) / 2;

        // Marco exterior celestial místico
        guiGraphics.fill(left - 4, top - 4, left + BOOK_WIDTH + 4, top + BOOK_HEIGHT + 4, 0xFF080C16);
        guiGraphics.fill(left - 2, top - 2, left + BOOK_WIDTH + 2, top + BOOK_HEIGHT + 2, 0xFF2A284A);
        guiGraphics.fill(left - 1, top - 1, left + BOOK_WIDTH + 1, top + BOOK_HEIGHT + 1, 0xFF584C82);

        // Fondo de pergamino de noche lunar
        guiGraphics.fill(left, top, left + BOOK_WIDTH, top + BOOK_HEIGHT, 0xFF0F1424);
        guiGraphics.fill(left + 3, top + 3, left + BOOK_WIDTH - 3, top + BOOK_HEIGHT - 3, 0xFF141A30);

        // Filigrana interior dorada y plateada
        drawOutline(guiGraphics, left + 5, top + 5, BOOK_WIDTH - 10, BOOK_HEIGHT - 10, 0xFF4A4B75);

        // Barra superior de pestañas
        renderTabs(guiGraphics, left, top, mouseX, mouseY);

        // Botón cerrar '✕'
        int closeX = left + BOOK_WIDTH - 18;
        int closeY = top + 8;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= closeY && mouseY <= closeY + 12;
        guiGraphics.drawString(this.font, hoverClose ? "§c✕" : "§7✕", closeX, closeY, 0xFFFFFFFF, false);

        // Contenido según página activa
        switch (activePage) {
            case 0 -> renderPageLore(guiGraphics, left, top);
            case 1 -> renderPageAuthorities(guiGraphics, left, top);
            case 2 -> renderPageSanctuary(guiGraphics, left, top);
        }

        // Barra inferior: Navegación de páginas
        renderBottomNav(guiGraphics, left, top, mouseX, mouseY);
    }

    private void renderTabs(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        String[] tabs = {"📜 Crónica", "🌙 Autoridades", "🏛️ Santuario"};
        int tabW = 88;
        int tabH = 16;
        int tabY = top + 8;

        for (int i = 0; i < tabs.length; i++) {
            int tabX = left + 10 + (i * (tabW + 6));
            boolean selected = (i == activePage);
            boolean hover = mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + tabH;

            int bg = selected ? 0xFF352B58 : (hover ? 0xFF24233F : 0xFF1A1D30);
            int border = selected ? 0xFFC0A0FF : (hover ? 0xFF7D72B0 : 0xFF3E3D60);
            guiGraphics.fill(tabX, tabY, tabX + tabW, tabY + tabH, bg);
            drawOutline(guiGraphics, tabX, tabY, tabW, tabH, border);

            String title = (selected ? "§d§l" : (hover ? "§f" : "§7")) + tabs[i];
            guiGraphics.drawCenteredString(this.font, title, tabX + tabW / 2, tabY + 4, 0xFFFFFFFF);
        }
    }

    private void renderPageLore(GuiGraphics guiGraphics, int left, int top) {
        int contentX = left + 14;
        int y = top + 34;

        guiGraphics.drawString(this.font, "§d§l✦ EL DESPERTAR DE COLUMBINA ✦", contentX, y, 0xFFFFFFFF, false);
        y += 12;
        guiGraphics.drawString(this.font, "§7Crónica Sagrada de las Tres Diosas Lunares", contentX, y, 0xFFFFFFFF, false);
        y += 14;

        // Separador estelar
        guiGraphics.fill(contentX, y, left + BOOK_WIDTH - 14, y + 1, 0xFF4A4B75);
        y += 8;

        String[] loreLines = {
                "§fColumbina §7se encuentra debilitada en este plano y no",
                "§7puede manifestar la plenitud de su poder como Diosa.",
                "§7El pueblo que la venera ha comenzado a inquietarse.",
                "",
                "§6Misión del Elegido:",
                "§7Viaja junto a Columbina hasta el ancestral §eSantuario",
                "§ede las Lunas§7, erigido en honor a las Tres Hermanas:",
                "§bAria §7(Luna Llena), §dSonnet §7(Creciente) y §5Canon §7(Nueva).",
                "",
                "§aObjetivo Sagrado:",
                "§7Reunir sus Tres Autoridades Lunares y activar el Altar.",
                "§7Al completarlo, despertará como la §d§lDiosa Trilunar§7,",
                "§7otorgando §e+135 Protogemas diarias §7(+50% permanente)."
        };

        for (String line : loreLines) {
            guiGraphics.drawString(this.font, line, contentX, y, 0xFFFFFFFF, false);
            y += 10;
        }
    }

    private void renderPageAuthorities(GuiGraphics guiGraphics, int left, int top) {
        int contentX = left + 14;
        int y = top + 34;

        Player player = Minecraft.getInstance().player;
        boolean hasAria = player != null && player.getInventory().contains(new ItemStack(SystemItems.LUNAR_AUTHORITY_ARIA.get()));
        boolean hasSonnet = player != null && player.getInventory().contains(new ItemStack(SystemItems.LUNAR_AUTHORITY_SONNET.get()));
        boolean hasCanon = player != null && player.getInventory().contains(new ItemStack(SystemItems.LUNAR_AUTHORITY_CANON.get()));

        guiGraphics.drawString(this.font, "§d§l✦ LAS TRES AUTORIDADES LUNARES ✦", contentX, y, 0xFFFFFFFF, false);
        y += 12;
        guiGraphics.drawString(this.font, "§7Inspección de Reliquias en tu Posesión", contentX, y, 0xFFFFFFFF, false);
        y += 14;

        // Separador
        guiGraphics.fill(contentX, y, left + BOOK_WIDTH - 14, y + 1, 0xFF4A4B75);
        y += 8;

        // 1. Aria
        guiGraphics.drawString(this.font, "§b✦ 1. Autoridad de Aria §8(Luna Llena)", contentX, y, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, hasAria ? "§a✔ [OBTENIDA]" : "§c✖ [PENDIENTE]", left + BOOK_WIDTH - 90, y, 0xFFFFFFFF, false);
        y += 10;
        guiGraphics.drawString(this.font, "§7  • Purificación de vestigios estelares en luna llena.", contentX, y, 0xFFFFFFFF, false);
        y += 16;

        // 2. Sonnet
        guiGraphics.drawString(this.font, "§d✦ 2. Autoridad de Sonnet §8(Luna Creciente)", contentX, y, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, hasSonnet ? "§a✔ [OBTENIDA]" : "§c✖ [PENDIENTE]", left + BOOK_WIDTH - 90, y, 0xFFFFFFFF, false);
        y += 10;
        guiGraphics.drawString(this.font, "§7  • Resonancia alquímica con Protogemas celestiales.", contentX, y, 0xFFFFFFFF, false);
        y += 16;

        // 3. Canon
        guiGraphics.drawString(this.font, "§5✦ 3. Autoridad de Canon §8(Luna Nueva)", contentX, y, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, hasCanon ? "§a✔ [OBTENIDA]" : "§c✖ [PENDIENTE]", left + BOOK_WIDTH - 90, y, 0xFFFFFFFF, false);
        y += 10;
        guiGraphics.drawString(this.font, "§7  • Derrotar al antiguo Guardián de la Noche Oscura.", contentX, y, 0xFFFFFFFF, false);
        y += 18;

        // Resumen
        if (hasAria && hasSonnet && hasCanon) {
            guiGraphics.drawString(this.font, "§6§l¡RELIQUIAS REUNIDAS! §eDirígete al Altar para la consagración.", contentX, y, 0xFFFFFFFF, false);
        } else {
            int missing = (hasAria ? 0 : 1) + (hasSonnet ? 0 : 1) + (hasCanon ? 0 : 1);
            guiGraphics.drawString(this.font, "§eEstado: §7Te faltan §c" + missing + " Autoridad(es) §7para despertar el Altar.", contentX, y, 0xFFFFFFFF, false);
        }
    }

    private void renderPageSanctuary(GuiGraphics guiGraphics, int left, int top) {
        int contentX = left + 14;
        int y = top + 34;

        Player player = Minecraft.getInstance().player;
        SystemData data = player != null ? player.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
        boolean awakened = data != null && data.isWelkinEnhanced();

        guiGraphics.drawString(this.font, "§d§l✦ SANTUARIO Y ALTAR TRILUNAR ✦", contentX, y, 0xFFFFFFFF, false);
        y += 12;
        guiGraphics.drawString(this.font, "§7Localización y Estado de Consagración", contentX, y, 0xFFFFFFFF, false);
        y += 14;

        guiGraphics.fill(contentX, y, left + BOOK_WIDTH - 14, y + 1, 0xFF4A4B75);
        y += 8;

        // Estado del Despertar
        if (awakened) {
            guiGraphics.drawString(this.font, "§a§l✔ COLUMBINA HA DESPERTADO COMO DIOSA TRILUNAR", contentX, y, 0xFFFFFFFF, false);
            y += 11;
            guiGraphics.drawString(this.font, "§e✦ Bono permanente activo: §b+135 Protogemas diarias", contentX, y, 0xFFFFFFFF, false);
            y += 10;
            guiGraphics.drawString(this.font, "§7  (+50% adicional a la bendición de la Luna de Welkin).", contentX, y, 0xFFFFFFFF, false);
        } else {
            guiGraphics.drawString(this.font, "§c§l✖ COLUMBINA AÚN DUERME SU SUEÑO MORTAL", contentX, y, 0xFFFFFFFF, false);
            y += 11;
            guiGraphics.drawString(this.font, "§7Coloca las 3 Autoridades en el Altar Trilunar para liberarla.", contentX, y, 0xFFFFFFFF, false);
        }
        y += 18;

        // Detección de Altar cercano
        guiGraphics.drawString(this.font, "§e✦ Sensor de Resonancia del Altar (Radio 64m):", contentX, y, 0xFFFFFFFF, false);
        y += 12;

        BlockPos nearestAltar = findNearestAltar();
        if (nearestAltar != null && player != null) {
            double dist = Math.sqrt(player.blockPosition().distSqr(nearestAltar));
            guiGraphics.drawString(this.font, "§a✔ ¡Altar detectado a §e" + (int) dist + " bloques§a!", contentX, y, 0xFFFFFFFF, false);
            y += 10;
            guiGraphics.drawString(this.font, "§b  Coordenadas: §fX: " + nearestAltar.getX() + "  Y: " + nearestAltar.getY() + "  Z: " + nearestAltar.getZ(), contentX, y, 0xFFFFFFFF, false);
        } else {
            guiGraphics.drawString(this.font, "§7Ningún Altar detectado en el rango inmediato (64 bloques).", contentX, y, 0xFFFFFFFF, false);
            y += 10;
            guiGraphics.drawString(this.font, "§8Explora ruinas antiguas o invócalo con §d/system welkin sanctuary§8.", contentX, y, 0xFFFFFFFF, false);
        }
        y += 16;

        guiGraphics.drawString(this.font, "§6Ritual:", contentX, y, 0xFFFFFFFF, false);
        y += 10;
        guiGraphics.drawString(this.font, "§7Lleva las reliquias en mano y haz clic derecho en el Altar.", contentX, y, 0xFFFFFFFF, false);
    }

    private BlockPos findNearestAltar() {
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) return null;
        BlockPos playerPos = this.minecraft.player.blockPosition();
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (int dx = -64; dx <= 64; dx += 4) {
            for (int dz = -64; dz <= 64; dz += 4) {
                for (int dy = -16; dy <= 16; dy += 4) {
                    BlockPos check = playerPos.offset(dx, dy, dz);
                    if (this.minecraft.level.getBlockState(check).is(SystemBlocks.TRILUNAR_ALTAR.get())) {
                        double d = playerPos.distSqr(check);
                        if (d < nearestDist) {
                            nearestDist = d;
                            nearest = check;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private void renderBottomNav(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int navY = top + BOOK_HEIGHT - 18;

        // Botón Anterior
        int prevX = left + 14;
        boolean hoverPrev = mouseX >= prevX && mouseX <= prevX + 70 && mouseY >= navY - 2 && mouseY <= navY + 10;
        if (activePage > 0) {
            guiGraphics.drawString(this.font, hoverPrev ? "§b[ ◀ Anterior ]" : "§7[ ◀ Anterior ]", prevX, navY, 0xFFFFFFFF, false);
        }

        // Indicador de Página
        String pageStr = "§8Página §f" + (activePage + 1) + " §8/ §f" + TOTAL_PAGES;
        guiGraphics.drawCenteredString(this.font, pageStr, left + BOOK_WIDTH / 2, navY, 0xFFFFFFFF);

        // Botón Siguiente
        int nextX = left + BOOK_WIDTH - 84;
        boolean hoverNext = mouseX >= nextX && mouseX <= nextX + 70 && mouseY >= navY - 2 && mouseY <= navY + 10;
        if (activePage < TOTAL_PAGES - 1) {
            guiGraphics.drawString(this.font, hoverNext ? "§b[ Siguiente ▶ ]" : "§7[ Siguiente ▶ ]", nextX, navY, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (this.width - BOOK_WIDTH) / 2;
            int top = (this.height - BOOK_HEIGHT) / 2;

            // Botón cerrar
            int closeX = left + BOOK_WIDTH - 18;
            int closeY = top + 8;
            if (mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= closeY && mouseY <= closeY + 12) {
                onClose();
                return true;
            }

            // Pestañas
            int tabW = 88;
            int tabH = 16;
            int tabY = top + 8;
            for (int i = 0; i < TOTAL_PAGES; i++) {
                int tabX = left + 10 + (i * (tabW + 6));
                if (mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
                    if (activePage != i) {
                        activePage = i;
                        playTurnSound();
                    }
                    return true;
                }
            }

            // Navegación inferior
            int navY = top + BOOK_HEIGHT - 18;
            int prevX = left + 14;
            if (activePage > 0 && mouseX >= prevX && mouseX <= prevX + 70 && mouseY >= navY - 2 && mouseY <= navY + 10) {
                activePage--;
                playTurnSound();
                return true;
            }

            int nextX = left + BOOK_WIDTH - 84;
            if (activePage < TOTAL_PAGES - 1 && mouseX >= nextX && mouseX <= nextX + 70 && mouseY >= navY - 2 && mouseY <= navY + 10) {
                activePage++;
                playTurnSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawOutline(GuiGraphics guiGraphics, int x, int y, int w, int h, int color) {
        guiGraphics.fill(x, y, x + w, y + 1, color);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, color);
        guiGraphics.fill(x, y, x + 1, y + h, color);
        guiGraphics.fill(x + w - 1, y, x + w, y + h, color);
    }
}
