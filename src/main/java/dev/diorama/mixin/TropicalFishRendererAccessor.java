package dev.diorama.mixin;

import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The tropical fish's two models, one per body shape, which its renderer switches between as it draws. */
@Mixin(TropicalFishRenderer.class)
public interface TropicalFishRendererAccessor {
	//? if >=1.21.11 {
	/*@Accessor("smallModel")
	net.minecraft.client.model.EntityModel<?> diorama$small();

	@Accessor("largeModel")
	net.minecraft.client.model.EntityModel<?> diorama$large();
	*///?} else if >=1.21.2 {
	/*@Accessor("modelA")
	net.minecraft.client.model.EntityModel<?> diorama$small();

	@Accessor("modelB")
	net.minecraft.client.model.EntityModel<?> diorama$large();
	*///?} else {
	@Accessor("modelA")
	net.minecraft.client.model.ColorableHierarchicalModel<?> diorama$small();

	@Accessor("modelB")
	net.minecraft.client.model.ColorableHierarchicalModel<?> diorama$large();
	//?}
}
