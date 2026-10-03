package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public enum CompanionProfile {
    COLUMBINA(
            "columbina",
            "Columbina",
            "Diosa Trilunar de Teyvat",
            5,
            "§d§l",
            "Lunar / Celestial",
            "Canto de las Tres Lunas: Despierta las autoridades de Aria, Sonnet y Canon. Lluvia de estrellas plateadas, regeneración cósmica y purificación del Abismo.",
            "assets/awakening_system/textures/entity/companion/columbina.png"
    ),
    RAIDEN_SHOGUN(
            "raiden_shogun",
            "Raiden Shogun",
            "La Shogun Todopoderosa",
            5,
            "§d§l",
            "Electro",
            "Ojo de la Devastación: Descarga rayos sobre los enemigos y energiza al jugador.",
            "assets/awakening_system/textures/entity/companion/raiden_shogun.png"
    ),
    HU_TAO(
            "hu_tao",
            "Hu Tao",
            "77.ª Directora de la Funeraria",
            5,
            "§c§l",
            "Pyro",
            "Guía hacia el Más Allá: Ráfagas de llamas ardientes y mariposas de fuego.",
            "assets/awakening_system/textures/entity/companion/hu_tao.png"
    ),
    ZHONGLI(
            "zhongli",
            "Zhongli",
            "Señor del Geo (Morax)",
            5,
            "§6§l",
            "Geo",
            "Dominio de la Tierra: Escudo de jade protector y petrificación a los enemigos.",
            "assets/awakening_system/textures/entity/companion/zhongli.png"
    ),
    FURINA(
            "furina",
            "Furina",
            "Estrella de Fontaine",
            5,
            "§b§l",
            "Hydro",
            "Cortesanos de las Aguas: Oleadas de agua que dañan enemigos y regeneran vida al jugador.",
            "assets/awakening_system/textures/entity/companion/furina.png"
    ),
    NAHIDA(
            "nahida",
            "Nahida",
            "Reina Menor Kusanali",
            5,
            "§a§l",
            "Dendro",
            "Templo Ilusorio: Enreda y paraliza enemigos desatando explosiones florecientes.",
            "assets/awakening_system/textures/entity/companion/nahida.png"
    ),
    KAZUHA(
            "kazuha",
            "Kaedehara Kazuha",
            "Espadachín Errante de Inazuma",
            5,
            "§f§l",
            "Anemo",
            "Corte de Chihayaburu: Atrae a los enemigos con torbellinos de viento y los eleva por los aires.",
            "assets/awakening_system/textures/entity/companion/kazuha.png"
    ),
    GOJO_SATORU(
            "gojo_satoru",
            "Satoru Gojo",
            "El Chamán Más Fuerte",
            5,
            "§3§l",
            "Infinito",
            "Técnica Maldita Inversa: Repulsión gravitacional que arrasa a los monstruos cercanos.",
            "assets/awakening_system/textures/entity/companion/gojo_satoru.png"
    ),
    SUNG_JINWOO(
            "sung_jinwoo",
            "Sung Jin-Woo",
            "Monarca de las Sombras",
            5,
            "§5§l",
            "Sombra",
            "Extracción y Combate de Sombras: Ataca con dagas veloces e invoca infantes de sombra.",
            "assets/awakening_system/textures/entity/companion/sung_jinwoo.png"
    ),
    NOELLE(
            "noelle",
            "Noelle",
            "Doncella Caballeresca de Favonius",
            4,
            "§e§l",
            "Geo",
            "Guardia de Favonius: Escudo acorazado y curación de vida al golpear.",
            "assets/awakening_system/textures/entity/companion/noelle.png"
    ),
    CHA_HAEIN(
            "cha_haein",
            "Cha Hae-In",
            "Bailarina de la Espada de Luz",
            4,
            "§6§l",
            "Luz",
            "Espada Sagrada: Embestidas fulgurantes de corte sagrado y alta velocidad.",
            "assets/awakening_system/textures/entity/companion/cha_haein.png"
    ),
    DILUC(
            "diluc",
            "Diluc",
            "El Amanecer Oscuro de Mondstadt",
            5,
            "§c§l",
            "Pyro",
            "Amanecer del Fénix: Desata un fénix ígneo que embiste y quema a todos los enemigos en su camino.",
            "assets/awakening_system/textures/entity/companion/diluc.png"
    ),
    JEAN(
            "jean",
            "Jean",
            "Gran Maestra Intendente",
            5,
            "§a§l",
            "Anemo",
            "Brisa de Diente de León: Despliega un campo de viento curativo que regenera salud masiva y repele enemigos.",
            "assets/awakening_system/textures/entity/companion/jean.png"
    ),
    BENNETT(
            "bennett",
            "Bennett",
            "Líder de la Brigada de Benny",
            4,
            "§6§l",
            "Pyro",
            "Viaje Fantástico: Crea un campo de inspiración que otorga Fuerza y Regeneración al jugador.",
            "assets/awakening_system/textures/entity/companion/bennett.png"
    ),

    // =========================================================================
    // HONKAI: STAR RAIL & ZENLESS ZONE ZERO
    // =========================================================================
    ACHERON(
            "acheron",
            "Acheron",
            "Emanadora de la Nihilidad",
            5,
            "§d§l",
            "Rayo / Nihilidad",
            "Corte del Fin: Desata una tormenta de lluvia carmesí y relámpagos oscuros que diezma a los enemigos.",
            "assets/awakening_system/textures/entity/companion/acheron.png"
    ),
    KAFKA(
            "kafka",
            "Kafka",
            "Cazadora de Estelaron",
            5,
            "§5§l",
            "Rayo / DoT",
            "Sugerencia Espiritual: Susurro de Estelaron que electrocuta enemigos en masa y detona daño persistente.",
            "assets/awakening_system/textures/entity/companion/kafka.png"
    ),
    ELLEN_JOE(
            "ellen_joe",
            "Ellen Joe",
            "Tiburón de Victoria Housekeeping",
            5,
            "§b§l",
            "Hielo / Escarcha",
            "Caza de Mareas: Deslizamiento glacial veloz y corte de tijeras que congela al instante a los hostiles.",
            "assets/awakening_system/textures/entity/companion/ellen_joe.png"
    ),

    // =========================================================================
    // SOLO LEVELING
    // =========================================================================
    IGRIS(
            "igris",
            "Igris",
            "Caballero Carmesí de Sangre",
            5,
            "§4§l",
            "Sombra / Corte",
            "Caballero Leal: Embestida con gran espada de sombra carmesí y teletransporte protector hacia el amo.",
            "assets/awakening_system/textures/entity/companion/igris.png"
    ),
    BERU(
            "beru",
            "Beru",
            "Rey Ant de las Sombras",
            5,
            "§9§l",
            "Sombra / Vorágine",
            "Grito del Depredador: Chillido subsónico que aterroriza a los enemigos e inflige veneno corrosivo veloz.",
            "assets/awakening_system/textures/entity/companion/beru.png"
    ),

    // =========================================================================
    // JUJUTSU KAISEN
    // =========================================================================
    SUKUNA(
            "sukuna",
            "Ryomen Sukuna",
            "Rey de las Maldiciones",
            5,
            "§c§l",
            "Maldición / Fuego",
            "Desmantelar & Kamifuga: Cortes invisibles devastadores seguidos de una flecha ardiente destructiva.",
            "assets/awakening_system/textures/entity/companion/sukuna.png"
    ),
    YUJI_ITADORI(
            "yuji_itadori",
            "Yuji Itadori",
            "Recipiente del Tigre",
            4,
            "§e§l",
            "Físico / Fuerza",
            "Destello Negro: Golpe concentrado con chispas carmesí y empuje extremo que pulveriza armaduras.",
            "assets/awakening_system/textures/entity/companion/yuji_itadori.png"
    ),

    // =========================================================================
    // FATE / TYPE-MOON
    // =========================================================================
    ARTORIA_PENDRAGON(
            "artoria_pendragon",
            "Artoria Pendragon",
            "Rey de los Caballeros (Saber)",
            5,
            "§b§l",
            "Sagrado / Luz",
            "Excalibur: Haz sagrado de luz celestial que aniquila monstruos en línea recta con gran resplandor.",
            "assets/awakening_system/textures/entity/companion/artoria_pendragon.png"
    ),
    GILGAMESH(
            "gilgamesh",
            "Gilgamesh",
            "Rey de los Héroes (Archer)",
            5,
            "§6§l",
            "Dorado / Tesoro",
            "Gate of Babylon: Abre portales dorados tras de sí y dispara una ráfaga continua de tesoros reales.",
            "assets/awakening_system/textures/entity/companion/gilgamesh.png"
    ),

    // =========================================================================
    // KIMETSU NO YAIBA / DEMON SLAYER
    // =========================================================================
    RENGOKU(
            "rengoku",
            "Kyojuro Rengoku",
            "Pilar de la Llama",
            5,
            "§6§l",
            "Fuego / Llama",
            "Novena Postura - Purgatorio: Carga en forma de fénix de fuego ardiente que calcina a todos los hostiles.",
            "assets/awakening_system/textures/entity/companion/rengoku.png"
    ),
    TANJIRO(
            "tanjiro",
            "Tanjiro Kamado",
            "Cazador de la Danza del Sol",
            4,
            "§a§l",
            "Agua / Solar",
            "Danza del Dios del Fuego: Cortes circulares de agua y fuego solar combinados con gran agilidad.",
            "assets/awakening_system/textures/entity/companion/tanjiro.png"
    ),
        NEZUKO(
            "nezuko",
            "Nezuko Kamado",
            "Demonio de la Sangre Ardiente",
            4,
            "§d§l",
            "Fuego Demoníaco",
            "Piroquinesis Carmesí: Llamas rosas que abrasan al enemigo y regeneran salud al jugador.",
            "assets/awakening_system/textures/entity/companion/nezuko.png"
    ),
    XIAO(
            "xiao",
            "Xiao",
            "Guardián Yaksha (Alatus)",
            5,
            "§b§l",
            "Anemo",
            "Azote de Todo Mal: Saltos acrobáticos fulgurantes e impacto descendente que pulveriza a los enemigos.",
            "assets/awakening_system/textures/entity/companion/xiao.png"
    ),
    ARLECCHINO(
            "arlecchino",
            "Arlecchino",
            "La Sota (Padre de la Casa de la Hoguera)",
            5,
            "§4§l",
            "Pyro / Carmesí",
            "Vasto Asolamiento: Ráfagas lunares de fuego carmesí y corte de guadaña sanguinaria.",
            "assets/awakening_system/textures/entity/companion/arlecchino.png"
    ),
    FIREFLY(
            "firefly",
            "Firefly (SAM)",
            "Armadura de la Caballería de Hierro",
            5,
            "§6§l",
            "Fuego / Combustión",
            "Sobrecarga Estratégica: Embestida aérea ígnea con propulsores que calcina el campo de batalla.",
            "assets/awakening_system/textures/entity/companion/firefly.png"
    ),
    MEGUMI(
            "megumi",
            "Megumi Fushiguro",
            "Chamán de las Diez Sombras",
            4,
            "§3§l",
            "Sombra / Shikigami",
            "Diez Sombras: Despliega al Perro Divino y Nue eléctrica para acorralar objetivos.",
            "assets/awakening_system/textures/entity/companion/megumi.png"
    ),
    ARCHER(
            "archer",
            "Archer (EMIYA)",
            "Espíritu Heroico de la Forja",
            4,
            "§c§l",
            "Proyección / Espadas",
            "Unlimited Blade Works: Proyecta espadas Kanshou & Bakuya y una lluvia de flechas calibradas.",
            "assets/awakening_system/textures/entity/companion/archer.png"
    ),
    GANYU(
            "ganyu",
            "Ganyu",
            "Vigía del Plenilunio",
            5,
            "§b§l",
            "Cryo",
            "Lluvia Celestial: Desata una tormenta de carámbanos que congela y ralentiza a los enemigos.",
            "assets/awakening_system/textures/entity/companion/ganyu.png"
    ),
    MARCH_7TH(
            "march_7th",
            "Siete de Marzo",
            "Arquera del Expreso Astral",
            4,
            "§d§l",
            "Hielo",
            "Poder del Arco Astral: Dispara flechas de cristal y otorga escudos a los aliados.",
            "assets/awakening_system/textures/entity/companion/march_7th.png"
    ),
    HOSHIMI_MIYABI(
            "hoshimi_miyabi",
            "Hoshimi Miyabi",
            "Jefa de la Sección 6",
            5,
            "§3§l",
            "Éter",
            "Corte Shunguang: Despliega tajos de velocidad hipersónica que aniquilan a los enemigos.",
            "assets/awakening_system/textures/entity/companion/hoshimi_miyabi.png"
    ),

    // =========================================================================
    // NUEVOS COMPAÑEROS GACHA (HSR / ZZZ / WUWA / GENSHIN)
    // =========================================================================
    DAN_HENG_IL(
            "dan_heng_il",
            "Dan Heng • Imbibitor Lunae",
            "Gran Anciano de los Vidyadhara",
            5,
            "§3§l",
            "Imaginario / Dragón",
            "Rugido del Dragón Sagrado: Invoca al dragón acuático de éter que desata ráfagas de ondas dracónicas.",
            "assets/awakening_system/textures/entity/companion/dan_heng_il.png"
    ),
    FEIXIAO(
            "feixiao",
            "Feixiao",
            "General Implacable de Yaoqing",
            5,
            "§a§l",
            "Viento / Cacería",
            "Torbellino de Caza: Embiste a velocidad del rayo combinando hachas y espadas que destrozan al objetivo.",
            "assets/awakening_system/textures/entity/companion/feixiao.png"
    ),
    AVENTURINE(
            "aventurine",
            "Aventurine",
            "Estratega de la Corporación (IPC)",
            5,
            "§6§l",
            "Imaginario / Preservación",
            "Apuesta Todo al Siete: Despliega un escudo de fortuna dorada y ametralla a los agresores con monedas numismáticas.",
            "assets/awakening_system/textures/entity/companion/aventurine.png"
    ),
    ROBIN(
            "robin",
            "Robin",
            "Cantante del Expreso de Halovian",
            5,
            "§d§l",
            "Físico / Armonía",
            "Concierto de Mil Voces: Entona una melodía celestial que potencia el ataque y la regeneración de todo el equipo.",
            "assets/awakening_system/textures/entity/companion/robin.png"
    ),
    JANE_DOE(
            "jane_doe",
            "Jane Doe",
            "Consultora Criminal de Nueva Eridu",
            5,
            "§c§l",
            "Físico / Anomalía",
            "Danza de la Cuchilla Pasional: Movimientos escurridizos ultrarrápidos que causan acumulación masiva de Asalto.",
            "assets/awakening_system/textures/entity/companion/jane_doe.png"
    ),
    ZHU_YUAN(
            "zhu_yuan",
            "Zhu Yuan",
            "Líder del Equipo Especial de Seguridad",
            5,
            "§9§l",
            "Éter / Balística",
            "Disparo Táctico Concentrado: Ráfaga de escopeta de éter que hace retroceder y aturde a las hordas.",
            "assets/awakening_system/textures/entity/companion/zhu_yuan.png"
    ),
    CAESAR_KING(
            "caesar_king",
            "Caesar King",
            "Líder de los Hijos de Calidón",
            5,
            "§4§l",
            "Físico / Defensa",
            "Barrera del Coliseo: Bloquea con su escudo de gladiadora y desata una onda expansiva con su hacha.",
            "assets/awakening_system/textures/entity/companion/caesar_king.png"
    ),
    JIYAN(
            "jiyan",
            "Jiyan",
            "General de los Midnight Rangers",
            5,
            "§2§l",
            "Aero / Lanza Dracónica",
            "Viento de Qingloong: Invoca al colosal dragón de viento esmeralda y barre a los enemigos en espiral.",
            "assets/awakening_system/textures/entity/companion/jiyan.png"
    ),
    CHANGLI(
            "changli",
            "Changli",
            "Mentora de Jinzhou",
            5,
            "§c§l",
            "Fusión / Llamas del Fénix",
            "Vuelo del Fénix Carmesí: Despliega tajos de fuego ardiente y plumas incandescentes que calcinan el área.",
            "assets/awakening_system/textures/entity/companion/changli.png"
    ),
    JINSHI(
            "jinshi",
            "Jinhsi",
            "Magistrada de Jinzhou",
            5,
            "§e§l",
            "Espectro / Divinidad",
            "Luz de Jué: Desciende con un haz de luz sagrada que aniquila monstruos en línea recta.",
            "assets/awakening_system/textures/entity/companion/jinshi.png"
    ),
    YINLIN(
            "yinlin",
            "Yinlin",
            "Investigadora Oculta",
            5,
            "§5§l",
            "Electro / Marioneta",
            "Castigo del Zapstring: Controla a su muñeca espectral detonando tormentas de truenos en cadena.",
            "assets/awakening_system/textures/entity/companion/yinlin.png"
    ),
    MAVUIKA(
            "mavuika",
            "Mavuika",
            "Arconte Pyro de Natlan",
            5,
            "§6§l",
            "Pyro / Fuego Primordial",
            "Carga Solar Ardiente: Golpea con el poder del sol desatando llamaradas de fuego carmesí.",
            "assets/awakening_system/textures/entity/companion/mavuika.png"
    ),
    KINICH(
            "kinich",
            "Kinich",
            "Cazador de Malpais",
            5,
            "§a§l",
            "Dendro / Bestia",
            "Disparo del Cañón Ajaw: Maniobras acrobáticas con gancho y cañonazos dendro de alta potencia.",
            "assets/awakening_system/textures/entity/companion/kinich.png"
    ),
    XILONEN(
            "xilonen",
            "Xilonen",
            "Forjadora de Nombres",
            5,
            "§e§l",
            "Geo / Ritmo",
            "Compás de la Tierra: Desliza sus patines sónicos triturando las armaduras y resistencias de los rivales.",
            "assets/awakening_system/textures/entity/companion/xilonen.png"
    ),
    CLORINDE(
            "clorinde",
            "Clorinde",
            "Duelista Judicial Invicta",
            5,
            "§9§l",
            "Electro / Estoque",
            "Caza Sombría del Trueno: Estocadas de esgrima eléctrica combinadas con disparos perforantes de pistola.",
            "assets/awakening_system/textures/entity/companion/clorinde.png"
    ),
    SKIRK(
            "skirk",
            "Skirk",
            "Maestra del Abismo",
            5,
            "§1§l",
            "Abismo / Vacío",
            "Vorágine del Caos Primordial: Desgarra el espacio-tiempo absorbiendo a los enemigos en un pozo sin fondo.",
            "assets/awakening_system/textures/entity/companion/skirk.png"
    ),
    JING_YUAN(
            "jing_yuan",
            "Jing Yuan",
            "General de los Caballeros de las Nubes",
            5,
            "§e§l",
            "Rayo / Erudición",
            "Señor del Rayo: Invoca al coloso del Rayo para desatar una tormenta de tajos demoledores sobre el campo de batalla.",
            "assets/awakening_system/textures/entity/companion/jing_yuan.png"
    );


    private final String id;
    private final String displayName;
    private final String title;
    private final int stars;
    private final String colorCode;
    private final String element;
    private final String skillDesc;
    private final ResourceLocation textureLocation;

    CompanionProfile(String id, String displayName, String title, int stars, String colorCode, String element, String skillDesc, String texturePath) {
        this.id = id;
        this.displayName = displayName;
        this.title = title;
        this.stars = stars;
        this.colorCode = colorCode;
        this.element = element;
        this.skillDesc = skillDesc;
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/companion/" + id + ".png");
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String title() { return title; }
    public int stars() { return stars; }
    public String colorCode() { return colorCode; }
    public String element() { return element; }
    public String skillDesc() { return skillDesc; }
    public ResourceLocation getTextureLocation() { return textureLocation; }
    public boolean isSlim() {
        return switch (this) {
            case RAIDEN_SHOGUN, HU_TAO, NOELLE, NAHIDA, KAZUHA, CHA_HAEIN,
                 ACHERON, KAFKA, ELLEN_JOE, ARTORIA_PENDRAGON, NEZUKO,
                 ARLECCHINO, FIREFLY, FEIXIAO, ROBIN, JANE_DOE, ZHU_YUAN,
                 CAESAR_KING, CHANGLI, JINSHI, YINLIN, MAVUIKA, XILONEN,
                 CLORINDE, SKIRK -> true;
            default -> false;
        };
    }

        public SoundEvent getSummonSound() {
        return switch (this) {
            case RAIDEN_SHOGUN -> ModSounds.COMPANION_RAIDEN_SHOGUN_SUMMON.get();
            case HU_TAO -> ModSounds.COMPANION_HU_TAO_SUMMON.get();
            case ZHONGLI -> ModSounds.COMPANION_ZHONGLI_SUMMON.get();
            case FURINA -> ModSounds.COMPANION_FURINA_SUMMON.get();
            case NAHIDA -> ModSounds.COMPANION_NAHIDA_SUMMON.get();
            case KAZUHA -> ModSounds.COMPANION_KAZUHA_SUMMON.get();
            case GOJO_SATORU -> ModSounds.COMPANION_GOJO_SATORU_SUMMON.get();
            case SUNG_JINWOO -> ModSounds.COMPANION_SUNG_JINWOO_SUMMON.get();
            case NOELLE -> ModSounds.COMPANION_NOELLE_SUMMON.get();
            case CHA_HAEIN -> ModSounds.COMPANION_CHA_HAEIN_SUMMON.get();
            case DILUC -> ModSounds.COMPANION_DILUC_SUMMON.get();
            case JEAN -> ModSounds.COMPANION_JEAN_SUMMON.get();
            case BENNETT -> ModSounds.COMPANION_BENNETT_SUMMON.get();
            case ACHERON -> ModSounds.COMPANION_ACHERON_SUMMON.get();
            case KAFKA -> ModSounds.COMPANION_KAFKA_SUMMON.get();
            case ELLEN_JOE -> ModSounds.COMPANION_ELLEN_JOE_SUMMON.get();
            case IGRIS -> ModSounds.COMPANION_IGRIS_SUMMON.get();
            case BERU -> ModSounds.COMPANION_BERU_SUMMON.get();
            case SUKUNA -> ModSounds.COMPANION_SUKUNA_SUMMON.get();
            case YUJI_ITADORI -> ModSounds.COMPANION_YUJI_ITADORI_SUMMON.get();
            case ARTORIA_PENDRAGON -> ModSounds.COMPANION_ARTORIA_PENDRAGON_SUMMON.get();
            case GILGAMESH -> ModSounds.COMPANION_GILGAMESH_SUMMON.get();
            case RENGOKU -> ModSounds.COMPANION_RENGOKU_SUMMON.get();
            case TANJIRO -> ModSounds.COMPANION_TANJIRO_SUMMON.get();
            case NEZUKO -> ModSounds.COMPANION_NEZUKO_SUMMON.get();
            case XIAO -> ModSounds.COMPANION_XIAO_SUMMON.get();
            case ARLECCHINO -> ModSounds.COMPANION_ARLECCHINO_SUMMON.get();
            case FIREFLY -> ModSounds.COMPANION_FIREFLY_SUMMON.get();
            case MEGUMI -> ModSounds.COMPANION_MEGUMI_SUMMON.get();
            case ARCHER -> ModSounds.COMPANION_ARCHER_SUMMON.get();
            default -> ModSounds.NPC_BLESSING.get();
        };
    }

    public SoundEvent getSkillSound() {
        return switch (this) {
            case RAIDEN_SHOGUN -> ModSounds.COMPANION_RAIDEN_SHOGUN_SKILL.get();
            case HU_TAO -> ModSounds.COMPANION_HU_TAO_SKILL.get();
            case ZHONGLI -> ModSounds.COMPANION_ZHONGLI_SKILL.get();
            case FURINA -> ModSounds.COMPANION_FURINA_SKILL.get();
            case NAHIDA -> ModSounds.COMPANION_NAHIDA_SKILL.get();
            case KAZUHA -> ModSounds.COMPANION_KAZUHA_SKILL.get();
            case GOJO_SATORU -> ModSounds.COMPANION_GOJO_SATORU_SKILL.get();
            case SUNG_JINWOO -> ModSounds.COMPANION_SUNG_JINWOO_SKILL.get();
            case NOELLE -> ModSounds.COMPANION_NOELLE_SKILL.get();
            case CHA_HAEIN -> ModSounds.COMPANION_CHA_HAEIN_SKILL.get();
            case DILUC -> ModSounds.COMPANION_DILUC_SKILL.get();
            case JEAN -> ModSounds.COMPANION_JEAN_SKILL.get();
            case BENNETT -> ModSounds.COMPANION_BENNETT_SKILL.get();
            case ACHERON -> ModSounds.COMPANION_ACHERON_SKILL.get();
            case KAFKA -> ModSounds.COMPANION_KAFKA_SKILL.get();
            case ELLEN_JOE -> ModSounds.COMPANION_ELLEN_JOE_SKILL.get();
            case IGRIS -> ModSounds.COMPANION_IGRIS_SKILL.get();
            case BERU -> ModSounds.COMPANION_BERU_SKILL.get();
            case SUKUNA -> ModSounds.COMPANION_SUKUNA_SKILL.get();
            case YUJI_ITADORI -> ModSounds.COMPANION_YUJI_ITADORI_SKILL.get();
            case ARTORIA_PENDRAGON -> ModSounds.COMPANION_ARTORIA_PENDRAGON_SKILL.get();
            case GILGAMESH -> ModSounds.COMPANION_GILGAMESH_SKILL.get();
            case RENGOKU -> ModSounds.COMPANION_RENGOKU_SKILL.get();
            case TANJIRO -> ModSounds.COMPANION_TANJIRO_SKILL.get();
            case NEZUKO -> ModSounds.COMPANION_NEZUKO_SKILL.get();
            case XIAO -> ModSounds.COMPANION_XIAO_SKILL.get();
            case ARLECCHINO -> ModSounds.COMPANION_ARLECCHINO_SKILL.get();
            case FIREFLY -> ModSounds.COMPANION_FIREFLY_SKILL.get();
            case MEGUMI -> ModSounds.COMPANION_MEGUMI_SKILL.get();
            case ARCHER -> ModSounds.COMPANION_ARCHER_SKILL.get();
            default -> ModSounds.NPC_BLESSING.get();
        };
    }

    public SoundEvent getAmbientSound() {
        return switch (this) {
            case RAIDEN_SHOGUN -> ModSounds.COMPANION_RAIDEN_SHOGUN_AMBIENT.get();
            case HU_TAO -> ModSounds.COMPANION_HU_TAO_AMBIENT.get();
            case ZHONGLI -> ModSounds.COMPANION_ZHONGLI_AMBIENT.get();
            case FURINA -> ModSounds.COMPANION_FURINA_AMBIENT.get();
            case NAHIDA -> ModSounds.COMPANION_NAHIDA_AMBIENT.get();
            case KAZUHA -> ModSounds.COMPANION_KAZUHA_AMBIENT.get();
            case GOJO_SATORU -> ModSounds.COMPANION_GOJO_SATORU_AMBIENT.get();
            case SUNG_JINWOO -> ModSounds.COMPANION_SUNG_JINWOO_AMBIENT.get();
            case NOELLE -> ModSounds.COMPANION_NOELLE_AMBIENT.get();
            case CHA_HAEIN -> ModSounds.COMPANION_CHA_HAEIN_AMBIENT.get();
            case DILUC -> ModSounds.COMPANION_DILUC_AMBIENT.get();
            case JEAN -> ModSounds.COMPANION_JEAN_AMBIENT.get();
            case BENNETT -> ModSounds.COMPANION_BENNETT_AMBIENT.get();
            case ACHERON -> ModSounds.COMPANION_ACHERON_AMBIENT.get();
            case KAFKA -> ModSounds.COMPANION_KAFKA_AMBIENT.get();
            case ELLEN_JOE -> ModSounds.COMPANION_ELLEN_JOE_AMBIENT.get();
            case IGRIS -> ModSounds.COMPANION_IGRIS_AMBIENT.get();
            case BERU -> ModSounds.COMPANION_BERU_AMBIENT.get();
            case SUKUNA -> ModSounds.COMPANION_SUKUNA_AMBIENT.get();
            case YUJI_ITADORI -> ModSounds.COMPANION_YUJI_ITADORI_AMBIENT.get();
            case ARTORIA_PENDRAGON -> ModSounds.COMPANION_ARTORIA_PENDRAGON_AMBIENT.get();
            case GILGAMESH -> ModSounds.COMPANION_GILGAMESH_AMBIENT.get();
            case RENGOKU -> ModSounds.COMPANION_RENGOKU_AMBIENT.get();
            case TANJIRO -> ModSounds.COMPANION_TANJIRO_AMBIENT.get();
            case NEZUKO -> ModSounds.COMPANION_NEZUKO_AMBIENT.get();
            case XIAO -> ModSounds.COMPANION_XIAO_AMBIENT.get();
            case ARLECCHINO -> ModSounds.COMPANION_ARLECCHINO_AMBIENT.get();
            case FIREFLY -> ModSounds.COMPANION_FIREFLY_AMBIENT.get();
            case MEGUMI -> ModSounds.COMPANION_MEGUMI_AMBIENT.get();
            case ARCHER -> ModSounds.COMPANION_ARCHER_AMBIENT.get();
            default -> ModSounds.NPC_INTERACT.get();
        };
    }

    public SoundEvent getHurtSound() {
        return switch (this) {
            case RAIDEN_SHOGUN -> ModSounds.COMPANION_RAIDEN_SHOGUN_HURT.get();
            case HU_TAO -> ModSounds.COMPANION_HU_TAO_HURT.get();
            case ZHONGLI -> ModSounds.COMPANION_ZHONGLI_HURT.get();
            case FURINA -> ModSounds.COMPANION_FURINA_HURT.get();
            case NAHIDA -> ModSounds.COMPANION_NAHIDA_HURT.get();
            case KAZUHA -> ModSounds.COMPANION_KAZUHA_HURT.get();
            case GOJO_SATORU -> ModSounds.COMPANION_GOJO_SATORU_HURT.get();
            case SUNG_JINWOO -> ModSounds.COMPANION_SUNG_JINWOO_HURT.get();
            case NOELLE -> ModSounds.COMPANION_NOELLE_HURT.get();
            case CHA_HAEIN -> ModSounds.COMPANION_CHA_HAEIN_HURT.get();
            case DILUC -> ModSounds.COMPANION_DILUC_HURT.get();
            case JEAN -> ModSounds.COMPANION_JEAN_HURT.get();
            case BENNETT -> ModSounds.COMPANION_BENNETT_HURT.get();
            case ACHERON -> ModSounds.COMPANION_ACHERON_HURT.get();
            case KAFKA -> ModSounds.COMPANION_KAFKA_HURT.get();
            case ELLEN_JOE -> ModSounds.COMPANION_ELLEN_JOE_HURT.get();
            case IGRIS -> ModSounds.COMPANION_IGRIS_HURT.get();
            case BERU -> ModSounds.COMPANION_BERU_HURT.get();
            case SUKUNA -> ModSounds.COMPANION_SUKUNA_HURT.get();
            case YUJI_ITADORI -> ModSounds.COMPANION_YUJI_ITADORI_HURT.get();
            case ARTORIA_PENDRAGON -> ModSounds.COMPANION_ARTORIA_PENDRAGON_HURT.get();
            case GILGAMESH -> ModSounds.COMPANION_GILGAMESH_HURT.get();
            case RENGOKU -> ModSounds.COMPANION_RENGOKU_HURT.get();
            case TANJIRO -> ModSounds.COMPANION_TANJIRO_HURT.get();
            case NEZUKO -> ModSounds.COMPANION_NEZUKO_HURT.get();
            case XIAO -> ModSounds.COMPANION_XIAO_HURT.get();
            case ARLECCHINO -> ModSounds.COMPANION_ARLECCHINO_HURT.get();
            case FIREFLY -> ModSounds.COMPANION_FIREFLY_HURT.get();
            case MEGUMI -> ModSounds.COMPANION_MEGUMI_HURT.get();
            case ARCHER -> ModSounds.COMPANION_ARCHER_HURT.get();
            default -> net.minecraft.sounds.SoundEvents.GENERIC_HURT;
        };
    }

    public net.minecraft.world.item.ItemStack getSignatureWeapon() {
        return switch (this) {
            case RAIDEN_SHOGUN -> com.soybe.awakeningsystem.SystemItems.ENGULFING_LIGHTNING.isBound() ? new net.minecraft.world.item.ItemStack(com.soybe.awakeningsystem.SystemItems.ENGULFING_LIGHTNING.get()) : net.minecraft.world.item.ItemStack.EMPTY;
            case HU_TAO -> com.soybe.awakeningsystem.SystemItems.STAFF_OF_HOMA.isBound() ? new net.minecraft.world.item.ItemStack(com.soybe.awakeningsystem.SystemItems.STAFF_OF_HOMA.get()) : net.minecraft.world.item.ItemStack.EMPTY;
            case ZHONGLI -> com.soybe.awakeningsystem.SystemItems.VORTEX_VANQUISHER.isBound() ? new net.minecraft.world.item.ItemStack(com.soybe.awakeningsystem.SystemItems.VORTEX_VANQUISHER.get()) : net.minecraft.world.item.ItemStack.EMPTY;
            case DILUC -> com.soybe.awakeningsystem.SystemItems.WOLFS_GRAVESTONE.isBound() ? new net.minecraft.world.item.ItemStack(com.soybe.awakeningsystem.SystemItems.WOLFS_GRAVESTONE.get()) : net.minecraft.world.item.ItemStack.EMPTY;
            case GANYU -> com.soybe.awakeningsystem.SystemItems.AMOS_BOW.isBound() ? new net.minecraft.world.item.ItemStack(com.soybe.awakeningsystem.SystemItems.AMOS_BOW.get()) : net.minecraft.world.item.ItemStack.EMPTY;
            case MARCH_7TH -> com.soybe.awakeningsystem.SystemItems.ASTRAL_ARCHERS_BOW.isBound() ? new net.minecraft.world.item.ItemStack(com.soybe.awakeningsystem.SystemItems.ASTRAL_ARCHERS_BOW.get()) : net.minecraft.world.item.ItemStack.EMPTY;
            case HOSHIMI_MIYABI -> com.soybe.awakeningsystem.SystemItems.SHUNGUANG_SWORD.isBound() ? new net.minecraft.world.item.ItemStack(com.soybe.awakeningsystem.SystemItems.SHUNGUANG_SWORD.get()) : net.minecraft.world.item.ItemStack.EMPTY;
            case SUNG_JINWOO -> com.soybe.awakeningsystem.SystemItems.SHADOW_DAGGERS.isBound() ? new net.minecraft.world.item.ItemStack(com.soybe.awakeningsystem.SystemItems.SHADOW_DAGGERS.get()) : net.minecraft.world.item.ItemStack.EMPTY;
            case CHA_HAEIN -> com.soybe.awakeningsystem.SystemItems.RADIANT_LIGHT_SWORD.isBound() ? new net.minecraft.world.item.ItemStack(com.soybe.awakeningsystem.SystemItems.RADIANT_LIGHT_SWORD.get()) : net.minecraft.world.item.ItemStack.EMPTY;
            default -> net.minecraft.world.item.ItemStack.EMPTY;
        };
    }

    public static CompanionProfile getById(String id) {
        if (id == null) return NOELLE;
        for (CompanionProfile cp : values()) {
            if (cp.id.equalsIgnoreCase(id)) return cp;
        }
        return NOELLE;
    }
}
