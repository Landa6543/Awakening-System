package com.soybe.awakeningsystem.artifact;

public enum ArtifactSet {
    CRIMSON_WITCH(
            "crimson_witch",
            "Bruja Carmesí en Llamas",
            "§c",
            5,
            "+20% Daño de Fuego y Habilidades Pyro.",
            "Explosiones y quemaduras infligen +40% de daño adicional y queman por el doble de tiempo."
    ),
    GLADIATORS_FINALE(
            "gladiator",
            "Final del Gladiador",
            "§6",
            5,
            "+18% Ataque Físico y Daño Base.",
            "+35% de Daño adicional al golpear con Espadas, Dagas y Lanzas."
    ),
    VIRIDESCENT_VENERER(
            "viridescent",
            "Sombra Verde Esmeralda",
            "§a",
            5,
            "+20% Daño Anemo / Viento y +15% Velocidad.",
            "Los torbellinos de viento reducen la resistencia física y elemental del enemigo un 30%."
    ),
    MAIDEN_BELOVED(
            "maiden",
            "Doncella Amada",
            "§d",
            5,
            "+20% Eficacia de Curación y Regeneración.",
            "Al curarte a ti o a un compañero, restaura un 25% de salud adicional a todos los aliados cercanos."
    ),
    BLOODSTAINED_CHIVALRY(
            "bloodstained",
            "Caballero Sanguinario",
            "§4",
            5,
            "+25% Daño Físico y Penetración.",
            "Derrotar a un enemigo otorga +50% de Daño al siguiente impacto y derriba al objetivo."
    ),
    BERSERKER(
            "berserker",
            "Berserker",
            "§c",
            4,
            "+12% Probabilidad de Golpe Crítico.",
            "Al estar por debajo del 70% de vida, la Probabilidad Crítica aumenta un +24% adicional."
    ),
    THE_EXILE(
            "exile",
            "El Exiliado",
            "§b",
            4,
            "+20% Recarga de Maná y Energía.",
            "Activar una habilidad definitiva o elemental regenera Maná instantáneo para ti y tus aliados."
    ),
    BRAVE_HEART(
            "brave_heart",
            "Corazón del Guerrero",
            "§e",
            4,
            "+18% Ataque General.",
            "+30% de Daño aumentado contra enemigos que tengan más del 50% de su vida restante."
    ),
    NOBLESSE_OBLIGE(
            "noblesse",
            "Nobleza del Antiguo Régimen",
            "§9",
            5,
            "+20% Daño de Habilidad Definitiva.",
            "Lanzar una Habilidad Definitiva aumenta el ATQ de todos los miembros del equipo un 20% durante 12s."
    ),
    EMBLEM_OF_SEVERED_FATE(
            "emblem",
            "Emblema del Destino",
            "§5",
            5,
            "+20% Recarga de Energía y Maná.",
            "Aumenta el daño de las Habilidades Definitivas en un 25% de la Recarga de Energía total (máx. 75%)."
    ),
    THUNDERING_FURY(
            "thundering_fury",
            "Furia del Trueno",
            "§d",
            5,
            "+15% Bono de Daño Electro.",
            "Aumenta el daño infligido por Sobrecarga, Electrocargado y Superconductor un 40%."
    ),
    BLIZZARD_STRAYER(
            "blizzard_strayer",
            "Nómada del Invierno",
            "§b",
            5,
            "+15% Bono de Daño Cryo.",
            "Al atacar a un enemigo afectado por Cryo, Prob. CRIT +20%. Si el enemigo está Congelado, +20% adicional."
    ),
    HEART_OF_DEPTH(
            "heart_of_depth",
            "Corazón de las Profundidades",
            "§1",
            5,
            "+15% Bono de Daño Hydro.",
            "Tras usar una Habilidad Elemental, aumenta el daño de Ataques Normales y Cargados un 30% por 15s."
    ),
    ARCHAIC_PETRA(
            "archaic_petra",
            "Petra Arcaica",
            "§6",
            5,
            "+15% Bono de Daño Geo.",
            "Al obtener un fragmento generado por Cristalización, todos los aliados obtienen +35% de Bono de Daño Elemental."
    ),
    DEEPWOOD_MEMORIES(
            "deepwood",
            "Recuerdos de la Floresta",
            "§2",
            5,
            "+15% Bono de Daño Dendro.",
            "Las Habilidades reducen la Resistencia Dendro de los enemigos golpeados en un 30% durante 8s."
    ),
    SHIMENAWA_REMINISCENCE(
            "shimenawa",
            "Reminiscencia de la Purificación",
            "§c",
            5,
            "+18% Ataque General.",
            "Al usar una Habilidad Elemental con 15+ de Energía, consume energía para aumentar el daño normal y cargado un 50%."
    ),
    DESERT_PAVILION(
            "desert_pavilion",
            "Épica del Pabellón del Desierto",
            "§3",
            5,
            "+15% Bono de Daño Anemo.",
            "Al golpear con un Ataque Cargado, la Vel. de Ataque aumenta un 10% y el daño infligido un 40% por 15s."
    ),
    OCEAN_HUED_CLAM(
            "ocean_clam",
            "Perla Oceánica",
            "§b",
            5,
            "+15% Bono de Curación.",
            "Acumula la salud restaurada en una Burbuja Espumosa marina que explota infligiendo daño físico equivalente al 90%."
    ),
    TENACITY_OF_THE_MILLELITH(
            "tenacity",
            "Tenacidad de la Geoarmada",
            "§e",
            5,
            "+20% Vida Máxima.",
            "Al golpear a un enemigo con una Habilidad, los miembros del equipo obtienen +20% ATQ y +30% Protección de Escudo."
    ),
    GOLDEN_TROUPE(
            "golden_troupe",
            "Compañía Dorada",
            "§6",
            5,
            "+20% Daño de Habilidad Elemental.",
            "Aumenta el daño de Habilidad Elemental un 25% adicional mientras el personaje no esté en combate activo."
    );

    private final String idName;
    private final String displayName;
    private final String colorCode;
    private final String bonus2p;
    private final String bonus4p;
    private final int maxStars;

    ArtifactSet(String idName, String displayName, String colorCode, int maxStars, String bonus2p, String bonus4p) {
        this.idName = idName;
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.maxStars = maxStars;
        this.bonus2p = bonus2p;
        this.bonus4p = bonus4p;
    }

    public String getIdName() {
        return idName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public int getMaxStars() {
        return maxStars;
    }

    public String getBonus2p() {
        return bonus2p;
    }

    public String getBonus4p() {
        return bonus4p;
    }

    public net.minecraft.network.chat.Component getDisplayNameComponent() {
        return net.minecraft.network.chat.Component.literal(colorCode + displayName);
    }

    public net.minecraft.network.chat.Component getBonus2pComponent() {
        return net.minecraft.network.chat.Component.literal("§7(2) " + bonus2p);
    }

    public net.minecraft.network.chat.Component getBonus4pComponent() {
        return net.minecraft.network.chat.Component.literal("§7(4) " + bonus4p);
    }
}
