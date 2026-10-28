package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.OpenVisionSelectionPayload;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
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
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Estatua de los Siete (Statue of the Seven) de Teyvat.
 * Permite a los viajeros sintonizar su Visión Elemental, curarse por completo
 * y restaurar su Energía con la Bendición de los Arcontes.
 */
public class StatueOfTheSevenBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<StatueOfTheSevenBlock> CODEC = simpleCodec(StatueOfTheSevenBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 32.0D, 15.0D);

    public StatueOfTheSevenBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            openStatueMenu(sp);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        String oculusElem = getOculusElement(stack);
        if (oculusElem != null) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                offerHeldOculus(sp, stack, oculusElem, pos);
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static void openStatueMenu(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.OpenStatueScreenPayload(
                data.anemoStatueLevel(), data.anemoOculiProgress(),
                data.geoStatueLevel(), data.geoOculiProgress(),
                data.electroStatueLevel(), data.electroOculiProgress(),
                data.dendroStatueLevel(), data.dendroOculiProgress()
        ));
    }

    public static void handleStatueAction(ServerPlayer player, com.soybe.awakeningsystem.StatueActionC2SPayload payload) {
        if (player == null || !(player.level() instanceof ServerLevel sl)) return;
        SystemData data = SystemEvents.data(player);

        if (payload.actionId() == com.soybe.awakeningsystem.StatueActionC2SPayload.ACTION_BLESSING) {
            player.setHealth(player.getMaxHealth());
            data.setEnergy(data.maxEnergy());
            AwakeningSystemMod.sync(player);

            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.2F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 1.1F);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.1D);

            player.displayClientMessage(Component.translatable("block.awakening_system.statue_of_the_seven.blessing"), true);
        } else if (payload.actionId() == com.soybe.awakeningsystem.StatueActionC2SPayload.ACTION_OFFER_OCULI) {
            boolean anyOffered = offerAllInventoryOculi(player, sl);
            if (!anyOffered) {
                player.displayClientMessage(Component.translatable("block.awakening_system.statue_of_the_seven.no_oculus"), true);
            }
            openStatueMenu(player);
        } else if (payload.actionId() == com.soybe.awakeningsystem.StatueActionC2SPayload.ACTION_RESONANCE) {
            PacketDistributor.sendToPlayer(player, new OpenVisionSelectionPayload());
        }
    }

    private static void offerHeldOculus(ServerPlayer player, ItemStack stack, String element, BlockPos pos) {
        if (player.level() instanceof ServerLevel sl) {
            int countToOffer = player.isShiftKeyDown() ? stack.getCount() : 1;
            boolean success = applyOculiOffering(player, element, countToOffer, sl);
            if (success) {
                stack.shrink(countToOffer);
            }
        }
    }

    private static boolean offerAllInventoryOculi(ServerPlayer player, ServerLevel sl) {
        boolean offeredAny = false;
        int anemoCount = 0, geoCount = 0, electroCount = 0, dendroCount = 0;

        for (ItemStack s : player.getInventory().items) {
            if (s.isEmpty()) continue;
            String elem = getOculusElement(s);
            if (elem != null) {
                int count = s.getCount();
                switch (elem) {
                    case "ANEMO" -> anemoCount += count;
                    case "GEO" -> geoCount += count;
                    case "ELECTRO" -> electroCount += count;
                    case "DENDRO" -> dendroCount += count;
                }
                s.shrink(count);
            }
        }

        if (anemoCount > 0) { applyOculiOffering(player, "ANEMO", anemoCount, sl); offeredAny = true; }
        if (geoCount > 0) { applyOculiOffering(player, "GEO", geoCount, sl); offeredAny = true; }
        if (electroCount > 0) { applyOculiOffering(player, "ELECTRO", electroCount, sl); offeredAny = true; }
        if (dendroCount > 0) { applyOculiOffering(player, "DENDRO", dendroCount, sl); offeredAny = true; }

        return offeredAny;
    }

    private static boolean applyOculiOffering(ServerPlayer player, String element, int amount, ServerLevel sl) {
        SystemData data = SystemEvents.data(player);
        int currentLevel = getElementLevel(data, element);
        int currentProgress = getElementProgress(data, element);

        if (currentLevel >= 10) {
            player.displayClientMessage(Component.translatable("block.awakening_system.statue_of_the_seven.max_level", getRegionName(element)), true);
            return false;
        }

        int available = amount;
        boolean leveledUp = false;
        int levelsGained = 0;

        while (available > 0 && currentLevel < 10) {
            int req = SystemData.getOculiRequiredForLevel(currentLevel + 1);
            int needed = req - currentProgress;
            if (available >= needed) {
                available -= needed;
                currentProgress = 0;
                currentLevel++;
                leveledUp = true;
                levelsGained++;
                giveLevelRewards(player, element, currentLevel);
            } else {
                currentProgress += available;
                available = 0;
            }
        }

        setElementLevel(data, element, currentLevel);
        setElementProgress(data, element, currentProgress);
        AwakeningSystemMod.sync(player);

        sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                leveledUp ? SoundEvents.PLAYER_LEVELUP : SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, 1.2F, leveledUp ? 1.2F : 1.5F);
        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(),
                leveledUp ? 40 : 15, 0.5D, 0.7D, 0.5D, 0.15D);

        int nextReq = SystemData.getOculiRequiredForLevel(currentLevel + 1);
        String progText = currentLevel >= 10 ? "§6[MAX LEVEL]" : "§7(" + currentProgress + "/" + nextReq + ")";

        if (leveledUp) {
            player.displayClientMessage(Component.translatable("block.awakening_system.statue_of_the_seven.level_up_rewards", currentLevel, (60 * levelsGained), (10 * levelsGained), element), true);
        } else {
            player.displayClientMessage(Component.translatable("block.awakening_system.statue_of_the_seven.offering_progress", amount, element, progText), true);
        }

        return true;
    }

    private static void giveLevelRewards(ServerPlayer player, String element, int newLevel) {
        SystemData data = SystemEvents.data(player);

        // 1. Protogemas (60 al balance del jugador y 1 ítem físico en inventario)
        data.addPrimogems(60);
        player.getInventory().add(new ItemStack(com.soybe.awakeningsystem.SystemItems.PRIMOGEM.get(), 1));

        // 2. Insignias elementales (+10)
        ItemStack sigil = switch (element) {
            case "GEO" -> new ItemStack(com.soybe.awakeningsystem.SystemItems.GEO_SIGIL.get(), 10);
            case "ELECTRO" -> new ItemStack(com.soybe.awakeningsystem.SystemItems.ELECTRO_SIGIL.get(), 10);
            case "DENDRO" -> new ItemStack(com.soybe.awakeningsystem.SystemItems.DENDRO_SIGIL.get(), 10);
            default -> new ItemStack(com.soybe.awakeningsystem.SystemItems.ANEMO_SIGIL.get(), 10);
        };
        player.getInventory().add(sigil);

        // 3. EXP
        player.giveExperiencePoints(500);

        // 4. Llaves de Santuario de las Profundidades (Niveles 2, 4, 6, 8, 10)
        if (newLevel == 2 || newLevel == 4 || newLevel == 6 || newLevel == 8 || newLevel == 10) {
            ItemStack key = switch (element) {
                case "GEO" -> new ItemStack(com.soybe.awakeningsystem.SystemItems.GEO_SHRINE_KEY.get(), 1);
                case "ELECTRO" -> new ItemStack(com.soybe.awakeningsystem.SystemItems.ELECTRO_SHRINE_KEY.get(), 1);
                case "DENDRO" -> new ItemStack(com.soybe.awakeningsystem.SystemItems.DENDRO_SHRINE_KEY.get(), 1);
                default -> new ItemStack(com.soybe.awakeningsystem.SystemItems.ANEMO_SHRINE_KEY.get(), 1);
            };
            player.getInventory().add(key);
            player.displayClientMessage(Component.translatable("block.awakening_system.statue_of_the_seven.shrine_key_reward", getRegionName(element)), true);
        }

        // 5. Aumento permanente de energía/stamina (+10)
        data.addStatueEnergyBonus(10);
    }

    private static String getOculusElement(ItemStack stack) {
        if (stack.is(com.soybe.awakeningsystem.SystemItems.ANEMOCULUS.get()) || stack.is(com.soybe.awakeningsystem.SystemItems.ANEMOCULUS_BLOCK_ITEM.get())) return "ANEMO";
        if (stack.is(com.soybe.awakeningsystem.SystemItems.GEOCULUS.get()) || stack.is(com.soybe.awakeningsystem.SystemItems.GEOCULUS_BLOCK_ITEM.get())) return "GEO";
        if (stack.is(com.soybe.awakeningsystem.SystemItems.ELECTROCULUS.get()) || stack.is(com.soybe.awakeningsystem.SystemItems.ELECTROCULUS_BLOCK_ITEM.get())) return "ELECTRO";
        if (stack.is(com.soybe.awakeningsystem.SystemItems.DENDROCULUS.get()) || stack.is(com.soybe.awakeningsystem.SystemItems.DENDROCULUS_BLOCK_ITEM.get())) return "DENDRO";
        return null;
    }

    private static int getElementLevel(SystemData data, String element) {
        return switch (element) {
            case "GEO" -> data.geoStatueLevel();
            case "ELECTRO" -> data.electroStatueLevel();
            case "DENDRO" -> data.dendroStatueLevel();
            default -> data.anemoStatueLevel();
        };
    }

    private static void setElementLevel(SystemData data, String element, int level) {
        switch (element) {
            case "GEO" -> data.setGeoStatueLevel(level);
            case "ELECTRO" -> data.setElectroStatueLevel(level);
            case "DENDRO" -> data.setDendroStatueLevel(level);
            default -> data.setAnemoStatueLevel(level);
        }
    }

    private static int getElementProgress(SystemData data, String element) {
        return switch (element) {
            case "GEO" -> data.geoOculiProgress();
            case "ELECTRO" -> data.electroOculiProgress();
            case "DENDRO" -> data.dendroOculiProgress();
            default -> data.anemoOculiProgress();
        };
    }

    private static void setElementProgress(SystemData data, String element, int progress) {
        switch (element) {
            case "GEO" -> data.setGeoOculiProgress(progress);
            case "ELECTRO" -> data.setElectroOculiProgress(progress);
            case "DENDRO" -> data.setDendroOculiProgress(progress);
            default -> data.setAnemoOculiProgress(progress);
        }
    }

    private static String getRegionName(String element) {
        return switch (element) {
            case "GEO" -> "Liyue";
            case "ELECTRO" -> "Inazuma";
            case "DENDRO" -> "Sumeru";
            default -> "Mondstadt";
        };
    }
}
