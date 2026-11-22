package dev.diorama.mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The pufferfish's three models, one per puff size, which its renderer switches between as it draws. */
@Mixin(PufferfishRenderer.class)
public interface PufferfishRendererAccessor {
	@Accessor("small")
	EntityModel<?> diorama$small();

	@Accessor("mid")
	EntityModel<?> diorama$mid();

	@Accessor("big")
	EntityModel<?> diorama$big();
}
