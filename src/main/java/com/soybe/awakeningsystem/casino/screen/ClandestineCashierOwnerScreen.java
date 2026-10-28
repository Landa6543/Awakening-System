package com.soybe.awakeningsystem.casino.screen;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.casino.network.ClandestineCashierOwnerActionC2SPayload;
import com.soybe.awakeningsystem.casino.network.OpenClandestineCashierOwnerPayload;
import com.soybe.awakeningsystem.casino.screen.CasinoCashierScreen.RpgCasinoButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.Locale;

public class ClandestineCashierOwnerScreen extends Screen {

    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);

    private final BlockPos pos;
    private String ownerName;
    private int stockChips;
    private long collectedMora;
    private int totalChipsSold;
    private int playerChips;
    private long playerMora;

    private final ItemStack blackChipIcon = new ItemStack(SystemItems.CASINO_CHIP_BLACK.get());
    private final ItemStack moraIcon = new ItemStack(SystemItems.MORA.get());

    public ClandestineCashierOwnerScreen(BlockPos pos, String ownerName, int stockChips, long collectedMora, int totalChipsSold, int playerChips, long playerMora) {
        super(Component.translatable("awakening_system.screen.clandestine_cashier_owner.title"));
        this.pos = pos;
        this.ownerName = ownerName;
        this.stockChips = stockChips;
        this.collectedMora = collectedMora;
        this.totalChipsSold = totalChipsSold;
        this.playerChips = playerChips;
        this.playerMora = playerMora;
    }

    public ClandestineCashierOwnerScreen(OpenClandestineCashierOwnerPayload payload) {
        this(payload.pos(), payload.ownerName(), payload.stockChips(), payload.collectedMora(),
                payload.totalChipsSold(), payload.chipsInPlayerInventory(), payload.playerMora());
    }

    public void updateData(OpenClandestineCashierOwnerPayload payload) {
        this.ownerName = payload.ownerName();
        this.stockChips = payload.stockChips();
        this.collectedMora = payload.collectedMora();
        this.totalChipsSold = payload.totalChipsSold();
        this.playerChips = payload.chipsInPlayerInventory();
        this.playerMora = payload.playerMora();
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
        int panelW = 420;
        int panelH = 246;
        int x0 = cx - panelW / 2;
        int y0 = (this.height - panelH) / 2;

        // Fila 1 de botones de stock (Depositar al stock)
        int depY = y0 + 64;
        this.addRenderableWidget(new RpgCasinoButton(x0 + 16, depY, 88, 20,
                Component.translatable("awakening_system.screen.clandestine_cashier_owner.btn_add_1"), b -> sendAction(0, 1),
                0xFF581C87, 0xFFA855F7, 0xEE2A0C4E, 0xEE4B1278, 0xFFF3E8FF, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(x0 + 110, depY, 88, 20,
                Component.translatable("awakening_system.screen.clandestine_cashier_owner.btn_add_5"), b -> sendAction(0, 5),
                0xFF581C87, 0xFFA855F7, 0xEE2A0C4E, 0xEE4B1278, 0xFFF3E8FF, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(x0 + 204, depY, 100, 20,
                Component.translatable("awakening_system.screen.clandestine_cashier_owner.btn_add_all"), b -> sendAction(0, -1),
                0xFF4A044E, 0xFFC026D3, 0xEE2E083B, 0xEE4C1D95, 0xFFFAE8FF, 0xFFFFFFFF));

        // Botones de retirar del stock
        this.addRenderableWidget(new RpgCasinoButton(x0 + 310, depY, 94, 20,
                Component.translatable("awakening_system.screen.clandestine_cashier_owner.btn_withdraw_stock"), b -> sendAction(1, -1),
                0xFF7F1D1D, 0xFFEF4444, 0xEE450A0A, 0xEE7F1D1D, 0xFFFEE2E2, 0xFFFFFFFF));

        // Botón de retirar ganancias de Mora
        int moraY = y0 + 158;
        Component withdrawComp = Component.translatable("awakening_system.screen.clandestine_cashier_owner.btn_withdraw_mora", NF.format(collectedMora));
        this.addRenderableWidget(new RpgCasinoButton(x0 + 16, moraY, 388, 24,
                withdrawComp, b -> sendAction(2, -1),
                0xFF78350F, 0xFFF59E0B, 0xEE451A03, 0xEE78350F, 0xFFFEF3C7, 0xFFFFFFFF));

        // Botones inferiores: Salir y Ver como Cliente
        int btmY = y0 + panelH - 26;
        this.addRenderableWidget(new RpgCasinoButton(cx - 150, btmY, 140, 18,
                Component.translatable("awakening_system.screen.clandestine_cashier_owner.btn_client_view"), b -> sendAction(3, 0),
                0xFF3B0764, 0xFFA855F7, 0xEE1E0B36, 0xEE3B1266, 0xFFE9D5FF, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(cx + 10, btmY, 140, 18,
                Component.translatable("awakening_system.screen.clandestine_cashier_owner.btn_close"), b -> this.onClose(),
                0xFF334155, 0xFF94A3B8, 0xEE111827, 0xEE1F2937, 0xFF94A3B8, 0xFFFFFFFF));
    }

    private void sendAction(int action, int amount) {
        PacketDistributor.sendToServer(new ClandestineCashierOwnerActionC2SPayload(this.pos, action, amount));
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 440.0F;
        float scaleY = (float) this.height / 260.0F;
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
        int panelW = 420;
        int panelH = 246;
        int x0 = cx - panelW / 2;
        int y0 = (this.height - panelH) / 2;

        // Fondo y marcos góticos
        g.fill(x0, y0, x0 + panelW, y0 + panelH, 0xF50B0813);
        g.fill(x0, y0, x0 + panelW, y0 + 2, 0xFFA855F7);
        g.fill(x0, y0 + panelH - 2, x0 + panelW, y0 + panelH, 0xFFA855F7);
        g.fill(x0, y0, x0 + 2, y0 + panelH, 0xFFA855F7);
        g.fill(x0 + panelW - 2, y0, x0 + panelW, y0 + panelH, 0xFFA855F7);

        g.fill(x0 + 4, y0 + 4, x0 + panelW - 4, y0 + 5, 0xFF581C87);
        g.fill(x0 + 4, y0 + panelH - 5, x0 + panelW - 4, y0 + panelH - 4, 0xFF581C87);
        g.fill(x0 + 4, y0 + 4, x0 + 5, y0 + panelH - 4, 0xFF581C87);
        g.fill(x0 + panelW - 5, y0 + 4, x0 + panelW - 4, y0 + panelH - 4, 0xFF581C87);

        // Encabezado
        g.drawCenteredString(this.font, Component.translatable("awakening_system.screen.clandestine_cashier_owner.header"), cx, y0 + 10, 0xFFFFFFFF);
        g.drawCenteredString(this.font, Component.translatable("awakening_system.screen.clandestine_cashier_owner.subtitle", this.ownerName), cx, y0 + 22, 0xFFAAAAAA);

        // Caja 1: Stock de Fichas Negras en Venta
        int box1Y = y0 + 38;
        int box1H = 50;
        g.fill(x0 + 12, box1Y, x0 + panelW - 12, box1Y + box1H, 0x551E1136);
        g.fill(x0 + 12, box1Y, x0 + 15, box1Y + box1H, 0xFFA855F7);

        g.renderItem(blackChipIcon, x0 + 20, box1Y + 5);

        Component stockComp = Component.translatable("awakening_system.screen.clandestine_cashier_owner.stock_for_sale", this.stockChips);
        g.drawString(this.font, stockComp, x0 + 42, box1Y + 5, 0xFFFFFFFF, false);

        Component inInvComp = Component.translatable("awakening_system.screen.clandestine_cashier_owner.in_inventory", (this.playerChips > 0 ? "§d" + this.playerChips : "§80"));
        int inInvW = this.font.width(inInvComp);
        g.drawString(this.font, inInvComp, x0 + panelW - 20 - inInvW, box1Y + 5, 0xFFFFFFFF, false);

        g.drawString(this.font, Component.translatable("awakening_system.screen.clandestine_cashier_owner.sale_price_desc"), x0 + 42, box1Y + 16, 0xFFAAAAAA, false);

        // Caja 2: Bóveda de Mora Recaudada
        int box2Y = y0 + 94;
        int box2H = 58;
        g.fill(x0 + 12, box2Y, x0 + panelW - 12, box2Y + box2H, 0x552B1E05);
        g.fill(x0 + 12, box2Y, x0 + 15, box2Y + box2H, 0xFFF59E0B);

        g.renderItem(moraIcon, x0 + 20, box2Y + 6);

        Component moraComp = Component.translatable("awakening_system.screen.clandestine_cashier_owner.mora_collected", NF.format(this.collectedMora));
        g.drawString(this.font, moraComp, x0 + 42, box2Y + 6, 0xFFFFFFFF, false);

        Component statsComp = Component.translatable("awakening_system.screen.clandestine_cashier_owner.stats", this.totalChipsSold, NF.format(this.playerMora));
        g.drawString(this.font, statsComp, x0 + 42, box2Y + 18, 0xFFAAAAAA, false);

        g.drawString(this.font, Component.translatable("awakening_system.screen.clandestine_cashier_owner.mora_hint"), x0 + 42, box2Y + 29, 0xFF888888, false);

        super.render(g, mX, mY, partialTick);
        g.pose().popPose();
    }
}
