package com.soybe.awakeningsystem.celestial.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.celestial.CelestialBoltEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a celestial bolt pointing where it flies: either a copy of the weapon itself (its blade forward) or a
 * sprite. It is drawn twice, crossed, so it can be seen from any side.
 */
public class CelestialBoltRenderer extends EntityRenderer<CelestialBoltEntity> {
    private static final int FULL_BRIGHT = 15728880;
    private final ItemRenderer items;

    /** Sprite size in blocks (width, height) and which way it points in its picture (degrees, 0 = right, 90 = up). */
    private record Sprite(ResourceLocation tex, float w, float h, float angle) {}

    private static Sprite sprite(String name) {
        ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/celestial/" + name + ".png");
        return switch (name) {
            case "empyrean_star" -> new Sprite(tex, 1.3F, 0.78F, 0);
            case "vehemence_bolt" -> new Sprite(tex, 0.55F, 1.65F, 90);
            case "atom_splitter_bolt" -> new Sprite(tex, 1.8F, 1.8F, 45);
            default -> new Sprite(tex, 1.0F, 1.0F, 0);
        };
    }

    public CelestialBoltRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.items = ctx.getItemRenderer();
    }

    @Override
    public void render(CelestialBoltEntity e, float entityYaw, float pt, PoseStack pose, MultiBufferSource buf, int light) {
        float yaw = Mth.rotLerp(pt, e.yRotO, e.getYRot());
        float pitch = Mth.lerp(pt, e.xRotO, e.getXRot());
        int kind = e.kind();
        float age = e.tickCount + pt;
        pose.pushPose();
        pose.translate(0, 0.25, 0);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));        // local +Z now points where it flies
        pose.mulPose(Axis.XP.rotationDegrees(-pitch));
        pose.mulPose(Axis.XP.rotationDegrees(90));         // the picture's +Y now points forward
        for (int pass = 0; pass < 2; pass++) {
            pose.pushPose();
            if (pass == 1) pose.mulPose(Axis.YP.rotationDegrees(90));
            ItemStack stack = e.item();
            if (!stack.isEmpty()) {
                float size = switch (kind) {
                    case CelestialBoltEntity.SCYTHE -> 1.7F;
                    case CelestialBoltEntity.JAVELIN -> 2.0F;
                    case CelestialBoltEntity.SHARD -> 1.1F;
                    default -> 1.8F;
                };
                if (kind == CelestialBoltEntity.SCYTHE) pose.mulPose(Axis.ZP.rotationDegrees(age * 36));
                pose.mulPose(Axis.ZP.rotationDegrees(45));     // the blade's diagonal now points forward
                pose.scale(size, size, size);
                items.renderStatic(stack, ItemDisplayContext.NONE, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose, buf, e.level(), e.getId());
            } else if (!e.sprite().isEmpty()) {
                Sprite s = sprite(e.sprite());
                float k = kind == CelestialBoltEntity.SHARD ? 0.65F : 1.0F;
                pose.mulPose(Axis.ZP.rotationDegrees(90 - s.angle()));
                quad(pose, buf.getBuffer(RenderType.entityTranslucent(s.tex())), s.w() * k, s.h() * k);
            }
            pose.popPose();
        }
        pose.popPose();
        super.render(e, entityYaw, pt, pose, buf, light);
    }

    static void quad(PoseStack pose, VertexConsumer vc, float w, float h) {
        quad(pose, vc, w, h, 0, 1, 0xFFFFFFFF);
    }

    static void quad(PoseStack pose, VertexConsumer vc, float w, float h, float v0, float v1, int argb) {
        PoseStack.Pose p = pose.last();
        float x = w / 2, y = h / 2;
        int a = argb >>> 24, r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
        vc.addVertex(p, -x, -y, 0).setColor(r, g, b, a).setUv(0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(p, 0, 0, 1);
        vc.addVertex(p, x, -y, 0).setColor(r, g, b, a).setUv(1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(p, 0, 0, 1);
        vc.addVertex(p, x, y, 0).setColor(r, g, b, a).setUv(1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(p, 0, 0, 1);
        vc.addVertex(p, -x, y, 0).setColor(r, g, b, a).setUv(0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(p, 0, 0, 1);
    }

    @Override
    public ResourceLocation getTextureLocation(CelestialBoltEntity e) {
        return sprite(e.sprite().isEmpty() ? "empyrean_star" : e.sprite()).tex();
    }
}
