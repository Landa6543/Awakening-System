package com.soybe.awakeningsystem.celestial;

/**
 * The celestial armory: every weapon, its shape (which sets its damage, speed and how it is held) and the skill
 * it casts on right-click. Projectile sprites live in textures/entity/celestial; a blank sprite means the weapon
 * throws a copy of itself.
 */
public enum CelestialWeapon {
    ANARCHY_BLADE("anarchy_blade", Kind.GREATSWORD, Ability.BEAM, 1, 0, 0xFF5A1E, Effect.FIRE, 3, ""),
    ARK_OF_THE_COSMOS("ark_of_the_cosmos", Kind.SWORD, Ability.STARFALL, 6, 0, 0xFF7AD8, Effect.NONE, 3, "empyrean_star"),
    DARKLIGHT_GREATSWORD("darklight_greatsword", Kind.GREATSWORD, Ability.BEAM, 2, 14, 0x8C6CFF, Effect.NONE, 2, ""),
    DEATHS_ASCENSION("deaths_ascension", Kind.SCYTHE, Ability.SCYTHE, 1, 0, 0x5A4CFF, Effect.WITHER, 3, ""),
    DEFILED_GREATSWORD("defiled_greatsword", Kind.GREATSWORD, Ability.BEAM, 1, 0, 0xB0303A, Effect.WITHER, 2, ""),
    DEVILS_DEVASTATION("devils_devastation", Kind.SWORD, Ability.BEAM, 3, 24, 0xE040FF, Effect.FIRE, 3, ""),
    DURANDAL("durandal", Kind.SWORD, Ability.SLAM, 0, 0, 0xE02A2A, Effect.NONE, 2, ""),
    EARTH_SWORD("earth_sword", Kind.GREATSWORD, Ability.BEAM, 5, 40, 0x9AF0FF, Effect.NONE, 3, ""),
    ENTROPIC_CLAYMORE("entropic_claymore", Kind.GREATSWORD, Ability.SLAM, 0, 0, 0x6A4A9A, Effect.WITHER, 2, ""),
    EXOBLADE("exoblade", Kind.SWORD, Ability.DASH, 0, 0, 0x9AE6FF, Effect.NONE, 3, ""),
    GALACTUS_BLADE("galactus_blade", Kind.SWORD, Ability.BEAM, 3, 20, 0xFFA040, Effect.NONE, 3, ""),
    GALAXIA("galaxia", Kind.SWORD, Ability.STARFALL, 5, 0, 0xC060FF, Effect.NONE, 3, "empyrean_star"),
    GREATSWORD_OF_JUDGEMENT("greatsword_of_judgement", Kind.SWORD, Ability.BEAM, 1, 0, 0x8AA0FF, Effect.NONE, 1, ""),
    MIRROR_BLADE("mirror_blade", Kind.SWORD, Ability.NEBULA, 0, 0, 0xA080FF, Effect.SLOW, 2, "nebula_cloud"),
    NOCTALGIA("noctalgia", Kind.GREATSWORD, Ability.SLAM, 0, 0, 0x4A8AFF, Effect.NONE, 3, ""),
    NUCLEOSYNTHESIS("nucleosynthesis", Kind.SWORD, Ability.BEAM, 6, 360, 0xD080FF, Effect.NONE, 3, ""),
    OMNIBLADE("omniblade", Kind.SWORD, Ability.DASH, 0, 0, 0xC8A060, Effect.NONE, 2, ""),
    ORDERBRINGER("orderbringer", Kind.SWORD, Ability.BEAM, 3, 18, 0xB0F0FF, Effect.NONE, 2, ""),
    SCARLET_DEVIL("scarlet_devil", Kind.SPEAR, Ability.JAVELIN, 0, 0, 0xE0202A, Effect.LIFESTEAL, 3, ""),
    SHARD_OF_ANTUMBRA("shard_of_antumbra", Kind.SPEAR, Ability.DASH, 0, 0, 0x8A80A0, Effect.WITHER, 2, ""),
    SOUL_HARVESTER("soul_harvester", Kind.SCYTHE, Ability.SCYTHE, 1, 0, 0x9AF040, Effect.LIFESTEAL, 1, ""),
    SPEAR_OF_DESTINY("spear_of_destiny", Kind.SPEAR, Ability.JAVELIN, 3, 0, 0xFFD060, Effect.NONE, 2, ""),
    ATOM_SPLITTER("atom_splitter", Kind.SPEAR, Ability.JAVELIN, 6, 0, 0x7AF080, Effect.NONE, 3, "atom_splitter_bolt"),
    LAST_MOURNING("last_mourning", Kind.GREATSWORD, Ability.BEAM, 3, 22, 0xFF8A2A, Effect.FIRE, 2, ""),
    OLD_REAPER("old_reaper", Kind.SCYTHE, Ability.SCYTHE, 1, 0, 0x50E070, Effect.WITHER, 2, ""),
    VEHEMENCE("vehemence", Kind.SPEAR, Ability.VEHEMENCE, 0, 0, 0xE0304A, Effect.LIFESTEAL, 3, "vehemence_bolt"),
    VOID_EDGE("void_edge", Kind.SWORD, Ability.BEAM, 2, 12, 0x8A70C0, Effect.SLOW, 2, ""),
    AURORA_TEMPEST("aurora_tempest", Kind.SWORD, Ability.BEAM, 3, 16, 0x7AF0E0, Effect.NONE, 2, "");

    /** How a weapon is held and swung. */
    public enum Kind {
        SWORD(8, -2.2F), GREATSWORD(12, -2.8F), SCYTHE(10, -2.6F), SPEAR(9, -2.5F);

        public final int bonus;
        public final float speed;

        Kind(int bonus, float speed) {
            this.bonus = bonus;
            this.speed = speed;
        }
    }

    /** The right-click skill. */
    public enum Ability {
        BEAM(45), STARFALL(120), SCYTHE(50), JAVELIN(60), VEHEMENCE(100), NEBULA(140), SLAM(100), DASH(60);

        public final int cooldown;

        Ability(int cooldown) {
            this.cooldown = cooldown;
        }
    }

    /** What every hit adds. */
    public enum Effect { NONE, FIRE, WITHER, SLOW, LIFESTEAL }

    public final String id;
    public final Kind kind;
    public final Ability ability;
    /** Beams: how many; starfall: how many stars; javelin: how many pieces it splits into. */
    public final int count;
    /** Beams: degrees between the outermost two (360 = a full ring). */
    public final int spread;
    public final int color;
    public final Effect effect;
    /** 1 to 3, adds damage. */
    public final int tier;
    public final String sprite;

    CelestialWeapon(String id, Kind kind, Ability ability, int count, int spread, int color, Effect effect, int tier, String sprite) {
        this.id = id;
        this.kind = kind;
        this.ability = ability;
        this.count = count;
        this.spread = spread;
        this.color = color;
        this.effect = effect;
        this.tier = tier;
        this.sprite = sprite;
    }

    /** Attack damage bonus over netherite. */
    public int bonus() {
        return kind.bonus + tier * 2;
    }

    /** Roughly the damage of one swing (netherite 4 + bonus + 1). */
    public float swing() {
        return 5 + bonus();
    }
}
