import json
import os

lang_dir = r"src/main/resources/assets/awakening_system/lang"

en_path = os.path.join(lang_dir, "en_us.json")
es_path = os.path.join(lang_dir, "es_es.json")

with open(en_path, "r", encoding="utf-8") as f:
    en_dict = json.load(f)

with open(es_path, "r", encoding="utf-8") as f:
    es_dict = json.load(f)

# 1. Update English translations
en_updates = {
    "item.awakening_system.gift_card_5": "Gift Card ($5 USD)",
    "item.awakening_system.gift_card_15": "Gift Card ($15 USD)",
    "item.awakening_system.gift_card_30": "Gift Card ($30 USD)",
    "item.awakening_system.gift_card_50": "Gift Card ($50 USD)",
    "item.awakening_system.gift_card_100": "Gift Card ($100 USD)",

    "awakening_system.screen.artifacts.title": "MONARCH ARTIFACTS",
    "awakening_system.screen.artifacts.btn_max": "MAX",
    "awakening_system.screen.artifacts.empty_slot": "Empty slot (%s)",
    "awakening_system.screen.artifacts.total_stats": "§6§l✦ TOTAL STATS",
    "awakening_system.screen.artifacts.stat.hp": "§a✦ Max HP: §f+%s HP",
    "awakening_system.screen.artifacts.stat.atk": "§c✦ Physical ATK: §f+%s ATK",
    "awakening_system.screen.artifacts.stat.crit_rate": "§e✦ Crit Rate: §f+%s%%",
    "awakening_system.screen.artifacts.stat.crit_dmg": "§e✦ Crit DMG: §f+%s%%",
    "awakening_system.screen.artifacts.set_bonuses": "§6§l✦ SET BONUSES",
    "awakening_system.screen.artifacts.no_set_bonus": "§8No 2P/4P set bonus active",
    "awakening_system.screen.artifacts.set_bonus_hint": "§8Equip 2 or 4 pieces of the same set",
    "awakening_system.screen.artifacts.upgrade_guide": "§e💡 Upgrade (+0 to +20): §7Press §a[▲+1] §7or Right Click. Costs §6Coins §7or §bOres§7.",
    "awakening_system.screen.artifacts.inv_label": "§7Artifacts in inventory (Click to equip):",
    "awakening_system.screen.artifacts.upgrade_title": "§6§lUpgrade Artifact (+1)",
    "awakening_system.screen.artifacts.current_level": "§7Current level: §a+%s §8➔ §e+%s §8/ +20",
    "awakening_system.screen.artifacts.cost": "§7Cost: §6%s Coins §7or §b1x Enhancement Ore",
    "awakening_system.screen.artifacts.upgrade_instruction": "§e✦ Left Click on [▲+1] or Right Click on slot.",
    "awakening_system.screen.artifacts.max_reached": "§a✔ Max Level (+20) reached.",
    "awakening_system.screen.artifacts.tip_right_click": "§8▪ §aRight Click: §7Upgrade (+1)",
    "awakening_system.screen.artifacts.tip_left_click": "§8▪ §cLeft Click: §7Unequip",

    "awakening_system.screen.artifacts.bonus.flower": "+Max HP",
    "awakening_system.screen.artifacts.bonus.plume": "+Flat ATK",
    "awakening_system.screen.artifacts.bonus.sands": "+% ATK Bonus",
    "awakening_system.screen.artifacts.bonus.goblet": "+% Elemental DMG",
    "awakening_system.screen.artifacts.bonus.circlet": "+% Crit Rate & Crit DMG",

    "awakening_system.artifact_type.flower.name": "Flower of Life",
    "awakening_system.artifact_type.plume.name": "Plume of Death",
    "awakening_system.artifact_type.sands.name": "Sands of Eon",
    "awakening_system.artifact_type.goblet.name": "Goblet of Eonothem",
    "awakening_system.artifact_type.circlet.name": "Circlet of Logos",

    "awakening_system.artifact_set.crimson_witch.name": "Crimson Witch of Flames",
    "awakening_system.artifact_set.crimson_witch.bonus2p": "+20% Pyro and Fire Skill DMG.",
    "awakening_system.artifact_set.crimson_witch.bonus4p": "Explosions and burns deal +40% additional damage and burn twice as long.",

    "awakening_system.artifact_set.gladiator.name": "Gladiator's Finale",
    "awakening_system.artifact_set.gladiator.bonus2p": "+18% Physical Attack and Base DMG.",
    "awakening_system.artifact_set.gladiator.bonus4p": "+35% additional damage when hitting with Swords, Daggers, and Spears.",

    "awakening_system.artifact_set.viridescent.name": "Viridescent Venerer",
    "awakening_system.artifact_set.viridescent.bonus2p": "+20% Anemo / Wind DMG and +15% Speed.",
    "awakening_system.artifact_set.viridescent.bonus4p": "Wind swirl vortices reduce enemy physical and elemental resistance by 30%.",

    "awakening_system.artifact_set.maiden.name": "Maiden Beloved",
    "awakening_system.artifact_set.maiden.bonus2p": "+20% Healing and Regeneration Effectiveness.",
    "awakening_system.artifact_set.maiden.bonus4p": "Healing yourself or a companion restores 25% additional health to all nearby allies.",

    "awakening_system.artifact_set.bloodstained.name": "Bloodstained Chivalry",
    "awakening_system.artifact_set.bloodstained.bonus2p": "+25% Physical DMG and Armor Piercing.",
    "awakening_system.artifact_set.bloodstained.bonus4p": "Defeating an enemy grants +50% DMG to the next strike and knocks down the target.",

    "awakening_system.artifact_set.berserker.name": "Berserker",
    "awakening_system.artifact_set.berserker.bonus2p": "+12% Critical Hit Rate.",
    "awakening_system.artifact_set.berserker.bonus4p": "When below 70% HP, Critical Rate increases by an additional +24%.",

    "awakening_system.artifact_set.exile.name": "The Exile",
    "awakening_system.artifact_set.exile.bonus2p": "+20% Mana and Energy Recharge.",
    "awakening_system.artifact_set.exile.bonus4p": "Using an ultimate or elemental skill regenerates instant Mana for you and allies.",

    "awakening_system.artifact_set.brave_heart.name": "Brave Heart",
    "awakening_system.artifact_set.brave_heart.bonus2p": "+18% Base Attack.",
    "awakening_system.artifact_set.brave_heart.bonus4p": "Increases DMG by +30% against enemies with more than 50% HP.",

    "awakening_system.artifact.tooltip.type": "§7Type: §e%s §8| §fEnhancement: §a+%s§7/20",
    "awakening_system.artifact.tooltip.hp": "§a✦ Max HP: §f+%s HP",
    "awakening_system.artifact.tooltip.atk": "§c✦ Base ATK: §f+%s ATK",
    "awakening_system.artifact.tooltip.atk_pct": "§6✦ ATK Bonus: §f+%s%%",
    "awakening_system.artifact.tooltip.elemental": "§b✦ Elemental DMG: §f+%s%%",
    "awakening_system.artifact.tooltip.crit_rate": "§e✦ Crit Rate: §f+%s%%",
    "awakening_system.artifact.tooltip.crit_dmg": "§e✦ Crit DMG: §f+%s%%",
    "awakening_system.artifact.tooltip.set_bonuses": "§6§l✦ Set Bonuses:",
    "awakening_system.artifact.tooltip.2p": " §e[2 Pieces]: §7%s",
    "awakening_system.artifact.tooltip.4p": " §e[4 Pieces]: §7%s",
    "awakening_system.artifact.tooltip.hint": "§8▸ Equippable in System (Player) or on Companions.",

    "awakening_system.artifact.msg.equipped": "§a§l✦ [ARTIFACT EQUIPPED] §fYou equipped %s%s §7(%s).",
    "awakening_system.artifact.msg.wrong_slot": "§cThis piece (%s) cannot be equipped in this slot.",
    "awakening_system.artifact.msg.unequipped": "§e✦ [ARTIFACT] Artifact removed and stored in your inventory.",
    "awakening_system.artifact.msg.companion_equipped": "§a§l✦ [ARTIFACT] §fEquipped %s%s §fto %s!",
    "awakening_system.artifact.msg.companion_unequipped": "§e✦ %s's artifact removed and stored in your inventory.",
    "awakening_system.artifact.msg.max_level": "§e✦ This artifact has already reached max level (+20).",
    "awakening_system.artifact.msg.insufficient_funds": "§c[Artifacts] Insufficient funds. Requires §e%s Awakening Coins §cor §b1 Enhancement Ore §cto upgrade to +%s.",
    "awakening_system.artifact.msg.upgraded": "§6§l✦ [ARTIFACT UPGRADED] §f%s §fupgraded to §a+%s§f! %s",
    "awakening_system.artifact.msg.cost_ore": "§b(Used 1 Enhancement Ore)",
    "awakening_system.artifact.msg.cost_coins": "§e(Spent %s Coins)",

    "awakening_system.screen.dim.subspace.title": "§9✦ §lDimensional Subspace",
    "awakening_system.screen.dim.subspace.desc": "§7Secure dimensional storage expandable with chests.\n§bClick to open your System inventory."
}

# 2. Update Spanish translations
es_updates = {
    "item.awakening_system.gift_card_5": "Tarjeta de Regalo ($5 USD)",
    "item.awakening_system.gift_card_15": "Tarjeta de Regalo ($15 USD)",
    "item.awakening_system.gift_card_30": "Tarjeta de Regalo ($30 USD)",
    "item.awakening_system.gift_card_50": "Tarjeta de Regalo ($50 USD)",
    "item.awakening_system.gift_card_100": "Tarjeta de Regalo ($100 USD)",

    "awakening_system.screen.artifacts.title": "ARTEFACTOS DEL MONARCA",
    "awakening_system.screen.artifacts.btn_max": "MÁX",
    "awakening_system.screen.artifacts.empty_slot": "Ranura vacía (%s)",
    "awakening_system.screen.artifacts.total_stats": "§6§l✦ ATRIBUTOS TOTALES",
    "awakening_system.screen.artifacts.stat.hp": "§a✦ Vida Máxima: §f+%s HP",
    "awakening_system.screen.artifacts.stat.atk": "§c✦ Ataque Físico: §f+%s ATQ",
    "awakening_system.screen.artifacts.stat.crit_rate": "§e✦ Probabilidad Crítica: §f+%s%%",
    "awakening_system.screen.artifacts.stat.crit_dmg": "§e✦ Daño Crítico: §f+%s%%",
    "awakening_system.screen.artifacts.set_bonuses": "§6§l✦ BONOS DE CONJUNTO",
    "awakening_system.screen.artifacts.no_set_bonus": "§8Ningún bono de 2P/4P activo",
    "awakening_system.screen.artifacts.set_bonus_hint": "§8Equipa 2 o 4 piezas del mismo set",
    "awakening_system.screen.artifacts.upgrade_guide": "§e💡 Mejora (+0 a +20): §7Pulsa §a[▲+1] §7o Clic Derecho. Cuesta §6Monedas §7o §bMinerales§7.",
    "awakening_system.screen.artifacts.inv_label": "§7Artefactos en inventario (Clic para equipar):",
    "awakening_system.screen.artifacts.upgrade_title": "§6§lMejorar Artefacto (+1)",
    "awakening_system.screen.artifacts.current_level": "§7Nivel actual: §a+%s §8➔ §e+%s §8/ +20",
    "awakening_system.screen.artifacts.cost": "§7Coste: §6%s Monedas §7o §b1x Mineral de Mejora",
    "awakening_system.screen.artifacts.upgrade_instruction": "§e✦ Clic Izquierdo en [▲+1] o Clic Derecho en la casilla.",
    "awakening_system.screen.artifacts.max_reached": "§a✔ Nivel Máximo (+20) alcanzado.",
    "awakening_system.screen.artifacts.tip_right_click": "§8▪ §aClic Derecho: §7Mejorar (+1)",
    "awakening_system.screen.artifacts.tip_left_click": "§8▪ §cClic Izquierdo: §7Desequipar",

    "awakening_system.screen.artifacts.bonus.flower": "+Vida Máxima",
    "awakening_system.screen.artifacts.bonus.plume": "+Ataque Plano",
    "awakening_system.screen.artifacts.bonus.sands": "+% Bono Ataque",
    "awakening_system.screen.artifacts.bonus.goblet": "+% Daño Elemental",
    "awakening_system.screen.artifacts.bonus.circlet": "+% Prob. y Daño Crítico",

    "awakening_system.artifact_type.flower.name": "Flor de la Vida",
    "awakening_system.artifact_type.plume.name": "Pluma de la Muerte",
    "awakening_system.artifact_type.sands.name": "Arenas del Eón",
    "awakening_system.artifact_type.goblet.name": "Cáliz de Eonothem",
    "awakening_system.artifact_type.circlet.name": "Tiara de Logos",

    "awakening_system.artifact_set.crimson_witch.name": "Bruja Carmesí en Llamas",
    "awakening_system.artifact_set.crimson_witch.bonus2p": "+20% Daño de Fuego y Habilidades Pyro.",
    "awakening_system.artifact_set.crimson_witch.bonus4p": "Explosiones y quemaduras infligen +40% de daño adicional y queman por el doble de tiempo.",

    "awakening_system.artifact_set.gladiator.name": "Final del Gladiador",
    "awakening_system.artifact_set.gladiator.bonus2p": "+18% Ataque Físico y Daño Base.",
    "awakening_system.artifact_set.gladiator.bonus4p": "+35% de Daño adicional al golpear con Espadas, Dagas y Lanzas.",

    "awakening_system.artifact_set.viridescent.name": "Sombra Verde Esmeralda",
    "awakening_system.artifact_set.viridescent.bonus2p": "+20% Daño Anemo / Viento y +15% Velocidad.",
    "awakening_system.artifact_set.viridescent.bonus4p": "Los torbellinos de viento reducen la resistencia física y elemental del enemigo un 30%.",

    "awakening_system.artifact_set.maiden.name": "Doncella Amada",
    "awakening_system.artifact_set.maiden.bonus2p": "+20% Eficacia de Curación y Regeneración.",
    "awakening_system.artifact_set.maiden.bonus4p": "Al curarte a ti o a un compañero, restaura un 25% de salud adicional a todos los aliados cercanos.",

    "awakening_system.artifact_set.bloodstained.name": "Caballero Sanguinario",
    "awakening_system.artifact_set.bloodstained.bonus2p": "+25% Daño Físico y Penetración.",
    "awakening_system.artifact_set.bloodstained.bonus4p": "Derrotar a un enemigo otorga +50% de Daño al siguiente impacto y derriba al objetivo.",

    "awakening_system.artifact_set.berserker.name": "Berserker",
    "awakening_system.artifact_set.berserker.bonus2p": "+12% Probabilidad de Golpe Crítico.",
    "awakening_system.artifact_set.berserker.bonus4p": "Al estar por debajo del 70% de vida, la Probabilidad Crítica aumenta un +24% adicional.",

    "awakening_system.artifact_set.exile.name": "El Exiliado",
    "awakening_system.artifact_set.exile.bonus2p": "+20% Recarga de Maná y Energía.",
    "awakening_system.artifact_set.exile.bonus4p": "Activar una habilidad definitiva o elemental regenera Maná instantáneo para ti y tus aliados.",

    "awakening_system.artifact_set.brave_heart.name": "Corazón del Guerrero",
    "awakening_system.artifact_set.brave_heart.bonus2p": "+18% Ataque General.",
    "awakening_system.artifact_set.brave_heart.bonus4p": "+30% de Daño aumentado contra enemigos que tengan más del 50% de su vida restante.",

    "awakening_system.artifact.tooltip.type": "§7Tipo: §e%s §8| §fMejora: §a+%s§7/20",
    "awakening_system.artifact.tooltip.hp": "§a✦ Vida Máxima: §f+%s HP",
    "awakening_system.artifact.tooltip.atk": "§c✦ Ataque Base: §f+%s ATQ",
    "awakening_system.artifact.tooltip.atk_pct": "§6✦ Bono de Ataque: §f+%s%%",
    "awakening_system.artifact.tooltip.elemental": "§b✦ Daño Elemental: §f+%s%%",
    "awakening_system.artifact.tooltip.crit_rate": "§e✦ Probabilidad Crítica: §f+%s%%",
    "awakening_system.artifact.tooltip.crit_dmg": "§e✦ Daño Crítico: §f+%s%%",
    "awakening_system.artifact.tooltip.set_bonuses": "§6§l✦ Bonos de Conjunto:",
    "awakening_system.artifact.tooltip.2p": " §e[2 Piezas]: §7%s",
    "awakening_system.artifact.tooltip.4p": " §e[4 Piezas]: §7%s",
    "awakening_system.artifact.tooltip.hint": "§8▸ Equipable en el Sistema (Jugador) o en Compañeros.",

    "awakening_system.artifact.msg.equipped": "§a§l✦ [ARTEFACTO EQUIPADO] §fHas equipado %s%s §7(%s).",
    "awakening_system.artifact.msg.wrong_slot": "§cEsta pieza (%s) no corresponde a esta ranura.",
    "awakening_system.artifact.msg.unequipped": "§e✦ [ARTEFACTO] Artefacto retirado y guardado en tu inventario.",
    "awakening_system.artifact.msg.companion_equipped": "§a§l✦ [ARTEFACTO] §f¡Has equipado %s%s §fa %s!",
    "awakening_system.artifact.msg.companion_unequipped": "§e✦ Artefacto de %s retirado a tu inventario.",
    "awakening_system.artifact.msg.max_level": "§e✦ Este artefacto ya ha alcanzado el nivel máximo (+20).",
    "awakening_system.artifact.msg.insufficient_funds": "§c[Artefactos] Monedas insuficientes. Requiere §e%s Monedas de Despertar §co §b1 Mineral de Mejora §cpara subir a +%s.",
    "awakening_system.artifact.msg.upgraded": "§6§l✦ [ARTEFACTO MEJORADO] §f¡%s §fha mejorado a §a+%s§f! %s",
    "awakening_system.artifact.msg.cost_ore": "§b(Usó 1 Mineral de Mejora)",
    "awakening_system.artifact.msg.cost_coins": "§e(Gastó %s Monedas)",

    "awakening_system.screen.dim.subspace.title": "§9✦ §lSubespacio Dimensional",
    "awakening_system.screen.dim.subspace.desc": "§7Almacén dimensional seguro expandible con cofres.\n§bHaz clic para abrir tu inventario del Sistema."
}

en_dict.update(en_updates)
es_dict.update(es_updates)

# Save en_us.json
with open(en_path, "w", encoding="utf-8") as f:
    json.dump(en_dict, f, ensure_ascii=False, indent=2)

# Save es_es.json
with open(es_path, "w", encoding="utf-8") as f:
    json.dump(es_dict, f, ensure_ascii=False, indent=2)

# Synchronize all Spanish dialect files
spanish_dialects = [
    "es_ar.json",
    "es_cl.json",
    "es_co.json",
    "es_ec.json",
    "es_mx.json",
    "es_pe.json",
    "es_uy.json",
    "es_ve.json"
]

for d in spanish_dialects:
    p = os.path.join(lang_dir, d)
    with open(p, "w", encoding="utf-8") as f:
        json.dump(es_dict, f, ensure_ascii=False, indent=2)
    print(f"Updated and synchronized {d} ({len(es_dict)} keys)")

print("All language files updated and synchronized successfully!")
