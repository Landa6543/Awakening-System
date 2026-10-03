package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Consola y Panel de Control de Conducción del Metro (ORV Coche 3707).
 * Bloque decorativo de cabina para ambientación de vagones de metro:
 * - Clic derecho: Hace sonar el timbre/campana de cabina de metro.
 * - Shift + Clic derecho: Abre o cierra de forma sincronizada todas las puertas corredizas conectadas del vagón.
 */
public class SubwayTrainControllerBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SubwayTrainControllerBlock> CODEC = simpleCodec(SubwayTrainControllerBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape CONSOLE_SHAPE = Shapes.or(
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D),
            Block.box(2.0D, 12.0D, 2.0D, 14.0D, 16.0D, 14.0D)
    );

    public SubwayTrainControllerBlock(BlockBehaviour.Properties properties) {
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
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                // Shift + Clic: Apertura / Cierre sincronizado de puertas corredizas del vagón
                boolean toggledDoors = toggleTrainDoors(level, pos, player);
                if (!toggledDoors) {
                    level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.8F, 1.2F);
                    player.displayClientMessage(Component.literal("§6✦ [Cabina Metro Coche 3707] §7No se detectaron puertas de metro cercanas en el vagón."), true);
                }
            } else {
                // Clic normal: Sonido de cabina / campanilla del metro
                level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 1.0F, 1.4F);
                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.8F, 1.6F);
                player.displayClientMessage(Component.literal("§6✦ [Cabina Metro Coche 3707] §eConsola de Mando de Metro §7(Estructura Decorativa ORV).\n§7Presiona §fShift + Clic §7para abrir/cerrar las puertas corredizas del vagón."), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private boolean toggleTrainDoors(Level level, BlockPos controllerPos, Player player) {
        List<BlockPos> doorPositions = new ArrayList<>();
        int radius = 24;
        for (BlockPos p : BlockPos.betweenClosed(controllerPos.offset(-radius, -4, -radius), controllerPos.offset(radius, 4, radius))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof SubwayDoorBlock) {
                doorPositions.add(p.immutable());
            }
        }

        if (doorPositions.isEmpty()) {
            return false;
        }

        BlockState firstState = level.getBlockState(doorPositions.get(0));
        boolean targetOpen = !firstState.getValue(SubwayDoorBlock.OPEN);

        for (BlockPos dp : doorPositions) {
            BlockState s = level.getBlockState(dp);
            if (s.getBlock() instanceof SubwayDoorBlock doorBlock) {
                doorBlock.setDoorOpen(level, dp, s, targetOpen);
            }
        }

        level.playSound(null, controllerPos, targetOpen ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.playSound(null, controllerPos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.0F, 1.4F);
        player.displayClientMessage(Component.literal("§e[Metro Línea 3] §fPuertas del vagón " + (targetOpen ? "§aABIERTAS §fpara pasajeros." : "§cCERRADAS. §f¡Atención a la salida!")), true);
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CONSOLE_SHAPE;
    }
}
