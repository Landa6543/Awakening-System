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

        // Botón Activar / Desactivar centrado y alineado con las ranuras de salida
        // Ranuras van de x=58 a x=136. Botón de ancho 80 en x=60, y=78, alto=18
        this.toggleBtn = Button.builder(Component.literal("..."), button -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            }
        }).bounds(this.leftPos + 60, this.topPos + 78, 80, 18)
        .build();

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
            this.toggleBtn.setMessage(Component.literal("§cDesactivar"));
            this.toggleBtn.setTooltip(Tooltip.create(Component.literal("§bPesca automática: §aACTIVADA\n§7Haz clic para §cdesactivar§7 la trampa.")));
        } else {
            this.toggleBtn.setMessage(Component.literal("§aActivar"));
            this.toggleBtn.setTooltip(Tooltip.create(Component.literal("§bPesca automática: §cDESACTIVADA\n§7Haz clic para §aactivar§7 la trampa cada 5s.")));
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Tinte suave nítido sin activar el post-processing shader de desenfoque (blur)
        g.fill(0, 0, this.width, this.height, 0x55060D15);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Textos canónicos de la interfaz original
        g.drawString(this.font, Component.literal("Bait"), 22, 21, 0xFFE0E0E0, false);
        g.drawString(this.font, Component.literal("Cebo"), 157, 21, 0xFFE0E0E0, false);

        // Indicador de estado centrado debajo del botón y sobre la barra de progreso (y = 98)
        int status = this.menu.getStatus();
        String statusText;
        int statusColor;
        switch (status) {
            case 4 -> {
                statusText = "§7⏸ Desactivada";
                statusColor = 0xFFAAAAAA;
            }
            case 5 -> {
                statusText = "§c⚡ Pausada por Redstone";
                statusColor = 0xFFFF5555;
            }
            case 0 -> {
                statusText = "§c⚠ Requiere agua";
                statusColor = 0xFFFF5555;
            }
            case 1 -> {
                statusText = "§e⚠ Requiere cebo";
                statusColor = 0xFFFFAA00;
            }
            case 3 -> {
                statusText = "§6⚠ Salidas llenas";
                statusColor = 0xFFFFAA00;
            }
            default -> {
                int progress = this.menu.getProgress();
                int totalTime = this.menu.getTotalTime();
                int pct = (totalTime > 0) ? Math.min(100, progress * 100 / totalTime) : 0;
                statusText = "§b⚡ Pescando: §a" + pct + "%";
                statusColor = 0xFF55FFFF;
            }
        }

        int textW = this.font.width(statusText);
        g.drawString(this.font, statusText, (this.imageWidth - textW) / 2, 98, statusColor, false);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        g.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 200, 200);

        // Barra de progreso reposicionada elegantemente en la zona inferior (y + 108)
        // Ancho 80 píxeles, centrada exactamente con el botón y las ranuras
        int barX = x + 60;
        int barY = y + 108;
        int barW = 80;
        int barH = 5;

        // Borde exterior
        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF373737);
        // Fondo interior de la ranura
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF141923);

        int status = this.menu.getStatus();
        if (status == 2) {
            int progress = this.menu.getProgress();
            int totalTime = this.menu.getTotalTime();
            int filled = (totalTime > 0) ? Math.min(barW, progress * barW / totalTime) : 0;
            if (filled > 0) {
                // Brillo superior cian claro y base cian profundo
                g.fill(barX, barY, barX + filled, barY + 2, 0xFF55FFFF);
                g.fill(barX, barY + 2, barX + filled, barY + barH, 0xFF00AAAA);
            }
        }
    }
}
