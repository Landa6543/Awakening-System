import os

print("Starting Java code patching for screen translations...")

def replace_in_file(fpath, old_text, new_text):
    if not os.path.exists(fpath):
        print(f"Error: {fpath} does not exist!")
        return False
    with open(fpath, "r", encoding="utf-8") as f:
        content = f.read()
    if old_text not in content:
        print(f"Warning: could not find target in {fpath}: {old_text[:60]}...")
        return False
    content = content.replace(old_text, new_text, 1)
    with open(fpath, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Patched {fpath}")
    return True

# 1. DungeonManager.java
p = "src/main/java/com/soybe/awakeningsystem/DungeonManager.java"
replace_in_file(p, 
    'AwakeningSystemMod.notify(player, "§a§l¡FASE 1 COMPLETADA!", "§eLa Muralla Mágica se ha disuelto. ¡Avanza a la Fase 2!", 0xFF50FA7B);',
    'AwakeningSystemMod.notify(player, "awakening_system.action.phase_completed_title", "awakening_system.action.phase_1_unlocked", 0xFF50FA7B, 1);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§cYa estás dentro de un calabozo activo."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.already_in_dungeon"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6§l✦ ¡PREMIO: §e" + session.finalWinningStack.getHoverName().getString() + " x" + session.finalWinningStack.getCount() + " §6(+§e" + session.finalCoinsWon + "⛃§6, +§b" + session.finalExpWon + "XP§6)! ✦"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.roulette_prize", session.finalWinningStack.getHoverName().getString(), session.finalWinningStack.getCount(), session.finalCoinsWon, session.finalExpWon), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c§l[BARRERA MÁGICA ACTIVA] §eElimina a los enemigos de la Fase 1 (§c" + rem + " restantes§e) para disolver la muralla."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_1", rem), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c§l[BARRERA MÁGICA ACTIVA] §e¡No puedes avanzar hasta eliminar a los enemigos de la Fase 1!"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_1"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c§l[BARRERA MÁGICA ACTIVA] §eElimina a los guardias de la Fase 2 (§c" + rem + " restantes§e) para abrir el Foso de Guerra."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_2", rem), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c§l[BARRERA MÁGICA ACTIVA] §e¡No puedes avanzar hasta eliminar a los guardias de la Fase 2!"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_2"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c§l[BARRERA MÁGICA ACTIVA] §eElimina a los guerreros de la Fase 3 (§c" + rem + " restantes§e) para abrir la Catedral."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_3", rem), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c§l[BARRERA MÁGICA ACTIVA] §e¡No puedes avanzar hasta eliminar a los guerreros de la Fase 3!"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_3"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c§l[BARRERA MÁGICA ACTIVA] §eElimina a los guardias de la Fase 4 (§c" + rem + " restantes§e) para abrir el Salón del Trono."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_warning_4", rem), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§c§l[BARRERA MÁGICA ACTIVA] §e¡No puedes avanzar al Trono hasta eliminar a los guardias de la Fase 4!"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.barrier_blocked_4"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("¡Cruzando el vórtice dimensional! ").append(getGateTitle(rift.rank, rift.isRedGate)), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.entering_rift", getGateTitle(rift.rank, rift.isRedGate)), true);'
)
replace_in_file(p,
    'player.displayClientMessage(getGateTitle(rift.rank, rift.isRedGate).copy().append(Component.literal(" §f- §eCruza el portal dimensional para entrar")), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.rift_nearby_prompt", getGateTitle(rift.rank, rift.isRedGate)), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§e⚠ Has salido del calabozo sin conquistarlo. El portal permanece activo en el exterior hasta que expire."), false);',
    'player.displayClientMessage(Component.translatable("awakening_system.dungeon.left_unconquered"), false);'
)
replace_in_file(p,
    'sp.displayClientMessage(Component.literal("§4§l⚠ ¡DESBORDE DE MAZMORRA INMINENTE! §cEl portal ha colapsado y los monstruos están invadiendo el exterior."), false);',
    'sp.displayClientMessage(Component.translatable("awakening_system.dungeon.dungeon_break_imminent"), false);'
)

# 2. SystemCommands.java
p = "src/main/java/com/soybe/awakeningsystem/SystemCommands.java"
replace_in_file(p,
    'target.displayClientMessage(Component.literal("§c§l[SISTEMA]§r Tu progreso ha sido completamente restablecido."), true);',
    'target.displayClientMessage(Component.translatable("awakening_system.command.reset_success"), true);'
)
replace_in_file(p,
    'finalP.displayClientMessage(Component.literal("§c[STAR STREAM] Has dejado de ser una Constelación. Has regresado al estatus de encarnación."), true);',
    'finalP.displayClientMessage(Component.translatable("awakening_system.command.constellation_reverted"), true);'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(player, "§c⚡ ESCENARIO PRINCIPAL FORZADO ⚡",\n                "§eEl Dokkaebi ha abierto una transmisión: ¡Elimina " + target + " enemigos en 3 minutos!", 0xFFFF4444);',
    'AwakeningSystemMod.notify(player, "awakening_system.notification.dokkaebi_scenario_start.title", "awakening_system.notification.dokkaebi_scenario_start.desc", 0xFFFF4444, target);'
)

# 3. AwakeningSystemMod.java
p = "src/main/java/com/soybe/awakeningsystem/AwakeningSystemMod.java"
replace_in_file(p,
    'player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c§l[RULETA] §7Saldo insuficiente (1.000 Monedas requeridas)."), true);',
    'player.displayClientMessage(net.minecraft.network.chat.Component.translatable("awakening_system.roulette.insufficient_coins"), true);'
)

# 4. IsekaiWorldManager.java
p = "src/main/java/com/soybe/awakeningsystem/IsekaiWorldManager.java"
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§d[Mundo Isekai] §fBienvenido de vuelta al Gran Valle de Yggdrasil."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dimension.isekai_welcome"), true);'
)

# 5. MurimWorldManager.java
p = "src/main/java/com/soybe/awakeningsystem/MurimWorldManager.java"
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6[Reino Murim] §fBienvenido de vuelta a la Gran Secta Inmortal."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dimension.murim_welcome"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§6§l[JUICIO MARCIAL] §fAbre el Pabellón de Cultivo §e[H] §fpara despertar tu Constitución Celestial innata."), false);',
    'player.displayClientMessage(Component.translatable("awakening_system.dimension.murim_martial_trial"), false);'
)

# 6. SystemHudOverlay.java
p = "src/main/java/com/soybe/awakeningsystem/SystemHudOverlay.java"
replace_in_file(p,
    'graphics.drawString(mc.font, "§6Nv." + data.level() + " §f" + player.getScoreboardName(), headX, curY, 0xFFFFFFFF, false);',
    'String lvlPrefix = Component.translatable("awakening_system.hud.level_prefix").getString();\n        graphics.drawString(mc.font, "§6" + lvlPrefix + " " + data.level() + " §f" + player.getScoreboardName(), headX, curY, 0xFFFFFFFF, false);'
)
old_switch = """            String compactRealm = switch (data.dantianRealm()) {
                case 1 -> "Cond";
                case 2 -> "Cim";
                case 3 -> "Núc";
                case 4 -> "Alma";
                case 5 -> "Asc";
                case 6 -> "Trib";
                case 7 -> "Inm";
                default -> "Mor";
            };"""
new_switch = '            String compactRealm = Component.translatable("awakening_system.realm.compact." + data.dantianRealm()).getString();'
replace_in_file(p, old_switch, new_switch)

old_kills = 'progress = Component.literal("§e" + timeStr + " §8| §fBajas: §a" + data.dokkaebiScenarioKills() + "§7/§e" + data.dokkaebiScenarioTargetKills() + " §6§l[+" + extraKills + " EXTRA!]");'
new_kills = 'progress = Component.translatable("awakening_system.hud.dokkaebi_extra_kills", timeStr, data.dokkaebiScenarioKills(), data.dokkaebiScenarioTargetKills(), extraKills);'
replace_in_file(p, old_kills, new_kills)

# 7. Block ElementalAltarBlock.java
p = "src/main/java/com/soybe/awakeningsystem/block/ElementalAltarBlock.java"
replace_in_file(p,
    'sp.displayClientMessage(Component.literal("§5§l[CASTILLO CENTRAL] §f¡Los 4 Sellos Elementales han caído! La Gran Barrera de Éter se ha disipado y el Salón del Trono del Rey Demonio está abierto."), false);',
    'sp.displayClientMessage(Component.translatable("awakening_system.block.elemental_altar.all_seals_broken"), false);'
)

# 8. MurimAlchemyCauldronBlock.java
p = "src/main/java/com/soybe/awakeningsystem/block/MurimAlchemyCauldronBlock.java"
replace_in_file(p, 'player.displayClientMessage(Component.literal("§e[Se necesita 1x Raíz de Mandrágora en el inventario para completar la píldora]"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.cauldron.need_mandrake"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§e[Se necesita 1x Ginseng Milenario en el inventario para completar la píldora]"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.cauldron.need_ginseng"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§e[Se necesita 1x Jade Espiritual o Fragmento de Jade en el inventario]"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.cauldron.need_jade"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§e[Se necesita 1x Lingote de Adamantita en el inventario para forjar el elixir]"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.cauldron.need_adamantite"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§c[El caldero requiere fuego espiritual bajo su base]"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.cauldron.need_fire"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§e[Faltan ingredientes: Ginseng Salvaje o Raíz de Mandrágora]"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.cauldron.missing_ingredients_1"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§e[Faltan ingredientes: Loto de Nieve o Ginseng Milenario]"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.cauldron.missing_ingredients_2"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§e[Faltan ingredientes: Ginseng Milenario o Jade Espiritual]"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.cauldron.missing_ingredients_3"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§e[Faltan ingredientes: Raíz de Mandrágora o Lingote de Adamantita]"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.cauldron.missing_ingredients_4"), true);')

# 9. AwakeningCoinEntity.java
p = "src/main/java/com/soybe/awakeningsystem/entity/AwakeningCoinEntity.java"
replace_in_file(p,
    'thrower.displayClientMessage(Component.literal("§6§l[CARA - SOL DORADO] §e¡La fortuna te favorece! (+Suerte II y Rapidez)"), true);',
    'thrower.displayClientMessage(Component.translatable("awakening_system.coin_entity.heads"), true);'
)
replace_in_file(p,
    'thrower.displayClientMessage(Component.literal("§b§l[CRUZ - LUNA SOMBRÍA] §f¡El destino te protege! (+Escudo de Absorción)"), true);',
    'thrower.displayClientMessage(Component.translatable("awakening_system.coin_entity.tails"), true);'
)

# 10. BloodQiTotemEntity.java
p = "src/main/java/com/soybe/awakeningsystem/entity/BloodQiTotemEntity.java"
replace_in_file(p,
    'sp.displayClientMessage(Component.literal("§c§l✦ ¡TODOS LOS ESTANDARTES HAN CAÍDO! ✦\\n§fLa barrera de Qi del Gran Patriarca ha colapsado."), true);',
    'sp.displayClientMessage(Component.translatable("awakening_system.entity.blood_qi_totem.all_banners_fallen"), true);'
)

# 11. CelestialDragonEntity.java
p = "src/main/java/com/soybe/awakeningsystem/entity/CelestialDragonEntity.java"
replace_in_file(p, 'player.displayClientMessage(Component.literal("§6§l✦ ¡EL DRAGÓN CELESTIAL RECONOCE TU ALMA SUPREMA! ✦"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.recognized"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§6§l✦ ¡LA PERLA DEL DRAGÓN CELESTIAL HA RESTAURADO TOTALMENTE AL DRAGÓN! ✦"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.pearl_restored"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§6§l✦ EL DRAGÓN CELESTIAL ENTRA EN REPOSO SERPENTINO ✦"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.resting"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§6§l✦ EL DRAGÓN CELESTIAL SE PREPARA PARA ELEVARSE ✦"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.taking_flight"), true);')
replace_in_file(p, 'player.displayClientMessage(Component.literal("§6§l✦ ¡EL DRAGÓN CELESTIAL FUE DOMESTICADO (MODO CREATIVO)! ✦"), true);', 'player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.creative_tamed"), true);')

# 12. DokkaebiEntity.java
p = "src/main/java/com/soybe/awakeningsystem/entity/DokkaebiEntity.java"
old_d1 = """        AwakeningSystemMod.notify(player,
                "literal:\\u00A7e\\u00A7l✦ [SUB-ESCENARIO DEL STAR STREAM] ✦",
                "literal:\\u00A7b" + title + "\\n\\u00A7f" + desc + "\\n\\u00A76Recompensa: \\u00A7e" + rewardCoins + " Monedas",
                0xFFFFD700);"""
new_d1 = """        AwakeningSystemMod.notify(player,
                "awakening_system.dokkaebi.quest_started_title",
                "awakening_system.dokkaebi.quest_started_desc",
                0xFFFFD700,
                title, desc, rewardCoins);"""
replace_in_file(p, old_d1, new_d1)

old_d2 = """            AwakeningSystemMod.notify(player,
                    "literal:\\u00A7a\\u00A7l✦ ¡OBJETIVO COMPLETADO! ✦",
                    "literal:\\u00A7fHabla con Bihyung para reclamar tus monedas y recompensas.",
                    0xFF55FF55);"""
new_d2 = """            AwakeningSystemMod.notify(player,
                    "awakening_system.dokkaebi.quest_objective_done_title",
                    "awakening_system.dokkaebi.quest_objective_done_desc",
                    0xFF55FF55);"""
replace_in_file(p, old_d2, new_d2)

replace_in_file(p,
    'player.displayClientMessage(Component.literal("§8[§eStar Stream§8] §fProgreso de Sub-Escenario: §e" + current + "/" + target), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.dokkaebi.quest_progress", current, target), true);'
)

old_d3 = """        AwakeningSystemMod.notify(player,
                "literal:\\u00A7e\\u00A7l✦ ¡SUB-ESCENARIO COMPLETADO! ✦",
                "literal:\\u00A7fBihyung: '¡Brillante transmisión, encarnación!\\nLas Constelaciones han enviado sus monedas.'\\n\\u00A7e+" + coins + " Monedas acreditadas.",
                0xFFFFD700);"""
new_d3 = """        AwakeningSystemMod.notify(player,
                "awakening_system.dokkaebi.quest_completed_title",
                "awakening_system.dokkaebi.quest_completed_desc",
                0xFFFFD700,
                coins);"""
replace_in_file(p, old_d3, new_d3)

# 13. ElementalDemonKingBossEntity.java
p = "src/main/java/com/soybe/awakeningsystem/entity/ElementalDemonKingBossEntity.java"
replace_in_file(p,
    'sp.displayClientMessage(Component.literal("§6§l[REY DEMONIO DERROTADO] §f¡El Soberano Elemental ha caído! Has conquistado los Castillos Flotantes de Isekai."), false);',
    'sp.displayClientMessage(Component.translatable("awakening_system.entity.demon_king.defeated"), false);'
)

# 14. ElementalLordBossEntity.java
p = "src/main/java/com/soybe/awakeningsystem/entity/ElementalLordBossEntity.java"
replace_in_file(p,
    'sp.displayClientMessage(Component.literal("§5§l[CASTILLO CENTRAL] §f¡Los 4 Sellos Elementales han caído! La Gran Barrera de Éter se ha disipado y el Salón del Trono del Rey Demonio está abierto."), false);',
    'sp.displayClientMessage(Component.translatable("awakening_system.entity.elemental_lord.all_seals_broken"), false);'
)

# 15. MountHuaDiscipleEntity.java
p = "src/main/java/com/soybe/awakeningsystem/entity/MountHuaDiscipleEntity.java"
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§d[Discípulo del Monte Hua] §a«Toma este Talismán Taoísta de Sellado. Que el sendero de la Espada de Ciruelo guíe tu cultivo.»"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.entity.mount_hua_disciple.talisman_given"), true);'
)
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§d[Discípulo del Monte Hua] §f«Saludos, compañero cultivador. Las flores de ciruelo del Monte Hua florecen firmes contra la corrupción demoníaca.»"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.entity.mount_hua_disciple.greeting"), true);'
)

# 16. CustomTierFurnaceBlock.java
p = "src/main/java/com/soybe/awakeningsystem/furnace/CustomTierFurnaceBlock.java"
with open(p, "r", encoding="utf-8") as f:
    fc = f.read()
fc = fc.replace('player.displayClientMessage(Component.literal("§e✦ Mejora desinstalada. Horno restaurado a modo estándar."), true);',
                'player.displayClientMessage(Component.translatable("awakening_system.furnace.upgrade_removed"), true);')
with open(p, "w", encoding="utf-8") as f:
    f.write(fc)
print(f"Patched {p}")

# 17. Weapons
p = "src/main/java/com/soybe/awakeningsystem/item/AdamantiteWarHammerItem.java"
replace_in_file(p,
    'p.displayClientMessage(Component.literal("§c§l✦ ¡GOLPE DEMOLEDOR! §fEnemigo aturdido (Lentitud IV, Debilidad II)"), true);',
    'p.displayClientMessage(Component.translatable("awakening_system.weapon.adamantite_warhammer.smash"), true);'
)
replace_in_file(p,
    'sp.displayClientMessage(Component.literal("§6§l✦ ¡CAÍDA EN PICADO SÍSMICA!"), true);',
    'sp.displayClientMessage(Component.translatable("awakening_system.weapon.adamantite_warhammer.plunge"), true);'
)

p = "src/main/java/com/soybe/awakeningsystem/item/AstralGuandaoItem.java"
replace_in_file(p,
    'sp.displayClientMessage(Component.literal("§b§l✦ ¡TAJO DE DRAGÓN ASTRAL! §f(Creciente Celestial desatado)"), true);',
    'sp.displayClientMessage(Component.translatable("awakening_system.weapon.astral_guandao.slash"), true);'
)

p = "src/main/java/com/soybe/awakeningsystem/item/AwakeningCoinItem.java"
replace_in_file(p,
    'p.displayClientMessage(Component.literal("§6§l[LANZAMIENTO] §e¡Lanzando ficha al aire (Cara o Cruz)...!"), true);',
    'p.displayClientMessage(Component.translatable("awakening_system.item.awakening_coin.toss"), true);'
)

p = "src/main/java/com/soybe/awakeningsystem/item/CelestialDragonPearlItem.java"
replace_in_file(p,
    'sp.displayClientMessage(Component.literal("§6§l✦ [PERLA CELESTIAL] §fHas asimilado el núcleo dracónico (+1,000 Qi / Salud y Energía al 100%)."), true);',
    'sp.displayClientMessage(Component.translatable("awakening_system.item.celestial_dragon_pearl.assimilated"), true);'
)

p = "src/main/java/com/soybe/awakeningsystem/item/ChaosManaCoreItem.java"
replace_in_file(p,
    'sp.displayClientMessage(Component.literal("§5§l[NÚCLEO DEL CAOS] §fHas asimilado la energía de los Cuatro Reyes Elementales (+3500 Qi / +2000 EXP)."), true);',
    'sp.displayClientMessage(Component.translatable("awakening_system.item.chaos_mana_core.assimilated"), true);'
)

p = "src/main/java/com/soybe/awakeningsystem/item/PlumBlossomSwordItem.java"
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§d§l[24 Movimientos de Flor de Ciruelo] §f¡Torbellino de pétalos de Qi desatado!"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.weapon.plum_blossom_sword.moves_24"), true);'
)
replace_in_file(p,
    'attacker.displayClientMessage(Component.literal("§d§l✦ ¡FLORECIMIENTO TOTAL DEL MONTE HUA! §f(+15 Daño Qi Explosivo)"), true);',
    'attacker.displayClientMessage(Component.translatable("awakening_system.weapon.plum_blossom_sword.bloom_burst"), true);'
)

p = "src/main/java/com/soybe/awakeningsystem/item/RadiantLightSwordItem.java"
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§e§l[Danza de Luz Radiante] §f¡Purificación sagrada y corte cegador desatados!"), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.weapon.radiant_light_sword.light_dance"), true);'
)

# 18. Pills
p = "src/main/java/com/soybe/awakeningsystem/item/BlackTortoisePillItem.java"
replace_in_file(p,
    'AwakeningSystemMod.notify(sp, "§3✦ ELIXIR DE LA TORTUGA NEGRA ✦", "§bVitalidad inmutable y Resistencia Marcial activadas.", 0xFF00CED1);',
    'AwakeningSystemMod.notify(sp, "awakening_system.item.black_tortoise_pill.notify_title", "awakening_system.item.black_tortoise_pill.notify_desc", 0xFF00CED1);'
)

p = "src/main/java/com/soybe/awakeningsystem/item/MarrowCleansingPillItem.java"
replace_in_file(p,
    'AwakeningSystemMod.notify(sp, "§a✦ LIMPIEZA DE MÉDULA ✦", "§f¡Impurezas purificadas! Has desbloqueado el Meridiano " + (openedIdx + 1) + ".", 0xFF55FF55);',
    'AwakeningSystemMod.notify(sp, "awakening_system.item.marrow_cleansing_pill.opened_title", "awakening_system.item.marrow_cleansing_pill.opened_desc", 0xFF55FF55, (openedIdx + 1));'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(sp, "§a✦ MÉDULA PERFECTA ✦", "§fTus 12 meridianos ya están abiertos: +1,200 Qi de Cultivo.", 0xFF55FF55);',
    'AwakeningSystemMod.notify(sp, "awakening_system.item.marrow_cleansing_pill.max_title", "awakening_system.item.marrow_cleansing_pill.max_desc", 0xFF55FF55);'
)

p = "src/main/java/com/soybe/awakeningsystem/item/RealmBreakthroughPillItem.java"
replace_in_file(p,
    'AwakeningSystemMod.notify(sp, "§d✦ CÚSPIDE DE REINO ALCANZADA ✦", "§fHas alcanzado el Sub-nivel 9 de " + SystemEvents.getDantianRealmComponent(realm).getString() + ".", 0xFFD055FF);',
    'AwakeningSystemMod.notify(sp, "awakening_system.item.realm_breakthrough_pill.pinnacle_title", "awakening_system.item.realm_breakthrough_pill.pinnacle_desc", 0xFFD055FF, SystemEvents.getDantianRealmComponent(realm).getString());'
)
replace_in_file(p,
    'AwakeningSystemMod.notify(sp, "§6✦ ¡RUPTURA DE REINO EXITOSA! ✦", "§f¡Has avanzado al " + SystemEvents.getDantianRealmComponent(realm + 1).getString() + "!", 0xFFFFD700);',
    'AwakeningSystemMod.notify(sp, "awakening_system.item.realm_breakthrough_pill.breakthrough_title", "awakening_system.item.realm_breakthrough_pill.breakthrough_desc", 0xFFFFD700, SystemEvents.getDantianRealmComponent(realm + 1).getString());'
)

p = "src/main/java/com/soybe/awakeningsystem/item/RisingDragonPillItem.java"
replace_in_file(p,
    'AwakeningSystemMod.notify(sp, "§6✦ PÍLDORA DEL DRAGÓN NACIENTE ✦", "§e¡Torrente espiritual de dragón! +1,500 Qi de Cultivo.", 0xFFFFD700);',
    'AwakeningSystemMod.notify(sp, "awakening_system.item.rising_dragon_pill.notify_title", "awakening_system.item.rising_dragon_pill.notify_desc", 0xFFFFD700);'
)

# 19. WuXingPuzzleManager.java
p = "src/main/java/com/soybe/awakeningsystem/world/WuXingPuzzleManager.java"
replace_in_file(p,
    'player.displayClientMessage(Component.literal("§a[Wu Xing] §fEl Ciclo de los Cinco Elementos ya está en perfecta armonía y la compuerta sagrada permanece abierta."), true);',
    'player.displayClientMessage(Component.translatable("awakening_system.wuxing.already_open"), true);'
)
replace_in_file(p,
    'sp.displayClientMessage(Component.literal("§6§l[GRUTA ANCESTRAL] §f¡El Ciclo de los Cinco Elementos está en armonía perfecta! La Gran Compuerta de Jade se ha abierto."), false);',
    'sp.displayClientMessage(Component.translatable("awakening_system.wuxing.gate_opened"), false);'
)

print("Java files patched successfully!")
