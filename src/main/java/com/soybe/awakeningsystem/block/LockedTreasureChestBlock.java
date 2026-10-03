package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.entity.LockedTreasureChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LockedTreasureChestBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<LockedTreasureChestBlock> CODEC = simpleCodec(LockedTreasureChestBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public enum ChestTheme implements StringRepresentable {
        AUTO("auto"),
        OVERWORLD("overworld"),
        NETHER("nether"),
        END("end"),
        TEYVAT("teyvat"),
        ISEKAI("isekai"),
        MURIM("murim");

        private final String name;
        ChestTheme(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    private final ChestTheme theme;

    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public LockedTreasureChestBlock(BlockBehaviour.Properties properties) {
        this(properties, ChestTheme.AUTO);
    }

    public LockedTreasureChestBlock(BlockBehaviour.Properties properties, ChestTheme theme) {
        super(properties);
        this.theme = theme != null ? theme : ChestTheme.AUTO;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LOCKED, true)
                .setValue(WATERLOGGED, false));
    }

    public ChestTheme getTheme() {
        return this.theme;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, com.soybe.awakeningsystem.block.entity.ModBlockEntities.LOCKED_TREASURE_CHEST.get(), LockedTreasureChestBlockEntity::lidTick);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(id, param);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOCKED, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(LOCKED, true)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LockedTreasureChestBlockEntity chest) {
            if (!chest.isUnlockedFor(player.getUUID()) && !player.isCreative()) {
                // Indestructible para un jugador que aún no ha desbloqueado su cofre
                return 0.0F;
            }
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    private String resolveChestDimension(Level level) {
        if (this.theme != null && this.theme != ChestTheme.AUTO) {
            return this.theme.getSerializedName();
        }
        return level.dimension().location().getPath().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof LockedTreasureChestBlockEntity chest)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Si el cofre ya está desbloqueado para este jugador: abrir su menú instanciado
        if (chest.isUnlockedFor(player.getUUID())) {
            if (!player.isSecondaryUseActive()) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    sp.openMenu(chest.getMenuProviderFor(sp));
                    level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.9F, 1.0F);
                }
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Si aún está cerrado para este jugador, verificar si sostiene la llave adecuada
        String dim = resolveChestDimension(level);
        boolean isKey = false;
        String requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_key";

        if (dim.contains("nether")) {
            isKey = (SystemItems.NETHER_FLAME_KEY.isBound() && stack.is(SystemItems.NETHER_FLAME_KEY.get()));
            requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_nether_key";
        } else if (dim.contains("end")) {
            isKey = (SystemItems.END_VOID_KEY.isBound() && stack.is(SystemItems.END_VOID_KEY.get()));
            requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_end_key";
        } else if (dim.contains("teyvat")) {
            isKey = (SystemItems.TEYVAT_SHRINE_KEY.isBound() && stack.is(SystemItems.TEYVAT_SHRINE_KEY.get()))
                    || (SystemItems.ANEMO_SHRINE_KEY.isBound() && stack.is(SystemItems.ANEMO_SHRINE_KEY.get()))
                    || (SystemItems.GEO_SHRINE_KEY.isBound() && stack.is(SystemItems.GEO_SHRINE_KEY.get()))
                    || (SystemItems.ELECTRO_SHRINE_KEY.isBound() && stack.is(SystemItems.ELECTRO_SHRINE_KEY.get()))
                    || (SystemItems.DENDRO_SHRINE_KEY.isBound() && stack.is(SystemItems.DENDRO_SHRINE_KEY.get()));
            requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_teyvat_key";
        } else if (dim.contains("isekai")) {
            isKey = (SystemItems.ISEKAI_MANA_KEY.isBound() && stack.is(SystemItems.ISEKAI_MANA_KEY.get()))
                    || (SystemItems.YGGDRASIL_TREANT_KEY.isBound() && stack.is(SystemItems.YGGDRASIL_TREANT_KEY.get()))
                    || (SystemItems.ELEMENTAL_CASTLE_KEY.isBound() && stack.is(SystemItems.ELEMENTAL_CASTLE_KEY.get()));
            requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_isekai_key";
        } else if (dim.contains("murim")) {
            isKey = (SystemItems.MURIM_DAO_KEY.isBound() && stack.is(SystemItems.MURIM_DAO_KEY.get()))
                    || (SystemItems.JADE_ANCESTRAL_KEY.isBound() && stack.is(SystemItems.JADE_ANCESTRAL_KEY.get()));
            requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_murim_key";
        } else {
            // Overworld y cualquier otra dimensión
            isKey = (SystemItems.OVERWORLD_DUNGEON_KEY.isBound() && stack.is(SystemItems.OVERWORLD_DUNGEON_KEY.get()))
                    || (SystemItems.YGGDRASIL_TREANT_KEY.isBound() && stack.is(SystemItems.YGGDRASIL_TREANT_KEY.get()));
            requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_overworld_key";
        }

        if (isKey) {
            if (level.isClientSide()) {
                return ItemInteractionResult.SUCCESS;
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            // Desbloquear individualmente para este jugador (Lootr-style)
            chest.unlockFor(player.getUUID());

            // Generar botín instanciado personal si su inventario está vacío
            NonNullList<ItemStack> playerInv = chest.getOrCreatePlayerInventory(player.getUUID());
            boolean hasItems = false;
            for (ItemStack it : playerInv) {
                if (!it.isEmpty()) {
                    hasItems = true;
                    break;
                }
            }
            if (!hasItems) {
                populateDimensionalChestLoot(playerInv, dim, level.getRandom());
                chest.setChanged();
            }

            if (level instanceof ServerLevel sl) {
                sl.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.8F, 0.85F);
                sl.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.2F, 1.3F);
                sl.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.2F);

                sl.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 40, 0.4D, 0.4D, 0.4D, 0.15D);
                sl.sendParticles(ParticleTypes.WAX_OFF, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D, 30, 0.3D, 0.3D, 0.3D, 0.1D);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D, 25, 0.4D, 0.4D, 0.4D, 0.08D);
            }

            if (player instanceof ServerPlayer sp) {
                AwakeningSystemMod.notify(sp,
                        "awakening_system.locked_chest.unlocked_title",
                        "awakening_system.locked_chest.unlocked_desc",
                        0xFF22BB55);
                com.soybe.awakeningsystem.guild.Guild g = com.soybe.awakeningsystem.guild.GuildSavedData.get(sp.server).getGuildByPlayer(sp.getUUID());
                if (g != null) {
                    g.progressHunt("chest", 1, sp.server);
                }
            }

            return ItemInteractionResult.SUCCESS;
        } else {
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.4F, 0.8F);
                level.playSound(null, pos, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 1.2F, 0.7F);
                player.displayClientMessage(Component.translatable(requiredMessageKey), true);
            }
            return ItemInteractionResult.SUCCESS;
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof LockedTreasureChestBlockEntity chest)) {
            return InteractionResult.PASS;
        }

        if (chest.isUnlockedFor(player.getUUID())) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                sp.openMenu(chest.getMenuProviderFor(sp));
                level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.9F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // Si no está desbloqueado para el jugador
        if (!level.isClientSide()) {
            String dim = resolveChestDimension(level);
            String requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_key";
            if (dim.contains("nether")) requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_nether_key";
            else if (dim.contains("end")) requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_end_key";
            else if (dim.contains("teyvat")) requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_teyvat_key";
            else if (dim.contains("isekai")) requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_isekai_key";
            else if (dim.contains("murim")) requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_murim_key";
            else requiredMessageKey = "block.awakening_system.locked_treasure_chest.requires_overworld_key";

            level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.4F, 0.8F);
            level.playSound(null, pos, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 1.2F, 0.7F);
            player.displayClientMessage(Component.translatable(requiredMessageKey), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LockedTreasureChestBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LockedTreasureChestBlockEntity chest) {
                // En modo supervivencia, tirar los contenidos comunes o generales
                Containers.dropContents(level, pos, chest);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    public static void populateDimensionalChestLoot(Level level, LockedTreasureChestBlockEntity chest, String dim, net.minecraft.util.RandomSource rand) {
        populateDimensionalChestLoot(chest.getItems(), dim, rand);
        chest.setChanged();
    }

    public static void populateDimensionalChestLoot(NonNullList<ItemStack> targetList, String dim, net.minecraft.util.RandomSource rand) {
        if (dim.contains("nether")) {
            targetList.set(0, new ItemStack(Items.NETHERITE_SCRAP, 1 + rand.nextInt(2)));
            targetList.set(2, new ItemStack(Items.BLAZE_ROD, 3 + rand.nextInt(4)));
            targetList.set(4, new ItemStack(Items.GOLD_INGOT, 8 + rand.nextInt(9)));
            targetList.set(6, new ItemStack(Items.GHAST_TEAR, 1 + rand.nextInt(3)));
            targetList.set(8, new ItemStack(Items.NETHER_WART, 4 + rand.nextInt(5)));
            targetList.set(13, new ItemStack(SystemItems.NETHER_FLAME_KEY.get(), 1));
        } else if (dim.contains("end")) {
            targetList.set(0, new ItemStack(SystemItems.ASTRAL_UPGRADE_SMITHING_TEMPLATE.get(), 1));
            targetList.set(4, new ItemStack(SystemItems.ASTRAL_INGOT.get(), 1 + rand.nextInt(2)));
            targetList.set(8, new ItemStack(Items.SHULKER_SHELL, 2 + rand.nextInt(2)));
            targetList.set(12, new ItemStack(Items.ENDER_PEARL, 4 + rand.nextInt(5)));
            targetList.set(14, new ItemStack(Items.CHORUS_FRUIT, 8 + rand.nextInt(9)));
            targetList.set(22, new ItemStack(SystemItems.END_VOID_KEY.get(), 1));
        } else if (dim.contains("teyvat")) {
            targetList.set(0, new ItemStack(SystemItems.MORA_BUNDLE.get(), 1));
            targetList.set(4, new ItemStack(SystemItems.PRIMOGEM.get(), 4 + rand.nextInt(5)));
            targetList.set(8, new ItemStack(SystemItems.MYSTIC_ENHANCEMENT_ORE.get(), 4 + rand.nextInt(5)));
            targetList.set(12, new ItemStack(SystemItems.HEROS_WIT.get(), 2 + rand.nextInt(3)));
            targetList.set(14, new ItemStack(SystemItems.TEYVAT_SHRINE_KEY.get(), 1));
            if (!SystemItems.EXPANDED_ARTIFACTS.isEmpty()) {
                targetList.set(22, new ItemStack(SystemItems.EXPANDED_ARTIFACTS.get(rand.nextInt(SystemItems.EXPANDED_ARTIFACTS.size())).get(), 1));
            }
        } else if (dim.contains("isekai")) {
            targetList.set(0, new ItemStack(SystemItems.MITHRIL_UPGRADE_SMITHING_TEMPLATE.get(), 1));
            targetList.set(4, new ItemStack(SystemItems.MITHRIL_INGOT.get(), 2 + rand.nextInt(3)));
            targetList.set(8, new ItemStack(SystemItems.CHAOS_MANA_CORE.get(), 1 + rand.nextInt(2)));
            targetList.set(12, new ItemStack(SystemItems.MANA_FRUIT.get(), 3 + rand.nextInt(4)));
            targetList.set(14, new ItemStack(SystemItems.AWAKENING_COIN.get(), 15 + rand.nextInt(20)));
            targetList.set(22, new ItemStack(SystemItems.ISEKAI_MANA_KEY.get(), 1));
        } else if (dim.contains("murim")) {
            boolean isJade = rand.nextBoolean();
            targetList.set(0, new ItemStack(isJade ? SystemItems.SPIRIT_JADE_UPGRADE_SMITHING_TEMPLATE.get() : SystemItems.ADAMANTITE_UPGRADE_SMITHING_TEMPLATE.get(), 1));
            targetList.set(4, new ItemStack(isJade ? SystemItems.SPIRIT_JADE.get() : SystemItems.ADAMANTITE_INGOT.get(), 2 + rand.nextInt(3)));
            targetList.set(8, new ItemStack(SystemItems.WILD_GINSENG.get(), 2 + rand.nextInt(3)));
            targetList.set(12, new ItemStack(SystemItems.FOUNDATION_BUILDING_PILL.get(), 2 + rand.nextInt(3)));
            targetList.set(14, new ItemStack(SystemItems.AWAKENING_COIN.get(), 15 + rand.nextInt(20)));
            targetList.set(22, new ItemStack(SystemItems.MURIM_DAO_KEY.get(), 1));
        } else {
            // Overworld
            targetList.set(0, new ItemStack(SystemItems.ABYSSAL_UPGRADE_SMITHING_TEMPLATE.get(), 1));
            targetList.set(2, new ItemStack(SystemItems.ABYSSAL_INGOT.get(), 1 + rand.nextInt(2)));
            targetList.set(4, new ItemStack(Items.DIAMOND, 3 + rand.nextInt(4)));
            targetList.set(6, new ItemStack(Items.EMERALD, 6 + rand.nextInt(7)));
            targetList.set(8, new ItemStack(Items.GOLDEN_APPLE, 1 + rand.nextInt(2)));
            targetList.set(12, new ItemStack(Items.EXPERIENCE_BOTTLE, 6 + rand.nextInt(7)));
            targetList.set(14, new ItemStack(SystemItems.AWAKENING_COIN.get(), 10 + rand.nextInt(15)));
            targetList.set(22, new ItemStack(SystemItems.OVERWORLD_DUNGEON_KEY.get(), 1));
        }
    }
}
