package com.soybe.awakeningsystem.item.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class SlimManhwaArmorModel extends HumanoidModel<LivingEntity> {
    private static SlimManhwaArmorModel outerModel;
    private static SlimManhwaArmorModel innerModel;

    public SlimManhwaArmorModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createOuterLayer() {
        // Vanilla outer armor uses CubeDeformation(1.0F), making the character huge and wide.
        // We use CubeDeformation(0.28F) so it cleanly overlays the player's 3D skin layer (0.25F)
        // while being over 70% sleeker than vanilla's 1.0F, creating a tailored, slim manhwa silhouette.
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.28F), 0.0F);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createInnerLayer() {
        // Vanilla inner armor (leggings) uses CubeDeformation(0.5F).
        // We use CubeDeformation(0.26F) for form-fitting trousers without excessive bulk.
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.26F), 0.0F);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static SlimManhwaArmorModel getModel(EquipmentSlot slot) {
        if (slot == EquipmentSlot.LEGS) {
            if (innerModel == null) {
                innerModel = new SlimManhwaArmorModel(createInnerLayer().bakeRoot());
            }
            return innerModel;
        } else {
            if (outerModel == null) {
                outerModel = new SlimManhwaArmorModel(createOuterLayer().bakeRoot());
            }
            return outerModel;
        }
    }
}
