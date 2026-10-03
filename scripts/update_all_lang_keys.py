import json, os

lang_dir = 'src/main/resources/assets/awakening_system/lang'

new_keys_en = {
    # Shadow Monarch Boss
    "awakening_system.boss.shadow_monarch.phase2_taunt": "§8§l[ABYSSAL MONARCH] §d§lDid you think you had cornered Death itself? KNEEL BEFORE THE TRUE SOVEREIGN!",
    "awakening_system.boss.shadow_monarch.domain_taunt": "§8§l[ABYSSAL MONARCH] §5This realm belongs to me! Let darkness devour your souls!",
    "awakening_system.boss.ashborn.defeat_quote": "§8§l[ASHBORN] §7You have proven worthy... The shadow authority and legion will now answer your call.",
    "awakening_system.boss.antares.defeat_quote": "§4§l[ANTARES] §cCurse you, hunter...! The flames of destruction and the dragon's breath will never extinguish...",

    # Elemental Bosses
    "awakening_system.castles.demon_king_defeated_chat": "§6§l[DEMON KING DEFEATED] §fThe Elemental Sovereign has fallen! You have conquered the Isekai Floating Castles.",
    "awakening_system.castles.barrier_dispelled": "§5§l[CENTRAL CASTLE] §fAll 4 Elemental Seals have fallen! The Great Aether Barrier has dissipated and the Demon King's Throne Room is unlocked.",
    "awakening_system.castles.elemental_lord_defeated": "§6§l[ELEMENTAL LORD DEFEATED] §fYou have vanquished the %s§f! The altar of their tower has been liberated.",

    # Ancient Celestial Dragon Boss
    "awakening_system.boss.ancient_celestial_dragon.name": "§6§l✦ ANCIENT CELESTIAL DRAGON OF THE NINE HEAVENS ✦",
    "awakening_system.boss.ancient_celestial_dragon.defeated_state": "§c§l✦ CELESTIAL DRAGON VANQUISHED — DELIVER THE FINAL BLOW! ✦",

    # Blood Sect & Totem
    "awakening_system.entity.blood_qi_totem.banner_destroyed": "§4§l[BLOOD SECT] §fBanner destroyed! (§c%s§f remaining)",

    # Dokkaebi Quests & Messages
    "awakening_system.dokkaebi.quest_started_title": "§e§l✦ [STAR STREAM SUB-SCENARIO] ✦",
    "awakening_system.dokkaebi.quest_started_msg": "§b%s\n§f%s\n§6Reward: §e%s Coins",
    "awakening_system.dokkaebi.quest_target_completed_title": "§a§l✦ OBJECTIVE COMPLETED! ✦",
    "awakening_system.dokkaebi.quest_target_completed_msg": "§fSpeak with Bihyung to claim your coins and rewards.",
    "awakening_system.dokkaebi.scenario_cleared_title": "§e§l✦ SUB-SCENARIO CLEARED! ✦",
    "awakening_system.dokkaebi.scenario_cleared_msg": "§fBihyung: 'Brilliant broadcast, incarnation!\nThe Constellations have sent their coins.'\n§e+%s Coins credited.",
    "awakening_system.dokkaebi.quest_in_progress": "§8[§eStar Stream§8] §fBihyung: 'You haven't completed: §b%s§f! Progress: §e%s/%s§f'",
    "awakening_system.dokkaebi.quest_progress": "§8[§eStar Stream§8] §fSub-Scenario Progress: §e%s/%s",

    # Dokkaebi Sub-Scenarios
    "awakening_system.dokkaebi.quest.kill_monsters.title": "Calamity Cleansing",
    "awakening_system.dokkaebi.quest.kill_monsters.desc": "Eliminate 6 hostile monsters in the surrounding area.",
    "awakening_system.dokkaebi.quest.combo_strike.title": "Star Stream Martial Exhibition",
    "awakening_system.dokkaebi.quest.combo_strike.desc": "Achieve a 6-hit combo streak against enemies.",
    "awakening_system.dokkaebi.quest.collect_iron.title": "Mineral Tribute to the Dokkaebi",
    "awakening_system.dokkaebi.quest.collect_iron.desc": "Right-click Bihyung holding 8 iron ingots.",
    "awakening_system.dokkaebi.quest.survive_damage.title": "Extreme Combat Survival",
    "awakening_system.dokkaebi.quest.survive_damage.desc": "Withstand 10 incoming hits without falling in battle.",
    "awakening_system.dokkaebi.quest.mine_diamonds.title": "Diamond Offering of the Earth",
    "awakening_system.dokkaebi.quest.mine_diamonds.desc": "Deliver 1 diamond to Bihyung to please the Constellations.",

    # Mount Hua Disciple
    "awakening_system.npc.mount_hua.gift_talisman": "§d[Mount Hua Disciple] §a«Take this Taoist Sealing Talisman. May the Plum Blossom Sword path guide your cultivation.»",
    "awakening_system.npc.mount_hua.greeting": "§d[Mount Hua Disciple] §f«Greetings, fellow cultivator. Mount Hua's plum blossoms bloom resolute against demonic corruption.»",

    # Wu Xing Puzzle
    "awakening_system.wuxing.already_open": "§a[Wu Xing] §fThe Five Elements Cycle is already in perfect harmony and the sacred gate remains open.",
    "awakening_system.wuxing.already_active": "§e[Wu Xing] §fThe element %s §eis already channeling Qi. Proceed to the next element.",
    "awakening_system.wuxing.element_activated": "§a[Wu Xing] §fElemental Harmony: %s §aactivated (§e%s/5§a).",
    "awakening_system.wuxing.gate_open": "§6§l[ANCESTRAL GROTTO] §fThe Five Elements Cycle is in perfect harmony! The Great Jade Gate has opened.",
    "awakening_system.wuxing.dissonance": "§c[Wu Xing] §fElemental Dissonance! You disrupted the generative cycle. Expected: §e%s§f. Qi flow reset.",

    # System Events & Altars
    "awakening_system.event.kim_gongja_rewind_title": "§a§l✦ KIM GONG-JA'S REWIND ✦",
    "awakening_system.event.kim_gongja_rewind_msg": "§e\"Death is not the end as long as you do not give up.\"\n§7Time turns back a moment: You have cheated death.",
    "awakening_system.event.demonic_bell_title": "§4§l✦ DEMONIC CULT BELL ✦",
    "awakening_system.event.demonic_bell_msg": "§fThe martial chime awakens your internal energy.\n§7(Strength II for 2 min)",
    "awakening_system.event.bihyung_channel_title": "§e§l✦ STAR STREAM: BIHYUNG'S CHANNEL ✦",
    "awakening_system.event.bihyung_channel_msg": "§fYou tuned into the Dokkaebi's live broadcast!\n§e+100 Star Stream Coins credited.",
    "awakening_system.event.bihyung_already_claimed_title": "§7[Star Stream] Live Broadcast",
    "awakening_system.event.bihyung_already_claimed_msg": "§fBihyung: 'You already claimed your daily coins, subscriber!'",
    "awakening_system.event.death_king_altar_title": "§b§l✦ DEATH KING ALTAR ✦",
    "awakening_system.event.death_king_altar_msg": "§fKim Gong-ja's unyielding spirit bolsters your core.\n§7(Absorption III & Regeneration II)",
    "awakening_system.event.black_dragon_heart_title": "§c§l✦ BLACK DRAGON HEART ✦",
    "awakening_system.event.black_dragon_heart_msg": "§fThe flames of Cain purify your flesh.\n§7(Fire Resistance for 5 min & Strength II)",
    "awakening_system.event.wild_dokkaebi_title": "§e§l✦ [STAR STREAM] DOKKAEBI BROADCASTING! ✦",
    "awakening_system.event.wild_dokkaebi_msg": "§fDokkaebi Bihyung has appeared floating near you.\n§7Right-click him to receive a Sub-Scenario.",

    # Murim Pills & Items
    "awakening_system.item.black_tortoise_pill.title": "§3✦ BLACK TORTOISE ELIXIR ✦",
    "awakening_system.item.black_tortoise_pill.desc": "§bImmutable Vitality and Martial Resistance activated.",
    "awakening_system.item.celestial_dragon_pearl.title": "§6§l✦ PURE DRACONIC ASSIMILATION! ✦",
    "awakening_system.item.celestial_dragon_pearl.desc": "§fYou absorbed the Celestial Dragon Pearl. (+1,000 Cultivation Qi & Full Recovery)",
    "awakening_system.item.celestial_dragon_pearl.chat": "§6§l✦ [CELESTIAL PEARL] §fYou assimilated the draconic core (+1,000 Qi / Health & Energy 100%).",
    "awakening_system.item.death_king_clock.title": "§a§l✦ DEATH KING CLOCK ✦",
    "awakening_system.item.death_king_clock.desc": "§e\"No matter how many times we fall... we will always rise again.\"\n§7(Regeneration III, Absorption III, Strength II for 1 min)",
    "awakening_system.item.death_king_clock.skill_title": "§6§l✦ SKILL UNLOCKED ✦",
    "awakening_system.item.death_king_clock.skill_desc": "§eYou assimilated §aDeath King Rewind§e.",
    "awakening_system.item.death_king_clock.cooldown": "§c⏳ The Death King Clock is recharging (%ss)...",
    "awakening_system.item.marrow_pill.title": "§a✦ MARROW PURIFICATION ✦",
    "awakening_system.item.marrow_pill.desc": "§fImpurities cleansed! You unlocked Meridian %s.",
    "awakening_system.item.marrow_pill.max_title": "§a✦ PERFECT MARROW ✦",
    "awakening_system.item.marrow_pill.max_desc": "§fAll 12 meridians are already open: +1,200 Cultivation Qi.",
    "awakening_system.item.breakthrough_pill.peak_title": "§d✦ REALM PINNACLE REACHED ✦",
    "awakening_system.item.breakthrough_pill.peak_desc": "§fYou reached Sub-level 9 of %s.",
    "awakening_system.item.breakthrough_pill.success_title": "§6✦ REALM BREAKTHROUGH SUCCESSFUL! ✦",
    "awakening_system.item.breakthrough_pill.success_desc": "§fYou advanced to %s!",
    "awakening_system.item.rising_dragon_pill.title": "§6✦ RISING DRAGON PILL ✦",
    "awakening_system.item.rising_dragon_pill.desc": "§eSpiritual dragon torrent! +1,500 Cultivation Qi.",
    "awakening_system.action.skill_cooldown": "§c⏳ %s on cooldown (%ss)..."
}

new_keys_es = {
    # Shadow Monarch Boss
    "awakening_system.boss.shadow_monarch.phase2_taunt": "§8§l[MONARCA DEL ABISMO] §d§l¿Creísteis que habíais acorralado a la Muerte? ¡ARRODÍLLATE ANTE EL VERDADERO SOBERANO!",
    "awakening_system.boss.shadow_monarch.domain_taunt": "§8§l[MONARCA DEL ABISMO] §5¡Este reino me pertenece! ¡Que la oscuridad devore vuestras almas!",
    "awakening_system.boss.ashborn.defeat_quote": "§8§l[ASHBORN] §7Has demostrado ser digno... La autoridad y el ejército de las sombras ahora responderán a tu llamado.",
    "awakening_system.boss.antares.defeat_quote": "§4§l[ANTARES] §c¡Maldito cazador...! Las llamas de la destrucción y el aliento del dragón nunca se extinguirán...",

    # Elemental Bosses
    "awakening_system.castles.demon_king_defeated_chat": "§6§l[REY DEMONIO DERROTADO] §f¡El Soberano Elemental ha caído! Has conquistado los Castillos Flotantes de Isekai.",
    "awakening_system.castles.barrier_dispelled": "§5§l[CASTILLO CENTRAL] §f¡Los 4 Sellos Elementales han caído! La Gran Barrera de Éter se ha disipado y el Salón del Trono del Rey Demonio está abierto.",
    "awakening_system.castles.elemental_lord_defeated": "§6§l[SEÑOR ELEMENTAL DERROTADO] §f¡Has vencido al %s§f! El altar de su torre ha sido liberado.",

    # Ancient Celestial Dragon Boss
    "awakening_system.boss.ancient_celestial_dragon.name": "§6§l✦ DRAGÓN CELESTIAL ANCESTRAL DE LOS NUEVE CIELOS ✦",
    "awakening_system.boss.ancient_celestial_dragon.defeated_state": "§c§l✦ DRAGÓN CELESTIAL DERROTADO — ¡DALE EL GOLPE FINAL! ✦",

    # Blood Sect & Totem
    "awakening_system.entity.blood_qi_totem.banner_destroyed": "§4§l[SECTA DE SANGRE] §f¡Estandarte destruido! (Quedan §c%s§f activos)",

    # Dokkaebi Quests & Messages
    "awakening_system.dokkaebi.quest_started_title": "§e§l✦ [SUB-ESCENARIO DEL STAR STREAM] ✦",
    "awakening_system.dokkaebi.quest_started_msg": "§b%s\n§f%s\n§6Recompensa: §e%s Monedas",
    "awakening_system.dokkaebi.quest_target_completed_title": "§a§l✦ ¡OBJETIVO COMPLETADO! ✦",
    "awakening_system.dokkaebi.quest_target_completed_msg": "§fHabla con Bihyung para reclamar tus monedas y recompensas.",
    "awakening_system.dokkaebi.scenario_cleared_title": "§e§l✦ ¡SUB-ESCENARIO COMPLETADO! ✦",
    "awakening_system.dokkaebi.scenario_cleared_msg": "§fBihyung: '¡Brillante transmisión, encarnación!\nLas Constelaciones han enviado sus monedas.'\n§e+%s Monedas acreditadas.",
    "awakening_system.dokkaebi.quest_in_progress": "§8[§eStar Stream§8] §fBihyung: '¡Aún no completas: §b%s§f! Progreso: §e%s/%s§f'",
    "awakening_system.dokkaebi.quest_progress": "§8[§eStar Stream§8] §fProgreso de Sub-Escenario: §e%s/%s",

    # Dokkaebi Sub-Scenarios
    "awakening_system.dokkaebi.quest.kill_monsters.title": "Caza de Calamidades Menores",
    "awakening_system.dokkaebi.quest.kill_monsters.desc": "Elimina 6 criaturas hostiles en las cercanías.",
    "awakening_system.dokkaebi.quest.combo_strike.title": "Espectáculo Marcial del Star Stream",
    "awakening_system.dokkaebi.quest.combo_strike.desc": "Alcanza una racha de combo de 6 golpes seguidos contra enemigos.",
    "awakening_system.dokkaebi.quest.collect_iron.title": "Tributo de Minerales al Dokkaebi",
    "awakening_system.dokkaebi.quest.collect_iron.desc": "Haz clic derecho sobre Bihyung sosteniendo 8 lingotes de hierro.",
    "awakening_system.dokkaebi.quest.survive_damage.title": "Supervivencia Extrema en Combate",
    "awakening_system.dokkaebi.quest.survive_damage.desc": "Resiste y combate sin morir recibiendo o bloqueando 10 impactos.",
    "awakening_system.dokkaebi.quest.mine_diamonds.title": "Ofrenda de Diamantes de la Tierra",
    "awakening_system.dokkaebi.quest.mine_diamonds.desc": "Entrega 1 diamante a Bihyung para complacer a las Constelaciones.",

    # Mount Hua Disciple
    "awakening_system.npc.mount_hua.gift_talisman": "§d[Discípulo del Monte Hua] §a«Toma este Talismán Taoísta de Sellado. Que el sendero de la Espada de Ciruelo guíe tu cultivo.»",
    "awakening_system.npc.mount_hua.greeting": "§d[Discípulo del Monte Hua] §f«Saludos, compañero cultivador. Las flores de ciruelo del Monte Hua florecen firmes contra la corrupción demoníaca.»",

    # Wu Xing Puzzle
    "awakening_system.wuxing.already_open": "§a[Wu Xing] §fEl Ciclo de los Cinco Elementos ya está en perfecta armonía y la compuerta sagrada permanece abierta.",
    "awakening_system.wuxing.already_active": "§e[Wu Xing] §fEl elemento %s §eya está canalizando Qi. Continúa con el siguiente elemento.",
    "awakening_system.wuxing.element_activated": "§a[Wu Xing] §fArmonía Elemental: %s §aactivado (§e%s/5§a).",
    "awakening_system.wuxing.gate_open": "§6§l[GRUTA ANCESTRAL] §f¡El Ciclo de los Cinco Elementos está en armonía perfecta! La Gran Compuerta de Jade se ha abierto.",
    "awakening_system.wuxing.dissonance": "§c[Wu Xing] §f¡Disonancia Elemental! Has quebrado el ciclo generador. Se esperaba §e%s§f. El flujo de Qi se ha reiniciado.",

    # System Events & Altars
    "awakening_system.event.kim_gongja_rewind_title": "§a§l✦ REBOBINADO DE KIM GONG-JA ✦",
    "awakening_system.event.kim_gongja_rewind_msg": "§e\"La muerte no es el final mientras no te rindas.\"\n§7El tiempo retrocede un instante: Has burlado a la muerte.",
    "awakening_system.event.demonic_bell_title": "§4§l✦ CAMPANA DEL CULTO DEMONÍACO ✦",
    "awakening_system.event.demonic_bell_msg": "§fEl resonar marcial despierta tu energía interna.\n§7(Fuerza II por 2 min)",
    "awakening_system.event.bihyung_channel_title": "§e§l✦ STAR STREAM: CANAL DE BIHYUNG ✦",
    "awakening_system.event.bihyung_channel_msg": "§f¡Has sintonizado la transmisión del Dokkaebi!\n§e+100 Monedas del Star Stream acreditadas.",
    "awakening_system.event.bihyung_already_claimed_title": "§7[Star Stream] Transmisión en directo",
    "awakening_system.event.bihyung_already_claimed_msg": "§fBihyung: '¡Ya reclamaste las monedas del día, suscriptor!'",
    "awakening_system.event.death_king_altar_title": "§b§l✦ ALTAR DEL REY DE LA MUERTE ✦",
    "awakening_system.event.death_king_altar_msg": "§fEl espíritu indomable de Kim Gong-ja fortalece tu núcleo.\n§7(Absorción III y Regeneración II)",
    "awakening_system.event.black_dragon_heart_title": "§c§l✦ CORAZÓN DEL DRAGÓN NEGRO ✦",
    "awakening_system.event.black_dragon_heart_msg": "§fLas llamas de Caín purifican tu cuerpo.\n§7(Inmunidad al Fuego por 5 min y Fuerza II)",
    "awakening_system.event.wild_dokkaebi_title": "§e§l✦ [STAR STREAM] ¡DOKKAEBI TRANSMITIENDO! ✦",
    "awakening_system.event.wild_dokkaebi_msg": "§fEl Dokkaebi Bihyung ha aparecido flotando cerca de ti.\n§7Haz clic derecho para recibir un Sub-Escenario.",

    # Murim Pills & Items
    "awakening_system.item.black_tortoise_pill.title": "§3✦ ELIXIR DE LA TORTUGA NEGRA ✦",
    "awakening_system.item.black_tortoise_pill.desc": "§bVitalidad inmutable y Resistencia Marcial activadas.",
    "awakening_system.item.celestial_dragon_pearl.title": "§6§l✦ ¡ASIMILACIÓN DRACÓNICA PURA! ✦",
    "awakening_system.item.celestial_dragon_pearl.desc": "§fHas absorbido la Perla del Dragón Celestial. (+1,000 Qi de Cultivo y Purificación Total)",
    "awakening_system.item.celestial_dragon_pearl.chat": "§6§l✦ [PERLA CELESTIAL] §fHas asimilado el núcleo dracónico (+1,000 Qi / Salud y Energía al 100%).",
    "awakening_system.item.death_king_clock.title": "§a§l✦ RELOJ DEL REY DE LA MUERTE ✦",
    "awakening_system.item.death_king_clock.desc": "§e\"No importa cuántas veces caigamos... siempre nos levantaremos.\"\n§7(Regeneración III, Absorción III, Fuerza II por 1 min)",
    "awakening_system.item.death_king_clock.skill_title": "§6§l✦ HABILIDAD DESBLOQUEADA ✦",
    "awakening_system.item.death_king_clock.skill_desc": "§eHas asimilado el §aRebobinado del Rey de la Muerte§e.",
    "awakening_system.item.death_king_clock.cooldown": "§c⏳ El Reloj del Rey de la Muerte se está recargando (%ss)...",
    "awakening_system.item.marrow_pill.title": "§a✦ LIMPIEZA DE MÉDULA ✦",
    "awakening_system.item.marrow_pill.desc": "§f¡Impurezas purificadas! Has desbloqueado el Meridiano %s.",
    "awakening_system.item.marrow_pill.max_title": "§a✦ MÉDULA PERFECTA ✦",
    "awakening_system.item.marrow_pill.max_desc": "§fTus 12 meridianos ya están abiertos: +1,200 Qi de Cultivo.",
    "awakening_system.item.breakthrough_pill.peak_title": "§d✦ CÚSPIDE DE REINO ALCANZADA ✦",
    "awakening_system.item.breakthrough_pill.peak_desc": "§fHas alcanzado el Sub-nivel 9 de %s.",
    "awakening_system.item.breakthrough_pill.success_title": "§6✦ ¡RUPTURA DE REINO EXITOSA! ✦",
    "awakening_system.item.breakthrough_pill.success_desc": "§f¡Has avanzado al %s!",
    "awakening_system.item.rising_dragon_pill.title": "§6✦ PÍLDORA DEL DRAGÓN NACIENTE ✦",
    "awakening_system.item.rising_dragon_pill.desc": "§e¡Torrente espiritual de dragón! +1,500 Qi de Cultivo.",
    "awakening_system.action.skill_cooldown": "§c⏳ %s en recarga (%ss)..."
}

# Apply to all lang files
for f in os.listdir(lang_dir):
    if not f.endswith('.json'):
        continue
    p = os.path.join(lang_dir, f)
    with open(p, 'r', encoding='utf-8') as jf:
        data = json.load(jf)
    
    if f == 'en_us.json':
        keys_to_use = new_keys_en
    elif f.startswith('es_'):
        keys_to_use = new_keys_es
    else:
        # Fallback to English for other languages like ru_ru if translation not available
        keys_to_use = new_keys_en

    for k, v in keys_to_use.items():
        data[k] = v

    with open(p, 'w', encoding='utf-8') as jf:
        json.dump(data, jf, ensure_ascii=False, indent=2)

    print(f"Updated {f}")
