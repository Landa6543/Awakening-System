const fs = require('fs');

const extraTranslations = {
  // Teyvat Fish
  "item.awakening_system.teyvat_fish.variety": {
    en: "§7Variety: ",
    es: "§7Variedad: ",
    ru: "§7Разновидность: "
  },
  "item.awakening_system.teyvat_fish.medaka.name": {
    en: "Medaka",
    es: "Medaka",
    ru: "Оризия"
  },
  "item.awakening_system.teyvat_fish.medaka.desc": {
    en: "§8Common and lively fish of Teyvat.",
    es: "§8Pez común y vivaz de Teyvat.",
    ru: "§8Обычная живая рыбка Тейвата."
  },
  "item.awakening_system.teyvat_fish.aizen_medaka.name": {
    en: "Aizen Medaka",
    es: "Medaka Aizen",
    ru: "Голубая оризия"
  },
  "item.awakening_system.teyvat_fish.aizen_medaka.desc": {
    en: "§8Blue fish inhabiting the freshwater lakes of Mondstadt.",
    es: "§8Pez azul que habita los lagos de Mondstadt.",
    ru: "§8Синяя рыба, обитающая в пресных озерах Мондштадта."
  },
  "item.awakening_system.teyvat_fish.sweet_flower_medaka.name": {
    en: "Sweet-Flower Medaka",
    es: "Medaka Flor Dulce",
    ru: "Сахарная оризия"
  },
  "item.awakening_system.teyvat_fish.sweet_flower_medaka.desc": {
    en: "§8Sweet fish with radiant golden shades.",
    es: "§8Pez dulce de tonos dorados.",
    ru: "§8Сладкая рыбка золотистых оттенков."
  },
  "item.awakening_system.teyvat_fish.glaze_medaka.name": {
    en: "Glaze Medaka",
    es: "Medaka Esmaltado",
    ru: "Глазурная оризия"
  },
  "item.awakening_system.teyvat_fish.glaze_medaka.desc": {
    en: "§8Pure species from the crystal clear waters of Liyue.",
    es: "§8Especie pura de aguas cristalinas de Liyue.",
    ru: "§8Чистый вид из кристальных вод Ли Юэ."
  },
  "item.awakening_system.teyvat_fish.crystalfish.name": {
    en: "Crystalfish",
    es: "Pez Cristalino",
    ru: "Кристальная рыба"
  },
  "item.awakening_system.teyvat_fish.crystalfish.desc": {
    en: "§8Translucent body almost invisible underwater.",
    es: "§8Cuerpo translúcido casi invisible en el agua.",
    ru: "§8Полупрозрачное тело, почти невидимое под водой."
  },
  "item.awakening_system.teyvat_fish.venomspine_fish.name": {
    en: "Venomspine Fish",
    es: "Pez Espina Venenosa",
    ru: "Ядовитая колюшка"
  },
  "item.awakening_system.teyvat_fish.venomspine_fish.desc": {
    en: "§8Spiky fins loaded with paralyzing venom.",
    es: "§8Aletas puntiagudas cargadas de veneno.",
    ru: "§8Острые плавники, наполненные парализующим ядом."
  },
  "item.awakening_system.teyvat_fish.abiding_angelfish.name": {
    en: "Abiding Angelfish",
    es: "Pez Ángel Sagrado",
    ru: "Алая рыба-ангел"
  },
  "item.awakening_system.teyvat_fish.abiding_angelfish.desc": {
    en: "§8Rare golden fish revered by the Adepti.",
    es: "§8Raro pez dorado venerado por los Adeptos.",
    ru: "§8Редкая золотая рыба, почитаемая Адептами."
  },
  "item.awakening_system.teyvat_fish.raimei_angelfish.name": {
    en: "Raimei Angelfish",
    es: "Pez Ángel Raimei",
    ru: "Рыба-ангел раймэй"
  },
  "item.awakening_system.teyvat_fish.raimei_angelfish.desc": {
    en: "§8Electrifying fish swimming in the stormy waters of Inazuma.",
    es: "§8Pez electrizante que nada en aguas tormentosas de Inazuma.",
    ru: "§8Электрическая рыба, плавающая в бурных водах Инадзумы."
  },

  // Food Messages
  "item.awakening_system.heavenly_longevity_peach.msg": {
    en: "§6§l🍑 [PEACH OF IMMORTALITY] §eThe divine fruit regenerates your body and meridians (+150 Supreme Qi)!",
    es: "§6§l🍑 [MELOCOTÓN DE LA INMORTALIDAD] §e¡La fruta divina regenera tu cuerpo y meridianos (+150 Qi Supremo)!",
    ru: "§6§l🍑 [ПЕРСИК БЕССМЕРТИЯ] §eБожественный плод восстанавливает тело и меридианы (+150 высшей Ци)!"
  },
  "item.awakening_system.spirit_plum_fruit.msg": {
    en: "§d§l🌸 [SPIRIT PLUM] §fMount Hua nectar cleanses your meridians (+30 Qi).",
    es: "§d§l🌸 [CIRUELA ESPIRITUAL] §fEl néctar de Monte Hua purifica tus meridianos (+30 Qi).",
    ru: "§d§l🌸 [ДУХОВНАЯ СЛИВА] §fНектар Горы Хуа очищает ваши меридианы (+30 Ци)."
  },

  // Weapon Skill Messages
  "item.awakening_system.staff_of_homa.cd": {
    en: "§c§l✦ Staff of Homa on cooldown: §f%ss",
    es: "§c§l✦ Báculo de Homa en enfriamiento: §f%ss",
    ru: "§c§l✦ Посох Хомы на перезарядке: §f%sс"
  },
  "item.awakening_system.staff_of_homa.msg": {
    en: "§6§l✦ [STAFF OF HOMA] §c§lNETHER BUTTERFLY UNLEASHED! §e(Spectral Pyro Slash +45% ATK)",
    es: "§6§l✦ [BÁCULO DE HOMA] §c§l¡MARIPOSA DEL AVERNO DESATADA! §e(Tajo ígneo espectral +45% ATQ)",
    ru: "§6§l✦ [ПОСОХ ХОМЫ] §c§lБАБОЧКА ПРЕИСПОДНЕЙ ВЫСВОБОЖДЕНА! §e(Призрачный пиро-взмах +45% АТК)"
  },
  "item.awakening_system.primogem_sword.cd": {
    en: "§d✦ Astral Wish on cooldown: §f%ss",
    es: "§d✦ Deseo del Destino en enfriamiento: §f%ss",
    ru: "§d✦ Астральное желание на перезарядке: §f%sс"
  },
  "item.awakening_system.primogem_sword.msg": {
    en: "§5§l✦ [PRIMOGEM SWORD] §dAstral Wish of Destiny released! (+Health restored)",
    es: "§5§l✦ [ESPADA DE PROTOGEMAS] §d¡Deseo Astral del Destino liberado! (+Salud restaurada)",
    ru: "§5§l✦ [МЕЧ ИЗ КАМНЕЙ ИСТОКА] §dАстральное желание судьбы высвобождено! (+Здоровье восстановлено)"
  },
  "item.awakening_system.la_doncella.msg.blood_wave": {
    en: "§4§l🌹 [ASH THRONE] §cColossal crimson crescent & rising slash unleashed with lifesteal!",
    es: "§4§l🌹 [TRONO DE CENIZA] §c¡Creciente colosal carmesí y tajo ascendente desatados con robo de vida!",
    ru: "§4§l🌹 [ПЕПЕЛЬНЫЙ ТРОН] §cКолоссальный багровый полумесяц и восходящий взмах с вампиризмом!"
  },
  "item.awakening_system.la_doncella.msg.silence_cd": {
    en: "§d§l✦ Fragrance of Silence on cooldown: §f%ss",
    es: "§d§l✦ Fragancia del Silencio en enfriamiento: §f%ss",
    ru: "§d§l✦ Аромат тишины на перезарядке: §f%sс"
  },
  "item.awakening_system.la_doncella.msg.silence": {
    en: "§d§l✦ [FRAGRANCE OF SILENCE] §fFragrance purifies your spirit and blesses your senses (65 cone damage).",
    es: "§d§l✦ [FRAGANCIA DEL SILENCIO] §fLa fragancia purifica tu espíritu y bendice tus sentidos (65 daño en cono).",
    ru: "§d§l✦ [АРОМАТ ТИШИНЫ] §fАромат очищает ваш дух и благословляет чувства (65 урона в конусе)."
  },
  "item.awakening_system.creator_claymore.cd": {
    en: "§6§l✦ Genesis Cataclysm on cooldown: §f%ss",
    es: "§6§l✦ Cataclismo del Génesis en enfriamiento: §f%ss",
    ru: "§6§l✦ Катаклизм Генезиса на перезарядке: §f%sс"
  },
  "item.awakening_system.creator_claymore.exec_msg": {
    en: "§6§l✦ CREATOR'S EXECUTION! §eThe enemy's existence was wiped into stardust (Strength V activated).",
    es: "§6§l✦ ¡EJECUCIÓN DEL CREADOR! §eLa existencia del enemigo fue aniquilada en polvo estelar (Fuerza V activada).",
    ru: "§6§l✦ КАЗНЬ СОЗДАТЕЛЯ! §eСуществование врага расщеплено в звездную пыль (Сила V активирована)."
  },
  "item.awakening_system.dread_scythe.cd": {
    en: "§5§l☠ [Vortex of Terror] on cooldown: §f%ss",
    es: "§5§l☠ [Vórtice del Terror] en enfriamiento: §f%ss",
    ru: "§5§l☠ [Вихрь Ужаса] на перезарядке: §f%sс"
  },
  "item.awakening_system.dread_scythe.vortex_msg": {
    en: "§5§l☠ [VORTEX OF TERROR] §dHarvested the souls of %s enemies! (+Health restored)",
    es: "§5§l☠ [VÓRTICE DEL TERROR] §d¡Cosechaste las almas de %s enemigos! (+Salud restaurada)",
    ru: "§5§l☠ [ВИХРЬ УЖАСА] §dПожаты души %s врагов! (+Здоровье восстановлено)"
  },

  // Genshin Bow Passives
  "item.awakening_system.genshin_bow.amos_bow.passive": {
    en: "Strongbow: Projectile damage increases by up to +50% based on flight distance in the air.",
    es: "Flecha Fuerte: El daño del proyectil aumenta hasta un +50% según la distancia recorrida en el aire.",
    ru: "Мощный лук: Урон стрелы увеличивается до +50% в зависимости от дальности полета в воздухе."
  },
  "item.awakening_system.genshin_bow.aqua_simulacra.passive": {
    en: "Cleansing Form: Hydro-infused shots deal increased damage and grant movement speed.",
    es: "Forma Limpia: Disparos imbuidos en Hydro que aumentan el daño y otorgan velocidad.",
    ru: "Чистая форма: Гидро-выстрелы наносят повышенный урон и дают прибавку к скорости."
  },
  "item.awakening_system.genshin_bow.thundering_pulse.passive": {
    en: "Rule by Thunder: Fully charged shots emit Electro sparks with widespread area damage.",
    es: "Carga del Rayo: Los disparos cargados emiten chispas Electro con daño expansivo.",
    ru: "Владыка гроз: Заряженные выстрелы создают электро-искры с уроном по площади."
  },
  "item.awakening_system.genshin_bow.astral_archers_bow.passive": {
    en: "Power of Preservation: Crystal arrows slow and freeze enemies upon impact.",
    es: "Poder de la Preservación: Flechas de cristal que ralentizan y congelan a los enemigos.",
    ru: "Сила Сохранения: Хрустальные стрелы замедляют и замораживают пораженных врагов."
  },
  "item.awakening_system.genshin_bow.skyward_harp.passive": {
    en: "Echoing Ballad: Anemo-infused shots unleash celestial gusts of slicing wind.",
    es: "Balada Resonante: Disparos imbuidos en Anemo con ráfagas celestiales de viento cortante.",
    ru: "Эхо баллады: Анемо-выстрелы вызывают небесные порывы режущего ветра."
  },
  "item.awakening_system.genshin_bow.polar_star.passive": {
    en: "Daylight's Augur: Fatui glacial arrows with increased critical strike chance.",
    es: "Luz del Invierno Polar: Flechas glaciares de Fatui con mayor probabilidad de golpe crítico.",
    ru: "Знамение полярного дня: Ледниковые стрелы Фатуи с повышенным крит. шансом."
  },
  "item.awakening_system.genshin_bow.hunter_path.passive": {
    en: "At the End of the Beast-Paths: Dendro-infused shots with increased elemental damage.",
    es: "Fin del Crepúsculo Silvano: Disparos imbuidos en Dendro con daño elemental aumentado.",
    ru: "Конец звериных троп: Дендро-выстрелы с увеличенным стихийным уроном."
  },
  "item.awakening_system.genshin_bow.hamayumi.passive": {
    en: "Full Draw: Exorcist fire arrows scaling in power based on current energy.",
    es: "Tiro Demoniaco: Disparos de fuego exorcista con mayor poder según la energía disponible.",
    ru: "Полное натяжение: Экзорцистские огненные стрелы, сила которых зависит от энергии."
  },
  "item.awakening_system.genshin_bow.end_of_the_line.passive": {
    en: "Net Snare: Water arrows that blast targets backward with surging sea currents.",
    es: "Surcador del Océano: Flechas de agua que empujan a los objetivos con corrientes marinas.",
    ru: "Ловец волн: Водные стрелы, отбрасывающие цели мощными морскими течениями."
  },

  // 24 Genshin Weapons (Lore + Active Skill + Passive)
  // 1. ENGULFING_LIGHTNING
  "item.awakening_system.genshin_weapon.engulfing_lightning.lore": {
    en: "Sacred naginata of the Electro Archon.",
    es: "Naginata sagrada de la Arconte Electro",
    ru: "Священная нагината Электро Архонта"
  },
  "item.awakening_system.genshin_weapon.engulfing_lightning.active.title": {
    en: "§d▸ Active Skill: Secret Art: Musou Shinsetsu (Right Click / ~)",
    es: "§d▸ Habilidad Activa: Sueño de Musou Isshin (Clic Derecho / ~)",
    ru: "§d▸ Активный навык: Тайное искусство: Мусо синсэцу (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.engulfing_lightning.active.desc": {
    en: "  §7Strikes lightning on foes within 10 blocks; grants Strength, Resistance & Speed.",
    es: "  §7Descarga rayos sobre enemigos en 10 bloques y otorga Fuerza, Resistencia y Velocidad.",
    ru: "  §7Поражает молниями врагов в 10 блоках, даруя Силу, Сопротивление и Скорость."
  },
  "item.awakening_system.genshin_weapon.engulfing_lightning.passive.title": {
    en: "§e▸ Passive: Timeless Dream: Eternal Stove",
    es: "§e▸ Pasiva: Eternidad Inquebrantable",
    ru: "§e▸ Пассивно: Мечта вечности: Бесконечный очаг"
  },
  "item.awakening_system.genshin_weapon.engulfing_lightning.passive.desc": {
    en: "  §7Attacks electrocute enemies and infuse +5 Mana per strike.",
    es: "  §7Ataques electrocutan e infunden +5 Maná por golpe.",
    ru: "  §7Атаки бьют током и восполняют +5 маны за удар."
  },

  // 2. PRIMORDIAL_JADE_WINGED_SPEAR
  "item.awakening_system.genshin_weapon.primordial_jade_winged_spear.lore": {
    en: "Jade spear forged for the Conqueror of Demons.",
    es: "Lanza de jade forjada para el Conquistador de Demonios",
    ru: "Нефритовое копье, выкованное для Охотника на демонов"
  },
  "item.awakening_system.genshin_weapon.primordial_jade_winged_spear.active.title": {
    en: "§a▸ Active Skill: Bane of All Evil (Right Click / ~)",
    es: "§a▸ Habilidad Activa: Azote de Todo Mal (Clic Derecho / ~)",
    ru: "§a▸ Активный навык: Истребление всего зла (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.primordial_jade_winged_spear.active.desc": {
    en: "  §7Propels player high into the skies to execute a devastating plunge attack.",
    es: "  §7Propulsa al jugador a los cielos para ejecutar un devastador ataque en picada.",
    ru: "  §7Подбрасывает в небеса для сокрушительной атаки в падении."
  },
  "item.awakening_system.genshin_weapon.primordial_jade_winged_spear.passive.title": {
    en: "§2▸ Passive: Eagle Spear of Justice",
    es: "§2▸ Pasiva: Justicia de Jade",
    ru: "§2▸ Пассивно: Нефритовая справедливость"
  },
  "item.awakening_system.genshin_weapon.primordial_jade_winged_spear.passive.desc": {
    en: "  §7Increases Attack Damage with each consecutive hit.",
    es: "  §7Aumenta el daño de ataque con cada impacto certero consecutivo.",
    ru: "  §7Увеличивает урон атаки с каждым последовательным попаданием."
  },

  // 3. CRIMSON_MOONS_SEMBLANCE
  "item.awakening_system.genshin_weapon.crimson_moons_semblance.lore": {
    en: "Crimson scythe of the Knave of the Fatui.",
    es: "Guadaña carmesí de la Sota de los Fatui",
    ru: "Багровая коса Слуги из Фатуи"
  },
  "item.awakening_system.genshin_weapon.crimson_moons_semblance.active.title": {
    en: "§4▸ Active Skill: Banquet of the Crimson Moon (Right Click / ~)",
    es: "§4▸ Habilidad Activa: Banquete de la Luna Roja (Clic Derecho / ~)",
    ru: "§4▸ Активный навык: Пир багровой луны (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.crimson_moons_semblance.active.desc": {
    en: "  §7Sweeps a 360° scythe slice, withering enemies and siphoning their life.",
    es: "  §7Barre un corte de guadaña en 360°, descompone enemigos y drena su salud.",
    ru: "  §7Круговой взмах на 360°, иссушающий врагов и вытягивающий жизнь."
  },
  "item.awakening_system.genshin_weapon.crimson_moons_semblance.passive.title": {
    en: "§c▸ Passive: Ashen Shadow's Feast",
    es: "§c▸ Pasiva: Pacto Militar Carmesí",
    ru: "§c▸ Пассивно: Пир пепельной тени"
  },
  "item.awakening_system.genshin_weapon.crimson_moons_semblance.passive.desc": {
    en: "  §7Restores health to the wielder on each scythe slash.",
    es: "  §7Restaura salud al jugador en cada corte de guadaña.",
    ru: "  §7Восстанавливает здоровье владельца при каждом взмахе косы."
  },

  // 4. VORTEX_VANQUISHER
  "item.awakening_system.genshin_weapon.vortex_vanquisher.lore": {
    en: "Mythical polearm capable of cleaving Liyue's mountain peaks.",
    es: "Lanza mítica capaz de hender las montañas de Liyue",
    ru: "Мифическое копье, способное раскалывать горы Ли Юэ"
  },
  "item.awakening_system.genshin_weapon.vortex_vanquisher.active.title": {
    en: "§6▸ Active Skill: Telluric Domain (Right Click / ~)",
    es: "§6▸ Habilidad Activa: Dominio Telúrico (Clic Derecho / ~)",
    ru: "§6▸ Активный навык: Теллурический домен (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.vortex_vanquisher.active.desc": {
    en: "  §7Petrifies and immobilizes nearby foes, granting Absorption Shields III.",
    es: "  §7Petrifica e inmoviliza a enemigos cercanos y otorga Escudos de Absorción III.",
    ru: "  §7Окаменяет и обездвиживает врагов поблизости, даруя щиты поглощения III."
  },
  "item.awakening_system.genshin_weapon.vortex_vanquisher.passive.title": {
    en: "§e▸ Passive: Golden Majesty",
    es: "§e▸ Pasiva: Pilar Inconmovible",
    ru: "§e▸ Пассивно: Золотое величие"
  },
  "item.awakening_system.genshin_weapon.vortex_vanquisher.passive.desc": {
    en: "  §7Grants steady resistance and fortified defense in combat.",
    es: "  §7Otorga resistencia continua mientras luchas.",
    ru: "  §7Дарует постоянное сопротивление и укрепляет защиту в бою."
  },

  // 5. WOLFS_GRAVESTONE
  "item.awakening_system.genshin_weapon.wolfs_gravestone.lore": {
    en: "Greatsword of pure steel tempered by the soul of the Wolf of the North.",
    es: "Mandoble de acero puro templado con el alma del Rey del Viento",
    ru: "Двуручный меч из чистой стали, закаленный душой Волка Севера"
  },
  "item.awakening_system.genshin_weapon.wolfs_gravestone.active.title": {
    en: "§c▸ Active Skill: Howl of the Wolf King (Right Click / ~)",
    es: "§c▸ Habilidad Activa: Aullido del Rey Lobo (Clic Derecho / ~)",
    ru: "§c▸ Активный навык: Вой Короля Волков (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.wolfs_gravestone.active.desc": {
    en: "  §7Unleashes ancestral howl: grants +40% Attack Damage to you and your party.",
    es: "  §7Desata el aullido ancestral: otorga +40% de Daño de Ataque a ti y a tus aliados.",
    ru: "  §7Издает древний вой: дарует +40% урона атаки вам и союзникам."
  },
  "item.awakening_system.genshin_weapon.wolfs_gravestone.passive.title": {
    en: "§6▸ Passive: Relentless Hunter",
    es: "§6▸ Pasiva: Cazador Implacable",
    ru: "§6▸ Пассивно: Неумолимый охотник"
  },
  "item.awakening_system.genshin_weapon.wolfs_gravestone.passive.desc": {
    en: "  §7Strikes against targets below 30% HP deal lethal execute damage.",
    es: "  §7Ataques a enemigos con menos del 30% HP infligen daño mortal ejecutante.",
    ru: "  §7Удары по целям с менее 30% HP наносят смертельный добивающий урон."
  },

  // 6. REDHORN_STONETHRESHER
  "item.awakening_system.genshin_weapon.redhorn_stonethresher.lore": {
    en: "Golden-horned greatsword of the supreme leader of the Arataki Gang.",
    es: "Espadón de cuerno dorado del líder supremo de la Banda de Arataki",
    ru: "Золотисто-рогатый клеймор великого главаря банды Аратаки"
  },
  "item.awakening_system.genshin_weapon.redhorn_stonethresher.active.title": {
    en: "§e▸ Active Skill: Golden Oni King's Fury (Right Click / ~)",
    es: "§e▸ Habilidad Activa: Furia del Rey Oni Dorado (Clic Derecho / ~)",
    ru: "§e▸ Активный навык: Ярость Золотого Царя Они (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.redhorn_stonethresher.active.desc": {
    en: "  §7Activates Strength III and Resistance III with ground-shattering seismic slams.",
    es: "  §7Activa Fuerza III y Resistencia III con impactos de terremoto.",
    ru: "  §7Активирует Силу III и Сопротивление III с сокрушительными землетрясениями."
  },
  "item.awakening_system.genshin_weapon.redhorn_stonethresher.passive.title": {
    en: "§6▸ Passive: Gokadaijin",
    es: "§6▸ Pasiva: Gokadaijin del Rey Oni",
    ru: "§6▸ Пассивно: Гокадайдин Царя Они"
  },
  "item.awakening_system.genshin_weapon.redhorn_stonethresher.passive.desc": {
    en: "  §7Attack damage scales directly with the wielder's total armor points.",
    es: "  §7El daño de ataque escala con los puntos totales de armadura del portador.",
    ru: "  §7Урон атаки напрямую зависит от общего показателя брони владельца."
  },

  // 7. VERDICT
  "item.awakening_system.genshin_weapon.verdict.lore": {
    en: "Golden ceremonial axe of the President of the Spina di Rosula.",
    es: "Hacha ceremonial dorada de la Presidenta de Spina di Rosula",
    ru: "Золотой церемониальный топор президента Спина-ди-Росула"
  },
  "item.awakening_system.genshin_weapon.verdict.active.title": {
    en: "§e▸ Active Skill: Ceremonial Broadside (Right Click / ~)",
    es: "§e▸ Habilidad Activa: Salva Ceremonial (Clic Derecho / ~)",
    ru: "§e▸ Активный навык: Церемониальный залп (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.verdict.active.desc": {
    en: "  §7Fires a frontal cannon broadside obliterating enemies in a line.",
    es: "  §7Dispara una salva frontal de cañón que arrasa enemigos en línea recta.",
    ru: "  §7Выстреливает пушечным залпом, сметающим врагов по прямой."
  },
  "item.awakening_system.genshin_weapon.verdict.passive.title": {
    en: "§6▸ Passive: Solar Shrapnel",
    es: "§6▸ Pasiva: Metralla Solar",
    ru: "§6▸ Пассивно: Солнечная картечь"
  },
  "item.awakening_system.genshin_weapon.verdict.passive.desc": {
    en: "  §7Every 3rd hit triggers an explosion of crystal shrapnel.",
    es: "  §7Cada 3er golpe produce una explosión de metralla cristalina.",
    ru: "  §7Каждый третий удар вызывает взрыв кристальной картечи."
  },

  // 8. SKYWARD_BLADE
  "item.awakening_system.genshin_weapon.skyward_blade.lore": {
    en: "Sword symbolizing the honor of the Knights of Favonius and the blessing of the Dragon of the Wind.",
    es: "Espada que simboliza el honor del Caballero de Favonius y la bendición del Dragón del Viento",
    ru: "Меч, символизирующий честь рыцарей Фавония и благословение Дракона Ветра"
  },
  "item.awakening_system.genshin_weapon.skyward_blade.active.title": {
    en: "§b▸ Active Skill: Sky-Piercing Might (Right Click / ~)",
    es: "§b▸ Habilidad Activa: Poder Perforacielos (Clic Derecho / ~)",
    ru: "§b▸ Активный навык: Пронзатель небес (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.skyward_blade.active.desc": {
    en: "  §7Unleashes Dvalin's gales: grants Strength II, Speed II, and Haste II.",
    es: "  §7Desata la velocidad de los vientos de Dvalin: Fuerza II, Velocidad II y Prisa II.",
    ru: "  §7Дарует мощь вихрей Двалина: Силу II, Скорость II и Спешку II."
  },
  "item.awakening_system.genshin_weapon.skyward_blade.passive.title": {
    en: "§3▸ Passive: Celestial Fang",
    es: "§3▸ Pasiva: Colmillo Celestial",
    ru: "§3▸ Пассивно: Небесный клык"
  },
  "item.awakening_system.genshin_weapon.skyward_blade.passive.desc": {
    en: "  §7Generates slicing air blades with every standard strike.",
    es: "  §7Genera cuchillas de viento cortantes con cada golpe básico.",
    ru: "  §7Создает режущие лезвия ветра при каждой обычной атаке."
  },

  // 9. MISTSPLITTER_REFORGED
  "item.awakening_system.genshin_weapon.mistsplitter_reforged.lore": {
    en: "Sword blazing with a fierce violet light like the stars of an autumn night.",
    es: "Espada que brilla con la luz de una noche sin nubes",
    ru: "Меч, пылающий яростным фиолетовым светом безоблачной ночи"
  },
  "item.awakening_system.genshin_weapon.mistsplitter_reforged.active.title": {
    en: "§9▸ Active Skill: Transcendent Mist Slash (Right Click / ~)",
    es: "§9▸ Habilidad Activa: Corte de Niebla Trascendente (Clic Derecho / ~)",
    ru: "§9▸ Активный навык: Рассекающий туман взмах (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.mistsplitter_reforged.active.desc": {
    en: "  §7Launches a slashing wave of mist and frost that freezes targets.",
    es: "  §7Lanza un filo cortante de escarcha y niebla que congela a los enemigos.",
    ru: "  §7Выпускает режущую волну инея и тумана, замораживая врагов."
  },
  "item.awakening_system.genshin_weapon.mistsplitter_reforged.passive.title": {
    en: "§b▸ Passive: Mistsplitter's Edge",
    es: "§b▸ Pasiva: Emblema Cortanebulosas",
    ru: "§b▸ Пассивно: Эмблема рассекателя тумана"
  },
  "item.awakening_system.genshin_weapon.mistsplitter_reforged.passive.desc": {
    en: "  §7Increases agility and deeply chills targets upon impact.",
    es: "  §7Aumenta la velocidad y congela instantáneamente al impactar.",
    ru: "  §7Увеличивает скорость и глубоко замораживает врагов при попадании."
  },

  // 10. PRIMORDIAL_JADE_CUTTER
  "item.awakening_system.genshin_weapon.primordial_jade_cutter.lore": {
    en: "Ceremonial blade carved meticulously from pure nephrite jade.",
    es: "Hoja tallada meticulosamente a partir de jade puro",
    ru: "Церемониальный клинок, искусно вырезанный из чистого нефрита"
  },
  "item.awakening_system.genshin_weapon.primordial_jade_cutter.active.title": {
    en: "§a▸ Active Skill: Emerald Purification (Right Click / ~)",
    es: "§a▸ Habilidad Activa: Purificación Esmeralda (Clic Derecho / ~)",
    ru: "§a▸ Активный навык: Изумрудное очищение (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.primordial_jade_cutter.active.desc": {
    en: "  §7Dispels all negative effects and restores 35% Max Health.",
    es: "  §7Disipa todas las desventajas y restaura un 35% de la Salud Máxima.",
    ru: "  §7Снимает все дебаффы и восстанавливает 35% максимального здоровья."
  },
  "item.awakening_system.genshin_weapon.primordial_jade_cutter.passive.title": {
    en: "§2▸ Passive: Protector's Virtue",
    es: "§2▸ Pasiva: Pureza de Jade",
    ru: "§2▸ Пассивно: Добродетель защитника"
  },
  "item.awakening_system.genshin_weapon.primordial_jade_cutter.passive.desc": {
    en: "  §7Grants +10 Extra Hearts (+20 Max HP) while wielded.",
    es: "  §7Otorga +10 Corazones Adicionales (+20 HP) mientras la empuñas.",
    ru: "  §7Дарует +10 дополнительных сердец (+20 HP) в руках."
  },

  // 11. FREEDOM_SWORN
  "item.awakening_system.genshin_weapon.freedom_sworn.lore": {
    en: "Sword as straight and resolute as the Wind's pledge of liberty.",
    es: "Espada tan recta como el juramento de libertad del Viento",
    ru: "Меч, прямой и стойкий, как клятва свободы Ветра"
  },
  "item.awakening_system.genshin_weapon.freedom_sworn.active.title": {
    en: "§b▸ Active Skill: Zephyr's Rebellion Vortex (Right Click / ~)",
    es: "§b▸ Habilidad Activa: Vórtice Céfiro de la Rebelión (Clic Derecho / ~)",
    ru: "§b▸ Активный навык: Вихрь мятежного зефира (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.freedom_sworn.active.desc": {
    en: "  §7Conjures a wind vortex pulling and launching surrounding enemies into the sky.",
    es: "  §7Crea un vórtice de viento que arrastra y eleva a los enemigos circundantes.",
    ru: "  §7Создает вихрь ветра, притягивающий и подбрасывающий врагов вокруг."
  },
  "item.awakening_system.genshin_weapon.freedom_sworn.passive.title": {
    en: "§3▸ Passive: Revolutionary Choral",
    es: "§3▸ Pasiva: Concierto Revolucionario",
    ru: "§3▸ Пассивно: Революционный хорал"
  },
  "item.awakening_system.genshin_weapon.freedom_sworn.passive.desc": {
    en: "  §7Grants heightened movement speed and elevated jump boost.",
    es: "  §7Otorga velocidad de movimiento y salto mejorado.",
    ru: "  §7Дарует повышенную скорость передвижения и прыгучесть."
  },

  // 12. KEY_OF_KHAJ_NISUT
  "item.awakening_system.genshin_weapon.key_of_khaj_nisut.lore": {
    en: "Ceremonial sword of the high priests of the Sumeru desert.",
    es: "Espada ceremonial de los sacerdotes del desierto de Sumeru",
    ru: "Церемониальный меч верховных жрецов пустыни Сумеру"
  },
  "item.awakening_system.genshin_weapon.key_of_khaj_nisut.active.title": {
    en: "§3▸ Active Skill: Dance of Sacred Inundation (Right Click / ~)",
    es: "§3▸ Habilidad Activa: Danza de la Inundación Sagrada (Clic Derecho / ~)",
    ru: "§3▸ Активный навык: Танец священного разлива (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.key_of_khaj_nisut.active.desc": {
    en: "  §7Summons holy waters granting continuous regeneration and absorption.",
    es: "  §7Despliega aguas sagradas que regeneran salud continua y otorgan absorción.",
    ru: "  §7Призывает святые воды, даруя постоянную регенерацию и поглощение."
  },

  // 13. ABSOLUTION
  "item.awakening_system.genshin_weapon.absolution.lore": {
    en: "Supreme rapier imbued with the divine judgment of the Covenant of the End.",
    es: "Florete supremo imbuido con el juicio del Pacto del Fin",
    ru: "Высшая рапира, наполненная правосудием Завета Конца"
  },
  "item.awakening_system.genshin_weapon.absolution.active.title": {
    en: "§d▸ Active Skill: Fulgurant Thrust (Right Click / ~)",
    es: "§d▸ Habilidad Activa: Estocada Fulgurante (Clic Derecho / ~)",
    ru: "§d▸ Активный навык: Сверкающий выпад (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.absolution.active.desc": {
    en: "  §7Dashes forward at lightning speed piercing enemies with electrical discharge.",
    es: "  §7Embiste a gran velocidad atravesando enemigos con un ataque perforante eléctrico.",
    ru: "  §7Мчится вперед с молниеносной скоростью, пронзая врагов электричеством."
  },

  // 14. THE_FLUTE
  "item.awakening_system.genshin_weapon.the_flute.lore": {
    en: "Light sword with harmonic fluting that plays tunes when brandished.",
    es: "Espada ligera con orificios armónicos que emite melodías al ser blandida",
    ru: "Легкий меч с гармоническими отверстиями, поющий при каждом взмахе"
  },
  "item.awakening_system.genshin_weapon.the_flute.active.title": {
    en: "§e▸ Active Skill: Melodic Overture (Right Click / ~)",
    es: "§e▸ Habilidad Activa: Obertura Melódica (Clic Derecho / ~)",
    ru: "§e▸ Активный навык: Мелодичная увертюра (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.the_flute.active.desc": {
    en: "  §7Plays a resonant chord repelling foes and granting Regeneration.",
    es: "  §7Emite un acorde resonante que repele a los enemigos y otorga Regeneración.",
    ru: "  §7Издает резонирующий аккорд, отталкивающий врагов и дарующий регенерацию."
  },
  "item.awakening_system.genshin_weapon.the_flute.passive.title": {
    en: "§6▸ Passive: Harmonic Chord",
    es: "§6▸ Pasiva: Acorde Armónico",
    ru: "§6▸ Пассивно: Гармонический аккорд"
  },
  "item.awakening_system.genshin_weapon.the_flute.passive.desc": {
    en: "  §7Accumulates harmonic notes; every 5 hits detonates a musical shockwave.",
    es: "  §7Acumula notas armónicas al atacar; cada 5 golpes detona una onda musical de daño.",
    ru: "  §7Накапливает гармоники: каждые 5 ударов вызывают музыкальную ударную волну."
  },

  // 15. THE_BLACK_SWORD
  "item.awakening_system.genshin_weapon.the_black_sword.lore": {
    en: "Blood-stained sword craving combat and rejuvenating its bearer.",
    es: "Espada ensangrentada que anhela el caos del combate y regenera al portador",
    ru: "Окровавленный меч, жаждущий битвы и исцеляющий своего владельца"
  },
  "item.awakening_system.genshin_weapon.the_black_sword.active.title": {
    en: "§4▸ Active Skill: Bloodlust (Right Click / ~)",
    es: "§4▸ Habilidad Activa: Sed de Sangre (Clic Derecho / ~)",
    ru: "§4▸ Активный навык: Жажда крови (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.the_black_sword.active.desc": {
    en: "  §7Enters a voracious battle frenzy granting Strength II and Speed I.",
    es: "  §7Entra en un frenesí voraz otorgando Fuerza II y Velocidad I.",
    ru: "  §7Впадает в боевое безумие, даруя Силу II и Скорость I."
  },
  "item.awakening_system.genshin_weapon.the_black_sword.passive.title": {
    en: "§c▸ Passive: Justice",
    es: "§c▸ Pasiva: Justicia Sombría",
    ru: "§c▸ Пассивно: Справедливость"
  },
  "item.awakening_system.genshin_weapon.the_black_sword.passive.desc": {
    en: "  §7Restores health upon landing successful strikes on enemies.",
    es: "  §7Restaura salud al asestar impactos certeros a los oponentes.",
    ru: "  §7Восстанавливает здоровье при точных ударах по врагам."
  },

  // 16. THE_CATCH
  "item.awakening_system.genshin_weapon.the_catch.lore": {
    en: "Legendary fishing trident charged with massive energetic power.",
    es: "Tridente de pesca legendario con una tremenda carga energética",
    ru: "Легендарный рыбацкий трезубец с колоссальным энергетическим зарядом"
  },
  "item.awakening_system.genshin_weapon.the_catch.active.title": {
    en: "§b▸ Active Skill: Fisherman's Charge (Right Click / ~)",
    es: "§b▸ Habilidad Activa: Carga del Pescador (Clic Derecho / ~)",
    ru: "§b▸ Активный навык: Натиск рыбака (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.the_catch.active.desc": {
    en: "  §7Instantly recharges 35 mana/energy and heightens critical power.",
    es: "  §7Recarga 35 puntos de maná/energía al instante y aumenta el poder crítico.",
    ru: "  §7Мгновенно восстанавливает 35 маны/энергии и усиливает криты."
  },

  // 17. BLACK_TASSEL
  "item.awakening_system.genshin_weapon.black_tassel.lore": {
    en: "Traditional hunting spear with a reinforced tip, exceptionally lethal against slimes.",
    es: "Lanza de cazador tradicional con punta reforzada y letal contra slimes",
    ru: "Традиционное охотничье копье, смертоносное против слаймов"
  },
  "item.awakening_system.genshin_weapon.black_tassel.active.title": {
    en: "§7▸ Active Skill: Hunter's Thrust (Right Click / ~)",
    es: "§7▸ Habilidad Activa: Estocada Cazadora (Clic Derecho / ~)",
    ru: "§7▸ Активный навык: Охотничий выпад (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.black_tassel.active.desc": {
    en: "  §7Frontal thrust with high piercing power against amorphous monsters.",
    es: "  §7Carga frontal perforante letal contra monstruos amorfos.",
    ru: "  §7Лобовой пронзающий выпад против аморфных монстров."
  },
  "item.awakening_system.genshin_weapon.black_tassel.passive.title": {
    en: "§f▸ Passive: Bane of the Soft",
    es: "§f▸ Pasiva: Perdición del Slime",
    ru: "§f▸ Пассивно: Погибель мягкотелых"
  },
  "item.awakening_system.genshin_weapon.black_tassel.passive.desc": {
    en: "  §7Deals massive damage to Slimes and Magma Cubes, granting +2 Hearts.",
    es: "  §7Inflige daño masivo demoledor a Slimes y Magma Cubes, y otorga +2 Corazones.",
    ru: "  §7Наносит сокрушительный урон слаймам и магма-кубам, даруя +2 сердца."
  },

  // 18. FLEUVE_CENDRE_FERRYMAN
  "item.awakening_system.genshin_weapon.fleuve_cendre_ferryman.lore": {
    en: "Tough iron pipe tool used by clandestine plumbers of Fontaine.",
    es: "Herramienta de hierro de los fontaneros clandestinos",
    ru: "Прочная железная труба подпольных сантехников Фонтейна"
  },
  "item.awakening_system.genshin_weapon.fleuve_cendre_ferryman.active.title": {
    en: "§7▸ Active Skill: Clandestine Pressure (Right Click / ~)",
    es: "§7▸ Habilidad Activa: Presión Clandestina (Clic Derecho / ~)",
    ru: "§7▸ Активный навык: Тайное давление (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.fleuve_cendre_ferryman.active.desc": {
    en: "  §7Replenishes mana while quickening mining and attack haste.",
    es: "  §7Recupera maná y acelera la velocidad de minado y ataque.",
    ru: "  §7Восстанавливает ману и ускоряет добычу и скорость атаки."
  },

  // 19. HERTA_HAMMER
  "item.awakening_system.genshin_weapon.herta_hammer.lore": {
    en: "Colossal hammer of Genius Society Member #83. Kuru Kuru!",
    es: "Martillo colosal de la Miembro #83 de la Sociedad de Genios. ¡Kuru Kuru!",
    ru: "Колоссальный молот члена Общества гениев №83. Куру-куру!"
  },
  "item.awakening_system.genshin_weapon.herta_hammer.active.title": {
    en: "§d▸ Active Skill: Kuru Kuru! ~ Kururin (Right Click / ~)",
    es: "§d▸ Habilidad Activa: ¡Kuru Kuru! ~ Kururin (Clic Derecho / ~)",
    ru: "§d▸ Активный навык: Куру-куру! ~ Курурин (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.herta_hammer.active.desc": {
    en: "  §7Spins in a 360° icy whirlwind knocking back and freezing enemies.",
    es: "  §7Gira en un torbellino helado de 360° repeliendo y congelando enemigos.",
    ru: "  §7Вращается в ледяном вихре на 360°, отбрасывая и замораживая врагов."
  },
  "item.awakening_system.genshin_weapon.herta_hammer.passive.title": {
    en: "§b▸ Passive: Genius Follow-up",
    es: "§b▸ Pasiva: Golpe Adicional de la Genio",
    ru: "§b▸ Пассивно: Бонусная атака гения"
  },
  "item.awakening_system.genshin_weapon.herta_hammer.passive.desc": {
    en: "  §7Striking enemies below 50% HP triggers an extra follow-up frost burst.",
    es: "  §7Impactar a enemigos con menos del 50% HP detona un ataque adicional de escarcha.",
    ru: "  §7Удар по врагам с менее 50% HP вызывает дополнительный взрыв инея."
  },

  // 20. QINGMING_SWORD
  "item.awakening_system.genshin_weapon.qingming_sword.lore": {
    en: "High-frequency ether-edged sword from New Eridu.",
    es: "Espada de alta frecuencia con filo de éter animado de Nueva Eridu",
    ru: "Высокочастотный эфирный клинок из Нью-Эриду"
  },
  "item.awakening_system.genshin_weapon.qingming_sword.active.title": {
    en: "§b▸ Active Skill: Ether Frost Bloom (Right Click / ~)",
    es: "§b▸ Habilidad Activa: Floración de Escarcha de Éter (Clic Derecho / ~)",
    ru: "§b▸ Активный навык: Цветение эфирного инея (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.qingming_sword.active.desc": {
    en: "  §7Dashes forward detonating frost and concentrated ether bursts.",
    es: "  §7Embiste en línea recta detonando escarcha y explosión de éter.",
    ru: "  §7Рывок вперед с детонацией инея и эфирными взрывами."
  },

  // 21. TAILLESS
  "item.awakening_system.genshin_weapon.tailless.lore": {
    en: "Heavy cybernetic greatsword utilized by elite hollow investigators.",
    es: "Mandoble cibernético pesado utilizado por agentes de élite",
    ru: "Тяжелый кибернетический двуручник элитных оперативников"
  },
  "item.awakening_system.genshin_weapon.tailless.active.title": {
    en: "§6▸ Active Skill: Anomaly Rupture (Right Click / ~)",
    es: "§6▸ Habilidad Activa: Ruptura de Anomalía (Clic Derecho / ~)",
    ru: "§6▸ Активный навык: Разрыв аномалии (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.tailless.active.desc": {
    en: "  §7Downward seismic strike shattering enemy armor points.",
    es: "  §7Golpe sísmico descendente que fractura la armadura del enemigo.",
    ru: "  §7Нисходящий сейсмический удар, раскалывающий броню цели."
  },

  // 22. SHUNGUANG_SWORD
  "item.awakening_system.genshin_weapon.shunguang_sword.lore": {
    en: "Electrified reaction katana flashing with instant lightning speed.",
    es: "Katana electrificada con filo relampagueante de reacción rápida",
    ru: "Электрифицированная катана с молниеносной скоростью реакции"
  },
  "item.awakening_system.genshin_weapon.shunguang_sword.active.title": {
    en: "§e▸ Active Skill: Fleeting Lightning Flash (Right Click / ~)",
    es: "§e▸ Habilidad Activa: Destello Relámpago Fugaz (Clic Derecho / ~)",
    ru: "§e▸ Активный навык: Мимолетная вспышка молнии (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.shunguang_sword.active.desc": {
    en: "  §7Instant teleport piercing through opponents with electric discharge.",
    es: "  §7Teletransporte instantáneo perforando enemigos con descarga eléctrica.",
    ru: "  §7Мгновенная телепортация сквозь врагов с электрическим разрядом."
  },

  // 23. SHIYUAN_SWORD
  "item.awakening_system.genshin_weapon.shiyuan_sword.lore": {
    en: "Balanced tactical sword of the Commission inspectors.",
    es: "Espada equilibrada de combate de los inspectores de la Comisión",
    ru: "Сбалансированный боевой меч инспекторов Комиссии"
  },
  "item.awakening_system.genshin_weapon.shiyuan_sword.active.title": {
    en: "§a▸ Active Skill: Origin Pulse (Right Click / ~)",
    es: "§a▸ Habilidad Activa: Pulso del Origen (Clic Derecho / ~)",
    ru: "§a▸ Активный навык: Пульс истока (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.shiyuan_sword.active.desc": {
    en: "  §7Grants absorption shields and restores +30 Mana.",
    es: "  §7Otorga escudos de absorción y restaura +30 Maná.",
    ru: "  §7Дарует щиты поглощения и восстанавливает +30 маны."
  },

  // 24. DOMAIN_OF_SANCTION
  "item.awakening_system.genshin_weapon.domain_of_sanction.lore": {
    en: "Sacred katana forged with divine thunder and the authority of judgment.",
    es: "Katana sagrada forjada con la autoridad y la furia del trueno divino",
    ru: "Священная катана, выкованная с властью и яростью божественного грома"
  },
  "item.awakening_system.genshin_weapon.domain_of_sanction.active.title": {
    en: "§d▸ Active Skill: Herrscher's Judgment (Right Click / ~)",
    es: "§d▸ Habilidad Activa: Juicio de la Herrscher (Clic Derecho / ~)",
    ru: "§d▸ Активный навык: Суд Судьи (ПКМ / ~)"
  },
  "item.awakening_system.genshin_weapon.domain_of_sanction.active.desc": {
    en: "  §7Unleashes thunder tempest dealing 20 paralyzing damage and granting Strength & Speed.",
    es: "  §7Desata una tormenta de rayos que paraliza enemigos con 20 de daño y otorga Fuerza y Velocidad.",
    ru: "  §7Шторм молний, наносящий 20 урона с параличом и дарующий Силу и Скорость."
  },
  "item.awakening_system.genshin_weapon.domain_of_sanction.passive.title": {
    en: "§e▸ Passive: Thunderous Wrath",
    es: "§e▸ Pasiva: Furia Eléctrica",
    ru: "§e▸ Пассивно: Ярость грома"
  },
  "item.awakening_system.genshin_weapon.domain_of_sanction.passive.desc": {
    en: "  §735% chance to call down lightning bolts upon striking foes.",
    es: "  §735% de probabilidad de electrocutar enemigos al impactar con rayos.",
    ru: "  §735% шанс призвать молнию на пораженного врага."
  }
};

const langDir = 'src/main/resources/assets/awakening_system/lang';
const esFiles = [
  'es_es.json',
  'es_ar.json',
  'es_cl.json',
  'es_co.json',
  'es_ec.json',
  'es_mx.json',
  'es_pe.json',
  'es_uy.json',
  'es_ve.json'
];

function updateLang(fileName, langKey) {
  const filePath = `${langDir}/${fileName}`;
  const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
  let added = 0;
  for (const [key, trans] of Object.entries(extraTranslations)) {
    data[key] = trans[langKey] || trans.en;
    added++;
  }
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf8');
  console.log(`Updated ${fileName} with ${added} entries. Total keys: ${Object.keys(data).length}`);
}

updateLang('en_us.json', 'en');
updateLang('ru_ru.json', 'ru');
for (const f of esFiles) {
  updateLang(f, 'es');
}
console.log('Successfully added all extra weapon and fish translations!');
