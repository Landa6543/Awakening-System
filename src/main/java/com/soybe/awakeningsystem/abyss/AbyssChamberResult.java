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
 * The window that comes up when a chamber is cleared, as in the game: its stars, the time left, and what next
 * (on to the next chamber or floor, leave the Abyss, or stay a while). The walk into the descent ring still works.
 */
public final class AbyssChamberResult {
    private AbyssChamberResult() {}

    public static final int NEXT = 0, LEAVE = 1, STAY = 2;

    /** Server to player: chamber stars (0-3), the floor's stars so far per chamber, seconds left, what comes next. */
    public record ResultPayload(int floor, int chamber, int stars, int[] floorStars, int secondsLeft, boolean lastChamber,
                                boolean canAdvanceFloor) implements CustomPacketPayload {
        public static final Type<ResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyss_chamber_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ResultPayload> STREAM_CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.floor);
                    buf.writeVarInt(p.chamber);
                    buf.writeVarInt(p.stars);
                    for (int i = 0; i < 3; i++) buf.writeVarInt(i < p.floorStars.length ? p.floorStars[i] : 0);
                    buf.writeVarInt(p.secondsLeft);
                    buf.writeBoolean(p.lastChamber);
                    buf.writeBoolean(p.canAdvanceFloor);
                },
                buf -> new ResultPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                        new int[]{buf.readVarInt(), buf.readVarInt(), buf.readVarInt()}, buf.readVarInt(), buf.readBoolean(), buf.readBoolean()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Player to server: the button pressed (NEXT, LEAVE or STAY). */
    public record ChoicePayload(int choice) implements CustomPacketPayload {
        public static final Type<ChoicePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyss_chamber_choice"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ChoicePayload> STREAM_CODEC = StreamCodec.of(
                (buf, p) -> buf.writeVarInt(p.choice), buf -> new ChoicePayload(buf.readVarInt()));

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
            var r = event.registrar("1").optional();
            r.playToClient(ResultPayload.TYPE, ResultPayload.STREAM_CODEC, (p, c) -> c.enqueueWork(() ->
                    com.soybe.awakeningsystem.abyss.client.AbyssChamberResultScreen.open(p)));
            r.playToServer(ChoicePayload.TYPE, ChoicePayload.STREAM_CODEC, (p, c) -> c.enqueueWork(() -> {
                if (c.player() instanceof ServerPlayer sp && sp.level().dimension().equals(SpiralAbyssManager.SPIRAL_ABYSS_DIMENSION))
                    SpiralAbyssManager.handleChamberChoice(sp, p.choice());
            }));
        }
    }
}
