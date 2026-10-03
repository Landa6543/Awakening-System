package com.soybe.awakeningsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class ConstellationChannelScreen extends Screen {
    private static final int PANEL_W = 480;
    private static final int PANEL_H = 265;

    private List<SyncStarStreamPayload.IncarnationEntry> incarnations = new ArrayList<>();
    private int selectedIndex = 0;
    private int selectedAmount = 100;
    private String selectedComment = "SATISFIED";
    private int scrollOffset = 0;

    private static final int[] AMOUNTS = {50, 100, 250, 500, 1000, 5000};
    private static final String[] COMMENT_KEYS = {
            "SATISFIED", "ENCOURAGING", "PROUD", "FASCINATED", "AMUSED", "RELIEVED", "MYTHIC", "SILENT"
    };
    private static final String[] COMMENT_LABELS = {
            "Satisfecho", "Alentador", "Orgulloso", "Fascinado", "Divertido", "Aliviado", "Mítico", "Silencioso"
    };

    public ConstellationChannelScreen() {
        super(Component.translatable("awakening_system.screen.constellation.channel_title"));
    }

    @Override
    protected void init() {
        super.init();
        PacketDistributor.sendToServer(new RequestStarStreamPayload());
    }

    public void updateIncarnations(List<SyncStarStreamPayload.IncarnationEntry> entries) {
        this.incarnations = new ArrayList<>(entries);
        if (selectedIndex >= this.incarnations.size()) {
            selectedIndex = Math.max(0, this.incarnations.size() - 1);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    public float getGuiScale() {
        float scaleX = (float) this.width / 510.0F;
        float scaleY = (float) this.height / 295.0F;
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
        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;
        if (mouseX < left || mouseX > left + PANEL_W || mouseY < top || mouseY > top + PANEL_H) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int listH = PANEL_H - 36;
        int rowH = 34;
        int visibleRows = (listH - 18) / rowH;
        int maxScroll = Math.max(0, incarnations.size() - visibleRows);
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
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Fondo translúcido cósmico
        g.fillGradient(0, 0, this.width, this.height, 0xA5050B16, 0xD50A1426);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            g.pose().translate(cx, cy, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cx, -cy, 0.0F);
        }

        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;


        // Marco Principal Cibernético / Estelar
        g.fill(left - 2, top - 2, left + PANEL_W + 2, top + PANEL_H + 2, 0xFF2A5F7A);
        g.fill(left, top, left + PANEL_W, top + PANEL_H, 0xF0081420);
        g.fill(left, top, left + PANEL_W, top + 26, 0xFF102636);
        g.fill(left, top + 25, left + PANEL_W, top + 27, 0xFFFFD700);

        // Cabecera de la Constelación
        SystemData myData = Minecraft.getInstance().player != null ?
                Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
        String rawMod = (myData != null && !myData.constellationModifier().isEmpty())
                ? myData.constellationModifier() : "";
        Component translatedMod = !rawMod.isEmpty()
                ? ConstellationAscension.getTranslatedTitleComponent(rawMod)
                : Component.translatable("awakening_system.screen.constellation.anonymous");
        String modName = translatedMod.getString();
        int myCoins = myData != null ? myData.coins() : 0;

        String coinsStr = Component.translatable("awakening_system.screen.constellation.coins", myCoins).getString();
        int coinsW = font.width(coinsStr);
        g.drawString(font, coinsStr, left + PANEL_W - 12 - coinsW, top + 8, 0xFFFFD866, false);

        int maxHeaderW = PANEL_W - 30 - coinsW;
        String headerPrefix = Component.translatable("awakening_system.screen.constellation.header_prefix").getString();
        int availHeader = maxHeaderW - font.width(headerPrefix);
        String safeModName = modName;
        if (font.width(safeModName) > availHeader) {
            safeModName = font.plainSubstrByWidth(safeModName, Math.max(20, availHeader - font.width("..."))) + "...";
        }
        g.drawString(font, headerPrefix + safeModName, left + 10, top + 8, 0xFFFFD700, false);

        int listX = left + 8;
        int listY = top + 30;
        int listW = 136;
        int listH = PANEL_H - 36;

        // ================= COLUMNA 1: ENCARNACIONES EN VIVO =================
        g.fill(listX, listY, listX + listW, listY + listH, 0xFF060E16);
        g.fill(listX, listY, listX + listW, listY + 16, 0xFF0E202E);
        g.drawString(font, Component.translatable("awakening_system.screen.constellation.incarnations").getString(), listX + 6, listY + 4, 0xFF70E8FF, false);

        int rowH = 34;
        int visibleRows = (listH - 18) / rowH;

        for (int i = 0; i < visibleRows && (i + scrollOffset) < incarnations.size(); i++) {
            int idx = i + scrollOffset;
            SyncStarStreamPayload.IncarnationEntry inc = incarnations.get(idx);
            int rowY = listY + 18 + i * rowH;
            boolean isSel = (idx == selectedIndex);
            boolean hov = mX >= listX + 2 && mX <= listX + listW - 2 && mY >= rowY && mY < rowY + rowH - 2;

            int bgCol = isSel ? 0xFF1A3D54 : (hov ? 0xFF122838 : 0xFF0A1822);
            int borderCol = isSel ? 0xFFFFD700 : (hov ? 0xFF39B9D5 : 0xFF163244);

            g.fill(listX + 2, rowY, listX + listW - 2, rowY + rowH - 2, borderCol);
            g.fill(listX + 3, rowY + 1, listX + listW - 3, rowY + rowH - 3, bgCol);

            int maxTextW = listW - 8;
            drawTruncatedString(g, (isSel ? "§e▶ " : "§7") + inc.name(), listX + 5, rowY + 3, maxTextW, isSel ? 0xFFFFFFFF : 0xFFDDECF2);

            String rankDisplay = inc.rank();
            if ("ABSOLUTE_GOD".equalsIgnoreCase(rankDisplay)) rankDisplay = Component.translatable("awakening_system.screen.constellation.rank.absolute_god").getString();
            else if ("NATIONAL_LEVEL".equalsIgnoreCase(rankDisplay)) rankDisplay = Component.translatable("awakening_system.screen.constellation.rank.national").getString();
            drawTruncatedString(g, Component.translatable("awakening_system.screen.constellation.level_tag", inc.level(), rankDisplay).getString(), listX + 5, rowY + 13, maxTextW, 0xFF8AAAB8);

            drawTruncatedString(g, "§a" + getTranslatedActivity(inc.activity()), listX + 5, rowY + 22, maxTextW, 0xFF50FA7B);
        }

        // ================= COLUMNA 2: PANTALLA DE TRANSMISIÓN EN DIRECTO =================
        int screenX = listX + listW + 6;
        int screenY = listY;
        int screenW = 160;
        int screenH = listH;

        // Bisel y monitor CRT
        g.fill(screenX, screenY, screenX + screenW, screenY + screenH, 0xFF152A38);
        g.fill(screenX + 1, screenY + 1, screenX + screenW - 1, screenY + screenH - 1, 0xFF040A10);

        // Cabecera del monitor
        g.fill(screenX + 2, screenY + 2, screenX + screenW - 2, screenY + 16, 0xDD0C1925);
        boolean liveBlink = (System.currentTimeMillis() / 600) % 2 == 0;
        g.drawString(font, (liveBlink ? "§c● " : "§4● ") + Component.translatable("awakening_system.screen.constellation.live", selectedIndex + 1).getString(), screenX + 6, screenY + 4, 0xFFFFFFFF, false);

        if (!incarnations.isEmpty() && selectedIndex >= 0 && selectedIndex < incarnations.size()) {
            SyncStarStreamPayload.IncarnationEntry target = incarnations.get(selectedIndex);

            // Viewport holográfico 3D con scanlines
            int viewX1 = screenX + 4;
            int viewY1 = screenY + 18;
            int viewX2 = screenX + screenW - 4;
            int viewY2 = screenY + 128;
            g.fill(viewX1, viewY1, viewX2, viewY2, 0xFF060F18);

            // Rejilla de scanlines sutiles
            for (int sy = viewY1 + 4; sy < viewY2; sy += 8) {
                g.fill(viewX1, sy, viewX2, sy + 1, 0x1200FFFF);
            }

            // Pedestal de energía
            int centerX = (viewX1 + viewX2) / 2;
            int feetY = viewY2 - 6;
            g.fill(centerX - 24, feetY, centerX + 24, feetY + 4, 0x50276A82);
            g.fill(centerX - 20, feetY + 1, centerX + 20, feetY + 3, 0x8070E8FF);

            // Renderizado 3D de la entidad
            LivingEntity renderEntity = getRenderEntity(target);
            if (renderEntity != null) {
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        g, viewX1, viewY1 + 2, viewX2, viewY2 - 4, 38, 0.0625F,
                        (float) mX, (float) mY, renderEntity
                );
            }

            // Datos de telemetría de la encarnación
            int telY = screenY + 132;
            int maxTelW = screenW - 12;
            drawTruncatedString(g, "§f§l" + target.name(), screenX + 6, telY, maxTelW, 0xFFFFFFFF);
            telY += 10;
            drawTruncatedString(g, Component.translatable("awakening_system.screen.constellation.rank_level", target.rank(), target.level()).getString(), screenX + 6, telY, maxTelW, 0xFFDDECF2);
            telY += 10;
            drawTruncatedString(g, Component.translatable("awakening_system.screen.constellation.dim", target.dimension()).getString(), screenX + 6, telY, maxTelW, 0xFF8AAAB8);
            telY += 10;

            // Barra de vida
            int hpBarW = screenW - 16;
            int hpBarY = telY;
            int hpBarH = 8;
            g.fill(screenX + 8, hpBarY, screenX + 8 + hpBarW, hpBarY + hpBarH, 0xFF2A0A0A);
            float hpPct = Math.max(0.0F, Math.min(1.0F, (float) target.hp() / Math.max(1, target.maxHp())));
            int fillW = (int) (hpBarW * hpPct);
            if (fillW > 0) g.fill(screenX + 8, hpBarY, screenX + 8 + fillW, hpBarY + hpBarH, 0xFFE74C3C);
            String hpStr = target.hp() + "/" + target.maxHp() + " HP";
            g.drawCenteredString(font, hpStr, screenX + screenW / 2, hpBarY, 0xFFFFFFFF);

            // Botón: SINTONIZAR VISIÓN EN VIVO / PROYECCIÓN ASTRAL
            int specBtnX = screenX + 6;
            int specBtnY = screenY + screenH - 22;
            int specBtnW = screenW - 12;
            int specBtnH = 17;
            boolean specHov = mX >= specBtnX && mX < specBtnX + specBtnW && mY >= specBtnY && mY < specBtnY + specBtnH;

            g.fill(specBtnX, specBtnY, specBtnX + specBtnW, specBtnY + specBtnH, specHov ? 0xFFFFD700 : 0xFF8A38B8);
            g.fill(specBtnX + 1, specBtnY + 1, specBtnX + specBtnW - 1, specBtnY + specBtnH - 1, specHov ? 0xDD2A1448 : 0xDD170B28);
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.constellation.tune_vision").getString(), specBtnX + specBtnW / 2, specBtnY + 5, 0xFFFFFFFF);

            // ================= COLUMNA 3: DONACIONES Y COMENTARIOS =================
            int detX = screenX + screenW + 6;
            int detY = listY;
            int detW = PANEL_W - (listW + screenW + 20);

            g.fill(detX, detY, detX + detW, detY + listH, 0xFF060E16);
            g.fill(detX + 1, detY + 1, detX + detW - 1, detY + listH - 1, 0xFF081420);

            // Selector de Cantidad a Donar
            g.drawString(font, Component.translatable("awakening_system.screen.constellation.coins_to_donate").getString(), detX + 6, detY + 4, 0xFFFFD866, false);

            int amtBtnW = (detW - 16) / 3;
            int amtBtnH = 14;
            int amountsY0 = detY + 16;
            int amountsY1 = detY + 33;

            for (int b = 0; b < AMOUNTS.length; b++) {
                int col = b % 3;
                int row = b / 3;
                int amX = detX + 5 + col * (amtBtnW + 3);
                int amY = (row == 0) ? amountsY0 : amountsY1;
                int amVal = AMOUNTS[b];
                boolean isCurAmt = (amVal == selectedAmount);
                boolean amHov = mX >= amX && mX < amX + amtBtnW && mY >= amY && mY < amY + amtBtnH;

                g.fill(amX, amY, amX + amtBtnW, amY + amtBtnH, isCurAmt ? 0xFFFFD700 : (amHov ? 0xFF39B9D5 : 0xFF1C3A4E));
                g.fill(amX + 1, amY + 1, amX + amtBtnW - 1, amY + amtBtnH - 1, isCurAmt ? 0xFF3D3200 : (amHov ? 0xFF10283A : 0xFF0D1B26));
                String label = amVal >= 1000 ? (amVal / 1000 + "k") : String.valueOf(amVal);
                g.drawCenteredString(font, label, amX + amtBtnW / 2, amY + 3, isCurAmt ? 0xFFFFD700 : 0xFFFFFFFF);
            }

            // Selector de Comentario Predeterminado
            g.drawString(font, Component.translatable("awakening_system.screen.constellation.cosmic_reaction").getString(), detX + 6, detY + 52, 0xFF70E8FF, false);

            int commentBtnW = (detW - 14) / 2;
            int commentBtnH = 13;
            int commentsStartY = detY + 65;

            for (int c = 0; c < COMMENT_KEYS.length; c++) {
                int col = c % 2;
                int row = c / 2;
                int cx = detX + 5 + col * (commentBtnW + 4);
                int cy = commentsStartY + row * (commentBtnH + 3);
                String cKey = COMMENT_KEYS[c];
                boolean isCurC = cKey.equals(selectedComment);
                boolean cHov = mX >= cx && mX < cx + commentBtnW && mY >= cy && mY < cy + commentBtnH;

                g.fill(cx, cy, cx + commentBtnW, cy + commentBtnH, isCurC ? 0xFFFFD700 : (cHov ? 0xFF39B9D5 : 0xFF1C3A4E));
                g.fill(cx + 1, cy + 1, cx + commentBtnW - 1, cy + commentBtnH - 1, isCurC ? 0xFF2B2200 : (cHov ? 0xFF10283A : 0xFF0D1B26));
                String cLabel = Component.translatable("awakening_system.constellation.comment." + c).getString();
                g.drawCenteredString(font, cLabel, cx + commentBtnW / 2, cy + 3, isCurC ? 0xFFFFD700 : 0xFFDDECF2);
            }

            // Vista previa del comentario en vivo
            int previewY = detY + 132;
            int previewH = 36;
            int previewW = detW - 10;
            g.fill(detX + 5, previewY, detX + 5 + previewW, previewY + previewH, 0xFF040A10);
            g.fill(detX + 6, previewY + 1, detX + 5 + previewW - 1, previewY + previewH - 1, 0xFF060E18);

            String preview = ConstellationChannelHandler.getCommentComponent(selectedComment, modName, selectedAmount).getString();
            var splitLines = font.split(Component.literal(preview), previewW - 8);
            int lineY = previewY + 3;
            g.enableScissor(detX + 6, previewY + 1, detX + 5 + previewW - 1, previewY + previewH - 1);
            for (int li = 0; li < Math.min(3, splitLines.size()); li++) {
                g.drawString(font, splitLines.get(li), detX + 8, lineY, 0xFF9BB7C7, false);
                lineY += 10;
            }
            g.disableScissor();

            // BOTÓN PRINCIPAL: PATROCINAR / ENVIAR MONEDAS
            int sendX = detX + 5;
            int sendY = detY + 174;
            int sendW = detW - 10;
            int sendH = 22;
            boolean canAfford = myCoins >= selectedAmount;
            boolean sendHov = mX >= sendX && mX < sendX + sendW && mY >= sendY && mY < sendY + sendH;

            int sendBorder = !canAfford ? 0xFF555555 : (sendHov ? 0xFFFFE066 : 0xFFFFD700);
            int sendBg = !canAfford ? 0xFF222222 : (sendHov ? 0xFF4D3F00 : 0xFF2E2400);

            g.fill(sendX, sendY, sendX + sendW, sendY + sendH, sendBorder);
            g.fill(sendX + 1, sendY + 1, sendX + sendW - 1, sendY + sendH - 1, sendBg);

            String btnText = canAfford ? Component.translatable("awakening_system.screen.constellation.sponsor_btn", selectedAmount).getString() : Component.translatable("awakening_system.screen.constellation.insufficient_coins").getString();
            g.enableScissor(sendX + 1, sendY + 1, sendX + sendW - 1, sendY + sendH - 1);
            g.drawCenteredString(font, btnText, sendX + sendW / 2, sendY + 7, canAfford ? 0xFFFFD700 : 0xFF888888);
            g.disableScissor();
        } else {
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.constellation.searching_incarnations").getString(), screenX + screenW / 2, screenY + 80, 0xFF8AAAB8);
        }

        g.pose().popPose();
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0) {
            int left = (this.width - PANEL_W) / 2;
            int top = (this.height - PANEL_H) / 2;

            int listX = left + 8;
            int listY = top + 30;
            int listW = 136;
            int listH = PANEL_H - 36;
            int rowH = 34;
            int visibleRows = (listH - 18) / rowH;

            // 1. Clic en lista de Encarnaciones (Columna 1)
            for (int i = 0; i < visibleRows && (i + scrollOffset) < incarnations.size(); i++) {
                int rowY = listY + 18 + i * rowH;
                if (mouseX >= listX + 2 && mouseX <= listX + listW - 2 && mouseY >= rowY && mouseY < rowY + rowH - 2) {
                    selectedIndex = i + scrollOffset;
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.2F);
                    }
                    return true;
                }
            }

            int screenX = listX + listW + 6;
            int screenY = listY;
            int screenW = 160;
            int screenH = listH;

            if (!incarnations.isEmpty() && selectedIndex >= 0 && selectedIndex < incarnations.size()) {
                SyncStarStreamPayload.IncarnationEntry target = incarnations.get(selectedIndex);

                // 2. Clic en Sintonizar Visión / Proyección Astral (Columna 2)
                int specBtnX = screenX + 6;
                int specBtnY = screenY + screenH - 22;
                int specBtnW = screenW - 12;
                int specBtnH = 17;
                if (mouseX >= specBtnX && mouseX < specBtnX + specBtnW && mouseY >= specBtnY && mouseY < specBtnY + specBtnH) {
                    PacketDistributor.sendToServer(new SpectateIncarnationPayload(target.uuid()));
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.9F, 1.3F);
                    }
                    this.onClose();
                    return true;
                }

                // 3. Columna 3: Donaciones y Comentarios
                int detX = screenX + screenW + 6;
                int detY = listY;
                int detW = PANEL_W - (listW + screenW + 20);

                // Botones de monto
                int amtBtnW = (detW - 16) / 3;
                int amtBtnH = 14;
                int amountsY0 = detY + 16;
                int amountsY1 = detY + 33;

                for (int b = 0; b < AMOUNTS.length; b++) {
                    int col = b % 3;
                    int row = b / 3;
                    int amX = detX + 5 + col * (amtBtnW + 3);
                    int amY = (row == 0) ? amountsY0 : amountsY1;

                    if (mouseX >= amX && mouseX < amX + amtBtnW && mouseY >= amY && mouseY < amY + amtBtnH) {
                        selectedAmount = AMOUNTS[b];
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.4F);
                        }
                        return true;
                    }
                }

                // Botones de comentarios
                int commentBtnW = (detW - 14) / 2;
                int commentBtnH = 13;
                int commentsStartY = detY + 65;

                for (int c = 0; c < COMMENT_KEYS.length; c++) {
                    int col = c % 2;
                    int row = c / 2;
                    int cx = detX + 5 + col * (commentBtnW + 4);
                    int cy = commentsStartY + row * (commentBtnH + 3);

                    if (mouseX >= cx && mouseX < cx + commentBtnW && mouseY >= cy && mouseY < cy + commentBtnH) {
                        selectedComment = COMMENT_KEYS[c];
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.3F);
                        }
                        return true;
                    }
                }

                // Botón de Enviar Donación
                int sendX = detX + 5;
                int sendY = detY + 174;
                int sendW = detW - 10;
                int sendH = 22;

                if (mouseX >= sendX && mouseX < sendX + sendW && mouseY >= sendY && mouseY < sendY + sendH) {
                    SystemData myData = Minecraft.getInstance().player != null ?
                            Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
                    if (myData != null && myData.coins() >= selectedAmount) {
                        PacketDistributor.sendToServer(new DonateConstellationPayload(target.uuid(), selectedAmount, selectedComment));
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.playSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.6F);
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private LivingEntity getRenderEntity(SyncStarStreamPayload.IncarnationEntry entry) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Player p = mc.level.getPlayerByUUID(entry.uuid());
            if (p != null) return p;
        }
        return mc.player;
    }

    private void drawTruncatedString(GuiGraphics g, String text, int x, int y, int maxW, int color) {
        if (font.width(text) <= maxW) {
            g.drawString(font, text, x, y, color, false);
        } else {
            String trimmed = font.plainSubstrByWidth(text, Math.max(10, maxW - font.width("..."))) + "...";
            g.drawString(font, trimmed, x, y, color, false);
        }
    }

    private String getTranslatedActivity(String rawActivity) {
        if (rawActivity == null) return "";
        return switch (rawActivity) {
            case "En Calabozo", "In Dungeon" -> Component.translatable("awakening_system.constellation.activity.dungeon").getString();
            case "¡Herido Crítico!", "Critically Wounded!" -> Component.translatable("awakening_system.constellation.activity.critical").getString();
            case "¡Modo Trance!", "Trance Mode!" -> Component.translatable("awakening_system.constellation.activity.trance").getString();
            case "Cacería Nocturna", "Night Hunting" -> Component.translatable("awakening_system.constellation.activity.night").getString();
            case "Explorando", "Exploring" -> Component.translatable("awakening_system.constellation.activity.exploring").getString();
            case "En Combate Naval", "Naval Combat" -> Component.translatable("awakening_system.constellation.activity.naval").getString();
            case "Canalizando Hebras", "Channeling Threads" -> Component.translatable("awakening_system.constellation.activity.threads").getString();
            case "Protegiendo Territorio", "Guarding Territory" -> Component.translatable("awakening_system.constellation.activity.shadow").getString();
            default -> rawActivity;
        };
    }
}
