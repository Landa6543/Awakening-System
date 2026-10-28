package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla interactiva de la Estación del Gremio de Pescadores de Teyvat.
 * Permite a los aventureros canjear peces raros por armas legendarias (La Captura, Vado del Río Ceniciento),
 * estabilizadores de sedal, prototipos de forja (Billets), cocinar pescado y vender capturas por monedas.
 */
public final class FishingStationScreen extends Screen {
    private final OpenFishingStationPayload payload;
    private int currentTab = 0;
    private int listScroll = 0;

    public FishingStationScreen(OpenFishingStationPayload payload) {
        super(Component.translatable("gui.awakening_system.fishing_station.title"));
        this.payload = payload;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = currentTab == 1 ? 6 : 4;
        if (scrollY > 0) {
            listScroll = Math.max(0, listScroll - 1);
            return true;
        } else if (scrollY < 0) {
            listScroll = Math.min(maxScroll, listScroll + 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 400.0F;
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(transformMouseX(mouseX), transformMouseY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(transformMouseX(mouseX), transformMouseY(mouseY), button, dragX, dragY);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0x55060D15);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

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

        int guiW = 400;
        int guiH = 240;
        int left = (this.width - guiW) / 2;
        int top = (this.height - guiH) / 2;

        g.fill(left - 2, top - 2, left + guiW + 2, top + guiH + 2, 0xFF1B2A4A);
        g.fill(left, top, left + guiW, top + guiH, 0xFF0D1829);

        g.fill(left, top, left + guiW, top + 36, 0xFF14243B);
        g.fill(left, top + 35, left + guiW, top + 36, 0xFF4A90E2);

        g.drawString(this.font, Component.translatable("fishing_station.title"), left + 14, top + 8, 0xFFFFFFFF, false);
        g.drawString(this.font, Component.translatable("fishing_station.stats", payload.totalFishCaught(), payload.coins()), left + 14, top + 22, 0xFFE0E0E0, false);

        int tabY = top + 42;
        Component[] tabNames = {
                Component.translatable("fishing_station.tab.weapons"),
                Component.translatable("fishing_station.tab.billets"),
                Component.translatable("fishing_station.tab.fish_market"),
                Component.translatable("fishing_station.tab.cooking")
        };
        int tabW = (guiW - 28 - 12) / 4;
        for (int i = 0; i < tabNames.length; i++) {
            int tx = left + 14 + i * (tabW + 4);
            boolean active = (currentTab == i);
            int bgColor = active ? 0xFF2A4D7C : 0xFF17283C;
            int borderColor = active ? 0xFF00E5FF : 0xFF233B58;

            g.fill(tx, tabY, tx + tabW, tabY + 18, borderColor);
            g.fill(tx + 1, tabY + 1, tx + tabW - 1, tabY + 17, bgColor);

            int textW = this.font.width(tabNames[i]);
            g.drawString(this.font, tabNames[i], tx + (tabW - textW) / 2, tabY + 5, 0xFFFFFFFF, false);
        }

        int contentLeft = left + 14;
        int contentTop = top + 66;
        int contentW = guiW - 28;
        int contentH = guiH - 76;

        g.fill(contentLeft, contentTop, contentLeft + contentW, contentTop + contentH, 0xFF101C2E);

        switch (currentTab) {
            case 0 -> renderWeaponsTab(g, contentLeft, contentTop, contentW, contentH, mX, mY);
            case 1 -> renderBilletsTab(g, contentLeft, contentTop, contentW, contentH, mX, mY);
            case 2 -> renderFishMarketTab(g, contentLeft, contentTop, contentW, contentH, mX, mY);
            case 3 -> renderCookingTab(g, contentLeft, contentTop, contentW, contentH, mX, mY);
        }

        int closeX = left + guiW - 24;
        int closeY = top + 8;
        boolean closeHover = (mX >= closeX && mX <= closeX + 16 && mY >= closeY && mY <= closeY + 16);
        g.fill(closeX, closeY, closeX + 16, closeY + 16, closeHover ? 0xFFE74C3C : 0xFF34495E);
        g.drawString(this.font, "§f✕", closeX + 5, closeY + 4, 0xFFFFFFFF, false);

        super.render(g, mX, mY, partialTick);
        g.pose().popPose();
    }

    private void renderWeaponsTab(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        record WeaponOffer(String name, String desc, ItemStack icon, List<String> reqs, boolean canCraft, int actionId) {}

        boolean canCatch = payload.raimeiAngelfishCount() >= 6 && payload.crystalfishCount() >= 20 && payload.venomspineCount() >= 20;
        boolean canFleuve = payload.raimeiAngelfishCount() >= 4 && payload.crystalfishCount() >= 12 && payload.aizenMedakaCount() >= 12;
        boolean canStabilizer = payload.medakaCount() >= 3 && payload.sweetFlowerMedakaCount() >= 3;
        boolean canRod = payload.medakaCount() >= 5 && payload.aizenMedakaCount() >= 5 && payload.glazeMedakaCount() >= 5;

        List<WeaponOffer> offers = List.of(
                new WeaponOffer("✦ " + Component.translatable("item.awakening_system.the_catch").getString() + " ✦ (4★)",
                        Component.translatable("fishing_station.offer.the_catch.desc").getString(),
                        new ItemStack(SystemItems.THE_CATCH.get()),
                        List.of("Raimei: " + payload.raimeiAngelfishCount() + "/6", "Cristal: " + payload.crystalfishCount() + "/20", "Espina: " + payload.venomspineCount() + "/20"),
                        canCatch, 0),
                new WeaponOffer("✦ " + Component.translatable("item.awakening_system.fleuve_cendre_ferryman").getString() + " ✦ (4★)",
                        Component.translatable("fishing_station.offer.fleuve.desc").getString(),
                        new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()),
                        List.of("Raimei: " + payload.raimeiAngelfishCount() + "/4", "Cristal: " + payload.crystalfishCount() + "/12", "Aizen: " + payload.aizenMedakaCount() + "/12"),
                        canFleuve, 1),
                new WeaponOffer("✦ " + Component.translatable("fishing_station.offer.stabilizer.name").getString() + " ✦",
                        Component.translatable("fishing_station.offer.stabilizer.desc").getString(),
                        new ItemStack(SystemItems.FISHING_LINE_STABILIZER.get()),
                        List.of("Medaka: " + payload.medakaCount() + "/3", "Flor Dulce: " + payload.sweetFlowerMedakaCount() + "/3"),
                        canStabilizer, 2),
                new WeaponOffer("✦ " + Component.translatable("fishing_station.offer.rod.name").getString() + " ✦",
                        Component.translatable("fishing_station.offer.rod.desc").getString(),
                        new ItemStack(Items.FISHING_ROD),
                        List.of("Medaka: " + payload.medakaCount() + "/5", "Aizen: " + payload.aizenMedakaCount() + "/5", "Esmaltado: " + payload.glazeMedakaCount() + "/5"),
                        canRod, 3)
        );

        int cardH = 36;
        for (int i = 0; i < offers.size(); i++) {
            WeaponOffer offer = offers.get(i);
            int cy = y + 6 + i * (cardH + 4);

            g.fill(x + 4, cy, x + w - 4, cy + cardH, 0xFF172942);
            g.fill(x + 5, cy + 1, x + w - 5, cy + cardH - 1, 0xFF1C314E);

            g.renderItem(offer.icon(), x + 8, cy + 10);

            int btnW = 56;
            int btnH = 20;
            int btnX = x + w - btnW - 8;
            int btnY = cy + 8;
            boolean can = offer.canCraft();
            boolean hover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

            int maxTextW = btnX - (x + 30) - 6;

            String nameText = font.plainSubstrByWidth(offer.name(), maxTextW);
            g.drawString(this.font, "§e" + nameText, x + 30, cy + 4, 0xFFFFFFFF, false);

            String descText = font.plainSubstrByWidth(offer.desc(), maxTextW);
            g.drawString(this.font, "§7" + descText, x + 30, cy + 14, 0xFFAAAAAA, false);

            String reqPrefix = Component.translatable("fishing_station.req_prefix").getString();
            String reqText = font.plainSubstrByWidth(reqPrefix + ": " + String.join(" §8| §f", offer.reqs()), maxTextW);
            g.drawString(this.font, "§f" + reqText, x + 30, cy + 24, 0xFFE0E0E0, false);

            int btnBg = can ? (hover ? 0xFF00C853 : 0xFF2E7D32) : 0xFF424242;
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
            Component btnComp = can ? Component.translatable("fishing_station.btn.claim") : Component.translatable("fishing_station.btn.missing");
            g.drawCenteredString(this.font, btnComp, btnX + btnW / 2, btnY + 6, 0xFFFFFFFF);
        }
    }

    private void renderBilletsTab(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        record BilletOffer(ItemStack icon, int billetId) {}
        List<BilletOffer> billets = List.of(
                new BilletOffer(new ItemStack(SystemItems.NORTHLANDER_SWORD_BILLET.get()), 0),
                new BilletOffer(new ItemStack(SystemItems.NORTHLANDER_CLAYMORE_BILLET.get()), 1),
                new BilletOffer(new ItemStack(SystemItems.NORTHLANDER_POLEARM_BILLET.get()), 2),
                new BilletOffer(new ItemStack(SystemItems.NORTHLANDER_BOW_BILLET.get()), 3),
                new BilletOffer(new ItemStack(SystemItems.NORTHLANDER_CATALYST_BILLET.get()), 4),
                new BilletOffer(new ItemStack(SystemItems.MIDLANDER_SWORD_BILLET.get()), 5),
                new BilletOffer(new ItemStack(SystemItems.MIDLANDER_CLAYMORE_BILLET.get()), 6),
                new BilletOffer(new ItemStack(SystemItems.MIDLANDER_POLEARM_BILLET.get()), 7),
                new BilletOffer(new ItemStack(SystemItems.MIDLANDER_BOW_BILLET.get()), 8),
                new BilletOffer(new ItemStack(SystemItems.MIDLANDER_CATALYST_BILLET.get()), 9)
        );

        int startIdx = Math.max(0, Math.min(listScroll, billets.size() - 4));
        int cardH = 34;

        for (int i = 0; i < 4 && (startIdx + i) < billets.size(); i++) {
            BilletOffer b = billets.get(startIdx + i);
            int cy = y + 6 + i * (cardH + 4);

            boolean isMidlander = b.billetId() >= 5;
            int secFishCount = isMidlander ? payload.glazeMedakaCount() : payload.crystalfishCount();
            String secFishName = isMidlander ? Component.translatable("item.awakening_system.glaze_medaka_item").getString() : Component.translatable("item.awakening_system.crystalfish_item").getString();
            boolean canBuy = payload.medakaCount() >= 10 && secFishCount >= 5;

            g.fill(x + 4, cy, x + w - 4, cy + cardH, 0xFF172942);
            g.renderItem(b.icon(), x + 8, cy + 9);

            int btnW = 56;
            int btnH = 18;
            int btnX = x + w - btnW - 8;
            int btnY = cy + 8;
            int maxTextW = btnX - (x + 30) - 6;

            String billetName = Component.translatable(b.icon().getItem().getDescriptionId()).getString();
            String seriesName = Component.translatable("item.awakening_system.forge_billet.series_name." + (isMidlander ? "midlander" : "northlander")).getString();
            String fullTitle = font.plainSubstrByWidth("§6" + billetName + " §7(" + seriesName + ")", maxTextW);
            g.drawString(this.font, fullTitle, x + 30, cy + 5, 0xFFFFFFFF, false);

            String costText = Component.translatable("fishing_station.billet.cost", payload.medakaCount(), secFishName, secFishCount).getString();
            g.drawString(this.font, "§f" + font.plainSubstrByWidth(costText, maxTextW), x + 30, cy + 18, 0xFFE0E0E0, false);

            boolean hover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, canBuy ? (hover ? 0xFF00C853 : 0xFF2E7D32) : 0xFF424242);
            Component btnComp = canBuy ? Component.translatable("fishing_station.btn.claim") : Component.translatable("fishing_station.btn.missing");
            g.drawCenteredString(this.font, btnComp, btnX + btnW / 2, btnY + 5, 0xFFFFFFFF);
        }

        g.drawString(this.font, Component.translatable("fishing_station.scroll_hint"), x + 20, y + h - 12, 0xFF888888, false);
    }

    private void renderFishMarketTab(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        record FishMarketItem(int price, int count, int fishId, ItemStack icon) {}
        List<FishMarketItem> fishList = List.of(
                new FishMarketItem(5, payload.medakaCount(), 0, new ItemStack(SystemItems.MEDAKA.get())),
                new FishMarketItem(8, payload.aizenMedakaCount(), 1, new ItemStack(SystemItems.AIZEN_MEDAKA.get())),
                new FishMarketItem(8, payload.sweetFlowerMedakaCount(), 2, new ItemStack(SystemItems.SWEET_FLOWER_MEDAKA.get())),
                new FishMarketItem(10, payload.glazeMedakaCount(), 3, new ItemStack(SystemItems.GLAZE_MEDAKA.get())),
                new FishMarketItem(18, payload.crystalfishCount(), 4, new ItemStack(SystemItems.CRYSTALFISH.get())),
                new FishMarketItem(20, payload.venomspineCount(), 5, new ItemStack(SystemItems.VENOMSPINE_FISH.get())),
                new FishMarketItem(45, payload.abidingAngelfishCount(), 6, new ItemStack(SystemItems.ABIDING_ANGELFISH.get())),
                new FishMarketItem(60, payload.raimeiAngelfishCount(), 7, new ItemStack(SystemItems.RAIMEI_ANGELFISH.get()))
        );

        int startIdx = Math.max(0, Math.min(listScroll, fishList.size() - 4));
        int cardH = 34;

        for (int i = 0; i < 4 && (startIdx + i) < fishList.size(); i++) {
            FishMarketItem item = fishList.get(startIdx + i);
            int cy = y + 6 + i * (cardH + 4);

            g.fill(x + 4, cy, x + w - 4, cy + cardH, 0xFF172942);
            g.renderItem(item.icon(), x + 8, cy + 9);

            String fishName = Component.translatable(item.icon().getItem().getDescriptionId()).getString();
            g.drawString(this.font, "§b" + font.plainSubstrByWidth(fishName, w - 145), x + 30, cy + 5, 0xFFFFFFFF, false);
            g.drawString(this.font, Component.translatable("fishing_station.market.info", item.count(), item.price()), x + 30, cy + 18, 0xFFE0E0E0, false);

            int b1X = x + w - 120;
            int b1Y = cy + 8;
            boolean canSell = item.count() > 0;
            boolean h1 = mouseX >= b1X && mouseX <= b1X + 50 && mouseY >= b1Y && mouseY <= b1Y + 18;
            g.fill(b1X, b1Y, b1X + 50, b1Y + 18, canSell ? (h1 ? 0xFF0288D1 : 0xFF0277BD) : 0xFF424242);
            g.drawCenteredString(this.font, Component.translatable("fishing_station.btn.sell_one"), b1X + 25, b1Y + 5, 0xFFFFFFFF);

            int b2X = x + w - 64;
            int b2Y = cy + 8;
            boolean h2 = mouseX >= b2X && mouseX <= b2X + 56 && mouseY >= b2Y && mouseY <= b2Y + 18;
            g.fill(b2X, b2Y, b2X + 56, b2Y + 18, canSell ? (h2 ? 0xFFF57C00 : 0xFFE65100) : 0xFF424242);
            g.drawCenteredString(this.font, Component.translatable("fishing_station.btn.sell_all"), b2X + 28, b2Y + 5, 0xFFFFFFFF);
        }

        g.drawString(this.font, Component.translatable("fishing_station.market.hint"), x + 20, y + h - 12, 0xFF888888, false);
    }

    private void renderCookingTab(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        int cy = y + 16;
        g.fill(x + 12, cy, x + w - 12, cy + 50, 0xFF172942);
        g.fill(x + 13, cy + 1, x + w - 13, cy + 49, 0xFF1C314E);

        g.renderItem(new ItemStack(SystemItems.GRILLED_TIGER_FISH.get()), x + 20, cy + 16);
        g.drawString(this.font, "§6§l✦ " + Component.translatable("fishing_station.cooking.title").getString() + " ✦", x + 44, cy + 8, 0xFFFFFFFF, false);
        g.drawString(this.font, "§7" + font.plainSubstrByWidth(Component.translatable("fishing_station.cooking.desc").getString(), w - 140), x + 44, cy + 20, 0xFFAAAAAA, false);
        g.drawString(this.font, "§f" + font.plainSubstrByWidth(Component.translatable("fishing_station.cooking.cost").getString(), w - 140), x + 44, cy + 32, 0xFFE0E0E0, false);

        int btnW = 60;
        int btnH = 22;
        int btnX = x + w - btnW - 24;
        int btnY = cy + 14;
        boolean canCook = payload.medakaCount() >= 1;
        boolean hover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, canCook ? (hover ? 0xFF00C853 : 0xFF2E7D32) : 0xFF424242);
        Component cookText = canCook ? Component.translatable("fishing_station.btn.cook") : Component.translatable("fishing_station.btn.no_fish");
        g.drawCenteredString(this.font, cookText, btnX + btnW / 2, btnY + 7, 0xFFFFFFFF);

        g.drawString(this.font, Component.translatable("fishing_station.cooking.tips_title"), x + 16, cy + 70, 0xFFFFFFFF, false);
        g.drawString(this.font, Component.translatable("fishing_station.cooking.tip1"), x + 16, cy + 84, 0xFFCCCCCC, false);
        g.drawString(this.font, Component.translatable("fishing_station.cooking.tip2"), x + 16, cy + 96, 0xFFCCCCCC, false);
        g.drawString(this.font, Component.translatable("fishing_station.cooking.tip3"), x + 16, cy + 108, 0xFFCCCCCC, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        double mX = transformMouseX(mouseX);
        double mY = transformMouseY(mouseY);

        int guiW = 400;
        int guiH = 240;
        int left = (this.width - guiW) / 2;
        int top = (this.height - guiH) / 2;

        int closeX = left + guiW - 24;
        int closeY = top + 8;
        if (mX >= closeX && mX <= closeX + 16 && mY >= closeY && mY <= closeY + 16) {
            onClose();
            return true;
        }

        int tabY = top + 42;
        int tabW = (guiW - 28 - 12) / 4;
        for (int i = 0; i < 4; i++) {
            int tx = left + 14 + i * (tabW + 4);
            if (mX >= tx && mX <= tx + tabW && mY >= tabY && mY <= tabY + 18) {
                currentTab = i;
                listScroll = 0;
                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.7F, 1.2F);
                return true;
            }
        }

        int contentLeft = left + 14;
        int contentTop = top + 66;
        int contentW = guiW - 28;

        if (currentTab == 0) {
            int cardH = 36;
            int btnW = 56;
            int btnH = 20;
            for (int i = 0; i < 4; i++) {
                int cy = contentTop + 6 + i * (cardH + 4);
                int btnX = contentLeft + contentW - btnW - 8;
                int btnY = cy + 8;
                if (mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH) {
                    PacketDistributor.sendToServer(new FishingStationActionPayload(0, i, 1));
                    return true;
                }
            }
        } else if (currentTab == 1) {
            int cardH = 34;
            int btnW = 56;
            int btnH = 18;
            int startIdx = Math.max(0, Math.min(listScroll, 10 - 4));
            for (int i = 0; i < 4 && (startIdx + i) < 10; i++) {
                int cy = contentTop + 6 + i * (cardH + 4);
                int btnX = contentLeft + contentW - btnW - 8;
                int btnY = cy + 8;
                if (mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH) {
                    PacketDistributor.sendToServer(new FishingStationActionPayload(1, startIdx + i, 1));
                    return true;
                }
            }
        } else if (currentTab == 2) {
            int cardH = 34;
            int startIdx = Math.max(0, Math.min(listScroll, 8 - 4));
            for (int i = 0; i < 4 && (startIdx + i) < 8; i++) {
                int cy = contentTop + 6 + i * (cardH + 4);
                int b1X = contentLeft + contentW - 120;
                int b1Y = cy + 8;
                int b2X = contentLeft + contentW - 64;
                int b2Y = cy + 8;
                if (mX >= b1X && mX <= b1X + 50 && mY >= b1Y && mY <= b1Y + 18) {
                    PacketDistributor.sendToServer(new FishingStationActionPayload(2, startIdx + i, 1));
                    return true;
                }
                if (mX >= b2X && mX <= b2X + 56 && mY >= b2Y && mY <= b2Y + 18) {
                    PacketDistributor.sendToServer(new FishingStationActionPayload(2, startIdx + i, -1));
                    return true;
                }
            }
        } else if (currentTab == 3) {
            int cy = contentTop + 16;
            int btnW = 60;
            int btnH = 22;
            int btnX = contentLeft + contentW - btnW - 24;
            int btnY = cy + 14;
            if (mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH) {
                PacketDistributor.sendToServer(new FishingStationActionPayload(3, 0, 1));
                return true;
            }
        }

        return super.mouseClicked(mX, mY, button);
    }
}
