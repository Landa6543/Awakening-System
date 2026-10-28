const fs = require('fs');

function replaceInFile(filePath, search, replacement) {
    if (!fs.existsSync(filePath)) {
        console.error('File not found: ' + filePath);
        return false;
    }
    let content = fs.readFileSync(filePath, 'utf8');
    const isCrlf = content.includes('\r\n');
    const normContent = content.replace(/\r\n/g, '\n');
    const normSearch = search.replace(/\r\n/g, '\n');
    const normReplacement = replacement.replace(/\r\n/g, '\n');
    if (!normContent.includes(normSearch)) {
        console.warn('Search block not found in ' + filePath);
        return false;
    }
    let newContent = normContent.replace(normSearch, normReplacement);
    if (isCrlf) {
        newContent = newContent.replace(/\n/g, '\r\n');
    }
    fs.writeFileSync(filePath, newContent, 'utf8');
    console.log('Successfully patched: ' + filePath);
    return true;
}

// 1. DoctorsRobeItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/DoctorsRobeItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§5§l['Curio' Manto del Doctor]").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("§7Vestidura de erudito del cosmos proveniente del Universo Simulado."));
        tooltip.add(Component.literal("§a✦ Canalización del Sabio:"));
        tooltip.add(Component.literal("§7  Al recibir daño severo (vida por debajo del 50%):"));
        tooltip.add(Component.literal("§2  • Regenera instantáneamente el 100% de tu Maná y Qi."));
        tooltip.add(Component.literal("§e  • Otorga 6 corazones dorados de Absorción y Regeneración."));
        tooltip.add(Component.literal("§8  (Tiempo de recarga: 60 segundos)"));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.desc"));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.skill_title"));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.cond"));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.effect1"));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.effect2"));
        tooltip.add(Component.translatable("item.awakening_system.doctors_robe.cd"));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// 2. RobeOfTheBeautyItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/RobeOfTheBeautyItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§d⭐⭐⭐⭐⭐ [Curio del Universo Simulado]").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("§7Un majestuoso manto celestial bendecido por Idrila, la Belleza Cósmica.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§6✦ Efecto Pasivo: El Manto de la Belleza").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("  §7Por cada §e100 Fragmentos Cósmicos §7en tu inventario,"));
        tooltip.add(Component.literal("  §7tu daño infligido aumenta en §a+16% §7(máx. §a+80%§7)."));
        tooltip.add(Component.literal("§d✦ Preservación Sagrada:").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("  §7Al recibir daño crítico, consume §e30 Fragmentos Cósmicos"));
        tooltip.add(Component.literal("  §7para mitigar el 50% del daño y ganar invulnerabilidad breve."));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.badge").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.passive_title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.passive_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.passive_desc2"));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_desc2"));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// 3. WildfireCrownItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/WildfireCrownItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6✦ Corona del Jefe Wildfire").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7\"Canaliza la esencia ardiente del coloso de fuego del Nether.\"").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e✦ Bendición Ígnea Primordial:").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §7Otorga resistencia absoluta y permanente al fuego y a la lava mientras se lleve equipada.").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.wildfire_crown.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.wildfire_crown.lore").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.wildfire_crown.blessing_title").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.wildfire_crown.blessing_desc").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// 4. TeyvatDelusionItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/TeyvatDelusionItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (element != null) {
            tooltip.add(Component.literal("§7Elemento Fatui: " + element.format + element.name));
            tooltip.add(Component.literal("§c[Click Derecho] §7Desata una explosión elemental destructiva."));
            tooltip.add(Component.literal("§4⚠ Advertencia: §7El poder inestable de los Fatui te causará corrosión y daño mágico al activarse."));
        } else {
            tooltip.add(Component.literal("§8Engaño apagado. Combínalo con Fragmentos de Engaño para forjar un artefacto elemental."));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (element != null) {
            tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.element").append(Component.literal(element.format + element.name)));
            tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.active"));
            tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.warn"));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.teyvat_delusion.blank"));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// 5. TeyvatVisionItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/TeyvatVisionItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6⭐⭐⭐⭐⭐ [Visión Elemental de Teyvat - 5★]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7Canalizador otorgado por los Arcontes Celestiales de Genshin Impact.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e▸ Elemento: " + element.format + element.name).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("§b▸ Efecto de Resonancia: §fCanaliza ataques y reacciones del elemento al equiparse en el Sistema.").withStyle(ChatFormatting.AQUA));
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.element").append(Component.literal(element.format + element.name)).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_vision.resonance").withStyle(ChatFormatting.AQUA));
    }`
);

// 6. VigorPotionItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/VigorPotionItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6✦ Poción de Vigor Arcano ✦").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7Brebaje alquímico destilado de esencias puras.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e[Clic Derecho] §fRestaura +100 Energía y otorga Regeneración, Velocidad y Fuerza.").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.vigor_potion.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.vigor_potion.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.vigor_potion.effect").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// 7. OliveElixirItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/OliveElixirItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§b✦ Elixir de Olivo Místico ✦").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("§7Un extracto milagroso bendecido por la rama del árbol sagrado.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e[Clic Derecho] §fRestablece todos los atributos y devuelve los puntos invertidos.").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.olive_elixir.title").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.olive_elixir.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.olive_elixir.effect").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// 8. ConstellationNectarItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/ConstellationNectarItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§d✦ Néctar de las Constelaciones ✦").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("§7Elixir destilado de los arroyos estelares del Gran Dao.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e[Mantener Clic Derecho] §fPurifica los canales energéticos y"));
        tooltip.add(Component.literal("§a§lRestablece tu Constitución / Físico Murim §fpara elegir uno nuevo."));
        tooltip.add(Component.literal("§bOtorga Regeneración y Absorción Celestial."));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.effect1"));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.effect2"));
        tooltip.add(Component.translatable("item.awakening_system.constellation_nectar.effect3"));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// 9. CommandSpellItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/CommandSpellItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int seals = getSeals(stack);
        tooltip.add(Component.literal("§6⭐⭐⭐⭐⭐ [Artefacto Místico 5★ - Fate/Grand Order]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7El blasón sagrado del Master que impone la voluntad absoluta.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§c▸ Sellos Restantes: §f" + seals + "/3").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("§e▸ Clic derecho a baja salud (<50% HP):").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("  §7Restaura 100% HP, Maná y otorga Absorción V con purificación.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§b▸ Clic derecho con salud alta (≥50% HP):").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("  §7Restaura 100% Maná y reinicia al instante todos los enfriamientos de armas.").withStyle(ChatFormatting.DARK_GRAY));
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int seals = getSeals(stack);
        tooltip.add(Component.translatable("item.awakening_system.command_spell.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.seals", seals).withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.low_hp_title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.low_hp_desc").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.high_hp_title").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.command_spell.high_hp_desc").withStyle(ChatFormatting.DARK_GRAY));
    }`
);

// 10. VisionsMenuItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/VisionsMenuItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6⭐⭐⭐⭐⭐ [Compendio Elemental de Teyvat]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7Un tomo arcaico otorgado por las Siete Estatuas de Los Siete.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e▸ Clic derecho: §fAbre el menú para resonar con cualquiera de los 7 elementos primordiales.").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("§b▸ Como el Viajero, puedes cambiar tu elemento libremente.").withStyle(ChatFormatting.AQUA));
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.visions_menu.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.visions_menu.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.visions_menu.use").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.visions_menu.hint").withStyle(ChatFormatting.AQUA));
    }`
);

// 11. FableScrollItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/FableScrollItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§d✦ Pergamino de Fábula Central ✦"));
        tooltip.add(Component.literal("§7Un cósmico fragmento de historia forjado en los escenarios del Star Stream."));
        tooltip.add(Component.literal("§e[Clic Derecho] §fDespliega la fábula otorgando +5,000 EXP de Sistema y +10,000 Mora."));
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.fable_scroll.title"));
        tooltip.add(Component.translatable("item.awakening_system.fable_scroll.desc"));
        tooltip.add(Component.translatable("item.awakening_system.fable_scroll.effect"));
    }`
);

// 12. FableStarFragmentItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/FableStarFragmentItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6✦ Fragmento Estelar de Fábula ✦"));
        tooltip.add(Component.literal("§7Un cristal radiante desprendido de una constelación legendaria."));
        tooltip.add(Component.literal("§e[Clic Derecho] §fCanjea la resonancia estelar por §a+3,500 EXP §fy §6+8,000 Mora."));
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.fable_star_fragment.title"));
        tooltip.add(Component.translatable("item.awakening_system.fable_star_fragment.desc"));
        tooltip.add(Component.translatable("item.awakening_system.fable_star_fragment.effect"));
    }`
);

// 13. PlushieItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/PlushieItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("§d♥ Clic derecho para apachurrar"));
        tooltip.add(Component.literal("§b👒 Agacharse + Clic Der para equipar en la cabeza"));
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.awakening_system.plushie.squeeze"));
        tooltip.add(Component.translatable("item.awakening_system.plushie.wear"));
    }`
);

// 14. CustomPlayerPlushieItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/CustomPlayerPlushieItem.java',
`        if (profile != null && profile.name().isPresent()) {
            tooltip.add(Component.literal("§7Dueño: §e" + profile.name().get()));
        } else {
            tooltip.add(Component.literal("§7Adopta la skin de quien lo coloque o nómbralo en un yunque."));
        }
        tooltip.add(Component.literal("§d♥ Clic derecho para apachurrar"));
        tooltip.add(Component.literal("§b👒 Agacharse + Clic Der para equipar en la cabeza"));`,
`        if (profile != null && profile.name().isPresent()) {
            tooltip.add(Component.translatable("item.awakening_system.custom_plushie.owner", profile.name().get()));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.custom_plushie.hint"));
        }
        tooltip.add(Component.translatable("item.awakening_system.plushie.squeeze"));
        tooltip.add(Component.translatable("item.awakening_system.plushie.wear"));`
);

// 15. PraiseOfLoftyMoralityItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/PraiseOfLoftyMoralityItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§e⭐⭐⭐⭐ [Prueba de Virtud y Decoro]").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("§7Un brillante distintivo otorgado a quienes realizan actos de bondad desinteresada en Belobog.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§a▸ Clic derecho: §fConsumir para recibir la bendición de la Suerte y la Salud").withStyle(ChatFormatting.GREEN));
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.praise_of_lofty_morality.badge").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.praise_of_lofty_morality.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.praise_of_lofty_morality.use").withStyle(ChatFormatting.GREEN));
    }`
);

// 16. OmnisciencePouchItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/OmnisciencePouchItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§5§l['Curio' Bolsa Omnisciente]").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("§7Un mítico talismán proveniente de las profundidades del Universo Simulado."));
        tooltip.add(Component.literal("§e✦ Fortuna Astral:"));
        tooltip.add(Component.literal("§7  Al romper Vasijas de Teyvat o abrir Cofres del Tesoro,"));
        tooltip.add(Component.literal("§a  duplica automáticamente todas las recompensas y materiales obtenidos."));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.omniscience_pouch.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.omniscience_pouch.desc"));
        tooltip.add(Component.translatable("item.awakening_system.omniscience_pouch.header"));
        tooltip.add(Component.translatable("item.awakening_system.omniscience_pouch.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.omniscience_pouch.desc2"));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// 17-28. Uma Musume items
// AlarmClockItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/AlarmClockItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6⏰ El fiel despertador que levanta a las Umas para el entrenamiento matutino.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7Un talismán de rescate que no te dejará caer en el sueño eterno.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e✦ Mecánica de Supervivencia:").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §b• Al recibir daño letal se consume para anular la muerte").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("  §a• Restaura el 100% de Salud, da invulnerabilidad breve y limpia desventajas").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("§8Funciona desde el inventario o curio sin necesidad de tenerlo en mano.").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.mech_header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.mech_desc1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.mech_desc2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.hint").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// AnkleWeightsItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/AnkleWeightsItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§8⚙ Pesas de plomo forjado diseñadas para fortalecer las piernas de las corredoras.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7Un clásico del entrenamiento riguroso de la Academia Tracen.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§e✦ Mientras están equipadas en botas:").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §c• Lentitud I (Entrenamiento riguroso de resistencia)").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("  §a• +50% EXP adicional al correr, esprintar y combatir").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("§8Retíralas tras una sesión para sentir una aceleración explosiva.").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.title").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.desc").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.effect1").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.effect2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.hint").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// BitterJuiceItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/BitterJuiceItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§2🧪 Tónico amargo de verduras concentradas para potenciar el entrenamiento.").withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.literal("§7Fórmula extrema de los entrenadores de la Academia Tracen.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e✦ Efectos de Estímulo en Entrenamiento (2:00 min):").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §c• Fuerza II").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("  §e• Celeridad II (Reflejos y trabajo rápido)").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("  §b• Salto Mejorado I").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("§c⚠ Penalización al expirar (1:00 min):").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("  §7• Hambre II y Debilidad I por agotamiento").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.title").withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.effect1").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.effect2").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.effect3").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.penalty_header").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.penalty_desc").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// CloverSparkItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/CloverSparkItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§a🍀 Trébol de la Suerte de la Academia Tracen (Puntos de Spark).").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("§7Divisa especial de compensación otorgada en las tiradas del gacha.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e✦ Uso en Intercambio:").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §f• Acumúlalos para canjear de forma garantizada armas celestiales de Genshin").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("  §f• Permite comprar amuletos y objetos legendarios de Uma Musume").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("§8¡Garantiza que ninguna tirada sea en vano!").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.title").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.desc1").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.desc2").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.hint").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// DarkStaminaElixirItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/DarkStaminaElixirItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§5☠ Sustancia prohibida destilada en los callejones del bajo mundo.").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("§7Fórmula clandestina confiscada recurrentemente en el hipódromo.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e✦ Efectos de Rendimiento Extremo (3:00 min):").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §d• Velocidad III (+60% Velocidad de carrera)").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("  §b• Salto Mejorado II").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("  §c• Fuerza I").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("§c⚠ Rebote de Resaca Clandestina (0:45 min al expirar):").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("  §7• Agotamiento extremo: Lentitud I, Debilidad II y Hambre II").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.title").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.effect1").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.effect2").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.effect3").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.penalty_header").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.penalty_desc").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// EliteCaratItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/EliteCaratItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6🥕 Zanahoria de Grado Especial cosechada en los huertos de Tracen.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7Concentrado de nutrientes de alta competición para corredoras.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e✦ Efectos al consumir:").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §b• Velocidad III §7(4:00 min)").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("  §a• Salto Elevado II §7(4:00 min)").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("§8Ideal para exploración rápida de terreno y escalada en cavernas.").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.effect1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.effect2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.hint").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// GachaTicketItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/GachaTicketItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§a🎟 Boleto de invitación oficial para el Gacha de la Academia Tracen.").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("§7Equivale al valor exacto de una invocación individual.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e▸ Clic derecho: §fCanjear 1 boleto (§e+150 🥕§f)").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("§e▸ Shift + Clic derecho: §fCanjear todo el stack").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.gacha_ticket.title").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.gacha_ticket.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.gacha_ticket.use1").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.gacha_ticket.use2").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// GoldenGachaTicketItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/GoldenGachaTicketItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6✨ Pase dorado de gala para 10 Invocaciones simultáneas en Tracen.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7Garantiza una sesión de reclutamiento completa con opción a Umas de alta rareza.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e▸ Clic derecho: §fCanjear 1 boleto de oro (§6+1500 🥕 §7- 10 Tiradas§f)").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("§e▸ Shift + Clic derecho: §fCanjear todo el stack").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.golden_ticket.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.golden_ticket.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.golden_ticket.use1").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.golden_ticket.use2").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// GoldHorseshoeItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/GoldHorseshoeItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6✨ Herradura dorada consagrada a las leyendas del Salón de la Fama.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§e✦ Efecto Pasivo (con llevarla en el inventario o pies):").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §b• +25% Velocidad de Movimiento base").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("  §a• Inmunidad Total al daño por caída §7(Caída de Pluma infinita)").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("§8Permite descender de grandes alturas sin recibir daño alguno.").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.effect1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.effect2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.hint").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// SilverHorseshoeItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/SilverHorseshoeItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7✦ Herradura forjada con plata pura bendecida por la Diosa de la Victoria.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e✦ Efecto Pasivo (con llevarla en el inventario o pies):").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §b• +15% Velocidad de Movimiento base").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("§8Armoniza la zancada permitiendo un desplazamiento veloz y fluido.").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.title").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.effect1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.hint").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// PleasingParfaitItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/PleasingParfaitItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§d🍨 El postre más codiciado por las Umas tras ganar una carrera G1.").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("§7Un deleite culinario legendario que renueva cuerpo y alma.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e✦ Efectos al consumir:").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §a• Purga inmediata de todos los estados negativos").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("  §c• Restaura el 100% de Salud Máxima").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("  §6• Restaura al máximo Hambre y Saturación").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("  §d• Regeneración II §7(0:15 min)").withStyle(ChatFormatting.LIGHT_PURPLE));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect1").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect2").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect3").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect4").withStyle(ChatFormatting.LIGHT_PURPLE));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

// TrainingWhistleItem.java
replaceInFile(
    'src/main/java/com/soybe/awakeningsystem/item/uma/TrainingWhistleItem.java',
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§b📣 Silbato metálico de alta frecuencia utilizado por los entrenadores.").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("§7El sonido despierta la mente de los aldeanos y reinicia su vocación.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e✦ Clic Derecho:").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  §f• Reinicia profesiones, XP y tradeos de aldeanos en un radio de §b10 bloques").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("  §a• Elimina la necesidad de romper y reemplazar mesas de trabajo").withStyle(ChatFormatting.GREEN));
        super.appendHoverText(stack, context, tooltip, flag);
    }`,
`    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.title").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.desc1").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.uma.training_whistle.desc2").withStyle(ChatFormatting.GREEN));
        super.appendHoverText(stack, context, tooltip, flag);
    }`
);

console.log('Finished patching Phase 1 items!');
