package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public final class GiantSandwormModelLayers {
    private GiantSandwormModelLayers() {}

    public static final ModelLayerLocation HEAD = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "giant_sandworm_head"), "main");

    public static final ModelLayerLocation BODY = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "giant_sandworm_body"), "main");
}
