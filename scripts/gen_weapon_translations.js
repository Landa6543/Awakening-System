const fs = require('fs');

const wlm = {
  "awakening_system.weapon_level.header": {
    en: "§6§l✦ WEAPON PROGRESSION ✦",
    es: "§6§l✦ PROGRESO DEL ARMA ✦",
    ru: "§6§l✦ ПРОГРЕСС ОРУЖИЯ ✦"
  },
  "awakening_system.weapon_level.max": {
    en: "§e✦ Level: §6§lLv. 100 §e(MAX TRANSCENDENCE)",
    es: "§e✦ Nivel: §6§lLv. 100 §e(TRASCENDENCIA MÁXIMA)",
    ru: "§e✦ Уровень: §6§lУр. 100 §e(МАКС. ТРАНСЦЕНДЕНТНОСТЬ)"
  },
  "awakening_system.weapon_level.level": {
    en: "§e✦ Level: §fLv. %s§8/§7100",
    es: "§e✦ Nivel: §fLv. %s§8/§7100",
    ru: "§e✦ Уровень: §fУр. %s§8/§7100"
  },
  "awakening_system.weapon_level.exp": {
    en: "§7EXP: §f%s §8/ §7%s",
    es: "§7EXP: §f%s §8/ §7%s",
    ru: "§7ОПЫТ: §f%s §8/ §7%s"
  },
  "awakening_system.weapon_level.bonus_dmg": {
    en: "§c⚔ Additional Damage: §a+%s%%",
    es: "§c⚔ Daño Adicional: §a+%s%%",
    ru: "§c⚔ Доп. урон: §a+%s%%"
  },
  "awakening_system.weapon_level.crit_chance": {
    en: "§6★ Critical Rate: §a+%s%%",
    es: "§6★ Probabilidad Crítica: §a+%s%%",
    ru: "§6★ Крит. шанс: §a+%s%%"
  },
  "awakening_system.weapon_level.armor_pen": {
    en: "§b✦ Armor Penetration: §a+%s%%",
    es: "§b✦ Perforación de Armadura: §a+%s%%",
    ru: "§b✦ Пробивание брони: §a+%s%%"
  },
  "awakening_system.weapon_level.total_kills": {
    en: "§8☠ Total Kills: §7%s",
    es: "§8☠ Bajas Totales: §7%s",
    ru: "§8☠ Всего убийств: §7%s"
  },
  "awakening_system.weapon_level.m10.unlocked": {
    en: "§a✔ Lv. 10: Blood Link (-20% Skill Cost)",
    es: "§a✔ Lv. 10: Vínculo de Sangre (-20% Coste Habilidades)",
    ru: "§a✔ Ур. 10: Узы крови (-20% затрат навыков)"
  },
  "awakening_system.weapon_level.m10.locked": {
    en: "§8✖ Lv. 10: Blood Link (Locked)",
    es: "§8✖ Lv. 10: Vínculo de Sangre (Bloqueado)",
    ru: "§8✖ Ур. 10: Узы крови (Заблокировано)"
  },
  "awakening_system.weapon_level.m25.unlocked": {
    en: "§b✔ Lv. 25: Martial Resonance (Shockwave on Crit)",
    es: "§b✔ Lv. 25: Resonancia Marcial (Onda de choque al crítico)",
    ru: "§b✔ Ур. 25: Боевой резонанс (Ударная волна при крите)"
  },
  "awakening_system.weapon_level.m25.locked": {
    en: "§8✖ Lv. 25: Martial Resonance (Locked)",
    es: "§8✖ Lv. 25: Resonancia Marcial (Bloqueado)",
    ru: "§8✖ Ур. 25: Боевой резонанс (Заблокировано)"
  },
  "awakening_system.weapon_level.m50.unlocked": {
    en: "§d✔ Lv. 50: Skill Awakening (+Power & -Cooldown)",
    es: "§d✔ Lv. 50: Despertar de Habilidad (+Potencia y -Cooldown)",
    ru: "§d✔ Ур. 50: Пробуждение навыка (+Мощь и -Кулдаун)"
  },
  "awakening_system.weapon_level.m50.locked": {
    en: "§8✖ Lv. 50: Skill Awakening (Locked)",
    es: "§8✖ Lv. 50: Despertar de Habilidad (Bloqueado)",
    ru: "§8✖ Ур. 50: Пробуждение навыка (Заблокировано)"
  },
  "awakening_system.weapon_level.m75.unlocked": {
    en: "§6✔ Lv. 75: Soul Radiance (3D Weapon Aura)",
    es: "§6✔ Lv. 75: Resplandor del Alma (Aura 3D del Arma)",
    ru: "§6✔ Ур. 75: Сияние души (3D аура оружия)"
  },
  "awakening_system.weapon_level.m75.locked": {
    en: "§8✖ Lv. 75: Soul Radiance (Locked)",
    es: "§8✖ Lv. 75: Resplandor del Alma (Bloqueado)",
    ru: "§8✖ Ур. 75: Сияние души (Заблокировано)"
  },
  "awakening_system.weapon_level.m100.unlocked": {
    en: "§e✔ Lv. 100: Divine Transcendence (Final DMG x1.5)",
    es: "§e✔ Lv. 100: Trascendencia Divina (Daño Final x1.5)",
    ru: "§e✔ Ур. 100: Божественная трансцендентность (Итоговый урон x1.5)"
  },
  "awakening_system.weapon_level.m100.locked": {
    en: "§8✖ Lv. 100: Divine Transcendence (Locked)",
    es: "§8✖ Lv. 100: Trascendencia Divina (Bloqueado)",
    ru: "§8✖ Ур. 100: Божественная трансцендентность (Заблокировано)"
  },
  "awakening_system.weapon_level.milestone.10": {
    en: "§a[Milestone I: Blood Link]",
    es: "§a[Hito I: Vínculo de Sangre]",
    ru: "§a[Этап I: Узы крови]"
  },
  "awakening_system.weapon_level.milestone.25": {
    en: "§b[Milestone II: Martial Resonance]",
    es: "§b[Hito II: Resonancia Marcial]",
    ru: "§b[Этап II: Боевой резонанс]"
  },
  "awakening_system.weapon_level.milestone.50": {
    en: "§d[Milestone III: Skill Awakening]",
    es: "§d[Hito III: Despertar de Habilidad]",
    ru: "§d[Этап III: Пробуждение навыка]"
  },
  "awakening_system.weapon_level.milestone.75": {
    en: "§6[Milestone IV: Soul Radiance]",
    es: "§6[Hito IV: Resplandor del Alma]",
    ru: "§6[Этап IV: Сияние души]"
  },
  "awakening_system.weapon_level.milestone.100": {
    en: "§e§l✦ [SUPREME MILESTONE: DIVINE TRANSCENDENCE] ✦",
    es: "§e§l✦ [HITO SUPREMO: TRASCENDENCIA DIVINA] ✦",
    ru: "§e§l✦ [ВЫСШИЙ ЭТАП: БОЖЕСТВЕННАЯ ТРАНСЦЕНДЕНТНОСТЬ] ✦"
  },
  "awakening_system.weapon_level.notify.subtitle": {
    en: "§f%s §8▸ §6Level %s/100",
    es: "§f%s §8▸ §6Nivel %s/100",
    ru: "§f%s §8▸ §6Уровень %s/100"
  },
  "awakening_system.weapon_level.notify.actionbar": {
    en: "§6§l✦ MILESTONE UNLOCKED! §e%s §8(§f%s §eLv. %s§8)",
    es: "§6§l✦ ¡HITO DESBLOQUEADO! §e%s §8(§f%s §eLv. %s§8)",
    ru: "§6§l✦ ЭТАП РАЗБЛОКИРОВАН! §e%s §8(§f%s §eУр. %s§8)"
  },
  "awakening_system.weapon_level.notify.chat": {
    en: "§6§l[Awakening] §eYour weapon §f%s §ehas unlocked %s §7(Lv. %s/100)!",
    es: "§6§l[Awakening] §e¡Tu arma §f%s §eha desbloqueado el %s §7(Lv. %s/100)!",
    ru: "§6§l[Awakening] §eВаше оружие §f%s §eразблокировало %s §7(Ур. %s/100)!"
  },
  "awakening_system.weapon_level.notify.level_up": {
    en: "§6✦ §f%s §7→ §e§lLevel %s§8/100 §6✦",
    es: "§6✦ §f%s §7→ §e§lNivel %s§8/100 §6✦",
    ru: "§6✦ §f%s §7→ §e§lУровень %s§8/100 §6✦"
  }
};

const soulSolid = {
  "item.awakening_system.soul_solid.subtitle": {
    en: "§b✦ Brook's Shikomizue Cane Sword",
    es: "§b✦ Espada-Bastón Shikomizue de Brook",
    ru: "§b✦ Меч-трость Сикомидзуэ Брука"
  },
  "item.awakening_system.soul_solid.lore": {
    en: "§7\"The chill emanating from the kingdom of the dead will freeze your very soul.\"",
    es: "§7\"El frío que emana del reino de los muertos congelará hasta tu alma.\"",
    ru: "§7\"Холод из царства мертвых заморозит саму вашу душу.\""
  },
  "item.awakening_system.soul_solid.passive.title": {
    en: "§e✦ Passive: Underworld Chill",
    es: "§e✦ Pasiva: Frío del Inframundo",
    ru: "§e✦ Пассивно: Загробный холод"
  },
  "item.awakening_system.soul_solid.passive.desc": {
    en: "  §7Your slashes deeply freeze the target, inflicting Slowness and icy frost.",
    es: "  §7Tus tajos congelan intensamente al objetivo, aplicando lentitud y escarcha helada.",
    ru: "  §7Ваши удары глубоко замораживают цель, накладывая Замедление и ледяной иней."
  },
  "item.awakening_system.soul_solid.active.title": {
    en: "§6✦ Active (Right Click): Aubade Coup Droit",
    es: "§6✦ Activa (Clic Derecho): Aubade Coup Droit",
    ru: "§6✦ Активно (ПКМ): Aubade Coup Droit"
  },
  "item.awakening_system.soul_solid.active.desc": {
    en: "  §7Lunge forward in a piercing sonic thrust that freezes and destroys all enemies in a line.",
    es: "  §7Arremete al frente en una estocada sónica perforante que congela y destruye a todos los enemigos en línea recta.",
    ru: "  §7Рывок вперед со звуковым колющим ударом, замораживающий и уничтожающий врагов по прямой."
  },
  "item.awakening_system.soul_solid.msg": {
    en: "§b✦ [Soul Solid] §fAubade Coup Droit! Underworld Frost Thrust unleashed.",
    es: "§b✦ [Soul Solid] §f¡Aubade Coup Droit! Estocada de Hielo del Inframundo desatada.",
    ru: "§b✦ [Soul Solid] §fAubade Coup Droit! Ледяной укол подземного мира высвобожден."
  }
};

const staffOfHoma = {
  "item.awakening_system.staff_of_homa.badge": {
    en: "§6⭐⭐⭐⭐⭐ [5★ Legendary Polearm]",
    es: "§6⭐⭐⭐⭐⭐ [Arma de Asta Legendaria 5★]",
    ru: "§6⭐⭐⭐⭐⭐ [Легендарное древковое оружие 5★]"
  },
  "item.awakening_system.staff_of_homa.desc": {
    en: "§7Hu Tao's legendary staff used to cleanse and purify souls.",
    es: "§7El legendario cetro de Hu Tao utilizado para purificar las almas.",
    ru: "§7Легендарный посох Ху Тао для очищения душ."
  },
  "item.awakening_system.staff_of_homa.active.title": {
    en: "§c▸ Skill: Guide to Afterlife (Right Click / ~)",
    es: "§c▸ Habilidad: Guía del Más Allá (Clic derecho / ~)",
    ru: "§c▸ Навык: Путеводитель по загробной жизни (ПКМ / ~)"
  },
  "item.awakening_system.staff_of_homa.active.desc": {
    en: "  §7Sacrifices 15% HP to unleash Strength III and ghostly flames for 12s.",
    es: "  §7Sacrifica 15% HP para desatar Fuerza III y llamas fantasmales por 12s.",
    ru: "  §7Жертвует 15% HP, наделяя Силой III и призрачным пламенем на 12 сек."
  },
  "item.awakening_system.staff_of_homa.passive.title": {
    en: "§6▸ Passive: Nether Butterfly",
    es: "§6▸ Pasiva: Mariposa del Averno",
    ru: "§6▸ Пассивно: Бабочка преисподней"
  },
  "item.awakening_system.staff_of_homa.passive.desc1": {
    en: "  §7Below 50% HP, hits incinerate and deal bonus fire damage.",
    es: "  §7Al tener menos del 50% HP, los golpes incineran e infligen daño ígneo extra.",
    ru: "  §7При менее 50% HP удары испепеляют и наносят доп. огненный урон."
  },
  "item.awakening_system.staff_of_homa.passive.desc2": {
    en: "  §7Restores 3% Max Health on each hit.",
    es: "  §7Restaura 3% de Vida Máxima en cada impacto.",
    ru: "  §7Восстанавливает 3% макс. здоровья за каждый удар."
  }
};

const primogemSword = {
  "item.awakening_system.primogem_sword.badge": {
    en: "§5§l[LIVING BLADE OF DESTINY]",
    es: "§5§l[HOJA VIVIENTE DEL DESTINO]",
    ru: "§5§l[ЖИВОЙ КЛИНОК СУДЬБЫ]"
  },
  "item.awakening_system.primogem_sword.desc": {
    en: "§7Forged with the primordial crystals of Teyvat's wishes.",
    es: "§7Forjada con los cristales primordiales de los deseos de Teyvat.",
    ru: "§7Выкован из первозданных кристаллов желаний Тейвата."
  },
  "item.awakening_system.primogem_sword.active": {
    en: "§d✦ Skill: Right Click — Astral Wish of Destiny (Amethyst Slash + Heal).",
    es: "§d✦ Habilidad: Clic Derecho — Deseo Astral del Destino (Tajo de Amatista + Cura).",
    ru: "§d✦ Навык: ПКМ — Астральное желание судьбы (Аметистовый взмах + лечение)."
  },
  "item.awakening_system.primogem_sword.passive": {
    en: "§a✦ Astral Drain: Critical hits absorb enemy vitality.",
    es: "§a✦ Drenaje Astral: Los impactos críticos absorben vitalidad enemiga.",
    ru: "§a✦ Астральное поглощение: Критические удары поглощают жизненную силу врага."
  }
};

const laDoncella = {
  "item.awakening_system.la_doncella.title": {
    en: "§d§l✦ [MAHIRO'S CLAYMORE // SUPREME MYTH] ✦",
    es: "§d§l✦ [MANDOBLE DE MAHIRO // MITO SUPREMO] ✦",
    ru: "§d§l✦ [ДВУРУЧНЫЙ МЕЧ МАХИРО // ВЫСШИЙ МИФ] ✦"
  },
  "item.awakening_system.la_doncella.quote": {
    en: "§f\"...whose Thorns Grew around the Maiden's Heart...\"",
    es: "§f\"...cuyas Espinas Crecieron alrededor del Corazón de la Doncella...\"",
    ru: "§f\"...чьи Шипы Разрослись вокруг Сердца Девы...\""
  },
  "item.awakening_system.la_doncella.pure.header": {
    en: "§f§l[PURE FORM: STEEL OF ROSES]",
    es: "§f§l[FORMA PURA: ACERO DE LAS ROSAS]",
    ru: "§f§l[ЧИСТАЯ ФОРМА: СТАЛЬ РОЗ]"
  },
  "item.awakening_system.la_doncella.pure.dmg": {
    en: "§c▪ Base Physical DMG: §f40.0 §8(Claymore 1.2 Speed)",
    es: "§c▪ Daño Físico Base: §f40.0 §8(Mandoble 1.2 Vel.)",
    ru: "§c▪ Базовый физ. урон: §f40.0 §8(Клеймор 1.2 скор.)"
  },
  "item.awakening_system.la_doncella.pure.petal": {
    en: "§a▪ Petal Cleave: §fStrikes deal §a+28 Area Damage §7(6.5 blocks).",
    es: "§a▪ Hendidura de Pétalos: §fGolpes causan §a+28 de Daño en Área §7(6.5 bloques).",
    ru: "§a▪ Рассечение лепестков: §fУдары наносят §a+28 урона по площади §7(6.5 блоков)."
  },
  "item.awakening_system.la_doncella.pure.active": {
    en: "§e▪ Active (Right Click): §dFragrance of Silence §7(Cone of §f65 damage §7+ Cleanse + Regen II).",
    es: "§e▪ Activa (Clic Derecho): §dFragancia del Silencio §7(Cono de §f65 daño §7+ Purificación + Regen II).",
    ru: "§e▪ Активно (ПКМ): §dАромат тишины §7(Конус §f65 урона §7+ очищение + регенерация II)."
  },
  "item.awakening_system.la_doncella.pure.cd": {
    en: "§8▪ Cooldown: §f8s.",
    es: "§8▪ Enfriamiento: §f8s.",
    ru: "§8▪ Кулдаун: §f8с."
  },
  "item.awakening_system.la_doncella.awakened.header": {
    en: "§4§l[AWAKENED FORM: CRIMSON MOON & THORNS]",
    es: "§4§l[FORMA DESPIERTA: LUNA CARMESÍ Y ESPINAS]",
    ru: "§4§l[ПРОБУЖДЕННАЯ ФОРМА: БАГРОВАЯ ЛУНА И ШИПЫ]"
  },
  "item.awakening_system.la_doncella.awakened.dmg": {
    en: "§c▪ Base Physical DMG: §f40.0 §8(+20 Thorn Armor Piercing = 60 Total)",
    es: "§c▪ Daño Físico Base: §f40.0 §8(+20 Perforación de Espinas = 60 Total)",
    ru: "§c▪ Базовый физ. урон: §f40.0 §8(+20 пробивание шипами = 60 всего)"
  },
  "item.awakening_system.la_doncella.awakened.thorns": {
    en: "§4▪ Crown of Thorns: §fThorns reflect §c32 piercing damage §fto attackers.",
    es: "§4▪ Corona de Espinas: §fEspinas devuelven §c32 de daño perforante §fa atacantes.",
    ru: "§4▪ Терновый венец: §fШипы отражают §c32 колющего урона §fатакующим."
  },
  "item.awakening_system.la_doncella.awakened.wither": {
    en: "§4▪ Shadow Laceration: §fApplies §5Wither III §7and §8Slowness III §7on hit.",
    es: "§4▪ Laceración Sombría: §fAplica §5Wither III §7y §8Lentitud III §7al golpear.",
    ru: "§4▪ Теневое рассечение: §fНакладывает §5Иссушение III §7и §8Замедление III §7при ударе."
  },
  "item.awakening_system.la_doncella.awakened.active": {
    en: "§c▪ Active (Right Click): §4Briar Cataclysm §7(§f95 area damage §7in 12 blocks).",
    es: "§c▪ Activa (Clic Derecho): §4Cataclismo de Espinas §7(§f95 daño en área §7en 12 bloques).",
    ru: "§c▪ Активно (ПКМ): §4Катаклизм шипов §7(§f95 урона по площади §7в 12 блоках)."
  },
  "item.awakening_system.la_doncella.awakened.cd": {
    en: "§8▪ Cooldown: §f12s.",
    es: "§8▪ Enfriamiento: §f12s.",
    ru: "§8▪ Кулдаун: §f12с."
  },
  "item.awakening_system.la_doncella.hint.shift": {
    en: "§8▪ Shift + Right Click: Toggle Maiden Form.",
    es: "§8▪ Shift + Clic Derecho: Alternar Forma de la Doncella.",
    ru: "§8▪ Shift + ПКМ: Переключить форму Девы."
  },
  "item.awakening_system.la_doncella.hint.gacha": {
    en: "§5▪ Gacha Drop Rate: §d0.020%",
    es: "§5▪ Probabilidad de Obtención en Gacha: §d0.020%",
    ru: "§5▪ Шанс выпадения в гаче: §d0.020%"
  },
  "item.awakening_system.la_doncella.msg.awakened": {
    en: "§4§l🌹 [LA DONCELLA] §cThe Ash Throne awakens under the Crimson Moon! Thorns have sprouted.",
    es: "§4§l🌹 [LA DONCELLA] §c¡El Trono de Ceniza despierta bajo la Luna Carmesí! Las espinas han brotado.",
    ru: "§4§l🌹 [ДЕВА] §cПепельный Трон пробуждается под Багровой Луной! Тернии проросли."
  },
  "item.awakening_system.la_doncella.msg.pure": {
    en: "§f§l✦ [LA DONCELLA] §7Thorns retract. Noble steel and pure roses shine forth.",
    es: "§f§l✦ [LA DONCELLA] §7Las espinas se repliegan. El acero noble y las rosas puras resplandecen.",
    ru: "§f§l✦ [ДЕВА] §7Шипы отступают. Благородная сталь и чистые розы сияют вновь."
  },
  "item.awakening_system.la_doncella.msg.cd": {
    en: "§c§l✦ Briar Cataclysm on cooldown: §f%ss",
    es: "§c§l✦ Cataclismo de Espinas en enfriamiento: §f%ss",
    ru: "§c§l✦ Катаклизм шипов на перезарядке: §f%sс"
  }
};

const creatorClaymore = {
  "item.awakening_system.creator_claymore.header": {
    en: "§d§l✦ TRANSCENDENTAL WEAPON OF THE CREATOR // SUPREME COSMOS RANK ✦",
    es: "§d§l✦ ARMA TRASCENDENTAL DEL CREADOR // RANGO SUPREMO COSMOS ✦",
    ru: "§d§l✦ ТРАНСЦЕНДЕНТНОЕ ОРУЖИЕ СОЗДАТЕЛЯ // ВЫСШИЙ РАНГ КОСМОСА ✦"
  },
  "item.awakening_system.creator_claymore.title": {
    en: "§5§lPrimordial Claymore: Cosmic Void & Absolute Genesis",
    es: "§5§lMandoble Primordial: Vacío Cósmico & Génesis Absoluto",
    ru: "§5§lПервозданный клеймор: Космическая пустота и абсолютный генезис"
  },
  "item.awakening_system.creator_claymore.desc1": {
    en: "§7Forged in the stellar abyss with primordial amethyst and lilac void essences.",
    es: "§7Forjado en el abismo estelar con amatista primordial y esencias lilas del vacío.",
    ru: "§7Выкован в звездной бездне из первозданного аметиста и сиреневых эссенций пустоты."
  },
  "item.awakening_system.creator_claymore.desc2": {
    en: "§8Bears the heraldry of cosmic creation and the 7 violet stars of the Monarch.",
    es: "§8Porta la heráldica de la creación cósmica y las 7 estrellas violetas del Monarca.",
    ru: "§8Несет геральдику космического творения и 7 фиолетовых звезд Монарха."
  },
  "item.awakening_system.creator_claymore.attr_header": {
    en: "§d§l[CREATION ATTRIBUTES]",
    es: "§d§l[ATRIBUTOS DE LA CREACIÓN]",
    ru: "§d§l[АТРИБУТЫ ТВОРЕНИЯ]"
  },
  "item.awakening_system.creator_claymore.attr_dmg": {
    en: "§c▪ Base Physical DMG: §f52 §8(+36 True Cosmic Damage)",
    es: "§c▪ Daño Físico Base: §f52 §8(+36 Daño Verdadero Cósmico)",
    ru: "§c▪ Базовый физ. урон: §f52 §8(+36 космический чистый урон)"
  },
  "item.awakening_system.creator_claymore.attr_speed": {
    en: "§e▪ Attack Speed: §f1.2 §8(Fast Colossal Greatsword)",
    es: "§e▪ Velocidad de Ataque: §f1.2 §8(Mandoble Colosal Rápido)",
    ru: "§e▪ Скорость атаки: §f1.2 §8(Быстрый колоссальный двуручник)"
  },
  "item.awakening_system.creator_claymore.attr_dura": {
    en: "§b▪ Durability: §aIndestructible §8(60,000 / Fire Immune)",
    es: "§b▪ Durabilidad: §aIndestructible §8(60,000 / Fuego Inmune)",
    ru: "§b▪ Прочность: §aНерушимый §8(60,000 / Огнестойкий)"
  },
  "item.awakening_system.creator_claymore.passive1_header": {
    en: "§e§l[PASSIVE: AUTHORITY OF THE CREATOR]",
    es: "§e§l[PASIVA: AUTORIDAD DEL CREADOR]",
    ru: "§e§l[ПАССИВНО: ВЛАСТЬ СОЗДАТЕЛЯ]"
  },
  "item.awakening_system.creator_claymore.passive1_desc1": {
    en: "§7▪ Each strike unleashes §e+36 True Damage §7ignoring armor.",
    es: "§7▪ Cada golpe desata §e+36 de Daño Verdadero §7que perfora armaduras.",
    ru: "§7▪ Каждый удар наносит §e+36 чистого урона§7, игнорирующего броню."
  },
  "item.awakening_system.creator_claymore.passive1_desc2": {
    en: "§7▪ Colossal sweep of §b8 blocks §7dealing §c30 damage §7to nearby foes.",
    es: "§7▪ Barrido colosal de §b8 bloques §7que inflige §c30 de daño §7a enemigos cercanos.",
    ru: "§7▪ Колоссальный взмах на §b8 блоков§7, наносящий §c30 урона §7ближайшим врагам."
  },
  "item.awakening_system.creator_claymore.passive2_header": {
    en: "§d§l[PASSIVE: SENTENCE OF GENESIS]",
    es: "§d§l[PASIVA: SENTENCIA DEL GÉNESIS]",
    ru: "§d§l[ПАССИВНО: ПРИГОВОР ГЕНЕЗИСА]"
  },
  "item.awakening_system.creator_claymore.passive2_desc1": {
    en: "§7▪ Marks targets with Weakness IV and Glowing.",
    es: "§7▪ Marca a los objetivos con debilidad IV y resplandor.",
    ru: "§7▪ Помечает цели Слабостью IV и Свечением."
  },
  "item.awakening_system.creator_claymore.passive2_desc2": {
    en: "§c▪ Cosmic Execution: §7If the target falls below §c35% HP§7,",
    es: "§c▪ Ejecución Cósmica: §7Si el objetivo cae por debajo del §c35% de vida§7,",
    ru: "§c▪ Космическая казнь: §7Если здоровье цели падает ниже §c35%§7,"
  },
  "item.awakening_system.creator_claymore.passive2_desc3": {
    en: "  §7its existence is §4instantly disintegrated§7, granting §6Strength V§7 & §bResistance III§7.",
    es: "  §7su existencia es §4desintegrada instantáneamente§7, otorgando §6Fuerza V§7 y §bResistencia III§7.",
    ru: "  §7ее существование §4мгновенно стирается§7, даруя §6Силу V§7 и §bСопротивление III§7."
  },
  "item.awakening_system.creator_claymore.active_header": {
    en: "§5§l[ACTIVE SKILL: GENESIS CATACLYSM] §8(Right Click)",
    es: "§5§l[HABILIDAD ACTIVA: CATACLISMO DEL GÉNESIS] §8(Clic Derecho)",
    ru: "§5§l[АКТИВНЫЙ НАВЫК: КАТАКЛИЗМ ГЕНЕЗИСА] §8(ПКМ)"
  },
  "item.awakening_system.creator_claymore.active_desc1": {
    en: "§7▪ Deploys two 18-block violet cosmic runic circles.",
    es: "§7▪ Despliega dos círculos rúnicos cósmicos violetas de 18 bloques.",
    ru: "§7▪ Разворачивает два 18-блочных фиолетовых космических рунических круга."
  },
  "item.awakening_system.creator_claymore.active_desc2": {
    en: "§7▪ Detonates a cosmic cataclysm dealing §e160 True Damage§7.",
    es: "§7▪ Detona un cataclismo cósmico infligiendo §e160 de Daño Verdadero§7.",
    ru: "§7▪ Вызывает космический катаклизм, нанося §e160 чистого урона§7."
  },
  "item.awakening_system.creator_claymore.active_desc3": {
    en: "§7▪ Pulls enemies in and launches them airborne with inverse gravity.",
    es: "§7▪ Atrae a los enemigos y los lanza por los aires con gravedad inversa.",
    ru: "§7▪ Притягивает врагов и подбрасывает в воздух обратной гравитацией."
  },
  "item.awakening_system.creator_claymore.active_desc4": {
    en: "§7▪ Grants user §6Temporary Immunity (Resistance V)§7, §eAbsorption V§7 & §cStrength IV§7.",
    es: "§7▪ Otorga al usuario §6Inmunidad Temporal (Resistencia V)§7, §eAbsorción V§7 y §cFuerza IV§7.",
    ru: "§7▪ Дарует §6временный иммунитет (Сопротивление V)§7, §eПоглощение V§7 и §cСилу IV§7."
  },
  "item.awakening_system.creator_claymore.active_cd": {
    en: "§8▪ Cooldown: §f30s.",
    es: "§8▪ Enfriamiento: §f30s.",
    ru: "§8▪ Кулдаун: §f30с."
  },
  "item.awakening_system.creator_claymore.jackpot": {
    en: "§5§l[SUPREME GACHA JACKPOT] §dUltra-secret chance: §e0.008%",
    es: "§5§l[GACHA JACKPOT SUPREMO] §dProbabilidad ultra secreta: §e0.008%",
    ru: "§5§l[ВЫСШИЙ ДЖЕКПОТ ГАЧИ] §dУльтра-секретный шанс: §e0.008%"
  },
  "item.awakening_system.creator_claymore.msg": {
    en: "§6§l👑 [JUDGMENT OF GENESIS] §fThe Creator's supreme authority disintegrates reality! %s enemies purged with 160 true damage.",
    es: "§6§l👑 [JUICIO DEL GÉNESIS] §f¡La autoridad suprema del Creador desintegra la realidad! %s enemigos purgados con 160 de daño verdadero.",
    ru: "§6§l👑 [СУД ГЕНЕЗИСА] §fВысшая власть Создателя расщепляет реальность! %s врагов уничтожено со 160 чистого урона."
  }
};

const dreadScythe = {
  "item.awakening_system.dread_scythe.header": {
    en: "§5§l✦ SHADOW ARCANE WEAPON // MONARCH RANK ✦",
    es: "§5§l✦ ARMA ARCANO DE LAS SOMBRAS // RANGO MONARCA ✦",
    ru: "§5§l✦ ТЕНЕВОЕ ТАИНСТВЕННОЕ ОРУЖИЕ // РАНГ МОНАРХА ✦"
  },
  "item.awakening_system.dread_scythe.title": {
    en: "§d§lDread Knight's Scythe",
    es: "§d§lGuadaña del Caballero del Terror",
    ru: "§d§lКоса Рыцаря Ужаса"
  },
  "item.awakening_system.dread_scythe.desc1": {
    en: "§7Forged with abyssal obsidian and the spectral remains of an ancient monarch.",
    es: "§7Forjada con obsidiana abisal y los restos espectrales de un antiguo soberano.",
    ru: "§7Выкована из обсидиана Бездны и призрачных останков древнего владыки."
  },
  "item.awakening_system.dread_scythe.desc2": {
    en: "§8Its blade emits a deadly cold that chills the soul of all who gaze upon it.",
    es: "§8Su filo emite un frío mortal que hiela el alma de quienes lo contemplan.",
    ru: "§8Ее лезвие источает смертоносный холод, леденящий души смотрящих на нее."
  },
  "item.awakening_system.dread_scythe.attr_header": {
    en: "§5§l[DREAD ATTRIBUTES]",
    es: "§5§l[ATRIBUTOS DEL TERROR]",
    ru: "§5§l[АТРИБУТЫ УЖАСА]"
  },
  "item.awakening_system.dread_scythe.attr_dmg": {
    en: "§c▪ Base Damage: §f29 §8(+12 Shadow Damage)",
    es: "§c▪ Daño Base: §f29 §8(+12 Daño de Sombra)",
    ru: "§c▪ Базовый урон: §f29 §8(+12 теневой урон)"
  },
  "item.awakening_system.dread_scythe.attr_speed": {
    en: "§e▪ Attack Speed: §f1.2 §8(Fast Scythe)",
    es: "§e▪ Velocidad de Ataque: §f1.2 §8(Hoz Rápida)",
    ru: "§e▪ Скорость атаки: §f1.2 §8(Быстрая коса)"
  },
  "item.awakening_system.dread_scythe.attr_dura": {
    en: "§b▪ Durability: §a45,000 §8(Fire Immune)",
    es: "§b▪ Durabilidad: §a45,000 §8(Inmune al Fuego)",
    ru: "§b▪ Прочность: §a45,000 §8(Огнестойкая)"
  },
  "item.awakening_system.dread_scythe.passive_header": {
    en: "§d§l[PASSIVE: SOUL HARVEST]",
    es: "§d§l[PASIVA: COSECHA DE ALMAS]",
    ru: "§d§l[ПАССИВНО: ЖАТВА ДУШ]"
  },
  "item.awakening_system.dread_scythe.passive_desc1": {
    en: "§7▪ Each strike deals §d+12 shadow damage §7and inflicts §8Wither II§7.",
    es: "§7▪ Cada impacto causa §d+12 de daño de sombra §7e inflige §8Wither II§7.",
    ru: "§7▪ Каждый удар наносит §d+12 теневого урона §7и накладывает §8Иссушение II§7."
  },
  "item.awakening_system.dread_scythe.passive_desc2": {
    en: "§7▪ Drains target's vitality, healing wielder for §c+1 Heart§7.",
    es: "§7▪ Drena la vitalidad del objetivo, curando al portador §c+1 Corazón§7.",
    ru: "§7▪ Вытягивает жизненную силу, исцеляя владельца на §c+1 сердце§7."
  },
  "item.awakening_system.dread_scythe.passive_desc3": {
    en: "§7▪ Slaying an enemy releases their soul, granting §6Strength II §7& §bSpeed II§7.",
    es: "§7▪ Matar a un enemigo libera su alma, otorgando §6Fuerza II §7y §bVelocidad II§7.",
    ru: "§7▪ Убийство врага освобождает его душу, даруя §6Силу II §7и §bСкорость II§7."
  },
  "item.awakening_system.dread_scythe.active_header": {
    en: "§5§l[ACTIVE SKILL: VORTEX OF TERROR] §8(Right Click)",
    es: "§5§l[HABILIDAD ACTIVA: VÓRTICE DEL TERROR] §8(Clic Derecho)",
    ru: "§5§l[АКТИВНЫЙ НАВЫК: ВИХРЬ УЖАСА] §8(ПКМ)"
  },
  "item.awakening_system.dread_scythe.active_desc1": {
    en: "§7▪ Deploys a whirlpool of souls and amethyst runes in a 12-block radius.",
    es: "§7▪ Despliega un remolino de almas y runas amatistas en un radio de 12 bloques.",
    ru: "§7▪ Разворачивает водоворот душ и аметистовых рун в радиусе 12 блоков."
  },
  "item.awakening_system.dread_scythe.active_desc2": {
    en: "§7▪ Pulls enemies in and inflicts §c58 necrotic damage§7.",
    es: "§7▪ Atrae a los enemigos y les inflige §c58 de daño necrótico§7.",
    ru: "§7▪ Притягивает врагов и наносит §c58 некротического урона§7."
  },
  "item.awakening_system.dread_scythe.active_desc3": {
    en: "§7▪ Absorbs life per victim and grants §dRegeneration II§7.",
    es: "§7▪ Absorbe vida por cada víctima y otorga §dRegeneración II§7.",
    ru: "§7▪ Поглощает здоровье каждой жертвы, даруя §dРегенерацию II§7."
  },
  "item.awakening_system.dread_scythe.active_cd": {
    en: "§8▪ Cooldown: §f30s.",
    es: "§8▪ Enfriamiento: §f30s.",
    ru: "§8▪ Кулдаун: §f30с."
  },
  "item.awakening_system.dread_scythe.msg": {
    en: "§5§l✦ SOUL HARVESTED! §dTerror empowers your strength and speed.",
    es: "§5§l✦ ¡ALMA COSECHADA! §dEl terror potencia tu fuerza y velocidad.",
    ru: "§5§l✦ ДУША ПОЖАТА! §dУжас усиливает вашу мощь и скорость."
  }
};

const genshinBow = {
  "item.awakening_system.genshin_bow.base_dmg": {
    en: "§7Base Shot Damage: §a+%s",
    es: "§7Daño Base de Disparo: §a+%s",
    ru: "§7Базовый урон выстрела: §a+%s"
  },
  "item.awakening_system.genshin_bow.passive_header": {
    en: "§e✦ Legendary Passive:",
    es: "§e✦ Pasiva Legendaria:",
    ru: "§e✦ Легендарная пассивная способность:"
  }
};

const genshinWeaponGeneral = {
  "item.awakening_system.genshin_weapon.star5": {
    en: "⭐⭐⭐⭐⭐ [5★ Legendary Weapon]",
    es: "⭐⭐⭐⭐⭐ [Arma Legendaria 5★]",
    ru: "⭐⭐⭐⭐⭐ [Легендарное оружие 5★]"
  },
  "item.awakening_system.genshin_weapon.star4": {
    en: "⭐⭐⭐⭐ [4★ Epic Weapon]",
    es: "⭐⭐⭐⭐ [Arma Épica 4★]",
    ru: "⭐⭐⭐⭐ [Эпическое оружие 4★]"
  },
  "item.awakening_system.genshin_weapon.star3": {
    en: "⭐⭐⭐ [3★ Adventurer Weapon]",
    es: "⭐⭐⭐ [Arma de Aventurero 3★]",
    ru: "⭐⭐⭐ [Оружие искателя приключений 3★]"
  },
  "item.awakening_system.genshin_weapon.refine_rank": {
    en: "§e✦ Refinement Rank: §6R%s§8/R5",
    es: "§e✦ Rango de Refinamiento: §6R%s§8/R5",
    ru: "§e✦ Ранг пробуждения: §6R%s§8/R5"
  },
  "item.awakening_system.genshin_weapon.refine_hint": {
    en: "  §8▸ Shift + Right Click with duplicate in offhand: §6%s Mora §8(R%s)",
    es: "  §8▸ Shift + Clic Der con duplicado en mano secundaria: §6%s Mora §8(R%s)",
    ru: "  §8▸ Shift + ПКМ с копией в левой руке: §6%s Моры §8(R%s)"
  },
  "item.awakening_system.genshin_weapon.refine_max": {
    en: "  §a✔ Max Refinement Rank Reached (R5)",
    es: "  §a✔ Rango Máximo de Refinamiento Alcanzado (R5)",
    ru: "  §a✔ Достигнут максимальный ранг пробуждения (R5)"
  }
};

const allDicts = [wlm, soulSolid, staffOfHoma, primogemSword, laDoncella, creatorClaymore, dreadScythe, genshinBow, genshinWeaponGeneral];

const data = { en: {}, es: {}, ru: {} };
for (const dict of allDicts) {
  for (const [key, trans] of Object.entries(dict)) {
    data.en[key] = trans.en;
    data.es[key] = trans.es;
    data.ru[key] = trans.ru;
  }
}

fs.writeFileSync('scripts/item_translations_weapons.json', JSON.stringify(data, null, 2), 'utf8');
console.log('Saved weapons translations:', Object.keys(data.en).length, 'keys');
