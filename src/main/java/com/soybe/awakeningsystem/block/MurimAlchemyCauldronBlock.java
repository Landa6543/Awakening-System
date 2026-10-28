package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MurimAlchemyCauldronBlock extends Block {
    public static final MapCodec<MurimAlchemyCauldronBlock> CODEC = simpleCodec(MurimAlchemyCauldronBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 15.0D, 14.0D);

    public MurimAlchemyCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    private static boolean isHeatSource(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE) ||
                below.is(Blocks.CAMPFIRE) || below.is(Blocks.SOUL_CAMPFIRE) ||
                below.is(Blocks.LAVA) || below.is(Blocks.MAGMA_BLOCK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(LIT, isHeatSource(context.getLevel(), context.getClickedPos()));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && level instanceof Level lvl) {
            return state.setValue(LIT, isHeatSource(lvl, currentPos));
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        boolean hasHeat = state.getValue(LIT) || isHeatSource(level, pos);
        ServerLevel sl = (ServerLevel) level;

        if (hasHeat && heldStack.is(SystemItems.WILD_GINSENG.get())) {
            int mandrakeSlot = findItemSlot(player, SystemItems.MANDRAKE_ROOT.get());
            if (mandrakeSlot >= 0) {
                consumeItem(player, heldStack);
                player.getInventory().getItem(mandrakeSlot).shrink(1);
                givePill(player, pos, new ItemStack(SystemItems.MARROW_CLEANSING_PILL.get()));
                playRefineEffects(sl, pos);
                return ItemInteractionResult.SUCCESS;
            }
            int jadeSlot = findItemSlot(player, SystemItems.SPIRIT_JADE.get());
            if (jadeSlot < 0) jadeSlot = findItemSlot(player, SystemItems.SPIRIT_JADE_SHARD.get());
            if (jadeSlot >= 0) {
                consumeItem(player, heldStack);
                player.getInventory().getItem(jadeSlot).shrink(1);
                givePill(player, pos, new ItemStack(SystemItems.SOUL_TRANSFORMATION_PILL.get()));
                playRefineEffects(sl, pos);
                return ItemInteractionResult.SUCCESS;
            }
            player.displayClientMessage(Component.translatable("awakening_system.cauldron.need_mandrake"), true);
            return ItemInteractionResult.FAIL;
        }

        if (hasHeat && heldStack.is(SystemItems.SNOW_LOTUS.get())) {
            int ginsengSlot = findItemSlot(player, SystemItems.THOUSAND_YEAR_GINSENG.get());
            if (ginsengSlot >= 0) {
                consumeItem(player, heldStack);
                player.getInventory().getItem(ginsengSlot).shrink(1);
                givePill(player, pos, new ItemStack(SystemItems.RISING_DRAGON_PILL.get()));
                playRefineEffects(sl, pos);
                return ItemInteractionResult.SUCCESS;
            }
            int jadeSlot = findItemSlot(player, SystemItems.SPIRIT_JADE.get());
            if (jadeSlot < 0) jadeSlot = findItemSlot(player, SystemItems.SPIRIT_JADE_SHARD.get());
            if (jadeSlot >= 0) {
                consumeItem(player, heldStack);
                player.getInventory().getItem(jadeSlot).shrink(1);
                givePill(player, pos, new ItemStack(SystemItems.TRIBULATION_SHIELD_PILL.get()));
                playRefineEffects(sl, pos);
                return ItemInteractionResult.SUCCESS;
            }
            player.displayClientMessage(Component.translatable("awakening_system.cauldron.need_ginseng"), true);
            return ItemInteractionResult.FAIL;
        }

        if (hasHeat && heldStack.is(SystemItems.THOUSAND_YEAR_GINSENG.get())) {
            int jadeSlot = findItemSlot(player, SystemItems.SPIRIT_JADE.get());
            if (jadeSlot < 0) jadeSlot = findItemSlot(player, SystemItems.SPIRIT_JADE_SHARD.get());
            if (jadeSlot >= 0) {
                consumeItem(player, heldStack);
                player.getInventory().getItem(jadeSlot).shrink(1);
                givePill(player, pos, new ItemStack(SystemItems.REALM_BREAKTHROUGH_PILL.get()));
                playRefineEffects(sl, pos);
                return ItemInteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.cauldron.need_jade"), true);
                return ItemInteractionResult.FAIL;
            }
        }

        if (hasHeat && heldStack.is(SystemItems.MANDRAKE_ROOT.get())) {
            int adamSlot = findItemSlot(player, SystemItems.ADAMANTITE_INGOT.get());
            if (adamSlot >= 0) {
                consumeItem(player, heldStack);
                player.getInventory().getItem(adamSlot).shrink(1);
                givePill(player, pos, new ItemStack(SystemItems.BLACK_TORTOISE_PILL.get()));
                playRefineEffects(sl, pos);
                return ItemInteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable("awakening_system.cauldron.need_adamantite"), true);
                return ItemInteractionResult.FAIL;
            }
        }

        if (hasHeat && (heldStack.is(SystemItems.SPIRIT_JADE.get()) || heldStack.is(SystemItems.SPIRIT_JADE_SHARD.get()))) {
            int ginsengSlot = findItemSlot(player, SystemItems.THOUSAND_YEAR_GINSENG.get());
            if (ginsengSlot >= 0) {
                consumeItem(player, heldStack);
                player.getInventory().getItem(ginsengSlot).shrink(1);
                givePill(player, pos, new ItemStack(SystemItems.REALM_BREAKTHROUGH_PILL.get()));
                playRefineEffects(sl, pos);
                return ItemInteractionResult.SUCCESS;
            }
            int snowLotusSlot = findItemSlot(player, SystemItems.SNOW_LOTUS.get());
            if (snowLotusSlot >= 0) {
                consumeItem(player, heldStack);
                player.getInventory().getItem(snowLotusSlot).shrink(1);
                givePill(player, pos, new ItemStack(SystemItems.TRIBULATION_SHIELD_PILL.get()));
                playRefineEffects(sl, pos);
                return ItemInteractionResult.SUCCESS;
            }
            int wildGinsengSlot = findItemSlot(player, SystemItems.WILD_GINSENG.get());
            if (wildGinsengSlot >= 0) {
                consumeItem(player, heldStack);
                player.getInventory().getItem(wildGinsengSlot).shrink(1);
                givePill(player, pos, new ItemStack(SystemItems.SOUL_TRANSFORMATION_PILL.get()));
                playRefineEffects(sl, pos);
                return ItemInteractionResult.SUCCESS;
            }
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new com.soybe.awakeningsystem.OpenMurimAlchemyPayload(pos, hasHeat));
            sp.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return ItemInteractionResult.SUCCESS;
    }

    public static void handleRefinePacket(net.minecraft.server.level.ServerPlayer player, com.soybe.awakeningsystem.MurimAlchemyRefinePayload payload) {
        BlockPos pos = payload.pos();
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) return;
        Level level = player.level();
        if (!(level instanceof ServerLevel sl)) return;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof MurimAlchemyCauldronBlock)) return;

        boolean hasHeat = state.getValue(LIT) || isHeatSource(level, pos);
        if (!hasHeat) {
            player.displayClientMessage(Component.translatable("awakening_system.cauldron.need_fire"), true);
            player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.0F);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.OpenMurimAlchemyPayload(pos, false));
            return;
        }

        switch (payload.recipeIndex()) {
            case 0 -> {
                int s1 = findItemSlot(player, SystemItems.WILD_GINSENG.get());
                int s2 = findItemSlot(player, SystemItems.MANDRAKE_ROOT.get());
                if (s1 >= 0 && s2 >= 0) {
                    player.getInventory().getItem(s1).shrink(1);
                    player.getInventory().getItem(s2).shrink(1);
                    givePill(player, pos, new ItemStack(SystemItems.MARROW_CLEANSING_PILL.get()));
                    playRefineEffects(sl, pos);
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.OpenMurimAlchemyPayload(pos, true));
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.cauldron.missing_ingredients_1"), true);
                }
            }
            case 1 -> {
                int s1 = findItemSlot(player, SystemItems.SNOW_LOTUS.get());
                int s2 = findItemSlot(player, SystemItems.THOUSAND_YEAR_GINSENG.get());
                if (s1 >= 0 && s2 >= 0) {
                    player.getInventory().getItem(s1).shrink(1);
                    player.getInventory().getItem(s2).shrink(1);
                    givePill(player, pos, new ItemStack(SystemItems.RISING_DRAGON_PILL.get()));
                    playRefineEffects(sl, pos);
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.OpenMurimAlchemyPayload(pos, true));
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.cauldron.missing_ingredients_2"), true);
                }
            }
            case 2 -> {
                int s1 = findItemSlot(player, SystemItems.THOUSAND_YEAR_GINSENG.get());
                int s2 = findItemSlot(player, SystemItems.SPIRIT_JADE.get());
                if (s2 < 0) s2 = findItemSlot(player, SystemItems.SPIRIT_JADE_SHARD.get());
                if (s1 >= 0 && s2 >= 0) {
                    player.getInventory().getItem(s1).shrink(1);
                    player.getInventory().getItem(s2).shrink(1);
                    givePill(player, pos, new ItemStack(SystemItems.REALM_BREAKTHROUGH_PILL.get()));
                    playRefineEffects(sl, pos);
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.OpenMurimAlchemyPayload(pos, true));
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.cauldron.missing_ingredients_3"), true);
                }
            }
            case 3 -> {
                int s1 = findItemSlot(player, SystemItems.MANDRAKE_ROOT.get());
                int s2 = findItemSlot(player, SystemItems.ADAMANTITE_INGOT.get());
                if (s1 >= 0 && s2 >= 0) {
                    player.getInventory().getItem(s1).shrink(1);
                    player.getInventory().getItem(s2).shrink(1);
                    givePill(player, pos, new ItemStack(SystemItems.BLACK_TORTOISE_PILL.get()));
                    playRefineEffects(sl, pos);
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.OpenMurimAlchemyPayload(pos, true));
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.cauldron.missing_ingredients_4"), true);
                }
            }
            case 4 -> {
                int s1 = findItemSlot(player, SystemItems.SNOW_LOTUS.get());
                int s2 = findItemSlot(player, SystemItems.SPIRIT_JADE.get());
                if (s2 < 0) s2 = findItemSlot(player, SystemItems.SPIRIT_JADE_SHARD.get());
                if (s1 >= 0 && s2 >= 0) {
                    player.getInventory().getItem(s1).shrink(1);
                    player.getInventory().getItem(s2).shrink(1);
                    givePill(player, pos, new ItemStack(SystemItems.TRIBULATION_SHIELD_PILL.get()));
                    playRefineEffects(sl, pos);
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.OpenMurimAlchemyPayload(pos, true));
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.cauldron.missing_ingredients_5"), true);
                }
            }
            case 5 -> {
                int s1 = findItemSlot(player, SystemItems.WILD_GINSENG.get());
                int s2 = findItemSlot(player, SystemItems.SPIRIT_JADE.get());
                if (s2 < 0) s2 = findItemSlot(player, SystemItems.SPIRIT_JADE_SHARD.get());
                if (s1 >= 0 && s2 >= 0) {
                    player.getInventory().getItem(s1).shrink(1);
                    player.getInventory().getItem(s2).shrink(1);
                    givePill(player, pos, new ItemStack(SystemItems.SOUL_TRANSFORMATION_PILL.get()));
                    playRefineEffects(sl, pos);
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.OpenMurimAlchemyPayload(pos, true));
                } else {
                    player.displayClientMessage(Component.translatable("awakening_system.cauldron.missing_ingredients_6"), true);
                }
            }
        }
    }

    private static int findItemSlot(Player player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                return i;
            }
        }
        return -1;
    }

    private static void consumeItem(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static void givePill(Player player, BlockPos pos, ItemStack pillStack) {
        if (!player.getInventory().add(pillStack)) {
            ItemEntity entity = new ItemEntity(player.level(), pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, pillStack);
            player.level().addFreshEntity(entity);
        }
        player.displayClientMessage(Component.translatable("block.awakening_system.murim_alchemy_cauldron.refined", pillStack.getHoverName()), true);
    }

    private static void playRefineEffects(ServerLevel level, BlockPos pos) {
        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 0.8D;
        double pz = pos.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py, pz, 40, 0.4D, 0.6D, 0.4D, 0.15D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py + 0.2D, pz, 25, 0.3D, 0.4D, 0.3D, 0.05D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py + 0.5D, pz, 15, 0.1D, 0.3D, 0.1D, 0.02D);

        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.4F, 1.2F);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.6F, 1.5F);
        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 1.4F);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double px = pos.getX() + 0.5D;
        double py = pos.getY() + 0.75D;
        double pz = pos.getZ() + 0.5D;

        if (state.getValue(LIT)) {
            if (random.nextFloat() < 0.6F) {
                double rx = px + (random.nextDouble() - 0.5D) * 0.4D;
                double rz = pz + (random.nextDouble() - 0.5D) * 0.4D;
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, rx, py, rz, 0.0D, 0.03D, 0.0D);
            }
            if (random.nextFloat() < 0.35F) {
                level.addParticle(ParticleTypes.ENCHANT, px, py, pz, (random.nextDouble() - 0.5D) * 0.2D, 0.05D, (random.nextDouble() - 0.5D) * 0.2D);
            }
            if (random.nextFloat() < 0.15F) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py - 0.1D, pz, 0.0D, 0.02D, 0.0D);
            }
        }
    }
}
