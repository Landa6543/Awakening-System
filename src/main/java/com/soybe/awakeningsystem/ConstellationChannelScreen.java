package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Manhwa / Solo Leveling Star Stream Broadcast Screen (Canal Estelar).
 * Connects Transcendent Constellations with live mortal incarnations.
 */
public final class ConstellationChannelScreen extends Screen {
    private static final int PANEL_W = 540;
    private static final int PANEL_H = 300;

    private List<SyncStarStreamPayload.IncarnationEntry> incarnations = new ArrayList<>();
    private int selectedIndex = 0;
    private int selectedAmount = 100;
    private String selectedComment = "SATISFIED";
    private int scrollOffset = 0;

    private static final int[] AMOUNTS = {50, 100, 250, 500, 1000, 5000};
    private static final String[] COMMENT_KEYS = {
            "SATISFIED", "ENCOURAGING", "PROUD", "FASCINATED", "AMUSED", "RELIEVED", "MYTHIC", "SILENT"
    };

    private final Screen lastScreen;

    public ConstellationChannelScreen() {
        this(new SystemScreen(0));
    }

    public ConstellationChannelScreen(Screen lastScreen) {
        super(Component.translatable("awakening_system.screen.constellation.channel_title"));
        this.lastScreen = lastScreen;
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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Clean - no dark murky screen-wide filter
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 560.0F;
        float scaleY = (float) this.height / 320.0F;
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
        int listH = PANEL_H - 46;
        int cardH = 46;
        int spacing = 5;
        int visibleRows = (listH - 26) / (cardH + spacing);
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
        renderBackground(g, mouseX, mouseY, partialTick);

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

        ModernGuiHelper.GuiTheme theme = ModernGuiHelper.GuiTheme.SOLO_LEVELING;
        ModernGuiHelper.drawModernWindow(g, left, top, PANEL_W, PANEL_H, theme);

        // Close button [✕] (Right) - seamlessly integrated without dark block
        int closeX = left + PANEL_W - 32;
        int closeY = top + 14;
        int closeW = 16;
        int closeH = 16;
        boolean closeHover = mX >= closeX && mX < closeX + closeW && mY >= closeY && mY < closeY + closeH;
        if (closeHover) {
            g.fill(closeX, closeY, closeX + closeW, closeY + closeH, 0xDD7A1515);
            g.renderOutline(closeX, closeY, closeW, closeH, 0xFFFF5555);
        }
        g.drawCenteredString(font, "✕", closeX + closeW / 2, closeY + 4, closeHover ? 0xFFFFFFFF : 0xFF70E8FF);

        // Player Data & Identity
        SystemData myData = Minecraft.getInstance().player != null ?
                Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
        String rawMod = (myData != null && !myData.constellationModifier().isEmpty())
                ? myData.constellationModifier() : "";
        Component translatedMod = !rawMod.isEmpty()
                ? ConstellationAscension.getTranslatedTitleComponent(rawMod)
                : Component.translatable("awakening_system.screen.constellation.anonymous");
        String modName = translatedMod.getString();
        int myCoins = myData != null ? myData.coins() : 0;

        // Header Title (Left, cleared from corner and central filigree)
        Component headerTitle = ModernGuiHelper.manhwa(Component.translatable("awakening_system.screen.constellation.header_title"));
        g.drawString(font, headerTitle, left + 36, top + 15, 0xFF00E5FF, false);

        // Coins (Right)
        String formattedCoins = String.format(java.util.Locale.US, "%,d", myCoins);
        String coinsStr = Component.translatable("awakening_system.screen.constellation.coins", formattedCoins).getString();
        int coinsW = font.width(coinsStr);
        int coinsX = closeX - 12 - coinsW;
        g.drawString(font, coinsStr, coinsX, top + 15, 0xFFFFD700, false);

        // Cosmic Identity Badge (Center-Right, between Title and Coins)
        String identityText = "✦ " + modName + " ✦";
        int idW = font.width(identityText) + 16;
        int badgeX = coinsX - 12 - idW;
        int headerTitleEnd = left + 36 + font.width(headerTitle) + 10;
        if (badgeX > headerTitleEnd) {
            boolean badgeHov = mX >= badgeX && mX < badgeX + idW && mY >= top + 10 && mY < top + 26;
            g.fill(badgeX, top + 10, badgeX + idW, top + 26, badgeHov ? 0x4000E5FF : 0x1500E5FF);
            g.renderOutline(badgeX, top + 10, idW, 16, badgeHov ? 0xFFFFD700 : 0x4400E5FF);
            g.drawCenteredString(font, identityText, badgeX + idW / 2, top + 14, badgeHov ? 0xFFFFFFFF : 0xFFE0B0FF);

            if (badgeHov) {
                List<Component> tip = List.of(
                        Component.translatable("awakening_system.screen.constellation.tooltip.identity_title"),
                        Component.translatable("awakening_system.screen.constellation.tooltip.identity_desc1"),
                        Component.translatable("awakening_system.screen.constellation.tooltip.identity_desc2")
                );
                g.renderComponentTooltip(font, tip, mX, mY);
            }
        }

        // Three-column geometry
        int listX = left + 14;
        int listY = top + 34;
        int listW = 145;
        int listH = PANEL_H - 46;

        int screenX = listX + listW + 8;
        int screenY = listY;
        int screenW = 168;
        int screenH = listH;

        int detX = screenX + screenW + 8;
        int detY = listY;
        int detW = left + PANEL_W - 14 - detX;
        int detH = listH;

        // ================= COLUMN 1: INCARNATIONS =================
        Component incHeader = Component.literal(Component.translatable("awakening_system.screen.constellation.incarnations").getString() + " §7(" + incarnations.size() + ")");
        ModernGuiHelper.drawModernSection(g, listX, listY, listW, listH, incHeader, theme, font);

        int cardH = 46;
        int spacing = 5;
        int visibleRows = (listH - 26) / (cardH + spacing);

        if (incarnations.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.constellation.searching_incarnations").getString(), listX + listW / 2, listY + listH / 2 - 4, 0xFF8AAAB8);
        } else {
            for (int i = 0; i < visibleRows && (i + scrollOffset) < incarnations.size(); i++) {
                int idx = i + scrollOffset;
                SyncStarStreamPayload.IncarnationEntry inc = incarnations.get(idx);
                int cardY = listY + 22 + i * (cardH + spacing);
                int cardX = listX + 5;
                int cardW = listW - 10;

                boolean isSel = (idx == selectedIndex);
                boolean hov = mX >= cardX && mX < cardX + cardW && mY >= cardY && mY < cardY + cardH;

                int bgCol = isSel ? 0x4000E5FF : (hov ? 0x2200E5FF : 0x0A00E5FF);
                int borderCol = isSel ? 0xFF00E5FF : (hov ? 0xAA00E5FF : 0x2500E5FF);

                g.fill(cardX, cardY, cardX + cardW, cardY + cardH, bgCol);
                g.renderOutline(cardX, cardY, cardW, cardH, borderCol);

                if (isSel) {
                    // Left cyan neon accent line
                    g.fill(cardX, cardY, cardX + 3, cardY + cardH, 0xFF00E5FF);
                }

                int maxTextW = cardW - 10;
                String dot = isSel ? "§b▶ " : "§7● ";
                drawTruncatedString(g, dot + inc.name(), cardX + 6, cardY + 4, maxTextW, isSel ? 0xFFFFFFFF : 0xFFDDECF2);

                String rankDisplay = inc.rank();
                if ("ABSOLUTE_GOD".equalsIgnoreCase(rankDisplay)) rankDisplay = Component.translatable("awakening_system.screen.constellation.rank.absolute_god").getString();
                else if ("NATIONAL_LEVEL".equalsIgnoreCase(rankDisplay)) rankDisplay = Component.translatable("awakening_system.screen.constellation.rank.national").getString();
                Component rankComp = Component.translatable("awakening_system.screen.constellation.rank_level", rankDisplay, inc.level());
                drawTruncatedString(g, rankComp.getString(), cardX + 6, cardY + 16, maxTextW, 0xFF8AAAB8);

                drawTruncatedString(g, "§a" + getTranslatedActivity(inc.activity()), cardX + 6, cardY + 28, maxTextW, 0xFF50FA7B);
            }

            // Scrollbar indicator
            if (incarnations.size() > visibleRows) {
                int maxScroll = incarnations.size() - visibleRows;
                int barTrackH = listH - 26;
                int thumbH = Math.max(14, barTrackH * visibleRows / incarnations.size());
                int thumbY = listY + 22 + (barTrackH - thumbH) * scrollOffset / maxScroll;
                int barX = listX + listW - 4;
                g.fill(barX, listY + 22, barX + 2, listY + 22 + barTrackH, 0x2000E5FF);
                g.fill(barX, thumbY, barX + 2, thumbY + thumbH, 0xCC00E5FF);
            }
        }

        // ================= COLUMN 2: LIVE STREAM & 3D MODEL =================
        boolean liveBlink = (System.currentTimeMillis() / 600) % 2 == 0;
        Component liveHeader = Component.literal((liveBlink ? "§c● " : "§4● ") + Component.translatable("awakening_system.screen.constellation.live", selectedIndex + 1).getString());
        ModernGuiHelper.drawModernSection(g, screenX, screenY, screenW, screenH, liveHeader, theme, font);

        if (!incarnations.isEmpty() && selectedIndex >= 0 && selectedIndex < incarnations.size()) {
            SyncStarStreamPayload.IncarnationEntry target = incarnations.get(selectedIndex);

            // 3D Viewport Box
            int viewX1 = screenX + 5;
            int viewY1 = screenY + 20;
            int viewX2 = screenX + screenW - 5;
            int viewY2 = screenY + 128;
            g.fill(viewX1, viewY1, viewX2, viewY2, 0x0A00E5FF);
            g.renderOutline(viewX1, viewY1, viewX2 - viewX1, viewY2 - viewY1, 0x4400E5FF);

            // Sci-fi corner brackets
            int cornerLen = 5;
            g.fill(viewX1, viewY1, viewX1 + cornerLen, viewY1 + 1, 0xFF00E5FF);
            g.fill(viewX1, viewY1, viewX1 + 1, viewY1 + cornerLen, 0xFF00E5FF);
            g.fill(viewX2 - cornerLen, viewY1, viewX2, viewY1 + 1, 0xFF00E5FF);
            g.fill(viewX2 - 1, viewY1, viewX2, viewY1 + cornerLen, 0xFF00E5FF);
            g.fill(viewX1, viewY2 - 1, viewX1 + cornerLen, viewY2, 0xFF00E5FF);
            g.fill(viewX1, viewY2 - cornerLen, viewX1 + 1, viewY2, 0xFF00E5FF);
            g.fill(viewX2 - cornerLen, viewY2 - 1, viewX2, viewY2, 0xFF00E5FF);
            g.fill(viewX2 - 1, viewY2 - cornerLen, viewX2, viewY2, 0xFF00E5FF);

            // Holographic floor ring
            int centerX = (viewX1 + viewX2) / 2;
            int feetY = viewY2 - 8;
            g.fill(centerX - 24, feetY, centerX + 24, feetY + 3, 0x5000E5FF);
            g.fill(centerX - 18, feetY + 1, centerX + 18, feetY + 2, 0xCC70E8FF);

            LivingEntity renderEntity = getRenderEntity(target);
            if (renderEntity != null) {
                Minecraft mc = Minecraft.getInstance();
                boolean prevHitboxes = mc.getEntityRenderDispatcher().shouldRenderHitBoxes();
                mc.getEntityRenderDispatcher().setRenderHitBoxes(false);
                try {
                    InventoryScreen.renderEntityInInventoryFollowsMouse(
                            g, viewX1, viewY1 + 2, viewX2, viewY2 - 4, 34, 0.0625F,
                            (float) mX, (float) mY, renderEntity
                    );
                } finally {
                    mc.getEntityRenderDispatcher().setRenderHitBoxes(prevHitboxes);
                }
            }

            // Stats Area Container
            int statCardY = screenY + 132;
            int statCardH = 70;
            g.fill(screenX + 5, statCardY, screenX + screenW - 5, statCardY + statCardH, 0x0A00E5FF);
            g.renderOutline(screenX + 5, statCardY, screenW - 10, statCardH, 0x4400E5FF);

            int telY = statCardY + 5;
            int maxTelW = screenW - 16;
            drawTruncatedString(g, "§f§l" + target.name(), screenX + 8, telY, maxTelW, 0xFFFFFFFF);
            telY += 12;
            drawTruncatedString(g, Component.translatable("awakening_system.screen.constellation.rank_level", target.rank(), target.level()).getString(), screenX + 8, telY, maxTelW, 0xFFDDECF2);
            telY += 11;
            drawTruncatedString(g, Component.translatable("awakening_system.screen.constellation.dim", target.dimension()).getString(), screenX + 8, telY, maxTelW, 0xFF8AAAB8);
            telY += 15;

            // Health Gauge
            int hpBarW = screenW - 16;
            int hpBarH = 11;
            float hpPct = Math.max(0.0F, Math.min(1.0F, (float) target.hp() / Math.max(1, target.maxHp())));
            ModernGuiHelper.drawModernGauge(g, screenX + 8, telY, hpBarW, hpBarH, hpPct, 0xFFFF3B30, 0xFFCC1111, Component.literal(target.hp() + "/" + target.maxHp() + " HP"), font);

            // Tune Vision Action Button
            int specBtnX = screenX + 5;
            int specBtnY = screenY + 206;
            int specBtnW = screenW - 10;
            int specBtnH = 22;
            boolean specHov = mX >= specBtnX && mX < specBtnX + specBtnW && mY >= specBtnY && mY < specBtnY + specBtnH;

            boolean isSelf = Minecraft.getInstance().player != null && target.uuid().equals(Minecraft.getInstance().player.getUUID());
            boolean isMock = target.uuid().toString().startsWith("00000000-0000-0000-0000-");

            if (isSelf) {
                g.fill(specBtnX, specBtnY, specBtnX + specBtnW, specBtnY + specBtnH, 0x1800E5FF);
                g.renderOutline(specBtnX, specBtnY, specBtnW, specBtnH, 0x4400E5FF);
                drawAutoScaledCenteredString(g, ModernGuiHelper.manhwa(Component.translatable("awakening_system.screen.constellation.cosmic_avatar")), specBtnX + specBtnW / 2, specBtnY + (specBtnH - 8) / 2, specBtnW - 8, 0xFF8AAAB8);
            } else if (isMock) {
                g.fill(specBtnX, specBtnY, specBtnX + specBtnW, specBtnY + specBtnH, specHov ? 0x603D1A58 : 0x40251038);
                g.renderOutline(specBtnX, specBtnY, specBtnW, specBtnH, specHov ? 0xFFFFD700 : 0x889B51E0);
                drawAutoScaledCenteredString(g, ModernGuiHelper.manhwa(Component.translatable("awakening_system.screen.constellation.simulation_btn")), specBtnX + specBtnW / 2, specBtnY + (specBtnH - 8) / 2, specBtnW - 8, specHov ? 0xFFFFD700 : 0xFFE0B0FF);
            } else {
                g.fill(specBtnX, specBtnY, specBtnX + specBtnW, specBtnY + specBtnH, specHov ? 0xDD782098 : 0x99481A78);
                g.renderOutline(specBtnX, specBtnY, specBtnW, specBtnH, specHov ? 0xFFFFD700 : 0xFFB388FF);
                drawAutoScaledCenteredString(g, ModernGuiHelper.manhwa(Component.translatable("awakening_system.screen.constellation.tune_vision")), specBtnX + specBtnW / 2, specBtnY + (specBtnH - 8) / 2, specBtnW - 8, specHov ? 0xFFFFD700 : 0xFFFFFFFF);
            }

            // ================= COLUMN 3: DONATIONS & REACTIONS =================
            ModernGuiHelper.drawModernSection(g, detX, detY, detW, detH, Component.literal(""), theme, font);

            // Section 1 Header: Coins
            Component coinHead = ModernGuiHelper.manhwa(Component.translatable("awakening_system.screen.constellation.coins_to_donate"));
            g.drawString(font, coinHead, detX + 6, detY + 6, 0xFFFFD700, false);

            int amtBtnW = (detW - 18) / 3;
            int amtBtnH = 16;
            int amountsY0 = detY + 18;
            int amountsY1 = detY + 37;

            for (int b = 0; b < AMOUNTS.length; b++) {
                int col = b % 3;
                int row = b / 3;
                int amX = detX + 5 + col * (amtBtnW + 4);
                int amY = (row == 0) ? amountsY0 : amountsY1;
                int amVal = AMOUNTS[b];
                boolean isCurAmt = (amVal == selectedAmount);
                boolean amHov = mX >= amX && mX < amX + amtBtnW && mY >= amY && mY < amY + amtBtnH;

                int aBg = isCurAmt ? 0x80FFB800 : (amHov ? 0x3500E5FF : 0x0A00E5FF);
                int aBorder = isCurAmt ? 0xFFFFD700 : (amHov ? 0xFF00E5FF : 0x3000E5FF);

                g.fill(amX, amY, amX + amtBtnW, amY + amtBtnH, aBg);
                g.renderOutline(amX, amY, amtBtnW, amtBtnH, aBorder);

                String label = amVal >= 1000 ? (amVal / 1000 + "k") : String.valueOf(amVal);
                int aTextCol = isCurAmt ? 0xFFFFFFFF : (amHov ? 0xFFFFFFFF : 0xFFB5D5E8);
                drawAutoScaledCenteredString(g, ModernGuiHelper.manhwa(label), amX + amtBtnW / 2, amY + 4, amtBtnW - 4, aTextCol);
            }

            // Section 2 Header: Cosmic Reactions
            Component reactHead = ModernGuiHelper.manhwa(Component.translatable("awakening_system.screen.constellation.cosmic_reaction"));
            g.drawString(font, reactHead, detX + 6, detY + 57, 0xFF70E8FF, false);

            int commentBtnW = (detW - 16) / 2;
            int commentBtnH = 14;
            int commentsStartY = detY + 69;

            for (int c = 0; c < COMMENT_KEYS.length; c++) {
                int col = c % 2;
                int row = c / 2;
                int cx = detX + 5 + col * (commentBtnW + 6);
                int cy = commentsStartY + row * 16;
                String cKey = COMMENT_KEYS[c];
                boolean isCurC = cKey.equals(selectedComment);
                boolean cHov = mX >= cx && mX < cx + commentBtnW && mY >= cy && mY < cy + commentBtnH;

                int cBg = isCurC ? 0x5000E5FF : (cHov ? 0x3000E5FF : 0x0A00E5FF);
                int cBorder = isCurC ? 0xFF00E5FF : (cHov ? 0xAA00E5FF : 0x2500E5FF);

                g.fill(cx, cy, cx + commentBtnW, cy + commentBtnH, cBg);
                g.renderOutline(cx, cy, commentBtnW, commentBtnH, cBorder);

                String cLabel = Component.translatable("awakening_system.constellation.comment." + c).getString();
                int cTextCol = isCurC ? 0xFFFFFFFF : (cHov ? 0xFFFFFFFF : 0xFF9BB7C7);
                drawAutoScaledCenteredString(g, ModernGuiHelper.manhwa(cLabel), cx + commentBtnW / 2, cy + 3, commentBtnW - 4, cTextCol);
            }

            // Section 3: Broadcast Preview Box
            int previewY = detY + 135;
            int previewH = 44;
            int previewW = detW - 10;
            g.fill(detX + 5, previewY, detX + 5 + previewW, previewY + previewH, 0x0A00E5FF);
            g.renderOutline(detX + 5, previewY, previewW, previewH, 0x4400E5FF);

            String preview = ConstellationChannelHandler.getCommentComponent(selectedComment, modName, selectedAmount).getString();
            var splitLines = font.split(Component.literal(preview), previewW - 8);
            int lineY = previewY + 4;
            g.enableScissor(detX + 6, previewY + 1, detX + 5 + previewW - 1, previewY + previewH - 1);
            for (int li = 0; li < Math.min(4, splitLines.size()); li++) {
                g.drawString(font, splitLines.get(li), detX + 8, lineY, 0xFFEAF5FA, false);
                lineY += 10;
            }
            g.disableScissor();

            // Section 4: Sponsor Button (Rich radiant gold gradient)
            int sendX = detX + 5;
            int sendY = detY + 183;
            int sendW = detW - 10;
            int sendH = 24;
            boolean canAfford = myCoins >= selectedAmount;
            boolean sendHov = mX >= sendX && mX < sendX + sendW && mY >= sendY && mY < sendY + sendH;

            if (canAfford) {
                int colTop = sendHov ? 0xFFFFD700 : 0xFFFFAA00;
                int colBtm = sendHov ? 0xFFFFB800 : 0xFFCC7700;
                g.fillGradient(sendX, sendY, sendX + sendW, sendY + sendH, colTop, colBtm);
                g.renderOutline(sendX, sendY, sendW, sendH, sendHov ? 0xFFFFFFFF : 0xFFFFD700);

                String formattedSendAmt = String.format(java.util.Locale.US, "%,d", selectedAmount);
                Component sendText = Component.translatable("awakening_system.screen.constellation.sponsor_btn", formattedSendAmt);
                drawAutoScaledCenteredString(g, ModernGuiHelper.manhwa(sendText), sendX + sendW / 2, sendY + (sendH - 8) / 2, sendW - 10, 0xFFFFFFFF);
            } else {
                g.fill(sendX, sendY, sendX + sendW, sendY + sendH, 0x1400E5FF);
                g.renderOutline(sendX, sendY, sendW, sendH, 0x3500E5FF);
                Component sendText = Component.translatable("awakening_system.screen.constellation.insufficient_coins");
                drawAutoScaledCenteredString(g, ModernGuiHelper.manhwa(sendText), sendX + sendW / 2, sendY + (sendH - 8) / 2, sendW - 10, 0xFF888888);
            }

            // Footnote hint: ESC to return
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.constellation.back_system").getString(), detX + detW / 2, detY + 212, 0xFF8AAAB8);
        } else {
            g.drawCenteredString(font, Component.translatable("awakening_system.screen.constellation.searching_incarnations").getString(), screenX + screenW / 2, screenY + 80, 0xFF8AAAB8);
        }

        g.pose().popPose();
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen != null ? this.lastScreen : new SystemScreen(0));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button == 0) {
            int left = (this.width - PANEL_W) / 2;
            int top = (this.height - PANEL_H) / 2;

            // Close button [✕]
            int closeX = left + PANEL_W - 32;
            int closeY = top + 14;
            int closeW = 16;
            int closeH = 16;
            if (mouseX >= closeX && mouseX < closeX + closeW && mouseY >= closeY && mouseY < closeY + closeH) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.9F));
                onClose();
                return true;
            }

            int listX = left + 14;
            int listY = top + 34;
            int listW = 145;
            int listH = PANEL_H - 46;
            int cardH = 46;
            int spacing = 5;
            int visibleRows = (listH - 26) / (cardH + spacing);

            // Incarnation card click
            for (int i = 0; i < visibleRows && (i + scrollOffset) < incarnations.size(); i++) {
                int cardY = listY + 22 + i * (cardH + spacing);
                int cardX = listX + 5;
                int cardW = listW - 10;
                if (mouseX >= cardX && mouseX <= cardX + cardW && mouseY >= cardY && mouseY < cardY + cardH) {
                    selectedIndex = i + scrollOffset;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    return true;
                }
            }

            int screenX = listX + listW + 8;
            int screenY = listY;
            int screenW = 168;
            int screenH = listH;

            if (!incarnations.isEmpty() && selectedIndex >= 0 && selectedIndex < incarnations.size()) {
                SyncStarStreamPayload.IncarnationEntry target = incarnations.get(selectedIndex);

                // Tune Vision button click
                int specBtnX = screenX + 5;
                int specBtnY = screenY + 206;
                int specBtnW = screenW - 10;
                int specBtnH = 22;
                if (mouseX >= specBtnX && mouseX < specBtnX + specBtnW && mouseY >= specBtnY && mouseY < specBtnY + specBtnH) {
                    boolean isSelf = Minecraft.getInstance().player != null && target.uuid().equals(Minecraft.getInstance().player.getUUID());
                    boolean isMock = target.uuid().toString().startsWith("00000000-0000-0000-0000-");
                    if (isSelf) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F));
                        return true;
                    }
                    if (isMock) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 0.9F));
                        return true;
                    }
                    PacketDistributor.sendToServer(new SpectateIncarnationPayload(target.uuid()));
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.3F));
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(null);
                    }
                    return true;
                }

                int detX = screenX + screenW + 8;
                int detY = listY;
                int detW = left + PANEL_W - 14 - detX;

                // Amount buttons click
                int amtBtnW = (detW - 18) / 3;
                int amtBtnH = 16;
                int amountsY0 = detY + 18;
                int amountsY1 = detY + 37;

                for (int b = 0; b < AMOUNTS.length; b++) {
                    int col = b % 3;
                    int row = b / 3;
                    int amX = detX + 5 + col * (amtBtnW + 4);
                    int amY = (row == 0) ? amountsY0 : amountsY1;

                    if (mouseX >= amX && mouseX < amX + amtBtnW && mouseY >= amY && mouseY < amY + amtBtnH) {
                        selectedAmount = AMOUNTS[b];
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.4F));
                        return true;
                    }
                }

                // Reaction buttons click
                int commentBtnW = (detW - 16) / 2;
                int commentBtnH = 14;
                int commentsStartY = detY + 69;

                for (int c = 0; c < COMMENT_KEYS.length; c++) {
                    int col = c % 2;
                    int row = c / 2;
                    int cx = detX + 5 + col * (commentBtnW + 6);
                    int cy = commentsStartY + row * 16;

                    if (mouseX >= cx && mouseX < cx + commentBtnW && mouseY >= cy && mouseY < cy + commentBtnH) {
                        selectedComment = COMMENT_KEYS[c];
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.3F));
                        return true;
                    }
                }

                // Sponsor button click
                int sendX = detX + 5;
                int sendY = detY + 183;
                int sendW = detW - 10;
                int sendH = 24;

                if (mouseX >= sendX && mouseX < sendX + sendW && mouseY >= sendY && mouseY < sendY + sendH) {
                    SystemData myData = Minecraft.getInstance().player != null ?
                            Minecraft.getInstance().player.getData(AwakeningSystemMod.PLAYER_SYSTEM) : null;
                    if (myData != null && myData.coins() >= selectedAmount) {
                        PacketDistributor.sendToServer(new DonateConstellationPayload(target.uuid(), selectedAmount, selectedComment));
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.6F));
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawAutoScaledCenteredString(GuiGraphics g, Component text, int centerX, int y, int maxW, int color) {
        if (text == null) return;
        int textW = font.width(text);
        if (textW > maxW && maxW > 0) {
            var pose = g.pose();
            pose.pushPose();
            float s = (float) maxW / textW;
            pose.translate(centerX, y + (8 * (1.0F - s)) / 2.0F, 0);
            pose.scale(s, s, 1.0F);
            g.drawCenteredString(font, text, 0, 0, color);
            pose.popPose();
        } else {
            g.drawCenteredString(font, text, centerX, y, color);
        }
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
            case "In Dungeon" -> Component.translatable("awakening_system.constellation.activity.dungeon").getString();
            case "Critically Wounded!" -> Component.translatable("awakening_system.constellation.activity.critical").getString();
            case "Trance Mode!" -> Component.translatable("awakening_system.constellation.activity.trance").getString();
            case "Night Hunting" -> Component.translatable("awakening_system.constellation.activity.night").getString();
            case "Exploring" -> Component.translatable("awakening_system.constellation.activity.exploring").getString();
            case "Naval Combat" -> Component.translatable("awakening_system.constellation.activity.naval").getString();
            case "Channeling Threads" -> Component.translatable("awakening_system.constellation.activity.threads").getString();
            case "Guarding Territory" -> Component.translatable("awakening_system.constellation.activity.shadow").getString();
            default -> rawActivity;
        };
    }
}
