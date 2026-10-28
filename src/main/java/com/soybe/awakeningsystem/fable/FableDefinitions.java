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
                "The First to Forge Netherite",
                "A fable engraved in the Star Stream when the first mortal forged the black alloy of the Nether.",
                Grade.HISTORICAL, 25, true, "⛏"));

        register(new Fable("first_awakening",
                "The First Awakening",
                "The testimony of the first human being to shatter mortal limits and choose a Hunter Class.",
                Grade.HISTORICAL, 20, true, "⚡"));

        register(new Fable("first_blood_hunter",
                "First Hunt Baptism",
                "Defeated their first hostile monster, etching their name into the Star Stream.",
                Grade.HISTORICAL, 15, false, "⚔"));

        register(new Fable("dungeon_initiate",
                "Dungeon Initiate",
                "Conquered their first dimensional dungeon, stepping through the threshold of danger.",
                Grade.HISTORICAL, 20, false, "🚪"));

        register(new Fable("miner_depths",
                "Echoes of the Deep",
                "Mined their first diamond in the heart of the deepslate.",
                Grade.HISTORICAL, 15, false, "⛏"));

        register(new Fable("first_trade",
                "Wandering Merchant's Pact",
                "Completed their first trade transaction with the System Shop or merchants.",
                Grade.HISTORICAL, 15, false, "🪙"));

        register(new Fable("iron_will",
                "Forged in Iron",
                "Donned a full set of tempered metal armor, shielding their body against fate.",
                Grade.HISTORICAL, 15, false, "🛡"));

        register(new Fable("apprentice_alchemist",
                "Essence Distiller",
                "Brewed or imbibed their first elixir or alchemical potion from the System.",
                Grade.HISTORICAL, 15, false, "🧪"));

        register(new Fable("adventurer_steps",
                "Wanderer's Footsteps",
                "Traveled over five thousand blocks exploring the far reaches of the world.",
                Grade.HISTORICAL, 20, false, "🧭"));

        register(new Fable("first_enchantment",
                "Runic Whisper",
                "Imbued a weapon with ancient runic magic at an enchanting table.",
                Grade.HISTORICAL, 20, false, "✨"));

        register(new Fable("bounty_hunter_novice",
                "Novice Bounty Hunter",
                "Fulfilled their first hunt contract at the Hunter Association Board.",
                Grade.HISTORICAL, 20, false, "📜"));

        register(new Fable("culinary_survivor",
                "Hunter's Feast",
                "Cooked and replenished vitality beside a camp hearth after the battle.",
                Grade.HISTORICAL, 15, false, "🍖"));

        register(new Fable("nether_traveler",
                "Crosser of the Nether",
                "Crossed the obsidian veil and set foot in the burning wastes of the Nether.",
                Grade.HISTORICAL, 20, false, "🔥"));

        register(new Fable("first_pet_tamed",
                "Beast Bond",
                "Forged an unbreakable bond of loyalty by taming a beast companion.",
                Grade.HISTORICAL, 15, false, "🐾"));

        register(new Fable("ruin_scavenger",
                "Ruin Scavenger",
                "Unlocked a sealed treasure chest deep within an ancient crypt or ruin.",
                Grade.HISTORICAL, 20, false, "🏛"));

        register(new Fable("first_create_train",
                "The First Dimensional Conductor",
                "The roar of steel on rails awakened the constellations. First train built and operated.",
                Grade.LEGENDARY, 50, true, "🚂"));

        register(new Fable("first_create_aircraft",
                "Pioneer of the Infinite Skies",
                "Defying scenario gravity, constructed the first vessel to sail the skies with Aeronautics.",
                Grade.LEGENDARY, 60, true, "✈"));

        register(new Fable("death_defier",
                "One Who Deceived Death",
                "Survived a fatal blow on the brink of the abyss, defying the laws of the Reaper.",
                Grade.LEGENDARY, 35, false, "❤"));

        register(new Fable("shadow_monarch_army",
                "Commander of the Shadow Legion",
                "Simultaneously commanded more than a dozen soldiers from the spectral shadow realm.",
                Grade.LEGENDARY, 40, false, "👥"));

        register(new Fable("sword_saint_twenty_four",
                "Dance of the Twenty-Four Petals",
                "Attained ecstasy in the Mount Hua plum blossom sword art slicing through the wind.",
                Grade.LEGENDARY, 40, false, "🌸"));

        register(new Fable("steam_colossus",
                "Architect of a Thousand Mechanisms",
                "Mastered steam engineering and kinetic rotational networks of Create.",
                Grade.LEGENDARY, 45, false, "⚙"));

        register(new Fable("igris_crimson_conqueror",
                "Conqueror of the Crimson Knight",
                "Defeated the formidable Red Commander Igris in single combat within the throne hall.",
                Grade.LEGENDARY, 50, false, "🩸"));

        register(new Fable("master_blacksmith",
                "Soulforge Master Blacksmith",
                "Crafted supreme equipment utilizing the Soulforge or Astralite metal.",
                Grade.LEGENDARY, 45, false, "🔨"));

        register(new Fable("speed_demon",
                "Intangible Lightning",
                "Broke physical boundaries reaching blinding swiftness in the heat of battle.",
                Grade.LEGENDARY, 40, false, "⚡"));

        register(new Fable("wealth_of_nations",
                "Star Stream Magnate",
                "Amassed a grand fortune of over twenty-five thousand Awakening Coins.",
                Grade.LEGENDARY, 45, false, "💰"));

        register(new Fable("combo_virtuoso",
                "Hundred-Blade Tempest",
                "Chained a devastating flurry of continuous strikes without receiving harm.",
                Grade.LEGENDARY, 40, false, "🌀"));

        register(new Fable("deep_dark_explorer",
                "Silence of the Deep Dark",
                "Trod among the ruins of the Sculk and struck down the guardian of shadows.",
                Grade.LEGENDARY, 50, false, "👁"));

        register(new Fable("elemental_harmonizer",
                "Elemental Harmonizer",
                "Harmonized and unleashed abilities across multiple combined elemental affinities.",
                Grade.LEGENDARY, 45, false, "🔮"));

        register(new Fable("guild_veteran",
                "Guild Pillar",
                "Completed fifteen hunt contracts, earning the highest esteem of the Association.",
                Grade.LEGENDARY, 50, false, "🏆"));

        register(new Fable("unbroken_rampart",
                "Unbroken Rampart",
                "Mitigated and withstood colossal damage in combat without breaking posture.",
                Grade.LEGENDARY, 40, false, "🛡"));

        register(new Fable("demon_king_subjugator",
                "Demon King Subjugator",
                "Defeated the Demon King Baran in dungeon combat, quenching his flames.",
                Grade.LEGENDARY, 55, false, "👹"));

        register(new Fable("first_ender_dragon",
                "Ender Dragon Slayer",
                "A triumphant chant to the warrior who crossed the End void and struck down the primordial dragon.",
                Grade.SEMI_MYTH, 80, true, "🐉"));

        register(new Fable("first_wither",
                "Conqueror of Decay",
                "The legend of the one who purged the three-headed storm and claimed the Nether Star.",
                Grade.SEMI_MYTH, 75, true, "☠"));

        register(new Fable("solo_dragon_slayer",
                "Solitary Dragon Slayer",
                "Challenged and liquidated a mythical dragon solo without seeking aid from other hunters.",
                Grade.SEMI_MYTH, 70, false, "🗡"));

        register(new Fable("titan_slayer",
                "Crusher of Titans",
                "Unleashed a devastating impact with over 150 points of concentrated damage.",
                Grade.SEMI_MYTH, 75, false, "💥"));

        register(new Fable("dungeon_red_gate_conqueror",
                "Survivor of the Red Gate",
                "Conquered a Red Rank calamity breach and returned alive to tell the tale.",
                Grade.SEMI_MYTH, 85, false, "🚪"));

        register(new Fable("beru_ant_king_crusher",
                "Slayer of the Ant King",
                "Crushed Beru, the apex predator of Jeju Island, silencing his shrieks.",
                Grade.SEMI_MYTH, 85, false, "🐜"));

        register(new Fable("tower_conqueror_floor_50",
                "Conqueror of the Trial Tower",
                "Conquered the grueling upper floors of the Hundred Trial Tower.",
                Grade.SEMI_MYTH, 80, false, "🗼"));

        register(new Fable("gate_breaker_s_rank",
                "S-Rank Gate Calamity",
                "Purged and sealed an S-Rank Calamity dungeon saving the surrounding lands.",
                Grade.SEMI_MYTH, 90, false, "💠"));

        register(new Fable("isekai_pioneer",
                "Yggdrasil Root Pioneer",
                "Traversed to the Isekai dimension and harvested sacred wood from the primordial roots.",
                Grade.SEMI_MYTH, 75, false, "🌳"));

        register(new Fable("murim_celestial_cultivator",
                "Ascension of the Murim Cultivator",
                "Attained the Spiritual Realm within the Millennial Pagoda of Murim.",
                Grade.SEMI_MYTH, 80, false, "☯"));

        register(new Fable("shadow_monarch_domain",
                "Sovereignty of the Shadow Army",
                "Maintained a contingent of fifteen shadow soldiers awake at the same time.",
                Grade.SEMI_MYTH, 85, false, "🌑"));

        register(new Fable("astral_dragon_subjugator",
                "Astral Dragon Subjugator",
                "Hunted down the ancient Astral Dragon, claiming its cosmic scales.",
                Grade.SEMI_MYTH, 90, false, "✨"));

        register(new Fable("colossal_sculk_warden_slayer",
                "Slayer of the Sculk Titan",
                "Felled the colossal Sculk Warden of the Abyss silencing its sonic boom.",
                Grade.SEMI_MYTH, 85, false, "🔊"));

        register(new Fable("tri_lunar_pilgrim",
                "Tri-Lunar Sanctuary Pilgrim",
                "Attuned with the three moons of Teyvat unlocking their sacred blessings.",
                Grade.SEMI_MYTH, 75, false, "🌙"));

        register(new Fable("first_monarch_defeat",
                "One Who Defied the Monarchs",
                "The Star Stream fell silent when a true sovereign fell before a mortal blade in a calamity trial.",
                Grade.MYTH, 160, true, "👑"));

        register(new Fable("first_level_100",
                "Mortal Transcendence",
                "Reached hunter Level 100, becoming an entity revered throughout cosmic nebulae.",
                Grade.MYTH, 180, true, "★"));

        register(new Fable("sky_dreadnought_commander",
                "Admiral of the Celestial Dreadnought",
                "Captained an armed flying fortress braving cosmic storms with Aeronautics.",
                Grade.MYTH, 130, false, "🛸"));

        register(new Fable("god_slayer_statue",
                "He Who Defied the Stone God",
                "Survived the Cartenon Temple commandments and struck down the colossal God Statue.",
                Grade.MYTH, 170, false, "🗿"));

        register(new Fable("prestige_transcendence",
                "Transcendence of the Immortal Soul",
                "Completed karmic Prestige purification rebirthing with superior might.",
                Grade.MYTH, 165, false, "🌟"));

        register(new Fable("limbo_void_walker",
                "Walker of the Limbo Void",
                "Crossed through the Limbo rift and purged a cosmic entity in the primordial void.",
                Grade.MYTH, 155, false, "🌀"));

        register(new Fable("abyss_conqueror_floor_12",
                "Master of the 12th Abyssal Floor",
                "Conquered the Twelfth Floor of the Spiral Abyss claiming the stellar stars.",
                Grade.MYTH, 160, false, "🌌"));

        register(new Fable("dragon_monarch_purger",
                "Purger of the Dragon Monarchs",
                "Annihilated an Ancient Primordial Dragon in combat of divine proportions.",
                Grade.MYTH, 175, false, "🐲"));

        register(new Fable("level_200_demigod",
                "Demigod of the Star Stream",
                "Attained legendary Hunter Level Two Hundred, consecrating their stellar name.",
                Grade.MYTH, 180, false, "⚜"));

        register(new Fable("hundred_dungeons_conqueror",
                "Conqueror of Fifty Dungeons",
                "Conquered fifty dimensional dungeons becoming a living legend across scenarios.",
                Grade.MYTH, 150, false, "🗝"));

        register(new Fable("giant_story_demon_king",
                "The Fall of the Demon King of the 73rd Scenario",
                "Giant Story: Led the purge of the Demon King Baran sealing the destiny of an era.",
                Grade.GIANT_STORY, 300, false, "🔥"));

        register(new Fable("giant_story_star_stream_savior",
                "Savior of the Forgotten Scenario",
                "Giant Story: Completed 50 guild quests and ascended to the Realm of Prestige.",
                Grade.GIANT_STORY, 350, false, "✨"));

        register(new Fable("giant_story_monarch_of_shadows",
                "Birth of the Shadow Monarch",
                "Giant Story: Awakened the absolute authority of shadows raising an immortal legion.",
                Grade.GIANT_STORY, 320, false, "👑"));

        register(new Fable("giant_story_torchbearer_of_the_end",
                "Torchbearer of the Apocalypse",
                "Giant Story: Contained and eradicated a Cataclysmic Dungeon Break saving the realm.",
                Grade.GIANT_STORY, 340, false, "🕯"));

        register(new Fable("giant_story_myth_creator",
                "Author of a Cosmic Myth",
                "Giant Story: Accumulated over fifteen glorious fables echoing across the universe.",
                Grade.GIANT_STORY, 380, false, "📜"));

        register(new Fable("giant_story_cosmic_liberator",
                "Liberator of the Infinite Scenario",
                "Giant Story: Conquered the greatest trials across three realms: Overworld, Isekai, and Murim.",
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
