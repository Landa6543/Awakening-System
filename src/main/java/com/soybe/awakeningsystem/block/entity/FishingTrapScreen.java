package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class FishingTrapScreen extends AbstractContainerScreen<FishingTrapMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/fishingblockgui.png");

    private Button toggleBtn;

    public FishingTrapScreen(FishingTrapMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();

        this.toggleBtn = new AestheticTrapButton(this.leftPos + 55, this.topPos + 76, 90, 18, button -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            }
        });

        this.addRenderableWidget(this.toggleBtn);
        updateButtonVisual();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtonVisual();
    }

    private void updateButtonVisual() {
        if (this.toggleBtn == null) return;
        boolean active = this.menu.isActive();
        if (active) {
            this.toggleBtn.setMessage(Component.translatable("block.awakening_system.fishing_trap.btn.deactivate"));
            this.toggleBtn.setTooltip(Tooltip.create(Component.translatable("block.awakening_system.fishing_trap.btn.deactivate.tooltip")));
        } else {
            this.toggleBtn.setMessage(Component.translatable("block.awakening_system.fishing_trap.btn.activate"));
            this.toggleBtn.setTooltip(Tooltip.create(Component.translatable("block.awakening_system.fishing_trap.btn.activate.tooltip")));
        }
    }

    private class AestheticTrapButton extends Button {
        public AestheticTrapButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            boolean active = menu.isActive();
            boolean hovered = this.isHoveredOrFocused();

            int bx = getX();
            int by = getY();
            int bw = getWidth();
            int bh = getHeight();

            int bgTop = active ? (hovered ? 0xF0183F2B : 0xEE0E2B1D) : (hovered ? 0xF03B1E1E : 0xEE2A1313);
            int bgBottom = active ? (hovered ? 0xFA0E281C : 0xF5081A12) : (hovered ? 0xFA241111 : 0xF5180B0B);
            int borderCol = active ? (hovered ? 0xFF55FFAA : 0xFF2ECC71) : (hovered ? 0xFFFF7777 : 0xFFE74C3C);
            int innerBorder = active ? (hovered ? 0x882ECC71 : 0x442ECC71) : (hovered ? 0x88E74C3C : 0x44E74C3C);

            g.fillGradient(bx, by, bx + bw, by + bh, bgTop, bgBottom);
            g.renderOutline(bx, by, bw, bh, borderCol);
            g.renderOutline(bx + 1, by + 1, bw - 2, bh - 2, innerBorder);

            if (hovered) {
                g.renderOutline(bx + 1, by + 1, bw - 2, bh - 2, 0x66FFFFFF);
            }

            int accentCol = active ? 0xFF00FFCC : 0xFFFF5555;
            g.fill(bx, by, bx + 3, by + 1, accentCol);
            g.fill(bx, by, bx + 1, by + 3, accentCol);
            g.fill(bx + bw - 3, by, bx + bw, by + 1, accentCol);
            g.fill(bx + bw - 1, by, bx + bw, by + 3, accentCol);
            g.fill(bx, by + bh - 1, bx + 3, by + bh, accentCol);
            g.fill(bx, by + bh - 3, bx + 1, by + bh, accentCol);
            g.fill(bx + bw - 3, by + bh - 1, bx + bw, by + bh, accentCol);
            g.fill(bx + bw - 1, by + bh - 3, bx + bw, by + bh, accentCol);

            Component msg = getMessage();
            int textW = font.width(msg);
            int textX = bx + (bw - textW) / 2;
            int textY = by + (bh - 8) / 2;
            int textColor = active ? (hovered ? 0xFFFFFFFF : 0xFFB9F6CA) : (hovered ? 0xFFFFFFFF : 0xFFFFCDD2);
            g.drawString(font, msg, textX, textY, textColor, true);
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0x77060D15);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, Component.translatable("block.awakening_system.fishing_trap.bait_label"), 12, 21, 0xFFB0BEC5, false);
        g.drawString(this.font, Component.translatable("block.awakening_system.fishing_trap.catch_label"), 12, 61, 0xFFB0BEC5, false);
        g.drawString(this.font, this.playerInventoryTitle, 20, 105, 0xFFB0BEC5, false);

        int status = this.menu.getStatus();
        Component statusComponent;
        int statusColor;
        switch (status) {
            case 4 -> {
                statusComponent = Component.translatable("block.awakening_system.fishing_trap.status.disabled");
                statusColor = 0xFFAAAAAA;
            }
            case 5 -> {
                statusComponent = Component.translatable("block.awakening_system.fishing_trap.status.redstone");
                statusColor = 0xFFFF5555;
            }
            case 0 -> {
                statusComponent = Component.translatable("block.awakening_system.fishing_trap.status.requires_water");
                statusColor = 0xFFFF5555;
            }
            case 1 -> {
                statusComponent = Component.translatable("block.awakening_system.fishing_trap.status.requires_bait");
                statusColor = 0xFFFFAA00;
            }
            case 3 -> {
                statusComponent = Component.translatable("block.awakening_system.fishing_trap.status.full");
                statusColor = 0xFFFFAA00;
            }
            default -> {
                int progress = this.menu.getProgress();
                int totalTime = this.menu.getTotalTime();
                int pct = (totalTime > 0) ? Math.min(100, progress * 100 / totalTime) : 0;
                statusComponent = Component.translatable("block.awakening_system.fishing_trap.status.fishing", pct);
                statusColor = 0xFF55FFFF;
            }
        }

        int textW = this.font.width(statusComponent);
        g.drawString(this.font, statusComponent, (this.imageWidth - textW) / 2, 98, statusColor, false);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Panel de fondo sólido para evitar que los bloques y mundo se trasluzcan o solapen
        g.fill(x - 2, y - 2, x + this.imageWidth + 2, y + this.imageHeight + 2, 0xFF1B2A4A);
        g.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF0D1829);
        g.fill(x, y, x + this.imageWidth, y + 14, 0xFF14243B);
        g.fill(x, y + 13, x + this.imageWidth, y + 14, 0xFF4A90E2);

        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        g.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 200, 200);

        int barX = x + 60;
        int barY = y + 108;
        int barW = 80;
        int barH = 5;

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF373737);
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF141923);

        int status = this.menu.getStatus();
        if (status == 2) {
            int progress = this.menu.getProgress();
            int totalTime = this.menu.getTotalTime();
            int filled = (totalTime > 0) ? Math.min(barW, progress * barW / totalTime) : 0;
            if (filled > 0) {
                g.fill(barX, barY, barX + filled, barY + 2, 0xFF55FFFF);
                g.fill(barX, barY + 2, barX + filled, barY + barH, 0xFF00AAAA);
            }
        }
    }
}
