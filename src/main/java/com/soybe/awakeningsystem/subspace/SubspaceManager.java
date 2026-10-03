package com.soybe.awakeningsystem.subspace;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class SubspaceManager {
    public static final ResourceKey<Level> SUBSPACE_REALM = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "subspace_realm")
    );

    private SubspaceManager() {}

    public static int getPlotOriginX(Player player) {
        int plotIndex = Math.abs(player.getUUID().hashCode() % 10000);
        return (plotIndex % 100) * 512;
    }

    public static int getPlotOriginZ(Player player) {
        int plotIndex = Math.abs(player.getUUID().hashCode() % 10000);
        return (plotIndex / 100) * 512;
    }

    public static int getPlotOriginY() {
        return 80;
    }

    public static List<Container> getPlotChestContainers(ServerLevel level, int ox, int oy, int oz) {
        List<Container> result = new java.util.ArrayList<>();
        int radius = 28;
        int minChunkX = (ox - radius) >> 4;
        int maxChunkX = (ox + radius) >> 4;
        int minChunkZ = (oz - radius) >> 4;
        int maxChunkZ = (oz + radius) >> 4;

        List<net.minecraft.world.level.block.entity.BlockEntity> chestEntities = new java.util.ArrayList<>();

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk != null) {
                    List<net.minecraft.world.level.block.entity.BlockEntity> snapshot;
                    try {
                        snapshot = new java.util.ArrayList<>(chunk.getBlockEntities().values());
                    } catch (Throwable t) {
                        snapshot = java.util.Collections.emptyList();
                    }
                    for (net.minecraft.world.level.block.entity.BlockEntity be : snapshot) {
                        if (be != null && !be.isRemoved()) {
                            BlockPos pos = be.getBlockPos();
                            if (pos.getX() >= ox - radius && pos.getX() <= ox + radius &&
                                    pos.getZ() >= oz - radius && pos.getZ() <= oz + radius &&
                                    pos.getY() >= oy - 1 && pos.getY() <= oy + 10) {
                                if (be instanceof net.minecraft.world.level.block.entity.ChestBlockEntity ||
                                        be instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity) {
                                    chestEntities.add(be);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ordenamiento determinista: Y (altura), luego X, luego Z
        chestEntities.sort(java.util.Comparator.comparingInt((net.minecraft.world.level.block.entity.BlockEntity be) -> be.getBlockPos().getY())
                .thenComparingInt(be -> be.getBlockPos().getX())
                .thenComparingInt(be -> be.getBlockPos().getZ()));

        for (net.minecraft.world.level.block.entity.BlockEntity be : chestEntities) {
            if (be instanceof Container c) {
                result.add(c);
            }
        }

        return result;
    }

    public static void syncWorldToSubspace(ServerLevel level, ServerPlayer player) {
        try {
            int ox = getPlotOriginX(player);
            int oz = getPlotOriginZ(player);
            int oy = getPlotOriginY();

            List<Container> containers = getPlotChestContainers(level, ox, oy, oz);
            int chestCount = Math.max(2, containers.size());
            int unlockedSlots = chestCount * 27;

            SystemData data = SystemEvents.data(player);
            data.ensureSubspaceCapacity(Math.max(54, unlockedSlots));
            data.setSubspaceChestCount(chestCount);
            data.setSubspaceUnlockedSlots(unlockedSlots);

            net.minecraft.core.NonNullList<ItemStack> items = data.getSubspaceItems();
            for (int c = 0; c < containers.size(); c++) {
                Container chest = containers.get(c);
                for (int s = 0; s < 27; s++) {
                    int globalIndex = c * 27 + s;
                    if (globalIndex < items.size()) {
                        items.set(globalIndex, chest.getItem(s).copy());
                    }
                }
            }
            AwakeningSystemMod.sync(player);
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.error("Error al sincronizar mundo a Subespacio", t);
        }
    }

    public static void syncSubspaceToWorld(ServerLevel level, ServerPlayer player) {
        try {
            int ox = getPlotOriginX(player);
            int oz = getPlotOriginZ(player);
            int oy = getPlotOriginY();

            List<Container> containers = getPlotChestContainers(level, ox, oy, oz);
            SystemData data = SystemEvents.data(player);
            net.minecraft.core.NonNullList<ItemStack> items = data.getSubspaceItems();

            for (int c = 0; c < containers.size(); c++) {
                Container chest = containers.get(c);
                boolean changed = false;
                for (int s = 0; s < 27; s++) {
                    int globalIndex = c * 27 + s;
                    ItemStack stack = (globalIndex < items.size()) ? items.get(globalIndex) : ItemStack.EMPTY;
                    if (!ItemStack.matches(chest.getItem(s), stack)) {
                        chest.setItem(s, stack.copy());
                        changed = true;
                    }
                }
                if (changed) {
                    chest.setChanged();
                }
            }
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.error("Error al sincronizar Subespacio a mundo", t);
        }
    }

    public static void openSubspace(ServerPlayer player) {
        try {
            SystemData data = SystemEvents.data(player);

            // Si el jugador está físicamente dentro del Reino, sincronizar con los cofres de la plataforma
            if (player.serverLevel().dimension().equals(SUBSPACE_REALM)) {
                syncWorldToSubspace(player.serverLevel(), player);
            }

            int chestCount = Math.max(2, data.subspaceChestCount());
            int unlockedSlots = Math.max(54, data.subspaceUnlockedSlots());

            data.ensureSubspaceCapacity(Math.max(54, unlockedSlots));
            int totalPages = Math.max(1, (Math.max(54, unlockedSlots) + 53) / 54);

            net.minecraft.world.inventory.SimpleContainerData menuData = new net.minecraft.world.inventory.SimpleContainerData(4);
            menuData.set(0, unlockedSlots);
            menuData.set(1, chestCount);
            menuData.set(2, 0); // Página 0 al abrir
            menuData.set(3, totalPages);

            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("§b✦ Subespacio del Sistema (Nivel " + data.subspaceLevel() + ") ✦");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                    return new SubspaceMenu(containerId, playerInventory,
                            new SubspaceContainer(data.getSubspaceItems(), () -> {}), menuData);
                }
            });
            player.playNotifySound(SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS, 0.9F, 1.1F);
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.error("Error al abrir Subespacio para {}", player.getName().getString(), t);
        }
    }

    public static void enterSubspace(ServerPlayer player) {
        if (player.serverLevel().dimension().equals(SUBSPACE_REALM)) {
            player.displayClientMessage(Component.literal("§e✦ Ya te encuentras dentro de tu Subespacio Dimensional."), true);
            return;
        }

        SystemData data = SystemEvents.data(player);
        // Guardar posición y dimensión actual para el retorno seguro
        data.setSubspaceReturnLocation(
                player.level().dimension().location().toString(),
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot()
        );

        ServerLevel subspaceLevel = player.getServer().getLevel(SUBSPACE_REALM);
        if (subspaceLevel == null) {
            player.sendSystemMessage(Component.literal("§c[ERROR] La dimensión del Subespacio no está disponible en este momento."));
            return;
        }

        int originX = getPlotOriginX(player);
        int originZ = getPlotOriginZ(player);
        int originY = getPlotOriginY();

        // Teletransportar sobre la plataforma
        player.teleportTo(subspaceLevel, originX + 0.5D, originY + 1.0D, originZ - 2.5D, 0.0F, 0.0F);
        player.fallDistance = 0.0F;

        ensureSubspaceStructure(subspaceLevel, originX, originY, originZ, data.subspaceLevel());
        syncSubspaceToWorld(subspaceLevel, player);

        subspaceLevel.sendParticles(ParticleTypes.PORTAL, originX + 0.5D, originY + 1.5D, originZ - 2.5D, 40, 0.5D, 0.8D, 0.5D, 0.1D);
        player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 1.0F, 1.2F);

        int chests = data.subspaceChestCount();
        int slots = data.subspaceUnlockedSlots();
        player.sendSystemMessage(Component.literal("§b🌌 ¡Has entrado a tu Subespacio Dimensional! §e(Nivel " + data.subspaceLevel() + ")"));
        player.sendSystemMessage(Component.literal("§7✦ Almacén actual: §e" + chests + " Cofres §7(§b" + slots + " ranuras§7). Coloca más cofres para expandir el inventario."));
        player.sendSystemMessage(Component.literal("§e✦ Haz clic en el Waypoint Central o usa §a/escape §7o §a/system subspace exit §7para regresar."));
        AwakeningSystemMod.sync(player);
    }

    public static void exitSubspace(ServerPlayer player) {
        ServerLevel currentSubspace = player.serverLevel();
        if (currentSubspace.dimension().equals(SUBSPACE_REALM)) {
            syncWorldToSubspace(currentSubspace, player);
        }

        SystemData data = SystemEvents.data(player);
        ResourceLocation dimLoc = ResourceLocation.tryParse(data.subspaceReturnDim());
        ServerLevel returnLevel = null;
        if (dimLoc != null && player.getServer() != null) {
            returnLevel = player.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimLoc));
        }
        if (returnLevel == null && player.getServer() != null) {
            returnLevel = player.getServer().overworld();
        }
        if (returnLevel == null) return;

        double rx = data.subspaceReturnX();
        double ry = data.subspaceReturnY();
        double rz = data.subspaceReturnZ();
        if (ry <= -60 || ry >= 320) {
            BlockPos spawn = returnLevel.getSharedSpawnPos();
            rx = spawn.getX() + 0.5;
            ry = spawn.getY() + 1.0;
            rz = spawn.getZ() + 0.5;
        }

        player.teleportTo(returnLevel, rx, ry, rz, data.subspaceReturnYaw(), data.subspaceReturnPitch());
        player.fallDistance = 0.0F;
        player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.literal("§a✔ Regresaste exitosamente de tu Subespacio Dimensional."), true);
    }

    public static boolean upgradeSubspace(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        int curLvl = data.subspaceLevel();
        if (curLvl >= 5) {
            player.sendSystemMessage(Component.literal("§a✔ Tu Subespacio Dimensional ya ha alcanzado el Nivel Máximo (Nivel 5: Pequeño Mundo del Monarca)."));
            return false;
        }

        int nextLvl = curLvl + 1;
        int coinCost = switch (nextLvl) {
            case 2 -> 2500;
            case 3 -> 8000;
            case 4 -> 20000;
            default -> 50000;
        };

        boolean hasOre = false;
        ItemStack oreStack = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (nextLvl == 2 && stack.is(SystemItems.ENHANCEMENT_ORE.get())) {
                    hasOre = true;
                    oreStack = stack;
                    break;
                } else if (nextLvl == 3 && stack.is(SystemItems.FINE_ENHANCEMENT_ORE.get())) {
                    hasOre = true;
                    oreStack = stack;
                    break;
                } else if (nextLvl >= 4 && stack.is(SystemItems.MYSTIC_ENHANCEMENT_ORE.get())) {
                    hasOre = true;
                    oreStack = stack;
                    break;
                }
            }
        }

        if (hasOre) {
            oreStack.shrink(1);
        } else if (data.coins() >= coinCost) {
            data.setCoins(data.coins() - coinCost);
        } else {
            player.sendSystemMessage(Component.literal("§cFondos insuficientes para mejorar el Subespacio a Nivel " + nextLvl + "."));
            player.sendSystemMessage(Component.literal("§7Coste requerido: §6" + coinCost + " Monedas §7o §b1x Mineral de Mejora acorde."));
            player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return false;
        }

        data.setSubspaceLevel(nextLvl);
        AwakeningSystemMod.sync(player);
        player.playNotifySound(ModSounds.SYSTEM_LEVEL_UP.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // Si el jugador está dentro de su subespacio, expandir la estructura en vivo
        if (player.serverLevel().dimension().equals(SUBSPACE_REALM)) {
            int plotIndex = Math.abs(player.getUUID().hashCode() % 10000);
            int originX = (plotIndex % 100) * 512;
            int originZ = (plotIndex / 100) * 512;
            int originY = 80;
            ensureSubspaceStructure(player.serverLevel(), originX, originY, originZ, nextLvl);
            player.serverLevel().sendParticles(ParticleTypes.END_ROD, originX + 0.5D, originY + 2.0D, originZ + 0.5D, 60, 2.0D, 1.5D, 2.0D, 0.1D);
        }

        player.sendSystemMessage(Component.literal("§6✨ ¡Tu Subespacio Dimensional ha ascendido a NIVEL " + nextLvl + "!"));
        player.sendSystemMessage(Component.literal("§7✦ El territorio se ha expandido significativamente. ¡Construye tu almacén y base sagrada!"));
        return true;
    }

    public static void ensureSubspaceStructure(ServerLevel level, int ox, int oy, int oz, int subLvl) {
        int radius = switch (subLvl) {
            case 1 -> 5;   // 11x11
            case 2 -> 9;   // 19x19
            case 3 -> 14;  // 29x29
            case 4 -> 19;  // 39x39
            default -> 26; // 53x53
        };

        // Generar suelo y plataforma (SOLO reemplaza aire para no destruir cofres ni construcciones del jugador)
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= radius + 0.4) {
                    BlockPos fpos = new BlockPos(ox + dx, oy - 1, oz + dz);
                    BlockState current = level.getBlockState(fpos);
                    if (current.isAir()) {
                        if (Math.abs(dist - radius) < 1.0) {
                            level.setBlockAndUpdate(fpos, Blocks.GILDED_BLACKSTONE.defaultBlockState());
                        } else if (Math.abs(dx) % 5 == 0 && Math.abs(dz) % 5 == 0) {
                            level.setBlockAndUpdate(fpos, Blocks.SEA_LANTERN.defaultBlockState());
                        } else if (Math.abs(dx) == Math.abs(dz) || Math.abs(dx) == 0 || Math.abs(dz) == 0) {
                            level.setBlockAndUpdate(fpos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                        } else if (subLvl >= 3 && dist <= 4.0 && dist > 1.5) {
                            level.setBlockAndUpdate(fpos, SystemBlocks.SPIRIT_JADE_BLOCK.get().defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(fpos, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                        }
                    }
                }
            }
        }

        // Colocar Waypoint Central de salida (en el centro)
        BlockPos centerPos = new BlockPos(ox, oy, oz);
        BlockPos belowCenter = new BlockPos(ox, oy - 1, oz);
        level.setBlockAndUpdate(belowCenter, Blocks.GILDED_BLACKSTONE.defaultBlockState());
        if (level.getBlockState(centerPos).isAir()) {
            level.setBlockAndUpdate(centerPos, SystemBlocks.CELESTIAL_WAYPOINT.get().defaultBlockState());
        }

        // Estructuras de bienvenida para Nivel 1 (Cofre Doble Inicial = 54 ranuras del Subespacio)
        BlockPos chest1 = new BlockPos(ox + 3, oy, oz);
        BlockPos chest2 = new BlockPos(ox + 3, oy, oz + 1);
        if (level.getBlockState(chest1).isAir() && level.getBlockState(chest2).isAir()) {
            level.setBlockAndUpdate(chest1, Blocks.CHEST.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.ChestBlock.FACING, net.minecraft.core.Direction.WEST)
                    .setValue(net.minecraft.world.level.block.ChestBlock.TYPE, net.minecraft.world.level.block.state.properties.ChestType.RIGHT));
            level.setBlockAndUpdate(chest2, Blocks.CHEST.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.ChestBlock.FACING, net.minecraft.core.Direction.WEST)
                    .setValue(net.minecraft.world.level.block.ChestBlock.TYPE, net.minecraft.world.level.block.state.properties.ChestType.LEFT));
        } else if (level.getBlockState(chest1).is(Blocks.CHEST) && level.getBlockState(chest2).isAir()) {
            level.setBlockAndUpdate(chest1, Blocks.CHEST.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.ChestBlock.FACING, net.minecraft.core.Direction.WEST)
                    .setValue(net.minecraft.world.level.block.ChestBlock.TYPE, net.minecraft.world.level.block.state.properties.ChestType.RIGHT));
            level.setBlockAndUpdate(chest2, Blocks.CHEST.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.ChestBlock.FACING, net.minecraft.core.Direction.WEST)
                    .setValue(net.minecraft.world.level.block.ChestBlock.TYPE, net.minecraft.world.level.block.state.properties.ChestType.LEFT));
        } else if (level.getBlockState(chest1).isAir()) {
            level.setBlockAndUpdate(chest1, Blocks.CHEST.defaultBlockState());
        }
        BlockPos craftPos = new BlockPos(ox - 3, oy, oz);
        if (level.getBlockState(craftPos).isAir()) {
            level.setBlockAndUpdate(craftPos, Blocks.CRAFTING_TABLE.defaultBlockState());
        }

        // Nivel 2+: Faroles en las 4 esquinas
        if (subLvl >= 2) {
            int[][] corners = {{6, 6}, {-6, 6}, {6, -6}, {-6, -6}};
            for (int[] c : corners) {
                BlockPos colPos = new BlockPos(ox + c[0], oy, oz + c[1]);
                if (level.getBlockState(colPos).isAir()) {
                    level.setBlockAndUpdate(colPos, Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState());
                    level.setBlockAndUpdate(colPos.above(), Blocks.SEA_LANTERN.defaultBlockState());
                }
            }
        }

        // Nivel 4+: Zona de cultivo espiritual en un lateral
        if (subLvl >= 4) {
            for (int gx = -12; gx <= -8; gx++) {
                for (int gz = -3; gz <= 3; gz++) {
                    BlockPos gpos = new BlockPos(ox + gx, oy - 1, oz + gz);
                    if (gx == -10 && gz == 0) {
                        level.setBlockAndUpdate(gpos, Blocks.WATER.defaultBlockState());
                    } else if (level.getBlockState(gpos).is(Blocks.POLISHED_DEEPSLATE)) {
                        level.setBlockAndUpdate(gpos, Blocks.FARMLAND.defaultBlockState());
                        BlockPos plantPos = gpos.above();
                        if (level.getBlockState(plantPos).isAir()) {
                            level.setBlockAndUpdate(plantPos, SystemBlocks.WILD_GINSENG.get().defaultBlockState());
                        }
                    }
                }
            }
        }
    }
}
