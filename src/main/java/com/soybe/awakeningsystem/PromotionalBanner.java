package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.entity.CompanionProfile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Supplier;

/**
 * Sistema Multiverso de Banners Promocionales de Gacha (6 Franquicias).
 * Rotación dinámica basada en días de Minecraft (22 días MC = 528,000 ticks).
 */
public final class PromotionalBanner {
    public static final long MC_DAY_TICKS = 24000L;
    public static final long BANNER_CYCLE_DAYS = 22L;
    public static final long BANNER_CYCLE_TICKS = BANNER_CYCLE_DAYS * MC_DAY_TICKS;

    public enum Franchise {
        GENSHIN(0, "Genshin Impact", "TEYVAT", "§b", 0xFF64B5F6, 0x661976D2),
        HONKAI_ZZZ(1, "Honkai & ZZZ", "HOYOVERSE", "§d", 0xFFBA68C8, 0x667B1FA2),
        SOLO_LEVELING(2, "Solo Leveling", "SOMBRAS", "§5", 0xFFAB47BC, 0x664A148C),
        JUJUTSU_KAISEN(3, "Jujutsu Kaisen", "HECHICERÍA", "§3", 0xFF26C6DA, 0x6600838F),
        FATE(4, "Fate Series", "SANTO GRIAL", "§e", 0xFFFFD54F, 0x66FF8F00),
        DEMON_SLAYER(5, "Demon Slayer", "CAZADORES", "§c", 0xFFFF7043, 0x66D32F2F),
        WUTHERING_WAVES(6, "Wuthering Waves", "SOLARIS-3", "§a", 0xFF81C784, 0x662E7D32);

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

    // ==========================================
    // 1. GENSHIN IMPACT (6 FASES)
    // ==========================================
    private static final BannerPhase[] GENSHIN_PHASES = new BannerPhase[]{
            new BannerPhase(
                    0, Franchise.GENSHIN,
                    "REINO DE LA SERENIDAD",
                    "Plano de la Eutimia & Luz de la Eternidad",
                    CompanionProfile.RAIDEN_SHOGUN, null,
                    () -> SystemItems.ENGULFING_LIGHTNING.isBound() ? new ItemStack(SystemItems.ENGULFING_LIGHTNING.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.MISTSPLITTER_REFORGED.isBound() ? new ItemStack(SystemItems.MISTSPLITTER_REFORGED.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFBA68C8, 0x667B1FA2,
                    "ELECTRO ⚡ 5★",
                    "\"La soledad es el precio que se paga por la Eternidad.\""
            ),
            new BannerPhase(
                    1, Franchise.GENSHIN,
                    "FLORACIÓN FUGAZ",
                    "Danza de las Mariposas & Fuego del Más Allá",
                    CompanionProfile.HU_TAO, null,
                    () -> SystemItems.STAFF_OF_HOMA.isBound() ? new ItemStack(SystemItems.STAFF_OF_HOMA.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : new ItemStack(Items.NETHERITE_AXE),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFFF7043, 0x66D32F2F,
                    "PYRO 🔥 5★",
                    "\"¡Si la vida es un viaje efímero, que arda como una mariposa de fuego!\""
            ),
            new BannerPhase(
                    2, Franchise.GENSHIN,
                    "SOLEDAD DE LA NOBLEZA",
                    "Dominador del Firmamento & Pilares de Piedra",
                    CompanionProfile.ZHONGLI, null,
                    () -> SystemItems.VORTEX_VANQUISHER.isBound() ? new ItemStack(SystemItems.VORTEX_VANQUISHER.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.REDHORN_STONETHRESHER.isBound() ? new ItemStack(SystemItems.REDHORN_STONETHRESHER.get()) : new ItemStack(Items.NETHERITE_AXE),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.SHIYUAN_SWORD.isBound() ? new ItemStack(SystemItems.SHIYUAN_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFFFD54F, 0x66FFA000,
                    "GEO ⛰ 5★",
                    "\"El vino de osmanto sabe igual... pero ¿dónde están aquellos con quienes compartirlo?\""
            ),
            new BannerPhase(
                    3, Franchise.GENSHIN,
                    "ODA A LAS AGUAS ONDULANTES",
                    "Reina del Escenario & Drama de Fontaine",
                    CompanionProfile.FURINA, null,
                    () -> SystemItems.ABSOLUTION.isBound() ? new ItemStack(SystemItems.ABSOLUTION.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    () -> SystemItems.KEY_OF_KHAJ_NISUT.isBound() ? new ItemStack(SystemItems.KEY_OF_KHAJ_NISUT.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF4FC3F7, 0x660288D1,
                    "HYDRO 💧 5★",
                    "\"¡Que el telón se alce y el mundo entero contemple este grandioso drama!\""
            ),
            new BannerPhase(
                    4, Franchise.GENSHIN,
                    "CABALLEROS DE FAVONIUS",
                    "El León del Viento y el Halcón Nocturno",
                    CompanionProfile.DILUC, CompanionProfile.JEAN,
                    () -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : new ItemStack(Items.NETHERITE_AXE),
                    () -> SystemItems.SKYWARD_BLADE.isBound() ? new ItemStack(SystemItems.SKYWARD_BLADE.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE),
                    List.of(
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFE57373, 0x66C62828,
                    "FAVONIUS 🦅 5★",
                    "\"Por la libertad de Mondstadt, mantendremos la guardia en las sombras.\""
            ),
            new BannerPhase(
                    5, Franchise.GENSHIN,
                    "SABIDURÍA DE LA FLORESTA",
                    "Sueños de Sumeru & Poema del Viento Errante",
                    CompanionProfile.NAHIDA, CompanionProfile.KAZUHA,
                    () -> SystemItems.FREEDOM_SWORN.isBound() ? new ItemStack(SystemItems.FREEDOM_SWORN.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    () -> SystemItems.PRIMORDIAL_JADE_CUTTER.isBound() ? new ItemStack(SystemItems.PRIMORDIAL_JADE_CUTTER.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF81C784, 0x662E7D32,
                    "DENDRO & ANEMO 🍃 5★",
                    "\"Las hojas caen al compás de la brisa, susurrando los secretos del Árbol Irminsul.\""
            ),
            new BannerPhase(
                    6, Franchise.GENSHIN,
                    "INVITACIÓN DE LOS VIENTOS",
                    "Guardián Yaksha & Danza del Conquistador del Mal",
                    CompanionProfile.XIAO, null,
                    () -> SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.isBound() ? new ItemStack(SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.SKYWARD_BLADE.isBound() ? new ItemStack(SystemItems.SKYWARD_BLADE.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF00BFA5, 0x66004D40,
                    "ANEMO 🍃 5★",
                    "\"Si despierta tu miedo, grita mi nombre: Xiao.\""
            ),
            new BannerPhase(
                    7, Franchise.GENSHIN,
                    "CENIZAS DE LA HOGUERA",
                    "La Sota de los Fatui & Luna Carmesí del Destino",
                    CompanionProfile.ARLECCHINO, null,
                    () -> SystemItems.CRIMSON_MOONS_SEMBLANCE.isBound() ? new ItemStack(SystemItems.CRIMSON_MOONS_SEMBLANCE.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.ABSOLUTION.isBound() ? new ItemStack(SystemItems.ABSOLUTION.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE),
                    List.of(
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFB71C1C, 0x667F0000,
                    "PYRO / LUNA CARMESÍ 🌙 5★",
                    "\"En la Casa de la Hoguera no hay lugar para la debilidad. Llámame Padre.\""
            ),
            new BannerPhase(
                    8, Franchise.GENSHIN,
                    "LLAMAS DE LA GUERRA",
                    "Arconte Pyro de Natlan & Resurrección Solar",
                    CompanionProfile.MAVUIKA, null,
                    () -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : new ItemStack(Items.NETHERITE_AXE),
                    () -> SystemItems.STAFF_OF_HOMA.isBound() ? new ItemStack(SystemItems.STAFF_OF_HOMA.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE),
                    List.of(
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFE65100, 0x66BF360C,
                    "PYRO 🔥 5★",
                    "\"¡El fuego de Natlan nunca se extinguirá mientras lata el corazón de un guerrero!\""
            ),
            new BannerPhase(
                    9, Franchise.GENSHIN,
                    "SINFONÍA DE LAS TRES LUNAS",
                    "Diosa Trilunar de Teyvat & Vigilia Celestial",
                    CompanionProfile.COLUMBINA, null,
                    () -> SystemItems.MISTSPLITTER_REFORGED.isBound() ? new ItemStack(SystemItems.MISTSPLITTER_REFORGED.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    () -> SystemItems.SKYWARD_BLADE.isBound() ? new ItemStack(SystemItems.SKYWARD_BLADE.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFBA68C8, 0x667B1FA2,
                    "LUNAR 🌙 5★",
                    "\"Las tres lunas cantan en la oscuridad eterna... escucha su melodía sagrada.\""
            ),
            new BannerPhase(
                    10, Franchise.GENSHIN,
                    "DUELO BAJO LA LLOVIZNA",
                    "Duelista Judicial Invicta & Rayo Cazador",
                    CompanionProfile.CLORINDE, null,
                    () -> SystemItems.ABSOLUTION.isBound() ? new ItemStack(SystemItems.ABSOLUTION.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    () -> SystemItems.MISTSPLITTER_REFORGED.isBound() ? new ItemStack(SystemItems.MISTSPLITTER_REFORGED.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE),
                    List.of(
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF1E88E5, 0x660D47A1,
                    "ELECTRO ⚡ 5★",
                    "\"La justicia no admite vacilaciones. Prepara tu espada y tu defensa.\""
            ),
            new BannerPhase(
                    11, Franchise.GENSHIN,
                    "RITMO Y CAZA DE NATLAN",
                    "Cazador de Malpais & Forjadora de Nombres",
                    CompanionProfile.KINICH, CompanionProfile.XILONEN,
                    () -> SystemItems.PRIMORDIAL_JADE_CUTTER.isBound() ? new ItemStack(SystemItems.PRIMORDIAL_JADE_CUTTER.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    () -> SystemItems.REDHORN_STONETHRESHER.isBound() ? new ItemStack(SystemItems.REDHORN_STONETHRESHER.get()) : new ItemStack(Items.NETHERITE_AXE),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.NOELLE),
                    List.of(
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF43A047, 0x661B5E20,
                    "NATLAN 🦎 5★",
                    "\"El cañón de Ajaw ruge y los patines de Xilonen marcan el compás de la batalla.\""
            ),
            new BannerPhase(
                    12, Franchise.GENSHIN,
                    "HORIZONTES DEL PLENILUNIO Y DEL VACÍO",
                    "Vigía de Liyue & Maestra del Abismo Primordial",
                    CompanionProfile.SKIRK, CompanionProfile.GANYU,
                    () -> SystemItems.TRANSCENDENT_THORN.isBound() ? new ItemStack(SystemItems.TRANSCENDENT_THORN.get()) : new ItemStack(Items.BOW),
                    () -> SystemItems.ASRAHAN_HOLY_SWORD.isBound() ? new ItemStack(SystemItems.ASRAHAN_HOLY_SWORD.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF00ACC1, 0x66006064,
                    "ABISMO & CRYO ❄ 5★",
                    "\"Al borde del abismo primordial, hasta la luz de las estrellas se desvanece.\""
            )
    };

    // ==========================================
    // 2. HONKAI: STAR RAIL & ZZZ (3 FASES)
    // ==========================================
    private static final BannerPhase[] HONKAI_PHASES = new BannerPhase[]{
            new BannerPhase(
                    0, Franchise.HONKAI_ZZZ,
                    "PALABRAS EN EL RÍO",
                    "Emanadora de la Nihilidad & Lluvia Carmesí",
                    CompanionProfile.ACHERON, null,
                    () -> SystemItems.LIGHT_CONE_PASSING_SHORE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PASSING_SHORE.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.DOMAIN_OF_SANCTION.isBound() ? new ItemStack(SystemItems.DOMAIN_OF_SANCTION.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF9C27B0, 0x664A148C,
                    "NIHILIDAD ⚡ 5★",
                    "\"Aquellos que pisan el Río del Olvido ya no miran hacia atrás.\""
            ),
            new BannerPhase(
                    1, Franchise.HONKAI_ZZZ,
                    "SINFONÍA DEL DESTINO",
                    "Hilos del Estelaron & Susurro Hipnótico",
                    CompanionProfile.KAFKA, null,
                    () -> SystemItems.LIGHT_CONE_PATIENCE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PATIENCE.get()) : new ItemStack(Items.TOTEM_OF_UNDYING),
                    () -> SystemItems.LIGHT_CONE_MILKY_WAY.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_MILKY_WAY.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.CHA_HAEIN, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF880E4F, 0x664A148C,
                    "ESTELARON 🕷 5★",
                    "\"Escucha el latido de tu corazón... Boom.\""
            ),
            new BannerPhase(
                    2, Franchise.HONKAI_ZZZ,
                    "CAZA DE MAREAS GLACIALES",
                    "Victoria Housekeeping & Depredador de la Calle",
                    CompanionProfile.ELLEN_JOE, null,
                    () -> SystemItems.HERTA_HAMMER.isBound() ? new ItemStack(SystemItems.HERTA_HAMMER.get()) : new ItemStack(Items.NETHERITE_AXE),
                    () -> SystemItems.PRIMORDIAL_JADE_CUTTER.isBound() ? new ItemStack(SystemItems.PRIMORDIAL_JADE_CUTTER.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.CHA_HAEIN),
                    List.of(
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.SHADOW_DAGGERS.isBound() ? new ItemStack(SystemItems.SHADOW_DAGGERS.get()) : new ItemStack(Items.SHEARS)
                    ),
                    0xFF00BCD4, 0x66006064,
                    "ESCARCHA 🦈 5★",
                    "\"Hora extra... qué fastidio. Terminemos con esto rápido.\""
            ),
            new BannerPhase(
                    3, Franchise.HONKAI_ZZZ,
                    "VUELO DE LA LUCIÉRNAGA",
                    "Armadura SAM de la Caballería de Hierro & Mar de Fuego",
                    CompanionProfile.FIREFLY, null,
                    () -> SystemItems.LIGHT_CONE_DREAMS_REST.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_DREAMS_REST.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.LIGHT_CONE_BEFORE_DAWN.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_BEFORE_DAWN.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.CHA_HAEIN),
                    List.of(
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFFF6F00, 0x66E65100,
                    "COMBUSTIÓN 🔥 5★",
                    "\"¡Prenderé fuego a los mares hasta que florezca la última luciérnaga!\""
            ),
            new BannerPhase(
                    4, Franchise.HONKAI_ZZZ,
                    "DRAGONES Y TRUENOS DEL LUOFU",
                    "Gran Anciano Vidyadhara & General del Rayo",
                    CompanionProfile.DAN_HENG_IL, CompanionProfile.JING_YUAN,
                    () -> SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.isBound() ? new ItemStack(SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.LIGHT_CONE_BEFORE_DAWN.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_BEFORE_DAWN.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.MARCH_7TH, CompanionProfile.CHA_HAEIN),
                    List.of(
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF00897B, 0x66004D40,
                    "DRAGÓN & RAYO 🐉 5★",
                    "\"El Señor del Rayo y las aguas del dragón barren cualquier aflicción.\""
            ),
            new BannerPhase(
                    5, Franchise.HONKAI_ZZZ,
                    "TORBELLINO Y MELODÍA CELESTIAL",
                    "General Implacable de Yaoqing & Cantante de Halovian",
                    CompanionProfile.FEIXIAO, CompanionProfile.ROBIN,
                    () -> SystemItems.LIGHT_CONE_PASSING_SHORE.isBound() ? new ItemStack(SystemItems.LIGHT_CONE_PASSING_SHORE.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.FREEDOM_SWORN.isBound() ? new ItemStack(SystemItems.FREEDOM_SWORN.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.MARCH_7TH, CompanionProfile.NOELLE),
                    List.of(
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF00ACC1, 0x66006064,
                    "CACERÍA & ARMONÍA 🌪 5★",
                    "\"¡Carga sin miedo! Las alas de Halovian bendicen cada golpe triunfante.\""
            ),
            new BannerPhase(
                    6, Franchise.HONKAI_ZZZ,
                    "LA MANO DEL DESTINO",
                    "Estratega de la IPC & Fortuna Dorada",
                    CompanionProfile.AVENTURINE, null,
                    () -> SystemItems.UNBREAKABLE_FAITH.isBound() ? new ItemStack(SystemItems.UNBREAKABLE_FAITH.get()) : new ItemStack(Items.SHIELD),
                    () -> SystemItems.KEY_OF_KHAJ_NISUT.isBound() ? new ItemStack(SystemItems.KEY_OF_KHAJ_NISUT.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.MARCH_7TH, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.SHIYUAN_SWORD.isBound() ? new ItemStack(SystemItems.SHIYUAN_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFFFB300, 0x66FF6F00,
                    "FORTUNA 🎲 5★",
                    "\"Apuesta todo al siete... El destino favorece a los que lo arriesgan todo.\""
            ),
            new BannerPhase(
                    7, Franchise.HONKAI_ZZZ,
                    "SOMBRAS Y BALÍSTICA DE ERIDU",
                    "Consultora Criminal & Líder del Equipo Policial Especial",
                    CompanionProfile.JANE_DOE, CompanionProfile.ZHU_YUAN,
                    () -> SystemItems.SHADOW_DAGGERS.isBound() ? new ItemStack(SystemItems.SHADOW_DAGGERS.get()) : new ItemStack(Items.SHEARS),
                    () -> SystemItems.HERTA_HAMMER.isBound() ? new ItemStack(SystemItems.HERTA_HAMMER.get()) : new ItemStack(Items.NETHERITE_AXE),
                    List.of(CompanionProfile.CHA_HAEIN, CompanionProfile.NOELLE),
                    List.of(
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFE53935, 0x66B71C1C,
                    "ERIDU 🐀 5★",
                    "\"En los callejones de Nueva Eridu, las trampas y la ley se cruzan en un parpadeo.\""
            ),
            new BannerPhase(
                    8, Franchise.HONKAI_ZZZ,
                    "FILO HIPERSÓNICO Y CALIDÓN",
                    "Jefa de la Sección 6 & Soberana del Coliseo",
                    CompanionProfile.HOSHIMI_MIYABI, CompanionProfile.CAESAR_KING,
                    () -> SystemItems.MISTSPLITTER_REFORGED.isBound() ? new ItemStack(SystemItems.MISTSPLITTER_REFORGED.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    () -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : new ItemStack(Items.NETHERITE_AXE),
                    List.of(CompanionProfile.MARCH_7TH, CompanionProfile.CHA_HAEIN),
                    List.of(
                            () -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFD81B60, 0x66880E4F,
                    "COLISEO & ÉTER ⚔ 5★",
                    "\"Corte de velocidad absoluta y la furia indómita del coliseo.\""
            )
    };

    // ==========================================
    // 3. SOLO LEVELING (3 FASES)
    // ==========================================
    private static final BannerPhase[] SOLO_LEVELING_PHASES = new BannerPhase[]{
            new BannerPhase(
                    0, Franchise.SOLO_LEVELING,
                    "EL MONARCA DE LAS SOMBRAS",
                    "Cazador Rango S & El Señor del Abismo",
                    CompanionProfile.SUNG_JINWOO, null,
                    () -> SystemItems.SHADOW_DAGGERS.isBound() ? new ItemStack(SystemItems.SHADOW_DAGGERS.get()) : new ItemStack(Items.SHEARS),
                    () -> SystemItems.ASRAHAN_HOLY_SWORD.isBound() ? new ItemStack(SystemItems.ASRAHAN_HOLY_SWORD.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.CHA_HAEIN),
                    List.of(
                            () -> SystemItems.BLACK_DRAGON_SCYTHE.isBound() ? new ItemStack(SystemItems.BLACK_DRAGON_SCYTHE.get()) : new ItemStack(Items.NETHERITE_AXE),
                            () -> SystemItems.RISING_DRAGON_PILL.isBound() ? new ItemStack(SystemItems.RISING_DRAGON_PILL.get()) : new ItemStack(Items.GOLDEN_APPLE)
                    ),
                    0xFF7B1FA2, 0x66311B92,
                    "SOMBRA 👑 5★",
                    "\"\"Arise...\" Entre la muerte y el abismo, surge el nuevo gobernante.\""
            ),
            new BannerPhase(
                    1, Franchise.SOLO_LEVELING,
                    "EL CABALLERO CARMESÍ",
                    "Comandante Leal & Filo Carmesí de Sombra",
                    CompanionProfile.IGRIS, null,
                    () -> SystemItems.BLACK_DRAGON_SCYTHE.isBound() ? new ItemStack(SystemItems.BLACK_DRAGON_SCYTHE.get()) : new ItemStack(Items.NETHERITE_AXE),
                    () -> SystemItems.ASRAHAN_HOLY_SWORD.isBound() ? new ItemStack(SystemItems.ASRAHAN_HOLY_SWORD.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.CHA_HAEIN),
                    List.of(
                            () -> SystemItems.SHADOW_DAGGERS.isBound() ? new ItemStack(SystemItems.SHADOW_DAGGERS.get()) : new ItemStack(Items.SHEARS),
                            () -> SystemItems.MARROW_CLEANSING_PILL.isBound() ? new ItemStack(SystemItems.MARROW_CLEANSING_PILL.get()) : new ItemStack(Items.HONEY_BOTTLE)
                    ),
                    0xFFC2185B, 0x66880E4F,
                    "SANGRE 🗡 5★",
                    "\"Mi espada y mi juramento pertenecen eternamente a mi Soberano.\""
            ),
            new BannerPhase(
                    2, Franchise.SOLO_LEVELING,
                    "LA DEVORACIÓN DEL REY",
                    "Monarca Ant de Jeju & Vorágine Destructiva",
                    CompanionProfile.BERU, null,
                    () -> SystemItems.SHADOW_DAGGERS.isBound() ? new ItemStack(SystemItems.SHADOW_DAGGERS.get()) : new ItemStack(Items.SHEARS),
                    () -> SystemItems.BLACK_DRAGON_SCYTHE.isBound() ? new ItemStack(SystemItems.BLACK_DRAGON_SCYTHE.get()) : new ItemStack(Items.NETHERITE_AXE),
                    List.of(CompanionProfile.CHA_HAEIN),
                    List.of(
                            () -> SystemItems.RISING_DRAGON_PILL.isBound() ? new ItemStack(SystemItems.RISING_DRAGON_PILL.get()) : new ItemStack(Items.GOLDEN_APPLE),
                            () -> SystemItems.MARROW_CLEANSING_PILL.isBound() ? new ItemStack(SystemItems.MARROW_CLEANSING_PILL.get()) : new ItemStack(Items.HONEY_BOTTLE)
                    ),
                    0xFF3F51B5, 0x661A237E,
                    "VORÁGINE 🐜 5★",
                    "\"¡Por mi Rey! ¡Nadie osará interponerse ante el paso de mi Señor!\""
            )
    };

    // ==========================================
    // 4. JUJUTSU KAISEN (2 FASES)
    // ==========================================
    private static final BannerPhase[] JJK_PHASES = new BannerPhase[]{
            new BannerPhase(
                    0, Franchise.JUJUTSU_KAISEN,
                    "EL HECHICERO SUPREMO",
                    "El Infinito Intocable & Seis Ojos Celestiales",
                    CompanionProfile.GOJO_SATORU, null,
                    () -> SystemItems.COMMAND_SPELL.isBound() ? new ItemStack(SystemItems.COMMAND_SPELL.get()) : new ItemStack(Items.NETHER_STAR),
                    () -> SystemItems.SKYWARD_BLADE.isBound() ? new ItemStack(SystemItems.SKYWARD_BLADE.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.YUJI_ITADORI, CompanionProfile.MEGUMI),
                    List.of(
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF00ACC1, 0x66006064,
                    "INFINITO 👁 5★",
                    "\"No te preocupes. Después de todo, soy el más fuerte.\""
            ),
            new BannerPhase(
                    1, Franchise.JUJUTSU_KAISEN,
                    "EL REY DE LAS MALDICIONES",
                    "Santuario Malevolente & Llamas del Caos",
                    CompanionProfile.SUKUNA, null,
                    () -> SystemItems.COMMAND_SPELL.isBound() ? new ItemStack(SystemItems.COMMAND_SPELL.get()) : new ItemStack(Items.NETHER_STAR),
                    () -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : new ItemStack(Items.NETHERITE_AXE),
                    List.of(CompanionProfile.YUJI_ITADORI, CompanionProfile.MEGUMI),
                    List.of(
                            () -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFD32F2F, 0x66B71C1C,
                    "MALDICIÓN 🔥 5★",
                    "\"Conoce tu lugar, insecto. Este mundo no es más que mi festín.\""
            )
    };

    // ==========================================
    // 5. FATE / TYPE-MOON (2 FASES)
    // ==========================================
    private static final BannerPhase[] FATE_PHASES = new BannerPhase[]{
            new BannerPhase(
                    0, Franchise.FATE,
                    "LA ESPADA DE LA VICTORIA",
                    "Rey de los Caballeros & Excalibur Sagrada",
                    CompanionProfile.ARTORIA_PENDRAGON, null,
                    () -> SystemItems.ASRAHAN_HOLY_SWORD.isBound() ? new ItemStack(SystemItems.ASRAHAN_HOLY_SWORD.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.SKYWARD_BLADE.isBound() ? new ItemStack(SystemItems.SKYWARD_BLADE.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.CHA_HAEIN, CompanionProfile.ARCHER),
                    List.of(
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.SHIYUAN_SWORD.isBound() ? new ItemStack(SystemItems.SHIYUAN_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF1976D2, 0x660D47A1,
                    "SAGRADO ⚔ 5★",
                    "\"¿Eres tú quien me ha convocado? Pregunto: ¿eres mi Master?\""
            ),
            new BannerPhase(
                    1, Franchise.FATE,
                    "EL REY DE LOS HÉROES",
                    "Puerta de Babilonia & Soberano Dorado",
                    CompanionProfile.GILGAMESH, null,
                    () -> SystemItems.VORTEX_VANQUISHER.isBound() ? new ItemStack(SystemItems.VORTEX_VANQUISHER.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.MISTSPLITTER_REFORGED.isBound() ? new ItemStack(SystemItems.MISTSPLITTER_REFORGED.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.CHA_HAEIN, CompanionProfile.BENNETT),
                    List.of(
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFFFD700, 0x66FF8F00,
                    "TESORO 👑 5★",
                    "\"¡Mestizo! Todos los tesoros del mundo me pertenecen por derecho primordial.\""
            )
    };

    // ==========================================
    // 6. KIMETSU NO YAIBA / DEMON SLAYER (2 FASES)
    // ==========================================
    private static final BannerPhase[] DEMON_SLAYER_PHASES = new BannerPhase[]{
            new BannerPhase(
                    0, Franchise.DEMON_SLAYER,
                    "¡ENCIENDE TU CORAZÓN!",
                    "Pilar de la Llama & Novena Postura: Purgatorio",
                    CompanionProfile.RENGOKU, null,
                    () -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : new ItemStack(Items.NETHERITE_AXE),
                    () -> SystemItems.STAFF_OF_HOMA.isBound() ? new ItemStack(SystemItems.STAFF_OF_HOMA.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.TANJIRO, CompanionProfile.NEZUKO),
                    List.of(
                            () -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFFF5722, 0x66BF360C,
                    "LLAMA 🔥 5★",
                    "\"¡Enciende tu corazón! ¡Supera tus límites y cumple tu deber con honor!\""
            ),
            new BannerPhase(
                    1, Franchise.DEMON_SLAYER,
                    "LAZOS INDESTRUCTIBLES",
                    "Danza del Dios del Fuego & Sangre Ardiente",
                    CompanionProfile.TANJIRO, CompanionProfile.NEZUKO,
                    () -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                    () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                    List.of(CompanionProfile.TANJIRO, CompanionProfile.NEZUKO),
                    List.of(
                            () -> SystemItems.SHIYUAN_SWORD.isBound() ? new ItemStack(SystemItems.SHIYUAN_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF4CAF50, 0x661B5E20,
                    "DANZA SOLAR 🌸 4★",
                    "\"No importa cuántas veces caigamos... ¡siempre nos levantaremos juntos!\""
            )
    };

    // ==========================================
    // 7. WUTHERING WAVES (3 FASES)
    // ==========================================
    private static final BannerPhase[] WUTHERING_WAVES_PHASES = new BannerPhase[]{
            new BannerPhase(
                    0, Franchise.WUTHERING_WAVES,
                    "DESTIS DEL DRAGÓN DE JUÉ",
                    "Magistrada de Jinzhou & Poder de la Iluminación",
                    CompanionProfile.JINSHI, null,
                    () -> SystemItems.ASRAHAN_HOLY_SWORD.isBound() ? new ItemStack(SystemItems.ASRAHAN_HOLY_SWORD.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.DAWNS_BALLAD.isBound() ? new ItemStack(SystemItems.DAWNS_BALLAD.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    List.of(CompanionProfile.CHA_HAEIN, CompanionProfile.NOELLE),
                    List.of(
                            () -> SystemItems.SHIYUAN_SWORD.isBound() ? new ItemStack(SystemItems.SHIYUAN_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_BLACK_SWORD.isBound() ? new ItemStack(SystemItems.THE_BLACK_SWORD.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFFFD54F, 0x66FF8F00,
                    "ESPECTRO 🐉 5★",
                    "\"La luz de Jué guía cada paso hacia la prosperidad y paz de Jinzhou.\""
            ),
            new BannerPhase(
                    1, Franchise.WUTHERING_WAVES,
                    "EL ARDOR DEL FÉNIX",
                    "Mentora de Jinzhou & Plumas Incandescentes",
                    CompanionProfile.CHANGLI, null,
                    () -> SystemItems.MISTSPLITTER_REFORGED.isBound() ? new ItemStack(SystemItems.MISTSPLITTER_REFORGED.get()) : new ItemStack(Items.DIAMOND_SWORD),
                    () -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                    List.of(CompanionProfile.BENNETT, CompanionProfile.CHA_HAEIN),
                    List.of(
                            () -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.THE_FLUTE.isBound() ? new ItemStack(SystemItems.THE_FLUTE.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFFFF5722, 0x66BF360C,
                    "FUSIÓN 🔥 5★",
                    "\"Las jugadas del tablero son como la vida: una chispa puede encender un fénix.\""
            ),
            new BannerPhase(
                    2, Franchise.WUTHERING_WAVES,
                    "VIENTO TEMPESTUOSO Y HILOS DEL TRUENO",
                    "General de los Midnight Rangers & Investigadora Oculta",
                    CompanionProfile.JIYAN, CompanionProfile.YINLIN,
                    () -> SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.isBound() ? new ItemStack(SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    () -> SystemItems.ENGULFING_LIGHTNING.isBound() ? new ItemStack(SystemItems.ENGULFING_LIGHTNING.get()) : new ItemStack(Items.NETHERITE_SWORD),
                    List.of(CompanionProfile.NOELLE, CompanionProfile.CHA_HAEIN),
                    List.of(
                            () -> SystemItems.THE_CATCH.isBound() ? new ItemStack(SystemItems.THE_CATCH.get()) : new ItemStack(Items.IRON_SWORD),
                            () -> SystemItems.FLEUVE_CENDRE_FERRYMAN.isBound() ? new ItemStack(SystemItems.FLEUVE_CENDRE_FERRYMAN.get()) : new ItemStack(Items.IRON_SWORD)
                    ),
                    0xFF00E676, 0x6600B0FF,
                    "AERO & ELECTRO ⚡ 5★",
                    "\"El dragón verde de Qingloong ruge al unísono con las marionetas del trueno.\""
            )
    };

    public static final List<BannerPhase> ALL_ROTATING_PHASES = List.of(
            GENSHIN_PHASES[0],          // 1. Raiden Shogun (Electro)
            HONKAI_PHASES[0],           // 2. Acheron (Nihility)
            SOLO_LEVELING_PHASES[0],    // 3. Sung Jin-Woo (Sombras)
            WUTHERING_WAVES_PHASES[0],  // 4. Jinhsi (Espectro)
            JJK_PHASES[0],              // 5. Gojo Satoru (Infinito)
            FATE_PHASES[0],             // 6. Artoria Pendragon (Saber)
            DEMON_SLAYER_PHASES[0],     // 7. Kyojuro Rengoku (Llama)
            GENSHIN_PHASES[1],          // 8. Hu Tao (Pyro)
            HONKAI_PHASES[1],           // 9. Kafka (Estelaron)
            WUTHERING_WAVES_PHASES[1],  // 10. Changli (Fusión)
            SOLO_LEVELING_PHASES[1],    // 11. Igris (Sangre)
            JJK_PHASES[1],              // 12. Ryomen Sukuna (Maldición)
            FATE_PHASES[1],             // 13. Gilgamesh (Tesoro)
            DEMON_SLAYER_PHASES[1],     // 14. Tanjiro & Nezuko (Danza Solar)
            GENSHIN_PHASES[2],          // 15. Zhongli (Geo)
            HONKAI_PHASES[2],           // 16. Ellen Joe (Escarcha)
            WUTHERING_WAVES_PHASES[2],  // 17. Jiyan & Yinlin (Aero/Electro)
            SOLO_LEVELING_PHASES[2],    // 18. Beru (Vorágine)
            GENSHIN_PHASES[3],          // 19. Furina (Hydro)
            HONKAI_PHASES[3],           // 20. Firefly (SAM)
            GENSHIN_PHASES[4],          // 21. Diluc & Jean (Favonius)
            HONKAI_PHASES[4],           // 22. Dan Heng • IL & Jing Yuan (Luofu)
            GENSHIN_PHASES[5],          // 23. Nahida & Kazuha (Dendro/Anemo)
            HONKAI_PHASES[5],           // 24. Feixiao & Robin (Cacería/Armonía)
            GENSHIN_PHASES[6],          // 25. Xiao (Anemo Yaksha)
            HONKAI_PHASES[6],           // 26. Aventurine (IPC)
            GENSHIN_PHASES[7],          // 27. Arlecchino (Luna Carmesí)
            HONKAI_PHASES[7],           // 28. Jane Doe & Zhu Yuan (ZZZ Eridu)
            GENSHIN_PHASES[8],          // 29. Mavuika (Pyro Natlan)
            HONKAI_PHASES[8],           // 30. Hoshimi Miyabi & Caesar King (Sección 6 / Calidón)
            GENSHIN_PHASES[9],          // 31. Columbina (Diosa Lunar)
            GENSHIN_PHASES[10],         // 32. Clorinde (Electro Duelista)
            GENSHIN_PHASES[11],         // 33. Kinich & Xilonen (Natlan)
            GENSHIN_PHASES[12]          // 34. Skirk & Ganyu (Abismo/Cryo)
    );

    private PromotionalBanner() {}

    private static BannerPhase[] getPhasesForFranchise(Franchise f) {
        return switch (f) {
            case GENSHIN -> GENSHIN_PHASES;
            case HONKAI_ZZZ -> HONKAI_PHASES;
            case SOLO_LEVELING -> SOLO_LEVELING_PHASES;
            case JUJUTSU_KAISEN -> JJK_PHASES;
            case FATE -> FATE_PHASES;
            case DEMON_SLAYER -> DEMON_SLAYER_PHASES;
            case WUTHERING_WAVES -> WUTHERING_WAVES_PHASES;
        };
    }

    public static BannerPhase getActivePhase(long worldDayTime) {
        long currentDay = Math.max(0L, worldDayTime) / MC_DAY_TICKS;
        int phaseIndex = (int) ((currentDay / BANNER_CYCLE_DAYS) % ALL_ROTATING_PHASES.size());
        return ALL_ROTATING_PHASES.get(phaseIndex);
    }

    public static BannerPhase getActivePhase(Franchise franchise, long worldDayTime) {
        return getActivePhase(worldDayTime);
    }

    public static int getPhaseNumber(long worldDayTime) {
        long currentDay = Math.max(0L, worldDayTime) / MC_DAY_TICKS;
        return (int) ((currentDay / BANNER_CYCLE_DAYS) % ALL_ROTATING_PHASES.size()) + 1;
    }

    public static int getTotalPhases() {
        return ALL_ROTATING_PHASES.size();
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

    public static long getCurrentDay(long worldDayTime) {
        return Math.max(0L, worldDayTime) / MC_DAY_TICKS;
    }

    public static long getDaysRemaining(long worldDayTime) {
        long currentDay = getCurrentDay(worldDayTime);
        return BANNER_CYCLE_DAYS - (currentDay % BANNER_CYCLE_DAYS) - 1L;
    }

    public static long getHoursRemaining(long worldDayTime) {
        long ticksToday = Math.max(0L, worldDayTime) % MC_DAY_TICKS;
        return (MC_DAY_TICKS - ticksToday) / 1000L;
    }

    public static String getFormattedTimeRemaining(long worldDayTime) {
        long days = getDaysRemaining(worldDayTime);
        long hours = getHoursRemaining(worldDayTime);
        return days + " d. " + hours + " h.";
    }
}
