package com.soybe.awakeningsystem.tower;

import net.minecraft.world.item.ItemStack;

/**
 * Plantilla inmutable para una misión diaria de la Torre del Ranker.
 * Cada misión está ligada estrictamente a un piso objetivo específico, con narrativa,
 * objetivos y recompensas completas (monedas, experiencia, primogemas y materiales).
 */
public record TowerMissionTemplate(
        int id,
        int floor,
        String title,
        String narrative,
        String targetType,
        String targetName,
        int targetCount,
        int rewardCoins,
        int rewardExp,
        int rewardGems,
        ItemStack rewardMaterial,
        String rewardMaterialName
) {
    public boolean isCollection() {
        return targetType != null && targetType.startsWith("collect_");
    }

    /** How much more the higher floors pay: x1 up to floor 10, then growing, about x49 on floor 100. */
    private double payScale() {
        double k = 1.0D + Math.max(0, floor - 10) / 15.0D;
        return k * k;
    }

    public int coins() {
        return (int) Math.min(Integer.MAX_VALUE, Math.round(rewardCoins * payScale()));
    }

    public int exp() {
        return (int) Math.min(Integer.MAX_VALUE, Math.round(rewardExp * payScale()));
    }

    /** The mana crystal shards of the floor's rank, paid in place of primogems (those belong to Teyvat). */
    public ItemStack crystalReward() {
        String rank = floor <= 10 ? "e" : floor <= 20 ? "d" : floor <= 40 ? "c" : floor <= 60 ? "b" : floor <= 80 ? "a" : floor < 100 ? "s" : "ex";
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("awakening_system", "mana_crystal_shard_" + rank));
        return new ItemStack(item, Math.max(1, Math.min(64, rewardGems / 2 + 1)));
    }

    /** A collection contract does not pay back in its own kind of block; only hunts add a material. */
    public ItemStack materialReward() {
        return isCollection() || rewardMaterial == null ? ItemStack.EMPTY : rewardMaterial;
    }
}
