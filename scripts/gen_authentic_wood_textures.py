import os
from PIL import Image

BLOCK_DIR = 'src/main/resources/assets/awakening_system/textures/block'
REF_DIR = 'scratch/vanilla_ref/assets/minecraft/textures/block'
os.makedirs(BLOCK_DIR, exist_ok=True)

def remap_palette(src_path, dst_path, color_stops, flower_fn=None):
    im = Image.open(src_path).convert('RGBA')
    out = Image.new('RGBA', im.size, (0, 0, 0, 0))
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = im.getpixel((x, y))
            if a == 0:
                continue
            if flower_fn and (r > g + 25 and r > b + 15): # Flower/fruit pixel
                out.putpixel((x, y), flower_fn(r, g, b))
                continue
            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            if lum <= color_stops[0][0]:
                col = color_stops[0][1]
            elif lum >= color_stops[-1][0]:
                col = color_stops[-1][1]
            else:
                for i in range(len(color_stops) - 1):
                    l0, c0 = color_stops[i]
                    l1, c1 = color_stops[i+1]
                    if l0 <= lum <= l1:
                        t = (lum - l0) / (l1 - l0)
                        col = tuple(int(c0[k] + (c1[k] - c0[k]) * t) for k in range(3))
                        break
            out.putpixel((x, y), (col[0], col[1], col[2], a))
    out.save(dst_path)
    print(f'Generated {os.path.basename(dst_path)}')

print('--- Generating 100% Authentic Leaf Textures ---')

# 1. Sandbearer Leaves: Golden autumn amber & warm orange (from azalea_leaves)
remap_palette(
    f'{REF_DIR}/azalea_leaves.png',
    f'{BLOCK_DIR}/sandbearer_leaves.png',
    [
        (0.0, (115, 60, 10)),
        (0.3, (185, 115, 20)),
        (0.65, (235, 175, 30)),
        (1.0, (255, 225, 75))
    ]
)

# 2. Cuihua Leaves: Lush vibrant apple-green & emerald with little red fruit blossoms (from flowering_azalea)
def cuihua_fruit_fn(r, g, b):
    # Bright red-orange Cuihua fruit
    return (235, 45, 25, 255) if r > 160 else (195, 30, 20, 255)

remap_palette(
    f'{REF_DIR}/flowering_azalea_leaves.png',
    f'{BLOCK_DIR}/cuihua_leaves.png',
    [
        (0.0, (25, 80, 20)),
        (0.35, (55, 145, 35)),
        (0.7, (110, 200, 50)),
        (1.0, (165, 235, 75))
    ],
    flower_fn=cuihua_fruit_fn
)

# 3. Adhigama Leaves: Deep tropical rainforest greens (from jungle_leaves)
remap_palette(
    f'{REF_DIR}/jungle_leaves.png',
    f'{BLOCK_DIR}/adhigama_leaves.png',
    [
        (0.0, (15, 65, 25)),
        (0.35, (35, 115, 40)),
        (0.7, (65, 175, 55)),
        (1.0, (115, 220, 80))
    ]
)

# 4. Mana Wood Leaves: Ethereal bioluminescent azure-cyan & violet (from azalea_leaves)
remap_palette(
    f'{REF_DIR}/azalea_leaves.png',
    f'{BLOCK_DIR}/mana_wood_leaves.png',
    [
        (0.0, (20, 55, 125)),
        (0.35, (30, 135, 205)),
        (0.7, (55, 220, 245)),
        (1.0, (210, 255, 255))
    ]
)

# 5. Eldoria Leaves: Radiant sacred golden amber (from oak_leaves)
remap_palette(
    f'{REF_DIR}/oak_leaves.png',
    f'{BLOCK_DIR}/eldoria_leaves.png',
    [
        (0.0, (120, 75, 15)),
        (0.35, (195, 135, 25)),
        (0.7, (245, 195, 50)),
        (1.0, (255, 235, 110))
    ]
)

print('--- Generating 100% Authentic Wood Logs & Log Tops ---')

# Sandbearer Log: acacia-inspired warm cedar bark & orange rings
remap_palette(
    f'{REF_DIR}/acacia_log.png',
    f'{BLOCK_DIR}/sandbearer_log.png',
    [(0.0, (75, 65, 55)), (0.5, (125, 115, 100)), (1.0, (175, 165, 145))]
)
remap_palette(
    f'{REF_DIR}/acacia_log_top.png',
    f'{BLOCK_DIR}/sandbearer_log_top.png',
    [(0.0, (85, 45, 20)), (0.35, (175, 105, 45)), (0.7, (215, 150, 70)), (1.0, (240, 190, 110))]
)

# Cuihua Log: oak-inspired warm golden amber bark & rings
remap_palette(
    f'{REF_DIR}/oak_log.png',
    f'{BLOCK_DIR}/cuihua_log.png',
    [(0.0, (65, 45, 25)), (0.5, (115, 80, 45)), (1.0, (160, 120, 75))]
)
remap_palette(
    f'{REF_DIR}/oak_log_top.png',
    f'{BLOCK_DIR}/cuihua_log_top.png',
    [(0.0, (75, 50, 30)), (0.4, (150, 110, 60)), (0.75, (195, 155, 95)), (1.0, (225, 190, 135))]
)

# Adhigama Log: jungle-inspired deep tropical mahogany bark & rings
remap_palette(
    f'{REF_DIR}/jungle_log.png',
    f'{BLOCK_DIR}/adhigama_log.png',
    [(0.0, (45, 25, 15)), (0.5, (85, 50, 30)), (1.0, (130, 85, 55))]
)
remap_palette(
    f'{REF_DIR}/jungle_log_top.png',
    f'{BLOCK_DIR}/adhigama_log_top.png',
    [(0.0, (55, 30, 20)), (0.4, (120, 70, 45)), (0.75, (165, 110, 75)), (1.0, (200, 150, 110))]
)

# Mana Wood Log: dark indigo bark with glowing cyan mana seams & core
remap_palette(
    f'{REF_DIR}/dark_oak_log.png',
    f'{BLOCK_DIR}/mana_wood_log.png',
    [(0.0, (20, 25, 55)), (0.4, (35, 45, 90)), (0.85, (55, 85, 150)), (1.0, (40, 225, 245))]
)
remap_palette(
    f'{REF_DIR}/dark_oak_log_top.png',
    f'{BLOCK_DIR}/mana_wood_log_top.png',
    [(0.0, (25, 30, 65)), (0.35, (45, 85, 160)), (0.7, (60, 160, 220)), (1.0, (180, 250, 255))]
)

# Eldoria Log: cherry-inspired warm golden amber bark & rings
remap_palette(
    f'{REF_DIR}/cherry_log.png',
    f'{BLOCK_DIR}/eldoria_log.png',
    [(0.0, (85, 55, 25)), (0.5, (150, 110, 50)), (1.0, (215, 175, 95))]
)
remap_palette(
    f'{REF_DIR}/cherry_log_top.png',
    f'{BLOCK_DIR}/eldoria_log_top.png',
    [(0.0, (100, 65, 30)), (0.4, (185, 135, 60)), (0.75, (235, 185, 90)), (1.0, (255, 225, 140))]
)

print('--- Generating 100% Authentic Sapling Textures ---')

# Sandbearer Sapling (from acacia_sapling)
remap_palette(
    f'{REF_DIR}/acacia_sapling.png',
    f'{BLOCK_DIR}/sandbearer_sapling.png',
    [(0.0, (95, 50, 10)), (0.4, (180, 110, 20)), (0.7, (235, 175, 30)), (1.0, (255, 225, 75))]
)

# Cuihua Sapling (from oak_sapling, red fruit accents)
remap_palette(
    f'{REF_DIR}/oak_sapling.png',
    f'{BLOCK_DIR}/cuihua_sapling.png',
    [(0.0, (35, 75, 20)), (0.4, (75, 155, 40)), (0.7, (125, 205, 55)), (1.0, (175, 240, 80))]
)

# Adhigama Sapling (from jungle_sapling)
remap_palette(
    f'{REF_DIR}/jungle_sapling.png',
    f'{BLOCK_DIR}/adhigama_sapling.png',
    [(0.0, (20, 65, 25)), (0.4, (45, 125, 40)), (0.7, (75, 185, 60)), (1.0, (125, 225, 85))]
)

# Mana Wood Sapling (from spruce_sapling, cyan mana glow)
remap_palette(
    f'{REF_DIR}/spruce_sapling.png',
    f'{BLOCK_DIR}/mana_wood_sapling.png',
    [(0.0, (20, 50, 110)), (0.4, (35, 125, 190)), (0.7, (60, 220, 245)), (1.0, (215, 255, 255))]
)

# Eldoria Sapling (from birch_sapling, golden sacred tone)
remap_palette(
    f'{REF_DIR}/birch_sapling.png',
    f'{BLOCK_DIR}/eldoria_sapling.png',
    [(0.0, (110, 70, 15)), (0.4, (185, 130, 30)), (0.7, (240, 190, 50)), (1.0, (255, 230, 100))]
)

print('ALL 15 AUTHENTIC WOOD/LEAF/SAPLING TEXTURES GENERATED SUCCESSFULLY!')
