package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.item.AwakeningWeaponItem;
import com.soybe.awakeningsystem.item.CreatorClaymoreItem;
import com.soybe.awakeningsystem.item.LaDoncellaSwordItem;
import com.soybe.awakeningsystem.item.PlumBlossomSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

/**
 * Renderizador de Aura de Espada 3D (Sword Qi / Geomgi / 剑气).
 * En lugar de envolver el cuerpo del jugador en un capullo o cilindro,
 * envuelve directamente la hoja de las armas míticas y de cultivo con
 * un flujo etéreo tridimensional de energía espiritual, fiel al estilo
 * de los manhwas de acción y artes marciales (Solo Leveling, Monte Hua, etc.).
 */
public final class SwordAuraRenderer {

    public static final ResourceLocation TEX_AURA_FLAME = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/aura_flame.png");
    public static final ResourceLocation TEX_AURA_KI = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/aura_ki_stream.png");
    public static final ResourceLocation TEX_AURA_LIGHTNING = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/aura_lightning.png");

    // Constantes matemáticas de orientación del filo de item/handheld en Minecraft
    private static final float INV_SQRT2 = 0.70710678F;
    // Vector unitario a lo largo de la hoja (diagonal desde el mango hacia la punta)
    private static final float UX = INV_SQRT2;
    private static final float UY = INV_SQRT2;
    // Vector unitario perpendicular en el plano de la hoja (anchura)
    private static final float VX = -INV_SQRT2;
    private static final float VY = INV_SQRT2;

    private SwordAuraRenderer() {}

    public record AuraStyle(
            int r1, int g1, int b1, int a1, // Núcleo interior brillante
            int r2, int g2, int b2, int a2, // Manto exterior etéreo
            ResourceLocation texture,
            ParticleOptions particle,
            float widthMultiplier,
            float lengthMultiplier
    ) {}

    public static boolean hasSwordAura(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof LaDoncellaSwordItem
                || item instanceof CreatorClaymoreItem
                || item instanceof PlumBlossomSwordItem
                || item instanceof AwakeningWeaponItem;
    }

    public static AuraStyle getAuraStyle(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof LaDoncellaSwordItem) {
            boolean transformed = LaDoncellaSwordItem.isTransformed(stack);
            if (transformed) {
                // Forma Despierta: Trono de Ceniza y Luna Carmesí
                // Llama de sangre profunda y sombras del vacío
                return new AuraStyle(
                        255, 25, 70, 235,
                        180, 15, 60, 160,
                        TEX_AURA_FLAME,
                        ParticleTypes.CRIMSON_SPORE,
                        1.20F, 1.25F
                );
            } else {
                // Forma Pura: Acero de las Rosas y Fragancia del Silencio
                // Resplandor oro-rosado celestial y platino brillante
                return new AuraStyle(
                        255, 195, 225, 220,
                        245, 145, 205, 145,
                        TEX_AURA_FLAME,
                        ParticleTypes.CHERRY_LEAVES,
                        1.12F, 1.20F
                );
            }
        }
        if (item instanceof CreatorClaymoreItem) {
            // Mandoble del Creador: Amatista cósmica y resplandor primordial
            return new AuraStyle(
                    225, 120, 255, 230,
                    160, 50, 245, 155,
                    TEX_AURA_FLAME,
                    ParticleTypes.PORTAL,
                    1.28F, 1.30F
            );
        }
        if (item instanceof PlumBlossomSwordItem) {
            // Espada de Flor de Ciruelo (Monte Hua): Qi de pétalos florecientes
            return new AuraStyle(
                    255, 110, 185, 220,
                    255, 175, 210, 140,
                    TEX_AURA_KI,
                    ParticleTypes.CHERRY_LEAVES,
                    1.08F, 1.15F
            );
        }
        String name = item.getDescriptionId().toLowerCase();
        if (name.contains("demon") || name.contains("shadow") || name.contains("dread")) {
            // Demoniaco / Sombras
            return new AuraStyle(
                    210, 20, 40, 225,
                    75, 10, 30, 165,
                    TEX_AURA_FLAME,
                    ParticleTypes.SMOKE,
                    1.12F, 1.22F
            );
        }
        if (name.contains("radiant") || name.contains("holy") || name.contains("faith") || name.contains("asrahan")) {
            // Sagrado / Radiante
            return new AuraStyle(
                    255, 235, 110, 225,
                    255, 185, 40, 150,
                    TEX_AURA_KI,
                    ParticleTypes.ELECTRIC_SPARK,
                    1.10F, 1.20F
            );
        }
        // General para cualquier AwakeningWeaponItem: Qi celestial azul
        return new AuraStyle(
                110, 220, 255, 215,
                45, 150, 245, 140,
                TEX_AURA_KI,
                ParticleTypes.SOUL_FIRE_FLAME,
                1.05F, 1.15F
        );
    }

    /**
     * Renderiza el aura de espada en vista de tercera persona (F5 y otros jugadores).
     */
    public static void renderThirdPerson(Player player, RenderLivingEvent.Post<?, ?> event) {
        if (!(event.getRenderer().getModel() instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Si es el jugador local en primera persona, no renderizar aquí (se maneja en renderFirstPerson)
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        float partialTick = event.getPartialTick();
        float time = mc.level.getGameTime() + partialTick;

        // 1. Mano Principal
        ItemStack mainStack = player.getMainHandItem();
        if (hasSwordAura(mainStack)) {
            renderAuraForArm(player, humanoidModel, poseStack, bufferSource, mainStack, player.getMainArm(), false, time);
        }

        // 2. Mano Secundaria (Offhand)
        ItemStack offStack = player.getOffhandItem();
        if (hasSwordAura(offStack)) {
            HumanoidArm offArm = (player.getMainArm() == HumanoidArm.RIGHT) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            renderAuraForArm(player, humanoidModel, poseStack, bufferSource, offStack, offArm, true, time);
        }
    }

    private static void renderAuraForArm(Player player, HumanoidModel<?> humanoidModel,
                                         PoseStack poseStack, MultiBufferSource bufferSource,
                                         ItemStack stack, HumanoidArm arm, boolean isOffhand, float time) {
        poseStack.pushPose();

        // Trasladar a la mano usando el modelo humanoide
        humanoidModel.translateToHand(arm, poseStack);

        // Transformación estándar de ItemInHandLayer en Minecraft
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean isLeft = (arm == HumanoidArm.LEFT);
        poseStack.translate((isLeft ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);

        // Aplicar la transformación de tercera persona definida en el JSON del item
        ItemDisplayContext displayContext = isLeft ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        Minecraft mc = Minecraft.getInstance();
        BakedModel bakedModel = mc.getItemRenderer().getModel(stack, player.level(), player, 0);
        if (bakedModel != null) {
            bakedModel.getTransforms().getTransform(displayContext).apply(isLeft, poseStack);
        }
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        // Renderizar la malla geométrica del aura de espada
        renderSwordAuraMesh(poseStack, bufferSource, stack, time);

        poseStack.popPose();
    }

    /**
     * Renderiza el aura de espada en vista de primera persona.
     */
    public static void renderFirstPerson(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        ItemStack stack = event.getItemStack();
        if (!hasSwordAura(stack)) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        float time = mc.level.getGameTime() + event.getPartialTick();

        poseStack.pushPose();

        boolean isMainHand = (event.getHand() == InteractionHand.MAIN_HAND);
        HumanoidArm arm = isMainHand ? player.getMainArm() : (player.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
        boolean isLeft = (arm == HumanoidArm.LEFT);
        ItemDisplayContext displayContext = isLeft ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

        BakedModel bakedModel = mc.getItemRenderer().getModel(stack, player.level(), player, 0);
        if (bakedModel != null) {
            bakedModel.getTransforms().getTransform(displayContext).apply(isLeft, poseStack);
        }
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        renderSwordAuraMesh(poseStack, bufferSource, stack, time);

        poseStack.popPose();
    }

    /**
     * Construye la malla volumétrica 3D del Qi de espada (Geomgi).
     * Compuesta por:
     * - Plano frontal y trasero a lo largo del filo (anchura)
     * - Plano transversal perpendicular a través del grosor de la hoja (volumen 3D)
     * - Punta de Qi afilada que se extiende más allá de la punta metálica
     * - Capa externa de ondas de llamas etéreas con scrolling UV continuo
     */
    private static void renderSwordAuraMesh(PoseStack poseStack, MultiBufferSource bufferSource,
                                            ItemStack stack, float time) {
        AuraStyle style = getAuraStyle(stack);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityTranslucent(style.texture()));
        Matrix4f matrix = poseStack.last().pose();

        int segments = 8;
        float baseHilt = 0.22F; // Inicio del aura justo sobre la cruceta/guarda
        float maxReach = 1.414F * style.lengthMultiplier(); // Extensión más allá de la punta (espada de qi)

        float scrollCore = -time * 0.16F;
        float scrollOuter = -time * 0.24F;
        float pulse = 1.0F + 0.08F * (float) Math.sin(time * 0.22F);

        // Pre-calcular perfiles a lo largo de los segmentos
        float[] sVals = new float[segments + 1];
        float[] halfWVals = new float[segments + 1];
        float[] halfTVals = new float[segments + 1];
        float[] alphaVals = new float[segments + 1];

        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            float s = baseHilt + t * (maxReach - baseHilt);
            sVals[i] = s;

            // Perfil de anchura con conicidad hacia la punta extendida
            float profile = (float) Math.sin(t * Math.PI) * 0.22F + (1.0F - t) * 0.05F;
            if (t > 0.82F) {
                // Afilar la punta de energía de geomgi
                profile *= (1.0F - t) / 0.18F;
            }
            float hw = profile * style.widthMultiplier() * pulse;
            halfWVals[i] = hw;
            halfTVals[i] = hw * 0.65F;

            // Factor de desvanecimiento en extremos
            float aFactor = (t < 0.15F) ? (t / 0.15F) : ((t > 0.85F) ? (1.0F - t) / 0.15F : 1.0F);
            alphaVals[i] = aFactor;
        }

        // ==========================================
        // 1. CAPA INTERIOR: NÚCLEO DE QI DE ESPADA
        // ==========================================
        for (int i = 0; i < segments; i++) {
            float s0 = sVals[i];
            float s1 = sVals[i + 1];
            float hw0 = halfWVals[i];
            float hw1 = halfWVals[i + 1];
            float ht0 = halfTVals[i];
            float ht1 = halfTVals[i + 1];

            float t0 = (float) i / segments;
            float t1 = (float) (i + 1) / segments;
            float v0 = t0 * 1.5F + scrollCore;
            float v1 = t1 * 1.5F + scrollCore;

            int a0 = (int) (style.a1() * alphaVals[i]);
            int a1 = (int) (style.a1() * alphaVals[i + 1]);

            // Centro en la hoja
            float cx0 = s0 * UX;
            float cy0 = s0 * UY;
            float cx1 = s1 * UX;
            float cy1 = s1 * UY;
            float cz = 0.5F;

            // --- Plano A: En el filo (Anchura XY) ---
            float ax0 = cx0 - hw0 * VX;
            float ay0 = cy0 - hw0 * VY;
            float bx0 = cx0 + hw0 * VX;
            float by0 = cy0 + hw0 * VY;

            float dx1 = cx1 - hw1 * VX;
            float dy1 = cy1 - hw1 * VY;
            float cx1P = cx1 + hw1 * VX;
            float cy1P = cy1 + hw1 * VY;

            // Cara frontal y trasera
            addQuad(builder, matrix,
                    ax0, ay0, cz,
                    bx0, by0, cz,
                    cx1P, cy1P, cz,
                    dx1, dy1, cz,
                    0.0F, 1.0F, v0, v1,
                    style.r1(), style.g1(), style.b1(), a0, a1);

            // --- Plano B: Transversal (Grosor Z) para volumen 3D completo ---
            addQuad(builder, matrix,
                    cx0, cy0, cz - ht0,
                    cx0, cy0, cz + ht0,
                    cx1, cy1, cz + ht1,
                    cx1, cy1, cz - ht1,
                    0.0F, 1.0F, v0, v1,
                    style.r1(), style.g1(), style.b1(), a0, a1);
        }

        // ==========================================
        // 2. CAPA EXTERIOR: MANTO DE LLAMAS ETÉREAS
        // ==========================================
        for (int i = 0; i < segments; i++) {
            float s0 = sVals[i];
            float s1 = sVals[i + 1];
            float hw0 = halfWVals[i] * 1.35F;
            float hw1 = halfWVals[i + 1] * 1.35F;
            float ht0 = halfTVals[i] * 1.35F;
            float ht1 = halfTVals[i + 1] * 1.35F;

            float t0 = (float) i / segments;
            float t1 = (float) (i + 1) / segments;
            float v0 = t0 * 1.8F + scrollOuter;
            float v1 = t1 * 1.8F + scrollOuter;

            int a0 = (int) (style.a2() * alphaVals[i]);
            int a1 = (int) (style.a2() * alphaVals[i + 1]);

            float cx0 = s0 * UX;
            float cy0 = s0 * UY;
            float cx1 = s1 * UX;
            float cy1 = s1 * UY;
            float cz = 0.5F;

            float ax0 = cx0 - hw0 * VX;
            float ay0 = cy0 - hw0 * VY;
            float bx0 = cx0 + hw0 * VX;
            float by0 = cy0 + hw0 * VY;

            float dx1 = cx1 - hw1 * VX;
            float dy1 = cy1 - hw1 * VY;
            float cx1P = cx1 + hw1 * VX;
            float cy1P = cy1 + hw1 * VY;

            addQuad(builder, matrix,
                    ax0, ay0, cz,
                    bx0, by0, cz,
                    cx1P, cy1P, cz,
                    dx1, dy1, cz,
                    0.0F, 1.0F, v0, v1,
                    style.r2(), style.g2(), style.b2(), a0, a1);

            addQuad(builder, matrix,
                    cx0, cy0, cz - ht0,
                    cx0, cy0, cz + ht0,
                    cx1, cy1, cz + ht1,
                    cx1, cy1, cz - ht1,
                    0.0F, 1.0F, v0, v1,
                    style.r2(), style.g2(), style.b2(), a0, a1);
        }
    }

    private static void addQuad(VertexConsumer builder, Matrix4f m,
                                float x0, float y0, float z0,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float x3, float y3, float z3,
                                float u0, float u1, float v0, float v1,
                                int r, int g, int b, int a0, int a1) {
        // Cara Directa
        builder.addVertex(m, x0, y0, z0).setColor(r, g, b, a0).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(m, x1, y1, z1).setColor(r, g, b, a0).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(m, x2, y2, z2).setColor(r, g, b, a1).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(m, x3, y3, z3).setColor(r, g, b, a1).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);

        // Cara Inversa (Doble cara para visibilidad 360°)
        builder.addVertex(m, x3, y3, z3).setColor(r, g, b, a1).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        builder.addVertex(m, x2, y2, z2).setColor(r, g, b, a1).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        builder.addVertex(m, x1, y1, z1).setColor(r, g, b, a0).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        builder.addVertex(m, x0, y0, z0).setColor(r, g, b, a0).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
    }

    /**
     * Emite partículas sutiles de Qi a lo largo de la hoja del arma en el mundo cada tick.
     */
    public static void tickParticles(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        LocalPlayer player = mc.player;
        if (!player.isAlive()) return;

        ItemStack stack = player.getMainHandItem();
        if (!hasSwordAura(stack)) {
            stack = player.getOffhandItem();
        }
        if (!hasSwordAura(stack)) return;

        AuraStyle style = getAuraStyle(stack);
        if (style.particle() == null) return;

        // Emitir 1 o 2 partículas por tick flotando cerca de la hoja
        if (player.getRandom().nextFloat() < 0.65F) {
            Vec3 look = player.getLookAngle();
            Vec3 up = new Vec3(0, 1, 0);
            Vec3 right = look.cross(up).normalize();

            // Posición de la mano derecha con respecto a la vista
            double handX = player.getX() + right.x * 0.42D + look.x * 0.55D;
            double handY = player.getY() + player.getEyeHeight() * 0.68D + look.y * 0.55D;
            double handZ = player.getZ() + right.z * 0.42D + look.z * 0.55D;

            // Desplazar a lo largo de la hoja
            float bladeFrac = player.getRandom().nextFloat();
            double px = handX + look.x * bladeFrac * 0.6D + (player.getRandom().nextDouble() - 0.5D) * 0.12D;
            double py = handY + (bladeFrac * 0.4D) + (player.getRandom().nextDouble() - 0.5D) * 0.12D;
            double pz = handZ + look.z * bladeFrac * 0.6D + (player.getRandom().nextDouble() - 0.5D) * 0.12D;

            mc.level.addParticle(style.particle(), px, py, pz,
                    (player.getRandom().nextDouble() - 0.5D) * 0.02D,
                    0.03D + player.getRandom().nextDouble() * 0.02D,
                    (player.getRandom().nextDouble() - 0.5D) * 0.02D);
        }
    }
}
