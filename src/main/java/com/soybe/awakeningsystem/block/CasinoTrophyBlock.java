package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.block.entity.CasinoTrophyBlockEntity;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CasinoTrophyBlock extends BaseEntityBlock {
    public static final MapCodec<CasinoTrophyBlock> CODEC = simpleCodec(CasinoTrophyBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty TROPHY_TYPE = IntegerProperty.create("trophy_type", 1, 5);

    protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 15.5D, 13.0D);

    private final int fixedTrophyType;

    public CasinoTrophyBlock(BlockBehaviour.Properties properties) {
        this(properties, 1);
    }

    public CasinoTrophyBlock(BlockBehaviour.Properties properties, int fixedTrophyType) {
        super(properties);
        this.fixedTrophyType = Math.max(1, Math.min(5, fixedTrophyType));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TROPHY_TYPE, this.fixedTrophyType));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TROPHY_TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int type = this.fixedTrophyType;
        ItemStack stack = context.getItemInHand();
        net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData != null) {
            net.minecraft.nbt.CompoundTag tag = customData.copyTag();
            if (tag.contains("trophy_type")) {
                type = Math.max(1, Math.min(5, tag.getInt("trophy_type")));
            }
        }
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(TROPHY_TYPE, type);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        CasinoTrophyBlockEntity be = new CasinoTrophyBlockEntity(pos, state);
        be.setTrophyType(state.getValue(TROPHY_TYPE));
        return be;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof CasinoTrophyBlockEntity be) {
            int type = this.fixedTrophyType;
            net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null) {
                net.minecraft.nbt.CompoundTag tag = customData.copyTag();
                if (tag.contains("trophy_type")) {
                    type = tag.getInt("trophy_type");
                }
                if (tag.contains("winner_name")) be.setWinnerName(tag.getString("winner_name"));
                if (tag.contains("achievement_desc")) be.setAchievementDesc(tag.getString("achievement_desc"));
                if (tag.contains("awarded_date")) be.setAwardedDate(tag.getString("awarded_date"));
            } else if (placer instanceof Player p) {
                be.setWinnerName(p.getName().getString());
                be.setAwardedDate(java.time.LocalDate.now().toString());
            }
            be.setTrophyType(type);
            level.setBlock(pos, state.setValue(TROPHY_TYPE, type), 3);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return handleInspect(level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isCreative() && player.isShiftKeyDown()) {
            int current = state.getValue(TROPHY_TYPE);
            int next = (current % 5) + 1;
            level.setBlock(pos, state.setValue(TROPHY_TYPE, next), 3);
            if (level.getBlockEntity(pos) instanceof CasinoTrophyBlockEntity be) {
                be.setTrophyType(next);
                player.displayClientMessage(Component.translatable("block.awakening_system.casino_trophy.changed", be.getTrophyTitle()), true);
            }
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.8F, 1.2F);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        ItemInteractionResult res = handleInspect(level, pos, player);
        return res == ItemInteractionResult.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private ItemInteractionResult handleInspect(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof CasinoTrophyBlockEntity be) {
            if (!level.isClientSide()) {
                String title = be.getTrophyTitle();
                String winner = be.getWinnerName().isEmpty() ? "A Legendary Master" : be.getWinnerName();
                String desc = be.getAchievementDesc().isEmpty() ? "Championship Feat in Parlor Games" : be.getAchievementDesc();
                String date = be.getAwardedDate().isEmpty() ? "Golden Age of Teyvat" : be.getAwardedDate();

                player.sendSystemMessage(Component.literal("§6§l✦==============================✦"));
                player.sendSystemMessage(Component.literal("§6§l🏆 " + title.toUpperCase() + " 🏆"));
                player.sendSystemMessage(Component.literal("§e✦ Champion: §f" + winner));
                player.sendSystemMessage(Component.literal("§b✦ Feat: §7" + desc));
                player.sendSystemMessage(Component.literal("§8✦ Awarded: §7" + date));
                player.sendSystemMessage(Component.literal("§6§l✦==============================✦"));

                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.6F);
                level.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 0.6F, 1.4F);
            } else {
                for (int i = 0; i < 12; i++) {
                    double px = pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.4D;
                    double py = pos.getY() + 0.8D + level.random.nextDouble() * 0.4D;
                    double pz = pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.4D;
                    level.addParticle(ParticleTypes.WAX_ON, px, py, pz, 0, 0.05, 0);
                    level.addParticle(ParticleTypes.CRIT, px, py, pz, 0, 0.03, 0);
                }
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.4D;
            double y = pos.getY() + 0.7D + random.nextDouble() * 0.3D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.4D;
            level.addParticle(ParticleTypes.WAX_ON, x, y, z, 0.0D, 0.02D, 0.0D);
            if (random.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.CRIT, x, y, z, 0.0D, 0.04D, 0.0D);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(this);
        if (level.getBlockEntity(pos) instanceof CasinoTrophyBlockEntity be) {
            if (!be.getWinnerName().isEmpty() || !be.getAchievementDesc().isEmpty()) {
                net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                tag.putInt("trophy_type", this.fixedTrophyType);
                tag.putString("winner_name", be.getWinnerName());
                tag.putString("achievement_desc", be.getAchievementDesc());
                tag.putString("awarded_date", be.getAwardedDate());
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("§6🏆 " + be.getTrophyTitle()));
            }
        }
        return stack;
    }

    @Override
    protected java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        BlockEntity be = builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        ItemStack stack = new ItemStack(this);
        if (be instanceof CasinoTrophyBlockEntity trophyBe && (!trophyBe.getWinnerName().isEmpty() || !trophyBe.getAchievementDesc().isEmpty())) {
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            tag.putInt("trophy_type", this.fixedTrophyType);
            tag.putString("winner_name", trophyBe.getWinnerName());
            tag.putString("achievement_desc", trophyBe.getAchievementDesc());
            tag.putString("awarded_date", trophyBe.getAwardedDate());
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("§6🏆 " + trophyBe.getTrophyTitle()));
        }
        return java.util.List.of(stack);
    }
}
