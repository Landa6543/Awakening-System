package com.soybe.awakeningsystem.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ManhwaArtifactItem extends Item {
    public enum ArtifactType {
        DEMON_MONARCH_RING(
                "§6§l[ANILLO DEL MONARCA DEMONIO BARAN]",
                "§7En Inventario: §e+20% Energía Máxima (EP) y ataques desatan chispas eléctricas (§6+4 de daño de choque§e)."
        ),
        ASRAHAN_MANA_CHALICE(
                "§b§l[CÁLIZ DE MANÁ DE ASRAHAN]",
                "§7En Inventario: §bRegenera 2 EP cada 3 segundos y extingue el fuego sobre el portador automáticamente."
        ),
        KING_SLAYER_NECKLACE(
                "§c§l[COLLAR DEL ASESINO DE REYES]",
                "§7En Inventario: §4+25% Daño directo contra Jefes de Calabozo y criaturas de más de 100 de salud."
        ),
        PHOENIX_FEATHER(
                "§6§l[PLUMA DEL FÉNIX INMORTAL]",
                "§7En Inventario: §eAl sufrir daño letal, desafía a la muerte restaurando 50% de salud, otorgando Absorción y Resistencia al Fuego (10 min CD)."
        );

        public final String header;
        public final String desc;

        ArtifactType(String header, String desc) {
            this.header = header;
            this.desc = desc;
        }
    }

    private final ArtifactType artifactType;

    public ManhwaArtifactItem(Properties properties, ArtifactType artifactType) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
        this.artifactType = artifactType;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public static boolean hasArtifact(Player player, ArtifactType type) {
        if (player == null) return false;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof ManhwaArtifactItem mai && mai.getArtifactType() == type) {
                return true;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && stack.getItem() instanceof ManhwaArtifactItem mai && mai.getArtifactType() == type) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.artifact." + artifactType.name().toLowerCase() + ".header"));
        tooltip.add(Component.translatable("awakening_system.artifact." + artifactType.name().toLowerCase() + ".desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
