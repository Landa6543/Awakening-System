package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.ClassSkillTree;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.UnlockSkillTreeNodePayload;
import com.soybe.awakeningsystem.UpgradeSkillTreeNodePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The class web in the full-dive look: the web as round nodes joined by lines (orange learnt, ringed learnable,
 * grey locked) that can be dragged and zoomed, a tooltip on the node under the mouse, and the chosen node on the
 * right with its button to learn or upgrade it. The same actions as the normal tree window.
 */
public class SaoClassTreeScreen extends SaoWindow {
    private static final int DETAIL_W = 150;
    private float camX = Float.NaN, camY, zoom;
    private boolean dragging;
    private double lastX, lastY;
    private String chosen;
    /** An evolved player can look at the base class's web too. */
    private boolean viewBase;

    public SaoClassTreeScreen(Screen parent) {
        super(parent, 540, 310);   // a big window for the web (it shrinks on small screens)
    }

    @Override
    protected void init() {
        super.init();
        PacketDistributor.sendToServer(new com.soybe.awakeningsystem.network.RequestSyncPayload());
    }

    private static Component key(String k, Object... args) {
        return Component.translatable("awakening_system.skill_tree." + k, args);
    }

    @Override
    protected Component heading() {
        SystemData d = data();
        return d.hasHunterClass() ? Component.translatable("awakening_system.class." + d.hunterClass() + ".name") : tr("class_tree", "Class tree");
    }

    @Override
    protected String[] glyph() {
        return STAR;
    }

    @Override
    protected String headerRight() {
        return tr("skill_points", "Skill points: %s", data().skillPoints()).getString();
    }

    // canvas
    private int cx() { return left() + 12; }
    private int cy() { return top() + HEADER + 8; }
    private int cw() { return pw - DETAIL_W - 30; }
    private int ch() { return ph - HEADER - 16; }

    /** Opens on the start node, close enough to read the first rings. */
    private void fit() {
        if (!Float.isNaN(camX)) return;
        zoom = 0.26F;
        camX = cw() / 2.0F;
        camY = ch() / 2.0F;
    }

    /** The web shown: the class's own, or its base class's when that tab is on. */
    private ClassSkillTree.Tree shownTree(SystemData d) {
        if (!d.hasHunterClass()) return null;
        String base = com.soybe.awakeningsystem.ClassEvolution.baseOf(d.hunterClass());
        if (viewBase && base != null) return ClassSkillTree.getTree(base);
        return ClassSkillTree.getTree(d.hunterClass());
    }

    private boolean evolved(SystemData d) {
        String base = com.soybe.awakeningsystem.ClassEvolution.baseOf(d.hunterClass());
        return base != null && !base.equals(ClassSkillTree.normalizeClassId(d.hunterClass()));
    }

    private float sx(int gx) { return cx() + camX + gx * zoom; }
    private float sy(int gy) { return cy() + camY + gy * zoom; }

    private static float radius(ClassSkillTree.SkillNode n) {
        if (n.isRoot()) return 16;
        if (!n.isAttribute()) return 12;
        return n.minor() ? 6 : 9;
    }

    /** Nodes shrink slower than the web, so a zoomed-out web still shows them. */
    private float screenRadius(ClassSkillTree.SkillNode n) {
        return radius(n) * (0.5F + zoom * 0.75F);
    }

    @Override
    protected void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        fit();
        int cx = cx(), cy = cy(), cw = cw(), ch = ch();
        g.fillGradient(cx, cy, cx + cw, cy + ch, alpha(0xFAFFFFFF, a), alpha(0xF0ECECF0, a));
        ClassSkillTree.Tree tree = shownTree(d);
        if (tree == null) {
            g.drawCenteredString(this.font, d.hasHunterClass() ? tr("tree_loading", "Loading the web…") : tr("class_pick", "Choose a class"),
                    cx + cw / 2, cy + ch / 2, alpha(GREY, a));
            return;
        }
        // the clip is in screen pixels, so it follows the window when it is shrunk to fit
        float fs = fitScale();
        g.enableScissor(Math.round(this.width / 2.0F + (cx - this.width / 2.0F) * fs), Math.round(this.height / 2.0F + (cy - this.height / 2.0F) * fs),
                Math.round(this.width / 2.0F + (cx + cw - this.width / 2.0F) * fs), Math.round(this.height / 2.0F + (cy + ch - this.height / 2.0F) * fs));
        // branch names, out past the end of each branch
        for (ClassSkillTree.Branch b : tree.branches()) {
            String s = key("branch." + b.name()).getString() + " · " + ClassSkillTree.getPointsInBranch(d, tree.hunterClass(), b.id());
            g.pose().pushPose();
            g.pose().translate(sx(b.x()), sy(b.y()) - 4, 0);
            float ts = Math.max(0.7F, zoom * 1.6F);
            g.pose().scale(ts, ts, 1);
            g.drawString(this.font, s, -this.font.width(s) / 2, 0, alpha(ORANGE_DARK, a), false);
            g.pose().popPose();
        }
        // links
        for (String[] l : tree.links()) {
            ClassSkillTree.SkillNode p = tree.byId().get(l[0]), n = tree.byId().get(l[1]);
            boolean hp = ClassSkillTree.has(d, p), hn = ClassSkillTree.has(d, n);
            boolean both = hp && hn, one = hp || hn;
            line(g, sx(p.gridX()), sy(p.gridY()), sx(n.gridX()), sy(n.gridY()), both ? 2.2F : one ? 1.4F : 1.0F,
                    alpha(both ? ORANGE : one ? 0xFFB4B4BA : 0xFFDADADE, a));
        }
        // nodes
        ClassSkillTree.SkillNode hover = null;
        for (ClassSkillTree.SkillNode n : tree.nodes()) {
            float nx = sx(n.gridX()), ny = sy(n.gridY()), r = screenRadius(n);
            if (nx < cx - r || nx > cx + cw + r || ny < cy - r || ny > cy + ch + r) continue;
            boolean have = ClassSkillTree.has(d, n);
            boolean can = !have && ClassSkillTree.canUnlock(d, n.skillId());
            boolean hot = (mouseX - nx) * (mouseX - nx) + (mouseY - ny) * (mouseY - ny) <= (r + 1) * (r + 1) && in(mouseX, mouseY, cx, cy, cw, ch);
            if (hot) {
                hovering(9000 + n.skillId().hashCode());
                hover = n;
            }
            if (n.skillId().equals(chosen)) ring(g, nx, ny, r + 2.5F, 1.2F, alpha(GREY_DARK, a));
            if (have) shinyDisc(g, nx, ny, r, ORANGE, a);
            else {
                disc(g, nx, ny, r, alpha(hot ? 0xFFFFF3DC : can ? 0xFFFFFFFF : 0xFFE6E6EA, a));
                ring(g, nx, ny, r, can ? 1.4F : 0.8F, alpha(can ? ORANGE : 0xFFC4C4CA, a));
            }
            // skills are ringed twice, so they stand out among the stat nodes
            if (!n.isAttribute()) ring(g, nx, ny, r + 1.6F, 0.8F, alpha(have ? ORANGE_DARK : 0xFFC4C4CA, a));
            if (!n.minor() || zoom >= 0.75F) {
                String glyph = n.iconSymbol();
                float s = Math.max(0.5F, r / 9.0F);
                g.pose().pushPose();
                g.pose().translate(nx, ny - 3.5F * s, 0);
                g.pose().scale(s, s, 1);
                g.drawString(this.font, glyph, -this.font.width(glyph) / 2, 0, alpha(have ? WHITE : can ? ORANGE_DARK : GREY_LIGHT, a), false);
                g.pose().popPose();
            }
            if (have && !n.isRoot() && !n.isAttribute()) {
                String rk = String.valueOf(ClassSkillTree.rank(d, n));
                g.drawString(this.font, rk, Math.round(nx + r - 2), Math.round(ny - r - 2), alpha(ORANGE_DARK, a), false);
            }
        }
        g.disableScissor();
        // the tabs (an evolved player) or the way to evolve (a base class)
        if (evolved(d)) {
            softButton(g, cx + 4, cy + 4, 62, 14, tr("tree_base", "Base"), viewBase, mouseX, mouseY, a);
            softButton(g, cx + 70, cy + 4, 62, 14, tr("tree_evolved", "Evolution"), !viewBase, mouseX, mouseY, a);
        } else if (com.soybe.awakeningsystem.ClassEvolution.isBase(d.hunterClass())) {
            button(g, cx + 4, cy + 4, 80, 14, tr("evolve", "Evolve"), true, mouseX, mouseY, a);
        }
        g.fill(cx, cy + ch - 12, cx + cw, cy + ch, alpha(0xE6FFFFFF, a));
        g.drawString(this.font, this.font.plainSubstrByWidth(key("web_hint").getString(), cw - 8), cx + 4, cy + ch - 10, alpha(GREY_LIGHT, a), false);

        detail(g, d, mouseX, mouseY, a);

        if (hover != null) {
            List<Component> tip = new ArrayList<>();
            tip.add(hover.getName());
            Component eff = hover.getEffect();
            if (!eff.getString().isEmpty() && hover.isAttribute()) tip.add(eff.copy().withStyle(net.minecraft.ChatFormatting.GREEN));
            if (!hover.isAttribute()) tip.add((hover.isRoot() ? key("node.master") : hover.isActive() ? key("node.active") : key("node.passive")).copy()
                    .withStyle(net.minecraft.ChatFormatting.GOLD));
            if (!ClassSkillTree.has(d, hover) && d.level() < hover.requiredLevel())
                tip.add(key("req_lvl", hover.requiredLevel()).copy().withStyle(net.minecraft.ChatFormatting.RED));
            g.renderComponentTooltip(this.font, tip, mouseX, mouseY);
        }
    }

    private void detail(GuiGraphics g, SystemData d, int mouseX, int mouseY, float a) {
        int dx = cx() + cw() + 8, dy = cy(), dw = DETAIL_W, dh = ch();
        g.fillGradient(dx, dy, dx + dw, dy + dh, alpha(0xFAFFFFFF, a), alpha(0xF0ECECF0, a));
        ClassSkillTree.SkillNode n = chosen == null ? null : ClassSkillTree.getNode(d.hunterClass(), chosen);
        if (n == null) {
            int ty = dy + 30;
            for (FormattedCharSequence l : this.font.split(key("select_skill"), dw - 16)) {
                g.drawString(this.font, l, dx + 8, ty, alpha(GREY, a), false);
                ty += 10;
            }
            return;
        }
        g.fill(dx, dy, dx + dw, dy + 2, alpha(ORANGE, a));
        int ty = dy + 7;
        for (FormattedCharSequence l : this.font.split(n.getName(), dw - 16)) {
            g.drawString(this.font, l, dx + 8, ty, alpha(GREY_DARK, a), false);
            ty += 10;
        }
        Component type = n.isRoot() ? key("node.master") : n.isAttribute() ? key(n.minor() ? "node.minor" : "node.attribute")
                : n.isActive() ? key("node.active") : key("node.passive");
        g.drawString(this.font, type, dx + 8, ty + 1, alpha(ORANGE_DARK, a), false);
        ty += 13;
        int lvl = ClassSkillTree.rank(d, n);
        if (!n.isRoot() && !n.isAttribute()) {
            Component rank = lvl > 0 ? key("rank_format", ClassSkillTree.getRankTitle(lvl), ClassSkillTree.getRankStars(lvl)) : key("not_learned");
            g.drawString(this.font, this.font.plainSubstrByWidth(rank.getString(), dw - 16), dx + 8, ty, alpha(GREY, a), false);
            ty += 11;
        }
        g.fill(dx + 8, ty, dx + dw - 8, ty + 1, alpha(0xFFDADADE, a));
        ty += 4;
        int stop = dy + dh - 30;
        for (FormattedCharSequence l : this.font.split(n.getDescription(), dw - 16)) {
            if (ty > stop - 10) break;
            g.drawString(this.font, l, dx + 8, ty, alpha(GREY, a), false);
            ty += 9;
        }
        if (!n.getEffect().getString().isEmpty() && ty < stop - 20) {
            ty += 2;
            for (FormattedCharSequence l : this.font.split(n.getEffect(), dw - 16)) {
                if (ty > stop - 10) break;
                g.drawString(this.font, l, dx + 8, ty, alpha(0xFF4FA84A, a), false);
                ty += 9;
            }
        }
        // the button: learn, upgrade, or why not
        boolean have = ClassSkillTree.has(d, n);
        int by = dy + dh - 24;
        if ((n.isRoot() && have) || (n.isAttribute() && have) || lvl >= ClassSkillTree.MAX_SKILL_LEVEL) {
            softButton(g, dx + 8, by, dw - 16, 18, key("mastered"), true, mouseX, mouseY, a);
        } else if (have && ClassSkillTree.canUpgrade(d, n.skillId())) {
            button(g, dx + 8, by, dw - 16, 18, key("upgrade_btn", lvl + 1 == 2 ? "II" : "III"), true, mouseX, mouseY, a);
        } else if (have) {
            button(g, dx + 8, by, dw - 16, 18, key("req_lvl_btn", ClassSkillTree.getRequiredLevelForRank(n, lvl + 1)), false, mouseX, mouseY, a);
        } else if (ClassSkillTree.canUnlock(d, n.skillId())) {
            button(g, dx + 8, by, dw - 16, 18, key("unlock_btn"), true, mouseX, mouseY, a);
        } else {
            Component why = d.level() < n.requiredLevel() ? key("req_lvl", n.requiredLevel())
                    : !ClassSkillTree.isReachable(d, n) ? key("not_linked") : key("insufficient_sp");
            button(g, dx + 8, by, dw - 16, 18, why, false, mouseX, mouseY, a);
        }
    }

    /** A straight line of the given width between two points (a thin rotated quad). */
    private static void line(GuiGraphics g, float x1, float y1, float x2, float y2, float w, int color) {
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

    @Override
    protected boolean clickBody(double mx, double my, int x, int y) {
        SystemData d = data();
        if (!d.hasHunterClass()) return false;
        int dx = cx() + cw() + 8, by = cy() + ch() - 24;
        if (chosen != null && in(mx, my, dx + 8, by, DETAIL_W - 16, 18)) {
            if (ClassSkillTree.canUpgrade(d, chosen)) { PacketDistributor.sendToServer(new UpgradeSkillTreeNodePayload(chosen)); SaoSounds.confirm(); return true; }
            if (ClassSkillTree.canUnlock(d, chosen)) { PacketDistributor.sendToServer(new UnlockSkillTreeNodePayload(chosen)); SaoSounds.confirm(); return true; }
            return true;
        }
        if (evolved(d) && in(mx, my, cx() + 4, cy() + 4, 128, 14)) {
            boolean base = mx < cx() + 68;
            if (base != viewBase) {
                viewBase = base;
                chosen = null;
                camX = Float.NaN;
                SaoSounds.click();
            }
            return true;
        }
        if (!evolved(d) && com.soybe.awakeningsystem.ClassEvolution.isBase(d.hunterClass()) && in(mx, my, cx() + 4, cy() + 4, 80, 14)) {
            SaoSounds.click();
            this.minecraft.setScreen(new SaoEvolutionScreen(this));
            return true;
        }
        ClassSkillTree.Tree shown = shownTree(d);
        if (shown != null && in(mx, my, cx(), cy(), cw(), ch())) {
            for (ClassSkillTree.SkillNode n : shown.nodes()) {
                float nx = sx(n.gridX()), ny = sy(n.gridY()), r = screenRadius(n) + 1;
                if ((mx - nx) * (mx - nx) + (my - ny) * (my - ny) <= r * r) {
                    // a second click on a chosen node that can be learnt learns it, as on the web of a passive tree
                    if (n.skillId().equals(chosen) && ClassSkillTree.canUnlock(d, chosen)) {
                        PacketDistributor.sendToServer(new UnlockSkillTreeNodePayload(chosen));
                        SaoSounds.confirm();
                        return true;
                    }
                    chosen = n.skillId();
                    SaoSounds.click();
                    return true;
                }
            }
            dragging = true;
            lastX = mx;
            lastY = my;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double rx, double ry, int button, double ddx, double ddy) {
        double mx = unX(rx), my = unY(ry);
        if (dragging) {
            camX += (float) (mx - lastX);
            camY += (float) (my - lastY);
            clampCamera();
            lastX = mx;
            lastY = my;
            return true;
        }
        return super.mouseDragged(rx, ry, button, ddx, ddy);
    }

    /** Keeps some of the web on the canvas. */
    private void clampCamera() {
        ClassSkillTree.Tree t = shownTree(data());
        if (t == null) return;
        camX = Math.max(cw() * 0.5F - t.maxX() * zoom, Math.min(cw() * 0.5F - t.minX() * zoom, camX));
        camY = Math.max(ch() * 0.5F - t.maxY() * zoom, Math.min(ch() * 0.5F - t.minY() * zoom, camY));
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double rx, double ry, double sxv, double syv) {
        double mx = unX(rx), my = unY(ry);
        if (!in(mx, my, cx(), cy(), cw(), ch())) return true;
        float old = zoom;
        zoom = Math.max(0.15F, Math.min(1.6F, zoom * (syv > 0 ? 1.12F : 1 / 1.12F)));
        // zoom around the mouse
        camX = (float) (mx - cx() - (mx - cx() - camX) * zoom / old);
        camY = (float) (my - cy() - (my - cy() - camY) * zoom / old);
        clampCamera();
        return true;
    }
}
