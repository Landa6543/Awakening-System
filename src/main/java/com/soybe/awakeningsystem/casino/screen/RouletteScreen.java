package com.soybe.awakeningsystem.casino.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.casino.network.OpenRoulettePayload;
import com.soybe.awakeningsystem.casino.network.RouletteResultS2CPayload;
import com.soybe.awakeningsystem.casino.network.RouletteSpinC2SPayload;
import com.soybe.awakeningsystem.casino.roulette.RouletteBetEntry;
import com.soybe.awakeningsystem.casino.roulette.RouletteBetType;
import com.soybe.awakeningsystem.casino.roulette.RouletteWheel;
import com.soybe.awakeningsystem.item.CasinoChipItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class RouletteScreen extends Screen {
    private final BlockPos pos;
    private long currentMora;
    private final int[] chipCounts = new int[5];

    private long selectedBetAmount = 500L;
    private int selectedChipTier = -1;

    private final List<RouletteBetEntry> currentBets = new ArrayList<>();
    private static final List<RouletteBetEntry> LAST_BETS = new ArrayList<>();

    public static final float TOTAL_SPIN_DURATION = 7.0F;
    private final boolean isIllegal;
    private final boolean isVip;

    private boolean isSpinning = false;
    private float spinTimer = 0.0F;
    private long lastFrameTime = 0L;
    private float clickTimer = 0.0F;
    private float wheelAngle = 0.0F;
    private float ballAngle = 0.0F;

    private float startWheelAngle = 0.0F;
    private float targetWheelDelta = 1440.0F;
    private float startBallAngle = 0.0F;
    private float targetBallDelta = -2880.0F;

    private int targetWinningNumber = -1;
    private long lastPayout = 0L;
    private long lastTotalBet = 0L;
    private float resultTimer = 0.0F;

    private static final List<Integer> RECENT_NUMBERS = new ArrayList<>();

    private int panelX, panelY;
    private final int panelWidth = 460;
    private final int panelHeight = 252;

    public RouletteScreen(OpenRoulettePayload payload) {
        super(Component.translatable("awakening_system.roulette.title"));
        this.pos = payload.pos();
        this.isIllegal = payload.isIllegal();
        this.isVip = payload.isVip();
        this.currentMora = payload.moraBalance();
        this.chipCounts[0] = payload.bronzeChips();
        this.chipCounts[1] = payload.silverChips();
        this.chipCounts[2] = payload.goldChips();
        this.chipCounts[3] = payload.platinumChips();
        this.chipCounts[4] = payload.blackChips();

        if (payload.isSpinning()) {
            this.isSpinning = true;
            this.targetWinningNumber = payload.winningNumber();
            this.lastPayout = payload.currentPayout();

            // Sincronizar suavemente con la ruleta física del mundo si el cliente ya la tiene cargada
            if (net.minecraft.client.Minecraft.getInstance().level != null &&
                    net.minecraft.client.Minecraft.getInstance().level.getBlockEntity(this.pos) instanceof com.soybe.awakeningsystem.block.entity.RouletteTableBlockEntity tableBe) {
                this.wheelAngle = tableBe.getInterpolatedWheelAngle(1.0F);
                this.ballAngle = tableBe.getInterpolatedBallAngle(1.0F);
            }

            setupSpinPhysics(payload.winningNumber());
            int totalTicks = Math.max(1, payload.totalSpinTicks());
            int remainingTicks = Math.max(0, payload.remainingSpinTicks());
            int elapsed = Math.max(0, totalTicks - remainingTicks);
            float progress = (float) elapsed / (float) totalTicks;
            this.spinTimer = progress * TOTAL_SPIN_DURATION;

            float ease = 1.0F - (float) Math.pow(1.0F - Math.min(1.0F, progress), 4.0);
            this.wheelAngle = (this.startWheelAngle + this.targetWheelDelta * ease) % 360.0F;
            this.ballAngle = (this.startBallAngle + this.targetBallDelta * ease) % 360.0F;
            if (this.wheelAngle < 0.0F) this.wheelAngle += 360.0F;
            if (this.ballAngle < 0.0F) this.ballAngle += 360.0F;
        } else if (payload.winningNumber() >= 0) {
            this.targetWinningNumber = payload.winningNumber();
            this.lastPayout = payload.currentPayout();
            int winIdx = RouletteWheel.getIndexInWheel(payload.winningNumber());
            float angleStep = 360.0F / RouletteWheel.POCKET_COUNT;
            this.ballAngle = (this.wheelAngle + winIdx * angleStep) % 360.0F;
        }
    }

    private void setupSpinPhysics(int winningNumber) {
        this.targetWinningNumber = winningNumber;
        this.startWheelAngle = this.wheelAngle % 360.0F;
        this.startBallAngle = this.ballAngle % 360.0F;

        int winIdx = RouletteWheel.getIndexInWheel(winningNumber);
        float angleStep = 360.0F / RouletteWheel.POCKET_COUNT;
        float pocketAngleOnWheel = winIdx * angleStep;

        this.targetWheelDelta = 1440.0F;
        float finalWheelAngle = (this.startWheelAngle + this.targetWheelDelta) % 360.0F;
        float finalPocketWorld = (finalWheelAngle + pocketAngleOnWheel) % 360.0F;

        float ballAngleOffset = (finalPocketWorld - (this.startBallAngle % 360.0F));
        while (ballAngleOffset > 0.0F) ballAngleOffset -= 360.0F;
        this.targetBallDelta = -2880.0F + ballAngleOffset;
    }

    @Override
    protected void init() {
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;
    }

    public void handleResult(RouletteResultS2CPayload payload) {
        this.targetWinningNumber = payload.winningNumber();
        this.lastPayout = payload.totalPayout();
        this.lastTotalBet = payload.totalBet();
        this.currentMora = payload.updatedMora();

        this.isSpinning = true;
        this.spinTimer = 0.0F;
        this.resultTimer = 0.0F;
        setupSpinPhysics(payload.winningNumber());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void renderTransparentBackground(GuiGraphics guiGraphics) {
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 480.0F;
        float scaleY = (float) this.height / 270.0F;
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
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        updateSpinAnimation();

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

        g.fill(panelX - 4, panelY - 4, panelX + panelWidth + 4, panelY + panelHeight + 4, 0xFF3E2723);
        g.renderOutline(panelX - 4, panelY - 4, panelWidth + 8, panelHeight + 8, 0xFFC5A059);

        g.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF0D3B22);
        g.renderOutline(panelX, panelY, panelWidth, panelHeight, 0xFF1B5E20);

        renderTopBar(g);

        renderRouletteWheel(g);

        renderAnnouncerPanel(g);

        renderBettingLayout(g, mX, mY);

        renderChipSelector(g, mX, mY);

        renderActionButtons(g, mX, mY);

        renderPersonalRecordPill(g);

        super.render(g, mX, mY, partialTicks);
        g.pose().popPose();
    }

    private void renderPersonalRecordPill(GuiGraphics g) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        com.soybe.awakeningsystem.SystemData sData = com.soybe.awakeningsystem.SystemEvents.data(this.minecraft.player);
        long totalWon = sData.getCasinoTotalWon();
        int maxPay = sData.getCasinoMaxPayout();
        Component recordComp = Component.translatable("awakening_system.casino.blackjack.record_pill",
                String.format(java.util.Locale.US, "%,d", totalWon),
                String.format(java.util.Locale.US, "%,d", (long) maxPay));
        int textW = this.font.width(recordComp);
        int pillX = panelX + (panelWidth - textW - 12) / 2;
        int pillY = panelY - 16;
        g.fill(pillX, pillY, pillX + textW + 12, pillY + 13, 0xEE121820);
        g.renderOutline(pillX, pillY, textW + 12, 13, 0x88D4AF37);
        g.drawString(this.font, recordComp, pillX + 6, pillY + 3, 0xFFFFFF);
    }

    private void updateSpinAnimation() {
        long now = net.minecraft.Util.getMillis();
        float dt = (lastFrameTime == 0L) ? 0.016F : (now - lastFrameTime) / 1000.0F;
        lastFrameTime = now;
        dt = Mth.clamp(dt, 0.001F, 0.05F);

        if (isSpinning) {
            spinTimer += dt;
            float p = Math.min(1.0F, spinTimer / TOTAL_SPIN_DURATION);
            float ease = 1.0F - (float) Math.pow(1.0F - p, 4.0);

            wheelAngle = (startWheelAngle + targetWheelDelta * ease) % 360.0F;
            ballAngle = (startBallAngle + targetBallDelta * ease) % 360.0F;
            if (wheelAngle < 0.0F) wheelAngle += 360.0F;
            if (ballAngle < 0.0F) ballAngle += 360.0F;

            clickTimer += dt;
            float currentInterval = 0.06F + p * p * 0.40F;
            if (clickTimer >= currentInterval && p < 0.95F) {
                clickTimer = 0.0F;
                if (this.minecraft != null && this.minecraft.player != null) {
                    float pitch = 1.3F - (p * 0.5F);
                    this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.35F, pitch);
                }
            }

            if (p >= 1.0F) {
                isSpinning = false;
                resultTimer = 1.0F;

                if (targetWinningNumber >= 0) {
                    RECENT_NUMBERS.add(0, targetWinningNumber);
                    if (RECENT_NUMBERS.size() > 9) {
                        RECENT_NUMBERS.remove(RECENT_NUMBERS.size() - 1);
                    }
                    int winIdx = RouletteWheel.getIndexInWheel(targetWinningNumber);
                    float angleStep = 360.0F / RouletteWheel.POCKET_COUNT;
                    ballAngle = (wheelAngle + winIdx * angleStep) % 360.0F;
                }

                LAST_BETS.clear();
                LAST_BETS.addAll(currentBets);
                currentBets.clear();

                if (this.minecraft != null && this.minecraft.player != null) {
                    if (lastPayout > 0) {
                        this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 0.9F, 1.2F);
                    } else {
                        this.minecraft.player.playSound(SoundEvents.DISPENSER_FAIL, 0.8F, 0.9F);
                    }
                }
            }
        } else {
            // Rodaje por inercia suave: La rueda no se clava bruscamente; gira suavemente y la bola acompaña
            wheelAngle = (wheelAngle + 10.0F * dt) % 360.0F;
            if (wheelAngle < 0.0F) wheelAngle += 360.0F;
            if (targetWinningNumber >= 0) {
                int winIdx = RouletteWheel.getIndexInWheel(targetWinningNumber);
                float angleStep = 360.0F / RouletteWheel.POCKET_COUNT;
                ballAngle = (wheelAngle + winIdx * angleStep) % 360.0F;
            }
        }
    }

    private void renderTopBar(GuiGraphics g) {
        int topBarBg = isVip ? 0xFF1A1505 : (isIllegal ? 0xFF2A0D0D : 0xFF082214);
        int topBarBorder = isVip ? 0xFFFFD700 : (isIllegal ? 0xFFE53935 : 0xFFC5A059);
        g.fill(panelX, panelY, panelX + panelWidth, panelY + 24, topBarBg);
        g.renderOutline(panelX, panelY, panelWidth, 24, topBarBorder);

        Component title = isVip ? Component.translatable("awakening_system.roulette.title_vip")
                : (isIllegal ? Component.translatable("awakening_system.roulette.title_illegal") : Component.translatable("awakening_system.roulette.title_normal"));
        g.drawString(this.font, title, panelX + 8, panelY + 8, isVip ? 0xFFFFD700 : 0xFFFFFF);

        String moraFormatted = String.format(java.util.Locale.US, "%,d", currentMora);
        Component moraText = Component.translatable("awakening_system.roulette.mora", moraFormatted);
        int titleW = this.font.width(title);
        int moraX = Math.max(panelX + 8 + titleW + 12, panelX + 175);
        g.drawString(this.font, moraText, moraX, panelY + 8, 0xFFFFFF);

        long totalBet = currentBets.stream().mapToLong(RouletteBetEntry::amount).sum();
        String betFormatted = String.format(java.util.Locale.US, "%,d", totalBet);
        Component betText = Component.translatable(isVip ? "awakening_system.roulette.bet_vip" : "awakening_system.roulette.bet", betFormatted);
        int moraW = this.font.width(moraText);
        int betX = moraX + moraW + 14;
        g.drawString(this.font, betText, betX, panelY + 8, 0xFFFFFF);

        int histX = panelX + panelWidth - 14;
        int shownCount = 0;
        for (int num : RECENT_NUMBERS) {
            if (shownCount >= 5) break;
            RouletteWheel.PocketColor col = RouletteWheel.getColor(num);
            g.fill(histX - 12, panelY + 4, histX, panelY + 20, col.getHexColor());
            g.renderOutline(histX - 12, panelY + 4, 12, 16, 0xFFFFFFFF);
            String ns = String.valueOf(num);
            int textW = this.font.width(ns);
            g.drawString(this.font, ns, histX - 6 - textW / 2, panelY + 8, 0xFFFFFF);
            histX -= 15;
            shownCount++;
        }
    }

    private void renderRouletteWheel(GuiGraphics g) {
        int cx = panelX + 70;
        int cy = panelY + 86;
        int radius = 52;

        drawCircle(g, cx, cy, radius + 4, 0xFF3E2723);
        drawCircle(g, cx, cy, radius + 2, isVip ? 0xFFFFD700 : 0xFFC5A059);
        drawCircle(g, cx, cy, radius, 0xFF141414);

        int pockets = RouletteWheel.POCKET_COUNT;
        float angleStep = 360.0F / pockets;

        float needleAngle = -90.0F;
        float relAngle = (needleAngle - wheelAngle) % 360.0F;
        if (relAngle < 0) relAngle += 360.0F;
        int currentPocketIndex = (int) (relAngle / angleStep) % pockets;
        int currentPassingNumber = RouletteWheel.WHEEL_ORDER[currentPocketIndex];

        for (int i = 0; i < pockets; i++) {
            int num = RouletteWheel.WHEEL_ORDER[i];
            RouletteWheel.PocketColor col = RouletteWheel.getColor(num);
            float currentAng = (wheelAngle + i * angleStep) * Mth.DEG_TO_RAD;

            int px = (int) (cx + Math.cos(currentAng) * (radius - 10));
            int py = (int) (cy + Math.sin(currentAng) * (radius - 10));

            g.fill(px - 2, py - 2, px + 2, py + 2, col.getHexColor());
        }

        g.fill(cx - 2, cy - radius - 6, cx + 2, cy - radius + 3, 0xFFFFD700);
        g.renderOutline(cx - 2, cy - radius - 6, 4, 9, 0xFF000000);

        float r;
        if (isSpinning) {
            float p = Math.min(1.0F, spinTimer / TOTAL_SPIN_DURATION);
            if (p < 0.60F) {
                r = radius - 4;
            } else if (p < 0.85F) {
                float fallProgress = (p - 0.60F) / 0.25F;
                r = (radius - 4) - 6.0F * fallProgress;
            } else {
                float settleProgress = (p - 0.85F) / 0.15F;
                r = (radius - 10) + (float) (Math.sin(settleProgress * Math.PI * 4.0D) * 1.5F * (1.0F - settleProgress));
            }
        } else {
            r = radius - 10;
        }

        float bAng = ballAngle * Mth.DEG_TO_RAD;
        int bx = (int) (cx + Math.cos(bAng) * r);
        int by = (int) (cy + Math.sin(bAng) * r);
        drawCircle(g, bx, by, 3, 0xFFFFFFFF);

        int displayNum = isSpinning ? currentPassingNumber : (targetWinningNumber >= 0 ? targetWinningNumber : 0);
        RouletteWheel.PocketColor dispCol = RouletteWheel.getColor(displayNum);

        drawCircle(g, cx, cy, 18, 0xFFC5A059);
        drawCircle(g, cx, cy, 16, dispCol.getHexColor());
        drawCircle(g, cx, cy, 14, 0xFF1B1B1B);

        String dispStr = String.valueOf(displayNum);
        int dsw = this.font.width(dispStr);
        int textColor = dispCol == RouletteWheel.PocketColor.RED ? 0xFFFF5252 : (dispCol == RouletteWheel.PocketColor.GREEN ? 0xFF69F0AE : 0xFFE0E0E0);
        g.drawString(this.font, dispStr, cx - dsw / 2, cy - 4, textColor);
    }

    private void renderAnnouncerPanel(GuiGraphics g) {
        int ax = panelX + 10;
        int ay = panelY + 148;
        int aw = 120;
        int ah = 92;

        g.fill(ax, ay, ax + aw, ay + ah, 0xFF0A1E13);
        g.renderOutline(ax, ay, aw, ah, 0xFFC5A059);

        g.fill(ax, ay, ax + aw, ay + 16, 0xFF143020);
        g.drawString(this.font, Component.translatable("awakening_system.roulette.result_header"), ax + 14, ay + 4, 0xFFFFFF);

        if (isSpinning) {
            g.drawString(this.font, Component.translatable("awakening_system.roulette.spinning_text"), ax + 10, ay + 30, 0xFFFFFF);
            g.drawString(this.font, Component.translatable("awakening_system.roulette.good_luck"), ax + 24, ay + 48, 0xFFFFFF);
        } else if (targetWinningNumber >= 0) {
            RouletteWheel.PocketColor col = RouletteWheel.getColor(targetWinningNumber);

            g.fill(ax + 10, ay + 24, ax + 46, ay + 50, col.getHexColor());
            g.renderOutline(ax + 10, ay + 24, 36, 26, 0xFFFFD700);

            String winStr = String.valueOf(targetWinningNumber);
            int ww = this.font.width(winStr);
            g.drawString(this.font, winStr, ax + 28 - ww / 2, ay + 33, 0xFFFFFF);

            g.drawString(this.font, col.getComponent(), ax + 52, ay + 28, 0xFFFFFF);
            Component parity = targetWinningNumber == 0 ? Component.translatable("awakening_system.roulette.zero") : (targetWinningNumber % 2 == 0 ? Component.translatable("awakening_system.roulette.even") : Component.translatable("awakening_system.roulette.odd"));
            g.drawString(this.font, parity, ax + 52, ay + 40, 0xFFAAAAAA);

            if (lastPayout > 0) {
                g.drawString(this.font, Component.translatable("awakening_system.roulette.win_header"), ax + 16, ay + 60, 0xFFFFFF);
                String payStr = "§6+" + String.format(java.util.Locale.US, "%,d", lastPayout);
                int pw = this.font.width(payStr);
                g.drawString(this.font, payStr, ax + (aw - pw) / 2, ay + 74, 0xFFFFFF);
            } else {
                g.drawString(this.font, Component.translatable("awakening_system.roulette.no_win"), ax + 6, ay + 64, 0xFFFFFF);
                g.drawString(this.font, Component.translatable("awakening_system.roulette.try_again"), ax + 14, ay + 76, 0xFFFFFF);
            }
        } else {
            g.drawString(this.font, Component.translatable("awakening_system.roulette.place_chips1"), ax + 12, ay + 32, 0xFFFFFF);
            g.drawString(this.font, Component.translatable("awakening_system.roulette.place_chips2"), ax + 22, ay + 46, 0xFFFFFF);
            g.drawString(this.font, Component.translatable("awakening_system.roulette.place_chips3"), ax + 20, ay + 60, 0xFFFFFF);
        }
    }

    private void renderBettingLayout(GuiGraphics g, int mouseX, int mouseY) {
        int startX = panelX + 140;
        int startY = panelY + 34;
        int cellW = 22;
        int cellH = 22;

        boolean hoverZero = isMouseIn(mouseX, mouseY, startX, startY, 22, cellH * 3);
        g.fill(startX, startY, startX + 22, startY + cellH * 3, hoverZero ? 0xFF388E3C : 0xFF2E7D32);
        g.renderOutline(startX, startY, 22, cellH * 3, 0xFFC5A059);
        g.drawString(this.font, "0", startX + 8, startY + (cellH * 3) / 2 - 4, 0xFFFFFF);
        renderPlacedChips(g, RouletteBetType.STRAIGHT_UP, 0, startX, startY, 22, cellH * 3);

        int gridX = startX + 24;

        for (int col = 0; col < 12; col++) {
            for (int row = 0; row < 3; row++) {
                int num = (3 - row) + col * 3;
                int cx = gridX + col * cellW;
                int cy = startY + row * cellH;

                boolean hover = isMouseIn(mouseX, mouseY, cx, cy, cellW, cellH);
                RouletteWheel.PocketColor pocketCol = RouletteWheel.getColor(num);
                int baseColor = pocketCol == RouletteWheel.PocketColor.RED ? 0xFFC62828 : 0xFF212121;
                if (hover) {
                    baseColor = pocketCol == RouletteWheel.PocketColor.RED ? 0xFFE53935 : 0xFF424242;
                }

                g.fill(cx, cy, cx + cellW, cy + cellH, baseColor);
                g.renderOutline(cx, cy, cellW, cellH, 0xFFC5A059);

                String ns = String.valueOf(num);
                int nw = this.font.width(ns);
                g.drawString(this.font, ns, cx + (cellW - nw) / 2, cy + 4, 0xFFFFFF);

                renderPlacedChips(g, RouletteBetType.STRAIGHT_UP, num, cx, cy, cellW, cellH);
            }
        }

        int colEndBtnX = gridX + 12 * cellW + 2;
        RouletteBetType[] colTypes = { RouletteBetType.COLUMN_3, RouletteBetType.COLUMN_2, RouletteBetType.COLUMN_1 };
        for (int r = 0; r < 3; r++) {
            int cy = startY + r * cellH;
            boolean hover = isMouseIn(mouseX, mouseY, colEndBtnX, cy, 28, cellH);
            g.fill(colEndBtnX, cy, colEndBtnX + 28, cy + cellH, hover ? 0xFF1976D2 : 0xFF0D47A1);
            g.renderOutline(colEndBtnX, cy, 28, cellH, 0xFFC5A059);
            g.drawString(this.font, "2:1", colEndBtnX + 6, cy + (cellH - 8) / 2, 0xFFFFFF);
            renderPlacedChips(g, colTypes[r], 0, colEndBtnX, cy, 28, cellH);
        }

        int dozenY = startY + 3 * cellH + 3;
        int dozenW = (12 * cellW) / 3;
        Component[] dozenLabels = {
                Component.translatable("awakening_system.roulette.dozen_1"),
                Component.translatable("awakening_system.roulette.dozen_2"),
                Component.translatable("awakening_system.roulette.dozen_3")
        };
        RouletteBetType[] dozenTypes = { RouletteBetType.DOZEN_1, RouletteBetType.DOZEN_2, RouletteBetType.DOZEN_3 };

        for (int d = 0; d < 3; d++) {
            int dx = gridX + d * dozenW;
            boolean hover = isMouseIn(mouseX, mouseY, dx, dozenY, dozenW, 16);
            g.fill(dx, dozenY, dx + dozenW, dozenY + 16, hover ? 0xFF2E7D32 : 0xFF1B5E20);
            g.renderOutline(dx, dozenY, dozenW, 16, 0xFFC5A059);

            int lw = this.font.width(dozenLabels[d]);
            g.drawString(this.font, dozenLabels[d], dx + (dozenW - lw) / 2, dozenY + 4, 0xFFE0E0E0);
            renderPlacedChips(g, dozenTypes[d], 0, dx, dozenY, dozenW, 16);
        }

        int simpleY = dozenY + 19;
        int simpleW = (12 * cellW) / 6;
        Component[] simpleLabels = {
                Component.literal("1-18"),
                Component.translatable("awakening_system.roulette.even"),
                Component.translatable("awakening_system.roulette.red"),
                Component.translatable("awakening_system.roulette.black"),
                Component.translatable("awakening_system.roulette.odd"),
                Component.literal("19-36")
        };
        RouletteBetType[] simpleTypes = {
                RouletteBetType.LOW, RouletteBetType.EVEN, RouletteBetType.RED,
                RouletteBetType.BLACK, RouletteBetType.ODD, RouletteBetType.HIGH
        };
        int[] simpleBg = { 0xFF1B5E20, 0xFF1B5E20, 0xFFB71C1C, 0xFF212121, 0xFF1B5E20, 0xFF1B5E20 };

        for (int s = 0; s < 6; s++) {
            int sx = gridX + s * simpleW;
            boolean hover = isMouseIn(mouseX, mouseY, sx, simpleY, simpleW, 18);
            int bg = hover ? (simpleBg[s] + 0x222222) : simpleBg[s];

            g.fill(sx, simpleY, sx + simpleW, simpleY + 18, bg);
            g.renderOutline(sx, simpleY, simpleW, 18, 0xFFC5A059);

            int sw = this.font.width(simpleLabels[s]);
            g.drawString(this.font, simpleLabels[s], sx + (simpleW - sw) / 2, simpleY + 5, 0xFFFFFFFF);
            renderPlacedChips(g, simpleTypes[s], 0, sx, simpleY, simpleW, 18);
        }
    }

    private void renderPlacedChips(GuiGraphics g, RouletteBetType type, int param, int x, int y, int w, int h) {
        long amountOnThis = currentBets.stream()
                .filter(b -> b.type() == type && b.param() == param)
                .mapToLong(RouletteBetEntry::amount)
                .sum();

        if (amountOnThis > 0) {
            g.renderOutline(x + 1, y + 1, w - 2, h - 2, 0xFFFFD700);

            String txt = formatShortAmount(amountOnThis);
            int tw = this.font.width(txt);
            int badgeX = x + (w - tw) / 2;
            int badgeY = y + h - 9;

            g.fill(badgeX - 2, badgeY - 1, badgeX + tw + 2, badgeY + 8, 0xEEFFD700);
            g.drawString(this.font, txt, badgeX, badgeY, 0xFF000000);
        }
    }

    private void renderChipSelector(GuiGraphics g, int mouseX, int mouseY) {
        int startX = panelX + 140;
        int chipY = panelY + 176;

        String chipValStr = String.format(java.util.Locale.US, "%,d", selectedBetAmount);
        g.drawString(this.font, Component.translatable("awakening_system.roulette.chip_value", chipValStr), startX, chipY - 10, 0xFFFFFF);

        long[] moraTiers = { 100L, 500L, 1000L, 5000L, 10000L };
        int bx = startX;

        for (long tierVal : moraTiers) {
            boolean isSelected = (selectedChipTier == -1 && selectedBetAmount == tierVal);
            boolean hover = isMouseIn(mouseX, mouseY, bx, chipY, 34, 18);

            int bg = isSelected ? 0xFFFFA000 : (hover ? 0xFF5D4037 : 0xFF3E2723);
            g.fill(bx, chipY, bx + 34, chipY + 18, bg);
            g.renderOutline(bx, chipY, 34, 18, isSelected ? 0xFFFFD700 : 0xFF8D6E63);

            String label = formatShortAmount(tierVal);
            int lw = this.font.width(label);
            g.drawString(this.font, label, bx + (34 - lw) / 2, chipY + 5, isSelected ? 0xFF000000 : 0xFFFFF8E1);

            bx += 37;
        }

        CasinoChipItem.ChipTier[] chipTiers = CasinoChipItem.ChipTier.values();
        int maxTier = isIllegal ? chipTiers.length : 4;
        for (int i = 0; i < maxTier; i++) {
            if (chipCounts[i] <= 0) continue;

            boolean isSelected = (selectedChipTier == i);
            boolean hover = isMouseIn(mouseX, mouseY, bx, chipY, 32, 18);

            int bg = isSelected ? 0xFF00B0FF : (hover ? 0xFF37474F : 0xFF263238);
            g.fill(bx, chipY, bx + 32, chipY + 18, bg);
            g.renderOutline(bx, chipY, 32, 18, isSelected ? 0xFF80D8FF : 0xFF546E7A);

            String label = chipTiers[i].getDisplayName().substring(0, Math.min(3, chipTiers[i].getDisplayName().length()));
            int lw = this.font.width(label);
            g.drawString(this.font, label, bx + (32 - lw) / 2, chipY + 5, 0xFFFFFFFF);

            bx += 35;
        }
    }

    private void renderActionButtons(GuiGraphics g, int mouseX, int mouseY) {
        int btnY = panelY + 208;

        int spinX = panelX + 355;
        boolean canSpin = !isSpinning && !currentBets.isEmpty();
        boolean hoverSpin = canSpin && isMouseIn(mouseX, mouseY, spinX, btnY, 95, 26);

        int spinBg = canSpin ? (hoverSpin ? 0xFF43A047 : 0xFF2E7D32) : 0xFF424242;
        g.fill(spinX, btnY, spinX + 95, btnY + 26, spinBg);
        g.renderOutline(spinX, btnY, 95, 26, canSpin ? 0xFFFFD700 : 0xFF616161);

        Component spinText = isSpinning
                ? Component.translatable("awakening_system.roulette.btn.spinning")
                : Component.translatable("awakening_system.roulette.btn.spin");
        int stw = this.font.width(spinText);
        g.drawString(this.font, spinText, spinX + (95 - stw) / 2, btnY + 9, 0xFFFFFF);

        int clearX = panelX + 280;
        boolean canClear = !isSpinning && !currentBets.isEmpty();
        boolean hoverClear = canClear && isMouseIn(mouseX, mouseY, clearX, btnY, 70, 26);
        int clearBg = canClear ? (hoverClear ? 0xFFD32F2F : 0xFFC62828) : 0xFF424242;
        g.fill(clearX, btnY, clearX + 70, btnY + 26, clearBg);
        g.renderOutline(clearX, btnY, 70, 26, 0xFF8D6E63);
        Component clearText = Component.translatable("awakening_system.roulette.btn.clear");
        int ctw = this.font.width(clearText);
        g.drawString(this.font, clearText, clearX + (70 - ctw) / 2, btnY + 9, 0xFFFFFFFF);

        int repeatX = panelX + 205;
        boolean canRepeat = !isSpinning && currentBets.isEmpty() && !LAST_BETS.isEmpty();
        boolean hoverRepeat = canRepeat && isMouseIn(mouseX, mouseY, repeatX, btnY, 70, 26);
        int repeatBg = canRepeat ? (hoverRepeat ? 0xFF0288D1 : 0xFF0277BD) : 0xFF424242;
        g.fill(repeatX, btnY, repeatX + 70, btnY + 26, repeatBg);
        g.renderOutline(repeatX, btnY, 70, 26, 0xFF8D6E63);
        Component repText = Component.translatable("awakening_system.roulette.btn.repeat");
        int rtw = this.font.width(repText);
        g.drawString(this.font, repText, repeatX + (70 - rtw) / 2, btnY + 9, 0xFFFFFFFF);

        int doubleX = panelX + 140;
        boolean canDouble = !isSpinning && !currentBets.isEmpty();
        boolean hoverDouble = canDouble && isMouseIn(mouseX, mouseY, doubleX, btnY, 60, 26);
        int dblBg = canDouble ? (hoverDouble ? 0xFFF57C00 : 0xFFEF6C00) : 0xFF424242;
        g.fill(doubleX, btnY, doubleX + 60, btnY + 26, dblBg);
        g.renderOutline(doubleX, btnY, 60, 26, 0xFF8D6E63);
        Component dblText = Component.translatable("awakening_system.roulette.btn.double");
        int dtw = this.font.width(dblText);
        g.drawString(this.font, dblText, doubleX + (60 - dtw) / 2, btnY + 9, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isSpinning) return super.mouseClicked(mouseX, mouseY, button);

        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int mx = (int) mouseX;
        int my = (int) mouseY;

        int startX = panelX + 140;
        int chipY = panelY + 176;
        long[] moraTiers = { 100L, 500L, 1000L, 5000L, 10000L };
        int bx = startX;

        for (long tierVal : moraTiers) {
            if (isMouseIn(mx, my, bx, chipY, 34, 18)) {
                selectedBetAmount = tierVal;
                selectedChipTier = -1;
                playClickSound();
                return true;
            }
            bx += 37;
        }

        CasinoChipItem.ChipTier[] chipTiers = CasinoChipItem.ChipTier.values();
        int maxTier = isIllegal ? chipTiers.length : 4;
        for (int i = 0; i < maxTier; i++) {
            if (chipCounts[i] <= 0) continue;
            if (isMouseIn(mx, my, bx, chipY, 32, 18)) {
                selectedChipTier = i;
                selectedBetAmount = chipTiers[i].getMoraValue();
                playClickSound();
                return true;
            }
            bx += 35;
        }

        int btnY = panelY + 208;

        int spinX = panelX + 355;
        if (!isSpinning && !currentBets.isEmpty() && isMouseIn(mx, my, spinX, btnY, 95, 26)) {
            PacketDistributor.sendToServer(new RouletteSpinC2SPayload(pos, new ArrayList<>(currentBets)));
            this.isSpinning = true;
            this.spinTimer = 0.0F;
            playClickSound();
            return true;
        }

        int clearX = panelX + 280;
        if (!currentBets.isEmpty() && isMouseIn(mx, my, clearX, btnY, 70, 26)) {
            currentBets.clear();
            playClickSound();
            return true;
        }

        int repeatX = panelX + 205;
        if (currentBets.isEmpty() && !LAST_BETS.isEmpty() && isMouseIn(mx, my, repeatX, btnY, 70, 26)) {
            currentBets.addAll(LAST_BETS);
            playClickSound();
            return true;
        }

        int doubleX = panelX + 140;
        if (!currentBets.isEmpty() && isMouseIn(mx, my, doubleX, btnY, 60, 26)) {
            List<RouletteBetEntry> doubled = new ArrayList<>();
            for (RouletteBetEntry b : currentBets) {
                doubled.add(new RouletteBetEntry(b.type(), b.param(), b.amount() * 2, b.chipTier()));
            }
            currentBets.clear();
            currentBets.addAll(doubled);
            playClickSound();
            return true;
        }

        handleBoardClick(mx, my, button);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleBoardClick(int mx, int my, int button) {
        int startX = panelX + 140;
        int startY = panelY + 34;
        int cellW = 22;
        int cellH = 22;

        if (isMouseIn(mx, my, startX, startY, 22, cellH * 3)) {
            modifyBet(RouletteBetType.STRAIGHT_UP, 0, button);
            return;
        }

        int gridX = startX + 24;

        for (int col = 0; col < 12; col++) {
            for (int row = 0; row < 3; row++) {
                int num = (3 - row) + col * 3;
                int cx = gridX + col * cellW;
                int cy = startY + row * cellH;

                if (isMouseIn(mx, my, cx, cy, cellW, cellH)) {
                    modifyBet(RouletteBetType.STRAIGHT_UP, num, button);
                    return;
                }
            }
        }

        int colEndBtnX = gridX + 12 * cellW + 2;
        RouletteBetType[] colTypes = { RouletteBetType.COLUMN_3, RouletteBetType.COLUMN_2, RouletteBetType.COLUMN_1 };
        for (int r = 0; r < 3; r++) {
            int cy = startY + r * cellH;
            if (isMouseIn(mx, my, colEndBtnX, cy, 28, cellH)) {
                modifyBet(colTypes[r], 0, button);
                return;
            }
        }

        int dozenY = startY + 3 * cellH + 3;
        int dozenW = (12 * cellW) / 3;
        RouletteBetType[] dozenTypes = { RouletteBetType.DOZEN_1, RouletteBetType.DOZEN_2, RouletteBetType.DOZEN_3 };
        for (int d = 0; d < 3; d++) {
            int dx = gridX + d * dozenW;
            if (isMouseIn(mx, my, dx, dozenY, dozenW, 16)) {
                modifyBet(dozenTypes[d], 0, button);
                return;
            }
        }

        int simpleY = dozenY + 19;
        int simpleW = (12 * cellW) / 6;
        RouletteBetType[] simpleTypes = {
                RouletteBetType.LOW, RouletteBetType.EVEN, RouletteBetType.RED,
                RouletteBetType.BLACK, RouletteBetType.ODD, RouletteBetType.HIGH
        };
        for (int s = 0; s < 6; s++) {
            int sx = gridX + s * simpleW;
            if (isMouseIn(mx, my, sx, simpleY, simpleW, 18)) {
                modifyBet(simpleTypes[s], 0, button);
                return;
            }
        }
    }

    private void modifyBet(RouletteBetType type, int param, int mouseButton) {
        if (mouseButton == 0) {
            currentBets.add(new RouletteBetEntry(type, param, selectedBetAmount, selectedChipTier));
            playChipSound();
        } else if (mouseButton == 1) {
            for (int i = currentBets.size() - 1; i >= 0; i--) {
                RouletteBetEntry b = currentBets.get(i);
                if (b.type() == type && b.param() == param) {
                    currentBets.remove(i);
                    playChipSound();
                    break;
                }
            }
        }
    }

    private void playClickSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.0F);
        }
    }

    private void playChipSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7F, 1.4F);
        }
    }

    private boolean isMouseIn(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void drawCircle(GuiGraphics g, int cx, int cy, int radius, int color) {
        if (radius <= 0) return;
        int r2 = radius * radius;
        for (int y = -radius; y <= radius; y++) {
            int dx = (int) Math.round(Math.sqrt(r2 - y * y));
            g.fill(cx - dx, cy + y, cx + dx + 1, cy + y + 1, color);
        }
    }

    private String formatShortAmount(long val) {
        if (val >= 1_000_000) return (val / 1_000_000) + "M";
        if (val >= 1_000) return (val / 1_000) + "k";
        return String.valueOf(val);
    }

    @Override
    public void onClose() {
        if (isSpinning) {
            if (!currentBets.isEmpty()) {
                LAST_BETS.clear();
                LAST_BETS.addAll(currentBets);
                currentBets.clear();
            }
            if (targetWinningNumber >= 0) {
                RECENT_NUMBERS.add(0, targetWinningNumber);
                if (RECENT_NUMBERS.size() > 9) {
                    RECENT_NUMBERS.remove(RECENT_NUMBERS.size() - 1);
                }
            }
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
