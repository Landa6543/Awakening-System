package com.soybe.awakeningsystem.item.pouch;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PouchScreen extends AbstractContainerScreen<PouchMenu> {
    public PouchScreen(PouchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = menu.getTier().getGuiWidth();
        this.imageHeight = menu.getTier().getGuiHeight();
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = switch (menu.getTier()) {
            case WANDERER -> 8;
            case ADVENTURER -> 8;
            case OUTLANDER -> 28;
            case EMERGENCY -> 81;
        };
        this.inventoryLabelY = switch (menu.getTier()) {
            case WANDERER -> 62;
            case ADVENTURER -> 72;
            case OUTLANDER -> 124;
            case EMERGENCY -> 144;
        };
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(menu.getTier().getTexture(), this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}
