package com.soybe.awakeningsystem.casino.screen;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.casino.blackjack.Card;
import com.soybe.awakeningsystem.casino.network.BlackjackActionC2SPayload;
import com.soybe.awakeningsystem.casino.network.BlackjackPvPActionC2SPayload;
import com.soybe.awakeningsystem.casino.network.BlackjackPvPStateS2CPayload;
import com.soybe.awakeningsystem.casino.network.BlackjackStateS2CPayload;
import com.soybe.awakeningsystem.casino.network.OpenBlackjackPayload;
import com.soybe.awakeningsystem.item.CasinoChipItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.Locale;

public class BlackjackScreen extends Screen {
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);

    public static final int TAB_SOLO = 0;
    public static final int TAB_PVP = 1;

    private final BlockPos tablePos;
    private long playerChipsTotal;
    private final int[] playerChips;

    private int guiLeft;
    private int guiTop;
    private static final int GUI_WIDTH = 350;
    private static final int GUI_HEIGHT = 244;

    private int activeTab = TAB_SOLO;

    private int stateOrdinal = 0;
    private int resultOrdinal = 0;
    private long currentBet = 1000;
    private long selectedBet = 1000;
    private int selectedChipTier = -1;
    private long payout = 0;
    private int[] playerCards = new int[0];
    private int[] dealerCards = new int[0];
    private boolean hideDealerCard = true;

    private int pvpStateOrdinal = -1;
    private int pvpResultOrdinal = 0;
    private String pvpHostName = "";
    private String pvpOpponentName = "";
    private boolean pvpIsHost = false;
    private boolean pvpIsMyTurn = false;
    private long pvpBetAmount = 50000;
    private long pvpSelectedBet = 50000;
    private long pvpPot = 0;
    private int pvpMyScore = 0;
    private int pvpOppScore = 0;
    private int[] pvpMyCards = new int[0];
    private int[] pvpOppCards = new int[0];
    private String pvpResultMessage = "";

    private float pulseTick = 0;

    private final boolean isIllegal;
    private final boolean isVip;

    public BlackjackScreen(OpenBlackjackPayload payload) {
        super(Component.translatable("awakening_system.casino.blackjack.screen_title"));
        this.tablePos = payload.pos();
        this.isIllegal = payload.isIllegal();
        this.isVip = payload.isVip();
        this.playerChipsTotal = payload.moraBalance();
        this.playerChips = new int[] {
                payload.bronzeChips(),
                payload.silverChips(),
                payload.goldChips(),
                payload.platinumChips(),
                payload.blackChips()
        };
        this.stateOrdinal = 0;
        this.selectedBet = this.isVip
                ? Math.min(50000, Math.max(5000, playerChipsTotal))
                : Math.min(10000, Math.max(1000, playerChipsTotal));
        this.pvpSelectedBet = Math.min(50000, Math.max(10000, playerChipsTotal));
    }

    public void updateState(BlackjackStateS2CPayload payload) {
        this.stateOrdinal = payload.stateOrdinal();
        this.resultOrdinal = payload.resultOrdinal();
        this.currentBet = payload.currentBet();
        this.payout = payload.payout();
        this.playerCards = payload.playerCards();
        this.dealerCards = payload.dealerCards();
        this.hideDealerCard = payload.hideDealerCard();
        this.playerChipsTotal = payload.updatedMora();
    }

    public void updatePvPState(BlackjackPvPStateS2CPayload payload) {
        this.pvpStateOrdinal = payload.stateOrdinal();
        this.pvpResultOrdinal = payload.resultOrdinal();
        this.pvpHostName = payload.hostName();
        this.pvpOpponentName = payload.opponentName();
        this.pvpIsHost = payload.isHost();
        this.pvpIsMyTurn = payload.isMyTurn();
        this.pvpBetAmount = payload.betAmount();
        this.pvpPot = payload.pot();
        this.pvpMyScore = payload.myScore();
        this.pvpOppScore = payload.opponentScore();
        this.pvpMyCards = payload.myCards();
        this.pvpOppCards = payload.opponentCards();
        this.pvpResultMessage = payload.resultMessage();
        this.playerChipsTotal = payload.updatedMora();

        if (payload.stateOrdinal() >= 0) {
            this.activeTab = TAB_PVP;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void renderTransparentBackground(GuiGraphics guiGraphics) {
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 400.0F;
        float scaleY = (float) this.height / 250.0F;
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
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        pulseTick += partialTick;

        g.fillGradient(0, 0, this.width, this.height, 0x66000000, 0x88000000);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            g.pose().translate(cx, cy, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cx, -cy, 0.0F);
        }

        g.fill(guiLeft - 5, guiTop - 5, guiLeft + GUI_WIDTH + 5, guiTop + GUI_HEIGHT + 5, 0xFF3D1F0D);
        g.fill(guiLeft - 3, guiTop - 3, guiLeft + GUI_WIDTH + 3, guiTop + GUI_HEIGHT + 3, 0xFF613317);

        g.fill(guiLeft - 1, guiTop - 1, guiLeft + GUI_WIDTH + 1, guiTop + GUI_HEIGHT + 1, 0xFFD4AF37);

        g.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFF0D4722);
        g.fillGradient(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + 30, 0x33002208, 0x00000000);
        g.fillGradient(guiLeft, guiTop + GUI_HEIGHT - 35, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0x00000000, 0x33002208);

        renderTopTabs(g, mX, mY);
        renderPersonalRecordPill(g);

        String balanceStr = "§6🪙 " + NF.format(playerChipsTotal);
        int balW = this.font.width(balanceStr);
        g.drawString(this.font, balanceStr, guiLeft + GUI_WIDTH - balW - 16, guiTop + 24, 0xFFFFFF);
        drawCloseButton(g, mX, mY);

        if (activeTab == TAB_SOLO) {
            renderSoloMode(g, mX, mY);
        } else {
            renderPvPMode(g, mX, mY);
        }

        super.render(g, mX, mY, partialTick);
        g.pose().popPose();
    }

    private void renderPersonalRecordPill(GuiGraphics g) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        SystemData sData = SystemEvents.data(this.minecraft.player);
        long totalWon = sData.getCasinoTotalWon();
        int maxPay = sData.getCasinoMaxPayout();
        Component recordComp = Component.translatable("awakening_system.casino.blackjack.record_pill", NF.format(totalWon), NF.format(maxPay));
        int textW = this.font.width(recordComp);
        int pillX = guiLeft + (GUI_WIDTH - textW - 12) / 2;
        int pillY = guiTop - 16;
        g.fill(pillX, pillY, pillX + textW + 12, pillY + 13, 0xEE121820);
        g.renderOutline(pillX, pillY, textW + 12, 13, 0x88D4AF37);
        g.drawString(this.font, recordComp, pillX + 6, pillY + 3, 0xFFFFFF);
    }

    private void renderTopTabs(GuiGraphics g, int mouseX, int mouseY) {
        Component title = isVip
                ? Component.translatable("awakening_system.casino.blackjack.title_vip")
                : (isIllegal ? Component.translatable("awakening_system.casino.blackjack.title_illegal") : Component.translatable("awakening_system.casino.blackjack.title_normal"));
        g.drawString(this.font, title, guiLeft + 10, guiTop + 7, isVip ? 0xFFFFD700 : 0xFFFFFFFF);

        int tabW = 58;
        int tabH = 13;
        int tab2W = 66;
        int tab2X = guiLeft + GUI_WIDTH - 24 - tab2W;
        int tab1X = tab2X - 5 - tabW;

        boolean hoverSolo = mouseX >= tab1X && mouseX <= tab1X + tabW && mouseY >= guiTop + 5 && mouseY <= guiTop + 5 + tabH;
        int bgSolo = (activeTab == TAB_SOLO) ? (isVip ? 0xFFC5A059 : 0xFF2E7D32) : (hoverSolo ? 0xFF3E2723 : 0xFF261814);
        g.fill(tab1X, guiTop + 5, tab1X + tabW, guiTop + 5 + tabH, bgSolo);
        g.renderOutline(tab1X, guiTop + 5, tabW, tabH, (activeTab == TAB_SOLO) ? 0xFFFFD700 : 0xFF8D6E63);
        Component soloTab = Component.translatable("awakening_system.casino.blackjack.tab_solo");
        g.drawString(this.font, soloTab, tab1X + (tabW - this.font.width(soloTab)) / 2, guiTop + 8, (activeTab == TAB_SOLO) ? 0xFFFFFFFF : 0xFFAAAAAA);

        boolean hoverPvP = mouseX >= tab2X && mouseX <= tab2X + tab2W && mouseY >= guiTop + 5 && mouseY <= guiTop + 5 + tabH;
        int bgPvP = (activeTab == TAB_PVP) ? 0xFFB71C1C : (hoverPvP ? 0xFF4A148C : 0xFF261814);
        g.fill(tab2X, guiTop + 5, tab2X + tab2W, guiTop + 5 + tabH, bgPvP);
        g.renderOutline(tab2X, guiTop + 5, tab2W, tabH, (activeTab == TAB_PVP) ? 0xFFFFD700 : 0xFF8D6E63);
        Component pvpTabTitle = (pvpStateOrdinal == 0)
                ? Component.translatable("awakening_system.casino.blackjack.tab_pvp_challenge")
                : Component.translatable("awakening_system.casino.blackjack.tab_pvp");
        g.drawString(this.font, pvpTabTitle, tab2X + (tab2W - this.font.width(pvpTabTitle)) / 2, guiTop + 8, (activeTab == TAB_PVP) ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    private void renderSoloMode(GuiGraphics g, int mouseX, int mouseY) {
        renderDealerArea(g);

        g.fill(guiLeft + 15, guiTop + 102, guiLeft + GUI_WIDTH - 15, guiTop + 103, 0x55D4AF37);

        if (stateOrdinal == 0) {
            Component waitMsg = isVip
                    ? Component.translatable("awakening_system.casino.blackjack.wait_vip")
                    : Component.translatable("awakening_system.casino.blackjack.wait_normal");
            g.drawString(this.font, waitMsg, guiLeft + (GUI_WIDTH - this.font.width(waitMsg)) / 2, guiTop + 93, 0xFFFFFF);
        }

        renderPlayerArea(g);

        renderSoloControls(g, mouseX, mouseY);
    }

    private void renderDealerArea(GuiGraphics g) {
        int dealerScore = calculateScore(dealerCards, hideDealerCard);
        Component scoreText = dealerCards.length == 0
                ? Component.translatable("awakening_system.casino.blackjack.waiting")
                : (hideDealerCard ? Component.literal("§7[?]") : Component.literal("§e[" + dealerScore + "]"));
        Component dealerHeader = Component.translatable("awakening_system.casino.blackjack.dealer").append(" ").append(scoreText);
        g.drawString(this.font, dealerHeader, guiLeft + 16, guiTop + 24, 0xFFFFFF);

        int startX = guiLeft + (GUI_WIDTH - (Math.max(2, dealerCards.length) * 32)) / 2;
        int cardY = guiTop + 37;

        if (dealerCards.length == 0) {
            drawEmptyCardSlot(g, startX, cardY);
            drawEmptyCardSlot(g, startX + 32, cardY);
        } else {
            for (int i = 0; i < dealerCards.length; i++) {
                boolean isHidden = (i == 1 && hideDealerCard);
                drawCard(g, startX + (i * 32), cardY, dealerCards[i], isHidden);
            }
        }
    }

    private void renderPlayerArea(GuiGraphics g) {
        int playerScore = calculateScore(playerCards, false);
        String scoreColor = playerScore > 21 ? "§c" : (playerScore == 21 ? "§a" : "§e");
        Component scoreText = playerCards.length == 0
                ? Component.translatable("awakening_system.casino.blackjack.waiting")
                : Component.literal(scoreColor + "[" + playerScore + "]");
        Component playerHeader = Component.translatable("awakening_system.casino.blackjack.your_hand").append(" ").append(scoreText);
        g.drawString(this.font, playerHeader, guiLeft + 16, guiTop + 108, 0xFFFFFF);

        if (stateOrdinal != 0) {
            Component betText = Component.translatable("awakening_system.casino.blackjack.bet_display", NF.format(currentBet));
            g.drawString(this.font, betText, guiLeft + GUI_WIDTH - this.font.width(betText) - 16, guiTop + 108, 0xFFFFFF);
        }

        int startX = guiLeft + (GUI_WIDTH - (Math.max(2, playerCards.length) * 32)) / 2;
        int cardY = guiTop + 120;

        if (playerCards.length == 0) {
            drawEmptyCardSlot(g, startX, cardY);
            drawEmptyCardSlot(g, startX + 32, cardY);
        } else {
            for (int i = 0; i < playerCards.length; i++) {
                drawCard(g, startX + (i * 32), cardY, playerCards[i], false);
            }
        }
    }

    private void renderSoloControls(GuiGraphics g, int mouseX, int mouseY) {
        int btnY = guiTop + 168;

        if (stateOrdinal == 0) {
            renderChipButtons(g, mouseX, mouseY, guiLeft + 22, btnY);

            int mY = btnY + 20;
            int mStartX = guiLeft + 23;
            long[] chipValues = { 1000L, 10000L, 50000L, 100000L, 500000L, 1000000L };
            String[] chipLabels = { "1k", "10k", "50k", "100k", "500k", "1M" };
            for (int i = 0; i < chipValues.length; i++) {
                drawBetButton(g, mouseX, mouseY, mStartX + (i * 44), mY, 40, 16, chipLabels[i], chipValues[i]);
            }

            long allInVal = Math.min(5000000L, Math.max(1000L, playerChipsTotal));
            String allInLabel = Component.translatable("awakening_system.casino.blackjack.all_in").getString();
            drawBetButton(g, mouseX, mouseY, mStartX + (6 * 44), mY, 40, 16, allInLabel, allInVal);

            int actY = btnY + 40;
            Component selDesc = (selectedChipTier >= 0)
                    ? Component.translatable("awakening_system.casino.blackjack.chip_selected",
                            CasinoChipItem.ChipTier.values()[selectedChipTier].getDisplayName(),
                            NF.format(selectedBet))
                    : Component.translatable("awakening_system.casino.blackjack.bet_selected",
                            NF.format(selectedBet));
            g.drawString(this.font, selDesc, guiLeft + 23, actY + 6, 0xFFFFFF);

            drawActionButton(g, mouseX, mouseY, guiLeft + 225, actY, 102, 20, Component.translatable("awakening_system.casino.blackjack.action_bet").getString(), 0xFF2E7D32, 0xFF1B5E20);

        } else if (stateOrdinal == 1) {
            drawActionButton(g, mouseX, mouseY, guiLeft + 45, btnY + 12, 80, 24, Component.translatable("awakening_system.casino.blackjack.action_hit").getString(), 0xFF1E88E5, 0xFF1565C0);
            drawActionButton(g, mouseX, mouseY, guiLeft + 135, btnY + 12, 80, 24, Component.translatable("awakening_system.casino.blackjack.action_stand").getString(), 0xFFD84315, 0xFFBF360C);

            boolean canDouble = (playerCards.length == 2 && playerChipsTotal >= currentBet);
            int doubleColor = canDouble ? 0xFFFFB300 : 0xFF757575;
            int doubleHover = canDouble ? 0xFFFFA000 : 0xFF616161;
            drawActionButton(g, mouseX, mouseY, guiLeft + 225, btnY + 12, 80, 24, Component.translatable("awakening_system.casino.blackjack.action_double").getString(), doubleColor, doubleHover);

        } else if (stateOrdinal == 3) {
            renderResultMessage(g);
            drawActionButton(g, mouseX, mouseY, guiLeft + (GUI_WIDTH - 130) / 2, btnY + 14, 130, 22, Component.translatable("awakening_system.casino.blackjack.action_new_round").getString(), 0xFF2E7D32, 0xFF1B5E20);
        }
    }

    private static int getChipTierColor(int tier) {
        return switch (tier) {
            case 0 -> 0xFFCD7F32;
            case 1 -> 0xFFDCDCDC;
            case 2 -> 0xFFFFD700;
            case 3 -> 0xFF00E5FF;
            default -> 0xFFAB47BC;
        };
    }

    private void renderChipButtons(GuiGraphics g, int mouseX, int mouseY, int startX, int y) {
        String[] labels = {"100", "1k", "10k", "100k", "500k"};
        int countTiers = isIllegal ? 5 : 4;
        int renderStartX = isIllegal ? startX : startX + 31;

        for (int i = 0; i < countTiers; i++) {
            int cx = renderStartX + (i * 62);
            int count = (i < playerChips.length) ? playerChips[i] : 0;
            boolean selected = (selectedChipTier == i);
            boolean hover = mouseX >= cx && mouseX <= cx + 58 && mouseY >= y && mouseY <= y + 17;

            int bg = selected ? 0xFF5D4037 : (hover ? 0xFF3E2723 : 0xFF261814);
            g.fill(cx, y, cx + 58, y + 17, bg);
            int outlineColor = selected ? 0xFFFFD700 : (count > 0 ? getChipTierColor(i) : 0xFF4E342E);
            g.renderOutline(cx, y, 58, 17, outlineColor);

            int dotColor = getChipTierColor(i);
            g.fill(cx + 4, y + 6, cx + 9, y + 11, dotColor);

            String text = labels[i];
            if (count > 0) {
                g.drawString(this.font, text, cx + 11, y + 5, 0xFFFFFFFF);
                String cntStr = "x" + count;
                int cntW = this.font.width(cntStr);
                g.drawString(this.font, cntStr, cx + 58 - cntW - 3, y + 5, 0xFF81C784);
            } else {
                g.drawString(this.font, text, cx + 11, y + 5, 0xFF777777);
            }
        }
    }

    private void renderPvPMode(GuiGraphics g, int mouseX, int mouseY) {
        if (pvpStateOrdinal == -1) {
            renderPvPCreateScreen(g, mouseX, mouseY);
        } else if (pvpStateOrdinal == 0) {
            renderPvPWaitingScreen(g, mouseX, mouseY);
        } else if (pvpStateOrdinal == 1 || pvpStateOrdinal == 2) {
            renderPvPPlayingScreen(g, mouseX, mouseY);
        } else if (pvpStateOrdinal == 3) {
            renderPvPFinishedScreen(g, mouseX, mouseY);
        }
    }

    private void renderPvPCreateScreen(GuiGraphics g, int mouseX, int mouseY) {
        int centerX = guiLeft + GUI_WIDTH / 2;

        Component title = Component.translatable("awakening_system.casino.blackjack.pvp.title");
        g.drawString(this.font, title, centerX - this.font.width(title) / 2, guiTop + 30, 0xFFFFD700);

        Component desc1 = Component.translatable("awakening_system.casino.blackjack.pvp.desc1");
        g.drawString(this.font, desc1, centerX - this.font.width(desc1) / 2, guiTop + 48, 0xFFFFFF);
        Component desc2 = Component.translatable("awakening_system.casino.blackjack.pvp.desc2");
        g.drawString(this.font, desc2, centerX - this.font.width(desc2) / 2, guiTop + 60, 0xFFFFFF);

        Component betTitle = Component.translatable("awakening_system.casino.blackjack.pvp.select_bet");
        g.drawString(this.font, betTitle, guiLeft + 30, guiTop + 85, 0xFFFFFF);

        int bY = guiTop + 102;
        drawPvPBetButton(g, mouseX, mouseY, guiLeft + 30, bY, 46, 18, "10k", 10000);
        drawPvPBetButton(g, mouseX, mouseY, guiLeft + 80, bY, 46, 18, "50k", 50000);
        drawPvPBetButton(g, mouseX, mouseY, guiLeft + 130, bY, 52, 18, "100k", 100000);
        drawPvPBetButton(g, mouseX, mouseY, guiLeft + 186, bY, 52, 18, "500k", 500000);
        drawPvPBetButton(g, mouseX, mouseY, guiLeft + 242, bY, 50, 18, "1M", 1000000);

        Component betDetail = Component.translatable("awakening_system.casino.blackjack.pvp.your_bet", NF.format(pvpSelectedBet));
        g.drawString(this.font, betDetail, centerX - this.font.width(betDetail) / 2, guiTop + 126, 0xFFFFFF);
        Component potDetail = Component.translatable("awakening_system.casino.blackjack.pvp.pot_detail", NF.format(pvpSelectedBet * 2));
        g.drawString(this.font, potDetail, centerX - this.font.width(potDetail) / 2, guiTop + 138, 0xFFFFFF);

        drawActionButton(g, mouseX, mouseY, centerX - 70, guiTop + 160, 140, 24, Component.translatable("awakening_system.casino.blackjack.pvp.btn_create").getString(), 0xFF2E7D32, 0xFF1B5E20);
    }

    private void renderPvPWaitingScreen(GuiGraphics g, int mouseX, int mouseY) {
        int centerX = guiLeft + GUI_WIDTH / 2;

        if (pvpIsHost) {
            Component title = Component.translatable("awakening_system.casino.blackjack.pvp.created_title");
            g.drawString(this.font, title, centerX - this.font.width(title) / 2, guiTop + 35, 0xFFFFD700);

            float alpha = 0.6F + 0.4F * Mth.sin(pulseTick * 0.15F);
            int pulseColor = ((int)(alpha * 255) << 24) | 0x00E0E0E0;
            Component waitText = Component.translatable("awakening_system.casino.blackjack.pvp.searching_challenger");
            g.drawString(this.font, waitText, centerX - this.font.width(waitText) / 2, guiTop + 58, pulseColor);

            Component infoBet = Component.translatable("awakening_system.casino.blackjack.pvp.fixed_bet", NF.format(pvpBetAmount));
            g.drawString(this.font, infoBet, centerX - this.font.width(infoBet) / 2, guiTop + 85, 0xFFFFFF);

            Component infoPot = Component.translatable("awakening_system.casino.blackjack.pvp.pot_info", NF.format(pvpBetAmount * 2));
            g.drawString(this.font, infoPot, centerX - this.font.width(infoPot) / 2, guiTop + 100, 0xFFFFFF);

            Component cancelHint = Component.translatable("awakening_system.casino.blackjack.pvp.cancel_hint");
            g.drawString(this.font, cancelHint, centerX - this.font.width(cancelHint) / 2, guiTop + 130, 0xFFFFFF);

            drawActionButton(g, mouseX, mouseY, centerX - 65, guiTop + 155, 130, 24, Component.translatable("awakening_system.casino.blackjack.pvp.btn_cancel").getString(), 0xFFC62828, 0xFFB71C1C);

        } else {
            Component title = Component.translatable("awakening_system.casino.blackjack.pvp.available_title");
            g.drawString(this.font, title, centerX - this.font.width(title) / 2, guiTop + 35, 0xFFFFD700);

            Component challengerText = Component.translatable("awakening_system.casino.blackjack.pvp.challenger", pvpHostName);
            g.drawString(this.font, challengerText, centerX - this.font.width(challengerText) / 2, guiTop + 60, 0xFFFFFF);

            Component infoBet = Component.translatable("awakening_system.casino.blackjack.pvp.accept_bet", NF.format(pvpBetAmount));
            g.drawString(this.font, infoBet, centerX - this.font.width(infoBet) / 2, guiTop + 85, 0xFFFFFF);

            Component infoPot = Component.translatable("awakening_system.casino.blackjack.pvp.pot_info", NF.format(pvpBetAmount * 2));
            g.drawString(this.font, infoPot, centerX - this.font.width(infoPot) / 2, guiTop + 100, 0xFFFFFF);

            boolean canAfford = playerChipsTotal >= pvpBetAmount;
            int joinBg = canAfford ? 0xFF2E7D32 : 0xFF616161;
            int joinHover = canAfford ? 0xFF1B5E20 : 0xFF616161;
            String joinBtnText = canAfford
                    ? Component.translatable("awakening_system.casino.blackjack.pvp.btn_accept").getString()
                    : Component.translatable("awakening_system.casino.insufficient_chips").getString();
            drawActionButton(g, mouseX, mouseY, centerX - 75, guiTop + 155, 150, 24, joinBtnText, joinBg, joinHover);
        }
    }

    private void renderPvPPlayingScreen(GuiGraphics g, int mouseX, int mouseY) {
        String oppDisplayName = pvpIsHost ? pvpOpponentName : pvpHostName;
        boolean isOppTurn = !pvpIsMyTurn;

        Component oppTurnBadge = isOppTurn
                ? Component.translatable("awakening_system.casino.blackjack.pvp.status_playing")
                : Component.translatable("awakening_system.casino.blackjack.waiting");
        Component oppHeader = Component.translatable("awakening_system.casino.blackjack.pvp.rival_header", oppDisplayName, pvpOppScore).append(" ").append(oppTurnBadge);
        g.drawString(this.font, oppHeader, guiLeft + 16, guiTop + 24, 0xFFFFFF);

        int startOppX = guiLeft + (GUI_WIDTH - (Math.max(2, pvpOppCards.length) * 32)) / 2;
        int cardOppY = guiTop + 37;
        for (int i = 0; i < pvpOppCards.length; i++) {
            drawCard(g, startOppX + (i * 32), cardOppY, pvpOppCards[i], false);
        }

        g.fill(guiLeft + 15, guiTop + 102, guiLeft + GUI_WIDTH - 15, guiTop + 103, 0x55D4AF37);
        Component potStr = Component.translatable("awakening_system.casino.blackjack.pvp.pot_bar", NF.format(pvpPot));
        g.drawString(this.font, potStr, guiLeft + (GUI_WIDTH - this.font.width(potStr)) / 2, guiTop + 93, 0xFFFFFF);

        String myScoreColor = pvpMyScore > 21 ? "§c" : (pvpMyScore == 21 ? "§a" : "§e");
        Component myTurnBadge = pvpIsMyTurn ? Component.translatable("awakening_system.casino.blackjack.pvp.your_turn") : Component.empty();
        Component myHeader = Component.translatable("awakening_system.casino.blackjack.your_hand").append(" ").append(Component.literal(myScoreColor + "[" + pvpMyScore + "] ")).append(myTurnBadge);
        g.drawString(this.font, myHeader, guiLeft + 16, guiTop + 108, 0xFFFFFF);

        int startMyX = guiLeft + (GUI_WIDTH - (Math.max(2, pvpMyCards.length) * 32)) / 2;
        int cardMyY = guiTop + 120;
        for (int i = 0; i < pvpMyCards.length; i++) {
            drawCard(g, startMyX + (i * 32), cardMyY, pvpMyCards[i], false);
        }

        int btnY = guiTop + 175;
        if (pvpIsMyTurn) {
            drawActionButton(g, mouseX, mouseY, guiLeft + 75, btnY, 90, 24, Component.translatable("awakening_system.casino.blackjack.action_hit").getString(), 0xFF1E88E5, 0xFF1565C0);
            drawActionButton(g, mouseX, mouseY, guiLeft + 185, btnY, 90, 24, Component.translatable("awakening_system.casino.blackjack.action_stand").getString(), 0xFFD84315, 0xFFBF360C);
        } else {
            float alpha = 0.7F + 0.3F * Mth.sin(pulseTick * 0.15F);
            int pulseColor = ((int)(alpha * 255) << 24) | 0x00FFD700;
            Component waitTurn = Component.translatable("awakening_system.casino.blackjack.pvp.waiting_rival", oppDisplayName);
            g.drawString(this.font, waitTurn, guiLeft + (GUI_WIDTH - this.font.width(waitTurn)) / 2, btnY + 7, pulseColor);
        }
    }

    private void renderPvPFinishedScreen(GuiGraphics g, int mouseX, int mouseY) {
        String oppDisplayName = pvpIsHost ? pvpOpponentName : pvpHostName;

        Component oppHeader = Component.translatable("awakening_system.casino.blackjack.pvp.rival_finished", oppDisplayName, pvpOppScore);
        g.drawString(this.font, oppHeader, guiLeft + 16, guiTop + 24, 0xFFFFFF);
        int startOppX = guiLeft + (GUI_WIDTH - (Math.max(2, pvpOppCards.length) * 32)) / 2;
        for (int i = 0; i < pvpOppCards.length; i++) {
            drawCard(g, startOppX + (i * 32), guiTop + 37, pvpOppCards[i], false);
        }

        g.fill(guiLeft + 15, guiTop + 102, guiLeft + GUI_WIDTH - 15, guiTop + 103, 0x55D4AF37);

        String myScoreColor = pvpMyScore > 21 ? "§c" : "§e";
        Component myHeader = Component.translatable("awakening_system.casino.blackjack.your_hand").append(" ").append(Component.literal(myScoreColor + "[" + pvpMyScore + "]"));
        g.drawString(this.font, myHeader, guiLeft + 16, guiTop + 108, 0xFFFFFF);
        int startMyX = guiLeft + (GUI_WIDTH - (Math.max(2, pvpMyCards.length) * 32)) / 2;
        for (int i = 0; i < pvpMyCards.length; i++) {
            drawCard(g, startMyX + (i * 32), guiTop + 120, pvpMyCards[i], false);
        }

        Component bannerMsg;
        int bannerColor;
        boolean iWon = (pvpIsHost && pvpResultOrdinal == 1) || (!pvpIsHost && pvpResultOrdinal == 2);
        boolean tied = (pvpResultOrdinal == 3);

        if (iWon) {
            bannerMsg = Component.translatable("awakening_system.casino.blackjack.pvp.banner_won", NF.format(pvpPot));
            bannerColor = 0xFFFFD700;
        } else if (tied) {
            bannerMsg = Component.translatable("awakening_system.casino.blackjack.pvp.banner_tie");
            bannerColor = 0xFFFFEB3B;
        } else {
            bannerMsg = Component.translatable("awakening_system.casino.blackjack.pvp.banner_lost");
            bannerColor = 0xFFEF5350;
        }

        int bW = this.font.width(bannerMsg);
        int bX = guiLeft + (GUI_WIDTH - bW) / 2;
        g.fill(bX - 6, guiTop + 93, bX + bW + 6, guiTop + 105, 0xDD000000);
        g.drawString(this.font, bannerMsg, bX, guiTop + 95, bannerColor);

        drawActionButton(g, mouseX, mouseY, guiLeft + (GUI_WIDTH - 140) / 2, guiTop + 175, 140, 24, Component.translatable("awakening_system.casino.blackjack.pvp.btn_new_or_exit").getString(), 0xFF2E7D32, 0xFF1B5E20);
    }

    private void drawEmptyCardSlot(GuiGraphics g, int x, int y) {
        int w = 28;
        int h = 40;
        g.fill(x, y, x + w, y + h, 0x33000000);
        g.renderOutline(x, y, w, h, 0x44D4AF37);
    }

    private void drawCard(GuiGraphics g, int x, int y, int cardId, boolean isHidden) {
        int w = 28;
        int h = 40;

        if (isHidden) {
            g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF1C2237);
            g.fill(x, y, x + w, y + h, 0xFF2A3454);
            g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xFF1E263D);
            g.drawString(this.font, "★", x + 10, y + 16, 0xFFFFD700);
            return;
        }

        Card card = Card.fromId(cardId);
        int textColor = card.suit().isRed() ? 0xFFD32F2F : 0xFF212121;

        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF424242);
        g.fill(x, y, x + w, y + h, 0xFFFAFAFA);

        g.drawString(this.font, card.rank().getDisplay(), x + 3, y + 3, textColor);
        g.drawString(this.font, card.suit().getSymbol(), x + 10, y + 15, textColor);
        int rankW = this.font.width(card.rank().getDisplay());
        g.drawString(this.font, card.rank().getDisplay(), x + w - rankW - 3, y + h - 11, textColor);
    }

    private void renderResultMessage(GuiGraphics g) {
        Component msg;
        int color;

        switch (resultOrdinal) {
            case 1 -> { msg = Component.translatable("awakening_system.casino.blackjack.result_natural", NF.format(payout)); color = 0xFFFFD700; }
            case 2 -> { msg = Component.translatable("awakening_system.casino.blackjack.result_win", NF.format(payout)); color = 0xFF4CAF50; }
            case 3 -> { msg = Component.translatable("awakening_system.casino.blackjack.result_dealer_wins"); color = 0xFFEF5350; }
            case 4 -> { msg = Component.translatable("awakening_system.casino.blackjack.result_push"); color = 0xFFFFEB3B; }
            case 5 -> { msg = Component.translatable("awakening_system.casino.blackjack.result_bust"); color = 0xFFE53935; }
            case 6 -> { msg = Component.translatable("awakening_system.casino.blackjack.result_dealer_bust", NF.format(payout)); color = 0xFF4CAF50; }
            default -> { msg = Component.empty(); color = 0xFFFFFF; }
        }

        if (!msg.getString().isEmpty()) {
            int msgW = this.font.width(msg);
            int msgX = guiLeft + (GUI_WIDTH - msgW) / 2;
            int msgY = guiTop + 93;
            g.fill(msgX - 6, msgY - 3, msgX + msgW + 6, msgY + 11, 0xCC000000);
            g.drawString(this.font, msg, msgX, msgY, color);
        }
    }

    private void drawBetButton(GuiGraphics g, int mouseX, int mouseY, int x, int y, int w, int h, String text, long value) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        boolean selected = (selectedChipTier == -1 && selectedBet == value);

        int bgColor = selected ? 0xFFD4AF37 : (hover ? 0xFF4E342E : 0xFF3E2723);
        int textColor = selected ? 0xFF000000 : 0xFFFFFFFF;

        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, selected ? 0xFFFFF8E1 : 0xFF8D6E63);
        g.fill(x, y, x + w, y + h, bgColor);
        g.drawString(this.font, text, x + (w - this.font.width(text)) / 2, y + (h - 8) / 2, textColor);
    }

    private void drawPvPBetButton(GuiGraphics g, int mouseX, int mouseY, int x, int y, int w, int h, String text, long value) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        boolean selected = (pvpSelectedBet == value);

        int bgColor = selected ? 0xFFD4AF37 : (hover ? 0xFF4E342E : 0xFF3E2723);
        int textColor = selected ? 0xFF000000 : 0xFFFFFFFF;

        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, selected ? 0xFFFFF8E1 : 0xFF8D6E63);
        g.fill(x, y, x + w, y + h, bgColor);
        g.drawString(this.font, text, x + (w - this.font.width(text)) / 2, y + (h - 8) / 2, textColor);
    }

    private void drawActionButton(GuiGraphics g, int mouseX, int mouseY, int x, int y, int w, int h, String text, int color, int hoverColor) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = hover ? hoverColor : color;

        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFFE0E0E0);
        g.fill(x, y, x + w, y + h, bg);
        g.drawString(this.font, text, x + (w - this.font.width(text)) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    private void drawCloseButton(GuiGraphics g, int mouseX, int mouseY) {
        int x = guiLeft + GUI_WIDTH - 18;
        int y = guiTop + 6;
        boolean hover = mouseX >= x && mouseX <= x + 12 && mouseY >= y && mouseY <= y + 12;
        g.drawString(this.font, "§c✕", x, y, hover ? 0xFFFF5555 : 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int closeX = guiLeft + GUI_WIDTH - 18;
        int closeY = guiTop + 6;
        if (mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= closeY && mouseY <= closeY + 12) {
            this.onClose();
            return true;
        }

        int tabW = 58;
        int tabH = 13;
        int tab2W = 66;
        int tab2X = guiLeft + GUI_WIDTH - 24 - tab2W;
        int tab1X = tab2X - 5 - tabW;

        if (mouseIn(mouseX, mouseY, tab1X, guiTop + 5, tabW, tabH)) {
            this.activeTab = TAB_SOLO;
            return true;
        }

        if (mouseIn(mouseX, mouseY, tab2X, guiTop + 5, tab2W, tabH)) {
            this.activeTab = TAB_PVP;
            return true;
        }

        if (activeTab == TAB_SOLO) {
            handleSoloClick(mouseX, mouseY);
        } else {
            handlePvPClick(mouseX, mouseY);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleSoloClick(double mouseX, double mouseY) {
        int btnY = guiTop + 168;

        if (stateOrdinal == 0) {
            int countTiers = isIllegal ? 5 : 4;
            int startX = isIllegal ? guiLeft + 22 : guiLeft + 53;
            for (int i = 0; i < countTiers; i++) {
                int cx = startX + (i * 62);
                if (mouseIn(mouseX, mouseY, cx, btnY, 58, 17)) {
                    selectedChipTier = i;
                    selectedBet = CasinoChipItem.ChipTier.values()[i].getMoraValue();
                    return;
                }
            }

            int mY = btnY + 20;
            int mStartX = guiLeft + 23;
            long[] chipValues = { 1000L, 10000L, 50000L, 100000L, 500000L, 1000000L };
            for (int i = 0; i < chipValues.length; i++) {
                if (mouseIn(mouseX, mouseY, mStartX + (i * 44), mY, 40, 16)) {
                    selectedChipTier = -1;
                    selectedBet = chipValues[i];
                    return;
                }
            }
            if (mouseIn(mouseX, mouseY, mStartX + (6 * 44), mY, 40, 16)) {
                selectedChipTier = -1;
                selectedBet = Math.min(5000000L, Math.max(1000L, playerChipsTotal));
                return;
            }

            int actY = btnY + 40;
            if (mouseIn(mouseX, mouseY, guiLeft + 225, actY, 102, 20)) {
                PacketDistributor.sendToServer(new BlackjackActionC2SPayload(
                        tablePos, BlackjackActionC2SPayload.ACTION_BET, selectedBet, selectedChipTier
                ));
            }

        } else if (stateOrdinal == 1) {
            if (mouseIn(mouseX, mouseY, guiLeft + 45, btnY + 12, 80, 24)) {
                PacketDistributor.sendToServer(new BlackjackActionC2SPayload(tablePos, BlackjackActionC2SPayload.ACTION_HIT, 0, -1));
            } else if (mouseIn(mouseX, mouseY, guiLeft + 135, btnY + 12, 80, 24)) {
                PacketDistributor.sendToServer(new BlackjackActionC2SPayload(tablePos, BlackjackActionC2SPayload.ACTION_STAND, 0, -1));
            } else if (mouseIn(mouseX, mouseY, guiLeft + 225, btnY + 12, 80, 24)) {
                if (playerCards.length == 2 && playerChipsTotal >= currentBet) {
                    PacketDistributor.sendToServer(new BlackjackActionC2SPayload(tablePos, BlackjackActionC2SPayload.ACTION_DOUBLE, 0, -1));
                }
            }

        } else if (stateOrdinal == 3) {
            if (mouseIn(mouseX, mouseY, guiLeft + (GUI_WIDTH - 130) / 2, btnY + 14, 130, 22)) {
                PacketDistributor.sendToServer(new BlackjackActionC2SPayload(tablePos, BlackjackActionC2SPayload.ACTION_NEW_ROUND, 0, -1));
                this.stateOrdinal = 0;
                this.resultOrdinal = 0;
                this.playerCards = new int[0];
                this.dealerCards = new int[0];
            }
        }
    }

    private void handlePvPClick(double mouseX, double mouseY) {
        int centerX = guiLeft + GUI_WIDTH / 2;

        if (pvpStateOrdinal == -1) {
            int bY = guiTop + 102;
            if (mouseIn(mouseX, mouseY, guiLeft + 30, bY, 46, 18)) pvpSelectedBet = 10000;
            else if (mouseIn(mouseX, mouseY, guiLeft + 80, bY, 46, 18)) pvpSelectedBet = 50000;
            else if (mouseIn(mouseX, mouseY, guiLeft + 130, bY, 52, 18)) pvpSelectedBet = 100000;
            else if (mouseIn(mouseX, mouseY, guiLeft + 186, bY, 52, 18)) pvpSelectedBet = 500000;
            else if (mouseIn(mouseX, mouseY, guiLeft + 242, bY, 50, 18)) pvpSelectedBet = 1000000;
            else if (mouseIn(mouseX, mouseY, centerX - 70, guiTop + 160, 140, 24)) {
                PacketDistributor.sendToServer(new BlackjackPvPActionC2SPayload(
                        tablePos, BlackjackPvPActionC2SPayload.ACTION_CREATE, pvpSelectedBet
                ));
            }

        } else if (pvpStateOrdinal == 0) {
            if (pvpIsHost) {
                if (mouseIn(mouseX, mouseY, centerX - 65, guiTop + 155, 130, 24)) {
                    PacketDistributor.sendToServer(new BlackjackPvPActionC2SPayload(
                            tablePos, BlackjackPvPActionC2SPayload.ACTION_CANCEL, 0
                    ));
                }
            } else {
                if (mouseIn(mouseX, mouseY, centerX - 75, guiTop + 155, 150, 24) && playerChipsTotal >= pvpBetAmount) {
                    PacketDistributor.sendToServer(new BlackjackPvPActionC2SPayload(
                            tablePos, BlackjackPvPActionC2SPayload.ACTION_JOIN, 0
                    ));
                }
            }

        } else if (pvpStateOrdinal == 1 || pvpStateOrdinal == 2) {
            if (pvpIsMyTurn) {
                int btnY = guiTop + 175;
                if (mouseIn(mouseX, mouseY, guiLeft + 75, btnY, 90, 24)) {
                    PacketDistributor.sendToServer(new BlackjackPvPActionC2SPayload(
                            tablePos, BlackjackPvPActionC2SPayload.ACTION_HIT, 0
                    ));
                } else if (mouseIn(mouseX, mouseY, guiLeft + 185, btnY, 90, 24)) {
                    PacketDistributor.sendToServer(new BlackjackPvPActionC2SPayload(
                            tablePos, BlackjackPvPActionC2SPayload.ACTION_STAND, 0
                    ));
                }
            }

        } else if (pvpStateOrdinal == 3) {
            if (mouseIn(mouseX, mouseY, guiLeft + (GUI_WIDTH - 140) / 2, guiTop + 175, 140, 24)) {
                this.pvpStateOrdinal = -1;
                this.pvpResultOrdinal = 0;
                this.pvpMyCards = new int[0];
                this.pvpOppCards = new int[0];
            }
        }
    }

    private boolean mouseIn(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private int calculateScore(int[] cardIds, boolean hideSecond) {
        int score = 0;
        int aceCount = 0;
        int count = hideSecond && cardIds.length > 1 ? 1 : cardIds.length;

        for (int i = 0; i < count; i++) {
            Card card = Card.fromId(cardIds[i]);
            score += card.rank().getValue();
            if (card.rank() == Card.Rank.ACE) {
                aceCount++;
            }
        }

        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }

        return score;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
