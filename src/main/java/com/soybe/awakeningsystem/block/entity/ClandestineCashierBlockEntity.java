package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClandestineCashierBlockEntity extends BlockEntity {

    private UUID ownerUUID = null;
    private String ownerName = "";
    private int stockChips = 0;
    private long collectedMora = 0L;
    private int totalChipsSold = 0;

    public ClandestineCashierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CLANDESTINE_CASHIER.get(), pos, state);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        return ownerName != null && !ownerName.isEmpty() ? ownerName : "Desconocido";
    }

    public void setOwner(Player player) {
        if (player != null) {
            this.ownerUUID = player.getUUID();
            this.ownerName = player.getName().getString();
            setChanged();
        }
    }

    public boolean isOwner(@Nullable Player player) {
        if (player == null) return false;
        if (player.isCreative() || player.hasPermissions(2)) return true;
        if (this.ownerUUID == null) return true;
        return this.ownerUUID.equals(player.getUUID());
    }

    public int getStockChips() {
        return stockChips;
    }

    public void setStockChips(int stockChips) {
        this.stockChips = Math.max(0, stockChips);
        setChanged();
    }

    public long getCollectedMora() {
        return collectedMora;
    }

    public void setCollectedMora(long collectedMora) {
        this.collectedMora = Math.max(0L, collectedMora);
        setChanged();
    }

    public void addCollectedMora(long amount) {
        if (amount > 0) {
            this.collectedMora += amount;
            setChanged();
        }
    }

    public int getTotalChipsSold() {
        return totalChipsSold;
    }

    public void addTotalChipsSold(int amount) {
        if (amount > 0) {
            this.totalChipsSold += amount;
            setChanged();
        }
    }

    public static int countBlackChips(Player player) {
        if (player == null) return 0;
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(SystemItems.CASINO_CHIP_BLACK.get())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public int depositStock(Player player, int amount) {
        int available = countBlackChips(player);
        if (available <= 0) return 0;
        int toDeposit = amount <= 0 ? available : Math.min(amount, available);

        int remaining = toDeposit;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(SystemItems.CASINO_CHIP_BLACK.get())) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }

        this.stockChips += toDeposit;
        setChanged();
        return toDeposit;
    }

    public int withdrawStock(Player player, int amount) {
        if (this.stockChips <= 0) return 0;
        int toWithdraw = amount <= 0 ? this.stockChips : Math.min(amount, this.stockChips);

        this.stockChips -= toWithdraw;
        setChanged();

        ItemStack stack = new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), toWithdraw);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        return toWithdraw;
    }

    public long withdrawCollectedMora(ServerPlayer player) {
        long mora = this.collectedMora;
        if (mora > 0) {
            SystemEvents.data(player).addMora((int) mora);
            this.collectedMora = 0L;
            setChanged();
            AwakeningSystemMod.sync(player);
        }
        return mora;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
        tag.putString("OwnerName", this.ownerName != null ? this.ownerName : "");
        tag.putInt("StockChips", this.stockChips);
        tag.putLong("CollectedMora", this.collectedMora);
        tag.putInt("TotalChipsSold", this.totalChipsSold);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        } else {
            this.ownerUUID = null;
        }
        this.ownerName = tag.getString("OwnerName");
        this.stockChips = tag.getInt("StockChips");
        this.collectedMora = tag.getLong("CollectedMora");
        this.totalChipsSold = tag.getInt("TotalChipsSold");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
