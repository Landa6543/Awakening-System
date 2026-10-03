package com.soybe.awakeningsystem.fable;

import net.minecraft.network.chat.Component;

import java.util.*;

public final class FableDefinitions {

    public enum Grade {
        HISTORICAL("Histórica", "§a", 15, 0xFF55FF55),
        LEGENDARY("Legendaria", "§6", 35, 0xFFFFAA00),
        SEMI_MYTH("Semi-Mítica", "§5", 75, 0xFFAA00AA),
        MYTH("Mítica", "§c", 150, 0xFFFF5555),
        GIANT_STORY("Fábula Gigante", "§b§l", 300, 0xFF55FFFF);

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
        // =========================================================================
        // FÁBULAS DE PRIMACÍA MUNDIAL (WORLD-FIRST - Solo 1 jugador por mundo)
        // =========================================================================
        register(new Fable("first_netherite",
                "El Primero en Forjar Netherita",
                "Una fábula grabada en el Star Stream cuando el primer mortal forjó la aleación negra del Inframundo.",
                Grade.HISTORICAL, 25, true, "⛏"));

        register(new Fable("first_awakening",
                "El Primer Despertar",
                "El testimonio del primer ser humano que rompió los límites mortales y seleccionó una Clase de Cazador.",
                Grade.HISTORICAL, 20, true, "⚡"));

        register(new Fable("first_ender_dragon",
                "El Asesino del Dragón del Fin",
                "Un canto triunfal al guerrero que cruzó el abismo del Fin y derribó a la bestia primigenia.",
                Grade.SEMI_MYTH, 80, true, "🐉"));

        register(new Fable("first_wither",
                "Conquistador de la Descomposición",
                "La leyenda de aquel que purgó a la tormenta de tres cabezas y reclamó la Estrella del Inframundo.",
                Grade.SEMI_MYTH, 75, true, "☠"));

        register(new Fable("first_monarch_defeat",
                "Aquel que Desafió a los Monarcas",
                "El Star Stream enmudeció cuando un soberano de la noche cayó ante la espada de un mortal.",
                Grade.MYTH, 160, true, "👑"));

        register(new Fable("first_create_train",
                "El Primer Maquinista Dimensional",
                "El rugido del acero sobre rieles despertó a las constelaciones. Primer tren construido y operado.",
                Grade.LEGENDARY, 50, true, "🚂"));

        register(new Fable("first_create_aircraft",
                "Pionero del Cielo Infinito",
                "Desafiando la gravedad del escenario, construyó la primera nave de surcar los cielos con Aeronautics.",
                Grade.LEGENDARY, 60, true, "✈"));

        register(new Fable("first_level_100",
                "Trascendencia Mortal",
                "Alcanzó el Nivel 100 de cazador, convirtiéndose en una entidad respetada por las nebulosas cósmicas.",
                Grade.MYTH, 180, true, "★"));

        // =========================================================================
        // FÁBULAS DE HAZAÑAS HEROICAS Y PERSONALES (Cualquier cazador calificado)
        // =========================================================================
        register(new Fable("death_defier",
                "Aquel que Engañó a la Muerte",
                "Sobrevivió a un golpe fatal al borde del abismo, desafiando las leyes de la Parca.",
                Grade.LEGENDARY, 35, false, "❤"));

        register(new Fable("solo_dragon_slayer",
                "Matadragones Solitario",
                "Desafió y liquidó a una bestia mítica en solitario sin pedir auxilio a otros cazadores.",
                Grade.SEMI_MYTH, 70, false, "🗡"));

        register(new Fable("shadow_monarch_army",
                "Comandante de la Legión de Sombras",
                "Comandó simultáneamente más de una docena de soldados del reino espectral.",
                Grade.LEGENDARY, 40, false, "👥"));

        register(new Fable("sword_saint_twenty_four",
                "Danza de los Veinticuatro Pétalos",
                "Alcanzó el éxtasis en el arte de la espada de flor de ciruelo cortando el viento.",
                Grade.LEGENDARY, 40, false, "🌸"));

        register(new Fable("titan_slayer",
                "El Aplastador de Titanes",
                "Descargó un impacto devastador con más de 150 puntos de daño concentrado.",
                Grade.SEMI_MYTH, 75, false, "💥"));

        register(new Fable("dungeon_red_gate_conqueror",
                "Superviviente del Gate Rojo",
                "Conquistó una brecha de calamidad de Rango Rojo y regresó con vida para contarlo.",
                Grade.SEMI_MYTH, 85, false, "🚪"));

        register(new Fable("steam_colossus",
                "Arquitecto de los Mil Mecanismos",
                "Maestría en ingeniería de vapor y redes cinéticas de Create.",
                Grade.LEGENDARY, 45, false, "⚙"));

        register(new Fable("sky_dreadnought_commander",
                "Almirante del Acorazado Celeste",
                "Capitaneó una fortaleza voladora armada surcando tormentas con Aeronautics.",
                Grade.MYTH, 130, false, "🛸"));

        register(new Fable("giant_story_demon_king",
                "La Caída del Rey Demonio del 73º Escenario",
                "Fábula Gigante: Lideró la purga del Rey Demonio Baran sellando el destino de una era.",
                Grade.GIANT_STORY, 300, false, "🔥"));

        register(new Fable("giant_story_star_stream_savior",
                "El Salvador del Escenario Olvidado",
                "Fábula Gigante: Completó 50 misiones del gremio y ascendió al Reino de Prestigio.",
                Grade.GIANT_STORY, 350, false, "✨"));
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
