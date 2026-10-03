# -*- coding: utf-8 -*-
"""
Remastered Mana Animals Generator:
Reads vanilla textures to preserve 100% anatomical correctness and UV precision,
then performs advanced artistic color grading, shading remapping, and hand-crafted
celestial features (runes, horns, tusks, glowing eyes, phoenix feathers, aether fleece).
"""
from PIL import Image, ImageEnhance
import os, math

OUT_DIR = "src/main/resources/assets/awakening_system/textures/entity/animal"
VANILLA_DIR = "scratch/vanilla_textures/assets/minecraft/textures/entity"
os.makedirs(OUT_DIR, exist_ok=True)

def rgb_to_hsv(r, g, b):
    r, g, b = r/255.0, g/255.0, b/255.0
    mx = max(r, g, b)
    mn = min(r, g, b)
    df = mx - mn
    if mx == mn: h = 0
    elif mx == r: h = (60 * ((g - b) / df) + 360) % 360
    elif mx == g: h = (60 * ((b - r) / df) + 120) % 360
    elif mx == b: h = (60 * ((r - g) / df) + 240) % 360
    s = 0 if mx == 0 else (df / mx)
    v = mx
    return h, s, v

def hsv_to_rgb(h, s, v):
    c = v * s
    x = c * (1 - abs((h / 60) % 2 - 1))
    m = v - c
    if 0 <= h < 60: rp, gp, bp = c, x, 0
    elif 60 <= h < 120: rp, gp, bp = x, c, 0
    elif 120 <= h < 180: rp, gp, bp = 0, c, x
    elif 180 <= h < 240: rp, gp, bp = 0, x, c
    elif 240 <= h < 300: rp, gp, bp = x, 0, c
    else: rp, gp, bp = c, 0, x
    return int((rp + m)*255), int((gp + m)*255), int((bp + m)*255)

# -------------------------------------------------------------------------
# 1. MANA COW (Vaca de Maná)
# -------------------------------------------------------------------------
def create_mana_cow():
    vanilla = Image.open(f"{VANILLA_DIR}/cow/cow.png").convert("RGBA")
    w, h = vanilla.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    v_pix = vanilla.load()
    o_pix = out.load()

    for x in range(w):
        for y in range(h):
            r, g, b, a = v_pix[x, y]
            if a == 0:
                continue

            # Check if this pixel is pink udder
            is_udder = (r > 180 and g < 150 and b < 160) or (r > 200 and g > 130 and b > 140)
            # Head box: texOffs(0, 0), size (8, 8, 6) -> spans x in [0..27], y in [0..13]
            is_head_box = (0 <= x < 28 and 0 <= y < 14)

            if is_udder:
                # Bioluminescent Mana Udder: Royal violet to radiant cyan
                lum = r / 255.0
                o_pix[x, y] = (int(30 + lum * 100), int(10 + lum * 50), int(140 + lum * 115), a)
            elif is_head_box:
                # Dedicated hand-crafted shading for head box below
                pass
            else:
                h_deg, s_val, v_val = rgb_to_hsv(r, g, b)
                # Check if white/grey patch on body
                is_white_patch = (s_val < 0.18 and v_val > 0.45)
                if is_white_patch:
                    # Celestial Glowing Cyan / Azure Spot
                    t = (v_val - 0.45) / 0.55
                    cr = int(0 + t * 180)
                    cg = int(180 + t * 75)
                    cb = 255
                    o_pix[x, y] = (cr, cg, cb, a)
                else:
                    # Midnight Cosmic Blue Fur with rich depth
                    base_v = v_val * 1.1
                    cr = int(14 + base_v * 40)
                    cg = int(20 + base_v * 55)
                    cb = int(35 + base_v * 110)
                    o_pix[x, y] = (cr, cg, cb, a)

    # ---------------------------------------------------------------------
    # Hand-painted Cow Head (Entire Head Box x in 0..27, y in 0..13)
    # ---------------------------------------------------------------------
    # 1. Base Midnight Cosmic Fur for entire head
    for hx in range(0, 28):
        for hy in range(0, 14):
            if v_pix[hx, hy][3] > 0:
                lum = v_pix[hx, hy][0] / 255.0
                cr = int(16 + lum * 24)
                cg = int(24 + lum * 32)
                cb = int(48 + lum * 65)
                o_pix[hx, hy] = (cr, cg, cb, 255)

    # 2. Top of Head Crest (x in 6..13, y in 0..5) - Symmetrical celestial star line
    # y=5 connects to forehead y=6
    o_pix[9, 5] = (110, 220, 255, 255)
    o_pix[10, 5] = (110, 220, 255, 255)
    o_pix[9, 4] = (70, 190, 245, 255)
    o_pix[10, 4] = (70, 190, 245, 255)
    o_pix[9, 3] = (40, 150, 210, 255)
    o_pix[10, 3] = (40, 150, 210, 255)

    # 3. Forehead Star Rune (x in 6..13, y in 6..8)
    o_pix[9, 6] = (140, 235, 255, 255)
    o_pix[10, 6] = (140, 235, 255, 255)
    o_pix[8, 7] = (70, 210, 255, 255)
    o_pix[9, 7] = (255, 255, 255, 255)  # Star Core
    o_pix[10, 7] = (255, 255, 255, 255) # Star Core
    o_pix[11, 7] = (70, 210, 255, 255)
    o_pix[9, 8] = (90, 220, 255, 255)
    o_pix[10, 8] = (90, 220, 255, 255)

    # 4. Sleek Brow / Upper Eye Rims (y=8)
    o_pix[6, 8] = (12, 18, 36, 255)
    o_pix[7, 8] = (15, 24, 48, 255)
    o_pix[12, 8] = (15, 24, 48, 255)
    o_pix[13, 8] = (12, 18, 36, 255)

    # 5. Symmetrical Piercing Sapphire & Cyan Eyes (y=9)
    # Right Eye:
    o_pix[6, 9] = (8, 28, 65, 255)      # Deep Sapphire Pupil
    o_pix[7, 9] = (240, 255, 255, 255)  # Specular shine
    o_pix[8, 9] = (25, 55, 100, 255)    # Eyeliner shadow
    # Left Eye:
    o_pix[11, 9] = (25, 55, 100, 255)   # Eyeliner shadow
    o_pix[12, 9] = (240, 255, 255, 255) # Specular shine
    o_pix[13, 9] = (8, 28, 65, 255)     # Deep Sapphire Pupil

    # Soft glowing cyan irises underneath eyes (y=10, outer)
    o_pix[7, 10] = (0, 210, 245, 255)
    o_pix[12, 10] = (0, 210, 245, 255)

    # 6. Cheeks (x=6 and x=13 for y=10..13)
    for cy in range(10, 14):
        o_pix[6, cy] = (22, 30, 58, 255)
        o_pix[13, cy] = (22, 30, 58, 255)

    # 7. Symmetrical Celestial Mana Muzzle (x=7..12, y=11..13)
    # Row 11: Muzzle upper bridge
    o_pix[7, 11] = (52, 48, 72, 255)
    o_pix[8, 11] = (82, 76, 110, 255)
    o_pix[9, 11] = (95, 88, 125, 255)
    o_pix[10, 11] = (95, 88, 125, 255)
    o_pix[11, 11] = (82, 76, 110, 255)
    o_pix[12, 11] = (52, 48, 72, 255)

    # Row 12: Nostrils Row! Perfectly symmetrical at x=8 and x=11
    o_pix[7, 12] = (54, 50, 75, 255)
    o_pix[8, 12] = (16, 12, 24, 255)    # Right Nostril (Dark Obsidian)
    o_pix[9, 12] = (82, 76, 110, 255)   # Muzzle Bridge / Septum
    o_pix[10, 12] = (82, 76, 110, 255)  # Muzzle Bridge / Septum
    o_pix[11, 12] = (16, 12, 24, 255)   # Left Nostril (Dark Obsidian)
    o_pix[12, 12] = (54, 50, 75, 255)

    # Row 13: Lower Chin / Lip
    o_pix[7, 13] = (46, 42, 64, 255)
    o_pix[8, 13] = (68, 62, 88, 255)
    o_pix[9, 13] = (74, 68, 96, 255)
    o_pix[10, 13] = (74, 68, 96, 255)
    o_pix[11, 13] = (68, 62, 88, 255)
    o_pix[12, 13] = (46, 42, 64, 255)

    # 8. Horns (x=22..25, y=0..3) - Obsidian root to glowing jade-cyan tip
    for hx in range(22, 26):
        o_pix[hx, 0] = (235, 255, 255, 255) # Silver-Cyan tip
        o_pix[hx, 1] = (80, 210, 225, 255)  # Jade-Cyan shine
        o_pix[hx, 2] = (32, 65, 75, 255)    # Mid horn
        o_pix[hx, 3] = (18, 34, 42, 255)    # Horn root

    # 9. Hooves (y=30..31 for legs at x=0..15)
    for lx in range(0, 16):
        if v_pix[lx, 31][3] > 0:
            o_pix[lx, 30] = (45, 60, 95, 255)  # Mana rim
            o_pix[lx, 31] = (15, 20, 35, 255)  # Obsidian hoof

    out.save(f"{OUT_DIR}/mana_cow.png")
    print("Saved remastered mana_cow.png")

# -------------------------------------------------------------------------
# 2. MANA PIG (Cerdo de Maná)
# -------------------------------------------------------------------------
def create_mana_pig():
    vanilla = Image.open(f"{VANILLA_DIR}/pig/pig.png").convert("RGBA")
    w, h = vanilla.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    v_pix = vanilla.load()
    o_pix = out.load()

    # 1. Base Anatomical Shading for Pig Body & Head
    for x in range(w):
        for y in range(h):
            r, g, b, a = v_pix[x, y]
            if a == 0:
                continue
            lum = r / 255.0

            is_spine = (54 <= x < 64 and 16 <= y < 32)
            is_belly = (36 <= x < 46 and 16 <= y < 32)
            is_butt  = (46 <= x < 56 and 8 <= y < 16)
            is_chest = (36 <= x < 46 and 8 <= y < 16)
            is_head_top = (8 <= x < 16 and 0 <= y < 8)

            if is_spine or is_head_top:
                t = (lum - 0.4) / 0.6
                cr = int(225 + t * 30)
                cg = int(165 + t * 50)
                cb = int(200 + t * 35)
            elif is_belly:
                t = (lum - 0.4) / 0.6
                cr = int(200 + t * 35)
                cg = int(140 + t * 40)
                cb = int(175 + t * 35)
            elif is_butt or is_chest:
                t = (lum - 0.4) / 0.6
                cr = int(210 + t * 35)
                cg = int(150 + t * 45)
                cb = int(185 + t * 35)
            else:
                t = (lum - 0.4) / 0.6
                cr = int(195 + t * 45)
                cg = int(135 + t * 50)
                cb = int(175 + t * 40)

            o_pix[x, y] = (max(0, min(255, cr)), max(0, min(255, cg)), max(0, min(255, cb)), a)

    # 2. Glorious Flowing Celestial Mana Constellation on the Spine (x in 54..63, y in 16..31)
    for y in range(16, 32):
        o_pix[58, y] = (50, 210, 255, 255)
        o_pix[59, y] = (50, 210, 255, 255)

    star_y_positions = [18, 22, 26, 30]
    for sy in star_y_positions:
        o_pix[58, sy] = (255, 255, 255, 255)
        o_pix[59, sy] = (255, 255, 255, 255)
        o_pix[57, sy] = (0, 235, 255, 255)
        o_pix[60, sy] = (0, 235, 255, 255)
        o_pix[56, sy] = (0, 180, 240, 255)
        o_pix[61, sy] = (0, 180, 240, 255)
        if sy - 1 >= 16:
            o_pix[57, sy - 1] = (100, 230, 255, 255)
            o_pix[60, sy - 1] = (100, 230, 255, 255)
        if sy + 1 < 32:
            o_pix[57, sy + 1] = (100, 230, 255, 255)
            o_pix[60, sy + 1] = (100, 230, 255, 255)

    o_pix[55, 20] = (140, 240, 255, 255)
    o_pix[62, 20] = (140, 240, 255, 255)
    o_pix[55, 28] = (140, 240, 255, 255)
    o_pix[62, 28] = (140, 240, 255, 255)

    # 3. Rear Face / Butt (x in 46..55, y in 8..15)
    for by in range(8, 16):
        o_pix[46, by] = (185, 125, 165, 255)
        o_pix[55, by] = (185, 125, 165, 255)

    o_pix[50, 10] = (255, 195, 230, 255)
    o_pix[51, 10] = (255, 195, 230, 255)
    o_pix[52, 11] = (0, 235, 255, 255)
    o_pix[52, 12] = (0, 235, 255, 255)
    o_pix[51, 12] = (255, 255, 255, 255)
    o_pix[50, 12] = (240, 160, 210, 255)
    o_pix[50, 11] = (240, 160, 210, 255)

    # 4. Flank Rib Runes (Right: 28..35, Left: 46..53, y in 16..31)
    o_pix[32, 20] = (0, 220, 255, 255)
    o_pix[31, 21] = (100, 235, 255, 255)
    o_pix[32, 25] = (0, 220, 255, 255)
    o_pix[31, 26] = (100, 235, 255, 255)
    o_pix[49, 20] = (0, 220, 255, 255)
    o_pix[50, 21] = (100, 235, 255, 255)
    o_pix[49, 25] = (0, 220, 255, 255)
    o_pix[50, 26] = (100, 235, 255, 255)

    # 5. Head Top (x in 8..15, y in 0..7)
    for ty in range(2, 8):
        o_pix[11, ty] = (70, 215, 255, 255)
        o_pix[12, ty] = (70, 215, 255, 255)
    o_pix[11, 6] = (200, 245, 255, 255)
    o_pix[12, 6] = (200, 245, 255, 255)
    o_pix[11, 7] = (255, 255, 255, 255)
    o_pix[12, 7] = (255, 255, 255, 255)

    # 6. Back of Head (x in 24..31, y in 8..15)
    for hy in range(8, 14):
        o_pix[27, hy] = (60, 200, 250, 255)
        o_pix[28, hy] = (60, 200, 250, 255)

    # 7. Front Face (x in 8..15, y in 8..15)
    o_pix[11, 8] = (255, 255, 255, 255)
    o_pix[12, 8] = (255, 255, 255, 255)
    o_pix[10, 8] = (0, 225, 255, 255)
    o_pix[13, 8] = (0, 225, 255, 255)
    o_pix[11, 9] = (0, 225, 255, 255)
    o_pix[12, 9] = (0, 225, 255, 255)

    o_pix[8, 11] = (40, 15, 55, 255)
    o_pix[9, 11] = (0, 240, 255, 255)
    o_pix[9, 10] = (255, 255, 255, 255)
    o_pix[14, 10] = (255, 255, 255, 255)
    o_pix[14, 11] = (0, 240, 255, 255)
    o_pix[15, 11] = (40, 15, 55, 255)

    o_pix[8, 12] = (235, 140, 185, 255)
    o_pix[15, 12] = (235, 140, 185, 255)
    o_pix[8, 13] = (230, 130, 175, 255)
    o_pix[15, 13] = (230, 130, 175, 255)

    # 8. Snout (texOffs 16, 16)
    for sx in range(17, 21):
        o_pix[sx, 16] = (255, 215, 235, 255)
    for sy in range(17, 20):
        o_pix[16, sy] = (205, 135, 175, 255)
        o_pix[21, sy] = (205, 135, 175, 255)

    o_pix[17, 17] = (245, 175, 210, 255)
    o_pix[18, 17] = (255, 235, 245, 255)
    o_pix[19, 17] = (255, 235, 245, 255)
    o_pix[20, 17] = (245, 175, 210, 255)

    o_pix[17, 18] = (75, 20, 65, 255)
    o_pix[18, 18] = (250, 185, 220, 255)
    o_pix[19, 18] = (250, 185, 220, 255)
    o_pix[20, 18] = (75, 20, 65, 255)

    o_pix[17, 19] = (215, 145, 185, 255)
    o_pix[18, 19] = (230, 160, 200, 255)
    o_pix[19, 19] = (230, 160, 200, 255)
    o_pix[20, 19] = (215, 145, 185, 255)

    # 9. Dedicated Textures for 3D Custom Parts (Ears, Tusks, Tail)
    for ex in range(16, 22):
        for ey in range(20, 25):
            if ex in [17, 18] and ey in [21, 22]:
                o_pix[ex, ey] = (255, 160, 215, 255)
            else:
                o_pix[ex, ey] = (240, 180, 215, 255)

    for tx in range(22, 26):
        for ty in range(20, 23):
            if ty == 20:
                o_pix[tx, ty] = (255, 255, 255, 255)
            elif ty == 21:
                o_pix[tx, ty] = (0, 235, 255, 255)
            else:
                o_pix[tx, ty] = (25, 110, 190, 255)

    for tx in range(16, 24):
        for ty in range(25, 29):
            if ty >= 27:
                o_pix[tx, ty] = (0, 235, 255, 255)
            else:
                o_pix[tx, ty] = (245, 185, 220, 255)

    # 10. Hooves on all legs (y=30..31, x in 0..15)
    for lx in range(0, 16):
        if v_pix[lx, 31][3] > 0:
            o_pix[lx, 30] = (70, 220, 255, 255)
            o_pix[lx, 31] = (45, 28, 55, 255)

    out.save(f"{OUT_DIR}/mana_pig.png")
    print("Saved remastered mana_pig.png")

# -------------------------------------------------------------------------
# 3. MANA SHEEP (Oveja de Maná)
# -------------------------------------------------------------------------
def create_mana_sheep():
    # 3.1 Base Sheep Skin (sheep.png)
    v_sheep = Image.open(f"{VANILLA_DIR}/sheep/sheep.png").convert("RGBA")
    w, h = v_sheep.size
    out_s = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    vs_pix = v_sheep.load()
    os_pix = out_s.load()

    for x in range(w):
        for y in range(h):
            r, g, b, a = vs_pix[x, y]
            if a == 0:
                continue
            h_deg, s_val, v_val = rgb_to_hsv(r, g, b)

            # Twilight astral slate
            lum = v_val
            cr = int(25 + lum * 65)
            cg = int(35 + lum * 85)
            cb = int(55 + lum * 120)
            os_pix[x, y] = (cr, cg, cb, a)

    # Hand-painted Eyes at exact sheep UVs (y=10, x=8,9 and x=12,13)
    # Right Eye:
    os_pix[8, 10] = (15, 22, 40, 255)    # Dark Pupil
    os_pix[9, 10] = (0, 235, 255, 255)   # Glowing Cyan Iris
    # Left Eye:
    os_pix[12, 10] = (0, 235, 255, 255)  # Glowing Cyan Iris
    os_pix[13, 10] = (15, 22, 40, 255)   # Dark Pupil

    # Specular glints
    os_pix[9, 9] = (160, 245, 255, 255)
    os_pix[12, 9] = (160, 245, 255, 255)

    # Hooves (y=30..31)
    for lx in range(0, 16):
        if vs_pix[lx, 31][3] > 0:
            os_pix[lx, 30] = (40, 75, 120, 255)
            os_pix[lx, 31] = (15, 20, 32, 255)

    out_s.save(f"{OUT_DIR}/mana_sheep.png")
    print("Saved remastered mana_sheep.png")

    # 3.2 Sheep Fleece / Wool (sheep_fur.png) - Volumetric Celestial Cloud
    v_fur = Image.open(f"{VANILLA_DIR}/sheep/sheep_fur.png").convert("RGBA")
    w_f, h_f = v_fur.size
    out_f = Image.new("RGBA", (w_f, h_f), (0, 0, 0, 0))
    vf_pix = v_fur.load()
    of_pix = out_f.load()

    for x in range(w_f):
        for y in range(h_f):
            r, g, b, a = vf_pix[x, y]
            if a == 0:
                continue

            lum = r / 255.0  # Vanilla fur is greyscale, r represents luminance (0.5..1.0)
            # Re-scale contrast
            norm_lum = max(0.0, min(1.0, (lum - 0.5) / 0.5))

            # Multi-layer Aether Cloud gradient:
            # Deep shadows: Rich navy-indigo (15, 75, 140)
            # Midtone: Vibrant aether cyan (30, 180, 240)
            # Highlights: Glowing starlight (220, 245, 255)
            if norm_lum < 0.4:
                t = norm_lum / 0.4
                cr = int(18 + t * 20)
                cg = int(75 + t * 85)
                cb = int(140 + t * 75)
            elif norm_lum < 0.75:
                t = (norm_lum - 0.4) / 0.35
                cr = int(38 + t * 110)
                cg = int(160 + t * 65)
                cb = int(215 + t * 35)
            else:
                t = (norm_lum - 0.75) / 0.25
                cr = int(148 + t * 105)
                cg = int(225 + t * 30)
                cb = int(250 + t * 5)

            of_pix[x, y] = (cr, cg, cb, a)

    # Add sparkling mana star dust on fleece
    stars = [(33, 11), (39, 15), (46, 12), (51, 16), (8, 4), (12, 3)]
    for sx, sy in stars:
        if sx < w_f and sy < h_f and vf_pix[sx, sy][3] > 0:
            of_pix[sx, sy] = (255, 255, 255, 255)
            if sx+1 < w_f and vf_pix[sx+1, sy][3] > 0:
                of_pix[sx+1, sy] = (190, 245, 255, 255)

    out_f.save(f"{OUT_DIR}/mana_sheep_fur.png")
    print("Saved remastered mana_sheep_fur.png")

# -------------------------------------------------------------------------
# 4. MANA CHICKEN (Gallina de Maná)
# -------------------------------------------------------------------------
def create_mana_chicken():
    vanilla = Image.open(f"{VANILLA_DIR}/chicken.png").convert("RGBA")
    w, h = vanilla.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    v_pix = vanilla.load()
    o_pix = out.load()

    for x in range(w):
        for y in range(h):
            r, g, b, a = v_pix[x, y]
            if a == 0:
                continue

            lum = r / 255.0

            # Exact anatomical box bounds from ChickenModel:
            is_beak = (14 <= x < 26 and 0 <= y < 4)
            is_wattle = (14 <= x < 22 and 4 <= y < 8)
            is_legs = (26 <= x < 38 and 0 <= y < 11)
            is_wings = (24 <= x < 38 and 13 <= y < 23)

            if is_beak:
                # Polished Celestial Sun-Gold Beak
                cr = int(180 + lum * 75)
                cg = int(120 + lum * 90)
                cb = int(25 + lum * 45)
                o_pix[x, y] = (cr, cg, cb, a)
            elif is_wattle:
                # Radiant Celestial Royal Amethyst / Mana Gem Wattle
                cr = int(185 + lum * 65)
                cg = int(35 + lum * 40)
                cb = int(210 + lum * 45)
                o_pix[x, y] = (cr, cg, cb, a)
            elif is_legs:
                if y >= 9:
                    # Sharp dark obsidian talons
                    o_pix[x, y] = (45, 30, 22, a)
                else:
                    # Scaled golden amber legs
                    cr = int(190 + lum * 65)
                    cg = int(130 + lum * 80)
                    cb = int(25 + lum * 40)
                    o_pix[x, y] = (cr, cg, cb, a)
            elif is_wings:
                # Tiered Spirit Wings: Moonlight silver base -> Aether Azure -> Radiant Mana Cyan flight tips
                prog = (y - 13) / 9.0
                if prog > 0.7:
                    # Wing flight tips: Radiant Cyan
                    cr = int(20 + lum * 140)
                    cg = int(210 + lum * 45)
                    cb = 255
                elif prog > 0.35:
                    # Mid wing: Aether Azure
                    cr = int(120 + lum * 100)
                    cg = int(190 + lum * 55)
                    cb = int(235 + lum * 20)
                else:
                    # Shoulder: Pearlescent moonlight silver
                    cr = int(215 + lum * 40)
                    cg = int(230 + lum * 25)
                    cb = int(245 + lum * 10)
                o_pix[x, y] = (cr, cg, cb, a)
            else:
                # Body & Head: Ethereal Moonlight Silver-White Plumage with soft aether-blue shading
                norm_lum = max(0.0, min(1.0, (lum - 0.7) / 0.3))
                cr = int(175 + norm_lum * 75)
                cg = int(198 + norm_lum * 55)
                cb = int(232 + norm_lum * 23)
                o_pix[x, y] = (cr, cg, cb, a)

    # Hand-painted Spirit Features:
    # 1. Eyes: Symmetrical Glowing Cyan with Starlight Specular Glint
    o_pix[3, 3] = (255, 255, 255, 255) # Glint
    o_pix[3, 4] = (0, 235, 255, 255)   # Glowing Cyan Iris
    o_pix[2, 4] = (60, 190, 245, 255)  # Eyeliner sheen

    o_pix[6, 3] = (255, 255, 255, 255) # Glint
    o_pix[6, 4] = (0, 235, 255, 255)   # Glowing Cyan Iris
    o_pix[7, 4] = (60, 190, 245, 255)  # Eyeliner sheen

    # 2. Golden & Amethyst Comb Crest on top of head (y=0..1, x=4..5)
    o_pix[4, 0] = (255, 215, 60, 255)
    o_pix[5, 0] = (255, 215, 60, 255)
    o_pix[4, 1] = (220, 60, 235, 255)  # Radiant Mana Amethyst comb center
    o_pix[5, 1] = (220, 60, 235, 255)

    # 3. Chest mana feather crest
    for cx in range(5, 9):
        o_pix[cx, 16] = (0, 225, 255, 255)
        o_pix[cx, 19] = (255, 215, 75, 255)

    # 4. Tail plume stardust sparkles (x in 19..22, y in 18..21)
    o_pix[20, 19] = (0, 240, 255, 255)
    o_pix[21, 20] = (0, 240, 255, 255)

    out.save(f"{OUT_DIR}/mana_chicken.png")
    print("Saved remastered mana_chicken.png")

if __name__ == "__main__":
    create_mana_cow()
    create_mana_pig()
    create_mana_sheep()
    create_mana_chicken()
    print("All Remastered Mana Animals created successfully!")
