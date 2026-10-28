package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class CultivationScreen extends Screen {
    private final Screen parentScreen;
    private int scrollOffset = 0;
    private int rollTicks = 0;
    private int activeTab = 0; // 0: Meridians (Visual Body), 1: Dao Skills

    private int selectedMeridian = 0;
    private int hoveredMeridian = -1;
    private float animTick = 0.0F;
    private int unlockEffectTicks = 0;
    private int lastUnlockedMeridian = -1;

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
        public Component shortName() {
            return Component.translatable("awakening_system.screen.cultivation.meridian_short_" + id);
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

    public record NodePos(int id, int relX, int relY) {}
    public record CircuitLine(int nodeA, int nodeB, int relX1, int relY1, int relX2, int relY2) {}

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

    // Spaced-out acupoints across the enlarged Minecraft player mannequin (relative to body center xc and top y0)
    private static final List<NodePos> NODES = List.of(
            new NodePos(0, -28, 46),  // 1. Pulmón (Brazo izquierdo superior)
            new NodePos(1, -28, 65),  // 2. Intestino Grueso (Brazo izquierdo medio)
            new NodePos(2, 11, 48),   // 3. Estómago (Pecho derecho)
            new NodePos(3, -12, 68),  // 4. Bazo (Costillas inferiores izquierdas)
            new NodePos(4, -11, 48),  // 5. Corazón (Pecho izquierdo)
            new NodePos(5, -28, 82),  // 6. Intestino Delgado (Mano izquierda)
            new NodePos(6, -9, 114),  // 7. Vejiga (Rodilla izquierda)
            new NodePos(7, -11, 80),  // 8. Riñón (Flanco inferior izquierdo)
            new NodePos(8, 0, 44),    // 9. Pericardio (Pecho central superior)
            new NodePos(9, 28, 46),   // 10. Triple Calentador (Brazo derecho)
            new NodePos(10, 11, 18),  // 11. Vesícula Biliar (Sien derecha)
            new NodePos(11, 12, 68)   // 12. Hígado (Costillas inferiores derechas)
    );

    // Energetic circuit lines connecting the meridian network across the Minecraft body
    private static final List<CircuitLine> CIRCUIT_LINES = List.of(
            // Head circuit (Third eye to temple and throat)
            new CircuitLine(10, 10, 0, 14, 11, 18),
            new CircuitLine(10, 8, 0, 14, 0, 34),
            new CircuitLine(8, 8, 0, 34, 0, 44),
            // Upper torso / chest circuit
            new CircuitLine(8, 4, 0, 44, -11, 48),
            new CircuitLine(8, 2, 0, 44, 11, 48),
            new CircuitLine(4, 0, -11, 48, -28, 46),
            new CircuitLine(2, 9, 11, 48, 28, 46),
            new CircuitLine(8, 8, 0, 44, 0, 58), // Pericardio to Middle Dantian
            // Left arm circuit
            new CircuitLine(0, 1, -28, 46, -28, 65),
            new CircuitLine(1, 5, -28, 65, -28, 82),
            // Right arm circuit
            new CircuitLine(9, 9, 28, 46, 28, 65),
            new CircuitLine(9, 9, 28, 65, 28, 82),
            // Core & Lower Dantian circuit
            new CircuitLine(8, 3, 0, 58, -12, 68),
            new CircuitLine(8, 11, 0, 58, 12, 68),
            new CircuitLine(3, 7, -12, 68, -11, 80),
            new CircuitLine(11, 7, 12, 68, 0, 80),
            new CircuitLine(7, 7, -11, 80, 0, 80), // Riñón to Lower Dantian
            // Legs circuit
            new CircuitLine(7, 6, 0, 80, -9, 114),
            new CircuitLine(6, 6, -9, 114, -9, 138),
            new CircuitLine(7, 11, 0, 80, 9, 114),
            new CircuitLine(11, 7, 9, 114, 9, 138)
    );

    private static final List<DaoSkillInfo> DAO_SKILLS = List.of(
            new DaoSkillInfo(0, 1, 20, 7),
            new DaoSkillInfo(1, 2, 35, 10),
            new DaoSkillInfo(2, 3, 50, 18),
            new DaoSkillInfo(3, 4, 75, 25),
            new DaoSkillInfo(4, 5, 45, 8),
            new DaoSkillInfo(5, 3, 30, 12),
            new DaoSkillInfo(6, 4, 65, 20),
            new DaoSkillInfo(7, 5, 80, 30)
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
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }

        if (activeTab == 0 && minecraft != null && minecraft.player != null) {
            SystemData data = minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                selectedMeridian = (selectedMeridian + 11) % 12;
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                selectedMeridian = (selectedMeridian + 1) % 12;
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                selectedMeridian = (selectedMeridian + 10) % 12;
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                selectedMeridian = (selectedMeridian + 2) % 12;
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
                if (!data.isMeridianOpen(selectedMeridian)) {
                    int cost = (selectedMeridian + 1) * 35;
                    if (data.cultivationQi() >= cost) {
                        unlockEffectTicks = 20;
                        lastUnlockedMeridian = selectedMeridian;
                        minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                        AwakeningSystemClient.performCultivationAction("open_meridian", selectedMeridian);
                        return true;
                    }
                }
            }
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
        animTick += 0.5F;
        if (unlockEffectTicks > 0) {
            unlockEffectTicks--;
        }
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
        float scaleX = (float) this.width / 465.0F;
        float scaleY = (float) this.height / 270.0F;
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
        if (activeTab == 1) {
            int maxScroll = Math.max(0, DAO_SKILLS.size() - 3);
            if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
                return true;
            } else if (scrollY < 0) {
                scrollOffset = Math.min(maxScroll, scrollOffset + 1);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0 && minecraft != null && minecraft.player != null) {
            SystemData data = minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);

            int panelW = 460;
            int panelH = 260;
            int left = (width - panelW) / 2;
            int top = (height - panelH) / 2;

            int backW = 55;
            int backH = 18;
            int backX = left + panelW - backW - 12;
            int backY = top + 7;
            if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH) {
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                onClose();
                return true;
            }

            int medW = 95;
            int medH = 18;
            int medX = backX - medW - 8;
            int medY = top + 7;
            if (mouseX >= medX && mouseX <= medX + medW && mouseY >= medY && mouseY <= medY + medH) {
                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                AwakeningSystemClient.performCultivationAction("toggle_meditation", 0);
                return true;
            }

            int cardTop = top + 28;

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

            int tabY = top + 77;
            if (mouseY >= tabY && mouseY < tabY + 16) {
                if (mouseX >= left + 12 && mouseX < left + 145) {
                    activeTab = 0;
                    minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
                if (mouseX >= left + 148 && mouseX < left + 285) {
                    activeTab = 1;
                    scrollOffset = 0;
                    minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }

            if (activeTab == 0) {
                int xc = left + panelW / 2;
                float breathe = (float) Math.sin(animTick * 0.08F) * 0.8F;
                int y0 = top + 102 + Math.round(breathe);

                // 1. Direct clicks on the central Minecraft body acupoint nodes
                for (NodePos node : NODES) {
                    int nx = xc + node.relX();
                    int ny = y0 + node.relY();
                    if (Math.abs(mouseX - nx) <= 7 && Math.abs(mouseY - ny) <= 7) {
                        minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.3F));
                        if (selectedMeridian == node.id() && !data.isMeridianOpen(node.id())) {
                            int cost = (node.id() + 1) * 35;
                            if (data.cultivationQi() >= cost) {
                                unlockEffectTicks = 20;
                                lastUnlockedMeridian = node.id();
                                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                                AwakeningSystemClient.performCultivationAction("open_meridian", node.id());
                            } else {
                                minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F));
                            }
                        } else {
                            selectedMeridian = node.id();
                        }
                        return true;
                    }
                }

                // 2. Clicks on the 12 quick selector pills in the left column
                int leftPillX = left + 18;
                int leftPillY = top + 158;
                for (int i = 0; i < 12; i++) {
                    int col = i % 2;
                    int row = i / 2;
                    int pillX = leftPillX + col * 64;
                    int pillY = leftPillY + row * 15;
                    if (mouseX >= pillX && mouseX <= pillX + 60 && mouseY >= pillY && mouseY <= pillY + 13) {
                        selectedMeridian = i;
                        minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                        return true;
                    }
                }

                // 3. Click on the unlock button in the right inspection card
                int btnX = left + 318;
                int btnY = top + 224;
                int btnW = 122;
                int btnH = 20;
                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    boolean isOpen = data.isMeridianOpen(selectedMeridian);
                    if (!isOpen) {
                        int cost = (selectedMeridian + 1) * 35;
                        if (data.cultivationQi() >= cost) {
                            unlockEffectTicks = 20;
                            lastUnlockedMeridian = selectedMeridian;
                            minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                            AwakeningSystemClient.performCultivationAction("open_meridian", selectedMeridian);
                        } else {
                            minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F));
                        }
                        return true;
                    }
                }
            } else {
                int listY = top + 96;
                int visibleCount = 3;
                int realm = data.dantianRealm();
                for (int i = 0; i < visibleCount; i++) {
                    int index = scrollOffset + i;
                    if (index >= DAO_SKILLS.size()) break;
                    DaoSkillInfo s = DAO_SKILLS.get(index);
                    int itemY = listY + i * 40;
                    boolean isUnlocked = realm >= s.reqRealm();

                    if (isUnlocked) {
                        int btnW1 = 70;
                        int btnH = 18;
                        int btnX1 = left + panelW - btnW1 - 65;
                        int btnY = itemY + 11;
                        if (mouseX >= btnX1 && mouseX <= btnX1 + btnW1 && mouseY >= btnY && mouseY <= btnY + btnH) {
                            minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                            AwakeningSystemClient.performCultivationAction("select_dao_skill", s.id());
                            return true;
                        }

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

        int panelW = 460;
        int panelH = 260;
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        graphics.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0xFF8A6D3B);
        graphics.fill(left - 1, top - 1, left + panelW + 1, top + panelH + 1, 0xFF1C2228);
        graphics.fill(left, top, left + panelW, top + panelH, 0xFA0B1015);

        Component headerTitleComp = Component.literal("§6☯ §f" + title.getString() + " §6☯");
        graphics.drawString(font, headerTitleComp, left + 14, top + 11, 0xFFFFFFFF);

        int backW = 55;
        int backH = 18;
        int backX = left + panelW - backW - 12;
        int backY = top + 7;
        boolean hoverBack = mX >= backX && mX <= backX + backW && mY >= backY && mY <= backY + backH;
        graphics.fill(backX, backY, backX + backW, backY + backH, hoverBack ? 0xFFC0392B : 0xFF922B21);
        drawBorder(graphics, backX, backY, backW, backH, hoverBack ? 0xFFFFFFFF : 0xFFE74C3C);
        graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.back"), backX + backW / 2, backY + 5, 0xFFFFFFFF);

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

        graphics.fill(left + 12, top + 26, left + panelW - 12, top + 27, 0xFF4A3B2C);

        int cardTop = top + 28;
        int cardH = 46;
        graphics.fill(left + 12, cardTop, left + panelW - 12, cardTop + cardH, 0xD0121920);
        drawBorder(graphics, left + 12, cardTop, panelW - 24, cardH, 0xFF3E4C59);

        int realm = data.dantianRealm();
        int subLevel = data.cultivationSubLevel();
        Component realmComp = Component.translatable("awakening_system.screen.cultivation.realm_label", SystemEvents.getDantianRealmComponent(realm));
        graphics.drawString(font, realmComp, left + 18, cardTop + 5, 0xFFFFFFFF);

        float qiDensity = SystemEvents.getZoneQiDensity(minecraft.player);
        Component infoComp = Component.translatable("awakening_system.screen.cultivation.sublevel_density", subLevel, String.format(java.util.Locale.US, "%.1fx", qiDensity));
        graphics.drawString(font, infoComp, left + 18, cardTop + 18, 0xFF95A5A6);

        int qi = data.cultivationQi();
        int maxQi = data.maxCultivationQi();
        float qiRatio = Math.max(0.0F, Math.min(1.0F, (float) qi / Math.max(1, maxQi)));
        int barW = 130;
        int barH = 5;
        int barX = left + 18;
        int barY = cardTop + 33;
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF1C2833);
        graphics.fill(barX, barY, barX + (int) (barW * qiRatio), barY + barH, 0xFF50E4FF);
        Component qiComp = Component.literal("§b" + qi + "§7/§e" + maxQi + " Qi");
        graphics.drawString(font, qiComp, barX + barW + 6, cardTop + 31, 0xFF50E4FF);

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

        int tabY = top + 77;
        int tab0W = 130;
        int tab1W = 135;

        boolean hoverTab0 = mX >= left + 12 && mX < left + 12 + tab0W && mY >= tabY && mY < tabY + 16;
        boolean hoverTab1 = mX >= left + 148 && mX < left + 148 + tab1W && mY >= tabY && mY < tabY + 16;

        graphics.fill(left + 12, tabY, left + 12 + tab0W, tabY + 16, activeTab == 0 ? 0xFF1F618D : (hoverTab0 ? 0xFF2C3E50 : 0xFF17202A));
        drawBorder(graphics, left + 12, tabY, tab0W, 16, activeTab == 0 ? 0xFF50E4FF : 0xFF3E4C59);
        graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.tab_meridians"), left + 12 + tab0W / 2, tabY + 4, activeTab == 0 ? 0xFFFFFFFF : 0xFFAAAAAA);

        graphics.fill(left + 148, tabY, left + 148 + tab1W, tabY + 16, activeTab == 1 ? 0xFFB7950B : (hoverTab1 ? 0xFF4A4020 : 0xFF17202A));
        drawBorder(graphics, left + 148, tabY, tab1W, 16, activeTab == 1 ? 0xFFFFD700 : 0xFF3E4C59);
        graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.tab_dao"), left + 148 + tab1W / 2, tabY + 4, activeTab == 1 ? 0xFFFFFFFF : 0xFFAAAAAA);

        if (data.isUnderTribulation()) {
            int strikesDone = data.getTribulationTotalStrikes() - data.getTribulationStrikesRemaining() + 1;
            Component tribComp = Component.translatable("awakening_system.screen.cultivation.tribulation_active", strikesDone, data.getTribulationTotalStrikes());
            graphics.drawString(font, tribComp, left + 290, tabY + 4, 0xFFFF5555);
        } else if (data.tribulationShieldTicks() > 0) {
            int sec = data.tribulationShieldTicks() / 20;
            Component shieldComp = Component.translatable("awakening_system.screen.cultivation.dao_shield", sec);
            graphics.drawString(font, shieldComp, left + 305, tabY + 4, 0xFF55FF88);
        }

        if (activeTab == 0) {
            renderMeridianMannequinTab(graphics, left, top, panelW, panelH, data, mX, mY);
        } else {
            renderDaoSkillsTab(graphics, left, top, panelW, panelH, data, realm, mX, mY);
        }

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

    private void renderMeridianMannequinTab(GuiGraphics g, int left, int top, int panelW, int panelH, SystemData data, int mX, int mY) {
        int openedCount = data.getOpenedMeridiansCount();

        // 1. LEFT COLUMN: Network Status & Quick Selector Pills
        int colLeftX = left + 12;
        int colLeftY = top + 96;
        int colLeftW = 138;
        int colLeftH = panelH - 104;

        g.fill(colLeftX, colLeftY, colLeftX + colLeftW, colLeftY + colLeftH, 0xC8101720);
        drawBorder(g, colLeftX, colLeftY, colLeftW, colLeftH, 0xFF2A3E50);

        // Header: Line 1 - Qi Circulation Title
        Component circTitle = Component.literal("§6☯ ").append(Component.translatable("awakening_system.screen.cultivation.circulation_title"));
        g.drawString(font, circTitle, colLeftX + 8, colLeftY + 6, 0xFFFFFFFF);

        // Header: Line 2 - Meridian count (0/12 Meridians)
        Component countComp = Component.translatable("awakening_system.screen.cultivation.meridian_count", openedCount);
        g.drawString(font, countComp, colLeftX + 8, colLeftY + 17, 0xFF50E4FF);

        // Progress bar
        int barW = colLeftW - 16;
        int barH = 4;
        int barX = colLeftX + 8;
        int barY = colLeftY + 28;
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF1C2833);
        int fillW = (int) (barW * (openedCount / 12.0F));
        int barCol = (openedCount == 12) ? 0xFFFFD700 : 0xFF50E4FF;
        g.fill(barX, barY, barX + fillW, barY + barH, barCol);

        // Active Circulation Badge
        int badgeY = colLeftY + 36;
        if (openedCount == 12) {
            g.drawString(font, Component.literal("§6★ ").append(Component.translatable("awakening_system.screen.cultivation.circulation_grand")), colLeftX + 8, badgeY, 0xFFFFD700);
            g.drawString(font, Component.translatable("awakening_system.screen.cultivation.circulation_grand_bonus"), colLeftX + 8, badgeY + 10, 0xFF50FA7B);
        } else if (openedCount >= 6) {
            g.drawString(font, Component.literal("§b✦ ").append(Component.translatable("awakening_system.screen.cultivation.circulation_minor")), colLeftX + 8, badgeY, 0xFF50E4FF);
            g.drawString(font, Component.translatable("awakening_system.screen.cultivation.circulation_minor_bonus"), colLeftX + 8, badgeY + 10, 0xFF50FA7B);
        } else {
            g.drawString(font, Component.literal("§7○ ").append(Component.translatable("awakening_system.screen.cultivation.circulation_none")), colLeftX + 8, badgeY, 0xFF888888);
            g.drawString(font, Component.translatable("awakening_system.screen.cultivation.circulation_none_desc"), colLeftX + 8, badgeY + 10, 0xFF666666);
        }

        // Separator
        g.fill(colLeftX + 8, colLeftY + 58, colLeftX + colLeftW - 8, colLeftY + 59, 0xFF243342);

        // 12 Quick Selector Pills (2 columns x 6 rows)
        int pillStartX = colLeftX + 6;
        int pillStartY = colLeftY + 62;
        for (int i = 0; i < 12; i++) {
            int col = i % 2;
            int row = i / 2;
            int pillX = pillStartX + col * 64;
            int pillY = pillStartY + row * 15;
            int pillW = 60;
            int pillH = 13;

            boolean isOpen = data.isMeridianOpen(i);
            boolean isSel = (selectedMeridian == i);
            boolean isHov = mX >= pillX && mX <= pillX + pillW && mY >= pillY && mY <= pillY + pillH;

            int bg = isSel ? 0xFF245070 : (isHov ? 0xFF1C3246 : (isOpen ? 0xFF14241C : 0xFF131A22));
            int border = isSel ? 0xFFFFD700 : (isHov ? 0xFF50E4FF : (isOpen ? 0xFF2ECC71 : 0xFF243342));
            g.fill(pillX, pillY, pillX + pillW, pillY + pillH, bg);
            drawBorder(g, pillX, pillY, pillW, pillH, border);

            String icon = isOpen ? "§a✓" : "§8" + (i + 1);
            Component shortName = MERIDIANS.get(i).shortName();
            Component pillComp = Component.literal(icon + " §f").append(shortName);
            g.drawString(font, pillComp, pillX + 3, pillY + 3, isSel ? 0xFFFFD700 : 0xFFCCCCCC);
        }

        // 2. CENTER AREA: Minecraft Character Spiritual Mannequin
        int xc = left + panelW / 2;
        int y0 = top + 102;
        renderMinecraftMannequin(g, xc, y0, data, animTick, mX, mY);

        // 3. RIGHT COLUMN: Selected Meridian Inspection & Action Card
        int colRightX = left + panelW - 150;
        int colRightY = top + 96;
        int colRightW = 138;
        int colRightH = panelH - 104;

        g.fill(colRightX, colRightY, colRightX + colRightW, colRightY + colRightH, 0xC8101720);
        drawBorder(g, colRightX, colRightY, colRightW, colRightH, 0xFF2A3E50);

        MeridianInfo m = MERIDIANS.get(selectedMeridian);
        boolean isSelectedOpen = data.isMeridianOpen(selectedMeridian);
        int cost = (selectedMeridian + 1) * 35;
        boolean canAfford = data.cultivationQi() >= cost;

        // Title: [#X] Meridian Name
        Component selTitle = Component.literal("§6[#" + (selectedMeridian + 1) + "] ").append(m.name());
        g.drawString(font, selTitle, colRightX + 8, colRightY + 6, 0xFFFFFFFF);

        // Organ channel
        g.drawString(font, Component.literal("§b").append(m.organ()), colRightX + 8, colRightY + 18, 0xFF50E4FF);

        // Status badge
        Component statusComp = isSelectedOpen ? Component.translatable("awakening_system.screen.cultivation.meridian_status_open") : Component.translatable("awakening_system.screen.cultivation.meridian_status_locked");
        int statusCol = isSelectedOpen ? 0xFF50FA7B : 0xFFFF5555;
        g.drawString(font, statusComp, colRightX + 8, colRightY + 30, statusCol);

        // Separator
        g.fill(colRightX + 8, colRightY + 42, colRightX + colRightW - 8, colRightY + 43, 0xFF243342);

        // Permanent Effect label & description lines
        g.drawString(font, Component.translatable("awakening_system.screen.cultivation.meridian_permanent_effect"), colRightX + 8, colRightY + 47, 0xFFFFD700);
        List<FormattedCharSequence> effectLines = font.split(m.effect(), colRightW - 16);
        int effY = colRightY + 59;
        for (int i = 0; i < Math.min(3, effectLines.size()); i++) {
            g.drawString(font, effectLines.get(i), colRightX + 8, effY + (i * 10), 0xFFAAAAAA);
        }

        // Cost & Player Qi Info
        int costY = colRightY + 98;
        Component costComp = Component.translatable("awakening_system.screen.cultivation.meridian_cost", cost);
        g.drawString(font, costComp, colRightX + 8, costY, isSelectedOpen ? 0xFF888888 : (canAfford ? 0xFF50FA7B : 0xFFFF7777));

        Component yourQiComp = Component.translatable("awakening_system.screen.cultivation.meridian_your_qi", data.cultivationQi());
        g.drawString(font, yourQiComp, colRightX + 8, costY + 11, 0xFF50E4FF);

        // Tactile Action Button
        int btnW = colRightW - 16;
        int btnH = 20;
        int btnX = colRightX + 8;
        int btnY = colRightY + colRightH - btnH - 8;
        boolean hoverBtn = mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH;

        if (isSelectedOpen) {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF145A32);
            drawBorder(g, btnX, btnY, btnW, btnH, 0xFF27AE60);
            Component openedText = Component.literal("§a✓ ").append(Component.translatable("awakening_system.screen.cultivation.opened"));
            g.drawCenteredString(font, openedText, btnX + btnW / 2, btnY + 6, 0xFFFFFFFF);
        } else if (canAfford) {
            int bBg = hoverBtn ? 0xFF2980B9 : 0xFF1F618D;
            int bBorder = hoverBtn ? 0xFFFFFFFF : 0xFF50E4FF;
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, bBg);
            drawBorder(g, btnX, btnY, btnW, btnH, bBorder);
            Component unlockText = Component.translatable("awakening_system.screen.cultivation.meridian_unlock_btn", cost);
            g.drawCenteredString(font, unlockText, btnX + btnW / 2, btnY + 6, 0xFFFFFFFF);
        } else {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF2C3E50);
            drawBorder(g, btnX, btnY, btnW, btnH, 0xFF3E4C59);
            Component lockedText = Component.translatable("awakening_system.screen.cultivation.meridian_not_enough", cost);
            g.drawCenteredString(font, lockedText, btnX + btnW / 2, btnY + 6, 0xFF888888);
        }
    }

    private void renderMinecraftMannequin(GuiGraphics g, int xc, int y0, SystemData data, float animTick, int mX, int mY) {
        float breathe = (float) Math.sin(animTick * 0.08F) * 0.8F;
        int curY0 = y0 + Math.round(breathe);

        int openedCount = data.getOpenedMeridiansCount();
        boolean isGrand = (openedCount == 12);
        boolean isMinor = (openedCount >= 6);

        int bodyBorderCol = isGrand ? 0xFFFFD700 : (isMinor ? 0xFF50E4FF : 0xFF2A6F97);
        int bodyFillCol = 0xD80A1118;

        // 1. Spiritual Cultivation Array / Background aura
        drawSpiritualArray(g, xc, curY0 + 64, animTick, openedCount);

        // 2. Active circuit pathways (rendered beneath the body outline and nodes)
        for (CircuitLine line : CIRCUIT_LINES) {
            boolean flowA = line.nodeA() < 0 || data.isMeridianOpen(line.nodeA());
            boolean flowB = line.nodeB() < 0 || data.isMeridianOpen(line.nodeB());
            boolean isFlowing = flowA && flowB;
            drawEnergyLine(g, xc + line.relX1(), curY0 + line.relY1(), xc + line.relX2(), curY0 + line.relY2(), isFlowing, animTick);
        }

        // 3. Minecraft Character Geometry (Proportioned Steve mannequin: Head 32x32, Torso 36x50, Arms 16x50, Legs 17x54)
        // Head (32 x 32)
        int headX = xc - 16;
        int headY = curY0;
        g.fill(headX, headY, headX + 32, headY + 32, bodyFillCol);
        drawBorder(g, headX, headY, 32, 32, bodyBorderCol);
        // Ethereal eyes
        g.fill(headX + 6, headY + 18, headX + 12, headY + 20, 0x6050E4FF);
        g.fill(headX + 20, headY + 18, headX + 26, headY + 20, 0x6050E4FF);

        // Torso (36 x 50)
        int torsoX = xc - 18;
        int torsoY = curY0 + 34;
        g.fill(torsoX, torsoY, torsoX + 36, torsoY + 50, bodyFillCol);
        drawBorder(g, torsoX, torsoY, 36, 50, bodyBorderCol);
        // Central meridian spinal line
        g.fill(xc, torsoY + 2, xc + 1, torsoY + 48, 0x3050E4FF);

        // Left Arm (16 x 50)
        int lArmX = xc - 36;
        int lArmY = curY0 + 34;
        g.fill(lArmX, lArmY, lArmX + 16, lArmY + 50, bodyFillCol);
        drawBorder(g, lArmX, lArmY, 16, 50, bodyBorderCol);
        g.fill(lArmX + 8, lArmY + 2, lArmX + 9, lArmY + 48, 0x2550E4FF);

        // Right Arm (16 x 50)
        int rArmX = xc + 20;
        int rArmY = curY0 + 34;
        g.fill(rArmX, rArmY, rArmX + 16, rArmY + 50, bodyFillCol);
        drawBorder(g, rArmX, rArmY, 16, 50, bodyBorderCol);
        g.fill(rArmX + 7, rArmY + 2, rArmX + 8, rArmY + 48, 0x2550E4FF);

        // Left Leg (17 x 54)
        int lLegX = xc - 18;
        int lLegY = curY0 + 86;
        g.fill(lLegX, lLegY, lLegX + 17, lLegY + 54, bodyFillCol);
        drawBorder(g, lLegX, lLegY, 17, 54, bodyBorderCol);
        g.fill(lLegX + 8, lLegY + 2, lLegX + 9, lLegY + 52, 0x2550E4FF);

        // Right Leg (17 x 54)
        int rLegX = xc + 1;
        int rLegY = curY0 + 86;
        g.fill(rLegX, rLegY, rLegX + 17, rLegY + 54, bodyFillCol);
        drawBorder(g, rLegX, rLegY, 17, 54, bodyBorderCol);
        g.fill(rLegX + 8, rLegY + 2, rLegX + 9, rLegY + 52, 0x2550E4FF);

        // 4. Draw The Three Dantians (Shang: Head, Zhong: Solar Plexus, Xia: Lower Abdomen)
        drawDantian(g, xc, curY0 + 14, 0, animTick); // Shang Dantian (Purple)
        drawDantian(g, xc, curY0 + 58, 1, animTick); // Zhong Dantian (Red)
        drawDantian(g, xc, curY0 + 80, 2, animTick); // Xia Dantian (Cyan)

        // 5. Draw Acupoint Nodes
        hoveredMeridian = -1;
        for (NodePos node : NODES) {
            int nx = xc + node.relX();
            int ny = curY0 + node.relY();
            boolean isOpen = data.isMeridianOpen(node.id());
            boolean isSelected = (selectedMeridian == node.id());
            boolean isHovered = (Math.abs(mX - nx) <= 7 && Math.abs(mY - ny) <= 7);
            if (isHovered) hoveredMeridian = node.id();

            drawAcupointNode(g, nx, ny, node.id(), isOpen, isSelected, isHovered, animTick, data, xc);
        }

        // 6. Unlock shockwave animation
        if (unlockEffectTicks > 0 && lastUnlockedMeridian >= 0 && lastUnlockedMeridian < NODES.size()) {
            NodePos unNode = NODES.get(lastUnlockedMeridian);
            int unX = xc + unNode.relX();
            int unY = curY0 + unNode.relY();
            int pulseR = (20 - unlockEffectTicks) * 2;
            int alpha = Math.max(0, Math.min(255, unlockEffectTicks * 12));
            int waveCol = (alpha << 24) | 0x00FFD700;
            drawExpandingRing(g, unX, unY, pulseR, waveCol);
        }
    }

    private void drawSpiritualArray(GuiGraphics g, int xc, int yc, float animTick, int openedCount) {
        int auraCol = (openedCount == 12) ? 0x20FFD700 : ((openedCount >= 6) ? 0x1850E4FF : 0x102A6F97);
        int ringCol = (openedCount == 12) ? 0x50FFD700 : ((openedCount >= 6) ? 0x3850E4FF : 0x242A6F97);

        g.fillGradient(xc - 52, yc - 70, xc + 52, yc + 70, auraCol, 0x00051018);

        // Celestial compass axes
        g.fill(xc - 48, yc, xc + 48, yc + 1, ringCol);
        g.fill(xc, yc - 68, xc + 1, yc + 68, ringCol);

        // Outer geometric array frame
        drawBorder(g, xc - 48, yc - 68, 96, 136, ringCol);
    }

    private void drawEnergyLine(GuiGraphics g, int x1, int y1, int x2, int y2, boolean isFlowing, float animTick) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;

        int baseCol = isFlowing ? 0x9050E4FF : 0x224A5A6A;
        int sparkIdx = isFlowing ? ((int) (animTick * 1.5F) % steps) : -10;

        for (int i = 0; i <= steps; i += 2) {
            int px = x1 + (dx * i) / steps;
            int py = y1 + (dy * i) / steps;

            int col = baseCol;
            if (isFlowing && Math.abs(i - sparkIdx) <= 2) {
                col = 0xFFFFFFFF; // Traveling Qi pulse particle
            }
            g.fill(px, py, px + 1, py + 1, col);
        }
    }

    private void drawAcupointNode(GuiGraphics g, int x, int y, int id, boolean isOpen, boolean isSelected, boolean isHovered, float animTick, SystemData data, int xc) {
        int cost = (id + 1) * 35;
        boolean canAfford = data.cultivationQi() >= cost;

        // Pulse ring on focus
        if (isSelected || isHovered) {
            float pulse = (float) Math.sin(animTick * 0.25F) * 1.5F;
            int r = Math.round(5 + pulse);
            g.fill(x - r, y - r, x + r + 1, y + r + 1, isSelected ? 0x60FFD700 : 0x5050E4FF);
            drawBorder(g, x - r, y - r, (r * 2) + 1, (r * 2) + 1, isSelected ? 0xFFFFD700 : 0xFFFFFFFF);
        }

        // Diamond / Gem acupoint node
        int r = 3;
        int coreCol;
        int borderCol;
        if (isOpen) {
            coreCol = 0xFF50E4FF;
            borderCol = 0xFF00AAFF;
        } else if (canAfford) {
            coreCol = 0xFFE67E22;
            borderCol = 0xFFF39C12;
        } else {
            coreCol = 0xFF2C3E50;
            borderCol = 0xFF1C2833;
        }

        g.fill(x - r, y - r, x + r + 1, y + r + 1, coreCol);
        drawBorder(g, x - r, y - r, (r * 2) + 1, (r * 2) + 1, borderCol);

        if (isOpen) {
            g.fill(x, y, x + 1, y + 1, 0xFFFFFFFF);
        } else if (canAfford) {
            g.fill(x, y, x + 1, y + 1, 0xFFFFD700);
        }

        // Only draw the floating number badge when selected or hovered (avoids cluttering the body)
        if (isSelected || isHovered) {
            String badge = "#" + (id + 1);
            int badgeW = font.width(badge) + 4;
            int badgeX = (x < xc) ? (x - badgeW - 3) : (x + 5);
            int badgeY = y - 5;
            g.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 10, 0xDC0B1218);
            drawBorder(g, badgeX, badgeY, badgeW, 10, isSelected ? 0xFFFFD700 : 0xFF50E4FF);
            g.drawString(font, badge, badgeX + 2, badgeY + 1, isSelected ? 0xFFFFD700 : 0xFF50E4FF, false);
        }
    }

    private void drawDantian(GuiGraphics g, int x, int y, int type, float animTick) {
        float pulse = (float) Math.sin(animTick * 0.15F + type) * 1.0F;
        int r = Math.round(3 + pulse);
        int coreCol = (type == 0) ? 0xFFBB86FC : ((type == 1) ? 0xFFFF5555 : 0xFF50E4FF);
        int haloCol = (type == 0) ? 0x40BB86FC : ((type == 1) ? 0x40FF5555 : 0x4050E4FF);

        g.fill(x - r - 2, y - r - 2, x + r + 3, y + r + 3, haloCol);
        g.fill(x, y - r, x + 1, y + r + 1, coreCol);
        g.fill(x - r, y, x + r + 1, y + 1, coreCol);
        g.fill(x - 1, y - 1, x + 2, y + 2, 0xFFFFFFFF);
    }

    private void drawExpandingRing(GuiGraphics g, int cx, int cy, int radius, int color) {
        if (radius <= 0) return;
        g.fill(cx - radius, cy - radius, cx + radius + 1, cy - radius + 1, color);
        g.fill(cx - radius, cy + radius, cx + radius + 1, cy + radius + 1, color);
        g.fill(cx - radius, cy - radius, cx - radius + 1, cy + radius + 1, color);
        g.fill(cx + radius, cy - radius, cx + radius + 1, cy + radius + 1, color);
    }

    private void renderDaoSkillsTab(GuiGraphics graphics, int left, int top, int panelW, int panelH, SystemData data, int realm, int mX, int mY) {
        int listY = top + 96;
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
            graphics.drawString(font, descText, left + 18, itemY + 16, isUnlocked ? 0xFFAAAAAA : 0xFF884444);

            if (isUnlocked) {
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

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
}
