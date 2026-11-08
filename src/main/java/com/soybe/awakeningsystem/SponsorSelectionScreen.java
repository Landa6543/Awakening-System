package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Elección de constelación patrocinadora. Lista a la izquierda; a la derecha la ficha de la elegida
 * (nombre, título, origen, estigma, bendición, donación y todas sus misiones). El texto largo se
 * desplaza con la rueda del ratón en vez de salirse del panel.
 */
public final class SponsorSelectionScreen extends Screen {
    private static final int BASE_W = 410;
    private static final int BASE_H = 240;
    private static final int LIST_W = 132;
    private static final int ITEM_H = 28;
    private static final int BTN_H = 18;

    private final Screen parentScreen;
    private final boolean forced;
    private final List<ConstellationDefinitions.Constellation> constellations;
    private int selectedIndex = 0;
    private int listScroll = 0;
    private int bodyScroll = 0;
    private int bodyContentH = 0;

    public SponsorSelectionScreen(Screen parentScreen, boolean forced) {
        super(Component.translatable("awakening_system.screen.sponsor.header"));
        this.parentScreen = parentScreen;
        this.forced = forced;
        this.constellations = ConstellationDefinitions.constellations();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !forced) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parentScreen);
    }

    // ---------------------------------------------------------------- scaling (small windows)
    public float getGuiScale() {
        float scale = Math.min((float) this.width / (BASE_W + 16.0F), (float) this.height / (BASE_H + 16.0F));
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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    // ---------------------------------------------------------------- layout
    private int left() { return (width - BASE_W) / 2; }
    private int top() { return (height - BASE_H) / 2; }
    private int listX() { return left() + 12; }
    private int bodyTop() { return top() + 42; }
    private int bodyBottom() { return top() + BASE_H - 12; }
    private int infoX() { return listX() + LIST_W + 8; }
    private int infoW() { return left() + BASE_W - 12 - infoX(); }
    private int visibleItems() { return (bodyBottom() - bodyTop() - 4) / (ITEM_H + 2); }
    private int btnY() { return bodyBottom() - BTN_H - 6; }
    private int scrollTop() { return bodyTop() + 44; }
    private int scrollBottom() { return btnY() - 6; }

    private String fit(String s, int w) {
        return font.width(s) <= w ? s : font.plainSubstrByWidth(s, w - font.width("…")) + "…";
    }

    /** "strength" → "Fuerza" (con las claves de atributos del Sistema). */
    private static Component statName(String id) {
        String key = switch (id == null ? "" : id.toLowerCase()) {
            case "strength", "str" -> "str";
            case "agility", "agi" -> "agi";
            case "vitality", "vit" -> "vit";
            case "willpower", "wil" -> "wil";
            case "perception", "per", "sense" -> "per";
            case "intelligence", "int" -> "int";
            default -> null;
        };
        return key == null ? Component.literal(id == null ? "" : id) : Component.translatable("awakening_system.attribute." + key + ".name");
    }

    private boolean isCurrentSponsor(ConstellationDefinitions.Constellation c) {
        if (minecraft == null || minecraft.player == null) return false;
        SystemData data = minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        return data != null && c.id().equals(data.sponsorConstellation());
    }

    // ---------------------------------------------------------------- render
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, width, height, 0xCC040810, 0xE0081420);
        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cx = width / 2.0F, cy = height / 2.0F;
            g.pose().translate(cx, cy, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cx, -cy, 0.0F);
        }

        int left = left(), top = top();
        SystemData data = minecraft != null && minecraft.player != null ? minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
        ModernGuiHelper.drawModernWindow(g, left, top, BASE_W, BASE_H, ModernGuiHelper.getTheme(data));

        Component header = Component.translatable("awakening_system.screen.sponsor.header").withStyle(ChatFormatting.BOLD);
        g.drawString(font, fit(header.getString(), BASE_W - 60), left + 16, top + 12, 0xFFFFC94A, false);
        g.drawString(font, fit(Component.translatable("awakening_system.screen.sponsor.subtitle").getString(), BASE_W - 40),
                left + 16, top + 25, 0xFF8FA6BC, false);

        if (!forced) {
            int closeX = left + BASE_W - 26, closeY = top + 10;
            boolean hov = mX >= closeX && mX < closeX + 14 && mY >= closeY && mY < closeY + 14;
            g.fill(closeX, closeY, closeX + 14, closeY + 14, hov ? 0xDDFF3344 : 0x88301018);
            g.drawCenteredString(font, "✕", closeX + 7, closeY + 3, 0xFFFFFFFF);
        }

        renderList(g, mX, mY);
        if (selectedIndex >= 0 && selectedIndex < constellations.size()) {
            renderInfo(g, constellations.get(selectedIndex), mX, mY);
        }

        super.render(g, mX, mY, partialTick);
        g.pose().popPose();
    }

    private void renderList(GuiGraphics g, int mX, int mY) {
        int x = listX(), y = bodyTop(), h = bodyBottom() - bodyTop();
        g.fill(x, y, x + LIST_W, y + h, 0x55081422);
        g.renderOutline(x, y, LIST_W, h, 0x4400E5FF);

        for (int i = 0; i < visibleItems(); i++) {
            int idx = listScroll + i;
            if (idx >= constellations.size()) break;
            ConstellationDefinitions.Constellation c = constellations.get(idx);
            int iy = y + 3 + i * (ITEM_H + 2);
            boolean sel = idx == selectedIndex;
            boolean hov = mX >= x + 3 && mX < x + LIST_W - 3 && mY >= iy && mY < iy + ITEM_H;
            g.fill(x + 3, iy, x + LIST_W - 3, iy + ITEM_H, sel ? 0xDD0D3554 : (hov ? 0x990A2238 : 0x66051220));
            g.renderOutline(x + 3, iy, LIST_W - 6, ITEM_H, sel ? 0xFFFFC94A : (hov ? 0xAA70E8FF : 0x3300E5FF));
            if (sel) g.fill(x + 3, iy + 4, x + 5, iy + ITEM_H - 4, 0xFFFFC94A);

            int tw = LIST_W - 14;
            g.drawString(font, fit(c.name().getString(), tw), x + 9, iy + 5, sel ? 0xFFFFE08A : 0xFFE0F4FF, false);
            String bonus = "★ +" + statName(c.attributeBonus()).getString() + (isCurrentSponsor(c) ? "  ✔" : "");
            g.drawString(font, fit(bonus, tw), x + 9, iy + 16, isCurrentSponsor(c) ? 0xFF50FA7B : 0xFF70E8FF, false);
        }
        // scroll hint when there are more
        int max = Math.max(0, constellations.size() - visibleItems());
        if (max > 0) {
            int trackTop = y + 3, trackH = h - 6;
            int thumbH = Math.max(12, trackH * visibleItems() / constellations.size());
            int thumbY = trackTop + (trackH - thumbH) * listScroll / max;
            g.fill(x + LIST_W - 3, trackTop, x + LIST_W - 1, trackTop + trackH, 0x33000000);
            g.fill(x + LIST_W - 3, thumbY, x + LIST_W - 1, thumbY + thumbH, 0xAA00E5FF);
        }
    }

    private void renderInfo(GuiGraphics g, ConstellationDefinitions.Constellation c, int mX, int mY) {
        int x = infoX(), y = bodyTop(), w = infoW(), h = bodyBottom() - bodyTop();
        g.fill(x, y, x + w, y + h, 0x55081422);
        g.renderOutline(x, y, w, h, 0x4400E5FF);

        // cabecera de la ficha
        int tw = w - 16;
        g.drawString(font, fit("【" + c.name().getString() + "】", tw), x + 8, y + 7, 0xFFFFC94A, false);
        g.drawString(font, fit(Component.translatable("awakening_system.screen.sponsor.title_label", c.title()).getString(), tw), x + 8, y + 19, 0xFFD0E0E8, false);
        g.drawString(font, fit(Component.translatable("awakening_system.screen.sponsor.origin_label", getSeriesReference(c.id())).getString(), tw), x + 8, y + 30, 0xFF70E8FF, false);
        g.fill(x + 6, y + 41, x + w - 6, y + 42, 0x4400E5FF);

        // cuerpo desplazable
        int st = scrollTop(), sb = scrollBottom();
        int maxScroll = Math.max(0, bodyContentH - (sb - st));
        bodyScroll = Mth.clamp(bodyScroll, 0, maxScroll);
        g.enableScissor(x + 1, st, x + w - 1, sb);
        int end = drawBody(g, c, x + 8, st - bodyScroll, w - 18);
        g.disableScissor();
        bodyContentH = end - (st - bodyScroll);
        if (maxScroll > 0) {
            int trackH = sb - st;
            int thumbH = Math.max(12, trackH * trackH / bodyContentH);
            int thumbY = st + (trackH - thumbH) * bodyScroll / maxScroll;
            g.fill(x + w - 4, st, x + w - 2, sb, 0x33000000);
            g.fill(x + w - 4, thumbY, x + w - 2, thumbY + thumbH, 0xAA00E5FF);
        }

        // botón de firmar
        int bx = x + 6, bw = w - 12, by = btnY();
        boolean signed = isCurrentSponsor(c);
        boolean hov = !signed && mX >= bx && mX < bx + bw && mY >= by && mY < by + BTN_H;
        g.fill(bx, by, bx + bw, by + BTN_H, signed ? 0xCC183B2B : (hov ? 0xF00F5A3B : 0xCC093E26));
        g.renderOutline(bx, by, bw, BTN_H, signed ? 0x8850FA7B : (hov ? 0xFF00FF88 : 0x8800E5FF));
        Component label = Component.translatable(signed ? "awakening_system.screen.sponsor.already_signed" : "awakening_system.screen.sponsor.sign_contract");
        g.drawCenteredString(font, label, bx + bw / 2, by + 5, signed ? 0xFF50FA7B : (hov ? 0xFFFFFFFF : 0xFF7BED9F));
    }

    /** Estigma, bendición, donación y misiones a partir de y; devuelve dónde termina. */
    private int drawBody(GuiGraphics g, ConstellationDefinitions.Constellation c, int x, int y, int w) {
        // estigma en un recuadro que crece con su texto
        List<FormattedCharSequence> effect = font.split(c.stigmaEffect(), w - 12);
        int boxH = 16 + effect.size() * 10 + 2;
        g.fill(x, y, x + w, y + boxH, 0x80102636);
        g.fill(x, y, x + 2, y + boxH, 0xFF50FA7B);
        Component stigma = Component.translatable("awakening_system.screen.sponsor.stigma_label").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                .append(Component.literal(" ")).append(c.stigmaName().copy().withStyle(ChatFormatting.WHITE));
        g.drawString(font, fit(stigma.getString(), w - 10), x + 6, y + 4, 0xFF50FA7B, false);
        int ly = y + 16;
        for (FormattedCharSequence line : effect) {
            g.drawString(font, line, x + 6, ly, 0xFFD8E4EC, false);
            ly += 10;
        }
        y += boxH + 6;

        g.drawString(font, Component.translatable("awakening_system.screen.sponsor.attribute_blessing", statName(c.attributeBonus())), x, y, 0xFF70E8FF, false);
        y += 11;
        g.drawString(font, Component.translatable("awakening_system.screen.sponsor.initial_donation", c.defaultDonation()), x, y, 0xFFFFD866, false);
        y += 15;

        List<SponsorQuestDefinitions.Quest> quests = SponsorQuestDefinitions.getQuestsForSponsor(c.id());
        if (quests.isEmpty()) {
            g.drawString(font, Component.translatable("awakening_system.screen.sponsor.quests_support"), x, y, 0xFF8AA8B8, false);
            return y + 12;
        }
        g.drawString(font, Component.translatable("awakening_system.screen.sponsor.quests_available", quests.size()), x, y, 0xFFFF79C6, false);
        y += 12;
        for (SponsorQuestDefinitions.Quest q : quests) {
            g.drawString(font, fit("• " + q.title().getString(), w), x + 4, y, 0xFFE0ECF2, false);
            y += 10;
            String reward = "+" + q.coinReward() + " $   +" + q.bonusStatAmount() + " " + statName(q.bonusStat()).getString();
            g.drawString(font, fit(reward, w - 10), x + 12, y, 0xFF8FA6BC, false);
            y += 12;
        }
        return y;
    }

    // ---------------------------------------------------------------- input
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double mx = transformMouseX(mouseX);
        if (mx < infoX()) {
            int max = Math.max(0, constellations.size() - visibleItems());
            listScroll = Mth.clamp(listScroll - (int) Math.signum(scrollY), 0, max);
        } else {
            bodyScroll -= (int) Math.signum(scrollY) * 10;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        if (!forced) {
            int closeX = left() + BASE_W - 26, closeY = top() + 10;
            if (mouseX >= closeX && mouseX < closeX + 14 && mouseY >= closeY && mouseY < closeY + 14) {
                onClose();
                return true;
            }
        }

        int x = listX(), y = bodyTop();
        for (int i = 0; i < visibleItems(); i++) {
            int idx = listScroll + i;
            if (idx >= constellations.size()) break;
            int iy = y + 3 + i * (ITEM_H + 2);
            if (mouseX >= x + 3 && mouseX < x + LIST_W - 3 && mouseY >= iy && mouseY < iy + ITEM_H) {
                if (selectedIndex != idx) {
                    selectedIndex = idx;
                    bodyScroll = 0;
                    if (minecraft != null && minecraft.player != null) minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.7F, 1.2F);
                }
                return true;
            }
        }

        int bx = infoX() + 6, bw = infoW() - 12, by = btnY();
        if (mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + BTN_H
                && selectedIndex >= 0 && selectedIndex < constellations.size()) {
            ConstellationDefinitions.Constellation selected = constellations.get(selectedIndex);
            if (isCurrentSponsor(selected)) return true;
            AwakeningSystemClient.selectConstellation(selected.id());
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
                minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.2F);
            }
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static String getSeriesReference(String constellationId) {
        return switch (constellationId) {
            case "prisoner_of_the_golden_headband" -> "Omniscient Reader's Viewpoint / Journey to the West";
            case "abyssal_black_dragon" -> "Omniscient Reader's Viewpoint (ORV)";
            case "demon_like_judge_of_fire" -> "Omniscient Reader's Viewpoint (Uriel / Eden)";
            case "secretive_plotter" -> "Omniscient Reader's Viewpoint";
            case "salvation_reader" -> "Omniscient Reader's Viewpoint (Kim Dokja)";
            case "master_of_the_underworld", "queen_of_the_darkest_night" -> "ORV (Underworld / Hades & Persephone)";
            case "architect_of_the_system" -> "Solo Leveling (Ashborn / Architect)";
            case "iron_blooded_sword_saint" -> "The Greatest Estate Developer";
            case "sacred_water_dragon" -> "The Greatest Estate Developer (Javier / Dragon)";
            case "martial_king_nayuta" -> "Second Life Ranker (Nayuta)";
            case "caregiver_of_the_abyss" -> "The S-Classes That I Raised (Han Yoojin)";
            case "murim_heavenly_demon" -> "Nano Machine (Cheon Yeo-woon)";
            case "master_of_mobius_gacha" -> "Pick Me Up, Infinite Gacha (Han Yslat)";
            case "blue_dragon_of_retribution" -> "Doom Breaker (Suicidal Battle God)";
            default -> "Manhwa Multiverse";
        };
    }
}
