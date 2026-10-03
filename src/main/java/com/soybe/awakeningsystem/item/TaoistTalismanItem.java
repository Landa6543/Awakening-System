package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.entity.JiangshiEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Talismán Taoísta de Sellado de Cinabrio (Taoist Sealing Talisman / 镇尸符).
 * Reliquia taoísta tradicional de Murim con inscripciones en tinta bermellón:
 * - Se adhiere a la frente de un Jiangshi hostil con clic derecho.
 * - Extingue cualquier estado de fuego/frenesí y paraliza por completo al muerto viviente durante 10 segundos.
 */
public class TaoistTalismanItem extends Item {

    public TaoistTalismanItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof JiangshiEntity jiangshi) {
            return jiangshi.mobInteract(player, hand);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.taoist_talisman.tooltip_lore"));
        tooltip.add(Component.translatable("item.awakening_system.taoist_talisman.tooltip_desc"));
    }
}
