package com.soybe.awakeningsystem.trial;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Respiradero Anemo: una corriente de viento que eleva a quien se ponga encima, hasta {@link #HEIGHT}
 * bloques. En la sala Elemental lleva a la cornisa de la bóveda.
 */
public class AnemoVentBlock extends BaseEntityBlock {
    public static final MapCodec<AnemoVentBlock> CODEC = simpleCodec(AnemoVentBlock::new);
    static final int HEIGHT = 10;

    public AnemoVentBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VentEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // en ambos lados: el cliente mueve al jugador local sin tirones
        return createTickerHelper(type, TrialRegistry.ANEMO_VENT_ENTITY.get(), VentEntity::tick);
    }

    public static class VentEntity extends BlockEntity {
        public VentEntity(BlockPos pos, BlockState state) {
            super(TrialRegistry.ANEMO_VENT_ENTITY.get(), pos, state);
        }

        static void tick(Level level, BlockPos pos, BlockState state, VentEntity be) {
            AABB column = new AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1 + HEIGHT, pos.getZ() + 1).inflate(0.2D, 0, 0.2D);
            for (Entity e : level.getEntitiesOfClass(Entity.class, column, en -> !en.isSpectator())) {
                Vec3 v = e.getDeltaMovement();
                e.setDeltaMovement(v.x, Math.min(0.7D, v.y + 0.14D), v.z);
                e.resetFallDistance();
                e.hurtMarked = true;
            }
            if (level instanceof ServerLevel sl && level.getGameTime() % 4 == 0) {
                sl.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 2, 0.2D, 0.1D, 0.2D, 0.15D);
            }
        }
    }
}
