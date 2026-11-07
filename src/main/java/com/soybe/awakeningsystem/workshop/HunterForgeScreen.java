package com.soybe.awakeningsystem.workshop;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import com.soybe.awakeningsystem.client.gui.ModernGuiHelper.GuiTheme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de la Mesa de Forja de Cazador, con el mismo cristal azul del resto del Sistema:
 * a la izquierda el holograma del objeto, a la derecha los cuatro materiales y el resultado,
 * y abajo la barra de templado con el botón de forjar.
 */
public class HunterForgeScreen extends AbstractContainerScreen<HunterForgeMenu> {

    private static final int BAR_X = 8, BAR_Y = 86, BAR_W = 114, BAR_H = 8;
    private static final int BTN_X = 128, BTN_Y = 82, BTN_W = 42, BTN_H = 16;
    private static final int PREVIEW_X = 6, PREVIEW_Y = 22, PREVIEW_W = 72, PREVIEW_H = 58;

    private GuiTheme theme = GuiTheme.SOLO_LEVELING;

    public HunterForgeScreen(HunterForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
        this.inventoryLabelY = 97;
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft != null && this.minecraft.player != null) {
            SystemData data = this.minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            this.theme = ModernGuiHelper.getTheme(data);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        int x = this.leftPos, y = this.topPos, w = this.imageWidth, h = this.imageHeight;

        // Ventana de cristal con borde de neón
        g.fillGradient(x, y, x + w, y + h, 0xE60A1828, 0xF0050C16);
        g.renderOutline(x, y, w, h, this.theme.primary);
        g.renderOutline(x + 2, y + 2, w - 4, h - 4, (this.theme.primary & 0x00FFFFFF) | 0x40000000);
        drawCorner(g, x, y, 1, 1);
        drawCorner(g, x + w - 1, y, -1, 1);
        drawCorner(g, x, y + h - 1, 1, -1);
        drawCorner(g, x + w - 1, y + h - 1, -1, -1);
        ModernGuiHelper.drawModernHeader(g, x + 4, y + 4, w - 8, 15, null, 0, this.theme, this.font);

        // Holograma del objeto que se forja
        ModernGuiHelper.drawModernSection(g, x + PREVIEW_X, y + PREVIEW_Y, PREVIEW_W, PREVIEW_H, null, this.theme, this.font);
        drawHologramBase(g, x + PREVIEW_X + PREVIEW_W / 2, y + PREVIEW_Y + PREVIEW_H - 8);
        ItemStack baseStack = this.menu.getSlot(HunterForgeMenu.SLOT_BASE).getItem();
        renderRotatingPreview(g, baseStack.isEmpty() ? new ItemStack(com.soybe.awakeningsystem.block.SystemBlocks.HUNTER_FORGE_TABLE.get()) : baseStack,
                x + PREVIEW_X + PREVIEW_W / 2, y + PREVIEW_Y + PREVIEW_H / 2 - 2, !baseStack.isEmpty());

        // Ranuras de materiales con su icono fantasma cuando están vacías
        drawForgeSlot(g, HunterForgeMenu.SLOT_BASE, new ItemStack(Items.IRON_SWORD));
        drawForgeSlot(g, HunterForgeMenu.SLOT_CORE, new ItemStack(SystemItems.MONSTER_CORE_E.get()));
        drawForgeSlot(g, HunterForgeMenu.SLOT_CATALYST, new ItemStack(Items.AMETHYST_SHARD));
        drawForgeSlot(g, HunterForgeMenu.SLOT_GEM, new ItemStack(Items.EMERALD));

        // Flecha hacia el resultado: late cuando todo está listo para forjar
        boolean canForge = this.menu.canForge();
        float pulse = canForge ? 0.5F + 0.5F * (float) Math.sin(System.currentTimeMillis() / 180.0D) : 0.0F;
        int arrowColor = canForge ? lerpColor(this.theme.primary, 0xFFFFFFFF, pulse) : 0xFF33475A;
        drawArrow(g, x + 130, y + 46, arrowColor);

        // Resultado, con un marco más grande
        Slot out = this.menu.getSlot(HunterForgeMenu.SLOT_OUTPUT);
        int ox = x + out.x - 3, oy = y + out.y - 3;
        g.fill(ox, oy, ox + 22, oy + 22, 0xCC06101C);
        g.renderOutline(ox, oy, 22, 22, out.hasItem() ? 0xFFFFD866 : this.theme.primary);
        g.renderOutline(ox + 1, oy + 1, 20, 20, (this.theme.primary & 0x00FFFFFF) | 0x50000000);

        drawTemperingBar(g, x + BAR_X, y + BAR_Y);

        boolean hovered = isOverButton(mouseX, mouseY);
        if (canForge) {
            ModernGuiHelper.drawModernButton(g, x + BTN_X, y + BTN_Y, BTN_W, BTN_H,
                    Component.translatable("awakening_system.workshop.forge_button"), hovered, this.theme, this.font);
        } else {
            g.fill(x + BTN_X, y + BTN_Y, x + BTN_X + BTN_W, y + BTN_Y + BTN_H, 0x40202833);
            g.renderOutline(x + BTN_X, y + BTN_Y, BTN_W, BTN_H, 0xFF3A4A5A);
            g.drawCenteredString(this.font, Component.translatable("awakening_system.workshop.forge_button"),
                    x + BTN_X + BTN_W / 2, y + BTN_Y + 4, 0xFF6A7A8A);
        }

        // Ranuras del inventario del jugador, sutiles
        for (Slot slot : this.menu.slots) {
            if (slot.index >= HunterForgeMenu.FORGE_SLOTS_COUNT) {
                g.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, 0x7006101C);
                g.renderOutline(x + slot.x - 1, y + slot.y - 1, 18, 18, 0x3800E5FF);
            }
        }
        g.fill(x + 8, y + 104, x + w - 8, y + 105, (this.theme.primary & 0x00FFFFFF) | 0x50000000);
    }

    private void drawForgeSlot(GuiGraphics g, int index, ItemStack ghost) {
        Slot slot = this.menu.getSlot(index);
        int sx = this.leftPos + slot.x - 1, sy = this.topPos + slot.y - 1;
        g.fill(sx, sy, sx + 18, sy + 18, 0xCC06101C);
        g.renderOutline(sx, sy, 18, 18, slot.hasItem() ? this.theme.primary : (this.theme.primary & 0x00FFFFFF) | 0x80000000);
        if (!slot.hasItem()) {
            g.renderItem(ghost, sx + 1, sy + 1);
            // Velo oscuro: el icono queda como silueta que indica qué va ahí
            g.pose().pushPose();
            g.pose().translate(0, 0, 200);
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xB806101C);
            g.pose().popPose();
        }
    }

    private void drawTemperingBar(GuiGraphics g, int bx, int by) {
        g.fill(bx - 1, by - 1, bx + BAR_W + 1, by + BAR_H + 1, 0xFF081420);
        int normalW = (int) (BAR_W * 0.42F);
        int rareW = (int) (BAR_W * 0.30F);
        int epicW = (int) (BAR_W * 0.20F);
        g.fillGradient(bx, by, bx + normalW, by + BAR_H, 0xFF3FA34D, 0xFF1F5E2A);
        g.fillGradient(bx + normalW, by, bx + normalW + rareW, by + BAR_H, 0xFF29B6D6, 0xFF0E5E78);
        g.fillGradient(bx + normalW + rareW, by, bx + normalW + rareW + epicW, by + BAR_H, 0xFFA05AE0, 0xFF4E2380);
        g.fillGradient(bx + normalW + rareW + epicW, by, bx + BAR_W, by + BAR_H, 0xFFFFD866, 0xFFC08A12);
        g.fill(bx, by, bx + BAR_W, by + 2, 0x40FFFFFF);
        // separadores entre zonas de calidad
        for (int sep : new int[]{normalW, normalW + rareW, normalW + rareW + epicW}) {
            g.fill(bx + sep, by, bx + sep + 1, by + BAR_H, 0xC0081420);
        }
        g.renderOutline(bx - 1, by - 1, BAR_W + 2, BAR_H + 2, this.theme.primary);

        int cursorX = bx + (int) ((BAR_W - 3) * (this.menu.getTemperingPos() / 100.0F));
        g.fill(cursorX - 2, by - 4, cursorX + 5, by + BAR_H + 4, 0x40FFFFFF);   // halo
        g.fill(cursorX, by - 3, cursorX + 3, by + BAR_H + 3, 0xFFFFFFFF);
        g.fill(cursorX + 1, by - 2, cursorX + 2, by + BAR_H + 2, 0xFFFF3366);
    }

    private void drawArrow(GuiGraphics g, int ax, int ay, int color) {
        g.fill(ax, ay - 2, ax + 11, ay + 2, color);
        // punta triangular rellena
        for (int i = 0; i < 6; i++) {
            g.fill(ax + 11 + i, ay - 6 + i, ax + 12 + i, ay + 6 - i, color);
        }
    }

    /** Anillo de luz bajo el holograma. */
    private void drawHologramBase(GuiGraphics g, int cx, int cy) {
        g.fill(cx - 22, cy, cx + 22, cy + 1, (this.theme.primary & 0x00FFFFFF) | 0xA0000000);
        g.fill(cx - 14, cy + 1, cx + 14, cy + 2, (this.theme.primary & 0x00FFFFFF) | 0x60000000);
        g.fillGradient(cx - 18, cy - 26, cx + 18, cy, 0x0000E5FF, (this.theme.primary & 0x00FFFFFF) | 0x28000000);
    }

    private void drawCorner(GuiGraphics g, int cx, int cy, int dx, int dy) {
        for (int i = 0; i < 6; i++) {
            g.fill(cx + dx * i, cy, cx + dx * i + 1, cy + dy, this.theme.core);
            g.fill(cx, cy + dy * i, cx + dx, cy + dy * i + 1, this.theme.core);
        }
    }

    private void renderRotatingPreview(GuiGraphics g, ItemStack stack, int px, int py, boolean active) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(px, py, 150.0F);
        float angle = (System.currentTimeMillis() % 6000L) / 6000.0F * 360.0F;
        float bob = (float) Math.sin(System.currentTimeMillis() / 500.0D) * 1.5F;
        pose.translate(0, bob, 0);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
        pose.scale(34.0F, -34.0F, 34.0F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        var model = itemRenderer.getModel(stack, this.minecraft.level, null, 0);
        if (!active) RenderSystem.setShaderColor(0.75F, 0.95F, 1.0F, 0.8F);
        itemRenderer.render(stack, ItemDisplayContext.FIXED, false, pose, g.bufferSource(), 0xF000F0, OverlayTexture.NO_OVERLAY, model);
        g.bufferSource().endBatch();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
    }

    private boolean isOverButton(double mouseX, double mouseY) {
        int bx = this.leftPos + BTN_X, by = this.topPos + BTN_Y;
        return mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H;
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return 0xFF000000 | ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isOverButton(mouseX, mouseY) && this.menu.canForge()
                && this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.menu.getTemperingPos());
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_HIT, 1.2F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, ModernGuiHelper.manhwa(this.title), 14, 8, this.theme.accent, false);
        g.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xFF8FA6B8, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);

        // Nombre de la ranura al pasar por encima de una vacía
        if (this.hoveredSlot != null && !this.hoveredSlot.hasItem() && this.hoveredSlot.index < HunterForgeMenu.FORGE_SLOTS_COUNT) {
            String key = switch (this.hoveredSlot.index) {
                case HunterForgeMenu.SLOT_BASE -> "base";
                case HunterForgeMenu.SLOT_CORE -> "core";
                case HunterForgeMenu.SLOT_CATALYST -> "catalyst";
                case HunterForgeMenu.SLOT_GEM -> "gem";
                default -> "output";
            };
            g.renderTooltip(this.font, Component.translatable("awakening_system.workshop.slot." + key), mouseX, mouseY);
        }

        int bx = this.leftPos + BAR_X, by = this.topPos + BAR_Y;
        if (mouseX >= bx && mouseX <= bx + BAR_W && mouseY >= by - 3 && mouseY <= by + BAR_H + 3) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.translatable("awakening_system.workshop.tempering_bar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            tip.add(Component.translatable("awakening_system.workshop.tempering_desc").withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal("• ").append(Component.translatable("awakening_system.workshop.quality.normal").withStyle(ChatFormatting.GREEN)));
            tip.add(Component.literal("• ").append(Component.translatable("awakening_system.workshop.quality.rare").withStyle(ChatFormatting.AQUA)));
            tip.add(Component.literal("• ").append(Component.translatable("awakening_system.workshop.quality.epic").withStyle(ChatFormatting.DARK_PURPLE)));
            tip.add(Component.literal("• ").append(Component.translatable("awakening_system.workshop.quality.legendary").withStyle(ChatFormatting.GOLD)));
            g.renderComponentTooltip(this.font, tip, mouseX, mouseY);
        }
    }
}
