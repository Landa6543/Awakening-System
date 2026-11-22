package com.soybe.awakeningsystem.abyss;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Leaving the Spiral Abyss whenever one likes, as in the game: a button in the pause menu while inside. The run
 * in progress is dropped (the chamber starts again next time); the floors already cleared stay cleared.
 */
public final class AbyssLeave {
    private AbyssLeave() {}

    public record LeavePayload() implements CustomPacketPayload {
        public static final Type<LeavePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyss_leave"));
        public static final StreamCodec<RegistryFriendlyByteBuf, LeavePayload> STREAM_CODEC = StreamCodec.unit(new LeavePayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").optional().playToServer(LeavePayload.TYPE, LeavePayload.STREAM_CODEC, (p, c) -> c.enqueueWork(() -> {
                if (c.player() instanceof ServerPlayer sp && sp.level().dimension().equals(SpiralAbyssManager.SPIRAL_ABYSS_DIMENSION))
                    SpiralAbyssManager.handleLeaveAbyss(sp);
            }));
        }
    }
}
