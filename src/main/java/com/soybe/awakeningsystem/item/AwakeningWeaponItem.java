package com.soybe.awakeningsystem.item;

import net.minecraft.world.item.ItemStack;

/**
 * Marker interface for all Awakening System unique manhwa weapons with special active skills.
 */
public interface AwakeningWeaponItem {
    static boolean isAwakeningWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof AwakeningWeaponItem;
    }
}
