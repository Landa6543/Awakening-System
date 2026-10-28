package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bloque Flotante de Óculos de Teyvat (Anemóculus, Geóculus, Electróculus, Dendróculus).
 * Los jugadores pueden caminar o planear a través de él para recolectarlo automáticamente como en Genshin Impact.
 */
public class OculusBlock extends Block {
    public static final MapCodec<OculusBlock> CODEC = simpleCodec(OculusBlock::new);

    private static final VoxelShape SHAPE = Block.box(3.0D, 1.0D, 3.0D, 13.0D, 15.0D, 13.0D);

    public OculusBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            collectOculus(level, pos, player);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp && !player.isCreative()) {
            collectOculus(level, pos, sp);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void collectOculus(Level level, BlockPos pos, ServerPlayer player) {
        // Evita doble recolección simultánea si varios ticks llaman a entityInside
        if (!level.getBlockState(pos).is(this)) return;

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        Item oculusItem;
        String oculusTitle;

        if (this == SystemBlocks.GEOCULUS_BLOCK.get()) {
            oculusItem = SystemItems.GEOCULUS.get();
            oculusTitle = "§6[Geoculus]";
        } else if (this == SystemBlocks.ELECTROCULUS_BLOCK.get()) {
            oculusItem = SystemItems.ELECTROCULUS.get();
            oculusTitle = "§d[Electroculus]";
        } else if (this == SystemBlocks.DENDROCULUS_BLOCK.get()) {
            oculusItem = SystemItems.DENDROCULUS.get();
            oculusTitle = "§2[Dendroculus]";
        } else {
            oculusItem = SystemItems.ANEMOCULUS.get();
            oculusTitle = "§a[Anemoculus]";
        }

        ItemStack stack = new ItemStack(oculusItem, 1);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }

        // Sonidos celestiales de tintineo y campana
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.8F);
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.5F);

        // Explosión de partículas brillantes
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 30, 0.35D, 0.35D, 0.35D, 0.15D);
            sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 20, 0.25D, 0.25D, 0.25D, 0.08D);
        }

        // Mensaje discreto en la barra de acción (CERO spam en el chat)
        player.displayClientMessage(Component.translatable("block.awakening_system.oculus.obtained", oculusTitle), true);
    }
}
