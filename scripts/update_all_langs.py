import os
import json

LANG_DIR = "src/main/resources/assets/awakening_system/lang"

es_entries = {
    "awakening_system.skill.shonen_infinite_void.name": "Expansión de Dominio: Vacío Infinito",
    "awakening_system.skill.shonen_infinite_void.description": "Técnica suprema de Gojo Satoru. Despliega el espacio interior del Infinito, sobrecargando el cerebro de los objetivos con información ilimitada.",
    "awakening_system.skill.shonen_infinite_void.effect": "Despliega una cúpula de 24 bloques: inmoviliza al 100% y silencia a todos los enemigos por 9s, infligiendo daño continuo y otorgando velocidad, fuerza y regeneración al usuario.",

    "awakening_system.skill.shonen_getsuga_tensho.name": "Getsuga Tensho",
    "awakening_system.skill.shonen_getsuga_tensho.description": "Técnica de corte espiritual de Bleach. Comprime el reiatsu en la hoja de la espada y lo libera como una luna creciente de devastación.",
    "awakening_system.skill.shonen_getsuga_tensho.effect": "Lanza una onda en media luna de llamas espirituales negras que perfora enemigos en 26 bloques en línea recta, infligiendo daño crítico masivo e incineración.",

    "awakening_system.skill.shonen_conquerors_haki.name": "Haki del Conquistador",
    "awakening_system.skill.shonen_conquerors_haki.description": "La manifestación de la fuerza de voluntad suprema de One Piece. Quien lo domina puede doblegar el espíritu de sus adversarios sin tocarlos.",
    "awakening_system.skill.shonen_conquerors_haki.effect": "Onda expansiva de 20 bloques: noquea e incapacita instantáneamente a enemigos normales, y reduce la defensa de jefes infligiendo daño espiritual.",

    "awakening_system.skill.shonen_zoltraak.name": "Zoltraak (Magia Perforadora)",
    "awakening_system.skill.shonen_zoltraak.description": "La magia destructora de demonios perfeccionada por Frieren. Proyecta un círculo mágico geométrico y dispara un rayo concentrado que aniquila la materia.",
    "awakening_system.skill.shonen_zoltraak.effect": "Proyecta un círculo mágico rúnico frente al usuario y dispara un rayo de 32 bloques que atraviesa y anula armaduras y defensas mágicas.",

    "awakening_system.skill.shonen_rasenshuriken.name": "Futon: Rasenshuriken",
    "awakening_system.skill.shonen_rasenshuriken.description": "Técnica definitiva de elemento viento creada por Naruto. Combina la manipulación de forma y naturaleza de chakra en aspas cortantes a nivel celular.",
    "awakening_system.skill.shonen_rasenshuriken.effect": "Arroja un shuriken de viento a 16 bloques que detona en una microtormenta de vórtice cortante, levantando y destrozando a los enemigos atrapados.",

    "item.awakening_system.primogem": "Protogema",
    "item.awakening_system.intertwined_fate": "Destino Entrelazado",
    "item.awakening_system.acquaint_fate": "Destino del Encuentro",
    "item.awakening_system.staff_of_homa": "Báculo de Homa (5★)",
    "item.awakening_system.light_cone_milky_way": "Cono de Luz: Noche en la Vía Láctea (5★)",
    "item.awakening_system.command_spell": "Sellos de Comando (Fate/Grand Order 5★)",
    "item.awakening_system.vision_pyro": "Visión Pyro (Fuego)",
    "item.awakening_system.vision_electro": "Visión Electro (Rayo)",
    "item.awakening_system.vision_cryo": "Visión Cryo (Hielo)",
    "item.awakening_system.vision_anemo": "Visión Anemo (Viento)",
    "item.awakening_system.paimon_spawn_egg": "Huevo Generador de Paimon",
    "entity.awakening_system.paimon": "Paimon"
}

en_entries = {
    "awakening_system.skill.shonen_infinite_void.name": "Domain Expansion: Infinite Void",
    "awakening_system.skill.shonen_infinite_void.description": "Supreme technique of Satoru Gojo. Unfolds the inner space of the Limitless, overloading targets' minds with infinite information.",
    "awakening_system.skill.shonen_infinite_void.effect": "Deploys a 24-block domain dome: completely freezes and disarms enemies for 9s while granting Speed IV, Strength III, and Regeneration to the user.",

    "awakening_system.skill.shonen_getsuga_tensho.name": "Getsuga Tensho",
    "awakening_system.skill.shonen_getsuga_tensho.description": "Spiritual slash technique from Bleach. Compresses spiritual pressure onto the blade edge and releases a crescent moon of destruction.",
    "awakening_system.skill.shonen_getsuga_tensho.effect": "Unleashes a 26-block piercing crescent wave of black-crimson spiritual flames, dealing massive slashing damage and burning enemies.",

    "awakening_system.skill.shonen_conquerors_haki.name": "Conqueror's Haki",
    "awakening_system.skill.shonen_conquerors_haki.description": "The manifestation of supreme willpower from One Piece. Subdues and shatters the spirit of adversaries without physical contact.",
    "awakening_system.skill.shonen_conquerors_haki.effect": "20-block shockwave: instantly knocks out standard enemies and shatters boss defense while dealing spiritual damage.",

    "awakening_system.skill.shonen_zoltraak.name": "Zoltraak (Killing Magic)",
    "awakening_system.skill.shonen_zoltraak.description": "Demon-slaying magic refined by Frieren. Projects a geometric magic circle and unleashes a concentrated piercing beam.",
    "awakening_system.skill.shonen_zoltraak.effect": "Projects a vertical runic circle and fires a 32-block beam piercing all magic and physical armor.",

    "awakening_system.skill.shonen_rasenshuriken.name": "Wind Style: Rasenshuriken",
    "awakening_system.skill.shonen_rasenshuriken.description": "Ultimate wind style technique by Naruto. Infuses chakra shape and wind nature into spinning blades that shred at cellular level.",
    "awakening_system.skill.shonen_rasenshuriken.effect": "Hurls a wind shuriken 16 blocks forward that explodes into a micro-vortex, lifting and shredding all trapped enemies.",

    "item.awakening_system.primogem": "Primogem",
    "item.awakening_system.intertwined_fate": "Intertwined Fate",
    "item.awakening_system.acquaint_fate": "Acquaint Fate",
    "item.awakening_system.staff_of_homa": "Staff of Homa (5★)",
    "item.awakening_system.light_cone_milky_way": "Light Cone: Night on the Milky Way (5★)",
    "item.awakening_system.command_spell": "Command Spells (Fate/Grand Order 5★)",
    "item.awakening_system.vision_pyro": "Pyro Vision",
    "item.awakening_system.vision_electro": "Electro Vision",
    "item.awakening_system.vision_cryo": "Cryo Vision",
    "item.awakening_system.vision_anemo": "Anemo Vision",
    "item.awakening_system.paimon_spawn_egg": "Paimon Spawn Egg",
    "entity.awakening_system.paimon": "Paimon"
}

for filename in os.listdir(LANG_DIR):
    if not filename.endswith(".json"):
        continue
    filepath = os.path.join(LANG_DIR, filename)
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)

    if filename == "en_us.json":
        data.update(en_entries)
    else:
        data.update(es_entries)

    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

print("All language files updated with Gacha and Shonen localizations!")
