package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoblinWarriorEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

/** Goblin warrior: the shared goblin body with its own gear (see {@link GoblinParts}). */
public class GoblinWarriorModel extends HierarchicalModel<GoblinWarriorEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "goblin_warrior"), "main");

    private final ModelPart root;
    private final GoblinParts.Rig rig;

    public GoblinWarriorModel(ModelPart root) {
        this.root = root;
        this.rig = GoblinParts.Rig.of(root);
    }

    public static LayerDefinition createBodyLayer() {
        return GoblinParts.create(GoblinParts.Kind.WARRIOR);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(GoblinWarriorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        GoblinParts.animate(rig, GoblinParts.Kind.WARRIOR, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, this.attackTime, false);
    }
}