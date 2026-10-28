package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.CustomPlayerPlushieBlock;
import com.soybe.awakeningsystem.client.model.CustomPlushieModel;
import com.soybe.awakeningsystem.item.CustomPlayerPlushieItem;
import com.soybe.awakeningsystem.item.PlushieItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.fml.ModList;

import java.util.UUID;

public class HeadPlushieLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
    private static final ResourceLocation LANDER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/landeralo_skin.png");

    private final CustomPlushieModel chibiModel;

    public HeadPlushieLayer(RenderLayerParent<T, M> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.chibiModel = new CustomPlushieModel(modelSet.bakeLayer(CustomPlushieModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack headStack = findEquippedPlushie(livingEntity);
        if (headStack.isEmpty()) return;

        boolean isCustomPlushie = headStack.getItem() instanceof CustomPlayerPlushieItem;
        boolean isStandardPlushie = headStack.getItem() instanceof PlushieItem;

        if (!isCustomPlushie && !isStandardPlushie) return;

        poseStack.pushPose();

        this.getParentModel().getHead().translateAndRotate(poseStack);

        poseStack.translate(0.0D, -0.52D, 0.0D);

        float headScale = 0.55F;

        if (isCustomPlushie) {
            renderCustomPlushieHead(headStack, livingEntity, poseStack, bufferSource, packedLight, headScale);
        } else if (headStack.getItem() instanceof BlockItem bi) {
            String path = BuiltInRegistries.BLOCK.getKey(bi.getBlock()).getPath();
            var tribute = PlushieBlockEntityRenderer.TRIBUTES.get(path);
            ResourceLocation chibiTexture = tribute != null ? tribute.fallbackTexture() : PlushieBlockEntityRenderer.CHIBI_SKIN_PLUSHIES.get(path);

            if (chibiTexture != null) {
                renderChibiHeadWithTexture(chibiTexture, poseStack, bufferSource, packedLight, headScale);
            } else {
                renderBlockPlushieHead(bi, poseStack, bufferSource, packedLight, headScale);
            }
        }

        poseStack.popPose();
    }

    public static boolean isPlushie(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.getItem() instanceof CustomPlayerPlushieItem || stack.getItem() instanceof PlushieItem;
    }

    private ItemStack findEquippedPlushie(T livingEntity) {
        ItemStack headStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        if (isPlushie(headStack)) return headStack;

        if (ModList.get().isLoaded("curios")) {
            try {
                Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                var method = apiClass.getMethod("getCuriosInventory", LivingEntity.class);
                var invOpt = (java.util.Optional<?>) method.invoke(null, livingEntity);
                if (invOpt.isPresent()) {
                    var handler = invOpt.get();
                    var getEquipped = handler.getClass().getMethod("getEquippedCurios");
                    var itemHandler = (net.neoforged.neoforge.items.IItemHandlerModifiable) getEquipped.invoke(handler);
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        ItemStack curio = itemHandler.getStackInSlot(i);
                        if (isPlushie(curio)) return curio;
                    }
                }
            } catch (Throwable t) {
                AwakeningSystemMod.LOGGER.debug("Error checking Curios equipped plushie: {}", t.getMessage());
            }
        }

        return ItemStack.EMPTY;
    }

    private void renderCustomPlushieHead(ItemStack stack, LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float scale) {
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        ResourceLocation skinTexture = null;

        if (profile != null) {
            String profileName = profile.name().orElse("");
            UUID profileId = profile.id().orElse(null);

            var localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null && ((profileId != null && profileId.equals(localPlayer.getUUID()))
                    || (!profileName.isEmpty() && profileName.equalsIgnoreCase(localPlayer.getGameProfile().getName())))) {
                skinTexture = localPlayer.getSkin().texture();
            }

            if (skinTexture == null) {
                if (profileName.equalsIgnoreCase("LanderAlo")) {
                    skinTexture = LANDER_TEXTURE;
                } else if (profileName.equalsIgnoreCase("Schvis") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.SCHVIS_UUID))) {
                    skinTexture = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/schvisplushie.png");
                } else if (profileName.toLowerCase().startsWith("techidna") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.TECHIDNA_UUID))) {
                    skinTexture = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/techidnaplushie.png");
                } else if (profileName.toLowerCase().startsWith("tomcraft") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.TOMCRAFT_UUID))) {
                    skinTexture = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/tomcraftplushie.png");
                } else if (profileName.equalsIgnoreCase("Nandachan") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.NANDACHAN_UUID))) {
                    skinTexture = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/nandachanplushie.png");
                } else if (profileName.equalsIgnoreCase("MisticPotato") || profileName.equalsIgnoreCase("Potato") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.MISTIC_POTATO_UUID))) {
                    skinTexture = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/misticpotatoplushie.png");
                }
            }

            if (skinTexture == null) {
                try {
                    var insecureSkin = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile.gameProfile());
                    if (insecureSkin != null && insecureSkin.texture() != null) {
                        skinTexture = insecureSkin.texture();
                    }
                } catch (Exception e) {
                    AwakeningSystemMod.LOGGER.debug("Failed to retrieve insecure skin for profile: {}", e.getMessage(), e);
                }
            }
        }

        if (skinTexture == null) {
            var localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null) {
                skinTexture = localPlayer.getSkin().texture();
            } else if (profile != null && profile.id().isPresent()) {
                skinTexture = DefaultPlayerSkin.get(profile.id().get()).texture();
            } else {
                skinTexture = DefaultPlayerSkin.getDefaultTexture();
            }
        }

        renderChibiHeadWithTexture(skinTexture, poseStack, bufferSource, packedLight, scale);
    }

    private void renderChibiHeadWithTexture(ResourceLocation texture, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float scale) {
        poseStack.pushPose();
        poseStack.scale(-scale, -scale, scale);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.chibiModel.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
    }

    private void renderBlockPlushieHead(BlockItem bi, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float scale) {
        poseStack.pushPose();
        poseStack.translate(-0.5D * scale, -1.0D * scale, -0.5D * scale);
        poseStack.scale(scale, scale, scale);

        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var state = bi.getBlock().defaultBlockState();
        var model = blockRenderer.getBlockModel(state);
        var vertexConsumer = bufferSource.getBuffer(RenderType.cutout());

        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                vertexConsumer,
                state,
                model,
                1.0F, 1.0F, 1.0F,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                net.neoforged.neoforge.client.model.data.ModelData.EMPTY,
                RenderType.cutout()
        );
        poseStack.popPose();
    }
}
