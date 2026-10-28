const fs = require('fs');

const teyvatFoods = {
  "awakening_system.food.adeptus_temptation.badge": {
    en: "§6★★★★★ [Liyue Legendary Dish]",
    es: "§6★★★★★ [Plato Legendario de Liyue]",
    ru: "§6★★★★★ [Легендарное блюдо Ли Юэ]"
  },
  "awakening_system.food.adeptus_temptation.desc": {
    en: "Exquisite slow-cooked delicacy that delights even the gods.",
    es: "Exquisito manjar cocinado a fuego lento que deleita incluso a los dioses.",
    ru: "Изысканный медленно томленый деликатес, восхищающий даже богов."
  },
  "awakening_system.food.adeptus_temptation.effect": {
    en: "§6▸ Effect: §fStrength II, Absorption II, and Regeneration for 5 min.",
    es: "§6▸ Efecto: §fFuerza II, Absorción II y Regeneración por 5 min.",
    ru: "§6▸ Эффект: §fСила II, Поглощение II и Регенерация на 5 мин."
  },
  "awakening_system.food.adeptus_temptation.msg": {
    en: "§6§l✦ [ADEPTUS' TEMPTATION] §fCelestial power imbued! (+30% Attack Damage, Absorption, and Regeneration for 5 min).",
    es: "§6§l✦ [TENTACIÓN DE ADEPTUS] §f¡Poder celestial imbuido! (+30% Daño de Ataque, Absorción y Regeneración durante 5 min).",
    ru: "§6§l✦ [ИСКУШЕНИЕ АДЕПТА] §fНебесная сила дарована! (+30% силы атаки, поглощение и регенерация на 5 мин)."
  },

  "awakening_system.food.sweet_madame.badge": {
    en: "§a★★ [Mondstadt Specialty]",
    es: "§a★★ [Especialidad de Mondstadt]",
    ru: "§a★★ [Фирменное блюдо Мондштадта]"
  },
  "awakening_system.food.sweet_madame.desc": {
    en: "Golden roasted chicken with sweet flowers. Comforts the body instantly.",
    es: "Pollo asado dorado con flores dulces. Reconforta el cuerpo al instante.",
    ru: "Золотистая запеченная курочка с цветками-сахарками. Мгновенно согревает тело."
  },
  "awakening_system.food.sweet_madame.effect": {
    en: "§a▸ Effect: §fCures +16 HP instantly and purges poison/wither.",
    es: "§a▸ Efecto: §fCura +16 HP al instante y purga veneno/descomposición.",
    ru: "§a▸ Эффект: §fМгновенно исцеляет +16 HP и снимает яд/иссушение."
  },
  "awakening_system.food.sweet_madame.msg": {
    en: "§a§l✦ [SWEET MADAME] §fHealth restored (+16 HP) and negative effects purified!",
    es: "§a§l✦ [POLLO DULCE] §f¡Salud restaurada (+16 HP) y efectos negativos purificados!",
    ru: "§a§l✦ [ЦЫПЛЕНОК В МЕДОВОМ СОУСЕ] §fЗдоровье восстановлено (+16 HP) и негативные эффекты сняты!"
  },

  "awakening_system.food.almond_tofu.badge": {
    en: "§b★★★ [Wangshu Inn Specialty]",
    es: "§b★★★ [Especialidad de la Posada Wangshu]",
    ru: "§b★★★ [Фирменное блюдо Постоялого двора Ваншу]"
  },
  "awakening_system.food.almond_tofu.desc": {
    en: "Sweet dessert favorite of the Demon Hunter Xiao. Clears the mind.",
    es: "Postre dulce favorito del Cazador de Demonios Xiao. Aclara los pensamientos.",
    ru: "Сладкий десерт, любимый Охотником на демонов Сяо. Проясняет мысли."
  },
  "awakening_system.food.almond_tofu.effect": {
    en: "§b▸ Effect: §fSpeed II, Haste II, and Luck II for 5 min.",
    es: "§b▸ Efecto: §fVelocidad II, Prisa II y Suerte II por 5 min.",
    ru: "§b▸ Эффект: §fСкорость II, Спешка II и Удача II на 5 мин."
  },
  "awakening_system.food.almond_tofu.msg": {
    en: "§b§l✦ [ALMOND TOFU] §fXiao's agility flows through you! (+Speed and Luck increased for 5 min).",
    es: "§b§l✦ [TOFU DE ALMENDRAS] §f¡La agilidad de Xiao fluye en ti! (+Velocidad y Suerte aumentadas durante 5 min).",
    ru: "§b§l✦ [МИНДАЛЬНЫЙ ТОФУ] §fЛовкость Сяо наполняет вас! (+Скорость и удача повышены на 5 мин)."
  },

  "awakening_system.food.sunsettia.badge": {
    en: "§e★ [Teyvat Wild Fruit]",
    es: "§e★ [Fruta Silvestre de Teyvat]",
    ru: "§e★ [Дикорастущий плод Тейвата]"
  },
  "awakening_system.food.sunsettia.desc": {
    en: "Plump, sweet fruit growing on Cuihua trees. Comforts the body.",
    es: "Fruta rolliza y dulce que crece en los árboles de Cuihua. Reconforta el cuerpo.",
    ru: "Пухлый сладкий плод с деревьев Цуйхуа. Восстанавливает силы."
  },
  "awakening_system.food.sunsettia.effect": {
    en: "§e▸ Effect: §fRestores 4 HP instantly.",
    es: "§e▸ Efecto: §fRestaura 4 HP al instante.",
    ru: "§e▸ Эффект: §fМгновенно восстанавливает 4 HP."
  },
  "awakening_system.food.sunsettia.msg": {
    en: "§e§l✦ [SUNSETTIA] §fSweet and juicy! (+4 HP restored).",
    es: "§e§l✦ [FRUTA DEL OCASO] §f¡Dulce y jugosa! (+4 HP restaurados).",
    ru: "§e§l✦ [ЗАКАТНИК] §fСладкий и сочный! (+4 HP восстановлено)."
  }
};

const otherFoods = {
  "item.awakening_system.celestial_pine_nut.title": {
    en: "§2§lCelestial Pine Nut of Ancestral Dao",
    es: "§2§lPiñón Celestial del Dao Ancestral",
    ru: "§2§lНебесный кедровый орех древнего Дао"
  },
  "item.awakening_system.celestial_pine_nut.desc": {
    en: "§7Concentrated seed fallen from the canopies of Murim's Colossal Pines.",
    es: "§7Semilla concentrada caída de las copas de los Pinos Colosales de Murim.",
    ru: "§7Концентрированное семя с вершин колоссальных сосен Мурима."
  },
  "item.awakening_system.celestial_pine_nut.on_consume": {
    en: "§e▪ On Consume:",
    es: "§e▪ Al Consumir:",
    ru: "§e▪ При употреблении:"
  },
  "item.awakening_system.celestial_pine_nut.effect1": {
    en: "  §b▪ +45 Cultivation Qi & +50 EXP",
    es: "  §b▪ +45 Qi de Cultivo y +50 EXP",
    ru: "  §b▪ +45 Ци культивации и +50 опыта"
  },
  "item.awakening_system.celestial_pine_nut.effect2": {
    en: "  §6▪ Resistance II (15s) & Absorption Shield I (20s)",
    es: "  §6▪ Resistencia II (15s) y Coraza de Absorción I (20s)",
    ru: "  §6▪ Сопротивление II (15с) и щит поглощения I (20с)"
  },

  "item.awakening_system.eldoria_fruit.title": {
    en: "§e§lStellar Fruit of Eldoria",
    es: "§e§lFruta Estelar de Eldoria",
    ru: "§e§lЗвездный фрукт Эльдории"
  },
  "item.awakening_system.eldoria_fruit.desc": {
    en: "§7Luminescent fruit growing on the highest branches of the Sacred Tree of Eldoria.",
    es: "§7Fruto luminiscente que crece en las ramas más altas del Árbol Sagrado de Eldoria.",
    ru: "§7Светящийся плод с самых высоких ветвей Священного древа Эльдории."
  },
  "item.awakening_system.eldoria_fruit.on_consume": {
    en: "§e▪ On Consume:",
    es: "§e▪ Al Consumir:",
    ru: "§e▪ При употреблении:"
  },
  "item.awakening_system.eldoria_fruit.effect1": {
    en: "  §b▪ +50 Mana Energy & +35 EXP",
    es: "  §b▪ +50 Energía de Maná y +35 EXP",
    ru: "  §b▪ +50 энергии маны и +35 опыта"
  },
  "item.awakening_system.eldoria_fruit.effect2": {
    en: "  §e▪ Night Vision (30s) & Speed II (15s)",
    es: "  §e▪ Visión Nocturna (30s) y Velocidad II (15s)",
    ru: "  §e▪ Ночное зрение (30с) и Скорость II (15с)"
  },

  "item.awakening_system.heavenly_longevity_peach.title": {
    en: "§6§lCelestial Peach of Immortality (Pantao)",
    es: "§6§lMelocotón Celestial de la Inmortalidad (Pantao)",
    ru: "§6§lНебесный персик бессмертия (Паньтао)"
  },
  "item.awakening_system.heavenly_longevity_peach.desc1": {
    en: "§dMythical fruit of the Heavenly Garden that ripens only once every three thousand years.",
    es: "§dFruta mítica del Jardín Celestial que madura solo una vez cada tres mil años.",
    ru: "§dМифический плод Небесного сада, созревающий лишь раз в три тысячи лет."
  },
  "item.awakening_system.heavenly_longevity_peach.desc2": {
    en: "§7Grants longevity, cures all ills, and nourishes the cultivator's Sea of Qi.",
    es: "§7Concede longevidad, cura todo mal y nutre el Mar de Qi del cultivador.",
    ru: "§7Дарует долголетие, исцеляет любые недуги и питает Море Ци культиватора."
  },
  "item.awakening_system.heavenly_longevity_peach.on_consume": {
    en: "§e▪ On Consume:",
    es: "§e▪ Al Consumir:",
    ru: "§e▪ При употреблении:"
  },
  "item.awakening_system.heavenly_longevity_peach.effect1": {
    en: "  §b▪ +150 Supreme Cultivation Qi, +100 EXP & +80 Energy",
    es: "  §b▪ +150 Qi Supremo de Cultivo, +100 EXP y +80 Energía",
    ru: "  §b▪ +150 высшей Ци культивации, +100 опыта и +80 энергии"
  },
  "item.awakening_system.heavenly_longevity_peach.effect2": {
    en: "  §a▪ Complete Purification of Curses & Negative Effects",
    es: "  §a▪ Purificación Total de Maleficios y Efectos Negativos",
    ru: "  §a▪ Полное очищение от проклятий и негативных эффектов"
  },
  "item.awakening_system.heavenly_longevity_peach.effect3": {
    en: "  §6▪ Regeneration III (20s), Absorption IV (30s) & Resistance II (30s)",
    es: "  §6▪ Regeneración III (20s), Absorción IV (30s) y Resistencia II (30s)",
    ru: "  §6▪ Регенерация III (20с), Поглощение IV (30с) и Сопротивление II (30с)"
  },

  "item.awakening_system.spirit_plum_fruit.title": {
    en: "§d§lMount Hua Spiritual Plum",
    es: "§d§lFruto Espiritual de Monte Hua",
    ru: "§d§lДуховный плод сливы Горы Хуа"
  },
  "item.awakening_system.spirit_plum_fruit.desc": {
    en: "§7Harvested from the sacred blossoming plum trees of the windy peak.",
    es: "§7Cosechado de los sagrados ciruelos en flor de la cumbre del viento.",
    ru: "§7Собран со священных цветущих слив ветреной вершины."
  },
  "item.awakening_system.spirit_plum_fruit.on_consume": {
    en: "§e▪ On Consume:",
    es: "§e▪ Al Consumir:",
    ru: "§e▪ При употреблении:"
  },
  "item.awakening_system.spirit_plum_fruit.effect1": {
    en: "  §b▪ +30 Cultivation Qi & +40 EXP",
    es: "  §b▪ +30 Qi de Cultivo y +40 EXP",
    ru: "  §b▪ +30 Ци культивации и +40 опыта"
  },
  "item.awakening_system.spirit_plum_fruit.effect2": {
    en: "  §a▪ Regeneration II (10s) & Feather Falling I (10s)",
    es: "  §a▪ Regeneración II (10s) y Ligereza I (10s)",
    ru: "  §a▪ Регенерация II (10с) и Плавное падение I (10с)"
  },
  "item.awakening_system.spirit_plum_fruit.harvest_hint": {
    en: "§8▪ Can be harvested directly by right-clicking leaves.",
    es: "§8▪ Se puede cosechar directamente haciendo clic derecho en las hojas.",
    ru: "§8▪ Можно собирать прямо с листвы правым кликом."
  },

  "item.awakening_system.teyvat_fish.hint": {
    en: "§eTeyvat Fishing: §7Used at the Fishing Association for exchanges and cooking recipes.",
    es: "§ePesca de Teyvat: §7Utilizado en el Gremio de Pescadores para canjes y recetas culinarias.",
    ru: "§eРыбалка Тейвата: §7Используется в Ассоциации рыболовов для обмена и кулинарных рецептов."
  }
};

const data = { en: {}, es: {}, ru: {} };
for (const dict of [teyvatFoods, otherFoods]) {
  for (const [key, trans] of Object.entries(dict)) {
    data.en[key] = trans.en;
    data.es[key] = trans.es;
    data.ru[key] = trans.ru;
  }
}

fs.writeFileSync('scripts/item_translations_foods.json', JSON.stringify(data, null, 2), 'utf8');
console.log('Saved foods translations:', Object.keys(data.en).length, 'keys');
