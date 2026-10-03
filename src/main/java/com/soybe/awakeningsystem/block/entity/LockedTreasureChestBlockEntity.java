package com.soybe.awakeningsystem.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Entidad de Cofre Dimensional con soporte de Loot Instanciado por Jugador (estilo Lootr).
 * Cada jugador desbloquea el cofre con su propia llave y recibe un inventario personal persistente.
 */
public class LockedTreasureChestBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final Set<UUID> unlockedPlayers = new HashSet<>();
    private final Map<UUID, NonNullList<ItemStack>> playerInventories = new HashMap<>();

    private float openness;
    private float prevOpenness;
    private int openersCounter;

    public LockedTreasureChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOCKED_TREASURE_CHEST.get(), pos, state);
    }

    public boolean isUnlockedFor(UUID playerUuid) {
        if (playerUuid == null) return false;
        return unlockedPlayers.contains(playerUuid);
    }

    public void unlockFor(UUID playerUuid) {
        if (playerUuid != null) {
            unlockedPlayers.add(playerUuid);
            setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    public NonNullList<ItemStack> getOrCreatePlayerInventory(UUID playerUuid) {
        if (playerUuid == null) return this.items;
        return playerInventories.computeIfAbsent(playerUuid, u -> NonNullList.withSize(27, ItemStack.EMPTY));
    }

    public MenuProvider getMenuProviderFor(ServerPlayer player) {
        final UUID uuid = player.getUUID();
        final NonNullList<ItemStack> pItems = getOrCreatePlayerInventory(uuid);

        SimpleContainer container = new SimpleContainer(pItems.toArray(new ItemStack[0])) {
            @Override
            public void setChanged() {
                super.setChanged();
                for (int i = 0; i < 27; i++) {
                    pItems.set(i, this.getItem(i));
                }
                LockedTreasureChestBlockEntity.this.setChanged();
            }

            @Override
            public void startOpen(Player p) {
                LockedTreasureChestBlockEntity.this.startOpen(p);
            }

            @Override
            public void stopOpen(Player p) {
                LockedTreasureChestBlockEntity.this.stopOpen(p);
            }
        };

        return new SimpleMenuProvider((id, inv, p) -> ChestMenu.threeRows(id, inv, container), getDefaultName());
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter++;
            if (this.level != null) {
                this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openersCounter);
            }
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter = Math.max(0, this.openersCounter - 1);
            if (this.level != null) {
                this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openersCounter);
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.openersCounter = type;
            return true;
        }
        return super.triggerEvent(id, type);
    }

    public static void lidTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, LockedTreasureChestBlockEntity be) {
        be.prevOpenness = be.openness;
        if (be.openersCounter > 0 && be.openness < 1.0F) {
            be.openness = Math.min(1.0F, be.openness + 0.15F);
        } else if (be.openersCounter <= 0 && be.openness > 0.0F) {
            be.openness = Math.max(0.0F, be.openness - 0.15F);
        }
    }

    public float getOpenness(float partialTick) {
        return net.minecraft.util.Mth.lerp(partialTick, this.prevOpenness, this.openness);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.awakening_system.locked_treasure_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        ListTag unlockedList = new ListTag();
        for (UUID u : unlockedPlayers) {
            unlockedList.add(StringTag.valueOf(u.toString()));
        }
        tag.put("UnlockedPlayers", unlockedList);
        return tag;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }

        unlockedPlayers.clear();
        if (tag.contains("UnlockedPlayers", 9)) {
            ListTag list = tag.getList("UnlockedPlayers", 8);
            for (int i = 0; i < list.size(); i++) {
                try {
                    unlockedPlayers.add(UUID.fromString(list.getString(i)));
                } catch (Exception ignored) {}
            }
        }

        playerInventories.clear();
        if (tag.contains("PlayerInventories", 10)) {
            CompoundTag invs = tag.getCompound("PlayerInventories");
            for (String key : invs.getAllKeys()) {
                try {
                    UUID u = UUID.fromString(key);
                    NonNullList<ItemStack> pList = NonNullList.withSize(27, ItemStack.EMPTY);
                    ContainerHelper.loadAllItems(invs.getCompound(key), pList, registries);
                    playerInventories.put(u, pList);
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }

        ListTag unlockedList = new ListTag();
        for (UUID u : unlockedPlayers) {
            unlockedList.add(StringTag.valueOf(u.toString()));
        }
        tag.put("UnlockedPlayers", unlockedList);

        CompoundTag invs = new CompoundTag();
        for (Map.Entry<UUID, NonNullList<ItemStack>> entry : playerInventories.entrySet()) {
            CompoundTag pTag = new CompoundTag();
            ContainerHelper.saveAllItems(pTag, entry.getValue(), registries);
            invs.put(entry.getKey().toString(), pTag);
        }
        tag.put("PlayerInventories", invs);
    }
}
