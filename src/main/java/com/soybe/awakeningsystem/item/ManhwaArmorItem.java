package com.soybe.awakeningsystem.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ManhwaArmorItem extends ArmorItem {
    public enum SetType {
        ABYSSAL("abyssal", "§9§l[CONJUNTO: RESONANCIA DEL ABISMO PRIMORDIAL]",
                "§74 Piezas: §9+20% Resistencia en cavernas, visión nocturna pasiva bajo tierra. [Z] Vórtice Abisal (Colapso de gravedad)."),
        MITHRIL("mithril", "§b§l[CONJUNTO: GRACIA ÉLFICA DE MITHRIL]",
                "§74 Piezas: §b+25% Velocidad pasiva, caída suave al agacharse en el aire. [Z] Parpadeo Dimensional (Dash sónico)."),
        ADAMANTITE("adamantite", "§c§l[CONJUNTO: FURIA VOLCÁNICA DE ADAMANTITA]",
                "§74 Piezas: §cInmunidad total a fuego y lava, +60% daño a baja vida. [Z] Fisura de Magma (Erupción volcánica)."),
        SPIRIT_JADE("spirit_jade", "§a§l[CONJUNTO: CUERPO INMORTAL DE JADE ESPIRITUAL]",
                "§74 Piezas: §a25% daño recibido convertido en Qi, purificación del Dao. [Z] Campana Dorada (Invulnerabilidad y reflejo)."),
        ASTRAL("astral", "§5§l[CONJUNTO: SOBERANÍA DEL DRAGÓN CELESTIAL]",
                "§74 Piezas: §5Vuelo cósmico libre, rescate del vacío. [Z] Estallido Astral (Supernova de Dragón Celestial)."),
        MORA("mora", "§e§l[CONJUNTO: BARRERA ÁUREA DE MORA]",
                "§74 Piezas: §eEscudo del Dios del Comercio. El daño letal o crítico es absorbido automáticamente gastando Monedas/Mora del inventario para salvar tu vida.");

        public final String id;
        public final String header;
        public final String bonusDesc;

        SetType(String id, String header, String bonusDesc) {
            this.id = id;
            this.header = header;
            this.bonusDesc = bonusDesc;
        }
    }

    private final SetType setType;

    public ManhwaArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, SetType setType) {
        super(material, type, properties);
        this.setType = setType;
    }

    public SetType getSetType() {
        return setType;
    }

    @Override
    public net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean inner) {
        String layerNum = (slot == EquipmentSlot.LEGS || inner) ? "2" : "1";
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/models/armor/" + setType.id + "_layer_" + layerNum + ".png");
    }

    public static boolean hasFullSet(Player player, SetType set) {
        if (player == null) return false;
        int count = 0;
        int required = 4;

        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ManhwaArmorItem mai && mai.getSetType() == set) {
                count++;
            }
        }
        return count >= required;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.armor_set." + setType.id + ".header"));
        tooltip.add(Component.translatable("awakening_system.armor_set." + setType.id + ".desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
