package com.soybe.awakeningsystem.subspace;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class SubspaceScreen extends AbstractContainerScreen<SubspaceMenu> {
    private static final int CYAN = 0xFF70E8FF;
    private static final int MUTED = 0xFF9BB7C7;
    private static final int PANEL_BG = 0xF2091424;
    private static final int EDGE_BORDER = 0xFF1B4357;
    private static final int SLOT_BG = 0x66000000;
    private static final int SLOT_BORDER = 0x3370E8FF;

    private SubspaceHoloButton travelButton;
    private SubspaceHoloButton upgradeButton;
    private SubspaceHoloButton prevPageBtn;
    private SubspaceHoloButton nextPageBtn;

    public SubspaceScreen(SubspaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 246;
        this.inventoryLabelY = 129;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        boolean inSubspace = this.minecraft != null && this.minecraft.level != null &&
                this.minecraft.level.dimension().location().getPath().equals("subspace_realm");

        int btnY = this.topPos + 224;
        int btnW = 78;
        int btnH = 18;

        // Botones de paginación
        this.prevPageBtn = new SubspaceHoloButton(
                this.leftPos + 6, this.topPos + 4, 15, 12,
                Component.literal("◀"), Component.literal("§f§l◀"),
                HoloTheme.CYAN,
                btn -> {
                    int p = this.menu.getCurrentPage();
                    if (p > 0) {
                        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                                new com.soybe.awakeningsystem.network.SubspaceActionPayload("page:" + (p - 1))
                        );
                        this.menu.setPage(p - 1);
                    }
                }
        );
        this.prevPageBtn.visible = false;
        this.addRenderableWidget(this.prevPageBtn);

        this.nextPageBtn = new SubspaceHoloButton(
                this.leftPos + this.imageWidth - 21, this.topPos + 4, 15, 12,
                Component.literal("▶"), Component.literal("§f§l▶"),
                HoloTheme.CYAN,
                btn -> {
                    int p = this.menu.getCurrentPage();
                    if (p < this.menu.getTotalPages() - 1) {
                        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                                new com.soybe.awakeningsystem.network.SubspaceActionPayload("page:" + (p + 1))
                        );
                        this.menu.setPage(p + 1);
                    }
                }
        );
        this.nextPageBtn.visible = false;
        this.addRenderableWidget(this.nextPageBtn);

        // Botón Entrar / Salir del Reino Interior (Holográfico Cyan / Carmesí)
        this.travelButton = new SubspaceHoloButton(
                this.leftPos + 8, btnY, btnW, btnH,
                Component.literal(inSubspace ? "§c🚪 Salir" : "§b🌌 Entrar"),
                Component.literal(inSubspace ? "§f§l🚪 Salir" : "§f§l🌌 Entrar"),
                inSubspace ? HoloTheme.RED : HoloTheme.CYAN,
                btn -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new com.soybe.awakeningsystem.network.SubspaceActionPayload(inSubspace ? "exit" : "enter")
                    );
                    this.onClose();
                }
        );
        this.addRenderableWidget(this.travelButton);

        // Botón Mejorar Territorio (Holográfico Dorado)
        this.upgradeButton = new SubspaceHoloButton(
                this.leftPos + 90, btnY, btnW, btnH,
                Component.literal("§6⭐ Mejorar"),
                Component.literal("§e§l⭐ Mejorar"),
                HoloTheme.GOLD,
                btn -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new com.soybe.awakeningsystem.network.SubspaceActionPayload("upgrade")
                    );
                }
        );
        this.addRenderableWidget(this.upgradeButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.prevPageBtn != null && this.nextPageBtn != null) {
            boolean multi = this.menu.getTotalPages() > 1;
            this.prevPageBtn.visible = multi;
            this.nextPageBtn.visible = multi;
            this.prevPageBtn.active = multi && this.menu.getCurrentPage() > 0;
            this.nextPageBtn.active = multi && this.menu.getCurrentPage() < this.menu.getTotalPages() - 1;
        }

        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        // Tooltip del botón Mejorar
        if (this.upgradeButton != null && this.upgradeButton.isHoveredOrFocused()) {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            int lvl = 1;
            if (player != null) {
                lvl = com.soybe.awakeningsystem.SystemEvents.data(player).subspaceLevel();
            }
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("§6§lMejorar Reino Subespacial"));
            tip.add(Component.literal("§7Nivel actual: §eNivel " + lvl + " / 5"));
            if (lvl < 5) {
                int next = lvl + 1;
                int cost = switch (next) {
                    case 2 -> 2500;
                    case 3 -> 8000;
                    case 4 -> 20000;
                    default -> 50000;
                };
                tip.add(Component.literal("§7Siguiente nivel: §aNivel " + next));
                tip.add(Component.literal("§7Coste: §6" + cost + " Monedas §7o §b1x Mineral"));
                tip.add(Component.literal("§e✦ Expande el territorio para colocar más cofres y altares."));
            } else {
                tip.add(Component.literal("§a✔ Nivel Máximo Alcanzado (Pequeño Mundo)."));
            }
            graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
        }

        // Tooltip del botón Entrar/Salir
        if (this.travelButton != null && this.travelButton.isHoveredOrFocused()) {
            boolean inSubspace = this.minecraft != null && this.minecraft.level != null &&
                    this.minecraft.level.dimension().location().getPath().equals("subspace_realm");
            List<Component> tip = new ArrayList<>();
            if (inSubspace) {
                tip.add(Component.literal("§c§lSalir del Subespacio"));
                tip.add(Component.literal("§7Regresa a tu ubicación original en el mundo exterior."));
            } else {
                tip.add(Component.literal("§b§lViajar al Reino Interior"));
                tip.add(Component.literal("§7Entra en persona a tu dimensión de bolsillo."));
                tip.add(Component.literal("§7Allí puedes colocar cofres, hornos y construir"));
                tip.add(Component.literal("§7tu almacén infinito como en los manhuas."));
            }
            graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
        }

        // Tooltip para ranuras bloqueadas
        for (Slot slot : this.menu.slots) {
            if (slot instanceof SubspaceMenu.SubspaceSlot subSlot && !subSlot.isSlotUnlocked()) {
                int sx = this.leftPos + slot.x;
                int sy = this.topPos + slot.y;
                if (mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18) {
                    List<Component> tip = new ArrayList<>();
                    tip.add(Component.literal("§c§l🔒 Ranura Bloqueada"));
                    tip.add(Component.literal("§7Esta ranura requiere más espacio físico en tu Reino."));
                    tip.add(Component.literal("§e✦ Coloca más cofres en tu §bReino Subespacial"));
                    tip.add(Component.literal("§7  para desbloquear más ranuras (§a+27 ranuras por cofre§7)."));
                    graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
                    break;
                }
            }
        }

        // Tooltip del contador de capacidad
        int capAreaX = this.leftPos + this.imageWidth - 65;
        int capAreaY = this.topPos + this.inventoryLabelY - 2;
        if (mouseX >= capAreaX && mouseX <= this.leftPos + this.imageWidth - 6 && mouseY >= capAreaY && mouseY <= capAreaY + 12) {
            int unlocked = this.menu.getUnlockedSlots();
            int chests = this.menu.getChestCount();
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("§b§lCapacidad Dimensional"));
            tip.add(Component.literal("§7Ranuras disponibles: §e" + unlocked));
            tip.add(Component.literal("§7Cofres en tu Reino: §6" + chests + " §7(" + (chests / 2) + " Dobles)"));
            tip.add(Component.literal("§7✦ Cada cofre colocado en tu dimensión expande tu Subespacio (+27 ranuras c/u)."));
            graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        // 1. Fondo cósmico del Sistema con borde holográfico
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x4017354B);
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, EDGE_BORDER);
        g.fill(x, y, x + w, y + h, PANEL_BG);

        // 2. Línea de acento superior estilo HUD
        g.fill(x, y, x + w, y + 2, CYAN);

        // 3. Esquinas holográficas del Sistema
        g.fill(x, y, x + 8, y + 2, CYAN);
        g.fill(x, y, x + 2, y + 8, CYAN);
        g.fill(x + w - 8, y, x + w, y + 2, CYAN);
        g.fill(x + w - 2, y, x + w, y + 8, CYAN);
        g.fill(x, y + h - 2, x + 8, y + h, CYAN);
        g.fill(x, y + h - 8, x + 2, y + h, CYAN);
        g.fill(x + w - 8, y + h - 2, x + w, y + h, CYAN);
        g.fill(x + w - 2, y + h - 8, x + w, y + h, CYAN);

        // 4. Cabecera del título
        g.fill(x + 6, y + 4, x + w - 6, y + 15, 0x990D2235);
        g.renderOutline(x + 6, y + 4, w - 12, 12, 0x662D7691);

        // 5. Casillas iluminadas para cada slot del menú (con detección de bloqueo)
        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;

            if (slot instanceof SubspaceMenu.SubspaceSlot subSlot) {
                boolean unlocked = subSlot.isSlotUnlocked();
                int slotBg = unlocked ? SLOT_BG : 0x772A0808; // tinte carmesí oscuro si está bloqueada
                int slotBorder = unlocked ? SLOT_BORDER : 0x55FF4444; // borde rojizo sutil

                g.fill(sx, sy, sx + 18, sy + 18, slotBg);
                g.renderOutline(sx, sy, 18, 18, slotBorder);

                if (!unlocked) {
                    g.drawCenteredString(this.font, "§c🔒", sx + 9, sy + 5, 0xFFFFFFFF);
                }
            } else {
                g.fill(sx, sy, sx + 18, sy + 18, 0x44000000);
                g.renderOutline(sx, sy, 18, 18, 0x229BB7C7);
            }
        }

        // 6. Separador entre el Subespacio y el Inventario
        g.fill(x + 8, y + 127, x + w - 8, y + 128, 0x4470E8FF);

        // 7. Separador superior del panel de control/botones
        g.fill(x + 8, y + 220, x + w - 8, y + 221, 0x3370E8FF);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        int lvl = 1;
        if (player != null) {
            lvl = com.soybe.awakeningsystem.SystemEvents.data(player).subspaceLevel();
        }

        // Título centrado con estilo del Sistema, página y nivel actual
        int totalPages = this.menu.getTotalPages();
        int curPage = this.menu.getCurrentPage() + 1;
        String title = (totalPages > 1)
                ? "✦ SUBESPACIO (" + curPage + "/" + totalPages + ") ✦"
                : "✦ SUBESPACIO DEL SISTEMA (NVL " + lvl + ") ✦";
        g.drawCenteredString(this.font, title, this.imageWidth / 2, 6, CYAN);

        // Etiqueta del inventario
        g.drawString(this.font, "Inventario del Cazador", 8, this.inventoryLabelY, MUTED, false);

        // Contador de capacidad en tiempo real basado en ranuras desbloqueadas
        int count = 0;
        if (player != null) {
            var items = com.soybe.awakeningsystem.SystemEvents.data(player).getSubspaceItems();
            for (var stack : items) {
                if (!stack.isEmpty()) count++;
            }
        } else {
            for (int i = 0; i < SubspaceMenu.SUBSPACE_SLOTS; i++) {
                if (this.menu.slots.size() > i && this.menu.slots.get(i).hasItem()) {
                    count++;
                }
            }
        }
        int unlocked = this.menu.getUnlockedSlots();
        String capText = count + " / " + unlocked;
        int capW = this.font.width(capText);
        int capColor = (count >= unlocked) ? 0xFFFF5555 : (count >= unlocked * 0.85 ? 0xFFFFAA00 : 0xFF88D8B0);
        g.drawString(this.font, capText, this.imageWidth - 8 - capW, this.inventoryLabelY, capColor, false);
    }

    public enum HoloTheme {
        CYAN(
                0xDD0A1828, 0xEE0E2E4A,
                0xFF1B4E68, 0xFF70E8FF,
                0xFF70E8FF, 0xFFFFFFFF,
                0x3370E8FF, 0x8870E8FF,
                0x4470E8FF
        ),
        RED(
                0xDD281016, 0xEE471822,
                0xFF6A2430, 0xFFFF6B81,
                0xFFFF5555, 0xFFFFFFFF,
                0x33FF6B81, 0x88FF6B81,
                0x44FF6B81
        ),
        GOLD(
                0xDD221605, 0xEE3D2908,
                0xFF6E4F10, 0xFFFFD700,
                0xFFFFD700, 0xFFFFFFFF,
                0x44FFD700, 0x99FFD700,
                0x44FFD700
        );

        final int baseBg, hoverBg;
        final int baseBorder, hoverBorder;
        final int baseCorner, hoverCorner;
        final int baseHighlight, hoverHighlight;
        final int glowColor;

        HoloTheme(int baseBg, int hoverBg, int baseBorder, int hoverBorder,
                  int baseCorner, int hoverCorner, int baseHighlight, int hoverHighlight, int glowColor) {
            this.baseBg = baseBg;
            this.hoverBg = hoverBg;
            this.baseBorder = baseBorder;
            this.hoverBorder = hoverBorder;
            this.baseCorner = baseCorner;
            this.hoverCorner = hoverCorner;
            this.baseHighlight = baseHighlight;
            this.hoverHighlight = hoverHighlight;
            this.glowColor = glowColor;
        }
    }

    public static class SubspaceHoloButton extends Button {
        private final HoloTheme theme;
        private final Component normalLabel;
        private final Component hoverLabel;

        public SubspaceHoloButton(int x, int y, int width, int height,
                                  Component normalLabel, Component hoverLabel,
                                  HoloTheme theme, OnPress onPress) {
            super(x, y, width, height, normalLabel, onPress, DEFAULT_NARRATION);
            this.normalLabel = normalLabel;
            this.hoverLabel = hoverLabel;
            this.theme = theme;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            boolean hovered = this.isHoveredOrFocused();

            int bx = this.getX();
            int by = this.getY();
            int bw = this.getWidth();
            int bh = this.getHeight();

            int bg = hovered ? theme.hoverBg : theme.baseBg;
            int border = hovered ? theme.hoverBorder : theme.baseBorder;
            int corner = hovered ? theme.hoverCorner : theme.baseCorner;
            int highlight = hovered ? theme.hoverHighlight : theme.baseHighlight;

            // Halo sutil en hover
            if (hovered) {
                graphics.renderOutline(bx - 1, by - 1, bw + 2, bh + 2, theme.glowColor);
            }

            // Fondo del botón
            graphics.fill(bx, by, bx + bw, by + bh, bg);

            // Borde holográfico
            graphics.renderOutline(bx, by, bw, bh, border);

            // Línea de brillo interna superior
            graphics.fill(bx + 1, by + 1, bx + bw - 1, by + 2, highlight);

            // Esquinas resaltadas (estilo Solo Leveling System)
            graphics.fill(bx, by, bx + 2, by + 2, corner);
            graphics.fill(bx + bw - 2, by, bx + bw, by + 2, corner);
            graphics.fill(bx, by + bh - 2, bx + 2, by + bh, corner);
            graphics.fill(bx + bw - 2, by + bh - 2, bx + bw, by + bh, corner);

            // Texto centrado
            Component text = hovered ? this.hoverLabel : this.normalLabel;
            graphics.drawCenteredString(mc.font, text, bx + bw / 2, by + (bh - 8) / 2, 0xFFFFFF);
        }
    }
}
