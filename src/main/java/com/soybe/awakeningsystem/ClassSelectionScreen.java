package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class ClassSelectionScreen extends Screen {
    private final Screen parentScreen;
    private int selectedIndex = 0;
    private int currentCategory = 0;
    private boolean showConfirmDialog = false;

    public record ClassOption(
            String id,
            String manhwa,
            String shortManhwa,
            int color,
            String signatureSkillId,
            int category
    ) {
        public Component getName() {
            return Component.translatable("awakening_system.class." + id + ".name");
        }

        public Component getLine1() {
            return Component.translatable("awakening_system.class." + id + ".card1");
        }

        public Component getLine2() {
            return Component.translatable("awakening_system.class." + id + ".card2");
        }

        public Component getRoleTag() {
            return Component.translatable("awakening_system.class." + id + ".role");
        }

        public Component getDescription() {
            return Component.translatable("awakening_system.class." + id + ".desc");
        }

        public Component getSignatureSkillName() {
            SkillDefinitions.Definition def = SkillDefinitions.get(signatureSkillId);
            return def != null ? def.name() : Component.literal(signatureSkillId);
        }

        public List<Component> getPerks() {
            return List.of(
                    Component.translatable("awakening_system.class." + id + ".perk1"),
                    Component.translatable("awakening_system.class." + id + ".perk2"),
                    Component.translatable("awakening_system.class." + id + ".perk3")
            );
        }
    }

    private static final List<ClassOption> ALL_CLASSES = List.of(
            new ClassOption("colossal_berserker", "Survival Story of a Sword King", "Sword King", 0xFFFF5555, "cb_valterian_instinct", 0),
            new ClassOption("sword_saint", "Return of the Blossoming Blade", "Blossoming Blade", 0xFF50FA7B, "asrahan_mana_blade", 0),
            new ClassOption("void_assassin", "Solo Max-Level Newbie", "Max-Level Newbie", 0xFF00E5FF, "nano_blade_overdrive", 0),
            new ClassOption("beast_monarch", "Solo Leveling / White Tiger", "Solo Leveling", 0xFFFFA500, "bm_seismic_roar", 0),

            new ClassOption("shadow_monarch", "Solo Leveling", "Solo Leveling", 0xFF9966FF, "shadow_monarch", 1),
            new ClassOption("dragon_monarch", "Solo Leveling / Dragon Sovereign", "Dragon Sovereign", 0xFFFF6E40, "isekai_dragon_roar", 1),
            new ClassOption("grand_archmage", "The Archmage Returns After 4000 Years", "4000Y Archmage", 0xFF7C4DFF, "isekai_mana_burst", 1),
            new ClassOption("blood_emperor", "Doom Breaker", "Doom Breaker", 0xFFFF1744, "be_bloodthirst", 1),

            new ClassOption("demonic_cultivator", "Nano Machine", "Nano Machine", 0xFFE040FB, "black_fire_wave", 2),
            new ClassOption("star_apostle", "Omniscient Reader's Viewpoint", "Star Stream", 0xFFFFD866, "fourth_wall", 2),
            new ClassOption("lightning_emperor", "Lightning Degree", "Lightning Degree", 0xFF00E5FF, "le_electrification", 2),
            new ClassOption("spiritual_healer", "The S-Classes That I Raised", "S-Classes", 0xFF69F0AE, "sh_caregiver_blessing", 2)
    );

    public ClassSelectionScreen(Screen parentScreen) {
        super(Component.translatable("awakening_system.screen.class_select.title"));
        this.parentScreen = parentScreen;
    }

    public List<ClassOption> getVisibleClasses() {
        if (currentCategory >= 0 && currentCategory <= 2) {
            return ALL_CLASSES.stream().filter(c -> c.category() == currentCategory).toList();
        }
        return ALL_CLASSES;
    }

    public ClassOption getSelectedClass() {
        List<ClassOption> visible = getVisibleClasses();
        if (visible.isEmpty()) return ALL_CLASSES.get(0);
        int idx = Math.max(0, Math.min(selectedIndex, visible.size() - 1));
        return visible.get(idx);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showConfirmDialog) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                showConfirmDialog = false;
                playClickSound();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
                confirmSelection();
                return true;
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }

        List<ClassOption> visible = getVisibleClasses();
        int size = visible.size();

        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) {
            selectedIndex = (selectedIndex + size - 1) % size;
            playClickSound();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) {
            selectedIndex = (selectedIndex + 1) % size;
            playClickSound();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            currentCategory = (currentCategory + 1) % 4;
            selectedIndex = 0;
            playClickSound();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
            openConfirmDialog();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void playClickSound() {
        AwakeningSystemClient.playButtonClick();
    }

    private void openConfirmDialog() {
        SystemData cData = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (cData != null && cData.hasHunterClass()) {
            AwakeningSystemClient.playUiSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            return;
        }
        showConfirmDialog = true;
        playClickSound();
    }

    private void confirmSelection() {
        SystemData cData = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        if (cData != null && cData.hasHunterClass()) {
            AwakeningSystemClient.playUiSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            return;
        }

        showConfirmDialog = false;
        ClassOption chosen = getSelectedClass();
        PacketDistributor.sendToServer(new SelectHunterClassPayload(chosen.id()));
        AwakeningSystemClient.playButtonClick();
        AwakeningSystemClient.playUiSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.2F, 1.0F);
        onClose();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 460.0F;
        float scaleY = (float) this.height / 320.0F;
        float scale = Math.min(scaleX, scaleY);
        return Math.min(1.0F, Math.max(0.4F, scale));
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0x80000000, 0xA0000000);

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

        int panelW = 440;
        int panelH = 300;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        SystemData cData = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        boolean sys30 = cData != null && cData.system30Updated();
        boolean sys20 = cData != null && (cData.systemUpdated() || cData.isSystemUpdating()) && !sys30;
        boolean alreadyChosen = cData != null && cData.hasHunterClass();

        graphics.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, sys30 ? 0xC0163E2A : (sys20 ? 0xC03A1259 : 0xC010202D));
        graphics.fill(left, top, left + panelW, top + panelH, sys30 ? 0xF00B1513 : (sys20 ? 0xF0140B22 : 0xF008121A));

        int cornerColor = (sys30 || sys20) ? 0xFFFFD700 : 0xFF70E8FF;
        graphics.fill(left, top, left + 8, top + 2, cornerColor);
        graphics.fill(left, top, left + 2, top + 8, cornerColor);
        graphics.fill(left + panelW - 8, top, left + panelW, top + 2, cornerColor);
        graphics.fill(left + panelW - 2, top, left + panelW, top + 8, cornerColor);
        graphics.fill(left, top + panelH - 2, left + 8, top + panelH, cornerColor);
        graphics.fill(left, top + panelH - 8, left + 2, top + panelH, cornerColor);
        graphics.fill(left + panelW - 8, top + panelH - 2, left + panelW, top + panelH, cornerColor);
        graphics.fill(left + panelW - 2, top + panelH - 8, left + panelW, top + panelH, cornerColor);

        graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.class_select.header"), left + panelW / 2, top + 6, 0xFFFFD700);

        int catTabW = (panelW - 30) / 4;
        String[] catLabels = {"⚔ Guerreros", "🧙 Arcano", "🥋 Murim", "✦ Todas"};
        int catY = top + 18;
        int catH = 16;

        for (int c = 0; c < 4; c++) {
            int cx = left + 15 + c * catTabW;
            boolean hov = mX >= cx && mX < cx + catTabW && mY >= catY && mY < catY + catH;
            boolean act = (currentCategory == c);

            int tabBg = act ? 0xDD1B3A4B : (hov ? 0x99162B3D : 0x550A1520);
            int tabBorder = act ? 0xFF00E5FF : (hov ? 0xFF70E8FF : 0xFF2A4255);

            graphics.fill(cx, catY, cx + catTabW - 2, catY + catH, tabBg);
            graphics.renderOutline(cx, catY, catTabW - 2, catH, tabBorder);
            graphics.drawCenteredString(font, catLabels[c], cx + (catTabW - 2) / 2, catY + 4, act ? 0xFF00E5FF : (hov ? 0xFFFFFFFF : 0xFFAAAAAA));
        }

        List<ClassOption> visible = getVisibleClasses();
        boolean isAllMode = (currentCategory == 3);

        int cardStartY = top + 38;
        if (!isAllMode) {
            int cardW = (panelW - 30 - 3 * 6) / 4;
            int cardH = 34;
            int spacingX = 6;

            for (int i = 0; i < visible.size(); i++) {
                ClassOption opt = visible.get(i);
                int cx = left + 15 + i * (cardW + spacingX);
                int cy = cardStartY;

                boolean hovered = mX >= cx && mX < cx + cardW && mY >= cy && mY < cy + cardH;
                boolean selected = (i == selectedIndex);

                int borderCol = selected ? opt.color() : (hovered ? 0xFFFFFFFF : 0xFF23556B);
                graphics.fill(cx - 1, cy - 1, cx + cardW + 1, cy + cardH + 1, borderCol);
                graphics.fill(cx, cy, cx + cardW, cy + cardH, selected ? 0xEE162B3D : 0xDD09151F);

                if (selected) {
                    graphics.fill(cx, cy, cx + cardW, cy + 2, opt.color());
                }

                graphics.enableScissor(cx + 1, cy + 1, cx + cardW - 1, cy + cardH - 1);
                int textColor1 = selected ? opt.color() : 0xFFE0E0E0;
                int textColor2 = selected ? 0xFFFFFFFF : 0xFFB0B0B0;
                graphics.drawCenteredString(font, opt.getLine1(), cx + cardW / 2, cy + 4, textColor1);
                graphics.drawCenteredString(font, opt.getLine2(), cx + cardW / 2, cy + 13, textColor2);

                if (selected) {
                    graphics.drawCenteredString(font, "✔ ELEGIDO", cx + cardW / 2, cy + 23, 0xFF50FA7B);
                } else {
                    graphics.drawCenteredString(font, "§8[" + opt.shortManhwa() + "]", cx + cardW / 2, cy + 23, 0xFF888888);
                }
                graphics.disableScissor();
            }
        } else {
            int cardW = (panelW - 30 - 5 * 4) / 6;
            int cardH = 26;
            int spacingX = 4;
            int spacingY = 3;

            for (int i = 0; i < visible.size(); i++) {
                ClassOption opt = visible.get(i);
                int col = i % 6;
                int row = i / 6;
                int cx = left + 15 + col * (cardW + spacingX);
                int cy = cardStartY + row * (cardH + spacingY);

                boolean hovered = mX >= cx && mX < cx + cardW && mY >= cy && mY < cy + cardH;
                boolean selected = (i == selectedIndex);

                int borderCol = selected ? opt.color() : (hovered ? 0xFFFFFFFF : 0xFF23556B);
                graphics.fill(cx - 1, cy - 1, cx + cardW + 1, cy + cardH + 1, borderCol);
                graphics.fill(cx, cy, cx + cardW, cy + cardH, selected ? 0xEE162B3D : 0xDD09151F);

                if (selected) {
                    graphics.fill(cx, cy, cx + cardW, cy + 2, opt.color());
                }

                graphics.enableScissor(cx + 1, cy + 1, cx + cardW - 1, cy + cardH - 1);
                int textColor1 = selected ? opt.color() : 0xFFE0E0E0;
                graphics.drawCenteredString(font, opt.getLine1(), cx + cardW / 2, cy + 4, textColor1);
                graphics.drawCenteredString(font, opt.getLine2(), cx + cardW / 2, cy + 14, selected ? 0xFF50FA7B : 0xFF888888);
                graphics.disableScissor();
            }
        }

        ClassOption sel = getSelectedClass();
        int chamberX = left + 15;
        int chamberY = isAllMode ? (top + 100) : (top + 78);
        int chamberW = panelW - 30;
        int chamberH = isAllMode ? 144 : 166;

        graphics.fill(chamberX - 1, chamberY - 1, chamberX + chamberW + 1, chamberY + chamberH + 1, sel.color());
        graphics.fill(chamberX, chamberY, chamberX + chamberW, chamberY + chamberH, 0xF508121B);

        graphics.fill(chamberX, chamberY, chamberX + 6, chamberY + 2, sel.color());
        graphics.fill(chamberX, chamberY, chamberX + 2, chamberY + 6, sel.color());
        graphics.fill(chamberX + chamberW - 6, chamberY, chamberX + chamberW, chamberY + 2, sel.color());
        graphics.fill(chamberX + chamberW - 2, chamberY, chamberX + chamberW, chamberY + 6, sel.color());
        graphics.fill(chamberX, chamberY + chamberH - 2, chamberX + 6, chamberY + chamberH, sel.color());
        graphics.fill(chamberX, chamberY + chamberH - 6, chamberX + 2, chamberY + chamberH, sel.color());
        graphics.fill(chamberX + chamberW - 6, chamberY + chamberH - 2, chamberX + chamberW, chamberY + chamberH, sel.color());
        graphics.fill(chamberX + chamberW - 2, chamberY + chamberH - 6, chamberX + chamberW, chamberY + chamberH, sel.color());

        graphics.drawString(font, Component.literal("✦ ").append(sel.getName()).append(" ✦").withStyle(s -> s.withBold(true).withColor(sel.color())), chamberX + 10, chamberY + 7, sel.color());
        Component manhwaTag = Component.translatable("awakening_system.screen.class_select.manhwa", sel.manhwa());
        graphics.drawString(font, manhwaTag, chamberX + chamberW - 10 - font.width(manhwaTag), chamberY + 7, 0xFF70E8FF);

        Component roleComp = Component.translatable("awakening_system.screen.class_select.role_label", sel.getRoleTag().getString(), sel.getDescription().getString());
        var roleLines = font.split(roleComp, chamberW - 20);
        int curTextY = chamberY + 19;
        for (var line : roleLines) {
            graphics.drawString(font, line, chamberX + 10, curTextY, 0xFFBBBBBB, false);
            curTextY += 10;
        }

        int skillBoxY = curTextY + 2;
        graphics.fill(chamberX + 8, skillBoxY, chamberX + chamberW - 8, skillBoxY + 16, 0x30FFFFFF);
        graphics.fill(chamberX + 8, skillBoxY, chamberX + 10, skillBoxY + 16, sel.color());
        Component sigSkillTitle = Component.translatable("awakening_system.class.signature_skill_header", sel.getSignatureSkillName());
        Component sigSkillStatus = Component.translatable("awakening_system.class.innate_tag");
        int maxTitleW = chamberW - 28 - font.width(sigSkillStatus) - 6;
        if (font.width(sigSkillTitle) > maxTitleW) {
            String clipped = font.plainSubstrByWidth(sigSkillTitle.getString(), maxTitleW - 8) + "...";
            graphics.drawString(font, Component.literal(clipped), chamberX + 14, skillBoxY + 4, 0xFFFFD700);
        } else {
            graphics.drawString(font, sigSkillTitle, chamberX + 14, skillBoxY + 4, 0xFFFFD700);
        }
        graphics.drawString(font, sigSkillStatus, chamberX + chamberW - 14 - font.width(sigSkillStatus), skillBoxY + 4, 0xFF50FA7B);

        int divY = skillBoxY + 19;
        graphics.fill(chamberX + 10, divY, chamberX + chamberW - 10, divY + 1, 0x40FFFFFF);

        graphics.drawString(font, Component.translatable("awakening_system.screen.class_select.perks_header"), chamberX + 10, divY + 4, 0xFFFFD700);

        int perkY = divY + 14;
        for (Component perk : sel.getPerks()) {
            var lines = font.split(Component.literal("§e✦ §f").append(perk), chamberW - 24);
            for (var line : lines) {
                graphics.drawString(font, line, chamberX + 12, perkY, 0xFFEEEEEE, false);
                perkY += 10;
            }
            perkY += 1;
        }

        if (!showConfirmDialog) {
            if (!alreadyChosen) {
                graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.class_select.warning_permanent"), left + panelW / 2, top + 256, 0xFFFF6666);
            }

            int btnW = 240;
            int btnH = 20;
            int btnX = left + (panelW - btnW) / 2;
            int btnY = top + 270;
            boolean btnHovered = mX >= btnX && mX < btnX + btnW && mY >= btnY && mY < btnY + btnH;

            if (alreadyChosen) {
                graphics.fill(btnX - 1, btnY - 1, btnX + btnW + 1, btnY + btnH + 1, 0xFF552222);
                graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF220A0A);
                Component chosenName = Component.translatable("awakening_system.class." + cData.hunterClass() + ".name");
                graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.class_select.sealed_label", chosenName), btnX + btnW / 2, btnY + 5, 0xFFFF7666);
            } else {
                graphics.fill(btnX - 1, btnY - 1, btnX + btnW + 1, btnY + btnH + 1, btnHovered ? 0xFFFFFFFF : sel.color());
                graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHovered ? sel.color() : 0xFF0E2232);
                graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.class_select.confirm_btn"), btnX + btnW / 2, btnY + 5, btnHovered ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        if (showConfirmDialog) {
            graphics.flush();
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 300.0F);

            graphics.fill(0, 0, this.width, this.height, 0xD8000000);

            int modalW = 320;
            int modalH = 135;
            int modalX = (width - modalW) / 2;
            int modalY = (height - modalH) / 2;

            graphics.fill(modalX - 2, modalY - 2, modalX + modalW + 2, modalY + modalH + 2, 0xFFFF3333);
            graphics.fill(modalX, modalY, modalX + modalW, modalY + modalH, 0xFF0E1318);

            graphics.fill(modalX, modalY, modalX + 8, modalY + 2, 0xFFFFD700);
            graphics.fill(modalX, modalY, modalX + 2, modalY + 8, 0xFFFFD700);
            graphics.fill(modalX + modalW - 8, modalY, modalX + modalW, modalY + 2, 0xFFFFD700);
            graphics.fill(modalX + modalW - 2, modalY, modalX + modalW, modalY + 8, 0xFFFFD700);
            graphics.fill(modalX, modalY + modalH - 2, modalX + 8, modalY + modalH, 0xFFFFD700);
            graphics.fill(modalX, modalY + modalH - 8, modalX + 2, modalY + modalH, 0xFFFFD700);
            graphics.fill(modalX + modalW - 8, modalY + modalH - 2, modalX + modalW, modalY + modalH, 0xFFFFD700);
            graphics.fill(modalX + modalW - 2, modalY + modalH - 8, modalX + modalW, modalY + modalH, 0xFFFFD700);

            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.class_select.confirm_title").copy().withStyle(ChatFormatting.BOLD), modalX + modalW / 2, modalY + 9, 0xFFFF5555);

            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.class_select.confirm_question", sel.getName()), modalX + modalW / 2, modalY + 24, 0xFFFFFFFF);

            int warnBoxY = modalY + 38;
            graphics.fill(modalX + 12, warnBoxY, modalX + modalW - 12, warnBoxY + 34, 0x603A0000);
            graphics.fill(modalX + 12, warnBoxY, modalX + 14, warnBoxY + 34, 0xFFFF4444);
            graphics.fill(modalX + modalW - 14, warnBoxY, modalX + modalW - 12, warnBoxY + 34, 0xFFFF4444);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.class_select.confirm_warning_1").copy().withStyle(ChatFormatting.BOLD), modalX + modalW / 2, warnBoxY + 5, 0xFFFF6B6B);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.class_select.confirm_warning_2"), modalX + modalW / 2, warnBoxY + 17, 0xFFFFD866);

            int mBtnW = 120;
            int mBtnH = 20;
            int mBtnY = modalY + modalH - 28;
            int yesX = modalX + 25;
            int noX = modalX + modalW - 25 - mBtnW;

            boolean yesHovered = mX >= yesX && mX < yesX + mBtnW && mY >= mBtnY && mY < mBtnY + mBtnH;
            boolean noHovered = mX >= noX && mX < noX + mBtnW && mY >= mBtnY && mY < mBtnY + mBtnH;

            graphics.fill(yesX - 1, mBtnY - 1, yesX + mBtnW + 1, mBtnY + mBtnH + 1, yesHovered ? 0xFFFFFFFF : 0xFF2E7D32);
            graphics.fill(yesX, mBtnY, yesX + mBtnW, mBtnY + mBtnH, yesHovered ? 0xFF388E3C : 0xEE1B5E20);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.class_select.confirm_yes").copy().withStyle(ChatFormatting.BOLD), yesX + mBtnW / 2, mBtnY + 6, 0xFFFFFFFF);

            graphics.fill(noX - 1, mBtnY - 1, noX + mBtnW + 1, mBtnY + mBtnH + 1, noHovered ? 0xFFFFFFFF : 0xFF757575);
            graphics.fill(noX, mBtnY, noX + mBtnW, mBtnY + mBtnH, noHovered ? 0xFF882222 : 0xEE333333);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.class_select.confirm_no"), noX + mBtnW / 2, mBtnY + 6, 0xFFE0E0E0);

            graphics.pose().popPose();
        }

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0) {
            if (showConfirmDialog) {
                int modalW = 320;
                int modalH = 135;
                int modalX = (width - modalW) / 2;
                int modalY = (height - modalH) / 2;

                int mBtnW = 120;
                int mBtnH = 20;
                int mBtnY = modalY + modalH - 28;
                int yesX = modalX + 25;
                int noX = modalX + modalW - 25 - mBtnW;

                if (mouseX >= yesX && mouseX < yesX + mBtnW && mouseY >= mBtnY && mouseY < mBtnY + mBtnH) {
                    confirmSelection();
                    return true;
                }
                if (mouseX >= noX && mouseX < noX + mBtnW && mouseY >= mBtnY && mouseY < mBtnY + mBtnH) {
                    showConfirmDialog = false;
                    playClickSound();
                    return true;
                }
                if (mouseX < modalX || mouseX > modalX + modalW || mouseY < modalY || mouseY > modalY + modalH) {
                    showConfirmDialog = false;
                    playClickSound();
                    return true;
                }
                return true;
            }

            int panelW = 440;
            int panelH = 300;
            int left = (width - panelW) / 2;
            int top = (height - panelH) / 2;

            int catTabW = (panelW - 30) / 4;
            int catY = top + 18;
            int catH = 16;
            for (int c = 0; c < 4; c++) {
                int cx = left + 15 + c * catTabW;
                if (mouseX >= cx && mouseX < cx + catTabW && mouseY >= catY && mouseY < catY + catH) {
                    if (currentCategory != c) {
                        currentCategory = c;
                        selectedIndex = 0;
                        playClickSound();
                    }
                    return true;
                }
            }

            List<ClassOption> visible = getVisibleClasses();
            boolean isAllMode = (currentCategory == 3);
            int cardStartY = top + 38;

            if (!isAllMode) {
                int cardW = (panelW - 30 - 3 * 6) / 4;
                int cardH = 34;
                int spacingX = 6;

                for (int i = 0; i < visible.size(); i++) {
                    int cx = left + 15 + i * (cardW + spacingX);
                    int cy = cardStartY;
                    if (mouseX >= cx && mouseX < cx + cardW && mouseY >= cy && mouseY < cy + cardH) {
                        selectedIndex = i;
                        playClickSound();
                        return true;
                    }
                }
            } else {
                int cardW = (panelW - 30 - 5 * 4) / 6;
                int cardH = 26;
                int spacingX = 4;
                int spacingY = 3;

                for (int i = 0; i < visible.size(); i++) {
                    int col = i % 6;
                    int row = i / 6;
                    int cx = left + 15 + col * (cardW + spacingX);
                    int cy = cardStartY + row * (cardH + spacingY);
                    if (mouseX >= cx && mouseX < cx + cardW && mouseY >= cy && mouseY < cy + cardH) {
                        selectedIndex = i;
                        playClickSound();
                        return true;
                    }
                }
            }

            int btnW = 240;
            int btnH = 20;
            int btnX = left + (panelW - btnW) / 2;
            int btnY = top + 270;
            if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                openConfirmDialog();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
