package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.AABB;
import com.soybe.awakeningsystem.entity.ElementalLordBossEntity;

public class ElementalAltarBlock extends Block {
    public static final MapCodec<ElementalAltarBlock> CODEC = simpleCodec(ElementalAltarBlock::new);
    public static final EnumProperty<ElementalType> ELEMENT = EnumProperty.create("element", ElementalType.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape BASE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 4.0D, 15.0D);
    private static final VoxelShape PILLAR = Block.box(3.0D, 4.0D, 3.0D, 13.0D, 12.0D, 13.0D);
    private static final VoxelShape TOP = Block.box(1.0D, 12.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final VoxelShape SHAPE = Shapes.or(BASE, PILLAR, TOP);

    public ElementalAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ELEMENT, ElementalType.WIND)
                .setValue(ACTIVE, false));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ELEMENT, ACTIVE);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            triggerActivation(sp, pos, state);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            triggerActivation(sp, pos, state);
        }
        return InteractionResult.SUCCESS;
    }

    private static void triggerActivation(ServerPlayer player, BlockPos pos, BlockState state) {
        ServerLevel level = player.serverLevel();
        ElementalType type = state.getValue(ELEMENT);

        if (state.getValue(ACTIVE)) {
            player.displayClientMessage(Component.translatable("awakening_system.altar.elemental_channeling", type.getDisplayName()), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.8F, 1.3F);
            return;
        }

        AABB searchBox = new AABB(pos).inflate(35.0D);
        List<ElementalLordBossEntity> livingLords = level.getEntitiesOfClass(ElementalLordBossEntity.class, searchBox, e -> e.isAlive() && e.getElementalType() == type);
        if (!livingLords.isEmpty()) {
            player.displayClientMessage(Component.translatable("awakening_system.altar.elemental_boss_protecting", livingLords.get(0).getName().getString()), true);
            player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F);
            return;
        }

        level.setBlock(pos, state.setValue(ACTIVE, true), 3);

        level.sendParticles(type.getParticle(),
                pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D,
                50, 0.4D, 0.4D, 0.4D, 0.1D);
        level.sendParticles(ParticleTypes.FLASH,
                pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D,
                2, 0, 0, 0, 0);

        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.2F);
        player.playNotifySound(SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.1F);

        List<BlockPos> altars = findAltarsInComplex(level, pos);
        int activeCount = 0;
        for (BlockPos p : altars) {
            BlockState st = level.getBlockState(p);
            if (st.is(SystemBlocks.ELEMENTAL_ALTAR.get()) && st.getValue(ACTIVE)) {
                activeCount++;
            }
        }

        player.displayClientMessage(Component.translatable("awakening_system.altar.elemental_activated", type.getDisplayName(), activeCount), false);

        if (activeCount >= 4) {
            disperseCentralBarrier(level, pos);

            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    pos.getX() + 0.5D, pos.getY() + 3.0D, pos.getZ() + 0.5D,
                    150, 2.0D, 2.0D, 2.0D, 0.25D);

            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
            player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.2F, 0.8F);

            AwakeningSystemMod.notify(player,
                    "awakening_system.castles.barrier_broken_title",
                    "awakening_system.castles.barrier_broken_desc",
                    0xFFFFD700);

            player.displayClientMessage(Component.translatable("awakening_system.block.elemental_altar.all_seals_broken"), false);
        }
    }

    public static List<BlockPos> findAltarsInComplex(ServerLevel level, BlockPos origin) {
        List<BlockPos> list = new ArrayList<>();
        BlockState originState = level.getBlockState(origin);
        if (!originState.is(SystemBlocks.ELEMENTAL_ALTAR.get())) {
            return list;
        }
        ElementalType type = originState.getValue(ELEMENT);
        BlockPos center = switch (type) {
            case WIND -> origin.south(48);
            case FROST -> origin.east(48);
            case FIRE -> origin.north(48);
            case STORM -> origin.west(48);
        };

        BlockPos[] expected = new BlockPos[]{
                center.north(48),
                center.west(48),
                center.south(48),
                center.east(48)
        };

        for (BlockPos target : expected) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        BlockPos p = target.offset(dx, dy, dz);
                        if (level.getBlockState(p).is(SystemBlocks.ELEMENTAL_ALTAR.get())) {
                            if (!list.contains(p)) {
                                list.add(p);
                            }
                        }
                    }
                }
            }
        }

        if (list.size() < 4) {
            for (int dx = -60; dx <= 60; dx++) {
                for (int dz = -60; dz <= 60; dz++) {
                    for (int dy = -4; dy <= 4; dy++) {
                        BlockPos p = center.offset(dx, dy, dz);
                        if (level.getBlockState(p).is(SystemBlocks.ELEMENTAL_ALTAR.get())) {
                            if (!list.contains(p)) {
                                list.add(p);
                            }
                        }
                    }
                }
            }
        }

        return list;
    }

    public static void disperseCentralBarrier(ServerLevel level, BlockPos origin) {
        BlockState originState = level.getBlockState(origin);
        ElementalType type = originState.is(SystemBlocks.ELEMENTAL_ALTAR.get()) ? originState.getValue(ELEMENT) : ElementalType.WIND;
        BlockPos center = switch (type) {
            case WIND -> origin.south(48);
            case FROST -> origin.east(48);
            case FIRE -> origin.north(48);
            case STORM -> origin.west(48);
        };

        for (int dx = -18; dx <= 18; dx++) {
            for (int dz = -18; dz <= 18; dz++) {
                for (int dy = -4; dy <= 45; dy++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    if (level.getBlockState(p).is(SystemBlocks.ELEMENTAL_BARRIER.get())) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        ElementalType type = state.getValue(ELEMENT);
        if (random.nextInt(state.getValue(ACTIVE) ? 2 : 5) == 0) {
            double px = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double py = pos.getY() + 1.05D;
            double pz = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            level.addParticle(type.getParticle(), px, py, pz, 0.0D, 0.03D, 0.0D);
        }
    }
}
