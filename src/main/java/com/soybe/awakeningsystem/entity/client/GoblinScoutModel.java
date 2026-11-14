package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoblinScoutEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

/** Goblin scout: the shared goblin body with its own gear (see {@link GoblinParts}). */
public class GoblinScoutModel extends HierarchicalModel<GoblinScoutEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "goblin_scout"), "main");

    private final ModelPart root;
    private final GoblinParts.Rig rig;

    public GoblinScoutModel(ModelPart root) {
        this.root = root;
        this.rig = GoblinParts.Rig.of(root);
    }

    public static LayerDefinition createBodyLayer() {
        return GoblinParts.create(GoblinParts.Kind.SCOUT);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(GoblinScoutEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        GoblinParts.animate(rig, GoblinParts.Kind.SCOUT, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, this.attackTime, false);
    }
}