const fs = require('fs');

const pills = {
  // Crimson Phoenix
  "item.awakening_system.crimson_phoenix_pill.title": {
    en: "§c✦ Crimson Phoenix Nirvana Pill",
    es: "§c✦ Píldora del Nirvana del Fénix Carmesí",
    ru: "§c✦ Пилюля нирваны алого феникса"
  },
  "item.awakening_system.crimson_phoenix_pill.quote": {
    en: "§7\"Distilled from feathers and fiery blood of the immortal ancestral phoenix.\"",
    es: "§7\"Destilada con plumas y sangre ardiente del fénix ancestral inmortal.\"",
    ru: "§7\"Создана из перьев и пылающей крови бессмертного древнего феникса.\""
  },
  "item.awakening_system.crimson_phoenix_pill.effects_header": {
    en: "§e✦ Refinement Effects:",
    es: "§e✦ Efectos de Refinamiento:",
    ru: "§e✦ Эффекты очищения:"
  },
  "item.awakening_system.crimson_phoenix_pill.effect1": {
    en: "  §a✔ +6,000 Instant Cultivation Qi Points",
    es: "  §a✔ +6,000 Puntos de Qi de Cultivo Inmediato",
    ru: "  §a✔ +6,000 очков Ци культивации мгновенно"
  },
  "item.awakening_system.crimson_phoenix_pill.effect2": {
    en: "  §c✔ Rebirth: Restores 100% Health and cleanses negative effects",
    es: "  §c✔ Renacimiento: Restaura el 100% de Vida y limpia efectos negativos",
    ru: "  §c✔ Перерождение: Восстанавливает 100% HP и снимает негативные эффекты"
  },
  "item.awakening_system.crimson_phoenix_pill.effect3": {
    en: "  §6✔ Fire Immunity for 10 minutes",
    es: "  §6✔ Inmunidad al fuego por 10 minutos",
    ru: "  §6✔ Иммунитет к огню на 10 минут"
  },

  // Foundation Building
  "item.awakening_system.foundation_building_pill.title": {
    en: "§a✦ Foundation Building Pill (Zhuji Dan)",
    es: "§a✦ Píldora de Construcción de Cimientos (Zhuji Dan)",
    ru: "§a✦ Пилюля создания фундамента (Чжуцзи Дань)"
  },
  "item.awakening_system.foundation_building_pill.quote": {
    en: "§7\"The indispensable fundamental pill to solidify the Dantian and meridians.\"",
    es: "§7\"La píldora fundamental indispensable para consolidar el Dantian y los meridianos.\"",
    ru: "§7\"Незаменимая пилюля для укрепления Даньтяня и меридианов.\""
  },
  "item.awakening_system.foundation_building_pill.effects_header": {
    en: "§e✦ Refinement Effects:",
    es: "§e✦ Efectos de Refinamiento:",
    ru: "§e✦ Эффекты очищения:"
  },
  "item.awakening_system.foundation_building_pill.effect1": {
    en: "  §a✔ +2,500 Instant Cultivation Qi Points",
    es: "  §a✔ +2,500 Puntos de Qi de Cultivo Inmediato",
    ru: "  §a✔ +2,500 очков Ци культивации мгновенно"
  },
  "item.awakening_system.foundation_building_pill.effect2": {
    en: "  §e✔ Solid Foundation: Absorption and Resistance for 3 minutes",
    es: "  §e✔ Cimientos Sólidos: Absorción y Resistencia por 3 minutos",
    ru: "  §e✔ Прочный фундамент: Поглощение и Сопротивление на 3 минуты"
  },

  // Heavenly Enlightenment
  "item.awakening_system.heavenly_enlightenment_pill.title": {
    en: "§5✦ Heavenly Enlightenment Pill",
    es: "§5✦ Píldora de la Iluminación Celestial",
    ru: "§5✦ Пилюля Небесного Просветления"
  },
  "item.awakening_system.heavenly_enlightenment_pill.quote": {
    en: "§7\"Clears the mind of mundane impurities and harmonizes the flow of the Dao.\"",
    es: "§7\"Aclara la mente de impurezas mundanas y sincroniza el flujo del Dao.\"",
    ru: "§7\"Очищает разум от мирской скверны и гармонизирует поток Дао.\""
  },
  "item.awakening_system.heavenly_enlightenment_pill.effects_header": {
    en: "§e✦ Refinement Effects:",
    es: "§e✦ Efectos de Refinamiento:",
    ru: "§e✦ Эффекты очищения:"
  },
  "item.awakening_system.heavenly_enlightenment_pill.effect1": {
    en: "  §a✔ +4,500 Instant Cultivation Qi Points",
    es: "  §a✔ +4,500 Puntos de Qi de Cultivo Inmediato",
    ru: "  §a✔ +4,500 очков Ци культивации мгновенно"
  },
  "item.awakening_system.heavenly_enlightenment_pill.effect2": {
    en: "  §d✔ Safe Breakthrough: Unlocks 1 Meridian with zero failure risk",
    es: "  §d✔ Desbloqueo Seguro: Abre 1 Meridiano sin riesgo de fracaso",
    ru: "  §d✔ Безопасный прорыв: Открывает 1 меридиан без риска неудачи"
  },
  "item.awakening_system.heavenly_enlightenment_pill.effect3": {
    en: "  §b✔ Swiftness and Night Vision for 5 minutes",
    es: "  §b✔ Celeridad y Visión Nocturna por 5 minutos",
    ru: "  §b✔ Скорость и ночное зрение на 5 минут"
  },

  // Nine Revolutions
  "item.awakening_system.nine_revolutions_pill.title": {
    en: "§6✦ Nine Revolutions Golden Pill",
    es: "§6✦ Píldora Legendaria de Alquimia Celestial",
    ru: "§6✦ Легендарная пилюля девяти превращений"
  },
  "item.awakening_system.nine_revolutions_pill.quote": {
    en: "§7\"Refined through nine cycles of solar fire and astral dew.\"",
    es: "§7\"Refinada a través de nueve ciclos de fuego solar y rocío astral.\"",
    ru: "§7\"Очищена через девять циклов солнечного огня и астральной росы.\""
  },
  "item.awakening_system.nine_revolutions_pill.effects_header": {
    en: "§e✦ Refinement Effects:",
    es: "§e✦ Efectos de Refinamiento:",
    ru: "§e✦ Эффекты очищения:"
  },
  "item.awakening_system.nine_revolutions_pill.effect1": {
    en: "  §a✔ +10,000 Instant Cultivation Qi Points",
    es: "  §a✔ +10,000 Puntos de Qi de Cultivo Inmediato",
    ru: "  §a✔ +10,000 очков Ци культивации мгновенно"
  },
  "item.awakening_system.nine_revolutions_pill.effect2": {
    en: "  §b✔ State of Enlightenment: Regeneration III & Strength III for 5 min",
    es: "  §b✔ Estado de Iluminación: Regeneración III y Fuerza III por 5 min",
    ru: "  §b✔ Состояние просветления: Регенерация III и Сила III на 5 мин"
  },
  "item.awakening_system.nine_revolutions_pill.effect3": {
    en: "  §d✔ Smooth sub-realm breakthrough without bottleneck",
    es: "  §d✔ Facilita el avance de sub-niveles sin cuello de botella",
    ru: "  §d✔ Облегчает прорыв подуровней без задержек"
  },

  // Pill of Primordial Rebirth
  "item.awakening_system.primordial_rebirth_pill.title": {
    en: "§6✦ Pill of Primordial Rebirth",
    es: "§6✦ Píldora del Renacimiento Primordial",
    ru: "§6✦ Пилюля первозданного перерождения"
  },
  "item.awakening_system.primordial_rebirth_pill.quote": {
    en: "§7\"The supreme elixir capable of reshaping the hunter's destiny.\"",
    es: "§7\"El elixir supremo capaz de recrear el destino del cazador.\"",
    ru: "§7\"Высший эликсир, способный изменить судьбу охотника.\""
  },
  "item.awakening_system.primordial_rebirth_pill.effects_header": {
    en: "§e✦ Unique Transcendence Effect:",
    es: "§e✦ Efecto Único de Trascendencia:",
    ru: "§e✦ Уникальный эффект трансцендентности:"
  },
  "item.awakening_system.primordial_rebirth_pill.desc1": {
    en: "  §fUpon consumption, dissolves your current class and opens",
    es: "  §fAl consumirla, disuelve tu clase actual y abre la pantalla",
    ru: "  §fПри употреблении сбрасывает ваш текущий класс и открывает"
  },
  "item.awakening_system.primordial_rebirth_pill.desc2": {
    en: "  §fthe §6Class Selection §fscreen to forge a new path.",
    es: "  §fde §6Selección de Clase §fpara escoger un nuevo destino.",
    ru: "  §fокно §6Выбора Класса §fдля выбора нового пути."
  },
  "item.awakening_system.primordial_rebirth_pill.desc3": {
    en: "  §a✔ Base level, stats, and cultivation progress are preserved.",
    es: "  §a✔ No se pierde nivel base, estadísticas ni progreso de cultivo.",
    ru: "  §a✔ Базовый уровень, характеристики и прогресс культивации сохраняются."
  },

  // White Tiger
  "item.awakening_system.white_tiger_pill.title": {
    en: "§b✦ White Tiger Martial Pill (Byakko)",
    es: "§b✦ Píldora Marcial del Tigre Blanco (Byakko)",
    ru: "§b✦ Боевая пилюля Белого Тигра (Бякко)"
  },
  "item.awakening_system.white_tiger_pill.quote": {
    en: "§7\"Infused with the lightning energy and ferocity of the Western Divine Beast.\"",
    es: "§7\"Infundida con la energía de relámpago y ferocidad de la Bestia Divina Occidental.\"",
    ru: "§7\"Наполнена энергией молнии и свирепостью Западного Божественного Зверя.\""
  },
  "item.awakening_system.white_tiger_pill.effects_header": {
    en: "§e✦ Refinement Effects:",
    es: "§e✦ Efectos de Refinamiento:",
    ru: "§e✦ Эффекты очищения:"
  },
  "item.awakening_system.white_tiger_pill.effect1": {
    en: "  §a✔ +7,500 Instant Cultivation Qi Points",
    es: "  §a✔ +7,500 Puntos de Qi de Cultivo Inmediato",
    ru: "  §a✔ +7,500 очков Ци культивации мгновенно"
  },
  "item.awakening_system.white_tiger_pill.effect2": {
    en: "  §b✔ Lightning Fury: Strength III, Speed II, and Resistance II for 3 min",
    es: "  §b✔ Furia de Rayo: Fuerza III, Velocidad II y Resistencia II por 3 min",
    ru: "  §b✔ Ярость молнии: Сила III, Скорость II и Сопротивление II на 3 мин"
  }
};

const data = { en: {}, es: {}, ru: {} };
for (const [key, trans] of Object.entries(pills)) {
  data.en[key] = trans.en;
  data.es[key] = trans.es;
  data.ru[key] = trans.ru;
}

fs.writeFileSync('scripts/item_translations_pills.json', JSON.stringify(data, null, 2), 'utf8');
console.log('Saved pills translations:', Object.keys(data.en).length, 'keys');
