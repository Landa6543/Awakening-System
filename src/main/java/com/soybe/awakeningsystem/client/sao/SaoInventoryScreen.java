package com.soybe.awakeningsystem.client.sao;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The vanilla inventory in the full-dive look: the same slots, crafting grid, recipe book and player model, drawn
 * on the white floating panel with soft slot wells (equipment marked in orange) instead of the grey texture.
 * Only the drawing changes, so everything that works in the vanilla inventory works here.
 */
public class SaoInventoryScreen extends InventoryScreen {
    private static final int HEADER_H = 18;

    public SaoInventoryScreen(Player player) {
        super(player);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos, y = this.topPos;
        // the panel, with a header strip above the slots
        panel(g, x - 6, y - HEADER_H - 4, this.imageWidth + 12, this.imageHeight + HEADER_H + 10, HEADER_H, 1.0F);
        shinyDisc(g, x + 5, y - HEADER_H / 2.0F - 4, 6.5F, ORANGE, 1.0F);
        icon(g, PERSON, x, y - HEADER_H / 2 - 9, WHITE);
        g.drawString(this.font, Component.translatable("container.inventory"), x + 16, y - HEADER_H / 2 - 8, GREY_DARK, false);

        // the player, in a soft well
        well(g, x + 25, y + 7, 51, 72, false);
        g.fillGradient(x + 26, y + 8, x + 75, y + 78, 0xFFF4F4F6, 0xFFD8D8DE);

        // a thin orange line between the backpack and the hotbar
        g.fill(x + 8, y + 139, x + 168, y + 140, alpha(ORANGE, 0.6F));

        // the slot wells
        for (int i = 0; i < this.menu.slots.size(); i++) {
            Slot s = this.menu.slots.get(i);
            if (!s.isActive()) continue;
            int sx = x + s.x - 1, sy = y + s.y - 1;
            if (i == InventoryScreenSlots.RESULT) {
                well(g, sx - 4, sy - 4, 26, 26, true);
                ring(g, sx + 9, sy + 9, 15, 1.0F, alpha(ORANGE, 0.7F));
            } else {
                boolean equipment = i >= InventoryScreenSlots.ARMOR_FIRST && i <= InventoryScreenSlots.ARMOR_LAST || i == InventoryScreenSlots.OFFHAND;
                well(g, sx, sy, 18, 18, equipment);
            }
        }
        // the arrow from the crafting grid to the result
        pointerRight(g, x + 136, y + 32, 5, ORANGE);
        g.fill(x + 134, y + 35, x + 138, y + 37, ORANGE);

        renderEntityInInventoryFollowsMouse(g, x + 26, y + 8, x + 75, y + 78, 30, 0.0625F, mouseX, mouseY, this.minecraft.player);
    }

    /** A slot well: light inside, a soft border, a shadow along the top; equipment gets an orange edge. */
    private static void well(GuiGraphics g, int x, int y, int w, int h, boolean accent) {
        g.fill(x, y, x + w, y + h, 0xFFC4C4CA);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFE6E6EA, 0xFFF6F6F8);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x22000000);
        if (accent) g.fill(x, y, x + 2, y + h, ORANGE);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // the "Crafting" label, in the panel's grey
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, GREY, false);
    }

    /** The vanilla inventory menu's slot numbers. */
    private static final class InventoryScreenSlots {
        static final int RESULT = 0;
        static final int ARMOR_FIRST = 5;
        static final int ARMOR_LAST = 8;
        static final int OFFHAND = 45;
    }
}
