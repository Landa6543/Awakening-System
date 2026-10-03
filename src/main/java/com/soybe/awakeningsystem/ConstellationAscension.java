package com.soybe.awakeningsystem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.Random;

public final class ConstellationAscension {
    private static final Random RNG = new Random();

    private static final List<String> STRENGTH_TITLES = List.of(
            "Soberano de la Espada Primordial",
            "Destructor de Montañas y Cielos",
            "Señor de la Furia de Hierro",
            "Titán del Juicio de Sangre",
            "Monarca del Puño Inquebrantable"
    );

    private static final List<String> AGILITY_TITLES = List.of(
            "Sombra del Viento Silencioso",
            "Cazador de la Luna Plateada",
            "Danza de la Tormenta Espectral",
            "Fantasma del Abismo Rápido",
            "Relámpago de la Noche Estrellada"
    );

    private static final List<String> VITALITY_TITLES = List.of(
            "Guardián de la Vida Eterna",
            "Bastión de la Fortaleza Imperecedera",
            "Corazón del Fénix Inmortal",
            "Señor de las Cien Heridas",
            "Escudo del Ocaso Sin Fin"
    );

    private static final List<String> INTELLIGENCE_TITLES = List.of(
            "Arquitecto del Conocimiento Arcano",
            "Señor de la Biblioteca Infinita",
            "Gobernante del Destino Cósmico",
            "Tejedor del Caos Estelar",
            "Maestro de los Secretos del Vacío"
    );

    private static final List<String> WILLPOWER_TITLES = List.of(
            "Monarca de la Voluntad Inquebrantable",
            "Ermitaño del Vórtice del Alma",
            "Juez del Vacío Sombrío",
            "Soberano del Trance Carmesí",
            "Espíritu que Desafía al Destino"
    );

    private static final List<String> PERCEPTION_TITLES = List.of(
            "Ojo del Destino Olvidado",
            "Observador de la Realidad Distante",
            "Oráculo del Ocaso Silencioso",
            "Vigía de las Estrellas Lejanas",
            "Aquel que Ve a Través del Velo"
    );

    private ConstellationAscension() {}

    public static String generateModifier(SystemData data) {
        int str = data.strength();
        int agi = data.agility();
        int vit = data.vitality();
        int in = data.intelligence();
        int wil = data.willpower();
        int per = data.perception();

        int max = Math.max(str, Math.max(agi, Math.max(vit, Math.max(in, Math.max(wil, per)))));
        List<String> pool;
        if (max == str) pool = STRENGTH_TITLES;
        else if (max == agi) pool = AGILITY_TITLES;
        else if (max == in) pool = INTELLIGENCE_TITLES;
        else if (max == vit) pool = VITALITY_TITLES;
        else if (max == wil) pool = WILLPOWER_TITLES;
        else pool = PERCEPTION_TITLES;

        return pool.get(RNG.nextInt(pool.size()));
    }

    public static boolean triggerAscension(ServerPlayer player, String reason, String customModifier) {
        SystemData data = SystemEvents.data(player);
        if (data == null) return false;
        if (data.isConstellation()) return false;

        String modifier = (customModifier != null && !customModifier.trim().isEmpty())
                ? customModifier.trim()
                : generateModifier(data);

        data.setIsConstellation(true);
        data.setConstellationModifier(modifier);

        // Anuncio ceremonial del Transmisor Estelar en el chat de todo el servidor
        if (player.getServer() != null) {
            String pName = player.getScoreboardName();
            for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                p.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0F, 1.0F);

                // Notificación emergente holográfica del Sistema en pantalla
                String displayMod = getTranslatedTitleComponent(modifier).getString();
                if (p.getUUID().equals(player.getUUID())) {
                    AwakeningSystemMod.notify(p,
                            "awakening_system.notification.constellation.ascension_self_title",
                            "awakening_system.notification.constellation.ascension_self_msg",
                            0xFFFFD700, displayMod, reason);
                } else {
                    AwakeningSystemMod.notify(p,
                            "awakening_system.notification.constellation.ascension_broadcast_title",
                            "awakening_system.notification.constellation.ascension_broadcast_msg",
                            0xFFFFD700, pName, displayMod);
                }
            }
        }

        // Efectos visuales de ascensión
        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 1.2D, 1.5D, 1.2D, 0.15D);
            sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 1.0D, 1.0D, 1.0D, 0.08D);
            sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.0D);
        }

        AwakeningSystemMod.sync(player);
        return true;
    }

    public static void checkMonarchBossDefeat(ServerPlayer player, int bossVariant) {
        SystemData data = SystemEvents.data(player);
        if (data == null || data.isConstellation()) return;

        String bossName = (bossVariant == 1) ? "Ashborn, Monarca de las Sombras" : "Antares, Monarca de la Destrucción";
        triggerAscension(player, "Victoria Épica sobre " + bossName, null);
    }

    public static void checkRedGateClear(ServerPlayer player, String rank) {
        SystemData data = SystemEvents.data(player);
        if (data == null || data.isConstellation()) return;

        int tier = DungeonManager.getRankTier(rank);
        if (tier >= 7) {
            triggerAscension(player, "Superación de Gate Rojo de Calamidad Rango " + rank, null);
        }
    }

    public static void checkLevelMilestone(ServerPlayer player, int level) {
        SystemData data = SystemEvents.data(player);
        if (data == null || data.isConstellation()) return;

        // Requisito canónico de ORV: El nivel ordinario por sí solo no otorga estatus de Constelación.
        // La ascensión a Constelación requiere acumulación de Fábulas míticas (checkFableAscension)
        // o derrotar Monarcas supremos (checkMonarchBossDefeat) o Gates Calamitosos (checkRedGateClear).
    }

    public static void checkFableAscension(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        if (data == null || data.isConstellation()) return;

        // Requisito de ORV: al menos 5 fábulas y un peso de fábulas de 150+
        if (data.unlockedFables().size() >= 5 && data.fableWeight() >= 150) {
            triggerAscension(player, "Acumulación Trascendente de Fábulas (" + data.fableWeight() + " Peso Estelar)", null);
        }
    }

    public static Component getTranslatedTitleComponent(String modifier) {
        if (modifier == null || modifier.isEmpty()) {
            return Component.translatable("awakening_system.constellation.default_modifier");
        }
        String key = switch (modifier.trim()) {
            case "Soberano de la Espada Primordial" -> "awakening_system.constellation.title.sovereign_primordial_sword";
            case "Destructor de Montañas y Cielos" -> "awakening_system.constellation.title.destroyer_mountains_skies";
            case "Señor de la Furia de Hierro" -> "awakening_system.constellation.title.lord_iron_fury";
            case "Titán del Juicio de Sangre" -> "awakening_system.constellation.title.titan_blood_judgment";
            case "Monarca del Puño Inquebrantable" -> "awakening_system.constellation.title.monarch_unbreakable_fist";

            case "Sombra del Viento Silencioso" -> "awakening_system.constellation.title.shadow_silent_wind";
            case "Cazador de la Luna Plateada" -> "awakening_system.constellation.title.hunter_silver_moon";
            case "Danza de la Tormenta Espectral" -> "awakening_system.constellation.title.dance_spectral_storm";
            case "Fantasma del Abismo Rápido" -> "awakening_system.constellation.title.ghost_swift_abyss";
            case "Relámpago de la Noche Estrellada" -> "awakening_system.constellation.title.lightning_starry_night";

            case "Guardián de la Vida Eterna" -> "awakening_system.constellation.title.guardian_eternal_life";
            case "Bastión de la Fortaleza Imperecedera" -> "awakening_system.constellation.title.bastion_imperishable_fortitude";
            case "Corazón del Fénix Inmortal" -> "awakening_system.constellation.title.heart_immortal_phoenix";
            case "Señor de las Cien Heridas" -> "awakening_system.constellation.title.lord_hundred_wounds";
            case "Escudo del Ocaso Sin Fin" -> "awakening_system.constellation.title.shield_endless_twilight";

            case "Arquitecto del Conocimiento Arcano" -> "awakening_system.constellation.title.architect_arcane_knowledge";
            case "Señor de la Biblioteca Infinita" -> "awakening_system.constellation.title.master_infinite_library";
            case "Gobernante del Destino Cósmico" -> "awakening_system.constellation.title.ruler_cosmic_destiny";
            case "Tejedor del Caos Estelar" -> "awakening_system.constellation.title.weaver_stellar_chaos";
            case "Maestro de los Secretos del Vacío" -> "awakening_system.constellation.title.master_void_secrets";

            case "Monarca de la Voluntad Inquebrantable" -> "awakening_system.constellation.title.monarch_unyielding_will";
            case "Ermitaño del Vórtice del Alma" -> "awakening_system.constellation.title.hermit_soul_vortex";
            case "Juez del Vacío Sombrío" -> "awakening_system.constellation.title.judge_somber_void";
            case "Soberano del Trance Carmesí" -> "awakening_system.constellation.title.sovereign_crimson_trance";
            case "Espíritu que Desafía al Destino" -> "awakening_system.constellation.title.spirit_defies_fate";

            case "Ojo del Destino Olvidado" -> "awakening_system.constellation.title.eye_forgotten_destiny";
            case "Observador de la Realidad Distante" -> "awakening_system.constellation.title.watcher_distant_reality";
            case "Oráculo del Ocaso Silencioso" -> "awakening_system.constellation.title.oracle_silent_twilight";
            case "Vigía de las Estrellas Lejanas" -> "awakening_system.constellation.title.sentinel_distant_stars";
            case "Aquel que Ve a Través del Velo" -> "awakening_system.constellation.title.he_who_sees_veil";

            case "Constelación Despertada" -> "awakening_system.constellation.default_modifier";
            default -> null;
        };
        if (key != null) {
            return Component.translatable(key);
        }
        return Component.literal(modifier);
    }
}
