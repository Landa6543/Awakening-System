package com.soybe.awakeningsystem.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

/**
 * Renders the active combat stance badge on the HUD for the Awakening Combat Engine (ACE).
 * Displays whether the hunter is in COMBAT stance (⚔) with combo counters or MINING stance (⛏).
 */
public class AceCombatHudOverlay {
    private static float displayAlpha = 1.0F;
    private static long lastModeChangeTime = 0;
    private static AceCombatMode lastRenderedMode = AceCombatMode.MINING;

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || player.isSpectator()) {
            return;
        }

        AceCombatState state = AceCombatState.getClientInstance();
        AceCombatMode mode = state.getMode();

        long now = System.currentTimeMillis();
        if (mode != lastRenderedMode) {
            lastRenderedMode = mode;
            lastModeChangeTime = now;
            displayAlpha = 1.0F;
        }

        // In MINING mode, gradually fade to subtle transparency after 3 seconds
        if (mode == AceCombatMode.MINING) {
            long elapsed = now - lastModeChangeTime;
            if (elapsed > 3000) {
                displayAlpha = Math.max(0.35F, displayAlpha - 0.02F);
            } else {
                displayAlpha = 1.0F;
            }
        } else {
            // In COMBAT mode, stay bright and clear
            displayAlpha = 1.0F;
        }

        int alphaInt = Math.clamp((int) (displayAlpha * 255.0F), 20, 255);
        int bgAlpha = Math.clamp((int) (displayAlpha * 180.0F), 10, 180);

        // Position: docked cleanly to the right of the vanilla hotbar
        int hotbarCenter = screenWidth / 2;
        int hotbarY = screenHeight - 22;
        int badgeX = hotbarCenter + 98; // Just to the right of the 9th hotbar slot
        int badgeY = hotbarY;
        int badgeWidth = 48;
        int badgeHeight = 18;

        // Render sleek glass backing
        int bgColor = (bgAlpha << 24) | (mode == AceCombatMode.COMBAT ? 0x1A0F1F : 0x0A1820);
        int borderColor = (alphaInt << 24) | (mode == AceCombatMode.COMBAT ? 0xF59E0B : 0x38BDF8);

        // Fill background
        g.fill(badgeX, badgeY, badgeX + badgeWidth, badgeY + badgeHeight, bgColor);
        // Border
        g.renderOutline(badgeX, badgeY, badgeWidth, badgeHeight, borderColor);

        // Icon & Stance Label
        String icon = mode.getIcon();
        String label = mode == AceCombatMode.COMBAT ? "COMBATE" : "MINERO";
        int textColor = (alphaInt << 24) | (mode == AceCombatMode.COMBAT ? 0xFFE082 : 0xB3E5FC);

        // Draw icon
        g.drawString(mc.font, icon, badgeX + 4, badgeY + 5, textColor, false);

        // Draw label
        g.pose().pushPose();
        g.pose().translate(badgeX + 16, badgeY + 3, 0);
        g.pose().scale(0.85F, 0.85F, 1.0F);
        g.drawString(mc.font, label, 0, 0, textColor, false);

        // If in COMBAT mode, show combo progress pips (● ○ ○)
        if (mode == AceCombatMode.COMBAT) {
            int combo = state.getComboIndex();
            String pips = switch (combo) {
                case 1 -> "● ○ ○";
                case 2 -> "● ● ○";
                case 3 -> "★ FINISH";
                default -> "○ ○ ○";
            };
            int pipColor = (alphaInt << 24) | (combo == 3 ? 0xFF5252 : 0xFFD54F);
            g.drawString(mc.font, pips, 0, 9, pipColor, false);
        } else {
            // In MINING mode, show a small sub-hint
            g.drawString(mc.font, "Vainilla", 0, 9, (alphaInt << 24) | 0x81D4FA, false);
        }
        g.pose().popPose();
    }
}
