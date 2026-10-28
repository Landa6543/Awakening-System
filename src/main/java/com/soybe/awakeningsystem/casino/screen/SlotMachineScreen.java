package com.soybe.awakeningsystem.casino.screen;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.casino.network.CloseSlotMachineC2SPayload;
import com.soybe.awakeningsystem.casino.network.SlotSpinC2SPayload;
import com.soybe.awakeningsystem.casino.slot.SlotLineResult;
import com.soybe.awakeningsystem.casino.slot.SlotLines;
import com.soybe.awakeningsystem.casino.slot.SlotReels;
import com.soybe.awakeningsystem.casino.slot.SlotSymbol;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class SlotMachineScreen extends Screen {

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/slot_machine_gui.png");
    private static final ResourceLocation REELS_TEXTURE = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/misc/reels.png");
    private static final ResourceLocation MORA_ICON = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/item/mora.png");

    private static final ResourceLocation BTN_SPIN = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/buttons/spin_button.png");
    private static final ResourceLocation BTN_BET = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/buttons/bet_button.png");
    private static final ResourceLocation BTN_MENU = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/buttons/menu_button.png");

    private static final Map<SlotSymbol, ResourceLocation> SYMBOL_TEXTURES = new EnumMap<>(SlotSymbol.class);

    static {
        for (SlotSymbol s : SlotSymbol.values()) {
            SYMBOL_TEXTURES.put(s, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID,
                    "textures/gui/casino/symbols/" + s.name().toLowerCase(Locale.ROOT) + ".png"));
        }
    }

    private final BlockPos machinePos;
    private long balance;
    private long pendingBalance = -1L;
    private int betBase = 1;
    private int linesMode = 1;

    private boolean isIllegal = false;
    private int chipsAvailable = 0;
    private int reserveChips = 0;
    private boolean isOwner = false;

    private static final int[] CHIP_BET_PRESETS = { 1, 2, 3, 5, 10, 20 };
    private int betPresetIndex = 0;

    private List<SlotLineResult> lastWins = List.of();
    private long lastWinAmount = 0;
    private long pendingWinAmount = -1;
    private boolean lastSpinHadWin = false;

    private SlotButton spinButton;
    private SlotButton betButton;
    private SlotButton linesButton;
    private SlotButton closeButton;

    private SlotLineSprite lineOneSprite;
    private SlotLineSprite lineTwoSprite;
    private SlotLineSprite lineThreeTopSprite;
    private SlotLineSprite lineThreeBottomSprite;

    private enum WinLine {
        CENTER, TOP, BOTTOM, DIAG_TOP, DIAG_BOTTOM
    }

    private final Set<WinLine> flashingLines = EnumSet.noneOf(WinLine.class);
    private final Set<WinLine> pendingFlashingLines = EnumSet.noneOf(WinLine.class);
    private int pendingFlashTicks = 0;
    private boolean flashVisible = true;
    private int flashTicksRemaining = 0;
    private int flashTickCounter = 0;

    private final int backgroundWidth = 220;
    private final int backgroundHeight = 205;
    private static final int SYMBOL_SIZE = 32;
    private static final int[] COLUMN_X = { 55, 94, 133 };
    private static final int ROW_Y = 53;
    private static final int VIRTUAL_ROWS = 5;

    private boolean isSpinning = false;
    private boolean isWaitingForServer = false;
    private SimpleSoundInstance spinSoundInstance = null;
    private final int[] reelIndex = new int[3];
    private final int[] targetTopIndex = new int[3];
    private final float[] reelOffset = new float[3];
    private final float[] reelBounce = new float[3];
    private final int[] reelTimer = new int[3];
    private final boolean[] reelSpinning = new boolean[3];
    private static final float REEL_VELOCITY = 16.0f;
    private static final int[] REEL_TICKS = { 32, 46, 60 };
    private static final int[] REEL_STEPS = { 16, 23, 30 };

    private final DancingPaimon paimonLeft = new DancingPaimon();
    private final DancingPaimon paimonRight = new DancingPaimon();

    public SlotMachineScreen(BlockPos machinePos, long initialBalance, int betBase, int linesMode) {
        this(machinePos, initialBalance, betBase, linesMode, false, (int) initialBalance, 0, false);
    }

    public SlotMachineScreen(BlockPos machinePos, long initialBalance, int betBase, int linesMode, boolean isIllegal, int chipsAvailable, int reserveChips, boolean isOwner) {
        super(isIllegal ? Component.translatable("awakening_system.screen.slot_machine.title_clandestine") : Component.translatable("awakening_system.screen.slot_machine.title_celestial"));
        this.machinePos = machinePos;
        this.balance = initialBalance;
        this.isIllegal = isIllegal;
        this.chipsAvailable = chipsAvailable;
        this.reserveChips = reserveChips;
        this.isOwner = isOwner;
        this.linesMode = Math.max(1, Math.min(3, linesMode));

        int matchedIdx = 0;
        for (int i = 0; i < CHIP_BET_PRESETS.length; i++) {
            if (CHIP_BET_PRESETS[i] == betBase) {
                matchedIdx = i;
                break;
            }
        }
        this.betPresetIndex = matchedIdx;
        this.betBase = CHIP_BET_PRESETS[matchedIdx];
    }

    @Override
    protected void init() {
        super.init();
        int originX = (this.width - this.backgroundWidth) / 2;
        int originY = (this.height - this.backgroundHeight) / 2;

        this.lineOneSprite = new SlotLineSprite(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/lines/line_one.png"), 160, 14);
        this.lineTwoSprite = new SlotLineSprite(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/lines/line_two.png"), 160, 14);
        this.lineThreeTopSprite = new SlotLineSprite(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/lines/line_three_top.png"), 160, 112);
        this.lineThreeBottomSprite = new SlotLineSprite(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/lines/line_three_bottom.png"), 160, 112);

        this.betButton = new SlotButton(originX + 8, originY + 168, 50, 26, BTN_BET, btn -> cycleBet(), Component.translatable("awakening_system.screen.slot_machine.btn_bet"));
        this.addRenderableWidget(this.betButton);

        this.linesButton = new SlotButton(originX + 63, originY + 168, 44, 26, BTN_MENU, btn -> cycleLines(), Component.translatable("awakening_system.screen.slot_machine.btn_lines"));
        this.addRenderableWidget(this.linesButton);

        this.closeButton = new SlotButton(originX + 112, originY + 168, 44, 26, BTN_MENU, btn -> onClose(), Component.translatable("awakening_system.screen.slot_machine.btn_exit"));
        this.addRenderableWidget(this.closeButton);

        this.spinButton = new SlotButton(originX + 161, originY + 168, 54, 26, BTN_SPIN, btn -> doSpin(), Component.translatable("awakening_system.screen.slot_machine.btn_spin"));
        this.addRenderableWidget(this.spinButton);

        updateButtonsState();
    }

    private void cycleBet() {
        if (isSpinning || isWaitingForServer) return;
        betPresetIndex = (betPresetIndex + 1) % CHIP_BET_PRESETS.length;
        this.betBase = CHIP_BET_PRESETS[betPresetIndex];
        updateButtonsState();
    }

    private void cycleLines() {
        if (isSpinning || isWaitingForServer) return;
        this.linesMode = (this.linesMode % 3) + 1;
        updateButtonsState();
    }

    private void doSpin() {
        long totalBet = (long) betBase * SlotLines.getLineCount(linesMode);
        if (isSpinning || isWaitingForServer || chipsAvailable < totalBet) return;

        PacketDistributor.sendToServer(new SlotSpinC2SPayload(machinePos, betBase, linesMode));
        this.isWaitingForServer = true;
        this.chipsAvailable -= (int) totalBet;
        updateButtonsState();
    }

    public void onSpinResult(int stop1, int stop2, int stop3, long totalWin, long newBalance, int mode, boolean isJackpot, List<SlotLineResult> wins) {
        this.isWaitingForServer = false;
        this.lastWins = wins;
        this.linesMode = mode;
        this.pendingWinAmount = totalWin;
        this.pendingBalance = newBalance;
        this.chipsAvailable = (int) newBalance;
        this.lastSpinHadWin = totalWin > 0;

        pendingFlashingLines.clear();
        pendingFlashTicks = 0;

        if (!wins.isEmpty()) {
            for (SlotLineResult win : wins) {
                if (win.win()) {
                    pendingFlashingLines.add(mapWinToLine(win));
                }
            }
            pendingFlashTicks = isJackpot ? 160 : 80;
        }

        int len = SlotReels.REEL_SIZE;
        targetTopIndex[0] = Math.floorMod(stop1 - 2, len);
        targetTopIndex[1] = Math.floorMod(stop2 - 2, len);
        targetTopIndex[2] = Math.floorMod(stop3 - 2, len);

        startSpinAnimation();
    }

    private void startSpinAnimation() {
        this.isSpinning = true;
        updateButtonsState();

        if (spinSoundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(spinSoundInstance);
        }
        spinSoundInstance = SimpleSoundInstance.forUI(ModSounds.CASINO_REELS_SPINNING.get(), 1.0F);
        Minecraft.getInstance().getSoundManager().play(spinSoundInstance);

        int len = SlotReels.REEL_SIZE;
        for (int col = 0; col < 3; col++) {
            reelTimer[col] = REEL_TICKS[col];
            reelOffset[col] = 0.0F;
            reelSpinning[col] = true;
            reelIndex[col] = Math.floorMod(targetTopIndex[col] - REEL_STEPS[col], len);
        }
    }

    private void finishSpin() {
        this.isSpinning = false;
        this.isWaitingForServer = false;

        if (spinSoundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(spinSoundInstance);
            spinSoundInstance = null;
        }

        if (pendingBalance >= 0) {
            this.balance = pendingBalance;
            this.pendingBalance = -1L;
        }

        if (pendingWinAmount >= 0) {
            this.lastWinAmount = pendingWinAmount;
            this.pendingWinAmount = -1L;
        }

        flashingLines.clear();
        if (pendingFlashTicks > 0 && !pendingFlashingLines.isEmpty()) {
            flashingLines.addAll(pendingFlashingLines);
            flashTicksRemaining = pendingFlashTicks;
            flashVisible = true;
            flashTickCounter = 0;

            if (lastWinAmount >= (long) betBase * 50) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CASINO_JACKPOT.get(), 1.0F));
            } else if (lastWinAmount > 0) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CASINO_WIN.get(), 1.0F));
            }
        } else if (lastWinAmount == 0) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CASINO_LOSE.get(), 0.8F));
        }

        updateButtonsState();
    }

    @Override
    public void tick() {
        super.tick();

        if (flashTicksRemaining > 0) {
            flashTicksRemaining--;
            flashTickCounter++;
            if (flashTickCounter >= 5) {
                flashTickCounter = 0;
                flashVisible = !flashVisible;
            }
        } else {
            flashingLines.clear();
        }

        if (isSpinning) {
            boolean anySpinning = false;
            int len = SlotReels.REEL_SIZE;

            for (int col = 0; col < 3; col++) {
                if (reelSpinning[col]) {
                    reelTimer[col]--;
                    float vel = REEL_VELOCITY;
                    if (reelTimer[col] <= 8) {
                        vel = Math.max(3.0F, REEL_VELOCITY * (reelTimer[col] / 8.0F));
                    }
                    reelOffset[col] += vel;

                    if (reelOffset[col] >= SYMBOL_SIZE) {
                        reelOffset[col] -= SYMBOL_SIZE;
                        reelIndex[col] = (reelIndex[col] + 1) % len;
                    }

                    if (reelTimer[col] <= 0) {
                        reelOffset[col] = 0.0F;
                        reelSpinning[col] = false;
                        reelIndex[col] = targetTopIndex[col];
                        reelBounce[col] = 5.0F; // Rebote mecánico elástico al frenar
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.2F + col * 0.25F)
                        );
                    } else {
                        anySpinning = true;
                    }
                } else if (Math.abs(reelBounce[col]) > 0.3F) {
                    reelBounce[col] *= -0.45F;
                } else {
                    reelBounce[col] = 0.0F;
                }
            }

            if (!anySpinning) {
                finishSpin();
            }
        }

        DancingPaimon.Phase phase;
        if (isSpinning) {
            phase = DancingPaimon.Phase.NEUTRAL;
        } else if (lastWinAmount > 0 || flashTicksRemaining > 0) {
            phase = DancingPaimon.Phase.WIN;
        } else if (lastSpinHadWin) {
            phase = DancingPaimon.Phase.WIN;
        } else {
            phase = DancingPaimon.Phase.NEUTRAL;
        }

        paimonLeft.tick(phase);
        paimonRight.tick(phase);
    }

    private void updateButtonsState() {
        boolean busy = isSpinning || isWaitingForServer;
        long totalBet = (long) betBase * SlotLines.getLineCount(linesMode);
        boolean canAfford = chipsAvailable >= totalBet;

        if (spinButton != null) {
            spinButton.active = !busy && canAfford;
            spinButton.setForcedPressed(busy);
            spinButton.setMessage(busy ? Component.literal("...") : (canAfford ? Component.translatable("awakening_system.screen.slot_machine.btn_spin") : Component.translatable("awakening_system.screen.slot_machine.btn_no_chips")));
        }
        if (betButton != null) {
            betButton.active = !busy;
            betButton.setSubtitle(Component.literal(betBase + (isIllegal ? " 🟣" : " 🟡")));
        }
        if (linesButton != null) {
            linesButton.active = !busy;
            linesButton.setSubtitle(Component.literal(SlotLines.getLineCount(linesMode) + " / " + (linesMode == 3 ? "5" : "3")));
        }
        if (closeButton != null) {
            closeButton.active = !busy;
        }
    }

    private WinLine mapWinToLine(SlotLineResult win) {
        int idx = win.lineIndex();
        if (this.linesMode == 1) return WinLine.CENTER;
        if (this.linesMode == 2) {
            return switch (idx) {
                case 0 -> WinLine.TOP;
                case 1 -> WinLine.CENTER;
                case 2 -> WinLine.BOTTOM;
                default -> WinLine.CENTER;
            };
        }
        return switch (idx) {
            case 0 -> WinLine.TOP;
            case 1 -> WinLine.CENTER;
            case 2 -> WinLine.BOTTOM;
            case 3 -> WinLine.DIAG_TOP;
            case 4 -> WinLine.DIAG_BOTTOM;
            default -> WinLine.CENTER;
        };
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / (backgroundWidth + 30.0F);
        float scaleY = (float) this.height / (backgroundHeight + 35.0F);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(transformMouseX(mouseX), transformMouseY(mouseY), button);
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

    @Override
    public void renderTransparentBackground(GuiGraphics guiGraphics) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0x88000000, 0xA0000000);

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

        int originX = (this.width - this.backgroundWidth) / 2;
        int originY = (this.height - this.backgroundHeight) / 2;

        graphics.blit(REELS_TEXTURE, originX + 50, originY + 47, 0, 0, 120, 112, 120, 112);

        drawSymbols(graphics, originX, originY);

        graphics.blit(GUI_TEXTURE, originX, originY, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        drawLines(graphics, originX, originY);

        paimonLeft.render(graphics, originX + 7, originY + 131);
        paimonRight.render(graphics, originX + 191, originY + 131);

        super.render(graphics, mX, mY, partialTick);

        drawTexts(graphics, originX, originY);

        renderPersonalRecordPill(graphics, originX, originY);

        graphics.pose().popPose();
    }

    private void renderPersonalRecordPill(GuiGraphics g, int originX, int originY) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        if (isIllegal) {
            Component status = isOwner ? Component.translatable("awakening_system.screen.slot_machine.status_clandestine_owner") : Component.translatable("awakening_system.screen.slot_machine.status_clandestine_underworld");
            int textW = this.font.width(status);
            int pillX = originX + (backgroundWidth - textW - 12) / 2;
            int pillY = originY - 16;
            g.fill(pillX, pillY, pillX + textW + 12, pillY + 13, 0xEE1E0B36);
            g.renderOutline(pillX, pillY, textW + 12, 13, 0x88A855F7);
            g.drawString(this.font, status, pillX + 6, pillY + 3, 0xFFFFFF);
            return;
        }
        com.soybe.awakeningsystem.SystemData sData = com.soybe.awakeningsystem.SystemEvents.data(this.minecraft.player);
        long totalWon = sData.getCasinoTotalWon();
        int maxPay = sData.getCasinoMaxPayout();
        Component recordComp = Component.translatable("awakening_system.screen.slot_machine.record_pill",
                String.format(Locale.US, "%,d", totalWon), (isIllegal ? "🟣" : "🟡"),
                String.format(Locale.US, "%,d", (long) maxPay), (isIllegal ? "🟣" : "🟡"));
        int textW = this.font.width(recordComp);
        int pillX = originX + (backgroundWidth - textW - 12) / 2;
        int pillY = originY - 16;
        g.fill(pillX, pillY, pillX + textW + 12, pillY + 13, 0xEE121820);
        g.renderOutline(pillX, pillY, textW + 12, 13, 0x88D4AF37);
        g.drawString(this.font, recordComp, pillX + 6, pillY + 3, 0xFFFFFF);
    }

    private void drawSymbols(GuiGraphics graphics, int originX, int originY) {
        int len = SlotReels.REEL_SIZE;

        graphics.enableScissor(originX + 54, originY + 51, originX + 166, originY + 155);

        for (int col = 0; col < 3; col++) {
            SlotSymbol[] strip = SlotReels.STRIPS[col];

            for (int row = 0; row < VIRTUAL_ROWS; row++) {
                int stripIndex = (reelIndex[col] + row) % len;
                SlotSymbol symbol = strip[stripIndex];
                if (symbol == null) continue;

                ResourceLocation tex = SYMBOL_TEXTURES.get(symbol);
                if (tex == null) continue;

                int drawX = originX + COLUMN_X[col];
                int drawY = originY + ROW_Y + ((row - 1) * SYMBOL_SIZE) + (int) (reelOffset[col] + reelBounce[col]);

                graphics.blit(tex, drawX, drawY, 0, 0, SYMBOL_SIZE, SYMBOL_SIZE, SYMBOL_SIZE, SYMBOL_SIZE);
            }
        }

        graphics.disableScissor();
    }

    private void drawLines(GuiGraphics graphics, int originX, int originY) {
        if (lineOneSprite == null) return;

        int x = originX + 30;
        int yDiag = originY + 47;
        int yTop = originY + 70;
        int yMid = originY + 96;
        int yBottom = originY + 122;

        boolean centerOn = (linesMode >= 1);
        boolean horizontalsOn = (linesMode >= 2);
        boolean diagonalsOn = (linesMode >= 3);

        boolean centerWinning = flashingLines.contains(WinLine.CENTER);
        boolean topWinning = flashingLines.contains(WinLine.TOP);
        boolean bottomWinning = flashingLines.contains(WinLine.BOTTOM);
        boolean diagTopWinning = flashingLines.contains(WinLine.DIAG_TOP);
        boolean diagBottomWinning = flashingLines.contains(WinLine.DIAG_BOTTOM);

        boolean centerEnabled = centerOn && (!centerWinning || flashVisible);
        boolean topEnabled = horizontalsOn && (!topWinning || flashVisible);
        boolean bottomEnabled = horizontalsOn && (!bottomWinning || flashVisible);
        boolean diagTopEnabled = diagonalsOn && (!diagTopWinning || flashVisible);
        boolean diagBottomEnabled = diagonalsOn && (!diagBottomWinning || flashVisible);

        lineThreeTopSprite.render(graphics, x, yDiag, diagTopEnabled);
        lineTwoSprite.render(graphics, x, yTop, topEnabled);
        lineOneSprite.render(graphics, x, yMid, centerEnabled);
        lineTwoSprite.render(graphics, x, yBottom, bottomEnabled);
        lineThreeBottomSprite.render(graphics, x, yDiag, diagBottomEnabled);
    }

    private void drawTexts(GuiGraphics graphics, int originX, int originY) {
        long totalBet = (long) betBase * SlotLines.getLineCount(linesMode);

        Component title = isIllegal ? Component.translatable("awakening_system.screen.slot_machine.header_clandestine") : Component.translatable("awakening_system.screen.slot_machine.header_royal");
        graphics.drawCenteredString(this.font, title, originX + backgroundWidth / 2, originY + 9, isIllegal ? 0xD8B4FE : 0xFFDF73);

        String chipIcon = isIllegal ? "🟣" : "🟡";
        String betStr = totalBet + " " + chipIcon;
        graphics.drawString(this.font, Component.translatable("awakening_system.screen.slot_machine.label_bet"), originX + 58, originY + 32, 0xFFE080, true);
        int betColor = chipsAvailable >= totalBet ? 0x55FF55 : 0xFF5555;
        graphics.drawString(this.font, betStr, originX + 115, originY + 32, betColor, true);

        String balStr = chipsAvailable + " " + chipIcon;
        graphics.drawString(this.font, balStr, originX + 14, originY + 146, 0xFFE080, true);

        graphics.drawString(this.font, Component.translatable("awakening_system.screen.slot_machine.label_prize"), originX + 120, originY + 146, 0xAAAAAA, true);
        String winStr = lastWinAmount + " " + chipIcon;
        int winColor = lastWinAmount > 0 ? 0x55FF55 : 0x888888;
        graphics.drawString(this.font, winStr, originX + 160, originY + 146, winColor, true);
    }

    @Override
    public void onClose() {
        if (spinSoundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(spinSoundInstance);
            spinSoundInstance = null;
        }
        PacketDistributor.sendToServer(new CloseSlotMachineC2SPayload(machinePos));
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
