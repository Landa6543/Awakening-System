package com.soybe.awakeningsystem.block.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.compat.CuriosCompat;
import com.soybe.awakeningsystem.network.ClearDeathWaypointPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * Entidad de Bloque para la Lápida del Sistema (Deadlock Tombstone).
 * Almacena el inventario completo (41 ranuras), accesorios Curios API, y experiencia acumulada.
 * Gestiona el controlador de animaciones GeckoLib 4 ('spawn', 'idle' y 'breakdown').
 */
public class TombstoneBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final RawAnimation SPAWN_ANIM = RawAnimation.begin().thenPlay("spawn").thenLoop("idle");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation BREAKDOWN_ANIM = RawAnimation.begin().thenPlay("breakdown");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    // Ranuras: 36 (main + hotbar) + 4 (armadura) + 1 (offhand) = 41
    private final NonNullList<ItemStack> storedInventory = NonNullList.withSize(41, ItemStack.EMPTY);
    private CompoundTag storedCurios = new CompoundTag();
    private int storedExpLevel = 0;
    private int storedTotalExp = 0;
    private float storedExpProgress = 0.0F;

    private UUID ownerUUID = null;
    private String ownerName = "";
    private boolean breakingDown = false;
    private int breakdownTicks = 0;
    private int spawnTicks = 45; // 2.25s para la animación de emergencia inicial
    private int idleSoundTicks = 0;

    public TombstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOMBSTONE.get(), pos, state);
    }

    public Optional<UUID> getOwnerUUID() {
        return Optional.ofNullable(ownerUUID);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
    }

    public String getOwnerName() {
        return ownerName != null ? ownerName : "";
    }

    public void setOwnerName(String name) {
        this.ownerName = name != null ? name : "";
        setChanged();
    }

    public boolean isBreakingDown() {
        return this.breakingDown;
    }

    public void setBreakingDown(boolean breakingDown) {
        this.breakingDown = breakingDown;
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public NonNullList<ItemStack> getStoredInventory() {
        return this.storedInventory;
    }

    public void setStoredCurios(CompoundTag tag) {
        this.storedCurios = tag != null ? tag.copy() : new CompoundTag();
        setChanged();
    }

    public void setStoredExperience(int totalExp, int level, float progress) {
        this.storedTotalExp = totalExp;
        this.storedExpLevel = level;
        this.storedExpProgress = progress;
        setChanged();
    }

    public InteractionResult interact(Player player) {
        if (this.isBreakingDown()) {
            return InteractionResult.SUCCESS;
        }

        if (this.level != null && this.level.isClientSide) {
            this.breakingDown = true;
            return InteractionResult.SUCCESS;
        }

        if (this.level != null && !this.level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Optional<UUID> owner = getOwnerUUID();
            boolean isOwner = owner.isPresent() && owner.get().equals(serverPlayer.getUUID());
            boolean isAdmin = serverPlayer.hasPermissions(2) || serverPlayer.isCreative();

            if (!isOwner && !isAdmin) {
                String name = getOwnerName();
                serverPlayer.sendSystemMessage(
                        Component.translatable("awakening_system.tombstone.not_owner", name.isEmpty() ? "Desconocido" : name)
                );
                return InteractionResult.SUCCESS;
            }

            reclaim(serverPlayer);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(this.level != null && this.level.isClientSide);
    }

    public void reclaim(ServerPlayer player) {
        setBreakingDown(true);

        if (this.level != null) {
            // Sonido de ruptura espectral de la lápida y resonancia del alma (alcance cercano)
            this.level.playSound(null, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D,
                    ModSounds.TOMBSTONE_BREAKDOWN.get(), SoundSource.BLOCKS, 0.85F, 1.0F);
            this.level.playSound(null, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D,
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.3F);
        }

        // 1. Restituir inventario exactamente en sus ranuras correspondientes
        var inv = player.getInventory();
        for (int i = 0; i < storedInventory.size() && i < inv.getContainerSize(); i++) {
            ItemStack saved = storedInventory.get(i);
            if (!saved.isEmpty()) {
                ItemStack current = inv.getItem(i);
                if (current.isEmpty()) {
                    inv.setItem(i, saved.copy());
                } else {
                    if (!player.getInventory().add(saved.copy())) {
                        player.drop(saved.copy(), false);
                    }
                }
            }
        }
        storedInventory.clear();

        // 2. Restituir accesorios de Curios API
        if (!storedCurios.isEmpty()) {
            CuriosCompat.restoreCurios(player, storedCurios);
            storedCurios = new CompoundTag();
        }

        // 3. Restituir experiencia acumulada
        if (this.storedTotalExp > 0 || this.storedExpLevel > 0) {
            player.setExperienceLevels(this.storedExpLevel);
            player.setExperiencePoints((int) (this.storedExpProgress * player.getXpNeededForNextLevel()));
            this.storedTotalExp = 0;
            this.storedExpLevel = 0;
            this.storedExpProgress = 0.0F;
        }

        // 4. Salir de Modo Fantasma
        SystemData data = SystemEvents.data(player);
        data.setGhostMode(false);
        data.setTombstonePos(null);
        data.setTombstoneDimension("");
        player.getPersistentData().remove("awakening_ghost_mode");
        AwakeningSystemMod.sync(player);

        // 5. Eliminar marcador de muerte en Diorama y Awakening System en el cliente
        PacketDistributor.sendToPlayer(player, new ClearDeathWaypointPayload());

        // Mensaje de éxito del Sistema
        player.sendSystemMessage(Component.translatable("awakening_system.tombstone.reclaimed"));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TombstoneBlockEntity tombstone) {
        if (tombstone.spawnTicks > 0) {
            tombstone.spawnTicks--;
        }

        if (level.isClientSide) {
            // Sonido ambiente en bucle espectral únicamente si hay un jugador cerca (a menos de 8 bloques)
            if (tombstone.spawnTicks <= 0 && !tombstone.isBreakingDown()) {
                if (tombstone.idleSoundTicks % 60 == 0) {
                    Player nearbyPlayer = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8.0D, false);
                    if (nearbyPlayer != null) {
                        level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                                ModSounds.TOMBSTONE_IDLE.get(), SoundSource.BLOCKS, 0.45F, 1.0F, false);
                    }
                }
                tombstone.idleSoundTicks++;
            }
        } else if (tombstone.isBreakingDown()) {
            tombstone.breakdownTicks++;
            // La animación breakdown dura 1.68 segundos (33 ticks)
            if (tombstone.breakdownTicks >= 33) {
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.hasUUID("owner_uuid")) {
            this.ownerUUID = tag.getUUID("owner_uuid");
        }
        if (tag.contains("owner_name")) {
            this.ownerName = tag.getString("owner_name");
        }
        this.breakingDown = tag.getBoolean("breaking_down");
        this.breakdownTicks = tag.getInt("breakdown_ticks");
        this.spawnTicks = tag.getInt("spawn_ticks");

        this.storedInventory.clear();
        ContainerHelper.loadAllItems(tag, this.storedInventory, registries);

        if (tag.contains("curios_data")) {
            this.storedCurios = tag.getCompound("curios_data");
        }

        this.storedExpLevel = tag.getInt("exp_level");
        this.storedTotalExp = tag.getInt("exp_total");
        this.storedExpProgress = tag.getFloat("exp_progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (this.ownerUUID != null) {
            tag.putUUID("owner_uuid", this.ownerUUID);
        }
        tag.putString("owner_name", getOwnerName());
        tag.putBoolean("breaking_down", this.breakingDown);
        tag.putInt("breakdown_ticks", this.breakdownTicks);
        tag.putInt("spawn_ticks", this.spawnTicks);

        ContainerHelper.saveAllItems(tag, this.storedInventory, registries);

        if (this.storedCurios != null && !this.storedCurios.isEmpty()) {
            tag.put("curios_data", this.storedCurios);
        }

        tag.putInt("exp_level", this.storedExpLevel);
        tag.putInt("exp_total", this.storedTotalExp);
        tag.putFloat("exp_progress", this.storedExpProgress);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, lookupProvider);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "tombstone_controller", 0, state -> {
            if (this.isBreakingDown()) {
                return state.setAndContinue(BREAKDOWN_ANIM);
            }
            if (this.spawnTicks > 0) {
                return state.setAndContinue(SPAWN_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
