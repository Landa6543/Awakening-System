package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SystemScreen extends Screen {
    private static final int W = 440;
    private static final int H = 310;
    private static final int CYAN = 0xFF70E8FF;
    private static final int MUTED = 0xFF9BB7C7;
    private static final int PANEL = 0xE516293B;
    private static final int EDGE = 0xFF2D7691;
    private static final int SECTION = 0xFF17354B;
    private static final String[] TABS = {"status", "skills", "missions", "titles", "shop", "progress", "ranking", "artifacts"};
    private static final String[] ATTRIBUTE_IDS = {
            "strength", "vitality", "agility", "perception", "intelligence", "willpower"
    };
    private static final String[] SHOP_CATEGORIES = {"all", "boxes", "gear", "consumables", "materials"};
    private static final String[] SELL_CATEGORIES = {"all", "ores", "drops", "farming", "blocks"};

    private int tab;
    private int skillScroll;
    private int skillTabMode = 0; // 0 = Activas, 1 = Pasivas
    private int activeSkillScroll = 0;
    private int passiveSkillScroll = 0;
    private int selectedActiveSlot = 0;
    private static final String[] ACTIVE_SLOT_KEYS = {"V", "R", "G", "B", "X"};
    private int titleScroll;
    private int shopScroll;
    private int progressScroll;
    private int rankingScroll;
    private int missionScroll;
    private int sellScroll;
    private int shopMode = 0; // 0 = Comprar, 1 = Vender, 2 = Ruleta del Sistema, 3 = Banco
    private String shopCategory = "all";
    private String sellCategory = "all";
    private boolean isDraggingScrollbar = false;
    private int draggingScrollbarTab = -1;
    private net.minecraft.client.gui.components.EditBox shopSearchBox;

    public SystemScreen() { this(0); }

    public SystemScreen(int tab) {
        super(Component.translatable("awakening_system.screen.title"));
        this.tab = tab;
    }

    @Override
    protected void init() {
        super.init();
        if (data().isAnySystemUpdating()) {
            this.minecraft.setScreen(new SystemMaintenanceScreen());
            return;
        }
        int left = getGuiLeft();
        int top = getGuiTop();
        String prevQuery = this.shopSearchBox != null ? this.shopSearchBox.getValue() : "";
        int searchW = 92;
        int searchH = 16;
        int searchX = left + 412 - searchW;
        int searchY = top + 100;
        this.shopSearchBox = new net.minecraft.client.gui.components.EditBox(
                this.font,
                searchX + 3,
                searchY + 1,
                searchW - 6,
                searchH - 2,
                Component.translatable("awakening_system.screen.shop.search")
        );
        this.shopSearchBox.setHint(Component.literal("§8🔍 Buscar..."));
        this.shopSearchBox.setMaxLength(32);
        this.shopSearchBox.setTextColor(0xFFFFFFFF);
        this.shopSearchBox.setBordered(false);
        if (!prevQuery.isEmpty()) {
            this.shopSearchBox.setValue(prevQuery);
        }
        this.shopSearchBox.setVisible(tab == 4 && (shopMode == 0 || shopMode == 1));
        this.shopSearchBox.setResponder(text -> {
            shopScroll = 0;
            sellScroll = 0;
        });
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_OPEN.value(), 1.0F, 1.0F);
        AwakeningSystemClient.requestLeaderboard();
    }

    private void updateSearchBoxVisibility() {
        if (shopSearchBox != null) {
            boolean visible = tab == 4 && (shopMode == 0 || shopMode == 1);
            shopSearchBox.setVisible(visible);
            if (!visible) {
                shopSearchBox.setFocused(false);
            }
        }
    }

    @Override
    public void onClose() {
        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLOSE.value(), 0.9F, 1.0F);
        super.onClose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.enableBackgroundBlur) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private boolean isDraggingWindow = false;
    private double dragStartMouseX;
    private double dragStartMouseY;
    private int dragStartOffsetX;
    private int dragStartOffsetY;

    private boolean isResizingWindow = false;
    private double resizeStartMouseX;
    private double resizeStartMouseY;
    private float resizeStartScale;

    public int getGuiLeft() {
        return (this.width - W) / 2 + com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiOffsetX;
    }

    public int getGuiTop() {
        return (this.height - H) / 2 + com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiOffsetY;
    }

    public double getGuiCenterX() {
        return getGuiLeft() + W / 2.0;
    }

    public double getGuiCenterY() {
        return getGuiTop() + H / 2.0;
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 510.0F;
        float scaleY = (float) this.height / 330.0F;
        float autoScale = Math.min(1.0F, Math.max(0.35F, Math.min(scaleX, scaleY)));
        return Math.max(0.4F, Math.min(2.0F, autoScale * com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiScale));
    }

    public double transformMouseX(double mouseX) {
        float scale = getGuiScale();
        if (Math.abs(scale - 1.0F) < 0.001F) return mouseX;
        double cx = getGuiCenterX();
        return (mouseX - cx) / scale + cx;
    }

    public double transformMouseY(double mouseY) {
        float scale = getGuiScale();
        if (Math.abs(scale - 1.0F) < 0.001F) return mouseY;
        double cy = getGuiCenterY();
        return (mouseY - cy) / scale + cy;
    }

    private void enableScissorScaled(GuiGraphics g, int minX, int minY, int maxX, int maxY) {
        float scale = getGuiScale();
        if (Math.abs(scale - 1.0F) < 0.001F) {
            g.enableScissor(minX, minY, maxX, maxY);
        } else {
            double cx = getGuiCenterX();
            double cy = getGuiCenterY();
            int sMinX = (int) Math.floor(cx + (minX - cx) * scale);
            int sMinY = (int) Math.floor(cy + (minY - cy) * scale);
            int sMaxX = (int) Math.ceil(cx + (maxX - cx) * scale);
            int sMaxY = (int) Math.ceil(cy + (maxY - cy) * scale);
            sMinX = Math.max(0, sMinX);
            sMinY = Math.max(0, sMinY);
            sMaxX = Math.min(this.width, Math.max(sMinX, sMaxX));
            sMaxY = Math.min(this.height, Math.max(sMinY, sMaxY));
            g.enableScissor(sMinX, sMinY, sMaxX, sMaxY);
        }
    }

    private void updateSearchBoxPosition() {
        if (this.shopSearchBox != null) {
            int left = getGuiLeft();
            int top = getGuiTop();
            int searchW = 92;
            int searchX = left + 412 - searchW;
            int searchY = top + 100;
            this.shopSearchBox.setX(searchX + 3);
            this.shopSearchBox.setY(searchY + 1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double rawX = mouseX;
        double rawY = mouseY;
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int left = getGuiLeft();
        int top = getGuiTop();

        // 1. Botón Configuración de Interfaz (Engranaje)
        if (mouseX >= left + W - 34 && mouseX < left + W - 18 && mouseY >= top + 16 && mouseY < top + 32) {
            com.soybe.awakeningsystem.client.gui.GuiConfigScreen.open(this);
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
            return true;
        }

        // 2. Botón Centrar Interfaz
        if (mouseX >= left + W - 52 && mouseX < left + W - 36 && mouseY >= top + 16 && mouseY < top + 32) {
            com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiOffsetX = 0;
            com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiOffsetY = 0;
            com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiScale = 1.0F;
            com.soybe.awakeningsystem.client.gui.GuiConfig.save();
            updateSearchBoxPosition();
            AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 0.7F, 1.4F);
            return true;
        }

        // 3. Botón Gremio de Cazadores (Cabecera)
        if (mouseX >= left + W - 70 && mouseX < left + W - 54 && mouseY >= top + 16 && mouseY < top + 32) {
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
            AwakeningSystemClient.requestOpenGuild();
            return true;
        }

        // 3. Agarrar esquina inferior derecha para redimensionar (Resize Grip)
        if (mouseX >= left + W - 20 && mouseX <= left + W && mouseY >= top + H - 20 && mouseY <= top + H) {
            isResizingWindow = true;
            resizeStartMouseX = rawX;
            resizeStartMouseY = rawY;
            resizeStartScale = com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiScale;
            return true;
        }

        // 4. Arrastrar cabecera para mover libremente la ventana con el ratón
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.freeMoveEnabled
                && mouseX >= left + 14 && mouseX < left + W - 60 && mouseY >= top + 12 && mouseY < top + 44) {
            isDraggingWindow = true;
            dragStartMouseX = rawX;
            dragStartMouseY = rawY;
            dragStartOffsetX = com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiOffsetX;
            dragStartOffsetY = com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiOffsetY;
            return true;
        }

        // Clic en pestañas dimensionales del lateral izquierdo
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showDimensionSideTabs) {
            int tx = left - 32;
            List<DimTab> sideTabs = getActiveSideTabs();
            for (int i = 0; i < sideTabs.size(); i++) {
                int tabY = top + 55 + i * 33;
                if (mouseX >= tx && mouseX < tx + 30 && mouseY >= tabY && mouseY < tabY + 28) {
                    DimTab t = sideTabs.get(i);
                    this.onClose();
                    if ("subspace".equals(t.id)) {
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new OpenSubspaceC2SPayload());
                    } else {
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.9F, 1.0F);
                        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new TravelDimensionPayload(t.id));
                    }
                    return true;
                }
            }
        }

        // Clic en pestañas superiores
        for (int i = 0; i < TABS.length; i++) {
            int tabX = tabX(left, i);
            if (mouseX >= tabX && mouseX < tabX + tabWidth() && mouseY >= top + 51 && mouseY < top + 71) {
                tab = i;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.95F, 1.0F);
                if (tab == 6) AwakeningSystemClient.requestLeaderboard();
                updateSearchBoxVisibility();
                return true;
            }
        }

        // Pestaña 0: Estado
        if (tab == 0) {
            for (int i = 0; i < ATTRIBUTE_IDS.length; i++) {
                int col = i / 3;
                int row = i % 3;
                int plusX = left + (col == 0 ? 88 : 174);
                int plusY = top + 178 + row * 26;
                if (mouseX >= plusX && mouseX < plusX + 16 && mouseY >= plusY && mouseY < plusY + 16) {
                    if (data().attributePoints() > 0) {
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_STAT_UP.value(), 1.0F, 1.0F);
                    } else {
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_ERROR.value(), 0.9F, 1.0F);
                    }
                    AwakeningSystemClient.spend(ATTRIBUTE_IDS[i]);
                    return true;
                }
            }
            if ((data().hasHunterClass() || data().level() >= 30) && mouseX >= left + 20 && mouseX < left + 200 && mouseY >= top + 95 && mouseY < top + 111) {
                if (data().hasHunterClass()) {
                    Minecraft.getInstance().setScreen(new ClassSkillTreeScreen(this));
                    return true;
                }
                AwakeningSystemClient.openClassSelectionScreen();
                return true;
            }
            if (data().level() >= 100 && mouseX >= left + 218 && mouseX < left + 415 && mouseY >= top + 95 && mouseY < top + 111) {
                AwakeningSystemClient.openSponsorScreen(false);
                return true;
            }
            // Botón interactivo de Visión Elemental (ubicado limpiamente en top + 178 sin colisionar con el título)
            if (mouseX >= left + 320 && mouseX < left + 394 && mouseY >= top + 178 && mouseY < top + 194) {
                com.soybe.awakeningsystem.client.gui.VisionSelectionScreen.open(this);
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                return true;
            }
            if (data().isConstellation()) {
                if (mouseX >= left + 20 && mouseX < left + 148 && mouseY >= top + 266 && mouseY < top + 292) {
                    AwakeningSystemClient.openConstellationChannelScreen();
                    return true;
                }
                if (mouseX >= left + 154 && mouseX < left + 282 && mouseY >= top + 266 && mouseY < top + 292) {
                    Minecraft.getInstance().setScreen(new com.soybe.awakeningsystem.fable.FablesScreen(this));
                    return true;
                }
                if (mouseX >= left + 288 && mouseX < left + 420 && mouseY >= top + 266 && mouseY < top + 292) {
                    Minecraft.getInstance().setScreen(new CultivationScreen(this));
                    return true;
                }
            } else {
                if (mouseX >= left + 20 && mouseX < left + 148 && mouseY >= top + 266 && mouseY < top + 292) {
                    Minecraft.getInstance().setScreen(new com.soybe.awakeningsystem.fable.FablesScreen(this));
                    return true;
                }
                if (mouseX >= left + 154 && mouseX < left + 282 && mouseY >= top + 266 && mouseY < top + 292) {
                    Minecraft.getInstance().setScreen(new CultivationScreen(this));
                    return true;
                }
                if (mouseX >= left + 288 && mouseX < left + 420 && mouseY >= top + 266 && mouseY < top + 292) {
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                    AwakeningSystemClient.requestOpenGuild();
                    return true;
                }
            }
        }

        // Pestaña 1: Habilidades (5 Ranuras Activas: V, R, G, B, X y 3 Talentos Pasivos + Árbol de Clase)
        if (tab == 1) {
            boolean hasClass = data().hasHunterClass();
            int subBtnW = hasClass ? ((W - 48) / 3) : ((W - 44) / 2);
            if (mouseX >= left + 20 && mouseX < left + 20 + subBtnW && mouseY >= top + 78 && mouseY < top + 98) {
                skillTabMode = 0;
                return true;
            }
            if (mouseX >= left + 24 + subBtnW && mouseX < left + 24 + subBtnW * 2 && mouseY >= top + 78 && mouseY < top + 98) {
                skillTabMode = 1;
                return true;
            }
            if (hasClass && mouseX >= left + 28 + subBtnW * 2 && mouseX < left + 28 + subBtnW * 3 && mouseY >= top + 78 && mouseY < top + 98) {
                Minecraft.getInstance().setScreen(new ClassSkillTreeScreen(this));
                return true;
            }

            SystemData d = data();

            if (skillTabMode == 0) {
                int unlockedActive = d.getUnlockedActiveSkillSlots();
                if (selectedActiveSlot >= unlockedActive) {
                    selectedActiveSlot = 0;
                }
                // Clic en las 5 ranuras activas superiores [V, R, G, B, X]
                for (int s = 0; s < 5; s++) {
                    int slotX = left + 20 + s * 81;
                    int slotY = top + 102;
                    int slotW = 76;
                    int slotH = 36;
                    if (mouseX >= slotX && mouseX < slotX + slotW && mouseY >= slotY && mouseY < slotY + slotH) {
                        if (s >= unlockedActive) {
                            return true;
                        }
                        String eq = d.equippedActiveSkill(s);
                        if (!eq.isEmpty() && mouseX >= slotX + slotW - 16 && mouseY <= slotY + 16) {
                            // Desequipar al presionar 'x'
                            AwakeningSystemClient.equipSkillSlot(false, s, "");
                            return true;
                        }
                        selectedActiveSlot = s;
                        return true;
                    }
                }

                // Clic en la lista de habilidades activas
                List<Map.Entry<String, Integer>> actives = visibleActiveSkills();
                for (int i = 0; i < actives.size(); i++) {
                    int itemY = top + 144 + (i - activeSkillScroll) * 38;
                    if (itemY < top + 140 || itemY >= top + 264) continue;
                    Map.Entry<String, Integer> entry = actives.get(i);

                    int eqSlot = -1;
                    for (int s = 0; s < unlockedActive; s++) {
                        if (entry.getKey().equals(d.equippedActiveSkill(s))) {
                            eqSlot = s;
                            break;
                        }
                    }

                    // Botón Equipar / Desequipar Ranura Activa
                    if (mouseX >= left + W - 150 && mouseX < left + W - 65 && mouseY >= itemY + 8 && mouseY < itemY + 26) {
                        if (eqSlot >= 0) {
                            AwakeningSystemClient.equipSkillSlot(false, eqSlot, "");
                        } else {
                            if (selectedActiveSlot >= unlockedActive) {
                                selectedActiveSlot = 0;
                            }
                            AwakeningSystemClient.equipSkillSlot(false, selectedActiveSlot, entry.getKey());
                            selectedActiveSlot = (selectedActiveSlot + 1) % Math.max(1, unlockedActive);
                        }
                        return true;
                    }

                    // Botón Evolucionar si es nivel 5
                    if (entry.getValue() == 5 && mouseX >= left + W - 60 && mouseX < left + W - 15 && mouseY >= itemY + 8 && mouseY < itemY + 26) {
                        AwakeningSystemClient.evolve(entry.getKey());
                        return true;
                    }

                    // Clic en la fila abre la pantalla detallada
                    if (mouseX >= left + 20 && mouseX < left + W - 20 && mouseY >= itemY && mouseY < itemY + 34) {
                        Minecraft.getInstance().setScreen(new SkillDetailScreen(entry.getKey(), entry.getValue()));
                        return true;
                    }
                }
            } else {
                int unlockedPassives = d.getUnlockedPassiveSkillSlots();
                // Clic en las 3 ranuras de talento pasivo superiores
                for (int s = 0; s < 3; s++) {
                    int slotX = left + 20 + s * 136;
                    int slotY = top + 102;
                    int slotW = 128;
                    int slotH = 32;
                    if (mouseX >= slotX && mouseX < slotX + slotW && mouseY >= slotY && mouseY < slotY + slotH) {
                        if (s >= unlockedPassives) {
                            return true;
                        }
                        String eq = d.equippedPassiveSkill(s);
                        if (!eq.isEmpty()) {
                            AwakeningSystemClient.equipSkillSlot(true, s, "");
                            return true;
                        }
                    }
                }

                // Clic en la lista de habilidades pasivas
                List<Map.Entry<String, Integer>> passives = visiblePassiveSkills();
                for (int i = 0; i < passives.size(); i++) {
                    int itemY = top + 138 + (i - passiveSkillScroll) * 38;
                    if (itemY < top + 134 || itemY >= top + 264) continue;
                    Map.Entry<String, Integer> entry = passives.get(i);

                    int eqSlot = -1;
                    for (int s = 0; s < unlockedPassives; s++) {
                        if (entry.getKey().equals(d.equippedPassiveSkill(s))) {
                            eqSlot = s;
                            break;
                        }
                    }

                    // Botón Equipar / Desequipar Talento Pasivo
                    int toggleX = entry.getValue() == 5 ? left + W - 165 : left + W - 90;
                    int toggleW = 70;
                    if (mouseX >= toggleX && mouseX < toggleX + toggleW && mouseY >= itemY + 8 && mouseY < itemY + 26) {
                        if (eqSlot >= 0) {
                            AwakeningSystemClient.equipSkillSlot(true, eqSlot, "");
                        } else {
                            int freeSlot = 0;
                            for (int s = 0; s < unlockedPassives; s++) {
                                if (d.equippedPassiveSkill(s).isEmpty()) {
                                    freeSlot = s;
                                    break;
                                }
                            }
                            AwakeningSystemClient.equipSkillSlot(true, freeSlot, entry.getKey());
                        }
                        return true;
                    }

                    // Botón Evolucionar si es nivel 5
                    if (entry.getValue() == 5 && mouseX >= left + W - 90 && mouseX < left + W - 15 && mouseY >= itemY + 8 && mouseY < itemY + 26) {
                        AwakeningSystemClient.evolve(entry.getKey());
                        return true;
                    }

                    // Clic en la fila abre la pantalla detallada
                    if (mouseX >= left + 20 && mouseX < left + W - 20 && mouseY >= itemY && mouseY < itemY + 34) {
                        Minecraft.getInstance().setScreen(new SkillDetailScreen(entry.getKey(), entry.getValue()));
                        return true;
                    }
                }
            }
        }

        // Pestaña 2: Misiones (Reclamar Escenario)
        if (tab == 2) {
            SystemData d = data();
            if (d.isScenarioComplete() && mouseX >= left + 28 + W - 56 - 108 && mouseX < left + 28 + W - 56 - 6 && mouseY >= top + 129 && mouseY < top + 147) {
                AwakeningSystemClient.claimScenario();
                return true;
            }
        }

        // Pestaña 3: Títulos
        if (tab == 3) {
            int fabBtnX = left + W - 150;
            int fabBtnY = top + 96;
            if (mouseX >= fabBtnX && mouseX < fabBtnX + 122 && mouseY >= fabBtnY && mouseY < fabBtnY + 18) {
                Minecraft.getInstance().setScreen(new com.soybe.awakeningsystem.fable.FablesScreen(this));
                return true;
            }
            List<TitleDefinitions.Definition> titles = TitleDefinitions.availableDefinitions(data());
            int row = 0;
            for (TitleDefinitions.Definition title : titles) {
                int titleY = top + 124 + (row++ - titleScroll) * 44;
                if (titleY < top + 120 || titleY >= top + 256) continue;
                if (mouseX >= left + 30 && mouseX < left + W - 34 && mouseY >= titleY && mouseY < titleY + 40) {
                    AwakeningSystemClient.equipTitle(title.id());
                    return true;
                }
            }
        }

        // Pestaña 4: Tienda (Modos Comprar / Vender / Ruleta, filtros de categoría y acciones)
        if (tab == 4) {
            if ((shopMode == 0 || shopMode == 1) && shopSearchBox != null) {
                int sX = left + 412 - 92;
                int sY = top + 100;
                int sW = 92;
                int sH = 16;
                if (mouseX >= sX && mouseX <= sX + sW && mouseY >= sY && mouseY <= sY + sH) {
                    shopSearchBox.setFocused(true);
                    return shopSearchBox.mouseClicked(mouseX, mouseY, button);
                } else {
                    shopSearchBox.setFocused(false);
                }
            }

            // Clic en la barra de scroll lateral de la Tienda (Comprar / Vender)
            int trackTop = top + 120;
            int trackBottom = top + 258;
            if (mouseX >= left + W - 32 && mouseX <= left + W - 4 && mouseY >= trackTop && mouseY <= trackBottom) {
                if (shopMode == 0) {
                    List<ShopDefinitions.Entry> entries = availableShopEntries();
                    int maxScroll = Math.max(0, entries.size() - 4);
                    if (maxScroll > 0) {
                        int thumbHeight = Math.max(14, (trackBottom - trackTop) * 4 / entries.size());
                        int thumbRange = Math.max(1, trackBottom - trackTop - thumbHeight);
                        int currentThumbY = trackTop + (int) Math.round((double) thumbRange * shopScroll / maxScroll);

                        if (mouseY < currentThumbY) {
                            shopScroll = Math.max(0, shopScroll - 1);
                        } else if (mouseY > currentThumbY + thumbHeight) {
                            shopScroll = Math.min(maxScroll, shopScroll + 1);
                        } else {
                            float progress = (float)(mouseY - trackTop - thumbHeight / 2.0) / (float)thumbRange;
                            shopScroll = Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * maxScroll);
                        }
                        isDraggingScrollbar = true;
                        draggingScrollbarTab = 4;
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        return true;
                    }
                } else if (shopMode == 1) {
                    List<SellDefinitions.SellEntry> entries = availableSellEntries();
                    int maxScroll = Math.max(0, entries.size() - 4);
                    if (maxScroll > 0) {
                        int thumbHeight = Math.max(14, (trackBottom - trackTop) * 4 / entries.size());
                        int thumbRange = Math.max(1, trackBottom - trackTop - thumbHeight);
                        int currentThumbY = trackTop + (int) Math.round((double) thumbRange * sellScroll / maxScroll);

                        if (mouseY < currentThumbY) {
                            sellScroll = Math.max(0, sellScroll - 1);
                        } else if (mouseY > currentThumbY + thumbHeight) {
                            sellScroll = Math.min(maxScroll, sellScroll + 1);
                        } else {
                            float progress = (float)(mouseY - trackTop - thumbHeight / 2.0) / (float)thumbRange;
                            sellScroll = Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * maxScroll);
                        }
                        isDraggingScrollbar = true;
                        draggingScrollbarTab = 4;
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        return true;
                    }
                }
            }

            // Alternar modo Comprar / Vender / Ruleta / Banco / Deseos
            if (mouseX >= left + 62 && mouseX < left + 108 && mouseY >= top + 80 && mouseY < top + 96) {
                shopMode = 0;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                updateSearchBoxVisibility();
                return true;
            }
            if (mouseX >= left + 111 && mouseX < left + 151 && mouseY >= top + 80 && mouseY < top + 96) {
                shopMode = 1;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                updateSearchBoxVisibility();
                return true;
            }
            if (mouseX >= left + 154 && mouseX < left + 202 && mouseY >= top + 80 && mouseY < top + 96) {
                shopMode = 2;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                updateSearchBoxVisibility();
                return true;
            }
            if (mouseX >= left + 205 && mouseX < left + 249 && mouseY >= top + 80 && mouseY < top + 96) {
                shopMode = 3;
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                updateSearchBoxVisibility();
                return true;
            }
            if (isSys40() && mouseX >= left + 252 && mouseX < left + 302 && mouseY >= top + 80 && mouseY < top + 96) {
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                SystemData d = data();
                java.util.List<String> hist = new java.util.ArrayList<>();
                for (SystemData.WishLogEntry e : d.wishHistory()) {
                    hist.add(e.itemName() + "::" + e.stars() + "::" + e.bannerName() + "::" + e.timestamp());
                }
                Minecraft.getInstance().setScreen(new GachaWishScreen(
                        d.primogems(),
                        d.intertwinedFates(),
                        d.acquaintFates(),
                        d.stardust(),
                        d.starglitter(),
                        d.gachaPity5Star(),
                        d.gachaPity4Star(),
                        d.gachaGuaranteedRateUp(),
                        d.beginnerPulls(),
                        d.mora(),
                        d.weaponBannerTarget(),
                        d.weaponBannerFatePoints(),
                        0,
                        d.coins(),
                        d.usdBalance(),
                        d.genesisCrystals(),
                        d.stellarJade(),
                        d.welkinDays(),
                        hist
                ));
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                return true;
            }

            if (shopMode == 3) {
                int panelX = left + 30;
                int panelY = top + 102;
                int panelW = W - 60;
                int colW = (panelW - 30) / 2;
                int col1X = panelX + 10;
                int col2X = panelX + 20 + colW;
                int b1Y = panelY + 66;
                int b2Y = panelY + 92;
                int btnH = 20;

                // Depositar Todas
                if (mouseX >= col1X && mouseX < col1X + colW && mouseY >= b1Y && mouseY < b1Y + btnH) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new BankTransactionPayload("deposit", 0));
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(ModSounds.SYSTEM_COINS.value(), 1.0F, 1.0F);
                    }
                    return true;
                }
                // Depositar 64
                if (mouseX >= col1X && mouseX < col1X + colW / 2 - 2 && mouseY >= b2Y && mouseY < b2Y + btnH) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new BankTransactionPayload("deposit", 64));
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(ModSounds.SYSTEM_COINS.value(), 1.0F, 1.0F);
                    }
                    return true;
                }
                // Depositar 10
                if (mouseX >= col1X + colW / 2 + 2 && mouseX < col1X + colW && mouseY >= b2Y && mouseY < b2Y + btnH) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new BankTransactionPayload("deposit", 10));
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(ModSounds.SYSTEM_COINS.value(), 1.0F, 1.0F);
                    }
                    return true;
                }
                // Retirar 64
                if (mouseX >= col2X && mouseX < col2X + colW && mouseY >= b1Y && mouseY < b1Y + btnH) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new BankTransactionPayload("withdraw", 64));
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(ModSounds.SYSTEM_COINS.value(), 1.0F, 1.0F);
                    }
                    return true;
                }
                // Retirar 10
                if (mouseX >= col2X && mouseX < col2X + colW / 2 - 2 && mouseY >= b2Y && mouseY < b2Y + btnH) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new BankTransactionPayload("withdraw", 10));
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(ModSounds.SYSTEM_COINS.value(), 1.0F, 1.0F);
                    }
                    return true;
                }
                // Retirar 1
                if (mouseX >= col2X + colW / 2 + 2 && mouseX < col2X + colW && mouseY >= b2Y && mouseY < b2Y + btnH) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new BankTransactionPayload("withdraw", 1));
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(ModSounds.SYSTEM_COINS.value(), 1.0F, 1.0F);
                    }
                    return true;
                }
                return true;
            }

            if (shopMode == 2) {
                // Clic en el botón GIRAR RULETA
                int btnW = 240;
                int btnH = 24;
                int btnX = left + W / 2 - btnW / 2;
                int btnY = top + 102 + 106;
                if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                    SystemData d = data();
                    if (d.coins() < 1000) {
                        if (minecraft != null && minecraft.player != null) {
                            minecraft.player.playSound(ModSounds.SYSTEM_ERROR.value(), 0.9F, 1.0F);
                        }
                        AwakeningSystemClient.showNotification("awakening_system.notification.shop.error_title",
                                "awakening_system.notification.shop.insufficient", "", 0xFFFF7666);
                        return true;
                    }
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(ModSounds.ROULETTE_SPIN.value(), 1.0F, 1.0F);
                    }
                    AwakeningSystemClient.buyShopItem("system_roulette");
                    Minecraft.getInstance().setScreen(new ChestOpeningScreen(this, "system_roulette", "awakening_system.screen.roulette.title", 5, 1000));
                    return true;
                }
                return true;
            }

            if (shopMode == 0) {
                int catX = left + 30;
                int catY = top + 100;
                int[] widths = {34, 48, 40, 68, 62};
                for (int c = 0; c < SHOP_CATEGORIES.length; c++) {
                    int btnW = widths[c];
                    if (mouseX >= catX && mouseX < catX + btnW && mouseY >= catY && mouseY < catY + 16) {
                        shopCategory = SHOP_CATEGORIES[c];
                        shopScroll = 0;
                        if (minecraft != null && minecraft.player != null) {
                            minecraft.player.playSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                        }
                        return true;
                    }
                    catX += btnW + 3;
                }

                List<ShopDefinitions.Entry> entries = availableShopEntries();
                for (int row = 0; row < entries.size(); row++) {
                    int entryY = top + 122 + (row - shopScroll) * 32;
                    if (entryY < top + 118 || entryY >= top + 258) continue;
                    if (mouseX >= left + 30 && mouseX < left + W - 30 && mouseY >= entryY && mouseY < entryY + 28) {
                        ShopDefinitions.Entry entry = entries.get(row);
                        SystemData d = data();
                        if (!ShopDefinitions.isUnlocked(entry, d)) {
                            if (minecraft != null && minecraft.player != null) {
                                minecraft.player.playSound(ModSounds.SYSTEM_ERROR.value(), 0.9F, 1.0F);
                            }
                            AwakeningSystemClient.showNotification("awakening_system.notification.shop.error_title",
                                    "awakening_system.notification.shop.dimension_locked", "", 0xFFFF7666);
                            return true;
                        }
                        boolean isDirectOpenBox = "box_common".equals(entry.id()) || "box_rare".equals(entry.id())
                                || "box_epic".equals(entry.id()) || "box_legendary".equals(entry.id()) || "lucky_box".equals(entry.id());
                        if (isDirectOpenBox) {
                            if (d.coins() < entry.cost()) {
                                if (minecraft != null && minecraft.player != null) {
                                    minecraft.player.playSound(ModSounds.SYSTEM_ERROR.value(), 0.9F, 1.0F);
                                }
                                AwakeningSystemClient.showNotification("awakening_system.notification.shop.error_title",
                                        "awakening_system.notification.shop.insufficient", "", 0xFFFF7666);
                                return true;
                            }
                            int tier = switch (entry.id()) {
                                case "box_rare" -> 2;
                                case "box_epic" -> 3;
                                case "box_legendary" -> 4;
                                default -> 1;
                            };
                            AwakeningSystemClient.buyShopItem(entry.id());
                            Minecraft.getInstance().setScreen(new ChestOpeningScreen(this, entry.id(), entry.nameKey(), tier, entry.cost()));
                            return true;
                        }

                        AwakeningSystemClient.buyShopItem(entry.id());
                        return true;
                    }
                }
            } else {
                int catX = left + 30;
                int catY = top + 100;
                int[] widths = {34, 56, 38, 48, 46};
                for (int c = 0; c < SELL_CATEGORIES.length; c++) {
                    int btnW = widths[c];
                    if (mouseX >= catX && mouseX < catX + btnW && mouseY >= catY && mouseY < catY + 16) {
                        sellCategory = SELL_CATEGORIES[c];
                        sellScroll = 0;
                        return true;
                    }
                    catX += btnW + 3;
                }

                List<SellDefinitions.SellEntry> entries = availableSellEntries();
                for (int row = 0; row < entries.size(); row++) {
                    int entryY = top + 122 + (row - sellScroll) * 32;
                    if (entryY < top + 118 || entryY >= top + 258) continue;
                    SellDefinitions.SellEntry entry = entries.get(row);
                    int count = SellDefinitions.countInInventory(Minecraft.getInstance().player, entry);
                    if (count < entry.amount()) continue;

                    // Botón VENDER (1 / lote)
                    if (mouseX >= left + 285 && mouseX < left + 335 && mouseY >= entryY + 5 && mouseY < entryY + 23) {
                        AwakeningSystemClient.sellShopItem(entry.id(), false);
                        return true;
                    }
                    // Botón TODO
                    if (mouseX >= left + 341 && mouseX < left + 401 && mouseY >= entryY + 5 && mouseY < entryY + 23) {
                        AwakeningSystemClient.sellShopItem(entry.id(), true);
                        return true;
                    }
                }
            }
        }

        // Pestaña 5: Progreso (Botón de prestigio)
        if (tab == 5) {
            SystemData d = data();
            if (d.canPrestige()) {
                int btnX = left + 260;
                int btnY = top + 98;
                if (mouseX >= btnX && mouseX < btnX + 135 && mouseY >= btnY && mouseY < btnY + 18) {
                    AwakeningSystemClient.prestige();
                    return true;
                }
            }
        }

        // Pestaña 7: Artefactos
        if (tab == 7) {
            int artX = left + 26;
            int artY = top + 98;
            // Clic en ranura para desequipar o mejorar
            for (int i = 0; i < 5; i++) {
                int sy = artY + i * 26;
                if (mouseX >= artX && mouseX < artX + 228 && mouseY >= sy && mouseY < sy + 24) {
                    if (!data().getArtifact(i).isEmpty()) {
                        int btnX = artX + 192;
                        int btnY = sy + 3;
                        boolean onUpgradeBtn = mouseX >= btnX && mouseX < btnX + 32 && mouseY >= btnY && mouseY < btnY + 18;
                        if (button == 1 || onUpgradeBtn) {
                            // MEJORAR / FORTALECER ARTEFACTO (+1)
                            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                                    new com.soybe.awakeningsystem.network.UpgradeArtifactPayload(false, 0, i)
                            );
                            AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 1.0F, 1.2F);
                            return true;
                        } else if (button == 0) {
                            // DESEQUIPAR ARTEFACTO
                            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                                    new com.soybe.awakeningsystem.network.EquipArtifactPayload(false, 0, i, -1)
                            );
                            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                            return true;
                        }
                    }
                }
            }

            // Clic en artefacto de inventario para equipar
            int invBoxX = left + 26;
            int invBoxY = top + 232;
            int invItemY = invBoxY + 22;
            var player = Minecraft.getInstance().player;
            if (player != null) {
                int slotRendered = 0;
                for (int i = 0; i < player.getInventory().items.size(); i++) {
                    ItemStack invStack = player.getInventory().getItem(i);
                    if (!invStack.isEmpty() && invStack.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem art) {
                        int ix = invBoxX + 8 + slotRendered * 20;
                        if (ix + 18 <= invBoxX + (W - 52) - 8) {
                            if (mouseX >= ix && mouseX < ix + 18 && mouseY >= invItemY && mouseY < invItemY + 18) {
                                int targetSlot = art.getArtifactType().getSlotIndex();
                                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                                        new com.soybe.awakeningsystem.network.EquipArtifactPayload(false, 0, targetSlot, i)
                                );
                                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.1F, 1.0F);
                                return true;
                            }
                            slotRendered++;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hasControlDown()) {
            com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiScale = (float) Math.max(0.5, Math.min(1.8, com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiScale + (float) verticalAmount * 0.05F));
            com.soybe.awakeningsystem.client.gui.GuiConfig.save();
            return true;
        }
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int left = getGuiLeft();
        int top = getGuiTop();

        if (tab == 0 && mouseX >= left + 215 && mouseX < left + 415 && mouseY >= top + 152 && mouseY < top + 264) {
            int maxScroll = Math.max(0, visibleSkills().size() - 3);
            skillScroll = Math.max(0, Math.min(maxScroll, skillScroll - (int) Math.signum(verticalAmount)));
            return true;
        }
        if (tab == 1 && mouseX >= left + 20 && mouseX < left + W - 20 && mouseY >= top + 78 && mouseY < top + 264) {
            if (skillTabMode == 0) {
                int maxScroll = Math.max(0, visibleActiveSkills().size() - 3);
                activeSkillScroll = Math.max(0, Math.min(maxScroll, activeSkillScroll - (int) Math.signum(verticalAmount)));
            } else {
                int maxScroll = Math.max(0, visiblePassiveSkills().size() - 3);
                passiveSkillScroll = Math.max(0, Math.min(maxScroll, passiveSkillScroll - (int) Math.signum(verticalAmount)));
            }
            return true;
        }
        if (tab == 2 && mouseX >= left + 20 && mouseX < left + W - 20 && mouseY >= top + 78 && mouseY < top + 264) {
            int maxScroll = Math.max(0, activeMissions(data()).size() - 4);
            missionScroll = Math.max(0, Math.min(maxScroll, missionScroll - (int) Math.signum(verticalAmount)));
            return true;
        }
        if (tab == 3 && mouseX >= left + 20 && mouseX < left + W - 20 && mouseY >= top + 78 && mouseY < top + 264) {
            int maxScroll = Math.max(0, TitleDefinitions.availableDefinitions(data()).size() - 3);
            titleScroll = Math.max(0, Math.min(maxScroll, titleScroll - (int) Math.signum(verticalAmount)));
            return true;
        }
        if (tab == 4 && mouseX >= left + 20 && mouseX < left + W - 20 && mouseY >= top + 78 && mouseY < top + 264) {
            if (shopMode == 1) {
                int maxScroll = Math.max(0, availableSellEntries().size() - 4);
                sellScroll = Math.max(0, Math.min(maxScroll, sellScroll - (int) Math.signum(verticalAmount)));
            } else if (shopMode == 0) {
                int maxScroll = Math.max(0, availableShopEntries().size() - 4);
                shopScroll = Math.max(0, Math.min(maxScroll, shopScroll - (int) Math.signum(verticalAmount)));
            }
            return true;
        }
        if (tab == 5 && mouseX >= left + 20 && mouseX < left + W - 20 && mouseY >= top + 78 && mouseY < top + 264) {
            int maxScroll = Math.max(0, AchievementDefinitions.definitions().size() - 3);
            progressScroll = Math.max(0, Math.min(maxScroll, progressScroll - (int) Math.signum(verticalAmount)));
            return true;
        }
        if (tab == 6 && mouseX >= left + 20 && mouseX < left + W - 20 && mouseY >= top + 78 && mouseY < top + 264) {
            int maxScroll = Math.max(0, AwakeningSystemClient.LEADERBOARD.size() - 5);
            rankingScroll = Math.max(0, Math.min(maxScroll, rankingScroll - (int) Math.signum(verticalAmount)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingWindow && button == 0) {
            double dx = mouseX - dragStartMouseX;
            double dy = mouseY - dragStartMouseY;
            com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiOffsetX = (int) Math.round(dragStartOffsetX + dx);
            com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiOffsetY = (int) Math.round(dragStartOffsetY + dy);
            updateSearchBoxPosition();
            return true;
        }
        if (isResizingWindow && button == 0) {
            double delta = ((mouseX - resizeStartMouseX) + (mouseY - resizeStartMouseY)) / 160.0;
            com.soybe.awakeningsystem.client.gui.GuiConfig.systemGuiScale = (float) Math.max(0.5, Math.min(1.8, resizeStartScale + delta));
            updateSearchBoxPosition();
            return true;
        }
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int left = getGuiLeft();
        int top = getGuiTop();

        if (isDraggingScrollbar && button == 0) {
            if (draggingScrollbarTab == 4 && tab == 4) {
                int trackTop = top + 122;
                int trackBottom = top + 256;
                if (shopMode == 0) {
                    List<ShopDefinitions.Entry> entries = availableShopEntries();
                    int maxScroll = Math.max(0, entries.size() - 4);
                    if (maxScroll > 0) {
                        int thumbHeight = Math.max(14, (trackBottom - trackTop) * 4 / entries.size());
                        int thumbRange = Math.max(1, trackBottom - trackTop - thumbHeight);
                        float progress = (float)(mouseY - trackTop - thumbHeight / 2.0) / (float)thumbRange;
                        progress = Math.max(0.0F, Math.min(1.0F, progress));
                        shopScroll = Math.round(progress * maxScroll);
                        return true;
                    }
                } else if (shopMode == 1) {
                    List<SellDefinitions.SellEntry> entries = availableSellEntries();
                    int maxScroll = Math.max(0, entries.size() - 4);
                    if (maxScroll > 0) {
                        int thumbHeight = Math.max(14, (trackBottom - trackTop) * 4 / entries.size());
                        int thumbRange = Math.max(1, trackBottom - trackTop - thumbHeight);
                        float progress = (float)(mouseY - trackTop - thumbHeight / 2.0) / (float)thumbRange;
                        progress = Math.max(0.0F, Math.min(1.0F, progress));
                        sellScroll = Math.round(progress * maxScroll);
                        return true;
                    }
                }
            } else if (draggingScrollbarTab == 1 && tab == 1) {
                if (skillTabMode == 0) {
                    List<Map.Entry<String, Integer>> actives = visibleActiveSkills();
                    int maxScroll = Math.max(0, actives.size() - 3);
                    if (maxScroll > 0) {
                        int trackTop = top + 144;
                        int trackBottom = top + 258;
                        int thumbHeight = Math.max(14, (trackBottom - trackTop) * 3 / actives.size());
                        int thumbRange = Math.max(1, trackBottom - trackTop - thumbHeight);
                        float progress = (float)(mouseY - trackTop - thumbHeight / 2.0) / (float)thumbRange;
                        activeSkillScroll = Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * maxScroll);
                        return true;
                    }
                } else {
                    List<Map.Entry<String, Integer>> passives = visiblePassiveSkills();
                    int maxScroll = Math.max(0, passives.size() - 3);
                    if (maxScroll > 0) {
                        int trackTop = top + 138;
                        int trackBottom = top + 258;
                        int thumbHeight = Math.max(14, (trackBottom - trackTop) * 3 / passives.size());
                        int thumbRange = Math.max(1, trackBottom - trackTop - thumbHeight);
                        float progress = (float)(mouseY - trackTop - thumbHeight / 2.0) / (float)thumbRange;
                        passiveSkillScroll = Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * maxScroll);
                        return true;
                    }
                }
            } else if (draggingScrollbarTab == 2 && tab == 2) {
                List<Component> missions = activeMissions(data());
                int maxScroll = Math.max(0, missions.size() - 4);
                if (maxScroll > 0) {
                    int trackTop = top + 146;
                    int trackBottom = top + 258;
                    int thumbHeight = Math.max(14, (trackBottom - trackTop) * 4 / missions.size());
                    int thumbRange = Math.max(1, trackBottom - trackTop - thumbHeight);
                    float progress = (float)(mouseY - trackTop - thumbHeight / 2.0) / (float)thumbRange;
                    missionScroll = Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * maxScroll);
                    return true;
                }
            } else if (draggingScrollbarTab == 3 && tab == 3) {
                List<TitleDefinitions.Definition> titles = TitleDefinitions.availableDefinitions(data());
                int maxScroll = Math.max(0, titles.size() - 3);
                if (maxScroll > 0) {
                    int trackTop = top + 120;
                    int trackBottom = top + 258;
                    int thumbHeight = Math.max(14, (trackBottom - trackTop) * 3 / titles.size());
                    int thumbRange = Math.max(1, trackBottom - trackTop - thumbHeight);
                    float progress = (float)(mouseY - trackTop - thumbHeight / 2.0) / (float)thumbRange;
                    titleScroll = Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * maxScroll);
                    return true;
                }
            } else if (draggingScrollbarTab == 5 && tab == 5) {
                int maxScroll = Math.max(0, AchievementDefinitions.definitions().size() - 3);
                if (maxScroll > 0) {
                    int trackTop = top + 160;
                    int trackBottom = top + 258;
                    int thumbHeight = Math.max(14, (trackBottom - trackTop) * 3 / AchievementDefinitions.definitions().size());
                    int thumbRange = Math.max(1, trackBottom - trackTop - thumbHeight);
                    float progress = (float)(mouseY - trackTop - thumbHeight / 2.0) / (float)thumbRange;
                    progressScroll = Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * maxScroll);
                    return true;
                }
            } else if (draggingScrollbarTab == 6 && tab == 6) {
                int maxScroll = Math.max(0, AwakeningSystemClient.LEADERBOARD.size() - 5);
                if (maxScroll > 0) {
                    int trackTop = top + 116;
                    int trackBottom = top + 258;
                    int thumbHeight = Math.max(14, (trackBottom - trackTop) * 5 / AwakeningSystemClient.LEADERBOARD.size());
                    int thumbRange = Math.max(1, trackBottom - trackTop - thumbHeight);
                    float progress = (float)(mouseY - trackTop - thumbHeight / 2.0) / (float)thumbRange;
                    rankingScroll = Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * maxScroll);
                    return true;
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDraggingScrollbar = false;
            draggingScrollbarTab = -1;
            if (isDraggingWindow || isResizingWindow) {
                isDraggingWindow = false;
                isResizingWindow = false;
                com.soybe.awakeningsystem.client.gui.GuiConfig.save();
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (shopSearchBox != null && shopSearchBox.isFocused()) {
            if (keyCode == 256) { // ESC deselecciona el buscador
                shopSearchBox.setFocused(false);
                return true;
            }
            if (shopSearchBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (shopSearchBox != null && shopSearchBox.isFocused()) {
            if (shopSearchBox.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Fondo oscuro cinemático exterior a pantalla completa
        g.fill(0, 0, width, height, 0x55060D15);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (Math.abs(scale - 1.0F) > 0.001F) {
            float cx = (float) getGuiCenterX();
            float cy = (float) getGuiCenterY();
            g.pose().translate(cx, cy, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cx, -cy, 0.0F);
        }

        int left = getGuiLeft();
        int top = getGuiTop();
        SystemData data = data();

        boolean sys40 = isSys40();
        boolean sys30 = isSys30() && !sys40;
        boolean sys20 = (isSys20() || sys30) && !sys40;

        int edgeCol = sys40 ? 0xFFDFB76C : (sys30 ? 0xFFD4AF37 : (sys20 ? 0xFF9D4EDD : EDGE));
        int panelCol = sys40 ? 0xF00A1324 : (sys30 ? 0xEE0B1513 : (sys20 ? 0xE8140B22 : PANEL));
        int accentCol = sys40 ? 0xFFDFB76C : (sys30 ? 0xFFFFD700 : (sys20 ? 0xFFFFD700 : CYAN));
        int topBarCol = sys40 ? 0xFF4EE2EC : (sys30 ? 0xFFC0392B : (sys20 ? 0xFF9D4EDD : CYAN));
        int sectionCol = sys40 ? 0xFF132238 : (sys30 ? 0xFF142420 : (sys20 ? 0xFF241438 : SECTION));

        // Marco exterior
        g.fill(left - 3, top - 3, left + W + 3, top + H + 3, sys30 ? 0x40142420 : (sys20 ? 0x403A1259 : 0x4017354B));
        g.fill(left - 2, top - 2, left + W + 2, top + H + 2, edgeCol);
        g.fill(left, top, left + W, top + H, panelCol);
        g.fill(left, top, left + W, top + 3, topBarCol);

        // Esquinas holográficas tipo HUD del Sistema
        g.fill(left, top, left + 10, top + 2, accentCol);
        g.fill(left, top, left + 2, top + 10, accentCol);
        g.fill(left + W - 10, top, left + W, top + 2, accentCol);
        g.fill(left + W - 2, top, left + W, top + 10, accentCol);
        g.fill(left, top + H - 2, left + 10, top + H, accentCol);
        g.fill(left, top + H - 10, left + 2, top + H, accentCol);
        g.fill(left + W - 10, top + H - 2, left + W, top + H, accentCol);
        g.fill(left + W - 2, top + H - 10, left + W, top + H, accentCol);

        // Cabecera principal
        g.fill(left + 14, top + 12, left + W - 14, top + 44, sectionCol);
        g.fill(left + 14, top + 43, left + W - 14, top + 44, (sys30 || sys20) ? 0xFFFFD700 : 0x80276A82);
        Component titleComp = sys40 ? Component.translatable("awakening_system.screen.teyvat_v4.title").withStyle(ChatFormatting.BOLD) :
                (sys30 ? Component.translatable("awakening_system.screen.murim_v3.title").withStyle(ChatFormatting.BOLD) :
                (sys20 ? Component.translatable("awakening_system.screen.title_20").withStyle(ChatFormatting.BOLD) : Component.translatable("awakening_system.screen.title").withStyle(ChatFormatting.BOLD)));
        g.drawString(font, titleComp, left + 24, top + 21, accentCol);

        Component bearerStatus = sys40 ? Component.translatable("awakening_system.screen.teyvat_v4.status") :
                (sys30 ? Component.translatable("awakening_system.screen.murim_v3.status") :
                (sys20 ? Component.translatable("awakening_system.screen.isekai_v2.status") : Component.translatable("awakening_system.screen.bearer_status")));
        int rightX = left + W - 76 - font.width(bearerStatus);
        if (left + 24 + font.width(titleComp) + 8 < rightX) {
            g.drawString(font, bearerStatus, rightX, top + 21, (sys30 || sys20) ? 0xFFFFD700 : MUTED);
        }

        // Botones de Cabecera: [⚙] Configuración, [⟲] Centrar y [⚔] Gremio
        int btnSetX = left + W - 34;
        int btnSetY = top + 16;
        boolean setHov = mX >= btnSetX && mX < btnSetX + 16 && mY >= btnSetY && mY < btnSetY + 16;
        g.fill(btnSetX, btnSetY, btnSetX + 16, btnSetY + 16, setHov ? 0xDD2A4560 : 0x88122030);
        g.renderOutline(btnSetX, btnSetY, 16, 16, setHov ? 0xFFFFFFFF : 0xFF4EE2EC);
        g.drawCenteredString(font, "⚙", btnSetX + 8, btnSetY + 4, setHov ? 0xFFFFFFFF : 0xFFDFB76C);

        int btnResetX = left + W - 52;
        int btnResetY = top + 16;
        boolean resetHov = mX >= btnResetX && mX < btnResetX + 16 && mY >= btnResetY && mY < btnResetY + 16;
        g.fill(btnResetX, btnResetY, btnResetX + 16, btnResetY + 16, resetHov ? 0xDD2A4560 : 0x88122030);
        g.renderOutline(btnResetX, btnResetY, 16, 16, resetHov ? 0xFFFFFFFF : 0xFF4EE2EC);
        g.drawCenteredString(font, "⟲", btnResetX + 8, btnResetY + 4, resetHov ? 0xFFFFFFFF : 0xFF70E8FF);

        int btnGuildX = left + W - 70;
        int btnGuildY = top + 16;
        boolean guildHov = mX >= btnGuildX && mX < btnGuildX + 16 && mY >= btnGuildY && mY < btnGuildY + 16;
        g.fill(btnGuildX, btnGuildY, btnGuildX + 16, btnGuildY + 16, guildHov ? 0xDD2A4560 : 0x88122030);
        g.renderOutline(btnGuildX, btnGuildY, 16, 16, guildHov ? 0xFFFFFFFF : 0xFF4EE2EC);
        g.drawCenteredString(font, "⚔", btnGuildX + 8, btnGuildY + 4, guildHov ? 0xFFFFFFFF : 0xFF70E8FF);

        // Pestañas dimensionales del lateral izquierdo
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showDimensionSideTabs) {
            renderDimensionSideTabs(g, left, top, mX, mY);
        }

        renderTabs(g, left, top, mX, mY);

        if (tab == 0) renderStatus(g, left, top, data, mX, mY);
        if (tab == 1) renderSkills(g, left, top, data, mX, mY);
        if (tab == 2) renderMissions(g, left, top, data, mX, mY);
        if (tab == 3) renderTitles(g, left, top, data, mX, mY);
        if (tab == 4) renderShop(g, left, top, data, mX, mY);
        if (tab == 5) renderProgress(g, left, top, data, mX, mY);
        if (tab == 6) renderRanking(g, left, top, data, mX, mY);
        if (tab == 7) renderArtifacts(g, left, top, data, mX, mY);

        g.drawString(font, Component.translatable("awakening_system.screen.close_hint"), left + W - 145, top + H - 16, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));

        // Grip de redimensionamiento en la esquina inferior derecha
        boolean gripHov = mX >= left + W - 18 && mX <= left + W && mY >= top + H - 18 && mY <= top + H;
        int gripCol = gripHov ? 0xFFFFFFFF : accentCol;
        g.fill(left + W - 12, top + H - 4, left + W - 4, top + H - 2, gripCol);
        g.fill(left + W - 8, top + H - 8, left + W - 4, top + H - 6, gripCol);
        g.fill(left + W - 4, top + H - 12, left + W - 2, top + H - 10, gripCol);

        // Tooltips de botones de cabecera y grip
        if (guildHov) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("§6⚔ Gremio de Cazadores").withStyle(ChatFormatting.BOLD));
            tip.add(Component.literal("§7Administra tu gremio, miembros, perks y sede."));
            tip.add(Component.literal("§b▸ Clic para abrir el panel del gremio").withStyle(ChatFormatting.AQUA));
            g.renderComponentTooltip(font, tip, mX, mY);
        } else if (setHov) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("§6✦ Configuración de Interfaz").withStyle(ChatFormatting.BOLD));
            tip.add(Component.literal("§7Personaliza la escala, el HUD y qué elementos mostrar/ocultar."));
            tip.add(Component.literal("§b▸ Clic para abrir el panel de ajustes").withStyle(ChatFormatting.AQUA));
            g.renderComponentTooltip(font, tip, mX, mY);
        } else if (resetHov) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("§b✦ Centrar y Restablecer Interfaz").withStyle(ChatFormatting.BOLD));
            tip.add(Component.literal("§7Devuelve la ventana al centro y escala al 100%."));
            g.renderComponentTooltip(font, tip, mX, mY);
        } else if (gripHov) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("§e✦ Redimensionar Ventana").withStyle(ChatFormatting.BOLD));
            tip.add(Component.literal("§7Haz clic y arrastra con el ratón para agrandar o achicar."));
            tip.add(Component.literal("§8(También puedes usar Ctrl + Rueda del ratón)"));
            g.renderComponentTooltip(font, tip, mX, mY);
        }

        // Tooltips de pestañas dimensionales al final para máxima prioridad visual
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showDimensionSideTabs) {
            renderDimensionSideTabTooltips(g, left, top, mX, mY);
        }

        g.pose().popPose();

        super.render(g, mouseX, mouseY, partialTick);
    }

    private static final String[] ARTIFACT_SLOT_EMOJIS = {"🌸", "🪶", "⏳", "🏆", "👑"};

    private void renderArtifacts(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        // Placa base sólida de alta fidelidad tecnológica (elimina 100% la transparencia del mundo exterior)
        g.fill(left + 20, top + 78, left + W - 20, top + 288, 0xFF0B1420);
        g.renderOutline(left + 20, top + 78, W - 40, 210, 0xFF1D3248);
        g.fill(left + 21, top + 79, left + W - 21, top + 93, 0xFF142436);
        g.drawString(font, Component.translatable("awakening_system.screen.artifacts.title").withStyle(ChatFormatting.BOLD), left + 26, top + 82, 0xFFFFD700);

        int artX = left + 26;
        int artY = top + 98;

        // 1. RENDERIZADO DE LAS 5 CASILLAS DE ARTEFACTOS
        for (int i = 0; i < 5; i++) {
            com.soybe.awakeningsystem.artifact.ArtifactType slotType = com.soybe.awakeningsystem.artifact.ArtifactType.fromSlot(i);
            int sy = artY + i * 26;
            boolean hoverSlot = mouseX >= artX && mouseX < artX + 228 && mouseY >= sy && mouseY < sy + 24;
            g.fill(artX, sy, artX + 228, sy + 24, hoverSlot ? 0xFF1E2D3F : 0xFF121B27);
            g.renderOutline(artX, sy, 228, 24, hoverSlot ? 0xFFFFD700 : 0xFF283B4F);

            ItemStack stack = d.getArtifact(i);
            if (!stack.isEmpty()) {
                g.renderItem(stack, artX + 4, sy + 4);
                String artName = stack.getHoverName().getString();
                if (font.width(artName) > 164) {
                    artName = font.plainSubstrByWidth(artName, 156) + "..";
                }
                g.drawString(font, "§f" + artName, artX + 24, sy + 3, 0xFFFFFF);
                int artLvl = com.soybe.awakeningsystem.artifact.GenshinArtifactItem.getLevel(stack);
                Component bonusComp = Component.literal("§a+" + artLvl + " §8| §7").append(slotType.getStatBonusComponent());
                g.drawString(font, bonusComp, artX + 24, sy + 13, 0x88FF88);

                // Botón visual de Mejora [▲+1]
                int btnX = artX + 192;
                int btnW = 32;
                int btnY = sy + 3;
                boolean hoverUp = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + 18;
                if (artLvl < 20) {
                    g.fill(btnX, btnY, btnX + btnW, btnY + 18, hoverUp ? 0xFF2D824A : 0xFF184524);
                    g.renderOutline(btnX, btnY, btnW, 18, hoverUp ? 0xFF55FF88 : 0xFF2A8540);
                    g.drawCenteredString(font, "§a▲+1", btnX + btnW / 2, btnY + 5, 0xFFFFFF);
                } else {
                    g.fill(btnX, btnY, btnX + btnW, btnY + 18, 0xFF1C2530);
                    g.renderOutline(btnX, btnY, btnW, 18, 0xFF354456);
                    g.drawCenteredString(font, Component.translatable("awakening_system.screen.artifacts.btn_max"), btnX + btnW / 2, btnY + 5, 0x888888);
                }
            } else {
                Component slotLabel = Component.literal("§8" + ARTIFACT_SLOT_EMOJIS[i] + " ").append(slotType.getDisplayNameComponent());
                g.drawString(font, slotLabel, artX + 8, sy + 4, 0x888888);
                g.drawString(font, Component.translatable("awakening_system.screen.artifacts.empty_slot", slotType.getStatBonusComponent()), artX + 8, sy + 13, 0x555555);
            }
        }

        // 2. PANEL DE RESUMEN DE ESTADÍSTICAS Y BONOS DE CONJUNTO (Derecha)
        int summaryX = left + 258;
        int summaryY = top + 98;
        int summaryW = left + W - 26 - summaryX;
        int summaryH = 128;

        g.fill(summaryX, summaryY, summaryX + summaryW, summaryY + summaryH, 0xFF111A26);
        g.renderOutline(summaryX, summaryY, summaryW, summaryH, 0xFF283B4F);

        g.drawString(font, Component.translatable("awakening_system.screen.artifacts.total_stats"), summaryX + 8, summaryY + 8, 0xFFFFD700);

        float hp = d.getArtifactBonusHp();
        float atk = d.getArtifactBonusAttack();
        float crit = d.getArtifactBonusCritRate();
        float critDmg = d.getArtifactBonusCritDmg();

        g.drawString(font, Component.translatable("awakening_system.screen.artifacts.stat.hp", (int)hp), summaryX + 8, summaryY + 22, 0xFFFFFF);
        g.drawString(font, Component.translatable("awakening_system.screen.artifacts.stat.atk", String.format("%.1f", atk)), summaryX + 8, summaryY + 34, 0xFFFFFF);
        g.drawString(font, Component.translatable("awakening_system.screen.artifacts.stat.crit_rate", String.format("%.1f", crit)), summaryX + 8, summaryY + 46, 0xFFFFFF);
        g.drawString(font, Component.translatable("awakening_system.screen.artifacts.stat.crit_dmg", String.format("%.1f", critDmg)), summaryX + 8, summaryY + 58, 0xFFFFFF);

        g.fill(summaryX + 6, summaryY + 71, summaryX + summaryW - 6, summaryY + 72, 0xFF283B4F);
        g.drawString(font, Component.translatable("awakening_system.screen.artifacts.set_bonuses"), summaryX + 8, summaryY + 76, 0xFFFFD700);

        var sets = d.getActiveArtifactSetPieces();
        int setLine = 0;
        for (var entry : sets.entrySet()) {
            if (entry.getValue() >= 2) {
                Component str = Component.literal(entry.getKey().getColorCode() + "• ")
                        .append(entry.getKey().getDisplayNameComponent())
                        .append(Component.literal(" §e(" + entry.getValue() + "P)"));
                g.drawString(font, str, summaryX + 8, summaryY + 88 + setLine * 10, 0xFFFFFF);
                setLine++;
                if (setLine >= 4) break;
            }
        }
        if (setLine == 0) {
            g.drawString(font, Component.translatable("awakening_system.screen.artifacts.no_set_bonus"), summaryX + 8, summaryY + 88, 0x666666);
            g.drawString(font, Component.translatable("awakening_system.screen.artifacts.set_bonus_hint"), summaryX + 8, summaryY + 98, 0x444444);
        }

        // 3. CONTENEDOR DE ARTEFACTOS EN INVENTARIO (Abajo)
        int invBoxX = left + 26;
        int invBoxY = top + 232;
        int invBoxW = W - 52;
        int invBoxH = 50;
        g.fill(invBoxX, invBoxY, invBoxX + invBoxW, invBoxY + invBoxH, 0xFF0E1622);
        g.renderOutline(invBoxX, invBoxY, invBoxW, invBoxH, 0xFF203247);
        g.drawString(font, "§6📦 §e" + Component.translatable("awakening_system.screen.artifacts.inv_label").getString() + " §7(Clic izquierdo para equipar)", invBoxX + 6, invBoxY + 5, 0xFFE0E0E0);

        int invItemY = invBoxY + 22;
        var player = Minecraft.getInstance().player;
        int slotRendered = 0;
        if (player != null) {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (!invStack.isEmpty() && invStack.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem) {
                    int ix = invBoxX + 8 + slotRendered * 20;
                    if (ix + 18 <= invBoxX + invBoxW - 8) {
                        boolean hoverInv = mouseX >= ix && mouseX < ix + 18 && mouseY >= invItemY && mouseY < invItemY + 18;
                        g.fill(ix - 1, invItemY - 1, ix + 17, invItemY + 17, hoverInv ? 0xFF2A425E : 0xFF14202E);
                        g.renderOutline(ix - 1, invItemY - 1, 18, 18, hoverInv ? 0xFFFFD700 : 0xFF283B4F);
                        g.renderItem(invStack, ix, invItemY);
                        slotRendered++;
                    }
                }
            }
        }
        if (slotRendered == 0) {
            g.drawString(font, "§7No tienes artefactos sin equipar en tu inventario.", invBoxX + 8, invItemY + 5, 0x666666);
        }

        // TOOLTIPS
        for (int i = 0; i < 5; i++) {
            int sy = artY + i * 26;
            if (mouseX >= artX && mouseX < artX + 228 && mouseY >= sy && mouseY < sy + 24) {
                ItemStack stack = d.getArtifact(i);
                if (!stack.isEmpty()) {
                    int btnX = artX + 192;
                    int btnW = 32;
                    int btnY = sy + 3;
                    boolean onUpgradeBtn = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + 18;
                    int artLvl = com.soybe.awakeningsystem.artifact.GenshinArtifactItem.getLevel(stack);
                    if (onUpgradeBtn) {
                        List<Component> upTip = new java.util.ArrayList<>();
                        upTip.add(Component.translatable("awakening_system.screen.artifacts.upgrade_title"));
                        if (artLvl < 20) {
                            int cost = (artLvl + 1) * 350;
                            upTip.add(Component.translatable("awakening_system.screen.artifacts.current_level", artLvl, artLvl + 1));
                            upTip.add(Component.translatable("awakening_system.screen.artifacts.cost", cost));
                            upTip.add(Component.translatable("awakening_system.screen.artifacts.upgrade_instruction"));
                        } else {
                            upTip.add(Component.translatable("awakening_system.screen.artifacts.max_reached"));
                        }
                        g.renderComponentTooltip(font, upTip, mouseX, mouseY);
                    } else {
                        List<Component> tooltip = new java.util.ArrayList<>(Screen.getTooltipFromItem(this.minecraft, stack));
                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("awakening_system.screen.artifacts.tip_right_click"));
                        tooltip.add(Component.translatable("awakening_system.screen.artifacts.tip_left_click"));
                        g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                    }
                }
            }
        }
        if (player != null) {
            int slotRenderedTooltip = 0;
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (!invStack.isEmpty() && invStack.getItem() instanceof com.soybe.awakeningsystem.artifact.GenshinArtifactItem) {
                    int ix = invBoxX + 8 + slotRenderedTooltip * 20;
                    if (ix + 18 <= invBoxX + invBoxW - 8) {
                        if (mouseX >= ix && mouseX < ix + 18 && mouseY >= invItemY && mouseY < invItemY + 18) {
                            g.renderTooltip(font, invStack, mouseX, mouseY);
                        }
                        slotRenderedTooltip++;
                    }
                }
            }
        }
    }

    private boolean isSys40() {
        return data().system40Updated() || data().isSystem40Updating() || data().teyvatUnlocked();
    }

    private boolean isSys30() {
        return isSys40() || data().system30Updated() || data().isSystem30Updating() || data().murimUnlocked();
    }

    private boolean isSys20() {
        return isSys30() || data().systemUpdated() || data().isSystemUpdating() || data().isekaiUnlocked();
    }

    private static final net.minecraft.resources.ResourceLocation TAB_OVERWORLD_TEX =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/tab_overworld.png");
    private static final net.minecraft.resources.ResourceLocation TAB_ISEKAI_TEX =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/tab_isekai.png");
    private static final net.minecraft.resources.ResourceLocation TAB_MURIM_TEX =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/tab_murim.png");
    private static final net.minecraft.resources.ResourceLocation TAB_TEYVAT_TEX =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/tab_teyvat.png");

    private static class DimTab {
        final String id;
        final net.minecraft.resources.ResourceLocation texture;
        final Component title;
        final Component desc;

        DimTab(String id, net.minecraft.resources.ResourceLocation texture, Component title, Component desc) {
            this.id = id;
            this.texture = texture;
            this.title = title;
            this.desc = desc;
        }
    }

    private List<DimTab> getActiveSideTabs() {
        List<DimTab> list = new ArrayList<>();
        boolean sys40 = isSys40();
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30 || sys40;

        if (sys20) {
            list.add(new DimTab("overworld", TAB_OVERWORLD_TEX,
                    Component.translatable("awakening_system.screen.dim.earth.title"),
                    Component.translatable("awakening_system.screen.dim.earth.desc")));
            list.add(new DimTab("isekai", TAB_ISEKAI_TEX,
                    Component.translatable("awakening_system.screen.dim.isekai.title"),
                    Component.translatable("awakening_system.screen.dim.isekai.desc")));
            if (sys30) {
                list.add(new DimTab("murim", TAB_MURIM_TEX,
                        Component.translatable("awakening_system.screen.dim.murim.title"),
                        Component.translatable("awakening_system.screen.dim.murim.desc")));
            }
            if (sys40) {
                list.add(new DimTab("teyvat", TAB_TEYVAT_TEX,
                        Component.translatable("awakening_system.screen.dim.teyvat.title"),
                        Component.translatable("awakening_system.screen.dim.teyvat.desc")));
            }
        }
        // Subespacio Dimensional siempre está disponible en todas las versiones (al final de la lista)
        list.add(new DimTab("subspace", null,
                Component.translatable("awakening_system.screen.dim.subspace.title"),
                Component.translatable("awakening_system.screen.dim.subspace.desc")));
        return list;
    }

    private void renderDimensionSideTabs(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        String currentDim = "";
        if (minecraft != null && minecraft.level != null) {
            var key = minecraft.level.dimension();
            if (key.equals(net.minecraft.world.level.Level.OVERWORLD)) currentDim = "overworld";
            else if (key.equals(IsekaiWorldManager.ISEKAI_WORLD)) currentDim = "isekai";
            else if (key.equals(MurimWorldManager.MURIM_WORLD)) currentDim = "murim";
            else if (key.equals(TeyvatWorldManager.TEYVAT_WORLD)) currentDim = "teyvat";
            else if (key.equals(com.soybe.awakeningsystem.subspace.SubspaceManager.SUBSPACE_REALM)) currentDim = "subspace";
        }

        int tx = left - 32;
        List<DimTab> sideTabs = getActiveSideTabs();
        for (int i = 0; i < sideTabs.size(); i++) {
            DimTab t = sideTabs.get(i);
            int tabY = top + 55 + i * 33;
            boolean hov = mouseX >= tx && mouseX < tx + 30 && mouseY >= tabY && mouseY < tabY + 28;
            boolean act = t.id.equals(currentDim);

            if ("subspace".equals(t.id)) {
                int bg = act ? 0xEE1E4578 : (hov ? 0xEE1A365D : 0xCC0F1E36);
                int border = act ? 0xFF70E8FF : (hov ? 0xFF90CAF9 : 0xFF2B6CB0);
                g.fill(tx, tabY, tx + 30, tabY + 28, bg);
                drawBorder(g, tx, tabY, 30, 28, border);
                if (act) g.fill(tx + 28, tabY, tx + 30, tabY + 28, 0xFF70E8FF);
                g.drawCenteredString(font, Component.literal("§9⬡"), tx + 15, tabY + 5, 0xFF63B3ED);
                g.drawCenteredString(font, Component.literal("§f✦"), tx + 15, tabY + 13, 0xFFEBF8FF);
            } else {
                int bg;
                int border;
                switch (t.id) {
                    case "overworld" -> {
                        bg = act ? 0xEE143249 : (hov ? 0xDD1B4357 : 0xCC0D2030);
                        border = act ? 0xFF70E8FF : (hov ? 0xFF5DADE2 : 0xFF276A82);
                    }
                    case "isekai" -> {
                        bg = act ? 0xEE3A1259 : (hov ? 0xDD4F1B78 : 0xCC200B32);
                        border = act ? 0xFFFFD700 : (hov ? 0xFFBD93F9 : 0xFF9D4EDD);
                    }
                    case "murim" -> {
                        bg = act ? 0xEE1C382E : (hov ? 0xDD244A3C : 0xCC0E1F1A);
                        border = act ? 0xFFFFD700 : (hov ? 0xFF50FA7B : 0xFFD4AF37);
                    }
                    case "teyvat" -> {
                        bg = act ? 0xEE142436 : (hov ? 0xDD1F354D : 0xCC0D1824);
                        border = act ? 0xFFFFD700 : (hov ? 0xFF58C4DC : 0xFF2A7590);
                    }
                    default -> {
                        bg = 0xCC0D2030;
                        border = 0xFF276A82;
                    }
                }
                g.fill(tx, tabY, tx + 30, tabY + 28, bg);
                drawBorder(g, tx, tabY, 30, 28, border);
                if (act) {
                    g.fill(tx + 28, tabY, tx + 30, tabY + 28, 0xFFFFD700);
                }
                if (t.texture != null) {
                    g.blit(t.texture, tx + 5, tabY + 4, 20, 20, 0.0F, 0.0F, 32, 32, 32, 32);
                }
            }
        }
    }

    private void renderDimensionSideTabTooltips(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        int tx = left - 32;
        List<DimTab> sideTabs = getActiveSideTabs();
        for (int i = 0; i < sideTabs.size(); i++) {
            DimTab t = sideTabs.get(i);
            int tabY = top + 55 + i * 33;
            if (mouseX >= tx && mouseX < tx + 30 && mouseY >= tabY && mouseY < tabY + 28) {
                List<Component> tip = new ArrayList<>();
                tip.add(t.title);
                String descStr = t.desc.getString();
                if (descStr.contains("\n")) {
                    for (String line : descStr.split("\n")) {
                        tip.add(Component.literal(line));
                    }
                } else {
                    tip.add(t.desc);
                }
                g.renderComponentTooltip(font, tip, mouseX, mouseY);
                break;
            }
        }
    }



    private void renderTabs(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        int tabWidth = tabWidth();
        boolean sys40 = isSys40();
        boolean sys30 = isSys30() && !sys40;
        boolean sys20 = (isSys20() || isSys30()) && !sys40;
        for (int i = 0; i < TABS.length; i++) {
            int x = tabX(left, i);
            boolean selected = tab == i;
            boolean hovered = mouseX >= x && mouseX < x + tabWidth && mouseY >= top + 51 && mouseY < top + 71;
            int bg = selected ? (sys40 ? 0xFF142C44 : (sys30 ? 0xFF1C382E : (sys20 ? 0xFF3A1259 : 0xFF1B4357))) :
                    (hovered ? (sys40 ? 0x60142C44 : (sys30 ? 0x601C382E : (sys20 ? 0x603A1259 : 0x6017354B))) :
                    (sys40 ? 0xFF0A1522 : (sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF0D1E2C))));
            g.fill(x, top + 51, x + tabWidth, top + 71, bg);
            if (selected) {
                int borderCol = sys40 ? 0xFFFFD700 : ((sys30 || sys20) ? 0xFFFFD700 : CYAN);
                g.fill(x, top + 51, x + tabWidth, top + 53, borderCol);
                g.fill(x, top + 69, x + tabWidth, top + 71, borderCol);
            } else if (hovered) {
                g.fill(x, top + 69, x + tabWidth, top + 71, sys40 ? 0x804EE2EC : (sys30 ? 0x8050FA7B : (sys20 ? 0x809D4EDD : 0x802D7691)));
            }
            Component label = Component.translatable("awakening_system.screen.tab." + TABS[i]);
            int textColor = selected ? (sys40 ? 0xFFFFE082 : ((sys30 || sys20) ? 0xFFFFD700 : 0xFFFFFFFF)) :
                    (hovered ? (sys40 ? 0xFF80D8FF : (sys30 ? 0xFF50FA7B : (sys20 ? 0xFFF2D6 : 0xFFDDECF2))) :
                    (sys40 ? 0xFF90CAF9 : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED))));
            drawAutoScaledCentered(g, label, x + tabWidth / 2, top + 58, textColor, tabWidth - 6);
        }
    }

    private int tabWidth() { return (W - 32) / TABS.length; }
    private int tabX(int left, int index) { return left + 16 + index * tabWidth(); }

    private void renderStatus(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        var player = Minecraft.getInstance().player;
        TitleDefinitions.Definition equippedDef = TitleDefinitions.get(d.equippedTitle());
        ConstellationDefinitions.Constellation sponsorDef = d.hasSponsor() ? ConstellationDefinitions.get(d.sponsorConstellation()) : null;
        boolean titleHovered = false;
        boolean classHovered = false;
        boolean sponsorHovered = false;
        boolean sys40 = isSys40();
        boolean sys30 = isSys30() && !sys40;
        boolean sys20 = (isSys20() || isSys30()) && !sys40;

        if (player != null) {
            // Fila 1: Nombre (Izquierda) y Puntos de atributo disponibles (Derecha)
            drawClipped(g, player.getName(), left + 24, top + 74, 0xFFFFFFFF, 135);
            drawClipped(g, Component.translatable("awakening_system.screen.profile.points", d.attributePoints()), left + 220, top + 74,
                    d.attributePoints() > 0 ? (sys40 ? 0xFFFFE082 : 0xFFFFD700) : (sys40 ? 0xFF90CAF9 : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED))), 175);

            // Fila 2: Nivel y Rango (Izquierda) y Título equipado (Derecha)
            drawClipped(g, Component.translatable("awakening_system.screen.profile.level_rank", d.level(), d.rankComponent()), left + 24, top + 86, sys40 ? 0xFFFFE082 : 0xFFFFD866, 135);
            titleHovered = mouseX >= left + 218 && mouseX < left + 398 && mouseY >= top + 82 && mouseY < top + 94;
            Component titleComp = Component.translatable("awakening_system.screen.profile.title",
                    equippedDef != null ? Component.translatable(equippedDef.translationKey()) : Component.translatable("awakening_system.title.none"));
            drawClipped(g, titleComp, left + 220, top + 86, titleHovered ? 0xFFFFD700 : (sys40 ? 0xFF80D8FF : (sys30 ? 0xFF50FA7B : (sys20 ? 0xFFFFE4A0 : 0xFFE4A7FF))), 175);

            // Fila 3: Clase de Cazador (Izquierda) y Patrocinador Cósmico (Derecha)
            classHovered = mouseX >= left + 20 && mouseX < left + 205 && mouseY >= top + 95 && mouseY < top + 109;
            if (d.hasHunterClass()) {
                Component cName = Component.translatable("awakening_system.class." + d.hunterClass() + ".name");
                drawClipped(g, Component.translatable("awakening_system.screen.class_tree_tag", cName), left + 24, top + 98, classHovered ? 0xFFFFD700 : (sys40 ? 0xFFB3E5FC : 0xFFB0C4DE), 175);
            } else if (d.level() >= 30) {
                int cBg = classHovered ? (sys40 ? 0xDD143E5C : (sys30 ? 0xDD1E6E45 : (sys20 ? 0xDD9D4EDD : 0xDD276A82))) : (sys40 ? 0xAA091B2E : (sys30 ? 0xAA0E261E : (sys20 ? 0xAA2B0D40 : 0xAA0E2230)));
                g.fill(left + 22, top + 96, left + 165, top + 109, cBg);
                g.drawCenteredString(font, Component.translatable("awakening_system.class.select_prompt").copy().withStyle(classHovered ? (sys40 ? ChatFormatting.AQUA : (sys30 ? ChatFormatting.GREEN : (sys20 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA))) : ChatFormatting.GOLD), left + 93, top + 98, 0xFFFFD700);
            } else {
                drawClipped(g, Component.translatable("awakening_system.class.locked_short"), left + 24, top + 98, 0xFF777777, 175);
            }

            sponsorHovered = mouseX >= left + 218 && mouseX < left + 398 && mouseY >= top + 95 && mouseY < top + 109;
            if (d.hasSponsor() && sponsorDef != null) {
                Component sponsorComp = Component.translatable("awakening_system.screen.sponsor.badge", sponsorDef.name());
                drawClipped(g, sponsorComp, left + 220, top + 98, sponsorHovered ? 0xFFFFD700 : (sys40 ? 0xFFFFE082 : (sys30 ? 0xFF50FA7B : 0xFFD055FF)), 175);
            } else if (d.level() >= 100) {
                Component prompt = Component.translatable("awakening_system.screen.sponsor.select_prompt");
                drawClipped(g, prompt, left + 220, top + 98, sponsorHovered ? 0xFFFFD700 : 0xFFFFD866, 175);
            } else {
                Component locked = Component.translatable("awakening_system.screen.sponsor.locked");
                drawClipped(g, locked, left + 220, top + 98, sys40 ? 0xFF90CAF9 : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED)), 175);
            }

            // Fila 4: Barras de Vida (HP) y Maná / Qi / Energía Celestial
            int maxHp = (int) player.getMaxHealth();
            int curHp = (int) Math.min(maxHp, player.getHealth());
            int hpBarW = 125;
            g.fill(left + 24, top + 112, left + 24 + hpBarW, top + 123, 0xFF1A0A0D);
            int hpFill = (int) (hpBarW * Math.max(0.0F, Math.min(1.0F, (float) curHp / maxHp)));
            if (hpFill > 0) g.fill(left + 24, top + 112, left + 24 + hpFill, top + 123, 0xFFE74C3C);
            String hpStr = curHp + "/" + maxHp + " HP";
            g.drawCenteredString(font, Component.literal(hpStr), left + 24 + hpBarW / 2, top + 113, 0xFFFFFFFF);

            int maxMp = d.maxMana();
            int curMp = d.mana();
            int mpBarW = 175;
            int mpBg = sys40 ? 0xFF081829 : (sys20 ? 0xFF1B0C2E : 0xFF081822);
            int mpFillCol = sys40 ? 0xFF4EE2EC : (sys20 ? 0xFFB388FF : 0xFF39B9D5);
            g.fill(left + 220, top + 112, left + 220 + mpBarW, top + 123, mpBg);
            int mpFill = (int) (mpBarW * Math.max(0.0F, Math.min(1.0F, (float) curMp / Math.max(1, maxMp))));
            if (mpFill > 0) g.fill(left + 220, top + 112, left + 220 + mpFill, top + 123, mpFillCol);
            String mpStr = curMp + "/" + maxMp + " " + (sys40 ? "CE" : (sys30 ? "QI" : "MP")) + " (+" + d.manaRegenRate() + "/tick)";
            g.drawCenteredString(font, Component.literal(mpStr), left + 220 + mpBarW / 2, top + 113, 0xFFFFFFFF);
        }

        // Fila 5: Texto de Experiencia
        int expNeeded = Math.max(1, d.nextLevelExperience());
        int expCurrent = d.experience();
        int pct = Math.min(100, (int) ((double) expCurrent / expNeeded * 100));
        String expStr = "EXP " + expCurrent + " / " + expNeeded + " (" + pct + "%)";
        g.drawString(font, Component.literal(expStr), left + 24, top + 127, sys40 ? 0xFFFFE082 : ((sys30 || sys20) ? 0xFFFFD700 : 0xFF8BE9FD));

        // Barra visual de Experiencia
        g.fill(left + 20, top + 138, left + W - 20, top + 145, sys40 ? 0xFF081829 : (sys30 ? 0xFF0A1F18 : (sys20 ? 0xFF1B0C2E : 0xFF0C1925)));
        int bar = (int) ((W - 40) * Math.min(1.0D, (double) expCurrent / expNeeded));
        if (bar > 0) {
            g.fill(left + 20, top + 138, left + 20 + bar, top + 145, sys40 ? 0xFF166070 : (sys30 ? 0xFF1B7A4E : (sys20 ? 0xFF8A2BE2 : 0xFF276A82)));
            g.fill(left + 20, top + 138, left + 20 + bar, top + 140, sys40 ? 0xFFFFD700 : ((sys30 || sys20) ? 0xFFFFD700 : 0xFF70E8FF));
        }

        // Bloque de Atributos con botones interactivos resaltados
        boolean canSpend = d.attributePoints() > 0;
        section(g, left + 20, top + 152, 185, 112, Component.translatable("awakening_system.screen.attributes"));
        enableScissorScaled(g, left + 22, top + 172, left + 203, top + 262);
        attribute(g, left + 28, top + 180, "strength", Component.translatable("awakening_system.attribute.str.short").getString(), d.strength(), left + 88, top + 178, mouseX, mouseY, canSpend);
        attribute(g, left + 28, top + 206, "vitality", Component.translatable("awakening_system.attribute.vit.short").getString(), d.vitality(), left + 88, top + 204, mouseX, mouseY, canSpend);
        attribute(g, left + 28, top + 232, "agility", Component.translatable("awakening_system.attribute.agi.short").getString(), d.agility(), left + 88, top + 230, mouseX, mouseY, canSpend);
        attribute(g, left + 115, top + 180, "perception", Component.translatable("awakening_system.attribute.per.short").getString(), d.perception(), left + 174, top + 178, mouseX, mouseY, canSpend);
        attribute(g, left + 115, top + 206, "intelligence", Component.translatable("awakening_system.attribute.int.short").getString(), d.intelligence(), left + 174, top + 204, mouseX, mouseY, canSpend);
        attribute(g, left + 115, top + 232, "willpower", Component.translatable("awakening_system.attribute.wil.short").getString(), d.willpower(), left + 174, top + 230, mouseX, mouseY, canSpend);
        g.disableScissor();

        renderPlayerModel(g, left, top, mouseX, mouseY);

        boolean constHovered = false;
        boolean cultHovered = false;
        boolean fableHovered = false;
        if (d.isConstellation()) {
            int btnY = top + 266;
            int btnH = 26;

            // 1. Botón Canal de Constelación (Izquierda)
            int btnX = left + 20;
            int btnW = 128;
            constHovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
            g.fill(btnX - 1, btnY - 1, btnX + btnW + 1, btnY + btnH + 1, constHovered ? 0xFFFFD866 : 0xFF8A38B8);
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, constHovered ? 0xDD2A1448 : 0xDD170B28);
            drawBorder(g, btnX, btnY, btnW, btnH, constHovered ? 0xFFFFD700 : 0xFF8A38B8);

            enableScissorScaled(g, btnX + 1, btnY + 1, btnX + btnW - 1, btnY + btnH - 1);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.btn.channel").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), btnX + btnW / 2, btnY + 4, 0xFFFFD700);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.btn.constellation"), btnX + btnW / 2, btnY + 14, 0xFFFFFFFF);
            g.disableScissor();

            // 2. Botón Fábulas del Star Stream (Centro)
            int fabX = left + 154;
            int fabW = 128;
            fableHovered = mouseX >= fabX && mouseX < fabX + fabW && mouseY >= btnY && mouseY < btnY + btnH;
            g.fill(fabX - 1, btnY - 1, fabX + fabW + 1, btnY + btnH + 1, fableHovered ? 0xFFFFD866 : 0xFF00838F);
            g.fill(fabX, btnY, fabX + fabW, btnY + btnH, fableHovered ? 0xDD0C2836 : 0xDD071822);
            drawBorder(g, fabX, btnY, fabW, btnH, fableHovered ? 0xFFFFD700 : 0xFF00E5FF);

            enableScissorScaled(g, fabX + 1, btnY + 1, fabX + fabW - 1, btnY + btnH - 1);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.btn.fables").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), fabX + fabW / 2, btnY + 4, 0xFFFFD700);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.btn.fables_sub", d.unlockedFables().size(), d.fableWeight()), fabX + fabW / 2, btnY + 14, 0xFFCCE3EC);
            g.disableScissor();

            // 3. Botón Cultivo Murim (Derecha)
            int cultX = left + 288;
            int cultW = 132;
            cultHovered = mouseX >= cultX && mouseX < cultX + cultW && mouseY >= btnY && mouseY < btnY + btnH;
            g.fill(cultX - 1, btnY - 1, cultX + cultW + 1, btnY + btnH + 1, cultHovered ? 0xFF50FA7B : 0xFF1E6E45);
            g.fill(cultX, btnY, cultX + cultW, btnY + btnH, cultHovered ? 0xDD123E28 : 0xDD0A2317);
            drawBorder(g, cultX, btnY, cultW, btnH, cultHovered ? 0xFF50FA7B : 0xFF1E6E45);

            Component realmName = Component.translatable("awakening_system.cultivation.realm." + d.dantianRealm());
            enableScissorScaled(g, cultX + 1, btnY + 1, cultX + cultW - 1, btnY + btnH - 1);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.btn.cultivation").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), cultX + cultW / 2, btnY + 4, 0xFF50FA7B);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation_sub", realmName, d.cultivationQi()), cultX + cultW / 2, btnY + 14, 0xFFCCE3EC);
            g.disableScissor();
        } else {
            int btnY = top + 266;
            int btnH = 26;

            // 1. Botón Fábulas del Star Stream (Izquierda)
            int fabX = left + 20;
            int fabW = 128;
            fableHovered = mouseX >= fabX && mouseX < fabX + fabW && mouseY >= btnY && mouseY < btnY + btnH;
            g.fill(fabX - 1, btnY - 1, fabX + fabW + 1, btnY + btnH + 1, fableHovered ? 0xFFFFD866 : 0xFF00838F);
            g.fill(fabX, btnY, fabX + fabW, btnY + btnH, fableHovered ? 0xDD0C2836 : 0xDD071822);
            drawBorder(g, fabX, btnY, fabW, btnH, fableHovered ? 0xFFFFD700 : 0xFF00E5FF);

            enableScissorScaled(g, fabX + 1, btnY + 1, fabX + fabW - 1, btnY + btnH - 1);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.btn.fables").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), fabX + fabW / 2, btnY + 4, 0xFFFFD700);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.btn.fables_sub", d.unlockedFables().size(), d.fableWeight()), fabX + fabW / 2, btnY + 14, 0xFFCCE3EC);
            g.disableScissor();

            // 2. Botón Cultivo Murim (Centro)
            int cultX = left + 154;
            int cultW = 128;
            cultHovered = mouseX >= cultX && mouseX < cultX + cultW && mouseY >= btnY && mouseY < btnY + btnH;
            g.fill(cultX - 1, btnY - 1, cultX + cultW + 1, btnY + btnH + 1, cultHovered ? 0xFF50FA7B : 0xFF1E6E45);
            g.fill(cultX, btnY, cultX + cultW, btnY + btnH, cultHovered ? 0xDD123E28 : 0xDD0A2317);
            drawBorder(g, cultX, btnY, cultW, btnH, cultHovered ? 0xFF50FA7B : 0xFF1E6E45);

            Component realmName = Component.translatable("awakening_system.cultivation.realm." + d.dantianRealm());
            enableScissorScaled(g, cultX + 1, btnY + 1, cultX + cultW - 1, btnY + btnH - 1);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.btn.cultivation").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), cultX + cultW / 2, btnY + 4, 0xFF50FA7B);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation_sub", realmName, d.cultivationQi()), cultX + cultW / 2, btnY + 14, 0xFFCCE3EC);
            g.disableScissor();

            // 3. Botón Gremio de Cazadores (Derecha)
            int guildX = left + 288;
            int guildW = 132;
            boolean guildHovered = mouseX >= guildX && mouseX < guildX + guildW && mouseY >= btnY && mouseY < btnY + btnH;
            g.fill(guildX - 1, btnY - 1, guildX + guildW + 1, btnY + btnH + 1, guildHovered ? 0xFF70E8FF : 0xFF1E456E);
            g.fill(guildX, btnY, guildX + guildW, btnY + btnH, guildHovered ? 0xDD10283E : 0xDD0A1828);
            drawBorder(g, guildX, btnY, guildW, btnH, guildHovered ? 0xFF00E5FF : 0xFF4EE2EC);

            enableScissorScaled(g, guildX + 1, btnY + 1, guildX + guildW - 1, btnY + btnH - 1);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.btn.guild").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), guildX + guildW / 2, btnY + 4, 0xFF00E5FF);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.btn.guild_sub"), guildX + guildW / 2, btnY + 14, 0xFFCCE3EC);
            g.disableScissor();
        }

        if (classHovered) {
            List<Component> tooltip = new ArrayList<>();
            if (d.hasHunterClass()) {
                tooltip.add(Component.translatable("awakening_system.class." + d.hunterClass() + ".name").copy().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
                tooltip.add(Component.translatable("awakening_system.screen.class_tree_tip"));
                tooltip.add(Component.translatable("awakening_system.class.permanent_title").withStyle(ChatFormatting.DARK_GRAY));
            } else if (d.level() >= 30) {
                tooltip.add(Component.translatable("awakening_system.class.available_title").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable("awakening_system.class.level30_desc"));
                tooltip.add(Component.translatable("awakening_system.class.click_select"));
            } else {
                tooltip.add(Component.translatable("awakening_system.class.locked_title"));
                tooltip.add(Component.translatable("awakening_system.class.locked_desc"));
            }
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }

        if (titleHovered && equippedDef != null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable(equippedDef.translationKey()).withStyle(ChatFormatting.BOLD));
            tooltip.add(Component.translatable("awakening_system.rarity." + equippedDef.rarity().id()));
            tooltip.add(Component.translatable(equippedDef.bonusKey()).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable(equippedDef.descriptionKey()).withStyle(ChatFormatting.GRAY));
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }

        if (sponsorHovered) {
            List<Component> tooltip = new ArrayList<>();
            if (d.hasSponsor() && sponsorDef != null) {
                tooltip.add(sponsorDef.name().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE));
                tooltip.add(sponsorDef.title().copy().withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("awakening_system.screen.sponsor.stigma_label", sponsorDef.stigmaName()).withStyle(ChatFormatting.YELLOW));
                tooltip.add(sponsorDef.stigmaEffect().copy().withStyle(ChatFormatting.GREEN));
                if (d.level() >= 100) {
                    tooltip.add(Component.translatable("awakening_system.screen.sponsor.change_hint").withStyle(ChatFormatting.DARK_GRAY));
                }
            } else if (d.level() >= 100) {
                tooltip.add(Component.translatable("awakening_system.screen.sponsor.select_title").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
                tooltip.add(Component.translatable("awakening_system.screen.sponsor.select_desc").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("awakening_system.screen.sponsor.locked_title").withStyle(ChatFormatting.RED));
                tooltip.add(Component.translatable("awakening_system.screen.sponsor.locked_desc").withStyle(ChatFormatting.GRAY));
            }
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }

        if (constHovered) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("awakening_system.screen.constellation.channel_title").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            tooltip.add(Component.translatable("awakening_system.screen.constellation.channel_desc").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("awakening_system.screen.constellation.channel_action").withStyle(ChatFormatting.YELLOW));
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    private void renderPlayerModel(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        var player = Minecraft.getInstance().player;
        Component title = Component.translatable("awakening_system.screen.hunter_model");
        if (player != null) {
            SystemData d = data();
            if (d.isConstellation() && !d.constellationModifier().isEmpty()) {
                title = Component.literal("§d✦ ")
                        .append(ConstellationAscension.getTranslatedTitleComponent(d.constellationModifier()))
                        .append(Component.literal(" §7(" + player.getScoreboardName() + ")"));
            } else {
                title = Component.translatable("awakening_system.screen.hunter_model")
                        .append(Component.literal(": §f" + player.getScoreboardName()));
            }
        }
        section(g, left + 215, top + 152, 185, 112, title);
        if (player == null) return;

        int boxX1 = left + 217;
        int boxY1 = top + 174;
        int boxX2 = left + 398;
        int boxY2 = top + 262;

        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;
        // Fondo y marco holográfico para el avatar del jugador
        g.fill(boxX1, boxY1, boxX2, boxY2, sys30 ? 0x450A1914 : (sys20 ? 0x45160926 : 0x450C1925));
        int centerX = (boxX1 + boxX2) / 2;
        int feetY = boxY2 - 8;

        // Pedestal de energía bajo los pies
        g.fill(centerX - 36, feetY, centerX + 36, feetY + 5, sys30 ? 0x60163E2A : (sys20 ? 0x609D4EDD : 0x60276A82));
        g.fill(centerX - 30, feetY + 1, centerX + 30, feetY + 4, (sys30 || sys20) ? 0x80FFD700 : 0x8070E8FF);

        // Renderizado del modelo 3D del jugador o tarjeta simplificada según configuración
        if (com.soybe.awakeningsystem.client.gui.GuiConfig.showPlayer3DModel) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g, boxX1, boxY1 + 2, boxX2, boxY2 - 4, 38, 0.0625F,
                    (float) mouseX, (float) mouseY, player
            );
        } else {
            int cx = (boxX1 + boxX2) / 2;
            int cy = (boxY1 + boxY2) / 2;
            g.drawCenteredString(font, "§6✦ " + player.getScoreboardName(), cx, cy - 14, 0xFFDFB76C);
            g.drawCenteredString(font, "§7[Modelo 3D Desactivado]", cx, cy, 0x888888);
            g.drawCenteredString(font, "§8(Activar en ⚙ Config GUI)", cx, cy + 12, 0x666666);
        }

        // Botón interactivo de Visión Elemental (ubicado en la esquina superior del panel de avatar sin solapar la cabecera)
        SystemData dData = data();
        String actVis = dData != null ? dData.activeVision() : "NONE";
        int visColor = switch (actVis) {
            case "PYRO" -> 0xFFFF4422;
            case "HYDRO" -> 0xFF3399FF;
            case "CRYO" -> 0xFF77DDFF;
            case "ANEMO" -> 0xFF55FFAA;
            case "ELECTRO" -> 0xFFAA55FF;
            case "GEO" -> 0xFFFFBB33;
            case "DENDRO" -> 0xFF44CC33;
            default -> 0xFFAAAAAA;
        };
        int btnVx = left + 320;
        int btnVy = top + 178;
        int btnVw = 74;
        int btnVh = 16;
        boolean visHovered = mouseX >= btnVx && mouseX < btnVx + btnVw && mouseY >= btnVy && mouseY < btnVy + btnVh;
        g.fill(btnVx, btnVy, btnVx + btnVw, btnVy + btnVh, visHovered ? 0xDD2A3B4C : 0xDD0D1B26);
        drawBorder(g, btnVx, btnVy, btnVw, btnVh, visHovered ? 0xFFFFFFFF : visColor);

        // Icono elemental
        net.minecraft.resources.ResourceLocation vIcon = null;
        for (com.soybe.awakeningsystem.client.gui.VisionSelectionScreen.VisionEntry ve : com.soybe.awakeningsystem.client.gui.VisionSelectionScreen.VISIONS) {
            if (ve.id().equalsIgnoreCase(actVis)) {
                vIcon = ve.icon();
                break;
            }
        }
        if (vIcon != null) {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            g.blit(vIcon, btnVx + 3, btnVy + 1, 0, 0, 14, 14, 14, 14);
        }

        String label = "NONE".equals(actVis) ? "§7+ Visión" : ("§f" + actVis.substring(0, 1) + actVis.substring(1).toLowerCase());
        g.drawString(font, label, btnVx + 19, btnVy + 4, visColor);

        if (visHovered) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("§6✦ Visión Elemental de Teyvat").withStyle(ChatFormatting.BOLD));
            if ("NONE".equals(actVis)) {
                tooltip.add(Component.literal("§7No tienes ninguna resonancia elemental activa."));
            } else {
                tooltip.add(Component.literal("§aResonancia activa: §e" + actVis));
            }
            tooltip.add(Component.literal("§b▸ Clic para cambiar tu Visión Elemental").withStyle(ChatFormatting.AQUA));
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    private List<Map.Entry<String, Integer>> visibleSkills() {
        List<Map.Entry<String, Integer>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : data().skills().entrySet()) {
            if (SkillDefinitions.get(entry.getKey()) != null && SkillDefinitions.isAvailable(entry.getKey())) result.add(entry);
        }
        return result;
    }

    private List<Map.Entry<String, Integer>> visibleActiveSkills() {
        return visibleSkills().stream()
                .filter(e -> SkillDefinitions.isActive(e.getKey()))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();
    }

    private List<Map.Entry<String, Integer>> visiblePassiveSkills() {
        return visibleSkills().stream()
                .filter(e -> SkillDefinitions.isPassive(e.getKey()))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();
    }

    private List<Component> activeMissions(SystemData d) {
        List<Component> list = new ArrayList<>();
        long day = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getDayTime() / 24000L;
        long week = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getDayTime() / SystemData.WEEK_TICKS;

        String weeklyType = switch (d.weeklyType(week)) {
            case 1 -> "ores";
            case 2 -> "evolution";
            default -> "creatures";
        };
        list.add(Component.translatable("awakening_system.screen.missions.weekly." + weeklyType,
                d.weeklyProgress(week), d.weeklyTarget(week)));

        if (!d.dailyComplete(day)) {
            String dailyType = switch (d.dailyType(day)) {
                case 1 -> "combat";
                case 2 -> "crafting";
                case 3 -> "mobility";
                case 4 -> "depth";
                default -> "mining";
            };
            list.add(Component.translatable("awakening_system.screen.missions.daily." + dailyType,
                    d.dailyProgress(day), d.dailyTarget(day)));
        }
        if (!d.emergencyComplete()) {
            list.add(Component.translatable("awakening_system.screen.missions.emergency", d.emergencySurvival(), 3));
        }
        if (!d.evolutionMissionComplete()) {
            list.add(Component.translatable("awakening_system.screen.missions.evolution"));
        }
        if (!d.classMissionComplete()) {
            list.add(Component.translatable("awakening_system.screen.missions.class", d.classAffinity()));
        }
        if (d.nextRankLevel() != 0) {
            list.add(Component.translatable("awakening_system.screen.missions.rank", Integer.toString(d.nextRankLevel())));
        }
        if (!d.secretMissionComplete()) {
            list.add(Component.translatable("awakening_system.screen.missions.secret"));
        }
        if (d.isWelkinEnhanced()) {
            list.add(Component.translatable("awakening_system.screen.missions.welkin_enhanced_active"));
        } else if (d.welkinQuestStep() > 0) {
            String stepKey = switch (d.welkinQuestStep()) {
                case 1 -> "awakening_system.screen.missions.welkin_quest_step1";
                case 2 -> "awakening_system.screen.missions.welkin_quest_step2";
                case 3 -> "awakening_system.screen.missions.welkin_quest_step3";
                default -> "awakening_system.screen.missions.welkin_quest_ready";
            };
            list.add(Component.translatable(stepKey, d.welkinQuestProgress()));
        }
        return list;
    }

    private void renderSkills(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;
        int tabBg = sys30 ? 0xB50E1F1A : (sys20 ? 0xB5170B28 : 0xB50C1C2B);
        int tabLine = sys30 ? 0xFFD4AF37 : (sys20 ? 0xFF9D4EDD : EDGE);
        g.fill(left + 20, top + 78, left + W - 20, top + 264, tabBg);
        g.fill(left + 20, top + 97, left + W - 20, top + 98, tabLine);

        boolean hasClass = d.hasHunterClass();
        int subBtnW = hasClass ? ((W - 48) / 3) : ((W - 44) / 2);
        int btn1X = left + 20;
        int btn2X = left + 24 + subBtnW;
        int btn3X = left + 28 + subBtnW * 2;
        int btnY = top + 78;

        int btn1Bg = skillTabMode == 0 ? (sys30 ? 0xFF1C382E : (sys20 ? 0xFF3A1259 : 0xFF1E465D)) :
                (mouseX >= btn1X && mouseX < btn1X + subBtnW && mouseY >= btnY && mouseY < btnY + 20 ? (sys30 ? 0x601C382E : (sys20 ? 0x603A1259 : 0x6017354B)) : (sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF0D1E2C)));
        g.fill(btn1X, btnY, btn1X + subBtnW, btnY + 20, btn1Bg);
        boolean sel1 = skillTabMode == 0;
        boolean hov1 = mouseX >= btn1X && mouseX < btn1X + subBtnW && mouseY >= btnY && mouseY < btnY + 20;
        if (sel1) {
            int borderCol = (sys30 || sys20) ? 0xFFFFD700 : CYAN;
            g.fill(btn1X, btnY, btn1X + subBtnW, btnY + 2, borderCol);
            g.fill(btn1X, btnY + 18, btn1X + subBtnW, btnY + 20, borderCol);
        } else if (hov1) {
            g.fill(btn1X, btnY + 18, btn1X + subBtnW, btnY + 20, sys30 ? 0x8050FA7B : (sys20 ? 0x809D4EDD : 0x802D7691));
        }
        List<Map.Entry<String, Integer>> actives = visibleActiveSkills();
        Component labelActives = Component.literal("⚡ " + Component.translatable("awakening_system.screen.skills.active_header").getString() + " (" + actives.size() + ")");
        g.drawCenteredString(font, labelActives, btn1X + subBtnW / 2, btnY + 6, sel1 ? ((sys30 || sys20) ? 0xFFFFD700 : 0xFFFFFFFF) : (hov1 ? (sys30 ? 0xFF50FA7B : (sys20 ? 0xFFF2D6 : 0xFFDDECF2)) : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED))));

        boolean sel2 = skillTabMode == 1;
        boolean hov2 = mouseX >= btn2X && mouseX < btn2X + subBtnW && mouseY >= btnY && mouseY < btnY + 20;
        int btn2Bg = sel2 ? (sys30 ? 0xFF1C382E : (sys20 ? 0xFF3A1259 : 0xFF1E465D)) :
                (hov2 ? (sys30 ? 0x601C382E : (sys20 ? 0x603A1259 : 0x6017354B)) : (sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF0D1E2C)));
        g.fill(btn2X, btnY, btn2X + subBtnW, btnY + 20, btn2Bg);
        if (sel2) {
            int borderCol = (sys30 || sys20) ? 0xFFFFD700 : CYAN;
            g.fill(btn2X, btnY, btn2X + subBtnW, btnY + 2, borderCol);
            g.fill(btn2X, btnY + 18, btn2X + subBtnW, btnY + 20, borderCol);
        } else if (hov2) {
            g.fill(btn2X, btnY + 18, btn2X + subBtnW, btnY + 20, sys30 ? 0x8050FA7B : (sys20 ? 0x809D4EDD : 0x802D7691));
        }
        List<Map.Entry<String, Integer>> passives = visiblePassiveSkills();
        Component labelPassives = Component.literal("🛡 " + Component.translatable("awakening_system.screen.skills.passive_header").getString() + " (" + passives.size() + ")");
        g.drawCenteredString(font, labelPassives, btn2X + subBtnW / 2, btnY + 6, sel2 ? ((sys30 || sys20) ? 0xFFFFD700 : 0xFFFFFFFF) : (hov2 ? (sys30 ? 0xFF50FA7B : (sys20 ? 0xFFF2D6 : 0xFFDDECF2)) : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED))));

        if (hasClass) {
            boolean hov3 = mouseX >= btn3X && mouseX < btn3X + subBtnW && mouseY >= btnY && mouseY < btnY + 20;
            int btn3Bg = hov3 ? (sys30 ? 0x601C382E : (sys20 ? 0x603A1259 : 0x6017354B)) : (sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF0D1E2C));
            g.fill(btn3X, btnY, btn3X + subBtnW, btnY + 20, btn3Bg);
            if (hov3) {
                g.fill(btn3X, btnY + 18, btn3X + subBtnW, btnY + 20, 0xFFFFD700);
            }
            Component labelTree = Component.literal("🌿 " + Component.translatable("awakening_system.screen.skills.class_tree_btn").getString());
            g.drawCenteredString(font, labelTree, btn3X + subBtnW / 2, btnY + 6, hov3 ? 0xFFFFD700 : (sys30 ? 0xFF50FA7B : 0xFF00E5FF));
        }

        if (skillTabMode == 0) {
            int unlockedActives = d.getUnlockedActiveSkillSlots();
            if (selectedActiveSlot >= unlockedActives) {
                selectedActiveSlot = 0;
            }
            // 5 Ranuras Activas Superiores [V, R, G, B, X]
            for (int s = 0; s < 5; s++) {
                int slotX = left + 20 + s * 81;
                int slotY = top + 102;
                int slotW = 76;
                int slotH = 36;
                boolean isLocked = s >= unlockedActives;
                boolean isSelected = !isLocked && s == selectedActiveSlot;
                boolean slotHov = !isLocked && mouseX >= slotX && mouseX < slotX + slotW && mouseY >= slotY && mouseY < slotY + slotH;

                if (isLocked) {
                    g.fill(slotX, slotY, slotX + slotW, slotY + slotH, 0x40101010);
                    drawBorder(g, slotX, slotY, slotW, slotH, 0xFF333333);
                    g.fill(slotX, slotY, slotX + slotW, slotY + 2, 0xFF444444);

                    // Tecla bloqueada
                    g.fill(slotX + 3, slotY + 4, slotX + 17, slotY + 16, 0xFF181818);
                    drawBorder(g, slotX + 3, slotY + 4, 14, 12, 0xFF444444);
                    g.drawCenteredString(font, Component.literal("🔒"), slotX + 10, slotY + 6, 0xFF888888);

                    g.drawString(font, Component.translatable("awakening_system.screen.locked"), slotX + 20, slotY + 6, 0xFF666666);
                    g.drawString(font, Component.translatable("awakening_system.screen.class_tree_tag_short"), slotX + 20, slotY + 20, 0xFF555555);
                } else {
                    int slotBg = isSelected ? (sys30 ? 0x801C382E : (sys20 ? 0x803A1259 : 0x80183F57)) : (slotHov ? (sys30 ? 0x601C382E : (sys20 ? 0x603A1259 : 0x60183F57)) : (sys30 ? 0x400E261E : (sys20 ? 0x40160926 : 0x400C1925)));
                    g.fill(slotX, slotY, slotX + slotW, slotY + slotH, slotBg);
                    drawBorder(g, slotX, slotY, slotW, slotH, isSelected ? 0xFF50FA7B : (slotHov ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : (sys30 ? 0xFFD4AF37 : (sys20 ? 0xFF9D4EDD : 0xFF2D7691))));
                    g.fill(slotX, slotY, slotX + slotW, slotY + 2, isSelected ? 0xFF50FA7B : ((sys30 || sys20) ? 0xFFFFD700 : CYAN));

                    // Tecla
                    g.fill(slotX + 3, slotY + 4, slotX + 17, slotY + 16, sys30 ? 0xFF142E24 : (sys20 ? 0xFF241038 : 0xFF102536));
                    drawBorder(g, slotX + 3, slotY + 4, 14, 12, isSelected ? 0xFF50FA7B : ((sys30 || sys20) ? 0xFFFFD700 : CYAN));
                    g.drawCenteredString(font, Component.literal(ACTIVE_SLOT_KEYS[s]).withStyle(ChatFormatting.BOLD), slotX + 10, slotY + 6, isSelected ? 0xFF50FA7B : ((sys30 || sys20) ? 0xFFFFD700 : CYAN));

                    String eq = d.equippedActiveSkill(s);
                    if (!eq.isEmpty() && SkillDefinitions.get(eq) != null) {
                        SkillDefinitions.Definition eqDef = SkillDefinitions.get(eq);
                        int eqLvl = d.skills().getOrDefault(eq, 1);
                        String name = eqDef.name().getString();
                        if (font.width(name) > 42) name = font.plainSubstrByWidth(name, 38) + "..";
                        g.drawString(font, Component.literal(name), slotX + 20, slotY + 6, rarityColor(eqDef.rarity()));
                        g.drawString(font, Component.translatable("awakening_system.screen.skills.level_short", eqLvl), slotX + 6, slotY + 22, MUTED);
                        g.drawString(font, Component.literal("§c✕"), slotX + slotW - 10, slotY + 5, 0xFFFF7666);
                    } else {
                        g.drawString(font, Component.translatable("awakening_system.screen.skills.slot_empty"), slotX + 22, slotY + 6, 0xFF888888);
                        g.drawString(font, Component.translatable("awakening_system.screen.skills.slot_index", s + 1), slotX + 6, slotY + 22, 0xFF666666);
                    }
                }
            }

            // Lista Scrolleable de Habilidades Activas
            enableScissorScaled(g, left + 20, top + 144, left + W - 20, top + 264);
            if (actives.isEmpty()) {
                g.drawCenteredString(font, Component.translatable("awakening_system.screen.skills.no_actives"), left + W / 2, top + 180, 0xFFFF8080);
                g.drawCenteredString(font, Component.translatable("awakening_system.screen.skills.no_actives_hint"), left + W / 2, top + 196, MUTED);
            } else {
                for (int i = 0; i < actives.size(); i++) {
                    int itemY = top + 144 + (i - activeSkillScroll) * 38;
                    if (itemY < top + 140 || itemY >= top + 264) continue;
                    Map.Entry<String, Integer> entry = actives.get(i);
                    SkillDefinitions.Definition def = SkillDefinitions.get(entry.getKey());
                    if (def == null) continue;

                    int eqSlot = -1;
                    for (int s = 0; s < unlockedActives; s++) {
                        if (entry.getKey().equals(d.equippedActiveSkill(s))) {
                            eqSlot = s;
                            break;
                        }
                    }
                    boolean isEq = eqSlot >= 0;
                    boolean itemHov = mouseX >= left + 20 && mouseX < left + W - 20 && mouseY >= itemY && mouseY < itemY + 34;

                    int rowBg = sys30 ? (itemHov ? 0x501C382E : 0x300E261E) : (itemHov ? (sys20 ? 0x503A1259 : 0x501C465D) : (sys20 ? 0x30160926 : 0x300C1925));
                    g.fill(left + 20, itemY, left + W - 20, itemY + 34, rowBg);
                    int rowBorder = isEq ? 0x8050FA7B : (itemHov ? ((sys30 || sys20) ? 0x80FFD700 : 0x6070E8FF) : (sys30 ? 0x40D4AF37 : (sys20 ? 0x359D4EDD : 0x352D7691)));
                    drawBorder(g, left + 20, itemY, W - 40, 34, rowBorder);

                    // Tira lateral con color de rareza
                    g.fill(left + 20, itemY, left + 23, itemY + 34, rarityColor(def.rarity()));

                    // Fila 1: Nombre + Nivel + Rareza
                    Component nameComp = def.name().copy().append(Component.translatable("awakening_system.screen.skills.level_tag", entry.getValue()));
                    drawClipped(g, nameComp, left + 28, itemY + 5, itemHov ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : rarityColor(def.rarity()), W - 200);

                    // Fila 2: Efecto y Coste EP
                    drawClipped(g, Component.translatable("awakening_system.screen.skills.ep_cost", def.effect()), left + 28, itemY + 19, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED), W - 200);

                    // Botones en la derecha
                    if (isEq) {
                        int bX = left + W - 150;
                        int bY = itemY + 8;
                        boolean bHov = mouseX >= bX && mouseX < bX + 85 && mouseY >= bY && mouseY < bY + 18;
                        g.fill(bX, bY, bX + 85, bY + 18, bHov ? (sys30 ? 0xFF1C382E : (sys20 ? 0xFF3A1259 : 0xFF276A82)) : 0xFF163E2A);
                        drawBorder(g, bX, bY, 85, 18, bHov ? 0xFFFFFFFF : 0xFF50FA7B);
                        g.drawCenteredString(font, Component.translatable("awakening_system.screen.skills.in_slot", ACTIVE_SLOT_KEYS[eqSlot]), bX + 42, bY + 5, 0xFF50FA7B);
                    } else {
                        int bX = left + W - 150;
                        int bY = itemY + 8;
                        boolean bHov = mouseX >= bX && mouseX < bX + 85 && mouseY >= bY && mouseY < bY + 18;
                        g.fill(bX, bY, bX + 85, bY + 18, bHov ? (sys30 ? 0xFF2ECC71 : (sys20 ? 0xFFFFD700 : 0xFF70E8FF)) : (sys30 ? 0xFF163E2A : (sys20 ? 0xFF8A2BE2 : 0xFF276A82)));
                        drawBorder(g, bX, bY, 85, 18, bHov ? 0xFFFFFFFF : ((sys30 || sys20) ? 0xFFFFD700 : 0xFF50B4D8));
                        g.drawCenteredString(font, Component.translatable("awakening_system.screen.skills.equip_slot", ACTIVE_SLOT_KEYS[selectedActiveSlot]), bX + 42, bY + 5, bHov ? (sys30 ? 0xFFFFFFFF : 0xFF10202D) : (sys30 ? 0xFFFFD700 : 0xFFFFFFFF));
                    }

                    if (entry.getValue() == 5) {
                        int evX = left + W - 60;
                        int evY = itemY + 8;
                        boolean evHov = mouseX >= evX && mouseX < evX + 45 && mouseY >= evY && mouseY < evY + 18;
                        g.fill(evX, evY, evX + 45, evY + 18, evHov ? 0xFFFFD866 : 0xFFB8860B);
                        drawBorder(g, evX, evY, 45, 18, evHov ? 0xFFFFFFFF : 0xFFFFD866);
                        g.drawCenteredString(font, Component.translatable("awakening_system.screen.skills.evol_short_btn"), evX + 22, evY + 5, evHov ? 0xFF10202D : 0xFFFFFFFF);
                    }
                }
            }
            g.disableScissor();

            if (actives.size() > 3) {
                int trackTop = top + 144;
                int trackBottom = top + 258;
                int thumbHeight = Math.max(14, (trackBottom - trackTop) * 3 / actives.size());
                int thumbRange = trackBottom - trackTop - thumbHeight;
                int thumbY = trackTop + thumbRange * activeSkillScroll / (actives.size() - 3);
                g.fill(left + W - 18, trackTop, left + W - 15, trackBottom, sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF0C1925));
                g.fill(left + W - 18, thumbY, left + W - 15, thumbY + thumbHeight, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
            }
        } else {
            int unlockedPassives = d.getUnlockedPassiveSkillSlots();
            // 3 Ranuras de Talentos Pasivos Superiores
            for (int s = 0; s < 3; s++) {
                int slotX = left + 20 + s * 136;
                int slotY = top + 102;
                int slotW = 128;
                int slotH = 32;
                boolean isLocked = s >= unlockedPassives;
                boolean slotHov = !isLocked && mouseX >= slotX && mouseX < slotX + slotW && mouseY >= slotY && mouseY < slotY + slotH;

                if (isLocked) {
                    g.fill(slotX, slotY, slotX + slotW, slotY + slotH, 0x40101010);
                    drawBorder(g, slotX, slotY, slotW, slotH, 0xFF333333);
                    g.fill(slotX, slotY, slotX + slotW, slotY + 2, 0xFF444444);
                    g.drawString(font, Component.literal("🔒 §8" + Component.translatable("awakening_system.screen.skills.talent_slot", s + 1).getString()), slotX + 6, slotY + 4, 0xFF666666);
                    g.drawString(font, Component.translatable("awakening_system.screen.skills.locked_class_tree"), slotX + 6, slotY + 17, 0xFF555555);
                } else {
                    int pSlotBg = slotHov ? (sys30 ? 0x601C382E : (sys20 ? 0x603A1259 : 0x60183F57)) : (sys30 ? 0x400E261E : (sys20 ? 0x40160926 : 0x400C1925));
                    g.fill(slotX, slotY, slotX + slotW, slotY + slotH, pSlotBg);
                    drawBorder(g, slotX, slotY, slotW, slotH, slotHov ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : (sys30 ? 0xFFD4AF37 : (sys20 ? 0xFF9D4EDD : 0xFF2D7691)));
                    g.drawString(font, Component.translatable("awakening_system.screen.skills.talent_slot", s + 1), slotX + 6, slotY + 4, (sys30 || sys20) ? 0xFFFFD700 : CYAN);

                    String eq = d.equippedPassiveSkill(s);
                    if (!eq.isEmpty() && SkillDefinitions.get(eq) != null) {
                        SkillDefinitions.Definition eqDef = SkillDefinitions.get(eq);
                        int eqLvl = d.skills().getOrDefault(eq, 1);
                        String name = eqDef.name().getString() + " " + Component.translatable("awakening_system.screen.skills.level_short", eqLvl).getString();
                        if (font.width(name) > 94) name = font.plainSubstrByWidth(name, 88) + "..";
                        g.drawString(font, Component.literal(name), slotX + 6, slotY + 17, rarityColor(eqDef.rarity()));
                        g.drawString(font, Component.literal("§c✕"), slotX + slotW - 12, slotY + 4, 0xFFFF7666);
                    } else {
                        g.drawString(font, Component.translatable("awakening_system.screen.skills.empty_slot"), slotX + 6, slotY + 17, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));
                    }
                }
            }

            // Lista Scrolleable de Habilidades Pasivas
            enableScissorScaled(g, left + 20, top + 138, left + W - 20, top + 264);
            if (passives.isEmpty()) {
                g.drawCenteredString(font, Component.translatable("awakening_system.screen.skills.no_passives"), left + W / 2, top + 180, 0xFFFF8080);
                g.drawCenteredString(font, Component.translatable("awakening_system.screen.skills.no_passives_hint"), left + W / 2, top + 196, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));
            } else {
                for (int i = 0; i < passives.size(); i++) {
                    int itemY = top + 138 + (i - passiveSkillScroll) * 38;
                    if (itemY < top + 134 || itemY >= top + 264) continue;
                    Map.Entry<String, Integer> entry = passives.get(i);
                    SkillDefinitions.Definition def = SkillDefinitions.get(entry.getKey());
                    if (def == null) continue;

                    int eqSlot = -1;
                    for (int s = 0; s < unlockedPassives; s++) {
                        if (entry.getKey().equals(d.equippedPassiveSkill(s))) {
                            eqSlot = s;
                            break;
                        }
                    }
                    boolean isEq = eqSlot >= 0;
                    boolean itemHov = mouseX >= left + 20 && mouseX < left + W - 20 && mouseY >= itemY && mouseY < itemY + 34;

                    int pRowBg = sys30 ? (itemHov ? 0x501C382E : 0x300E261E) : (itemHov ? (sys20 ? 0x503A1259 : 0x501C465D) : (sys20 ? 0x30160926 : 0x300C1925));
                    g.fill(left + 20, itemY, left + W - 20, itemY + 34, pRowBg);
                    int pBorderCol = isEq ? 0x8050FA7B : (itemHov ? ((sys30 || sys20) ? 0x80FFD700 : 0x6070E8FF) : (sys30 ? 0x40D4AF37 : (sys20 ? 0x359D4EDD : 0x352D7691)));
                    drawBorder(g, left + 20, itemY, W - 40, 34, pBorderCol);

                    // Tira lateral con color de rareza
                    g.fill(left + 20, itemY, left + 23, itemY + 34, rarityColor(def.rarity()));

                    // Fila 1: Nombre + Nivel + Progreso
                    int curProg = d.skillProgress(entry.getKey());
                    int reqProg = d.skillProgressRequired(entry.getKey());
                    Component nameComp = def.name().copy().append(Component.translatable("awakening_system.screen.skills.level_tag", entry.getValue())).append(" §8(" + curProg + "/" + reqProg + ")");
                    drawClipped(g, nameComp, left + 28, itemY + 5, itemHov ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : rarityColor(def.rarity()), W - 180);

                    // Fila 2: Efecto Pasivo
                    drawClipped(g, def.effect(), left + 28, itemY + 19, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED), W - 180);

                    // Insignia de Pasiva Activa / Apagada y Evolución
                    int toggleX = entry.getValue() == 5 ? left + W - 165 : left + W - 90;
                    int toggleW = 70;
                    int toggleY = itemY + 8;
                    boolean togHov = mouseX >= toggleX && mouseX < toggleX + toggleW && mouseY >= toggleY && mouseY < toggleY + 18;

                    if (isEq) {
                        g.fill(toggleX, toggleY, toggleX + toggleW, toggleY + 18, togHov ? (sys30 ? 0xFF1C382E : (sys20 ? 0xFF3A1259 : 0xFF276A82)) : 0xFF163E2A);
                        drawBorder(g, toggleX, toggleY, toggleW, 18, togHov ? 0xFFFFFFFF : 0xFF50FA7B);
                        g.drawCenteredString(font, Component.translatable("awakening_system.screen.skills.talent_equipped", eqSlot + 1), toggleX + toggleW / 2, toggleY + 5, 0xFF50FA7B);
                    } else {
                        g.fill(toggleX, toggleY, toggleX + toggleW, toggleY + 18, togHov ? (sys30 ? 0xFF2ECC71 : (sys20 ? 0xFFFFD700 : 0xFF70E8FF)) : (sys30 ? 0xFF163E2A : (sys20 ? 0xFF8A2BE2 : 0xFF276A82)));
                        drawBorder(g, toggleX, toggleY, toggleW, 18, togHov ? 0xFFFFFFFF : ((sys30 || sys20) ? 0xFFFFD700 : 0xFF50B4D8));
                        g.drawCenteredString(font, Component.translatable("awakening_system.screen.skills.equip"), toggleX + toggleW / 2, toggleY + 5, togHov ? (sys30 ? 0xFFFFFFFF : 0xFF10202D) : (sys30 ? 0xFFFFD700 : 0xFFFFFFFF));
                    }

                    if (entry.getValue() == 5) {
                        int evX = left + W - 90;
                        int evY = itemY + 8;
                        boolean evHov = mouseX >= evX && mouseX < evX + 75 && mouseY >= evY && mouseY < evY + 18;
                        g.fill(evX, evY, evX + 75, evY + 18, evHov ? 0xFFFFD866 : 0xFFB8860B);
                        drawBorder(g, evX, evY, 75, 18, evHov ? 0xFFFFFFFF : 0xFFFFD866);
                        g.drawCenteredString(font, Component.translatable("awakening_system.screen.skills.evol_btn"), evX + 37, evY + 5, evHov ? 0xFF10202D : 0xFFFFFFFF);
                    }
                }
            }
            g.disableScissor();

            if (passives.size() > 3) {
                int trackTop = top + 138;
                int trackBottom = top + 258;
                int thumbHeight = Math.max(14, (trackBottom - trackTop) * 3 / passives.size());
                int thumbRange = trackBottom - trackTop - thumbHeight;
                int thumbY = trackTop + thumbRange * passiveSkillScroll / (passives.size() - 3);
                g.fill(left + W - 18, trackTop, left + W - 15, trackBottom, sys30 ? 0xFF0A1815 : (sys20 ? 0xFF140822 : 0xFF0C1925));
                g.fill(left + W - 18, thumbY, left + W - 15, thumbY + thumbHeight, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
            }
        }
    }

    private void renderMissions(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        section(g, left + 20, top + 78, W - 40, 186, Component.translatable("awakening_system.screen.tab.missions"));
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;

        if (!d.awakeningComplete()) {
            int startY = top + 108;
            g.drawString(font, Component.translatable("awakening_system.screen.missions.awakening").copy().withStyle(ChatFormatting.BOLD), left + 34, startY, (sys30 || sys20) ? 0xFFFFD700 : 0xFF70E8FF);

            // Minería
            Component mineText = Component.translatable("awakening_system.screen.missions.mine", d.awakeningMining(), 16);
            drawClipped(g, mineText, left + 34, startY + 20, 0xFFE0E0E0, W - 68);
            int barW = W - 68;
            g.fill(left + 34, startY + 34, left + 34 + barW, startY + 42, sys30 ? 0xFF0E261E : (sys20 ? 0xFF160926 : 0xFF0B141D));
            drawBorder(g, left + 34, startY + 34, barW, 8, sys30 ? 0x50D4AF37 : (sys20 ? 0x509D4EDD : 0x502D7691));
            int mineFill = (int) (barW * Math.min(1.0F, (float) d.awakeningMining() / 16.0F));
            if (mineFill > 0) {
                g.fill(left + 34, startY + 34, left + 34 + mineFill, startY + 42, 0xFF50FA7B);
            }

            // Combate
            Component combatText = Component.translatable("awakening_system.screen.missions.combat", d.awakeningCombat(), 5);
            drawClipped(g, combatText, left + 34, startY + 54, 0xFFE0E0E0, W - 68);
            g.fill(left + 34, startY + 68, left + 34 + barW, startY + 76, sys30 ? 0xFF0E261E : (sys20 ? 0xFF160926 : 0xFF0B141D));
            drawBorder(g, left + 34, startY + 68, barW, 8, sys30 ? 0x50D4AF37 : (sys20 ? 0x509D4EDD : 0x502D7691));
            int combatFill = (int) (barW * Math.min(1.0F, (float) d.awakeningCombat() / 5.0F));
            if (combatFill > 0) {
                g.fill(left + 34, startY + 68, left + 34 + combatFill, startY + 76, 0xFFFF5555);
            }

            // Recompensa
            drawClipped(g, Component.translatable("awakening_system.screen.missions.reward"), left + 34, startY + 98, 0xFFFFD866, W - 68);
            drawClipped(g, Component.translatable("awakening_system.screen.progress.quest_hint"), left + 34, startY + 114, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED), W - 68);
            return;
        }

        // Tarjeta de Escenario Mundial Dinamico (ORV)
        int cardX = left + 28;
        int cardY = top + 104;
        int cardW = W - 56;
        int cardH = 46;
        g.fill(cardX, cardY, cardX + cardW, cardY + cardH, sys30 ? 0x500E261E : (sys20 ? 0x50160926 : 0x500C1925));
        g.fill(cardX, cardY, cardX + cardW, cardY + 1, (sys30 || sys20) ? 0x80FFD700 : 0x8070E8FF);
        g.fill(cardX, cardY + cardH - 1, cardX + cardW, cardY + cardH, sys30 ? 0x40163E2A : (sys20 ? 0x409D4EDD : 0x402D7691));

        String scenId = d.activeScenario();
        if (!net.minecraft.client.resources.language.I18n.exists("awakening_system.scenario." + scenId + ".name")) {
            scenId = "night_hunt";
        }
        Component scenName = Component.translatable("awakening_system.scenario." + scenId + ".name");
        Component scenDesc = Component.translatable("awakening_system.scenario." + scenId + ".desc");

        drawClipped(g, Component.translatable("awakening_system.screen.missions.scenario_header", scenName), cardX + 6, cardY + 4, 0xFFFFD866, cardW - 12);
        drawClipped(g, scenDesc, cardX + 6, cardY + 16, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED), cardW - 12);

        // Barra de progreso del escenario
        int barX = cardX + 6;
        int barY = cardY + 28;
        int barW = cardW - 120;
        int barH = 10;
        float progressRatio = Math.max(0.0F, Math.min(1.0F, (float) d.scenarioProgress() / d.scenarioTarget()));
        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF050B10);
        g.fill(barX, barY, barX + barW, barY + barH, sys30 ? 0xFF0E261E : (sys20 ? 0xFF28103F : 0xFF17354B));
        int filledW = (int) (barW * progressRatio);
        if (filledW > 0) {
            g.fill(barX, barY, barX + filledW, barY + barH, d.isScenarioComplete() ? 0xFF50FA7B : (sys30 ? 0xFF1B7A4E : (sys20 ? 0xFF8A2BE2 : 0xFF276A82)));
            g.fill(barX, barY, barX + filledW, barY + 2, d.isScenarioComplete() ? 0xFF70FF9A : ((sys30 || sys20) ? 0xFFFFD700 : 0xFF70E8FF));
        }
        String progressText = d.scenarioProgress() + " / " + d.scenarioTarget();
        g.drawCenteredString(font, Component.literal(progressText), barX + barW / 2, barY + 1, 0xFFFFFFFF);

        // Boton de reclamo o estado
        if (d.isScenarioComplete()) {
            int btnX = cardX + cardW - 108;
            int btnY = cardY + 25;
            int btnW = 102;
            int btnH = 16;
            boolean hovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hovered ? 0xFF50FA7B : (sys30 ? 0xFF1B7A4E : (sys20 ? 0xFF8A2BE2 : 0xFF276A82)));
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.missions.claim_btn"), btnX + btnW / 2, btnY + 4, hovered ? 0xFF10202D : 0xFFFFFFFF);
        } else {
            drawClipped(g, Component.translatable("awakening_system.screen.missions.reward_summary"), cardX + cardW - 108, cardY + 29, 0xFFFFD866, 102);
        }

        // Lista scrolleable de misiones diarias, semanales, emergencia
        List<Component> missions = activeMissions(d);

        enableScissorScaled(g, left + 28, top + 154, left + W - 28, top + 260);
        int line = top + 156 - missionScroll * 26;
        for (int i = 0; i < missions.size(); i++) {
            if (line >= top + 146 && line < top + 258) {
                boolean hovered = mouseX >= left + 30 && mouseX < left + W - 30 && mouseY >= line && mouseY < line + 24;
                if (hovered) g.fill(left + 30, line, left + W - 30, line + 24, sys30 ? 0x30163E2A : (sys20 ? 0x303A1259 : 0x301C465D));
                drawClipped(g, missions.get(i), left + 34, line + 4, i == 0 ? ((sys30 || sys20) ? 0xFFFFD700 : 0xFF8BE9FD) : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED)), W - 76);
            }
            line += 26;
        }
        g.disableScissor();

        if (missions.size() > 4) {
            int trackTop = top + 156;
            int trackBottom = top + 256;
            int thumbHeight = Math.max(14, (trackBottom - trackTop) * 4 / missions.size());
            int thumbRange = trackBottom - trackTop - thumbHeight;
            int thumbY = trackTop + thumbRange * missionScroll / (missions.size() - 4);
            g.fill(left + W - 18, trackTop, left + W - 15, trackBottom, sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF0C1925));
            g.fill(left + W - 18, thumbY, left + W - 15, thumbY + thumbHeight, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
        }
    }

    private void renderTitles(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        section(g, left + 20, top + 78, W - 40, 186, Component.translatable("awakening_system.screen.tab.titles"));
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;
        drawClipped(g, Component.translatable("awakening_system.screen.titles.equipped",
                Component.translatable(TitleDefinitions.get(d.equippedTitle()).translationKey())), left + 34, top + 102, sys30 ? 0xFFFFD700 : (sys20 ? 0xFFFFE4A0 : 0xFFE4A7FF), W - 170);

        // Botón rápido para ver Fábulas
        int fabBtnX = left + W - 150;
        int fabBtnY = top + 96;
        int fabBtnW = 122;
        int fabBtnH = 18;
        boolean fabHov = mouseX >= fabBtnX && mouseX < fabBtnX + fabBtnW && mouseY >= fabBtnY && mouseY < fabBtnY + fabBtnH;
        g.fill(fabBtnX, fabBtnY, fabBtnX + fabBtnW, fabBtnY + fabBtnH, fabHov ? 0xFF0D3442 : 0xFF08202B);
        drawBorder(g, fabBtnX, fabBtnY, fabBtnW, fabBtnH, fabHov ? 0xFFFFD700 : 0xFF00838F);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.titles.fables_btn").withStyle(ChatFormatting.BOLD), fabBtnX + fabBtnW / 2, fabBtnY + 5, fabHov ? 0xFFFFD700 : 0xFF00E5FF);

        List<TitleDefinitions.Definition> titles = TitleDefinitions.availableDefinitions(d);
        TitleDefinitions.Definition hoveredTitle = null;

        enableScissorScaled(g, left + 28, top + 118, left + W - 28, top + 260);
        int line = top + 120 - titleScroll * 44;
        for (TitleDefinitions.Definition title : titles) {
            if (line >= top + 116 && line < top + 256) {
                boolean equipped = title.id().equals(d.equippedTitle());
                boolean hovered = mouseX >= left + 30 && mouseX < left + W - 30 && mouseY >= line && mouseY < line + 40;
                if (hovered) {
                    g.fill(left + 30, line, left + W - 30, line + 40, sys30 ? 0x35163E2A : (sys20 ? 0x353A1259 : 0x351C465D));
                    hoveredTitle = title;
                }

                // Banda lateral con el color de rareza del título
                g.fill(left + 30, line, left + 33, line + 40, rarityColor(title.rarity()));

                Component rarityComp = Component.translatable("awakening_system.rarity." + title.rarity().id());
                int rarityW = font.width(rarityComp);
                int rarityX = left + W - 62 - (equipped ? 52 : 0) - rarityW;
                g.drawString(font, rarityComp, rarityX, line + 4, rarityColor(title.rarity()));

                Component label = Component.translatable(title.translationKey());
                int maxLabelW = Math.max(80, rarityX - left - 44);
                drawClipped(g, label, left + 38, line + 4, equipped ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : rarityColor(title.rarity()), maxLabelW);

                if (equipped) {
                    int eqX = left + W - 80;
                    g.fill(eqX, line + 3, eqX + 44, line + 14, 0xFF1D5A40);
                    g.drawCenteredString(font, Component.translatable("awakening_system.screen.titles.equipped_short"), eqX + 22, line + 4, 0xFF50FA7B);
                }

                drawClipped(g, Component.translatable(title.bonusKey()), left + 38, line + 16, 0xFF50FA7B, 330);
                drawClipped(g, Component.translatable(title.descriptionKey()), left + 38, line + 27, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED), 330);
            }
            line += 44;
        }
        g.disableScissor();

        if (hoveredTitle != null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable(hoveredTitle.translationKey()).withStyle(ChatFormatting.BOLD));
            tooltip.add(Component.translatable("awakening_system.rarity." + hoveredTitle.rarity().id()));
            tooltip.add(Component.translatable(hoveredTitle.bonusKey()).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable(hoveredTitle.descriptionKey()).withStyle(ChatFormatting.GRAY));
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }

        if (titles.size() > 3) {
            int trackTop = top + 120;
            int trackBottom = top + 256;
            int thumbHeight = Math.max(14, (trackBottom - trackTop) * 3 / titles.size());
            int thumbRange = trackBottom - trackTop - thumbHeight;
            int thumbY = trackTop + thumbRange * titleScroll / (titles.size() - 3);
            g.fill(left + W - 18, trackTop, left + W - 15, trackBottom, sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF0C1925));
            g.fill(left + W - 18, thumbY, left + W - 15, thumbY + thumbHeight, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
        }

        if (titles.isEmpty()) g.drawString(font, Component.translatable("awakening_system.screen.titles.none"), left + 34, top + 135, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));
    }

    private void renderShop(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        section(g, left + 20, top + 78, W - 40, 186, Component.empty());

        // Etiqueta compacta de sección
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;
        g.drawString(font, Component.translatable("awakening_system.screen.shop.header"), left + 22, top + 84, (sys30 || sys20) ? 0xFFFFD700 : CYAN);

        // Alternador de modo COMPRAR / VENDER / RULETA / BANCO / DESEOS (ancho y posiciones anti-solapamiento)
        int buyX = left + 62;
        int buyW = 46;
        boolean buyHovered = mouseX >= buyX && mouseX < buyX + buyW && mouseY >= top + 80 && mouseY < top + 96;

        int sellX = left + 111;
        int sellW = 40;
        boolean sellHovered = mouseX >= sellX && mouseX < sellX + sellW && mouseY >= top + 80 && mouseY < top + 96;

        int rouX = left + 154;
        int rouW = 48;
        boolean rouletteHovered = mouseX >= rouX && mouseX < rouX + rouW && mouseY >= top + 80 && mouseY < top + 96;

        int bankX = left + 205;
        int bankW = 44;
        boolean bankHovered = mouseX >= bankX && mouseX < bankX + bankW && mouseY >= top + 80 && mouseY < top + 96;

        int gachaX = left + 252;
        int gachaW = 50;
        boolean gachaHovered = mouseX >= gachaX && mouseX < gachaX + gachaW && mouseY >= top + 80 && mouseY < top + 96;

        // COMPRAR
        int buyBg = shopMode == 0 ? (sys30 ? 0xFF1C382E : (sys20 ? 0xFF3A1259 : 0xFF1B4357)) : (buyHovered ? (sys30 ? 0x601C382E : (sys20 ? 0x603A1259 : 0x6017354B)) : (sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF400C1C)));
        g.fill(buyX, top + 80, buyX + buyW, top + 96, buyBg);
        drawBorder(g, buyX, top + 80, buyW, 16, shopMode == 0 ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : (sys30 ? 0x40D4AF37 : (sys20 ? 0x409D4EDD : 0x402D7691)));
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.shop.mode.buy"), buyX + buyW / 2, top + 84,
                shopMode == 0 ? ((sys30 || sys20) ? 0xFFFFD700 : 0xFFFFFFFF) : (buyHovered ? (sys30 ? 0xFF50FA7B : (sys20 ? 0xFFF2D6 : 0xFFDDECF2)) : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED))));

        // VENDER
        g.fill(sellX, top + 80, sellX + sellW, top + 96, shopMode == 1 ? 0xFF1D5A40 : (sellHovered ? (sys30 ? 0x601C382E : (sys20 ? 0x603A1259 : 0x6017354B)) : (sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF400C1C))));
        drawBorder(g, sellX, top + 80, sellW, 16, shopMode == 1 ? 0xFF50FA7B : (sys30 ? 0x40D4AF37 : (sys20 ? 0x409D4EDD : 0x402D7691)));
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.shop.mode.sell"), sellX + sellW / 2, top + 84,
                shopMode == 1 ? 0xFFFFFFFF : (sellHovered ? (sys30 ? 0xFF50FA7B : (sys20 ? 0xFFF2D6 : 0xFFDDECF2)) : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED))));

        // RULETA
        g.fill(rouX, top + 80, rouX + rouW, top + 96, shopMode == 2 ? 0xFF5D4017 : (rouletteHovered ? 0x605D4017 : (sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF400C1C))));
        drawBorder(g, rouX, top + 80, rouW, 16, shopMode == 2 ? 0xFFFFD866 : (sys30 ? 0x40D4AF37 : (sys20 ? 0x409D4EDD : 0x402D7691)));
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.shop.mode.roulette"), rouX + rouW / 2, top + 84,
                shopMode == 2 ? 0xFFFFD866 : (rouletteHovered ? 0xFFFFE899 : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED))));

        // BANCO
        g.fill(bankX, top + 80, bankX + bankW, top + 96, shopMode == 3 ? 0xFF352B5A : (bankHovered ? 0x60352B5A : (sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF400C1C))));
        drawBorder(g, bankX, top + 80, bankW, 16, shopMode == 3 ? 0xFFBD93F9 : (sys30 ? 0x40D4AF37 : (sys20 ? 0x409D4EDD : 0x402D7691)));
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.shop.mode.bank"), bankX + bankW / 2, top + 84,
                shopMode == 3 ? 0xFFBD93F9 : (bankHovered ? 0xFFE0D0FF : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED))));

        boolean sys40 = isSys40();

        // DESEOS (Gacha - Exclusivo de Sistema 4.0 / Teyvat)
        if (sys40) {
            g.fill(gachaX, top + 80, gachaX + gachaW, top + 96, gachaHovered ? 0xFF5D4017 : 0xFF3D2A0F);
            drawBorder(g, gachaX, top + 80, gachaW, 16, gachaHovered ? 0xFFFFFFFF : 0xFFFFD700);
            g.drawCenteredString(font, Component.literal("§6★ §eDESEOS"), gachaX + gachaW / 2, top + 84, gachaHovered ? 0xFFFFFFFF : 0xFFFFD700);
        }

        // Saldo de monedas en formato limpio a la derecha (anti-solapamiento garantizado)
        Component balance = Component.translatable("awakening_system.screen.shop.balance_short", d.coins());
        int balW = font.width(balance);
        int balX = left + W - 24 - balW;
        int minBalX = sys40 ? (gachaX + gachaW + 8) : (bankX + bankW + 8);
        if (balX < minBalX) balX = minBalX;
        drawClipped(g, balance, balX, top + 84, 0xFFFFD866, (left + W - 22) - balX);

        if (shopMode == 0) {
            renderBuyShop(g, left, top, d, mouseX, mouseY);
        } else if (shopMode == 1) {
            renderSellShop(g, left, top, d, mouseX, mouseY);
        } else if (shopMode == 2) {
            renderRouletteShop(g, left, top, d, mouseX, mouseY);
        } else {
            renderBankShop(g, left, top, d, mouseX, mouseY);
        }
    }

    private void renderBankShop(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        int panelX = left + 30;
        int panelY = top + 102;
        int panelW = W - 60;
        int panelH = 152;

        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0x500C1925);
        drawBorder(g, panelX, panelY, panelW, panelH, 0xFFBD93F9);

        Component bTitle = Component.translatable("awakening_system.screen.bank.title").withStyle(ChatFormatting.BOLD);
        g.drawCenteredString(font, bTitle, left + W / 2, panelY + 8, 0xFFBD93F9);

        // Contar monedas físicas en el inventario del jugador
        int physicalCoins = 0;
        if (minecraft != null && minecraft.player != null) {
            for (net.minecraft.world.item.ItemStack stack : minecraft.player.getInventory().items) {
                if (!stack.isEmpty() && stack.is(SystemItems.AWAKENING_COIN.get())) {
                    physicalCoins += stack.getCount();
                }
            }
        }

        // Fila informativa de balances
        Component virtBal = Component.translatable("awakening_system.screen.bank.virtual", d.coins());
        Component physBal = Component.translatable("awakening_system.screen.bank.physical", physicalCoins);
        g.drawCenteredString(font, Component.literal(virtBal.getString() + "   §8|   " + physBal.getString()), left + W / 2, panelY + 22, 0xFFDDECF2);

        // Divisor
        g.fill(panelX + 10, panelY + 34, panelX + panelW - 10, panelY + 35, 0x402D7691);

        int colW = (panelW - 30) / 2;
        int col1X = panelX + 10;
        int col2X = panelX + 20 + colW;

        // --- COLUMNA IZQUIERDA: DEPÓSITO (Físico -> Virtual) ---
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bank.deposit_header"), col1X + colW / 2, panelY + 40, 0xFF50FA7B);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bank.deposit_hint"), col1X + colW / 2, panelY + 52, MUTED);

        int b1Y = panelY + 66;
        int b2Y = panelY + 92;
        int btnH = 20;

        // Botón Depositar Todo
        boolean depAllHov = mouseX >= col1X && mouseX < col1X + colW && mouseY >= b1Y && mouseY < b1Y + btnH;
        g.fill(col1X, b1Y, col1X + colW, b1Y + btnH, depAllHov ? 0xFF2ECC71 : 0xFF1D5A40);
        drawBorder(g, col1X, b1Y, colW, btnH, 0xFF50FA7B);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bank.deposit_all", physicalCoins), col1X + colW / 2, b1Y + 6, depAllHov ? 0xFFFFFFFF : 0xFFDDECF2);

        // Botón Depositar 64
        boolean dep64Hov = mouseX >= col1X && mouseX < col1X + colW / 2 - 2 && mouseY >= b2Y && mouseY < b2Y + btnH;
        g.fill(col1X, b2Y, col1X + colW / 2 - 2, b2Y + btnH, dep64Hov ? 0x801D5A40 : 0x50102536);
        drawBorder(g, col1X, b2Y, colW / 2 - 2, btnH, 0xFF50FA7B);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bank.deposit_64"), col1X + (colW / 2 - 2) / 2, b2Y + 6, dep64Hov ? 0xFFFFFFFF : 0xFFDDECF2);

        // Botón Depositar 10
        boolean dep10Hov = mouseX >= col1X + colW / 2 + 2 && mouseX < col1X + colW && mouseY >= b2Y && mouseY < b2Y + btnH;
        g.fill(col1X + colW / 2 + 2, b2Y, col1X + colW, b2Y + btnH, dep10Hov ? 0x801D5A40 : 0x50102536);
        drawBorder(g, col1X + colW / 2 + 2, b2Y, colW / 2 - 2, btnH, 0xFF50FA7B);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bank.deposit_10"), col1X + colW / 2 + 2 + (colW / 2 - 2) / 2, b2Y + 6, dep10Hov ? 0xFFFFFFFF : 0xFFDDECF2);

        // --- COLUMNA DERECHA: RETIRO (Virtual -> Físico) ---
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bank.withdraw_header"), col2X + colW / 2, panelY + 40, 0xFFFFD866);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bank.withdraw_hint"), col2X + colW / 2, panelY + 52, MUTED);

        // Botón Retirar 64
        boolean wit64Hov = mouseX >= col2X && mouseX < col2X + colW && mouseY >= b1Y && mouseY < b1Y + btnH;
        g.fill(col2X, b1Y, col2X + colW, b1Y + btnH, wit64Hov ? 0xFFFFAA00 : 0xFF5D4017);
        drawBorder(g, col2X, b1Y, colW, btnH, 0xFFFFD866);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bank.withdraw_64"), col2X + colW / 2, b1Y + 6, wit64Hov ? 0xFF10202D : 0xFFFFE899);

        // Botón Retirar 10
        boolean wit10Hov = mouseX >= col2X && mouseX < col2X + colW / 2 - 2 && mouseY >= b2Y && mouseY < b2Y + btnH;
        g.fill(col2X, b2Y, col2X + colW / 2 - 2, b2Y + btnH, wit10Hov ? 0x805D4017 : 0x50102536);
        drawBorder(g, col2X, b2Y, colW / 2 - 2, btnH, 0xFFFFD866);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bank.withdraw_10"), col2X + (colW / 2 - 2) / 2, b2Y + 6, wit10Hov ? 0xFFFFFFFF : 0xFFFFD866);

        // Botón Retirar 1
        boolean wit1Hov = mouseX >= col2X + colW / 2 + 2 && mouseX < col2X + colW && mouseY >= b2Y && mouseY < b2Y + btnH;
        g.fill(col2X + colW / 2 + 2, b2Y, col2X + colW, b2Y + btnH, wit1Hov ? 0x805D4017 : 0x50102536);
        drawBorder(g, col2X + colW / 2 + 2, b2Y, colW / 2 - 2, btnH, 0xFFFFD866);
        g.drawCenteredString(font, Component.translatable("awakening_system.screen.bank.withdraw_1"), col2X + colW / 2 + 2 + (colW / 2 - 2) / 2, b2Y + 6, wit1Hov ? 0xFFFFFFFF : 0xFFFFD866);

        // Consejo al pie con ajuste automático de líneas anti-desborde
        Component tipComp = Component.translatable("awakening_system.screen.bank.tip");
        var tipLines = font.split(tipComp, panelW - 24);
        int tipStartY = panelY + 131 - (tipLines.size() * 10) / 2;
        for (var line : tipLines) {
            g.drawCenteredString(font, line, left + W / 2, tipStartY, 0xFF888888);
            tipStartY += 10;
        }
    }

    private void renderRouletteShop(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;
        int panelX = left + 30;
        int panelY = top + 102;
        int panelW = W - 60;
        int panelH = 152;

        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, sys30 ? 0x500E261E : (sys20 ? 0x501E0A2E : 0x500C1925));
        drawBorder(g, panelX, panelY, panelW, panelH, 0xFFFFD866);

        Component rTitle = Component.translatable("awakening_system.screen.roulette.title").withStyle(ChatFormatting.BOLD);
        g.drawCenteredString(font, rTitle, left + W / 2, panelY + 10, 0xFFFFD866);

        Component rDesc1 = Component.translatable("awakening_system.screen.roulette.desc1");
        Component rDesc2 = Component.translatable("awakening_system.screen.roulette.desc2");
        int maxTextW = panelW - 20;
        int textY = panelY + 22;
        var lines1 = font.split(rDesc1, maxTextW);
        var lines2 = font.split(rDesc2, maxTextW);
        if (!lines1.isEmpty()) {
            g.drawCenteredString(font, lines1.get(0), left + W / 2, textY, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));
            textY += 10;
        }
        if (!lines2.isEmpty()) {
            g.drawCenteredString(font, lines2.get(0), left + W / 2, textY, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));
            textY += 10;
        }

        Component poolHeader = Component.translatable("awakening_system.screen.roulette.potential");
        int poolHeaderY = Math.max(textY + 4, panelY + 45);
        g.drawCenteredString(font, poolHeader, left + W / 2, poolHeaderY, (sys30 || sys20) ? 0xFFFFD700 : CYAN);

        net.minecraft.world.item.Item[] showcase = new net.minecraft.world.item.Item[]{
                net.minecraft.world.item.Items.NETHERITE_INGOT,
                net.minecraft.world.item.Items.TOTEM_OF_UNDYING,
                net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE,
                net.minecraft.world.item.Items.DIAMOND_BLOCK,
                net.minecraft.world.item.Items.NETHER_STAR,
                net.minecraft.world.item.Items.NETHERITE_SCRAP,
                net.minecraft.world.item.Items.GOLDEN_APPLE,
                net.minecraft.world.item.Items.ENDER_PEARL
        };

        int startItemX = left + W / 2 - (showcase.length * 36) / 2 + 10;
        int itemY = Math.max(panelY + 59, poolHeaderY + 14);
        for (int i = 0; i < showcase.length; i++) {
            int ix = startItemX + i * 36;
            g.fill(ix - 3, itemY - 3, ix + 21, itemY + 21, 0x60162D3D);
            drawBorder(g, ix - 3, itemY - 3, 24, 24, 0xFF2D7691);
            g.renderItem(new net.minecraft.world.item.ItemStack(showcase[i]), ix, itemY);
        }

        int cost = 1000;
        boolean canAfford = d.coins() >= cost;
        Component costComp = Component.translatable("awakening_system.screen.roulette.cost", cost, (canAfford ? "§a" : "§c") + d.coins());
        g.drawCenteredString(font, costComp, left + W / 2, panelY + 89, canAfford ? 0xFFDDECF2 : 0xFFFF7666);

        int btnW = 240;
        int btnH = 24;
        int btnX = left + W / 2 - btnW / 2;
        int btnY = panelY + 106;
        boolean btnHov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;

        int btnBg = canAfford ? (btnHov ? 0xFFFFAA00 : 0xFFB8860B) : 0xFF333333;
        int borderCol = canAfford ? (btnHov ? 0xFFFFFFFF : 0xFFFFD866) : 0xFF555555;
        int textCol = canAfford ? (btnHov ? 0xFF10202D : 0xFFFFFFFF) : 0xFF888888;

        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        drawBorder(g, btnX, btnY, btnW, btnH, borderCol);
        Component btnText = Component.translatable("awakening_system.screen.roulette.spin_btn", cost).withStyle(ChatFormatting.BOLD);
        g.drawCenteredString(font, btnText, btnX + btnW / 2, btnY + 8, textCol);
    }

    private void renderBuyShop(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;
        // Botones de filtro de categorías con ancho balanceado y sin superposición
        int catX = left + 30;
        int catY = top + 100;
        int[] widths = {34, 48, 40, 68, 62};
        for (int c = 0; c < SHOP_CATEGORIES.length; c++) {
            String cat = SHOP_CATEGORIES[c];
            int btnW = widths[c];
            boolean selected = shopCategory.equalsIgnoreCase(cat);
            boolean hovered = mouseX >= catX && mouseX < catX + btnW && mouseY >= catY && mouseY < catY + 16;
            g.fill(catX, catY, catX + btnW, catY + 16, selected ? (sys30 ? 0xFF1C382E : (sys20 ? 0xFF3A1259 : 0xFF276A82)) : (hovered ? (sys30 ? 0x40163E2A : (sys20 ? 0x409D4EDD : 0x402D7691)) : (sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF102536))));
            if (selected) g.fill(catX, catY + 14, catX + btnW, catY + 16, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
            int textColor = selected ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : (hovered ? (sys30 ? 0xFF50FA7B : (sys20 ? 0xFFF2D6 : 0xFFFFFFFF)) : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED)));
            if ("boxes".equals(cat)) textColor = selected ? 0xFFFFAA00 : (hovered ? 0xFFFFD866 : 0xFFFFAA00);
            Component catLabel = Component.translatable("awakening_system.screen.shop.cat." + cat);
            g.drawCenteredString(font, catLabel, catX + btnW / 2, catY + 4, textColor);
            catX += btnW + 3;
        }

        if (shopSearchBox != null) {
            int sX = left + 412 - 92;
            int sY = top + 100;
            int sW = 92;
            int sH = 16;
            boolean searchFocused = shopSearchBox.isFocused();
            int bg = sys30 ? 0xFF0D1F17 : (sys20 ? 0xFF1A0B2E : 0xFF0A1824);
            int border = searchFocused ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : (sys30 ? 0x6050FA7B : (sys20 ? 0x609D4EDD : 0x502D7691));
            g.fill(sX, sY, sX + sW, sY + sH, bg);
            drawBorder(g, sX, sY, sW, sH, border);
            shopSearchBox.render(g, mouseX, mouseY, 0);
        }

        List<ShopDefinitions.Entry> entries = availableShopEntries();
        int maxScroll = Math.max(0, entries.size() - 4);
        if (shopScroll > maxScroll) shopScroll = maxScroll;

        enableScissorScaled(g, left + 28, top + 119, left + 412, top + 260);
        ItemStack hoveredStack = null;
        List<Component> lockedTooltip = null;
        for (int row = 0; row < entries.size(); row++) {
            ShopDefinitions.Entry entry = entries.get(row);
            int entryY = top + 122 + (row - shopScroll) * 32;
            if (entryY < top + 118 || entryY >= top + 258) continue;
            boolean hovered = mouseX >= left + 30 && mouseX < left + 406 && mouseY >= entryY && mouseY < entryY + 28;
            if (hovered) g.fill(left + 30, entryY, left + 406, entryY + 28, sys30 ? 0x30163E2A : (sys20 ? 0x303A1259 : 0x301C465D));
            ItemStack stack = ShopDefinitions.createStack(entry);
            g.renderItem(stack, left + 34, entryY + 4);
            if (mouseX >= left + 34 && mouseX < left + 52 && mouseY >= entryY + 4 && mouseY < entryY + 22) {
                hoveredStack = stack;
            }

            boolean unlocked = ShopDefinitions.isUnlocked(entry, d);

            int nameColor = 0xFFFFFFFF;
            if ("boxes".equals(entry.category())) {
                nameColor = switch (entry.id()) {
                    case "box_rare" -> 0xFF55AAFF;
                    case "box_epic" -> 0xFFD055FF;
                    case "box_legendary" -> 0xFFFFAA00;
                    case "lucky_box" -> 0xFFFFD866;
                    default -> 0xFFE0E0E0;
                };
            }

            int btnW = 58;
            int btnX = left + 344;
            boolean btnHovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= entryY + 5 && mouseY < entryY + 23;

            Component price = Component.translatable("awakening_system.screen.shop.price", entry.cost(), entry.amount());
            boolean canAfford = d.coins() >= entry.cost();
            int priceColor = !unlocked ? 0xFF888888 : (canAfford ? 0xFFFFD866 : 0xFFFF7666);
            int priceW = font.width(price);
            int priceX = btnX - 8 - priceW;
            g.drawString(font, price, priceX, entryY + 9, priceColor);

            int titleMaxW = Math.max(70, priceX - (left + 58) - 4);
            drawClipped(g, Component.translatable(entry.nameKey()), left + 58, entryY + 3, hovered ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : nameColor, titleMaxW);
            
            if (unlocked) {
                g.drawString(font, Component.translatable("awakening_system.screen.shop.category." + entry.category()), left + 58, entryY + 15, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));
            } else {
                String lockMsg;
                if (!entry.requiredDimension().isEmpty() && !d.hasVisitedDimension(entry.requiredDimension())) {
                    lockMsg = switch (entry.requiredDimension()) {
                        case "minecraft:the_nether", "the_nether" -> "§c🔒 [EL NETHER]";
                        case "minecraft:the_end", "the_end" -> "§c🔒 [EL END]";
                        case "aether:the_aether", "aether" -> "§c🔒 [EL ÉTER (AETHER)]";
                        case "twilightforest:twilight_forest", "twilightforest" -> "§c🔒 [BOSQUE CREPUSCULAR]";
                        case "undergarden:undergarden", "undergarden" -> "§c🔒 [SUBJARDÍN (UNDERGARDEN)]";
                        default -> "§c🔒 [OTRA DIMENSIÓN]";
                    };
                } else if (entry.requiredSystemTier() == 2) {
                    lockMsg = "§c🔒 [SISTEMA 2.0 - ISEKAI]";
                } else if (entry.requiredSystemTier() == 3) {
                    lockMsg = "§c🔒 [SISTEMA 3.0 - MURIM]";
                } else if (entry.requiredSystemTier() >= 4) {
                    lockMsg = "§c🔒 [SISTEMA 4.0 - ASTRAL]";
                } else {
                    lockMsg = "§c🔒 [BLOQUEADO]";
                }
                g.drawString(font, Component.literal(lockMsg), left + 58, entryY + 15, 0xFFFF5555);
            }

            // Botón de acción Comprar / Abrir / Bloqueado
            if (unlocked) {
                boolean isDirectOpenBox = "box_common".equals(entry.id()) || "box_rare".equals(entry.id())
                        || "box_epic".equals(entry.id()) || "box_legendary".equals(entry.id()) || "lucky_box".equals(entry.id());
                String btnKey = isDirectOpenBox ? "awakening_system.screen.shop.open_btn" : "awakening_system.screen.shop.buy_btn";
                Component btnText = Component.translatable(btnKey);
                g.fill(btnX, entryY + 5, btnX + btnW, entryY + 23, btnHovered ? (canAfford ? 0xFF2ECC71 : 0xFFE74C3C) : (canAfford ? 0xFF1D5A40 : 0xFF3A1E24));
                drawBorder(g, btnX, entryY + 5, btnW, 18, btnHovered ? (canAfford ? 0xFFFFFFFF : 0xFFFF5555) : (canAfford ? 0xFF50FA7B : 0xFF882222));
                g.drawCenteredString(font, btnText, btnX + btnW / 2, entryY + 9, btnHovered ? 0xFFFFFFFF : (canAfford ? 0xFF50FA7B : 0xFFFF7666));
            } else {
                g.fill(btnX, entryY + 5, btnX + btnW, entryY + 23, btnHovered ? 0xFF3A1118 : 0xFF200B0E);
                drawBorder(g, btnX, entryY + 5, btnW, 18, 0xFF882222);
                g.drawCenteredString(font, Component.translatable("awakening_system.shop.locked_badge"), btnX + btnW / 2, entryY + 9, 0xFFFF7666);
                if (btnHovered || (hovered && mouseX >= left + 58)) {
                    if (!entry.requiredDimension().isEmpty() && !d.hasVisitedDimension(entry.requiredDimension())) {
                        Component dimName = switch (entry.requiredDimension()) {
                            case "minecraft:the_nether", "the_nether" -> Component.translatable("advancements.nether.root.title");
                            case "minecraft:the_end", "the_end" -> Component.translatable("advancements.end.root.title");
                            case "aether:the_aether", "aether" -> Component.literal("The Aether");
                            case "twilightforest:twilight_forest", "twilightforest" -> Component.literal("Twilight Forest");
                            case "undergarden:undergarden", "undergarden" -> Component.literal("The Undergarden");
                            default -> Component.literal(entry.requiredDimension());
                        };
                        lockedTooltip = List.of(
                                Component.translatable("awakening_system.shop.locked_title"),
                                Component.translatable("awakening_system.shop.locked_dim", dimName)
                        );
                    } else if (entry.requiredSystemTier() >= 4) {
                        lockedTooltip = List.of(
                                Component.translatable("awakening_system.shop.locked_title"),
                                Component.translatable("awakening_system.shop.locked_tier", "4.0"),
                                Component.literal("§8Requiere Dantian 3+, Torre Piso 50 o Pagoda conquistada.")
                        );
                    } else {
                        Component dimName = entry.requiredSystemTier() == 2 ?
                                Component.translatable("awakening_system.screen.dim.isekai.title") :
                                Component.translatable("awakening_system.screen.dim.murim.title");
                        lockedTooltip = List.of(
                                Component.translatable("awakening_system.shop.locked_title"),
                                Component.translatable("awakening_system.shop.locked_tier", (entry.requiredSystemTier() == 2 ? "2.0" : "3.0")),
                                Component.translatable("awakening_system.shop.locked_dim", dimName)
                        );
                    }
                }
            }
        }
        g.disableScissor();

        if (hoveredStack != null && !hoveredStack.isEmpty()) {
            g.renderTooltip(font, hoveredStack, mouseX, mouseY);
        } else if (lockedTooltip != null) {
            g.renderComponentTooltip(font, lockedTooltip, mouseX, mouseY);
        }

        if (entries.size() > 4) {
            int trackTop = top + 122;
            int trackBottom = top + 256;
            int thumbHeight = Math.max(14, (trackBottom - trackTop) * 4 / entries.size());
            int thumbRange = trackBottom - trackTop - thumbHeight;
            int thumbY = trackTop + thumbRange * shopScroll / maxScroll;
            boolean scrollHover = mouseX >= left + W - 24 && mouseX <= left + W - 8 && mouseY >= trackTop && mouseY <= trackBottom;
            int trackCol = sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF0C1925);
            int thumbCol = (sys30 || sys20) ? ((scrollHover || (isDraggingScrollbar && draggingScrollbarTab == 4)) ? 0xFFFFF176 : 0xFFFFD700)
                    : ((scrollHover || (isDraggingScrollbar && draggingScrollbarTab == 4)) ? 0xFFA0F5FF : CYAN);
            g.fill(left + W - 19, trackTop, left + W - 14, trackBottom, trackCol);
            g.fill(left + W - 19, thumbY, left + W - 14, thumbY + thumbHeight, thumbCol);
        }

        if (entries.isEmpty()) g.drawString(font, Component.translatable("awakening_system.screen.shop.empty"), left + 34, top + 140, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));
    }

    private void renderSellShop(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;
        // Botones de filtro de categorías de venta con ancho balanceado y sin superposición
        int catX = left + 30;
        int catY = top + 100;
        int[] widths = {34, 56, 38, 48, 46};
        for (int c = 0; c < SELL_CATEGORIES.length; c++) {
            String cat = SELL_CATEGORIES[c];
            int btnW = widths[c];
            boolean selected = sellCategory.equalsIgnoreCase(cat);
            boolean hovered = mouseX >= catX && mouseX < catX + btnW && mouseY >= catY && mouseY < catY + 16;
            g.fill(catX, catY, catX + btnW, catY + 16, selected ? 0xFF1D5A40 : (hovered ? 0x402ECC71 : (sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF102536))));
            if (selected) g.fill(catX, catY + 14, catX + btnW, catY + 16, 0xFF50FA7B);
            Component catLabel = Component.translatable("awakening_system.screen.shop.sell_cat." + cat);
            g.drawCenteredString(font, catLabel, catX + btnW / 2, catY + 4, selected ? 0xFF50FA7B : (hovered ? (sys30 ? 0xFF50FA7B : (sys20 ? 0xFFF2D6 : 0xFFDDECF2)) : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED))));
            catX += btnW + 3;
        }

        if (shopSearchBox != null) {
            int sX = left + 412 - 92;
            int sY = top + 100;
            int sW = 92;
            int sH = 16;
            boolean searchFocused = shopSearchBox.isFocused();
            int bg = sys30 ? 0xFF0D1F17 : (sys20 ? 0xFF1A0B2E : 0xFF0A1824);
            int border = searchFocused ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : (sys30 ? 0x6050FA7B : (sys20 ? 0x609D4EDD : 0x502D7691));
            g.fill(sX, sY, sX + sW, sY + sH, bg);
            drawBorder(g, sX, sY, sW, sH, border);
            shopSearchBox.render(g, mouseX, mouseY, 0);
        }

        List<SellDefinitions.SellEntry> entries = availableSellEntries();
        int maxScroll = Math.max(0, entries.size() - 4);
        if (sellScroll > maxScroll) sellScroll = maxScroll;

        enableScissorScaled(g, left + 28, top + 119, left + 412, top + 260);
        ItemStack hoveredStack = null;
        for (int row = 0; row < entries.size(); row++) {
            SellDefinitions.SellEntry entry = entries.get(row);
            int entryY = top + 122 + (row - sellScroll) * 32;
            if (entryY < top + 118 || entryY >= top + 258) continue;
            boolean hovered = mouseX >= left + 30 && mouseX < left + 406 && mouseY >= entryY && mouseY < entryY + 28;
            if (hovered) g.fill(left + 30, entryY, left + 406, entryY + 28, sys30 ? 0x30163E2A : (sys20 ? 0x303A1259 : 0x301C465D));
            ItemStack stack = SellDefinitions.createStack(entry);
            g.renderItem(stack, left + 34, entryY + 4);
            if (mouseX >= left + 34 && mouseX < left + 52 && mouseY >= entryY + 4 && mouseY < entryY + 22) {
                hoveredStack = stack;
            }

            int count = SellDefinitions.countInInventory(Minecraft.getInstance().player, entry);
            Component ownedComp = Component.translatable("awakening_system.screen.shop.owned", count);
            g.drawString(font, ownedComp, left + 58, entryY + 15, count >= entry.amount() ? 0xFF50FA7B : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED)));

            boolean canSell = count >= entry.amount();

            int vBtnX = left + 285;
            int vBtnW = 50;
            int allBtnX = left + 341;
            int allBtnW = 60;

            String priceStr = "+" + entry.unitPrice() + " ⛃" + (entry.amount() > 1 ? " /" + entry.amount() : "");
            Component priceComp = Component.literal(priceStr);
            int priceW = font.width(priceComp);
            int priceX = vBtnX - 6 - priceW;
            g.drawString(font, priceComp, priceX, entryY + 9, 0xFFFFD866);

            int titleMaxW = Math.max(60, priceX - (left + 58) - 4);
            drawClipped(g, Component.translatable(entry.nameKey()), left + 58, entryY + 3, hovered ? ((sys30 || sys20) ? 0xFFFFD700 : CYAN) : 0xFFFFFFFF, titleMaxW);

            // Botón Vender (1 / lote)
            boolean vHovered = mouseX >= vBtnX && mouseX < vBtnX + vBtnW && mouseY >= entryY + 5 && mouseY < entryY + 23;
            g.fill(vBtnX, entryY + 5, vBtnX + vBtnW, entryY + 23, vHovered && canSell ? 0xFF2ECC71 : (canSell ? 0xFF1D5A40 : 0x25102536));
            drawBorder(g, vBtnX, entryY + 5, vBtnW, 18, canSell ? 0x8050FA7B : (sys30 ? 0x20D4AF37 : (sys20 ? 0x209D4EDD : 0x202D7691)));
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.shop.sell_btn"), vBtnX + vBtnW / 2, entryY + 9,
                    vHovered && canSell ? 0xFFFFFFFF : (canSell ? 0xFF50FA7B : 0x409BB7C7));

            // Botón Vender Todo
            boolean allHovered = mouseX >= allBtnX && mouseX < allBtnX + allBtnW && mouseY >= entryY + 5 && mouseY < entryY + 23;
            g.fill(allBtnX, entryY + 5, allBtnX + allBtnW, entryY + 23, allHovered && canSell ? 0xFF2ECC71 : (canSell ? 0xFF1D5A40 : 0x25102536));
            drawBorder(g, allBtnX, entryY + 5, allBtnW, 18, canSell ? 0x8050FA7B : (sys30 ? 0x20D4AF37 : (sys20 ? 0x209D4EDD : 0x202D7691)));
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.shop.sell_all_btn"), allBtnX + allBtnW / 2, entryY + 9,
                    allHovered && canSell ? 0xFFFFFFFF : (canSell ? 0xFF50FA7B : 0x409BB7C7));
        }
        g.disableScissor();

        if (hoveredStack != null && !hoveredStack.isEmpty()) {
            g.renderTooltip(font, hoveredStack, mouseX, mouseY);
        }

        if (entries.size() > 4) {
            int trackTop = top + 122;
            int trackBottom = top + 256;
            int thumbHeight = Math.max(14, (trackBottom - trackTop) * 4 / entries.size());
            int thumbRange = trackBottom - trackTop - thumbHeight;
            int thumbY = trackTop + thumbRange * sellScroll / maxScroll;
            boolean scrollHover = mouseX >= left + W - 24 && mouseX <= left + W - 8 && mouseY >= trackTop && mouseY <= trackBottom;
            int trackCol = sys30 ? 0xFF0A1815 : (sys20 ? 0xFF160926 : 0xFF0C1925);
            int thumbCol = (scrollHover || (isDraggingScrollbar && draggingScrollbarTab == 4)) ? 0xFF7DFF9F : 0xFF50FA7B;
            g.fill(left + W - 19, trackTop, left + W - 14, trackBottom, trackCol);
            g.fill(left + W - 19, thumbY, left + W - 14, thumbY + thumbHeight, thumbCol);
        }

        if (entries.isEmpty()) g.drawString(font, Component.translatable("awakening_system.screen.shop.sell_empty"), left + 34, top + 140, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));
    }

    private void renderProgress(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        section(g, left + 20, top + 78, W - 40, 186, Component.translatable("awakening_system.screen.tab.progress"));
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;

        // Fila 1: Prestigio
        g.drawString(font, Component.translatable("awakening_system.screen.progress.prestige", d.prestigeLevel()), left + 32, top + 102, 0xFFFFB86C);
        if (d.canPrestige()) {
            int btnX = left + 250;
            int btnY = top + 98;
            boolean hovered = mouseX >= btnX && mouseX < btnX + 135 && mouseY >= btnY && mouseY < btnY + 18;
            g.fill(btnX, btnY, btnX + 135, btnY + 18, hovered ? 0xFF50FA7B : (sys30 ? 0xFF1B7A4E : (sys20 ? 0xFF8A2BE2 : 0xFF276A82)));
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.progress.prestige_btn"), btnX + 67, btnY + 5, hovered ? 0xFF10202D : 0xFFFFFFFF);
        } else {
            drawClipped(g, Component.translatable("awakening_system.screen.progress.prestige_hint"), left + 155, top + 102, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED), 230);
        }

        // Fila 2: Panel de Estadísticas del Portador
        g.fill(left + 28, top + 119, left + W - 28, top + 147, sys30 ? 0x400E261E : (sys20 ? 0x401E0A2E : 0x400C1925));
        g.fill(left + 28, top + 119, left + W - 28, top + 120, sys30 ? 0x50D4AF37 : (sys20 ? 0x509D4EDD : 0x502D7691));
        drawClipped(g, Component.translatable("awakening_system.screen.stats.summary",
                d.creaturesDefeated(), d.blocksMined(), d.oresMined(), d.purchases()), left + 34, top + 122, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED), W - 72);
        drawClipped(g, Component.translatable("awakening_system.screen.stats.extra",
                d.bossKills(), d.distanceMoved(), d.deaths(), d.missionsCompleted()), left + 34, top + 134, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED), W - 72);

        // Fila 3: Encabezado de Logros
        g.drawString(font, Component.translatable("awakening_system.screen.progress.achievements_header"), left + 32, top + 151, (sys30 || sys20) ? 0xFFFFD700 : CYAN);

        // Fila 4: Lista Scrolleable de Logros
        List<AchievementDefinitions.Definition> achievements = AchievementDefinitions.definitions();
        enableScissorScaled(g, left + 28, top + 162, left + W - 28, top + 260);
        int line = top + 164 - progressScroll * 28;
        for (AchievementDefinitions.Definition achievement : achievements) {
            if (line >= top + 158 && line < top + 258) {
                boolean unlocked = d.hasAchievement(achievement.id());
                boolean hovered = mouseX >= left + 30 && mouseX < left + W - 30 && mouseY >= line && mouseY < line + 26;
                if (hovered) g.fill(left + 30, line, left + W - 30, line + 26, sys30 ? 0x30163E2A : (sys20 ? 0x303E1559 : 0x301C465D));
                drawClipped(g, Component.translatable(achievement.nameKey()), left + 36, line + 2, unlocked ? 0xFF50FA7B : 0xFFFFFFFF, 195);
                Component status = unlocked
                        ? Component.translatable("awakening_system.screen.achievement.unlocked")
                        : Component.literal(AchievementDefinitions.statValue(d, achievement.statRequired()) + " / " + achievement.target());
                g.drawString(font, status, left + 240, line + 2, unlocked ? 0xFF50FA7B : (sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED)));
                drawClipped(g, Component.translatable(achievement.descriptionKey()), left + 36, line + 14, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED), 320);
            }
            line += 28;
        }
        g.disableScissor();

        if (achievements.size() > 3) {
            int trackTop = top + 164;
            int trackBottom = top + 258;
            int thumbHeight = Math.max(14, (trackBottom - trackTop) * 3 / achievements.size());
            int thumbRange = trackBottom - trackTop - thumbHeight;
            int thumbY = trackTop + thumbRange * progressScroll / (achievements.size() - 3);
            g.fill(left + W - 18, trackTop, left + W - 15, trackBottom, sys30 ? 0xFF0A1815 : (sys20 ? 0xFF140822 : 0xFF0C1925));
            g.fill(left + W - 18, thumbY, left + W - 15, thumbY + thumbHeight, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
        }
    }

    private void renderRanking(GuiGraphics g, int left, int top, SystemData d, int mouseX, int mouseY) {
        section(g, left + 20, top + 78, W - 40, 186, Component.translatable("awakening_system.screen.tab.ranking"));
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;
        var list = AwakeningSystemClient.LEADERBOARD;
        if (list.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.ranking.empty"), left + W / 2, top + 140, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));
            return;
        }

        // Subencabezado de columnas
        g.fill(left + 28, top + 98, left + W - 28, top + 112, sys30 ? 0x50142E24 : (sys20 ? 0x5028103F : 0x50102536));
        g.drawString(font, Component.translatable("awakening_system.screen.ranking.col.rank"), left + 34, top + 101, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
        g.drawString(font, Component.translatable("awakening_system.screen.ranking.col.level"), left + 195, top + 101, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
        g.drawString(font, Component.translatable("awakening_system.screen.ranking.col.prestige"), left + 245, top + 101, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
        g.drawString(font, Component.translatable("awakening_system.screen.ranking.col.kills"), left + 305, top + 101, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
        g.drawString(font, Component.translatable("awakening_system.screen.ranking.col.coins"), left + 355, top + 101, (sys30 || sys20) ? 0xFFFFD700 : CYAN);

        var player = Minecraft.getInstance().player;
        String myName = player != null ? player.getGameProfile().getName() : "";

        LeaderboardPayload.LeaderboardEntry hoveredEntry = null;
        int hoveredRank = -1;

        enableScissorScaled(g, left + 28, top + 114, left + W - 28, top + 260);
        int line = top + 116 - rankingScroll * 24;
        for (int i = 0; i < list.size(); i++) {
            var entry = list.get(i);
            if (line >= top + 112 && line < top + 258) {
                boolean isMe = myName.equalsIgnoreCase(entry.name());
                boolean hovered = mouseX >= left + 30 && mouseX < left + W - 34 && mouseY >= line && mouseY < line + 22;

                int rankColor = switch (i) {
                    case 0 -> 0xFFFFD866; // Oro
                    case 1 -> 0xFFE0E0E0; // Plata
                    case 2 -> 0xFFCD7F32; // Bronce
                    default -> ((sys30 || sys20) ? 0xFFFFD700 : CYAN);
                };

                // Fondo de fila con estilo Solo Leveling HUD
                if (isMe) {
                    g.fill(left + 30, line, left + W - 34, line + 22, sys30 ? 0x45163E2A : (sys20 ? 0x459D4EDD : 0x452D7691));
                    drawBorder(g, left + 30, line, W - 64, 22, 0x8050FA7B);
                    g.fill(left + 30, line, left + 33, line + 22, 0xFF50FA7B);
                } else if (hovered) {
                    g.fill(left + 30, line, left + W - 34, line + 22, sys30 ? 0x30163E2A : (sys20 ? 0x303E1559 : 0x301C465D));
                    drawBorder(g, left + 30, line, W - 64, 22, sys30 ? 0x60D4AF37 : (sys20 ? 0x609D4EDD : 0x602D7691));
                    if (i < 3) g.fill(left + 30, line, left + 33, line + 22, rankColor);
                } else {
                    g.fill(left + 30, line, left + W - 34, line + 22, sys30 ? ((i % 2 == 0) ? 0x20102B20 : 0x120B1F17) : (sys20 ? ((i % 2 == 0) ? 0x202B1242 : 0x121D0B2E) : ((i % 2 == 0) ? 0x20102536 : 0x120C1925)));
                    if (i < 3) g.fill(left + 30, line, left + 33, line + 22, rankColor);
                }

                String badge = switch (i) {
                    case 0 -> "★ #1 ";
                    case 1 -> "✦ #2 ";
                    case 2 -> "◆ #3 ";
                    default -> "#" + (i + 1) + " ";
                };

                String displayName = badge + entry.name() + (isMe ? " " + Component.translatable("awakening_system.screen.ranking.you").getString() : "");
                drawClipped(g, Component.literal(displayName), left + 36, line + 5, isMe ? 0xFF50FA7B : rankColor, 155);

                g.drawString(font, String.valueOf(entry.level()), left + 200, line + 5, 0xFFFFFFFF);
                g.drawString(font, String.valueOf(entry.prestige()), left + 252, line + 5, 0xFFFFB86C);
                g.drawString(font, String.valueOf(entry.kills()), left + 310, line + 5, sys30 ? 0xFF88C999 : (sys20 ? 0xFFD1B3E0 : MUTED));
                g.drawString(font, String.valueOf(entry.coins()), left + 358, line + 5, 0xFFFFD866);

                if (hovered) {
                    hoveredEntry = entry;
                    hoveredRank = i + 1;
                }
            }
            line += 24;
        }
        g.disableScissor();

        if (hoveredEntry != null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("#" + hoveredRank + " " + hoveredEntry.name()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            tooltip.add(Component.translatable("awakening_system.screen.ranking.col.level").append(": " + hoveredEntry.level()).withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.translatable("awakening_system.screen.ranking.col.prestige").append(": " + hoveredEntry.prestige()).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("awakening_system.screen.ranking.col.kills").append(": " + hoveredEntry.kills()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("awakening_system.screen.ranking.col.coins").append(": " + hoveredEntry.coins() + " ⛃").withStyle(ChatFormatting.YELLOW));
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }

        if (list.size() > 5) {
            int trackTop = top + 116;
            int trackBottom = top + 256;
            int thumbHeight = Math.max(14, (trackBottom - trackTop) * 5 / list.size());
            int thumbRange = trackBottom - trackTop - thumbHeight;
            int thumbY = trackTop + thumbRange * rankingScroll / (list.size() - 5);
            g.fill(left + W - 18, trackTop, left + W - 15, trackBottom, sys30 ? 0xFF0A1815 : (sys20 ? 0xFF140822 : 0xFF0C1925));
            g.fill(left + W - 18, thumbY, left + W - 15, thumbY + thumbHeight, (sys30 || sys20) ? 0xFFFFD700 : CYAN);
        }
    }

    private List<ShopDefinitions.Entry> availableShopEntries() {
        List<ShopDefinitions.Entry> list = ShopDefinitions.availableEntries(shopCategory);
        if (shopSearchBox == null) return list;
        String query = shopSearchBox.getValue().trim().toLowerCase(java.util.Locale.ROOT);
        if (query.isEmpty()) return list;
        return list.stream().filter(entry -> {
            if (entry.id().toLowerCase(java.util.Locale.ROOT).contains(query)) return true;
            if (entry.itemId().toLowerCase(java.util.Locale.ROOT).contains(query)) return true;
            String trans = Component.translatable(entry.nameKey()).getString().toLowerCase(java.util.Locale.ROOT);
            if (trans.contains(query)) return true;
            ItemStack stack = ShopDefinitions.createStack(entry);
            if (!stack.isEmpty() && stack.getHoverName().getString().toLowerCase(java.util.Locale.ROOT).contains(query)) return true;
            return false;
        }).toList();
    }

    private List<SellDefinitions.SellEntry> availableSellEntries() {
        List<SellDefinitions.SellEntry> list = SellDefinitions.availableEntries(sellCategory);
        if (shopSearchBox == null) return list;
        String query = shopSearchBox.getValue().trim().toLowerCase(java.util.Locale.ROOT);
        if (query.isEmpty()) return list;
        return list.stream().filter(entry -> {
            if (entry.id().toLowerCase(java.util.Locale.ROOT).contains(query)) return true;
            if (entry.itemId().toLowerCase(java.util.Locale.ROOT).contains(query)) return true;
            String trans = Component.translatable(entry.nameKey()).getString().toLowerCase(java.util.Locale.ROOT);
            if (trans.contains(query)) return true;
            ItemStack stack = SellDefinitions.createStack(entry);
            if (!stack.isEmpty() && stack.getHoverName().getString().toLowerCase(java.util.Locale.ROOT).contains(query)) return true;
            return false;
        }).toList();
    }

    private int rarityColor(SkillDefinitions.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFFE0E0E0;
            case UNCOMMON -> 0xFF55FF55;
            case RARE -> 0xFF55AAFF;
            case EPIC -> 0xFFD055FF;
            case LEGENDARY -> 0xFFFFAA00;
            case MYTHIC -> 0xFFFF55FF;
            case UNIQUE -> 0xFFFFD700;
        };
    }

    private void section(GuiGraphics g, int x, int y, int w, int h, Component title) {
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;
        int bg = sys30 ? 0xB50E1F1A : (sys20 ? 0xB5170B28 : 0xB50C1C2B);
        int secHeader = sys30 ? 0xFF142E24 : (sys20 ? 0xFF28103F : SECTION);
        int edgeLine = sys30 ? 0xFFD4AF37 : (sys20 ? 0xFF9D4EDD : EDGE);
        int titleCol = (sys30 || sys20) ? 0xFFFFD700 : CYAN;
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 20, secHeader);
        g.fill(x, y + 19, x + w, y + 20, edgeLine);
        drawClipped(g, title, x + 10, y + 6, titleCol, w - 20);
    }

    private void attribute(GuiGraphics g, int x, int y, String id, String name, int value, int plusX, int plusY, int mouseX, int mouseY, boolean canSpend) {
        drawClipped(g, Component.literal(name + "  " + value), x, y, 0xFFFFFFFF, Math.max(50, plusX - x - 4));
        boolean hovered = mouseX >= plusX && mouseX < plusX + 16 && mouseY >= plusY && mouseY < plusY + 16;
        boolean sys30 = isSys30();
        boolean sys20 = isSys20() || sys30;
        if (canSpend) {
            int btnBg = hovered ? 0xFF2ECC71 : (sys30 ? 0xFF163E2A : (sys20 ? 0xFF5B1B7D : 0xFF1D5A40));
            int border = hovered ? 0xFFFFFFFF : (sys30 ? 0xFFFFD700 : (sys20 ? 0xFFFFD700 : 0xFF50FA7B));
            g.fill(plusX, plusY, plusX + 16, plusY + 16, btnBg);
            drawBorder(g, plusX, plusY, 16, 16, border);
            g.drawString(font, Component.literal("+"), plusX + 5, plusY + 2, hovered ? 0xFFFFFFFF : (sys30 ? 0xFFFFD700 : (sys20 ? 0xFFFFD700 : 0xFF50FA7B)));
        } else {
            g.fill(plusX, plusY, plusX + 16, plusY + 16, sys30 ? 0x300E261E : (sys20 ? 0x303A1259 : 0x30102536));
            g.drawString(font, Component.literal("+"), plusX + 5, plusY + 2, sys30 ? 0x6088C999 : (sys20 ? 0x60D1B3E0 : 0x409BB7C7));
        }
    }

    private void drawAutoScaledCentered(GuiGraphics g, Component text, int cx, int y, int color, int maxW) {
        int w = font.width(text);
        if (w <= maxW) {
            g.drawCenteredString(font, text, cx, y, color);
            return;
        }
        float scale = (float) maxW / (float) w;
        var pose = g.pose();
        pose.pushPose();
        pose.translate(cx, y + (font.lineHeight * (1.0F - scale) / 2.0F), 0);
        pose.scale(scale, scale, 1.0F);
        g.drawCenteredString(font, text, 0, 0, color);
        pose.popPose();
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void drawClipped(GuiGraphics g, Component text, int x, int y, int color, int maxWidth) {
        int textWidth = font.width(text);
        if (textWidth <= maxWidth) {
            g.drawString(font, text, x, y, color);
            return;
        }
        float scale = (float) maxWidth / textWidth;
        if (scale >= 0.50F) {
            var pose = g.pose();
            pose.pushPose();
            pose.translate(x, y + (1.0F - scale) * 4.0F, 0);
            pose.scale(scale, scale, 1.0F);
            g.drawString(font, text, 0, 0, color);
            pose.popPose();
            return;
        }
        var pose = g.pose();
        pose.pushPose();
        pose.translate(x, y + 2.0F, 0);
        pose.scale(0.50F, 0.50F, 1.0F);
        int scaledMax = (int) (maxWidth / 0.50F);
        String clipped = font.plainSubstrByWidth(text.getString(), Math.max(0, scaledMax - font.width("...")));
        g.drawString(font, Component.literal(clipped + "..."), 0, 0, color);
        pose.popPose();
    }

    private void drawClipped(GuiGraphics g, String text, int x, int y, int color, int maxWidth) {
        drawClipped(g, Component.literal(text), x, y, color, maxWidth);
    }

    private SystemData data() {
        return Minecraft.getInstance().player == null ? new SystemData()
                : Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
    }
}
