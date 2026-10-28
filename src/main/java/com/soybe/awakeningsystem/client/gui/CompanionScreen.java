package com.soybe.awakeningsystem.client.gui;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.artifact.ArtifactSet;
import com.soybe.awakeningsystem.artifact.ArtifactType;
import com.soybe.awakeningsystem.artifact.GenshinArtifactItem;
import com.soybe.awakeningsystem.entity.CompanionEntity;
import com.soybe.awakeningsystem.network.CompanionActionPayload;
import com.soybe.awakeningsystem.network.EquipArtifactPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.sounds.SoundEvents;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompanionScreen extends Screen {
    private final int entityId;
    private final String profileId;
    private final String name;
    private int level;
    private final int stars;
    private final String element;
    private float health;
    private float maxHealth;
    private boolean guarding;
    private int ascensionRank;
    private final NonNullList<ItemStack> artifacts = NonNullList.withSize(5, ItemStack.EMPTY);

    private static final int W = 360;
    private static final int H = 230;

    public CompanionScreen(int entityId, CompoundTag data) {
        super(Component.translatable("gui.awakening_system.companion.title"));
        this.entityId = entityId;
        this.profileId = data.getString("ProfileId");
        this.name = data.getString("Name");
        this.level = data.getInt("Level");
        this.stars = data.getInt("Stars");
        this.element = data.getString("Element");
        this.ascensionRank = data.contains("AscensionRank") ? data.getInt("AscensionRank") : 0;
        this.health = data.getFloat("Health");
        this.maxHealth = data.getFloat("MaxHealth");
        this.guarding = data.getBoolean("Guarding");

        if (data.contains("Artifacts", Tag.TAG_LIST)) {
            ListTag list = data.getList("Artifacts", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag slotTag = list.getCompound(i);
                int s = slotTag.getByte("Slot") & 255;
                if (s < 5 && slotTag.contains("id")) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level != null) {
                        ItemStack.parse(mc.level.registryAccess(), slotTag).ifPresent(stack -> artifacts.set(s, stack));
                    }
                }
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        com.soybe.awakeningsystem.entity.CompanionProfile prof = com.soybe.awakeningsystem.entity.CompanionProfile.getById(profileId);
        if (prof != null) {
            AwakeningSystemClient.playUiSound(prof.getAmbientSound(), 1.0F, 1.0F);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / (W + 20.0F);
        float scaleY = (float) this.height / (H + 20.0F);
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(transformMouseX(mouseX), transformMouseY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(transformMouseX(mouseX), transformMouseY(mouseY), button, dragX, dragY);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0x60000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cxF = this.width / 2.0F;
            float cyF = this.height / 2.0F;
            g.pose().translate(cxF, cyF, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cxF, -cyF, 0.0F);
        }

        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        g.fill(left, top, left + W, top + H, 0xF00D0F14);
        g.renderOutline(left, top, W, H, 0xFF35445A);
        g.renderOutline(left + 1, top + 1, W - 2, H - 2, 0xFFD4AF37);

        String starsStr = "★".repeat(Math.max(1, stars));
        String title = "§6" + starsStr + " §f" + name + " §8| §e" + element;
        g.drawString(font, title, left + 14, top + 12, 0xFFFFFF);

        g.drawString(font, Component.translatable("gui.awakening_system.companion.level_status", level, (guarding ? Component.translatable("gui.awakening_system.companion.status_guarding").getString() : Component.translatable("gui.awakening_system.companion.status_following").getString())).getString(), left + 14, top + 26, 0xAAAAAA);

        int modelBoxX = left + 14;
        int modelBoxY = top + 42;
        int modelBoxW = 90;
        int modelBoxH = 120;
        g.fill(modelBoxX, modelBoxY, modelBoxX + modelBoxW, modelBoxY + modelBoxH, 0x80141A24);
        g.renderOutline(modelBoxX, modelBoxY, modelBoxW, modelBoxH, 0xFF2A374A);

        Entity entity = (minecraft != null && minecraft.level != null) ? minecraft.level.getEntity(entityId) : null;
        if (entity instanceof CompanionEntity comp) {
            this.level = comp.getCompanionLevel();
            this.ascensionRank = comp.getAscensionRank();
            this.guarding = comp.isGuarding();
            this.health = comp.getHealth();
            this.maxHealth = comp.getMaxHealth();
        }
        if (entity instanceof LivingEntity living) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g,
                    modelBoxX,
                    modelBoxY,
                    modelBoxX + modelBoxW,
                    modelBoxY + modelBoxH,
                    42,
                    0.0625F,
                    (float) mX,
                    (float) mY,
                    living
            );
        }

        int hpY = modelBoxY + modelBoxH + 6;
        g.fill(modelBoxX, hpY, modelBoxX + modelBoxW, hpY + 8, 0xFF222222);
        float hpRatio = maxHealth > 0 ? Math.min(1.0F, Math.max(0.0F, health / maxHealth)) : 1.0F;
        g.fill(modelBoxX, hpY, modelBoxX + (int) (modelBoxW * hpRatio), hpY + 8, 0xFF2ECC71);
        g.drawString(font, "§fHP: " + (int) health + "/" + (int) maxHealth, modelBoxX + 4, hpY + 1, 0xFFFFFF);

        int btnX = left + 114;
        int btnW = 105;

        int rankCap = com.soybe.awakeningsystem.entity.CompanionAscensionManager.getRankMaxLevel(ascensionRank);
        boolean atRankCap = level >= rankCap;

        int btnY1 = top + 42;
        boolean hoverBtn1 = mX >= btnX && mX < btnX + btnW && mY >= btnY1 && mY < btnY1 + 22;
        g.fill(btnX, btnY1, btnX + btnW, btnY1 + 22, atRankCap ? 0xFF2A1C1C : (hoverBtn1 ? 0xFF283A52 : 0xFF1B2433));
        g.renderOutline(btnX, btnY1, btnW, 22, atRankCap ? 0xFFE74C3C : (hoverBtn1 ? 0xFFFFD700 : 0xFF3D4F66));
        g.drawCenteredString(font, atRankCap ? Component.translatable("gui.awakening_system.companion.rank_limit_btn", rankCap).getString() : Component.translatable("gui.awakening_system.companion.level_up_btn").getString(), btnX + btnW / 2, btnY1 + 7, 0xFFFFFF);

        int btnY2 = top + 70;
        boolean hoverBtn2 = mX >= btnX && mX < btnX + btnW && mY >= btnY2 && mY < btnY2 + 22;
        g.fill(btnX, btnY2, btnX + btnW, btnY2 + 22, hoverBtn2 ? 0xFF283A52 : 0xFF1B2433);
        g.renderOutline(btnX, btnY2, btnW, 22, hoverBtn2 ? 0xFFFFD700 : 0xFF3D4F66);
        g.drawCenteredString(font, guarding ? Component.translatable("gui.awakening_system.companion.action_follow").getString() : Component.translatable("gui.awakening_system.companion.action_guard").getString(), btnX + btnW / 2, btnY2 + 7, 0xFFFFFF);

        int btnY3 = top + 98;
        boolean hoverBtn3 = mX >= btnX && mX < btnX + btnW && mY >= btnY3 && mY < btnY3 + 22;
        g.fill(btnX, btnY3, btnX + btnW, btnY3 + 22, hoverBtn3 ? 0xFF3B1E22 : 0xFF2A1518);
        g.renderOutline(btnX, btnY3, btnW, 22, hoverBtn3 ? 0xFFFF6B6B : 0xFF5C2B30);
        g.drawCenteredString(font, Component.translatable("gui.awakening_system.companion.action_recall").getString(), btnX + btnW / 2, btnY3 + 7, 0xFFFFFF);

        int statBoxY = top + 126;
        g.drawString(font, Component.translatable("gui.awakening_system.companion.attributes_header").getString(), btnX, statBoxY, 0xFFFFFF);
        g.drawString(font, Component.translatable("gui.awakening_system.companion.attr_base_atk", String.format("%.1f", (18.0F + (level - 1) * 1.5F))).getString(), btnX + 4, statBoxY + 12, 0xCCCCCC);
        g.drawString(font, Component.translatable("gui.awakening_system.companion.attr_defense", String.format("%.1f", (18.0F + (level - 1) * 1.0F))).getString(), btnX + 4, statBoxY + 23, 0xCCCCCC);
        g.drawString(font, Component.translatable("gui.awakening_system.companion.attr_skill_dmg", (int)(20 + level * 2)).getString(), btnX + 4, statBoxY + 34, 0xCCCCCC);

        int artX = left + 230;
        int artY = top + 42;
        g.drawString(font, Component.translatable("gui.awakening_system.companion.artifacts_header").getString(), artX, top + 28, 0xFFFFFF);

        String[] slotIcons = {
                Component.translatable("gui.awakening_system.companion.slot_flower").getString(),
                Component.translatable("gui.awakening_system.companion.slot_plume").getString(),
                Component.translatable("gui.awakening_system.companion.slot_sands").getString(),
                Component.translatable("gui.awakening_system.companion.slot_goblet").getString(),
                Component.translatable("gui.awakening_system.companion.slot_circlet").getString()
        };
        for (int i = 0; i < 5; i++) {
            int sy = artY + i * 24;
            boolean hoverSlot = mouseX >= artX && mouseX < artX + 116 && mouseY >= sy && mouseY < sy + 22;
            g.fill(artX, sy, artX + 116, sy + 22, hoverSlot ? 0xFF243042 : 0xFF141A24);
            g.renderOutline(artX, sy, 116, 22, hoverSlot ? 0xFFFFD700 : 0xFF2A374A);

            ItemStack stack = artifacts.get(i);
            if (!stack.isEmpty()) {
                g.renderItem(stack, artX + 3, sy + 3);
                String artName = stack.getHoverName().getString();
                if (artName.length() > 10) artName = artName.substring(0, 9) + "..";
                g.drawString(font, "§f" + artName, artX + 22, sy + 4, 0xFFFFFF);
                int artLvl = GenshinArtifactItem.getLevel(stack);
                g.drawString(font, "§a+" + artLvl, artX + 22, sy + 13, 0x88FF88);

                int artBtnX = artX + 86;
                int btnY = sy + 3;
                boolean hoverUp = mouseX >= artBtnX && mouseX < artBtnX + 27 && mouseY >= btnY && mouseY < btnY + 16;
                if (artLvl < 20) {
                    g.fill(artBtnX, btnY, artBtnX + 27, btnY + 16, hoverUp ? 0xFF2D824A : 0xFF184524);
                    g.renderOutline(artBtnX, btnY, 27, 16, hoverUp ? 0xFF55FF88 : 0xFF2A8540);
                    g.drawCenteredString(font, "§a▲+1", artBtnX + 13, btnY + 4, 0xFFFFFF);
                } else {
                    g.fill(artBtnX, btnY, artBtnX + 27, btnY + 16, 0xFF222222);
                    g.renderOutline(artBtnX, btnY, 27, 16, 0xFF444444);
                    g.drawCenteredString(font, "§7MAX", artBtnX + 13, btnY + 4, 0x888888);
                }
            } else {
                g.drawString(font, "§8" + slotIcons[i], artX + 8, sy + 7, 0x666666);
            }
        }

        Map<ArtifactSet, Integer> sets = getActiveSetPieces();
        int bonusY = top + 168;
        g.drawString(font, Component.translatable("gui.awakening_system.companion.active_bonuses").getString(), artX, bonusY, 0xFFFFFF);
        int setLine = 0;
        for (Map.Entry<ArtifactSet, Integer> entry : sets.entrySet()) {
            if (entry.getValue() >= 2) {
                String setName = entry.getKey().getDisplayName();
                if (setName.length() > 13) {
                    setName = setName.substring(0, 12) + ".";
                }
                String str = entry.getKey().getColorCode() + "• " + setName + " §e(" + entry.getValue() + "P)";
                g.drawString(font, str, artX + 2, bonusY + 11 + setLine * 10, 0xFFFFFF);
                setLine++;
                if (setLine >= 2) break;
            }
        }
        if (setLine == 0) {
            g.drawString(font, Component.translatable("gui.awakening_system.companion.no_set_bonuses").getString(), artX + 4, bonusY + 11, 0x666666);
        }

        int invY = top + 192;
        g.fill(left + 14, invY, left + 220, invY + 28, 0x600E121A);
        g.renderOutline(left + 14, invY, 206, 28, 0xFF2A374A);
        g.drawString(font, Component.translatable("gui.awakening_system.companion.inventory_hint").getString(), left + 18, invY + 2, 0xAAAAAA);

        if (minecraft != null && minecraft.player != null) {
            int slotRendered = 0;
            for (int i = 0; i < minecraft.player.getInventory().items.size(); i++) {
                ItemStack invStack = minecraft.player.getInventory().getItem(i);
                if (!invStack.isEmpty() && invStack.getItem() instanceof GenshinArtifactItem) {
                    int ix = left + 18 + slotRendered * 20;
                    int iy = invY + 10;
                    if (ix + 18 < left + 218) {
                        g.renderItem(invStack, ix, iy);
                        slotRendered++;
                    }
                }
            }
        }

        for (int i = 0; i < 5; i++) {
            int sy = artY + i * 24;
            if (mX >= artX && mX < artX + 116 && mY >= sy && mY < sy + 22) {
                ItemStack stack = artifacts.get(i);
                if (!stack.isEmpty()) {
                    int artBtnX = artX + 86;
                    int btnY = sy + 3;
                    boolean onUpgradeBtn = mX >= artBtnX && mX < artBtnX + 27 && mY >= btnY && mY < btnY + 16;
                    int artLvl = GenshinArtifactItem.getLevel(stack);
                    if (onUpgradeBtn) {
                        List<Component> upTip = new java.util.ArrayList<>();
                        upTip.add(Component.translatable("gui.awakening_system.companion.upgrade_art_title"));
                        if (artLvl < 20) {
                            int cost = (artLvl + 1) * 350;
                            upTip.add(Component.translatable("gui.awakening_system.companion.upgrade_art_level", artLvl, (artLvl + 1)));
                            upTip.add(Component.translatable("gui.awakening_system.companion.upgrade_art_cost", cost));
                            upTip.add(Component.translatable("gui.awakening_system.companion.upgrade_art_hint"));
                        } else {
                            upTip.add(Component.translatable("gui.awakening_system.companion.upgrade_art_max"));
                        }
                        g.renderComponentTooltip(font, upTip, mX, mY);
                    } else {
                        List<Component> tooltip = new java.util.ArrayList<>(Screen.getTooltipFromItem(this.minecraft, stack));
                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.translatable("gui.awakening_system.companion.art_right_click"));
                        tooltip.add(Component.translatable("gui.awakening_system.companion.art_left_click"));
                        g.renderComponentTooltip(font, tooltip, mX, mY);
                    }
                }
            }
        }

        if (hoverBtn1) {
            boolean creative = minecraft != null && minecraft.player != null && minecraft.player.isCreative();
            int costMora = level * 1500;
            java.util.List<Component> tooltip = new java.util.ArrayList<>();
            tooltip.add(Component.translatable("gui.awakening_system.companion.level_up_title"));
            if (level >= rankCap) {
                tooltip.add(Component.translatable("gui.awakening_system.companion.rank_cap_reached", rankCap));
                tooltip.add(Component.translatable("gui.awakening_system.companion.ascend_hint_1"));
                tooltip.add(Component.translatable("gui.awakening_system.companion.ascend_hint_2"));
            } else if (level >= 90) {
                tooltip.add(Component.translatable("gui.awakening_system.companion.level_max"));
            } else if (creative) {
                tooltip.add(Component.translatable("gui.awakening_system.companion.creative_instant"));
            } else {
                tooltip.add(Component.translatable("gui.awakening_system.companion.level_cost_book", String.format("%,d", costMora)));
                tooltip.add(Component.translatable("gui.awakening_system.companion.level_cost_alt", String.format("%,d", costMora * 2)));
            }
            tooltip.add(Component.translatable("gui.awakening_system.companion.level_stat_bonus"));
            g.renderComponentTooltip(font, tooltip, mX, mY);
        }

        g.pose().popPose();
    }

    private Map<ArtifactSet, Integer> getActiveSetPieces() {
        Map<ArtifactSet, Integer> map = new LinkedHashMap<>();
        for (ItemStack stack : artifacts) {
            if (!stack.isEmpty() && stack.getItem() instanceof GenshinArtifactItem art) {
                map.put(art.getArtifactSet(), map.getOrDefault(art.getArtifactSet(), 0) + 1);
            }
        }
        return map;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);

        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        int btnX = left + 114;
        int btnW = 105;

        if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= top + 42 && mouseY < top + 64) {
            PacketDistributor.sendToServer(new CompanionActionPayload(entityId, 0));
            AwakeningSystemClient.playButtonClick();
            return true;
        }

        if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= top + 70 && mouseY < top + 92) {
            PacketDistributor.sendToServer(new CompanionActionPayload(entityId, 1));
            AwakeningSystemClient.playButtonClick();
            this.guarding = !this.guarding;
            return true;
        }

        if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= top + 98 && mouseY < top + 120) {
            PacketDistributor.sendToServer(new CompanionActionPayload(entityId, 2));
            AwakeningSystemClient.playButtonClick();
            this.onClose();
            return true;
        }

        int artX = left + 230;
        int artY = top + 42;
        for (int i = 0; i < 5; i++) {
            int sy = artY + i * 24;
            if (mouseX >= artX && mouseX < artX + 116 && mouseY >= sy && mouseY < sy + 22) {
                if (!artifacts.get(i).isEmpty()) {
                    int artBtnX = artX + 86;
                    int btnY = sy + 3;
                    boolean onUpgradeBtn = mouseX >= artBtnX && mouseX < artBtnX + 27 && mouseY >= btnY && mouseY < btnY + 16;
                    if (button == 1 || onUpgradeBtn) {
                        PacketDistributor.sendToServer(new com.soybe.awakeningsystem.network.UpgradeArtifactPayload(true, entityId, i));
                        AwakeningSystemClient.playButtonClick();
                        AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 1.0F, 1.2F);
                        int cur = GenshinArtifactItem.getLevel(artifacts.get(i));
                        if (cur < 20) GenshinArtifactItem.setLevel(artifacts.get(i), cur + 1);
                        return true;
                    } else if (button == 0) {
                        PacketDistributor.sendToServer(new EquipArtifactPayload(true, entityId, i, -1));
                        AwakeningSystemClient.playButtonClick();
                        artifacts.set(i, ItemStack.EMPTY);
                        return true;
                    }
                }
            }
        }

        int invY = top + 192;
        if (minecraft != null && minecraft.player != null) {
            int slotRendered = 0;
            for (int i = 0; i < minecraft.player.getInventory().items.size(); i++) {
                ItemStack invStack = minecraft.player.getInventory().getItem(i);
                if (!invStack.isEmpty() && invStack.getItem() instanceof GenshinArtifactItem art) {
                    int ix = left + 18 + slotRendered * 20;
                    int iy = invY + 10;
                    if (mouseX >= ix && mouseX < ix + 18 && mouseY >= iy && mouseY < iy + 18) {
                        int targetSlot = art.getArtifactType().getSlotIndex();
                        PacketDistributor.sendToServer(new EquipArtifactPayload(true, entityId, targetSlot, i));
                        AwakeningSystemClient.playButtonClick();
                        artifacts.set(targetSlot, invStack.copy());
                        return true;
                    }
                    slotRendered++;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
