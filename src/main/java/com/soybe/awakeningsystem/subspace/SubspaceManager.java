package com.soybe.awakeningsystem.subspace;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.List;
import java.util.Optional;
import java.io.InputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public final class SubspaceManager {
    public static final ResourceKey<Level> SUBSPACE_REALM = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "subspace_realm")
    );

    private SubspaceManager() {}

    public static ServerLevel getSubspaceLevel(net.minecraft.server.MinecraftServer server) {
        if (server == null) return null;
        ServerLevel lvl = server.getLevel(SUBSPACE_REALM);
        if (lvl != null) return lvl;
        lvl = server.getLevel(com.soybe.awakeningsystem.PagodaTrialManager.PAGODA_REALM);
        if (lvl != null) return lvl;
        lvl = server.getLevel(com.soybe.awakeningsystem.MurimWorldManager.MURIM_WORLD);
        if (lvl != null) return lvl;
        return server.overworld();
    }

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

    public static boolean isExcludedStoragePath(String path) {
        if (path == null) return true;
        return path.contains("ender") || path.contains("crafting") || path.contains("furnace")
                || path.contains("blast") || path.contains("smoker") || path.contains("brewing")
                || path.contains("hopper") || path.contains("dispenser") || path.contains("dropper")
                || path.contains("crafter") || path.contains("jukebox") || path.contains("lectern")
                || path.contains("bookshelf") || path.contains("roulette") || path.contains("plushie")
                || path.contains("pool") || path.contains("bell") || path.contains("respawn")
                || path.contains("spawner");
    }

    public static boolean isStorageBlock(net.minecraft.world.level.block.entity.BlockEntity be, Level level) {
        if (be == null || be.isRemoved()) return false;

        if (be instanceof net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
                || be instanceof net.minecraft.world.level.block.entity.BrewingStandBlockEntity
                || be instanceof net.minecraft.world.level.block.entity.HopperBlockEntity
                || be instanceof net.minecraft.world.level.block.entity.DispenserBlockEntity
                || be instanceof net.minecraft.world.level.block.entity.CrafterBlockEntity
                || be instanceof net.minecraft.world.level.block.entity.EnderChestBlockEntity
                || be instanceof net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity
                || be instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity
                || be instanceof net.minecraft.world.level.block.entity.BeaconBlockEntity) {
            return false;
        }

        String className = be.getClass().getName().toLowerCase(java.util.Locale.ROOT);
        if (className.contains("roulette") || className.contains("plushie") || className.contains("pool")) {
            return false;
        }

        if (be instanceof Container c) {
            return c.getContainerSize() > 0;
        }

        if (level != null) {
            try {
                var handler = level.getCapability(Capabilities.ItemHandler.BLOCK,
                        be.getBlockPos(), be.getBlockState(), be, null);
                if (handler != null && handler.getSlots() > 0) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        return false;
    }

    public static boolean isStorageBlockState(BlockState state, Level level, BlockPos pos) {
        if (state == null) return false;
        if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL)
                || state.getBlock() instanceof ShulkerBoxBlock) {
            return true;
        }

        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id != null) {
            String path = id.getPath().toLowerCase(java.util.Locale.ROOT);
            if (!isExcludedStoragePath(path)) {
                if (path.contains("chest") || path.contains("barrel") || path.contains("crate")
                        || path.contains("storage") || path.contains("vault") || path.contains("drawer")
                        || path.contains("box") || path.contains("safe") || path.contains("cabinet")
                        || path.contains("locker")) {
                    return true;
                }
            }
        }

        if (level != null && pos != null) {
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
            if (be != null && isStorageBlock(be, level)) {
                return true;
            }
        }
        return false;
    }

    public static class ItemHandlerContainerWrapper implements Container {
        private final IItemHandler handler;
        private final net.minecraft.world.level.block.entity.BlockEntity blockEntity;

        public ItemHandlerContainerWrapper(IItemHandler handler, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
            this.handler = handler;
            this.blockEntity = blockEntity;
        }

        public IItemHandler getHandler() {
            return handler;
        }

        @Override
        public int getContainerSize() {
            return handler.getSlots();
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (slot < 0 || slot >= handler.getSlots()) {
                return ItemStack.EMPTY;
            }
            return handler.getStackInSlot(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (slot < 0 || slot >= handler.getSlots()) {
                return ItemStack.EMPTY;
            }
            return handler.extractItem(slot, amount, false);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            if (slot < 0 || slot >= handler.getSlots()) {
                return ItemStack.EMPTY;
            }
            ItemStack current = handler.getStackInSlot(slot);
            return handler.extractItem(slot, current.getCount(), false);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot < 0 || slot >= handler.getSlots()) {
                return;
            }
            if (handler instanceof IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(slot, stack != null ? stack.copy() : ItemStack.EMPTY);
            } else {
                handler.extractItem(slot, handler.getSlotLimit(slot), false);
                if (stack != null && !stack.isEmpty()) {
                    handler.insertItem(slot, stack.copy(), false);
                }
            }
        }

        @Override
        public void setChanged() {
            if (blockEntity != null) {
                blockEntity.setChanged();
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return blockEntity != null && !blockEntity.isRemoved();
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < handler.getSlots(); i++) {
                setItem(i, ItemStack.EMPTY);
            }
        }
    }

    public static List<Container> getPlotChestContainers(ServerLevel level, int ox, int oy, int oz) {
        List<Container> result = new java.util.ArrayList<>();
        int radius = 32;
        int minChunkX = (ox - radius) >> 4;
        int maxChunkX = (ox + radius) >> 4;
        int minChunkZ = (oz - radius) >> 4;
        int maxChunkZ = (oz + radius) >> 4;

        List<net.minecraft.world.level.block.entity.BlockEntity> chestEntities = new java.util.ArrayList<>();

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunk(cx, cz);
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
                                    pos.getY() >= oy - 5 && pos.getY() <= oy + 40) {
                                if (isStorageBlock(be, level)) {
                                    chestEntities.add(be);
                                }
                            }
                        }
                    }
                }
            }
        }

        chestEntities.sort(java.util.Comparator.comparingInt((net.minecraft.world.level.block.entity.BlockEntity be) -> be.getBlockPos().getY())
                .thenComparingInt(be -> be.getBlockPos().getX())
                .thenComparingInt(be -> be.getBlockPos().getZ()));

        for (net.minecraft.world.level.block.entity.BlockEntity be : chestEntities) {
            if (be instanceof Container c) {
                result.add(c);
            } else {
                try {
                    var handler = level.getCapability(Capabilities.ItemHandler.BLOCK,
                            be.getBlockPos(), be.getBlockState(), be, null);
                    if (handler != null && handler.getSlots() > 0) {
                        result.add(new ItemHandlerContainerWrapper(handler, be));
                    }
                } catch (Throwable ignored) {}
            }
        }

        return result;
    }

    public static void reconcileSubspaceStorage(List<Container> containers, SystemData data) {
        net.minecraft.core.NonNullList<ItemStack> items = data.getSubspaceItems();
        int globalIndex = 0;
        for (Container chest : containers) {
            int cSize = chest.getContainerSize();
            boolean changed = false;
            for (int s = 0; s < cSize; s++) {
                ItemStack virtualStack = (globalIndex < items.size()) ? items.get(globalIndex) : ItemStack.EMPTY;
                ItemStack physicalStack = chest.getItem(s);

                if (virtualStack.isEmpty() && !physicalStack.isEmpty()) {
                    if (globalIndex < items.size()) {
                        items.set(globalIndex, physicalStack.copy());
                    }
                } else if (!virtualStack.isEmpty() && physicalStack.isEmpty()) {
                    chest.setItem(s, virtualStack.copy());
                    changed = true;
                } else if (!ItemStack.matches(virtualStack, physicalStack)) {
                    if (globalIndex < items.size()) {
                        items.set(globalIndex, physicalStack.copy());
                    }
                }
                globalIndex++;
            }
            if (changed) {
                chest.setChanged();
            }
        }
    }

    public static void syncWorldToSubspace(ServerLevel level, ServerPlayer player) {
        try {
            int ox = getPlotOriginX(player);
            int oz = getPlotOriginZ(player);
            int oy = getPlotOriginY();

            List<Container> containers = getPlotChestContainers(level, ox, oy, oz);
            int totalPhysicalSlots = 0;
            for (Container c : containers) {
                totalPhysicalSlots += c.getContainerSize();
            }
            int chestCount = Math.max(2, containers.size());

            SystemData data = SystemEvents.data(player);
            int unlockedSlots;
            if (!containers.isEmpty() || totalPhysicalSlots > 0) {
                unlockedSlots = Math.max(54, totalPhysicalSlots);
                data.setSubspaceChestCount(chestCount);
                data.setSubspaceUnlockedSlots(unlockedSlots);
            } else {
                unlockedSlots = Math.max(54, data.subspaceUnlockedSlots());
            }

            data.ensureSubspaceCapacity(Math.max(54, unlockedSlots));

            net.minecraft.core.NonNullList<ItemStack> items = data.getSubspaceItems();
            int globalIndex = 0;
            for (Container chest : containers) {
                int cSize = chest.getContainerSize();
                for (int s = 0; s < cSize; s++) {
                    if (globalIndex < items.size()) {
                        items.set(globalIndex, chest.getItem(s).copy());
                    }
                    globalIndex++;
                }
            }
            while (globalIndex < unlockedSlots && globalIndex < items.size()) {
                items.set(globalIndex, ItemStack.EMPTY);
                globalIndex++;
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

            int globalIndex = 0;
            for (Container chest : containers) {
                int cSize = chest.getContainerSize();
                boolean changed = false;
                for (int s = 0; s < cSize; s++) {
                    ItemStack stack = (globalIndex < items.size()) ? items.get(globalIndex) : ItemStack.EMPTY;
                    if (!ItemStack.matches(chest.getItem(s), stack)) {
                        chest.setItem(s, stack.copy());
                        changed = true;
                    }
                    globalIndex++;
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
            menuData.set(2, 0);
            menuData.set(3, totalPages);

            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("awakening_system.subspace.menu_title", data.subspaceLevel());
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
            player.displayClientMessage(Component.translatable("awakening_system.subspace.msg.already_inside"), true);
            return;
        }

        SystemData data = SystemEvents.data(player);
        data.setSubspaceReturnLocation(
                player.level().dimension().location().toString(),
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot()
        );

        ServerLevel subspaceLevel = getSubspaceLevel(player.getServer());
        if (subspaceLevel == null) {
            player.sendSystemMessage(Component.translatable("awakening_system.subspace.msg.server_error"));
            return;
        }

        int originX = getPlotOriginX(player);
        int originZ = getPlotOriginZ(player);
        int originY = getPlotOriginY();

        player.teleportTo(subspaceLevel, originX + 0.5D, originY + 1.0D, originZ - 2.5D, 0.0F, 0.0F);
        player.fallDistance = 0.0F;

        // Si la estructura construida es inferior al nivel alcanzado o el área está vacía, construirla/actualizarla
        if (data.subspaceBuiltLevel() < data.subspaceLevel()) {
            regenerateSubspaceStructure(player);
            data.setSubspaceBuiltLevel(data.subspaceLevel());
            AwakeningSystemMod.sync(player);
        } else {
            BlockPos centerBelow = new BlockPos(originX, originY - 1, originZ);
            BlockPos centerPos = new BlockPos(originX, originY, originZ);
            if (subspaceLevel.getBlockState(centerBelow).isAir() && subspaceLevel.getBlockState(centerPos).isAir()) {
                ensureSubspaceStructure(subspaceLevel, originX, originY, originZ, data.subspaceLevel());
                data.setSubspaceBuiltLevel(data.subspaceLevel());
                AwakeningSystemMod.sync(player);
            }
        }
        clearBedrockBelow(subspaceLevel, originX, originZ, 20);

        List<Container> containers = getPlotChestContainers(subspaceLevel, originX, originY, originZ);
        int totalPhysicalSlots = 0;
        for (Container c : containers) {
            totalPhysicalSlots += c.getContainerSize();
        }
        if (totalPhysicalSlots > 0 || !containers.isEmpty()) {
            int unlockedSlots = Math.max(54, totalPhysicalSlots);
            data.setSubspaceChestCount(Math.max(2, containers.size()));
            data.setSubspaceUnlockedSlots(unlockedSlots);
            data.ensureSubspaceCapacity(unlockedSlots);
        }
        reconcileSubspaceStorage(containers, data);

        subspaceLevel.sendParticles(ParticleTypes.PORTAL, originX + 0.5D, originY + 1.5D, originZ - 2.5D, 40, 0.5D, 0.8D, 0.5D, 0.1D);
        player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 1.0F, 1.2F);

        int chests = data.subspaceChestCount();
        int slots = data.subspaceUnlockedSlots();
        player.sendSystemMessage(Component.translatable("awakening_system.subspace.msg.entered", data.subspaceLevel()));
        player.sendSystemMessage(Component.translatable("awakening_system.subspace.msg.storage_info", chests, slots));
        player.sendSystemMessage(Component.translatable("awakening_system.subspace.msg.return_hint"));
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
        player.displayClientMessage(Component.translatable("awakening_system.subspace.msg.returned_success"), true);
    }

    public static boolean upgradeSubspace(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        int curLvl = data.subspaceLevel();
        if (curLvl >= 3) {
            player.sendSystemMessage(Component.literal("§e[Subespacio] §bNivel 3§e alcanzado. Es el nivel máximo disponible por ahora (próximamente niveles 4 y 5)."));
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
            player.sendSystemMessage(Component.translatable("awakening_system.subspace.msg.insufficient_funds", nextLvl));
            player.sendSystemMessage(Component.translatable("awakening_system.subspace.msg.required_cost", coinCost));
            player.playNotifySound(ModSounds.SYSTEM_ERROR.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return false;
        }

        data.setSubspaceLevel(nextLvl);
        AwakeningSystemMod.sync(player);
        player.playNotifySound(ModSounds.SYSTEM_LEVEL_UP.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

        if (player.serverLevel().dimension().equals(SUBSPACE_REALM)) {
            // Actualizar la estructura física de la parcela inmediatamente al nuevo nivel
            regenerateSubspaceStructure(player);
            data.setSubspaceBuiltLevel(nextLvl);
            AwakeningSystemMod.sync(player);
        } else {
            data.setSubspaceBuiltLevel(0);
        }

        player.sendSystemMessage(Component.translatable("awakening_system.subspace.msg.ascended", nextLvl));
        player.sendSystemMessage(Component.literal("§a[Subespacio] ¡Tu parcela ha sido mejorada al diseño oficial de Nivel " + nextLvl + "!"));
        return true;
    }

    public static void clearBedrockBelow(ServerLevel level, int ox, int oz, int radius) {
        try {
            BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
            int minY = level.getMinBuildHeight();
            for (int x = ox - radius; x <= ox + radius; x++) {
                for (int z = oz - radius; z <= oz + radius; z++) {
                    for (int y = minY; y <= 0; y++) {
                        mpos.set(x, y, z);
                        if (level.getBlockState(mpos).is(Blocks.BEDROCK)) {
                            level.setBlock(mpos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public static boolean placeSubspaceNbt(ServerLevel serverLevel, BlockPos center, String name, int offsetX, int offsetY, int offsetZ) {
        try {
            StructureTemplate template = null;
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, name);
            StructureTemplateManager stm = serverLevel.getStructureManager();
            Optional<StructureTemplate> opt = stm.get(id);

            if (opt.isPresent()) {
                template = opt.get();
            } else {
                String[] paths = {
                        "/data/awakening_system/structure/" + name + ".nbt",
                        "/data/awakening_system/structures/" + name + ".nbt",
                        "data/awakening_system/structure/" + name + ".nbt",
                        "data/awakening_system/structures/" + name + ".nbt"
                };
                for (String p : paths) {
                    try (InputStream in = SubspaceManager.class.getResourceAsStream(p)) {
                        if (in != null) {
                            CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                            template = new StructureTemplate();
                            template.load(serverLevel.holderLookup(Registries.BLOCK), tag);
                            break;
                        }
                    } catch (Exception ignored) {}
                    if (template == null) {
                        try (InputStream in = SubspaceManager.class.getClassLoader().getResourceAsStream(p.startsWith("/") ? p.substring(1) : p)) {
                            if (in != null) {
                                CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                                template = new StructureTemplate();
                                template.load(serverLevel.holderLookup(Registries.BLOCK), tag);
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

            if (template != null) {
                BlockPos origin = center.offset(offsetX, offsetY, offsetZ);
                StructurePlaceSettings settings = new StructurePlaceSettings()
                        .setIgnoreEntities(false)
                        .setKnownShape(true)
                        .setMirror(Mirror.NONE)
                        .setRotation(Rotation.NONE);

                template.placeInWorld(serverLevel, origin, origin, settings, serverLevel.getRandom(), 2);
                return true;
            }
        } catch (Throwable t) {
            AwakeningSystemMod.LOGGER.error("Error al colocar estructura NBT de subespacio: " + name, t);
        }
        return false;
    }

    public static void requestUpdateStructure(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        int lvl = data.subspaceLevel();
        player.sendSystemMessage(Component.literal("§e========================================"));
        player.sendSystemMessage(Component.literal("§c§l[ADVERTENCIA: REGENERACIÓN DE SUBESPACIO]"));
        player.sendSystemMessage(Component.literal("§eEsta acción limpiará tu parcela y regenerará el nuevo diseño oficial de §bNivel " + lvl + "§e."));
        player.sendSystemMessage(Component.literal("§c¡PELIGRO! §fCualquier cofre, construcción o ítem en tu parcela actual será §c§lSOBRESCRITO§f."));
        player.sendSystemMessage(Component.literal("§6Por favor, retira todos tus cofres y objetos valiosos antes de continuar."));
        player.sendSystemMessage(Component.literal("§aSi estás listo y deseas proceder, ejecuta: §b/subspace update_structure confirm"));
        player.sendSystemMessage(Component.literal("§e========================================"));
    }

    public static void regenerateSubspaceStructure(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(SUBSPACE_REALM)) {
            player.sendSystemMessage(Component.literal("§cDebes estar dentro de tu subespacio para regenerar la estructura."));
            return;
        }

        SystemData data = SystemEvents.data(player);
        int lvl = data.subspaceLevel();
        int ox = getPlotOriginX(player);
        int oz = getPlotOriginZ(player);
        int oy = getPlotOriginY();
        ServerLevel level = player.serverLevel();

        // 1. Limpieza segura de la parcela previa
        int cleanRadius = 16;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int dx = -cleanRadius; dx <= cleanRadius; dx++) {
            for (int dz = -cleanRadius; dz <= cleanRadius; dz++) {
                for (int y = oy - 4; y <= oy + 22; y++) {
                    mpos.set(ox + dx, y, oz + dz);
                    if (!level.getBlockState(mpos).isAir()) {
                        level.setBlock(mpos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
        clearBedrockBelow(level, ox, oz, 20);

        // 2. Colocar la nueva estructura del nivel correspondiente
        ensureSubspaceStructure(level, ox, oy, oz, lvl);

        // 3. Teletransportar al jugador a una posición segura frente al waypoint
        player.teleportTo(level, ox + 0.5D, oy + 1.0D, oz - 2.5D, 0.0F, 0.0F);
        player.fallDistance = 0.0F;

        // 4. Sincronizar contenedores nuevos
        List<Container> containers = getPlotChestContainers(level, ox, oy, oz);
        reconcileSubspaceStorage(containers, data);
        syncWorldToSubspace(level, player);

        // 5. Notificación y efectos
        level.sendParticles(ParticleTypes.END_ROD, ox + 0.5D, oy + 2.0D, oz + 0.5D, 80, 2.0D, 1.5D, 2.0D, 0.1D);
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.literal("§a§l[Subespacio] ¡Tu subespacio ha sido regenerado con éxito al nuevo diseño oficial de Nivel " + lvl + "!"));
        AwakeningSystemMod.sync(player);
    }

    public static void ensureSubspaceStructure(ServerLevel level, int ox, int oy, int oz, int subLvl) {
        clearBedrockBelow(level, ox, oz, 20);

        String templateName = switch (subLvl) {
            case 1 -> "subspace_level_1";
            case 2 -> "subspace_level_2";
            default -> "subspace_level_3";
        };

        // Desfases calculados para alinear el waypoint exactamente en (ox, oy, oz)
        int offsetX = switch (subLvl) {
            case 1 -> -6;
            case 2 -> -8;
            default -> -12;
        };
        int offsetY = switch (subLvl) {
            case 1 -> -1;
            case 2 -> -2;
            default -> -2;
        };
        int offsetZ = switch (subLvl) {
            case 1 -> -6;
            case 2 -> -9;
            default -> -12;
        };

        boolean placed = placeSubspaceNbt(level, new BlockPos(ox, oy, oz), templateName, offsetX, offsetY, offsetZ);
        if (!placed) {
            placeProceduralSubspaceFallback(level, ox, oy, oz, subLvl);
        }
    }

    public static void placeProceduralSubspaceFallback(ServerLevel level, int ox, int oy, int oz, int subLvl) {
        int radius = switch (subLvl) {
            case 1 -> 5;
            case 2 -> 9;
            case 3 -> 14;
            case 4 -> 19;
            default -> 26;
        };

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

        BlockPos centerPos = new BlockPos(ox, oy, oz);
        BlockPos belowCenter = new BlockPos(ox, oy - 1, oz);
        level.setBlockAndUpdate(belowCenter, Blocks.GILDED_BLACKSTONE.defaultBlockState());
        if (level.getBlockState(centerPos).isAir()) {
            level.setBlockAndUpdate(centerPos, SystemBlocks.CELESTIAL_WAYPOINT.get().defaultBlockState());
        }

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
    }
}
