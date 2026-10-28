package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.DungeonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bloque maestro "Núcleo de Mazmorra" (Dungeon Core).
 * Sustituye completamente el Respawn Anchor del Nether al derrotar jefes de mazmorra.
 * Cuenta con 3 estados dinámicos (Locked palpitante, Activated cinemático radiante, Claimed dormido).
 */
public class DungeonCoreBlock extends Block {
    public static final MapCodec<DungeonCoreBlock> CODEC = simpleCodec(DungeonCoreBlock::new);

    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    public static final BooleanProperty CLAIMED = BooleanProperty.create("claimed");

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(2.0D, 4.0D, 2.0D, 14.0D, 8.0D, 14.0D),
            Block.box(3.0D, 8.0D, 3.0D, 13.0D, 10.0D, 13.0D),
            Block.box(5.0D, 10.0D, 5.0D, 11.0D, 16.0D, 11.0D)
    );

    public DungeonCoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ACTIVATED, false)
                .setValue(CLAIMED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return handleInteraction(state, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        ItemInteractionResult res = handleInteraction(state, level, pos, player);
        return res == ItemInteractionResult.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private ItemInteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer sp) {
            // First allow DungeonManager to handle per-player claiming / reward distribution
            if (DungeonManager.onRightClickChest(sp, pos)) {
                return ItemInteractionResult.SUCCESS;
            }

            if (state.getValue(CLAIMED)) {
                sp.sendSystemMessage(Component.translatable("block.awakening_system.dungeon_core.claimed"), true);
                return ItemInteractionResult.SUCCESS;
            } else {
                sp.sendSystemMessage(Component.translatable("block.awakening_system.dungeon_core.resonating"), true);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.75D;
        double z = pos.getZ() + 0.5D;

        boolean claimed = state.getValue(CLAIMED);
        boolean activated = state.getValue(ACTIVATED);

        if (!claimed) {
            if (activated) {
                level.addParticle(ParticleTypes.TOTEM_OF_UNDYING, x + (random.nextDouble() - 0.5D) * 0.4D, y + 0.3D, z + (random.nextDouble() - 0.5D) * 0.4D, 0.0D, 0.08D, 0.0D);
                level.addParticle(ParticleTypes.CRIT, x + (random.nextDouble() - 0.5D) * 0.5D, y + 0.2D, z + (random.nextDouble() - 0.5D) * 0.5D, 0.0D, 0.05D, 0.0D);
            } else {
                if (random.nextInt(2) == 0) {
                    level.addParticle(ParticleTypes.PORTAL, x + (random.nextDouble() - 0.5D) * 0.6D, y + random.nextDouble() * 0.4D, z + (random.nextDouble() - 0.5D) * 0.6D, (random.nextDouble() - 0.5D) * 0.2D, 0.05D, (random.nextDouble() - 0.5D) * 0.2D);
                }
                if (random.nextInt(4) == 0) {
                    level.addParticle(ParticleTypes.ENCHANT, x + (random.nextDouble() - 0.5D) * 0.8D, y + 0.4D, z + (random.nextDouble() - 0.5D) * 0.8D, 0.0D, 0.1D, 0.0D);
                }
            }
        } else {
            if (random.nextInt(6) == 0) {
                level.addParticle(ParticleTypes.SMOKE, x, y + 0.2D, z, 0.0D, 0.01D, 0.0D);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED, CLAIMED);
    }
}
