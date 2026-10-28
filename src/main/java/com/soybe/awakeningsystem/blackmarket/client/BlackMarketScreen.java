package com.soybe.awakeningsystem.blackmarket.client;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.blackmarket.BlackMarketServerHandler;
import com.soybe.awakeningsystem.blackmarket.network.BlackMarketActionC2SPayload;
import com.soybe.awakeningsystem.casino.screen.CasinoCashierScreen.RpgCasinoButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Pantalla de Interfaz para el Mercado Negro del Bajo Mundo.
 * Permite comerciar con Valerius el Silencioso: máquinas ilegales, elixires y divisas ilícitas.
 */
public class BlackMarketScreen extends Screen {

    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);

    private long moraBalance;
    private int blackChipsCount;
    private int currentTab = 0;

    public BlackMarketScreen(long moraBalance, int blackChipsCount) {
        super(Component.translatable("awakening_system.blackmarket.title"));
        this.moraBalance = moraBalance;
        this.blackChipsCount = blackChipsCount;
    }

    public BlackMarketScreen(com.soybe.awakeningsystem.blackmarket.network.OpenBlackMarketPayload payload) {
        this(payload != null ? payload.moraBalance() : 0L, payload != null ? payload.blackChipsCount() : 0);
    }

    public void updateData(long newMora, int newChips) {
        this.moraBalance = newMora;
        this.blackChipsCount = newChips;
    }

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();
    }

    @Override
    public void onClose() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().getSoundManager().stop(null, SoundSource.VOICE);
        }
        super.onClose();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();

        int cx = this.width / 2;
        int panelW = 450;
        int panelH = 246;
        int x0 = cx - panelW / 2;
        int y0 = (this.height - panelH) / 2;

        int tabW = 100;
        int tabH = 20;
        int tabY = y0 + 36;

        this.addRenderableWidget(new RpgCasinoButton(x0 + 16, tabY, tabW, tabH,
                Component.translatable(currentTab == 0 ? "awakening_system.blackmarket.tab.machines_active" : "awakening_system.blackmarket.tab.machines"),
                b -> switchTab(0),
                0xFF3B0764, 0xFFA855F7, 0xEE1E0B36, 0xEE3B1266, 0xFFE9D5FF, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(x0 + 122, tabY, tabW, tabH,
                Component.translatable(currentTab == 1 ? "awakening_system.blackmarket.tab.tables_active" : "awakening_system.blackmarket.tab.tables"),
                b -> switchTab(1),
                0xFF3B0764, 0xFFA855F7, 0xEE1E0B36, 0xEE3B1266, 0xFFE9D5FF, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(x0 + 228, tabY, tabW, tabH,
                Component.translatable(currentTab == 2 ? "awakening_system.blackmarket.tab.contraband_active" : "awakening_system.blackmarket.tab.contraband"),
                b -> switchTab(2),
                0xFF3B0764, 0xFFA855F7, 0xEE1E0B36, 0xEE3B1266, 0xFFE9D5FF, 0xFFFFFFFF));

        this.addRenderableWidget(new RpgCasinoButton(x0 + 334, tabY, tabW, tabH,
                Component.translatable(currentTab == 3 ? "awakening_system.blackmarket.tab.currency_active" : "awakening_system.blackmarket.tab.currency"),
                b -> switchTab(3),
                0xFF3B0764, 0xFFA855F7, 0xEE1E0B36, 0xEE3B1266, 0xFFE9D5FF, 0xFFFFFFFF));

        int contentY = y0 + 62;
        int cardStep = 52;
        int btnW = 95;
        int btnH = 22;
        int btnX = x0 + panelW - 16 - btnW - 6;

        if (currentTab == 0) {
            addTradeButton(btnX, contentY + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.buy_chips", 5),
                    BlackMarketServerHandler.TRADE_ILLEGAL_SLOT);
            addTradeButton(btnX, contentY + cardStep + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.buy_chips", 3),
                    BlackMarketServerHandler.TRADE_CLANDESTINE_CASHIER);
            addTradeButton(btnX, contentY + cardStep * 2 + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.buy_key", 1),
                    BlackMarketServerHandler.TRADE_BUY_CLANDESTINE_KEY);
        } else if (currentTab == 1) {
            addTradeButton(btnX, contentY + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.buy_chips", 6),
                    BlackMarketServerHandler.TRADE_ILLEGAL_BLACKJACK);
            addTradeButton(btnX, contentY + cardStep + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.buy_chips", 6),
                    BlackMarketServerHandler.TRADE_ILLEGAL_ROULETTE);
            addTradeButton(btnX, contentY + cardStep * 2 + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.buy_chips", 8),
                    BlackMarketServerHandler.TRADE_ILLEGAL_POOL);
        } else if (currentTab == 2) {
            addTradeButton(btnX, contentY + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.pack", 2),
                    BlackMarketServerHandler.TRADE_DARK_ELIXIR_3);
            addTradeButton(btnX, contentY + cardStep + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.ticket", 3),
                    BlackMarketServerHandler.TRADE_GOLDEN_GACHA_TICKET);
            addTradeButton(btnX, contentY + cardStep * 2 + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.hatch", 2),
                    BlackMarketServerHandler.TRADE_BUY_BLACK_MARKET_HATCH);
        } else {
            addTradeButton(btnX, contentY + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.buy_mora", "500k"),
                    BlackMarketServerHandler.TRADE_BUY_CHIP_1);
            addTradeButton(btnX, contentY + cardStep + 13, btnW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.wholesale", "5M"),
                    BlackMarketServerHandler.TRADE_WHOLESALE_BLACK_CHIP_10);
            int halfW = (btnW - 4) / 2;
            addTradeButton(btnX, contentY + cardStep * 2 + 13, halfW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.sell_chip", "1M", "x1"),
                    BlackMarketServerHandler.TRADE_SELL_CHIP_1);
            addTradeButton(btnX + halfW + 4, contentY + cardStep * 2 + 13, halfW, btnH,
                    Component.translatable("awakening_system.blackmarket.btn.sell_chip", "5M", "x5"),
                    BlackMarketServerHandler.TRADE_SELL_CHIP_5);
        }

        this.addRenderableWidget(new RpgCasinoButton(cx - 80, y0 + panelH - 24, 160, 18,
                Component.translatable("awakening_system.blackmarket.btn.exit"), b -> this.onClose(),
                0xFF334155, 0xFFEF4444, 0xEE111827, 0xEE1F2937, 0xFF94A3B8, 0xFFFFFFFF));
    }

    private void addTradeButton(int x, int y, int w, int h, Component label, int tradeId) {
        this.addRenderableWidget(new RpgCasinoButton(x, y, w, h,
                label, b -> executeTrade(tradeId),
                0xFF581C87, 0xFFA855F7, 0xEE2A0C4E, 0xEE4B1278, 0xFFF3E8FF, 0xFFFFFFFF));
    }

    private void switchTab(int tab) {
        this.currentTab = tab;
        rebuildWidgets();
    }

    private void executeTrade(int tradeId) {
        PacketDistributor.sendToServer(new BlackMarketActionC2SPayload(tradeId, 1));
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 470.0F;
        float scaleY = (float) this.height / 266.0F;
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
        g.fillGradient(0, 0, this.width, this.height, 0xB008060F, 0xD0040208);

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
        int panelW = 450;
        int panelH = 246;
        int x0 = cx - panelW / 2;
        int y0 = (this.height - panelH) / 2;

        g.fill(x0, y0, x0 + panelW, y0 + panelH, 0xF80B0813);

        g.fill(x0, y0, x0 + panelW, y0 + 2, 0xFFA855F7);
        g.fill(x0, y0 + panelH - 2, x0 + panelW, y0 + panelH, 0xFFA855F7);
        g.fill(x0, y0, x0 + 2, y0 + panelH, 0xFFA855F7);
        g.fill(x0 + panelW - 2, y0, x0 + panelW, y0 + panelH, 0xFFA855F7);

        g.fill(x0 + 3, y0 + 3, x0 + panelW - 3, y0 + 4, 0xFF581C87);
        g.fill(x0 + 3, y0 + panelH - 4, x0 + panelW - 3, y0 + panelH - 3, 0xFF581C87);
        g.fill(x0 + 3, y0 + 3, x0 + 4, y0 + panelH - 3, 0xFF581C87);
        g.fill(x0 + panelW - 4, y0 + 3, x0 + panelW - 3, y0 + panelH - 3, 0xFF581C87);

        Component titleComp = Component.translatable("awakening_system.blackmarket.title");
        g.drawCenteredString(this.font, titleComp, cx, y0 + 8, 0xFFFFFFFF);

        Component dealerName = Component.translatable("entity.awakening_system.black_market_dealer");
        Component dealerLabel = Component.translatable("awakening_system.blackmarket.dealer_label", dealerName);
        g.drawCenteredString(this.font, dealerLabel, cx, y0 + 19, 0xFFAAAAAA);

        int hudY = y0 + 19;
        Component moraComp = Component.translatable("awakening_system.blackmarket.mora_format", NF.format(moraBalance));
        Component chipsComp = Component.translatable("awakening_system.blackmarket.chips_format", blackChipsCount);
        g.drawString(this.font, moraComp, x0 + 16, hudY, 0xFFFFD700);
        g.drawString(this.font, chipsComp, x0 + panelW - this.font.width(chipsComp) - 16, hudY, 0xFFD8B4FE);

        int contentY = y0 + 62;
        int cardH = 48;
        int cardStep = 52;
        int cardW = panelW - 32;

        if (currentTab == 0) {
            renderCard(g, x0 + 16, contentY, cardW, cardH,
                    new ItemStack(SystemItems.ILLEGAL_SLOT_MACHINE.get()),
                    Component.translatable("item.awakening_system.illegal_slot_machine"),
                    Component.translatable("awakening_system.blackmarket.card.slot.desc"),
                    Component.translatable("awakening_system.blackmarket.card.slot.cost"));

            renderCard(g, x0 + 16, contentY + cardStep, cardW, cardH,
                    new ItemStack(SystemItems.CLANDESTINE_CASHIER.get()),
                    Component.translatable("block.awakening_system.clandestine_cashier"),
                    Component.translatable("awakening_system.blackmarket.card.cashier.desc"),
                    Component.translatable("awakening_system.blackmarket.card.cashier.cost"));

            renderCard(g, x0 + 16, contentY + cardStep * 2, cardW, cardH,
                    new ItemStack(SystemItems.CLANDESTINE_VAULT_KEY.get()),
                    Component.translatable("item.awakening_system.clandestine_vault_key"),
                    Component.translatable("awakening_system.blackmarket.card.key.desc"),
                    Component.translatable("awakening_system.blackmarket.card.key.cost"));
        } else if (currentTab == 1) {
            renderCard(g, x0 + 16, contentY, cardW, cardH,
                    new ItemStack(SystemItems.ILLEGAL_BLACKJACK_TABLE.get()),
                    Component.translatable("block.awakening_system.illegal_blackjack_table"),
                    Component.translatable("awakening_system.blackmarket.card.blackjack.desc"),
                    Component.translatable("awakening_system.blackmarket.card.blackjack.cost"));

            renderCard(g, x0 + 16, contentY + cardStep, cardW, cardH,
                    new ItemStack(SystemItems.ILLEGAL_ROULETTE_TABLE.get()),
                    Component.translatable("block.awakening_system.illegal_roulette_table"),
                    Component.translatable("awakening_system.blackmarket.card.roulette.desc"),
                    Component.translatable("awakening_system.blackmarket.card.roulette.cost"));

            renderCard(g, x0 + 16, contentY + cardStep * 2, cardW, cardH,
                    new ItemStack(SystemItems.ILLEGAL_POOL_TABLE.get()),
                    Component.translatable("block.awakening_system.illegal_pool_table"),
                    Component.translatable("awakening_system.blackmarket.card.pool.desc"),
                    Component.translatable("awakening_system.blackmarket.card.pool.cost"));
        } else if (currentTab == 2) {
            renderCard(g, x0 + 16, contentY, cardW, cardH,
                    new ItemStack(SystemItems.DARK_STAMINA_ELIXIR.get(), 3),
                    Component.translatable("awakening_system.blackmarket.card.elixir.title"),
                    Component.translatable("awakening_system.blackmarket.card.elixir.desc"),
                    Component.translatable("awakening_system.blackmarket.card.elixir.cost"));

            renderCard(g, x0 + 16, contentY + cardStep, cardW, cardH,
                    new ItemStack(SystemItems.GOLDEN_GACHA_TICKET.get()),
                    Component.translatable("item.awakening_system.golden_gacha_ticket"),
                    Component.translatable("awakening_system.blackmarket.card.ticket.desc"),
                    Component.translatable("awakening_system.blackmarket.card.ticket.cost"));

            renderCard(g, x0 + 16, contentY + cardStep * 2, cardW, cardH,
                    new ItemStack(SystemItems.BLACK_MARKET_HATCH.get()),
                    Component.translatable("block.awakening_system.black_market_hatch"),
                    Component.translatable("awakening_system.blackmarket.card.hatch.desc"),
                    Component.translatable("awakening_system.blackmarket.card.hatch.cost"));
        } else {
            renderCard(g, x0 + 16, contentY, cardW, cardH,
                    new ItemStack(SystemItems.CASINO_CHIP_BLACK.get()),
                    Component.translatable("awakening_system.blackmarket.card.chip1.title"),
                    Component.translatable("awakening_system.blackmarket.card.chip1.desc"),
                    Component.translatable("awakening_system.blackmarket.card.chip1.cost"));

            renderCard(g, x0 + 16, contentY + cardStep, cardW, cardH,
                    new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), 10),
                    Component.translatable("awakening_system.blackmarket.card.chip10.title"),
                    Component.translatable("awakening_system.blackmarket.card.chip10.desc"),
                    Component.translatable("awakening_system.blackmarket.card.chip10.cost"));

            renderCard(g, x0 + 16, contentY + cardStep * 2, cardW, cardH,
                    new ItemStack(SystemItems.MORA.get()),
                    Component.translatable("awakening_system.blackmarket.card.liquidation.title"),
                    Component.translatable("awakening_system.blackmarket.card.liquidation.desc"),
                    Component.translatable("awakening_system.blackmarket.card.liquidation.cost"));
        }

        super.render(g, mX, mY, partialTick);
        g.pose().popPose();
    }

    private void renderCard(GuiGraphics g, int x, int y, int w, int h, ItemStack icon, Component title, Component desc, Component cost) {
        g.fill(x, y, x + w, y + h, 0xEE161024);
        g.renderOutline(x, y, w, h, 0xFF4A148C);

        g.renderItem(icon, x + 6, y + (h - 16) / 2);

        int textX = x + 28;
        int maxTextW = w - 28 - 104;
        String titleStr = title.getString();
        String descStr = desc.getString();
        String costStr = cost.getString();
        String safeTitle = this.font.width(titleStr) > maxTextW ? this.font.plainSubstrByWidth(titleStr, maxTextW - 6) + "…" : titleStr;
        String safeDesc = this.font.width(descStr) > maxTextW ? this.font.plainSubstrByWidth(descStr, maxTextW - 6) + "…" : descStr;
        String safeCost = this.font.width(costStr) > maxTextW ? this.font.plainSubstrByWidth(costStr, maxTextW - 6) + "…" : costStr;

        g.drawString(this.font, safeTitle, textX, y + 5, 0xFFFFFFFF);
        g.drawString(this.font, safeDesc, textX, y + 18, 0xFFAAAAAA);
        g.drawString(this.font, safeCost, textX, y + 31, 0xFFD8B4FE);
    }
}
