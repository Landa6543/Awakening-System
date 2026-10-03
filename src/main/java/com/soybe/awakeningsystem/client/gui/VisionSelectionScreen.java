package com.soybe.awakeningsystem.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SelectVisionPayload;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Pantalla Holográfica Celestial de Resonancia Elemental (Visiones de Teyvat).
 * Rediseñada completamente con el estilo visual sci-fi/celestial del Sistema.
 */
public class VisionSelectionScreen extends Screen {
    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 240;

    private final Screen parentScreen;

    public record VisionEntry(
            String id,
            String name,
            String title,
            String perkDesc,
            int color,
            ChatFormatting format,
            ResourceLocation icon
    ) {}

    public static final List<VisionEntry> VISIONS = List.of(
            new VisionEntry("NONE", "Ninguna", "Sin Resonancia", "Desactiva todos los efectos elementales pasivos.", 0xFFAAAAAA, ChatFormatting.GRAY,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/no_vision_icon.png")),
            new VisionEntry("ANEMO", "Anemo", "Viento de Mondstadt", "Velocidad II, Salto II, caída suave al agacharse e impulso eólico al atacar.", 0xFF55FFAA, ChatFormatting.GREEN,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/anemo_icon.png")),
            new VisionEntry("GEO", "Geo", "Roca de Liyue", "Resistencia II, inmunidad a empujes y escudo de absorción con golpes cuerpo a cuerpo.", 0xFFFFBB33, ChatFormatting.GOLD,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/geo_icon.png")),
            new VisionEntry("ELECTRO", "Electro", "Rayo de Inazuma", "Prisa Minera II (Haste), Velocidad I y chispas de rayo en cadena al golpear.", 0xFFAA55FF, ChatFormatting.LIGHT_PURPLE,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/electro_icon.png")),
            new VisionEntry("DENDRO", "Dendro", "Naturaleza de Sumeru", "Regeneración I, fertiliza cultivos cercanos al agacharse y envenena enemigos.", 0xFF44CC33, ChatFormatting.DARK_GREEN,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/dendro_icon.png")),
            new VisionEntry("HYDRO", "Hydro", "Marea de Fontaine", "Respiración acuática, Gracia del Delfín, extingue fuego y drena vida al golpear.", 0xFF3399FF, ChatFormatting.BLUE,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/hydro_icon.png")),
            new VisionEntry("PYRO", "Pyro", "Llama de Natlan", "Resistencia al fuego infinita, vaporiza agua bajo los pies e incendia enemigos.", 0xFFFF4422, ChatFormatting.RED,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/pyro_icon.png")),
            new VisionEntry("CRYO", "Cryo", "Escarcha de Snezhnaya", "Caminante de hielo (Frost Walker), inmunidad a congelamiento y ralentización IV al golpear.", 0xFF77DDFF, ChatFormatting.AQUA,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/cryo_icon.png"))
    );

    public VisionSelectionScreen(Screen parentScreen) {
        super(Component.literal("Canalizador de Visiones de Teyvat"));
        this.parentScreen = parentScreen;
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new VisionSelectionScreen(null));
    }

    public static void open(Screen parent) {
        Minecraft.getInstance().setScreen(new VisionSelectionScreen(parent));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private SystemData data() {
        var p = Minecraft.getInstance().player;
        return p != null ? p.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (GuiConfig.enableBackgroundBlur) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
        } else {
            graphics.fill(0, 0, width, height, 0x55060D15);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        // Marco exterior holográfico celestial
        g.fill(left - 2, top - 2, left + GUI_WIDTH + 2, top + GUI_HEIGHT + 2, 0xFFDFB76C);
        g.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xF508111A);

        // Cabecera estilizada
        g.fill(left, top, left + GUI_WIDTH, top + 26, 0xFF0D1D2B);
        g.fill(left, top + 25, left + GUI_WIDTH, top + 26, 0xFFDFB76C);

        // Esquinas reforzadas
        int accentCol = 0xFF4EE2EC;
        g.fill(left, top, left + 8, top + 2, accentCol);
        g.fill(left, top, left + 2, top + 8, accentCol);
        g.fill(left + GUI_WIDTH - 8, top, left + GUI_WIDTH, top + 2, accentCol);
        g.fill(left + GUI_WIDTH - 2, top, left + GUI_WIDTH, top + 8, accentCol);

        g.drawCenteredString(font, "§6✦ §fRESONANCIA ELEMENTAL DE TEYVAT §6✦", left + GUI_WIDTH / 2, top + 8, 0xFFDFB76C);

        // Botón cerrar X
        boolean closeHover = mouseX >= left + GUI_WIDTH - 20 && mouseX < left + GUI_WIDTH - 6 && mouseY >= top + 6 && mouseY < top + 20;
        g.fill(left + GUI_WIDTH - 20, top + 6, left + GUI_WIDTH - 6, top + 20, closeHover ? 0xDDFF4444 : 0x66222222);
        g.drawCenteredString(font, "✕", left + GUI_WIDTH - 13, top + 8, closeHover ? 0xFFFFFFFF : 0xFFAAAAAA);

        SystemData d = data();
        String currentVision = d != null ? d.activeVision() : "NONE";

        VisionEntry hoveredEntry = null;

        // Cuadrícula 2 columnas x 4 filas
        int btnW = 140;
        int btnH = 26;
        int gridTop = top + 34;

        for (int i = 0; i < VISIONS.size(); i++) {
            VisionEntry v = VISIONS.get(i);
            int col = i % 2;
            int row = i / 2;
            int bx = left + 14 + col * 152;
            int by = gridTop + row * 29;

            boolean isCurrent = v.id.equalsIgnoreCase(currentVision);
            boolean hovered = mouseX >= bx && mouseX < bx + btnW && mouseY >= by && mouseY < by + btnH;

            if (hovered) {
                hoveredEntry = v;
            }

            // Fondo y borde
            int bgCol = isCurrent ? 0xEE143547 : (hovered ? 0xDD1B2836 : 0xAA0D1822);
            int borderCol = isCurrent ? v.color : (hovered ? 0xFFFFFFFF : 0xFF2A3D4D);

            g.fill(bx, by, bx + btnW, by + btnH, bgCol);
            g.renderOutline(bx, by, btnW, btnH, borderCol);

            // Si es la actual, indicador brillante en el borde izquierdo
            if (isCurrent) {
                g.fill(bx, by, bx + 3, by + btnH, v.color);
            }

            // Icono elemental
            RenderSystem.enableBlend();
            g.blit(v.icon, bx + 6, by + 5, 0, 0, 16, 16, 16, 16);

            // Nombre del elemento
            int nameColor = isCurrent ? v.color : (hovered ? 0xFFFFFFFF : v.color);
            g.drawString(font, v.name, bx + 26, by + 4, nameColor);

            // Subtítulo / Región
            g.drawString(font, "§7" + v.title, bx + 26, by + 14, 0x888888);

            // Estado activo
            if (isCurrent) {
                g.drawCenteredString(font, "§a✔", bx + btnW - 12, by + 8, 0xFF50FA7B);
            }
        }

        // Panel inferior de descripción detallada
        VisionEntry displayEntry = hoveredEntry;
        if (displayEntry == null) {
            for (VisionEntry v : VISIONS) {
                if (v.id.equalsIgnoreCase(currentVision)) {
                    displayEntry = v;
                    break;
                }
            }
        }
        if (displayEntry == null) displayEntry = VISIONS.get(0);

        int descY = top + 154;
        int descW = GUI_WIDTH - 28;
        int descH = 48;
        g.fill(left + 14, descY, left + 14 + descW, descY + descH, 0xCC0A141E);
        g.renderOutline(left + 14, descY, descW, descH, displayEntry.color);

        // Título del elemento seleccionado/hovered
        String headerDesc = "§l" + displayEntry.name + " §r§7(" + displayEntry.title + ")";
        g.drawString(font, headerDesc, left + 20, descY + 6, displayEntry.color);

        // Texto descriptivo con salto de línea suave
        String descText = displayEntry.perkDesc;
        if (font.width(descText) > descW - 16) {
            List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.literal("§f" + descText), descW - 16);
            for (int l = 0; l < Math.min(3, lines.size()); l++) {
                g.drawString(font, lines.get(l), left + 20, descY + 18 + l * 10, 0xFFE0E0E0);
            }
        } else {
            g.drawString(font, "§f" + descText, left + 20, descY + 20, 0xFFE0E0E0);
        }

        // Pie de ventana con botón de retorno
        int footY = top + GUI_HEIGHT - 26;
        int footW = GUI_WIDTH - 28;
        boolean footHov = mouseX >= left + 14 && mouseX < left + 14 + footW && mouseY >= footY && mouseY < footY + 18;
        g.fill(left + 14, footY, left + 14 + footW, footY + 18, footHov ? 0xDD1B3B48 : 0xAA0D202A);
        g.renderOutline(left + 14, footY, footW, 18, footHov ? 0xFF4EE2EC : 0xFF2A5060);
        g.drawCenteredString(font, "§b✔ Volver al Sistema (ESC)", left + GUI_WIDTH / 2, footY + 5, 0xFF70E8FF);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (width - GUI_WIDTH) / 2;
            int top = (height - GUI_HEIGHT) / 2;

            // Botón cerrar X
            if (mouseX >= left + GUI_WIDTH - 20 && mouseX < left + GUI_WIDTH - 6 && mouseY >= top + 6 && mouseY < top + 20) {
                close();
                return true;
            }

            // Botón volver inferior
            int footY = top + GUI_HEIGHT - 26;
            int footW = GUI_WIDTH - 28;
            if (mouseX >= left + 14 && mouseX < left + 14 + footW && mouseY >= footY && mouseY < footY + 18) {
                close();
                return true;
            }

            // Clic en elementos
            int btnW = 140;
            int btnH = 26;
            int gridTop = top + 34;

            for (int i = 0; i < VISIONS.size(); i++) {
                VisionEntry v = VISIONS.get(i);
                int col = i % 2;
                int row = i / 2;
                int bx = left + 14 + col * 152;
                int by = gridTop + row * 29;

                if (mouseX >= bx && mouseX < bx + btnW && mouseY >= by && mouseY < by + btnH) {
                    PacketDistributor.sendToServer(new SelectVisionPayload(v.id));
                    AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.4F);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void close() {
        if (parentScreen != null) {
            Minecraft.getInstance().setScreen(parentScreen);
        } else {
            onClose();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
