import re

path = "src/main/java/com/soybe/awakeningsystem/GachaWishScreen.java"
with open(path, "r", encoding="utf-8") as f:
    code = f.read()

# 1. Update subtabs in render()
old_subtabs = """        // 2. Sub-modos en Fila 1: [Personajes 5★] [Armas 5★] [Permanente] [Principiantes]
        boolean hasNovice = beginnerPulls < 20;
        int numSubTabs = hasNovice ? 4 : 3;
        int subTabW = hasNovice ? 94 : 110;
        int subTabH = 17;
        int subModeStartX = exitX + exitW + 6;

        String[] subTitles = hasNovice
                ? new String[]{"✦ PERSONAJES 5★", "✦ ARMAS 5★", "✦ PERMANENTE", "✦ PRINCIPIANTES"}
                : new String[]{"✦ PERSONAJES 5★", "✦ ARMAS 5★", "✦ PERMANENTE"};

        for (int i = 0; i < numSubTabs; i++) {
            int sx = subModeStartX + i * (subTabW + 4);
            boolean isSelected;
            if (i == 0) isSelected = isPromotionalCharacterBanner();
            else if (i == 1) isSelected = (activeBanner == 0);
            else if (i == 2) isSelected = (activeBanner == 2);
            else isSelected = (activeBanner == 3);"""

new_subtabs = """        // 2. Sub-modos en Fila 1: [Promocional] [Armas 5★] [Permanente] [Principiantes] [Tienda USD]
        boolean hasNovice = beginnerPulls < 20;
        int numSubTabs = hasNovice ? 5 : 4;
        int subTabW = hasNovice ? 80 : 96;
        int subTabH = 17;
        int subModeStartX = exitX + exitW + 6;

        String[] subTitles = hasNovice
                ? new String[]{"✦ PROMOCIONAL", "✦ ARMAS 5★", "✦ PERMANENTE", "✦ PRINCIPIANTES", "✦ RECARGAS USD"}
                : new String[]{"✦ PROMOCIONAL", "✦ ARMAS 5★", "✦ PERMANENTE", "✦ RECARGAS USD"};

        for (int i = 0; i < numSubTabs; i++) {
            int sx = subModeStartX + i * (subTabW + 4);
            boolean isSelected;
            if (i == 0) isSelected = isPromotionalCharacterBanner() && !showTopUpModal;
            else if (i == 1) isSelected = (activeBanner == 0) && !showTopUpModal;
            else if (i == 2) isSelected = (activeBanner == 2) && !showTopUpModal;
            else if (hasNovice && i == 3) isSelected = (activeBanner == 3) && !showTopUpModal;
            else isSelected = showTopUpModal;"""

code = code.replace(old_subtabs, new_subtabs, 1)

# 2. Add renderTopUpModal in render()
render_modal_anchor = """        if (showShopModal) {
            renderShopModal(graphics, mouseX, mouseY);
        }"""

new_render_modal = """        if (showShopModal) {
            renderShopModal(graphics, mouseX, mouseY);
        }

        if (showTopUpModal) {
            renderTopUpModal(graphics, mouseX, mouseY);
        }"""

code = code.replace(render_modal_anchor, new_render_modal, 1)

# 3. Add renderTopUpModal method
top_up_method = """    private void renderTopUpModal(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(0, 0, this.width, this.height, 0xEE05070E);

        int mW = 520;
        int mH = 270;
        int mX = this.width / 2 - mW / 2;
        int mY = this.height / 2 - mH / 2;

        graphics.fill(mX, mY, mX + mW, mY + mH, 0xFF141926);
        graphics.renderOutline(mX, mY, mW, mH, 0xFFFFD700);
        graphics.renderOutline(mX + 2, mY + 2, mW - 4, mH - 4, 0x33FFFFFF);

        // Cabecera
        graphics.drawString(this.font, "§6§l✦ TIENDA DE RECARGAS & GIFT CARDS MULTIVERSO ✦", mX + 14, mY + 10, 0xFFFFD700, false);

        // Botón Cerrar [X]
        int closeX = mX + mW - 22;
        int closeY = mY + 10;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 14 && mouseY >= closeY && mouseY <= closeY + 14;
        graphics.drawString(this.font, hoverClose ? "§c[X]" : "§7[X]", closeX, closeY, 0xFFFFFF, false);

        // Barra de Balances
        String balStr = "§6🪙 " + String.format(java.util.Locale.US, "%,d", coins) + " §7(5k=$1) | §a💳 $" + String.format(java.util.Locale.US, "%.2f", usdBalance) + " USD | §b💎 " + genesisCrystals + " | §d💠 " + stellarJade + (welkinDays > 0 ? " §6| 🌙 " + welkinDays + "d" : "");
        renderFittedText(graphics, balStr, mX + 14, mY + 24, mW - 28, 0xFFE0E0E0, false);

        int colW = 240;
        int panelY = mY + 38;

        // PANEL IZQUIERDO: GIFT CARDS
        graphics.fill(mX + 12, panelY, mX + 12 + colW, panelY + 185, 0xAA0D121D);
        graphics.renderOutline(mX + 12, panelY, colW, 185, 0xFF334155);
        graphics.drawString(this.font, "§a§lTARJETAS DE REGALO (GIFT CARDS)", mX + 18, panelY + 6, 0xFF81C784, false);

        int[] gcVals = {5, 15, 30, 50, 100};
        int[] gcCosts = {25000, 75000, 150000, 250000, 500000};
        int[] gcColors = {0xFF4CAF50, 0xFF2196F3, 0xFF9C27B0, 0xFFFF9800, 0xFFE91E63};

        for (int i = 0; i < 5; i++) {
            int rowY = panelY + 20 + i * 32;
            boolean hoverRow = mouseX >= mX + 16 && mouseX <= mX + 12 + colW - 4 && mouseY >= rowY && mouseY <= rowY + 28;
            graphics.fill(mX + 16, rowY, mX + 12 + colW - 4, rowY + 28, hoverRow ? 0xDD1B2538 : 0x88121A28);
            graphics.renderOutline(mX + 16, rowY, colW - 20, 28, gcColors[i]);

            ItemStack cardItem = switch (i) {
                case 1 -> SystemItems.GIFT_CARD_15.isBound() ? new ItemStack(SystemItems.GIFT_CARD_15.get()) : ItemStack.EMPTY;
                case 2 -> SystemItems.GIFT_CARD_30.isBound() ? new ItemStack(SystemItems.GIFT_CARD_30.get()) : ItemStack.EMPTY;
                case 3 -> SystemItems.GIFT_CARD_50.isBound() ? new ItemStack(SystemItems.GIFT_CARD_50.get()) : ItemStack.EMPTY;
                case 4 -> SystemItems.GIFT_CARD_100.isBound() ? new ItemStack(SystemItems.GIFT_CARD_100.get()) : ItemStack.EMPTY;
                default -> SystemItems.GIFT_CARD_5.isBound() ? new ItemStack(SystemItems.GIFT_CARD_5.get()) : ItemStack.EMPTY;
            };
            if (!cardItem.isEmpty()) {
                graphics.renderItem(cardItem, mX + 18, rowY + 6);
            }

            graphics.drawString(this.font, "§fTarjeta §e$" + gcVals[i] + " USD", mX + 38, rowY + 5, 0xFFFFFF, false);
            graphics.drawString(this.font, "§6" + String.format(java.util.Locale.US, "%,d", gcCosts[i]) + " 🪙", mX + 38, rowY + 15, 0xFFFFD54F, false);

            int btnX = mX + 12 + colW - 56;
            int btnY = rowY + 6;
            boolean canBuy = coins >= gcCosts[i];
            boolean hoverBtn = mouseX >= btnX && mouseX <= btnX + 48 && mouseY >= btnY && mouseY <= btnY + 16;
            graphics.fill(btnX, btnY, btnX + 48, btnY + 16, canBuy ? (hoverBtn ? 0xFF2E7D32 : 0xFF1B5E20) : 0xFF37474F);
            graphics.renderOutline(btnX, btnY, 48, 16, canBuy ? (hoverBtn ? 0xFF81C784 : 0xFF4CAF50) : 0xFF546E7A);
            graphics.drawCenteredString(this.font, "Comprar", btnX + 24, btnY + 4, canBuy ? 0xFFFFFF : 0x90A4AE);
        }

        // PANEL DERECHO: PAQUETES DE CRISTALES & BENDICIÓN
        int rightX = mX + 12 + colW + 8;
        int rightW = mW - (12 + colW + 8) - 12;
        graphics.fill(rightX, panelY, rightX + rightW, panelY + 185, 0xAA0D121D);
        graphics.renderOutline(rightX, panelY, rightW, 185, 0xFF334155);
        graphics.drawString(this.font, "§b§lCRISTALES GÉNESIS & JADE ESTELAR", rightX + 6, panelY + 6, 0xFF80D8FF, false);

        String[] packNames = {
                "60 Cristales", "330 Cristales", "1,090 Cristales", "2,240 Cristales",
                "3,880 Cristales", "8,080 Cristales", "🌙 Bendición Lunar"
        };
        String[] packPrices = {"$0.99", "$4.99", "$14.99", "$29.99", "$49.99", "$99.99", "$4.99"};
        double[] packUsd = {0.99, 4.99, 14.99, 29.99, 49.99, 99.99, 4.99};
        int[] packCoinCosts = {5000, 25000, 75000, 150000, 250000, 500000, 25000};

        for (int i = 0; i < 7; i++) {
            int rowY = panelY + 18 + i * 23;
            boolean hoverRow = mouseX >= rightX + 4 && mouseX <= rightX + rightW - 4 && mouseY >= rowY && mouseY <= rowY + 21;
            graphics.fill(rightX + 4, rowY, rightX + rightW - 4, rowY + 21, hoverRow ? 0xDD1B2538 : (i == 6 ? 0x664A3510 : 0x66121A28));
            graphics.renderOutline(rightX + 4, rowY, rightW - 8, 21, i == 6 ? 0xFFFFD700 : 0xFF2A3950);

            graphics.drawString(this.font, (i == 6 ? "§6" : "§b") + packNames[i], rightX + 8, rowY + 6, 0xFFFFFF, false);

            // Botón USD
            int btnUsdX = rightX + rightW - 98;
            int btnY = rowY + 3;
            boolean canUsd = usdBalance >= packUsd[i] - 0.001;
            boolean hoverUsd = mouseX >= btnUsdX && mouseX <= btnUsdX + 44 && mouseY >= btnY && mouseY <= btnY + 15;
            graphics.fill(btnUsdX, btnY, btnUsdX + 44, btnY + 15, canUsd ? (hoverUsd ? 0xFF0277BD : 0xFF01579B) : 0xFF263238);
            graphics.renderOutline(btnUsdX, btnY, 44, 15, canUsd ? 0xFF4FC3F7 : 0xFF37474F);
            renderCenteredFittedText(graphics, packPrices[i], btnUsdX + 22, btnY + 4, 40, canUsd ? 0xFFFFFF : 0x78909C);

            // Botón Monedas del Sistema
            int btnCoinX = btnUsdX + 47;
            boolean canCoin = coins >= packCoinCosts[i];
            boolean hoverCoin = mouseX >= btnCoinX && mouseX <= btnCoinX + 44 && mouseY >= btnY && mouseY <= btnY + 15;
            graphics.fill(btnCoinX, btnY, btnCoinX + 44, btnY + 15, canCoin ? (hoverCoin ? 0xFFE65100 : 0xFFBF360C) : 0xFF263238);
            graphics.renderOutline(btnCoinX, btnY, 44, 15, canCoin ? 0xFFFFB74D : 0xFF37474F);
            String coinLabel = (packCoinCosts[i] >= 1000) ? (packCoinCosts[i] / 1000 + "k 🪙") : (packCoinCosts[i] + "🪙");
            renderCenteredFittedText(graphics, coinLabel, btnCoinX + 22, btnY + 4, 40, canCoin ? 0xFFFFE082 : 0x78909C);
        }

        // PANEL INFERIOR: CONVERSIÓN 1:1 DIRECTA
        int convY = mY + mH - 38;
        int btnConvW = (mW - 34) / 2;

        // Botón 1: Cristales Génesis -> Protogemas
        int b1X = mX + 12;
        boolean canConv1 = genesisCrystals > 0;
        boolean hoverB1 = mouseX >= b1X && mouseX <= b1X + btnConvW && mouseY >= convY && mouseY <= convY + 24;
        graphics.fill(b1X, convY, b1X + btnConvW, convY + 24, canConv1 ? (hoverB1 ? 0xFF0D47A1 : 0xFF0A2B66) : 0xFF1C2230);
        graphics.renderOutline(b1X, convY, btnConvW, 24, canConv1 ? (hoverB1 ? 0xFF64B5F6 : 0xFF1976D2) : 0xFF37474F);
        renderCenteredFittedText(graphics, "💎 Convertir Cristales a Protogemas (1:1) [" + genesisCrystals + "]", b1X + btnConvW / 2, convY + 8, btnConvW - 10, canConv1 ? 0xFF80D8FF : 0x78909C);

        // Botón 2: Jade Estelar -> Protogemas
        int b2X = b1X + btnConvW + 10;
        boolean canConv2 = stellarJade > 0;
        boolean hoverB2 = mouseX >= b2X && mouseX <= b2X + btnConvW && mouseY >= convY && mouseY <= convY + 24;
        graphics.fill(b2X, convY, b2X + btnConvW, convY + 24, canConv2 ? (hoverB2 ? 0xFF4A148C : 0xFF2A0852) : 0xFF1C2230);
        graphics.renderOutline(b2X, convY, btnConvW, 24, canConv2 ? (hoverB2 ? 0xFFBA68C8 : 0xFF7B1FA2) : 0xFF37474F);
        renderCenteredFittedText(graphics, "💠 Convertir Jade Estelar a Protogemas (1:1) [" + stellarJade + "]", b2X + btnConvW / 2, convY + 8, btnConvW - 10, canConv2 ? 0xFFE1BEE7 : 0x78909C);
    }
"""

render_shop_anchor = "    private void renderShopModal(GuiGraphics graphics, int mouseX, int mouseY) {"
code = code.replace(render_shop_anchor, top_up_method + "\n" + render_shop_anchor, 1)

# 4. Update mouseClicked for top-up modal and subtabs
old_mouse_tabs = """        // 1. Clics en Sub-modos de Fila 1 (Personajes, Armas, Permanente, Principiantes)
        boolean hasNovice = beginnerPulls < 20;
        int numSubTabs = hasNovice ? 4 : 3;
        int subTabW = hasNovice ? 94 : 110;
        int subTabH = 17;
        int subModeStartX = 10 + 54 + 6;
        int subModeY = 6;

        for (int i = 0; i < numSubTabs; i++) {
            int sx = subModeStartX + i * (subTabW + 4);
            if (mouseX >= sx && mouseX <= sx + subTabW && mouseY >= subModeY && mouseY <= subModeY + subTabH) {
                if (i == 0) {
                    if (isPromotionalCharacterBanner()) {
                        // Si ya está en Personajes, hacer clic rota cíclicamente a la siguiente saga
                        cycleNextFranchise();
                    } else {
                        activeBanner = getBannerForFranchise(activeFranchise);
                        playFranchiseSwitchSound(activeFranchise);
                    }
                    return true;
                }
                int targetBanner = switch (i) {
                    case 1 -> 0;
                    case 2 -> 2;
                    case 3 -> 3;
                    default -> 1;
                };
                if (activeBanner != targetBanner) {
                    activeBanner = targetBanner;
                    stopCurrentVoiceSound();
                    if (targetBanner == 0) {
                        playVoiceSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 1.2F, 1.1F);
                    } else if (targetBanner == 2) {
                        playVoiceSound(ModSounds.MUSIC_DISC_DAY_OF_HOPE.get(), 1.0F, 1.0F);
                    } else if (targetBanner == 3) {
                        playVoiceSound(ModSounds.COMPANION_NOELLE_SUMMON.get(), 1.2F, 1.0F);
                    } else {
                        playFranchiseSwitchSound(activeFranchise);
                    }
                }
                return true;
            }
        }"""

new_mouse_tabs = """        // 1. Clics en Sub-modos de Fila 1 (Promocional, Armas, Permanente, Principiantes, Tienda USD)
        boolean hasNovice = beginnerPulls < 20;
        int numSubTabs = hasNovice ? 5 : 4;
        int subTabW = hasNovice ? 80 : 96;
        int subTabH = 17;
        int subModeStartX = 10 + 54 + 6;
        int subModeY = 6;

        for (int i = 0; i < numSubTabs; i++) {
            int sx = subModeStartX + i * (subTabW + 4);
            if (mouseX >= sx && mouseX <= sx + subTabW && mouseY >= subModeY && mouseY <= subModeY + subTabH) {
                if ((hasNovice && i == 4) || (!hasNovice && i == 3)) {
                    showTopUpModal = true;
                    showShopModal = false;
                    showHistoryModal = false;
                    showPathModal = false;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                    return true;
                }
                showTopUpModal = false;
                if (i == 0) {
                    if (isPromotionalCharacterBanner()) {
                        cycleNextFranchise();
                    } else {
                        activeBanner = getBannerForFranchise(activeFranchise);
                        playFranchiseSwitchSound(activeFranchise);
                    }
                    return true;
                }
                int targetBanner = switch (i) {
                    case 1 -> 0;
                    case 2 -> 2;
                    case 3 -> 3;
                    default -> 1;
                };
                if (activeBanner != targetBanner) {
                    activeBanner = targetBanner;
                    stopCurrentVoiceSound();
                    if (targetBanner == 0) {
                        playVoiceSound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 1.2F, 1.1F);
                    } else if (targetBanner == 2) {
                        playVoiceSound(ModSounds.MUSIC_DISC_DAY_OF_HOPE.get(), 1.0F, 1.0F);
                    } else if (targetBanner == 3) {
                        playVoiceSound(ModSounds.COMPANION_NOELLE_SUMMON.get(), 1.2F, 1.0F);
                    } else {
                        playFranchiseSwitchSound(activeFranchise);
                    }
                }
                return true;
            }
        }"""

code = code.replace(old_mouse_tabs, new_mouse_tabs, 1)

# 5. Add click handling for showTopUpModal inside mouseClicked
top_up_click_logic = """        // Si el modal de recargas está abierto
        if (showTopUpModal) {
            int mW = 520;
            int mH = 270;
            int mX = this.width / 2 - mW / 2;
            int mY = this.height / 2 - mH / 2;

            int closeX = mX + mW - 22;
            int closeY = mY + 10;
            boolean clickedClose = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
            boolean clickedOutside = mouseX < mX || mouseX > mX + mW || mouseY < mY || mouseY > mY + mH;

            if (clickedClose || clickedOutside) {
                showTopUpModal = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.9F));
                return true;
            }

            int colW = 240;
            int panelY = mY + 38;

            // Clics en Gift Cards (Panel Izquierdo)
            int[] gcCosts = {25000, 75000, 150000, 250000, 500000};
            for (int i = 0; i < 5; i++) {
                int rowY = panelY + 20 + i * 32;
                int btnX = mX + 12 + colW - 56;
                int btnY = rowY + 6;
                if (mouseX >= btnX && mouseX <= btnX + 48 && mouseY >= btnY && mouseY <= btnY + 16) {
                    if (coins >= gcCosts[i]) {
                        PacketDistributor.sendToServer(new GachaTopUpPayload(3, i));
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    }
                    return true;
                }
            }

            // Clics en Paquetes de Cristales (Panel Derecho)
            int rightX = mX + 12 + colW + 8;
            int rightW = mW - (12 + colW + 8) - 12;
            double[] packUsd = {0.99, 4.99, 14.99, 29.99, 49.99, 99.99, 4.99};
            int[] packCoinCosts = {5000, 25000, 75000, 150000, 250000, 500000, 25000};

            for (int i = 0; i < 7; i++) {
                int rowY = panelY + 18 + i * 23;
                int btnUsdX = rightX + rightW - 98;
                int btnY = rowY + 3;
                int btnCoinX = btnUsdX + 47;

                // Comprar con USD
                if (mouseX >= btnUsdX && mouseX <= btnUsdX + 44 && mouseY >= btnY && mouseY <= btnY + 15) {
                    if (usdBalance >= packUsd[i] - 0.001) {
                        PacketDistributor.sendToServer(new GachaTopUpPayload(0, i));
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    }
                    return true;
                }

                // Comprar con Monedas del Sistema
                if (mouseX >= btnCoinX && mouseX <= btnCoinX + 44 && mouseY >= btnY && mouseY <= btnY + 15) {
                    if (coins >= packCoinCosts[i]) {
                        PacketDistributor.sendToServer(new GachaTopUpPayload(4, i));
                    } else {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    }
                    return true;
                }
            }

            // Clics en Conversión 1:1
            int convY = mY + mH - 38;
            int btnConvW = (mW - 34) / 2;
            int b1X = mX + 12;
            int b2X = b1X + btnConvW + 10;

            if (mouseX >= b1X && mouseX <= b1X + btnConvW && mouseY >= convY && mouseY <= convY + 24) {
                if (genesisCrystals > 0) {
                    PacketDistributor.sendToServer(new GachaTopUpPayload(1, 0));
                } else {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                }
                return true;
            }

            if (mouseX >= b2X && mouseX <= b2X + btnConvW && mouseY >= convY && mouseY <= convY + 24) {
                if (stellarJade > 0) {
                    PacketDistributor.sendToServer(new GachaTopUpPayload(2, 0));
                } else {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                }
                return true;
            }

            return true;
        }
"""

modal_click_anchor = "        // Si el modal de gangas de Paimon está abierto\n        if (showShopModal) {"
code = code.replace(modal_click_anchor, top_up_click_logic + "\n" + modal_click_anchor, 1)

# Also update keyPressed or close modals on escape
code = code.replace("if (showHistoryModal || showShopModal || showPathModal) {", "if (showHistoryModal || showShopModal || showPathModal || showTopUpModal) {")
code = code.replace("showShopModal = false;", "showShopModal = false;\n            showTopUpModal = false;")

with open(path, "w", encoding="utf-8") as f:
    f.write(code)

print("Updated GachaWishScreen.java with Top-Up shop modal and tabs successfully!")
