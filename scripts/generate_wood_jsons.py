import os
import json

BASE_ASSETS = "src/main/resources/assets/awakening_system"
BASE_DATA = "src/main/resources/data/awakening_system"

WOOD_TYPES = ["spirit_plum", "celestial_pine"]

def make_self_drop_loot_table(block_name):
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "bonus_rolls": 0.0,
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    }
                ],
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": f"awakening_system:{block_name}"
                    }
                ],
                "rolls": 1.0
            }
        ]
    }

def make_door_loot_table(block_name):
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "bonus_rolls": 0.0,
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    }
                ],
                "entries": [
                    {
                        "type": "minecraft:item",
                        "conditions": [
                            {
                                "block": f"awakening_system:{block_name}",
                                "condition": "minecraft:block_state_property",
                                "properties": {
                                    "half": "lower"
                                }
                            }
                        ],
                        "name": f"awakening_system:{block_name}"
                    }
                ],
                "rolls": 1.0
            }
        ]
    }

def copy_or_adapt_template(src_wood, dst_wood):
    # 1. Stairs
    with open(f"{BASE_ASSETS}/blockstates/{src_wood}_stairs.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/blockstates/{dst_wood}_stairs.json", "w") as f:
        f.write(content)
        
    for sub in ["", "_inner", "_outer"]:
        with open(f"{BASE_ASSETS}/models/block/{src_wood}_stairs{sub}.json", "r") as f:
            content = f.read().replace(src_wood, dst_wood)
        with open(f"{BASE_ASSETS}/models/block/{dst_wood}_stairs{sub}.json", "w") as f:
            f.write(content)
            
    with open(f"{BASE_ASSETS}/models/item/{src_wood}_stairs.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/models/item/{dst_wood}_stairs.json", "w") as f:
        f.write(content)
        
    with open(f"{BASE_DATA}/loot_table/blocks/{dst_wood}_stairs.json", "w") as f:
        json.dump(make_self_drop_loot_table(f"{dst_wood}_stairs"), f, indent=2)

    # 2. Slab
    with open(f"{BASE_ASSETS}/blockstates/{src_wood}_slab.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/blockstates/{dst_wood}_slab.json", "w") as f:
        f.write(content)
        
    for sub in ["", "_top"]:
        with open(f"{BASE_ASSETS}/models/block/{src_wood}_slab{sub}.json", "r") as f:
            content = f.read().replace(src_wood, dst_wood)
        with open(f"{BASE_ASSETS}/models/block/{dst_wood}_slab{sub}.json", "w") as f:
            f.write(content)
            
    with open(f"{BASE_ASSETS}/models/item/{src_wood}_slab.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/models/item/{dst_wood}_slab.json", "w") as f:
        f.write(content)
        
    with open(f"{BASE_DATA}/loot_table/blocks/{dst_wood}_slab.json", "w") as f:
        json.dump(make_self_drop_loot_table(f"{dst_wood}_slab"), f, indent=2)

    # 3. Fence
    with open(f"{BASE_ASSETS}/blockstates/{src_wood}_fence.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/blockstates/{dst_wood}_fence.json", "w") as f:
        f.write(content)
        
    for sub in ["_post", "_side", "_inventory"]:
        if os.path.exists(f"{BASE_ASSETS}/models/block/{src_wood}_fence{sub}.json"):
            with open(f"{BASE_ASSETS}/models/block/{src_wood}_fence{sub}.json", "r") as f:
                content = f.read().replace(src_wood, dst_wood)
            with open(f"{BASE_ASSETS}/models/block/{dst_wood}_fence{sub}.json", "w") as f:
                f.write(content)
                
    with open(f"{BASE_ASSETS}/models/item/{src_wood}_fence.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/models/item/{dst_wood}_fence.json", "w") as f:
        f.write(content)
        
    with open(f"{BASE_DATA}/loot_table/blocks/{dst_wood}_fence.json", "w") as f:
        json.dump(make_self_drop_loot_table(f"{dst_wood}_fence"), f, indent=2)

    # 4. Fence Gate
    with open(f"{BASE_ASSETS}/blockstates/{src_wood}_fence_gate.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/blockstates/{dst_wood}_fence_gate.json", "w") as f:
        f.write(content)
        
    for sub in ["", "_open", "_wall", "_wall_open"]:
        if os.path.exists(f"{BASE_ASSETS}/models/block/{src_wood}_fence_gate{sub}.json"):
            with open(f"{BASE_ASSETS}/models/block/{src_wood}_fence_gate{sub}.json", "r") as f:
                content = f.read().replace(src_wood, dst_wood)
            with open(f"{BASE_ASSETS}/models/block/{dst_wood}_fence_gate{sub}.json", "w") as f:
                f.write(content)
                
    with open(f"{BASE_ASSETS}/models/item/{src_wood}_fence_gate.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/models/item/{dst_wood}_fence_gate.json", "w") as f:
        f.write(content)
        
    with open(f"{BASE_DATA}/loot_table/blocks/{dst_wood}_fence_gate.json", "w") as f:
        json.dump(make_self_drop_loot_table(f"{dst_wood}_fence_gate"), f, indent=2)

    # 5. Door
    with open(f"{BASE_ASSETS}/blockstates/{src_wood}_door.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/blockstates/{dst_wood}_door.json", "w") as f:
        f.write(content)
        
    for sub in ["_bottom", "_bottom_rh", "_top", "_top_rh"]:
        if os.path.exists(f"{BASE_ASSETS}/models/block/{src_wood}_door{sub}.json"):
            with open(f"{BASE_ASSETS}/models/block/{src_wood}_door{sub}.json", "r") as f:
                content = f.read().replace(src_wood, dst_wood)
            with open(f"{BASE_ASSETS}/models/block/{dst_wood}_door{sub}.json", "w") as f:
                f.write(content)
                
    with open(f"{BASE_ASSETS}/models/item/{src_wood}_door.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/models/item/{dst_wood}_door.json", "w") as f:
        f.write(content)
        
    with open(f"{BASE_DATA}/loot_table/blocks/{dst_wood}_door.json", "w") as f:
        json.dump(make_door_loot_table(f"{dst_wood}_door"), f, indent=2)

    # 6. Trapdoor
    with open(f"{BASE_ASSETS}/blockstates/{src_wood}_trapdoor.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/blockstates/{dst_wood}_trapdoor.json", "w") as f:
        f.write(content)
        
    for sub in ["_bottom", "_top", "_open"]:
        if os.path.exists(f"{BASE_ASSETS}/models/block/{src_wood}_trapdoor{sub}.json"):
            with open(f"{BASE_ASSETS}/models/block/{src_wood}_trapdoor{sub}.json", "r") as f:
                content = f.read().replace(src_wood, dst_wood)
            with open(f"{BASE_ASSETS}/models/block/{dst_wood}_trapdoor{sub}.json", "w") as f:
                f.write(content)
                
    with open(f"{BASE_ASSETS}/models/item/{src_wood}_trapdoor.json", "r") as f:
        content = f.read().replace(src_wood, dst_wood)
    with open(f"{BASE_ASSETS}/models/item/{dst_wood}_trapdoor.json", "w") as f:
        f.write(content)
        
    with open(f"{BASE_DATA}/loot_table/blocks/{dst_wood}_trapdoor.json", "w") as f:
        json.dump(make_self_drop_loot_table(f"{dst_wood}_trapdoor"), f, indent=2)

def main():
    for target in WOOD_TYPES:
        print(f"Generating models, blockstates, and loot tables for {target}...")
        copy_or_adapt_template("cuihua", target)
    print("Done generating wood JSONs!")

if __name__ == "__main__":
    main()
