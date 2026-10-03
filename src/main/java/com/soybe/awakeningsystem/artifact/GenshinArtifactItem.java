package com.soybe.awakeningsystem.artifact;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class GenshinArtifactItem extends Item {
    private final ArtifactSet set;
    private final ArtifactType type;
    private final int stars;

    public GenshinArtifactItem(ArtifactSet set, ArtifactType type, Properties properties) {
        super(properties.stacksTo(1).rarity(set.getMaxStars() >= 5 ? Rarity.EPIC : Rarity.RARE));
        this.set = set;
        this.type = type;
        this.stars = set.getMaxStars();
    }

    public ArtifactSet getArtifactSet() {
        return set;
    }

    public ArtifactType getArtifactType() {
        return type;
    }

    public int getStars() {
        return stars;
    }

    @Override
    public Component getName(ItemStack stack) {
        String key = this.getDescriptionId(stack);
        return Component.translatableWithFallback(key, type.getDisplayName() + " (" + set.getDisplayName() + ")");
    }

    public static int getLevel(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("ArtifactLevel")) {
            return Math.max(0, Math.min(20, cd.copyTag().getInt("ArtifactLevel")));
        }
        return 0;
    }

    public static void setLevel(ItemStack stack, int level) {
        if (stack.isEmpty()) return;
        int clamped = Math.max(0, Math.min(20, level));
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = (cd != null) ? cd.copyTag() : new CompoundTag();
        tag.putInt("ArtifactLevel", clamped);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public float getHpBonus(ItemStack stack) {
        if (type != ArtifactType.FLOWER) return 0.0F;
        int lvl = getLevel(stack);
        return (stars * 40.0F) + (lvl * 18.0F);
    }

    public float getAttackBonus(ItemStack stack) {
        if (type != ArtifactType.PLUME) return 0.0F;
        int lvl = getLevel(stack);
        return (stars * 3.5F) + (lvl * 1.5F);
    }

    public float getAttackPercentBonus(ItemStack stack) {
        if (type != ArtifactType.SANDS) return 0.0F;
        int lvl = getLevel(stack);
        return (stars * 2.5F) + (lvl * 1.2F);
    }

    public float getElementalDamagePercent(ItemStack stack) {
        if (type != ArtifactType.GOBLET) return 0.0F;
        int lvl = getLevel(stack);
        return (stars * 3.0F) + (lvl * 1.5F);
    }

    public float getCritRatePercent(ItemStack stack) {
        if (type != ArtifactType.CIRCLET) return 0.0F;
        int lvl = getLevel(stack);
        return (stars * 1.8F) + (lvl * 0.8F);
    }

    public float getCritDmgPercent(ItemStack stack) {
        if (type != ArtifactType.CIRCLET) return 0.0F;
        int lvl = getLevel(stack);
        return (stars * 3.2F) + (lvl * 1.6F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int lvl = getLevel(stack);
        String starsStr = "★".repeat(stars);
        String starColor = stars >= 5 ? "§6" : (stars >= 4 ? "§d" : "§9");

        tooltip.add(Component.literal(starColor + starsStr + " §f[").append(set.getDisplayNameComponent()).append("]").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable("awakening_system.artifact.tooltip.type", type.getDisplayNameComponent(), lvl));
        tooltip.add(Component.empty());

        // Atributo principal
        switch (type) {
            case FLOWER -> tooltip.add(Component.translatable("awakening_system.artifact.tooltip.hp", (int) getHpBonus(stack)));
            case PLUME -> tooltip.add(Component.translatable("awakening_system.artifact.tooltip.atk", String.format("%.1f", getAttackBonus(stack))));
            case SANDS -> tooltip.add(Component.translatable("awakening_system.artifact.tooltip.atk_pct", String.format("%.1f", getAttackPercentBonus(stack))));
            case GOBLET -> tooltip.add(Component.translatable("awakening_system.artifact.tooltip.elemental", String.format("%.1f", getElementalDamagePercent(stack))));
            case CIRCLET -> {
                tooltip.add(Component.translatable("awakening_system.artifact.tooltip.crit_rate", String.format("%.1f", getCritRatePercent(stack))));
                tooltip.add(Component.translatable("awakening_system.artifact.tooltip.crit_dmg", String.format("%.1f", getCritDmgPercent(stack))));
            }
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("awakening_system.artifact.tooltip.set_bonuses"));
        tooltip.add(Component.translatable("awakening_system.artifact.tooltip.2p", set.getBonus2pComponent()));
        tooltip.add(Component.translatable("awakening_system.artifact.tooltip.4p", set.getBonus4pComponent()));

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("awakening_system.artifact.tooltip.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
