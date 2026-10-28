const fs = require('fs');
const path = require('path');

// ==========================================
// 1. DICTIONARY OF ALL NEW TRANSLATIONS
// ==========================================
const dict = {
  // --- SUBWAY BLOCKS ---
  "block.awakening_system.subway_passenger_seat.desc": {
    en: "§7Stainless steel ergonomic seat from Seoul Metro train cars.",
    es: "§7Asiento ergonómico de acero inoxidable de los vagones del Metro de Seúl.",
    ru: "§7Эргономичное сиденье из нержавеющей стали вагонов Сеульского метро."
  },
  "block.awakening_system.subway_passenger_seat.use": {
    en: "§e✦ Right click to sit and cultivate Qi (+4/s).",
    es: "§e✦ Clic derecho para sentarse y cultivar Qi (+4/s).",
    ru: "§e✦ ПКМ, чтобы сесть и культивировать Ци (+4/с)."
  },
  "block.awakening_system.subway_stainless_steel.desc": {
    en: "§7Railway-grade stainless steel plating.",
    es: "§7Placas de acero inoxidable de grado ferroviario.",
    ru: "§7Пластины из нержавеющей стали железнодорожного класса."
  },
  "block.awakening_system.subway_stainless_steel.use": {
    en: "§e✦ Construction block for chassis, flooring and subway car body.",
    es: "§e✦ Bloque de construcción para chasis, piso y carrocería de vagones de metro.",
    ru: "§e✦ Строительный блок для шасси, пола и кузова вагонов метро."
  },
  "block.awakening_system.subway_door.desc": {
    en: "§7Double sliding subway door with reinforced viewing glass.",
    es: "§7Puerta corrediza doble de metro con ventanal reforzado.",
    ru: "§7Двойная раздвижная дверь метро с усиленным окном."
  },
  "block.awakening_system.subway_door.use": {
    en: "§a✦ Synchronized dual opening for boarding from platforms and stations.",
    es: "§a✦ Apertura doble sincronizada para abordaje desde andenes y estaciones.",
    ru: "§a✦ Синхронное открытие для посадки с платформ и станций."
  },
  "block.awakening_system.subway_platform.desc": {
    en: "§7Polished terrazzo tiles for subway station platforms (ORV).",
    es: "§7Baldosas de terrazo pulido para andenes de estaciones de metro (ORV).",
    ru: "§7Полированная терраццо плитка для платформ станций метро (ORV)."
  },
  "block.awakening_system.subway_platform.use": {
    en: "§e✦ Enables building boarding platforms level with train cars.",
    es: "§e✦ Permite construir andenes de abordaje a nivel del vagón.",
    ru: "§e✦ Позволяет строить платформы посадки на уровне вагона."
  },
  "block.awakening_system.subway_platform_edge.desc": {
    en: "§7Platform edge with tactile paving and safety yellow caution line.",
    es: "§7Borde de andén con franja podotáctil y línea amarilla de seguridad.",
    ru: "§7Край платформы с тактильной плиткой и желтой линией безопасности."
  },
  "block.awakening_system.subway_platform_edge.use": {
    en: "§6✦ Face towards railway tracks to delimit the waiting platform.",
    es: "§6✦ Orienta mirando hacia las vías férreas para delimitar el andén de espera.",
    ru: "§6✦ Размещайте лицом к путям для обозначения зоны ожидания."
  },

  // --- CHAOS MATERIALS ---
  "item.awakening_system.chaos_device.badge": {
    en: "§6§l[Automaton Material ✦✦]",
    es: "§6§l[Material de Autómata ✦✦]",
    ru: "§6§l[Материал автоматонов ✦✦]"
  },
  "item.awakening_system.chaos_device.desc": {
    en: "§7Chaos Device recovered from a Ruin Guard.",
    es: "§7Dispositivo del Caos obtenido de un Guardián de las Ruinas.",
    ru: "§7Устройство хаоса, добытое с Стража руин."
  },
  "item.awakening_system.chaos_device.lore": {
    en: "§8Arcane gear structure originating from ancient Khaenri'ah.",
    es: "§8Estructura de engranajes arcanos proveniente de la antigua Khaenri'ah.",
    ru: "§8Тайный механизм шестерен древней Каэнри'ах."
  },
  "item.awakening_system.chaos_circuit.badge": {
    en: "§5§l[Automaton Material ✦✦✦]",
    es: "§5§l[Material de Autómata ✦✦✦]",
    ru: "§5§l[Материал автоматонов ✦✦✦]"
  },
  "item.awakening_system.chaos_circuit.desc": {
    en: "§7Chaos Circuit carrying residual biomechanical energy.",
    es: "§7Circuito del Caos con energía biomecánica residual.",
    ru: "§7Цепь хаоса с остаточной биомеханической энергией."
  },
  "item.awakening_system.chaos_circuit.lore": {
    en: "§8Channels motive impulses of destructor automatons.",
    es: "§8Canaliza impulsos motrices de los autómatas destructores.",
    ru: "§8Проводит двигательные импульсы боевых автоматонов."
  },
  "item.awakening_system.chaos_core.badge": {
    en: "§d§l[Automaton Material ✦✦✦✦]",
    es: "§d§l[Material de Autómata ✦✦✦✦]",
    ru: "§d§l[Материал автоматонов ✦✦✦✦]"
  },
  "item.awakening_system.chaos_core.desc": {
    en: "§7Flawless Chaos Core pulsating with limitless energy.",
    es: "§7Núcleo del Caos perfecto y palpitante de energía infinita.",
    ru: "§7Ядро хаоса, пульсирующее неиссякаемой энергией."
  },
  "item.awakening_system.chaos_core.lore": {
    en: "§8The supreme power heart of Khaenri'ah's war engines.",
    es: "§8El corazón motriz supremo de la maquinaria de guerra de Khaenri'ah.",
    ru: "§8Верховное силовое сердце боевых машин Каэнри'ах."
  },

  // --- LEY LINE ABYSS MATERIALS ---
  "item.awakening_system.dead_ley_line_branch.badge": {
    en: "§a§l[Abyss Material ✦✦]",
    es: "§a§l[Material del Abismo ✦✦]",
    ru: "§a§l[Материал Бездны ✦✦]"
  },
  "item.awakening_system.dead_ley_line_branch.desc": {
    en: "§7Withered twig obtained from an Abyss Mage.",
    es: "§7Ramita seca obtenida de un Mago del Abismo.",
    ru: "§7Сухая ветвь, полученная от мага Бездны."
  },
  "item.awakening_system.dead_ley_line_branch.lore": {
    en: "§8Holds faded traces of subterranean telluric energy.",
    es: "§8Contiene vestigios marchitos de energía telúrica subterránea.",
    ru: "§8Хранит увядшие следы подземной теллурической энергии."
  },
  "item.awakening_system.dead_ley_line_leaves.badge": {
    en: "§9§l[Abyss Material ✦✦✦]",
    es: "§9§l[Material del Abismo ✦✦✦]",
    ru: "§9§l[Материал Бездны ✦✦✦]"
  },
  "item.awakening_system.dead_ley_line_leaves.desc": {
    en: "§7Dried leaves from the deep branches of the ley lines.",
    es: "§7Hojas secas de las ramas profundas de las líneas ley.",
    ru: "§7Сухие листья из глубинных ветвей артерий земли."
  },
  "item.awakening_system.dead_ley_line_leaves.lore": {
    en: "§8Channels the arcane mysteries of the world's wisdom.",
    es: "§8Canalizan los misterios arcanos de la sabiduría del mundo.",
    ru: "§8Проводят сокровенные тайны мудрости мира."
  },
  "item.awakening_system.ley_line_sprout.badge": {
    en: "§5§l[Abyss Material ✦✦✦✦]",
    es: "§5§l[Material del Abismo ✦✦✦✦]",
    ru: "§5§l[Материал Бездны ✦✦✦✦]"
  },
  "item.awakening_system.ley_line_sprout.desc": {
    en: "§7Tender, radiant sprout from the ley lines.",
    es: "§7Brote tierno y resplandeciente de las líneas ley.",
    ru: "§7Нежный сияющий росток артерий земли."
  },
  "item.awakening_system.ley_line_sprout.lore": {
    en: "§8Flows with the purest life current of the tree upholding creation.",
    es: "§8Fluye con la corriente vital más pura del árbol que sostiene la creación.",
    ru: "§8Струится чистейшим жизненным потоком древа мироздания."
  },

  // --- HYPOSTASIS & REGISVINE BOSS MATERIALS ---
  "item.awakening_system.hurricane_seed.badge": {
    en: "§b§l[Ascension Material ✦✦✦✦]",
    es: "§b§l[Material de Ascensión ✦✦✦✦]",
    ru: "§b§l[Материал возвышения ✦✦✦✦]"
  },
  "item.awakening_system.hurricane_seed.desc": {
    en: "§7Cylindrical seed left by an Anemo Hypostasis.",
    es: "§7Semilla cilíndrica dejada por el Hipóstasis Anemo.",
    ru: "§7Семя урагана, оставленное Анемо гипостазисом."
  },
  "item.awakening_system.hurricane_seed.lore": {
    en: "§8Contains the fury of the primordial tempest sweeping the skies.",
    es: "§8Contiene el poder de la tempestad primordial que surca los cielos.",
    ru: "§8Содержит мощь первозданной бури, рассекающей небеса."
  },
  "item.awakening_system.lightning_prism.badge": {
    en: "§d§l[Ascension Material ✦✦✦✦]",
    es: "§d§l[Material de Ascensión ✦✦✦✦]",
    ru: "§d§l[Материал возвышения ✦✦✦✦]"
  },
  "item.awakening_system.lightning_prism.desc": {
    en: "§7Brilliant prism dropped by an Electro Hypostasis.",
    es: "§7Prisma brillante abandonado por el Hipóstasis Electro.",
    ru: "§7Призма молнии Электро гипостазиса."
  },
  "item.awakening_system.lightning_prism.lore": {
    en: "§8Refracts pure, highly concentrated lightning energy.",
    es: "§8Refracta la energía de relámpago pura y de alta concentración.",
    ru: "§8Преломляет чистую концентрированную энергию молнии."
  },
  "item.awakening_system.basalt_pillar.badge": {
    en: "§6§l[Ascension Material ✦✦✦✦]",
    es: "§6§l[Material de Ascensión ✦✦✦✦]",
    ru: "§6§l[Материал возвышения ✦✦✦✦]"
  },
  "item.awakening_system.basalt_pillar.desc": {
    en: "§7Dense stone pillar fallen from a Geo Hypostasis.",
    es: "§7Pilar de piedra densa caído del Hipóstasis Geo.",
    ru: "§7Базальтовая колонна Гео гипостазиса."
  },
  "item.awakening_system.basalt_pillar.lore": {
    en: "§8An earthen core imbued with the unyielding gravity of the land.",
    es: "§8El núcleo terrenal imbuido con la gravedad inquebrantable de la tierra.",
    ru: "§8Земное ядро, пропитанное несокрушимой гравитацией земли."
  },
  "item.awakening_system.crystalline_bloom.badge": {
    en: "§3§l[Ascension Material ✦✦✦✦]",
    es: "§3§l[Material de Ascensión ✦✦✦✦]",
    ru: "§3§l[Материал возвышения ✦✦✦✦]"
  },
  "item.awakening_system.crystalline_bloom.desc": {
    en: "§7Glacial crystalline flower shed by a Cryo Hypostasis.",
    es: "§7Flor cristalina gélida desprendida del Hipóstasis Cryo.",
    ru: "§7Кристальный цветок Крио гипостазиса."
  },
  "item.awakening_system.crystalline_bloom.lore": {
    en: "§8Emanates perpetual frost capable of freezing elemental flow.",
    es: "§8Emana una escarcha perpetua capaz de congelar el flujo elemental.",
    ru: "§8Источает вечный мороз, сковывающий потоки элементов."
  },
  "item.awakening_system.smoldering_pearl.badge": {
    en: "§c§l[Ascension Material ✦✦✦✦]",
    es: "§c§l[Material de Ascensión ✦✦✦✦]",
    ru: "§c§l[Материал возвышения ✦✦✦✦]"
  },
  "item.awakening_system.smoldering_pearl.desc": {
    en: "§7Searing pearl harvested from the core of a Pyro Hypostasis.",
    es: "§7Perla ardiente obtenida del núcleo del Hipóstasis Pyro.",
    ru: "§7Тлеющая жемчужина Пиро гипостазиса."
  },
  "item.awakening_system.smoldering_pearl.lore": {
    en: "§8Burns with an inextinguishable flame born of purest combustion.",
    es: "§8Arde con una llama inextinguible nacida de la combustión más pura.",
    ru: "§8Горит неугасимым пламенем чистейшего горения."
  },
  "item.awakening_system.dew_of_repudiation.badge": {
    en: "§9§l[Ascension Material ✦✦✦✦]",
    es: "§9§l[Material de Ascensión ✦✦✦✦]",
    ru: "§9§l[Материал возвышения ✦✦✦✦]"
  },
  "item.awakening_system.dew_of_repudiation.desc": {
    en: "§7Pristine water drop condensed from a Hydro Hypostasis.",
    es: "§7Gota de agua pristina condensada del Hipóstasis Hydro.",
    ru: "§7Роса отречения Гидро гипостазиса."
  },
  "item.awakening_system.dew_of_repudiation.lore": {
    en: "§8Repels all impurities and flows eternally vibrant.",
    es: "§8Rechaza cualquier impureza en su corriente y fluye eternamente viva.",
    ru: "§8Отвергает любые примеси и течет вечно чистой."
  },
  "item.awakening_system.hoarfrost_core.badge": {
    en: "§b§l[Ascension Material ✦✦✦✦]",
    es: "§b§l[Material de Ascensión ✦✦✦✦]",
    ru: "§b§l[Материал возвышения ✦✦✦✦]"
  },
  "item.awakening_system.hoarfrost_core.desc": {
    en: "§7Pure Frost Core harvested from a Cryo Regisvine.",
    es: "§7Núcleo de Escarcha puro desprendido de la Regisvid Criogénica.",
    ru: "§7Инеевое ядро Крио папоротника."
  },
  "item.awakening_system.hoarfrost_core.lore": {
    en: "§8Contains supercooled plant essence born of the ley lines.",
    es: "§8Contiene la esencia vegetal sobreenfriada nacida de las líneas ley.",
    ru: "§8Хранит сверхохлажденную растительную эссенцию артерий земли."
  },
  "item.awakening_system.everflame_seed.badge": {
    en: "§c§l[Ascension Material ✦✦✦✦]",
    es: "§c§l[Material de Ascensión ✦✦✦✦]",
    ru: "§c§l[Материал возвышения ✦✦✦✦]"
  },
  "item.awakening_system.everflame_seed.desc": {
    en: "§7Seed of inextinguishable flame taken from a Pyro Regisvine.",
    es: "§7Semilla de llama inextinguible obtenida de la Regisvid Pyro.",
    ru: "§7Семя вечного пламени Пиро орхидеи."
  },
  "item.awakening_system.everflame_seed.lore": {
    en: "§8Nourished by the supreme elemental fire burning within the earth.",
    es: "§8Nutrida por el fuego elemental supremo que arde en la tierra.",
    ru: "§8Вскормлено верховным элементальным огнем глубин земли."
  },

  // --- WYVERN ITEMS ---
  "item.awakening_system.wyvern_scale.badge": {
    en: "§2§l[S-Rank RPG Material] §7Ultra-light and durable scale of an aerial apex predator.",
    es: "§2§l[Material RPG Rango S] §7Escama ultraligera y resistente de depredador aéreo.",
    ru: "§2§l[Материал RPG ранга S] §7Сверхлегкая и прочная чешуя воздушного хищника."
  },
  "item.awakening_system.wyvern_scale.lore": {
    en: "§8Immune to acid corrosion and severe burns.",
    es: "§8Inmune a la corrosión de ácidos y quemaduras.",
    ru: "§8Невосприимчива к кислоте и ожогам."
  },
  "item.awakening_system.wyvern_fang.badge": {
    en: "§a§l[RPG Material] §7Razor-sharp fang soaked in wyvern venom.",
    es: "§a§l[Material RPG] §7Colmillo afilado empapado en ponzoña de wyvern.",
    ru: "§a§l[Материал RPG] §7Острый клык, пропитанный ядом виверны."
  },
  "item.awakening_system.wyvern_fang.lore": {
    en: "§8Utilized in forging weapons with bleed and poison properties.",
    es: "§8Utilizado en la forja de armas con sangrado y veneno.",
    ru: "§8Используется для ковки оружия с кровотечением и ядом."
  },
  "item.awakening_system.wyvern_egg.badge": {
    en: "§6§l[Mythic Wyvern Egg] §fStolen from the highest peaks and nests.",
    es: "§6§l[Huevo Mítico de Wyvern] §fRobado de los riscos y nidos más altos.",
    ru: "§6§l[Мифическое яйцо виверны] §fДобыто из высоких гнезд на скалах."
  },
  "item.awakening_system.wyvern_egg.hint": {
    en: "§eIncubate near campfires or lava to hatch a tamable hatchling!",
    es: "§e¡Incúbalo cerca de fogatas o lava para obtener una cría domesticable!",
    ru: "§eИнкубируйте у костра или лавы, чтобы вывести питомца!"
  },
  "item.awakening_system.wyvern_saddle.badge": {
    en: "§d§l[Aerial Rider Harness] §fArtisan saddle reinforced with draconic leather straps.",
    es: "§d§l[Arnés de Jinete Aéreo] §fMontura artesanal reforzada con correas de cuero draconiano.",
    ru: "§d§l[Сбруя небесного наездника] §fСедло из кожи дракона."
  },
  "item.awakening_system.wyvern_saddle.hint": {
    en: "§bEquip onto a tamed Wyvern to soar across the skies at blazing speed.",
    es: "§bEquípala en un Wyvern domesticado para surcar los cielos a toda velocidad.",
    ru: "§bНаденьте на прирученную виверну для полетов на бешеной скорости."
  },

  // --- MITACHURL / LAWACHURL HORNS ---
  "item.awakening_system.heavy_horn.badge": {
    en: "§a§l[Mitachurl Material ✦✦]",
    es: "§a§l[Material de Mitachurl ✦✦]",
    ru: "§a§l[Материал митачурла ✦✦]"
  },
  "item.awakening_system.heavy_horn.desc": {
    en: "§7A crude, curved horn torn from a Mitachurl.",
    es: "§7Un cuerno tosco y curvado arrancado de un Mitachurl.",
    ru: "§7Тяжелый изогнутый рог митачурла."
  },
  "item.awakening_system.heavy_horn.lore": {
    en: "§8Often utilized as a battle horn across Hilichurl war camps.",
    es: "§8Se suele emplear como bocina de combate en los campamentos Hilichurl.",
    ru: "§8Используется как боевой горн в лагерях хиличурлов."
  },
  "item.awakening_system.black_bronze_horn.badge": {
    en: "§9§l[Mitachurl Material ✦✦✦]",
    es: "§9§l[Material de Mitachurl ✦✦✦]",
    ru: "§9§l[Материал митачурла ✦✦✦]"
  },
  "item.awakening_system.black_bronze_horn.desc": {
    en: "§7A metallic horn stained with dark, ominous bronze.",
    es: "§7Un cuerno metálico teñido de un bronce oscuro y amenazante.",
    ru: "§7Металлический рог темной бронзы."
  },
  "item.awakening_system.black_bronze_horn.lore": {
    en: "§8Symbolizes the status of captain and veteran chieftain.",
    es: "§8Simboliza el estatus de capitán y combatiente veterano entre los suyos.",
    ru: "§8Символизирует статус капитана среди сородичей."
  },
  "item.awakening_system.black_crystal_horn.badge": {
    en: "§d§l[Lawachurl Material ✦✦✦✦]",
    es: "§d§l[Material de Lawachurl ✦✦✦✦]",
    ru: "§d§l[Материал лавачурла ✦✦✦✦]"
  },
  "item.awakening_system.black_crystal_horn.desc": {
    en: "§7Colossal horn crystallized with veins of pure elemental power.",
    es: "§7Cuerno colosal cristalizado con vetas de energía elemental pura.",
    ru: "§7Колоссальный рог с кристальными жилами чистой энергии."
  },
  "item.awakening_system.black_crystal_horn.lore": {
    en: "§8Belonging to a supreme Armored Lawachurl chieftain.",
    es: "§8Perteneciente al cacique supremo Lawachurl Acorazado.",
    ru: "§8Принадлежит верховному вождю лавачурлу."
  },

  // --- PRIMOGEM & CURRENCIES ---
  "item.awakening_system.primogem.desc1": {
    en: "§7Primordial currency bestowed from distant stars.",
    es: "§7Divisa primordial traída de estrellas lejanas.",
    ru: "§7Изначальная валюта из далеких звезд."
  },
  "item.awakening_system.primogem.desc2": {
    en: "§7Used in the §eWish System (Gacha) §7to summon weapons and powers.",
    es: "§7Se utiliza en el §eSistema de Deseos (Gacha) §7para invocar armas y poderes.",
    ru: "§7Используется в §eСистеме молитв (гаче) §7для призыва оружия и сил."
  },
  "item.awakening_system.primogem.use1": {
    en: "§e▸ Right Click: §fDeposit §d160 Primogems §fto your wallet",
    es: "§e▸ Clic derecho: §fDepositar §d160 Protogemas §fal monedero",
    ru: "§e▸ ПКМ: §fПоложить §d160 Камней Истока §fв кошелек"
  },
  "item.awakening_system.primogem.use2": {
    en: "§e▸ Shift + Right Click: §fDeposit entire stack",
    es: "§e▸ Shift + Clic derecho: §fDepositar todo el stack",
    ru: "§e▸ Shift + ПКМ: §fПоложить всю стопку"
  },

  "item.awakening_system.genesis_crystal.badge": {
    en: "§b⭐⭐⭐⭐⭐ [Primordial Cosmic Energy]",
    es: "§b⭐⭐⭐⭐⭐ [Energía Primordial del Cosmos]",
    ru: "§b⭐⭐⭐⭐⭐ [Первозданная космическая энергия]"
  },
  "item.awakening_system.genesis_crystal.desc": {
    en: "§7Pure energy crystal crystallized at the beginning of the firmament.",
    es: "§7Cristal de energía pura formado en el origen del firmamento.",
    ru: "§7Кристалл чистой энергии, возникший на заре мироздания."
  },
  "item.awakening_system.genesis_crystal.use1": {
    en: "§b▸ Right Click: §fExchange for §d+300 Primogems",
    es: "§b▸ Clic derecho: §fCanjear por §d+300 Protogemas",
    ru: "§b▸ ПКМ: §fОбменять на §d+300 Камней Истока"
  },
  "item.awakening_system.genesis_crystal.use2": {
    en: "§b▸ Shift + Right Click: §fExchange entire stack",
    es: "§b▸ Shift + Clic derecho: §fCanjear todo el stack",
    ru: "§b▸ Shift + ПКМ: §fОбменять всю стопку"
  },

  "item.awakening_system.intertwined_fate.desc1": {
    en: "§7Seed of destiny that intertwines wishes from different realities.",
    es: "§7Semilla del destino que entrelaza los deseos de diferentes realidades.",
    ru: "§7Семя судьбы, сплетающее чаяния разных миров."
  },
  "item.awakening_system.intertwined_fate.desc2": {
    en: "§7Used in the §6Promotional Event Banner §7to obtain 5★ equipment.",
    es: "§7Se utiliza en el §6Banner Promocional de Evento §7para obtener equipo 5★.",
    ru: "§7Используется в §6Ивентовой молитве §7для получения снаряжения 5★."
  },
  "item.awakening_system.intertwined_fate.use1": {
    en: "§e▸ Right Click: §fDeposit into Wish wallet",
    es: "§e▸ Clic derecho: §fDepositar en el monedero de Deseos",
    ru: "§e▸ ПКМ: §fПоложить в кошелек молитв"
  },
  "item.awakening_system.intertwined_fate.use2": {
    en: "§e▸ Shift + Right Click: §fDeposit entire stack",
    es: "§e▸ Shift + Clic derecho: §fDepositar todo el stack",
    ru: "§e▸ Shift + ПКМ: §fПоложить всю стопку"
  },

  "item.awakening_system.acquaint_fate.desc1": {
    en: "§7Seed of destiny glowing with a gentle celestial radiance.",
    es: "§7Semilla del destino que brilla con un tenue fulgor celeste.",
    ru: "§7Семя судьбы, сияющее мягким небесным светом."
  },
  "item.awakening_system.acquaint_fate.desc2": {
    en: "§7Used in the §bStandard Wanderlust Banner §7for standard summons.",
    es: "§7Se utiliza en el §bBanner Permanente del Errante §7para invocaciones generales.",
    ru: "§7Используется в §bСтандартной молитве §7для призывов."
  },
  "item.awakening_system.acquaint_fate.use1": {
    en: "§e▸ Right Click: §fDeposit into Wish wallet",
    es: "§e▸ Clic derecho: §fDepositar en el monedero de Deseos",
    ru: "§e▸ ПКМ: §fПоложить в кошелек молитв"
  },
  "item.awakening_system.acquaint_fate.use2": {
    en: "§e▸ Shift + Right Click: §fDeposit entire stack",
    es: "§e▸ Shift + Clic derecho: §fDepositar todo el stack",
    ru: "§e▸ Shift + ПКМ: §fПоложить всю стопку"
  },

  "item.awakening_system.stellar_jade.badge": {
    en: "§b⭐⭐⭐⭐ [Interstellar Rails Currency]",
    es: "§b⭐⭐⭐⭐ [Moneda Interestelar de los Raíles]",
    ru: "§b⭐⭐⭐⭐ [Межзвездная валюта Звездных путей]"
  },
  "item.awakening_system.stellar_jade.desc": {
    en: "§7Condensate stardust glistening in the cosmic expanse.",
    es: "§7Polvo estelar coagulado que centellea en la inmensidad del cosmos.",
    ru: "§7Сгусток звездной пыли, мерцающий в глубинах космоса."
  },
  "item.awakening_system.stellar_jade.use1": {
    en: "§b▸ Right Click: §fDeposit §b160 Stellar Jades §finto Astral Wallet",
    es: "§b▸ Clic derecho: §fDepositar §b160 Jades Estelares §fen la Cartera Astral",
    ru: "§b▸ ПКМ: §fПоложить §b160 Звездных нефритов §fв звездный кошелек"
  },
  "item.awakening_system.stellar_jade.use2": {
    en: "§b▸ Shift + Right Click: §fDeposit entire stack",
    es: "§b▸ Shift + Clic derecho: §fDepositar todo el stack",
    ru: "§b▸ Shift + ПКМ: §fПоложить всю стопку"
  },
  "item.awakening_system.stellar_jade.hint": {
    en: "§e✦ Exclusive for: §fAstral Express Warp (Honkai: Star Rail)",
    es: "§e✦ Exclusivo para: §fSalto Estelar del Expreso Astral (Honkai: Star Rail)",
    ru: "§e✦ Эксклюзивно для: §fПрыжка Звездного Экспресса (Honkai: Star Rail)"
  },

  "item.awakening_system.star_rail_special_pass.badge": {
    en: "§d⭐⭐⭐⭐⭐ [Special Rail Pass]",
    es: "§d⭐⭐⭐⭐⭐ [Pase Especial de los Raíles]",
    ru: "§d⭐⭐⭐⭐⭐ [Особый звездный пропуск]"
  },
  "item.awakening_system.star_rail_special_pass.desc": {
    en: "§7Special ticket granting boarding access to the Astral Express.",
    es: "§7Boleto especial que permite abordar el Expreso Astral (Honkai: Star Rail).",
    ru: "§7Особый билет на борт Звездного экспресса."
  },
  "item.awakening_system.star_rail_special_pass.rate1": {
    en: "§b✦ 1 Pass = 1 Warp / Wish (160 Jade)",
    es: "§b✦ 1 Pase = 1 Deseo / Salto Hiperespacial (160 Jade)",
    ru: "§b✦ 1 пропуск = 1 Прыжок (160 Нефрита)"
  },
  "item.awakening_system.star_rail_special_pass.rate2": {
    en: "§6✦ 10 Passes = 1 Multi-Warp x10 (1,600 Jade)",
    es: "§6✦ 10 Pases = 1 Tirada Múltiple x10 / Multi (1,600 Jade)",
    ru: "§6✦ 10 пропусков = 10 Прыжков (1,600 Нефрита)"
  },
  "item.awakening_system.star_rail_special_pass.use1": {
    en: "§e▸ Right Click in hand: §fDeposit 160 Stellar Jade",
    es: "§e▸ Clic derecho en mano: §fDepositar 160 Jade Estelar",
    ru: "§e▸ ПКМ в руке: §fПоложить 160 Звездных нефритов"
  },
  "item.awakening_system.star_rail_special_pass.use2": {
    en: "§e▸ Shift + Right Click: §fDeposit entire stack as Jade",
    es: "§e▸ Shift + Clic derecho: §fDepositar todo el stack como Jade",
    ru: "§e▸ Shift + ПКМ: §fПоложить всю стопку как Нефрит"
  },

  // --- COSMIC FRAGMENT ---
  "item.awakening_system.cosmic_fragment.badge": {
    en: "§9⭐⭐⭐⭐ [Stellar Currency & Essence]",
    es: "§9⭐⭐⭐⭐ [Moneda y Esencia Estelar]",
    ru: "§9⭐⭐⭐⭐ [Звездная валюта и эссенция]"
  },
  "item.awakening_system.cosmic_fragment.desc": {
    en: "§7Memory fragments and cosmic laws compacted into crystalline form.",
    es: "§7Fragmentos de memoria y leyes del universo compactados en cristal puro.",
    ru: "§7Осколки памяти и законов вселенной в чистом кристалле."
  },
  "item.awakening_system.cosmic_fragment.synergy_title": {
    en: "§e✦ Synergy with Robe of the Beauty:",
    es: "§e✦ Sinergia con Manto de la Belleza:",
    ru: "§e✦ Синергия с Мантией Красоты:"
  },
  "item.awakening_system.cosmic_fragment.synergy_desc": {
    en: "  §7Grants +16% Attack Damage per 100 fragments in inventory.",
    es: "  §7Otorga +16% de Daño de Ataque por cada 100 fragmentos en inventario.",
    ru: "  §7Дает +16% к урону атаки за каждые 100 фрагментов в инвентаре."
  },
  "item.awakening_system.cosmic_fragment.repair_title": {
    en: "§b✦ Right Click: Cosmic Repair",
    es: "§b✦ Clic Derecho: Reparación Cósmica",
    ru: "§b✦ ПКМ: Космический ремонт"
  },
  "item.awakening_system.cosmic_fragment.repair_desc": {
    en: "  §7Repairs 25% durability of off-hand item §8(-16 fragments)§7.",
    es: "  §7Repara 25% de durabilidad del objeto en mano secundaria §8(-16 fragmentos)§7.",
    ru: "  §7Восстанавливает 25% прочности предмета во второй руке §8(-16 фрагментов)§7."
  },
  "item.awakening_system.cosmic_fragment.synth_title": {
    en: "§6✦ Shift + Right Click: Stellar Synthesis",
    es: "§6✦ Shift + Clic Derecho: Síntesis Estelar",
    ru: "§6✦ Shift + ПКМ: Звездный синтез"
  },
  "item.awakening_system.cosmic_fragment.synth_desc": {
    en: "  §7Condenses 160 fragments into §b1 Intertwined Fate§7.",
    es: "  §7Condensa 160 fragmentos en §b1 Destino Entrelazado§7.",
    ru: "  §7Сгущает 160 фрагментов в §b1 Переплетающуюся судьбу§7."
  },

  // --- RESINS ---
  "item.awakening_system.condensed_resin.badge": {
    en: "§6⭐⭐⭐⭐ [Domain Energy]",
    es: "§6⭐⭐⭐⭐ [Energía de Dominio]",
    ru: "§6⭐⭐⭐⭐ [Энергия подземелий]"
  },
  "item.awakening_system.condensed_resin.desc": {
    en: "§7An amber crystal storing an immense surge of original resin.",
    es: "§7Un cristal ámbar que almacena un inmenso flujo de resina original.",
    ru: "§7Янтарный кристалл, хранящий мощный поток первородной смолы."
  },
  "item.awakening_system.condensed_resin.blessing_title": {
    en: "§e✦ Right Click: Domain Blessing",
    es: "§e✦ Clic Derecho: Bendición de Dominio",
    ru: "§e✦ ПКМ: Благословение подземелья"
  },
  "item.awakening_system.condensed_resin.blessing_desc1": {
    en: "  §f• Grants 15 minutes of Haste, Speed and Saturation.",
    es: "  §f• Otorga 15 minutos de Prisa Minera, Velocidad y Saciedad.",
    ru: "  §f• Дает 15 минут спешки, скорости и сытости."
  },
  "item.awakening_system.condensed_resin.blessing_desc2": {
    en: "  §f• Doubles dungeon and domain drop rewards and EXP.",
    es: "  §f• Duplica el botín y la experiencia en mazmorras y dominios.",
    ru: "  §f• Удваивает добычу и опыт в подземельях."
  },
  "item.awakening_system.condensed_resin.recipe": {
    en: "§8✦ Recipe: 1 Crystal Core + 40 Primogems.",
    es: "§8✦ Receta: 1 Núcleo de Cristal + 40 Protogemas.",
    ru: "§8✦ Рецепт: 1 Кристальное ядро + 40 Камней Истока."
  },

  "item.awakening_system.original_resin.badge": {
    en: "§6⭐⭐⭐ [Teyvat Precious Material]",
    es: "§6⭐⭐⭐ [Material Precioso de Teyvat]",
    ru: "§6⭐⭐⭐ [Драгоценный материал Тейвата]"
  },
  "item.awakening_system.original_resin.desc": {
    en: "§7Condensed energy from the roots of Petrified Trees.",
    es: "§7Energía condensada procedente de las raíces de los Árboles Petrificados.",
    ru: "§7Сгусток энергии корней окаменевших деревьев."
  },
  "item.awakening_system.original_resin.use": {
    en: "§e▸ Right Click: §fRestores 20 System Energy and heals health.",
    es: "§e▸ Clic derecho: §fRestaura 20 puntos de Energía del Sistema y regenera salud.",
    ru: "§e▸ ПКМ: §fВосстанавливает 20 энергии Системы и лечит HP."
  },
  "item.awakening_system.original_resin.craft": {
    en: "§b▸ Also used to craft Condensed Resin.",
    es: "§b▸ Se utiliza también para sintetizar Resina Concentrada.",
    ru: "§b▸ Также используется для создания густой смолы."
  },

  "item.awakening_system.transient_resin.badge": {
    en: "§d⭐⭐⭐⭐ [Serenitea Pot Relic]",
    es: "§d⭐⭐⭐⭐ [Reliquia de Tetera de Relajatetera]",
    ru: "§d⭐⭐⭐⭐ [Реликвия Чайника безмятежности]"
  },
  "item.awakening_system.transient_resin.desc": {
    en: "§7Receptacle of pure condensed energy from the ethereal realm.",
    es: "§7Un receptáculo de energía pura condensada procedente del reino etéreo.",
    ru: "§7Сосуд чистой энергии из эфирной обители."
  },
  "item.awakening_system.transient_resin.use": {
    en: "§e▸ Right Click: §fConsume to instantly gain 60 Original Resin.",
    es: "§e▸ Clic derecho: §fConsúmela para obtener instantáneamente 60 Resinas Originales.",
    ru: "§e▸ ПКМ: §fДает 60 единиц первородной смолы."
  },

  // --- CROWN OF INSIGHT & HERO'S WIT ---
  "item.awakening_system.crown_of_insight.badge": {
    en: "§6★★★★★ [Supreme Talent Material]",
    es: "§6★★★★★ [Material Supremo de Talentos]",
    ru: "§6★★★★★ [Высший материал талантов]"
  },
  "item.awakening_system.crown_of_insight.desc": {
    en: "§7Precious testimonial of ancient wisdom from the peak of Celestia.",
    es: "§7Un precioso testimonio de sabiduría ancestral procedente de la cumbre de Celestia.",
    ru: "§7Свидетельство древней мудрости с вершин Селестии."
  },
  "item.awakening_system.crown_of_insight.title": {
    en: "§e✦ Right Click: Celestial Coronation",
    es: "§e✦ Clic Derecho: Coronación Celestial",
    ru: "§e✦ ПКМ: Небесная коронация"
  },
  "item.awakening_system.crown_of_insight.desc1": {
    en: "  §f• Grants +1 Level to all unlocked skills.",
    es: "  §f• Otorga +1 Nivel a todas tus habilidades desbloqueadas.",
    ru: "  §f• Дарует +1 уровень всем открытым навыкам."
  },
  "item.awakening_system.crown_of_insight.desc2": {
    en: "  §f• Grants 20 minutes of Strength III, Regeneration II and Luck III.",
    es: "  §f• Otorga 20 minutos de Fuerza III, Regeneración II y Suerte III.",
    ru: "  §f• Дает 20 минут силы III, регенерации II и удачи III."
  },

  "item.awakening_system.heros_wit.badge": {
    en: "§9⭐⭐⭐⭐ [Adventurer EXP Material]",
    es: "§9⭐⭐⭐⭐ [Material de EXP de Aventurero]",
    ru: "§9⭐⭐⭐⭐ [Материал опыта искателя приключений]"
  },
  "item.awakening_system.heros_wit.desc": {
    en: "§7Experiences and insights of legendary heroes who walked Teyvat.",
    es: "§7Las experiencias y conocimientos de grandes héroes que caminaron por Teyvat.",
    ru: "§7Опыт и знания великих героев Тейвата."
  },
  "item.awakening_system.heros_wit.use1": {
    en: "§a▸ Right Click: §fConsume to gain §e+1,000 EXP",
    es: "§a▸ Clic derecho: §fConsumir para ganar §e+1,000 EXP",
    ru: "§a▸ ПКМ: §fДает §e+1,000 опыта"
  },
  "item.awakening_system.heros_wit.use2": {
    en: "§a▸ Shift + Right Click: §fConsume entire stack",
    es: "§a▸ Shift + Clic derecho: §fConsumir todo el stack de golpe",
    ru: "§a▸ Shift + ПКМ: §fПоглотить всю стопку"
  },

  // --- OCULUS & EXPLORATION ---
  "item.awakening_system.oculus.region": {
    en: "§7Region: %s",
    es: "§7Región: %s",
    ru: "§7Регион: %s"
  },
  "item.awakening_system.oculus.substance": {
    en: "§eSubstance of The Seven: §7Offer to Statues of The Seven for divine blessings.",
    es: "§eSustancia de los Siete: §7Ofrenda a las Estatuas de los Siete para recibir bendición divina.",
    ru: "§eСубстанция Семи: §7Подношение Статуям Семи за божественное благословение."
  },
  "item.awakening_system.oculus.lore": {
    en: "§8Hovers across the skies and hidden corners of Teyvat awaiting collection.",
    es: "§8Flota en los cielos y rincones ocultos de Teyvat esperando ser recolectado.",
    ru: "§8Парит в небесах и тайных уголках Тейвата."
  },

  "item.awakening_system.oculus_resonance_stone.element": {
    en: "§7Element: %s",
    es: "§7Elemento: %s",
    ru: "§7Элемент: %s"
  },
  "item.awakening_system.oculus_resonance_stone.region": {
    en: "§7Region: %s",
    es: "§7Región: %s",
    ru: "§7Регион: %s"
  },
  "item.awakening_system.oculus_resonance_stone.use": {
    en: "§e[Right Click] §7Tracks and projects a light beam towards the nearest %s.",
    es: "§e[Click Derecho] §7Rastrea y proyecta un haz luminoso hacia el %s más cercano.",
    ru: "§e[ПКМ] §7Ищет и указывает лучом на ближайший %s."
  },

  "item.awakening_system.teyvat_sigil.region": {
    en: "§7Region: %s",
    es: "§7Región: %s",
    ru: "§7Регион: %s"
  },
  "item.awakening_system.teyvat_sigil.use": {
    en: "§e[Right Click] §7Redeem for §6%s Mora§7.",
    es: "§e[Click Derecho] §7Canjear por §6%s Mora§7.",
    ru: "§e[ПКМ] §7Обменять на §6%s Моры§7."
  },
  "item.awakening_system.teyvat_sigil.lore": {
    en: "§8Ceremonial exchange currency honored by the peoples of Teyvat.",
    es: "§8Moneda de intercambio ceremonial venerada por las gentes de Teyvat.",
    ru: "§8Церемониальная валюта народов Тейвата."
  },

  "item.awakening_system.teyvat_shrine_key.nation": {
    en: "§7Nation: %s",
    es: "§7Nación: %s",
    ru: "§7Нация: %s"
  },
  "item.awakening_system.teyvat_shrine_key.lore": {
    en: "§8Capable of breaking sacred seals barring access to past civilizations' treasures.",
    es: "§8Capaz de romper los sellos sagrados que bloquean el acceso al tesoro de las civilizaciones pasadas.",
    ru: "§8Снимает священные печати с сокровищниц былых цивилизаций."
  },

  "item.awakening_system.clandestine_vault_key.title": {
    en: "§5✦ Underworld Vault Key",
    es: "§5✦ Llave de Bóveda del Inframundo",
    ru: "§5✦ Ключ от подпольного хранилища"
  },
  "item.awakening_system.clandestine_vault_key.desc": {
    en: "§7Forged in shadows with clandestine alloy and dark amethyst.",
    es: "§7Forjada en las sombras con aleación clandestina y amatista oscura.",
    ru: "§7Выкован в тени из подпольного сплава и аметиста."
  },
  "item.awakening_system.clandestine_vault_key.use_title": {
    en: "§d▶ Exclusive Owner Use:",
    es: "§d▶ Uso Exclusivo del Propietario:",
    ru: "§d▶ Исключительно для владельца:"
  },
  "item.awakening_system.clandestine_vault_key.use_desc1": {
    en: "§7Unlocks access to the prize pool and collection vault",
    es: "§7Desbloquea el acceso a la reserva de premios y a la",
    ru: "§7Открывает доступ к банку призов и хранилищу"
  },
  "item.awakening_system.clandestine_vault_key.use_desc2": {
    en: "§7of your §5Clandestine Slot Machine§7.",
    es: "§7bóveda de recaudación de tu §5Tragamonedas Clandestina§7.",
    ru: "§7вашего §5Подпольного игрового автомата§7."
  },
  "item.awakening_system.clandestine_vault_key.hint": {
    en: "§e[Shift + Right Click] §8on machine to access.",
    es: "§e[Shift + Clic Derecho] §8sobre la máquina para acceder.",
    ru: "§e[Shift + ПКМ] §8по автомату для доступа."
  },

  "item.awakening_system.forge_billet.series": {
    en: "§7Series: %s",
    es: "§7Serie: %s",
    ru: "§7Серия: %s"
  },
  "item.awakening_system.forge_billet.category": {
    en: "§7Weapon Type: §f%s",
    es: "§7Tipo de Arma: §f%s",
    ru: "§7Тип оружия: §f%s"
  },
  "item.awakening_system.forge_billet.material": {
    en: "§eForging Material: §7Used with Crystal Chunks and White Iron to craft 4★ weapons.",
    es: "§eMaterial de Forja: §7Se usa con Cristal Mágico y Trozos de Hierro Blanco para crear armas de 4 estrellas.",
    ru: "§eМатериал ковки: §7Используется для ковки 4★ оружия."
  },

  // --- GACHA CAPSULES ---
  "item.awakening_system.gacha_capsule.rarity_gold": {
    en: "§6✦ Rarity: §6§lLegendary (5★)",
    es: "§6✦ Rareza: §6§lLegendaria (5★)",
    ru: "§6✦ Редкость: §6§lЛегендарная (5★)"
  },
  "item.awakening_system.gacha_capsule.desc_gold": {
    en: "§7Stellar capsule of supreme purity from Teyvat's Gachapon.",
    es: "§7Cápsula estelar de máxima pureza del Gachapón de Teyvat.",
    ru: "§7Звездная капсула высшей чистоты Тейвата."
  },
  "item.awakening_system.gacha_capsule.guarantee_gold": {
    en: "§e✦ 100% Guaranteed 5★ Reward!",
    es: "§e✦ ¡Garantiza recompensa de 5★ al 100%!",
    ru: "§e✦ 100% гарантированная награда 5★!"
  },
  "item.awakening_system.gacha_capsule.prizes_gold": {
    en: "§6✦ Possible Prizes: §f3D Light Cones, Intertwined Fates, 100k Mora.",
    es: "§6✦ Posibles Premios: §fConos de Luz 3D, Destinos Entrelazados, 100k Mora.",
    ru: "§6✦ Возможные призы: §fСветовые конусы 3D, Судьбы, 100k Моры."
  },
  "item.awakening_system.gacha_capsule.rarity_purple": {
    en: "§d✦ Rarity: §d§lEpic (4★)",
    es: "§d✦ Rareza: §d§lÉpica (4★)",
    ru: "§d✦ Редкость: §d§lЭпическая (4★)"
  },
  "item.awakening_system.gacha_capsule.desc_purple": {
    en: "§7Capsule refined with astral essences.",
    es: "§7Cápsula refinada con esencias astrales.",
    ru: "§7Капсула, очищенная астральными эссенциями."
  },
  "item.awakening_system.gacha_capsule.rewards_purple": {
    en: "§d✦ Rewards: §f75% 4★ §7| §625% 5★",
    es: "§d✦ Recompensas: §f75% 4★ §7| §625% 5★",
    ru: "§d✦ Награды: §f75% 4★ §7| §625% 5★"
  },
  "item.awakening_system.gacha_capsule.prizes_purple": {
    en: "§d✦ Possible Prizes: §fRune Stones, Starglitter, Pills, Sigils, Mora.",
    es: "§d✦ Posibles Premios: §fPiedras de Runa, Brillo Estelar, Píldoras, Sigilos, Mora.",
    ru: "§d✦ Возможные призы: §fРунные камни, блеск, пилюли, печати, Мора."
  },
  "item.awakening_system.gacha_capsule.rarity_blue": {
    en: "§9✦ Rarity: §9§lRare (3★)",
    es: "§9✦ Rareza: §9§lRara (3★)",
    ru: "§9✦ Редкость: §9§lРедкая (3★)"
  },
  "item.awakening_system.gacha_capsule.desc_blue": {
    en: "§7Standard sealed capsule from Teyvat's Gachapon.",
    es: "§7Cápsula estándar sellada del Gachapón de Teyvat.",
    ru: "§7Стандартная запечатанная капсула Тейвата."
  },
  "item.awakening_system.gacha_capsule.rewards_blue": {
    en: "§b✦ Rewards: §b60% 3★ §7| §d30% 4★ §7| §610% 5★",
    es: "§b✦ Recompensas: §b60% 3★ §7| §d30% 4★ §7| §610% 5★",
    ru: "§b✦ Награды: §b60% 3★ §7| §d30% 4★ §7| §610% 5★"
  },
  "item.awakening_system.gacha_capsule.prizes_blue": {
    en: "§b✦ Possible Prizes: §fHero's Wit, Stardust, Elemental Sigils, Mora.",
    es: "§b✦ Posibles Premios: §fIngenio del Héroe, Polvo Estelar, Sigilos Elementales, Mora.",
    ru: "§b✦ Возможные призы: §fОпыт героя, пыль, элементальные печати, Мора."
  },
  "item.awakening_system.gacha_capsule.open": {
    en: "§eRight Click §7to open and claim your treasure.",
    es: "§eHaz clic derecho §7para abrir y reclamar tu tesoro.",
    ru: "§eПКМ§7, чтобы открыть и забрать сокровище."
  },

  // --- CASINO ITEMS ---
  "item.awakening_system.casino_chip.clandestine_title": {
    en: "§5§l✦ Clandestine Chip (Illegal Casino) ✦",
    es: "§5§l✦ Ficha Clandestina (Casino Ilegal) ✦",
    ru: "§5§l✦ Подпольная фишка (Нелегальное казино) ✦"
  },
  "item.awakening_system.casino_chip.clandestine_desc": {
    en: "§7Underworld token for the Clandestine Slot Machine.",
    es: "§7Ficha del bajo mundo para la Tragamonedas Clandestina.",
    ru: "§7Фишка преступного мира для подпольного автомата."
  },
  "item.awakening_system.casino_chip.black_market_val": {
    en: "✦ Black Market Value: §6%s Mora",
    es: "✦ Valor en el Mercado Negro: §6%s Mora",
    ru: "✦ Цена на черном рынке: §6%s Моры"
  },
  "item.awakening_system.casino_chip.clandestine_warn": {
    en: "§cHigh-stakes bets with prohibited underworld rewards.",
    es: "§cApuestas extremas con recompensas prohibidas del inframundo.",
    ru: "§cОпасные ставки с запретными наградами подполья."
  },
  "item.awakening_system.casino_chip.official_desc": {
    en: "§7Official chip of the Awakening Casino.",
    es: "§7Ficha oficial del Casino de Awakening.",
    ru: "§7Официальная фишка Казино Пробуждения."
  },
  "item.awakening_system.casino_chip.official_val": {
    en: "✦ Value: §6%s Mora",
    es: "✦ Valor: §6%s Mora",
    ru: "✦ Стоимость: §6%s Моры"
  },
  "item.awakening_system.casino_chip.official_hint": {
    en: "§8Right click on the Slot Machine to place your bet.",
    es: "§8Haz clic derecho en la Tragamonedas para apostar.",
    ru: "§8ПКМ по игровому автомату, чтобы сделать ставку."
  },

  "item.awakening_system.casino_trophy.winner": {
    en: "Legendary Champion",
    es: "Campeón Legendario",
    ru: "Легендарный чемпион"
  },
  "item.awakening_system.casino_trophy.feat": {
    en: "Championship Feat",
    es: "Hazaña de Campeonato",
    ru: "Чемпионский подвиг"
  },
  "item.awakening_system.casino_trophy.cat_derby": {
    en: "🏇 Uma Musume Grand Derby",
    es: "🏇 Gran Derby de Uma Musume",
    ru: "🏇 Гран-дерби Ума Мусуме"
  },
  "item.awakening_system.casino_trophy.cat_blackjack": {
    en: "♠️ High Roller Blackjack Tournament",
    es: "♠️ Torneo de Blackjack High Roller",
    ru: "♠️ Турнир хайроллеров по блэкджеку"
  },
  "item.awakening_system.casino_trophy.cat_jackpot": {
    en: "🎰 Casino & Roulette Grand Jackpot",
    es: "🎰 Gran Jackpot de Casino & Ruleta",
    ru: "🎰 Гран-джекпот казино и рулетки"
  },
  "item.awakening_system.casino_trophy.cat_billiards": {
    en: "🎱 8-Ball Billiards Tournament",
    es: "🎱 Torneo de Billar Bola 8",
    ru: "🎱 Турнир по бильярду (восьмерка)"
  },
  "item.awakening_system.casino_trophy.cat_darts": {
    en: "🎯 Professional Darts 180 Competition",
    es: "🎯 Competición de Dardos Profesional 180",
    ru: "🎯 Профессиональный дартс 180"
  },
  "item.awakening_system.casino_trophy.cat_default": {
    en: "🏆 Exclusive Casino Achievement",
    es: "🏆 Logro Exclusivo de Casino",
    ru: "🏆 Эксклюзивное достижение казино"
  },
  "item.awakening_system.casino_trophy.awarded_to": {
    en: "§e✦ Awarded to: §f%s",
    es: "§e✦ Condecorado: §f%s",
    ru: "§e✦ Награжден: §f%s"
  },
  "item.awakening_system.casino_trophy.feat_line": {
    en: "§b✦ Feat: §7%s",
    es: "§b✦ Hazaña: §7%s",
    ru: "§b✦ Подвиг: §7%s"
  },
  "item.awakening_system.casino_trophy.date_line": {
    en: "§8✦ Awarded: §7%s",
    es: "§8✦ Otorgado: §7%s",
    ru: "§8✦ Дата: §7%s"
  },
  "item.awakening_system.casino_trophy.hint": {
    en: "§7(Place in your showcase to admire)",
    es: "§7(Colócalo en tu vitrina para admirar)",
    ru: "§7(Поместите в витрину для любования)"
  },

  "item.awakening_system.casino_vip_pass.title": {
    en: "§6✦ Exclusive High Roller Membership",
    es: "§6✦ Membresía Exclusiva de Gran Apostador",
    ru: "§6✦ Эксклюзивное членство хайроллера"
  },
  "item.awakening_system.casino_vip_pass.desc1": {
    en: "§7Grants unrestricted passage through the §eVIP Gate§7.",
    es: "§7Concede paso irrestricto por la §eCancela VIP§7.",
    ru: "§7Дает свободный проход через §eVIP-ворота§7."
  },
  "item.awakening_system.casino_vip_pass.desc2": {
    en: "§7Access to VIP §eBlackjack§7 and §eRoulette§7 tables.",
    es: "§7Acceso a mesas VIP de §eBlackjack§7 y §eRuleta§7.",
    ru: "§7Доступ к VIP-столам §eблэкджека§7 и §eрулетки§7."
  },
  "item.awakening_system.casino_vip_pass.owner_bound": {
    en: "§a✦ Official Holder: §e%s",
    es: "§a✦ Titular Oficial: §e%s",
    ru: "§a✦ Владелец: §e%s"
  },
  "item.awakening_system.casino_vip_pass.status_bound": {
    en: "§c✦ Status: §4Soulbound",
    es: "§c✦ Estado: §4Intransferible (Soulbound)",
    ru: "§c✦ Статус: §4Персональный (Soulbound)"
  },
  "item.awakening_system.casino_vip_pass.owner_unbound": {
    en: "§e✦ Holder: §7Unregistered",
    es: "§e✦ Titular: §7Sin registrar",
    ru: "§e✦ Владелец: §7Не зарегистрирован"
  },
  "item.awakening_system.casino_vip_pass.bind_hint": {
    en: "§b✦ Right click in hand to bind to your name.",
    es: "§b✦ Haz clic derecho en mano para vincular a tu nombre.",
    ru: "§b✦ ПКМ в руке, чтобы привязать к себе."
  },
  "item.awakening_system.casino_vip_pass.footer": {
    en: "§8Royal Grand Casino of Teyvat • High-End Lounge",
    es: "§8Gran Casino Real de Teyvat • Sala de Alta Gama",
    ru: "§8Королевское казино Тейвата • VIP-зал"
  },

  "item.awakening_system.carat.title": {
    en: "§eGolden Tracen Academy carrot brimming with energy.",
    es: "§eZanahoria dorada de la Academia Tracen rebosante de energía.",
    ru: "§eЗолотая морковь Академии Трейсен, полная энергии."
  },
  "item.awakening_system.carat.desc": {
    en: "§7Official invocation currency to recruit your favorite racers.",
    es: "§7Moneda de invocación oficial para reclutar a tus corredoras favoritas.",
    ru: "§7Официальная валюта призыва любимых бегуний."
  },
  "item.awakening_system.carat.rates": {
    en: "§6• 1 Summon: §f150 🥕  §7|  §6• 10 Summons: §f1500 🥕",
    es: "§6• 1 Invocación: §f150 🥕  §7|  §6• 10 Invocaciones: §f1500 🥕",
    ru: "§6• 1 призыв: §f150 🥕  §7|  §6• 10 призывов: §f1500 🥕"
  },
  "item.awakening_system.carat.deposit1": {
    en: "§e▸ Right Click: §fDeposit 100 🥕 to stable",
    es: "§e▸ Clic derecho: §fDepositar 100 🥕 al establo",
    ru: "§e▸ ПКМ: §fВнести 100 🥕 в конюшню"
  },
  "item.awakening_system.carat.deposit_all": {
    en: "§e▸ Shift + Right Click: §fDeposit entire stack",
    es: "§e▸ Shift + Clic derecho: §fDepositar todo el stack",
    ru: "§e▸ Shift + ПКМ: §fВнести всю стопку"
  },

  // --- 5★ LIGHT CONES ---
  "item.awakening_system.light_cone.passing_shore.badge": {
    en: "§6✦ ⭐⭐⭐⭐⭐ ✦ §e§l[LEGENDARY 5★ LIGHT CONE]",
    es: "§6✦ ⭐⭐⭐⭐⭐ ✦ §e§l[CONO DE LUZ LEGENDARIO 5★]",
    ru: "§6✦ ⭐⭐⭐⭐⭐ ✦ §e§l[ЛЕГЕНДАРНЫЙ 5★ СВЕТОВОЙ КОНУС]"
  },
  "item.awakening_system.light_cone.passing_shore.path": {
    en: "§d✧ Path: §fNihility §7| §cExclusive: §fAcheron",
    es: "§d✧ Vía: §fNihilidad §7| §cExclusivo: §fAcheron",
    ru: "§d✧ Путь: §fНебытие §7| §cЭксклюзив: §fАхерон"
  },
  "item.awakening_system.light_cone.passing_shore.lore": {
    en: "§7\"Beneath the bloody rain of the River of Oblivion, neither past nor future remains.\"",
    es: "§7\"Bajo la lluvia de sangre del Río del Olvido, no queda ni pasado ni futuro.\"",
    ru: "§7«Под кровавым дождем Реки Забвения не осталось ни прошлого, ни будущего»."
  },
  "item.awakening_system.light_cone.passing_shore.stats_title": {
    en: "§c▸ Stellar Attributes in System Menu [K]:",
    es: "§c▸ Atributos Estelares en Menú [K]:",
    ru: "§c▸ Звездные атрибуты в меню Системы [K]:"
  },
  "item.awakening_system.light_cone.passing_shore.stat1": {
    en: "  §c⚔ +25% §7Overall Attack Damage and Stygian Slash bonus",
    es: "  §c⚔ +25% §7Daño de Ataque general y bonificación de corte estigio",
    ru: "  §c⚔ +25% §7к базовому урону и бонус рассечения"
  },
  "item.awakening_system.light_cone.passing_shore.stat2": {
    en: "  §e🎯 +12% §7Critical Rate §8| §d💥 +30% §7Critical Damage",
    es: "  §e🎯 +12% §7Probabilidad de Crítico §8| §d💥 +30% §7Daño Crítico",
    ru: "  §e🎯 +12% §7крит. шанса §8| §d💥 +30% §7крит. урона"
  },
  "item.awakening_system.light_cone.equip_hint": {
    en: "§8(Click your Light Cone slot in [K] to equip)",
    es: "§8(Haz clic en tu ranura de Cono de Luz en [K] para engarzarlo)",
    ru: "§8(Нажмите на слот Светового конуса в [K], чтобы экипировать)"
  },
  "item.awakening_system.light_cone.passing_shore.use_msg": {
    en: "§c✦ [LIGHT CONE] §fEquip this Light Cone in System Menu [K] to activate stellar attributes.",
    es: "§c✦ [CONO DE LUZ] §fEquipa este Cono de Luz en el Menú del Sistema [K] para activar sus atributos estelares y bonificaciones de combate pasivas.",
    ru: "§c✦ [СВЕТОВОЙ КОНУС] §fЭкипируйте в Меню Системы [K] для активации атрибутов."
  },

  "item.awakening_system.light_cone.before_dawn.path": {
    en: "§b✧ Path: §fErudition §7| §dExclusive: §fJing Yuan",
    es: "§b✧ Vía: §fErudición §7| §dExclusivo: §fJing Yuan",
    ru: "§b✧ Путь: §fЭрудиция §7| §dЭксклюзив: §fЦзин Юань"
  },
  "item.awakening_system.light_cone.before_dawn.lore": {
    en: "§7\"He who commands celestial soldiers watches calmly before dawn.\"",
    es: "§7\"Aquel que comanda a los soldados divinos observa con calma antes del alba.\"",
    ru: "§7«Командующий божественными воинами сохраняет спокойствие перед рассветом»."
  },
  "item.awakening_system.light_cone.before_dawn.stats_title": {
    en: "§e▸ Stellar Attributes in System Menu [K]:",
    es: "§e▸ Atributos Estelares en Menú [K]:",
    ru: "§e▸ Звездные атрибуты в меню Системы [K]:"
  },
  "item.awakening_system.light_cone.before_dawn.stat1": {
    en: "  §c⚔ +20% §7Celestial Attack Damage",
    es: "  §c⚔ +20% §7Daño de Ataque general celestial",
    ru: "  §c⚔ +20% §7к общему небесному урону"
  },
  "item.awakening_system.light_cone.before_dawn.stat2": {
    en: "  §d💥 +35% §7Massive Critical Damage and lightning resonance",
    es: "  §d💥 +35% §7Daño Crítico masivo y resonancia de relámpago",
    ru: "  §d💥 +35% §7к крит. урону и молниеносный резонанс"
  },

  "item.awakening_system.light_cone.milky_way.path": {
    en: "§b✧ Path: §fErudition §7| §cExclusive: §fHimeko",
    es: "§b✧ Vía: §fErudición §7| §cExclusivo: §fHimeko",
    ru: "§b✧ Путь: §fЭрудиция §7| §cЭксклюзив: §fХимеко"
  },
  "item.awakening_system.light_cone.milky_way.lore": {
    en: "§7\"Captures the light in a cup of coffee under an infinite sea of galaxies.\"",
    es: "§7\"Capta la luz de una taza de café bajo un mar interminable de galaxias.\"",
    ru: "§7«Ловит свет чашечки кофе под бесконечным океаном галактик»."
  },
  "item.awakening_system.light_cone.milky_way.stats_title": {
    en: "§d▸ Stellar Attributes in System Menu [K]:",
    es: "§d▸ Atributos Estelares en Menú [K]:",
    ru: "§d▸ Звездные атрибуты в меню Системы [K]:"
  },
  "item.awakening_system.light_cone.milky_way.stat1": {
    en: "  §c⚔ +20% §7Attack Damage §8| §b⚡ +20% §7Energy Recharge",
    es: "  §c⚔ +20% §7Daño de Ataque §8| §b⚡ +20% §7Recarga de Energía",
    ru: "  §c⚔ +20% §7к урону атаки §8| §b⚡ +20% §7к восстановлению энергии"
  },
  "item.awakening_system.light_cone.milky_way.stat2": {
    en: "  §e🎯 +8% §7Critical Rate §8| §d💥 +15% §7Critical Damage",
    es: "  §e🎯 +8% §7Probabilidad de Crítico §8| §d💥 +15% §7Daño Crítico",
    ru: "  §e🎯 +8% §7крит. шанса §8| §d💥 +15% §7крит. урона"
  },

  "item.awakening_system.light_cone.patience.path": {
    en: "§d✧ Path: §fNihility §7| §5Exclusive: §fKafka",
    es: "§d✧ Vía: §fNihilidad §7| §5Exclusivo: §fKafka",
    ru: "§d✧ Путь: §fНебытие §7| §5Эксклюзив: §fКафка"
  },
  "item.awakening_system.light_cone.patience.lore": {
    en: "§7\"Under a quiet rain, listen to your heartbeat... Boom.\"",
    es: "§7\"Bajo una lluvia tranquila, escucha el latir de tu corazón... Boom.\"",
    ru: "§7«Под тихим дождем прислушайся к биению своего сердца... Бум»."
  },
  "item.awakening_system.light_cone.patience.stats_title": {
    en: "§d▸ Stellar Attributes in System Menu [K]:",
    es: "§d▸ Atributos Estelares en Menú [K]:",
    ru: "§d▸ Звездные атрибуты в меню Системы [K]:"
  },
  "item.awakening_system.light_cone.patience.stat1": {
    en: "  §c⚔ +24% §7Attack Damage §8| §e🎯 +10% §7Critical Rate",
    es: "  §c⚔ +24% §7Daño de Ataque §8| §e🎯 +10% §7Probabilidad de Crítico",
    ru: "  §c⚔ +24% §7к урону атаки §8| §e🎯 +10% §7крит. шанса"
  },
  "item.awakening_system.light_cone.patience.stat2": {
    en: "  §5⚡ Acceleration stacks and lethal damage-over-time (DoT)",
    es: "  §5⚡ Cargas de aceleración y daño continuo letal (DoT)",
    ru: "  §5⚡ Заряды ускорения и смертельный периодический урон (DoT)"
  },

  "item.awakening_system.light_cone.dreams_rest.path": {
    en: "§6✧ Path: §fDestruction §7| §eExclusive: §fFirefly / SAM",
    es: "§6✧ Vía: §fDestrucción §7| §eExclusivo: §fFirefly / SAM",
    ru: "§6✧ Путь: §fРазрушение §7| §eЭксклюзив: §fСветлячок / СЭМ"
  },
  "item.awakening_system.light_cone.dreams_rest.lore": {
    en: "§7\"The awakening of Iron Cavalry beneath a shroud of incandescent embers.\"",
    es: "§7\"El despertar de la Caballería de Hierro bajo un manto de cenizas incandescentes.\"",
    ru: "§7«Пробуждение Железной кавалерии под покровом раскаленного пепла»."
  },
  "item.awakening_system.light_cone.dreams_rest.stats_title": {
    en: "§6▸ Stellar Attributes in System Menu [K]:",
    es: "§6▸ Atributos Estelares en Menú [K]:",
    ru: "§6▸ Звездные атрибуты в меню Системы [K]:"
  },
  "item.awakening_system.light_cone.dreams_rest.stat1": {
    en: "  §c⚔ +25% §7Attack Damage §8| §d💥 +20% §7Blazing Critical Damage",
    es: "  §c⚔ +25% §7Daño de Ataque §8| §d💥 +20% §7Daño Crítico ardiente",
    ru: "  §c⚔ +25% §7к урону §8| §d💥 +20% §7к пылающему крит. урону"
  },
  "item.awakening_system.light_cone.dreams_rest.stat2": {
    en: "  §6🔥 Break Effect and devastating combustion explosions",
    es: "  §6🔥 Efecto Rompedor y combustión de ruptura devastadora",
    ru: "  §6🔥 Эффект пробития и разрушительное горение"
  },

  // --- HSR LIGHT CONES COMMON & 3★ / 4★ ---
  "item.awakening_system.hsr_light_cone.header": {
    en: "§6✦ %s §6✦ %s §e[%s: %s]",
    es: "§6✦ %s §6✦ %s §e[%s: %s]",
    ru: "§6✦ %s §6✦ %s §e[%s: %s]"
  },
  "item.awakening_system.hsr_light_cone.path_label": {
    en: "Path",
    es: "Vía",
    ru: "Путь"
  },
  "item.awakening_system.hsr_light_cone.skill_header": {
    en: "§b▸ Active Skill: §f%s",
    es: "§b▸ Habilidad Activa: §f%s",
    ru: "§b▸ Активный навык: §f%s"
  },
  "item.awakening_system.hsr_light_cone.hint": {
    en: "§8(Right Click: Activate in combat | Socket in Menu [K])",
    es: "§8(Clic derecho: Activar en combate §8| §eEngarzar en Menú [K]§8)",
    ru: "§8(ПКМ: Активировать в бою | Установить в меню [K])"
  },
  "item.awakening_system.hsr_light_cone.cd_msg": {
    en: "§d✦ Light Cone on cooldown: §f%ss",
    es: "§d✦ Cono de Luz en enfriamiento: §f%ss",
    ru: "§d✦ Световой конус на перезарядке: §f%sс"
  },
  "item.awakening_system.hsr_light_cone.activate_msg": {
    en: "§d§l✦ [LIGHT CONE] §f%s activated! §7(Path of %s)",
    es: "§d§l✦ [CONO DE LUZ] §f¡%s activado! §7(Vía de la %s)",
    ru: "§d§l✦ [СВЕТОВОЙ КОНУС] §f%s активирован! §7(Путь: %s)"
  },
  "item.awakening_system.hsr_light_cone.rarity_4": {
    en: "[4★ EPIC]",
    es: "[4★ ÉPICO]",
    ru: "[4★ ЭПИЧЕСКИЙ]"
  },
  "item.awakening_system.hsr_light_cone.rarity_3": {
    en: "[3★ RARE]",
    es: "[3★ RARO]",
    ru: "[3★ РЕДКИЙ]"
  },

  // HSR Paths
  "item.awakening_system.hsr_path.hunt": { en: "The Hunt", es: "Cacería", ru: "Охота" },
  "item.awakening_system.hsr_path.cacería": { en: "The Hunt", es: "Cacería", ru: "Охота" },
  "item.awakening_system.hsr_path.abundance": { en: "Abundance", es: "Abundancia", ru: "Изобилие" },
  "item.awakening_system.hsr_path.abundancia": { en: "Abundance", es: "Abundancia", ru: "Изобилие" },
  "item.awakening_system.hsr_path.preservation": { en: "Preservation", es: "Conservación", ru: "Сохранение" },
  "item.awakening_system.hsr_path.conservación": { en: "Preservation", es: "Conservación", ru: "Сохранение" },
  "item.awakening_system.hsr_path.erudition": { en: "Erudition", es: "Erudición", ru: "Эрудиция" },
  "item.awakening_system.hsr_path.erudición": { en: "Erudition", es: "Erudición", ru: "Эрудиция" },
  "item.awakening_system.hsr_path.harmony": { en: "Harmony", es: "Armonía", ru: "Гармония" },
  "item.awakening_system.hsr_path.armonía": { en: "Harmony", es: "Armonía", ru: "Гармония" },
  "item.awakening_system.hsr_path.nihility": { en: "Nihility", es: "Nihilidad", ru: "Небытие" },
  "item.awakening_system.hsr_path.nihilidad": { en: "Nihility", es: "Nihilidad", ru: "Небытие" },
  "item.awakening_system.hsr_path.destruction": { en: "Destruction", es: "Destrucción", ru: "Разрушение" },
  "item.awakening_system.hsr_path.destrucción": { en: "Destruction", es: "Destrucción", ru: "Разрушение" },

  // --- LUNAR & TRILUNAR & EXPLORATION ---
  "item.awakening_system.lunar_authority.title": {
    en: "✦ Authority of %s (%s)",
    es: "✦ Autoridad de %s (%s)",
    ru: "✦ Власть: %s (%s)"
  },
  "item.awakening_system.lunar_authority.relic": {
    en: "§7[Sacred Relic of the Three Moon Goddesses]",
    es: "§7[Reliquia Sagrada de las Tres Diosas de la Luna]",
    ru: "§7[Священная реликвия Трех Лунных Богинь]"
  },
  "item.awakening_system.lunar_authority.altar": {
    en: "§e▸ Consecration: §fPlace all 3 authorities on the §dTrilunar Altar",
    es: "§e▸ Consagración: §fColoca las 3 autoridades en el §dAltar Trilunar",
    ru: "§e▸ Освящение: §fПоместите все 3 власти на §dТрилунный алтарь"
  },

  "item.awakening_system.trilunar_guide.title": {
    en: "§d✦ Sacred Scroll of Columbina",
    es: "§d✦ Pergamino Sagrado de Columbina",
    ru: "§d✦ Священный свиток Коломбины"
  },
  "item.awakening_system.trilunar_guide.desc": {
    en: "§7Contains the chronicle of the Three Moon Goddesses.",
    es: "§7Contiene la crónica de las Tres Diosas de la Luna.",
    ru: "§7Хроника Трех Лунных Богинь."
  },
  "item.awakening_system.trilunar_guide.goal": {
    en: "§6Goal: §fHelp Columbina restore her lost power.",
    es: "§6Objetivo: §fAyuda a Columbina a recuperar su poder perdido.",
    ru: "§6Цель: §fПомогите Коломбине вернуть утраченную силу."
  },
  "item.awakening_system.trilunar_guide.mission": {
    en: "§bTrial: §7Overcome the trials of Aria, Sonnet and Canon.",
    es: "§bMisión: §7Superar las pruebas de Aria, Sonnet y Canon.",
    ru: "§bИспытание: §7Пройдите испытания Арии, Соннет и Канон."
  },
  "item.awakening_system.trilunar_guide.target": {
    en: "§dReward: §eAwaken her divine power as Trilunar Goddess (+50% Primos/day).",
    es: "§dMeta: §eDespertar su poder como Diosa Trilunar (+50% Protos/día).",
    ru: "§dНаграда: §eПробуждение силы Трилунной Богини (+50% Примо/день)."
  },
  "item.awakening_system.trilunar_guide.use": {
    en: "§e▸ Right Click: §fCheck quest status and shrine guide",
    es: "§e▸ Clic derecho: §fConsultar estado de la misión y guía del santuario",
    ru: "§e▸ ПКМ: §fУзнать статус задания и руководство"
  },

  "item.awakening_system.track_marker_wand.title": {
    en: "§6Surveying and track design wand from Tracen Academy.",
    es: "§6Herramienta de topografía y trazado de pistas de la Academia Tracen.",
    ru: "§6Инструмент разметки трасс Академии Трейсен."
  },
  "item.awakening_system.track_marker_wand.stats": {
    en: "§7Waypoints: §e%s §7| Length: §e%sm",
    es: "§7Waypoints: §e%s §7| Longitud: §e%sm",
    ru: "§7Точки: §e%s §7| Длина: §e%sм"
  },
  "item.awakening_system.track_marker_wand.add_point": {
    en: "§a▸ Right Click on ground: §fRecord next Waypoint",
    es: "§a▸ Clic derecho en el suelo: §fRegistrar siguiente Waypoint",
    ru: "§a▸ ПКМ по земле: §fЗаписать следующую точку"
  },
  "item.awakening_system.track_marker_wand.undo": {
    en: "§e▸ Shift + Right Click: §fUndo last Waypoint",
    es: "§e▸ Shift + Clic derecho: §fDeshacer último Waypoint (Undo)",
    ru: "§e▸ Shift + ПКМ: §fОтменить последнюю точку"
  },
  "item.awakening_system.track_marker_wand.clear": {
    en: "§c▸ Shift + Click looking at sky: §fClear track circuit",
    es: "§c▸ Shift + Clic mirando al cielo: §fBorrar circuito",
    ru: "§c▸ Shift + клик в небо: §fОчистить трассу"
  },
  "item.awakening_system.track_marker_wand.link": {
    en: "§b▸ Right Click on Terminal: §fLink circuit to Racetrack",
    es: "§b▸ Clic derecho en Terminal: §fVincular circuito al Hipódromo",
    ru: "§b▸ ПКМ по терминалу: §fПривязать трассу к ипподрому"
  },

  "item.awakening_system.primogem_pickaxe.title": {
    en: "§b§l[ASTRAL RESONANCE]",
    es: "§b§l[RESONANCIA ASTRAL]",
    ru: "§b§l[АСТРАЛЬНЫЙ РЕЗОНАНС]"
  },
  "item.awakening_system.primogem_pickaxe.desc": {
    en: "§7Channels cosmic celestial force to uncover treasure:",
    es: "§7Canaliza la fuerza cósmica de las estrellas para excavar tesoros:",
    ru: "§7Канализирует силу звезд для добычи сокровищ:"
  },
  "item.awakening_system.primogem_pickaxe.eff1": {
    en: "§e✦ 25% chance to excavate Cosmic Fragments when mining ores.",
    es: "§e✦ 25% de prob. de desenterrar Fragmentos Cósmicos al picar minerales.",
    ru: "§e✦ 25% шанс добыть Космические фрагменты при вскапывании руды."
  },
  "item.awakening_system.primogem_pickaxe.eff2": {
    en: "§d✦ 6% chance to unearth pure Primogems directly.",
    es: "§d✦ 6% de prob. de hallar Protogemas puras directas.",
    ru: "§d✦ 6% шанс найти чистые Камни Истока."
  },

  // --- COMPANION CONTRACT ---
  "item.awakening_system.companion_contract.rarity": {
    en: "§eRarity: §6%s (%s★)",
    es: "§eRareza: §6%s (%s★)",
    ru: "§eРедкость: §6%s (%s★)"
  },
  "item.awakening_system.companion_contract.tactique": {
    en: "§7Tactical Archetype: §f%s",
    es: "§7Arquetipo Táctico: §f%s",
    ru: "§7Тактический архетип: §f%s"
  },
  "item.awakening_system.companion_contract.track_info": {
    en: "§7Distance: §f%s §8| §7Surface: §f%s",
    es: "§7Distancia: §f%s §8| §7Pista: §f%s",
    ru: "§7Дистанция: §f%s §8| §7Покрытие: §f%s"
  },
  "item.awakening_system.companion_contract.skill": {
    en: "§b✦ Signature Skill: §e%s",
    es: "§b✦ Habilidad Insignia: §e%s",
    ru: "§b✦ Фирменный навык: §e%s"
  },
  "item.awakening_system.companion_contract.unlocked": {
    en: "§a✔ Unlocked in your Tracen Academy Stable",
    es: "§a✔ Desbloqueada en tu Establo de Tracen Academy",
    ru: "§a✔ Разблокировано в конюшне Академии Трейсен"
  },
  "item.awakening_system.companion_contract.shards": {
    en: "§e+%s Shards gained (★ Talent Bloom)",
    es: "§e+%s Shards obtenidos (★ Talent Bloom)",
    ru: "§e+%s осколков получено (★ Talent Bloom)"
  },
  "item.awakening_system.companion_contract.race_hint": {
    en: "§7Train and race her from the Racetrack Terminal.",
    es: "§7Entrénala y compite desde la Terminal del Hipódromo.",
    ru: "§7Тренируйте и участвуйте в гонках через терминал ипподрома."
  },
  "item.awakening_system.companion_contract.companion_rarity_5": {
    en: " (Legendary 5★)",
    es: " (Legendario 5★)",
    ru: " (Легендарный 5★)"
  },
  "item.awakening_system.companion_contract.companion_rarity_4": {
    en: " (Epic 4★)",
    es: " (Épico 4★)",
    ru: " (Эпический 4★)"
  },
  "item.awakening_system.companion_contract.companion_level": {
    en: "§aCompanion Level: §2Lv. %s §7(Phase %s/6)",
    es: "§aNivel de Acompañante: §2Nv. %s §7(Fase %s/6)",
    ru: "§aУровень спутника: §2Ур. %s §7(Фаза %s/6)"
  },
  "item.awakening_system.companion_contract.constellation_awakened": {
    en: "§d✦ Awakened Constellation: C%s",
    es: "§d✦ Constelación Despertada: C%s",
    ru: "§d✦ Пробужденное созвездие: C%s"
  },
  "item.awakening_system.companion_contract.constellation_transcendent": {
    en: "§5✦ Transcendent Constellation: C%s (Infinite)",
    es: "§5✦ Constelación Trascendente: C%s (Infinita)",
    ru: "§5✦ Запредельное созвездие: C%s (Бесконечное)"
  },
  "item.awakening_system.companion_contract.element_path": {
    en: "§7Element / Path: %s%s",
    es: "§7Elemento / Sendero: %s%s",
    ru: "§7Элемент / Путь: %s%s"
  },
  "item.awakening_system.companion_contract.title": {
    en: "§7Title: §f%s",
    es: "§7Título: §f%s",
    ru: "§7Титул: §f%s"
  },
  "item.awakening_system.companion_contract.combat_skill_title": {
    en: "§b✦ Elemental Combat Skill:",
    es: "§b✦ Habilidad Elemental en Combate:",
    ru: "§b✦ Боевой элементальный навык:"
  },
  "item.awakening_system.companion_contract.summon_hint": {
    en: "§a✔ Right click a block to summon companion",
    es: "§a✔ Clic derecho en un bloque para invocar al acompañante",
    ru: "§a✔ ПКМ по блоку для призыва спутника"
  },
  "item.awakening_system.companion_contract.store_hint": {
    en: "§e✔ Shift + Right click companion to retrieve",
    es: "§e✔ Shift + Clic derecho en el acompañante para guardarlo",
    ru: "§e✔ Shift + ПКМ по спутнику, чтобы отозвать"
  },
  "item.awakening_system.companion_contract.upgrade_hint": {
    en: "§d✔ Upgrade level, talents and infinite constellations at Ascension Bench",
    es: "§d✔ Mejora nivel, talentos y constelaciones infinitas en la Mesa de Ascensión",
    ru: "§d✔ Улучшайте уровень, таланты и созвездия на Столе возвышения"
  },

  // --- GALACTIC BIG LOTTO ---
  "item.awakening_system.galactic_big_lotto.badge": {
    en: "§e⭐⭐⭐⭐⭐ [Simulated Universe Curio]",
    es: "§e⭐⭐⭐⭐⭐ [Curiosidad del Universo Simulado]",
    ru: "§e⭐⭐⭐⭐⭐ [Диковина Виртуальной вселенной]"
  },
  "item.awakening_system.galactic_big_lotto.desc": {
    en: "§7A lottery ticket of dubious cosmic provenance.",
    es: "§7Un billete de lotería de dudosa procedencia cósmica.",
    ru: "§7Лотерейный билет сомнительного космического происхождения."
  },
  "item.awakening_system.galactic_big_lotto.use": {
    en: "§6▸ Right Click: §fTry your luck for fabulous rewards.",
    es: "§6▸ Clic derecho: §fTentar a la suerte por fabulosos premios.",
    ru: "§6▸ ПКМ: §fИспытать удачу в погоне за сказочными призами."
  },
  "item.awakening_system.galactic_big_lotto.warn": {
    en: "§c⚠ WARNING: §420% chance to break and reduce health to 1 HP.",
    es: "§c⚠ ADVERTENCIA: §420% de probabilidad de que se destruya y deje tu salud en 1 HP.",
    ru: "§c⚠ ВНИМАНИЕ: §420% шанс сломаться и оставить вас с 1 HP."
  },

  // Extra helper keys for RobeOfTheBeauty
  "item.awakening_system.robe_of_the_beauty.current_bonus": {
    en: "  §b▸ Current Bonus: §f+%s%% Damage §7(§e%s §7Fragments owned)",
    es: "  §b▸ Bono Actual: §f+%s%% de Daño §7(§e%s §7Fragmentos poseídos)",
    ru: "  §b▸ Текущий бонус: §f+%s%% урона §7(§e%s §7фрагментов в наличии)"
  },
  "item.awakening_system.robe_of_the_beauty.preservation_msg": {
    en: "§d✦ [ROBE OF THE BEAUTY] §fCosmic Preservation activated! (-30 Cosmic Fragments) Damage mitigated.",
    es: "§d✦ [MANTO DE LA BELLEZA] §f¡Preservación Cósmica activada! (-30 Fragmentos Cósmicos) Daño mitigado.",
    ru: "§d✦ [МАНТИЯ КРАСОТЫ] §fКосмическое сохранение активировано! (-30 Космических фрагментов) Урон снижен."
  }
};

// ==========================================
// 2. INJECT INTO ALL 11 LANGUAGE FILES
// ==========================================
const langDir = 'src/main/resources/assets/awakening_system/lang';
const esFiles = [
  'es_es.json', 'es_ar.json', 'es_cl.json', 'es_co.json',
  'es_ec.json', 'es_mx.json', 'es_pe.json', 'es_uy.json', 'es_ve.json'
];

function updateLangFile(fileName, langKey) {
  const filePath = path.join(langDir, fileName);
  const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
  let added = 0;
  for (const [key, trans] of Object.entries(dict)) {
    data[key] = trans[langKey] || trans.en;
    added++;
  }
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf8');
  console.log(`Updated ${fileName} with ${added} entries. Total keys: ${Object.keys(data).length}`);
}

updateLangFile('en_us.json', 'en');
updateLangFile('ru_ru.json', 'ru');
for (const f of esFiles) {
  updateLangFile(f, 'es');
}

console.log('Language files successfully updated!');
