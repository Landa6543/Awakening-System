import json

en_path = "src/main/resources/assets/awakening_system/lang/en_us.json"
es_path = "src/main/resources/assets/awakening_system/lang/es_es.json"

translations_en = {
    "block.awakening_system.spirit_plum_stairs": "Spirit Plum Stairs",
    "block.awakening_system.spirit_plum_slab": "Spirit Plum Slab",
    "block.awakening_system.spirit_plum_fence": "Spirit Plum Fence",
    "block.awakening_system.spirit_plum_fence_gate": "Spirit Plum Fence Gate",
    "block.awakening_system.spirit_plum_door": "Spirit Plum Door",
    "block.awakening_system.spirit_plum_trapdoor": "Spirit Plum Trapdoor",
    
    "block.awakening_system.celestial_pine_stairs": "Celestial Pine Stairs",
    "block.awakening_system.celestial_pine_slab": "Celestial Pine Slab",
    "block.awakening_system.celestial_pine_fence": "Celestial Pine Fence",
    "block.awakening_system.celestial_pine_fence_gate": "Celestial Pine Fence Gate",
    "block.awakening_system.celestial_pine_door": "Celestial Pine Door",
    "block.awakening_system.celestial_pine_trapdoor": "Celestial Pine Trapdoor"
}

translations_es = {
    "block.awakening_system.spirit_plum_stairs": "Escaleras de Ciruelo Espiritual",
    "block.awakening_system.spirit_plum_slab": "Losa de Ciruelo Espiritual",
    "block.awakening_system.spirit_plum_fence": "Valla de Ciruelo Espiritual",
    "block.awakening_system.spirit_plum_fence_gate": "Puerta de Valla de Ciruelo Espiritual",
    "block.awakening_system.spirit_plum_door": "Puerta de Ciruelo Espiritual",
    "block.awakening_system.spirit_plum_trapdoor": "Trampilla de Ciruelo Espiritual",
    
    "block.awakening_system.celestial_pine_stairs": "Escaleras de Pino Celestial",
    "block.awakening_system.celestial_pine_slab": "Losa de Pino Celestial",
    "block.awakening_system.celestial_pine_fence": "Valla de Pino Celestial",
    "block.awakening_system.celestial_pine_fence_gate": "Puerta de Valla de Pino Celestial",
    "block.awakening_system.celestial_pine_door": "Puerta de Pino Celestial",
    "block.awakening_system.celestial_pine_trapdoor": "Trampilla de Pino Celestial"
}

def update_lang(path, new_keys):
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    for k, v in new_keys.items():
        data[k] = v
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"Updated {path} with {len(new_keys)} keys")

update_lang(en_path, translations_en)
update_lang(es_path, translations_es)
