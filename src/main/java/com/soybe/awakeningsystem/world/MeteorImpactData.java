package com.soybe.awakeningsystem.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Recuerda dónde cayó el último meteorito, para que la brújula de meteoritos pueda señalarlo. */
public final class MeteorImpactData extends SavedData {

    private static final String DATA_NAME = "awakening_meteor_impact";

    private GlobalPos lastImpact;

    public static MeteorImpactData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(MeteorImpactData::new, MeteorImpactData::load, null), DATA_NAME);
    }

    private static MeteorImpactData load(CompoundTag tag, HolderLookup.Provider registries) {
        MeteorImpactData data = new MeteorImpactData();
        if (tag.contains("Dimension")) {
            ResourceKey<net.minecraft.world.level.Level> dimension = ResourceKey.create(
                    Registries.DIMENSION, ResourceLocation.parse(tag.getString("Dimension")));
            NbtUtils.readBlockPos(tag, "Pos").ifPresent(pos -> data.lastImpact = GlobalPos.of(dimension, pos));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        if (lastImpact != null) {
            tag.putString("Dimension", lastImpact.dimension().location().toString());
            tag.put("Pos", NbtUtils.writeBlockPos(lastImpact.pos()));
        }
        return tag;
    }

    public GlobalPos lastImpact() {
        return lastImpact;
    }

    public void setLastImpact(ResourceKey<net.minecraft.world.level.Level> dimension, BlockPos pos) {
        this.lastImpact = GlobalPos.of(dimension, pos.immutable());
        setDirty();
    }
}
