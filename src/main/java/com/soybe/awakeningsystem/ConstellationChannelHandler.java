package com.soybe.awakeningsystem;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConstellationChannelHandler {
    private ConstellationChannelHandler() {}

    public record AstralReturnData(
            ResourceKey<Level> dimension,
            Vec3 position,
            float yRot,
            float xRot,
            GameType previousGameMode
    ) {}

    private static final Map<UUID, AstralReturnData> ASTRAL_WATCHERS = new ConcurrentHashMap<>();

    public static boolean isAstralWatching(UUID playerId) {
        return ASTRAL_WATCHERS.containsKey(playerId);
    }

    public static void handleRequestStarStream(ServerPlayer constellationPlayer) {
        if (constellationPlayer.getServer() == null) return;
        SystemData cData = SystemEvents.data(constellationPlayer);
        if (cData == null || !cData.isConstellation()) return;

        List<SyncStarStreamPayload.IncarnationEntry> entries = new ArrayList<>();
        List<ServerPlayer> players = constellationPlayer.getServer().getPlayerList().getPlayers();

        for (ServerPlayer p : players) {
            SystemData d = SystemEvents.data(p);
            if (d == null) continue;

            String activity;
            if (DungeonManager.isInDungeon(p.getUUID())) {
                activity = "En Calabozo";
            } else if (p.getHealth() <= p.getMaxHealth() * 0.3F) {
                activity = "¡Herido Crítico!";
            } else if (d.isInTrance()) {
                activity = "¡Modo Trance!";
            } else if (p.level().isNight()) {
                activity = "Cacería Nocturna";
            } else {
                activity = "Explorando";
            }

            entries.add(new SyncStarStreamPayload.IncarnationEntry(
                    p.getUUID(),
                    p.getScoreboardName(),
                    d.level(),
                    d.rank(),
                    (int) p.getHealth(),
                    (int) p.getMaxHealth(),
                    p.level().dimension().location().getPath(),
                    activity
            ));
        }

        // Si está en solitario o no hay más jugadores, incluir encarnaciones narrativas para pruebas
        if (entries.size() <= 1) {
            entries.add(new SyncStarStreamPayload.IncarnationEntry(
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    "Lee Jihye",
                    42,
                    "B",
                    18,
                    20,
                    "overworld",
                    "En Combate Naval"
            ));
            entries.add(new SyncStarStreamPayload.IncarnationEntry(
                    UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    "Yoo Sangah",
                    50,
                    "A",
                    20,
                    20,
                    "overworld",
                    "Canalizando Hebras"
            ));
            entries.add(new SyncStarStreamPayload.IncarnationEntry(
                    UUID.fromString("00000000-0000-0000-0000-000000000003"),
                    "Soldado de Sombra",
                    65,
                    "S",
                    45,
                    50,
                    "the_nether",
                    "Protegiendo Territorio"
            ));
        }

        PacketDistributor.sendToPlayer(constellationPlayer, new SyncStarStreamPayload(entries));
    }

    public static void handleDonate(ServerPlayer donor, DonateConstellationPayload payload) {
        if (donor.getServer() == null) return;
        SystemData donorData = SystemEvents.data(donor);
        if (donorData == null || !donorData.isConstellation()) return;

        int amount = payload.amount();
        if (amount <= 0 || donorData.coins() < amount) {
            AwakeningSystemMod.notify(donor, "awakening_system.notification.shop.error_title",
                    "awakening_system.notification.shop.insufficient", 0xFFFF5555);
            return;
        }

        donorData.spendCoins(amount);

        String rawMod = donorData.constellationModifier();
        String modifier = (rawMod != null && !rawMod.isEmpty())
                ? ConstellationAscension.getTranslatedTitleComponent(rawMod).getString()
                : donor.getScoreboardName();

        String commentText = getCommentText(payload.commentKey(), modifier, amount);

        // Buscar si es un jugador real online
        ServerPlayer target = donor.getServer().getPlayerList().getPlayer(payload.targetPlayerId());
        String targetName;
        if (target != null) {
            targetName = target.getScoreboardName();
            SystemData targetData = SystemEvents.data(target);
            if (targetData != null) {
                targetData.addCoins(amount);
                AwakeningSystemMod.sync(target);
            }

            // Notificación emergente holográfica al receptor
            AwakeningSystemMod.notify(target,
                    "awakening_system.notification.constellation.sponsor_received_title",
                    "awakening_system.notification.constellation.sponsor_received_msg",
                    0xFFFFD866, modifier, amount, commentText);
            target.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.4F);
        } else {
            targetName = switch (payload.targetPlayerId().toString()) {
                case "00000000-0000-0000-0000-000000000001" -> "Lee Jihye";
                case "00000000-0000-0000-0000-000000000002" -> "Yoo Sangah";
                default -> "Soldado de Sombra";
            };
        }

        // Notificación emergente holográfica en pantalla al donante
        AwakeningSystemMod.notify(donor,
                "awakening_system.notification.constellation.sponsor_sent_title",
                "awakening_system.notification.constellation.sponsor_sent_msg",
                0xFFFFD866, amount, targetName, commentText);

        // Notificación a los demás jugadores conectados en el servidor
        for (ServerPlayer p : donor.getServer().getPlayerList().getPlayers()) {
            if (!p.getUUID().equals(donor.getUUID()) && (target == null || !p.getUUID().equals(target.getUUID()))) {
                AwakeningSystemMod.notify(p,
                        "awakening_system.notification.constellation.sponsor_broadcast_title",
                        "awakening_system.notification.constellation.sponsor_broadcast_msg",
                        0xFFFFD866, modifier, amount, targetName, commentText);
            }
        }

        donor.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
        AwakeningSystemMod.sync(donor);

        // Actualizar la lista para el donante
        handleRequestStarStream(donor);
    }

    public static void handleSpectate(ServerPlayer constellationPlayer, UUID targetId) {
        if (constellationPlayer.getServer() == null) return;
        SystemData myData = SystemEvents.data(constellationPlayer);
        if (myData == null || !myData.isConstellation()) {
            AwakeningSystemMod.notify(constellationPlayer,
                    "awakening_system.notification.constellation.spectate_error_title",
                    "awakening_system.notification.constellation.spectate_not_constellation",
                    0xFFFF5555);
            return;
        }

        ServerPlayer target = constellationPlayer.getServer().getPlayerList().getPlayer(targetId);
        if (target == null) {
            AwakeningSystemMod.notify(constellationPlayer,
                    "awakening_system.notification.constellation.spectate_title",
                    "awakening_system.notification.constellation.spectate_not_present",
                    0xFF8AAAB8);
            return;
        }

        if (target.getUUID().equals(constellationPlayer.getUUID())) {
            AwakeningSystemMod.notify(constellationPlayer,
                    "awakening_system.notification.constellation.spectate_title",
                    "awakening_system.notification.constellation.spectate_self",
                    0xFFFFD866);
            return;
        }

        if (!ASTRAL_WATCHERS.containsKey(constellationPlayer.getUUID())) {
            ASTRAL_WATCHERS.put(constellationPlayer.getUUID(), new AstralReturnData(
                    constellationPlayer.level().dimension(),
                    constellationPlayer.position(),
                    constellationPlayer.getYRot(),
                    constellationPlayer.getXRot(),
                    constellationPlayer.gameMode.getGameModeForPlayer()
            ));
        }

        ServerLevel targetLevel = (ServerLevel) target.level();
        constellationPlayer.teleportTo(targetLevel, target.getX(), target.getY() + 1.5D, target.getZ(),
                target.getYRot(), target.getXRot());
        constellationPlayer.setGameMode(GameType.SPECTATOR);

        AwakeningSystemMod.notify(constellationPlayer,
                "awakening_system.notification.constellation.astral_start_title",
                "awakening_system.notification.constellation.astral_start_msg",
                0xFFFFD700, target.getScoreboardName());
    }

    public static boolean returnFromAstral(ServerPlayer player) {
        AstralReturnData returnData = ASTRAL_WATCHERS.remove(player.getUUID());
        if (returnData == null) return false;

        if (player.getServer() != null) {
            ServerLevel originLevel = player.getServer().getLevel(returnData.dimension());
            if (originLevel != null) {
                player.teleportTo(originLevel, returnData.position().x, returnData.position().y, returnData.position().z,
                        returnData.yRot(), returnData.xRot());
            }
        }
        player.setGameMode(returnData.previousGameMode());

        AwakeningSystemMod.notify(player,
                "awakening_system.notification.constellation.astral_end_title",
                "awakening_system.notification.constellation.astral_end_msg",
                0xFF70E8FF);
        return true;
    }

    public static Component getCommentComponent(String key, String modifier, int coins) {
        String cleanKey = (key != null ? key.toLowerCase() : "satisfied");
        return Component.translatable("awakening_system.constellation.comment_text." + cleanKey, modifier);
    }

    public static String getCommentText(String key, String modifier, int coins) {
        return getCommentComponent(key, modifier, coins).getString();
    }
}
