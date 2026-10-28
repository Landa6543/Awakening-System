const fs = require('fs');
const path = require('path');

// 1. DICTIONARY OF ALL REMAINING ITEMS
const translations = {
  // --- DOCTOR'S ROBE (Curio) ---
  "item.awakening_system.doctors_robe.title": {
    en: "§5§l['Curio' Doctor's Robe]",
    es: "§5§l['Curio' Manto del Doctor]",
    ru: "§5§l[«Диковина» Мантия доктора]"
  },
  "item.awakening_system.doctors_robe.desc": {
    en: "§7Cosmic scholar robe from the Simulated Universe.",
    es: "§7Vestidura de erudito del cosmos proveniente del Universo Simulado.",
    ru: "§7Одеяние ученого космоса из Виртуальной вселенной."
  },
  "item.awakening_system.doctors_robe.skill_title": {
    en: "§a✦ Sage's Channeling:",
    es: "§a✦ Canalización del Sabio:",
    ru: "§a✦ Канализация мудреца:"
  },
  "item.awakening_system.doctors_robe.cond": {
    en: "§7  Upon taking severe damage (HP below 50%):",
    es: "§7  Al recibir daño severo (vida por debajo del 50%):",
    ru: "§7  При получении тяжелого урона (HP ниже 50%):"
  },
  "item.awakening_system.doctors_robe.effect1": {
    en: "§2  • Instantly restores 100% of your Mana and Qi.",
    es: "§2  • Regenera instantáneamente el 100% de tu Maná y Qi.",
    ru: "§2  • Мгновенно восстанавливает 100% маны и Ци."
  },
  "item.awakening_system.doctors_robe.effect2": {
    en: "§e  • Grants 6 golden hearts of Absorption and Regeneration.",
    es: "§e  • Otorga 6 corazones dorados de Absorción y Regeneración.",
    ru: "§e  • Дарует 6 золотых сердец поглощения и регенерацию."
  },
  "item.awakening_system.doctors_robe.cd": {
    en: "§8  (Cooldown: 60 seconds)",
    es: "§8  (Tiempo de recarga: 60 segundos)",
    ru: "§8  (Перезарядка: 60 секунд)"
  },

  // --- ROBE OF THE BEAUTY (Curio) ---
  "item.awakening_system.robe_of_the_beauty.badge": {
    en: "§d⭐⭐⭐⭐⭐ [Simulated Universe Curio]",
    es: "§d⭐⭐⭐⭐⭐ [Curio del Universo Simulado]",
    ru: "§d⭐⭐⭐⭐⭐ [Диковина Виртуальной вселенной]"
  },
  "item.awakening_system.robe_of_the_beauty.desc": {
    en: "§7Majestic celestial robe blessed by Idrila, the Cosmic Beauty.",
    es: "§7Un majestuoso manto celestial bendecido por Idrila, la Belleza Cósmica.",
    ru: "§7Величественная мантия, благословленная Идрилой, Космической Красотой."
  },
  "item.awakening_system.robe_of_the_beauty.passive_title": {
    en: "§6✦ Passive Effect: Robe of Beauty",
    es: "§6✦ Efecto Pasivo: El Manto de la Belleza",
    ru: "§6✦ Пассивный эффект: Мантия Красоты"
  },
  "item.awakening_system.robe_of_the_beauty.passive_desc1": {
    en: "  §7For every §e100 Cosmic Fragments §7in your inventory,",
    es: "  §7Por cada §e100 Fragmentos Cósmicos §7en tu inventario,",
    ru: "  §7За каждые §e100 Космических фрагментов §7в инвентаре,"
  },
  "item.awakening_system.robe_of_the_beauty.passive_desc2": {
    en: "  §7your outgoing damage increases by §a+16% §7(max §a+80%§7).",
    es: "  §7tu daño infligido aumenta en §a+16% §7(máx. §a+80%§7).",
    ru: "  §7ваш наносимый урон увеличивается на §a+16% §7(макс. §a+80%§7)."
  },
  "item.awakening_system.robe_of_the_beauty.preservation_title": {
    en: "§d✦ Sacred Preservation:",
    es: "§d✦ Preservación Sagrada:",
    ru: "§d✦ Священное сохранение:"
  },
  "item.awakening_system.robe_of_the_beauty.preservation_desc1": {
    en: "  §7Upon taking fatal or critical damage, consumes §e30 Cosmic Fragments",
    es: "  §7Al recibir daño crítico, consume §e30 Fragmentos Cósmicos",
    ru: "  §7При получении критического урона расходует §e30 Космических фрагментов,"
  },
  "item.awakening_system.robe_of_the_beauty.preservation_desc2": {
    en: "  §7to mitigate 50% damage and grant brief invulnerability.",
    es: "  §7para mitigar el 50% del daño y ganar invulnerabilidad breve.",
    ru: "  §7снижая урон на 50% и даруя кратковременную неуязвимость."
  },

  // --- WILDFIRE CROWN ---
  "item.awakening_system.wildfire_crown.title": {
    en: "§6✦ Crown of Wildfire Boss",
    es: "§6✦ Corona del Jefe Wildfire",
    ru: "§6✦ Корона босса лесного пожара"
  },
  "item.awakening_system.wildfire_crown.lore": {
    en: "§7\"Channels the fiery essence of the Nether's colossal flame colossus.\"",
    es: "§7\"Canaliza la esencia ardiente del coloso de fuego del Nether.\"",
    ru: "§7\"Канализирует огненную эссенцию колосса пламени из Незера.\""
  },
  "item.awakening_system.wildfire_crown.blessing_title": {
    en: "§e✦ Primordial Fire Blessing:",
    es: "§e✦ Bendición Ígnea Primordial:",
    ru: "§e✦ Первозданное огненное благословение:"
  },
  "item.awakening_system.wildfire_crown.blessing_desc": {
    en: "  §7Grants absolute and permanent immunity to fire and lava while equipped.",
    es: "  §7Otorga resistencia absoluta y permanente al fuego y a la lava mientras se lleve equipada.",
    ru: "  §7Дарует полный постоянный иммунитет к огню и лаве при ношении."
  },

  // --- TEYVAT DELUSION ---
  "item.awakening_system.teyvat_delusion.element": {
    en: "§7Fatui Element: ",
    es: "§7Elemento Fatui: ",
    ru: "§7Элемент Фатуи: "
  },
  "item.awakening_system.teyvat_delusion.active": {
    en: "§c[Right Click] §7Unleashes a destructive elemental explosion.",
    es: "§c[Click Derecho] §7Desata una explosión elemental destructiva.",
    ru: "§c[ПКМ] §7Вызывает разрушительный элементальный взрыв."
  },
  "item.awakening_system.teyvat_delusion.warn": {
    en: "§4⚠ Warning: §7Fatui's unstable power inflicts corrosion and magical damage upon activation.",
    es: "§4⚠ Advertencia: §7El poder inestable de los Fatui te causará corrosión y daño mágico al activarse.",
    ru: "§4⚠ Внимание: §7Нестабильная сила Глаза Порчи вызывает коррозию при активации."
  },
  "item.awakening_system.teyvat_delusion.blank": {
    en: "§8Dormant Delusion. Combine with Delusion Shards to forge an elemental artifact.",
    es: "§8Engaño apagado. Combínalo con Fragmentos de Engaño para forjar un artefacto elemental.",
    ru: "§8Погасший Глаз Порчи. Объедините с осколками, чтобы выковать элементальный артефакт."
  },

  // --- TEYVAT VISION ---
  "item.awakening_system.teyvat_vision.badge": {
    en: "§6⭐⭐⭐⭐⭐ [Teyvat Elemental Vision - 5★]",
    es: "§6⭐⭐⭐⭐⭐ [Visión Elemental de Teyvat - 5★]",
    ru: "§6⭐⭐⭐⭐⭐ [Элементальный Глаз Бога Тейвата - 5★]"
  },
  "item.awakening_system.teyvat_vision.desc": {
    en: "§7Channeler granted by the Celestial Archons of Genshin Impact.",
    es: "§7Canalizador otorgado por los Arcontes Celestiales de Genshin Impact.",
    ru: "§7Проводник, дарованный Небесными Архонтами Genshin Impact."
  },
  "item.awakening_system.teyvat_vision.element": {
    en: "§e▸ Element: ",
    es: "§e▸ Elemento: ",
    ru: "§e▸ Элемент: "
  },
  "item.awakening_system.teyvat_vision.resonance": {
    en: "§b▸ Resonance Effect: §fChannels attacks and elemental reactions when equipped in the System.",
    es: "§b▸ Efecto de Resonancia: §fCanaliza ataques y reacciones del elemento al equiparse en el Sistema.",
    ru: "§b▸ Эффект резонанса: §fПроводит элементальные атаки и реакции при экипировке в Системе."
  },

  // --- POTIONS & ELIXIRS ---
  "item.awakening_system.vigor_potion.title": {
    en: "§6✦ Potion of Arcane Vigor ✦",
    es: "§6✦ Poción de Vigor Arcano ✦",
    ru: "§6✦ Зелье тайной бодрости ✦"
  },
  "item.awakening_system.vigor_potion.desc": {
    en: "§7Alchemical concoction distilled from pure essences.",
    es: "§7Brebaje alquímico destilado de esencias puras.",
    ru: "§7Алхимическое варево из чистейших эссенций."
  },
  "item.awakening_system.vigor_potion.effect": {
    en: "§e[Right Click] §fRestores +100 Energy and grants Regeneration, Speed & Strength.",
    es: "§e[Clic Derecho] §fRestaura +100 Energía y otorga Regeneración, Velocidad y Fuerza.",
    ru: "§e[ПКМ] §fВосстанавливает +100 энергии и дает Регенерацию, Скорость и Силу."
  },

  "item.awakening_system.olive_elixir.title": {
    en: "§b✦ Elixir of the Mystic Olive ✦",
    es: "§b✦ Elixir de Olivo Místico ✦",
    ru: "§b✦ Эликсир мистической оливы ✦"
  },
  "item.awakening_system.olive_elixir.desc": {
    en: "§7Miraculous extract blessed by the sacred bough.",
    es: "§7Un extracto milagroso bendecido por la rama del árbol sagrado.",
    ru: "§7Чудесный экстракт священной ветви."
  },
  "item.awakening_system.olive_elixir.effect": {
    en: "§e[Right Click] §fResets all attributes and refunds invested stat points.",
    es: "§e[Clic Derecho] §fRestablece todos los atributos y devuelve los puntos invertidos.",
    ru: "§e[ПКМ] §fСбрасывает все характеристики и возвращает очки параметров."
  },

  "item.awakening_system.constellation_nectar.title": {
    en: "§d✦ Constellation Nectar ✦",
    es: "§d✦ Néctar de las Constelaciones ✦",
    ru: "§d✦ Нектар созвездий ✦"
  },
  "item.awakening_system.constellation_nectar.desc": {
    en: "§7Elixir distilled from the astral streams of the Great Dao.",
    es: "§7Elixir destilado de los arroyos estelares del Gran Dao.",
    ru: "§7Эликсир из звездных потоков Великого Дао."
  },
  "item.awakening_system.constellation_nectar.effect1": {
    en: "§e[Hold Right Click] §fPurifies energy channels and",
    es: "§e[Mantener Clic Derecho] §fPurifica los canales energéticos y",
    ru: "§e[Зажать ПКМ] §fОчищает энергетические каналы и"
  },
  "item.awakening_system.constellation_nectar.effect2": {
    en: "§a§lResets your Murim Constitution / Physique §fto choose a new one.",
    es: "§a§lRestablece tu Constitución / Físico Murim §fpara elegir uno nuevo.",
    ru: "§a§lСбрасывает телосложение Мурима §fдля выбора нового пути."
  },
  "item.awakening_system.constellation_nectar.effect3": {
    en: "§bGrants Celestial Regeneration and Absorption.",
    es: "§bOtorga Regeneración y Absorción Celestial.",
    ru: "§bДарует небесную регенерацию и поглощение."
  },

  // --- UMA MUSUME ITEMS ---
  "item.awakening_system.uma.alarm_clock.title": {
    en: "§6⏰ Tracen Academy Rescue Alarm Clock",
    es: "§6⏰ Despertador de Rescate de Tracen Academy",
    ru: "§6⏰ Спасительный будильник Академии Трейсен"
  },
  "item.awakening_system.uma.alarm_clock.desc": {
    en: "§7A rescue talisman ensuring you never slip into eternal sleep.",
    es: "§7Un talismán de rescate que no te dejará caer en el sueño eterno.",
    ru: "§7Талисман спасения, не дающий уснуть вечным сном."
  },
  "item.awakening_system.uma.alarm_clock.mech_header": {
    en: "§e✦ Survival Mechanic:",
    es: "§e✦ Mecánica de Supervivencia:",
    ru: "§e✦ Механика выживания:"
  },
  "item.awakening_system.uma.alarm_clock.mech_desc1": {
    en: "  §b• Consumed upon taking fatal damage to cancel death",
    es: "  §b• Al recibir daño letal se consume para anular la muerte",
    ru: "  §b• Расходуется при смертельном уроне, отменяя гибель"
  },
  "item.awakening_system.uma.alarm_clock.mech_desc2": {
    en: "  §a• Restores 100% Health, gives brief invulnerability and cleanses debuffs",
    es: "  §a• Restaura el 100% de Salud, da invulnerabilidad breve y limpia desventajas",
    ru: "  §a• Восстанавливает 100% HP, дает неуязвимость и очищает дебаффы"
  },
  "item.awakening_system.uma.alarm_clock.hint": {
    en: "§8Works from your inventory or curio slot without needing to be held.",
    es: "§8Funciona desde el inventario o curio sin necesidad de tenerlo en mano.",
    ru: "§8Срабатывает из инвентаря или слота диковины."
  },

  "item.awakening_system.uma.ankle_weights.title": {
    en: "§8⚙ Forged Lead Weights for Tracen Racers.",
    es: "§8⚙ Pesas de plomo forjado diseñadas para fortalecer las piernas de las corredoras.",
    ru: "§8⚙ Свинцовые утяжелители для бегуний Трейсен."
  },
  "item.awakening_system.uma.ankle_weights.desc": {
    en: "§7A staple of rigorous training at Tracen Academy.",
    es: "§7Un clásico del entrenamiento riguroso de la Academia Tracen.",
    ru: "§7Классика суровых тренировок Академии Трейсен."
  },
  "item.awakening_system.uma.ankle_weights.header": {
    en: "§e✦ While equipped on boots / feet:",
    es: "§e✦ Mientras están equipadas en botas:",
    ru: "§e✦ При экипировке на обувь:"
  },
  "item.awakening_system.uma.ankle_weights.effect1": {
    en: "  §c• Slowness I (Rigorous high-resistance workout)",
    es: "  §c• Lentitud I (Entrenamiento riguroso de resistencia)",
    ru: "  §c• Замедление I (Тяжелая силовая тренировка)"
  },
  "item.awakening_system.uma.ankle_weights.effect2": {
    en: "  §a• +50% Additional EXP from running, sprinting & combat",
    es: "  §a• +50% EXP adicional al correr, esprintar y combatir",
    ru: "  §a• +50% доп. опыта при беге, спринте и в бою"
  },
  "item.awakening_system.uma.ankle_weights.hint": {
    en: "§8Unlocks elite physical prowess once removed.",
    es: "§8Retíralas tras una sesión para sentir una aceleración explosiva.",
    ru: "§8Снимите после забега, чтобы ощутить взрывную скорость."
  },

  "item.awakening_system.uma.bitter_juice.title": {
    en: "§2🧪 Concentrated Vegetable Tonic for race training.",
    es: "§2🧪 Tónico amargo de verduras concentradas para potenciar el entrenamiento.",
    ru: "§2🧪 Горький концентрированный овощной тоник для тренировок."
  },
  "item.awakening_system.uma.bitter_juice.desc": {
    en: "§7Extreme formula favored by Tracen Academy trainers.",
    es: "§7Fórmula extrema de los entrenadores de la Academia Tracen.",
    ru: "§7Экстремальная формула тренеров Академии Трейсен."
  },
  "item.awakening_system.uma.bitter_juice.header": {
    en: "§e✦ Training Surge Effects (2:00 min):",
    es: "§e✦ Efectos de Estímulo en Entrenamiento (2:00 min):",
    ru: "§e✦ Эффекты тренировочного прилива (2:00 мин):"
  },
  "item.awakening_system.uma.bitter_juice.effect1": {
    en: "  §c• Strength II",
    es: "  §c• Fuerza II",
    ru: "  §c• Сила II"
  },
  "item.awakening_system.uma.bitter_juice.effect2": {
    en: "  §e• Haste II (Quick reflexes & work rate)",
    es: "  §e• Celeridad II (Reflejos y trabajo rápido)",
    ru: "  §e• Спешка II (Быстрые рефлексы)"
  },
  "item.awakening_system.uma.bitter_juice.effect3": {
    en: "  §b• Jump Boost I",
    es: "  §b• Salto Mejorado I",
    ru: "  §b• Прыгучесть I"
  },
  "item.awakening_system.uma.bitter_juice.penalty_header": {
    en: "§c⚠ Penalty upon expiry (1:00 min):",
    es: "§c⚠ Penalización al expirar (1:00 min):",
    ru: "§c⚠ Откат при окончании (1:00 мин):"
  },
  "item.awakening_system.uma.bitter_juice.penalty_desc": {
    en: "  §7• Hunger II and Weakness I from intense exhaustion",
    es: "  §7• Hambre II y Debilidad I por agotamiento",
    ru: "  §7• Голод II и Слабость I от переутомления"
  },

  "item.awakening_system.uma.clover_spark.title": {
    en: "§a🍀 Tracen Academy Lucky Clover (Spark Points).",
    es: "§a🍀 Trébol de la Suerte de la Academia Tracen (Puntos de Spark).",
    ru: "§a🍀 Счастливый клевер Академии Трейсен (Очки спарка)."
  },
  "item.awakening_system.uma.clover_spark.desc": {
    en: "§7Special compensation currency awarded from gacha pulls.",
    es: "§7Divisa especial de compensación otorgada en las tiradas del gacha.",
    ru: "§7Особая валюта компенсации за крутки в гаче."
  },
  "item.awakening_system.uma.clover_spark.header": {
    en: "§e✦ Exchange Usage:",
    es: "§e✦ Uso en Intercambio:",
    ru: "§e✦ Применение в обмене:"
  },
  "item.awakening_system.uma.clover_spark.desc1": {
    en: "  §f• Accumulate to guarantee celestial Genshin weapons",
    es: "  §f• Acumúlalos para canjear de forma garantizada armas celestiales de Genshin",
    ru: "  §f• Накопите для гарантированного обмена на оружие Геншина"
  },
  "item.awakening_system.uma.clover_spark.desc2": {
    en: "  §f• Exchange for legendary Uma Musume talismans & items",
    es: "  §f• Permite comprar amuletos y objetos legendarios de Uma Musume",
    ru: "  §f• Позволяет покупать легендарные амулеты Ума Мусуме"
  },
  "item.awakening_system.uma.clover_spark.hint": {
    en: "§8Guarantees that no wish or pull goes in vain!",
    es: "§8¡Garantiza que ninguna tirada sea en vano!",
    ru: "§8Гарантирует, что ни одна крутка не пропадет даром!"
  },

  "item.awakening_system.uma.dark_stamina.title": {
    en: "§5☠ Prohibited substance distilled in the underworld alleys.",
    es: "§5☠ Sustancia prohibida destilada en los callejones del bajo mundo.",
    ru: "§5☠ Запрещенное вещество из подворотен преступного мира."
  },
  "item.awakening_system.uma.dark_stamina.desc": {
    en: "§7Clandestine formula regularly confiscated at the racetracks.",
    es: "§7Fórmula clandestina confiscada recurrentemente en el hipódromo.",
    ru: "§7Подпольная формула, изымаемая на ипподроме."
  },
  "item.awakening_system.uma.dark_stamina.header": {
    en: "§e✦ Extreme Performance Effects (3:00 min):",
    es: "§e✦ Efectos de Rendimiento Extremo (3:00 min):",
    ru: "§e✦ Эффекты пиковой формы (3:00 мин):"
  },
  "item.awakening_system.uma.dark_stamina.effect1": {
    en: "  §d• Speed III (+60% Sprint Speed)",
    es: "  §d• Velocidad III (+60% Velocidad de carrera)",
    ru: "  §d• Скорость III (+60% скорости бега)"
  },
  "item.awakening_system.uma.dark_stamina.effect2": {
    en: "  §b• Jump Boost II",
    es: "  §b• Salto Mejorado II",
    ru: "  §b• Прыгучесть II"
  },
  "item.awakening_system.uma.dark_stamina.effect3": {
    en: "  §c• Strength I",
    es: "  §c• Fuerza I",
    ru: "  §c• Сила I"
  },
  "item.awakening_system.uma.dark_stamina.penalty_header": {
    en: "§c⚠ Clandestine Crash Hangover (0:45 min after expiry):",
    es: "§c⚠ Rebote de Resaca Clandestina (0:45 min al expirar):",
    ru: "§c⚠ Подпольное похмелье (0:45 мин при окончании):"
  },
  "item.awakening_system.uma.dark_stamina.penalty_desc": {
    en: "  §7• Severe exhaustion: Slowness I, Weakness II & Hunger II",
    es: "  §7• Agotamiento extremo: Lentitud I, Debilidad II y Hambre II",
    ru: "  §7• Сильное истощение: Замедление I, Слабость II и Голод II"
  },

  "item.awakening_system.uma.elite_carat.title": {
    en: "§6🥕 Special Grade Carrot harvested in Tracen orchards.",
    es: "§6🥕 Zanahoria de Grado Especial cosechada en los huertos de Tracen.",
    ru: "§6🥕 Морковь высшего сорта из садов Трейсен."
  },
  "item.awakening_system.uma.elite_carat.desc": {
    en: "§7High-competition nutrient concentrate for racers.",
    es: "§7Concentrado de nutrientes de alta competición para corredoras.",
    ru: "§7Высококонцентрированный питательный рацион чемпионок."
  },
  "item.awakening_system.uma.elite_carat.header": {
    en: "§e✦ Consumption Effects:",
    es: "§e✦ Efectos al consumir:",
    ru: "§e✦ Эффекты при употреблении:"
  },
  "item.awakening_system.uma.elite_carat.effect1": {
    en: "  §b• Speed III §7(4:00 min)",
    es: "  §b• Velocidad III §7(4:00 min)",
    ru: "  §b• Скорость III §7(4:00 мин)"
  },
  "item.awakening_system.uma.elite_carat.effect2": {
    en: "  §a• Jump Boost II §7(4:00 min)",
    es: "  §a• Salto Elevado II §7(4:00 min)",
    ru: "  §a• Прыгучесть II §7(4:00 мин)"
  },
  "item.awakening_system.uma.elite_carat.hint": {
    en: "§8Ideal for rapid terrain exploration and cave mountaineering.",
    es: "§8Ideal para exploración rápida de terreno y escalada en cavernas.",
    ru: "§8Идеально для быстрой разведки местности и пещер."
  },

  "item.awakening_system.uma.gacha_ticket.title": {
    en: "§a🎟 Official Invitation Ticket for the Tracen Academy Gacha.",
    es: "§a🎟 Boleto de invitación oficial para el Gacha de la Academia Tracen.",
    ru: "§a🎟 Официальный билет приглашения в гачу Академии Трейсен."
  },
  "item.awakening_system.uma.gacha_ticket.desc": {
    en: "§7Equivalent to the exact cost of a single recruitment summon.",
    es: "§7Equivale al valor exacto de una invocación individual.",
    ru: "§7Эквивалентен стоимости одной крутки призыва."
  },
  "item.awakening_system.uma.gacha_ticket.use1": {
    en: "§e▸ Right Click: §fRedeem 1 ticket (§e+150 🥕§f)",
    es: "§e▸ Clic derecho: §fCanjear 1 boleto (§e+150 🥕§f)",
    ru: "§e▸ ПКМ: §fОбменять 1 билет (§e+150 🥕§f)"
  },
  "item.awakening_system.uma.gacha_ticket.use2": {
    en: "§e▸ Shift + Right Click: §fRedeem entire stack",
    es: "§e▸ Shift + Clic derecho: §fCanjear todo el stack",
    ru: "§e▸ Shift + ПКМ: §fОбменять всю стопку"
  },

  "item.awakening_system.uma.golden_ticket.title": {
    en: "§6✨ Gala Golden Pass for 10 Simultaneous Invocations at Tracen.",
    es: "§6✨ Pase dorado de gala para 10 Invocaciones simultáneas en Tracen.",
    ru: "§6✨ Золотой гала-пропуск на 10 одновременных призывов в Трейсен."
  },
  "item.awakening_system.uma.golden_ticket.desc": {
    en: "§7Guarantees a complete recruitment session with high-rarity rate-up.",
    es: "§7Garantiza una sesión de reclutamiento completa con opción a Umas de alta rareza.",
    ru: "§7Гарантирует полноценную сессию призыва с шансом на редких героинь."
  },
  "item.awakening_system.uma.golden_ticket.use1": {
    en: "§e▸ Right Click: §fRedeem 1 golden pass (§6+1500 🥕 §7- 10 Pulls§f)",
    es: "§e▸ Clic derecho: §fCanjear 1 boleto de oro (§6+1500 🥕 §7- 10 Tiradas§f)",
    ru: "§e▸ ПКМ: §fОбменять 1 золотой билет (§6+1500 🥕 §7- 10 круток§f)"
  },
  "item.awakening_system.uma.golden_ticket.use2": {
    en: "§e▸ Shift + Right Click: §fRedeem entire stack",
    es: "§e▸ Shift + Clic derecho: §fCanjear todo el stack",
    ru: "§e▸ Shift + ПКМ: §fОбменять всю стопку"
  },

  "item.awakening_system.uma.gold_horseshoe.title": {
    en: "§6✨ Golden Horseshoe consecrated to Hall of Fame legends.",
    es: "§6✨ Herradura dorada consagrada a las leyendas del Salón de la Fama.",
    ru: "§6✨ Золотая подкова, посвященная легендам Зала Славы."
  },
  "item.awakening_system.uma.gold_horseshoe.header": {
    en: "§e✦ Passive Effect (carried in inventory or equipped on feet):",
    es: "§e✦ Efecto Pasivo (con llevarla en el inventario o pies):",
    ru: "§e✦ Пассивный эффект (в инвентаре или на ногах):"
  },
  "item.awakening_system.uma.gold_horseshoe.effect1": {
    en: "  §b• +25% Base Movement Speed",
    es: "  §b• +25% Velocidad de Movimiento base",
    ru: "  §b• +25% базовой скорости движения"
  },
  "item.awakening_system.uma.gold_horseshoe.effect2": {
    en: "  §a• Complete Fall Damage Immunity §7(Infinite Feather Falling)",
    es: "  §a• Inmunidad Total al daño por caída §7(Caída de Pluma infinita)",
    ru: "  §a• Полный иммунитет к урону от падения §7(Вечное плавное падение)"
  },
  "item.awakening_system.uma.gold_horseshoe.hint": {
    en: "§8Enables descending from extreme heights without taking any injury.",
    es: "§8Permite descender de grandes alturas sin recibir daño alguno.",
    ru: "§8Позволяет прыгать с любой высоты без малейшего урона."
  },

  "item.awakening_system.uma.silver_horseshoe.title": {
    en: "§7✦ Pure Silver Horseshoe blessed by the Goddess of Victory.",
    es: "§7✦ Herradura forjada con plata pura bendecida por la Diosa de la Victoria.",
    ru: "§7✦ Подкова из чистого серебра, благословленная Богиней Победы."
  },
  "item.awakening_system.uma.silver_horseshoe.header": {
    en: "§e✦ Passive Effect (carried in inventory or equipped on feet):",
    es: "§e✦ Efecto Pasivo (con llevarla en el inventario o pies):",
    ru: "§e✦ Пассивный эффект (в инвентаре или на ногах):"
  },
  "item.awakening_system.uma.silver_horseshoe.effect1": {
    en: "  §b• +15% Base Movement Speed",
    es: "  §b• +15% Velocidad de Movimiento base",
    ru: "  §b• +15% базовой скорости движения"
  },
  "item.awakening_system.uma.silver_horseshoe.hint": {
    en: "§8Harmonizes stride rhythm for smooth, agile traveling.",
    es: "§8Armoniza la zancada permitiendo un desplazamiento veloz y fluido.",
    ru: "§8Гармонизирует шаг, обеспечивая плавный и быстрый бег."
  },

  "item.awakening_system.uma.pleasing_parfait.title": {
    en: "§d🍨 The most coveted championship dessert after winning a G1 race.",
    es: "§d🍨 El postre más codiciado por las Umas tras ganar una carrera G1.",
    ru: "§d🍨 Заветный чемпионский десерт после победы в гонке G1."
  },
  "item.awakening_system.uma.pleasing_parfait.desc": {
    en: "§7Legendary culinary delight revitalizing body and spirit.",
    es: "§7Un deleite culinario legendario que renueva cuerpo y alma.",
    ru: "§7Легендарное лакомство, исцеляющее тело и душу."
  },
  "item.awakening_system.uma.pleasing_parfait.header": {
    en: "§e✦ Consumption Effects:",
    es: "§e✦ Efectos al consumir:",
    ru: "§e✦ Эффекты при употреблении:"
  },
  "item.awakening_system.uma.pleasing_parfait.effect1": {
    en: "  §a• Instant purge of all negative status effects",
    es: "  §a• Purga inmediata de todos los estados negativos",
    ru: "  §a• Мгновенное снятие всех негативных эффектов"
  },
  "item.awakening_system.uma.pleasing_parfait.effect2": {
    en: "  §c• Restores 100% of Maximum Health",
    es: "  §c• Restaura el 100% de Salud Máxima",
    ru: "  §c• Восстанавливает 100% максимального здоровья"
  },
  "item.awakening_system.uma.pleasing_parfait.effect3": {
    en: "  §6• Fully refills Hunger and Saturation bars",
    es: "  §6• Restaura al máximo Hambre y Saturación",
    ru: "  §6• Полностью восполняет голод и насыщение"
  },
  "item.awakening_system.uma.pleasing_parfait.effect4": {
    en: "  §d• Regeneration II §7(0:15 min)",
    es: "  §d• Regeneración II §7(0:15 min)",
    ru: "  §d• Регенерация II §7(0:15 мин)"
  },

  "item.awakening_system.uma.training_whistle.title": {
    en: "§b📣 High-frequency metal whistle used by licensed trainers.",
    es: "§b📣 Silbato metálico de alta frecuencia utilizado por los entrenadores.",
    ru: "§b📣 Высокочастотный металлический свисток лицензированных тренеров."
  },
  "item.awakening_system.uma.training_whistle.desc": {
    en: "§7The resonant pitch resets the mindset and vocations of villagers.",
    es: "§7El sonido despierta la mente de los aldeanos y reinicia su vocación.",
    ru: "§7Звук проясняет разум жителей и сбрасывает их профессию."
  },
  "item.awakening_system.uma.training_whistle.header": {
    en: "§e✦ Right Click:",
    es: "§e✦ Clic Derecho:",
    ru: "§e✦ ПКМ:"
  },
  "item.awakening_system.uma.training_whistle.desc1": {
    en: "  §f• Resets professions, XP & trades of villagers within §b10 blocks",
    es: "  §f• Reinicia profesiones, XP y tradeos de aldeanos en un radio de §b10 bloques",
    ru: "  §f• Сбрасывает профессии, опыт и сделки жителей в радиусе §b10 блоков"
  },
  "item.awakening_system.uma.training_whistle.desc2": {
    en: "  §a• Eliminates the tedious need to break and replace job workstations",
    es: "  §a• Elimina la necesidad de romper y reemplazar mesas de trabajo",
    ru: "  §a• Избавляет от необходимости ломать и ставить рабочие места"
  },

  // --- SPECIAL / FABLE / PLUSHIE ITEMS ---
  "item.awakening_system.fable_scroll.title": {
    en: "§d✦ Core Fable Story Scroll ✦",
    es: "§d✦ Pergamino de Fábula Central ✦",
    ru: "§d✦ Свиток центральной истории сказки ✦"
  },
  "item.awakening_system.fable_scroll.desc": {
    en: "§7Cosmic story fragment forged across the Star Stream scenarios.",
    es: "§7Un cósmico fragmento de historia forjado en los escenarios del Star Stream.",
    ru: "§7Космический фрагмент истории, выкованный в сценариях Звездного Потока."
  },
  "item.awakening_system.fable_scroll.effect": {
    en: "§e[Right Click] §fUnfurl the fable to gain +5,000 System EXP & +10,000 Mora.",
    es: "§e[Clic Derecho] §fDespliega la fábula otorgando +5,000 EXP de Sistema y +10,000 Mora.",
    ru: "§e[ПКМ] §fРаскрывает историю, даруя +5,000 опыта Системы и +10,000 Моры."
  },

  "item.awakening_system.fable_star_fragment.title": {
    en: "§6✦ Fable Stellar Fragment ✦",
    es: "§6✦ Fragmento Estelar de Fábula ✦",
    ru: "§6✦ Звездный осколок истории ✦"
  },
  "item.awakening_system.fable_star_fragment.desc": {
    en: "§7A radiant crystal fallen from a legendary constellation.",
    es: "§7Un cristal radiante desprendido de una constelación legendaria.",
    ru: "§7Сияющий кристалл, отколовшийся от легендарного созвездия."
  },
  "item.awakening_system.fable_star_fragment.effect": {
    en: "§e[Right Click] §fExchanges stellar resonance for §a+3,500 EXP §fand §6+8,000 Mora.",
    es: "§e[Clic Derecho] §fCanjea la resonancia estelar por §a+3,500 EXP §fy §6+8,000 Mora.",
    ru: "§e[ПКМ] §fОбменивает звездный резонанс на §a+3,500 опыта §fи §6+8,000 Моры."
  },

  "item.awakening_system.plushie.squeeze": {
    en: "§d♥ Right click to squeeze",
    es: "§d♥ Clic derecho para apachurrar",
    ru: "§d♥ ПКМ, чтобы потискать"
  },
  "item.awakening_system.plushie.wear": {
    en: "§b👒 Sneak + Right Click to equip on head",
    es: "§b👒 Agacharse + Clic Der para equipar en la cabeza",
    ru: "§b👒 Присесть + ПКМ, чтобы надеть на голову"
  },
  "item.awakening_system.custom_plushie.owner": {
    en: "§7Owner: §e%s",
    es: "§7Dueño: §e%s",
    ru: "§7Владелец: §e%s"
  },
  "item.awakening_system.custom_plushie.hint": {
    en: "§7Adopts the skin of whoever places it, or name it on an anvil.",
    es: "§7Adopta la skin de quien lo coloque o nómbralo en un yunque.",
    ru: "§7Принимает скин того, кто его ставит, или назовите в наковальне."
  },

  "item.awakening_system.praise_of_lofty_morality.badge": {
    en: "§e⭐⭐⭐⭐ [Praise of High Morality]",
    es: "§e⭐⭐⭐⭐ [Prueba de Virtud y Decoro]",
    ru: "§e⭐⭐⭐⭐ [Похвала высокой морали]"
  },
  "item.awakening_system.praise_of_lofty_morality.desc": {
    en: "§7A shiny badge awarded to those who perform selfless acts in Belobog.",
    es: "§7Un brillante distintivo otorgado a quienes realizan actos de bondad desinteresada en Belobog.",
    ru: "§7Сияющая медаль за бескорыстные добрые поступки в Белобоге."
  },
  "item.awakening_system.praise_of_lofty_morality.use": {
    en: "§a▸ Right Click: §fConsume to receive the blessing of Health and Fortune",
    es: "§a▸ Clic derecho: §fConsumir para recibir la bendición de la Suerte y la Salud",
    ru: "§a▸ ПКМ: §fИспользовать для получения благословения Здоровья и Удачи"
  },

  "item.awakening_system.omniscience_pouch.title": {
    en: "§5§l['Curio' Omniscience Pouch]",
    es: "§5§l['Curio' Bolsa Omnisciente]",
    ru: "§5§l[«Диковина» Всеведущая сума]"
  },
  "item.awakening_system.omniscience_pouch.desc": {
    en: "§7A mythical talisman from the depths of the Simulated Universe.",
    es: "§7Un mítico talismán proveniente de las profundidades del Universo Simulado.",
    ru: "§7Мифический талисман из глубин Виртуальной вселенной."
  },
  "item.awakening_system.omniscience_pouch.header": {
    en: "§e✦ Astral Fortune:",
    es: "§e✦ Fortuna Astral:",
    ru: "§e✦ Астральная удача:"
  },
  "item.awakening_system.omniscience_pouch.desc1": {
    en: "§7  When breaking Teyvat Pots or unlocking Treasure Chests,",
    es: "§7  Al romper Vasijas de Teyvat o abrir Cofres del Tesoro,",
    ru: "§7  При разбивании кувшинов Тейвата или открытии сундуков,"
  },
  "item.awakening_system.omniscience_pouch.desc2": {
    en: "§a  automatically doubles all dropped rewards and materials.",
    es: "§a  duplica automáticamente todas las recompensas y materiales obtenidos.",
    ru: "§a  автоматически удваивает все полученные награды и ресурсы."
  },

  "item.awakening_system.command_spell.badge": {
    en: "§6⭐⭐⭐⭐⭐ [Mystic Artifact 5★ - Fate/Grand Order]",
    es: "§6⭐⭐⭐⭐⭐ [Artefacto Místico 5★ - Fate/Grand Order]",
    ru: "§6⭐⭐⭐⭐⭐ [Мистический артефакт 5★ - Fate/Grand Order]"
  },
  "item.awakening_system.command_spell.desc": {
    en: "§7Sacred Master seal that imposes absolute sovereign will.",
    es: "§7El blasón sagrado del Master que impone la voluntad absoluta.",
    ru: "§7Священная командная печать Мастера, повелевающая абсолютной волей."
  },
  "item.awakening_system.command_spell.seals": {
    en: "§c▸ Remaining Seals: §f%s/3",
    es: "§c▸ Sellos Restantes: §f%s/3",
    ru: "§c▸ Оставшиеся печати: §f%s/3"
  },
  "item.awakening_system.command_spell.low_hp_title": {
    en: "§e▸ Right Click at low health (<50% HP):",
    es: "§e▸ Clic derecho a baja salud (<50% HP):",
    ru: "§e▸ ПКМ при низком здоровье (<50% HP):"
  },
  "item.awakening_system.command_spell.low_hp_desc": {
    en: "  §7Restores 100% HP, Mana and grants Absorption V with purification.",
    es: "  §7Restaura 100% HP, Maná y otorga Absorción V con purificación.",
    ru: "  §7Восстанавливает 100% HP, ману и дает Поглощение V с очищением."
  },
  "item.awakening_system.command_spell.high_hp_title": {
    en: "§b▸ Right Click at high health (≥50% HP):",
    es: "§b▸ Clic derecho con salud alta (≥50% HP):",
    ru: "§b▸ ПКМ при высоком здоровье (≥50% HP):"
  },
  "item.awakening_system.command_spell.high_hp_desc": {
    en: "  §7Restores 100% Mana and instantly resets all weapon cooldowns.",
    es: "  §7Restaura 100% Maná y reinicia al instante todos los enfriamientos de armas.",
    ru: "  §7Восстанавливает 100% маны и мгновенно сбрасывает кулдауны оружия."
  },

  "item.awakening_system.visions_menu.badge": {
    en: "§6⭐⭐⭐⭐⭐ [Teyvat Elemental Compendium]",
    es: "§6⭐⭐⭐⭐⭐ [Compendio Elemental de Teyvat]",
    ru: "§6⭐⭐⭐⭐⭐ [Элементальный компендиум Тейвата]"
  },
  "item.awakening_system.visions_menu.desc": {
    en: "§7An archaic tome bestowed by the Statues of The Seven.",
    es: "§7Un tomo arcaico otorgado por las Siete Estatuas de Los Siete.",
    ru: "§7Древний фолиант, дарованный Статуями Семи."
  },
  "item.awakening_system.visions_menu.use": {
    en: "§e▸ Right Click: §fOpens menu to resonate with any of the 7 primordial elements.",
    es: "§e▸ Clic derecho: §fAbre el menú para resonar con cualquiera de los 7 elementos primordiales.",
    ru: "§e▸ ПКМ: §fОткрывает меню для резонанса с любым из 7 элементов."
  },
  "item.awakening_system.visions_menu.hint": {
    en: "§b▸ Like the Traveler, you can switch elements freely.",
    es: "§b▸ Como el Viajero, puedes cambiar tu elemento libremente.",
    ru: "§b▸ Как и Путешественник, вы можете менять элемент в любой момент."
  }
};

// Update lang files
const langDir = 'src/main/resources/assets/awakening_system/lang';
const esFiles = [
  'es_es.json', 'es_ar.json', 'es_cl.json', 'es_co.json',
  'es_ec.json', 'es_mx.json', 'es_pe.json', 'es_uy.json', 'es_ve.json'
];

function updateLang(fileName, langKey) {
  const filePath = `${langDir}/${fileName}`;
  const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
  let added = 0;
  for (const [key, trans] of Object.entries(translations)) {
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

console.log('Saved all remaining translations successfully!');
