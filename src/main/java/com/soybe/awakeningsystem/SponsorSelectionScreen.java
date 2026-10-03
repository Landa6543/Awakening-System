package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class SponsorSelectionScreen extends Screen {
    private final Screen parentScreen;
    private final boolean forced;
    private final List<ConstellationDefinitions.Constellation> constellations;
    private int selectedIndex = 0;
    private int listScroll = 0;

    public SponsorSelectionScreen(Screen parentScreen, boolean forced) {
        super(Component.translatable("awakening_system.screen.sponsor.header"));
        this.parentScreen = parentScreen;
        this.forced = forced;
        this.constellations = ConstellationDefinitions.constellations();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !forced) {
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, constellations.size() - 6);
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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Evita deliberadamente el desenfoque (blur shader) de Minecraft vanilla
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Fondo translúcido oscuro nítido sin desenfoque
        g.fillGradient(0, 0, width, height, 0xDD040810, 0xF0081420);

        int boxW = Math.min(410, width - 16);
        int boxH = Math.min(235, height - 16);
        int left = (width - boxW) / 2;
        int top = (height - boxH) / 2;

        // Borde exterior y marco estilo Manhwa / ORV
        g.fill(left - 1, top - 1, left + boxW + 1, top + boxH + 1, 0xFF276A82);
        g.fill(left, top, left + boxW, top + boxH, 0xFA0B1622);
        g.fill(left, top, left + boxW, top + 2, 0xFFFFD866);

        // Encabezado
        Component headerComp = Component.translatable("awakening_system.screen.sponsor.header");
        Component subtitleComp = Component.translatable("awakening_system.screen.sponsor.subtitle");
        g.drawCenteredString(font, headerComp, left + boxW / 2, top + 7, 0xFFFFD866);
        g.drawCenteredString(font, subtitleComp, left + boxW / 2, top + 19, 0xFF8AA8B8);

        // Botón Cerrar (X) si no es forzado
        if (!forced) {
            int closeX = left + boxW - 20;
            int closeY = top + 6;
            boolean closeHov = mouseX >= closeX && mouseX < closeX + 14 && mouseY >= closeY && mouseY < closeY + 12;
            g.fill(closeX, closeY, closeX + 14, closeY + 12, closeHov ? 0xFFFF4444 : 0xFF203545);
            g.drawCenteredString(font, Component.literal("X"), closeX + 7, closeY + 2, 0xFFFFFFFF);
        }

        // Panel Izquierdo: Lista de Constelaciones (125px ancho)
        int listX = left + 10;
        int listY = top + 34;
        int listW = 125;
        int itemH = 29;
        int visibleCount = Math.min(6, (boxH - 44) / itemH);

        for (int i = 0; i < visibleCount; i++) {
            int idx = listScroll + i;
            if (idx >= constellations.size()) break;
            ConstellationDefinitions.Constellation c = constellations.get(idx);
            int iy = listY + i * itemH;
            boolean isSelected = (idx == selectedIndex);
            boolean isHovered = mouseX >= listX && mouseX < listX + listW && mouseY >= iy && mouseY < iy + itemH - 2;

            int bg = isSelected ? 0xFF225577 : (isHovered ? 0xFF18374D : 0x80102030);
            int border = isSelected ? 0xFFFFD866 : (isHovered ? 0xFF70E8FF : 0xFF203A4C);

            g.fill(listX, iy, listX + listW, iy + itemH - 2, bg);
            g.fill(listX, iy, listX + 2, iy + itemH - 2, border);

            // Nombre de la constelación recortado
            String cName = c.name().getString();
            if (font.width(cName) > listW - 8) {
                cName = font.plainSubstrByWidth(cName, listW - 14) + "..";
            }
            g.drawString(font, Component.literal(cName), listX + 6, iy + 4, isSelected ? 0xFFFFD866 : 0xFFE0F4FF, false);

            // Atributo principal
            String attr = "★ +" + c.attributeBonus().toUpperCase();
            g.drawString(font, Component.literal(attr), listX + 6, iy + 16, 0xFF70E8FF, false);
        }

        // Panel Derecho: Detalles del Contrato (Dossier Cósmico)
        int infoX = left + 142;
        int infoY = top + 34;
        int infoW = boxW - 152;
        int infoH = boxH - 42;

        g.fill(infoX, infoY, infoX + infoW, infoY + infoH, 0x7007111A);
        g.fill(infoX, infoY, infoX + infoW, infoY + 1, 0xFF356B82);

        if (selectedIndex >= 0 && selectedIndex < constellations.size()) {
            ConstellationDefinitions.Constellation current = constellations.get(selectedIndex);

            // Título y Rótulo de la Constelación
            g.drawString(font, Component.literal("§6§l[").append(current.name()).append(Component.literal("]")), infoX + 8, infoY + 6, 0xFFFFD866, false);
            g.drawString(font, Component.translatable("awakening_system.screen.sponsor.title_label", current.title()), infoX + 8, infoY + 18, 0xFFD0E0E8, false);

            // Serie de Origen / Referencia
            String seriesTag = getSeriesReference(current.id());
            g.drawString(font, Component.translatable("awakening_system.screen.sponsor.origin_label", seriesTag), infoX + 8, infoY + 30, 0xFF70E8FF, false);

            // Caja de Estigma
            int stgY = infoY + 44;
            int stgH = 46;
            g.fill(infoX + 6, stgY, infoX + infoW - 6, stgY + stgH, 0x80102636);
            g.fill(infoX + 6, stgY, infoX + 8, stgY + stgH, 0xFF50FA7B);

            Component stgLabel = Component.translatable("awakening_system.screen.sponsor.stigma_label");
            g.drawString(font, Component.literal("§a§l" + stgLabel.getString() + " §f").append(current.stigmaName()), infoX + 12, stgY + 4, 0xFF50FA7B, false);

            // Descripción del Estigma (envuelta si es larga)
            var stgLines = font.split(current.stigmaEffect(), infoW - 24);
            int ly = stgY + 16;
            for (int lineIdx = 0; lineIdx < Math.min(3, stgLines.size()); lineIdx++) {
                g.drawString(font, stgLines.get(lineIdx), infoX + 12, ly, 0xFFD8E4EC, false);
                ly += 10;
            }

            // Atributo & Monedas
            int statY = stgY + stgH + 6;
            g.drawString(font, Component.translatable("awakening_system.screen.sponsor.attribute_blessing", current.attributeBonus().toUpperCase()), infoX + 8, statY, 0xFF70E8FF, false);
            g.drawString(font, Component.translatable("awakening_system.screen.sponsor.initial_donation", current.defaultDonation()), infoX + 8, statY + 11, 0xFFFFD866, false);

            // Misiones Temáticas Disponibles
            List<SponsorQuestDefinitions.Quest> quests = SponsorQuestDefinitions.getQuestsForSponsor(current.id());
            int qY = statY + 24;
            if (!quests.isEmpty()) {
                g.drawString(font, Component.translatable("awakening_system.screen.sponsor.quests_available", quests.size()), infoX + 8, qY, 0xFFFF79C6, false);
                SponsorQuestDefinitions.Quest firstQ = quests.get(0);
                g.drawString(font, Component.literal("§7• ").append(firstQ.title()).append(Component.literal(" §8(+" + firstQ.coinReward() + " $ / +" + firstQ.bonusStatAmount() + " " + firstQ.bonusStat() + ")")), infoX + 12, qY + 11, 0xFFC0D0D8, false);
            } else {
                g.drawString(font, Component.translatable("awakening_system.screen.sponsor.quests_support"), infoX + 8, qY, 0xFF8AA8B8, false);
            }

            // Botón Firmar Contrato
            int btnW = infoW - 16;
            int btnH = 20;
            int btnX = infoX + 8;
            int btnY = infoY + infoH - btnH - 6;

            boolean isCurrentSponsor = false;
            if (minecraft != null && minecraft.player != null) {
                SystemData data = minecraft.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                if (data != null && current.id().equals(data.sponsorConstellation())) {
                    isCurrentSponsor = true;
                }
            }

            boolean btnHov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
            if (isCurrentSponsor) {
                g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF183B2B);
                g.drawCenteredString(font, Component.translatable("awakening_system.screen.sponsor.already_signed"), btnX + btnW / 2, btnY + 6, 0xFF50FA7B);
            } else {
                int btnColor = btnHov ? 0xFF358C62 : 0xFF216844;
                g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnColor);
                g.fill(btnX, btnY, btnX + btnW, btnY + 1, 0xFF50FA7B);
                g.drawCenteredString(font, Component.translatable("awakening_system.screen.sponsor.sign_contract"), btnX + btnW / 2, btnY + 6, btnHov ? 0xFFFFFFFF : 0xFFE0FFE8);
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int boxW = Math.min(410, width - 16);
        int boxH = Math.min(235, height - 16);
        int left = (width - boxW) / 2;
        int top = (height - boxH) / 2;

        // Clic en botón cerrar
        if (!forced) {
            int closeX = left + boxW - 20;
            int closeY = top + 6;
            if (mouseX >= closeX && mouseX < closeX + 14 && mouseY >= closeY && mouseY < closeY + 12) {
                onClose();
                return true;
            }
        }

        // Clic en lista de constelaciones
        int listX = left + 10;
        int listY = top + 34;
        int listW = 125;
        int itemH = 29;
        int visibleCount = Math.min(6, (boxH - 44) / itemH);

        for (int i = 0; i < visibleCount; i++) {
            int idx = listScroll + i;
            if (idx >= constellations.size()) break;
            int iy = listY + i * itemH;
            if (mouseX >= listX && mouseX < listX + listW && mouseY >= iy && mouseY < iy + itemH - 2) {
                selectedIndex = idx;
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.7F, 1.2F);
                }
                return true;
            }
        }

        // Clic en botón Firmar Contrato
        int infoX = left + 142;
        int infoY = top + 34;
        int infoW = boxW - 152;
        int infoH = boxH - 42;
        int btnW = infoW - 16;
        int btnH = 20;
        int btnX = infoX + 8;
        int btnY = infoY + infoH - btnH - 6;

        if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
            if (selectedIndex >= 0 && selectedIndex < constellations.size()) {
                ConstellationDefinitions.Constellation selected = constellations.get(selectedIndex);
                AwakeningSystemClient.selectConstellation(selected.id());
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
                    minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.2F);
                }
                onClose();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static String getSeriesReference(String constellationId) {
        return switch (constellationId) {
            case "prisoner_of_the_golden_headband" -> "Omniscient Reader's Viewpoint / Viaje al Oeste";
            case "abyssal_black_dragon" -> "Omniscient Reader's Viewpoint (ORV)";
            case "demon_like_judge_of_fire" -> "Omniscient Reader's Viewpoint (Uriel / Edén)";
            case "secretive_plotter" -> "Omniscient Reader's Viewpoint";
            case "salvation_reader" -> "Omniscient Reader's Viewpoint (Kim Dokja)";
            case "master_of_the_underworld", "queen_of_the_darkest_night" -> "ORV (Inframundo / Hades y Perséfone)";
            case "architect_of_the_system" -> "Solo Leveling (Ashborn / Arquitecto)";
            case "iron_blooded_sword_saint" -> "The Greatest Estate Developer";
            case "sacred_water_dragon" -> "The Greatest Estate Developer (Javier / Dragón)";
            case "martial_king_nayuta" -> "Second Life Ranker (Nayuta)";
            case "caregiver_of_the_abyss" -> "The S-Classes That I Raised (Han Yoojin)";
            case "murim_heavenly_demon" -> "Nano Machine (Cheon Yeo-woon)";
            case "master_of_mobius_gacha" -> "Pick Me Up, Infinite Gacha (Han Yslat)";
            case "blue_dragon_of_retribution" -> "Doom Breaker (Suicidal Battle God)";
            default -> "Multiverso Manhwa";
        };
    }
}
