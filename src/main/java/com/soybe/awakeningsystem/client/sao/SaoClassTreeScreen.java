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

import java.util.List;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The class skill tree in the full-dive look: the tree as round nodes joined by lines (orange learnt, ringed
 * learnable, grey locked) that can be dragged and zoomed, and the chosen node on the right with its button to
 * learn or upgrade it. The same actions as the normal tree window.
 */
public class SaoClassTreeScreen extends SaoWindow {
    private static final int DETAIL_W = 150;
    private float camX = Float.NaN, camY, zoom;
    private boolean dragging;
    private double lastX, lastY;
    private String chosen;

    public SaoClassTreeScreen(Screen parent) {
        super(parent, 430, 244);
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

    private void fit() {
        if (!Float.isNaN(camX)) return;
        zoom = Math.min(cw() / 520.0F, ch() / 330.0F);
        camX = (cw() - 510 * zoom) / 2.0F;
        camY = (ch() - 330 * zoom) / 2.0F - 40 * zoom;
    }

    private float sx(int gx) { return cx() + camX + gx * zoom; }
    private float sy(int gy) { return cy() + camY + gy * zoom; }

    private static float radius(ClassSkillTree.SkillNode n) {
        return n.isRoot() ? 14 : n.isAttribute() ? 9 : 11;
    }

    @Override
    protected void body(GuiGraphics g, int x, int y, int mouseX, int mouseY, float a) {
        SystemData d = data();
        fit();
        int cx = cx(), cy = cy(), cw = cw(), ch = ch();
        g.fillGradient(cx, cy, cx + cw, cy + ch, alpha(0xFAFFFFFF, a), alpha(0xF0ECECF0, a));
        if (!d.hasHunterClass()) {
            g.drawCenteredString(this.font, tr("class_pick", "Choose a class"), cx + cw / 2, cy + ch / 2, alpha(GREY, a));
            return;
        }
        List<ClassSkillTree.SkillNode> nodes = ClassSkillTree.getNodesForClass(d.hunterClass());
        // the clip is in screen pixels, so it follows the window when it is shrunk to fit
        float fs = fitScale();
        g.enableScissor(Math.round(this.width / 2.0F + (cx - this.width / 2.0F) * fs), Math.round(this.height / 2.0F + (cy - this.height / 2.0F) * fs),
                Math.round(this.width / 2.0F + (cx + cw - this.width / 2.0F) * fs), Math.round(this.height / 2.0F + (cy + ch - this.height / 2.0F) * fs));
        // branch names
        String[][] branches = {{"solidity", "branch_c"}, {"fighting_spirit", "branch_a"}, {"ultimatum", "branch_b"}};
        int[] bx = {85, 255, 425};
        for (int i = 0; i < 3; i++) {
            String s = key("branch." + branches[i][0]).getString() + " · " + ClassSkillTree.getPointsInBranch(d, d.hunterClass(), branches[i][1]);
            // pinned to the top of the canvas, above the column it names, so it never sits on a node
            float tx = sx(bx[i]);
            g.pose().pushPose();
            g.pose().translate(tx, cy + 4, 0);
            g.pose().scale(0.75F, 0.75F, 1);
            g.drawString(this.font, s, -this.font.width(s) / 2, 0, alpha(ORANGE_DARK, a), false);
            g.pose().popPose();
        }
        // links
        for (ClassSkillTree.SkillNode n : nodes) {
            if (!ClassSkillTree.isRevealed(d, n)) continue;
            for (String pid : n.prerequisiteIds()) {
                ClassSkillTree.SkillNode p = ClassSkillTree.getNode(d.hunterClass(), pid);
                if (p == null || !ClassSkillTree.isRevealed(d, p)) continue;
                boolean both = d.skills().containsKey(n.skillId()) && d.skills().containsKey(p.skillId());
                boolean parent = d.skills().containsKey(p.skillId());
                line(g, sx(p.gridX()), sy(p.gridY()), sx(n.gridX()), sy(n.gridY()), both ? 2.0F : 1.0F,
                        alpha(both ? ORANGE : parent ? 0xFFB4B4BA : 0xFFDADADE, a));
            }
        }
        // nodes
        for (ClassSkillTree.SkillNode n : nodes) {
            if (!ClassSkillTree.isRevealed(d, n)) continue;
            float nx = sx(n.gridX()), ny = sy(n.gridY()), r = radius(n) * zoom * 1.25F;
            boolean have = d.skills().containsKey(n.skillId());
            boolean can = !have && ClassSkillTree.canUnlock(d, n.skillId());
            boolean hot = (mouseX - nx) * (mouseX - nx) + (mouseY - ny) * (mouseY - ny) <= r * r && in(mouseX, mouseY, cx, cy, cw, ch);
            if (hot) hovering(9000 + n.skillId().hashCode());
            if (n.skillId().equals(chosen)) ring(g, nx, ny, r + 2.5F, 1.2F, alpha(GREY_DARK, a));
            if (have) shinyDisc(g, nx, ny, r, ORANGE, a);
            else {
                disc(g, nx, ny, r, alpha(hot ? 0xFFFFF3DC : can ? 0xFFFFFFFF : 0xFFE6E6EA, a));
                ring(g, nx, ny, r, can ? 1.4F : 0.8F, alpha(can ? ORANGE : 0xFFC4C4CA, a));
            }
            String glyph = n.iconSymbol();
            g.pose().pushPose();
            g.pose().translate(nx, ny - 3.5F * Math.max(0.6F, zoom * 1.3F), 0);
            float s = Math.max(0.6F, zoom * 1.3F);
            g.pose().scale(s, s, 1);
            g.drawString(this.font, glyph, -this.font.width(glyph) / 2, 0, alpha(have ? WHITE : can ? ORANGE_DARK : GREY_LIGHT, a), false);
            g.pose().popPose();
            if (have && !n.isRoot() && !n.isAttribute()) {
                String rk = String.valueOf(d.skills().getOrDefault(n.skillId(), 0));
                g.drawString(this.font, rk, Math.round(nx + r - 2), Math.round(ny - r - 2), alpha(ORANGE_DARK, a), false);
            }
        }
        g.disableScissor();

        // the chosen node
        int dx = cx + cw + 8, dy = cy, dw = DETAIL_W, dh = ch;
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
        Component type = n.isRoot() ? key("node.master") : n.isAttribute() ? key("node.attribute") : n.isActive() ? key("node.active") : key("node.passive");
        g.drawString(this.font, type, dx + 8, ty + 1, alpha(ORANGE_DARK, a), false);
        ty += 13;
        int lvl = d.skills().getOrDefault(n.skillId(), 0);
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
        boolean have = d.skills().containsKey(n.skillId());
        int by = dy + dh - 24;
        if (n.isRoot() || (n.isAttribute() && have) || lvl >= ClassSkillTree.MAX_SKILL_LEVEL) {
            softButton(g, dx + 8, by, dw - 16, 18, key("mastered"), true, mouseX, mouseY, a);
        } else if (have && ClassSkillTree.canUpgrade(d, n.skillId())) {
            button(g, dx + 8, by, dw - 16, 18, key("upgrade_btn", lvl + 1 == 2 ? "II" : "III"), true, mouseX, mouseY, a);
        } else if (have) {
            button(g, dx + 8, by, dw - 16, 18, key("req_lvl_btn", ClassSkillTree.getRequiredLevelForRank(n, lvl + 1)), false, mouseX, mouseY, a);
        } else if (ClassSkillTree.canUnlock(d, n.skillId())) {
            button(g, dx + 8, by, dw - 16, 18, key("unlock_btn"), true, mouseX, mouseY, a);
        } else {
            Component why = d.level() < n.requiredLevel() ? key("req_lvl", n.requiredLevel()) : key("insufficient_sp");
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
        if (in(mx, my, cx(), cy(), cw(), ch())) {
            for (ClassSkillTree.SkillNode n : ClassSkillTree.getNodesForClass(d.hunterClass())) {
                if (!ClassSkillTree.isRevealed(d, n)) continue;
                float nx = sx(n.gridX()), ny = sy(n.gridY()), r = radius(n) * zoom * 1.25F + 1;
                if ((mx - nx) * (mx - nx) + (my - ny) * (my - ny) <= r * r) {
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
            lastX = mx;
            lastY = my;
            return true;
        }
        return super.mouseDragged(rx, ry, button, ddx, ddy);
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
        zoom = Math.max(0.3F, Math.min(1.6F, zoom * (syv > 0 ? 1.12F : 1 / 1.12F)));
        // zoom around the mouse
        camX = (float) (mx - cx() - (mx - cx() - camX) * zoom / old);
        camY = (float) (my - cy() - (my - cy() - camY) * zoom / old);
        return true;
    }
}
