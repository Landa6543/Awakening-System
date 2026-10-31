package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.block.BountyBoardBlock;
import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Pantalla interactiva del Gran Tablón de Anuncios y Cacerías del Gremio.
 * Muestra la selección aleatoria de contratos asignada específicamente al jugador,
 * con sellos de cera por rango (F a S), expedientes de cacería, botón de rotación y reclamo.
 */
public final class BountyBoardScreen extends Screen {
    private final OpenBountyBoardPayload payload;
    private int selectedIndex = 0;
    private int listScroll = 0;

    public BountyBoardScreen(OpenBountyBoardPayload payload) {
        super(Component.translatable("awakening_system.bounty.screen_title"));
        this.payload = payload;

        int[] offers = payload.availableBounties();
        if (payload.hasActiveBounty() && offers != null && offers.length > 0) {
            for (int i = 0; i < offers.length; i++) {
                if (offers[i] == payload.activeIndex()) {
                    this.selectedIndex = i;
                    this.listScroll = Math.max(0, Math.min(this.selectedIndex, offers.length - 6));
                    break;
                }
            }
        }
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
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int boardW = 380;
        int boardH = 236;
        int left = (this.width - boardW) / 2;
        int top = (this.height - boardH) / 2;
        if (mouseX < left || mouseX > left + boardW || mouseY < top || mouseY > top + boardH) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int[] offers = payload.availableBounties();
        int total = offers != null ? offers.length : 0;
        int maxScroll = Math.max(0, total - 6);
        if (scrollY > 0) {
            listScroll = Math.max(0, listScroll - 1);
            return true;
        } else if (scrollY < 0) {
            listScroll = Math.min(maxScroll, listScroll + 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 400.0F;
        float scaleY = (float) this.height / 255.0F;
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
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

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

        int boardW = 380;
        int boardH = 236;
        int left = (this.width - boardW) / 2;
        int top = (this.height - boardH) / 2;

        ModernGuiHelper.drawModernWindow(g, left, top, boardW, boardH, ModernGuiHelper.GuiTheme.SOLO_LEVELING);

        int closeX = left + boardW - 24;
        int closeY = top + 8;
        boolean closeHov = mX >= closeX && mX < closeX + 16 && mY >= closeY && mY < closeY + 16;
        g.fill(closeX, closeY, closeX + 16, closeY + 16, closeHov ? 0xDD7A1515 : 0x55122030);
        g.renderOutline(closeX, closeY, 16, 16, closeHov ? 0xFFFF5555 : 0x7700E5FF);
        g.drawCenteredString(this.font, "✕", closeX + 8, closeY + 4, closeHov ? 0xFFFFFFFF : 0xFF70E8FF);

        g.drawCenteredString(this.font, Component.translatable("awakening_system.bounty_board.title").copy().withStyle(ChatFormatting.BOLD), left + boardW / 2, top + 10, 0xFFFFD700);

        int refX = left + boardW - 96;
        int refY = top + 26;
        int refW = 68;
        int refH = 14;
        boolean refHov = mX >= refX && mX <= refX + refW && mY >= refY && mY <= refY + refH;
        g.fill(refX, refY, refX + refW, refY + refH, refHov ? 0xDD2A4560 : 0x55122030);
        g.renderOutline(refX, refY, refW, refH, refHov ? 0xFFFFFFFF : 0x7700E5FF);
        g.drawCenteredString(this.font, Component.translatable("awakening_system.bounty_board.btn_refresh"), refX + refW / 2, refY + 3, refHov ? 0xFFFFFFFF : 0xFF70E8FF);

        int needed = payload.nextRankNeeded();
        int completed = payload.completedBounties();
        int remain = Math.max(0, needed - completed);
        Component rankStr = Component.translatable("awakening_system.bounty_board.rank_status", payload.guildRank(), completed, remain);
        g.drawString(this.font, rankStr, left + 14, top + 29, 0xFFCCE3EC, false);

        int listX = left + 10;
        int listY = top + 44;
        int listW = 156;
        int listH = 180;
        g.fill(listX, listY, listX + listW, listY + listH, 0x40081422);
        g.renderOutline(listX, listY, listW, listH, 0x5500E5FF);

        int[] offers = payload.availableBounties();
        int totalOffers = (offers != null) ? offers.length : 0;
        int maxScroll = Math.max(0, totalOffers - 6);

        if (listScroll > 0) {
            g.fill(listX + listW - 16, listY + 2, listX + listW - 2, listY + 12, 0xFF142436);
            g.renderOutline(listX + listW - 16, listY + 2, 14, 10, 0xAA00E5FF);
            g.drawCenteredString(this.font, "▲", listX + listW - 9, listY + 3, 0xFFFFD700);
        }
        if (listScroll < maxScroll) {
            g.fill(listX + listW - 16, listY + listH - 12, listX + listW - 2, listY + listH - 2, 0xFF142436);
            g.renderOutline(listX + listW - 16, listY + listH - 12, 14, 10, 0xAA00E5FF);
            g.drawCenteredString(this.font, "▼", listX + listW - 9, listY + listH - 11, 0xFFFFD700);
        }

        int visibleCount = Math.min(6, totalOffers);
        for (int i = 0; i < visibleCount; i++) {
            int slotIdx = listScroll + i;
            if (slotIdx >= totalOffers) break;
            int globalIdx = offers[slotIdx];
            if (globalIdx < 0 || globalIdx >= BountyBoardBlock.ALL_BOUNTIES.size()) continue;

            BountyBoardBlock.BountyTemplate item = BountyBoardBlock.ALL_BOUNTIES.get(globalIdx);
            int itemY = listY + 5 + i * 28;
            boolean isSelected = (slotIdx == selectedIndex);
            boolean isHovered = mX >= listX + 2 && mX <= listX + listW - 2 && mY >= itemY && mY <= itemY + 26;

            int bgCol = isSelected ? 0xDD183F57 : (isHovered ? 0x9912283A : 0x550A1624);
            int borderCol = isSelected ? 0xFF00FFFF : (isHovered ? 0xFF70E8FF : 0x3500E5FF);
            g.fill(listX + 2, itemY, listX + listW - 2, itemY + 27, bgCol);
            g.renderOutline(listX + 2, itemY, listW - 4, 27, borderCol);

            int rankColor = switch (item.rank) {
                case "F" -> 0xFF888888;
                case "E" -> 0xFF55AA55;
                case "D" -> 0xFF33BB77;
                case "C" -> 0xFF2299DD;
                case "B" -> 0xFFDDAA11;
                case "A" -> 0xFFEE5522;
                default  -> 0xFFAA22CC;
            };
            g.fill(listX + 2, itemY, listX + 6, itemY + 27, rankColor);

            g.fill(listX + 8, itemY + 5, listX + 22, itemY + 22, (rankColor & 0x00FFFFFF) | 0x33000000);
            g.renderOutline(listX + 8, itemY + 5, 14, 17, rankColor);
            g.drawCenteredString(this.font, item.rank, listX + 15, itemY + 9, rankColor);

            String title = Component.translatable(item.nameKey).getString();
            int maxTitleW = listW - 32;
            if (this.font.width(title) > maxTitleW) {
                title = this.font.plainSubstrByWidth(title, maxTitleW - 6) + "…";
            }
            g.drawString(this.font, title, listX + 26, itemY + 4, isSelected ? 0xFFFFFFFF : 0xFFD4E6F1, false);

            if (payload.hasActiveBounty() && payload.activeIndex() == globalIdx) {
                if (payload.activeKilled() >= payload.activeNeeded()) {
                    g.drawString(this.font, Component.translatable("awakening_system.bounty_board.ready"), listX + 26, itemY + 15, 0xFF50FA7B, false);
                } else {
                    g.drawString(this.font, "§c[" + payload.activeKilled() + "/" + payload.activeNeeded() + "] §eEn curso", listX + 26, itemY + 15, 0xFFFFD700, false);
                }
            } else {
                g.drawString(this.font, "§e" + item.coins + " ⛃ §7| §d" + item.medals + "🏅", listX + 26, itemY + 15, 0xFF88C999, false);
            }
        }

        int detX = left + 174;
        int detY = top + 44;
        int detW = 196;
        int detH = 180;

        g.fill(detX, detY, detX + detW, detY + detH, 0x40081422);
        g.renderOutline(detX, detY, detW, detH, 0x5500E5FF);

        if (offers != null && selectedIndex >= 0 && selectedIndex < offers.length) {
            int selGlobalIdx = offers[selectedIndex];
            if (selGlobalIdx >= 0 && selGlobalIdx < BountyBoardBlock.ALL_BOUNTIES.size()) {
                BountyBoardBlock.BountyTemplate sel = BountyBoardBlock.ALL_BOUNTIES.get(selGlobalIdx);
                int rCol = switch (sel.rank) {
                    case "F" -> 0xFF888888;
                    case "E" -> 0xFF55AA55;
                    case "D" -> 0xFF33BB77;
                    case "C" -> 0xFF2299DD;
                    case "B" -> 0xFFDDAA11;
                    case "A" -> 0xFFEE5522;
                    default  -> 0xFFAA22CC;
                };

                g.fill(detX + 6, detY + 6, detX + detW - 6, detY + 22, (rCol & 0x00FFFFFF) | 0x33000000);
                g.renderOutline(detX + 6, detY + 6, detW - 12, 16, rCol);
                g.drawCenteredString(this.font, Component.translatable("awakening_system.bounty_board.contract_rank", sel.rank).copy().withStyle(ChatFormatting.BOLD), detX + detW / 2, detY + 10, rCol);

                String fullTitle = Component.translatable(sel.nameKey).getString();
                List<FormattedCharSequence> titleLines = this.font.split(Component.literal("§f§l" + fullTitle), detW - 16);
                int ty = detY + 25;
                for (FormattedCharSequence line : titleLines) {
                    int lw = this.font.width(line);
                    g.drawString(this.font, line, detX + (detW - lw) / 2, ty, 0xFFFFFFFF, false);
                    ty += 10;
                }

                int objY = Math.max(ty + 3, detY + 46);
                g.drawString(this.font, Component.translatable("awakening_system.bounty_board.objective_header"), detX + 10, objY, 0xFF00E5FF, false);

                Component targetDesc = switch (sel.targetType) {
                    case "collect_flower" -> Component.translatable("awakening_system.bounty_target.collect_flower", sel.count);
                    case "collect_wood" -> Component.translatable("awakening_system.bounty_target.collect_wood", sel.count);
                    case "collect_iron" -> Component.translatable("awakening_system.bounty_target.collect_iron", sel.count);
                    case "collect_wheat" -> Component.translatable("awakening_system.bounty_target.collect_wheat", sel.count);
                    case "collect_feather" -> Component.translatable("awakening_system.bounty_target.collect_feather", sel.count);
                    case "collect_bone" -> Component.translatable("awakening_system.bounty_target.collect_bone", sel.count);
                    case "collect_berries" -> Component.translatable("awakening_system.bounty_target.collect_berries", sel.count);
                    case "collect_mana_pork" -> Component.translatable("awakening_system.bounty_target.collect_mana_pork", sel.count);
                    case "collect_leather" -> Component.translatable("awakening_system.bounty_target.collect_leather", sel.count);
                    case "collect_bamboo" -> Component.translatable("awakening_system.bounty_target.collect_bamboo", sel.count);
                    case "collect_gold" -> Component.translatable("awakening_system.bounty_target.collect_gold", sel.count);
                    case "collect_mana_wool" -> Component.translatable("awakening_system.bounty_target.collect_mana_wool", sel.count);
                    case "collect_redstone" -> Component.translatable("awakening_system.bounty_target.collect_redstone", sel.count);
                    case "collect_lapis" -> Component.translatable("awakening_system.bounty_target.collect_lapis", sel.count);
                    case "collect_quartz" -> Component.translatable("awakening_system.bounty_target.collect_quartz", sel.count);
                    case "collect_blaze_rod" -> Component.translatable("awakening_system.bounty_target.collect_blaze_rod", sel.count);
                    case "collect_diamond" -> Component.translatable("awakening_system.bounty_target.collect_diamond", sel.count);

                    case "isekai_slime" -> Component.translatable("awakening_system.bounty_target.isekai_slime", sel.count);
                    case "giant_slime" -> Component.translatable("awakening_system.bounty_target.giant_slime", sel.count);
                    case "goblin_scout" -> Component.translatable("awakening_system.bounty_target.goblin_scout", sel.count);
                    case "goblin_warrior" -> Component.translatable("awakening_system.bounty_target.goblin_warrior", sel.count);
                    case "goblin_shaman" -> Component.translatable("awakening_system.bounty_target.goblin_shaman", sel.count);
                    case "goblin_threat" -> Component.translatable("awakening_system.bounty_target.goblin_threat", sel.count);
                    case "isekai_wolf" -> Component.translatable("awakening_system.bounty_target.isekai_wolf", sel.count);
                    case "isekai_skeleton" -> Component.translatable("awakening_system.bounty_target.isekai_skeleton", sel.count);
                    case "isekai_spider" -> Component.translatable("awakening_system.bounty_target.isekai_spider", sel.count);
                    case "isekai_beholder" -> Component.translatable("awakening_system.bounty_target.isekai_beholder", sel.count);
                    case "isekai_guardian" -> Component.translatable("awakening_system.bounty_target.isekai_guardian", sel.count);
                    case "dragon_threat" -> Component.translatable("awakening_system.bounty_target.dragon_threat", sel.count);
                    case "isekai_monster" -> Component.translatable("awakening_system.bounty_target.isekai_monster", sel.count);

                    case "bat_hunt" -> Component.translatable("awakening_system.bounty_target.bat_hunt", sel.count);
                    case "drowned_hunt" -> Component.translatable("awakening_system.bounty_target.drowned_hunt", sel.count);
                    case "hilichurl_hunt" -> Component.translatable("awakening_system.bounty_target.hilichurl_hunt", sel.count);
                    case "mitachurl_hunt" -> Component.translatable("awakening_system.bounty_target.mitachurl_hunt", sel.count);
                    case "ruin_guard_hunt" -> Component.translatable("awakening_system.bounty_target.ruin_guard_hunt", sel.count);
                    case "shadow_direwolf" -> Component.translatable("awakening_system.bounty_target.shadow_direwolf", sel.count);
                    case "geovishap_hunt" -> Component.translatable("awakening_system.bounty_target.geovishap_hunt", sel.count);
                    case "ruin_hunter_hunt" -> Component.translatable("awakening_system.bounty_target.ruin_hunter_hunt", sel.count);
                    case "ruin_drake_hunt" -> Component.translatable("awakening_system.bounty_target.ruin_drake_hunt", sel.count);
                    case "boss_snowboar_king" -> Component.translatable("awakening_system.bounty_target.boss_snowboar_king", sel.count);
                    case "spirit_fox" -> Component.translatable("awakening_system.bounty_target.spirit_fox", sel.count);
                    case "creeper_hunt" -> Component.translatable("awakening_system.bounty_target.creeper_hunt", sel.count);
                    case "frost_elf" -> Component.translatable("awakening_system.bounty_target.frost_elf", sel.count);
                    case "mandrake_hunt" -> Component.translatable("awakening_system.bounty_target.mandrake_hunt", sel.count);
                    case "cave_spider" -> Component.translatable("awakening_system.bounty_target.cave_spider", sel.count);
                    case "beastman_warrior" -> Component.translatable("awakening_system.bounty_target.beastman_warrior", sel.count);
                    case "blood_disciple" -> Component.translatable("awakening_system.bounty_target.blood_disciple", sel.count);
                    case "enderman_hunt" -> Component.translatable("awakening_system.bounty_target.enderman_hunt", sel.count);
                    case "witch_hunt" -> Component.translatable("awakening_system.bounty_target.witch_hunt", sel.count);
                    case "abyssal_orc" -> Component.translatable("awakening_system.bounty_target.abyssal_orc", sel.count);
                    case "abyssal_necromancer" -> Component.translatable("awakening_system.bounty_target.abyssal_necromancer", sel.count);
                    case "jiangshi_hunt" -> Component.translatable("awakening_system.bounty_target.jiangshi_hunt", sel.count);
                    case "demonic_cultivator" -> Component.translatable("awakening_system.bounty_target.demonic_cultivator", sel.count);
                    case "ant_soldier" -> Component.translatable("awakening_system.bounty_target.ant_soldier", sel.count);
                    case "thunder_tiger" -> Component.translatable("awakening_system.bounty_target.thunder_tiger", sel.count);
                    case "cloud_crane" -> Component.translatable("awakening_system.bounty_target.cloud_crane", sel.count);
                    case "jade_beast" -> Component.translatable("awakening_system.bounty_target.jade_beast", sel.count);
                    case "warden_hunt" -> Component.translatable("awakening_system.bounty_target.warden_hunt", sel.count);
                    case "boss_elemental_demon_king" -> Component.translatable("awakening_system.bounty_target.boss_elemental_demon_king", sel.count);
                    case "boss_blood_patriarch" -> Component.translatable("awakening_system.bounty_target.boss_blood_patriarch", sel.count);
                    case "boss_yggdrasil_treant" -> Component.translatable("awakening_system.bounty_target.boss_yggdrasil_treant", sel.count);
                    case "boss_shadow_monarch" -> Component.translatable("awakening_system.bounty_target.boss_shadow_monarch", sel.count);

                    default -> Component.translatable("awakening_system.bounty_target.default", sel.count);
                };

                List<FormattedCharSequence> targetLines = this.font.split(Component.literal("§f").append(targetDesc), detW - 20);
                int toY = objY + 10;
                for (FormattedCharSequence line : targetLines) {
                    g.drawString(this.font, line, detX + 10, toY, 0xFFFFFFFF, false);
                    toY += 10;
                }

                int rewY = toY + 3;
                int rewH = 32;
                g.fill(detX + 8, rewY, detX + detW - 8, rewY + rewH, 0x600C1F30);
                g.fill(detX + 8, rewY, detX + 11, rewY + rewH, 0xFF00E5FF);
                g.drawString(this.font, Component.translatable("awakening_system.bounty_board.rewards_header"), detX + 14, rewY + 4, 0xFFFFD700, false);
                g.drawString(this.font, Component.literal("§f• §6+" + sel.coins + " M   §a+" + sel.exp + " EXP   §d+" + sel.medals + " 🏅"), detX + 14, rewY + 16, 0xFFFFFFFF, false);

                boolean isActiveContract = (payload.hasActiveBounty() && payload.activeIndex() == selGlobalIdx);
                int progY = rewY + rewH + 4;
                if (isActiveContract) {
                    int k = payload.activeKilled();
                    int n = payload.activeNeeded();
                    Component progText = sel.targetType.startsWith("collect_")
                            ? Component.translatable("awakening_system.bounty_board.delivered", k, n)
                            : Component.translatable("awakening_system.bounty_board.progress", k, n);
                    g.drawString(this.font, Component.literal("§b").append(progText), detX + 12, progY, 0xFF70E8FF, false);
                    g.fill(detX + 12, progY + 9, detX + detW - 12, progY + 14, 0xFF0B141D);
                    int fillW = n > 0 ? Math.min(detW - 24, (detW - 24) * k / n) : 0;
                    g.fill(detX + 12, progY + 9, detX + 12 + fillW, progY + 14, k >= n ? 0xFF50FA7B : 0xFF00E5FF);
                }

                if (sel.targetType.startsWith("collect_")) {
                    int hintY = isActiveContract ? progY + 17 : progY + 2;
                    if (hintY + 8 < detY + detH - 22) {
                        g.drawString(this.font, Component.translatable("awakening_system.bounty_board.delivery_hint"), detX + 10, hintY, 0xFF888888, false);
                    }
                }

                int btnX = detX + 10;
                int btnY = detY + detH - 22;
                int btnW = detW - 20;
                int btnH = 18;

                boolean canClaim = isActiveContract && (payload.activeKilled() >= payload.activeNeeded());
                boolean canAccept = !payload.hasActiveBounty();

                if (canClaim) {
                    boolean hov = mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH;
                    g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hov ? 0xFF50FA7B : 0xFF2ECC71);
                    g.renderOutline(btnX, btnY, btnW, btnH, 0xFFFFFFFF);
                    g.drawCenteredString(this.font, Component.translatable("awakening_system.bounty_board.btn_claim").copy().withStyle(ChatFormatting.BOLD), btnX + btnW / 2, btnY + 5, 0xFF10202D);
                } else if (isActiveContract) {
                    g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x882A3A4D);
                    g.renderOutline(btnX, btnY, btnW, btnH, 0xFFFFD700);
                    g.drawCenteredString(this.font, Component.translatable("awakening_system.bounty_board.btn_in_progress"), btnX + btnW / 2, btnY + 5, 0xFFFFD700);
                } else if (canAccept) {
                    boolean hov = mX >= btnX && mX <= btnX + btnW && mY >= btnY && mY <= btnY + btnH;
                    g.fill(btnX, btnY, btnX + btnW, btnY + btnH, hov ? 0xFF00B4D8 : 0xFF0077B6);
                    g.renderOutline(btnX, btnY, btnW, btnH, hov ? 0xFFFFFFFF : 0xFF00E5FF);
                    g.drawCenteredString(this.font, Component.translatable("awakening_system.bounty_board.btn_accept").copy().withStyle(ChatFormatting.BOLD), btnX + btnW / 2, btnY + 5, 0xFFFFFFFF);
                } else {
                    g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x44102030);
                    g.renderOutline(btnX, btnY, btnW, btnH, 0x442D7691);
                    g.drawCenteredString(this.font, Component.translatable("awakening_system.bounty_board.btn_busy"), btnX + btnW / 2, btnY + 5, 0xFF888888);
                }
            }
        }

        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int boardW = 380;
        int boardH = 236;
        int left = (this.width - boardW) / 2;
        int top = (this.height - boardH) / 2;

        int closeX = left + boardW - 24;
        int closeY = top + 8;
        if (mouseX >= closeX && mouseX < closeX + 16 && mouseY >= closeY && mouseY < closeY + 16) {
            onClose();
            return true;
        }

        int refX = left + boardW - 96;
        int refY = top + 26;
        int refW = 68;
        int refH = 14;
        if (mouseX >= refX && mouseX <= refX + refW && mouseY >= refY && mouseY <= refY + refH) {
            PacketDistributor.sendToServer(new SelectBountyPayload(-1, false, true));
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.2F);
            }
            return true;
        }

        int listX = left + 10;
        int listY = top + 44;
        int listW = 156;
        int listH = 180;

        int[] offers = payload.availableBounties();
        int totalOffers = (offers != null) ? offers.length : 0;
        int maxScroll = Math.max(0, totalOffers - 6);

        if (listScroll > 0 && mouseX >= listX + listW - 16 && mouseX <= listX + listW - 2 && mouseY >= listY + 2 && mouseY <= listY + 12) {
            listScroll = Math.max(0, listScroll - 1);
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
            }
            return true;
        }

        if (listScroll < maxScroll && mouseX >= listX + listW - 16 && mouseX <= listX + listW - 2 && mouseY >= listY + listH - 12 && mouseY <= listY + listH - 2) {
            listScroll = Math.min(maxScroll, listScroll + 1);
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
            }
            return true;
        }

        int visibleCount = Math.min(6, totalOffers);
        for (int i = 0; i < visibleCount; i++) {
            int slotIdx = listScroll + i;
            if (slotIdx >= totalOffers) break;
            int itemY = listY + 5 + i * 28;
            if (mouseX >= listX + 2 && mouseX <= listX + listW - 2 && mouseY >= itemY && mouseY <= itemY + 26) {
                this.selectedIndex = slotIdx;
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.8F, 1.2F);
                }
                return true;
            }
        }

        int detX = left + 174;
        int detY = top + 44;
        int detW = 196;
        int detH = 180;
        int btnX = detX + 10;
        int btnY = detY + detH - 22;
        int btnW = detW - 20;
        int btnH = 18;

        if (offers != null && selectedIndex >= 0 && selectedIndex < offers.length) {
            int selGlobalIdx = offers[selectedIndex];
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                boolean isActiveContract = (payload.hasActiveBounty() && payload.activeIndex() == selGlobalIdx);
                boolean canClaim = isActiveContract && (payload.activeKilled() >= payload.activeNeeded());
                boolean canAccept = !payload.hasActiveBounty();

                if (canClaim) {
                    PacketDistributor.sendToServer(new SelectBountyPayload(selGlobalIdx, true, false));
                    onClose();
                    return true;
                } else if (canAccept) {
                    PacketDistributor.sendToServer(new SelectBountyPayload(selGlobalIdx, false, false));
                    onClose();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
