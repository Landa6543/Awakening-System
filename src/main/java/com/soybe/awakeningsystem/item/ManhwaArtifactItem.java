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
                "§6§l[DEMON MONARCH BARAN RING]",
                "§7In Inventory: §e+20% Max Energy (EP) and attacks unleash electric sparks (§6+4 shock damage§e)."
        ),
        ASRAHAN_MANA_CHALICE(
                "§b§l[ASRAHAN MANA CHALICE]",
                "§7In Inventory: §bRegenerates 2 EP every 3 seconds and extinguishes fire on the bearer automatically."
        ),
        KING_SLAYER_NECKLACE(
                "§c§l[KING SLAYER NECKLACE]",
                "§7In Inventory: §4+25% Direct damage against Dungeon Bosses and creatures with over 100 HP."
        ),
        PHOENIX_FEATHER(
                "§6§l[IMMORTAL PHOENIX FEATHER]",
                "§7In Inventory: §eWhen taking lethal damage, defies death restoring 50% HP, granting Absorption and Fire Resistance (10 min CD)."
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
