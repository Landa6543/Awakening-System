package com.soybe.awakeningsystem.block;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.entity.CustomPlayerPlushieBlockEntity;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CustomPlayerPlushieBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final java.util.UUID DEV_DEFAULT_UUID = java.util.UUID.fromString("9c6e2002-2a3d-4a19-bc57-ecd32c1f129d");
    public static final java.util.UUID SCHVIS_UUID = java.util.UUID.fromString("dddf8989-c266-4b5f-b91a-987f9b3e7c64");
    public static final java.util.UUID TECHIDNA_UUID = java.util.UUID.fromString("87f53b32-38b1-4233-9209-8da99fec7315");
    public static final java.util.UUID TOMCRAFT_UUID = java.util.UUID.fromString("e846a525-b14a-4f6a-963e-bf8d2f2db519");
    public static final java.util.UUID NANDACHAN_UUID = java.util.UUID.fromString("21115ef9-68a9-4612-ac2c-2b27d7527849");
    public static final java.util.UUID MISTIC_POTATO_UUID = java.util.UUID.fromString("936dab1b-f931-45b7-9f6d-e9ad2430e66a");

    public static ResolvableProfile resolveNamedProfile(String name) {
        if ("Dev".equalsIgnoreCase(name) || "LanderAlo".equalsIgnoreCase(name)) {
            return new ResolvableProfile(java.util.Optional.of("LanderAlo"), java.util.Optional.of(DEV_DEFAULT_UUID), new com.mojang.authlib.properties.PropertyMap());
        }
        if ("Schvis".equalsIgnoreCase(name)) {
            return new ResolvableProfile(java.util.Optional.of("Schvis"), java.util.Optional.of(SCHVIS_UUID), new com.mojang.authlib.properties.PropertyMap());
        }
        if ("Techidnaaaa_".equalsIgnoreCase(name) || "Techidna".equalsIgnoreCase(name)) {
            return new ResolvableProfile(java.util.Optional.of("Techidnaaaa_"), java.util.Optional.of(TECHIDNA_UUID), new com.mojang.authlib.properties.PropertyMap());
        }
        if ("TomCraft2003".equalsIgnoreCase(name) || "TomCraft".equalsIgnoreCase(name)) {
            return new ResolvableProfile(java.util.Optional.of("TomCraft2003"), java.util.Optional.of(TOMCRAFT_UUID), new com.mojang.authlib.properties.PropertyMap());
        }
        if ("Nandachan".equalsIgnoreCase(name)) {
            return new ResolvableProfile(java.util.Optional.of("Nandachan"), java.util.Optional.of(NANDACHAN_UUID), new com.mojang.authlib.properties.PropertyMap());
        }
        if ("MisticPotato".equalsIgnoreCase(name) || "Mistic_Potato".equalsIgnoreCase(name) || "Potato".equalsIgnoreCase(name)) {
            return new ResolvableProfile(java.util.Optional.of("MisticPotato"), java.util.Optional.of(MISTIC_POTATO_UUID), new com.mojang.authlib.properties.PropertyMap());
        }
        if (StringUtil.isValidPlayerName(name)) {
            return new ResolvableProfile(java.util.Optional.of(name), java.util.Optional.empty(), new com.mojang.authlib.properties.PropertyMap());
        }
        return new ResolvableProfile(java.util.Optional.of("LanderAlo"), java.util.Optional.of(DEV_DEFAULT_UUID), new com.mojang.authlib.properties.PropertyMap());
    }

    protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 13.0D, 13.0D);

    public CustomPlayerPlushieBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(CustomPlayerPlushieBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomPlayerPlushieBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.CUSTOM_PLAYER_PLUSHIE.get(), CustomPlayerPlushieBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof CustomPlayerPlushieBlockEntity plushie) {
            ResolvableProfile stackProfile = stack.get(DataComponents.PROFILE);
            if (stackProfile != null) {
                plushie.setOwner(stackProfile);
            } else if (stack.has(DataComponents.CUSTOM_NAME)) {
                String name = stack.getHoverName().getString();
                plushie.setOwner(resolveNamedProfile(name));
                plushie.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
            } else if (placer instanceof Player player) {
                plushie.setOwner(new ResolvableProfile(player.getGameProfile()));
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.NAME_TAG) && stack.has(DataComponents.CUSTOM_NAME)) {
            if (level.getBlockEntity(pos) instanceof CustomPlayerPlushieBlockEntity plushie) {
                String newName = stack.getHoverName().getString();
                plushie.setOwner(resolveNamedProfile(newName));
                plushie.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8F, 1.2F);
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 8, 0.2D, 0.2D, 0.2D, 0.05D);
                }
                player.displayClientMessage(Component.translatable("block.awakening_system.plushie.skin_set", newName), true);
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CustomPlayerPlushieBlockEntity plushie) {
            plushie.triggerSquash();
            level.blockEvent(pos, this, 1, 0);
            level.playSound(null, pos, com.soybe.awakeningsystem.ModSounds.PLUSHIE_SQUISH.get(), SoundSource.BLOCKS, 1.0F, 0.95F + level.random.nextFloat() * 0.15F);
            level.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 0.85F, 1.25F + level.random.nextFloat() * 0.2F);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, pos.getX() + 0.5D, pos.getY() + 0.85D, pos.getZ() + 0.5D, 4, 0.16D, 0.12D, 0.16D, 0.05D);
            }

            player.displayClientMessage(Component.translatable("block.awakening_system.plushie.owner_view", plushie.getOwnerName()), true);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof CustomPlayerPlushieBlockEntity plushie) {
            if (plushie.getOwnerProfile() != null) {
                stack.set(DataComponents.PROFILE, plushie.getOwnerProfile());
            }
            if (plushie.getCustomName() != null) {
                stack.set(DataComponents.CUSTOM_NAME, plushie.getCustomName());
            }
        }
        return stack;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == 1) {
            if (level.isClientSide()) {
                if (level.getBlockEntity(pos) instanceof CustomPlayerPlushieBlockEntity plushie) {
                    plushie.triggerSquash();
                }
            }
            return true;
        }
        return super.triggerEvent(state, level, pos, id, param);
    }
}
