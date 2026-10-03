package com.soybe.awakeningsystem.casino.screen;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class DancingPaimon {
    public enum Phase {
        NEUTRAL, // esperando / girando rodillos
        LOSS,    // sin premio
        WIN      // ¡premio conseguido!
    }

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/misc/dancing_paimon.png");
    private final int width = 22;
    private final int frameHeight = 23;
    private final int totalHeight = 138; // 6 frames

    private Phase currentPhase = Phase.NEUTRAL;
    private int frameToggle = 0;
    private int tickCounter = 0;

    private static final int TICKS_PER_SWAP = 6;

    public void tick(Phase phase) {
        this.currentPhase = phase;
        tickCounter++;
        if (tickCounter >= TICKS_PER_SWAP) {
            tickCounter = 0;
            frameToggle ^= 1;
        }
    }

    public void render(GuiGraphics graphics, int x, int y) {
        int pairIndex = switch (currentPhase) {
            case NEUTRAL -> 0;
            case LOSS -> 1;
            case WIN -> 2;
        };

        int frame = pairIndex * 2 + frameToggle;
        int v = frame * frameHeight;

        graphics.blit(TEXTURE, x, y, 0, v, width, frameHeight, width, totalHeight);
    }
}
