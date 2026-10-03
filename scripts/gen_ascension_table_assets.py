from PIL import Image, ImageDraw
import json
import os

block_tex_dir = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\textures\block"
os.makedirs(block_tex_dir, exist_ok=True)

# 1. Top texture: 32x32 Celestial golden/amethyst star chart with glowing constellation lines
img_top = Image.new("RGBA", (32, 32), (25, 20, 45, 255))
draw_t = ImageDraw.Draw(img_top)

# Wood border
draw_t.rectangle([0, 0, 31, 31], outline=(55, 35, 75, 255), width=2)
draw_t.rectangle([2, 2, 29, 29], fill=(30, 22, 50, 255))

# Outer gold celestial ring
draw_t.ellipse([4, 4, 27, 27], outline=(230, 190, 60, 255), width=1)
draw_t.ellipse([7, 7, 24, 24], outline=(160, 110, 220, 200), width=1)

# Center star
draw_t.point((16, 16), fill=(255, 255, 255, 255))
draw_t.line([(16, 10), (16, 22)], fill=(255, 225, 120, 255))
draw_t.line([(10, 16), (22, 16)], fill=(255, 225, 120, 255))

# Constellation stars
stars = [(11, 11), (21, 11), (11, 21), (21, 21), (16, 8), (16, 24), (8, 16), (24, 16)]
for (sx, sy) in stars:
    draw_t.point((sx, sy), fill=(140, 230, 255, 255))

top_path = os.path.join(block_tex_dir, "companion_ascension_table_top.png")
img_top.save(top_path)
print("Saved top texture:", top_path)

# 2. Side texture: 32x32 Ornate table leg & side with mystic runes
img_side = Image.new("RGBA", (32, 32), (35, 25, 55, 255))
draw_s = ImageDraw.Draw(img_side)
# Top and bottom rims
draw_s.rectangle([0, 0, 31, 4], fill=(60, 40, 85, 255))
draw_s.rectangle([0, 27, 31, 31], fill=(50, 30, 75, 255))
draw_s.line([(0, 4), (31, 4)], fill=(230, 190, 60, 255))
draw_s.line([(0, 27), (31, 27)], fill=(230, 190, 60, 255))
# Center gem socket
draw_s.rectangle([13, 13, 18, 18], fill=(20, 15, 35, 255))
draw_s.rectangle([14, 14, 17, 17], fill=(180, 70, 240, 255))
draw_s.point((15, 15), fill=(255, 255, 255, 255))

side_path = os.path.join(block_tex_dir, "companion_ascension_table_side.png")
img_side.save(side_path)
print("Saved side texture:", side_path)

# 3. Bottom texture
img_bot = Image.new("RGBA", (32, 32), (40, 25, 60, 255))
bot_path = os.path.join(block_tex_dir, "companion_ascension_table_bottom.png")
img_bot.save(bot_path)
print("Saved bottom texture:", bot_path)

# 4. Block Model JSON
block_model_dir = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\models\block"
os.makedirs(block_model_dir, exist_ok=True)
block_model_path = os.path.join(block_model_dir, "companion_ascension_table.json")
with open(block_model_path, 'w', encoding='utf-8') as f:
    json.dump({
        "parent": "minecraft:block/cube_bottom_top",
        "textures": {
            "top": "awakening_system:block/companion_ascension_table_top",
            "bottom": "awakening_system:block/companion_ascension_table_bottom",
            "side": "awakening_system:block/companion_ascension_table_side"
        }
    }, f, indent=2)
print("Saved block model:", block_model_path)

# 5. Blockstate JSON
blockstate_dir = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\blockstates"
os.makedirs(blockstate_dir, exist_ok=True)
blockstate_path = os.path.join(blockstate_dir, "companion_ascension_table.json")
with open(blockstate_path, 'w', encoding='utf-8') as f:
    json.dump({
        "variants": {
            "": { "model": "awakening_system:block/companion_ascension_table" }
        }
    }, f, indent=2)
print("Saved blockstate:", blockstate_path)
