package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

public final class ClassSkillTreeScreen extends Screen {
    private final Screen parentScreen;
    private String selectedSkillId = null;

    private double camX = 0.0;
    private double camY = 0.0;
    private float zoom = 1.0f;
    private boolean isDragging = false;
    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    private int tickCount = 0;
    private boolean centered = false;
    private ClassSkillTree.SkillNode hoveredNode;

    public ClassSkillTreeScreen(Screen parentScreen) {
        super(Component.translatable("awakening_system.screen.skill_tree.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        PacketDistributor.sendToServer(new com.soybe.awakeningsystem.network.RequestSyncPayload());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
    }

    private void playClickSound() {
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.9F, 1.1F);
    }

    private void playUpgradeSound() {
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_SKILL_UNLOCK.value(), 1.0F, 1.0F);
    }

    /** Where everything goes: the window fills the screen (at least 620 x 360, shrunk to fit on small screens). */
    private record Layout(int left, int top, int pw, int ph, int canvasX, int canvasY, int canvasW, int canvasH,
                          int inspX, int inspY, int inspW, int inspH, int slotY) {}

    private Layout layout() {
        float scale = getGuiScale();
        int pw = Math.max(620, Math.round(this.width / scale) - 48);
        int ph = Math.max(360, Math.round(this.height / scale) - 28);
        int left = (this.width - pw) / 2;
        int top = (this.height - ph) / 2;
        int inspW = Math.min(260, Math.max(212, pw / 4));
        // inside the frame's corners (about 20 px at the edges)
        int bodyY = top + 52;
        int bodyH = ph - 52 - 58;
        return new Layout(left, top, pw, ph, left + 20, bodyY, pw - inspW - 48, bodyH,
                left + pw - inspW - 20, bodyY, inspW, bodyH, top + ph - 54);
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 640.0F;
        float scaleY = (float) this.height / 380.0F;
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

    private void enableScissorScaled(GuiGraphics g, int minX, int minY, int maxX, int maxY) {
        float scale = getGuiScale();
        if (scale >= 1.0F) {
            g.enableScissor(minX, minY, maxX, maxY);
        } else {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            int sMinX = (int) Math.floor(cx + (minX - cx) * scale);
            int sMinY = (int) Math.floor(cy + (minY - cy) * scale);
            int sMaxX = (int) Math.ceil(cx + (maxX - cx) * scale);
            int sMaxY = (int) Math.ceil(cy + (maxY - cy) * scale);
            sMinX = Math.max(0, sMinX);
            sMinY = Math.max(0, sMinY);
            sMaxX = Math.min(this.width, Math.max(sMinX, sMaxX));
            sMaxY = Math.min(this.height, Math.max(sMinY, sMaxY));
            g.enableScissor(sMinX, sMinY, sMaxX, sMaxY);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0x95000000, 0xD5000000);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        graphics.pose().pushPose();
        if (scale < 1.0F) {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            graphics.pose().translate(cx, cy, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.pose().translate(-cx, -cy, 0.0F);
        }

        Layout L = layout();
        int panelW = L.pw();
        int panelH = L.ph();
        int left = L.left();
        int top = L.top();

        SystemData data = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (data == null || !data.hasHunterClass()) {
            graphics.fill(left, top, left + panelW, top + panelH, 0xF008121A);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.skill_tree.no_class"), left + panelW / 2, top + panelH / 2 - 10, 0xFFFF5555);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.skill_tree.reach_level30"), left + panelW / 2, top + panelH / 2 + 6, 0xFFAAAAAA);
            graphics.pose().popPose();
            return;
        }

        boolean sys30 = data.system30Updated();
        boolean sys20 = (data.systemUpdated() || data.isSystemUpdating()) && !sys30;

        ModernGuiHelper.GuiTheme theme = ModernGuiHelper.getTheme(data);
        ModernGuiHelper.drawModernWindow(graphics, left, top, panelW, panelH, theme);
        ModernGuiHelper.drawModernHeader(graphics, left + 24, top + 14, panelW - 48, 32, null, 0, theme, font);

        // title and points inside the header, clear of the window's corner marks
        Component className = Component.translatable("awakening_system.class." + data.hunterClass() + ".name");
        Component headerTitle = Component.translatable("awakening_system.skill_tree.header", className).withStyle(ChatFormatting.BOLD);
        graphics.drawString(font, headerTitle, left + 30, top + 20, 0xFFFFD700);

        int sp = data.skillPoints();
        int treeCount = data.getUnlockedTreeSkillsCount();
        Component subHeader = Component.translatable("awakening_system.skill_tree.subheader", sp, treeCount);
        graphics.drawString(font, subHeader, left + 30, top + 33, 0xFFCCE3EC);

        int closeBtnW = 60;
        int closeBtnH = 18;
        int closeBtnX = left + panelW - closeBtnW - 30;
        int closeBtnY = top + 21;
        boolean closeHov = mX >= closeBtnX && mX < closeBtnX + closeBtnW && mY >= closeBtnY && mY < closeBtnY + closeBtnH;
        graphics.fill(closeBtnX, closeBtnY, closeBtnX + closeBtnW, closeBtnY + closeBtnH, closeHov ? 0xFF882222 : 0xFF142430);
        drawBorder(graphics, closeBtnX, closeBtnY, closeBtnW, closeBtnH, closeHov ? 0xFFFF5555 : 0xFF2D7691);
        graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.close_x"), closeBtnX + closeBtnW / 2, closeBtnY + 5, closeHov ? 0xFFFFFFFF : 0xFFCCE3EC);

        int canvasX = L.canvasX();
        int canvasY = L.canvasY();
        int canvasW = L.canvasW();
        int canvasH = L.canvasH();

        int inspX = L.inspX();
        int inspY = L.inspY();
        int inspW = L.inspW();
        int inspH = L.inspH();

        renderTreeCanvas(graphics, data, canvasX, canvasY, canvasW, canvasH, mX, mY);

        renderInspectionPanel(graphics, data, inspX, inspY, inspW, inspH, mX, mY);

        int slotBarX = left + 20;
        int slotBarY = L.slotY();
        int slotBarW = panelW - 40;
        int slotBarH = 34;
        renderSlotProgressTracker(graphics, data, slotBarX, slotBarY, slotBarW, slotBarH);

        if (hoveredNode != null) {
            java.util.List<Component> tip = new java.util.ArrayList<>();
            tip.add(hoveredNode.getName().copy().withStyle(ChatFormatting.BOLD));
            if (hoveredNode.isAttribute()) tip.add(hoveredNode.getEffect().copy().withStyle(ChatFormatting.GREEN));
            else tip.add(Component.translatable(hoveredNode.isRoot() ? "awakening_system.skill_tree.node.master"
                    : hoveredNode.isActive() ? "awakening_system.skill_tree.node.active" : "awakening_system.skill_tree.node.passive").withStyle(ChatFormatting.AQUA));
            if (!ClassSkillTree.has(data, hoveredNode) && data.level() < hoveredNode.requiredLevel())
                tip.add(Component.translatable("awakening_system.skill_tree.req_lvl", hoveredNode.requiredLevel()).withStyle(ChatFormatting.RED));
            // inside the window's scale, so it is the same size as the window's text
            graphics.renderComponentTooltip(font, tip, mX, mY);
        }
        graphics.pose().popPose();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static int nodeHalf(ClassSkillTree.SkillNode node) {
        if (node.isRoot()) return 20;
        if (!node.isAttribute()) return 16;
        return node.minor() ? 8 : 12;
    }

    /** A straight line of the given width between two points (a thin rotated quad), in the current pose. */
    private static void quadLine(GuiGraphics g, float x1, float y1, float x2, float y2, float w, int color) {
        float dx = x2 - x1, dy = y2 - y1, len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.5F) return;
        g.pose().pushPose();
        g.pose().translate(x1, y1, 0);
        g.pose().mulPose(com.mojang.math.Axis.ZP.rotation((float) Math.atan2(dy, dx)));
        var m = g.pose().last().pose();
        var vc = g.bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.gui());
        vc.addVertex(m, 0, -w / 2, 0).setColor(color);
        vc.addVertex(m, 0, w / 2, 0).setColor(color);
        vc.addVertex(m, len, w / 2, 0).setColor(color);
        vc.addVertex(m, len, -w / 2, 0).setColor(color);
        g.pose().popPose();
    }

    private void renderTreeCanvas(GuiGraphics graphics, SystemData data, int x, int y, int w, int h, int mouseX, int mouseY) {
        graphics.fill(x, y, x + w, y + h, 0xFF060B10);
        drawBorder(graphics, x, y, w, h, 0xFF153344);
        if (!centered) {
            // open on the start node, close enough to read the first rings
            centered = true;
            zoom = 0.3f;
            camX = w / 2.0;
            camY = h / 2.0;
        }

        enableScissorScaled(graphics, x + 1, y + 1, x + w - 1, y + h - 1);

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x + camX, y + camY, 0);
        pose.scale(zoom, zoom, 1.0f);

        ClassSkillTree.Tree tree = shownTree(data);
        if (tree == null) {
            pose.popPose();
            graphics.disableScissor();
            return;
        }
        int gridSpacing = 48;
        for (int gx = (tree.minX() - 96) / gridSpacing * gridSpacing; gx < tree.maxX() + 96; gx += gridSpacing) {
            for (int gy = (tree.minY() - 96) / gridSpacing * gridSpacing; gy < tree.maxY() + 96; gy += gridSpacing) {
                graphics.fill(gx, gy, gx + 2, gy + 2, 0x221E4258);
            }
        }

        // branch names, out past the end of each branch
        int[][] branchColors = {{0xFF602A2A, 0xFFFF7666}, {0xFF502A60, 0xFFE056FD}, {0xFF2A5060, 0xFF50FA7B}};
        for (int i = 0; i < tree.branches().size(); i++) {
            ClassSkillTree.Branch b = tree.branches().get(i);
            int[] col = branchColors[i % branchColors.length];
            String label = Component.translatable("awakening_system.skill_tree.branch." + b.name()).getString() + ": "
                    + ClassSkillTree.getPointsInBranch(data, tree.hunterClass(), b.id());
            pose.pushPose();
            pose.translate(b.x(), b.y(), 0);
            pose.scale(2.0F, 2.0F, 1.0F);
            renderBranchHeader(graphics, 0, 0, label, col[0], col[1]);
            pose.popPose();
        }

        float worldMouseX = (float) ((mouseX - (x + camX)) / zoom);
        float worldMouseY = (float) ((mouseY - (y + camY)) / zoom);
        boolean mouseInCanvas = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;

        for (String[] l : tree.links()) {
            ClassSkillTree.SkillNode p = tree.byId().get(l[0]), n = tree.byId().get(l[1]);
            boolean hp = ClassSkillTree.has(data, p), hn = ClassSkillTree.has(data, n);
            int color = hp && hn ? 0xFFFFD700 : (hp || hn) ? 0xB000E5FF : 0x60607A8A;
            // at least about a pixel wide on screen, or zoomed-out lines break up
            float lw = hp && hn ? Math.max(5.0F, 2.2F / zoom) : (hp || hn) ? Math.max(3.0F, 1.6F / zoom) : Math.max(2.0F, 1.1F / zoom);
            quadLine(graphics, p.gridX(), p.gridY(), n.gridX(), n.gridY(), lw, color);
        }

        hoveredNode = null;
        for (ClassSkillTree.SkillNode node : tree.nodes()) {
            int nx = node.gridX();
            int ny = node.gridY();
            int half = nodeHalf(node);

            boolean hovered = mouseInCanvas && worldMouseX >= nx - half - 1 && worldMouseX <= nx + half + 1 && worldMouseY >= ny - half - 1 && worldMouseY <= ny + half + 1;
            if (hovered) hoveredNode = node;
            boolean isUnlocked = ClassSkillTree.has(data, node);
            boolean canUnlock = !isUnlocked && ClassSkillTree.canUnlock(data, node.skillId());
            boolean isSelected = node.skillId().equals(selectedSkillId);

            int slotBg = isUnlocked ? (hovered ? 0xFF2A2006 : 0xFF161205) : (canUnlock ? (hovered ? 0xFF092E3D : 0xFF061B24) : (hovered ? 0xFF22262B : 0xFF121417));
            int borderColor = isSelected ? 0xFFFFFFFF : (isUnlocked ? 0xFFFFD700 : (canUnlock ? 0xFF00E5FF : 0xFF3E4F5B));

            if (canUnlock && !isSelected) {
                int alpha = 180 + (int) (Math.sin(tickCount * 0.15D) * 75);
                borderColor = (alpha << 24) | 0x00E5FF;
            }

            drawSquareSlot(graphics, nx, ny, half, slotBg, borderColor, node.isRoot() || !node.isAttribute());

            {
                // every node shows its icon at any zoom, as big as the node allows
                String glyph = node.iconSymbol();
                int glyphColor = isUnlocked ? 0xFFFFD700 : (canUnlock ? 0xFF50FA7B : 0xFF88A0B0);
                float gs = node.minor() ? 1.0F : node.isAttribute() ? 1.4F : 2.0F;
                gs = Math.min(Math.max(gs, 0.6F / zoom), half * 2.0F / 9.0F);
                pose.pushPose();
                pose.translate(nx, ny, 0);
                pose.scale(gs, gs, 1.0F);
                graphics.drawCenteredString(font, Component.literal(glyph), 0, -4, glyphColor);
                pose.popPose();
            }

            if (!node.isAttribute() && !node.isRoot()) {
                int rankNum = ClassSkillTree.rank(data, node);
                int rankColor = isUnlocked ? 0xFFFFD700 : (canUnlock ? 0xFF00E5FF : 0xFF667788);
                pose.pushPose();
                pose.translate(nx + half + 4, ny - half, 0);
                pose.scale(1.6F, 1.6F, 1.0F);
                graphics.drawString(font, Component.literal(String.valueOf(rankNum)), 0, 0, rankColor);
                pose.popPose();
                if (isUnlocked && ClassSkillTree.canUpgrade(data, node.skillId())) {
                    graphics.drawCenteredString(font, Component.literal("▲").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), nx, ny - half - 10, 0xFF50FA7B);
                }
            }
        }
        pose.popPose();
        graphics.disableScissor();

        String zoomPercent = String.format(Locale.US, "%.0f%%", zoom * 100);
        graphics.drawString(font, Component.translatable("awakening_system.skill_tree.zoom_controls", zoomPercent), x + 8, y + h - 14, 0xFF446677);

        int resetBtnX = x + w - 24;
        int resetBtnY = y + 6;
        boolean resetHov = mouseX >= resetBtnX && mouseX < resetBtnX + 18 && mouseY >= resetBtnY && mouseY < resetBtnY + 18;
        graphics.fill(resetBtnX, resetBtnY, resetBtnX + 18, resetBtnY + 18, resetHov ? 0xFF1C382E : 0xFF0A1822);
        drawBorder(graphics, resetBtnX, resetBtnY, 18, 18, resetHov ? 0xFFFFD700 : 0xFF2D7691);
        graphics.drawCenteredString(font, Component.literal("⌖"), resetBtnX + 9, resetBtnY + 5, resetHov ? 0xFFFFD700 : 0xFF88AABB);

        // the tabs (an evolved player) or the way to evolve (a base class)
        if (evolved(data)) {
            tab(graphics, x + 6, y + 6, TAB_W, Component.translatable("awakening_system.sao.tree_base"), viewBase, mouseX, mouseY);
            tab(graphics, x + 10 + TAB_W, y + 6, TAB_W, Component.translatable("awakening_system.sao.tree_evolved"), !viewBase, mouseX, mouseY);
        } else if (ClassEvolution.isBase(data.hunterClass())) {
            tab(graphics, x + 6, y + 6, TAB_W + 20, Component.literal("★ ").append(Component.translatable("awakening_system.sao.evolve")), true, mouseX, mouseY);
        }
    }

    private static final int TAB_W = 70;

    private void tab(GuiGraphics g, int tx, int ty, int w, Component text, boolean on, int mouseX, int mouseY) {
        boolean hov = mouseX >= tx && mouseX < tx + w && mouseY >= ty && mouseY < ty + 18;
        g.fill(tx, ty, tx + w, ty + 18, on ? 0xFF3A2E08 : hov ? 0xFF1C382E : 0xFF0A1822);
        drawBorder(g, tx, ty, w, 18, on || hov ? 0xFFFFD700 : 0xFF2D7691);
        String s = font.plainSubstrByWidth(text.getString(), w - 6);
        g.drawString(font, s, tx + w / 2 - font.width(s) / 2, ty + 5, on ? 0xFFFFD700 : 0xFFCCE3EC);
    }

    /** An evolved player can look at the base class's web too. */
    private boolean viewBase;

    private static boolean evolved(SystemData d) {
        String base = ClassEvolution.baseOf(d);
        return base != null && !base.equals(ClassSkillTree.normalizeClassId(d.hunterClass()));
    }

    /** The web shown: the class's own, or its base class's when that tab is on. */
    private ClassSkillTree.Tree shownTree(SystemData d) {
        if (d == null || !d.hasHunterClass()) return null;
        String base = ClassEvolution.baseOf(d);
        if (viewBase && base != null) return ClassSkillTree.getTree(base);
        return ClassSkillTree.getTree(d.hunterClass());
    }

    private void renderBranchHeader(GuiGraphics g, int cx, int cy, String text, int bg, int textColor) {
        int halfW = 56;
        g.fill(cx - halfW, cy - 6, cx + halfW, cy + 8, bg);
        drawBorder(g, cx - halfW, cy - 6, halfW * 2, 14, 0x80FFA500);
        g.drawCenteredString(font, Component.literal(text).withStyle(ChatFormatting.BOLD), cx, cy - 4, textColor);
    }

    private void drawSquareSlot(GuiGraphics g, int cx, int cy, int half, int bg, int border, boolean isRoot) {
        g.fill(cx - half, cy - half, cx + half + 1, cy + half + 1, bg);
        drawBorder(g, cx - half, cy - half, half * 2 + 1, half * 2 + 1, border);
        if (isRoot) {
            drawBorder(g, cx - half + 2, cy - half + 2, half * 2 - 3, half * 2 - 3, 0x80FFA500);
        }
    }

    private void drawDownwardArrow(GuiGraphics g, int nx, int ny, int half, int color) {
        int tipY = ny - half - 1;
        g.fill(nx - 2, tipY - 2, nx + 3, tipY - 1, color);
        g.fill(nx - 1, tipY - 1, nx + 2, tipY, color);
        g.fill(nx, tipY, nx + 1, tipY + 1, color);
    }

    private void renderInspectionPanel(GuiGraphics graphics, SystemData data, int x, int y, int w, int h, int mouseX, int mouseY) {
        graphics.fill(x, y, x + w, y + h, 0xF5060D14);
        drawBorder(graphics, x, y, w, h, 0xFF1D3B4E);
        graphics.fill(x, y, x + w, y + 2, 0xFFFFD700);

        int innerW = w - 16;

        if (selectedSkillId == null) {
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.inspection").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), x + w / 2, y + 6, 0xFFFFD700);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.select_skill"), x + w / 2, y + h / 2 - 12, 0xFF888888);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.select_skill_sub1"), x + w / 2, y + h / 2, 0xFF555555);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.select_skill_sub2"), x + w / 2, y + h / 2 + 12, 0xFF555555);
            return;
        }

        ClassSkillTree.SkillNode node = ClassSkillTree.getNode(data.hunterClass(), selectedSkillId);
        if (node == null) {
            selectedSkillId = null;
            return;
        }

        int curY = y + 8;

        List<FormattedCharSequence> titleLines = font.split(node.getName().copy().withStyle(ChatFormatting.BOLD), innerW);
        for (FormattedCharSequence line : titleLines) {
            graphics.drawString(font, line, x + 8, curY, rarityColor(node.getRarity()));
            curY += 10;
        }
        curY += 2;

        Component typeStr = node.isRoot()
                ? Component.translatable("awakening_system.skill_tree.node.master")
                : (node.isAttribute()
                ? Component.translatable(node.minor() ? "awakening_system.skill_tree.node.minor" : "awakening_system.skill_tree.node.attribute")
                : (node.isActive()
                ? Component.translatable("awakening_system.skill_tree.node.active")
                : Component.translatable("awakening_system.skill_tree.node.passive")));
        graphics.drawString(font, typeStr, x + 8, curY, 0xFF00E5FF);
        curY += 10;

        if (!node.isAttribute() && !node.isRoot()) {
            int curLvl = data.skills().getOrDefault(node.skillId(), 0);
            if (curLvl > 0) {
                Component rankTitle = ClassSkillTree.getRankTitle(curLvl);
                String stars = ClassSkillTree.getRankStars(curLvl);
                graphics.drawString(font, Component.translatable("awakening_system.skill_tree.rank_format", rankTitle, stars), x + 8, curY, 0xFFFFD700);
                curY += 11;
            } else {
                graphics.drawString(font, Component.translatable("awakening_system.skill_tree.not_learned"), x + 8, curY, 0xFFAAAAAA);
                curY += 11;
            }
        }

        graphics.fill(x + 8, curY, x + w - 8, curY + 1, 0xFF1D3B4E);
        curY += 5;

        List<FormattedCharSequence> descLines = font.split(node.getDescription(), innerW);
        for (int i = 0; i < Math.min(3, descLines.size()); i++) {
            graphics.drawString(font, descLines.get(i), x + 8, curY, 0xFFCCE3EC);
            curY += 10;
        }
        curY += 3;

        Component eff = node.getEffect();
        if (!eff.getString().isEmpty()) {
            graphics.drawString(font, Component.translatable("awakening_system.skill_tree.effect_label"), x + 8, curY, 0xFF70E8FF);
            curY += 10;
            List<FormattedCharSequence> effLines = font.split(eff, innerW);
            for (FormattedCharSequence line : effLines) {
                graphics.drawString(font, line, x + 8, curY, 0xFF88C999);
                curY += 10;
            }
            curY += 3;
        }

        if (!node.isAttribute() && !node.isRoot()) {
            int curLvl = data.skills().getOrDefault(node.skillId(), 0);
            if (curLvl >= 1 && curLvl < ClassSkillTree.MAX_SKILL_LEVEL) {
                int nextRank = curLvl + 1;
                String nextRoman = nextRank == 2 ? "II" : "III";
                Component bonusDesc = ClassSkillTree.getUpgradeBonusDescription(node, nextRank);

                int boxY = y + h - 68;
                int boxH = 34;
                graphics.fill(x + 8, boxY, x + w - 8, boxY + boxH, 0x80181500);
                drawBorder(graphics, x + 8, boxY, w - 16, boxH, 0x80FFD700);

                graphics.drawString(font, Component.translatable("awakening_system.skill_tree.upgrade_to_rank", nextRoman), x + 12, boxY + 4, 0xFFFFD700);
                List<FormattedCharSequence> bLines = font.split(bonusDesc, innerW - 8);
                int by = boxY + 14;
                for (FormattedCharSequence bl : bLines) {
                    graphics.drawString(font, bl, x + 12, by, 0xFF50FA7B);
                    by += 9;
                }
            }
        }

        int btnX = x + 8;
        int btnY = y + h - 28;
        int btnW = w - 16;
        int btnH = 22;

        boolean btnHovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        boolean isUnlocked = ClassSkillTree.has(data, node);
        int curLvl = ClassSkillTree.rank(data, node);

        if ((node.isRoot() && isUnlocked) || (node.isAttribute() && isUnlocked) || curLvl >= ClassSkillTree.MAX_SKILL_LEVEL) {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF14241C);
            drawBorder(graphics, btnX, btnY, btnW, btnH, 0xFF50FA7B);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.mastered").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), btnX + btnW / 2, btnY + 7, 0xFFFFD700);
        } else if (isUnlocked && ClassSkillTree.canUpgrade(data, node.skillId())) {
            int nextRank = curLvl + 1;
            String nextRoman = nextRank == 2 ? "II" : "III";
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHovered ? 0xFF2E6B45 : 0xFF1A462C);
            drawBorder(graphics, btnX, btnY, btnW, btnH, btnHovered ? 0xFFFFFFFF : 0xFF50FA7B);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.upgrade_btn", nextRoman).withStyle(ChatFormatting.BOLD), btnX + btnW / 2, btnY + 7, btnHovered ? 0xFFFFFFFF : 0xFFFFD700);
        } else if (isUnlocked) {
            int nextRank = curLvl + 1;
            int reqLvl = ClassSkillTree.getRequiredLevelForRank(node, nextRank);
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF241414);
            drawBorder(graphics, btnX, btnY, btnW, btnH, 0xFF663333);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.req_lvl_btn", reqLvl).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), btnX + btnW / 2, btnY + 7, 0xFFFF7666);
        } else if (ClassSkillTree.canUnlock(data, node.skillId())) {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHovered ? 0xFF007A99 : 0xFF00556B);
            drawBorder(graphics, btnX, btnY, btnW, btnH, btnHovered ? 0xFFFFFFFF : 0xFF00E5FF);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill_tree.unlock_btn").withStyle(ChatFormatting.BOLD), btnX + btnW / 2, btnY + 7, btnHovered ? 0xFFFFFFFF : 0xFF00E5FF);
        } else {
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF1A1A1A);
            drawBorder(graphics, btnX, btnY, btnW, btnH, 0xFF333333);
            Component reason = data.level() < node.requiredLevel()
                    ? Component.translatable("awakening_system.skill_tree.req_lvl", node.requiredLevel())
                    : !ClassSkillTree.isReachable(data, node)
                    ? Component.translatable("awakening_system.skill_tree.not_linked")
                    : Component.translatable("awakening_system.skill_tree.insufficient_sp");
            graphics.drawCenteredString(font, Component.literal("🔒 ").append(reason).withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), btnX + btnW / 2, btnY + 7, 0xFF888888);
        }
    }

    private void renderSlotProgressTracker(GuiGraphics graphics, SystemData data, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xE0060D12);
        drawBorder(graphics, x, y, w, h, 0xFF153344);

        int activeUnlocked = data.getUnlockedActiveSkillSlots();
        int passiveUnlocked = data.getUnlockedPassiveSkillSlots();

        Component title = Component.translatable("awakening_system.skill_tree.combat_slots_title");
        graphics.drawString(font, title, x + 8, y + 4, 0xFFFFD700);

        String[] actKeys = {"V", "R", "G", "B", "X"};
        int[] actReqs = {0, 0, 1, 3, 5};
        int actStartX = x + 8;
        int actY = y + 17;
        int activeBoxW = 44;
        for (int i = 0; i < 5; i++) {
            activeBoxW = Math.max(activeBoxW, font.width(Component.translatable("awakening_system.skill_tree.slot_req", actReqs[i])) + 8);
        }

        for (int i = 0; i < 5; i++) {
            int sx = actStartX + i * (activeBoxW + 4);
            boolean isUnlocked = i < activeUnlocked;
            int bg = isUnlocked ? 0xFF142E24 : 0xFF10161A;
            int border = isUnlocked ? 0xFF50FA7B : 0xFF334450;
            graphics.fill(sx, actY, sx + activeBoxW, actY + 12, bg);
            drawBorder(graphics, sx, actY, activeBoxW, 12, border);

            Component label = isUnlocked
                    ? Component.literal("[" + actKeys[i] + "] ✔")
                    : Component.translatable("awakening_system.skill_tree.slot_req", actReqs[i]);
            graphics.drawCenteredString(font, label, sx + activeBoxW / 2, actY + 2, isUnlocked ? 0xFF50FA7B : 0xFF777777);
        }

        int sepX = actStartX + 5 * (activeBoxW + 4) + 6;
        graphics.fill(sepX, actY, sepX + 1, actY + 12, 0xFF2D7691);

        int[] pasReqs = {0, 2, 4};
        int pasStartX = sepX + 8;
        int passiveBoxW = 54;
        for (int i = 0; i < 3; i++) {
            passiveBoxW = Math.max(passiveBoxW, font.width(Component.translatable("awakening_system.skill_tree.passive_slot", (i + 1))) + 8);
            passiveBoxW = Math.max(passiveBoxW, font.width(Component.translatable("awakening_system.skill_tree.slot_req", pasReqs[i])) + 8);
        }
        // never past the bar's right edge
        passiveBoxW = Math.min(passiveBoxW, (x + w - 8 - pasStartX) / 3 - 4);

        for (int i = 0; i < 3; i++) {
            int sx = pasStartX + i * (passiveBoxW + 4);
            boolean isUnlocked = i < passiveUnlocked;
            int bg = isUnlocked ? 0xFF2A1538 : 0xFF10161A;
            int border = isUnlocked ? 0xFFE056FD : 0xFF334450;
            graphics.fill(sx, actY, sx + passiveBoxW, actY + 12, bg);
            drawBorder(graphics, sx, actY, passiveBoxW, 12, border);

            Component label = isUnlocked
                    ? Component.translatable("awakening_system.skill_tree.passive_slot", (i + 1))
                    : Component.translatable("awakening_system.skill_tree.slot_req", pasReqs[i]);
            graphics.drawCenteredString(font, label, sx + passiveBoxW / 2, actY + 2, isUnlocked ? 0xFFE056FD : 0xFF777777);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        Layout L = layout();
        int panelW = L.pw();
        int left = L.left();
        int top = L.top();

        int closeBtnW = 60;
        int closeBtnH = 18;
        int closeBtnX = left + panelW - closeBtnW - 30;
        int closeBtnY = top + 21;
        if (mouseX >= closeBtnX && mouseX < closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY < closeBtnY + closeBtnH) {
            playClickSound();
            onClose();
            return true;
        }

        int canvasX = L.canvasX();
        int canvasY = L.canvasY();
        int canvasW = L.canvasW();
        int canvasH = L.canvasH();

        int resetBtnX = canvasX + canvasW - 24;
        int resetBtnY = canvasY + 6;
        if (mouseX >= resetBtnX && mouseX < resetBtnX + 18 && mouseY >= resetBtnY && mouseY < resetBtnY + 18) {
            playClickSound();
            centered = false;
            return true;
        }
        SystemData tabData = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (tabData != null && tabData.hasHunterClass() && mouseY >= canvasY + 6 && mouseY < canvasY + 24) {
            if (evolved(tabData) && mouseX >= canvasX + 6 && mouseX < canvasX + 6 + 2 * TAB_W + 4) {
                boolean base = mouseX < canvasX + 6 + TAB_W;
                if (base != viewBase) {
                    viewBase = base;
                    selectedSkillId = null;
                    centered = false;
                    playClickSound();
                }
                return true;
            }
            if (!evolved(tabData) && ClassEvolution.isBase(tabData.hunterClass()) && mouseX >= canvasX + 6 && mouseX < canvasX + 6 + TAB_W + 20) {
                playClickSound();
                Minecraft.getInstance().setScreen(new ClassEvolutionScreen(this));
                return true;
            }
        }

        int inspX = L.inspX();
        int inspY = L.inspY();
        int inspW = L.inspW();
        int inspH = L.inspH();
        int btnX = inspX + 8;
        int btnY = inspY + inspH - 28;
        int btnW = inspW - 16;
        int btnH = 22;

        if (selectedSkillId != null && mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
            SystemData data = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (data != null) {
                if (ClassSkillTree.canUpgrade(data, selectedSkillId)) {
                    playUpgradeSound();
                    PacketDistributor.sendToServer(new UpgradeSkillTreeNodePayload(selectedSkillId));
                    return true;
                } else if (ClassSkillTree.canUnlock(data, selectedSkillId)) {
                    playClickSound();
                    PacketDistributor.sendToServer(new UnlockSkillTreeNodePayload(selectedSkillId));
                    return true;
                }
            }
        }

        if (mouseX >= canvasX && mouseX < canvasX + canvasW && mouseY >= canvasY && mouseY < canvasY + canvasH) {
            float worldMouseX = (float) ((mouseX - (canvasX + camX)) / zoom);
            float worldMouseY = (float) ((mouseY - (canvasY + camY)) / zoom);

            SystemData data = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (data != null && data.hasHunterClass()) {
                ClassSkillTree.Tree shown = shownTree(data);
                List<ClassSkillTree.SkillNode> nodes = shown == null ? List.of() : shown.nodes();
                for (ClassSkillTree.SkillNode node : nodes) {
                    int nx = node.gridX();
                    int ny = node.gridY();
                    int half = nodeHalf(node);

                    if (worldMouseX >= nx - half - 1 && worldMouseX <= nx + half + 1 && worldMouseY >= ny - half - 1 && worldMouseY <= ny + half + 1) {
                        // a second click on a chosen node that can be learnt learns it
                        if (node.skillId().equals(selectedSkillId) && ClassSkillTree.canUnlock(data, selectedSkillId)) {
                            playUpgradeSound();
                            PacketDistributor.sendToServer(new UnlockSkillTreeNodePayload(selectedSkillId));
                            return true;
                        }
                        playClickSound();
                        selectedSkillId = node.skillId();
                        return true;
                    }
                }
            }

            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        float scale = getGuiScale();
        if (isDragging) {
            camX += dragX / scale;
            camY += dragY / scale;

            clampCamera();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        Layout L = layout();
        int canvasX = L.canvasX();
        int canvasY = L.canvasY();
        int canvasW = L.canvasW();
        int canvasH = L.canvasH();

        if (mouseX >= canvasX && mouseX < canvasX + canvasW && mouseY >= canvasY && mouseY < canvasY + canvasH) {
            float oldZoom = zoom;
            if (scrollY > 0) {
                zoom = Math.min(2.2f, zoom * 1.15f);
            } else if (scrollY < 0) {
                zoom = Math.max(0.15f, zoom / 1.15f);
            }

            if (zoom != oldZoom) {
                double relX = mouseX - canvasX;
                double relY = mouseY - canvasY;
                camX = relX - (relX - camX) * (zoom / oldZoom);
                camY = relY - (relY - camY) * (zoom / oldZoom);
                clampCamera();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /** Keeps some of the web on the canvas. */
    private void clampCamera() {
        SystemData data = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        ClassSkillTree.Tree t = shownTree(data);
        if (t == null) return;
        Layout L = layout();
        double w = L.canvasW(), h = L.canvasH();
        camX = Math.max(w * 0.5 - t.maxX() * zoom, Math.min(w * 0.5 - t.minX() * zoom, camX));
        camY = Math.max(h * 0.5 - t.maxY() * zoom, Math.min(h * 0.5 - t.minY() * zoom, camY));
    }

    private void drawThickLine(GuiGraphics g, int x1, int y1, int x2, int y2, int thickness, int color) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;
        float xInc = (float) dx / steps;
        float yInc = (float) dy / steps;
        float curX = x1;
        float curY = y1;
        int halfT = thickness / 2;
        for (int i = 0; i <= steps; i++) {
            int ix = Math.round(curX);
            int iy = Math.round(curY);
            g.fill(ix - halfT, iy - halfT, ix + halfT + 1, iy + halfT + 1, color);
            curX += xInc;
            curY += yInc;
        }
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private int rarityColor(SkillDefinitions.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFFAAAAAA;
            case UNCOMMON -> 0xFF55FF55;
            case RARE -> 0xFF55FFFF;
            case EPIC -> 0xFFFF55FF;
            case LEGENDARY -> 0xFFFFAA00;
            case MYTHIC -> 0xFFFF3333;
            case UNIQUE -> 0xFFFFD700;
        };
    }
}
