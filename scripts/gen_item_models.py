import json
import os

model_dir = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\models\item"
os.makedirs(model_dir, exist_ok=True)

items = [
    "nine_revolutions_pill",
    "crimson_phoenix_pill",
    "white_tiger_pill",
    "heavenly_enlightenment_pill",
    "foundation_building_pill",
    "primordial_rebirth_pill",
    "wildfire_crown",
    "wildfire_crown_fragment",
    "wildfire_spawn_egg",
    "lunar_shadow_spawn_egg",
    "lunar_guardian_spawn_egg",
    "companion_ascension_table"
]

for name in items:
    path = os.path.join(model_dir, f"{name}.json")
    if name == "companion_ascension_table":
        data = {
            "parent": "awakening_system:block/companion_ascension_table"
        }
    elif "spawn_egg" in name:
        data = {
            "parent": "minecraft:item/template_spawn_egg"
        }
    else:
        data = {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": f"awakening_system:item/{name}"
            }
        }
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2)
    print("Created model:", path)
