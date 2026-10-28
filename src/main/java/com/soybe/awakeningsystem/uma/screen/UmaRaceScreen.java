package com.soybe.awakeningsystem.uma.screen;

import com.soybe.awakeningsystem.uma.UmaProfile;
import com.soybe.awakeningsystem.uma.network.OpenUmaRacePayload;
import com.soybe.awakeningsystem.uma.network.UmaRaceActionC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla gráfica interactiva para el Gran Hipódromo de Teyvat (Uma Musume Derby).
 * Diseño de alta fidelidad: Nítido (sin blur), botones estilizados con el marco del mod,
 * tarjetas interactivas de corredoras, panel de botín de mazmorra a apostar y sin solapamientos.
 */
public class UmaRaceScreen extends Screen {

    private static final int GUI_WIDTH = 456;
    private static final int GUI_HEIGHT = 250;

    private final OpenUmaRacePayload data;
    private final List<UmaProfile> participants = new ArrayList<>();
    private int selectedRunnerIndex = 0;

    public UmaRaceScreen(OpenUmaRacePayload data) {
        super(Component.translatable("awakening_system.uma.race.title"));
        this.data = data;
        for (String id : data.participantIds()) {
            this.participants.add(UmaProfile.byId(id));
        }
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 470.0F;
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
        graphics.fill(0, 0, this.width, this.height, 0x77000000);
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;

        int startX = left + 12;
        int startY = top + 35;
        int cardW = 140;
        int cardH = 37;
        int spacingY = 39;
        int colSpacing = 146;

        for (int i = 0; i < participants.size() && i < 9; i++) {
            UmaProfile p = participants.get(i);
            int col = i % 3;
            int row = i / 3;
            int cardX = startX + col * colSpacing;
            int cardY = startY + row * spacingY;

            this.addRenderableWidget(new UmaCardWidget(cardX, cardY, cardW, cardH, i, p));
        }

        int btnY = top + GUI_HEIGHT - 32;

        boolean isBettingOpen = (data.raceState() == 0);
        boolean hasExistingBet = (data.myBetUmaId() != null && !data.myBetUmaId().isEmpty());

        if (!isBettingOpen) {
            this.addRenderableWidget(new UmaActionButton(
                    left + 24, btnY, 145, 22,
                    Component.translatable("awakening_system.uma.race.btn.closed"), 0xFF888888, 0xAA333333, false,
                    () -> {
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.displayClientMessage(
                                    Component.translatable("awakening_system.uma.betting_closed"),
                                    true
                            );
                        }
                    }
            ));
        } else if (hasExistingBet) {
            this.addRenderableWidget(new UmaActionButton(
                    left + 24, btnY, 145, 22,
                    Component.translatable("awakening_system.uma.race.btn.placed"), 0xFF2ECC71, 0xAA004411, false,
                    () -> {
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.displayClientMessage(
                                    Component.translatable("awakening_system.uma.bet_already_placed"),
                                    true
                            );
                        }
                    }
            ));
        } else {
            this.addRenderableWidget(new UmaActionButton(
                    left + 24, btnY, 145, 22,
                    Component.translatable("awakening_system.uma.race.btn.bet"), 0xFFFFD700, 0xAA664400, true,
                    () -> placeBet()
            ));
        }

        if (isBettingOpen) {
            this.addRenderableWidget(new UmaActionButton(
                    left + 179, btnY, 150, 22,
                    Component.translatable("awakening_system.uma.race.btn.start"), 0xFF2ECC71, 0xAA005522, true,
                    () -> startRaceNow()
            ));
        } else {
            this.addRenderableWidget(new UmaActionButton(
                    left + 179, btnY, 150, 22,
                    Component.translatable("awakening_system.uma.race.btn.running"), 0xFF888888, 0xAA333333, false,
                    () -> {}
            ));
        }

        this.addRenderableWidget(new UmaActionButton(
                left + GUI_WIDTH - 105, btnY, 80, 22,
                Component.translatable("awakening_system.uma.race.btn.close"), 0xFFE74C3C, 0xAA551111, true,
                () -> this.onClose()
        ));
    }

    private void placeBet() {
        if (data.raceState() != 0) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                        Component.translatable("awakening_system.uma.betting_closed"),
                        true
                );
            }
            return;
        }
        if (participants.isEmpty()) return;
        UmaProfile chosen = participants.get(selectedRunnerIndex);
        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack held = this.minecraft.player.getMainHandItem();
            if (held.isEmpty()) {
                this.minecraft.player.displayClientMessage(
                        Component.translatable("awakening_system.uma.invalid_bet_item"),
                        true
                );
                return;
            }
            PacketDistributor.sendToServer(new UmaRaceActionC2SPayload(
                    "BET",
                    chosen.getId(),
                    this.minecraft.player.getInventory().selected,
                    held.getCount()
            ));
            this.onClose();
        }
    }

    private void startRaceNow() {
        PacketDistributor.sendToServer(new UmaRaceActionC2SPayload(
                "START",
                "",
                0,
                0
        ));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

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

        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;

        graphics.fillGradient(left, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xF810131E, 0xFC0A0C14);

        graphics.renderOutline(left, top, GUI_WIDTH, GUI_HEIGHT, 0xFFD4AF37);
        graphics.renderOutline(left + 1, top + 1, GUI_WIDTH - 2, GUI_HEIGHT - 2, 0x55D4AF37);

        drawCornerDecorations(graphics, left, top, GUI_WIDTH, GUI_HEIGHT);

        graphics.fillGradient(left + 2, top + 2, left + GUI_WIDTH - 2, top + 34, 0xEE1A2238, 0xBB10131E);
        graphics.renderOutline(left + 2, top + 2, GUI_WIDTH - 4, 32, 0x44D4AF37);

        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.race.title"), left + GUI_WIDTH / 2, top + 8, 0xFFD4AF37);

        Component statusComponent;
        switch (data.raceState()) {
            case 0 -> statusComponent = Component.translatable("awakening_system.uma.race.status.betting_open", data.stateTimer());
            case 1 -> statusComponent = Component.translatable("awakening_system.uma.race.status.on_your_marks");
            case 2 -> statusComponent = Component.translatable("awakening_system.uma.race.status.running");
            default -> statusComponent = Component.translatable("awakening_system.uma.race.status.finished");
        }
        graphics.drawCenteredString(this.font, statusComponent, left + GUI_WIDTH / 2, top + 21, 0xFFFFFF);

        super.render(graphics, mX, mY, partialTick);

        int panelY = top + 155;
        int panelH = 54;
        graphics.fillGradient(left + 14, panelY, left + GUI_WIDTH - 14, panelY + panelH, 0xDD141824, 0xEE0D101A);
        graphics.renderOutline(left + 14, panelY, GUI_WIDTH - 28, panelH, 0x66D4AF37);

        if (!participants.isEmpty() && selectedRunnerIndex < participants.size()) {
            UmaProfile sel = participants.get(selectedRunnerIndex);

            graphics.drawString(this.font, Component.translatable("awakening_system.uma.race.label.runner", sel.getDisplayName(), (selectedRunnerIndex + 1)), left + 22, panelY + 6, 0xFFFFFF, false);
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.race.label.tactique_surface", sel.getTactique(), sel.getSurface()), left + 22, panelY + 19, 0xCCCCCC, false);
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.race.label.skill_odds", sel.getSpecialSkill(), sel.getBaseOdds()), left + 22, panelY + 32, 0xFFD4AF37, false);

            int slotX = left + GUI_WIDTH - 80;
            int slotY = panelY + 14;

            graphics.fill(slotX - 1, slotY - 1, slotX + 23, slotY + 23, 0xAA000000);
            graphics.renderOutline(slotX - 1, slotY - 1, 24, 24, 0xFFFFD700);

            if (!data.myWagerItem().isEmpty()) {
                ItemStack wager = data.myWagerItem();
                graphics.renderItem(wager, slotX + 4, slotY + 4);
                graphics.renderItemDecorations(this.font, wager, slotX + 4, slotY + 4);
                graphics.drawString(this.font, Component.translatable("awakening_system.uma.race.label.wagered"), slotX - 58, panelY + 14, 0xFFD4AF37, false);
                graphics.drawString(this.font, "§e" + wager.getCount() + "x", slotX - 58, panelY + 26, 0xFFFF88, false);
            } else if (this.minecraft != null && this.minecraft.player != null) {
                ItemStack held = this.minecraft.player.getMainHandItem();
                if (!held.isEmpty()) {
                    graphics.renderItem(held, slotX + 4, slotY + 4);
                    graphics.renderItemDecorations(this.font, held, slotX + 4, slotY + 4);

                    graphics.drawString(this.font, Component.translatable("awakening_system.uma.race.label.loot"), slotX - 44, panelY + 14, 0x88FF88, false);
                    graphics.drawString(this.font, "§e" + held.getCount() + "x", slotX - 44, panelY + 26, 0xFFFF88, false);
                } else {
                    graphics.drawString(this.font, Component.translatable("awakening_system.uma.race.label.empty_hand_1"), slotX - 44, panelY + 14, 0x888888, false);
                    graphics.drawString(this.font, Component.translatable("awakening_system.uma.race.label.empty_hand_2"), slotX - 44, panelY + 26, 0x888888, false);
                }
            }
        }

        graphics.pose().popPose();
    }

    private void drawCornerDecorations(GuiGraphics graphics, int x, int y, int w, int h) {
        int gold = 0xFFFFD700;
        graphics.fill(x + 2, y + 2, x + 6, y + 4, gold);
        graphics.fill(x + 2, y + 2, x + 4, y + 6, gold);
        graphics.fill(x + w - 6, y + 2, x + w - 2, y + 4, gold);
        graphics.fill(x + w - 4, y + 2, x + w - 2, y + 6, gold);
        graphics.fill(x + 2, y + h - 4, x + 6, y + h - 2, gold);
        graphics.fill(x + 2, y + h - 6, x + 4, y + h - 2, gold);
        graphics.fill(x + w - 6, y + h - 4, x + w - 2, y + h - 2, gold);
        graphics.fill(x + w - 4, y + h - 6, x + w - 2, y + h - 2, gold);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Botón de acción con estilo nativo a juego con la interfaz de Teyvat.
     */
    private class UmaActionButton extends AbstractWidget {
        private final Component message;
        private final int textColor;
        private final int hoverBg;
        private final boolean isActionEnabled;
        private final Runnable action;

        public UmaActionButton(int x, int y, int width, int height, Component message, int textColor, int hoverBg, boolean enabled, Runnable action) {
            super(x, y, width, height, message);
            this.message = message;
            this.textColor = textColor;
            this.hoverBg = hoverBg;
            this.isActionEnabled = enabled;
            this.action = action;
            this.active = enabled;
        }

        public UmaActionButton(int x, int y, int width, int height, Component message, int textColor, int hoverBg, Runnable action) {
            this(x, y, width, height, message, textColor, hoverBg, true, action);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            this.action.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (!this.isActionEnabled) {
                graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xEE161820, 0xFA0F1116);
                graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFF444455);
                int textX = this.getX() + (this.width - font.width(this.message)) / 2;
                int textY = this.getY() + (this.height - 8) / 2;
                graphics.drawString(font, this.message, textX, textY, 0xFF888899, false);
                return;
            }

            boolean hovered = this.isHoveredOrFocused();

            int bgTop = hovered ? this.hoverBg : 0xEE141824;
            int bgBottom = hovered ? (this.hoverBg | 0x22000000) : 0xFA0B0E16;
            graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgTop, bgBottom);

            int outlineColor = hovered ? 0xFFFFFFFF : this.textColor;
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, outlineColor);

            if (hovered) {
                graphics.renderOutline(this.getX() + 1, this.getY() + 1, this.width - 2, this.height - 2, 0x66FFFFFF);
            }

            int color = hovered ? 0xFFFFFFFF : this.textColor;
            int textX = this.getX() + (this.width - font.width(this.message)) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            graphics.drawString(font, this.message, textX, textY, color, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }

    /**
     * Widget personalizado para cada tarjeta de corredora.
     */
    private class UmaCardWidget extends AbstractWidget {
        private final int index;
        private final UmaProfile profile;

        public UmaCardWidget(int x, int y, int width, int height, int index, UmaProfile profile) {
            super(x, y, width, height, Component.literal(profile.getDisplayName()));
            this.index = index;
            this.profile = profile;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            selectedRunnerIndex = this.index;
            playDownSound(Minecraft.getInstance().getSoundManager());
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean isSelected = (selectedRunnerIndex == this.index);
            boolean isHovered = this.isHoveredOrFocused();

            int baseColor = profile.getColorHex();
            int r = (baseColor >> 16) & 0xFF;
            int g = (baseColor >> 8) & 0xFF;
            int b = baseColor & 0xFF;

            int bgColor = isSelected ? 0xEE1E2436 : (isHovered ? 0xDD181D2A : 0xBB111420);
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);

            if (isSelected) {
                graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFFFFD700);
                graphics.renderOutline(this.getX() + 1, this.getY() + 1, this.width - 2, this.height - 2, 0x88FFD700);
            } else if (isHovered) {
                graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFFFFFFFF);
            } else {
                int outlineColor = (0x55 << 24) | (r << 16) | (g << 8) | b;
                graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, outlineColor);
            }

            int barColor = (0xFF << 24) | (r << 16) | (g << 8) | b;
            graphics.fill(this.getX() + 2, this.getY() + 2, this.getX() + 5, this.getY() + this.height - 2, barColor);

            String dorsal = (isSelected ? "▶ #" : "#") + (index + 1);
            int numColor = isSelected ? 0xFFFFD700 : 0xAAAAAA;
            graphics.drawString(font, dorsal, this.getX() + 8, this.getY() + 3, numColor, false);

            String oddsStr = profile.getBaseOdds() + "x";
            int badgeW = font.width(oddsStr) + 6;
            int badgeX = this.getX() + this.width - badgeW - 4;
            int badgeY = this.getY() + 2;
            graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 11, isSelected ? 0xAA8B7500 : 0x55000000);
            graphics.renderOutline(badgeX, badgeY, badgeW, 11, isSelected ? 0xFFFFD700 : 0x88D4AF37);
            graphics.drawString(font, oddsStr, badgeX + 3, badgeY + 2, isSelected ? 0xFFFFFFFF : 0xFFFFD700, false);

            String name = profile.getDisplayName();
            int maxNameW = this.width - 16;
            if (font.width(name) > maxNameW) {
                name = font.plainSubstrByWidth(name, maxNameW - 6) + "…";
            }
            int nameColor = isSelected ? 0xFFFFFF : (isHovered ? 0xEEEEEE : 0xDDDDDD);
            graphics.drawString(font, (isSelected ? "§e§l" : "§f") + name, this.getX() + 8, this.getY() + 13, nameColor, false);

            String rawTac = profile.getTactique().split(" ")[0];
            if (rawTac.length() > 9) rawTac = rawTac.substring(0, 9);
            String skillTag = "§7" + rawTac + " §6★";
            graphics.drawString(font, skillTag, this.getX() + 8, this.getY() + 24, 0x888888, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }
}
