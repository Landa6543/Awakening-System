import os
from PIL import Image, ImageDraw, ImageFont

ITEM_DIR = "src/main/resources/assets/awakening_system/textures/item"
BLOCK_DIR = "src/main/resources/assets/awakening_system/textures/block"
OUTPUT = "C:/Users/soybe/.gemini/antigravity/brain/930e4101-5310-48ee-9b53-1a9777f45603/pixelart_showcase.png"

# Collect showcases
items_to_show = [
    ("mana_beef.png", "Carne Maná"),
    ("cooked_mana_beef.png", "Carne Asada"),
    ("mana_chicken_meat.png", "Pollo Maná"),
    ("cooked_mana_chicken_meat.png", "Pollo Asado"),
    ("mana_porkchop.png", "Chuleta Maná"),
    ("cooked_mana_porkchop.png", "Chuleta Asada"),
    ("mana_mutton.png", "Cordero Maná"),
    ("cooked_mana_mutton.png", "Cordero Asado"),
    ("sunsettia.png", "Fruta Ocaso"),
    ("mana_fruit.png", "Fruta Maná"),
    ("sweet_madame.png", "Pollo Dulce"),
    ("slime_condensate.png", "Condensado"),
    ("slime_secretions.png", "Secreciones"),
    ("slime_concentrate.png", "Concentrado"),
    ("condensed_resin.png", "Resina Conc."),
    ("damaged_mask.png", "Másc. Dañada"),
    ("stained_mask.png", "Másc. Manch."),
    ("ominous_mask.png", "Másc. Amen."),
    ("heavy_horn.png", "Cuerno Pesado")
]

blocks_to_show = [
    ("cuihua_leaves.png", "Cuihua Hojas"),
    ("cuihua_leaves_fruited.png", "Cuihua Frutal"),
    ("sandbearer_leaves.png", "Sandbearer Hojas"),
    ("adhigama_leaves.png", "Adhigama Hojas"),
    ("mana_wood_leaves.png", "Maná Hojas"),
    ("mana_wood_leaves_fruited.png", "Maná Frutal"),
    ("eldoria_leaves.png", "Eldoria Hojas"),
    ("cuihua_log.png", "Cuihua Tronco"),
    ("sandbearer_log.png", "Sandb. Tronco"),
    ("adhigama_log.png", "Adhigama Tronco"),
    ("mana_wood_log.png", "Maná Tronco"),
    ("eldoria_log.png", "Eldoria Tronco"),
    ("cuihua_planks.png", "Cuihua Madera"),
    ("sandbearer_planks.png", "Sandb. Madera")
]

CELL_SIZE = 64 # Upscale 16x16 4x
PADDING = 12
COLS = 6

num_items = len(items_to_show)
num_blocks = len(blocks_to_show)
total = num_items + num_blocks
rows = (total + COLS - 1) // COLS

img_w = COLS * (CELL_SIZE + PADDING * 2) + PADDING * 2
img_h = rows * (CELL_SIZE + 36) + 70

canvas = Image.new("RGBA", (img_w, img_h), (22, 24, 32, 255))
draw = ImageDraw.Draw(canvas)

# Title
draw.rectangle([0, 0, img_w, 48], fill=(35, 39, 52, 255))
draw.text((20, 14), "AWAKENING SYSTEM - 100% AUTENTICO PIXEL ART MINECRAFT (16x16)", fill=(255, 215, 0, 255))

all_entries = [(os.path.join(ITEM_DIR, f), label) for f, label in items_to_show] + \
              [(os.path.join(BLOCK_DIR, f), label) for f, label in blocks_to_show]

for idx, (path, label) in enumerate(all_entries):
    col = idx % COLS
    row = idx // COLS
    x = PADDING * 2 + col * (CELL_SIZE + PADDING * 2)
    y = 60 + row * (CELL_SIZE + 36)

    # Box background
    draw.rectangle([x - 4, y - 4, x + CELL_SIZE + 4, y + CELL_SIZE + 4], fill=(14, 16, 22, 255), outline=(50, 56, 75, 255))

    if os.path.exists(path):
        icon = Image.open(path).convert("RGBA")
        icon_scaled = icon.resize((CELL_SIZE, CELL_SIZE), Image.Resampling.NEAREST)
        canvas.paste(icon_scaled, (x, y), icon_scaled)
    
    # Label
    draw.text((x - 2, y + CELL_SIZE + 6), label, fill=(200, 210, 225, 255))

canvas.save(OUTPUT)
print(f"Showcase generated at {OUTPUT}")
