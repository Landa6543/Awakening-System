package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.entity.SkeletonLordEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SkeletonLordRenderer extends GeoEntityRenderer<SkeletonLordEntity> {
    public SkeletonLordRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonLordModel());
        this.shadowRadius = 1.2F;
        // the crystal's glow (skeleton_lord_glowmask.png), drawn lit over the crystal
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
