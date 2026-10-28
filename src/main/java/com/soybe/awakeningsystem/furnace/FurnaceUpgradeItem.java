package com.soybe.awakeningsystem.furnace;

import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FurnaceUpgradeItem extends Item {
    private final FurnaceTier targetTier;
    private final String requiredPreviousTier;

    public FurnaceUpgradeItem(Properties properties, FurnaceTier targetTier, String requiredPreviousTier) {
        super(properties);
        this.targetTier = targetTier;
        this.requiredPreviousTier = requiredPreviousTier;
    }

    public FurnaceUpgradeItem(FurnaceTier targetTier, Properties properties) {
        this(properties, targetTier, null);
    }

    public FurnaceTier getTargetTier() {
        return targetTier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();

        if (!canUpgradeBlock(state.getBlock())) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        NonNullList<ItemStack> savedItems = NonNullList.withSize(3, ItemStack.EMPTY);
        int savedBurnTime = 0;
        int savedLitDuration = 0;
        boolean wasLit = false;

        BlockEntity be = serverLevel.getBlockEntity(pos);
        if (be instanceof CustomTierFurnaceBlockEntity customBE) {
            for (int i = 0; i < 3; i++) {
                savedItems.set(i, customBE.getItem(i).copy());
            }
            savedBurnTime = customBE.dataAccess.get(CustomTierFurnaceBlockEntity.DATA_LIT_TIME);
            savedLitDuration = customBE.dataAccess.get(CustomTierFurnaceBlockEntity.DATA_LIT_DURATION);
            wasLit = customBE.isLit();
            customBE.clearContent();
        } else if (be instanceof AbstractFurnaceBlockEntity vanillaBE) {
            for (int i = 0; i < Math.min(3, vanillaBE.getContainerSize()); i++) {
                savedItems.set(i, vanillaBE.getItem(i).copy());
            }
            vanillaBE.clearContent();
        }

        Direction facing = Direction.NORTH;
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            facing = state.getValue(HorizontalDirectionalBlock.FACING);
        }

        Block targetBlock = getTargetBlock();
        BlockState newState = targetBlock.defaultBlockState()
                .setValue(CustomTierFurnaceBlock.FACING, facing)
                .setValue(CustomTierFurnaceBlock.LIT, wasLit);

        serverLevel.setBlock(pos, newState, 3);

        BlockEntity newBE = serverLevel.getBlockEntity(pos);
        if (newBE instanceof CustomTierFurnaceBlockEntity customNewBE) {
            customNewBE.setInventoryItems(savedItems);
            customNewBE.dataAccess.set(CustomTierFurnaceBlockEntity.DATA_LIT_TIME, savedBurnTime);
            customNewBE.dataAccess.set(CustomTierFurnaceBlockEntity.DATA_LIT_DURATION, savedLitDuration);
            customNewBE.setChanged();
        }

        serverLevel.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
        serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.4F);
        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 35, 0.4D, 0.4D, 0.4D, 0.1D);

        if (player != null) {
            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }
            player.displayClientMessage(Component.translatable("awakening_system.furnace.upgraded_toast", targetTier.getDisplayName())
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
        }

        return InteractionResult.SUCCESS;
    }

    private boolean canUpgradeBlock(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        String path = id.getPath();

        return switch (this.targetTier) {
            case ABYSSAL -> path.contains("diamond_furnace") || path.contains("blast_furnace") || path.equals("furnace");
            case SOUL_ALLOY -> block == SystemBlocks.ABYSSAL_FURNACE.get() || path.contains("netherite_furnace") || path.contains("diamond_furnace") || path.contains("blast_furnace");
            case ASTRALITE -> block == SystemBlocks.SOUL_ALLOY_FURNACE.get();
            case MITHRIL -> block == SystemBlocks.ASTRALITE_FURNACE.get() || block == SystemBlocks.SOUL_ALLOY_FURNACE.get() || path.contains("netherite_furnace") || path.contains("astral_furnace") || path.contains("blast_furnace") || path.equals("furnace");
            case ADAMANTITE -> block == SystemBlocks.MITHRIL_FURNACE.get();
            case SPIRIT_JADE -> block == SystemBlocks.ADAMANTITE_FURNACE.get();
            case ASTRAL -> block == SystemBlocks.SPIRIT_JADE_FURNACE.get();
            case CELESTIAL -> block == SystemBlocks.ASTRAL_FURNACE.get();
            case PRIMORDIAL -> block == SystemBlocks.CELESTIAL_FURNACE.get();
        };
    }

    private Block getTargetBlock() {
        return switch (this.targetTier) {
            case ABYSSAL -> SystemBlocks.ABYSSAL_FURNACE.get();
            case SOUL_ALLOY -> SystemBlocks.SOUL_ALLOY_FURNACE.get();
            case ASTRALITE -> SystemBlocks.ASTRALITE_FURNACE.get();
            case MITHRIL -> SystemBlocks.MITHRIL_FURNACE.get();
            case ADAMANTITE -> SystemBlocks.ADAMANTITE_FURNACE.get();
            case SPIRIT_JADE -> SystemBlocks.SPIRIT_JADE_FURNACE.get();
            case ASTRAL -> SystemBlocks.ASTRAL_FURNACE.get();
            case CELESTIAL -> SystemBlocks.CELESTIAL_FURNACE.get();
            case PRIMORDIAL -> SystemBlocks.PRIMORDIAL_FURNACE.get();
        };
    }

    public Component getRequiredPreviousTierComponent() {
        if (requiredPreviousTier != null) {
            return Component.literal(requiredPreviousTier);
        }
        return switch (targetTier) {
            case ABYSSAL -> Component.translatable("awakening_system.furnace.tier.abyssal_req");
            case SOUL_ALLOY -> Component.translatable("block.awakening_system.abyssal_furnace");
            case ASTRALITE -> Component.translatable("block.awakening_system.soul_alloy_furnace");
            case MITHRIL -> Component.translatable("block.awakening_system.astralite_furnace");
            case ADAMANTITE -> Component.translatable("block.awakening_system.mithril_furnace");
            case SPIRIT_JADE -> Component.translatable("block.awakening_system.adamantite_furnace");
            case ASTRAL -> Component.translatable("block.awakening_system.spirit_jade_furnace");
            case CELESTIAL -> Component.translatable("block.awakening_system.astral_furnace");
            case PRIMORDIAL -> Component.translatable("block.awakening_system.celestial_furnace");
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("item.awakening_system.upgrade." + targetTier.getId() + ".desc"));
        tooltip.add(Component.literal("§7• ")
                .append(Component.translatable("awakening_system.furnace.speed_label").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": "))
                .append(Component.literal(targetTier.getSpeedDisplay()).withStyle(ChatFormatting.YELLOW)));
        if (targetTier.getDoubleOutputChance() > 0.0F) {
            tooltip.add(Component.literal("§7• ")
                    .append(Component.translatable("awakening_system.furnace.alchemy_label").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(": "))
                    .append(Component.literal("+" + (int)(targetTier.getDoubleOutputChance() * 100) + "% ").withStyle(ChatFormatting.GREEN))
                    .append(Component.translatable("awakening_system.furnace.duplicate_label").withStyle(ChatFormatting.DARK_GREEN)));
        }
        tooltip.add(Component.translatable("awakening_system.furnace.upgrade_instruction_prefix").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(" "))
                .append(getRequiredPreviousTierComponent().copy().withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" "))
                .append(Component.translatable("awakening_system.furnace.upgrade_instruction_suffix").withStyle(ChatFormatting.DARK_GRAY)));
    }
}
