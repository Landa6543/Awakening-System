package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Esquirla de Cristal de Maná (Mana Crystal Shard).
 * Recurso extraído de las profundidades de la Gran Torre.
 * Puede ser absorbido / vendido al Sistema haciendo clic derecho (o Shift + Clic Derecho para todo el stack).
 */
public class ManaCrystalShardItem extends Item {

    public enum CrystalRank {
        E("E", 150, ChatFormatting.WHITE, Rarity.COMMON),
        D("D", 600, ChatFormatting.GREEN, Rarity.UNCOMMON),
        C("C", 2500, ChatFormatting.AQUA, Rarity.RARE),
        B("B", 12000, ChatFormatting.DARK_PURPLE, Rarity.EPIC),
        A("A", 60000, ChatFormatting.RED, Rarity.EPIC),
        S("S", 250000, ChatFormatting.GOLD, Rarity.EPIC);

        private final String label;
        private final int coinValue;
        private final ChatFormatting color;
        private final Rarity rarity;

        CrystalRank(String label, int coinValue, ChatFormatting color, Rarity rarity) {
            this.label = label;
            this.coinValue = coinValue;
            this.color = color;
            this.rarity = rarity;
        }

        public String getLabel() { return label; }
        public int getCoinValue() { return coinValue; }
        public ChatFormatting getColor() { return color; }
        public Rarity getRarity() { return rarity; }
    }

    private final CrystalRank rank;

    public ManaCrystalShardItem(CrystalRank rank) {
        super(new Item.Properties().stacksTo(64).rarity(rank.getRarity()));
        this.rank = rank;
    }

    public CrystalRank getRank() {
        return this.rank;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        tooltip.add(Component.translatable("item.awakening_system.mana_crystal.hunter_rank", this.rank.getColor().toString(), this.rank.getLabel()));
        tooltip.add(Component.translatable("item.awakening_system.mana_crystal.shop_value", nf.format(this.rank.getCoinValue())));
        tooltip.add(Component.translatable("item.awakening_system.mana_crystal.sell_desc"));
        tooltip.add(Component.translatable("item.awakening_system.mana_crystal.shard_usage"));
    }
}
