package com.soybe.awakeningsystem.casino.screen;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
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
    private int betBase = 1000;
    private int linesMode = 1;

    private static final int[] BET_PRESETS = { 100, 500, 1000, 2500, 5000, 10000, 25000, 50000 };
    private int betPresetIndex = 2;

    private List<SlotLineResult> lastWins = List.of();
    private long lastWinAmount = 0;
    private long pendingWinAmount = -1;
    private boolean lastSpinHadWin = false;

    // --- BOTONES ---
    private SlotButton spinButton;
    private SlotButton betButton;
    private SlotButton linesButton;
    private SlotButton closeButton;

    // --- LÍNEAS DE PAGO ---
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

    // --- LAYOUT ---
    private final int backgroundWidth = 220;
    private final int backgroundHeight = 205;
    private static final int SYMBOL_SIZE = 32;
    private static final int[] COLUMN_X = { 55, 94, 133 };
    private static final int ROW_Y = 53;
    private static final int VIRTUAL_ROWS = 5;

    // --- ANIMACIÓN ---
    private boolean isSpinning = false;
    private boolean isWaitingForServer = false;
    private SimpleSoundInstance spinSoundInstance = null;
    private final int[] reelIndex = new int[3];
    private final int[] targetTopIndex = new int[3];
    private final float[] reelOffset = new float[3];
    private final int[] reelTimer = new int[3];
    private final boolean[] reelSpinning = new boolean[3];
    private static final float REEL_VELOCITY = 16.0f;
    private static final int[] REEL_TICKS = { 32, 46, 60 };
    private static final int[] REEL_STEPS = { 16, 23, 30 }; // REEL_TICKS / 2

    // --- PAIMON ---
    private final DancingPaimon paimonLeft = new DancingPaimon();
    private final DancingPaimon paimonRight = new DancingPaimon();

    public SlotMachineScreen(BlockPos machinePos, long initialBalance, int betBase, int linesMode) {
        super(Component.literal("Tragamonedas Celestial"));
        this.machinePos = machinePos;
        this.balance = initialBalance;
        this.betBase = betBase;
        this.linesMode = linesMode;

        for (int i = 0; i < BET_PRESETS.length; i++) {
            if (BET_PRESETS[i] == betBase) {
                this.betPresetIndex = i;
                break;
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        int originX = (this.width - this.backgroundWidth) / 2;
        int originY = (this.height - this.backgroundHeight) / 2;

        // Cargar sprites de líneas con sus dimensiones exactas de textura (14px para horizontales, 112px para diagonales)
        this.lineOneSprite = new SlotLineSprite(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/lines/line_one.png"), 160, 14);
        this.lineTwoSprite = new SlotLineSprite(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/lines/line_two.png"), 160, 14);
        this.lineThreeTopSprite = new SlotLineSprite(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/lines/line_three_top.png"), 160, 112);
        this.lineThreeBottomSprite = new SlotLineSprite(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/casino/lines/line_three_bottom.png"), 160, 112);

        // Botón APUESTA (Ajustar valor base de Mora)
        this.betButton = new SlotButton(originX + 8, originY + 168, 50, 26, BTN_BET, btn -> cycleBet(), Component.literal("Apuesta"));
        this.addRenderableWidget(this.betButton);

        // Botón LÍNEAS (Modo 1, 2 o 3)
        this.linesButton = new SlotButton(originX + 63, originY + 168, 44, 26, BTN_MENU, btn -> cycleLines(), Component.literal("Líneas"));
        this.addRenderableWidget(this.linesButton);

        // Botón SALIR
        this.closeButton = new SlotButton(originX + 112, originY + 168, 44, 26, BTN_MENU, btn -> onClose(), Component.literal("Salir"));
        this.addRenderableWidget(this.closeButton);

        // Botón GIRAR (Arcade Spin Button)
        this.spinButton = new SlotButton(originX + 161, originY + 168, 54, 26, BTN_SPIN, btn -> doSpin(), Component.literal("GIRAR"));
        this.addRenderableWidget(this.spinButton);

        updateButtonsState();
    }

    private void cycleBet() {
        if (isSpinning || isWaitingForServer) return;
        betPresetIndex = (betPresetIndex + 1) % BET_PRESETS.length;
        this.betBase = BET_PRESETS[betPresetIndex];
        updateButtonsState();
    }

    private void cycleLines() {
        if (isSpinning || isWaitingForServer) return;
        this.linesMode = (this.linesMode % 3) + 1;
        updateButtonsState();
    }

    private void doSpin() {
        long totalBet = (long) betBase * SlotLines.getLineCount(linesMode);
        if (isSpinning || isWaitingForServer || balance < totalBet) return;

        // Enviar solicitud de giro al servidor
        PacketDistributor.sendToServer(new SlotSpinC2SPayload(machinePos, betBase, linesMode));
        this.isWaitingForServer = true;
        this.balance -= totalBet;
        updateButtonsState();
    }

    public void onSpinResult(int stop1, int stop2, int stop3, long totalWin, long newBalance, int mode, boolean isJackpot, List<SlotLineResult> wins) {
        this.isWaitingForServer = false;
        this.lastWins = wins;
        this.linesMode = mode;
        this.pendingWinAmount = totalWin;
        this.pendingBalance = newBalance;
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

        // Parpadeo de líneas ganadoras
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

        // Animación sincronizada y determinista de rodillos
        if (isSpinning) {
            boolean anySpinning = false;
            int len = SlotReels.REEL_SIZE;

            for (int col = 0; col < 3; col++) {
                if (reelSpinning[col]) {
                    reelTimer[col]--;
                    reelOffset[col] += REEL_VELOCITY;

                    if (reelOffset[col] >= SYMBOL_SIZE) {
                        reelOffset[col] -= SYMBOL_SIZE;
                        reelIndex[col] = (reelIndex[col] + 1) % len;
                    }

                    if (reelTimer[col] <= 0) {
                        reelOffset[col] = 0.0F;
                        reelSpinning[col] = false;
                        reelIndex[col] = targetTopIndex[col];
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.2F + col * 0.25F)
                        );
                    } else {
                        anySpinning = true;
                    }
                }
            }

            if (!anySpinning) {
                finishSpin();
            }
        }

        // Animación de Paimon
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
        boolean canAfford = balance >= totalBet;

        if (spinButton != null) {
            spinButton.active = !busy && canAfford;
            spinButton.setForcedPressed(busy);
            spinButton.setMessage(Component.literal(busy ? "..." : "GIRAR"));
        }
        if (betButton != null) {
            betButton.active = !busy;
            betButton.setSubtitle(Component.literal(String.format(Locale.US, "%,d", betBase)));
        }
        if (linesButton != null) {
            linesButton.active = !busy;
            linesButton.setSubtitle(Component.literal(SlotLines.getLineCount(linesMode) + " / 3"));
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

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Vacío intencionalmente para evitar shaders de blur vanilla de Minecraft
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int originX = (this.width - this.backgroundWidth) / 2;
        int originY = (this.height - this.backgroundHeight) / 2;

        // Fondo oscuro semitransparente limpio sin shader de blur
        graphics.fillGradient(0, 0, this.width, this.height, 0x88000000, 0xA0000000);

        // Fondo de rodillos
        graphics.blit(REELS_TEXTURE, originX + 50, originY + 47, 0, 0, 120, 112, 120, 112);

        // Dibujar símbolos que ruedan
        drawSymbols(graphics, originX, originY);

        // Gabinete de la tragamonedas (marco superior que oculta los rodillos fuera del área)
        graphics.blit(GUI_TEXTURE, originX, originY, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        // Dibujar líneas de pago
        drawLines(graphics, originX, originY);

        // Dibujar Paimons animadas en los laterales
        paimonLeft.render(graphics, originX + 7, originY + 131);
        paimonRight.render(graphics, originX + 191, originY + 131);

        // Renderizar widgets/botones
        super.render(graphics, mouseX, mouseY, partialTick);

        // Textos de saldo, apuesta y ganancias
        drawTexts(graphics, originX, originY);
    }

    private void drawSymbols(GuiGraphics graphics, int originX, int originY) {
        int len = SlotReels.REEL_SIZE;

        // Limitar el renderizado de símbolos estrictamente a la ventana visible de los rodillos
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
                int drawY = originY + ROW_Y + ((row - 1) * SYMBOL_SIZE) + (int) reelOffset[col];

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

        // Título celestial en la marquesina superior
        graphics.drawCenteredString(this.font, "✦ TRAGAMONEDAS CELESTIAL ✦", originX + backgroundWidth / 2, originY + 9, 0xFFDF73);

        // Apuesta en la pantalla superior (y=32)
        String betStr = String.format(Locale.US, "%,d", totalBet);
        graphics.drawString(this.font, "Apuesta:", originX + 58, originY + 32, 0xFFE080, true);
        int betColor = balance >= totalBet ? 0x55FF55 : 0xFF5555;
        graphics.drawString(this.font, betStr, originX + 115, originY + 32, betColor, true);

        // Saldo de Mora (inferior izquierda)
        graphics.blit(MORA_ICON, originX + 14, originY + 145, 0, 0, 10, 10, 10, 10);
        String balStr = String.format(Locale.US, "%,d", balance);
        graphics.drawString(this.font, balStr, originX + 27, originY + 146, 0xFFE080, true);

        // Ganancias de la tirada (inferior derecha)
        graphics.drawString(this.font, "Premio:", originX + 120, originY + 146, 0xAAAAAA, true);
        String winStr = String.format(Locale.US, "%,d", lastWinAmount);
        int winColor = lastWinAmount > 0 ? 0x55FF55 : 0x888888;
        graphics.drawString(this.font, winStr, originX + 160, originY + 146, winColor, true);
    }

    @Override
    public void onClose() {
        if (isSpinning) return;
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
