import json
import os
from PIL import Image, ImageDraw

BLOCK_TEX_DIR = "src/main/resources/assets/awakening_system/textures/block"
BLOCK_MODEL_DIR = "src/main/resources/assets/awakening_system/models/block"
ITEM_MODEL_DIR = "src/main/resources/assets/awakening_system/models/item"

os.makedirs(BLOCK_TEX_DIR, exist_ok=True)
os.makedirs(BLOCK_MODEL_DIR, exist_ok=True)
os.makedirs(ITEM_MODEL_DIR, exist_ok=True)

# -------------------------------------------------------------------------
# 1. TEXTURES FOR REAL-LIFE GACHAPON
# -------------------------------------------------------------------------

def gen_gacha_textures():
    # 1. Base Machine Texture: 64x64 texture atlas
    # Areas:
    # (0, 0, 32, 32): White/Crimson Cabinet Body
    # (32, 0, 64, 32): Front Mechanism (Coin slot & Turn Dial)
    # (0, 32, 32, 64): Dispenser Tray & Flap
    # (32, 32, 64, 64): Top Lid & Banner
    img = Image.new("RGBA", (64, 64), (245, 245, 248, 255))
    d = ImageDraw.Draw(img)

    # A. Cabinet Body (0,0,32,32) - Pearl white with royal crimson & gold trims
    d.rectangle([0, 0, 31, 31], fill=(245, 245, 250, 255), outline=(210, 210, 220, 255), width=1)
    d.rectangle([0, 0, 31, 4], fill=(195, 30, 45, 255)) # Crimson top bar
    d.rectangle([0, 27, 31, 31], fill=(195, 30, 45, 255)) # Crimson bottom bar
    d.line([(0, 4), (31, 4)], fill=(230, 185, 45, 255), width=1) # Gold line
    d.line([(0, 27), (31, 27)], fill=(230, 185, 45, 255), width=1) # Gold line

    # B. Front Mechanism (32,0,64,32) - Metallic Silver / Chrome Panel with Coin Slot & Turn Dial
    d.rectangle([32, 0, 63, 31], fill=(220, 225, 230, 255), outline=(150, 155, 165, 255), width=1)
    # Gold border plate
    d.rectangle([34, 2, 61, 29], outline=(230, 185, 45, 255), width=1)
    # Coin insertion slot at top
    d.rectangle([45, 5, 50, 7], fill=(40, 40, 45, 255), outline=(130, 135, 140, 255))
    # Dial circle
    d.ellipse([40, 11, 55, 26], fill=(190, 195, 205, 255), outline=(110, 115, 125, 255), width=2)
    # Dial handle bar
    d.rectangle([38, 17, 57, 20], fill=(230, 185, 45, 255), outline=(140, 100, 20, 255))
    d.ellipse([46, 16, 49, 21], fill=(255, 255, 255, 255))

    # C. Dispenser Tray (0,32,32,64) - Dark recessed prize chute with hinged flap
    d.rectangle([0, 32, 31, 63], fill=(35, 35, 40, 255), outline=(20, 20, 25, 255), width=2)
    # Hinged flap
    d.rectangle([3, 35, 28, 55], fill=(70, 75, 85, 255), outline=(130, 135, 145, 255), width=1)
    d.text((10, 42), "PUSH", fill=(210, 215, 225, 255))

    # D. Top Lid & Header (32,32,64,64)
    d.rectangle([32, 32, 63, 63], fill=(195, 30, 45, 255), outline=(140, 15, 25, 255), width=2)
    # Golden star emblem on header
    d.rectangle([36, 36, 59, 59], fill=(230, 185, 45, 255), outline=(255, 225, 95, 255))
    d.rectangle([40, 40, 55, 55], fill=(20, 25, 40, 255))
    # Primogem 4-point star in center
    d.polygon([(47, 42), (50, 47), (47, 52), (44, 47)], fill=(0, 240, 255, 255))

    img.save(os.path.join(BLOCK_TEX_DIR, "gacha_machine_base.png"))
    print("Generated gacha_machine_base.png")

    # 2. Glass Texture: 16x16 semi-transparent acrylic
    glass = Image.new("RGBA", (16, 16), (200, 230, 255, 60))
    gd = ImageDraw.Draw(glass)
    # Bright specular diagonal reflections
    gd.line([(2, 0), (15, 13)], fill=(255, 255, 255, 160), width=1)
    gd.line([(0, 6), (9, 15)], fill=(255, 255, 255, 110), width=1)
    # Glass edges
    gd.rectangle([0, 0, 15, 15], outline=(180, 220, 255, 90))
    glass.save(os.path.join(BLOCK_TEX_DIR, "gacha_machine_glass.png"))
    print("Generated gacha_machine_glass.png")

# -------------------------------------------------------------------------
# 2. BLOCKBENCH VOXEL MODELS
# -------------------------------------------------------------------------

def gen_lower_model():
    model = {
        "credit": "Awakening System - Real-Life Capsule Toy Gashapon Machine (Lower)",
        "textures": {
            "base": "awakening_system:block/gacha_machine_base",
            "particle": "awakening_system:block/gacha_machine_base"
        },
        "elements": [
            # 1. Base Stand / Rubber feet
            {
                "name": "base_pedestal",
                "from": [2, 0, 2],
                "to": [14, 2, 14],
                "faces": {
                    "north": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "south": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "east": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "west": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "down": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "up": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # 2. Main Lower Cabinet
            {
                "name": "cabinet_body",
                "from": [2.5, 2, 2.5],
                "to": [13.5, 15, 13.5],
                "faces": {
                    "north": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "south": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "east": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "west": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "up": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # 3. Front Prize Retrieval Chute (Bottom)
            {
                "name": "prize_dispenser",
                "from": [4, 2.5, 1.2],
                "to": [12, 6.5, 2.5],
                "faces": {
                    "north": {"uv": [0, 8, 8, 16], "texture": "#base"},
                    "east": {"uv": [0, 8, 2, 16], "texture": "#base"},
                    "west": {"uv": [0, 8, 2, 16], "texture": "#base"},
                    "down": {"uv": [0, 8, 8, 10], "texture": "#base"},
                    "up": {"uv": [0, 8, 8, 10], "texture": "#base"}
                }
            },
            # 4. Coin Mechanism & Turn Dial Plate (Front Middle)
            {
                "name": "coin_mechanism",
                "from": [4.5, 8, 1.5],
                "to": [11.5, 14, 2.5],
                "faces": {
                    "north": {"uv": [8, 0, 16, 8], "texture": "#base"},
                    "east": {"uv": [8, 0, 10, 8], "texture": "#base"},
                    "west": {"uv": [8, 0, 10, 8], "texture": "#base"},
                    "up": {"uv": [8, 0, 16, 2], "texture": "#base"},
                    "down": {"uv": [8, 6, 16, 8], "texture": "#base"}
                }
            },
            # 5. Rotary Turn-Crank Dial (Manivela)
            {
                "name": "turn_crank",
                "from": [6.5, 9.5, 0.7],
                "to": [9.5, 12.5, 1.5],
                "faces": {
                    "north": {"uv": [10, 2, 14, 6], "texture": "#base"},
                    "east": {"uv": [10, 2, 12, 6], "texture": "#base"},
                    "west": {"uv": [10, 2, 12, 6], "texture": "#base"},
                    "up": {"uv": [10, 2, 14, 4], "texture": "#base"},
                    "down": {"uv": [10, 4, 14, 6], "texture": "#base"}
                }
            },
            # 6. Upper Dividing Collar (Rim separating lower and upper halves)
            {
                "name": "top_rim",
                "from": [2, 15, 2],
                "to": [14, 16, 14],
                "faces": {
                    "north": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "south": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "east": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "west": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "up": {"uv": [8, 8, 16, 16], "texture": "#base"},
                    "down": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            }
        ]
    }
    with open(os.path.join(BLOCK_MODEL_DIR, "gacha_machine_lower.json"), "w") as f:
        json.dump(model, f, indent=2)
    print("Generated gacha_machine_lower.json")

def gen_upper_model():
    model = {
        "credit": "Awakening System - Real-Life Capsule Toy Gashapon Machine (Upper)",
        "render_type": "minecraft:translucent",
        "textures": {
            "base": "awakening_system:block/gacha_machine_base",
            "glass": "awakening_system:block/gacha_machine_glass",
            "capsule_gold": "awakening_system:block/gacha_capsule_gold",
            "capsule_blue": "awakening_system:block/gacha_capsule_blue",
            "capsule_purple": "awakening_system:block/gacha_capsule_purple",
            "particle": "awakening_system:block/gacha_machine_base"
        },
        "elements": [
            # 1. Lower Glass Base Frame
            {
                "name": "glass_tank_base",
                "from": [2.5, 0, 2.5],
                "to": [13.5, 1, 13.5],
                "faces": {
                    "north": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "south": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "east": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "west": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "down": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "up": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # 2. Transparent Acrylic / Glass Container Walls
            {
                "name": "glass_north",
                "from": [3, 1, 3],
                "to": [13, 12, 3.5],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#glass"}
                }
            },
            {
                "name": "glass_south",
                "from": [3, 1, 12.5],
                "to": [13, 12, 13],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#glass"}
                }
            },
            {
                "name": "glass_west",
                "from": [3, 1, 3.5],
                "to": [3.5, 12, 12.5],
                "faces": {
                    "west": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#glass"}
                }
            },
            {
                "name": "glass_east",
                "from": [12.5, 1, 3.5],
                "to": [13, 12, 12.5],
                "faces": {
                    "east": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#glass"}
                }
            },
            # 3. Colorful Toy Capsules Piled Inside!
            # Capsule 1: Gold 5★ (Left front)
            {
                "name": "capsule_gold_1",
                "from": [4.5, 1.2, 4.5],
                "to": [7.5, 4.2, 7.5],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"}
                }
            },
            # Capsule 2: Blue 3★ (Right front)
            {
                "name": "capsule_blue_1",
                "from": [8.5, 1.2, 5],
                "to": [11.5, 4.2, 8],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"}
                }
            },
            # Capsule 3: Purple 4★ (Left back)
            {
                "name": "capsule_purple_1",
                "from": [4.8, 1.2, 8.5],
                "to": [7.8, 4.2, 11.5],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"}
                }
            },
            # Capsule 4: Gold 5★ (Right back)
            {
                "name": "capsule_gold_2",
                "from": [8.5, 1.2, 8.5],
                "to": [11.5, 4.2, 11.5],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"}
                }
            },
            # Capsule 5: Blue (Second tier center)
            {
                "name": "capsule_blue_2",
                "from": [6.5, 4.2, 6.5],
                "to": [9.5, 7.2, 9.5],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"}
                }
            },
            # Capsule 6: Purple (Second tier left)
            {
                "name": "capsule_purple_2",
                "from": [4.5, 4.2, 6.8],
                "to": [7.5, 7.2, 9.8],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "down": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"}
                }
            },
            # 4. Top Machine Lid / Roof
            {
                "name": "top_lid",
                "from": [2, 12, 2],
                "to": [14, 14.5, 14],
                "faces": {
                    "north": {"uv": [8, 8, 16, 10], "texture": "#base"},
                    "south": {"uv": [8, 8, 16, 10], "texture": "#base"},
                    "east": {"uv": [8, 8, 16, 10], "texture": "#base"},
                    "west": {"uv": [8, 8, 16, 10], "texture": "#base"},
                    "up": {"uv": [8, 8, 16, 16], "texture": "#base"},
                    "down": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # 5. Marquee Header Frame / Banner Plate
            {
                "name": "marquee_header",
                "from": [3.5, 14.5, 7.5],
                "to": [12.5, 16, 8.5],
                "faces": {
                    "north": {"uv": [8, 8, 16, 16], "texture": "#base"},
                    "south": {"uv": [8, 8, 16, 16], "texture": "#base"},
                    "east": {"uv": [8, 8, 10, 16], "texture": "#base"},
                    "west": {"uv": [8, 8, 10, 16], "texture": "#base"},
                    "up": {"uv": [8, 8, 16, 10], "texture": "#base"}
                }
            }
        ]
    }
    with open(os.path.join(BLOCK_MODEL_DIR, "gacha_machine_upper.json"), "w") as f:
        json.dump(model, f, indent=2)
    print("Generated gacha_machine_upper.json")

def gen_unified_item_model():
    # Full Gashapon Machine scaled for 1x1 inventory and block representation
    model = {
        "credit": "Awakening System - Real-Life Capsule Toy Gashapon Machine (Unified Item & Showcase)",
        "render_type": "minecraft:translucent",
        "textures": {
            "base": "awakening_system:block/gacha_machine_base",
            "glass": "awakening_system:block/gacha_machine_glass",
            "capsule_gold": "awakening_system:block/gacha_capsule_gold",
            "capsule_blue": "awakening_system:block/gacha_capsule_blue",
            "capsule_purple": "awakening_system:block/gacha_capsule_purple",
            "particle": "awakening_system:block/gacha_machine_base"
        },
        "elements": [
            # Lower Base Stand
            {
                "name": "base_pedestal",
                "from": [3, 0, 3],
                "to": [13, 1.5, 13],
                "faces": {
                    "north": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "south": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "east": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "west": {"uv": [0, 0, 8, 2], "texture": "#base"},
                    "up": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "down": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # Lower Body
            {
                "name": "lower_body",
                "from": [3.5, 1.5, 3.5],
                "to": [12.5, 7.5, 12.5],
                "faces": {
                    "north": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "south": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "east": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "west": {"uv": [0, 0, 8, 8], "texture": "#base"},
                    "up": {"uv": [0, 0, 8, 8], "texture": "#base"}
                }
            },
            # Dispenser Chute
            {
                "name": "dispenser",
                "from": [5, 2, 2.3],
                "to": [11, 4.2, 3.5],
                "faces": {
                    "north": {"uv": [0, 8, 8, 16], "texture": "#base"},
                    "east": {"uv": [0, 8, 2, 16], "texture": "#base"},
                    "west": {"uv": [0, 8, 2, 16], "texture": "#base"}
                }
            },
            # Turn Crank Dial
            {
                "name": "dial",
                "from": [6.2, 4.8, 2.3],
                "to": [9.8, 6.8, 3.2],
                "faces": {
                    "north": {"uv": [8, 0, 16, 8], "texture": "#base"}
                }
            },
            # Dividing Rim
            {
                "name": "mid_rim",
                "from": [3, 7.5, 3],
                "to": [13, 8.2, 13],
                "faces": {
                    "north": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "south": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "east": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "west": {"uv": [0, 0, 8, 1], "texture": "#base"},
                    "up": {"uv": [8, 8, 16, 16], "texture": "#base"}
                }
            },
            # Transparent Glass Walls
            {
                "name": "glass_n",
                "from": [3.8, 8.2, 3.8],
                "to": [12.2, 14, 4.2],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#glass"}
                }
            },
            {
                "name": "glass_s",
                "from": [3.8, 8.2, 11.8],
                "to": [12.2, 14, 12.2],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#glass"}
                }
            },
            {
                "name": "glass_w",
                "from": [3.8, 8.2, 4.2],
                "to": [4.2, 14, 11.8],
                "faces": {
                    "east": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#glass"}
                }
            },
            {
                "name": "glass_e",
                "from": [11.8, 8.2, 4.2],
                "to": [12.2, 14, 11.8],
                "faces": {
                    "east": {"uv": [0, 0, 16, 16], "texture": "#glass"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#glass"}
                }
            },
            # Capsules Inside
            {
                "name": "cap_gold",
                "from": [5, 8.3, 5],
                "to": [7.4, 10.7, 7.4],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_gold"}
                }
            },
            {
                "name": "cap_blue",
                "from": [8.5, 8.3, 5.5],
                "to": [10.9, 10.7, 7.9],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_blue"}
                }
            },
            {
                "name": "cap_purple",
                "from": [6.8, 8.3, 8.5],
                "to": [9.2, 10.7, 10.9],
                "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "east": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"},
                    "up": {"uv": [0, 0, 16, 16], "texture": "#capsule_purple"}
                }
            },
            # Top Lid
            {
                "name": "lid",
                "from": [3, 14, 3],
                "to": [13, 15.5, 13],
                "faces": {
                    "north": {"uv": [8, 8, 16, 10], "texture": "#base"},
                    "south": {"uv": [8, 8, 16, 10], "texture": "#base"},
                    "east": {"uv": [8, 8, 16, 10], "texture": "#base"},
                    "west": {"uv": [8, 8, 16, 10], "texture": "#base"},
                    "up": {"uv": [8, 8, 16, 16], "texture": "#base"}
                }
            }
        ],
        "display": {
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
            "ground": {
                "rotation": [0, 0, 0],
                "translation": [0, 3, 0],
                "scale": [0.35, 0.35, 0.35]
            },
            "gui": {
                "rotation": [30, 225, 0],
                "translation": [0, 0, 0],
                "scale": [0.62, 0.62, 0.62]
            },
            "fixed": {
                "rotation": [0, 0, 0],
                "translation": [0, 0, 0],
                "scale": [0.5, 0.5, 0.5]
            }
        }
    }
    with open(os.path.join(BLOCK_MODEL_DIR, "gacha_machine.json"), "w") as f:
        json.dump(model, f, indent=2)
    print("Generated unified gacha_machine.json")

    # Item model points directly to unified gacha_machine
    with open(os.path.join(ITEM_MODEL_DIR, "gacha_machine.json"), "w") as f:
        json.dump({"parent": "awakening_system:block/gacha_machine"}, f, indent=2)
    print("Generated models/item/gacha_machine.json")

def main():
    print("--- Generating Gachapon Textures ---")
    gen_gacha_textures()
    print("--- Generating Gachapon Blockbench Models ---")
    gen_lower_model()
    gen_upper_model()
    gen_unified_item_model()
    print("Gachapon real-life models generated successfully!")

if __name__ == "__main__":
    main()
