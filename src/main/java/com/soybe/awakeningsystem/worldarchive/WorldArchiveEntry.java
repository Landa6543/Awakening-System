package com.soybe.awakeningsystem.worldarchive;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Predicate;

/**
 * Ficha de un mundo en el Archivo del Sistema: los mismos mundos que las pestañas del costado
 * de la ventana del Sistema, con lo que se puede encontrar en cada uno.
 */
public record WorldArchiveEntry(
        String id,
        Component name,
        Component subtitle,
        Component description,
        ResourceLocation iconTexture,
        Predicate<Player> unlockCondition,
        Component unlockConditionText,
        List<Component> keyStructures,
        List<Component> keyBosses,
        List<Component> keyMaterials
) {
    public boolean isUnlocked(Player player) {
        if (player == null) return false;
        if (unlockCondition == null) return true;
        return unlockCondition.test(player);
    }
}
