package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import java.time.LocalDate;
import java.util.List;

public class CasinoTrophyItem extends BlockItem {

    public CasinoTrophyItem(Block block, Properties properties) {
        super(block, properties.rarity(Rarity.RARE).stacksTo(16));
    }

    public CasinoTrophyItem(Properties properties) {
        this(SystemBlocks.CASINO_TROPHY.get(), properties);
    }

    public static ItemStack createTrophy(int type, String winnerName, String achievementDesc) {
        Item item = switch (type) {
            case 2 -> SystemItems.BLACKJACK_TROPHY.get();
            case 3 -> SystemItems.ROULETTE_TROPHY.get();
            case 4 -> SystemItems.POOL_TROPHY.get();
            case 5 -> SystemItems.DARTS_TROPHY.get();
            default -> SystemItems.CASINO_TROPHY.get();
        };
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        tag.putInt("trophy_type", type);
        tag.putString("winner_name", winnerName != null ? winnerName : "");
        tag.putString("achievement_desc", achievementDesc != null ? achievementDesc : "");
        tag.putString("awarded_date", LocalDate.now().toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        String title = switch (type) {
            case 1 -> "UMA Derby Cup Trophy";
            case 2 -> "Blackjack Grand Ace Trophy";
            case 3 -> "Roulette Grand Jackpot Trophy";
            case 4 -> "Billiards Legend Trophy";
            case 5 -> "Darts Master Trophy";
            default -> "Casino Trophy";
        };
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("§6🏆 " + title));
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        int type = 1;
        String winner = "Legendary Champion";
        String desc = "Championship Feat";
        String date = "";

        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("trophy_type")) type = tag.getInt("trophy_type");
            if (tag.contains("winner_name") && !tag.getString("winner_name").isEmpty()) winner = tag.getString("winner_name");
            if (tag.contains("achievement_desc") && !tag.getString("achievement_desc").isEmpty()) desc = tag.getString("achievement_desc");
            if (tag.contains("awarded_date")) date = tag.getString("awarded_date");
        }

        Component category = switch (type) {
            case 1 -> Component.translatable("item.awakening_system.casino_trophy.cat_derby");
            case 2 -> Component.translatable("item.awakening_system.casino_trophy.cat_blackjack");
            case 3 -> Component.translatable("item.awakening_system.casino_trophy.cat_jackpot");
            case 4 -> Component.translatable("item.awakening_system.casino_trophy.cat_billiards");
            case 5 -> Component.translatable("item.awakening_system.casino_trophy.cat_darts");
            default -> Component.translatable("item.awakening_system.casino_trophy.cat_default");
        };

        tooltip.add(Component.literal("§6✦ ").append(category).append(" ✦").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.casino_trophy.awarded_to", winner).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.casino_trophy.feat_line", desc).withStyle(ChatFormatting.AQUA));
        if (!date.isEmpty()) {
            tooltip.add(Component.translatable("item.awakening_system.casino_trophy.date_line", date).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("item.awakening_system.casino_trophy.hint").withStyle(ChatFormatting.GRAY));
    }
}
