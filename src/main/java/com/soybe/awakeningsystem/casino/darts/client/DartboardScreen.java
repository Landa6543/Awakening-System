package com.soybe.awakeningsystem.casino.darts.client;

import com.soybe.awakeningsystem.casino.darts.network.DartboardActionC2SPayload;
import com.soybe.awakeningsystem.casino.darts.network.OpenDartboardPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

public class DartboardScreen extends Screen {

    private final BlockPos boardPos;
    private final long playerChips;
    private final int pvpState;
    private final String challengerName;
    private final int challengerBet;
    private final int pot;
    private final String statusMessage;

    private int selectedMode = 0;

    private static final int[] BET_SOLO_OPTIONS = {0, 5000, 25000, 100000, 500000, 1000000};
    private static final int[] BET_PVP_OPTIONS  = {5000, 25000, 100000, 500000, 1000000};
    private int selectedSoloBetIdx = 1;
    private int selectedPvpBetIdx  = 1;

    public DartboardScreen(OpenDartboardPayload payload) {
        super(Component.translatable("awakening_system.casino.darts.screen_title"));
        this.boardPos = payload.pos();
        this.playerChips = payload.coins();
        this.pvpState = payload.pvpState();
        this.challengerName = payload.challengerName();
        this.challengerBet = payload.challengerBet();
        this.pot = payload.pot();
        this.statusMessage = payload.statusMessage();

        if (this.pvpState != 0) {
            this.selectedMode = 1;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 370.0F;
        float scaleY = (float) this.height / 268.0F;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

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

        int guiW = 350;
        int guiH = 248;
        int x = (this.width - guiW) / 2;
        int y = (this.height - guiH) / 2;

        graphics.fillGradient(x, y, x + guiW, y + guiH, 0xFA12151E, 0xFA0A0C12);
        graphics.renderOutline(x, y, guiW, guiH, 0xFFFFD700);
        graphics.renderOutline(x + 2, y + 2, guiW - 4, guiH - 4, 0x66FFD700);

        Component header = Component.translatable("awakening_system.casino.darts.header");
        graphics.drawCenteredString(this.font, header, x + guiW / 2, y + 10, 0xFFFFFF);
        Component subheader = Component.translatable("awakening_system.casino.darts.subheader");
        graphics.drawCenteredString(this.font, subheader, x + guiW / 2, y + 22, 0xCCCCCC);

        String chipStr = "🪙 " + String.format(Locale.US, "%,d", playerChips);
        int chipW = this.font.width(chipStr) + 8;
        int chipX = x + guiW - 14 - chipW;
        graphics.fill(chipX, y + 8, chipX + chipW, y + 21, 0xDD121620);
        graphics.renderOutline(chipX, y + 8, chipW, 13, 0x66FFD700);
        graphics.drawString(this.font, chipStr, chipX + 4, y + 10, 0xFFFFD700, false);

        int tabW = 150;
        int tabH = 20;
        int tabY = y + 36;
        int tab1X = x + 16;
        int tab2X = x + guiW - 16 - tabW;

        boolean hoverTab1 = mX >= tab1X && mX <= tab1X + tabW && mY >= tabY && mY <= tabY + tabH;
        graphics.fill(tab1X, tabY, tab1X + tabW, tabY + tabH, selectedMode == 0 ? 0xDD2E7D32 : (hoverTab1 ? 0xDD1B5E20 : 0xAA21252B));
        graphics.renderOutline(tab1X, tabY, tabW, tabH, selectedMode == 0 ? 0xFF81C784 : 0x55888888);
        Component tab1Text = Component.literal((selectedMode == 0 ? "§a§l✔ " : "")).append(Component.translatable("awakening_system.casino.darts.tab.solo"));
        graphics.drawCenteredString(this.font, tab1Text, tab1X + tabW / 2, tabY + 6, 0xFFFFFF);

        boolean hoverTab2 = mX >= tab2X && mX <= tab2X + tabW && mY >= tabY && mY <= tabY + tabH;
        graphics.fill(tab2X, tabY, tab2X + tabW, tabY + tabH, selectedMode == 1 ? 0xDDD84315 : (hoverTab2 ? 0xDDBF360C : 0xAA21252B));
        graphics.renderOutline(tab2X, tabY, tabW, tabH, selectedMode == 1 ? 0xFFFF8A65 : 0x55888888);
        Component tab2Text = Component.literal((selectedMode == 1 ? "§6§l✔ " : "")).append(Component.translatable("awakening_system.casino.darts.tab.pvp"));
        graphics.drawCenteredString(this.font, tab2Text, tab2X + tabW / 2, tabY + 6, 0xFFFFFF);

        int contentY = tabY + 28;
        if (selectedMode == 0) {
            renderSoloPanel(graphics, x, contentY, guiW, mX, mY);
        } else {
            renderPvpPanel(graphics, x, contentY, guiW, mX, mY);
        }

        graphics.fill(x + 10, y + guiH - 26, x + guiW - 10, y + guiH - 8, 0xDD181B24);
        graphics.renderOutline(x + 10, y + guiH - 26, guiW - 20, 18, 0x44FFD700);
        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.darts.footer_hint"), x + guiW / 2, y + guiH - 21, 0xFFFFFF);

        graphics.pose().popPose();
    }

    private void renderSoloPanel(GuiGraphics graphics, int x, int y, int guiW, int mouseX, int mouseY) {
        graphics.fill(x + 16, y, x + guiW - 16, y + 42, 0x881E2330);
        graphics.renderOutline(x + 16, y, guiW - 32, 42, 0x4481C784);
        graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.solo.title"), x + 24, y + 6, 0xFFFFFF, true);
        graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.solo.desc1"), x + 24, y + 18, 0xCCCCCC);
        graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.solo.desc2"), x + 24, y + 29, 0xCCCCCC);

        int betY = y + 50;
        graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.solo.bet_title"), x + 16, betY, 0xFFD700, true);

        for (int i = 0; i < BET_SOLO_OPTIONS.length; i++) {
            int bVal = BET_SOLO_OPTIONS[i];
            int btnW = 48;
            int btnH = 18;
            int btnX = x + 16 + i * 51;
            int btnYpos = betY + 12;
            boolean isSelected = (selectedSoloBetIdx == i);
            boolean hover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnYpos && mouseY <= btnYpos + btnH;

            graphics.fill(btnX, btnYpos, btnX + btnW, btnYpos + btnH, isSelected ? 0xFF2E7D32 : (hover ? 0xFF37474F : 0xFF21252B));
            graphics.renderOutline(btnX, btnYpos, btnW, btnH, isSelected ? 0xFF81C784 : 0x55888888);
            String label = (bVal == 0) ? Component.translatable("awakening_system.casino.darts.free").getString() : (bVal >= 1000000 ? "1M" : (bVal >= 1000 ? (bVal / 1000) + "k" : String.valueOf(bVal)));
            graphics.drawCenteredString(this.font, label, btnX + btnW / 2, btnYpos + 5, 0xFFFFFF);
        }

        int actY = betY + 42;
        int actW = guiW - 32;
        int actH = 24;
        int actX = x + 16;
        boolean hoverAct = mouseX >= actX && mouseX <= actX + actW && mouseY >= actY && mouseY <= actY + actH;

        int bet = BET_SOLO_OPTIONS[selectedSoloBetIdx];
        boolean canAfford = playerChips >= bet;

        graphics.fill(actX, actY, actX + actW, actY + actH, !canAfford ? 0xFF424242 : (hoverAct ? 0xFF2E7D32 : 0xFF388E3C));
        graphics.renderOutline(actX, actY, actW, actH, canAfford ? 0xFF81C784 : 0xFF757575);
        Component actText = !canAfford
                ? Component.translatable("awakening_system.casino.insufficient_chips")
                : Component.translatable("awakening_system.casino.darts.solo.start_btn");
        graphics.drawCenteredString(this.font, actText, actX + actW / 2, actY + 8, 0xFFFFFF);
    }

    private void renderPvpPanel(GuiGraphics graphics, int x, int y, int guiW, int mouseX, int mouseY) {
        if (pvpState == 1) {
            graphics.fill(x + 16, y, x + guiW - 16, y + 54, 0xDD3E2723);
            graphics.renderOutline(x + 16, y, guiW - 32, 54, 0xFFFF7043);
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.active_header"), x + 24, y + 6, 0xFFFFFF, true);
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.challenger_label", challengerName), x + 24, y + 18, 0xFFFFFF);
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.bet_label", String.format(Locale.US, "%,d", challengerBet)), x + 24, y + 29, 0xFFFFFF);
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.pot_label", String.format(Locale.US, "%,d", pot)), x + 24, y + 41, 0xFFFFFF);

            boolean isSelf = (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getName().getString().equalsIgnoreCase(challengerName));
            int actY = y + 64;
            int actW = guiW - 32;
            int actH = 24;
            int actX = x + 16;
            boolean hoverAct = mouseX >= actX && mouseX <= actX + actW && mouseY >= actY && mouseY <= actY + actH;

            if (isSelf) {
                graphics.fill(actX, actY, actX + actW, actY + actH, hoverAct ? 0xFFC62828 : 0xFFD32F2F);
                graphics.renderOutline(actX, actY, actW, actH, 0xFFFF5252);
                graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.darts.pvp.cancel_btn"), actX + actW / 2, actY + 8, 0xFFFFFF);
            } else {
                boolean canAfford = playerChips >= challengerBet;
                graphics.fill(actX, actY, actX + actW, actY + actH, !canAfford ? 0xFF424242 : (hoverAct ? 0xFFE65100 : 0xFFF57C00));
                graphics.renderOutline(actX, actY, actW, actH, canAfford ? 0xFFFFB74D : 0xFF757575);
                Component btnText = !canAfford
                        ? Component.translatable("awakening_system.casino.darts.insufficient_chips_join_btn")
                        : Component.translatable("awakening_system.casino.darts.pvp.accept_btn");
                graphics.drawCenteredString(this.font, btnText, actX + actW / 2, actY + 8, 0xFFFFFF);
            }
        } else if (pvpState == 2) {
            graphics.fill(x + 16, y, x + guiW - 16, y + 46, 0xDD263238);
            graphics.renderOutline(x + 16, y, guiW - 32, 46, 0xFF00B0FF);
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.in_progress_header"), x + 24, y + 6, 0xFFFFFF, true);
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.in_progress_turn", statusMessage), x + 24, y + 18, 0xFFFFFF);
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.accumulated_pot", String.format(Locale.US, "%,d", pot)), x + 24, y + 30, 0xFFFFFF);

            int actY = y + 58;
            int actW = guiW - 32;
            int actH = 22;
            int actX = x + 16;
            boolean hoverAct = mouseX >= actX && mouseX <= actX + actW && mouseY >= actY && mouseY <= actY + actH;
            graphics.fill(actX, actY, actX + actW, actY + actH, hoverAct ? 0xFF00838F : 0xFF0097A7);
            graphics.renderOutline(actX, actY, actW, actH, 0xFF80DEEA);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.darts.pvp.close_and_watch"), actX + actW / 2, actY + 7, 0xFFFFFF);
        } else {
            graphics.fill(x + 16, y, x + guiW - 16, y + 42, 0x882A1B14);
            graphics.renderOutline(x + 16, y, guiW - 32, 42, 0x44FF7043);
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.rules_title"), x + 24, y + 6, 0xFFFFFF, true);
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.rules_desc1"), x + 24, y + 18, 0xCCCCCC);
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.rules_desc2"), x + 24, y + 29, 0xCCCCCC);

            int betY = y + 50;
            graphics.drawString(this.font, Component.translatable("awakening_system.casino.darts.pvp.select_bet_title"), x + 16, betY, 0xFFD700, true);

            for (int i = 0; i < BET_PVP_OPTIONS.length; i++) {
                int bVal = BET_PVP_OPTIONS[i];
                int btnW = 58;
                int btnH = 18;
                int btnX = x + 16 + i * 62;
                int btnYpos = betY + 12;
                boolean isSelected = (selectedPvpBetIdx == i);
                boolean hover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnYpos && mouseY <= btnYpos + btnH;

                graphics.fill(btnX, btnYpos, btnX + btnW, btnYpos + btnH, isSelected ? 0xFFD84315 : (hover ? 0xFF4E342E : 0xFF21252B));
                graphics.renderOutline(btnX, btnYpos, btnW, btnH, isSelected ? 0xFFFF8A65 : 0x55888888);
                String label = (bVal >= 1000000 ? "1M 🪙" : (bVal / 1000) + "k 🪙");
                graphics.drawCenteredString(this.font, label, btnX + btnW / 2, btnYpos + 5, 0xFFFFFF);
            }

            int actY = betY + 42;
            int actW = guiW - 32;
            int actH = 24;
            int actX = x + 16;
            boolean hoverAct = mouseX >= actX && mouseX <= actX + actW && mouseY >= actY && mouseY <= actY + actH;

            int bet = BET_PVP_OPTIONS[selectedPvpBetIdx];
            boolean canAfford = playerChips >= bet;

            graphics.fill(actX, actY, actX + actW, actY + actH, !canAfford ? 0xFF424242 : (hoverAct ? 0xFFD84315 : 0xFFE64A19));
            graphics.renderOutline(actX, actY, actW, actH, canAfford ? 0xFFFF8A65 : 0xFF757575);
            Component actText = !canAfford
                    ? Component.translatable("awakening_system.casino.insufficient_chips")
                    : Component.translatable("awakening_system.casino.darts.pvp.publish_btn", String.format(Locale.US, "%,d", bet * 2));
            graphics.drawCenteredString(this.font, actText, actX + actW / 2, actY + 8, 0xFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double mX = transformMouseX(mouseX);
        double mY = transformMouseY(mouseY);
        if (button == 0) {
            int guiW = 350;
            int guiH = 248;
            int x = (this.width - guiW) / 2;
            int y = (this.height - guiH) / 2;

            int tabW = 150;
            int tabH = 20;
            int tabY = y + 36;
            int tab1X = x + 16;
            int tab2X = x + guiW - 16 - tabW;

            if (mX >= tab1X && mX <= tab1X + tabW && mY >= tabY && mY <= tabY + tabH) {
                selectedMode = 0;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (mX >= tab2X && mX <= tab2X + tabW && mY >= tabY && mY <= tabY + tabH) {
                selectedMode = 1;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            int contentY = tabY + 28;
            if (selectedMode == 0) {
                int betY = contentY + 50;
                for (int i = 0; i < BET_SOLO_OPTIONS.length; i++) {
                    int btnW = 48;
                    int btnH = 18;
                    int btnX = x + 16 + i * 51;
                    int btnYpos = betY + 12;
                    if (mX >= btnX && mX <= btnX + btnW && mY >= btnYpos && mY <= btnYpos + btnH) {
                        selectedSoloBetIdx = i;
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                        return true;
                    }
                }

                int actY = betY + 42;
                int actW = guiW - 32;
                int actH = 24;
                int actX = x + 16;
                if (mX >= actX && mX <= actX + actW && mY >= actY && mY <= actY + actH) {
                    int bet = BET_SOLO_OPTIONS[selectedSoloBetIdx];
                    if (playerChips >= bet) {
                        PacketDistributor.sendToServer(new DartboardActionC2SPayload(
                                boardPos, DartboardActionC2SPayload.ACTION_START_SOLO, bet
                        ));
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARROW_SHOOT, 1.2F));
                        this.onClose();
                        return true;
                    }
                }
            } else {
                if (pvpState == 1) {
                    boolean isSelf = (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getName().getString().equalsIgnoreCase(challengerName));
                    int actY = contentY + 64;
                    int actW = guiW - 32;
                    int actH = 24;
                    int actX = x + 16;
                    if (mX >= actX && mX <= actX + actW && mY >= actY && mY <= actY + actH) {
                        if (isSelf) {
                            PacketDistributor.sendToServer(new DartboardActionC2SPayload(
                                    boardPos, DartboardActionC2SPayload.ACTION_CANCEL_PVP, 0
                            ));
                        } else if (playerChips >= challengerBet) {
                            PacketDistributor.sendToServer(new DartboardActionC2SPayload(
                                    boardPos, DartboardActionC2SPayload.ACTION_JOIN_PVP, challengerBet
                            ));
                        }
                        this.onClose();
                        return true;
                    }
                } else if (pvpState == 2) {
                    int actY = contentY + 58;
                    int actW = guiW - 32;
                    int actH = 22;
                    int actX = x + 16;
                    if (mX >= actX && mX <= actX + actW && mY >= actY && mY <= actY + actH) {
                        this.onClose();
                        return true;
                    }
                } else {
                    int betY = contentY + 50;
                    for (int i = 0; i < BET_PVP_OPTIONS.length; i++) {
                        int btnW = 58;
                        int btnH = 18;
                        int btnX = x + 16 + i * 62;
                        int btnYpos = betY + 12;
                        if (mX >= btnX && mX <= btnX + btnW && mY >= btnYpos && mY <= btnYpos + btnH) {
                            selectedPvpBetIdx = i;
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                            return true;
                        }
                    }

                    int actY = betY + 42;
                    int actW = guiW - 32;
                    int actH = 24;
                    int actX = x + 16;
                    if (mX >= actX && mX <= actX + actW && mY >= actY && mY <= actY + actH) {
                        int bet = BET_PVP_OPTIONS[selectedPvpBetIdx];
                        if (playerChips >= bet) {
                            PacketDistributor.sendToServer(new DartboardActionC2SPayload(
                                    boardPos, DartboardActionC2SPayload.ACTION_CREATE_PVP, bet
                            ));
                            this.onClose();
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mX, mY, button);
    }
}
