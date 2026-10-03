# -*- coding: utf-8 -*-
import os, json

eggs = [
    'shadow_infantry_spawn_egg',
    'abyssal_orc_spawn_egg',
    'shadow_monarch_boss_spawn_egg',
    'abyssal_necromancer_spawn_egg',
    'frost_elf_assassin_spawn_egg',
    'isekai_slime_spawn_egg',
    'spirit_fox_spawn_egg',
    'astral_dragon_spawn_egg',
    'shadow_direwolf_spawn_egg',
    'white_thunder_tiger_spawn_egg',
    'cloud_crane_spawn_egg',
    'dokkaebi_spawn_egg',
    'igris_boss_spawn_egg',
    'baran_boss_spawn_egg',
    'blood_qi_totem_spawn_egg',
    'blood_sect_disciple_spawn_egg',
    'blood_patriarch_boss_spawn_egg',
    'yggdrasil_treant_boss_spawn_egg'
]

dir_path = 'src/main/resources/assets/awakening_system/models/item'
os.makedirs(dir_path, exist_ok=True)

model_data = {
    "parent": "minecraft:item/template_spawn_egg"
}

for e in eggs:
    p = os.path.join(dir_path, f"{e}.json")
    with open(p, 'w', encoding='utf-8') as f:
        json.dump(model_data, f, indent=2)
    print(f"Created: {p}")

print(f"All {len(eggs)} spawn egg models successfully written!")
