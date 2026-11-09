package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.entity.SkeletonLordEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SkeletonLordRenderer extends GeoEntityRenderer<SkeletonLordEntity> {
    public SkeletonLordRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonLordModel());
        this.shadowRadius = 1.2F;
    }
}
