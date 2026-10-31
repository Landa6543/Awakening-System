package com.soybe.awakeningsystem.uma.screen;

import com.soybe.awakeningsystem.client.gui.ModernGuiHelper;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.SystemScreen;
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
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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

    private static final int GUI_WIDTH = 520;
    private static final int GUI_HEIGHT = 295;

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
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new SystemScreen());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double tX = transformMouseX(mouseX);
        double tY = transformMouseY(mouseY);
        int left = (this.width - GUI_WIDTH) / 2;
        int top = (this.height - GUI_HEIGHT) / 2;
        int btnCloseX = left + GUI_WIDTH - 24;
        int btnCloseY = top + 7;
        if (button == 0 && tX >= btnCloseX && tX <= btnCloseX + 16 && tY >= btnCloseY && tY <= btnCloseY + 16) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onClose();
            return true;
        }
        return super.mouseClicked(tX, tY, button);
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

        int tabY = top + 26;
        int tabW = (GUI_WIDTH - 28 - 8) / 2; // 242px
        this.addRenderableWidget(new UmaTabButton(left + 14, tabY, tabW, 19, Component.translatable("awakening_system.uma.hub.tab.stable"), activeTab == 0, () -> switchTab(0)));
        this.addRenderableWidget(new UmaTabButton(left + 14 + tabW + 8, tabY, tabW, 19, Component.translatable("awakening_system.uma.hub.tab.race"), activeTab == 1, () -> switchTab(1)));

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
        int colLeftX = left + 14;
        int colLeftY = top + 50;
        int maxVisible = 6;
        int itemW = 130;
        int itemH = 32;

        for (int i = 0; i < maxVisible; i++) {
            int actualIndex = establoScrollOffset + i;
            if (actualIndex >= umas.size()) break;
            final int index = actualIndex;
            OpenUmaHubPayload.OwnedUmaDto ou = umas.get(actualIndex);
            boolean isAce = ou.umaId().equalsIgnoreCase(data.activeUmaId());
            this.addRenderableWidget(new UmaSelectorWidget(
                    colLeftX + 4, colLeftY + 4 + i * 35, itemW, itemH,
                    ou, isAce, selectedUmaIndex == index,
                    () -> {
                        selectedUmaIndex = index;
                        rebuildWidgets();
                    }
            ));
        }

        if (umas.size() > maxVisible) {
            int scrollBtnY = colLeftY + 214;
            this.addRenderableWidget(new UmaMenuButton(
                    colLeftX + 4, scrollBtnY, 63, 16, Component.translatable("awakening_system.uma.hub.btn.up"), 0xFFCCE3EC, 0x40142436, 0x70203854,
                    () -> {
                        if (establoScrollOffset > 0) {
                            establoScrollOffset--;
                            rebuildWidgets();
                        }
                    }
            ));
            this.addRenderableWidget(new UmaMenuButton(
                    colLeftX + 71, scrollBtnY, 63, 16, Component.translatable("awakening_system.uma.hub.btn.down"), 0xFFCCE3EC, 0x40142436, 0x70203854,
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

        int colMidX = left + 158;
        int colMidY = top + 50;
        boolean isAce = sel.umaId().equalsIgnoreCase(data.activeUmaId());
        Component aceLabel = isAce ? Component.translatable("awakening_system.uma.hub.btn.active_racer") : Component.translatable("awakening_system.uma.hub.btn.assign_ace");
        this.addRenderableWidget(new UmaMenuButton(
                colMidX + 8, colMidY + 204, 136, 22,
                aceLabel,
                isAce ? 0xFF50FA7B : 0xFF00E5FF, isAce ? 0x600F3520 : 0x60142A3E, isAce ? 0x90185032 : 0x90204868,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("SELECT_ACE", data.trackPos(), sel.umaId(), 0))
        ));

        int colRightX = left + 316;
        int colRightY = top + 50;
        int colRightW = 190;
        int trainW = 38;
        int trainH = 12;
        int trainX = colRightX + colRightW - 46;

        Component trainLabel = Component.literal("+5");
        this.addRenderableWidget(new UmaMenuButton(trainX, colRightY + 18, trainW, trainH, trainLabel, 0xFFFFD866, 0x602A2412, 0x90453A1B,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("TRAIN", data.trackPos(), sel.umaId() + ":speed", 0))));
        this.addRenderableWidget(new UmaMenuButton(trainX, colRightY + 34, trainW, trainH, trainLabel, 0xFF00E5FF, 0x600C2538, 0x90143C59,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("TRAIN", data.trackPos(), sel.umaId() + ":stamina", 0))));
        this.addRenderableWidget(new UmaMenuButton(trainX, colRightY + 50, trainW, trainH, trainLabel, 0xFFFF9100, 0x60321B0F, 0x904F2B18,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("TRAIN", data.trackPos(), sel.umaId() + ":power", 0))));
        this.addRenderableWidget(new UmaMenuButton(trainX, colRightY + 66, trainW, trainH, trainLabel, 0xFFFF4081, 0x602D0E1C, 0x904E1730,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("TRAIN", data.trackPos(), sel.umaId() + ":guts", 0))));
        this.addRenderableWidget(new UmaMenuButton(trainX, colRightY + 82, trainW, trainH, trainLabel, 0xFF00E676, 0x600F291B, 0x9019442C,
                () -> PacketDistributor.sendToServer(new UmaHubActionPayload("TRAIN", data.trackPos(), sel.umaId() + ":wisdom", 0))));

        int playerStatues = getPlayerStatues();
        boolean hasStatues = playerStatues >= 5;
        UmaMenuButton exchangeBtn = new UmaMenuButton(
                colRightX + 8, colRightY + 158, colRightW - 16, 18,
                Component.translatable("awakening_system.uma.hub.btn.exchange_statues"),
                0xFFFFFFFF, hasStatues ? 0x60123522 : 0x401A242E, hasStatues ? 0x901F5234 : 0x602A3848,
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
                colRightX + 8, colRightY + 182, colRightW - 16, 22,
                bloomLabel,
                canBloom ? 0xFFFFD866 : 0xFF8FA7B8, canBloom ? 0x60382B14 : 0x401A242E, canBloom ? 0x90584420 : 0x602A3848,
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
        int btnY = top + GUI_HEIGHT - 30;
        int panelX = left + 14;
        int panelW = GUI_WIDTH - 28; // 492px
        int btnW = (panelW - 18) / 4; // 118px
        boolean hasTrack = data.trackPos() != null && !data.trackPos().equals(net.minecraft.core.BlockPos.ZERO);

        this.addRenderableWidget(new UmaMenuButton(
                panelX, btnY, btnW, 20,
                Component.translatable("awakening_system.uma.hub.race.btn.join"), 0xFFFFFFFF, 0x600D2D42, 0x9016486B,
                () -> {
                    if (hasTrack) {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("JOIN_CUSTOM_RACE", data.trackPos(), "", 0));
                    }
                }
        ));

        this.addRenderableWidget(new UmaMenuButton(
                panelX + (btnW + 6), btnY, btnW, 20,
                Component.translatable("awakening_system.uma.hub.race.btn.fill_npcs"), 0xFFFFFFFF, 0x603D2612, 0x905E3C1B,
                () -> {
                    if (hasTrack) {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("FILL_NPCS", data.trackPos(), "", 0));
                    }
                }
        ));

        this.addRenderableWidget(new UmaMenuButton(
                panelX + 2 * (btnW + 6), btnY, btnW, 20,
                Component.translatable("awakening_system.uma.hub.race.btn.clear_lobby"), 0xFFFFFFFF, 0x603A1418, 0x905A2026,
                () -> {
                    if (hasTrack) {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("CLEAR_LOBBY", data.trackPos(), "", 0));
                    }
                }
        ));

        boolean canStart = hasTrack && data.waypointCount() >= 2 && !data.participants().isEmpty();
        int lastBtnW = panelW - 3 * (btnW + 6);
        this.addRenderableWidget(new UmaMenuButton(
                panelX + 3 * (btnW + 6), btnY, lastBtnW, 20,
                Component.translatable("awakening_system.uma.hub.race.btn.start"), 0xFFFFFFFF, canStart ? 0x60143822 : 0x401A242E, canStart ? 0x90225634 : 0x602A3848,
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
            int panelY = top + 50;
            this.addRenderableWidget(new UmaMenuButton(
                    panelX + panelW - 134, panelY + 12, 62, 18,
                    Component.translatable("awakening_system.uma.hub.race.btn.export"), 0xFFCCE3EC, 0x6026193A, 0x903D285E,
                    () -> {
                        PacketDistributor.sendToServer(new UmaHubActionPayload("EXPORT_TRACK_JSON", data.trackPos(), "", 0));
                    }
            ));

            this.addRenderableWidget(new UmaMenuButton(
                    panelX + panelW - 68, panelY + 12, 62, 18,
                    Component.translatable("awakening_system.uma.hub.race.btn.load"), 0xFF00E5FF, 0x600C2A2D, 0x90144449,
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

        ModernGuiHelper.drawModernWindow(graphics, left, top, GUI_WIDTH, GUI_HEIGHT, ModernGuiHelper.GuiTheme.SOLO_LEVELING);

        // Sleek Manhwa close button [✕] matching SystemScreen & GuildScreen
        int btnCloseX = left + GUI_WIDTH - 24;
        int btnCloseY = top + 7;
        boolean closeHov = mX >= btnCloseX && mX < btnCloseX + 16 && mY >= btnCloseY && mY < btnCloseY + 16;
        graphics.fill(btnCloseX, btnCloseY, btnCloseX + 16, btnCloseY + 16, closeHov ? 0xDD7A1515 : 0x55122030);
        graphics.renderOutline(btnCloseX, btnCloseY, 16, 16, closeHov ? 0xFFFF5555 : 0x7700E5FF);
        graphics.drawCenteredString(this.font, "✕", btnCloseX + 8, btnCloseY + 4, closeHov ? 0xFFFFFFFF : 0xFF70E8FF);

        int statues = getPlayerStatues();
        String coinsText = "§e§l🥕 " + String.format("%,d", data.carats()) + " §7| §6§l🪙 " + String.format("%,d", data.playerCoins()) + " §7| §a§l☘ " + statues;
        int coinsW = this.font.width(coinsText);
        int coinsX = left + GUI_WIDTH - 32 - coinsW;
        graphics.drawString(this.font, coinsText, coinsX, top + 10, 0xFFFFFF, true);

        int maxHeaderW = Math.max(60, (coinsX - 8) - (left + 16));
        String fullHeader = Component.translatable("awakening_system.uma.hub.header").getString();
        if (this.font.width(fullHeader) > maxHeaderW) {
            fullHeader = this.font.plainSubstrByWidth(fullHeader, maxHeaderW - 10) + "…";
        }
        graphics.drawString(this.font, fullHeader, left + 16, top + 10, 0xFF00E5FF, true);

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
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.hub.empty.title"), left + GUI_WIDTH / 2, top + 110, 0xFFFF6666);
            graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.hub.empty.desc"), left + GUI_WIDTH / 2, top + 125, 0xFFAAAAAA);
            return;
        }

        int colLeftX = left + 14;
        int colLeftW = 138;
        int colLeftY = top + 50;
        int colMidX = left + 158;
        int colMidW = 152;
        int colMidY = top + 50;
        int colRightX = left + 316;
        int colRightW = 190;
        int colRightY = top + 50;
        int cardH = 234;

        // Card 1: Left List Card
        graphics.fill(colLeftX, colLeftY, colLeftX + colLeftW, colLeftY + cardH, 0x400C1B2B);
        graphics.renderOutline(colLeftX, colLeftY, colLeftW, cardH, 0x5500E5FF);
        graphics.fill(colLeftX + 1, colLeftY + 1, colLeftX + colLeftW - 1, colLeftY + 2, 0x8800E5FF);

        // Card 2: Center Showcase Card
        graphics.fill(colMidX, colMidY, colMidX + colMidW, colMidY + cardH, 0x400E1F30);
        graphics.renderOutline(colMidX, colMidY, colMidW, cardH, 0x6600E5FF);
        graphics.fill(colMidX + 1, colMidY + 1, colMidX + colMidW - 1, colMidY + 2, 0x8800E5FF);

        // Card 3: Right Attributes & Talent Bloom Card
        graphics.fill(colRightX, colRightY, colRightX + colRightW, colRightY + cardH, 0x400C1B2B);
        graphics.renderOutline(colRightX, colRightY, colRightW, cardH, 0x5500E5FF);
        graphics.fill(colRightX + 1, colRightY + 1, colRightX + colRightW - 1, colRightY + 2, 0x8800E5FF);

        OpenUmaHubPayload.OwnedUmaDto sel = umas.get(Math.min(selectedUmaIndex, umas.size() - 1));
        UmaProfile profile = UmaProfile.byId(sel.umaId());

        String rankLetter = calculateRankLetter(sel);
        int rankColor = getRankColor(rankLetter);

        // Center card text & 3D model
        graphics.drawCenteredString(this.font, "§6§l" + profile.getDisplayName(), colMidX + colMidW / 2, colMidY + 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.hub.rank", rankLetter), colMidX + colMidW / 2, colMidY + 20, rankColor);

        UmaRacerEntity dummy = getOrCreateDummyUma(profile);
        if (dummy != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    colMidX + 8, colMidY + 32,
                    colMidX + colMidW - 8, colMidY + 168,
                    44, 0.0625F,
                    (float) mouseX, (float) mouseY,
                    dummy
            );
            graphics.flush();
            com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.hub.tactic", profile.getTactique()), colMidX + colMidW / 2, colMidY + 172, 0xFFCCE3EC);
        graphics.drawCenteredString(this.font, Component.translatable("awakening_system.uma.hub.stats.races", sel.totalRaces(), sel.totalWins()), colMidX + colMidW / 2, colMidY + 184, 0xFF8FA7B8);

        // Right card text & stats
        graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.attributes.title"), colRightX + 8, colRightY + 6, 0xFFFFD866, true);

        renderStatBarCompact(graphics, colRightX + 8, colRightY + 18, Component.translatable("awakening_system.uma.hub.stat.speed").getString(), sel.speed(), 0xFFFFD866);
        renderStatBarCompact(graphics, colRightX + 8, colRightY + 34, Component.translatable("awakening_system.uma.hub.stat.stamina").getString(), sel.stamina(), 0xFF00E5FF);
        renderStatBarCompact(graphics, colRightX + 8, colRightY + 50, Component.translatable("awakening_system.uma.hub.stat.power").getString(), sel.power(), 0xFFFF9100);
        renderStatBarCompact(graphics, colRightX + 8, colRightY + 66, Component.translatable("awakening_system.uma.hub.stat.guts").getString(), sel.guts(), 0xFFFF4081);
        renderStatBarCompact(graphics, colRightX + 8, colRightY + 82, Component.translatable("awakening_system.uma.hub.stat.wisdom").getString(), sel.wisdom(), 0xFF00E676);

        graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.stat.cost"), colRightX + 8, colRightY + 98, 0xFF8FA7B8, true);

        graphics.fill(colRightX + 8, colRightY + 110, colRightX + colRightW - 8, colRightY + 111, 0x4000E5FF);
        graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.bloom.title"), colRightX + 8, colRightY + 116, 0xFFFFD866, true);

        String stars = "★".repeat(sel.rarity()) + "☆".repeat(Math.max(0, 5 - sel.rarity()));
        Component lvlStr = Component.literal("§e§l" + stars + " ").append(Component.translatable("awakening_system.uma.hub.bloom.level", sel.rarity(), 5));
        graphics.drawString(this.font, lvlStr, colRightX + 8, colRightY + 128, 0xFFFFFF, true);

        int reqShards = getRequiredShards(sel.rarity());
        int shardBarX = colRightX + 8;
        int shardBarY = colRightY + 140;
        int shardBarW = colRightW - 16;
        graphics.fill(shardBarX, shardBarY, shardBarX + shardBarW, shardBarY + 10, 0x60091320);
        int filledShards = reqShards > 0 ? Math.min(shardBarW, (sel.shards() * shardBarW) / reqShards) : shardBarW;
        if (filledShards > 0) {
            graphics.fillGradient(shardBarX, shardBarY, shardBarX + filledShards, shardBarY + 10, 0xFFFFD866, 0xFFE6A817);
            graphics.fill(shardBarX, shardBarY, shardBarX + filledShards, shardBarY + 2, 0x88FFFFFF);
        }
        graphics.renderOutline(shardBarX, shardBarY, shardBarW, 10, 0x7700E5FF);
        Component shardLabel = reqShards > 0 ? Component.translatable("awakening_system.uma.hub.bloom.shards", sel.shards(), reqShards) : Component.translatable("awakening_system.uma.hub.bloom.max_shards");
        int sLabelW = this.font.width(shardLabel);
        graphics.drawString(this.font, shardLabel, shardBarX + (shardBarW - sLabelW) / 2, shardBarY + 1, 0xFFFFFF, true);
    }

    private void renderStatBarCompact(GuiGraphics graphics, int x, int y, String label, int val, int color) {
        int neonColor = color | 0xFF000000;
        graphics.drawString(this.font, label, x, y + 2, neonColor, true);
        int barX = x + 52;
        int barW = 44;
        int barH = 10;
        graphics.fill(barX, y + 2, barX + barW, y + 2 + barH, 0x6008101C);
        graphics.fill(barX + 1, y + 3, barX + barW - 1, y + 1 + barH, (color & 0x00FFFFFF) | 0x2B000000);
        graphics.renderOutline(barX, y + 2, barW, barH, (color & 0x00FFFFFF) | 0x66000000);

        int filled = val > 0 ? Math.max(3, Math.min(barW - 2, (val * (barW - 2)) / 1200)) : 0;
        if (filled > 0) {
            graphics.fillGradient(barX + 1, y + 3, barX + 1 + filled, y + 1 + barH, neonColor, (color & 0x00FFFFFF) | 0xFF000000);
            graphics.fill(barX + 1, y + 3, barX + 1 + filled, y + 5, 0xDDFFFFFF);
        }

        int pillX = barX + barW + 3;
        int pillW = 28;
        graphics.fillGradient(pillX, y + 2, pillX + pillW, y + 2 + barH, 0x60122234, 0x600B1622);
        graphics.renderOutline(pillX, y + 2, pillW, barH, (color & 0x00FFFFFF) | 0x77000000);
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
        int panelX = left + 14;
        int panelY = top + 50;
        int panelW = GUI_WIDTH - 28; // 492px

        graphics.fillGradient(panelX, panelY, panelX + panelW, panelY + 44, 0x400E1C2C, 0x40060E18);
        graphics.renderOutline(panelX, panelY, panelW, 44, 0x5500E5FF);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 2, 0x8800E5FF);

        if (hasTrack) {
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.linked", data.trackPos().getX(), data.trackPos().getY(), data.trackPos().getZ()), panelX + 10, panelY + 6, 0xFFFFFF, true);
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.waypoints", data.waypointCount()), panelX + 10, panelY + 18, 0xFFCCE3EC, true);
            Component stStr = switch (data.raceState()) {
                case 0 -> Component.translatable("awakening_system.uma.hub.race.state.idle");
                case 1 -> Component.translatable("awakening_system.uma.hub.race.state.starting");
                case 2 -> Component.translatable("awakening_system.uma.hub.race.state.running");
                default -> Component.translatable("awakening_system.uma.hub.race.state.finished");
            };
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.track_status").append(stStr), panelX + 10, panelY + 30, 0xFF70E8FF, true);
        } else {
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.no_track_title"), panelX + 10, panelY + 8, 0xFFFF6666, true);
            graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.no_track_desc"), panelX + 10, panelY + 22, 0xFFFFEE, true);
        }

        int gridY = panelY + 50;
        int cardW = (panelW - 10) / 2; // 241px
        int cardH = 32;
        for (int lane = 0; lane < 8; lane++) {
            int col = lane % 2;
            int row = lane / 2;
            int cx = panelX + col * (cardW + 10);
            int cy = gridY + row * (cardH + 5);

            OpenUmaHubPayload.ParticipantDto part = findParticipant(lane);
            if (part != null) {
                graphics.fillGradient(cx, cy, cx + cardW, cy + cardH, 0x50162B42, 0x500E1B2B);
                graphics.renderOutline(cx, cy, cardW, cardH, 0x8800E5FF);
                String tag = part.isNpc() ? "§e[NPC] " : "§6★ ";
                graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.lane", lane + 1, tag + part.displayName()), cx + 8, cy + 4, 0xFFFFFF, true);
                graphics.drawString(this.font, "§e" + UmaProfile.byId(part.umaId()).getDisplayName() + " §6(★" + part.rarity() + ") §7• Spd: §a§l" + part.speed(), cx + 8, cy + 16, 0xFFCCE3EC, true);
            } else {
                graphics.fillGradient(cx, cy, cx + cardW, cy + cardH, 0x300F1F30, 0x30091420);
                graphics.renderOutline(cx, cy, cardW, cardH, 0x4000E5FF);
                graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.lane_ready", lane + 1), cx + 8, cy + 5, 0xFFCCE3EC, true);
                graphics.drawString(this.font, Component.translatable("awakening_system.uma.hub.race.lane_hint"), cx + 8, cy + 17, 0xFF8FA7B8, true);
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
            boolean hov = this.isHoveredOrFocused();
            int bg = activeTab ? 0x6012293F : (hov ? 0x40162B42 : 0x250A1420);
            int border = activeTab ? 0xCC00E5FF : (hov ? 0x7700E5FF : 0x3000E5FF);
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, border);
            if (activeTab) {
                graphics.fill(this.getX() + 2, this.getY() + this.height - 2, this.getX() + this.width - 2, this.getY() + this.height, 0xFF00E5FF);
                graphics.fill(this.getX() + 4, this.getY() + 1, this.getX() + this.width - 4, this.getY() + 2, 0x66B4FAFF);
            }
            int textX = this.getX() + (this.width - font.width(this.text)) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            graphics.drawString(font, this.text, textX, textY, activeTab ? 0xFFFFFFFF : (hov ? 0xFFE0F7FA : 0xFF8FA7B8), false);
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
            this(x, y, width, height, text, textColor, 0x50142335, hoverBg, action);
        }

        public UmaMenuButton(int x, int y, int width, int height, String text, int textColor, int normalBg, int hoverBg, Runnable action) {
            this(x, y, width, height, Component.literal(text), textColor, normalBg, hoverBg, action);
        }

        public UmaMenuButton(int x, int y, int width, int height, String text, int textColor, int hoverBg, Runnable action) {
            this(x, y, width, height, Component.literal(text), textColor, 0x50142335, hoverBg, action);
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
            int botCol = hovered ? darkenColor(this.hoverBg, 0.85F) : darkenColor(this.normalBg, 0.75F);

            graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, topCol, botCol);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, hovered ? 0xFFFFFFFF : (this.textColor & 0x00FFFFFF) | 0x88000000);

            if (this.height >= 12) {
                graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + 2, hovered ? 0x55FFFFFF : 0x22FFFFFF);
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
            boolean hov = this.isHoveredOrFocused();
            int bg = isSelected ? 0x601B3855 : (hov ? 0x40162B42 : 0x250A1420);
            int border = isSelected ? 0xFF00E5FF : (hov ? 0x8800E5FF : (isAce ? 0x662ECC71 : 0x3000E5FF));
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, border);

            if (isSelected) {
                graphics.fill(this.getX(), this.getY(), this.getX() + 3, this.getY() + this.height, 0xFF00E5FF);
            } else if (isAce) {
                graphics.fill(this.getX(), this.getY(), this.getX() + 3, this.getY() + this.height, 0xFF2ECC71);
            }

            UmaProfile p = UmaProfile.byId(dto.umaId());
            String aceBadge = isAce ? " §a[👑Ace]" : "";
            String fullTitle = p.getDisplayName() + aceBadge;
            int maxNameW = this.width - 10;
            if (font.width(fullTitle) > maxNameW) {
                fullTitle = font.plainSubstrByWidth(fullTitle, maxNameW - 8) + "…";
            }
            graphics.drawString(font, fullTitle, this.getX() + 6, this.getY() + 4, isSelected ? 0xFFFFFFFF : 0xFFCCE3EC, false);

            String rankLetter = calculateRankLetter(dto);
            int rColor = getRankColor(rankLetter);
            graphics.drawString(font, "★".repeat(dto.rarity()) + " §7• " + rankLetter, this.getX() + 6, this.getY() + 17, rColor, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }
}
