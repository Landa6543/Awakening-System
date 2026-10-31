package com.soybe.awakeningsystem.subspace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SubspaceScreen extends AbstractContainerScreen<SubspaceMenu> {
    // Paleta de Estilo Solo Leveling / System UI
    private static final int CYAN_PRIMARY = 0xFF00E5FF;
    private static final int CYAN_BRIGHT = 0xFF70F0FF;
    private static final int CYAN_GLOW = 0x6600E5FF;
    private static final int PANEL_BG = 0xF4050E18;
    private static final int EDGE_BORDER = 0xFF00E5FF;
    private static final int MUTED = 0xFF7E9DAF;
    private static final int SLOT_BG = 0x77061422;
    private static final int SLOT_BORDER = 0x4400E5FF;
    private static final int SLOT_MATCH_BG = 0x4400E5FF;
    private static final int SLOT_MATCH_BORDER = 0xFF00FFFF;
    private static final int SLOT_DIM = 0xB002060C;

    private SubspaceHoloButton travelButton;
    private SubspaceHoloButton upgradeButton;
    private SubspaceHoloButton prevPageBtn;
    private SubspaceHoloButton nextPageBtn;
    private SubspaceHoloButton compactBtn;
    private SubspaceHoloButton updateStructureBtn;
    private EditBox searchBox;

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

        // Botón Página Anterior
        this.prevPageBtn = new SubspaceHoloButton(
                this.leftPos + 6, this.topPos + 3, 14, 12,
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

        // Botón Página Siguiente
        this.nextPageBtn = new SubspaceHoloButton(
                this.leftPos + 21, this.topPos + 3, 14, 12,
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

        // Botón Compactar y Unificar Stacks (⚡)
        this.compactBtn = new SubspaceHoloButton(
                this.leftPos + 37, this.topPos + 3, 16, 12,
                Component.literal("§b⚡"), Component.literal("§f§l⚡"),
                HoloTheme.CYAN,
                btn -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new com.soybe.awakeningsystem.network.SubspaceActionPayload("compact")
                    );
                }
        );
        this.addRenderableWidget(this.compactBtn);

        // Buscador / Filtro Integrado de Ítems
        int searchX = this.leftPos + 56;
        int searchW = this.imageWidth - 62;
        this.searchBox = new EditBox(this.font, searchX + 2, this.topPos + 3, searchW - 4, 12, Component.translatable("awakening_system.subspace.search_hint"));
        this.searchBox.setHint(Component.translatable("awakening_system.subspace.search_hint"));
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(0xFF00E5FF);
        this.searchBox.setMaxLength(30);
        this.addRenderableWidget(this.searchBox);

        Component travelLabel = inSubspace ? Component.translatable("awakening_system.subspace.button.exit") : Component.translatable("awakening_system.subspace.button.enter");
        this.travelButton = new SubspaceHoloButton(
                this.leftPos + 8, btnY, btnW, btnH,
                travelLabel,
                travelLabel,
                inSubspace ? HoloTheme.RED : HoloTheme.CYAN,
                btn -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new com.soybe.awakeningsystem.network.SubspaceActionPayload(inSubspace ? "exit" : "enter")
                    );
                    this.onClose();
                }
        );
        this.travelButton.active = true;
        this.addRenderableWidget(this.travelButton);

        var player = Minecraft.getInstance().player;
        int curSubLvl = 1;
        if (player != null) {
            curSubLvl = com.soybe.awakeningsystem.SystemEvents.data(player).subspaceLevel();
        }
        Component upgLabel = (curSubLvl >= 3)
                ? Component.translatable("awakening_system.subspace.button.max_tier")
                : Component.translatable("awakening_system.subspace.button.upgrade", curSubLvl + 1);
        Component upgHover = (curSubLvl >= 3)
                ? Component.translatable("awakening_system.subspace.button.max_tier_hover")
                : Component.translatable("awakening_system.subspace.button.upgrade_hover");

        this.upgradeButton = new SubspaceHoloButton(
                this.leftPos + 90, btnY, btnW, btnH,
                upgLabel,
                upgLabel,
                HoloTheme.GOLD,
                btn -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new com.soybe.awakeningsystem.network.SubspaceActionPayload("upgrade")
                    );
                }
        );
        this.upgradeButton.active = curSubLvl < 3;
        this.addRenderableWidget(this.upgradeButton);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox != null && this.searchBox.isFocused()) {
            if (keyCode == 256) { // Escape: desfocalizar el buscador
                this.searchBox.setFocused(false);
                return true;
            }
            if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.searchBox != null && this.searchBox.isFocused()) {
            if (this.searchBox.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    private boolean matchesFilter(ItemStack stack, String query) {
        if (query == null || query.isEmpty()) return true;
        if (stack.isEmpty()) return false;

        String name = stack.getHoverName().getString().toLowerCase();
        if (name.contains(query)) return true;

        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase();
        return id.contains(query);
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

        // Tooltip del botón de compactación
        if (this.compactBtn != null && this.compactBtn.isHovered()) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.translatable("awakening_system.subspace.tooltip.compact_title"));
            tip.add(Component.translatable("awakening_system.subspace.tooltip.compact_desc1"));
            tip.add(Component.translatable("awakening_system.subspace.tooltip.compact_desc2"));
            graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
        }

        // Renderizado de resaltado o atenuación de slots según el buscador
        String query = this.searchBox != null ? this.searchBox.getValue().trim().toLowerCase() : "";
        if (!query.isEmpty()) {
            int x = this.leftPos;
            int y = this.topPos;
            for (Slot slot : this.menu.slots) {
                if (slot.hasItem()) {
                    int sx = x + slot.x - 1;
                    int sy = y + slot.y - 1;
                    boolean match = matchesFilter(slot.getItem(), query);
                    if (match) {
                        graphics.fill(sx, sy, sx + 18, sy + 18, SLOT_MATCH_BG);
                        graphics.renderOutline(sx, sy, 18, 18, SLOT_MATCH_BORDER);
                    } else {
                        graphics.fill(sx, sy, sx + 18, sy + 18, SLOT_DIM);
                    }
                }
            }
        }

        this.renderTooltip(graphics, mouseX, mouseY);

        if (this.upgradeButton != null && this.upgradeButton.isHovered()) {
            var player = Minecraft.getInstance().player;
            int lvl = 1;
            if (player != null) {
                lvl = com.soybe.awakeningsystem.SystemEvents.data(player).subspaceLevel();
            }
            List<Component> tip = new ArrayList<>();
            tip.add(Component.translatable("awakening_system.subspace.tooltip.upgrade_title"));
            tip.add(Component.translatable("awakening_system.subspace.tooltip.current_level", lvl, 3));
            if (lvl < 3) {
                int next = lvl + 1;
                int cost = switch (next) {
                    case 2 -> 2500;
                    case 3 -> 8000;
                    case 4 -> 20000;
                    default -> 50000;
                };
                tip.add(Component.translatable("awakening_system.subspace.tooltip.next_level", next));
                tip.add(Component.translatable("awakening_system.subspace.tooltip.cost", cost));
                tip.add(Component.translatable("awakening_system.subspace.tooltip.upgrade_desc"));
            } else {
                tip.add(Component.translatable("awakening_system.subspace.tooltip.max_level"));
            }
            graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
        }

        if (this.travelButton != null && this.travelButton.isHovered()) {
            boolean inSubspace = this.minecraft != null && this.minecraft.level != null &&
                    this.minecraft.level.dimension().location().getPath().equals("subspace_realm");
            List<Component> tip = new ArrayList<>();
            if (inSubspace) {
                tip.add(Component.translatable("awakening_system.subspace.tooltip.exit_title"));
                tip.add(Component.translatable("awakening_system.subspace.tooltip.exit_desc"));
            } else {
                tip.add(Component.translatable("awakening_system.subspace.tooltip.enter_title"));
                tip.add(Component.translatable("awakening_system.subspace.tooltip.enter_desc1"));
                tip.add(Component.translatable("awakening_system.subspace.tooltip.enter_desc2"));
                tip.add(Component.translatable("awakening_system.subspace.tooltip.enter_desc3"));
            }
            graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
        }

        for (Slot slot : this.menu.slots) {
            if (slot instanceof SubspaceMenu.SubspaceSlot subSlot && !subSlot.isSlotUnlocked()) {
                int sx = this.leftPos + slot.x;
                int sy = this.topPos + slot.y;
                if (mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18) {
                    List<Component> tip = new ArrayList<>();
                    tip.add(Component.translatable("awakening_system.subspace.tooltip.slot_locked_title"));
                    tip.add(Component.translatable("awakening_system.subspace.tooltip.slot_locked_desc1"));
                    tip.add(Component.translatable("awakening_system.subspace.tooltip.slot_locked_desc2"));
                    tip.add(Component.translatable("awakening_system.subspace.tooltip.slot_locked_desc3"));
                    graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
                    break;
                }
            }
        }

        var player = Minecraft.getInstance().player;
        int countTooltip = 0;
        if (player != null) {
            var data = com.soybe.awakeningsystem.SystemEvents.data(player);
            if (data != null && data.getSubspaceItems() != null) {
                for (var stack : data.getSubspaceItems()) {
                    if (stack != null && !stack.isEmpty()) countTooltip++;
                }
            }
        }
        int unlockedTooltip = this.menu.getUnlockedSlots();
        int capWTooltip = this.font.width(countTooltip + " / " + unlockedTooltip);
        int capAreaX = this.leftPos + this.imageWidth - 8 - capWTooltip - 2;
        int capAreaY = this.topPos + this.inventoryLabelY - 2;
        if (mouseX >= capAreaX && mouseX <= this.leftPos + this.imageWidth - 6 && mouseY >= capAreaY && mouseY <= capAreaY + 12) {
            int unlocked = this.menu.getUnlockedSlots();
            int chests = this.menu.getChestCount();
            List<Component> tip = new ArrayList<>();
            tip.add(Component.translatable("awakening_system.subspace.tooltip.cap_title"));
            tip.add(Component.translatable("awakening_system.subspace.tooltip.cap_available", unlocked));
            tip.add(Component.translatable("awakening_system.subspace.tooltip.cap_chests", chests, chests / 2));
            tip.add(Component.translatable("awakening_system.subspace.tooltip.cap_desc"));
            graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        // 1. Resplandor exterior de alta tecnología
        g.fill(x - 3, y - 3, x + w + 3, y + h + 3, 0x1E00E5FF);
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x3300E5FF);

        // 2. Contorno principal Neon Cyan (Idéntico a System Screen)
        g.renderOutline(x - 1, y - 1, w + 2, h + 2, EDGE_BORDER);
        g.fill(x, y, x + w, y + h, PANEL_BG);

        // 3. Cantoneras angulares sci-fi (L-brackets estilo Solo Leveling)
        int cornerLen = 8;
        int cCol = CYAN_PRIMARY;
        // Superior Izquierda
        g.fill(x - 1, y - 1, x + cornerLen, y + 1, cCol);
        g.fill(x - 1, y - 1, x + 1, y + cornerLen, cCol);
        // Superior Derecha
        g.fill(x + w - cornerLen, y - 1, x + w + 1, y + 1, cCol);
        g.fill(x + w - 1, y - 1, x + w + 1, y + cornerLen, cCol);
        // Inferior Izquierda
        g.fill(x - 1, y + h - 1, x + cornerLen, y + h + 1, cCol);
        g.fill(x - 1, y + h - cornerLen, x + 1, y + h + 1, cCol);
        // Inferior Derecha
        g.fill(x + w - cornerLen, y + h - 1, x + w + 1, y + h + 1, cCol);
        g.fill(x + w - 1, y + h - cornerLen, x + w + 1, y + h + 1, cCol);

        // 4. Barra Superior del Buscador
        int searchBoxX = this.leftPos + 56;
        int searchBoxW = this.imageWidth - 62;
        g.fill(searchBoxX, y + 3, searchBoxX + searchBoxW, y + 15, 0xDD040E1A);
        g.renderOutline(searchBoxX, y + 3, searchBoxW, 12, (this.searchBox != null && this.searchBox.isFocused()) ? 0xFF00FFFF : 0x6600E5FF);

        // 5. Renderizado de Ranuras (Slots)
        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;

            if (slot instanceof SubspaceMenu.SubspaceSlot subSlot) {
                boolean unlocked = subSlot.isSlotUnlocked();
                int slotBg = unlocked ? SLOT_BG : 0x772A0808;
                int slotBorder = unlocked ? SLOT_BORDER : 0x66FF4444;

                g.fill(sx, sy, sx + 18, sy + 18, slotBg);
                g.renderOutline(sx, sy, 18, 18, slotBorder);

                if (!unlocked) {
                    g.drawCenteredString(this.font, "§c🔒", sx + 9, sy + 5, 0xFFFFFFFF);
                }
            } else {
                g.fill(sx, sy, sx + 18, sy + 18, 0x55040E1A);
                g.renderOutline(sx, sy, 18, 18, 0x2200E5FF);
            }
        }

        // 6. Separadores horizontales Cyan Neón
        g.fill(x + 8, y + 127, x + w - 8, y + 128, CYAN_PRIMARY);
        g.fill(x + 16, y + 126, x + w - 16, y + 129, 0x3300E5FF);

        g.fill(x + 8, y + 220, x + w - 8, y + 221, CYAN_PRIMARY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        var player = Minecraft.getInstance().player;
        int lvl = 1;
        if (player != null) {
            lvl = com.soybe.awakeningsystem.SystemEvents.data(player).subspaceLevel();
        }

        int count = 0;
        if (player != null) {
            var data = com.soybe.awakeningsystem.SystemEvents.data(player);
            if (data != null && data.getSubspaceItems() != null) {
                for (var stack : data.getSubspaceItems()) {
                    if (stack != null && !stack.isEmpty()) count++;
                }
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
        int capColor = (count >= unlocked) ? 0xFFFF5555 : (count >= unlocked * 0.85 ? 0xFFFFAA00 : 0xFF00E5FF);

        // Indicador de Paginación o Filtro
        String query = this.searchBox != null ? this.searchBox.getValue().trim().toLowerCase() : "";
        if (!query.isEmpty()) {
            int matchCount = 0;
            for (Slot slot : this.menu.slots) {
                if (slot instanceof SubspaceMenu.SubspaceSlot && slot.hasItem() && matchesFilter(slot.getItem(), query)) {
                    matchCount++;
                }
            }
            String matchStr = (matchCount > 0) ? "§b⚡ " + matchCount + " coinc." : "§c✖ 0 coinc.";
            g.drawString(this.font, matchStr, 8, this.inventoryLabelY, 0xFFFFFFFF, false);
        } else {
            Component invLabel = Component.translatable("awakening_system.subspace.hunter_inventory");
            int maxInvW = this.imageWidth - 16 - capW - 4;
            if (this.font.width(invLabel) > maxInvW) {
                invLabel = Component.translatable("container.inventory");
            }
            g.drawString(this.font, invLabel, 8, this.inventoryLabelY, MUTED, false);
        }

        g.drawString(this.font, capText, this.imageWidth - 8 - capW, this.inventoryLabelY, capColor, false);
    }

    public enum HoloTheme {
        CYAN(
                0xEE05121E, 0xFF082238,
                0xFF00E5FF, 0xFF00FFFF,
                0xFFFFFFFF, 0xFFFFFFFF,
                0x4400E5FF, 0x8800E5FF,
                0x6600E5FF
        ),
        RED(
                0xEE240A10, 0xFF3C0E1A,
                0xFFFF3355, 0xFFFF5577,
                0xFFFFFFFF, 0xFFFFFFFF,
                0x44FF3355, 0x88FF3355,
                0x66FF3355
        ),
        GOLD(
                0xEE221605, 0xFF3D2908,
                0xFFFFD700, 0xFFFFE844,
                0xFFFFFFFF, 0xFFFFFFFF,
                0x44FFD700, 0x88FFD700,
                0x66FFD700
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
            Minecraft mc = Minecraft.getInstance();
            boolean hovered = this.isHoveredOrFocused();

            int bx = this.getX();
            int by = this.getY();
            int bw = this.getWidth();
            int bh = this.getHeight();

            int bg = hovered ? theme.hoverBg : theme.baseBg;
            int border = hovered ? theme.hoverBorder : theme.baseBorder;
            int corner = hovered ? theme.hoverCorner : theme.baseCorner;
            int highlight = hovered ? theme.hoverHighlight : theme.baseHighlight;

            if (hovered) {
                graphics.renderOutline(bx - 1, by - 1, bw + 2, bh + 2, theme.glowColor);
            }

            graphics.fill(bx, by, bx + bw, by + bh, bg);
            graphics.renderOutline(bx, by, bw, bh, border);
            graphics.fill(bx + 1, by + 1, bx + bw - 1, by + 2, highlight);

            // Esquinas iluminadas
            graphics.fill(bx, by, bx + 2, by + 2, corner);
            graphics.fill(bx + bw - 2, by, bx + bw, by + 2, corner);
            graphics.fill(bx, by + bh - 2, bx + 2, by + bh, corner);
            graphics.fill(bx + bw - 2, by + bh - 2, bx + bw, by + bh, corner);

            Component text = hovered ? this.hoverLabel : this.normalLabel;
            int textColor = hovered ? 0xFFFFFFFF : 0xFF00E5FF;
            graphics.drawCenteredString(mc.font, text, bx + bw / 2, by + (bh - 8) / 2, textColor);
        }
    }
}
