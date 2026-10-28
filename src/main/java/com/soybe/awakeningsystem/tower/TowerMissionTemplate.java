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
}
