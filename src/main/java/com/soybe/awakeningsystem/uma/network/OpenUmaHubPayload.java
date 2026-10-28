package com.soybe.awakeningsystem.uma.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.block.entity.CustomRacetrackBlockEntity;
import com.soybe.awakeningsystem.uma.model.OwnedUma;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public record OpenUmaHubPayload(
        int carats,
        int playerCoins,
        String activeUmaId,
        List<OwnedUmaDto> ownedUmas,
        BlockPos trackPos,
        String trackName,
        int waypointCount,
        int raceState,
        int stateTimer,
        List<ParticipantDto> participants
) implements CustomPacketPayload {

    public static final Type<OpenUmaHubPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "open_uma_hub"));

    public record OwnedUmaDto(
            String umaId,
            int rarity,
            int shards,
            int speed,
            int stamina,
            int power,
            int guts,
            int wisdom,
            int trainingLevel,
            int totalRaces,
            int totalWins
    ) {
        public static void encode(RegistryFriendlyByteBuf buf, OwnedUmaDto d) {
            buf.writeUtf(d.umaId);
            buf.writeVarInt(d.rarity);
            buf.writeVarInt(d.shards);
            buf.writeVarInt(d.speed);
            buf.writeVarInt(d.stamina);
            buf.writeVarInt(d.power);
            buf.writeVarInt(d.guts);
            buf.writeVarInt(d.wisdom);
            buf.writeVarInt(d.trainingLevel);
            buf.writeVarInt(d.totalRaces);
            buf.writeVarInt(d.totalWins);
        }

        public static OwnedUmaDto decode(RegistryFriendlyByteBuf buf) {
            return new OwnedUmaDto(
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt()
            );
        }
    }

    public record ParticipantDto(
            String displayName,
            String umaId,
            int rarity,
            int laneIndex,
            boolean isNpc,
            int speed
    ) {
        public static void encode(RegistryFriendlyByteBuf buf, ParticipantDto p) {
            buf.writeUtf(p.displayName);
            buf.writeUtf(p.umaId);
            buf.writeVarInt(p.rarity);
            buf.writeVarInt(p.laneIndex);
            buf.writeBoolean(p.isNpc);
            buf.writeVarInt(p.speed);
        }

        public static ParticipantDto decode(RegistryFriendlyByteBuf buf) {
            return new ParticipantDto(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readVarInt()
            );
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenUmaHubPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeVarInt(payload.carats());
                buf.writeVarInt(payload.playerCoins());
                buf.writeUtf(payload.activeUmaId());

                buf.writeVarInt(payload.ownedUmas().size());
                for (OwnedUmaDto d : payload.ownedUmas()) {
                    OwnedUmaDto.encode(buf, d);
                }

                buf.writeBlockPos(payload.trackPos());
                buf.writeUtf(payload.trackName());
                buf.writeVarInt(payload.waypointCount());
                buf.writeVarInt(payload.raceState());
                buf.writeVarInt(payload.stateTimer());

                buf.writeVarInt(payload.participants().size());
                for (ParticipantDto p : payload.participants()) {
                    ParticipantDto.encode(buf, p);
                }
            },
            buf -> {
                int carats = buf.readVarInt();
                int coins = buf.readVarInt();
                String activeUma = buf.readUtf();

                int umasSize = buf.readVarInt();
                List<OwnedUmaDto> umas = new ArrayList<>(umasSize);
                for (int i = 0; i < umasSize; i++) {
                    umas.add(OwnedUmaDto.decode(buf));
                }

                BlockPos trackPos = buf.readBlockPos();
                String trackName = buf.readUtf();
                int wpCount = buf.readVarInt();
                int raceState = buf.readVarInt();
                int timer = buf.readVarInt();

                int partsSize = buf.readVarInt();
                List<ParticipantDto> parts = new ArrayList<>(partsSize);
                for (int i = 0; i < partsSize; i++) {
                    parts.add(ParticipantDto.decode(buf));
                }

                return new OpenUmaHubPayload(carats, coins, activeUma, umas, trackPos, trackName, wpCount, raceState, timer, parts);
            }
    );

    public static OpenUmaHubPayload createForPlayer(ServerPlayer player, BlockPos trackPos) {
        SystemData data = SystemEvents.data(player);
        List<OwnedUmaDto> dtoList = new ArrayList<>();
        for (OwnedUma ou : data.getOwnedUmas().values()) {
            dtoList.add(new OwnedUmaDto(
                    ou.getUmaId(),
                    ou.getRarity(),
                    ou.getShards(),
                    ou.getSpeed(),
                    ou.getStamina(),
                    ou.getPower(),
                    ou.getGuts(),
                    ou.getWisdom(),
                    ou.getTrainingLevel(),
                    ou.getTotalRaces(),
                    ou.getTotalWins()
            ));
        }

        String tName = "Pista Libre";
        int wpCount = 0;
        int rState = 0;
        int sTimer = 0;
        List<ParticipantDto> partList = new ArrayList<>();

        if (trackPos == null || trackPos.equals(BlockPos.ZERO)) {
            BlockPos pPos = player.blockPosition();
            for (BlockPos check : BlockPos.betweenClosed(pPos.offset(-24, -8, -24), pPos.offset(24, 8, 24))) {
                if (player.level().getBlockEntity(check) instanceof CustomRacetrackBlockEntity) {
                    trackPos = check.immutable();
                    break;
                }
            }
        }

        if (trackPos != null && !trackPos.equals(BlockPos.ZERO) && player.level().getBlockEntity(trackPos) instanceof CustomRacetrackBlockEntity be) {
            tName = be.getTrackName();
            wpCount = be.getWaypoints().size();
            rState = be.getState().ordinal();
            sTimer = be.getStateTimer();
            for (CustomRacetrackBlockEntity.Participant p : be.getParticipants()) {
                partList.add(new ParticipantDto(p.displayName, p.umaId, p.rarity, p.laneIndex, p.isNpc, p.speed));
            }
        } else {
            trackPos = BlockPos.ZERO;
        }

        return new OpenUmaHubPayload(
                data.getCarats(),
                data.coins(),
                data.getActiveUmaId(),
                dtoList,
                trackPos,
                tName,
                wpCount,
                rState,
                sTimer,
                partList
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
