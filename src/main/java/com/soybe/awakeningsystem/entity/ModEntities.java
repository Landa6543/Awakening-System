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

    public static final Supplier<EntityType<SculkGolemEntity>> SCULK_GOLEM =
            ENTITY_TYPES.register("sculk_golem", () ->
                    EntityType.Builder.<SculkGolemEntity>of(SculkGolemEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 2.7F)
                            .eyeHeight(2.3F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("sculk_golem"));

    public static final Supplier<EntityType<ColossalSculkWardenEntity>> COLOSSAL_SCULK_WARDEN =
            ENTITY_TYPES.register("colossal_sculk_warden", () ->
                    EntityType.Builder.<ColossalSculkWardenEntity>of(ColossalSculkWardenEntity::new, MobCategory.MONSTER)
                            .sized(2.0F, 6.0F)
                            .eyeHeight(5.0F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .fireImmune()
                            .build("colossal_sculk_warden"));

    public static final Supplier<EntityType<MotherSpiderEntity>> MOTHER_SPIDER =
            ENTITY_TYPES.register("mother_spider", () ->
                    EntityType.Builder.<MotherSpiderEntity>of(MotherSpiderEntity::new, MobCategory.MONSTER)
                            .sized(1.8F, 1.2F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("mother_spider"));

    public static final Supplier<EntityType<MotherSpiderBossEntity>> MOTHER_SPIDER_BOSS =
            ENTITY_TYPES.register("mother_spider_boss", () ->
                    EntityType.Builder.<MotherSpiderBossEntity>of(MotherSpiderBossEntity::new, MobCategory.MONSTER)
                            .sized(4.5F, 4.0F)
                            .eyeHeight(3.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("mother_spider_boss"));

    public static final Supplier<EntityType<SpiderWebProjectileEntity>> SPIDER_WEB_PROJECTILE =
            ENTITY_TYPES.register("spider_web_projectile", () ->
                    EntityType.Builder.<SpiderWebProjectileEntity>of(SpiderWebProjectileEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("spider_web_projectile"));

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

    public static final Supplier<EntityType<AntSoldierEntity>> ANT_SOLDIER =
            ENTITY_TYPES.register("ant_soldier", () ->
                    EntityType.Builder.<AntSoldierEntity>of(AntSoldierEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 2.4F)
                            .eyeHeight(2.1F)
                            .clientTrackingRange(36)
                            .updateInterval(2)
                            .build("ant_soldier"));

    public static final Supplier<EntityType<LimboOphanimEntity>> LIMBO_OPHANIM =
            ENTITY_TYPES.register("limbo_ophanim", () ->
                    EntityType.Builder.<LimboOphanimEntity>of(LimboOphanimEntity::new, MobCategory.MONSTER)
                            .sized(3.6F, 3.6F)
                            .eyeHeight(1.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .fireImmune()
                            .build("limbo_ophanim"));

    public static final Supplier<EntityType<StiltWalkerEntity>> STILT_WALKER =
            ENTITY_TYPES.register("stilt_walker", () ->
                    EntityType.Builder.<StiltWalkerEntity>of(StiltWalkerEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 3.2F)
                            .eyeHeight(2.8F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("stilt_walker"));

    public static final Supplier<EntityType<SpiderHeadEntity>> SPIDER_HEAD =
            ENTITY_TYPES.register("spider_head", () ->
                    EntityType.Builder.<SpiderHeadEntity>of(SpiderHeadEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 1.0F)
                            .eyeHeight(0.8F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("spider_head"));

    public static final Supplier<EntityType<LimboEntity>> LIMBO_ENTITY =
            ENTITY_TYPES.register("limbo_entity", () ->
                    EntityType.Builder.<LimboEntity>of(LimboEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 3.2F)
                            .eyeHeight(2.8F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("limbo_entity"));

    public static final Supplier<EntityType<TallStalkerEntity>> TALL_STALKER =
            ENTITY_TYPES.register("tall_stalker", () ->
                    EntityType.Builder.<TallStalkerEntity>of(TallStalkerEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 3.0F)
                            .eyeHeight(2.6F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("tall_stalker"));

    public static final Supplier<EntityType<LimboDoorEntity>> LIMBO_DOOR =
            ENTITY_TYPES.register("limbo_door", () ->
                    EntityType.Builder.<LimboDoorEntity>of(LimboDoorEntity::new, MobCategory.MISC)
                            .sized(7.2F, 10.5F)
                            .clientTrackingRange(128)
                            .updateInterval(2)
                            .fireImmune()
                            .build("limbo_door"));

    public static final Supplier<EntityType<LimboDivineOrbProjectileEntity>> LIMBO_DIVINE_ORB =
            ENTITY_TYPES.register("limbo_divine_orb", () ->
                    EntityType.Builder.<LimboDivineOrbProjectileEntity>of(LimboDivineOrbProjectileEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("limbo_divine_orb"));

    public static final Supplier<EntityType<AbyssalBeholderEntity>> ABYSSAL_BEHOLDER =
            ENTITY_TYPES.register("abyssal_beholder", () ->
                    EntityType.Builder.<AbyssalBeholderEntity>of(AbyssalBeholderEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 2.4F)
                            .eyeHeight(1.2F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("abyssal_beholder"));

    public static final Supplier<EntityType<IsekaiSlimeEntity>> ISEKAI_SLIME =
            ENTITY_TYPES.register("isekai_slime", () ->
                    EntityType.Builder.<IsekaiSlimeEntity>of(IsekaiSlimeEntity::new, MobCategory.MONSTER)
                            .sized(0.85F, 0.95F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("isekai_slime"));

    public static final Supplier<EntityType<GenshinSlimeEntity>> GENSHIN_SLIME =
            ENTITY_TYPES.register("genshin_slime", () ->
                    EntityType.Builder.<GenshinSlimeEntity>of(GenshinSlimeEntity::new, MobCategory.MONSTER)
                            .sized(0.90F, 0.80F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("genshin_slime"));

    public static final Supplier<EntityType<HilichurlEntity>> HILICHURL =
            ENTITY_TYPES.register("hilichurl", () ->
                    EntityType.Builder.<HilichurlEntity>of(HilichurlEntity::new, MobCategory.MONSTER)
                            .sized(0.65F, 1.55F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("hilichurl"));

    public static final Supplier<EntityType<MitachurlEntity>> MITACHURL =
            ENTITY_TYPES.register("mitachurl", () ->
                    EntityType.Builder.<MitachurlEntity>of(MitachurlEntity::new, MobCategory.MONSTER)
                            .sized(1.85F, 3.7F)
                            .eyeHeight(3.0F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("mitachurl"));

    public static final Supplier<EntityType<SpiritFoxEntity>> SPIRIT_FOX =
            ENTITY_TYPES.register("spirit_fox", () ->
                    EntityType.Builder.<SpiritFoxEntity>of(SpiritFoxEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.85F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("spirit_fox"));

    public static final Supplier<EntityType<AstralDragonEntity>> ASTRAL_DRAGON =
            ENTITY_TYPES.register("astral_dragon", () ->
                    EntityType.Builder.<AstralDragonEntity>of(AstralDragonEntity::new, MobCategory.CREATURE)
                            .sized(0.65F, 0.6F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("astral_dragon"));

    public static final Supplier<EntityType<ShadowDirewolfEntity>> SHADOW_DIREWOLF =
            ENTITY_TYPES.register("shadow_direwolf", () ->
                    EntityType.Builder.<ShadowDirewolfEntity>of(ShadowDirewolfEntity::new, MobCategory.CREATURE)
                            .sized(1.5F, 1.6F)
                            .eyeHeight(1.2F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("shadow_direwolf"));

    public static final Supplier<EntityType<WhiteThunderTigerEntity>> WHITE_THUNDER_TIGER =
            ENTITY_TYPES.register("white_thunder_tiger", () ->
                    EntityType.Builder.<WhiteThunderTigerEntity>of(WhiteThunderTigerEntity::new, MobCategory.CREATURE)
                            .sized(1.7F, 1.7F)
                            .eyeHeight(1.3F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("white_thunder_tiger"));

    public static final Supplier<EntityType<CloudCraneEntity>> CLOUD_CRANE =
            ENTITY_TYPES.register("cloud_crane", () ->
                    EntityType.Builder.<CloudCraneEntity>of(CloudCraneEntity::new, MobCategory.CREATURE)
                            .sized(1.0F, 2.2F)
                            .eyeHeight(1.8F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("cloud_crane"));

    public static final Supplier<EntityType<ManaPixieEntity>> MANA_PIXIE =
            ENTITY_TYPES.register("mana_pixie", () ->
                    EntityType.Builder.<ManaPixieEntity>of(ManaPixieEntity::new, MobCategory.CREATURE)
                            .sized(0.45F, 0.55F)
                            .eyeHeight(0.35F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("mana_pixie"));

    public static final Supplier<EntityType<ScreamingMandrakeEntity>> SCREAMING_MANDRAKE =
            ENTITY_TYPES.register("screaming_mandrake", () ->
                    EntityType.Builder.<ScreamingMandrakeEntity>of(ScreamingMandrakeEntity::new, MobCategory.CREATURE)
                            .sized(0.55F, 0.65F)
                            .eyeHeight(0.40F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("screaming_mandrake"));

    public static final Supplier<EntityType<CelestialDragonEntity>> CELESTIAL_DRAGON =
            ENTITY_TYPES.register("celestial_dragon", () ->
                    EntityType.Builder.<CelestialDragonEntity>of(CelestialDragonEntity::new, MobCategory.MONSTER)
                            .sized(3.6F, 4.2F)
                            .eyeHeight(3.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("celestial_dragon"));

    public static final Supplier<EntityType<AncientCelestialDragonBossEntity>> ANCIENT_CELESTIAL_DRAGON =
            ENTITY_TYPES.register("ancient_celestial_dragon", () ->
                    EntityType.Builder.<AncientCelestialDragonBossEntity>of(AncientCelestialDragonBossEntity::new, MobCategory.MONSTER)
                            .sized(5.5F, 5.5F)
                            .eyeHeight(5.0F)
                            .clientTrackingRange(128)
                            .updateInterval(2)
                            .build("ancient_celestial_dragon"));

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

    public static final Supplier<EntityType<BeastmanWarriorEntity>> BEASTMAN_WARRIOR =
            ENTITY_TYPES.register("beastman_warrior", () ->
                    EntityType.Builder.<BeastmanWarriorEntity>of(BeastmanWarriorEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 2.2F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("beastman_warrior"));

    public static final Supplier<EntityType<JiangshiEntity>> JIANGSHI =
            ENTITY_TYPES.register("jiangshi", () ->
                    EntityType.Builder.<JiangshiEntity>of(JiangshiEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(36)
                            .updateInterval(2)
                            .build("jiangshi"));

    public static final Supplier<EntityType<DemonicCultivatorEntity>> DEMONIC_CULTIVATOR =
            ENTITY_TYPES.register("demonic_cultivator", () ->
                    EntityType.Builder.<DemonicCultivatorEntity>of(DemonicCultivatorEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(40)
                            .updateInterval(2)
                            .build("demonic_cultivator"));

    public static final Supplier<EntityType<DemonicSwordQiEntity>> DEMONIC_SWORD_QI =
            ENTITY_TYPES.register("demonic_sword_qi", () ->
                    EntityType.Builder.<DemonicSwordQiEntity>of(DemonicSwordQiEntity::new, MobCategory.MISC)
                            .sized(0.6F, 0.6F)
                            .clientTrackingRange(48)
                            .updateInterval(1)
                            .build("demonic_sword_qi"));

    public static final Supplier<EntityType<JadeFeijianEntity>> JADE_FEIJIAN =
            ENTITY_TYPES.register("jade_feijian", () ->
                    EntityType.Builder.<JadeFeijianEntity>of(JadeFeijianEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("jade_feijian"));

    public static final Supplier<EntityType<AstralDragonCrescentEntity>> ASTRAL_DRAGON_CRESCENT =
            ENTITY_TYPES.register("astral_dragon_crescent", () ->
                    EntityType.Builder.<AstralDragonCrescentEntity>of(AstralDragonCrescentEntity::new, MobCategory.MISC)
                            .sized(1.2F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("astral_dragon_crescent"));

    public static final Supplier<EntityType<SlashWaveEntity>> SLASH_WAVE =
            ENTITY_TYPES.register("slash_wave", () ->
                    EntityType.Builder.<SlashWaveEntity>of(SlashWaveEntity::new, MobCategory.MISC)
                            .sized(2.4F, 0.6F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("slash_wave"));

    public static final Supplier<EntityType<JadeArmoredBeastEntity>> JADE_ARMORED_BEAST =
            ENTITY_TYPES.register("jade_armored_beast", () ->
                    EntityType.Builder.<JadeArmoredBeastEntity>of(JadeArmoredBeastEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 1.0F)
                            .clientTrackingRange(40)
                            .updateInterval(2)
                            .build("jade_armored_beast"));

    public static final Supplier<EntityType<MountHuaDiscipleEntity>> MOUNT_HUA_DISCIPLE =
            ENTITY_TYPES.register("mount_hua_disciple", () ->
                    EntityType.Builder.<MountHuaDiscipleEntity>of(MountHuaDiscipleEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(40)
                            .updateInterval(2)
                            .build("mount_hua_disciple"));

    public static final Supplier<EntityType<CelestialDragonBreathEntity>> CELESTIAL_DRAGON_BREATH =
            ENTITY_TYPES.register("celestial_dragon_breath", () ->
                    EntityType.Builder.<CelestialDragonBreathEntity>of(CelestialDragonBreathEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(48)
                            .updateInterval(1)
                            .build("celestial_dragon_breath"));

    public static final Supplier<EntityType<AstralBreathProjectileEntity>> ASTRAL_BREATH =
            ENTITY_TYPES.register("astral_breath", () ->
                    EntityType.Builder.<AstralBreathProjectileEntity>of(AstralBreathProjectileEntity::new, MobCategory.MISC)
                            .sized(0.35F, 0.35F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build("astral_breath"));

    public static final Supplier<EntityType<ShadowSoulEntity>> SHADOW_SOUL =
            ENTITY_TYPES.register("shadow_soul", () ->
                    EntityType.Builder.<ShadowSoulEntity>of(ShadowSoulEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(32)
                            .updateInterval(3)
                            .build("shadow_soul"));

    public static final Supplier<EntityType<MeditationSeatEntity>> MEDITATION_SEAT =
            ENTITY_TYPES.register("meditation_seat", () ->
                    EntityType.Builder.<MeditationSeatEntity>of(MeditationSeatEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.2F)
                            .noSave()
                            .build("meditation_seat"));

    public static final Supplier<EntityType<TavernSeatEntity>> TAVERN_SEAT =
            ENTITY_TYPES.register("tavern_seat", () ->
                    EntityType.Builder.<TavernSeatEntity>of(TavernSeatEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.2F)
                            .noSave()
                            .build("tavern_seat"));

    public static final Supplier<EntityType<SubwayPilotSeatEntity>> SUBWAY_PILOT_SEAT =
            ENTITY_TYPES.register("subway_pilot_seat", () ->
                    EntityType.Builder.<SubwayPilotSeatEntity>of(SubwayPilotSeatEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.2F)
                            .noSave()
                            .build("subway_pilot_seat"));

    public static final Supplier<EntityType<FlyingSwordEntity>> FLYING_SWORD =
            ENTITY_TYPES.register("flying_sword", () ->
                    EntityType.Builder.<FlyingSwordEntity>of(FlyingSwordEntity::new, MobCategory.MISC)
                            .sized(1.2F, 0.4F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("flying_sword"));

    public static final Supplier<EntityType<SubwayTrainCarriageEntity>> SUBWAY_TRAIN_CARRIAGE =
            ENTITY_TYPES.register("subway_train_carriage", () ->
                    EntityType.Builder.<SubwayTrainCarriageEntity>of(SubwayTrainCarriageEntity::new, MobCategory.MISC)
                            .sized(3.2F, 2.6F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("subway_train_carriage"));

    public static final Supplier<EntityType<AwakeningCoinEntity>> AWAKENING_COIN_PROJECTILE =
            ENTITY_TYPES.register("awakening_coin_projectile", () ->
                    EntityType.Builder.<AwakeningCoinEntity>of(AwakeningCoinEntity::new, MobCategory.MISC)
                            .sized(0.35F, 0.35F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build("awakening_coin_projectile"));

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

    public static final Supplier<EntityType<SeriesNpcEntity>> BLACK_MARKET_DEALER =
            ENTITY_TYPES.register("black_market_dealer", () ->
                    EntityType.Builder.<SeriesNpcEntity>of((type, level) -> new SeriesNpcEntity(type, level, NpcProfile.BLACK_MARKET_DEALER), MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("black_market_dealer"));

    public static final Supplier<EntityType<DokkaebiEntity>> DOKKAEBI =
            ENTITY_TYPES.register("dokkaebi", () ->
                    EntityType.Builder.<DokkaebiEntity>of(DokkaebiEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.9F)
                            .clientTrackingRange(32)
                            .updateInterval(2)
                            .build("dokkaebi"));

    public static final Supplier<EntityType<PaimonEntity>> PAIMON =
            ENTITY_TYPES.register("paimon", () ->
                    EntityType.Builder.<PaimonEntity>of(PaimonEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.85F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("paimon"));

    public static final Supplier<EntityType<CompanionEntity>> COMPANION =
            ENTITY_TYPES.register("companion", () ->
                    EntityType.Builder.<CompanionEntity>of(CompanionEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("companion"));

    public static final Supplier<EntityType<WyvernEntity>> WYVERN =
            ENTITY_TYPES.register("wyvern", () ->
                    EntityType.Builder.<WyvernEntity>of(WyvernEntity::new, MobCategory.CREATURE)
                            .sized(2.2F, 2.0F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("wyvern"));

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

    public static final Supplier<EntityType<StatueOfGodEntity>> STATUE_OF_GOD =
            ENTITY_TYPES.register("statue_of_god", () ->
                    EntityType.Builder.<StatueOfGodEntity>of(StatueOfGodEntity::new, MobCategory.MONSTER)
                            .sized(4.0F, 10.0F)
                            .eyeHeight(9.5F)
                            .clientTrackingRange(96)
                            .updateInterval(2)
                            .fireImmune()
                            .build("statue_of_god"));

    public static final Supplier<EntityType<BeruBossEntity>> BERU_BOSS =
            ENTITY_TYPES.register("beru_boss", () ->
                    EntityType.Builder.<BeruBossEntity>of(BeruBossEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 2.8F)
                            .eyeHeight(2.4F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("beru_boss"));


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

    public static final Supplier<EntityType<AncientDragonEntity>> ANCIENT_DRAGON =
            ENTITY_TYPES.register("ancient_dragon", () ->
                    EntityType.Builder.<AncientDragonEntity>of(AncientDragonEntity::new, MobCategory.CREATURE)
                            .sized(6.0F, 5.0F)
                            .eyeHeight(4.8F)
                            .clientTrackingRange(128)
                            .updateInterval(2)
                            .build("ancient_dragon"));

    public static final Supplier<EntityType<AncientDragonBreathEntity>> ANCIENT_DRAGON_BREATH =
            ENTITY_TYPES.register("ancient_dragon_breath", () ->
                    EntityType.Builder.<AncientDragonBreathEntity>of(AncientDragonBreathEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("ancient_dragon_breath"));

    public static final Supplier<EntityType<LeyLineMonolithEntity>> LEY_LINE_MONOLITH =
            ENTITY_TYPES.register("ley_line_monolith", () ->
                    EntityType.Builder.<LeyLineMonolithEntity>of(LeyLineMonolithEntity::new, MobCategory.MISC)
                            .sized(1.6F, 3.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("ley_line_monolith"));

    public static final Supplier<EntityType<AbyssChallengeKeyEntity>> ABYSS_CHALLENGE_KEY =
            ENTITY_TYPES.register("abyss_challenge_key", () ->
                    EntityType.Builder.<AbyssChallengeKeyEntity>of(AbyssChallengeKeyEntity::new, MobCategory.MISC)
                            .sized(1.0F, 2.0F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("abyss_challenge_key"));

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

    public static final Supplier<EntityType<WildfireEntity>> WILDFIRE =
            ENTITY_TYPES.register("wildfire", () ->
                    EntityType.Builder.<WildfireEntity>of(WildfireEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 2.6F)
                            .eyeHeight(2.2F)
                            .fireImmune()
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("wildfire"));

    public static final Supplier<EntityType<GlareEntity>> GLARE =
            ENTITY_TYPES.register("glare", () ->
                    EntityType.Builder.<GlareEntity>of(GlareEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.9F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("glare"));

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

    public static final Supplier<EntityType<StonehideLawachurlEntity>> STONEHIDE_LAWACHURL =
            ENTITY_TYPES.register("stonehide_lawachurl", () ->
                    EntityType.Builder.<StonehideLawachurlEntity>of(StonehideLawachurlEntity::new, MobCategory.MONSTER)
                            .sized(3.6F, 5.0F)
                            .eyeHeight(4.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("stonehide_lawachurl"));

    public static final Supplier<EntityType<CryoRegisvineEntity>> CRYO_REGISVINE =
            ENTITY_TYPES.register("cryo_regisvine", () ->
                    EntityType.Builder.<CryoRegisvineEntity>of(CryoRegisvineEntity::new, MobCategory.MONSTER)
                            .sized(4.8F, 8.2F)
                            .eyeHeight(6.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("cryo_regisvine"));

    public static final Supplier<EntityType<PyroRegisvineEntity>> PYRO_REGISVINE =
            ENTITY_TYPES.register("pyro_regisvine", () ->
                    EntityType.Builder.<PyroRegisvineEntity>of(PyroRegisvineEntity::new, MobCategory.MONSTER)
                            .sized(4.8F, 8.2F)
                            .eyeHeight(6.2F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("pyro_regisvine"));

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

    public static final Supplier<EntityType<com.soybe.awakeningsystem.uma.UmaRacerEntity>> UMA_RACER =
            ENTITY_TYPES.register("uma_racer", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.uma.UmaRacerEntity>of(com.soybe.awakeningsystem.uma.UmaRacerEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.85F)
                            .clientTrackingRange(48)
                            .updateInterval(2)
                            .build("uma_racer"));

    public static final Supplier<EntityType<com.soybe.awakeningsystem.casino.darts.DartEntity>> DART =
            ENTITY_TYPES.register("dart", () ->
                    EntityType.Builder.<com.soybe.awakeningsystem.casino.darts.DartEntity>of(com.soybe.awakeningsystem.casino.darts.DartEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("dart"));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(LIMBO_OPHANIM.get(), LimboOphanimEntity.createAttributes().build());
        event.put(STILT_WALKER.get(), StiltWalkerEntity.createAttributes().build());
        event.put(SPIDER_HEAD.get(), SpiderHeadEntity.createAttributes().build());
        event.put(TALL_STALKER.get(), TallStalkerEntity.createAttributes().build());
        event.put(LIMBO_ENTITY.get(), LimboEntity.createAttributes().build());
        event.put(UMA_RACER.get(), com.soybe.awakeningsystem.uma.UmaRacerEntity.createAttributes().build());
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
        event.put(SCULK_GOLEM.get(), SculkGolemEntity.createAttributes().build());
        event.put(COLOSSAL_SCULK_WARDEN.get(), ColossalSculkWardenEntity.createAttributes().build());
        event.put(MOTHER_SPIDER.get(), MotherSpiderEntity.createAttributes().build());
        event.put(MOTHER_SPIDER_BOSS.get(), MotherSpiderBossEntity.createBossAttributes().build());
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
        event.put(BLACK_MARKET_DEALER.get(), npcAttrs);
        event.put(DOKKAEBI.get(), DokkaebiEntity.createAttributes().build());
        event.put(PAIMON.get(), PaimonEntity.createAttributes().build());
        event.put(COMPANION.get(), CompanionEntity.createAttributes().build());
        event.put(WYVERN.get(), WyvernEntity.createAttributes().build());
        event.put(IGRIS_BOSS.get(), IgrisBossEntity.createAttributes().build());
        event.put(BARAN_BOSS.get(), BaranBossEntity.createAttributes().build());
        event.put(STATUE_OF_GOD.get(), StatueOfGodEntity.createAttributes().build());
        event.put(BERU_BOSS.get(), BeruBossEntity.createAttributes().build());
        event.put(BLOOD_QI_TOTEM.get(), BloodQiTotemEntity.createAttributes().build());
        event.put(BLOOD_SECT_DISCIPLE.get(), BloodSectDiscipleEntity.createAttributes().build());
        event.put(BLOOD_PATRIARCH_BOSS.get(), BloodPatriarchBossEntity.createAttributes().build());
        event.put(YGGDRASIL_TREANT_BOSS.get(), YggdrasilTreantBossEntity.createAttributes().build());
        event.put(JADE_GUARDIAN_BOSS.get(), JadeGuardianBossEntity.createAttributes().build());
        event.put(ELEMENTAL_DEMON_KING.get(), ElementalDemonKingBossEntity.createAttributes().build());
        event.put(ELEMENTAL_LORD.get(), ElementalLordBossEntity.createAttributes().build());

        event.put(GOBLIN_SCOUT.get(), GoblinScoutEntity.createAttributes().build());
        event.put(GOBLIN_WARRIOR.get(), GoblinWarriorEntity.createAttributes().build());
        event.put(GOBLIN_SHAMAN.get(), GoblinShamanEntity.createAttributes().build());
        event.put(BEASTMAN_WARRIOR.get(), BeastmanWarriorEntity.createAttributes().build());
        event.put(JIANGSHI.get(), JiangshiEntity.createAttributes().build());
        event.put(DEMONIC_CULTIVATOR.get(), DemonicCultivatorEntity.createAttributes().build());
        event.put(JADE_ARMORED_BEAST.get(), JadeArmoredBeastEntity.createAttributes().build());
        event.put(MOUNT_HUA_DISCIPLE.get(), MountHuaDiscipleEntity.createAttributes().build());

        event.put(GENSHIN_SLIME.get(), GenshinSlimeEntity.createAttributes().build());
        event.put(HILICHURL.get(), HilichurlEntity.createAttributes().build());
        event.put(MITACHURL.get(), MitachurlEntity.createAttributes().build());

        event.put(LEY_LINE_MONOLITH.get(), LeyLineMonolithEntity.createAttributes().build());
        event.put(ABYSS_CHALLENGE_KEY.get(), AbyssChallengeKeyEntity.createAttributes().build());

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

    public static <T extends net.minecraft.world.entity.monster.Monster> boolean checkLimboStalkerSpawnRules(
            net.minecraft.world.entity.EntityType<T> type,
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.entity.MobSpawnType reason,
            net.minecraft.core.BlockPos pos,
            net.minecraft.util.RandomSource random) {
        // 1. Zona de exclusión del santuario de spawn (mínimo 160 bloques del origen)
        if (pos.closerThan(new net.minecraft.core.Vec3i(0, 2, 0), 160.0D)) {
            return false;
        }

        // 1.1 Zona de exclusión sagrada de las 4 Puertas Monumentales del Limbo (mínimo 96 bloques de distancia)
        for (net.minecraft.core.BlockPos doorPos : com.soybe.awakeningsystem.limbo.LimboWorldManager.CARDINAL_DOORS) {
            if (pos.closerThan(doorPos, 96.0D)) {
                return false;
            }
        }

        // 2. Probabilidad muy baja por ciclo de spawn natural para evitar acumulación
        if (random.nextFloat() > 0.04F) {
            return false;
        }

        // 3. Separación estricta y tope poblacional en el área cargada
        net.minecraft.world.level.Level l = level.getLevel();
        if (l instanceof net.minecraft.server.level.ServerLevel sl) {
            // Anti-amontonamiento: ninguna criatura acechante puede spawnear a menos de 48 bloques de otra
            net.minecraft.world.phys.AABB densityBox = new net.minecraft.world.phys.AABB(pos).inflate(48.0D);
            if (!sl.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, densityBox, e -> e instanceof LimboStalkerCreature).isEmpty()) {
                return false;
            }

            // Límite de presencia atmosférica: máximo 6 acechadores en 160 bloques a la redonda
            net.minecraft.world.phys.AABB areaBox = new net.minecraft.world.phys.AABB(pos).inflate(160.0D);
            if (sl.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, areaBox, e -> e instanceof LimboStalkerCreature).size() >= 6) {
                return false;
            }
        }

        return net.minecraft.world.entity.monster.Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random);
    }

    public static void registerSpawnPlacements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
        event.register(STILT_WALKER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntities::checkLimboStalkerSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(SPIDER_HEAD.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntities::checkLimboStalkerSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(LIMBO_ENTITY.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntities::checkLimboStalkerSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TALL_STALKER.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntities::checkLimboStalkerSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
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

        event.register(SCULK_GOLEM.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(COLOSSAL_SCULK_WARDEN.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, rand) -> true,
                net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);

        event.register(MOTHER_SPIDER.get(),
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

