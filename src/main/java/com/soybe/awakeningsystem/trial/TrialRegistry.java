package com.soybe.awakeningsystem.trial;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** Bloques, objetos y block entities de las Cámaras de Desafío, en sus propios registros. */
public final class TrialRegistry {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, AwakeningSystemMod.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, AwakeningSystemMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AwakeningSystemMod.MOD_ID);

    // Irrompibles en supervivencia, como los generadores de prueba de Minecraft
    public static final DeferredHolder<Block, TrialSpawnerBlock> SHADOW_TRIAL_SPAWNER = BLOCKS.register("shadow_trial_spawner",
            () -> new TrialSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.0F).sound(SoundType.TRIAL_SPAWNER).noOcclusion()
                    .lightLevel(s -> s.getValue(TrialSpawnerBlock.ACTIVE) ? 12 : 5)));
    public static final DeferredHolder<Block, TrialVaultBlock> AWAKENING_VAULT = BLOCKS.register("awakening_vault",
            () -> new TrialVaultBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
                    .strength(-1.0F, 3600000.0F).sound(SoundType.VAULT).lightLevel(s -> 6)));

    public static final DeferredHolder<Item, BlockItem> SHADOW_TRIAL_SPAWNER_ITEM = ITEMS.register("shadow_trial_spawner",
            () -> new BlockItem(SHADOW_TRIAL_SPAWNER.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, BlockItem> AWAKENING_VAULT_ITEM = ITEMS.register("awakening_vault",
            () -> new BlockItem(AWAKENING_VAULT.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> TRIAL_KEY = ITEMS.register("awakening_trial_key",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.awakening_system.awakening_trial_key.desc"));
                }
            });

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrialSpawnerBlockEntity>> SPAWNER_ENTITY =
            BLOCK_ENTITIES.register("shadow_trial_spawner",
                    () -> BlockEntityType.Builder.of(TrialSpawnerBlockEntity::new, SHADOW_TRIAL_SPAWNER.get()).build(null));
    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrialVaultBlockEntity>> VAULT_ENTITY =
            BLOCK_ENTITIES.register("awakening_vault",
                    () -> BlockEntityType.Builder.of(TrialVaultBlockEntity::new, AWAKENING_VAULT.get()).build(null));

    private TrialRegistry() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
