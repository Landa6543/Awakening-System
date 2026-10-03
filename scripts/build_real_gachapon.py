import json
import os
from PIL import Image, ImageDraw

BLOCK_TEX_DIR = "src/main/resources/assets/awakening_system/textures/block"
ITEM_TEX_DIR = "src/main/resources/assets/awakening_system/textures/item"
BLOCK_MODEL_DIR = "src/main/resources/assets/awakening_system/models/block"
ITEM_MODEL_DIR = "src/main/resources/assets/awakening_system/models/item"

os.makedirs(BLOCK_TEX_DIR, exist_ok=True)
os.makedirs(ITEM_TEX_DIR, exist_ok=True)
os.makedirs(BLOCK_MODEL_DIR, exist_ok=True)
os.makedirs(ITEM_MODEL_DIR, exist_ok=True)

# -------------------------------------------------------------------------
# 1. TEXTURES FOR REAL-LIFE GASHAPON
# -------------------------------------------------------------------------

def build_textures():
    atlas = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(atlas)
    
    # Quadrant A: [0..31, 0..31] -> Cabinet Body (Pearl White Metal with Crimson Trims)
    d.rectangle([0, 0, 31, 31], fill=(242, 244, 248, 255), outline=(195, 200, 210, 255))
    d.rectangle([0, 0, 31, 4], fill=(195, 28, 45, 255))
    d.line([(0, 4), (31, 4)], fill=(235, 185, 45, 255))
    d.rectangle([0, 27, 31, 31], fill=(195, 28, 45, 255))
    d.line([(0, 27), (31, 27)], fill=(235, 185, 45, 255))
    d.line([(2, 5), (2, 26)], fill=(215, 220, 228, 255))
    d.line([(29, 5), (29, 26)], fill=(215, 220, 228, 255))
    
    # Quadrant B: [32..63, 0..31] -> Front Dial Mechanism Plate
    d.rectangle([32, 0, 63, 31], fill=(215, 220, 226, 255), outline=(140, 145, 155, 255))
    d.rectangle([34, 2, 61, 29], outline=(230, 180, 45, 255))
    # Coin slot on top
    d.rectangle([45, 4, 50, 6], fill=(30, 30, 35, 255), outline=(100, 105, 115, 255))
    d.point([(44, 5), (51, 5)], fill=(235, 185, 45, 255))
    # Rotary dial circle
    d.ellipse([39, 10, 56, 27], fill=(185, 192, 202, 255), outline=(110, 115, 125, 255), width=2)
    # Turn crank handle
    d.rectangle([36, 17, 59, 20], fill=(235, 185, 45, 255), outline=(150, 110, 20, 255))
    d.ellipse([45, 16, 50, 21], fill=(255, 255, 255, 255), outline=(180, 140, 30, 255))
    
    # Quadrant C: [0..31, 32..63] -> Prize Dispenser Chute
    d.rectangle([0, 32, 31, 63], fill=(32, 34, 40, 255), outline=(15, 15, 20, 255), width=2)
    d.rectangle([3, 35, 28, 56], fill=(60, 65, 75, 255), outline=(110, 115, 130, 255))
    d.line([(5, 37), (26, 37)], fill=(160, 165, 180, 255))
    d.rectangle([8, 43, 23, 50], fill=(45, 48, 56, 255), outline=(90, 95, 110, 255))
    d.line([(10, 46), (21, 46)], fill=(200, 210, 225, 255))
    
    # Quadrant D: [32..63, 32..63] -> Top Lid & Primogem Banner
    d.rectangle([32, 32, 63, 63], fill=(195, 28, 45, 255), outline=(130, 15, 25, 255), width=2)
    d.rectangle([35, 35, 60, 60], fill=(235, 185, 45, 255), outline=(255, 225, 90, 255))
    d.rectangle([38, 38, 57, 57], fill=(20, 25, 40, 255))
    # Glowing Primogem 4-point star in center
    d.polygon([(47, 40), (51, 47), (47, 54), (43, 47)], fill=(0, 240, 255, 255))
    d.point([(47, 47)], fill=(255, 255, 255, 255))
    
    atlas.save(os.path.join(BLOCK_TEX_DIR, "gacha_machine_base.png"))
    print("Generated gacha_machine_base.png")

    # 2. Glass Texture: 16x16 semi-transparent acrylic
    glass = Image.new("RGBA", (16, 16), (210, 235, 255, 55))
    gd = ImageDraw.Draw(glass)
    gd.line([(2, 0), (15, 13)], fill=(255, 255, 255, 160), width=1)
    gd.line([(0, 6), (9, 15)], fill=(255, 255, 255, 100), width=1)
    gd.rectangle([0, 0, 15, 15], outline=(180, 220, 255, 80))
    glass.save(os.path.join(BLOCK_TEX_DIR, "gacha_machine_glass.png"))
    print("Generated gacha_machine_glass.png")

    # 3. Mini Capsule Textures for inside the machine
    colors = {
        "gacha_capsule_gold": ((245, 190, 35), (160, 120, 15)),
        "gacha_capsule_blue": ((45, 135, 245), (20, 65, 160)),
        "gacha_capsule_purple": ((165, 55, 240), (95, 25, 150))
    }
    for name, (col_main, col_dark) in colors.items():
        cap = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        cd = ImageDraw.Draw(cap)
        # Top half clear glass with shine
        cd.rectangle([2, 2, 13, 7], fill=(210, 235, 255, 180), outline=(170, 210, 245, 220))
        cd.line([(4, 3), (7, 3)], fill=(255, 255, 255, 255))
        # Inner prize paper roll
        cd.rectangle([5, 4, 10, 7], fill=(245, 240, 225, 240))
        # Bottom half colored plastic cup
        cd.rectangle([2, 8, 13, 13], fill=col_main, outline=col_dark)
        cd.line([(2, 8), (13, 8)], fill=(255, 255, 255, 220))
        cd.line([(3, 13), (12, 13)], fill=col_dark)
        cap.save(os.path.join(BLOCK_TEX_DIR, f"{name}.png"))
        print(f"Generated {name}.png")

# -------------------------------------------------------------------------
# 2. 3D BLOCK MODELS
# -------------------------------------------------------------------------

def build_models():
    textures_full = {
        "base": "awakening_system:block/gacha_machine_base",
        "glass": "awakening_system:block/gacha_machine_glass",
        "capsule_gold": "awakening_system:block/gacha_capsule_gold",
        "capsule_blue": "awakening_system:block/gacha_capsule_blue",
        "capsule_purple": "awakening_system:block/gacha_capsule_purple",
        "particle": "awakening_system:block/gacha_machine_base"
    }

    # A. LOWER MODEL
    lower = {
        "credit": "Awakening System - Canonical Gashapon Dispenser (Lower)",
        "textures": {
            "base": "awakening_system:block/gacha_machine_base",
            "particle": "awakening_system:block/gacha_machine_base"
        },
        "elements": [
            # Base pedestal feet
            {
                "name": "base_stand",
                "from": [2, 0, 2],
                "to": [14, 2, 14],
                "faces": {
                    "north": {"uv": [0, 6, 8, 8], "texture": "#base"},
                    "south": {"uv": [0, 6, 8, 8], "texture": "#base"},
                    "east": {"uv": [0, 6, 8, 8], "texture": "#base"},
                    "west": {"uv": [0, 6, 8, 8], "texture": "#base"},
                    "down": {"uv": [0, 6, 8, 8], "texture": "#base"},
                    "up": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # Main cabinet body
            {
                "name": "cabinet_body",
                "from": [2.5, 2, 2.5],
                "to": [13.5, 16, 13.5],
                "faces": {
                    "north": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "south": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "east": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "west": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "up": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # Dispenser pocket chute
            {
                "name": "dispenser_pocket",
                "from": [4, 2.5, 1.8],
                "to": [12, 6.5, 2.5],
                "faces": {
                    "north": {"uv": [0, 8, 8, 16], "texture": "#base"},
                    "east": {"uv": [0, 8, 2, 16], "texture": "#base"},
                    "west": {"uv": [6, 8, 8, 16], "texture": "#base"},
                    "up": {"uv": [0, 8, 8, 10], "texture": "#base"},
                    "down": {"uv": [0, 14, 8, 16], "texture": "#base"}
                }
            },
            # Coin dial plate
            {
                "name": "dial_plate",
                "from": [4.5, 8, 1.8],
                "to": [11.5, 14, 2.5],
                "faces": {
                    "north": {"uv": [8, 0, 16, 8], "texture": "#base"},
                    "east": {"uv": [8, 0, 10, 8], "texture": "#base"},
                    "west": {"uv": [14, 0, 16, 8], "texture": "#base"},
                    "up": {"uv": [8, 0, 16, 2], "texture": "#base"},
                    "down": {"uv": [8, 6, 16, 8], "texture": "#base"}
                }
            },
            # Twist dial butterfly handle
            {
                "name": "dial_handle",
                "from": [6.5, 9.5, 1.0],
                "to": [9.5, 12.5, 1.8],
                "faces": {
                    "north": {"uv": [9, 2, 15, 6], "texture": "#base"},
                    "east": {"uv": [9, 2, 11, 4], "texture": "#base"},
                    "west": {"uv": [9, 2, 11, 4], "texture": "#base"},
                    "up": {"uv": [9, 2, 13, 4], "texture": "#base"},
                    "down": {"uv": [9, 2, 13, 4], "texture": "#base"}
                }
            }
        ]
    }
    with open(os.path.join(BLOCK_MODEL_DIR, "gacha_machine_lower.json"), "w") as f:
        json.dump(lower, f, indent=2)
    print("Saved gacha_machine_lower.json")

    # B. UPPER MODEL
    upper = {
        "credit": "Awakening System - Canonical Gashapon Dispenser (Upper)",
        "textures": textures_full,
        "elements": [
            # Connector collar
            {
                "name": "collar",
                "from": [2.5, 0, 2.5],
                "to": [13.5, 1.5, 13.5],
                "faces": {
                    "north": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "south": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "east": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "west": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "down": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "up": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # Clear acrylic display chamber
            {
                "name": "glass_tank",
                "from": [3, 1.5, 3],
                "to": [13, 12, 13],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#glass"}
                }
            },
            # Capsule toys resting inside
            {
                "name": "toy_cap1",
                "from": [4.5, 1.5, 4.5],
                "to": [7.5, 4.5, 7.5],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"}
                }
            },
            {
                "name": "toy_cap2",
                "from": [8.5, 1.5, 5],
                "to": [11.5, 4.5, 8],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"}
                }
            },
            {
                "name": "toy_cap3",
                "from": [6, 1.5, 8.5],
                "to": [9, 4.5, 11.5],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"}
                }
            },
            {
                "name": "toy_cap4",
                "from": [5.5, 4.5, 6],
                "to": [8.5, 7.5, 9],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"}
                }
            },
            {
                "name": "toy_cap5",
                "from": [8, 4.5, 7.5],
                "to": [11, 7.5, 10.5],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"}
                }
            },
            # Top lid with Primogem header banner
            {
                "name": "top_lid",
                "from": [2, 12, 2],
                "to": [14, 15, 14],
                "faces": {
                    "north": {"uv": [8, 8, 16, 16], "texture": "#base"},
                    "south": {"uv": [8, 8, 16, 12], "texture": "#base"},
                    "east": {"uv": [8, 8, 16, 12], "texture": "#base"},
                    "west": {"uv": [8, 8, 16, 12], "texture": "#base"},
                    "up": {"uv": [8, 8, 16, 16], "texture": "#base"},
                    "down": {"uv": [8, 8, 16, 16], "texture": "#base"}
                }
            }
        ]
    }
    with open(os.path.join(BLOCK_MODEL_DIR, "gacha_machine_upper.json"), "w") as f:
        json.dump(upper, f, indent=2)
    print("Saved gacha_machine_upper.json")

    # C. UNIFIED SHOWCASE / ITEM MODEL (Complete machine scaled to 16 height)
    item_model = {
        "credit": "Awakening System - Canonical Gashapon Dispenser (Item Showcase)",
        "textures": textures_full,
        "elements": [
            # Base stand
            {
                "name": "base_stand",
                "from": [3, 0, 3],
                "to": [13, 1.2, 13],
                "faces": {
                    "north": {"uv": [0, 6, 8, 8], "texture": "#base"},
                    "south": {"uv": [0, 6, 8, 8], "texture": "#base"},
                    "east": {"uv": [0, 6, 8, 8], "texture": "#base"},
                    "west": {"uv": [0, 6, 8, 8], "texture": "#base"},
                    "down": {"uv": [0, 6, 8, 8], "texture": "#base"},
                    "up": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # Lower cabinet
            {
                "name": "cabinet",
                "from": [3.5, 1.2, 3.5],
                "to": [12.5, 7.5, 12.5],
                "faces": {
                    "north": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "south": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "east": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "west": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "up": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # Dispenser chute
            {
                "name": "dispenser",
                "from": [5, 1.8, 2.8],
                "to": [11, 4.0, 3.5],
                "faces": {
                    "north": {"uv": [0, 8, 8, 16], "texture": "#base"},
                    "east": {"uv": [0, 8, 2, 16], "texture": "#base"},
                    "west": {"uv": [6, 8, 8, 16], "texture": "#base"}
                }
            },
            # Dial plate & handle
            {
                "name": "dial",
                "from": [5.2, 4.6, 2.8],
                "to": [10.8, 7.0, 3.5],
                "faces": {
                    "north": {"uv": [8, 0, 16, 8], "texture": "#base"}
                }
            },
            {
                "name": "crank",
                "from": [6.8, 5.2, 2.2],
                "to": [9.2, 6.4, 2.8],
                "faces": {
                    "north": {"uv": [9, 2, 15, 6], "texture": "#base"},
                    "east": {"uv": [9, 2, 11, 4], "texture": "#base"},
                    "west": {"uv": [9, 2, 11, 4], "texture": "#base"}
                }
            },
            # Upper glass chamber
            {
                "name": "glass",
                "from": [4, 7.5, 4],
                "to": [12, 13.5, 12],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#glass"}
                }
            },
            # Little capsules inside
            {
                "name": "c1",
                "from": [5, 7.5, 5],
                "to": [7.5, 10, 7.5],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"}
                }
            },
            {
                "name": "c2",
                "from": [8.5, 7.5, 6],
                "to": [11, 10, 8.5],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"}
                }
            },
            {
                "name": "c3",
                "from": [6.5, 7.5, 8.5],
                "to": [9, 10, 11],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"}
                }
            },
            # Top lid with header card
            {
                "name": "lid",
                "from": [3.5, 13.5, 3.5],
                "to": [12.5, 15.5, 12.5],
                "faces": {
                    "north": {"uv": [8, 8, 16, 16], "texture": "#base"},
                    "south": {"uv": [8, 8, 16, 12], "texture": "#base"},
                    "east": {"uv": [8, 8, 16, 12], "texture": "#base"},
                    "west": {"uv": [8, 8, 16, 12], "texture": "#base"},
                    "up": {"uv": [8, 8, 16, 16], "texture": "#base"},
                    "down": {"uv": [8, 8, 16, 16], "texture": "#base"}
                }
            }
        ],
        "display": {
            "gui": {
                "rotation": [30, 225, 0],
                "translation": [0, -0.5, 0],
                "scale": [0.65, 0.65, 0.65]
            },
            "ground": {
                "rotation": [0, 0, 0],
                "translation": [0, 3, 0],
                "scale": [0.35, 0.35, 0.35]
            },
            "thirdperson_righthand": {
                "rotation": [75, 45, 0],
                "translation": [0, 2.5, 0],
                "scale": [0.38, 0.38, 0.38]
            },
            "thirdperson_lefthand": {
                "rotation": [75, 45, 0],
                "translation": [0, 2.5, 0],
                "scale": [0.38, 0.38, 0.38]
            },
            "firstperson_righthand": {
                "rotation": [0, 45, 0],
                "translation": [0, 0, 0],
                "scale": [0.40, 0.40, 0.40]
            },
            "firstperson_lefthand": {
                "rotation": [0, 225, 0],
                "translation": [0, 0, 0],
                "scale": [0.40, 0.40, 0.40]
            },
            "fixed": {
                "rotation": [0, 0, 0],
                "translation": [0, 0, 0],
                "scale": [0.5, 0.5, 0.5]
            }
        }
    }
    with open(os.path.join(BLOCK_MODEL_DIR, "gacha_machine.json"), "w") as f:
        json.dump(item_model, f, indent=2)
    with open(os.path.join(ITEM_MODEL_DIR, "gacha_machine.json"), "w") as f:
        json.dump({"parent": "awakening_system:block/gacha_machine"}, f, indent=2)
    print("Saved unified gacha_machine.json for block and item!")

if __name__ == "__main__":
    print("Building Real Gachapon Textures & Models...")
    build_textures()
    build_models()
    print("Finished successfully!")
