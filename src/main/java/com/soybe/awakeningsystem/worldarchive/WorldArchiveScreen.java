package com.soybe.awakeningsystem.worldarchive;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.TravelDimensionPayload;
import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Archivo de Mundos: se abre desde la pestaña que aparece en el costado de la ventana del Sistema
 * cuando ya no entran todas las pestañas de mundos. Lista los mundos a la izquierda y, a la
 * derecha, la ficha del elegido con su descripción y lo que se encuentra en él (el texto largo se
 * desplaza con la rueda del ratón, nada se encima).
 */
public final class WorldArchiveScreen extends Screen {
    private static final int W = 400;
    private static final int H = 250;
    private static final int LIST_W = 124;
    private static final int ITEM_H = 28;
    private static final int BTN_W = 116;
    private static final int BTN_H = 18;

    private final Screen parentScreen;
    private int selectedIndex = 0;
    private int listScroll = 0;
    private int bodyScroll = 0;
    private int bodyContentH = 0;

    public WorldArchiveScreen(Screen parentScreen) {
        super(Component.translatable("awakening_system.world_archive.screen_title"));
        this.parentScreen = parentScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // sin el desenfoque de 1.21
    }

    @Override
    public void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void renderTransparentBackground(GuiGraphics g) {
    }

    // ---------------------------------------------------------------- layout
    private int left() { return (this.width - W) / 2; }
    private int top() { return (this.height - H) / 2; }
    private int listX() { return left() + 12; }
    private int bodyTop() { return top() + 42; }
    private int bodyBottom() { return top() + H - 12; }
    private int detailX() { return listX() + LIST_W + 8; }
    private int detailW() { return left() + W - 12 - detailX(); }
    private int visibleItems() { return (bodyBottom() - bodyTop() - 4) / (ITEM_H + 2); }
    private int footerY() { return bodyBottom() - BTN_H - 6; }
    private int scrollTop() { return bodyTop() + 40; }
    private int scrollBottom() { return footerY() - 6; }

    private Player player() {
        return minecraft != null ? minecraft.player : null;
    }

    private String currentWorldId() {
        if (minecraft == null || minecraft.level == null) return "";
        var key = minecraft.level.dimension();
        if (key.equals(net.minecraft.world.level.Level.OVERWORLD)) return "overworld";
        if (key.equals(com.soybe.awakeningsystem.IsekaiWorldManager.ISEKAI_WORLD)) return "isekai";
        if (key.equals(com.soybe.awakeningsystem.MurimWorldManager.MURIM_WORLD)) return "murim";
        if (key.equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD)) return "teyvat";
        return "";
    }

    private boolean inSealedRealm() {
        return minecraft != null && minecraft.level != null &&
                (com.soybe.awakeningsystem.penalty.PenaltyZoneManager.isPenaltyDimension(minecraft.level) ||
                 com.soybe.awakeningsystem.limbo.LimboWorldManager.isLimboDimension(minecraft.level));
    }

    private WorldArchiveEntry selected() {
        List<WorldArchiveEntry> entries = WorldArchiveRegistry.getEntries();
        if (entries.isEmpty()) return null;
        selectedIndex = Mth.clamp(selectedIndex, 0, entries.size() - 1);
        return entries.get(selectedIndex);
    }

    private String fit(String s, int w) {
        return font.width(s) <= w ? s : font.plainSubstrByWidth(s, w - font.width("…")) + "…";
    }

    // ---------------------------------------------------------------- render
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0x66000000, 0x88000000);
        int left = left(), top = top();
        SystemData data = minecraft != null && minecraft.player != null ? minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
        ModernGuiHelper.drawModernWindow(g, left, top, W, H, ModernGuiHelper.getTheme(data));

        // cabecera
        Component title = Component.translatable("awakening_system.world_archive.header_title").withStyle(ChatFormatting.BOLD);
        g.drawString(font, ModernGuiHelper.manhwa(title), left + 16, top + 12, 0xFFFFC94A, false);
        g.drawString(font, fit(Component.translatable("awakening_system.world_archive.header_subtitle").getString(), W - 60),
                left + 16, top + 25, 0xFF8FA6BC, false);

        int closeX = left + W - 26, closeY = top + 10;
        boolean closeHov = mouseX >= closeX && mouseX < closeX + 14 && mouseY >= closeY && mouseY < closeY + 14;
        g.fill(closeX, closeY, closeX + 14, closeY + 14, closeHov ? 0xDDFF3344 : 0x88301018);
        g.drawCenteredString(font, "✕", closeX + 7, closeY + 3, 0xFFFFFFFF);

        renderList(g, mouseX, mouseY);
        WorldArchiveEntry entry = selected();
        if (entry != null) renderDetail(g, entry, mouseX, mouseY);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderList(GuiGraphics g, int mouseX, int mouseY) {
        int x = listX(), y = bodyTop(), h = bodyBottom() - bodyTop();
        g.fill(x, y, x + LIST_W, y + h, 0x55081422);
        g.renderOutline(x, y, LIST_W, h, 0x4400E5FF);

        List<WorldArchiveEntry> entries = WorldArchiveRegistry.getEntries();
        String here = currentWorldId();
        for (int i = 0; i < visibleItems(); i++) {
            int idx = listScroll + i;
            if (idx >= entries.size()) break;
            WorldArchiveEntry e = entries.get(idx);
            int iy = y + 3 + i * (ITEM_H + 2);
            boolean sel = idx == selectedIndex;
            boolean hov = mouseX >= x + 3 && mouseX < x + LIST_W - 3 && mouseY >= iy && mouseY < iy + ITEM_H;
            boolean open = e.isUnlocked(player());

            g.fill(x + 3, iy, x + LIST_W - 3, iy + ITEM_H, sel ? 0xDD0D3554 : (hov ? 0x990A2238 : 0x66051220));
            g.renderOutline(x + 3, iy, LIST_W - 6, ITEM_H, sel ? 0xFF00E5FF : (hov ? 0xAA70E8FF : 0x3300E5FF));
            if (sel) g.fill(x + 3, iy + 4, x + 5, iy + ITEM_H - 4, 0xFF00FFFF);

            RenderSystem.enableBlend();
            if (!open) RenderSystem.setShaderColor(0.45F, 0.45F, 0.5F, 1.0F);
            g.blit(e.iconTexture(), x + 8, iy + 5, 18, 18, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int tx = x + 30, tw = LIST_W - 36;
            g.drawString(font, fit(e.name().getString(), tw), tx, iy + 5, open ? (sel ? 0xFFFFFFFF : 0xFFB4FAFF) : 0xFF7F8D99, false);
            Component status = e.id().equals(here)
                    ? Component.translatable("awakening_system.world_archive.status_here")
                    : Component.translatable(open ? "awakening_system.world_archive.status_available" : "awakening_system.world_archive.badge_locked");
            int statusCol = e.id().equals(here) ? 0xFFFFC94A : (open ? 0xFF44D68C : 0xFFFF6B6B);
            g.drawString(font, fit(status.getString(), tw), tx, iy + 16, statusCol, false);
        }
    }

    private void renderDetail(GuiGraphics g, WorldArchiveEntry e, int mouseX, int mouseY) {
        int x = detailX(), y = bodyTop(), w = detailW(), h = bodyBottom() - bodyTop();
        boolean open = e.isUnlocked(player());
        g.fill(x, y, x + w, y + h, 0x55081422);
        g.renderOutline(x, y, w, h, 0x4400E5FF);

        // ficha: ícono grande, nombre y subtítulo
        RenderSystem.enableBlend();
        if (!open) RenderSystem.setShaderColor(0.45F, 0.45F, 0.5F, 1.0F);
        g.blit(e.iconTexture(), x + 7, y + 6, 28, 28, 0.0F, 0.0F, 32, 32, 32, 32);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int tx = x + 42, tw = w - 48;
        g.drawString(font, fit(e.name().getString(), tw), tx, y + 9, open ? 0xFF00E5FF : 0xFFA0B4C8, false);
        g.drawString(font, fit(e.subtitle().getString(), tw), tx, y + 21, 0xFF80A0B8, false);
        g.fill(x + 6, y + 37, x + w - 6, y + 38, 0x4400E5FF);

        // cuerpo desplazable: descripción y qué hay en el mundo
        int sx = x + 8, sw = w - 16, st = scrollTop(), sb = scrollBottom();
        int maxScroll = Math.max(0, bodyContentH - (sb - st));
        bodyScroll = Mth.clamp(bodyScroll, 0, maxScroll);
        g.enableScissor(x + 1, st, x + w - 1, sb);
        int end = drawBody(g, e, sx, st - bodyScroll, sw);
        g.disableScissor();
        bodyContentH = end - (st - bodyScroll);
        if (maxScroll > 0) {
            int trackH = sb - st;
            int thumbH = Math.max(12, trackH * trackH / bodyContentH);
            int thumbY = st + (trackH - thumbH) * bodyScroll / maxScroll;
            g.fill(x + w - 4, st, x + w - 2, sb, 0x33000000);
            g.fill(x + w - 4, thumbY, x + w - 2, thumbY + thumbH, 0xAA00E5FF);
        }

        // pie: estado o requisito, y botón de viaje
        int fy = footerY();
        g.fill(x + 6, fy - 4, x + w - 6, fy - 3, 0x3300E5FF);
        int bx = x + w - BTN_W - 6;
        boolean here = e.id().equals(currentWorldId());
        boolean sealed = inSealedRealm();
        boolean canGo = open && !here && !sealed;
        String note;
        int noteCol;
        if (!open) { note = e.unlockConditionText().getString(); noteCol = 0xFFFF8585; }
        else if (sealed) { note = Component.translatable("awakening_system.screen.dim.sealed_title").getString(); noteCol = 0xFFFF8585; }
        else if (here) { note = Component.translatable("awakening_system.world_archive.status_here").getString(); noteCol = 0xFFFFC94A; }
        else { note = Component.translatable("awakening_system.world_archive.status_available").getString(); noteCol = 0xFF7BED9F; }
        List<FormattedCharSequence> noteLines = font.split(Component.literal(note), bx - x - 14);
        for (int i = 0; i < Math.min(2, noteLines.size()); i++) {
            g.drawString(font, noteLines.get(i), x + 8, fy + (noteLines.size() > 1 ? i * 9 : 5), noteCol, false);
        }

        boolean hov = canGo && mouseX >= bx && mouseX < bx + BTN_W && mouseY >= fy && mouseY < fy + BTN_H;
        g.fill(bx, fy, bx + BTN_W, fy + BTN_H, canGo ? (hov ? 0xF00F5A3B : 0xCC093E26) : 0x66181E26);
        g.renderOutline(bx, fy, BTN_W, BTN_H, canGo ? (hov ? 0xFF00FF88 : 0x8800E5FF) : 0x33445566);
        Component label = Component.translatable(open ? "awakening_system.world_archive.btn_travel" : "awakening_system.world_archive.btn_locked");
        g.drawCenteredString(font, fit(label.getString(), BTN_W - 6), bx + BTN_W / 2, fy + 5, canGo ? (hov ? 0xFFFFFFFF : 0xFF7BED9F) : 0xFF667788);
    }

    /** Dibuja descripción y secciones a partir de y; devuelve dónde termina. */
    private int drawBody(GuiGraphics g, WorldArchiveEntry e, int x, int y, int w) {
        Component desc = e.description();
        for (FormattedCharSequence line : font.split(desc, w - 4)) {
            g.drawString(font, line, x, y, 0xFFCDE2F0, false);
            y += 10;
        }
        y += 4;
        y = drawChips(g, "awakening_system.world_archive.section_structures", e.keyStructures(), x, y, w, 0xFFFFC94A, 0x770E283E, 0x5500E5FF, 0xFF8FE4FF);
        y = drawChips(g, "awakening_system.world_archive.section_bosses", e.keyBosses(), x, y, w, 0xFFFF7A7A, 0x77331118, 0x66FF3355, 0xFFFFA8B4);
        y = drawChips(g, "awakening_system.world_archive.section_materials", e.keyMaterials(), x, y, w, 0xFF55E8FF, 0x770D2B24, 0x5500FF88, 0xFFA8FFC0);
        return y;
    }

    private int drawChips(GuiGraphics g, String headerKey, List<Component> items, int x, int y, int w,
                          int headerCol, int chipBg, int chipBorder, int chipText) {
        g.drawString(font, Component.translatable(headerKey).withStyle(ChatFormatting.BOLD), x, y, headerCol, false);
        y += 12;
        int cx = x;
        for (Component item : items) {
            String s = fit(item.getString(), w - 10);
            int cw = font.width(s) + 8;
            if (cx > x && cx + cw > x + w - 4) { cx = x; y += 14; }
            g.fill(cx, y, cx + cw, y + 12, chipBg);
            g.renderOutline(cx, y, cw, 12, chipBorder);
            g.drawString(font, s, cx + 4, y + 2, chipText, false);
            cx += cw + 4;
        }
        return y + 18;
    }

    // ---------------------------------------------------------------- input
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int closeX = left() + W - 26, closeY = top() + 10;
        if (mouseX >= closeX && mouseX < closeX + 14 && mouseY >= closeY && mouseY < closeY + 14) {
            back();
            return true;
        }

        List<WorldArchiveEntry> entries = WorldArchiveRegistry.getEntries();
        int x = listX(), y = bodyTop();
        for (int i = 0; i < visibleItems(); i++) {
            int idx = listScroll + i;
            if (idx >= entries.size()) break;
            int iy = y + 3 + i * (ITEM_H + 2);
            if (mouseX >= x + 3 && mouseX < x + LIST_W - 3 && mouseY >= iy && mouseY < iy + ITEM_H) {
                if (selectedIndex != idx) {
                    selectedIndex = idx;
                    bodyScroll = 0;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.1F);
                }
                return true;
            }
        }

        WorldArchiveEntry e = selected();
        int bx = detailX() + detailW() - BTN_W - 6, fy = footerY();
        if (e != null && mouseX >= bx && mouseX < bx + BTN_W && mouseY >= fy && mouseY < fy + BTN_H) {
            boolean open = e.isUnlocked(player());
            if (!open || inSealedRealm() || e.id().equals(currentWorldId())) {
                AwakeningSystemClient.playUiSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F, 0.7F);
                return true;
            }
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.9F, 1.0F);
            this.onClose();
            PacketDistributor.sendToServer(new TravelDimensionPayload(e.id()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < detailX()) {
            int max = Math.max(0, WorldArchiveRegistry.getEntries().size() - visibleItems());
            listScroll = Mth.clamp(listScroll - (int) Math.signum(scrollY), 0, max);
        } else {
            bodyScroll -= (int) Math.signum(scrollY) * 10;
        }
        return true;
    }

    private void back() {
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 0.9F);
        if (minecraft != null) minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            back();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
