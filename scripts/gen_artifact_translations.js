const fs = require('fs');

const sets = [
  {
    id: "crimson_witch",
    name: { en: "Crimson Witch of Flames", es: "Bruja Carmesí en Llamas", ru: "Горящая алая ведьма" },
    bonus2p: { en: "+20% Fire Damage and Pyro Skills.", es: "+20% Daño de Fuego y Habilidades Pyro.", ru: "+20% к урону от огня и Пиро навыкам." },
    bonus4p: {
      en: "Explosions and burns deal +40% additional damage and burn twice as long.",
      es: "Explosiones y quemaduras infligen +40% de daño adicional y queman por el doble de tiempo.",
      ru: "Взрывы и горение наносят +40% доп. урона и длятся вдвое дольше."
    }
  },
  {
    id: "gladiator",
    name: { en: "Gladiator's Finale", es: "Final del Gladiador", ru: "Конец гладиатора" },
    bonus2p: { en: "+18% Physical Attack and Base DMG.", es: "+18% Ataque Físico y Daño Base.", ru: "+18% к физической атаке и базовому урону." },
    bonus4p: {
      en: "+35% additional damage when hitting with Swords, Daggers, and Spears.",
      es: "+35% de Daño adicional al golpear con Espadas, Dagas y Lanzas.",
      ru: "+35% к урону при атаке мечами, кинжалами и копьями."
    }
  },
  {
    id: "viridescent",
    name: { en: "Viridescent Venerer", es: "Sombra Verde Esmeralda", ru: "Изумрудная тень" },
    bonus2p: { en: "+20% Anemo / Wind DMG and +15% Speed.", es: "+20% Daño Anemo / Viento y +15% Velocidad.", ru: "+20% к Анемо урону и +15% к скорости." },
    bonus4p: {
      en: "Swirls reduce enemy physical and elemental resistance by 30%.",
      es: "Los torbellinos de viento reducen la resistencia física y elemental del enemigo un 30%.",
      ru: "Рассеивание снижает сопротивление врага физическому и стихийному урону на 30%."
    }
  },
  {
    id: "maiden",
    name: { en: "Maiden Beloved", es: "Doncella Amada", ru: "Возлюбленная юная дева" },
    bonus2p: { en: "+20% Healing Effectiveness and Regeneration.", es: "+20% Eficacia de Curación y Regeneración.", ru: "+20% к эффективности лечения и регенерации." },
    bonus4p: {
      en: "Healing yourself or a companion restores an additional 25% health to nearby allies.",
      es: "Al curarte a ti o a un compañero, restaura un 25% de salud adicional a todos los aliados cercanos.",
      ru: "При лечении себя или спутника восстанавливает еще 25% здоровья всем союзникам поблизости."
    }
  },
  {
    id: "bloodstained",
    name: { en: "Bloodstained Chivalry", es: "Caballero Sanguinario", ru: "Рыцарь крови" },
    bonus2p: { en: "+25% Physical DMG and Penetration.", es: "+25% Daño Físico y Penetración.", ru: "+25% к физ. урону и пробиванию." },
    bonus4p: {
      en: "Defeating an enemy grants +50% DMG to the next strike and knocks down the target.",
      es: "Derrotar a un enemigo otorga +50% de Daño al siguiente impacto y derriba al objetivo.",
      ru: "Победа над врагом дает +50% к урону следующего удара и опрокидывает цель."
    }
  },
  {
    id: "berserker",
    name: { en: "Berserker", es: "Berserker", ru: "Берсерк" },
    bonus2p: { en: "+12% Critical Hit Rate.", es: "+12% Probabilidad de Golpe Crítico.", ru: "+12% к шансу критического удара." },
    bonus4p: {
      en: "When HP drops below 70%, Critical Rate increases by an additional +24%.",
      es: "Al estar por debajo del 70% de vida, la Probabilidad Crítica aumenta un +24% adicional.",
      ru: "Когда здоровье падает ниже 70%, шанс крита увеличивается еще на +24%."
    }
  },
  {
    id: "exile",
    name: { en: "The Exile", es: "El Exiliado", ru: "Изгнанник" },
    bonus2p: { en: "+20% Mana and Energy Recharge.", es: "+20% Recarga de Maná y Energía.", ru: "+20% к восстановлению маны и энергии." },
    bonus4p: {
      en: "Activating an ultimate or elemental skill instantly restores Mana for you and allies.",
      es: "Activar una habilidad definitiva o elemental regenera Maná instantáneo para ti y tus aliados.",
      ru: "Использование ультимейта или навыка мгновенно восстанавливает ману вам и союзникам."
    }
  },
  {
    id: "brave_heart",
    name: { en: "Brave Heart", es: "Corazón del Guerrero", ru: "Душа храбреца" },
    bonus2p: { en: "+18% General Attack.", es: "+18% Ataque General.", ru: "+18% к общей атаке." },
    bonus4p: {
      en: "+30% increased DMG against enemies that have more than 50% HP remaining.",
      es: "+30% de Daño aumentado contra enemigos que tengan más del 50% de su vida restante.",
      ru: "+30% к урону по врагам, у которых осталось более 50% здоровья."
    }
  },
  {
    id: "noblesse",
    name: { en: "Noblesse Oblige", es: "Nobleza del Antiguo Régimen", ru: "Церемония древней знати" },
    bonus2p: { en: "+20% Ultimate Skill DMG.", es: "+20% Daño de Habilidad Definitiva.", ru: "+20% к урону взрыва стихии." },
    bonus4p: {
      en: "Using an Ultimate increases all team members' ATK by 20% for 12s.",
      es: "Lanzar una Habilidad Definitiva aumenta el ATQ de todos los miembros del equipo un 20% durante 12s.",
      ru: "Применение взрыва стихии увеличивает силу атаки всех членов команды на 20% на 12 сек."
    }
  },
  {
    id: "emblem",
    name: { en: "Emblem of Severed Fate", es: "Emblema del Destino", ru: "Эмблема рассечённой судьбы" },
    bonus2p: { en: "+20% Energy and Mana Recharge.", es: "+20% Recarga de Energía y Maná.", ru: "+20% к восстановлению энергии и маны." },
    bonus4p: {
      en: "Increases Ultimate Skill DMG by 25% of total Energy Recharge (max 75%).",
      es: "Aumenta el daño de las Habilidades Definitivas en un 25% de la Recarga de Energía total (máx. 75%).",
      ru: "Увеличивает урон взрыва стихии на 25% от общей величины восстановления энергии (макс. 75%)."
    }
  },
  {
    id: "thundering_fury",
    name: { en: "Thundering Fury", es: "Furia del Trueno", ru: "Громогласный рёв ярости" },
    bonus2p: { en: "+15% Electro DMG Bonus.", es: "+15% Bono de Daño Electro.", ru: "+15% к бонусу Электро урона." },
    bonus4p: {
      en: "Increases damage dealt by Overloaded, Electro-Charged, and Superconduct by 40%.",
      es: "Aumenta el daño infligido por Sobrecarga, Electrocargado y Superconductor un 40%.",
      ru: "Увеличивает урон реакций Перегрузка, Заряжен и Сверхпроводник на 40%."
    }
  },
  {
    id: "blizzard_strayer",
    name: { en: "Blizzard Strayer", es: "Nómada del Invierno", ru: "Заблудший в метели" },
    bonus2p: { en: "+15% Cryo DMG Bonus.", es: "+15% Bono de Daño Cryo.", ru: "+15% к бонусу Крио урона." },
    bonus4p: {
      en: "When attacking Cryo-affected enemies, CRIT Rate +20%. If Frozen, additional +20%.",
      es: "Al atacar a un enemigo afectado por Cryo, Prob. CRIT +20%. Si el enemigo está Congelado, +20% adicional.",
      ru: "При атаке противников с эффектом Крио шанс крита +20%. Если заморожен, еще +20%."
    }
  },
  {
    id: "heart_of_depth",
    name: { en: "Heart of Depth", es: "Corazón de las Profundidades", ru: "Сердце глубин" },
    bonus2p: { en: "+15% Hydro DMG Bonus.", es: "+15% Bono de Daño Hydro.", ru: "+15% к бонусу Гидро урона." },
    bonus4p: {
      en: "After using an Elemental Skill, increases Normal and Charged Attack DMG by 30% for 15s.",
      es: "Tras usar una Habilidad Elemental, aumenta el daño de Ataques Normales y Cargados un 30% por 15s.",
      ru: "После использования элементального навыка увеличивает урон обычной и заряженной атаки на 30% на 15 сек."
    }
  },
  {
    id: "archaic_petra",
    name: { en: "Archaic Petra", es: "Petra Arcaica", ru: "Архаичный камень" },
    bonus2p: { en: "+15% Geo DMG Bonus.", es: "+15% Bono de Daño Geo.", ru: "+15% к бонусу Гео урона." },
    bonus4p: {
      en: "Upon obtaining an elemental shard from Crystallize, all party members gain +35% DMG Bonus for that element.",
      es: "Al obtener un fragmento generado por Cristalización, todos los aliados obtienen +35% de Bono de Daño Elemental.",
      ru: "При подборе элементального осколка от Кристаллизации все союзники получают +35% к урону этой стихии."
    }
  },
  {
    id: "deepwood",
    name: { en: "Deepwood Memories", es: "Recuerdos de la Floresta", ru: "Воспоминания дремучего леса" },
    bonus2p: { en: "+15% Dendro DMG Bonus.", es: "+15% Bono de Daño Dendro.", ru: "+15% к бонусу Дендро урона." },
    bonus4p: {
      en: "Skills decrease the Dendro RES of hit opponents by 30% for 8s.",
      es: "Las Habilidades reducen la Resistencia Dendro de los enemigos golpeados en un 30% durante 8s.",
      ru: "Навыки снижают Дендро сопротивление пораженных врагов на 30% на 8 сек."
    }
  },
  {
    id: "shimenawa",
    name: { en: "Shimenawa's Reminiscence", es: "Reminiscencia de la Purificación", ru: "Воспоминания Симэнавы" },
    bonus2p: { en: "+18% General Attack.", es: "+18% Ataque General.", ru: "+18% к общей атаке." },
    bonus4p: {
      en: "When using an Elemental Skill with 15+ Energy, consumes energy to increase Normal/Charged DMG by 50%.",
      es: "Al usar una Habilidad Elemental con 15+ de Energía, consume energía para aumentar el daño normal y cargado un 50%.",
      ru: "При активации элементального навыка при 15+ энергии расходует ее, увеличивая урон обычной/заряженной атаки на 50%."
    }
  },
  {
    id: "desert_pavilion",
    name: { en: "Desert Pavilion Chronicle", es: "Épica del Pabellón del Desierto", ru: "Хроники Чертогов в пустыне" },
    bonus2p: { en: "+15% Anemo DMG Bonus.", es: "+15% Bono de Daño Anemo.", ru: "+15% к бонусу Анемо урона." },
    bonus4p: {
      en: "When a Charged Attack hits, Normal Attack SPD +10% and attack DMG increases by 40% for 15s.",
      es: "Al golpear con un Ataque Cargado, la Vel. de Ataque aumenta un 10% y el daño infligido un 40% por 15s.",
      ru: "При попадании заряженной атакой скорость обычной атаки +10%, а весь урон увеличивается на 40% на 15 сек."
    }
  },
  {
    id: "ocean_clam",
    name: { en: "Ocean-Hued Clam", es: "Perla Oceánica", ru: "Моллюск морских красок" },
    bonus2p: { en: "+15% Healing Bonus.", es: "+15% Bono de Curación.", ru: "+15% к бонусу лечения." },
    bonus4p: {
      en: "Accumulates restored health into a Sea-Dyed Foam bubble that explodes dealing Physical DMG equal to 90%.",
      es: "Acumula la salud restaurada en una Burbuja Espumosa marina que explota infligiendo daño físico equivalente al 90%.",
      ru: "Накапливает исцеленное здоровье в морской пене, взрыв которой наносит физический урон в размере 90%."
    }
  },
  {
    id: "tenacity",
    name: { en: "Tenacity of the Millelith", es: "Tenacidad de la Geoarmada", ru: "Стойкость Миллелита" },
    bonus2p: { en: "+20% Max Health.", es: "+20% Vida Máxima.", ru: "+20% к макс. здоровью." },
    bonus4p: {
      en: "When an Elemental Skill hits an opponent, party members gain +20% ATK and +30% Shield Strength.",
      es: "Al golpear a un enemigo con una Habilidad, los miembros del equipo obtienen +20% ATQ y +30% Protección de Escudo.",
      ru: "При попадании элементальным навыком члены команды получают +20% к атаке и +30% к прочности щита."
    }
  },
  {
    id: "golden_troupe",
    name: { en: "Golden Troupe", es: "Compañía Dorada", ru: "Золотая труппа" },
    bonus2p: { en: "+20% Elemental Skill DMG.", es: "+20% Daño de Habilidad Elemental.", ru: "+20% к урону элементального навыка." },
    bonus4p: {
      en: "Increases Elemental Skill DMG by an additional 25% while the character is not active in combat.",
      es: "Aumenta el daño de Habilidad Elemental un 25% adicional mientras el personaje no esté en combate activo.",
      ru: "Увеличивает урон элементального навыка еще на 25%, когда персонаж не находится на поле боя."
    }
  }
];

const data = { en: {}, es: {}, ru: {} };

for (const s of sets) {
  const kName = `awakening_system.artifact_set.${s.id}.name`;
  const kB2 = `awakening_system.artifact_set.${s.id}.bonus2p`;
  const kB4 = `awakening_system.artifact_set.${s.id}.bonus4p`;

  data.en[kName] = s.name.en;
  data.es[kName] = s.name.es;
  data.ru[kName] = s.name.ru;

  data.en[kB2] = s.bonus2p.en;
  data.es[kB2] = s.bonus2p.es;
  data.ru[kB2] = s.bonus2p.ru;

  data.en[kB4] = s.bonus4p.en;
  data.es[kB4] = s.bonus4p.es;
  data.ru[kB4] = s.bonus4p.ru;
}

fs.writeFileSync('scripts/item_translations_artifacts.json', JSON.stringify(data, null, 2), 'utf8');
console.log('Saved artifacts translations:', Object.keys(data.en).length, 'keys');
