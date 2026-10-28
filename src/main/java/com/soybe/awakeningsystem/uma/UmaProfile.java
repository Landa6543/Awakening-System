package com.soybe.awakeningsystem.uma;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.resources.ResourceLocation;

/**
 * Perfil oficial de las 20 corredoras Uma Musume en el Gran Hipódromo de Teyvat.
 * Cada una cuenta con arquetipo táctico, distancia predilecta, pista favorita,
 * cuotas base de apuestas y estadísticas de ritmo de carrera.
 */
public enum UmaProfile {
    AGNES_TACHYON(
            "agnes_tachyon", "Agnes Tachyon",
            "Senkou (Leader)", "Medium", "Turf",
            2.4, 0.42f, 0.40f, 0.40f,
            "Chemical Acceleration", 0xE67E22
    ),
    MEJIRO_DOBER(
            "mejiro_dober", "Mejiro Dober",
            "Sashi (Betweener)", "Medium", "Turf",
            2.8, 0.40f, 0.45f, 0.39f,
            "Explosive Sprint", 0x3498DB
    ),
    TRANSCEND(
            "transcend", "Transcend",
            "Senkou-Nige", "Mile - Medium", "Dirt",
            2.1, 0.43f, 0.40f, 0.42f,
            "Dirt Dominance", 0xD35400
    ),
    CHEVAL_GRAND(
            "cheval_grand", "Cheval Grand",
            "Sashi (Stayers)", "Long (Stayers)", "Turf",
            3.2, 0.38f, 0.44f, 0.48f,
            "Late Surge", 0x9B59B6
    ),
    KITASAN_BLACK(
            "kitasan_black", "Kitasan Black",
            "Nige (Runner)", "Long (Stayers)", "Turf",
            1.9, 0.46f, 0.42f, 0.47f,
            "Dominant Pace", 0xC0392B
    ),
    SATONO_DIAMOND(
            "satono_diamond", "Satono Diamond",
            "Sashi", "Medium - Long", "Turf",
            2.3, 0.41f, 0.43f, 0.44f,
            "Steady Speed", 0x2ECC71
    ),
    OGURI_CAP(
            "oguri_cap", "Oguri Cap",
            "Senkou - Sashi", "Mile", "Turf & Dirt",
            2.0, 0.43f, 0.44f, 0.43f,
            "Total Versatility", 0xBDC3C7
    ),
    TWIN_TURBO(
            "twin_turbo", "Twin Turbo",
            "Dai-Nige (Great Escape)", "Medium", "Turf",
            5.5, 0.55f, 0.52f, 0.28f,
            "Suicide Breakaway", 0x00FFFF
    ),
    SYMBOLI_RUDOLF(
            "symboli_rudolf", "Symboli Rudolf",
            "Senkou", "Medium - Long", "Turf",
            1.7, 0.45f, 0.46f, 0.46f,
            "The Emperor", 0x27AE60
    ),
    RICE_SHOWER(
            "rice_shower", "Rice Shower",
            "Senkou", "Long", "Turf",
            3.5, 0.39f, 0.42f, 0.49f,
            "Steel Determination", 0x2C3E50
    ),
    DAIWA_SCARLET(
            "daiwa_scarlet", "Daiwa Scarlet",
            "Nige-Leader", "Mile - Medium", "Turf",
            2.2, 0.44f, 0.43f, 0.41f,
            "Competitive Fury", 0xE74C3C
    ),
    VODKA(
            "vodka", "Vodka",
            "Sashi", "Mile", "Turf",
            2.3, 0.43f, 0.46f, 0.39f,
            "Brutal Power", 0x7F8C8D
    ),
    GOLD_SHIP(
            "gold_ship", "Gold Ship",
            "Oikomi (Closer)", "Long", "Turf",
            4.8, 0.40f, 0.52f, 0.46f,
            "Unpredictable Chaos", 0xF1C40F
    ),
    TOKAI_TEIO(
            "tokai_teio", "Tokai Teio",
            "Senkou", "Medium", "Turf",
            2.1, 0.44f, 0.47f, 0.40f,
            "Teio Step", 0x2980B9
    ),
    SILENCE_SUZUKA(
            "silence_suzuka", "Silence Suzuka",
            "Nige (Pure Runner)", "Medium", "Turf",
            1.8, 0.50f, 0.43f, 0.38f,
            "Supersonic Speed", 0x1ABC9C
    ),
    SPECIAL_WEEK(
            "special_week", "Special Week",
            "Sashi-Leader", "Medium - Long", "Turf",
            2.5, 0.42f, 0.45f, 0.44f,
            "Indomitable Spirit", 0x8E44AD
    ),
    MEJIRO_MCQUEEN(
            "mejiro_mcqueen", "Mejiro McQueen",
            "Senkou", "Long", "Turf",
            2.6, 0.41f, 0.42f, 0.48f,
            "Aristocratic Stamina", 0xA3E4D7
    ),
    MAYANO_TOP_GUN(
            "mayano_top_gun", "Mayano Top Gun",
            "All-Rounder (Versatile)", "Medium - Long", "Turf",
            3.0, 0.41f, 0.43f, 0.43f,
            "Chameleonic Adaptation", 0xF39C12
    ),
    MANHATTAN_CAFE(
            "manhattan_cafe", "Manhattan Cafe",
            "Oikomi", "Long", "Turf",
            3.4, 0.39f, 0.48f, 0.47f,
            "Silent Shadow", 0x34495E
    ),
    GRASS_WONDER(
            "grass_wonder", "Grass Wonder",
            "Sashi", "Mile - Long", "Turf",
            2.7, 0.41f, 0.44f, 0.44f,
            "Emerald Breeze Strike", 0x16A085
    ),
    TM_OPERA_O(
            "tm_opera_o", "T.M. Opera O",
            "Senkou (The Conqueror)", "Medium - Long", "Turf",
            2.1, 0.44f, 0.46f, 0.45f,
            "Brilliant Opera Overkill", 0xD4AC0D
    ),
    SUPER_CREEK(
            "super_creek", "Super Creek",
            "Senkou (Leader / Stayers)", "Long", "Turf",
            2.2, 0.42f, 0.43f, 0.49f,
            "Pure Heart", 0xFADBD8
    ),
    TAMAMO_CROSS(
            "tamamo_cross", "Tamamo Cross",
            "Oikomi (White Lightning)", "Mile - Medium", "Turf",
            2.3, 0.42f, 0.50f, 0.44f,
            "White Lightning Discharge", 0x5DADE2
    ),
    SEIUN_SKY(
            "seiun_sky", "Seiun Sky",
            "Nige (Strategist)", "Medium - Long", "Turf",
            2.0, 0.47f, 0.43f, 0.45f,
            "Angling x Scheming", 0x48C9B0
    ),
    EL_CONDOR_PASA(
            "el_condor_pasa", "El Condor Pasa",
            "Senkou", "Mile - Medium", "Turf & Dirt",
            2.1, 0.43f, 0.45f, 0.44f,
            "Condor Flight Plan", 0xF39C12
    ),
    KING_HALO(
            "king_halo", "King Halo",
            "Sashi", "Sprint - Mile", "Turf",
            3.1, 0.41f, 0.47f, 0.38f,
            "Pride of Halo", 0x52BE80
    ),
    NICE_NATURE(
            "nice_nature", "Nice Nature",
            "Sashi", "Medium - Long", "Turf",
            3.3, 0.39f, 0.45f, 0.46f,
            "Glow of the Underdog", 0xBA4A00
    ),
    MATIKANETANNHAUSER(
            "matikanetannhauser", "Matikane Tannhauser",
            "Senkou", "Medium - Long", "Turf",
            3.6, 0.40f, 0.43f, 0.45f,
            "Machitan Miracle!", 0x3498DB
    ),
    NARITA_BRIAN(
            "narita_brian", "Narita Brian",
            "Senkou - Sashi", "Medium - Long", "Turf",
            1.8, 0.46f, 0.47f, 0.47f,
            "Shadow Break", 0x7D6608
    ),
    HISHI_AMAZON(
            "hishi_amazon", "Hishi Amazon",
            "Oikomi (Amazon)", "Medium - Long", "Turf",
            2.5, 0.41f, 0.51f, 0.43f,
            "Tie-Break Amazon", 0xAF7AC5
    ),
    MEJIRO_PALMER(
            "mejiro_palmer", "Mejiro Palmer",
            "Dai-Nige (Great Escape)", "Long", "Turf",
            4.5, 0.51f, 0.48f, 0.35f,
            "Grand Festival Escape", 0x58D68D
    ),
    DAITAKU_HELIOS(
            "daitaku_helios", "Daitaku Helios",
            "Nige", "Mile", "Turf",
            2.4, 0.46f, 0.45f, 0.39f,
            "Check-It-Out Rave!", 0xF4D03F
    ),
    MIHONO_BOURBON(
            "mihono_bourbon", "Mihono Bourbon",
            "Nige (Cyborg Precision)", "Medium", "Turf",
            1.9, 0.48f, 0.44f, 0.44f,
            "G00 1st.F_", 0xE74C3C
    ),
    MARUZENSKY(
            "maruzensky", "Maruzensky",
            "Nige (Supercar)", "Mile", "Turf",
            1.9, 0.49f, 0.44f, 0.41f,
            "Red Shift Supercar", 0xC0392B
    ),
    AIR_GROOVE(
            "air_groove", "Air Groove",
            "Senkou (The Empress)", "Medium", "Turf",
            2.3, 0.45f, 0.45f, 0.42f,
            "Empress Pride Pulse", 0x34495E
    ),
    HARU_URARA(
            "haru_urara", "Haru Urara",
            "Sashi (Crowd Favorite)", "Sprint", "Dirt",
            5.0, 0.38f, 0.43f, 0.37f,
            "Urara Ganbaru!", 0xF1948A
    ),
    CURREN_CHAN(
            "curren_chan", "Curren Chan",
            "Senkou", "Sprint", "Turf",
            2.2, 0.45f, 0.48f, 0.36f,
            "One More Wink♡", 0xF8BBD0
    ),
    SMART_FALCON(
            "smart_falcon", "Smart Falcon",
            "Nige (Dirt Idol)", "Mile - Medium", "Dirt",
            2.1, 0.46f, 0.44f, 0.43f,
            "Kiratto☆Falcon Live", 0xF7DC6F
    ),
    COPANO_RICKEY(
            "copano_rickey", "Copano Rickey",
            "Senkou", "Mile", "Dirt",
            2.3, 0.44f, 0.45f, 0.42f,
            "Lucky Feng Shui", 0xF5B041
    ),
    BIWA_HAYAHIDE(
            "biwa_hayahide", "Biwa Hayahide",
            "Senkou (Cool Mind)", "Medium - Long", "Turf",
            2.4, 0.43f, 0.46f, 0.46f,
            "Theoretical Victory Formula", 0x5DADE2
    );

    private final String id;
    private final String displayName;
    private final String tactique;
    private final String distance;
    private final String surface;
    private final double baseOdds;
    private final float baseSpeed;
    private final float acceleration;
    private final float stamina;
    private final String specialSkill;
    private final int colorHex;

    UmaProfile(String id, String displayName, String tactique, String distance, String surface,
               double baseOdds, float baseSpeed, float acceleration, float stamina,
               String specialSkill, int colorHex) {
        this.id = id;
        this.displayName = displayName;
        this.tactique = tactique;
        this.distance = distance;
        this.surface = surface;
        this.baseOdds = baseOdds;
        this.baseSpeed = baseSpeed;
        this.acceleration = acceleration;
        this.stamina = stamina;
        this.specialSkill = specialSkill;
        this.colorHex = colorHex;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getTactique() { return tactique; }
    public String getDistance() { return distance; }
    public String getSurface() { return surface; }
    public double getBaseOdds() { return baseOdds; }
    public float getBaseSpeed() { return baseSpeed; }
    public float getAcceleration() { return acceleration; }
    public float getStamina() { return stamina; }
    public String getSpecialSkill() { return specialSkill; }
    public String getSkillName() { return specialSkill; }
    public int getColorHex() { return colorHex; }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/uma/" + id + ".png");
    }

    public static UmaProfile byId(String id) {
        if (id == null) return SPECIAL_WEEK;
        for (UmaProfile profile : values()) {
            if (profile.id.equalsIgnoreCase(id)) {
                return profile;
            }
        }
        return SPECIAL_WEEK;
    }

    public enum SkillTriggerCategory {
        LEAD_BREAKAWAY,
        CHASE_SURGE,
        FINAL_SPURT,
        OIKOMI_SWEEP
    }

    public SkillTriggerCategory getSkillTriggerCategory() {
        String t = tactique.toLowerCase();
        if (t.contains("dai-nige") || t.contains("nige")) {
            return SkillTriggerCategory.LEAD_BREAKAWAY;
        } else if (t.contains("senkou") || t.contains("senko")) {
            return SkillTriggerCategory.CHASE_SURGE;
        } else if (t.contains("oikomi")) {
            return SkillTriggerCategory.OIKOMI_SWEEP;
        } else {
            return SkillTriggerCategory.FINAL_SPURT;
        }
    }

    public float getSkillSpeedBoost() {
        SkillTriggerCategory cat = getSkillTriggerCategory();
        return switch (cat) {
            case LEAD_BREAKAWAY -> 0.28F;
            case CHASE_SURGE -> 0.32F;
            case FINAL_SPURT -> 0.35F;
            case OIKOMI_SWEEP -> 0.40F;
        };
    }

    public int getSkillDurationTicks() {
        SkillTriggerCategory cat = getSkillTriggerCategory();
        return switch (cat) {
            case LEAD_BREAKAWAY -> 90;
            case CHASE_SURGE -> 80;
            case FINAL_SPURT -> 85;
            case OIKOMI_SWEEP -> 95;
        };
    }
}
