package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ChristmasTreeBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private boolean hasGift = true;
    private long lastClaimDay = -1L;

    public ChristmasTreeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CHRISTMAS_TREE.get(), pos, blockState);
    }

    public boolean hasGift() {
        return hasGift;
    }

    public void setHasGift(boolean hasGift) {
        this.hasGift = hasGift;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public InteractionResult interact(Player player) {
        if (level == null) return InteractionResult.PASS;

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel sl)) {
            return InteractionResult.PASS;
        }

        if (!com.soybe.awakeningsystem.seasonal.SeasonalEventManager.isChristmasActive()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("awakening_system.msg.el_arbol_de_navidad_descansa_en"),
                    true
            );
            sl.playSound(null, worldPosition, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.8F, 1.2F);
            return InteractionResult.SUCCESS;
        }

        if (!hasGift) {
            serverPlayer.displayClientMessage(
                    Component.translatable("awakening_system.msg.las_ramas_del_arbol_de_navidad"),
                    true
            );
            sl.playSound(null, worldPosition, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.8F, 1.2F);
            return InteractionResult.SUCCESS;
        }

        // Claim gifts!
        setHasGift(false);
        this.lastClaimDay = sl.getDayTime() / 24000L;
        setChanged();

        double bx = worldPosition.getX() + 0.5D;
        double by = worldPosition.getY() + 0.2D;
        double bz = worldPosition.getZ() + 0.5D;

        // Sounds & Spectacular celebratory particles
        sl.playSound(null, worldPosition, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.2F, 1.0F);
        sl.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 1.2F);
        sl.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.1F);

        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, bx, by + 0.5D, bz, 35, 0.6D, 0.5D, 0.6D, 0.15D);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, bx, by + 0.5D, bz, 40, 0.7D, 0.6D, 0.7D, 0.08D);
        sl.sendParticles(ParticleTypes.FIREWORK, bx, by + 1.2D, bz, 20, 0.4D, 0.4D, 0.4D, 0.1D);

        // 1. Give Christmas Gift Box
        if (SystemItems.CHRISTMAS_GIFT_BOX != null && SystemItems.CHRISTMAS_GIFT_BOX.isBound()) {
            ItemStack box = new ItemStack(SystemItems.CHRISTMAS_GIFT_BOX.value(), 1);
            if (!player.getInventory().add(box)) player.drop(box, false);
        }

        // 2. Give Festive Confectionery
        int sweets = 2 + sl.random.nextInt(3);
        for (int i = 0; i < sweets; i++) {
            ItemStack sweet = getRandomSweet(sl);
            if (!player.getInventory().add(sweet)) player.drop(sweet, false);
        }

        // 3. Give Mora / Coins
        int coins = 100 + sl.random.nextInt(200);
        SystemData data = SystemEvents.data(serverPlayer);
        if (data != null) {
            data.addCoins(coins);
            serverPlayer.sendSystemMessage(Component.translatable("awakening_system.msg.monedas_del_sistema_regalo_diario_del", String.valueOf(coins)).withStyle(ChatFormatting.GOLD));
            SystemEvents.grantFable(serverPlayer, "fable_seasonal_miracle");
        }

        // 4. Rare 5% chance for Christmas Sword directly from the tree!
        if (sl.random.nextFloat() < 0.05F && SystemItems.XMAS_TREE_SWORD != null && SystemItems.XMAS_TREE_SWORD.isBound()) {
            ItemStack sword = new ItemStack(SystemItems.XMAS_TREE_SWORD.value());
            if (!player.getInventory().add(sword)) player.drop(sword, false);
            serverPlayer.sendSystemMessage(Component.translatable("awakening_system.msg.milagro_navideno_bajo_el_arbol_encontraste")
                    .withStyle(ChatFormatting.GOLD));
        }

        serverPlayer.sendSystemMessage(Component.translatable("awakening_system.msg.festividad_navidena_has_abierto_los_regalos")
                .withStyle(ChatFormatting.GREEN));

        return InteractionResult.SUCCESS;
    }

    private static ItemStack getRandomSweet(ServerLevel sl) {
        int r = sl.random.nextInt(5);
        var item = switch (r) {
            case 0 -> SystemItems.GINGERBREAD_COOKIE != null && SystemItems.GINGERBREAD_COOKIE.isBound() ? SystemItems.GINGERBREAD_COOKIE.value() : Items.COOKIE;
            case 1 -> SystemItems.CANDY_CANE != null && SystemItems.CANDY_CANE.isBound() ? SystemItems.CANDY_CANE.value() : Items.SUGAR;
            case 2 -> SystemItems.HOT_CHOCOLATE != null && SystemItems.HOT_CHOCOLATE.isBound() ? SystemItems.HOT_CHOCOLATE.value() : Items.HONEY_BOTTLE;
            case 3 -> SystemItems.ALMOND_NOUGAT != null && SystemItems.ALMOND_NOUGAT.isBound() ? SystemItems.ALMOND_NOUGAT.value() : Items.PUMPKIN_PIE;
            default -> SystemItems.POLAR_PEPPERMINT != null && SystemItems.POLAR_PEPPERMINT.isBound() ? SystemItems.POLAR_PEPPERMINT.value() : Items.APPLE;
        };
        return new ItemStack(item, 1 + sl.random.nextInt(2));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChristmasTreeBlockEntity be) {
        if (level.isClientSide) return;

        // Check day change every 100 ticks (5 seconds)
        if (level.getGameTime() % 100L == 0L) {
            if (!be.hasGift) {
                long currentDay = level.getDayTime() / 24000L;
                if (currentDay > be.lastClaimDay) {
                    be.hasGift = true;
                    be.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);

                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D,
                                15, 0.4D, 0.2D, 0.4D, 0.05D);
                        sl.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.4F);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("HasGift", this.hasGift);
        tag.putLong("LastClaimDay", this.lastClaimDay);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("HasGift")) {
            this.hasGift = tag.getBoolean("HasGift");
        } else {
            this.hasGift = true;
        }
        this.lastClaimDay = tag.getLong("LastClaimDay");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithFullMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idleController", 0, state -> {
            state.getController().setAnimation(IDLE_ANIM);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
