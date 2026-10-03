package com.soybe.awakeningsystem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record GachaWishResultPayload(
        int highestRarity,
        List<ItemStack> wonItems,
        List<Integer> itemRarities,
        int newPity5,
        boolean newGuaranteed,
        int newPrimos,
        int newIntertwined,
        int newAcquaint,
        int newStardust,
        int newStarglitter,
        int newBeginnerPulls,
        int newMora,
        int newWeaponTarget,
        int newWeaponFatePoints,
        int newCharTarget,
        int newTotalWishes
) implements CustomPacketPayload {
    public static final Type<GachaWishResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gacha_wish_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GachaWishResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.highestRarity);
                buf.writeVarInt(p.wonItems.size());
                for (ItemStack item : p.wonItems) {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, item);
                }
                buf.writeVarInt(p.itemRarities.size());
                for (int r : p.itemRarities) {
                    buf.writeVarInt(r);
                }
                buf.writeVarInt(p.newPity5);
                buf.writeBoolean(p.newGuaranteed);
                buf.writeVarInt(p.newPrimos);
                buf.writeVarInt(p.newIntertwined);
                buf.writeVarInt(p.newAcquaint);
                buf.writeVarInt(p.newStardust);
                buf.writeVarInt(p.newStarglitter);
                buf.writeVarInt(p.newBeginnerPulls);
                buf.writeVarInt(p.newMora);
                buf.writeVarInt(p.newWeaponTarget);
                buf.writeVarInt(p.newWeaponFatePoints);
                buf.writeVarInt(p.newCharTarget);
                buf.writeVarInt(p.newTotalWishes);
            },
            buf -> {
                int highest = buf.readVarInt();
                int size = buf.readVarInt();
                List<ItemStack> items = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
                }
                int rSize = buf.readVarInt();
                List<Integer> rarities = new ArrayList<>(rSize);
                for (int i = 0; i < rSize; i++) {
                    rarities.add(buf.readVarInt());
                }
                int pity = buf.readVarInt();
                boolean guar = buf.readBoolean();
                int primos = buf.readVarInt();
                int intertwined = buf.readVarInt();
                int acquaint = buf.readVarInt();
                int stardust = buf.readVarInt();
                int starglitter = buf.readVarInt();
                int beginner = buf.readVarInt();
                int mora = buf.readVarInt();
                int weaponTarget = buf.readVarInt();
                int weaponFate = buf.readVarInt();
                int charTarget = buf.readVarInt();
                int totalWishes = buf.readVarInt();
                return new GachaWishResultPayload(highest, items, rarities, pity, guar, primos, intertwined, acquaint, stardust, starglitter, beginner, mora, weaponTarget, weaponFate, charTarget, totalWishes);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
