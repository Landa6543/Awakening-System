package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AntSoldierEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D para el Soldado Hormiga Gigante (Ant Soldier / Beru).
 * Cuenta con postura bípeda ágil, mandíbulas articuladas, antenas dinámicas,
 * brazos de guadaña desgarradores, patas digitígradas y alas membranosas desplegables.
 */
public class AntSoldierModel extends HierarchicalModel<AntSoldierEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ant_soldier"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftAntenna;
    private final ModelPart rightAntenna;
    private final ModelPart leftMandible;
    private final ModelPart rightMandible;
    private final ModelPart thorax;
    private final ModelPart abdomen;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public AntSoldierModel(ModelPart root) {
        this.root = root;
        this.thorax = root.getChild("thorax");
        this.head = this.thorax.getChild("head");
        this.leftAntenna = this.head.getChild("left_antenna");
        this.rightAntenna = this.head.getChild("right_antenna");
        this.leftMandible = this.head.getChild("left_mandible");
        this.rightMandible = this.head.getChild("right_mandible");
        this.abdomen = this.thorax.getChild("abdomen");
        this.leftWing = this.thorax.getChild("left_wing");
        this.rightWing = this.thorax.getChild("right_wing");
        this.rightArm = this.thorax.getChild("right_arm");
        this.leftArm = this.thorax.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Tórax central (inclinado ligeramente hacia adelante en pose de combate)
        PartDefinition thorax = root.addOrReplaceChild("thorax", CubeListBuilder.create()
                // Placas principales del tórax (U: 0..34, V: 0..22)
                .texOffs(0, 0).addBox(-4.5F, -14.0F, -3.5F, 9.0F, 14.0F, 8.0F, new CubeDeformation(0.0F))
                // Cresta dorsal de quitina (U: 36..56, V: 0..19)
                .texOffs(36, 0).addBox(-1.5F, -15.0F, -2.0F, 3.0F, 12.0F, 7.0F, new CubeDeformation(0.0F))
                // Cuello robusto blindado que conecta firmemente con la cabeza (U: 58..86, V: 0..12)
                .texOffs(58, 0).addBox(-3.5F, -16.5F, -3.5F, 7.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.22F, 0.0F, 0.0F));

        // Patas secundarias de insecto en el tórax (U: 88..100, V: 0..12)
        thorax.addOrReplaceChild("sub_leg_right", CubeListBuilder.create()
                .texOffs(88, 0).addBox(-4.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F)
                .texOffs(88, 4).addBox(-6.0F, -1.0F, 1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, -6.0F, 1.0F, 0.2F, 0.3F, -0.4F));

        thorax.addOrReplaceChild("sub_leg_left", CubeListBuilder.create()
                .texOffs(88, 0).mirror().addBox(0.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F)
                .texOffs(88, 4).mirror().addBox(4.0F, -1.0F, 1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, -6.0F, 1.0F, 0.2F, -0.3F, 0.4F));

        // 2. Cabeza insectoide firmemente asentada sobre el cuello (U: 0..28, V: 24..38)
        PartDefinition head = thorax.addOrReplaceChild("head", CubeListBuilder.create()
                // Cabeza triangular
                .texOffs(0, 24).addBox(-3.5F, -6.5F, -4.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                // Cresta superior de la cabeza (U: 30..44, V: 24..32)
                .texOffs(30, 24).addBox(-1.0F, -8.5F, -3.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                // Ojos facetados laterales (U: 46..56, V: 24..32)
                .texOffs(46, 24).addBox(-4.2F, -5.0F, -3.5F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(46, 24).mirror().addBox(3.2F, -5.0F, -3.5F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -14.5F, -1.5F));

        // Antenas dinámicas continuas (U: 58..62, V: 24..37)
        head.addOrReplaceChild("left_antenna", CubeListBuilder.create()
                .texOffs(58, 24).addBox(-0.5F, -12.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, -6.5F, -2.5F, 0.4F, 0.2F, 0.25F));

        head.addOrReplaceChild("right_antenna", CubeListBuilder.create()
                .texOffs(58, 24).mirror().addBox(-0.5F, -12.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.0F, -6.5F, -2.5F, 0.4F, -0.2F, -0.25F));

        // Mandíbulas afiladas articuladas (U: 66..80 y U: 82..92, V: 24..32)
        head.addOrReplaceChild("left_mandible", CubeListBuilder.create()
                .texOffs(66, 24).addBox(0.0F, -1.0F, -4.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(82, 24).addBox(-1.5F, 0.0F, -6.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, -0.5F, -3.5F, 0.1F, -0.3F, 0.0F));

        head.addOrReplaceChild("right_mandible", CubeListBuilder.create()
                .texOffs(66, 24).mirror().addBox(-2.0F, -1.0F, -4.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(82, 24).mirror().addBox(-0.5F, 0.0F, -6.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.0F, -0.5F, -3.5F, 0.1F, 0.3F, 0.0F));

        // 3. Abdomen (Gáster alargado hacia atrás con aguijón)
        PartDefinition abdomen = thorax.addOrReplaceChild("abdomen", CubeListBuilder.create()
                // Conexión cintura estrecha (U: 0..16, V: 42..49)
                .texOffs(0, 42).addBox(-2.5F, -1.0F, 0.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                // Abdomen bulboso (U: 18..64, V: 42..65)
                .texOffs(18, 42).addBox(-4.5F, -2.5F, 3.0F, 9.0F, 9.0F, 14.0F, new CubeDeformation(0.0F))
                // Punta con aguijón de quitina (U: 66..84, V: 42..51 y U: 86..94, V: 42..47)
                .texOffs(66, 42).addBox(-2.0F, 0.0F, 17.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(86, 42).addBox(-0.5F, 1.0F, 22.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.0F, 3.5F, -0.45F, 0.0F, 0.0F));

        // 4. Alas membranosas plegables en la espalda (U: 78..96, V: 68..89)
        thorax.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(78, 68).addBox(0.0F, 0.0F, 0.0F, 8.0F, 20.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.5F, -12.5F, 4.5F, 0.35F, 0.1F, 0.05F));

        thorax.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(78, 68).mirror().addBox(-8.0F, 0.0F, 0.0F, 8.0F, 20.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.5F, -12.5F, 4.5F, 0.35F, -0.1F, -0.05F));

        // 5. Brazos de Guadaña (Armas de asalto principales)
        PartDefinition rightArm = thorax.addOrReplaceChild("right_arm", CubeListBuilder.create()
                // Hombro robusto (U: 0..18, V: 68..79)
                .texOffs(0, 68).addBox(-4.0F, -2.5F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                // Antebrazo (U: 20..34, V: 68..80)
                .texOffs(20, 68).addBox(-3.5F, 3.5F, -2.0F, 3.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                // Cuchilla de Guadaña de quitina (curvada hacia adelante) (U: 36..60, V: 68..82)
                .texOffs(36, 68).addBox(-3.0F, 11.5F, -8.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                // Punta de la guadaña (U: 62..76, V: 68..77)
                .texOffs(62, 68).addBox(-2.5F, 12.0F, -14.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.0F, -12.0F, 0.0F, 0.4F, -0.15F, 0.25F));

        PartDefinition leftArm = thorax.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 68).mirror().addBox(0.0F, -2.5F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(20, 68).mirror().addBox(0.5F, 3.5F, -2.0F, 3.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(36, 68).mirror().addBox(1.0F, 11.5F, -8.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(62, 68).mirror().addBox(1.5F, 12.0F, -14.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, -12.0F, 0.0F, 0.4F, 0.15F, -0.25F));

        // 6. Piernas Digitígradas bípedas (Muslos potentes + pantorrillas articuladas + garras)
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                // Muslo superior (U: 0..18, V: 84..97)
                .texOffs(0, 84).addBox(-2.5F, -1.0F, -3.0F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
                // Pantorrilla digitígrada angulada (U: 20..34, V: 84..97)
                .texOffs(20, 84).addBox(-2.0F, 6.0F, 0.0F, 3.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
                // Pie con garras triples de insecto (U: 36..56, V: 84..92)
                .texOffs(36, 84).addBox(-2.5F, 14.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.8F, 8.0F, 2.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 84).mirror().addBox(-1.5F, -1.0F, -3.0F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(20, 84).mirror().addBox(-1.0F, 6.0F, 0.0F, 3.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(36, 84).mirror().addBox(-1.5F, 14.0F, -4.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.8F, 8.0F, 2.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(AntSoldierEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Rotación de cabeza (asentada sobre el cuello)
        this.head.yRot = netHeadYaw * (Mth.PI / 180.0F);
        this.head.xRot = headPitch * (Mth.PI / 180.0F) - 0.22F;

        // Vibración sutil de antenas
        float antennaWave = Mth.sin(ageInTicks * 0.25F) * 0.08F;
        this.leftAntenna.xRot = 0.4F + antennaWave;
        this.rightAntenna.xRot = 0.4F - antennaWave;
        this.leftAntenna.yRot = 0.2F + Mth.cos(ageInTicks * 0.2F) * 0.06F;
        this.rightAntenna.yRot = -0.2F - Mth.cos(ageInTicks * 0.2F) * 0.06F;

        // Mandíbulas: Chasquido o apertura
        float mandibleSpread = Mth.sin(ageInTicks * 0.15F) * 0.06F;
        this.leftMandible.yRot = -0.3F - mandibleSpread;
        this.rightMandible.yRot = 0.3F + mandibleSpread;

        // Movimiento de respiración del abdomen
        this.abdomen.xRot = -0.45F + Mth.sin(ageInTicks * 0.08F) * 0.04F;

        // Locomoción bípeda digitígrada
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.7F) * 1.3F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.7F + Mth.PI) * 1.3F * limbSwingAmount;

        // Brazos en posición de guardia / combate
        this.rightArm.xRot = 0.4F + Mth.cos(limbSwing * 0.7F + Mth.PI) * 0.4F * limbSwingAmount;
        this.leftArm.xRot = 0.4F + Mth.cos(limbSwing * 0.7F) * 0.4F * limbSwingAmount;

        // Modo Salto / Vuelo en picada (Pouncing)
        if (entity.isPouncing()) {
            // Desplegar y aletear alas a máxima velocidad
            float wingFlutter = Mth.sin(ageInTicks * 1.8F) * 0.75F;
            this.leftWing.yRot = 0.65F + wingFlutter;
            this.rightWing.yRot = -0.65F - wingFlutter;
            this.leftWing.xRot = 0.8F;
            this.rightWing.xRot = 0.8F;

            // Inclinación aerodinámica
            this.thorax.xRot = 0.65F;
            this.rightArm.xRot = -0.5F;
            this.leftArm.xRot = -0.5F;
            this.rightLeg.xRot = 0.6F;
            this.leftLeg.xRot = 0.6F;
        } else {
            // Alas en reposo
            if (limbSwingAmount > 0.1F) {
                // Aleteo suave al correr rápido
                float runFlutter = Mth.sin(ageInTicks * 0.9F) * 0.2F;
                this.leftWing.yRot = 0.25F + runFlutter;
                this.rightWing.yRot = -0.25F - runFlutter;
            } else {
                this.leftWing.yRot = 0.1F;
                this.rightWing.yRot = -0.1F;
            }
        }

        // Ataque cuerpo a cuerpo (Tajo de Guadañas)
        if (entity.attackAnimationState.isStarted()) {
            float attackProg = Mth.sin(ageInTicks * 0.7F);
            this.rightArm.xRot = -0.9F + attackProg * 0.9F;
            this.leftArm.xRot = -0.9F - attackProg * 0.9F;
            this.rightArm.yRot = -0.4F;
            this.leftArm.yRot = 0.4F;
            this.leftMandible.yRot = -0.6F;
            this.rightMandible.yRot = 0.6F;
        }

        // Chillido de enjambre (Screech)
        if (entity.isScreeching()) {
            this.head.xRot = -0.6F + Mth.sin(ageInTicks * 0.6F) * 0.08F;
            this.leftMandible.yRot = -0.8F;
            this.rightMandible.yRot = 0.8F;
            this.rightArm.xRot = -1.2F;
            this.leftArm.xRot = -1.2F;
            this.leftArm.zRot = -0.6F;
            this.rightArm.zRot = 0.6F;
            this.leftWing.yRot = 0.9F;
            this.rightWing.yRot = -0.9F;
        }
    }
}
