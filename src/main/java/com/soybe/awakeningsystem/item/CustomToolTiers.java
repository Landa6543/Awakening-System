package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public enum CustomToolTiers implements Tier {
    ABYSSAL(1800, 8.5F, 3.5F, 12, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, () -> Ingredient.of(SystemItems.ABYSSAL_INGOT.get())),
    SOUL_ALLOY(2200, 10.0F, 4.2F, 16, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> Ingredient.of(SystemItems.SOUL_ALLOY_INGOT.get())),
    ASTRALITE(2450, 10.8F, 4.6F, 20, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> Ingredient.of(SystemItems.ASTRALITE_INGOT.get())),
    MITHRIL(2600, 11.5F, 5.0F, 22, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> Ingredient.of(SystemItems.MITHRIL_INGOT.get())),
    ADAMANTITE(3800, 14.0F, 7.0F, 18, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> Ingredient.of(SystemItems.ADAMANTITE_INGOT.get())),
    SPIRIT_JADE(5500, 18.0F, 9.5F, 24, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> Ingredient.of(SystemItems.SPIRIT_JADE.get())),
    ASTRAL(9000, 24.0F, 12.5F, 30, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> Ingredient.of(SystemItems.ASTRAL_INGOT.get())),
    PRIMOGEM(3200, 13.0F, 6.5F, 25, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> Ingredient.of(SystemItems.PRIMOGEM.get())),
    CELESTIAL(12000, 28.0F, 15.0F, 32, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> Ingredient.of(SystemItems.CELESTIAL_INGOT.get())),
    PRIMORDIAL(16000, 34.0F, 18.0F, 35, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> Ingredient.of(SystemItems.PRIMORDIAL_INGOT.get()));

    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final TagKey<Block> incorrectBlocks;
    private final Supplier<Ingredient> repairIngredient;

    CustomToolTiers(int uses, float speed, float damage, int enchantmentValue, TagKey<Block> incorrectBlocks, Supplier<Ingredient> repairIngredient) {
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.incorrectBlocks = incorrectBlocks;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return this.incorrectBlocks;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
