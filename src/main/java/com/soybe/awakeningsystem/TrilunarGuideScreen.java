package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Interfaz sagrada de la Guía de la Diosa Trilunar.
 * Reemplaza el spam de mensajes en chat con un libro místico visual
 * con pestañas de Lore, Estado dinámico de Autoridades y Localizador de Altares.
 */
public class TrilunarGuideScreen extends Screen {

    private static final int BOOK_WIDTH = 320;
    private static final int BOOK_HEIGHT = 224;

    private int activePage = 0;
    private static final int TOTAL_PAGES = 3;

    public TrilunarGuideScreen() {
        super(Component.translatable("gui.awakening_system.trilunar_guide.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) {
            if (activePage > 0) {
                activePage--;
                playTurnSound();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) {
            if (activePage < TOTAL_PAGES - 1) {
                activePage++;
                playTurnSound();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_1) {
            activePage = 0;
            playTurnSound();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_2) {
            activePage = 1;
            playTurnSound();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_3) {
            activePage = 2;
            playTurnSound();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / (BOOK_WIDTH + 20.0F);
        float scaleY = (float) this.height / (BOOK_HEIGHT + 20.0F);
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

    private void playTurnSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.9F, 1.1F);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x55060D15);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        guiGraphics.pose().pushPose();
        if (scale < 1.0F) {
            float cxF = this.width / 2.0F;
            float cyF = this.height / 2.0F;
            guiGraphics.pose().translate(cxF, cyF, 0.0F);
            guiGraphics.pose().scale(scale, scale, 1.0F);
            guiGraphics.pose().translate(-cxF, -cyF, 0.0F);
        }

        int left = (this.width - BOOK_WIDTH) / 2;
        int top = (this.height - BOOK_HEIGHT) / 2;

        guiGraphics.fill(left - 4, top - 4, left + BOOK_WIDTH + 4, top + BOOK_HEIGHT + 4, 0xFF080C16);
        guiGraphics.fill(left - 2, top - 2, left + BOOK_WIDTH + 2, top + BOOK_HEIGHT + 2, 0xFF2A284A);
        guiGraphics.fill(left - 1, top - 1, left + BOOK_WIDTH + 1, top + BOOK_HEIGHT + 1, 0xFF584C82);

        guiGraphics.fill(left, top, left + BOOK_WIDTH, top + BOOK_HEIGHT, 0xFF0F1424);
        guiGraphics.fill(left + 3, top + 3, left + BOOK_WIDTH - 3, top + BOOK_HEIGHT - 3, 0xFF141A30);

        drawOutline(guiGraphics, left + 5, top + 5, BOOK_WIDTH - 10, BOOK_HEIGHT - 10, 0xFF4A4B75);

        renderTabs(guiGraphics, left, top, mX, mY);

        int closeX = left + BOOK_WIDTH - 18;
        int closeY = top + 8;
        boolean hoverClose = mX >= closeX && mX <= closeX + 12 && mY >= closeY && mY <= closeY + 12;
        guiGraphics.drawString(this.font, hoverClose ? "§c✕" : "§7✕", closeX, closeY, 0xFFFFFFFF, false);

        switch (activePage) {
            case 0 -> renderPageLore(guiGraphics, left, top);
            case 1 -> renderPageAuthorities(guiGraphics, left, top);
            case 2 -> renderPageSanctuary(guiGraphics, left, top);
        }

        renderBottomNav(guiGraphics, left, top, mX, mY);

        super.render(guiGraphics, mX, mY, partialTick);
        guiGraphics.pose().popPose();
    }

    private void renderTabs(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        String[] tabs = {
            Component.translatable("gui.awakening_system.trilunar_guide.tab.lore").getString(),
            Component.translatable("gui.awakening_system.trilunar_guide.tab.authorities").getString(),
            Component.translatable("gui.awakening_system.trilunar_guide.tab.sanctuary").getString()
        };
        int tabW = 88;
        int tabH = 16;
        int tabY = top + 8;

        for (int i = 0; i < tabs.length; i++) {
            int tabX = left + 10 + (i * (tabW + 6));
            boolean selected = (i == activePage);
            boolean hover = mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + tabH;

            int bg = selected ? 0xFF352B58 : (hover ? 0xFF24233F : 0xFF1A1D30);
            int border = selected ? 0xFFC0A0FF : (hover ? 0xFF7D72B0 : 0xFF3E3D60);
            guiGraphics.fill(tabX, tabY, tabX + tabW, tabY + tabH, bg);
            drawOutline(guiGraphics, tabX, tabY, tabW, tabH, border);

            String title = (selected ? "§d§l" : (hover ? "§f" : "§7")) + tabs[i];
            guiGraphics.drawCenteredString(this.font, title, tabX + tabW / 2, tabY + 4, 0xFFFFFFFF);
        }
    }

    private void renderPageLore(GuiGraphics guiGraphics, int left, int top) {
        int contentX = left + 14;
        int y = top + 34;

        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.lore.title").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 12;
        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.lore.subtitle").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 14;

        guiGraphics.fill(contentX, y, left + BOOK_WIDTH - 14, y + 1, 0xFF4A4B75);
        y += 8;

        String[] loreLines = {
                Component.translatable("gui.awakening_system.trilunar_guide.lore.line1").getString(),
                Component.translatable("gui.awakening_system.trilunar_guide.lore.line2").getString(),
                Component.translatable("gui.awakening_system.trilunar_guide.lore.line3").getString(),
                "",
                Component.translatable("gui.awakening_system.trilunar_guide.lore.mission_title").getString(),
                Component.translatable("gui.awakening_system.trilunar_guide.lore.mission_line1").getString(),
                Component.translatable("gui.awakening_system.trilunar_guide.lore.mission_line2").getString(),
                Component.translatable("gui.awakening_system.trilunar_guide.lore.mission_line3").getString(),
                "",
                Component.translatable("gui.awakening_system.trilunar_guide.lore.goal_title").getString(),
                Component.translatable("gui.awakening_system.trilunar_guide.lore.goal_line1").getString(),
                Component.translatable("gui.awakening_system.trilunar_guide.lore.goal_line2").getString(),
                Component.translatable("gui.awakening_system.trilunar_guide.lore.goal_line3").getString()
        };

        for (String line : loreLines) {
            guiGraphics.drawString(this.font, line, contentX, y, 0xFFFFFFFF, false);
            y += 10;
        }
    }

    private void renderPageAuthorities(GuiGraphics guiGraphics, int left, int top) {
        int contentX = left + 14;
        int y = top + 34;

        Player player = Minecraft.getInstance().player;
        boolean hasAria = player != null && player.getInventory().contains(new ItemStack(SystemItems.LUNAR_AUTHORITY_ARIA.get()));
        boolean hasSonnet = player != null && player.getInventory().contains(new ItemStack(SystemItems.LUNAR_AUTHORITY_SONNET.get()));
        boolean hasCanon = player != null && player.getInventory().contains(new ItemStack(SystemItems.LUNAR_AUTHORITY_CANON.get()));

        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.auth.title").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 12;
        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.auth.subtitle").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 14;

        guiGraphics.fill(contentX, y, left + BOOK_WIDTH - 14, y + 1, 0xFF4A4B75);
        y += 8;

        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.auth.aria_title").getString(), contentX, y, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, hasAria ? Component.translatable("gui.awakening_system.trilunar_guide.auth.obtained").getString() : Component.translatable("gui.awakening_system.trilunar_guide.auth.pending").getString(), left + BOOK_WIDTH - 90, y, 0xFFFFFFFF, false);
        y += 10;
        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.auth.aria_desc").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 16;

        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.auth.sonnet_title").getString(), contentX, y, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, hasSonnet ? Component.translatable("gui.awakening_system.trilunar_guide.auth.obtained").getString() : Component.translatable("gui.awakening_system.trilunar_guide.auth.pending").getString(), left + BOOK_WIDTH - 90, y, 0xFFFFFFFF, false);
        y += 10;
        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.auth.sonnet_desc").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 16;

        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.auth.canon_title").getString(), contentX, y, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, hasCanon ? Component.translatable("gui.awakening_system.trilunar_guide.auth.obtained").getString() : Component.translatable("gui.awakening_system.trilunar_guide.auth.pending").getString(), left + BOOK_WIDTH - 90, y, 0xFFFFFFFF, false);
        y += 10;
        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.auth.canon_desc").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 18;

        if (hasAria && hasSonnet && hasCanon) {
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.auth.all_gathered").getString(), contentX, y, 0xFFFFFFFF, false);
        } else {
            int missing = (hasAria ? 0 : 1) + (hasSonnet ? 0 : 1) + (hasCanon ? 0 : 1);
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.auth.missing", missing).getString(), contentX, y, 0xFFFFFFFF, false);
        }
    }

    private void renderPageSanctuary(GuiGraphics guiGraphics, int left, int top) {
        int contentX = left + 14;
        int y = top + 34;

        Player player = Minecraft.getInstance().player;
        SystemData data = player != null ? player.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
        boolean awakened = data != null && data.isWelkinEnhanced();

        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.title").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 12;
        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.subtitle").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 14;

        guiGraphics.fill(contentX, y, left + BOOK_WIDTH - 14, y + 1, 0xFF4A4B75);
        y += 8;

        if (awakened) {
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.awakened_title").getString(), contentX, y, 0xFFFFFFFF, false);
            y += 11;
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.bonus_title").getString(), contentX, y, 0xFFFFFFFF, false);
            y += 10;
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.bonus_desc").getString(), contentX, y, 0xFFFFFFFF, false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.asleep_title").getString(), contentX, y, 0xFFFFFFFF, false);
            y += 11;
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.asleep_desc").getString(), contentX, y, 0xFFFFFFFF, false);
        }
        y += 18;

        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.sensor_title").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 12;

        BlockPos nearestAltar = findNearestAltar();
        if (nearestAltar != null && player != null) {
            double dist = Math.sqrt(player.blockPosition().distSqr(nearestAltar));
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.detected", (int) dist).getString(), contentX, y, 0xFFFFFFFF, false);
            y += 10;
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.coords", nearestAltar.getX(), nearestAltar.getY(), nearestAltar.getZ()).getString(), contentX, y, 0xFFFFFFFF, false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.not_detected").getString(), contentX, y, 0xFFFFFFFF, false);
            y += 10;
            guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.hint_moon").getString(), contentX, y, 0xFFFFFFFF, false);
        }
        y += 16;

        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.ritual_title").getString(), contentX, y, 0xFFFFFFFF, false);
        y += 10;
        guiGraphics.drawString(this.font, Component.translatable("gui.awakening_system.trilunar_guide.sanc.ritual_desc").getString(), contentX, y, 0xFFFFFFFF, false);
    }

    private BlockPos findNearestAltar() {
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) return null;
        BlockPos playerPos = this.minecraft.player.blockPosition();
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (int dx = -64; dx <= 64; dx += 4) {
            for (int dz = -64; dz <= 64; dz += 4) {
                for (int dy = -16; dy <= 16; dy += 4) {
                    BlockPos check = playerPos.offset(dx, dy, dz);
                    if (this.minecraft.level.getBlockState(check).is(SystemBlocks.TRILUNAR_ALTAR.get())) {
                        double d = playerPos.distSqr(check);
                        if (d < nearestDist) {
                            nearestDist = d;
                            nearest = check;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private void renderBottomNav(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int navY = top + BOOK_HEIGHT - 18;

        int prevX = left + 14;
        boolean hoverPrev = mouseX >= prevX && mouseX <= prevX + 70 && mouseY >= navY - 2 && mouseY <= navY + 10;
        if (activePage > 0) {
            guiGraphics.drawString(this.font, hoverPrev ? Component.translatable("gui.awakening_system.trilunar_guide.nav.prev_hover").getString() : Component.translatable("gui.awakening_system.trilunar_guide.nav.prev").getString(), prevX, navY, 0xFFFFFFFF, false);
        }

        String pageStr = Component.translatable("gui.awakening_system.trilunar_guide.nav.page", activePage + 1, TOTAL_PAGES).getString();
        guiGraphics.drawCenteredString(this.font, pageStr, left + BOOK_WIDTH / 2, navY, 0xFFFFFFFF);

        int nextX = left + BOOK_WIDTH - 84;
        boolean hoverNext = mouseX >= nextX && mouseX <= nextX + 70 && mouseY >= navY - 2 && mouseY <= navY + 10;
        if (activePage < TOTAL_PAGES - 1) {
            guiGraphics.drawString(this.font, hoverNext ? Component.translatable("gui.awakening_system.trilunar_guide.nav.next_hover").getString() : Component.translatable("gui.awakening_system.trilunar_guide.nav.next").getString(), nextX, navY, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0) {
            int left = (this.width - BOOK_WIDTH) / 2;
            int top = (this.height - BOOK_HEIGHT) / 2;

            int closeX = left + BOOK_WIDTH - 18;
            int closeY = top + 8;
            if (mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= closeY && mouseY <= closeY + 12) {
                onClose();
                return true;
            }

            int tabW = 88;
            int tabH = 16;
            int tabY = top + 8;
            for (int i = 0; i < TOTAL_PAGES; i++) {
                int tabX = left + 10 + (i * (tabW + 6));
                if (mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + tabH) {
                    if (activePage != i) {
                        activePage = i;
                        playTurnSound();
                    }
                    return true;
                }
            }

            int navY = top + BOOK_HEIGHT - 18;
            int prevX = left + 14;
            if (activePage > 0 && mouseX >= prevX && mouseX <= prevX + 70 && mouseY >= navY - 2 && mouseY <= navY + 10) {
                activePage--;
                playTurnSound();
                return true;
            }

            int nextX = left + BOOK_WIDTH - 84;
            if (activePage < TOTAL_PAGES - 1 && mouseX >= nextX && mouseX <= nextX + 70 && mouseY >= navY - 2 && mouseY <= navY + 10) {
                activePage++;
                playTurnSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawOutline(GuiGraphics guiGraphics, int x, int y, int w, int h, int color) {
        guiGraphics.fill(x, y, x + w, y + 1, color);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, color);
        guiGraphics.fill(x, y, x + 1, y + h, color);
        guiGraphics.fill(x + w - 1, y, x + w, y + h, color);
    }
}
