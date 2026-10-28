package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PrimogemPickaxeItem extends PickaxeItem {

    public PrimogemPickaxeItem() {
        super(CustomToolTiers.PRIMOGEM, new Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .attributes(PickaxeItem.createAttributes(CustomToolTiers.PRIMOGEM, 1.5F, -2.8F)));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        if (!level.isClientSide() && result && level instanceof ServerLevel serverLevel) {
            boolean isOre = state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.IRON_ORES)
                    || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.REDSTONE_ORES)
                    || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.COPPER_ORES)
                    || state.is(BlockTags.COAL_ORES) || state.is(BlockTags.EMERALD_ORES)
                    || state.getBlock().getDescriptionId().contains("ore");

            if (isOre) {
                double roll = level.getRandom().nextDouble();
                if (roll < 0.25) {
                    int count = 1 + level.getRandom().nextInt(3);
                    ItemStack frags = new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), count);
                    ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, frags);
                    level.addFreshEntity(entity);
                    serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.1);
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
                }

                if (roll < 0.06) {
                    ItemStack primo = new ItemStack(SystemItems.PRIMOGEM.get(), 1);
                    ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, primo);
                    level.addFreshEntity(entity);
                    serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.15);
                    level.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8F, 1.8F);
                }
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.primogem_pickaxe.title").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.primogem_pickaxe.desc"));
        tooltip.add(Component.translatable("item.awakening_system.primogem_pickaxe.eff1"));
        tooltip.add(Component.translatable("item.awakening_system.primogem_pickaxe.eff2"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
