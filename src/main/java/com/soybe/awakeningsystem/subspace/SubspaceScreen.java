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
    private SubspaceHoloButton updateStructureBtn;

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
        boolean inLimbo = this.minecraft != null && this.minecraft.level != null &&
                this.minecraft.level.dimension().location().getPath().equals("limbo");

        int btnY = this.topPos + 224;
        int btnW = 78;
        int btnH = 18;

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

        var player = net.minecraft.client.Minecraft.getInstance().player;
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

        if (this.upgradeButton != null && this.upgradeButton.isHovered()) {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            int lvl = 1;
            if (player != null) {
                lvl = com.soybe.awakeningsystem.SystemEvents.data(player).subspaceLevel();
            }
            List<Component> tip = new ArrayList<>();
            tip.add(Component.translatable("awakening_system.subspace.tooltip.upgrade_title"));
            tip.add(Component.literal("§7Nivel Actual: §b" + lvl + " / 3 (Próximamente 4 y 5)"));
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

        if (this.updateStructureBtn != null && this.updateStructureBtn.isHovered()) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("§6§lActualizar Diseño de Parcela"));
            tip.add(Component.literal("§7Regenera tu parcela con el nuevo"));
            tip.add(Component.literal("§7diseño arquitectónico de tu nivel."));
            tip.add(Component.literal("§c¡ADVERTENCIA! §fRetira tus cofres"));
            tip.add(Component.literal("§fe ítems antes de proceder."));
            tip.add(Component.literal("§e(Clic para ver advertencia en el chat)"));
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

        var player = net.minecraft.client.Minecraft.getInstance().player;
        int countTooltip = 0;
        if (player != null) {
            var items = com.soybe.awakeningsystem.SystemEvents.data(player).getSubspaceItems();
            for (var stack : items) {
                if (!stack.isEmpty()) countTooltip++;
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

        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x4017354B);
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, EDGE_BORDER);
        g.fill(x, y, x + w, y + h, PANEL_BG);

        g.fill(x, y, x + w, y + 2, CYAN);

        g.fill(x, y, x + 8, y + 2, CYAN);
        g.fill(x, y, x + 2, y + 8, CYAN);
        g.fill(x + w - 8, y, x + w, y + 2, CYAN);
        g.fill(x + w - 2, y, x + w, y + 8, CYAN);
        g.fill(x, y + h - 2, x + 8, y + h, CYAN);
        g.fill(x, y + h - 8, x + 2, y + h, CYAN);
        g.fill(x + w - 8, y + h - 2, x + w, y + h, CYAN);
        g.fill(x + w - 2, y + h - 8, x + w, y + h, CYAN);

        g.fill(x + 6, y + 4, x + w - 6, y + 15, 0x990D2235);
        g.renderOutline(x + 6, y + 4, w - 12, 12, 0x662D7691);

        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;

            if (slot instanceof SubspaceMenu.SubspaceSlot subSlot) {
                boolean unlocked = subSlot.isSlotUnlocked();
                int slotBg = unlocked ? SLOT_BG : 0x772A0808;
                int slotBorder = unlocked ? SLOT_BORDER : 0x55FF4444;

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

        g.fill(x + 8, y + 127, x + w - 8, y + 128, 0x4470E8FF);

        g.fill(x + 8, y + 220, x + w - 8, y + 221, 0x3370E8FF);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        int lvl = 1;
        if (player != null) {
            lvl = com.soybe.awakeningsystem.SystemEvents.data(player).subspaceLevel();
        }

        int totalPages = this.menu.getTotalPages();
        int curPage = this.menu.getCurrentPage() + 1;
        Component title = (totalPages > 1)
                ? Component.translatable("awakening_system.subspace.title_paged", curPage, totalPages)
                : Component.translatable("awakening_system.subspace.title_level", lvl);
        g.drawCenteredString(this.font, title, this.imageWidth / 2, 6, CYAN);

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

        Component invLabel = Component.translatable("awakening_system.subspace.hunter_inventory");
        int maxInvW = this.imageWidth - 16 - capW - 4;
        if (this.font.width(invLabel) > maxInvW) {
            invLabel = Component.translatable("container.inventory");
        }
        g.drawString(this.font, invLabel, 8, this.inventoryLabelY, MUTED, false);
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

            if (hovered) {
                graphics.renderOutline(bx - 1, by - 1, bw + 2, bh + 2, theme.glowColor);
            }

            graphics.fill(bx, by, bx + bw, by + bh, bg);

            graphics.renderOutline(bx, by, bw, bh, border);

            graphics.fill(bx + 1, by + 1, bx + bw - 1, by + 2, highlight);

            graphics.fill(bx, by, bx + 2, by + 2, corner);
            graphics.fill(bx + bw - 2, by, bx + bw, by + 2, corner);
            graphics.fill(bx, by + bh - 2, bx + 2, by + bh, corner);
            graphics.fill(bx + bw - 2, by + bh - 2, bx + bw, by + bh, corner);

            Component text = this.normalLabel;
            graphics.drawCenteredString(mc.font, text, bx + bw / 2, by + (bh - 8) / 2, hovered ? 0xFFFFFFFF : 0xFFCCCCCC);
        }
    }
}
