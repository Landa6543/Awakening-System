package com.soybe.awakeningsystem.casino.screen;

import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SlotButton extends Button {
    private final ResourceLocation texture;
    private final int texWidth;
    private final int texHeight;
    private final int stateHeight;

    private boolean pressed = false;
    private boolean forcedPressed = false;
    private boolean fakePressed = false;

    public SlotButton(int x, int y, int width, int height, ResourceLocation texture, OnPress onPress, Component title) {
        super(x, y, width, height, title, onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.texWidth = width;
        this.stateHeight = height;
        this.texHeight = height * 3;
    }

    public void setForcedPressed(boolean forced) {
        this.forcedPressed = forced;
    }

    public void setFakePressed(boolean fake) {
        this.fakePressed = fake;
        this.active = !fake;
        if (fake) {
            this.pressed = false;
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.pressed = true;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        this.pressed = false;
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(ModSounds.CASINO_BUTTON.get(), 1.0F));
    }

    private Component subtitle = null;

    public void setSubtitle(Component sub) {
        this.subtitle = sub;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int vOffset = 0;

        if (this.fakePressed) {
            vOffset = stateHeight * 2;
        } else if (this.forcedPressed || this.pressed) {
            vOffset = stateHeight * 2;
        } else if (this.isHoveredOrFocused() && this.active) {
            vOffset = stateHeight;
        }

        graphics.blit(texture, getX(), getY(), 0, vOffset, width, height, texWidth, texHeight);

        if (getMessage() != null && !getMessage().getString().isEmpty()) {
            int textColor = this.active ? 0xFFFFFF : 0x888888;
            if (this.subtitle != null && !this.subtitle.getString().isEmpty()) {
                graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + 4, textColor);
                graphics.drawCenteredString(Minecraft.getInstance().font, this.subtitle, getX() + width / 2, getY() + 14, this.active ? 0xFFFFE082 : 0x777777);
            } else {
                graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, textColor);
            }
        }
    }
}
