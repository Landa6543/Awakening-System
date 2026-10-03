package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.entity.*;
import com.soybe.awakeningsystem.entity.animal.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class SystemBestiaryScreen extends Screen {

    public record BestiaryEntry(
            String name,
            String category,
            int categoryColor,
            String title,
            LivingEntity entity,
            int scale,
            String hp,
            String dmg,
            String armor,
            String speed,
            List<String> abilities,
            String tactics,
            String drops,
            String lore
    ) {}

    private final List<BestiaryEntry> entries = new ArrayList<>();
    private int selectedIndex = 0;
    private int scrollOffset = 0;
    private int dossierScrollOffset = 0;
    private float modelRotationYaw = 0.0F;

    public SystemBestiaryScreen() {
        super(Component.translatable("awakening_system.screen.bestiary.title"));
    }

    @Override
    protected void init() {
        super.init();
        initEntries();
        dossierScrollOffset = 0;
    }

    private void initEntries() {
        entries.clear();
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        // 1. Ashborn, Monarca de las Sombras
        var ashborn = new ShadowMonarchBossEntity(ModEntities.SHADOW_MONARCH_BOSS.get(), level);
        ashborn.setVariant(1);
        entries.add(new BestiaryEntry(
                "Ashborn", "MONARCA", 0xFFE4A7FF, "Monarca de las Sombras (Solo Leveling)", ashborn, 22,
                "500 - 8500 HP", "20 - 75 ATK", "16 - 30 DEF", "0.30 SPD",
                List.of(
                        "• Dominio de la Muerte: Drena vida y sumerge el área en Wither y Oscuridad.",
                        "• Autoridad del Gobernante: Telequinesis que arrastra al cazador hacia su filo.",
                        "• Gran Tajo 360: Barrido giratorio devastador de Gran Espada de las Sombras.",
                        "• Invocación de Sombras: Convoca batallones de infantería espectral leal."
                ),
                "Mantén la distancia durante su Dominio, usa saltos evasivos contra su tirón gravitacional.",
                "Corazón del Monarca de las Sombras, Esencia de la Muerte, Cristales Abisales.",
                "El mayor Fragmento de Luz Brillante que renació como el Rey de los Muertos y soberano absoluto de la oscuridad eterna."
        ));

        // 2. Antares, Monarca de la Destrucción
        var antares = new ShadowMonarchBossEntity(ModEntities.SHADOW_MONARCH_BOSS.get(), level);
        antares.setVariant(0);
        entries.add(new BestiaryEntry(
                "Antares", "MONARCA", 0xFFFF5555, "Monarca de la Destrucción (Solo Leveling)", antares, 22,
                "500 - 8500 HP", "20 - 75 ATK", "16 - 30 DEF", "0.32 SPD",
                List.of(
                        "• Aliento del Dragón: Llamas infernales continuas que funden armaduras.",
                        "• Garras de la Ruina: Desgarre brutal con hemorragia profunda.",
                        "• Lluvia de Meteoros: Bombardeo ígneo devastador de amplio radio.",
                        "• Furia Berserk (Fase 2): Incremento crítico de daño de impacto y velocidad."
                ),
                "Equipa pociones de resistencia al fuego extremo y ataca su flanco tras el aliento pesado.",
                "Escamas del Dragón de Destrucción, Núcleo Volcánico Primordial, Orbe de Furia.",
                "El Rey de los Dragones Berserk y antagonista supremo. Su único propósito es reducir la creación entera a cenizas."
        ));

        // 3. Igris, Caballero Carmesí
        var igris = new IgrisBossEntity(ModEntities.IGRIS_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "Igris", "COMANDANTE", 0xFFFF4444, "Comandante Sangriento del Salón del Trono", igris, 24,
                "450 - 5500 HP", "18 - 60 ATK", "16 - 28 DEF", "0.34 SPD",
                List.of(
                        "• Gran Espada Carmesí: Cortes colosales que aplican desangramiento profundo.",
                        "• Tajo Supersónico (Dash): Acometida instantánea que rompe cualquier guardia de escudo.",
                        "• Guardia del Trono: Despliegue de caballeros withered con aura de supresión.",
                        "• Lealtad Monárquica: Al ser derrotado puede extraerse como sombra de rango Comandante."
                ),
                "Esquiva hacia los laterales cuando alce su espada a dos manos. Su alcance frontal es inmenso.",
                "Yelmo Carmesí de Igris, Espada Larga de Sangre, Esencia de Nobleza Leal.",
                "El noble comandante de caballería que custodió el trono vacío durante centurias esperando al digno sucesor."
        ));

        // 4. Baran, Monarca de las Llamas Blancas
        var baran = new BaranBossEntity(ModEntities.BARAN_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "Baran", "MONARCA", 0xFFFFD700, "Monarca de las Llamas Blancas y Rey Demonio", baran, 22,
                "550 - 7500 HP", "20 - 70 ATK", "18 - 30 DEF", "0.36 SPD",
                List.of(
                        "• Rayo de Fuego Blanco: Descargas de plasma continuo de daño abrumador.",
                        "• Dagas de Tormenta: Ráfagas duales relampagueantes a velocidad supersónica.",
                        "• Tormenta del Rey Demonio: Cielos electrizados con lluvia de rayos constantes.",
                        "• Invocación Demoníaca: Convoca legionarios y orcos del rayo celestial."
                ),
                "Muévete continuamente en círculos para eludir las descargas celestes y mantén tu escudo listo.",
                "Cuerno de Baran, Dagas de Tormenta Demoníaca, Esencia de Rayo Blanco.",
                "Soberano de la nobleza demoníaca. Su dominio del fuego blanco y el trueno lo convirtió en una de las mayores pesadillas del Sistema."
        ));

        // 5. Hombre Bestia Guerrero (Beastman)
        var beastman = new BeastmanWarriorEntity(ModEntities.BEASTMAN_WARRIOR.get(), level);
        entries.add(new BestiaryEntry(
                "Hombre Bestia", "ISEKAI", 0xFFFFAA00, "Guerrero Feroz de las Estepas Isekai", beastman, 26,
                "260 - 680 HP", "16 - 26 ATK", "12 - 20 DEF", "0.33 SPD",
                List.of(
                        "• Rugido de Batalla: Intimida a los rivales cercanos reduciendo su ataque.",
                        "• Salto Demoledor: Salto acrobático que estalla al caer con onda sísmica.",
                        "• Frenesí Sangriento: Aumenta velocidad de movimiento al detectar presas heridas."
                ),
                "Interrumpe su salto con ataques contundentes a distancia o proyectiles mágicos.",
                "Colmillos Bestiales, Cuero Feroz, Minerales de Hierro Mágico.",
                "Tribus guerreras que habitan los valles salvajes de la dimensión Isekai, leales solo a la ley del más fuerte."
        ));

        // 6. Soldado Hormiga (Beru Colony)
        var ant = new AntSoldierEntity(ModEntities.ANT_SOLDIER.get(), level);
        entries.add(new BestiaryEntry(
                "Soldado Hormiga", "ÉLITE", 0xFF70E8FF, "Quimera de Choque de la Colonia de Jeju", ant, 24,
                "200 - 550 HP", "15 - 25 ATK", "14 - 22 DEF", "0.36 SPD",
                List.of(
                        "• Mandíbulas Trituradoras: Perforación de armadura letal a corto alcance.",
                        "• Brazos de Guadaña: Cortes rápidos en tijera de alta frecuencia.",
                        "• Vuelo en Ráfaga: Aceleración alada de choque directo con empuje masivo."
                ),
                "Ataca sus alas para obligarlo a descender al suelo donde sus movimientos son más predecibles.",
                "Quitina Reforzada de Hormiga, Mandíbulas Venenosas, Esencia Quimérica.",
                "Subespecie de insecto mutante desarrollada para la invasión y el consumo indiscriminado de toda forma de vida."
        ));

        // 7. Ojo Vigilante del Abismo (Abyssal Beholder)
        var beholder = new AbyssalBeholderEntity(ModEntities.ABYSSAL_BEHOLDER.get(), level);
        entries.add(new BestiaryEntry(
                "Ojo del Abismo", "ABISAL", 0xFF9955FF, "Centinela Colosal del Vacío", beholder, 18,
                "350 - 900 HP", "18 - 30 ATK", "10 - 18 DEF", "0.38 SPD",
                List.of(
                        "• Rayo Anulador del Abismo: Haz de partículas violetas que cancela efectos positivos.",
                        "• Pulso de Distorsión: Onda sónica gravitatoria que empuja a los cazadores al aire.",
                        "• Vuelo Acrobático en Picada: Embestida aérea súbita de alta velocidad."
                ),
                "Usa arcos o magia a larga distancia mientras esquivas su rayo concentrado en línea recta.",
                "Ojo Concentrado del Vacío, Tentáculo Abisal, Cristal de Telequinesis.",
                "Entidades colosales flotantes nacidas de las grietas del espacio dimensional, vigilantes eternos de los portales más oscuros."
        ));

        // 8. Orco de Sangre
        var orc = new AbyssalOrcEntity(ModEntities.ABYSSAL_ORC.get(), level);
        entries.add(new BestiaryEntry(
                "Orco de Sangre", "ÉLITE", 0xFFFF6666, "Guerrero Devastador de las Mazmorras", orc, 24,
                "220 - 750 HP", "14 - 24 ATK", "10 - 18 DEF", "0.26 SPD",
                List.of(
                        "• Embestida Brutal: Salta y aplasta el suelo aturdiendo objetivos en área.",
                        "• Furia Berserker: Incrementa radicalmente su daño conforme pierde vida.",
                        "• Piel de Hierro: Gran resistencia al empuje y a los cortes físicos."
                ),
                "Aprovecha sus momentos de recuperación tras el salto de impacto para golpear por la espalda.",
                "Colmillos Sangrientos, Cuero Reforzado, Hacha Abisal de Élite.",
                "Guerreros mutados por el mana subterráneo del calabozo cuya sed de batalla solo se apaga con la victoria absoluta."
        ));

        // 9. Infantería Sombría
        var infantry = new ShadowInfantryEntity(ModEntities.SHADOW_INFANTRY.get(), level);
        entries.add(new BestiaryEntry(
                "Infantería Sombría", "SOMBRA", 0xFF8888FF, "Soldado Espectral del Ejército Oscuro", infantry, 26,
                "80 - 280 HP", "7 - 16 ATK", "6 - 14 DEF", "0.28 SPD",
                List.of(
                        "• Tajo Espectral: Cortes veloces imbuidos con energía residual de las sombras.",
                        "• Regeneración de Sombras: Recupera vitalidad si no recibe daño continuo.",
                        "• Lealtad Eterna: Si es extraído por el Monarca, se convierte en sirviente indestructible."
                ),
                "Ataques de fuego espiritual o cortes rápidos continuos evitan que active su regeneración pasiva.",
                "Fragmento de Alma Sombría, Acero Negro Imperial, Esencia Espectral.",
                "Espíritus de antiguos combatientes cuyos juramentos de batalla fueron atados al manto de las sombras por la eternidad."
        ));

        // 10. Nigromante del Abismo
        var necro = new AbyssalNecromancerEntity(ModEntities.ABYSSAL_NECROMANCER.get(), level);
        entries.add(new BestiaryEntry(
                "Nigromante del Abismo", "ABISAL", 0xFF50FA7B, "Maestro de Maldiciones y Nigromancia", necro, 24,
                "220 - 500 HP", "14 - 22 ATK", "8 - 14 DEF", "0.27 SPD",
                List.of(
                        "• Ráfaga de Almas: Proyectiles oscuros teledirigidos que infligen ceguera.",
                        "• Maldición Abisal: Niebla necrótica que aplica Wither, Lentitud y Debilidad.",
                        "• Alzamiento de Muertos: Resucita esqueletos blindados para protegerse de los ataques."
                ),
                "Focaliza el daño inmediatamente sobre el nigromante para que sus invocaciones se desvanezcan.",
                "Cráneo Necrótico, Polvo de Almas Abisales, Báculo Maldito.",
                "Ermitaños arcanos que dedicaron siglos al estudio prohibido de la muerte en las profundidades de los calabozos."
        ));

        // 11. Asesino Elfo de Hielo
        var assassin = new FrostElfAssassinEntity(ModEntities.FROST_ELF_ASSASSIN.get(), level);
        entries.add(new BestiaryEntry(
                "Asesino Elfo de Hielo", "ÉLITE", 0xFF70FFFF, "Sombra Ágil de la Tundra Gélida", assassin, 26,
                "180 - 380 HP", "16 - 25 ATK", "10 - 16 DEF", "0.35 SPD",
                List.of(
                        "• Doble Corte de Hielo: Golpea velozmente congelando la pantalla del jugador.",
                        "• Frost Dash: Deslizamiento instantáneo sobre ráfagas de escarcha.",
                        "• Desvanecimiento en Humo: Se vuelve invisible y se reposiciona al recibir daño."
                ),
                "Aplica efectos de fuego o daño en área cuando se camufle para forzar su reaparición.",
                "Dagas de Baruka (Ghost Moon), Cuero de Escarcha, Esencia Helada.",
                "Cazadores silenciosos nacidos en las cavernas congeladas de los calabozos de nieve, veloces como la ventisca blanca."
        ));

        // 12. Dragón Ancestral de Fuego
        var dragonFire = new AncientDragonEntity(ModEntities.ANCIENT_DRAGON.get(), level);
        dragonFire.setVariant(AncientDragonEntity.VARIANT_FIRE);
        entries.add(new BestiaryEntry(
                "Dragón de Fuego", "DRAGÓN", 0xFFFF4500, "Soberano Colosal del Fuego Primordial", dragonFire, 8,
                "800 - 3200 HP", "28 - 65 ATK", "20 - 32 DEF", "1.40 SPD",
                List.of(
                        "• Aliento Ígneo Cataclísmico: Torrente masivo de magma y fuego concentrado.",
                        "• Vuelo Majestuoso: Desplazamiento aéreo colosal de gran empuje por aleteo.",
                        "• Rugido de la Llama: Aturde y calcina a criaturas en un radio de 20 bloques.",
                        "• Montura Dracónica: Puede ser domesticado con corazones dracónicos para surcar los cielos."
                ),
                "Ataca desde el flanco o bajo su vientre cuando prepare el aliento frontal de magma.",
                "Escamas de Dragón de Fuego, Corazón Dracónico, Cuerno Carmesí, Garra de Dragón.",
                "Colosos alados antiguos que dominaron los cielos volcánicos mucho antes de la apertura de las primeras fallas dimensionales."
        ));

        // 13. Dragón Ancestral de Hielo
        var dragonIce = new AncientDragonEntity(ModEntities.ANCIENT_DRAGON.get(), level);
        dragonIce.setVariant(AncientDragonEntity.VARIANT_ICE);
        entries.add(new BestiaryEntry(
                "Dragón de Hielo", "DRAGÓN", 0xFF00E5FF, "Señor de la Ventisca Glacial Eterna", dragonIce, 8,
                "850 - 3400 HP", "26 - 60 ATK", "24 - 36 DEF", "1.35 SPD",
                List.of(
                        "• Aliento Glacial Absoluto: Haz de congelamiento que inmoviliza y congela la pantalla.",
                        "• Escamas de Diamante Escarchado: Reduce drásticamente el daño físico recibido.",
                        "• Nova de Escarcha: Explosión de carámbanos que ralentiza a todos los cazadores."
                ),
                "Usa armaduras cálidas o ataques de fuego para romper su armadura de escarcha.",
                "Escamas de Hielo Eterno, Hielo Glacial Dracónico, Orbe de Escarcha.",
                "Bestias titánicas nacidas en las cumbres más heladas de las dimensiones árticas."
        ));

        // 14. Dragón Ancestral del Rayo
        var dragonLightning = new AncientDragonEntity(ModEntities.ANCIENT_DRAGON.get(), level);
        dragonLightning.setVariant(AncientDragonEntity.VARIANT_LIGHTNING);
        entries.add(new BestiaryEntry(
                "Dragón del Rayo", "DRAGÓN", 0xFFFFD700, "Tormenta Viviente de Plasma Celeste", dragonLightning, 8,
                "750 - 3000 HP", "30 - 70 ATK", "18 - 28 DEF", "1.60 SPD",
                List.of(
                        "• Rayo de Plasma Celestial: Descarga fulminante de daño instantáneo a distancia.",
                        "• Vuelo Relámpago: Velocidad aérea superior con estela de partículas eléctricas.",
                        "• Tempestad Estática: Parálisis electromagnética en área continua."
                ),
                "Equipa pararrayos o mantente en movimiento constante para esquivar sus proyectiles.",
                "Escamas de Trueno, Cuerno Electrificado, Núcleo del Rayo Primordial.",
                "Encarnación dracónica de las tormentas celestes, capaz de viajar como un relámpago."
        ));

        // 15. Dragón Ancestral de las Sombras
        var dragonShadow = new AncientDragonEntity(ModEntities.ANCIENT_DRAGON.get(), level);
        dragonShadow.setVariant(AncientDragonEntity.VARIANT_SHADOW);
        entries.add(new BestiaryEntry(
                "Dragón Sombrío", "DRAGÓN", 0xFF9933FF, "Pesadilla Alada del Reino de las Sombras", dragonShadow, 8,
                "820 - 3300 HP", "28 - 68 ATK", "22 - 30 DEF", "1.45 SPD",
                List.of(
                        "• Aliento Abisal de Sombras: Vapor oscuro que ciega y drena la fuerza del rival.",
                        "• Fusión con la Oscuridad: Se vuelve translúcido esquivando ataques en vuelo nocturno.",
                        "• Mandíbula de la Condenación: Mordisco aplastante que inflige Wither severo."
                ),
                "Usa fuentes intensas de luz y proyectiles arcanos consagrados.",
                "Escamas Sombrías, Fragmento de Vacío, Huevo Dracónico Oscuro.",
                "Criaturas que habitan la frontera entre la vida y la muerte, leales al aliento del abismo."
        ));

        // 16. Dragón Ancestral de Bosque
        var dragonForest = new AncientDragonEntity(ModEntities.ANCIENT_DRAGON.get(), level);
        dragonForest.setVariant(AncientDragonEntity.VARIANT_FOREST);
        entries.add(new BestiaryEntry(
                "Dragón de Bosque", "DRAGÓN", 0xFF50FA7B, "Devorador Esmeralda de las Selvas Arcanas", dragonForest, 8,
                "880 - 3500 HP", "25 - 58 ATK", "26 - 38 DEF", "1.30 SPD",
                List.of(
                        "• Aliento Tóxico Corrosivo: Nube ácida que derrite equipamiento y envenena.",
                        "• Regeneración de la Selva: Recupera salud rápidamente en contacto con vegetación.",
                        "• Espinas Venenosas: Devuelve parte del daño físico recibido en forma de ponzoña."
                ),
                "Mantén el combate lejos de follaje denso y consume leche o antídotos.",
                "Escamas de Jade Tóxico, Glándula de Veneno Puro, Cuero Ancestral.",
                "Titán reptiliano custodiado por los bosques primigenios que purga cualquier intruso."
        ));

        // 17. Dragón Celestial Divino (Murim)
        var celestialDragon = new CelestialDragonEntity(ModEntities.CELESTIAL_DRAGON.get(), level);
        entries.add(new BestiaryEntry(
                "Dragón Celestial", "DIVINO", 0xFFFFE066, "Dragón Oriental Sagrado de los Nueve Cielos (Murim)", celestialDragon, 8,
                "1200 - 5000 HP", "35 - 85 ATK", "28 - 42 DEF", "1.50 SPD",
                List.of(
                        "• Aliento Celestial del Dragón: Rayo sagrado dorado que purifica o desintegra la materia.",
                        "• Ascenso Taoísta: Vuelo ondulante sinuoso como el viento de las montañas sagradas.",
                        "• Bendición del Dao: Proyecta aura que debilita el Qi demoníaco enemigo."
                ),
                "Usa técnicas de espada de alta maestría y evita el choque frontal con su cabeza.",
                "Perla del Dragón Celestial, Barbas Sagradas, Qi Primordial Concentrado.",
                "La máxima deidad bestial de la dimensión Murim. Representa la armonía perfecta del Yin y el Yang en el cielo supremo."
        ));

        // 18. Dragón Astral del Vacío
        var astralDragon = new AstralDragonEntity(ModEntities.ASTRAL_DRAGON.get(), level);
        entries.add(new BestiaryEntry(
                "Dragón Astral", "CÓSMICO", 0xFFBD93F9, "Devorador de Estrellas del Vacío Dimensional", astralDragon, 8,
                "1500 - 6000 HP", "40 - 95 ATK", "30 - 45 DEF", "1.55 SPD",
                List.of(
                        "• Aliento Astral del Vacío: Haz estelar de distorsión espacio-temporal que ignora armadura.",
                        "• Creciente Astral: Proyectiles lunares de energía pura en arco.",
                        "• Gravedad Cero: Altera las órbitas y levita a los cazadores en las proximidades."
                ),
                "Ataca inmediatamente tras su disparo de luna creciente mientras recarga su núcleo cósmico.",
                "Fragmento Estelar, Escamas Celestiales del Vacío, Corazón de Supernova.",
                "Entidad legendaria que navega las grietas entre dimensiones alimentándose del mana cósmico."
        ));

        // 19. Lobo Temible de las Sombras
        var shadowWolf = new ShadowDirewolfEntity(ModEntities.SHADOW_DIREWOLF.get(), level);
        entries.add(new BestiaryEntry(
                "Lobo de Sombras", "SOMBRA", 0xFF6272A4, "Depredador Espectral Nocturno (Solo Leveling)", shadowWolf, 22,
                "160 - 420 HP", "14 - 24 ATK", "10 - 18 DEF", "0.42 SPD",
                List.of(
                        "• Caza en Manada: Aumenta su daño y velocidad por cada lobo sombrío cercano.",
                        "• Salto Eviscerador: Salto rápido al cuello que derriba al objetivo.",
                        "• Aullido Espectral: Alerta a los enemigos cercanos y desorienta la brújula."
                ),
                "Sepáralos de la manada antes de enfrentarlos para anular sus bonificadores de grupo.",
                "Pelaje de Sombras, Colmillo Espectral, Esencia Canina de Sombra.",
                "Caninos colosales nacidos de las sombras de los bosques encantados de la mazmorra."
        ));

        // 20. Tigre Blanco del Trueno (Baekho)
        var thunderTiger = new WhiteThunderTigerEntity(ModEntities.WHITE_THUNDER_TIGER.get(), level);
        entries.add(new BestiaryEntry(
                "Tigre Blanco", "MURIM", 0xFFF8F8F2, "Guardián Feroz de la Montaña Blanca (Baekho)", thunderTiger, 20,
                "320 - 850 HP", "18 - 32 ATK", "14 - 22 DEF", "0.40 SPD",
                List.of(
                        "• Garra de Relámpago: Zarpazo infundido con electricidad que encadena daño.",
                        "• Rugido del Trueno: Onda de choque sónica que aleja y ensordece al enemigo.",
                        "• Salto de Baekho: Abalanzamiento ágil que esquiva proyectiles frontales."
                ),
                "Mantén un escudo en alto durante su embestida para romper su ritmo de salto.",
                "Piel de Tigre Blanco, Garra de Baekho, Cristal de Trueno Sagrado.",
                "Una de las cuatro bestias auspiciosas del continente marcial, símbolo de fuerza indomable y rectitud marcial."
        ));

        // 21. Zorro Espiritual de Nueve Colas
        var spiritFox = new SpiritFoxEntity(ModEntities.SPIRIT_FOX.get(), level);
        entries.add(new BestiaryEntry(
                "Zorro Espiritual", "ESPÍRITU", 0xFFFF79C6, "Hechicero Astral de los Nueve Orbes (Gumiho)", spiritFox, 24,
                "200 - 550 HP", "16 - 28 ATK", "12 - 20 DEF", "0.38 SPD",
                List.of(
                        "• Fuego Fatuo Astral: Esferas de llamas espirituales flotantes teledirigidas.",
                        "• Encanto Espiritual: Ilusión óptica que confunde la visión del objetivo.",
                        "• Teletransportación de Niebla: Se disuelve en pétalos al estar acorralado."
                ),
                "Ataca desde la distancia y no mires directamente sus orbes de ilusión luminosa.",
                "Pelaje de Nueve Colas, Orbe Espiritual Zorro, Esencia de Jade Místico.",
                "Espíritu milenario de gran inteligencia capaz de dominar el maná espiritual con elegancia."
        ));

        // 22. Grulla de las Nubes
        var cloudCrane = new CloudCraneEntity(ModEntities.CLOUD_CRANE.get(), level);
        entries.add(new BestiaryEntry(
                "Grulla de Nubes", "TAOÍSTA", 0xFF8BE9FD, "Mensajero Alado de los Inmortales (Murim)", cloudCrane, 22,
                "140 - 380 HP", "12 - 22 ATK", "8 - 16 DEF", "0.45 SPD",
                List.of(
                        "• Ráfaga de Viento Nebuloso: Torbellino que eleva por los aires a los atacantes.",
                        "• Vuelo Celestial: Gran agilidad aérea con maniobras evasivas verticales.",
                        "• Canto Curativo: Regenera suavemente a los aliados pacíficos cercanos."
                ),
                "Atráela al suelo usando redes o arcos de precisión antes de que cree remolinos.",
                "Pluma de Grulla Celestial, Rocío de Nube Inmortal, Jade Blanco.",
                "Aves sagradas veneradas por los maestros inmortales por su pureza y gracia incorruptible."
        ));

        // 23. Hada de Maná
        var manaPixie = new ManaPixieEntity(ModEntities.MANA_PIXIE.get(), level);
        entries.add(new BestiaryEntry(
                "Hada de Maná", "FÉRICA", 0xFFFFB86C, "Espíritu Resonante de Maná Puro (Isekai)", manaPixie, 32,
                "60 - 180 HP", "8 - 15 ATK", "6 - 12 DEF", "0.40 SPD",
                List.of(
                        "• Destello Cegador de Maná: Pulso de luz que causa ceguera momentánea.",
                        "• Polvo de Hada: Otorga levitación y regeneración a cazadores amistosos.",
                        "• Dardo de Cristal de Maná: Proyectil diminuto pero de penetración perforante."
                ),
                "Captúrala o trátala con respeto; no responde agresivamente a menos que sea provocada.",
                "Polvo de Hada de Maná, Frasco de Rocío Élfico, Fragmento Resonante.",
                "Criaturas etéreas que pululan en bosques de alta densidad de maná en la dimensión Isekai."
        ));

        // 24. Mandrágora Chillona
        var mandrake = new ScreamingMandrakeEntity(ModEntities.SCREAMING_MANDRAKE.get(), level);
        entries.add(new BestiaryEntry(
                "Mandrágora", "FLORA", 0xFF50FA7B, "Raíz Chocante de la Tierra Prohibida", mandrake, 28,
                "120 - 320 HP", "10 - 20 ATK", "12 - 18 DEF", "0.22 SPD",
                List.of(
                        "• Grito Ensordecedor: Onda de ultrasonido que causa náuseas, lentitud y daño auditivo.",
                        "• Enraizamiento Rápido: Se sumerge en la tierra para reaparecer a espaldas del enemigo.",
                        "• Esporas Alucinógenas: Nube verde que invierte controles sensoriales temporales."
                ),
                "Usa tapones auditivos o elimínala a larga distancia antes de que comience a gritar.",
                "Raíz de Mandrágora, Savia Chillona, Tallo Alquímico Raro.",
                "Planta semi-vegetal temida por herboristas; su alarido al ser desenterrada es legendario."
        ));

        // 25. Slime de Élite Isekai
        var isekaiSlime = new IsekaiSlimeEntity(ModEntities.ISEKAI_SLIME.get(), level);
        entries.add(new BestiaryEntry(
                "Slime Isekai", "ISEKAI", 0xFF8BE9FD, "Gelatina Consciente Devora-Materiales", isekaiSlime, 26,
                "100 - 350 HP", "8 - 18 ATK", "16 - 25 DEF", "0.25 SPD",
                List.of(
                        "• Absorción de Impacto: Inmunidad casi total al daño por caída y empuje físico.",
                        "• Salto Elástico Triturador: Aplastamiento desde gran altura.",
                        "• División Celular: Se subdivide en clones menores al recibir daño crítico."
                ),
                "Utiliza magia elemental de fuego o congelamiento para neutralizar su elasticidad gelatinosa.",
                "Gelatina de Slime Isekai, Esencia de Absorción, Núcleo Viscoso.",
                "El monstruo más representativo del mundo Isekai, subestimado por novatos hasta su división."
        ));

        // 26. Goblin Explorador
        var goblinScout = new GoblinScoutEntity(ModEntities.GOBLIN_SCOUT.get(), level);
        entries.add(new BestiaryEntry(
                "Goblin Explorador", "GOBLIN", 0xFF50FA7B, "Rastreador Ágil de la Vanguardia", goblinScout, 28,
                "90 - 240 HP", "10 - 18 ATK", "6 - 12 DEF", "0.36 SPD",
                List.of(
                        "• Flecha Envenenada: Disparos de cerbatana con veneno corrosivo.",
                        "• Alarma de Cuerno: Llama a gorriones y guerreros goblin cercanos.",
                        "• Evasión Rápida: Saltos hacia atrás ante aproximaciones cuerpo a cuerpo."
                ),
                "Cancela su llamada de cuerno con un golpe aturdidor antes de que atraiga refuerzos.",
                "Daga Goblin Rústica, Cuero Maloliente, Monedas de Cobre Manchadas.",
                "Ojos y oídos de las tribus goblin en los calabozos de rango D y C."
        ));

        // 27. Goblin Guerrero
        var goblinWarrior = new GoblinWarriorEntity(ModEntities.GOBLIN_WARRIOR.get(), level);
        entries.add(new BestiaryEntry(
                "Goblin Guerrero", "GOBLIN", 0xFFFFB86C, "Incursor Blindado del Calabozo", goblinWarrior, 26,
                "140 - 380 HP", "14 - 24 ATK", "12 - 20 DEF", "0.30 SPD",
                List.of(
                        "• Tajo Rabioso: Ataques feroces consecutivos con espada de chatarra.",
                        "• Bloqueo con Escudo: Detiene flechas y golpes frontales ocasionales.",
                        "• Carga Desesperada: Acometida veloz cuando su vida está por debajo del 30%."
                ),
                "Rompe su escudo con hachas o ataques pesados por arriba.",
                "Maza de Hierro Goblin, Escudo de Madera Reforzado, Dientes Afilados.",
                "Guerreros equipados con armaduras oxidadas recolectadas de aventureros caídos."
        ));

        // 28. Goblin Chamán
        var goblinShaman = new GoblinShamanEntity(ModEntities.GOBLIN_SHAMAN.get(), level);
        entries.add(new BestiaryEntry(
                "Goblin Chamán", "GOBLIN", 0xFFBD93F9, "Brujo del Tótem y Fuego Tribal", goblinShaman, 26,
                "160 - 420 HP", "16 - 26 ATK", "8 - 14 DEF", "0.28 SPD",
                List.of(
                        "• Bola de Fuego Tribal: Proyectil incendiario explosivo de área media.",
                        "• Tótem de Sanación: Planta un poste que regenera la salud de los goblins aliados.",
                        "• Maldición de Debilidad: Reduce el daño físico de los cazadores en el radio."
                ),
                "Destruye su tótem en cuanto lo coloque y priorízalo antes que a los guerreros.",
                "Báculo de Tótem Goblin, Plumas de Ritual, Piedra Mágica Tribal.",
                "Líder espiritual y conjurador elemental reverenciado por las tribus subterráneas."
        ));

        // 29. Jiangshi (Zombi Saltador Murim)
        var jiangshi = new JiangshiEntity(ModEntities.JIANGSHI.get(), level);
        entries.add(new BestiaryEntry(
                "Jiangshi", "MURIM", 0xFF8BE9FD, "Cadáver Reanimado por Talismán Taoísta (Murim)", jiangshi, 24,
                "280 - 750 HP", "18 - 30 ATK", "18 - 28 DEF", "0.32 SPD",
                List.of(
                        "• Salto Rígido Devastador: Desplazamiento a saltos altos con caída pesada.",
                        "• Zarpas de Rigor Mortis: Golpes rígidos que causan parálisis temporal.",
                        "• Resistencia a Cortes: Su carne momificada absorbe casi por completo flechas y cortes leves."
                ),
                "Arranca o quema su talismán frontal con fuego o golpes contundentes de maza.",
                "Talismán Taoísta Amarillo, Seda Imperial Vieja, Polvo de Momia Murim.",
                "Cuerpos de maestros marciales reanimados mediante nigromancia oscura de la dinastía antigua."
        ));

        // 30. Cultivador Demoníaco
        var demonicCultivator = new DemonicCultivatorEntity(ModEntities.DEMONIC_CULTIVATOR.get(), level);
        entries.add(new BestiaryEntry(
                "Cultivador Demoníaco", "MURIM", 0xFFFF5555, "Fanático del Culto Celestial Demoníaco", demonicCultivator, 24,
                "350 - 950 HP", "22 - 38 ATK", "16 - 26 DEF", "0.36 SPD",
                List.of(
                        "• Espada de Qi Demoníaco: Ondas cortantes carmesí a distancia de alta frecuencia.",
                        "• Paso Celestial Sangriento: Teletransportación corta entre las sombras del cazador.",
                        "• Absorción de Qi: Roba mana o vitalidad al impactar combos continuos."
                ),
                "Interrumpe su canalización de Qi de espada con armas arrojadizas pesadas.",
                "Manual de Cultivo Demoníaco, Espada de Acero Negro Murim, Píldora de Qi Sangriento.",
                "Marcialistas renegados que vendieron su alma a las artes oscuras en busca de inmortalidad y poder absoluto."
        ));

        // 31. Discípulo del Monte Hua
        var mountHua = new MountHuaDiscipleEntity(ModEntities.MOUNT_HUA_DISCIPLE.get(), level);
        entries.add(new BestiaryEntry(
                "Discípulo Mount Hua", "MURIM", 0xFFFF79C6, "Espadachín de la Espada de Flor de Ciruelo", mountHua, 24,
                "300 - 800 HP", "20 - 34 ATK", "14 - 24 DEF", "0.38 SPD",
                List.of(
                        "• Danza de Flor de Ciruelo: Ráfaga estética de cortes con pétalos de Qi deslumbrantes.",
                        "• Paso de la Montaña Ilusoria: Esquivas fluidas que dejan imágenes residuales.",
                        "• Empuje de Espada Recta: Estocada relampagueante que perfora guardias."
                ),
                "No permitas que enlace su secuencia de danza; usa contraataques tras su estocada.",
                "Espada de Flor de Ciruelo, Túnica Blanca y Rosa, Vino de Ciruela Añejo.",
                "Hábiles espadachines de la legendaria secta Mount Hua que defienden el honor de su templo con espada poética."
        ));

        // 32. Discípulo de la Secta de Sangre
        var bloodDisciple = new BloodSectDiscipleEntity(ModEntities.BLOOD_SECT_DISCIPLE.get(), level);
        entries.add(new BestiaryEntry(
                "Discípulo de Sangre", "MURIM", 0xFFFF4444, "Iniciado del Mar de Sangre Sangriento", bloodDisciple, 24,
                "260 - 720 HP", "18 - 30 ATK", "12 - 20 DEF", "0.35 SPD",
                List.of(
                        "• Garras de Sangre Hirviente: Desgarraduras que aumentan el sangrado del cazador.",
                        "• Escudo de Qi Sangriento: Barrera esférica que absorbe daño convirtiéndolo en curación.",
                        "• Estallido de Sangre: Sacrifica un porcentaje de su vida para detonar un pulso carmesí."
                ),
                "Castiga su escudo con ataques masivos concentrados para romper su barrera antes de que cure.",
                "Dagas de Sangre, Perla Escarlata, Elixir de Refinamiento de Médula.",
                "Miembros de la temida Secta de Sangre cuya doctrina enseña que la fuerza suprema emana del flujo vital ajeno."
        ));

        // 33. Bestia Blindada de Jade
        var jadeBeast = new JadeArmoredBeastEntity(ModEntities.JADE_ARMORED_BEAST.get(), level);
        entries.add(new BestiaryEntry(
                "Bestia de Jade", "BESTIA", 0xFF50FA7B, "Coloso Acorazado de Jade Imperial (Murim)", jadeBeast, 20,
                "500 - 1500 HP", "24 - 45 ATK", "30 - 48 DEF", "0.22 SPD",
                List.of(
                        "• Caparazón de Jade Reforzado: Reduce el daño físico en un 60% y es inmune a proyectiles ligeros.",
                        "• Terremoto de Jade: Pisotón que agrieta el piso generando esquirlas cortantes.",
                        "• Carga del Rinodonte: Arremetida frontal imparable que derriba bloques ligeros."
                ),
                "Ataca sus articulaciones traseras y usa daño mágico o explosivo que ignore su coraza.",
                "Placas de Jade Puro, Núcleo Terrestre de Jade, Cuerno de Bestia Imperial.",
                "Criaturas ancestrales que bebieron del Qi mineral de las vetas de jade de las montañas sagradas durante eones."
        ));

        // 34. Patriarca de Sangre (Jefe)
        var bloodPatriarch = new BloodPatriarchBossEntity(ModEntities.BLOOD_PATRIARCH_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "Patriarca de Sangre", "JEFE MURIM", 0xFFFF0033, "Líder Supremo de la Secta de Sangre y Señor del Qi Carmesí", bloodPatriarch, 22,
                "1800 - 8000 HP", "35 - 80 ATK", "24 - 38 DEF", "0.38 SPD",
                List.of(
                        "• Mar de Sangre Hirviente: Inunda el campo de batalla con charcos de Qi sangriento que asfixian.",
                        "• Tótem del Juicio de Sangre: Invoca pilares totémicos que canalizan rayos de drenaje vital.",
                        "• Juicio de las Mil Cuchillas de Sangre: Ráfaga omnidireccional de proyectiles punzantes.",
                        "• Resurrección Sangrienta: Recupera un 25% de vida si consume la esencia de discípulos caídos."
                ),
                "Destruye sus tótems de sangre de inmediato para impedir que drene tu vida y mantén distancia de los charcos.",
                "Báculo del Patriarca de Sangre, Manual del Mar Carmesí, Corazón de Sangre Primordial.",
                "Un monstruoso cultivador que alcanzó la inmortalidad corrompida sacrificando sectas enteras en la era dorada del Murim."
        ));

        // 35. Treant Ancestral de Yggdrasil (Jefe)
        var yggdrasilTreant = new YggdrasilTreantBossEntity(ModEntities.YGGDRASIL_TREANT_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "Treant Yggdrasil", "JEFE ISEKAI", 0xFF50FA7B, "Guardián de Madera Divina del Árbol del Mundo", yggdrasilTreant, 18,
                "2000 - 9000 HP", "30 - 75 ATK", "32 - 46 DEF", "0.20 SPD",
                List.of(
                        "• Raíces Enredaderas de Yggdrasil: Brotan del suelo aprisionando y aplastando a los cazadores.",
                        "• Lluvia de Bellotas Explosivas: Bombardeo vegetal con fragmentación de astillas.",
                        "• Fotosíntesis Divina: Regeneración pasiva masiva mientras esté expuesto a la luz o maná del bosque.",
                        "• Estallido de Polen Sagrado: Nube aromática que induce sueño y reduce la armadura a la mitad."
                ),
                "El fuego concentrado cancela su regeneración de savia. Ataca su núcleo brillante expuesto en el tronco.",
                "Madera Sagrada de Yggdrasil, Semilla de Vida Eterna, Núcleo del Bosque Mágico.",
                "Manifestación viviente de las raíces más profundas del Árbol Cósmico de Yggdrasil, guardián de los secretos arcanos."
        ));

        // 36. Guardián Colosal de Jade (Jefe)
        var jadeGuardian = new JadeGuardianBossEntity(ModEntities.JADE_GUARDIAN_BOSS.get(), level);
        entries.add(new BestiaryEntry(
                "Guardián de Jade", "JEFE MURIM", 0xFF50FA7B, "Golem Titánico de la Tumba del Primer Emperador", jadeGuardian, 18,
                "2200 - 9500 HP", "38 - 85 ATK", "35 - 50 DEF", "0.24 SPD",
                List.of(
                        "• Espada Pesada de Jade Milenario: Mandoble gigantesco que crea ondas sísmicas de 15 bloques.",
                        "• Barrera de Jade Resonante: Escudo inmune a daño que solo se rompe al derrotar sus centinelas.",
                        "• Ojo de Jade Láser: Rayo ocular de Qi verde continuo que incinera el terreno.",
                        "• Pisotón Terrestre Colosal: Levanta losas de piedra del suelo como proyectiles arrojadizos."
                ),
                "Esquiva rodando bajo su entrepierna cuando alce la gran espada de jade y ataca los sellos en su espalda.",
                "Gran Espada de Jade Imperial, Núcleo de Golem Ancestral, Lingotes de Jade Refinado.",
                "Construido por los más grandes alquimistas del imperio oriental para proteger el sepulcro de los secretos del Dao."
        ));

        // 37. Rey Demonio Elemental
        var elementalKing = new ElementalDemonKingBossEntity(ModEntities.ELEMENTAL_DEMON_KING.get(), level);
        entries.add(new BestiaryEntry(
                "Rey Elemental", "JEFE SUPREMO", 0xFFFF5555, "Soberano de los Cuatro Elementos del Caos", elementalKing, 20,
                "2500 - 10000 HP", "40 - 90 ATK", "30 - 45 DEF", "0.34 SPD",
                List.of(
                        "• Fusión Elemental Cuádruple: Alterna fuego, hielo, rayo y tierra con efectos devastadores.",
                        "• Vórtice del Caos: Tifón ígneo con descargas eléctricas que atrae a todos los jugadores.",
                        "• Armadura de Plasma: Refleja proyectiles mágicos y absorbe explosiones.",
                        "• Ira del Rey Demonio (Fase Final): Desata un apocalipsis de meteoritos en toda la sala."
                ),
                "Cambia tu equipamiento para contrarrestar el elemento activo que esté canalizando en cada momento.",
                "Corona del Rey Demonio, Núcleo Cuádruple Elemental, Manto del Caos Supremo.",
                "Un gobernante del plano abisal que sometió los espíritus elementales puros para moldear un imperio de caos infinito."
        ));

        // 38. Señor Elemental
        var elementalLord = new ElementalLordBossEntity(ModEntities.ELEMENTAL_LORD.get(), level);
        entries.add(new BestiaryEntry(
                "Señor Elemental", "ÉLITE", 0xFF8BE9FD, "Avatar Supremo de la Naturaleza Primigenia", elementalLord, 22,
                "600 - 1800 HP", "25 - 50 ATK", "20 - 32 DEF", "0.32 SPD",
                List.of(
                        "• Proyectiles de Energía Pura: Ráfaga continua de esferas arcanas.",
                        "• Escudo de Viento Huracanado: Repele flechas y proyectiles físicos convencionales.",
                        "• Nova Terrestre: Estallido de esquirlas de roca que aturde en área circular."
                ),
                "Usa armas imbuidas con energía del vacío para penetrar sus auras de viento.",
                "Esencia Primordial Elemental, Cristal de Resonancia Arcana, Polvo de Tormenta.",
                "Manifestaciones vivientes de las fuerzas elementales que rigen el equilibrio del mundo material."
        ));

        // 39. Dokkaebi del Canal Estelar
        var dokkaebi = new DokkaebiEntity(ModEntities.DOKKAEBI.get(), level);
        entries.add(new BestiaryEntry(
                "Dokkaebi", "SISTEMA", 0xFFFFB86C, "Transmisor y Administrador de Escenarios (Omniscient Reader)", dokkaebi, 26,
                "500 - 1200 HP", "12 - 25 ATK", "15 - 25 DEF", "0.35 SPD",
                List.of(
                        "• Apertura de Canal: Conecta la sala con las Constelaciones del cielo estelar.",
                        "• Monedas del Escenario: Premia a cazadores que realizan hazañas imposibles.",
                        "• Barrera de Transmisión: Puede repeler ataques deshonestos o agresiones imprevistas."
                ),
                "No lo ataques; cumple sus escenarios y desafíos para recibir increíbles donaciones de Constelaciones.",
                "Monedas del Canal Estelar, Cuerno de Dokkaebi, Contrato del Escenario.",
                "Pequeñas criaturas aladas con un único cuerno que gestionan los escenarios para el deleite de las Constelaciones en el cielo nocturno."
        ));

        // 40. Vaca de Maná
        var manaCow = new ManaCowEntity(ModEntities.MANA_COW.get(), level);
        entries.add(new BestiaryEntry(
                "Vaca de Maná", "CRIATURA", 0xFF8BE9FD, "Bovino Mágico de Leche Enriquecida con Qi", manaCow, 24,
                "40 - 100 HP", "0 ATK", "6 - 12 DEF", "0.20 SPD",
                List.of(
                        "• Leche de Maná Concentrada: Al ser ordeñada otorga leche mágica que restaura mana y purga venenos.",
                        "• Pasto Resonante: Estimula el crecimiento de flores mágicas a su paso.",
                        "• Docilidad Pacífica: Jamás ataca y se reproduce con trigo imbuido en maná."
                ),
                "Críala en tu base o granja mística para obtener suministros constantes de consumibles de maná.",
                "Carne de Maná Suculenta, Cuero Mágico Suave, Balde de Leche de Maná.",
                "Ganado adaptado a las atmósferas ricas en éter de los valles pacíficos entre mazmorras."
        ));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Evita desenfoque vanilla
    }

    public float getGuiScale() {
        float scaleX = (float) this.width / 500.0F;
        float scaleY = (float) this.height / 290.0F;
        float scale = Math.min(scaleX, scaleY);
        return Math.min(1.0F, Math.max(0.35F, scale));
    }

    public double transformMouseX(double mouseX) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseX;
        double cx = this.width / 2.0;
        return (mouseX - cx) / scale + cx;
    }

    public double transformMouseY(double mouseY) {
        float scale = getGuiScale();
        if (scale >= 1.0F) return mouseY;
        double cy = this.height / 2.0;
        return (mouseY - cy) / scale + cy;
    }

    private void enableScissorScaled(GuiGraphics g, int minX, int minY, int maxX, int maxY) {
        float scale = getGuiScale();
        if (scale >= 1.0F) {
            g.enableScissor(minX, minY, maxX, maxY);
        } else {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            int sMinX = (int) Math.floor((minX - cx) * scale + cx);
            int sMinY = (int) Math.floor((minY - cy) * scale + cy);
            int sMaxX = (int) Math.ceil((maxX - cx) * scale + cx);
            int sMaxY = (int) Math.ceil((maxY - cy) * scale + cy);
            g.enableScissor(sMinX, sMinY, sMaxX, sMaxY);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Fondo translúcido con gradiente de alta definición
        g.fillGradient(0, 0, this.width, this.height, 0x9A050D14, 0xBA0A1620);

        float scale = getGuiScale();
        int mX = (int) Math.round(transformMouseX(mouseX));
        int mY = (int) Math.round(transformMouseY(mouseY));

        g.pose().pushPose();
        if (scale < 1.0F) {
            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            g.pose().translate(cx, cy, 0.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.pose().translate(-cx, -cy, 0.0F);
        }

        int panelW = 480;
        int panelH = 270;
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2;

        // Borde exterior cibernético holográfico
        g.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, 0xFF1D556C);
        g.fill(left - 1, top - 1, left + panelW + 1, top + panelH + 1, 0xFF39B9D5);
        g.fill(left, top, left + panelW, top + panelH, 0xFA0B161F);

        // Cabecera superior con estilo de archivo del sistema
        g.fill(left, top, left + panelW, top + 26, 0xFF122430);
        g.fill(left, top + 25, left + panelW, top + 26, 0xFF39B9D5);

        // Acentos de esquinas holográficas
        int cornerLen = 12;
        g.fill(left, top, left + cornerLen, top + 2, 0xFF70E8FF);
        g.fill(left, top, left + 2, top + cornerLen, 0xFF70E8FF);
        g.fill(left + panelW - cornerLen, top, left + panelW, top + 2, 0xFF70E8FF);
        g.fill(left + panelW - 2, top, left + panelW, top + cornerLen, 0xFF70E8FF);

        g.drawString(font, Component.translatable("awakening_system.screen.bestiary.header"), left + 14, top + 9, 0xFF70E8FF, false);

        // Botón [X] Salir
        boolean exitHovered = mX >= left + panelW - 32 && mX <= left + panelW - 10 && mY >= top + 6 && mY <= top + 20;
        g.fill(left + panelW - 32, top + 6, left + panelW - 10, top + 20, exitHovered ? 0xFF992222 : 0xFF203545);
        g.fill(left + panelW - 31, top + 7, left + panelW - 11, top + 19, exitHovered ? 0xFFCC3333 : 0xFF14222D);
        g.drawCenteredString(font, "§f✖", left + panelW - 21, top + 8, exitHovered ? 0xFFFFFFFF : 0xFF8AAAB8);

        // Estructura de 3 Paneles:
        // 1. Panel Izquierdo: Lista de Mobs con Categoría (width 135)
        // 2. Panel Central: Pedestal Holográfico 3D (width 125)
        // 3. Panel Derecho: Dossier Táctico, Estadísticas y Lore (width 200)

        int listX = left + 8;
        int listY = top + 32;
        int listW = 135;
        int listH = panelH - 40;

        renderMonsterList(g, listX, listY, listW, listH, mX, mY);

        if (!entries.isEmpty() && selectedIndex >= 0 && selectedIndex < entries.size()) {
            BestiaryEntry cur = entries.get(selectedIndex);

            // Panel Central: 3D Hologram Pedestal
            int centerBoxX = listX + listW + 8;
            int centerBoxY = top + 32;
            int centerBoxW = 125;
            int centerBoxH = panelH - 40;
            renderHoloPedestal(g, centerBoxX, centerBoxY, centerBoxW, centerBoxH, cur, mX, mY);

            // Panel Derecho: Hunter Dossier
            int dossierX = centerBoxX + centerBoxW + 8;
            int dossierY = top + 32;
            int dossierW = left + panelW - dossierX - 8;
            int dossierH = panelH - 40;
            renderDossier(g, dossierX, dossierY, dossierW, dossierH, cur);
        }

        g.pose().popPose();
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderMonsterList(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        g.fill(x, y, x + w, y + h, 0xFF0D1822);
        g.fill(x + w, y, x + w + 1, y + h, 0xFF1F485B);

        int itemH = 26;
        int maxVisible = h / itemH;
        int maxScroll = Math.max(0, entries.size() - maxVisible);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        for (int i = 0; i < maxVisible && (i + scrollOffset) < entries.size(); i++) {
            int idx = i + scrollOffset;
            BestiaryEntry entry = entries.get(idx);
            int ey = y + 4 + i * itemH;
            boolean isSel = (idx == selectedIndex);
            boolean hov = mouseX >= x + 2 && mouseX <= x + w - 2 && mouseY >= ey && mouseY <= ey + itemH - 2;

            int bgCol = isSel ? 0xFF1E465D : (hov ? 0xFF173445 : 0xFF122430);
            int borderCol = isSel ? 0xFF70E8FF : (hov ? 0xFF39B9D5 : 0xFF1D3E4F);

            g.fill(x + 2, ey, x + w - 2, ey + itemH - 2, borderCol);
            g.fill(x + 3, ey + 1, x + w - 3, ey + itemH - 3, bgCol);

            // Mini Badge de Categoría
            g.fill(x + 5, ey + 3, x + 7, ey + itemH - 5, entry.categoryColor);

            g.drawString(font, entry.name, x + 11, ey + 3, isSel ? 0xFFFFFFFF : 0xFFDDECF2, false);
            g.drawString(font, "§8[" + entry.category + "] §7" + entry.hp.split(" ")[0] + " HP", x + 11, ey + 13, 0xFF8AAAB8, false);
        }

        // Barra de Scroll si hay más elementos
        if (entries.size() > maxVisible) {
            int barX = x + w - 3;
            int trackH = h - 8;
            int thumbH = Math.max(16, trackH * maxVisible / entries.size());
            int thumbY = y + 4 + (trackH - thumbH) * scrollOffset / maxScroll;
            g.fill(barX, y + 4, barX + 2, y + 4 + trackH, 0xFF142735);
            g.fill(barX, thumbY, barX + 2, thumbY + thumbH, 0xFF39B9D5);
        }
    }

    private void renderHoloPedestal(GuiGraphics g, int x, int y, int w, int h, BestiaryEntry cur, int mouseX, int mouseY) {
        // Marco de contención holográfico
        g.fill(x, y, x + w, y + h, 0xFF0B141C);
        g.fill(x, y, x + w, y + 1, 0xFF276A82);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF276A82);
        g.fill(x, y, x + 1, y + h, 0xFF276A82);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF276A82);

        // Rayos de retícula cibernética en el fondo del pedestal
        int gridSpacing = 16;
        for (int gy = y + 10; gy < y + h - 20; gy += gridSpacing) {
            g.fill(x + 8, gy, x + w - 8, gy + 1, 0x1870E8FF);
        }

        // Anillos holográficos concéntricos en la base
        int pedCenterX = x + w / 2;
        int pedCenterY = y + h - 26;
        g.fill(pedCenterX - 38, pedCenterY, pedCenterX + 38, pedCenterY + 4, 0x501D556C);
        g.fill(pedCenterX - 30, pedCenterY + 1, pedCenterX + 30, pedCenterY + 3, 0x8039B9D5);
        g.fill(pedCenterX - 18, pedCenterY + 1, pedCenterX + 18, pedCenterY + 3, 0xC070E8FF);

        // Indicador de rotación / interactividad
        g.drawCenteredString(font, "§8[ 3D PROJECTION ]", pedCenterX, y + 6, 0xFF5D8496);

        // Renderizado de la Entidad 3D Viva
        if (cur.entity != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g, x + 4, y + 14, x + w - 4, y + h - 20,
                    cur.scale, 0.0625F, (float) mouseX, (float) mouseY, cur.entity
            );
        }

        g.drawCenteredString(font, "§e" + cur.scale + "x SCALE", pedCenterX, y + h - 14, 0xFF8AAAB8);
    }

    private int calculateDossierContentHeight(BestiaryEntry cur, int maxW) {
        int h = 8; // margen superior inicial
        h += 11;   // nombre
        h += font.split(Component.literal("§b" + cur.title()), maxW).size() * 10;
        h += 3;    // separación
        h += 36;   // caja de estadísticas
        h += 10;   // cabecera habilidades
        for (String abil : cur.abilities()) {
            h += font.split(Component.literal(abil), maxW).size() * 10;
        }
        h += 12;   // cabecera estrategia + separación
        h += font.split(Component.literal("• " + cur.tactics()), maxW).size() * 10;
        h += 12;   // cabecera botín notable + separación
        h += font.split(Component.literal("• " + cur.drops()), maxW).size() * 10;
        h += 12;   // cabecera lore + separación
        h += font.split(Component.literal("§8\"" + cur.lore() + "\""), maxW).size() * 10;
        h += 16;   // margen inferior
        return h;
    }

    private void renderDossier(GuiGraphics g, int x, int y, int w, int h, BestiaryEntry cur) {
        g.fill(x, y, x + w, y + h, 0xFF0D1922);
        g.fill(x, y, x + w, y + 1, 0xFF1F485B);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF1F485B);
        g.fill(x, y, x + 1, y + h, 0xFF1F485B);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF1F485B);

        int maxW = w - 20;
        int contentH = calculateDossierContentHeight(cur, maxW);
        int maxScroll = Math.max(0, contentH - (h - 16));
        dossierScrollOffset = Math.max(0, Math.min(dossierScrollOffset, maxScroll));

        // Delimitar renderizado al interior del panel táctico
        enableScissorScaled(g, x + 2, y + 2, x + w - 2, y + h - 2);

        int cy = y + 8 - dossierScrollOffset;

        // Nombre y Título
        g.drawString(font, "§6§l" + cur.name().toUpperCase(), x + 8, cy, 0xFFFFD700, false);
        cy += 11;
        cy = drawWrapped(g, "§b" + cur.title(), x + 8, cy, maxW, 0xFF70E8FF);
        cy += 3;

        // Tarjetas de Estadísticas (Gauges en barra horizontal compacta)
        g.fill(x + 8, cy, x + w - 12, cy + 32, 0xFF11232E);
        g.fill(x + 8, cy, x + w - 12, cy + 1, 0xFF27556A);

        g.drawString(font, "§c❤ " + cur.hp(), x + 12, cy + 4, 0xFFFF7666, false);
        g.drawString(font, "§6⚔ " + cur.dmg(), x + 98, cy + 4, 0xFFFFB86C, false);
        g.drawString(font, "§9🛡 " + cur.armor(), x + 12, cy + 17, 0xFF70E8FF, false);
        g.drawString(font, "§a⚡ " + cur.speed(), x + 98, cy + 17, 0xFF50FA7B, false);
        cy += 36;

        // Habilidades y Rasgos
        g.drawString(font, Component.translatable("awakening_system.screen.bestiary.section.abilities"), x + 8, cy, 0xFFFFD866, false);
        cy += 10;
        for (String abil : cur.abilities()) {
            cy = drawWrapped(g, abil, x + 8, cy, maxW, 0xFFDDECF2);
        }

        // Táctica y Debilidades
        cy += 2;
        g.drawString(font, Component.translatable("awakening_system.screen.bestiary.section.strategy"), x + 8, cy, 0xFF50FA7B, false);
        cy += 10;
        cy = drawWrapped(g, "• " + cur.tactics(), x + 8, cy, maxW, 0xFFDDECF2);

        // Botín Notable
        cy += 2;
        g.drawString(font, Component.translatable("awakening_system.screen.bestiary.section.drops"), x + 8, cy, 0xFFFFB86C, false);
        cy += 10;
        cy = drawWrapped(g, "• " + cur.drops(), x + 8, cy, maxW, 0xFFDDECF2);

        // Lore / Notas de Campo
        cy += 2;
        cy = drawWrapped(g, "§8\"" + cur.lore() + "\"", x + 8, cy, maxW, 0xFF8AAAB8);

        g.disableScissor();

        // Barra de Scroll cibernética si el contenido supera el alto visible
        if (maxScroll > 0) {
            int barX = x + w - 4;
            int trackY = y + 4;
            int trackH = h - 8;
            int thumbH = Math.max(16, trackH * (h - 16) / contentH);
            int thumbY = trackY + (trackH - thumbH) * dossierScrollOffset / maxScroll;
            g.fill(barX, trackY, barX + 2, trackY + trackH, 0xFF142735);
            g.fill(barX, thumbY, barX + 2, thumbY + thumbH, 0xFF39B9D5);
        }
    }

    private int drawWrapped(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        for (var line : font.split(Component.literal(text), maxWidth)) {
            g.drawString(font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);
        int panelW = 480;
        int panelH = 270;
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2;

        int listX = left + 8;
        int listY = top + 32;
        int listW = 135;
        int listH = panelH - 40;

        // Scroll en Lista de Mobs (Izquierda)
        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else if (scrollY < 0) {
                int itemH = 26;
                int maxVisible = listH / itemH;
                int maxScroll = Math.max(0, entries.size() - maxVisible);
                scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            }
            return true;
        }

        // Scroll en Hunter Dossier (Derecha)
        int centerBoxX = listX + listW + 8;
        int centerBoxW = 125;
        int dossierX = centerBoxX + centerBoxW + 8;
        int dossierY = top + 32;
        int dossierW = left + panelW - dossierX - 8;
        int dossierH = panelH - 40;

        if (mouseX >= dossierX && mouseX <= dossierX + dossierW && mouseY >= dossierY && mouseY <= dossierY + dossierH) {
            if (!entries.isEmpty() && selectedIndex >= 0 && selectedIndex < entries.size()) {
                int maxW = dossierW - 20;
                int contentH = calculateDossierContentHeight(entries.get(selectedIndex), maxW);
                int maxScroll = Math.max(0, contentH - (dossierH - 16));
                if (maxScroll > 0) {
                    if (scrollY > 0) {
                        dossierScrollOffset = Math.max(0, dossierScrollOffset - 22);
                    } else if (scrollY < 0) {
                        dossierScrollOffset = Math.min(maxScroll, dossierScrollOffset + 22);
                    }
                    return true;
                }
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = transformMouseX(mouseX);
        mouseY = transformMouseY(mouseY);

        int panelW = 480;
        int panelH = 270;
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2;

        if (button == 0) {
            // Clic en Botón [X] Salir
            if (mouseX >= left + panelW - 32 && mouseX <= left + panelW - 10 && mouseY >= top + 6 && mouseY <= top + 20) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.0F);
                }
                onClose();
                return true;
            }

            // Clic en Lista de Entidades
            int listX = left + 8;
            int listY = top + 32;
            int listW = 135;
            int listH = panelH - 40;
            int itemH = 26;
            int maxVisible = listH / itemH;

            for (int i = 0; i < maxVisible && (i + scrollOffset) < entries.size(); i++) {
                int idx = i + scrollOffset;
                int ey = listY + 4 + i * itemH;
                if (mouseX >= listX + 2 && mouseX <= listX + listW - 2 && mouseY >= ey && mouseY <= ey + itemH - 2) {
                    if (selectedIndex != idx) {
                        selectedIndex = idx;
                        dossierScrollOffset = 0;
                    }
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.4F);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
