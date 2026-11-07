package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.NpcProfile;
import com.soybe.awakeningsystem.entity.SeriesNpcEntity;
import com.soybe.awakeningsystem.guild.Guild;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        Set<UUID> addedUuids = new HashSet<>();

        // 1. Jugadores reales en línea
        List<ServerPlayer> players = constellationPlayer.getServer().getPlayerList().getPlayers();
        for (ServerPlayer p : players) {
            SystemData d = SystemEvents.data(p);
            if (d == null) continue;

            String activity;
            if (DungeonManager.isInDungeon(p.getUUID())) {
                activity = "In Dungeon";
            } else if (p.getHealth() <= p.getMaxHealth() * 0.3F) {
                activity = "Critically Wounded!";
            } else if (d.isInTrance()) {
                activity = "Trance Mode!";
            } else if (p.level().isNight()) {
                activity = "Night Hunting";
            } else {
                activity = "Exploring";
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
            addedUuids.add(p.getUUID());
        }

        // 2. NPCs activos en el mundo (SeriesNpcEntity)
        for (ServerLevel level : constellationPlayer.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof SeriesNpcEntity npc && npc.isAlive() && !addedUuids.contains(npc.getUUID())) {
                    NpcProfile prof = npc.getProfile();
                    String act;
                    if (npc.getTarget() != null) {
                        act = "In Combat";
                    } else {
                        act = switch (prof) {
                            case SUNG_JINWOO -> "Extracting Shadows";
                            case CHA_HAEIN -> "Sword Training";
                            case KIM_DOKJA -> "Channeling Threads";
                            case YOO_JOONGHYUK -> "Breaking Fate";
                            case RYU_HANBIN -> "Aura Cleave";
                            case HUNTER_EVALUATOR -> "Evaluating Hunters";
                            default -> "Guarding Territory";
                        };
                    }

                    int npcLevel = switch (prof) {
                        case SUNG_JINWOO -> 180;
                        case CHA_HAEIN -> 110;
                        case KIM_DOKJA -> 150;
                        case YOO_JOONGHYUK -> 190;
                        case RYU_HANBIN -> 175;
                        case HUNTER_EVALUATOR -> 95;
                        default -> 60;
                    };

                    String npcRank = switch (prof) {
                        case SUNG_JINWOO -> "MONARCH";
                        case CHA_HAEIN -> "S";
                        case KIM_DOKJA -> "TRANSCENDENT";
                        case YOO_JOONGHYUK -> "MONARCH";
                        case RYU_HANBIN -> "MONARCH";
                        case HUNTER_EVALUATOR -> "A";
                        default -> "B";
                    };

                    entries.add(new SyncStarStreamPayload.IncarnationEntry(
                            npc.getUUID(),
                            prof.displayName() + " [NPC]",
                            npcLevel,
                            npcRank,
                            (int) npc.getHealth(),
                            (int) npc.getMaxHealth(),
                            level.dimension().location().getPath(),
                            act
                    ));
                    addedUuids.add(npc.getUUID());
                }
            }
        }

        // 3. Transmisiones canónicas adicionales si hay pocos participantes
        if (entries.size() <= 4) {
            UUID jinwooUuid = Guild.getNpcUuid("sung_jinwoo");
            if (!addedUuids.contains(jinwooUuid)) {
                entries.add(new SyncStarStreamPayload.IncarnationEntry(
                        jinwooUuid,
                        "Sung Jin-Woo [NPC]",
                        180,
                        "MONARCH",
                        200,
                        200,
                        "overworld",
                        "Extracting Shadows"
                ));
            }

            UUID haeinUuid = Guild.getNpcUuid("cha_haein");
            if (!addedUuids.contains(haeinUuid)) {
                entries.add(new SyncStarStreamPayload.IncarnationEntry(
                        haeinUuid,
                        "Cha Hae-In [NPC]",
                        110,
                        "S",
                        120,
                        120,
                        "overworld",
                        "Sword Training"
                ));
            }

            UUID dokjaUuid = Guild.getNpcUuid("kim_dokja");
            if (!addedUuids.contains(dokjaUuid)) {
                entries.add(new SyncStarStreamPayload.IncarnationEntry(
                        dokjaUuid,
                        "Kim Dokja [NPC]",
                        150,
                        "TRANSCENDENT",
                        140,
                        140,
                        "overworld",
                        "Channeling Threads"
                ));
            }

            UUID evaluatorUuid = Guild.getNpcUuid("hunter_evaluator");
            if (!addedUuids.contains(evaluatorUuid)) {
                entries.add(new SyncStarStreamPayload.IncarnationEntry(
                        evaluatorUuid,
                        "Inspector de la Asociación [NPC]",
                        95,
                        "A",
                        500,
                        500,
                        "overworld",
                        "Evaluating Hunters"
                ));
            }
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
        String titleKey = ConstellationAscension.getTitleTranslationKey(rawMod);
        String modifier = (titleKey != null) ? titleKey : ((rawMod != null && !rawMod.isEmpty()) ? rawMod : donor.getScoreboardName());

        String cleanKey = (payload.commentKey() != null ? payload.commentKey().toLowerCase() : "satisfied");
        String commentArg = "awakening_system.constellation.comment_text." + cleanKey + "@@@" + modifier;

        ServerPlayer target = donor.getServer().getPlayerList().getPlayer(payload.targetPlayerId());
        String targetName;
        if (target != null) {
            targetName = target.getScoreboardName();
            SystemData targetData = SystemEvents.data(target);
            if (targetData != null) {
                targetData.addCoins(amount);
                AwakeningSystemMod.sync(target);
            }

            AwakeningSystemMod.notify(target,
                    "awakening_system.notification.constellation.sponsor_received_title",
                    "awakening_system.notification.constellation.sponsor_received_msg",
                    0xFFFFD866, modifier, amount, commentArg);
            target.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.4F);
        } else {
            // Verificar si es un NPC existente en el servidor
            Entity targetNpc = null;
            for (ServerLevel sl : donor.getServer().getAllLevels()) {
                Entity ent = sl.getEntity(payload.targetPlayerId());
                if (ent != null) {
                    targetNpc = ent;
                    break;
                }
            }

            if (targetNpc instanceof SeriesNpcEntity sne) {
                targetName = sne.getProfile().displayName();
                if (sne.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HEART, sne.getX(), sne.getY() + 1.2D, sne.getZ(), 12, 0.4, 0.4, 0.4, 0.05);
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sne.getX(), sne.getY() + 1.0D, sne.getZ(), 15, 0.5, 0.5, 0.5, 0.05);
                }
            } else {
                targetName = switch (payload.targetPlayerId().toString()) {
                    case "00000000-0000-0000-0000-000000000001" -> "Lee Jihye";
                    case "00000000-0000-0000-0000-000000000002" -> "Yoo Sangah";
                    default -> "Encarnación";
                };
            }
        }

        AwakeningSystemMod.notify(donor,
                "awakening_system.notification.constellation.sponsor_sent_title",
                "awakening_system.notification.constellation.sponsor_sent_msg",
                0xFFFFD866, amount, targetName, commentArg);

        for (ServerPlayer p : donor.getServer().getPlayerList().getPlayers()) {
            if (!p.getUUID().equals(donor.getUUID()) && (target == null || !p.getUUID().equals(target.getUUID()))) {
                AwakeningSystemMod.notify(p,
                        "awakening_system.notification.constellation.sponsor_broadcast_title",
                        "awakening_system.notification.constellation.sponsor_broadcast_msg",
                        0xFFFFD866, modifier, amount, targetName, commentArg);
            }
        }

        donor.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
        AwakeningSystemMod.sync(donor);

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

        if (targetId.equals(constellationPlayer.getUUID())) {
            AwakeningSystemMod.notify(constellationPlayer,
                    "awakening_system.notification.constellation.spectate_title",
                    "awakening_system.notification.constellation.spectate_self",
                    0xFFFFD866);
            return;
        }

        // 1. Buscar si es un jugador en línea
        ServerPlayer targetPlayer = constellationPlayer.getServer().getPlayerList().getPlayer(targetId);
        Entity targetEntity = targetPlayer;
        ServerLevel targetLevel = targetPlayer != null ? (ServerLevel) targetPlayer.level() : null;

        // 2. Si no es un jugador, buscar si es un NPC vivo en cualquier dimensión
        if (targetEntity == null) {
            for (ServerLevel sl : constellationPlayer.getServer().getAllLevels()) {
                Entity ent = sl.getEntity(targetId);
                if (ent != null && ent.isAlive()) {
                    targetEntity = ent;
                    targetLevel = sl;
                    break;
                }
            }
        }

        // 3. Si aún no existe en el mundo pero es un NPC canónico, proyectar/manifestar en el mundo exterior
        if (targetEntity == null) {
            NpcProfile matchedProfile = null;
            for (NpcProfile prof : NpcProfile.values()) {
                if (Guild.getNpcUuid(prof.id()).equals(targetId)) {
                    matchedProfile = prof;
                    break;
                }
            }
            if (matchedProfile == null) {
                if (targetId.toString().equals("00000000-0000-0000-0000-000000000001")) matchedProfile = NpcProfile.CHA_HAEIN;
                else if (targetId.toString().equals("00000000-0000-0000-0000-000000000002")) matchedProfile = NpcProfile.KIM_DOKJA;
            }

            if (matchedProfile != null) {
                targetLevel = constellationPlayer.serverLevel();
                SeriesNpcEntity npc = new SeriesNpcEntity(ModEntities.SERIES_NPC.get(), targetLevel);
                npc.setUUID(targetId);
                npc.setProfile(matchedProfile);
                npc.setPos(constellationPlayer.getX() + 6.0D, constellationPlayer.getY(), constellationPlayer.getZ() + 6.0D);
                targetLevel.addFreshEntity(npc);
                targetEntity = npc;
            }
        }

        if (targetEntity == null || targetLevel == null) {
            AwakeningSystemMod.notify(constellationPlayer,
                    "awakening_system.notification.constellation.spectate_title",
                    "awakening_system.notification.constellation.spectate_not_present",
                    0xFF8AAAB8);
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

        constellationPlayer.teleportTo(targetLevel, targetEntity.getX(), targetEntity.getY() + 1.2D, targetEntity.getZ(),
                targetEntity.getYRot(), targetEntity.getXRot());
        constellationPlayer.setGameMode(GameType.SPECTATOR);
        constellationPlayer.setCamera(targetEntity);

        String entityName = targetEntity.getName().getString();
        AwakeningSystemMod.notify(constellationPlayer,
                "awakening_system.notification.constellation.astral_start_title",
                "awakening_system.notification.constellation.astral_start_msg",
                0xFFFFD700, entityName);
    }

    public static boolean returnFromAstral(ServerPlayer player) {
        AstralReturnData returnData = ASTRAL_WATCHERS.remove(player.getUUID());
        if (returnData == null) return false;

        player.setCamera(player);

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
        Object modArg = (modifier != null && modifier.startsWith("awakening_system."))
                ? Component.translatable(modifier)
                : (modifier != null ? modifier : "");
        return Component.translatable("awakening_system.constellation.comment_text." + cleanKey, modArg);
    }

    public static String getCommentText(String key, String modifier, int coins) {
        return getCommentComponent(key, modifier, coins).getString();
    }
}
