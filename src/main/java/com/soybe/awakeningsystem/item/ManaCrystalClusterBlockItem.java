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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Racimo Completo de Cristal de Maná (Mana Crystal Cluster BlockItem).
 * Puede colocarse como bloque decorativo con luz propia o canjearse en el aire por Monedas del Sistema.
 */
public class ManaCrystalClusterBlockItem extends BlockItem {

    private final ManaCrystalShardItem.CrystalRank rank;
    private final int clusterCoinValue;

    public ManaCrystalClusterBlockItem(Block block, ManaCrystalShardItem.CrystalRank rank, int clusterCoinValue) {
        super(block, new Item.Properties().rarity(rank.getRarity()));
        this.rank = rank;
        this.clusterCoinValue = clusterCoinValue;
    }

    public ManaCrystalShardItem.CrystalRank getRank() {
        return this.rank;
    }

    public int getClusterCoinValue() {
        return this.clusterCoinValue;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        tooltip.add(Component.translatable("item.awakening_system.mana_crystal.hunter_rank", this.rank.getColor().toString(), this.rank.getLabel()));
        tooltip.add(Component.translatable("item.awakening_system.mana_crystal.shop_value", nf.format(this.clusterCoinValue)));
        tooltip.add(Component.translatable("item.awakening_system.mana_crystal.cluster_place"));
        tooltip.add(Component.translatable("item.awakening_system.mana_crystal.sell_desc"));
        tooltip.add(Component.translatable("item.awakening_system.mana_crystal.cluster_usage"));
    }
}
