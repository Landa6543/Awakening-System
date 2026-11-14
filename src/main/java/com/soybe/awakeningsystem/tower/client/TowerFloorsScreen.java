package com.soybe.awakeningsystem.tower.client;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.TravelDimensionPayload;
import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import com.soybe.awakeningsystem.tower.GreatTowerWorldManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The Great Tower's floors in the classic System look: every floor the player has reached can be travelled
 * to again from here; the rest are shown locked. Opened from the tower tab on the side of the System window.
 */
public final class TowerFloorsScreen extends Screen {
    private static final int W = 330, H = 200, COLS = 5, CW = 58, CH = 32;
    private final Screen parent;

    public TowerFloorsScreen(Screen parent) {
        super(Component.translatable("awakening_system.sao.tower_floors"));
        this.parent = parent;
    }

    private int left() { return (this.width - W) / 2; }
    private int top() { return (this.height - H) / 2; }
    private int cellX(int i) { return left() + 12 + (i % COLS) * (CW + 3); }
    private int cellY(int i) { return top() + 40 + (i / COLS) * (CH + 3); }

    private SystemData data() {
        return minecraft != null && minecraft.player != null ? minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
    }

    private int reached() {
        SystemData d = data();
        return d == null ? 0 : Math.min(20, d.savedTowerFloor());
    }

    private int here() {
        if (minecraft == null || minecraft.level == null) return 0;
        for (int f = 1; f <= 20; f++)
            if (minecraft.level.dimension().equals(GreatTowerWorldManager.getDimensionForFloor(f))) return f;
        return 0;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0x66000000, 0x88000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int x = left(), y = top();
        ModernGuiHelper.drawModernWindow(g, x, y, W, H, ModernGuiHelper.getTheme(data()));
        g.drawString(font, ModernGuiHelper.manhwa(Component.translatable("awakening_system.sao.tower_row").withStyle(ChatFormatting.BOLD)),
                x + 14, y + 12, 0xFFFFC94A, false);
        g.drawString(font, Component.translatable("awakening_system.sao.tower_floors"), x + 14, y + 24, 0xFF8FA6BC, false);
        int closeX = x + W - 24, closeY = y + 10;
        boolean closeHov = mouseX >= closeX && mouseX < closeX + 14 && mouseY >= closeY && mouseY < closeY + 14;
        g.fill(closeX, closeY, closeX + 14, closeY + 14, closeHov ? 0xDDFF3344 : 0x88301018);
        g.drawCenteredString(font, "✕", closeX + 7, closeY + 3, 0xFFFFFFFF);

        int reached = reached(), here = here();
        for (int i = 0; i < 20; i++) {
            int f = i + 1, cx = cellX(i), cy = cellY(i);
            boolean open = f <= reached, cur = f == here;
            boolean hov = open && !cur && mouseX >= cx && mouseX < cx + CW && mouseY >= cy && mouseY < cy + CH;
            g.fill(cx, cy, cx + CW, cy + CH, cur ? 0xDD0D3554 : hov ? 0xCC0A2A40 : open ? 0x88061424 : 0x55101418);
            g.renderOutline(cx, cy, CW, CH, cur ? 0xFFFFC94A : hov ? 0xFF70E8FF : open ? 0x6600E5FF : 0x33445566);
            g.pose().pushPose();
            g.pose().translate(cx + 5, cy + 4, 0);
            g.pose().scale(1.5F, 1.5F, 1.0F);
            g.drawString(font, String.valueOf(f), 0, 0, cur ? 0xFFFFC94A : open ? 0xFFB4FAFF : 0xFF55606A, false);
            g.pose().popPose();
            String sub = open ? GreatTowerWorldManager.getFloorTitle(f) : Component.translatable("awakening_system.world_archive.badge_locked").getString();
            g.pose().pushPose();
            g.pose().translate(cx + 4, cy + 22, 0);
            g.pose().scale(0.6F, 0.6F, 1.0F);
            g.drawString(font, font.plainSubstrByWidth(sub, (int) ((CW - 6) / 0.6F)), 0, 0, open ? 0xFF8FA6BC : 0xFF667788, false);
            g.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int closeX = left() + W - 24, closeY = top() + 10;
        if (mouseX >= closeX && mouseX < closeX + 14 && mouseY >= closeY && mouseY < closeY + 14) {
            onClose();
            return true;
        }
        int reached = reached(), here = here();
        for (int i = 0; i < 20; i++) {
            int f = i + 1, cx = cellX(i), cy = cellY(i);
            if (mouseX < cx || mouseX >= cx + CW || mouseY < cy || mouseY >= cy + CH) continue;
            if (f > reached || f == here) {
                AwakeningSystemClient.playUiSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F, 0.7F);
                return true;
            }
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.9F, 1.0F);
            if (minecraft != null) minecraft.setScreen(null);
            PacketDistributor.sendToServer(new TravelDimensionPayload("tower_floor:" + f));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 0.9F);
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
