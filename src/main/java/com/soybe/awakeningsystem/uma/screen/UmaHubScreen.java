package com.soybe.awakeningsystem.uma.screen;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.uma.UmaProfile;
import com.soybe.awakeningsystem.uma.UmaRacerEntity;
import com.soybe.awakeningsystem.uma.network.OpenUmaHubPayload;
import com.soybe.awakeningsystem.uma.network.UmaGachaResultPayload;
import com.soybe.awakeningsystem.uma.network.UmaHubActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Pantalla principal de la Academia Tracen (Uma Musume Pretty Derby Hub).
 * Incluye Establo con exhibición 3D, entrenamiento de stats, ascensión de estrellas (Talent Bloom)
 * mediante Fragmentos y Estatuas de la Diosa (☘), y lobby de Carreras Customs.
 */
public class UmaHubScreen extends Screen {

    private static final int GUI_WIDTH = 540;
    private static final int GUI_HEIGHT = 270;

    private OpenUmaHubPayload data;
    private int activeTab = 0;
    private int selectedUmaIndex = 0;
    private int establoScrollOffset = 0;

    private final Map<UmaProfile, UmaRacerEntity> dummyUmas = new EnumMap<>(UmaProfile.class);

    public UmaHubScreen(OpenUmaHubPayload data) {
        super(Component.translatable("awakening_system.uma.hub.screen_title"));
        this.data = data;
        if (data.trackPos() != null && !data.trackPos().equals(net.minecraft.core.BlockPos.ZERO)) {
            this.activeTab = 1;
        }
    }

    private UmaRacerEntity getOrCreateDummyUma(UmaProfile profile) {
        if (this.minecraft == null || this.minecraft.level == null || profile == null) return null;
        return dummyUmas.computeIfAbsent(profile, p -> {
            UmaRacerEntity racer = new UmaRacerEntity(ModEntities.UMA_RACER.get(), this.minecraft.level);
            racer.setUmaProfile(p);
            racer.setSilent(true);
            racer.setNoAi(true);
            return racer;
        });
    }

    public void updateData(OpenUmaHubPayload newData) {
        this.data = newData;
        if (this.selectedUmaIndex >= this.data.ownedUmas().size()) {
            this.selectedUmaIndex = Math.max(0, this.data.ownedUmas().size() - 1);
        }
        this.rebuildWidgets();
    }

    public void setGachaResults(List<UmaGachaResultPayload.GachaCardEntry> results) {
        this.rebuildWidgets();
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 560.0F;
        float scaleY = (float) this.height / 290.0F;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        if (activeTab == 0 && data != null && data.ownedUmas().size() > 6) {
            int maxOffset = Math.max(0, data.ownedUmas().size() - 6);
            if (scrollY > 0 && establoScrollOffset > 0) {
                establoScrollOffset--;
                this.rebuildWidgets();
                return true;
            } else if (scrollY < 0 && establoScrollOffset < maxOffset) {
                establoScrollOffset++;
                this.rebuildWidgets();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x88000000);
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;

        int tabY = top + 24;
        int tabW = 252;
        this.addRenderableWidget(new UmaTabButton(left + 14, tabY, tabW, 18, Component.translatable("awakening_system.uma.hub.tab.stable"), activeTab == 0, () -> switchTab(0)));
        this.addRenderableWidget(new UmaTabButton(left + 274, tabY, tabW, 18, Component.translatable("awakening_system.uma.hub.tab.race"), activeTab == 1, () -> switchTab(1)));

        this.addRenderableWidget(new UmaMenuButton(
                left + GUI_WIDTH - 24, top + 4, 18, 18,
                "✕", 0xFFE74C3C, 0xAA551111,
                this::onClose
        ));

        if (activeTab == 0) {
            initEstabloTab(left, top);
        } else {
            initCarreraTab(left, top);
        }
    }

    private void switchTab(int tab) {
        this.activeTab = tab;
        this.rebuildWidgets();
    }

    private void initEstabloTab(int left, int top) {
        List<OpenUmaHubPayload.OwnedUmaDto> umas = data.ownedUmas();
        int listX = left + 14;
        int listY = top + 48;
        int maxVisible = 6;
        int itemW = 132;
        int itemH = 28;

        for (int i = 0; i < maxVisible; i++) {
            int actualIndex = establoScrollOffset + i;
            if (actualIndex >= umas.size()) break;
            final int index = actualIndex;
            OpenUmaHubPayload.OwnedUmaDto ou = umas.get(actualIndex);
            boolean isAce = ou.umaId().equalsIgnoreCase(data.activeUmaId());
            this.addRenderableWidget(new UmaSelectorWidget(
                    listX, listY + i * 30, itemW, itemH,
                    ou, isAce, selectedUmaIndex == index,
                    () -> {
                        selectedUmaIndex = index;
                        rebuildWidgets();
                    }
            ));
        }

        if (umas.size() > maxVisible) {
            int scrollBtnY = listY + maxVisible * 30 + 2;
            this.addRenderableWidget(new UmaMenuButton(
                    listX, scrollBtnY, 64, 14, Component.translatable("awakening_system.uma.hub.btn.up"), 0xFFFFFFFF, 0xAA223344,
                    () -> {
                        if (establoScrollOffset > 0) {
                            establoScrollOffset--;
                            rebuildWidgets();
                        }
                    }
            ));
            this.addRenderableWidget(new UmaMenuButton(
                    listX + 68, scrollBtnY, 64, 14, Component.translatable("awakening_system.uma.hub.btn.down"), 0xFFFFFFFF, 0xAA223344,
                    () -> {
                        if (establoScrollOffset < umas.size() - maxVisible) {
                            establoScrollOffset++;
                            rebuildWidgets();
                        }
                    }
            ));
        }

        if (umas.isEmpty()) return;
        OpenUmaHubPayload.OwnedUmaDto sel = umas.get(Math.min(selectedUmaIndex, umas.size() - 1));

        int showcaseX = left + 154;
        int showcaseY = top + 48;
        boolean isAce = sel.umaId().equalsIgnoreCase(data.activeUmaId());
        Component aceLabel = isAce ? Component.translatable("awakening_system.uma.hub.btn.active_racer") : Component.translatable("awakening_system.uma.hub.btn.assign_ace");
        this.addRenderableWidget(new UmaMenuButton(
                showcaseX + 6, showcaseY + 186, 138, 18,
                aceLabel,
                isAce ? 0xFF2ECC71 : 0xFFFFD700, isAce ? 0xAA005522 : 0xAA664400,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("SELECT_ACE", data.trackPos(), sel.umaId(), 0))
        ));

        int rightX = left + 312;
        int rightY = top + 48;
        int trainX = rightX + 154;
        int trainW = 54;
        int trainH = 13;
        int statY = rightY + 18;

        Component trainLabel = Component.translatable("awakening_system.uma.hub.btn.train");
        this.addRenderableWidget(new UmaMenuButton(trainX, statY, trainW, trainH, trainLabel, 0xFFFFFFFF, 0xFFC69214, 0xFFFFD700,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("TRAIN", data.trackPos(), sel.umaId() + ":speed", 0))));
        this.addRenderableWidget(new UmaMenuButton(trainX, statY + 15, trainW, trainH, trainLabel, 0xFFFFFFFF, 0xFF0277BD, 0xFF00E5FF,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("TRAIN", data.trackPos(), sel.umaId() + ":stamina", 0))));
        this.addRenderableWidget(new UmaMenuButton(trainX, statY + 30, trainW, trainH, trainLabel, 0xFFFFFFFF, 0xFFD84315, 0xFFFF7043,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("TRAIN", data.trackPos(), sel.umaId() + ":power", 0))));
        this.addRenderableWidget(new UmaMenuButton(trainX, statY + 45, trainW, trainH, trainLabel, 0xFFFFFFFF, 0xFFAD1457, 0xFFFF4081,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("TRAIN", data.trackPos(), sel.umaId() + ":guts", 0))));
        this.addRenderableWidget(new UmaMenuButton(trainX, statY + 60, trainW, trainH, trainLabel, 0xFFFFFFFF, 0xFF2E7D32, 0xFF00E676,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("TRAIN", data.trackPos(), sel.umaId() + ":wisdom", 0))));

        int playerStatues = getPlayerStatues();
        boolean hasStatues = playerStatues >= 5;
        UmaMenuButton exchangeBtn = new UmaMenuButton(
                rightX + 6, top + 198, 202, 16,
                Component.translatable("awakening_system.uma.hub.btn.exchange_statues"),
                0xFFFFFFFF, hasStatues ? 0xFF27AE60 : 0xFF37474F, hasStatues ? 0xFF2ECC71 : 0xFF546E7A,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("EXCHANGE_STATUES", data.trackPos(), sel.umaId(), 0))
        );
        exchangeBtn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("awakening_system.uma.hub.btn.exchange_statues_desc")));
        this.addRenderableWidget(exchangeBtn);

        boolean canBloom = sel.rarity() < 5 && sel.shards() >= getRequiredShards(sel.rarity());
        int reqShards = getRequiredShards(sel.rarity());
        Component bloomLabel = sel.rarity() >= 5
                ? Component.translatable("awakening_system.uma.hub.bloom.max")
                : Component.translatable("awakening_system.uma.hub.bloom.awaken", (sel.rarity() + 1), sel.shards(), reqShards);
        this.addRenderableWidget(new UmaMenuButton(
                rightX + 6, top + 218, 202, 20,
                bloomLabel,
                0xFFFFFFFF, canBloom ? 0xFFD4AF37 : 0xFF37474F, canBloom ? 0xFFFFD700 : 0xFF546E7A,
                () -> {
                    if (canBloom) {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("BLOOM", data.trackPos(), sel.umaId(), 0));
                    }
                }
        ));
    }

    private int getPlayerStatues() {
        int count = 0;
        if (Minecraft.getInstance().player != null) {
            for (net.minecraft.world.item.ItemStack st : Minecraft.getInstance().player.getInventory().items) {
                if (!st.isEmpty() && st.is(SystemItems.CLOVER_SPARK.get())) {
                    count += st.getCount();
                }
            }
        }
        return count;
    }

    private int getRequiredShards(int rarity) {
        return switch (rarity) {
            case 1 -> 15;
            case 2 -> 30;
            case 3 -> 50;
            case 4 -> 100;
            default -> 0;
        };
    }

    private void initCarreraTab(int left, int top) {
        int btnY = top + GUI_HEIGHT - 32;
        boolean hasTrack = data.trackPos() != null && !data.trackPos().equals(net.minecraft.core.BlockPos.ZERO);

        this.addRenderableWidget(new UmaMenuButton(
                left + 16, btnY, 120, 20,
                Component.translatable("awakening_system.uma.hub.race.btn.join"), 0xFFFFFFFF, 0xFF0277BD, 0xFF00B0FF,
                () -> {
                    if (hasTrack) {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("JOIN_CUSTOM_RACE", data.trackPos(), "", 0));
                    }
                }
        ));

        this.addRenderableWidget(new UmaMenuButton(
                left + 144, btnY, 120, 20,
                Component.translatable("awakening_system.uma.hub.race.btn.fill_npcs"), 0xFFFFFFFF, 0xFFE65100, 0xFFFF9800,
                () -> {
                    if (hasTrack) {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("FILL_NPCS", data.trackPos(), "", 0));
                    }
                }
        ));

        this.addRenderableWidget(new UmaMenuButton(
                left + 272, btnY, 120, 20,
                Component.translatable("awakening_system.uma.hub.race.btn.clear_lobby"), 0xFFFFFFFF, 0xFFC62828, 0xFFFF5252,
                () -> {
                    if (hasTrack) {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("CLEAR_LOBBY", data.trackPos(), "", 0));
                    }
                }
        ));

        boolean canStart = hasTrack && data.waypointCount() >= 2 && !data.participants().isEmpty();
        this.addRenderableWidget(new UmaMenuButton(
                left + 400, btnY, 124, 20,
                Component.translatable("awakening_system.uma.hub.race.btn.start"), 0xFFFFFFFF, canStart ? 0xFF2E7D32 : 0xFF424242, canStart ? 0xFF00E676 : 0xFF616161,
                () -> {
                    if (canStart) {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("START_CUSTOM_RACE", data.trackPos(), "", 0));
                        this.onClose();
                    } else if (!hasTrack && Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(Component.translatable("awakening_system.uma.hub.race.msg.no_track"), true);
                    } else if (data.waypointCount() < 2 && Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(Component.translatable("awakening_system.uma.hub.race.msg.need_waypoints"), true);
                    }
                }
        ));

        if (hasTrack) {
            int panelX = left + 16;
            int panelY = top + 50;
            this.addRenderableWidget(new UmaMenuButton(
                    panelX + 372, panelY + 12, 60, 20,
                    Component.translatable("awakening_system.uma.hub.race.btn.export"), 0xFF9B59B6, 0xAA4A235A,
                    () -> {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("EXPORT_TRACK_JSON", data.trackPos(), "", 0));
                    }
            ));

            this.addRenderableWidget(new UmaMenuButton(
                    panelX + 438, panelY + 12, 60, 20,
                    Component.translatable("awakening_system.uma.hub.race.btn.load"), 0xFF1ABC9C, 0xAA0E6655,
                    () -> {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("RELOAD_TRACK_JSON", data.trackPos(), "", 0));
                    }
            ));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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



        int statues = getPlayerStatues();
        String coinsText = "§e§l🥕 " + String.format("%,d", data.carats()) + " §7| §6§l🪙 " + String.format("%,d", data.playerCoins()) + " §7| §a§l☘ " + statues;
        int coinsW = this.font.width(coinsText);
        int capsuleW = coinsW + 12;
        int capsuleX = (left + GUI_WIDTH - 28) - capsuleW;
        graphics.fillGradient(capsuleX, top + 4, capsuleX + capsuleW, top + 20, 0xEE161D2B, 0xEE0E131C);
        graphics.renderOutline(capsuleX, top + 4, capsuleW, 16, 0x884FC3F7);


        graphics.drawString(this.font, coinsText, capsuleX + 6, top + 8, 0xFFFFFF, true);

        int maxHeaderW = Math.max(60, (capsuleX - 8) - (left + 10));
        graphics.fillGradient(left + 10, top + 4, left + 10 + maxHeaderW, top + 20, 0xEE1E170A, 0x882B200D);
        graphics.renderOutline(left + 10, top + 4, maxHeaderW, 16, 0x88FFD700);

        String fullHeader = Component.translatable("awakening_system.uma.hub.header").getString();
        if (this.font.width(fullHeader) > maxHeaderW - 8) {
            fullHeader = this.font.plainSubstrByWidth(fullHeader, maxHeaderW - 14) + "…";
        }
        graphics.drawString(this.font, fullHeader, left + 14, top + 8, 0xFFFFE066, true);



        if (activeTab == 0) {
            renderEstabloTab(graphics, left, top, mX, mY);
        } else {
            renderCarreraTab(graphics, left, top);
        }

        super.render(graphics, mX, mY, partialTick);
        graphics.pose().popPose();
    }

    private void renderEstabloTab(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        List<OpenUmaHubPayload.OwnedUmaDto> umas = data.ownedUmas();
        if (umas.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.hub.empty.title"), left + GUI_WIDTH / 2, top + 100, 0xFF8888);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.hub.empty.desc"), left + GUI_WIDTH / 2, top + 115, 0xAAAAAA);
            return;
        }

        int listX = left + 14;
        int listY = top + 48;
        int showcaseX = left + 154;
        int showcaseW = 150;
        int showcaseY = top + 48;
        int showcaseH = 210;
        int rightX = left + 312;
        int rightW = 214;
        int rightY = top + 48;
        int rightH = 210;

        graphics.fillGradient(listX - 2, listY - 2, listX + 134, listY + showcaseH, 0xEE121722, 0xEE0C0F17);
        graphics.renderOutline(listX - 2, listY - 2, 136, showcaseH, 0x55D4AF37);

        graphics.fillGradient(showcaseX, showcaseY, showcaseX + showcaseW, showcaseY + showcaseH, 0xEE161D2B, 0xEE0D111A);
        graphics.renderOutline(showcaseX, showcaseY, showcaseW, showcaseH, 0xFFD4AF37);

        graphics.fillGradient(rightX, rightY, rightX + rightW, rightY + rightH, 0xEE121722, 0xEE0C0F17);
        graphics.renderOutline(rightX, rightY, rightW, rightH, 0x55D4AF37);

        OpenUmaHubPayload.OwnedUmaDto sel = umas.get(Math.min(selectedUmaIndex, umas.size() - 1));
        UmaProfile profile = UmaProfile.byId(sel.umaId());

        String rankLetter = calculateRankLetter(sel);
        int rankColor = getRankColor(rankLetter);

        graphics.drawCenteredString(this.font, "§6§l" + profile.getDisplayName(), showcaseX + showcaseW / 2, showcaseY + 6, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.hub.rank", rankLetter), showcaseX + showcaseW / 2, showcaseY + 18, rankColor);

        UmaRacerEntity dummy = getOrCreateDummyUma(profile);
        if (dummy != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    showcaseX + 6, showcaseY + 28,
                    showcaseX + showcaseW - 6, showcaseY + 154,
                    38, 0.0625F,
                    (float) mouseX, (float) mouseY,
                    dummy
            );
            graphics.flush();
            com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.hub.tactic", profile.getTactique()), showcaseX + showcaseW / 2, showcaseY + 158, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.hub.stats.races", sel.totalRaces(), sel.totalWins()), showcaseX + showcaseW / 2, showcaseY + 170, 0xFFFFFF);

        graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.attributes.title"), rightX + 6, rightY + 6, 0xFFFFD700, true);

        int statY = rightY + 18;
        renderStatBarCompact(graphics, rightX + 6, statY, Component.translatable("awakening_system.uma.hub.stat.speed").getString(), sel.speed(), 0xFFFFD700);
        renderStatBarCompact(graphics, rightX + 6, statY + 15, Component.translatable("awakening_system.uma.hub.stat.stamina").getString(), sel.stamina(), 0xFF00E5FF);
        renderStatBarCompact(graphics, rightX + 6, statY + 30, Component.translatable("awakening_system.uma.hub.stat.power").getString(), sel.power(), 0xFFFF9100);
        renderStatBarCompact(graphics, rightX + 6, statY + 45, Component.translatable("awakening_system.uma.hub.stat.guts").getString(), sel.guts(), 0xFFFF1744);
        renderStatBarCompact(graphics, rightX + 6, statY + 60, Component.translatable("awakening_system.uma.hub.stat.wisdom").getString(), sel.wisdom(), 0xFF00E676);

        graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.stat.cost"), rightX + 6, statY + 76, 0xFFFFFF, true);

        graphics.fill(rightX + 6, statY + 88, rightX + rightW - 6, statY + 89, 0x88FFD700);
        graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.bloom.title"), rightX + 6, statY + 93, 0xFFFFD700, true);

        String stars = "★".repeat(sel.rarity()) + "☆".repeat(Math.max(0, 5 - sel.rarity()));
        Component lvlStr = Component.literal("§e§l" + stars + " ").append(Component.translatable("awakening_system.uma.hub.bloom.level", sel.rarity(), 5));
        graphics.drawString(this.font, lvlStr, rightX + 6, statY + 105, 0xFFFFFF, true);

        int reqShards = getRequiredShards(sel.rarity());
        int shardBarX = rightX + 6;
        int shardBarY = statY + 117;
        int shardBarW = rightW - 12;
        graphics.fill(shardBarX, shardBarY, shardBarX + shardBarW, shardBarY + 9, 0xFF0E131C);
        int filledShards = reqShards > 0 ? Math.min(shardBarW, (sel.shards() * shardBarW) / reqShards) : shardBarW;
        if (filledShards > 0) {
            graphics.fill(shardBarX, shardBarY, shardBarX + filledShards, shardBarY + 9, 0xFFFFD700);
            graphics.fill(shardBarX, shardBarY, shardBarX + filledShards, shardBarY + 2, 0x88FFFFFF);
        }
        graphics.renderOutline(shardBarX, shardBarY, shardBarW, 9, 0xFFFFD700);
        Component shardLabel = reqShards > 0 ? Component.translatable("awakening_system.uma.hub.bloom.shards", sel.shards(), reqShards) : Component.translatable("awakening_system.uma.hub.bloom.max_shards");
        int sLabelW = this.font.width(shardLabel);
        graphics.drawString(this.font, shardLabel, shardBarX + (shardBarW - sLabelW) / 2, shardBarY + 1, 0xFFFFFF, true);
    }

    private void renderStatBarCompact(GuiGraphics graphics, int x, int y, String label, int val, int color) {
        int neonColor = color | 0xFF000000;
        graphics.drawString(this.font, label, x, y + 2, neonColor, true);
        int barX = x + 66;
        int barW = 46;
        int barH = 10;
        graphics.fill(barX, y + 2, barX + barW, y + 2 + barH, 0xFF101624);
        graphics.fill(barX + 1, y + 3, barX + barW - 1, y + 1 + barH, (color & 0x00FFFFFF) | 0x2B000000);
        graphics.renderOutline(barX, y + 2, barW, barH, neonColor);

        int filled = val > 0 ? Math.max(3, Math.min(barW - 2, (val * (barW - 2)) / 1200)) : 0;
        if (filled > 0) {
            graphics.fillGradient(barX + 1, y + 3, barX + 1 + filled, y + 1 + barH, neonColor, (color & 0x00FFFFFF) | 0xFF000000);
            graphics.fill(barX + 1, y + 3, barX + 1 + filled, y + 5, 0xDDFFFFFF);
        }

        int pillX = barX + barW + 4;
        int pillW = 28;
        graphics.fillGradient(pillX, y + 2, pillX + pillW, y + 2 + barH, 0xFF1E2D44, 0xFF141D2D);
        graphics.renderOutline(pillX, y + 2, pillW, barH, neonColor);
        String valStr = String.valueOf(val);
        int vW = this.font.width(valStr);
        graphics.drawString(this.font, "§f§l" + valStr, pillX + (pillW - vW) / 2, y + 2, 0xFFFFFF, true);
    }

    private int getRankColor(String rank) {
        return switch (rank) {
            case "SS" -> 0xFFFF4444;
            case "S" -> 0xFFFFD700;
            case "A" -> 0xFFE67E22;
            case "B" -> 0xFF2ECC71;
            case "C" -> 0xFF3498DB;
            case "D" -> 0xFF9B59B6;
            case "E" -> 0xFF95A5A6;
            default -> 0xFF7F8C8D;
        };
    }

    private String calculateRankLetter(OpenUmaHubPayload.OwnedUmaDto ou) {
        int avg = (ou.speed() + ou.stamina() + ou.power() + ou.guts() + ou.wisdom()) / 5;
        if (avg >= 1150) return "SS";
        if (avg >= 1000) return "S";
        if (avg >= 850) return "A";
        if (avg >= 700) return "B";
        if (avg >= 550) return "C";
        if (avg >= 400) return "D";
        if (avg >= 250) return "E";
        return "F";
    }

    private void renderCarreraTab(GuiGraphics graphics, int left, int top) {
        com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        boolean hasTrack = data.trackPos() != null && !data.trackPos().equals(net.minecraft.core.BlockPos.ZERO);
        int panelX = left + 16;
        int panelY = top + 50;

        graphics.fillGradient(panelX, panelY, left + GUI_WIDTH - 16, panelY + 44, 0xF0182438, 0xF0101826);
        graphics.renderOutline(panelX, panelY, GUI_WIDTH - 32, 44, 0xFFFFD700);

        if (hasTrack) {
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.linked", data.trackPos().getX(), data.trackPos().getY(), data.trackPos().getZ()), panelX + 10, panelY + 6, 0xFFFFFF, true);
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.waypoints", data.waypointCount()), panelX + 10, panelY + 18, 0xFFFFFF, true);
            Component stStr = switch (data.raceState()) {
                case 0 -> Component.translatable("awakening_system.uma.hub.race.state.idle");
                case 1 -> Component.translatable("awakening_system.uma.hub.race.state.starting");
                case 2 -> Component.translatable("awakening_system.uma.hub.race.state.running");
                default -> Component.translatable("awakening_system.uma.hub.race.state.finished");
            };
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.track_status").append(stStr), panelX + 10, panelY + 30, 0xFFFFFF, true);
        } else {
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.no_track_title"), panelX + 10, panelY + 8, 0xFFFF6666, true);
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.no_track_desc"), panelX + 10, panelY + 22, 0xFFFFEE, true);
        }

        int gridY = panelY + 52;
        int cardW = 248;
        int cardH = 26;
        for (int lane = 0; lane < 8; lane++) {
            int col = lane % 2;
            int row = lane / 2;
            int cx = panelX + col * (cardW + 12);
            int cy = gridY + row * (cardH + 6);

            OpenUmaHubPayload.ParticipantDto part = findParticipant(lane);
            if (part != null) {
                graphics.fillGradient(cx, cy, cx + cardW, cy + cardH, 0xF0243654, 0xF0162236);
                graphics.renderOutline(cx, cy, cardW, cardH, 0xFFFFD700);
                String tag = part.isNpc() ? "§e[NPC] " : "§6★ ";
                graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.lane", lane + 1, tag + part.displayName()), cx + 8, cy + 4, 0xFFFFFF, true);
                graphics.drawString(this.font, "§e" + UmaProfile.byId(part.umaId()).getDisplayName() + " §6(★" + part.rarity() + ") §7• Spd: §a§l" + part.speed(), cx + 8, cy + 14, 0xFFFFFF, true);
            } else {
                graphics.fillGradient(cx, cy, cx + cardW, cy + cardH, 0xF018253D, 0xF0101828);
                graphics.renderOutline(cx, cy, cardW, cardH, 0xFF00E5FF);
                graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.lane_ready", lane + 1), cx + 8, cy + 5, 0xFFFFFF, true);
                graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.lane_hint"), cx + 8, cy + 15, 0xFFFFFF, true);
            }
        }
    }

    private OpenUmaHubPayload.ParticipantDto findParticipant(int lane) {
        for (OpenUmaHubPayload.ParticipantDto p : data.participants()) {
            if (p.laneIndex() == lane) return p;
        }
        return null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class UmaTabButton extends AbstractWidget {
        private final Component text;
        private final boolean activeTab;
        private final Runnable action;

        public UmaTabButton(int x, int y, int width, int height, Component text, boolean activeTab, Runnable action) {
            super(x, y, width, height, text);
            this.text = text;
            this.activeTab = activeTab;
            this.action = action;
        }

        public UmaTabButton(int x, int y, int width, int height, String text, boolean activeTab, Runnable action) {
            this(x, y, width, height, Component.literal(text), activeTab, action);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            this.action.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int bg = activeTab ? 0xEE2A3550 : 0xAA121622;
            int border = activeTab ? 0xFFFFD700 : 0x55888888;
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, border);
            int textX = this.getX() + (this.width - font.width(this.text)) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            graphics.drawString(font, this.text, textX, textY, activeTab ? 0xFFFFD700 : 0xCCCCCC, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }

    private static int darkenColor(int argb, float factor) {
        int a = (argb >> 24) & 0xFF;
        int r = Math.max(0, Math.min(255, (int) (((argb >> 16) & 0xFF) * factor)));
        int g = Math.max(0, Math.min(255, (int) (((argb >> 8) & 0xFF) * factor)));
        int b = Math.max(0, Math.min(255, (int) ((argb & 0xFF) * factor)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private class UmaMenuButton extends AbstractWidget {
        private final Component text;
        private final int textColor;
        private final int normalBg;
        private final int hoverBg;
        private final Runnable action;

        public UmaMenuButton(int x, int y, int width, int height, Component text, int textColor, int normalBg, int hoverBg, Runnable action) {
            super(x, y, width, height, text);
            this.text = text;
            this.textColor = textColor;
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.action = action;
        }

        public UmaMenuButton(int x, int y, int width, int height, Component text, int textColor, int hoverBg, Runnable action) {
            this(x, y, width, height, text, textColor, 0xEE1E283D, hoverBg, action);
        }

        public UmaMenuButton(int x, int y, int width, int height, String text, int textColor, int normalBg, int hoverBg, Runnable action) {
            this(x, y, width, height, Component.literal(text), textColor, normalBg, hoverBg, action);
        }

        public UmaMenuButton(int x, int y, int width, int height, String text, int textColor, int hoverBg, Runnable action) {
            this(x, y, width, height, Component.literal(text), textColor, 0xEE1E283D, hoverBg, action);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            this.action.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            boolean hovered = this.isHoveredOrFocused();
            int topCol = hovered ? this.hoverBg : this.normalBg;
            int botCol = hovered ? this.hoverBg : darkenColor(this.normalBg, 0.72F);

            graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, topCol, botCol);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, hovered ? 0xFFFFFFFF : this.textColor);

            if (this.height >= 12) {
                graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + 2, 0x55FFFFFF);
            }

            int textW = font.width(this.text);
            int textY = this.getY() + (this.height - 8) / 2;
            if (textW > this.width - 6) {
                String trimmed = font.plainSubstrByWidth(this.text.getString(), this.width - 12) + "..";
                graphics.drawString(font, trimmed, this.getX() + 4, textY, hovered ? 0xFFFFFFFF : this.textColor, true);
            } else {
                int textX = this.getX() + (this.width - textW) / 2;
                graphics.drawString(font, this.text, textX, textY, hovered ? 0xFFFFFFFF : this.textColor, true);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }

    private class UmaSelectorWidget extends AbstractWidget {
        private final OpenUmaHubPayload.OwnedUmaDto dto;
        private final boolean isAce;
        private final boolean isSelected;
        private final Runnable action;

        public UmaSelectorWidget(int x, int y, int width, int height, OpenUmaHubPayload.OwnedUmaDto dto, boolean isAce, boolean isSelected, Runnable action) {
            super(x, y, width, height, Component.literal(dto.umaId()));
            this.dto = dto;
            this.isAce = isAce;
            this.isSelected = isSelected;
            this.action = action;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            this.action.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int bg = isSelected ? 0xEE2A3550 : 0xAA121622;
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, isSelected ? 0xFFFFD700 : (isAce ? 0xFF2ECC71 : 0x44888888));

            UmaProfile p = UmaProfile.byId(dto.umaId());
            String aceBadge = isAce ? " §a[👑Ace]" : "";
            graphics.drawString(font, p.getDisplayName() + aceBadge, this.getX() + 5, this.getY() + 4, 0xFFFFFF, false);
            graphics.drawString(font, "★".repeat(dto.rarity()) + " §7• " + calculateRankLetter(dto), this.getX() + 5, this.getY() + 15, 0xFFFFD700, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }
}
