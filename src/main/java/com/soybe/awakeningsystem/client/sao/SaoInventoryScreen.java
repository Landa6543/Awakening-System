package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The vanilla inventory in the full-dive look: the same slots, crafting grid, recipe book and player model, drawn
 * on the white floating panel: rounded slot wells (equipment edged in orange, the held hotbar slot ringed), the
 * player standing on a lit disc, the crafting result in its own frame, and the player's name and level in the
 * header. Only the drawing changes, so everything that works in the vanilla inventory works here.
 */
public class SaoInventoryScreen extends InventoryScreen {
    private static final int HEADER_H = 18;
    private static final int RESULT = 0, ARMOR_FIRST = 5, ARMOR_LAST = 8, HOTBAR_FIRST = 36, HOTBAR_LAST = 44, OFFHAND = 45;
    private int ticks;

    public SaoInventoryScreen(Player player) {
        super(player);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        ticks++;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos, y = this.topPos;
        float open = Math.min(1.0F, (ticks + partialTick) / 4.0F);
        float a = open * open * (3 - 2 * open);

        // the panel, with the header strip above the slots
        panel(g, x - 8, y - HEADER_H - 6, this.imageWidth + 16, this.imageHeight + HEADER_H + 14, HEADER_H, a);
        int hy = y - HEADER_H - 6;
        shinyDisc(g, x + 4, hy + HEADER_H / 2.0F, 6.5F, ORANGE, a);
        icon(g, PERSON, x - 1, hy + HEADER_H / 2 - 5, alpha(WHITE, a));
        g.drawString(this.font, Component.translatable("container.inventory"), x + 15, hy + 5, alpha(GREY_DARK, a), false);
        if (this.minecraft != null && this.minecraft.player != null) {
            int lvl = this.minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM).level();
            String who = this.minecraft.player.getName().getString() + "  ·  Lv " + lvl;
            g.drawString(this.font, who, x + this.imageWidth - this.font.width(who), hy + 5, alpha(ORANGE_DARK, a), false);
        }

        // the player on a lit disc, inside a soft rounded frame
        rounded(g, x + 25, y + 7, 51, 72, 0xFFC9C9CE, 0xFFF7F7F9, 0xFFE2E2E7, a);
        radial(g, x + 50, y + 40, x + 50, y + 40, 26, alpha(0x60FFFFFF, a), alpha(0x00FFFFFF, a));
        // a soft shadow and a thin orange line under the feet
        g.fillGradient(x + 34, y + 73, x + 67, y + 76, alpha(0x30000000, a), alpha(0x08000000, a));
        g.fill(x + 30, y + 76, x + 71, y + 77, alpha(ORANGE, a * 0.8F));

        // backpack and hotbar, split by a thin orange line with a bead in the middle
        g.fill(x + 8, y + 139, x + 168, y + 140, alpha(0xFFDADADF, a));
        g.fill(x + 70, y + 139, x + 106, y + 140, alpha(ORANGE, a));

        int held = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getInventory().selected : -1;
        for (int i = 0; i < this.menu.slots.size(); i++) {
            Slot s = this.menu.slots.get(i);
            if (!s.isActive()) continue;
            int sx = x + s.x - 1, sy = y + s.y - 1;
            if (i == RESULT) {
                // the crafting result: a bigger rounded frame, kept inside the panel
                rounded(g, sx - 3, sy - 3, 24, 24, ORANGE, 0xFFFFFFFF, 0xFFF0E6D2, a);
                continue;
            }
            boolean equipment = (i >= ARMOR_FIRST && i <= ARMOR_LAST) || i == OFFHAND;
            boolean heldSlot = i >= HOTBAR_FIRST && i <= HOTBAR_LAST && i - HOTBAR_FIRST == held;
            rounded(g, sx, sy, 18, 18, heldSlot ? ORANGE : 0xFFC9C9CE, 0xFFE9E9ED, 0xFFF7F7F9, a);
            if (equipment) g.fill(sx + 1, sy + 3, sx + 2, sy + 15, alpha(ORANGE, a));
        }

        // a chevron from the crafting grid to the result
        int ax = x + 135, ay = y + 36;
        for (int i = 0; i < 4; i++) {
            g.fill(ax + i, ay - 3 + i, ax + i + 2, ay - 2 + i, alpha(ORANGE, a));
            g.fill(ax + i, ay + 3 - i, ax + i + 2, ay + 4 - i, alpha(ORANGE, a));
        }

        renderEntityInInventoryFollowsMouse(g, x + 26, y + 8, x + 75, y + 78, 30, 0.0625F, mouseX, mouseY, this.minecraft.player);
    }

    /** A rounded well: a one-pixel border with its corners cut, a gradient inside and a faint shade along the top. */
    private static void rounded(GuiGraphics g, int x, int y, int w, int h, int border, int top, int bottom, float a) {
        g.fill(x + 1, y, x + w - 1, y + 1, alpha(border, a));
        g.fill(x + 1, y + h - 1, x + w - 1, y + h, alpha(border, a));
        g.fill(x, y + 1, x + 1, y + h - 1, alpha(border, a));
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, alpha(border, a));
        g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, alpha(top, a), alpha(bottom, a));
        g.fill(x + 1, y + 1, x + w - 1, y + 2, alpha(0x1A000000, a));
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // the "Crafting" label, in the panel's grey
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, GREY, false);
    }
}
