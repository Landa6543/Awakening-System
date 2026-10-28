package com.soybe.awakeningsystem.casino.pool.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.casino.pool.PoolBall;
import com.soybe.awakeningsystem.casino.pool.PoolTableBlockEntity;
import com.soybe.awakeningsystem.casino.pool.network.PoolTableActionC2SPayload;
import com.soybe.awakeningsystem.casino.pool.network.PoolTableStateS2CPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PoolTableScreen extends Screen {
    private final BlockPos tablePos;
    private final Direction facing;

    private List<PoolBall> balls = new ArrayList<>();
    private int gameMode = PoolTableBlockEntity.MODE_AI_NORMAL;
    private int betAmount = 0;
    private String player1Name = "Jugador 1";
    private String player2Name = "IA Hustler";
    private int currentTurn = 1;
    private int p1Suit = PoolTableBlockEntity.SUIT_NONE;
    private int p2Suit = PoolTableBlockEntity.SUIT_NONE;
    private int gameStatus = PoolTableBlockEntity.STATUS_READY;
    private String statusMessage = Component.translatable("awakening_system.casino.pool.status.welcome").getString();

    private float aimAngle = 0.0F;
    private float shotPower = 0.35F;
    private boolean isDraggingPower = false;
    private boolean isDraggingVerticalMeter = false;
    private long lastAimSyncTime = 0L;

    private boolean showSettingsModal = false;
    private int selectedMode = PoolTableBlockEntity.MODE_AI_NORMAL;
    private int selectedBet = 0;
    private boolean isIllegal = false;

    private static final int[] BET_OPTIONS = {0, 1000, 5000, 10000, 25000, 50000, 100000};
    private static final int[] BET_OPTIONS_ILLEGAL = {0, 1, 2, 5, 10, 20, 50};

    public int[] getCurrentBetOptions() {
        return isIllegal ? BET_OPTIONS_ILLEGAL : BET_OPTIONS;
    }

    public String getBetLabel(int index) {
        int[] curBets = getCurrentBetOptions();
        if (index < 0 || index >= curBets.length) return "";
        int bVal = curBets[index];
        if (bVal == 0) {
            return Component.translatable("awakening_system.casino.darts.free").getString();
        }
        if (isIllegal) {
            return Component.translatable("awakening_system.casino.pool.chip_count", bVal).getString();
        } else {
            return String.format(Locale.US, "%,d 🪙", (long) bVal);
        }
    }

    public PoolTableScreen(BlockPos tablePos, Direction facing, boolean isIllegal) {
        super(Component.translatable(isIllegal ? "awakening_system.casino.pool.screen_title_illegal" : "awakening_system.casino.pool.screen_title"));
        this.tablePos = tablePos;
        this.facing = facing;
        this.isIllegal = isIllegal;
    }

    public PoolTableScreen(BlockPos tablePos, Direction facing) {
        this(tablePos, facing, false);
    }

    public PoolTableScreen(com.soybe.awakeningsystem.casino.pool.network.OpenPoolTablePayload payload) {
        this(payload.tablePos(), payload.facing(), payload.isIllegal());
    }

    public void updateState(PoolTableStateS2CPayload payload) {
        if (!payload.tablePos().equals(this.tablePos)) return;
        this.balls = new ArrayList<>(payload.balls());
        this.gameMode = payload.gameMode();
        this.betAmount = payload.betAmount();
        this.player1Name = payload.player1Name();
        this.player2Name = payload.player2Name();
        this.currentTurn = payload.currentTurn();
        this.p1Suit = payload.p1Suit();
        this.p2Suit = payload.p2Suit();
        this.gameStatus = payload.gameStatus();
        this.statusMessage = payload.statusMessage();
        this.isIllegal = payload.isIllegal();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int getTableY() {
        return 64;
    }

    private int getTableH() {
        return Math.min(235, this.height - getTableY() - 48);
    }

    private int getTableW() {
        return (int) (getTableH() * (PoolTableBlockEntity.TABLE_HALF_WIDTH / PoolTableBlockEntity.TABLE_HALF_LENGTH));
    }

    private int getTableX() {
        return (this.width - getTableW()) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int w = this.width;
        int h = this.height;

        graphics.fillGradient(0, 0, w, h, 0xEE11151E, 0xFA080B12);

        int tableY = getTableY();
        int tableH = getTableH();
        int tableW = getTableW();
        int tableX = getTableX();

        int frameBorder = 14;
        int frameColor = isIllegal ? 0xFF18151E : 0xFF3E2723;
        int frameOutline = isIllegal ? 0xFFA855F7 : 0xFFFFD700;
        graphics.fill(tableX - frameBorder, tableY - frameBorder, tableX + tableW + frameBorder, tableY + tableH + frameBorder, frameColor);
        graphics.renderOutline(tableX - frameBorder, tableY - frameBorder, tableW + frameBorder * 2, tableH + frameBorder * 2, frameOutline);

        renderAimingDiamonds(graphics, tableX, tableY, tableW, tableH, frameBorder);

        int feltColor = isIllegal ? 0xFF4A0812 : 0xFF1B5E20;
        int feltOutline = isIllegal ? 0xFF2B0309 : 0xFF0D3311;
        graphics.fill(tableX, tableY, tableX + tableW, tableY + tableH, feltColor);
        graphics.renderOutline(tableX, tableY, tableW, tableH, feltOutline);

        renderPockets(graphics, tableX, tableY, tableW, tableH);

        float scaleX = (float) tableW / (PoolTableBlockEntity.TABLE_HALF_WIDTH * 2.0F);
        float scaleZ = (float) tableH / (PoolTableBlockEntity.TABLE_HALF_LENGTH * 2.0F);
        int tableCenterX = tableX + tableW / 2;
        int tableCenterY = tableY + tableH / 2;

        PoolBall cueBall = findCueBall();

        boolean canShoot = isMyTurn() && gameStatus == PoolTableBlockEntity.STATUS_READY;
        if (canShoot && cueBall != null && !cueBall.pocketed) {
            updateAimAngleWithMouse(mouseX, mouseY, tableCenterX, tableCenterY, scaleX, scaleZ, cueBall);
            renderAimingGuide(graphics, cueBall, tableCenterX, tableCenterY, scaleX, scaleZ, tableX, tableY, tableW, tableH);
        }

        int ballRadiusPx = Math.max(5, (int) Math.round(PoolBall.RADIUS * scaleX * 0.95F));
        for (PoolBall ball : balls) {
            if (ball.pocketed) continue;
            int screenBX = tableCenterX + (int) (ball.x * scaleX);
            int screenBY = tableCenterY + (int) (ball.z * scaleZ);
            renderBall2D(graphics, ball, screenBX, screenBY, ballRadiusPx);
        }

        if (canShoot && cueBall != null && !cueBall.pocketed) {
            int cueScreenX = tableCenterX + (int) (cueBall.x * scaleX);
            int cueScreenY = tableCenterY + (int) (cueBall.z * scaleZ);
            renderCueStick2D(graphics, cueScreenX, cueScreenY, aimAngle, shotPower);
        }

        boolean isGameActive = (gameStatus == PoolTableBlockEntity.STATUS_READY || gameStatus == PoolTableBlockEntity.STATUS_MOVING);
        if (isGameActive && !showSettingsModal) {
            int meterX = tableX + tableW + 16;
            int meterY = tableY;
            int meterW = 14;
            int meterH = tableH;
            renderVerticalPowerMeter(graphics, meterX, meterY, meterW, meterH);
        }

        renderScoreboardHUD(graphics, w);
        renderPersonalRecordPill(graphics, w);

        if (!showSettingsModal) {
            renderBottomControls(graphics, mouseX, mouseY, tableX, tableY + tableH + 16, tableW, canShoot);
        }

        if (showSettingsModal) {
            renderSettingsModal(graphics, mouseX, mouseY);
        }
    }

    private PoolBall findCueBall() {
        for (PoolBall b : balls) {
            if (b.isCueBall()) return b;
        }
        return null;
    }

    private boolean isMyTurn() {
        if (this.minecraft == null || this.minecraft.player == null) return false;
        if (gameStatus == PoolTableBlockEntity.STATUS_WAITING_P2) return false;
        if (gameMode != PoolTableBlockEntity.MODE_PVP) {
            return currentTurn == 1;
        }
        String myName = this.minecraft.player.getName().getString();
        if (currentTurn == 1) return player1Name.equals(myName) || player1Name.equals("Jugador 1");
        if (currentTurn == 2) return player2Name.equals(myName);
        return false;
    }

    private void updateAimAngleWithMouse(int mouseX, int mouseY, int centerX, int centerY, float scaleX, float scaleZ, PoolBall cue) {
        if (isDraggingPower || isDraggingVerticalMeter) return;

        int cueScreenX = centerX + (int) (cue.x * scaleX);
        int cueScreenY = centerY + (int) (cue.z * scaleZ);

        float dx = mouseX - cueScreenX;
        float dy = mouseY - cueScreenY;

        if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
            this.aimAngle = (float) Math.atan2(dx, dy);

            long now = System.currentTimeMillis();
            if (now - lastAimSyncTime > 150L) {
                lastAimSyncTime = now;
                PacketDistributor.sendToServer(new PoolTableActionC2SPayload(
                        tablePos, PoolTableActionC2SPayload.ACTION_AIM, aimAngle, shotPower, 0, 0
                ));
            }
        }
    }

    private void renderAimingDiamonds(GuiGraphics graphics, int tx, int ty, int tw, int th, int b) {
        int[] xFractions = {tw / 4, tw / 2, (tw * 3) / 4};
        int[] yFractions = {th / 6, (th * 2) / 6, (th * 3) / 6, (th * 4) / 6, (th * 5) / 6};

        for (int fx : xFractions) {
            graphics.fill(tx + fx - 2, ty - b / 2 - 2, tx + fx + 2, ty - b / 2 + 2, 0xFFFFF8DC);
            graphics.fill(tx + fx - 2, ty + th + b / 2 - 2, tx + fx + 2, ty + th + b / 2 + 2, 0xFFFFF8DC);
        }
        for (int fy : yFractions) {
            graphics.fill(tx - b / 2 - 2, ty + fy - 2, tx - b / 2 + 2, ty + fy + 2, 0xFFFFF8DC);
            graphics.fill(tx + tw + b / 2 - 2, ty + fy - 2, tx + tw + b / 2 + 2, ty + fy + 2, 0xFFFFF8DC);
        }
    }

    private void renderPockets(GuiGraphics graphics, int tx, int ty, int tw, int th) {
        int pr = 12;
        int[][] pList = {
                {tx, ty}, {tx + tw, ty},
                {tx, ty + th / 2}, {tx + tw, ty + th / 2},
                {tx, ty + th}, {tx + tw, ty + th}
        };
        for (int[] p : pList) {
            graphics.fill(p[0] - pr, p[1] - pr, p[0] + pr, p[1] + pr, 0xFF0A0A0A);
            graphics.renderOutline(p[0] - pr, p[1] - pr, pr * 2, pr * 2, 0xFF795548);
        }
    }

    private void renderAimingGuide(GuiGraphics graphics, PoolBall cue, int cx, int cy, float sx, float sz, int tx, int ty, int tw, int th) {
        float dirX = (float) Math.sin(aimAngle);
        float dirZ = (float) Math.cos(aimAngle);

        int startX = cx + (int) (cue.x * sx);
        int startY = cy + (int) (cue.z * sz);

        for (int d = 16; d < 240; d += 8) {
            int px = startX + (int) (dirX * d);
            int py = startY + (int) (dirZ * d);
            if (px < tx || px > tx + tw || py < ty || py > ty + th) break;
            graphics.fill(px - 1, py - 1, px + 1, py + 1, 0x88FFFFFF);
        }
    }

    private void renderBall2D(GuiGraphics graphics, PoolBall ball, int bx, int by, int r) {
        int col = ball.getColorRGB() | 0xFF000000;

        graphics.fill(bx - r + 1, by + r - 1, bx + r, by + r + 2, 0x44000000);
        graphics.fill(bx - r, by + r, bx + r + 1, by + r + 1, 0x22000000);

        if (ball.isCueBall()) {
            renderCircle(graphics, bx, by, r, 0xFFF5F5F5);
            graphics.fill(bx - 2, by - 2, bx, by, 0xFFFFFFFF);
            return;
        }

        if (ball.isEightBall()) {
            renderCircle(graphics, bx, by, r, 0xFF141414);
            renderCircle(graphics, bx, by, 3, 0xFFFFFFFF);
            renderBallNumber(graphics, bx, by, 8);
            graphics.fill(bx - 2, by - 2, bx, by, 0x77FFFFFF);
            return;
        }

        if (ball.isStripe()) {
            renderCircle(graphics, bx, by, r, 0xFFF5F5F5);
            for (int dy = -2; dy <= 2; dy++) {
                int dxMax = (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
                graphics.fill(bx - dxMax, by + dy, bx + dxMax + 1, by + dy + 1, col);
            }
            renderCircle(graphics, bx, by, 3, 0xFFFFFFFF);
            renderBallNumber(graphics, bx, by, ball.getId());
            graphics.fill(bx - 2, by - 2, bx, by, 0x77FFFFFF);
        } else {
            renderCircle(graphics, bx, by, r, col);
            renderCircle(graphics, bx, by, 3, 0xFFFFFFFF);
            renderBallNumber(graphics, bx, by, ball.getId());
            graphics.fill(bx - 2, by - 2, bx, by, 0x77FFFFFF);
        }
    }

    private void renderCircle(GuiGraphics graphics, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dxMax = (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
            graphics.fill(cx - dxMax, cy + dy, cx + dxMax + 1, cy + dy + 1, color);
        }
    }

    private void renderBallNumber(GuiGraphics graphics, int bx, int by, int number) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        float scale = number >= 10 ? 0.46F : 0.58F;
        float yOff = number >= 10 ? -1.8F : -2.0F;
        pose.translate(bx, by + yOff, 0);
        pose.scale(scale, scale, 1.0F);
        String s = String.valueOf(number);
        int nw = this.font.width(s);
        graphics.drawString(this.font, s, -nw / 2, 0, 0x111111, false);
        pose.popPose();
    }

    private void renderCueStick2D(GuiGraphics graphics, int cueX, int cueY, float angle, float power) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(cueX, cueY, 0.0F);
        poseStack.mulPose(Axis.ZP.rotation((float) (Math.PI - angle)));

        int pullback = (int) (14 + power * 24);
        int cueLength = 68;

        graphics.fill(-2, pullback, 2, pullback + 3, 0xFF1976D2);
        graphics.fill(-2, pullback + 3, 2, pullback + 6, 0xFFFFFFFF);
        graphics.fill(-2, pullback + 6, 2, pullback + 40, 0xFFDEB887);
        graphics.fill(-3, pullback + 40, 3, pullback + cueLength, 0xFF3E2723);
        graphics.renderOutline(-3, pullback + 40, 6, cueLength - 40, 0xFFD7CCC8);
        graphics.fill(-2, pullback + cueLength, 2, pullback + cueLength + 3, 0xFF111111);

        poseStack.popPose();
    }

    private void renderVerticalPowerMeter(GuiGraphics graphics, int mx, int my, int mw, int mh) {
        graphics.fill(mx - 2, my - 2, mx + mw + 2, my + mh + 2, 0xFF3E2723);
        graphics.renderOutline(mx - 2, my - 2, mw + 4, mh + 4, 0xFFFFD700);

        graphics.fill(mx, my, mx + mw, my + mh, 0xFF10141D);

        for (int step = 1; step <= 3; step++) {
            int tickY = my + (int) (mh * (1.0F - step * 0.25F));
            graphics.fill(mx + 1, tickY, mx + mw - 1, tickY + 1, 0x44FFFFFF);
        }

        int fillH = (int) (shotPower * (mh - 4));
        if (fillH > 0) {
            int pColor = (shotPower > 0.75F) ? 0xFFE53935 : ((shotPower > 0.4F) ? 0xFFFB8C00 : 0xFF43A047);
            graphics.fill(mx + 2, my + mh - 2 - fillH, mx + mw - 2, my + mh - 2, pColor);
            graphics.fill(mx, my + mh - 2 - fillH - 1, mx + mw, my + mh - 2 - fillH + 1, 0xFFFFFFFF);
        }

        graphics.drawCenteredString(this.font, (int) (shotPower * 100) + "%", mx + mw / 2, my - 11, 0xFFFFD700);
    }

    private void renderScoreboardHUD(GuiGraphics graphics, int screenW) {
        int hudW = Math.min(380, screenW - 24);
        int hudX = (screenW - hudW) / 2;
        int cardH = 22;
        int cardY = 4;
        int gap = 8;
        int cardW = (hudW - gap * 2) / 3;

        boolean isP1Active = (currentTurn == 1);
        int p1CardX = hudX;
        graphics.fill(p1CardX, cardY, p1CardX + cardW, cardY + cardH, 0xEE121722);
        graphics.renderOutline(p1CardX, cardY, cardW, cardH, isP1Active ? 0xFFFFD700 : 0x55555555);
        String p1SuitStr = (p1Suit == PoolTableBlockEntity.SUIT_SOLIDS
                ? (" ● " + Component.translatable("awakening_system.casino.pool.suit_solids").getString())
                : (p1Suit == PoolTableBlockEntity.SUIT_STRIPES
                ? (" ◍ " + Component.translatable("awakening_system.casino.pool.suit_stripes").getString())
                : ""));
        graphics.drawCenteredString(this.font, "§e" + player1Name, p1CardX + cardW / 2, cardY + 2, 0xFFFFFF);
        String p1Sub = p1SuitStr.isEmpty() ? Component.translatable("awakening_system.casino.pool.open_table").getString() : p1SuitStr;
        graphics.drawCenteredString(this.font, "§7" + p1Sub, p1CardX + cardW / 2, cardY + 12, 0xCCCCCC);

        int centerCardX = hudX + cardW + gap;
        graphics.fill(centerCardX, cardY, centerCardX + cardW, cardY + cardH, 0xEE161D2B);
        graphics.renderOutline(centerCardX, cardY, cardW, cardH, 0xFFFFD700);
        if (betAmount > 0) {
            String potText = isIllegal
                    ? Component.translatable("awakening_system.casino.pool.pot_illegal", (betAmount * 2)).getString()
                    : Component.translatable("awakening_system.casino.pool.pot_normal", String.format(Locale.US, "%,d", (long) betAmount * 2)).getString();
            graphics.drawCenteredString(this.font, potText, centerCardX + cardW / 2, cardY + 2, isIllegal ? 0xFFA855F7 : 0xFFFFD700);
        } else {
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.pool.friendly_match").getString(), centerCardX + cardW / 2, cardY + 2, 0xAAAAAA);
        }
        String turnText;
        if (gameStatus == PoolTableBlockEntity.STATUS_WAITING_P2) {
            turnText = Component.translatable("awakening_system.casino.pool.waiting_p2").getString();
        } else if (isMyTurn()) {
            turnText = Component.translatable("awakening_system.casino.pool.your_turn").getString();
        } else {
            turnText = (currentTurn == 1)
                    ? Component.translatable("awakening_system.casino.pool.turn_p1", player1Name).getString()
                    : Component.translatable("awakening_system.casino.pool.turn_p2", player2Name).getString();
        }
        graphics.drawCenteredString(this.font, turnText, centerCardX + cardW / 2, cardY + 12, 0xFFFFFF);

        boolean isP2Active = (currentTurn == 2);
        int p2CardX = hudX + (cardW + gap) * 2;
        graphics.fill(p2CardX, cardY, p2CardX + cardW, cardY + cardH, 0xEE121722);
        graphics.renderOutline(p2CardX, cardY, cardW, cardH, isP2Active ? 0xFF00E5FF : 0x55555555);
        String p2SuitStr = (p2Suit == PoolTableBlockEntity.SUIT_SOLIDS
                ? (" ● " + Component.translatable("awakening_system.casino.pool.suit_solids").getString())
                : (p2Suit == PoolTableBlockEntity.SUIT_STRIPES
                ? (" ◍ " + Component.translatable("awakening_system.casino.pool.suit_stripes").getString())
                : ""));
        graphics.drawCenteredString(this.font, "§b" + player2Name, p2CardX + cardW / 2, cardY + 2, 0xFFFFFF);
        String p2Sub = p2SuitStr.isEmpty() ? Component.translatable("awakening_system.casino.pool.open_table").getString() : p2SuitStr;
        graphics.drawCenteredString(this.font, "§7" + p2Sub, p2CardX + cardW / 2, cardY + 12, 0xCCCCCC);

        if (statusMessage != null && !statusMessage.trim().isEmpty()) {
            int pillW = this.font.width(statusMessage) + 16;
            int pillX = (screenW - pillW) / 2;
            int pillY = cardY + cardH + 4;
            graphics.fill(pillX, pillY, pillX + pillW, pillY + 13, 0xF2121724);
            graphics.renderOutline(pillX, pillY, pillW, 13, 0x88D4AF37);
            graphics.drawCenteredString(this.font, statusMessage, screenW / 2, pillY + 3, 0xFFFFEE);
        }
    }

    private void renderPersonalRecordPill(GuiGraphics g, int screenW) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        SystemData sData = SystemEvents.data(this.minecraft.player);
        long totalWon = sData.getCasinoTotalWon();
        int maxPay = sData.getCasinoMaxPayout();
        Component recordComp = Component.translatable("awakening_system.casino.pool.record_pill",
                String.format(Locale.US, "%,d", totalWon),
                String.format(Locale.US, "%,d", (long) maxPay));
        int textW = this.font.width(recordComp);
        int pillX = screenW - textW - 20;
        int pillY = 6;
        g.fill(pillX, pillY, pillX + textW + 12, pillY + 13, 0xEE121820);
        g.renderOutline(pillX, pillY, textW + 12, 13, 0x88D4AF37);
        g.drawString(this.font, recordComp, pillX + 6, pillY + 3, 0xFFFFFF);
    }

    private void renderBottomControls(GuiGraphics graphics, int mouseX, int mouseY, int tx, int ty, int tw, boolean canShoot) {
        String myName = (this.minecraft != null && this.minecraft.player != null) ? this.minecraft.player.getName().getString() : "";
        boolean isWaitingP2 = (gameMode == PoolTableBlockEntity.MODE_PVP && gameStatus == PoolTableBlockEntity.STATUS_WAITING_P2);

        if (isWaitingP2) {
            boolean isP1 = player1Name.equals(myName) || player1Name.equals("Jugador 1");
            if (!isP1) {
                int joinW = 220;
                int joinH = 20;
                int joinX = (this.width - joinW) / 2;
                int joinY = ty;
                boolean hoverJoin = mouseX >= joinX && mouseX <= joinX + joinW && mouseY >= joinY && mouseY <= joinY + joinH;
                graphics.fill(joinX, joinY, joinX + joinW, joinY + joinH, hoverJoin ? 0xFF2E7D32 : 0xFF1B5E20);
                graphics.renderOutline(joinX, joinY, joinW, joinH, 0xFFFFD700);
                String joinLabel = (betAmount > 0)
                        ? (isIllegal
                        ? Component.translatable("awakening_system.casino.pool.join_p2_illegal", betAmount).getString()
                        : Component.translatable("awakening_system.casino.pool.join_p2_chips", String.format(Locale.US, "%,d", (long) betAmount)).getString())
                        : Component.translatable("awakening_system.casino.pool.join_p2_free").getString();
                graphics.drawCenteredString(this.font, joinLabel, joinX + joinW / 2, joinY + 6, 0xFFFFFF);
            } else {
                int waitW = 210;
                int waitH = 20;
                int waitX = (this.width - waitW - 80) / 2;
                int waitY = ty;
                graphics.fill(waitX, waitY, waitX + waitW, waitY + waitH, 0xEE161D2B);
                graphics.renderOutline(waitX, waitY, waitW, waitH, 0xFFFFA000);
                graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.pool.waiting_p2_banner").getString(), waitX + waitW / 2, waitY + 6, 0xFFFFD700);

                int optW = 75;
                int optH = 20;
                int optX = waitX + waitW + 6;
                int optY = ty;
                boolean hoverOpt = mouseX >= optX && mouseX <= optX + optW && mouseY >= optY && mouseY <= optY + optH;
                graphics.fill(optX, optY, optX + optW, optY + optH, hoverOpt ? 0xFF4A148C : 0xFF311B92);
                graphics.renderOutline(optX, optY, optW, optH, 0xFFBA68C8);
                graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.pool.btn_options").getString(), optX + optW / 2, optY + 6, 0xFFFFFF);
            }
            return;
        }

        if (gameStatus >= PoolTableBlockEntity.STATUS_P1_WON && gameStatus <= PoolTableBlockEntity.STATUS_SCRATCH_LOSS) {
            int restartW = 160;
            int optW = 105;
            int totalFinW = restartW + 10 + optW;
            int finStartX = (this.width - totalFinW) / 2;
            int finY = ty - 2;

            graphics.fill(finStartX - 8, finY - 6, finStartX + totalFinW + 8, finY + 26, 0xF5101420);
            graphics.renderOutline(finStartX - 8, finY - 6, totalFinW + 16, 32, 0xFFFFD700);

            boolean hoverRestart = mouseX >= finStartX && mouseX <= finStartX + restartW && mouseY >= finY && mouseY <= finY + 20;
            graphics.fill(finStartX, finY, finStartX + restartW, finY + 20, hoverRestart ? 0xFF2E7D32 : 0xFF1B5E20);
            graphics.renderOutline(finStartX, finY, restartW, 20, 0xFFFFD700);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.pool.btn_play_again").getString(), finStartX + restartW / 2, finY + 6, 0xFFFFFF);

            int finOptX = finStartX + restartW + 10;
            boolean hoverFinOpt = mouseX >= finOptX && mouseX <= finOptX + optW && mouseY >= finY && mouseY <= finY + 20;
            graphics.fill(finOptX, finY, finOptX + optW, finY + 20, hoverFinOpt ? 0xFF4A148C : 0xFF311B92);
            graphics.renderOutline(finOptX, finY, optW, 20, 0xFFBA68C8);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.pool.btn_options").getString(), finOptX + optW / 2, finY + 6, 0xFFFFFF);
            return;
        }

        int barW = 110;
        int barH = 14;
        int btnQuick = 18;
        int totalControlsW = 70 + 6 + btnQuick + 4 + barW + 4 + btnQuick + 6 + 75;
        int startX = (this.width - totalControlsW) / 2;
        int ctrlY = ty;

        graphics.fill(startX - 8, ctrlY - 6, startX + totalControlsW + 8, ctrlY + 24, 0xF5101420);
        graphics.renderOutline(startX - 8, ctrlY - 6, totalControlsW + 16, 30, 0x66D4AF37);

        int optW = 70;
        int optH = 18;
        int optX = startX;
        int optY = ctrlY - 2;
        boolean hoverOpt = mouseX >= optX && mouseX <= optX + optW && mouseY >= optY && mouseY <= optY + optH;
        graphics.fill(optX, optY, optX + optW, optY + optH, hoverOpt ? 0xFF4A148C : 0xFF311B92);
        graphics.renderOutline(optX, optY, optW, optH, 0xFFBA68C8);
        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.pool.btn_options").getString(), optX + optW / 2, optY + 5, 0xFFFFFF);

        int minusX = optX + optW + 6;
        int minusY = ctrlY - 2;
        boolean hoverMinus = mouseX >= minusX && mouseX <= minusX + btnQuick && mouseY >= minusY && mouseY <= minusY + btnQuick;
        graphics.fill(minusX, minusY, minusX + btnQuick, minusY + btnQuick, hoverMinus ? 0xFF455A64 : 0xFF263238);
        graphics.renderOutline(minusX, minusY, btnQuick, btnQuick, 0xFF90A4AE);
        graphics.drawCenteredString(this.font, "§l-", minusX + btnQuick / 2, minusY + 5, 0xFFFFFF);

        int barX = minusX + btnQuick + 4;
        int barY = ctrlY;
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xAA000000);
        graphics.renderOutline(barX, barY, barW, barH, 0xFFFFD700);
        int fillW = (int) (shotPower * (barW - 4));
        if (fillW > 0) {
            int pColor = (shotPower > 0.75F) ? 0xFFE53935 : ((shotPower > 0.4F) ? 0xFFFB8C00 : 0xFF43A047);
            graphics.fill(barX + 2, barY + 2, barX + 2 + fillW, barY + barH - 2, pColor);
        }
        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.pool.power_wheel", (int) (shotPower * 100)).getString(), barX + barW / 2, barY + 3, 0xFFFFFF);

        int plusX = barX + barW + 4;
        int plusY = ctrlY - 2;
        boolean hoverPlus = mouseX >= plusX && mouseX <= plusX + btnQuick && mouseY >= plusY && mouseY <= plusY + btnQuick;
        graphics.fill(plusX, plusY, plusX + btnQuick, plusY + btnQuick, hoverPlus ? 0xFF455A64 : 0xFF263238);
        graphics.renderOutline(plusX, plusY, btnQuick, btnQuick, 0xFF90A4AE);
        graphics.drawCenteredString(this.font, "§l+", plusX + btnQuick / 2, plusY + 5, 0xFFFFFF);

        int shootW = 75;
        int shootH = 18;
        int shootX = plusX + btnQuick + 6;
        int shootY = ctrlY - 2;
        boolean hoverShoot = mouseX >= shootX && mouseX <= shootX + shootW && mouseY >= shootY && mouseY <= shootY + shootH;
        graphics.fill(shootX, shootY, shootX + shootW, shootY + shootH, canShoot ? (hoverShoot ? 0xFF2E7D32 : 0xFF1B5E20) : 0xFF37474F);
        graphics.renderOutline(shootX, shootY, shootW, shootH, canShoot ? 0xFF81C784 : 0xFF546E7A);
        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.casino.pool.btn_shoot").getString(), shootX + shootW / 2, shootY + 5, canShoot ? 0xFFFFFF : 0x90A4AE);
    }

    public float getModalScale() {
        float scaleX = (float) this.width / 450.0F;
        float scaleY = (float) this.height / 220.0F;
        return Math.min(1.0F, Math.max(0.35F, Math.min(scaleX, scaleY)));
    }

    public double transformModalMouseX(double mouseX) {
        float scale = getModalScale();
        if (scale >= 1.0F) return mouseX;
        double cx = this.width / 2.0;
        return (mouseX - cx) / scale + cx;
    }

    public double transformModalMouseY(double mouseY) {
        float scale = getModalScale();
        if (scale >= 1.0F) return mouseY;
        double cy = this.height / 2.0;
        return (mouseY - cy) / scale + cy;
    }

    private void renderSettingsModal(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(0, 0, this.width, this.height, 0xB0000000);

        float scale = getModalScale();
        int mMouseX = (int) Math.round(transformModalMouseX(mouseX));
        int mMouseY = (int) Math.round(transformModalMouseY(mouseY));

        graphics.pose().pushPose();
        if (scale < 1.0F) {
            float cxF = this.width / 2.0F;
            float cyF = this.height / 2.0F;
            graphics.pose().translate(cxF, cyF, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.pose().translate(-cxF, -cyF, 0.0F);
        }

        int mW = 430;
        int mH = 200;
        int mX = (this.width - mW) / 2;
        int mY = Math.max(16, (this.height - mH) / 2);

        graphics.fill(mX - 4, mY - 4, mX + mW + 4, mY + mH + 4, 0x88000000);
        graphics.fill(mX, mY, mX + mW, mY + mH, 0xFF0D121B);
        graphics.renderOutline(mX, mY, mW, mH, isIllegal ? 0xFFA855F7 : 0xFFFFD700);

        String modalTitle = isIllegal
                ? Component.translatable("awakening_system.casino.pool.modal_title_illegal").getString()
                : Component.translatable("awakening_system.casino.pool.modal_title_normal").getString();
        graphics.drawCenteredString(this.font, modalTitle, mX + mW / 2, mY + 12, 0xFFFFFF);

        graphics.drawString(this.font, Component.translatable("awakening_system.casino.pool.modal_game_mode").getString(), mX + 22, mY + 30, 0xCCCCCC);
        String[] modes = isIllegal
                ? new String[]{
                        Component.translatable("awakening_system.casino.pool.mode_shark").getString(),
                        Component.translatable("awakening_system.casino.pool.mode_hustler").getString(),
                        Component.translatable("awakening_system.casino.pool.mode_pvp").getString()
                }
                : new String[]{
                        Component.translatable("awakening_system.casino.pool.mode_ai_normal").getString(),
                        Component.translatable("awakening_system.casino.pool.mode_ai_master").getString(),
                        Component.translatable("awakening_system.casino.pool.mode_pvp").getString()
                };
        for (int m = 0; m < 3; m++) {
            int bX = mX + 22 + m * 130;
            int bY = mY + 44;
            boolean selected = (selectedMode == m);
            graphics.fill(bX, bY, bX + 126, bY + 20, selected ? (isIllegal ? 0xFF6B21A8 : 0xFF2E7D32) : 0xFF263238);
            graphics.renderOutline(bX, bY, 126, 20, selected ? (isIllegal ? 0xFFA855F7 : 0xFF81C784) : 0xFF455A64);
            graphics.drawCenteredString(this.font, modes[m], bX + 63, bY + 6, 0xFFFFFF);
        }

        String betHeader = isIllegal
                ? Component.translatable("awakening_system.casino.pool.modal_bet_illegal").getString()
                : Component.translatable("awakening_system.casino.pool.modal_bet_normal").getString();
        graphics.drawString(this.font, betHeader, mX + 22, mY + 70, 0xCCCCCC);
        int[] curBets = getCurrentBetOptions();
        for (int b = 0; b < curBets.length; b++) {
            int bX, bY, bW;
            if (b < 4) {
                bX = mX + 22 + b * 98;
                bY = mY + 84;
                bW = 90;
            } else {
                bX = mX + 22 + (b - 4) * 131;
                bY = mY + 106;
                bW = 124;
            }
            boolean selected = (selectedBet == curBets[b]);
            graphics.fill(bX, bY, bX + bW, bY + 18, selected ? (isIllegal ? 0xFF6B21A8 : 0xFFF57F17) : 0xFF263238);
            graphics.renderOutline(bX, bY, bW, 18, selected ? (isIllegal ? 0xFFA855F7 : 0xFFFFD700) : 0xFF455A64);
            graphics.drawCenteredString(this.font, getBetLabel(b), bX + bW / 2, bY + 5, 0xFFFFFF);
        }

        int totalPot = selectedBet * 2;
        String info = isIllegal
                ? (selectedBet > 0
                        ? Component.translatable("awakening_system.casino.pool.dispute_pot_illegal", totalPot).getString()
                        : Component.translatable("awakening_system.casino.pool.no_bet_match").getString())
                : (selectedBet > 0
                        ? Component.translatable("awakening_system.casino.pool.dispute_pot_normal", String.format(Locale.US, "%,d", (long) totalPot)).getString()
                        : Component.translatable("awakening_system.casino.pool.no_bet_match").getString());
        graphics.drawCenteredString(this.font, "§7" + info, mX + mW / 2, mY + 132, 0xCCCCCC);

        int startBtnW = 180;
        int startBtnH = 22;
        int startBtnX = mX + (mW - startBtnW) / 2;
        int startBtnY = mY + mH - 32;
        boolean hoverStart = mMouseX >= startBtnX && mMouseX <= startBtnX + startBtnW && mMouseY >= startBtnY && mMouseY <= startBtnY + startBtnH;

        graphics.fill(startBtnX, startBtnY, startBtnX + startBtnW, startBtnY + startBtnH, hoverStart ? (isIllegal ? 0xFF581C87 : 0xFF1B5E20) : (isIllegal ? 0xFF3B0764 : 0xFF0D3311));
        graphics.renderOutline(startBtnX, startBtnY, startBtnW, startBtnH, isIllegal ? 0xFFA855F7 : 0xFFFFD700);
        String startBtnText = isIllegal
                ? Component.translatable("awakening_system.casino.pool.btn_start_illegal").getString()
                : Component.translatable("awakening_system.casino.pool.btn_start_normal").getString();
        graphics.drawCenteredString(this.font, startBtnText, startBtnX + startBtnW / 2, startBtnY + 7, 0xFFFFFF);

        int closeX = mX + mW - 18;
        int closeY = mY + 10;
        graphics.drawString(this.font, "§c✕", closeX, closeY, 0xFF5555);

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!showSettingsModal && isMyTurn() && gameStatus == PoolTableBlockEntity.STATUS_READY) {
            this.shotPower = Math.max(0.05F, Math.min(1.0F, this.shotPower + (float) scrollY * 0.05F));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (showSettingsModal) {
                mouseX = transformModalMouseX(mouseX);
                mouseY = transformModalMouseY(mouseY);
                int mW = 430;
                int mH = 200;
                int mX = (this.width - mW) / 2;
                int mY = Math.max(16, (this.height - mH) / 2);

                if (mouseX >= mX + mW - 24 && mouseX <= mX + mW && mouseY >= mY && mouseY <= mY + 24) {
                    showSettingsModal = false;
                    return true;
                }

                for (int m = 0; m < 3; m++) {
                    int bX = mX + 22 + m * 130;
                    int bY = mY + 44;
                    if (mouseX >= bX && mouseX <= bX + 126 && mouseY >= bY && mouseY <= bY + 20) {
                        selectedMode = m;
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
                }

                int[] curBets = getCurrentBetOptions();
                for (int b = 0; b < curBets.length; b++) {
                    int bX, bY, bW;
                    if (b < 4) {
                        bX = mX + 22 + b * 98;
                        bY = mY + 84;
                        bW = 90;
                    } else {
                        bX = mX + 22 + (b - 4) * 131;
                        bY = mY + 106;
                        bW = 124;
                    }
                    if (mouseX >= bX && mouseX <= bX + bW && mouseY >= bY && mouseY <= bY + 18) {
                        selectedBet = curBets[b];
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
                }

                int startBtnW = 180;
                int startBtnH = 22;
                int startBtnX = mX + (mW - startBtnW) / 2;
                int startBtnY = mY + mH - 32;
                if (mouseX >= startBtnX && mouseX <= startBtnX + startBtnW && mouseY >= startBtnY && mouseY <= startBtnY + startBtnH) {
                    PacketDistributor.sendToServer(new PoolTableActionC2SPayload(
                            tablePos, PoolTableActionC2SPayload.ACTION_RESET_GAME, 0.0F, 0.0F, selectedMode, selectedBet
                    ));
                    showSettingsModal = false;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                    return true;
                }
                return true;
            }

            int tableY = getTableY();
            int tableH = getTableH();
            int tableW = getTableW();
            int tableX = getTableX();

            int meterX = tableX + tableW + 16;
            int meterY = tableY;
            int meterW = 14;
            int meterH = tableH;
            if (mouseX >= meterX && mouseX <= meterX + meterW && mouseY >= meterY && mouseY <= meterY + meterH) {
                isDraggingVerticalMeter = true;
                this.shotPower = Math.max(0.05F, Math.min(1.0F, (float) (meterY + meterH - mouseY) / (float) meterH));
                return true;
            }

            String myName = (this.minecraft != null && this.minecraft.player != null) ? this.minecraft.player.getName().getString() : "";
            boolean isWaitingP2 = (gameMode == PoolTableBlockEntity.MODE_PVP && gameStatus == PoolTableBlockEntity.STATUS_WAITING_P2);
            int ctrlY = tableY + tableH + 16;

            if (isWaitingP2) {
                boolean isP1 = player1Name.equals(myName) || player1Name.equals("Jugador 1");
                if (!isP1) {
                    int joinW = 220;
                    int joinH = 20;
                    int joinX = (this.width - joinW) / 2;
                    if (mouseX >= joinX && mouseX <= joinX + joinW && mouseY >= ctrlY && mouseY <= ctrlY + joinH) {
                        PacketDistributor.sendToServer(new PoolTableActionC2SPayload(
                                tablePos, PoolTableActionC2SPayload.ACTION_JOIN_P2, 0.0F, 0.0F, 0, 0
                        ));
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0F));
                        return true;
                    }
                } else {
                    int waitW = 210;
                    int optW = 75;
                    int optH = 20;
                    int waitX = (this.width - waitW - 80) / 2;
                    int optX = waitX + waitW + 6;
                    if (mouseX >= optX && mouseX <= optX + optW && mouseY >= ctrlY && mouseY <= ctrlY + optH) {
                        showSettingsModal = true;
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            if (gameStatus >= PoolTableBlockEntity.STATUS_P1_WON && gameStatus <= PoolTableBlockEntity.STATUS_SCRATCH_LOSS) {
                int restartW = 160;
                int optW = 105;
                int totalFinW = restartW + 10 + optW;
                int finStartX = (this.width - totalFinW) / 2;
                int finY = ctrlY - 2;

                if (mouseX >= finStartX && mouseX <= finStartX + restartW && mouseY >= finY && mouseY <= finY + 20) {
                    PacketDistributor.sendToServer(new PoolTableActionC2SPayload(
                            tablePos, PoolTableActionC2SPayload.ACTION_RESET_GAME, 0.0F, 0.0F, gameMode, betAmount
                    ));
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                    return true;
                }

                int finOptX = finStartX + restartW + 10;
                if (mouseX >= finOptX && mouseX <= finOptX + optW && mouseY >= finY && mouseY <= finY + 20) {
                    showSettingsModal = true;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            int barW = 110;
            int barH = 14;
            int btnQuick = 18;
            int totalControlsW = 70 + 6 + btnQuick + 4 + barW + 4 + btnQuick + 6 + 75;
            int startX = (this.width - totalControlsW) / 2;

            int optW = 70;
            int optH = 18;
            int optX = startX;
            int optY = ctrlY - 2;
            if (mouseX >= optX && mouseX <= optX + optW && mouseY >= optY && mouseY <= optY + optH) {
                showSettingsModal = true;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            int minusX = optX + optW + 6;
            int minusY = ctrlY - 2;
            if (mouseX >= minusX && mouseX <= minusX + btnQuick && mouseY >= minusY && mouseY <= minusY + btnQuick) {
                this.shotPower = Math.max(0.05F, Math.min(1.0F, this.shotPower - 0.10F));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.9F));
                return true;
            }

            int barX = minusX + btnQuick + 4;
            int barY = ctrlY;
            if (mouseX >= barX && mouseX <= barX + barW && mouseY >= barY && mouseY <= barY + barH) {
                isDraggingPower = true;
                this.shotPower = Math.max(0.05F, Math.min(1.0F, (float) (mouseX - barX) / (float) barW));
                return true;
            }

            int plusX = barX + barW + 4;
            int plusY = ctrlY - 2;
            if (mouseX >= plusX && mouseX <= plusX + btnQuick && mouseY >= plusY && mouseY <= plusY + btnQuick) {
                this.shotPower = Math.max(0.05F, Math.min(1.0F, this.shotPower + 0.10F));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                return true;
            }

            int shootW = 75;
            int shootH = 18;
            int shootX = plusX + btnQuick + 6;
            int shootY = ctrlY - 2;
            if (mouseX >= shootX && mouseX <= shootX + shootW && mouseY >= shootY && mouseY <= shootY + shootH) {
                triggerShot();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int tableY = getTableY();
        int tableH = getTableH();
        int tableW = getTableW();
        int tableX = getTableX();

        if (isDraggingVerticalMeter) {
            this.shotPower = Math.max(0.05F, Math.min(1.0F, (float) (tableY + tableH - mouseY) / (float) tableH));
            return true;
        }

        if (isDraggingPower) {
            int barW = 110;
            int btnQuick = 18;
            int totalControlsW = 70 + 6 + btnQuick + 4 + barW + 4 + btnQuick + 6 + 75;
            int startX = (this.width - totalControlsW) / 2;
            int barX = startX + 70 + 6 + btnQuick + 4;
            this.shotPower = Math.max(0.05F, Math.min(1.0F, (float) (mouseX - barX) / (float) barW));
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDraggingPower) {
            isDraggingPower = false;
        }
        if (isDraggingVerticalMeter) {
            isDraggingVerticalMeter = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showSettingsModal) {
            if (keyCode == 256) {
                showSettingsModal = false;
                return true;
            }
            return true;
        }
        if (keyCode == 32) {
            triggerShot();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void triggerShot() {
        if (isMyTurn() && gameStatus == PoolTableBlockEntity.STATUS_READY) {
            float power = Math.max(0.1F, shotPower);
            PacketDistributor.sendToServer(new PoolTableActionC2SPayload(
                    tablePos, PoolTableActionC2SPayload.ACTION_SHOOT, aimAngle, power, 0, 0
            ));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BONE_BLOCK_HIT, 1.35F));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.POINTED_DRIPSTONE_HIT, 1.25F));
        }
    }
}
