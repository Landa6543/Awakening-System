package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.sanctuary.PocketSanctuaryManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Ítem de Invocación de la Puerta a Dimensión de Bolsillo (Sección 29).
 * Permite materializar en cualquier momento la puerta física tridimensional
 * para acceder al Santuario personal privado.
 */
public class PocketSanctuaryDoorItem extends Item {

    public PocketSanctuaryDoorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(held, true);
        }

        if (player instanceof ServerPlayer sp) {
            if (PocketSanctuaryManager.isSanctuaryDimension(level)) {
                sp.displayClientMessage(Component.literal("§eYa estás dentro de tu dimensión de bolsillo. Utiliza la puerta interior para regresar."), true);
                return InteractionResultHolder.fail(held);
            }

            if (PocketSanctuaryManager.isDimensionSealed(sp)) {
                sp.displayClientMessage(Component.literal("§c§l[SANCTUARIO SELLADO] §7Las leyes espaciales de esta dimensión impiden abrir la puerta de bolsillo."), true);
                return InteractionResultHolder.fail(held);
            }

            var door = PocketSanctuaryManager.summonExteriorDoor(sp);
            if (door != null) {
                return InteractionResultHolder.sidedSuccess(held, false);
            }
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6§lHabilidad de Refugio Dimensional §7(Sección 29)"));
        tooltip.add(Component.literal("§fInvoca una §bpuerta física tridimensional§f frente a ti."));
        tooltip.add(Component.literal("§7• Haz clic derecho para abrirla y cruzar al Santuario."));
        tooltip.add(Component.literal("§7• Al cerrarse desde dentro, la puerta exterior §ndesaparece del mundo§7."));
        tooltip.add(Component.literal("§7• Dimensión 100% privada, pacífica y libre de monstruos."));
        tooltip.add(Component.literal("§8Shift + Clic derecho sobre la puerta exterior para retirarla."));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
