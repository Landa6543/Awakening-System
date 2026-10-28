package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.FishingStationActionPayload;
import com.soybe.awakeningsystem.OpenFishingStationPayload;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Estación del Gremio de Pescadores de Teyvat (Fishing Association Station).
 * Bloque funcional de canje de peces raros, armas legendarias (La Captura, Vado del Río Ceniciento),
 * estabilizadores de sedal, prototipos de forja (Billets) y pescadería.
 */
public class FishingStationBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<FishingStationBlock> CODEC = simpleCodec(FishingStationBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape TABLE_TOP = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape TABLE_LEGS = Shapes.or(
            Block.box(1.0D, 0.0D, 1.0D, 4.0D, 12.0D, 4.0D),
            Block.box(12.0D, 0.0D, 1.0D, 15.0D, 12.0D, 4.0D),
            Block.box(1.0D, 0.0D, 12.0D, 4.0D, 12.0D, 15.0D),
            Block.box(12.0D, 0.0D, 12.0D, 15.0D, 12.0D, 15.0D)
    );
    private static final VoxelShape SHELF = Block.box(2.0D, 3.0D, 2.0D, 14.0D, 5.0D, 14.0D);
    private static final VoxelShape SHAPE = Shapes.or(TABLE_TOP, TABLE_LEGS, SHELF);

    public FishingStationBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer sp) {
            openStationFor(sp);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.FAIL;
    }

    public static void openStationFor(ServerPlayer sp) {
        SystemData data = SystemEvents.data(sp);
        int coins = data != null ? data.coins() : 0;
        int totalCaught = sp.getPersistentData().getInt("total_fish_caught");

        int medaka = countItem(sp, SystemItems.MEDAKA.get());
        int aizen = countItem(sp, SystemItems.AIZEN_MEDAKA.get());
        int sweetFlower = countItem(sp, SystemItems.SWEET_FLOWER_MEDAKA.get());
        int glaze = countItem(sp, SystemItems.GLAZE_MEDAKA.get());
        int crystal = countItem(sp, SystemItems.CRYSTALFISH.get());
        int venom = countItem(sp, SystemItems.VENOMSPINE_FISH.get());
        int abiding = countItem(sp, SystemItems.ABIDING_ANGELFISH.get());
        int raimei = countItem(sp, SystemItems.RAIMEI_ANGELFISH.get());

        PacketDistributor.sendToPlayer(sp, new OpenFishingStationPayload(
                totalCaught, coins,
                medaka, aizen, sweetFlower, glaze, crystal, venom, abiding, raimei
        ));

        sp.playNotifySound(SoundEvents.VILLAGER_WORK_FISHERMAN, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static int countItem(Player player, Item item) {
        if (item == null || player == null) return 0;
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static boolean removeItem(Player player, Item item, int count) {
        if (countItem(player, item) < count) return false;
        int remaining = count;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
        player.containerMenu.broadcastChanges();
        return true;
    }

    public static void handleAction(ServerPlayer sp, FishingStationActionPayload payload) {
        SystemData data = SystemEvents.data(sp);
        if (data == null) return;

        int type = payload.actionType();
        int itemId = payload.itemId();
        int qty = payload.quantity();

        switch (type) {
            case 0 -> handleTradeWeapon(sp, itemId, data);
            case 1 -> handleTradeBillet(sp, itemId, data);
            case 2 -> handleSellFish(sp, itemId, qty, data);
            case 3 -> handleCookFish(sp, itemId, data);
        }

        openStationFor(sp);
    }

    private static void handleTradeWeapon(ServerPlayer sp, int weaponId, SystemData data) {
        switch (weaponId) {
            case 0 -> {
                if (countItem(sp, SystemItems.RAIMEI_ANGELFISH.get()) >= 6
                        && countItem(sp, SystemItems.CRYSTALFISH.get()) >= 20
                        && countItem(sp, SystemItems.VENOMSPINE_FISH.get()) >= 20) {
                    removeItem(sp, SystemItems.RAIMEI_ANGELFISH.get(), 6);
                    removeItem(sp, SystemItems.CRYSTALFISH.get(), 20);
                    removeItem(sp, SystemItems.VENOMSPINE_FISH.get(), 20);

                    ItemStack stack = new ItemStack(SystemItems.THE_CATCH.get());
                    giveItem(sp, stack);
                    sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
                    sp.sendSystemMessage(Component.translatable("fishing_station.msg.get_the_catch"));
                } else {
                    sp.sendSystemMessage(Component.translatable("fishing_station.msg.lack_the_catch"));
                    sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            case 1 -> {
                if (countItem(sp, SystemItems.RAIMEI_ANGELFISH.get()) >= 4
                        && countItem(sp, SystemItems.CRYSTALFISH.get()) >= 12
                        && countItem(sp, SystemItems.AIZEN_MEDAKA.get()) >= 12) {
                    removeItem(sp, SystemItems.RAIMEI_ANGELFISH.get(), 4);
                    removeItem(sp, SystemItems.CRYSTALFISH.get(), 12);
                    removeItem(sp, SystemItems.AIZEN_MEDAKA.get(), 12);

                    ItemStack stack = new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get());
                    giveItem(sp, stack);
                    sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2F, 1.2F);
                    sp.sendSystemMessage(Component.translatable("fishing_station.msg.get_fleuve"));
                } else {
                    sp.sendSystemMessage(Component.translatable("fishing_station.msg.lack_fleuve"));
                    sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            case 2 -> {
                if (countItem(sp, SystemItems.MEDAKA.get()) >= 3
                        && countItem(sp, SystemItems.SWEET_FLOWER_MEDAKA.get()) >= 3) {
                    removeItem(sp, SystemItems.MEDAKA.get(), 3);
                    removeItem(sp, SystemItems.SWEET_FLOWER_MEDAKA.get(), 3);

                    ItemStack stack = new ItemStack(SystemItems.FISHING_LINE_STABILIZER.get());
                    giveItem(sp, stack);
                    sp.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.2F, 1.4F);
                    sp.sendSystemMessage(Component.translatable("fishing_station.msg.get_stabilizer"));
                } else {
                    sp.sendSystemMessage(Component.translatable("fishing_station.msg.lack_stabilizer"));
                    sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            case 3 -> {
                if (countItem(sp, SystemItems.MEDAKA.get()) >= 5
                        && countItem(sp, SystemItems.AIZEN_MEDAKA.get()) >= 5
                        && countItem(sp, SystemItems.GLAZE_MEDAKA.get()) >= 5) {
                    removeItem(sp, SystemItems.MEDAKA.get(), 5);
                    removeItem(sp, SystemItems.AIZEN_MEDAKA.get(), 5);
                    removeItem(sp, SystemItems.GLAZE_MEDAKA.get(), 5);

                    ItemStack rod = new ItemStack(Items.FISHING_ROD);
                    rod.set(DataComponents.CUSTOM_NAME, Component.translatable("fishing_station.offer.rod.name"));
                    var enchantLookup = sp.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                    rod.enchant(enchantLookup.getOrThrow(Enchantments.LUCK_OF_THE_SEA), 3);
                    rod.enchant(enchantLookup.getOrThrow(Enchantments.LURE), 3);
                    rod.enchant(enchantLookup.getOrThrow(Enchantments.UNBREAKING), 3);

                    giveItem(sp, rod);
                    sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.1F, 1.5F);
                    sp.sendSystemMessage(Component.translatable("fishing_station.msg.get_rod"));
                } else {
                    sp.sendSystemMessage(Component.translatable("fishing_station.msg.lack_rod"));
                    sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }

    private static void handleTradeBillet(ServerPlayer sp, int billetId, SystemData data) {
        Item billetItem = switch (billetId) {
            case 0 -> SystemItems.NORTHLANDER_SWORD_BILLET.get();
            case 1 -> SystemItems.NORTHLANDER_CLAYMORE_BILLET.get();
            case 2 -> SystemItems.NORTHLANDER_POLEARM_BILLET.get();
            case 3 -> SystemItems.NORTHLANDER_BOW_BILLET.get();
            case 4 -> SystemItems.NORTHLANDER_CATALYST_BILLET.get();
            case 5 -> SystemItems.MIDLANDER_SWORD_BILLET.get();
            case 6 -> SystemItems.MIDLANDER_CLAYMORE_BILLET.get();
            case 7 -> SystemItems.MIDLANDER_POLEARM_BILLET.get();
            case 8 -> SystemItems.MIDLANDER_BOW_BILLET.get();
            case 9 -> SystemItems.MIDLANDER_CATALYST_BILLET.get();
            default -> null;
        };

        if (billetItem == null) return;

        Item secondaryFish = (billetId >= 5) ? SystemItems.GLAZE_MEDAKA.get() : SystemItems.CRYSTALFISH.get();
        if (countItem(sp, SystemItems.MEDAKA.get()) >= 10 && countItem(sp, secondaryFish) >= 5) {
            removeItem(sp, SystemItems.MEDAKA.get(), 10);
            removeItem(sp, secondaryFish, 5);
            giveItem(sp, new ItemStack(billetItem));
            sp.playNotifySound(SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
            sp.sendSystemMessage(Component.translatable("fishing_station.msg.billet_success", billetItem.getDescription()));
        } else {
            sp.sendSystemMessage(Component.translatable("fishing_station.msg.billet_lack", secondaryFish.getDescription()));
            sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private static void handleSellFish(ServerPlayer sp, int fishId, int quantity, SystemData data) {
        Item fishItem = getFishItemByIndex(fishId);
        int unitPrice = getFishPriceByIndex(fishId);
        if (fishItem == null || unitPrice <= 0) return;

        int available = countItem(sp, fishItem);
        if (available <= 0) {
            sp.sendSystemMessage(Component.translatable("fishing_station.msg.sell_no_fish"));
            return;
        }

        int toSell = (quantity <= 0 || quantity > available) ? available : quantity;
        removeItem(sp, fishItem, toSell);
        int earnedCoins = toSell * unitPrice;
        data.addCoins(earnedCoins);
        AwakeningSystemMod.sync(sp);
        sp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.2F, 1.3F);
        sp.sendSystemMessage(Component.translatable("fishing_station.msg.sell_success", toSell, fishItem.getDescription(), earnedCoins));
    }

    private static void handleCookFish(ServerPlayer sp, int recipeId, SystemData data) {
        if (countItem(sp, SystemItems.MEDAKA.get()) >= 1 || countItem(sp, Items.COD) >= 1 || countItem(sp, Items.SALMON) >= 1) {
            if (countItem(sp, SystemItems.MEDAKA.get()) >= 1) {
                removeItem(sp, SystemItems.MEDAKA.get(), 1);
            } else if (countItem(sp, Items.SALMON) >= 1) {
                removeItem(sp, Items.SALMON, 1);
            } else {
                removeItem(sp, Items.COD, 1);
            }
            giveItem(sp, new ItemStack(SystemItems.GRILLED_TIGER_FISH.get()));
            sp.playNotifySound(SoundEvents.CAMPFIRE_CRACKLE, SoundSource.PLAYERS, 1.2F, 1.1F);
            sp.sendSystemMessage(Component.translatable("fishing_station.msg.cook_success"));
        } else {
            sp.sendSystemMessage(Component.translatable("fishing_station.msg.cook_lack"));
        }
    }

    public static Item getFishItemByIndex(int idx) {
        return switch (idx) {
            case 0 -> SystemItems.MEDAKA.get();
            case 1 -> SystemItems.AIZEN_MEDAKA.get();
            case 2 -> SystemItems.SWEET_FLOWER_MEDAKA.get();
            case 3 -> SystemItems.GLAZE_MEDAKA.get();
            case 4 -> SystemItems.CRYSTALFISH.get();
            case 5 -> SystemItems.VENOMSPINE_FISH.get();
            case 6 -> SystemItems.ABIDING_ANGELFISH.get();
            case 7 -> SystemItems.RAIMEI_ANGELFISH.get();
            case 8 -> Items.COD;
            case 9 -> Items.SALMON;
            case 10 -> Items.TROPICAL_FISH;
            case 11 -> Items.PUFFERFISH;
            default -> null;
        };
    }

    public static int getFishPriceByIndex(int idx) {
        return switch (idx) {
            case 0 -> 5;
            case 1 -> 8;
            case 2 -> 8;
            case 3 -> 10;
            case 4 -> 18;
            case 5 -> 20;
            case 6 -> 45;
            case 7 -> 60;
            case 8 -> 6;
            case 9 -> 8;
            case 10 -> 10;
            case 11 -> 15;
            default -> 0;
        };
    }

    private static void giveItem(ServerPlayer sp, ItemStack stack) {
        if (!sp.getInventory().add(stack)) {
            sp.drop(stack, false);
        }
        sp.containerMenu.broadcastChanges();
    }
}
