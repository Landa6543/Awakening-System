const fs = require('fs');
const path = require('path');

const finalDict = {
  "block.awakening_system.fishing_trap.desc": {
    en: "§7Only works underwater. Use fish baits.",
    es: "§7Sólo funciona bajo agua. Usa cebos de pesca.",
    ru: "§7Работает только под водой. Используйте наживку."
  },
  "item.awakening_system.dart.title": {
    en: "§6🎯 Professional Competition Dart",
    es: "§6🎯 Dardo de Competición Profesional",
    ru: "§6🎯 Профессиональный соревновательный дротик"
  },
  "item.awakening_system.dart.desc": {
    en: "§7Forged tungsten body and aerodynamic flights.",
    es: "§7Cuerpo de tungsteno forjado y plumas aerodinámicas.",
    ru: "§7Вольфрамовый корпус и аэродинамическое оперение."
  },
  "item.awakening_system.dart.fp_title": {
    en: "§e✦ First-Person Throw:",
    es: "§e✦ Lanzamiento en Primera Persona:",
    ru: "§e✦ Бросок от первого лица:"
  },
  "item.awakening_system.dart.fp_desc": {
    en: "  §7Hold in hand to activate the sway reticle.",
    es: "  §7Sostenlo en la mano para activar la retícula de oscilación.",
    ru: "  §7Держите в руке для включения прицела колебаний."
  },
  "item.awakening_system.dart.throw_right": {
    en: "  §a• Right Click: §fThrow dart toward wall Target Board.",
    es: "  §a• Clic Derecho: §fLanza el dardo hacia la Diana en la pared.",
    ru: "  §a• ПКМ: §fБросить дротик в мишень на стене."
  },
  "item.awakening_system.dart.crouch_shift": {
    en: "  §b• Shift (Sneak): §fStabilizes pulse to reduce sway.",
    es: "  §b• Shift (Agacharse): §fEstabiliza el pulso reduciendo la oscilación.",
    ru: "  §b• Shift (Присесть): §fСтабилизирует пульс, снижая разброс."
  },
  "item.awakening_system.elemental_gem.header": {
    en: "%s%s [%s Ascension Material]",
    es: "%s%s [Material de Ascensión %s]",
    ru: "%s%s [Материал возвышения (%s)]"
  },
  "item.awakening_system.elemental_gem.desc": {
    en: "§7Elemental gem imbued with the pure %s essence of Teyvat.",
    es: "§7Gema elemental imbuida con la pura esencia %s de Teyvat.",
    ru: "§7Элементальный самоцвет с чистой сущностью %s Тейвата."
  },
  "item.awakening_system.elemental_gem.use": {
    en: "§8▸ Used to ascend weapons and elemental abilities.",
    es: "§8▸ Utilizado para ascender armas y habilidades elementales.",
    ru: "§8▸ Используется для возвышения оружия и элементальных навыков."
  },
  "item.awakening_system.wyvern_egg.hatched_msg": {
    en: "§6A baby Wyvern has hatched! Feed it with Mana Meat to tame it!",
    es: "§6¡Ha nacido una cría de Wyvern! ¡Aliméntala con Carne de Maná para domesticarla!",
    ru: "§6Вылупился детеныш виверны! Покормите его мясом маны, чтобы приручить!"
  },
  "item.awakening_system.light_cone.use_msg": {
    en: "§6✦ [LIGHT CONE] §fEquip this Light Cone in System Menu [K] to activate its stellar attributes and passive combat bonuses.",
    es: "§6✦ [CONO DE LUZ] §fEquipa este Cono de Luz en el Menú del Sistema [K] para activar sus atributos estelares y bonificaciones de combate pasivas.",
    ru: "§6✦ [СВЕТОВОЙ КОНУС] §fЭкипируйте в Меню Системы [K] для активации звездных атрибутов и пассивных боевых бонусов."
  }
};

// 1. Update Lang Files
const langDir = 'src/main/resources/assets/awakening_system/lang';
const esFiles = [
  'es_es.json', 'es_ar.json', 'es_cl.json', 'es_co.json',
  'es_ec.json', 'es_mx.json', 'es_pe.json', 'es_uy.json', 'es_ve.json'
];

function updateLang(fileName, langKey) {
  const filePath = path.join(langDir, fileName);
  const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
  for (const [key, trans] of Object.entries(finalDict)) {
    data[key] = trans[langKey] || trans.en;
  }
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf8');
}

updateLang('en_us.json', 'en');
updateLang('ru_ru.json', 'ru');
for (const f of esFiles) {
  updateLang(f, 'es');
}
console.log('Final dictionary entries saved to all 11 language files!');

// 2. Patch FishingTrapBlock.java
let trap = fs.readFileSync('src/main/java/com/soybe/awakeningsystem/block/FishingTrapBlock.java', 'utf8');
trap = trap.replace(
  /tooltipComponents\.add\(Component\.literal\("§7Sólo funciona bajo agua\. Usa cebos"\)\);\s*tooltipComponents\.add\(Component\.literal\("§8\(Only works underwater\. Use baits\)"\)\);/,
  'tooltipComponents.add(Component.translatable("block.awakening_system.fishing_trap.desc"));'
);
fs.writeFileSync('src/main/java/com/soybe/awakeningsystem/block/FishingTrapBlock.java', trap, 'utf8');
console.log('Patched FishingTrapBlock.java');

// 3. Patch DartItem.java
let dart = fs.readFileSync('src/main/java/com/soybe/awakeningsystem/casino/darts/DartItem.java', 'utf8');
dart = dart.replace(
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.dart.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.dart.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.dart.fp_title").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.dart.fp_desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.dart.throw_right").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.dart.crouch_shift").withStyle(ChatFormatting.AQUA));
    }`
);
fs.writeFileSync('src/main/java/com/soybe/awakeningsystem/casino/darts/DartItem.java', dart, 'utf8');
console.log('Patched DartItem.java');

// 4. Patch ElementalGemItem.java
let gem = fs.readFileSync('src/main/java/com/soybe/awakeningsystem/item/ElementalGemItem.java', 'utf8');
gem = gem.replace(
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String starsStr = "★".repeat(stars);
        tooltip.add(Component.translatable("item.awakening_system.elemental_gem.header", colorCode, starsStr, elementName).withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.elemental_gem.desc", elementName).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.elemental_gem.use").withStyle(ChatFormatting.DARK_GRAY));
    }`
);
fs.writeFileSync('src/main/java/com/soybe/awakeningsystem/item/ElementalGemItem.java', gem, 'utf8');
console.log('Patched ElementalGemItem.java');

// 5. Patch CompanionContractItem.java
let comp = fs.readFileSync('src/main/java/com/soybe/awakeningsystem/item/CompanionContractItem.java', 'utf8');
comp = comp.replace(
  /tooltip\.add\(Component\.literal\("§e"\)\.append\(/,
  'tooltip.add('
);
fs.writeFileSync('src/main/java/com/soybe/awakeningsystem/item/CompanionContractItem.java', comp, 'utf8');
console.log('Patched CompanionContractItem.java');

// 6. Patch SystemItems.java Wyvern hatched message
let sys = fs.readFileSync('src/main/java/com/soybe/awakeningsystem/SystemItems.java', 'utf8');
sys = sys.replace(
  /ctx\.getPlayer\(\)\.displayClientMessage\(Component\.literal\("§6¡Ha nacido una cría de Wyvern! ¡Aliméntala con Carne de Maná para domesticarla!"\), false\);/,
  'ctx.getPlayer().displayClientMessage(Component.translatable("item.awakening_system.wyvern_egg.hatched_msg"), false);'
);
fs.writeFileSync('src/main/java/com/soybe/awakeningsystem/SystemItems.java', sys, 'utf8');
console.log('Patched SystemItems.java Wyvern hatched message');

// 7. Patch LightCone 5★ use messages
const cones5 = [
  'src/main/java/com/soybe/awakeningsystem/item/LightConeBeforeDawnItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/LightConeNightOnTheMilkyWayItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/LightConePatienceIsAllYouNeedItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/LightConeWhereDreamsShouldRestItem.java'
];

for (const c of cones5) {
  let cContent = fs.readFileSync(c, 'utf8');
  cContent = cContent.replace(
    /player\.displayClientMessage\(Component\.literal\("[^"]+\[CONO DE LUZ\][^"]+"\), true\);/,
    'player.displayClientMessage(Component.translatable("item.awakening_system.light_cone.use_msg"), true);'
  );
  fs.writeFileSync(c, cContent, 'utf8');
  console.log('Patched 5★ light cone use message in', c);
}

console.log('All final patches applied successfully!');
