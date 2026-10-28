package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public enum CompanionProfile {
    COLUMBINA(
            "columbina",
            "Columbina",
            "Trilunar Goddess of Teyvat",
            5,
            "§d§l",
            "Lunar / Celestial",
            "Song of the Three Moons: Awakens the authorities of Aria, Sonnet, and Canon. Silver star shower, cosmic regeneration, and Abyss purification.",
            "assets/awakening_system/textures/entity/companion/columbina.png"
    ),
    RAIDEN_SHOGUN(
            "raiden_shogun",
            "Raiden Shogun",
            "The Almighty Shogun",
            5,
            "§d§l",
            "Electro",
            "Eye of Stormy Judgment: Unleashes lightning upon enemies and energizes the player.",
            "assets/awakening_system/textures/entity/companion/raiden_shogun.png"
    ),
    HU_TAO(
            "hu_tao",
            "Hu Tao",
            "77th Director of the Wangsheng Funeral Parlor",
            5,
            "§c§l",
            "Pyro",
            "Guide to Afterlife: Blazes with searing flames and fiery spirit butterflies.",
            "assets/awakening_system/textures/entity/companion/hu_tao.png"
    ),
    ZHONGLI(
            "zhongli",
            "Zhongli",
            "Lord of Geo (Morax)",
            5,
            "§6§l",
            "Geo",
            "Dominance of Earth: Protective jade shield and petrifies nearby enemies.",
            "assets/awakening_system/textures/entity/companion/zhongli.png"
    ),
    FURINA(
            "furina",
            "Furina",
            "Star of Fontaine",
            5,
            "§b§l",
            "Hydro",
            "Salon Solitaire: Waves of water that damage enemies and restore the player's health.",
            "assets/awakening_system/textures/entity/companion/furina.png"
    ),
    NAHIDA(
            "nahida",
            "Nahida",
            "Lesser Lord Kusanali",
            5,
            "§a§l",
            "Dendro",
            "Illusory Heart: Binds and paralyzes enemies, triggering Bloom explosions.",
            "assets/awakening_system/textures/entity/companion/nahida.png"
    ),
    KAZUHA(
            "kazuha",
            "Kaedehara Kazuha",
            "Wandering Swordsman of Inazuma",
            5,
            "§f§l",
            "Anemo",
            "Chihayaburu: Pulls enemies with whirlwind vortexes and launches them into the air.",
            "assets/awakening_system/textures/entity/companion/kazuha.png"
    ),
    GOJO_SATORU(
            "gojo_satoru",
            "Satoru Gojo",
            "The Strongest Jujutsu Sorcerer",
            5,
            "§3§l",
            "Limitless",
            "Reverse Cursed Technique: Gravitational repulsion that blasts away nearby foes.",
            "assets/awakening_system/textures/entity/companion/gojo_satoru.png"
    ),
    SUNG_JINWOO(
            "sung_jinwoo",
            "Sung Jin-Woo",
            "Shadow Monarch",
            5,
            "§5§l",
            "Shadow",
            "Arise!: Summons a legion of shadow soldiers to overwhelm the target.",
            "assets/awakening_system/textures/entity/companion/sung_jinwoo.png"
    ),
    NOELLE(
            "noelle",
            "Noelle",
            "Chivalric Maid of Favonius",
            4,
            "§e§l",
            "Geo",
            "Breastplate: Armored shield that absorbs massive damage and heals on hit.",
            "assets/awakening_system/textures/entity/companion/noelle.png"
    ),
    CHA_HAEIN(
            "cha_haein",
            "Cha Hae-In",
            "Dancer of the Light Sword",
            4,
            "§6§l",
            "Light",
            "Sword of the Radiant Light: High-speed thrusts that penetrate armor with light waves.",
            "assets/awakening_system/textures/entity/companion/cha_haein.png"
    ),
    DILUC(
            "diluc",
            "Diluc",
            "The Dark Dawn of Mondstadt",
            5,
            "§c§l",
            "Pyro",
            "Dawn: Summons a blazing phoenix that sweeps through all nearby enemies.",
            "assets/awakening_system/textures/entity/companion/diluc.png"
    ),
    JEAN(
            "jean",
            "Jean",
            "Acting Grand Master",
            5,
            "§a§l",
            "Anemo",
            "Dandelion Breeze: Creates a swirling wind domain that cleanses and heals allies.",
            "assets/awakening_system/textures/entity/companion/jean.png"
    ),
    BENNETT(
            "bennett",
            "Bennett",
            "Leader of Benny's Adventure Team",
            4,
            "§6§l",
            "Pyro",
            "Fantastic Voyage: Inspiring zone that buffs attack damage and continuously heals.",
            "assets/awakening_system/textures/entity/companion/bennett.png"
    ),

    ACHERON(
            "acheron",
            "Acheron",
            "Emanator of Nihility",
            5,
            "§d§l",
            "Lightning / Nihility",
            "Slashed Dream Cries in Red: Slashes through dimensions with dark crimson thunder.",
            "assets/awakening_system/textures/entity/companion/acheron.png"
    ),
    KAFKA(
            "kafka",
            "Kafka",
            "Stellaron Hunter",
            5,
            "§5§l",
            "Lightning / DoT",
            "Twilight Trill: Detonates all active DoT status effects across all enemies.",
            "assets/awakening_system/textures/entity/companion/kafka.png"
    ),
    ELLEN_JOE(
            "ellen_joe",
            "Ellen Joe",
            "Shark of Victoria Housekeeping",
            5,
            "§b§l",
            "Ice / Frost",
            "Arctic Ambush: Ice-skating shark sweeps that freeze and cleave through foes.",
            "assets/awakening_system/textures/entity/companion/ellen_joe.png"
    ),

    IGRIS(
            "igris",
            "Igris",
            "Crimson Blood Knight",
            5,
            "§4§l",
            "Shadow / Slash",
            "Crimson Greatsword: Massive spinning shockwave strikes that inflict deep Bleed.",
            "assets/awakening_system/textures/entity/companion/igris.png"
    ),
    BERU(
            "beru",
            "Beru",
            "Shadow Ant King",
            5,
            "§9§l",
            "Shadow / Vortex",
            "Apex Gluttony: Piercing sonic screams that shred armor and stun surrounding enemies.",
            "assets/awakening_system/textures/entity/companion/beru.png"
    ),

    SUKUNA(
            "sukuna",
            "Ryomen Sukuna",
            "King of Curses",
            5,
            "§c§l",
            "Curse / Fire",
            "Malevolent Shrine: Dismantles space itself, shredding all targets within range.",
            "assets/awakening_system/textures/entity/companion/sukuna.png"
    ),
    YUJI_ITADORI(
            "yuji_itadori",
            "Yuji Itadori",
            "Tiger Vessel",
            4,
            "§e§l",
            "Physical / Force",
            "Black Flash: Unleashes cursed energy impact waves with tremendous knockback.",
            "assets/awakening_system/textures/entity/companion/yuji_itadori.png"
    ),

    ARTORIA_PENDRAGON(
            "artoria_pendragon",
            "Artoria Pendragon",
            "King of Knights (Saber)",
            5,
            "§b§l",
            "Holy / Light",
            "Excalibur: Holy pillar of divine light that smites evil across the battlefield.",
            "assets/awakening_system/textures/entity/companion/artoria_pendragon.png"
    ),
    GILGAMESH(
            "gilgamesh",
            "Gilgamesh",
            "King of Heroes (Archer)",
            5,
            "§6§l",
            "Golden / Treasury",
            "Gate of Babylon: Golden armory barrage firing legendary blades from ripples in space.",
            "assets/awakening_system/textures/entity/companion/gilgamesh.png"
    ),

    RENGOKU(
            "rengoku",
            "Kyojuro Rengoku",
            "Flame Hashira",
            5,
            "§6§l",
            "Fire / Flame",
            "Flame Breathing - Ninth Form: Rengoku: A devastating blazing charge that scorches the earth.",
            "assets/awakening_system/textures/entity/companion/rengoku.png"
    ),
    TANJIRO(
            "tanjiro",
            "Tanjiro Kamado",
            "Sun Dance Hunter",
            4,
            "§a§l",
            "Water / Solar",
            "Hinokami Kagura: Solar slash combos that burn away demonic resistances.",
            "assets/awakening_system/textures/entity/companion/tanjiro.png"
    ),
        NEZUKO(
            "nezuko",
            "Nezuko Kamado",
            "Blazing Blood Demon",
            4,
            "§d§l",
            "Demonic Fire",
            "Blood Demon Art: Exploding Blood: Pink demonic flames that burn enemies and cleanse allies.",
            "assets/awakening_system/textures/entity/companion/nezuko.png"
    ),
    XIAO(
            "xiao",
            "Xiao",
            "Yaksha Guardian (Alatus)",
            5,
            "§b§l",
            "Anemo",
            "Bane of All Evil: Yaksha mask donning, granting immense leaps and plunging Anemo shockwaves.",
            "assets/awakening_system/textures/entity/companion/xiao.png"
    ),
    ARLECCHINO(
            "arlecchino",
            "Arlecchino",
            "The Knave (Father of the House of the Hearth)",
            5,
            "§4§l",
            "Pyro / Crimson",
            "Balemoon Rising: Cleaves foes with crimson blood scythes, consuming Bond of Life for burst DMG.",
            "assets/awakening_system/textures/entity/companion/arlecchino.png"
    ),
    FIREFLY(
            "firefly",
            "Firefly (SAM)",
            "Iron Cavalry Armor",
            5,
            "§6§l",
            "Fire / Combustion",
            "Complete Combustion: Transforms into the SAM armor, scorching the battlefield with molten fury.",
            "assets/awakening_system/textures/entity/companion/firefly.png"
    ),
    MEGUMI(
            "megumi",
            "Megumi Fushiguro",
            "Ten Shadows Shaman",
            4,
            "§3§l",
            "Shadow / Shikigami",
            "Divine Dog & Nue: Summons shadow familiars to strike with lightning and tearing claws.",
            "assets/awakening_system/textures/entity/companion/megumi.png"
    ),
    ARCHER(
            "archer",
            "Archer (EMIYA)",
            "Heroic Spirit of Unlimited Blades",
            4,
            "§c§l",
            "Projection / Blades",
            "Unlimited Blade Works: Projectile sword storm that saturates the target zone.",
            "assets/awakening_system/textures/entity/companion/archer.png"
    ),
    GANYU(
            "ganyu",
            "Ganyu",
            "Plenilune Gaze",
            5,
            "§b§l",
            "Cryo",
            "Celestial Shower: Ice Soul Gem that continuously rains sacred glacial shards.",
            "assets/awakening_system/textures/entity/companion/ganyu.png"
    ),
    NEUVILLETTE(
            "neuvillette",
            "Neuvillette",
            "Iudex of Fontaine",
            5,
            "§9§l",
            "Hydro",
            "O Tears, I Shall Repay: Torrents of surging primal water that obliterate obstacles in a straight line.",
            "assets/awakening_system/textures/entity/companion/neuvillette.png"
    ),
    MARCH_7TH(
            "march_7th",
            "March 7th",
            "Archer of the Astral Express",
            4,
            "§d§l",
            "Ice",
            "Glacial Arrow: Freezes and pins target with kaleidoscopic frost crystal arrows.",
            "assets/awakening_system/textures/entity/companion/march_7th.png"
    ),
    HOSHIMI_MIYABI(
            "hoshimi_miyabi",
            "Hoshimi Miyabi",
            "Chief of Section 6",
            5,
            "§3§l",
            "Ether",
            "Frost Petal Slash: Icy fox blade drawn with lightning speed, slicing all enemies into ribbons.",
            "assets/awakening_system/textures/entity/companion/hoshimi_miyabi.png"
    ),

    DAN_HENG_IL(
            "dan_heng_il",
            "Dan Heng • Imbibitor Lunae",
            "Vidyadhara High Elder",
            5,
            "§3§l",
            "Imaginary / Dragon",
            "Azure Dragon Roar: Draconic leaps unleashing surging waves of Imaginary light.",
            "assets/awakening_system/textures/entity/companion/dan_heng_il.png"
    ),
    FEIXIAO(
            "feixiao",
            "Feixiao",
            "Implacable General of Yaoqing",
            5,
            "§a§l",
            "Wind / The Hunt",
            "Terrasplit Strike: Axe and gunblade fury that annihilates elite barriers.",
            "assets/awakening_system/textures/entity/companion/feixiao.png"
    ),
    AVENTURINE(
            "aventurine",
            "Aventurine",
            "Strategist of the IPC",
            5,
            "§6§l",
            "Imaginary / Preservation",
            "Roulette of Fate: Shimmering dice and shields that nullify incoming fatal blows.",
            "assets/awakening_system/textures/entity/companion/aventurine.png"
    ),
    ROBIN(
            "robin",
            "Robin",
            "Halovian Express Singer",
            5,
            "§d§l",
            "Physical / Harmony",
            "Vox Harmonique: Concerto performance empowering entire team with massive ATK boosts.",
            "assets/awakening_system/textures/entity/companion/robin.png"
    ),
    JANE_DOE(
            "jane_doe",
            "Jane Doe",
            "Criminal Consultant of New Eridu",
            5,
            "§c§l",
            "Physical / Anomaly",
            "Phantom Daggers: Flurry of venomous stabs building extreme Anomaly buildup.",
            "assets/awakening_system/textures/entity/companion/jane_doe.png"
    ),
    ZHU_YUAN(
            "zhu_yuan",
            "Zhu Yuan",
            "Special Security Squad Captain",
            5,
            "§9§l",
            "Ether / Ballistics",
            "Full Metal Shell: Shotgun buckshot barrages that stagger and crush heavily armored foes.",
            "assets/awakening_system/textures/entity/companion/zhu_yuan.png"
    ),
    CAESAR_KING(
            "caesar_king",
            "Caesar King",
            "Leader of the Sons of Calydon",
            5,
            "§4§l",
            "Physical / Defense",
            "Shield of the King: Vanguard riot shield charge that grants unwavering poise.",
            "assets/awakening_system/textures/entity/companion/caesar_king.png"
    ),
    BURNICE_WHITE(
            "burnice_white",
            "Burnice White",
            "Mixologist of Sons of Calydon",
            5,
            "§6§l",
            "Fire / Anomaly",
            "Flamethrower Cocktail: Twin flamethrowers that ignite the entire field in continuous inferno.",
            "assets/awakening_system/textures/entity/companion/burnice_white.png"
    ),
    NICOLE_DEMARA(
            "nicole_demara",
            "Nicole Demara",
            "Boss of Cunning Hares",
            4,
            "§d§l",
            "Ether / Support",
            "Black Hole Cannon: Gravitational orb briefcase that pulls all surrounding enemies together.",
            "assets/awakening_system/textures/entity/companion/nicole_demara.png"
    ),
    JIYAN(
            "jiyan",
            "Jiyan",
            "Midnight Rangers General",
            5,
            "§2§l",
            "Aero / Dragon Spear",
            "Qingloong Lances: Draconic wind polearm sweeps that dominate the battlefield.",
            "assets/awakening_system/textures/entity/companion/jiyan.png"
    ),
    CHANGLI(
            "changli",
            "Changli",
            "Mentor of Jinzhou",
            5,
            "§c§l",
            "Fusion / Phoenix Flames",
            "Flaming Feather: Swift flaming broadsword strikes that kindle radiant phoenix bursts.",
            "assets/awakening_system/textures/entity/companion/changli.png"
    ),
    JINSHI(
            "jinshi",
            "Jinhsi",
            "Magistrate of Jinzhou",
            5,
            "§e§l",
            "Spectro / Divinity",
            "Jueyuan Light: Celestial dragon avatar summoning radiant beams that pierce through darkness.",
            "assets/awakening_system/textures/entity/companion/jinshi.png"
    ),
    YINLIN(
            "yinlin",
            "Yinlin",
            "Hidden Inquirer",
            5,
            "§5§l",
            "Electro / Puppet",
            "Zapstring Marionette: Electric puppets that chain lightning through groups of enemies.",
            "assets/awakening_system/textures/entity/companion/yinlin.png"
    ),
    MAVUIKA(
            "mavuika",
            "Mavuika",
            "Pyro Archon of Natlan",
            5,
            "§6§l",
            "Pyro / Primordial Fire",
            "Ancient Flame of Natlan: Ignites ancient battle drive, unleashing volcanic sun bursts.",
            "assets/awakening_system/textures/entity/companion/mavuika.png"
    ),
    KINICH(
            "kinich",
            "Kinich",
            "Wasteland Hunter",
            5,
            "§a§l",
            "Dendro / Beast",
            "Grappling Hook Cannon: High-mobility pixel beast hooks that shred enemies from the air.",
            "assets/awakening_system/textures/entity/companion/kinich.png"
    ),
    XILONEN(
            "xilonen",
            "Xilonen",
            "Name Forger",
            5,
            "§e§l",
            "Geo / Rhythm",
            "Rhythm of the Earth: Roller-skates that shred the armor and resistances of all rivals.",
            "assets/awakening_system/textures/entity/companion/xilonen.png"
    ),
    CLORINDE(
            "clorinde",
            "Clorinde",
            "Champion Duelist of Fontaine",
            5,
            "§9§l",
            "Electro / Rapier",
            "Shadow Hunt Thunder: Rapier fencing and piercing pistol shots charged with lightning.",
            "assets/awakening_system/textures/entity/companion/clorinde.png"
    ),
    SKIRK(
            "skirk",
            "Skirk",
            "Master of the Abyss",
            5,
            "§1§l",
            "Abyss / Void",
            "Primordial Chaos Vortex: Tears space-time apart, dragging enemies into a bottomless abyss.",
            "assets/awakening_system/textures/entity/companion/skirk.png"
    ),
    JING_YUAN(
            "jing_yuan",
            "Jing Yuan",
            "General of the Cloud Knights",
            5,
            "§e§l",
            "Lightning / Erudition",
            "Lightning-Lord: Summons the colossal divine guardian to cleave down upon the entire vanguard.",
            "assets/awakening_system/textures/entity/companion/jing_yuan.png"
    ),
    JINGLIU(
            "jingliu",
            "Jingliu",
            "Sword Champion of the Luofu",
            5,
            "§b§l",
            "Ice / Destruction",
            "Moonlit Transmigration: Glacial trance releasing lunar sword beams that freeze the soul.",
            "assets/awakening_system/textures/entity/companion/jingliu.png"
    ),
    SHOREKEEPER(
            "shorekeeper",
            "Shorekeeper",
            "Guardian of the Stellar Domain",
            5,
            "§b§l",
            "Spectro / Domain",
            "Song of Tethys: Stellar domain barrier that continuously heals the master and amplifies DMG.",
            "assets/awakening_system/textures/entity/companion/shorekeeper.png"
    ),
    CAMELLYA(
            "camellya",
            "Camellya",
            "Bloom of Chaos and Thorns",
            5,
            "§d§l",
            "Destruction / Tendrils",
            "Thorn of Crimson Bloom: Entangles enemies in thorny chaotic vines, triggering explosive blooms.",
            "assets/awakening_system/textures/entity/companion/camellya.png"
    ),
    ROVER(
            "rover",
            "Rover",
            "Primordial Resonator",
            5,
            "§e§l",
            "Spectro / Primordial",
            "Echo Resonance: Balances light and shadow blades, releasing versatile elemental shockwaves.",
            "assets/awakening_system/textures/entity/companion/rover.png"
    ),

    NANALLY(
            "nanally",
            "Nanally",
            "Anomaly Researcher",
            5,
            "§d§l",
            "Supernatural / Ether",
            "Scarlet Judgement: Crimson blade strikes that condemn and execute wounded foes.",
            "assets/awakening_system/textures/entity/companion/nanally.png"
    ),
    MINT(
            "mint",
            "Mint",
            "Hacker Mechanic of Hethereau",
            4,
            "§b§l",
            "Electro / Cyber",
            "Celestial Arrow: High-altitude holy bolts that rain down like purifying sunbeams.",
            "assets/awakening_system/textures/entity/companion/mint.png"
    ),
    LACRIMOSA(
            "lacrimosa",
            "Lacrimosa",
            "Spectral Lady of Dusk",
            5,
            "§5§l",
            "Shadow / Spectral",
            "Spectral Tear: Deep-sea phantom song that submerges enemies under crushing tidal pressure.",
            "assets/awakening_system/textures/entity/companion/lacrimosa.png"
    ),

    KLEE(
            "klee",
            "Klee",
            "Spark Knight of Favonius",
            5,
            "§c§l",
            "Pyro",
            "Sparks 'n' Splash: Tosses bouncing bombs that detonate into fiery cluster explosions.",
            "assets/awakening_system/textures/entity/companion/klee.png"
    ),
    SIGEWINNE(
            "sigewinne",
            "Sigewinne",
            "Head Nurse of Meropide",
            5,
            "§b§l",
            "Hydro",
            "Bouncing Bubble: Bounces restorative healing bubbles that trap small enemies and soothe allies.",
            "assets/awakening_system/textures/entity/companion/sigewinne.png"
    ),
    YAOYAO(
            "yaoyao",
            "Yaoyao",
            "Disciple of Madame Ping",
            4,
            "§a§l",
            "Dendro",
            "Yuegui Toss: Deploys jade rabbit turrets throwing explosive radishes that heal and damage.",
            "assets/awakening_system/textures/entity/companion/yaoyao.png"
    ),
    SAYU(
            "sayu",
            "Sayu",
            "Ninja Badger of the Shuumatsuban",
            4,
            "§f§l",
            "Anemo",
            "Yoohoo Art: Fuufuu Windwheel: Curls into a spinning windwheel, rolling through enemies and healing.",
            "assets/awakening_system/textures/entity/companion/sayu.png"
    ),
    DIONA(
            "diona",
            "Diona",
            "Cat's Tail Bartender",
            4,
            "§b§l",
            "Cryo",
            "Icy Paws: Fires feline frost claws that grant an ice shield and healing cocktail mist.",
            "assets/awakening_system/textures/entity/companion/diona.png"
    ),
    QIQI(
            "qiqi",
            "Qiqi",
            "Resurrection of the Past",
            5,
            "§b§l",
            "Cryo",
            "Adeptus Art: Preserver of Fortune: Applies talisman marks that heal allies upon striking enemies.",
            "assets/awakening_system/textures/entity/companion/qiqi.png"
    ),
    YELAN(
            "yelan",
            "Yelan",
            "The Valley of Orchids",
            5,
            "§9§l",
            "Hydro",
            "Lingering Lifeline: Swift phantom wire dash that entangles and obliterates marked enemies.",
            "assets/awakening_system/textures/entity/companion/yelan.png"
    ),
    NAVIA(
            "navia",
            "Navia",
            "President of Spina di Rosula",
            5,
            "§6§l",
            "Geo",
            "Ceremonial Gunbrella: Cannon fire barrage consuming Crystallize shrapnel for devastating blasts.",
            "assets/awakening_system/textures/entity/companion/navia.png"
    ),

    BAILU(
            "bailu",
            "Bailu",
            "Lady of Healing Dew",
            5,
            "§9§l",
            "Lightning / Abundance",
            "Singing Among Clouds: Dragon invigorator that revives fallen allies and provides passive healing.",
            "assets/awakening_system/textures/entity/companion/bailu.png"
    ),
    CLARA(
            "clara",
            "Clara",
            "The Girl of the Sledge and Svarog",
            5,
            "§c§l",
            "Physical / Destruction",
            "Svarog's Protection: Heavy mechanical retaliation laser that obliterates anyone who harms Clara.",
            "assets/awakening_system/textures/entity/companion/clara.png"
    ),
    HUOHUO(
            "huohuo",
            "Huohuo",
            "Novice Ten-Lords Commission Judge",
            5,
            "§a§l",
            "Wind / Abundance",
            "Tail: Spiritual Domination: Exorcises sinister spirits, recharging ally energy and buffing ATK.",
            "assets/awakening_system/textures/entity/companion/huohuo.png"
    ),
    SPARKLE(
            "sparkle",
            "Sparkle",
            "Masked Fool",
            5,
            "§d§l",
            "Quantum / Harmony",
            "Dreamdiver Theatrix: Illusionist stagecraft providing massive skill points and CRIT DMG buffs.",
            "assets/awakening_system/textures/entity/companion/sparkle.png"
    ),
    BLACK_SWAN(
            "black_swan",
            "Black Swan",
            "Memokeeper",
            5,
            "§5§l",
            "Wind / Nihility",
            "Fate's Tarot: Weaves Arcana cards across enemies, eroding their defenses through psychological torment.",
            "assets/awakening_system/textures/entity/companion/black_swan.png"
    ),
    TOPAZ(
            "topaz",
            "Topaz & Numby",
            "IPC Senior Investment Specialist",
            5,
            "§6§l",
            "Fire / The Hunt",
            "Numby Trotter Charge: Directs her dimensional trotter to hunt down treasures and stagger enemies.",
            "assets/awakening_system/textures/entity/companion/topaz.png"
    ),
    RUAN_MEI(
            "ruan_mei",
            "Ruan Mei",
            "Genius Society Scientist #81",
            5,
            "§b§l",
            "Ice / Harmony",
            "Floral Petal Realm: Plucks her ruan to increase Weakness Break efficiency and all-type RES PEN.",
            "assets/awakening_system/textures/entity/companion/ruan_mei.png"
    ),
    SUNDAY(
            "sunday",
            "Sunday",
            "Leader of the Penacony Oak Family",
            5,
            "§e§l",
            "Imaginary / Harmony",
            "Choir of the Order: Chants celestial hymns that shield allies and amplify incoming damage bursts.",
            "assets/awakening_system/textures/entity/companion/sunday.png"
    ),
    DAN_HENG(
            "dan_heng",
            "Dan Heng",
            "Astral Express Guard",
            4,
            "§a§l",
            "Wind / The Hunt",
            "Knight Spear: Wind piercing thrust that slows down targets and applies heavy knockback.",
            "assets/awakening_system/textures/entity/companion/dan_heng.png"
    ),
    HERTA(
            "herta",
            "Herta",
            "Hacker Mechanic of Hethereau",
            4,
            "§b§l",
            "Ice / Erudition",
            "Kuru Kuru!: Spins her giant toy hammer, sweeping and freezing all surrounding foes.",
            "assets/awakening_system/textures/entity/companion/herta.png"
    ),
    SERVAL(
            "serval",
            "Serval",
            "Belobog's Rebel Mechanic",
            4,
            "§9§l",
            "Lightning / Erudition",
            "Electro-Rock Concert: Unleashes electric guitar shock chords that shock and electrocute all foes.",
            "assets/awakening_system/textures/entity/companion/serval.png"
    ),
    NATASHA(
            "natasha",
            "Natasha",
            "Underworld Doctor",
            4,
            "§f§l",
            "Physical / Abundance",
            "Medical Grenade Launcher: Fires restorative alchemical shells that heal the whole party.",
            "assets/awakening_system/textures/entity/companion/natasha.png"
    ),
    ASTA(
            "asta",
            "Asta",
            "Lead Researcher",
            4,
            "§6§l",
            "Fire / Harmony",
            "Astral Meteor Shower: Rains searing starfire that accelerates team movement SPD significantly.",
            "assets/awakening_system/textures/entity/companion/asta.png"
    ),
    TINGYUN(
            "tingyun",
            "Tingyun",
            "Whistling Emissary of the Sky-Faring Commission",
            4,
            "§e§l",
            "Lightning / Harmony",
            "Ritual of the Fox Fan: Waves her auspicious fan to grant allies massive Energy and ATK boosts.",
            "assets/awakening_system/textures/entity/companion/tingyun.png"
    ),
    QINGQUE(
            "qingque",
            "Qingque",
            "Diviner of the Divination Commission",
            4,
            "§a§l",
            "Quantum / Erudition",
            "Celestial Mahjong: Draws jade tiles until landing a winning hand, crushing foes with a giant tile.",
            "assets/awakening_system/textures/entity/companion/qingque.png"
    ),
    GALLAGHER(
            "gallagher",
            "Gallagher",
            "Hound Security Officer",
            4,
            "§c§l",
            "Fire / Abundance",
            "Brew of the Underworld: Mixes a fiery brew that heals allies when they attack debuffed targets.",
            "assets/awakening_system/textures/entity/companion/gallagher.png"
    ),
    XUEYI(
            "xueyi",
            "Xueyi",
            "Ten-Lords Commission Judge",
            4,
            "§d§l",
            "Quantum / Destruction",
            "Karma Iron Chains: Rushes forward with divine iron shackles that ignore enemy elemental shields.",
            "assets/awakening_system/textures/entity/companion/xueyi.png"
    ),
    LYNX(
            "lynx",
            "Lynx",
            "Snowfield Explorer",
            4,
            "§3§l",
            "Quantum / Abundance",
            "Polar First Aid: Deploys outdoor emergency kits that cleanse debuffs and increase Max HP.",
            "assets/awakening_system/textures/entity/companion/lynx.png"
    ),

    ENCORE(
            "encore",
            "Encore",
            "Black Shores Consultant",
            5,
            "§c§l",
            "Fusion / Plushies",
            "Cloudy & Cosmos: Calls plush guardians to unleash explosive tantrums across the battlefield.",
            "assets/awakening_system/textures/entity/companion/encore.png"
    ),
    VERINA(
            "verina",
            "Verina",
            "Sacred Botanical Gardener",
            5,
            "§a§l",
            "Spectro / Botany",
            "Sprout of Life: Blossoms celestial flora that continuously heals the party and buffs ATK.",
            "assets/awakening_system/textures/entity/companion/verina.png"
    ),
    JIANXIN(
            "jianxin",
            "Jianxin",
            "Serene Wind Taoist",
            5,
            "§2§l",
            "Aero / Tai Chi",
            "Tai Chi Chi-Gathering: Channels an inner energy sphere that parries attacks and creates a shield.",
            "assets/awakening_system/textures/entity/companion/jianxin.png"
    ),
    CALCHARO(
            "calcharo",
            "Calcharo",
            "Leader of the Ghost Hounds",
            5,
            "§5§l",
            "Electro / Shadow",
            "Deathblade Phantom: Summons phantom lightning swords that execute enemies with unrelenting slashes.",
            "assets/awakening_system/textures/entity/companion/calcharo.png"
    ),
    SANHUA(
            "sanhua",
            "Sanhua",
            "Elite Jinzhou Guard",
            4,
            "§b§l",
            "Glacio / Rapier",
            "Glacio Vision: Precise frost katana cleaves that freeze enemies in crystalline ice.",
            "assets/awakening_system/textures/entity/companion/sanhua.png"
    ),
    DANJIN(
            "danjin",
            "Danjin",
            "Roaming Crimson Avenger",
            4,
            "§4§l",
            "Chaos / Blood",
            "Crimson Cleave: Sacrifices a portion of vitality to unleash devastating blood shockwaves.",
            "assets/awakening_system/textures/entity/companion/danjin.png"
    ),
    CHIXIA(
            "chixia",
            "Chixia",
            "Fiery Gunslinger",
            4,
            "§6§l",
            "Fusion / Bullets",
            "Blazing Revolvers: High-speed dual-revolver bursts that perforate enemy vanguard units.",
            "assets/awakening_system/textures/entity/companion/chixia.png"
    ),
    BAIZHI(
            "baizhi",
            "Baizhi",
            "Medical Researcher of Huaxu",
            4,
            "§3§l",
            "Glacio / Ether",
            "You'tan Healing: Summons an aquatic familiar that restores vitality and revives fallen allies.",
            "assets/awakening_system/textures/entity/companion/baizhi.png"
    ),

    PIPER_WHEEL(
            "piper_wheel",
            "Piper Wheel",
            "Trucker of Sons of Calydon",
            4,
            "§6§l",
            "Physical / Giant Wrench",
            "Tire Spin Hurricane: Swings a colossal tire wrench, spinning through crowds with massive daze.",
            "assets/awakening_system/textures/entity/companion/piper_wheel.png"
    ),
    KOLEDA_BELOBOG(
            "koleda_belobog",
            "Koleda Belobog",
            "President of Belobog Heavy Industries",
            5,
            "§c§l",
            "Fire / Hammer",
            "Forge Seismic Smash: Titanic hammer blows fused with explosive charges that shatter guard.",
            "assets/awakening_system/textures/entity/companion/koleda_belobog.png"
    ),
    LUCY(
            "lucy",
            "Lucy (Luciana)",
            "Leader of the Ghost Hounds",
            4,
            "§e§l",
            "Fire / Baseball Bat",
            "Guardboar Home Run: Bats mechanical explosive guardboars that fly into enemy lines and detonate.",
            "assets/awakening_system/textures/entity/companion/lucy.png"
    ),
    CORIN_WICKES(
            "corin_wickes",
            "Corin Wickes",
            "Cleaner of Victoria Housekeeping",
            4,
            "§7§l",
            "Physical / Chainsaw",
            "Extreme Cleaning Saw: Runs her heavy circular saw continuously into staggered foes, ripping through armor.",
            "assets/awakening_system/textures/entity/companion/corin_wickes.png"
    ),
    SOUKAKU(
            "soukaku",
            "Soukaku",
            "Oni of Section 6",
            4,
            "§b§l",
            "Ice / Fan",
            "Voracious Wind Vortex: Plants a massive frost battle banner that freezes foes and buffs team DMG.",
            "assets/awakening_system/textures/entity/companion/soukaku.png"
    ),
    ANBY_DEMARA(
            "anby_demara",
            "Anby Demara",
            "Electric Swordsman of Cunning Hares",
            4,
            "§e§l",
            "Electric / Sword",
            "Lightning Discharge: Fast thunder slashes and shock kicks that destabilize and stun enemies.",
            "assets/awakening_system/textures/entity/companion/anby_demara.png"
    ),
    BILLY_KID(
            "billy_kid",
            "Billy Kid",
            "Cybernetic Starlight Knight",
            4,
            "§c§l",
            "Physical / Dual Pistols",
            "Starlight Acrobatic Salvo: Slow-motion spinning dual revolver shots that clear out entire enemy waves.",
            "assets/awakening_system/textures/entity/companion/billy_kid.png"
    ),
    GRACE_HOWARD(
            "grace_howard",
            "Grace Howard",
            "Anomaly Researcher",
            5,
            "§9§l",
            "Electric / Nailgun",
            "Electric Nail Barrage: High-tech nailgun bursts and shock grenades that electrocute mechanical foes.",
            "assets/awakening_system/textures/entity/companion/grace_howard.png"
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

    public Component getDisplayNameComponent() {
        return Component.translatableWithFallback("awakening_system.companion." + this.id + ".name", this.displayName);
    }

    public Component getTitleComponent() {
        return Component.translatableWithFallback("awakening_system.companion." + this.id + ".title", this.title);
    }

    public Component getElementComponent() {
        return Component.translatableWithFallback("awakening_system.companion." + this.id + ".element", this.element);
    }

    public Component getSkillDescComponent() {
        return Component.translatableWithFallback("awakening_system.companion." + this.id + ".skill_desc", this.skillDesc);
    }

    public float getModelScale() {
        return switch (this) {
            case NAHIDA, KLEE, SIGEWINNE, YAOYAO, SAYU, DIONA, QIQI,
                 BAILU, CLARA, HUOHUO, LYNX,
                 ENCORE, VERINA,
                 PIPER_WHEEL, KOLEDA_BELOBOG, LUCY -> 0.75F;
            case CORIN_WICKES, SOUKAKU, HERTA, QINGQUE -> 0.85F;
            default -> 1.0F;
        };
    }

    public boolean isSmallModel() {
        return getModelScale() < 1.0F;
    }

    public boolean isSlim() {
        return switch (this) {
            case RAIDEN_SHOGUN, HU_TAO, NOELLE, NAHIDA, KAZUHA, CHA_HAEIN,
                 ACHERON, KAFKA, ELLEN_JOE, ARTORIA_PENDRAGON, NEZUKO,
                 ARLECCHINO, FIREFLY, FEIXIAO, ROBIN, JANE_DOE, ZHU_YUAN,
                 CAESAR_KING, CHANGLI, JINSHI, YINLIN, MAVUIKA, XILONEN,
                 CLORINDE, SKIRK, GANYU, MARCH_7TH, JINGLIU, NEUVILLETTE, FURINA,
                 HOSHIMI_MIYABI, BURNICE_WHITE, NICOLE_DEMARA,
                 SHOREKEEPER, CAMELLYA, ROVER, COLUMBINA,
                 NANALLY, MINT, LACRIMOSA,
                 KLEE, SIGEWINNE, YAOYAO, SAYU, DIONA, QIQI, YELAN, NAVIA,
                 BAILU, CLARA, HUOHUO, SPARKLE, BLACK_SWAN, TOPAZ, RUAN_MEI, SUNDAY,
                 HERTA, SERVAL, NATASHA, ASTA, TINGYUN, QINGQUE, XUEYI, LYNX,
                 ENCORE, VERINA, JIANXIN, SANHUA, DANJIN, CHIXIA, BAIZHI,
                 PIPER_WHEEL, KOLEDA_BELOBOG, LUCY, CORIN_WICKES, SOUKAKU, ANBY_DEMARA, GRACE_HOWARD -> true;
            default -> false;
        };
    }

        public SoundEvent getSummonSound() {
        return switch (this) {
            case HERTA -> ModSounds.HERTA_KURURIN.get();
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
            case GANYU -> ModSounds.COMPANION_GANYU_SUMMON.get();
            case NEUVILLETTE -> ModSounds.COMPANION_NEUVILLETTE_SUMMON.get();
            case MARCH_7TH -> ModSounds.COMPANION_MARCH_7TH_SUMMON.get();
            case DAN_HENG_IL -> ModSounds.COMPANION_DAN_HENG_IL_SUMMON.get();
            case FEIXIAO -> ModSounds.COMPANION_FEIXIAO_SUMMON.get();
            case AVENTURINE -> ModSounds.COMPANION_AVENTURINE_SUMMON.get();
            case ROBIN -> ModSounds.COMPANION_ROBIN_SUMMON.get();
            case JING_YUAN -> ModSounds.COMPANION_JING_YUAN_SUMMON.get();
            case JINGLIU -> ModSounds.COMPANION_JINGLIU_SUMMON.get();
            case HOSHIMI_MIYABI -> ModSounds.COMPANION_HOSHIMI_MIYABI_SUMMON.get();
            case JANE_DOE -> ModSounds.COMPANION_JANE_DOE_SUMMON.get();
            case CAESAR_KING -> ModSounds.COMPANION_CAESAR_KING_SUMMON.get();
            case BURNICE_WHITE -> ModSounds.COMPANION_BURNICE_WHITE_SUMMON.get();
            case NICOLE_DEMARA -> ModSounds.COMPANION_NICOLE_DEMARA_SUMMON.get();
            case ZHU_YUAN -> ModSounds.COMPANION_ZHU_YUAN_SUMMON.get();
            case JIYAN -> ModSounds.COMPANION_JIYAN_SUMMON.get();
            case YINLIN -> ModSounds.COMPANION_YINLIN_SUMMON.get();
            case CHANGLI -> ModSounds.COMPANION_CHANGLI_SUMMON.get();
            case JINSHI -> ModSounds.COMPANION_JINSHI_SUMMON.get();
            case SHOREKEEPER -> ModSounds.COMPANION_SHOREKEEPER_SUMMON.get();
            case CAMELLYA -> ModSounds.COMPANION_CAMELLYA_SUMMON.get();
            case ROVER -> ModSounds.COMPANION_ROVER_SUMMON.get();
            case CLORINDE -> ModSounds.COMPANION_CLORINDE_SUMMON.get();
            case SKIRK -> ModSounds.COMPANION_SKIRK_SUMMON.get();
            case XILONEN -> ModSounds.COMPANION_XILONEN_SUMMON.get();
            case MAVUIKA -> ModSounds.COMPANION_MAVUIKA_SUMMON.get();
            case KINICH -> ModSounds.COMPANION_KINICH_SUMMON.get();
            case COLUMBINA -> ModSounds.COMPANION_COLUMBINA_SUMMON.get();

            case DIONA -> net.minecraft.sounds.SoundEvents.CAT_PURREOW;
            case SAYU -> net.minecraft.sounds.SoundEvents.WIND_CHARGE_BURST.value();
            case KLEE -> net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_TWINKLE;
            case QIQI -> net.minecraft.sounds.SoundEvents.SNOW_GOLEM_AMBIENT;
            case YAOYAO -> net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BELL.value();
            case SIGEWINNE -> net.minecraft.sounds.SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE;
            case YELAN -> net.minecraft.sounds.SoundEvents.ARROW_HIT_PLAYER;
            case NAVIA -> net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value();

            case BAILU -> net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER;
            case CLARA -> net.minecraft.sounds.SoundEvents.IRON_GOLEM_REPAIR;
            case HUOHUO -> net.minecraft.sounds.SoundEvents.SOUL_ESCAPE.value();
            case SPARKLE -> net.minecraft.sounds.SoundEvents.NOTE_BLOCK_CHIME.value();
            case BLACK_SWAN -> net.minecraft.sounds.SoundEvents.AMETHYST_CLUSTER_STEP;
            case TOPAZ -> net.minecraft.sounds.SoundEvents.PIG_AMBIENT;
            case RUAN_MEI -> net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME;
            case SUNDAY -> net.minecraft.sounds.SoundEvents.BELL_RESONATE;
            case DAN_HENG -> net.minecraft.sounds.SoundEvents.TRIDENT_RIPTIDE_1.value();
            case SERVAL -> net.minecraft.sounds.SoundEvents.NOTE_BLOCK_GUITAR.value();
            case NATASHA -> net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW;
            case ASTA -> net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_BLAST;
            case TINGYUN -> net.minecraft.sounds.SoundEvents.NOTE_BLOCK_FLUTE.value();
            case QINGQUE -> net.minecraft.sounds.SoundEvents.ANVIL_USE;
            case GALLAGHER -> net.minecraft.sounds.SoundEvents.FIRECHARGE_USE;
            case XUEYI -> net.minecraft.sounds.SoundEvents.CHAIN_PLACE;
            case LYNX -> net.minecraft.sounds.SoundEvents.POWDER_SNOW_STEP;

            case ANBY_DEMARA -> ModSounds.COMPANION_ANBY_DEMARA_SUMMON.get();
            case BILLY_KID -> ModSounds.COMPANION_BILLY_KID_SUMMON.get();
            case PIPER_WHEEL -> ModSounds.COMPANION_PIPER_WHEEL_SUMMON.get();
            case KOLEDA_BELOBOG -> ModSounds.COMPANION_KOLEDA_SUMMON.get();
            case LUCY -> ModSounds.COMPANION_LUCY_SUMMON.get();
            case CORIN_WICKES -> ModSounds.COMPANION_CORIN_WICKES_SUMMON.get();
            case SOUKAKU -> ModSounds.COMPANION_SOUKAKU_SUMMON.get();
            case GRACE_HOWARD -> ModSounds.COMPANION_GRACE_HOWARD_SUMMON.get();
            case CALCHARO -> net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT;
            case SANHUA -> net.minecraft.sounds.SoundEvents.GLASS_BREAK;
            case DANJIN -> net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT;
            case CHIXIA -> net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_SHOOT;
            case BAIZHI -> net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME;
            case JIANXIN -> net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE;
            case VERINA -> net.minecraft.sounds.SoundEvents.FLOWERING_AZALEA_PLACE;
            case ENCORE -> net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR;
            case NANALLY -> net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE;
            case MINT -> net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BELL.value();
            case LACRIMOSA -> net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME;

            default -> ModSounds.NPC_BLESSING.get();
        };
    }

    public SoundEvent getSkillSound() {
        return switch (this) {
            case HERTA -> ModSounds.HERTA_KURURIN.get();
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
            case GANYU -> ModSounds.COMPANION_GANYU_SKILL.get();
            case NEUVILLETTE -> ModSounds.COMPANION_NEUVILLETTE_SKILL.get();
            case MARCH_7TH -> ModSounds.COMPANION_MARCH_7TH_SKILL.get();
            case DAN_HENG_IL -> ModSounds.COMPANION_DAN_HENG_IL_SKILL.get();
            case FEIXIAO -> ModSounds.COMPANION_FEIXIAO_SKILL.get();
            case AVENTURINE -> ModSounds.COMPANION_AVENTURINE_SKILL.get();
            case ROBIN -> ModSounds.COMPANION_ROBIN_SKILL.get();
            case JING_YUAN -> ModSounds.COMPANION_JING_YUAN_SKILL.get();
            case JINGLIU -> ModSounds.COMPANION_JINGLIU_SKILL.get();
            case HOSHIMI_MIYABI -> ModSounds.COMPANION_HOSHIMI_MIYABI_SKILL.get();
            case JANE_DOE -> ModSounds.COMPANION_JANE_DOE_SKILL.get();
            case CAESAR_KING -> ModSounds.COMPANION_CAESAR_KING_SKILL.get();
            case BURNICE_WHITE -> ModSounds.COMPANION_BURNICE_WHITE_SKILL.get();
            case NICOLE_DEMARA -> ModSounds.COMPANION_NICOLE_DEMARA_SKILL.get();
            case ZHU_YUAN -> ModSounds.COMPANION_ZHU_YUAN_SKILL.get();
            case JIYAN -> ModSounds.COMPANION_JIYAN_SKILL.get();
            case YINLIN -> ModSounds.COMPANION_YINLIN_SKILL.get();
            case CHANGLI -> ModSounds.COMPANION_CHANGLI_SKILL.get();
            case JINSHI -> ModSounds.COMPANION_JINSHI_SKILL.get();
            case SHOREKEEPER -> ModSounds.COMPANION_SHOREKEEPER_SKILL.get();
            case CAMELLYA -> ModSounds.COMPANION_CAMELLYA_SKILL.get();
            case ROVER -> ModSounds.COMPANION_ROVER_SKILL.get();
            case CLORINDE -> ModSounds.COMPANION_CLORINDE_SKILL.get();
            case SKIRK -> ModSounds.COMPANION_SKIRK_SKILL.get();
            case XILONEN -> ModSounds.COMPANION_XILONEN_SKILL.get();
            case MAVUIKA -> ModSounds.COMPANION_MAVUIKA_SKILL.get();
            case KINICH -> ModSounds.COMPANION_KINICH_SKILL.get();
            case COLUMBINA -> ModSounds.COMPANION_COLUMBINA_SKILL.get();
            case ANBY_DEMARA -> ModSounds.COMPANION_ANBY_DEMARA_SKILL.get();
            case BILLY_KID -> ModSounds.COMPANION_BILLY_KID_SKILL.get();
            case PIPER_WHEEL -> ModSounds.COMPANION_PIPER_WHEEL_SKILL.get();
            case KOLEDA_BELOBOG -> ModSounds.COMPANION_KOLEDA_SKILL.get();
            case LUCY -> ModSounds.COMPANION_LUCY_SKILL.get();
            case CORIN_WICKES -> ModSounds.COMPANION_CORIN_WICKES_SKILL.get();
            case SOUKAKU -> ModSounds.COMPANION_SOUKAKU_SKILL.get();
            case GRACE_HOWARD -> ModSounds.COMPANION_GRACE_HOWARD_SKILL.get();
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
            case GANYU -> ModSounds.COMPANION_GANYU_AMBIENT.get();
            case NEUVILLETTE -> ModSounds.COMPANION_NEUVILLETTE_AMBIENT.get();
            case MARCH_7TH -> ModSounds.COMPANION_MARCH_7TH_AMBIENT.get();
            case DAN_HENG_IL -> ModSounds.COMPANION_DAN_HENG_IL_AMBIENT.get();
            case FEIXIAO -> ModSounds.COMPANION_FEIXIAO_AMBIENT.get();
            case AVENTURINE -> ModSounds.COMPANION_AVENTURINE_AMBIENT.get();
            case ROBIN -> ModSounds.COMPANION_ROBIN_AMBIENT.get();
            case JING_YUAN -> ModSounds.COMPANION_JING_YUAN_AMBIENT.get();
            case JINGLIU -> ModSounds.COMPANION_JINGLIU_AMBIENT.get();
            case HOSHIMI_MIYABI -> ModSounds.COMPANION_HOSHIMI_MIYABI_AMBIENT.get();
            case JANE_DOE -> ModSounds.COMPANION_JANE_DOE_AMBIENT.get();
            case CAESAR_KING -> ModSounds.COMPANION_CAESAR_KING_AMBIENT.get();
            case BURNICE_WHITE -> ModSounds.COMPANION_BURNICE_WHITE_AMBIENT.get();
            case NICOLE_DEMARA -> ModSounds.COMPANION_NICOLE_DEMARA_AMBIENT.get();
            case ZHU_YUAN -> ModSounds.COMPANION_ZHU_YUAN_AMBIENT.get();
            case JIYAN -> ModSounds.COMPANION_JIYAN_AMBIENT.get();
            case YINLIN -> ModSounds.COMPANION_YINLIN_AMBIENT.get();
            case CHANGLI -> ModSounds.COMPANION_CHANGLI_AMBIENT.get();
            case JINSHI -> ModSounds.COMPANION_JINSHI_AMBIENT.get();
            case SHOREKEEPER -> ModSounds.COMPANION_SHOREKEEPER_AMBIENT.get();
            case CAMELLYA -> ModSounds.COMPANION_CAMELLYA_AMBIENT.get();
            case ROVER -> ModSounds.COMPANION_ROVER_AMBIENT.get();
            case CLORINDE -> ModSounds.COMPANION_CLORINDE_AMBIENT.get();
            case SKIRK -> ModSounds.COMPANION_SKIRK_AMBIENT.get();
            case XILONEN -> ModSounds.COMPANION_XILONEN_AMBIENT.get();
            case MAVUIKA -> ModSounds.COMPANION_MAVUIKA_AMBIENT.get();
            case KINICH -> ModSounds.COMPANION_KINICH_AMBIENT.get();
            case COLUMBINA -> ModSounds.COMPANION_COLUMBINA_AMBIENT.get();
            case LUCY -> ModSounds.COMPANION_LUCY_AMBIENT.get();
            case SOUKAKU -> ModSounds.COMPANION_SOUKAKU_AMBIENT.get();
            case BAILU, CLARA, HUOHUO, SERVAL, ASTA, TINGYUN, QINGQUE, HERTA -> ModSounds.COMPANION_MARCH_7TH_AMBIENT.get();
            case CORIN_WICKES -> ModSounds.COMPANION_CORIN_WICKES_AMBIENT.get();
            case KOLEDA_BELOBOG -> ModSounds.COMPANION_KOLEDA_AMBIENT.get();
            case PIPER_WHEEL -> ModSounds.COMPANION_PIPER_WHEEL_AMBIENT.get();
            case GRACE_HOWARD -> ModSounds.COMPANION_GRACE_HOWARD_AMBIENT.get();
            case SPARKLE, BLACK_SWAN -> ModSounds.COMPANION_KAFKA_AMBIENT.get();
            case BILLY_KID -> ModSounds.COMPANION_BILLY_KID_AMBIENT.get();
            case ANBY_DEMARA -> ModSounds.COMPANION_ANBY_DEMARA_AMBIENT.get();
            case KLEE, SIGEWINNE, YAOYAO, SAYU, DIONA, QIQI, VERINA, ENCORE, LYNX -> ModSounds.COMPANION_NAHIDA_AMBIENT.get();
            case YELAN, NAVIA -> ModSounds.COMPANION_CLORINDE_AMBIENT.get();
            case TOPAZ, RUAN_MEI -> ModSounds.COMPANION_ROBIN_AMBIENT.get();
            case DAN_HENG, GALLAGHER, SUNDAY -> ModSounds.COMPANION_DAN_HENG_IL_AMBIENT.get();
            case CALCHARO -> ModSounds.COMPANION_JIYAN_AMBIENT.get();
            case SANHUA, DANJIN, CHIXIA, BAIZHI, JIANXIN, NANALLY, MINT, LACRIMOSA -> ModSounds.COMPANION_CHANGLI_AMBIENT.get();
            default -> ModSounds.NPC_INTERACT.get();
        };
    }

    public SoundEvent getHurtSound() {
        return switch (this) {
            case RAIDEN_SHOGUN -> ModSounds.COMPANION_RAIDEN_SHOGUN_HURT.get();
            case HU_TAO -> ModSounds.COMPANION_HU_TAO_HURT.get();
            case KOLEDA_BELOBOG -> ModSounds.COMPANION_KOLEDA_HURT.get();
            case ZHONGLI -> ModSounds.COMPANION_ZHONGLI_HURT.get();
            case FURINA -> ModSounds.COMPANION_FURINA_HURT.get();
            case NAHIDA, KLEE, SIGEWINNE, YAOYAO, SAYU, DIONA, QIQI, VERINA, ENCORE, LYNX -> ModSounds.COMPANION_NAHIDA_HURT.get();
            case KAZUHA -> ModSounds.COMPANION_KAZUHA_HURT.get();
            case GOJO_SATORU -> ModSounds.COMPANION_GOJO_SATORU_HURT.get();
            case SUNG_JINWOO -> ModSounds.COMPANION_SUNG_JINWOO_HURT.get();
            case NOELLE -> ModSounds.COMPANION_NOELLE_HURT.get();
            case CORIN_WICKES -> ModSounds.COMPANION_CORIN_WICKES_HURT.get();
            case CHA_HAEIN -> ModSounds.COMPANION_CHA_HAEIN_HURT.get();
            case DILUC -> ModSounds.COMPANION_DILUC_HURT.get();
            case JEAN -> ModSounds.COMPANION_JEAN_HURT.get();
            case BENNETT -> ModSounds.COMPANION_BENNETT_HURT.get();
            case BILLY_KID -> ModSounds.COMPANION_BILLY_KID_HURT.get();
            case ACHERON -> ModSounds.COMPANION_ACHERON_HURT.get();
            case KAFKA, SPARKLE, BLACK_SWAN -> ModSounds.COMPANION_KAFKA_HURT.get();
            case GRACE_HOWARD -> ModSounds.COMPANION_GRACE_HOWARD_HURT.get();
            case ELLEN_JOE -> ModSounds.COMPANION_ELLEN_JOE_HURT.get();
            case PIPER_WHEEL -> ModSounds.COMPANION_PIPER_WHEEL_HURT.get();
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
            case GANYU -> ModSounds.COMPANION_GANYU_HURT.get();
            case NEUVILLETTE -> ModSounds.COMPANION_NEUVILLETTE_HURT.get();
            case SOUKAKU -> ModSounds.COMPANION_SOUKAKU_HURT.get();
            case MARCH_7TH, BAILU, CLARA, HUOHUO, SERVAL, ASTA, TINGYUN, QINGQUE, HERTA -> ModSounds.COMPANION_MARCH_7TH_HURT.get();
            case DAN_HENG_IL, DAN_HENG, GALLAGHER, SUNDAY -> ModSounds.COMPANION_DAN_HENG_IL_HURT.get();
            case FEIXIAO -> ModSounds.COMPANION_FEIXIAO_HURT.get();
            case AVENTURINE -> ModSounds.COMPANION_AVENTURINE_HURT.get();
            case ROBIN, TOPAZ, RUAN_MEI -> ModSounds.COMPANION_ROBIN_HURT.get();
            case JING_YUAN -> ModSounds.COMPANION_JING_YUAN_HURT.get();
            case JINGLIU -> ModSounds.COMPANION_JINGLIU_HURT.get();
            case HOSHIMI_MIYABI -> ModSounds.COMPANION_HOSHIMI_MIYABI_HURT.get();
            case ANBY_DEMARA -> ModSounds.COMPANION_ANBY_DEMARA_HURT.get();
            case JANE_DOE -> ModSounds.COMPANION_JANE_DOE_HURT.get();
            case CAESAR_KING -> ModSounds.COMPANION_CAESAR_KING_HURT.get();
            case BURNICE_WHITE -> ModSounds.COMPANION_BURNICE_WHITE_HURT.get();
            case LUCY -> ModSounds.COMPANION_LUCY_HURT.get();
            case NICOLE_DEMARA -> ModSounds.COMPANION_NICOLE_DEMARA_HURT.get();
            case ZHU_YUAN -> ModSounds.COMPANION_ZHU_YUAN_HURT.get();
            case JIYAN, CALCHARO -> ModSounds.COMPANION_JIYAN_HURT.get();
            case YINLIN -> ModSounds.COMPANION_YINLIN_HURT.get();
            case CHANGLI, SANHUA, DANJIN, CHIXIA, BAIZHI, JIANXIN, NANALLY, MINT, LACRIMOSA -> ModSounds.COMPANION_CHANGLI_HURT.get();
            case JINSHI -> ModSounds.COMPANION_JINSHI_HURT.get();
            case SHOREKEEPER -> ModSounds.COMPANION_SHOREKEEPER_HURT.get();
            case CAMELLYA -> ModSounds.COMPANION_CAMELLYA_HURT.get();
            case ROVER -> ModSounds.COMPANION_ROVER_HURT.get();
            case CLORINDE, YELAN, NAVIA -> ModSounds.COMPANION_CLORINDE_HURT.get();
            case SKIRK -> ModSounds.COMPANION_SKIRK_HURT.get();
            case XILONEN -> ModSounds.COMPANION_XILONEN_HURT.get();
            case MAVUIKA -> ModSounds.COMPANION_MAVUIKA_HURT.get();
            case KINICH -> ModSounds.COMPANION_KINICH_HURT.get();
            case COLUMBINA -> ModSounds.COMPANION_COLUMBINA_HURT.get();
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
