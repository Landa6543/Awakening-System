package com.soybe.awakeningsystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Pantalla de lectura de libros desgastados y crónicas ancestrales.
 * Evita cualquier fallo de NBT o atril de Minecraft vanilla.
 */
public class WornBookScreen extends Screen {
    private final int volume;
    private static final int BOOK_WIDTH = 290;
    private static final int BOOK_HEIGHT = 210;

    public WornBookScreen(int volume) {
        super(Component.translatable("awakening_system.worn_book.title"));
        this.volume = volume;
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
        return super.keyPressed(keyCode, scanCode, modifiers);
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

        // Cubierta de cuero envejecido
        guiGraphics.fill(left - 3, top - 3, left + BOOK_WIDTH + 3, top + BOOK_HEIGHT + 3, 0xFF2A1B0E);
        guiGraphics.fill(left - 1, top - 1, left + BOOK_WIDTH + 1, top + BOOK_HEIGHT + 1, 0xFF4A341E);

        // Página de pergamino gastado
        guiGraphics.fill(left, top, left + BOOK_WIDTH, top + BOOK_HEIGHT, 0xFFE8DCC4);
        guiGraphics.fill(left + 4, top + 4, left + BOOK_WIDTH - 4, top + BOOK_HEIGHT - 4, 0xFFF2E8D5);

        // Borde interior de tinta dorada antigua
        drawOutline(guiGraphics, left + 6, top + 6, BOOK_WIDTH - 12, BOOK_HEIGHT - 12, 0xFF8A6E3C);

        // Botón cerrar '✕'
        int closeX = left + BOOK_WIDTH - 18;
        int closeY = top + 9;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 10 && mouseY >= closeY && mouseY <= closeY + 10;
        guiGraphics.drawString(this.font, hoverClose ? "§c✕" : "§8✕", closeX, closeY, 0xFF000000, false);

        int vol = (volume == 2 || volume == 3) ? volume : 1;
        Component title = Component.translatable("awakening_system.worn_book.vol_" + vol + ".title");
        Component subtitle = Component.translatable("awakening_system.worn_book.vol_" + vol + ".subtitle");

        // Títulos
        guiGraphics.drawCenteredString(this.font, title, left + BOOK_WIDTH / 2, top + 14, 0xFF000000);
        guiGraphics.drawCenteredString(this.font, subtitle, left + BOOK_WIDTH / 2, top + 26, 0xFF555555);
        guiGraphics.drawCenteredString(this.font, Component.literal("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), left + BOOK_WIDTH / 2, top + 36, 0xFF888888);

        // Cuerpo del texto
        int textY = top + 47;
        int maxTextY = top + BOOK_HEIGHT - 20;
        int maxLineWidth = BOOK_WIDTH - 32;

        for (int l = 0; l < 9; l++) {
            Component line = Component.translatable("awakening_system.worn_book.vol_" + vol + ".line_" + l);
            List<net.minecraft.util.FormattedCharSequence> splitLines = this.font.split(line, maxLineWidth);
            for (net.minecraft.util.FormattedCharSequence seq : splitLines) {
                if (textY + 9 <= maxTextY) {
                    guiGraphics.drawString(this.font, seq, left + 16, textY, 0xFF000000, false);
                    textY += 10;
                }
            }
            textY += 2; // Breve espacio de separación de párrafo
        }

        // Pie de página
        guiGraphics.drawCenteredString(this.font, Component.translatable("awakening_system.worn_book.footer"), left + BOOK_WIDTH / 2, top + BOOK_HEIGHT - 16, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (this.width - BOOK_WIDTH) / 2;
            int top = (this.height - BOOK_HEIGHT) / 2;
            int closeX = left + BOOK_WIDTH - 18;
            int closeY = top + 9;
            if (mouseX >= closeX && mouseX <= closeX + 10 && mouseY >= closeY && mouseY <= closeY + 10) {
                onClose();
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
