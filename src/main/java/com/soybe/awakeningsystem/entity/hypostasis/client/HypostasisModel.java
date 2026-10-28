package com.soybe.awakeningsystem.entity.hypostasis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.hypostasis.HypostasisAttackType;
import com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class HypostasisModel<T extends HypostasisBaseBossEntity> extends EntityModel<T> {

    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart[] cubes = new ModelPart[8];

    public HypostasisModel(ModelPart root) {
        this.root = root;
        this.core = root.getChild("core");
        for (int i = 0; i < 8; i++) {
            this.cubes[i] = root.getChild("cube" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        for (int i = 0; i < 8; i++) {
            root.addOrReplaceChild("cube" + i,
                    CubeListBuilder.create()
                            .texOffs(88, 108)
                            .addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F),
                    PartPose.offset(0.0F, 0.0F, 0.0F));
        }

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        int state = entity.getBossState();
        HypostasisAttackType attack = entity.getAttackType();
        float progress = entity.getAttackProgress();
        float time = entity.getAnimTick();

        float bob = Mth.sin(time * 0.05F) * 1.5F;

        for (int i = 0; i < 8; i++) {
            this.cubes[i].xScale = 1.35F;
            this.cubes[i].yScale = 1.35F;
            this.cubes[i].zScale = 1.35F;
        }

        if (state == HypostasisBaseBossEntity.STATE_VULNERABLE) {
            this.core.setPos(0.0F, 10.0F + Mth.sin(time * 0.15F) * 0.6F, 0.0F);
            float pulse = 0.75F * (1.0F + Mth.sin(time * 0.3F) * 0.12F);
            this.core.xScale = pulse;
            this.core.yScale = pulse;
            this.core.zScale = pulse;
        } else {
            this.core.setPos(0.0F, bob, 0.0F);
            float pulse = 0.85F + Mth.sin(time * 0.1F) * 0.04F;
            this.core.xScale = pulse;
            this.core.yScale = pulse;
            this.core.zScale = pulse;
        }

        this.core.xRot = 0.7854F + Mth.sin(time * 0.05F) * 0.1F;
        this.core.zRot = 0.7854F + Mth.cos(time * 0.05F) * 0.1F;
        this.core.yRot = time * 0.06F;

        switch (state) {
            case HypostasisBaseBossEntity.STATE_PROTECTED -> {
                float shellYaw = time * 0.02F;
                float shellPitch = Mth.sin(time * 0.03F) * 0.05F;
                float shellRoll = Mth.cos(time * 0.03F) * 0.05F;

                float breath = Mth.sin(time * 0.06F) * 1.5F;
                float dist = 13.5F + Math.max(0.0F, breath);

                for (int i = 0; i < 8; i++) {
                    float sx = (i & 1) == 0 ? -1.0F : 1.0F;
                    float sy = (i & 2) == 0 ? -1.0F : 1.0F;
                    float sz = (i & 4) == 0 ? -1.0F : 1.0F;

                    float lx = sx * dist;
                    float lz = sz * dist;
                    float rx = lx * Mth.cos(shellYaw) - lz * Mth.sin(shellYaw);
                    float rz = lx * Mth.sin(shellYaw) + lz * Mth.cos(shellYaw);
                    float cubeBob = Mth.sin(time * 0.08F + i * 0.785F) * 0.6F;
                    float ry = bob + sy * dist + cubeBob;

                    this.cubes[i].setPos(rx, ry, rz);
                    this.cubes[i].yRot = shellYaw;
                    this.cubes[i].xRot = shellPitch + Mth.sin(time * 0.06F + i) * 0.06F;
                    this.cubes[i].zRot = shellRoll + Mth.cos(time * 0.06F + i) * 0.06F;
                }
            }
            case HypostasisBaseBossEntity.STATE_ATTACKING -> {
                animateAttackFormation(entity, attack, progress, time, bob);
            }
            case HypostasisBaseBossEntity.STATE_VULNERABLE -> {
                float groundY = 16.0F;
                float scatterR = 22.0F;
                for (int i = 0; i < 8; i++) {
                    float angle = i * ((float) Math.PI / 4.0F);
                    this.cubes[i].setPos(Mth.cos(angle) * scatterR, groundY, Mth.sin(angle) * scatterR);
                    this.cubes[i].xRot = 0.35F;
                    this.cubes[i].yRot = angle + 0.25F;
                    this.cubes[i].zRot = 0.15F;
                }
            }
            case HypostasisBaseBossEntity.STATE_REVIVAL -> {
                float shieldRot = time * 0.02F;
                float shieldDist = 9.5F;
                for (int i = 0; i < 8; i++) {
                    float sx = (i & 1) == 0 ? -1.0F : 1.0F;
                    float sy = (i & 2) == 0 ? -1.0F : 1.0F;
                    float sz = (i & 4) == 0 ? -1.0F : 1.0F;

                    float rx = (sx * shieldDist) * Mth.cos(shieldRot) - (sz * shieldDist) * Mth.sin(shieldRot);
                    float rz = (sx * shieldDist) * Mth.sin(shieldRot) + (sz * shieldDist) * Mth.cos(shieldRot);
                    float ry = bob + sy * shieldDist;

                    this.cubes[i].setPos(rx, ry, rz);
                    this.cubes[i].yRot = shieldRot;
                    this.cubes[i].xRot = 0.0F;
                    this.cubes[i].zRot = 0.0F;
                }
            }
            default -> {
                for (int i = 0; i < 8; i++) {
                    this.cubes[i].setPos(0.0F, 0.0F, 0.0F);
                }
            }
        }
    }

    private void animateAttackFormation(T entity, HypostasisAttackType attack, float progress, float time, float bob) {
        switch (attack) {
            case CLAP -> {
                float pullBack = (progress < 0.4F) ? (progress / 0.4F) : 1.0F;
                float slam = (progress >= 0.4F && progress <= 0.65F) ? ((progress - 0.4F) / 0.25F) : 0.0F;
                float recover = (progress > 0.65F) ? ((progress - 0.65F) / 0.35F) : 0.0F;

                float currentDist;
                if (progress < 0.4F) {
                    currentDist = 6.4F + pullBack * 18.0F;
                } else if (progress <= 0.65F) {
                    currentDist = 24.4F - slam * 23.4F;
                } else {
                    currentDist = 1.0F + recover * 5.4F;
                }

                for (int i = 0; i < 8; i++) {
                    float sx = (i & 1) == 0 ? -1.0F : 1.0F;
                    float sy = (i & 2) == 0 ? -1.0F : 1.0F;
                    float sz = (i & 4) == 0 ? -1.0F : 1.0F;

                    this.cubes[i].setPos(sx * currentDist, sy * 6.4F + bob, sz * 6.4F + 4.0F);
                    this.cubes[i].xRot = 0.0F;
                    this.cubes[i].yRot = 0.0F;
                    this.cubes[i].zRot = 0.0F;
                }
            }
            case MISSILES -> {
                float ringRadius = 16.0F;
                float ringRot = time * 0.05F;
                for (int i = 0; i < 8; i++) {
                    float angle = i * ((float) Math.PI / 4.0F) + ringRot;
                    float rx = Mth.cos(angle) * ringRadius;
                    float ry = Mth.sin(angle) * ringRadius + bob;
                    float rz = -6.0F;

                    if ((int) (time * 0.35F) % 8 == i) {
                        rz -= 3.5F;
                    }

                    this.cubes[i].setPos(rx, ry, rz);
                    this.cubes[i].xRot = 0.0F;
                    this.cubes[i].yRot = 0.0F;
                    this.cubes[i].zRot = angle;
                }
            }
            case SPECIAL_ACTION -> {
                boolean isAnemo = entity instanceof com.soybe.awakeningsystem.entity.hypostasis.AnemoHypostasisBossEntity;
                if (isAnemo) {
                    float flap = Mth.sin(time * 0.28F) * 0.65F;
                    for (int i = 0; i < 8; i++) {
                        boolean isLeft = (i < 4);
                        int sub = i % 4;
                        float side = isLeft ? -1.0F : 1.0F;
                        float wx = side * (10.0F + (sub % 2) * 10.0F);
                        float wy = ((sub / 2 == 0) ? -7.0F : 7.0F) + bob;
                        float wz = (sub % 2 == 0) ? -2.0F : 2.0F;

                        this.cubes[i].setPos(wx, wy, wz);
                        this.cubes[i].xRot = flap * 0.35F;
                        this.cubes[i].yRot = 0.0F;
                        this.cubes[i].zRot = side * flap;
                    }
                } else {
                    float drillSpin = time * 0.45F;
                    for (int i = 0; i < 8; i++) {
                        float zOffset = (i - 3.5F) * 3.5F + 6.0F;
                        float radius = 3.0F + (7 - i) * 1.1F;
                        float angle = drillSpin + i * ((float) Math.PI / 2.0F);

                        this.cubes[i].setPos(Mth.cos(angle) * radius, Mth.sin(angle) * radius + bob, zOffset);
                        this.cubes[i].xRot = 0.0F;
                        this.cubes[i].yRot = 0.0F;
                        this.cubes[i].zRot = angle;
                    }
                }
            }
            case BURST_ULTIMATE -> {
                float laserRingRot = time * 0.035F;
                float ringR = 22.0F;
                for (int i = 0; i < 8; i++) {
                    float angle = i * ((float) Math.PI / 4.0F) + laserRingRot;
                    this.cubes[i].setPos(Mth.cos(angle) * ringR, 10.0F, Mth.sin(angle) * ringR);
                    this.cubes[i].xRot = 0.0F;
                    this.cubes[i].yRot = -angle;
                    this.cubes[i].zRot = 0.0F;
                }
            }
            default -> {
                for (int i = 0; i < 8; i++) {
                    this.cubes[i].setPos(0.0F, 0.0F, 0.0F);
                }
            }
        }
    }

    public void renderCore(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.core.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
