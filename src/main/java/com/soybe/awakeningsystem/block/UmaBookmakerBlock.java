package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.uma.TeyvatRacetrackManager;
import com.soybe.awakeningsystem.uma.UmaProfile;
import com.soybe.awakeningsystem.uma.network.OpenUmaRacePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Bloque Taquilla / Casa de Apuestas del Hipódromo de Teyvat (Uma Derby Bookmaker).
 * Permite a los jugadores abrir la interfaz de apuestas de carreras de Uma Musume
 * y apostar drops de mazmorras bajo la regla de Doble o Nada.
 */
public class UmaBookmakerBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<UmaBookmakerBlock> CODEC = simpleCodec(UmaBookmakerBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public UmaBookmakerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            openBookmakerScreen(sp, level);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            openBookmakerScreen(sp, level);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static void openBookmakerScreen(ServerPlayer sp, Level level) {
        TeyvatRacetrackManager manager = TeyvatRacetrackManager.get(level);
        if (manager == null) return;

        List<String> participantIds = new ArrayList<>();
        for (UmaProfile p : manager.getCurrentParticipants()) {
            participantIds.add(p.getId());
        }

        int stateOrd = manager.getState().ordinal();
        int timer = manager.getStateTimer() / 20;

        TeyvatRacetrackManager.PlayerBet bet = manager.getActiveBet(sp.getUUID());
        String betUmaId = (bet != null && bet.chosenUma() != null) ? bet.chosenUma().getId() : "";
        ItemStack betStack = (bet != null && bet.wageredStack() != null) ? bet.wageredStack() : ItemStack.EMPTY;

        PacketDistributor.sendToPlayer(sp, new OpenUmaRacePayload(
                stateOrd,
                timer,
                participantIds,
                betUmaId,
                betStack
        ));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return net.minecraft.world.level.block.Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }
}
