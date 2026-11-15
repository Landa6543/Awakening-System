package com.soybe.awakeningsystem.dialogue;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.dialogue.network.CloseDialoguePayload;
import com.soybe.awakeningsystem.dialogue.network.OpenDialoguePayload;
import com.soybe.awakeningsystem.entity.NpcProfile;
import com.soybe.awakeningsystem.entity.SeriesNpcEntity;
import com.soybe.awakeningsystem.guild.Guild;
import com.soybe.awakeningsystem.guild.GuildMember;
import com.soybe.awakeningsystem.guild.GuildRole;
import com.soybe.awakeningsystem.guild.GuildSavedData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor del servidor para el Sistema de Diálogos estilo Manhwa / Solo Leveling.
 * Maneja sesiones activas, evaluación de rangos (hasta Nv. 2000), reclutamiento en gremios,
 * transacciones y ramas de conversación.
 */
public final class NpcDialogueManager {

    public record ActiveSession(int entityId, String dialogueId, String currentNodeId, long timestamp) {}

    private static final Map<UUID, ActiveSession> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<Integer, UUID> NPC_SPEAKERS = new ConcurrentHashMap<>();

    private NpcDialogueManager() {}

    public static boolean isNpcSpeaking(int entityId) {
        return NPC_SPEAKERS.containsKey(entityId);
    }

    public static UUID getSpeakerForNpc(int entityId) {
        return NPC_SPEAKERS.get(entityId);
    }

    public static void startDialogue(ServerPlayer player, Entity npc, String dialogueId) {
        if (player == null || npc == null) return;

        NPC_SPEAKERS.put(npc.getId(), player.getUUID());
        if (npc instanceof net.minecraft.world.entity.Mob mob) {
            mob.getNavigation().stop();
            mob.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
        }

        if (dialogueId.equalsIgnoreCase("hunter_evaluator")) {
            startHunterEvaluatorDialogue(player, npc, "root");
        } else if (dialogueId.equalsIgnoreCase("series_npc") && npc instanceof SeriesNpcEntity sne) {
            startSeriesNpcDialogue(player, sne);
        } else if (dialogueId.equalsIgnoreCase("town_npc")) {
            townNode(player, npc, "root");
        } else {
            // Diálogo genérico de saludo si no está registrado
            List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "Entendido, muchas gracias.", "close", "CLOSE")
            );
            sendNode(player, npc.getId(), dialogueId, "greeting", "DEFAULT",
                    "§b§l[SISTEMA] §fNPC",
                    "Saludos, Cazador. Que tus cacerías en las mazmorras sean prósperas.",
                    choices);
        }
    }

    public static void handleChoice(ServerPlayer player, int entityId, String dialogueId, String nodeId, int choiceId) {
        if (player == null) return;
        Entity npc = player.level().getEntity(entityId);
        if (npc == null || player.distanceToSqr(npc) > 64.0D) {
            closeDialogue(player);
            return;
        }

        if (dialogueId.equalsIgnoreCase("hunter_evaluator")) {
            handleHunterEvaluatorChoice(player, npc, nodeId, choiceId);
        } else if (dialogueId.equalsIgnoreCase("series_npc")) {
            handleSeriesNpcChoice(player, npc, nodeId, choiceId);
        } else if (dialogueId.equalsIgnoreCase("town_npc")) {
            if (nodeId.equals("root")) townNode(player, npc, choiceId == 0 ? "work" : choiceId == 1 ? "tip" : "town");
            else if (choiceId == 0) townNode(player, npc, "root");
            else closeDialogue(player);
        }
    }

    public static void closeDialogue(ServerPlayer player) {
        if (player == null) return;
        ActiveSession session = SESSIONS.remove(player.getUUID());
        if (session != null) {
            NPC_SPEAKERS.remove(session.entityId());
            PacketDistributor.sendToPlayer(player, new CloseDialoguePayload(session.entityId()));
        }
    }

    public static void clearForPlayer(UUID uuid) {
        ActiveSession session = SESSIONS.remove(uuid);
        if (session != null) {
            NPC_SPEAKERS.remove(session.entityId());
        }
    }

    // ==========================================
    // ESCALADO DE RANGOS HASTA NIVEL 2000
    // ==========================================

    public static int getRequiredAscensionKills(String nextRank) {
        if (nextRank == null) return 10;
        return switch (nextRank.toUpperCase(java.util.Locale.ROOT)) {
            case "E" -> 10;
            case "D" -> 15;
            case "C" -> 20;
            case "B" -> 25;
            case "A" -> 30;
            case "S" -> 40;
            case "EX" -> 50;
            case "SS" -> 60;
            case "SSS" -> 75;
            case "NATIONAL" -> 100;
            case "UR" -> 120;
            case "UR+" -> 140;
            case "LR" -> 160;
            case "SLR" -> 180;
            case "MR" -> 200;
            case "GR" -> 220;
            case "TRANSCENDENT" -> 250;
            case "TRANSCENDENTAL" -> 280;
            case "MONARCH" -> 320;
            case "RULER" -> 360;
            case "SUPREME_BEING" -> 400;
            case "DEMIGOD" -> 450;
            case "GOD" -> 500;
            case "DEITY" -> 550;
            case "ANCESTRAL" -> 600;
            case "COSMIC" -> 650;
            case "MULTIVERSAL" -> 700;
            case "OMNIVERSAL" -> 750;
            case "PRIMORDIAL" -> 800;
            case "INFINITE" -> 850;
            case "OMNIPOTENT" -> 900;
            case "ABSOLUTE_BEING" -> 950;
            case "ABSOLUTE" -> 1000;
            case "SUPREME_CREATOR" -> 1100;
            case "ABSOLUTE_CREATOR" -> 1200;
            case "CHAOS_SOVEREIGN" -> 1300;
            case "TIMELESS_MONARCH" -> 1400;
            case "ASTRAL_DEITY" -> 1500;
            case "SINGULARITY" -> 1600;
            case "LIMITLESS" -> 1800;
            case "ABSOLUTE_GOD" -> 2000;
            default -> 25;
        };
    }

    // ==========================================
    // DIÁLOGO DEL EVALUADOR DE LA ASOCIACIÓN
    // ==========================================

    private static void startHunterEvaluatorDialogue(ServerPlayer player, Entity npc, String targetNode) {
        SystemData data = SystemEvents.data(player);
        if (data == null) return;

        String speaker = "awakening_system.dialogue.hunter_evaluator.speaker";
        String currentRank = data.evaluatedRank();
        int capLevel = data.maxAllowedLevelForEvaluatedRank();
        boolean pending = data.isEvaluationPending() || (data.level() >= capLevel && !currentRank.equalsIgnoreCase("ABSOLUTE_GOD"));

        if ("root".equalsIgnoreCase(targetNode)) {
            if (pending) {
                String nextRank = data.getNextRank(currentRank);
                int reqKills = getRequiredAscensionKills(nextRank);

                if (data.isAscensionQuestActive()) {
                    if (data.isAscensionCompleted()) {
                        String text = "awakening_system.dialogue.hunter_evaluator.ascension_ready|"
                                + data.ascensionProgress() + "|" + data.ascensionTarget() + "|" + nextRank;

                        List<DialogueChoice> choices = new ArrayList<>();
                        choices.add(new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.calibrate", "do_mana_calibration", "CALIBRATE"));
                        choices.add(new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.not_ready", "goodbye", "CLOSE"));
                        sendNode(player, npc.getId(), "hunter_evaluator", "ascension_ready", "DEFAULT", speaker, text, choices);
                    } else {
                        String text = "awakening_system.dialogue.hunter_evaluator.ascension_in_progress|"
                                + data.ascensionTargetRank() + "|" + data.ascensionProgress() + "|" + data.ascensionTarget();

                        List<DialogueChoice> choices = new ArrayList<>();
                        choices.add(new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.continue_hunt", "goodbye", "CLOSE"));
                        choices.add(new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.buy_tracker", "buy_tracker", "BUY_TRACKER"));
                        choices.add(new DialogueChoice(2, "awakening_system.dialogue.hunter_evaluator.choice.cancel_quest", "cancel_ascension_confirm", ""));
                        sendNode(player, npc.getId(), "hunter_evaluator", "ascension_in_progress", "DEFAULT", speaker, text, choices);
                    }
                } else {
                    String text = "awakening_system.dialogue.hunter_evaluator.root_pending|"
                            + currentRank + "|" + data.level() + "|" + nextRank + "|" + reqKills + "|" + data.reserveExperience();

                    List<DialogueChoice> choices = List.of(
                            new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.start_quest", "start_quest", "START_QUEST"),
                            new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.explain_ascension", "explain_ascension", ""),
                            new DialogueChoice(2, "awakening_system.dialogue.hunter_evaluator.choice.buy_tracker", "buy_tracker", "BUY_TRACKER"),
                            new DialogueChoice(3, "awakening_system.dialogue.hunter_evaluator.choice.later", "goodbye", "CLOSE")
                    );
                    sendNode(player, npc.getId(), "hunter_evaluator", "root_pending", "DEFAULT", speaker, text, choices);
                }
            } else {
                String text = "awakening_system.dialogue.hunter_evaluator.root_normal|"
                        + currentRank + "|" + data.level() + "|" + capLevel;

                List<DialogueChoice> choices = List.of(
                        new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.rank_table", "rank_table", ""),
                        new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.buy_tracker", "buy_tracker", "BUY_TRACKER"),
                        new DialogueChoice(2, "awakening_system.dialogue.hunter_evaluator.choice.goodbye", "goodbye", "CLOSE")
                );

                sendNode(player, npc.getId(), "hunter_evaluator", "root_normal", "DEFAULT", speaker, text, choices);
            }
        } else if ("rank_table".equalsIgnoreCase(targetNode)) {
            String text = "awakening_system.dialogue.hunter_evaluator.rank_table";

            List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.tier1", "rank_table_t1", ""),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.tier2", "rank_table_t2", ""),
                    new DialogueChoice(2, "awakening_system.dialogue.hunter_evaluator.choice.tier3", "rank_table_t3", ""),
                    new DialogueChoice(3, "awakening_system.dialogue.hunter_evaluator.choice.tier4", "rank_table_t4", ""),
                    new DialogueChoice(4, "awakening_system.dialogue.hunter_evaluator.choice.tier5", "rank_table_t5", ""),
                    new DialogueChoice(5, "awakening_system.dialogue.hunter_evaluator.choice.back", "root", "")
            );
            sendNode(player, npc.getId(), "hunter_evaluator", "rank_table", "DEFAULT", speaker, text, choices);
        } else if ("rank_table_t1".equalsIgnoreCase(targetNode)) {
            String text = "awakening_system.dialogue.hunter_evaluator.rank_table_t1";

            List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.back_to_index", "rank_table", ""),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.back", "root", "")
            );
            sendNode(player, npc.getId(), "hunter_evaluator", "rank_table_t1", "DEFAULT", speaker, text, choices);
        } else if ("rank_table_t2".equalsIgnoreCase(targetNode)) {
            String text = "awakening_system.dialogue.hunter_evaluator.rank_table_t2";

            List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.back_to_index", "rank_table", ""),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.back", "root", "")
            );
            sendNode(player, npc.getId(), "hunter_evaluator", "rank_table_t2", "DEFAULT", speaker, text, choices);
        } else if ("rank_table_t3".equalsIgnoreCase(targetNode)) {
            String text = "awakening_system.dialogue.hunter_evaluator.rank_table_t3";

            List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.back_to_index", "rank_table", ""),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.back", "root", "")
            );
            sendNode(player, npc.getId(), "hunter_evaluator", "rank_table_t3", "DEFAULT", speaker, text, choices);
        } else if ("rank_table_t4".equalsIgnoreCase(targetNode)) {
            String text = "awakening_system.dialogue.hunter_evaluator.rank_table_t4";

            List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.back_to_index", "rank_table", ""),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.back", "root", "")
            );
            sendNode(player, npc.getId(), "hunter_evaluator", "rank_table_t4", "DEFAULT", speaker, text, choices);
        } else if ("rank_table_t5".equalsIgnoreCase(targetNode)) {
            String text = "awakening_system.dialogue.hunter_evaluator.rank_table_t5";

            List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.back_to_index", "rank_table", ""),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.back", "root", "")
            );
            sendNode(player, npc.getId(), "hunter_evaluator", "rank_table_t5", "DEFAULT", speaker, text, choices);
        } else if ("explain_ascension".equalsIgnoreCase(targetNode)) {
            String text = "awakening_system.dialogue.hunter_evaluator.explain_ascension";

            List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.start_quest", "start_quest", "START_QUEST"),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.back", "root", "")
            );
            sendNode(player, npc.getId(), "hunter_evaluator", "explain_ascension", "DEFAULT", speaker, text, choices);
        }
    }

    private static void handleHunterEvaluatorChoice(ServerPlayer player, Entity npc, String nodeId, int choiceId) {
        SystemData data = SystemEvents.data(player);
        if (data == null) return;
        String speaker = "awakening_system.dialogue.hunter_evaluator.speaker";

        // Iniciar Misión Oficial de Ascensión
        if (("root_pending".equals(nodeId) && choiceId == 0) || ("explain_ascension".equals(nodeId) && choiceId == 0)) {
            String currentRank = data.evaluatedRank();
            String nextRank = data.getNextRank(currentRank);
            int reqKills = getRequiredAscensionKills(nextRank);

            data.startAscensionQuest(nextRank, reqKills);
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
            AwakeningSystemMod.sync(player);

            String text = "awakening_system.dialogue.hunter_evaluator.quest_started|" + reqKills + "|" + nextRank;
            List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.goodbye", "goodbye", "CLOSE"),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.buy_tracker", "buy_tracker", "BUY_TRACKER")
            );
            sendNode(player, npc.getId(), "hunter_evaluator", "quest_started", "DEFAULT", speaker, text, choices);
            return;
        }

        // Medición Oficial de Maná (Calibración exitosa)
        if ("ascension_ready".equals(nodeId) && choiceId == 0) {
            String oldRank = data.evaluatedRank();
            int bankedExp = data.reserveExperience();
            boolean passed = data.passEvaluation();

            if (passed) {
                String newRank = data.evaluatedRank();

                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 0.5D, 0.8D, 0.5D, 0.1D);
                    sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2D, player.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.05D);
                    sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.6D, 0.8D, 0.6D, 0.15D);
                }
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.1F);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.3F);
                AwakeningSystemMod.sync(player);

                String text = "awakening_system.dialogue.hunter_evaluator.eval_success|" + oldRank + "|" + newRank + "|" + bankedExp + "|" + data.level();

                List<DialogueChoice> choices = List.of(
                        new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.goodbye", "goodbye", "CLOSE")
                );
                sendNode(player, npc.getId(), "hunter_evaluator", "eval_success", "DEFAULT", speaker, text, choices);
                return;
            }
        }

        // Cancelar misión de ascensión
        if ("ascension_in_progress".equals(nodeId) && choiceId == 2) {
            data.cancelAscensionQuest();
            AwakeningSystemMod.sync(player);
            String text = "awakening_system.dialogue.hunter_evaluator.quest_cancelled";
            List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.back", "root", ""),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.goodbye", "goodbye", "CLOSE")
            );
            sendNode(player, npc.getId(), "hunter_evaluator", "quest_cancelled", "DEFAULT", speaker, text, choices);
            return;
        }

        // Comprar Rastreador de la Asociación
        if (("root_pending".equals(nodeId) && choiceId == 2) || ("root_normal".equals(nodeId) && choiceId == 1) || ("ascension_in_progress".equals(nodeId) && choiceId == 1) || ("quest_started".equals(nodeId) && choiceId == 1)) {
            if (data.coins() >= 100) {
                data.addCoins(-100);
                ItemStack tracker = new ItemStack(SystemItems.HUNTER_ASSOCIATION_TRACKER.get());
                if (!player.addItem(tracker)) {
                    player.drop(tracker, false);
                }
                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                AwakeningSystemMod.sync(player);

                String text = "awakening_system.dialogue.hunter_evaluator.tracker_bought";
                List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.back", "root", ""),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.goodbye", "goodbye", "CLOSE")
                );
                sendNode(player, npc.getId(), "hunter_evaluator", "tracker_bought", "DEFAULT", speaker, text, choices);
            } else {
                String text = "awakening_system.dialogue.hunter_evaluator.tracker_failed|" + data.coins();
                List<DialogueChoice> choices = List.of(
                    new DialogueChoice(0, "awakening_system.dialogue.hunter_evaluator.choice.back", "root", ""),
                    new DialogueChoice(1, "awakening_system.dialogue.hunter_evaluator.choice.later", "goodbye", "CLOSE")
                );
                sendNode(player, npc.getId(), "hunter_evaluator", "tracker_failed", "DEFAULT", speaker, text, choices);
            }
            return;
        }

        // Navegación de menús y rangos
        if ("root_pending".equals(nodeId) && choiceId == 1) {
            startHunterEvaluatorDialogue(player, npc, "explain_ascension");
            return;
        }
        if ("root_normal".equals(nodeId) && choiceId == 0) {
            startHunterEvaluatorDialogue(player, npc, "rank_table");
            return;
        }
        if ("rank_table".equals(nodeId)) {
            switch (choiceId) {
                case 0 -> startHunterEvaluatorDialogue(player, npc, "rank_table_t1");
                case 1 -> startHunterEvaluatorDialogue(player, npc, "rank_table_t2");
                case 2 -> startHunterEvaluatorDialogue(player, npc, "rank_table_t3");
                case 3 -> startHunterEvaluatorDialogue(player, npc, "rank_table_t4");
                case 4 -> startHunterEvaluatorDialogue(player, npc, "rank_table_t5");
                default -> startHunterEvaluatorDialogue(player, npc, "root");
            }
            return;
        }
        if (nodeId != null && nodeId.startsWith("rank_table_t")) {
            if (choiceId == 0) {
                startHunterEvaluatorDialogue(player, npc, "rank_table");
            } else {
                startHunterEvaluatorDialogue(player, npc, "root");
            }
            return;
        }
        if ("explain_ascension".equals(nodeId) && choiceId == 1) {
            startHunterEvaluatorDialogue(player, npc, "root");
            return;
        }
        if ("tracker_bought".equals(nodeId) && choiceId == 0) {
            startHunterEvaluatorDialogue(player, npc, "root");
            return;
        }
        if ("tracker_failed".equals(nodeId) && choiceId == 0) {
            startHunterEvaluatorDialogue(player, npc, "root");
            return;
        }
        if ("quest_cancelled".equals(nodeId) && choiceId == 0) {
            startHunterEvaluatorDialogue(player, npc, "root");
            return;
        }
        if ("quest_started".equals(nodeId) && choiceId == 1) {
            handleHunterEvaluatorChoice(player, npc, "root_normal", 1);
            return;
        }

        closeDialogue(player);
    }

    // ==========================================
    // DIÁLOGOS Y RECLUTAMIENTO DE SERIES NPCS
    // ==========================================

    public static void startSeriesNpcDialogue(ServerPlayer player, SeriesNpcEntity npc) {
        if (player == null || npc == null) return;
        NpcProfile profile = npc.getProfile();
        if (profile == NpcProfile.HUNTER_EVALUATOR) {
            startHunterEvaluatorDialogue(player, npc, "root");
            return;
        }

        NPC_SPEAKERS.put(npc.getId(), player.getUUID());
        npc.getNavigation().stop();
        npc.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, player.getEyePosition());

        String speaker = profile.titleColor() + "[" + profile.displayName() + "] §f" + profile.title();
        String quote = profile.dialogues().isEmpty() ? "La verdadera fuerza se demuestra en combate." : profile.dialogues().get(0);
        String text = "§f«" + quote + "»\n\n¿En qué puedo ayudarte hoy, Cazador?";

        GuildSavedData guildData = GuildSavedData.get(player.server);
        Guild guild = (guildData != null && player.server != null) ? guildData.getGuildByPlayer(player.getUUID()) : null;
        
        // El Evaluador y las recepcionistas institucionales son personal neutral y nunca se pueden reclutar
        boolean isSpecialNpc = (profile == NpcProfile.HUNTER_EVALUATOR 
                || profile == NpcProfile.ELENA_RECEPTIONIST 
                || profile == NpcProfile.BLACK_MARKET_DEALER 
                || profile == NpcProfile.CASINO_RECEPTIONIST);

        boolean isGuildMember = !isSpecialNpc && guild != null && guild.hasNpcMember(profile.id());
        boolean canInviteGuild = !isSpecialNpc && !isGuildMember && guild != null && (guild.getLeaderUuid().equals(player.getUUID())
                || (guild.getMember(player.getUUID()) != null && guild.getMember(player.getUUID()).getRole().canInvite()));

        List<DialogueChoice> choices = new ArrayList<>();
        int cId = 0;
        choices.add(new DialogueChoice(cId++, "★ Recibir bendición diaria de cazador ★", "blessing", "BLESSING"));
        SeriesNpcEntity sne = (npc instanceof SeriesNpcEntity s) ? s : null;
        if (isGuildMember) {
            if (sne != null && sne.isFollowing()) {
                choices.add(new DialogueChoice(cId++, "★ Orden de Guardia: Quédate aquí y vigila este lugar ★", "companion_stay", "COMPANION_STAY"));
            } else {
                choices.add(new DialogueChoice(cId++, "★ Orden de Combate: Sígueme y apóyame en la cacería ★", "companion_follow", "COMPANION_FOLLOW"));
            }
        } else if (canInviteGuild) {
            choices.add(new DialogueChoice(cId++, "★ Invitar a unirse al Gremio [" + guild.getName() + "] ★", "invite_guild", "GUILD_INVITE"));
        }
        choices.add(new DialogueChoice(cId++, "Consultar consejos de combate y sabiduría", "lore", "LORE"));
        choices.add(new DialogueChoice(cId++, "Entendido, muchas gracias.", "goodbye", "CLOSE"));

        sendNode(player, npc.getId(), "series_npc", "root", "DEFAULT", speaker, text, choices);
    }

    private static void handleSeriesNpcChoice(ServerPlayer player, Entity npc, String nodeId, int choiceId) {
        if (!(npc instanceof SeriesNpcEntity sne)) {
            closeDialogue(player);
            return;
        }
        NpcProfile profile = sne.getProfile();
        String speaker = profile.titleColor() + "[" + profile.displayName() + "] §f" + profile.title();

        GuildSavedData guildData = GuildSavedData.get(player.server);
        Guild guild = (guildData != null && player.server != null) ? guildData.getGuildByPlayer(player.getUUID()) : null;
        
        // El Evaluador y las recepcionistas institucionales son personal neutral y nunca se pueden reclutar
        boolean isSpecialNpc = (profile == NpcProfile.HUNTER_EVALUATOR 
                || profile == NpcProfile.ELENA_RECEPTIONIST 
                || profile == NpcProfile.BLACK_MARKET_DEALER 
                || profile == NpcProfile.CASINO_RECEPTIONIST);

        boolean isGuildMember = !isSpecialNpc && guild != null && guild.hasNpcMember(profile.id());
        boolean canInviteGuild = !isSpecialNpc && !isGuildMember && guild != null && (guild.getLeaderUuid().equals(player.getUUID())
                || (guild.getMember(player.getUUID()) != null && guild.getMember(player.getUUID()).getRole().canInvite()));

        if ("root".equals(nodeId)) {
            // Caso: Bendición diaria
            if (choiceId == 0) {
                long currentDay = player.level().getDayTime() / 24000L;
                String tagKey = "blessing_day_" + profile.id();
                long lastBlessingDay = player.getPersistentData().getLong(tagKey);

                if (lastBlessingDay != currentDay) {
                    player.getPersistentData().putLong(tagKey, currentDay);
                    profile.applyBlessing(player);
                    player.playNotifySound(ModSounds.NPC_BLESSING.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                    String text = "§a§l★ BENDICIÓN DE CAZADOR OTORGADA ★§r\n\n"
                            + "§fHas recibido la bendición directa de §e" + profile.displayName() + "§f.\n"
                            + "Tus capacidades físicas y espirituales han sido potenciadas por el Sistema.";
                    List<DialogueChoice> choices = List.of(
                            new DialogueChoice(0, "¡Muchas gracias!", "goodbye", "CLOSE")
                    );
                    sendNode(player, npc.getId(), "series_npc", "blessing_given", "DEFAULT", speaker, text, choices);
                } else {
                    String text = "§eYa has recibido mi bendición el día de hoy, Cazador.§f\n\n"
                            + "Regresa mañana cuando el flujo de maná cósmico se haya restablecido.";
                    List<DialogueChoice> choices = List.of(
                            new DialogueChoice(0, "« Volver", "root", ""),
                            new DialogueChoice(1, "Entendido, hasta luego.", "goodbye", "CLOSE")
                    );
                    sendNode(player, npc.getId(), "series_npc", "blessing_cooldown", "DEFAULT", speaker, text, choices);
                }
                return;
            }

            // Caso: Miembro de gremio (Seguir / Montar Guardia)
            if (isGuildMember && choiceId == 1) {
                if (sne.isFollowing()) {
                    sne.setFollowing(false);
                    sne.setHomePos(sne.blockPosition());
                    player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SMOKE, sne.getX(), sne.getY() + 1.0D, sne.getZ(), 25, 0.4D, 0.5D, 0.4D, 0.05D);
                    }
                    String text = "§6§l★ ORDEN DE GUARDIA RECIBIDA ★§r\n\n"
                            + profile.titleColor() + profile.displayName() + ":\n§f«Entendido. Montaré guardia en esta posición hasta que me des nuevas órdenes.»\n\n"
                            + "§7El cazador permanecerá custodiando esta zona.";
                    List<DialogueChoice> choices = List.of(
                            new DialogueChoice(0, "¡Excelente, mantén la vigilancia!", "goodbye", "CLOSE")
                    );
                    sendNode(player, npc.getId(), "series_npc", "companion_stayed", "DEFAULT", speaker, text, choices);
                } else {
                    sne.setOwnerUuid(player.getUUID());
                    sne.setFollowing(true);
                    sne.setHomePos(null);
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sne.getX(), sne.getY() + 1.2D, sne.getZ(), 30, 0.5D, 0.6D, 0.5D, 0.1D);
                    }
                    String text = "§a§l★ ORDEN DE COMBATE ASIGNADA ★§r\n\n"
                            + profile.titleColor() + profile.displayName() + ":\n§f«A la orden, Líder. Te seguiré y lucharé a tu lado contra cualquier enemigo que amenace nuestra expedición.»\n\n"
                            + "§7El cazador te seguirá, atacará a tus objetivos y te defenderá en combate.";
                    List<DialogueChoice> choices = List.of(
                            new DialogueChoice(0, "¡En marcha!", "goodbye", "CLOSE")
                    );
                    sendNode(player, npc.getId(), "series_npc", "companion_following", "DEFAULT", speaker, text, choices);
                }
                return;
            }

            // Caso: Invitar al gremio
            if (canInviteGuild && choiceId == 1) {
                guildData.recruitNpcToGuild(guild, player.server, profile.id(), profile.displayName(), GuildRole.MEMBER);
                sne.setOwnerUuid(player.getUUID());
                sne.setFollowing(true);
                sne.setHomePos(null);
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.1F);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sne.getX(), sne.getY() + 1.2D, sne.getZ(), 30, 0.5D, 0.8D, 0.5D, 0.1D);
                }

                String reply = switch (profile) {
                    case SUNG_JINWOO -> "«Un gremio con tu determinación... Muy bien. Acepto tu invitación. Las sombras respaldarán tu causa.»";
                    case CHA_HAEIN -> "«Me parece una propuesta honorable. Mientras mantengamos la disciplina en las mazmorras, lucharé bajo tu estandarte.»";
                    case KIM_DOKJA -> "«Interesante oferta. Un gremio fuerte nos dará una gran ventaja en los próximos escenarios del Star Stream. Cuenta conmigo.»";
                    case YOO_JOONGHYUK -> "«Hmph... Más vale que no te conviertas en una carga en las regresiones futuras. Acepto por ahora.»";
                    default -> "«Es un honor recibir tu invitación. Acepto unirme a tu gremio y lucharé por su prestigio.»";
                };

                String text = "§a§l★ ¡INVITACIÓN DE GREMIO ACEPTADA! ★§r\n\n"
                        + profile.titleColor() + profile.displayName() + ":\n§f" + reply + "\n\n"
                        + "§7Ahora este cazador forma parte activa de tu gremio. Te seguirá automáticamente en combate o puedes ordenarle montar guardia cuando desees.";
                List<DialogueChoice> choices = List.of(
                        new DialogueChoice(0, "¡Bienvenido al Gremio!", "goodbye", "CLOSE")
                );
                sendNode(player, npc.getId(), "series_npc", "guild_recruited", "DEFAULT", speaker, text, choices);
                return;
            }

            // Caso: Consejos y sabiduría
            int loreChoiceIdx = (isGuildMember || canInviteGuild) ? 2 : 1;
            if (choiceId == loreChoiceIdx) {
                StringBuilder sb = new StringBuilder("§6§l★ CONSEJOS Y EXPERIENCIA DE COMBATE ★§r\n\n");
                for (String line : profile.dialogues()) {
                    sb.append("§7• §f«").append(line).append("»\n");
                }
                List<DialogueChoice> choices = List.of(
                        new DialogueChoice(0, "« Volver", "root", ""),
                        new DialogueChoice(1, "Muchas gracias por tu sabiduría.", "goodbye", "CLOSE")
                );
                sendNode(player, npc.getId(), "series_npc", "lore_display", "DEFAULT", speaker, sb.toString(), choices);
                return;
            }

            // Caso: Entendido, muchas gracias (Cerrar diálogo)
            int closeChoiceIdx = (isGuildMember || canInviteGuild) ? 3 : 2;
            if (choiceId >= closeChoiceIdx) {
                closeDialogue(player);
                return;
            }
        }

        if ("blessing_cooldown".equals(nodeId) && choiceId == 0) {
            startSeriesNpcDialogue(player, sne);
            return;
        }
        if ("lore_display".equals(nodeId) && choiceId == 0) {
            startSeriesNpcDialogue(player, sne);
            return;
        }

        closeDialogue(player);
    }

    // ==========================================
    // TOWN PEOPLE (floor 11): a short RPG-style talk
    // ==========================================

    /** The role a townsperson talks as, from their trade and skin. */
    private static String townRole(Entity npc) {
        if (!(npc instanceof com.soybe.awakeningsystem.world.village.TownNpcEntity t)) return "villager";
        return switch (t.job()) {
            case "weaponsmith", "toolsmith", "armorer", "mason", "leatherworker", "fletcher" -> "smith";
            case "butcher", "fisherman" -> "merchant";
            case "farmer", "shepherd" -> "farmer";
            case "innkeeper", "barmaid" -> "innkeeper";
            case "baker" -> "baker";
            case "librarian", "cartographer" -> "librarian";
            case "cleric" -> "priest";
            default -> com.soybe.awakeningsystem.world.village.TownNpcEntity.roleOfSkin(t.skin());
        };
    }

    /** Lines for a role: plain townsfolk have several, chosen per person, and women their own set. */
    private static String townLines(Entity npc, String role) {
        if (!role.equals("villager")) return role;
        boolean woman = npc instanceof com.soybe.awakeningsystem.world.village.TownNpcEntity t
                && com.soybe.awakeningsystem.world.village.TownNpcEntity.woman(t.skin());
        int v = Math.floorMod(npc.getUUID().hashCode(), 4);
        return (woman ? "villager_f" : "villager") + (v == 0 ? "" : "." + v);
    }

    /** The title over the talk box: the role, in its women's form for a woman ("Tabernera", "Vecina"). */
    private static String townTitle(Entity npc, String role) {
        boolean woman = npc instanceof com.soybe.awakeningsystem.world.village.TownNpcEntity t
                && com.soybe.awakeningsystem.world.village.TownNpcEntity.woman(t.skin());
        return "awakening_system.town.role." + role + (woman && !role.equals("priest") && !role.equals("smith") ? "_f" : "");
    }

    private static void townNode(ServerPlayer player, Entity npc, String node) {
        String role = townRole(npc);
        String speaker = townTitle(npc, role);
        String lines = townLines(npc, role);
        String base = "awakening_system.town.talk.";
        switch (node) {
            case "root" -> sendNode(player, npc.getId(), "town_npc", "root", "DEFAULT", speaker, base + lines + ".hello", List.of(
                    new DialogueChoice(0, base + "ask.work", "work", "NEXT"),
                    new DialogueChoice(1, base + "ask.tip", "tip", "NEXT"),
                    new DialogueChoice(2, base + "ask.town", "town", "NEXT"),
                    new DialogueChoice(3, base + "ask.bye", "goodbye", "CLOSE")));
            default -> {
                String text = switch (node) {
                    case "work" -> base + lines + ".work";
                    case "tip" -> base + "tip." + Math.floorMod(npc.getId() + (int) (player.level().getGameTime() / 400), 5);
                    default -> base + "town";
                };
                sendNode(player, npc.getId(), "town_npc", node, "DEFAULT", speaker, text, List.of(
                        new DialogueChoice(0, base + "ask.more", "root", "NEXT"),
                        new DialogueChoice(1, base + "ask.bye", "goodbye", "CLOSE")));
            }
        }
    }

    private static void sendNode(ServerPlayer player, int entityId, String dialogueId, String nodeId, String nodeType,
                                 String speaker, String text, List<DialogueChoice> choices) {
        SESSIONS.put(player.getUUID(), new ActiveSession(entityId, dialogueId, nodeId, System.currentTimeMillis()));
        PacketDistributor.sendToPlayer(player, new OpenDialoguePayload(
                entityId, dialogueId, nodeId, nodeType, speaker, text, choices
        ));
    }
}
