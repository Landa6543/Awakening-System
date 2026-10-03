package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.MeditationSeatEntity;
import com.soybe.awakeningsystem.item.CreatorClaymoreItem;
import com.soybe.awakeningsystem.item.LaDoncellaSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * Motor de renderizado volumétrico de auras 3D para Awakening System.
 * Genera un Manto Espiritual Completo (Body Shroud Dome) que envuelve por completo
 * el cuerpo del jugador (pies, piernas, torso, brazos, hombros y cierra en cúpula sobre la cabeza),
 * complementado con llamas etéreas internas que cruzan el núcleo del cuerpo para eliminar
 * el aspecto de cilindro hueco:
 * - Aura de Cultivo Ki: manto espiritual blanco perlado y celeste que envuelve al meditador sentado.
 * - Aura Celestial del Creador: amatista cósmica y resplandor divino permanente al empuñar el Mandoble del Creador o Mahiro.
 * - Aura de Habilidad Despierta: resplandor espiritual azul celeste durante trances y habilidades.
 */
public final class VolumetricAuraRenderer {

    public static final ResourceLocation TEX_AURA_FLAME = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/aura_flame.png");
    public static final ResourceLocation TEX_AURA_KI = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/aura_ki_stream.png");
    public static final ResourceLocation TEX_AURA_LIGHTNING = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/aura_lightning.png");
    public static final ResourceLocation TEX_AURA_RING = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/aura_ground_ring.png");

    private static final int SEGMENTS = 20;

    private VolumetricAuraRenderer() {}

    public static void renderAuras(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float time = mc.level.getGameTime() + partialTick;
        Vec3 camPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        var bufferSource = mc.renderBuffers().bufferSource();

        for (Player player : mc.level.players()) {
            if (!player.isAlive()) continue;

            // En primera persona deshabilitamos el aura sobre uno mismo para no tapar la vista
            if (player == mc.player && mc.options.getCameraType().isFirstPerson()) {
                continue;
            }

            double dx = Mth.lerp(partialTick, player.xo, player.getX()) - camPos.x;
            double dy = Mth.lerp(partialTick, player.yo, player.getY()) - camPos.y;
            double dz = Mth.lerp(partialTick, player.zo, player.getZ()) - camPos.z;

            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > 64.0D * 64.0D) continue; // Descarte de distancia (64 bloques)

            boolean isMeditating = player.getVehicle() instanceof MeditationSeatEntity
                    || player.getPersistentData().getBoolean("is_cultivating_qi");

            if (isMeditating) {
                if (player.getVehicle() instanceof MeditationSeatEntity seat) {
                    dx = Mth.lerp(partialTick, seat.xo, seat.getX()) - camPos.x;
                    dy = Mth.lerp(partialTick, seat.yo, seat.getY()) - camPos.y;
                    dz = Mth.lerp(partialTick, seat.zo, seat.getZ()) - camPos.z;
                } else {
                    dy = Math.floor(player.getY()) - camPos.y;
                }
            }

            boolean hasCreatorSword = player.getMainHandItem().getItem() instanceof CreatorClaymoreItem
                    || player.getOffhandItem().getItem() instanceof CreatorClaymoreItem
                    || player.getMainHandItem().getItem() instanceof LaDoncellaSwordItem
                    || player.getOffhandItem().getItem() instanceof LaDoncellaSwordItem;

            boolean inCombatTrance = player.getPersistentData().getLong("system_trance_until") > mc.level.getGameTime();
            boolean inSkillBurst = player.getPersistentData().getLong("active_skill_aura_until") > mc.level.getGameTime();

            // Determinar estilo de aura predominante
            if (isMeditating) {
                // 1. AURA DE CULTIVO KI (Manto espiritual completo en cúpula que envuelve al meditador de pies a cabeza)
                renderKiCultivationAura(poseStack, bufferSource, dx, dy, dz, time);
            } else if (hasCreatorSword) {
                // 2. AURA CELESTIAL DEL CREADOR (Permanente al empuñar el mandoble mítico, intensificada en combate)
                renderCreatorDivineAura(poseStack, bufferSource, dx, dy, dz, time, inCombatTrance || inSkillBurst);
            } else if (inCombatTrance || inSkillBurst) {
                // 3. AURA DE HABILIDAD DESPIERTA / TRANCE (Manto etéreo azul ceñido al cuerpo)
                renderAwakenedSkillAura(poseStack, bufferSource, dx, dy, dz, time);
            }
        }
    }

    /**
     * Renderiza el Aura de Cultivo Ki: Manto espiritual blanco perlado y celeste que envuelve
     * completamente al cultivador sentado desde el suelo hasta la cabeza, con llamas internas y cúpula cerrada.
     */
    private static void renderKiCultivationAura(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource,
                                                double x, double y, double z, float time) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        VertexConsumer kiBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(TEX_AURA_KI));

        // 1. Llamas interiores que atraviesan el núcleo del cuerpo desde el suelo hasta la cabeza
        renderInteriorCoreFlames(poseStack, kiBuffer, 1.15F, 1.65F, time * 0.05F, 245, 252, 255, 130);

        // 2. Manto espiritual íntimo en cúpula (Blanco perlado)
        renderBodyAuraShroud(poseStack, kiBuffer, 0.95F, 1.70F, time * 0.04F, 0.03F,
                240, 248, 255, 140, true, true);

        // 3. Manto exterior ondulante de condensación de Qi (Celeste translúcido)
        poseStack.mulPose(Axis.YP.rotationDegrees(-time * 1.2F));
        renderBodyAuraShroud(poseStack, kiBuffer, 1.15F, 1.80F, -time * 0.05F, 0.04F,
                160, 230, 255, 90, true, true);

        // 4. Círculo Mágico de Cultivo Murim en el suelo (sincronizado al 100% con la cúpula de Ki)
        renderCultivationMurimCircle(poseStack, bufferSource, time);

        poseStack.popPose();
    }

    /**
     * Renderiza la formación de cultivo Murim en el suelo con doble sello concéntrico contra-rotante.
     */
    private static void renderCultivationMurimCircle(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, float time) {
        poseStack.pushPose();
        VertexConsumer murimBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(com.soybe.awakeningsystem.AwakeningSystemClient.MAGIC_CIRCLE_MURIM));

        float outerRadius = 1.95F + 0.05F * (float) Math.sin(time * 0.08F);
        float y = 0.025F;

        // Sello exterior giratorio (Verde espiritual esmeralda / turquesa celestial)
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 0.9F));
        renderGroundDiscDirect(poseStack, murimBuffer, outerRadius, y, 90, 245, 175, 210);

        // Sello interior concéntrico contra-rotante (Blanco perlado / celeste)
        poseStack.mulPose(Axis.YP.rotationDegrees(-time * 2.2F));
        float innerRadius = outerRadius * 0.65F;
        renderGroundDiscDirect(poseStack, murimBuffer, innerRadius, y + 0.005F, 190, 255, 240, 230);

        poseStack.popPose();
    }

    private static void renderGroundDiscDirect(PoseStack poseStack, VertexConsumer builder, float radius, float y, int r, int g, int b, int a) {
        Matrix4f matrix = poseStack.last().pose();

        builder.addVertex(matrix, -radius, y, -radius).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, radius, y, -radius).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, radius, y, radius).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, -radius, y, radius).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);

        builder.addVertex(matrix, -radius, y, radius).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        builder.addVertex(matrix, radius, y, radius).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        builder.addVertex(matrix, radius, y, -radius).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        builder.addVertex(matrix, -radius, y, -radius).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
    }

    /**
     * Renderiza el Aura de Habilidad Despierta: energía espiritual etérea azul que cubre
     * de pies a cabeza con cúpula sobre la coronilla y llamas de alma a través del pecho.
     */
    private static void renderAwakenedSkillAura(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource,
                                                double x, double y, double z, float time) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        VertexConsumer flameBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(TEX_AURA_FLAME));

        // 1. Llamas interiores cruzadas que suben por el torso y cabeza
        renderInteriorCoreFlames(poseStack, flameBuffer, 0.85F, 2.10F, time * 0.12F, 110, 205, 255, 135);

        // 2. Manto espiritual ceñido en cúpula (Azul cian)
        renderBodyAuraShroud(poseStack, flameBuffer, 0.58F, 2.15F, time * 0.10F, 0.07F,
                100, 190, 255, 140, false, false);

        // 3. Capa exterior de vapor arcano
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.6F));
        renderBodyAuraShroud(poseStack, flameBuffer, 0.74F, 2.30F, -time * 0.12F, 0.09F,
                60, 140, 255, 90, false, false);

        // 4. Anillo de impacto en la base
        renderGroundDisc(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TEX_AURA_RING)),
                1.15F + 0.05F * (float) Math.sin(time * 0.18F), 100, 200, 255, 120);

        poseStack.popPose();
    }

    /**
     * Renderiza el Aura Divina del Creador: amatista cósmica y resplandor violeta
     * continuo que envuelve todo el cuerpo al empuñar el mandoble mítico.
     */
    private static void renderCreatorDivineAura(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource,
                                                double x, double y, double z, float time, boolean intensified) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        VertexConsumer flameBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(TEX_AURA_FLAME));

        int coreAlpha = intensified ? 175 : 135;
        int shroudAlpha = intensified ? 165 : 125;
        int outerAlpha = intensified ? 110 : 80;

        // 1. Llamas cósmicas interiores que cruzan el cuerpo del portador
        renderInteriorCoreFlames(poseStack, flameBuffer, 0.90F, 2.20F, time * (intensified ? 0.16F : 0.10F),
                185, 100, 255, coreAlpha);

        // 2. Manto espiritual cósmico en cúpula (Amatista primordial)
        renderBodyAuraShroud(poseStack, flameBuffer, 0.60F, 2.22F, time * 0.09F, 0.08F,
                170, 80, 240, shroudAlpha, false, false);

        // 3. Capa externa estelar lila
        poseStack.mulPose(Axis.YP.rotationDegrees(-time * 1.5F));
        renderBodyAuraShroud(poseStack, flameBuffer, 0.78F, 2.38F, -time * 0.11F, 0.10F,
                230, 180, 255, outerAlpha, false, false);

        // 4. Relámpagos arcanos violetas suaves
        if (intensified || Math.sin(time * 0.25F) > 0.2D) {
            VertexConsumer lightningBuf = bufferSource.getBuffer(RenderType.entityTranslucent(TEX_AURA_LIGHTNING));
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.8F));
            renderBodyAuraShroud(poseStack, lightningBuf, 0.68F, 2.25F, time * 0.18F, 0.05F,
                    240, 195, 255, intensified ? 140 : 100, false, false);
            poseStack.popPose();
        }

        // 5. Disco cósmico en el suelo
        renderGroundDisc(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TEX_AURA_RING)),
                1.20F + 0.06F * (float) Math.sin(time * 0.15F), 210, 130, 255, intensified ? 145 : 110);

        poseStack.popPose();
    }

    /**
     * Renderiza llamas etéreas interiores que cruzan diametralmente el centro del jugador
     * en 3 ángulos (0°, 60°, 120°), inundando el torso, cabeza y extremidades con energía espiritual.
     */
    private static void renderInteriorCoreFlames(PoseStack poseStack, VertexConsumer builder,
                                                float width, float height, float vOffset,
                                                int r, int g, int b, int a) {
        Matrix4f matrix = poseStack.last().pose();
        float halfW = width * 0.5F;

        for (int p = 0; p < 3; p++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(p * 60.0F));
            Matrix4f m = poseStack.last().pose();

            // Cara frontal y trasera del plano de llama cruzada
            addVertex(builder, m, -halfW, 0.0F, 0.0F, 0.0F, 1.0F + vOffset, r, g, b, 0);
            addVertex(builder, m, halfW, 0.0F, 0.0F, 1.0F, 1.0F + vOffset, r, g, b, 0);
            addVertex(builder, m, halfW, height, 0.0F, 1.0F, 0.0F + vOffset, r, g, b, a);
            addVertex(builder, m, -halfW, height, 0.0F, 0.0F, 0.0F + vOffset, r, g, b, a);

            addVertex(builder, m, -halfW, height, 0.0F, 0.0F, 0.0F + vOffset, r, g, b, a);
            addVertex(builder, m, halfW, height, 0.0F, 1.0F, 0.0F + vOffset, r, g, b, a);
            addVertex(builder, m, halfW, 0.0F, 0.0F, 1.0F, 1.0F + vOffset, r, g, b, 0);
            addVertex(builder, m, -halfW, 0.0F, 0.0F, 0.0F, 1.0F + vOffset, r, g, b, 0);

            poseStack.popPose();
        }
    }

    /**
     * Genera la malla 3D de un Manto Espiritual Completo (Body Shroud Dome):
     * Malla de 5 anillos concéntricos que cubre los pies, cadera, hombros, y se curva
     * cerrándose herméticamente en una cúpula por encima de la cabeza, cubriendo todo el cuerpo.
     */
    private static void renderBodyAuraShroud(PoseStack poseStack, VertexConsumer builder,
                                            float baseRadius, float height, float vOffset, float waveAmplitude,
                                            int r, int g, int b, int a, boolean gentle, boolean sitting) {
        Matrix4f matrix = poseStack.last().pose();
        float angleStep = (float) (2.0 * Math.PI / SEGMENTS);

        // Definición de las 5 alturas y radios del manto corporal
        float[] ringY;
        float[] ringR;

        if (sitting) {
            // Perfil ceñido al cuerpo sentado en posición de loto (cubre piernas cruzadas y cierra sobre cabeza)
            ringY = new float[] { 0.00F, height * 0.25F, height * 0.55F, height * 0.82F, height * 1.05F };
            ringR = new float[] { baseRadius * 1.15F, baseRadius * 1.25F, baseRadius * 0.95F, baseRadius * 0.60F, 0.03F };
        } else {
            // Perfil ceñido al cuerpo erguido (pies, torso, hombros y cúpula cerrada sobre la coronilla)
            ringY = new float[] { -0.05F, height * 0.35F, height * 0.70F, height * 0.90F, height * 1.08F };
            ringR = new float[] { baseRadius * 0.90F, baseRadius * 1.05F, baseRadius * 1.00F, baseRadius * 0.62F, 0.03F };
        }

        int tiers = ringY.length - 1;

        for (int t = 0; t < tiers; t++) {
            float yBot = ringY[t];
            float yTop = ringY[t + 1];
            float rBotBase = ringR[t];
            float rTopBase = ringR[t + 1];

            float vBot = 1.0F - (float) t / tiers + vOffset;
            float vTop = 1.0F - (float) (t + 1) / tiers + vOffset;

            // Opacidad gradual: suave en los pies, plena en el cuerpo, suavemente difusa en la corona
            int aBot = (t == 0) ? (int)(a * 0.3F) : a;
            int aTop = (t == tiers - 1) ? (int)(a * 0.65F) : a;

            for (int i = 0; i < SEGMENTS; i++) {
                float theta0 = i * angleStep;
                float theta1 = (i + 1) * angleStep;

                float wave0 = 1.0F + waveAmplitude * (float) Math.sin(vOffset * 6.0F + theta0 * 3.0F);
                float wave1 = 1.0F + waveAmplitude * (float) Math.sin(vOffset * 6.0F + theta1 * 3.0F);

                float r0_bot = rBotBase;
                float r1_bot = rBotBase;
                float r0_top = rTopBase * (gentle ? 1.02F : 1.10F) * wave0;
                float r1_top = rTopBase * (gentle ? 1.02F : 1.10F) * wave1;

                float x0_bot = (float) Math.cos(theta0) * r0_bot;
                float z0_bot = (float) Math.sin(theta0) * r0_bot;
                float x1_bot = (float) Math.cos(theta1) * r1_bot;
                float z1_bot = (float) Math.sin(theta1) * r1_bot;

                float x0_top = (float) Math.cos(theta0) * r0_top;
                float z0_top = (float) Math.sin(theta0) * r0_top;
                float x1_top = (float) Math.cos(theta1) * r1_top;
                float z1_top = (float) Math.sin(theta1) * r1_top;

                float u0 = (float) i / SEGMENTS;
                float u1 = (float) (i + 1) / SEGMENTS;

                // Doble cara para que el manto sea visible tanto desde el exterior como desde el interior
                addVertex(builder, matrix, x0_bot, yBot, z0_bot, u0, vBot, r, g, b, aBot);
                addVertex(builder, matrix, x1_bot, yBot, z1_bot, u1, vBot, r, g, b, aBot);
                addVertex(builder, matrix, x1_top, yTop, z1_top, u1, vTop, r, g, b, aTop);
                addVertex(builder, matrix, x0_top, yTop, z0_top, u0, vTop, r, g, b, aTop);

                addVertex(builder, matrix, x0_top, yTop, z0_top, u0, vTop, r, g, b, aTop);
                addVertex(builder, matrix, x1_top, yTop, z1_top, u1, vTop, r, g, b, aTop);
                addVertex(builder, matrix, x1_bot, yBot, z1_bot, u1, vBot, r, g, b, aBot);
                addVertex(builder, matrix, x0_bot, yBot, z0_bot, u0, vBot, r, g, b, aBot);
            }
        }
    }

    /**
     * Renderiza el disco de choque horizontal en el suelo.
     */
    private static void renderGroundDisc(PoseStack poseStack, VertexConsumer builder,
                                        float radius, int r, int g, int b, int a) {
        Matrix4f matrix = poseStack.last().pose();
        float y = 0.02F;

        builder.addVertex(matrix, -radius, y, -radius).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, radius, y, -radius).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, radius, y, radius).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, -radius, y, radius).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);

        builder.addVertex(matrix, -radius, y, radius).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        builder.addVertex(matrix, radius, y, radius).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        builder.addVertex(matrix, radius, y, -radius).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        builder.addVertex(matrix, -radius, y, -radius).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
    }

    private static void addVertex(VertexConsumer builder, Matrix4f matrix,
                                  float x, float y, float z, float u, float v,
                                  int r, int g, int b, int a) {
        builder.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 1, 0);
    }
}
