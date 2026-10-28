package com.soybe.awakeningsystem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, AwakeningSystemMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> DART_ANNOUNCER_GAMEON =
            SOUND_EVENTS.register("dart_announcer_gameon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dart_announcer_gameon")));

    public static final DeferredHolder<SoundEvent, SoundEvent> DART_ANNOUNCER_180 =
            SOUND_EVENTS.register("dart_announcer_180",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dart_announcer_180")));

    public static final DeferredHolder<SoundEvent, SoundEvent> DART_ANNOUNCER_BULLSEYE =
            SOUND_EVENTS.register("dart_announcer_bullseye",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dart_announcer_bullseye")));

    public static final DeferredHolder<SoundEvent, SoundEvent> DART_ANNOUNCER_TRIPLE =
            SOUND_EVENTS.register("dart_announcer_triple",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dart_announcer_triple")));

    public static final DeferredHolder<SoundEvent, SoundEvent> UMA_START_CHEER =
            SOUND_EVENTS.register("uma_start_cheer",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "uma_start_cheer")));

    public static final DeferredHolder<SoundEvent, SoundEvent> UMA_SKILL_BURST =
            SOUND_EVENTS.register("uma_skill_burst",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "uma_skill_burst")));

    public static final DeferredHolder<SoundEvent, SoundEvent> UMA_VICTORY_CHEER =
            SOUND_EVENTS.register("uma_victory_cheer",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "uma_victory_cheer")));

    public static final DeferredHolder<SoundEvent, SoundEvent> NPC_INTERACT =
            SOUND_EVENTS.register("npc_interact",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "npc_interact")));

    public static final DeferredHolder<SoundEvent, SoundEvent> NPC_BLESSING =
            SOUND_EVENTS.register("npc_blessing",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "npc_blessing")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MOTHER_SPIDER_AMBIENT =
            SOUND_EVENTS.register("mother_spider_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mother_spider_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTHER_SPIDER_BOSS_AMBIENT =
            SOUND_EVENTS.register("mother_spider_boss_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mother_spider_boss_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTHER_SPIDER_ATTACK =
            SOUND_EVENTS.register("mother_spider_attack",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mother_spider_attack")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTHER_SPIDER_SHOOT_WEB =
            SOUND_EVENTS.register("mother_spider_shoot_web",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mother_spider_shoot_web")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTHER_SPIDER_HURT =
            SOUND_EVENTS.register("mother_spider_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mother_spider_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTHER_SPIDER_DEATH =
            SOUND_EVENTS.register("mother_spider_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mother_spider_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_SOLDIER_AMBIENT =
            SOUND_EVENTS.register("ant_soldier_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ant_soldier_ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_SOLDIER_HURT =
            SOUND_EVENTS.register("ant_soldier_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ant_soldier_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_SOLDIER_DEATH =
            SOUND_EVENTS.register("ant_soldier_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ant_soldier_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_SOLDIER_STEP =
            SOUND_EVENTS.register("ant_soldier_step",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ant_soldier_step")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_SOLDIER_SCREECH =
            SOUND_EVENTS.register("ant_soldier_screech",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ant_soldier_screech")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_BEHOLDER_AMBIENT =
            SOUND_EVENTS.register("abyssal_beholder_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_beholder_ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_BEHOLDER_HURT =
            SOUND_EVENTS.register("abyssal_beholder_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_beholder_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_BEHOLDER_DEATH =
            SOUND_EVENTS.register("abyssal_beholder_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_beholder_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_BEHOLDER_BEAM =
            SOUND_EVENTS.register("abyssal_beholder_beam",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_beholder_beam")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_BEHOLDER_ROAR =
            SOUND_EVENTS.register("abyssal_beholder_roar",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_beholder_roar")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MANA_PIXIE_AMBIENT =
            SOUND_EVENTS.register("mana_pixie_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mana_pixie_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MANA_PIXIE_HURT =
            SOUND_EVENTS.register("mana_pixie_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mana_pixie_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MANA_PIXIE_DEATH =
            SOUND_EVENTS.register("mana_pixie_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mana_pixie_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MANA_PIXIE_BLESS =
            SOUND_EVENTS.register("mana_pixie_bless",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mana_pixie_bless")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MANDRAKE_AMBIENT =
            SOUND_EVENTS.register("mandrake_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mandrake_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MANDRAKE_SCREAM =
            SOUND_EVENTS.register("mandrake_scream",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mandrake_scream")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MANDRAKE_HURT =
            SOUND_EVENTS.register("mandrake_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mandrake_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MANDRAKE_DEATH =
            SOUND_EVENTS.register("mandrake_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mandrake_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CELESTIAL_DRAGON_AMBIENT =
            SOUND_EVENTS.register("celestial_dragon_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "celestial_dragon_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CELESTIAL_DRAGON_ROAR =
            SOUND_EVENTS.register("celestial_dragon_roar",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "celestial_dragon_roar")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CELESTIAL_DRAGON_BREATH =
            SOUND_EVENTS.register("celestial_dragon_breath",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "celestial_dragon_breath")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CELESTIAL_DRAGON_HURT =
            SOUND_EVENTS.register("celestial_dragon_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "celestial_dragon_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CELESTIAL_DRAGON_DEATH =
            SOUND_EVENTS.register("celestial_dragon_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "celestial_dragon_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> GOBLIN_AMBIENT =
            SOUND_EVENTS.register("goblin_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "goblin_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GOBLIN_HURT =
            SOUND_EVENTS.register("goblin_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "goblin_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GOBLIN_DEATH =
            SOUND_EVENTS.register("goblin_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "goblin_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GOBLIN_CHANT =
            SOUND_EVENTS.register("goblin_chant",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "goblin_chant")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CULTIVATOR_AMBIENT =
            SOUND_EVENTS.register("cultivator_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "cultivator_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CULTIVATOR_SLASH =
            SOUND_EVENTS.register("cultivator_slash",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "cultivator_slash")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CULTIVATOR_DASH =
            SOUND_EVENTS.register("cultivator_dash",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "cultivator_dash")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CULTIVATOR_HURT =
            SOUND_EVENTS.register("cultivator_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "cultivator_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CULTIVATOR_DEATH =
            SOUND_EVENTS.register("cultivator_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "cultivator_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> JADE_BEAST_AMBIENT =
            SOUND_EVENTS.register("jade_beast_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "jade_beast_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> JADE_BEAST_ROAR =
            SOUND_EVENTS.register("jade_beast_roar",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "jade_beast_roar")));
    public static final DeferredHolder<SoundEvent, SoundEvent> JADE_BEAST_CHARGE =
            SOUND_EVENTS.register("jade_beast_charge",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "jade_beast_charge")));
    public static final DeferredHolder<SoundEvent, SoundEvent> JADE_BEAST_HURT =
            SOUND_EVENTS.register("jade_beast_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "jade_beast_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> JADE_BEAST_DEATH =
            SOUND_EVENTS.register("jade_beast_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "jade_beast_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WHITE_TIGER_AMBIENT =
            SOUND_EVENTS.register("white_tiger_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "white_tiger_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WHITE_TIGER_ROAR =
            SOUND_EVENTS.register("white_tiger_roar",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "white_tiger_roar")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WHITE_TIGER_HURT =
            SOUND_EVENTS.register("white_tiger_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "white_tiger_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WHITE_TIGER_DEATH =
            SOUND_EVENTS.register("white_tiger_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "white_tiger_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_WOLF_AMBIENT =
            SOUND_EVENTS.register("shadow_wolf_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shadow_wolf_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_WOLF_HOWL =
            SOUND_EVENTS.register("shadow_wolf_howl",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shadow_wolf_howl")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_WOLF_BITE =
            SOUND_EVENTS.register("shadow_wolf_bite",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shadow_wolf_bite")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_WOLF_HURT =
            SOUND_EVENTS.register("shadow_wolf_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shadow_wolf_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_WOLF_DEATH =
            SOUND_EVENTS.register("shadow_wolf_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shadow_wolf_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BEASTMAN_AMBIENT =
            SOUND_EVENTS.register("beastman_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "beastman_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BEASTMAN_ATTACK =
            SOUND_EVENTS.register("beastman_attack",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "beastman_attack")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BEASTMAN_HURT =
            SOUND_EVENTS.register("beastman_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "beastman_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BEASTMAN_DEATH =
            SOUND_EVENTS.register("beastman_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "beastman_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> GOBLIN_SCOUT_SCREECH =
            SOUND_EVENTS.register("goblin_scout_screech",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "goblin_scout_screech")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GOBLIN_WARRIOR_GRUNT =
            SOUND_EVENTS.register("goblin_warrior_grunt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "goblin_warrior_grunt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_OPEN =
            SOUND_EVENTS.register("system_open",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_open")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_CLOSE =
            SOUND_EVENTS.register("system_close",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_close")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_CLICK =
            SOUND_EVENTS.register("system_click",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_click")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_STAT_UP =
            SOUND_EVENTS.register("system_stat_up",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_stat_up")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_SKILL_UNLOCK =
            SOUND_EVENTS.register("system_skill_unlock",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_skill_unlock")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_ERROR =
            SOUND_EVENTS.register("system_error",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_error")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_LEVEL_UP =
            SOUND_EVENTS.register("system_level_up",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_level_up")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_AWAKENING =
            SOUND_EVENTS.register("system_awakening",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_awakening")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_RANK_UP =
            SOUND_EVENTS.register("system_rank_up",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_rank_up")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_QUEST_ALERT =
            SOUND_EVENTS.register("system_quest_alert",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_quest_alert")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_QUEST_COMPLETE =
            SOUND_EVENTS.register("system_quest_complete",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_quest_complete")));

    public static final DeferredHolder<SoundEvent, SoundEvent> DANTIAN_BREAKTHROUGH =
            SOUND_EVENTS.register("dantian_breakthrough",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dantian_breakthrough")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CELESTIAL_TRIBULATION =
            SOUND_EVENTS.register("celestial_tribulation",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "celestial_tribulation")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MARTIAL_SKILL_ACTIVATE =
            SOUND_EVENTS.register("martial_skill_activate",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "martial_skill_activate")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TALISMAN_SEAL =
            SOUND_EVENTS.register("talisman_seal",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "talisman_seal")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_EXTRACT =
            SOUND_EVENTS.register("shadow_extract",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shadow_extract")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ARISE_VOICE =
            SOUND_EVENTS.register("arise_voice",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "arise_voice")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ROULETTE_SPIN =
            SOUND_EVENTS.register("roulette_spin",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "roulette_spin")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROULETTE_WIN =
            SOUND_EVENTS.register("roulette_win",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "roulette_win")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SYSTEM_COINS =
            SOUND_EVENTS.register("system_coins",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "system_coins")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DUNGEON_GATE_OPEN =
            SOUND_EVENTS.register("dungeon_gate_open",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dungeon_gate_open")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GATE_AMBIENT =
            SOUND_EVENTS.register("gate_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gate_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GATE_TRAVEL =
            SOUND_EVENTS.register("gate_travel",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gate_travel")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DUNGEON_BREAK =
            SOUND_EVENTS.register("dungeon_break",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dungeon_break")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ANCIENT_DRAGON_AMBIENT =
            SOUND_EVENTS.register("ancient_dragon_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ancient_dragon_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANCIENT_DRAGON_ROAR =
            SOUND_EVENTS.register("ancient_dragon_roar",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ancient_dragon_roar")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANCIENT_DRAGON_BREATH =
            SOUND_EVENTS.register("ancient_dragon_breath",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ancient_dragon_breath")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANCIENT_DRAGON_FLAP =
            SOUND_EVENTS.register("ancient_dragon_flap",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ancient_dragon_flap")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANCIENT_DRAGON_HURT =
            SOUND_EVENTS.register("ancient_dragon_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ancient_dragon_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANCIENT_DRAGON_DEATH =
            SOUND_EVENTS.register("ancient_dragon_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ancient_dragon_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> PAIMON_SPAWN =
            SOUND_EVENTS.register("paimon_spawn",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "paimon_spawn")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PAIMON_TALK =
            SOUND_EVENTS.register("paimon_talk",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "paimon_talk")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PAIMON_THANK =
            SOUND_EVENTS.register("paimon_thank",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "paimon_thank")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PAIMON_TP =
            SOUND_EVENTS.register("paimon_tp",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "paimon_tp")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PAIMON_VANISH =
            SOUND_EVENTS.register("paimon_vanish",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "paimon_vanish")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WYVERN_AMBIENT =
            SOUND_EVENTS.register("wyvern_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wyvern_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WYVERN_ROAR =
            SOUND_EVENTS.register("wyvern_roar",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wyvern_roar")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WYVERN_BREATH =
            SOUND_EVENTS.register("wyvern_breath",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wyvern_breath")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WYVERN_HURT =
            SOUND_EVENTS.register("wyvern_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wyvern_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WYVERN_DEATH =
            SOUND_EVENTS.register("wyvern_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wyvern_death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HERTA_SWING =
            SOUND_EVENTS.register("herta_swing",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "herta_swing")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HERTA_HIT =
            SOUND_EVENTS.register("herta_hit",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "herta_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HERTA_KURU =
            SOUND_EVENTS.register("herta_kuru",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "herta_kuru")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HERTA_KURURIN =
            SOUND_EVENTS.register("herta_kururin",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "herta_kururin")));

    public static final DeferredHolder<SoundEvent, SoundEvent> DOMAIN_OF_SANCTION_SLASH =
            SOUND_EVENTS.register("domain_of_sanction_slash",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "domain_of_sanction_slash")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_METEOR_5STAR =
            SOUND_EVENTS.register("wish_meteor_5star",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wish_meteor_5star")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_METEOR_4STAR =
            SOUND_EVENTS.register("wish_meteor_4star",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wish_meteor_4star")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_METEOR_3STAR =
            SOUND_EVENTS.register("wish_meteor_3star",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wish_meteor_3star")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_REVEAL_5STAR =
            SOUND_EVENTS.register("wish_reveal_5star",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wish_reveal_5star")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_REVEAL_4STAR =
            SOUND_EVENTS.register("wish_reveal_4star",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wish_reveal_4star")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_REVEAL_3STAR =
            SOUND_EVENTS.register("wish_reveal_3star",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wish_reveal_3star")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_RESULT_LIST =
            SOUND_EVENTS.register("wish_result_list",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wish_result_list")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WISH_AMBIENT =
            SOUND_EVENTS.register("wish_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wish_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GENSHIN_LEVELUP =
            SOUND_EVENTS.register("genshin_levelup",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "genshin_levelup")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GENSHIN_CHEST_OPEN =
            SOUND_EVENTS.register("genshin_chest_open",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "genshin_chest_open")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WELKIN_OPEN =
            SOUND_EVENTS.register("welkin_open",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "welkin_open")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WELKIN_CLAIM =
            SOUND_EVENTS.register("welkin_claim",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "welkin_claim")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WELKIN_BGM =
            SOUND_EVENTS.register("welkin_bgm",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "welkin_bgm")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_TAKE_THE_JOURNEY =
            SOUND_EVENTS.register("music_disc_take_the_journey",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "music_disc_take_the_journey")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SPACE_WALK =
            SOUND_EVENTS.register("music_disc_space_walk",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "music_disc_space_walk")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_QINGCE_NIGHT =
            SOUND_EVENTS.register("music_disc_qingce_night",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "music_disc_qingce_night")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WINDRISE_NIGHT =
            SOUND_EVENTS.register("music_disc_windrise_night",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "music_disc_windrise_night")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_DIVINE_FOX =
            SOUND_EVENTS.register("music_disc_divine_fox",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "music_disc_divine_fox")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_WATERS_POEM =
            SOUND_EVENTS.register("music_disc_waters_poem",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "music_disc_waters_poem")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_DAY_OF_HOPE =
            SOUND_EVENTS.register("music_disc_day_of_hope",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "music_disc_day_of_hope")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_CHASING_STARS =
            SOUND_EVENTS.register("music_disc_chasing_stars",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "music_disc_chasing_stars")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_SALTED_MOON =
            SOUND_EVENTS.register("music_disc_salted_moon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "music_disc_salted_moon")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CURIO_BREAK =
            SOUND_EVENTS.register("curio_break",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "curio_break")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CHILDE_BATTLE_BGM =
            SOUND_EVENTS.register("childe_battle_bgm",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "childe_battle_bgm")));

    public static final DeferredHolder<SoundEvent, SoundEvent> RAIDEN_BATTLE_BGM =
            SOUND_EVENTS.register("raiden_battle_bgm",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "raiden_battle_bgm")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FURINA_SUMMON =
            SOUND_EVENTS.register("companion_furina_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_furina_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FURINA_SKILL =
            SOUND_EVENTS.register("companion_furina_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_furina_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FURINA_AMBIENT =
            SOUND_EVENTS.register("companion_furina_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_furina_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FURINA_HURT =
            SOUND_EVENTS.register("companion_furina_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_furina_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_RAIDEN_SHOGUN_SUMMON =
            SOUND_EVENTS.register("companion_raiden_shogun_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_raiden_shogun_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_RAIDEN_SHOGUN_SKILL =
            SOUND_EVENTS.register("companion_raiden_shogun_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_raiden_shogun_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_RAIDEN_SHOGUN_AMBIENT =
            SOUND_EVENTS.register("companion_raiden_shogun_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_raiden_shogun_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_RAIDEN_SHOGUN_HURT =
            SOUND_EVENTS.register("companion_raiden_shogun_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_raiden_shogun_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_HU_TAO_SUMMON =
            SOUND_EVENTS.register("companion_hu_tao_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_hu_tao_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_HU_TAO_SKILL =
            SOUND_EVENTS.register("companion_hu_tao_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_hu_tao_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_HU_TAO_AMBIENT =
            SOUND_EVENTS.register("companion_hu_tao_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_hu_tao_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_HU_TAO_HURT =
            SOUND_EVENTS.register("companion_hu_tao_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_hu_tao_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ZHONGLI_SUMMON =
            SOUND_EVENTS.register("companion_zhongli_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_zhongli_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ZHONGLI_SKILL =
            SOUND_EVENTS.register("companion_zhongli_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_zhongli_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ZHONGLI_AMBIENT =
            SOUND_EVENTS.register("companion_zhongli_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_zhongli_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ZHONGLI_HURT =
            SOUND_EVENTS.register("companion_zhongli_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_zhongli_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NAHIDA_SUMMON =
            SOUND_EVENTS.register("companion_nahida_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nahida_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NAHIDA_SKILL =
            SOUND_EVENTS.register("companion_nahida_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nahida_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NAHIDA_AMBIENT =
            SOUND_EVENTS.register("companion_nahida_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nahida_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NAHIDA_HURT =
            SOUND_EVENTS.register("companion_nahida_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nahida_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KAZUHA_SUMMON =
            SOUND_EVENTS.register("companion_kazuha_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kazuha_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KAZUHA_SKILL =
            SOUND_EVENTS.register("companion_kazuha_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kazuha_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KAZUHA_AMBIENT =
            SOUND_EVENTS.register("companion_kazuha_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kazuha_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KAZUHA_HURT =
            SOUND_EVENTS.register("companion_kazuha_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kazuha_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NOELLE_SUMMON =
            SOUND_EVENTS.register("companion_noelle_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_noelle_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NOELLE_SKILL =
            SOUND_EVENTS.register("companion_noelle_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_noelle_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NOELLE_AMBIENT =
            SOUND_EVENTS.register("companion_noelle_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_noelle_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NOELLE_HURT =
            SOUND_EVENTS.register("companion_noelle_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_noelle_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GOJO_SATORU_SUMMON =
            SOUND_EVENTS.register("companion_gojo_satoru_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_gojo_satoru_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GOJO_SATORU_SKILL =
            SOUND_EVENTS.register("companion_gojo_satoru_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_gojo_satoru_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GOJO_SATORU_AMBIENT =
            SOUND_EVENTS.register("companion_gojo_satoru_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_gojo_satoru_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GOJO_SATORU_HURT =
            SOUND_EVENTS.register("companion_gojo_satoru_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_gojo_satoru_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SUNG_JINWOO_SUMMON =
            SOUND_EVENTS.register("companion_sung_jinwoo_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_sung_jinwoo_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SUNG_JINWOO_SKILL =
            SOUND_EVENTS.register("companion_sung_jinwoo_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_sung_jinwoo_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SUNG_JINWOO_AMBIENT =
            SOUND_EVENTS.register("companion_sung_jinwoo_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_sung_jinwoo_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SUNG_JINWOO_HURT =
            SOUND_EVENTS.register("companion_sung_jinwoo_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_sung_jinwoo_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CHA_HAEIN_SUMMON =
            SOUND_EVENTS.register("companion_cha_haein_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_cha_haein_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CHA_HAEIN_SKILL =
            SOUND_EVENTS.register("companion_cha_haein_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_cha_haein_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CHA_HAEIN_AMBIENT =
            SOUND_EVENTS.register("companion_cha_haein_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_cha_haein_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CHA_HAEIN_HURT =
            SOUND_EVENTS.register("companion_cha_haein_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_cha_haein_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_DILUC_SUMMON =
            SOUND_EVENTS.register("companion_diluc_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_diluc_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_DILUC_SKILL =
            SOUND_EVENTS.register("companion_diluc_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_diluc_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_DILUC_AMBIENT =
            SOUND_EVENTS.register("companion_diluc_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_diluc_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_DILUC_HURT =
            SOUND_EVENTS.register("companion_diluc_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_diluc_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JEAN_SUMMON =
            SOUND_EVENTS.register("companion_jean_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jean_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JEAN_SKILL =
            SOUND_EVENTS.register("companion_jean_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jean_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JEAN_AMBIENT =
            SOUND_EVENTS.register("companion_jean_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jean_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JEAN_HURT =
            SOUND_EVENTS.register("companion_jean_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jean_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BENNETT_SUMMON =
            SOUND_EVENTS.register("companion_bennett_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_bennett_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BENNETT_SKILL =
            SOUND_EVENTS.register("companion_bennett_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_bennett_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BENNETT_AMBIENT =
            SOUND_EVENTS.register("companion_bennett_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_bennett_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BENNETT_HURT =
            SOUND_EVENTS.register("companion_bennett_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_bennett_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_XIAO_SUMMON =
            SOUND_EVENTS.register("companion_xiao_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_xiao_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_XIAO_SKILL =
            SOUND_EVENTS.register("companion_xiao_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_xiao_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_XIAO_AMBIENT =
            SOUND_EVENTS.register("companion_xiao_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_xiao_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_XIAO_HURT =
            SOUND_EVENTS.register("companion_xiao_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_xiao_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARLECCHINO_SUMMON =
            SOUND_EVENTS.register("companion_arlecchino_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_arlecchino_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARLECCHINO_SKILL =
            SOUND_EVENTS.register("companion_arlecchino_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_arlecchino_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARLECCHINO_AMBIENT =
            SOUND_EVENTS.register("companion_arlecchino_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_arlecchino_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARLECCHINO_HURT =
            SOUND_EVENTS.register("companion_arlecchino_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_arlecchino_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ACHERON_SUMMON =
            SOUND_EVENTS.register("companion_acheron_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_acheron_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ACHERON_SKILL =
            SOUND_EVENTS.register("companion_acheron_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_acheron_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ACHERON_AMBIENT =
            SOUND_EVENTS.register("companion_acheron_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_acheron_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ACHERON_HURT =
            SOUND_EVENTS.register("companion_acheron_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_acheron_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KAFKA_SUMMON =
            SOUND_EVENTS.register("companion_kafka_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kafka_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KAFKA_SKILL =
            SOUND_EVENTS.register("companion_kafka_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kafka_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KAFKA_AMBIENT =
            SOUND_EVENTS.register("companion_kafka_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kafka_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KAFKA_HURT =
            SOUND_EVENTS.register("companion_kafka_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kafka_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FIREFLY_SUMMON =
            SOUND_EVENTS.register("companion_firefly_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_firefly_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FIREFLY_SKILL =
            SOUND_EVENTS.register("companion_firefly_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_firefly_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FIREFLY_AMBIENT =
            SOUND_EVENTS.register("companion_firefly_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_firefly_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FIREFLY_HURT =
            SOUND_EVENTS.register("companion_firefly_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_firefly_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ELLEN_JOE_SUMMON =
            SOUND_EVENTS.register("companion_ellen_joe_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_ellen_joe_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ELLEN_JOE_SKILL =
            SOUND_EVENTS.register("companion_ellen_joe_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_ellen_joe_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ELLEN_JOE_AMBIENT =
            SOUND_EVENTS.register("companion_ellen_joe_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_ellen_joe_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ELLEN_JOE_HURT =
            SOUND_EVENTS.register("companion_ellen_joe_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_ellen_joe_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_IGRIS_SUMMON =
            SOUND_EVENTS.register("companion_igris_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_igris_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_IGRIS_SKILL =
            SOUND_EVENTS.register("companion_igris_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_igris_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_IGRIS_AMBIENT =
            SOUND_EVENTS.register("companion_igris_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_igris_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_IGRIS_HURT =
            SOUND_EVENTS.register("companion_igris_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_igris_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BERU_SUMMON =
            SOUND_EVENTS.register("companion_beru_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_beru_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BERU_SKILL =
            SOUND_EVENTS.register("companion_beru_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_beru_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BERU_AMBIENT =
            SOUND_EVENTS.register("companion_beru_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_beru_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BERU_HURT =
            SOUND_EVENTS.register("companion_beru_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_beru_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SUKUNA_SUMMON =
            SOUND_EVENTS.register("companion_sukuna_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_sukuna_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SUKUNA_SKILL =
            SOUND_EVENTS.register("companion_sukuna_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_sukuna_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SUKUNA_AMBIENT =
            SOUND_EVENTS.register("companion_sukuna_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_sukuna_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SUKUNA_HURT =
            SOUND_EVENTS.register("companion_sukuna_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_sukuna_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_YUJI_ITADORI_SUMMON =
            SOUND_EVENTS.register("companion_yuji_itadori_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_yuji_itadori_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_YUJI_ITADORI_SKILL =
            SOUND_EVENTS.register("companion_yuji_itadori_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_yuji_itadori_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_YUJI_ITADORI_AMBIENT =
            SOUND_EVENTS.register("companion_yuji_itadori_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_yuji_itadori_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_YUJI_ITADORI_HURT =
            SOUND_EVENTS.register("companion_yuji_itadori_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_yuji_itadori_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MEGUMI_SUMMON =
            SOUND_EVENTS.register("companion_megumi_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_megumi_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MEGUMI_SKILL =
            SOUND_EVENTS.register("companion_megumi_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_megumi_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MEGUMI_AMBIENT =
            SOUND_EVENTS.register("companion_megumi_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_megumi_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MEGUMI_HURT =
            SOUND_EVENTS.register("companion_megumi_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_megumi_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARTORIA_PENDRAGON_SUMMON =
            SOUND_EVENTS.register("companion_artoria_pendragon_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_artoria_pendragon_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARTORIA_PENDRAGON_SKILL =
            SOUND_EVENTS.register("companion_artoria_pendragon_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_artoria_pendragon_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARTORIA_PENDRAGON_AMBIENT =
            SOUND_EVENTS.register("companion_artoria_pendragon_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_artoria_pendragon_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARTORIA_PENDRAGON_HURT =
            SOUND_EVENTS.register("companion_artoria_pendragon_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_artoria_pendragon_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GILGAMESH_SUMMON =
            SOUND_EVENTS.register("companion_gilgamesh_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_gilgamesh_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GILGAMESH_SKILL =
            SOUND_EVENTS.register("companion_gilgamesh_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_gilgamesh_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GILGAMESH_AMBIENT =
            SOUND_EVENTS.register("companion_gilgamesh_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_gilgamesh_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GILGAMESH_HURT =
            SOUND_EVENTS.register("companion_gilgamesh_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_gilgamesh_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARCHER_SUMMON =
            SOUND_EVENTS.register("companion_archer_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_archer_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARCHER_SKILL =
            SOUND_EVENTS.register("companion_archer_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_archer_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARCHER_AMBIENT =
            SOUND_EVENTS.register("companion_archer_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_archer_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ARCHER_HURT =
            SOUND_EVENTS.register("companion_archer_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_archer_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_RENGOKU_SUMMON =
            SOUND_EVENTS.register("companion_rengoku_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_rengoku_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_RENGOKU_SKILL =
            SOUND_EVENTS.register("companion_rengoku_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_rengoku_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_RENGOKU_AMBIENT =
            SOUND_EVENTS.register("companion_rengoku_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_rengoku_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_RENGOKU_HURT =
            SOUND_EVENTS.register("companion_rengoku_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_rengoku_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_TANJIRO_SUMMON =
            SOUND_EVENTS.register("companion_tanjiro_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_tanjiro_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_TANJIRO_SKILL =
            SOUND_EVENTS.register("companion_tanjiro_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_tanjiro_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_TANJIRO_AMBIENT =
            SOUND_EVENTS.register("companion_tanjiro_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_tanjiro_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_TANJIRO_HURT =
            SOUND_EVENTS.register("companion_tanjiro_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_tanjiro_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NEZUKO_SUMMON =
            SOUND_EVENTS.register("companion_nezuko_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nezuko_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NEZUKO_SKILL =
            SOUND_EVENTS.register("companion_nezuko_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nezuko_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NEZUKO_AMBIENT =
            SOUND_EVENTS.register("companion_nezuko_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nezuko_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NEZUKO_HURT =
            SOUND_EVENTS.register("companion_nezuko_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nezuko_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GANYU_SUMMON =
            SOUND_EVENTS.register("companion_ganyu_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_ganyu_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GANYU_SKILL =
            SOUND_EVENTS.register("companion_ganyu_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_ganyu_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GANYU_AMBIENT =
            SOUND_EVENTS.register("companion_ganyu_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_ganyu_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GANYU_HURT =
            SOUND_EVENTS.register("companion_ganyu_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_ganyu_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NEUVILLETTE_SUMMON =
            SOUND_EVENTS.register("companion_neuvillette_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_neuvillette_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NEUVILLETTE_SKILL =
            SOUND_EVENTS.register("companion_neuvillette_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_neuvillette_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NEUVILLETTE_AMBIENT =
            SOUND_EVENTS.register("companion_neuvillette_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_neuvillette_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NEUVILLETTE_HURT =
            SOUND_EVENTS.register("companion_neuvillette_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_neuvillette_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MARCH_7TH_SUMMON =
            SOUND_EVENTS.register("companion_march_7th_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_march_7th_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MARCH_7TH_SKILL =
            SOUND_EVENTS.register("companion_march_7th_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_march_7th_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MARCH_7TH_AMBIENT =
            SOUND_EVENTS.register("companion_march_7th_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_march_7th_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MARCH_7TH_HURT =
            SOUND_EVENTS.register("companion_march_7th_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_march_7th_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_DAN_HENG_IL_SUMMON =
            SOUND_EVENTS.register("companion_dan_heng_il_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_dan_heng_il_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_DAN_HENG_IL_SKILL =
            SOUND_EVENTS.register("companion_dan_heng_il_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_dan_heng_il_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_DAN_HENG_IL_AMBIENT =
            SOUND_EVENTS.register("companion_dan_heng_il_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_dan_heng_il_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_DAN_HENG_IL_HURT =
            SOUND_EVENTS.register("companion_dan_heng_il_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_dan_heng_il_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FEIXIAO_SUMMON =
            SOUND_EVENTS.register("companion_feixiao_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_feixiao_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FEIXIAO_SKILL =
            SOUND_EVENTS.register("companion_feixiao_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_feixiao_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FEIXIAO_AMBIENT =
            SOUND_EVENTS.register("companion_feixiao_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_feixiao_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_FEIXIAO_HURT =
            SOUND_EVENTS.register("companion_feixiao_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_feixiao_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_AVENTURINE_SUMMON =
            SOUND_EVENTS.register("companion_aventurine_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_aventurine_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_AVENTURINE_SKILL =
            SOUND_EVENTS.register("companion_aventurine_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_aventurine_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_AVENTURINE_AMBIENT =
            SOUND_EVENTS.register("companion_aventurine_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_aventurine_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_AVENTURINE_HURT =
            SOUND_EVENTS.register("companion_aventurine_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_aventurine_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ROBIN_SUMMON =
            SOUND_EVENTS.register("companion_robin_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_robin_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ROBIN_SKILL =
            SOUND_EVENTS.register("companion_robin_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_robin_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ROBIN_AMBIENT =
            SOUND_EVENTS.register("companion_robin_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_robin_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ROBIN_HURT =
            SOUND_EVENTS.register("companion_robin_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_robin_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JING_YUAN_SUMMON =
            SOUND_EVENTS.register("companion_jing_yuan_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jing_yuan_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JING_YUAN_SKILL =
            SOUND_EVENTS.register("companion_jing_yuan_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jing_yuan_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JING_YUAN_AMBIENT =
            SOUND_EVENTS.register("companion_jing_yuan_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jing_yuan_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JING_YUAN_HURT =
            SOUND_EVENTS.register("companion_jing_yuan_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jing_yuan_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JINGLIU_SUMMON =
            SOUND_EVENTS.register("companion_jingliu_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jingliu_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JINGLIU_SKILL =
            SOUND_EVENTS.register("companion_jingliu_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jingliu_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JINGLIU_AMBIENT =
            SOUND_EVENTS.register("companion_jingliu_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jingliu_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JINGLIU_HURT =
            SOUND_EVENTS.register("companion_jingliu_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jingliu_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_HOSHIMI_MIYABI_SUMMON =
            SOUND_EVENTS.register("companion_hoshimi_miyabi_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_hoshimi_miyabi_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_HOSHIMI_MIYABI_SKILL =
            SOUND_EVENTS.register("companion_hoshimi_miyabi_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_hoshimi_miyabi_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_HOSHIMI_MIYABI_AMBIENT =
            SOUND_EVENTS.register("companion_hoshimi_miyabi_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_hoshimi_miyabi_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_HOSHIMI_MIYABI_HURT =
            SOUND_EVENTS.register("companion_hoshimi_miyabi_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_hoshimi_miyabi_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JANE_DOE_SUMMON =
            SOUND_EVENTS.register("companion_jane_doe_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jane_doe_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JANE_DOE_SKILL =
            SOUND_EVENTS.register("companion_jane_doe_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jane_doe_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JANE_DOE_AMBIENT =
            SOUND_EVENTS.register("companion_jane_doe_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jane_doe_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JANE_DOE_HURT =
            SOUND_EVENTS.register("companion_jane_doe_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jane_doe_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CAESAR_KING_SUMMON =
            SOUND_EVENTS.register("companion_caesar_king_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_caesar_king_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CAESAR_KING_SKILL =
            SOUND_EVENTS.register("companion_caesar_king_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_caesar_king_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CAESAR_KING_AMBIENT =
            SOUND_EVENTS.register("companion_caesar_king_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_caesar_king_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CAESAR_KING_HURT =
            SOUND_EVENTS.register("companion_caesar_king_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_caesar_king_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BURNICE_WHITE_SUMMON =
            SOUND_EVENTS.register("companion_burnice_white_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_burnice_white_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BURNICE_WHITE_SKILL =
            SOUND_EVENTS.register("companion_burnice_white_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_burnice_white_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BURNICE_WHITE_AMBIENT =
            SOUND_EVENTS.register("companion_burnice_white_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_burnice_white_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BURNICE_WHITE_HURT =
            SOUND_EVENTS.register("companion_burnice_white_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_burnice_white_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NICOLE_DEMARA_SUMMON =
            SOUND_EVENTS.register("companion_nicole_demara_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nicole_demara_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NICOLE_DEMARA_SKILL =
            SOUND_EVENTS.register("companion_nicole_demara_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nicole_demara_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NICOLE_DEMARA_AMBIENT =
            SOUND_EVENTS.register("companion_nicole_demara_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nicole_demara_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_NICOLE_DEMARA_HURT =
            SOUND_EVENTS.register("companion_nicole_demara_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_nicole_demara_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ZHU_YUAN_SUMMON =
            SOUND_EVENTS.register("companion_zhu_yuan_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_zhu_yuan_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ZHU_YUAN_SKILL =
            SOUND_EVENTS.register("companion_zhu_yuan_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_zhu_yuan_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ZHU_YUAN_AMBIENT =
            SOUND_EVENTS.register("companion_zhu_yuan_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_zhu_yuan_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ZHU_YUAN_HURT =
            SOUND_EVENTS.register("companion_zhu_yuan_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_zhu_yuan_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JIYAN_SUMMON =
            SOUND_EVENTS.register("companion_jiyan_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jiyan_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JIYAN_SKILL =
            SOUND_EVENTS.register("companion_jiyan_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jiyan_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JIYAN_AMBIENT =
            SOUND_EVENTS.register("companion_jiyan_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jiyan_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JIYAN_HURT =
            SOUND_EVENTS.register("companion_jiyan_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jiyan_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_YINLIN_SUMMON =
            SOUND_EVENTS.register("companion_yinlin_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_yinlin_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_YINLIN_SKILL =
            SOUND_EVENTS.register("companion_yinlin_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_yinlin_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_YINLIN_AMBIENT =
            SOUND_EVENTS.register("companion_yinlin_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_yinlin_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_YINLIN_HURT =
            SOUND_EVENTS.register("companion_yinlin_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_yinlin_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CHANGLI_SUMMON =
            SOUND_EVENTS.register("companion_changli_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_changli_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CHANGLI_SKILL =
            SOUND_EVENTS.register("companion_changli_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_changli_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CHANGLI_AMBIENT =
            SOUND_EVENTS.register("companion_changli_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_changli_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CHANGLI_HURT =
            SOUND_EVENTS.register("companion_changli_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_changli_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JINSHI_SUMMON =
            SOUND_EVENTS.register("companion_jinshi_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jinshi_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JINSHI_SKILL =
            SOUND_EVENTS.register("companion_jinshi_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jinshi_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JINSHI_AMBIENT =
            SOUND_EVENTS.register("companion_jinshi_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jinshi_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_JINSHI_HURT =
            SOUND_EVENTS.register("companion_jinshi_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_jinshi_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SHOREKEEPER_SUMMON =
            SOUND_EVENTS.register("companion_shorekeeper_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_shorekeeper_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SHOREKEEPER_SKILL =
            SOUND_EVENTS.register("companion_shorekeeper_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_shorekeeper_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SHOREKEEPER_AMBIENT =
            SOUND_EVENTS.register("companion_shorekeeper_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_shorekeeper_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SHOREKEEPER_HURT =
            SOUND_EVENTS.register("companion_shorekeeper_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_shorekeeper_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CAMELLYA_SUMMON =
            SOUND_EVENTS.register("companion_camellya_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_camellya_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CAMELLYA_SKILL =
            SOUND_EVENTS.register("companion_camellya_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_camellya_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CAMELLYA_AMBIENT =
            SOUND_EVENTS.register("companion_camellya_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_camellya_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CAMELLYA_HURT =
            SOUND_EVENTS.register("companion_camellya_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_camellya_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ROVER_SUMMON =
            SOUND_EVENTS.register("companion_rover_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_rover_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ROVER_SKILL =
            SOUND_EVENTS.register("companion_rover_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_rover_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ROVER_AMBIENT =
            SOUND_EVENTS.register("companion_rover_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_rover_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ROVER_HURT =
            SOUND_EVENTS.register("companion_rover_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_rover_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CLORINDE_SUMMON =
            SOUND_EVENTS.register("companion_clorinde_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_clorinde_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CLORINDE_SKILL =
            SOUND_EVENTS.register("companion_clorinde_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_clorinde_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CLORINDE_AMBIENT =
            SOUND_EVENTS.register("companion_clorinde_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_clorinde_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CLORINDE_HURT =
            SOUND_EVENTS.register("companion_clorinde_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_clorinde_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SKIRK_SUMMON =
            SOUND_EVENTS.register("companion_skirk_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_skirk_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SKIRK_SKILL =
            SOUND_EVENTS.register("companion_skirk_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_skirk_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SKIRK_AMBIENT =
            SOUND_EVENTS.register("companion_skirk_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_skirk_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SKIRK_HURT =
            SOUND_EVENTS.register("companion_skirk_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_skirk_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_XILONEN_SUMMON =
            SOUND_EVENTS.register("companion_xilonen_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_xilonen_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_XILONEN_SKILL =
            SOUND_EVENTS.register("companion_xilonen_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_xilonen_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_XILONEN_AMBIENT =
            SOUND_EVENTS.register("companion_xilonen_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_xilonen_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_XILONEN_HURT =
            SOUND_EVENTS.register("companion_xilonen_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_xilonen_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MAVUIKA_SUMMON =
            SOUND_EVENTS.register("companion_mavuika_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_mavuika_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MAVUIKA_SKILL =
            SOUND_EVENTS.register("companion_mavuika_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_mavuika_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MAVUIKA_AMBIENT =
            SOUND_EVENTS.register("companion_mavuika_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_mavuika_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_MAVUIKA_HURT =
            SOUND_EVENTS.register("companion_mavuika_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_mavuika_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KINICH_SUMMON =
            SOUND_EVENTS.register("companion_kinich_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kinich_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KINICH_SKILL =
            SOUND_EVENTS.register("companion_kinich_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kinich_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KINICH_AMBIENT =
            SOUND_EVENTS.register("companion_kinich_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kinich_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KINICH_HURT =
            SOUND_EVENTS.register("companion_kinich_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_kinich_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_COLUMBINA_SUMMON =
            SOUND_EVENTS.register("companion_columbina_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_columbina_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_COLUMBINA_SKILL =
            SOUND_EVENTS.register("companion_columbina_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_columbina_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_COLUMBINA_AMBIENT =
            SOUND_EVENTS.register("companion_columbina_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_columbina_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_COLUMBINA_HURT =
            SOUND_EVENTS.register("companion_columbina_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_columbina_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CASINO_INSERT_COIN =
            SOUND_EVENTS.register("casino_insert_coin",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "casino_insert_coin")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CASINO_REELS_SPINNING =
            SOUND_EVENTS.register("casino_reels_spinning",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "casino_reels_spinning")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CASINO_WIN =
            SOUND_EVENTS.register("casino_win",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "casino_win")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CASINO_JACKPOT =
            SOUND_EVENTS.register("casino_jackpot",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "casino_jackpot")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CASINO_LOSE =
            SOUND_EVENTS.register("casino_lose",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "casino_lose")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CASINO_BONUS_PRIZE =
            SOUND_EVENTS.register("casino_bonus_prize",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "casino_bonus_prize")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CASINO_BUTTON =
            SOUND_EVENTS.register("casino_button",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "casino_button")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CASINO_AMBIENT =
            SOUND_EVENTS.register("casino_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "casino_ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> GACHA_COMMON_PRIZE =
            SOUND_EVENTS.register("gacha_common_prize",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gacha_common_prize")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GACHA_UNCOMMON_PRIZE =
            SOUND_EVENTS.register("gacha_uncommon_prize",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gacha_uncommon_prize")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GACHA_RARE_PRIZE =
            SOUND_EVENTS.register("gacha_rare_prize",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gacha_rare_prize")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GACHA_ULTRARARE_PRIZE =
            SOUND_EVENTS.register("gacha_ultrarare_prize",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gacha_ultrarare_prize")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GACHA_LEGENDARY_PRIZE =
            SOUND_EVENTS.register("gacha_legendary_prize",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gacha_legendary_prize")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GACHA_OPEN_PRIZE =
            SOUND_EVENTS.register("gacha_open_prize",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "gacha_open_prize")));

    public static final DeferredHolder<SoundEvent, SoundEvent> GENSHIN_SLIME_AMBIENT =
            SOUND_EVENTS.register("genshin_slime_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "genshin_slime_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GENSHIN_SLIME_HURT =
            SOUND_EVENTS.register("genshin_slime_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "genshin_slime_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GENSHIN_SLIME_DEATH =
            SOUND_EVENTS.register("genshin_slime_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "genshin_slime_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GENSHIN_SLIME_JUMP =
            SOUND_EVENTS.register("genshin_slime_jump",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "genshin_slime_jump")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HILICHURL_AMBIENT =
            SOUND_EVENTS.register("hilichurl_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "hilichurl_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HILICHURL_HURT =
            SOUND_EVENTS.register("hilichurl_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "hilichurl_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HILICHURL_DEATH =
            SOUND_EVENTS.register("hilichurl_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "hilichurl_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HILICHURL_ATTACK =
            SOUND_EVENTS.register("hilichurl_attack",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "hilichurl_attack")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MITACHURL_AMBIENT =
            SOUND_EVENTS.register("mitachurl_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mitachurl_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MITACHURL_HURT =
            SOUND_EVENTS.register("mitachurl_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mitachurl_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MITACHURL_DEATH =
            SOUND_EVENTS.register("mitachurl_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mitachurl_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MITACHURL_AXE_SLAM =
            SOUND_EVENTS.register("mitachurl_axe_slam",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mitachurl_axe_slam")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MITACHURL_SHIELD_CHARGE =
            SOUND_EVENTS.register("mitachurl_shield_charge",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mitachurl_shield_charge")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSS_ENTER =
            SOUND_EVENTS.register("abyss_enter",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyss_enter")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ANEMO_HYPOSTASIS_CLAP =
            SOUND_EVENTS.register("anemo_hypostasis_clap",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "anemo_hypostasis_clap")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANEMO_HYPOSTASIS_MISSILES =
            SOUND_EVENTS.register("anemo_hypostasis_missiles",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "anemo_hypostasis_missiles")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANEMO_HYPOSTASIS_TORNADOES =
            SOUND_EVENTS.register("anemo_hypostasis_tornadoes",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "anemo_hypostasis_tornadoes")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANEMO_HYPOSTASIS_BUTTERFLY =
            SOUND_EVENTS.register("anemo_hypostasis_butterfly",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "anemo_hypostasis_butterfly")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANEMO_HYPOSTASIS_VACUUM =
            SOUND_EVENTS.register("anemo_hypostasis_vacuum",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "anemo_hypostasis_vacuum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRO_HYPOSTASIS_CLAP =
            SOUND_EVENTS.register("electro_hypostasis_clap",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "electro_hypostasis_clap")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRO_HYPOSTASIS_DRILL =
            SOUND_EVENTS.register("electro_hypostasis_drill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "electro_hypostasis_drill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRO_HYPOSTASIS_LASERS =
            SOUND_EVENTS.register("electro_hypostasis_lasers",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "electro_hypostasis_lasers")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRO_HYPOSTASIS_RPS =
            SOUND_EVENTS.register("electro_hypostasis_rps",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "electro_hypostasis_rps")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRO_HYPOSTASIS_REBIRTH =
            SOUND_EVENTS.register("electro_hypostasis_rebirth",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "electro_hypostasis_rebirth")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HYPOSTASIS_COLLAPSE =
            SOUND_EVENTS.register("hypostasis_collapse",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "hypostasis_collapse")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HYPOSTASIS_REFORM =
            SOUND_EVENTS.register("hypostasis_reform",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "hypostasis_reform")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WILDFIRE_AMBIENT =
            SOUND_EVENTS.register("wildfire_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "entity.wildfire.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WILDFIRE_HURT =
            SOUND_EVENTS.register("wildfire_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "entity.wildfire.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WILDFIRE_DEATH =
            SOUND_EVENTS.register("wildfire_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "entity.wildfire.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WILDFIRE_SHIELD_BREAK =
            SOUND_EVENTS.register("wildfire_shield_break",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "entity.wildfire.shield_break")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WILDFIRE_SHOCKWAVE =
            SOUND_EVENTS.register("wildfire_shockwave",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "entity.wildfire.shockwave")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WILDFIRE_SHOOT =
            SOUND_EVENTS.register("wildfire_shoot",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "entity.wildfire.shoot")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WILDFIRE_SUMMON_BLAZE =
            SOUND_EVENTS.register("wildfire_summon_blaze",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "entity.wildfire.summon_blaze")));

    // ZZZ & New Companions Voices
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GRACE_HOWARD_SUMMON =
            SOUND_EVENTS.register("companion_grace_howard_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_grace_howard_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GRACE_HOWARD_SKILL =
            SOUND_EVENTS.register("companion_grace_howard_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_grace_howard_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GRACE_HOWARD_AMBIENT =
            SOUND_EVENTS.register("companion_grace_howard_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_grace_howard_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_GRACE_HOWARD_HURT =
            SOUND_EVENTS.register("companion_grace_howard_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_grace_howard_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BILLY_KID_SUMMON =
            SOUND_EVENTS.register("companion_billy_kid_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_billy_kid_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BILLY_KID_SKILL =
            SOUND_EVENTS.register("companion_billy_kid_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_billy_kid_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BILLY_KID_AMBIENT =
            SOUND_EVENTS.register("companion_billy_kid_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_billy_kid_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_BILLY_KID_HURT =
            SOUND_EVENTS.register("companion_billy_kid_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_billy_kid_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ANBY_DEMARA_SUMMON =
            SOUND_EVENTS.register("companion_anby_demara_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_anby_demara_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ANBY_DEMARA_SKILL =
            SOUND_EVENTS.register("companion_anby_demara_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_anby_demara_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ANBY_DEMARA_AMBIENT =
            SOUND_EVENTS.register("companion_anby_demara_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_anby_demara_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_ANBY_DEMARA_HURT =
            SOUND_EVENTS.register("companion_anby_demara_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_anby_demara_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KOLEDA_SUMMON =
            SOUND_EVENTS.register("companion_koleda_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_koleda_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KOLEDA_SKILL =
            SOUND_EVENTS.register("companion_koleda_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_koleda_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KOLEDA_AMBIENT =
            SOUND_EVENTS.register("companion_koleda_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_koleda_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_KOLEDA_HURT =
            SOUND_EVENTS.register("companion_koleda_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_koleda_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_LUCY_SUMMON =
            SOUND_EVENTS.register("companion_lucy_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_lucy_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_LUCY_SKILL =
            SOUND_EVENTS.register("companion_lucy_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_lucy_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_LUCY_AMBIENT =
            SOUND_EVENTS.register("companion_lucy_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_lucy_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_LUCY_HURT =
            SOUND_EVENTS.register("companion_lucy_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_lucy_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_PIPER_WHEEL_SUMMON =
            SOUND_EVENTS.register("companion_piper_wheel_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_piper_wheel_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_PIPER_WHEEL_SKILL =
            SOUND_EVENTS.register("companion_piper_wheel_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_piper_wheel_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_PIPER_WHEEL_AMBIENT =
            SOUND_EVENTS.register("companion_piper_wheel_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_piper_wheel_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_PIPER_WHEEL_HURT =
            SOUND_EVENTS.register("companion_piper_wheel_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_piper_wheel_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CORIN_WICKES_SUMMON =
            SOUND_EVENTS.register("companion_corin_wickes_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_corin_wickes_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CORIN_WICKES_SKILL =
            SOUND_EVENTS.register("companion_corin_wickes_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_corin_wickes_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CORIN_WICKES_AMBIENT =
            SOUND_EVENTS.register("companion_corin_wickes_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_corin_wickes_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_CORIN_WICKES_HURT =
            SOUND_EVENTS.register("companion_corin_wickes_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_corin_wickes_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SOUKAKU_SUMMON =
            SOUND_EVENTS.register("companion_soukaku_summon",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_soukaku_summon")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SOUKAKU_SKILL =
            SOUND_EVENTS.register("companion_soukaku_skill",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_soukaku_skill")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SOUKAKU_AMBIENT =
            SOUND_EVENTS.register("companion_soukaku_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_soukaku_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANION_SOUKAKU_HURT =
            SOUND_EVENTS.register("companion_soukaku_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_soukaku_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> PLUSHIE_SQUISH =
            SOUND_EVENTS.register("plushie_squish",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "plushie_squish")));

    public static final DeferredHolder<SoundEvent, SoundEvent> LIMBO_OPHANIM_AMBIENT =
            SOUND_EVENTS.register("limbo_ophanim_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "limbo_ophanim_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LIMBO_OPHANIM_HURT =
            SOUND_EVENTS.register("limbo_ophanim_hurt",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "limbo_ophanim_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LIMBO_OPHANIM_DEATH =
            SOUND_EVENTS.register("limbo_ophanim_death",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "limbo_ophanim_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LIMBO_OPHANIM_CHARGE =
            SOUND_EVENTS.register("limbo_ophanim_charge",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "limbo_ophanim_charge")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LIMBO_OPHANIM_SHOOT =
            SOUND_EVENTS.register("limbo_ophanim_shoot",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "limbo_ophanim_shoot")));

    public static final DeferredHolder<SoundEvent, SoundEvent> LIMBO_ENTITY_AMBIENT =
            SOUND_EVENTS.register("limbo_entity_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "limbo_entity_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LIMBO_ENTITY_FOLLOWING =
            SOUND_EVENTS.register("limbo_entity_following",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "limbo_entity_following")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SPIDER_HEAD_AMBIENT =
            SOUND_EVENTS.register("spider_head_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "spider_head_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIDER_HEAD_FOLLOWING =
            SOUND_EVENTS.register("spider_head_following",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "spider_head_following")));

    public static final DeferredHolder<SoundEvent, SoundEvent> STILT_WALKER_AMBIENT =
            SOUND_EVENTS.register("stilt_walker_ambient",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "stilt_walker_ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> STILT_WALKER_FOLLOWING =
            SOUND_EVENTS.register("stilt_walker_following",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "stilt_walker_following")));

    public static final DeferredHolder<SoundEvent, SoundEvent> LIMBO_AWAKENING =
            SOUND_EVENTS.register("limbo_awakening",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "limbo_awakening")));

    private ModSounds() {}
}

