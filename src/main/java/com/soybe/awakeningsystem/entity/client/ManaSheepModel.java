package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.animal.ManaSheepEntity;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** The mana sheep: the vanilla shape with glowing mana crystals growing from it (see {@link ManaCrystals}). */
public class ManaSheepModel extends SheepModel<ManaSheepEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mana_sheep"), "main");

    public ManaSheepModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return ManaCrystals.extend(SheepModel.createBodyLayer(), root -> {
            PartDefinition head = root.getChild("head");
            ManaCrystals.horn(head, -3.2F, -3.0F, -3.0F, -1);
            ManaCrystals.horn(head, 3.2F, -3.0F, -3.0F, 1);
            ManaCrystals.shard(root, 0.0F, 5.0F, -3.0F, 5, -0.2F, 0.15F);
            ManaCrystals.shard(root, 2.5F, 5.0F, 2.0F, 4, 0.15F, 0.45F);
            ManaCrystals.shard(root, -2.5F, 5.0F, 4.0F, 4, 0.2F, -0.4F);
        });
    }
}