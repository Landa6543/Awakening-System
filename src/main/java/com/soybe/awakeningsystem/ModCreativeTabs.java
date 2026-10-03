package com.soybe.awakeningsystem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AwakeningSystemMod.MOD_ID);

    // ==========================================
    // 1. PESTAÑA: ARSENAL, COMBATE Y ARMADURAS
    // ==========================================
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COMBAT_TAB = CREATIVE_MODE_TABS.register("awakening_combat", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.awakening_system.combat"))
                    .icon(() -> SystemItems.STAFF_OF_HOMA.isBound() ? new ItemStack(SystemItems.STAFF_OF_HOMA.get()) : new ItemStack(Items.NETHERITE_SWORD))
                    .displayItems((parameters, output) -> {
                        // --- NOVEDADES DESTACADAS Y ARMAS PRINCIPALES ---
                        accept(output, SystemItems.SOUL_SOLID);
                        accept(output, SystemItems.LA_DONCELLA);
                        accept(output, SystemItems.WILDFIRE_CROWN);

                        // --- LANZAS Y ARMAS DE ASTA (POLEARMS) ---
                        accept(output, SystemItems.STAFF_OF_HOMA);
                        accept(output, SystemItems.ENGULFING_LIGHTNING);
                        accept(output, SystemItems.PRIMORDIAL_JADE_WINGED_SPEAR);
                        accept(output, SystemItems.CRIMSON_MOONS_SEMBLANCE);
                        accept(output, SystemItems.VORTEX_VANQUISHER);
                        accept(output, SystemItems.THE_CATCH);
                        accept(output, SystemItems.BLACK_TASSEL);
                        accept(output, SystemItems.ASTRAL_GUANDAO);

                        // --- ARCOS Y ARMAS A DISTANCIA (BOWS) ---
                        accept(output, SystemItems.AMOS_BOW);
                        accept(output, SystemItems.ASTRAL_ARCHERS_BOW);
                        accept(output, SystemItems.THUNDERING_PULSE);
                        accept(output, SystemItems.SKYWARD_HARP);
                        accept(output, SystemItems.AQUA_SIMULACRA);
                        accept(output, SystemItems.POLAR_STAR);
                        accept(output, SystemItems.HUNTER_PATH);
                        accept(output, SystemItems.HAMAYUMI);
                        accept(output, SystemItems.END_OF_THE_LINE);

                        // --- MANDOBLES Y ARMAS PESADAS ---
                        accept(output, SystemItems.WOLFS_GRAVESTONE);
                        accept(output, SystemItems.REDHORN_STONETHRESHER);
                        accept(output, SystemItems.VERDICT);
                        accept(output, SystemItems.TAILLESS);
                        accept(output, SystemItems.GIGANTIC_GREATSWORD);
                        accept(output, SystemItems.HERTA_HAMMER);
                        accept(output, SystemItems.ADAMANTITE_WAR_HAMMER);

                        // --- ESPADAS LIGERAS, KATANAS Y DAGAS ---
                        accept(output, SystemItems.SKYWARD_BLADE);
                        accept(output, SystemItems.MISTSPLITTER_REFORGED);
                        accept(output, SystemItems.PRIMORDIAL_JADE_CUTTER);
                        accept(output, SystemItems.FREEDOM_SWORN);
                        accept(output, SystemItems.KEY_OF_KHAJ_NISUT);
                        accept(output, SystemItems.ABSOLUTION);
                        accept(output, SystemItems.THE_FLUTE);
                        accept(output, SystemItems.THE_BLACK_SWORD);
                        accept(output, SystemItems.FLEUVE_CENDRE_FERRYMAN);
                        accept(output, SystemItems.QINGMING_SWORD);
                        accept(output, SystemItems.SHUNGUANG_SWORD);
                        accept(output, SystemItems.SHIYUAN_SWORD);
                        accept(output, SystemItems.DOMAIN_OF_SANCTION);
                        accept(output, SystemItems.PRIMOGEM_SWORD);

                        // --- ARMAS SHONEN, MANHWA Y MÍSTICAS ---
                        accept(output, SystemItems.LA_DONCELLA);
                        accept(output, SystemItems.SOUL_SOLID);
                        accept(output, SystemItems.PLUM_BLOSSOM_SWORD);
                        accept(output, SystemItems.ASRAHAN_HOLY_SWORD);
                        accept(output, SystemItems.DAWNS_BALLAD);
                        accept(output, SystemItems.CREATOR_CLAYMORE);
                        accept(output, SystemItems.ELUCIDATOR);
                        accept(output, SystemItems.DARK_REPULSER);
                        accept(output, SystemItems.LAMBENT_LIGHT);
                        accept(output, SystemItems.BLACK_DRAGON_SCYTHE);
                        accept(output, SystemItems.DREAD_SCYTHE);
                        accept(output, SystemItems.SHADOW_DAGGERS);
                        accept(output, SystemItems.RADIANT_LIGHT_SWORD);
                        accept(output, SystemItems.SKY_DEMON_SWORD);
                        accept(output, SystemItems.HEAVEN_SHAKING_SWORD);
                        accept(output, SystemItems.UNBREAKABLE_FAITH);
                        accept(output, SystemItems.TRANSCENDENT_THORN);
                        accept(output, SystemItems.FLYING_SWORD);
                        accept(output, SystemItems.JADE_FEIJIAN);
                        accept(output, SystemItems.DEATH_KING_CLOCK);
                        accept(output, SystemItems.WILDFIRE_CROWN);

                        // --- ESPADAS Y HACHAS POST-NETHERITE ---
                        accept(output, SystemItems.ABYSSAL_SWORD);
                        accept(output, SystemItems.ABYSSAL_AXE);
                        accept(output, SystemItems.MITHRIL_SWORD);
                        accept(output, SystemItems.MITHRIL_AXE);
                        accept(output, SystemItems.ADAMANTITE_SWORD);
                        accept(output, SystemItems.ADAMANTITE_AXE);
                        accept(output, SystemItems.SPIRIT_JADE_SWORD);
                        accept(output, SystemItems.SPIRIT_JADE_AXE);
                        accept(output, SystemItems.ASTRAL_SWORD);
                        accept(output, SystemItems.ASTRAL_AXE);

                        // --- ARMADURAS POST-NETHERITE ---
                        accept(output, SystemItems.ABYSSAL_HELMET);
                        accept(output, SystemItems.ABYSSAL_CHESTPLATE);
                        accept(output, SystemItems.ABYSSAL_LEGGINGS);
                        accept(output, SystemItems.ABYSSAL_BOOTS);

                        accept(output, SystemItems.MITHRIL_HELMET);
                        accept(output, SystemItems.MITHRIL_CHESTPLATE);
                        accept(output, SystemItems.MITHRIL_LEGGINGS);
                        accept(output, SystemItems.MITHRIL_BOOTS);

                        accept(output, SystemItems.ADAMANTITE_HELMET);
                        accept(output, SystemItems.ADAMANTITE_CHESTPLATE);
                        accept(output, SystemItems.ADAMANTITE_LEGGINGS);
                        accept(output, SystemItems.ADAMANTITE_BOOTS);

                        accept(output, SystemItems.SPIRIT_JADE_HELMET);
                        accept(output, SystemItems.SPIRIT_JADE_CHESTPLATE);
                        accept(output, SystemItems.SPIRIT_JADE_LEGGINGS);
                        accept(output, SystemItems.SPIRIT_JADE_BOOTS);

                        accept(output, SystemItems.ASTRAL_HELMET);
                        accept(output, SystemItems.ASTRAL_CHESTPLATE);
                        accept(output, SystemItems.ASTRAL_LEGGINGS);
                        accept(output, SystemItems.ASTRAL_BOOTS);
                        
                        // --- ARMADURA ÁUREA DE MORA (GENSHIN IMPACT / PRIMOGEMCRAFT) ---
                        accept(output, SystemItems.MORA_HELMET);
                        accept(output, SystemItems.MORA_CHESTPLATE);
                        accept(output, SystemItems.MORA_LEGGINGS);
                        accept(output, SystemItems.MORA_BOOTS);

                        // --- ARTEFACTOS DE TEYVAT (GENSHIN IMPACT) ---
                        // Bruja Carmesí en Llamas (Crimson Witch of Flames)
                        accept(output, SystemItems.ARTIFACT_CRIMSON_WITCH_FLOWER);
                        accept(output, SystemItems.ARTIFACT_CRIMSON_WITCH_PLUME);
                        accept(output, SystemItems.ARTIFACT_CRIMSON_WITCH_SANDS);
                        accept(output, SystemItems.ARTIFACT_CRIMSON_WITCH_GOBLET);
                        accept(output, SystemItems.ARTIFACT_CRIMSON_WITCH_CIRCLET);

                        // Final del Gladiador (Gladiator's Finale)
                        accept(output, SystemItems.ARTIFACT_GLADIATORS_FINALE_FLOWER);
                        accept(output, SystemItems.ARTIFACT_GLADIATORS_FINALE_PLUME);
                        accept(output, SystemItems.ARTIFACT_GLADIATORS_FINALE_SANDS);
                        accept(output, SystemItems.ARTIFACT_GLADIATORS_FINALE_GOBLET);
                        accept(output, SystemItems.ARTIFACT_GLADIATORS_FINALE_CIRCLET);

                        // Sombra Verde Esmeralda (Viridescent Venerer)
                        accept(output, SystemItems.ARTIFACT_VIRIDESCENT_VENERER_FLOWER);
                        accept(output, SystemItems.ARTIFACT_VIRIDESCENT_VENERER_PLUME);
                        accept(output, SystemItems.ARTIFACT_VIRIDESCENT_VENERER_SANDS);
                        accept(output, SystemItems.ARTIFACT_VIRIDESCENT_VENERER_GOBLET);
                        accept(output, SystemItems.ARTIFACT_VIRIDESCENT_VENERER_CIRCLET);

                        // Doncella Amada (Maiden Beloved)
                        accept(output, SystemItems.ARTIFACT_MAIDEN_BELOVED_FLOWER);
                        accept(output, SystemItems.ARTIFACT_MAIDEN_BELOVED_PLUME);
                        accept(output, SystemItems.ARTIFACT_MAIDEN_BELOVED_SANDS);
                        accept(output, SystemItems.ARTIFACT_MAIDEN_BELOVED_GOBLET);
                        accept(output, SystemItems.ARTIFACT_MAIDEN_BELOVED_CIRCLET);

                        // Caballería Sanguinaria (Bloodstained Chivalry)
                        accept(output, SystemItems.ARTIFACT_BLOODSTAINED_CHIVALRY_FLOWER);
                        accept(output, SystemItems.ARTIFACT_BLOODSTAINED_CHIVALRY_PLUME);
                        accept(output, SystemItems.ARTIFACT_BLOODSTAINED_CHIVALRY_SANDS);
                        accept(output, SystemItems.ARTIFACT_BLOODSTAINED_CHIVALRY_GOBLET);
                        accept(output, SystemItems.ARTIFACT_BLOODSTAINED_CHIVALRY_CIRCLET);

                        // Berserker
                        accept(output, SystemItems.ARTIFACT_BERSERKER_FLOWER);
                        accept(output, SystemItems.ARTIFACT_BERSERKER_PLUME);
                        accept(output, SystemItems.ARTIFACT_BERSERKER_SANDS);
                        accept(output, SystemItems.ARTIFACT_BERSERKER_GOBLET);
                        accept(output, SystemItems.ARTIFACT_BERSERKER_CIRCLET);

                        // El Exiliado (The Exile)
                        accept(output, SystemItems.ARTIFACT_THE_EXILE_FLOWER);
                        accept(output, SystemItems.ARTIFACT_THE_EXILE_PLUME);
                        accept(output, SystemItems.ARTIFACT_THE_EXILE_SANDS);
                        accept(output, SystemItems.ARTIFACT_THE_EXILE_GOBLET);
                        accept(output, SystemItems.ARTIFACT_THE_EXILE_CIRCLET);

                        // Corazón del Guerrero (Brave Heart)
                        accept(output, SystemItems.ARTIFACT_BRAVE_HEART_FLOWER);
                        accept(output, SystemItems.ARTIFACT_BRAVE_HEART_PLUME);
                        accept(output, SystemItems.ARTIFACT_BRAVE_HEART_SANDS);
                        accept(output, SystemItems.ARTIFACT_BRAVE_HEART_GOBLET);
                        accept(output, SystemItems.ARTIFACT_BRAVE_HEART_CIRCLET);

                        // --- 60 NUEVOS ARTEFACTOS CANÓNICOS (EXPANDIDOS) ---
                        for (var artHolder : SystemItems.EXPANDED_ARTIFACTS) {
                            accept(output, artHolder);
                        }
                    })
                    .build()
    );

    // ==========================================
    // 2. PESTAÑA: SISTEMA, GACHA Y RELIQUIAS
    // ==========================================
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SYSTEM_TAB = CREATIVE_MODE_TABS.register("awakening_system", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.awakening_system.system"))
                    .icon(() -> SystemItems.INTERTWINED_FATE.isBound() ? new ItemStack(SystemItems.INTERTWINED_FATE.get()) : new ItemStack(Items.NETHER_STAR))
                    .displayItems((parameters, output) -> {
                        // --- MONEDAS Y DESEOS GACHA ---
                        accept(output, SystemItems.PRIMOGEM);
                        accept(output, SystemItems.GENESIS_CRYSTAL);
                        accept(output, SystemItems.STELLAR_JADE);
                        accept(output, SystemItems.INTERTWINED_FATE);
                        accept(output, SystemItems.ACQUAINT_FATE);
                        accept(output, SystemItems.STAR_RAIL_SPECIAL_PASS);
                        accept(output, SystemItems.MASTERLESS_STARDUST);
                        accept(output, SystemItems.MASTERLESS_STARGLITTER);
                        accept(output, SystemItems.AWAKENING_COIN);
                        accept(output, SystemItems.ROULETTE_TOKEN);
                        accept(output, SystemItems.MORA);
                        accept(output, SystemItems.MORA_POUCH);
                        accept(output, SystemItems.MORA_BUNDLE);
                        accept(output, SystemItems.GIFT_CARD_5);
                        accept(output, SystemItems.GIFT_CARD_15);
                        accept(output, SystemItems.GIFT_CARD_30);
                        accept(output, SystemItems.GIFT_CARD_50);
                        accept(output, SystemItems.GIFT_CARD_100);

                        // --- CURIOSIDADES Y CONSUMIBLES HOYOVERSE ---
                        accept(output, SystemItems.COSMIC_FRAGMENT);
                        accept(output, SystemItems.ADVENTURERS_EXPERIENCE);
                        accept(output, SystemItems.HEROS_WIT);
                        accept(output, SystemItems.CROWN_OF_INSIGHT);
                        accept(output, SystemItems.CRYSTAL_CORE);
                        accept(output, SystemItems.ENHANCEMENT_ORE);
                        accept(output, SystemItems.FINE_ENHANCEMENT_ORE);
                        accept(output, SystemItems.MYSTIC_ENHANCEMENT_ORE);
                        accept(output, SystemItems.GALACTIC_BIG_LOTTO);
                        accept(output, SystemItems.ROBE_OF_THE_BEAUTY);
                        accept(output, SystemItems.PRAISE_OF_LOFTY_MORALITY);
                        accept(output, SystemItems.ORIGINAL_RESIN);
                        accept(output, SystemItems.CONDENSED_RESIN);
                        accept(output, SystemItems.TRANSIENT_RESIN);
                        accept(output, SystemItems.VISIONS_MENU);
                        accept(output, SystemItems.SLIME_CONDENSATE);
                        accept(output, SystemItems.SLIME_SECRETIONS);
                        accept(output, SystemItems.SLIME_CONCENTRATE);
                        accept(output, SystemItems.DAMAGED_MASK);
                        accept(output, SystemItems.STAINED_MASK);
                        accept(output, SystemItems.OMINOUS_MASK);
                        accept(output, SystemItems.HEAVY_HORN);
                        accept(output, SystemItems.TEYVAT_JAR);
                        accept(output, SystemItems.WANDERER_POUCH);
                        accept(output, SystemItems.ADVENTURER_POUCH);
                        accept(output, SystemItems.OUTLANDER_POUCH);
                        accept(output, SystemItems.EMERGENCY_POUCH);
                        accept(output, SystemItems.OMNISCIENCE_POUCH);
                        accept(output, SystemItems.DOCTORS_ROBE);
                        accept(output, SystemItems.GACHA_CAPSULE);
                        accept(output, SystemItems.SLOT_MACHINE);
                        accept(output, SystemItems.GACHA_MACHINE);


                        // --- RELIQUIAS SHONEN Y VISIONES ---
                        accept(output, SystemItems.LIGHT_CONE_MILKY_WAY);
                        accept(output, SystemItems.LIGHT_CONE_PASSING_SHORE);
                        accept(output, SystemItems.LIGHT_CONE_PATIENCE);
                        accept(output, SystemItems.LIGHT_CONE_DREAMS_REST);
                        accept(output, SystemItems.LIGHT_CONE_BEFORE_DAWN);
                        accept(output, SystemItems.COMMAND_SPELL);
                        accept(output, SystemItems.ELUCIDATOR);
                        accept(output, SystemItems.DARK_REPULSER);
                        accept(output, SystemItems.LAMBENT_LIGHT);
                        accept(output, SystemItems.VISION_PYRO);
                        accept(output, SystemItems.VISION_ELECTRO);
                        accept(output, SystemItems.VISION_CRYO);
                        accept(output, SystemItems.VISION_ANEMO);
                        accept(output, SystemItems.VISION_HYDRO);
                        accept(output, SystemItems.VISION_GEO);
                        accept(output, SystemItems.VISION_DENDRO);

                        // --- ENGAÑOS DE LOS FATUI (DELUSIONS) ---
                        accept(output, SystemItems.PYRO_DELUSION);
                        accept(output, SystemItems.HYDRO_DELUSION);
                        accept(output, SystemItems.CRYO_DELUSION);
                        accept(output, SystemItems.ELECTRO_DELUSION);
                        accept(output, SystemItems.ANEMO_DELUSION);
                        accept(output, SystemItems.GEO_DELUSION);
                        accept(output, SystemItems.DENDRO_DELUSION);
                        accept(output, SystemItems.BLANK_DELUSION);
                        accept(output, SystemItems.DELUSION_FRAGMENT);

                        // --- SIGILOS ELEMENTALES DE TEYVAT ---
                        accept(output, SystemItems.ANEMO_SIGIL);
                        accept(output, SystemItems.GEO_SIGIL);
                        accept(output, SystemItems.ELECTRO_SIGIL);
                        accept(output, SystemItems.DENDRO_SIGIL);
                        accept(output, SystemItems.GUARDIANS_SIGIL);
                        accept(output, SystemItems.LIGHT_REALM_SIGIL);

                        // --- LLAVES DE SANTUARIOS REGIONALES ---
                        accept(output, SystemItems.ANEMO_SHRINE_KEY);
                        accept(output, SystemItems.GEO_SHRINE_KEY);
                        accept(output, SystemItems.ELECTRO_SHRINE_KEY);
                        accept(output, SystemItems.DENDRO_SHRINE_KEY);

                        // --- ÓCULOS Y PIEDRAS DE RESONANCIA ---
                        accept(output, SystemItems.ANEMOCULUS);
                        accept(output, SystemItems.GEOCULUS);
                        accept(output, SystemItems.ELECTROCULUS);
                        accept(output, SystemItems.DENDROCULUS);
                        accept(output, SystemItems.ANEMOCULUS_RESONANCE_STONE);
                        accept(output, SystemItems.GEOCULUS_RESONANCE_STONE);
                        accept(output, SystemItems.ELECTROCULUS_RESONANCE_STONE);
                        accept(output, SystemItems.DENDROCULUS_RESONANCE_STONE);

                        // --- PROTOTIPOS DE FORJA (BILLETS) ---
                        accept(output, SystemItems.NORTHLANDER_SWORD_BILLET);
                        accept(output, SystemItems.NORTHLANDER_BOW_BILLET);
                        accept(output, SystemItems.NORTHLANDER_CLAYMORE_BILLET);
                        accept(output, SystemItems.NORTHLANDER_CATALYST_BILLET);
                        accept(output, SystemItems.NORTHLANDER_POLEARM_BILLET);
                        accept(output, SystemItems.MIDLANDER_SWORD_BILLET);
                        accept(output, SystemItems.MIDLANDER_BOW_BILLET);
                        accept(output, SystemItems.MIDLANDER_CLAYMORE_BILLET);
                        accept(output, SystemItems.MIDLANDER_CATALYST_BILLET);
                        accept(output, SystemItems.MIDLANDER_POLEARM_BILLET);

                        // --- PIEDRAS DE RUNA ---
                        accept(output, SystemItems.RUNE_STONE_RULERS_AUTHORITY);
                        accept(output, SystemItems.RUNE_STONE_DRAGON_EYES);
                        accept(output, SystemItems.RUNE_STONE_BLOODTHIRST);
                        accept(output, SystemItems.RUNE_STONE_SHADOW_STEP);
                        accept(output, SystemItems.RUNE_STONE_FOURTH_WALL);
                        accept(output, SystemItems.RUNE_STONE_IRON_BODY);

                        // --- RELIQUIAS Y JOYAS SUPREMAS ---
                        accept(output, SystemItems.DEMON_MONARCH_RING);
                        accept(output, SystemItems.ASRAHAN_MANA_CHALICE);
                        accept(output, SystemItems.KING_SLAYER_NECKLACE);
                        accept(output, SystemItems.PHOENIX_FEATHER);
                        accept(output, SystemItems.ELEMENTAL_DEMON_CROWN);

                        // --- LIBROS, TICKETS Y PERGAMINOS ---
                        accept(output, SystemItems.HUNTER_GUIDE);
                        accept(output, SystemItems.HUNTER_BESTIARY);
                        accept(output, SystemItems.WORN_LORE_BOOK);
                        accept(output, SystemItems.RANKER_TOWER_TICKET);
                        accept(output, SystemItems.RANKER_RETURN_SCROLL);
                        accept(output, SystemItems.ADVENTURER_MEDAL);

                        // --- DISCOS DE MÚSICA OFICIALES HOYOVERSE ---
                        accept(output, SystemItems.MUSIC_DISC_TAKE_THE_JOURNEY);
                        accept(output, SystemItems.MUSIC_DISC_SPACE_WALK);
                        accept(output, SystemItems.MUSIC_DISC_QINGCE_NIGHT);
                        accept(output, SystemItems.MUSIC_DISC_WINDRISE_NIGHT);
                        accept(output, SystemItems.MUSIC_DISC_DIVINE_FOX);
                        accept(output, SystemItems.MUSIC_DISC_WATERS_POEM);
                        accept(output, SystemItems.MUSIC_DISC_DAY_OF_HOPE);
                        accept(output, SystemItems.MUSIC_DISC_CHASING_STARS);
                        accept(output, SystemItems.MUSIC_DISC_SALTED_MOON);
                    })
                    .build()
    );

    // ==========================================
    // 3. PESTAÑA: MINERALES, BLOQUES Y FORJA
    // ==========================================
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS_TAB = CREATIVE_MODE_TABS.register("awakening_materials", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.awakening_system.materials"))
                    .icon(() -> SystemItems.ASTRAL_INGOT.isBound() ? new ItemStack(SystemItems.ASTRAL_INGOT.get()) : new ItemStack(Items.NETHERITE_INGOT))
                    .displayItems((parameters, output) -> {
                        // --- FAMILIA ABISAL ---
                        accept(output, SystemItems.RAW_ABYSSAL_ORE);
                        accept(output, SystemItems.ABYSSAL_INGOT);
                        accept(output, SystemItems.ABYSSAL_NUGGET);
                        accept(output, SystemItems.ABYSSAL_STONE);
                        accept(output, SystemItems.ABYSSAL_RESONANT_CRYSTAL);
                        accept(output, SystemItems.ABYSSAL_ORE);
                        accept(output, SystemItems.DEEPSLATE_ABYSSAL_ORE);
                        accept(output, SystemItems.ABYSSAL_BLOCK);

                        // --- PLANTILLAS DE MEJORA DE HERRERÍA ---
                        accept(output, SystemItems.ABYSSAL_UPGRADE_SMITHING_TEMPLATE);
                        accept(output, SystemItems.MITHRIL_UPGRADE_SMITHING_TEMPLATE);
                        accept(output, SystemItems.ADAMANTITE_UPGRADE_SMITHING_TEMPLATE);
                        accept(output, SystemItems.SPIRIT_JADE_UPGRADE_SMITHING_TEMPLATE);
                        accept(output, SystemItems.ASTRAL_UPGRADE_SMITHING_TEMPLATE);

                        // --- LLAVES DIMENSIONALES ---
                        accept(output, SystemItems.OVERWORLD_DUNGEON_KEY);
                        accept(output, SystemItems.NETHER_FLAME_KEY);
                        accept(output, SystemItems.END_VOID_KEY);
                        accept(output, SystemItems.TEYVAT_SHRINE_KEY);
                        accept(output, SystemItems.ISEKAI_MANA_KEY);
                        accept(output, SystemItems.MURIM_DAO_KEY);

                        // --- FAMILIA MITHRIL ---
                        accept(output, SystemItems.RAW_MITHRIL);
                        accept(output, SystemItems.MITHRIL_INGOT);
                        accept(output, SystemItems.MITHRIL_NUGGET);
                        accept(output, SystemItems.MITHRIL_ORE);
                        accept(output, SystemItems.DEEPSLATE_MITHRIL_ORE);
                        accept(output, SystemItems.MITHRIL_BLOCK);

                        // --- FAMILIA ADAMANTITE ---
                        accept(output, SystemItems.RAW_ADAMANTITE);
                        accept(output, SystemItems.ADAMANTITE_INGOT);
                        accept(output, SystemItems.ADAMANTITE_NUGGET);
                        accept(output, SystemItems.ADAMANTITE_ORE);
                        accept(output, SystemItems.ADAMANTITE_BLOCK);

                        // --- FAMILIA JADE ESPIRITUAL ---
                        accept(output, SystemItems.RAW_SPIRIT_JADE);
                        accept(output, SystemItems.SPIRIT_JADE);
                        accept(output, SystemItems.SPIRIT_JADE_SHARD);
                        accept(output, SystemItems.SPIRIT_JADE_ORE);
                        accept(output, SystemItems.SPIRIT_JADE_BLOCK);

                        // --- FAMILIA ASTRAL Y BLOQUES SUPREMOS ---
                        accept(output, SystemItems.RAW_ASTRAL);
                        accept(output, SystemItems.ASTRAL_INGOT);
                        accept(output, SystemItems.ASTRAL_NUGGET);
                        accept(output, SystemItems.ASTRAL_ORE);
                        accept(output, SystemItems.ASTRAL_BLOCK);
                        accept(output, SystemItems.ABYSSAL_VOID_STONE);
                        accept(output, SystemItems.CELESTIAL_WAYPOINT);

                        // --- MINERALES Y GEMAS ELEMENTALES DE TEYVAT ---
                        accept(output, SystemItems.PRIMOGEM_ORE);
                        accept(output, SystemItems.DEEPSLATE_PRIMOGEM_ORE);
                        accept(output, SystemItems.WHITE_IRON_ORE);
                        accept(output, SystemItems.WHITE_IRON_CHUNK);
                        accept(output, SystemItems.MORA_BLOCK);

                        // Pyro (Agnidus Agate)
                        accept(output, SystemItems.AGNIDUS_AGATE_ORE);
                        accept(output, SystemItems.AGNIDUS_AGATE_SLIVER);
                        accept(output, SystemItems.AGNIDUS_AGATE_FRAGMENT);
                        accept(output, SystemItems.AGNIDUS_AGATE_CHUNK);
                        accept(output, SystemItems.AGNIDUS_AGATE_GEMSTONE);

                        // Cryo (Shivada Jade)
                        accept(output, SystemItems.SHIVADA_JADE_ORE);
                        accept(output, SystemItems.SHIVADA_JADE_SLIVER);
                        accept(output, SystemItems.SHIVADA_JADE_FRAGMENT);
                        accept(output, SystemItems.SHIVADA_JADE_CHUNK);
                        accept(output, SystemItems.SHIVADA_JADE_GEMSTONE);

                        // Hydro (Varunada Lazurite)
                        accept(output, SystemItems.VARUNADA_LAZURITE_ORE);
                        accept(output, SystemItems.VARUNADA_LAZURITE_SLIVER);
                        accept(output, SystemItems.VARUNADA_LAZURITE_FRAGMENT);
                        accept(output, SystemItems.VARUNADA_LAZURITE_CHUNK);
                        accept(output, SystemItems.VARUNADA_LAZURITE_GEMSTONE);

                        // Electro (Vajrada Amethyst)
                        accept(output, SystemItems.VAJRADA_AMETHYST_ORE);
                        accept(output, SystemItems.VAJRADA_AMETHYST_SLIVER);
                        accept(output, SystemItems.VAJRADA_AMETHYST_FRAGMENT);
                        accept(output, SystemItems.VAJRADA_AMETHYST_CHUNK);
                        accept(output, SystemItems.VAJRADA_AMETHYST_GEMSTONE);

                        // Anemo (Vayuda Turquoise)
                        accept(output, SystemItems.VAYUDA_TURQUOISE_ORE);
                        accept(output, SystemItems.VAYUDA_TURQUOISE_SLIVER);
                        accept(output, SystemItems.VAYUDA_TURQUOISE_FRAGMENT);
                        accept(output, SystemItems.VAYUDA_TURQUOISE_CHUNK);
                        accept(output, SystemItems.VAYUDA_TURQUOISE_GEMSTONE);

                        // Geo (Prithiva Topaz)
                        accept(output, SystemItems.PRITHIVA_TOPAZ_ORE);
                        accept(output, SystemItems.PRITHIVA_TOPAZ_SLIVER);
                        accept(output, SystemItems.PRITHIVA_TOPAZ_FRAGMENT);
                        accept(output, SystemItems.PRITHIVA_TOPAZ_CHUNK);
                        accept(output, SystemItems.PRITHIVA_TOPAZ_GEMSTONE);

                        // Dendro (Nagadus Emerald)
                        accept(output, SystemItems.NAGADUS_EMERALD_ORE);
                        accept(output, SystemItems.NAGADUS_EMERALD_SLIVER);
                        accept(output, SystemItems.NAGADUS_EMERALD_FRAGMENT);
                        accept(output, SystemItems.NAGADUS_EMERALD_CHUNK);
                        accept(output, SystemItems.NAGADUS_EMERALD_GEMSTONE);

                        // Materiales de Mejora y Núcleos
                        accept(output, SystemItems.CRYSTAL_CORE);
                        accept(output, SystemItems.ENHANCEMENT_ORE);
                        accept(output, SystemItems.FINE_ENHANCEMENT_ORE);
                        accept(output, SystemItems.MYSTIC_ENHANCEMENT_ORE);
                        accept(output, SystemItems.MASTERLESS_STARDUST);
                        accept(output, SystemItems.MASTERLESS_STARGLITTER);

                        // --- MATERIALES DE ASCENSIÓN DE HIPÓSTASIS (GENSHIN IMPACT) ---
                        accept(output, SystemItems.HURRICANE_SEED);
                        accept(output, SystemItems.LIGHTNING_PRISM);
                        accept(output, SystemItems.BASALT_PILLAR);
                        accept(output, SystemItems.CRYSTALLINE_BLOOM);
                        accept(output, SystemItems.SMOLDERING_PEARL);
                        accept(output, SystemItems.DEW_OF_REPUDIATION);

                        // --- MATERIALES DE AUTÓMATAS DE RUINAS (KHAENRI'AH) ---
                        accept(output, SystemItems.CHAOS_DEVICE);
                        accept(output, SystemItems.CHAOS_CIRCUIT);
                        accept(output, SystemItems.CHAOS_CORE);

                        // --- MATERIALES DE REGISVID (GENSHIN IMPACT) ---
                        accept(output, SystemItems.HOARFROST_CORE);
                        accept(output, SystemItems.EVERFLAME_SEED);

                        // --- MATERIALES DE JEFES SEMANALES Y MUNDIALES (GENSHIN IMPACT) ---
                        accept(output, SystemItems.MUDRA_OF_THE_MALEFIC_GENERAL);
                        // accept(output, SystemItems.RIFTBORN_REGALIA);
                        // accept(output, SystemItems.GOLDEN_WOLFLORD_CORE);

                        // --- MATERIALES DE MITACHURL Y LAWACHURL ---
                        accept(output, SystemItems.HEAVY_HORN);
                        accept(output, SystemItems.BLACK_BRONZE_HORN);
                        accept(output, SystemItems.BLACK_CRYSTAL_HORN);

                        // --- BLOQUES DE PODER ELEMENTAL DE HIPÓSTASIS ---
                        accept(output, SystemItems.ANEMO_HYPOSTASIS_BLOCK);
                        accept(output, SystemItems.ELECTRO_HYPOSTASIS_BLOCK);
                        accept(output, SystemItems.GEO_HYPOSTASIS_BLOCK);
                        accept(output, SystemItems.CRYO_HYPOSTASIS_BLOCK);
                        accept(output, SystemItems.PYRO_HYPOSTASIS_BLOCK);
                        accept(output, SystemItems.HYDRO_HYPOSTASIS_BLOCK);

                        // --- MONUMENTOS ELEMENTALES DE TEYVAT ---
                        accept(output, SystemItems.ANEMO_MONUMENT);
                        accept(output, SystemItems.GEO_MONUMENT);
                        accept(output, SystemItems.ELECTRO_MONUMENT);
                        accept(output, SystemItems.DENDRO_MONUMENT);
                        accept(output, SystemItems.HYDRO_MONUMENT);
                        accept(output, SystemItems.PYRO_MONUMENT);
                        accept(output, SystemItems.CRYO_MONUMENT);
                        accept(output, SystemItems.BLANK_MONUMENT);

                        // --- HORNOS MÁGICOS ---
                        accept(output, SystemItems.ABYSSAL_FURNACE);
                        accept(output, SystemItems.MITHRIL_FURNACE);
                        accept(output, SystemItems.ADAMANTITE_FURNACE);
                        accept(output, SystemItems.SPIRIT_JADE_FURNACE);
                        accept(output, SystemItems.ASTRAL_FURNACE);

                        // --- PLANTILLAS DE HERRERÍA (SMITHING TEMPLATES) ---
                        accept(output, SystemItems.UPGRADE_NETHERITE_TO_ABYSSAL);
                        accept(output, SystemItems.UPGRADE_NETHERITE_TO_MITHRIL);
                        accept(output, SystemItems.UPGRADE_MITHRIL_TO_ADAMANTITE);
                        accept(output, SystemItems.UPGRADE_ADAMANTITE_TO_SPIRIT_JADE);
                        accept(output, SystemItems.UPGRADE_SPIRIT_JADE_TO_ASTRAL);

                        // --- HERRAMIENTAS DE MINERÍA Y UTILIDAD ---
                        accept(output, SystemItems.ABYSSAL_PICKAXE);
                        accept(output, SystemItems.ABYSSAL_SHOVEL);
                        accept(output, SystemItems.ABYSSAL_HOE);

                        accept(output, SystemItems.MITHRIL_PICKAXE);
                        accept(output, SystemItems.MITHRIL_SHOVEL);
                        accept(output, SystemItems.MITHRIL_HOE);

                        accept(output, SystemItems.ADAMANTITE_PICKAXE);
                        accept(output, SystemItems.ADAMANTITE_SHOVEL);
                        accept(output, SystemItems.ADAMANTITE_HOE);

                        accept(output, SystemItems.SPIRIT_JADE_PICKAXE);
                        accept(output, SystemItems.SPIRIT_JADE_SHOVEL);
                        accept(output, SystemItems.SPIRIT_JADE_HOE);

                        accept(output, SystemItems.ASTRAL_PICKAXE);
                        accept(output, SystemItems.ASTRAL_SHOVEL);
                        accept(output, SystemItems.ASTRAL_HOE);

                        // --- HERRAMIENTAS PRIMORDIALES DE TEYVAT ---
                        accept(output, SystemItems.PRIMOGEM_PICKAXE);

                        // --- MADERAS Y ÁRBOLES CANÓNICOS DE TEYVAT ---
                        accept(output, SystemItems.CUIHUA_LOG);
                        accept(output, SystemItems.CUIHUA_LEAVES);
                        accept(output, SystemItems.CUIHUA_SAPLING);
                        accept(output, SystemItems.CUIHUA_PLANKS);

                        accept(output, SystemItems.ADHIGAMA_LOG);
                        accept(output, SystemItems.ADHIGAMA_LEAVES);
                        accept(output, SystemItems.ADHIGAMA_SAPLING);
                        accept(output, SystemItems.ADHIGAMA_PLANKS);

                        accept(output, SystemItems.SANDBEARER_LOG);
                        accept(output, SystemItems.SANDBEARER_LEAVES);
                        accept(output, SystemItems.SANDBEARER_SAPLING);
                        accept(output, SystemItems.SANDBEARER_PLANKS);

                        accept(output, SystemItems.ARALIA_WOOD_ITEM);
                        accept(output, SystemItems.ARALIA_LEAVES_ITEM);
                        accept(output, SystemItems.ARALIA_SAPLING_ITEM);
                        accept(output, SystemItems.ARALIA_PLANKS_ITEM);

                        accept(output, SystemItems.OTOGI_WOOD_ITEM);
                        accept(output, SystemItems.OTOGI_LEAVES_ITEM);
                        accept(output, SystemItems.OTOGI_SAPLING_ITEM);
                        accept(output, SystemItems.OTOGI_PLANKS_ITEM);

                        accept(output, SystemItems.YUMEMIRU_WOOD_ITEM);
                        accept(output, SystemItems.YUMEMIRU_LEAVES_ITEM);
                        accept(output, SystemItems.YUMEMIRU_SAPLING_ITEM);
                        accept(output, SystemItems.YUMEMIRU_PLANKS_ITEM);

                        accept(output, SystemItems.MAPLE_WOOD_ITEM);
                        accept(output, SystemItems.MAPLE_LEAVES_ITEM);
                        accept(output, SystemItems.MAPLE_SAPLING_ITEM);
                        accept(output, SystemItems.MAPLE_PLANKS_ITEM);

                        // --- MADERAS Y ÁRBOLES CANÓNICOS DE ISEKAI ---
                        accept(output, SystemItems.MANA_WOOD_LOG);
                        accept(output, SystemItems.MANA_WOOD_LEAVES);
                        accept(output, SystemItems.MANA_WOOD_SAPLING);
                        accept(output, SystemItems.MANA_WOOD_PLANKS);

                        accept(output, SystemItems.ELDORIA_LOG);
                        accept(output, SystemItems.ELDORIA_LEAVES);
                        accept(output, SystemItems.ELDORIA_SAPLING);
                        accept(output, SystemItems.ELDORIA_PLANKS);

                        // --- MADERAS Y ÁRBOLES CANÓNICOS DE MURIM (CULTIVO) ---
                        accept(output, SystemItems.SPIRIT_PLUM_LOG);
                        accept(output, SystemItems.SPIRIT_PLUM_LEAVES);
                        accept(output, SystemItems.SPIRIT_PLUM_SAPLING);
                        accept(output, SystemItems.SPIRIT_PLUM_PLANKS);
                        accept(output, SystemItems.SPIRIT_PLUM_STAIRS);
                        accept(output, SystemItems.SPIRIT_PLUM_SLAB);
                        accept(output, SystemItems.SPIRIT_PLUM_FENCE);
                        accept(output, SystemItems.SPIRIT_PLUM_FENCE_GATE);
                        accept(output, SystemItems.SPIRIT_PLUM_DOOR);
                        accept(output, SystemItems.SPIRIT_PLUM_TRAPDOOR);

                        accept(output, SystemItems.CELESTIAL_PINE_LOG);
                        accept(output, SystemItems.CELESTIAL_PINE_LEAVES);
                        accept(output, SystemItems.CELESTIAL_PINE_SAPLING);
                        accept(output, SystemItems.CELESTIAL_PINE_PLANKS);
                        accept(output, SystemItems.CELESTIAL_PINE_STAIRS);
                        accept(output, SystemItems.CELESTIAL_PINE_SLAB);
                        accept(output, SystemItems.CELESTIAL_PINE_FENCE);
                        accept(output, SystemItems.CELESTIAL_PINE_FENCE_GATE);
                        accept(output, SystemItems.CELESTIAL_PINE_DOOR);
                        accept(output, SystemItems.CELESTIAL_PINE_TRAPDOOR);

                        accept(output, SystemItems.IMMORTALITY_PEACH_LEAVES);

                        // --- MINERALES Y GEMAS ADICIONALES DE TEYVAT ---
                        accept(output, SystemItems.COR_LAPIS_BLOCK_ITEM);
                        accept(output, SystemItems.COR_LAPIS_ITEM);
                        accept(output, SystemItems.NOCTILUCOUS_JADE_BLOCK_ITEM);
                        accept(output, SystemItems.NOCTILUCOUS_JADE_ITEM);
                        accept(output, SystemItems.STARSILVER_BLOCK_ITEM);
                        accept(output, SystemItems.STARSILVER_CHUNK);
                        accept(output, SystemItems.CRYSTAL_CHUNK_BLOCK_ITEM);
                        accept(output, SystemItems.CRYSTAL_CHUNK);
                        accept(output, SystemItems.MAGICAL_CRYSTAL_BLOCK_ITEM);
                        accept(output, SystemItems.MAGICAL_CRYSTAL_CHUNK);
                        accept(output, SystemItems.ELECTRO_CRYSTAL_BLOCK_ITEM);
                        accept(output, SystemItems.ELECTRO_CRYSTAL);

                        // --- SANTUARIOS DE LAS PROFUNDIDADES ---
                        accept(output, SystemItems.ANEMO_SHRINE_BLOCK_ITEM);
                        accept(output, SystemItems.GEO_SHRINE_BLOCK_ITEM);
                        accept(output, SystemItems.ELECTRO_SHRINE_BLOCK_ITEM);
                        accept(output, SystemItems.DENDRO_SHRINE_BLOCK_ITEM);

                        // --- BLOQUES DE ÓCULOS Y UTILIDAD ---
                        accept(output, SystemItems.ANEMOCULUS_BLOCK_ITEM);
                        accept(output, SystemItems.GEOCULUS_BLOCK_ITEM);
                        accept(output, SystemItems.ELECTROCULUS_BLOCK_ITEM);
                        accept(output, SystemItems.DENDROCULUS_BLOCK_ITEM);
                        accept(output, SystemItems.FISHING_BLOCK_ITEM);
                        accept(output, SystemItems.FISHING_GUILD_STATION_ITEM);
                        accept(output, SystemItems.GUILD_HUNTING_BOARD_ITEM);

                        // --- MOBILIARIO Y AMBIENTACIÓN DE TEYVAT ---
                        accept(output, SystemItems.BAMBOO_TEA_TABLE_ITEM);
                        accept(output, SystemItems.OTOGI_TABLE_ITEM);
                        accept(output, SystemItems.OTOGI_STOOL_ITEM);
                        accept(output, SystemItems.YUMEMIRU_STOOL_ITEM);
                        accept(output, SystemItems.SANDBEARER_FLOOR_LAMP_ITEM);

                        // --- SETS COMPLETOS DE MADERAS DE TEYVAT ---
                        accept(output, SystemItems.CUIHUA_STAIRS_ITEM);
                        accept(output, SystemItems.CUIHUA_SLAB_ITEM);
                        accept(output, SystemItems.CUIHUA_FENCE_ITEM);
                        accept(output, SystemItems.CUIHUA_FENCE_GATE_ITEM);
                        accept(output, SystemItems.CUIHUA_DOOR_ITEM);
                        accept(output, SystemItems.CUIHUA_TRAPDOOR_ITEM);

                        accept(output, SystemItems.SANDBEARER_STAIRS_ITEM);
                        accept(output, SystemItems.SANDBEARER_SLAB_ITEM);
                        accept(output, SystemItems.SANDBEARER_FENCE_ITEM);
                        accept(output, SystemItems.SANDBEARER_FENCE_GATE_ITEM);
                        accept(output, SystemItems.SANDBEARER_DOOR_ITEM);
                        accept(output, SystemItems.SANDBEARER_TRAPDOOR_ITEM);

                        accept(output, SystemItems.ADHIGAMA_STAIRS_ITEM);
                        accept(output, SystemItems.ADHIGAMA_SLAB_ITEM);
                        accept(output, SystemItems.ADHIGAMA_FENCE_ITEM);
                        accept(output, SystemItems.ADHIGAMA_FENCE_GATE_ITEM);
                        accept(output, SystemItems.ADHIGAMA_DOOR_ITEM);
                        accept(output, SystemItems.ADHIGAMA_TRAPDOOR_ITEM);

                        accept(output, SystemItems.ARALIA_WOOD_ITEM);
                        accept(output, SystemItems.ARALIA_LEAVES_ITEM);
                        accept(output, SystemItems.ARALIA_PLANKS_ITEM);
                        accept(output, SystemItems.ARALIA_STAIRS_ITEM);
                        accept(output, SystemItems.ARALIA_SLAB_ITEM);
                        accept(output, SystemItems.ARALIA_FENCE_ITEM);
                        accept(output, SystemItems.ARALIA_FENCE_GATE_ITEM);
                        accept(output, SystemItems.ARALIA_DOOR_ITEM);
                        accept(output, SystemItems.ARALIA_TRAPDOOR_ITEM);

                        accept(output, SystemItems.YUMEMIRU_WOOD_ITEM);
                        accept(output, SystemItems.YUMEMIRU_LEAVES_ITEM);
                        accept(output, SystemItems.YUMEMIRU_PLANKS_ITEM);
                        accept(output, SystemItems.YUMEMIRU_STAIRS_ITEM);
                        accept(output, SystemItems.YUMEMIRU_SLAB_ITEM);
                        accept(output, SystemItems.YUMEMIRU_FENCE_ITEM);
                        accept(output, SystemItems.YUMEMIRU_FENCE_GATE_ITEM);
                        accept(output, SystemItems.YUMEMIRU_DOOR_ITEM);
                        accept(output, SystemItems.YUMEMIRU_TRAPDOOR_ITEM);

                        accept(output, SystemItems.OTOGI_WOOD_ITEM);
                        accept(output, SystemItems.OTOGI_LEAVES_ITEM);
                        accept(output, SystemItems.OTOGI_PLANKS_ITEM);
                        accept(output, SystemItems.OTOGI_STAIRS_ITEM);
                        accept(output, SystemItems.OTOGI_SLAB_ITEM);
                        accept(output, SystemItems.OTOGI_FENCE_ITEM);
                        accept(output, SystemItems.OTOGI_FENCE_GATE_ITEM);
                        accept(output, SystemItems.OTOGI_DOOR_ITEM);
                        accept(output, SystemItems.OTOGI_TRAPDOOR_ITEM);

                        accept(output, SystemItems.BAMBOO_SEGMENT_ITEM);
                        accept(output, SystemItems.BAMBOO_PLANKS_ITEM);
                        accept(output, SystemItems.BAMBOO_STAIRS_ITEM);
                        accept(output, SystemItems.BAMBOO_SLAB_ITEM);
                        accept(output, SystemItems.BAMBOO_FENCE_ITEM);
                        accept(output, SystemItems.BAMBOO_FENCE_GATE_ITEM);
                        accept(output, SystemItems.BAMBOO_DOOR_ITEM);
                        accept(output, SystemItems.BAMBOO_TRAPDOOR_ITEM);

                        accept(output, SystemItems.MAPLE_WOOD_ITEM);
                        accept(output, SystemItems.MAPLE_LEAVES_ITEM);
                        accept(output, SystemItems.MAPLE_PLANKS_ITEM);
                        accept(output, SystemItems.MAPLE_STAIRS_ITEM);
                        accept(output, SystemItems.MAPLE_SLAB_ITEM);
                        accept(output, SystemItems.MAPLE_FENCE_ITEM);
                        accept(output, SystemItems.MAPLE_FENCE_GATE_ITEM);
                        accept(output, SystemItems.MAPLE_DOOR_ITEM);
                        accept(output, SystemItems.MAPLE_TRAPDOOR_ITEM);
                    })
                    .build()
    );

    // ==========================================
    // 4. PESTAÑA: ALQUIMIA, MAZMORRAS Y CRIATURAS
    // ==========================================
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAGIC_ENTITIES_TAB = CREATIVE_MODE_TABS.register("awakening_magic_entities", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.awakening_system.magic_entities"))
                    .icon(() -> SystemItems.RISING_DRAGON_PILL.isBound() ? new ItemStack(SystemItems.RISING_DRAGON_PILL.get()) : new ItemStack(Items.GOLDEN_APPLE))
                    .displayItems((parameters, output) -> {
                        // --- ESTACIONES CELESTIALES Y NOVEDADES ---
                        accept(output, SystemItems.COMPANION_ASCENSION_TABLE);

                        // --- ALQUIMIA Y PÍLDORAS MURIM ---
                        accept(output, SystemItems.MURIM_ALCHEMY_CAULDRON);
                        accept(output, SystemItems.RISING_DRAGON_PILL);
                        accept(output, SystemItems.MARROW_CLEANSING_PILL);
                        accept(output, SystemItems.SOUL_TRANSFORMATION_PILL);
                        accept(output, SystemItems.REALM_BREAKTHROUGH_PILL);
                        accept(output, SystemItems.BLACK_TORTOISE_PILL);
                        accept(output, SystemItems.TRIBULATION_SHIELD_PILL);
                        accept(output, SystemItems.NINE_REVOLUTIONS_PILL);
                        accept(output, SystemItems.CRIMSON_PHOENIX_PILL);
                        accept(output, SystemItems.WHITE_TIGER_PILL);
                        accept(output, SystemItems.HEAVENLY_ENLIGHTENMENT_PILL);
                        accept(output, SystemItems.FOUNDATION_BUILDING_PILL);
                        accept(output, SystemItems.PRIMORDIAL_REBIRTH_PILL);
                        accept(output, SystemItems.WU_XING_MANUAL);
                        accept(output, SystemItems.MURIM_ASCENSION_TALISMAN);
                        accept(output, SystemItems.TAOIST_TALISMAN);

                        // --- BOLSAS DE ALMACENAMIENTO GIGANTE (RELIQUIAS DE VIAJE) ---
                        accept(output, SystemItems.WANDERER_POUCH);
                        accept(output, SystemItems.ADVENTURER_POUCH);
                        accept(output, SystemItems.OUTLANDER_POUCH);
                        accept(output, SystemItems.EMERGENCY_POUCH);
                        accept(output, SystemItems.OMNISCIENCE_POUCH);

                        // --- HIERBAS Y PLANTAS MEDICINALES SAGRADAS ---
                        accept(output, SystemItems.THOUSAND_YEAR_GINSENG);
                        accept(output, SystemItems.WILD_GINSENG);
                        accept(output, SystemItems.SNOW_LOTUS);
                        accept(output, SystemItems.MANDRAKE_ROOT);
                        accept(output, SystemItems.YGGDRASIL_HEART_SEED);

                        // --- ALIMENTOS Y CARNES DE MANÁ ---
                        accept(output, SystemItems.MANA_BEEF);
                        accept(output, SystemItems.COOKED_MANA_BEEF);
                        accept(output, SystemItems.MANA_PORKCHOP);
                        accept(output, SystemItems.COOKED_MANA_PORKCHOP);
                        accept(output, SystemItems.MANA_MUTTON);
                        accept(output, SystemItems.COOKED_MANA_MUTTON);
                        accept(output, SystemItems.MANA_CHICKEN_MEAT);
                        accept(output, SystemItems.COOKED_MANA_CHICKEN_MEAT);
                        accept(output, SystemItems.MANA_MILK_BUCKET);

                        // --- PLATOS Y ALIMENTOS DE TEYVAT ---
                        accept(output, SystemItems.ADEPTUS_TEMPTATION);
                        accept(output, SystemItems.SWEET_MADAME);
                        accept(output, SystemItems.ALMOND_TOFU);
                        accept(output, SystemItems.SUNSETTIA);
                        accept(output, SystemItems.GRILLED_TIGER_FISH);
                        accept(output, SystemItems.CALLA_LILY_SEAFOOD_SOUP);
                        accept(output, SystemItems.FRIED_RADISH_BALLS);
                        accept(output, SystemItems.OMINOUS_DISH);
                        accept(output, SystemItems.RADISH);

                        // --- PECES Y PESCA DE TEYVAT ---
                        accept(output, SystemItems.MEDAKA);
                        accept(output, SystemItems.AIZEN_MEDAKA);
                        accept(output, SystemItems.SWEET_FLOWER_MEDAKA);
                        accept(output, SystemItems.GLAZE_MEDAKA);
                        accept(output, SystemItems.CRYSTALFISH);
                        accept(output, SystemItems.VENOMSPINE_FISH);
                        accept(output, SystemItems.ABIDING_ANGELFISH);
                        accept(output, SystemItems.RAIMEI_ANGELFISH);
                        accept(output, SystemItems.FISHING_LINE_STABILIZER);
                        accept(output, SystemItems.FRUIT_PASTE_BAIT);
                        accept(output, SystemItems.REDROT_BAIT);
                        accept(output, SystemItems.FALSE_WORM_BAIT);
                        accept(output, SystemItems.FAKE_FLY_BAIT);
                        accept(output, SystemItems.GLOWGRASS_BAIT);
                        accept(output, SystemItems.SUGARDEW_BAIT);

                        // --- FRUTAS ESPIRITUALES, ASTRALES Y DE CULTIVO ---
                        accept(output, SystemItems.SPIRIT_PLUM_FRUIT);
                        accept(output, SystemItems.CELESTIAL_PINE_NUT);
                        accept(output, SystemItems.IMMORTALITY_PEACH);
                        accept(output, SystemItems.ELDORIA_FRUIT);
                        accept(output, SystemItems.MANA_FRUIT);

                        // --- DROPS DE MONSTRUOS Y MATERIALES ---
                        accept(output, SystemItems.GOBLIN_EAR);
                        accept(output, SystemItems.BEASTMAN_FUR);
                        accept(output, SystemItems.BEASTMAN_FANG);
                        accept(output, SystemItems.JADE_CARAPACE_SCALE);
                        accept(output, SystemItems.ASTRAL_CRYSTAL_SCALE);
                        accept(output, SystemItems.IMMORTAL_DRAGON_SCALE);
                        accept(output, SystemItems.CELESTIAL_DRAGON_PEARL);
                        accept(output, SystemItems.BOTTLED_MANA_PIXIE);
                        accept(output, SystemItems.CHAOS_MANA_CORE);
                        accept(output, SystemItems.WYVERN_SCALE);
                        accept(output, SystemItems.WYVERN_FANG);
                        accept(output, SystemItems.WYVERN_SADDLE);
                        accept(output, SystemItems.WILDFIRE_CROWN_FRAGMENT);

                        // --- MAZMORRAS, ALTARES Y MOBILIARIO ---
                        accept(output, SystemItems.ROULETTE_ALTAR);
                        accept(output, SystemItems.CELESTIAL_DRAGON_TRIBUTE);
                        accept(output, SystemItems.CELESTIAL_DRAGON_ALTAR);
                        accept(output, SystemItems.GODDESS_ALTAR);
                        accept(output, SystemItems.TRILUNAR_ALTAR);
                        accept(output, SystemItems.TRILUNAR_GUIDE);
                        accept(output, SystemItems.COMPANION_ASCENSION_TABLE);
                        accept(output, SystemItems.LUNAR_AUTHORITY_ARIA);
                        accept(output, SystemItems.LUNAR_AUTHORITY_SONNET);
                        accept(output, SystemItems.LUNAR_AUTHORITY_CANON);
                        accept(output, SystemItems.ELEMENTAL_ALTAR);
                        accept(output, SystemItems.ELEMENTAL_BARRIER);
                        accept(output, SystemItems.FIVE_ELEMENTS_PEDESTAL);
                        accept(output, SystemItems.BOUNTY_BOARD);
                        accept(output, SystemItems.TAVERN_CHAIR);
                        accept(output, SystemItems.CAMPFIRE_LOG_SEAT);
                        accept(output, SystemItems.TAVERN_TABLE);
                        accept(output, SystemItems.SUBWAY_RAIL_TRACK);
                        accept(output, SystemItems.SUBWAY_WIDE_RAIL_TRACK);
                        accept(output, SystemItems.SUBWAY_DIGITAL_SCREEN);
                        accept(output, SystemItems.SUBWAY_GRAB_HANDLE);
                        accept(output, SystemItems.SUBWAY_TRAIN_WHEEL);
                        accept(output, SystemItems.SUBWAY_PASSENGER_SEAT);
                        accept(output, SystemItems.SUBWAY_TRAIN_CONTROLLER);
                        accept(output, SystemItems.SUBWAY_STAINLESS_STEEL);
                        accept(output, SystemItems.SUBWAY_DOOR);
                        accept(output, SystemItems.ARCHITECT_DRAFTING_TABLE);
                        accept(output, SystemItems.DIMENSIONAL_RIFT_STAND);
                        accept(output, SystemItems.LOCKED_TREASURE_CHEST);
                        accept(output, SystemItems.LOCKED_CHEST_OVERWORLD);
                        accept(output, SystemItems.LOCKED_CHEST_NETHER);
                        accept(output, SystemItems.LOCKED_CHEST_END);
                        accept(output, SystemItems.LOCKED_CHEST_TEYVAT);
                        accept(output, SystemItems.LOCKED_CHEST_ISEKAI);
                        accept(output, SystemItems.LOCKED_CHEST_MURIM);

                        // --- LLAVES DE MAZMORRA ---
                        accept(output, SystemItems.DUNGEON_KEY);
                        accept(output, SystemItems.YGGDRASIL_TREANT_KEY);
                        accept(output, SystemItems.JADE_ANCESTRAL_KEY);
                        accept(output, SystemItems.ELEMENTAL_CASTLE_KEY);
                        accept(output, SystemItems.TEYVAT_SHRINE_KEY);

                        // --- NIDOS Y HUEVOS DE DRAGÓN Y WYVERN ---
                        accept(output, SystemItems.DRAGON_NEST);
                        accept(output, SystemItems.ANCIENT_DRAGON_EGG_FIRE);
                        accept(output, SystemItems.ANCIENT_DRAGON_EGG_ICE);
                        accept(output, SystemItems.ANCIENT_DRAGON_EGG_FOREST);
                        accept(output, SystemItems.ANCIENT_DRAGON_EGG_SHADOW);
                        accept(output, SystemItems.ANCIENT_DRAGON_EGG_LIGHTNING);
                        accept(output, SystemItems.ANCIENT_DRAGON_EGG_CELESTIAL);
                        accept(output, SystemItems.WYVERN_EGG);

                        // --- HUEVOS DE GENERACIÓN (SPAWN EGGS) ---
                        accept(output, SystemItems.PAIMON_SPAWN_EGG);
                        accept(output, SystemItems.ANT_SOLDIER_SPAWN_EGG);
                        accept(output, SystemItems.ABYSSAL_BEHOLDER_SPAWN_EGG);
                        accept(output, SystemItems.MANA_PIXIE_SPAWN_EGG);
                        accept(output, SystemItems.SCREAMING_MANDRAKE_SPAWN_EGG);
                        accept(output, SystemItems.CELESTIAL_DRAGON_SPAWN_EGG);
                        accept(output, SystemItems.ANCIENT_CELESTIAL_DRAGON_SPAWN_EGG);
                        accept(output, SystemItems.ANCIENT_DRAGON_SPAWN_EGG);
                        accept(output, SystemItems.ANCIENT_DRAGON_ICE_SPAWN_EGG);
                        accept(output, SystemItems.ANCIENT_DRAGON_FOREST_SPAWN_EGG);
                        accept(output, SystemItems.ANCIENT_DRAGON_SHADOW_SPAWN_EGG);
                        accept(output, SystemItems.ANCIENT_DRAGON_LIGHTNING_SPAWN_EGG);
                        accept(output, SystemItems.ANCIENT_DRAGON_CELESTIAL_SPAWN_EGG);
                        accept(output, SystemItems.ASTRAL_DRAGON_SPAWN_EGG);
                        accept(output, SystemItems.WYVERN_SPAWN_EGG);
                        accept(output, SystemItems.JADE_GUARDIAN_SPAWN_EGG);
                        accept(output, SystemItems.ELEMENTAL_DEMON_KING_SPAWN_EGG);
                        accept(output, SystemItems.ELEMENTAL_LORD_SPAWN_EGG);
                        accept(output, SystemItems.GOBLIN_SCOUT_SPAWN_EGG);
                        accept(output, SystemItems.GOBLIN_WARRIOR_SPAWN_EGG);
                        accept(output, SystemItems.GOBLIN_SHAMAN_SPAWN_EGG);
                        accept(output, SystemItems.BEASTMAN_WARRIOR_SPAWN_EGG);
                        accept(output, SystemItems.JIANGSHI_SPAWN_EGG);
                        accept(output, SystemItems.DEMONIC_CULTIVATOR_SPAWN_EGG);
                        accept(output, SystemItems.JADE_ARMORED_BEAST_SPAWN_EGG);
                        accept(output, SystemItems.MOUNT_HUA_DISCIPLE_SPAWN_EGG);
                        accept(output, SystemItems.MANA_COW_SPAWN_EGG);
                        accept(output, SystemItems.MANA_PIG_SPAWN_EGG);
                        accept(output, SystemItems.MANA_SHEEP_SPAWN_EGG);
                        accept(output, SystemItems.MANA_CHICKEN_SPAWN_EGG);
                        accept(output, SystemItems.SHADOW_INFANTRY_SPAWN_EGG);
                        accept(output, SystemItems.ABYSSAL_ORC_SPAWN_EGG);
                        accept(output, SystemItems.SHADOW_MONARCH_BOSS_SPAWN_EGG);
                        accept(output, SystemItems.ABYSSAL_NECROMANCER_SPAWN_EGG);
                        accept(output, SystemItems.FROST_ELF_ASSASSIN_SPAWN_EGG);
                        accept(output, SystemItems.ISEKAI_SLIME_SPAWN_EGG);
                        accept(output, SystemItems.GENSHIN_SLIME_SPAWN_EGG);
                        accept(output, SystemItems.HILICHURL_SPAWN_EGG);
                        accept(output, SystemItems.MITACHURL_SPAWN_EGG);
                        accept(output, SystemItems.SPIRIT_FOX_SPAWN_EGG);
                        accept(output, SystemItems.SHADOW_DIREWOLF_SPAWN_EGG);
                        accept(output, SystemItems.WHITE_THUNDER_TIGER_SPAWN_EGG);
                        accept(output, SystemItems.CLOUD_CRANE_SPAWN_EGG);
                        accept(output, SystemItems.DOKKAEBI_SPAWN_EGG);
                        accept(output, SystemItems.IGRIS_BOSS_SPAWN_EGG);
                        accept(output, SystemItems.BARAN_BOSS_SPAWN_EGG);
                        accept(output, SystemItems.BLOOD_SECT_DISCIPLE_SPAWN_EGG);
                        accept(output, SystemItems.BLOOD_PATRIARCH_BOSS_SPAWN_EGG);
                        accept(output, SystemItems.YGGDRASIL_TREANT_BOSS_SPAWN_EGG);
                        accept(output, SystemItems.ANEMO_HYPOSTASIS_SPAWN_EGG);
                        accept(output, SystemItems.ELECTRO_HYPOSTASIS_SPAWN_EGG);
                        accept(output, SystemItems.GEO_HYPOSTASIS_SPAWN_EGG);
                        accept(output, SystemItems.CRYO_HYPOSTASIS_SPAWN_EGG);
                        accept(output, SystemItems.PYRO_HYPOSTASIS_SPAWN_EGG);
                        accept(output, SystemItems.HYDRO_HYPOSTASIS_SPAWN_EGG);
                        accept(output, SystemItems.RUIN_GUARD_SPAWN_EGG);
                        accept(output, SystemItems.RUIN_HUNTER_SPAWN_EGG);
                        accept(output, SystemItems.STONEHIDE_LAWACHURL_SPAWN_EGG);
                        accept(output, SystemItems.CRYO_REGISVINE_SPAWN_EGG);
                        accept(output, SystemItems.WILDFIRE_SPAWN_EGG);
                        accept(output, SystemItems.LUNAR_SHADOW_SPAWN_EGG);
                        accept(output, SystemItems.LUNAR_GUARDIAN_SPAWN_EGG);
                        accept(output, SystemItems.GLARE_SPAWN_EGG);
                        accept(output, SystemItems.CHILDE_BOSS_SPAWN_EGG);
                        accept(output, SystemItems.AZHDAHA_BOSS_SPAWN_EGG);
                        accept(output, SystemItems.RAIDEN_SHOGUN_BOSS_SPAWN_EGG);
                        // accept(output, SystemItems.GOLDEN_WOLFLORD_BOSS_SPAWN_EGG);

                        // --- COMPAÑEROS INVOCABLES (PACTOS DE INVOCACIÓN) ---
                        accept(output, SystemItems.COMPANION_CONTRACT);
                        for (com.soybe.awakeningsystem.entity.CompanionProfile profile : com.soybe.awakeningsystem.entity.CompanionProfile.values()) {
                            output.accept(com.soybe.awakeningsystem.item.CompanionContractItem.createContract(profile));
                        }
                    })
                    .build()
    );

    // ==========================================
    // 5. PESTAÑA: PELUCHES Y COLECCIONABLES DE TEYVAT
    // ==========================================
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PLUSHIES_TAB = CREATIVE_MODE_TABS.register("awakening_plushies", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.awakening_system.plushies"))
                    .icon(() -> {
                        var paimon = SystemItems.PLUSHIE_ITEMS.get("paimonplushie");
                        return (paimon != null && paimon.isBound()) ? new ItemStack(paimon.get()) : new ItemStack(Items.EMERALD);
                    })
                    .displayItems((parameters, output) -> {
                        accept(output, SystemItems.PLUSHIES_LOOTBOX);
                        for (var itemHolder : SystemItems.PLUSHIE_ITEMS.values()) {
                            accept(output, itemHolder);
                        }
                    })
                    .build()
    );

    private static void accept(CreativeModeTab.Output output, DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.Item> holder) {
        if (holder != null && holder.isBound()) {
            output.accept(holder.get());
        }
    }

    private static void accept(CreativeModeTab.Output output, net.neoforged.neoforge.registries.DeferredBlock<?> holder) {
        if (holder != null && holder.isBound()) {
            output.accept(holder.get());
        }
    }
}
