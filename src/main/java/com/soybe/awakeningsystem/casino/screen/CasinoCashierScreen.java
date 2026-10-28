package com.soybe.awakeningsystem.casino.screen;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.casino.network.CasinoExchangeC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class CasinoCashierScreen extends Screen {

    private static final ResourceLocation MORA_ICON = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/item/mora.png");

    private long moraBalance;

    private static final int[] CHIP_VALUES = { 100, 1000, 10000, 100000 };
    private static final String[] CHIP_COLORS = { "§6", "§f", "§e", "§b" };

    public CasinoCashierScreen(long moraBalance) {
        super(Component.translatable("awakening_system.screen.casino_cashier.title"));
        this.moraBalance = moraBalance;
    }

    public void updateBalance(long newBalance) {
        this.moraBalance = newBalance;
    }

    private static final int PANEL_W = 420;
    private static final int PANEL_H = 205;
    private static final int ROW_HEIGHT = 28;

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int y0 = (this.height - PANEL_H) / 2;
        int startY = y0 + 52;

        for (int i = 0; i < 4; i++) {
            final int tierIndex = i;
            int y = startY + i * ROW_HEIGHT;

            this.addRenderableWidget(new RpgCasinoButton(cx + 14, y + 2, 36, 18,
                    Component.literal("§a+1"), b -> buyChip(tierIndex, 1),
                    0xFF1B5E20, 0xFF00E676, 0xDD0D2314, 0xEE163B23, 0xFFA5D6A7, 0xFFFFFFFF));

            this.addRenderableWidget(new RpgCasinoButton(cx + 54, y + 2, 40, 18,
                    Component.literal("§a+10"), b -> buyChip(tierIndex, 10),
                    0xFF00695C, 0xFF00E5FF, 0xDD0B2421, 0xEE143D39, 0xFF80CBC4, 0xFFFFFFFF));

            this.addRenderableWidget(new RpgCasinoButton(cx + 98, y + 2, 36, 18,
                    Component.literal("§c-1"), b -> sellChip(tierIndex, 1),
                    0xFF7F1D1D, 0xFFFF5252, 0xDD280F12, 0xEE42181D, 0xFFEF9A9A, 0xFFFFFFFF));

            this.addRenderableWidget(new RpgCasinoButton(cx + 138, y + 2, 46, 18,
                    Component.literal("§6").append(Component.translatable("awakening_system.screen.casino_cashier.btn_all")), b -> sellChip(tierIndex, -1),
                    0xFFB45309, 0xFFFFD700, 0xDD2C1B06, 0xEE4B2E0A, 0xFFFFD54F, 0xFFFFFFFF));
        }

        this.addRenderableWidget(new RpgCasinoButton(cx - 75, y0 + PANEL_H - 26, 150, 20,
                Component.literal("§c").append(Component.translatable("awakening_system.screen.casino_cashier.btn_close")), b -> this.onClose(),
                0xFF475569, 0xFFFF5252, 0xDD18202C, 0xEE2A1218, 0xFFCBD5E1, 0xFFFFFFFF));
    }

    private void buyChip(int tierIndex, int amount) {
        PacketDistributor.sendToServer(new CasinoExchangeC2SPayload(0, tierIndex, amount));
    }

    private void sellChip(int tierIndex, int amount) {
        PacketDistributor.sendToServer(new CasinoExchangeC2SPayload(1, tierIndex, amount));
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / (PANEL_W + 20.0F);
        float scaleY = (float) this.height / (PANEL_H + 20.0F);
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
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0x88000000, 0x99000000);

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
        int x0 = cx - PANEL_W / 2;
        int y0 = (this.height - PANEL_H) / 2;

        g.fill(x0, y0, x0 + PANEL_W, y0 + PANEL_H, 0xF50D111A);
        g.fill(x0, y0, x0 + PANEL_W, y0 + 2, 0xFFFFD700);
        g.fill(x0, y0 + PANEL_H - 2, x0 + PANEL_W, y0 + PANEL_H, 0xFFFFD700);
        g.fill(x0, y0, x0 + 2, y0 + PANEL_H, 0xFFFFD700);
        g.fill(x0 + PANEL_W - 2, y0, x0 + PANEL_W, y0 + PANEL_H, 0xFFFFD700);

        g.fill(x0 + 4, y0 + 4, x0 + PANEL_W - 4, y0 + 5, 0xFF8A6D2B);
        g.fill(x0 + 4, y0 + PANEL_H - 5, x0 + PANEL_W - 4, y0 + PANEL_H - 4, 0xFF8A6D2B);
        g.fill(x0 + 4, y0 + 4, x0 + 5, y0 + PANEL_H - 4, 0xFF8A6D2B);
        g.fill(x0 + PANEL_W - 5, y0 + 4, x0 + PANEL_W - 4, y0 + PANEL_H - 4, 0xFF8A6D2B);

        g.drawCenteredString(this.font, Component.translatable("awakening_system.screen.casino_cashier.header"), cx, y0 + 8, 0xFFFFFFFF);
        g.drawCenteredString(this.font, Component.translatable("awakening_system.screen.casino_cashier.subtitle"), cx, y0 + 19, 0xFFAAAAAA);

        g.fill(cx - 120, y0 + 31, cx + 120, y0 + 46, 0x88202A3E);
        g.fill(cx - 120, y0 + 31, cx + 120, y0 + 32, 0xAAFFD700);
        g.fill(cx - 120, y0 + 45, cx + 120, y0 + 46, 0xAAFFD700);
        Component moraText = Component.translatable("awakening_system.screen.casino_cashier.balance", String.format("%,d", this.moraBalance));
        g.drawCenteredString(this.font, moraText, cx, y0 + 34, 0xFFFFFFFF);

        int startY = y0 + 52;
        ItemStack[] chipIcons = {
                new ItemStack(SystemItems.CASINO_CHIP_BRONZE.get()),
                new ItemStack(SystemItems.CASINO_CHIP_SILVER.get()),
                new ItemStack(SystemItems.CASINO_CHIP_GOLD.get()),
                new ItemStack(SystemItems.CASINO_CHIP_PLATINUM.get())
        };

        for (int i = 0; i < 4; i++) {
            int y = startY + i * ROW_HEIGHT;

            int rowBg = (i % 2 == 0) ? 0x401C2638 : 0x2B131D2D;
            g.fill(x0 + 8, y, x0 + PANEL_W - 8, y + 23, rowBg);
            g.fill(x0 + 8, y, x0 + 10, y + 23, 0x88FFD700);

            g.renderItem(chipIcons[i], x0 + 14, y + 3);

            Component title = Component.literal(CHIP_COLORS[i] + "§l").append(chipIcons[i].getHoverName());
            Component cost = Component.translatable("awakening_system.screen.casino_cashier.value", String.format("%,d", CHIP_VALUES[i]));
            g.drawString(this.font, title, x0 + 36, y + 3, 0xFFFFFFFF, false);
            g.drawString(this.font, cost, x0 + 36, y + 13, 0xFFAAAAAA, false);

            int inInv = countChipsInInventory(chipIcons[i]);
            Component inInvStr = Component.translatable("awakening_system.screen.casino_cashier.in_hand", (inInv > 0 ? "§a" + inInv : "§80"));
            int inInvW = this.font.width(inInvStr);
            g.drawString(this.font, inInvStr, cx + 6 - inInvW, y + 7, 0xFFFFFFFF, false);
        }

        g.drawCenteredString(this.font, Component.translatable("awakening_system.screen.casino_cashier.footer"), cx, y0 + PANEL_H - 38, 0xFFAAAAAA);

        super.render(g, mX, mY, partialTick);
        g.pose().popPose();
    }

    private int countChipsInInventory(ItemStack chip) {
        if (this.minecraft == null || this.minecraft.player == null) return 0;
        int count = 0;
        for (ItemStack st : this.minecraft.player.getInventory().items) {
            if (ItemStack.isSameItem(st, chip)) {
                count += st.getCount();
            }
        }
        return count;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static class RpgCasinoButton extends Button {
        private final int baseBorder;
        private final int hoverBorder;
        private final int baseBg;
        private final int hoverBg;
        private final int normalTextColor;
        private final int hoverTextColor;

        public RpgCasinoButton(int x, int y, int width, int height, Component message, OnPress onPress,
                               int baseBorder, int hoverBorder, int baseBg, int hoverBg, int normalTextColor, int hoverTextColor) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.baseBorder = baseBorder;
            this.hoverBorder = hoverBorder;
            this.baseBg = baseBg;
            this.hoverBg = hoverBg;
            this.normalTextColor = normalTextColor;
            this.hoverTextColor = hoverTextColor;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            boolean hov = this.isHovered();
            int bg = !this.active ? 0xCC111620 : (hov ? this.hoverBg : this.baseBg);
            int border = !this.active ? 0xFF2A3445 : (hov ? this.hoverBorder : this.baseBorder);

            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, border);
            g.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, border);
            g.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, border);
            g.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, border);

            int textColor = !this.active ? 0xFF667788 : (hov ? this.hoverTextColor : this.normalTextColor);
            Minecraft mc = Minecraft.getInstance();
            g.drawCenteredString(mc.font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
        }
    }
}
