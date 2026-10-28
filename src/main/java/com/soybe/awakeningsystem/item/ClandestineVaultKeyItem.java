package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Llave Maestra de Bóveda Clandestina.
 * Permite al dueño legítimo desbloquear y administrar el compartimento de reserva y recaudación
 * de su máquina tragamonedas clandestina del bajo mundo.
 */
public class ClandestineVaultKeyItem extends Item {

    public ClandestineVaultKeyItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    /**
     * Comprueba si el jugador porta la llave en sus manos o en cualquier ranura de su inventario.
     */
    public static boolean hasKey(Player player) {
        if (player == null) return false;
        if (player.isCreative()) return true;

        if (player.getMainHandItem().is(SystemItems.CLANDESTINE_VAULT_KEY.get()) ||
                player.getOffhandItem().is(SystemItems.CLANDESTINE_VAULT_KEY.get())) {
            return true;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(SystemItems.CLANDESTINE_VAULT_KEY.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.title"));
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.desc"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.use_title"));
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.use_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.use_desc2"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.hint").withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
