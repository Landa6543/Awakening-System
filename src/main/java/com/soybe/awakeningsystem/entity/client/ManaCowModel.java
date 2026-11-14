package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.animal.ManaCowEntity;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** The mana cow: the vanilla shape with glowing mana crystals growing from it (see {@link ManaCrystals}). */
public class ManaCowModel extends CowModel<ManaCowEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mana_cow"), "main");

    public ManaCowModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return ManaCrystals.extend(CowModel.createBodyLayer(), root -> {
            PartDefinition head = root.getChild("head");
            ManaCrystals.horn(head, -4.5F, -4.0F, -4.0F, -1);
            ManaCrystals.horn(head, 4.5F, -4.0F, -4.0F, 1);
            ManaCrystals.shard(root, 0.0F, 2.5F, -5.0F, 5, -0.15F, 0.1F);
            ManaCrystals.shard(root, -2.5F, 2.5F, -1.0F, 4, 0.1F, -0.35F);
            ManaCrystals.shard(root, 1.5F, 2.5F, 2.0F, 7, -0.1F, 0.2F);
            ManaCrystals.shard(root, -1.0F, 2.5F, 6.0F, 4, 0.3F, -0.1F);
        });
    }
}