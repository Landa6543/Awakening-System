package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.effect.ModMobEffects;
import com.soybe.awakeningsystem.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Motor visual de Efectos y Animaciones para las Pociones Abisales:
 * - Eco Abisal: Pulso de onda sónica cian en el suelo y ecolocalización de radar.
 * - Densidad Gravitacional: Disco de acreción gravitacional violeta oscuro y partículas de masa densa.
 * - Resonancia Abisal: Tríada de orbes de maná en órbita tridimensional armónica con intensificación en las profundidades.
 */
public final class AbyssalPotionVfxRenderer {
    private AbyssalPotionVfxRenderer() {}

    public static final ResourceLocation TEX_SONAR_RING =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/aura_ground_ring.png");

    public static void tickClientParticles(Minecraft mc) {
        if (mc.level == null) return;
        var players = mc.level.players();
        if (players.isEmpty()) return;

        float time = mc.level.getGameTime();

        for (Player player : players) {
            if (!player.isAlive()) continue;

            boolean hasEcho = player.hasEffect(ModMobEffects.ABYSSAL_ECHO);
            boolean hasGrav = player.hasEffect(ModMobEffects.GRAVITATIONAL_DENSITY);
            boolean hasReso = player.hasEffect(ModMobEffects.ABYSSAL_RESONANCE);

            if (!hasEcho && !hasGrav && !hasReso) continue;

            boolean isLocalFirstPerson = (player instanceof LocalPlayer && mc.options.getCameraType().isFirstPerson());

            // 1. Eco Abisal: Anillo sónico de ecolocalización
            if (hasEcho) {
                int cycle = (int) (time % 20);
                if (cycle < 6) {
                    double pulseRadius = 0.5D + (cycle * 0.4D);
                    int count = 6;
                    for (int i = 0; i < count; i++) {
                        double angle = (2 * Math.PI * i) / count + (cycle * 0.15D);
                        double px = player.getX() + Math.cos(angle) * pulseRadius;
                        double py = player.getY() + (isLocalFirstPerson ? 0.05D : 0.15D);
                        double pz = player.getZ() + Math.sin(angle) * pulseRadius;
                        mc.level.addParticle(ModParticles.MANA_AURA.get(), px, py, pz,
                                Math.cos(angle) * 0.03D, 0.01D, Math.sin(angle) * 0.03D);
                    }
                }
            }

            // 2. Densidad Gravitacional: Absorción vertical hacia la tierra (masa densa)
            if (hasGrav) {
                if (mc.level.random.nextFloat() < 0.45F) {
                    double angle = mc.level.random.nextDouble() * Math.PI * 2.0D;
                    double r = 0.35D + mc.level.random.nextDouble() * 0.35D;
                    double px = player.getX() + Math.cos(angle) * r;
                    double py = player.getY() + (isLocalFirstPerson ? 0.1D : 0.6D);
                    double pz = player.getZ() + Math.sin(angle) * r;
                    mc.level.addParticle(ModParticles.SHADOW_AURA.get(), px, py, pz, 0.0D, -0.04D, 0.0D);
                }
            }

            // 3. Resonancia Abisal: Órbita armónica de 3 orbes de maná alrededor del torso
            if (hasReso) {
                boolean deep = player.getY() <= -10.0D;
                float speed = deep ? 0.16F : 0.08F;
                double orbitRadius = 0.75D;

                for (int i = 0; i < 3; i++) {
                    double orbitAngle = (time * speed) + (i * (2.0D * Math.PI / 3.0D));
                    double px = player.getX() + Math.cos(orbitAngle) * orbitRadius;
                    double py = player.getY() + 0.85D + Math.sin(time * 0.12F + i) * 0.15D;
                    double pz = player.getZ() + Math.sin(orbitAngle) * orbitRadius;

                    if (isLocalFirstPerson) {
                        py = player.getY() + 0.25D;
                    }

                    if (mc.level.random.nextFloat() < (deep ? 0.9F : 0.6F)) {
                        mc.level.addParticle(ModParticles.MANA_AURA.get(), px, py, pz,
                                -Math.sin(orbitAngle) * 0.02D, 0.005D, Math.cos(orbitAngle) * 0.02D);
                    }
                }
            }
        }
    }

    public static void renderPotionAuras(PoseStack poseStack, MultiBufferSource bufferSource,
                                         Player player, double x, double y, double z, float time) {
        boolean hasEcho = player.hasEffect(ModMobEffects.ABYSSAL_ECHO);
        boolean hasGrav = player.hasEffect(ModMobEffects.GRAVITATIONAL_DENSITY);
        boolean hasReso = player.hasEffect(ModMobEffects.ABYSSAL_RESONANCE);

        if (!hasEcho && !hasGrav && !hasReso) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        VertexConsumer ringBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(VolumetricAuraRenderer.TEX_AURA_RING));

        // 1. Eco Abisal: Disco sónico cian pulsante
        if (hasEcho) {
            float pulse = (float) (0.5D + 0.5D * Math.sin(time * 0.25F));
            float radius = 1.35F + (pulse * 0.45F);
            int alpha = (int) (80 + pulse * 75);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.5F));
            VolumetricAuraRenderer.renderGroundDisc(poseStack, ringBuffer, radius, 0, 229, 255, alpha);
            poseStack.popPose();
        }

        // 2. Densidad Gravitacional: Disco gravitacional púrpura oscuro
        if (hasGrav) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-time * 2.2F));
            VolumetricAuraRenderer.renderGroundDisc(poseStack, ringBuffer, 1.15F, 49, 27, 146, 160);
            poseStack.popPose();
        }

        // 3. Resonancia Abisal: Disco resonante magenta
        if (hasReso) {
            boolean deep = player.getY() <= -10.0D;
            float pulse = (float) (0.6D + 0.4D * Math.sin(time * (deep ? 0.35F : 0.18F)));
            int alpha = (int) (deep ? 165 : 110);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(time * (deep ? 3.0F : 1.8F)));
            VolumetricAuraRenderer.renderGroundDisc(poseStack, ringBuffer, 1.25F * pulse, 142, 36, 170, alpha);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
