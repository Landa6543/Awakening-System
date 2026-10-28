package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class SkillDetailScreen extends Screen {
    private static final int W = 390;
    private static final int H = 260;
    private static final int CYAN = 0xFF70E8FF;
    private static final int GOLD = 0xFFFFD866;
    private static final int MUTED = 0xFF9BB7C7;
    private final SkillDefinitions.Definition definition;
    private final int skillLevel;
    private final int progress;
    private final int progressRequired;
    private final String skillId;

    public SkillDetailScreen(String skillId, int skillLevel) {
        super(Component.translatable("awakening_system.skill.detail_title"));
        this.skillId = skillId;
        this.definition = SkillDefinitions.get(skillId);
        this.skillLevel = skillLevel;
        SystemData data = Minecraft.getInstance().player == null ? new SystemData()
                : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        this.progress = data.skillProgress(skillId);
        this.progressRequired = data.skillProgressRequired(skillId);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new SystemScreen(1));
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / (W + 20.0F);
        float scaleY = (float) this.height / (H + 20.0F);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);

        int left = (width - W) / 2;
        int top = (height - H) / 2;

        if (button == 0 && SkillDefinitions.isActive(skillId)) {
            int btnW = 120;
            int btnH = 20;
            int eqX = left + (W / 2) - btnW - 6;
            int eqY = top + 84;
            if (mouseX >= eqX && mouseX <= eqX + btnW && mouseY >= eqY && mouseY <= eqY + btnH) {
                AwakeningSystemClient.equipSkill(skillId);
                return true;
            }

            int actX = left + (W / 2) + 6;
            int actY = top + 84;
            if (mouseX >= actX && mouseX <= actX + btnW && mouseY >= actY && mouseY <= actY + btnH) {
                AwakeningSystemClient.activateSkill(skillId);
                return true;
            }
        }

        if (button == 0 && skillLevel == 5) {
            int y = top + H - 44;
            if (mouseX >= left + 40 && mouseX <= left + W - 40 && mouseY >= y && mouseY <= y + 16) {
                AwakeningSystemClient.evolve(skillId);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        graphics.pose().pushPose();
        if (scale < 1.0F) {
            float cxF = this.width / 2.0F;
            float cyF = this.height / 2.0F;
            graphics.pose().translate(cxF, cyF, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.pose().translate(-cxF, -cyF, 0.0F);
        }

        SystemData currentData = Minecraft.getInstance().player == null ? new SystemData()
                : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
        boolean sys30 = currentData.system30Updated();
        boolean isSys20 = (currentData.systemUpdated() || currentData.isSystemUpdating()) && !sys30;

        int left = (width - W) / 2;
        int top = (height - H) / 2;
        int contentX = left + 28;
        int contentWidth = W - 56;
        List<net.minecraft.util.FormattedCharSequence> description = font.split(definition.description(), contentWidth);
        List<net.minecraft.util.FormattedCharSequence> effect = font.split(definition.effect(), contentWidth);

        graphics.fill(0, 0, width, height, sys30 ? 0x600B1513 : (isSys20 ? 0x60140B22 : 0x40101A2A));
        graphics.fill(left - 3, top - 3, left + W + 3, top + H + 3, sys30 ? 0x80D4AF37 : (isSys20 ? 0x809D4EDD : 0x802D7691));
        graphics.fill(left - 1, top - 1, left + W + 1, top + H + 1, (sys30 || isSys20) ? 0xFFFFD700 : CYAN);
        graphics.fill(left, top, left + W, top + H, sys30 ? 0xF00B1513 : (isSys20 ? 0xF0140B22 : 0xF0132537));
        graphics.fill(left, top, left + W, top + 3, (sys30 || isSys20) ? 0xFFFFD700 : CYAN);

        graphics.fill(left + 14, top + 14, left + W - 14, top + 76, sys30 ? 0xFF142420 : (isSys20 ? 0xFF28103F : 0xFF17354B));
        graphics.fill(left + 14, top + 74, left + W - 14, top + 76, GOLD);

        graphics.drawCenteredString(font, definition.name().copy().withStyle(ChatFormatting.BOLD), width / 2, top + 22, GOLD);

        boolean isActive = SkillDefinitions.isActive(skillId);
        Component typeTag = (isActive
                ? Component.translatable("awakening_system.skill.active_tag")
                : Component.translatable("awakening_system.skill.passive_tag"))
                .copy().withStyle(isActive ? ((sys30 || isSys20) ? ChatFormatting.GOLD : ChatFormatting.AQUA) : ChatFormatting.GREEN);
        Component levelComp = Component.translatable("awakening_system.skill.level_separator", skillLevel).withStyle(ChatFormatting.WHITE);
        Component rarityComp = Component.translatable("awakening_system.rarity." + definition.rarity().id());
        Component subHeader = typeTag.copy().append(levelComp).append(rarityComp);
        graphics.drawCenteredString(font, subHeader, width / 2, top + 38, isActive ? ((sys30 || isSys20) ? 0xFFFFD700 : CYAN) : 0xFF50FA7B);

        graphics.drawCenteredString(font, Component.translatable("awakening_system.skill.mastery", progress, progressRequired), width / 2, top + 54, sys30 ? 0xFF88C999 : (isSys20 ? 0xFFD1B3E0 : MUTED));

        if (isActive) {
            int btnW = 120;
            int btnH = 20;

            boolean isEquipped = skillId.equals(currentData.equippedSkill());
            int eqX = left + (W / 2) - btnW - 6;
            int eqY = top + 84;
            boolean eqHovered = mX >= eqX && mX <= eqX + btnW && mY >= eqY && mY <= eqY + btnH;

            graphics.fill(eqX, eqY, eqX + btnW, eqY + btnH, isEquipped ? 0xFF163E2A : (eqHovered ? (sys30 ? 0xFF2ECC71 : (isSys20 ? 0xFF9D4EDD : 0xFF70E8FF)) : (sys30 ? 0xFF163E2A : (isSys20 ? 0xFF3A1259 : 0xFF276A82))));
            drawBorder(graphics, eqX, eqY, btnW, btnH, isEquipped ? 0xFF50FA7B : (eqHovered ? 0xFFFFFFFF : ((sys30 || isSys20) ? 0xFFFFD700 : 0xFF50B4D8)));
            Component eqText = isEquipped
                    ? Component.translatable("awakening_system.skill.equipped_v")
                    : Component.translatable("awakening_system.skill.equip_v");
            graphics.drawCenteredString(font, eqText, eqX + btnW / 2, eqY + 6, isEquipped ? 0xFF50FA7B : (eqHovered ? 0xFF10202D : (sys30 ? 0xFFFFD700 : 0xFFFFFFFF)));

            long curGameTime = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
            int activeCd = currentData.activeCooldownSeconds(skillId, curGameTime);
            boolean canActivate = activeCd <= 0 && currentData.energy() >= SystemData.ACTIVE_SKILL_COST;
            int actX = left + (W / 2) + 6;
            int actY = top + 84;
            boolean actHovered = mX >= actX && mX <= actX + btnW && mY >= actY && mY <= actY + btnH;

            graphics.fill(actX, actY, actX + btnW, actY + btnH, activeCd > 0 ? (sys30 ? 0xFF0E261E : (isSys20 ? 0xFF28103F : 0xFF1C3243)) : (actHovered && canActivate ? 0xFF50FA7B : (canActivate ? (sys30 ? 0xFF163E2A : (isSys20 ? 0xFF5B1B7D : 0xFF1D5A40)) : (sys30 ? 0xFF0E261E : (isSys20 ? 0xFF28103F : 0xFF1C3243)))));
            drawBorder(graphics, actX, actY, btnW, btnH, activeCd > 0 ? (sys30 ? 0x40D4AF37 : (isSys20 ? 0x409D4EDD : 0x402D7691)) : (canActivate ? ((sys30 || isSys20) ? 0xFFFFD700 : 0xFF50FA7B) : (sys30 ? 0x40D4AF37 : (isSys20 ? 0x409D4EDD : 0x402D7691))));
            Component actText = activeCd > 0
                    ? Component.translatable("awakening_system.skill.cooldown", activeCd)
                    : (canActivate ? Component.translatable("awakening_system.skill.activate") : Component.translatable("awakening_system.skill.no_energy"));
            graphics.drawCenteredString(font, actText, actX + btnW / 2, actY + 6, activeCd > 0 ? (sys30 ? 0xFF88C999 : (isSys20 ? 0xFFD1B3E0 : MUTED)) : (actHovered && canActivate ? 0xFF10202D : (canActivate ? ((sys30 || isSys20) ? 0xFFFFD700 : 0xFF50FA7B) : (sys30 ? 0xFF88C999 : (isSys20 ? 0xFFD1B3E0 : MUTED)))));
        } else {
            int passW = 230;
            int passH = 20;
            int passX = left + (W - passW) / 2;
            int passY = top + 84;
            graphics.fill(passX, passY, passX + passW, passY + passH, sys30 ? 0xFF142420 : (isSys20 ? 0xFF2D1244 : 0xFF0F2B1D));
            drawBorder(graphics, passX, passY, passW, passH, (sys30 || isSys20) ? 0xFFFFD700 : 0xFF50FA7B);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill.passive_blessing"), passX + passW / 2, passY + 6, (sys30 || isSys20) ? 0xFFFFD700 : 0xFF50FA7B);
        }

        int y = top + 112;
        y = drawTextBlock(graphics, Component.translatable("awakening_system.skill.description"), description, contentX, y, sys30 ? 0xFFE0F2E9 : (isSys20 ? 0xFFEDE2FF : 0xFFE9F2F6), sys30 || isSys20);
        y += 8;
        drawTextBlock(graphics, Component.translatable("awakening_system.skill.effect"), effect, contentX, y, GOLD, sys30 || isSys20);

        if (skillLevel == 5) {
            String affinity = SkillDefinitions.affinity(skillId);
            long gameTime = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
            int cooldown = currentData.evolutionCooldownSeconds(skillId, gameTime);
            boolean eligible = cooldown <= 0 && currentData.affinity(affinity) >= 250 && currentData.attributePoints() >= 3;

            graphics.drawCenteredString(font, Component.translatable("awakening_system.skill.evolution_requirement",
                    currentData.affinity(affinity), currentData.attributePoints()), width / 2, top + H - 54, sys30 ? 0xFF88C999 : (isSys20 ? 0xFFD1B3E0 : MUTED));

            int evY = top + H - 42;
            boolean evHovered = mX >= left + 40 && mX <= left + W - 40 && mY >= evY && mY <= evY + 16;
            if (eligible) {
                graphics.fill(left + 60, evY, left + W - 60, evY + 16, evHovered ? 0xFFFFD866 : 0xFFB8860B);
                drawBorder(graphics, left + 60, evY, W - 120, 16, evHovered ? 0xFFFFFFFF : 0xFFFFD866);
                graphics.drawCenteredString(font, Component.translatable("awakening_system.skill.evolve"), width / 2, evY + 4, evHovered ? 0xFF10202D : 0xFFFFFFFF);
            } else {
                graphics.drawCenteredString(font, cooldown > 0
                        ? Component.translatable("awakening_system.skill.evolution_cooldown", cooldown)
                        : Component.translatable("awakening_system.skill.evolve_locked"),
                        width / 2, evY + 4, sys30 ? 0xFF88C999 : (isSys20 ? 0xFFD1B3E0 : MUTED));
            }
        }

        graphics.drawCenteredString(font, Component.translatable("awakening_system.skill.back"), width / 2, top + H - 18, sys30 ? 0xFF88C999 : (isSys20 ? 0xFFD1B3E0 : MUTED));

        graphics.pose().popPose();
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private net.minecraft.resources.ResourceLocation skillTexture() {
        String base = skillId.endsWith("_evolution") ? skillId.substring(0, skillId.length() - 11) : skillId;
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID,
                "textures/skill_" + base + ".png");
    }

    private int drawTextBlock(GuiGraphics graphics, Component title, List<net.minecraft.util.FormattedCharSequence> lines,
                              int x, int y, int textColor, boolean isSys20) {
        graphics.fill(x, y, x + 4, y + 14, isSys20 ? 0xFFFFD700 : CYAN);
        graphics.drawString(font, title, x + 10, y + 3, isSys20 ? 0xFFFFD700 : CYAN);
        int lineY = y + 20;
        for (net.minecraft.util.FormattedCharSequence line : lines) {
            graphics.drawString(font, line, x + 2, lineY, textColor);
            lineY += 11;
        }
        return lineY;
    }
}
