package com.soybe.awakeningsystem.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Modern Manhwa / Solo Leveling UI Engine.
 * Provides authentic translucent cobalt/navy glass styling, glowing neon borders,
 * cybernetic chamfered corner filigrees, sleek header plates, and glowing gauges.
 */
public final class ModernGuiHelper {

    public static final ResourceLocation SYSTEM_WINDOW_FRAME = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/gui/system_window_frame.png");
    public static final ResourceLocation SYSTEM_WINDOW_FRAME_ISEKAI = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/gui/system_window_frame_isekai.png");
    public static final ResourceLocation SYSTEM_WINDOW_FRAME_MURIM = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/gui/system_window_frame_murim.png");
    public static final ResourceLocation SYSTEM_WINDOW_FRAME_TEYVAT = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/gui/system_window_frame_teyvat.png");
    public static final ResourceLocation SYSTEM_POPUP_FRAME = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/gui/system_popup_frame.png");
    public static final ResourceLocation SYSTEM_FONT = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "system_font");

    public static net.minecraft.network.chat.Style systemFontStyle() {
        return net.minecraft.network.chat.Style.EMPTY;
    }

    public static Component manhwa(String text) {
        return Component.literal(text);
    }

    public static Component manhwa(Component component) {
        if (component == null) return Component.empty();
        return component;
    }

    public enum GuiTheme {
        SOLO_LEVELING(
                0x1500E5FF, 0x3000E5FF, 0x6000E5FF, // aura layers
                0xFF00E5FF, 0xFFB4FAFF, 0xFF70E8FF, // primary, core, accent
                0x780A1828, 0x88050C16,             // pure authentic frosted manhwa cobalt glass (matching SystemScreen)
                0x2800E5FF, 0x2000E5FF, 0x1200E5FF  // header plate, section plate, section bg
        ),
        ISEKAI(
                0x159D4EDD, 0x309D4EDD, 0x609D4EDD,
                0xFF9D4EDD, 0xFFE0AAFF, 0xFFFFD700,
                0x781A0A2C, 0x880B0418,             // matching SystemScreen sys20
                0x289D4EDD, 0x209D4EDD, 0x129D4EDD
        ),
        MURIM(
                0x1500FF88, 0x3000FF88, 0x6000FF88,
                0xFF00FF88, 0xFFD4AF37, 0xFFFFD700,
                0x780A1D16, 0x88040E0B,             // matching SystemScreen sys30
                0x2800FF88, 0x2000FF88, 0x1200FF88
        ),
        TEYVAT(
                0x154EE2EC, 0x304EE2EC, 0x604EE2EC,
                0xFFDFB76C, 0xFF4EE2EC, 0xFFFFE082,
                0x780E1928, 0x88060D16,             // matching SystemScreen sys40
                0x284EE2EC, 0x204EE2EC, 0x124EE2EC
        );

        public final int aura1, aura2, aura3;
        public final int primary, core, accent;
        public final int glassTop, glassBottom;
        public final int headerPlate, sectionPlate, sectionBg;

        GuiTheme(int a1, int a2, int a3, int prim, int cor, int acc, int gt, int gb, int hp, int sp, int sb) {
            this.aura1 = a1; this.aura2 = a2; this.aura3 = a3;
            this.primary = prim; this.core = cor; this.accent = acc;
            this.glassTop = gt; this.glassBottom = gb;
            this.headerPlate = hp; this.sectionPlate = sp; this.sectionBg = sb;
        }
    }

    public static GuiTheme getTheme(SystemData data) {
        if (data == null) return GuiTheme.SOLO_LEVELING;
        if (data.system40Updated() || data.isSystem40Updating() || data.teyvatUnlocked()) return GuiTheme.TEYVAT;
        if (data.system30Updated() || data.isSystem30Updating() || data.murimUnlocked()) return GuiTheme.MURIM;
        if (data.systemUpdated() || data.isSystemUpdating() || data.isekaiUnlocked()) return GuiTheme.ISEKAI;
        return GuiTheme.SOLO_LEVELING;
    }

    public static ResourceLocation getFrameForTheme(GuiTheme theme) {
        if (theme == null) return SYSTEM_WINDOW_FRAME;
        return switch (theme) {
            case ISEKAI -> SYSTEM_WINDOW_FRAME_ISEKAI;
            case MURIM -> SYSTEM_WINDOW_FRAME_MURIM;
            case TEYVAT -> SYSTEM_WINDOW_FRAME_TEYVAT;
            default -> SYSTEM_WINDOW_FRAME;
        };
    }

    /**
     * Draws a master manhwa system window with authentic translucent glass texture,
     * glowing neon borders, and cybernetic filigree.
     */
    public static void drawModernWindow(GuiGraphics g, int x, int y, int w, int h, GuiTheme theme) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Under-frame frosted glass backplate for authentic manhwa opacity & contrast (matching SystemScreen)
        int gt = theme != null ? theme.glassTop : 0x780A1828;
        int gb = theme != null ? theme.glassBottom : 0x88050C16;
        g.fillGradient(x + 8, y + 8, x + w - 8, y + h - 8, gt, gb);

        // Render the official Manhwa System Window Frame (translucent frosted cobalt glass & filigree)
        ResourceLocation frame = getFrameForTheme(theme);
        drawFrameNineSlice(g, frame, x, y, w, h);
    }

    /**
     * The 880 x 620 frame drawn in pieces: the corners and the ornament on top keep their shape at one size, and
     * only the straight edges and the glass stretch, so a wide or tall window does not blow up the filigree.
     */
    private static void drawFrameNineSlice(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h) {
        final int TW = 880, TH = 620, C = 64, ORN_L = 352, ORN_R = 528;
        float k = Math.max(0.3F, Math.min(0.5F, Math.min(w / (float) TW, h / (float) TH)));
        int c = Math.round(C * k);
        int ornW = Math.round((ORN_R - ORN_L) * k);
        int midW = w - 2 * c, midH = h - 2 * c;
        if (midW <= ornW + 2 || midH <= 0) {
            g.blit(tex, x, y, w, h, 0.0F, 0.0F, TW, TH, TW, TH);
            return;
        }
        // corners
        g.blit(tex, x, y, c, c, 0, 0, C, C, TW, TH);
        g.blit(tex, x + w - c, y, c, c, TW - C, 0, C, C, TW, TH);
        g.blit(tex, x, y + h - c, c, c, 0, TH - C, C, C, TW, TH);
        g.blit(tex, x + w - c, y + h - c, c, c, TW - C, TH - C, C, C, TW, TH);
        // top edge: stretched on both sides of the ornament, which keeps its shape in the middle
        int ornX = x + w / 2 - ornW / 2;
        g.blit(tex, x + c, y, ornX - (x + c), c, C, 0, ORN_L - C, C, TW, TH);
        g.blit(tex, ornX, y, ornW, c, ORN_L, 0, ORN_R - ORN_L, C, TW, TH);
        g.blit(tex, ornX + ornW, y, x + w - c - (ornX + ornW), c, ORN_R, 0, TW - C - ORN_R, C, TW, TH);
        // bottom, left and right edges, and the glass
        g.blit(tex, x + c, y + h - c, midW, c, C, TH - C, TW - 2 * C, C, TW, TH);
        g.blit(tex, x, y + c, c, midH, 0, C, C, TH - 2 * C, TW, TH);
        g.blit(tex, x + w - c, y + c, c, midH, TW - C, C, C, TH - 2 * C, TW, TH);
        g.blit(tex, x + c, y + c, midW, midH, C, C, TW - 2 * C, TH - 2 * C, TW, TH);
    }

    /**
     * Draws a modern manhwa popup modal dialog with translucent glass and glowing border.
     */
    public static void drawModernPopup(GuiGraphics g, int x, int y, int w, int h, GuiTheme theme) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int gt = theme != null ? theme.glassTop : 0x780A1828;
        int gb = theme != null ? theme.glassBottom : 0x88050C16;
        g.fillGradient(x + 6, y + 6, x + w - 6, y + h - 6, gt, gb);

        // Render official Manhwa System Popup Frame
        g.blit(SYSTEM_POPUP_FRAME, x, y, w, h, 0.0F, 0.0F, 768, 432, 768, 432);
    }

    /**
     * Draws an integrated glowing header plate.
     */
    public static void drawModernHeader(GuiGraphics g, int x, int y, int w, int h, Component title, int titleColor, GuiTheme theme, Font font) {
        // Frosted header plate
        g.fillGradient(x, y, x + w, y + h, theme.headerPlate, 0x0500E5FF);
        // Left glowing vertical neon stripe
        g.fill(x, y, x + 3, y + h, theme.primary);
        g.fill(x + 1, y, x + 2, y + h, theme.core);
        // Horizontal glowing divider line
        g.fill(x, y + h - 1, x + w, y + h, theme.primary);
        g.fill(x + 10, y + h - 1, x + w - 10, y + h, theme.core);

        if (title != null && font != null) {
            g.drawString(font, manhwa(title), x + 14, y + (h - 8) / 2, titleColor);
        }
    }

    /**
     * Draws a modern manhwa subpanel section with clean translucent glass and glowing header.
     */
    public static void drawModernSection(GuiGraphics g, int x, int y, int w, int h, Component title, GuiTheme theme, Font font) {
        // Translucent Glass background
        g.fill(x, y, x + w, y + h, theme.sectionBg);
        g.renderOutline(x, y, w, h, (theme.primary & 0x00FFFFFF) | 0x80000000);
        g.fill(x + 2, y, x + w - 2, y + 1, (theme.primary & 0x00FFFFFF) | 0xCC000000);

        boolean hasTitle = title != null && !title.getString().trim().isEmpty();
        if (hasTitle) {
            Component mTitle = manhwa(title);
            // Header plate
            g.fillGradient(x + 1, y + 1, x + w - 1, y + 18, theme.sectionPlate, 0x0500E5FF);
            // Header line
            g.fill(x + 1, y + 17, x + w - 1, y + 18, (theme.primary & 0x00FFFFFF) | 0x99000000);
            // Left vertical indicator
            g.fill(x + 1, y + 2, x + 3, y + 16, theme.primary);

            if (font != null) {
                int maxW = Math.max(20, w - 20);
                if (font.width(mTitle) > maxW) {
                    var pose = g.pose();
                    pose.pushPose();
                    pose.translate(x + 8, y + 5, 0);
                    float s = (float) maxW / font.width(mTitle);
                    pose.scale(s, s, 1.0F);
                    g.drawString(font, mTitle, 0, 0, theme.accent);
                    pose.popPose();
                } else {
                    g.drawString(font, mTitle, x + 8, y + 5, theme.accent);
                }
            }
        }
    }

    /**
     * Draws a modern manhwa gauge bar with dark glass trough, dynamic fill, and top specular shine.
     */
    public static void drawModernGauge(GuiGraphics g, int x, int y, int w, int h, float ratio, int col1, int col2, Component label, Font font) {
        // Trough
        g.fill(x, y, x + w, y + h, 0x66081420);
        g.renderOutline(x, y, w, h, 0x882D7691);

        int fillW = (int) ((w - 2) * Math.max(0.0F, Math.min(1.0F, ratio)));
        if (fillW > 0) {
            g.fillGradient(x + 1, y + 1, x + 1 + fillW, y + h - 1, col1, col2);
            g.fill(x + 1, y + 1, x + 1 + fillW, Math.min(y + 3, y + h - 1), 0x60FFFFFF);
        }

        if (label != null && font != null) {
            g.drawCenteredString(font, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
        }
    }

    /**
     * Draws a sleek modern cyber-button.
     */
    public static void drawModernButton(GuiGraphics g, int x, int y, int w, int h, Component label, boolean hovered, GuiTheme theme, Font font) {
        int bg = hovered ? 0x991B3A54 : 0x45091522;
        int border = hovered ? 0xFFFFFFFF : theme.primary;
        int textCol = hovered ? 0xFFFFFFFF : theme.accent;

        g.fill(x, y, x + w, y + h, bg);
        g.renderOutline(x, y, w, h, border);
        g.fill(x + 2, y, x + w - 2, y + 1, (border & 0x00FFFFFF) | 0x88000000);

        if (label != null && font != null) {
            g.drawCenteredString(font, label, x + w / 2, y + (h - 8) / 2, textCol);
        }
    }
}
