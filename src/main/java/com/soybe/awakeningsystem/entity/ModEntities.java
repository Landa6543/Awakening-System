package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, AwakeningSystemMod.MOD_ID);

    public static final Supplier<EntityType<ShadowInfantryEntity>> SHADOW_INFANTRY =
            ENTITY_TYPES.register("shadow_infantry", () ->
                    EntityType.Builder.of(ShadowInfantryEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("shadow_infantry"));

    public static final Supplier<EntityType<AbyssalOrcEntity>> ABYSSAL_ORC =
            ENTITY_TYPES.register("abyssal_orc", () ->
                    EntityType.Builder.of(AbyssalOrcEntity::new, MobCategory.MONSTER)
                            .sized(0.85F, 2.2F)
                            .build("abyssal_orc"));

    public static final Supplier<EntityType<ShadowMonarchBossEntity>> SHADOW_MONARCH_BOSS =
            ENTITY_TYPES.register("shadow_monarch_boss", () ->
                    EntityType.Builder.of(ShadowMonarchBossEntity::new, MobCategory.MONSTER)
                            .sized(1.35F, 3.4F)
                            .eyeHeight(2.9F)
                            .build("shadow_monarch_boss"));

    public static final Supplier<EntityType<AbyssalNecromancerEntity>> ABYSSAL_NECROMANCER =
            ENTITY_TYPES.register("abyssal_necromancer", () ->
                    EntityType.Builder.of(AbyssalNecromancerEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 2.1F)
                            .build("abyssal_necromancer"));

    public static final Supplier<EntityType<FrostElfAssassinEntity>> FROST_ELF_ASSASSIN =
            ENTITY_TYPES.register("frost_elf_assassin", () ->
                    EntityType.Builder.of(FrostElfAssassinEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("frost_elf_assassin"));

    // Soldado Hormiga Gigante (Ant Soldier / Beru - Quimera de Choque)
    public static final Supplier<EntityType<AntSoldierEntity>> ANT_SOLDIER =
            ENTITY_TYPES.register("ant_soldier", () ->
                    EntityType.Builder.<AntSoldierEntity>of(AntSoldierEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 2.4F)
                            .eyeHeight(2.1F)
                            .clientTrackingRange(36)
                            .updateInterval(2)
                            .build("ant_soldier"));

    // Ojo Vigilante del Abismo (Abyssal Beholder - Ojo colosal de Cthulhu flotante)
    public static final Supplier<EntityType<AbyssalBeholderEntity>> ABYSSAL_BEHOLDER =
            ENTITY_TYPES.register("abyssal_beholder", () ->
                    EntityType.Builder.<AbyssalBeholderEntity>of(AbyssalBeholderEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 2.4F)
                            .eyeHeight(1.2F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("abyssal_beholder"));

    // Slime de Fantasía Isekai (Gota anime redondeada multicolor)
    public static final Supplier<EntityType<IsekaiSlimeEntity>> ISEKAI_SLIME =
            ENTITY_TYPES.register("isekai_slime", () ->
                    EntityType.Builder.<IsekaiSlimeEntity>of(IsekaiSlimeEntity::new, MobCategory.MONSTER)
                            .sized(0.75F, 0.85F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("isekai_slime"));

    // Slime Elemental de Genshin Impact (7 elementos con ataques y silueta canónica)
    public static final Supplier<EntityType<GenshinSlimeEntity>> GENSHIN_SLIME =
            ENTITY_TYPES.register("genshin_slime", () ->
                    EntityType.Builder.<GenshinSlimeEntity>of(GenshinSlimeEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 1.15F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("genshin_slime"));

    // Hilichurl de Genshin Impact (Máscara de hueso tribal, Berserker/Guardia/Tirador)
    public static final Supplier<EntityType<HilichurlEntity>> HILICHURL =
            ENTITY_TYPES.register("hilichurl", () ->
                    EntityType.Builder.<HilichurlEntity>of(HilichurlEntity::new, MobCategory.MONSTER)
                            .sized(0.65F, 1.55F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("hilichurl"));

    // Mitachurl de Genshin Impact (Cacique colosal con cuernos masivos, Hacha ardiente y Muro de escudos)
    public static final Supplier<EntityType<MitachurlEntity>> MITACHURL =
            ENTITY_TYPES.register("mitachurl", () ->
                    EntityType.Builder.<MitachurlEntity>of(MitachurlEntity::new, MobCategory.MONSTER)
                            .sized(1.85F, 3.7F)
                            .eyeHeight(3.0F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("mitachurl"));

    // Zorro Espiritual de Nueve Colas del Reino Murim
    public static final Supplier<EntityType<SpiritFoxEntity>> SPIRIT_FOX =
            ENTITY_TYPES.register("spirit_fox", () ->
                    EntityType.Builder.<SpiritFoxEntity>of(SpiritFoxEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.85F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("spirit_fox"));

    // Dragón Bebé de Cristal Astral (Criatura mística voladora del Reino Isekai)
    public static final Supplier<EntityType<AstralDragonEntity>> ASTRAL_DRAGON =
            ENTITY_TYPES.register("astral_dragon", () ->
                    EntityType.Builder.<AstralDragonEntity>of(AstralDragonEntity::new, MobCategory.CREATURE)
                            .sized(0.65F, 0.6F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("astral_dragon"));

    // Lobo Plateado de las Sombras (Shadow Direwolf - Ranga)
    public static final Supplier<EntityType<ShadowDirewolfEntity>> SHADOW_DIREWOLF =
            ENTITY_TYPES.register("shadow_direwolf", () ->
                    EntityType.Builder.<ShadowDirewolfEntity>of(ShadowDirewolfEntity::new, MobCategory.CREATURE)
                            .sized(1.5F, 1.6F)
                            .eyeHeight(1.2F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("shadow_direwolf"));

    // Tigre Blanco del Trueno (White Thunder Tiger / Byakko)
    public static final Supplier<EntityType<WhiteThunderTigerEntity>> WHITE_THUNDER_TIGER =
            ENTITY_TYPES.register("white_thunder_tiger", () ->
                    EntityType.Builder.<WhiteThunderTigerEntity>of(WhiteThunderTigerEntity::new, MobCategory.CREATURE)
                            .sized(1.7F, 1.7F)
                            .eyeHeight(1.3F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("white_thunder_tiger"));

    // Grulla Celestial de las Nubes (Cloud Crane - Montura voladora mística de cultivadores)
    public static final Supplier<EntityType<CloudCraneEntity>> CLOUD_CRANE =
            ENTITY_TYPES.register("cloud_crane", () ->
                    EntityType.Builder.<CloudCraneEntity>of(CloudCraneEntity::new, MobCategory.CREATURE)
                            .sized(1.0F, 2.2F)
                            .eyeHeight(1.8F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("cloud_crane"));

    // Hada de Maná Silvana (Mana Pixie - Ser feérico alado del Árbol del Mundo)
    public static final Supplier<EntityType<ManaPixieEntity>> MANA_PIXIE =
            ENTITY_TYPES.register("mana_pixie", () ->
                    EntityType.Builder.<ManaPixieEntity>of(ManaPixieEntity::new, MobCategory.CREATURE)
                            .sized(0.45F, 0.55F)
                            .eyeHeight(0.35F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("mana_pixie"));

    // Mandrágora Cantarina (Screaming Mandrake - Planta mágica viva que grita al cosecharse)
    public static final Supplier<EntityType<ScreamingMandrakeEntity>> SCREAMING_MANDRAKE =
            ENTITY_TYPES.register("screaming_mandrake", () ->
                    EntityType.Builder.<ScreamingMandrakeEntity>of(ScreamingMandrakeEntity::new, MobCategory.CREATURE)
                            .sized(0.55F, 0.65F)
                            .eyeHeight(0.40F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("screaming_mandrake"));

    // Dragón Chino Celestial Ancestral de Murim (Shenlong / Qinglong)
    public static final Supplier<EntityType<CelestialDragonEntity>> CELESTIAL_DRAGON =
            ENTITY_TYPES.register("celestial_dragon", () ->
                    EntityType.Builder.<CelestialDragonEntity>of(CelestialDragonEntity::new, MobCategory.MONSTER)
                            .sized(3.6F, 4.2F)
                            .eyeHeight(3.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("celestial_dragon"));

    // Dragón Celestial Ancestral Titánico (Variante Boss +100 bloques, robusto y destructor de bloques)
    public static final Supplier<EntityType<AncientCelestialDragonBossEntity>> ANCIENT_CELESTIAL_DRAGON =
            ENTITY_TYPES.register("ancient_celestial_dragon", () ->
                    EntityType.Builder.<AncientCelestialDragonBossEntity>of(AncientCelestialDragonBossEntity::new, MobCategory.MONSTER)
                            .sized(5.5F, 5.5F)
                            .eyeHeight(5.0F)
                            .clientTrackingRange(128)
                            .updateInterval(2)
                            .build("ancient_celestial_dragon"));

    // Goblins del Reino Isekai
    public static final Supplier<EntityType<GoblinScoutEntity>> GOBLIN_SCOUT =
            ENTITY_TYPES.register("goblin_scout", () ->
                    EntityType.Builder.<GoblinScoutEntity>of(GoblinScoutEntity::new, MobCategory.MONSTER)
                            .sized(0.55F, 1.1F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("goblin_scout"));

    public static final Supplier<EntityType<GoblinWarriorEntity>> GOBLIN_WARRIOR =
            ENTITY_TYPES.register("goblin_warrior", () ->
                    EntityType.Builder.<GoblinWarriorEntity>of(GoblinWarriorEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 1.35F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("goblin_warrior"));

    public static final Supplier<EntityType<GoblinShamanEntity>> GOBLIN_SHAMAN =
            ENTITY_TYPES.register("goblin_shaman", () ->
                    EntityType.Builder.<GoblinShamanEntity>of(GoblinShamanEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.25F)
                            .clientTrackingRange(36)
                            .updateInterval(2)
                            .build("goblin_shaman"));

    // Hombre Bestia del Reino Isekai
    public static final Supplier<EntityType<BeastmanWarriorEntity>> BEASTMAN_WARRIOR =
            ENTITY_TYPES.register("beastman_warrior", () ->
                    EntityType.Builder.<BeastmanWarriorEntity>of(BeastmanWarriorEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 2.2F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("beastman_warrior"));

    // Jiangshi (Zombi Saltador Taoísta de Murim)
    public static final Supplier<EntityType<JiangshiEntity>> JIANGSHI =
            ENTITY_TYPES.register("jiangshi", () ->
                    EntityType.Builder.<JiangshiEntity>of(JiangshiEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(36)
                            .updateInterval(2)
                            .build("jiangshi"));

    // Cultivador Hereje del Culto Demoníaco de Murim
    public static final Supplier<EntityType<DemonicCultivatorEntity>> DEMONIC_CULTIVATOR =
            ENTITY_TYPES.register("demonic_cultivator", () ->
                    EntityType.Builder.<DemonicCultivatorEntity>of(DemonicCultivatorEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(40)
                            .updateInterval(2)
                            .build("demonic_cultivator"));

    // Proyectil de Tajo de Espada de Qi Carmesí
    public static final Supplier<EntityType<DemonicSwordQiEntity>> DEMONIC_SWORD_QI =
            ENTITY_TYPES.register("demonic_sword_qi", () ->
                    EntityType.Builder.<DemonicSwordQiEntity>of(DemonicSwordQiEntity::new, MobCategory.MISC)
                            .sized(0.6F, 0.6F)
                            .clientTrackingRange(48)
                            .updateInterval(1)
                            .build("demonic_sword_qi"));

    // Espada Voladora Teledirigida de Murim (Jade Feijian)
    public static final Supplier<EntityType<JadeFeijianEntity>> JADE_FEIJIAN =
            ENTITY_TYPES.register("jade_feijian", () ->
                    EntityType.Builder.<JadeFeijianEntity>of(JadeFeijianEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("jade_feijian"));

    // Creciente de Dragón Celestial (Guandao Astral)
    public static final Supplier<EntityType<AstralDragonCrescentEntity>> ASTRAL_DRAGON_CRESCENT =
            ENTITY_TYPES.register("astral_dragon_crescent", () ->
                    EntityType.Builder.<AstralDragonCrescentEntity>of(AstralDragonCrescentEntity::new, MobCategory.MISC)
                            .sized(1.2F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("astral_dragon_crescent"));

    // Bestia Acorazada de Jade Espiritual de Murim
    public static final Supplier<EntityType<JadeArmoredBeastEntity>> JADE_ARMORED_BEAST =
            ENTITY_TYPES.register("jade_armored_beast", () ->
                    EntityType.Builder.<JadeArmoredBeastEntity>of(JadeArmoredBeastEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 1.0F)
                            .clientTrackingRange(40)
                            .updateInterval(2)
                            .build("jade_armored_beast"));

    // Discípulo de la Secta del Monte Hua de Murim (Espadachín Ortodoxo)
    public static final Supplier<EntityType<MountHuaDiscipleEntity>> MOUNT_HUA_DISCIPLE =
            ENTITY_TYPES.register("mount_hua_disciple", () ->
                    EntityType.Builder.<MountHuaDiscipleEntity>of(MountHuaDiscipleEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(40)
                            .updateInterval(2)
                            .build("mount_hua_disciple"));


    // Proyectil de Aliento de Qi Celestial
    public static final Supplier<EntityType<CelestialDragonBreathEntity>> CELESTIAL_DRAGON_BREATH =
            ENTITY_TYPES.register("celestial_dragon_breath", () ->
                    EntityType.Builder.<CelestialDragonBreathEntity>of(CelestialDragonBreathEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(48)
                            .updateInterval(1)
                            .build("celestial_dragon_breath"));

    // Proyectil de Aliento Astral
    public static final Supplier<EntityType<AstralBreathProjectileEntity>> ASTRAL_BREATH =
            ENTITY_TYPES.register("astral_breath", () ->
                    EntityType.Builder.<AstralBreathProjectileEntity>of(AstralBreathProjectileEntity::new, MobCategory.MISC)
                            .sized(0.35F, 0.35F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build("astral_breath"));

    // Resto de Alma de Sombra (Para extracción con 'Surge')
    public static final Supplier<EntityType<ShadowSoulEntity>> SHADOW_SOUL =
            ENTITY_TYPES.register("shadow_soul", () ->
                    EntityType.Builder.<ShadowSoulEntity>of(ShadowSoulEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(32)
                            .updateInterval(3)
                            .build("shadow_soul"));

    // Asiento de Meditación de Loto para Cultivo de Qi
    public static final Supplier<EntityType<MeditationSeatEntity>> MEDITATION_SEAT =
            ENTITY_TYPES.register("meditation_seat", () ->
                    EntityType.Builder.<MeditationSeatEntity>of(MeditationSeatEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.2F)
                            .noSave()
                            .build("meditation_seat"));

    // Asiento de Silla de Taberna (Sin meditación ni cultivo de Qi)
    public static final Supplier<EntityType<TavernSeatEntity>> TAVERN_SEAT =
            ENTITY_TYPES.register("tavern_seat", () ->
                    EntityType.Builder.<TavernSeatEntity>of(TavernSeatEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.2F)
                            .noSave()
                            .build("tavern_seat"));

    // Asiento de Conducción de Metro (Control por W/S estilo Create)
    public static final Supplier<EntityType<SubwayPilotSeatEntity>> SUBWAY_PILOT_SEAT =
            ENTITY_TYPES.register("subway_pilot_seat", () ->
                    EntityType.Builder.<SubwayPilotSeatEntity>of(SubwayPilotSeatEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.2F)
                            .noSave()
                            .build("subway_pilot_seat"));

    // Espada Voladora de Cultivo 3D
    public static final Supplier<EntityType<FlyingSwordEntity>> FLYING_SWORD =
            ENTITY_TYPES.register("flying_sword", () ->
                    EntityType.Builder.<FlyingSwordEntity>of(FlyingSwordEntity::new, MobCategory.MISC)
                            .sized(1.2F, 0.4F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("flying_sword"));

    // Vagón de Metro Funcional (ORV Coche 3707)
    public static final Supplier<EntityType<SubwayTrainCarriageEntity>> SUBWAY_TRAIN_CARRIAGE =
            ENTITY_TYPES.register("subway_train_carriage", () ->
                    EntityType.Builder.<SubwayTrainCarriageEntity>of(SubwayTrainCarriageEntity::new, MobCategory.MISC)
                            .sized(3.2F, 2.6F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("subway_train_carriage"));

    // Proyectil Físico de Moneda del Despertar / Ficha de Ruleta
    public static final Supplier<EntityType<AwakeningCoinEntity>> AWAKENING_COIN_PROJECTILE =
            ENTITY_TYPES.register("awakening_coin_projectile", () ->
                    EntityType.Builder.<AwakeningCoinEntity>of(AwakeningCoinEntity::new, MobCategory.MISC)
                            .sized(0.35F, 0.35F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build("awakening_coin_projectile"));

    // NPCs de Series Manhwa
    public static final Supplier<EntityType<SeriesNpcEntity>> SERIES_NPC =
            ENTITY_TYPES.register("series_npc", () ->
                    EntityType.Builder.<SeriesNpcEntity>of(SeriesNpcEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("series_npc"));

    public static final Supplier<EntityType<SeriesNpcEntity>> SUNG_JINWOO =
            ENTITY_TYPES.register("sung_jinwoo", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.SUNG_JINWOO), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("sung_jinwoo"));

    public static final Supplier<EntityType<SeriesNpcEntity>> CHA_HAEIN =
            ENTITY_TYPES.register("cha_haein", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.CHA_HAEIN), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("cha_haein"));

    public static final Supplier<EntityType<SeriesNpcEntity>> KIM_DOKJA =
            ENTITY_TYPES.register("kim_dokja", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.KIM_DOKJA), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("kim_dokja"));

    public static final Supplier<EntityType<SeriesNpcEntity>> YOO_JOONGHYUK =
            ENTITY_TYPES.register("yoo_joonghyuk", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.YOO_JOONGHYUK), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("yoo_joonghyuk"));

    public static final Supplier<EntityType<SeriesNpcEntity>> LLOYD_FRONTERA =
            ENTITY_TYPES.register("lloyd_frontera", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.LLOYD_FRONTERA), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("lloyd_frontera"));

    public static final Supplier<EntityType<SeriesNpcEntity>> JAVIER_ASRAHAN =
            ENTITY_TYPES.register("javier_asrahan", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.JAVIER_ASRAHAN), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("javier_asrahan"));

    public static final Supplier<EntityType<SeriesNpcEntity>> CHEON_YEOWOON =
            ENTITY_TYPES.register("cheon_yeowoon", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.CHEON_YEOWOON), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("cheon_yeowoon"));

    public static final Supplier<EntityType<SeriesNpcEntity>> ARTHUR_LEYWIN =
            ENTITY_TYPES.register("arthur_leywin", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.ARTHUR_LEYWIN), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("arthur_leywin"));

    public static final Supplier<EntityType<SeriesNpcEntity>> CHA_YEONWOO =
            ENTITY_TYPES.register("cha_yeonwoo", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.CHA_YEONWOO), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("cha_yeonwoo"));

    public static final Supplier<EntityType<SeriesNpcEntity>> CHUNG_MYUNG =
            ENTITY_TYPES.register("chung_myung", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.CHUNG_MYUNG), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("chung_myung"));

    public static final Supplier<EntityType<SeriesNpcEntity>> RYU_HANBIN =
            ENTITY_TYPES.register("ryu_hanbin", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.RYU_HANBIN), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("ryu_hanbin"));

    public static final Supplier<EntityType<SeriesNpcEntity>> TWENTY_FIFTH_BAAM =
            ENTITY_TYPES.register("twenty_fifth_baam", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.TWENTY_FIFTH_BAAM), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("twenty_fifth_baam"));

    public static final Supplier<EntityType<DokkaebiEntity>> DOKKAEBI =
            ENTITY_TYPES.register("dokkaebi", () ->
                    EntityType.Builder.<DokkaebiEntity>of(DokkaebiEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.9F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("dokkaebi"));

    // Compañera Paimon del Sistema Gacha
    public static final Supplier<EntityType<PaimonEntity>> PAIMON =
            ENTITY_TYPES.register("paimon", () ->
                    EntityType.Builder.<PaimonEntity>of(PaimonEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.85F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("paimon"));

    // Compañeros Invocables del Sistema Gacha (Personajes con Skin)
    public static final Supplier<EntityType<CompanionEntity>> COMPANION =
            ENTITY_TYPES.register("companion", () ->
                    EntityType.Builder.<CompanionEntity>of(CompanionEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("companion"));

    // Wyvern Depredador Aéreo del Mundo RPG
    public static final Supplier<EntityType<WyvernEntity>> WYVERN =
            ENTITY_TYPES.register("wyvern", () ->
                    EntityType.Builder.<WyvernEntity>of(WyvernEntity::new, MobCategory.CREATURE)
                            .sized(2.2F, 2.0F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("wyvern"));

    // Jefes de Manhwa Emblemáticos
    public static final Supplier<EntityType<IgrisBossEntity>> IGRIS_BOSS =
            ENTITY_TYPES.register("igris_boss", () ->
                    EntityType.Builder.<IgrisBossEntity>of(IgrisBossEntity::new, MobCategory.MONSTER)
                            .sized(0.85F, 2.65F)
                            .eyeHeight(2.3F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("igris_boss"));

    public static final Supplier<EntityType<BaranBossEntity>> BARAN_BOSS =
            ENTITY_TYPES.register("baran_boss", () ->
                    EntityType.Builder.<BaranBossEntity>of(BaranBossEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 3.0F)
                            .eyeHeight(2.6F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("baran_boss"));

    // Entidades de la Fortaleza de la Secta de Sangre (Culto Demoníaco)
    public static final Supplier<EntityType<BloodQiTotemEntity>> BLOOD_QI_TOTEM =
            ENTITY_TYPES.register("blood_qi_totem", () ->
                    EntityType.Builder.<BloodQiTotemEntity>of(BloodQiTotemEntity::new, MobCategory.MISC)
                            .sized(0.8F, 1.8F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("blood_qi_totem"));

    public static final Supplier<EntityType<BloodSectDiscipleEntity>> BLOOD_SECT_DISCIPLE =
            ENTITY_TYPES.register("blood_sect_disciple", () ->
                    EntityType.Builder.<BloodSectDiscipleEntity>of(BloodSectDiscipleEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(36)
                            .updateInterval(2)
                            .build("blood_sect_disciple"));

    public static final Supplier<EntityType<BloodPatriarchBossEntity>> BLOOD_PATRIARCH_BOSS =
            ENTITY_TYPES.register("blood_patriarch_boss", () ->
                    EntityType.Builder.<BloodPatriarchBossEntity>of(BloodPatriarchBossEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 2.9F)
                            .eyeHeight(2.5F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("blood_patriarch_boss"));

    public static final Supplier<EntityType<YggdrasilTreantBossEntity>> YGGDRASIL_TREANT_BOSS =
            ENTITY_TYPES.register("yggdrasil_treant_boss", () ->
                    EntityType.Builder.<YggdrasilTreantBossEntity>of(YggdrasilTreantBossEntity::new, MobCategory.MONSTER)
                            .sized(3.0F, 5.4F)
                            .eyeHeight(4.5F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("yggdrasil_treant_boss"));

    public static final Supplier<EntityType<JadeGuardianBossEntity>> JADE_GUARDIAN_BOSS =
            ENTITY_TYPES.register("jade_guardian_boss", () ->
                    EntityType.Builder.<JadeGuardianBossEntity>of(JadeGuardianBossEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 4.2F)
                            .eyeHeight(3.6F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("jade_guardian_boss"));

    public static final Supplier<EntityType<ElementalDemonKingBossEntity>> ELEMENTAL_DEMON_KING =
            ENTITY_TYPES.register("elemental_demon_king", () ->
                    EntityType.Builder.<ElementalDemonKingBossEntity>of(ElementalDemonKingBossEntity::new, MobCategory.MONSTER)
                            .sized(1.7F, 3.9F)
                            .eyeHeight(3.4F)
                            .fireImmune()
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("elemental_demon_king"));

    public static final Supplier<EntityType<ElementalLordBossEntity>> ELEMENTAL_LORD =
            ENTITY_TYPES.register("elemental_lord", () ->
                    EntityType.Builder.<ElementalLordBossEntity>of(ElementalLordBossEntity::new, MobCategory.MONSTER)
                            .sized(1.25F, 3.0F)
                            .eyeHeight(2.6F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("elemental_lord"));

    // Variantes de Bestias de Maná / Animales Místicos
    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.animal.ManaCowEntity>> MANA_COW =
            ENTITY_TYPES.register("mana_cow", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.animal.ManaCowEntity>of(com.soybe.awakeningsystem.entity.animal.ManaCowEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.4F)
                            .clientTrackingRange(10)
                            .build("mana_cow"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.animal.ManaPigEntity>> MANA_PIG =
            ENTITY_TYPES.register("mana_pig", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.animal.ManaPigEntity>of(com.soybe.awakeningsystem.entity.animal.ManaPigEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 0.9F)
                            .clientTrackingRange(10)
                            .build("mana_pig"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.animal.ManaSheepEntity>> MANA_SHEEP =
            ENTITY_TYPES.register("mana_sheep", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.animal.ManaSheepEntity>of(com.soybe.awakeningsystem.entity.animal.ManaSheepEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.3F)
                            .clientTrackingRange(10)
                            .build("mana_sheep"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.animal.ManaChickenEntity>> MANA_CHICKEN =
            ENTITY_TYPES.register("mana_chicken", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.animal.ManaChickenEntity>of(com.soybe.awakeningsystem.entity.animal.ManaChickenEntity::new, MobCategory.CREATURE)
                            .sized(0.4F, 0.7F)
                            .clientTrackingRange(10)
                            .build("mana_chicken"));

    // Dragón Ancestral Occidental (True Hexapod Dragon)
    public static final Supplier<EntityType<AncientDragonEntity>> ANCIENT_DRAGON =
            ENTITY_TYPES.register("ancient_dragon", () ->
                    EntityType.Builder.<AncientDragonEntity>of(AncientDragonEntity::new, MobCategory.CREATURE)
                            .sized(6.0F, 5.0F)
                            .eyeHeight(4.8F)
                            .clientTrackingRange(128)
                            .updateInterval(2)
                            .build("ancient_dragon"));

    // Proyectil de Aliento Elemental Dracónico
    public static final Supplier<EntityType<AncientDragonBreathEntity>> ANCIENT_DRAGON_BREATH =
            ENTITY_TYPES.register("ancient_dragon_breath", () ->
                    EntityType.Builder.<AncientDragonBreathEntity>of(AncientDragonBreathEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("ancient_dragon_breath"));

    // Monolito de las Líneas Ley de Genshin Impact (Abismo)
    public static final Supplier<EntityType<LeyLineMonolithEntity>> LEY_LINE_MONOLITH =
            ENTITY_TYPES.register("ley_line_monolith", () ->
                    EntityType.Builder.<LeyLineMonolithEntity>of(LeyLineMonolithEntity::new, MobCategory.MISC)
                            .sized(1.6F, 3.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("ley_line_monolith"));

    // Llave Roja de Desafío del Abismo de Genshin Impact
    public static final Supplier<EntityType<AbyssChallengeKeyEntity>> ABYSS_CHALLENGE_KEY =
            ENTITY_TYPES.register("abyss_challenge_key", () ->
                    EntityType.Builder.<AbyssChallengeKeyEntity>of(AbyssChallengeKeyEntity::new, MobCategory.MISC)
                            .sized(1.0F, 2.0F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("abyss_challenge_key"));

    // === JEFES HIPÓSTASIS ELEMENTALES DE GENSHIN IMPACT ===
    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.hypostasis.AnemoHypostasisBossEntity>> ANEMO_HYPOSTASIS =
            ENTITY_TYPES.register("anemo_hypostasis", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.hypostasis.AnemoHypostasisBossEntity>of(com.soybe.awakeningsystem.entity.hypostasis.AnemoHypostasisBossEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 2.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("anemo_hypostasis"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.hypostasis.ElectroHypostasisBossEntity>> ELECTRO_HYPOSTASIS =
            ENTITY_TYPES.register("electro_hypostasis", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.hypostasis.ElectroHypostasisBossEntity>of(com.soybe.awakeningsystem.entity.hypostasis.ElectroHypostasisBossEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 2.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("electro_hypostasis"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.hypostasis.AnemoRevivalOrbEntity>> ANEMO_REVIVAL_ORB =
            ENTITY_TYPES.register("anemo_revival_orb", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.hypostasis.AnemoRevivalOrbEntity>of(com.soybe.awakeningsystem.entity.hypostasis.AnemoRevivalOrbEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("anemo_revival_orb"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.hypostasis.ElectroRevivalPrismEntity>> ELECTRO_REVIVAL_PRISM =
            ENTITY_TYPES.register("electro_revival_prism", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.hypostasis.ElectroRevivalPrismEntity>of(com.soybe.awakeningsystem.entity.hypostasis.ElectroRevivalPrismEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 1.6F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("electro_revival_prism"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.hypostasis.GeoHypostasisBossEntity>> GEO_HYPOSTASIS =
            ENTITY_TYPES.register("geo_hypostasis", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.hypostasis.GeoHypostasisBossEntity>of(com.soybe.awakeningsystem.entity.hypostasis.GeoHypostasisBossEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 2.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("geo_hypostasis"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.hypostasis.CryoHypostasisBossEntity>> CRYO_HYPOSTASIS =
            ENTITY_TYPES.register("cryo_hypostasis", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.hypostasis.CryoHypostasisBossEntity>of(com.soybe.awakeningsystem.entity.hypostasis.CryoHypostasisBossEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 2.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("cryo_hypostasis"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.hypostasis.PyroHypostasisBossEntity>> PYRO_HYPOSTASIS =
            ENTITY_TYPES.register("pyro_hypostasis", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.hypostasis.PyroHypostasisBossEntity>of(com.soybe.awakeningsystem.entity.hypostasis.PyroHypostasisBossEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 2.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("pyro_hypostasis"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.hypostasis.HydroHypostasisBossEntity>> HYDRO_HYPOSTASIS =
            ENTITY_TYPES.register("hydro_hypostasis", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.hypostasis.HydroHypostasisBossEntity>of(com.soybe.awakeningsystem.entity.hypostasis.HydroHypostasisBossEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 2.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("hydro_hypostasis"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.hypostasis.DendroHypostasisBossEntity>> DENDRO_HYPOSTASIS =
            ENTITY_TYPES.register("dendro_hypostasis", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.hypostasis.DendroHypostasisBossEntity>of(com.soybe.awakeningsystem.entity.hypostasis.DendroHypostasisBossEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 2.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("dendro_hypostasis"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.entity.hypostasis.DendroRevivalPithEntity>> DENDRO_REVIVAL_PITH =
            ENTITY_TYPES.register("dendro_revival_pith", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.entity.hypostasis.DendroRevivalPithEntity>of(com.soybe.awakeningsystem.entity.hypostasis.DendroRevivalPithEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 1.6F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("dendro_revival_pith"));

    // === ENTIDADES LUNARES CUSTOM DEL ALTAR TRILUNAR ===
    public static final Supplier<EntityType<LunarShadowEntity>> LUNAR_SHADOW =
            ENTITY_TYPES.register("lunar_shadow", () ->
                    EntityType.Builder.<LunarShadowEntity>of(LunarShadowEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("lunar_shadow"));

    public static final Supplier<EntityType<LunarGuardianEntity>> LUNAR_GUARDIAN =
            ENTITY_TYPES.register("lunar_guardian", () ->
                    EntityType.Builder.<LunarGuardianEntity>of(LunarGuardianEntity::new, MobCategory.MONSTER)
                            .sized(1.75F, 4.0F)
                            .eyeHeight(3.5F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("lunar_guardian"));

    // === JEFE WILDFIRE ARDIENTE ===
    public static final Supplier<EntityType<WildfireEntity>> WILDFIRE =
            ENTITY_TYPES.register("wildfire", () ->
                    EntityType.Builder.<WildfireEntity>of(WildfireEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 2.6F)
                            .eyeHeight(2.2F)
                            .fireImmune()
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("wildfire"));

    // === GLARE (MIRADA) ===
    public static final Supplier<EntityType<GlareEntity>> GLARE =
            ENTITY_TYPES.register("glare", () ->
                    EntityType.Builder.<GlareEntity>of(GlareEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.9F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("glare"));

    // === AUTÓMATAS DE RUINAS (GENSHIN IMPACT) ===
    public static final Supplier<EntityType<RuinGuardEntity>> RUIN_GUARD =
            ENTITY_TYPES.register("ruin_guard", () ->
                    EntityType.Builder.<RuinGuardEntity>of(RuinGuardEntity::new, MobCategory.MONSTER)
                            .sized(3.25F, 4.5F)
                            .eyeHeight(3.6F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("ruin_guard"));

    public static final Supplier<EntityType<RuinHunterEntity>> RUIN_HUNTER =
            ENTITY_TYPES.register("ruin_hunter", () ->
                    EntityType.Builder.<RuinHunterEntity>of(RuinHunterEntity::new, MobCategory.MONSTER)
                            .sized(2.5F, 3.7F)
                            .eyeHeight(3.0F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("ruin_hunter"));

    public static final Supplier<EntityType<RuinMissileEntity>> RUIN_MISSILE =
            ENTITY_TYPES.register("ruin_missile", () ->
                    EntityType.Builder.<RuinMissileEntity>of(RuinMissileEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(48)
                            .updateInterval(1)
                            .build("ruin_missile"));

    // === MITACHURL / LAWACHURL (GENSHIN IMPACT) ===
    public static final Supplier<EntityType<StonehideLawachurlEntity>> STONEHIDE_LAWACHURL =
            ENTITY_TYPES.register("stonehide_lawachurl", () ->
                    EntityType.Builder.<StonehideLawachurlEntity>of(StonehideLawachurlEntity::new, MobCategory.MONSTER)
                            .sized(3.6F, 5.0F)
                            .eyeHeight(4.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("stonehide_lawachurl"));

    // === REGISVID CRIOGÉNICA / CRYO REGISVINE (GENSHIN IMPACT) ===
    public static final Supplier<EntityType<CryoRegisvineEntity>> CRYO_REGISVINE =
            ENTITY_TYPES.register("cryo_regisvine", () ->
                    EntityType.Builder.<CryoRegisvineEntity>of(CryoRegisvineEntity::new, MobCategory.MONSTER)
                            .sized(4.8F, 8.2F)
                            .eyeHeight(6.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("cryo_regisvine"));

    // === REGISVID PIROTÉCNICA / PYRO REGISVINE (GENSHIN IMPACT) ===
    public static final Supplier<EntityType<PyroRegisvineEntity>> PYRO_REGISVINE =
            ENTITY_TYPES.register("pyro_regisvine", () ->
                    EntityType.Builder.<PyroRegisvineEntity>of(PyroRegisvineEntity::new, MobCategory.MONSTER)
                            .sized(4.8F, 8.2F)
                            .eyeHeight(6.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("pyro_regisvine"));

    // === REGISVID ELECTRO / ELECTRO REGISVINE (GENSHIN IMPACT) ===
    public static final Supplier<EntityType<ElectroRegisvineEntity>> ELECTRO_REGISVINE =
            ENTITY_TYPES.register("electro_regisvine", () ->
                    EntityType.Builder.<ElectroRegisvineEntity>of(ElectroRegisvineEntity::new, MobCategory.MONSTER)
                            .sized(4.8F, 8.2F)
                            .eyeHeight(6.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("electro_regisvine"));

    public static final Supplier<EntityType<HoarfrostShardEntity>> HOARFROST_SHARD =
            ENTITY_TYPES.register("hoarfrost_shard", () ->
                    EntityType.Builder.<HoarfrostShardEntity>of(HoarfrostShardEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(48)
                            .updateInterval(1)
                            .build("hoarfrost_shard"));

    // === JEFES MAZMORRAS LEGENDARIAS DE TEYVAT ===
    public static final Supplier<EntityType<ChildeBossEntity>> CHILDE_BOSS =
            ENTITY_TYPES.register("childe_boss", () ->
                    EntityType.Builder.<ChildeBossEntity>of(ChildeBossEntity::new, MobCategory.MONSTER)
                            .sized(0.95F, 2.45F)
                            .eyeHeight(2.1F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("childe_boss"));

    public static final Supplier<EntityType<AzhdahaBossEntity>> AZHDAHA_BOSS =
            ENTITY_TYPES.register("azhdaha_boss", () ->
                    EntityType.Builder.<AzhdahaBossEntity>of(AzhdahaBossEntity::new, MobCategory.MONSTER)
                            .sized(11.0F, 6.5F)
                            .eyeHeight(4.5F)
                            .clientTrackingRange(96)
                            .updateInterval(2)
                            .build("azhdaha_boss"));

    public static final Supplier<EntityType<RaidenBossEntity>> RAIDEN_SHOGUN_BOSS =
            ENTITY_TYPES.register("raiden_shogun_boss", () ->
                    EntityType.Builder.<RaidenBossEntity>of(RaidenBossEntity::new, MobCategory.MONSTER)
                            .sized(0.95F, 2.45F)
                            .eyeHeight(2.1F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("raiden_shogun_boss"));

    public static final Supplier<EntityType<GoldenWolflordBossEntity>> GOLDEN_WOLFLORD_BOSS =
            ENTITY_TYPES.register("golden_wolflord_boss", () ->
                    EntityType.Builder.<GoldenWolflordBossEntity>of(GoldenWolflordBossEntity::new, MobCategory.MONSTER)
                            .sized(6.5F, 4.5F)
                            .eyeHeight(3.2F)
                            .clientTrackingRange(80)
                            .updateInterval(2)
                            .build("golden_wolflord_boss"));

    // === MAGOS DEL ABISMO / ABYSS MAGES (GENSHIN IMPACT) ===
    public static final Supplier<EntityType<AbyssMagePyroEntity>> ABYSS_MAGE_PYRO =
            ENTITY_TYPES.register("abyss_mage_pyro", () ->
                    EntityType.Builder.<AbyssMagePyroEntity>of(AbyssMagePyroEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 1.8F)
                            .eyeHeight(1.5F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("abyss_mage_pyro"));

    public static final Supplier<EntityType<AbyssMageHydroEntity>> ABYSS_MAGE_HYDRO =
            ENTITY_TYPES.register("abyss_mage_hydro", () ->
                    EntityType.Builder.<AbyssMageHydroEntity>of(AbyssMageHydroEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 1.8F)
                            .eyeHeight(1.5F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("abyss_mage_hydro"));

    public static final Supplier<EntityType<AbyssMageCryoEntity>> ABYSS_MAGE_CRYO =
            ENTITY_TYPES.register("abyss_mage_cryo", () ->
                    EntityType.Builder.<AbyssMageCryoEntity>of(AbyssMageCryoEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 1.8F)
                            .eyeHeight(1.5F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("abyss_mage_cryo"));

    public static final Supplier<EntityType<AbyssMageElectroEntity>> ABYSS_MAGE_ELECTRO =
            ENTITY_TYPES.register("abyss_mage_electro", () ->
                    EntityType.Builder.<AbyssMageElectroEntity>of(AbyssMageElectroEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 1.8F)
                            .eyeHeight(1.5F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("abyss_mage_electro"));

    public static final Supplier<EntityType<AbyssBubbleEntity>> ABYSS_BUBBLE =
            ENTITY_TYPES.register("abyss_bubble", () ->
                    EntityType.Builder.<AbyssBubbleEntity>of(AbyssBubbleEntity::new, MobCategory.MISC)
                            .sized(0.6F, 0.6F)
                            .clientTrackingRange(48)
                            .updateInterval(1)
                            .build("abyss_bubble"));

    // === JEFES MUNDIALES Y SEMANALES DE GENSHIN IMPACT ===
    public static final Supplier<EntityType<GreatSnowboarKingBossEntity>> GREAT_SNOWBOAR_KING =
            ENTITY_TYPES.register("great_snowboar_king", () ->
                    EntityType.Builder.<GreatSnowboarKingBossEntity>of(GreatSnowboarKingBossEntity::new, MobCategory.MONSTER)
                            .sized(3.6F, 3.8F)
                            .eyeHeight(2.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("great_snowboar_king"));

    public static final Supplier<EntityType<RhodeiaOceanidBossEntity>> RHODEIA_OCEANID =
            ENTITY_TYPES.register("rhodeia_oceanid", () ->
                    EntityType.Builder.<RhodeiaOceanidBossEntity>of(RhodeiaOceanidBossEntity::new, MobCategory.MONSTER)
                            .sized(3.4F, 4.5F)
                            .eyeHeight(3.2F)
                            .clientTrackingRange(80)
                            .updateInterval(2)
                            .build("rhodeia_oceanid"));

    public static final Supplier<EntityType<WaterMimicEntity>> WATER_MIMIC =
            ENTITY_TYPES.register("water_mimic", () ->
                    EntityType.Builder.<WaterMimicEntity>of(WaterMimicEntity::new, MobCategory.MONSTER)
                            .sized(1.5F, 1.75F)
                            .eyeHeight(1.3F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("water_mimic"));

    public static final Supplier<EntityType<PerpetualMechanicalArrayBossEntity>> PERPETUAL_MECHANICAL_ARRAY =
            ENTITY_TYPES.register("perpetual_mechanical_array", () ->
                    EntityType.Builder.<PerpetualMechanicalArrayBossEntity>of(PerpetualMechanicalArrayBossEntity::new, MobCategory.MONSTER)
                            .sized(4.5F, 4.5F)
                            .eyeHeight(2.25F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("perpetual_mechanical_array"));

    // === MAQUINARIA Y AUTÓMATAS DE RUINAS DE GENSHIN IMPACT ===
    public static final Supplier<EntityType<RuinSentinelEntity>> RUIN_SENTINEL =
            ENTITY_TYPES.register("ruin_sentinel", () ->
                    EntityType.Builder.<RuinSentinelEntity>of(RuinSentinelEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 2.0F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("ruin_sentinel"));

    public static final Supplier<EntityType<RuinGraderEntity>> RUIN_GRADER =
            ENTITY_TYPES.register("ruin_grader", () ->
                    EntityType.Builder.<RuinGraderEntity>of(RuinGraderEntity::new, MobCategory.MONSTER)
                            .sized(4.8F, 6.6F)
                            .eyeHeight(5.4F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("ruin_grader"));

    public static final Supplier<EntityType<RuinDrakeEntity>> RUIN_DRAKE =
            ENTITY_TYPES.register("ruin_drake", () ->
                    EntityType.Builder.<RuinDrakeEntity>of(RuinDrakeEntity::new, MobCategory.MONSTER)
                            .sized(3.4F, 4.2F)
                            .eyeHeight(3.5F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("ruin_drake"));

    public static final Supplier<EntityType<RuinSerpentBossEntity>> RUIN_SERPENT =
            ENTITY_TYPES.register("ruin_serpent", () ->
                    EntityType.Builder.<RuinSerpentBossEntity>of(RuinSerpentBossEntity::new, MobCategory.MONSTER)
                            .sized(4.8F, 5.6F)
                            .eyeHeight(4.5F)
                            .clientTrackingRange(80)
                            .updateInterval(2)
                            .build("ruin_serpent"));

    // === TRIBUS HILICHURL Y LAWACHURLS ELEMENTALES ===
    public static final Supplier<EntityType<FrostarmLawachurlEntity>> FROSTARM_LAWACHURL =
            ENTITY_TYPES.register("frostarm_lawachurl", () ->
                    EntityType.Builder.<FrostarmLawachurlEntity>of(FrostarmLawachurlEntity::new, MobCategory.MONSTER)
                            .sized(3.6F, 5.0F)
                            .eyeHeight(4.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("frostarm_lawachurl"));

    public static final Supplier<EntityType<ThunderhelmLawachurlEntity>> THUNDERHELM_LAWACHURL =
            ENTITY_TYPES.register("thunderhelm_lawachurl", () ->
                    EntityType.Builder.<ThunderhelmLawachurlEntity>of(ThunderhelmLawachurlEntity::new, MobCategory.MONSTER)
                            .sized(3.6F, 5.0F)
                            .eyeHeight(4.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("thunderhelm_lawachurl"));

    public static final Supplier<EntityType<HilichurlShooterEntity>> HILICHURL_SHOOTER =
            ENTITY_TYPES.register("hilichurl_shooter", () ->
                    EntityType.Builder.<HilichurlShooterEntity>of(HilichurlShooterEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.2F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("hilichurl_shooter"));

    public static final Supplier<EntityType<HilichurlBerserkerEntity>> HILICHURL_BERSERKER =
            ENTITY_TYPES.register("hilichurl_berserker", () ->
                    EntityType.Builder.<HilichurlBerserkerEntity>of(HilichurlBerserkerEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.2F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("hilichurl_berserker"));

    // === DRAGARTOS, ESPECTROS Y MEGAFLORAS ===
    public static final Supplier<EntityType<GeovishapEntity>> GEOVISHAP =
            ENTITY_TYPES.register("geovishap", () ->
                    EntityType.Builder.<GeovishapEntity>of(GeovishapEntity::new, MobCategory.MONSTER)
                            .sized(3.0F, 3.0F)
                            .eyeHeight(2.2F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("geovishap"));

    public static final Supplier<EntityType<BathysmalVishapEntity>> BATHYSMAL_VISHAP =
            ENTITY_TYPES.register("bathysmal_vishap", () ->
                    EntityType.Builder.<BathysmalVishapEntity>of(BathysmalVishapEntity::new, MobCategory.MONSTER)
                            .sized(3.3F, 3.3F)
                            .eyeHeight(2.5F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("bathysmal_vishap"));

    public static final Supplier<EntityType<SpecterEntity>> SPECTER =
            ENTITY_TYPES.register("specter", () ->
                    EntityType.Builder.<SpecterEntity>of(SpecterEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 1.2F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("specter"));

    public static final Supplier<EntityType<WhopperflowerEntity>> WHOPPERFLOWER =
            ENTITY_TYPES.register("whopperflower", () ->
                    EntityType.Builder.<WhopperflowerEntity>of(WhopperflowerEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 1.8F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("whopperflower"));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ABYSS_MAGE_PYRO.get(), AbstractAbyssMageEntity.createAbyssMageAttributes().build());
        event.put(ABYSS_MAGE_HYDRO.get(), AbstractAbyssMageEntity.createAbyssMageAttributes().build());
        event.put(ABYSS_MAGE_CRYO.get(), AbstractAbyssMageEntity.createAbyssMageAttributes().build());
        event.put(ABYSS_MAGE_ELECTRO.get(), AbstractAbyssMageEntity.createAbyssMageAttributes().build());
        event.put(CHILDE_BOSS.get(), ChildeBossEntity.createAttributes().build());
        event.put(AZHDAHA_BOSS.get(), AzhdahaBossEntity.createAttributes().build());
        event.put(RAIDEN_SHOGUN_BOSS.get(), RaidenBossEntity.createAttributes().build());
        event.put(GOLDEN_WOLFLORD_BOSS.get(), GoldenWolflordBossEntity.createAttributes().build());
        event.put(CRYO_REGISVINE.get(), CryoRegisvineEntity.createAttributes().build());
        event.put(PYRO_REGISVINE.get(), PyroRegisvineEntity.createAttributes().build());
        event.put(ELECTRO_REGISVINE.get(), ElectroRegisvineEntity.createAttributes().build());
        event.put(STONEHIDE_LAWACHURL.get(), StonehideLawachurlEntity.createAttributes().build());
        event.put(RUIN_GUARD.get(), RuinGuardEntity.createAttributes().build());
        event.put(RUIN_HUNTER.get(), RuinHunterEntity.createAttributes().build());
        event.put(GLARE.get(), GlareEntity.createAttributes().build());
        event.put(LUNAR_SHADOW.get(), LunarShadowEntity.createAttributes().build());
        event.put(LUNAR_GUARDIAN.get(), LunarGuardianEntity.createAttributes().build());
        event.put(WILDFIRE.get(), WildfireEntity.createAttributes().build());
        event.put(ANEMO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity.createHypostasisAttributes().build());
        event.put(ELECTRO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity.createHypostasisAttributes().build());
        event.put(GEO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity.createHypostasisAttributes().build());
        event.put(CRYO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity.createHypostasisAttributes().build());
        event.put(PYRO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity.createHypostasisAttributes().build());
        event.put(HYDRO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity.createHypostasisAttributes().build());
        event.put(DENDRO_HYPOSTASIS.get(), com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity.createHypostasisAttributes().build());
        event.put(ANEMO_REVIVAL_ORB.get(), com.soybe.awakeningsystem.entity.hypostasis.AnemoRevivalOrbEntity.createAttributes().build());
        event.put(ELECTRO_REVIVAL_PRISM.get(), com.soybe.awakeningsystem.entity.hypostasis.ElectroRevivalPrismEntity.createAttributes().build());
        event.put(DENDRO_REVIVAL_PITH.get(), com.soybe.awakeningsystem.entity.hypostasis.DendroRevivalPithEntity.createAttributes().build());
        event.put(ANCIENT_DRAGON.get(), AncientDragonEntity.createAttributes().build());
        event.put(MANA_COW.get(), com.soybe.awakeningsystem.entity.animal.ManaCowEntity.createAttributes().build());
        event.put(MANA_PIG.get(), com.soybe.awakeningsystem.entity.animal.ManaPigEntity.createAttributes().build());
        event.put(MANA_SHEEP.get(), com.soybe.awakeningsystem.entity.animal.ManaSheepEntity.createAttributes().build());
        event.put(MANA_CHICKEN.get(), com.soybe.awakeningsystem.entity.animal.ManaChickenEntity.createAttributes().build());
        event.put(SHADOW_INFANTRY.get(), ShadowInfantryEntity.createAttributes().build());
        event.put(ABYSSAL_ORC.get(), AbyssalOrcEntity.createAttributes().build());
        event.put(SHADOW_MONARCH_BOSS.get(), ShadowMonarchBossEntity.createAttributes().build());
        event.put(ABYSSAL_NECROMANCER.get(), AbyssalNecromancerEntity.createAttributes().build());
        event.put(FROST_ELF_ASSASSIN.get(), FrostElfAssassinEntity.createAttributes().build());
        event.put(ANT_SOLDIER.get(), AntSoldierEntity.createAttributes().build());
        event.put(ABYSSAL_BEHOLDER.get(), AbyssalBeholderEntity.createAttributes().build());
        event.put(ISEKAI_SLIME.get(), IsekaiSlimeEntity.createAttributes().build());
        event.put(SPIRIT_FOX.get(), SpiritFoxEntity.createAttributes().build());
        event.put(ASTRAL_DRAGON.get(), AstralDragonEntity.createAttributes().build());
        event.put(SHADOW_DIREWOLF.get(), ShadowDirewolfEntity.createAttributes().build());
        event.put(WHITE_THUNDER_TIGER.get(), WhiteThunderTigerEntity.createAttributes().build());
        event.put(CLOUD_CRANE.get(), CloudCraneEntity.createAttributes().build());
        event.put(MANA_PIXIE.get(), ManaPixieEntity.createAttributes().build());
        event.put(SCREAMING_MANDRAKE.get(), ScreamingMandrakeEntity.createAttributes().build());
        event.put(CELESTIAL_DRAGON.get(), CelestialDragonEntity.createAttributes().build());
        event.put(ANCIENT_CELESTIAL_DRAGON.get(), AncientCelestialDragonBossEntity.createAttributes().build());


        // Atributos de NPCs
        var npcAttrs = SeriesNpcEntity.createAttributes().build();
        event.put(SERIES_NPC.get(), npcAttrs);
        event.put(SUNG_JINWOO.get(), npcAttrs);
        event.put(CHA_HAEIN.get(), npcAttrs);
        event.put(KIM_DOKJA.get(), npcAttrs);
        event.put(YOO_JOONGHYUK.get(), npcAttrs);
        event.put(LLOYD_FRONTERA.get(), npcAttrs);
        event.put(JAVIER_ASRAHAN.get(), npcAttrs);
        event.put(CHEON_YEOWOON.get(), npcAttrs);
        event.put(ARTHUR_LEYWIN.get(), npcAttrs);
        event.put(CHA_YEONWOO.get(), npcAttrs);
        event.put(CHUNG_MYUNG.get(), npcAttrs);
        event.put(RYU_HANBIN.get(), npcAttrs);
        event.put(TWENTY_FIFTH_BAAM.get(), npcAttrs);
        event.put(DOKKAEBI.get(), DokkaebiEntity.createAttributes().build());
        event.put(PAIMON.get(), PaimonEntity.createAttributes().build());
        event.put(COMPANION.get(), CompanionEntity.createAttributes().build());
        event.put(WYVERN.get(), WyvernEntity.createAttributes().build());
        event.put(IGRIS_BOSS.get(), IgrisBossEntity.createAttributes().build());
        event.put(BARAN_BOSS.get(), BaranBossEntity.createAttributes().build());
        event.put(BLOOD_QI_TOTEM.get(), BloodQiTotemEntity.createAttributes().build());
        event.put(BLOOD_SECT_DISCIPLE.get(), BloodSectDiscipleEntity.createAttributes().build());
        event.put(BLOOD_PATRIARCH_BOSS.get(), BloodPatriarchBossEntity.createAttributes().build());
        event.put(YGGDRASIL_TREANT_BOSS.get(), YggdrasilTreantBossEntity.createAttributes().build());
        event.put(JADE_GUARDIAN_BOSS.get(), JadeGuardianBossEntity.createAttributes().build());
        event.put(ELEMENTAL_DEMON_KING.get(), ElementalDemonKingBossEntity.createAttributes().build());
        event.put(ELEMENTAL_LORD.get(), ElementalLordBossEntity.createAttributes().build());

        // Nuevos Mobs Isekai & Murim
        event.put(GOBLIN_SCOUT.get(), GoblinScoutEntity.createAttributes().build());
        event.put(GOBLIN_WARRIOR.get(), GoblinWarriorEntity.createAttributes().build());
        event.put(GOBLIN_SHAMAN.get(), GoblinShamanEntity.createAttributes().build());
        event.put(BEASTMAN_WARRIOR.get(), BeastmanWarriorEntity.createAttributes().build());
        event.put(JIANGSHI.get(), JiangshiEntity.createAttributes().build());
        event.put(DEMONIC_CULTIVATOR.get(), DemonicCultivatorEntity.createAttributes().build());
        event.put(JADE_ARMORED_BEAST.get(), JadeArmoredBeastEntity.createAttributes().build());
        event.put(MOUNT_HUA_DISCIPLE.get(), MountHuaDiscipleEntity.createAttributes().build());

        // Mobs Canónicos de Genshin Impact
        event.put(GENSHIN_SLIME.get(), GenshinSlimeEntity.createAttributes().build());
        event.put(HILICHURL.get(), HilichurlEntity.createAttributes().build());
        event.put(MITACHURL.get(), MitachurlEntity.createAttributes().build());

        // Elementos Emblemáticos del Abismo de Genshin Impact
        event.put(LEY_LINE_MONOLITH.get(), LeyLineMonolithEntity.createAttributes().build());
        event.put(ABYSS_CHALLENGE_KEY.get(), AbyssChallengeKeyEntity.createAttributes().build());

        // Nuevos Jefes y Criaturas de Genshin Impact (Árbol 407 Criaturas)
        event.put(GREAT_SNOWBOAR_KING.get(), GreatSnowboarKingBossEntity.createAttributes().build());
        event.put(RHODEIA_OCEANID.get(), RhodeiaOceanidBossEntity.createAttributes().build());
        event.put(WATER_MIMIC.get(), WaterMimicEntity.createAttributes().build());
        event.put(PERPETUAL_MECHANICAL_ARRAY.get(), PerpetualMechanicalArrayBossEntity.createAttributes().build());
        event.put(RUIN_SENTINEL.get(), RuinSentinelEntity.createAttributes().build());
        event.put(RUIN_GRADER.get(), RuinGraderEntity.createAttributes().build());
        event.put(RUIN_DRAKE.get(), RuinDrakeEntity.createAttributes().build());
        event.put(RUIN_SERPENT.get(), RuinSerpentBossEntity.createAttributes().build());
        event.put(FROSTARM_LAWACHURL.get(), FrostarmLawachurlEntity.createAttributes().build());
        event.put(THUNDERHELM_LAWACHURL.get(), ThunderhelmLawachurlEntity.createAttributes().build());
        event.put(HILICHURL_SHOOTER.get(), HilichurlEntity.createAttributes().build());
        event.put(HILICHURL_BERSERKER.get(), HilichurlEntity.createAttributes().build());
        event.put(GEOVISHAP.get(), GeovishapEntity.createAttributes().build());
        event.put(BATHYSMAL_VISHAP.get(), BathysmalVishapEntity.createAttributes().build());
        event.put(SPECTER.get(), SpecterEntity.createAttributes().build());
        event.put(WHOPPERFLOWER.get(), WhopperflowerEntity.createAttributes().build());
    }

    public static void registerSpawnPlacements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
        event.register(MOUNT_HUA_DISCIPLE.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), type),
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ISEKAI_SLIME.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(GOBLIN_SCOUT.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(GOBLIN_WARRIOR.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(GOBLIN_SHAMAN.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(BEASTMAN_WARRIOR.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(JIANGSHI.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(DEMONIC_CULTIVATOR.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(JADE_ARMORED_BEAST.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(SPIRIT_FOX.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ASTRAL_DRAGON.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ANCIENT_DRAGON.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AncientDragonEntity::checkDragonSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(SHADOW_DIREWOLF.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(WHITE_THUNDER_TIGER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(CLOUD_CRANE.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(MANA_COW.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntities::checkManaAnimalSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(MANA_PIG.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntities::checkManaAnimalSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(MANA_SHEEP.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntities::checkManaAnimalSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(MANA_CHICKEN.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntities::checkManaAnimalSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ANT_SOLDIER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ABYSSAL_BEHOLDER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(MANA_PIXIE.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), type),
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(SCREAMING_MANDRAKE.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), type),
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(BLOOD_SECT_DISCIPLE.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ABYSSAL_ORC.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(FROST_ELF_ASSASSIN.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ABYSSAL_NECROMANCER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(HILICHURL.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(MITACHURL.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(GENSHIN_SLIME.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos.below()).isSolid() && level.getFluidState(pos).isEmpty(),
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(GLARE.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBlockState(pos.below()).isSolid() && level.getFluidState(pos).isEmpty(),
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        // Genshin Impact Field Mobs
        event.register(HILICHURL_SHOOTER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(HILICHURL_BERSERKER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(FROSTARM_LAWACHURL.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(THUNDERHELM_LAWACHURL.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(RUIN_SENTINEL.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(RUIN_GRADER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(RUIN_DRAKE.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(GEOVISHAP.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(BATHYSMAL_VISHAP.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(SPECTER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(WHOPPERFLOWER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(WILDFIRE.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(YGGDRASIL_TREANT_BOSS.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(JADE_GUARDIAN_BOSS.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(ELEMENTAL_LORD.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(GREAT_SNOWBOAR_KING.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static boolean checkManaAnimalSpawnRules(
            net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.animal.Animal> type,
            net.minecraft.world.level.LevelAccessor level,
            net.minecraft.world.entity.MobSpawnType reason,
            net.minecraft.core.BlockPos pos,
            net.minecraft.util.RandomSource random) {
        if (reason == net.minecraft.world.entity.MobSpawnType.SPAWN_EGG
                || reason == net.minecraft.world.entity.MobSpawnType.COMMAND
                || reason == net.minecraft.world.entity.MobSpawnType.DISPENSER
                || reason == net.minecraft.world.entity.MobSpawnType.BREEDING) {
            return net.minecraft.world.entity.animal.Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
        }
        net.minecraft.server.level.ServerLevel serverLevel = null;
        if (level instanceof net.minecraft.world.level.ServerLevelAccessor sla) {
            serverLevel = sla.getLevel();
        }
        if (serverLevel == null || !serverLevel.dimension().equals(com.soybe.awakeningsystem.IsekaiWorldManager.ISEKAI_WORLD)) {
            return false;
        }
        return net.minecraft.world.entity.animal.Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
    }
}

