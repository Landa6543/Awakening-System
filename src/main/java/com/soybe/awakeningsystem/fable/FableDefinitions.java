package com.soybe.awakeningsystem.fable;

import net.minecraft.network.chat.Component;

import java.util.*;

public final class FableDefinitions {

    public enum Grade {
        HISTORICAL("Historical", "§a", 15, 0xFF55FF55),
        LEGENDARY("Legendary", "§6", 40, 0xFFFFAA00),
        SEMI_MYTH("Semi-Mythic", "§5", 80, 0xFFAA00AA),
        MYTH("Mythic", "§c", 160, 0xFFFF5555),
        GIANT_STORY("Giant Story", "§b§l", 320, 0xFF55FFFF);

        private final String displayName;
        private final String colorCode;
        private final int defaultWeight;
        private final int hexColor;

        Grade(String displayName, String colorCode, int defaultWeight, int hexColor) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.defaultWeight = defaultWeight;
            this.hexColor = hexColor;
        }

        public String displayName() { return displayName; }
        public Component getDisplayName() {
            return Component.translatable("awakening_system.fable.grade." + name().toLowerCase(java.util.Locale.ROOT));
        }
        public String colorCode() { return colorCode; }
        public int defaultWeight() { return defaultWeight; }
        public int hexColor() { return hexColor; }
    }

    public record Fable(
            String id,
            String title,
            String description,
            Grade grade,
            int weight,
            boolean isWorldFirst,
            String icon
    ) {
        public Component getTitleComponent() {
            return Component.translatable("awakening_system.fable." + id + ".title");
        }

        public Component getDescriptionComponent() {
            return Component.translatable("awakening_system.fable." + id + ".desc");
        }

        public Component getFormattedTitle() {
            return Component.literal(grade.colorCode() + "[").append(getTitleComponent()).append(Component.literal("]"));
        }
    }

    private static final Map<String, Fable> FABLES = new LinkedHashMap<>();

    private static void register(Fable fable) {
        FABLES.put(fable.id(), fable);
    }

    static {
        register(new Fable("first_netherite",
                "El Primero en Forjar Netherita",
                "Una fábula grabada en el Star Stream cuando el primer mortal forjó la aleación negra del Inframundo.",
                Grade.HISTORICAL, 25, true, "⛏"));

        register(new Fable("first_awakening",
                "El Primer Despertar",
                "El testimonio del primer ser humano que rompió los límites mortales y seleccionó una Clase de Cazador.",
                Grade.HISTORICAL, 20, true, "⚡"));

        register(new Fable("first_blood_hunter",
                "Primer Bautismo de Caza",
                "Derrotó a su primera criatura hostil, grabando su nombre en el Star Stream.",
                Grade.HISTORICAL, 15, false, "⚔"));

        register(new Fable("dungeon_initiate",
                "Iniciado de las Mazmorras",
                "Conquistó su primera mazmorra dimensional, cruzando el umbral del peligro.",
                Grade.HISTORICAL, 20, false, "🚪"));

        register(new Fable("miner_depths",
                "Ecos de las Profundidades",
                "Extrajo su primer diamante en las entrañas de la roca profunda.",
                Grade.HISTORICAL, 15, false, "⛏"));

        register(new Fable("first_trade",
                "Pacto del Mercader Errante",
                "Realizó su primera transacción comercial con la Tienda del Sistema o mercaderes.",
                Grade.HISTORICAL, 15, false, "🪙"));

        register(new Fable("iron_will",
                "Templado en Hierro",
                "Portó una armadura completa de metal templado, blindando su cuerpo contra el destino.",
                Grade.HISTORICAL, 15, false, "🛡"));

        register(new Fable("apprentice_alchemist",
                "Destilador de Esencias",
                "Elaboró o consumió su primer elixir o poción alquímica del Sistema.",
                Grade.HISTORICAL, 15, false, "🧪"));

        register(new Fable("adventurer_steps",
                "Pasos del Caminante",
                "Recorrió más de cinco mil bloques explorando los confines del mundo.",
                Grade.HISTORICAL, 20, false, "🧭"));

        register(new Fable("first_enchantment",
                "Susurro de Runas",
                "Imbuyó un arma con magia rúnica antigua en la mesa de encantamientos.",
                Grade.HISTORICAL, 20, false, "✨"));

        register(new Fable("bounty_hunter_novice",
                "Cazarrecompensas Novato",
                "Completó su primera misión de cacería en el Tablón de la Asociación de Cazadores.",
                Grade.HISTORICAL, 20, false, "📜"));

        register(new Fable("culinary_survivor",
                "Banquete del Cazador",
                "Cocinó y repuso fuerzas junto al calor de una hoguera de campamento tras la batalla.",
                Grade.HISTORICAL, 15, false, "🍖"));

        register(new Fable("nether_traveler",
                "Cruzador del Inframundo",
                "Cruzó el velo de obsidiana y pisó los ardientes páramos del Nether.",
                Grade.HISTORICAL, 20, false, "🔥"));

        register(new Fable("first_pet_tamed",
                "Lazo con las Bestias",
                "Forjó un lazo de lealtad inquebrantable domesticando a una criatura compañera.",
                Grade.HISTORICAL, 15, false, "🐾"));

        register(new Fable("ruin_scavenger",
                "Saqueador de Ruinas",
                "Desbloqueó un cofre de tesoro sellado en una cripta o templo en ruinas.",
                Grade.HISTORICAL, 20, false, "🏛"));

        register(new Fable("first_create_train",
                "El Primer Maquinista Dimensional",
                "El rugido del acero sobre rieles despertó a las constelaciones. Primer tren construido y operado.",
                Grade.LEGENDARY, 50, true, "🚂"));

        register(new Fable("first_create_aircraft",
                "Pionero del Cielo Infinito",
                "Desafiando la gravedad del escenario, construyó la primera nave de surcar los cielos con Aeronautics.",
                Grade.LEGENDARY, 60, true, "✈"));

        register(new Fable("death_defier",
                "Aquel que Engañó a la Muerte",
                "Sobrevivió a un golpe fatal al borde del abismo, desafiando las leyes de la Parca.",
                Grade.LEGENDARY, 35, false, "❤"));

        register(new Fable("shadow_monarch_army",
                "Comandante de la Legión de Sombras",
                "Comandó simultáneamente a más de una docena de soldados del reino espectral de sombras.",
                Grade.LEGENDARY, 40, false, "👥"));

        register(new Fable("sword_saint_twenty_four",
                "Danza de los Veinticuatro Pétalos",
                "Alcanzó el éxtasis en el arte de la espada de la flor de ciruelo de Mount Hua cortando el viento.",
                Grade.LEGENDARY, 40, false, "🌸"));

        register(new Fable("steam_colossus",
                "Arquitecto de Mil Mecanismos",
                "Dominó la ingeniería de vapor y redes cinéticas rotacionales de Create.",
                Grade.LEGENDARY, 45, false, "⚙"));

        register(new Fable("igris_crimson_conqueror",
                "Triunfo sobre el Caballero Carmesí",
                "Venció al imponente Comandante Rojo Igris en duelo singular en la sala del trono.",
                Grade.LEGENDARY, 50, false, "🩸"));

        register(new Fable("master_blacksmith",
                "Herrero Celestial de la Forja de Almas",
                "Forjó una pieza de equipo supremo utilizando la Forja de Almas o Astralita.",
                Grade.LEGENDARY, 45, false, "🔨"));

        register(new Fable("speed_demon",
                "Relámpago Intangible",
                "Superó los límites físicos alcanzando una velocidad vertiginosa en pleno combate.",
                Grade.LEGENDARY, 40, false, "⚡"));

        register(new Fable("wealth_of_nations",
                "Magnate del Star Stream",
                "Amasó una fortuna de más de veinticinco mil Monedas del Despertar.",
                Grade.LEGENDARY, 45, false, "💰"));

        register(new Fable("combo_virtuoso",
                "Tempestad de Cien Filos",
                "Encadenó una ráfaga devastadora de ataques continuos sin ser tocado.",
                Grade.LEGENDARY, 40, false, "🌀"));

        register(new Fable("deep_dark_explorer",
                "El Silencio del Abismo Oscuro",
                "Caminó entre las ruinas del Sculk y derrotó al guardián de las sombras.",
                Grade.LEGENDARY, 50, false, "👁"));

        register(new Fable("elemental_harmonizer",
                "Maestro de los Elementos",
                "Armonizó y desató habilidades de múltiples afinidades elementales combinadas.",
                Grade.LEGENDARY, 45, false, "🔮"));

        register(new Fable("guild_veteran",
                "Pilar del Gremio de Cazadores",
                "Completó quince contratos de cacería, ganándose el respeto de la Asociación.",
                Grade.LEGENDARY, 50, false, "🏆"));

        register(new Fable("unbroken_rampart",
                "Muralla Inquebrantable",
                "Mitigó y resistió una cantidad colosal de daño en combate sin quebrantarse.",
                Grade.LEGENDARY, 40, false, "🛡"));

        register(new Fable("demon_king_subjugator",
                "Subyugador del Rey Demonio",
                "Derrotó al Rey Demonio Baran en combate de mazmorra, disipando sus llamas.",
                Grade.LEGENDARY, 55, false, "👹"));

        register(new Fable("first_ender_dragon",
                "El Asesino del Dragón del Fin",
                "Un canto triunfal al guerrero que cruzó el abismo del Fin y derribó a la bestia primigenia.",
                Grade.SEMI_MYTH, 80, true, "🐉"));

        register(new Fable("first_wither",
                "Conquistador de la Descomposición",
                "La leyenda de aquel que purgó a la tormenta de tres cabezas y reclamó la Estrella del Inframundo.",
                Grade.SEMI_MYTH, 75, true, "☠"));

        register(new Fable("solo_dragon_slayer",
                "Asesino Solitario de Dragones",
                "Desafió y liquidó a un dragón mítico en solitario sin buscar auxilio de otros cazadores.",
                Grade.SEMI_MYTH, 70, false, "🗡"));

        register(new Fable("titan_slayer",
                "Aplastador de Titanes",
                "Desató un impacto demoledor con más de 150 puntos de daño concentrado.",
                Grade.SEMI_MYTH, 75, false, "💥"));

        register(new Fable("dungeon_red_gate_conqueror",
                "Superviviente del Gate Rojo",
                "Conquistó una brecha de calamidad de Rango Rojo y regresó con vida para contarlo.",
                Grade.SEMI_MYTH, 85, false, "🚪"));

        register(new Fable("beru_ant_king_crusher",
                "El Verdugo del Rey Hormiga",
                "Aplastó a Beru, el depredador supremo de la Isla Jeju, silenciando su chillido.",
                Grade.SEMI_MYTH, 85, false, "🐜"));

        register(new Fable("tower_conqueror_floor_50",
                "Conquistador de la Torre de Juicio",
                "Superó los desafiantes pisos superiores de la Torre de las Cien Pruebas.",
                Grade.SEMI_MYTH, 80, false, "🗼"));

        register(new Fable("gate_breaker_s_rank",
                "Conquistador de la Puerta Rango S",
                "Purgó y selló una mazmorra de Calamidad Rango S salvando la región circundante.",
                Grade.SEMI_MYTH, 90, false, "💠"));

        register(new Fable("isekai_pioneer",
                "Explorador de las Raíces de Yggdrasil",
                "Cruzó a la dimensión Isekai y cosechó madera sagrada de las raíces primordiales.",
                Grade.SEMI_MYTH, 75, false, "🌳"));

        register(new Fable("murim_celestial_cultivator",
                "Ascensión del Cultivador de Murim",
                "Alcanzó el Reino Espiritual en la Pagoda Milenaria del mundo de Murim.",
                Grade.SEMI_MYTH, 80, false, "☯"));

        register(new Fable("shadow_monarch_domain",
                "Soberanía del Ejército de Sombras",
                "Mantuvo un contingente de quince soldados de sombra despiertos simultáneamente.",
                Grade.SEMI_MYTH, 85, false, "🌑"));

        register(new Fable("astral_dragon_subjugator",
                "El Domador del Dragón Astral",
                "Dio caza al milenario Dragón Astral, reclamando sus escamas cósmicas.",
                Grade.SEMI_MYTH, 90, false, "✨"));

        register(new Fable("colossal_sculk_warden_slayer",
                "Verdugo del Titán de Sculk",
                "Derribó al colosal Warden del Abismo silenciando su onda sónica definitiva.",
                Grade.SEMI_MYTH, 85, false, "🔊"));

        register(new Fable("tri_lunar_pilgrim",
                "Peregrino del Santuario Trilunar",
                "Sintonizó con las tres lunas de Teyvat desbloqueando sus bendiciones sagradas.",
                Grade.SEMI_MYTH, 75, false, "🌙"));

        register(new Fable("first_monarch_defeat",
                "Aquel que Desafió a los Monarcas",
                "El Star Stream enmudeció cuando un soberano de la noche cayó ante la espada de un mortal en una prueba de calamidad.",
                Grade.MYTH, 160, true, "👑"));

        register(new Fable("first_level_100",
                "Trascendencia Mortal",
                "Alcanzó el Nivel 100 de cazador, convirtiéndose en una entidad respetada por las nebulosas cósmicas.",
                Grade.MYTH, 180, true, "★"));

        register(new Fable("sky_dreadnought_commander",
                "Almirante del Acorazado Celeste",
                "Capitaneó una fortaleza voladora artillada desafiando tormentas cósmicas con Aeronautics.",
                Grade.MYTH, 130, false, "🛸"));

        register(new Fable("god_slayer_statue",
                "Aquel que Desafió al Dios de Piedra",
                "Sobrevivió a los mandamientos del Templo Cartenon y derribó a la colosal Estatua de Dios.",
                Grade.MYTH, 170, false, "🗿"));

        register(new Fable("prestige_transcendence",
                "Trascendencia del Alma Inmortal",
                "Completó la purificación kármica del Prestigio renaciendo con poder superior.",
                Grade.MYTH, 165, false, "🌟"));

        register(new Fable("limbo_void_walker",
                "El Caminante del Vórtice del Limbo",
                "Cruzó el portal del Limbo y purgó a una entidad cósmica en el vacío primordial.",
                Grade.MYTH, 155, false, "🌀"));

        register(new Fable("abyss_conqueror_floor_12",
                "Amo de la Espiral del Abismo",
                "Conquistó el Piso Doce de la Espiral del Abismo reclamando las estrellas estelares.",
                Grade.MYTH, 160, false, "🌌"));

        register(new Fable("dragon_monarch_purger",
                "El Purgador de Reyes Dragón",
                "Aniquiló a un Dragón Primordial Ancestral en combate de proporciones divinas.",
                Grade.MYTH, 175, false, "🐲"));

        register(new Fable("level_200_demigod",
                "Semidiós del Star Stream",
                "Alcanzó el legendario Nivel Doscientos de Cazador, consagrando su nombre estelar.",
                Grade.MYTH, 180, false, "⚜"));

        register(new Fable("hundred_dungeons_conqueror",
                "El Conquistador de Cincuenta Catacumbas",
                "Completó cincuenta mazmorras dimensionales convirtiéndose en una leyenda viviente.",
                Grade.MYTH, 150, false, "🗝"));

        register(new Fable("giant_story_demon_king",
                "La Caída del Rey Demonio del Escenario 73",
                "Relato Gigante: Lideró la purga del Rey Demonio Baran sellando el destino de una era.",
                Grade.GIANT_STORY, 300, false, "🔥"));

        register(new Fable("giant_story_star_stream_savior",
                "Salvador del Escenario Olvidado",
                "Relato Gigante: Completó 50 misiones del gremio y ascendió al Reino del Prestigio.",
                Grade.GIANT_STORY, 350, false, "✨"));

        register(new Fable("giant_story_monarch_of_shadows",
                "El Nacimiento del Monarca de las Sombras",
                "Relato Gigante: Despertó el poder absoluto de las sombras y levantó un ejército inmortal.",
                Grade.GIANT_STORY, 320, false, "👑"));

        register(new Fable("giant_story_torchbearer_of_the_end",
                "El Portador de la Antorcha del Apocalipsis",
                "Relato Gigante: Contuvo y erradicó una Ruptura Dimensional Masiva salvando al mundo.",
                Grade.GIANT_STORY, 340, false, "🕯"));

        register(new Fable("giant_story_myth_creator",
                "Autor de un Mito Cósmico",
                "Relato Gigante: Acumuló más de quince fábulas gloriosas resonando en todo el universo.",
                Grade.GIANT_STORY, 380, false, "📜"));

        register(new Fable("giant_story_cosmic_liberator",
                "Libertador del Escenario Infinito",
                "Relato Gigante: Conquistó los mayores desafíos de las tres dimensiones: Overworld, Isekai y Murim.",
                Grade.GIANT_STORY, 400, false, "🌌"));

    }

    public static Fable get(String id) {
        return FABLES.get(id);
    }

    public static Collection<Fable> getAll() {
        return Collections.unmodifiableCollection(FABLES.values());
    }

    public static List<Fable> getWorldFirstFables() {
        List<Fable> list = new ArrayList<>();
        for (Fable f : FABLES.values()) {
            if (f.isWorldFirst()) list.add(f);
        }
        return list;
    }
}
