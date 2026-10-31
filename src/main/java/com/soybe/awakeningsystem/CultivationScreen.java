package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Manhwa / Solo Leveling Murim Cultivation Screen (Dantian & Meridianos).
 * Perfectly aligned with the official System Window Frame architecture.
 */
public final class CultivationScreen extends Screen {
    private static final int PANEL_W = 520;
    private static final int PANEL_H = 295;

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

    // Spaced-out acupoints across the spiritual mannequin (relative to center xc and top y0)
    private static final List<NodePos> NODES = List.of(
            new NodePos(0, -28, 44),  // 1. Pulmón (Brazo izquierdo superior)
            new NodePos(1, -28, 62),  // 2. Intestino Grueso (Brazo izquierdo medio)
            new NodePos(2, 11, 46),   // 3. Estómago (Pecho derecho)
            new NodePos(3, -12, 66),  // 4. Bazo (Costillas inferiores izquierdas)
            new NodePos(4, -11, 46),  // 5. Corazón (Pecho izquierdo)
            new NodePos(5, -28, 78),  // 6. Intestino Delgado (Mano izquierda)
            new NodePos(6, -9, 108),  // 7. Vejiga (Pierna izquierda)
            new NodePos(7, -11, 78),  // 8. Riñón (Flanco inferior izquierdo)
            new NodePos(8, 0, 42),    // 9. Pericardio (Pecho central superior)
            new NodePos(9, 28, 44),   // 10. Triple Calentador (Brazo derecho)
            new NodePos(10, 11, 16),  // 11. Vesícula Biliar (Sien derecha)
            new NodePos(11, 12, 66)   // 12. Hígado (Costillas inferiores derechas)
    );

    // Energetic circuit lines connecting the meridian network
    private static final List<CircuitLine> CIRCUIT_LINES = List.of(
            new CircuitLine(10, 10, 0, 14, 11, 16),
            new CircuitLine(10, 8, 0, 14, 0, 32),
            new CircuitLine(8, 8, 0, 32, 0, 42),
            new CircuitLine(8, 4, 0, 42, -11, 46),
            new CircuitLine(8, 2, 0, 42, 11, 46),
            new CircuitLine(4, 0, -11, 46, -28, 44),
            new CircuitLine(2, 9, 11, 46, 28, 44),
            new CircuitLine(8, 8, 0, 42, 0, 56), // Pericardio to Middle Dantian
            new CircuitLine(0, 1, -28, 44, -28, 62),
            new CircuitLine(1, 5, -28, 62, -28, 78),
            new CircuitLine(9, 9, 28, 44, 28, 62),
            new CircuitLine(9, 9, 28, 62, 28, 78),
            new CircuitLine(8, 3, 0, 56, -12, 66),
            new CircuitLine(8, 11, 0, 56, 12, 66),
            new CircuitLine(3, 7, -12, 66, -11, 78),
            new CircuitLine(11, 7, 12, 66, 0, 78),
            new CircuitLine(7, 7, -11, 78, 0, 78), // Riñón to Lower Dantian
            new CircuitLine(7, 6, 0, 78, -9, 108),
            new CircuitLine(6, 6, -9, 108, -9, 130),
            new CircuitLine(7, 11, 0, 78, 9, 108),
            new CircuitLine(11, 7, 9, 108, 9, 130)
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
        // Clean - no dark murky screen-wide filter
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }

        if (activeTab == 0 && minecraft != null && minecraft.player != null) {
            SystemData data = minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                selectedMeridian = (selectedMeridian + 11) % 12;
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                selectedMeridian = (selectedMeridian + 1) % 12;
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                selectedMeridian = (selectedMeridian + 10) % 12;
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                selectedMeridian = (selectedMeridian + 2) % 12;
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
                if (!data.isMeridianOpen(selectedMeridian)) {
                    int cost = (selectedMeridian + 1) * 35;
                    if (data.cultivationQi() >= cost) {
                        unlockEffectTicks = 20;
                        lastUnlockedMeridian = selectedMeridian;
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
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
            minecraft.setScreen(parentScreen != null ? parentScreen : new SystemScreen(0));
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
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.4F));
            }
            if (rollTicks == 0) {
                AwakeningSystemClient.performCultivationAction("awaken_physique", 0);
            }
        }
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 540.0F;
        float scaleY = (float) this.height / 315.0F;
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

            int left = (width - PANEL_W) / 2;
            int top = (height - PANEL_H) / 2;

            // Close button [✕]
            int closeX = left + PANEL_W - 32;
            int closeY = top + 14;
            int closeW = 16;
            int closeH = 16;
            if (mouseX >= closeX && mouseX < closeX + closeW && mouseY >= closeY && mouseY < closeY + closeH) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.9F));
                onClose();
                return true;
            }

            // Meditation pill button
            int medW = 86;
            int medH = 16;
            int medX = closeX - 10 - medW;
            int medY = top + 14;
            if (mouseX >= medX && mouseX <= medX + medW && mouseY >= medY && mouseY <= medY + medH) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                AwakeningSystemClient.performCultivationAction("toggle_meditation", 0);
                return true;
            }

            int cardTop = top + 31;

            // Physique Button
            int phyW = 120;
            int phyH = 16;
            int phyX = left + PANEL_W - 12 - phyW - 8;
            int phyY = cardTop + 4;
            boolean isUnawakened = data.murimPhysique() == null || data.murimPhysique().isEmpty();
            if (mouseX >= phyX && mouseX <= phyX + phyW && mouseY >= phyY && mouseY <= phyY + phyH) {
                if (isUnawakened && rollTicks <= 0) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 1.2F));
                    rollTicks = 24;
                } else if (!isUnawakened) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F));
                }
                return true;
            }

            // Advancement / Breakthrough Button
            int advW = 120;
            int advH = 16;
            int advX = phyX;
            int advY = cardTop + 24;
            if (mouseX >= advX && mouseX <= advX + advW && mouseY >= advY && mouseY <= advY + advH) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                if (data.cultivationSubLevel() < 9) {
                    AwakeningSystemClient.performCultivationAction("breakthrough_sublevel", 0);
                } else {
                    AwakeningSystemClient.performCultivationAction("advance_realm", 0);
                }
                return true;
            }

            // Navigation Tabs
            int tabY = top + 78;
            int tab0W = 160;
            int tab1W = 145;
            if (mouseY >= tabY && mouseY < tabY + 18) {
                if (mouseX >= left + 12 && mouseX < left + 12 + tab0W) {
                    activeTab = 0;
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
                if (mouseX >= left + 12 + tab0W + 4 && mouseX < left + 12 + tab0W + 4 + tab1W) {
                    activeTab = 1;
                    scrollOffset = 0;
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }

            if (activeTab == 0) {
                int colLeftX = left + 12;
                int colLeftY = top + 100;

                // 1. Selector Pills in left column
                int pillStartX = colLeftX + 6;
                int pillStartY = colLeftY + 57;
                for (int i = 0; i < 12; i++) {
                    int col = i % 2;
                    int row = i / 2;
                    int pillX = pillStartX + col * 64;
                    int pillY = pillStartY + row * 16;
                    if (mouseX >= pillX && mouseX <= pillX + 62 && mouseY >= pillY && mouseY <= pillY + 15) {
                        selectedMeridian = i;
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                        return true;
                    }
                }

                // 2. Direct clicks on acupoint nodes in middle mannequin
                int midX = colLeftX + 140 + 8;
                int xc = midX + 90;
                float breathe = (float) Math.sin(animTick * 0.08F) * 0.8F;
                int y0 = top + 104 + Math.round(breathe);

                for (NodePos node : NODES) {
                    int nx = xc + node.relX();
                    int ny = y0 + node.relY();
                    if (Math.abs(mouseX - nx) <= 7 && Math.abs(mouseY - ny) <= 7) {
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.3F));
                        if (selectedMeridian == node.id() && !data.isMeridianOpen(node.id())) {
                            int cost = (node.id() + 1) * 35;
                            if (data.cultivationQi() >= cost) {
                                unlockEffectTicks = 20;
                                lastUnlockedMeridian = node.id();
                                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                                AwakeningSystemClient.performCultivationAction("open_meridian", node.id());
                            } else {
                                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F));
                            }
                        } else {
                            selectedMeridian = node.id();
                        }
                        return true;
                    }
                }

                // 3. Unlock button in right column
                int colRightX = midX + 180 + 8;
                int colRightW = left + PANEL_W - 12 - colRightX;
                int colRightY = top + 100;
                int colRightH = 186;

                int btnW = colRightW - 16;
                int btnH = 22;
                int btnX = colRightX + 8;
                int btnY = colRightY + colRightH - btnH - 6;
                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    boolean isOpen = data.isMeridianOpen(selectedMeridian);
                    if (!isOpen) {
                        int cost = (selectedMeridian + 1) * 35;
                        if (data.cultivationQi() >= cost) {
                            unlockEffectTicks = 20;
                            lastUnlockedMeridian = selectedMeridian;
                            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
                            AwakeningSystemClient.performCultivationAction("open_meridian", selectedMeridian);
                        } else {
                            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F));
                        }
                        return true;
                    }
                }
            } else {
                int listY = top + 100;
                int visibleCount = 3;
                int realm = data.dantianRealm();
                for (int i = 0; i < visibleCount; i++) {
                    int index = scrollOffset + i;
                    if (index >= DAO_SKILLS.size()) break;
                    DaoSkillInfo s = DAO_SKILLS.get(index);
                    int itemY = listY + i * 44;
                    boolean isUnlocked = realm >= s.reqRealm();

                    if (isUnlocked) {
                        int btnW1 = 70;
                        int btnH = 18;
                        int btnX1 = left + PANEL_W - btnW1 - 65;
                        int btnY = itemY + 12;
                        if (mouseX >= btnX1 && mouseX <= btnX1 + btnW1 && mouseY >= btnY && mouseY <= btnY + btnH) {
                            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                            AwakeningSystemClient.performCultivationAction("select_dao_skill", s.id());
                            return true;
                        }

                        int btnW2 = 50;
                        int btnX2 = left + PANEL_W - btnW2 - 14;
                        if (mouseX >= btnX2 && mouseX <= btnX2 + btnW2 && mouseY >= btnY && mouseY <= btnY + btnH) {
                            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
        renderBackground(graphics, mouseX, mouseY, partialTick);

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

        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        ModernGuiHelper.GuiTheme theme = ModernGuiHelper.GuiTheme.SOLO_LEVELING;
        ModernGuiHelper.drawModernWindow(graphics, left, top, PANEL_W, PANEL_H, theme);

        // Header Title (Left, cleared from corner and central filigree)
        Component headerTitleComp = ModernGuiHelper.manhwa(Component.translatable("awakening_system.screen.cultivation.header"));
        graphics.drawString(font, headerTitleComp, left + 36, top + 15, 0xFF00E5FF, false);

        // Close Button [✕] (Right) - seamlessly integrated without dark block
        int closeX = left + PANEL_W - 32;
        int closeY = top + 14;
        int closeW = 16;
        int closeH = 16;
        boolean closeHover = mX >= closeX && mX < closeX + closeW && mY >= closeY && mY < closeY + closeH;
        if (closeHover) {
            graphics.fill(closeX, closeY, closeX + closeW, closeY + closeH, 0xDD7A1515);
            graphics.renderOutline(closeX, closeY, closeW, closeH, 0xFFFF5555);
        }
        graphics.drawCenteredString(font, "✕", closeX + closeW / 2, closeY + 4, closeHover ? 0xFFFFFFFF : 0xFF70E8FF);

        // Meditation Pill Button
        int medW = 86;
        int medH = 16;
        int medX = closeX - 10 - medW;
        int medY = top + 14;
        boolean hoverMed = mX >= medX && mX <= medX + medW && mY >= medY && mY <= medY + medH;
        boolean isMeditating = minecraft.player.getVehicle() instanceof com.soybe.awakeningsystem.entity.MeditationSeatEntity;
        int medBg = isMeditating ? (hoverMed ? 0x601E8449 : 0x40196F3D) : (hoverMed ? 0x3500E5FF : 0x1800E5FF);
        int medBorder = isMeditating ? 0xFF50FA7B : (hoverMed ? 0xFF00E5FF : 0x5500E5FF);
        graphics.fill(medX, medY, medX + medW, medY + medH, medBg);
        graphics.renderOutline(medX, medY, medW, medH, medBorder);
        Component medText = isMeditating
                ? Component.literal("§a☯ ").append(Component.translatable("awakening_system.screen.cultivation.meditating"))
                : Component.literal("§b☯ ").append(Component.translatable("awakening_system.screen.cultivation.start_meditation_short"));
        graphics.drawCenteredString(font, ModernGuiHelper.manhwa(medText), medX + medW / 2, medY + 4, isMeditating ? 0xFF50FA7B : 0xFF70E8FF);

        // Dantian & Realm Status Card
        int cardTop = top + 31;
        int cardW = PANEL_W - 24; // 496px
        int cardH = 44;
        graphics.fill(left + 12, cardTop, left + 12 + cardW, cardTop + cardH, 0x0A00E5FF);
        graphics.renderOutline(left + 12, cardTop, cardW, cardH, 0x5500E5FF);

        int realm = data.dantianRealm();
        int subLevel = data.cultivationSubLevel();
        Component realmComp = Component.translatable("awakening_system.screen.cultivation.realm_sublevel", SystemEvents.getDantianRealmComponent(realm), subLevel, 9);
        graphics.drawString(font, ModernGuiHelper.manhwa(realmComp), left + 18, cardTop + 5, 0xFFFFFFFF, false);

        float qiDensity = SystemEvents.getZoneQiDensity(minecraft.player);
        Component infoComp = Component.translatable("awakening_system.screen.cultivation.qi_density_info", String.format(Locale.US, "%.1fx", qiDensity), data.cultivationQi(), data.maxCultivationQi());
        graphics.drawString(font, ModernGuiHelper.manhwa(infoComp), left + 18, cardTop + 17, 0xFFCCE3EC, false);

        // Qi gauge
        int qi = data.cultivationQi();
        int maxQi = data.maxCultivationQi();
        float qiRatio = Math.max(0.0F, Math.min(1.0F, (float) qi / Math.max(1, maxQi)));
        int barW = 200;
        int barH = 7;
        int barX = left + 18;
        int barY = cardTop + 30;
        graphics.fill(barX, barY, barX + barW, barY + barH, 0x2000E5FF);
        graphics.renderOutline(barX, barY, barW, barH, 0x4000E5FF);
        int fillW = (int) (barW * qiRatio);
        if (fillW > 0) {
            graphics.fillGradient(barX, barY, barX + fillW, barY + barH, 0xFF00E5FF, 0xFF70E8FF);
        }

        // Action Buttons on Card (Physique & Breakthrough)
        int phyW = 120;
        int phyH = 16;
        int phyX = left + PANEL_W - 12 - phyW - 8;
        int phyY = cardTop + 4;
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
            phyBg = 0x807D3C98;
            phyBorder = 0xFFFFD700;
        } else if (isUnawakened) {
            phyBtnText = Component.literal("§6⚡ ").append(Component.translatable("awakening_system.screen.cultivation.awaken_physique"));
            phyBg = hoverPhy ? 0x80FFB800 : 0x50FFB800;
            phyBorder = hoverPhy ? 0xFFFFF088 : 0xFFFFD700;
        } else {
            Component phyShort = Component.translatable("awakening_system.physique." + curPhys + ".short");
            phyBtnText = Component.literal("§d☯ ").append(phyShort);
            phyBg = hoverPhy ? 0x705B2C6F : 0x504A235A;
            phyBorder = hoverPhy ? 0xFFFFD700 : 0xAA9B51E0;
        }
        graphics.fill(phyX, phyY, phyX + phyW, phyY + phyH, phyBg);
        graphics.renderOutline(phyX, phyY, phyW, phyH, phyBorder);
        graphics.drawCenteredString(font, ModernGuiHelper.manhwa(phyBtnText), phyX + phyW / 2, phyY + 4, 0xFFFFFFFF);

        // Breakthrough button
        int advW = 120;
        int advH = 16;
        int advX = phyX;
        int advY = cardTop + 24;
        boolean hoverAdv = mX >= advX && mX <= advX + advW && mY >= advY && mY <= advY + advH;

        Component advComp;
        int advColor;
        int advBorderCol;
        int advTextColor;
        if (subLevel < 9) {
            boolean canAdvanceSub = qi >= maxQi;
            advColor = canAdvanceSub ? (hoverAdv ? 0x80124860 : 0x50124860) : 0x1800E5FF;
            advBorderCol = canAdvanceSub ? (hoverAdv ? 0xFFFFF088 : 0xFF00E5FF) : 0x3500E5FF;
            advTextColor = canAdvanceSub ? 0xFF70E8FF : 0xFF8AAAB8;
            advComp = Component.translatable("awakening_system.screen.cultivation.breakthrough_sublevel", (subLevel + 1));
        } else if (realm < 9) {
            int reqMeridians = Math.min(12, (realm + 1) * 2);
            int reqQi = (realm + 1) * 150;
            boolean canBreakthrough = data.getOpenedMeridiansCount() >= reqMeridians && qi >= reqQi;
            advColor = canBreakthrough ? (hoverAdv ? 0x90996800 : 0x70805500) : 0x20FFD700;
            advBorderCol = canBreakthrough ? (hoverAdv ? 0xFFFFF088 : 0xFFFFD700) : 0x40665520;
            advTextColor = canBreakthrough ? 0xFFFFD700 : 0xFF888888;
            advComp = Component.literal("⚡ ").append(Component.translatable("awakening_system.screen.cultivation.breakthrough"));
        } else {
            advColor = 0x1500E5FF;
            advBorderCol = 0x3500E5FF;
            advTextColor = 0xFF7F8C8D;
            advComp = Component.translatable("awakening_system.screen.cultivation.max_realm");
        }
        graphics.fill(advX, advY, advX + advW, advY + advH, advColor);
        graphics.renderOutline(advX, advY, advW, advH, advBorderCol);
        graphics.drawCenteredString(font, ModernGuiHelper.manhwa(advComp), advX + advW / 2, advY + 4, advTextColor);

        // Navigation Tabs Bar
        int tabY = top + 78;
        int tab0W = 160;
        int tab1W = 145;

        boolean hoverTab0 = mX >= left + 12 && mX < left + 12 + tab0W && mY >= tabY && mY < tabY + 18;
        boolean hoverTab1 = mX >= left + 12 + tab0W + 4 && mX < left + 12 + tab0W + 4 + tab1W && mY >= tabY && mY < tabY + 18;

        // Tab 0
        int t0Bg = activeTab == 0 ? 0x4000E5FF : (hoverTab0 ? 0x2200E5FF : 0x1000E5FF);
        int t0Border = activeTab == 0 ? 0xFF00E5FF : (hoverTab0 ? 0x8800E5FF : 0x3000E5FF);
        graphics.fill(left + 12, tabY, left + 12 + tab0W, tabY + 18, t0Bg);
        graphics.renderOutline(left + 12, tabY, tab0W, 18, t0Border);
        if (activeTab == 0) graphics.fill(left + 13, tabY, left + 12 + tab0W - 1, tabY + 1, 0xFF70E8FF);
        graphics.drawCenteredString(font, ModernGuiHelper.manhwa(Component.translatable("awakening_system.screen.cultivation.tab_meridians")), left + 12 + tab0W / 2, tabY + 5, activeTab == 0 ? 0xFF00E5FF : 0xFF8AAAB8);

        // Tab 1
        int t1X = left + 12 + tab0W + 4;
        int t1Bg = activeTab == 1 ? 0x4000E5FF : (hoverTab1 ? 0x2200E5FF : 0x1000E5FF);
        int t1Border = activeTab == 1 ? 0xFF00E5FF : (hoverTab1 ? 0x8800E5FF : 0x3000E5FF);
        graphics.fill(t1X, tabY, t1X + tab1W, tabY + 18, t1Bg);
        graphics.renderOutline(t1X, tabY, tab1W, 18, t1Border);
        if (activeTab == 1) graphics.fill(t1X + 1, tabY, t1X + tab1W - 1, tabY + 1, 0xFF70E8FF);
        graphics.drawCenteredString(font, ModernGuiHelper.manhwa(Component.translatable("awakening_system.screen.cultivation.tab_dao")), t1X + tab1W / 2, tabY + 5, activeTab == 1 ? 0xFF00E5FF : 0xFF8AAAB8);

        // Tribulation / Shield Hint on tab right
        if (data.isUnderTribulation()) {
            int strikesDone = data.getTribulationTotalStrikes() - data.getTribulationStrikesRemaining() + 1;
            Component tribComp = Component.translatable("awakening_system.screen.cultivation.tribulation_active", strikesDone, data.getTribulationTotalStrikes());
            graphics.drawString(font, tribComp, left + 330, tabY + 5, 0xFFFF5555, false);
        } else if (data.tribulationShieldTicks() > 0) {
            int sec = data.tribulationShieldTicks() / 20;
            Component shieldComp = Component.translatable("awakening_system.screen.cultivation.dao_shield", sec);
            graphics.drawString(font, shieldComp, left + 345, tabY + 5, 0xFF50FA7B, false);
        }

        // Content Rendering
        if (activeTab == 0) {
            renderMeridianMannequinTab(graphics, left, top, data, mX, mY);
        } else {
            renderDaoSkillsTab(graphics, left, top, data, realm, mX, mY);
        }

        // Physique Tooltip
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

    private void renderMeridianMannequinTab(GuiGraphics g, int left, int top, SystemData data, int mX, int mY) {
        int openedCount = data.getOpenedMeridiansCount();

        // 1. LEFT COLUMN: Circulation Network & Quick Selectors
        int colLeftX = left + 12;
        int colLeftY = top + 100;
        int colLeftW = 140;
        int colLeftH = 186;

        g.fill(colLeftX, colLeftY, colLeftX + colLeftW, colLeftY + colLeftH, 0x0A00E5FF);
        g.renderOutline(colLeftX, colLeftY, colLeftW, colLeftH, 0x4400E5FF);

        // Header Title
        g.drawString(font, ModernGuiHelper.manhwa(Component.literal("✦ ").append(Component.translatable("awakening_system.screen.cultivation.circulation_title"))), colLeftX + 8, colLeftY + 6, 0xFF00E5FF, false);
        g.drawString(font, Component.translatable("awakening_system.screen.cultivation.meridians_count_label", openedCount, 12), colLeftX + 8, colLeftY + 18, 0xFFCCE3EC, false);

        // Mini gauge
        int barW = colLeftW - 16;
        int barH = 4;
        int barX = colLeftX + 8;
        int barY = colLeftY + 29;
        g.fill(barX, barY, barX + barW, barY + barH, 0x2000E5FF);
        g.renderOutline(barX, barY, barW, barH, 0x3500E5FF);
        int fillW = (int) (barW * (openedCount / 12.0F));
        int barCol = (openedCount == 12) ? 0xFFFFD700 : 0xFF00E5FF;
        if (fillW > 0) g.fill(barX, barY, barX + fillW, barY + barH, barCol);

        // Active Circulation Badge
        int badgeY = colLeftY + 36;
        if (openedCount == 12) {
            g.drawString(font, Component.literal("§6★ ").append(Component.translatable("awakening_system.screen.cultivation.circulation_grand")), colLeftX + 8, badgeY, 0xFFFFD700, false);
            g.drawString(font, Component.literal("§a").append(Component.translatable("awakening_system.screen.cultivation.circulation_grand_bonus")), colLeftX + 8, badgeY + 9, 0xFF50FA7B, false);
        } else if (openedCount >= 6) {
            g.drawString(font, Component.literal("§b✦ ").append(Component.translatable("awakening_system.screen.cultivation.circulation_minor")), colLeftX + 8, badgeY, 0xFF50E4FF, false);
            g.drawString(font, Component.literal("§a").append(Component.translatable("awakening_system.screen.cultivation.circulation_minor_bonus")), colLeftX + 8, badgeY + 9, 0xFF50FA7B, false);
        } else {
            g.drawString(font, Component.literal("§7○ ").append(Component.translatable("awakening_system.screen.cultivation.circulation_none")), colLeftX + 8, badgeY, 0xFF8AAAB8, false);
            g.drawString(font, Component.literal("§8").append(Component.translatable("awakening_system.screen.cultivation.circulation_none_desc")), colLeftX + 8, badgeY + 9, 0xFF607080, false);
        }

        // Separator
        g.fill(colLeftX + 8, colLeftY + 54, colLeftX + colLeftW - 8, colLeftY + 55, 0x3000E5FF);

        // 12 Quick Selector Pills (2 columns x 6 rows)
        int pillStartX = colLeftX + 6;
        int pillStartY = colLeftY + 57;
        for (int i = 0; i < 12; i++) {
            int col = i % 2;
            int row = i / 2;
            int pillX = pillStartX + col * 64;
            int pillY = pillStartY + row * 16;
            int pillW = 62;
            int pillH = 15;

            boolean isOpen = data.isMeridianOpen(i);
            boolean isSel = (selectedMeridian == i);
            boolean isHov = mX >= pillX && mX <= pillX + pillW && mY >= pillY && mY <= pillY + pillH;

            int bg = isSel ? 0x5000E5FF : (isHov ? 0x3000E5FF : (isOpen ? 0x2050FA7B : 0x1000E5FF));
            int border = isSel ? 0xFF00E5FF : (isHov ? 0xAA00E5FF : (isOpen ? 0xFF50FA7B : 0x2800E5FF));
            g.fill(pillX, pillY, pillX + pillW, pillY + pillH, bg);
            g.renderOutline(pillX, pillY, pillW, pillH, border);

            String icon = isOpen ? "§a✓" : "§8" + (i + 1);
            Component shortName = MERIDIANS.get(i).shortName();
            Component pillComp = Component.literal(icon + " §f").append(shortName);
            g.drawString(font, pillComp, pillX + 3, pillY + 4, isSel ? 0xFF00E5FF : 0xFFCCE3EC, false);
        }

        // 2. CENTER AREA: Spiritual Character Mannequin
        int midX = colLeftX + colLeftW + 8; // left + 160
        int midW = 180;
        int midH = 186;
        g.fill(midX, colLeftY, midX + midW, colLeftY + midH, 0x1400E5FF);
        g.renderOutline(midX, colLeftY, midW, midH, 0x4400E5FF);

        int xc = midX + midW / 2;
        int y0 = top + 104;
        renderMinecraftMannequin(g, xc, y0, data, animTick, mX, mY);

        // 3. RIGHT COLUMN: Selected Meridian Inspection & Action Card
        int colRightX = midX + midW + 8; // left + 348
        int colRightY = colLeftY;
        int colRightW = left + PANEL_W - 12 - colRightX; // 160px
        int colRightH = 186;

        g.fill(colRightX, colRightY, colRightX + colRightW, colRightY + colRightH, 0x1400E5FF);
        g.renderOutline(colRightX, colRightY, colRightW, colRightH, 0x4400E5FF);

        MeridianInfo m = MERIDIANS.get(selectedMeridian);
        boolean isSelectedOpen = data.isMeridianOpen(selectedMeridian);
        int cost = (selectedMeridian + 1) * 35;
        boolean canAfford = data.cultivationQi() >= cost;

        // Top accent line
        g.fill(colRightX + 1, colRightY + 1, colRightX + colRightW - 1, colRightY + 3, isSelectedOpen ? 0xFF50FA7B : 0xFF00E5FF);

        // Title: [#X] Meridian Name
        Component selTitle = ModernGuiHelper.manhwa("§6[#" + (selectedMeridian + 1) + "] " + m.name().getString());
        g.drawString(font, selTitle, colRightX + 8, colRightY + 6, 0xFFFFFFFF, false);

        // Organ channel
        g.drawString(font, Component.literal("§b").append(m.organ()), colRightX + 8, colRightY + 18, 0xFF50E4FF, false);

        // Status badge
        Component statusComp = isSelectedOpen
                ? Component.translatable("awakening_system.screen.cultivation.unlocked_badge")
                : Component.translatable("awakening_system.screen.cultivation.locked_badge");
        g.drawString(font, statusComp, colRightX + 8, colRightY + 30, isSelectedOpen ? 0xFF50FA7B : 0xFFFF5555, false);

        // Separator
        g.fill(colRightX + 8, colRightY + 42, colRightX + colRightW - 8, colRightY + 43, 0x3000E5FF);

        // Permanent Effect label & description lines
        g.drawString(font, Component.literal("✦ ").append(Component.translatable("awakening_system.screen.cultivation.meridian_permanent_effect")), colRightX + 8, colRightY + 47, 0xFFFFD700, false);
        List<FormattedCharSequence> effectLines = font.split(m.effect(), colRightW - 16);
        int effY = colRightY + 59;
        for (int i = 0; i < Math.min(3, effectLines.size()); i++) {
            g.drawString(font, effectLines.get(i), colRightX + 8, effY + (i * 10), 0xFFCCE3EC, false);
        }

        // Cost & Player Qi Info
        int costY = colRightY + 100;
        Component costComp = Component.translatable("awakening_system.screen.cultivation.meridian_cost", cost);
        g.drawString(font, costComp, colRightX + 8, costY, isSelectedOpen ? 0xFF8AAAB8 : (canAfford ? 0xFF50FA7B : 0xFFFF7777), false);

        Component yourQiComp = Component.translatable("awakening_system.screen.cultivation.meridian_your_qi", data.cultivationQi());
        g.drawString(font, yourQiComp, colRightX + 8, costY + 11, 0xFF50E4FF, false);

        // Action Button
        int btnW = colRightW - 16;
        int btnH = 22;
        int btnX = colRightX + 8;
        int btnY = colRightY + colRightH - btnH - 6;
        boolean hoverBtn = mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH;

        if (isSelectedOpen) {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x60103E22);
            g.renderOutline(btnX, btnY, btnW, btnH, 0xFF50FA7B);
            Component openedText = Component.literal("§a✔ ").append(Component.translatable("awakening_system.screen.cultivation.opened"));
            g.drawCenteredString(font, ModernGuiHelper.manhwa(openedText), btnX + btnW / 2, btnY + 7, 0xFF50FA7B);
        } else if (canAfford) {
            int bBg = hoverBtn ? 0x991E5B7A : 0x7015435B;
            int bBorder = hoverBtn ? 0xFFFFF088 : 0xFF00E5FF;
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, bBg);
            g.renderOutline(btnX, btnY, btnW, btnH, bBorder);
            Component unlockText = Component.translatable("awakening_system.screen.cultivation.meridian_unlock_btn", cost);
            g.drawCenteredString(font, ModernGuiHelper.manhwa(unlockText), btnX + btnW / 2, btnY + 7, hoverBtn ? 0xFFFFF088 : 0xFF00E5FF);
        } else {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x1400E5FF);
            g.renderOutline(btnX, btnY, btnW, btnH, 0x3000E5FF);
            Component lockedText = Component.translatable("awakening_system.screen.cultivation.meridian_not_enough", cost);
            g.drawCenteredString(font, ModernGuiHelper.manhwa(lockedText), btnX + btnW / 2, btnY + 7, 0xFF888888);
        }
    }

    private void renderMinecraftMannequin(GuiGraphics g, int xc, int y0, SystemData data, float animTick, int mX, int mY) {
        float breathe = (float) Math.sin(animTick * 0.08F) * 0.8F;
        int curY0 = y0 + Math.round(breathe);

        int openedCount = data.getOpenedMeridiansCount();
        boolean isGrand = (openedCount == 12);
        boolean isMinor = (openedCount >= 6);

        // Luminous ethereal silhouette styling
        int bodyFillCol = 0x550B2538;
        int bodyBorderCol = isGrand ? 0xCCFFD700 : (isMinor ? 0x9900E5FF : 0x7000E5FF);

        // 1. Spiritual Cultivation Array Background
        drawSpiritualArray(g, xc, curY0 + 60, animTick, openedCount);

        // 2. Active circuit pathways
        for (CircuitLine line : CIRCUIT_LINES) {
            boolean flowA = line.nodeA() < 0 || data.isMeridianOpen(line.nodeA());
            boolean flowB = line.nodeB() < 0 || data.isMeridianOpen(line.nodeB());
            boolean isFlowing = flowA && flowB;
            drawEnergyLine(g, xc + line.relX1(), curY0 + line.relY1(), xc + line.relX2(), curY0 + line.relY2(), isFlowing, animTick);
        }

        // 3. Spiritual Player Mannequin
        // Head (30 x 30)
        int headX = xc - 15;
        int headY = curY0;
        g.fill(headX, headY, headX + 30, headY + 30, bodyFillCol);
        g.renderOutline(headX, headY, 30, 30, bodyBorderCol);
        // Ethereal spiritual eyes
        g.fill(headX + 5, headY + 16, headX + 11, headY + 18, 0xCC00E5FF);
        g.fill(headX + 19, headY + 16, headX + 25, headY + 18, 0xCC00E5FF);

        // Torso (34 x 48)
        int torsoX = xc - 17;
        int torsoY = curY0 + 32;
        g.fill(torsoX, torsoY, torsoX + 34, torsoY + 48, bodyFillCol);
        g.renderOutline(torsoX, torsoY, 34, 48, bodyBorderCol);
        // Spiritual spinal cord
        g.fill(xc, torsoY + 2, xc + 1, torsoY + 46, 0x6000E5FF);

        // Left Arm (14 x 48)
        int lArmX = xc - 33;
        int lArmY = curY0 + 32;
        g.fill(lArmX, lArmY, lArmX + 14, lArmY + 48, bodyFillCol);
        g.renderOutline(lArmX, lArmY, 14, 48, bodyBorderCol);

        // Right Arm (14 x 48)
        int rArmX = xc + 19;
        int rArmY = curY0 + 32;
        g.fill(rArmX, rArmY, rArmX + 14, rArmY + 48, bodyFillCol);
        g.renderOutline(rArmX, rArmY, 14, 48, bodyBorderCol);

        // Left Leg (15 x 52)
        int lLegX = xc - 16;
        int lLegY = curY0 + 82;
        g.fill(lLegX, lLegY, lLegX + 15, lLegY + 52, bodyFillCol);
        g.renderOutline(lLegX, lLegY, 15, 52, bodyBorderCol);

        // Right Leg (15 x 52)
        int rLegX = xc + 1;
        int rLegY = curY0 + 82;
        g.fill(rLegX, rLegY, rLegX + 15, rLegY + 52, bodyFillCol);
        g.renderOutline(rLegX, rLegY, 15, 52, bodyBorderCol);

        // 4. Three Dantians (Shang: Upper, Zhong: Middle, Xia: Lower)
        drawDantian(g, xc, curY0 + 14, 0, animTick); // Shang Dantian (Purple)
        drawDantian(g, xc, curY0 + 56, 1, animTick); // Zhong Dantian (Crimson)
        drawDantian(g, xc, curY0 + 78, 2, animTick); // Xia Dantian (Cyan)

        // 5. Acupoint Nodes
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
        int auraCol = (openedCount == 12) ? 0x25FFD700 : ((openedCount >= 6) ? 0x1E00E5FF : 0x1400E5FF);
        int ringCol = (openedCount == 12) ? 0x60FFD700 : ((openedCount >= 6) ? 0x4000E5FF : 0x2800E5FF);

        g.fillGradient(xc - 50, yc - 65, xc + 50, yc + 65, auraCol, 0x00051018);

        // Celestial axis lines
        g.fill(xc - 45, yc, xc + 45, yc + 1, ringCol);
        g.fill(xc, yc - 60, xc + 1, yc + 60, ringCol);

        // Outer geometric array frame
        g.renderOutline(xc - 45, yc - 60, 90, 120, ringCol);
    }

    private void drawEnergyLine(GuiGraphics g, int x1, int y1, int x2, int y2, boolean isFlowing, float animTick) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;

        int baseCol = isFlowing ? 0xCC00E5FF : 0x2800E5FF;
        int sparkIdx = isFlowing ? ((int) (animTick * 1.5F) % steps) : -10;

        for (int i = 0; i <= steps; i += 2) {
            int px = x1 + (dx * i) / steps;
            int py = y1 + (dy * i) / steps;

            int col = baseCol;
            if (isFlowing && Math.abs(i - sparkIdx) <= 2) {
                col = 0xFFFFFFFF; // Traveling Qi spark
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
            g.fill(x - r, y - r, x + r + 1, y + r + 1, isSelected ? 0x7000E5FF : 0x4000E5FF);
            g.renderOutline(x - r, y - r, (r * 2) + 1, (r * 2) + 1, isSelected ? 0xFFFFFFFF : 0xAA00E5FF);
        }

        // Diamond node
        int r = 3;
        int coreCol;
        int borderCol;
        if (isOpen) {
            coreCol = 0xFF50FA7B;
            borderCol = 0xFFFFFFFF;
        } else if (canAfford) {
            coreCol = 0xFFFFD700;
            borderCol = 0xFFFFF088;
        } else {
            coreCol = 0xFF2A3D4E;
            borderCol = 0xFF456178;
        }

        g.fill(x - r, y - r, x + r + 1, y + r + 1, coreCol);
        g.renderOutline(x - r, y - r, (r * 2) + 1, (r * 2) + 1, borderCol);

        if (isOpen) {
            g.fill(x, y, x + 1, y + 1, 0xFFFFFFFF);
        } else if (canAfford) {
            g.fill(x, y, x + 1, y + 1, 0xFFFFFFFF);
        }

        // Floating badge when selected or hovered
        if (isSelected || isHovered) {
            String badge = "#" + (id + 1);
            int badgeW = font.width(badge) + 6;
            int badgeX = (x < xc) ? (x - badgeW - 3) : (x + 5);
            int badgeY = y - 5;
            g.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 11, 0xD00A2030);
            g.renderOutline(badgeX, badgeY, badgeW, 11, isSelected ? 0xFF00E5FF : 0x6600E5FF);
            g.drawString(font, badge, badgeX + 3, badgeY + 2, isSelected ? 0xFF00E5FF : 0xFFCCE3EC, false);
        }
    }

    private void drawDantian(GuiGraphics g, int x, int y, int type, float animTick) {
        float pulse = (float) Math.sin(animTick * 0.15F + type) * 1.0F;
        int r = Math.round(3 + pulse);
        int coreCol = (type == 0) ? 0xFFBB86FC : ((type == 1) ? 0xFFFF4444 : 0xFF00E5FF);
        int haloCol = (type == 0) ? 0x40BB86FC : ((type == 1) ? 0x40FF4444 : 0x4000E5FF);

        g.fill(x - r - 2, y - r - 2, x + r + 3, y + r + 3, haloCol);
        g.fill(x, y - r, x + 1, y + r + 1, coreCol);
        g.fill(x - r, y, x + r + 1, y + 1, coreCol);
        g.fill(x - 1, y - 1, x + 2, y + 2, 0xFFFFFFFF);
    }

    private void drawExpandingRing(GuiGraphics g, int cx, int cy, int radius, int color) {
        if (radius <= 0) return;
        g.renderOutline(cx - radius, cy - radius, (radius * 2) + 1, (radius * 2) + 1, color);
    }

    private void renderDaoSkillsTab(GuiGraphics graphics, int left, int top, SystemData data, int realm, int mX, int mY) {
        int listY = top + 92;
        int visibleCount = 3;
        for (int i = 0; i < visibleCount; i++) {
            int index = scrollOffset + i;
            if (index >= DAO_SKILLS.size()) break;
            DaoSkillInfo s = DAO_SKILLS.get(index);
            int itemY = listY + i * 44;
            boolean isUnlocked = realm >= s.reqRealm();
            boolean isEquipped = data.selectedDaoSkill() == s.id();

            int bgCol = isUnlocked ? (isEquipped ? 0x3500E5FF : 0x1400E5FF) : 0x0C00E5FF;
            int borderCol = isUnlocked ? (isEquipped ? 0xFF00E5FF : 0x4400E5FF) : 0x2000E5FF;
            graphics.fill(left + 12, itemY, left + PANEL_W - 12, itemY + 40, bgCol);
            graphics.renderOutline(left + 12, itemY, PANEL_W - 24, 40, borderCol);

            if (isEquipped) {
                // Left cyan indicator bar
                graphics.fill(left + 12, itemY, left + 15, itemY + 40, 0xFF00E5FF);
            }

            Component prefix = isEquipped
                    ? Component.translatable("awakening_system.screen.cultivation.dao_equipped_prefix")
                    : Component.literal(isUnlocked ? "§a[✓] " : "§c[🔒] ");
            Component titleComp = Component.empty().append(prefix).append(Component.literal("§f")).append(s.name()).append(Component.literal(" §8| §b" + s.qiCost() + " Qi §8| §e" + s.cooldownSec() + "s CD"));
            graphics.drawString(font, ModernGuiHelper.manhwa(titleComp), left + 20, itemY + 5, 0xFFFFFFFF, false);

            Component descComp = isUnlocked ? s.desc() : Component.translatable("awakening_system.screen.cultivation.dao_req_realm", s.reqRealm(), SystemEvents.getDantianRealmComponent(s.reqRealm()));
            String descText = descComp.getString();
            if (font.width(descText) > 280) {
                descText = font.plainSubstrByWidth(descText, 270) + "...";
            }
            graphics.drawString(font, descText, left + 20, itemY + 18, isUnlocked ? 0xFFCCE3EC : 0xFFFF7777, false);

            if (isUnlocked) {
                int btnW1 = 70;
                int btnH = 18;
                int btnX1 = left + PANEL_W - btnW1 - 65;
                int btnY = itemY + 11;
                boolean hoverBtn1 = mX >= btnX1 && mX <= btnX1 + btnW1 && mY >= btnY && mY <= btnY + btnH;
                int bCol1 = isEquipped ? 0x80805500 : (hoverBtn1 ? 0x4000E5FF : 0x2000E5FF);
                graphics.fill(btnX1, btnY, btnX1 + btnW1, btnY + btnH, bCol1);
                graphics.renderOutline(btnX1, btnY, btnW1, btnH, isEquipped ? 0xFFFFD700 : (hoverBtn1 ? 0xFF00E5FF : 0x4000E5FF));
                Component assignText = isEquipped ? Component.translatable("awakening_system.screen.cultivation.dao_assigned") : Component.translatable("awakening_system.screen.cultivation.dao_assign");
                graphics.drawCenteredString(font, ModernGuiHelper.manhwa(assignText), btnX1 + btnW1 / 2, btnY + 5, isEquipped ? 0xFFFFD700 : 0xFFCCE3EC);

                int btnW2 = 50;
                int btnX2 = left + PANEL_W - btnW2 - 12;
                boolean canAfford = data.cultivationQi() >= s.qiCost();
                boolean hoverBtn2 = mX >= btnX2 && mX <= btnX2 + btnW2 && mY >= btnY && mY <= btnY + btnH;
                int bCol2 = canAfford ? (hoverBtn2 ? 0x5000E5FF : 0x2500E5FF) : 0x1400E5FF;
                graphics.fill(btnX2, btnY, btnX2 + btnW2, btnY + btnH, bCol2);
                graphics.renderOutline(btnX2, btnY, btnW2, btnH, canAfford ? (hoverBtn2 ? 0xFF00E5FF : 0x5000E5FF) : 0x2500E5FF);
                graphics.drawCenteredString(font, ModernGuiHelper.manhwa(Component.translatable("awakening_system.screen.cultivation.dao_use")), btnX2 + btnW2 / 2, btnY + 5, canAfford ? 0xFF00E5FF : 0xFF888888);
            } else {
                int tagW = 100;
                int tagX = left + PANEL_W - tagW - 14;
                graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.dao_locked").getString(), tagX + tagW / 2, itemY + 15, 0xFFFF7777);
            }
        }

        int scrollBarH = 120;
        int scrollBarY = listY;
        graphics.fill(left + PANEL_W - 8, scrollBarY, left + PANEL_W - 6, scrollBarY + scrollBarH, 0x2500E5FF);
        int maxScroll = Math.max(1, DAO_SKILLS.size() - visibleCount);
        int thumbH = Math.max(20, scrollBarH / (DAO_SKILLS.size() - visibleCount + 1));
        int thumbY = scrollBarY + (int) ((float) scrollOffset / maxScroll * (scrollBarH - thumbH));
        graphics.fill(left + PANEL_W - 8, thumbY, left + PANEL_W - 6, thumbY + thumbH, 0xCC00E5FF);

        // Guide hint at bottom
        graphics.drawCenteredString(font, Component.translatable("awakening_system.screen.cultivation.dao_help").getString(), left + PANEL_W / 2, top + PANEL_H - 18, 0xFF8AAAB8);
    }
}
