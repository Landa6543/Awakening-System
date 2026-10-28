package com.soybe.awakeningsystem.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CustomPlayerPlushieBlockEntity extends BlockEntity {
    @Nullable
    private ResolvableProfile ownerProfile;
    @Nullable
    private Component customName;
    private int squashTicks = 0;
    private int prevSquashTicks = 0;

    public CustomPlayerPlushieBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CUSTOM_PLAYER_PLUSHIE.get(), pos, blockState);
    }

    @Nullable
    public ResolvableProfile getOwnerProfile() {
        return this.ownerProfile;
    }

    public String getOwnerName() {
        if (this.customName != null) {
            return this.customName.getString();
        }
        if (this.ownerProfile != null && this.ownerProfile.name().isPresent()) {
            return this.ownerProfile.name().get();
        }
        return "Jugador";
    }

    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(@Nullable Component name) {
        this.customName = name;
        this.setChanged();
    }

    public void setOwner(@Nullable ResolvableProfile profile) {
        synchronized (this) {
            this.ownerProfile = profile;
        }
        this.updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        if (this.ownerProfile != null && !this.ownerProfile.isResolved()) {
            this.ownerProfile.resolve().thenAcceptAsync(resolved -> {
                this.ownerProfile = resolved;
                this.setChanged();
                if (this.level != null) {
                    this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                }
            }, SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR);
        } else {
            this.setChanged();
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    public void triggerSquash() {
        this.squashTicks = 10;
        this.prevSquashTicks = 10;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CustomPlayerPlushieBlockEntity entity) {
        entity.prevSquashTicks = entity.squashTicks;
        if (entity.squashTicks > 0) {
            entity.squashTicks--;
        }
    }

    public float getSquashTicks(float partialTick) {
        return Mth.lerp(partialTick, (float) this.prevSquashTicks, (float) this.squashTicks);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("squashTicks", this.squashTicks);
        if (this.ownerProfile != null) {
            ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, this.ownerProfile)
                    .resultOrPartial(err -> {})
                    .ifPresent(profileTag -> tag.put("profile", profileTag));
        }
        if (this.customName != null) {
            tag.putString("custom_name", Component.Serializer.toJson(this.customName, registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.squashTicks = tag.getInt("squashTicks");
        this.prevSquashTicks = this.squashTicks;

        if (tag.contains("profile")) {
            ResolvableProfile.CODEC.parse(NbtOps.INSTANCE, tag.get("profile"))
                    .resultOrPartial(err -> {})
                    .ifPresent(this::setOwner);
        }
        if (tag.contains("custom_name")) {
            try {
                this.customName = Component.Serializer.fromJson(tag.getString("custom_name"), registries);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        ResolvableProfile profile = input.get(DataComponents.PROFILE);
        if (profile != null) {
            this.setOwner(profile);
        }
        Component name = input.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            this.customName = name;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (this.ownerProfile != null) {
            builder.set(DataComponents.PROFILE, this.ownerProfile);
        }
        if (this.customName != null) {
            builder.set(DataComponents.CUSTOM_NAME, this.customName);
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("profile");
        tag.remove("custom_name");
    }
}
