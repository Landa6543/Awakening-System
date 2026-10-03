package com.soybe.awakeningsystem.guild;

import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class GuildChunkMapScreen extends Screen {
    private static final int GRID_SIZE = 9; // Cuadrícula visible de 9x9 chunks
    private static final int CELL_SIZE = 24;

    private Guild guild;
    private CompoundTag allClaimsTag;
    private final Screen parentScreen;

    private int centerChunkX;
    private int centerChunkZ;

    // Control de arrastre interactivo (Panning con ratón estilo mapa)
    private double mouseDownX = -1;
    private double mouseDownY = -1;
    private boolean isDragging = false;
    private double accumulatedDragX = 0;
    private double accumulatedDragZ = 0;

    public GuildChunkMapScreen(Guild guild, CompoundTag allClaimsTag, Screen parentScreen) {
        super(Component.literal("Mapa de Territorio"));
        this.guild = guild;
        this.allClaimsTag = allClaimsTag != null ? allClaimsTag : new CompoundTag();
        this.parentScreen = parentScreen;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            this.centerChunkX = mc.player.chunkPosition().x;
            this.centerChunkZ = mc.player.chunkPosition().z;
        } else {
            this.centerChunkX = 0;
            this.centerChunkZ = 0;
        }
    }

    /**
     * Permite al cliente actualizar los reclamos y datos del gremio en tiempo real
     * sin cerrar esta pantalla ni expulsar al jugador al menú anterior.
     */
    public void updateData(Guild updatedGuild, CompoundTag updatedClaimsTag) {
        if (updatedGuild != null) {
            this.guild = updatedGuild;
        }
        if (updatedClaimsTag != null) {
            this.allClaimsTag = updatedClaimsTag;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

        int gridPixelSize = GRID_SIZE * (CELL_SIZE + 2);
        int mapLeft = (this.width - gridPixelSize) / 2;
        int mapTop = (this.height - gridPixelSize) / 2 - 10;

        // Marco y fondo del mapa
        g.fill(mapLeft - 12, mapTop - 28, mapLeft + gridPixelSize + 12, mapTop + gridPixelSize + 36, 0xEE0A1118);
        g.renderOutline(mapLeft - 12, mapTop - 28, gridPixelSize + 24, gridPixelSize + 64, 0xFFDFB76C);

        // Encabezado
        String guildTag = guild != null ? guild.getTag() : "---";
        String guildName = guild != null ? guild.getName() : "Sin Gremio";
        g.drawString(this.font, "§6🗺️ MAPA DE TERRITORIO §7- [" + guildTag + "] " + guildName, mapLeft - 4, mapTop - 22, 0xFFFFD700);

        int claimedCount = guild != null ? guild.getClaimedChunks().size() : 0;
        int maxClaims = guild != null ? guild.getMaxClaims() : 0;
        int guildLevel = guild != null ? guild.getLevel() : 1;
        boolean canClaimMore = claimedCount < maxClaims;

        String claimStatus = canClaimMore
                ? "§fChunks: §e" + claimedCount + " §7/ §a" + maxClaims + " §7(Nivel " + guildLevel + ") §a[Reclamable]"
                : "§fChunks: §c" + claimedCount + " §7/ §c" + maxClaims + " §7(Nivel " + guildLevel + ") §c[Límite Alcanzado]";
        g.drawString(this.font, claimStatus, mapLeft - 4, mapTop - 10, 0xFFFFFFFF);

        Minecraft mc = Minecraft.getInstance();
        String currentDim = mc.level != null ? mc.level.dimension().location().toString() : "minecraft:overworld";
        int playerChunkX = mc.player != null ? mc.player.chunkPosition().x : 0;
        int playerChunkZ = mc.player != null ? mc.player.chunkPosition().z : 0;

        int halfGrid = GRID_SIZE / 2;
        int hoveredChunkX = Integer.MIN_VALUE;
        int hoveredChunkZ = Integer.MIN_VALUE;
        String hoveredClaimInfo = null;

        // Renderizado de celdas de chunks
        for (int gz = 0; gz < GRID_SIZE; gz++) {
            for (int gx = 0; gx < GRID_SIZE; gx++) {
                int cx = centerChunkX + (gx - halfGrid);
                int cz = centerChunkZ + (gz - halfGrid);
                int cellX = mapLeft + gx * (CELL_SIZE + 2);
                int cellY = mapTop + 6 + gz * (CELL_SIZE + 2);

                String key = currentDim + "#" + cx + "#" + cz;
                boolean isMyGuild = guild != null && guild.getClaimedChunks().contains(key);
                String otherTag = (!isMyGuild && allClaimsTag.contains(key)) ? allClaimsTag.getString(key) : null;
                boolean isOtherGuild = otherTag != null && !otherTag.isEmpty();

                boolean isPlayerHere = (cx == playerChunkX && cz == playerChunkZ);
                boolean isHqHere = guild != null && guild.hasHq() && currentDim.equals(guild.getHqDimension())
                        && ((int) Math.floor(guild.getHqX() / 16.0)) == cx
                        && ((int) Math.floor(guild.getHqZ() / 16.0)) == cz;

                boolean isHovered = mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE;
                if (isHovered) {
                    hoveredChunkX = cx;
                    hoveredChunkZ = cz;
                    if (isMyGuild) {
                        hoveredClaimInfo = "§a🛡️ Reclamado por tu Gremio [" + guild.getTag() + "] (Clic para Liberar)";
                    } else if (isOtherGuild) {
                        hoveredClaimInfo = "§c⚔ Ocupado por Gremio [" + otherTag + "] (Protegido)";
                    } else if (canClaimMore) {
                        hoveredClaimInfo = "§a🌲 Tierra Salvaje Reclamable (Clic para Reclamar)";
                    } else {
                        hoveredClaimInfo = "§7🔒 Tierra Salvaje (Límite Máximo de Chunks Alcanzado)";
                    }
                }

                // Determinación de colores de celda y bordes
                int fillColor;
                int borderColor;

                if (isMyGuild) {
                    fillColor = isHqHere ? 0xDD6E5515 : 0xDD1A4A6E;
                    borderColor = isHqHere ? 0xFFFFD700 : 0xFF4EE2EC;
                } else if (isOtherGuild) {
                    fillColor = 0xDD5A1A1A;
                    borderColor = 0xFFFF5555;
                } else if (canClaimMore) {
                    // Libre y reclamable: verde tenue con borde verde brillante
                    fillColor = 0xFF12281D;
                    borderColor = 0xFF2D6A42;
                } else {
                    // Libre pero límite alcanzado: grisáceo
                    fillColor = 0xFF14181B;
                    borderColor = 0xFF252D33;
                }

                if (isHovered) {
                    fillColor = isMyGuild ? 0xFF2A5A7E : (isOtherGuild ? 0xFF7A2020 : (canClaimMore ? 0xFF1D4D32 : 0xFF253038));
                }

                g.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, fillColor);
                g.renderOutline(cellX, cellY, CELL_SIZE, CELL_SIZE, borderColor);

                // Si es reclamable libre, dibujar un punto verde central de disponibilidad
                if (!isMyGuild && !isOtherGuild && canClaimMore) {
                    g.fill(cellX + CELL_SIZE / 2 - 1, cellY + CELL_SIZE / 2 - 1, cellX + CELL_SIZE / 2 + 1, cellY + CELL_SIZE / 2 + 1, 0x8050FA7B);
                }

                // Icono central de celda
                if (isHqHere) {
                    g.drawCenteredString(this.font, "🏛️", cellX + CELL_SIZE / 2, cellY + 4, 0xFFFFD700);
                } else if (isMyGuild) {
                    g.drawCenteredString(this.font, "🛡️", cellX + CELL_SIZE / 2, cellY + 4, 0xFF50FA7B);
                } else if (isOtherGuild) {
                    g.drawCenteredString(this.font, "⚔", cellX + CELL_SIZE / 2, cellY + 4, 0xFFFF5555);
                }

                // Marcador del jugador
                if (isPlayerHere) {
                    g.renderOutline(cellX + 2, cellY + 2, CELL_SIZE - 4, CELL_SIZE - 4, 0xFFFFFFFF);
                    g.drawCenteredString(this.font, "▲", cellX + CELL_SIZE / 2, cellY + (isMyGuild || isOtherGuild || isHqHere ? 13 : 8), 0xFFFFFF55);
                }
            }
        }

        // Indicación de arrastre y Botones de Navegación del mapa
        g.drawCenteredString(this.font, Component.literal("§8Arrastra el ratón para mover el mapa"), mapLeft + gridPixelSize / 2, mapTop + gridPixelSize + 2, 0xFF888888);
        int navY = mapTop + gridPixelSize + 14;
        int btnH = 16;

        // Centrar en Jugador
        int myLocBtnX = mapLeft;
        int myLocBtnW = 95;
        boolean myLocHov = mouseX >= myLocBtnX && mouseX < myLocBtnX + myLocBtnW && mouseY >= navY && mouseY < navY + btnH;
        g.fill(myLocBtnX, navY, myLocBtnX + myLocBtnW, navY + btnH, myLocHov ? 0xDD225577 : 0x88113344);
        g.renderOutline(myLocBtnX, navY, myLocBtnW, btnH, myLocHov ? 0xFF00E5FF : 0xFF2A4255);
        g.drawCenteredString(this.font, "📍 Centrar en Mí", myLocBtnX + myLocBtnW / 2, navY + 4, 0xFF70E8FF);

        // Flechas de navegación táctil (Oeste, Norte, Sur, Este)
        int arrowStartX = myLocBtnX + myLocBtnW + 8;
        renderNavArrow(g, arrowStartX, navY, "◀", mouseX, mouseY);
        renderNavArrow(g, arrowStartX + 20, navY, "▲", mouseX, mouseY);
        renderNavArrow(g, arrowStartX + 40, navY, "▼", mouseX, mouseY);
        renderNavArrow(g, arrowStartX + 60, navY, "▶", mouseX, mouseY);

        // Volver
        int backBtnX = mapLeft + gridPixelSize - 75;
        int backBtnW = 75;
        boolean backHov = mouseX >= backBtnX && mouseX < backBtnX + backBtnW && mouseY >= navY && mouseY < navY + btnH;
        g.fill(backBtnX, navY, backBtnX + backBtnW, navY + btnH, backHov ? 0xDD8B2B2B : 0x885A1A1A);
        g.renderOutline(backBtnX, navY, backBtnW, btnH, backHov ? 0xFFFF5555 : 0xFF8B2B2B);
        g.drawCenteredString(this.font, "🔙 Volver", backBtnX + backBtnW / 2, navY + 4, 0xFFFF8888);

        // Tooltip del chunk inspeccionado
        if (hoveredChunkX != Integer.MIN_VALUE) {
            String blockRange = "(" + (hoveredChunkX * 16) + ", " + (hoveredChunkZ * 16) + ") a (" + (hoveredChunkX * 16 + 15) + ", " + (hoveredChunkZ * 16 + 15) + ")";
            Component tip = Component.literal("§eChunk [" + hoveredChunkX + ", " + hoveredChunkZ + "]\n§8Bloques: " + blockRange + "\n" + hoveredClaimInfo);
            g.renderTooltip(this.font, tip, mouseX, mouseY);
        }
    }

    private void renderNavArrow(GuiGraphics g, int x, int y, String label, int mouseX, int mouseY) {
        boolean hov = mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
        g.fill(x, y, x + 16, y + 16, hov ? 0xDD2A4D66 : 0x88152533);
        g.renderOutline(x, y, 16, 16, hov ? 0xFF00E5FF : 0xFF2A4255);
        g.drawCenteredString(this.font, label, x + 8, y + 4, hov ? 0xFFFFFFFF : 0xFF70E8FF);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int cellPitch = CELL_SIZE + 2;
        accumulatedDragX += dragX;
        accumulatedDragZ += dragY;

        boolean moved = false;
        while (accumulatedDragX >= cellPitch) {
            centerChunkX--;
            accumulatedDragX -= cellPitch;
            moved = true;
        }
        while (accumulatedDragX <= -cellPitch) {
            centerChunkX++;
            accumulatedDragX += cellPitch;
            moved = true;
        }
        while (accumulatedDragZ >= cellPitch) {
            centerChunkZ--;
            accumulatedDragZ -= cellPitch;
            moved = true;
        }
        while (accumulatedDragZ <= -cellPitch) {
            centerChunkZ++;
            accumulatedDragZ += cellPitch;
            moved = true;
        }

        if (moved) {
            this.isDragging = true;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            this.mouseDownX = mouseX;
            this.mouseDownY = mouseY;
            this.isDragging = false;
        }

        if (button == 0) {
            int gridPixelSize = GRID_SIZE * (CELL_SIZE + 2);
            int mapLeft = (this.width - gridPixelSize) / 2;
            int mapTop = (this.height - gridPixelSize) / 2 - 10;
            int navY = mapTop + gridPixelSize + 14;
            int btnH = 16;

            // Botón Centrar en Mí
            int myLocBtnX = mapLeft;
            int myLocBtnW = 95;
            if (mouseX >= myLocBtnX && mouseX < myLocBtnX + myLocBtnW && mouseY >= navY && mouseY < navY + btnH) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    this.centerChunkX = mc.player.chunkPosition().x;
                    this.centerChunkZ = mc.player.chunkPosition().z;
                    this.accumulatedDragX = 0;
                    this.accumulatedDragZ = 0;
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.2F);
                }
                return true;
            }

            // Flechas de navegación táctil
            int arrowStartX = myLocBtnX + myLocBtnW + 8;
            if (mouseY >= navY && mouseY < navY + 16) {
                if (mouseX >= arrowStartX && mouseX < arrowStartX + 16) {
                    centerChunkX--; // Oeste
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.0F);
                    return true;
                } else if (mouseX >= arrowStartX + 20 && mouseX < arrowStartX + 36) {
                    centerChunkZ--; // Norte
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.0F);
                    return true;
                } else if (mouseX >= arrowStartX + 40 && mouseX < arrowStartX + 56) {
                    centerChunkZ++; // Sur
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.0F);
                    return true;
                } else if (mouseX >= arrowStartX + 60 && mouseX < arrowStartX + 76) {
                    centerChunkX++; // Este
                    AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 0.8F, 1.0F);
                    return true;
                }
            }

            // Botón Volver
            int backBtnX = mapLeft + gridPixelSize - 75;
            int backBtnW = 75;
            if (mouseX >= backBtnX && mouseX < backBtnX + backBtnW && mouseY >= navY && mouseY < navY + btnH) {
                if (this.parentScreen != null) {
                    Minecraft.getInstance().setScreen(this.parentScreen);
                } else {
                    onClose();
                }
                AwakeningSystemClient.playUiSound(ModSounds.SYSTEM_CLICK.value(), 1.0F, 1.0F);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Solo procesar clic en chunk si no fue un arrastre de mapa (distancia recorrida < 6 píxeles)
        boolean wasClick = !this.isDragging;
        if (this.mouseDownX >= 0 && this.mouseDownY >= 0) {
            double distSq = Math.pow(mouseX - this.mouseDownX, 2) + Math.pow(mouseY - this.mouseDownY, 2);
            if (distSq > 36.0) {
                wasClick = false;
            }
        }
        this.mouseDownX = -1;
        this.mouseDownY = -1;
        this.isDragging = false;

        if (button == 0 && wasClick) {
            int gridPixelSize = GRID_SIZE * (CELL_SIZE + 2);
            int mapLeft = (this.width - gridPixelSize) / 2;
            int mapTop = (this.height - gridPixelSize) / 2 - 10;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && guild != null) {
                UUID myUuid = mc.player.getUUID();
                GuildRole role = guild.getMemberRole(myUuid);
                boolean canClaim = (role != null && role.canClaimTerritory());

                String currentDim = mc.level != null ? mc.level.dimension().location().toString() : "minecraft:overworld";
                int halfGrid = GRID_SIZE / 2;

                // Clic en celdas de chunk
                for (int gz = 0; gz < GRID_SIZE; gz++) {
                    for (int gx = 0; gx < GRID_SIZE; gx++) {
                        int cx = centerChunkX + (gx - halfGrid);
                        int cz = centerChunkZ + (gz - halfGrid);
                        int cellX = mapLeft + gx * (CELL_SIZE + 2);
                        int cellY = mapTop + 6 + gz * (CELL_SIZE + 2);

                        if (mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE) {
                            String key = currentDim + "#" + cx + "#" + cz;
                            boolean isMyGuild = guild.getClaimedChunks().contains(key);
                            boolean isOtherGuild = (!isMyGuild && allClaimsTag.contains(key));

                            if (isOtherGuild) {
                                AwakeningSystemClient.playUiSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                return true;
                            }

                            if (!canClaim) {
                                mc.player.sendSystemMessage(Component.literal("§cNo tienes rango de Oficial o Líder para gestionar territorio."));
                                AwakeningSystemClient.playUiSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                return true;
                            }

                            if (isMyGuild) {
                                // Liberar chunk
                                guild.unclaimChunk(currentDim, cx, cz);
                                PacketDistributor.sendToServer(new GuildActionPayload("UNCLAIM_CHUNK", cx + ":" + cz, 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.ANVIL_DESTROY, 0.7F, 1.4F);
                            } else {
                                // Reclamar chunk
                                if (guild.getClaimedChunks().size() >= guild.getMaxClaims()) {
                                    mc.player.sendSystemMessage(Component.literal("§cLímite de territorio alcanzado (" + guild.getClaimedChunks().size() + "/" + guild.getMaxClaims() + "). Sube el gremio de nivel para expandirte."));
                                    AwakeningSystemClient.playUiSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                    return true;
                                }
                                guild.claimChunk(currentDim, cx, cz);
                                PacketDistributor.sendToServer(new GuildActionPayload("CLAIM_CHUNK", cx + ":" + cz, 0));
                                AwakeningSystemClient.playUiSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.6F);
                            }
                            return true;
                        }
                    }
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }
}
