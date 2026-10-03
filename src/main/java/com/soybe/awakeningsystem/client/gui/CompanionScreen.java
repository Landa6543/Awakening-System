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
    private final NonNullList<ItemStack> artifacts = NonNullList.withSize(5, ItemStack.EMPTY);

    private static final int W = 360;
    private static final int H = 230;

    public CompanionScreen(int entityId, CompoundTag data) {
        super(Component.literal("Gestión de Compañero"));
        this.entityId = entityId;
        this.profileId = data.getString("ProfileId");
        this.name = data.getString("Name");
        this.level = data.getInt("Level");
        this.stars = data.getInt("Stars");
        this.element = data.getString("Element");
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
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        // Fondo y marcos elegantes
        g.fill(left, top, left + W, top + H, 0xF00D0F14);
        g.renderOutline(left, top, W, H, 0xFF35445A);
        g.renderOutline(left + 1, top + 1, W - 2, H - 2, 0xFFD4AF37); // Borde dorado

        // Encabezado
        String starsStr = "★".repeat(Math.max(1, stars));
        String title = "§6" + starsStr + " §f" + name + " §8| §e" + element;
        g.drawString(font, title, left + 14, top + 12, 0xFFFFFF);

        // Subtítulo
        g.drawString(font, "§7Nivel: §e" + level + "§7/90 §8• §7Estado: " + (guarding ? "§6Guardia" : "§aSiguiendo"), left + 14, top + 26, 0xAAAAAA);

        // 1. RENDER 3D DEL COMPAÑERO (Panel Izquierdo)
        int modelBoxX = left + 14;
        int modelBoxY = top + 42;
        int modelBoxW = 90;
        int modelBoxH = 120;
        g.fill(modelBoxX, modelBoxY, modelBoxX + modelBoxW, modelBoxY + modelBoxH, 0x80141A24);
        g.renderOutline(modelBoxX, modelBoxY, modelBoxW, modelBoxH, 0xFF2A374A);

        Entity entity = (minecraft != null && minecraft.level != null) ? minecraft.level.getEntity(entityId) : null;
        if (entity instanceof LivingEntity living) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g,
                    modelBoxX,
                    modelBoxY,
                    modelBoxX + modelBoxW,
                    modelBoxY + modelBoxH,
                    42,
                    0.0625F,
                    (float) mouseX,
                    (float) mouseY,
                    living
            );
        }

        // Barra de Vida debajo del modelo
        int hpY = modelBoxY + modelBoxH + 6;
        g.fill(modelBoxX, hpY, modelBoxX + modelBoxW, hpY + 8, 0xFF222222);
        float hpRatio = maxHealth > 0 ? Math.min(1.0F, Math.max(0.0F, health / maxHealth)) : 1.0F;
        g.fill(modelBoxX, hpY, modelBoxX + (int) (modelBoxW * hpRatio), hpY + 8, 0xFF2ECC71);
        g.drawString(font, "§fHP: " + (int) health + "/" + (int) maxHealth, modelBoxX + 4, hpY + 1, 0xFFFFFF);

        // 2. BOTONES DE ACCIÓN (Centro)
        int btnX = left + 114;
        int btnW = 105;

        // Botón Subir Nivel
        int btnY1 = top + 42;
        boolean hoverBtn1 = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY1 && mouseY < btnY1 + 22;
        g.fill(btnX, btnY1, btnX + btnW, btnY1 + 22, hoverBtn1 ? 0xFF283A52 : 0xFF1B2433);
        g.renderOutline(btnX, btnY1, btnW, 22, hoverBtn1 ? 0xFFFFD700 : 0xFF3D4F66);
        g.drawCenteredString(font, "§6✦ Subir Nivel", btnX + btnW / 2, btnY1 + 7, 0xFFFFFF);

        // Botón Guardia
        int btnY2 = top + 70;
        boolean hoverBtn2 = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY2 && mouseY < btnY2 + 22;
        g.fill(btnX, btnY2, btnX + btnW, btnY2 + 22, hoverBtn2 ? 0xFF283A52 : 0xFF1B2433);
        g.renderOutline(btnX, btnY2, btnW, 22, hoverBtn2 ? 0xFFFFD700 : 0xFF3D4F66);
        g.drawCenteredString(font, guarding ? "§e✦ Seguir Amo" : "§6✦ Modo Guardia", btnX + btnW / 2, btnY2 + 7, 0xFFFFFF);

        // Botón Guardar en Contrato
        int btnY3 = top + 98;
        boolean hoverBtn3 = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY3 && mouseY < btnY3 + 22;
        g.fill(btnX, btnY3, btnX + btnW, btnY3 + 22, hoverBtn3 ? 0xFF3B1E22 : 0xFF2A1518);
        g.renderOutline(btnX, btnY3, btnW, 22, hoverBtn3 ? 0xFFFF6B6B : 0xFF5C2B30);
        g.drawCenteredString(font, "§c✦ Guardar Contrato", btnX + btnW / 2, btnY3 + 7, 0xFFFFFF);

        // Atributos del compañero en el centro
        int statBoxY = top + 126;
        g.drawString(font, "§e✦ Atributos:", btnX, statBoxY, 0xFFFFFF);
        g.drawString(font, "§7Ataque Base: §f" + String.format("%.1f", (18.0F + (level - 1) * 1.5F)), btnX + 4, statBoxY + 12, 0xCCCCCC);
        g.drawString(font, "§7Defensa: §f" + String.format("%.1f", (18.0F + (level - 1) * 1.0F)), btnX + 4, statBoxY + 23, 0xCCCCCC);
        g.drawString(font, "§7Daño Habilidad: §b" + (int)(20 + level * 2) + " DMG", btnX + 4, statBoxY + 34, 0xCCCCCC);

        // 3. RANURAS DE ARTEFACTOS (Panel Derecho)
        int artX = left + 230;
        int artY = top + 42;
        g.drawString(font, "§6§l✦ ARTEFACTOS (5)", artX, top + 28, 0xFFFFFF);

        String[] slotIcons = {"🌸 Flor", "🪶 Pluma", "⏳ Reloj", "🏆 Cáliz", "👑 Tiara"};
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

        // 4. BONOS DE CONJUNTO ACTIVOS
        Map<ArtifactSet, Integer> sets = getActiveSetPieces();
        int bonusY = top + 168;
        g.drawString(font, "§e✦ Bonos Activos:", artX, bonusY, 0xFFFFFF);
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
            g.drawString(font, "§8Sin bonos de 2P/4P activos", artX + 4, bonusY + 11, 0x666666);
        }

        // 5. MINI INVENTARIO INFERIOR (Para arrastrar o equipar artefactos)
        int invY = top + 192;
        g.fill(left + 14, invY, left + 220, invY + 28, 0x600E121A);
        g.renderOutline(left + 14, invY, 206, 28, 0xFF2A374A);
        g.drawString(font, "§7Tus artefactos: (Haz clic para equipar)", left + 18, invY + 2, 0xAAAAAA);

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

        // TOOLTIPS AL PASAR EL RATÓN
        for (int i = 0; i < 5; i++) {
            int sy = artY + i * 24;
            if (mouseX >= artX && mouseX < artX + 116 && mouseY >= sy && mouseY < sy + 22) {
                ItemStack stack = artifacts.get(i);
                if (!stack.isEmpty()) {
                    int artBtnX = artX + 86;
                    int btnY = sy + 3;
                    boolean onUpgradeBtn = mouseX >= artBtnX && mouseX < artBtnX + 27 && mouseY >= btnY && mouseY < btnY + 16;
                    int artLvl = GenshinArtifactItem.getLevel(stack);
                    if (onUpgradeBtn) {
                        List<Component> upTip = new java.util.ArrayList<>();
                        upTip.add(Component.literal("§6§lMejorar Artefacto (+1)"));
                        if (artLvl < 20) {
                            int cost = (artLvl + 1) * 350;
                            upTip.add(Component.literal("§7Nivel actual: §a+" + artLvl + " §8➔ §e+" + (artLvl + 1) + " §8/ +20"));
                            upTip.add(Component.literal("§7Coste: §6" + cost + " Monedas §7o §b1x Mineral"));
                            upTip.add(Component.literal("§e✦ Clic Izquierdo en [▲+1] o Clic Derecho en la ranura."));
                        } else {
                            upTip.add(Component.literal("§a✔ Nivel Máximo (+20) alcanzado."));
                        }
                        g.renderComponentTooltip(font, upTip, mouseX, mouseY);
                    } else {
                        List<Component> tooltip = new java.util.ArrayList<>(Screen.getTooltipFromItem(this.minecraft, stack));
                        tooltip.add(Component.literal(""));
                        tooltip.add(Component.literal("§8▪ §aClic Derecho: §7Mejorar (+1)"));
                        tooltip.add(Component.literal("§8▪ §cClic Izquierdo: §7Desequipar"));
                        g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                    }
                }
            }
        }

        if (hoverBtn1) {
            int costMora = level * 2500;
            g.renderComponentTooltip(font, java.util.List.of(
                    Component.literal("§6✦ Ascender Compañero"),
                    Component.literal("§7Coste: §6" + String.format("%,d", costMora) + " Mora §7+ §b1x Ingenio del Héroe"),
                    Component.literal("§aAumenta salud, ataque y daño de habilidades.")
            ), mouseX, mouseY);
        }
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
        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        int btnX = left + 114;
        int btnW = 105;

        // Botón Subir Nivel
        if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= top + 42 && mouseY < top + 64) {
            PacketDistributor.sendToServer(new CompanionActionPayload(entityId, 0));
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
            this.level = Math.min(90, level + 1);
            return true;
        }

        // Botón Guardia
        if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= top + 70 && mouseY < top + 92) {
            PacketDistributor.sendToServer(new CompanionActionPayload(entityId, 1));
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
            this.guarding = !this.guarding;
            return true;
        }

        // Botón Guardar en Contrato
        if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= top + 98 && mouseY < top + 120) {
            PacketDistributor.sendToServer(new CompanionActionPayload(entityId, 2));
            AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
            this.onClose();
            return true;
        }

        // Clic en ranura de artefacto equipada -> mejorar o desequipar
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
                        AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_USE, 1.0F, 1.2F);
                        int cur = GenshinArtifactItem.getLevel(artifacts.get(i));
                        if (cur < 20) GenshinArtifactItem.setLevel(artifacts.get(i), cur + 1);
                        return true;
                    } else if (button == 0) {
                        PacketDistributor.sendToServer(new EquipArtifactPayload(true, entityId, i, -1));
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                        artifacts.set(i, ItemStack.EMPTY);
                        return true;
                    }
                }
            }
        }

        // Clic en artefacto del inventario -> equipar en la ranura correspondiente
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
                        AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.1F, 1.0F);
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
