package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.animal.ManaChickenEntity;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** The mana chicken: the vanilla shape with glowing mana crystals growing from it (see {@link ManaCrystals}). */
public class ManaChickenModel extends ChickenModel<ManaChickenEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mana_chicken"), "main");

    public ManaChickenModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return ManaCrystals.extend(ChickenModel.createBodyLayer(), root -> {
            PartDefinition head = root.getChild("head");
            ManaCrystals.shard(head, 0.0F, -6.0F, -0.5F, 3, -0.15F, 0.0F);
            ManaCrystals.shard(head, 0.0F, -6.0F, 0.8F, 2, 0.35F, 0.0F);
            ManaCrystals.shard(root, 0.0F, 13.5F, 3.5F, 5, 0.75F, 0.0F);
            ManaCrystals.shard(root, -1.5F, 13.5F, 3.0F, 3, 0.6F, -0.4F);
            ManaCrystals.shard(root, 1.5F, 13.5F, 3.0F, 3, 0.6F, 0.4F);
        });
    }
}