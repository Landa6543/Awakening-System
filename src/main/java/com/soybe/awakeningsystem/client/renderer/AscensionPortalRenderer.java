package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.AscensionPortalBlock;
import com.soybe.awakeningsystem.block.entity.AscensionPortalBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * Renderer GeckoLib para el Portal Dimensional de la Gran Torre (Ascension Portal / Gate).
 * In the Overworld the portal sits inside the round gate of the Great Tower's vestibule, so there it is
 * drawn as a turning vortex filling that gate instead of the stone gate model.
 */
public class AscensionPortalRenderer extends GeoBlockRenderer<AscensionPortalBlockEntity> {
    private static final ResourceLocation DEPTH = tex("tower_vortex_depth");
    private static final ResourceLocation SWIRL = tex("tower_vortex_swirl");
    private static final ResourceLocation GLOW = tex("tower_vortex_glow");
    /** The gate is a circle of radius 4.4 centred 4 blocks above the portal block. */
    private static final float RADIUS = 5.0F;   // reaches under the obsidian ring, so no gap shows
    private static final float CENTER_Y = 4.0F;
    private static int tint = 0xFFFFFF;

    public AscensionPortalRenderer(BlockEntityRendererProvider.Context context) {
        super(new AscensionPortalGeoModel());
    }

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/" + name + ".png");
    }

    @Override
    public RenderType getRenderType(AscensionPortalBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static boolean isTowerGate(AscensionPortalBlockEntity be) {
        // only the Great Tower's own entrance (a BASE portal in the Overworld); every other portal keeps its gate
        return be.getLevel() != null && be.getLevel().dimension().equals(Level.OVERWORLD)
                && be.getBlockState().hasProperty(AscensionPortalBlock.PORTAL_TYPE)
                && be.getBlockState().getValue(AscensionPortalBlock.PORTAL_TYPE) == AscensionPortalBlock.PortalType.BASE;
    }

    @Override
    public void render(AscensionPortalBlockEntity animatable, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (isTowerGate(animatable)) {
            renderVortex(animatable, partialTick, poseStack, bufferSource);
            return;
        }
        poseStack.pushPose();
        // Escalar sutilmente (1.4x) centrado en el bloque para darle porte imponente de Gate
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.scale(1.4F, 1.4F, 1.4F);
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        super.render(animatable, partialTick, poseStack, bufferSource, LightTexture.FULL_BRIGHT, packedOverlay);
        poseStack.popPose();
    }

    private void renderVortex(AscensionPortalBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        float time = (be.getLevel().getGameTime() % 72000L) + partialTick;
        Direction facing = getFacing(be);
        boolean overworld = be.getLevel().dimension().equals(Level.OVERWORLD);
        // the Great Tower's own gate is big; the shrines on the tower's floors have a smaller one
        float radius = overworld ? RADIUS : 2.75F;
        float centerY = overworld ? CENTER_Y : 2.5F;
        // the way back glows red, the way up blue, the tower's entrance keeps its violet
        AscensionPortalBlock.PortalType type = be.getBlockState().hasProperty(AscensionPortalBlock.PORTAL_TYPE)
                ? be.getBlockState().getValue(AscensionPortalBlock.PORTAL_TYPE) : AscensionPortalBlock.PortalType.BASE;
        tint = overworld || type == AscensionPortalBlock.PortalType.BASE ? 0xFFFFFF
                : type == AscensionPortalBlock.PortalType.RETURN ? 0xFF8C8C : 0x8CC8FF;
        pose.pushPose();
        pose.translate(0.5D, centerY, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        // the dark depth behind everything, then two swirls turning at different speeds, then the glow
        disc(pose, buffers.getBuffer(RenderType.entityTranslucent(DEPTH)), radius, 0.0F, -0.06F, 255);
        disc(pose, buffers.getBuffer(RenderType.entityTranslucent(SWIRL)), radius * 0.98F, time * 2.2F, -0.03F, 235);
        // glowing layers that still respect the textures' transparency (no square around the circle)
        disc(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(SWIRL)), radius * 0.7F, time * 4.0F + 40.0F, 0.0F, 110);
        float pulse = 0.92F + 0.08F * (float) Math.sin(time * 0.08F);
        disc(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(GLOW)), radius * 0.92F * pulse, 0.0F, 0.03F, 170);
        pose.popPose();
        tint = 0xFFFFFF;
    }

    /** A square of side 2r centred on the pose, turned {@code spin} degrees, visible from both sides. */
    private static void disc(PoseStack pose, VertexConsumer vc, float r, float spin, float z, int alpha) {
        pose.pushPose();
        pose.translate(0.0F, 0.0F, z);
        pose.mulPose(Axis.ZP.rotationDegrees(spin % 360.0F));
        PoseStack.Pose last = pose.last();
        Matrix4f m = last.pose();
        int light = LightTexture.FULL_BRIGHT;
        // front
        vertex(vc, last, m, -r, -r, 0, 1, alpha, light, 1);
        vertex(vc, last, m, r, -r, 1, 1, alpha, light, 1);
        vertex(vc, last, m, r, r, 1, 0, alpha, light, 1);
        vertex(vc, last, m, -r, r, 0, 0, alpha, light, 1);
        // back
        vertex(vc, last, m, -r, r, 0, 0, alpha, light, -1);
        vertex(vc, last, m, r, r, 1, 0, alpha, light, -1);
        vertex(vc, last, m, r, -r, 1, 1, alpha, light, -1);
        vertex(vc, last, m, -r, -r, 0, 1, alpha, light, -1);
        pose.popPose();
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose last, Matrix4f m, float x, float y, float u, float v, int alpha, int light, int nz) {
        vc.addVertex(m, x, y, 0.0F).setColor((tint >> 16) & 255, (tint >> 8) & 255, tint & 255, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(last, 0.0F, 0.0F, nz);
    }

    @Override
    public AABB getRenderBoundingBox(AscensionPortalBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(6.0D);
    }

    @Override
    public boolean shouldRenderOffScreen(AscensionPortalBlockEntity be) {
        return true;
    }

    @Override
    protected Direction getFacing(AscensionPortalBlockEntity animatable) {
        if (animatable != null && animatable.getBlockState() != null && animatable.getBlockState().hasProperty(AscensionPortalBlock.FACING)) {
            return animatable.getBlockState().getValue(AscensionPortalBlock.FACING);
        }
        return Direction.NORTH;
    }
}
