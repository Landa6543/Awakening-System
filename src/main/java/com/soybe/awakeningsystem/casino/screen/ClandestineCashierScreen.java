package com.soybe.awakeningsystem.casino.screen;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.casino.network.ClandestineCashierOwnerActionC2SPayload;
import com.soybe.awakeningsystem.casino.network.ClandestineExchangeC2SPayload;
import com.soybe.awakeningsystem.casino.network.OpenClandestineCashierPayload;
import com.soybe.awakeningsystem.casino.screen.CasinoCashierScreen.RpgCasinoButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.Locale;

public class ClandestineCashierScreen extends Screen {

    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);

    private final BlockPos pos;
    private long moraBalance;
    private int stockChips;
    private boolean isOwner;

    private final ItemStack blackChipIcon = new ItemStack(SystemItems.CASINO_CHIP_BLACK.get());

    public ClandestineCashierScreen(BlockPos pos, long moraBalance, int stockChips, boolean isOwner) {
        super(Component.translatable("awakening_system.screen.clandestine_cashier.title"));
        this.pos = pos != null ? pos : BlockPos.ZERO;
        this.moraBalance = moraBalance;
        this.stockChips = stockChips;
        this.isOwner = isOwner;
    }

    public ClandestineCashierScreen(OpenClandestineCashierPayload payload) {
        this(payload.pos(), payload.moraBalance(), payload.stockChips(), payload.isOwner());
    }

    public ClandestineCashierScreen(long moraBalance) {
        this(BlockPos.ZERO, moraBalance, -1, false);
    }

    public void updateData(OpenClandestineCashierPayload payload) {
        this.moraBalance = payload.moraBalance();
        this.stockChips = payload.stockChips();
        this.isOwner = payload.isOwner();
        rebuildWidgets();
    }

    public void updateBalance(long newBalance) {
        this.moraBalance = newBalance;
        rebuildWidgets();
    }

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();

        int cx = this.width / 2;
        int panelH = 224;
        int y0 = (this.height - panelH) / 2;
        int yBase = y0 + 104;

        this.addRenderableWidget(new RpgCasinoButton(cx - 150, yBase, 95, 22,
                Component.literal("§a").append(Component.translatable("awakening_system.screen.clandestine_cashier.btn_buy_1")), b -> buyChips(1),
                0xFF581C87, 0xFFA855F7, 0xEE1E0B36, 0xEE3B1266, 0xFFE9D5FF, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(cx - 48, yBase, 95, 22,
                Component.literal("§a").append(Component.translatable("awakening_system.screen.clandestine_cashier.btn_buy_5")), b -> buyChips(5),
                0xFF581C87, 0xFFA855F7, 0xEE1E0B36, 0xEE3B1266, 0xFFE9D5FF, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(cx + 54, yBase, 95, 22,
                Component.literal("§a").append(Component.translatable("awakening_system.screen.clandestine_cashier.btn_buy_10")), b -> buyChips(10),
                0xFF581C87, 0xFFA855F7, 0xEE1E0B36, 0xEE3B1266, 0xFFE9D5FF, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(cx - 150, yBase + 30, 95, 22,
                Component.literal("§c").append(Component.translatable("awakening_system.screen.clandestine_cashier.btn_sell_1")), b -> sellChips(1),
                0xFF7F1D1D, 0xFFFF5252, 0xEE2A0C11, 0xEE4B141E, 0xFFFECDD3, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(cx - 48, yBase + 30, 95, 22,
                Component.literal("§c").append(Component.translatable("awakening_system.screen.clandestine_cashier.btn_sell_5")), b -> sellChips(5),
                0xFF7F1D1D, 0xFFFF5252, 0xEE2A0C11, 0xEE4B141E, 0xFFFECDD3, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(cx + 54, yBase + 30, 95, 22,
                Component.literal("§6").append(Component.translatable("awakening_system.screen.clandestine_cashier.btn_sell_all")), b -> sellChips(-1),
                0xFFB45309, 0xFFFFD700, 0xEE2C1B06, 0xEE4B2E0A, 0xFFFEF08A, 0xFFFFFFFF));

        // Barra inferior: botón de salir y acceso directo a panel de dueño
        if (this.isOwner) {
            this.addRenderableWidget(new RpgCasinoButton(cx - 150, yBase + 64, 145, 22,
                    Component.literal("§5").append(Component.translatable("awakening_system.screen.clandestine_cashier.btn_owner_panel")), b -> openOwnerPanel(),
                    0xFF4A044E, 0xFFC026D3, 0xEE2E083B, 0xEE4C1D95, 0xFFFAE8FF, 0xFFFFFFFF));

            this.addRenderableWidget(new RpgCasinoButton(cx + 5, yBase + 64, 145, 22,
                    Component.literal("§c").append(Component.translatable("awakening_system.screen.clandestine_cashier.btn_exit")), b -> this.onClose(),
                    0xFF334155, 0xFFEF4444, 0xEE111827, 0xEE1F2937, 0xFF94A3B8, 0xFFFFFFFF));
        } else {
            this.addRenderableWidget(new RpgCasinoButton(cx - 75, yBase + 64, 150, 22,
                    Component.literal("§c").append(Component.translatable("awakening_system.screen.clandestine_cashier.btn_leave_alley")), b -> this.onClose(),
                    0xFF334155, 0xFFEF4444, 0xEE111827, 0xEE1F2937, 0xFF94A3B8, 0xFFFFFFFF));
        }
    }

    private void buyChips(int amount) {
        PacketDistributor.sendToServer(new ClandestineExchangeC2SPayload(this.pos, 0, amount));
    }

    private void sellChips(int amount) {
        PacketDistributor.sendToServer(new ClandestineExchangeC2SPayload(this.pos, 1, amount));
    }

    private void openOwnerPanel() {
        PacketDistributor.sendToServer(new ClandestineCashierOwnerActionC2SPayload(this.pos, 3, 0));
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 380.0F;
        float scaleY = (float) this.height / 240.0F;
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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void renderTransparentBackground(GuiGraphics guiGraphics) {}

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cxF = this.width / 2.0F;
            float cyF = this.height / 2.0F;
            g.pose().translate(cxF, cyF, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cxF, -cyF, 0.0F);
        }

        int cx = this.width / 2;
        int panelW = 360;
        int panelH = 224;
        int x0 = cx - panelW / 2;
        int y0 = (this.height - panelH) / 2;

        g.fill(x0, y0, x0 + panelW, y0 + panelH, 0xF50B0813);

        g.fill(x0, y0, x0 + panelW, y0 + 2, 0xFFA855F7);
        g.fill(x0, y0 + panelH - 2, x0 + panelW, y0 + panelH, 0xFFA855F7);
        g.fill(x0, y0, x0 + 2, y0 + panelH, 0xFFA855F7);
        g.fill(x0 + panelW - 2, y0, x0 + panelW, y0 + panelH, 0xFFA855F7);

        g.fill(x0 + 4, y0 + 4, x0 + panelW - 4, y0 + 5, 0xFF581C87);
        g.fill(x0 + 4, y0 + panelH - 5, x0 + panelW - 4, y0 + panelH - 4, 0xFF581C87);
        g.fill(x0 + 4, y0 + 4, x0 + 5, y0 + panelH - 4, 0xFF581C87);
        g.fill(x0 + panelW - 5, y0 + 4, x0 + panelW - 4, y0 + panelH - 4, 0xFF581C87);

        g.drawCenteredString(this.font, Component.translatable("awakening_system.screen.clandestine_cashier.header"), cx, y0 + 10, 0xFFFFFFFF);
        g.drawCenteredString(this.font, Component.translatable("awakening_system.screen.clandestine_cashier.subtitle"), cx, y0 + 22, 0xFFAAAAAA);

        g.fill(cx - 130, y0 + 36, cx + 130, y0 + 52, 0x881E1B4B);
        g.fill(cx - 130, y0 + 36, cx + 130, y0 + 37, 0xAAA855F7);
        g.fill(cx - 130, y0 + 51, cx + 130, y0 + 52, 0xAAA855F7);
        Component moraText = Component.translatable("awakening_system.screen.casino_cashier.balance", NF.format(this.moraBalance));
        g.drawCenteredString(this.font, moraText, cx, y0 + 40, 0xFFFFFFFF);

        // Tarjeta de la divisa (sin solapamiento)
        int cardY = y0 + 58;
        int cardH = 38;
        g.fill(x0 + 16, cardY, x0 + panelW - 16, cardY + cardH, 0x6618112C);
        g.fill(x0 + 16, cardY, x0 + 19, cardY + cardH, 0xFFC084FC);

        g.renderItem(blackChipIcon, x0 + 24, cardY + 11);

        // Fila Superior de la tarjeta: Título a la izquierda, Posesión a la derecha
        Component blackTitle = Component.literal("§d§l").append(blackChipIcon.getHoverName());
        g.drawString(this.font, blackTitle, x0 + 48, cardY + 8, 0xFFFFFFFF, false);

        int count = countChipsInInventory();
        Component inHand = Component.translatable("awakening_system.screen.clandestine_cashier.in_possession", (count > 0 ? "§d" + count : "§80"));
        int inHandW = this.font.width(inHand);
        g.drawString(this.font, inHand, x0 + panelW - 24 - inHandW, cardY + 8, 0xFFFFFFFF, false);

        // Fila Inferior de la tarjeta: Valor a la izquierda, Stock a la derecha
        g.drawString(this.font, Component.translatable("awakening_system.screen.clandestine_cashier.rate"), x0 + 48, cardY + 22, 0xFFAAAAAA, false);

        Component stockText;
        if (this.stockChips > 0) {
            stockText = Component.translatable("awakening_system.screen.clandestine_cashier.stock", this.stockChips);
        } else if (this.stockChips == 0) {
            stockText = Component.translatable("awakening_system.screen.clandestine_cashier.no_stock");
        } else {
            stockText = Component.translatable("awakening_system.screen.clandestine_cashier.stock_unlimited");
        }
        int stockW = this.font.width(stockText);
        g.drawString(this.font, stockText, x0 + panelW - 24 - stockW, cardY + 22, 0xFFFFFFFF, false);

        super.render(g, mX, mY, partialTick);
        g.pose().popPose();
    }

    private int countChipsInInventory() {
        if (this.minecraft == null || this.minecraft.player == null) return 0;
        int total = 0;
        for (int i = 0; i < this.minecraft.player.getInventory().getContainerSize(); i++) {
            ItemStack s = this.minecraft.player.getInventory().getItem(i);
            if (s.is(SystemItems.CASINO_CHIP_BLACK.get())) {
                total += s.getCount();
            }
        }
        return total;
    }
}
