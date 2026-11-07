package com.soybe.awakeningsystem.workshop;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de la Mesa de Forja de Cazador. El fondo está pintado (plancha de acero remachada,
 * boca de horno con brasas, ranuras hundidas); aquí solo se dibuja lo que se mueve: las brasas,
 * el objeto girando dentro del horno, la flecha de metal fundido y la barra de templado.
 */
public class HunterForgeScreen extends AbstractContainerScreen<HunterForgeMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/container/hunter_forge_screen.png");

    private static final int BAR_X = 8, BAR_Y = 86, BAR_W = 114, BAR_H = 8;
    private static final int BTN_X = 128, BTN_Y = 82, BTN_W = 42, BTN_H = 16;
    // interior de la boca del horno
    private static final int MOUTH_X = 10, MOUTH_Y = 26, MOUTH_W = 64, MOUTH_H = 50;

    private static final int EMBER = 0xFFF59A3C, EMBER_HOT = 0xFFFFD98A, CYAN = 0xFF3FD1E0, STEEL_TEXT = 0xFF9FB0D0;

    public HunterForgeScreen(HunterForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos, y = this.topPos;
        RenderSystem.enableBlend();
        g.blit(BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        long time = System.currentTimeMillis();

        // Brasas que respiran en el fondo del horno
        float flicker = 0.5F + 0.25F * (float) Math.sin(time / 230.0D) + 0.25F * (float) Math.sin(time / 97.0D);
        int glowAlpha = (int) (40 + 50 * flicker);
        g.fillGradient(x + MOUTH_X, y + MOUTH_Y + MOUTH_H - 26, x + MOUTH_X + MOUTH_W, y + MOUTH_Y + MOUTH_H - 4,
                0x00F59A3C, (glowAlpha << 24) | 0xF59A3C);

        // El objeto que se forja flota y gira dentro del horno; vacío, se ve la propia forja
        ItemStack baseStack = this.menu.getSlot(HunterForgeMenu.SLOT_BASE).getItem();
        ItemStack shown = baseStack.isEmpty()
                ? new ItemStack(com.soybe.awakeningsystem.block.SystemBlocks.HUNTER_FORGE_TABLE.get()) : baseStack;
        // vacía, la forja de muestra baja un poco para dejar sitio al texto de ayuda
        renderRotatingItem(g, shown, x + MOUTH_X + MOUTH_W / 2, y + MOUTH_Y + MOUTH_H / 2 + (baseStack.isEmpty() ? 9 : -2), !baseStack.isEmpty());

        int strike = this.menu.getStrikeTicks();
        if (strike > 0) {
            // Resultado del golpe: destello del color de la calidad y su nombre en grande
            HunterForgeQuality quality = this.menu.getLastQuality();
            int color = quality.getColorHex();
            int flashAlpha = Math.min(160, strike * 8);
            g.fill(x + MOUTH_X, y + MOUTH_Y, x + MOUTH_X + MOUTH_W, y + MOUTH_Y + MOUTH_H, (flashAlpha << 24) | color);
            g.pose().pushPose();
            g.pose().translate(x + MOUTH_X + MOUTH_W / 2.0F, y + MOUTH_Y + 8, 300);
            g.pose().scale(1.4F, 1.4F, 1.0F);
            g.drawCenteredString(this.font, quality.getDisplayName(), 0, 0, 0xFF000000 | color);
            g.pose().popPose();
        } else if (baseStack.isEmpty()) {
            // Sin objeto: explica para qué sirve la forja
            g.pose().pushPose();
            g.pose().translate(x + MOUTH_X + 3, y + MOUTH_Y + 3, 300);
            g.pose().scale(0.6F, 0.6F, 1.0F);
            int line = 0;
            for (var text : this.font.split(Component.translatable("awakening_system.workshop.hint"), (int) ((MOUTH_W - 6) / 0.6F))) {
                g.drawString(this.font, text, 0, line * 10, 0xFFE8EEFA, true);
                line++;
            }
            g.pose().popPose();
        }

        // Siluetas de lo que va en cada ranura vacía
        drawGhost(g, HunterForgeMenu.SLOT_BASE, new ItemStack(Items.IRON_SWORD));
        drawGhost(g, HunterForgeMenu.SLOT_CORE, new ItemStack(SystemItems.MONSTER_CORE_E.get()));
        drawGhost(g, HunterForgeMenu.SLOT_CATALYST, new ItemStack(Items.AMETHYST_SHARD));
        drawGhost(g, HunterForgeMenu.SLOT_GEM, new ItemStack(Items.EMERALD));

        // Flecha: el canal grabado se llena de metal fundido cuando todo está listo
        boolean canForge = this.menu.canForge();
        if (canForge) {
            float pulse = 0.5F + 0.5F * (float) Math.sin(time / 180.0D);
            drawMoltenArrow(g, x + 130, y + 46, lerp(EMBER, EMBER_HOT, pulse));
        }

        drawTemperingBar(g, x + BAR_X, y + BAR_Y);

        // Botón pintado en el fondo: aquí solo el texto y su estado
        boolean hovered = isOverButton(mouseX, mouseY);
        int bx = x + BTN_X, by = y + BTN_Y;
        if (!canForge) {
            g.fill(bx + 1, by + 1, bx + BTN_W - 1, by + BTN_H - 1, 0x660E1016);
        } else if (hovered) {
            g.fill(bx + 1, by + 1, bx + BTN_W - 1, by + BTN_H - 1, 0x40F59A3C);
        }
        int textColor = !canForge ? 0xFF56607A : hovered ? EMBER_HOT : 0xFFE8EEFA;
        g.drawCenteredString(this.font, Component.translatable("awakening_system.workshop.forge_button"),
                bx + BTN_W / 2, by + 4, textColor);
    }

    private void drawGhost(GuiGraphics g, int index, ItemStack ghost) {
        Slot slot = this.menu.getSlot(index);
        if (slot.hasItem()) return;
        int sx = this.leftPos + slot.x, sy = this.topPos + slot.y;
        g.renderItem(ghost, sx, sy);
        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        g.fill(sx, sy, sx + 16, sy + 16, 0xC01A1E2A);
        g.pose().popPose();
    }

    private void drawMoltenArrow(GuiGraphics g, int ax, int ay, int color) {
        g.fill(ax, ay - 2, ax + 11, ay + 2, color);
        for (int i = 0; i < 6; i++) {
            g.fill(ax + 11 + i, ay - 6 + i, ax + 12 + i, ay + 6 - i, color);
        }
        g.fill(ax, ay - 2, ax + 11, ay - 1, EMBER_HOT);
    }

    private void drawTemperingBar(GuiGraphics g, int bx, int by) {
        int normalW = (int) (BAR_W * 0.42F);
        int rareW = (int) (BAR_W * 0.30F);
        int epicW = (int) (BAR_W * 0.20F);
        g.fillGradient(bx, by, bx + normalW, by + BAR_H, 0xFF4CB35A, 0xFF1F5E2A);
        g.fillGradient(bx + normalW, by, bx + normalW + rareW, by + BAR_H, 0xFF35C2E0, 0xFF0E5E78);
        g.fillGradient(bx + normalW + rareW, by, bx + normalW + rareW + epicW, by + BAR_H, 0xFFB06BEA, 0xFF4E2380);
        g.fillGradient(bx + normalW + rareW + epicW, by, bx + BAR_W, by + BAR_H, 0xFFFFE08A, 0xFFC08A12);
        g.fill(bx, by, bx + BAR_W, by + 1, 0x50FFFFFF);
        for (int sep : new int[]{normalW, normalW + rareW, normalW + rareW + epicW}) {
            g.fill(bx + sep, by, bx + sep + 1, by + BAR_H, 0xD00E1016);
        }

        // Cursor: una aguja de acero al rojo que recorre la barra
        int cursorX = bx + (int) ((BAR_W - 3) * (this.menu.getTemperingPos() / 100.0F));
        g.fill(cursorX - 1, by - 3, cursorX + 4, by + BAR_H + 3, 0x50FFD98A);
        g.fill(cursorX, by - 2, cursorX + 3, by + BAR_H + 2, 0xFFE8EEFA);
        g.fill(cursorX + 1, by - 1, cursorX + 2, by + BAR_H + 1, 0xFFC8481E);
    }

    private void renderRotatingItem(GuiGraphics g, ItemStack stack, int px, int py, boolean active) {
        PoseStack pose = g.pose();
        pose.pushPose();
        long time = System.currentTimeMillis();
        pose.translate(px, py + (float) Math.sin(time / 500.0D) * 1.5F, 150.0F);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees((time % 6000L) / 6000.0F * 360.0F));
        pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-12.0F));
        pose.scale(30.0F, -30.0F, 30.0F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        var model = itemRenderer.getModel(stack, this.minecraft.level, null, 0);
        // vacío se ve como un reflejo tenue; con objeto, iluminado por las brasas
        if (!active) RenderSystem.setShaderColor(0.8F, 0.85F, 0.95F, 0.7F);
        itemRenderer.render(stack, ItemDisplayContext.FIXED, false, pose, g.bufferSource(), 0xF000F0, OverlayTexture.NO_OVERLAY, model);
        g.bufferSource().endBatch();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
    }

    private boolean isOverButton(double mouseX, double mouseY) {
        int bx = this.leftPos + BTN_X, by = this.topPos + BTN_Y;
        return mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H;
    }

    private static int lerp(int a, int b, float t) {
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
        // Solo el título: el inventario se reconoce solo y así no choca con la línea divisoria
        g.drawString(this.font, this.title, 15, 8, CYAN, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);

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
