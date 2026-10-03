import os
import math
from PIL import Image, ImageDraw

OUT_DIR = r"c:\Users\soybe\Desktop\Mods NeoForge\Sistema\src\main\resources\assets\awakening_system\textures\item"
os.makedirs(OUT_DIR, exist_ok=True)

def new_canvas(size=32):
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))

def finalize(img, filename):
    # Enforce strict pixel art transparency (alpha threshold)
    pix = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = pix[x, y]
            if a < 64:
                pix[x, y] = (0, 0, 0, 0)
            elif a < 255:
                # Solidify antialiasing to preserve crisp pixel look
                pix[x, y] = (r, g, b, 255)
    dest = os.path.join(OUT_DIR, filename)
    img.save(dest)
    print(f"Saved {filename}")

# =========================================================================
# 1. PIEDRAS DE RUNA (32x32 Mystical Hexagonal Tablets)
# =========================================================================
def draw_rune_stone(base_stone_dark, base_stone_mid, base_stone_light, rune_glow, rune_core, rune_type):
    img = new_canvas(32)
    d = ImageDraw.Draw(img)

    # 1. Outer Dark Bevel Outline
    poly = [(16, 2), (28, 8), (28, 23), (16, 29), (4, 23), (4, 8)]
    d.polygon(poly, fill=(20, 22, 28, 255))

    # 2. Main Stone Body
    inner_poly = [(16, 3), (27, 9), (27, 22), (16, 28), (5, 22), (5, 9)]
    d.polygon(inner_poly, fill=base_stone_dark)

    # 3. Facet Shading (Light from Top-Left, Shadow on Bottom-Right)
    # Top-left highlight facet
    d.polygon([(16, 4), (26, 9), (16, 12), (6, 9)], fill=base_stone_light)
    d.polygon([(6, 9), (16, 12), (16, 24), (6, 21)], fill=base_stone_mid)
    d.polygon([(16, 12), (26, 9), (26, 21), (16, 24)], fill=base_stone_dark)

    # Chiseled Border Rim
    d.line([(16, 4), (26, 9)], fill=base_stone_light, width=1)
    d.line([(6, 9), (16, 4)], fill=base_stone_light, width=1)
    d.line([(26, 9), (26, 21)], fill=(15, 15, 20, 255), width=1)
    d.line([(26, 21), (16, 27)], fill=(15, 15, 20, 255), width=1)
    d.line([(16, 27), (6, 21)], fill=(15, 15, 20, 255), width=1)
    d.line([(6, 21), (6, 9)], fill=base_stone_mid, width=1)

    # 4. Engraved Rune Glow & Symbol
    cx, cy = 16, 16
    # Glow aura
    for r in range(6, 1, -1):
        alpha = int(40 + (6 - r) * 35)
        col = (rune_glow[0], rune_glow[1], rune_glow[2], alpha)
        d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=col, width=1)

    if rune_type == "monarch": # Ruler's Authority Crown/Hand
        d.polygon([(16, 9), (21, 14), (18, 14), (18, 21), (14, 21), (14, 14), (11, 14)], fill=rune_core)
        d.polygon([(16, 11), (19, 14), (17, 14), (17, 19), (15, 19), (15, 14), (13, 14)], fill=(255, 255, 255, 255))
        d.point([(10, 11), (16, 8), (22, 11)], fill=(255, 255, 255, 255))
    elif rune_type == "dragon": # Dragon Slit-Eye
        d.ellipse([cx - 6, cy - 4, cx + 6, cy + 4], fill=rune_glow)
        d.ellipse([cx - 4, cy - 3, cx + 4, cy + 3], fill=(255, 220, 50, 255))
        d.polygon([(16, 11), (17, 16), (16, 21), (15, 16)], fill=(30, 10, 10, 255))
        d.point([(15, 14), (16, 14)], fill=(255, 255, 255, 255))
    elif rune_type == "blood": # Blood Claws
        d.line([(12, 11), (14, 21)], fill=rune_core, width=2)
        d.line([(16, 9), (16, 22)], fill=rune_core, width=2)
        d.line([(20, 11), (18, 21)], fill=rune_core, width=2)
        d.point([(16, 10), (16, 11), (12, 12), (20, 12)], fill=(255, 255, 255, 255))
    elif rune_type == "shadow": # Shadow Rift Spiral
        d.ellipse([cx - 5, cy - 5, cx + 5, cy + 5], outline=rune_core, width=2)
        d.line([(16, 11), (16, 21)], fill=(255, 255, 255, 255), width=1)
        d.line([(11, 16), (21, 16)], fill=(255, 255, 255, 255), width=1)
        d.point([(cx, cy)], fill=(255, 255, 255, 255))
    elif rune_type == "wall": # Fourth Wall Prismatic Square
        d.rectangle([11, 11, 21, 21], outline=rune_core, width=2)
        d.rectangle([13, 13, 19, 19], fill=(240, 250, 255, 255))
        d.point([(12, 12), (20, 12), (16, 16)], fill=(255, 255, 255, 255))
    elif rune_type == "iron": # Iron Body Shield
        d.polygon([(16, 10), (22, 12), (21, 18), (16, 22), (11, 18), (10, 12)], fill=rune_core)
        d.polygon([(16, 12), (20, 13), (19, 17), (16, 20), (13, 17), (12, 13)], fill=(255, 255, 255, 255))

    return img

def gen_runes():
    runes = [
        ("rune_stone_rulers_authority.png", (25, 32, 50), (45, 60, 95), (80, 110, 170), (0, 200, 255), (100, 240, 255), "monarch"),
        ("rune_stone_dragon_eyes.png", (45, 20, 22), (85, 35, 38), (140, 50, 55), (255, 60, 30), (255, 180, 50), "dragon"),
        ("rune_stone_bloodthirst.png", (38, 15, 25), (75, 25, 45), (135, 35, 75), (255, 20, 60), (255, 100, 140), "blood"),
        ("rune_stone_shadow_step.png", (28, 18, 45), (55, 32, 85), (100, 55, 150), (160, 60, 255), (220, 150, 255), "shadow"),
        ("rune_stone_fourth_wall.png", (40, 48, 60), (75, 90, 110), (140, 165, 195), (100, 220, 255), (200, 245, 255), "wall"),
        ("rune_stone_iron_body.png", (35, 40, 45), (65, 75, 85), (115, 130, 145), (180, 210, 255), (240, 248, 255), "iron"),
    ]
    for fn, d_c, m_c, l_c, r_g, r_c, sym in runes:
        finalize(draw_rune_stone(d_c, m_c, l_c, r_g, r_c, sym), fn)

# =========================================================================
# 2. RELIQUIAS Y ACCESORIOS (32x32)
# =========================================================================
def gen_relics():
    # 1. demon_monarch_ring
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Ring band
    d.ellipse([6, 12, 26, 26], outline=(30, 25, 35, 255), width=4)
    d.ellipse([7, 13, 25, 25], outline=(75, 65, 85, 255), width=2)
    d.ellipse([8, 14, 24, 24], outline=(145, 135, 160, 255), width=1)
    # Horned Skull Mount
    d.polygon([(16, 5), (25, 9), (22, 16), (10, 16), (7, 9)], fill=(40, 30, 50, 255))
    d.polygon([(16, 6), (23, 10), (21, 15), (11, 15), (9, 10)], fill=(85, 60, 110, 255))
    # Glowing Demon Eye Ruby
    d.ellipse([12, 8, 20, 14], fill=(220, 20, 40, 255), outline=(130, 10, 20, 255))
    d.ellipse([14, 9, 18, 13], fill=(255, 90, 110, 255))
    d.point([(15, 10)], fill=(255, 255, 255, 255))
    finalize(img, "demon_monarch_ring.png")

    # 2. asrahan_mana_chalice
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Golden Chalice Cup
    d.polygon([(8, 6), (24, 6), (21, 16), (11, 16)], fill=(218, 165, 32, 255), outline=(90, 65, 10, 255))
    d.polygon([(10, 7), (22, 7), (20, 15), (12, 15)], fill=(255, 215, 60, 255))
    # Mana Liquid (Glowing Cyan-Violet)
    d.polygon([(11, 9), (21, 9), (19, 14), (13, 14)], fill=(120, 50, 230, 255))
    d.polygon([(12, 10), (20, 10), (18, 13), (14, 13)], fill=(0, 220, 255, 255))
    d.point([(15, 11), (17, 10)], fill=(255, 255, 255, 255))
    # Stem & Foot
    d.rectangle([14, 16, 18, 23], fill=(218, 165, 32, 255), outline=(90, 65, 10, 255))
    d.rectangle([15, 17, 17, 22], fill=(255, 215, 60, 255))
    d.polygon([(9, 24), (23, 24), (25, 27), (7, 27)], fill=(218, 165, 32, 255), outline=(90, 65, 10, 255))
    d.line([(10, 25), (22, 25)], fill=(255, 240, 150, 255))
    finalize(img, "asrahan_mana_chalice.png")

    # 3. king_slayer_necklace
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Silver chain
    d.arc([7, 4, 25, 18], start=20, end=160, fill=(180, 190, 205, 255), width=2)
    d.arc([8, 5, 24, 17], start=25, end=155, fill=(235, 242, 250, 255), width=1)
    # Golden tooth cap
    d.rectangle([13, 14, 19, 17], fill=(218, 165, 32, 255), outline=(110, 80, 15, 255))
    d.point([(14, 15), (17, 15)], fill=(255, 235, 130, 255))
    # Predator curved fang
    d.polygon([(14, 17), (18, 17), (18, 23), (16, 28), (14, 22)], fill=(245, 240, 230, 255), outline=(130, 120, 110, 255))
    d.line([(15, 18), (15, 24), (16, 27)], fill=(255, 255, 255, 255))
    # Blood edge
    d.point([(16, 26), (16, 27)], fill=(180, 20, 30, 255))
    finalize(img, "king_slayer_necklace.png")

    # 4. phoenix_feather
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Diagonal Flaming Feather
    # Quill
    d.line([(8, 24), (24, 8)], fill=(255, 240, 160, 255), width=2)
    # Barbs with fire gradient
    for i in range(14):
        x = 9 + i
        y = 23 - i
        # Left vane (flame orange to red)
        d.line([(x - 2, y + 2), (x - 6, y - 2)], fill=(255, 80, 20, 255), width=2)
        d.line([(x - 1, y + 1), (x - 4, y - 1)], fill=(255, 180, 40, 255), width=1)
        # Right vane (golden yellow)
        d.line([(x + 2, y - 2), (x + 6, y + 2)], fill=(255, 130, 20, 255), width=2)
        d.line([(x + 1, y - 1), (x + 4, y + 1)], fill=(255, 220, 60, 255), width=1)
    # Glow points
    d.point([(24, 7), (25, 8), (16, 16)], fill=(255, 255, 255, 255))
    finalize(img, "phoenix_feather.png")

    # 5. elemental_demon_crown
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Horned Obsidian Crown
    d.polygon([(4, 18), (8, 9), (12, 17), (16, 7), (20, 17), (24, 9), (28, 18), (25, 24), (7, 24)], fill=(35, 25, 45, 255), outline=(15, 10, 20, 255))
    d.polygon([(6, 18), (8, 11), (12, 16), (16, 9), (20, 16), (24, 11), (26, 18), (23, 22), (9, 22)], fill=(65, 45, 80, 255))
    # 5 Inlaid Elemental Gems
    gems = [
        (9, 19, (255, 40, 40, 255)),   # Pyro
        (13, 19, (0, 220, 255, 255)),   # Cryo
        (16, 17, (255, 220, 0, 255)),   # Geo/Sol
        (19, 19, (180, 50, 255, 255)),  # Electro
        (23, 19, (50, 230, 120, 255)),  # Anemo
    ]
    for gx, gy, col in gems:
        d.rectangle([gx - 1, gy - 1, gx + 1, gy + 1], fill=col)
        d.point([(gx, gy)], fill=(255, 255, 255, 255))
    finalize(img, "elemental_demon_crown.png")

# =========================================================================
# 3. PÍLDORAS, ALQUIMIA Y HIERBAS MURIM (32x32)
# =========================================================================
def draw_pill(glow_color, mid_color, light_color, swirl_color, filename):
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    cx, cy = 16, 16
    # Aura
    for r in range(12, 7, -1):
        alpha = int(25 + (12 - r) * 20)
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(glow_color[0], glow_color[1], glow_color[2], alpha))
    # Sphere Base
    d.ellipse([cx - 7, cy - 7, cx + 7, cy + 7], fill=mid_color, outline=(20, 20, 25, 255), width=2)
    # Sphere Shading
    d.ellipse([cx - 6, cy - 6, cx + 5, cy + 5], fill=mid_color)
    d.ellipse([cx - 5, cy - 5, cx + 2, cy + 2], fill=light_color)
    # Swirling Qi Pattern
    d.arc([cx - 5, cy - 4, cx + 5, cy + 4], start=30, end=210, fill=swirl_color, width=2)
    d.arc([cx - 3, cy - 2, cx + 3, cy + 2], start=210, end=390, fill=(255, 255, 255, 255), width=1)
    # Specular shine
    d.point([(cx - 3, cy - 3), (cx - 2, cy - 4)], fill=(255, 255, 255, 255))
    finalize(img, filename)

def gen_murim():
    draw_pill((255, 100, 30), (220, 40, 20, 255), (255, 140, 60, 255), (255, 220, 80, 255), "rising_dragon_pill.png")
    draw_pill((40, 220, 140), (25, 150, 80, 255), (80, 230, 150, 255), (200, 255, 230, 255), "marrow_cleansing_pill.png")
    draw_pill((160, 60, 255), (105, 30, 180, 255), (180, 90, 255, 255), (235, 190, 255, 255), "soul_transformation_pill.png")
    draw_pill((255, 215, 0), (218, 145, 15, 255), (255, 230, 80, 255), (255, 255, 200, 255), "realm_breakthrough_pill.png")
    draw_pill((20, 100, 160), (15, 60, 110, 255), (45, 140, 210, 255), (140, 220, 255, 255), "black_tortoise_pill.png")

    # wu_xing_manual (Ancient Bound Silk Book)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Cover
    d.rectangle([6, 5, 25, 27], fill=(25, 45, 90, 255), outline=(15, 25, 50, 255), width=2)
    # Spine & Thread Binding
    d.line([(9, 5), (9, 27)], fill=(180, 140, 40, 255), width=2)
    for y in [8, 14, 20, 25]:
        d.line([(6, y), (11, y)], fill=(240, 220, 160, 255), width=1)
    # Title Label
    d.rectangle([13, 8, 17, 23], fill=(245, 240, 225, 255), outline=(140, 40, 30, 255))
    d.line([(15, 10), (15, 21)], fill=(140, 40, 30, 255), width=1)
    # Yin-Yang / Five Elements Crest
    d.ellipse([19, 14, 23, 18], fill=(218, 165, 32, 255))
    finalize(img, "wu_xing_manual.png")

    # murim_ascension_talisman & taoist_talisman (Yellow Paper with Cinnabar Runes)
    for fn, is_asc in [("murim_ascension_talisman.png", True), ("taoist_talisman.png", False)]:
        img = new_canvas(32)
        d = ImageDraw.Draw(img)
        # Yellow Paper Strip
        paper_col = (255, 225, 85, 255) if is_asc else (240, 210, 75, 255)
        d.rectangle([9, 4, 22, 27], fill=paper_col, outline=(150, 115, 25, 255), width=1)
        # Cinnabar Rune Script
        d.line([(15, 7), (15, 24)], fill=(195, 25, 25, 255), width=2)
        d.line([(11, 10), (20, 10)], fill=(195, 25, 25, 255), width=1)
        d.line([(12, 14), (19, 14)], fill=(195, 25, 25, 255), width=2)
        d.line([(11, 19), (20, 19)], fill=(195, 25, 25, 255), width=1)
        d.rectangle([13, 21, 18, 25], fill=(195, 25, 25, 255))
        d.point([(14, 22), (17, 24)], fill=(255, 220, 80, 255))
        if is_asc: # Flaming edges
            d.point([(9, 4), (22, 4), (10, 5), (21, 5)], fill=(255, 100, 30, 255))
        finalize(img, fn)

    # thousand_year_ginseng
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Gnarled Humanoid Root Body
    d.polygon([(15, 12), (17, 12), (19, 18), (17, 24), (14, 24), (13, 18)], fill=(225, 180, 105, 255), outline=(110, 75, 25, 255))
    # Legs / Rootlets
    d.line([(14, 24), (11, 29)], fill=(200, 155, 85, 255), width=2)
    d.line([(17, 24), (20, 29)], fill=(200, 155, 85, 255), width=2)
    d.line([(11, 29), (9, 31)], fill=(170, 130, 65, 255), width=1)
    d.line([(20, 29), (22, 31)], fill=(170, 130, 65, 255), width=1)
    # Wispy Rootlets
    for rx, ry, ex, ey in [(13, 16, 8, 20), (19, 16, 23, 20), (14, 20, 9, 25), (18, 20, 23, 25)]:
        d.line([(rx, ry), (ex, ey)], fill=(210, 165, 95, 255), width=1)
    # Green Leaves & Red Berries
    d.line([(16, 12), (16, 7)], fill=(65, 140, 45, 255), width=2)
    d.ellipse([11, 4, 15, 8], fill=(75, 175, 55, 255))
    d.ellipse([17, 4, 21, 8], fill=(75, 175, 55, 255))
    d.ellipse([14, 3, 18, 6], fill=(95, 205, 65, 255))
    d.point([(15, 6), (17, 6)], fill=(225, 30, 30, 255)) # Berries
    finalize(img, "thousand_year_ginseng.png")

    # wild_ginseng (Simpler slender ginseng)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.line([(16, 10), (16, 22)], fill=(215, 175, 100, 255), width=3)
    d.line([(15, 22), (13, 28)], fill=(185, 145, 75, 255), width=1)
    d.line([(17, 22), (19, 28)], fill=(185, 145, 75, 255), width=1)
    d.ellipse([13, 5, 18, 10], fill=(65, 160, 45, 255))
    finalize(img, "wild_ginseng.png")

    # snow_lotus (Frozen crystalline lotus)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Frost Petals
    petals = [(16, 7), (11, 12), (21, 12), (9, 19), (23, 19), (16, 23)]
    for px, py in petals:
        d.polygon([(16, 17), (px - 3, py), (px, py - 3), (px + 3, py)], fill=(220, 245, 255, 255), outline=(90, 170, 210, 255))
    d.ellipse([14, 15, 18, 19], fill=(130, 225, 255, 255))
    d.point([(15, 16), (16, 17)], fill=(255, 255, 255, 255))
    finalize(img, "snow_lotus.png")

    # mandrake_root (Screeching Root)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.polygon([(13, 11), (19, 11), (21, 17), (19, 24), (13, 24), (11, 17)], fill=(175, 125, 75, 255), outline=(85, 55, 25, 255))
    # Screaming face
    d.ellipse([14, 14, 15, 16], fill=(50, 30, 15, 255))
    d.ellipse([17, 14, 18, 16], fill=(50, 30, 15, 255))
    d.ellipse([15, 18, 17, 22], fill=(50, 30, 15, 255))
    # Leafy Sprout
    d.line([(16, 11), (16, 5)], fill=(65, 145, 45, 255), width=2)
    d.ellipse([12, 3, 16, 7], fill=(85, 180, 55, 255))
    d.ellipse([16, 3, 20, 7], fill=(85, 180, 55, 255))
    finalize(img, "mandrake_root.png")

    # yggdrasil_heart_seed (Pulsing Emerald Heart Seed)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Glow
    d.ellipse([8, 8, 24, 24], fill=(40, 210, 100, 60))
    # Acorn/Seed Form
    d.polygon([(16, 8), (24, 14), (21, 23), (16, 27), (11, 23), (8, 14)], fill=(45, 185, 95, 255), outline=(15, 75, 35, 255), width=2)
    d.polygon([(16, 10), (22, 14), (19, 21), (16, 25), (13, 21), (10, 14)], fill=(110, 240, 155, 255))
    d.line([(16, 10), (16, 25)], fill=(225, 255, 235, 255), width=1)
    d.point([(15, 13), (16, 14)], fill=(255, 255, 255, 255))
    finalize(img, "yggdrasil_heart_seed.png")

# =========================================================================
# 4. LLAVES, TICKETS, MONEDAS Y PERGAMINOS (32x32)
# =========================================================================
def gen_keys_and_scrolls():
    # 1. dungeon_key (Ominous Gate Key with Purple Runes)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Bow (Handle)
    d.ellipse([6, 6, 16, 16], outline=(35, 25, 45, 255), width=3)
    d.ellipse([8, 8, 14, 14], fill=(140, 50, 230, 255), outline=(60, 40, 80, 255))
    d.point([(11, 11)], fill=(255, 200, 255, 255))
    # Shaft
    d.line([(14, 14), (25, 25)], fill=(120, 115, 135, 255), width=3)
    d.line([(15, 14), (25, 24)], fill=(200, 195, 215, 255), width=1)
    # Bit (Teeth)
    d.line([(22, 22), (25, 19)], fill=(120, 115, 135, 255), width=2)
    d.line([(25, 25), (28, 22)], fill=(120, 115, 135, 255), width=2)
    finalize(img, "dungeon_key.png")

    # 2. yggdrasil_treant_key (Living Bark Key)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.ellipse([6, 6, 16, 16], outline=(75, 45, 20, 255), width=3)
    d.ellipse([8, 8, 14, 14], fill=(50, 180, 80, 255))
    d.line([(14, 14), (25, 25)], fill=(110, 70, 35, 255), width=3)
    d.ellipse([19, 13, 23, 17], fill=(80, 220, 100, 255)) # Leaf sprout
    d.line([(23, 23), (26, 20)], fill=(110, 70, 35, 255), width=2)
    finalize(img, "yggdrasil_treant_key.png")

    # 3. jade_ancestral_key (Imperial Jade Key with Silk Tassel)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.ellipse([6, 6, 16, 16], outline=(20, 75, 40, 255), width=3)
    d.ellipse([7, 7, 15, 15], fill=(70, 215, 125, 255))
    d.line([(14, 14), (25, 25)], fill=(70, 215, 125, 255), width=3)
    d.line([(14, 13), (24, 23)], fill=(185, 255, 210, 255), width=1)
    d.line([(22, 22), (25, 19)], fill=(50, 170, 95, 255), width=2)
    # Red tassel
    d.line([(7, 15), (4, 21), (3, 26)], fill=(210, 30, 40, 255), width=2)
    finalize(img, "jade_ancestral_key.png")

    # 4. elemental_castle_key (Prismatic Key)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.ellipse([6, 6, 16, 16], outline=(40, 50, 70, 255), width=3)
    d.ellipse([7, 7, 15, 15], fill=(0, 230, 255, 255))
    d.line([(14, 14), (25, 25)], fill=(255, 215, 60, 255), width=3)
    d.line([(22, 22), (25, 19)], fill=(255, 60, 60, 255), width=2)
    d.line([(24, 24), (27, 21)], fill=(180, 50, 255, 255), width=2)
    finalize(img, "elemental_castle_key.png")

    # 5. ranker_tower_ticket (Holographic Golden Tower Pass)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.polygon([(5, 10), (27, 10), (25, 22), (3, 22)], fill=(218, 165, 32, 255), outline=(85, 60, 10, 255), width=1)
    d.polygon([(6, 11), (25, 11), (24, 21), (5, 21)], fill=(255, 230, 100, 255))
    # Holographic Spire Crest
    d.polygon([(14, 13), (16, 12), (18, 13), (16, 19)], fill=(0, 190, 255, 255))
    d.line([(8, 14), (8, 18)], fill=(130, 95, 20, 255), width=1)
    d.line([(10, 14), (10, 18)], fill=(130, 95, 20, 255), width=1)
    d.line([(21, 14), (21, 18)], fill=(130, 95, 20, 255), width=1)
    finalize(img, "ranker_tower_ticket.png")

    # 6. ranker_return_scroll (Magic Parchment with Blue Mana Ribbon)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Diagonal Parchment Roll
    d.polygon([(8, 8), (22, 22), (18, 26), (4, 12)], fill=(235, 220, 185, 255), outline=(130, 110, 80, 255))
    d.line([(9, 9), (21, 21)], fill=(255, 245, 220, 255), width=2)
    # Rolled ends
    d.ellipse([6, 6, 10, 10], fill=(210, 190, 150, 255), outline=(100, 80, 50, 255))
    d.ellipse([20, 20, 24, 24], fill=(210, 190, 150, 255), outline=(100, 80, 50, 255))
    # Mana Ribbon (Glowing Cyan)
    d.line([(10, 18), (18, 10)], fill=(0, 210, 255, 255), width=3)
    d.line([(11, 17), (17, 11)], fill=(200, 250, 255, 255), width=1)
    finalize(img, "ranker_return_scroll.png")

    # 7. adventurer_medal (Gold Guild Medal)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Blue Ribbon V-shape
    d.polygon([(11, 5), (15, 12), (12, 12), (8, 5)], fill=(30, 80, 190, 255))
    d.polygon([(21, 5), (17, 12), (20, 12), (24, 5)], fill=(30, 80, 190, 255))
    # Gold Medal
    d.ellipse([10, 12, 22, 24], fill=(218, 165, 32, 255), outline=(90, 65, 10, 255), width=2)
    d.ellipse([12, 14, 20, 22], fill=(255, 220, 65, 255))
    # Winged Star
    d.point([(16, 16), (15, 17), (16, 17), (17, 17), (16, 18)], fill=(255, 255, 255, 255))
    finalize(img, "adventurer_medal.png")

    # 8. roulette_token (Casino Chip)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.ellipse([6, 6, 26, 26], fill=(218, 165, 32, 255), outline=(85, 60, 10, 255), width=2)
    # Red & Black Rim segments
    d.ellipse([8, 8, 24, 24], fill=(210, 30, 45, 255))
    d.ellipse([11, 11, 21, 21], fill=(20, 20, 25, 255))
    d.ellipse([13, 13, 19, 19], fill=(255, 215, 60, 255))
    d.point([(16, 15), (15, 16), (16, 16), (17, 16), (16, 17)], fill=(255, 255, 255, 255))
    finalize(img, "roulette_token.png")

    # 9. awakening_coin (Iconic System Currency)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Octagonal Gold Coin
    poly = [(12, 5), (20, 5), (27, 12), (27, 20), (20, 27), (12, 27), (5, 20), (5, 12)]
    d.polygon(poly, fill=(218, 165, 32, 255), outline=(90, 65, 10, 255))
    inner_poly = [(12, 7), (20, 7), (25, 12), (25, 20), (20, 25), (12, 25), (7, 20), (7, 12)]
    d.polygon(inner_poly, fill=(255, 220, 65, 255))
    # Monarch's "A" / Eye Emblem
    d.line([(16, 10), (11, 22)], fill=(90, 65, 10, 255), width=2)
    d.line([(16, 10), (21, 22)], fill=(90, 65, 10, 255), width=2)
    d.line([(13, 18), (19, 18)], fill=(90, 65, 10, 255), width=2)
    d.point([(16, 14)], fill=(0, 220, 255, 255)) # Cyan eye core
    finalize(img, "awakening_coin.png")

# =========================================================================
# 5. DROPS Y MATERIALES DE MONSTRUOS (32x32)
# =========================================================================
def gen_monster_drops():
    # 1. goblin_ear (Pointed green ear with copper ring)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.polygon([(9, 23), (12, 13), (25, 7), (19, 21), (14, 25)], fill=(85, 150, 60, 255), outline=(35, 70, 25, 255), width=2)
    d.polygon([(11, 22), (13, 15), (22, 10), (18, 20), (14, 23)], fill=(120, 185, 80, 255))
    # Copper hoop earring
    d.ellipse([7, 21, 12, 26], outline=(190, 95, 45, 255), width=2)
    finalize(img, "goblin_ear.png")

    # 2. beastman_fur (Thick wild fur pelt)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.polygon([(9, 8), (23, 8), (27, 16), (22, 26), (10, 26), (5, 16)], fill=(125, 75, 40, 255), outline=(60, 35, 15, 255), width=2)
    d.polygon([(11, 10), (21, 10), (24, 16), (20, 24), (12, 24), (8, 16)], fill=(185, 125, 70, 255))
    # Tuft streaks
    d.line([(14, 12), (12, 22)], fill=(230, 180, 120, 255), width=2)
    d.line([(18, 12), (17, 22)], fill=(230, 180, 120, 255), width=2)
    finalize(img, "beastman_fur.png")

    # 3. beastman_fang (Predator canine)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.polygon([(11, 7), (21, 7), (19, 14), (16, 26), (13, 14)], fill=(245, 242, 235, 255), outline=(100, 95, 90, 255), width=2)
    d.line([(14, 9), (15, 23)], fill=(255, 255, 255, 255), width=2)
    d.rectangle([11, 7, 21, 10], fill=(160, 40, 40, 255)) # Bloodied gum root
    finalize(img, "beastman_fang.png")

    # 4. jade_carapace_scale (Hexagonal lustrous jade plate)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    poly = [(16, 5), (26, 11), (26, 21), (16, 27), (6, 21), (6, 11)]
    d.polygon(poly, fill=(30, 110, 65, 255), outline=(10, 45, 25, 255), width=2)
    inner = [(16, 8), (23, 12), (23, 20), (16, 24), (9, 20), (9, 12)]
    d.polygon(inner, fill=(55, 175, 105, 255))
    d.line([(16, 9), (21, 13)], fill=(160, 245, 195, 255), width=2)
    finalize(img, "jade_carapace_scale.png")

    # 5. astral_crystal_scale (Cosmic Star Scale)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    poly = [(16, 5), (26, 11), (26, 21), (16, 27), (6, 21), (6, 11)]
    d.polygon(poly, fill=(35, 20, 65, 255), outline=(15, 10, 30, 255), width=2)
    inner = [(16, 8), (23, 12), (23, 20), (16, 24), (9, 20), (9, 12)]
    d.polygon(inner, fill=(90, 50, 160, 255))
    # Stardust gleam
    d.point([(16, 16), (15, 16), (16, 15), (14, 12), (20, 18)], fill=(0, 230, 255, 255))
    d.point([(16, 16), (19, 13)], fill=(255, 255, 255, 255))
    finalize(img, "astral_crystal_scale.png")

    # 6. immortal_dragon_scale (Jagged crimson dragon scale)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.polygon([(16, 4), (27, 10), (25, 22), (16, 28), (7, 22), (5, 10)], fill=(120, 15, 25, 255), outline=(40, 10, 15, 255), width=2)
    d.polygon([(16, 7), (24, 12), (22, 20), (16, 25), (10, 20), (8, 12)], fill=(205, 35, 45, 255))
    # Magma veins
    d.line([(16, 8), (16, 24)], fill=(255, 140, 20, 255), width=2)
    d.line([(12, 14), (16, 17), (20, 14)], fill=(255, 220, 50, 255), width=1)
    finalize(img, "immortal_dragon_scale.png")

    # 7. celestial_dragon_pearl (Pearlescent White-Rainbow Orb)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    cx, cy = 16, 16
    for r in range(12, 8, -1):
        alpha = int(30 + (12 - r) * 30)
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(220, 235, 255, alpha))
    d.ellipse([cx - 8, cy - 8, cx + 8, cy + 8], fill=(230, 238, 250, 255), outline=(130, 145, 175, 255), width=2)
    d.ellipse([cx - 7, cy - 7, cx + 5, cy + 5], fill=(245, 250, 255, 255))
    d.ellipse([cx - 5, cy - 5, cx + 1, cy + 1], fill=(255, 255, 255, 255))
    # Rainbow sheen
    d.point([(cx + 2, cy - 2)], fill=(255, 200, 220, 255))
    d.point([(cx - 2, cy + 2)], fill=(180, 240, 255, 255))
    finalize(img, "celestial_dragon_pearl.png")

    # 8. bottled_mana_pixie (Potion bottle with winged sprite)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    # Cork & Neck
    d.rectangle([14, 5, 18, 7], fill=(160, 110, 60, 255))
    d.rectangle([13, 8, 19, 11], fill=(180, 215, 235, 255), outline=(75, 115, 140, 255))
    # Bottle Body
    d.ellipse([8, 11, 24, 27], fill=(210, 235, 250, 140), outline=(75, 115, 140, 255), width=2)
    # Glowing Cyan Pixie
    d.ellipse([14, 17, 18, 21], fill=(0, 240, 255, 255))
    d.point([(16, 18)], fill=(255, 255, 255, 255))
    # Wings
    d.ellipse([10, 15, 15, 19], fill=(180, 255, 255, 200))
    d.ellipse([17, 15, 22, 19], fill=(180, 255, 255, 200))
    finalize(img, "bottled_mana_pixie.png")

    # 9. chaos_mana_core (Crackling Dark Purple Chaos Core)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    cx, cy = 16, 16
    d.ellipse([cx - 10, cy - 10, cx + 10, cy + 10], fill=(120, 20, 180, 60))
    d.ellipse([cx - 8, cy - 8, cx + 8, cy + 8], fill=(50, 10, 75, 255), outline=(25, 5, 40, 255), width=2)
    d.ellipse([cx - 6, cy - 6, cx + 6, cy + 6], fill=(145, 25, 210, 255))
    # Crackling red/cyan lightning arcs
    d.line([(10, 13), (15, 16), (13, 20), (22, 18)], fill=(255, 40, 90, 255), width=2)
    d.line([(18, 10), (16, 14), (21, 16)], fill=(0, 240, 255, 255), width=1)
    d.point([(16, 15)], fill=(255, 255, 255, 255))
    finalize(img, "chaos_mana_core.png")

    # 10. wyvern_scale (Olive-Green Wyvern Scale)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    poly = [(16, 5), (26, 11), (25, 22), (16, 27), (7, 22), (6, 11)]
    d.polygon(poly, fill=(75, 95, 45, 255), outline=(35, 45, 20, 255), width=2)
    inner = [(16, 8), (23, 12), (22, 20), (16, 24), (10, 20), (9, 12)]
    d.polygon(inner, fill=(125, 150, 70, 255))
    d.line([(16, 9), (16, 23)], fill=(175, 205, 100, 255), width=1)
    finalize(img, "wyvern_scale.png")

    # 11. wyvern_fang (Hooked venom tooth)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.polygon([(12, 7), (21, 7), (22, 15), (17, 27), (13, 18)], fill=(235, 235, 220, 255), outline=(80, 80, 70, 255), width=2)
    d.line([(14, 9), (16, 24)], fill=(255, 255, 255, 255), width=2)
    # Venom tip
    d.point([(17, 26), (16, 25)], fill=(120, 220, 40, 255))
    finalize(img, "wyvern_fang.png")

    # 12. wyvern_saddle (Heavy Leather Flight Saddle)
    img = new_canvas(32)
    d = ImageDraw.Draw(img)
    d.polygon([(6, 11), (26, 11), (24, 21), (8, 21)], fill=(110, 60, 25, 255), outline=(50, 25, 10, 255), width=2)
    d.polygon([(9, 13), (23, 13), (21, 19), (11, 19)], fill=(155, 95, 45, 255))
    # Metal Horn / Cantle & Stirrup Straps
    d.rectangle([13, 8, 19, 12], fill=(160, 165, 180, 255), outline=(70, 75, 85, 255))
    d.line([(11, 21), (11, 27)], fill=(60, 35, 15, 255), width=2)
    d.line([(21, 21), (21, 27)], fill=(60, 35, 15, 255), width=2)
    d.ellipse([9, 26, 13, 29], fill=(160, 165, 180, 255))
    d.ellipse([19, 26, 23, 29], fill=(160, 165, 180, 255))
    finalize(img, "wyvern_saddle.png")

def main():
    print("Generating Category 1: Rune Stones...")
    gen_runes()
    print("Generating Category 2: Relics & Accessories...")
    gen_relics()
    print("Generating Category 3: Murim Pills & Herbs...")
    gen_murim()
    print("Generating Category 4: Keys, Tickets & Scrolls...")
    gen_keys_and_scrolls()
    print("Generating Category 5: Monster Drops & Materials...")
    gen_monster_drops()
    print("Done! All 45 custom textures successfully updated!")

if __name__ == "__main__":
    main()
