import os
import math
from PIL import Image, ImageDraw, ImageFilter

OUT_DIR = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\textures\item"
os.makedirs(OUT_DIR, exist_ok=True)

def new_canvas(size=128):
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))

def finalize(img, filename):
    small = img.resize((32, 32), Image.Resampling.LANCZOS)
    pix = small.load()
    w, h = small.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = pix[x, y]
            if a < 110:
                pix[x, y] = (0, 0, 0, 0)
            else:
                pix[x, y] = (r, g, b, 255)
    dest = os.path.join(OUT_DIR, filename)
    small.save(dest)
    print(f"Saved detailed {filename}")

# =========================================================================
# 1. PIEDRAS DE RUNA (128x128 -> 32x32)
# =========================================================================
def draw_detailed_rune(stone_dark, stone_mid, stone_light, stone_rim, rune_glow, rune_core, corner_metal, rune_symbol_type):
    base = new_canvas(128)
    d = ImageDraw.Draw(base)

    poly = [(64, 8), (112, 32), (112, 96), (64, 120), (16, 96), (16, 32)]
    d.polygon(poly, fill=(15, 18, 25, 255))

    d.polygon([(64, 14), (106, 36), (64, 48), (22, 36)], fill=stone_light)
    d.polygon([(22, 36), (64, 48), (64, 102), (22, 92)], fill=stone_mid)
    d.polygon([(64, 48), (106, 36), (106, 92), (64, 102)], fill=stone_dark)
    d.polygon([(22, 92), (64, 102), (64, 114), (18, 94)], fill=(12, 14, 20, 255))
    d.polygon([(64, 102), (106, 92), (108, 94), (64, 114)], fill=(8, 10, 15, 255))

    d.line([(64, 14), (106, 36)], fill=stone_rim, width=2)
    d.line([(22, 36), (64, 14)], fill=stone_rim, width=3)
    d.line([(22, 36), (64, 48)], fill=stone_rim, width=2)
    d.line([(64, 48), (106, 36)], fill=stone_mid, width=2)
    d.line([(22, 36), (22, 92)], fill=stone_mid, width=2)
    d.line([(106, 36), (106, 92)], fill=(15, 18, 25, 255), width=2)

    d.line([(28, 42), (40, 56), (36, 68), (48, 76)], fill=(12, 14, 20, 255), width=2)
    d.line([(100, 42), (88, 54), (92, 66)], fill=(10, 12, 18, 255), width=1)
    d.line([(64, 48), (64, 102)], fill=(stone_dark[0]//2, stone_dark[1]//2, stone_dark[2]//2, 255), width=2)

    corners = [(64, 11), (110, 34), (110, 94), (64, 117), (18, 94), (18, 34)]
    for cx, cy in corners:
        d.ellipse([cx - 7, cy - 7, cx + 7, cy + 7], fill=corner_metal[0], outline=corner_metal[1], width=2)
        d.ellipse([cx - 4, cy - 4, cx + 4, cy + 4], fill=corner_metal[2])
        d.point([(cx - 1, cy - 1)], fill=(255, 255, 255, 255))

    glow = new_canvas(128)
    dg = ImageDraw.Draw(glow)
    for r in range(36, 8, -4):
        alpha = int((36 - r) * 4)
        dg.ellipse([64 - r, 64 - r, 64 + r, 64 + r], fill=(rune_glow[0], rune_glow[1], rune_glow[2], min(255, alpha)))

    comp = Image.alpha_composite(base, glow)
    d = ImageDraw.Draw(comp)

    if rune_symbol_type == "rulers_authority":
        d.polygon([(64, 34), (88, 56), (76, 56), (76, 88), (52, 88), (52, 56), (40, 56)], fill=(8, 20, 36, 255))
        d.polygon([(64, 38), (84, 56), (72, 56), (72, 84), (56, 84), (56, 56), (44, 56)], fill=rune_glow)
        d.polygon([(64, 42), (76, 56), (68, 56), (68, 80), (60, 80), (60, 56), (52, 56)], fill=rune_core)
        d.line([(64, 40), (64, 82)], fill=(255, 255, 255, 255), width=4)
        for jx, jy in [(64, 28), (40, 50), (88, 50)]:
            d.ellipse([jx - 4, jy - 4, jx + 4, jy + 4], fill=rune_core, outline=(255, 255, 255, 255))

    elif rune_symbol_type == "dragon_eyes":
        d.ellipse([38, 48, 90, 80], fill=(30, 8, 8, 255), outline=rune_glow, width=3)
        d.ellipse([42, 52, 86, 76], fill=rune_glow)
        d.ellipse([46, 55, 82, 73], fill=(255, 220, 60, 255))
        d.polygon([(64, 46), (70, 64), (64, 82), (58, 64)], fill=(20, 4, 4, 255))
        d.ellipse([58, 56, 62, 60], fill=(255, 255, 255, 255))
        d.ellipse([67, 68, 70, 71], fill=(255, 255, 255, 255))
        d.line([(38, 64), (24, 64), (18, 56)], fill=(255, 140, 20, 255), width=3)
        d.line([(90, 64), (104, 64), (110, 72)], fill=(255, 140, 20, 255), width=3)

    elif rune_symbol_type == "bloodthirst":
        claws = [(46, 44, 52, 86), (64, 38, 64, 90), (82, 44, 76, 86)]
        for sx, sy, ex, ey in claws:
            d.line([(sx, sy), (ex, ey)], fill=(50, 4, 12, 255), width=8)
            d.line([(sx, sy), (ex, ey)], fill=rune_glow, width=5)
            d.line([(sx, sy), (ex, ey)], fill=rune_core, width=2)
        d.ellipse([50, 90, 56, 96], fill=rune_core)
        d.ellipse([61, 94, 67, 100], fill=rune_core)
        d.ellipse([72, 90, 78, 96], fill=rune_core)

    elif rune_symbol_type == "shadow_step":
        d.ellipse([42, 42, 86, 86], outline=rune_glow, width=4)
        d.ellipse([48, 48, 80, 80], outline=rune_core, width=2)
        d.line([(44, 44), (84, 84)], fill=(20, 8, 35, 255), width=7)
        d.line([(44, 44), (84, 84)], fill=(255, 255, 255, 255), width=2)
        d.line([(84, 44), (44, 84)], fill=(20, 8, 35, 255), width=7)
        d.line([(84, 44), (44, 84)], fill=(255, 255, 255, 255), width=2)
        d.ellipse([60, 60, 68, 68], fill=(255, 255, 255, 255))

    elif rune_symbol_type == "fourth_wall":
        d.rectangle([42, 42, 86, 86], outline=(15, 30, 50, 255), width=5)
        d.rectangle([46, 46, 82, 82], outline=rune_glow, width=3)
        d.rectangle([52, 52, 76, 76], fill=(160, 235, 255, 140), outline=rune_core, width=2)
        d.line([(46, 64), (82, 64)], fill=(255, 255, 255, 255), width=2)
        d.line([(64, 46), (64, 82)], fill=(255, 255, 255, 255), width=2)
        d.line([(46, 46), (82, 82)], fill=rune_core, width=2)
        d.line([(82, 46), (46, 82)], fill=rune_core, width=2)
        d.ellipse([60, 60, 68, 68], fill=(255, 255, 255, 255))

    elif rune_symbol_type == "iron_body":
        d.polygon([(64, 38), (88, 46), (84, 76), (64, 92), (44, 76), (40, 46)], fill=(18, 22, 28, 255))
        d.polygon([(64, 42), (84, 49), (80, 73), (64, 87), (48, 73), (44, 49)], fill=rune_glow)
        d.polygon([(64, 46), (78, 52), (76, 70), (64, 82), (52, 70), (50, 52)], fill=rune_core)
        d.line([(64, 46), (64, 82)], fill=(255, 255, 255, 255), width=4)
        d.line([(52, 60), (76, 60)], fill=(255, 255, 255, 255), width=4)
        d.ellipse([61, 57, 67, 63], fill=(255, 255, 255, 255))

    return comp

def gen_all_runes():
    gold_corners = ((180, 130, 20, 255), (90, 65, 10, 255), (255, 220, 90, 255))
    dark_corners = ((60, 65, 75, 255), (25, 28, 35, 255), (160, 175, 195, 255))
    silver_corners = ((160, 170, 185, 255), (70, 75, 85, 255), (240, 245, 255, 255))

    finalize(draw_detailed_rune((25, 38, 65, 255), (45, 68, 115, 255), (85, 120, 190, 255), (140, 180, 250, 255),
                               (0, 210, 255), (180, 245, 255, 255), gold_corners, "rulers_authority"), "rune_stone_rulers_authority.png")
    finalize(draw_detailed_rune((45, 18, 18, 255), (85, 32, 32, 255), (145, 50, 50, 255), (210, 80, 80, 255),
                               (255, 70, 20), (255, 200, 60, 255), gold_corners, "dragon_eyes"), "rune_stone_dragon_eyes.png")
    finalize(draw_detailed_rune((40, 12, 25, 255), (80, 22, 45, 255), (140, 35, 75, 255), (210, 60, 115, 255),
                               (255, 20, 70), (255, 140, 175, 255), silver_corners, "bloodthirst"), "rune_stone_bloodthirst.png")
    finalize(draw_detailed_rune((25, 15, 45, 255), (55, 30, 90, 255), (105, 55, 160, 255), (170, 95, 245, 255),
                               (165, 60, 255), (235, 180, 255, 255), silver_corners, "shadow_step"), "rune_stone_shadow_step.png")
    finalize(draw_detailed_rune((35, 50, 65, 255), (65, 95, 120, 255), (125, 170, 205, 255), (195, 230, 255, 255),
                               (0, 220, 255), (220, 250, 255, 255), silver_corners, "fourth_wall"), "rune_stone_fourth_wall.png")
    finalize(draw_detailed_rune((35, 40, 48, 255), (65, 75, 88, 255), (115, 130, 150, 255), (180, 195, 215, 255),
                               (160, 200, 255), (245, 250, 255, 255), dark_corners, "iron_body"), "rune_stone_iron_body.png")

# =========================================================================
# 2. RELIQUIAS Y ACCESORIOS (128x128 -> 32x32)
# =========================================================================
def gen_all_relics():
    # 1. demon_monarch_ring
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.ellipse([24, 46, 104, 114], fill=(15, 10, 22, 255), outline=(10, 8, 15, 255), width=6)
    d.ellipse([26, 48, 102, 112], outline=(60, 48, 75, 255), width=14)
    d.ellipse([32, 54, 96, 106], outline=(140, 120, 170, 255), width=6)
    d.ellipse([35, 57, 93, 103], outline=(200, 185, 225, 255), width=2)
    d.ellipse([42, 64, 86, 96], fill=(0, 0, 0, 0))

    d.polygon([(16, 26), (36, 16), (46, 36), (36, 50), (22, 42)], fill=(25, 18, 32, 255), outline=(10, 5, 15, 255))
    d.polygon([(112, 26), (92, 16), (82, 36), (92, 50), (106, 42)], fill=(25, 18, 32, 255), outline=(10, 5, 15, 255))
    d.line([(22, 28), (40, 42)], fill=(90, 75, 110, 255), width=3)
    d.line([(106, 28), (88, 42)], fill=(90, 75, 110, 255), width=3)
    d.polygon([(40, 24), (88, 24), (80, 58), (48, 58)], fill=(35, 25, 45, 255), outline=(15, 10, 20, 255))

    d.ellipse([42, 20, 86, 56], fill=(90, 10, 20, 255), outline=(50, 5, 10, 255), width=2)
    d.polygon([(64, 22), (84, 34), (64, 54), (44, 34)], fill=(225, 25, 45, 255))
    d.polygon([(64, 24), (80, 34), (64, 44), (48, 34)], fill=(255, 75, 95, 255))
    d.polygon([(64, 26), (72, 34), (64, 40), (56, 34)], fill=(255, 160, 175, 255))
    d.ellipse([54, 26, 62, 32], fill=(255, 255, 255, 255))
    d.point([(70, 42)], fill=(255, 200, 210, 255))
    finalize(img, "demon_monarch_ring.png")

    # 2. asrahan_mana_chalice
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.arc([16, 28, 46, 72], start=120, end=300, fill=(140, 100, 15, 255), width=8)
    d.arc([18, 30, 44, 70], start=120, end=300, fill=(255, 225, 80, 255), width=4)
    d.arc([82, 28, 112, 72], start=240, end=420, fill=(140, 100, 15, 255), width=8)
    d.arc([84, 30, 110, 70], start=240, end=420, fill=(255, 225, 80, 255), width=4)

    d.polygon([(34, 24), (94, 24), (82, 68), (46, 68)], fill=(218, 165, 32, 255), outline=(90, 65, 10, 255), width=3)
    d.polygon([(36, 26), (64, 26), (56, 66), (48, 66)], fill=(255, 235, 120, 255))
    d.polygon([(64, 26), (92, 26), (80, 66), (56, 66)], fill=(180, 130, 20, 255))
    d.line([(34, 24), (94, 24)], fill=(255, 250, 180, 255), width=5)
    d.ellipse([50, 44, 58, 52], fill=(0, 210, 255, 255), outline=(0, 60, 120, 255))
    d.ellipse([70, 44, 78, 52], fill=(240, 30, 50, 255), outline=(100, 10, 20, 255))

    d.ellipse([38, 20, 90, 32], fill=(30, 10, 75, 255), outline=(110, 80, 15, 255))
    d.ellipse([42, 22, 86, 30], fill=(0, 220, 255, 255))
    d.ellipse([48, 24, 80, 28], fill=(210, 250, 255, 255))
    d.ellipse([52, 10, 58, 16], fill=(0, 230, 255, 220))
    d.ellipse([68, 6, 74, 12], fill=(180, 110, 255, 220))

    d.rectangle([(58, 68), (70, 96)], fill=(218, 165, 32, 255), outline=(90, 65, 10, 255), width=2)
    d.line([(60, 70), (60, 94)], fill=(255, 235, 110, 255), width=3)
    d.ellipse([54, 76, 74, 88], fill=(255, 220, 65, 255), outline=(90, 65, 10, 255), width=2)
    d.ellipse([61, 80, 67, 85], fill=(0, 220, 255, 255))
    d.polygon([(36, 96), (92, 96), (102, 112), (26, 112)], fill=(218, 165, 32, 255), outline=(90, 65, 10, 255), width=3)
    d.line([(30, 108), (98, 108)], fill=(255, 245, 160, 255), width=3)
    finalize(img, "asrahan_mana_chalice.png")

    # 3. king_slayer_necklace
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    for ang in range(25, 156, 10):
        rad = math.radians(ang)
        cx = int(64 + 44 * math.cos(rad))
        cy = int(22 + 36 * math.sin(rad))
        d.ellipse([cx - 7, cy - 5, cx + 7, cy + 5], fill=(195, 205, 220, 255), outline=(60, 65, 80, 255), width=2)
        d.ellipse([cx - 4, cy - 2, cx + 4, cy + 2], fill=(255, 255, 255, 255))

    d.polygon([(46, 44), (82, 44), (78, 64), (64, 72), (50, 64)], fill=(218, 165, 32, 255), outline=(100, 70, 10, 255), width=2)
    d.line([(48, 46), (80, 46)], fill=(255, 240, 140, 255), width=3)
    d.ellipse([53, 50, 59, 56], fill=(140, 20, 30, 255))
    d.ellipse([69, 50, 75, 56], fill=(140, 20, 30, 255))

    # Thick curved predator fang
    fang = [(50, 64), (78, 64), (82, 88), (72, 116), (56, 120), (52, 90)]
    d.polygon(fang, fill=(245, 240, 230, 255), outline=(85, 80, 70, 255), width=3)
    d.line([(56, 66), (62, 116)], fill=(255, 255, 255, 255), width=6)
    d.line([(74, 66), (74, 98)], fill=(205, 195, 175, 255), width=5)
    d.line([(80, 84), (72, 88)], fill=(80, 70, 60, 255), width=2)
    d.polygon([(56, 104), (64, 118), (70, 104)], fill=(185, 15, 30, 255))
    d.ellipse([60, 114, 68, 122], fill=(220, 20, 40, 255))
    finalize(img, "king_slayer_necklace.png")

    # 4. phoenix_feather
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.line([(32, 100), (98, 26)], fill=(255, 255, 255, 255), width=5)
    d.line([(30, 102), (96, 28)], fill=(255, 230, 120, 255), width=8)

    for i in range(24):
        t = i / 24.0
        x = int(34 + t * 62)
        y = int(98 - t * 70)
        barb_len = int(math.sin(t * math.pi) * 32 + 6)
        col_fire = (int(255), int(40 + t * 180), int(10 + t * 50), 255)
        d.line([(x, y), (x - barb_len, y - int(barb_len * 0.4))], fill=col_fire, width=4)
        d.line([(x, y), (x - int(barb_len * 0.6), y - int(barb_len * 0.2))], fill=(255, 240, 160, 255), width=2)
        d.line([(x, y), (x + int(barb_len * 0.7), y + int(barb_len * 0.5))], fill=col_fire, width=4)
        d.line([(x, y), (x + int(barb_len * 0.4), y + int(barb_len * 0.3))], fill=(255, 210, 80, 255), width=2)

    d.ellipse([94, 22, 106, 32], fill=(255, 255, 255, 255))
    for ex, ey in [(84, 16), (104, 18), (110, 36), (72, 28), (48, 68), (28, 88)]:
        d.ellipse([ex - 3, ey - 3, ex + 3, ey + 3], fill=(255, 220, 60, 255))
        d.point([(ex, ey)], fill=(255, 255, 255, 255))
    finalize(img, "phoenix_feather.png")

    # 5. elemental_demon_crown
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.polygon([(14, 80), (18, 36), (34, 16), (42, 38), (32, 70)], fill=(30, 22, 40, 255), outline=(12, 8, 18, 255), width=3)
    d.polygon([(114, 80), (110, 36), (94, 16), (86, 38), (96, 70)], fill=(30, 22, 40, 255), outline=(12, 8, 18, 255), width=3)
    d.line([(24, 34), (32, 54)], fill=(85, 65, 105, 255), width=3)
    d.line([(104, 34), (96, 54)], fill=(85, 65, 105, 255), width=3)

    crown_pts = [(26, 74), (36, 44), (48, 66), (64, 26), (80, 66), (92, 44), (102, 74), (96, 96), (32, 96)]
    d.polygon(crown_pts, fill=(38, 28, 48, 255), outline=(14, 10, 20, 255), width=4)
    d.polygon([(28, 74), (36, 48), (48, 68), (64, 32), (80, 68), (92, 48), (100, 74), (94, 92), (34, 92)], fill=(65, 48, 82, 255))
    d.line([(32, 90), (96, 90)], fill=(150, 125, 185, 255), width=3)

    gems = [
        (36, 78, (255, 45, 45, 255), (140, 10, 10, 255)),
        (50, 76, (0, 220, 255, 255), (0, 80, 140, 255)),
        (64, 68, (255, 220, 40, 255), (150, 110, 10, 255)),
        (78, 76, (185, 55, 255, 255), (90, 15, 150, 255)),
        (92, 78, (45, 230, 130, 255), (15, 100, 45, 255))
    ]
    for gx, gy, c_lit, c_drk in gems:
        d.ellipse([gx - 8, gy - 8, gx + 8, gy + 8], fill=(218, 165, 32, 255), outline=(100, 70, 10, 255), width=2)
        d.polygon([(gx, gy - 6), (gx + 6, gy), (gx, gy + 6), (gx - 6, gy)], fill=c_lit, outline=c_drk, width=1)
        d.polygon([(gx, gy - 4), (gx + 4, gy), (gx, gy + 2), (gx - 3, gy)], fill=(255, 255, 255, 220))
        d.point([(gx, gy - 2)], fill=(255, 255, 255, 255))

    d.line([(36, 78), (50, 76), (64, 68), (78, 76), (92, 78)], fill=(255, 255, 255, 180), width=2)
    finalize(img, "elemental_demon_crown.png")

# =========================================================================
# 3. PÍLDORAS, ALQUIMIA Y HIERBAS MURIM (128x128 -> 32x32)
# =========================================================================
def draw_detailed_pill(glow_col, shell_dark, shell_mid, shell_light, swirl_col, filename):
    base = new_canvas(128)
    d = ImageDraw.Draw(base)

    for r in range(54, 38, -3):
        alpha = int((54 - r) * 4)
        d.ellipse([64 - r, 64 - r, 64 + r, 64 + r], outline=(glow_col[0], glow_col[1], glow_col[2], min(255, alpha)), width=2)

    cx, cy = 64, 64
    d.ellipse([cx - 36, cy - 36, cx + 36, cy + 36], fill=shell_dark, outline=(15, 15, 20, 255), width=4)
    d.ellipse([cx - 32, cy - 32, cx + 30, cy + 30], fill=shell_mid)
    d.ellipse([cx - 28, cy - 28, cx + 18, cy + 18], fill=shell_light)

    d.arc([cx - 26, cy - 24, cx + 26, cy + 24], start=30, end=210, fill=swirl_col, width=6)
    d.arc([cx - 18, cy - 16, cx + 18, cy + 16], start=210, end=390, fill=(255, 255, 255, 255), width=4)
    d.ellipse([cx - 10, cy - 10, cx + 10, cy + 10], fill=swirl_col)
    d.ellipse([cx - 4, cy - 4, cx + 4, cy + 4], fill=(255, 255, 255, 255))

    d.ellipse([cx - 20, cy - 20, cx - 8, cy - 8], fill=(255, 255, 255, 255))
    d.point([(cx + 12, cy - 14), (cx - 14, cy + 16), (cx + 18, cy + 12)], fill=(255, 255, 220, 255))

    finalize(base, filename)

def gen_all_murim():
    draw_detailed_pill((255, 100, 30), (120, 20, 10, 255), (220, 45, 20, 255), (255, 140, 60, 255), (255, 220, 90, 255), "rising_dragon_pill.png")
    draw_detailed_pill((30, 220, 120), (15, 75, 40, 255), (25, 160, 85, 255), (75, 230, 145, 255), (200, 255, 230, 255), "marrow_cleansing_pill.png")
    draw_detailed_pill((170, 60, 255), (60, 15, 100, 255), (115, 35, 195, 255), (185, 95, 255, 255), (240, 195, 255, 255), "soul_transformation_pill.png")
    draw_detailed_pill((255, 215, 0), (130, 85, 10, 255), (218, 155, 20, 255), (255, 230, 80, 255), (255, 255, 220, 255), "realm_breakthrough_pill.png")
    draw_detailed_pill((20, 120, 200), (10, 45, 85, 255), (20, 90, 160, 255), (60, 165, 240, 255), (180, 235, 255, 255), "black_tortoise_pill.png")

    # wu_xing_manual
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.rectangle([24, 18, 104, 110], fill=(22, 38, 75, 255), outline=(12, 20, 42, 255), width=4)
    d.rectangle([100, 22, 104, 106], fill=(240, 230, 205, 255))
    d.line([(102, 22), (102, 106)], fill=(180, 165, 135, 255), width=1)
    corners = [(24, 18), (104, 18), (24, 110), (104, 110)]
    for cx, cy in corners:
        d.polygon([(cx, cy), (cx + (14 if cx < 64 else -14), cy), (cx, cy + (14 if cy < 64 else -14))], fill=(218, 165, 32, 255), outline=(110, 80, 15, 255))

    d.line([(36, 18), (36, 110)], fill=(218, 165, 32, 255), width=3)
    for hy in [30, 52, 76, 98]:
        d.ellipse([33, hy - 3, 39, hy + 3], fill=(12, 20, 42, 255))
        d.line([(24, hy), (42, hy)], fill=(255, 250, 235, 255), width=3)

    d.rectangle([50, 28, 68, 92], fill=(248, 244, 232, 255), outline=(140, 30, 25, 255), width=2)
    d.line([(59, 36), (59, 84)], fill=(170, 25, 25, 255), width=4)
    d.line([(54, 46), (64, 46)], fill=(170, 25, 25, 255), width=3)
    d.line([(54, 62), (64, 62)], fill=(170, 25, 25, 255), width=3)

    d.ellipse([74, 52, 94, 72], fill=(218, 165, 32, 255), outline=(110, 80, 15, 255), width=2)
    d.ellipse([78, 56, 90, 68], fill=(255, 235, 120, 255))
    finalize(img, "wu_xing_manual.png")

    # Talismans
    for fn, is_asc in [("murim_ascension_talisman.png", True), ("taoist_talisman.png", False)]:
        img = new_canvas(128)
        d = ImageDraw.Draw(img)
        paper_bg = (255, 225, 80, 255) if is_asc else (245, 215, 75, 255)
        d.polygon([(36, 14), (92, 14), (90, 114), (34, 114)], fill=paper_bg, outline=(140, 105, 20, 255), width=3)
        d.line([(36, 38), (92, 38)], fill=(225, 195, 65, 255), width=2)
        d.line([(35, 76), (91, 76)], fill=(225, 195, 65, 255), width=2)

        red_ink = (195, 20, 20, 255)
        d.line([(64, 22), (64, 100)], fill=red_ink, width=5)
        d.line([(48, 30), (80, 30)], fill=red_ink, width=4)
        d.line([(50, 48), (78, 48)], fill=red_ink, width=5)
        d.line([(46, 68), (82, 68)], fill=red_ink, width=4)
        d.rectangle([54, 82, 74, 96], fill=red_ink)
        d.point([(58, 86), (70, 92)], fill=(255, 220, 80, 255))

        if is_asc:
            for fx, fy in [(36, 14), (92, 14), (34, 114), (90, 114), (64, 8), (28, 54), (98, 68)]:
                d.ellipse([fx - 4, fy - 4, fx + 4, fy + 4], fill=(255, 90, 20, 255))
        else:
            d.line([(64, 114), (64, 124)], fill=(195, 20, 20, 255), width=4)
            d.ellipse([60, 112, 68, 118], fill=(70, 215, 120, 255))
        finalize(img, fn)

    # thousand_year_ginseng
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    for r in range(54, 30, -4):
        d.ellipse([64 - r, 64 - r, 64 + r, 64 + r], outline=(255, 215, 60, int((54 - r) * 3)), width=2)

    d.ellipse([54, 38, 74, 58], fill=(225, 180, 105, 255), outline=(110, 75, 25, 255), width=3)
    d.polygon([(52, 54), (76, 54), (80, 86), (48, 86)], fill=(215, 170, 95, 255), outline=(110, 75, 25, 255), width=2)
    d.line([(54, 62), (74, 64)], fill=(150, 105, 45, 255), width=2)
    d.line([(52, 74), (76, 76)], fill=(150, 105, 45, 255), width=2)
    d.line([(52, 86), (38, 114)], fill=(210, 160, 85, 255), width=6)
    d.line([(76, 86), (90, 114)], fill=(210, 160, 85, 255), width=6)
    for sx, sy, ex, ey in [(48, 70, 26, 92), (80, 70, 102, 92), (54, 98, 42, 122), (74, 98, 86, 122), (64, 88, 64, 124)]:
        d.line([(sx, sy), (ex, ey)], fill=(230, 195, 130, 255), width=2)

    d.line([(64, 38), (64, 18)], fill=(50, 130, 35, 255), width=4)
    d.ellipse([44, 8, 62, 24], fill=(65, 165, 45, 255), outline=(30, 85, 20, 255), width=2)
    d.ellipse([66, 8, 84, 24], fill=(65, 165, 45, 255), outline=(30, 85, 20, 255), width=2)
    d.ellipse([54, 4, 74, 18], fill=(85, 195, 55, 255), outline=(30, 85, 20, 255), width=2)
    for bx, by in [(60, 18), (68, 18), (64, 24), (58, 26), (70, 26)]:
        d.ellipse([bx - 3, by - 3, bx + 3, by + 3], fill=(225, 25, 25, 255), outline=(120, 10, 10, 255))
        d.point([(bx - 1, by - 1)], fill=(255, 255, 255, 255))
    finalize(img, "thousand_year_ginseng.png")

    # wild_ginseng
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.line([(64, 42), (64, 96)], fill=(220, 180, 110, 255), width=8)
    d.line([(64, 96), (54, 118)], fill=(200, 160, 90, 255), width=4)
    d.line([(64, 96), (74, 118)], fill=(200, 160, 90, 255), width=4)
    d.line([(62, 60), (44, 76)], fill=(215, 175, 105, 255), width=2)
    d.line([(66, 70), (84, 86)], fill=(215, 175, 105, 255), width=2)
    d.line([(64, 42), (64, 22)], fill=(55, 140, 40, 255), width=3)
    d.ellipse([46, 12, 62, 26], fill=(70, 170, 50, 255), outline=(30, 85, 20, 255))
    d.ellipse([66, 12, 82, 26], fill=(70, 170, 50, 255), outline=(30, 85, 20, 255), width=1)
    d.ellipse([56, 8, 72, 20], fill=(90, 200, 60, 255))
    d.ellipse([62, 24, 66, 28], fill=(220, 25, 25, 255))
    finalize(img, "wild_ginseng.png")

    # snow_lotus (Cupped blooming water lily of pure ice)
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    sepals = [
        [(64, 96), (24, 78), (34, 62), (64, 82)],
        [(64, 96), (104, 78), (94, 62), (64, 82)],
        [(64, 98), (14, 90), (32, 104), (64, 102)],
        [(64, 98), (114, 90), (96, 104), (64, 102)],
        [(64, 100), (44, 114), (64, 118), (84, 114)]
    ]
    for poly in sepals:
        d.polygon(poly, fill=(130, 215, 225, 255), outline=(50, 135, 155, 255), width=2)

    mid_petals = [
        [(64, 86), (32, 54), (46, 32), (60, 68)],
        [(64, 86), (96, 54), (82, 32), (68, 68)],
        [(64, 86), (46, 44), (64, 26), (64, 76)],
        [(64, 86), (82, 44), (64, 26), (64, 76)],
    ]
    for poly in mid_petals:
        d.polygon(poly, fill=(230, 248, 255, 255), outline=(100, 195, 235, 255), width=2)
        d.line([(poly[0][0], poly[0][1]), (poly[2][0], poly[2][1])], fill=(255, 255, 255, 255), width=2)

    d.ellipse([52, 62, 76, 86], fill=(255, 220, 70, 255), outline=(0, 220, 255, 255), width=3)
    d.ellipse([56, 66, 72, 82], fill=(255, 255, 255, 255))
    for fx, fy in [(28, 36), (100, 36), (64, 16), (20, 68), (108, 68)]:
        d.ellipse([fx - 2, fy - 2, fx + 2, fy + 2], fill=(0, 240, 255, 255))
        d.point([(fx, fy)], fill=(255, 255, 255, 255))
    finalize(img, "snow_lotus.png")

    # mandrake_root (Gnarled Screaming Tuber)
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.polygon([(46, 38), (82, 38), (88, 70), (76, 104), (52, 104), (40, 70)], fill=(175, 125, 70, 255), outline=(85, 55, 20, 255), width=3)
    d.line([(48, 54), (80, 54)], fill=(130, 85, 35, 255), width=2)
    d.line([(46, 68), (82, 68)], fill=(130, 85, 35, 255), width=2)

    d.ellipse([50, 46, 58, 56], fill=(45, 25, 10, 255))
    d.ellipse([70, 46, 78, 56], fill=(45, 25, 10, 255))
    d.ellipse([54, 64, 74, 88], fill=(45, 20, 10, 255), outline=(85, 55, 20, 255), width=2)
    d.ellipse([58, 70, 70, 82], fill=(15, 5, 5, 255))

    d.line([(52, 104), (36, 122)], fill=(160, 115, 60, 255), width=5)
    d.line([(76, 104), (92, 122)], fill=(160, 115, 60, 255), width=5)

    d.line([(64, 38), (64, 18)], fill=(65, 140, 40, 255), width=4)
    d.ellipse([44, 12, 64, 28], fill=(80, 180, 55, 255), outline=(35, 95, 20, 255))
    d.ellipse([64, 12, 84, 28], fill=(80, 180, 55, 255), outline=(35, 95, 20, 255))
    d.ellipse([58, 24, 66, 32], fill=(90, 20, 120, 255))
    finalize(img, "mandrake_root.png")

    # yggdrasil_heart_seed
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    for r in range(54, 32, -4):
        d.ellipse([64 - r, 64 - r, 64 + r, 64 + r], outline=(40, 220, 110, int((54 - r) * 3)), width=2)

    d.polygon([(36, 36), (92, 36), (84, 58), (44, 58)], fill=(100, 65, 30, 255), outline=(50, 30, 10, 255), width=3)
    d.line([(38, 42), (90, 42)], fill=(145, 95, 45, 255), width=2)
    d.line([(64, 36), (64, 16)], fill=(60, 140, 40, 255), width=4)
    d.ellipse([48, 12, 64, 24], fill=(255, 215, 60, 255), outline=(130, 95, 15, 255))
    d.ellipse([64, 12, 80, 24], fill=(75, 220, 110, 255), outline=(25, 95, 40, 255))

    acorn_pts = [(44, 56), (84, 56), (90, 80), (64, 114), (38, 80)]
    d.polygon(acorn_pts, fill=(35, 165, 80, 255), outline=(15, 65, 30, 255), width=3)
    d.polygon([(46, 58), (64, 58), (64, 110), (42, 80)], fill=(75, 225, 130, 255))
    d.polygon([(64, 58), (82, 58), (86, 80), (64, 110)], fill=(30, 145, 70, 255))
    d.line([(64, 58), (64, 108)], fill=(225, 255, 240, 255), width=4)
    d.ellipse([60, 72, 68, 80], fill=(255, 255, 255, 255))
    finalize(img, "yggdrasil_heart_seed.png")

# =========================================================================
# 4. LLAVES, TICKETS, MONEDAS Y PERGAMINOS (128x128 -> 32x32)
# =========================================================================
def gen_all_keys_and_scrolls():
    # 1. dungeon_key (Gothic Dark Steel Demon Skull Key)
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    # Bow: Menacing Demonic Skull
    d.ellipse([20, 20, 60, 60], fill=(45, 35, 55, 255), outline=(15, 10, 20, 255), width=4)
    d.ellipse([26, 26, 54, 54], fill=(75, 60, 90, 255))
    d.ellipse([34, 34, 46, 46], fill=(160, 40, 240, 255), outline=(220, 150, 255, 255), width=2)
    d.point([(38, 38)], fill=(255, 255, 255, 255))
    # Sweeping curved demon horns
    d.polygon([(20, 24), (8, 6), (28, 16)], fill=(30, 20, 35, 255))
    d.polygon([(60, 24), (72, 6), (52, 16)], fill=(30, 20, 35, 255))

    d.line([(50, 50), (102, 102)], fill=(45, 35, 55, 255), width=10)
    d.line([(52, 52), (100, 100)], fill=(140, 130, 155, 255), width=4)
    d.line([(54, 50), (98, 94)], fill=(160, 40, 240, 255), width=2)

    d.line([(90, 90), (104, 76)], fill=(45, 35, 55, 255), width=7)
    d.line([(90, 90), (104, 76)], fill=(140, 130, 155, 255), width=3)
    d.line([(100, 100), (114, 86)], fill=(45, 35, 55, 255), width=7)
    d.line([(100, 100), (114, 86)], fill=(140, 130, 155, 255), width=3)
    finalize(img, "dungeon_key.png")

    # 2. yggdrasil_treant_key
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.ellipse([20, 20, 60, 60], outline=(85, 50, 20, 255), width=8)
    d.ellipse([28, 28, 52, 52], fill=(50, 190, 90, 255), outline=(130, 80, 35, 255), width=2)
    d.ellipse([34, 34, 46, 46], fill=(225, 255, 200, 255))

    d.line([(50, 50), (102, 102)], fill=(110, 70, 30, 255), width=10)
    d.line([(52, 52), (100, 100)], fill=(165, 110, 55, 255), width=4)
    d.ellipse([64, 46, 78, 60], fill=(65, 185, 65, 255), outline=(25, 85, 25, 255))
    d.ellipse([82, 64, 96, 78], fill=(65, 185, 65, 255), outline=(25, 85, 25, 255))
    d.line([(92, 92), (108, 76)], fill=(110, 70, 30, 255), width=7)
    d.ellipse([104, 72, 112, 80], fill=(85, 210, 85, 255))
    finalize(img, "yggdrasil_treant_key.png")

    # 3. jade_ancestral_key
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.ellipse([20, 20, 60, 60], fill=(50, 180, 110, 255), outline=(15, 75, 40, 255), width=6)
    d.ellipse([28, 28, 52, 52], fill=(130, 245, 175, 255), outline=(25, 115, 65, 255), width=3)
    d.ellipse([36, 36, 44, 44], fill=(218, 165, 32, 255))

    d.line([(50, 50), (102, 102)], fill=(45, 175, 105, 255), width=10)
    d.line([(52, 50), (100, 98)], fill=(180, 255, 210, 255), width=3)
    d.line([(60, 66), (66, 60)], fill=(255, 215, 60, 255), width=4)
    d.line([(78, 84), (84, 78)], fill=(255, 215, 60, 255), width=4)

    d.line([(90, 90), (106, 74)], fill=(45, 175, 105, 255), width=7)
    d.line([(90, 90), (106, 74)], fill=(180, 255, 210, 255), width=2)
    d.line([(100, 100), (116, 84)], fill=(45, 175, 105, 255), width=7)

    d.line([(24, 52), (14, 76), (12, 104)], fill=(195, 25, 35, 255), width=5)
    d.line([(24, 52), (18, 78), (22, 104)], fill=(225, 45, 55, 255), width=3)
    d.ellipse([10, 72, 20, 82], fill=(218, 165, 32, 255))
    finalize(img, "jade_ancestral_key.png")

    # 4. elemental_castle_key
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.ellipse([20, 20, 60, 60], fill=(218, 165, 32, 255), outline=(100, 70, 10, 255), width=5)
    d.ellipse([28, 28, 52, 52], fill=(225, 30, 45, 255), outline=(130, 15, 20, 255), width=3)
    d.ellipse([36, 36, 44, 44], fill=(255, 210, 90, 255))

    d.line([(50, 50), (102, 102)], fill=(0, 210, 255, 255), width=10)
    d.line([(52, 50), (100, 98)], fill=(225, 250, 255, 255), width=4)
    d.line([(44, 62), (62, 44)], fill=(40, 210, 90, 255), width=6)

    d.line([(90, 90), (106, 74)], fill=(180, 50, 255, 255), width=7)
    d.line([(90, 90), (106, 74)], fill=(255, 220, 255, 255), width=2)
    d.line([(100, 100), (116, 84)], fill=(180, 50, 255, 255), width=7)
    finalize(img, "elemental_castle_key.png")

    # 5. ranker_tower_ticket (Holographic Gold VIP Pass with ticket punch notches)
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    # Beveled card with side ticket notches
    ticket_poly = [
        (16, 34), (60, 34), (64, 40), (68, 34), (112, 34),
        (104, 94), (68, 94), (64, 88), (60, 94), (8, 94)
    ]
    d.polygon(ticket_poly, fill=(218, 165, 32, 255), outline=(90, 65, 10, 255), width=4)
    # Iridescent Holographic gradient
    d.polygon([(20, 38), (108, 38), (100, 90), (12, 90)], fill=(30, 15, 60, 255))
    d.polygon([(22, 40), (80, 40), (70, 88), (16, 88)], fill=(0, 185, 235, 255))
    d.polygon([(54, 40), (106, 40), (98, 88), (46, 88)], fill=(150, 55, 225, 255))

    # Spire Tower Silhouette
    d.polygon([(56, 46), (64, 42), (72, 46), (68, 80), (60, 80)], fill=(255, 255, 255, 255))
    for bx in range(24, 52, 4):
        d.line([(bx, 76), (bx, 84)], fill=(255, 230, 100, 255), width=2)

    # Red "S-RANK" Wax Stamp
    d.ellipse([78, 52, 98, 72], fill=(195, 20, 25, 255), outline=(110, 10, 15, 255), width=2)
    d.ellipse([82, 56, 94, 68], fill=(245, 50, 65, 255))
    d.point([(88, 62)], fill=(255, 255, 255, 255))
    finalize(img, "ranker_tower_ticket.png")

    # 6. ranker_return_scroll
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.polygon([(28, 28), (96, 96), (82, 110), (14, 42)], fill=(240, 225, 195, 255), outline=(110, 90, 60, 255), width=4)
    d.line([(30, 32), (92, 94)], fill=(255, 245, 225, 255), width=8)

    d.ellipse([10, 34, 26, 50], fill=(210, 190, 150, 255), outline=(90, 70, 45, 255), width=3)
    d.ellipse([82, 94, 98, 110], fill=(210, 190, 150, 255), outline=(90, 70, 45, 255), width=3)
    d.ellipse([14, 38, 22, 46], fill=(150, 130, 95, 255))
    d.ellipse([86, 98, 94, 106], fill=(150, 130, 95, 255))

    d.line([(34, 82), (76, 40)], fill=(0, 220, 255, 255), width=10)
    d.line([(36, 80), (74, 42)], fill=(220, 250, 255, 255), width=4)
    d.ellipse([48, 54, 62, 68], fill=(218, 165, 32, 255), outline=(90, 65, 10, 255), width=2)
    d.ellipse([52, 58, 58, 64], fill=(0, 230, 255, 255))
    finalize(img, "ranker_return_scroll.png")

    # 7. adventurer_medal
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.polygon([(46, 14), (82, 14), (74, 48), (54, 48)], fill=(25, 55, 140, 255), outline=(15, 30, 80, 255), width=2)
    d.line([(48, 16), (56, 46)], fill=(255, 215, 60, 255), width=3)
    d.line([(80, 16), (72, 46)], fill=(255, 215, 60, 255), width=3)
    d.rectangle([42, 12, 86, 18], fill=(180, 190, 205, 255), outline=(60, 65, 75, 255), width=2)

    cx, cy = 64, 76
    for ang in range(0, 360, 45):
        rad = math.radians(ang)
        rad_n = math.radians(ang + 22.5)
        px1 = int(cx + 38 * math.cos(rad))
        py1 = int(cy + 38 * math.sin(rad))
        px2 = int(cx + 20 * math.cos(rad_n))
        py2 = int(cy + 20 * math.sin(rad_n))
        d.polygon([(cx, cy), (px1, py1), (px2, py2)], fill=(255, 215, 60, 255), outline=(110, 80, 15, 255))
        d.polygon([(cx, cy), (px1, py1), (int(cx + 20 * math.cos(rad - 0.39)), int(cy + 20 * math.sin(rad - 0.39)))], fill=(218, 165, 32, 255))

    d.ellipse([cx - 18, cy - 18, cx + 18, cy + 18], fill=(20, 50, 130, 255), outline=(218, 165, 32, 255), width=3)
    d.line([(cx - 10, cy - 10), (cx + 10, cy + 10)], fill=(255, 235, 120, 255), width=3)
    d.line([(cx + 10, cy - 10), (cx - 10, cy + 10)], fill=(255, 235, 120, 255), width=3)
    d.ellipse([cx - 4, cy - 4, cx + 4, cy + 4], fill=(255, 255, 255, 255))
    finalize(img, "adventurer_medal.png")

    # 8. roulette_token
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.ellipse([14, 14, 114, 114], fill=(218, 165, 32, 255), outline=(90, 65, 10, 255), width=4)
    d.ellipse([22, 22, 106, 106], fill=(195, 25, 40, 255), outline=(90, 15, 20, 255), width=2)
    for ang in range(0, 360, 45):
        rad = math.radians(ang)
        nx = int(64 + 42 * math.cos(rad))
        ny = int(64 + 42 * math.sin(rad))
        d.ellipse([nx - 5, ny - 5, nx + 5, ny + 5], fill=(255, 250, 235, 255))

    d.ellipse([34, 34, 94, 94], fill=(25, 20, 35, 255), outline=(255, 220, 70, 255), width=3)
    d.polygon([(46, 72), (82, 72), (84, 52), (72, 60), (64, 46), (56, 60), (44, 52)], fill=(255, 220, 70, 255), outline=(120, 90, 15, 255))
    d.ellipse([60, 58, 68, 66], fill=(225, 25, 45, 255))
    finalize(img, "roulette_token.png")

    # 9. awakening_coin
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    poly = [(42, 14), (86, 14), (114, 42), (114, 86), (86, 114), (42, 114), (14, 86), (14, 42)]
    d.polygon(poly, fill=(218, 165, 32, 255), outline=(90, 65, 10, 255), width=4)
    inner_poly = [(44, 20), (84, 20), (108, 44), (108, 84), (84, 108), (44, 108), (20, 84), (20, 44)]
    d.polygon(inner_poly, fill=(255, 225, 80, 255))
    d.ellipse([28, 28, 100, 100], fill=(235, 185, 35, 255), outline=(130, 95, 15, 255), width=3)

    d.line([(64, 36), (42, 88)], fill=(100, 70, 10, 255), width=7)
    d.line([(64, 36), (86, 88)], fill=(100, 70, 10, 255), width=7)
    d.line([(50, 72), (78, 72)], fill=(100, 70, 10, 255), width=7)
    d.line([(64, 38), (44, 86)], fill=(255, 250, 160, 255), width=3)
    d.line([(64, 38), (84, 86)], fill=(255, 250, 160, 255), width=3)
    d.ellipse([56, 52, 72, 68], fill=(0, 220, 255, 255), outline=(0, 90, 150, 255), width=2)
    d.ellipse([60, 56, 68, 64], fill=(225, 255, 255, 255))
    finalize(img, "awakening_coin.png")

# =========================================================================
# 5. DROPS Y MATERIALES DE MONSTRUOS (128x128 -> 32x32)
# =========================================================================
def gen_all_monster_drops():
    # 1. goblin_ear
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    ear_pts = [(36, 96), (46, 50), (104, 24), (82, 84), (56, 104)]
    d.polygon(ear_pts, fill=(85, 150, 60, 255), outline=(30, 65, 20, 255), width=4)
    inner_pts = [(44, 90), (52, 58), (92, 34), (74, 80), (58, 96)]
    d.polygon(inner_pts, fill=(125, 195, 85, 255))
    d.polygon([(54, 70), (74, 52), (68, 80)], fill=(70, 125, 50, 255))

    for hx, hy in [(96, 28), (88, 44)]:
        d.ellipse([hx - 8, hy - 8, hx + 8, hy + 8], outline=(180, 90, 35, 255), width=4)
        d.ellipse([hx - 4, hy - 4, hx + 4, hy + 4], outline=(235, 150, 80, 255), width=2)

    d.polygon([(34, 94), (46, 88), (56, 102), (40, 106)], fill=(120, 15, 20, 255))
    d.polygon([(36, 96), (44, 92), (52, 102)], fill=(185, 25, 35, 255))
    finalize(img, "goblin_ear.png")

    # 2. beastman_fur
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.polygon([(36, 26), (92, 26), (112, 60), (96, 106), (32, 106), (16, 60)], fill=(85, 45, 20, 255), outline=(35, 18, 8, 255), width=4)
    d.polygon([(40, 30), (88, 30), (106, 60), (90, 100), (38, 100), (22, 60)], fill=(155, 95, 45, 255))

    tufts = [
        [(44, 38), (56, 64), (40, 68)], [(60, 38), (72, 68), (54, 72)], [(76, 38), (88, 64), (72, 68)],
        [(34, 68), (48, 94), (32, 96)], [(54, 68), (68, 96), (52, 98)], [(74, 68), (86, 94), (72, 96)]
    ]
    for pts in tufts:
        d.polygon(pts, fill=(215, 155, 85, 255))
        d.line([(pts[0][0], pts[0][1]), (pts[1][0], pts[1][1])], fill=(255, 215, 150, 255), width=3)

    d.line([(24, 60), (104, 60)], fill=(60, 30, 15, 255), width=4)
    d.ellipse([60, 56, 68, 64], fill=(240, 235, 220, 255))
    finalize(img, "beastman_fur.png")

    # 3. beastman_fang (Diagonal curved saber-tooth)
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    pts = [(28, 22), (64, 18), (86, 38), (108, 98), (104, 108), (82, 102), (48, 62), (24, 34)]
    d.polygon(pts, fill=(245, 240, 230, 255), outline=(75, 70, 60, 255), width=4)
    d.line([(32, 28), (56, 52), (86, 88), (102, 104)], fill=(255, 255, 255, 255), width=6)
    d.line([(42, 38), (66, 62), (92, 94)], fill=(255, 255, 255, 255), width=3)
    d.line([(58, 26), (78, 44), (98, 96)], fill=(205, 195, 175, 255), width=5)
    d.line([(80, 50), (74, 56)], fill=(85, 75, 65, 255), width=3)
    d.line([(94, 76), (88, 80)], fill=(85, 75, 65, 255), width=3)
    root_pts = [(26, 24), (64, 18), (58, 36), (24, 34)]
    d.polygon(root_pts, fill=(160, 25, 30, 255), outline=(85, 10, 15, 255), width=2)
    d.line([(30, 28), (58, 24)], fill=(225, 50, 60, 255), width=3)
    finalize(img, "beastman_fang.png")

    # 4. jade_carapace_scale
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    outer = [(64, 18), (110, 42), (110, 86), (64, 110), (18, 86), (18, 42)]
    d.polygon(outer, fill=(20, 85, 50, 255), outline=(10, 40, 25, 255), width=4)
    inner = [(64, 24), (102, 46), (102, 82), (64, 104), (26, 82), (26, 46)]
    d.polygon(inner, fill=(50, 175, 100, 255))
    for scale in [0.75, 0.5, 0.25]:
        pts = [(int(64 + (p[0] - 64) * scale), int(64 + (p[1] - 64) * scale)) for p in inner]
        d.polygon(pts, outline=(30, 130, 75, 255), width=2)
    d.line([(64, 26), (98, 46)], fill=(180, 255, 210, 255), width=4)
    d.point([(64, 28), (66, 30)], fill=(255, 255, 255, 255))
    finalize(img, "jade_carapace_scale.png")

    # 5. astral_crystal_scale
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    outer = [(64, 18), (110, 42), (110, 86), (64, 110), (18, 86), (18, 42)]
    d.polygon(outer, fill=(35, 15, 65, 255), outline=(15, 8, 30, 255), width=4)
    inner = [(64, 24), (102, 46), (102, 82), (64, 104), (26, 82), (26, 46)]
    d.polygon(inner, fill=(75, 30, 140, 255))
    d.ellipse([42, 42, 86, 86], fill=(0, 180, 240, 160))
    d.ellipse([52, 52, 76, 76], fill=(160, 230, 255, 200))
    stars = [(64, 64), (50, 50), (78, 54), (54, 76), (74, 78), (64, 38)]
    for sx, sy in stars:
        d.ellipse([sx - 2, sy - 2, sx + 2, sy + 2], fill=(255, 255, 255, 255))
    d.line([(50, 50), (64, 64), (78, 54)], fill=(200, 240, 255, 180), width=2)
    d.line([(54, 76), (64, 64), (74, 78)], fill=(200, 240, 255, 180), width=2)
    finalize(img, "astral_crystal_scale.png")

    # 6. immortal_dragon_scale
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    outer = [(64, 16), (112, 38), (106, 90), (64, 114), (22, 90), (16, 38)]
    d.polygon(outer, fill=(30, 10, 12, 255), outline=(10, 5, 5, 255), width=4)
    inner = [(64, 22), (104, 42), (98, 84), (64, 108), (30, 84), (24, 42)]
    d.polygon(inner, fill=(120, 18, 25, 255))
    d.polygon([(64, 26), (88, 48), (82, 80), (64, 102), (46, 80), (40, 48)], fill=(50, 14, 18, 255))

    d.line([(64, 24), (64, 106)], fill=(255, 80, 20, 255), width=6)
    d.line([(64, 24), (64, 106)], fill=(255, 220, 60, 255), width=2)
    d.line([(40, 52), (64, 68), (88, 52)], fill=(255, 120, 30, 255), width=4)
    d.line([(44, 78), (64, 88), (84, 78)], fill=(255, 120, 30, 255), width=4)
    d.point([(60, 36), (68, 36), (64, 64)], fill=(255, 255, 255, 255))
    finalize(img, "immortal_dragon_scale.png")

    # 7. celestial_dragon_pearl
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    cx, cy = 64, 64
    for r in range(54, 36, -3):
        d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(200, 230, 255, int((54 - r) * 4)), width=2)

    d.ellipse([cx - 36, cy - 36, cx + 36, cy + 36], fill=(225, 235, 248, 255), outline=(120, 145, 180, 255), width=4)
    d.ellipse([cx - 32, cy - 32, cx + 28, cy + 28], fill=(242, 248, 255, 255))
    d.arc([cx - 28, cy - 28, cx + 28, cy + 28], start=120, end=260, fill=(160, 230, 255, 255), width=6)
    d.arc([cx - 24, cy - 24, cx + 24, cy + 24], start=300, end=420, fill=(255, 190, 215, 255), width=6)
    d.ellipse([cx - 16, cy - 16, cx + 4, cy + 4], fill=(255, 255, 255, 255))
    d.ellipse([cx - 10, cy - 10, cx - 2, cy - 2], fill=(255, 255, 255, 255))
    finalize(img, "celestial_dragon_pearl.png")

    # 8. bottled_mana_pixie
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.rectangle([52, 14, 76, 26], fill=(160, 110, 60, 255), outline=(70, 45, 20, 255), width=3)
    d.rectangle([48, 26, 80, 38], fill=(218, 165, 32, 255), outline=(90, 65, 10, 255), width=2)
    d.ellipse([24, 34, 104, 114], fill=(180, 235, 255, 130), outline=(60, 115, 160, 255), width=4)
    d.ellipse([30, 62, 98, 108], fill=(0, 210, 255, 190))
    d.ellipse([38, 76, 90, 104], fill=(10, 140, 235, 220))

    d.ellipse([34, 46, 62, 64], fill=(210, 255, 255, 240), outline=(255, 255, 255, 255), width=2)
    d.ellipse([66, 46, 94, 64], fill=(210, 255, 255, 240), outline=(255, 255, 255, 255), width=2)
    d.ellipse([38, 62, 58, 76], fill=(180, 240, 255, 200), outline=(255, 255, 255, 255), width=1)
    d.ellipse([70, 62, 90, 76], fill=(180, 240, 255, 200), outline=(255, 255, 255, 255), width=1)
    d.line([(64, 52), (64, 72)], fill=(255, 255, 255, 255), width=4)
    d.ellipse([60, 44, 68, 52], fill=(255, 255, 255, 255))
    for sx, sy in [(50, 40), (78, 40), (64, 82), (48, 88), (80, 88)]:
        d.point([(sx, sy)], fill=(255, 255, 255, 255))
        d.ellipse([sx-2, sy-2, sx+2, sy+2], fill=(255, 255, 200, 200))
    d.arc([30, 40, 98, 108], start=135, end=215, fill=(255, 255, 255, 240), width=4)
    finalize(img, "bottled_mana_pixie.png")

    # 9. chaos_mana_core
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    cx, cy = 64, 64
    for r in range(54, 34, -4):
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(120, 20, 190, int((54 - r) * 3)))

    d.ellipse([cx - 36, cy - 36, cx + 36, cy + 36], fill=(30, 8, 45, 255), outline=(15, 4, 25, 255), width=4)
    d.ellipse([cx - 26, cy - 26, cx + 26, cy + 26], fill=(175, 25, 220, 255))
    d.ellipse([cx - 16, cy - 16, cx + 16, cy + 16], fill=(0, 240, 255, 255))
    d.ellipse([cx - 8, cy - 8, cx + 8, cy + 8], fill=(255, 255, 255, 255))

    d.line([(34, 44), (54, 58), (46, 76), (82, 68)], fill=(255, 40, 120, 255), width=4)
    d.line([(76, 36), (66, 54), (88, 62)], fill=(0, 255, 255, 255), width=3)
    d.line([(48, 86), (64, 76), (78, 92)], fill=(255, 220, 50, 255), width=3)
    finalize(img, "chaos_mana_core.png")

    # 10. wyvern_scale
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    outer = [(64, 18), (110, 42), (104, 90), (64, 112), (24, 90), (18, 42)]
    d.polygon(outer, fill=(55, 75, 30, 255), outline=(25, 35, 15, 255), width=4)
    inner = [(64, 24), (102, 46), (96, 84), (64, 106), (32, 84), (26, 46)]
    d.polygon(inner, fill=(110, 140, 60, 255))
    d.polygon([(64, 24), (74, 64), (64, 106), (54, 64)], fill=(155, 190, 85, 255))
    d.line([(64, 24), (64, 106)], fill=(215, 245, 145, 255), width=3)
    d.line([(40, 56), (54, 64)], fill=(75, 100, 40, 255), width=2)
    d.line([(88, 56), (74, 64)], fill=(75, 100, 40, 255), width=2)
    finalize(img, "wyvern_scale.png")

    # 11. wyvern_fang (Hooked venom tooth with neon poison canal)
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    pts = [(28, 20), (62, 18), (84, 40), (106, 96), (102, 108), (80, 100), (46, 60), (24, 32)]
    d.polygon(pts, fill=(235, 242, 230, 255), outline=(65, 75, 60, 255), width=4)
    d.line([(32, 26), (54, 50), (84, 86), (100, 102)], fill=(255, 255, 255, 255), width=5)
    d.line([(46, 32), (68, 54), (94, 90), (102, 106)], fill=(120, 240, 30, 255), width=5)
    d.line([(48, 32), (70, 54), (96, 90)], fill=(200, 255, 100, 255), width=2)
    d.ellipse([98, 104, 110, 116], fill=(140, 255, 40, 255), outline=(60, 160, 10, 255), width=2)
    d.point([(102, 108)], fill=(255, 255, 255, 255))
    finalize(img, "wyvern_fang.png")

    # 12. wyvern_saddle (Fantasy Studded Flying Saddle)
    img = new_canvas(128)
    d = ImageDraw.Draw(img)
    d.polygon([(20, 48), (108, 48), (98, 92), (30, 92)], fill=(240, 235, 220, 255), outline=(120, 110, 95, 255), width=3)
    d.polygon([(24, 50), (44, 40), (84, 40), (104, 50), (94, 86), (34, 86)], fill=(115, 60, 25, 255), outline=(50, 25, 10, 255), width=4)
    d.polygon([(30, 52), (48, 44), (80, 44), (98, 52), (88, 82), (40, 82)], fill=(160, 85, 35, 255))

    d.polygon([(26, 34), (42, 34), (38, 52), (24, 52)], fill=(170, 180, 195, 255), outline=(70, 75, 85, 255), width=2)
    d.polygon([(86, 34), (102, 34), (104, 52), (90, 52)], fill=(170, 180, 195, 255), outline=(70, 75, 85, 255), width=2)

    for bx in [36, 52, 64, 76, 92]:
        d.ellipse([bx - 3, 58 - 3, bx + 3, 58 + 3], fill=(255, 215, 60, 255))

    d.line([(42, 86), (42, 110)], fill=(65, 32, 12, 255), width=5)
    d.line([(86, 86), (86, 110)], fill=(65, 32, 12, 255), width=5)
    d.ellipse([34, 106, 50, 122], outline=(170, 180, 195, 255), width=4)
    d.ellipse([78, 106, 94, 122], outline=(170, 180, 195, 255), width=4)
    finalize(img, "wyvern_saddle.png")

def main():
    print("Generating Category 1: Detailed Rune Stones...")
    gen_all_runes()
    print("Generating Category 2: Detailed Relics & Accessories...")
    gen_all_relics()
    print("Generating Category 3: Detailed Murim Pills & Herbs...")
    gen_all_murim()
    print("Generating Category 4: Detailed Keys, Tickets & Scrolls...")
    gen_all_keys_and_scrolls()
    print("Generating Category 5: Detailed Monster Drops & Materials...")
    gen_all_monster_drops()
    print("All 45 detailed custom textures generated successfully!")

if __name__ == "__main__":
    main()
