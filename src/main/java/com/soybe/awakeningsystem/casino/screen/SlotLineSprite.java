package com.soybe.awakeningsystem.casino.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class SlotLineSprite {
    private final ResourceLocation texture;
    private final int width;
    private final int heightPerState;
    private final int fullHeight;

    public SlotLineSprite(ResourceLocation texture, int width, int heightPerState) {
        this.texture = texture;
        this.width = width;
        this.heightPerState = heightPerState;
        this.fullHeight = heightPerState * 2;
    }

    public void render(GuiGraphics graphics, int x, int y, boolean enabled) {
        int v = enabled ? heightPerState : 0;
        graphics.blit(texture, x, y, 0, v, width, heightPerState, width, fullHeight);
    }
}
