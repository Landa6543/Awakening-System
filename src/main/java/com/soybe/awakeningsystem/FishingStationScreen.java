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
    private int currentTab = 0; // 0 = Armas y Reliquias, 1 = Prototipos de Forja, 2 = Pescadería, 3 = Cocina
    private int listScroll = 0;

    public FishingStationScreen(OpenFishingStationPayload payload) {
        super(Component.literal("Estación del Gremio de Pescadores"));
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

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Fondo translúcido nítido sin activar el shader de desenfoque (blur)
        g.fill(0, 0, this.width, this.height, 0x55060D15);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

        int guiW = 380;
        int guiH = 240;
        int left = (this.width - guiW) / 2;
        int top = (this.height - guiH) / 2;

        // Fondo exterior estilizado (Azul marino y oro náutico)
        g.fill(left - 2, top - 2, left + guiW + 2, top + guiH + 2, 0xFF1B2A4A);
        g.fill(left, top, left + guiW, top + guiH, 0xFF0D1829);

        // Barra superior de encabezado
        g.fill(left, top, left + guiW, top + 36, 0xFF14243B);
        g.fill(left, top + 35, left + guiW, top + 36, 0xFF4A90E2);

        // Título del Gremio
        g.drawString(this.font, "§b§l✦ GREMIO DE PESCADORES DE TEYVAT ✦", left + 14, top + 8, 0xFFFFFFFF, false);
        g.drawString(this.font, "§7Peces: §e" + payload.totalFishCaught() + " capturados §8| §7Monedas: §6" + payload.coins() + " ⛃", left + 14, top + 22, 0xFFE0E0E0, false);

        // Pestañas de navegación
        int tabY = top + 42;
        String[] tabNames = {"§eArmas", "§6Prototipos", "§aPescadería", "§dCocina"};
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

        // Contenedor principal de contenido
        int contentLeft = left + 14;
        int contentTop = top + 66;
        int contentW = guiW - 28;
        int contentH = guiH - 76;

        g.fill(contentLeft, contentTop, contentLeft + contentW, contentTop + contentH, 0xFF101C2E);

        // Renderizado según la pestaña activa
        switch (currentTab) {
            case 0 -> renderWeaponsTab(g, contentLeft, contentTop, contentW, contentH, mouseX, mouseY);
            case 1 -> renderBilletsTab(g, contentLeft, contentTop, contentW, contentH, mouseX, mouseY);
            case 2 -> renderFishMarketTab(g, contentLeft, contentTop, contentW, contentH, mouseX, mouseY);
            case 3 -> renderCookingTab(g, contentLeft, contentTop, contentW, contentH, mouseX, mouseY);
        }

        // Botón de Cerrar en la esquina superior derecha
        int closeX = left + guiW - 24;
        int closeY = top + 8;
        boolean closeHover = (mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16);
        g.fill(closeX, closeY, closeX + 16, closeY + 16, closeHover ? 0xFFE74C3C : 0xFF34495E);
        g.drawString(this.font, "§f✕", closeX + 5, closeY + 4, 0xFFFFFFFF, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderWeaponsTab(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        // 4 Armas / Reliquias principales
        record WeaponOffer(String name, String desc, ItemStack icon, List<String> reqs, boolean canCraft, int actionId) {}

        boolean canCatch = payload.raimeiAngelfishCount() >= 6 && payload.crystalfishCount() >= 20 && payload.venomspineCount() >= 20;
        boolean canFleuve = payload.raimeiAngelfishCount() >= 4 && payload.crystalfishCount() >= 12 && payload.aizenMedakaCount() >= 12;
        boolean canStabilizer = payload.medakaCount() >= 3 && payload.sweetFlowerMedakaCount() >= 3;
        boolean canRod = payload.medakaCount() >= 5 && payload.aizenMedakaCount() >= 5 && payload.glazeMedakaCount() >= 5;

        List<WeaponOffer> offers = List.of(
                new WeaponOffer("✦ La Captura ✦ (4★)", "Tridente legendario con tremenda recarga (+35 Maná)",
                        new ItemStack(SystemItems.THE_CATCH.get()),
                        List.of("6x Raimei (" + payload.raimeiAngelfishCount() + "/6)", "20x Cristalino (" + payload.crystalfishCount() + "/20)", "20x Espina Venenosa (" + payload.venomspineCount() + "/20)"),
                        canCatch, 0),
                new WeaponOffer("✦ Vado del Río Ceniciento ✦ (4★)", "Espada tubería de Fontaine con alta cadencia",
                        new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()),
                        List.of("4x Raimei (" + payload.raimeiAngelfishCount() + "/4)", "12x Cristalino (" + payload.crystalfishCount() + "/12)", "12x Medaka Aizen (" + payload.aizenMedakaCount() + "/12)"),
                        canFleuve, 1),
                new WeaponOffer("✦ Estabilizador de Pesca ✦", "Accesorio que incrementa un +25% la velocidad y suerte",
                        new ItemStack(SystemItems.FISHING_LINE_STABILIZER.get()),
                        List.of("3x Medaka (" + payload.medakaCount() + "/3)", "3x Flor Dulce (" + payload.sweetFlowerMedakaCount() + "/3)"),
                        canStabilizer, 2),
                new WeaponOffer("✦ Caña del Maestro Pescador ✦", "Caña marina con Suerte Marina III, Señuelo III e Irrompible",
                        new ItemStack(Items.FISHING_ROD),
                        List.of("5x Medaka (" + payload.medakaCount() + "/5)", "5x Aizen (" + payload.aizenMedakaCount() + "/5)", "5x Esmaltado (" + payload.glazeMedakaCount() + "/5)"),
                        canRod, 3)
        );

        int cardH = 36;
        for (int i = 0; i < offers.size(); i++) {
            WeaponOffer offer = offers.get(i);
            int cy = y + 6 + i * (cardH + 4);

            g.fill(x + 4, cy, x + w - 4, cy + cardH, 0xFF172942);
            g.fill(x + 5, cy + 1, x + w - 5, cy + cardH - 1, 0xFF1C314E);

            // Icono del ítem
            g.renderItem(offer.icon(), x + 8, cy + 10);

            // Título y descripción
            g.drawString(this.font, "§e" + offer.name(), x + 30, cy + 4, 0xFFFFFFFF, false);
            g.drawString(this.font, "§7" + offer.desc(), x + 30, cy + 14, 0xFFAAAAAA, false);

            // Requisitos
            String reqText = "§fRequiere: " + String.join(" §8| §f", offer.reqs());
            g.drawString(this.font, reqText, x + 30, cy + 24, 0xFFE0E0E0, false);

            // Botón de canje
            int btnW = 60;
            int btnH = 20;
            int btnX = x + w - btnW - 10;
            int btnY = cy + 8;
            boolean can = offer.canCraft();
            boolean hover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

            int btnBg = can ? (hover ? 0xFF00C853 : 0xFF2E7D32) : 0xFF424242;
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
            String btnText = can ? "§a¡Canjear!" : "§7Faltan";
            g.drawString(this.font, btnText, btnX + (btnW - this.font.width(btnText)) / 2, btnY + 6, 0xFFFFFFFF, false);
        }
    }

    private void renderBilletsTab(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        // Prototipos de Forja
        record BilletOffer(String name, String series, ItemStack icon, int billetId) {}
        List<BilletOffer> billets = List.of(
                new BilletOffer("Prototipo de Espada", "Tierras del Norte", new ItemStack(SystemItems.NORTHLANDER_SWORD_BILLET.get()), 0),
                new BilletOffer("Prototipo de Mandoble", "Tierras del Norte", new ItemStack(SystemItems.NORTHLANDER_CLAYMORE_BILLET.get()), 1),
                new BilletOffer("Prototipo de Lanza", "Tierras del Norte", new ItemStack(SystemItems.NORTHLANDER_POLEARM_BILLET.get()), 2),
                new BilletOffer("Prototipo de Arco", "Tierras del Norte", new ItemStack(SystemItems.NORTHLANDER_BOW_BILLET.get()), 3),
                new BilletOffer("Prototipo de Catalizador", "Tierras del Norte", new ItemStack(SystemItems.NORTHLANDER_CATALYST_BILLET.get()), 4),
                new BilletOffer("Prototipo de Espada", "Tierras Centrales", new ItemStack(SystemItems.MIDLANDER_SWORD_BILLET.get()), 5),
                new BilletOffer("Prototipo de Mandoble", "Tierras Centrales", new ItemStack(SystemItems.MIDLANDER_CLAYMORE_BILLET.get()), 6),
                new BilletOffer("Prototipo de Lanza", "Tierras Centrales", new ItemStack(SystemItems.MIDLANDER_POLEARM_BILLET.get()), 7),
                new BilletOffer("Prototipo de Arco", "Tierras Centrales", new ItemStack(SystemItems.MIDLANDER_BOW_BILLET.get()), 8),
                new BilletOffer("Prototipo de Catalizador", "Tierras Centrales", new ItemStack(SystemItems.MIDLANDER_CATALYST_BILLET.get()), 9)
        );

        int startIdx = Math.max(0, Math.min(listScroll, billets.size() - 4));
        int cardH = 34;

        for (int i = 0; i < 4 && (startIdx + i) < billets.size(); i++) {
            BilletOffer b = billets.get(startIdx + i);
            int cy = y + 6 + i * (cardH + 4);

            boolean isMidlander = b.billetId() >= 5;
            int secFishCount = isMidlander ? payload.glazeMedakaCount() : payload.crystalfishCount();
            String secFishName = isMidlander ? "Esmaltado" : "Cristalino";
            boolean canBuy = payload.medakaCount() >= 10 && secFishCount >= 5;

            g.fill(x + 4, cy, x + w - 4, cy + cardH, 0xFF172942);
            g.renderItem(b.icon(), x + 8, cy + 9);

            g.drawString(this.font, "§6" + b.name() + " §7(" + b.series() + ")", x + 30, cy + 5, 0xFFFFFFFF, false);
            g.drawString(this.font, "§fCoste: 10x Medaka (" + payload.medakaCount() + "/10) §8| §f5x " + secFishName + " (" + secFishCount + "/5)", x + 30, cy + 18, 0xFFE0E0E0, false);

            int btnW = 56;
            int btnH = 18;
            int btnX = x + w - btnW - 10;
            int btnY = cy + 8;
            boolean hover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, canBuy ? (hover ? 0xFF00C853 : 0xFF2E7D32) : 0xFF424242);
            String btnText = canBuy ? "§aCanjear" : "§7Faltan";
            g.drawString(this.font, btnText, btnX + (btnW - this.font.width(btnText)) / 2, btnY + 5, 0xFFFFFFFF, false);
        }

        // Indicador de scroll
        g.drawString(this.font, "§8(Usa la rueda del ratón para ver más prototipos)", x + 20, y + h - 12, 0xFF888888, false);
    }

    private void renderFishMarketTab(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        // Venta de pescado por Monedas y Mora
        record FishMarketItem(String name, int price, int count, int fishId, ItemStack icon) {}
        List<FishMarketItem> fishList = List.of(
                new FishMarketItem("Medaka Común", 5, payload.medakaCount(), 0, new ItemStack(SystemItems.MEDAKA.get())),
                new FishMarketItem("Medaka Aizen", 8, payload.aizenMedakaCount(), 1, new ItemStack(SystemItems.AIZEN_MEDAKA.get())),
                new FishMarketItem("Medaka Flor Dulce", 8, payload.sweetFlowerMedakaCount(), 2, new ItemStack(SystemItems.SWEET_FLOWER_MEDAKA.get())),
                new FishMarketItem("Medaka Esmaltado", 10, payload.glazeMedakaCount(), 3, new ItemStack(SystemItems.GLAZE_MEDAKA.get())),
                new FishMarketItem("Pez Cristalino", 18, payload.crystalfishCount(), 4, new ItemStack(SystemItems.CRYSTALFISH.get())),
                new FishMarketItem("Pez Espina Venenosa", 20, payload.venomspineCount(), 5, new ItemStack(SystemItems.VENOMSPINE_FISH.get())),
                new FishMarketItem("Pez Ángel Sagrado", 45, payload.abidingAngelfishCount(), 6, new ItemStack(SystemItems.ABIDING_ANGELFISH.get())),
                new FishMarketItem("Pez Ángel Raimei", 60, payload.raimeiAngelfishCount(), 7, new ItemStack(SystemItems.RAIMEI_ANGELFISH.get()))
        );

        int startIdx = Math.max(0, Math.min(listScroll, fishList.size() - 4));
        int cardH = 34;

        for (int i = 0; i < 4 && (startIdx + i) < fishList.size(); i++) {
            FishMarketItem item = fishList.get(startIdx + i);
            int cy = y + 6 + i * (cardH + 4);

            g.fill(x + 4, cy, x + w - 4, cy + cardH, 0xFF172942);
            g.renderItem(item.icon(), x + 8, cy + 9);

            g.drawString(this.font, "§b" + item.name(), x + 30, cy + 5, 0xFFFFFFFF, false);
            g.drawString(this.font, "§7Tienes: §e" + item.count() + " §8| §7Precio: §6" + item.price() + " Monedas c/u", x + 30, cy + 18, 0xFFE0E0E0, false);

            // Botón Vender 1
            int b1X = x + w - 120;
            int b1Y = cy + 8;
            boolean canSell = item.count() > 0;
            boolean h1 = mouseX >= b1X && mouseX <= b1X + 50 && mouseY >= b1Y && mouseY <= b1Y + 18;
            g.fill(b1X, b1Y, b1X + 50, b1Y + 18, canSell ? (h1 ? 0xFF0288D1 : 0xFF0277BD) : 0xFF424242);
            g.drawString(this.font, "§fVender 1", b1X + 4, b1Y + 5, 0xFFFFFFFF, false);

            // Botón Vender Todos
            int b2X = x + w - 64;
            int b2Y = cy + 8;
            boolean h2 = mouseX >= b2X && mouseX <= b2X + 56 && mouseY >= b2Y && mouseY <= b2Y + 18;
            g.fill(b2X, b2Y, b2X + 56, b2Y + 18, canSell ? (h2 ? 0xFFF57C00 : 0xFFE65100) : 0xFF424242);
            g.drawString(this.font, "§fTodos", b2X + 12, b2Y + 5, 0xFFFFFFFF, false);
        }

        g.drawString(this.font, "§8(Ganas Monedas del Sistema directas por cada pez entregado)", x + 20, y + h - 12, 0xFF888888, false);
    }

    private void renderCookingTab(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        // Cocina de Pescador
        int cy = y + 16;
        g.fill(x + 12, cy, x + w - 12, cy + 50, 0xFF172942);
        g.fill(x + 13, cy + 1, x + w - 13, cy + 49, 0xFF1C314E);

        g.renderItem(new ItemStack(SystemItems.GRILLED_TIGER_FISH.get()), x + 20, cy + 16);
        g.drawString(this.font, "§6§l✦ Pescado Asado de Tigre ✦", x + 44, cy + 8, 0xFFFFFFFF, false);
        g.drawString(this.font, "§7Platillo tradicional de Liyue. Restaura 6 de comida y otorga Regeneración I.", x + 44, cy + 20, 0xFFAAAAAA, false);
        g.drawString(this.font, "§fCoste: 1x Cualquier Pez (Medaka, Bacalao o Salmón)", x + 44, cy + 32, 0xFFE0E0E0, false);

        int btnW = 60;
        int btnH = 22;
        int btnX = x + w - btnW - 24;
        int btnY = cy + 14;
        boolean canCook = payload.medakaCount() >= 1;
        boolean hover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, canCook ? (hover ? 0xFF00C853 : 0xFF2E7D32) : 0xFF424242);
        String txt = canCook ? "§a¡Cocinar!" : "§7Sin pez";
        g.drawString(this.font, txt, btnX + (btnW - this.font.width(txt)) / 2, btnY + 7, 0xFFFFFFFF, false);

        // Consejos del gremio
        g.drawString(this.font, "§b✦ Consejos de Pesca de Teyvat:", x + 16, cy + 70, 0xFFFFFFFF, false);
        g.drawString(this.font, "§7• Pesca en ríos y lagos de Teyvat para hallar peces exóticos.", x + 16, cy + 84, 0xFFCCCCCC, false);
        g.drawString(this.font, "§7• El Pez Ángel Raimei solo nada en aguas tormentosas de Inazuma.", x + 16, cy + 96, 0xFFCCCCCC, false);
        g.drawString(this.font, "§7• Equipa el Estabilizador de Pesca para facilitar capturas raras.", x + 16, cy + 108, 0xFFCCCCCC, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int guiW = 380;
        int guiH = 240;
        int left = (this.width - guiW) / 2;
        int top = (this.height - guiH) / 2;

        // Botón cerrar
        int closeX = left + guiW - 24;
        int closeY = top + 8;
        if (mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16) {
            onClose();
            return true;
        }

        // Pestañas
        int tabY = top + 42;
        int tabW = (guiW - 28 - 12) / 4;
        for (int i = 0; i < 4; i++) {
            int tx = left + 14 + i * (tabW + 4);
            if (mouseX >= tx && mouseX <= tx + tabW && mouseY >= tabY && mouseY <= tabY + 18) {
                currentTab = i;
                listScroll = 0;
                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.7F, 1.2F);
                return true;
            }
        }

        int contentLeft = left + 14;
        int contentTop = top + 66;
        int contentW = guiW - 28;

        if (currentTab == 0) { // Armas
            int cardH = 36;
            int btnW = 60;
            int btnH = 20;
            for (int i = 0; i < 4; i++) {
                int cy = contentTop + 6 + i * (cardH + 4);
                int btnX = contentLeft + contentW - btnW - 10;
                int btnY = cy + 8;
                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    PacketDistributor.sendToServer(new FishingStationActionPayload(0, i, 1));
                    return true;
                }
            }
        } else if (currentTab == 1) { // Billets
            int cardH = 34;
            int btnW = 56;
            int btnH = 18;
            int startIdx = Math.max(0, Math.min(listScroll, 10 - 4));
            for (int i = 0; i < 4 && (startIdx + i) < 10; i++) {
                int cy = contentTop + 6 + i * (cardH + 4);
                int btnX = contentLeft + contentW - btnW - 10;
                int btnY = cy + 8;
                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    PacketDistributor.sendToServer(new FishingStationActionPayload(1, startIdx + i, 1));
                    return true;
                }
            }
        } else if (currentTab == 2) { // Venta
            int cardH = 34;
            int startIdx = Math.max(0, Math.min(listScroll, 8 - 4));
            for (int i = 0; i < 4 && (startIdx + i) < 8; i++) {
                int cy = contentTop + 6 + i * (cardH + 4);
                int b1X = contentLeft + contentW - 120;
                int b1Y = cy + 8;
                int b2X = contentLeft + contentW - 64;
                int b2Y = cy + 8;
                if (mouseX >= b1X && mouseX <= b1X + 50 && mouseY >= b1Y && mouseY <= b1Y + 18) {
                    PacketDistributor.sendToServer(new FishingStationActionPayload(2, startIdx + i, 1));
                    return true;
                }
                if (mouseX >= b2X && mouseX <= b2X + 56 && mouseY >= b2Y && mouseY <= b2Y + 18) {
                    PacketDistributor.sendToServer(new FishingStationActionPayload(2, startIdx + i, -1)); // -1 = todos
                    return true;
                }
            }
        } else if (currentTab == 3) { // Cocina
            int cy = contentTop + 16;
            int btnW = 60;
            int btnH = 22;
            int btnX = contentLeft + contentW - btnW - 24;
            int btnY = cy + 14;
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                PacketDistributor.sendToServer(new FishingStationActionPayload(3, 0, 1));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
