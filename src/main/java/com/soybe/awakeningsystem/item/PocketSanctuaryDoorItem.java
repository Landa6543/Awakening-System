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
                sp.displayClientMessage(Component.translatable("awakening_system.msg.ya_estas_dentro_de_tu_dimension"), true);
                return InteractionResultHolder.fail(held);
            }

            if (PocketSanctuaryManager.isDimensionSealed(sp)) {
                sp.displayClientMessage(Component.translatable("awakening_system.msg.sanctuario_sellado_las_leyes_espaciales_de"), true);
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
        tooltip.add(Component.translatable("awakening_system.msg.habilidad_de_refugio_dimensional_seccion_29"));
        tooltip.add(Component.translatable("awakening_system.msg.invoca_una_puerta_fisica_tridimensional_frente"));
        tooltip.add(Component.translatable("awakening_system.msg.haz_clic_derecho_para_abrirla_cruzar"));
        tooltip.add(Component.translatable("awakening_system.msg.al_cerrarse_desde_dentro_la_puerta"));
        tooltip.add(Component.translatable("awakening_system.msg.dimension_100_privada_pacifica_libre_de"));
        tooltip.add(Component.translatable("awakening_system.msg.shift_clic_derecho_sobre_la_puerta"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
