package com.soybe.awakeningsystem.furnace;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class CustomTierFurnaceScreen extends AbstractContainerScreen<CustomTierFurnaceMenu> {
    private static final ResourceLocation TEXTURE_FURNACE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/container/custom_tier_furnace.png");
    private static final ResourceLocation TEXTURE_AUGMENT =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/container/custom_tier_furnace_augment.png");

    public CustomTierFurnaceScreen(CustomTierFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);

        int relX = mouseX - this.leftPos;
        int relY = mouseY - this.topPos;
        FurnaceTier tier = this.menu.getTier();

        if (relX >= 161 && relX <= 172 && relY >= 4 && relY <= 15) {
            java.util.List<Component> tooltip = new java.util.ArrayList<>();
            if (this.menu.isAugmentGuiOpen()) {
                tooltip.add(Component.translatable("awakening_system.furnace.view_smelting"));
                tooltip.add(Component.translatable("awakening_system.furnace.view_smelting_desc"));
            } else {
                tooltip.add(Component.translatable("awakening_system.furnace.config_augments"));
                tooltip.add(Component.translatable("awakening_system.furnace.config_augments_desc"));
            }
            g.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            return;
        }

        if (!this.menu.isAugmentGuiOpen()) {
            if (relX >= 79 && relX <= 103 && relY >= 34 && relY <= 50) {
                java.util.List<Component> tooltip = new java.util.ArrayList<>();
                int augType = this.menu.getAugmentType();
                Component activeMode = (augType == 2) ? Component.translatable("awakening_system.furnace.mode.blast") :
                        (augType == 1) ? Component.translatable("awakening_system.furnace.mode.smoker") :
                        Component.translatable("awakening_system.furnace.mode.standard");
                tooltip.add(Component.translatable("awakening_system.furnace.base_speed", tier.getSpeedDisplay()));
                tooltip.add(Component.translatable("awakening_system.furnace.active_mode", activeMode));
                if (tier.getDoubleOutputChance() > 0.0F) {
                    tooltip.add(Component.translatable("awakening_system.furnace.alchemy_bonus", (int)(tier.getDoubleOutputChance() * 100)));
                }
                g.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            } else if (relX >= 56 && relX <= 70 && relY >= 36 && relY <= 50) {
                java.util.List<Component> tooltip = new java.util.ArrayList<>();
                tooltip.add(Component.translatable("awakening_system.furnace.flame"));
                tooltip.add(Component.translatable("awakening_system.furnace.base_speed", tier.getSpeedDisplay()));
                if (this.menu.isLit()) {
                    tooltip.add(Component.translatable("awakening_system.furnace.lit_active"));
                }
                g.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }
        } else {
            if (relX >= 26 && relX <= 42 && relY >= 35 && relY <= 51 && !this.menu.getSlot(CustomTierFurnaceMenu.AUGMENT_RED_SLOT).hasItem()) {
                java.util.List<Component> tooltip = new java.util.ArrayList<>();
                tooltip.add(Component.translatable("awakening_system.furnace.slot_mode_title"));
                tooltip.add(Component.translatable("awakening_system.furnace.slot_mode_desc1"));
                tooltip.add(Component.translatable("awakening_system.furnace.slot_mode_desc2"));
                g.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            } else if (relX >= 80 && relX <= 96 && relY >= 35 && relY <= 51 && !this.menu.getSlot(CustomTierFurnaceMenu.AUGMENT_GREEN_SLOT).hasItem()) {
                java.util.List<Component> tooltip = new java.util.ArrayList<>();
                tooltip.add(Component.translatable("awakening_system.furnace.slot_perf_title"));
                tooltip.add(Component.translatable("awakening_system.furnace.slot_perf_desc"));
                g.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            } else if (relX >= 134 && relX <= 150 && relY >= 35 && relY <= 51 && !this.menu.getSlot(CustomTierFurnaceMenu.AUGMENT_BLUE_SLOT).hasItem()) {
                java.util.List<Component> tooltip = new java.util.ArrayList<>();
                tooltip.add(Component.translatable("awakening_system.furnace.slot_util_title"));
                tooltip.add(Component.translatable("awakening_system.furnace.slot_util_desc"));
                g.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        FurnaceTier tier = this.menu.getTier();

        if (this.menu.isAugmentGuiOpen()) {
            String tierLabel = switch (tier) {
                case ABYSSAL -> "Abisal";
                case SOUL_ALLOY -> "Almas";
                case ASTRALITE -> "Astralita";
                case MITHRIL -> "Mithril";
                case ADAMANTITE -> "Adamantita";
                case SPIRIT_JADE -> "Jade";
                case ASTRAL -> "Astral";
                case CELESTIAL -> "Celestial";
                case PRIMORDIAL -> "Primordial";
            };
            g.drawString(this.font, Component.literal("Mejoras: " + tierLabel), 8, 6, 0x404040, false);

            g.drawCenteredString(this.font, "§cModo", 35, 23, 0x802020);
            g.drawCenteredString(this.font, "§aVelocidad", 89, 23, 0x206020);
            g.drawCenteredString(this.font, "§bUtilidad", 143, 23, 0x105070);

            g.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 93, 0x404040, false);
        } else {
            String titleStr = this.title.getString();
            g.drawString(this.font, Component.literal(titleStr), 8, 6, 0x404040, false);

            int titleW = this.font.width(titleStr);
            int augType = this.menu.getAugmentType();
            String modeTag = switch (augType) {
                case 2 -> "[Alto Horno 2x]";
                case 1 -> "[Ahumador 2x]";
                default -> "[" + tier.getSpeedDisplay() + "]";
            };
            int tagColor = switch (augType) {
                case 2 -> 0xB04000;
                case 1 -> 0x207020;
                default -> 0x606060;
            };
            int tagW = this.font.width(modeTag);
            if (8 + titleW + 6 + tagW < 158) {
                g.drawString(this.font, Component.literal(modeTag), 8 + titleW + 6, 6, tagColor, false);
            }

            g.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 93, 0x404040, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        if (this.menu.isAugmentGuiOpen()) {
            g.blit(TEXTURE_AUGMENT, x, y, 0, 0, this.imageWidth, this.imageHeight);
        } else {
            g.blit(TEXTURE_FURNACE, x, y, 0, 0, this.imageWidth, this.imageHeight);

            if (this.menu.isLit()) {
                int litProgress = this.menu.getLitProgress();
                g.blit(TEXTURE_FURNACE, x + 56, y + 36 + 12 - litProgress, 176, 12 - litProgress, 14, litProgress + 1);
            }

            int burnProgress = this.menu.getBurnProgress();
            g.blit(TEXTURE_FURNACE, x + 79, y + 34, 176, 14, burnProgress + 1, 16);
        }

        int relX = mouseX - this.leftPos;
        int relY = mouseY - this.topPos;
        if (relX >= 161 && relX <= 172 && relY >= 4 && relY <= 15) {
            g.fill(x + 161, y + 4, x + 172, y + 15, 0x40FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int relX = (int) mouseX - this.leftPos;
        int relY = (int) mouseY - this.topPos;

        if (button == 0 && relX >= 161 && relX <= 172 && relY >= 4 && relY <= 15) {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
