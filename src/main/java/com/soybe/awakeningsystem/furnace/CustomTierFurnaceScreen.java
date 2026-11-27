package com.soybe.awakeningsystem.furnace;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CustomTierFurnaceScreen extends AbstractContainerScreen<CustomTierFurnaceMenu> {
    private static final ResourceLocation TEXTURE_FURNACE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/container/custom_tier_furnace.png");
    private static final ResourceLocation TEXTURE_AUGMENT =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/container/custom_tier_furnace_augment.png");
    private static final ResourceLocation TEXTURE_WIDGETS =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/gui/container/furnace_widgets.png");

    // Left automation panel matching Iron Furnaces exact proportions relative to (leftPos, topPos)
    private static final int PANEL_X = -54;
    private static final int PANEL_Y = 0;
    private static final int PANEL_W = 54;
    private static final int PANEL_H = 104;

    // 6 side buttons (10x10 each): {buttonId, relSideIndex, offsetX_from_leftPos, offsetY_from_topPos}
    private static final int[][] SIDE_BUTTONS = {
            {10, 0, -32, 27}, // TOP
            {14, 4, -44, 39}, // LEFT
            {12, 2, -32, 39}, // FRONT
            {15, 5, -20, 39}, // RIGHT
            {11, 1, -32, 51}, // BOTTOM
            {13, 3, -20, 51}, // BACK
    };
    private static final String[] SIDE_NAMES = {"Top", "Bottom", "Front", "Back", "Left", "Right"};

    private static boolean showAutomationPanel = false;

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

        // Tooltip for automation panel toggle button
        if (!showAutomationPanel) {
            if (relX >= -17 && relX <= 0 && relY >= 0 && relY <= 17) {
                g.renderComponentTooltip(this.font, java.util.List.of(Component.literal("Open Config")), mouseX, mouseY);
                return;
            }
        } else {
            if (relX >= -13 && relX <= -1 && relY >= 6 && relY <= 18) {
                g.renderComponentTooltip(this.font, java.util.List.of(Component.literal("Close Config")), mouseX, mouseY);
                return;
            }
            for (int[] btn : SIDE_BUTTONS) {
                if (relX >= btn[2] && relX < btn[2] + 10 && relY >= btn[3] && relY < btn[3] + 10) {
                    int mode = this.menu.getSideConfig(btn[1]);
                    String modeName = switch (mode) {
                        case 1 -> "§9Input (Top Slot)";
                        case 2 -> "§6Output (Result)";
                        case 3 -> "§5Fuel (Bottom Slot)";
                        case 4 -> "§bInput / Output";
                        default -> "§7Disabled";
                    };
                    g.renderComponentTooltip(this.font, java.util.List.of(
                            Component.literal("§fSide: §e" + SIDE_NAMES[btn[1]]),
                            Component.literal("§7Mode: " + modeName),
                            Component.literal("§8Click to cycle")
                    ), mouseX, mouseY);
                    return;
                }
            }
            if (relX >= -47 && relX < -33 && relY >= 8 && relY < 22) {
                g.renderComponentTooltip(this.font, java.util.List.of(
                        Component.literal("§fAuto-Input"),
                        Component.literal(this.menu.isAutoInput() ? "§aON" : "§cOFF")
                ), mouseX, mouseY);
                return;
            }
            if (relX >= -29 && relX < -15 && relY >= 8 && relY < 22) {
                g.renderComponentTooltip(this.font, java.util.List.of(
                        Component.literal("§fAuto-Output"),
                        Component.literal(this.menu.isAutoOutput() ? "§aON" : "§cOFF")
                ), mouseX, mouseY);
                return;
            }
            if (relY >= 66 && relY < 80) {
                if (relX >= -47 && relX < -33) {
                    g.renderComponentTooltip(this.font, java.util.List.of(Component.literal("Redstone Ignored")), mouseX, mouseY);
                    return;
                }
                if (relX >= -31 && relX < -17) {
                    g.renderComponentTooltip(this.font, java.util.List.of(Component.literal("Redstone Low")), mouseX, mouseY);
                    return;
                }
                if (relX >= -15 && relX < -1) {
                    g.renderComponentTooltip(this.font, java.util.List.of(Component.literal("Redstone High")), mouseX, mouseY);
                    return;
                }
            }
        }

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
            g.drawString(this.font, Component.translatable("awakening_system.msg.mejoras", String.valueOf(tierLabel)), 8, 6, 0x404040, false);

            g.drawCenteredString(this.font, Component.translatable("awakening_system.msg.modo").getString(), 35, 23, 0x802020);
            g.drawCenteredString(this.font, Component.translatable("awakening_system.msg.velocidad").getString(), 89, 23, 0x206020);
            g.drawCenteredString(this.font, Component.translatable("awakening_system.msg.utilidad").getString(), 143, 23, 0x105070);

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

            // Colored slot borders ONLY shown when automation config panel is open (matching Iron Furnaces)
            if (showAutomationPanel) {
                g.renderOutline(x + 55, y + 16, 18, 18, 0xFF2584EA);
                g.renderOutline(x + 55, y + 52, 18, 18, 0xFF8E24AA);
                g.renderOutline(x + 111, y + 30, 26, 26, 0xFFD96514);
            }
        }

        int relX = mouseX - this.leftPos;
        int relY = mouseY - this.topPos;
        // Visible Augments / Furnace toggle button at (161, 4)
        g.blit(TEXTURE_WIDGETS, x + 161, y + 4, this.menu.isAugmentGuiOpen() ? 24 : 12, 0, 12, 12, 64, 64);
        if (relX >= 161 && relX <= 172 && relY >= 4 && relY <= 15) {
            g.fill(x + 161, y + 4, x + 173, y + 16, 0x40FFFFFF);
        }

        // Render left automation panel OR closed tab (seamlessly joined with main GUI)
        if (showAutomationPanel) {
            int px = x + PANEL_X;
            int py = y + PANEL_Y;
            g.fill(px, py, px + PANEL_W + 2, py + PANEL_H, 0xFFC6C6C6);
            // Outer black edges (top, left, bottom — open on right to merge cleanly with main GUI)
            g.fill(px, py, px + PANEL_W, py + 1, 0xFF000000);
            g.fill(px, py + PANEL_H - 1, px + PANEL_W, py + PANEL_H, 0xFF000000);
            g.fill(px, py, px + 1, py + PANEL_H, 0xFF000000);
            // Bevel highlights & shadows matching vanilla GUI
            g.fill(px + 1, py + 1, px + PANEL_W + 2, py + 2, 0xFFFFFFFF);
            g.fill(px + 1, py + 1, px + 2, py + PANEL_H - 1, 0xFFFFFFFF);
            g.fill(px + 2, py + PANEL_H - 2, px + PANEL_W + 1, py + PANEL_H - 1, 0xFF555555);
            // Seamless fill over the main GUI's left border along the panel height
            g.fill(x - 1, py + 2, x + 3, py + PANEL_H - 2, 0xFFC6C6C6);

            // Top row: Auto-Input (-47, 8), Auto-Output (-29, 8), Close gear (-13, 6)
            drawWidgetButton(g, x - 47, y + 8, this.menu.isAutoInput() ? 0xFF2584EA : 0xFF555555, 36, 0, mouseX, mouseY);
            drawWidgetButton(g, x - 29, y + 8, this.menu.isAutoOutput() ? 0xFFD96514 : 0xFF555555, 48, 0, mouseX, mouseY);
            drawGearButton(g, x - 13, y + 6, 12, mouseX, mouseY);

            // 6-side cross (10x10 each)
            for (int[] btn : SIDE_BUTTONS) {
                int bx = x + btn[2];
                int by = y + btn[3];
                int mode = this.menu.getSideConfig(btn[1]);
                int borderColor = switch (mode) {
                    case 1 -> 0xFF2584EA; // Input (Blue)
                    case 2 -> 0xFFD96514; // Output (Orange)
                    case 3 -> 0xFF8E24AA; // Fuel (Purple)
                    case 4 -> 0xFF06B6D4; // I/O (Cyan)
                    default -> 0xFF555555; // None (Gray)
                };
                g.fill(bx, by, bx + 10, by + 10, borderColor);
                g.fill(bx + 1, by + 1, bx + 9, by + 9, 0xFF8B8B8B);
                if (mouseX >= bx && mouseX < bx + 10 && mouseY >= by && mouseY < by + 10) {
                    g.fill(bx + 1, by + 1, bx + 9, by + 9, 0x40FFFFFF);
                }
            }

            // 3 separate Redstone buttons side-by-side at y=66 (14x14 each) using vanilla item icons
            int rs = this.menu.getRedstoneMode();
            drawVanillaItemButton(g, x - 47, y + 66, rs == 0 ? 0xFF2584EA : 0xFF555555, new ItemStack(Items.REDSTONE), mouseX, mouseY);
            drawVanillaItemButton(g, x - 31, y + 66, rs == 1 ? 0xFF2584EA : 0xFF555555, new ItemStack(Items.REDSTONE_TORCH), mouseX, mouseY);
            drawVanillaItemButton(g, x - 15, y + 66, rs == 2 ? 0xFF2584EA : 0xFF555555, new ItemStack(Items.REPEATER), mouseX, mouseY);
        } else {
            // Protruding 17x18 tab on top-left edge (seamlessly merged with main GUI)
            int tx = x - 17;
            int ty = y;
            g.fill(tx, ty, tx + 19, ty + 18, 0xFFC6C6C6);
            g.fill(tx, ty, tx + 17, ty + 1, 0xFF000000);
            g.fill(tx, ty + 17, tx + 17, ty + 18, 0xFF000000);
            g.fill(tx, ty, tx + 1, ty + 18, 0xFF000000);
            g.fill(tx + 1, ty + 1, tx + 19, ty + 2, 0xFFFFFFFF);
            g.fill(tx + 1, ty + 1, tx + 2, ty + 17, 0xFFFFFFFF);
            g.fill(tx + 2, ty + 16, tx + 18, ty + 17, 0xFF555555);
            g.fill(x - 1, ty + 2, x + 2, ty + 16, 0xFFC6C6C6);
            drawGearButton(g, x - 15, y + 2, 14, mouseX, mouseY);
        }
    }

    private void drawGearButton(GuiGraphics g, int bx, int by, int size, int mouseX, int mouseY) {
        g.fill(bx, by, bx + size, by + size, 0xFF555555);
        g.fill(bx + 1, by + 1, bx + size - 1, by + size - 1, 0xFFC6C6C6);
        int iconOff = (size - 12) / 2;
        g.blit(TEXTURE_WIDGETS, bx + iconOff, by + iconOff, 0, 0, 12, 12, 64, 64);
        if (mouseX >= bx && mouseX < bx + size && mouseY >= by && mouseY < by + size) {
            g.fill(bx + 1, by + 1, bx + size - 1, by + size - 1, 0x40FFFFFF);
        }
    }

    private void drawWidgetButton(GuiGraphics g, int bx, int by, int borderColor, int u, int v, int mouseX, int mouseY) {
        g.fill(bx, by, bx + 14, by + 14, borderColor);
        g.fill(bx + 1, by + 1, bx + 13, by + 13, 0xFF8B8B8B);
        g.blit(TEXTURE_WIDGETS, bx + 1, by + 1, u, v, 12, 12, 64, 64);
        if (mouseX >= bx && mouseX < bx + 14 && mouseY >= by && mouseY < by + 14) {
            g.fill(bx + 1, by + 1, bx + 13, by + 13, 0x40FFFFFF);
        }
    }

    private void drawVanillaItemButton(GuiGraphics g, int bx, int by, int borderColor, ItemStack icon, int mouseX, int mouseY) {
        g.fill(bx, by, bx + 14, by + 14, borderColor);
        g.fill(bx + 1, by + 1, bx + 13, by + 13, 0xFF8B8B8B);
        g.pose().pushPose();
        g.pose().translate(bx + 1, by + 1, 0);
        g.pose().scale(0.75F, 0.75F, 1.0F);
        g.renderItem(icon, 0, 0);
        g.pose().popPose();
        if (mouseX >= bx && mouseX < bx + 14 && mouseY >= by && mouseY < by + 14) {
            g.fill(bx + 1, by + 1, bx + 13, by + 13, 0x40FFFFFF);
        }
    }

    private void drawAutoButton(GuiGraphics g, int bx, int by, int borderColor, ItemStack icon, int mouseX, int mouseY) {
        g.fill(bx, by, bx + 16, by + 16, borderColor);
        g.fill(bx + 1, by + 1, bx + 15, by + 15, 0xFF8B8B8B);
        g.renderItem(icon, bx, by);
        if (mouseX >= bx && mouseX < bx + 16 && mouseY >= by && mouseY < by + 16) {
            g.fill(bx + 1, by + 1, bx + 15, by + 15, 0x40FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int relX = (int) mouseX - this.leftPos;
        int relY = (int) mouseY - this.topPos;

        if (button == 0 && this.minecraft != null && this.minecraft.gameMode != null) {
            if (!showAutomationPanel) {
                if (relX >= -17 && relX <= 0 && relY >= 0 && relY <= 17) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    showAutomationPanel = true;
                    return true;
                }
            } else {
                if (relX >= -13 && relX <= -1 && relY >= 6 && relY <= 18) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    showAutomationPanel = false;
                    return true;
                }
                if (relY >= 8 && relY < 22) {
                    if (relX >= -47 && relX < -33) {
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 16);
                        return true;
                    }
                    if (relX >= -29 && relX < -15) {
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 17);
                        return true;
                    }
                }
                for (int[] btn : SIDE_BUTTONS) {
                    if (relX >= btn[2] && relX < btn[2] + 10 && relY >= btn[3] && relY < btn[3] + 10) {
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, btn[0]);
                        return true;
                    }
                }
                if (relY >= 66 && relY < 80) {
                    int clickId = -1;
                    if (relX >= -47 && relX < -33) clickId = 19;
                    else if (relX >= -31 && relX < -17) clickId = 20;
                    else if (relX >= -15 && relX < -1) clickId = 21;
                    if (clickId != -1) {
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, clickId);
                        return true;
                    }
                }
            }
            if (relX >= 161 && relX <= 172 && relY >= 4 && relY <= 15) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
