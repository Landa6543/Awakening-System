package com.soybe.awakeningsystem.client.gui;

import com.soybe.awakeningsystem.OpenStatueScreenPayload;
import com.soybe.awakeningsystem.StatueActionC2SPayload;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class StatueOfTheSevenScreen extends Screen {
    private static final int GUI_WIDTH = 356;
    private static final int GUI_HEIGHT = 240;

    private int anemoLevel = 1;
    private int anemoProgress = 0;
    private int geoLevel = 1;
    private int geoProgress = 0;
    private int electroLevel = 1;
    private int electroProgress = 0;
    private int dendroLevel = 1;
    private int dendroProgress = 0;

    private Component statusMessage = Component.empty();
    private long statusMessageExpireTime = 0L;

    public StatueOfTheSevenScreen(OpenStatueScreenPayload payload) {
        super(Component.translatable("statue_of_the_seven.title"));
        updateProgress(payload);
    }

    public void updateProgress(OpenStatueScreenPayload payload) {
        if (payload != null) {
            this.anemoLevel = payload.anemoLevel();
            this.anemoProgress = payload.anemoProgress();
            this.geoLevel = payload.geoLevel();
            this.geoProgress = payload.geoProgress();
            this.electroLevel = payload.electroLevel();
            this.electroProgress = payload.electroProgress();
            this.dendroLevel = payload.dendroLevel();
            this.dendroProgress = payload.dendroProgress();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private float getGuiScale() {
        float scaleX = (float) this.width / (GUI_WIDTH + 20.0F);
        float scaleY = (float) this.height / (GUI_HEIGHT + 20.0F);
        return Math.min(1.0F, Math.min(scaleX, scaleY));
    }

    private double transformMouseX(double mouseX) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseX;
        double cx = this.width / 2.0;
        return (mouseX - cx) / scale + cx;
    }

    private double transformMouseY(double mouseY) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseY;
        double cy = this.height / 2.0;
        return (mouseY - cy) / scale + cy;
    }

    private int countOculiInInventory(Player player) {
        if (player == null) return 0;
        int count = 0;
        for (ItemStack s : player.getInventory().items) {
            if (s.isEmpty()) continue;
            if (s.is(SystemItems.ANEMOCULUS.get()) || s.is(SystemItems.GEOCULUS.get()) ||
                s.is(SystemItems.ELECTROCULUS.get()) || s.is(SystemItems.DENDROCULUS.get()) ||
                s.is(SystemItems.ANEMOCULUS_BLOCK_ITEM.get()) || s.is(SystemItems.GEOCULUS_BLOCK_ITEM.get()) ||
                s.is(SystemItems.ELECTROCULUS_BLOCK_ITEM.get()) || s.is(SystemItems.DENDROCULUS_BLOCK_ITEM.get())) {
                count += s.getCount();
            }
        }
        return count;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x66060D15);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cxF = this.width / 2.0F;
            float cyF = this.height / 2.0F;
            g.pose().translate(cxF, cyF, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cxF, -cyF, 0.0F);
        }

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        // Fondo y marcos dorados celestiales
        g.fill(left - 2, top - 2, left + GUI_WIDTH + 2, top + GUI_HEIGHT + 2, 0xFFDFB76C);
        g.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xF508111A);

        // Barra de Título
        g.fill(left, top, left + GUI_WIDTH, top + 26, 0xFF0D1D2B);
        g.fill(left, top + 25, left + GUI_WIDTH, top + 26, 0xFFDFB76C);

        int accentCol = 0xFF4EE2EC;
        g.fill(left, top, left + 8, top + 2, accentCol);
        g.fill(left, top, left + 2, top + 8, accentCol);
        g.fill(left + GUI_WIDTH - 8, top, left + GUI_WIDTH, top + 2, accentCol);
        g.fill(left + GUI_WIDTH - 2, top, left + GUI_WIDTH, top + 8, accentCol);

        g.drawCenteredString(font, Component.translatable("statue_of_the_seven.title"), left + GUI_WIDTH / 2, top + 8, 0xFFDFB76C);

        // Botón Cerrar (X)
        boolean closeHover = mX >= left + GUI_WIDTH - 22 && mX < left + GUI_WIDTH - 6 && mY >= top + 6 && mY < top + 20;
        g.fill(left + GUI_WIDTH - 22, top + 6, left + GUI_WIDTH - 6, top + 20, closeHover ? 0xDDFF4444 : 0x66222222);
        g.drawCenteredString(font, "✕", left + GUI_WIDTH - 14, top + 8, closeHover ? 0xFFFFFFFF : 0xFFAAAAAA);

        // Subtítulo
        g.drawCenteredString(font, Component.translatable("statue_of_the_seven.subtitle"), left + GUI_WIDTH / 2, top + 31, 0xFF88A0B0);

        Player player = Minecraft.getInstance().player;
        int totalHeldOculi = countOculiInInventory(player);

        // PANEL 1: Progreso de Óculos por Región
        int panelTop = top + 44;
        g.fill(left + 12, panelTop, left + GUI_WIDTH - 12, panelTop + 62, 0x990D1822);
        g.fill(left + 12, panelTop, left + GUI_WIDTH - 12, panelTop + 1, 0xFF2A3D4D);
        g.fill(left + 12, panelTop + 61, left + GUI_WIDTH - 12, panelTop + 62, 0xFF2A3D4D);

        int reqA = SystemData.getOculiRequiredForLevel(anemoLevel + 1);
        int reqG = SystemData.getOculiRequiredForLevel(geoLevel + 1);
        int reqE = SystemData.getOculiRequiredForLevel(electroLevel + 1);
        int reqD = SystemData.getOculiRequiredForLevel(dendroLevel + 1);

        String progA = (anemoLevel >= 10 ? "MAX" : anemoProgress + "/" + reqA);
        String progG = (geoLevel >= 10 ? "MAX" : geoProgress + "/" + reqG);
        String progE = (electroLevel >= 10 ? "MAX" : electroProgress + "/" + reqE);
        String progD = (dendroLevel >= 10 ? "MAX" : dendroProgress + "/" + reqD);

        Component textAnemo = Component.translatable("statue_of_the_seven.anemo", anemoLevel, progA);
        Component textGeo = Component.translatable("statue_of_the_seven.geo", geoLevel, progG);
        Component textElectro = Component.translatable("statue_of_the_seven.electro", electroLevel, progE);
        Component textDendro = Component.translatable("statue_of_the_seven.dendro", dendroLevel, progD);

        g.drawString(font, textAnemo, left + 20, panelTop + 6, 0xFFFFFFFF);
        g.drawString(font, textGeo, left + 185, panelTop + 6, 0xFFFFFFFF);
        g.drawString(font, textElectro, left + 20, panelTop + 20, 0xFFFFFFFF);
        g.drawString(font, textDendro, left + 185, panelTop + 20, 0xFFFFFFFF);

        Component oculiHeld = Component.translatable("statue_of_the_seven.oculi_held", totalHeldOculi);
        g.drawString(font, oculiHeld, left + 20, panelTop + 36, 0xFFDFB76C);

        String rewText = Component.translatable("statue_of_the_seven.rewards").getString();
        rewText = font.plainSubstrByWidth(rewText, GUI_WIDTH - 44);
        g.drawString(font, "§8" + rewText, left + 20, panelTop + 48, 0xFF778899);

        // BOTÓN 1: OFRENDAR ÓCULOS
        int btn1Y = top + 112;
        boolean b1Hover = mX >= left + 16 && mX < left + GUI_WIDTH - 16 && mY >= btn1Y && mY < btn1Y + 24;
        int b1Bg = totalHeldOculi > 0 ? (b1Hover ? 0xDD254B36 : 0xAA183526) : 0x55111B15;
        int b1Border = totalHeldOculi > 0 ? (b1Hover ? 0xFF55FFAA : 0xFF339966) : 0xFF334433;
        g.fill(left + 16, btn1Y, left + GUI_WIDTH - 16, btn1Y + 24, b1Bg);
        drawBoxBorder(g, left + 16, btn1Y, GUI_WIDTH - 32, 24, b1Border);
        Component b1Text = totalHeldOculi > 0 ? Component.translatable("statue_of_the_seven.worship") : Component.translatable("statue_of_the_seven.worship.empty");
        g.drawCenteredString(font, b1Text, left + GUI_WIDTH / 2, btn1Y + 8, totalHeldOculi > 0 ? 0xFFFFFFFF : 0xFF888888);

        // BOTÓN 2: BENDICIÓN DE LA ESTATUA (CURAR Y RESTAURAR)
        int btn2Y = top + 142;
        boolean b2Hover = mX >= left + 16 && mX < left + GUI_WIDTH - 16 && mY >= btn2Y && mY < btn2Y + 24;
        int b2Bg = b2Hover ? 0xDD1B3347 : 0xAA122332;
        int b2Border = b2Hover ? 0xFF4EE2EC : 0xFF2A5570;
        g.fill(left + 16, btn2Y, left + GUI_WIDTH - 16, btn2Y + 24, b2Bg);
        drawBoxBorder(g, left + 16, btn2Y, GUI_WIDTH - 32, 24, b2Border);
        g.drawCenteredString(font, Component.translatable("statue_of_the_seven.blessing"), left + GUI_WIDTH / 2, btn2Y + 8, 0xFFFFFFFF);

        // BOTÓN 3: RESONANCIA ELEMENTAL (SELECTOR DE VISIONES)
        int btn3Y = top + 172;
        boolean b3Hover = mX >= left + 16 && mX < left + GUI_WIDTH - 16 && mY >= btn3Y && mY < btn3Y + 24;
        int b3Bg = b3Hover ? 0xDD3B2B1B : 0xAA2A1E13;
        int b3Border = b3Hover ? 0xFFFFD700 : 0xFF8B6B2B;
        g.fill(left + 16, btn3Y, left + GUI_WIDTH - 16, btn3Y + 24, b3Bg);
        drawBoxBorder(g, left + 16, btn3Y, GUI_WIDTH - 32, 24, b3Border);
        g.drawCenteredString(font, Component.translatable("statue_of_the_seven.resonate"), left + GUI_WIDTH / 2, btn3Y + 8, 0xFFFFFFFF);

        // MENSAJE DE ESTADO INTERNO DEL MENÚ
        if (!statusMessage.getString().isEmpty() && System.currentTimeMillis() < statusMessageExpireTime) {
            g.drawCenteredString(font, statusMessage, left + GUI_WIDTH / 2, top + 204, 0xFF55FFAA);
        } else {
            g.drawCenteredString(font, Component.translatable("statue_of_the_seven.footer"), left + GUI_WIDTH / 2, top + 204, 0xFF556677);
        }

        // BOTÓN SALIR
        int btnExitY = top + 218;
        boolean exitHover = mX >= left + GUI_WIDTH / 2 - 40 && mX < left + GUI_WIDTH / 2 + 40 && mY >= btnExitY && mY < btnExitY + 16;
        g.fill(left + GUI_WIDTH / 2 - 40, btnExitY, left + GUI_WIDTH / 2 + 40, btnExitY + 16, exitHover ? 0xDD442222 : 0x88221111);
        drawBoxBorder(g, left + GUI_WIDTH / 2 - 40, btnExitY, 80, 16, exitHover ? 0xFFFF5555 : 0xFF773333);
        g.drawCenteredString(font, Component.translatable("statue_of_the_seven.close"), left + GUI_WIDTH / 2, btnExitY + 4, exitHover ? 0xFFFFFFFF : 0xFFAAAAAA);

        g.pose().popPose();
    }

    private void drawBoxBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        int left = (width - GUI_WIDTH) / 2;
        int top = (height - GUI_HEIGHT) / 2;

        // Botón Cerrar (X)
        if (mX >= left + GUI_WIDTH - 22 && mX < left + GUI_WIDTH - 6 && mY >= top + 6 && mY < top + 20) {
            playClickSound();
            onClose();
            return true;
        }

        // Botón Cerrar Inferior
        int btnExitY = top + 218;
        if (mX >= left + GUI_WIDTH / 2 - 40 && mX < left + GUI_WIDTH / 2 + 40 && mY >= btnExitY && mY < btnExitY + 16) {
            playClickSound();
            onClose();
            return true;
        }

        // BOTÓN 1: OFRENDAR ÓCULOS
        int btn1Y = top + 112;
        if (mX >= left + 16 && mX < left + GUI_WIDTH - 16 && mY >= btn1Y && mY < btn1Y + 24) {
            Player player = Minecraft.getInstance().player;
            int total = countOculiInInventory(player);
            if (total > 0) {
                playClickSound();
                PacketDistributor.sendToServer(new StatueActionC2SPayload(StatueActionC2SPayload.ACTION_OFFER_OCULI, ""));
                setStatus(Component.translatable("statue_of_the_seven.status.success"));
            } else {
                playDenySound();
                setStatus(Component.translatable("statue_of_the_seven.status.no_oculi"));
            }
            return true;
        }

        // BOTÓN 2: BENDICIÓN DE LA ESTATUA
        int btn2Y = top + 142;
        if (mX >= left + 16 && mX < left + GUI_WIDTH - 16 && mY >= btn2Y && mY < btn2Y + 24) {
            playClickSound();
            PacketDistributor.sendToServer(new StatueActionC2SPayload(StatueActionC2SPayload.ACTION_BLESSING, ""));
            setStatus(Component.translatable("statue_of_the_seven.status.blessed"));
            return true;
        }

        // BOTÓN 3: RESONANCIA ELEMENTAL
        int btn3Y = top + 172;
        if (mX >= left + 16 && mX < left + GUI_WIDTH - 16 && mY >= btn3Y && mY < btn3Y + 24) {
            playClickSound();
            VisionSelectionScreen.open(this);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void setStatus(Component msg) {
        this.statusMessage = msg;
        this.statusMessageExpireTime = System.currentTimeMillis() + 3500L;
    }

    private void playClickSound() {
        Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.0F);
    }

    private void playDenySound() {
        Minecraft.getInstance().player.playSound(SoundEvents.VILLAGER_NO, 0.8F, 1.0F);
    }
}
