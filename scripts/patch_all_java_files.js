const fs = require('fs');
const path = require('path');

function patchFile(filePath, searchRegexOrStr, replacement) {
  if (!fs.existsSync(filePath)) {
    console.error('File not found:', filePath);
    return false;
  }
  let content = fs.readFileSync(filePath, 'utf8');
  const isCrlf = content.includes('\r\n');
  const normContent = content.replace(/\r\n/g, '\n');
  let newContent;

  if (typeof searchRegexOrStr === 'string') {
    const normSearch = searchRegexOrStr.replace(/\r\n/g, '\n');
    if (!normContent.includes(normSearch)) {
      console.warn('String not found in', filePath);
      return false;
    }
    newContent = normContent.replace(normSearch, replacement.replace(/\r\n/g, '\n'));
  } else {
    if (!searchRegexOrStr.test(normContent)) {
      console.warn('Regex not matched in', filePath);
      return false;
    }
    newContent = normContent.replace(searchRegexOrStr, replacement.replace(/\r\n/g, '\n'));
  }

  if (isCrlf) {
    newContent = newContent.replace(/\n/g, '\r\n');
  }
  fs.writeFileSync(filePath, newContent, 'utf8');
  console.log('Successfully patched:', filePath);
  return true;
}

// -------------------------------------------------------------
// 1. PATCH SystemItems.java
// -------------------------------------------------------------
console.log('--- Patching SystemItems.java ---');
const sysItemsMap = [
  {
    find: /tooltip\.add\(Component\.literal\("§7Asiento ergonómico de acero inoxidable de los vagones del Metro de Seúl\."\)\);\s*tooltip\.add\(Component\.literal\("§e✦ Clic derecho para sentarse y cultivar Qi \(\+4\/s\)\."\)\);/,
    replace: `tooltip.add(Component.translatable("block.awakening_system.subway_passenger_seat.desc"));\n                    tooltip.add(Component.translatable("block.awakening_system.subway_passenger_seat.use"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§7Placas de acero inoxidable de grado ferroviario\."\)\);\s*tooltip\.add\(Component\.literal\("§e✦ Bloque de construcción para chasis, piso y carrocería de vagones de metro\."\)\);/,
    replace: `tooltip.add(Component.translatable("block.awakening_system.subway_stainless_steel.desc"));\n                    tooltip.add(Component.translatable("block.awakening_system.subway_stainless_steel.use"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§7Puerta corrediza doble de metro con ventanal reforzado\."\)\);\s*tooltip\.add\(Component\.literal\("§a✦ Apertura doble sincronizada para abordaje desde andenes y estaciones\."\)\);/,
    replace: `tooltip.add(Component.translatable("block.awakening_system.subway_door.desc"));\n                    tooltip.add(Component.translatable("block.awakening_system.subway_door.use"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§7Baldosas de terrazo pulido para andenes de estaciones de metro \(ORV\)\."\)\);\s*tooltip\.add\(Component\.literal\("§e✦ Permite construir andenes de abordaje a nivel del vagón\."\)\);/,
    replace: `tooltip.add(Component.translatable("block.awakening_system.subway_platform.desc"));\n                    tooltip.add(Component.translatable("block.awakening_system.subway_platform.use"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§7Borde de andén con franja podotáctil y línea amarilla de seguridad\."\)\);\s*tooltip\.add\(Component\.literal\("§6✦ Orienta mirando hacia las vías férreas para delimitar el andén de espera\."\)\);/,
    replace: `tooltip.add(Component.translatable("block.awakening_system.subway_platform_edge.desc"));\n                    tooltip.add(Component.translatable("block.awakening_system.subway_platform_edge.use"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§6§l\[Material de Autómata ✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Dispositivo del Caos obtenido de un Guardián de las Ruinas\."\)\);\s*tooltip\.add\(Component\.literal\("§8Estructura de engranajes arcanos proveniente de la antigua Khaenri'ah\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.chaos_device.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.chaos_device.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.chaos_device.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§5§l\[Material de Autómata ✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Circuito del Caos con energía biomecánica residual\."\)\);\s*tooltip\.add\(Component\.literal\("§8Canaliza impulsos motrices de los autómatas destructores\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.chaos_circuit.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.chaos_circuit.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.chaos_circuit.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§d§l\[Material de Autómata ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Núcleo del Caos perfecto y palpitante de energía infinita\."\)\);\s*tooltip\.add\(Component\.literal\("§8El corazón motriz supremo de la maquinaria de guerra de Khaenri'ah\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.chaos_core.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.chaos_core.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.chaos_core.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§a§l\[Material del Abismo ✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Ramita seca obtenida de un Mago del Abismo\."\)\);\s*tooltip\.add\(Component\.literal\("§8Contiene vestigios marchitos de energía telúrica subterránea\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.dead_ley_line_branch.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.dead_ley_line_branch.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.dead_ley_line_branch.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§9§l\[Material del Abismo ✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Hojas secas de las ramas profundas de las líneas ley\."\)\);\s*tooltip\.add\(Component\.literal\("§8Canalizan los misterios arcanos de la sabiduría del mundo\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.dead_ley_line_leaves.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.dead_ley_line_leaves.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.dead_ley_line_leaves.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§5§l\[Material del Abismo ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Brote tierno y resplandeciente de las líneas ley\."\)\);\s*tooltip\.add\(Component\.literal\("§8Fluye con la corriente vital más pura del árbol que sostiene la creación\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.ley_line_sprout.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.ley_line_sprout.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.ley_line_sprout.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§b§l\[Material de Ascensión ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Semilla cilíndrica dejada por el Hipóstasis Anemo\."\)\);\s*tooltip\.add\(Component\.literal\("§8Contiene el poder de la tempestad primordial que surca los cielos\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.hurricane_seed.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.hurricane_seed.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.hurricane_seed.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§d§l\[Material de Ascensión ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Prisma brillante abandonado por el Hipóstasis Electro\."\)\);\s*tooltip\.add\(Component\.literal\("§8Refracta la energía de relámpago pura y de alta concentración\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.lightning_prism.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.lightning_prism.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.lightning_prism.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§6§l\[Material de Ascensión ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Pilar de piedra densa caído del Hipóstasis Geo\."\)\);\s*tooltip\.add\(Component\.literal\("§8El núcleo terrenal imbuido con la gravedad inquebrantable de la tierra\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.basalt_pillar.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.basalt_pillar.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.basalt_pillar.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§3§l\[Material de Ascensión ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Flor cristalina gélida desprendida del Hipóstasis Cryo\."\)\);\s*tooltip\.add\(Component\.literal\("§8Emana una escarcha perpetua capaz de congelar el flujo elemental\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.crystalline_bloom.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.crystalline_bloom.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.crystalline_bloom.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§c§l\[Material de Ascensión ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Perla ardiente obtenida del núcleo del Hipóstasis Pyro\."\)\);\s*tooltip\.add\(Component\.literal\("§8Arde con una llama inextinguible nacida de la combustión más pura\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.smoldering_pearl.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.smoldering_pearl.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.smoldering_pearl.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§9§l\[Material de Ascensión ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Gota de agua pristina condensada del Hipóstasis Hydro\."\)\);\s*tooltip\.add\(Component\.literal\("§8Rechaza cualquier impureza en su corriente y fluye eternamente viva\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.dew_of_repudiation.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.dew_of_repudiation.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.dew_of_repudiation.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§b§l\[Material de Ascensión ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Núcleo de Escarcha puro desprendido de la Regisvid Criogénica\."\)\);\s*tooltip\.add\(Component\.literal\("§8Contiene la esencia vegetal sobreenfriada nacida de las líneas ley\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.hoarfrost_core.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.hoarfrost_core.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.hoarfrost_core.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§c§l\[Material de Ascensión ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Semilla de llama inextinguible obtenida de la Regisvid Pyro\."\)\);\s*tooltip\.add\(Component\.literal\("§8Nutrida por el fuego elemental supremo que arde en la tierra\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.everflame_seed.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.everflame_seed.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.everflame_seed.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§2§l\[Material RPG Rango S\] §7Escama ultraligera y resistente de depredador aéreo\."\)\);\s*tooltip\.add\(Component\.literal\("§8Inmune a la corrosión de ácidos y quemaduras\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.wyvern_scale.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.wyvern_scale.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§a§l\[Material RPG\] §7Colmillo afilado empapado en ponzoña de wyvern\."\)\);\s*tooltip\.add\(Component\.literal\("§8Utilizado en la forja de armas con sangrado y veneno\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.wyvern_fang.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.wyvern_fang.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§6§l\[Huevo Mítico de Wyvern\] §fRobado de los riscos y nidos más altos\."\)\);\s*tooltip\.add\(Component\.literal\("§e¡Incúbalo cerca de fogatas o lava para obtener una cría domesticable!"\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.wyvern_egg.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.wyvern_egg.hint"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§d§l\[Arnés de Jinete Aéreo\] §fMontura artesanal reforzada con correas de cuero draconiano\."\)\);\s*tooltip\.add\(Component\.literal\("§bEquípala en un Wyvern domesticado para surcar los cielos a toda velocidad\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.wyvern_saddle.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.wyvern_saddle.hint"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§a§l\[Material de Mitachurl ✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Un cuerno tosco y curvado arrancado de un Mitachurl\."\)\);\s*tooltip\.add\(Component\.literal\("§8Se suele emplear como bocina de combate en los campamentos Hilichurl\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.heavy_horn.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.heavy_horn.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.heavy_horn.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§9§l\[Material de Mitachurl ✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Un cuerno metálico teñido de un bronce oscuro y amenazante\."\)\);\s*tooltip\.add\(Component\.literal\("§8Simboliza el estatus de capitán y combatiente veterano entre los suyos\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.black_bronze_horn.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.black_bronze_horn.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.black_bronze_horn.lore"));`
  },
  {
    find: /tooltip\.add\(Component\.literal\("§d§l\[Material de Lawachurl ✦✦✦✦\]"\)\);\s*tooltip\.add\(Component\.literal\("§7Cuerno colosal cristalizado con vetas de energía elemental pura\."\)\);\s*tooltip\.add\(Component\.literal\("§8Perteneciente al cacique supremo Lawachurl Acorazado\."\)\);/,
    replace: `tooltip.add(Component.translatable("item.awakening_system.black_crystal_horn.badge"));\n                    tooltip.add(Component.translatable("item.awakening_system.black_crystal_horn.desc"));\n                    tooltip.add(Component.translatable("item.awakening_system.black_crystal_horn.lore"));`
  }
];

let sysFile = fs.readFileSync('src/main/java/com/soybe/awakeningsystem/SystemItems.java', 'utf8');
const isCrlf = sysFile.includes('\r\n');
let normSys = sysFile.replace(/\r\n/g, '\n');

let patchedCount = 0;
for (const entry of sysItemsMap) {
  if (entry.find.test(normSys)) {
    normSys = normSys.replace(entry.find, entry.replace);
    patchedCount++;
  } else {
    console.warn('SystemItems pattern not matched:', entry.find);
  }
}

if (isCrlf) normSys = normSys.replace(/\n/g, '\r\n');
fs.writeFileSync('src/main/java/com/soybe/awakeningsystem/SystemItems.java', normSys, 'utf8');
console.log(`SystemItems.java: successfully patched ${patchedCount} / ${sysItemsMap.length} items.`);

// -------------------------------------------------------------
// 2. PATCH INDIVIDUAL ITEM CLASSES
// -------------------------------------------------------------
console.log('--- Patching Item Classes ---');

// RobeOfTheBeautyItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/RobeOfTheBeautyItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.badge").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.passive_title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.passive_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.passive_desc2"));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientTooltip.addCosmicFragmentTooltip(tooltip);
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_desc2"));
    }`
);
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/RobeOfTheBeautyItem.java',
  /player\.displayClientMessage\(Component\.literal\("§d✦ \[MANTO DE LA BELLEZA\] §f¡Preservación Cósmica activada! \(-30 Fragmentos Cósmicos\) Daño mitigado\."\), true\);/,
  `player.displayClientMessage(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_msg"), true);`
);
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/RobeOfTheBeautyItem.java',
  /tooltip\.add\(Component\.literal\("  §b▸ Bono Actual: §f\+" \+ bonus \+ "% de Daño §7\(§e" \+ frags \+ " §7Fragmentos poseídos\)"\)\);/,
  `tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.current_bonus", bonus, frags));`
);

// WildfireCrownItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/WildfireCrownItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.awakening_system.wildfire_crown.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltipComponents.add(Component.translatable("item.awakening_system.wildfire_crown.lore"));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.awakening_system.wildfire_crown.blessing_title").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("item.awakening_system.wildfire_crown.blessing_desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }`
);

// TeyvatDelusionItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/TeyvatDelusionItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.element").append(Component.literal(element.format + element.title)));
        if (element != DelusionElement.BLANK) {
            tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.active"));
            tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.warn"));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.blank"));
        }
    }`
);

// TeyvatVisionItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/TeyvatVisionItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.element").append(Component.literal(element.name)).withStyle(element.format));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.resonance").withStyle(ChatFormatting.AQUA));
    }`
);

// VigorPotionItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/VigorPotionItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.vigor_potion.title"));
        tooltip.add(Component.translatable("item.awakening_system.vigor_potion.desc"));
        tooltip.add(Component.translatable("item.awakening_system.vigor_potion.effect"));
    }`
);

// OliveElixirItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/OliveElixirItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.olive_elixir.title"));
        tooltip.add(Component.translatable("item.awakening_system.olive_elixir.desc"));
        tooltip.add(Component.translatable("item.awakening_system.olive_elixir.effect"));
    }`
);

// ConstellationNectarItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/ConstellationNectarItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.title"));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.desc"));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.effect1"));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.effect2"));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.effect3"));
    }`
);

// GalacticBigLottoItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/GalacticBigLottoItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.galactic_big_lotto.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.galactic_big_lotto.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.galactic_big_lotto.use").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.galactic_big_lotto.warn").withStyle(ChatFormatting.RED));
    }`
);

// Uma Musume items
const umaMap = [
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/AlarmClockItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.mech_header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.mech_desc1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.mech_desc2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/AnkleWeightsItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.title").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.desc").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.effect1").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.effect2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/BitterJuiceItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.title").withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.effect1").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.effect2").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.effect3").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.penalty_header").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.penalty_desc").withStyle(ChatFormatting.GRAY));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/CloverSparkItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.title").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.desc1").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.desc2").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.hint").withStyle(ChatFormatting.DARK_GREEN));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/DarkStaminaElixirItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.title").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.effect1").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.effect2").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.effect3").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.penalty_header").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.penalty_desc").withStyle(ChatFormatting.GRAY));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/EliteCaratItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.effect1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.effect2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/GachaTicketItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.gacha_ticket.title").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.gacha_ticket.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.gacha_ticket.use1").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.gacha_ticket.use2").withStyle(ChatFormatting.YELLOW));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/GoldenGachaTicketItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.golden_ticket.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.golden_ticket.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.golden_ticket.use1").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.golden_ticket.use2").withStyle(ChatFormatting.YELLOW));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/GoldHorseshoeItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.effect1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.effect2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/SilverHorseshoeItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.title").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.effect1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/PleasingParfaitItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect1").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect2").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect3").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect4").withStyle(ChatFormatting.LIGHT_PURPLE));
    }`
  },
  {
    file: 'src/main/java/com/soybe/awakeningsystem/item/uma/TrainingWhistleItem.java',
    replace: `void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.title").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.desc1").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.desc2").withStyle(ChatFormatting.GREEN));
    }`
  }
];

for (const u of umaMap) {
  patchFile(u.file, /void appendHoverText[\s\S]*?\n    \}/, u.replace);
}

// PrimogemItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/PrimogemItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.primogem.desc1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.primogem.desc2").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.primogem.use1").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.primogem.use2").withStyle(ChatFormatting.GOLD));
    }`
);

// GenesisCrystalItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/GenesisCrystalItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.genesis_crystal.badge").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.genesis_crystal.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.genesis_crystal.use1").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.genesis_crystal.use2").withStyle(ChatFormatting.DARK_AQUA));
    }`
);

// IntertwinedFateItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/IntertwinedFateItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.intertwined_fate.desc1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.intertwined_fate.desc2").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.intertwined_fate.use1").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.intertwined_fate.use2").withStyle(ChatFormatting.GOLD));
    }`
);

// AcquaintFateItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/AcquaintFateItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.acquaint_fate.desc1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.acquaint_fate.desc2").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.acquaint_fate.use1").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.acquaint_fate.use2").withStyle(ChatFormatting.GOLD));
    }`
);

// StellarJadeItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/StellarJadeItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.stellar_jade.badge").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.stellar_jade.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.stellar_jade.use1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.stellar_jade.use2").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.stellar_jade.hint").withStyle(ChatFormatting.GOLD));
    }`
);

// StarRailSpecialPassItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/StarRailSpecialPassItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.star_rail_special_pass.badge").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.star_rail_special_pass.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.star_rail_special_pass.rate1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.star_rail_special_pass.rate2").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.star_rail_special_pass.use1").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.star_rail_special_pass.use2").withStyle(ChatFormatting.YELLOW));
    }`
);

// CosmicFragmentItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/CosmicFragmentItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.badge").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.synergy_title").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.synergy_desc"));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.repair_title").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.repair_desc"));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.synth_title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.synth_desc"));
    }`
);

// CondensedResinItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/CondensedResinItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.blessing_title").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.blessing_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.blessing_desc2"));
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.recipe"));
    }`
);

// OriginalResinItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/OriginalResinItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.original_resin.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.original_resin.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.original_resin.use").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.original_resin.craft").withStyle(ChatFormatting.AQUA));
    }`
);

// TransientResinItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/TransientResinItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.transient_resin.badge").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.transient_resin.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.transient_resin.use").withStyle(ChatFormatting.YELLOW));
    }`
);

// CrownOfInsightItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/CrownOfInsightItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.crown_of_insight.badge").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.crown_of_insight.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.crown_of_insight.title").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.crown_of_insight.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.crown_of_insight.desc2"));
    }`
);

// HerosWitItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/HerosWitItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.heros_wit.badge").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.awakening_system.heros_wit.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.heros_wit.use1").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.heros_wit.use2").withStyle(ChatFormatting.DARK_GREEN));
    }`
);

// OculusItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/OculusItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.oculus.region", element.format + element.region));
        tooltip.add(Component.translatable("item.awakening_system.oculus.substance"));
        tooltip.add(Component.translatable("item.awakening_system.oculus.lore"));
    }`
);

// OculusResonanceStoneItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/OculusResonanceStoneItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.oculus_resonance_stone.element", element.format + element.title));
        tooltip.add(Component.translatable("item.awakening_system.oculus_resonance_stone.region", element.format + element.region));
        tooltip.add(Component.translatable("item.awakening_system.oculus_resonance_stone.use", element.title));
    }`
);

// TeyvatSigilItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/TeyvatSigilItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.teyvat_sigil.region", type.format + type.region));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_sigil.use", String.valueOf(type.moraValue)));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_sigil.lore"));
    }`
);

// TeyvatShrineKeyItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/TeyvatShrineKeyItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.teyvat_shrine_key.nation", region.format + region.region));
        tooltip.add(Component.literal("§e" + region.title));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_shrine_key.lore"));
    }`
);

// ClandestineVaultKeyItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/ClandestineVaultKeyItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.title"));
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.desc"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.use_title"));
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.use_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.use_desc2"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.clandestine_vault_key.hint").withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// ForgeBilletItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/ForgeBilletItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.forge_billet.series", series.format + series.title));
        tooltip.add(Component.translatable("item.awakening_system.forge_billet.category", category.name));
        tooltip.add(Component.literal("§8" + category.desc));
        tooltip.add(Component.translatable("item.awakening_system.forge_billet.material"));
    }`
);

// GachaCapsuleItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/GachaCapsuleItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (capsuleRarity == CapsuleRarity.GOLD) {
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.rarity_gold").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.desc_gold").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.guarantee_gold").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.prizes_gold").withStyle(ChatFormatting.GOLD));
        } else if (capsuleRarity == CapsuleRarity.PURPLE) {
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.rarity_purple").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.desc_purple").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.rewards_purple").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.prizes_purple").withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.rarity_blue").withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.desc_blue").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.rewards_blue").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.prizes_blue").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.awakening_system.gacha_capsule.open").withStyle(ChatFormatting.YELLOW));
    }`
);

// CasinoChipItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/CasinoChipItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (tier == ChipTier.BLACK) {
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.clandestine_title"));
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.clandestine_desc"));
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.black_market_val", String.format("%,d", tier.getMoraValue())).withStyle(tier.getColorCode().isEmpty() ? ChatFormatting.WHITE : ChatFormatting.RESET));
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.clandestine_warn"));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.official_desc"));
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.official_val", String.format("%,d", tier.getMoraValue())).withStyle(tier.getColorCode().isEmpty() ? ChatFormatting.WHITE : ChatFormatting.RESET));
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.official_hint"));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// CasinoTrophyItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/CasinoTrophyItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        int type = 1;
        String winner = "Campeón Legendario";
        String desc = "Hazaña de Campeonato";
        String date = "";

        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("trophy_type")) type = tag.getInt("trophy_type");
            if (tag.contains("winner_name") && !tag.getString("winner_name").isEmpty()) winner = tag.getString("winner_name");
            if (tag.contains("achievement_desc") && !tag.getString("achievement_desc").isEmpty()) desc = tag.getString("achievement_desc");
            if (tag.contains("awarded_date")) date = tag.getString("awarded_date");
        }

        Component category = switch (type) {
            case 1 -> Component.translatable("item.awakening_system.casino_trophy.cat_derby");
            case 2 -> Component.translatable("item.awakening_system.casino_trophy.cat_blackjack");
            case 3 -> Component.translatable("item.awakening_system.casino_trophy.cat_jackpot");
            case 4 -> Component.translatable("item.awakening_system.casino_trophy.cat_billiards");
            case 5 -> Component.translatable("item.awakening_system.casino_trophy.cat_darts");
            default -> Component.translatable("item.awakening_system.casino_trophy.cat_default");
        };

        tooltip.add(Component.literal("§6✦ ").append(category).append(" ✦").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.casino_trophy.awarded_to", winner).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.casino_trophy.feat_line", desc).withStyle(ChatFormatting.AQUA));
        if (!date.isEmpty()) {
            tooltip.add(Component.translatable("item.awakening_system.casino_trophy.date_line", date).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("item.awakening_system.casino_trophy.hint").withStyle(ChatFormatting.GRAY));
    }`
);

// CasinoVipPassItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/CasinoVipPassItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.desc2"));

        if (isBound(stack)) {
            String name = getBoundName(stack);
            tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.owner_bound", (name.isEmpty() ? "Acreditado" : name)).withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.status_bound").withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.owner_unbound").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.bind_hint").withStyle(ChatFormatting.AQUA));
        }

        tooltip.add(Component.translatable("item.awakening_system.casino_vip_pass.footer"));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// CaratItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/CaratItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.carat.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.carat.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.carat.rates").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.carat.deposit1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.carat.deposit_all").withStyle(ChatFormatting.AQUA));
    }`
);

// 5★ Light Cones
// LightConeAlongThePassingShoreItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LightConeAlongThePassingShoreItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.path").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.lore").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.stats_title").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.stat1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.stat2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.equip_hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
);
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LightConeAlongThePassingShoreItem.java',
  /player\.displayClientMessage\(Component\.literal\("§c✦ \[CONO DE LUZ\][^\"]+"\), true\);/,
  `player.displayClientMessage(Component.translatable("item.awakening_system.light_cone.passing_shore.use_msg"), true);`
);

// LightConeBeforeDawnItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LightConeBeforeDawnItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.before_dawn.path").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.before_dawn.lore").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.before_dawn.stats_title").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.before_dawn.stat1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.before_dawn.stat2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.equip_hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
);
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LightConeBeforeDawnItem.java',
  /player\.displayClientMessage\(Component\.literal\("§c✦ \[CONO DE LUZ\][^\"]+"\), true\);/,
  `player.displayClientMessage(Component.translatable("item.awakening_system.light_cone.passing_shore.use_msg"), true);`
);

// LightConeNightOnTheMilkyWayItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LightConeNightOnTheMilkyWayItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.milky_way.path").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.milky_way.lore").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.milky_way.stats_title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.milky_way.stat1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.milky_way.stat2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.equip_hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
);
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LightConeNightOnTheMilkyWayItem.java',
  /player\.displayClientMessage\(Component\.literal\("§c✦ \[CONO DE LUZ\][^\"]+"\), true\);/,
  `player.displayClientMessage(Component.translatable("item.awakening_system.light_cone.passing_shore.use_msg"), true);`
);

// LightConePatienceIsAllYouNeedItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LightConePatienceIsAllYouNeedItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.patience.path").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.patience.lore").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.patience.stats_title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.patience.stat1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.patience.stat2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.equip_hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
);
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LightConePatienceIsAllYouNeedItem.java',
  /player\.displayClientMessage\(Component\.literal\("§c✦ \[CONO DE LUZ\][^\"]+"\), true\);/,
  `player.displayClientMessage(Component.translatable("item.awakening_system.light_cone.passing_shore.use_msg"), true);`
);

// LightConeWhereDreamsShouldRestItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LightConeWhereDreamsShouldRestItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.dreams_rest.path").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.dreams_rest.lore").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.dreams_rest.stats_title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.dreams_rest.stat1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.dreams_rest.stat2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.equip_hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
);
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LightConeWhereDreamsShouldRestItem.java',
  /player\.displayClientMessage\(Component\.literal\("§c✦ \[CONO DE LUZ\][^\"]+"\), true\);/,
  `player.displayClientMessage(Component.translatable("item.awakening_system.light_cone.passing_shore.use_msg"), true);`
);

// HsrLightConeItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/HsrLightConeItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String starColor = (stars == 4) ? "§d★★★★" : "§b★★★";
        Component rarityTag = Component.translatable(stars == 4 ? "item.awakening_system.hsr_light_cone.rarity_4" : "item.awakening_system.hsr_light_cone.rarity_3");
        Component path = Component.translatable("item.awakening_system.hsr_path." + pathName.toLowerCase());
        Component pathLabel = Component.translatable("item.awakening_system.hsr_light_cone.path_label");
        tooltip.add(Component.translatable("item.awakening_system.hsr_light_cone.header", starColor, rarityTag, pathLabel, path).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatableWithFallback("item.awakening_system.light_cone." + coneId + ".lore", "§7\\"" + lore + "\\"").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.hsr_light_cone.skill_header", Component.translatableWithFallback("item.awakening_system.light_cone." + coneId + ".skill_name", skillName)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("  ").append(Component.translatableWithFallback("item.awakening_system.light_cone." + coneId + ".skill_desc", "§7" + skillDesc)).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.hsr_light_cone.hint").withStyle(ChatFormatting.DARK_GRAY));
    }`
);
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/HsrLightConeItem.java',
  /sp\.displayClientMessage\(Component\.literal\("§d✦ Cono de Luz en enfriamiento: §f" \+ sec \+ "s"\), true\);/,
  `sp.displayClientMessage(Component.translatable("item.awakening_system.hsr_light_cone.cd_msg", sec), true);`
);
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/HsrLightConeItem.java',
  /sp\.displayClientMessage\(Component\.literal\("§d§l✦ \[CONO DE LUZ\] §f¡" \+ coneDisplayName \+ " activado! §7\(Vía de la " \+ pathName \+ "\)"\), true\);/,
  `sp.displayClientMessage(Component.translatable("item.awakening_system.hsr_light_cone.activate_msg", coneDisplayName, pathName), true);`
);

// LunarAuthorityItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/LunarAuthorityItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(goddess.getColorCode()).append(Component.translatable("item.awakening_system.lunar_authority.title", goddess.getGoddessName(), goddess.getPhase())).withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable(goddess.getDescKey()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.lunar_authority.relic").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.lunar_authority.altar").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// TrilunarGuideItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/TrilunarGuideItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.trilunar_guide.title").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.trilunar_guide.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.trilunar_guide.goal").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.trilunar_guide.mission").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.trilunar_guide.target").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.trilunar_guide.use").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// TrackMarkerWandItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/TrackMarkerWandItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<BlockPos> pts = getWaypoints(stack);
        int count = pts.size();
        double dist = calculateTotalDistance(pts);
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.stats", count, String.format("%.0f", dist)).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.add_point").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.undo").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.clear").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.track_marker_wand.link").withStyle(ChatFormatting.AQUA));
    }`
);

// PrimogemPickaxeItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/PrimogemPickaxeItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.primogem_pickaxe.title").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.primogem_pickaxe.desc"));
        tooltip.add(Component.translatable("item.awakening_system.primogem_pickaxe.eff1"));
        tooltip.add(Component.translatable("item.awakening_system.primogem_pickaxe.eff2"));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// CompanionContractItem.java
patchFile(
  'src/main/java/com/soybe/awakeningsystem/item/CompanionContractItem.java',
  /void appendHoverText[\s\S]*?\n    \}/,
`void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("UmaId")) {
            CompoundTag tag = cd.copyTag();
            com.soybe.awakeningsystem.uma.UmaProfile up = com.soybe.awakeningsystem.uma.UmaProfile.byId(tag.getString("UmaId"));
            int r = tag.getInt("UmaRarity");
            tooltip.add(Component.translatable("item.awakening_system.companion_contract.rarity", "★".repeat(r), r));
            tooltip.add(Component.translatable("item.awakening_system.companion_contract.tactique", up.getTactique()));
            tooltip.add(Component.translatable("item.awakening_system.companion_contract.track_info", up.getDistance(), up.getSurface()));
            tooltip.add(Component.translatable("item.awakening_system.companion_contract.skill", up.getSkillName()));
            tooltip.add(Component.empty());
            if (tag.getBoolean("UmaNew")) {
                tooltip.add(Component.translatable("item.awakening_system.companion_contract.unlocked"));
            } else {
                tooltip.add(Component.translatable("item.awakening_system.companion_contract.shards", tag.getInt("UmaShards")));
            }
            tooltip.add(Component.translatable("item.awakening_system.companion_contract.race_hint"));
            return;
        }

        CompanionProfile p = getProfile(stack);
        String stars = "★".repeat(p.stars());
        int lvl = getLevel(stack);
        int c = getConstellation(stack);
        int rank = getAscensionRank(stack);
        tooltip.add(Component.literal("§e").append(Component.translatable("item.awakening_system.companion_contract.rarity", stars, p.stars())).append(Component.translatable(p.stars() == 5 ? "item.awakening_system.companion_contract.companion_rarity_5" : "item.awakening_system.companion_contract.companion_rarity_4")));
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.companion_level", lvl, rank));
        if (c > 0) {
            tooltip.add(Component.translatable(c <= 6 ? "item.awakening_system.companion_contract.constellation_awakened" : "item.awakening_system.companion_contract.constellation_transcendent", c));
        }
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.element_path", p.colorCode(), p.element()));
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.title", p.title()));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.combat_skill_title"));
        tooltip.add(Component.literal("§f" + p.skillDesc()));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.summon_hint"));
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.store_hint"));
        tooltip.add(Component.translatable("item.awakening_system.companion_contract.upgrade_hint"));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

console.log('All patches complete!');
