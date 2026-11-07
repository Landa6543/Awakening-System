package com.soybe.awakeningsystem.workshop;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MonsterCoreItem extends Item {

    public enum CoreRank {
        E("E", ChatFormatting.WHITE, Rarity.COMMON, 1),
        D("D", ChatFormatting.GREEN, Rarity.UNCOMMON, 2),
        C("C", ChatFormatting.AQUA, Rarity.RARE, 3),
        B("B", ChatFormatting.DARK_PURPLE, Rarity.RARE, 4),
        A("A", ChatFormatting.RED, Rarity.EPIC, 5),
        S("S", ChatFormatting.GOLD, Rarity.EPIC, 6);

        private final String rankName;
        private final ChatFormatting formatting;
        private final Rarity rarity;
        private final int tier;

        CoreRank(String rankName, ChatFormatting formatting, Rarity rarity, int tier) {
            this.rankName = rankName;
            this.formatting = formatting;
            this.rarity = rarity;
            this.tier = tier;
        }

        public String getRankName() { return rankName; }
        public ChatFormatting getFormatting() { return formatting; }
        public Rarity getRarity() { return rarity; }
        public int getTier() { return tier; }
    }

    private final CoreRank coreRank;

    public MonsterCoreItem(CoreRank rank, Properties properties) {
        super(properties.rarity(rank.getRarity()).stacksTo(64));
        this.coreRank = rank;
    }

    public CoreRank getCoreRank() {
        return coreRank;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.monster_core.rank",
                Component.literal("[" + coreRank.getRankName() + "]").withStyle(coreRank.getFormatting(), ChatFormatting.BOLD))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.monster_core.desc").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.awakening_system.monster_core.usage").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
