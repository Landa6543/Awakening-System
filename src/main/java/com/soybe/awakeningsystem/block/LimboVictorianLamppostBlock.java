package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Farol Victoriano del Limbo (Victorian Gothic Triple-Lantern Streetlamp of Limbo):
 * Imponente farola monumental victoriana de hierro forjado oscuro del Limbo con tridente de triple linterna.
 * Estructura de 5 bloques de altura completa (Secciones 0..4).
 * Arde con fuego de almas etéreo turquesa/cian (#00F0FF) disipando la densa niebla del purgatorio.
 * Se puede encender y apagar haciendo clic derecho en cualquiera de sus secciones.
 */
public class LimboVictorianLamppostBlock extends Block {

    public static final MapCodec<LimboVictorianLamppostBlock> CODEC = simpleCodec(LimboVictorianLamppostBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty SECTION = IntegerProperty.create("section", 0, 4);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    // Voxel shapes para cada una de las 5 secciones
    private static final VoxelShape SHAPE_BASE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE_COLUMN = Block.box(4.5D, 0.0D, 4.5D, 11.5D, 16.0D, 11.5D);

    private static final VoxelShape SHAPE_UPPER_Z = Block.box(1.0D, 0.0D, 5.0D, 15.0D, 16.0D, 11.0D);
    private static final VoxelShape SHAPE_UPPER_X = Block.box(5.0D, 0.0D, 1.0D, 11.0D, 16.0D, 15.0D);

    private static final VoxelShape SHAPE_CROWN_Z = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 12.0D);
    private static final VoxelShape SHAPE_CROWN_X = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);

    public LimboVictorianLamppostBlock(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> {
            if (!state.getValue(LIT)) return 0;
            int sec = state.getValue(SECTION);
            return switch (sec) {
                case 4 -> 15;
                case 3 -> 12;
                case 2 -> 9;
                case 1 -> 7;
                default -> 5;
            };
        }));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SECTION, 0)
                .setValue(LIT, true));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SECTION, LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int sec = state.getValue(SECTION);
        Direction facing = state.getValue(FACING);
        boolean axisX = facing.getAxis() == Direction.Axis.X;

        return switch (sec) {
            case 0 -> SHAPE_BASE;
            case 1, 2 -> SHAPE_COLUMN;
            case 3 -> axisX ? SHAPE_UPPER_X : SHAPE_UPPER_Z;
            default -> axisX ? SHAPE_CROWN_X : SHAPE_CROWN_Z;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();

        // Verificar que los 4 bloques superiores estén libres y dentro del límite del mundo
        for (int i = 1; i <= 4; i++) {
            BlockPos above = pos.above(i);
            if (above.getY() >= level.getMaxBuildHeight() || !level.getBlockState(above).canBeReplaced(context)) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, facing).setValue(SECTION, 0).setValue(LIT, true);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            boolean lit = state.getValue(LIT);
            for (int i = 1; i <= 4; i++) {
                BlockPos target = pos.above(i);
                level.setBlock(target, this.defaultBlockState()
                        .setValue(FACING, facing)
                        .setValue(SECTION, i)
                        .setValue(LIT, lit), 3);
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int sec = state.getValue(SECTION);
        if (sec == 0) {
            BlockPos below = pos.below();
            return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
        } else {
            BlockState belowState = level.getBlockState(pos.below());
            return belowState.is(this) && belowState.getValue(SECTION) == sec - 1;
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        int sec = state.getValue(SECTION);
        if (direction == Direction.DOWN && sec > 0) {
            if (!neighborState.is(this) || neighborState.getValue(SECTION) != sec - 1) {
                return Blocks.AIR.defaultBlockState();
            }
        } else if (direction == Direction.UP && sec < 4) {
            if (!neighborState.is(this) || neighborState.getValue(SECTION) != sec + 1) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            int sec = state.getValue(SECTION);
            BlockPos basePos = pos.below(sec);
            for (int i = 0; i <= 4; i++) {
                BlockPos partPos = basePos.above(i);
                BlockState partState = level.getBlockState(partPos);
                if (partState.is(this)) {
                    level.setBlock(partPos, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, partPos, Block.getId(partState));
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            toggleLamp(level, pos, state);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    private void toggleLamp(Level level, BlockPos pos, BlockState state) {
        int sec = state.getValue(SECTION);
        BlockPos basePos = pos.below(sec);
        boolean newLit = !state.getValue(LIT);

        for (int i = 0; i <= 4; i++) {
            BlockPos partPos = basePos.above(i);
            BlockState partState = level.getBlockState(partPos);
            if (partState.is(this)) {
                level.setBlock(partPos, partState.setValue(LIT, newLit), 3);
            }
        }

        float pitch = newLit ? 1.0F : 0.75F;
        level.playSound(null, pos, newLit ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, pitch);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT) || state.getValue(SECTION) != 4) {
            return;
        }

        // Emitir sutiles partículas de fuego de alma en las 3 linternas del tridente
        Direction facing = state.getValue(FACING);
        boolean axisX = facing.getAxis() == Direction.Axis.X;

        double cy = pos.getY() + 0.35D;
        double sideOffset = 0.38D;

        // Linterna central
        spawnLanternParticle(level, pos.getX() + 0.5D, cy + 0.1D, pos.getZ() + 0.5D, random);

        // Linternas laterales
        if (axisX) {
            spawnLanternParticle(level, pos.getX() + 0.5D, cy, pos.getZ() + 0.5D - sideOffset, random);
            spawnLanternParticle(level, pos.getX() + 0.5D, cy, pos.getZ() + 0.5D + sideOffset, random);
        } else {
            spawnLanternParticle(level, pos.getX() + 0.5D - sideOffset, cy, pos.getZ() + 0.5D, random);
            spawnLanternParticle(level, pos.getX() + 0.5D + sideOffset, cy, pos.getZ() + 0.5D, random);
        }
    }

    private void spawnLanternParticle(Level level, double x, double y, double z, RandomSource random) {
        if (random.nextFloat() < 0.35F) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + (random.nextDouble() - 0.5D) * 0.08D, y + random.nextDouble() * 0.08D, z + (random.nextDouble() - 0.5D) * 0.08D, 0.0D, 0.005D, 0.0D);
        }
        if (random.nextFloat() < 0.12F) {
            level.addParticle(ParticleTypes.SMOKE, x, y + 0.15D, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
