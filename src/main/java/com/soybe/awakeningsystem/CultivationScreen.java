package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class CultivationScreen extends Screen {
    private final Screen parentScreen;
    private int scrollOffset = 0;
    private int rollTicks = 0;
    private int activeTab = 0; // 0: Meridianos, 1: Técnicas del Dao

    public record MeridianInfo(int id, int qiCost) {
        public Component name() {
            return Component.translatable("awakening_system.meridian." + id + ".name");
        }
        public Component organ() {
            return Component.translatable("awakening_system.meridian." + id + ".organ");
        }
        public Component effect() {
            return Component.translatable("awakening_system.meridian." + id + ".effect");
        }
    }

    public record DaoSkillInfo(int id, int reqRealm, int qiCost, int cooldownSec) {
        public Component name() {
            return Component.translatable("awakening_system.dao_skill." + id + ".name");
        }
        public Component desc() {
            return Component.translatable("awakening_system.dao_skill." + id + ".desc");
        }
    }

    private static final List<MeridianInfo> MERIDIANS = List.of(
            new MeridianInfo(0, 35),
            new MeridianInfo(1, 70),
            new MeridianInfo(2, 105),
            new MeridianInfo(3, 140),
            new MeridianInfo(4, 175),
            new MeridianInfo(5, 210),
            new MeridianInfo(6, 245),
            new MeridianInfo(7, 280),
            new MeridianInfo(8, 315),
            new MeridianInfo(9, 350),
            new MeridianInfo(10, 385),
            new MeridianInfo(11, 420)
    );

    private static final List<DaoSkillInfo> DAO_SKILLS = List.of(
            new DaoSkillInfo(0, 1, 20, 7),
            new DaoSkillInfo(1, 2, 35, 10),
            new DaoSkillInfo(2, 3, 50, 18),
            new DaoSkillInfo(3, 4, 75, 25),
            new DaoSkillInfo(4, 5, 45, 8)
    );

    public CultivationScreen(Screen parentScreen) {
        super(Component.translatable("awakening_system.screen.cultivation.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Evita el shader de desenfoque de Minecraft vanilla
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (rollTicks > 0) {
            rollTicks--;
            if (rollTicks % 4 == 0 && minecraft != null && rollTicks > 0) {
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.4F));
            }
            if (rollTicks == 0) {
                AwakeningSystemClient.performCultivationAction("awaken_physique", 0);
            }
        }
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 450.0F;
        float scaleY = (float) this.height / 265.0F;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int maxScroll = (activeTab == 0) ? Math.max(0, MERIDIANS.size() - 4) : Math.max(0, DAO_SKILLS.size() - 3);
        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
            return true;
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0 && minecraft != null && minecraft.player != null) {
            SystemData data = minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);

            int panelW = 430;
            int panelH = 248;
            int left = (width - panelW) / 2;
            int top = (height - panelH) / 2;

            // Botón Volver (esquina superior derecha)
            int backW = 55;
            int backH = 18;
            int backX = left + panelW - backW - 12;
            int backY = top + 7;
            if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH) {
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                onClose();
                return true;
            }

            // Botón Meditar / Cultivar Qi
            int medW = 95;
            int medH = 18;
            int medX = backX - medW - 8;
            int medY = top + 7;
            if (mouseX >= medX && mouseX <= medX + medW && mouseY >= medY && mouseY <= medY + medH) {
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                AwakeningSystemClient.performCultivationAction("toggle_meditation", 0);
                return true;
            }

            // Sección de la tarjeta de estado (cardTop = top + 28)
            int cardTop = top + 28;

            // Botón Físico Marcial (Fila 1 de la tarjeta, derecha)
            int phyW = 115;
            int phyH = 16;
            int phyX = left + panelW - phyW - 14;
            int phyY = cardTop + 5;
            boolean isUnawakened = data.murimPhysique() == null || data.murimPhysique().isEmpty();
            if (mouseX >= phyX && mouseX <= phyX + phyW && mouseY >= phyY && mouseY <= phyY + phyH) {
                if (isUnawakened && rollTicks <= 0) {
                    minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 1.2F));
                    rollTicks = 24;
                } else if (!isUnawakened) {
                    minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F));
                }
                return true;
            }

            // Botón Avanzar de Sub-nivel o Reino (Fila 2 de la tarjeta, derecha)
            int btnAdvW = 115;
            int btnAdvH = 18;
            int btnAdvX = left + panelW - btnAdvW - 14;
            int btnAdvY = cardTop + 24;
            if (mouseX >= btnAdvX && mouseX <= btnAdvX + btnAdvW && mouseY >= btnAdvY && mouseY <= btnAdvY + btnAdvH) {
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                if (data.cultivationSubLevel() < 9) {
                    AwakeningSystemClient.performCultivationAction("breakthrough_sublevel", 0);
                } else {
                    AwakeningSystemClient.performCultivationAction("advance_realm", 0);
                }
                return true;
            }

            // Pestañas Superiores (Meridianos vs Técnicas del Dao)
            int tabY = top + 77;
            if (mouseY >= tabY && mouseY < tabY + 16) {
                if (mouseX >= left + 12 && mouseX < left + 135) {
                    activeTab = 0;
                    scrollOffset = 0;
                    minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
                if (mouseX >= left + 138 && mouseX < left + 275) {
                    activeTab = 1;
                    scrollOffset = 0;
                    minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }

            // CONTENIDO SEGÚN LA PESTAÑA ACTIVA
            int listY = top + 96;
            if (activeTab == 0) {
                // Pestaña 0: Meridianos
                int visibleCount = 4;
                for (int i = 0; i < visibleCount; i++) {
                    int index = scrollOffset + i;
                    if (index >= MERIDIANS.size()) break;
                    MeridianInfo m = MERIDIANS.get(index);
                    boolean isOpen = data.isMeridianOpen(m.id());
                    int itemY = listY + i * 31;

                    if (!isOpen) {
                        int btnW = 85;
                        int btnH = 20;
                        int btnX = left + panelW - btnW - 18;
                        int btnY = itemY + 5;
                        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                            minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            AwakeningSystemClient.performCultivationAction("open_meridian", m.id());
                            return true;
                        }
                    }
                }
            } else {
                // Pestaña 1: Técnicas del Dao
                int visibleCount = 3;
                int realm = data.dantianRealm();
                for (int i = 0; i < visibleCount; i++) {
                    int index = scrollOffset + i;
                    if (index >= DAO_SKILLS.size()) break;
                    DaoSkillInfo s = DAO_SKILLS.get(index);
                    int itemY = listY + i * 40;
                    boolean isUnlocked = realm >= s.reqRealm();

                    if (isUnlocked) {
                        // Botón Asignar a [J]
                        int btnW1 = 70;
                        int btnH = 18;
                        int btnX1 = left + panelW - btnW1 - 65;
                        int btnY = itemY + 11;
                        if (mouseX >= btnX1 && mouseX <= btnX1 + btnW1 && mouseY >= btnY && mouseY <= btnY + btnH) {
                            minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                            AwakeningSystemClient.performCultivationAction("select_dao_skill", s.id());
                            return true;
                        }

                        // Botón Usar Inmediatamente
                        int btnW2 = 50;
                        int btnX2 = left + panelW - btnW2 - 12;
                        if (mouseX >= btnX2 && mouseX <= btnX2 + btnW2 && mouseY >= btnY && mouseY <= btnY + btnH) {
                            minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            AwakeningSystemClient.performCultivationAction("use_dao_skill", s.id());
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0x90000000, 0xB0000000);

        if (minecraft == null || minecraft.player == null) return;
        SystemData data = minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        graphics.pose().pushPose();
        if (scale < 1.0F) {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            graphics.pose().translate(cx, cy, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.pose().translate(-cx, -cy, 0.0F);
        }

        int panelW = 430;
        int panelH = 246;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        // Marco y Fondo Wuxia Oriental
        graphics.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0xFF8A6D3B);
        graphics.fill(left - 1, top - 1, left + panelW + 1, top + panelH + 1, 0xFF1C2228);
        graphics.fill(left, top, left + panelW, top + panelH, 0xFA0B1015);

        // Cabecera: Título Místico
        Component headerTitleComp = Component.literal("§6☯ §f" + title.getString() + " §6☯");
        graphics.drawString(font, headerTitleComp, left + 14, top + 11, 0xFFFFFFFF);

        // Botón Volver (esquina superior derecha)
        int backW = 55;
        int backH = 18;
        int backX = left + panelW - backW - 12;
        int backY = top + 7;
        boolean hoverBack = mX >= backX && mX <= backX + backW && mY >= backY && mY <= backY + backH;
        graphics.fill(backX, backY, backX + backW, backY + backH, hoverBack ? 0xFFC0392B : 0xFF922B21);
        drawBorder(graphics, backX, backY, backW, backH, hoverBack ? 0xFFFFFFFF : 0xFFE74C3C);
        graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.back"), backX + backW / 2, backY + 5, 0xFFFFFFFF);

        // Botón Meditar / Cultivar Qi
        int medW = 95;
        int medH = 18;
        int medX = backX - medW - 8;
        int medY = top + 7;
        boolean hoverMed = mX >= medX && mX <= medX + medW && mY >= medY && mY <= medY + medH;
        boolean isMeditating = minecraft.player.getVehicle() instanceof com.soybe.awakeningsystem.entity.MeditationSeatEntity;
        int medBg = isMeditating ? (hoverMed ? 0xFF1E8449 : 0xFF196F3D) : (hoverMed ? 0xFF2874A6 : 0xFF1B4F72);
        int medBorder = isMeditating ? 0xFF58D68D : 0xFF5DADE2;
        graphics.fill(medX, medY, medX + medW, medY + medH, medBg);
        drawBorder(graphics, medX, medY, medW, medH, medBorder);
        Component medText = isMeditating ? Component.translatable("awakening_system.screen.cultivation.stop_meditation") : Component.translatable("awakening_system.screen.cultivation.start_meditation");
        graphics.drawCenteredString(font, medText, medX + medW / 2, medY + 5, 0xFFFFFFFF);

        // Línea divisoria superior
        graphics.fill(left + 12, top + 26, left + panelW - 12, top + 27, 0xFF4A3B2C);

        // =========================================================================
        // TARJETA DE ESTADO SUPERIOR
        // =========================================================================
        int cardTop = top + 28;
        int cardH = 46;
        graphics.fill(left + 12, cardTop, left + panelW - 12, cardTop + cardH, 0xD0121920);
        drawBorder(graphics, left + 12, cardTop, panelW - 24, cardH, 0xFF3E4C59);

        // Fila 1 izquierda: Reino Dantian (sin desbordar hacia el botón de físico)
        int realm = data.dantianRealm();
        int subLevel = data.cultivationSubLevel();
        Component realmComp = Component.literal("§6Reino: ").append(SystemEvents.getDantianRealmComponent(realm));
        graphics.drawString(font, realmComp, left + 18, cardTop + 5, 0xFFFFFFFF);

        // Fila 2 izquierda: Sub-nivel actual y Densidad de Qi de la zona
        float qiDensity = SystemEvents.getZoneQiDensity(minecraft.player);
        Component infoComp = Component.literal("§eSub-nivel: §f" + subLevel + "/9 §7• §7Densidad: §a" + String.format(java.util.Locale.US, "%.1fx", qiDensity));
        graphics.drawString(font, infoComp, left + 18, cardTop + 18, 0xFF95A5A6);

        // Fila 3 izquierda: Barra de Progreso de Qi y Texto
        int qi = data.cultivationQi();
        int maxQi = data.maxCultivationQi();
        float qiRatio = Math.max(0.0F, Math.min(1.0F, (float) qi / Math.max(1, maxQi)));
        int barW = 120;
        int barH = 5;
        int barX = left + 18;
        int barY = cardTop + 33;
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF1C2833);
        graphics.fill(barX, barY, barX + (int) (barW * qiRatio), barY + barH, 0xFF50E4FF);
        Component qiComp = Component.literal("§b" + qi + "§7/§e" + maxQi + " Qi");
        graphics.drawString(font, qiComp, barX + barW + 6, cardTop + 31, 0xFF50E4FF);

        // Fila 1 derecha: Botón Físico Marcial / Despertar
        int phyW = 115;
        int phyH = 16;
        int phyX = left + panelW - phyW - 14;
        int phyY = cardTop + 5;
        boolean hoverPhy = mX >= phyX && mX <= phyX + phyW && mY >= phyY && mY <= phyY + phyH;
        String curPhys = data.murimPhysique();
        boolean isUnawakened = curPhys == null || curPhys.isEmpty();

        Component phyBtnText;
        int phyBg;
        int phyBorder;
        if (rollTicks > 0) {
            String[] rollNames = {"pure_yang", "nine_yin", "innate_sword", "celestial_dragon", "heavenly_demon", "primordial_chaos"};
            String rolling = rollNames[(rollTicks / 4) % rollNames.length];
            phyBtnText = Component.literal("§e⚡ ").append(Component.translatable("awakening_system.physique." + rolling + ".short"));
            phyBg = 0xFF7D3C98;
            phyBorder = 0xFFFFD700;
        } else if (isUnawakened) {
            phyBtnText = Component.translatable("awakening_system.screen.cultivation.awaken_btn");
            phyBg = hoverPhy ? 0xFFD4AC0D : 0xFFB7950B;
            phyBorder = hoverPhy ? 0xFFFFFFFF : 0xFFFFD700;
        } else {
            Component phyShort = Component.translatable("awakening_system.physique." + curPhys + ".short");
            phyBtnText = Component.literal("§d☯ ").append(phyShort);
            phyBg = hoverPhy ? 0xFF5B2C6F : 0xFF4A235A;
            phyBorder = hoverPhy ? 0xFFFFD700 : 0xFF8E44AD;
        }
        graphics.fill(phyX, phyY, phyX + phyW, phyY + phyH, phyBg);
        drawBorder(graphics, phyX, phyY, phyW, phyH, phyBorder);
        graphics.drawCenteredString(font, phyBtnText, phyX + phyW / 2, phyY + 4, 0xFFFFFFFF);

        // Fila 2 derecha: Botón Avanzar Sub-nivel o Reino (Escalado hasta Reino 9)
        int btnAdvW = 115;
        int btnAdvH = 18;
        int btnAdvX = left + panelW - btnAdvW - 14;
        int btnAdvY = cardTop + 24;
        boolean hoverAdv = mX >= btnAdvX && mX <= btnAdvX + btnAdvW && mY >= btnAdvY && mY <= btnAdvY + btnAdvH;

        Component advComp;
        int advColor;
        int advBorderCol;
        int advTextColor;
        if (subLevel < 9) {
            boolean canAdvanceSub = qi >= maxQi;
            advColor = canAdvanceSub ? (hoverAdv ? 0xFF2980B9 : 0xFF1F618D) : 0xFF34495E;
            advBorderCol = canAdvanceSub ? (hoverAdv ? 0xFFFFFFFF : 0xFF50E4FF) : 0xFF4A5568;
            advTextColor = canAdvanceSub ? 0xFFFFFFFF : 0xFFAAAAAA;
            advComp = Component.translatable("awakening_system.screen.cultivation.breakthrough_sublevel", subLevel + 1);
        } else if (realm < 9) {
            int reqMeridians = Math.min(12, (realm + 1) * 2);
            int reqQi = (realm + 1) * 150;
            boolean canBreakthrough = data.getOpenedMeridiansCount() >= reqMeridians && qi >= reqQi;
            advColor = canBreakthrough ? (hoverAdv ? 0xFFD4AC0D : 0xFFB7950B) : 0xFF4A4020;
            advBorderCol = canBreakthrough ? (hoverAdv ? 0xFFFFFFFF : 0xFFFFD700) : 0xFF665520;
            advTextColor = canBreakthrough ? 0xFFFFD700 : 0xFF888888;
            advComp = Component.translatable("awakening_system.screen.cultivation.breakthrough");
        } else {
            advColor = 0xFF2C3E50;
            advBorderCol = 0xFF4A5568;
            advTextColor = 0xFF7F8C8D;
            advComp = Component.translatable("awakening_system.screen.cultivation.max_realm");
        }
        graphics.fill(btnAdvX, btnAdvY, btnAdvX + btnAdvW, btnAdvY + btnAdvH, advColor);
        drawBorder(graphics, btnAdvX, btnAdvY, btnAdvW, btnAdvH, advBorderCol);
        graphics.drawCenteredString(font, advComp, btnAdvX + btnAdvW / 2, btnAdvY + 5, advTextColor);

        // =========================================================================
        // PESTAÑAS INTERACTIVAS (Meridianos vs Técnicas del Dao)
        // =========================================================================
        int tabY = top + 77;
        int tab0W = 120;
        int tab1W = 135;

        boolean hoverTab0 = mX >= left + 12 && mX < left + 12 + tab0W && mY >= tabY && mY < tabY + 16;
        boolean hoverTab1 = mX >= left + 138 && mX < left + 138 + tab1W && mY >= tabY && mY < tabY + 16;

        graphics.fill(left + 12, tabY, left + 12 + tab0W, tabY + 16, activeTab == 0 ? 0xFF1F618D : (hoverTab0 ? 0xFF2C3E50 : 0xFF17202A));
        drawBorder(graphics, left + 12, tabY, tab0W, 16, activeTab == 0 ? 0xFF50E4FF : 0xFF3E4C59);
        graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.tab_meridians"), left + 12 + tab0W / 2, tabY + 4, activeTab == 0 ? 0xFFFFFFFF : 0xFFAAAAAA);

        graphics.fill(left + 138, tabY, left + 138 + tab1W, tabY + 16, activeTab == 1 ? 0xFFB7950B : (hoverTab1 ? 0xFF4A4020 : 0xFF17202A));
        drawBorder(graphics, left + 138, tabY, tab1W, 16, activeTab == 1 ? 0xFFFFD700 : 0xFF3E4C59);
        graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.tab_dao"), left + 138 + tab1W / 2, tabY + 4, activeTab == 1 ? 0xFFFFFFFF : 0xFFAAAAAA);

        // Indicador de Calamidad Celestial si está activa
        if (data.isUnderTribulation()) {
            int strikesDone = data.getTribulationTotalStrikes() - data.getTribulationStrikesRemaining() + 1;
            Component tribComp = Component.translatable("awakening_system.screen.cultivation.tribulation_active", strikesDone, data.getTribulationTotalStrikes());
            graphics.drawString(font, tribComp, left + 280, tabY + 4, 0xFFFF5555);
        } else if (data.tribulationShieldTicks() > 0) {
            int sec = data.tribulationShieldTicks() / 20;
            Component shieldComp = Component.translatable("awakening_system.screen.cultivation.dao_shield", sec);
            graphics.drawString(font, shieldComp, left + 295, tabY + 4, 0xFF55FF88);
        }

        // =========================================================================
        // CONTENIDO SEGÚN LA PESTAÑA SELECCIONADA
        // =========================================================================
        int listY = top + 96;

        if (activeTab == 0) {
            // PESTAÑA 0: LISTA DE 12 MERIDIANOS
            int visibleCount = 4;
            for (int i = 0; i < visibleCount; i++) {
                int index = scrollOffset + i;
                if (index >= MERIDIANS.size()) break;
                MeridianInfo m = MERIDIANS.get(index);
                boolean isOpen = data.isMeridianOpen(m.id());
                int itemY = listY + i * 31;

                int bgCol = isOpen ? 0x60122822 : 0x5015181C;
                int borderCol = isOpen ? 0xFF27AE60 : 0xFF2C3E50;
                graphics.fill(left + 10, itemY, left + panelW - 10, itemY + 28, bgCol);
                graphics.fill(left + 10, itemY, left + 13, itemY + 28, borderCol);

                String prefix = isOpen ? "§a[✓] " : "§8[" + (m.id() + 1) + "] ";
                Component titleComp = Component.literal(prefix).append(m.name()).append(" §8(").append(m.organ()).append(")");
                graphics.drawString(font, titleComp, left + 18, itemY + 3, 0xFFFFD866);

                Component effComp = m.effect();
                if (font.width(effComp) > 280) {
                    String clipped = font.plainSubstrByWidth(effComp.getString(), 270) + "...";
                    graphics.drawString(font, Component.literal(clipped), left + 18, itemY + 15, 0xFFAAAAAA);
                } else {
                    graphics.drawString(font, effComp, left + 18, itemY + 15, 0xFFAAAAAA);
                }

                int btnW = 85;
                int btnH = 18;
                int btnX = left + panelW - btnW - 18;
                int btnY = itemY + 5;
                if (isOpen) {
                    graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.opened"), btnX + btnW / 2, btnY + 4, 0xFF50FA7B);
                } else {
                    boolean canAfford = data.cultivationQi() >= m.qiCost();
                    boolean hoverBtn = mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH;
                    int bColor = canAfford ? (hoverBtn ? 0xFF2980B9 : 0xFF1F618D) : 0xFF34495E;
                    graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, bColor);
                    drawBorder(graphics, btnX, btnY, btnW, btnH, hoverBtn && canAfford ? 0xFFFFFFFF : 0xFF50E4FF);
                    Component btnComp = canAfford ? Component.translatable("awakening_system.screen.cultivation.open_btn", m.qiCost()) : Component.translatable("awakening_system.screen.cultivation.qi_cost", m.qiCost());
                    graphics.drawCenteredString(font, btnComp, btnX + btnW / 2, btnY + 5, canAfford ? 0xFFFFFFFF : 0xFF888888);
                }
            }

            int scrollBarH = 124;
            int scrollBarY = listY;
            graphics.fill(left + panelW - 9, scrollBarY, left + panelW - 6, scrollBarY + scrollBarH, 0xFF1C2833);
            int maxScroll = Math.max(1, MERIDIANS.size() - visibleCount);
            int thumbH = Math.max(20, scrollBarH / (MERIDIANS.size() - visibleCount + 1));
            int thumbY = scrollBarY + (int) ((float) scrollOffset / maxScroll * (scrollBarH - thumbH));
            graphics.fill(left + panelW - 9, thumbY, left + panelW - 6, thumbY + thumbH, 0xFF50E4FF);

            graphics.fill(left + 10, top + panelH - 22, left + panelW - 10, top + panelH - 6, 0x8011161B);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.tip"), left + panelW / 2, top + panelH - 17, 0xFFFFE066);

        } else {
            // PESTAÑA 1: LISTA DE 5 TÉCNICAS DEL DAO
            int visibleCount = 3;
            for (int i = 0; i < visibleCount; i++) {
                int index = scrollOffset + i;
                if (index >= DAO_SKILLS.size()) break;
                DaoSkillInfo s = DAO_SKILLS.get(index);
                int itemY = listY + i * 40;
                boolean isUnlocked = realm >= s.reqRealm();
                boolean isEquipped = data.selectedDaoSkill() == s.id();

                int bgCol = isUnlocked ? (isEquipped ? 0x70302810 : 0x5015181C) : 0x40101014;
                int borderCol = isUnlocked ? (isEquipped ? 0xFFFFD700 : 0xFF2980B9) : 0xFF444444;
                graphics.fill(left + 10, itemY, left + panelW - 10, itemY + 37, bgCol);
                graphics.fill(left + 10, itemY, left + 13, itemY + 37, borderCol);

                Component prefix = isEquipped ? Component.translatable("awakening_system.screen.cultivation.dao_equipped_prefix") : Component.literal(isUnlocked ? "§a[✓] " : "§c[🔒] ");
                Component titleComp = Component.empty().append(prefix).append(Component.literal("§f")).append(s.name()).append(Component.literal(" §8| §b" + s.qiCost() + " Qi §8| §e" + s.cooldownSec() + "s CD"));
                graphics.drawString(font, titleComp, left + 18, itemY + 4, 0xFFFFFF);

                Component descComp = isUnlocked ? s.desc() : Component.translatable("awakening_system.screen.cultivation.dao_req_realm", s.reqRealm(), SystemEvents.getDantianRealmComponent(s.reqRealm()));
                String descText = descComp.getString();
                if (font.width(descText) > 270) {
                    descText = font.plainSubstrByWidth(descText, 260) + "...";
                }
                graphics.drawString(font, Component.literal(descText), left + 18, itemY + 16, isUnlocked ? 0xFFAAAAAA : 0xFF884444);

                if (isUnlocked) {
                    // Botón Asignar [J]
                    int btnW1 = 70;
                    int btnH = 18;
                    int btnX1 = left + panelW - btnW1 - 65;
                    int btnY = itemY + 10;
                    boolean hoverBtn1 = mX >= btnX1 && mX <= btnX1 + btnW1 && mY >= btnY && mY <= btnY + btnH;
                    int bCol1 = isEquipped ? 0xFF887010 : (hoverBtn1 ? 0xFFD4AC0D : 0xFF997A0A);
                    graphics.fill(btnX1, btnY, btnX1 + btnW1, btnY + btnH, bCol1);
                    drawBorder(graphics, btnX1, btnY, btnW1, btnH, isEquipped ? 0xFFFFD700 : (hoverBtn1 ? 0xFFFFFFFF : 0xFFE5C158));
                    Component assignText = isEquipped ? Component.translatable("awakening_system.screen.cultivation.dao_assigned") : Component.translatable("awakening_system.screen.cultivation.dao_assign");
                    graphics.drawCenteredString(font, assignText, btnX1 + btnW1 / 2, btnY + 5, 0xFFFFFFFF);

                    // Botón Usar
                    int btnW2 = 50;
                    int btnX2 = left + panelW - btnW2 - 12;
                    boolean canAfford = data.cultivationQi() >= s.qiCost();
                    boolean hoverBtn2 = mX >= btnX2 && mX <= btnX2 + btnW2 && mY >= btnY && mY <= btnY + btnH;
                    int bCol2 = canAfford ? (hoverBtn2 ? 0xFF27AE60 : 0xFF1E8449) : 0xFF34495E;
                    graphics.fill(btnX2, btnY, btnX2 + btnW2, btnY + btnH, bCol2);
                    drawBorder(graphics, btnX2, btnY, btnW2, btnH, hoverBtn2 && canAfford ? 0xFFFFFFFF : 0xFF2ECC71);
                    graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.dao_use"), btnX2 + btnW2 / 2, btnY + 5, canAfford ? 0xFFFFFFFF : 0xFF888888);
                } else {
                    int tagW = 100;
                    int tagX = left + panelW - tagW - 14;
                    graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.dao_locked"), tagX + tagW / 2, itemY + 14, 0xFFFF7777);
                }
            }

            int scrollBarH = 124;
            int scrollBarY = listY;
            graphics.fill(left + panelW - 9, scrollBarY, left + panelW - 6, scrollBarY + scrollBarH, 0xFF1C2833);
            int maxScroll = Math.max(1, DAO_SKILLS.size() - visibleCount);
            int thumbH = Math.max(20, scrollBarH / (DAO_SKILLS.size() - visibleCount + 1));
            int thumbY = scrollBarY + (int) ((float) scrollOffset / maxScroll * (scrollBarH - thumbH));
            graphics.fill(left + panelW - 9, thumbY, left + panelW - 6, thumbY + thumbH, 0xFFFFD700);

            graphics.fill(left + 10, top + panelH - 22, left + panelW - 10, top + panelH - 6, 0x8011161B);
            graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.dao_help"), left + panelW / 2, top + panelH - 17, 0xFFFFE066);
        }

        // Tooltip del Físico Marcial / Despertar
        if (hoverPhy && rollTicks <= 0) {
            List<Component> tooltip = new ArrayList<>();
            if (isUnawakened) {
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.title"));
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.desc1"));
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.desc2"));
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.rates_header"));
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.rate_yang"));
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.rate_yin"));
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.rate_sword"));
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.rate_dragon"));
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.rate_demon"));
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.rate_chaos"));
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("awakening_system.cultivation.judgment.click_prompt"));
            } else {
                tooltip.add(Component.translatable("awakening_system.cultivation.physique_header", Component.translatable("awakening_system.physique." + curPhys + ".name")));
                tooltip.add(Component.translatable("awakening_system.physique." + curPhys + ".desc").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("awakening_system.cultivation.permanent_header"));
                tooltip.add(Component.translatable("awakening_system.cultivation.permanent_desc1"));
                tooltip.add(Component.translatable("awakening_system.cultivation.permanent_desc2"));
            }
            graphics.renderComponentTooltip(font, tooltip, mX, mY);
        }

        graphics.pose().popPose();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
}
