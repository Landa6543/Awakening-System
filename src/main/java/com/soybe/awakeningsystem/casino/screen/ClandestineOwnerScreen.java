package com.soybe.awakeningsystem.casino.screen;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.casino.network.ClandestineOwnerActionC2SPayload;
import com.soybe.awakeningsystem.casino.network.OpenClandestineOwnerPayload;
import com.soybe.awakeningsystem.casino.screen.CasinoCashierScreen.RpgCasinoButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.Locale;

public class ClandestineOwnerScreen extends Screen {

    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);

    private final BlockPos pos;
    private String ownerName;
    private int reserveChips;
    private int vaultChips;
    private int totalCustomerSpins;
    private int playerChips;

    public ClandestineOwnerScreen(BlockPos pos, String ownerName, int reserveChips, int vaultChips, int totalCustomerSpins, int playerChips) {
        super(Component.translatable("awakening_system.screen.clandestine_owner.title"));
        this.pos = pos;
        this.ownerName = ownerName;
        this.reserveChips = reserveChips;
        this.vaultChips = vaultChips;
        this.totalCustomerSpins = totalCustomerSpins;
        this.playerChips = playerChips;
    }

    public ClandestineOwnerScreen(OpenClandestineOwnerPayload payload) {
        this(payload.pos(), payload.ownerName(), payload.reserveChips(), payload.vaultChips(), payload.totalCustomerSpins(), payload.chipsInPlayerInventory());
    }

    public void updateData(OpenClandestineOwnerPayload payload) {
        this.ownerName = payload.ownerName();
        this.reserveChips = payload.reserveChips();
        this.vaultChips = payload.vaultChips();
        this.totalCustomerSpins = payload.totalCustomerSpins();
        this.playerChips = payload.chipsInPlayerInventory();
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
        int panelW = 400;
        int panelH = 220;
        int x0 = cx - panelW / 2;
        int y0 = (this.height - panelH) / 2;

        int totalAccumulated = vaultChips + reserveChips;

        // Botón principal de retirada de fichas
        int btnY = y0 + 104;
        this.addRenderableWidget(new RpgCasinoButton(x0 + 16, btnY, panelW - 32, 26,
                Component.literal("§6").append(Component.translatable("awakening_system.screen.clandestine_owner.btn_withdraw", totalAccumulated)),
                b -> sendAction(1, -1),
                0xFF78350F, 0xFFF59E0B, 0xEE451A03, 0xEE78350F, 0xFFFEF3C7, 0xFFFFFFFF));

        // Botón de Salida
        this.addRenderableWidget(new RpgCasinoButton(cx - 70, y0 + panelH - 26, 140, 20,
                Component.literal("§7").append(Component.translatable("awakening_system.screen.clandestine_owner.btn_close")), b -> this.onClose(),
                0xFF334155, 0xFF94A3B8, 0xEE111827, 0xEE1F2937, 0xFF94A3B8, 0xFFFFFFFF));
    }

    private void sendAction(int action, int amount) {
        PacketDistributor.sendToServer(new ClandestineOwnerActionC2SPayload(pos, action, amount));
    }

    @Override
    public void render(GuiGraphics g, int mX, int mY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0x88000000, 0x99000000);

        int cx = this.width / 2;
        int panelW = 400;
        int panelH = 220;
        int x0 = cx - panelW / 2;
        int y0 = (this.height - panelH) / 2;

        int totalAccumulated = vaultChips + reserveChips;

        // Fondo y marcos oscuros con destellos púrpura
        g.fill(x0, y0, x0 + panelW, y0 + panelH, 0xF60F0A1A);
        g.fill(x0, y0, x0 + panelW, y0 + 2, 0xFFA855F7);
        g.fill(x0, y0 + panelH - 2, x0 + panelW, y0 + panelH, 0xFFA855F7);
        g.fill(x0, y0, x0 + 2, y0 + panelH, 0xFFA855F7);
        g.fill(x0 + panelW - 2, y0, x0 + panelW, y0 + panelH, 0xFFA855F7);

        g.drawCenteredString(this.font, Component.translatable("awakening_system.screen.clandestine_owner.header"), cx, y0 + 8, 0xFFFFFFFF);
        g.drawCenteredString(this.font, Component.translatable("awakening_system.screen.clandestine_owner.info", ownerName, playerChips), cx, y0 + 20, 0xFFAAAAAA);

        // Tarjeta Principal: Bóveda de Recaudación (Ganancias del Dueño)
        int c1Y = y0 + 36;
        int c1H = 62;
        g.fill(x0 + 12, c1Y, x0 + panelW - 12, c1Y + c1H, 0xEE2A1708);
        g.renderOutline(x0 + 12, c1Y, panelW - 24, c1H, 0xFFD97706);
        g.renderItem(new ItemStack(SystemItems.CASINO_CHIP_BLACK.get()), x0 + 18, c1Y + 8);
        g.drawString(this.font, Component.translatable("awakening_system.screen.clandestine_owner.vault_title", totalAccumulated), x0 + 40, c1Y + 9, 0xFFFFFFFF);
        g.drawString(this.font, Component.translatable("awakening_system.screen.clandestine_owner.vault_value", NF.format((long) totalAccumulated * 1_000_000L)), x0 + 40, c1Y + 22, 0xFFE5E7EB);
        g.drawString(this.font, Component.translatable("awakening_system.screen.clandestine_owner.vault_hint"), x0 + 40, c1Y + 34, 0xFF9CA3AF);

        // Tarjeta Secundaria: Información del Negocio
        int c2Y = y0 + 138;
        int c2H = 48;
        g.fill(x0 + 12, c2Y, x0 + panelW - 12, c2Y + c2H, 0xEE13101C);
        g.renderOutline(x0 + 12, c2Y, panelW - 24, c2H, 0xFF374151);
        g.drawString(this.font, Component.translatable("awakening_system.screen.clandestine_owner.stats", totalCustomerSpins), x0 + 18, c2Y + 8, 0xFFE5E7EB);
        g.drawString(this.font, Component.translatable("awakening_system.screen.clandestine_owner.continuous"), x0 + 18, c2Y + 20, 0xFF9CA3AF);
        g.drawString(this.font, Component.translatable("awakening_system.screen.clandestine_owner.jackpots"), x0 + 18, c2Y + 32, 0xFF9CA3AF);

        super.render(g, mX, mY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void renderTransparentBackground(GuiGraphics guiGraphics) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
