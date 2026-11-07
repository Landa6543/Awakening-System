package com.soybe.awakeningsystem.item.client;

import com.soybe.awakeningsystem.item.ManhwaArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class SideriteArmorRenderer extends GeoArmorRenderer<ManhwaArmorItem> {

    public SideriteArmorRenderer() {
        super(new SideriteArmorGeoModel());
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot slot) {
        super.applyBoneVisibilityBySlot(slot);

        if (this.model != null) {
            // El cinturón y falda acorazada (armorWaist) son parte exclusiva de los pantalones (LEGS) idéntico a Eclipse
            this.model.getBone("armorWaist").ifPresent(bone -> {
                bone.setHidden(slot != EquipmentSlot.LEGS);
            });

            // Ocultar permanentemente las alas (bone3) para dejar el modelo de coraza limpio idéntico a Eclipse
            this.model.getBone("bone3").ifPresent(bone -> {
                bone.setHidden(true);
            });
        }
    }

    @Override
    protected void applyBaseTransformations(HumanoidModel<?> baseModel) {
        super.applyBaseTransformations(baseModel);

        // Sincronizar la posición y rotación de la cadera/faldar con el torso del jugador (idéntico a Eclipse)
        if (this.model != null && baseModel != null) {
            this.model.getBone("armorWaist").ifPresent(bone -> {
                RenderUtil.matchModelPartRot(baseModel.body, bone);
                bone.updatePosition(baseModel.body.x, -baseModel.body.y, baseModel.body.z);
            });
        }
    }
}
