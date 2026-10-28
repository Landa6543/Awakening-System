package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.entity.CompanionProfile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sistema Multiverso de Banners Promocionales de Gacha (Banners Dobles Oficiales).
 * Cada fase cuenta con dos personajes 5★ rate-up simultáneos, acompañados de sus armas
 * insignia canónicas y personajes 4★ de la franquicia.
 * Rotación dinámica basada en días de Minecraft (14 días MC = 336,000 ticks).
 */
public final class PromotionalBanner {
    public static final long MC_DAY_TICKS = 24000L;
    public static final long BANNER_CYCLE_DAYS = 14L;
    public static final long BANNER_CYCLE_TICKS = BANNER_CYCLE_DAYS * MC_DAY_TICKS;

    public enum Franchise {
        GENSHIN(0, "Genshin Impact", "TEYVAT", "§b", 0xFF64B5F6, 0x661976D2),
        HONKAI_ZZZ(1, "Honkai & ZZZ", "HOYOVERSE", "§d", 0xFFBA68C8, 0x667B1FA2),
        SOLO_LEVELING(2, "Solo Leveling", "SHADOWS", "§5", 0xFFAB47BC, 0x664A148C),
        JUJUTSU_KAISEN(3, "Jujutsu Kaisen", "SORCERY", "§3", 0xFF26C6DA, 0x6600838F),
        FATE(4, "Fate Series", "HOLY GRAIL", "§e", 0xFFFFD54F, 0x66FF8F00),
        DEMON_SLAYER(5, "Demon Slayer", "SLAYERS", "§c", 0xFFFF7043, 0x66D32F2F),
        WUTHERING_WAVES(6, "Wuthering Waves", "SOLARIS-3", "§a", 0xFF81C784, 0x662E7D32),
        NTE(7, "Neverness to Everness", "HETHEREAU", "§6", 0xFFFFB74D, 0x66E65100);

        private final int id;
        private final String displayName;
        private final String shortBadge;
        private final String colorCode;
        private final int primaryColor;
        private final int glowColor;

        Franchise(int id, String displayName, String shortBadge, String colorCode, int primaryColor, int glowColor) {
            this.id = id;
            this.displayName = displayName;
            this.shortBadge = shortBadge;
            this.colorCode = colorCode;
            this.primaryColor = primaryColor;
            this.glowColor = glowColor;
        }

        public int id() { return id; }
        public String displayName() { return displayName; }
        public String shortBadge() { return shortBadge; }
        public String colorCode() { return colorCode; }
        public int primaryColor() { return primaryColor; }
        public int glowColor() { return glowColor; }

        public static Franchise byId(int id) {
            for (Franchise f : values()) {
                if (f.id == id) return f;
            }
            return GENSHIN;
        }
    }

    public record BannerPhase(
            int phaseIndex,
            Franchise franchise,
            String bannerTitle,
            String bannerSubtitle,
            CompanionProfile featuredCharacter,
            CompanionProfile secondaryFeaturedCharacter,
            Supplier<ItemStack> primaryFeaturedWeapon,
            Supplier<ItemStack> secondaryFeaturedWeapon,
            List<CompanionProfile> rateUp4StarCharacters,
            List<Supplier<ItemStack>> rateUp4StarWeapons,
            int themeColor,
            int themeGlowColor,
            String elementBadge,
            String archonQuote
    ) {}

    private static Supplier<ItemStack> wp(DeferredHolder<Item, Item> holder, Item fallback) {
        return () -> holder.isBound() ? new ItemStack(holder.get()) : new ItemStack(fallback);
    }

    private static Supplier<ItemStack> wp(DeferredHolder<Item, Item> holder) {
        return wp(holder, Items.DIAMOND_SWORD);
    }

    private static final BannerPhase[] GENSHIN_PHASES = new BannerPhase[]{
            new BannerPhase(
                    1, Franchise.GENSHIN,
                    "REALM OF SERENITY & WISDOM",
                    "Plane of Euthymia & Heart of Kusanali",
                    CompanionProfile.RAIDEN_SHOGUN, CompanionProfile.NAHIDA,
                    wp(SystemItems.ENGULFING_LIGHTNING, Items.NETHERITE_SWORD),
                    wp(SystemItems.FREEDOM_SWORN, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT, CompanionProfile.YAOYAO),
                    List.of(wp(SystemItems.THE_CATCH, Items.IRON_SWORD), wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD)),
                    0xFFBA68C8, 0x667B1FA2,
                    "ELECTRO & DENDRO ⚡🍃 5★",
                    "\"La eternidad y los secretos del Irminsul convergen en el plano celestial.\""
            ),
            new BannerPhase(
                    2, Franchise.GENSHIN,
                    "FLEETING BLOOM & COURT OF FONTAINE",
                    "Dance of Butterflies & Drama of Fontaine",
                    CompanionProfile.HU_TAO, CompanionProfile.FURINA,
                    wp(SystemItems.STAFF_OF_HOMA, Items.NETHERITE_SWORD),
                    wp(SystemItems.ABSOLUTION, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE, CompanionProfile.DIONA),
                    List.of(wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD), wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD)),
                    0xFFFF7043, 0x66D32F2F,
                    "PYRO & HYDRO 🔥💧 5★",
                    "\"¡Que el telón se alce y las mariposas de fuego dancen en el drama de Fontaine!\""
            ),
            new BannerPhase(
                    3, Franchise.GENSHIN,
                    "SOLITUDE OF NOBILITY & SUPREME JUDGMENT",
                    "Ruler of the Firmament & Iudex of Fontaine",
                    CompanionProfile.ZHONGLI, CompanionProfile.NEUVILLETTE,
                    wp(SystemItems.VORTEX_VANQUISHER, Items.NETHERITE_SWORD),
                    wp(SystemItems.PRIMORDIAL_JADE_CUTTER, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT, CompanionProfile.SAYU),
                    List.of(wp(SystemItems.THE_FLUTE, Items.IRON_SWORD), wp(SystemItems.THE_CATCH, Items.IRON_SWORD)),
                    0xFFFFD54F, 0x66FFA000,
                    "GEO & HYDRO ⛰💧 5★",
                    "\"El pacto inmemorial de Liyue y el veredicto de Fontaine sostienen el orden de Teyvat.\""
            ),
            new BannerPhase(
                    4, Franchise.GENSHIN,
                    "KNIGHTS OF FAVONIUS",
                    "Wind Lion and Night Falcon",
                    CompanionProfile.DILUC, CompanionProfile.JEAN,
                    wp(SystemItems.WOLFS_GRAVESTONE, Items.NETHERITE_AXE),
                    wp(SystemItems.SKYWARD_BLADE, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE, CompanionProfile.DIONA),
                    List.of(wp(SystemItems.THE_FLUTE, Items.IRON_SWORD), wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD)),
                    0xFFE57373, 0x66C62828,
                    "FAVONIUS 🦅 5★",
                    "\"Por la libertad de Mondstadt, mantendremos la guardia en el viento y las sombras.\""
            ),
            new BannerPhase(
                    5, Franchise.GENSHIN,
                    "INVITATION OF WIND-BLOWN EMBERS",
                    "Vigilant Yaksha & Poem of the Wandering Wind",
                    CompanionProfile.XIAO, CompanionProfile.KAZUHA,
                    wp(SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR, Items.NETHERITE_SWORD),
                    wp(SystemItems.FREEDOM_SWORN, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT, CompanionProfile.SAYU),
                    List.of(wp(SystemItems.THE_CATCH, Items.IRON_SWORD), wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD)),
                    0xFF00BFA5, 0x66004D40,
                    "ANEMO 🍃 5★",
                    "\"Grita el nombre de Xiao mientras las hojas de arce susurran el poema del viento errante.\""
            ),
            new BannerPhase(
                    6, Franchise.GENSHIN,
                    "CRIMSON MOON & JUDICIAL DUEL",
                    "The Knave of the Fatui & Undefeated Champion Duelist",
                    CompanionProfile.ARLECCHINO, CompanionProfile.CLORINDE,
                    wp(SystemItems.CRIMSON_MOONS_SEMBLANCE, Items.NETHERITE_SWORD),
                    wp(SystemItems.ABSOLUTION, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE, CompanionProfile.DIONA),
                    List.of(wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD), wp(SystemItems.THE_FLUTE, Items.IRON_SWORD)),
                    0xFFB71C1C, 0x667F0000,
                    "PYRO & ELECTRO 🌙⚡ 5★",
                    "\"En la Casa de la Hoguera y en el tribunal del duelo, la debilidad no tiene cabida.\""
            ),
            new BannerPhase(
                    7, Franchise.GENSHIN,
                    "SPARKS AND FAVONIUS BUBBLES",
                    "Explosive Sparkle & Head Nurse of Meropide",
                    CompanionProfile.KLEE, CompanionProfile.SIGEWINNE,
                    wp(SystemItems.STAFF_OF_HOMA, Items.NETHERITE_SWORD),
                    wp(SystemItems.ABSOLUTION, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.YAOYAO, CompanionProfile.DIONA, CompanionProfile.SAYU),
                    List.of(wp(SystemItems.THE_FLUTE, Items.IRON_SWORD), wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD)),
                    0xFFFF7043, 0x66D32F2F,
                    "PYRO & HYDRO 💥💧 5★",
                    "\"¡Las bombas saltarinas de Klee y el cuidado de Sigewinne alegran el día!\""
            ),
            new BannerPhase(
                    8, Franchise.GENSHIN,
                    "ESPIONAGE AND CRYSTAL UMBRELLAS",
                    "Valley of Orchids & Spina di Rosula President",
                    CompanionProfile.YELAN, CompanionProfile.NAVIA,
                    wp(SystemItems.AQUA_SIMULACRA, Items.BOW),
                    wp(SystemItems.VERDICT, Items.NETHERITE_AXE),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT, CompanionProfile.YAOYAO),
                    List.of(wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD), wp(SystemItems.THE_CATCH, Items.IRON_SWORD)),
                    0xFF1E88E5, 0x660D47A1,
                    "HYDRO & GEO 💎 5★",
                    "\"Entre los hilos del destino de Yelan y los cañones ceremoniales de Navia, ningún secreto escapa.\""
            ),
            new BannerPhase(
                    9, Franchise.GENSHIN,
                    "FLAMES OF WAR & BEAT OF NATLAN",
                    "Pyro Archon of Natlan & Nameforger",
                    CompanionProfile.MAVUIKA, CompanionProfile.XILONEN,
                    wp(SystemItems.WOLFS_GRAVESTONE, Items.NETHERITE_AXE),
                    wp(SystemItems.PRIMORDIAL_JADE_CUTTER, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE, CompanionProfile.YAOYAO),
                    List.of(wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD), wp(SystemItems.THE_FLUTE, Items.IRON_SWORD)),
                    0xFFE65100, 0x66BF360C,
                    "PYRO & GEO 🔥⛰ 5★",
                    "\"¡El fuego sagrado de Mavuika y los ritmos de obsidiana de Xilonen sacuden las tierras de Natlan!\""
            ),
            new BannerPhase(
                    10, Franchise.GENSHIN,
                    "MALPAIS HUNT & PRIMORDIAL ABYSS",
                    "Malpais Hunter & Master of the Abyss",
                    CompanionProfile.KINICH, CompanionProfile.SKIRK,
                    wp(SystemItems.REDHORN_STONETHRESHER, Items.NETHERITE_AXE),
                    wp(SystemItems.ASRAHAN_HOLY_SWORD, Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE, CompanionProfile.SAYU),
                    List.of(wp(SystemItems.THE_CATCH, Items.IRON_SWORD), wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD)),
                    0xFF43A047, 0x661B5E20,
                    "DENDRO & ABYSS 🦎🌌 5★",
                    "\"Los saltos de gancho de Ajaw desafían la insondable inmensidad del Abismo Primordial.\""
            ),
            new BannerPhase(
                    11, Franchise.GENSHIN,
                    "TRILUNAR SYMPHONY & FROST PLENILUNE",
                    "Trilunar Goddess of Teyvat & Watcher of the Plenilune",
                    CompanionProfile.COLUMBINA, CompanionProfile.GANYU,
                    wp(SystemItems.MISTSPLITTER_REFORGED, Items.DIAMOND_SWORD),
                    wp(SystemItems.POLAR_STAR, Items.BOW),
                    List.of(CompanionProfile.DIONA, CompanionProfile.NOELLE, CompanionProfile.YAOYAO),
                    List.of(wp(SystemItems.THE_FLUTE, Items.IRON_SWORD), wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD)),
                    0xFFBA68C8, 0x667B1FA2,
                    "LUNAR & CRYO 🌙❄ 5★",
                    "\"Las tres lunas cantan en la noche cósmica bajo la lluvia de flechas de loto de escarcha.\""
            )
    };

    private static final BannerPhase[] HONKAI_PHASES = new BannerPhase[]{
            new BannerPhase(
                    12, Franchise.HONKAI_ZZZ,
                    "WORDS IN THE RIVER & SYMPHONY OF FATE",
                    "Emanator of Nihility & Threads of the Stellaron",
                    CompanionProfile.ACHERON, CompanionProfile.KAFKA,
                    wp(SystemItems.LIGHT_CONE_PASSING_SHORE, Items.NETHERITE_SWORD),
                    wp(SystemItems.LIGHT_CONE_PATIENCE, Items.TOTEM_OF_UNDYING),
                    List.of(CompanionProfile.MARCH_7TH, CompanionProfile.DAN_HENG, CompanionProfile.SERVAL),
                    List.of(wp(SystemItems.LIGHT_CONE_GOOD_NIGHT_SLEEP_WELL, Items.IRON_SWORD), wp(SystemItems.LIGHT_CONE_EYES_OF_THE_PREY, Items.IRON_SWORD)),
                    0xFF9C27B0, 0x664A148C,
                    "NIHILITY & STELLARON ⚡🕷 5★",
                    "\"Aquellos que pisan el Río del Olvido escuchan el susurro del Estelaron... Boom.\""
            ),
            new BannerPhase(
                    13, Franchise.HONKAI_ZZZ,
                    "FLIGHT OF THE FIREFLY & GLACIAL REFLECTION",
                    "Cavalry SAM Armor & Twilight Glacial Blade",
                    CompanionProfile.FIREFLY, CompanionProfile.JINGLIU,
                    wp(SystemItems.LIGHT_CONE_DREAMS_REST, Items.NETHERITE_SWORD),
                    wp(SystemItems.MISTSPLITTER_REFORGED, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.MARCH_7TH, CompanionProfile.HERTA, CompanionProfile.NATASHA),
                    List.of(wp(SystemItems.LIGHT_CONE_THE_DAY_COSMOS_FELL, Items.IRON_SWORD), wp(SystemItems.LIGHT_CONE_POST_OP_CONVERSATION, Items.IRON_SWORD)),
                    0xFFFF6F00, 0x66E65100,
                    "COMBUSTION & FROST 🔥❄ 5★",
                    "\"¡Prenderé fuego a los mares hasta que la espada glacial corte los límites del universo!\""
            ),
            new BannerPhase(
                    14, Franchise.HONKAI_ZZZ,
                    "DRAGONS AND THUNDER OF THE LUOFU",
                    "High Elder Vidyadhara & Lightning General",
                    CompanionProfile.DAN_HENG_IL, CompanionProfile.JING_YUAN,
                    wp(SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR, Items.NETHERITE_SWORD),
                    wp(SystemItems.LIGHT_CONE_BEFORE_DAWN, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.DAN_HENG, CompanionProfile.TINGYUN, CompanionProfile.QINGQUE),
                    List.of(wp(SystemItems.LIGHT_CONE_BIRTH_OF_THE_SELF, Items.IRON_SWORD), wp(SystemItems.LIGHT_CONE_DANCE_DANCE_DANCE, Items.IRON_SWORD)),
                    0xFF00897B, 0x66004D40,
                    "DRAGON & LIGHTNING 🐉⚡ 5★",
                    "\"El Señor del Rayo y las aguas del Gran Anciano Vidyadhara barren cualquier aflicción.\""
            ),
            new BannerPhase(
                    15, Franchise.HONKAI_ZZZ,
                    "WHIRLWIND AND CELESTIAL MELODY",
                    "Relentless General of Yaoqing & Halovian Singer",
                    CompanionProfile.FEIXIAO, CompanionProfile.ROBIN,
                    wp(SystemItems.LIGHT_CONE_PASSING_SHORE, Items.NETHERITE_SWORD),
                    wp(SystemItems.FREEDOM_SWORN, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.MARCH_7TH, CompanionProfile.ASTA, CompanionProfile.GALLAGHER),
                    List.of(wp(SystemItems.LIGHT_CONE_DANCE_DANCE_DANCE, Items.IRON_SWORD), wp(SystemItems.THE_FLUTE, Items.IRON_SWORD)),
                    0xFF00ACC1, 0x66006064,
                    "HUNT & HARMONY 🌪🕊 5★",
                    "\"¡Carga sin miedo! Las alas de Halovian bendicen cada golpe triunfante de la Caza.\""
            ),
            new BannerPhase(
                    16, Franchise.HONKAI_ZZZ,
                    "GOLDEN FORTUNE AND IPC INVESTMENTS",
                    "Corporation Strategist & Reclamation Director",
                    CompanionProfile.AVENTURINE, CompanionProfile.TOPAZ,
                    wp(SystemItems.UNBREAKABLE_FAITH, Items.SHIELD),
                    wp(SystemItems.LIGHT_CONE_BEFORE_DAWN, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.MARCH_7TH, CompanionProfile.XUEYI, CompanionProfile.LYNX),
                    List.of(wp(SystemItems.LIGHT_CONE_DEFENSE, Items.IRON_SWORD), wp(SystemItems.LIGHT_CONE_GOOD_NIGHT_SLEEP_WELL, Items.IRON_SWORD)),
                    0xFFFFB300, 0x66FF6F00,
                    "FORTUNE & COMMERCE 🎲📈 5★",
                    "\"Apuesta todo al siete... Numby y las inversiones de la CPI garantizan dividendos.\""
            ),
            new BannerPhase(
                    17, Franchise.HONKAI_ZZZ,
                    "MASKS AND THE ARCANE LOOM",
                    "Masked Fool & Keeper of Memories",
                    CompanionProfile.SPARKLE, CompanionProfile.BLACK_SWAN,
                    wp(SystemItems.LIGHT_CONE_MILKY_WAY, Items.TOTEM_OF_UNDYING),
                    wp(SystemItems.LIGHT_CONE_PATIENCE, Items.TOTEM_OF_UNDYING),
                    List.of(CompanionProfile.QINGQUE, CompanionProfile.HERTA, CompanionProfile.SERVAL),
                    List.of(wp(SystemItems.LIGHT_CONE_POST_OP_CONVERSATION, Items.IRON_SWORD), wp(SystemItems.LIGHT_CONE_EYES_OF_THE_PREY, Items.IRON_SWORD)),
                    0xFFBA68C8, 0x667B1FA2,
                    "QUANTUM & REMEMBRANCE 🎭🃏 5★",
                    "\"El juego de máscaras confunde al mundo mientras las cartas del arcano tejen el porvenir.\""
            ),
            new BannerPhase(
                    18, Franchise.HONKAI_ZZZ,
                    "CHANTS OF ORDER AND TALISMANS OF ABUNDANCE",
                    "Family Head & Ten-Lords Commission Judge",
                    CompanionProfile.SUNDAY, CompanionProfile.HUOHUO,
                    wp(SystemItems.ASRAHAN_HOLY_SWORD, Items.NETHERITE_SWORD),
                    wp(SystemItems.LIGHT_CONE_PASSING_SHORE, Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.NATASHA, CompanionProfile.GALLAGHER, CompanionProfile.TINGYUN),
                    List.of(wp(SystemItems.LIGHT_CONE_POST_OP_CONVERSATION, Items.IRON_SWORD), wp(SystemItems.LIGHT_CONE_DANCE_DANCE_DANCE, Items.IRON_SWORD)),
                    0xFFFFD54F, 0x66FFA000,
                    "ORDER & ABUNDANCE 🕊👻 5★",
                    "\"Cánticos celestiales de juicio sagrado y talismanes que purifican a los espíritus errantes.\""
            ),
            new BannerPhase(
                    19, Franchise.HONKAI_ZZZ,
                    "HEALING DRAGONESS AND SVAROG PROTOCOLS",
                    "Healer of the Luofu & Guardian of Belobog",
                    CompanionProfile.BAILU, CompanionProfile.CLARA,
                    wp(SystemItems.LIGHT_CONE_PASSING_SHORE, Items.NETHERITE_SWORD),
                    wp(SystemItems.WOLFS_GRAVESTONE, Items.NETHERITE_AXE),
                    List.of(CompanionProfile.HERTA, CompanionProfile.NATASHA, CompanionProfile.SERVAL),
                    List.of(wp(SystemItems.LIGHT_CONE_BIRTH_OF_THE_SELF, Items.IRON_SWORD), wp(SystemItems.THE_FLUTE, Items.IRON_SWORD)),
                    0xFF4FC3F7, 0x660288D1,
                    "ABUNDANCE & SVAROG 🐉🤖 5★",
                    "\"¡Bailu prepara infusiones de bienestar mientras Svarog activa el fuego defensivo!\""
            ),
            new BannerPhase(
                    20, Franchise.HONKAI_ZZZ,
                    "GENIUS RESEARCH & HYPERSONIC BLADE",
                    "Society Genius #81 & Section 6 Chief",
                    CompanionProfile.RUAN_MEI, CompanionProfile.HOSHIMI_MIYABI,
                    wp(SystemItems.PRIMORDIAL_JADE_CUTTER, Items.DIAMOND_SWORD),
                    wp(SystemItems.MISTSPLITTER_REFORGED, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.ANBY_DEMARA, CompanionProfile.NICOLE_DEMARA, CompanionProfile.MARCH_7TH),
                    List.of(wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD), wp(SystemItems.PLUM_BLOSSOM_SWORD, Items.IRON_SWORD)),
                    0xFF80CBC4, 0x66004D40,
                    "GENIUS & SECTION 6 🌸⚔ 5★",
                    "\"Creaciones biológicas del Genio 81 y los tajos a velocidad absoluta de la Sección 6.\""
            ),
            new BannerPhase(
                    21, Franchise.HONKAI_ZZZ,
                    "GLACIAL TIDES HUNT & SHADOW RODENT",
                    "Victoria Housekeeping & Criminal Consultant",
                    CompanionProfile.ELLEN_JOE, CompanionProfile.JANE_DOE,
                    wp(SystemItems.PRIMORDIAL_JADE_CUTTER, Items.DIAMOND_SWORD),
                    wp(SystemItems.SHADOW_DAGGERS, Items.SHEARS),
                    List.of(CompanionProfile.NICOLE_DEMARA, CompanionProfile.ANBY_DEMARA, CompanionProfile.BILLY_KID),
                    List.of(wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD), wp(SystemItems.SHADOW_DAGGERS, Items.SHEARS)),
                    0xFF00BCD4, 0x66006064,
                    "VICTORIA & ERIDU 🦈🐀 5★",
                    "\"Hora extra de Victoria Housekeeping y las trampas elusivas de los callejones de Eridu.\""
            ),
            new BannerPhase(
                    22, Franchise.HONKAI_ZZZ,
                    "SPECIAL BALLISTICS & COLISEUM RULER",
                    "Special Response Security & Sons of Calydon",
                    CompanionProfile.ZHU_YUAN, CompanionProfile.CAESAR_KING,
                    wp(SystemItems.VERDICT, Items.NETHERITE_AXE),
                    wp(SystemItems.UNBREAKABLE_FAITH, Items.SHIELD),
                    List.of(CompanionProfile.LUCY, CompanionProfile.PIPER_WHEEL, CompanionProfile.CORIN_WICKES),
                    List.of(wp(SystemItems.THE_CATCH, Items.IRON_SWORD), wp(SystemItems.THE_FLUTE, Items.IRON_SWORD)),
                    0xFFE53935, 0x66B71C1C,
                    "SECURITY & COLISEUM 🛡🔫 5★",
                    "\"¡Fuego táctico de la policía de Eridu y el impacto arrollador de los Hijos de Calidón!\""
            ),
            new BannerPhase(
                    23, Franchise.HONKAI_ZZZ,
                    "NITRO COCKTAIL & BELOBOG SEISMIC MIGHT",
                    "Calydon Mixologist & Belobog President",
                    CompanionProfile.BURNICE_WHITE, CompanionProfile.KOLEDA_BELOBOG,
                    wp(SystemItems.STAFF_OF_HOMA, Items.NETHERITE_SWORD),
                    wp(SystemItems.REDHORN_STONETHRESHER, Items.NETHERITE_AXE),
                    List.of(CompanionProfile.PIPER_WHEEL, CompanionProfile.LUCY, CompanionProfile.SOUKAKU),
                    List.of(wp(SystemItems.PLUM_BLOSSOM_SWORD, Items.IRON_SWORD), wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD)),
                    0xFFFF5722, 0x66D84315,
                    "FIRE & FORGE 🔥🔨 5★",
                    "\"¡Rondas de cóctel nitro bien ardientes y los martillazos demoledores de Belobog!\""
            )
    };

    private static final BannerPhase[] SOLO_LEVELING_PHASES = new BannerPhase[]{
            new BannerPhase(
                    24, Franchise.SOLO_LEVELING,
                    "THE MONARCH AND THE CRIMSON KNIGHT",
                    "Rank S Hunter & Crimson Shadow Blade",
                    CompanionProfile.SUNG_JINWOO, CompanionProfile.IGRIS,
                    wp(SystemItems.SHADOW_DAGGERS, Items.SHEARS),
                    wp(SystemItems.WOLFS_GRAVESTONE, Items.NETHERITE_AXE),
                    List.of(CompanionProfile.CHA_HAEIN, CompanionProfile.BENNETT),
                    List.of(wp(SystemItems.BLACK_DRAGON_SCYTHE, Items.NETHERITE_AXE), wp(SystemItems.RISING_DRAGON_PILL, Items.GOLDEN_APPLE)),
                    0xFF7B1FA2, 0x66311B92,
                    "SHADOWS & BLOOD 👑🗡 5★",
                    "\"\"Arise...\" Entre la muerte y el abismo, el Monarca y su Caballero reclaman el trono.\""
            ),
            new BannerPhase(
                    25, Franchise.SOLO_LEVELING,
                    "MECHANICAL MASTERY & VORTEX OF SHADOWS",
                    "Belobog Engineer & Ant Monarch of Shadows",
                    CompanionProfile.GRACE_HOWARD, CompanionProfile.BERU,
                    wp(SystemItems.KEY_OF_KHAJ_NISUT, Items.DIAMOND_SWORD),
                    wp(SystemItems.SHADOW_DAGGERS, Items.SHEARS),
                    List.of(CompanionProfile.CORIN_WICKES, CompanionProfile.CHA_HAEIN),
                    List.of(wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD), wp(SystemItems.BLACK_DRAGON_SCYTHE, Items.NETHERITE_AXE)),
                    0xFF3F51B5, 0x661A237E,
                    "LIGHTNING & VORTEX ⚡🐜 5★",
                    "\"¡Clavadoras de alta precisión y el chillido desgarrador del Rey Ant de las Sombras!\""
            )
    };

    private static final BannerPhase[] JJK_PHASES = new BannerPhase[]{
            new BannerPhase(
                    26, Franchise.JUJUTSU_KAISEN,
                    "INFINITY & THE KING OF CURSES",
                    "The Strongest Jujutsu Sorcerer & Malevolent Shrine",
                    CompanionProfile.GOJO_SATORU, CompanionProfile.SUKUNA,
                    wp(SystemItems.UNBREAKABLE_FAITH, Items.NETHER_STAR),
                    wp(SystemItems.HEAVEN_SHAKING_SWORD, Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.YUJI_ITADORI, CompanionProfile.MEGUMI),
                    List.of(wp(SystemItems.TRANSCENDENT_THORN, Items.BOW), wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD)),
                    0xFF00ACC1, 0x66006064,
                    "INFINITY & SHRINE 👁🔥 5★",
                    "\"\"Don't worry, I'm the strongest.\" vs \"Know your place in my Malevolent Shrine.\"\""
            )
    };

    private static final BannerPhase[] FATE_PHASES = new BannerPhase[]{
            new BannerPhase(
                    27, Franchise.FATE,
                    "THE CROWN AND THE KING'S TREASURY",
                    "King of Knights & King of Heroes",
                    CompanionProfile.ARTORIA_PENDRAGON, CompanionProfile.GILGAMESH,
                    wp(SystemItems.ASRAHAN_HOLY_SWORD, Items.NETHERITE_SWORD),
                    wp(SystemItems.RADIANT_LIGHT_SWORD, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.ARCHER, CompanionProfile.CHA_HAEIN),
                    List.of(wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD), wp(SystemItems.SHIYUAN_SWORD, Items.IRON_SWORD)),
                    0xFFFFD700, 0x66FF8F00,
                    "EXCALIBUR & BABYLON ⚔👑 5★",
                    "\"\"Are you my Master?\" Primordial treasures of the Golden King await the chosen.\""
            )
    };

    private static final BannerPhase[] DEMON_SLAYER_PHASES = new BannerPhase[]{
            new BannerPhase(
                    28, Franchise.DEMON_SLAYER,
                    "SET YOUR HEART ABLAZE AND SUN DANCE!",
                    "Flame Hashira & Dance of the Fire God",
                    CompanionProfile.RENGOKU, CompanionProfile.TANJIRO,
                    wp(SystemItems.PLUM_BLOSSOM_SWORD, Items.IRON_SWORD),
                    wp(SystemItems.SKY_DEMON_SWORD, Items.IRON_SWORD),
                    List.of(CompanionProfile.NEZUKO, CompanionProfile.CHA_HAEIN),
                    List.of(wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD), wp(SystemItems.SHIYUAN_SWORD, Items.IRON_SWORD)),
                    0xFFFF5722, 0x66BF360C,
                    "FLAME & SUN DANCE 🔥🌸 5★",
                    "\"Set your heart ablaze! The Dance of the Fire God and Purgatory shall never waver!\""
            )
    };

    private static final BannerPhase[] WUTHERING_WAVES_PHASES = new BannerPhase[]{
            new BannerPhase(
                    29, Franchise.WUTHERING_WAVES,
                    "DESTINY OF JUE'S DRAGON & PHOENIX ARDOR",
                    "Magistrate of Jinzhou & Mentor of Jinzhou",
                    CompanionProfile.JINSHI, CompanionProfile.CHANGLI,
                    wp(SystemItems.SOUL_SOLID, Items.NETHERITE_SWORD),
                    wp(SystemItems.MISTSPLITTER_REFORGED, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.SANHUA, CompanionProfile.DANJIN, CompanionProfile.CHIXIA),
                    List.of(wp(SystemItems.THE_FLUTE, Items.IRON_SWORD), wp(SystemItems.SHIYUAN_SWORD, Items.IRON_SWORD)),
                    0xFFFFD54F, 0x66FF8F00,
                    "SPECTRO & FUSION 🐉🔥 5★",
                    "\"Jué's celestial light and the phoenix's fiery feathers guide Jinzhou's prosperity.\""
            ),
            new BannerPhase(
                    30, Franchise.WUTHERING_WAVES,
                    "TEMPEST WIND & THREADS OF THUNDER",
                    "Midnight Rangers General & Hidden Researcher",
                    CompanionProfile.JIYAN, CompanionProfile.YINLIN,
                    wp(SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR, Items.NETHERITE_SWORD),
                    wp(SystemItems.ENGULFING_LIGHTNING, Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.SANHUA, CompanionProfile.DANJIN),
                    List.of(wp(SystemItems.THE_CATCH, Items.IRON_SWORD), wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD)),
                    0xFF00E676, 0x6600B0FF,
                    "AERO & ELECTRO 🌪⚡ 5★",
                    "\"El dragón verde Qingloong ruge al unísono con las marionetas eléctricas de Yinlin.\""
            ),
            new BannerPhase(
                    31, Franchise.WUTHERING_WAVES,
                    "SONG OF THE STELLAR SEA & THORN BLOOM",
                    "Stellar Domain Guardian & Crimson Thorn Sprout",
                    CompanionProfile.SHOREKEEPER, CompanionProfile.CAMELLYA,
                    wp(SystemItems.LIGHT_CONE_PASSING_SHORE, Items.NETHERITE_SWORD),
                    wp(SystemItems.CRIMSON_MOONS_SEMBLANCE, Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.CHIXIA, CompanionProfile.BAIZHI),
                    List.of(wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD), wp(SystemItems.THE_FLUTE, Items.IRON_SWORD)),
                    0xFF00E5FF, 0x660097A7,
                    "TETHYS & THORNS 🌌🌸 5★",
                    "\"En los confines de Tethys, cada estrella susurra mientras las espinas rojas florecen.\""
            ),
            new BannerPhase(
                    32, Franchise.WUTHERING_WAVES,
                    "PRIMORDIAL RESONANCE & TAOIST SERENITY",
                    "Solaris-3 Primordial Resonator & Taoist of the Wind",
                    CompanionProfile.ROVER, CompanionProfile.JIANXIN,
                    wp(SystemItems.ASRAHAN_HOLY_SWORD, Items.NETHERITE_SWORD),
                    wp(SystemItems.UNBREAKABLE_FAITH, Items.SHIELD),
                    List.of(CompanionProfile.SANHUA, CompanionProfile.DANJIN),
                    List.of(wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD), wp(SystemItems.SHIYUAN_SWORD, Items.IRON_SWORD)),
                    0xFFE91E63, 0x66AD1457,
                    "RESONANCE & TAI CHI ☯🍃 5★",
                    "\"El despertar del Resonador Primordial fluye con el equilibrio del Qi celestial.\""
            ),
            new BannerPhase(
                    33, Franchise.WUTHERING_WAVES,
                    "THE GHOST PACK & PLUSH GARDEN",
                    "Ghost Hounds Leader & Black Shores Consultant",
                    CompanionProfile.CALCHARO, CompanionProfile.ENCORE,
                    wp(SystemItems.WOLFS_GRAVESTONE, Items.NETHERITE_AXE),
                    wp(SystemItems.MISTSPLITTER_REFORGED, Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.CHIXIA, CompanionProfile.BAIZHI, CompanionProfile.SANHUA),
                    List.of(wp(SystemItems.THE_BLACK_SWORD, Items.IRON_SWORD), wp(SystemItems.THE_CATCH, Items.IRON_SWORD)),
                    0xFF7C4DFF, 0x66512DA8,
                    "ELECTRO & PLUSHIES 🐺🐑 5★",
                    "\"La ferocidad implacable de los Ghost Hounds se une a las travesuras de peluche de Cosmos.\""
            )
    };

    private static final BannerPhase[] NTE_PHASES = new BannerPhase[]{
            new BannerPhase(
                    34, Franchise.NTE,
                    "HETHEREAU URBAN DISTORTION",
                    "Anomaly Researcher & Twilight Spectral Maiden",
                    CompanionProfile.NANALLY, CompanionProfile.LACRIMOSA,
                    wp(SystemItems.DEATH_KING_CLOCK, Items.CLOCK),
                    wp(SystemItems.SHADOW_DAGGERS, Items.SHEARS),
                    List.of(CompanionProfile.MINT, CompanionProfile.NOELLE),
                    List.of(wp(SystemItems.THE_CATCH, Items.IRON_SWORD), wp(SystemItems.FLEUVE_CENDRE_FERRYMAN, Items.IRON_SWORD)),
                    0xFFFFB74D, 0x66E65100,
                    "HETHEREAU 🔮 5★",
                    "\"Las anomalías urbanas ocultan la verdad detrás de las sombras de Hethereau.\""
            )
    };

    public static final List<BannerPhase> ALL_ROTATING_PHASES = List.of(
            GENSHIN_PHASES[0],
            SOLO_LEVELING_PHASES[0],
            HONKAI_PHASES[0],
            JJK_PHASES[0],
            GENSHIN_PHASES[1],
            FATE_PHASES[0],
            WUTHERING_WAVES_PHASES[0],
            HONKAI_PHASES[9],
            DEMON_SLAYER_PHASES[0],
            HONKAI_PHASES[1],
            GENSHIN_PHASES[2],
            HONKAI_PHASES[2],
            WUTHERING_WAVES_PHASES[1],
            HONKAI_PHASES[3],
            GENSHIN_PHASES[5],
            WUTHERING_WAVES_PHASES[2],
            HONKAI_PHASES[8],
            NTE_PHASES[0],
            GENSHIN_PHASES[3],
            HONKAI_PHASES[4],
            HONKAI_PHASES[10],
            GENSHIN_PHASES[4],
            HONKAI_PHASES[11],
            HONKAI_PHASES[5],
            GENSHIN_PHASES[7],
            WUTHERING_WAVES_PHASES[3],
            HONKAI_PHASES[6],
            GENSHIN_PHASES[6],
            WUTHERING_WAVES_PHASES[4],
            SOLO_LEVELING_PHASES[1],
            HONKAI_PHASES[7],
            GENSHIN_PHASES[8],
            GENSHIN_PHASES[9],
            GENSHIN_PHASES[10]
    );

    private static long bannerEpochTick = 0L;

    public static void setBannerEpochTick(long epochTick) {
        bannerEpochTick = Math.max(0L, epochTick);
    }

    public static long getBannerEpochTick() {
        return bannerEpochTick;
    }

    public static long getEffectiveWorldTime(long worldDayTime) {
        long elapsed = worldDayTime - bannerEpochTick;
        return Math.max(0L, elapsed);
    }

    private PromotionalBanner() {}

    public static BannerPhase getActivePhase(long worldDayTime) {
        return getActivePhase(worldDayTime, 0);
    }

    public static BannerPhase getActivePhase(long worldDayTime, int bannerIndex) {
        return getActivePhaseByEffectiveTicks(getEffectiveWorldTime(worldDayTime), bannerIndex);
    }

    public static BannerPhase getActivePhaseByEffectiveTicks(long effTime, int bannerIndex) {
        if (ALL_ROTATING_PHASES.isEmpty()) return GENSHIN_PHASES[0];
        long currentDay = Math.max(0L, effTime) / MC_DAY_TICKS;
        int phaseIndex = (int) ((currentDay / BANNER_CYCLE_DAYS) % ALL_ROTATING_PHASES.size());
        BannerPhase basePhase = ALL_ROTATING_PHASES.get(phaseIndex);

        if (bannerIndex == 1) {
            if (basePhase.secondaryFeaturedCharacter() != null) {
                return new BannerPhase(
                        basePhase.phaseIndex(),
                        basePhase.franchise(),
                        basePhase.bannerTitle(),
                        basePhase.bannerSubtitle(),
                        basePhase.secondaryFeaturedCharacter(),
                        basePhase.featuredCharacter(),
                        basePhase.secondaryFeaturedWeapon(),
                        basePhase.primaryFeaturedWeapon(),
                        basePhase.rateUp4StarCharacters(),
                        basePhase.rateUp4StarWeapons(),
                        basePhase.themeColor(),
                        basePhase.themeGlowColor(),
                        basePhase.secondaryFeaturedCharacter().element() + " 5★",
                        basePhase.archonQuote()
                );
            }
        }
        return basePhase;
    }

    public static BannerPhase getNextPhase(long worldDayTime) {
        if (ALL_ROTATING_PHASES.isEmpty()) return GENSHIN_PHASES[0];
        long effTime = getEffectiveWorldTime(worldDayTime);
        long currentDay = effTime / MC_DAY_TICKS;
        int nextIndex = (int) (((currentDay / BANNER_CYCLE_DAYS) + 1) % ALL_ROTATING_PHASES.size());
        return ALL_ROTATING_PHASES.get(nextIndex);
    }

    public static BannerPhase getHsrActivePhase(long worldDayTime) {
        if (HONKAI_PHASES.length == 0) return HONKAI_PHASES[0];
        long effTime = getEffectiveWorldTime(worldDayTime);
        long currentDay = effTime / MC_DAY_TICKS;
        int phaseIndex = (int) ((currentDay / BANNER_CYCLE_DAYS) % HONKAI_PHASES.length);
        return HONKAI_PHASES[phaseIndex];
    }

    public static BannerPhase getNextHsrPhase(long worldDayTime) {
        if (HONKAI_PHASES.length == 0) return HONKAI_PHASES[0];
        long effTime = getEffectiveWorldTime(worldDayTime);
        long currentDay = effTime / MC_DAY_TICKS;
        int nextIndex = (int) (((currentDay / BANNER_CYCLE_DAYS) + 1) % HONKAI_PHASES.length);
        return HONKAI_PHASES[nextIndex];
    }

    public static BannerPhase getActiveWeaponPhase(long worldDayTime) {
        return getActiveWeaponPhaseByEffectiveTicks(getEffectiveWorldTime(worldDayTime));
    }

    public static BannerPhase getActiveWeaponPhaseByEffectiveTicks(long effTime) {
        BannerPhase b1 = getActivePhaseByEffectiveTicks(effTime, 0);
        BannerPhase b2 = getActivePhaseByEffectiveTicks(effTime, 1);
        return new BannerPhase(
                0,
                Franchise.GENSHIN,
                "EPITOME INVOCATION",
                "Cosmic Plane Signature Weapons",
                b1.featuredCharacter(),
                b2.featuredCharacter(),
                b1.primaryFeaturedWeapon(),
                b2.primaryFeaturedWeapon(),
                b1.rateUp4StarCharacters(),
                b1.rateUp4StarWeapons(),
                0xFFFFD700,
                0x66B8860B,
                "WEAPONS 5★",
                "\"Las armas forjadas por los dioses aguardan a sus elegidos.\""
        );
    }

    public static int getPhaseNumber(long worldDayTime) {
        return getPhaseNumberByEffectiveTicks(getEffectiveWorldTime(worldDayTime));
    }

    public static int getPhaseNumberByEffectiveTicks(long effTime) {
        long currentDay = Math.max(0L, effTime) / MC_DAY_TICKS;
        return (int) ((currentDay / BANNER_CYCLE_DAYS) % ALL_ROTATING_PHASES.size()) + 1;
    }

    public static int getTotalPhases() {
        return ALL_ROTATING_PHASES.size();
    }

    public static long getCurrentDay(long worldDayTime) {
        long effTime = getEffectiveWorldTime(worldDayTime);
        return effTime / MC_DAY_TICKS;
    }

    public static int getPhaseCount(Franchise franchise) {
        return ALL_ROTATING_PHASES.size();
    }

    public static int getPhaseCount() {
        return ALL_ROTATING_PHASES.size();
    }

    public static BannerPhase getPhaseByIndex(Franchise franchise, int index) {
        int idx = Math.floorMod(index, ALL_ROTATING_PHASES.size());
        return ALL_ROTATING_PHASES.get(idx);
    }

    public static BannerPhase getPhaseByIndex(int index) {
        int idx = Math.floorMod(index, ALL_ROTATING_PHASES.size());
        return ALL_ROTATING_PHASES.get(idx);
    }

    public static long getTicksUntilNextPhase(long worldDayTime) {
        long effTime = getEffectiveWorldTime(worldDayTime);
        long currentCycleTicks = effTime % BANNER_CYCLE_TICKS;
        return BANNER_CYCLE_TICKS - currentCycleTicks;
    }

    public static String getFormattedTimeRemaining(long worldDayTime) {
        long ticks = getTicksUntilNextPhase(worldDayTime);
        long days = ticks / MC_DAY_TICKS;
        long hours = (ticks % MC_DAY_TICKS) / 1000L;
        return days + " d. " + hours + " h.";
    }
}
