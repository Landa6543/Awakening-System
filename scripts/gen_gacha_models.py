import os
import json

MODELS_DIR = "src/main/resources/assets/awakening_system/models/item"
os.makedirs(MODELS_DIR, exist_ok=True)

generated_items = [
    "primogem",
    "intertwined_fate",
    "acquaint_fate",
    "light_cone_milky_way",
    "command_spell",
    "vision_pyro",
    "vision_electro",
    "vision_cryo",
    "vision_anemo"
]

for item in generated_items:
    path = os.path.join(MODELS_DIR, f"{item}.json")
    content = {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"awakening_system:item/{item}"
        }
    }
    with open(path, "w", encoding="utf-8") as f:
        json.dump(content, f, indent=2)

# Staff of Homa (Handheld)
with open(os.path.join(MODELS_DIR, "staff_of_homa.json"), "w", encoding="utf-8") as f:
    json.dump({
        "parent": "minecraft:item/handheld",
        "textures": {
            "layer0": "awakening_system:item/staff_of_homa"
        }
    }, f, indent=2)

# Paimon Spawn Egg
with open(os.path.join(MODELS_DIR, "paimon_spawn_egg.json"), "w", encoding="utf-8") as f:
    json.dump({
        "parent": "minecraft:item/template_spawn_egg"
    }, f, indent=2)

print("All item models generated!")
