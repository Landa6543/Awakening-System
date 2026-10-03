import os
import numpy as np
from PIL import Image, ImageDraw

TEX_DIR = "src/main/resources/assets/awakening_system/textures/item"
CASINO_DIR = "src/main/resources/assets/awakening_system/textures/gui/casino/symbols"

os.makedirs(TEX_DIR, exist_ok=True)
os.makedirs(CASINO_DIR, exist_ok=True)

# ==============================================================================
# 1. SLENDER POINTED MONSTER SCALES (32x32)
# ==============================================================================
def generate_scale_image(palette):
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    pixels = img.load()
    
    y_top = 3
    y_tip = 28
    y_wide = 10
    max_w = 8  # maximum width = 16 pixels
    
    inside = np.zeros((32, 32), dtype=bool)
    for y in range(y_top, y_tip + 1):
        if y < y_wide:
            t = (y - y_top) / (y_wide - y_top)
            w = int(round(2.0 + (max_w - 2.0) * np.sin(t * np.pi / 2.0)))
        else:
            t = (y - y_wide) / (y_tip - y_wide)
            w = max(1, int(round(max_w * (1.0 - t**1.15))))
        inside[y, 16 - w : 15 + w + 1] = True

    border = np.zeros((32, 32), dtype=bool)
    for y in range(32):
        for x in range(32):
            if inside[y, x]:
                if (y == 0 or not inside[y-1, x] or 
                    y == 31 or not inside[y+1, x] or 
                    x == 0 or not inside[y, x-1] or 
                    x == 31 or not inside[y, x+1]):
                    border[y, x] = True

    for y in range(y_top, y_tip + 1):
        for x in range(32):
            if not inside[y, x]:
                continue
            if border[y, x]:
                pixels[x, y] = palette['border']
                continue

            dist_keel = abs(x - 15.5)
            is_left = (x < 15.5)

            # Central dorsal keel
            if x == 15:
                pixels[x, y] = palette['keel_hi']
                continue
            elif x == 16:
                pixels[x, y] = palette['keel_lo']
                continue

            # Curved chevron growth rings
            rib_dist = (y - int(dist_keel * 0.7)) % 6
            is_rib = (rib_dist == 0)

            if is_left:
                if is_rib:
                    pixels[x, y] = palette['rib_hi']
                elif y < 10 and dist_keel < 4:
                    pixels[x, y] = palette['body_lit']
                else:
                    pixels[x, y] = palette['body_mid']
            else:
                if is_rib:
                    pixels[x, y] = palette['rib_lo']
                elif dist_keel > 4:
                    pixels[x, y] = palette['border']
                else:
                    pixels[x, y] = palette['body_dark']

    # Specular highlights
    pixels[15, 4] = palette['glint']
    pixels[15, 5] = palette['glint']
    pixels[14, 5] = palette['glint']
    pixels[15, 27] = palette['glint']

    return img

scales_palettes = {
    'immortal_dragon_scale': {
        'border': (28, 6, 8, 255),
        'body_lit': (215, 45, 35, 255),
        'body_mid': (165, 25, 25, 255),
        'body_dark': (105, 15, 20, 255),
        'keel_hi': (255, 175, 40, 255),
        'keel_lo': (210, 85, 20, 255),
        'rib_hi': (245, 100, 30, 255),
        'rib_lo': (135, 20, 20, 255),
        'glint': (255, 240, 180, 255)
    },
    'wyvern_scale': {
        'border': (20, 35, 15, 255),
        'body_lit': (95, 195, 70, 255),
        'body_mid': (65, 150, 48, 255),
        'body_dark': (38, 95, 28, 255),
        'keel_hi': (185, 250, 95, 255),
        'keel_lo': (120, 190, 50, 255),
        'rib_hi': (140, 225, 75, 255),
        'rib_lo': (48, 115, 35, 255),
        'glint': (240, 255, 190, 255)
    },
    'jade_carapace_scale': {
        'border': (10, 42, 28, 255),
        'body_lit': (75, 215, 145, 255),
        'body_mid': (45, 165, 105, 255),
        'body_dark': (25, 105, 65, 255),
        'keel_hi': (185, 255, 225, 255),
        'keel_lo': (100, 210, 155, 255),
        'rib_hi': (135, 240, 185, 255),
        'rib_lo': (35, 130, 80, 255),
        'glint': (255, 255, 255, 255)
    },
    'astral_crystal_scale': {
        'border': (20, 10, 45, 255),
        'body_lit': (65, 175, 245, 255),
        'body_mid': (45, 105, 205, 255),
        'body_dark': (55, 35, 130, 255),
        'keel_hi': (215, 235, 255, 255),
        'keel_lo': (135, 125, 235, 255),
        'rib_hi': (130, 215, 255, 255),
        'rib_lo': (75, 55, 165, 255),
        'glint': (255, 255, 255, 255)
    }
}

for name, pal in scales_palettes.items():
    img = generate_scale_image(pal)
    path = os.path.join(TEX_DIR, f"{name}.png")
    img.save(path)
    print(f"Saved scale: {path}")

# ==============================================================================
# 2. FATE COMMAND SPELL (32x32)
# ==============================================================================
def generate_command_spell():
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    c_out = (110, 0, 15, 255)
    c_base = (200, 10, 25, 255)
    c_glow = (255, 45, 60, 255)
    c_hi = (255, 140, 150, 255)
    c_hot = (255, 235, 240, 255)

    # Central Stiletto Seal (Segment 1)
    blade_poly = [
        (15, 1), (16, 1), (18, 5), (18, 12), (20, 14), (17, 15), (16, 28), (15, 28), (14, 15), (11, 14), (13, 12), (13, 5)
    ]
    d.polygon(blade_poly, fill=c_out)
    blade_in = [
        (15, 2), (16, 2), (17, 5), (17, 11), (19, 14), (16, 14), (16, 27), (15, 27), (15, 14), (12, 14), (14, 11), (14, 5)
    ]
    d.polygon(blade_in, fill=c_base)
    d.line([(15, 3), (15, 26)], fill=c_glow, width=1)
    d.line([(16, 3), (16, 26)], fill=c_glow, width=1)
    d.line([(15, 5), (15, 11)], fill=c_hi, width=1)
    d.point([(15, 7), (16, 7)], fill=c_hot)

    # Left Wing Seal (Segment 2)
    wing_poly = [
        (12, 9), (9, 5), (4, 5), (2, 7), (3, 11), (5, 10), (3, 14), (4, 19), (7, 24), (10, 26),
        (11, 26), (10, 23), (8, 20), (6, 16), (7, 13), (9, 14), (12, 11)
    ]
    d.polygon(wing_poly, fill=c_out)
    wing_in = [
        (11, 9), (9, 6), (5, 6), (3, 7), (4, 10), (6, 10), (4, 14), (5, 18), (7, 23), (9, 25),
        (10, 24), (9, 21), (7, 17), (7, 14), (9, 13), (11, 10)
    ]
    d.polygon(wing_in, fill=c_base)
    d.line([(5, 7), (6, 9), (5, 14), (6, 18), (8, 22)], fill=c_glow, width=1)
    d.point([(5, 7), (5, 8), (5, 14)], fill=c_hi)
    d.point([(5, 7)], fill=c_hot)

    # Right Wing Seal (Segment 3)
    right_poly = [(31 - x, y) for (x, y) in wing_poly]
    d.polygon(right_poly, fill=c_out)
    right_in = [(31 - x, y) for (x, y) in wing_in]
    d.polygon(right_in, fill=c_base)
    d.line([(31 - 5, 7), (31 - 6, 9), (31 - 5, 14), (31 - 6, 18), (31 - 8, 22)], fill=c_glow, width=1)
    d.point([(31 - 5, 7), (31 - 5, 8), (31 - 5, 14)], fill=c_hi)
    d.point([(31 - 5, 7)], fill=c_hot)

    return img

cs_img = generate_command_spell()
cs_path = os.path.join(TEX_DIR, "command_spell.png")
cs_img.save(cs_path)
print(f"Saved Command Spell: {cs_path}")

# ==============================================================================
# 3. TEYVAT VISIONS (7 Elements - 32x32)
# ==============================================================================
def draw_vision_frame(d):
    g_dark = (75, 50, 15, 255)
    g_base = (195, 145, 30, 255)
    g_hi   = (255, 210, 65, 255)
    g_spec = (255, 248, 185, 255)

    # Top loop / crown
    d.polygon([(14, 2), (17, 2), (19, 5), (12, 5)], fill=g_dark)
    d.polygon([(15, 3), (16, 3), (18, 4), (13, 4)], fill=g_hi)

    # Flanking ornate wings
    left_wing = [
        (11, 4), (7, 6), (4, 9), (2, 14), (2, 18), (4, 23), (7, 26), (11, 28),
        (8, 25), (6, 21), (5, 16), (6, 11), (9, 7)
    ]
    d.polygon(left_wing, fill=g_dark)
    right_wing = [(31 - x, y) for (x, y) in left_wing]
    d.polygon(right_wing, fill=g_dark)

    left_wing_in = [
        (10, 5), (7, 7), (5, 10), (3, 15), (3, 17), (5, 22), (7, 24),
        (6, 20), (5, 16), (7, 11), (9, 8)
    ]
    d.polygon(left_wing_in, fill=g_base)
    right_wing_in = [(31 - x, y) for (x, y) in left_wing_in]
    d.polygon(right_wing_in, fill=g_base)

    d.line([(8, 7), (5, 10), (4, 14)], fill=g_hi, width=1)
    d.line([(31-8, 7), (31-5, 10), (31-4, 14)], fill=g_hi, width=1)
    d.point([(8, 7), (31-8, 7)], fill=g_spec)

    # Bottom pendant point
    d.polygon([(12, 28), (19, 28), (16, 30), (15, 30)], fill=g_dark)
    d.polygon([(13, 28), (18, 28), (16, 29), (15, 29)], fill=g_base)
    d.point([(15, 29), (16, 29)], fill=g_hi)

    # Central Bezel
    d.ellipse([7, 7, 24, 24], fill=g_dark, outline=g_dark)
    d.ellipse([8, 8, 23, 23], fill=g_base, outline=g_base)
    d.arc([8, 8, 23, 23], start=135, end=315, fill=g_hi, width=1)
    d.point([(9, 10), (10, 9)], fill=g_spec)

def create_vision(elem_colors, sigil_fn):
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    draw_vision_frame(d)

    bg_dark, bg_mid, bg_light, sigil_bright, sigil_glow = elem_colors

    # Glass sphere
    d.ellipse([9, 9, 22, 22], fill=bg_dark)
    d.ellipse([10, 10, 21, 21], fill=bg_mid)
    d.ellipse([11, 10, 18, 17], fill=bg_light)

    # Elemental Sigil
    sigil_fn(d, sigil_bright, sigil_glow)

    # Glass curvature reflection
    d.arc([10, 10, 21, 21], start=180, end=270, fill=(255, 255, 255, 255), width=1)
    d.point([(11, 11), (12, 10)], fill=(255, 255, 255, 255))
    return img

def sigil_pyro(d, sb, sg):
    d.polygon([(15, 12), (16, 12), (18, 15), (19, 18), (17, 20), (14, 20), (12, 18), (13, 15)], fill=sg)
    d.polygon([(15, 13), (16, 13), (17, 16), (16, 19), (15, 19), (14, 16)], fill=sb)
    d.point([(15, 16), (16, 16), (15, 17)], fill=(255, 255, 255, 255))

def sigil_electro(d, sb, sg):
    # Inazuma double lightning bolt / mitsudomoe
    d.polygon([(16, 11), (18, 11), (15, 15), (18, 15), (14, 21), (15, 16), (13, 16)], fill=sg)
    d.line([(16, 12), (15, 15), (17, 15), (14, 20)], fill=sb, width=1)
    d.point([(15, 15), (16, 15)], fill=(255, 255, 255, 255))

def sigil_cryo(d, sb, sg):
    # Hexagonal snowflake
    d.line([(15, 12), (16, 12), (16, 20), (15, 20)], fill=sg, width=1)
    d.line([(12, 14), (19, 18)], fill=sg, width=1)
    d.line([(12, 18), (19, 14)], fill=sg, width=1)
    d.line([(15, 13), (16, 13), (16, 19), (15, 19)], fill=sb, width=1)
    d.line([(13, 14), (18, 18)], fill=sb, width=1)
    d.line([(13, 18), (18, 14)], fill=sb, width=1)
    d.point([(15, 16), (16, 16)], fill=(255, 255, 255, 255))

def sigil_anemo(d, sb, sg):
    # Tripartite wind swirl / cyclone wings
    d.polygon([(15, 12), (16, 12), (18, 15), (16, 16), (15, 16)], fill=sg)
    d.polygon([(16, 16), (19, 18), (16, 19), (15, 17)], fill=sg)
    d.polygon([(15, 16), (12, 18), (14, 19), (15, 18)], fill=sg)
    d.point([(15, 15), (16, 15)], fill=sb)
    d.point([(15, 16), (16, 16)], fill=(255, 255, 255, 255))

def sigil_hydro(d, sb, sg):
    # Water droplet with ripple wave
    d.polygon([(15, 12), (16, 12), (18, 16), (17, 19), (14, 19), (13, 16)], fill=sg)
    d.polygon([(15, 13), (16, 13), (17, 16), (16, 18), (15, 18), (14, 16)], fill=sb)
    d.point([(15, 15), (16, 15)], fill=(255, 255, 255, 255))

def sigil_geo(d, sb, sg):
    # Diamond shield pillar
    d.polygon([(15, 12), (16, 12), (19, 16), (16, 20), (15, 20), (12, 16)], fill=sg)
    d.polygon([(15, 13), (16, 13), (18, 16), (16, 19), (15, 19), (13, 16)], fill=sb)
    d.point([(15, 16), (16, 16)], fill=(255, 255, 255, 255))

def sigil_dendro(d, sb, sg):
    # Triple sacred leaf
    d.polygon([(15, 12), (16, 12), (17, 15), (15, 16)], fill=sg)
    d.polygon([(15, 16), (18, 17), (16, 19), (15, 17)], fill=sg)
    d.polygon([(15, 16), (13, 17), (14, 19), (15, 17)], fill=sg)
    d.point([(15, 15), (16, 15)], fill=sb)
    d.point([(15, 16), (16, 16)], fill=(255, 255, 255, 255))

visions_data = {
    'vision_pyro': (
        ((110, 15, 10, 255), (190, 45, 15, 255), (245, 105, 30, 255), (255, 220, 80, 255), (255, 140, 30, 255)),
        sigil_pyro
    ),
    'vision_electro': (
        ((65, 15, 110, 255), (135, 35, 195, 255), (185, 75, 240, 255), (255, 200, 255, 255), (210, 110, 255, 255)),
        sigil_electro
    ),
    'vision_cryo': (
        ((15, 70, 120, 255), (35, 135, 210, 255), (95, 200, 250, 255), (235, 255, 255, 255), (150, 230, 255, 255)),
        sigil_cryo
    ),
    'vision_anemo': (
        ((15, 90, 75, 255), (30, 165, 135, 255), (75, 230, 185, 255), (220, 255, 240, 255), (130, 250, 210, 255)),
        sigil_anemo
    ),
    'vision_hydro': (
        ((10, 45, 125, 255), (20, 95, 205, 255), (60, 160, 250, 255), (210, 240, 255, 255), (110, 200, 255, 255)),
        sigil_hydro
    ),
    'vision_geo': (
        ((110, 70, 10, 255), (190, 130, 20, 255), (245, 185, 45, 255), (255, 245, 160, 255), (255, 210, 75, 255)),
        sigil_geo
    ),
    'vision_dendro': (
        ((25, 85, 20, 255), (55, 155, 35, 255), (110, 220, 65, 255), (230, 255, 180, 255), (165, 245, 95, 255)),
        sigil_dendro
    )
}

for vname, (colors, sigil_fn) in visions_data.items():
    vimg = create_vision(colors, sigil_fn)
    vpath = os.path.join(TEX_DIR, f"{vname}.png")
    vimg.save(vpath)
    print(f"Saved Vision: {vpath}")

# ==============================================================================
# 4. LIGHT CONES (5 Iconic 5★ Cards - 32x32)
# ==============================================================================
def generate_card_base(border_colors):
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    b_dark, b_gold, b_hi, b_spec = border_colors
    
    card_outer = [
        (8, 2), (23, 2), (25, 4), (25, 27), (23, 29), (8, 29), (6, 27), (6, 4)
    ]
    d.polygon(card_outer, fill=b_dark)
    
    card_gold = [
        (8, 3), (23, 3), (24, 4), (24, 27), (23, 28), (8, 28), (7, 27), (7, 4)
    ]
    d.polygon(card_gold, fill=b_gold)
    
    d.line([(8, 3), (23, 3)], fill=b_hi, width=1)
    d.line([(7, 4), (7, 27)], fill=b_hi, width=1)
    d.point([(8, 3), (7, 4)], fill=b_spec)
    return img, d

def add_stars(d):
    star_col = (255, 230, 80, 255)
    for sx in [11, 13, 15, 17, 19]:
        d.point([(sx, 27)], fill=star_col)

# 1. Milky Way
def make_milky_way():
    gold = ((60, 45, 15, 255), (200, 155, 40, 255), (255, 215, 75, 255), (255, 250, 190, 255))
    img, d = generate_card_base(gold)
    for y in range(5, 27):
        for x in range(8, 24):
            if (y == 5 or y == 26) and (x == 8 or x == 23): continue
            t = (y - 5) / 21.0
            r = int(25 + 35 * t)
            g = int(10 + 15 * t)
            b = int(50 + 55 * (1 - t))
            d.point([(x, y)], fill=(r, g, b, 255))
    d.line([(9, 23), (12, 19), (15, 15), (18, 11), (22, 7)], fill=(255, 215, 120, 255), width=2)
    d.line([(10, 22), (13, 18), (16, 14), (19, 10), (22, 6)], fill=(255, 245, 200, 255), width=1)
    d.ellipse([13, 13, 17, 17], fill=(255, 120, 160, 255))
    d.ellipse([14, 14, 16, 16], fill=(255, 255, 255, 255))
    for (sx, sy) in [(10, 8), (21, 16), (11, 14), (19, 22)]:
        d.point([(sx, sy)], fill=(255, 255, 255, 255))
    add_stars(d)
    return img

# 2. Along the Passing Shore (Acheron)
def make_passing_shore():
    nihility_border = ((35, 10, 25, 255), (165, 30, 60, 255), (235, 70, 100, 255), (255, 200, 215, 255))
    img, d = generate_card_base(nihility_border)
    for y in range(5, 27):
        for x in range(8, 24):
            if (y == 5 or y == 26) and (x == 8 or x == 23): continue
            t = (y - 5) / 21.0
            r = int(20 + 80 * t)
            g = int(20 * (1 - t))
            b = int(25 * (1 - t))
            d.point([(x, y)], fill=(r, g, b, 255))
    d.line([(22, 6), (18, 12), (13, 19), (9, 24)], fill=(255, 20, 50, 255), width=2)
    d.line([(21, 6), (17, 12), (12, 19), (8, 24)], fill=(255, 150, 170, 255), width=1)
    for (px, py) in [(13, 14), (17, 14), (12, 17), (18, 17), (15, 11), (15, 20)]:
        d.point([(px, py)], fill=(255, 40, 70, 255))
    d.point([(15, 15), (16, 15)], fill=(255, 255, 255, 255))
    add_stars(d)
    return img

# 3. Patience Is All You Need (Kafka)
def make_patience():
    spider_border = ((40, 15, 45, 255), (150, 35, 110, 255), (225, 80, 190, 255), (255, 210, 245, 255))
    img, d = generate_card_base(spider_border)
    for y in range(5, 27):
        for x in range(8, 24):
            if (y == 5 or y == 26) and (x == 8 or x == 23): continue
            t = (y - 5) / 21.0
            r = int(50 - 20 * t)
            g = int(10 + 10 * t)
            b = int(40 + 35 * t)
            d.point([(x, y)], fill=(r, g, b, 255))
    d.line([(8, 6), (23, 25)], fill=(180, 60, 240, 255), width=1)
    d.line([(23, 6), (8, 25)], fill=(180, 60, 240, 255), width=1)
    d.arc([10, 10, 21, 21], start=0, end=360, fill=(230, 120, 255, 255), width=1)
    d.rectangle([14, 14, 17, 17], fill=(255, 60, 150, 255))
    d.point([(15, 15), (16, 15)], fill=(255, 255, 255, 255))
    for (ex, ey) in [(11, 11), (20, 11), (11, 20), (20, 20)]:
        d.point([(ex, ey)], fill=(255, 200, 255, 255))
    add_stars(d)
    return img

# 4. Where Dreams Should Rest (Firefly)
def make_dreams_rest():
    fire_border = ((55, 25, 10, 255), (210, 110, 25, 255), (255, 185, 45, 255), (255, 245, 180, 255))
    img, d = generate_card_base(fire_border)
    for y in range(5, 27):
        for x in range(8, 24):
            if (y == 5 or y == 26) and (x == 8 or x == 23): continue
            t = (y - 5) / 21.0
            r = int(30 + 180 * t)
            g = int(15 + 80 * t)
            b = int(20)
            d.point([(x, y)], fill=(r, g, b, 255))
    wing_cyan = (0, 240, 190, 255)
    wing_core = (200, 255, 240, 255)
    d.polygon([(15, 16), (11, 10), (10, 13), (12, 18)], fill=wing_cyan)
    d.point([(11, 11), (12, 13)], fill=wing_core)
    d.polygon([(16, 16), (20, 10), (21, 13), (19, 18)], fill=wing_cyan)
    d.point([(20, 11), (19, 13)], fill=wing_core)
    d.ellipse([14, 15, 17, 19], fill=(255, 220, 60, 255))
    d.point([(15, 16), (16, 16)], fill=(255, 255, 255, 255))
    add_stars(d)
    return img

# 5. Before Dawn (Jing Yuan)
def make_before_dawn():
    dawn_border = ((50, 40, 10, 255), (215, 170, 35, 255), (255, 230, 80, 255), (255, 255, 210, 255))
    img, d = generate_card_base(dawn_border)
    for y in range(5, 27):
        for x in range(8, 24):
            if (y == 5 or y == 26) and (x == 8 or x == 23): continue
            t = (y - 5) / 21.0
            r = int(30 + 130 * t)
            g = int(45 + 120 * t)
            b = int(70 + 40 * (1 - t))
            d.point([(x, y)], fill=(r, g, b, 255))
    d.line([(15, 6), (16, 6), (16, 22), (15, 22)], fill=(255, 235, 120, 255), width=1)
    d.polygon([(15, 6), (16, 6), (18, 9), (13, 9)], fill=(255, 255, 200, 255))
    d.line([(10, 10), (14, 13), (12, 16), (15, 18)], fill=(255, 240, 100, 255), width=1)
    d.line([(21, 10), (17, 13), (19, 16), (16, 18)], fill=(255, 240, 100, 255), width=1)
    d.point([(15, 13), (16, 13)], fill=(255, 255, 255, 255))
    add_stars(d)
    return img

light_cones = {
    'light_cone_milky_way': make_milky_way(),
    'light_cone_passing_shore': make_passing_shore(),
    'light_cone_patience': make_patience(),
    'light_cone_dreams_rest': make_dreams_rest(),
    'light_cone_before_dawn': make_before_dawn()
}

for lc_name, lc_img in light_cones.items():
    lc_path = os.path.join(TEX_DIR, f"{lc_name}.png")
    lc_img.save(lc_path)
    print(f"Saved Light Cone: {lc_path}")

# Update casino slot machine symbol with clean Milky Way Light Cone
casino_lc_path = os.path.join(CASINO_DIR, "light_cone.png")
light_cones['light_cone_milky_way'].save(casino_lc_path)
print(f"Saved Casino symbol: {casino_lc_path}")

print("\n--- ALL TEXTURES GENERATED SUCCESSFULLY ---")
