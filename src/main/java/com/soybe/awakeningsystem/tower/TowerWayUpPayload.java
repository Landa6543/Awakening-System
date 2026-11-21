package com.soybe.awakeningsystem.tower;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Where the way up of the floor the player is on stands (shown as a column of light); {@code has} false clears it. */
public record TowerWayUpPayload(boolean has, int x, int y, int z) implements CustomPacketPayload {
    public static final Type<TowerWayUpPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "tower_way_up"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TowerWayUpPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> { buf.writeBoolean(p.has); buf.writeVarInt(p.x); buf.writeVarInt(p.y); buf.writeVarInt(p.z); },
            buf -> new TowerWayUpPayload(buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").optional().playToClient(TYPE, STREAM_CODEC, (payload, context) -> context.enqueueWork(
                    () -> com.soybe.awakeningsystem.client.TowerWayUpBeam.set(payload)));
        }
    }
}
