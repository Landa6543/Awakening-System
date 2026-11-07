package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.world.MeteorImpactData;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Brújula forjada con esquirlas de meteorito: su aguja señala el último meteorito caído.
 * Guarda el destino en el mismo dato que usan las brújulas de magnetita, así el cliente
 * puede calcular el ángulo igual que una brújula normal.
 */
public class MeteorCompassItem extends Item {

    public MeteorCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(level instanceof ServerLevel serverLevel) || serverLevel.getGameTime() % 20 != 0) return;

        GlobalPos impact = MeteorImpactData.get(serverLevel.getServer()).lastImpact();
        LodestoneTracker current = stack.get(DataComponents.LODESTONE_TRACKER);
        GlobalPos currentTarget = current != null ? current.target().orElse(null) : null;
        if (Objects.equals(impact, currentTarget)) return;

        if (impact == null) {
            stack.remove(DataComponents.LODESTONE_TRACKER);
        } else {
            // tracked = false: no depende de que haya un bloque de magnetita en ese punto
            stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(impact), false));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        boolean hasTarget = stack.get(DataComponents.LODESTONE_TRACKER) != null;
        tooltip.add(Component.translatable(hasTarget
                ? "item.awakening_system.meteor_compass.tooltip"
                : "item.awakening_system.meteor_compass.tooltip_none"));
    }
}
