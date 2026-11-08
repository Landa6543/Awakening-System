package com.soybe.awakeningsystem.trial;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Trampa de flechas mágicas: una piedra rúnica que dispara flechas hechizadas (ralentizan y hacen
 * brillar al objetivo) a cualquier cazador que se cruce delante de ella.
 */
public class RuneArrowTrapBlock extends BaseEntityBlock {
    public static final MapCodec<RuneArrowTrapBlock> CODEC = simpleCodec(RuneArrowTrapBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final int RANGE = 7;
    private static final int COOLDOWN = 28;

    public RuneArrowTrapBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Entity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, TrialRegistry.ARROW_TRAP_ENTITY.get(), Entity::serverTick);
    }

    public static class Entity extends BlockEntity {
        private int cooldown = 0;

        public Entity(BlockPos pos, BlockState state) {
            super(TrialRegistry.ARROW_TRAP_ENTITY.get(), pos, state);
        }

        static void serverTick(Level level, BlockPos pos, BlockState state, Entity be) {
            if (be.cooldown > 0) {
                be.cooldown--;
                return;
            }
            if (!(level instanceof ServerLevel sl) || level.getGameTime() % 5 != 0) return;
            Direction facing = state.getValue(FACING);
            BlockPos front = pos.relative(facing);
            AABB lane = new AABB(front).expandTowards(facing.getStepX() * (RANGE - 1), 1, facing.getStepZ() * (RANGE - 1));

            for (ServerPlayer player : sl.getEntitiesOfClass(ServerPlayer.class, lane)) {
                if (player.isSpectator() || player.isCreative() || !player.isAlive()) continue;
                Vec3 muzzle = Vec3.atCenterOf(pos).add(facing.getStepX() * 0.6D, 0.0D, facing.getStepZ() * 0.6D);
                Vec3 target = player.getEyePosition().subtract(0, 0.4D, 0);
                // no dispara a través de paredes
                if (sl.clip(new ClipContext(muzzle, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.BLOCK) continue;

                Arrow arrow = new Arrow(sl, muzzle.x, muzzle.y, muzzle.z, new ItemStack(Items.ARROW), null);
                arrow.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                arrow.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                arrow.setBaseDamage(3.0D);
                Vec3 dir = target.subtract(muzzle);
                arrow.shoot(dir.x, dir.y, dir.z, 1.7F, 1.5F);
                sl.addFreshEntity(arrow);
                sl.playSound(null, pos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.3F);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, muzzle.x, muzzle.y, muzzle.z, 6, 0.1D, 0.1D, 0.1D, 0.02D);
                be.cooldown = COOLDOWN;
                return;
            }
        }
    }
}
