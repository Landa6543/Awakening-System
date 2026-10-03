import os
import json
from PIL import Image, ImageDraw

BLOCK_TEX_DIR = "src/main/resources/assets/awakening_system/textures/block"
BLOCK_MODEL_DIR = "src/main/resources/assets/awakening_system/models/block"
ITEM_MODEL_DIR = "src/main/resources/assets/awakening_system/models/item"
BLOCKSTATE_DIR = "src/main/resources/assets/awakening_system/blockstates"

os.makedirs(BLOCK_TEX_DIR, exist_ok=True)
os.makedirs(BLOCK_MODEL_DIR, exist_ok=True)
os.makedirs(ITEM_MODEL_DIR, exist_ok=True)
os.makedirs(BLOCKSTATE_DIR, exist_ok=True)

# -------------------------------------------------------------------------
# Texture Generation Helpers
# -------------------------------------------------------------------------

def gen_log_texture(name, base_rgb, groove_rgb, highlight_rgb):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    d = ImageDraw.Draw(img)
    for y in range(16):
        for x in range(16):
            # Vertical bark grooves
            groove = (x * 7 + (y // 3) * 3) % 5 == 0
            ridge = (x * 7 + (y // 3) * 3) % 5 == 1
            if groove:
                c = groove_rgb
            elif ridge:
                c = highlight_rgb
            else:
                c = base_rgb
            img.putpixel((x, y), c)
    img.save(os.path.join(BLOCK_TEX_DIR, f"{name}_log.png"))
    print(f"Generated {name}_log.png")

def gen_log_top_texture(name, bark_rgb, wood_base_rgb, wood_ring_rgb):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    for y in range(16):
        for x in range(16):
            dx = abs(x - 7.5)
            dy = abs(y - 7.5)
            r = (dx*dx + dy*dy)**0.5
            if r > 6.8:
                c = bark_rgb
            elif int(r * 2) % 3 == 0:
                c = wood_ring_rgb
            else:
                c = wood_base_rgb
            img.putpixel((x, y), c)
    img.save(os.path.join(BLOCK_TEX_DIR, f"{name}_log_top.png"))
    print(f"Generated {name}_log_top.png")

def gen_leaves_texture(name, base_rgb, dark_rgb, light_rgb, fruit_rgb=None):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            val = (x * 13 + y * 7 + (x ^ y)) % 10
            if val < 2:
                # small leaf gap
                c = (0, 0, 0, 0)
            elif val < 5:
                c = dark_rgb
            elif val < 8:
                c = base_rgb
            else:
                c = light_rgb
            if fruit_rgb and (x, y) in [(4, 5), (11, 4), (6, 12), (12, 11)]:
                c = fruit_rgb
            img.putpixel((x, y), c)
    img.save(os.path.join(BLOCK_TEX_DIR, f"{name}_leaves.png"))
    print(f"Generated {name}_leaves.png")

def gen_sapling_texture(name, leaf_rgb, stem_rgb, fruit_rgb=None):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Stem
    d.line([(7, 15), (7, 6)], fill=stem_rgb, width=1)
    d.line([(8, 15), (8, 6)], fill=stem_rgb, width=1)
    # Foliage clusters
    d.ellipse([4, 2, 11, 8], fill=leaf_rgb)
    d.ellipse([2, 5, 8, 11], fill=leaf_rgb)
    d.ellipse([8, 5, 13, 11], fill=leaf_rgb)
    if fruit_rgb:
        img.putpixel((7, 4), fruit_rgb)
        img.putpixel((8, 4), fruit_rgb)
    img.save(os.path.join(BLOCK_TEX_DIR, f"{name}_sapling.png"))
    print(f"Generated {name}_sapling.png")

def gen_planks_texture(name, base_rgb, line_rgb, alt_rgb):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    d = ImageDraw.Draw(img)
    for y in range(16):
        for x in range(16):
            if y in [0, 4, 8, 12]:
                c = line_rgb
            elif (x + (y // 4) * 5) % 8 == 0:
                c = line_rgb
            elif (x + y) % 3 == 0:
                c = alt_rgb
            else:
                c = base_rgb
            img.putpixel((x, y), c)
    # Nail accents
    img.putpixel((2, 2), line_rgb)
    img.putpixel((14, 6), line_rgb)
    img.putpixel((2, 10), line_rgb)
    img.putpixel((14, 14), line_rgb)
    img.save(os.path.join(BLOCK_TEX_DIR, f"{name}_planks.png"))
    print(f"Generated {name}_planks.png")

# -------------------------------------------------------------------------
# Build the 3 Teyvat Woods
# -------------------------------------------------------------------------

# 1. Cuihua (Warm amber-golden oak with Sunsettia/Apple blooms)
gen_log_texture("cuihua", (140, 95, 45, 255), (85, 55, 25, 255), (175, 125, 65, 255))
gen_log_top_texture("cuihua", (85, 55, 25, 255), (210, 165, 105, 255), (170, 125, 70, 255))
gen_leaves_texture("cuihua", (65, 145, 40, 255), (35, 95, 25, 255), (115, 195, 60, 255), (255, 140, 40, 255))
gen_sapling_texture("cuihua", (75, 165, 45, 255), (95, 65, 30, 255), (255, 150, 45, 255))
gen_planks_texture("cuihua", (215, 170, 105, 255), (150, 110, 60, 255), (230, 185, 120, 255))

# 2. Adhigama (Sumeru rainforest olive/mossy teak)
gen_log_texture("adhigama", (90, 85, 55, 255), (50, 48, 30, 255), (135, 125, 80, 255))
gen_log_top_texture("adhigama", (50, 48, 30, 255), (160, 150, 110, 255), (120, 110, 75, 255))
gen_leaves_texture("adhigama", (35, 120, 65, 255), (20, 75, 40, 255), (65, 175, 95, 255))
gen_sapling_texture("adhigama", (45, 145, 75, 255), (65, 60, 35, 255))
gen_planks_texture("adhigama", (165, 155, 115, 255), (105, 95, 65, 255), (180, 170, 130, 255))

# 3. Sandbearer (Liyue Karsts amber/autumn mountain pine)
gen_log_texture("sandbearer", (165, 140, 100, 255), (110, 90, 60, 255), (200, 175, 130, 255))
gen_log_top_texture("sandbearer", (110, 90, 60, 255), (225, 200, 155, 255), (180, 155, 110, 255))
gen_leaves_texture("sandbearer", (225, 140, 30, 255), (160, 90, 15, 255), (255, 185, 60, 255))
gen_sapling_texture("sandbearer", (230, 150, 35, 255), (115, 95, 65, 255))
gen_planks_texture("sandbearer", (225, 195, 145, 255), (165, 135, 90, 255), (240, 210, 160, 255))

# -------------------------------------------------------------------------
# Blockstates & Models Generation
# -------------------------------------------------------------------------

woods = ["cuihua", "adhigama", "sandbearer"]

for w in woods:
    # 1. Log blockstate
    bs_log = {
        "variants": {
            "axis=y": {"model": f"awakening_system:block/{w}_log"},
            "axis=z": {"model": f"awakening_system:block/{w}_log_horizontal", "x": 90},
            "axis=x": {"model": f"awakening_system:block/{w}_log_horizontal", "x": 90, "y": 90}
        }
    }
    with open(os.path.join(BLOCKSTATE_DIR, f"{w}_log.json"), "w") as f:
        json.dump(bs_log, f, indent=2)

    # Log models
    m_log = {
        "parent": "minecraft:block/cube_column",
        "textures": {
            "end": f"awakening_system:block/{w}_log_top",
            "side": f"awakening_system:block/{w}_log"
        }
    }
    with open(os.path.join(BLOCK_MODEL_DIR, f"{w}_log.json"), "w") as f:
        json.dump(m_log, f, indent=2)

    m_log_h = {
        "parent": "minecraft:block/cube_column_horizontal",
        "textures": {
            "end": f"awakening_system:block/{w}_log_top",
            "side": f"awakening_system:block/{w}_log"
        }
    }
    with open(os.path.join(BLOCK_MODEL_DIR, f"{w}_log_horizontal.json"), "w") as f:
        json.dump(m_log_h, f, indent=2)

    # Log item model
    with open(os.path.join(ITEM_MODEL_DIR, f"{w}_log.json"), "w") as f:
        json.dump({"parent": f"awakening_system:block/{w}_log"}, f, indent=2)

    # 2. Leaves
    with open(os.path.join(BLOCKSTATE_DIR, f"{w}_leaves.json"), "w") as f:
        json.dump({"variants": {"": {"model": f"awakening_system:block/{w}_leaves"}}}, f, indent=2)

    with open(os.path.join(BLOCK_MODEL_DIR, f"{w}_leaves.json"), "w") as f:
        json.dump({"parent": "minecraft:block/leaves", "textures": {"all": f"awakening_system:block/{w}_leaves"}}, f, indent=2)

    with open(os.path.join(ITEM_MODEL_DIR, f"{w}_leaves.json"), "w") as f:
        json.dump({"parent": f"awakening_system:block/{w}_leaves"}, f, indent=2)

    # 3. Sapling
    with open(os.path.join(BLOCKSTATE_DIR, f"{w}_sapling.json"), "w") as f:
        json.dump({"variants": {"": {"model": f"awakening_system:block/{w}_sapling"}}}, f, indent=2)

    with open(os.path.join(BLOCK_MODEL_DIR, f"{w}_sapling.json"), "w") as f:
        json.dump({"parent": "minecraft:block/cross", "textures": {"cross": f"awakening_system:block/{w}_sapling"}}, f, indent=2)

    with open(os.path.join(ITEM_MODEL_DIR, f"{w}_sapling.json"), "w") as f:
        json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": f"awakening_system:block/{w}_sapling"}}, f, indent=2)

    # 4. Planks
    with open(os.path.join(BLOCKSTATE_DIR, f"{w}_planks.json"), "w") as f:
        json.dump({"variants": {"": {"model": f"awakening_system:block/{w}_planks"}}}, f, indent=2)

    with open(os.path.join(BLOCK_MODEL_DIR, f"{w}_planks.json"), "w") as f:
        json.dump({"parent": "minecraft:block/cube_all", "textures": {"all": f"awakening_system:block/{w}_planks"}}, f, indent=2)

    with open(os.path.join(ITEM_MODEL_DIR, f"{w}_planks.json"), "w") as f:
        json.dump({"parent": f"awakening_system:block/{w}_planks"}, f, indent=2)

print("All Teyvat wood textures, blockstates, and models successfully generated!")
