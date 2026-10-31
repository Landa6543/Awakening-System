package com.soybe.awakeningsystem;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class SystemWelcomeScreen extends Screen {
    private static final ResourceLocation POPUP_FRAME = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/gui/system_popup_frame.png");

    // Exact 60% scale of the 640x360 source frame
    private static final int W = 384;
    private static final int H = 216;

    public SystemWelcomeScreen() {
        super(Component.translatable("awakening_system.screen.welcome.player"));
    }

    @Override
    protected void init() {
        super.init();
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.2F, 1.0F));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x55060D15);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int left = (width - W) / 2;
        int top = (height - H) / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Under-frame frosted glass backplate
        g.fillGradient(left + 6, top + 6, left + W - 6, top + H - 6, 0x780A1828, 0x88050C16);

        // 1. Manhwa Popup Window Frame (640x360 source blitted into 384x216)
        g.blit(POPUP_FRAME, left, top, W, H, 0.0F, 0.0F, 640, 360, 640, 360);

        // Close button [X] at top-right
        int closeX = left + W - 32;
        int closeY = top + 14;
        int closeW = 16;
        int closeH = 16;
        boolean closeHov = mouseX >= closeX && mouseX < closeX + closeW && mouseY >= closeY && mouseY < closeY + closeH;
        int closeBg = closeHov ? 0xDD7A1515 : 0x2500E5FF;
        int closeBorder = closeHov ? 0xFFFF5555 : 0x6600E5FF;
        g.fill(closeX, closeY, closeX + closeW, closeY + closeH, closeBg);
        g.renderOutline(closeX, closeY, closeW, closeH, closeBorder);
        g.drawCenteredString(font, "✕", closeX + closeW / 2, closeY + 4, closeHov ? 0xFFFFFFFF : 0xFF70E8FF);

        // 2. Header: (!) ALERTA DEL SISTEMA
        int headerY = top + 26;
        Component headerText = Component.literal("(!) ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                .append(Component.translatable("awakening_system.screen.welcome.alarm").withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE));
        g.drawCenteredString(font, headerText, width / 2, headerY, 0xFF00E5FF);

        // Header Divider line
        int divY = headerY + 14;
        g.fill(left + 45, divY, left + W - 45, divY + 1, 0x8000E5FF);
        g.fill(left + 65, divY, left + W - 65, divY + 1, 0xFF4EE2EC);

        // 3. Main Welcome Callout: [BIENVENIDO, JUGADOR.]
        int titleY = top + 54;
        Component welcomePart = Component.translatable("awakening_system.screen.welcome.callout.prefix").withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);
        Component playerPart = Component.translatable("awakening_system.screen.welcome.callout.player").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN);
        Component fullWelcome = welcomePart.copy().append(playerPart);
        g.drawCenteredString(font, fullWelcome, width / 2, titleY, 0xFFFFFFFF);

        // 4. Message Body (Centered with comfortable inner padding)
        int maxTextWidth = W - 80;
        int bodyY = top + 78;
        Component line1 = Component.translatable("awakening_system.screen.welcome.desc.line1");
        Component line2 = Component.translatable("awakening_system.screen.welcome.desc.line2");
        g.drawCenteredString(font, line1, width / 2, bodyY, 0xFFE0F4FF);
        g.drawCenteredString(font, line2, width / 2, bodyY + 14, 0xFFB0D8EB);

        // 5. System Menu Key Hint
        int hintY = top + 116;
        Component hint = Component.translatable("awakening_system.screen.welcome.hint");
        g.drawCenteredString(font, hint, width / 2, hintY, 0xFFFFE082);

        // 6. [ ACEPTAR ] Button
        int btnW = 110;
        int btnH = 22;
        int btnX = (width - btnW) / 2;
        int btnY = top + 154;
        boolean btnHov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;

        int btnBg = btnHov ? 0xDD1B4357 : 0x990C1925;
        int btnBorder = btnHov ? 0xFFFFFFFF : 0xFF00E5FF;
        int btnTextCol = btnHov ? 0xFFFFFFFF : 0xFF70E8FF;

        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        g.renderOutline(btnX, btnY, btnW, btnH, btnBorder);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.welcome.accept").withStyle(ChatFormatting.BOLD),
                btnX + btnW / 2, btnY + 7, btnTextCol);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (width - W) / 2;
            int top = (height - H) / 2;

            // Check [X] close button
            int closeX = left + W - 32;
            int closeY = top + 14;
            if (mouseX >= closeX && mouseX < closeX + 16 && mouseY >= closeY && mouseY < closeY + 16) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                onClose();
                return true;
            }

            // Check [ ACEPTAR ] button
            int btnW = 110;
            int btnH = 22;
            int btnX = (width - btnW) / 2;
            int btnY = top + 154;
            if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || keyCode == 257 || keyCode == 32) { // ESC, ENTER, SPACE
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
