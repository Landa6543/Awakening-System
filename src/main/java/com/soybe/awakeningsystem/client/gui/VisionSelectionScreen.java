package com.soybe.awakeningsystem.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SelectVisionPayload;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Pantalla Holográfica Celestial de Resonancia Elemental (Visiones de Teyvat).
 * Rediseñada completamente con el estilo visual sci-fi/celestial del Sistema.
 */
public class VisionSelectionScreen extends Screen {
    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 240;

    private final Screen parentScreen;

    public record VisionEntry(
            String id,
            String nameKey,
            String titleKey,
            String perkDescKey,
            int color,
            ChatFormatting format,
            ResourceLocation icon
    ) {
        public String getName() {
            return Component.translatable(nameKey).getString();
        }

        public String getTitle() {
            return Component.translatable(titleKey).getString();
        }

        public String getPerkDesc() {
            return Component.translatable(perkDescKey).getString();
        }
    }

    public static final List<VisionEntry> VISIONS = List.of(
            new VisionEntry("NONE", "vision.awakening_system.none.name", "vision.awakening_system.none.title", "vision.awakening_system.none.desc", 0xFFAAAAAA, ChatFormatting.GRAY,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/no_vision_icon.png")),
            new VisionEntry("ANEMO", "vision.awakening_system.anemo.name", "vision.awakening_system.anemo.title", "vision.awakening_system.anemo.desc", 0xFF55FFAA, ChatFormatting.GREEN,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/anemo_icon.png")),
            new VisionEntry("GEO", "vision.awakening_system.geo.name", "vision.awakening_system.geo.title", "vision.awakening_system.geo.desc", 0xFFFFBB33, ChatFormatting.GOLD,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/geo_icon.png")),
            new VisionEntry("ELECTRO", "vision.awakening_system.electro.name", "vision.awakening_system.electro.title", "vision.awakening_system.electro.desc", 0xFFAA55FF, ChatFormatting.LIGHT_PURPLE,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/electro_icon.png")),
            new VisionEntry("DENDRO", "vision.awakening_system.dendro.name", "vision.awakening_system.dendro.title", "vision.awakening_system.dendro.desc", 0xFF44CC33, ChatFormatting.DARK_GREEN,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/dendro_icon.png")),
            new VisionEntry("HYDRO", "vision.awakening_system.hydro.name", "vision.awakening_system.hydro.title", "vision.awakening_system.hydro.desc", 0xFF3399FF, ChatFormatting.BLUE,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/hydro_icon.png")),
            new VisionEntry("PYRO", "vision.awakening_system.pyro.name", "vision.awakening_system.pyro.title", "vision.awakening_system.pyro.desc", 0xFFFF4422, ChatFormatting.RED,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/pyro_icon.png")),
            new VisionEntry("CRYO", "vision.awakening_system.cryo.name", "vision.awakening_system.cryo.title", "vision.awakening_system.cryo.desc", 0xFF77DDFF, ChatFormatting.AQUA,
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/cryo_icon.png"))
    );

    public VisionSelectionScreen(Screen parentScreen) {
        super(Component.translatable("vision.awakening_system.title"));
        this.parentScreen = parentScreen;
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new VisionSelectionScreen(null));
    }

    public static void open(Screen parent) {
        Minecraft.getInstance().setScreen(new VisionSelectionScreen(parent));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private SystemData data() {
        var p = Minecraft.getInstance().player;
        return p != null ? p.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x55060D15);
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / (GUI_WIDTH + 20.0F);
        float scaleY = (float) this.height / (GUI_HEIGHT + 20.0F);
        float scale = Math.min(scaleX, scaleY);
        return Math.min(1.0F, Math.max(0.35F, scale));
    }

    public double transformMouseX(double mouseX) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseX;
        double cx = this.width / 2.0;
        return (mouseX - cx) / scale + cx;
    }

    public double transformMouseY(double mouseY) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseY;
        double cy = this.height / 2.0;
        return (mouseY - cy) / scale + cy;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(transformMouseX(mouseX), transformMouseY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(transformMouseX(mouseX), transformMouseY(mouseY), button, dragX, dragY);
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

        g.fill(left - 2, top - 2, left + GUI_WIDTH + 2, top + GUI_HEIGHT + 2, 0xFFDFB76C);
        g.fill(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xF508111A);

        g.fill(left, top, left + GUI_WIDTH, top + 26, 0xFF0D1D2B);
        g.fill(left, top + 25, left + GUI_WIDTH, top + 26, 0xFFDFB76C);

        int accentCol = 0xFF4EE2EC;
        g.fill(left, top, left + 8, top + 2, accentCol);
        g.fill(left, top, left + 2, top + 8, accentCol);
        g.fill(left + GUI_WIDTH - 8, top, left + GUI_WIDTH, top + 2, accentCol);
        g.fill(left + GUI_WIDTH - 2, top, left + GUI_WIDTH, top + 8, accentCol);

        g.drawCenteredString(font, Component.translatable("vision.awakening_system.gui_title"), left + GUI_WIDTH / 2, top + 8, 0xFFDFB76C);

        boolean closeHover = mX >= left + GUI_WIDTH - 20 && mX < left + GUI_WIDTH - 6 && mY >= top + 6 && mY < top + 20;
        g.fill(left + GUI_WIDTH - 20, top + 6, left + GUI_WIDTH - 6, top + 20, closeHover ? 0xDDFF4444 : 0x66222222);
        g.drawCenteredString(font, "✕", left + GUI_WIDTH - 13, top + 8, closeHover ? 0xFFFFFFFF : 0xFFAAAAAA);

        SystemData d = data();
        String currentVision = d != null ? d.activeVision() : "NONE";

        VisionEntry hoveredEntry = null;

        int btnW = 142;
        int btnH = 26;
        int gridTop = top + 34;

        for (int i = 0; i < VISIONS.size(); i++) {
            VisionEntry v = VISIONS.get(i);
            int col = i % 2;
            int row = i / 2;
            int bx = left + 14 + col * 150;
            int by = gridTop + row * 29;

            boolean isCurrent = v.id.equalsIgnoreCase(currentVision);
            boolean hovered = mX >= bx && mX < bx + btnW && mY >= by && mY < by + btnH;

            if (hovered) {
                hoveredEntry = v;
            }

            int bgCol = isCurrent ? 0xEE143547 : (hovered ? 0xDD1B2836 : 0xAA0D1822);
            int borderCol = isCurrent ? v.color : (hovered ? 0xFFFFFFFF : 0xFF2A3D4D);

            g.fill(bx, by, bx + btnW, by + btnH, bgCol);
            g.renderOutline(bx, by, btnW, btnH, borderCol);

            if (isCurrent) {
                g.fill(bx, by, bx + 3, by + btnH, v.color);
            }

            RenderSystem.enableBlend();
            g.blit(v.icon, bx + 6, by + 5, 0, 0, 16, 16, 16, 16);

            int nameColor = isCurrent ? v.color : (hovered ? 0xFFFFFFFF : v.color);
            g.drawString(font, v.getName(), bx + 26, by + 4, nameColor);

            int maxTitleW = isCurrent ? (btnW - 26 - 16) : (btnW - 26 - 4);
            String titleStr = v.getTitle();
            if (font.width(titleStr) > maxTitleW) {
                titleStr = font.plainSubstrByWidth(titleStr, maxTitleW - 6) + "…";
            }
            g.drawString(font, "§7" + titleStr, bx + 26, by + 14, 0x888888);

            if (isCurrent) {
                g.drawCenteredString(font, "§a✔", bx + btnW - 10, by + 8, 0xFF50FA7B);
            }
        }

        VisionEntry displayEntry = hoveredEntry;
        if (displayEntry == null) {
            for (VisionEntry v : VISIONS) {
                if (v.id.equalsIgnoreCase(currentVision)) {
                    displayEntry = v;
                    break;
                }
            }
        }
        if (displayEntry == null) displayEntry = VISIONS.get(0);

        int descY = top + 154;
        int descW = GUI_WIDTH - 28;
        int descH = 48;
        g.fill(left + 14, descY, left + 14 + descW, descY + descH, 0xCC0A141E);
        g.renderOutline(left + 14, descY, descW, descH, displayEntry.color);

        String headerDesc = "§l" + displayEntry.getName() + " §r§7(" + displayEntry.getTitle() + ")";
        g.drawString(font, headerDesc, left + 20, descY + 6, displayEntry.color);

        String descText = displayEntry.getPerkDesc();
        if (font.width(descText) > descW - 16) {
            List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.literal("§f" + descText), descW - 16);
            for (int l = 0; l < Math.min(3, lines.size()); l++) {
                g.drawString(font, lines.get(l), left + 20, descY + 18 + l * 10, 0xFFE0E0E0);
            }
        } else {
            g.drawString(font, "§f" + descText, left + 20, descY + 20, 0xFFE0E0E0);
        }

        int footY = top + GUI_HEIGHT - 26;
        int footW = GUI_WIDTH - 28;
        boolean footHov = mX >= left + 14 && mX < left + 14 + footW && mY >= footY && mY < footY + 18;
        g.fill(left + 14, footY, left + 14 + footW, footY + 18, footHov ? 0xDD1B3B48 : 0xAA0D202A);
        g.renderOutline(left + 14, footY, footW, 18, footHov ? 0xFF4EE2EC : 0xFF2A5060);
        g.drawCenteredString(font, Component.translatable("vision.awakening_system.back_btn"), left + GUI_WIDTH / 2, footY + 5, 0xFF70E8FF);

        super.render(g, mX, mY, partialTick);
        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0) {
            int left = (width - GUI_WIDTH) / 2;
            int top = (height - GUI_HEIGHT) / 2;

            if (mouseX >= left + GUI_WIDTH - 20 && mouseX < left + GUI_WIDTH - 6 && mouseY >= top + 6 && mouseY < top + 20) {
                AwakeningSystemClient.playButtonClick();
                close();
                return true;
            }

            int footY = top + GUI_HEIGHT - 26;
            int footW = GUI_WIDTH - 28;
            if (mouseX >= left + 14 && mouseX < left + 14 + footW && mouseY >= footY && mouseY < footY + 18) {
                AwakeningSystemClient.playButtonClick();
                close();
                return true;
            }

            int btnW = 142;
            int btnH = 26;
            int gridTop = top + 34;

            for (int i = 0; i < VISIONS.size(); i++) {
                VisionEntry v = VISIONS.get(i);
                int col = i % 2;
                int row = i / 2;
                int bx = left + 14 + col * 150;
                int by = gridTop + row * 29;

                if (mouseX >= bx && mouseX < bx + btnW && mouseY >= by && mouseY < by + btnH) {
                    AwakeningSystemClient.playButtonClick();
                    PacketDistributor.sendToServer(new SelectVisionPayload(v.id));
                    AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.4F);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void close() {
        if (parentScreen != null) {
            Minecraft.getInstance().setScreen(parentScreen);
        } else {
            onClose();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
