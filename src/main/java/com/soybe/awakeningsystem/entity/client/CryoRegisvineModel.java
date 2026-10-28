package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.CryoRegisvineEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class CryoRegisvineModel extends EntityModel<CryoRegisvineEntity> {

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart head;
    private final ModelPart leftarm;
    private final ModelPart rightarm;

    public CryoRegisvineModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.head = root.getChild("head");
        this.leftarm = root.getChild("leftarm");
        this.rightarm = root.getChild("rightarm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition part_bone = root.addOrReplaceChild("bone",
                CubeListBuilder.create()
                .texOffs(94, 87).mirror(false).addBox(-4.0F, 5.5F, -6.0F, 9.0F, 33.0F, 8.0F),
                PartPose.offset(0.0F, -14.5F, 4.0F));

        PartDefinition part_cube_r1 = part_bone.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                .texOffs(104, 65).mirror(true).addBox(-4.5F, -8.5F, -1.5F, 9.0F, 9.0F, 3.0F),
                PartPose.offsetAndRotation(0.5F, 38.0F, -7.5F, 2.4435F, 0.0F, -3.1416F));

        PartDefinition part_cube_r2 = part_bone.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                .texOffs(104, 65).mirror(false).addBox(-4.5F, -8.5F, -1.5F, 9.0F, 9.0F, 3.0F),
                PartPose.offsetAndRotation(0.5F, 38.0F, 3.5F, -0.6981F, 0.0F, 0.0F));

        PartDefinition part_cube_r3 = part_bone.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                .texOffs(0, 111).mirror(true).addBox(-1.5F, -8.5F, -4.0F, 3.0F, 9.0F, 8.0F),
                PartPose.offsetAndRotation(6.5F, 38.0F, -2.0F, 0.0F, 0.0F, 0.6981F));

        PartDefinition part_cube_r4 = part_bone.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                .texOffs(0, 111).mirror(false).addBox(-1.5F, -8.5F, -4.0F, 3.0F, 9.0F, 8.0F),
                PartPose.offsetAndRotation(-5.5F, 38.0F, -2.0F, 0.0F, 0.0F, -0.6981F));

        PartDefinition part_cube_r5 = part_bone.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                .texOffs(94, 91).mirror(false).addBox(-4.5F, -25.6577F, -3.3126F, 9.0F, 29.0F, 8.0F),
                PartPose.offsetAndRotation(0.5F, -16.5668F, 10.9249F, 0.2618F, 0.0F, 0.0F));

        PartDefinition part_cube_r6 = part_bone.addOrReplaceChild("cube_r6",
                CubeListBuilder.create()
                .texOffs(94, 92).mirror(false).addBox(-4.0F, -20.5F, -2.0F, 9.0F, 28.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5672F, 0.0F, 0.0F));

        PartDefinition part_head = root.addOrReplaceChild("head",
                CubeListBuilder.create(),
                PartPose.offset(0.5F, -53.3932F, 7.5616F));

        PartDefinition part_cube_r7 = part_head.addOrReplaceChild("cube_r7",
                CubeListBuilder.create()
                .texOffs(96, 101).mirror(false).addBox(-4.5F, -15.6264F, -2.7579F, 9.0F, 20.0F, 7.0F)
                .texOffs(106, 14).mirror(false).addBox(-2.5F, -18.6264F, 3.2421F, 5.0F, 3.0F, 6.0F)
                .texOffs(25, 30).mirror(false).addBox(-1.5F, -21.6264F, 3.2421F, 3.0F, 3.0F, 5.0F)
                .texOffs(25, 30).mirror(false).addBox(3.5F, -21.6264F, -1.7579F, 5.0F, 3.0F, 3.0F)
                .texOffs(106, 14).mirror(true).addBox(3.5F, -18.6264F, -2.7579F, 6.0F, 3.0F, 5.0F)
                .texOffs(106, 14).mirror(false).addBox(-9.5F, -18.6264F, -2.7579F, 6.0F, 3.0F, 5.0F)
                .texOffs(25, 30).mirror(true).addBox(-8.5F, -21.6264F, -1.7579F, 5.0F, 3.0F, 3.0F)
                .texOffs(75, 26).mirror(false).addBox(-3.5F, -21.6264F, -4.7579F, 7.0F, 6.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -1.7084F, 0.3489F, 1.3526F, 0.0F, 0.0F));

        PartDefinition part_cube_r8 = part_head.addOrReplaceChild("cube_r8",
                CubeListBuilder.create()
                .texOffs(25, 30).mirror(true).addBox(-1.5F, -2.1493F, -0.5711F, 3.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(-3.0F, -8.6706F, -18.0576F, 1.4366F, -0.1726F, -0.9047F));

        PartDefinition part_cube_r9 = part_head.addOrReplaceChild("cube_r9",
                CubeListBuilder.create()
                .texOffs(25, 30).mirror(false).addBox(-1.5F, -2.1493F, -0.5711F, 3.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(3.0F, -8.6706F, -18.0576F, 1.4366F, 0.1726F, 0.9047F));

        PartDefinition part_bone2 = part_head.addOrReplaceChild("bone2",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-6.0F, 0.3168F, -14.0419F, 0.0F, 0.0F, -0.2618F));

        PartDefinition part_cube_r10 = part_bone2.addOrReplaceChild("cube_r10",
                CubeListBuilder.create()
                .texOffs(0, 50).mirror(false).addBox(-47.9591F, -3.0862F, 4.7932F, 49.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-13.9591F, -3.0862F, 9.7932F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-23.9591F, -3.0862F, 9.7932F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-33.9591F, -3.0862F, 9.7932F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-43.9591F, -3.0862F, 9.7932F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-13.9591F, -3.0862F, -0.2068F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-23.9591F, -3.0862F, -0.2068F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-33.9591F, -3.0862F, -0.2068F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-43.9591F, -3.0862F, -0.2068F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(43.0409F, -3.0862F, -0.2068F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(33.0409F, -3.0862F, -0.2068F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(23.0409F, -3.0862F, -0.2068F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(13.0409F, -3.0862F, -0.2068F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(13.0409F, -3.0862F, 9.7932F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(23.0409F, -3.0862F, 9.7932F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(33.0409F, -3.0862F, 9.7932F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(43.0409F, -3.0862F, 9.7932F, 7.0F, 3.0F, 5.0F)
                .texOffs(0, 50).mirror(true).addBox(5.0409F, -3.0862F, 4.7932F, 49.0F, 3.0F, 5.0F)
                .texOffs(0, 0).mirror(true).addBox(5.0409F, 0.9138F, 0.7932F, 19.0F, 1.0F, 13.0F)
                .texOffs(0, 0).mirror(false).addBox(-17.9591F, 0.9138F, 0.7932F, 19.0F, 1.0F, 13.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4153F, 0.1536F, 0.7734F));

        PartDefinition part_bone3 = part_head.addOrReplaceChild("bone3",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.1917F, -6.7106F, -12.5568F, 0.3638F, 0.0819F, 1.8098F));

        PartDefinition part_cube_r11 = part_bone3.addOrReplaceChild("cube_r11",
                CubeListBuilder.create()
                .texOffs(0, 0).mirror(false).addBox(-17.3668F, 0.9432F, -0.1464F, 19.0F, 1.0F, 13.0F)
                .texOffs(104, 0).mirror(false).addBox(-44.3668F, -4.0568F, -1.1464F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-34.3668F, -4.0568F, -1.1464F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-14.3668F, -4.0568F, -1.1464F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-24.3668F, -4.0568F, -1.1464F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-14.3668F, -4.0568F, 8.8536F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-24.3668F, -4.0568F, 8.8536F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-34.3668F, -4.0568F, 8.8536F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(-44.3668F, -4.0568F, 8.8536F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(43.6332F, -4.0568F, 8.8536F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(33.6332F, -4.0568F, 8.8536F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(23.6332F, -4.0568F, 8.8536F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(13.6332F, -4.0568F, 8.8536F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(13.6332F, -4.0568F, -1.1464F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(23.6332F, -4.0568F, -1.1464F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(33.6332F, -4.0568F, -1.1464F, 7.0F, 3.0F, 5.0F)
                .texOffs(104, 0).mirror(false).addBox(43.6332F, -4.0568F, -1.1464F, 7.0F, 3.0F, 5.0F)
                .texOffs(0, 50).mirror(true).addBox(5.6332F, -4.0568F, 3.8536F, 49.0F, 3.0F, 5.0F)
                .texOffs(0, 50).mirror(false).addBox(-48.3668F, -4.0568F, 3.8536F, 49.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(-2.9502F, 4.3243F, -0.7707F, 1.4153F, 0.1536F, 0.7734F));

        PartDefinition part_bone4 = part_head.addOrReplaceChild("bone4",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.1917F, -6.7106F, -12.5568F, 0.3638F, 0.0819F, 1.8098F));

        PartDefinition part_cube_r12 = part_bone4.addOrReplaceChild("cube_r12",
                CubeListBuilder.create()
                .texOffs(0, 0).mirror(true).addBox(5.6332F, 0.9432F, -0.1464F, 19.0F, 1.0F, 13.0F),
                PartPose.offsetAndRotation(-2.9502F, 4.3243F, -0.7707F, 1.4153F, 0.1536F, 0.7734F));

        PartDefinition part_leftarm = root.addOrReplaceChild("leftarm",
                CubeListBuilder.create(),
                PartPose.offset(4.2976F, -52.8022F, 0.9342F));

        PartDefinition part_cube_r13 = part_leftarm.addOrReplaceChild("cube_r13",
                CubeListBuilder.create()
                .texOffs(64, 67).mirror(false).addBox(15.1987F, -2.0671F, -11.3556F, 3.0F, 5.0F, 9.0F)
                .texOffs(64, 67).mirror(false).addBox(15.1987F, -2.0671F, 2.6444F, 3.0F, 5.0F, 9.0F)
                .texOffs(48, 67).mirror(false).addBox(15.1987F, 2.9329F, -2.3556F, 3.0F, 9.0F, 5.0F)
                .texOffs(48, 67).mirror(false).addBox(15.1987F, -11.0671F, -2.3556F, 3.0F, 9.0F, 5.0F)
                .texOffs(46, 122).mirror(false).addBox(19.1987F, -1.0671F, -1.3556F, 12.0F, 3.0F, 3.0F)
                .texOffs(21, 96).mirror(false).addBox(-1.8013F, -2.0671F, -2.3556F, 21.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(37.5176F, 23.5885F, -6.5786F, 1.4604F, 0.1886F, 1.0367F));

        PartDefinition part_cube_r14 = part_leftarm.addOrReplaceChild("cube_r14",
                CubeListBuilder.create()
                .texOffs(21, 96).mirror(true).addBox(-1.9995F, -3.3704F, -1.0152F, 21.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(19.0869F, 20.1019F, -4.4707F, 1.3598F, 0.056F, 0.2559F));

        PartDefinition part_cube_r15 = part_leftarm.addOrReplaceChild("cube_r15",
                CubeListBuilder.create()
                .texOffs(0, 86).mirror(false).addBox(3.0409F, 3.9138F, 4.7932F, 30.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(-9.7976F, -0.2742F, -7.4145F, 1.4153F, 0.1536F, 0.7734F));

        PartDefinition part_rightarm = root.addOrReplaceChild("rightarm",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-2.1907F, -53.4099F, 1.5633F, -2.9138F, -0.0198F, 3.0286F));

        PartDefinition part_cube_r16 = part_rightarm.addOrReplaceChild("cube_r16",
                CubeListBuilder.create()
                .texOffs(64, 67).mirror(false).addBox(15.1987F, -2.0671F, -11.3556F, 3.0F, 5.0F, 9.0F)
                .texOffs(64, 67).mirror(false).addBox(15.1987F, -2.0671F, 2.6444F, 3.0F, 5.0F, 9.0F)
                .texOffs(48, 67).mirror(false).addBox(15.1987F, 2.9329F, -2.3556F, 3.0F, 9.0F, 5.0F)
                .texOffs(48, 67).mirror(false).addBox(15.1987F, -11.0671F, -2.3556F, 3.0F, 9.0F, 5.0F)
                .texOffs(46, 122).mirror(false).addBox(19.1987F, -1.0671F, -1.3556F, 12.0F, 3.0F, 3.0F)
                .texOffs(21, 96).mirror(false).addBox(-1.8013F, -2.0671F, -2.3556F, 21.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(37.5725F, 24.7002F, -5.4583F, 1.4604F, 0.1886F, 1.0367F));

        PartDefinition part_cube_r17 = part_rightarm.addOrReplaceChild("cube_r17",
                CubeListBuilder.create()
                .texOffs(21, 96).mirror(true).addBox(-1.9995F, -3.3704F, -1.0152F, 21.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(19.1418F, 21.2136F, -3.3504F, 1.3598F, 0.056F, 0.2559F));

        PartDefinition part_cube_r18 = part_rightarm.addOrReplaceChild("cube_r18",
                CubeListBuilder.create()
                .texOffs(0, 86).mirror(false).addBox(3.0409F, 3.9138F, 4.7932F, 30.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(-9.7427F, 0.8375F, -6.2942F, 1.4153F, 0.1536F, 0.7734F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(CryoRegisvineEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float breathe = Mth.sin(ageInTicks * 0.05F) * 0.04F;
        this.bone.zRot = breathe;

        if (entity.isStunned()) {
            this.head.xRot = 0.95F;
            this.head.yRot = 0.0F;
            this.head.zRot = 0.0F;
            this.leftarm.xRot = 0.6F;
            this.leftarm.zRot = 0.4F;
            this.rightarm.xRot = -2.4F;
            this.rightarm.zRot = 2.4F;
        } else if (entity.isSlamming()) {
            float progress = entity.getSlamProgress(ageInTicks);
            float slamAngle = Mth.sin(progress * (float) Math.PI) * 1.1F;
            this.head.xRot = slamAngle;
            this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.5F;
            this.leftarm.xRot = slamAngle * 0.6F;
            this.rightarm.xRot = -2.9138F + slamAngle * 0.6F;
        } else if (entity.isSpinning()) {
            float spin = ageInTicks * 0.45F;
            this.head.yRot = spin;
            this.head.xRot = 0.2F;
            this.leftarm.yRot = spin;
            this.rightarm.yRot = spin;
        } else {
            this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
            this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.6F;

            float armWave = Mth.cos(ageInTicks * 0.08F) * 0.12F;
            this.leftarm.xRot = armWave;
            this.rightarm.xRot = -2.9138F - armWave;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
