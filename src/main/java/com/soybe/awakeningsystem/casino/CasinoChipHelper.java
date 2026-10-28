package com.soybe.awakeningsystem.casino;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.item.CasinoChipItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public final class CasinoChipHelper {
    private CasinoChipHelper() {}

    public static Item getItemForTier(CasinoChipItem.ChipTier tier) {
        return switch (tier) {
            case BRONZE -> SystemItems.CASINO_CHIP_BRONZE.get();
            case SILVER -> SystemItems.CASINO_CHIP_SILVER.get();
            case GOLD -> SystemItems.CASINO_CHIP_GOLD.get();
            case PLATINUM -> SystemItems.CASINO_CHIP_PLATINUM.get();
            case BLACK -> SystemItems.CASINO_CHIP_BLACK.get();
        };
    }

    public static int countChips(Player player, CasinoChipItem.ChipTier tier) {
        if (player == null) return 0;
        int count = 0;
        Item target = getItemForTier(tier);
        for (ItemStack st : player.getInventory().items) {
            if (!st.isEmpty() && st.getItem() == target) {
                count += st.getCount();
            }
        }
        return count;
    }

    public static long getTotalChipValue(Player player, boolean isIllegal) {
        if (player == null) return 0L;
        if (isIllegal) {
            return (long) countChips(player, CasinoChipItem.ChipTier.BLACK) * CasinoChipItem.ChipTier.BLACK.getMoraValue();
        }
        long sum = 0L;
        sum += (long) countChips(player, CasinoChipItem.ChipTier.BRONZE) * CasinoChipItem.ChipTier.BRONZE.getMoraValue();
        sum += (long) countChips(player, CasinoChipItem.ChipTier.SILVER) * CasinoChipItem.ChipTier.SILVER.getMoraValue();
        sum += (long) countChips(player, CasinoChipItem.ChipTier.GOLD) * CasinoChipItem.ChipTier.GOLD.getMoraValue();
        sum += (long) countChips(player, CasinoChipItem.ChipTier.PLATINUM) * CasinoChipItem.ChipTier.PLATINUM.getMoraValue();
        return sum;
    }

    public static boolean hasEnoughChips(Player player, long value, boolean isIllegal) {
        return getTotalChipValue(player, isIllegal) >= value;
    }

    public static boolean consumeChipValue(Player player, long value, boolean isIllegal) {
        if (player == null || value <= 0) return true;
        if (!hasEnoughChips(player, value, isIllegal)) return false;

        if (isIllegal) {
            long blackVal = CasinoChipItem.ChipTier.BLACK.getMoraValue();
            int neededBlack = (int) Math.ceil((double) value / blackVal);
            if (!removeChips(player, CasinoChipItem.ChipTier.BLACK, neededBlack)) {
                return false;
            }
            long overpay = ((long) neededBlack * blackVal) - value;
            if (overpay > 0) {
                giveChips(player, overpay, true);
            }
            return true;
        }

        // Legal chips: try greedy from highest to lowest
        CasinoChipItem.ChipTier[] tiers = {
                CasinoChipItem.ChipTier.PLATINUM,
                CasinoChipItem.ChipTier.GOLD,
                CasinoChipItem.ChipTier.SILVER,
                CasinoChipItem.ChipTier.BRONZE
        };

        long remaining = value;
        Map<CasinoChipItem.ChipTier, Integer> toRemove = new EnumMap<>(CasinoChipItem.ChipTier.class);

        for (CasinoChipItem.ChipTier tier : tiers) {
            long val = tier.getMoraValue();
            if (remaining >= val) {
                int available = countChips(player, tier);
                int take = (int) Math.min((long) available, remaining / val);
                if (take > 0) {
                    toRemove.put(tier, take);
                    remaining -= (long) take * val;
                }
            }
        }

        if (remaining == 0) {
            for (Map.Entry<CasinoChipItem.ChipTier, Integer> e : toRemove.entrySet()) {
                removeChips(player, e.getKey(), e.getValue());
            }
            return true;
        }

        // If we still have remaining, we need to break one larger chip from the lowest tier that exceeds remaining
        CasinoChipItem.ChipTier breakerTier = null;
        for (int i = tiers.length - 1; i >= 0; i--) {
            CasinoChipItem.ChipTier t = tiers[i];
            int alreadyTaken = toRemove.getOrDefault(t, 0);
            int available = countChips(player, t) - alreadyTaken;
            if (available > 0 && t.getMoraValue() >= remaining) {
                breakerTier = t;
                break;
            }
        }

        // If not found in simple reverse search, search from highest
        if (breakerTier == null) {
            for (CasinoChipItem.ChipTier t : tiers) {
                int alreadyTaken = toRemove.getOrDefault(t, 0);
                int available = countChips(player, t) - alreadyTaken;
                if (available > 0 && t.getMoraValue() >= remaining) {
                    breakerTier = t;
                }
            }
        }

        if (breakerTier != null) {
            // Apply all planned removals
            for (Map.Entry<CasinoChipItem.ChipTier, Integer> e : toRemove.entrySet()) {
                removeChips(player, e.getKey(), e.getValue());
            }
            // Remove the breaker chip
            removeChips(player, breakerTier, 1);
            long change = breakerTier.getMoraValue() - remaining;
            if (change > 0) {
                giveChips(player, change, false);
            }
            return true;
        }

        return false;
    }

    public static boolean removeChips(Player player, CasinoChipItem.ChipTier tier, int count) {
        if (player == null || count <= 0) return true;
        Item target = getItemForTier(tier);
        int needed = count;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (!st.isEmpty() && st.getItem() == target) {
                int take = Math.min(st.getCount(), needed);
                st.shrink(take);
                needed -= take;
                if (needed <= 0) return true;
            }
        }
        return needed <= 0;
    }

    public static void giveChips(Player player, long value, boolean isIllegal) {
        if (player == null || value <= 0) return;

        if (isIllegal) {
            long blackVal = CasinoChipItem.ChipTier.BLACK.getMoraValue();
            int count = (int) (value / blackVal);
            if (count > 0) {
                addStackOrDrop(player, new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), count));
            }
            return;
        }

        CasinoChipItem.ChipTier[] tiers = {
                CasinoChipItem.ChipTier.PLATINUM,
                CasinoChipItem.ChipTier.GOLD,
                CasinoChipItem.ChipTier.SILVER,
                CasinoChipItem.ChipTier.BRONZE
        };

        long rem = value;
        for (CasinoChipItem.ChipTier tier : tiers) {
            long val = tier.getMoraValue();
            if (rem >= val) {
                int count = (int) (rem / val);
                if (count > 0) {
                    addStackOrDrop(player, new ItemStack(getItemForTier(tier), count));
                    rem -= (long) count * val;
                }
            }
        }
    }

    private static void addStackOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty() || player == null) return;
        while (!stack.isEmpty()) {
            int toAdd = Math.min(stack.getCount(), stack.getMaxStackSize());
            ItemStack split = stack.split(toAdd);
            if (!player.getInventory().add(split)) {
                player.drop(split, false);
            }
        }
    }
}
