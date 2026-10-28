package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.entity.LimboDivineOrbProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer GeckoLib para el Orbe Divino del Limbo.
 */
public class DivineOrbRenderer extends GeoEntityRenderer<LimboDivineOrbProjectileEntity> {

    public DivineOrbRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DivineOrbGeoModel());
        this.shadowRadius = 0.3F;
    }

    @Override
    protected float getDeathMaxRotation(LimboDivineOrbProjectileEntity animatable) {
        return 0.0F;
    }
}
